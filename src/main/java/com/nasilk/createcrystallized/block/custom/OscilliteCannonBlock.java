package com.nasilk.createcrystallized.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class OscilliteCannonBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public OscilliteCannonBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null) return super.getStateForPlacement(context);
        if (player.isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }
}