package com.nasilk.createcrystallized.block.multiblock.imp;

import com.nasilk.createcrystallized.block.multiblock.AbstractMultiblockEntity;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.server.block.WellConfig;
import com.nasilk.createcrystallized.util.helper.CCLangHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import java.util.ArrayList;
import java.util.List;

// They grow up so fast!
public class DensiteWellMultiblockEntity extends AbstractMultiblockEntity implements IHaveGoggleInformation {
    // Tick variables (saved)
    private int power = 0;
    private int size = getMultiblockSize().getSize() - 1;

    // Tick variables (unsaved)
    private final List<ServerSubLevel> contactTargets = new ArrayList<>();
    private final List<ServerSubLevel> fieldTargets = new ArrayList<>();

    // Tick constants
    private static final int RANDOM_TICK_RATE = 20;
    private static final double AMBIENT_RATE = 8e-5d;
    private static final double DAMPEN_SCALE = 0.2d;
    private static final double BASE_CONTACT_RADIUS = 0.5 * DensiteWellMultiblockBlock.getBaseShape().bounds().inflate(2.0d/16.0d).getSize();
    private static final double BASE_DAMPEN_RADIUS = 2.0d;
    private static final Vector3dc ZERO_VECTOR = new Vector3d(0.0d, 0.0d, 0.0d); // Read-only reference

    // Particle constants
    private static final int PULL_PARTICLE_TICK_RATE = 30;
    private static final int[] PULL_PARTICLE_COUNT = {0, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5};

    // Tick constants (config)
    private static final double[][] FIELD_STRENGTH_CURVE = new double[3][16];
    private static final double[][] FIELD_RADIUS_CURVE = new double[3][16];
    private static final double[][] FIELD_RADIUS_SQUARED_CURVE = new double[3][16];
    private static final double[]   CONTACT_RADIUS_CURVE = new double[3];
    private static final double[]   DAMPEN_RADIUS_SQUARED_CURVE = new double[3];

    // Particle constants (config)
    private static double PULL_PARTICLE_TRAVEL_TICKS;

    // Config constants
    public static void updateConstants() {
        WellConfig wellConfig = ModConfigs.server().blockConfig.wellConfig;
        double STRENGTH_SCALE = wellConfig.wellStrengthScale.get();
        double RADIUS_SCALE = wellConfig.wellRadiusScale.get();
        double DAMPEN_RADIUS_SQUARED = BASE_DAMPEN_RADIUS * BASE_DAMPEN_RADIUS;
        for (int i = 0; i < 3; i++) {
            int s1 = i + 1;
            int s2 = s1 * s1;
            CONTACT_RADIUS_CURVE[i] = BASE_CONTACT_RADIUS * s1;
            DAMPEN_RADIUS_SQUARED_CURVE[i] = DAMPEN_RADIUS_SQUARED * s2;
            for (int j = 0; j < 16; j++) {
                FIELD_STRENGTH_CURVE[i][j] = STRENGTH_SCALE * j * s2;
                FIELD_RADIUS_CURVE[i][j] = RADIUS_SCALE * j * s1;
                FIELD_RADIUS_SQUARED_CURVE[i][j] = FIELD_RADIUS_CURVE[i][j] * FIELD_RADIUS_CURVE[i][j];
            }
        }
        PULL_PARTICLE_TRAVEL_TICKS = ModConfigs.client().particleConfig.wellParticleTravelTicks.get();
    }

    // Cache
    private static class Cache {
        final BoundingBox3d searchBox = new BoundingBox3d();
        final Vector3d wellPosition = new Vector3d();
        final Vector3d targetPosition = new Vector3d();
        final Vector3d impulseVelocity = new Vector3d();
        final Vector3d currentLinearVelocity = new Vector3d();
        final Vector3d currentAngularVelocity = new Vector3d();
    }
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public DensiteWellMultiblockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    // CUSTOM TICKS
    @Override
    protected void serverTick(ServerLevel level) {
        // Only controllers tick
        if (!isController()) return;

        // Have a little ambiance
        if (level.getRandom().nextDouble() < AMBIENT_RATE) level.playSound(
            null, worldPosition,
            SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS, SoundSource.BLOCKS,
            1.0f,0.8f
        );

        // Get block power
        BlockState state = getBlockState();
        int newPower = Math.clamp(state.getValue(DensiteWellMultiblockBlock.POWER), 0, 15);
        boolean powerChanged = power != newPower;
        if (powerChanged) {
            power = newPower;
            if (power == 0) {
                contactTargets.clear();
                fieldTargets.clear();
                return;
            }
        }


        // Get global position
        Cache cache = CACHE.get();
        double center = 0.5d * (double) getMultiblockSize().getSize();
        cache.wellPosition.set(worldPosition.getX() + center, worldPosition.getY() + center, worldPosition.getZ() + center);
        ServerSubLevel wellSubLevel = null;
        if (Sable.HELPER.getContaining(level, worldPosition) instanceof ServerSubLevel subLevel) {
            wellSubLevel = subLevel;
            subLevel.logicalPose().transformPosition(cache.wellPosition);
        }

        // Get size-dependent constants
        int newSize = getMultiblockSize().getSize() - 1;
        boolean sizeChanged = size != newSize;
        if (sizeChanged) size = newSize;

        // Run effects
        int randomTickModulo = Math.floorMod(level.getGameTime() + worldPositionHash, RANDOM_TICK_RATE);
        if (powerChanged || sizeChanged) {
            updateContactTargets(level, wellSubLevel, cache);
            updateFieldTargets(level, wellSubLevel, cache);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            this.setChanged();
        }
        else if (randomTickModulo == 0) updateContactTargets(level, wellSubLevel, cache);
        else if (randomTickModulo == 1) updateFieldTargets(level, wellSubLevel, cache);
        if (!contactTargets.isEmpty()) applyContactGravity(cache);
        if (!fieldTargets.isEmpty()) applyFieldGravity(cache);
        if ((level.getGameTime() + worldPositionHash) % PULL_PARTICLE_TICK_RATE == 0) addPullParticles(level, cache);
    }


