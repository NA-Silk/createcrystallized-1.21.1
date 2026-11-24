package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.custom.*;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.*;

public final class ModItems {
    private static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

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
            .attributes(ShovelItem.createAttributes(ModTiers.AEROLITE, 1.5f, -3.0f))
        )
    ).register();

    public static final ItemEntry<AeroliteSwordItem> AEROLITE_SWORD = REGISTRATE.item(
            "aerolite_sword",
            (properties) -> new AeroliteSwordItem(ModTiers.AEROLITE, properties
                    .stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModTiers.AEROLITE, 2.5f, -2.4f))
            )
    ).register();

    public static final ItemEntry<AeroliteAxeItem> AEROLITE_AXE = REGISTRATE.item(
            "aerolite_axe",
            (properties) -> new AeroliteAxeItem(ModTiers.AEROLITE, properties
                    .stacksTo(1)
                    .attributes(AxeItem.createAttributes(ModTiers.AEROLITE, 5.0f, -3.0f))
            )
    ).register();

    public static final ItemEntry<AeroliteHoeItem> AEROLITE_HOE = REGISTRATE.item(
            "aerolite_hoe",
            (properties) -> new AeroliteHoeItem(ModTiers.AEROLITE, properties
                    .stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModTiers.AEROLITE, 0.0f, -3.0f))
            )
    ).register();

    public static final ItemEntry<AerolitePickaxeItem> AEROLITE_PICKAXE = REGISTRATE.item(
            "aerolite_pickaxe",
            (properties) -> new AerolitePickaxeItem(ModTiers.AEROLITE, properties
                    .stacksTo(1)
                    .attributes(PickaxeItem.createAttributes(ModTiers.AEROLITE, 1.5f, -2.8f))
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

    public static final ItemEntry<Item> AEROLITE_NUGGET = REGISTRATE.item(
            "aerolite_nugget",
            (properties) -> new Item(properties.stacksTo(64))
    ).register();

    public static final ItemEntry<Item> AEROLITE_SHEET = REGISTRATE.item(
        "aerolite_sheet",
        (properties) -> new Item(properties.stacksTo(64))
    ).register();

    public static void init() {
        CreateCrystallized.LOGGER.info("Items initialized");
    }
}
