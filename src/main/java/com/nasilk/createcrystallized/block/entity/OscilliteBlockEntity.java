package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

public class OscilliteBlockEntity extends BlockEntity {
    // Constants
    private static final double DAMPING_SCALE = -0.9d;
    private static final double TORQUE_SCALE = 2.0d;

    // Cache
    private static class Cache {
        final Vector3d blockPosition = new Vector3d();
        final Vector3d angularVelocity = new Vector3d();
        final Vector3d rotation = new Vector3d();
        final Vector3d unitUp = new Vector3d(0.0d, 1.0d, 0.0d); // Read-only reference
        final Vector3d zeroVector = new Vector3d(0.0d, 0.0d, 0.0d); // Read-only reference
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public OscilliteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OSCILLITE_BLOCK.get(), pos, state);
    }

    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel
            && Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel
        ) {
            // Get global position
            Cache cache = CACHE.get();
            cache.blockPosition.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d);
            subLevel.logicalPose().transformPosition(cache.blockPosition);

            // Run gyroscope effect
            gyroscope(subLevel, cache);
        }
    }

    private void gyroscope(ServerSubLevel subLevel, Cache cache) {
        // Get the physics handle
        RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
        if (!handle.isValid()) return;

        // Get rotation vector
        cache.rotation.set(cache.unitUp); // Set to global UP
        subLevel.logicalPose().transformNormal(cache.rotation); // Convert to local UP
        cache.rotation.cross(cache.unitUp); // Cross local with global to get target rotation

        // Handle damping and scale
        handle.getAngularVelocity(cache.angularVelocity);
        cache.rotation.fma(DAMPING_SCALE, cache.angularVelocity);
        cache.rotation.mul(TORQUE_SCALE);

        // Apply rotation
        handle.addLinearAndAngularVelocity(cache.zeroVector, cache.rotation);
    }
}