    /// CONTACT
    private void updateContactTargets(ServerLevel level, ServerSubLevel wellSubLevel, Cache cache) {
        // Reset the target list
        contactTargets.clear();

        // Get Sable's SubLevel container for this dimension
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return;

        // Set the bounding box
        double contactRadius = CONTACT_RADIUS_CURVE[size];
        cache.searchBox.setUnchecked(
            cache.wellPosition.x - contactRadius, cache.wellPosition.y - contactRadius, cache.wellPosition.z - contactRadius,
            cache.wellPosition.x + contactRadius, cache.wellPosition.y + contactRadius, cache.wellPosition.z + contactRadius
        );

        // Populate the target list
        container.queryIntersecting(cache.searchBox).forEach(subLevel -> {
            if (subLevel instanceof ServerSubLevel targetSubLevel && targetSubLevel != wellSubLevel) contactTargets.add(targetSubLevel);
        });
    }

    private void applyContactGravity(Cache cache) {
        // Iterate backwards for safe removals
        for (int i = contactTargets.size() - 1; i >= 0; i--) {
            ServerSubLevel targetSubLevel = contactTargets.get(i);

            // If the sublevel was destroyed or unloaded, remove it from the list
            if (targetSubLevel.isRemoved()) {
                contactTargets.remove(i);
                continue;
            }

            // Get the physics handle
            RigidBodyHandle handle = RigidBodyHandle.of(targetSubLevel);
            if (!handle.isValid()) {
                contactTargets.remove(i);
                continue;
            }

            // Get current linear and angular velocity
            handle.getLinearVelocity(cache.currentLinearVelocity);
            handle.getAngularVelocity(cache.currentAngularVelocity);

            // Apply opposite vectors to negate current motion
            cache.currentLinearVelocity.negate();
            cache.currentAngularVelocity.mul(-DAMPEN_SCALE);
            handle.addLinearAndAngularVelocity(cache.currentLinearVelocity, cache.currentAngularVelocity);
        }
    }


    /// FIELD
    private void updateFieldTargets(ServerLevel level, ServerSubLevel wellSubLevel, Cache cache) {
        // Reset the target list
        fieldTargets.clear();

        // Get Sable's SubLevel container for this dimension
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return;

        // Set the bounding box
        double fieldRadius = FIELD_RADIUS_CURVE[size][power];
        cache.searchBox.setUnchecked(
            cache.wellPosition.x - fieldRadius, cache.wellPosition.y - fieldRadius, cache.wellPosition.z - fieldRadius,
            cache.wellPosition.x + fieldRadius, cache.wellPosition.y + fieldRadius, cache.wellPosition.z + fieldRadius
        );

        // Populate the target list
        container.queryIntersecting(cache.searchBox).forEach(subLevel -> {
            if (subLevel instanceof ServerSubLevel targetSubLevel && targetSubLevel != wellSubLevel && !contactTargets.contains(targetSubLevel))
                fieldTargets.add(targetSubLevel);
        });
    }

