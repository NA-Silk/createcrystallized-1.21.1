package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.custom.DensiteWellBlock;
import com.nasilk.createcrystallized.util.CCLang;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class DensiteWellEntity extends BlockEntity implements IHaveGoggleInformation {
    // Tick variables (saved)
    private int power = 0;
    private double fieldStrength = 0.0d;
    private double fieldRadius = 0.0d;
    private double fieldRadiusSquared = 0.0d;

    // Tick variables (unsaved)
    private int tickCounter = 0;
    private final List<SubLevel> targets = new ArrayList<>();

    // Tick constants
    private static final int TICK_RATE = 20;
    private static final double MIN_RADIUS = 5.0d;
    private static final double RADIUS_SCALE = 2.0d;
    private static final double FIELD_CONSTANT = 1.0d;

    // Physics constants
    private static final double IMPACT_RADIUS = 0.5d;
    private static final double IMPACT_RADIUS_SQUARED = IMPACT_RADIUS * IMPACT_RADIUS;
    private static final double RADIUS = 1.5d;
    private static final double RADIUS_SQUARED = RADIUS * RADIUS;
    private static final double RADIUS_CUBED = RADIUS * RADIUS * RADIUS;
    private static final double DAMPEN_FACTOR = 0.2d;

    // Cache
    private static class Cache {
        final BoundingBox3d searchBox = new BoundingBox3d();
        final Vector3d wellPosition = new Vector3d();
        final Vector3d targetPosition = new Vector3d();
        final Vector3d impulseVelocity = new Vector3d();
        final Vector3d currentLinearVelocity = new Vector3d();
        final Vector3d currentAngularVelocity = new Vector3d();
        final Vector3d zeroVector = new Vector3d(0, 0, 0); // Read-only reference
        double distance = 0.0d;
        double distanceSquared = 0.0d;
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
                    serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                    this.setChanged();
                    if (!targets.isEmpty()) targets.clear();
                    return;
                } else {
                    fieldStrength = FIELD_CONSTANT * power;
                    fieldRadius = RADIUS_SCALE * power + MIN_RADIUS;
                    fieldRadiusSquared = fieldRadius * fieldRadius;
                    serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                    this.setChanged();
                }
            }

            // Get global position
            Cache cache = CACHE.get();
            cache.wellPosition.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            ServerSubLevel wellSubLevel = null;
            if (Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
                wellSubLevel = subLevel;
                subLevel.logicalPose().transformPosition(cache.wellPosition);
            }

            // Run gravity effect
            if (tickCounter++ % TICK_RATE == 0) updateTargets(serverLevel, wellSubLevel, cache);
            applyGravity(cache);
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
            cache.distanceSquared = cache.impulseVelocity.lengthSquared();

            // Handle out of range entities
            if (cache.distanceSquared > fieldRadiusSquared) {
                targets.remove(i);
                continue;
            }

            // Handle impact when very close
            if (cache.distanceSquared < IMPACT_RADIUS_SQUARED) {
                // Get current linear and angular velocity
                handle.getLinearVelocity(cache.currentLinearVelocity);
                handle.getAngularVelocity(cache.currentAngularVelocity);

                // Apply opposite vectors to negate current motion
                cache.currentLinearVelocity.negate();
                cache.currentAngularVelocity.negate();
                handle.addLinearAndAngularVelocity(cache.currentLinearVelocity, cache.currentAngularVelocity);
                handle.teleport(cache.wellPosition, subLevel.logicalPose().orientation());
                continue;
            }

            // Handle dampening when within well radius: F = fieldStrength * distance / RADIUS^3
            cache.distance = Math.sqrt(cache.distanceSquared);
            if (cache.distanceSquared < RADIUS_SQUARED) {
                // Apply velocity dampening / drag
                handle.getLinearVelocity(cache.currentLinearVelocity);
                cache.currentLinearVelocity.mul(-DAMPEN_FACTOR);
                handle.addLinearAndAngularVelocity(cache.currentLinearVelocity, cache.zeroVector);

                // Apply reduced pull impulse
                cache.impulseVelocity.mul(fieldStrength / RADIUS_CUBED);
                handle.applyLinearImpulse(cache.impulseVelocity);
                continue;
            }

            // Handle standard pull impulse: F = fieldStrength / distance^2
            cache.impulseVelocity.mul(fieldStrength / (cache.distanceSquared * cache.distance));
            handle.applyLinearImpulse(cache.impulseVelocity);
        }
    }


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        CCLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentFieldStrength = CCLang
                .pixelNewton(fieldStrength)
                .style(ChatFormatting.AQUA)
                .component();
        CCLang.translate("goggles.field_strength", currentFieldStrength)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        final MutableComponent currentFieldRadius = CCLang
                .meter(fieldRadius)
                .style(ChatFormatting.AQUA)
                .component();
        CCLang.translate("goggles.field_radius", currentFieldRadius)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        return true;
    }

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
        this.fieldRadiusSquared = fieldRadius * fieldRadius;
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
        this.fieldRadiusSquared = fieldRadius * fieldRadius;
    }
}
