package com.nasilk.createfluidsandfixins.fluid.flowingfluid;

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
 */
public class FluidTransformationSettings {
    // Random transformation chance per tick
    public final float transformRate;

    // Maximum skylight level allowed
    public final int maxSkyLight;

    // Height restrictions
    public final int minYLevel;
    public final int maxYLevel;

    // Environmental requirements
    public final boolean requireColdBiome;
    public final boolean requireRaining;
    public final boolean requireThundering;
    public final boolean requireNight;

    // Neighbor requirements
    public final Set<Supplier<Block>> requireAdjacentBlocks;

    // Whether flowing fluid blocks may transform
    public final boolean transformFlowingFluids;

    // Nether-style vaporization support
    public final boolean vaporizeInUltraWarmDimension;

    // Allowed dimensions
    public final Set<ResourceKey<Level>> allowedDimensions;

    // Optional transformation sound
    public final Optional<Supplier<SoundEvent>> transformSound;

    public FluidTransformationSettings(
            float transformRate,
            int maxSkyLight,
            int minYLevel,
            int maxYLevel,
            boolean requireColdBiome,
            boolean requireRaining,
            boolean requireThundering,
            boolean requireNight,
            Set<Supplier<Block>> requireAdjacentBlocks,
            boolean transformFlowingFluids,
            boolean vaporizeInUltraWarmDimension,
            Set<ResourceKey<Level>> allowedDimensions,
            Optional<Supplier<SoundEvent>> transformSound
    ) {
        this.transformRate = transformRate;
        this.maxSkyLight = maxSkyLight;
        this.minYLevel = minYLevel;
        this.maxYLevel = maxYLevel;
        this.requireColdBiome = requireColdBiome;
        this.requireRaining = requireRaining;
        this.requireThundering = requireThundering;
        this.requireNight = requireNight;
        this.requireAdjacentBlocks = requireAdjacentBlocks;
        this.transformFlowingFluids = transformFlowingFluids;
        this.vaporizeInUltraWarmDimension = vaporizeInUltraWarmDimension;
        this.allowedDimensions = allowedDimensions;
        this.transformSound = transformSound;
    }
}
