package com.nasilk.createcrystallized.item.custom;

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
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AeroliteShovelItem extends ShovelItem {
    public static final Tier AEROLITE_TIER = Tiers.DIAMOND; // TODO custom tier

    public AeroliteShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    //oh my johd is that a sky paddle
    public static BlockHitResult getSkyPaddle(Player player) {
        Vec3 hitPosition = player.getEyePosition().add(player.getLookAngle().scale(2.5));
        return new BlockHitResult(hitPosition, Direction.DOWN, BlockPos.containing(hitPosition), false);
    }

    // Durability loss on use
    public static void damageSkyPaddle(Player player) {
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    // PARTICLES
    public static void skyPaddleParticles(ServerLevel serverLevel, Player player, BlockHitResult hitResult) {
        Vec3 pos = hitResult.getLocation();
        Vec3 particleVelocity = player.getLookAngle().scale(0.2);
        for (int i = 0; i < 4; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
            double offsetY = (serverLevel.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                0,
                particleVelocity.x, particleVelocity.y, particleVelocity.z,
                1
            );
        }
    }

    // SOUNDS
    public static void skyPaddleSound(ServerLevel serverLevel, BlockHitResult hitResult) {
        Vec3 pos = hitResult.getLocation();
        serverLevel.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS,
            0.2F, 1.0F
        );
    }
}
