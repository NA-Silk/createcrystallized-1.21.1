package com.nasilk.createcrystallized.block.dummy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

// He's not very smart, but he's got spirit
public class DummyMultiblockEntity extends BlockEntity {
    // Variables
    private Size multiblockSize = Size.SINGLE;
    private BlockPos controllerPos = null;
    private boolean multiblockUpdate = true;

    // Constants
    private static final double AMBIENT_RATE = 8e-5d;
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

    public DummyMultiblockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        //this.worldPositionHash = pos.hashCode();
    }


    // MULTIBLOCK TICK HANDLING
    public void tick() {
        if (level == null) return;
        if (level.isClientSide) clientTick(level);
        else if (getMultiblockUpdate()) updateMultiblockTick((ServerLevel) level);
        else serverTick((ServerLevel) level);
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
            DummyMultiblockEntity controller = getController();
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
        if (!level.isLoaded(originPos)) return false;                                                 // Fail if unloaded
        if (!isValidBlock(level, originPos)) return false;                                            // or an invalid block
        if (!(level.getBlockEntity(originPos) instanceof DummyMultiblockEntity origin)) return false; // or an invalid entity
        if (!origin.isController()) return false;                                                     // or not a current controller
        if (origin.getMultiblockSize().getSize() > size) return false;                                // or in a larger or equal multiblock

        // Scan forward and return true if all blocks match
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;                                             // Fail if unloaded
                    if (!isValidBlock(level, pos)) return false;                                        // or an invalid block
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false; // or an invalid entity
                    if (!be.getControllerPos().equals(originPos) &&                                     // or part of another multiblock
                        be.getMultiblockSize().getSize() >= size                                        // with larger or equal size
                    ) return false;
                }
            }
        }
        return true;
    }

    private static boolean setMultiblockController(ServerLevel level, BlockPos originPos, int size) {
        // Scan forward from origin and store detected members
        List<DummyMultiblockEntity> members = new ArrayList<>();
        Set<BlockPos> conflicts = new HashSet<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);

                    // Normal validation cycle to add new members
                    if (!level.isLoaded(pos)) return false;                                             // Fail if unloaded
                    if (!isValidBlock(level, pos)) return false;                                        // or an invalid block
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false; // or an invalid entity
                    members.add(be);

                    // Extra validation to avoid conflicting blocks
                    BlockPos controllerPos = be.getControllerPos();
                    if (controllerPos.equals(originPos)) continue;
                    if (!be.isMultiblock()) continue;
                    if (!level.isLoaded(controllerPos)) return false; // Don't mess with unloaded stuff
                    if (!isValidBlock(level, controllerPos)) continue;
                    if (!(level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity controller)) continue;
                    if (!controller.isController()) continue;
                    if (!controller.isMultiblock()) continue;
                    if (controller.getMultiblockSize().getSize() >= size) return false; // Don't mess with big stuff
                    conflicts.add(controllerPos);
                }
            }
        }

        // Handle conflicts/overlaps
        for (BlockPos pos : conflicts) {
            if (!level.isLoaded(pos)) return false;
            if (!isValidBlock(level, pos)) return false;
            if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity controller)) return false;
            if (!controller.isLoadedMultiblockController()) return false;
        }
        for (BlockPos pos : conflicts) {
            disassemble(level, pos);
        }

        // Set controller state/multiblock/pos values
        for (DummyMultiblockEntity be : members) {
            boolean controller = be.getBlockPos().equals(originPos);
            boolean changed =
                be.getMultiblockSize().getSize() != size ||
                !be.getControllerPos().equals(originPos) ||
                be.getBlockState().getValue(DummyMultiblockBlock.CONTROLLER) != controller ||
                be.getBlockState().getValue(DummyMultiblockBlock.MULTIBLOCK_SIZE) != size;

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
        if (!level.isLoaded(controllerPos)) return;
        if (!isValidBlock(level, controllerPos)) return;
        if (!(level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity controller)) return;
        if (!controller.isMultiblockController()) return;
        if (!controller.isLoadedMultiblockController()) return;
        int size = controller.getMultiblockSize().getSize();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) continue;
                    if (!isValidBlock(level, pos)) continue;
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
                    if (!be.getControllerPos().equals(controllerPos)) continue;
                    be.reset();
                }
            }
        }
    }


    // CUSTOM TICKS
    private void serverTick(ServerLevel level) {
        if (!isController()) return;
        if (level.getRandom().nextDouble() < AMBIENT_RATE) level.sendParticles(
            ParticleTypes.LARGE_SMOKE,
            worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d,
            5, 0.5d, 0.5d, 0.5d, 1.0d
        );
    }

    private void clientTick(Level level) {
        if (level.getRandom().nextDouble() < AMBIENT_RATE) level.playLocalSound(
            worldPosition,
            SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
            1.0f,0.8f, false
        );
    }


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
        tag.putLong("controllerPos", controllerPos == null ? worldPosition.asLong() : controllerPos.asLong());
    }
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.multiblockSize = Size.of(tag.getInt("multiblockSize"));
        if (!tag.contains("controllerPos")) return;
        BlockPos pos = BlockPos.of(tag.getLong("controllerPos"));
        this.controllerPos = worldPosition.equals(pos) ? null : pos;
    }


    // HELPERS
    public boolean isController() {
        return this.controllerPos == null;
    }
    @SuppressWarnings("unused")
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
                    if (!level.isLoaded(pos)) return false;
                    if (!isValidBlock(level, pos)) return false;
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false;
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
    @SuppressWarnings("unused")
    public boolean isValidMultiblockMember() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockMember()) return false;
        DummyMultiblockEntity controller = getController();
        if (controller == null || controller == this) return false;
        return controller.isValidMultiblockController();
    }
    @SuppressWarnings("unused")
    public boolean isLoadedMultiblockMember() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockMember()) return false;
        DummyMultiblockEntity controller = getController();
        if (controller == null || controller == this) return false;
        return controller.isLoadedMultiblockController();
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
    public boolean getMultiblockUpdate() {
        if (this.multiblockUpdate) {
            this.multiblockUpdate = false;
            return true;
        }
        return false;
    }
    public void setMultiblockUpdate() {
        this.multiblockUpdate = true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidBlock(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return false;
        return DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(level.getBlockState(pos));
    }
    public static boolean inMultiblockRange(BlockPos pos, BlockPos controllerPos, int size) {
        if (size == 1) return true;
        int dx = pos.getX() - controllerPos.getX();
        int dy = pos.getY() - controllerPos.getY();
        int dz = pos.getZ() - controllerPos.getZ();
        return dx >= 0 && dx < size
            && dy >= 0 && dy < size
            && dz >= 0 && dz < size;
    }
    public static void setState(Level level, BlockPos pos, boolean controller, int size) {
        if (level == null || level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(DummyMultiblockBlock.CONTROLLER)) return;
        if (!state.hasProperty(DummyMultiblockBlock.MULTIBLOCK_SIZE)) return;
        if (state.getValue(DummyMultiblockBlock.CONTROLLER) == controller &&
                state.getValue(DummyMultiblockBlock.MULTIBLOCK_SIZE) == size) return;
        state = state.setValue(DummyMultiblockBlock.CONTROLLER, controller);
        state = state.setValue(DummyMultiblockBlock.MULTIBLOCK_SIZE, size);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    public DummyMultiblockEntity getController() {
        if (level == null || level.isClientSide) return null;
        if (isController()) return this; // Current block is a controller
        if (!isMultiblock()) return null;
        if (!level.isLoaded(controllerPos)) return null;
        if (!isValidBlock(level, controllerPos)) return null;
        if (!(level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity controller)) return null;
        if (!controller.isController()) return null;
        if (!inMultiblockRange(worldPosition, controllerPos, multiblockSize.getSize())) return null;
        return controller;
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
                    if (!level.isLoaded(pos)) continue;
                    if (!isValidBlock(level, pos)) continue;
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
                    be.setMultiblockUpdate();
                }
            }
        }
    }
    private void reset() {
        if (level == null || level.isClientSide) return;
        setMultiblockSize(1);
        setControllerPos(null);
        setState(level, worldPosition, true, 1);
        notifyBlockUpdate();
        setMultiblockUpdate();
    }
    public void notifyBlockUpdate() {
        setChanged();
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }
}
