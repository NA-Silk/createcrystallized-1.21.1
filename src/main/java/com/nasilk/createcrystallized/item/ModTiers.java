package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.common.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModTiers {
    public static final Tier AEROLITE = new SimpleTier(
        ModTags.Blocks.INCORRECT_FOR_AEROLITE_TOOL,
        1800, 6.0f, 2.0f, 20,
        () -> Ingredient.of(ModItems.AEROLITE_INGOT)
    );
}
