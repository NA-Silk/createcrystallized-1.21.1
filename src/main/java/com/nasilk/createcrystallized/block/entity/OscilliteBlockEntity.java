package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.block.OscilliteConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class OscilliteBlockEntity extends BlockEntity {
    // Constants
    private static final Vector3dc ZERO_VECTOR = new Vector3d(0.0d, 0.0d, 0.0d); // Read-only reference
    private static final double PHYSICS_DT = 1.0d / 20.0d;
    //private static final double TORQUE_SCALE = 0.76d; // This value PERFECTLY holds 4kpg on a swivel without tipping
    //private static final double DAMPING_SCALE = 0.96d;
    //private static final double NATURAL_FREQUENCY = 3.0d;
    //private static final double DAMPING_RATIO = 1.0d;

    // Config constants (called from config on load & reload)
    private static int GYRO_FUNCTION;
    private static double TORQUE_SCALE;
    private static double DAMPING_SCALE;
    private static double MASS_CAPACITY;
    private static double K_p;
    private static double K_d;
    public static void updateConstants() {
        OscilliteConfig config = ModConfigs.server().blockConfig.oscilliteConfig;
        GYRO_FUNCTION = config.gyroFunction.get();
        TORQUE_SCALE = config.torqueScale.get();
        DAMPING_SCALE = config.dampingScale.get();
        MASS_CAPACITY = config.massCapacity.get();
        double NATURAL_FREQUENCY = config.naturalFrequency.get();
        double DAMPING_RATIO = config.dampingRatio.get();
        // K_p = w_n * w_n
        // K_d = 2 * d * w_n
        // : K_p = leveling strength (proportional), w_n = natural frequency
        // : K_d = rotational dampening strength (derivative), d = damping ratio [for critical damping, d = 1]
        K_p = NATURAL_FREQUENCY * NATURAL_FREQUENCY;
        K_d = 2.0d * DAMPING_RATIO * NATURAL_FREQUENCY;

    }

    // Cache
    private static class Cache {
        final Vector3d angularVelocity = new Vector3d();
        final Vector3d impulse = new Vector3d();
        final Vector3d temp = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    private enum UnitDir {
        UP(new Vector3d(0, 1, 0)),
        DOWN(new Vector3d(0, -1, 0)),
        NORTH(new Vector3d(0, 0, -1)),
        SOUTH(new Vector3d(0, 0, 1)),
        EAST(new Vector3d(1, 0, 0)),
        WEST(new Vector3d(-1, 0, 0));
        private final Vector3d normal;

        UnitDir(Vector3d normal) { this.normal = normal; }
        public Vector3d getNormal() { return normal; }
        private static final UnitDir[] VALUES = values();
    }

    public OscilliteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel && Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
            Cache cache = CACHE.get();
            switch(GYRO_FUNCTION) {
                case 1 -> gyroscope1(subLevel, cache); // Retained for comparison testing
                case 2 -> gyroscope2(subLevel, cache); // Default config selection
            }
        }
    }

    /// Gyro Function 1
    // Correction function:
    // dw = 0.76 * ( n X u ) - 0.96 * w_t
    private void gyroscope1(ServerSubLevel subLevel, Cache cache) {
        // Get the physics handle
        RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
        if (!handle.isValid()) return;

        // Find closest upward face and get target rotation vector
        double max = Double.NEGATIVE_INFINITY;
        for (UnitDir unitDir : UnitDir.VALUES) {
            cache.temp.set(unitDir.getNormal()); // Set to local orientation
            subLevel.logicalPose().transformNormal(cache.temp); // Convert to global orientation
            double dot = cache.temp.dot(UnitDir.UP.getNormal()); // Compare to global UP
            if (dot > max) {
                max = dot;
                cache.impulse.set(cache.temp); // Store closest global orientation
            }
        }

        // Compute global orientation error
        // e = n X u
        // : e = orientation error
        // : n = current up face, u = world up
        cache.impulse.cross(UnitDir.UP.getNormal()); // Cross to get target rotation

        // Get angular velocity of tilt
        handle.getAngularVelocity(cache.angularVelocity);
        cache.angularVelocity.y = 0.0d; // No yaw damping

        // Compute damped angular velocity delta
        // dw = 0.76 * e - 0.96 * w_t
        // : dw = angular velocity delta
        // : w_t = angular velocity of tilt
        cache.impulse.mul(TORQUE_SCALE);
        cache.impulse.fma(-DAMPING_SCALE, cache.angularVelocity);

        // Apply rotation
        handle.addLinearAndAngularVelocity(ZERO_VECTOR, cache.impulse);
    }

    /// Gyro Function 2 *new*
    // Correction function: J = I * ( w_n * w_n * ( n X u ) - 2 * d * w_n * w_t ) * dt
    private void gyroscope2(ServerSubLevel subLevel, Cache cache) {
        // Get the physics handle
        RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
        if (!handle.isValid()) return;

        // Get mass tracker
        MassData massData = subLevel.getMassTracker();
        if (massData == null || massData.isInvalid()) return;

        // Find closest upward face and get target rotation vector
        double max = Double.NEGATIVE_INFINITY;
        for (UnitDir unitDir : UnitDir.VALUES) {
            cache.temp.set(unitDir.getNormal()); // Set to local orientation
            subLevel.logicalPose().transformNormal(cache.temp); // Convert to global orientation
            double dot = cache.temp.dot(UnitDir.UP.getNormal()); // Compare to global UP
            if (dot > max) {
                max = dot;
                cache.impulse.set(cache.temp); // Store closest global orientation
            }
        }

        // Compute global orientation error
        // e = n X u
        // : e = orientation error
        // : n = current up face, u = world up
        cache.impulse.cross(UnitDir.UP.getNormal()); // Cross to get target rotation

        // Get angular velocity of tilt
        handle.getAngularVelocity(cache.angularVelocity);
        cache.angularVelocity.y = 0.0d; // No yaw damping

        // Compute proportional derivative controller correction (angular acceleration)
        // a = K_p * e - K_d * w_t
        // : a = angular acceleration
        // : w_t = angular velocity of tilt
        cache.impulse.mul(K_p);
        cache.impulse.fma(-K_d, cache.angularVelocity);

        // Scale angular acceleration by mass authority
        cache.impulse.mul(Math.min(1.0d, MASS_CAPACITY / massData.getMass()));

        // Convert to local
        subLevel.logicalPose().transformNormalInverse(cache.impulse);

        // Compute impulse from torque
        // J = T * dt
        // T = I * a
        // : J = impulse, T = torque, dt = time delta of tick
        // : I = moment of inertia, a = angular acceleration,
        massData.getInertiaTensor().transform(cache.impulse);
        cache.impulse.mul(PHYSICS_DT);

        // Apply impulse
        handle.applyTorqueImpulse(cache.impulse);
    }
}
