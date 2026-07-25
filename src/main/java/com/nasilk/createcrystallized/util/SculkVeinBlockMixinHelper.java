package com.nasilk.createcrystallized.util;

import com.nasilk.createcrystallized.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SculkVeinBlockMixinHelper {
    public static BlockState getConvertedBlockState(BlockState blockState) {
        // Set default null target state
        BlockState targetState = null;

        // Get simple conversion for Blocks.AMETHYST_BLOCK and Blocks.BUDDING_AMETHYST
        if (blockState.is(Blocks.AMETHYST_BLOCK)) targetState = ModBlocks.ECHO_CRYSTAL_BLOCK.get().defaultBlockState();
        else if (blockState.is(Blocks.BUDDING_AMETHYST)) targetState = ModBlocks.BUDDING_ECHO_CRYSTAL.get().defaultBlockState();

        // Get state-value matching conversion for AmethystClusterBlock extensions
        else if (blockState.getBlock() instanceof AmethystClusterBlock) {
            if (blockState.is(Blocks.AMETHYST_CLUSTER)) targetState = ModBlocks.ECHO_CRYSTAL_CLUSTER.get().defaultBlockState();
            else if (blockState.is(Blocks.LARGE_AMETHYST_BUD)) targetState = ModBlocks.LARGE_ECHO_CRYSTAL_BUD.get().defaultBlockState();
            else if (blockState.is(Blocks.MEDIUM_AMETHYST_BUD)) targetState = ModBlocks.MEDIUM_ECHO_CRYSTAL_BUD.get().defaultBlockState();
            else if (blockState.is(Blocks.SMALL_AMETHYST_BUD)) targetState = ModBlocks.SMALL_ECHO_CRYSTAL_BUD.get().defaultBlockState();
            else return null;
            targetState = targetState
                .setValue(AmethystClusterBlock.FACING, blockState.getValue(AmethystClusterBlock.FACING))
                .setValue(AmethystClusterBlock.WATERLOGGED, blockState.getValue(AmethystClusterBlock.WATERLOGGED));
        }

        // Return conversion target state
        return targetState;
    }

    public static boolean tryConvertCluster(LevelAccessor level, BlockPos pos) {
        // Get conversion target state
        BlockState targetState = getConvertedBlockState(level.getBlockState(pos));
        if (targetState == null) return false;

        // Replace if converting a cluster
        if (targetState.getBlock() instanceof AmethystClusterBlock) {
            level.setBlock(pos, targetState, 3);
            level.playSound(null, pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0f, 1.0f);

            // Convert attached block
            Direction attached = targetState.getValue(AmethystClusterBlock.FACING).getOpposite();
            BlockPos attachedPos = pos.relative(attached);
            targetState = getConvertedBlockState(level.getBlockState(attachedPos));
            if (targetState != null) {
                level.setBlock(attachedPos, targetState, 3);
                convertAdjacentClusters(level, attachedPos);
            } else if (level.getBlockState(attachedPos).is(BlockTags.SCULK_REPLACEABLE)) {
                level.setBlock(attachedPos, Blocks.SCULK.defaultBlockState(), 3);
                convertAdjacentClusters(level, attachedPos);
            }

            // Return success
            return true;
        } else return false;
    }

    public static void convertAdjacentClusters(LevelAccessor level, BlockPos pos) {
        // Iterate over surrounding block directions
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Check for AmethystClusterBlock in matching direction (attached to the block at pos) and get conversion state
            if (neighborState.getBlock() instanceof AmethystClusterBlock && neighborState.getValue(AmethystClusterBlock.FACING) == direction) {
                neighborState = getConvertedBlockState(neighborState);

                // Convert the attached cluster if a valid conversion is found
                if (neighborState != null) {
                    level.setBlock(neighborPos, neighborState, 3);
                    level.playSound(null, neighborPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0f, 0.8f + level.getRandom().nextFloat() * 0.4f);
                }
            }
        }
    }
}
