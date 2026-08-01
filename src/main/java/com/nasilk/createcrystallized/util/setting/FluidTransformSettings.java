package com.nasilk.createcrystallized.util.setting;

import com.nasilk.createcrystallized.util.type.FluidTransformTriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Fluid Transformation Settings
 * - Allows more convenient fluid settings

 * @param transformRate                 Random transformation chance per tick
 * @param maxSkyLight                   Maximum skylight level allowed
 * @param yRange                        Height restrictions
 * @param requireColdBiome              Biome requirements
 * @param requireRaining                Weather requirements
 * @param requireThundering             Weather requirements
 * @param requireNight                  Time requirements
 * @param requireSourceBlock            State requirements
 * @param requireAdjacentBlocks         Neighbor requirements
 * @param allowedDimensions             Allowed transformation dimensions
 * @param lightningSettings             Lightning requirements
 * @param vibrationSettings             Vibration requirements
 * @param transformParticle             Optional transformation particle
 * @param transformSound                Optional transformation sound
 * @param chainCatalyzes                Transforms adjacent same-fluid blocks

 * Frequency Table
 * Event:                               Frequency:
 * Walking	                            1 (Not supported)
 * Projectile impact	                2 (Not supported)
 * Elytra	                            4
 * Damage	                            7
 * Doors	                            10
 * Block break	                        12
 * Block place	                        13
 * Explosion	                        15
 */
public record FluidTransformSettings(
    Supplier<Block> transformBlock,
    float transformRate,
    int maxSkyLight,
    YRange yRange,
    boolean requireColdBiome,
    boolean requireRaining,
    boolean requireThundering,
    boolean requireNight,
    boolean requireSourceBlock,
    Set<Supplier<Block>> requireAdjacentBlocks,
    Set<ResourceKey<Level>> allowedDimensions,
    LightningSettings lightningSettings,
    VibrationSettings vibrationSettings,
    Optional<Supplier<SimpleParticleType>> transformParticle,
    Optional<Supplier<SoundEvent>> transformSound,
    boolean chainCatalyzes
) {
    public record YRange(
        int minYLevel,
        int maxYLevel
    ) {}

    public record LightningSettings(
        boolean requireLightning,
        Integer radius
    ) {}

    public record VibrationSettings(
        boolean requireVibration,
        Integer radius,
        Integer minimumFrequency
    ) {}

    public boolean canTransform(ServerLevel serverLevel, BlockPos pos, FluidState state, FluidTransformTriggerType trigger) {
        return passesRandomCheck(serverLevel)
            && passesEnvironmentChecks(serverLevel, pos)
            && passesContextChecks(serverLevel, pos, state)
            && passesTriggerChecks(trigger);
    }

    private boolean passesRandomCheck(ServerLevel serverLevel) {
        return serverLevel.getRandom().nextFloat() <= transformRate;
    }

    private boolean passesEnvironmentChecks(ServerLevel serverLevel, BlockPos pos) {
        return (!requireColdBiome || serverLevel.getBiome(pos).value().coldEnoughToSnow(pos))
            && (!requireRaining || serverLevel.isRaining())
            && (!requireThundering || serverLevel.isThundering())
            && (!requireNight || serverLevel.isNight())
            && (allowedDimensions.isEmpty() || allowedDimensions.contains(serverLevel.dimension()));
    }

    private boolean passesContextChecks(ServerLevel serverLevel, BlockPos pos, FluidState state) {
        return (serverLevel.getBrightness(LightLayer.SKY, pos) <= maxSkyLight)
            && (pos.getY() >= yRange.minYLevel() && pos.getY() <= yRange.maxYLevel())
            && (!requireSourceBlock || state.isSource())
            && (requireAdjacentBlocks.isEmpty() || hasAdjacentBlocks(serverLevel, pos));

    }

    private boolean passesTriggerChecks(FluidTransformTriggerType trigger) {
        return (!lightningSettings.requireLightning() || trigger == FluidTransformTriggerType.LIGHTNING)
            && (!vibrationSettings.requireVibration() || trigger == FluidTransformTriggerType.VIBRATION || trigger == FluidTransformTriggerType.LIGHTNING);
    }

    private boolean hasAdjacentBlocks(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            Block adjacentBlock = level.getBlockState(pos.relative(direction)).getBlock();

            for (Supplier<Block> blockSupplier : requireAdjacentBlocks) {
                if (adjacentBlock == blockSupplier.get()) return true;
            }
        }
        return false;
    }
}
