package com.nasilk.createcrystallized.util.helper;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.world.level.ItemLike;

public class CreateTooltipHelper {
    public static void register(ItemLike item) {
        TooltipModifier.REGISTRY.register(
            item.asItem(),
            new ItemDescription.Modifier(
                item.asItem(),
                FontHelper.Palette.STANDARD_CREATE
            )
        );
    }
}
