package com.nasilk.createcrystallized.item.custom;

import dev.simulated_team.simulated.index.SimSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;

public class AeroliteAxeItem extends AxeItem {
    public AeroliteAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static void doAssemblyAxeEffects(ServerLevel level, BlockPos pos) {
        level.sendParticles(
            ParticleTypes.CLOUD,
            pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 8,
            0.5d, 0.5d, 0.5d, 1.0d
        );
        level.playSound(
            null, pos,
            SimSoundEvents.SIMULATED_CONTRAPTION_MOVES.event(), SoundSource.BLOCKS,
            1.0f, 1.0f
        );
    }
}
