package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.custom.OscilliteCannonBlock;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.CCLang;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.foundation.utility.BlockHelper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import java.util.List;

public class OscilliteCannonEntity extends BlockEntity implements IHaveGoggleInformation {
    // Variables (saved)
    private int charge = 0;
    private int cooldown = 0;
    private boolean armed = false;

    // Variables (unsaved)
    private int tickCounter = 0;
    private final Vector3d cannonPosition = new Vector3d();

    // Tick constants
    private static final int MAX_CHARGE = 100;
    private static final int MAX_COOLDOWN = 180;
    private static final double LINEAR_SCALE = 15.0d;
    private static final double ANGULAR_SCALE = 0.75d;
    private static final double THRESHOLD = 1.0d;
    private static final double FACE_OFFSET = 1.4d;

    // Firing constants
    private static final float DAMAGE_AMOUNT = 10.0f;
    private static final double KNOCKBACK_AMOUNT = 3.0d;
    private static final double MAX_RANGE = 20.0d; // Length effectiveness distance
    private static final double MAX_RADIUS = 1.0d; // Radial effectiveness distance
    private static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;

    // Charging particle constants
    private static final int NUM_PARTICLES = 2;
    private static final double PARTICLE_RADIUS = 1.5;

    // Cache
    private static class Cache {
        // Tick
        double velocitySquared = 0;
        boolean powered = false;
        final Vector3d cannonPositionCurrent = new Vector3d();
        final Vector3d cannonDirection = new Vector3d();
        final Vector3d cannonFace =  new Vector3d();
        final Vector3d angularVelocity = new Vector3d();

        // Firing
        final BoundingBox3d aabb = new BoundingBox3d();
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final Vector3d relEntityPosition = new Vector3d();
        final Vector3d beamPosition = new Vector3d();
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
            // Get the physics handle and tick data
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) return;
            Cache cache = CACHE.get();
            BlockState state = getBlockState();
            cache.powered = state.getValue(OscilliteCannonBlock.POWERED);

            // Get global position
            cache.cannonPositionCurrent.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            subLevel.logicalPose().transformPosition(cache.cannonPositionCurrent);

            // Get linear velocitySquared component
            cache.velocitySquared = cannonPosition.distanceSquared(cache.cannonPositionCurrent);
            if (cache.velocitySquared > 1e-3d) {
                cache.velocitySquared *= LINEAR_SCALE; // Set velocitySquared = LINEAR_SCALE*||-linearVelocity||^2
                cannonPosition.set(cache.cannonPositionCurrent); // Set cannonPosition = cannonPositionCurrent
            } else {
                cache.velocitySquared = 0.0d;
            }

            // Get angular velocitySquared component
            handle.getAngularVelocity(cache.angularVelocity); // Get angularVelocity
            cache.velocitySquared += ANGULAR_SCALE*cache.angularVelocity.lengthSquared(); // Set velocitySquared += ANGULAR_SCALE*||angularVelocity||^2

            // Get facing data
            cache.cannonDirection.set(state.getValue(OscilliteCannonBlock.FACING).step());
            subLevel.logicalPose().transformNormal(cache.cannonDirection);
            cache.cannonFace.set(cannonPosition).fma(FACE_OFFSET, cache.cannonDirection);

