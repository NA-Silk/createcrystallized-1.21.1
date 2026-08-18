package com.nasilk.createcrystallized.client.items;

import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import dev.ryanhcode.sable.network.client.ClientSubLevelPunchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AeroliteShovelClient {

    public static void trySkyPaddle() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) return;

        BlockHitResult hitResult = AeroliteShovelItem.getSkyPaddle(minecraft.player);

        ClientSubLevelPunchHelper.clientTryPunch(
                hitResult,
                minecraft.level,
                false
        );

        for (int i = 0; i < 4; i++) {
            double offsetX = (minecraft.level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (minecraft.level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (minecraft.level.random.nextDouble() - 0.5) * 0.5;

            Vec3 particleVelocity = minecraft.player.getLookAngle().scale(0.2);

            minecraft.level.addParticle(
                    ParticleTypes.CLOUD,
                    hitResult.getLocation().x + offsetX,
                    hitResult.getLocation().y + offsetY,
                    hitResult.getLocation().z + offsetZ,
                    particleVelocity.x,
                    particleVelocity.y,
                    particleVelocity.z
            );
        }

        minecraft.level.playLocalSound(
                hitResult.getLocation().x,
                hitResult.getLocation().y,
                hitResult.getLocation().z,
                SoundEvents.BREEZE_DEFLECT,
                SoundSource.PLAYERS,
                0.2F,
                1.0F,
                false
        );





    }
}