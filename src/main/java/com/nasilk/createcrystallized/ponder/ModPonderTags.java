package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.fluid.ModFluids;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class ModPonderTags {
    public static final ResourceLocation CRYSTAL_FLUIDS = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "crystal_fluids");
    public static final ResourceLocation CRYSTAL_BLOCKS = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "crystal_blocks");
    public static final ResourceLocation MACHINED_CRYSTAL_BLOCKS = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "machined_crystal_blocks");

    public ModPonderTags() {}

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(RegisteredObjectsHelper::getKeyOrThrow);

        /// FLUIDS
        helper.registerTag(CRYSTAL_FLUIDS).addToIndex()
            .item(ModFluids.DENSITE_EMULSION_BUCKET.asItem())
            .title("Crystal Fluids")
            .description("Fluids that produce fancy crystals.")
            .register();
        itemHelper.addToTag(CRYSTAL_FLUIDS)
            .add(ModFluids.DENSITE_EMULSION_BUCKET.asItem())
            .add(ModFluids.PROPULSITE_FLURRY_BUCKET.asItem())
            .add(ModFluids.OSCILLITE_SUSPENSION_BUCKET.asItem());

        /// CRYSTAL BLOCKS
        helper.registerTag(CRYSTAL_BLOCKS).addToIndex()
            .item(ModBlocks.DENSITE_BLOCK.asItem())
            .title("Crystal Blocks")
            .description("Blocks that enable crystal shenanigans.")
            .register();
        itemHelper.addToTag(CRYSTAL_BLOCKS)
            .add(ModBlocks.DENSITE_BLOCK.asItem())
            .add(ModBlocks.PROPULSITE_BLOCK.asItem())
            .add(ModBlocks.OSCILLITE_BLOCK.asItem());

        /// MACHINED CRYSTAL BLOCKS
        helper.registerTag(MACHINED_CRYSTAL_BLOCKS).addToIndex()
            .item(ModBlocks.DENSITE_WELL.asItem())
            .title("Machined Crystal Blocks")
            .description("Blocks that enable crystal powered physics interactions.")
            .register();
        itemHelper.addToTag(MACHINED_CRYSTAL_BLOCKS)
            .add(ModBlocks.DENSITE_WELL.asItem())
            .add(ModBlocks.PROPULSITE_THRUSTER.asItem())
            .add(ModBlocks.OSCILLITE_CANNON.asItem());
    }
}
