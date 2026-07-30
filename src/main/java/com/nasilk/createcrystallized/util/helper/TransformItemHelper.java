package com.nasilk.createcrystallized.util.helper;

import com.nasilk.createcrystallized.block.custom.OscilliteCannonBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;
import java.util.Optional;

public class TransformItemHelper {
    public static Optional<InteractionResult> tryTransform(UseOnContext context, Map<Block, Block> blockMap) {
        // Get level
        Level level = context.getLevel();
        if (level.isClientSide()) return Optional.of(InteractionResult.sidedSuccess(true));

        // Get player
        Player player = context.getPlayer();
        if (player == null) return Optional.empty();
        Direction direction = context.getClickedFace();
        Direction facing = player.isShiftKeyDown() ? direction.getOpposite() : direction;

        // Get matching BlockState
        BlockPos sourcePos = context.getClickedPos();
        Block targetBlock = blockMap.get(level.getBlockState(sourcePos).getBlock());
        if (targetBlock == null) return Optional.empty();
        BlockState targetState = targetBlock.defaultBlockState();

        // Transform logic
        if (targetState.hasProperty(BlockStateProperties.FACING)) targetState = targetState.setValue(BlockStateProperties.FACING, facing);
        if (targetState.hasProperty(OscilliteCannonBlock.IS_BARREL)) {
            if (level.getBlockState(sourcePos.relative(facing)).canBeReplaced()) {
                level.setBlockAndUpdate(sourcePos.relative(facing), targetState.setValue(OscilliteCannonBlock.IS_BARREL, true));
                targetState = targetState.setValue(OscilliteCannonBlock.IS_BARREL, false);
            } else return Optional.empty();
        }
        level.setBlockAndUpdate(sourcePos, targetState);

        // Reduce itemStack count
        if (!player.isCreative()) context.getItemInHand().shrink(1);
        return Optional.of(InteractionResult.sidedSuccess(false));
    }
}
