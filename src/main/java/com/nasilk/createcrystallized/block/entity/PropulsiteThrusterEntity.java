package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.behavior.PropulsiteThrusterBehavior;
import com.nasilk.createcrystallized.block.custom.PropulsiteThrusterBlock;
import com.nasilk.createcrystallized.common.ModDamageTypes;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import com.nasilk.createcrystallized.util.helper.CCLangHelper;
import com.nasilk.createcrystallized.util.type.TickState;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import java.util.List;
import static com.nasilk.createcrystallized.block.behavior.PropulsiteThrusterBehavior.*;

public class PropulsiteThrusterEntity extends BlockEntity implements IHaveGoggleInformation {
    // Tick state
    private TickState tickState = TickState.IDLE;
    private final PropulsiteThrusterBehavior behavior = new PropulsiteThrusterBehavior(this);

    // Variables (saved)
    private int charge = 0;
    private int cooldown = 0;
    private int firingTick = 0;
    private double amplitude = FIRING_AMPLITUDE;
    private double maxThrust = 0.0d;
    private double thrust = 0.0d;

    // Variables (unsaved)
    private DamageSource thrusterDamageSource = null;

    // Cache (short-lived storage to avoid garbage build-up)
    public static class Cache {
        // Tick public
        public Direction facing = Direction.NORTH;
        public final Vector3d thrusterPosition = new Vector3d();
        public final Vector3d thrusterPositionLocal = new Vector3d();
        public final Vector3d thrusterFace =  new Vector3d();
        public final Vector3d thrusterDirection = new Vector3d();
        public final Vector3d thrusterVelocity = new Vector3d();
        public final Vector3d thrusterForce =  new Vector3d();

        // BFS
        final LongOpenHashSet cluster = new LongOpenHashSet(16);
        final long[] queue = new long[16];
        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();

        // Entity pushing
        final BoundingBox3d searchBox = new BoundingBox3d();
        final Vector3d relEntityPosition = new Vector3d();

        // Particles
        public final Vector3d spawnPosition = new Vector3d();
        public final Vector3d spawnVelocity = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);


