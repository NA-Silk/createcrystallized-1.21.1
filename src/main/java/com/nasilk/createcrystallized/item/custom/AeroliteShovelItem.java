package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.item.ModItems;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import org.joml.Vector3d;

// oh my johd is that a sky paddle
public class AeroliteShovelItem extends ShovelItem {
    public AeroliteShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public static void doSkyPaddleEffects(ServerLevel level, Vector3d origin, Vector3d lookAngle, double distance) {
        // Particles
        origin.fma(distance, lookAngle);
        lookAngle.mul(0.2d);
        for (int i = 0; i < 4; i++) {
            double offsetX = (level.random.nextDouble() - 0.5d) * 0.5d;
            double offsetY = (level.random.nextDouble() - 0.5d) * 0.5d;
            double offsetZ = (level.random.nextDouble() - 0.5d) * 0.5d;
            level.sendParticles(
                ParticleTypes.CLOUD,
                origin.x + offsetX, origin.y + offsetY, origin.z + offsetZ,
                0,
                lookAngle.x, lookAngle.y, lookAngle.z,
                1.0d
            );
        }

        // Sound
        level.playSound(
            null,
            origin.x, origin.y, origin.z,
            SoundEvents.BREEZE_DEFLECT, SoundSource.PLAYERS,
            0.2f, 1.0f
        );
    }

    public static void doSkyPaddleEffects(Player player) {
        // Durability loss on use
        player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.getCooldowns().addCooldown(ModItems.AEROLITE_SHOVEL.get(), 2);

        // Particles and sound
        ServerLevel level = (ServerLevel) player.level();
        Vector3d origin = JOMLConversion.toJOML(player.getEyePosition());
        Vector3d lookAngle = JOMLConversion.toJOML(player.getLookAngle());
        doSkyPaddleEffects(level, origin, lookAngle, 2.5d);
    }
}
