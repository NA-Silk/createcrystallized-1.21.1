package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.custom.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import static com.nasilk.createcrystallized.item.custom.AeroliteShovelItem.AEROLITE_TIER;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateCrystallized.MOD_ID);


    /** ADVANCED ITEMS */
    public static final DeferredItem<Item> CREATIVE_FLUID_ERASER = ITEMS.registerItem(
        "creative_fluid_eraser",
        (properties) -> new CreativeFluidEraserItem(properties.stacksTo(1))
    );

    public static final DeferredItem<Item> CREATIVE_BAG_OF_LONGS = ITEMS.registerItem(
        "creative_bag_of_longs",
        (properties) -> new CreativeBagOfLongsItem(properties.stacksTo(1))
    );

    public static final DeferredItem<Item> AEROLITE_SHOVEL = ITEMS.registerItem(
        "aerolite_shovel",
        (properties) -> new AeroliteShovelItem(AEROLITE_TIER, properties.stacksTo(1))
    );


    /** CRAFTING ITEMS */
    // Credit to @Eevneon from the Create Aeronautics Discord for the Sprite!
    public static final DeferredItem<Item> OSCILLITE_RESONATOR = ITEMS.registerItem(
        "oscillite_resonator",
        (properties) -> new Item(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> DENSITE_CORE = ITEMS.registerItem(
        "densite_core",
        (properties) -> new DensiteCoreItem(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> CHORA_INGOT = ITEMS.registerItem(
        "chora_ingot",
        (properties) -> new Item(properties.stacksTo(64))
    );

    public static final DeferredItem<Item> RAW_AEROLITE = ITEMS.registerItem(
        "raw_aerolite",
        (properties) -> new Item(properties.stacksTo(64))
    );

    public static final DeferredItem<Item> CRUSHED_RAW_AEROLITE = ITEMS.registerItem(
        "crushed_raw_aerolite",
        (properties) -> new Item(properties.stacksTo(64))
    );

    public static final DeferredItem<Item> AEROLITE_INGOT = ITEMS.registerItem(
        "aerolite_ingot",
        (properties) -> new Item(properties.stacksTo(64))
    );

    public static final DeferredItem<Item> AEROLITE_SHEET = ITEMS.registerItem(
        "aerolite_sheet",
        (properties) -> new Item(properties.stacksTo(64))
    );

    public static void register(IEventBus eventbus) {
      ITEMS.register(eventbus);
    }
}
