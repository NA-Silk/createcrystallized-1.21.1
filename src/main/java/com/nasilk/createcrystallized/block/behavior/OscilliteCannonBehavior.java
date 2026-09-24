package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.entity.OscilliteCannonEntity;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.block.CannonConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// TODO Tune constants with config
public class OscilliteCannonBehavior {
    private final OscilliteCannonEntity be;

    // Tick constants
    public static final int MAX_COOLDOWN = 180;
    public static final int FUEL_RADIUS = 1;
    public static final int RANDOM_TICK_RATE = 20;
    public static final int PACKET_UPDATE_RATE = 10;
    public static final double AMBIENT_RATE = 8e-5d;
    public static final double FACE_OFFSET = 1.6d;

    // Firing constants (config)
    public static double DAMAGE;
    public static double RECOIL;
    public static double ENTITY_KNOCKBACK;
    public static double SUBLEVEL_KNOCKBACK;
    public static double MAX_RANGE;          // Length effectiveness distance
    public static double MAX_RADIUS;         // Radial effectiveness distance
    public static double MAX_RADIUS_SQUARED;

    // Charging particle constants
    public static final int NUM_PARTICLES = 10;
    public static final double PARTICLE_RADIUS = 1.5d;

    // Config constants
    public static void updateConstants() {
        CannonConfig cannonConfig = ModConfigs.server().blockConfig.cannonConfig;
        DAMAGE = cannonConfig.cannonDamage.get();
        RECOIL = cannonConfig.cannonRecoil.get();
        ENTITY_KNOCKBACK = cannonConfig.cannonEntityKnockback.get();
        SUBLEVEL_KNOCKBACK = cannonConfig.cannonSublevelKnockback.get();
        MAX_RANGE = cannonConfig.cannonMaxRange.get();
        MAX_RADIUS = cannonConfig.cannonMaxRadius.get();
        MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;
    }

    public OscilliteCannonBehavior(OscilliteCannonEntity be) {
        this.be = be;
    }

    public void tick(ServerLevel serverLevel, boolean powered, OscilliteCannonEntity.Cache cache) {
        boolean randTick = (serverLevel.getGameTime() + be.getBlockPos().hashCode()) % RANDOM_TICK_RATE == 0;
        switch (be.getTickState()) {
            case COOLDOWN -> cooldown(serverLevel, randTick, powered, cache);
            case CHARGING -> { if (randTick) charging(serverLevel, cache); }
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
