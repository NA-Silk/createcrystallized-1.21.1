package com.nasilk.createcrystallized.ponder;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class ModPonderTags {
    public static final ResourceLocation PHYSICS_BEHAVIOR = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "physics_behavior");

    public ModPonderTags() {}

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(RegisteredObjectsHelper::getKeyOrThrow);

        helper.registerTag(PHYSICS_BEHAVIOR).addToIndex()
            .item(ModBlocks.DENSITE_WELL.asItem())
            .title("Crystal Blocks")
            .description("Blocks that enable crystal powered physics interactions")
            .register();
        itemHelper.addToTag(PHYSICS_BEHAVIOR)
            .add(ModBlocks.DENSITE_WELL.asItem())
            .add(ModBlocks.OSCILLITE_CANNON.asItem())
            .add(ModBlocks.PROPULSITE_THRUSTER.asItem());
    }
}
