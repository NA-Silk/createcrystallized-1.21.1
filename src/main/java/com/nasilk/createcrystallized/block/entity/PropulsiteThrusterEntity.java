package com.nasilk.createcrystallized.block.entity;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.behavior.PropulsiteThrusterBehavior;
import com.nasilk.createcrystallized.block.custom.PropulsiteThrusterBlock;
import com.nasilk.createcrystallized.damage.ModDamageTypes;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.helper.CCLangHelper;
import com.nasilk.createcrystallized.util.type.TickState;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings({"SpellCheckingInspection", "GrazieInspectionRunner"})
public class PropulsiteThrusterEntity extends BlockEntity implements IHaveGoggleInformation {
    // Variables (saved)
    private int charge = 0;
    private int cooldown = 0;
    private int firingTick = 0;
    private double amplitude = AMPLITUDE;
    private double thrust = 0.0d;
    private boolean armed = false;
    private boolean firing = false;

    // Variables (unsaved)
    private DamageSource thrusterDamageSource = null;

    // Tick constants
    private static final int PACKET_UPDATE_RATE = 10;
    private static final int SIMPLE_PARTICLE_RATE = 20;
    private static final int CHARGING_PARTICLE_RATE = 2;
    private static final int MAX_CHARGE = 60; // How long it takes for the burst to be ready after receiving redstone power in ticks
    private static final int MAX_COOLDOWN = 100; // How long it takes for the block to be able to be charged again in ticks
    private static final int BURST_DURATION = 10; // How long it takes for the full burst to go though in ticks
    private static final double FACE_OFFSET = 0.6d;
    private static final double AMBIENT_RATE = 8e-5d;
    private static final double VELOCITY_SCALE = 15.0d;
    private static final double THRESHOLD = 1.0d;
    private static final double AMPLITUDE = 100.0d; // How much total thrust is output over the length of the burst
    private static final double STANDARD_DEVIATION = 1.5d; // Curve spread
    private static final double MEAN = 3.0d; // Curve middle
    private static final double NORM_DENOMINATOR = STANDARD_DEVIATION * Math.sqrt(2.0d * Math.PI); // Precomputed denominator
    private static final double[] BURST_CURVE = new double[BURST_DURATION];
    static {
        for (int i = 0; i < BURST_DURATION; i++) {
            double diff = (i - MEAN) / STANDARD_DEVIATION;
            BURST_CURVE[i] = Math.exp(-0.5d * diff * diff);
        }
    }

    // BFS constants
    private static final int MAX_CLUSTER_SIZE = 16; // 15 Propulsite + 1 Thruster
    private static final double CLUSTER_SCALE = 2.0d;
    private static final Direction[] DIRECTIONS = Direction.values();

    // Entity pushing constants
    private static final double MAX_ACCELERATION = 6.0d; // Maximum acceleration allowed in blocks per tick
    private static final double MAX_PUSH_RANGE = 8.0d; // Length effectiveness distance
    private static final double MAX_PUSH_RADIUS = 0.75d; // Radial effectiveness distance
    private static final double PUSH_FACTOR = 0.1d; // Acceleration multiplier
    private static final double PUSH_SHIFT_FACTOR = 0.125d; // Acceleration multiplier while holding shift
    private static final double DAMAGE_MULTIPLIER = 5.0d; // Thruster damage multiplier
    private static final double SQR_MAX_PUSH_RADIUS = MAX_PUSH_RADIUS * MAX_PUSH_RADIUS; // Precomputed radial distance squared
    private static final Predicate<Entity> PUSH_PREDICATE = entity ->
        !entity.isSpectator()
            && !(entity instanceof AbstractContraptionEntity)
            && !AirCurrent.isPlayerCreativeFlying(entity)
            && !DivingBootsItem.isWornBy(entity);

    // Charging particle constants
    private static final int NUM_PARTICLES = 2; // Number of particles to spawn per tick
    private static final double PARTICLE_RADIUS = 1.5d; // Particle spawn range from the face, in blocks

