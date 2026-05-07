package com.nasilk.createfluidsandfixins.fluid;

import com.nasilk.createfluidsandfixins.CreateFluidsAndFixins;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    /* DEFAULT
     * public static final ResourceLocation WATER_STILL_RL = ResourceLocation.parse("block/water_still");
     * public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.parse("block/water_flow");
     * public static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.parse("block/water_overlay");
     */

    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CreateFluidsAndFixins.MOD_ID);


    // DENSITE EMULSION
    public static final ResourceLocation DENSITE_STILL_RL =
        ResourceLocation.fromNamespaceAndPath(
            CreateFluidsAndFixins.MOD_ID,
            "block/source_densite_emulsion"
        );
    public static final ResourceLocation DENSITE_FLOWING_RL =
        ResourceLocation.fromNamespaceAndPath(
            CreateFluidsAndFixins.MOD_ID,
            "block/flowing_densite_emulsion"
        );
    public static final Supplier<FluidType> DENSITE_EMULSION_FLUID_TYPE =
        registerFluidType(
            "densite_emulsion_fluid_type",
            new BaseFluidType(
                DENSITE_STILL_RL,
                DENSITE_FLOWING_RL,
                null,
                null,
                new Vector3f(0.141f, 0.0f, 0.259f), // Fog color
                FluidType.Properties.create()
                    .lightLevel(1) // Glow?
                    .viscosity(5000) // Physics related (higher = heavier)
                    .density(5000) // Physics related (higher = heavier)
                    .canSwim(false)
            )
        );


    // VOID SEA SLURRY
    public static final ResourceLocation VOID_SEA_SLURRY_STILL_RL =
        ResourceLocation.fromNamespaceAndPath(
            CreateFluidsAndFixins.MOD_ID,
            "block/source_void_sea_slurry" // OKAY FINE
        );
    public static final ResourceLocation VOID_SEA_SLURRY_FLOWING_RL =
        ResourceLocation.fromNamespaceAndPath(
            CreateFluidsAndFixins.MOD_ID,
            "block/flowing_void_sea_slurry" // DON'T YELL AT ME
        );
    public static final Supplier<FluidType> VOID_SEA_SLURRY_FLUID_TYPE =
        registerFluidType(
            "void_sea_slurry_fluid_type",
            new BaseFluidType(
                VOID_SEA_SLURRY_STILL_RL,
                VOID_SEA_SLURRY_FLOWING_RL,
                null,
                null,
                new Vector3f(0.141f, 0.0f, 0.259f), // Fog color
                FluidType.Properties.create()
                    .lightLevel(2) // Glow?
                    .viscosity(1500) // Physics related (higher = heavier)
                    .density(1500) // Physics related (higher = heavier)
                    .canSwim(false)
            )
        );

    private static Supplier<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }


    // DRIFT CONDENSATE
    public static final ResourceLocation DRIFT_STILL_RL =
            ResourceLocation.fromNamespaceAndPath(
                    CreateFluidsAndFixins.MOD_ID,
                    "block/source_drift_condensate"
            );
    public static final ResourceLocation DRIFT_FLOWING_RL =
            ResourceLocation.fromNamespaceAndPath(
                    CreateFluidsAndFixins.MOD_ID,
                    "block/flowing_drift_condensate"
            );
    public static final Supplier<FluidType> DRIFT_CONDENSATE_FLUID_TYPE =
        registerFluidType(
            "drift_condensate_fluid_type",
            new BaseFluidType(
                DRIFT_STILL_RL,
                DRIFT_FLOWING_RL,
                null,
                null,
                new Vector3f(0.95f, 0.78f, 0.68f),  // Fog color
                FluidType.Properties.create()
                    .lightLevel(6) // Glow?
                    .viscosity(200) // Physics related (higher = heavier)
                    .density(-1000) // Physics related (higher = heavier)
                    .motionScale(0.002)
                    .temperature(250)
                    .canSwim(false)
            )
        );


    // NEXT FLUID ...

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}
