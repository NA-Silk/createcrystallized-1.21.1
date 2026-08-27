package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.entity.renderer.DensiteWellEntityRenderer;
import com.nasilk.createcrystallized.client.models.DensiteWellCubeModel;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.entity.renderer.ThrownDensiteCoreRenderer;
import com.nasilk.createcrystallized.entity.ModEntities;
import com.nasilk.createcrystallized.util.helper.CreateTooltipHelper;
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
@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, value = Dist.CLIENT)
public class CreateCrystallizedClient {
    public CreateCrystallizedClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // spinspinspinspinspinspinspinspinspinspinspinspinspin
        event.registerBlockEntityRenderer(ModBlockEntities.DENSITE_WELL.get(), DensiteWellEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_DENSITE_CORE.get(), ThrownDensiteCoreRenderer::new);
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
        event.enqueueWork(() -> { //anything in here gets custom tooltips, add them in the lang file
            CreateTooltipHelper.register(ModItems.AEROLITE_SHOVEL.get());
            CreateTooltipHelper.register(ModItems.DENSITE_CORE.get());
            CreateTooltipHelper.register(ModItems.CREATIVE_FLUID_ERASER.get());
            CreateTooltipHelper.register(ModBlocks.PEBBLE.get());
            CreateTooltipHelper.register(ModBlocks.OSCILLITE_CANNON.get());
            CreateTooltipHelper.register(ModBlocks.PROPULSITE_THRUSTER.get());
            CreateTooltipHelper.register(ModBlocks.DENSITE_WELL.get());
            //moar item go here
        });
        CreateCrystallized.LOGGER.info("HELLO FROM CLIENT SETUP");
        CreateCrystallized.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
