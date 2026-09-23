package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import com.nasilk.createcrystallized.client.ModSounds;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

// TODO Tune constants with config
public class PropulsiteThrusterBehavior {
    protected PropulsiteThrusterEntity be;

    // Tick constants
    private static final int MAX_COOLDOWN = 100; // How long it takes for the block to be able to be charged again in ticks
    private static final int FIRING_DURATION = 10; // How long it takes for the full burst to go though in ticks
    private static final int RANDOM_TICK_RATE = 20;
    private static final int PACKET_UPDATE_RATE = 10;
    private static final int SIMPLE_PARTICLE_RATE = 20;
    private static final int CHARGING_PARTICLE_RATE = 2;
    private static final double AMBIENT_RATE = 8e-5d;
    public final int MAX_CHARGE = 60; // How long it takes for the burst to be ready after receiving redstone power in ticks
    public final double FACE_OFFSET = 0.6d;
    private double NORM_STANDARD_DEVIATION = Double.NaN; // 1.5d; // Curve spread
    private double NORM_MEAN = Double.NaN; // 3.0d; // Curve middle
    private double NORM_DENOMINATOR = Double.NaN; // NORM_STANDARD_DEVIATION * Math.sqrt(2.0d * Math.PI); // Precomputed denominator
    public double FIRING_AMPLITUDE = Double.NaN; // 100.0d; // How much total thrust is output over the length of the burst
    public double VELOCITY_SENSITIVITY = Double.NaN; // 15.0d;
    public double VELOCITY_THRESHOLD = Double.NaN; // 1.0d;
    private final double[] NORM_CURVE = new double[FIRING_DURATION];

    // BFS constants
    public final Direction[] DIRECTIONS = Direction.values();
    public double CLUSTER_BONUS_SCALE = Double.NaN; // 2.0d;

    // Entity pushing constants
    public final Predicate<Entity> PUSH_PREDICATE = entity ->
        !entity.isSpectator() &&
        !(entity instanceof AbstractContraptionEntity) &&
        !AirCurrent.isPlayerCreativeFlying(entity) &&
        !DivingBootsItem.isWornBy(entity);
    public double MAX_ENTITY_KNOCKBACK = Double.NaN; // 6.0d; // Maximum acceleration allowed in blocks per tick
    public double MAX_PUSH_RANGE = Double.NaN; // 8.0d; // Length effectiveness distance
    public double MAX_PUSH_RADIUS = Double.NaN; // 0.75d; // Radial effectiveness distance
    public double MAX_PUSH_RADIUS_SQUARED = Double.NaN; // MAX_PUSH_RADIUS * MAX_PUSH_RADIUS; // Precomputed radial distance squared
    public double PUSH_SCALE = Double.NaN; // 0.1d; // Acceleration multiplier
    public double PUSH_SHIFT_REDUCTION = Double.NaN; // 8.0d; // Acceleration multiplier while holding shift
    public double DAMAGE_SCALE = Double.NaN; // 5.0d; // Thruster damage multiplier

    // Charging particle constants
    public final int NUM_PARTICLES = 2; // Number of particles to spawn per tick
    public final double PARTICLE_RADIUS = 1.5d; // Particle spawn range from the face, in blocks

    // Firing particle constants
    public final int MIN_PARTICLES = 3;
    public final int MAX_PARTICLES = 11;
    public final double PARTICLE_SPREAD = 0.10d;
    public final double MIN_PARTICLE_SPEED = 0.15d;
    public final double MAX_PARTICLE_SPEED = 0.5d;

