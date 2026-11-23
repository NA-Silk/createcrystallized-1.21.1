package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.network.payload.SkyPaddlePayload;
import com.nasilk.createcrystallized.network.payload.SubLevelAirPunchPayload;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, value = Dist.CLIENT)
public class AeroliteShovelEventHandler {
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        // Check attack
        if (!event.isAttack()) return;

        // Check item
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !ModItems.AEROLITE_SHOVEL.isIn(player.getMainHandItem())) return;

        // Check hitResult (only run on miss)
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.MISS) return;

        // Dude Sables are so cool
        sendSubLevelAirPunchPacket(minecraft, player);

        // Tell server that we lost the baby
        PacketDistributor.sendToServer(new SkyPaddlePayload());
    }

    private static void sendSubLevelAirPunchPacket(Minecraft minecraft, Player player) {
        // Get position and direction
        Vector3d hitPosition = JOMLConversion.toJOML(player.getEyePosition().add(player.getLookAngle().scale(2.5d)));
        Vector3d hitDirection = JOMLConversion.toJOML(player.getLookAngle());

        // Validate values
        Level level = player.level();
        if (minecraft.gameMode == null ||
            player.blockActionRestricted(level, BlockPos.containing(hitPosition.x, hitPosition.y, hitPosition.z), minecraft.gameMode.getPlayerMode()) ||
            player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())
        ) return;

        // Get sublevels
        SubLevel targetSubLevel = Sable.HELPER.getContaining(level, hitPosition);
        SubLevel trackingSubLevel = Sable.HELPER.getTrackingSubLevel(player);
        if (targetSubLevel == trackingSubLevel || trackingSubLevel == null) return;
        trackingSubLevel.lastPose().transformNormalInverse(hitDirection); // Global -> Local

        // Send packet
        PacketDistributor.sendToServer(new SubLevelAirPunchPayload(hitDirection));
    }
}
