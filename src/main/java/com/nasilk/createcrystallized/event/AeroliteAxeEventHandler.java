package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteAxeItem;
import com.nasilk.createcrystallized.mixin.TreeGetterAccessor;
import com.simibubi.create.content.kinetics.saw.TreeCutter;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import java.util.*;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class AeroliteAxeEventHandler {
    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!ModItems.AEROLITE_AXE.isIn(event.getPlayer().getMainHandItem()) ||
            !TreeCutter.isLog(event.getState())
        ) return;
        if (event.getPlayer().level() instanceof ServerLevel level) onLogBroken(level, event.getPos(), event.getState());
    }

    public static void onLogBroken(ServerLevel level, BlockPos breakingPos, BlockState stateToBreak) {
        // Just break dynamic trees for now TODO Figure this out too
        Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(stateToBreak.getBlock(), breakingPos);
        if (dynamicTree.isPresent()) {
            dynamicTree.get().destroyBlocks(level, null, (pos, stack) -> dropItemFromCutTree(level, breakingPos, pos, stack));
            return;
        }

        // Don't process already existing sublevels
        if (Sable.HELPER.getContaining(level, breakingPos.above()) != null) return;

        // Get tree
        TreeCutter.Tree tree = TreeCutter.findTree(level, breakingPos, stateToBreak);
        if (tree == TreeCutter.NO_TREE) return;

        // Convert tree to single list
        TreeGetterAccessor accessibleTree = (TreeGetterAccessor) tree;
        Set<BlockPos> blocks = new HashSet<>();
        blocks.addAll(accessibleTree.getLogs());
        blocks.addAll(accessibleTree.getLeaves());
        blocks.addAll(accessibleTree.getAttachments());
        if (blocks.isEmpty()) return;

        // Convert to sublevel
        if (SubLevelAssemblyHelper.assembleBlocks(level, breakingPos.above(), blocks, new BoundingBox3i()) != null) {
            AeroliteAxeItem.doAssemblyAxeEffects(level, breakingPos);
        }
    }

    public static void dropItemFromCutTree(ServerLevel level, BlockPos breakingPos, BlockPos pos, ItemStack stack) {
        float distance = (float) Math.sqrt(pos.distSqr(breakingPos));
        Vec3 dropPos = VecHelper.getCenterOf(pos);
        ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setDeltaMovement(Vec3.atLowerCornerOf(breakingPos.subtract(breakingPos)).scale(distance / 20f));
        level.addFreshEntity(entity);
    }
}
