package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.entity.renderer.DensiteWellEntityRenderer;
import com.nasilk.createcrystallized.client.models.DensiteWellCubeModel;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.entity.renderer.ThrownDensiteCoreRenderer;
import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.particle.custom.*;
import com.nasilk.createcrystallized.util.helper.CreateTooltipHelper;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@SuppressWarnings("SpellCheckingInspection")
@Mod(value = CreateCrystallized.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, value = Dist.CLIENT)
public class CreateCrystallizedClient {
    public CreateCrystallizedClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Custom Fluid Renders
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_VOID_SEA_SLURRY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_DENSITE_EMULSION.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_DENSITE_EMULSION.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_DRIFT_CONDENSATE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_PROPULSITE_FLURRY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_PROPULSITE_FLURRY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_OSCILLITE_SUSPENSION.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_OSCILLITE_SUSPENSION.get(), RenderType.translucent());

            // Custom Tooltips, add them in the lang file
            CreateTooltipHelper.register(ModItems.AEROLITE_SHOVEL.get());
            CreateTooltipHelper.register(ModItems.DENSITE_CORE.get());
            CreateTooltipHelper.register(ModItems.CREATIVE_FLUID_ERASER.get());
            CreateTooltipHelper.register(ModBlocks.PEBBLE.get());
            CreateTooltipHelper.register(ModBlocks.OSCILLITE_CANNON.get());
            CreateTooltipHelper.register(ModBlocks.PROPULSITE_THRUSTER.get());
            CreateTooltipHelper.register(ModBlocks.DENSITE_WELL.get());
        });
        CreateCrystallized.LOGGER.info("HELLO from client setup");
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        IClientBlockExtensions noDefaultParticles = new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                return true; // Cancel default particles
            }
        };

        // Disable Default Destroy Particles
        event.registerBlock(
            noDefaultParticles,
            ModBlocks.DENSITE_BLOCK.get(),
            ModBlocks.ENCASED_DENSITE_BLOCK.get(),
            ModBlocks.DENSITE_WELL.get(),
            ModBlocks.PROPULSITE_BLOCK.get(),
            ModBlocks.ENCASED_PROPULSITE_BLOCK.get(),
            ModBlocks.PROPULSITE_THRUSTER.get(),
            ModBlocks.OSCILLITE_BLOCK.get(),
            ModBlocks.ENCASED_OSCILLITE_BLOCK.get(),
            ModBlocks.OSCILLITE_CANNON.get(),
            ModBlocks.ENCASED_LEVITITE_BLOCK.get()
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // spinspinspinspinspinspinspinspinspinspinspinspinspin
        event.registerBlockEntityRenderer(ModBlockEntities.DENSITE_WELL.get(), DensiteWellEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_DENSITE_CORE.get(), ThrownDensiteCoreRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) { // For the densite cube and well
        event.registerLayerDefinition(
            DensiteWellCubeModel.LAYER_LOCATION,
            DensiteWellCubeModel::createBodyLayer
        );
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DENSITE_PARTICLES.get(), DensiteParticles.Provider::new);
        event.registerSpriteSet(ModParticles.PROPULSITE_PARTICLES.get(), PropulsiteParticles.Provider::new);
        event.registerSpriteSet(ModParticles.PROPULSITE_THRUSTER_FIRING_PARTICLES.get(), PropulsiteThrusterFiringParticles.Provider::new);
        event.registerSpriteSet(ModParticles.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(), PropulsiteThrusterChargingParticles.Provider::new);
        event.registerSpriteSet(ModParticles.OSCILLITE_CANNON_CHARGING_PARTICLES.get(), OscilliteCannonChargingParticles.Provider::new);
        event.registerSpriteSet(ModParticles.OSCILLITE_CANNON_FIRING_PARTICLES.get(), OscilliteCannonFiringParticles.Provider::new);
    }

    // @SubscribeEvent lets the Event Bus discover methods to call
    // @EventBusSubscriber automatically registers all static methods in the class annotated with @SubscribeEvent
}