    // Firing particle constants
    private static final int MIN_PARTICLES = 3;
    private static final int MAX_PARTICLES = 11;
    private static final double PARTICLE_SPREAD = 0.10d;
    private static final double MIN_PARTICLE_SPEED = 0.15d;
    private static final double MAX_PARTICLE_SPEED = 0.5d;

    // Cache (short-lived storage to avoid garbage build-up)
    private static class Cache {
        // Tick
        Direction facing = Direction.NORTH;
        final Vector3d thrusterForce =  new Vector3d();
        final Vector3d thrusterDirection = new Vector3d();
        final Vector3d thrusterFace =  new Vector3d();
        final Vector3d thrusterPosition = new Vector3d();
        final Vector3d thrusterPositionLocal = new Vector3d();
        final Vector3d thrusterVelocity = new Vector3d();

        // BFS
        final long[] queue = new long[MAX_CLUSTER_SIZE];
        final LongOpenHashSet cluster = new LongOpenHashSet(MAX_CLUSTER_SIZE);

        // Entity pushing
        final BoundingBox3d searchBox = new BoundingBox3d();
        final Vector3d relEntityPosition = new Vector3d();
        final Vector3d relEntityRadialDistance = new Vector3d();

        // Particles
        final Vector3d spawnPosition = new Vector3d();
        final Vector3d spawnVelocity = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    // Tick state
    private TickState tickState = TickState.IDLE;
    public TickState getTickState() {
        return tickState;
    }
    private final PropulsiteThrusterBehavior behavior = new PropulsiteThrusterBehavior(this);


