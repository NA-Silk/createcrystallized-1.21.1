package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.common.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModTiers {
    public static final Tier AEROLITE = new SimpleTier(
        ModTags.Blocks.INCORRECT_FOR_AEROLITE_TOOL,
        1300, 6.0f, 2.0f, 15,
        () -> Ingredient.of(ModItems.AEROLITE_INGOT)
    );

    public static void init() {
        CreateCrystallized.LOGGER.info("Tiers initialized");
    } //im gonna vwomit on the fwoor
}
