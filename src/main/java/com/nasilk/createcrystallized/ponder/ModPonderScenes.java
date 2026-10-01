package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.ponder.custom.CrystalBlockScenes;
import com.nasilk.createcrystallized.ponder.custom.FluidScenes;
import com.nasilk.createcrystallized.ponder.custom.MachinedCrystalBlockScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModPonderScenes {
    public ModPonderScenes() {}

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> registry) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registry.withKeyFunction(DeferredHolder::getId);

        /// FLUIDS
        helper.forComponents(itemProvider("densite_emulsion_bucket"))
            .addStoryBoard("fluids/densite_emulsion", FluidScenes::densiteEmulsion)
            .addStoryBoard("crystal_blocks/densite", CrystalBlockScenes::densite);
        helper.forComponents(ModBlocks.DENSITE_BLOCK)
            .addStoryBoard("crystal_blocks/densite", CrystalBlockScenes::densite)
            .addStoryBoard("fluids/densite_emulsion", FluidScenes::densiteEmulsion);

        helper.forComponents(itemProvider("propulsite_flurry_bucket"))
            .addStoryBoard("fluids/propulsite_flurry", FluidScenes::propulsiteFlurry)
            .addStoryBoard("crystal_blocks/propulsite", CrystalBlockScenes::propulsite);
        helper.forComponents(ModBlocks.PROPULSITE_BLOCK)
            .addStoryBoard("crystal_blocks/propulsite", CrystalBlockScenes::propulsite)
            .addStoryBoard("fluids/propulsite_flurry", FluidScenes::propulsiteFlurry);

        helper.forComponents(itemProvider("oscillite_suspension_bucket"))
            .addStoryBoard("fluids/oscillite_suspension", FluidScenes::oscilliteSuspension)
            .addStoryBoard("crystal_blocks/oscillite", CrystalBlockScenes::oscillite);
        helper.forComponents(ModBlocks.OSCILLITE_BLOCK)
            .addStoryBoard("crystal_blocks/oscillite", CrystalBlockScenes::oscillite)
            .addStoryBoard("fluids/oscillite_suspension", FluidScenes::oscilliteSuspension);

        /// MACHINED CRYSTAL BLOCKS
        helper.forComponents(ModBlocks.DENSITE_WELL)
            .addStoryBoard("machined_crystal_blocks/densite_well", MachinedCrystalBlockScenes::densiteWell);
        helper.forComponents(ModBlocks.PROPULSITE_THRUSTER)
            .addStoryBoard("machined_crystal_blocks/propulsite_thruster", MachinedCrystalBlockScenes::propulsiteThruster);
        helper.forComponents(ModBlocks.OSCILLITE_CANNON)
            .addStoryBoard("machined_crystal_blocks/oscillite_cannon", MachinedCrystalBlockScenes::oscilliteCannon);
    }

    private static ItemProviderEntry<Item, Item> itemProvider(String name) {
        return new ItemProviderEntry<>(CreateCrystallized.getRegistrate(), DeferredHolder.create(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, name))));
    }
}