    // Config constants
    private boolean updateConstants() { // Don't worry about it
        double tempVar; boolean changed = false;
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterNormStandardDeviation.get(); if (NORM_STANDARD_DEVIATION != tempVar) { NORM_STANDARD_DEVIATION = tempVar; changed = true; NORM_DENOMINATOR = NORM_STANDARD_DEVIATION * Math.sqrt(2.0d * Math.PI); }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterNormMean.get();              if (NORM_MEAN               != tempVar) { NORM_MEAN               = tempVar; changed = true; }
        if (changed) for (int i = 0; i < FIRING_DURATION; i++) {
            double diff = (i - NORM_MEAN) / NORM_STANDARD_DEVIATION;
            NORM_CURVE[i] = Math.exp(-0.5d * diff * diff) / NORM_DENOMINATOR;
        }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterFiringAmplitude.get();       if (FIRING_AMPLITUDE        != tempVar) { FIRING_AMPLITUDE         = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterVelocitySensitivity.get();   if (VELOCITY_SENSITIVITY    != tempVar) { VELOCITY_SENSITIVITY     = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterVelocityThreshold.get();     if (VELOCITY_THRESHOLD      != tempVar) { VELOCITY_THRESHOLD       = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterClusterBonusScale.get();     if (CLUSTER_BONUS_SCALE     != tempVar) { CLUSTER_BONUS_SCALE      = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterMaxEntityKnockback.get();    if (MAX_ENTITY_KNOCKBACK    != tempVar) { MAX_ENTITY_KNOCKBACK     = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterMaxPushRange.get();          if (MAX_PUSH_RANGE          != tempVar) { MAX_PUSH_RANGE           = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterMaxPushRadius.get();         if (MAX_PUSH_RADIUS         != tempVar) { MAX_PUSH_RADIUS          = tempVar; changed = true; MAX_PUSH_RADIUS_SQUARED = MAX_PUSH_RADIUS * MAX_PUSH_RADIUS; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterPushScale.get();             if (PUSH_SCALE              != tempVar) { PUSH_SCALE               = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterPushShiftReduction.get();    if (PUSH_SHIFT_REDUCTION    != tempVar) { PUSH_SHIFT_REDUCTION     = tempVar; changed = true; }
        tempVar = ModConfigs.server().blockConfig.thrusterConfig.thrusterDamageScale.get();           if (DAMAGE_SCALE            != tempVar) { DAMAGE_SCALE             = tempVar; changed = true; }
        return changed;
    }

    public double getPeakThrust(double currentAmplitude) {
        if (Double.isNaN(NORM_DENOMINATOR) || NORM_DENOMINATOR == 0) return 0.0d;
        return currentAmplitude / NORM_DENOMINATOR;
    }

    public PropulsiteThrusterBehavior(PropulsiteThrusterEntity be) {
        this.be = be;
        updateConstants();
    }

    public void tick(ServerLevel serverLevel, RigidBodyHandle handle, boolean powered, PropulsiteThrusterEntity.Cache cache) {
        boolean randTick = (serverLevel.getGameTime() + be.getBlockPos().hashCode()) % RANDOM_TICK_RATE == 0;
        if (randTick) {
            if (updateConstants()) { // Handle config changes
                serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
                be.setChanged();
            }
            be.updateAmplitude(serverLevel, be.getBlockPos());
        }
        switch (be.getTickState()) {
            case COOLDOWN -> cooldown(serverLevel, randTick, powered, cache);
            case CHARGING -> charging(serverLevel, cache);
            case DISCHARGING -> discharging(serverLevel, cache);
            case FIRING_INIT -> firingInitialization(serverLevel);
            case FIRING -> firing(serverLevel, handle, cache);
            default -> idle(serverLevel);
        }
    }

    private void cooldown(ServerLevel serverLevel, boolean randTick, boolean powered, PropulsiteThrusterEntity.Cache cache) {
        if (randTick) serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            cache.thrusterPosition.x, cache.thrusterPosition.y, cache.thrusterPosition.z,
            1, 0.5d, 0.5d, 0.5d, 0.1d
        );
        if (!powered) {
            be.setCooldown(be.getCooldown() - 1);
            be.setChanged();
            if (be.getCooldown() % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }
    }

    private void charging(ServerLevel serverLevel, PropulsiteThrusterEntity.Cache cache) {
        be.setCharge(be.getCharge() + 1);
        if (be.getCharge() >= MAX_CHARGE) {
            be.setArmed(true);
            serverLevel.playSound(
                null, be.getBlockPos(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS,
                1.5f, 1.2f
            );
        }
        if (be.getCharge() % CHARGING_PARTICLE_RATE == 0) be.addChargingParticles(serverLevel, cache);
        be.setChanged();
        if (be.getCharge() % PACKET_UPDATE_RATE == 0 || be.getArmed()) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void discharging(ServerLevel serverLevel, PropulsiteThrusterEntity.Cache cache) {
        be.setCharge(be.getCharge() - 1);
        if (be.getCharge() % SIMPLE_PARTICLE_RATE == 0) {
            serverLevel.sendParticles(
                ParticleTypes.WHITE_SMOKE,
                cache.thrusterPosition.x, cache.thrusterPosition.y, cache.thrusterPosition.z,
                10, 0.5d, 0.5d, 0.5d, 0.1d
            );
        }
        be.setChanged();
        if (be.getCharge() % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firingInitialization(ServerLevel serverLevel) {
        be.setFiring(true);
        be.setFiringTick(0);
        serverLevel.playSound(
            null, be.getBlockPos(),
            ModSounds.PROPULSITE_THRUSTER_FIRE.get(), SoundSource.BLOCKS,
            1.5f, 1.0f
        );
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firing(ServerLevel serverLevel, RigidBodyHandle handle, PropulsiteThrusterEntity.Cache cache) {
        // The curve that determines the total thrust of the burst
        be.setThrust(be.getAmplitude() * NORM_CURVE[be.getFiringTick()]); // Curve computation

        // Hande subLevel effects
        cache.thrusterForce.set(cache.facing.step()).mul(-be.getThrust());
        handle.applyImpulseAtPoint(cache.thrusterPositionLocal, cache.thrusterForce);

        // Update firing state
        be.setFiringTick(be.getFiringTick() + 1);
        if (be.getFiringTick() >= FIRING_DURATION) {
            be.setFiring(false);
            be.setArmed(false);
            be.setCharge(0);
            be.setThrust(0);
            be.setCooldown(MAX_COOLDOWN);
        }

        // Effects
        be.pushEntities(serverLevel, cache);
        be.addFiringParticles(serverLevel, cache);
        be.setChanged();
        if (be.getFiringTick() % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void idle(ServerLevel serverLevel) {
        if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) {
            if (be.getCharge() == 0) serverLevel.playSound(
                null, be.getBlockPos(),
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS,
                1.0f, 0.8f
            );
            else if (be.getCharge() == MAX_CHARGE) serverLevel.playSound(
                null, be.getBlockPos(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                1.0f, 0.8f
            );
        }
    }
}
