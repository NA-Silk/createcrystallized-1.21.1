package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.custom.DensiteWellBlock;
import com.nasilk.createcrystallized.config.Configs;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.helper.CCLangHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.List;

// TODO Tune constants with config
public class DensiteWellEntity extends BlockEntity implements IHaveGoggleInformation {
    // Tick variables (saved)
    private int power = 0;
    private double fieldStrength = 0.0d;
    private double fieldRadius = 0.0d;
    private double fieldRadiusSquared = 0.0d;

    // Tick variables (unsaved)
    private final List<SubLevel> targets = new ArrayList<>();

    // Tick constants
    private static final int TICK_RATE = 20;
    private static final double AMBIENT_RATE = 8e-5d;
    private static final double PARTICLE_RATE = 0.025d;
    private double MIN_RADIUS = Double.NaN; // 0.0d;
    private double RADIUS_SCALE = Double.NaN; // 2.0d;
    private double FIELD_SCALE = Double.NaN; // 0.5d;
    private final double[] FIELD_STRENGTH_CURVE = new double[16];
    private final double[] FIELD_RADIUS_CURVE = new double[16];
    private final double[] FIELD_RADIUS_SQUARED_CURVE = new double[16];

    // Physics constants
    private double IMPACT_RADIUS = Double.NaN; // 0.5d;
    private double DAMPEN_RADIUS = Double.NaN; // 1.5d;
    private double DAMPEN_SCALE = Double.NaN; // 0.2d;
    private double IMPACT_RADIUS_SQUARED = Double.NaN;
    private double DAMPEN_RADIUS_SQUARED = Double.NaN;

    // Config constants
    private boolean updateConstants() { // Don't worry about it
        double tempVar; boolean changed = false;
        tempVar = Configs.server().blockConfig.wellMinRadius.get();    if (MIN_RADIUS    != tempVar) { MIN_RADIUS    = tempVar; changed = true; }
        tempVar = Configs.server().blockConfig.wellRadiusScale.get();  if (RADIUS_SCALE  != tempVar) { RADIUS_SCALE  = tempVar; changed = true; }
        tempVar = Configs.server().blockConfig.wellFieldScale.get();   if (FIELD_SCALE   != tempVar) { FIELD_SCALE   = tempVar; changed = true; }
        if (changed) for (int i = 0; i < 16; i++) {
            FIELD_STRENGTH_CURVE[i] = FIELD_SCALE * i;
            FIELD_RADIUS_CURVE[i] = RADIUS_SCALE * i + MIN_RADIUS;
            FIELD_RADIUS_SQUARED_CURVE[i] = FIELD_RADIUS_CURVE[i] * FIELD_RADIUS_CURVE[i];
        }
        tempVar = Configs.server().blockConfig.wellImpactRadius.get(); if (IMPACT_RADIUS != tempVar) { IMPACT_RADIUS = tempVar; changed = true; IMPACT_RADIUS_SQUARED = IMPACT_RADIUS * IMPACT_RADIUS; }
        tempVar = Configs.server().blockConfig.wellDampenRadius.get(); if (DAMPEN_RADIUS != tempVar) { DAMPEN_RADIUS = tempVar; changed = true; DAMPEN_RADIUS_SQUARED = DAMPEN_RADIUS * DAMPEN_RADIUS; }
        tempVar = Configs.server().blockConfig.wellDampenScale.get();  if (DAMPEN_SCALE  != tempVar) { DAMPEN_SCALE  = tempVar; changed = true; }
        if (changed && power > 0) {
            fieldStrength = FIELD_STRENGTH_CURVE[power];
            fieldRadius = FIELD_RADIUS_CURVE[power];
            fieldRadiusSquared = FIELD_RADIUS_SQUARED_CURVE[power];
        }
        return changed;
    }
    { updateConstants(); }

    // Cache
    private static class Cache {
        final BoundingBox3d searchBox = new BoundingBox3d();
        final Vector3d wellPosition = new Vector3d();
        final Vector3d targetPosition = new Vector3d();
        final Vector3d impulseVelocity = new Vector3d();
        final Vector3d currentLinearVelocity = new Vector3d();
        final Vector3d currentAngularVelocity = new Vector3d();
        final Vector3d zeroVector = new Vector3d(0.0d, 0.0d, 0.0d); // Read-only reference
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);


