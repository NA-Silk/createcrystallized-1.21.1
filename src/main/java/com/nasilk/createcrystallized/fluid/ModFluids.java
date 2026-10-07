package com.nasilk.createcrystallized.fluid;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.util.setting.FluidTransformSettings;
import com.nasilk.createcrystallized.fluid.flowingfluid.TransformBaseFlowingFluid;
import com.nasilk.createcrystallized.fluid.flowingfluid.UpwardBaseFlowingFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.joml.Vector3f;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModFluids {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();


    // FLUID REGISTRATIONS
    /// VOID SEA SLURRY
    public static final FluidEntry<BaseFlowingFluid.Flowing> VOID_SEA_SLURRY = REGISTRATE.fluid(
        "void_sea_slurry",
        fluidTexture("void_sea_slurry", true),
        fluidTexture("void_sea_slurry", false),
        (properties, stillTexture, flowTexture) ->
            new BaseFluidType(stillTexture, flowTexture, null, 0xE6FFFFFF, new Vector3f(0.20f, 0.086f, 0.322f), properties)
    ).properties(properties ->
        properties.lightLevel(2).viscosity(2500).density(2500).canSwim(false)
    ).fluidProperties(properties ->
        properties.slopeFindDistance(1).levelDecreasePerBlock(1).tickRate(15)
    ).source(
        BaseFlowingFluid.Source::new
    ).block().build().bucket().build().register();

    /// DENSITE EMULSION
    public static final FluidTransformSettings DENSITE_EMULSION_SETTINGS_1 = new FluidTransformSettings(ModBlocks.DENSITE_BLOCK, 0.8f, 15, new FluidTransformSettings.YRange(-64, 319), false, false, false, false, true, Set.of(() -> Blocks.ICE, () -> Blocks.PACKED_ICE, () -> Blocks.FROSTED_ICE), Set.of(Level.OVERWORLD, Level.NETHER, Level.END), new FluidTransformSettings.LightningSettings(false, null), new FluidTransformSettings.VibrationSettings(false, null, null), Optional.of(ModParticles.DENSITE_PARTICLES), Optional.of(() -> SoundEvents.ENDER_EYE_DEATH), true);
    // public static final FluidTransformSettings DENSITE_EMULSION_SETTINGS_2 = new FluidTransformSettings(() -> Blocks.BELL, 0.8f, 15, new FluidTransformSettings.YRange(-64, 319), false, false, false, false, true, Set.of(() -> Blocks.BLUE_ICE), Set.of(Level.OVERWORLD, Level.NETHER, Level.END), new FluidTransformSettings.LightningSettings(false, null), new FluidTransformSettings.VibrationSettings(false, null, null), Optional.of(ModParticles.DENSITE_PARTICLES), Optional.of(() -> SoundEvents.ENDER_EYE_DEATH), true);
    public static final FluidEntry<TransformBaseFlowingFluid.Flowing> DENSITE_EMULSION = REGISTRATE.fluid(
        "densite_emulsion",
        fluidTexture("densite_emulsion", true),
        fluidTexture("densite_emulsion", false),
        (properties, stillTexture, flowTexture) ->
            new BaseFluidType(stillTexture, flowTexture, null, null, new Vector3f(0.141f, 0.0f, 0.259f), properties),
        (properties) -> new TransformBaseFlowingFluid.Flowing(properties, List.of(DENSITE_EMULSION_SETTINGS_1))
    ).properties(properties ->
        properties.lightLevel(2).viscosity(5000).density(5000).canSwim(false)
    ).fluidProperties(properties ->
        properties.slopeFindDistance(1).levelDecreasePerBlock(1).tickRate(25)
    ).source(properties ->
        new TransformBaseFlowingFluid.Source(properties, List.of(DENSITE_EMULSION_SETTINGS_1))
    ).block().build().bucket().build().register();

    /// DRIFT CONDENSATE
    public static final UpwardBaseFlowingFluid.AnimationSettings DRIFT_CONDENSATE_SETTINGS = new UpwardBaseFlowingFluid.AnimationSettings(10,3,1.0f,0.2d,0.4d,0.5d);
    public static final FluidEntry<UpwardBaseFlowingFluid.Flowing> DRIFT_CONDENSATE = REGISTRATE.fluid(
        "drift_condensate",
        fluidTexture("drift_condensate", true),
        fluidTexture("drift_condensate", false),
        (properties, stillTexture, flowTexture) ->
            new BaseFluidType(stillTexture, flowTexture, null, 0xAAFFFFFF, new Vector3f(1.0f, 0.867f, 0.729f), properties),
        (properties) -> new UpwardBaseFlowingFluid.Flowing(properties, DRIFT_CONDENSATE_SETTINGS)
    ).properties(properties ->
        properties.lightLevel(6).viscosity(200).density(-1000).motionScale(0.002D).temperature(250).canSwim(false)
    ).source(properties ->
        new UpwardBaseFlowingFluid.Source(properties, DRIFT_CONDENSATE_SETTINGS)
    ).block().build().bucket().build().register();

    /// PROPULSITE FLURRY
    public static final FluidTransformSettings PROPULSITE_FLURRY_SETTINGS = new FluidTransformSettings(ModBlocks.PROPULSITE_BLOCK, 1.0f, 15, new FluidTransformSettings.YRange(-64, 319), false, false, false, false, true, Set.of(), Set.of(Level.OVERWORLD, Level.NETHER, Level.END), new FluidTransformSettings.LightningSettings(true, 6), new FluidTransformSettings.VibrationSettings(false, null, null), Optional.of(ModParticles.PROPULSITE_PARTICLES), Optional.of(() -> SoundEvents.GLASS_PLACE), true);
    public static final FluidEntry<TransformBaseFlowingFluid.Flowing> PROPULSITE_FLURRY = REGISTRATE.fluid(
        "propulsite_flurry",
        fluidTexture("propulsite_flurry", true),
        fluidTexture("propulsite_flurry", false),
        (properties, stillTexture, flowTexture) ->
            new BaseFluidType(stillTexture, flowTexture, null, 0xEEFFFFFF, new Vector3f(1.0f, 0.867f, 0.729f), properties),
        (properties) -> new TransformBaseFlowingFluid.Flowing(properties, List.of(PROPULSITE_FLURRY_SETTINGS))
    ).properties(properties ->
        properties.lightLevel(8).viscosity(300).density(0).motionScale(0.03D).temperature(250).canSwim(false)
    ).fluidProperties(properties ->
        properties.slopeFindDistance(8).levelDecreasePerBlock(1).tickRate(2)
    ).source(properties ->
        new TransformBaseFlowingFluid.Source(properties, List.of(PROPULSITE_FLURRY_SETTINGS))
    ).block().build().bucket().build().register();

    /// OSCILLITE SUSPENSION
    public static final FluidTransformSettings OSCILLITE_SUSPENSION_SETTINGS = new FluidTransformSettings(ModBlocks.OSCILLITE_BLOCK, 0.01f, 15, new FluidTransformSettings.YRange(-64, 319), false, false, false, false, true, Set.of(), Set.of(Level.OVERWORLD, Level.NETHER, Level.END), new FluidTransformSettings.LightningSettings(false, null), new FluidTransformSettings.VibrationSettings(true, 6, 10), Optional.of(() -> ParticleTypes.SCULK_SOUL), Optional.of(() -> SoundEvents.GLASS_PLACE), true);
    public static final FluidEntry<TransformBaseFlowingFluid.Flowing> OSCILLITE_SUSPENSION = REGISTRATE.fluid(
        "oscillite_suspension",
        fluidTexture("oscillite_suspension", true),
        fluidTexture("oscillite_suspension", false),
        (properties, stillTexture, flowTexture) ->
            new BaseFluidType(stillTexture, flowTexture, null, 0xEEFFFFFF, new Vector3f(0.271f, 0.804f, 1.0f), properties),
        (properties) -> new TransformBaseFlowingFluid.Flowing(properties, List.of(OSCILLITE_SUSPENSION_SETTINGS))
    ).properties(properties ->
        properties.lightLevel(8).viscosity(300).density(0).motionScale(0.03D).temperature(250).canSwim(false)
    ).fluidProperties(properties ->
        properties.slopeFindDistance(8).levelDecreasePerBlock(1).tickRate(2)
    ).source(properties ->
        new TransformBaseFlowingFluid.Source(properties, List.of(OSCILLITE_SUSPENSION_SETTINGS))
    ).block().build().bucket().build().register();


    // REGISTRY HELPERS
    public static final FluidEntry<?>[] FLUIDS = {
        VOID_SEA_SLURRY,
        DENSITE_EMULSION,
        DRIFT_CONDENSATE,
        PROPULSITE_FLURRY,
        OSCILLITE_SUSPENSION
    };

    private static ResourceLocation fluidTexture(String name, boolean source) {
        String suffix = source ? "_still" : "_flow";
        return ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "fluid/" + name + suffix);
    }

    public static void register() {
        CreateCrystallized.LOGGER.info("Fluids registered");
    }
}
