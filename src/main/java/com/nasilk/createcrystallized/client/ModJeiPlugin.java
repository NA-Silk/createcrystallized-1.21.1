package com.nasilk.createcrystallized.client;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        MysteriousItemConversionCategory.RECIPES.add(
            ConversionRecipe.create(
                new ItemStack(ModFluids.DENSITE_EMULSION.getBucket().orElseThrow()),
                new ItemStack(ModBlocks.DENSITE_BLOCK)
            )
        );
        MysteriousItemConversionCategory.RECIPES.add(
            ConversionRecipe.create(
                new ItemStack(ModFluids.PROPULSITE_FLURRY.getBucket().orElseThrow()),
                new ItemStack(ModBlocks.PROPULSITE_BLOCK)
            )
        );
        MysteriousItemConversionCategory.RECIPES.add(
            ConversionRecipe.create(
                new ItemStack(ModFluids.OSCILLITE_SUSPENSION.getBucket().orElseThrow()),
                new ItemStack(ModBlocks.OSCILLITE_BLOCK)
            )
        );
    }

    public static void register() {
        CreateCrystallized.LOGGER.info("JEI Plugin registered");
    }
}
