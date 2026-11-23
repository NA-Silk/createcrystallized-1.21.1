package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.simibubi.create.content.kinetics.saw.TreeCutter;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Optional;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class AeroliteAxeEventHandler {
    @SubscribeEvent
    public static void onInteraction(BlockEvent.BreakEvent event) {
        if (!ModItems.AEROLITE_AXE.isIn(event.getPlayer().getMainHandItem()) ||
            !event.getState().is(BlockTags.LOGS)
        ) return;
        CreateCrystallized.LOGGER.info("Aerolite Axe has hit a log at {}", event.getPos());
        onBlockBroken(event.getPlayer().level(), event.getPos(), event.getState());
    }

    // TODO Instead of breaking the rest, convert them to a sublevel
    public static void onBlockBroken(Level level, BlockPos breakingPos, BlockState stateToBreak) {
        Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(stateToBreak.getBlock(), breakingPos);
        if (dynamicTree.isPresent()) {
            dynamicTree.get()
                .destroyBlocks(level, null, (pos, stack) -> dropItemFromCutTree(level, breakingPos, pos, stack));
            return;
        }

        TreeCutter.findTree(level, breakingPos, stateToBreak)
            .destroyBlocks(level, null, (pos, stack) -> dropItemFromCutTree(level, breakingPos, pos, stack));
    }

    public static void dropItemFromCutTree(Level level, BlockPos breakingPos, BlockPos pos, ItemStack stack) {
        float distance = (float) Math.sqrt(pos.distSqr(breakingPos));
        Vec3 dropPos = VecHelper.getCenterOf(pos);
        ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setDeltaMovement(Vec3.atLowerCornerOf(breakingPos.subtract(breakingPos)).scale(distance / 20f));
        level.addFreshEntity(entity);
    }
}
