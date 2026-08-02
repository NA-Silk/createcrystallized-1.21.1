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
    private static final int TICK_RATE = 5;

    // Cache
    private static class Cache {
        final Vector3d blockPosition = new Vector3d();
        final Vector3d angularVelocity = new Vector3d();
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
            cache.blockPosition.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            subLevel.logicalPose().transformPosition(cache.blockPosition);

            // Run gyroscope effect
            if ((serverLevel.getGameTime() + worldPosition.hashCode()) % TICK_RATE == 0) gyroscope(subLevel, cache);
        }
    }

    private void gyroscope(ServerSubLevel subLevel, Cache cache) {
        // Get the physics handle
        RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
        if (!handle.isValid()) return;

        // Get angular velocity and apply negation of x and z components
        handle.getAngularVelocity(cache.angularVelocity);
        cache.angularVelocity.set(-cache.angularVelocity.x, 0, -cache.angularVelocity.z);
        handle.addLinearAndAngularVelocity(cache.zeroVector, cache.angularVelocity);
    }
}
