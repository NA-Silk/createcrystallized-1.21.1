package com.nasilk.createcrystallized.fluid.flowingfluid;

import com.nasilk.createcrystallized.event.TaskEventScheduler;
import com.nasilk.createcrystallized.util.setting.FluidTransformSettings;
import com.nasilk.createcrystallized.util.type.FluidTransformTriggerType;
import com.simibubi.create.foundation.utility.BlockHelper;
import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import java.util.List;

public abstract class TransformBaseFlowingFluid extends BaseFlowingFluid {
    private final List<FluidTransformSettings> settingsList;

    protected TransformBaseFlowingFluid(Properties properties, List<FluidTransformSettings> settingsList) {
        super(properties);
        this.settingsList = settingsList;
    }

    public List<FluidTransformSettings> getSettingsList() {
        return this.settingsList;
    }

    // BEHAVIOR OVERRIDES
    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        super.randomTick(level, pos, state, random);
        if (level instanceof ServerLevel serverLevel) {
            tryTransform(serverLevel, pos, state, FluidTransformTriggerType.RANDOM_TICK);
        }
    }

    // TOOLS
    public void tryTransform(ServerLevel serverLevel, BlockPos pos, FluidState state, FluidTransformTriggerType trigger) {
        // Loop through settings
        for (FluidTransformSettings settings : this.settingsList) {
            // Check transformation conditions
            if (settings.canTransform(serverLevel, pos, state, trigger)) {
                performTransformation(serverLevel, pos, settings);
                return;
            }
        }
    }

    public void tryEventTransform(ServerLevel serverLevel, BlockPos pos, FluidState state, FluidTransformTriggerType trigger, FluidTransformSettings settings) {
        // Check transformation conditions
        if (settings.canTransform(serverLevel, pos, state, trigger)) {
            performTransformation(serverLevel, pos, settings);
        }
    }

    private void performTransformation(ServerLevel level, BlockPos pos, FluidTransformSettings settings) {
        // Verify current state
        FluidState currentState = level.getFluidState(pos);
        if (!currentState.is(this) || !currentState.isSource()) return;

        // Transform block
        level.setBlockAndUpdate(pos, settings.transformBlock().get().defaultBlockState());

        // Break cast blocks (Levitite consistency)
        CAST:
        for (Direction castDirection : Direction.values()) {
            // Get cast block and validate tag
            BlockPos castPos = pos.relative(castDirection);
            if (!level.getBlockState(castPos).is(AeroTags.BlockTags.LEVITITE_BREAKABLE)) continue;

            // Skip current iteration if fluid would flow
            for (Direction freeFluidPos : Direction.values()) {
                if (level.getFluidState(castPos.relative(freeFluidPos)).is(this)) continue CAST;
            }

            // Break cast block
            BlockHelper.destroyBlock(level, castPos, 1.0f);
        }

        // Play effects
        settings.transformParticle().ifPresent(
            particle -> level.sendParticles(
                particle.get(),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                8,0.5,0.5,0.5,0.5
            )
        );
        settings.transformSound().ifPresent(
            sound -> level.playSound(
                null, pos,
                sound.get(), SoundSource.BLOCKS,
                1.0f, 1.0f
            )
        );

        // Tell nearby same-type fluids to begin catalyzing
        if (settings.chainCatalyzes()) {
            for (Direction direction : Direction.values()) {
                BlockPos targetPos = pos.relative(direction);
                FluidState targetState = level.getFluidState(targetPos);

                // Check if the nearby fluid is valid and apply catalysis on a later tick
                if (!targetState.isEmpty() && targetState.isSource() && targetState.is(this)) {
                    TaskEventScheduler.schedule(
                        level.getServer(),
                        level.random.nextInt(20,200),
                        () -> {
                            if (level.isLoaded(targetPos)) performTransformation(level, targetPos, settings);
                        }
                    );
                }
            }
        }
    }

    // INNER CLASSES
    public static class Flowing extends TransformBaseFlowingFluid {
        public Flowing(Properties properties, List<FluidTransformSettings> settingsList) { super(properties, settingsList); }
        @Override public boolean isSource(FluidState state) { return false; }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
    }

    public static class Source extends TransformBaseFlowingFluid {
        public Source(Properties properties, List<FluidTransformSettings> settingsList) { super(properties, settingsList); }
        @Override public boolean isSource(FluidState state) { return true; }
        @Override public int getAmount(FluidState state) { return 8; }
    }
}
