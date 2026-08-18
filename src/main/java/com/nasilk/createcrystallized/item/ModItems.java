package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.item.custom.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

import static com.nasilk.createcrystallized.item.custom.AeroliteShovelItem.AEROLITE_TIER;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateCrystallized.MOD_ID);

    /**  ̶S̶T̶U̶P̶I̶D̶ ADVANCED ITEMS */
    public static final DeferredItem<CreativeFluidEraserItem> CREATIVE_FLUID_ERASER = ITEMS.registerItem(
        "creative_fluid_eraser",
        (properties) -> new CreativeFluidEraserItem(properties.stacksTo(1))
    );

    public static final DeferredItem<Item> AEROLITE_SHOVEL = ITEMS.register(
        "aerolite_shovel",
        () -> new AeroliteShovelItem(AEROLITE_TIER, new Item.Properties())
    );

    // TODO Make this do something - does it really need to?..
    public static final DeferredItem<Item> CREATIVE_BAG_OF_LONGS = ITEMS.register(
        "creative_bag_of_longs",
        () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<TransformItem> CHORA_INGOT = ITEMS.registerItem(
        "chora_ingot",
        (properties) -> new TransformItem(
            properties.stacksTo(64),
            new HashMap<>(Map.of(Blocks.GLASS, ModBlocks.CHORA_CASING.get()))
        )
    );

    // Credit to @Eevneon from the Create Aeronautics Discord for the Sprite! // TODO move transformation related things to recipies using creates own system
    public static final DeferredItem<TransformItem> OSCILLITE_RESONATOR = ITEMS.registerItem(
        "oscillite_resonator",
        (properties) -> new TransformItem(
            properties.stacksTo(1),
            new HashMap<>(Map.of(ModBlocks.ENCASED_OSCILLITE_BLOCK.get(), ModBlocks.OSCILLITE_CANNON.get()))
        )
    );

    /** CRAFTING ITEMS */
    public static final DeferredItem<Item> AEROLITE_INGOT = ITEMS.register(
            "aerolite_ingot",
            () -> new Item(new Item.Properties().stacksTo(64))

    );
    public static final DeferredItem<Item> DENSITE_CORE = ITEMS.register(
            "densite_core",
            () -> new DensiteCoreItem(new Item.Properties().stacksTo(16))
    );

    public static final DeferredItem<Item> RAW_AEROLITE = ITEMS.register(
            "raw_aerolite",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final DeferredItem<Item> CRUSHED_RAW_AEROLITE = ITEMS.register(
            "crushed_raw_aerolite",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final DeferredItem<Item> AEROLITE_SHEET = ITEMS.register(
            "aerolite_sheet",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static void register(IEventBus eventbus) {
      ITEMS.register(eventbus);
    }
}
