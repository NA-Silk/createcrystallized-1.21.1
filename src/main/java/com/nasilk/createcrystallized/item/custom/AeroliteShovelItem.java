package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AeroliteShovelItem extends ShovelItem {
    public AeroliteShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    // oh my johd is that a sky paddle
    public static BlockHitResult getSkyPaddleHitResult(Player player) {
        Vec3 hitPosition = player.getEyePosition().add(player.getLookAngle().scale(2.5d));
        return new BlockHitResult(hitPosition, Direction.DOWN, BlockPos.containing(hitPosition), false);
    }

    public static void doSkyPaddleEffects(Player player) {
        // Durability loss on use
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.getCooldowns().addCooldown(ModItems.AEROLITE_SHOVEL.get(), 1);

        // Particles
        ServerLevel level = (ServerLevel) player.level();
        Vec3 lookAngle = player.getLookAngle();
        Vec3 hitPosition = player.getEyePosition().add(lookAngle.scale(2.5d));
        Vec3 particleVelocity = lookAngle.scale(0.2d);
        for (int i = 0; i < 4; i++) {
            double offsetX = (level.random.nextDouble() - 0.5d) * 0.5d;
            double offsetY = (level.random.nextDouble() - 0.5d) * 0.5d;
            double offsetZ = (level.random.nextDouble() - 0.5d) * 0.5d;
            level.sendParticles(
                ParticleTypes.CLOUD,
                hitPosition.x + offsetX, hitPosition.y + offsetY, hitPosition.z + offsetZ,
                0,
                particleVelocity.x, particleVelocity.y, particleVelocity.z,
                1.0d
            );
        }

        // Sound
        level.playSound(
            null,
            hitPosition.x, hitPosition.y, hitPosition.z,
            SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS,
            0.2f, 1.0f
        );
    }
}
