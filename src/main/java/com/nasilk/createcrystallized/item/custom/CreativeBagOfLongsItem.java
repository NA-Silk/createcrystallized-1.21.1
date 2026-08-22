package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.api.IHaveLongs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CreativeBagOfLongsItem extends Item {
    public CreativeBagOfLongsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Server side
        ItemStack itemStack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(itemStack);

        // Only use on BLOCK action
        BlockHitResult blockhitresult = getPlayerPOVHitResult(serverLevel, player, ClipContext.Fluid.ANY);
        if (blockhitresult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(itemStack);

        // Get BlockState at blockPos
        BlockPos blockPos = blockhitresult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isEmpty()) return InteractionResultHolder.pass(itemStack);

        // Use longBlock
        if (blockState.getBlock() instanceof IHaveLongs longBlock
            && ((player.isShiftKeyDown() && longBlock.shiftUpdateLongs(serverLevel, blockState, blockPos)) || longBlock.defaultUpdateLongs(serverLevel, blockState, blockPos))
        ) return InteractionResultHolder.success(itemStack);
        return InteractionResultHolder.pass(itemStack);
    }
}
