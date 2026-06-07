package com.nasilk.createcrystallized.fluid.flowingfluid;

import com.nasilk.createcrystallized.util.FluidTransformationSettings;
import com.nasilk.createcrystallized.util.FluidTransformationTriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import java.util.function.Supplier;

public abstract class TransformBaseFlowingFluid extends BaseFlowingFluid {
    private final Supplier<Block> transformBlock;
    private final FluidTransformationSettings settings;

    protected TransformBaseFlowingFluid(
        Properties properties,
        Supplier<Block> transformBlock,
        FluidTransformationSettings settings
    ) {
        super(properties);
        this.transformBlock = transformBlock;
        this.settings = settings;
    }

    public FluidTransformationSettings getSettings() {
        return this.settings;
    }

    // BEHAVIOR OVERRIDES
    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        super.randomTick(level, pos, state, random);
        tryTransform(level, pos, state, FluidTransformationTriggerType.RANDOM_TICK);
    }

    // TOOLS
    public void tryTransform(Level level, BlockPos pos, FluidState state, FluidTransformationTriggerType trigger) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getRandom().nextFloat() > settings.transformRate()) return;

        // Skylight requirement
        if (serverLevel.getBrightness(LightLayer.SKY, pos) > settings.maxSkyLight()) return;
        // Height restrictions
        if (pos.getY() < settings.yRange().minYLevel() || pos.getY() > settings.yRange().maxYLevel()) return;
        // Cold biome requirement
        if (settings.requireColdBiome() && !serverLevel.getBiome(pos).value().coldEnoughToSnow(pos)) return;
        // Rain requirement
        if (settings.requireRaining() && !serverLevel.isRaining()) return;
        // Thunder requirement
        if (settings.requireThundering() && !serverLevel.isThundering()) return;
        // Night requirement
        if (settings.requireNight() && serverLevel.isDay()) return;
        // Adjacent blocks requirement
        if (!settings.requireAdjacentBlocks().isEmpty() && !hasAdjacentBlocks(serverLevel, pos)) return;
        // Trigger Constraints
        if (settings.lightningSettings().requireLightning() && trigger != FluidTransformationTriggerType.LIGHTNING) return;
        if (settings.vibrationSettings().requireVibration() && trigger != FluidTransformationTriggerType.VIBRATION && trigger != FluidTransformationTriggerType.LIGHTNING) return;
        // Source-only restriction
        if (!settings.transformFlowingFluids() && !state.isSource()) return;
        // Vaporize in ultrawarm dimensions
        if (settings.vaporizeInUltraWarmDimension() && serverLevel.dimensionType().ultraWarm()) {
            serverLevel.removeBlock(pos, false);
            return;
        }
        // Allowed dimensions
        if (!settings.allowedDimensions().isEmpty() && !settings.allowedDimensions().contains(serverLevel.dimension())) return;

        // Transform
        performTransformation(serverLevel, pos);
    }

    private boolean hasAdjacentBlocks(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            Block adjacentBlock = level.getBlockState(pos.relative(direction)).getBlock();

            for (Supplier<Block> blockSupplier : settings.requireAdjacentBlocks()) {
                if (adjacentBlock == blockSupplier.get()) return true;
            }
        }
        return false;
    }

    private void performTransformation(Level serverLevel, BlockPos pos) {
        // Transform block
        serverLevel.setBlockAndUpdate(pos, transformBlock.get().defaultBlockState());

        // Play sound
        settings.transformSound().ifPresent(
            sound -> serverLevel.playSound(
                null,
                pos,
                sound.get(),
                SoundSource.BLOCKS,
                1.0f,
                1.0f
            )
        );
    }


    // INNER CLASSES
    public static class Flowing extends TransformBaseFlowingFluid {
        public Flowing(Properties properties, Supplier<Block> transformBlock, FluidTransformationSettings settings) { super(properties, transformBlock, settings); }
        @Override public boolean isSource(FluidState state) { return false; }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
    }

    public static class Source extends TransformBaseFlowingFluid {
        public Source(Properties properties, Supplier<Block> transformBlock, FluidTransformationSettings settings) { super(properties, transformBlock, settings); }
        @Override public boolean isSource(FluidState state) { return true; }
        @Override public int getAmount(FluidState state) { return 8; }
    }
}
