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
import net.minecraft.world.level.Level;
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
    private boolean updateMultiblock = true;

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
        if (level.isClientSide) clientTick((ClientLevel) level); // TODO Abstract clientTick
        else if (getUpdateMultiblock()) updateMultiblockTick((ServerLevel) level);
        //else serverTick((ServerLevel) level); // TODO Abstract serverTick
    }

    //abstract void serverTick(ServerLevel level); // TODO Abstract serverTick

    private void updateMultiblockTick(ServerLevel level) {
        //CreateCrystallized.LOGGER.info("Server Tick starting: {}", worldPosition); // TODO Remove debug line
        if (!isController())
            if (!level.isLoaded(controllerPos) ||                                          // Skip if controller is unloaded OR
                multiblockSize != Size.SINGLE &&                                           // current block is not single
                isValidBlock(level, controllerPos) &&                                      // and its controller block is valid
                level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity be && // and its controller entity is valid
                be.isController() &&                                                       // and its controller knows it's a controller
                be.getMultiblockSize() == multiblockSize                                   // and its controller has the right size
            ) return;

        // Look for origin
        CreateCrystallized.LOGGER.info("Origin search starting: {}", worldPosition); // TODO Remove debug line
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
        CreateCrystallized.LOGGER.info("Stranded BE resetting: {}", worldPosition); // TODO Remove debug line
        setMultiblockSize(1);
        setControllerPos(null);
        setState(level, worldPosition, true, 1);
        setChanged();
        setUpdateMultiblock(); // Check one more time to avoid needing an external update in overlap cases
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
        if (origin.getMultiblockSize().getSize() >= size) return false;                               // or in a larger or equal multiblock

        // Scan forward and return true if all blocks match
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;                                             // Fail if unloaded
                    if (!isValidBlock(level, pos)) return false;                                        // or an invalid block
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
        // Scan forward from origin and store detected members // TODO Extract to standalone method
        List<DummyMultiblockEntity> members = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;                                             // Fail if unloaded
                    if (!isValidBlock(level, pos)) return false;                                        // or an invalid block
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false; // or an invalid entity
                    members.add(be);
                }
            }
        }

        // Set controller state/multiblock/pos values
        for (DummyMultiblockEntity be : members) {
            be.setMultiblockSize(size);
            be.setControllerPos(controllerPos);
            setState(level, be.getBlockPos(), be.getBlockPos().equals(controllerPos), size);
            be.setChanged();
        }
        return true;
    }

    //abstract void clientTick(ClientLevel level); // TODO Abstract clientTick
    private void clientTick(ClientLevel level) {
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
    public boolean getUpdateMultiblock() {
        if (this.updateMultiblock) {
            this.updateMultiblock = false;
            return true;
        }
        return false;
    }
    public void setUpdateMultiblock() {
        this.updateMultiblock = true;
    }

    public void propagateSetUpdateMultiblockController() {
        if (level == null || level.isClientSide) return;
        if (!isController()) return;
        // Update self first
        setUpdateMultiblock();

        // Update everything on the next tick
        int size = this.multiblockSize.getSize(); // TODO Move controller access into task to avoid stale references
        if (level.getServer() instanceof MinecraftServer server) TaskEventScheduler.schedule(server, 1, () -> {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    for (int z = 0; z < size; z++) {
                        BlockPos pos = this.worldPosition.offset(x, y, z);
                        if (!level.isLoaded(pos)) continue;
                        if (!isValidBlock(level, pos)) continue;
                        if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
                        be.setUpdateMultiblock();
                        CreateCrystallized.LOGGER.info("updateConnectivity set: {}", pos); // TODO Remove debug line
                    }
                }
            }
        });
    }
    public static void propagateSetUpdateMultiblockStatic(Level level, BlockPos controllerPos, int size) {
        if (level == null || level.isClientSide) return;
        if (size == 1) return;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    if (pos.equals(controllerPos)) continue;
                    if (!level.isLoaded(pos)) continue;
                    if (!isValidBlock(level, pos)) continue;
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) continue;
                    be.setUpdateMultiblock();
                }
            }
        }
    }

    public boolean isController() {
        return this.controllerPos == null;
    }
    public DummyMultiblockEntity getController() {
        if (level == null || level.isClientSide) return null;
        return isController() ? this :                                                 // Get this if it's a controller
            level.isLoaded(controllerPos) &&                                           // else get the controller if it's loaded
            isValidBlock(level, controllerPos) &&                                      // and a valid block
            level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity be && // and a valid entity
            be.isController() &&                                                       // and it knows it's a controller
            be.getMultiblockSize() == multiblockSize ? be : null;                      // and it has the right size
    }

    public static boolean isValidBlock(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return false;
        BlockState state = level.getBlockState(pos);
        return DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(state) &&
            state.hasProperty(DummyMultiblockBlock.CONTROLLER);
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
}
