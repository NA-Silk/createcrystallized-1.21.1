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

    /** ADVANCED ITEMS */
    public static final DeferredItem<Item> CREATIVE_FLUID_ERASER = ITEMS.registerItem(
        "creative_fluid_eraser",
        (properties) -> new CreativeFluidEraserItem(properties.stacksTo(1))
    );

    public static final DeferredItem<Item> AEROLITE_SHOVEL = ITEMS.registerItem(
        "aerolite_shovel",
        (properties) -> new AeroliteShovelItem(AEROLITE_TIER, properties.stacksTo(1))
    );

    public static final DeferredItem<Item> CREATIVE_BAG_OF_LONGS = ITEMS.registerItem(
        "creative_bag_of_longs",
        (properties) -> new CreativeBagOfLongsItem(properties.stacksTo(1))
    );

    public static final DeferredItem<Item> CHORA_INGOT = ITEMS.registerItem( //TODO move transformation related things to recipies using creates own system
        "chora_ingot",
        (properties) -> new TransformItem(
            properties.stacksTo(64),
            new HashMap<>(Map.of(Blocks.GLASS, ModBlocks.CHORA_CASING.get()))
        )
    );

    // Credit to @Eevneon from the Create Aeronautics Discord for the Sprite! // TODO move transformation related things to recipies using creates own system
    public static final DeferredItem<Item> OSCILLITE_RESONATOR = ITEMS.registerItem(
        "oscillite_resonator",
        (properties) -> new TransformItem(
            properties.stacksTo(1),
            new HashMap<>(Map.of(ModBlocks.ENCASED_OSCILLITE_BLOCK.get(), ModBlocks.OSCILLITE_CANNON.get()))
        )
    );

    /** CRAFTING ITEMS */
    public static final DeferredItem<Item> DENSITE_CORE = ITEMS.registerItem(
            "densite_core",
            (properties) -> new DensiteCoreItem(properties.stacksTo(16))
    );

    public static final DeferredItem<Item> AEROLITE_INGOT = ITEMS.registerItem(
            "aerolite_ingot",
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

    public static final DeferredItem<Item> AEROLITE_SHEET = ITEMS.registerItem(
            "aerolite_sheet",
            (properties) -> new Item(properties.stacksTo(64))
    );

    public static void register(IEventBus eventbus) {
      ITEMS.register(eventbus);
    }
}
