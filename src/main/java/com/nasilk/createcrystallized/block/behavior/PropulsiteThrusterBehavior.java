package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import com.nasilk.createcrystallized.client.ModSounds;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.block.ThrusterConfig;
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
    private final PropulsiteThrusterEntity be;

    // Tick constants
    public static final int MAX_COOLDOWN = 100;   // How long it takes for the block to be able to be charged again in ticks
    public static final int MAX_CHARGE = 60;      // How long it takes for the burst to be ready after receiving redstone power in ticks
    public static final int FIRING_DURATION = 10; // How long it takes for the full burst to go though in ticks
    public static final int RANDOM_TICK_RATE = 20;
    public static final int PACKET_UPDATE_RATE = 10;
    public static final int SIMPLE_PARTICLE_RATE = 20;
    public static final int CHARGING_PARTICLE_RATE = 2;
    public static final double AMBIENT_RATE = 8e-5d;
    public static final double FACE_OFFSET = 0.6d;

    // Tick constants (config)
    public static double NORM_STANDARD_DEVIATION; // Curve spread
    public static double NORM_MEAN;               // Curve middle
    public static double NORM_DENOMINATOR;        // Precomputed denominator
    public static double FIRING_AMPLITUDE;         // How much total thrust is output over the length of the burst
    public static double VELOCITY_SENSITIVITY;
    public static double VELOCITY_THRESHOLD;
    public static final double[] NORM_CURVE = new double[FIRING_DURATION];

    // BFS constants
    public static final Direction[] DIRECTIONS = Direction.values();

    // BFS constants (config)
    public static double CLUSTER_BONUS_SCALE;

    // Entity pushing constants
    public static final Predicate<Entity> PUSH_PREDICATE = entity ->
        !entity.isSpectator() &&
        !(entity instanceof AbstractContraptionEntity) &&
        !AirCurrent.isPlayerCreativeFlying(entity) &&
        !DivingBootsItem.isWornBy(entity);

    // Entity pushing constants (config)
    public static double MAX_ENTITY_KNOCKBACK;    // Maximum acceleration allowed in blocks per tick
    public static double MAX_PUSH_RANGE;          // Length effectiveness distance
    public static double MAX_PUSH_RADIUS;         // Radial effectiveness distance
    public static double MAX_PUSH_RADIUS_SQUARED; // MAX_PUSH_RADIUS * MAX_PUSH_RADIUS; // Precomputed radial distance squared
    public static double PUSH_SCALE;              // Acceleration multiplier
    public static double PUSH_SHIFT_REDUCTION;    // Acceleration multiplier while holding shift
    public static double DAMAGE_SCALE;            // Thruster damage multiplier

    // Charging particle constants
    public static final int NUM_PARTICLES = 2;         // Number of particles to spawn per tick
    public static final double PARTICLE_RADIUS = 1.5d; // Particle spawn range from the face, in blocks

    // Firing particle constants
    public static final int MIN_PARTICLES = 3;
    public static final int MAX_PARTICLES = 11;
    public static final double PARTICLE_SPREAD = 0.10d;
    public static final double MIN_PARTICLE_SPEED = 0.15d;
    public static final double MAX_PARTICLE_SPEED = 0.5d;

    // Config constants
    public static void updateConstants() {
        ThrusterConfig thrusterConfig = ModConfigs.server().blockConfig.thrusterConfig;
        NORM_STANDARD_DEVIATION = thrusterConfig.thrusterNormStandardDeviation.get();
        NORM_MEAN = thrusterConfig.thrusterNormMean.get();
        NORM_DENOMINATOR = NORM_STANDARD_DEVIATION * Math.sqrt(2.0d * Math.PI);
        for (int i = 0; i < FIRING_DURATION; i++) {
            double diff = (i - NORM_MEAN) / NORM_STANDARD_DEVIATION;
            NORM_CURVE[i] = Math.exp(-0.5d * diff * diff) / NORM_DENOMINATOR;
        }
        FIRING_AMPLITUDE = thrusterConfig.thrusterFiringAmplitude.get();
        VELOCITY_SENSITIVITY = thrusterConfig.thrusterVelocitySensitivity.get();
        VELOCITY_THRESHOLD = thrusterConfig.thrusterVelocityThreshold.get();
        CLUSTER_BONUS_SCALE = thrusterConfig.thrusterClusterBonusScale.get();
        MAX_ENTITY_KNOCKBACK = thrusterConfig.thrusterMaxEntityKnockback.get();
        MAX_PUSH_RANGE = thrusterConfig.thrusterMaxPushRange.get();
        MAX_PUSH_RADIUS = thrusterConfig.thrusterMaxPushRadius.get();
        MAX_PUSH_RADIUS_SQUARED = MAX_PUSH_RADIUS * MAX_PUSH_RADIUS;
        PUSH_SCALE = thrusterConfig.thrusterPushScale.get();
        PUSH_SHIFT_REDUCTION = thrusterConfig.thrusterPushShiftReduction.get();
        DAMAGE_SCALE = thrusterConfig.thrusterDamageScale.get();
    }

    public PropulsiteThrusterBehavior(PropulsiteThrusterEntity be) {
        this.be = be;
    }

    public void tick(ServerLevel serverLevel, RigidBodyHandle handle, boolean powered, PropulsiteThrusterEntity.Cache cache) {
        boolean randTick = (serverLevel.getGameTime() + be.getBlockPos().hashCode()) % RANDOM_TICK_RATE == 0;
        if (randTick) be.updateAmplitude(serverLevel, be.getBlockPos());
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
