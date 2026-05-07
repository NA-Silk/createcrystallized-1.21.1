package com.nasilk.createfluidsandfixins.fluid;

import com.nasilk.createfluidsandfixins.CreateFluidsAndFixins;
import com.nasilk.createfluidsandfixins.block.ModBlocks;
import com.nasilk.createfluidsandfixins.item.ModItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

// NOTE: FluidTransformationSettings only required if using ModBaseFlowingFluid
public class ModFluids {
    // DEFAULT
    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(BuiltInRegistries.FLUID, CreateFluidsAndFixins.MOD_ID);


    // DENSITE EMULSION
    public static final FluidTransformationSettings DENSITE_EMULSION_SETTINGS = new FluidTransformationSettings(
        0.02f, // Transform chance
        2, // Max skylight
        -64, // min Y
        319, // max Y
        false, // Require cold biome
        false, // Require rain
        false, // Require thunder
        false, // Require night
        true, // Require adjacent ice
        false, // Transform flowing fluids
        false, // Vaporize in ultrawarm dimensions
        Set.of(Level.OVERWORLD), // Allowed dimensions
        Optional.of(() -> SoundEvents.ENDER_EYE_DEATH) // Sound effect
    );
    public static final Supplier<FlowingFluid> SOURCE_DENSITE_EMULSION = FLUIDS.register(
        "source_densite_emulsion",
        () -> new ModBaseFlowingFluid.Source(
            ModFluids.DENSITE_EMULSION_PROPERTIES,
            ModBlocks.DENSITE_BLOCK,
            DENSITE_EMULSION_SETTINGS
        )
    );
    public static final Supplier<FlowingFluid> FLOWING_DENSITE_EMULSION = FLUIDS.register(
        "flowing_densite_emulsion",
        () -> new ModBaseFlowingFluid.Flowing(
            ModFluids.DENSITE_EMULSION_PROPERTIES,
            ModBlocks.DENSITE_BLOCK,
            DENSITE_EMULSION_SETTINGS
        )
    );

    public static final DeferredBlock<LiquidBlock> DENSITE_EMULSION_BLOCK = ModBlocks.BLOCKS.register(
        "densite_emulsion_block",
        () -> new LiquidBlock(ModFluids.SOURCE_DENSITE_EMULSION.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).randomTicks().noLootTable())
    );
    public static final DeferredItem<Item> DENSITE_EMULSION_BUCKET = ModItems.ITEMS.registerItem(
        "densite_emulsion_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_DENSITE_EMULSION.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final ModBaseFlowingFluid.Properties DENSITE_EMULSION_PROPERTIES = new ModBaseFlowingFluid.Properties(
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
        () -> new BaseFlowingFluid.Source(ModFluids.VOID_SEA_SLURRY_PROPERTIES)
    );
    public static final Supplier<FlowingFluid> FLOWING_VOID_SEA_SLURRY = FLUIDS.register(
        "flowing_void_sea_slurry",
        () -> new BaseFlowingFluid.Flowing(ModFluids.VOID_SEA_SLURRY_PROPERTIES)
    );

    public static final DeferredBlock<LiquidBlock> VOID_SEA_SLURRY_BLOCK = ModBlocks.BLOCKS.register(
        "void_sea_slurry_block",
        () -> new LiquidBlock(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );
    public static final DeferredItem<Item> VOID_SEA_SLURRY_BUCKET = ModItems.ITEMS.registerItem(
        "void_sea_slurry_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final BaseFlowingFluid.Properties VOID_SEA_SLURRY_PROPERTIES = new BaseFlowingFluid.Properties(
        ModFluidTypes.VOID_SEA_SLURRY_FLUID_TYPE,
        SOURCE_VOID_SEA_SLURRY,
        FLOWING_VOID_SEA_SLURRY
    )
        .slopeFindDistance(1) // Horizontal searching rate (flow speed)
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(15) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.VOID_SEA_SLURRY_BLOCK)
        .bucket(ModFluids.VOID_SEA_SLURRY_BUCKET);


    // DRIFT CONDENSATE
    public static final Supplier<FlowingFluid> SOURCE_DRIFT_CONDENSATE = FLUIDS.register(
        "source_drift_condensate",
        () -> new UpwardBaseFlowingFluid.Source(ModFluids.DRIFT_CONDENSATE_PROPERTIES)
    );
    public static final Supplier<FlowingFluid> FLOWING_DRIFT_CONDENSATE = FLUIDS.register(
        "flowing_drift_condensate",
        () -> new UpwardBaseFlowingFluid.Flowing(ModFluids.DRIFT_CONDENSATE_PROPERTIES)
    );

    public static final DeferredBlock<LiquidBlock> DRIFT_CONDENSATE_BLOCK = ModBlocks.BLOCKS.register(
        "drift_condensate_block",
        () -> new LiquidBlock(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
    );
    public static final DeferredItem<Item> DRIFT_CONDENSATE_BUCKET = ModItems.ITEMS.registerItem(
        "drift_condensate_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final UpwardBaseFlowingFluid.Properties DRIFT_CONDENSATE_PROPERTIES = new UpwardBaseFlowingFluid.Properties(
        ModFluidTypes.DRIFT_CONDENSATE_FLUID_TYPE,
        SOURCE_DRIFT_CONDENSATE,
        FLOWING_DRIFT_CONDENSATE
    )
        .slopeFindDistance(1) // Horizontal searching rate (flow speed)
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(15) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.DRIFT_CONDENSATE_BLOCK)
        .bucket(ModFluids.DRIFT_CONDENSATE_BUCKET);


    // NEXT FLUID ...

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
