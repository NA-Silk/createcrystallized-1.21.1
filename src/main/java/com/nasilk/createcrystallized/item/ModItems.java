package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.custom.*;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

public class ModItems {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();


    // ITEM REGISTRATIONS
    /// ADVANCED ITEMS
    public static final ItemEntry<CreativeFluidEraserItem> CREATIVE_FLUID_ERASER = REGISTRATE.item(
        "creative_fluid_eraser",
        (properties) -> new CreativeFluidEraserItem(properties.stacksTo(1))
    ).register();

    public static final ItemEntry<CreativeBagOfLongsItem> CREATIVE_BAG_OF_LONGS = REGISTRATE.item(
        "creative_bag_of_longs",
        (properties) -> new CreativeBagOfLongsItem(properties.stacksTo(1))
    ).register();

    public static final ItemEntry<AeroliteShovelItem> AEROLITE_SHOVEL = REGISTRATE.item(
        "aerolite_shovel",
        (properties) -> new AeroliteShovelItem(ModTiers.AEROLITE, properties
            .stacksTo(1)
            .attributes(ShovelItem.createAttributes(ModTiers.AEROLITE, 1.5f, -2.4f))
        )
    ).register();


    /// CRAFTING ITEMS
    // Credit to @Eevneon from the Create Aeronautics Discord for the Sprite!
    public static final ItemEntry<Item> OSCILLITE_RESONATOR = REGISTRATE.item(
        "oscillite_resonator",
        (properties) -> new Item(properties.stacksTo(16))
    ).register();

    public static final ItemEntry<DensiteCoreItem> DENSITE_CORE = REGISTRATE.item(
        "densite_core",
        (properties) -> new DensiteCoreItem(properties.stacksTo(16))
    ).register();

    public static final ItemEntry<Item> CHORA_INGOT = REGISTRATE.item(
        "chora_ingot",
        (properties) -> new Item(properties.stacksTo(64))
    ).register();

    public static final ItemEntry<Item> RAW_AEROLITE = REGISTRATE.item(
        "raw_aerolite",
        (properties) -> new Item(properties.stacksTo(64))
    ).register();

    public static final ItemEntry<Item> CRUSHED_RAW_AEROLITE = REGISTRATE.item(
        "crushed_raw_aerolite",
        (properties) -> new Item(properties.stacksTo(64))
    ).register();

    public static final ItemEntry<Item> AEROLITE_INGOT = REGISTRATE.item(
        "aerolite_ingot",
        (properties) -> new Item(properties.stacksTo(64))
    ).register();

    public static final ItemEntry<Item> AEROLITE_SHEET = REGISTRATE.item(
        "aerolite_sheet",
        (properties) -> new Item(properties.stacksTo(64))
    ).register();


    // REGISTRY HELPERS
    public static void register() {
        CreateCrystallized.LOGGER.info("Items registered");
    }
}
