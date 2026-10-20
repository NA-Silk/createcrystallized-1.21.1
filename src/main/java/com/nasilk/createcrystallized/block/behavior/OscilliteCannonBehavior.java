package com.nasilk.createcrystallized.block.behavior;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.custom.OscilliteCannonBlock;
import com.nasilk.createcrystallized.block.entity.OscilliteCannonEntity;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.block.CannonConfig;
import com.nasilk.createcrystallized.util.type.TickState;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import java.util.function.Predicate;

// TODO Tune constants with config
public class OscilliteCannonBehavior {
    private final OscilliteCannonEntity be;

    // Variables (unsaved)
    private final int worldPositionHash;
    private final BlockPos.MutableBlockPos fuelPos = new BlockPos.MutableBlockPos();

    // Tick constants
    public static final int MAX_COOLDOWN = 180;
    public static final int RANDOM_TICK_RATE = 20;
    public static final int PACKET_UPDATE_RATE = 10;
    public static final double AMBIENT_RATE = 8e-5d;
    public static final double FACE_OFFSET = 1.6d;

    // Tick constants (config)
    public static int FUEL_RADIUS;

    // Firing constants
    public static final double MAX_RADIUS = 2.12d; // Radial effectiveness distance
    public static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;
    public static final Predicate<Entity> DAMAGE_PREDICATE = entity ->
        !(entity instanceof AbstractContraptionEntity) &&
        !AirCurrent.isPlayerCreativeFlying(entity) &&
        !entity.isSpectator() &&
        !DivingBootsItem.isWornBy(entity);

    // Firing constants (config)
    public static float DAMAGE;
    public static double RECOIL;
    public static double ENTITY_KNOCKBACK;
    public static double SUBLEVEL_KNOCKBACK;
    public static double MAX_RANGE; // Length effectiveness distance

    // Charging particle constants
    public static final int NUM_PARTICLES = 10;
    public static final double PARTICLE_RADIUS = 1.5d;

    // Config constants
    public static void updateConstants() {
        CannonConfig cannonConfig = ModConfigs.server().blockConfig.cannonConfig;
        FUEL_RADIUS = cannonConfig.cannonCrystalRadius.get();
        DAMAGE = cannonConfig.cannonDamage.getF();
        RECOIL = cannonConfig.cannonRecoil.get();
        ENTITY_KNOCKBACK = cannonConfig.cannonEntityKnockback.get();
        SUBLEVEL_KNOCKBACK = cannonConfig.cannonSublevelKnockback.get();
        MAX_RANGE = cannonConfig.cannonMaxRange.get();
    }

    public OscilliteCannonBehavior(OscilliteCannonEntity be) {
        this.be = be;
        this.worldPositionHash = be.getBlockPos().hashCode();
    }


    /// TICK
    // IDLE -> ARMED (randTick && hasCrystalFuel)
    // ARMED -> FIRING_INIT (powered && tryConsumeCrystalFuel)
    // ARMED -> IDLE (powered && !tryConsumeCrystalFuel)
    // FIRING_INIT -> FIRING (*)
    // FIRING -> COOLDOWN (*)
    // COOLDOWN -> IDLE (cooldown <= 0)
    public void tick(ServerLevel serverLevel, ServerSubLevel cannonSubLevel, OscilliteCannonEntity.Cache cache) {
        switch (be.getTickState()) {
            case IDLE -> idle(serverLevel, cache);
            case ARMED -> armed(serverLevel);
            case FIRING_INIT -> firingInit(serverLevel);
            case FIRING -> firing(serverLevel, cannonSubLevel, cache);
            case COOLDOWN -> cooldown(serverLevel, cache);
            default -> {
                be.setTickState(TickState.IDLE);
                be.setChanged();
                serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
            }
        }
    }


    /// STATE HANDLERS
    private void idle(ServerLevel serverLevel, OscilliteCannonEntity.Cache cache) {
        // Play spooky listening noise on occasion
        if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.WARDEN_LISTENING, SoundSource.BLOCKS,
            1.0f, 0.8f
        );

        // IDLE -> ARMED (randTick && hasCrystalFuel)
        if ((serverLevel.getGameTime() + worldPositionHash) % RANDOM_TICK_RATE == 0 && hasCrystalFuel(serverLevel, cache)) {
            serverLevel.playSound(null, be.getBlockPos(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.BLOCKS, 1.5f, 0.8f);
            addChargingParticles(serverLevel, cache);
            be.setTickState(TickState.ARMED);
            be.setChanged();
            serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }
    }

