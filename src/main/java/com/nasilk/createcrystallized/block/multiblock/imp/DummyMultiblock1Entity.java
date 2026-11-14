package com.nasilk.createcrystallized.block.multiblock.imp;

import com.nasilk.createcrystallized.block.multiblock.AbstractMultiblockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// He's not very smart, but he's got spirit
public class DummyMultiblock1Entity extends AbstractMultiblockEntity {
    // Constants
    private static final double AMBIENT_RATE = 8e-5d;

    public DummyMultiblock1Entity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    // CUSTOM TICKS
    @Override
    protected void serverTick(ServerLevel level) {
        if (!isController()) return;
        if (level.getRandom().nextDouble() < AMBIENT_RATE) level.sendParticles(
            ParticleTypes.LARGE_SMOKE,
            worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d,
            5, 0.5d, 0.5d, 0.5d, 1.0d
        );
    }

    @Override
    protected void clientTick(Level level) {
        if (!isController()) return;
        if (level.getRandom().nextDouble() < AMBIENT_RATE) level.playLocalSound(
            worldPosition,
            SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
            1.0f, 0.8f, false
        );
    }
}
