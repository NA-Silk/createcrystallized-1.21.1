package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.fluid.ModFluidTypes;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.particle.custom.*;
import com.nasilk.createcrystallized.behavior.ModDispenserBehavior;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.nasilk.createcrystallized.common.ModCreativeModeTabs;
import com.nasilk.createcrystallized.common.ModSounds;
import com.nasilk.createcrystallized.common.ModSpriteShifts;
import net.minecraft.client.Minecraft;
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
    public static final String MOD_ID = "createcrystallized"; // Define mod id in a common place for everything to reference
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID); // Connected textures registrator
    public static final Logger LOGGER = LogUtils.getLogger(); // Directly reference a slf4j logger

    // The constructor for the mod class is the first code that is run when the mod is loaded
    public CreateCrystallized(IEventBus modEventBus, ModContainer modContainer) {
        // Custom registrations
        ModSounds.register(modEventBus); // Custom sounds
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus); // Unique CreativeMode Tab
        ModBlockEntities.register(modEventBus);
        ModFluidTypes.register(modEventBus); // Fluid textures
        ModFluids.register(modEventBus); // Fluid behaviors
        ModParticles.register(modEventBus);

        // Connected textures
        ModSpriteShifts.init();
        REGISTRATE.registerEventListeners(modEventBus);

        // Default registrations
        NeoForge.EVENT_BUS.register(this); // Register ourselves for server and other game events
        modEventBus.addListener(this::commonSetup); // Register the commonSetup method for mod loading
        modEventBus.addListener(this::addCreative); // Register the items to a creative tab
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC); // Register ModConfigSpec so that FML can create and load the config file
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Minecraft Name >> {}", Minecraft.getInstance().getUser().getName());
        LOGGER.info("HELLO from server starting");
    }

    @SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModDispenserBehavior.register();
        });
        LOGGER.info("Create:Crystallized Loaded");
        LOGGER.info("HELLO from common setup");
    }

    // Add block items to creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == ModCreativeModeTabs.CREATECRYSTALLIZED_TAB.get()) {
            // Buckets
            event.accept(ModFluids.VOID_SEA_SLURRY_BUCKET);
            event.accept(ModFluids.DENSITE_EMULSION_BUCKET);
            event.accept(ModFluids.DRIFT_CONDENSATE_BUCKET);
            event.accept(ModFluids.PROPULSITE_FLURRY_BUCKET);
            event.accept(ModFluids.OSCILLITE_SUSPENSION_BUCKET);

            // Transformation Items
            event.accept(ModItems.OSCILLITE_RESONATOR);

            // Uncategorized Items
            event.accept(ModItems.CREATIVE_FLUID_ERASER);
            event.accept(ModItems.CREATIVE_BAG_OF_LONGS);
            event.accept(ModItems.CHORA_INGOT);
            event.accept(ModItems.CRUSHED_RAW_AEROLITE);
            event.accept(ModItems.RAW_AEROLITE);
            event.accept(ModItems.AEROLITE_INGOT);
            event.accept(ModItems.AEROLITE_SHEET);
            event.accept(ModItems.DENSITE_CORE);
            event.accept(ModItems.AEROLITE_SHOVEL);

            // Uncategorized Blocks
            event.accept(ModBlocks.PEBBLE);
            event.accept(ModBlocks.AEROLITE_ORE);
            event.accept(ModBlocks.DEEPSLATE_AEROLITE_ORE);
            event.accept(ModBlocks.AEROLITE_BLOCK);
            event.accept(ModBlocks.PROPULSITE_CRYSTAL);

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
}
