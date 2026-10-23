package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.behavior.OscilliteCannonBehavior;
import com.nasilk.createcrystallized.block.custom.OscilliteCannonBlock;
import com.nasilk.createcrystallized.common.ModDamageTypes;
import com.nasilk.createcrystallized.network.custom.OscilliteCannonBeamPayload;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import com.nasilk.createcrystallized.util.helper.CCLangHelper;
import com.nasilk.createcrystallized.util.type.TickState;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import static com.nasilk.createcrystallized.block.behavior.OscilliteCannonBehavior.*;

public class OscilliteCannonEntity extends BlockEntity implements IHaveGoggleInformation {
    // Tick state
    private TickState tickState = TickState.IDLE;
    private final OscilliteCannonBehavior behavior = new OscilliteCannonBehavior(this);

    // Variables (saved)
    private int cooldown = 0;

    // Variables (unsaved)
    private DamageSource cannonDamageSource = null;

    // Cache
    public static class Cache {
        // Tick public
        public Direction facing = Direction.NORTH;
        public final Vector3d cannonPosition = new Vector3d();
        public final Vector3d cannonFace = new Vector3d();
        public final Vector3d cannonVelocity = new Vector3d();

        // Tick
        final Vector3d cannonDirection = new Vector3d();

        // Firing
        final BoundingBox3d searchBox = new BoundingBox3d();
        final List<ServerSubLevel> targets = new ArrayList<>();
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final Vector3d relEntityPosition = new Vector3d();
        final Vector3d globalBeamPosition = new Vector3d();
        final Vector3d localBeamPosition = new Vector3d();

        // 3x3 Firing
        final Vector3d tempVector = new Vector3d();
        final Vector3d beamOrigin = new Vector3d();
        final Vector3d cannonI = new Vector3d();
        final Vector3d cannonJ = new Vector3d();
        //             cannonK = cannonDirection

        // Particles
        public final Vector3d spawnPosition = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);


