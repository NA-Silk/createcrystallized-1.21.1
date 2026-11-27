package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.ModMultiblockTypes;
import com.nasilk.createcrystallized.dummy.ModDummyRegistry;
import com.nasilk.createcrystallized.common.ModTags;
import com.nasilk.createcrystallized.config.ModConfigs;
import com.nasilk.createcrystallized.item.ModItemEntities;
import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.common.ModDispenserBehaviors;
import com.nasilk.createcrystallized.item.ModTiers;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.common.ModCreativeModeTabs;
import com.nasilk.createcrystallized.common.ModSounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import com.simibubi.create.foundation.data.CreateRegistrate;

@Mod(CreateCrystallized.MOD_ID)
public class CreateCrystallized {
    public static final String MOD_ID = "createcrystallized"; // Define mod id in a common place for everything to reference
    public static final Logger LOGGER = LogUtils.getLogger(); // Directly reference a slf4j logger
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID).defaultCreativeTab((ResourceKey<CreativeModeTab>) null); // Connected textures registrator

    // The fitness gram pacer test is the first code that is run when the mod is loaded
    public CreateCrystallized(IEventBus modEventBus, ModContainer modContainer) {
        // ModContainer registrations
        ModConfigs.register(modContainer);

        // Static initializations
        ModTags.init();
        ModTiers.init();
        ModMultiblockTypes.init();

        // REGISTRATE initializations
        ModDummyRegistry.init(); // Testing area
        ModBlocks.init();
        ModBlockEntities.init();
        ModItemEntities.init();
        ModFluids.init();
        ModItems.init();
        ModParticleTypes.init(); // Types are common, factories are client
        ModSounds.init();
        ModCreativeModeTabs.init();

        // ModBus registrations
        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::commonSetup); // Register the commonSetup method for mod loading
        //NeoForge.EVENT_BUS.register(this); // Register ourselves for server and other game events (retained for reference)
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModCreativeModeTabs.register();
            ModDispenserBehaviors.register();
            //ModFluidInteractions.register();
        });
        //LOGGER.info("HELLO from common setup");
    }

    @SuppressWarnings("SameReturnValue")
    public static CreateRegistrate getRegistrate() {
        return REGISTRATE;
    }

    public static ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, path);
    }
}

/* Server starting block (retained for reference)
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
*/
