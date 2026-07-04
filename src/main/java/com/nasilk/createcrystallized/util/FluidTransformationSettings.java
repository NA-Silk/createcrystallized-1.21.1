package com.nasilk.createcrystallized.util;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
public record FluidTransformationSettings(
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
}
