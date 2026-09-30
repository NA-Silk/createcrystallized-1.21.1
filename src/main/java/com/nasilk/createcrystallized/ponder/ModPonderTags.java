package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class ModPonderTags {
    public static final ResourceLocation MACHINED_CRYSTAL_BLOCKS = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "machined_crystal_blocks");

    public ModPonderTags() {}

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(RegisteredObjectsHelper::getKeyOrThrow);

        helper.registerTag(MACHINED_CRYSTAL_BLOCKS).addToIndex()
            .item(ModBlocks.DENSITE_WELL.asItem())
            .title("Machined Crystal Blocks")
            .description("Blocks that enable crystal powered physics interactions.")
            .register();
        itemHelper.addToTag(MACHINED_CRYSTAL_BLOCKS)
            .add(ModBlocks.DENSITE_WELL.asItem())
            .add(ModBlocks.OSCILLITE_CANNON.asItem())
            .add(ModBlocks.PROPULSITE_THRUSTER.asItem());
    }
}
