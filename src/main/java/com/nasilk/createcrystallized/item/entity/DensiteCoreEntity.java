package com.nasilk.createcrystallized.item.entity;

import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.ItemConfig;
import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DensiteCoreEntity extends ThrowableItemProjectile {
    // Tick variables
    private final List<SubLevel> sublevelTargets = new ArrayList<>();
    private final BoundingBox3d searchBox = new BoundingBox3d();
    private final Vector3d corePosition = new Vector3d();
    private final Vector3d impulseVelocity = new Vector3d();
    private final List<Entity> entityTargets = new ArrayList<>();

    // Tick constants
    private static final double PARTICLE_RATE = 0.01d;
    private static final Predicate<Entity> ENTITY_PREDICATE = entity ->
        !entity.isSpectator() &&
        !(entity instanceof AbstractContraptionEntity) &&
        !AirCurrent.isPlayerCreativeFlying(entity) &&
        !DivingBootsItem.isWornBy(entity);

    // Tick constants (config)
    private static double FIELD_RADIUS;
    private static double FIELD_RADIUS_SQUARED;
    private static double IMPACT_RADIUS_SQUARED;
    private static double SUBLEVEL_STRENGTH;
    private static double ENTITY_STRENGTH;

    // Config constants
    public static void updateConstants() {
        ItemConfig itemConfig = ModConfigs.server().itemConfig;
        FIELD_RADIUS = itemConfig.coreFieldRadius.get();
        FIELD_RADIUS_SQUARED = FIELD_RADIUS * FIELD_RADIUS;
        double IMPACT_RADIUS = itemConfig.coreImpactRadius.get();
        IMPACT_RADIUS_SQUARED = IMPACT_RADIUS * IMPACT_RADIUS;
        SUBLEVEL_STRENGTH = itemConfig.coreSublevelStrength.get();
        ENTITY_STRENGTH = itemConfig.coreEntityStrength.get();
    }


    // CONSTRUCTORS
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


    // TICK
    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.level().random.nextDouble() < PARTICLE_RATE)
            serverLevel.broadcastEntityEvent(this, (byte) 3); // Triggers handleEntityEvent() on client
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, (byte) 3); // Triggers handleEntityEvent() on client
            serverLevel.broadcastEntityEvent(this, (byte) 4);
            this.discard();
            corePosition.set(this.getX(), this.getY(), this.getZ()); // Get position as a Vector3d object
            this.updateSublevelTargets();
            if (!sublevelTargets.isEmpty()) this.applySublevelGravity();
            this.updateEntityTargets();
            if (!entityTargets.isEmpty()) this.applyEntityGravity();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 5.0f);
        }
    }


    // BEHAVIOR
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

    private void applySublevelGravity() {
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

            // Get sublevel position relative to the core
            impulseVelocity.set(corePosition).sub(targetSubLevel.logicalPose().position());
            double distanceSquared = impulseVelocity.lengthSquared();

            // Handle out of range sublevels
            if (distanceSquared > FIELD_RADIUS_SQUARED) {
                sublevelTargets.remove(i);
                continue;
            }

            // Handle impact when very close
            if (distanceSquared < IMPACT_RADIUS_SQUARED) {
                continue;
            }

            // Handle standard pull impulse
            impulseVelocity.mul(SUBLEVEL_STRENGTH / distanceSquared);

            // Apply rotation transformed impulse
            targetSubLevel.logicalPose().orientation().transformInverse(impulseVelocity);
            handle.applyLinearImpulse(impulseVelocity);
        }
    }

    private void updateEntityTargets() {
        // Reset the target list
        entityTargets.clear();

        // Get ServerLevel
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // Set bounding box to query entities
        searchBox.setUnchecked(
            this.position().x - FIELD_RADIUS, this.position().y - FIELD_RADIUS, this.position().z - FIELD_RADIUS,
            this.position().x + FIELD_RADIUS, this.position().y + FIELD_RADIUS, this.position().z + FIELD_RADIUS
        );

        // Get eligible entities within the bounding box
        entityTargets.addAll(serverLevel.getEntities((Entity) null, searchBox.toMojang(), ENTITY_PREDICATE)); // toMojang() allocates a new Mojang AABB...
    }

    private void applyEntityGravity() {
        // Iterate backwards for safe removals
        for (int i = entityTargets.size() - 1; i >= 0; i--) {
            Entity entity = entityTargets.get(i);

            // If the entity was destroyed or unloaded, remove it from the list
            if (entity.isRemoved()) {
                sublevelTargets.remove(i);
                continue;
            }

            // Get entity position relative to the core
            AABB entityBoundingBox = entity.getBoundingBox(); // Avoids a Vec3 allocation from entity.getBoundingBox().getCenter()
            double entityX = (entityBoundingBox.minX + entityBoundingBox.maxX) * 0.5d;
            double entityY = (entityBoundingBox.minY + entityBoundingBox.maxY) * 0.5d;
            double entityZ = (entityBoundingBox.minZ + entityBoundingBox.maxZ) * 0.5d;
            impulseVelocity.set(corePosition).sub(entityX, entityY, entityZ);
            double distanceSquared = impulseVelocity.lengthSquared();

            // Handle out of range entities
            if (distanceSquared > FIELD_RADIUS_SQUARED) {
                entityTargets.remove(i);
                continue;
            }

            // Handle impact when very close
            if (distanceSquared < IMPACT_RADIUS_SQUARED) {
                continue;
            }

            // Handle standard pull impulse
            impulseVelocity.mul(ENTITY_STRENGTH / distanceSquared);

            // Apply impulse
            entity.push(impulseVelocity.x, impulseVelocity.y, impulseVelocity.z);
            if (entity instanceof ServerPlayer serverPlayer) serverPlayer.hurtMarked = true;
        }
    }


    // PARTICLES
    /**
     * Handles a client entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 3:
                for (int i = 0; i < 16; i++) this.level().addParticle(
                    ModParticles.DENSITE_CORE_PARTICLES.get(),
                    this.getX(), this.getY(), this.getZ(),
                    this.nextSpeed(), this.nextSpeed(), this.nextSpeed()
                );
                break;
            case 4:
                DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.20f, 0.0f, 0.310f),1.0f);
                for (int i = 0; i < 8; i++) this.level().addParticle(
                    dust,
                    nextPos(this.getX()), nextPos(this.getY()), nextPos(this.getZ()),
                    0.4d, 0.4d, 0.4d
                );
                break;
            default:
                super.handleEntityEvent(id);
        }
    }

    private double nextPos(double a) {
        return a + getRandom().nextDouble() - 0.5d;
    }

    private double nextSpeed() {
        return Math.clamp(getRandom().nextGaussian() * FIELD_RADIUS / 2.5758293, -FIELD_RADIUS, FIELD_RADIUS);
    }
}
