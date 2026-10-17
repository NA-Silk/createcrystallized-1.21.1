package com.nasilk.createcrystallized.client;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID = CreateCrystallized.getRL("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    //Register items in Creates "Mysterious Fluid" JEI tab.
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

    // Hide the Pebble Recipe from JEI, if you're a player digging into this code, don't tell!
    private static final ResourceLocation PEBBLE_RECIPE_ID = CreateCrystallized.getRL("sequenced_assembly/the_pebble");
    private static final RecipeType<RecipeHolder<SequencedAssemblyRecipe>> SEQUENCED_ASSEMBLY_JEI_TYPE = RecipeType.createRecipeHolderType(Create.asResource("sequenced_assembly"));

    @SuppressWarnings("unchecked")
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) return;

        Optional<RecipeHolder<?>> holder = listener.getRecipeManager().byKey(PEBBLE_RECIPE_ID);
        if (holder.isEmpty()) {
            CreateCrystallized.LOGGER.warn("Could not find Pebble recipe: {}", PEBBLE_RECIPE_ID);
            return;
        }

        RecipeHolder<SequencedAssemblyRecipe> pebbleRecipe = (RecipeHolder<SequencedAssemblyRecipe>) holder.get();
        jeiRuntime.getRecipeManager().hideRecipes(SEQUENCED_ASSEMBLY_JEI_TYPE, List.of(pebbleRecipe));
    }
}