    // CONSTRUCTOR
    public OscilliteCannonEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel) {
            // Get initial variables
            Cache cache = CACHE.get();
            BlockState blockState = getBlockState();

            // Handle sublevels
            ServerSubLevel cannonSubLevel = null;
            if (Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel serverSubLevel) cannonSubLevel = serverSubLevel;

            // Get position, I, J, cannonDirection/K vectors
            cache.cannonPosition.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d); // Local position
            cache.facing = blockState.getValue(OscilliteCannonBlock.FACING);
            cache.cannonDirection.set(cache.facing.step()); // K
            if (cache.facing.getAxis() == Direction.Axis.Y) cache.tempVector.set(0.0d, 0.0d, -1.0d); // North
            else cache.tempVector.set(0.0d, 1.0d, 0.0d); // Up
            cache.cannonI.set(cache.cannonDirection).cross(cache.tempVector).normalize();
            cache.cannonJ.set(cache.cannonI).cross(cache.cannonDirection).normalize();
            cache.cannonFace.set(cache.cannonPosition).fma(FACE_OFFSET, cache.cannonDirection);

            // Transform local to global vectors and get face position
            if (cannonSubLevel != null) {
                Sable.HELPER.getVelocity(serverLevel, cannonSubLevel, cache.cannonPosition, cache.cannonVelocity);
                cache.cannonVelocity.mul(1.0d / 20.0d); // bps -> bpt
                cannonSubLevel.logicalPose().transformPosition(cache.cannonPosition);
                cannonSubLevel.logicalPose().transformNormal(cache.cannonDirection);
                cannonSubLevel.logicalPose().transformNormal(cache.cannonI);
                cannonSubLevel.logicalPose().transformNormal(cache.cannonJ);
                cannonSubLevel.logicalPose().transformPosition(cache.cannonFace);
            } else {
                cache.cannonVelocity.zero();
            }

            // Run tick
            behavior.tick(serverLevel, cannonSubLevel, cache);
        }
    }


    /// FIRING
    public void fireCannon(ServerLevel serverLevel, ServerSubLevel cannonSubLevel, Cache cache) {
        // Set bounding box to query subLevels
        cache.targets.clear();
        setSearchBox(MAX_RANGE, cache);

        // Populate the target sublevel list
        ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
        if (container != null) {
            container.queryIntersecting(cache.searchBox).forEach(subLevel -> {
                if (subLevel instanceof ServerSubLevel targetSubLevel) cache.targets.add(targetSubLevel);
            });
        }

        // Move along beam paths and find nearest block
        double range = 0.0d;
        for (int u = -1; u <= 1; u++) {
            for (int v = -1; v <= 1; v++) {
                // Set starting position
                cache.beamOrigin.set(cache.cannonFace).fma(u, cache.cannonI).fma(v, cache.cannonJ);

                // Destroy blocks and update total range
                double newRange = destroyBlocksAndGetRange(serverLevel, cannonSubLevel, cache);
                if (newRange > range) range = newRange;
            }
        }
        if (range <= 0.0d) return;

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

        // Apply recoil
        if (cannonSubLevel != null) {
            RigidBodyHandle handle = RigidBodyHandle.of(cannonSubLevel);
            if (handle.isValid()) {
                cache.localBeamPosition.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d);
                cache.tempVector.set(cache.cannonDirection).mul(-RECOIL);
                cannonSubLevel.logicalPose().orientation().transformInverse(cache.tempVector); // Use local orientation
                handle.applyImpulseAtPoint(cache.localBeamPosition, cache.tempVector);
            }
        }

        // Set bounding box to query entities
        setSearchBox(range, cache);

        // Get entities within the bounding box
        List<Entity> entities = serverLevel.getEntities((Entity) null, cache.searchBox.toMojang(), DAMAGE_PREDICATE); // toMojang() allocates a new Mojang AABB...
        if (entities.isEmpty()) return;

        // Iterate through entities
        for (Entity entity : entities) {
            pushAndDamageEntity(entity, range, serverLevel, cache);
        }
    }

    private double destroyBlocksAndGetRange(ServerLevel serverLevel, ServerSubLevel cannonSubLevel, Cache cache) {
        // Get initial voxel (block) coordinates
        int currentX = (int) Math.floor(cache.beamOrigin.x);
        int currentY = (int) Math.floor(cache.beamOrigin.y);
        int currentZ = (int) Math.floor(cache.beamOrigin.z);

        // Get direction step values (increment/decrement amount)
        int stepX = (cache.cannonDirection.x > 0) ? 1  :
                    (cache.cannonDirection.x < 0) ? -1 :
                    0;
        int stepY = (cache.cannonDirection.y > 0) ? 1  :
                    (cache.cannonDirection.y < 0) ? -1 :
                    0;
        int stepZ = (cache.cannonDirection.z > 0) ? 1  :
                    (cache.cannonDirection.z < 0) ? -1 :
                    0;

        // Get parametric distance per axis
        // - These are scale values to translate 1 x/y/z steps into equivalent beam distances,
        //   that way the 3 possible beam lengths (tMaxX/Y/Z) can be compared and
        //   the shortest can be chosen by comparison.
        // - 2D comparison for tDeltaX:
        //      tDeltaX = sqrt(1 + dy^2/dx^2) = sqrt((dx^2 + dy^2)/dx^2) = sqrt(1/dx^2 = |1/dx|
        // - Double.POSITIVE_INFINITY prevents null errors for unreachable directions
        double tDeltaX = (stepX != 0) ? Math.abs(1.0d / cache.cannonDirection.x) :
                         Double.POSITIVE_INFINITY;
        double tDeltaY = (stepY != 0) ? Math.abs(1.0d / cache.cannonDirection.y) :
                         Double.POSITIVE_INFINITY;
        double tDeltaZ = (stepZ != 0) ? Math.abs(1.0d / cache.cannonDirection.z) :
                         Double.POSITIVE_INFINITY;

        // Get projected ray lengths after following each possible step for comparison
        // - Add step and subtract origin for 1D distance in positive directions
        // - Subtract current from origin in negative directions
        // - Double.POSITIVE_INFINITY prevents null errors for unreachable directions
        double tMaxX = (stepX > 0) ? (currentX + 1.0d - cache.beamOrigin.x) * tDeltaX :
                       (stepX < 0) ? (cache.beamOrigin.x - currentX) * tDeltaX        :
                       Double.POSITIVE_INFINITY;
        double tMaxY = (stepY > 0) ? (currentY + 1.0d - cache.beamOrigin.y) * tDeltaY :
                       (stepY < 0) ? (cache.beamOrigin.y - currentY) * tDeltaY        :
                       Double.POSITIVE_INFINITY;
        double tMaxZ = (stepZ > 0) ? (currentZ + 1.0d - cache.beamOrigin.z) * tDeltaZ :
                       (stepZ < 0) ? (cache.beamOrigin.z - currentZ) * tDeltaZ        :
                       Double.POSITIVE_INFINITY;

        // Initialize current range
        double t = 0.0d;

        // DDA traversal
        BlockState blockState;
        while (t < MAX_RANGE) {
            /// Check non-sublevel blocks
            cache.mutablePos.set(currentX, currentY, currentZ);
            blockState = serverLevel.getBlockState(cache.mutablePos);
            if (!(blockState.isAir() || blockState.canBeReplaced() || tryPierceBlock(serverLevel, cache.mutablePos, blockState))) return t;

            /// Check sublevel blocks
            if (!cache.targets.isEmpty()) {
                cache.globalBeamPosition.set(cache.beamOrigin).fma(t + 1.0e-5d, cache.cannonDirection); // Tiny offset to make sure we're inside the block
                // Iterate backwards for safe removals
                for (int i = cache.targets.size() - 1; i >= 0; i--) {
                    ServerSubLevel targetSubLevel = cache.targets.get(i);

                    // If the sublevel was destroyed or unloaded, remove it from the list
                    if (targetSubLevel.isRemoved()) {
                        cache.targets.remove(i);
                        continue;
                    }

                    // Convert to sublevel coordinates
                    cache.localBeamPosition.set(cache.globalBeamPosition);
                    targetSubLevel.logicalPose().transformPositionInverse(cache.localBeamPosition);

                    // Check and try to destroy block
                    cache.mutablePos.set(cache.localBeamPosition.x, cache.localBeamPosition.y, cache.localBeamPosition.z);
                    blockState = serverLevel.getBlockState(cache.mutablePos);
                    if (blockState.isAir() || blockState.canBeReplaced() || tryPierceBlock(serverLevel, cache.mutablePos, blockState)) continue;

                    // Apply knockback to surviving block
                    RigidBodyHandle targetHandle = RigidBodyHandle.of(targetSubLevel);
                    if (targetHandle.isValid()) {
                        // Get local direction
                        cache.tempVector.set(cache.cannonDirection);
                        targetSubLevel.logicalPose().transformNormalInverse(cache.tempVector);

                        // Scale and apply impulse
                        if(targetSubLevel == cannonSubLevel) cache.tempVector.mul(RECOIL_9); // Undo recoil if self-hit
                        else cache.tempVector.mul(SUBLEVEL_KNOCKBACK);
                        targetHandle.applyImpulseAtPoint(cache.localBeamPosition, cache.tempVector);
                    }
                    return t;
                }
            }

            /// Update DDA variables
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) { t = tMaxX; tMaxX += tDeltaX; currentX += stepX; }
                else               { t = tMaxZ; tMaxZ += tDeltaZ; currentZ += stepZ; }
            } else {
                if (tMaxY < tMaxZ) { t = tMaxY; tMaxY += tDeltaY; currentY += stepY; }
                else               { t = tMaxZ; tMaxZ += tDeltaZ; currentZ += stepZ; }
            }
        }

        // Default return
        return MAX_RANGE;
    }

    @SuppressWarnings("deprecation")
    private boolean tryPierceBlock(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        float resistance = blockState.getBlock().getExplosionResistance();
        if (resistance <= 5.0f) {
            serverLevel.destroyBlock(blockPos, true);
            return true;
        } else if (resistance <= 6.0f) {
            serverLevel.destroyBlock(blockPos, true);
            return false;
        }
        return false;
    }

    private void pushAndDamageEntity(Entity entity, double range, ServerLevel serverLevel, Cache cache) {
        // Get entity position relative to the thruster
        AABB entityBoundingBox = entity.getBoundingBox(); // Avoids a Vec3 allocation from entity.getBoundingBox().getCenter()
        double entityX = (entityBoundingBox.minX + entityBoundingBox.maxX) * 0.5d;
        double entityY = (entityBoundingBox.minY + entityBoundingBox.maxY) * 0.5d;
        double entityZ = (entityBoundingBox.minZ + entityBoundingBox.maxZ) * 0.5d;
        cache.relEntityPosition.set(entityX, entityY, entityZ).sub(cache.cannonFace);

        // Longitudinal distance
        double longitudinalDistance = cache.cannonDirection.dot(cache.relEntityPosition);
        if (longitudinalDistance < 0.0d || longitudinalDistance > range) return;

        // Radial distance
        double distanceSquared = cache.relEntityPosition.lengthSquared();
        double radialDistanceSquared = distanceSquared - longitudinalDistance * longitudinalDistance;
        if (radialDistanceSquared > MAX_RADIUS_SQUARED) return;

        // Apply Knockback
        if (distanceSquared > 1e-3d) cache.relEntityPosition.normalize().mul(ENTITY_KNOCKBACK);
        else cache.relEntityPosition.set(cache.cannonDirection).normalize().mul(ENTITY_KNOCKBACK);
        entity.push(cache.relEntityPosition.x, cache.relEntityPosition.y, cache.relEntityPosition.z);
        if (entity instanceof ServerPlayer serverPlayer) serverPlayer.hurtMarked = true;
        entity.fallDistance = 0.0f;

        // Apply damage, Warden takes additional damage
        if (cannonDamageSource == null) cannonDamageSource = ModDamageTypes.getSource(serverLevel, ModDamageTypes.OSCILLITE_CANNON);
        boolean wasAlive = entity.isAlive();
        boolean isWarden = entity instanceof Warden;
        entity.hurt(cannonDamageSource, isWarden ? DAMAGE * 5.0f : DAMAGE); // Racism line

        // Handle "Hair of the Dog" Advancement
        if (wasAlive && !entity.isAlive() && isWarden) AdvancementHelper.hitEmWithTheOlSwitcheroo(
            serverLevel,
            entity.blockPosition(),
            "hair_of_the_dog"
        );
    }

    private void setSearchBox(double range, Cache cache) {
        // Get direction components
        double dx = cache.cannonDirection.x;
        double dy = cache.cannonDirection.y;
        double dz = cache.cannonDirection.z;

        // Find beam center
        double xCenter = cache.cannonFace.x + range * dx * 0.5d;
        double yCenter = cache.cannonFace.y + range * dy * 0.5d;
        double zCenter = cache.cannonFace.z + range * dz * 0.5d;

        // Get beam range offsets
        double xOffset = range * Math.abs(dx) * 0.5d                  // X longitudinal component
            + MAX_RADIUS * Math.sqrt(Math.max(0.0d, 1.0d - dx * dx)); // X radial component
        double yOffset = range * Math.abs(dy) * 0.5d                  // Y longitudinal component
            + MAX_RADIUS * Math.sqrt(Math.max(0.0d, 1.0d - dy * dy)); // X radial component
        double zOffset = range * Math.abs(dz) * 0.5d                  // Z longitudinal component
            + MAX_RADIUS * Math.sqrt(Math.max(0.0d, 1.0d - dz * dz)); // Z radial component

        // Set search box
        cache.searchBox.setUnchecked(
            xCenter - xOffset, yCenter - yOffset, zCenter - zOffset,
            xCenter + xOffset, yCenter + yOffset, zCenter + zOffset
        );
    }


    // LONGS
    public void defaultUpdateLongs(ServerLevel serverLevel) {
        // Change state
        switch (tickState) {
            case IDLE -> {
                serverLevel.playSound(null, worldPosition, SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.BLOCKS, 1.5f, 0.8f);
                setTickState(TickState.ARMED);
            }
            case ARMED -> setTickState(TickState.FIRING_INIT);
            case COOLDOWN -> {
                setCooldown(0);
                setTickState(TickState.IDLE);
            }
        }

        // Send update packet
        this.setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    public void shiftUpdateLongs(ServerLevel serverLevel) {
        // Set state to FIRING_INIT
        setCooldown(0);
        setTickState(TickState.FIRING_INIT);

        // Send update packet
        this.setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        CCLangHelper.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent armedState = CCLangHelper
            .text(TickState.ARMED.equals(tickState) ? "Armed" : "Disarmed")
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


    // SYNCING
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Cooldown", this.cooldown);
        tag.putInt("State", this.tickState != null ? this.tickState.getKey() : TickState.IDLE.getKey());
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.cooldown = tag.getInt("Cooldown");
        this.tickState = TickState.get(tag.getInt("State"));
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
        tag.putInt("Cooldown", this.cooldown);
        tag.putInt("State", this.tickState != null ? this.tickState.getKey() : TickState.IDLE.getKey());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.cooldown = tag.getInt("Cooldown");
        this.tickState = TickState.get(tag.getInt("State"));
    }


    // GETTERS & SETTERS
    public int getCooldown()                      { return cooldown;            }
    public void setCooldown(int cooldown)         { this.cooldown = cooldown;   }
    public TickState getTickState()               { return tickState;           }
    public void setTickState(TickState tickState) { this.tickState = tickState; }
}