    private void armed(ServerLevel serverLevel) {
        // Play even spookier angry noise on occasion
        if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.WARDEN_LISTENING_ANGRY, SoundSource.BLOCKS,
            1.0f, 0.8f
        );

        // ARMED -> FIRING_INIT (powered && tryConsumeCrystalFuel)
        // ARMED -> IDLE (powered && !tryConsumeCrystalFuel)
        if (be.getBlockState().getValue(OscilliteCannonBlock.POWERED)) {
            if (tryConsumeCrystalFuel(serverLevel)) be.setTickState(TickState.FIRING_INIT);
            else {
                serverLevel.playSound(null, be.getBlockPos(), SoundEvents.WARDEN_AGITATED, SoundSource.BLOCKS, 1.5f, 0.8f);
                be.setTickState(TickState.IDLE);
            }
            be.setChanged();
            serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
        }
    }

    private void firingInit(ServerLevel serverLevel) {
        // Play loud firing noise
        serverLevel.playSound(
            null, be.getBlockPos(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS,
            1.5f, 0.8f
        );

        // FIRING_INIT -> FIRING (*)
        be.setTickState(TickState.FIRING);
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void firing(ServerLevel serverLevel, ServerSubLevel cannonSubLevel, OscilliteCannonEntity.Cache cache) {
        // Set cooldown and run firing effects
        be.setCooldown(MAX_COOLDOWN);
        be.fireCannon(serverLevel, cannonSubLevel, cache);

        // FIRING -> COOLDOWN (*)
        be.setTickState(TickState.COOLDOWN);
        be.setChanged();
        serverLevel.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 2);
    }

    private void cooldown(ServerLevel serverLevel, OscilliteCannonEntity.Cache cache) {
        // Sometimes create some smoke particles to show something is happening
        if ((serverLevel.getGameTime() + worldPositionHash) % RANDOM_TICK_RATE == 0) serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            cache.cannonPosition.x, cache.cannonPosition.y, cache.cannonPosition.z,
            1, 0.5d, 0.5d, 0.5d, 0.1d
        );

        // Decrement cooldown if unpowered
        if (!be.getBlockState().getValue(OscilliteCannonBlock.POWERED)) {
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
    public boolean hasCrystalFuel(ServerLevel serverLevel, OscilliteCannonEntity.Cache cache) {
        // Get coordinates
        BlockPos blockPos = be.getBlockPos();
        int x1 = cache.facing.getStepX() < 0 ? blockPos.getX() - FUEL_RADIUS - 1 : blockPos.getX() - FUEL_RADIUS;
        int x2 = cache.facing.getStepX() > 0 ? blockPos.getX() + FUEL_RADIUS + 1 : blockPos.getX() + FUEL_RADIUS;
        int y1 = cache.facing.getStepY() < 0 ? blockPos.getY() - FUEL_RADIUS - 1 : blockPos.getY() - FUEL_RADIUS;
        int y2 = cache.facing.getStepY() > 0 ? blockPos.getY() + FUEL_RADIUS + 1 : blockPos.getY() + FUEL_RADIUS;
        int z1 = cache.facing.getStepZ() < 0 ? blockPos.getZ() - FUEL_RADIUS - 1 : blockPos.getZ() - FUEL_RADIUS;
        int z2 = cache.facing.getStepZ() > 0 ? blockPos.getZ() + FUEL_RADIUS + 1 : blockPos.getZ() + FUEL_RADIUS;

        // Run check
        for (BlockPos pos : BlockPos.betweenClosed(
            x1, y1, z1,
            x2, y2, z2
        )) if (ModBlocks.ECHO_CRYSTAL_CLUSTER.has(serverLevel.getBlockState(pos))) {
            fuelPos.set(pos);
            return true;
        }
        return false;
    }

    public boolean tryConsumeCrystalFuel(ServerLevel serverLevel) {
        if (ModBlocks.ECHO_CRYSTAL_CLUSTER.has(serverLevel.getBlockState(fuelPos))) {
            serverLevel.destroyBlock(fuelPos, false);
            return true;
        }
        return false;
    }

    public void addChargingParticles(ServerLevel serverLevel, OscilliteCannonEntity.Cache cache) {
        // Compute each particle
        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;
            double ySpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;
            double zSpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;

            // Handle motion
            cache.spawnPosition.set(cache.cannonFace).fma(i, cache.cannonVelocity);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticleTypes.OSCILLITE_CANNON_CHARGING_PARTICLES.get(),
                cache.spawnPosition.x, cache.spawnPosition.y, cache.spawnPosition.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                xSpeed, ySpeed, zSpeed,
                1.0d // Use above speed values
            );
        }
    }


    /// FIRING {@link OscilliteCannonEntity:133}
}
