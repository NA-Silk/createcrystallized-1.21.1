package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.ponder.custom.CrystalBlockScenes;
import com.nasilk.createcrystallized.ponder.custom.FluidScenes;
import com.nasilk.createcrystallized.ponder.custom.MachinedCrystalBlockScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ModPonderScenes {
    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> registry) {
        /// FLUIDS
        registry.forComponents(CreateCrystallized.getRL("densite_emulsion_bucket"))
            .addStoryBoard("fluids/densite_emulsion", FluidScenes::densiteEmulsion)
            .addStoryBoard("crystal_blocks/densite", CrystalBlockScenes::densite);
        registry.forComponents(ModBlocks.DENSITE_BLOCK.getId())
            .addStoryBoard("crystal_blocks/densite", CrystalBlockScenes::densite)
            .addStoryBoard("fluids/densite_emulsion", FluidScenes::densiteEmulsion);

        registry.forComponents(CreateCrystallized.getRL("propulsite_flurry_bucket"))
            .addStoryBoard("fluids/propulsite_flurry", FluidScenes::propulsiteFlurry)
            .addStoryBoard("crystal_blocks/propulsite", CrystalBlockScenes::propulsite);
        registry.forComponents(ModBlocks.PROPULSITE_BLOCK.getId())
            .addStoryBoard("crystal_blocks/propulsite", CrystalBlockScenes::propulsite)
            .addStoryBoard("fluids/propulsite_flurry", FluidScenes::propulsiteFlurry);

        registry.forComponents(CreateCrystallized.getRL("oscillite_suspension_bucket"))
            .addStoryBoard("fluids/oscillite_suspension", FluidScenes::oscilliteSuspension)
            .addStoryBoard("crystal_blocks/oscillite", CrystalBlockScenes::oscillite);
        registry.forComponents(ModBlocks.OSCILLITE_BLOCK.getId())
            .addStoryBoard("crystal_blocks/oscillite", CrystalBlockScenes::oscillite)
            .addStoryBoard("fluids/oscillite_suspension", FluidScenes::oscilliteSuspension);

        /// MACHINED CRYSTAL BLOCKS
        registry.forComponents(ModBlocks.DENSITE_WELL.getId())
            .addStoryBoard("machined_crystal_blocks/densite_well", MachinedCrystalBlockScenes::densiteWell);
        registry.forComponents(ModBlocks.PROPULSITE_THRUSTER.getId())
            .addStoryBoard("machined_crystal_blocks/propulsite_thruster", MachinedCrystalBlockScenes::propulsiteThruster);
        registry.forComponents(ModBlocks.OSCILLITE_CANNON.getId())
            .addStoryBoard("machined_crystal_blocks/oscillite_cannon", MachinedCrystalBlockScenes::oscilliteCannon);
    }
}
