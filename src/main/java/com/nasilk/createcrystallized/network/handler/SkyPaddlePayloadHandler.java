package com.nasilk.createcrystallized.network.handler;

import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import com.nasilk.createcrystallized.network.payload.SkyPaddlePayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SkyPaddlePayloadHandler {
    public static void handleDataOnMain(SkyPaddlePayload ignored, IPayloadContext context) {
        // Push code to the main server thread
        context.enqueueWork(() -> {
            // Validate context
            Player player = context.player();
            AeroliteShovelItem shovel = ModItems.AEROLITE_SHOVEL.get();
            if (!player.getMainHandItem().is(shovel) || player.getCooldowns().isOnCooldown(shovel)) return;

            // Run effects
            AeroliteShovelItem.doSkyPaddleEffects(player);
        });
    }
}
