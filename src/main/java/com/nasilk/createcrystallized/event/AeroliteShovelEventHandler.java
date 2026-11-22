package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.network.payload.SkyPaddlePayload;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.network.packets.tcp.ServerboundPunchSubLevelPacket;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
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

        // Check hitResult
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.MISS) return;

        // Dude Sables are so cool
        sendPunchSubLevelPacket(minecraft, player);

        // Tell server that we lost the baby
        PacketDistributor.sendToServer(new SkyPaddlePayload());
    }

    private static void sendPunchSubLevelPacket(Minecraft minecraft, Player player) {
        // Get position and direction
        Vector3d hitPosition = JOMLConversion.toJOML(player.getEyePosition().add(player.getLookAngle().scale(2.5d)));
        Vector3d hitDirection = JOMLConversion.toJOML(player.getLookAngle());
        BlockPos hitBlockPos = BlockPos.containing(hitPosition.x, hitPosition.y, hitPosition.z);

        // Validate values
        Level level = player.level();
        if (minecraft.gameMode == null ||
            player.blockActionRestricted(level, hitBlockPos, minecraft.gameMode.getPlayerMode()) ||
            player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())
        ) return;

        // Get sublevels
        SubLevel targetSubLevel = Sable.HELPER.getContaining(level, hitPosition);
        @SuppressWarnings("UnstableApiUsage")
        SubLevel trackingSubLevel = ((EntityMovementExtension)player).sable$getTrackingSubLevel();
        if (targetSubLevel == trackingSubLevel || trackingSubLevel == null) return;
        trackingSubLevel.lastPose().transformNormalInverse(hitDirection); // Global -> Local

        // Send packet
        PacketDistributor.sendToServer(new ServerboundPunchSubLevelPacket(
            hitBlockPos, hitPosition, hitDirection
        ));
    }
}
