package com.nasilk.createcrystallized.block.dummy;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

// He's not very smart, but he's got spirit
@SuppressWarnings("all")
public class DummyMultiblockEntity extends BlockEntity {
    // Variables
    private BlockPos controllerPos = null;

    // Constants
    private final int worldPositionHash;
    private static final int RANDOM_TICK_RATE = 20;
    private static final double AMBIENT_RATE = 8e-5d;

    // Cache
    private static class Cache {
        final Vector3d position = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);


    public DummyMultiblockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.worldPositionHash = pos.hashCode();
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level.isClientSide) clientTick((ClientLevel) level);
        else serverTick((ServerLevel) level);
    }

    private void serverTick(ServerLevel level) {
        if (controllerPos != null) {
            if (DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(level.getBlockState(controllerPos)) &&
                    level.getBlockState(controllerPos).getValue(DummyMultiblockBlock.CONTROLLER)
            ) return;
            else controllerPos = null;
        }

        // Look for group
        List<BlockPos> blockPosList = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
            worldPosition.getX() - 1, worldPosition.getY() - 1, worldPosition.getZ() - 1,
            worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1
        )) {
            if (pos.equals(worldPosition)) continue; // Skip self
            if (!DummyRegistry.DUMMY_MULTIBLOCK_BLOCK.has(level.getBlockState(pos))) continue; // Skip non-dummies
            if (level.getBlockState(pos).getValue(DummyMultiblockBlock.CONTROLLER)) {
                controllerPos = pos;
                return;
            }
            blockPosList.add(pos.immutable());
        }

        // Set controller
        if (blockPosList.size() == 26) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DummyMultiblockBlock.CONTROLLER, true));
            for (BlockPos pos : blockPosList) {
                BlockState state = level.getBlockState(pos);
                level.setBlockAndUpdate(pos, state.setValue(DummyMultiblockBlock.CONTROLLER, false));
            }
        }
        else level.setBlockAndUpdate(worldPosition, getBlockState().setValue(DummyMultiblockBlock.CONTROLLER, false));


//        // Get global position
//        Cache cache = CACHE.get();
//        cache.position.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d);
//        ServerSubLevel subLevel = null;
//        if (Sable.HELPER.getContaining(level, worldPosition) instanceof ServerSubLevel serverSubLevel) {
//            subLevel = serverSubLevel;
//            subLevel.logicalPose().transformPosition(cache.position);
//        }
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
    private void markUpdated() {
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = this.getBlockState();
            serverLevel.sendBlockUpdated(worldPosition, state, state, 2);
            this.setChanged();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }
}
