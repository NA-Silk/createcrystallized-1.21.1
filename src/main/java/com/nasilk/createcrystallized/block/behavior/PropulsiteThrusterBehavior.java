package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.custom.PropulsiteThrusterBlock;
import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.common.ModSounds;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.block.ThrusterConfig;
import com.nasilk.createcrystallized.util.type.TickState;
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

    // Variables (unsaved)
    private final int worldPositionHash;

    // Tick constants
    public static final int MAX_COOLDOWN = 100;   // How long it takes for the block to be able to be charged again in ticks
    public static final int MAX_CHARGE = 60;      // How long it takes for the burst to be ready after receiving redstone power in ticks
    public static final int FIRING_DURATION = 10; // How long it takes for the full burst to go though in ticks
    public static final int RANDOM_TICK_RATE = 20;
    public static final int PACKET_UPDATE_RATE = 10;
    public static final int SIMPLE_PARTICLE_RATE = 20;
    public static final int CHARGING_PARTICLE_RATE = 2;
    public static final double AMBIENT_RATE = 8e-5d;
    public static final double FACE_OFFSET = 1.4d;

    // Tick constants (config)
    public static double NORM_STANDARD_DEVIATION; // Curve spread
    public static double NORM_MEAN;               // Curve middle
    public static double NORM_DENOMINATOR;        // Precomputed denominator
    public static double FIRING_AMPLITUDE;         // How much total thrust is output over the length of the burst
    public static double VELOCITY_THRESHOLD;
    public static final double[] NORM_CURVE = new double[FIRING_DURATION];

    // BFS constants
    public static final Direction[] DIRECTIONS = Direction.values();

    // BFS constants (config)
    public static double CLUSTER_BONUS_SCALE;

    // Entity pushing constants
    public static final Predicate<Entity> PUSH_PREDICATE = entity ->
        !(entity instanceof AbstractContraptionEntity) &&
        !AirCurrent.isPlayerCreativeFlying(entity) &&
        !entity.isSpectator() &&
        !DivingBootsItem.isWornBy(entity);

    // Entity pushing constants (config)
    public static double MAX_ENTITY_KNOCKBACK;    // Maximum acceleration allowed in blocks per tick
    public static double MAX_PUSH_RANGE;          // Longitudinal effectiveness distance
    public static double MAX_PUSH_RANGE_2;                 // Half longitudinal effectiveness distance
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
        VELOCITY_THRESHOLD = thrusterConfig.thrusterVelocityThreshold.get();
        CLUSTER_BONUS_SCALE = thrusterConfig.thrusterClusterBonusScale.get();
        MAX_ENTITY_KNOCKBACK = thrusterConfig.thrusterMaxEntityKnockback.get();
        MAX_PUSH_RANGE = thrusterConfig.thrusterMaxPushRange.get();
        MAX_PUSH_RANGE_2 = MAX_PUSH_RANGE * 0.5d;
        MAX_PUSH_RADIUS = thrusterConfig.thrusterMaxPushRadius.get();
        MAX_PUSH_RADIUS_SQUARED = MAX_PUSH_RADIUS * MAX_PUSH_RADIUS;
        PUSH_SCALE = thrusterConfig.thrusterPushScale.get();
        PUSH_SHIFT_REDUCTION = thrusterConfig.thrusterPushShiftReduction.get();
        DAMAGE_SCALE = thrusterConfig.thrusterDamageScale.get();
    }

    public PropulsiteThrusterBehavior(PropulsiteThrusterEntity be) {
        this.be = be;
        this.worldPositionHash = be.getBlockPos().hashCode();
    }


    /// TICK
    // IDLE -> CHARGING (canCharge)
    // CHARGING -> ARMED (charge >= MAX_CHARGE)
    // CHARGING -> DISCHARGING (!canCharge)
    // DISCHARGING -> IDLE (charge <= 0)
    // DISCHARGING -> CHARGING (canCharge)
    // ARMED -> FIRING_INIT (powered)
    // FIRING_INIT -> FIRING (*)
    // FIRING -> COOLDOWN (firingTick >= FIRING_DURATION)
    // COOLDOWN -> IDLE (cooldown <= 0)
    public void tick(ServerLevel serverLevel, RigidBodyHandle handle, boolean canCharge, PropulsiteThrusterEntity.Cache cache) {
        boolean randTick = (serverLevel.getGameTime() + worldPositionHash) % RANDOM_TICK_RATE == 0;
        if (randTick) be.updateAmplitude(serverLevel, be.getBlockPos());
        switch (be.getTickState()) {
            case IDLE -> idle(serverLevel, canCharge);
            case CHARGING -> charging(serverLevel, canCharge, cache);
            case DISCHARGING -> discharging(serverLevel, canCharge, cache);
            case ARMED -> armed(serverLevel);
            case FIRING_INIT -> firingInit(serverLevel);
            case FIRING -> firing(serverLevel, handle, cache);
            case COOLDOWN -> cooldown(serverLevel, randTick, cache);
            default -> {
                be.setTickState(TickState.IDLE);
                be.setChanged();
                serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
            }
        }
    }


    /// STATE HANDLERS
    private void idle(ServerLevel serverLevel, boolean canCharge) {
        // Play happy little resonation noises on occasion
        if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS,
            1.0f, 0.8f
        );

        // IDLE -> CHARGING (canCharge)
        if (canCharge) {
            be.setTickState(TickState.CHARGING);
            be.setChanged();
            serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }

    }

    private void charging(ServerLevel serverLevel, boolean canCharge, PropulsiteThrusterEntity.Cache cache) {
        // Increment charge and create particles
        be.setCharge(be.getCharge() + 1);
        if (be.getCharge() % CHARGING_PARTICLE_RATE == 0) addChargingParticles(serverLevel, cache);

        // CHARGING -> ARMED (charge >= MAX_CHARGE)
        // CHARGING -> DISCHARGING (!canCharge)
        if (be.getCharge() >= MAX_CHARGE) {
            serverLevel.playSound(null, be.getBlockPos(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1.2f);
            be.setTickState(TickState.ARMED);
        } else if (!canCharge) be.setTickState(TickState.DISCHARGING);
        be.setChanged();
        if (be.getCharge() % PACKET_UPDATE_RATE == 0 || !TickState.CHARGING.equals(be.getTickState())) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);

    }

    private void discharging(ServerLevel serverLevel, boolean canCharge, PropulsiteThrusterEntity.Cache cache) {
        // Decrement charge and create particles
        be.setCharge(be.getCharge() - 1);
        if (be.getCharge() % SIMPLE_PARTICLE_RATE == 0) serverLevel.sendParticles(ParticleTypes.WHITE_SMOKE, cache.thrusterPosition.x, cache.thrusterPosition.y, cache.thrusterPosition.z, 10, 0.5d, 0.5d, 0.5d, 0.1d);

        // DISCHARGING -> IDLE (charge <= 0)
        // DISCHARGING -> CHARGING (canCharge)
        if (be.getCharge() <= 0) be.setTickState(TickState.IDLE);
        else if (canCharge) be.setTickState(TickState.CHARGING);
        be.setChanged();
        if (be.getCharge() % PACKET_UPDATE_RATE == 0 || !TickState.DISCHARGING.equals(be.getTickState())) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void armed(ServerLevel serverLevel) {
        // Play a nice chime noise on occasion
        if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
            1.0f, 0.8f
        );

        // ARMED -> FIRING_INIT (powered)
        if (be.getBlockState().getValue(PropulsiteThrusterBlock.POWERED)) {
            be.setTickState(TickState.FIRING_INIT);
            be.setChanged();
            serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }
    }

    private void firingInit(ServerLevel serverLevel) {
        // Reset charge and firingTick and play loud firing noise
        be.setCharge(0);
        be.setFiringTick(0);
        serverLevel.playSound(
            null, be.getBlockPos(),
            ModSounds.PROPULSITE_THRUSTER_FIRE.get(), SoundSource.BLOCKS,
            1.5f, 1.0f
        );

        // FIRING_INIT -> FIRING (*)
        be.setTickState(TickState.FIRING);
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firing(ServerLevel serverLevel, RigidBodyHandle handle, PropulsiteThrusterEntity.Cache cache) {
        // Set thrust using norm curve and increment firing tick
        be.setThrust(be.getAmplitude() * NORM_CURVE[be.getFiringTick()]);
        be.setFiringTick(be.getFiringTick() + 1);

        // Run firing effects
        cache.thrusterForce.set(cache.facing.step()).mul(-be.getThrust());
        handle.applyImpulseAtPoint(cache.thrusterPositionLocal, cache.thrusterForce);
        be.pushAndDamageEntities(serverLevel, cache);
        addFiringParticles(serverLevel, cache);

        // FIRING -> COOLDOWN (firingTick >= FIRING_DURATION)
        if (be.getFiringTick() >= FIRING_DURATION) {
            be.setCooldown(MAX_COOLDOWN);
            be.setThrust(0);
            be.setTickState(TickState.COOLDOWN);
        }
        be.setChanged();
        if (be.getCharge() % PACKET_UPDATE_RATE == 0 || !TickState.FIRING.equals(be.getTickState())) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);

    }

    private void cooldown(ServerLevel serverLevel, boolean randTick, PropulsiteThrusterEntity.Cache cache) {
        // Sometimes create some smoke particles to show something is happening
        if (randTick) serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            cache.thrusterPosition.x, cache.thrusterPosition.y, cache.thrusterPosition.z,
            1, 0.5d, 0.5d, 0.5d, 0.1d
        );

        // Decrement cooldown if unpowered
        if (!be.getBlockState().getValue(PropulsiteThrusterBlock.POWERED)) {
            be.setCooldown(be.getCooldown() - 1);
            be.setChanged();
            if (be.getCooldown() % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }

        // COOLDOWN -> IDLE (cooldown <= 0)
        if (be.getCooldown() <= 0) {
            be.setTickState(TickState.IDLE);
            be.setChanged();
            serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }
    }


    /// CHARGING
    public void addChargingParticles(ServerLevel serverLevel, PropulsiteThrusterEntity.Cache cache) {
        // Compute each particle
        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;
            double ySpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;
            double zSpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;

            // Handle motion
            cache.spawnPosition.set(cache.thrusterFace).fma(i, cache.thrusterVelocity);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticleTypes.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(),
                cache.spawnPosition.x, cache.spawnPosition.y, cache.spawnPosition.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                xSpeed, ySpeed, zSpeed,
                1.0d // Use above speed values
            );
        }
    }


    /// FIRING {@link PropulsiteThrusterEntity:___}
    public void addFiringParticles(ServerLevel serverLevel, PropulsiteThrusterEntity.Cache cache) {
        // Get starting values
        double thrustRatio = Math.max(0.0d, be.getThrust() / be.getMaxThrust()); // [0.0 to 1.0] multiplier based on current thrust strength
        double baseVelocity = MIN_PARTICLE_SPEED + ((MAX_PARTICLE_SPEED - MIN_PARTICLE_SPEED) * thrustRatio); // Faster jet at peak thrust
        int particleCount = MIN_PARTICLES + (int) ((MAX_PARTICLES - MIN_PARTICLES) * thrustRatio);

        // Compute each particle
        for (int i = 0; i < particleCount; i++) {
            // Apply slight random spread to the cone of the thrust
            double dirX = cache.thrusterDirection.x + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);
            double dirY = cache.thrusterDirection.y + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);
            double dirZ = cache.thrusterDirection.z + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);

            // Normalize the direction and scale by baseVelocity
            cache.spawnVelocity.set(dirX, dirY, dirZ).normalize().mul(baseVelocity);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticleTypes.PROPULSITE_THRUSTER_FIRING_PARTICLES.get(),
                cache.thrusterFace.x, cache.thrusterFace.y, cache.thrusterFace.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                cache.spawnVelocity.x, cache.spawnVelocity.y, cache.spawnVelocity.z,
                1.0d // Use spawnVelocity values
            );
        }
    }
}
