package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingEchoCrystalBlock extends BuddingAmethystBlock {
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingEchoCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int randInt = random.nextInt(6);
        if (randInt % 5 == 0) {
            Direction direction = DIRECTIONS[randInt];
            BlockPos blockPos = pos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos);

            Block block = null;
            if (canClusterGrowAtState(blockState)) {
                block = ModBlocks.SMALL_ECHO_CRYSTAL_BUD.get();
            } else if (blockState.hasProperty(AmethystClusterBlock.FACING) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                if (blockState.is(ModBlocks.SMALL_ECHO_CRYSTAL_BUD.get())) block = ModBlocks.MEDIUM_ECHO_CRYSTAL_BUD.get();
                else if (blockState.is(ModBlocks.MEDIUM_ECHO_CRYSTAL_BUD.get())) block = ModBlocks.LARGE_ECHO_CRYSTAL_BUD.get();
                else if (blockState.is(ModBlocks.LARGE_ECHO_CRYSTAL_BUD.get())) block = ModBlocks.ECHO_CRYSTAL_CLUSTER.get();
            }

            if (block != null) {
                level.setBlockAndUpdate(
                    blockPos,
                    block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER)
                );
            }
        }
    }
}
