package com.nasilk.createcrystallized.fluid;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;
import java.util.function.Supplier;

@SuppressWarnings({"SpellCheckingInspection", "GrazieInspectionRunner", "CommentedOutCode"})
public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreateCrystallized.MOD_ID);


    // VOID SEA SLURRY
    public static final Supplier<FluidType> VOID_SEA_SLURRY_FLUID_TYPE = registerFluidType(
        "void_sea_slurry_fluid_type",
        new BaseFluidType(
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/source_void_sea_slurry"),
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/flowing_void_sea_slurry"),
            null,
            0xE6FFFFFF, // 0xAARRGGBB (ARGB format)
            new Vector3f(0.20f, 0.086f, 0.322f), // Fog color
            FluidType.Properties.create()
                .lightLevel(2) // Glow?
                .viscosity(2500) // Physics related (higher = heavier)
                .density(2500) // Physics related (higher = heavier), upside down pipe flow if negative
                .canSwim(false)
        )
    );


    // DENSITE EMULSION
    public static final Supplier<FluidType> DENSITE_EMULSION_FLUID_TYPE = registerFluidType(
        "densite_emulsion_fluid_type",
        new BaseFluidType(
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/source_densite_emulsion"),
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/flowing_densite_emulsion"),
            null,
            null, // 0xAARRGGBB (ARGB format)
            new Vector3f(0.141f, 0.0f, 0.259f), // Fog color
            FluidType.Properties.create()
                .lightLevel(2) // Glow?
                .viscosity(5000) // Physics related (higher = heavier)
                .density(5000) // Physics related (higher = heavier), upside down pipe flow if negative
                .canSwim(false)
        )
    );


    // DRIFT CONDENSATE
    public static final Supplier<FluidType> DRIFT_CONDENSATE_FLUID_TYPE = registerFluidType(
        "drift_condensate_fluid_type",
        new BaseFluidType(
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/source_drift_condensate"),
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/flowing_drift_condensate"),
            null,
            0xAAFFFFFF, // 0xAARRGGBB (ARGB format)
            new Vector3f(1.0f, 0.867f, 0.729f),  // Fog color
            FluidType.Properties.create()
                .lightLevel(6) // Glow?
                .viscosity(200) // Physics related (higher = heavier)
                .density(-1000) // Physics related (higher = heavier), upside down pipe flow if negative
                .motionScale(0.002D)
                .temperature(250)
                .canSwim(false)
        )
    );


    // PROPULSITE FLURRY
    public static final Supplier<FluidType> PROPULSITE_FLURRY_FLUID_TYPE = registerFluidType(
        "propulsite_flurry_fluid_type",
        new BaseFluidType(
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/source_propulsite_flurry"),
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/flowing_propulsite_flurry"),
            null,
            0xEEFFFFFF, // 0xAARRGGBB (ARGB format)
            new Vector3f(1.0f, 0.867f, 0.729f),  // Fog color
            FluidType.Properties.create()
                .lightLevel(8) // Glow?
                .viscosity(300) // Physics related (higher = heavier)
                .density(0) // Physics related (higher = heavier), upside down pipe flow
                .motionScale(0.03D)
                .temperature(250)
                .canSwim(false)
        )
    );


    // OSCILLITE SUSPENSION
    public static final Supplier<FluidType> OSCILLITE_SUSPENSION_FLUID_TYPE = registerFluidType(
        "oscillite_suspension_fluid_type",
        new BaseFluidType(
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/source_oscillite_suspension"),
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "block/flowing_oscillite_suspension"),
            null,
            0xEEFFFFFF, // 0xAARRGGBB (ARGB format)
            new Vector3f(0.271f, 0.804f, 1.0f),  // Fog color
            FluidType.Properties.create()
                .lightLevel(8) // Glow?
                .viscosity(300) // Physics related (higher = heavier)
                .density(0) // Physics related (higher = heavier), upside down pipe flow
                .motionScale(0.03D)
                .temperature(250)
                .canSwim(false)
        )
    );


    private static Supplier<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }

    // public static final ResourceLocation WATER_STILL_RL = ResourceLocation.parse("block/water_still");
    // public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.parse("block/water_flow");
    // public static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.parse("block/water_overlay");
}
