package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SkyPaddlePayloadHandler {
    public static void handleDataOnMain(SkyPaddlePayload ignored, IPayloadContext context) {
        // Push code to the main thread
        context.enqueueWork(() -> {
            // Validate context
            Player player = context.player();
            AeroliteShovelItem shovel = ModItems.AEROLITE_SHOVEL.get();
            if (!player.getMainHandItem().is(shovel)) return;
            if (player.getCooldowns().isOnCooldown(shovel)) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            // Run effects
            BlockHitResult hitResult = AeroliteShovelItem.getSkyPaddle(player);
            AeroliteShovelItem.damageSkyPaddle(player);
            AeroliteShovelItem.skyPaddleParticles(serverLevel, player, hitResult);
            AeroliteShovelItem.skyPaddleSound(serverLevel, hitResult);
        });
    }
}
