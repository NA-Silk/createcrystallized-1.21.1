package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.custom.PropulsiteThrusterBlock;
import com.nasilk.createcrystallized.damage.ModDamageTypes;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.CCLang;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import java.util.List;

@SuppressWarnings({"SpellCheckingInspection", "GrazieInspectionRunner"})
public class PropulsiteThrusterEntity extends BlockEntity implements IHaveGoggleInformation {
    // Tick variables (saved)
    private int charge = 0;
    private int cooldown = 0;
    private int firingTick = 0;
    private double amplitude = AMPLITUDE;
    private double thrust = 0.0d;
    private boolean armed = false;
    private boolean firing = false;

    // Tick variables (unsaved)
    private int tickCounter = 0;
    private Direction facing = Direction.NORTH;
    private final Vector3d thrusterDirection = new Vector3d();
    private final Vector3d thrusterPosition = new Vector3d();
    private final Vector3d thrusterFace =  new Vector3d();

    // Entity pushing variables (unsaved)
    private Direction facingValidation = null;
    private final Vector3d localMin = new Vector3d();
    private final Vector3d localMax = new Vector3d();
    private DamageSource thrusterDamageSource = null;

    // Tick constants TODO consider JSONifying these for fast /reload testing
    private static final int MAX_CHARGE = 60; // How long it takes for the burst to be ready after receiving redstone power in ticks
    private static final int MAX_COOLDOWN = 100; // How long it takes for the block to be able to be charged again in ticks
    private static final int BURST_DURATION = 40; // How long it takes for the full burst to go though in ticks
    private static final double AMPLITUDE = 100.0d; // How much total thrust is output over the length of the burst
    private static final double STANDARD_DEVIATION = 5.0d; // Curve spread
    private static final double MEAN = 20.0d; // Curve middle
    private static final double NORM_DENOMINATOR = STANDARD_DEVIATION * Math.sqrt(2.0 * Math.PI); // Precomputed denominator

    // BFS constants
    private static final int MAX_CLUSTER_SIZE = 16; // 15 Propulsite + 1 Thruster
    private static final double CLUSTER_SCALE = 2.0d;
    private static final Direction[] DIRECTIONS = Direction.values();

    // Entity pushing constants
    private static final double MAX_ACCELERATION = 6.0d; // Maximum acceleration allowed in blocks per tick
    private static final double MAX_PUSH_RANGE = 8.0d; // Length effectiveness distance
    private static final double MAX_PUSH_RADIUS = 0.75d; // Radial effectiveness distance
    private static final double PUSH_DECAY_RATE = 3.0d; // Acceleration distance dropoff rate
    private static final double PUSH_FACTOR = 0.1d; // Acceleration multiplier
    private static final double PUSH_SHIFT_FACTOR = 0.125d; // Acceleration multiplier while holding shift
    private static final double DAMAGE_MULTIPLIER = 5.0d; // Thruster damage multiplier
    private static final double SQR_MAX_PUSH_RADIUS = MAX_PUSH_RADIUS * MAX_PUSH_RADIUS; // Precomputed radial distance squared

    // Charging particle constants
    private static final int NUM_PARTICLES = 2; // Number of particles to spawn per tick
    private static final double PARTICLE_RADIUS = 1.5; // Particle spawn range from the face, in blocks

    // Firing particle constants
    private static final int MIN_PARTICLES = 3;
    private static final int MAX_PARTICLES = 11;
    private static final double PARTICLE_SPREAD = 0.10;
    private static final double MIN_PARTICLE_SPEED = 0.15;
    private static final double MAX_PARTICLE_SPEED = 0.5;

    // Cache (short-lived storage to avoid garbage build-up)
    private static class Cache {
        // Tick
        final Vector3d thrusterForce =  new Vector3d();

        // BFS
        final LongOpenHashSet cluster = new LongOpenHashSet(MAX_CLUSTER_SIZE);
        final long[] queue = new long[MAX_CLUSTER_SIZE];