    public PropulsiteThrusterEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROPULSITE_THRUSTER.get(), pos, state);
    }


    // TICK BEHAVIOR
    public void tick() {
        if (level instanceof ServerLevel serverLevel && Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel subLevel) {
            // Get the physics handle and cache
            RigidBodyHandle handle = RigidBodyHandle.of(subLevel);
            if (!handle.isValid()) return;
            Cache cache = CACHE.get();
            BlockState blockState = getBlockState();
            boolean powered = blockState.getValue(PropulsiteThrusterBlock.POWERED);

            // Get global position
            cache.thrusterPositionLocal.set(worldPosition.getX() + 0.5d, worldPosition.getY() + 0.5d, worldPosition.getZ() + 0.5d);
            cache.thrusterPosition.set(cache.thrusterPositionLocal);
            subLevel.logicalPose().transformPosition(cache.thrusterPosition);

            // Get current net velocity
            Sable.HELPER.getVelocity(serverLevel, subLevel, cache.thrusterPositionLocal, cache.thrusterVelocity);
            cache.thrusterVelocity.mul(1.0d / 20.0d); // bps -> bpt

            // Get velocitySquared
            double velocitySquared = cache.thrusterVelocity.lengthSquared();
            if (velocitySquared > 1e-3d) velocitySquared *= VELOCITY_SCALE; // Set velocitySquared = LINEAR_SCALE*||-linearVelocity||^2
            else velocitySquared = 0.0d;

            // Transform local to global vectors and get face position
            cache.facing = blockState.getValue(PropulsiteThrusterBlock.FACING);
            cache.thrusterDirection.set(cache.facing.step());
            subLevel.logicalPose().transformNormal(cache.thrusterDirection);
            cache.thrusterFace.set(cache.thrusterPosition).fma(FACE_OFFSET, cache.thrusterDirection);

            // Update amplitude
            boolean randTick = (serverLevel.getGameTime() + worldPosition.hashCode()) % 20 == 0;
            if (randTick) updateAmplitude(serverLevel, worldPosition);

            // Select state
            if      (            cooldown > 0        && !powered                     ) tickState = TickState.COOLDOWN;
            else if (!armed  &&  charge < MAX_CHARGE &&  velocitySquared >= THRESHOLD) tickState = TickState.CHARGING;
            else if (!armed  &&  charge > 0          &&  velocitySquared <  THRESHOLD) tickState = TickState.DISCHARGING;
            else if ( armed  && !firing              &&  powered                     ) tickState = TickState.FIRING_INIT;
            else if (            firing                                              ) tickState = TickState.FIRING;
            else                                                                       tickState = TickState.IDLE;

            // Cooldown
            if (tickState == TickState.COOLDOWN) {
                cooldown--;
                if (cooldown % SIMPLE_PARTICLE_RATE == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        cache.thrusterPosition.x, cache.thrusterPosition.y, cache.thrusterPosition.z,
                        1, 0.5d, 0.5d, 0.5d, 0.1d
                    );
                }
                this.setChanged();
                if (cooldown % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                return;
            }

            // Charging
            if (tickState == TickState.CHARGING) {
                charge++;
                if (charge >= MAX_CHARGE) {
                    armed = true;
                    serverLevel.playSound(
                        null, worldPosition,
                        SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS,
                        1.5f,1.2f
                    );
                }
                if (charge % CHARGING_PARTICLE_RATE == 0) addChargingParticles(serverLevel, cache);
                this.setChanged();
                if (charge % PACKET_UPDATE_RATE == 0 || armed) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                return;
            }

            // Discharging
            if (tickState == TickState.DISCHARGING) {
                charge--;
                if (charge % SIMPLE_PARTICLE_RATE == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.WHITE_SMOKE,
                        cache.thrusterPosition.x, cache.thrusterPosition.y, cache.thrusterPosition.z,
                        10, 0.5d, 0.5d, 0.5d, 0.1d
                    );
                }
                this.setChanged();
                if (charge % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                return;
            }

            // Firing initialization
            if (tickState == TickState.FIRING_INIT) {
                firing = true;
                firingTick = 0;
                serverLevel.playSound(
                    null, worldPosition,
                    SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.BLOCKS,
                    1.5f,1.0f
                );
                this.setChanged();
                serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                return; // Wait a tick, don't be too hasty with sending packets
            }

            // Firing sequence
            if (tickState == TickState.FIRING) {
                // The curve that determines the total thrust of the burst
                thrust = (amplitude / NORM_DENOMINATOR) // Maximum
                    * BURST_CURVE[firingTick]; // Curve computation

                // Hande subLevel effects
                cache.thrusterForce.set(cache.facing.step()).mul(-thrust);
                handle.applyImpulseAtPoint(cache.thrusterPositionLocal, cache.thrusterForce);

                // Update firing state
                firingTick++;
                if (firingTick >= BURST_DURATION) {
                    firing = false;
                    armed = false;
                    charge = 0;
                    thrust = 0;
                    cooldown = MAX_COOLDOWN;
                }

                // Effects
                pushEntities(serverLevel, cache);
                addFiringParticles(serverLevel, cache);
                this.setChanged();
                if (firingTick % PACKET_UPDATE_RATE == 0) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                return;
            }

            // Idling; ambiance is nice
            if (tickState == TickState.IDLE) {
                if (serverLevel.getRandom().nextDouble() < AMBIENT_RATE) {
                    if (charge == 0) serverLevel.playSound(
                        null, worldPosition,
                        SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS,
                        1.0f,0.8f
                    );
                    else if (charge == MAX_CHARGE) serverLevel.playSound(
                        null, worldPosition,
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                        1.0f,0.8f
                    );
                }
            }
        }
    }

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
        BFS:
        while (head < tail) {
            // Dequeue a block position
            BlockPos currentPos = BlockPos.of(cache.queue[head++]); // Dequeue

            // Search each direction around currentPos for Propulsite and other Propulsite Thruster blocks
            for (Direction direction : DIRECTIONS) {
                // Exit loop if queue is filled
                if (tail >= MAX_CLUSTER_SIZE) break BFS;

                // Get position and skip if unloaded || already counted
                BlockPos neighborPos = currentPos.relative(direction);
                long neighborLong = neighborPos.asLong();
                if (cache.cluster.contains(neighborLong) || !serverLevel.isLoaded(neighborPos)) continue;

                // Update counts, cluster, and queue
                BlockState neighborState = serverLevel.getBlockState(neighborPos);
                if (neighborState.is(ModBlocks.PROPULSITE_BLOCK)) propulsiteCount++;
                else if (neighborState.is(ModBlocks.PROPULSITE_THRUSTER)) thrusterCount++;
                else continue;
                cache.cluster.add(neighborLong);
                cache.queue[tail++] = neighborLong; // Enqueue
            }
        }

        // Set updated amplitude
        double oldAmplitude = amplitude;
        amplitude = (1.0d + (CLUSTER_SCALE * propulsiteCount) / thrusterCount) * AMPLITUDE;

        // Only trigger saves and network packets if the cluster actually changed
        if (oldAmplitude != amplitude) {
            this.setChanged();
            if (!serverLevel.isClientSide) serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    private void pushEntities(ServerLevel serverLevel, Cache cache) {
        // Exit if thrust is too low
        if (thrust < 0.1d) return;

        // Set bounding box
        cache.searchBox.setUnchecked(
            cache.thrusterPosition.x - MAX_PUSH_RANGE, cache.thrusterPosition.y - MAX_PUSH_RANGE, cache.thrusterPosition.z - MAX_PUSH_RANGE,
            cache.thrusterPosition.x + MAX_PUSH_RANGE, cache.thrusterPosition.y + MAX_PUSH_RANGE, cache.thrusterPosition.z + MAX_PUSH_RANGE
        );

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
            cache.relEntityPosition.set(entityX, entityY, entityZ).sub(cache.thrusterPosition);

            // Length distance scalar
            double relEntityLengthScalar = cache.thrusterDirection.dot(cache.relEntityPosition);
            if (relEntityLengthScalar < 0.5d || relEntityLengthScalar > 0.5d + MAX_PUSH_RANGE) continue;

            // Radial distance scalar
            cache.relEntityRadialDistance.set(cache.relEntityPosition).fma(-relEntityLengthScalar, cache.thrusterDirection);
            if (cache.relEntityRadialDistance.lengthSquared() > SQR_MAX_PUSH_RADIUS) continue;

            // Acceleration scalar
            double inverseDistanceRatio = 1.0d - (relEntityLengthScalar - 0.5d) / MAX_PUSH_RANGE; // 1.0 - [0.0 to 1.0] distanct ratio
            double decay = inverseDistanceRatio * inverseDistanceRatio * inverseDistanceRatio; // (1 - x)^3 approximates e^(-3x) from [0.0 to 1.0] and is cheaper on CPU
            double accelerationScalar = thrust * PUSH_FACTOR * decay;
            if (entity.isShiftKeyDown()) accelerationScalar *= PUSH_SHIFT_FACTOR;
            if (accelerationScalar < 0.1d) continue;

            // Handle acceleration effect
            Vec3 entityVelocity = entity.getDeltaMovement(); // Internal minecraft reference, no extra allocation (yay)
            entity.setDeltaMovement(
                entityVelocity.add(
                    Math.clamp(accelerationScalar * cache.thrusterDirection.x, -MAX_ACCELERATION, MAX_ACCELERATION),
                    Math.clamp(accelerationScalar * cache.thrusterDirection.y, -MAX_ACCELERATION, MAX_ACCELERATION),
                    Math.clamp(accelerationScalar * cache.thrusterDirection.z, -MAX_ACCELERATION, MAX_ACCELERATION)
                )
            );
            entity.fallDistance = 0.0f;

            // Sync client-side (player) motion
            if (entity instanceof ServerPlayer serverPlayer) serverPlayer.hurtMarked = true;

            // Handle damage effect
            float appliedDamage = (float) (accelerationScalar * DAMAGE_MULTIPLIER);
            if (appliedDamage < 0.5d) continue;
            if (thrusterDamageSource == null) thrusterDamageSource = ModDamageTypes.getSource(serverLevel, ModDamageTypes.PROPULSITE_THRUSTER);
            entity.hurt(thrusterDamageSource, appliedDamage);
        }
    }

    private void addChargingParticles(ServerLevel serverLevel, Cache cache) {
        // Compute each particle
        for (int i = 0; i < NUM_PARTICLES; i++) {
            // Get initial speeds: a*PARTICLE_RADIUS, where a ∈ [-1, 1)
            double xSpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;
            double ySpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;
            double zSpeed = (serverLevel.random.nextDouble() - 0.5d) * 2.0d * PARTICLE_RADIUS;

            // Handle motion
            cache.spawnPosition.set(cache.thrusterFace).fma(i, cache.thrusterVelocity);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticles.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(),
                cache.spawnPosition.x, cache.spawnPosition.y, cache.spawnPosition.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                xSpeed, ySpeed, zSpeed,
                1.0d // Use above speed values
            );
        }
    }

    private void addFiringParticles(ServerLevel serverLevel, Cache cache) {
        // Get starting values
        double maxThrust = amplitude / NORM_DENOMINATOR;
        double thrustRatio = Math.max(0.0d, thrust / maxThrust); // [0.0 to 1.0] multiplier based on current thrust strength
        double baseVelocity = MIN_PARTICLE_SPEED + ((MAX_PARTICLE_SPEED - MIN_PARTICLE_SPEED) * thrustRatio); // Faster jet at peak thrust
        int particleCount = MIN_PARTICLES + (int) ((MAX_PARTICLES - MIN_PARTICLES) * thrustRatio);

        // Compute each particle
        for (int i = 0; i < particleCount; i++) {
            // Apply slight random spread to the cone of the thrust
            double dirX = cache.thrusterDirection.x + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);
            double dirY = cache.thrusterDirection.y + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);
            double dirZ = cache.thrusterDirection.z + (serverLevel.random.nextGaussian() * PARTICLE_SPREAD);

            // Normalize the direction and scale by baseVelocity
            cache.spawnVelocity.set(dirX, dirY, dirZ).normalize().mul(baseVelocity);

            // By setting count to 0, xOffset, yOffset, and zOffset act as xSpeed, ySpeed, and zSpeed
            serverLevel.sendParticles(
                ModParticles.PROPULSITE_THRUSTER_FIRING_PARTICLES.get(),
                cache.thrusterFace.x, cache.thrusterFace.y, cache.thrusterFace.z,
                0, // Count = 0 (Crucial for passing custom payloads)
                cache.spawnVelocity.x, cache.spawnVelocity.y, cache.spawnVelocity.z,
                1.0d // Use spawnVelocity values
            );
        }
    }


    // LONGS
    @SuppressWarnings({"DuplicateCondition", "ConstantValue"})
    public void defaultUpdateLongs(ServerLevel serverLevel) {
        if (!(Sable.HELPER.getContaining(serverLevel, worldPosition) instanceof ServerSubLevel)) return;

        // Cooldown
        if (cooldown > 0) {
            cooldown = 0;
        }

        // Charging
        else if (!armed) {
            charge = MAX_CHARGE;
            armed = true;
            serverLevel.playSound(
                null, worldPosition,
                SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS,
                1.5f,1.2f
            );
        }

        // Firing
        else if (armed) {
            firing = true;
            firingTick = 0;
            serverLevel.playSound(
                null, worldPosition,
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.BLOCKS,
                1.5f,1.0f
            );
        }

        // Send update packet
        else return;
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
            .text(armed ? "Armed" : "Disarmed")
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.armed_state", armedState)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent maximumThrust = CCLangHelper
            .pixelNewton(amplitude / NORM_DENOMINATOR)
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Save data to the network sync packet
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Charge", this.charge);
        tag.putInt("Cooldown", this.cooldown);
        tag.putDouble("Thrust", this.thrust);
        tag.putDouble("Amplitude", this.amplitude);
        tag.putBoolean("Armed", this.armed);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        // Handle receiving the packet on the Client side
        CompoundTag tag = pkt.getTag();
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
        this.thrust = tag.getDouble("Thrust");
        this.amplitude = tag.getDouble("Amplitude");
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
        tag.putInt("FiringTick", this.firingTick);
        tag.putDouble("Thrust", this.thrust);
        tag.putDouble("Amplitude", this.amplitude);
        tag.putBoolean("Armed", this.armed);
        tag.putBoolean("Firing", this.firing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.charge = tag.getInt("Charge");
        this.cooldown = tag.getInt("Cooldown");
        this.firingTick = tag.getInt("FiringTick");
        this.thrust = tag.getDouble("Thrust");
        this.amplitude = tag.getDouble("Amplitude");
        this.armed = tag.getBoolean("Armed");
        this.firing = tag.getBoolean("Firing");
    }
}
