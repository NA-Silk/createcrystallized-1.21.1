package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.entity.OscilliteCannonEntity;
import com.nasilk.createcrystallized.config.ModConfigs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// TODO Tune constants with config
public class OscilliteCannonBehavior {
    private final OscilliteCannonEntity be;

    // Tick constants
    private static final int MAX_COOLDOWN = 180;
    private static final int RANDOM_TICK_RATE = 20;
    private static final int PACKET_UPDATE_RATE = 10;
    private static final double AMBIENT_RATE = 8e-5d;
    public final int FUEL_RADIUS = 1;
    public final double FACE_OFFSET = 1.6d;

    // Firing constants
    public double DAMAGE = Float.NaN; // 50.0f;
    public double RECOIL = Double.NaN; // 25.0d;
    public double ENTITY_KNOCKBACK = Double.NaN; // 3.0d;
    public double SUBLEVEL_KNOCKBACK = Double.NaN; // 500.0d;
    public double MAX_RANGE = Double.NaN; // 80.0d; // Length effectiveness distance
    public double MAX_RADIUS = Double.NaN; // 2.12d; // Radial effectiveness distance
    public double MAX_RADIUS_SQUARED = Double.NaN;

    // Charging particle constants
    public final int NUM_PARTICLES = 10;
    public final double PARTICLE_RADIUS = 1.5d;

    // Config constants
    private boolean updateConstants() { // Don't worry about it
        double tempVar; boolean changed = false;
        tempVar = ModConfigs.server().blockConfig.cannonConfig.cannonDamage.get();            if (DAMAGE             != tempVar) { DAMAGE             = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.cannonConfig.cannonRecoil.get();            if (RECOIL             != tempVar) { RECOIL             = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.cannonConfig.cannonEntityKnockback.get();   if (ENTITY_KNOCKBACK   != tempVar) { ENTITY_KNOCKBACK   = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.cannonConfig.cannonSublevelKnockback.get(); if (SUBLEVEL_KNOCKBACK != tempVar) { SUBLEVEL_KNOCKBACK = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.cannonConfig.cannonMaxRange.get();          if (MAX_RANGE          != tempVar) { MAX_RANGE          = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.cannonConfig.cannonMaxRadius.get();         if (MAX_RADIUS         != tempVar) { MAX_RADIUS         = tempVar; changed = true; MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS; }
        return changed;
    }

    public OscilliteCannonBehavior(OscilliteCannonEntity be) {
        this.be = be;
        updateConstants();
    }

    public void tick(ServerLevel serverLevel, boolean powered, OscilliteCannonEntity.Cache cache) {
        boolean randTick = (serverLevel.getGameTime() + be.getBlockPos().hashCode()) % RANDOM_TICK_RATE == 0;
        if (randTick && updateConstants()) { // Handle config changes
            serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
            be.setChanged();
        }
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
