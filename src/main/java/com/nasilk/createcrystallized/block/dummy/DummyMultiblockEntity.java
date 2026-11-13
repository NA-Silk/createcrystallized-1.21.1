package com.nasilk.createcrystallized.block.dummy;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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


    // TICK BEHAVIOR
    public void tick() {
        if (level == null) return;
        if (level.isClientSide) clientTick(level); // TODO Abstract clientTick
        else if (getMultiblockUpdate()) updateMultiblockTick((ServerLevel) level);
        //else serverTick((ServerLevel) level); // TODO Abstract serverTick
    }

    //abstract void serverTick(ServerLevel level); // TODO Abstract serverTick

    private void updateMultiblockTick(ServerLevel level) {
        CreateCrystallized.LOGGER.debug("Update Multiblock Tick starting: {}", worldPosition); // TODO Remove debug line

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
                BlockPos originPos = findOrigin(level, size);
                if (originPos == null) continue;
                if (setMultiblockController(level, originPos, size)) return;
            }
            return;
        }

        // CASE 2: Current multiblock member
        if (isMultiblockMember()) {
            // Evaluate current controller status
            if (!level.isLoaded(controllerPos)) return;
            DummyMultiblockEntity controller = getControllerReference();
            if (controller != null && controller.isMultiblockController()) {
                if (!controller.isLoadedMultiblockController()) return;
                if (!controller.isValidMultiblockController()) {
                    controller.setMultiblockUpdate();
                    return;
                }
                return;
            }
            reset(level);
            return;
        }

        // CASE 3: Broken member metadata
        if (!isController()) {
            reset(level);
            return;
        }

        // CASE 4: Single controller
        for (int size = 3; size >= 2; size--) {
            BlockPos originPos = findOrigin(level, size);
            if (originPos == null) continue;
            if (setMultiblockController(level, originPos, size)) return;
        }
    }

    private BlockPos findOrigin(ServerLevel level, int size) {
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

    private boolean isValidOrigin(ServerLevel level, BlockPos originPos, int size) {
        if (!level.isLoaded(originPos)) return false;                                                 // Fail if unloaded
        if (!isValidBlock(level, originPos)) return false;                                            // or an invalid block
        if (!(level.getBlockEntity(originPos) instanceof DummyMultiblockEntity origin)) return false; // or an invalid entity
        if (!origin.isController()) return false;                                                     // or not a current controller
        if (origin.getMultiblockSize().getSize() > size) return false;                               // or in a larger or equal multiblock

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

    private boolean setMultiblockController(ServerLevel level, BlockPos originPos, int size) {
        // Scan forward from origin and store detected members // TODO Extract to standalone method
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

    private void reset(ServerLevel level) {
        setMultiblockSize(1);
        setControllerPos(null);
        setState(level, worldPosition, true, 1);
        notifyBlockUpdate();
        setMultiblockUpdate();
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
                    be.reset(level);
//                    be.setMultiblockSize(1);
//                    be.setControllerPos(null);
//                    setState(level, pos, true, 1);
//                    be.notifyBlockUpdate();
//                    be.setMultiblockUpdate();
                }
            }
        }
    }

    //abstract void clientTick(Level level); // TODO Abstract clientTick
    private void clientTick(Level level) {
        // Have a little ambiance
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
    public boolean isSingleController() {
        return isController() && !isMultiblock();
    }
    public boolean isMultiblock() {
        return this.multiblockSize != Size.SINGLE;
    }
    public boolean isMultiblockController() {
        return isController() && isMultiblock();
    }
    // Validate the entire group
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
    // Check whether the entire group can be inspected
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
    // Validate this member through its controller
    public boolean isValidMultiblockMember() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockMember()) return false;
        DummyMultiblockEntity controller = getControllerReference();
        if (controller == null || controller == this) return false;
        return controller.isValidMultiblockController();
    }
    // Check whether its referenced controller's cube is loaded
    public boolean isLoadedMultiblockMember() {
        if (level == null || level.isClientSide) return false;
        if (!isMultiblockMember()) return false;
        DummyMultiblockEntity controller = getControllerReference();
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

    public static boolean isValidBlock(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return false;
        return DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(level.getBlockState(pos));
    }
    public static boolean isInRange(BlockPos pos, BlockPos controllerPos, int size) {
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

//    public DummyMultiblockEntity getController() {
//        if (level == null || level.isClientSide) return null;
//        if (isSingleController()) return this;          // Get this if it's a single controller
//        if (isValidMultiblockController()) return this; // Get this if it's a valid multiblock controller
//        if (isMultiblock() &&                                                                   // Get controller if it's a multiblock
//            level.isLoaded(this.controllerPos) &&                                               // and the controller is loaded
//            isInRange(this.worldPosition, this.controllerPos, this.multiblockSize.getSize()) && // and in range
//            isValidBlock(level, this.controllerPos) &&                                          // and a valid block
//            level.getBlockEntity(this.controllerPos) instanceof DummyMultiblockEntity be &&     // and a valid entity
//            be.isValidMultiblockController()                                                    // and is a valid controller
//        ) return be;
//        return null;
//    }
    // TODO Review these
    // Resolve a currently valid controller
    public DummyMultiblockEntity getController() {
        if (level == null || level.isClientSide) return null;
        if (isController()) { // Handle controllers explicitly
            if (!isMultiblock()) return this;
            return isValidMultiblockController() ? this : null;
        }
        DummyMultiblockEntity controller = getControllerReference(); // Resolve the referenced controller
        if (controller == null) return null;
        if (!controller.isMultiblock()) return null; // Members require a fully valid controller
        if (controller.getMultiblockSize() != multiblockSize) return null;
        return controller.isValidMultiblockController() ? controller : null;
    }
    // Resolve the stored controller, even if invalid
    public DummyMultiblockEntity getControllerReference() {
        if (level == null || level.isClientSide) return null;
        if (isController()) return this; // A controller always references itself
        if (!isMultiblockMember() || controllerPos == null) return null; // This must be a member with stored ownership
        if (!level.isLoaded(controllerPos)) return null; // The referenced controller must be accessible
        if (!isValidBlock(level, controllerPos)) return null;
        if (!(level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity controller)) return null;
        if (!controller.isController()) return null; // Check controller identity, but NOT group validity
        if (!isInRange(worldPosition, controllerPos, multiblockSize.getSize())) return null; // The member must geometrically belong to this controller
        return controller;
    }

    // TODO Try to consolidate these two
//    public void propagateMultiblockUpdate() {
//        if (level == null || level.isClientSide) return;
//        if (!isController()) return;
//
//        // Update everything on the next tick
//        BlockPos controllerPos = this.worldPosition;
//        ResourceKey<Level> dimension = level.dimension();
//        if (level.getServer() instanceof MinecraftServer server) {
//            TaskEventScheduler.schedule(
//                server,
//                UUID.fromString(dimension + controllerPos.toString()),
//                1,
//                () -> {
//                    ServerLevel currentLevel = server.getLevel(dimension);
//                    if (currentLevel == null || !currentLevel.isLoaded(controllerPos)) return;
//                    if (!(currentLevel.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity controller)) return;
//                    if (!controller.isController()) return;
//                    int size = controller.getMultiblockSize().getSize();
//                    for (int x = 0; x < size; x++) {
//                        for (int y = 0; y < size; y++) {
//                            for (int z = 0; z < size; z++) {
//                                BlockPos pos = controllerPos.offset(x, y, z);
//                                if (!currentLevel.isLoaded(pos)) continue;
//                                if (!isValidBlock(currentLevel, pos)) continue;
//                                if (!(currentLevel.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
//                                be.setMultiblockUpdate();
//                                CreateCrystallized.LOGGER.debug("updateConnectivity set: {}", pos); // TODO Remove debug line
//                            }
//                        }
//                    }
//                }
//            );
//        }
//    }
//    public static void propagateMultiblockUpdate(Level level, BlockPos controllerPos, int size) {
//        if (level == null || level.isClientSide) return;
//        if (size == 1) return;
//        for (int x = 0; x < size; x++) {
//            for (int y = 0; y < size; y++) {
//                for (int z = 0; z < size; z++) {
//                    BlockPos pos = controllerPos.offset(x, y, z);
//                    if (pos.equals(controllerPos)) continue;
//                    if (!level.isLoaded(pos)) continue;
//                    if (!isValidBlock(level, pos)) continue;
//                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
//                    be.setMultiblockUpdate();
//                }
//            }
//        }
//    }
    // TODO Review these
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
    public void notifyBlockUpdate() {
        setChanged();
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }
}
