package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteAxeItem;
import com.nasilk.createcrystallized.mixin.TreeGetterAccessor;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.kinetics.saw.TreeCutter;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
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
        if (Mods.DYNAMICTREES.isLoaded()) return; // Don't bother with Dynamic Trees
        if (event.getPlayer().level() instanceof ServerLevel level) onLogBroken(level, event.getPos(), event.getState());
    }

    public static void onLogBroken(ServerLevel level, BlockPos breakingPos, BlockState stateToBreak) {
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

        // Convert to sublevel and play effects
        if (SubLevelAssemblyHelper.assembleBlocks(level, breakingPos.above(), blocks, new BoundingBox3i()) != null) {
            AeroliteAxeItem.doAssemblyAxeEffects(level, breakingPos);
        }
    }
}