    public DensiteWellEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DENSITE_WELL.get(), pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel) {
            // Get block power
            BlockState state = getBlockState();
            int newPower = state.getValue(DensiteWellBlock.POWER);
            if (power != newPower) {
                power = newPower;
                if (power == 0) {
                    fieldStrength = 0.0d;
                    fieldRadius = 0.0d;
                    fieldRadiusSquared = 0.0d;
                    if (!targets.isEmpty()) targets.clear();
                } else {
                    fieldStrength = FIELD_STRENGTH_CURVE[power];
                    fieldRadius = FIELD_RADIUS_CURVE[power];
                    fieldRadiusSquared = FIELD_RADIUS_SQUARED_CURVE[power];
                }
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                this.setChanged();
            }

            // Have a little ambiance
            if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) serverLevel.playSound(
                null, worldPosition,
                SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS, SoundSource.BLOCKS,
                1.0f,0.8f
            );

            // Exit if unpowered
            if (power == 0) return;

            // Get global position
            Cache cache = CACHE.get();
            cache.wellPosition.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d);
            ServerSubLevel wellSubLevel = null;
            if (Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
                wellSubLevel = subLevel;
                subLevel.logicalPose().transformPosition(cache.wellPosition);
            }

            // Run effects
            if ((serverLevel.getGameTime() + worldPosition.hashCode()) % TICK_RATE == 0) {
                if (updateConstants()) { // Handle config changes
                    serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                    this.setChanged();
                }
                updateTargets(serverLevel, wellSubLevel, cache); // Run gravity effect
            }
            if (!targets.isEmpty()) applyGravity(cache);
            if (serverLevel.getRandom().nextDouble() < PARTICLE_RATE) addEffectParticles(serverLevel, cache);
        }
    }

    private void updateTargets(ServerLevel level, ServerSubLevel wellSubLevel, Cache cache) {
        // Reset the target list
        targets.clear();

        // Get Sable's SubLevel container for this dimension
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return;

        // Set the bounding box
        cache.searchBox.setUnchecked(
            cache.wellPosition.x - fieldRadius, cache.wellPosition.y - fieldRadius, cache.wellPosition.z - fieldRadius,
            cache.wellPosition.x + fieldRadius, cache.wellPosition.y + fieldRadius, cache.wellPosition.z + fieldRadius
        );

        // Populate the target list
        container.queryIntersecting(cache.searchBox).forEach(targetSubLevel -> {
            if (targetSubLevel != wellSubLevel) targets.add(targetSubLevel);
        });
    }

    private void applyGravity(Cache cache) {
        // Iterate backwards for safe removals
        for (int i = targets.size() - 1; i >= 0; i--) {
            SubLevel targetSubLevel = targets.get(i);

            // If the sublevel was destroyed or unloaded, remove it from the list
            if (!(targetSubLevel instanceof ServerSubLevel subLevel) || targetSubLevel.isRemoved()) {
                targets.remove(i);
                continue;
            }

            // Get the physics handle
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) continue;

            // Get sublevel position, impulse velocity, and current distance^2
            cache.targetPosition.set(targetSubLevel.logicalPose().position());
            cache.impulseVelocity.set(cache.wellPosition).sub(cache.targetPosition);
            double distanceSquared = cache.impulseVelocity.lengthSquared();

            // Handle out of range sublevels
            if (distanceSquared > fieldRadiusSquared) {
                targets.remove(i);
                continue;
            }

            // Handle impact when very close
            if (distanceSquared < IMPACT_RADIUS_SQUARED) {
                // Get current linear and angular velocity
                handle.getLinearVelocity(cache.currentLinearVelocity);
                handle.getAngularVelocity(cache.currentAngularVelocity);

                // Apply opposite vectors to negate current motion
                cache.currentLinearVelocity.negate();
                cache.currentAngularVelocity.negate();
                handle.addLinearAndAngularVelocity(cache.currentLinearVelocity, cache.currentAngularVelocity);
                continue;
            }

            // double distance = Math.sqrt(distanceSquared);
            if (distanceSquared < DAMPEN_RADIUS_SQUARED) {
                // Handle dampening when within well radius
                handle.getLinearVelocity(cache.currentLinearVelocity);
                cache.currentLinearVelocity.mul(-DAMPEN_SCALE);
                handle.addLinearAndAngularVelocity(cache.currentLinearVelocity, cache.zeroVector);

                // Handle reduced pull impulse: F = fieldStrength * distance / RADIUS^3
                // cache.impulseVelocity.mul(fieldStrength / DAMPEN_RADIUS_CUBED);
                cache.impulseVelocity.mul(fieldStrength / DAMPEN_RADIUS_SQUARED);
            } else {
                // Handle standard pull impulse: F = fieldStrength / distance^2
                // cache.impulseVelocity.mul(fieldStrength / (distanceSquared * distance));
                cache.impulseVelocity.mul(fieldStrength / distanceSquared);
            }

            // Apply rotation transformed impulse
            targetSubLevel.logicalPose().orientation().transformInverse(cache.impulseVelocity);
            handle.applyLinearImpulse(cache.impulseVelocity);
        }
    }


    // PARTICLES
    private void addEffectParticles(ServerLevel serverLevel, Cache cache) {
        // Compute each particle
        for (int i = 0; i < power; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = Math.clamp(serverLevel.random.nextGaussian() * fieldRadius / 2.5758293, -fieldRadius, fieldRadius);
            double ySpeed = Math.clamp(serverLevel.random.nextGaussian() * fieldRadius / 2.5758293, -fieldRadius, fieldRadius);
            double zSpeed = Math.clamp(serverLevel.random.nextGaussian() * fieldRadius / 2.5758293, -fieldRadius, fieldRadius);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticles.DENSITE_WELL_PARTICLES.get(),
                cache.wellPosition.x, cache.wellPosition.y, cache.wellPosition.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                xSpeed, ySpeed, zSpeed,
                1.0d // Use above speed values
            );
        }
    }


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        CCLangHelper.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentFieldStrength = CCLangHelper
            .pixelNewton(fieldStrength)
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.field_strength", currentFieldStrength)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent currentFieldRadius = CCLangHelper
            .meter(fieldRadius)
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.field_radius", currentFieldRadius)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        return true;
    }


    // SYNCING
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Power", this.power);
        tag.putDouble("FieldStrength", this.fieldStrength);
        tag.putDouble("FieldRadius", this.fieldRadius);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.power = tag.getInt("Power");
        this.fieldStrength = tag.getDouble("FieldStrength");
        this.fieldRadius = tag.getDouble("FieldRadius");
        this.fieldRadiusSquared = this.fieldRadius * this.fieldRadius;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Wrap the tag into the standard vanilla packet
        return ClientboundBlockEntityDataPacket.create(this);
    }


    // DATA PERSISTENCE
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Power", this.power);
        tag.putDouble("FieldStrength", this.fieldStrength);
        tag.putDouble("FieldRadius", this.fieldRadius);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getInt("Power");
        this.fieldStrength = tag.getDouble("FieldStrength");
        this.fieldRadius = tag.getDouble("FieldRadius");
        this.fieldRadiusSquared = this.fieldRadius * this.fieldRadius;
    }
}
