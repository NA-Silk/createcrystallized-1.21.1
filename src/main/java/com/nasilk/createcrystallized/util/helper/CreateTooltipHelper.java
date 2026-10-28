package com.nasilk.createcrystallized.util.helper;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class CreateTooltipHelper {
    public static void registerSimpleTooltip(ItemProviderEntry<?, ?> itemProvider) {
        Item item = itemProvider.asItem();
        TooltipModifier.REGISTRY.register(
            item,
            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
        );
    }

    public static void registerGoggleTooltip(ItemProviderEntry<?, ?> itemProvider, int lines) {
        Item item = itemProvider.asItem();
        ResourceLocation itemId = itemProvider.getId();
        TooltipModifier.REGISTRY.register(
            item,
            new GoggleItemDescriptionModifier(item, itemId, lines)
        );
    }

    private static class GoggleItemDescriptionModifier extends ItemDescription.Modifier {
        private final ResourceLocation itemId;
        private final int lines;

        public GoggleItemDescriptionModifier(Item item, ResourceLocation itemId, int lines) {
            super(item, FontHelper.Palette.STANDARD_CREATE);
            this.itemId = itemId;
            this.lines = lines;
        }

        @Override
        public void modify(ItemTooltipEvent context) {
            // Show main context
            super.modify(context);

            // Verify goggles
            Player player = context.getEntity();
            if (player == null || !GogglesItem.isWearingGoggles(player)) return;

            // Add tooltips
            String path = CreateCrystallized.MOD_ID + "." + itemId.getPath();
            for (int i = 0; i < lines; i++) context.getToolTip().add(
                Component.translatable(path + ".goggles_tooltip." + i).withStyle(ChatFormatting.AQUA)
            );
        }
    }
}