    private void applyFieldGravity(Cache cache) {
        double fieldRadiusSquared = FIELD_RADIUS_SQUARED_CURVE[size][power];
        double dampenRadiusSquared = DAMPEN_RADIUS_SQUARED_CURVE[size];
        double fieldStrength = FIELD_STRENGTH_CURVE[size][power];

        // Iterate backwards for safe removals
        for (int i = fieldTargets.size() - 1; i >= 0; i--) {
            ServerSubLevel targetSubLevel = fieldTargets.get(i);

            // If the sublevel was destroyed or unloaded, remove it from the list
            if (targetSubLevel.isRemoved()) {
                fieldTargets.remove(i);
                continue;
            }

            // Get the physics handle
            RigidBodyHandle handle = RigidBodyHandle.of(targetSubLevel);
            if (!handle.isValid()) {
                fieldTargets.remove(i);
                continue;
            }

            // Get sublevel position, impulse velocity, and current distance^2
            cache.targetPosition.set(targetSubLevel.logicalPose().position());
            cache.impulseVelocity.set(cache.wellPosition).sub(cache.targetPosition);
            double distanceSquared = cache.impulseVelocity.lengthSquared();

            // Handle out of range sublevels
            if (distanceSquared > fieldRadiusSquared) {
                fieldTargets.remove(i);
                continue;
            }

            // Handle dampened and standard linear impulse (inverse square impulse feels choppy)
            if (distanceSquared < dampenRadiusSquared) {
                // Apply dampening when within well radius
                handle.getLinearVelocity(cache.currentLinearVelocity);
                cache.currentLinearVelocity.mul(-DAMPEN_SCALE);
                handle.addLinearAndAngularVelocity(cache.currentLinearVelocity, ZERO_VECTOR);

                // Handle reduced pull impulse: F = fieldStrength * distance / RADIUS^2
                cache.impulseVelocity.mul(fieldStrength / dampenRadiusSquared);
            } else {
                // Handle standard pull impulse: F = fieldStrength * distance / distance^2 = fieldStrength / distance
                cache.impulseVelocity.mul(fieldStrength / distanceSquared);
            }

            // Apply rotation transformed impulse
            targetSubLevel.logicalPose().orientation().transformInverse(cache.impulseVelocity);
            handle.applyLinearImpulse(cache.impulseVelocity);
        }
    }


    // PARTICLES
    private void addPullParticles(ServerLevel level, Cache cache) {
        // Compute each particle
        for (int i = 0; i < PULL_PARTICLE_COUNT[power]; i++) {
            // Uniformly sample a direction on a sphere.
            double yUnit = level.random.nextDouble() * 2.0d - 1.0d; // yUnit ∈ [-1.0, 1.0)
            double azimuth = level.random.nextDouble() * (Math.PI * 2.0d); // Ooh, vocab! So this is the circumferential angle, cool name right? azimuth ∈ [0.0, 2.0*PI)
            double xzComp = Math.sqrt(Math.max(0.0d, 1.0d - yUnit * yUnit)); // Get x & z component along the azimuth, basically a partial radius
            double xUnit = xzComp * Math.cos(azimuth);
            double zUnit = xzComp * Math.sin(azimuth);

            // Spawn within the outer 45% of the field radius
            double distance = FIELD_RADIUS_CURVE[size][power] * (0.55d + 0.45d * level.random.nextDouble()); // distance = fieldRadius * s : s ∈ [0.55, 1.0)
            double x = cache.wellPosition.x + xUnit * distance;
            double y = cache.wellPosition.y + yUnit * distance;
            double z = cache.wellPosition.z + zUnit * distance;

            // Scale speed with the distance from the well
            double speed = distance / PULL_PARTICLE_TRAVEL_TICKS;

            // By setting particleCount to 0, xOffset, yOffset, and zOffset act as speed
            level.sendParticles(
                ModParticleTypes.DENSITE_WELL_PULL_PARTICLES.get(),
                x, y, z,
                0,
                -xUnit * speed, -yUnit * speed, -zUnit * speed,
                1.0d
            );
        }
    }


    // GOGGLE TOOLTIPS
    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        if (!isController()) return false;
        CCLangHelper.blockName(this.getBlockState()).text(":").forGoggles(tooltip);

        final MutableComponent currentFieldStrength = CCLangHelper
            .pixelNewton(FIELD_STRENGTH_CURVE[size][power])
            .style(ChatFormatting.AQUA)
            .component();
        CCLangHelper.translate("goggles.field_strength", currentFieldStrength)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip, 1);

        final MutableComponent currentFieldRadius = CCLangHelper
            .meter(FIELD_RADIUS_CURVE[size][power])
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
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Power", this.power);
        tag.putInt("Size", this.size);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        CompoundTag tag = pkt.getTag();
        this.power = tag.getInt("Power");
        this.size = tag.getInt("Size");
    }


    // DATA PERSISTENCE
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Power", this.power);
        tag.putInt("Size", this.size);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getInt("Power");
        this.size = tag.getInt("Size");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.contactTargets.clear();
        this.fieldTargets.clear();
    }


    // GETTERS
    public boolean isPowered()     { return power > 0;                       }
    public int getPower()          { return power;                           }
    public double getFieldRadius() { return FIELD_RADIUS_CURVE[size][power]; }
}
