package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class PropulsiteThrusterBehavior {
    protected PropulsiteThrusterEntity be;
    private static final int PACKET_UPDATE_RATE = 10;
    private static final int SIMPLE_PARTICLE_RATE = 20;
    private static final int CHARGING_PARTICLE_RATE = 2;
    private static final int MAX_COOLDOWN = 100; // How long it takes for the block to be able to be charged again in ticks
    private static final int BURST_DURATION = 10; // How long it takes for the full burst to go though in ticks
    private static final double AMBIENT_RATE = 8e-5d;
    private static final double STANDARD_DEVIATION = 1.5d; // Curve spread
    private static final double MEAN = 3.0d; // Curve middle
    private static final double[] BURST_CURVE = new double[BURST_DURATION];
    static {
        for (int i = 0; i < BURST_DURATION; i++) {
            double diff = (i - MEAN) / STANDARD_DEVIATION;
            BURST_CURVE[i] = Math.exp(-0.5d * diff * diff);
        }
    }
    public static final int RANDOM_TICK_RATE = 20;
    public static final int MAX_CHARGE = 60; // How long it takes for the burst to be ready after receiving redstone power in ticks
    public static final double FACE_OFFSET = 0.6d;
    public static final double VELOCITY_SCALE = 15.0d;
    public static final double THRESHOLD = 1.0d;
    public static final double AMPLITUDE = 100.0d; // How much total thrust is output over the length of the burst
    public static final double NORM_DENOMINATOR = STANDARD_DEVIATION * Math.sqrt(2.0d * Math.PI); // Precomputed denominator

    public PropulsiteThrusterBehavior(PropulsiteThrusterEntity be) {
        this.be = be;
    }

    public void tick(ServerLevel serverLevel, RigidBodyHandle handle, boolean randTick, boolean powered, PropulsiteThrusterEntity.Cache cache) {
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
            SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.BLOCKS,
            1.5f, 1.0f
        );
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firing(ServerLevel serverLevel, RigidBodyHandle handle, PropulsiteThrusterEntity.Cache cache) {
        // The curve that determines the total thrust of the burst
        be.setThrust((be.getAmplitude() / NORM_DENOMINATOR) // Maximum
            * BURST_CURVE[be.getFiringTick()]); // Curve computation

        // Hande subLevel effects
        cache.thrusterForce.set(cache.facing.step()).mul(-be.getThrust());
        handle.applyImpulseAtPoint(cache.thrusterPositionLocal, cache.thrusterForce);

        // Update firing state
        be.setFiringTick(be.getFiringTick() + 1);
        if (be.getFiringTick() >= BURST_DURATION) {
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
