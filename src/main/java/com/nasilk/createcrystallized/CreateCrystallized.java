package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.fluid.ModFluidTypes;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.util.ModDispenser;
import com.nasilk.createcrystallized.particle.custom.DensiteParticles;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.particle.custom.PropulsiteParticles;
import com.nasilk.createcrystallized.particle.custom.PropulsiteThrusterChargingParticles;
import com.nasilk.createcrystallized.particle.custom.PropulsiteThrusterFiringParticles;
import com.nasilk.createcrystallized.util.ModCreativeModeTabs;
import com.nasilk.createcrystallized.util.ModSounds;
import com.nasilk.createcrystallized.util.ModSpriteShifts;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import com.simibubi.create.foundation.data.CreateRegistrate;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateCrystallized.MOD_ID)
public class CreateCrystallized {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "createcrystallized";

    // Connected textures registrator
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when the mod is loaded
    public CreateCrystallized(IEventBus modEventBus, ModContainer modContainer) {
        // Custom registrations
        ModSounds.register(modEventBus); // Custom sounds
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus); // Unique CreativeMode Tab
        ModBlockEntities.register(modEventBus);
        ModFluidTypes.register(modEventBus); // Fluid textures
        ModFluids.register(modEventBus); // Fluid behaviors
        ModParticles.register(modEventBus);

        // Connected textures
        ModSpriteShifts.init();
        REGISTRATE.registerEventListeners(modEventBus);

        // Register ourselves for server and other game events
        NeoForge.EVENT_BUS.register(this);

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register the items to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register ModConfigSpec so that FML can create and load the config file
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModDispenser.register();
        });
        LOGGER.info("Create: Crystallized Loaded");
    }

    // Add block items to creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == ModCreativeModeTabs.FLUIDSANDFIXINS_TAB.get()) {

            // Buckets
            event.accept(ModFluids.VOID_SEA_SLURRY_BUCKET);
            event.accept(ModFluids.DENSITE_EMULSION_BUCKET);
            event.accept(ModFluids.DRIFT_CONDENSATE_BUCKET);
            event.accept(ModFluids.PROPULSITE_FLURRY_BUCKET);
            event.accept(ModFluids.OSCILLITE_SUSPENSION_BUCKET);

            // Uncategorized Items
            event.accept(ModItems.CREATIVE_FLUID_ERASER);
            event.accept(ModItems.CHORA_INGOT);
            event.accept(ModItems.CRUSHED_RAW_AEROLITE);
            event.accept(ModItems.RAW_AEROLITE);
            event.accept(ModItems.AEROLITE_INGOT);

            // Uncategorized Blocks
            event.accept(ModBlocks.PEBBLE);
            event.accept(ModBlocks.AEROLITE_ORE);
            event.accept(ModBlocks.DEEPSLATE_AEROLITE_ORE);
            event.accept(ModBlocks.AEROLITE_BLOCK);

            // Echo Shard Blocks
            event.accept(ModBlocks.ECHO_CRYSTAL_BLOCK);
            event.accept(ModBlocks.BUDDING_ECHO_CRYSTAL);
            event.accept(ModBlocks.ECHO_CRYSTAL_CLUSTER);
            event.accept(ModBlocks.LARGE_ECHO_CRYSTAL_BUD);
            event.accept(ModBlocks.MEDIUM_ECHO_CRYSTAL_BUD);
            event.accept(ModBlocks.SMALL_ECHO_CRYSTAL_BUD);

            // Chora Blocks
            event.accept(ModBlocks.CHORA_BLOCK);
            event.accept(ModBlocks.CHORA_CASING);
            event.accept(ModBlocks.DENSE_CHORA_CASING);
            event.accept(ModBlocks.PROPULSED_CHORA_CASING);
            event.accept(ModBlocks.OSCILLATING_CHORA_CASING);
            event.accept(ModBlocks.LEVITATING_CHORA_CASING);


            // Crystal Blocks
            event.accept(ModBlocks.DENSITE_BLOCK);
            event.accept(ModBlocks.ENCASED_DENSITE_BLOCK);
            event.accept(ModBlocks.DENSITE_WELL);
            event.accept(ModBlocks.PROPULSITE_BLOCK);
            event.accept(ModBlocks.ENCASED_PROPULSITE_BLOCK);
            event.accept(ModBlocks.PROPULSITE_THRUSTER);
            event.accept(ModBlocks.OSCILLITE_BLOCK);
            event.accept(ModBlocks.ENCASED_OSCILLITE_BLOCK);
            event.accept(ModBlocks.OSCILLITE_CANNON);
            event.accept(ModBlocks.ENCASED_LEVITITE_BLOCK);
        }
    }

    // @SubscribeEvent lets the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    // @EventBusSubscriber automatically registers all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
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
            });
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.DENSITE_PARTICLES.get(), DensiteParticles.Provider::new);
            event.registerSpriteSet(ModParticles.PROPULSITE_PARTICLES.get(), PropulsiteParticles.Provider::new);
            event.registerSpriteSet(ModParticles.PROPULSITE_THRUSTER_FIRING_PARTICLES.get(), PropulsiteThrusterFiringParticles.Provider::new);
            event.registerSpriteSet(ModParticles.PROPULSITE_THRUSTER_CHARGING_PARTICLES.get(), PropulsiteThrusterChargingParticles.Provider::new);
        }

        @SubscribeEvent
        public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
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
        }
    }
}
