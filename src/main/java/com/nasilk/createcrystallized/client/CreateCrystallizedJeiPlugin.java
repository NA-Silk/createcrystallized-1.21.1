package com.nasilk.createcrystallized.client;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class CreateCrystallizedJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("createcrystallized", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        MysteriousItemConversionCategory.RECIPES.add(
                ConversionRecipe.create(
                        new ItemStack((ItemLike) ModFluids.DENSITE_EMULSION_BUCKET),
                        new ItemStack(ModBlocks.DENSITE_BLOCK)
                )
        );
        MysteriousItemConversionCategory.RECIPES.add(
                ConversionRecipe.create(
                        new ItemStack((ItemLike) ModFluids.PROPULSITE_FLURRY_BUCKET),
                        new ItemStack(ModBlocks.PROPULSITE_BLOCK)
                )
        );
        MysteriousItemConversionCategory.RECIPES.add(
                ConversionRecipe.create(
                        new ItemStack((ItemLike) ModFluids.OSCILLITE_SUSPENSION_BUCKET),
                        new ItemStack(ModBlocks.OSCILLITE_BLOCK)
                )
        );
    }
}