    // CONSTRUCTOR
    public PropulsiteThrusterEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel && Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
            // Get initial variables
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) return;
            Cache cache = CACHE.get();

            // Get global position
            cache.thrusterPositionLocal.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d);
            cache.thrusterPosition.set(cache.thrusterPositionLocal);
            subLevel.logicalPose().transformPosition(cache.thrusterPosition);

            // Get current net velocity
            Sable.HELPER.getVelocity(serverLevel, subLevel, cache.thrusterPositionLocal, cache.thrusterVelocity);
            cache.thrusterVelocity.mul(1.0d / 20.0d); // bps -> bpt

            // Transform local to global vectors and get face position
            cache.facing = getBlockState().getValue(PropulsiteThrusterBlock.FACING);
            cache.thrusterDirection.set(cache.facing.step());
            subLevel.logicalPose().transformNormal(cache.thrusterDirection);
            cache.thrusterFace.set(cache.thrusterPosition).fma(FACE_OFFSET, cache.thrusterDirection);

            // Select state and run tick
            behavior.tick(serverLevel, handle, cache.thrusterVelocity.lengthSquared() >= VELOCITY_THRESHOLD, cache);
        }
    }


    /// FIRING
    public void updateAmplitude(ServerLevel serverLevel, BlockPos pos) {
        // Cache setup
        Cache cache = CACHE.get(); // Get fresh cache since this method may be called from outside this class
        cache.cluster.clear(); // cluster must be manually emptied, queue will be overwritten
        int head = 0; // Front of the queue, increment to dequeue
        int tail = 0; // Back of the queue, increment to enqueue

        // Start fill
        long startLong = pos.asLong();
        cache.cluster.add(startLong);
        cache.queue[tail++] = startLong; // Enqueue

        // Perform breadth-first search (BFS) on cluster blocks for block counts
        int propulsiteCount = 0;
        int thrusterCount = 1;
        boolean overflow = false;
        BFS:
        while (head < tail) {
            // Dequeue a block position
            cache.currentPos.set(cache.queue[head++]); // Dequeue

            // Search each direction around currentPos for Propulsite and other Propulsite Thruster blocks
            for (Direction direction : DIRECTIONS) {
                // Get position and skip if unloaded || already counted
                cache.neighborPos.setWithOffset(cache.currentPos, direction);
                long neighborLong = cache.neighborPos.asLong();
                if (cache.cluster.contains(neighborLong) ||
                    !serverLevel.isLoaded(cache.neighborPos)
                ) continue;

                // Exit BFS if queue is filled
                if (tail >= 16) {
                    overflow = true;
                    break BFS;
                }

                // Update counts, cluster, and queue
                BlockState neighborState = serverLevel.getBlockState(cache.neighborPos);
                if (ModBlocks.PROPULSITE_BLOCK.has(neighborState)) propulsiteCount++;
                else if (ModBlocks.PROPULSITE_THRUSTER.has(neighborState)) thrusterCount++;
                else continue;
                cache.cluster.add(neighborLong);
                cache.queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Set updated amplitude and maxThrust
        double oldAmplitude = amplitude;
        amplitude = (1.0d + (CLUSTER_BONUS_SCALE * propulsiteCount) / thrusterCount) * FIRING_AMPLITUDE;
        this.setMaxThrust((Double.isNaN(NORM_DENOMINATOR) || NORM_DENOMINATOR == 0) ? 0.0d : amplitude / NORM_DENOMINATOR);

        // Only trigger saves and network packets if the cluster actually changed
        if (oldAmplitude != amplitude) {
            this.setChanged();
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (overflow) {
                serverLevel.sendParticles(PROPULSITE_OVERLOAD_DUST, worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d, 8, 0.5d, 0.5d, 0.5d, 1.0d);
                serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);
            }
        }
    }

    public void pushAndDamageEntities(ServerLevel serverLevel, Cache cache) {
        // Exit if thrust is too low
        if (thrust < 0.1d) return;

        // Set bounding box
        setSearchBox(cache);

        // Get entities within the bounding box
        List<Entity> entities = serverLevel.getEntities((Entity) null, cache.searchBox.toMojang(), PUSH_PREDICATE); // toMojang() allocates a new Mojang AABB...
        if (entities.isEmpty()) return;

        // Iterate through entities to apply acceleration
        for (Entity entity : entities) {
            // Get entity position relative to the thruster
            AABB entityBoundingBox = entity.getBoundingBox(); // Avoids a Vec3 allocation from entity.getBoundingBox().getCenter()
            double entityX = (entityBoundingBox.minX + entityBoundingBox.maxX) * 0.5d;
            double entityY = (entityBoundingBox.minY + entityBoundingBox.maxY) * 0.5d;
            double entityZ = (entityBoundingBox.minZ + entityBoundingBox.maxZ) * 0.5d;
            cache.relEntityPosition.set(entityX, entityY, entityZ).sub(cache.thrusterFace);

            // Longitudinal distance
            double longitudinalDistance = cache.thrusterDirection.dot(cache.relEntityPosition);
            if (longitudinalDistance < 0.0d || longitudinalDistance > 0.5d + MAX_PUSH_RANGE) continue;

            // Radial distance
            double distanceSquared = cache.relEntityPosition.lengthSquared();
            double radialDistanceSquared = distanceSquared - longitudinalDistance * longitudinalDistance;
            if (radialDistanceSquared > MAX_PUSH_RADIUS_SQUARED) continue;

            // Knockback scale
            double knockback = getKnockback(entity, longitudinalDistance);
            if (knockback < 0.1d) continue;

            // Apply knockback
            cache.relEntityPosition.set(cache.thrusterDirection).mul(Math.min(knockback, MAX_ENTITY_KNOCKBACK));
            entity.push(cache.relEntityPosition.x, cache.relEntityPosition.y, cache.relEntityPosition.z);
            boolean isServerPlayer = entity instanceof ServerPlayer;
            if (isServerPlayer) entity.hurtMarked = true;
            entity.fallDistance = 0.0f;

            // Apply damage, scale with thrust and distance
            float appliedDamage = (float) (knockback * DAMAGE_SCALE);
            if (appliedDamage < 0.5d) continue;
            if (thrusterDamageSource == null) thrusterDamageSource = ModDamageTypes.getSource(serverLevel, ModDamageTypes.PROPULSITE_THRUSTER);
            boolean wasAlive = entity.isAlive();
            entity.hurt(thrusterDamageSource, appliedDamage);

            // Handle "Crystal Cremation" Advancement
            if (wasAlive && !entity.isAlive() && isServerPlayer) AdvancementHelper.yoinkerSploinker(
                (ServerPlayer) entity,
                "crystal_cremation"
            );
        }
    }

    private void setSearchBox(Cache cache) {
        // Get direction components
        double dx = cache.thrusterDirection.x;
        double dy = cache.thrusterDirection.y;
        double dz = cache.thrusterDirection.z;

        // Find beam center
        double xCenter = cache.thrusterFace.x + MAX_PUSH_RANGE_2 * dx;
        double yCenter = cache.thrusterFace.y + MAX_PUSH_RANGE_2 * dy;
        double zCenter = cache.thrusterFace.z + MAX_PUSH_RANGE_2 * dz;

        // Get beam range offsets
        double xOffset = MAX_PUSH_RANGE_2 * Math.abs(dx)                   // X longitudinal component
            + MAX_PUSH_RADIUS * Math.sqrt(Math.max(0.0d, 1.0d - dx * dx)); // X radial component
        double yOffset = MAX_PUSH_RANGE_2 * Math.abs(dy)                   // Y longitudinal component
            + MAX_PUSH_RADIUS * Math.sqrt(Math.max(0.0d, 1.0d - dy * dy)); // X radial component
        double zOffset = MAX_PUSH_RANGE_2 * Math.abs(dz)                   // Z longitudinal component
            + MAX_PUSH_RADIUS * Math.sqrt(Math.max(0.0d, 1.0d - dz * dz)); // Z radial component

        // Set search box
        cache.searchBox.setUnchecked(
            xCenter - xOffset, yCenter - yOffset, zCenter - zOffset,
            xCenter + xOffset, yCenter + yOffset, zCenter + zOffset
        );
    }

    private double getKnockback(Entity entity, double longitudinalDistance) {
        double inverseDistanceRatio = 1.0d - (longitudinalDistance - 0.5d) / MAX_PUSH_RANGE; // 1.0 - [0.0 to 1.0] distance ratio
        double decay = inverseDistanceRatio * inverseDistanceRatio * inverseDistanceRatio; // (1 - x)^3 approximates e^(-3x) from [0.0 to 1.0] and is cheaper on CPU
        double knockback = thrust * PUSH_SCALE * decay;
        if (entity.isShiftKeyDown()) knockback /= PUSH_SHIFT_REDUCTION;
        return knockback;
    }


    // LONGS
    public void defaultUpdateLongs(ServerLevel serverLevel) {
        if (!(Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel)) return;

        // Change state
        switch (tickState) {
            case IDLE -> {
                setCharge(MAX_CHARGE);
                serverLevel.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f,1.2f);
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
        if (!(Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel)) return;

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

        final MutableComponent currentCharge = CCLangHelper
            .number(5.0d * charge / 3.0d).text("%")
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.current_charge", currentCharge)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent armedState = CCLangHelper
            .text(TickState.ARMED.equals(tickState) ? "Armed" : "Disarmed")
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.armed_state", armedState)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent maximumThrust = CCLangHelper
            .pixelNewton(maxThrust)
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.maximum_thrust", maximumThrust)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        if (cooldown > 0) {
            CCLangHelper.translate("goggles.cooling_down", CCLangHelper.number(cooldown / 20.0d).text("s").component())
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
        tag.putInt("Charge", this.charge);
        tag.putInt("Cooldown", this.cooldown);
        tag.putInt("FiringTick", this.firingTick);
        tag.putDouble("Amplitude", this.amplitude);
        tag.putDouble("MaxThrust", this.maxThrust);
        tag.putDouble("Thrust", this.thrust);
        tag.putInt("State", this.tickState != null ? this.tickState.getKey() : TickState.IDLE.getKey());
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
        this.firingTick = Math.clamp(tag.getInt("FiringTick"), 0, FIRING_DURATION - 1);
        this.amplitude = tag.getDouble("Amplitude");
        this.maxThrust = tag.getDouble("MaxThrust");
        this.thrust = tag.getDouble("Thrust");
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
        tag.putInt("Charge", this.charge);
        tag.putInt("Cooldown", this.cooldown);
        tag.putInt("FiringTick", this.firingTick);
        tag.putDouble("Amplitude", this.amplitude);
        tag.putDouble("MaxThrust", this.maxThrust);
        tag.putDouble("Thrust", this.thrust);
        tag.putInt("State", this.tickState != null ? this.tickState.getKey() : TickState.IDLE.getKey());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
        this.firingTick = Math.clamp(tag.getInt("FiringTick"), 0, FIRING_DURATION - 1);
        this.amplitude = tag.getDouble("Amplitude");
        this.maxThrust = tag.getDouble("MaxThrust");
        this.thrust = tag.getDouble("Thrust");
        this.tickState = TickState.get(tag.getInt("State"));
    }


    // GETTERS & SETTERS
    public int getCharge()                        { return charge;                }
    public void setCharge(int charge)             { this.charge = charge;         }
    public int getCooldown()                      { return cooldown;              }
    public void setCooldown(int cooldown)         { this.cooldown = cooldown;     }
    public int getFiringTick()                    { return firingTick;            }
    public void setFiringTick(int firingTick)     { this.firingTick = firingTick; }
    public double getAmplitude()                  { return amplitude;             }
    public double getMaxThrust()                  { return maxThrust;             }
    public void setMaxThrust(double maxThrust)    { this.maxThrust = maxThrust;   }
    public double getThrust()                     { return thrust;                }
    public void setThrust(double thrust)          { this.thrust = thrust;         }
    public TickState getTickState()               { return tickState;             }
    public void setTickState(TickState tickState) { this.tickState = tickState;   }
}
