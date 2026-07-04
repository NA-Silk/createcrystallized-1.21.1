package com.nasilk.createcrystallized.fluid;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.FluidTransformationSettings;
import com.nasilk.createcrystallized.fluid.flowingfluid.TransformBaseFlowingFluid;
import com.nasilk.createcrystallized.fluid.flowingfluid.UpwardBaseFlowingFluid;
import com.nasilk.createcrystallized.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
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

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(BuiltInRegistries.FLUID, CreateCrystallized.MOD_ID);


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


    // DENSITE EMULSION
    public static final FluidTransformationSettings DENSITE_EMULSION_SETTINGS = new FluidTransformationSettings(
        0.8f, // Transform rate [0.0f, 1,0f]
        15, // Max skylight
        new FluidTransformationSettings.YRange(-64, 319), // y level range
        false, // Require cold biome
        false, // Require rain
        false, // Require thunder
        false, // Require night
        true, // Require source block
        Set.of( // Require adjacent blocks
            () -> Blocks.ICE, // Vanilla blocks must be clearly supplied
            () -> Blocks.PACKED_ICE,
            () -> Blocks.BLUE_ICE,
            () -> Blocks.FROSTED_ICE
        ),
        new FluidTransformationSettings.LightningSettings(false, null), // Lightning requirements
        new FluidTransformationSettings.VibrationSettings(false, null, null), // Vibration requirements
        Set.of(Level.OVERWORLD, Level.NETHER, Level.END), // Allowed dimensions
        Optional.of(ModParticles.DENSITE_PARTICLES), // Particle effect
        Optional.of(() -> SoundEvents.ENDER_EYE_DEATH), // Sound effect
        true // Chain catalyzes
    );
    public static final Supplier<FlowingFluid> SOURCE_DENSITE_EMULSION = FLUIDS.register(
        "source_densite_emulsion",
        () -> new TransformBaseFlowingFluid.Source(
            ModFluids.DENSITE_EMULSION_PROPERTIES,
            ModBlocks.DENSITE_BLOCK,
            DENSITE_EMULSION_SETTINGS
        )
    );
    public static final Supplier<FlowingFluid> FLOWING_DENSITE_EMULSION = FLUIDS.register(
        "flowing_densite_emulsion",
        () -> new TransformBaseFlowingFluid.Flowing(
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

    public static final TransformBaseFlowingFluid.Properties DENSITE_EMULSION_PROPERTIES = new TransformBaseFlowingFluid.Properties(
        ModFluidTypes.DENSITE_EMULSION_FLUID_TYPE,
        SOURCE_DENSITE_EMULSION,
        FLOWING_DENSITE_EMULSION
    )
        .slopeFindDistance(1) // Horizontal searching rate
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(25) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.DENSITE_EMULSION_BLOCK)
        .bucket(ModFluids.DENSITE_EMULSION_BUCKET);


    // DRIFT CONDENSATE
    public static final Supplier<FlowingFluid> SOURCE_DRIFT_CONDENSATE = FLUIDS.register(
        "source_drift_condensate",
        () -> new UpwardBaseFlowingFluid.Source(ModFluids.DRIFT_CONDENSATE_PROPERTIES)
            .setFlowAnimationOptions(10,3,0.2,0.4,0.5,1.0f)
    );
    public static final Supplier<FlowingFluid> FLOWING_DRIFT_CONDENSATE = FLUIDS.register(
        "flowing_drift_condensate",
        () -> new UpwardBaseFlowingFluid.Flowing(ModFluids.DRIFT_CONDENSATE_PROPERTIES)
            .setFlowAnimationOptions(10,3,0.2,0.4,0.8,1.0f)
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
        .block(ModFluids.DRIFT_CONDENSATE_BLOCK)
        .bucket(ModFluids.DRIFT_CONDENSATE_BUCKET);


    // PROPULSITE FLURRY
    public static final FluidTransformationSettings PROPULSITE_FLURRY_SETTINGS = new FluidTransformationSettings(
        1.0f, // Transform rate [0.0f, 1,0f]
        15, // Max skylight
        new FluidTransformationSettings.YRange(-64, 319), // y level range
        false, // Require cold biome
        false, // Require rain
        false, // Require thunder
        false, // Require night
        true, // Require source block
        Set.of(), // Require adjacent blocks
        new FluidTransformationSettings.LightningSettings(true, 6), // Lightning requirements
        new FluidTransformationSettings.VibrationSettings(false, null, null), // Vibration requirements
        Set.of(Level.OVERWORLD, Level.NETHER, Level.END), // Allowed dimensions
        Optional.of(ModParticles.PROPULSITE_PARTICLES), // Particle effect
        Optional.of(() -> SoundEvents.GLASS_PLACE), // Sound effect
        true // Chain catalyzes
    );
    public static final Supplier<FlowingFluid> SOURCE_PROPULSITE_FLURRY = FLUIDS.register(
        "source_propulsite_flurry",
        () -> new TransformBaseFlowingFluid.Source(
            ModFluids.PROPULSITE_FLURRY_PROPERTIES,
            ModBlocks.PROPULSITE_BLOCK, //had to slightly change this because propulsite is no longer a deferred block -pebb // Now it is lol -na
            PROPULSITE_FLURRY_SETTINGS
        )
    );
    public static final Supplier<FlowingFluid> FLOWING_PROPULSITE_FLURRY = FLUIDS.register(
        "flowing_propulsite_flurry",
        () -> new TransformBaseFlowingFluid.Flowing(
            ModFluids.PROPULSITE_FLURRY_PROPERTIES,
            ModBlocks.PROPULSITE_BLOCK, //had to slightly change this because propulsite is no longer a deferred block -pebb // Now it is lol -na
            PROPULSITE_FLURRY_SETTINGS
        )
    );

    public static final DeferredBlock<LiquidBlock> PROPULSITE_FLURRY_BLOCK = ModBlocks.BLOCKS.register(
        "propulsite_flurry_block",
        () -> new LiquidBlock(ModFluids.SOURCE_PROPULSITE_FLURRY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).randomTicks().noLootTable())
    );
    public static final DeferredItem<Item> PROPULSITE_FLURRY_BUCKET = ModItems.ITEMS.registerItem(
        "propulsite_flurry_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_PROPULSITE_FLURRY.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final TransformBaseFlowingFluid.Properties PROPULSITE_FLURRY_PROPERTIES = new TransformBaseFlowingFluid.Properties(
        ModFluidTypes.PROPULSITE_FLURRY_FLUID_TYPE,
        SOURCE_PROPULSITE_FLURRY,
        FLOWING_PROPULSITE_FLURRY
    )
        .slopeFindDistance(8) // Horizontal searching rate
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(2) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.PROPULSITE_FLURRY_BLOCK)
        .bucket(ModFluids.PROPULSITE_FLURRY_BUCKET);


    // OSCILLITE SUSPENSION
    public static final FluidTransformationSettings OSCILLITE_SUSPENSION_SETTINGS = new FluidTransformationSettings(
        0.01f, // Transform rate [0.0f, 1,0f]
        15, // Max skylight
        new FluidTransformationSettings.YRange(-64, 319), // y level range
        false, // Require cold biome
        false, // Require rain
        false, // Require thunder
        false, // Require night
        true, // Require source block
        Set.of(), // Require adjacent blocks
        new FluidTransformationSettings.LightningSettings(false, null), // Lightning requirements
        new FluidTransformationSettings.VibrationSettings(true, 6, 10), // Vibration requirements
        Set.of(Level.OVERWORLD, Level.NETHER, Level.END), // Allowed dimensions
        Optional.of(() -> ParticleTypes.SCULK_SOUL), // Particle effect
        Optional.of(() -> SoundEvents.GLASS_PLACE), // Sound effect
        true // Chain catalyzes
    );
    public static final Supplier<FlowingFluid> SOURCE_OSCILLITE_SUSPENSION = FLUIDS.register(
        "source_oscillite_suspension",
        () -> new TransformBaseFlowingFluid.Source(
            ModFluids.OSCILLITE_SUSPENSION_PROPERTIES,
            ModBlocks.OSCILLITE_BLOCK,
            OSCILLITE_SUSPENSION_SETTINGS
        )
    );
    public static final Supplier<FlowingFluid> FLOWING_OSCILLITE_SUSPENSION = FLUIDS.register(
        "flowing_oscillite_suspension",
        () -> new TransformBaseFlowingFluid.Flowing(
            ModFluids.OSCILLITE_SUSPENSION_PROPERTIES,
            ModBlocks.OSCILLITE_BLOCK,
            OSCILLITE_SUSPENSION_SETTINGS
        )
    );

    public static final DeferredBlock<LiquidBlock> OSCILLITE_SUSPENSION_BLOCK = ModBlocks.BLOCKS.register(
        "oscillite_suspension_block",
        () -> new LiquidBlock(ModFluids.SOURCE_OSCILLITE_SUSPENSION.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).randomTicks().noLootTable())
    );
    public static final DeferredItem<Item> OSCILLITE_SUSPENSION_BUCKET = ModItems.ITEMS.registerItem(
        "oscillite_suspension_bucket",
        properties -> new BucketItem(ModFluids.SOURCE_OSCILLITE_SUSPENSION.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final TransformBaseFlowingFluid.Properties OSCILLITE_SUSPENSION_PROPERTIES = new TransformBaseFlowingFluid.Properties(
        ModFluidTypes.OSCILLITE_SUSPENSION_FLUID_TYPE,
        SOURCE_OSCILLITE_SUSPENSION,
        FLOWING_OSCILLITE_SUSPENSION
    )
        .slopeFindDistance(8) // Horizontal searching rate
        .levelDecreasePerBlock(1) // Spread distance
        .tickRate(2) // Spread rate (water ~5, inverted scale)
        .block(ModFluids.OSCILLITE_SUSPENSION_BLOCK)
        .bucket(ModFluids.OSCILLITE_SUSPENSION_BUCKET);


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
