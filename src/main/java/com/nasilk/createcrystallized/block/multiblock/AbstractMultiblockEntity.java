package com.nasilk.createcrystallized.block.multiblock;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public abstract class AbstractMultiblockEntity extends BlockEntity implements TransformableBlockEntity {
    // Variables
    private Size multiblockSize = Size.SINGLE;
    private BlockPos controllerPos = null;
    private boolean multiblockUpdate = true;

    // Constants
    private static final long STATE_VALIDATION_INTERVAL = 6000L;
    protected final int worldPositionHash;
    public enum Size {
        SINGLE(1), DOUBLE(2), TRIPLE(3);
        private final int size;
        Size(int size) { this.size = size; }
        public int getSize() { return size; }
        public static Size of(int size) {
            return switch (size) {
                case 2 -> DOUBLE;
                case 3 -> TRIPLE;
                default -> SINGLE;
            };
        }
    }

    protected AbstractMultiblockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.worldPositionHash = pos.hashCode();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level == null || level.isClientSide)) setMultiblockUpdate();
    }

    @Override
    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        if (!(level == null || level.isClientSide)) setMultiblockUpdate();
    }


    // MULTIBLOCK TICK HANDLING
    public final void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            clientTick(level);
            return;
        }
        else if (consumeMultiblockUpdate()) {
            updateMultiblockTick((ServerLevel) level);
            return;
        }
        else if (Math.floorMod(level.getGameTime() + worldPositionHash, STATE_VALIDATION_INTERVAL) == 0) {
            stateValidationTick((ServerLevel) level);
        }
        serverTick((ServerLevel) level);
    }

    private void stateValidationTick(ServerLevel level) {
        // Get BE values
        boolean controller = isController();
        int size = getMultiblockSize().getSize();

        // Compare blockstate and BE
        BlockState state = getBlockState();
        boolean validController = state.getValue(AbstractMultiblockBlock.CONTROLLER) == controller;
        boolean validSize = state.getValue(AbstractMultiblockBlock.MULTIBLOCK_SIZE) == size;

        // Repair state if necessary
        if (!validController || !validSize) setState(level, worldPosition, controller, size);
    }

    private void updateMultiblockTick(ServerLevel level) {
        // CASE 1: Current multiblock controller
        if (isMultiblockController()) {
            // Evaluate current multiblock status
            if (!isLoadedMultiblockController()) return;
            if (!isValidMultiblockController()) {
                disassemble(level, worldPosition);
                return;
            }

            // Look for upgrade
            int currentSize = multiblockSize.getSize();
            for (int size = 3; size > currentSize; size--) {
                BlockPos originPos = findOrigin(level, worldPosition, size);
                if (originPos == null) continue;
                if (setMultiblockController(level, originPos, size)) return;
            }
            return;
        }

        // CASE 2: Current multiblock member
        if (isMultiblockMember()) {
            // Evaluate current controller status
            if (!level.isLoaded(controllerPos)) return;
            AbstractMultiblockEntity controller = getControllerReference();
            if (controller != null && controller.isMultiblockController()) {
                if (!controller.isLoadedMultiblockController()) return;
                if (!controller.isValidMultiblockController()) {
                    controller.setMultiblockUpdate();
                    return;
                }
                return;
            }
            reset();
            return;
        }

        // CASE 3: Broken member metadata
        if (!isController()) {
            reset();
            return;
        }

        // CASE 4: Single controller
        for (int size = 3; size >= 2; size--) {
            BlockPos originPos = findOrigin(level, worldPosition, size);
            if (originPos == null) continue;
            if (setMultiblockController(level, originPos, size)) return;
        }
    }

    private static BlockPos findOrigin(ServerLevel level, BlockPos worldPosition, int size) {
        // Scan backwards to find matching blocks
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = worldPosition.offset(-x, -y, -z);
                    if (isValidOrigin(level, pos, size)) return pos;
                }
            }
        }
        return null;
    }

    private static boolean isValidOrigin(ServerLevel level, BlockPos originPos, int size) {
        AbstractMultiblockEntity origin = getMultiblockEntity(level, originPos);
        if (origin == null) return false;                              // Fail if invalid
        if (!origin.isController()) return false;                      // or not a current controller
        if (origin.getMultiblockSize().getSize() > size) return false; // or in a larger or equal multiblock

        // Scan forward and return true if all blocks match
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);
                    AbstractMultiblockEntity be = getMultiblockEntity(level, pos);
                    if (be == null) return false;                   // Fail if invalid
                    if (!origin.isCompatibleWith(be)) return false; // or an incompatible multiblock
                    if (!be.getControllerPos().equals(originPos) && // or part of another multiblock
                        be.getMultiblockSize().getSize() >= size    // with larger or equal size
                    ) return false;
                }
            }
        }
        return true;
    }

    private static boolean setMultiblockController(ServerLevel level, BlockPos originPos, int size) {
        // Check origin entity
        AbstractMultiblockEntity origin = getMultiblockEntity(level, originPos);
        if (origin == null) return false;
        if (!origin.isController()) return false;

        // Scan forward from origin and store detected members
        List<AbstractMultiblockEntity> members = new ArrayList<>();
        Set<BlockPos> conflicts = new HashSet<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);

                    // Normal validation cycle to add new members
                    AbstractMultiblockEntity be = getMultiblockEntity(level, pos);
                    if (be == null) return false;                   // Fail if invalid
                    if (!origin.isCompatibleWith(be)) return false; // or an incompatible multiblock
                    members.add(be);

                    // Extra validation to avoid conflicting blocks
                    if (!be.isMultiblock()) continue;
                    BlockPos controllerPos = be.getControllerPos();
                    if (controllerPos.equals(originPos)) continue;
                    if (!level.isLoaded(controllerPos)) return false; // Don't mess with unloaded stuff
                    if (!isValidBlock(level, controllerPos)) continue;
                    if (!(level.getBlockEntity(controllerPos) instanceof AbstractMultiblockEntity controller)) continue;
                    if (!controller.isMultiblockController()) continue;
                    if (controller.getMultiblockSize().getSize() >= size) return false; // Don't mess with large stuff
                    conflicts.add(controllerPos);
                }
            }
        }

        // Handle conflicts/overlaps
        for (BlockPos pos : conflicts) {
            AbstractMultiblockEntity controller = getMultiblockEntity(level, pos);
            if (controller == null) return false;
            if (!controller.isLoadedMultiblockController()) return false;
        }
        for (BlockPos pos : conflicts) {
            disassemble(level, pos);
        }

        // Set controller state/multiblock/pos values
        for (AbstractMultiblockEntity be : members) {
            boolean controller = be.getBlockPos().equals(originPos);
            boolean changed =
                be.getMultiblockSize().getSize() != size ||
                !be.getControllerPos().equals(originPos) ||
                be.getBlockState().getValue(AbstractMultiblockBlock.CONTROLLER) != controller ||
                be.getBlockState().getValue(AbstractMultiblockBlock.MULTIBLOCK_SIZE) != size;

            // Only update if something actually changed
            if (!changed) continue;
            be.setMultiblockSize(size);
            be.setControllerPos(originPos);
            setState(level, be.getBlockPos(), controller, size);
            be.notifyBlockUpdate();
        }
        return true;
    }

    private static void disassemble(ServerLevel level, BlockPos controllerPos) {
        AbstractMultiblockEntity controller = getMultiblockEntity(level, controllerPos);
        if (controller == null) return;
        if (!controller.isLoadedMultiblockController()) return;
        int size = controller.getMultiblockSize().getSize();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    AbstractMultiblockEntity be = getMultiblockEntity(level, pos);
                    if (be == null) continue;
                    if (!controller.isCompatibleWith(be)) continue;
                    if (!be.getControllerPos().equals(controllerPos)) continue;
                    be.reset();
                }
            }
        }
    }


    // CUSTOM TICKS
    protected void serverTick(ServerLevel level) {}
    protected void clientTick(Level level) {}


    // SYNCING
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Wrap the tag into the standard vanilla packet
        return ClientboundBlockEntityDataPacket.create(this);
    }


    // DATA PERSISTENCE
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("multiblockSize", multiblockSize.getSize());
        tag.putBoolean("isController", controllerPos == null);
        if (controllerPos != null) {
            tag.putLong("controllerPos", controllerPos.asLong());
        }
    }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("multiblockSize", Tag.TAG_INT))
            this.multiblockSize = Size.of(tag.getInt("multiblockSize"));
        else this.multiblockSize = Size.SINGLE;

        if (tag.contains("isController") && tag.getBoolean("isController"))
            this.controllerPos = null;
        else if (tag.contains("controllerPos", Tag.TAG_LONG)) {
            this.controllerPos = BlockPos.of(tag.getLong("controllerPos"));
            if (this.worldPosition.equals(this.controllerPos))
                this.controllerPos = null;
        }
        else {
            this.controllerPos = null;
            this.multiblockSize = Size.SINGLE;
            this.multiblockUpdate = true;
        }
    }


    // HELPERS
    public boolean isController() {
        return this.controllerPos == null;
    }
    public boolean isSingleController() {
        return isController() && !isMultiblock();
    }
    public boolean isMultiblock() {
        return this.multiblockSize != Size.SINGLE;
    }
    public boolean isMultiblockController() {
        return isController() && isMultiblock();
    }
    public boolean isValidMultiblockController() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockController()) return false;
        int size = this.multiblockSize.getSize();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = this.worldPosition.offset(x, y, z);
                    AbstractMultiblockEntity be = getMultiblockEntity(level, pos);
                    if (be == null) return false;
                    if (!isCompatibleWith(be)) return false;
                    if (!be.getControllerPos().equals(this.worldPosition)) return false;
                    if (be.getMultiblockSize() != this.multiblockSize) return false;
                }
            }
        }
        return true;
    }
    public boolean isLoadedMultiblockController() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockController()) return false;
        int size = this.multiblockSize.getSize();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = this.worldPosition.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;
                }
            }
        }
        return true;
    }
    public boolean isMultiblockMember() {
        return !isController() && isMultiblock();
    }
    public boolean isValidMultiblockMember() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockMember()) return false;
        AbstractMultiblockEntity controller = getControllerReference();
        if (controller == null || controller == this) return false;
        return controller.isValidMultiblockController();
    }
    @SuppressWarnings("unused")
    public boolean isLoadedMultiblockMember() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockMember()) return false;
        AbstractMultiblockEntity controller = getControllerReference();
        if (controller == null || controller == this) return false;
        return controller.isLoadedMultiblockController();
    }
    public boolean isConnected(AbstractMultiblockEntity other) {
        if (level == null || level.isClientSide) return false;
        if (other == null) return false;
        if (level != other.getLevel()) return false;
        if (!isCompatibleWith(other)) return false;
        BlockPos controllerPos = getControllerPos();
        if (!controllerPos.equals(other.getControllerPos())) return false;
        int size = getMultiblockSize().getSize();
        if (size != other.getMultiblockSize().getSize()) return false;
        return inMultiblockRange(getBlockPos(), controllerPos, size)
            && inMultiblockRange(other.getBlockPos(), controllerPos, size);
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidBlock(Level level, BlockPos pos) {
        return getMultiblockEntity(level, pos) != null;
    }
    public static boolean inMultiblockRange(BlockPos pos, BlockPos controllerPos, int size) {
        int dx = pos.getX() - controllerPos.getX();
        int dy = pos.getY() - controllerPos.getY();
        int dz = pos.getZ() - controllerPos.getZ();
        return dx >= 0 && dx < size
            && dy >= 0 && dy < size
            && dz >= 0 && dz < size;
    }

    public BlockPos getControllerPos() {
        if (this.controllerPos == null) return this.worldPosition;
        return this.controllerPos;
    }
    public void setControllerPos(BlockPos controllerPos) {
        if (this.worldPosition.equals(controllerPos)) controllerPos = null;
        this.controllerPos = controllerPos;
    }
    public Size getMultiblockSize() {
        return this.multiblockSize;
    }
    public void setMultiblockSize(int size) {
        this.multiblockSize = Size.of(size);
    }
    public boolean consumeMultiblockUpdate() {
        if (this.multiblockUpdate) {
            this.multiblockUpdate = false;
            return true;
        }
        return false;
    }
    public void setMultiblockUpdate() {
        this.multiblockUpdate = true;
    }

    public AbstractMultiblockEntity getControllerReference() {
        if (level == null || level.isClientSide) return null;
        if (isController()) return this; // Current block is a controller
        if (!isMultiblock()) return null;
        AbstractMultiblockEntity controller = getMultiblockEntity(level, controllerPos);
        if (controller == null) return null;
        if (!isCompatibleWith(controller)) return null;
        if (!controller.isController()) return null;
        if (!inMultiblockRange(worldPosition, controllerPos, multiblockSize.getSize())) return null;
        return controller;
    }
    @SuppressWarnings("unused")
    public List<BlockPos> getFrontier() {
        if (level == null || level.isClientSide) return Collections.emptyList();
        AbstractMultiblockEntity controller = getControllerReference();
        if (controller == null) return Collections.emptyList();
        if (!controller.isValidMultiblockController()) return Collections.emptyList();
        List<BlockPos> frontier = new ArrayList<>();
        int size = controller.getMultiblockSize().getSize();
        BlockPos origin = controller.getControllerPos();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    frontier.add(origin.offset(x, y, z));
                }
            }
        }
        return frontier;
    }
    public static void setState(Level level, BlockPos pos, boolean controller, int size) {
        if (level == null || level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(AbstractMultiblockBlock.CONTROLLER)) return;
        if (!state.hasProperty(AbstractMultiblockBlock.MULTIBLOCK_SIZE)) return;
        if (state.getValue(AbstractMultiblockBlock.CONTROLLER) == controller &&
            state.getValue(AbstractMultiblockBlock.MULTIBLOCK_SIZE) == size) return;
        state = state.setValue(AbstractMultiblockBlock.CONTROLLER, controller);
        state = state.setValue(AbstractMultiblockBlock.MULTIBLOCK_SIZE, size);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
    public void reset() {
        if (level == null || level.isClientSide) return;
        setMultiblockSize(1);
        setControllerPos(null);
        setState(level, worldPosition, true, 1);
        notifyBlockUpdate();
        setMultiblockUpdate();
    }
    public void propagateMultiblockUpdate() {
        if (level == null || level.isClientSide) return;
        if (!isController()) return;
        propagateMultiblockUpdate(level, worldPosition, multiblockSize.getSize());
    }
    public static void propagateMultiblockUpdate(Level level, BlockPos controllerPos, int size) {
        if (level == null || level.isClientSide) return;
        if (size < 1) return;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    AbstractMultiblockEntity be = getMultiblockEntity(level, pos);
                    if (be == null) continue;
                    be.setMultiblockUpdate();
                }
            }
        }
    }
    public void notifyBlockUpdate() {
        setChanged();
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    private AbstractMultiblockBlock<?> getMultiblockBlock() {
        if (getBlockState().getBlock() instanceof AbstractMultiblockBlock<?> block) return block;
        return null;
    }
    public static AbstractMultiblockEntity getMultiblockEntity(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return null;
        if (!level.isLoaded(pos)) return null;
        if (!(level.getBlockState(pos).getBlock() instanceof AbstractMultiblockBlock<?> block)) return null;
        if (!(level.getBlockEntity(pos) instanceof AbstractMultiblockEntity be)) return null;

        // Verify the concrete entity type matches its block
        if (!block.getBlockEntityClass().isInstance(be)) return null;
        if (be.getType() != block.getBlockEntityType()) return null;
        return be;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean isCompatibleWith(AbstractMultiblockEntity other) {
        if (level == null || level.isClientSide) return false;
        if (other == null) return false;
        if (level != other.getLevel()) return false;
        AbstractMultiblockBlock<?> thisBlock = getMultiblockBlock();
        AbstractMultiblockBlock<?> otherBlock = other.getMultiblockBlock();
        if (thisBlock == null || otherBlock == null) return false;
        return thisBlock.getMultiblockType().equals(otherBlock.getMultiblockType());
    }
}
