package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import com.nasilk.createcrystallized.network.custom.SkyPaddlePayload;
import dev.ryanhcode.sable.network.client.ClientSubLevelPunchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
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
        if (!event.isAttack()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
            || !(minecraft.player instanceof LocalPlayer player)
            || !player.getMainHandItem().is(ModItems.AEROLITE_SHOVEL.get())
            || !(minecraft.hitResult instanceof HitResult hitResult)
            || !(hitResult.getType() == HitResult.Type.MISS)
        ) return;

        //Dude Sables are so cool
        BlockHitResult blockHitResult = AeroliteShovelItem.getSkyPaddle(minecraft.player);
        ClientSubLevelPunchHelper.clientTryPunch(blockHitResult, minecraft.level, false);

        //Tell server that we lost the baby
        PacketDistributor.sendToServer(new SkyPaddlePayload());
    }
}
