package com.nasilk.createcrystallized.item.entity;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.particle.ModParticles;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.List;

// TODO NICK DO FUN THINGS HERE
public class DensiteCoreEntity extends ThrowableItemProjectile {
    public DensiteCoreEntity(EntityType<? extends DensiteCoreEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DensiteCoreEntity(Level level, LivingEntity shooter) {
        super(ModEntities.THROWN_DENSITE_CORE.get(), shooter, level);
    }

    public DensiteCoreEntity(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_DENSITE_CORE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DENSITE_CORE.get();
    }

    // TODO Test this
    private static final double PARTICLE_RATE = 0.05d;
    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel)
            if (this.level().random.nextDouble() < PARTICLE_RATE)
                serverLevel.broadcastEntityEvent(this, (byte) 3); // Triggers handleEntityEvent() on client
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, (byte) 3); // Triggers handleEntityEvent() on client
            this.discard();
            this.updateSublevelTargets();
            this.applySublevelGravity();
            this.applyEntityGravity();
            CreateCrystallized.LOGGER.info("HIT");
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 5.0f);
            CreateCrystallized.LOGGER.info("ENTITY HIT");
        }
    }

    @SuppressWarnings("deprecation") // TODO Delete line
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            BlockPos blockPos = result.getBlockPos(); // TODO Delete line
            if (serverLevel.getBlockState(blockPos).getBlock().getExplosionResistance() <= 6.0f) serverLevel.destroyBlock(blockPos, false); // TODO Delete line
            CreateCrystallized.LOGGER.info("BLOCK HIT");
        }
    }

    private final List<SubLevel> sublevelTargets = new ArrayList<>();
    private final BoundingBox3d searchBox = new BoundingBox3d();
    private static final int FIELD_RADIUS = 5;
    private void updateSublevelTargets() {
        // Reset the target list
        sublevelTargets.clear();

        // Get Sable's SubLevel container for this dimension
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (!(SubLevelContainer.getContainer(serverLevel) instanceof ServerSubLevelContainer container)) return;

        // Set the bounding box
        searchBox.setUnchecked(
            this.position().x - FIELD_RADIUS, this.position().y - FIELD_RADIUS, this.position().z - FIELD_RADIUS,
            this.position().x + FIELD_RADIUS, this.position().y + FIELD_RADIUS, this.position().z + FIELD_RADIUS
        );

        // Populate the target list
        container.queryIntersecting(searchBox).forEach(targetSubLevel -> {
            if (targetSubLevel != Sable.HELPER.getContaining(serverLevel, this.position())) sublevelTargets.add(targetSubLevel);
        });
    }

    private final Vector3d corePosition = new Vector3d();
    private final Vector3d targetPosition = new Vector3d();
    private final Vector3d impulseVelocity = new Vector3d();
    private final Vector3d currentLinearVelocity = new Vector3d();
    private final Vector3d currentAngularVelocity = new Vector3d();
    private final Vector3d zeroVector = new Vector3d(0.0d, 0.0d, 0.0d);
    private static final int FIELD_RADIUS_SQUARED = FIELD_RADIUS * FIELD_RADIUS;
    private static final int FIELD_STRENGTH = 8;
    private static final double IMPACT_RADIUS = 0.5d;
    private static final double IMPACT_RADIUS_SQUARED = IMPACT_RADIUS * IMPACT_RADIUS;
    private static final double DAMPEN_RADIUS = 1.5d;
    private static final double DAMPEN_RADIUS_SQUARED = DAMPEN_RADIUS * DAMPEN_RADIUS;
    private static final double DAMPEN_STRENGTH = FIELD_STRENGTH / DAMPEN_RADIUS_SQUARED;
    private static final double DAMPEN_SCALE = 0.2d;
    private void applySublevelGravity() {
        // Get position as a Vector3d object
        corePosition.set(this.position().x, this.position().y, this.position().z);

        // Iterate backwards for safe removals
        for (int i = sublevelTargets.size() - 1; i >= 0; i--) {
            SubLevel targetSubLevel = sublevelTargets.get(i);

            // If the sublevel was destroyed or unloaded, remove it from the list
            if (!(targetSubLevel instanceof ServerSubLevel subLevel) || targetSubLevel.isRemoved()) {
                sublevelTargets.remove(i);
                continue;
            }

            // Get the physics handle
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) continue;

            // Get sublevel position, impulse velocity, and current distance^2
            targetPosition.set(targetSubLevel.logicalPose().position());
            impulseVelocity.set(corePosition).sub(targetPosition);
            double distanceSquared = impulseVelocity.lengthSquared();

            // Handle out of range entities
            if (distanceSquared > FIELD_RADIUS_SQUARED) {
                sublevelTargets.remove(i);
                continue;
            }

            // Handle impact when very close
            if (distanceSquared < IMPACT_RADIUS_SQUARED) {
                // Get current linear and angular velocity
                handle.getLinearVelocity(currentLinearVelocity);
                handle.getAngularVelocity(currentAngularVelocity);

                // Apply opposite vectors to negate current motion
                currentLinearVelocity.negate();
                currentAngularVelocity.negate();
                handle.addLinearAndAngularVelocity(currentLinearVelocity, currentAngularVelocity);
                continue;
            }

            // Handle dampening relative to well radius
            if (distanceSquared < DAMPEN_RADIUS_SQUARED) {
                // Handle dampening when within well radius
                handle.getLinearVelocity(currentLinearVelocity);
                currentLinearVelocity.mul(-DAMPEN_SCALE);
                handle.addLinearAndAngularVelocity(currentLinearVelocity, zeroVector);

                // Handle reduced pull impulse
                impulseVelocity.mul(DAMPEN_STRENGTH);
            } else {
                // Handle standard pull impulse when outside well radius
                impulseVelocity.mul(FIELD_STRENGTH / distanceSquared);
            }

            // Apply rotation transformed impulse
            targetSubLevel.logicalPose().orientation().transformInverse(impulseVelocity);
            handle.applyLinearImpulse(impulseVelocity);
        }
    }

    private void applyEntityGravity() {

    }

    /**
     * Handles a client entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 3:
                RandomSource random = this.level().random;
                for (int i = 0; i < 8; i++) this.level().addParticle(
                    ModParticles.DENSITE_WELL_PARTICLES.get(),
                    this.getX(), this.getY(), this.getZ(),
                    5.0d * random.nextDouble(), 5.0d * random.nextDouble(), 5.0d * random.nextDouble()
                );
                break;
            case 4:
                CreateCrystallized.LOGGER.info("How did we get here?");
                break;
            default:
                super.handleEntityEvent(id);
        }
    }
}
