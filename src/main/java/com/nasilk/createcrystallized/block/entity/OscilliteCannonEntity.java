package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.custom.OscilliteCannonBlock;
import com.nasilk.createcrystallized.network.custom.OscilliteCannonBeamPayload;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.helper.CCLangHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class OscilliteCannonEntity extends BlockEntity implements IHaveGoggleInformation {
    // Variables (saved)
    private int charge = 0;
    private int cooldown = 0;
    private boolean armed = false;

    // Variables (unsaved)
    private int tickCounter = 0;
    private boolean initialized = false;
    private final Vector3d cannonPosition = new Vector3d();

    // Tick constants
    private static final int MAX_CHARGE = 100;
    private static final int MAX_COOLDOWN = 180;
    private static final double LINEAR_SCALE = 15.0d;
    private static final double ANGULAR_SCALE = 0.75d;
    private static final double THRESHOLD = 1.0d;
    private static final double FACE_OFFSET = 1.4d;

    // Firing constants
    private static final float DAMAGE_AMOUNT = 50.0f;
    private static final double KNOCKBACK_AMOUNT = 3.0d;
    private static final double MAX_RANGE = 80.0d; // Length effectiveness distance
    private static final double MAX_RADIUS = 2.12d; // Radial effectiveness distance
    private static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;
    private static final double RECOIL = 25.0;

    // Charging particle constants
    private static final double PARTICLE_RADIUS = 1.5;

    // Cache
    private static class Cache {
        // Tick
        Direction facing = Direction.NORTH;
        final Vector3d cannonPositionCurrent = new Vector3d();
        final Vector3d cannonDirection = new Vector3d();
        final Vector3d cannonFace =  new Vector3d();
        final Vector3d angularVelocity = new Vector3d();

        // Firing
        final BoundingBox3d searchBox = new BoundingBox3d();
        final List<SubLevel> targets = new ArrayList<>();
        final Vector3d relEntityPosition = new Vector3d();
        final Vector3d globalBeamPosition = new Vector3d();
        final Vector3d localBeamPosition = new Vector3d();

        // 3x3 Firing
        final Vector3d tempVector = new Vector3d();
        final Vector3d cannonI = new Vector3d();
        final Vector3d cannonJ = new Vector3d();
        //             cannonK = cannonDirection
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);


    public OscilliteCannonEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OSCILLITE_CANNON.get(), pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel && Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
            // Get the physics handle and tick data
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) return;
            Cache cache = CACHE.get();
            BlockState state = getBlockState();
            boolean powered = state.getValue(OscilliteCannonBlock.POWERED);
            tickCounter++;
            if (tickCounter > 400) tickCounter = 1;

            // Get global position
            cache.cannonPositionCurrent.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            subLevel.logicalPose().transformPosition(cache.cannonPositionCurrent);

            // Prevent server load jump from (0, 0, 0) to cache.cannonPositionCurrent
            if (!initialized) {
                cannonPosition.set(cache.cannonPositionCurrent);
                initialized = true;
            }

            // Get linear velocitySquared component
            double velocitySquared = cannonPosition.distanceSquared(cache.cannonPositionCurrent);
            cannonPosition.set(cache.cannonPositionCurrent); // Set cannonPosition = cannonPositionCurrent
            if (velocitySquared > 1e-3d) velocitySquared *= LINEAR_SCALE; // Set velocitySquared = LINEAR_SCALE*||-linearVelocity||^2
            else velocitySquared = 0.0d;

            // Get angular velocitySquared component
            handle.getAngularVelocity(cache.angularVelocity); // Get angularVelocity
            velocitySquared += ANGULAR_SCALE*cache.angularVelocity.lengthSquared(); // Set velocitySquared += ANGULAR_SCALE*||angularVelocity||^2

            // Get I, J, K vectors
            cache.facing = state.getValue(OscilliteCannonBlock.FACING);
            cache.cannonDirection.set(cache.facing.step()); // K
            if (cache.facing.getAxis() == Direction.Axis.Y) cache.tempVector.set(0, 0, -1); // North
            else cache.tempVector.set(0, 1, 0); // Up
            cache.cannonI.set(cache.cannonDirection).cross(cache.tempVector).normalize();
            cache.cannonJ.set(cache.cannonI).cross(cache.cannonDirection).normalize();

            // Transform local to global vectors and get face position
            subLevel.logicalPose().transformNormal(cache.cannonDirection);
            subLevel.logicalPose().transformNormal(cache.cannonI);
            subLevel.logicalPose().transformNormal(cache.cannonJ);
            cache.cannonFace.set(cannonPosition).fma(FACE_OFFSET, cache.cannonDirection);

            // Cooldown
            if (cooldown > 0) {
                if (!powered) {
                    cooldown--;
                    this.setChanged();
                    if (cooldown % 10 == 0) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                }
                if (tickCounter % 20 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        cannonPosition.x, cannonPosition.y, cannonPosition.z,
                        5, 0.5, 0.5, 0.5, 0.1
                    );
                }
                return;
            }

            // Charging
            if (!armed && charge < MAX_CHARGE && velocitySquared >= THRESHOLD) {
                charge++;
                if (tickCounter % 2 == 0) addChargingParticles(serverLevel, cache);
                if (charge >= MAX_CHARGE) {
                    armed = true;
                    serverLevel.playSound(
                        null, worldPosition,
                        SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.BLOCKS,
                        1.5F,0.7F
                    );
                }
                this.setChanged();
                if (charge % 10 == 0 || armed) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }

            // Firing
            if (armed && powered) {
                charge = 0;
                armed = false;
                cooldown = MAX_COOLDOWN;
                this.setChanged();
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                serverLevel.playSound(
                    null, worldPosition,
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS,
                    1.5F,1.0F
                );
                fireCannon(serverLevel, cache);
            }
        }
    }

    private void fireCannon(ServerLevel serverLevel, Cache cache) {
        // Set bounding box to query subLevels
        cache.targets.clear();
        cache.searchBox.setUnchecked(
            cannonPosition.x - MAX_RANGE, cannonPosition.y - MAX_RANGE, cannonPosition.z - MAX_RANGE,
            cannonPosition.x + MAX_RANGE, cannonPosition.y + MAX_RANGE, cannonPosition.z + MAX_RANGE
        );

        // Populate the target sublevel list
        SubLevel cannonSubLevel = Sable.HELPER.getContaining(serverLevel, worldPosition);
        ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
        if (container != null) {
            container.queryIntersecting(cache.searchBox).forEach(targetSubLevel -> {
                if (targetSubLevel != cannonSubLevel) cache.targets.add(targetSubLevel);
            });
        }

        // Move along beam paths and find nearest block
        double range = 0.0;
        for (int u = -1; u <= 1; u++) {
            for (int v = -1; v <= 1; v++) {
                // Set starting position
                cache.tempVector.set(cache.cannonFace).fma(u, cache.cannonI).fma(v, cache.cannonJ);

                // Destroy blocks and update total range
                double currentRange = destroyBlocksAndGetRange(serverLevel, cache);
                if (range < currentRange) range = currentRange;
            }
        }

        // Fire particles (sadly, nothing I can do about the allocations...)
        OscilliteCannonBeamPayload payload = new OscilliteCannonBeamPayload(
            new Vector3f().set(cache.cannonFace),
            new Vector3f().set(cache.cannonDirection),
            range
        );
        PacketDistributor.sendToPlayersTrackingChunk(
            serverLevel,
            new ChunkPos(worldPosition),
            payload
        );

        // Apply knockback
        if (cannonSubLevel instanceof ServerSubLevel cannonServerSubLevel) {
            RigidBodyHandle handle = RigidBodyHandle.of(cannonServerSubLevel);
            if (handle.isValid()) {
                cache.localBeamPosition.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
                cache.tempVector.set(cache.facing.step()).mul(-RECOIL);
                handle.applyImpulseAtPoint(cache.localBeamPosition, cache.tempVector);
            }
        }

        // Set bounding box to query entities
        cache.searchBox.setUnchecked(
            cannonPosition.x - range, cannonPosition.y - range, cannonPosition.z - range,
            cannonPosition.x + range, cannonPosition.y + range, cannonPosition.z + range
        );

        // Get entities within the bounding box
        List<Entity> entities = serverLevel.getEntities(null, cache.searchBox.toMojang()); // toMojang() Allocates a new Mojang AABB...
        if (entities.isEmpty()) return;

        // Iterate through entities
        for (Entity entity : entities) {
            damageEntity(entity, range, serverLevel, cache);
        }
    }

    private double destroyBlocksAndGetRange(ServerLevel serverLevel, Cache cache) {
        // Begin single beam loop
        double currentRange = MAX_RANGE;
        NEAR:
        for (double i = 0; i < MAX_RANGE; i += 0.5) {
            cache.globalBeamPosition.set(cache.tempVector).fma(i, cache.cannonDirection);

            // Check non-sublevel blocks
            BlockPos globalPos = BlockPos.containing(cache.globalBeamPosition.x, cache.globalBeamPosition.y, cache.globalBeamPosition.z);
            BlockState blockState = serverLevel.getBlockState(globalPos);
            if (!blockState.isAir() && !blockState.canBeReplaced() && tryPierceBlock(serverLevel, globalPos, blockState)) {
                currentRange = i;
                break;
            }

            // Check sublevel blocks
            for (SubLevel subLevel : cache.targets) {
                if (!(subLevel instanceof ServerSubLevel targetSubLevel) || subLevel.isRemoved()) continue;
                // Convert to sublevel coordinates
                cache.localBeamPosition.set(cache.globalBeamPosition);
                targetSubLevel.logicalPose().transformPositionInverse(cache.localBeamPosition);

                // Check states
                BlockPos localPos = BlockPos.containing(cache.localBeamPosition.x, cache.localBeamPosition.y, cache.localBeamPosition.z);
                BlockState subLevelBlockState = serverLevel.getBlockState(localPos);
                if (!subLevelBlockState.isAir() && !subLevelBlockState.canBeReplaced() && tryPierceBlock(serverLevel, localPos, subLevelBlockState)) {
                    currentRange = i;
                    break NEAR;
                }
            }
        }

        // Return range
        return currentRange;
    }

    @SuppressWarnings("deprecation")
    private boolean tryPierceBlock(Level level, BlockPos blockPos, BlockState blockState) {
        float resistance = blockState.getBlock().getExplosionResistance();
        if (resistance <= 5.0f) {
            level.destroyBlock(blockPos, true);
            return false;
        } else if (resistance <= 6.0f) {
            level.destroyBlock(blockPos, true);
            return true;
        }
        return true;
    }

    private void damageEntity(Entity entity, double range, ServerLevel serverLevel, Cache cache) {
        // Skip invalid entities
        if (entity instanceof AbstractContraptionEntity || AirCurrent.isPlayerCreativeFlying(entity) || DivingBootsItem.isWornBy(entity)) return;

        // Get entity position relative to the thruster
        AABB entityBoundingBox = entity.getBoundingBox(); // Avoids a Vec3 allocation from entity.getBoundingBox().getCenter()
        double entityX = (entityBoundingBox.minX + entityBoundingBox.maxX) * 0.5d;
        double entityY = (entityBoundingBox.minY + entityBoundingBox.maxY) * 0.5d;
        double entityZ = (entityBoundingBox.minZ + entityBoundingBox.maxZ) * 0.5d;
        cache.relEntityPosition.set(entityX, entityY, entityZ).sub(cannonPosition);

        // Linear distance
        double entityLinearDistance = cache.cannonDirection.dot(cache.relEntityPosition);
        if (entityLinearDistance < 0.0d || entityLinearDistance > range) return;

        // Radial distance
        double entityRadialDistanceSquared = cache.relEntityPosition.lengthSquared() - entityLinearDistance*entityLinearDistance;
        if (entityRadialDistanceSquared > MAX_RADIUS_SQUARED) return;

        // Apply damage and knockback
        entity.hurt(serverLevel.damageSources().magic(), DAMAGE_AMOUNT);
        if (cache.relEntityPosition.lengthSquared() > 1e-3d) cache.relEntityPosition.normalize().mul(KNOCKBACK_AMOUNT);
        else cache.relEntityPosition.set(cache.cannonDirection).normalize().mul(KNOCKBACK_AMOUNT);
        entity.push(cache.relEntityPosition.x, cache.relEntityPosition.y, cache.relEntityPosition.z);
        if (entity instanceof ServerPlayer serverPlayer) serverPlayer.hurtMarked = true;
    }

    private void addChargingParticles(ServerLevel level, Cache cache) {
        // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
        double xSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
        double ySpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
        double zSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;

        // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
        level.sendParticles(
            ModParticles.OSCILLITE_CANNON_CHARGING_PARTICLES.get(),
            cache.cannonFace.x, cache.cannonFace.y, cache.cannonFace.z,
            0, // Count = 0 (Crucial for passing custom payloads)
            xSpeed, ySpeed, zSpeed,
            1.0 // Use above speed values
        );
    }


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        CCLangHelper.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentCharge = CCLangHelper
            .number(charge).text("%")
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.current_charge", currentCharge)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent armedState = CCLangHelper
            .text(armed ? "Armed" : "Disarmed")
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.armed_state", armedState)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        if (cooldown > 0) {
            CCLangHelper.translate("goggles.cooling_down", CCLangHelper.number(cooldown / 20.0).text("s").component())
                .style(ChatFormatting.RED)
                .forGoggles(tooltip, 1);
        }

        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Charge", this.charge);
        tag.putInt("Cooldown", this.cooldown);
        tag.putBoolean("Armed", this.armed);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
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
