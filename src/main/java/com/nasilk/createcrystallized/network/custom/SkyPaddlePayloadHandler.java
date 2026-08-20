package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SkyPaddlePayloadHandler {

    public static void handleDataOnMain(SkyPaddlePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {var player = context.player();

            if (!player.getMainHandItem().is(ModItems.AEROLITE_SHOVEL.get())) return;

            AeroliteShovelItem.damageSkyPaddle(player); //ME PADDLE MISTER SQUIDWARD
        }
      );
    }
}