package com.nasilk.createcrystallized.block.dummy;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.event.TaskEventScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

// He's not very smart, but he's got spirit
public class DummyMultiblockEntity extends BlockEntity {
    // Variables
    private Size multiblockSize = Size.SINGLE;
    private BlockPos controllerPos = null;
    private boolean updateConnectivity = true;

    // Constants
    //private final int worldPositionHash;
    //private static final int RANDOM_TICK_RATE = 20;
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
        if (level.isClientSide) clientTick((ClientLevel) level);
        else if (getUpdateConnectivity()) serverTick((ServerLevel) level);
        //else serverTick((ServerLevel) level);
    }

    private void serverTick(ServerLevel level) {
        //CreateCrystallized.LOGGER.info("Server Tick starting: {}", worldPosition); // TODO DEBUG
        if (!isController())
            if (!level.isLoaded(controllerPos) ||                                          // Skip if controller is unloaded OR
                multiblockSize != Size.SINGLE &&                                           // current block is not single
                level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity be && // and its controller is valid
                be.isController() &&                                                       // and its controller knows it's a controller
                be.getMultiblockSize() == multiblockSize                                   // and its controller has the right size
            ) return;

        // TODO Make the controller NOT check and update every member every tick
        CreateCrystallized.LOGGER.info("Origin search starting: {}", worldPosition); // TODO DEBUG
        // Look for group
        for (int size = 3; size >= 2; size--) {
            BlockPos originPos = findOrigin(level, size);
            if (originPos == null) continue;
            CreateCrystallized.LOGGER.info("Origin search of size {} successful: {}", size, worldPosition); // TODO DEBUG
            if (setMultiblockController(level, originPos, size)) return;
        }

        // Reset stranded members to controllers
        if (multiblockSize == Size.SINGLE &&                          // Skip if single
            isController() &&                                         // and a controller
            getBlockState().getValue(DummyMultiblockBlock.CONTROLLER) // and the state agrees that it's a controller
        ) return;
        CreateCrystallized.LOGGER.info("Stranded BE resetting: {}", worldPosition); // TODO DEBUG
        setMultiblockSize(1);
        setControllerPos(null);
        setControllerState(worldPosition, true);
        setControllerStateDebug(worldPosition, 1); // TODO DEBUG
        setUpdateConnectivity(); // Check one more time to avoid needing an external update in overlap cases
        markUpdated();
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
        if (!isValidBlock(originPos)) return false;                                                   // or an invalid block
        if (!(level.getBlockEntity(originPos) instanceof DummyMultiblockEntity origin)) return false; // or an invalid entity
        if (!origin.isController()) return false;                                                     // or not a current controller
        //if (origin.getMultiblockSize().getSize() >= size) return false;                               // or in a larger or equal multiblock

        // Scan forward and return true if all blocks match
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;                                             // Fail if unloaded
                    if (!isValidBlock(pos)) return false;                                               // or an invalid block
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false; // or an invalid entity
                    if (!be.isController() &&                                                           // or a member
                        !be.getControllerPos().equals(originPos) &&                                     // that's part of another multiblock
                        be.getMultiblockSize().getSize() >= size                                        // with larger or equal size
                    ) return false;
                }
            }
        }
        return true;
    }

    private boolean setMultiblockController(ServerLevel level, BlockPos controllerPos, int size) {
        // Scan forward from origin and store detected members
        List<DummyMultiblockEntity> members = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;                                             // Fail if unloaded
                    if (!isValidBlock(pos)) return false;                                               // or an invalid block
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false; // or an invalid entity
                    members.add(be);
                }
            }
        }

        // Set controller state/multiblock/pos values
        for (DummyMultiblockEntity be : members) {
            be.setMultiblockSize(size);
            be.setControllerPos(controllerPos);
            setControllerState(be.getBlockPos(), be.getBlockPos().equals(controllerPos));
            setControllerStateDebug(be.getBlockPos(), size); // TODO DEBUG
            be.markUpdated();
        }
        return true;
    }

    private void clientTick(ClientLevel level) {
        // Have a little ambiance
        if (level.getRandom().nextDouble() < AMBIENT_RATE) level.playLocalSound(
            worldPosition,
            SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
            1.0f,0.8f, false
        );
    }


    // SYNCING
    public void markUpdated() {
        if (level == null || level.isClientSide) return;
        BlockState state = this.getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        this.setChanged();
    }

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
    public BlockPos getControllerPos() {
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
    public boolean getUpdateConnectivity() {
        if (this.updateConnectivity) {
            this.updateConnectivity = false;
            return true;
        }
        return false;
    }
    public void setUpdateConnectivity() {
        this.updateConnectivity = true;
    }
    public void propagateSetUpdateConnectivity() {
        if (level == null || level.isClientSide) return;
        if (!isController()) return;
        // Update self first
        setUpdateConnectivity();

        // Update everything on the next tick
        int size = this.multiblockSize.getSize();
        if (level.getServer() instanceof MinecraftServer server) TaskEventScheduler.schedule(server, 1, () -> {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    for (int z = 0; z < size; z++) {
                        BlockPos pos = this.worldPosition.offset(x, y, z);
                        if (!level.isLoaded(pos)) continue;
                        if (!isValidBlock(pos)) continue;
                        if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
                        be.setUpdateConnectivity();
                        CreateCrystallized.LOGGER.info("updateConnectivity set: {}", pos);
                    }
                }
            }
        });
    }

    public boolean isController() {
        return this.controllerPos == null;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidBlock(BlockPos pos) {
        if (level == null || level.isClientSide) return false;
        BlockState state = level.getBlockState(pos);
        return DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(state) &&
            state.hasProperty(DummyMultiblockBlock.CONTROLLER);
    }

    public void setControllerState(BlockPos pos, boolean value) {
        if (level == null || level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(DummyMultiblockBlock.CONTROLLER)) return;
        if (state.getValue(DummyMultiblockBlock.CONTROLLER) == value) return;
        level.setBlock(pos, state.setValue(DummyMultiblockBlock.CONTROLLER, value), Block.UPDATE_CLIENTS);
    }
    // TODO DEBUG
    public void setControllerStateDebug(BlockPos pos, int value) {
        if (level == null || level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(DummyMultiblockBlock.MULTIBLOCK_SIZE)) return;
        if (state.getValue(DummyMultiblockBlock.MULTIBLOCK_SIZE) == value) return;
        level.setBlock(pos, state.setValue(DummyMultiblockBlock.MULTIBLOCK_SIZE, value), Block.UPDATE_CLIENTS);
    }
    public DummyMultiblockEntity getControllerEntity() {
        if (level == null || level.isClientSide) return null;
        return isController() ? this :       // Get this if it's a controller
            level.isLoaded(controllerPos) && // else get the controller if it's valid
            level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity be &&
            be.isController() &&
            be.getMultiblockSize() == multiblockSize ? be : null;
    }
    // TODO Write and implement this (well, maybe not, it works as-is...)
    //public void disassembleMultiblock() {}
}
