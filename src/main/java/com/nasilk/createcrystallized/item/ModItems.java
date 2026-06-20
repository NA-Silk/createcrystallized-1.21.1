package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.custom.ChoraIngotItem;
import com.nasilk.createcrystallized.item.custom.CreativeFluidEraserItem;
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

    public static final DeferredItem<ChoraIngotItem> AEROLITE_INGOT = ITEMS.registerItem(
            "aerolite_ingot",
            (properties) -> new ChoraIngotItem(properties.stacksTo(64))
    );

    public static final DeferredItem<ChoraIngotItem> RAW_AEROLITE = ITEMS.registerItem(
            "raw_aerolite",
            (properties) -> new ChoraIngotItem(properties.stacksTo(64))
    );

    public static final DeferredItem<ChoraIngotItem> CRUSHED_RAW_AEROLITE = ITEMS.registerItem(
            "crushed_raw_aerolite",
            (properties) -> new ChoraIngotItem(properties.stacksTo(64))
    );

    public static void register(IEventBus eventbus) {
      ITEMS.register(eventbus);
    }
}
