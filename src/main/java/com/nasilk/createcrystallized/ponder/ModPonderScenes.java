package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.ponder.custom.MachinedCrystalBlocksScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModPonderScenes {
    public ModPonderScenes() {}

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> registry) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registry.withKeyFunction(DeferredHolder::getId);

        helper.forComponents(ModBlocks.DENSITE_WELL)
            .addStoryBoard("machined_crystal_blocks/densite_well", MachinedCrystalBlocksScenes::densiteWell);
    }
}
