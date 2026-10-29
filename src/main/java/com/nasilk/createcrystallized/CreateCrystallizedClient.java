package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.client.ModSpriteShifts;
import com.nasilk.createcrystallized.client.renderer.DensiteWellEntityRenderer;
import com.nasilk.createcrystallized.client.model.DensiteWellCubeModel;
import com.nasilk.createcrystallized.fluid.BaseFluidType;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.client.renderer.ThrownDensiteCoreRenderer;
import com.nasilk.createcrystallized.item.ModItemEntities;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.client.particle.*;
import com.nasilk.createcrystallized.client.ponder.ModPonderPlugin;
import com.nasilk.createcrystallized.util.helper.CreateTooltipHelper;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
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
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import static com.nasilk.createcrystallized.fluid.ModFluids.FLUIDS;

@Mod(value = CreateCrystallized.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, value = Dist.CLIENT)
public class CreateCrystallizedClient {
    public CreateCrystallizedClient(ModContainer container) {
        // Open Catnip's GUI instead of the default one
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (modContainer, parentScreen) -> new BaseConfigScreen(parentScreen, CreateCrystallized.MOD_ID)
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Static initializations
            ModSpriteShifts.init();

            // Custom Fluid Renders
            for (FluidEntry<?> fluid : FLUIDS) {
                ItemBlockRenderTypes.setRenderLayer(fluid.getSource(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(fluid.get(), RenderType.translucent());
            }

            // Custom Tooltips, add them in the lang file
            CreateTooltipHelper.registerSimpleTooltip(ModItems.AEROLITE_SHOVEL);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.DENSITE_CORE);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.CREATIVE_FLUID_ERASER);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.CREATIVE_BAG_OF_LONGS);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.PEBBLE);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.OSCILLITE_CANNON);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.PROPULSITE_THRUSTER);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.DENSITE_WELL);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.DENSITE_BLOCK);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.PROPULSITE_BLOCK);
            CreateTooltipHelper.registerGoggleTooltip(ModBlocks.OSCILLITE_BLOCK, 0);

            // Ponders
            PonderIndex.addPlugin(new ModPonderPlugin());
        });
        //CreateCrystallized.LOGGER.info("HELLO from client setup");
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Register Fluid Textures
        for (FluidEntry<?> fluid : FLUIDS) {
            BaseFluidType type = (BaseFluidType) fluid.getType();
            event.registerFluidType(type.getClientExtensions(), type);
        }

        // Disable Default Destroy Particles
        IClientBlockExtensions noDefaultParticles = new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                return true; // Cancel default particles
            }
        };
        event.registerBlock(noDefaultParticles,
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
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // Spin
        event.registerBlockEntityRenderer(ModBlockEntities.DENSITE_WELL.get(), DensiteWellEntityRenderer::new);
        event.registerEntityRenderer(ModItemEntities.THROWN_DENSITE_CORE.get(), ThrownDensiteCoreRenderer::new);
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
        event.registerSpriteSet(ModParticleTypes.DENSITE_PARTICLES.get(), DensiteParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.DENSITE_WELL_PULL_PARTICLES.get(), DensiteWellPullParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.DENSITE_CORE_PARTICLES.get(), DensiteCoreParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.PROPULSITE_PARTICLES.get(), PropulsiteParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.PROPULSITE_THRUSTER_FIRING_PARTICLES.get(), PropulsiteThrusterFiringParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(), PropulsiteThrusterChargingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.OSCILLITE_CANNON_CHARGING_PARTICLES.get(), OscilliteCannonChargingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.OSCILLITE_CANNON_FIRING_PARTICLES.get(), OscilliteCannonFiringParticle.Provider::new);
    }

    // @SubscribeEvent lets the Event Bus discover methods to call
    // @EventBusSubscriber automatically registers all static methods in the class annotated with @SubscribeEvent
}
