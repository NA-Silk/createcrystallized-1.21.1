package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SkyPaddlePayloadHandler {

    public static void handleDataOnMain(SkyPaddlePayload ignoredPayload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (!player.getMainHandItem().is(ModItems.AEROLITE_SHOVEL.get())) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            BlockHitResult hitResult = AeroliteShovelItem.getSkyPaddle(player);
            AeroliteShovelItem.damageSkyPaddle(player);
            AeroliteShovelItem.skyPaddleParticles(serverLevel, player, hitResult);
            AeroliteShovelItem.skyPaddleSound(serverLevel, hitResult);
        });
    }
}
