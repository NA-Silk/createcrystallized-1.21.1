package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.common.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class UmberDensiteBlock extends DensiteBlock {
    public UmberDensiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) serverLevel.sendParticles(
            ModParticleTypes.UMBER_DENSITE_PARTICLES.get(),
            pos.getX() + 0.5d,
            pos.getY() + 0.5d,
            pos.getZ() + 0.5d,
            8,0.3d,0.2d,0.3d,0.05d
        );
    }
}
//ლ( ﾟ□ ﾟლ) REMEMBER MEEEEE FOR CENTURIIIIIES