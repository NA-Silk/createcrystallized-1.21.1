package com.nasilk.createfluidsandfixins.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class UpwardBaseFlowingFluid extends BaseFlowingFluid {
    protected UpwardBaseFlowingFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected void spread(Level level, BlockPos pos, FluidState state) {
        int amount = state.getAmount();

        // Kill weak fluid
        if (amount == 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        // Vanish at world ceiling instead of spreading
        if (pos.getY() >= level.getMaxBuildHeight() - 1) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        // Only move upward
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        FluidState aboveFluid = level.getFluidState(abovePos);
        if (aboveFluid.isEmpty() && aboveState.canBeReplaced()) {
            int newAmount = amount - 2;
            if (newAmount > 0) {
                level.setBlock(
                    abovePos,
                    this.getFlowing(newAmount, false).createLegacyBlock(),
                    3
                );
            }

            // Remove current block after moving upward (prevents infinite columns)
            if (!state.isSource()) {
                if (Math.random() > 0.1f) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
            return;
        }

        // Keep flowing blocks updated
        if (!state.isSource()) {
            level.scheduleTick(pos, this, 2);
        }
    }

    // Higher values weaken fluid
    @Override
    protected int getDropOff(LevelReader level) {
        return 0;
    }

    // Prevent source recreation
    @Override
    public FluidState getSource(boolean falling) {
        return super.getSource(false);
    }

    public static class Flowing extends UpwardBaseFlowingFluid {

        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<Fluid, FluidState> builder
        ) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }

    public static class Source extends UpwardBaseFlowingFluid {

        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }
}
