package com.nasilk.createcrystallized.common;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.config.common.CreativeConfig;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.item.ModItems;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.registrate.simulated_tab.CreativeTabItemTransforms;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.function.Supplier;

public class ModCreativeModeTabs {
    private static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> CREATE_CRYSTALLIZED_TAB = REGISTRATE.simple(
        "create_crystallized_tab",
        Registries.CREATIVE_MODE_TAB,
        () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModBlocks.DENSITE_BLOCK.get()))
            .title(Component.translatable("creativetab.createcrystallized.create_crystallized_tab"))
            .build()
    );

    private static final ResourceLocation MAIN_SECTION = CreateCrystallized.getRL("main_section");
    private static final List<Supplier<Item>> TAB_ITEMS = new ArrayList<>();

    private static boolean sectionsInitialized = false;
    public static void register() {
        if (sectionsInitialized) return;
        CreativeConfig creativeConfig = ModConfigs.common().creativeConfig;

        // Buckets
        registerMainSectionFluid(ModFluids.VOID_SEA_SLURRY);
        registerMainSectionFluid(ModFluids.DENSITE_EMULSION);
        registerMainSectionFluid(ModFluids.DRIFT_CONDENSATE);
        registerMainSectionFluid(ModFluids.PROPULSITE_FLURRY);
        registerMainSectionFluid(ModFluids.OSCILLITE_SUSPENSION);

        // Transformation Items
        registerMainSectionItem(ModItems.OSCILLITE_RESONATOR);

        // Uncategorized Items
        registerMainSectionItem(ModItems.CHORA_INGOT);
        registerMainSectionItem(ModItems.CRUSHED_RAW_AEROLITE);
        registerMainSectionItem(ModItems.RAW_AEROLITE);
        registerMainSectionItem(ModItems.AEROLITE_INGOT);
        if (creativeConfig.enableUnusedItems.get().equals(true)) {
            registerMainSectionItem(ModItems.AEROLITE_SHEET);
        }
        registerMainSectionItem(ModItems.DENSITE_CORE);
        registerMainSectionItem(ModItems.AEROLITE_SHOVEL);

        // Uncategorized Blocks
        registerMainSectionBlock(ModBlocks.AEROLITE_ORE);
        registerMainSectionBlock(ModBlocks.DEEPSLATE_AEROLITE_ORE);
        registerMainSectionBlock(ModBlocks.AEROLITE_BLOCK);
        registerMainSectionBlock(ModBlocks.PROPULSITE_CRYSTAL);
        registerMainSectionBlock(ModBlocks.DENSITE_CRYSTAL);
        registerMainSectionBlock(ModBlocks.LEVITITE_CRYSTAL);

        // Echo Shard Blocks
        registerMainSectionBlock(ModBlocks.ECHO_CRYSTAL_BLOCK);
        registerMainSectionBlock(ModBlocks.BUDDING_ECHO_CRYSTAL);
        registerMainSectionBlock(ModBlocks.ECHO_CRYSTAL_CLUSTER);
        registerMainSectionBlock(ModBlocks.LARGE_ECHO_CRYSTAL_BUD);
        registerMainSectionBlock(ModBlocks.MEDIUM_ECHO_CRYSTAL_BUD);
        registerMainSectionBlock(ModBlocks.SMALL_ECHO_CRYSTAL_BUD);

        // Chora Blocks
        registerMainSectionBlock(ModBlocks.CHORA_BLOCK);
        registerMainSectionBlock(ModBlocks.CHORA_CASING);
        registerMainSectionBlock(ModBlocks.DENSE_CHORA_CASING);
        registerMainSectionBlock(ModBlocks.PROPULSED_CHORA_CASING);
        registerMainSectionBlock(ModBlocks.OSCILLATING_CHORA_CASING);
        registerMainSectionBlock(ModBlocks.LEVITATING_CHORA_CASING);

        // Crystal Blocks
        registerMainSectionBlock(ModBlocks.DENSITE_BLOCK);
        registerMainSectionBlock(ModBlocks.ENCASED_DENSITE_BLOCK);
        registerMainSectionBlock(ModBlocks.DENSITE_WELL);
        registerMainSectionBlock(ModBlocks.PROPULSITE_BLOCK);
        registerMainSectionBlock(ModBlocks.ENCASED_PROPULSITE_BLOCK);
        registerMainSectionBlock(ModBlocks.PROPULSITE_THRUSTER);
        registerMainSectionBlock(ModBlocks.OSCILLITE_BLOCK);
        registerMainSectionBlock(ModBlocks.ENCASED_OSCILLITE_BLOCK);
        registerMainSectionBlock(ModBlocks.OSCILLITE_CANNON);
        registerMainSectionBlock(ModBlocks.ENCASED_LEVITITE_BLOCK);

        // Development Items and Blocks
        if (creativeConfig.enableDevItems.get().equals(true)) {
            registerMainSectionItem(ModItems.CREATIVE_FLUID_ERASER);
            registerMainSectionItem(ModItems.CREATIVE_BAG_OF_LONGS);
            registerMainSectionBlock(ModBlocks.PEBBLE);
        }

        sectionsInitialized = true;
    }

    private static void registerMainSectionBlock(BlockEntry<?> blockEntry) {
        SimulatedRegistrate.ITEM_TO_SECTION.put(blockEntry.getId(), ModCreativeModeTabs.MAIN_SECTION);
        TAB_ITEMS.add(blockEntry::asItem);
    }

    private static void registerMainSectionItem(ItemEntry<?> itemEntry) {
        SimulatedRegistrate.ITEM_TO_SECTION.put(itemEntry.getId(), ModCreativeModeTabs.MAIN_SECTION);
        TAB_ITEMS.add(itemEntry::get);
    }

    private static void registerMainSectionFluid(FluidEntry<?> fluidEntry) {
        SimulatedRegistrate.ITEM_TO_SECTION.put(fluidEntry.getId().withSuffix("_bucket"), ModCreativeModeTabs.MAIN_SECTION);
        TAB_ITEMS.add(() -> fluidEntry.getBucket().orElseThrow());
    }

    public static void buildContents(List<ItemStack> displayItems, Set<ItemStack> searchItems) {
        // Empty first row
        displayItems.clear();
        searchItems.clear();
        for (int i = 0; i < 9; i++) displayItems.add(ItemStack.EMPTY);

        // Fill main section
        for (Supplier<Item> itemSupplier : TAB_ITEMS) {
            // Handle restrictions
            Item item = itemSupplier.get();
            if (item == null)
                continue; // Skip null
            ItemStack stack = CreativeTabItemTransforms.applyTransform(item.getDefaultInstance());
            if (CreativeTabItemTransforms.VisibilityType.INVISIBLE.has(stack.getItem()))
                continue; // Skip invisible
            if (CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.has(stack.getItem())) {
                searchItems.add(stack);
                continue; // Skip search only
            }

            // Add item and print log
            displayItems.add(stack);
            searchItems.add(stack.copy());
            CreateCrystallized.LOGGER.info(
                "Built Create Crystallized creative tab with {} display stacks and {} search stacks",
                displayItems.size(),
                searchItems.size()
            );
        }
    }

    public static void init() {
        CreateCrystallized.LOGGER.info("Creative Mode Tabs initialized");
    }
}
