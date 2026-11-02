package com.nasilk.createcrystallized.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CreativeFluidEraserItem extends Item {
    public static final int RADIUS = 32;

    public CreativeFluidEraserItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        // Server side
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(itemStack);

        // Only use on BLOCK action
        BlockHitResult blockhitresult = getPlayerPOVHitResult(serverLevel, player, ClipContext.Fluid.ANY);
        if (blockhitresult.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(itemStack);

        // Get Fluid at targetPos
        BlockPos targetPos = blockhitresult.getBlockPos();
        FluidState fluidState = serverLevel.getFluidState(targetPos);
        if (!fluidState.isEmpty()) {
            // Delete logic
            for (BlockPos currentPos : BlockPos.betweenClosed(
                targetPos.offset(-RADIUS, -RADIUS, -RADIUS),
                targetPos.offset(RADIUS, RADIUS, RADIUS)
            )) {
                if (serverLevel.getFluidState(currentPos).getType().isSame(fluidState.getType())) {
                    serverLevel.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                }
            }
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }
}
