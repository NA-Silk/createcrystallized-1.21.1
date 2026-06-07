package com.nasilk.createcrystallized.util;

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

public class TransformItemUtil {
    public static Optional<InteractionResult> tryTransform(UseOnContext context, Map<Block, Block> blockMap) {
        // Get BlockState at targetPos
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos();
        BlockState targetState = level.getBlockState(targetPos);

        // Get matching block
        Block transformBlock = blockMap.get(targetState.getBlock());
        if (transformBlock == null) return Optional.empty();

        // Transform logic
        if (!level.isClientSide()) {
            if (transformBlock.defaultBlockState().hasProperty(BlockStateProperties.FACING)) {
                Direction direction = context.getClickedFace();
                level.setBlockAndUpdate(targetPos, transformBlock.defaultBlockState().setValue(BlockStateProperties.FACING, direction));
            } else {
                level.setBlockAndUpdate(targetPos, transformBlock.defaultBlockState());
            }

            // Reduce itemStack count
            Player player = context.getPlayer();
            if (player != null && !player.isCreative()) context.getItemInHand().shrink(1);
        }
        return Optional.of(InteractionResult.sidedSuccess(level.isClientSide()));
    }
}
