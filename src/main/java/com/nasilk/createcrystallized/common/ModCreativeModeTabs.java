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

    public static final ResourceLocation CRYSTALLIZED_SECTION = CreateCrystallized.getRL("crystallized_section");
    private static final List<Supplier<Item>> TAB_ITEMS = new ArrayList<>();

    private static boolean sectionsInitialized = false;
    public static void register() {
        if (sectionsInitialized) return;
        CreativeConfig creativeConfig = ModConfigs.common().creativeConfig;

        // Fluid Buckets
        registerFluid(ModFluids.VOID_SEA_SLURRY);
        registerFluid(ModFluids.DENSITE_EMULSION);
        registerFluid(ModFluids.DRIFT_CONDENSATE);
        registerFluid(ModFluids.PROPULSITE_FLURRY);
        registerFluid(ModFluids.OSCILLITE_SUSPENSION);

        // Aerolite Items and Blocks
        registerBlock(ModBlocks.AEROLITE_ORE);
        registerBlock(ModBlocks.DEEPSLATE_AEROLITE_ORE);
        registerItem(ModItems.RAW_AEROLITE);
        registerItem(ModItems.CRUSHED_RAW_AEROLITE);
        registerItem(ModItems.AEROLITE_INGOT);
        if (creativeConfig.enableUnusedItems.get().equals(true)) {
            registerItem(ModItems.AEROLITE_SHEET);
        }
        registerBlock(ModBlocks.AEROLITE_BLOCK);
        registerItem(ModItems.AEROLITE_SHOVEL);

        // Chora Items and Blocks
        registerItem(ModItems.CHORA_INGOT);
        registerBlock(ModBlocks.CHORA_BLOCK);
        registerBlock(ModBlocks.CHORA_CASING);
        registerBlock(ModBlocks.DENSE_CHORA_CASING);
        registerBlock(ModBlocks.PROPULSED_CHORA_CASING);
        registerBlock(ModBlocks.OSCILLATING_CHORA_CASING);
        registerBlock(ModBlocks.LEVITATING_CHORA_CASING);

        // Crystals and Echo Shard Blocks
        registerBlock(ModBlocks.DENSITE_CRYSTAL);
        registerBlock(ModBlocks.PROPULSITE_CRYSTAL);
        registerBlock(ModBlocks.ECHO_CRYSTAL_BLOCK);
        registerBlock(ModBlocks.BUDDING_ECHO_CRYSTAL);
        registerBlock(ModBlocks.ECHO_CRYSTAL_CLUSTER);
        registerBlock(ModBlocks.LARGE_ECHO_CRYSTAL_BUD);
        registerBlock(ModBlocks.MEDIUM_ECHO_CRYSTAL_BUD);
        registerBlock(ModBlocks.SMALL_ECHO_CRYSTAL_BUD);
        registerBlock(ModBlocks.LEVITITE_CRYSTAL);

        // Crystal Crafting Items
        registerItem(ModItems.DENSITE_CORE);
        registerItem(ModItems.OSCILLITE_RESONATOR);

        // Crystal Blocks
        registerBlock(ModBlocks.DENSITE_BLOCK);
        registerBlock(ModBlocks.PROPULSITE_BLOCK);
        registerBlock(ModBlocks.OSCILLITE_BLOCK);

        // Encased Crystal Blocks
        registerBlock(ModBlocks.ENCASED_DENSITE_BLOCK);
        registerBlock(ModBlocks.ENCASED_PROPULSITE_BLOCK);
        registerBlock(ModBlocks.ENCASED_OSCILLITE_BLOCK);
        registerBlock(ModBlocks.ENCASED_LEVITITE_BLOCK);

        // Machined Crystal Blocks
        registerBlock(ModBlocks.DENSITE_WELL);
        registerBlock(ModBlocks.PROPULSITE_THRUSTER);
        registerBlock(ModBlocks.OSCILLITE_CANNON);

        // Special Blocks
        registerBlock(ModBlocks.PEBBLE);

        // Development Items
        if (creativeConfig.enableDevItems.get().equals(true)) {
            registerItem(ModItems.CREATIVE_FLUID_ERASER);
            registerItem(ModItems.CREATIVE_BAG_OF_LONGS);
        }

        sectionsInitialized = true;
    }

    private static void registerBlock(BlockEntry<?> blockEntry) {
        SimulatedRegistrate.ITEM_TO_SECTION.put(blockEntry.getId(), ModCreativeModeTabs.CRYSTALLIZED_SECTION);
        TAB_ITEMS.add(blockEntry::asItem);
    }

    private static void registerItem(ItemEntry<?> itemEntry) {
        SimulatedRegistrate.ITEM_TO_SECTION.put(itemEntry.getId(), ModCreativeModeTabs.CRYSTALLIZED_SECTION);
        TAB_ITEMS.add(itemEntry::get);
    }

    private static void registerFluid(FluidEntry<?> fluidEntry) {
        SimulatedRegistrate.ITEM_TO_SECTION.put(fluidEntry.getId().withSuffix("_bucket"), ModCreativeModeTabs.CRYSTALLIZED_SECTION);
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
