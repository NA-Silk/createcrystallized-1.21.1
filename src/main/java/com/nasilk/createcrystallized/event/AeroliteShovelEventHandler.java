package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import com.nasilk.createcrystallized.network.payload.SkyPaddlePayload;
import dev.ryanhcode.sable.network.client.ClientSubLevelPunchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, value = Dist.CLIENT)
public class AeroliteShovelEventHandler {
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        // Check attack
        if (!event.isAttack()) return;

        // Check item
        Player player = Minecraft.getInstance().player;
        if (player == null || !ModItems.AEROLITE_SHOVEL.isIn(player.getMainHandItem())) return;

        // Check hitResult
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.MISS) return;

        // Dude Sables are so cool
        ClientSubLevelPunchHelper.clientTryPunch(AeroliteShovelItem.getSkyPaddleHitResult(player), player.level(), false);

        // Tell server that we lost the baby
        PacketDistributor.sendToServer(new SkyPaddlePayload());
    }
}
