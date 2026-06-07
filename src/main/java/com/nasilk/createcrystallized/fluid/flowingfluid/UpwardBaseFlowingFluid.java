package com.nasilk.createcrystallized.fluid.flowingfluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class UpwardBaseFlowingFluid extends BaseFlowingFluid {
    public int tickRate = 8, flowLife = 2;
    public double timeFactor = 0.1, yFactor = 0.2, threshold = 0.8;
    public float flowingBlockHeight = 0.88f;

    protected UpwardBaseFlowingFluid(Properties properties) {
        super(properties);
    }

    public BaseFlowingFluid setFlowAnimationOptions(
        int tickRate,
        int flowLife,
        double timeFactor,
        double yFactor,
        double threshold,
        float flowingBlockHeight
    ) {
        this.tickRate = tickRate;
        this.flowLife = flowLife;
        this.timeFactor = timeFactor;
        this.yFactor = yFactor;
        this.threshold = threshold;
        this.flowingBlockHeight = flowingBlockHeight;
        return this;
    }

    // BEHAVIOR OVERRIDES
    /**
     * Overriding tick without calling super.tick() prevents the vanilla fluid engine from ever touching this fluid.
     * This allows fully custom behavior at the expense of complexity.
     */
    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        if (level.isClientSide) return;

        // 1. Death Check (prevent the Death Balloon)
        if (!state.isSource() && state.hasProperty(FALLING) && state.getValue(FALLING)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }

        // 2. Cleanup & Height Limit
        int amount = state.getAmount();
        if (amount <= 1 || pos.getY() >= level.getMaxBuildHeight() - 1) {
            if (!state.isSource()) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }

        // 3. Target Calculation
        long time = level.getGameTime();
        double wave = (time * timeFactor) - (pos.getY() * yFactor);
        int offsetX = (Math.sin(wave) > threshold) ? 1 : (Math.sin(wave) < -threshold ? -1 : 0);
        int offsetZ = (Math.cos(wave) > threshold) ? 1 : (Math.cos(wave) < -threshold ? -1 : 0);
        BlockPos targetPos = pos.above().offset(offsetX, 0, offsetZ);

        // 4. Propagation
        BlockState targetState = level.getBlockState(targetPos);
        FluidState targetFluid = targetState.getFluidState();

        // Allow overwriting if AIR, or if it's a dying piece of the SAME fluid
        boolean isDyingSegment = targetFluid.getType().isSame(this) && targetFluid.hasProperty(FALLING) && targetFluid.getValue(FALLING);
        if (targetState.canBeReplaced() || isDyingSegment) {
            // nextAmount = 7 if isSource, else amount || amount - 1 if flowing (dependent on y-level)
            int nextAmount = state.isSource() ? 7 : ((pos.getY() % flowLife == 0) ? amount : amount - 1);
            //noinspection ConstantValue
            if (nextAmount > 0) {
                // Spawn the next piece (FALLING = false by default)
                level.setBlock(targetPos, this.getFlowing(nextAmount, false).createLegacyBlock(), 2);
                level.scheduleTick(targetPos, this, 6);
            }
        }

        // 5. Persistence
        if (!state.isSource()) {
            // Mark the "Fresh" block for death (FALLING = true) in 2 ticks
            level.setBlock(pos, state.setValue(FALLING, true).createLegacyBlock(), 2);
            level.scheduleTick(pos, this, 2);
        } else {
            // Source stays alive and pumping
            level.scheduleTick(pos, this, 8);
        }
    }

    // VISUAL OVERRIDES
    // 0.88f = height of a Level 8 flowing block
    @Override
    public float getOwnHeight(FluidState state) {
        return state.isSource() ? 1.0f : flowingBlockHeight;
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return state.isSource() ? 1.0f : flowingBlockHeight;
    }

    // DISABLE VANILLA FLOW BEHAVIOR
    @Override
    protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState) { return Fluids.EMPTY.defaultFluidState(); } // No source creation

    @Override
    protected void spreadTo(LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState) {}

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) { return Vec3.ZERO; }

    @Override
    protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState, Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fromFluid) { return false; } // No horizontal spreading

    @Override
    protected void spread(Level level, BlockPos pos, FluidState state) {} // Do nothing, handle everything in tick()

    @Override
    protected int getSlopeFindDistance(LevelReader level) { return 0; }

    @Override
    protected int getDropOff(LevelReader level) { return 1; }

    @Override
    public int getTickDelay(LevelReader level) { return tickRate; }

    @Override
    public FluidState getSource(boolean falling) { return super.getSource(false); }


    // INNER CLASSES
    public static class Flowing extends UpwardBaseFlowingFluid {
        public Flowing(Properties properties) { super(properties); }
        @Override public boolean isSource(FluidState state) { return false; }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
    }

    public static class Source extends UpwardBaseFlowingFluid {
        public Source(Properties properties) { super(properties); }
        @Override public boolean isSource(FluidState state) { return true; }
        @Override public int getAmount(FluidState state) { return 8; }
    }
}
