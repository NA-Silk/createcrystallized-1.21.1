package com.nasilk.createcrystallized.util.setting;

import com.nasilk.createcrystallized.util.type.FluidTransformationTriggerType;
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
 * @param requireColdBiome              Environmental requirements
 * @param requireRaining
 * @param requireThundering
 * @param requireNight
 * @param requireSourceBlock
 * @param requireAdjacentBlocks         Neighbor requirements
 * @param lightningSettings             Lightning requirements
 * @param vibrationSettings             Vibration requirements
 * @param allowedDimensions             Allowed transformation dimensions
 * @param transformParticle             Optional transformation particle
 * @param transformSound                Optional transformation sound
 * @param chainCatalyzes                Transforms adjacent same-fluid blocks

 * Frequency Table
 * Event:                               Frequency:
 * Walking	                            1
 * Projectile impact	                2
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
    LightningSettings lightningSettings,
    VibrationSettings vibrationSettings,
    Set<ResourceKey<Level>> allowedDimensions,
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

    public boolean canTransform(ServerLevel serverLevel, BlockPos pos, FluidState state, FluidTransformationTriggerType trigger) {
        // Random rate
        if (serverLevel.getRandom().nextFloat() > transformRate) return false;

        // Skylight requirement
        if (serverLevel.getBrightness(LightLayer.SKY, pos) > maxSkyLight) return false;

        // Height restrictions
        if (pos.getY() < yRange.minYLevel() || pos.getY() > yRange.maxYLevel()) return false;

        // Cold biome requirement
        if (requireColdBiome && !serverLevel.getBiome(pos).value().coldEnoughToSnow(pos)) return false;

        // Rain requirement
        if (requireRaining && !serverLevel.isRaining()) return false;

        // Thunder requirement
        if (requireThundering && !serverLevel.isThundering()) return false;

        // Night requirement
        if (requireNight && serverLevel.isDay()) return false;

        // Source-only restriction
        if (requireSourceBlock && !state.isSource()) return false;

        // Adjacent blocks requirement
        if (!requireAdjacentBlocks.isEmpty() && !hasAdjacentBlocks(serverLevel, pos)) return false;

        // Trigger Constraints
        if (lightningSettings.requireLightning() && trigger != FluidTransformationTriggerType.LIGHTNING) return false;
        if (vibrationSettings.requireVibration() && trigger != FluidTransformationTriggerType.VIBRATION && trigger != FluidTransformationTriggerType.LIGHTNING) return false;

        // Allowed dimensions
        if (!allowedDimensions.isEmpty() && !allowedDimensions.contains(serverLevel.dimension())) return false;

        // Validate transformation
        return true;
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
