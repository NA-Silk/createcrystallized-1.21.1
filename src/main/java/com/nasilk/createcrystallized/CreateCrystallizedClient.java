package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.client.renderer.AerolitePickaxeRenderer;
import com.nasilk.createcrystallized.dummy.DensiteWellMultiblockEntityRenderer;
import com.nasilk.createcrystallized.client.ModSpriteShifts;
import com.nasilk.createcrystallized.client.renderer.DensiteWellEntityRenderer;
import com.nasilk.createcrystallized.client.model.DensiteWellCubeModel;
import com.nasilk.createcrystallized.dummy.ModDummyRegistry;
import com.nasilk.createcrystallized.fluid.BaseFluidType;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.client.renderer.ThrownDensiteCoreRenderer;
import com.nasilk.createcrystallized.item.ModItemEntities;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.client.particle.*;
import com.nasilk.createcrystallized.client.ponder.ModPonderPlugin;
import com.nasilk.createcrystallized.util.api.IHaveClientItemExtensions;
import com.nasilk.createcrystallized.util.helper.CreateTooltipHelper;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import static com.nasilk.createcrystallized.fluid.ModFluids.FLUIDS;
import static com.nasilk.createcrystallized.item.ModItems.ANIMATED_ITEMS;

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

            // Aerolite Sword model switching properties, used for the third person charging anim
            ItemProperties.register(ModItems.AEROLITE_SWORD.get(),
                CreateCrystallized.getRL("charging"),
                (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f
            );

            // Custom Fluid Renders
            for (FluidEntry<?> fluid : FLUIDS) {
                ItemBlockRenderTypes.setRenderLayer(fluid.getSource(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(fluid.get(), RenderType.translucent());
            }

            // Custom Tooltips, add them in the lang file
            CreateTooltipHelper.registerSimpleTooltip(ModItems.AEROLITE_SHOVEL);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.AEROLITE_PICKAXE);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.AEROLITE_AXE);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.AEROLITE_HOE);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.AEROLITE_SWORD);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.DENSITE_CORE);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.CREATIVE_FLUID_ERASER);
            CreateTooltipHelper.registerSimpleTooltip(ModItems.CREATIVE_BAG_OF_LONGS);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.PEBBLE);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.OSCILLITE_CANNON);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.PROPULSITE_THRUSTER);
            CreateTooltipHelper.registerSimpleTooltip(ModBlocks.DENSITE_WELL);
            CreateTooltipHelper.registerGoggleTooltip(ModBlocks.DENSITE_BLOCK, 1);
            CreateTooltipHelper.registerGoggleTooltip(ModBlocks.PROPULSITE_BLOCK, 2);
            CreateTooltipHelper.registerGoggleTooltip(ModBlocks.OSCILLITE_BLOCK, 1);

            // CREATE PLS DON'T ROTATE (currently broken, probably won't fix ever)
            RadialWrenchMenu.registerBlacklistedBlock(ModBlocks.PROPULSITE_THRUSTER.getId());
            RadialWrenchMenu.registerBlacklistedBlock(ModBlocks.OSCILLITE_CANNON.getId());

            // Ponders
            PonderIndex.addPlugin(new ModPonderPlugin());
        });
        //CreateCrystallized.LOGGER.info("HELLO from client setup");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (Mods.DYNAMICTREES.isLoaded()) {
            event.getEntity().sendSystemMessage(Component.translatable(
                "§b<Create Crystallized> §f" +
                "Dynamic Trees detected:\n" +
                "Aerolite Axe will not behave as intended on modded trees."
            ));
        }
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

        // Register Item Animations
        for (ItemEntry<?> item : ANIMATED_ITEMS) {
            IHaveClientItemExtensions type = (IHaveClientItemExtensions) item.asItem();
            event.registerItem(type.getClientExtensions(), item.asItem());
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // Spin
        event.registerBlockEntityRenderer(ModDummyRegistry.DENSITE_WELL_MULTIBLOCK_ENTITY.get(), DensiteWellMultiblockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DENSITE_WELL.get(), DensiteWellEntityRenderer::new);
        event.registerEntityRenderer(ModItemEntities.THROWN_DENSITE_CORE.get(), ThrownDensiteCoreRenderer::new);
        event.registerEntityRenderer(ModItemEntities.AEROLITE_PICKAXE.get(), AerolitePickaxeRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) { // For the densite cube and well
        event.registerLayerDefinition(DensiteWellCubeModel.LAYER_LOCATION, DensiteWellCubeModel::createBodyLayer);
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
    // @4amPeanutButterSandwich makes me not wanna eat my monitor
}
