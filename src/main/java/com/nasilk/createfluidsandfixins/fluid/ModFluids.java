package com.nasilk.createfluidsandfixins.fluid;

import com.nasilk.createfluidsandfixins.CreateFluidsAndFixins;
import com.nasilk.createfluidsandfixins.block.ModBlocks;
import com.nasilk.createfluidsandfixins.item.ModItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModFluids {
    // DEFAULT
    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(BuiltInRegistries.FLUID, CreateFluidsAndFixins.MOD_ID);


    // DENSITE EMULSION
    public static final Supplier<FlowingFluid> SOURCE_DENSITE_EMULSION = FLUIDS.register(
        "source_densite_emulsion",
        () -> new BaseFlowingFluid.Source(ModFluids.DENSITE_EMULSION_PROPERTIES));
    public static final Supplier<FlowingFluid> FLOWING_DENSITE_EMULSION = FLUIDS.register(
        "flowing_densite_emulsion",
        () -> new BaseFlowingFluid.Flowing(ModFluids.DENSITE_EMULSION_PROPERTIES));
    public static final DeferredBlock<LiquidBlock> DENSITE_EMULSION_BLOCK = ModBlocks.BLOCKS.register(
        "densite_emulsion_block",
        () -> new LiquidBlock(ModFluids.SOURCE_DENSITE_EMULSION.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredItem<Item> DENSITE_EMULSION_BUCKET = ModItems.ITEMS.registerItem(
        "densite_emulsion_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_DENSITE_EMULSION.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties DENSITE_EMULSION_PROPERTIES = new BaseFlowingFluid.Properties(
        ModFluidTypes.DENSITE_EMULSION_FLUID_TYPE,
        SOURCE_DENSITE_EMULSION,
        FLOWING_DENSITE_EMULSION
    )
        .slopeFindDistance(1) // Horizontal searching rate
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(25) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.DENSITE_EMULSION_BLOCK)
        .bucket(ModFluids.DENSITE_EMULSION_BUCKET);


    // VOID SEA SLURRY
    public static final Supplier<FlowingFluid> SOURCE_VOID_SEA_SLURRY = FLUIDS.register(
        "source_void_sea_slurry",
        () -> new BaseFlowingFluid.Source(ModFluids.VOID_SEA_SLURRY_PROPERTIES));
    public static final Supplier<FlowingFluid> FLOWING_VOID_SEA_SLURRY = FLUIDS.register(
        "flowing_void_sea_slurry",
        () -> new BaseFlowingFluid.Flowing(ModFluids.VOID_SEA_SLURRY_PROPERTIES));
    public static final DeferredBlock<LiquidBlock> VOID_SEA_SLURRY_BLOCK = ModBlocks.BLOCKS.register(
        "void_sea_slurry_block",
        () -> new LiquidBlock(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredItem<Item> VOID_SEA_SLURRY_BUCKET = ModItems.ITEMS.registerItem(
        "void_sea_slurry_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties VOID_SEA_SLURRY_PROPERTIES = new BaseFlowingFluid.Properties(
        ModFluidTypes.VOID_SEA_SLURRY_FLUID_TYPE,
        SOURCE_VOID_SEA_SLURRY,
        FLOWING_VOID_SEA_SLURRY
    )
        .slopeFindDistance(5) // Horizontal searching rate (flow speed)
        .levelDecreasePerBlock(5) // Spread distance
        .tickRate(15) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.VOID_SEA_SLURRY_BLOCK)
        .bucket(ModFluids.VOID_SEA_SLURRY_BUCKET);


    // DRIFT CONDENSATE
    public static final Supplier<FlowingFluid> SOURCE_DRIFT_CONDENSATE = FLUIDS.register(
        "source_drift_condensate",
        () -> new BaseFlowingFluid.Source(ModFluids.DRIFT_CONDENSATE_PROPERTIES));
    public static final Supplier<FlowingFluid> FLOWING_DRIFT_CONDENSATE = FLUIDS.register(
        "flowing_drift_condensate",
        () -> new BaseFlowingFluid.Flowing(ModFluids.DRIFT_CONDENSATE_PROPERTIES));
    public static final DeferredBlock<LiquidBlock> DRIFT_CONDENSATE_BLOCK = ModBlocks.BLOCKS.register(
        "drift_condensate_block",
        () -> new LiquidBlock(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredItem<Item> DRIFT_CONDENSATE_BUCKET = ModItems.ITEMS.registerItem(
        "drift_condensate_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties DRIFT_CONDENSATE_PROPERTIES = new BaseFlowingFluid.Properties(
        ModFluidTypes.DRIFT_CONDENSATE_FLUID_TYPE,
        SOURCE_DRIFT_CONDENSATE,
        FLOWING_DRIFT_CONDENSATE
    )
        .slopeFindDistance(1) // Horizontal searching rate (flow speed)
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(10) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.DRIFT_CONDENSATE_BLOCK)
        .bucket(ModFluids.DRIFT_CONDENSATE_BUCKET);


    // NEXT FLUID ...

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
