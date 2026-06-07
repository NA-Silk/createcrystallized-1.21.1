package com.nasilk.createcrystallized.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CreativeFluidEraserItem extends Item {
    public final int RADIUS = 32;

    public CreativeFluidEraserItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        // Only use on BLOCK action
        if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }

        // Get Fluid at targetPos
        BlockPos targetPos = blockhitresult.getBlockPos();
        FluidState fluidState = level.getFluidState(targetPos);
        if (!level.isClientSide() && !fluidState.isEmpty()) {
            Fluid targetFluid = fluidState.getType();

            // Delete logic
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int y = -RADIUS; y <= RADIUS; y++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        BlockPos currentPos = targetPos.offset(x, y, z);
                        if (level.getFluidState(currentPos).getType().isSame(targetFluid)) {
                            level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }

            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }
}
