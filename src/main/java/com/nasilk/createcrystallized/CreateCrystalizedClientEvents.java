package com.nasilk.createcrystallized;

import com.nasilk.createcrystallized.client.items.AeroliteShovelClient;
import com.nasilk.createcrystallized.item.ModItems;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(
        modid = CreateCrystallized.MOD_ID,
        value = Dist.CLIENT
)
class CreateCrystallizedClientEvents {

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return;

        if (minecraft.player.getMainHandItem().is(ModItems.AEROLITE_SHOVEL.get())) {
            AeroliteShovelClient.trySkyPaddle();
        }
    }
}