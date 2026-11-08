package com.nasilk.createcrystallized.block.dummy;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

// He's not very smart, but he's got spirit
//@SuppressWarnings("all")
public class DummyMultiblockEntity extends BlockEntity {
    // Variables
    private Size multiblockSize = Size.SINGLE;
    private BlockPos controllerPos = null;

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
        else serverTick((ServerLevel) level);
    }

    private void serverTick(ServerLevel level) {
        if (!isController())
            if (!level.isLoaded(controllerPos) ||                                          // Skip if controller is unloaded OR
                multiblockSize != Size.SINGLE &&                                           // current block is not single
                level.getBlockEntity(controllerPos) instanceof DummyMultiblockEntity be && // and its controller is valid
                be.isController() &&                                                       // and its controller knows it's a controller
                be.getMultiblockSize() == multiblockSize                                   // and its controller has the right size
            ) return;

        // TODO Make the controller NOT check and update every member every tick
        // Look for group
        for (int size = 3; size >= 2; size--) {
            BlockPos originPos = findOrigin(level, size);
            if (originPos == null) continue;
            if (setMultiblockController(level, originPos, size)) return;
        }

        // Reset stranded members to controllers
        if (multiblockSize == Size.SINGLE &&
            isController() &&
            getBlockState().getValue(DummyMultiblockBlock.CONTROLLER)
        ) return;
        setMultiblockSize(1);
        setControllerPos(null);
        setControllerState(worldPosition, true);
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
        if (!level.isLoaded(originPos)) return false;                                                 // Skip if unloaded
        if (!isValidBlock(originPos)) return false;                                                   // or an invalid block
        if (!(level.getBlockEntity(originPos) instanceof DummyMultiblockEntity origin)) return false; // or an invalid entity
        if (!origin.isController()) return false;                                                     // or not a current controller
        if (origin.getMultiblockSize().getSize() >= size) return false;                               // or in a larger or equal multiblock

        // Scan forward and return true if all blocks match
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = originPos.offset(x, y, z);
                    if (!level.isLoaded(pos)) return false;                                             // Skip if unloaded
                    if (!isValidBlock(pos)) return false;                                               // or an invalid block
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false; // or an invalid entity
                    if (!be.isController() &&                                                           // or a member
                        !be.controllerPos.equals(originPos) &&                                          // that's part of another multiblock
                        be.getMultiblockSize().getSize() >= size                                        // with the same or larger size
                    ) return false;
                }
            }
        }
        return true;
    }

    private boolean setMultiblockController(ServerLevel level, BlockPos controllerPos, int size) {
        // Scan forward from origin
        List<DummyMultiblockEntity> members = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    BlockPos pos = controllerPos.offset(x, y, z);
                    if (!isValidBlock(pos)) return false;
                    if (!(level.getBlockEntity(pos) instanceof DummyMultiblockEntity be)) return false;
                    members.add(be);
                }
            }
        }

        // Set controller state/multiblock/pos values
        for (DummyMultiblockEntity be : members) {
            be.setMultiblockSize(size);
            be.setControllerPos(controllerPos);
            BlockPos pos = be.getBlockPos();
            setControllerState(pos, pos.equals(controllerPos));
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
        if (level == null) return;
        BlockState state = this.getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 2);
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidBlock(BlockPos pos) {
        if (level == null) return false;
        BlockState state = level.getBlockState(pos);
        return DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(state) &&
            state.hasProperty(DummyMultiblockBlock.CONTROLLER);
    }
    public boolean isController() {
        return controllerPos == null;
    }
    public void setControllerPos(BlockPos controllerPos) {
        if (this.worldPosition.equals(controllerPos)) controllerPos = null;
        this.controllerPos = controllerPos;
    }
    public void setControllerState(BlockPos pos, boolean value) {
        if (level == null) return;
        BlockState state = level.getBlockState(pos);
        if (state.getValue(DummyMultiblockBlock.CONTROLLER) == value) return;
        level.setBlockAndUpdate(pos, state.setValue(DummyMultiblockBlock.CONTROLLER, value));
    }
    public void setMultiblockSize(int size) {
        this.multiblockSize = Size.of(size);
    }
    public Size getMultiblockSize() {
        return multiblockSize;
    }
    @SuppressWarnings("unused")
    public DummyMultiblockEntity getController() {
        if (level == null) return null;
        BlockEntity be = controllerPos == null ? level.getBlockEntity(worldPosition) : level.getBlockEntity(controllerPos);
        if (be instanceof DummyMultiblockEntity controller) return controller;
        return null;
    }
}