        // Entity pushing
        final BoundingBox3d aabb = new BoundingBox3d();
        final Vector3d globalThrusterDirection = new Vector3d();
        final Vector3d globalThrusterPosition = new Vector3d();
        final Vector3d relEntityPosition = new Vector3d();
        final Vector3d relEntityRadialDistance = new Vector3d();

        // Particles
        final Vector3d spawnPosition =  new Vector3d();
        final Vector3d spawnVelocity = new Vector3d();
        final Vector3d endPosition = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);


    public PropulsiteThrusterEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROPULSITE_THRUSTER.get(), pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlockState();
            boolean powered = state.getValue(PropulsiteThrusterBlock.POWERED);

            facing = state.getValue(PropulsiteThrusterBlock.FACING);
            thrusterDirection.set(facing.step());
            thrusterPosition.set(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            thrusterFace.set(thrusterPosition).fma(0.6, thrusterDirection);

            // Update amplitude every second (20 ticks)
            if (tickCounter++ % 20 == 0) {
                updateAmplitude(serverLevel, worldPosition);
                tickCounter = 1;
            }

            // Cooldown TODO custom cooling down sound, maybe smoke particle too?
            if (cooldown > 0) {
                if (!powered) {
                    cooldown--;
                    this.setChanged();
                }
                return;
            }

            // Charging TODO custom charging sound
            if (powered && !armed && charge < MAX_CHARGE) {
                charge++;
                this.setChanged();

                if (Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
                    addChargingParticles(serverLevel, subLevel);
                } else {
                    addChargingParticles(serverLevel, null);
                }

                if (charge >= MAX_CHARGE) {
                    armed = true;
                    this.setChanged();

                    // TODO custom arming sound, or something that fits
                    serverLevel.playSound(
                        null,
                        worldPosition,
                        SoundEvents.BEACON_ACTIVATE,
                        SoundSource.BLOCKS,
                        1.5F,1.2F
                    );
                }
            }

            // Discharging TODO make some sort of visual/audio queue that discharging is happening
            if (!powered && !armed && charge > 0) {
                charge = Math.max(charge - 2, 0);
                this.setChanged();
            }

            // Firing initialization
            if (armed && !powered && !firing) {
                firing = true;
                firingTick = 0;
                this.setChanged();

                serverLevel.playSound(
                    null,
                    worldPosition,
                    SoundEvents.ENDER_DRAGON_SHOOT, // Firing sound
                    SoundSource.BLOCKS,
                    1.5F,1.0F
                );
            }

            // Firing sequence
            if (firing) {
                // The curve that determines the total thrust of the burst
                thrust =
                    (amplitude / NORM_DENOMINATOR) // Maximum
                    * Math.exp(-0.5 * Math.pow((firingTick - MEAN) / STANDARD_DEVIATION, 2)); // Curve computation

                // Hande subLevel effects
                ServerSubLevel serverSubLevel = null; // Assigned for external use
                if (Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
                    serverSubLevel = subLevel;

                    RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
                    if (!handle.isValid()) return;

                    Cache cache = CACHE.get();
                    cache.thrusterForce.set(thrusterDirection).mul(-thrust);
                    handle.applyImpulseAtPoint(thrusterPosition, cache.thrusterForce);
                }

                firingTick++;
                if (firingTick >= BURST_DURATION) {
                    firing = false;
                    armed = false;
                    charge = 0;
                    thrust = 0;
                    cooldown = MAX_COOLDOWN;
                }
                this.setChanged();

                // Effects
                pushEntities(serverLevel, serverSubLevel);
                addFiringParticles(serverLevel, serverSubLevel);

                // Force packet update (for tooltips)
                if (firingTick % 5 == 0) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
    }

    public void updateAmplitude(Level level, BlockPos pos) {
        // Using specialized set and primitive array for long efficiency
        Cache cache = CACHE.get();
        LongOpenHashSet cluster = cache.cluster;
        long[] queue = cache.queue;

        // Cache setup
        cluster.clear(); // cluster must be manually emptied, queue will be overwritten
        int head = 0; // Front of the queue, increment to dequeue
        int tail = 0; // Back of the queue, increment to enqueue

        // Start fill
        long startLong = pos.asLong();
        cluster.add(startLong);
        queue[tail++] = startLong; // Enqueue

        // Perform breadth-first search (BFS) on cluster blocks for block counts
        int propulsiteCount = 0;
        int thrusterCount = 1;
        BFS:
        while (head < tail) {
            // Dequeue a block position
            BlockPos currentPos = BlockPos.of(queue[head++]); // Dequeue

            // Search each direction around currentPos for Propulsite and other Propulsite Thruster blocks
            for (Direction direction : DIRECTIONS) {
                // Exit loop if queue is filled
                if (tail >= MAX_CLUSTER_SIZE) break BFS;

                // Get position and skip if unloaded || already counted
                BlockPos neighborPos = currentPos.relative(direction);
                long neighborLong = neighborPos.asLong();
                if (cluster.contains(neighborLong) || !level.isLoaded(neighborPos)) continue;

                // Update counts, cluster, and queue
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.is(ModBlocks.PROPULSITE_BLOCK)) propulsiteCount++;
                else if (neighborState.is(ModBlocks.PROPULSITE_THRUSTER)) thrusterCount++;
                else continue;
                cluster.add(neighborLong);
                queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Set updated amplitude
        amplitude = (1.0d + (CLUSTER_SCALE * propulsiteCount) / thrusterCount) * AMPLITUDE;

        // Tell the game this block needs to be saved to disk
        this.setChanged();

        // Tell the server to send the new amplitude to the client for the Goggle tooltips
        if (!level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    private void pushEntities(ServerLevel serverLevel, ServerSubLevel subLevel) {
        if (thrust < 0.1) return;
        Cache cache = CACHE.get();

        // Recompute localMin and localMax if facing has changed (from level shifting)
        if (facingValidation != facing) {
            facingValidation = facing;

            // Reset localMin and localMax
            localMin.set(-MAX_PUSH_RADIUS);
            localMax.set(MAX_PUSH_RADIUS);

            // Get step variations
            int stepX = facing.getStepX(); // 1 if East,  -1 if West,  0 otherwise
            int stepY = facing.getStepY(); // 1 if Up,    -1 if Down,  0 otherwise
            int stepZ = facing.getStepZ(); // 1 if South, -1 if North, 0 otherwise

            // Translate step values into bounding box direction
            if      (stepX == 1)  { localMin.x = 0.5d;                      localMax.x = 0.5d + MAX_PUSH_RANGE; } // East
            else if (stepX == -1) { localMin.x = -0.5d - MAX_PUSH_RANGE;    localMax.x = -0.5d;                 } // West
            else if (stepY == 1)  { localMin.y = 0.5d;                      localMax.y = 0.5d + MAX_PUSH_RANGE; } // Up
            else if (stepY == -1) { localMin.y = -0.5d - MAX_PUSH_RANGE;    localMax.y = -0.5d;                 } // Down
            else if (stepZ == 1)  { localMin.z = 0.5d;                      localMax.z = 0.5d + MAX_PUSH_RANGE; } // South
            else if (stepZ == -1) { localMin.z = -0.5d - MAX_PUSH_RANGE;    localMax.z = -0.5d;                 } // North
        }

        // Getting bounding box
        cache.aabb.setUnchecked(
            localMin.x + thrusterPosition.x, localMin.y + thrusterPosition.y, localMin.z + thrusterPosition.z,
            localMax.x + thrusterPosition.x, localMax.y + thrusterPosition.y, localMax.z + thrusterPosition.z
        );

        // Convert sublevel (local) vectors to global vectors (call by reference)
        cache.globalThrusterDirection.set(thrusterDirection);
        cache.globalThrusterPosition.set(thrusterPosition);
        if (subLevel != null) {
            cache.aabb.transform(subLevel.logicalPose(), cache.aabb);
            subLevel.logicalPose().transformNormal(cache.globalThrusterDirection);
            subLevel.logicalPose().transformPosition(cache.globalThrusterPosition);
        }

        // Get entities within the bounding box
        List<Entity> entities = serverLevel.getEntities(null, cache.aabb.toMojang()); // toMojang() Allocates a new Mojang AABB...
        if (entities.isEmpty()) return;

        // Iterate through entities to apply acceleration
        for (Entity entity : entities) {
            if (entity instanceof AbstractContraptionEntity || AirCurrent.isPlayerCreativeFlying(entity) || DivingBootsItem.isWornBy(entity)) continue;

            // Get entity position relative to the thruster
            AABB entityBoundingBox = entity.getBoundingBox(); // Avoids a Vec3 allocation from entity.getBoundingBox().getCenter()
            double entityX = (entityBoundingBox.minX + entityBoundingBox.maxX) * 0.5d;
            double entityY = (entityBoundingBox.minY + entityBoundingBox.maxY) * 0.5d;
            double entityZ = (entityBoundingBox.minZ + entityBoundingBox.maxZ) * 0.5d;
            cache.relEntityPosition.set(entityX, entityY, entityZ).sub(cache.globalThrusterPosition);

            // Length distance scalar
            double relEntityLengthScalar = cache.globalThrusterDirection.dot(cache.relEntityPosition);
            if (relEntityLengthScalar < 0.5d || relEntityLengthScalar > 0.5d + MAX_PUSH_RANGE) continue;

            // Radial distance scalar
            cache.relEntityRadialDistance.set(cache.relEntityPosition).fma(-relEntityLengthScalar, cache.globalThrusterDirection);
            if (cache.relEntityRadialDistance.lengthSquared() > SQR_MAX_PUSH_RADIUS) continue;

            // Acceleration scalar
            double distanceRatio = (relEntityLengthScalar - 0.5d) / MAX_PUSH_RANGE; // [0.0 to 1.0] distanct ratio
            double accelerationScalar = thrust * PUSH_FACTOR * Math.exp(-PUSH_DECAY_RATE * distanceRatio);
            if (entity.isShiftKeyDown()) accelerationScalar *= PUSH_SHIFT_FACTOR;
            if (accelerationScalar < 0.1d) continue;

            // Handle acceleration effect
            Vec3 entityVelocity = entity.getDeltaMovement(); // Internal minecraft reference, no extra allocation (yay)
            entity.setDeltaMovement(
                entityVelocity.add(
                    Math.clamp(accelerationScalar * cache.globalThrusterDirection.x, -MAX_ACCELERATION, MAX_ACCELERATION),
                    Math.clamp(accelerationScalar * cache.globalThrusterDirection.y, -MAX_ACCELERATION, MAX_ACCELERATION),
                    Math.clamp(accelerationScalar * cache.globalThrusterDirection.z, -MAX_ACCELERATION, MAX_ACCELERATION)
                )
            );
            entity.fallDistance = 0;

            // Sync client-side (player) motion
            if (entity instanceof ServerPlayer serverPlayer) serverPlayer.hurtMarked = true;

            // Handle damage effect
            float appliedDamage = (float) (accelerationScalar * DAMAGE_MULTIPLIER);
            if (appliedDamage < 0.5d) continue;
            if (thrusterDamageSource == null) thrusterDamageSource = ModDamageTypes.getSource(serverLevel, ModDamageTypes.PROPULSITE_THRUSTER);
            entity.hurt(thrusterDamageSource, appliedDamage);
        }
    }

    private void addChargingParticles(ServerLevel level, ServerSubLevel subLevel) {
        Cache cache = CACHE.get();

        // Compute each particle
        for(int i = 0; i < NUM_PARTICLES; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
            double ySpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;
            double zSpeed = (level.random.nextDouble() - 0.5) * 2.0 * PARTICLE_RADIUS;

            // Get local/sublevel vectors
            cache.spawnPosition.set(thrusterFace);
            cache.spawnVelocity.set(xSpeed, ySpeed, zSpeed);

            // Convert sublevel (local) vectors to global vectors
            if (subLevel != null) {
                cache.endPosition.set(cache.spawnPosition).add(cache.spawnVelocity);

                // Local -> global conversion (call by reference)
                subLevel.logicalPose().transformPosition(cache.spawnPosition);
                subLevel.logicalPose().transformPosition(cache.endPosition);

                cache.spawnVelocity.set(cache.endPosition).sub(cache.spawnPosition);
            }

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            level.sendParticles(
                ModParticles.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(),
                cache.spawnPosition.x, cache.spawnPosition.y, cache.spawnPosition.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                cache.spawnVelocity.x, cache.spawnVelocity.y, cache.spawnVelocity.z,
                1.0 // Use above speed values
            );
        }
    }

    private void addFiringParticles(ServerLevel serverLevel, ServerSubLevel subLevel) {
        Cache cache = CACHE.get();

        double maxThrust = amplitude / NORM_DENOMINATOR;
        double thrustRatio = Math.max(0.0, thrust / maxThrust); // [0.0 to 1.0] multiplier based on current thrust strength

        double baseVelocity = MIN_PARTICLE_SPEED + ((MAX_PARTICLE_SPEED - MIN_PARTICLE_SPEED) * thrustRatio); // Faster jet at peak thrust
        int particleCount = MIN_PARTICLES + (int) ((MAX_PARTICLES - MIN_PARTICLES) * thrustRatio);

        // Compute each particle
        for (int i = 0; i < particleCount; i++) {
            // Apply slight random spread to the cone of the thrust
            double dirX = facing.getStepX() + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);
            double dirY = facing.getStepY() + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);
            double dirZ = facing.getStepZ() + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);

            // Get local/sublevel vectors
            cache.spawnPosition.set(thrusterFace);
            cache.spawnVelocity.set(dirX, dirY, dirZ).normalize().mul(baseVelocity); // Normalize the direction and scale by baseVelocity

            // Convert sublevel (local) vectors to global vectors
            if (subLevel != null) {
                cache.endPosition.set(cache.spawnPosition).add(cache.spawnVelocity);

                // Local -> global conversion (call by reference)
                subLevel.logicalPose().transformPosition(cache.spawnPosition);
                subLevel.logicalPose().transformPosition(cache.endPosition);

                cache.spawnVelocity.set(cache.endPosition).sub(cache.spawnPosition);
            }

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticles.PROPULSITE_THRUSTER_FIRING_PARTICLES.get(),
                cache.spawnPosition.x, cache.spawnPosition.y, cache.spawnPosition.z,
                0,
                cache.spawnVelocity.x, cache.spawnVelocity.y, cache.spawnVelocity.z,
                1.0 // Use spawnVelocity values
            );
        }
    }


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        CCLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentThrust = CCLang
                .pixelNewton(thrust)
                .style(ChatFormatting.AQUA)
                .component();
        CCLang.translate("goggles.current_thrust", currentThrust)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        final MutableComponent maximumThrust = CCLang
                .pixelNewton(amplitude / NORM_DENOMINATOR)
                .style(ChatFormatting.AQUA)
                .component();
        CCLang.translate("goggles.maximum_thrust", maximumThrust)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        final MutableComponent totalThrust = CCLang
                .pixelNewton(amplitude)
                .style(ChatFormatting.AQUA)
                .component();
        CCLang.translate("goggles.total_thrust", totalThrust)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putDouble("Thrust", this.thrust);
        tag.putDouble("Amplitude", this.amplitude);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.thrust = tag.getDouble("Thrust");
        this.amplitude = tag.getDouble("Amplitude");
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
        tag.putDouble("Thrust", this.thrust);
        tag.putBoolean("Armed", this.armed);
        tag.putBoolean("Firing", this.firing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
        this.firingTick = tag.getInt("FiringTick");
        this.amplitude = tag.getDouble("Amplitude");
        this.thrust = tag.getDouble("Thrust");
        this.armed = tag.getBoolean("Armed");
        this.firing = tag.getBoolean("Firing");
    }


    // GETTERS & SETTERS (for compatibility)
    @SuppressWarnings("unused")
    public Direction getBlockDirection() {
        return this.facing;
    }

    @SuppressWarnings({"unused", "SameReturnValue"})
    public double getAirflow() {
        return 0;
    }

    @SuppressWarnings("unused")
    public double getThrust() {
        return this.thrust;
    }

    @SuppressWarnings("unused")
    public boolean isActive() {
        return this.firing;
    }
}