            // Cooldown TODO custom cooling down effect
            if (cooldown > 0) {
                if (!cache.powered) {
                    cooldown--;
                    this.setChanged();
                }
                if (tickCounter++ % 40 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        cannonPosition.x, cannonPosition.y, cannonPosition.z,
                        1, 0.5, 0.5, 0.5, 0.1
                    );
                    tickCounter = 0;
                }
                return;
            }

            // Charging
            if (!armed && charge < MAX_CHARGE && cache.velocitySquared >= THRESHOLD) {
                charge++;
                addChargingParticles(serverLevel, cache);
                if (charge >= MAX_CHARGE) {
                    armed = true;
                    serverLevel.playSound(
                        null,
                        worldPosition,
                        SoundEvents.WARDEN_SONIC_CHARGE,
                        SoundSource.BLOCKS,
                        1.5F,1.2F
                    );
                }
                this.setChanged();
                if (charge % 10 == 0 || armed) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }

            // Firing
            if (armed && cache.powered) {
                charge = 0;
                armed = false;
                cooldown = MAX_COOLDOWN;
                this.setChanged();
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                serverLevel.playSound(
                    null,
                    worldPosition,
                    SoundEvents.WARDEN_SONIC_BOOM,
                    SoundSource.BLOCKS,
                    1.5F,1.0F
                );
                fireCannon(serverLevel, cache);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void fireCannon(ServerLevel level, Cache cache) {
        double range = MAX_RANGE;

        // Move along beam path and find nearest block
        for (double i = 0; i < range; i += 0.5) {
            cache.beamPosition.set(cache.cannonFace).fma(i, cache.cannonDirection);
            cache.mutablePos.set(cache.beamPosition.x, cache.beamPosition.y, cache.beamPosition.z);
            BlockState blockState = level.getBlockState(cache.mutablePos);
            if (blockState.isAir() || blockState.canBeReplaced()) continue;

            float resistance = blockState.getBlock().getExplosionResistance();
            if (resistance <= 5.0f) {
                BlockHelper.destroyBlock(level, cache.mutablePos, 1.0f);
            } else if (resistance <= 6.0f) {
                BlockHelper.destroyBlock(level, cache.mutablePos, 1.0f);
                range = i;
                break;
            } else {
                range = i;
                break;
            }
        }

        // Set bounding box
        cache.aabb.setUnchecked(
            cannonPosition.x - range, cannonPosition.y - range, cannonPosition.z - range,
            cannonPosition.x + range, cannonPosition.y + range, cannonPosition.z + range
        );

        // Get entities within the bounding box
        List<Entity> entities = level.getEntities(null, cache.aabb.toMojang()); // toMojang() Allocates a new Mojang AABB...
        if (entities.isEmpty()) return;

        // Iterate through entities
        for (Entity entity : entities) {
            if (entity instanceof AbstractContraptionEntity || AirCurrent.isPlayerCreativeFlying(entity) || DivingBootsItem.isWornBy(entity)) continue;

            // Get entity position relative to the thruster
            AABB entityBoundingBox = entity.getBoundingBox(); // Avoids a Vec3 allocation from entity.getBoundingBox().getCenter()
            double entityX = (entityBoundingBox.minX + entityBoundingBox.maxX) * 0.5d;
            double entityY = (entityBoundingBox.minY + entityBoundingBox.maxY) * 0.5d;
            double entityZ = (entityBoundingBox.minZ + entityBoundingBox.maxZ) * 0.5d;
            cache.relEntityPosition.set(entityX, entityY, entityZ).sub(cannonPosition);

            // Linear distance
            double entityLinearDistance = cache.cannonDirection.dot(cache.relEntityPosition);
            if (entityLinearDistance < 0.0d || entityLinearDistance > range) continue;

            // Radial distance
            double entityRadialDistanceSquared = cache.relEntityPosition.lengthSquared() - entityLinearDistance*entityLinearDistance;
            if (entityRadialDistanceSquared > MAX_RADIUS_SQUARED) continue;

            // Apply damage and knockback
            entity.hurt(level.damageSources().magic(), DAMAGE_AMOUNT);
            cache.relEntityPosition.normalize().mul(KNOCKBACK_AMOUNT);
            entity.push(cache.relEntityPosition.x, cache.relEntityPosition.y, cache.relEntityPosition.z);
            if (entity instanceof ServerPlayer serverPlayer) serverPlayer.hurtMarked = true;
        }

        // Fire straight outward
        for (double i = 0; i <= range; i+=0.5) {
            cache.beamPosition.set(cache.cannonFace).fma(i, cache.cannonDirection);
            level.sendParticles(
                ParticleTypes.SONIC_BOOM,
                cache.beamPosition.x, cache.beamPosition.y, cache.beamPosition.z,
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    private void addChargingParticles(ServerLevel level, Cache cache) {
        // Compute each particle
        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
            double ySpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
            double zSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;

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


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        CCLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentCharge = CCLang
            .number(charge).text("%")
            .style(ChatFormatting.AQUA)
            .component();
        CCLang.translate("goggles.current_charge", currentCharge)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent armedState = CCLang
            .text(armed ? "Armed" : "Disarmed")
            .style(ChatFormatting.AQUA)
            .component();
        CCLang.translate("goggles.armed_state", armedState)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Charge", this.charge);
        tag.putBoolean("Armed", this.armed);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.charge = tag.getInt("Charge");
        this.armed = tag.getBoolean("Armed");
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
        tag.putInt("Charge", this.charge);
        tag.putInt("Cooldown", this.cooldown);
        tag.putBoolean("Armed", this.armed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
        this.armed = tag.getBoolean("Armed");
    }
}
