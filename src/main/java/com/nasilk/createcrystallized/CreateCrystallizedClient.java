package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.entity.renderer.DensiteWellEntityRenderer;
import com.nasilk.createcrystallized.client.models.DensiteWellCubeModel;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CreateCrystallized.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateCrystallizedClient { public CreateCrystallizedClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // Registers any BER i pull out of my redbull can at 4am
            event.registerBlockEntityRenderer(ModBlockEntities.DENSITE_WELL.get(), DensiteWellEntityRenderer::new
            );
        }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                DensiteWellCubeModel.LAYER_LOCATION,
                DensiteWellCubeModel::createBodyLayer
        );
    }





    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Client setup code
        CreateCrystallized.LOGGER.info("HELLO FROM CLIENT SETUP");
        CreateCrystallized.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
