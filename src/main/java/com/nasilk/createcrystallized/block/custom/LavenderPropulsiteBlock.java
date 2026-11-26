package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.common.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class LavenderPropulsiteBlock extends PropulsiteBlock{
    public LavenderPropulsiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(
            ModParticleTypes.LAVENDER_PROPULSITE_PARTICLES.get(),
            pos.getX() + 0.5d,
            pos.getY() + 0.5d,
            pos.getZ() + 0.5d,
            16,0.5d,0.5d,0.5d,0.25d
        );
    }
}
