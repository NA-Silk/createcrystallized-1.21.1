package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.entity.OscilliteCannonEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class OscilliteCannonBehavior {
    private final OscilliteCannonEntity be;
    private static final int PACKET_UPDATE_RATE = 10;
    private static final int MAX_COOLDOWN = 180;
    private static final double AMBIENT_RATE = 8e-5d;
    public static final int RANDOM_TICK_RATE = 20;
    public static final int FUEL_RADIUS = 1;
    public static final double FACE_OFFSET = 1.6d;

    public OscilliteCannonBehavior(OscilliteCannonEntity be) {
        this.be = be;
    }

    public void tick(ServerLevel serverLevel, boolean randTick, boolean powered, OscilliteCannonEntity.Cache cache) {
        switch (be.getTickState()) {
            case COOLDOWN -> cooldown(serverLevel, randTick, powered, cache);
            case CHARGING -> charging(serverLevel, cache);
            case FIRING_INIT -> firingInitialization(serverLevel);
            case FIRING -> firing(serverLevel, cache);
            default -> idle(serverLevel);
        }
    }

    private void cooldown(ServerLevel serverLevel, boolean randTick, boolean powered, OscilliteCannonEntity.Cache cache) {
        if (randTick) serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            cache.cannonPosition.x, cache.cannonPosition.y, cache.cannonPosition.z,
            1, 0.5d, 0.5d, 0.5d, 0.1d
        );
        if (!powered) {
            be.setCooldown(be.getCooldown() - 1);
            be.setChanged();
            if (be.getCooldown() % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }
    }

    private void charging(ServerLevel serverLevel, OscilliteCannonEntity.Cache cache) {
        be.setArmed(true);
        serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.BLOCKS,
            1.5f, 0.8f
        );
        be.addChargingParticles(serverLevel, cache);
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firingInitialization(ServerLevel serverLevel) {
        be.setFiring(true);
        serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS,
            1.5f, 0.8f
        );
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firing(ServerLevel serverLevel, OscilliteCannonEntity.Cache cache) {
        be.setFiring(false);
        be.setArmed(false);
        be.setCooldown(MAX_COOLDOWN);
        be.fireCannon(serverLevel, cache);
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void idle(ServerLevel serverLevel) {
        if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) {
            if (!be.getArmed()) serverLevel.playSound(
                null, be.getBlockPos(),
                SoundEvents.WARDEN_LISTENING, SoundSource.BLOCKS,
                1.0f, 0.8f
            );
            else serverLevel.playSound(
                null, be.getBlockPos(),
                SoundEvents.WARDEN_LISTENING_ANGRY, SoundSource.BLOCKS,
                1.0f, 0.8f
            );
        }
    }
}
