package com.nasilk.createcrystallized.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.border.WorldBorder;

public class AeroliteOreBlock extends Block {
    public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;

    public AeroliteOreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISARMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISARMED);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!state.getValue(DISARMED)) teleport(state, level, pos);
        super.attack(state, level, pos, player);
    }

    /** See DragonEggBlock.java for reference code */
    private void teleport(BlockState state, Level level, BlockPos pos) {
        WorldBorder worldborder = level.getWorldBorder();

        // Check up to 100 block positions
        for (int i = 0; i < 100; i++) {
            // Select offsets between [-7,7]
            BlockPos blockpos = pos.offset(
                    level.random.nextInt(8) - level.random.nextInt(8),
                    level.random.nextInt(8) - level.random.nextInt(8),
                    level.random.nextInt(8) - level.random.nextInt(8)
            );

            // Check if offset position is viable and place
            if (
                level.getBlockState(blockpos).isAir()
                && worldborder.isWithinBounds(blockpos)
                && blockpos.getY() >= level.getMinBuildHeight()
                && blockpos.getY() <= level.getMaxBuildHeight()
                && level instanceof ServerLevel serverLevel
            ) {
                serverLevel.setBlockAndUpdate(blockpos, state.setValue(DISARMED, true));
                serverLevel.removeBlock(pos, false);
                return;
            }
        }
    }
}
