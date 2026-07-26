package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.custom.ChoraIngotItem;
import com.nasilk.createcrystallized.item.custom.CreativeFluidEraserItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateCrystallized.MOD_ID);

    public static final DeferredItem<CreativeFluidEraserItem> CREATIVE_FLUID_ERASER = ITEMS.registerItem(
        "creative_fluid_eraser",
        (properties) -> new CreativeFluidEraserItem(properties.stacksTo(1))
    );

    public static final DeferredItem<ChoraIngotItem> CHORA_INGOT = ITEMS.registerItem(
        "chora_ingot",
        (properties) -> new ChoraIngotItem(properties.stacksTo(64))
    );

    public static final DeferredItem<Item> AEROLITE_INGOT = ITEMS.register(
            "aerolite_ingot",
            () -> new Item(new Item.Properties().stacksTo(64))
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
