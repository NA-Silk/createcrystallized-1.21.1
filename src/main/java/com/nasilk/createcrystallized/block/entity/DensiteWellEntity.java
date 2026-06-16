package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.custom.DensiteWellBlock;
import com.nasilk.createcrystallized.util.FFLang;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

// TODO Fix this entire thing, it's really inefficient and doesn't work on sublevels...
public class DensiteWellEntity extends BlockEntity implements IHaveGoggleInformation {
    private int tickCounter = 0;
    private double fieldStrength = 0.0d;
    private double fieldRadius = 0.0d;
    private final List<SubLevel> targets = new ArrayList<>();
    private static final int TICK_RATE = 20;
    private static final double MIN_RADIUS = 5.0d;
    private static final double RADIUS_SCALE = 2.0d;
    private static final double FIELD_CONSTANT = 1.0d;

    public DensiteWellEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DENSITE_WELL.get(), pos, state);
    }

    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel) {
            // Get block power
            BlockState state = getBlockState();
            int power = state.getValue(DensiteWellBlock.POWER);
            if (power == 0) {
                fieldStrength = 0.0d;
                if (!targets.isEmpty()) targets.clear();
                return;
            }

            // Run gravity effect
            fieldRadius = MIN_RADIUS + power * RADIUS_SCALE;
            fieldStrength = FIELD_CONSTANT * power;
            if (tickCounter++ % TICK_RATE == 0) updateTargets(serverLevel, worldPosition, fieldRadius);
            applyGravity(worldPosition, fieldRadius, fieldStrength);

            // Update tooltips
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    private void updateTargets(ServerLevel level, BlockPos pos, double radius) {
        // Reset target list
        targets.clear();

        // Get Sable's SubLevel container for this dimension
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return;
        AABB searchArea = new AABB(pos).inflate(radius);

        // Convert Minecraft AABB to Sable's BoundingBox3dc
        BoundingBox3d sableBounds = new BoundingBox3d(
            searchArea.minX, searchArea.minY, searchArea.minZ,
            searchArea.maxX, searchArea.maxY, searchArea.maxZ
        );

        // Populate target list
        container.queryIntersecting(sableBounds).forEach(targets::add);
    }

    private void applyGravity(BlockPos pos, double radius, double fieldStrength) {
        // Get current block center position
        Vec3 magnetCenter = pos.getCenter();

        // Iterate backwards for safe removals
        for (int i = targets.size() - 1; i >= 0; i--) {
            SubLevel targetSubLevel = targets.get(i);
            if (!(targetSubLevel instanceof ServerSubLevel subLevel)) continue;

            // If the sublevel was destroyed or unloaded, remove it from cache
            if (targetSubLevel.isRemoved()) {
                targets.remove(i);
                continue;
            }

            // Get the physics handle
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) continue;

            // Get sublevel position and current distance to block center
            Vector3d subLevelPos3d = targetSubLevel.logicalPose().position();
            double distance = subLevelPos3d.distance(magnetCenter.x, magnetCenter.y, magnetCenter.z);

            // Handle out of range entities
            if (distance > radius) {
                targets.remove(i);
                continue;
            }

            // Snap very close motion
            if (distance < 0.25) {
                handle.addLinearAndAngularVelocity(
                    new Vector3d(0,0,0).sub(handle.getLinearVelocity(new Vector3d())),
                    new Vector3d(0,0,0).sub(handle.getAngularVelocity(new Vector3d()))
                );
                subLevel.logicalPose().position().set(magnetCenter.x, magnetCenter.y, magnetCenter.z);
                continue;
            }

            // Dampen close motion
            if (distance < 1.5) {
                // "Dampen": Reduce current velocity
                Vector3d currentVel = handle.getLinearVelocity(new Vector3d());
                handle.addLinearAndAngularVelocity(currentVel.mul(-0.5), new Vector3d(0,0,0));
                Vec3 direction = magnetCenter.subtract(new Vec3(subLevelPos3d.x(), subLevelPos3d.y(), subLevelPos3d.z())).normalize();
                handle.applyLinearImpulse(new Vector3d(direction.x * 0.01, direction.y * 0.01, direction.z * 0.01));
                continue;
            }

            // Standard pull Logic
            Vec3 direction = magnetCenter.subtract(new Vec3(subLevelPos3d.x(), subLevelPos3d.y(), subLevelPos3d.z())).normalize();
            double force = fieldStrength / (distance * distance);
            handle.applyLinearImpulse(new Vector3d(direction.x * force, direction.y * force, direction.z * force));
        }
    }

    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        FFLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentFieldStrength = FFLang
                .pixelNewton(fieldStrength)
                .style(ChatFormatting.AQUA)
                .component();
        FFLang.translate("goggles.field_strength", currentFieldStrength)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        final MutableComponent currentFieldRadius = FFLang
                .number(fieldRadius)
                .style(ChatFormatting.AQUA)
                .component();
        FFLang.translate("goggles.field_radius", currentFieldRadius)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putDouble("FieldStrength", this.fieldStrength);
        tag.putDouble("FieldRadius", this.fieldRadius);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.fieldStrength = tag.getDouble("FieldStrength");
        this.fieldRadius = tag.getDouble("FieldRadius");
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
        tag.putDouble("FieldStrength", this.fieldStrength);
        tag.putDouble("FieldRadius", this.fieldRadius);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fieldStrength = tag.getDouble("FieldStrength");
        this.fieldRadius = tag.getDouble("FieldRadius");
    }
}
