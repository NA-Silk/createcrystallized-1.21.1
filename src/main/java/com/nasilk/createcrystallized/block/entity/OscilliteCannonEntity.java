package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.custom.OscilliteCannonBlock;
import com.nasilk.createcrystallized.particle.ModParticles;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public class OscilliteCannonEntity extends BlockEntity {
    // Variables
    private int charge = 0;
    private int tickCounter = 0;
    private double acceleration = 0;
    private boolean armed = false;
    private final Vector3d cannonPosition = new Vector3d();

    // Constants
    private static final int MAX_CHARGE = 90;
    private static final double LINEAR_SCALE = 15.0d;
    private static final double ANGULAR_SCALE = 1.0d;
    private static final double ACCELERATION_THRESHOLD = 1.0d;
    private static final double FACE_OFFSET = 1.6d;

    private static final int NUM_PARTICLES = 2;
    private static final double PARTICLE_RADIUS = 1.5;

    // Cache
    private static class Cache {
        final Vector3d cannonPositionCurrent = new Vector3d();
        final Vector3d cannonDirection = new Vector3d();
        final Vector3d cannonFace =  new Vector3d();
        final Vector3d angularVelocity = new Vector3d();
        final Vector3d spawnPosition = new Vector3d();
        final Vector3d spawnVelocity = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public OscilliteCannonEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OSCILLITE_CANNON.get(), pos, state);
    }

    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel
            && Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel
        ) {
            // TODO Remove logger controller and variable
            boolean print = tickCounter++ % 40 == 0;

            // Get the physics handle and tick data
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) return;
            Cache cache = CACHE.get();
            BlockState state = getBlockState();

            // Get global position
            cache.cannonPositionCurrent.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            subLevel.logicalPose().transformPosition(cache.cannonPositionCurrent);

            // Get total acceleration
            if (cannonPosition != cache.cannonPositionCurrent) {
                cannonPosition.sub(cache.cannonPositionCurrent); // Get -linearVelocity
                acceleration = LINEAR_SCALE*cannonPosition.lengthSquared(); // Set acceleration = LINEAR_SCALE*||-linearVelocity||^2
                cannonPosition.set(cache.cannonPositionCurrent); // Set cannonPosition = cannonPositionCurrent
            } else {
                acceleration = 0.0d;
            }

            // TODO Remove logger
            if (print) CreateCrystallized.LOGGER.info("Linear Component: {}", acceleration);

            handle.getAngularVelocity(cache.angularVelocity); // Get angularVelocity
            acceleration += ANGULAR_SCALE*cache.angularVelocity.lengthSquared(); // Set acceleration += ANGULAR_SCALE*||angularVelocity||^2

            // TODO Remove logger
            if (print) CreateCrystallized.LOGGER.info("Angular Component: {}", ANGULAR_SCALE*cache.angularVelocity.lengthSquared());
            if (print) CreateCrystallized.LOGGER.info("Total Acceleration: {}", acceleration);

            // Get facing data
            cache.cannonDirection.set(state.getValue(OscilliteCannonBlock.FACING).step());
            cache.cannonFace.set(cannonPosition).fma(FACE_OFFSET, cache.cannonDirection);

            // Charging TODO Custom charging sound
            if (!armed && charge < MAX_CHARGE && acceleration >= ACCELERATION_THRESHOLD) {
                charge++;
                this.setChanged();
                addChargingParticles(serverLevel, cache);

                if (charge >= MAX_CHARGE) {
                    armed = true;
                    this.setChanged();
                    serverLevel.playSound( // TODO Custom arming sound, or something that fits
                        null,
                        worldPosition,
                        SoundEvents.BEACON_ACTIVATE,
                        SoundSource.BLOCKS,
                        1.5F,1.2F
                    );

                    // TODO Remove logger
                    CreateCrystallized.LOGGER.info("CHARGED!");
                }
            }

            // TODO Remove logger
            if (print) {
                if (acceleration >= ACCELERATION_THRESHOLD) {
                    CreateCrystallized.LOGGER.info("Exceeding Threshold: True");
                } else {
                    CreateCrystallized.LOGGER.info("Exceeding Threshold: False");
                }
            }

            // TODO The rest
        }
    }

    private void addChargingParticles(ServerLevel level, Cache cache) {
        // Compute each particle
        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
            double ySpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
            double zSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;

            // Get global vectors
            cache.spawnPosition.set(cache.cannonFace);
            cache.spawnVelocity.set(xSpeed, ySpeed, zSpeed);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            level.sendParticles(
                ModParticles.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(), // TODO Custom charging particles
                cache.cannonFace.x, cache.cannonFace.y, cache.cannonFace.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                xSpeed, ySpeed, zSpeed,
                1.0 // Use above speed values
            );
        }
    }
}
