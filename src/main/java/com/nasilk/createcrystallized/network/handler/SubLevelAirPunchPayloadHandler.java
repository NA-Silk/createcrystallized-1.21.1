package com.nasilk.createcrystallized.network.handler;

import com.nasilk.createcrystallized.network.payload.SubLevelAirPunchPayload;
import com.nasilk.createcrystallized.util.helper.ServerSubLevelPunchHelper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;

public class SubLevelAirPunchPayloadHandler {
    public static void handleDataOnMain(final SubLevelAirPunchPayload data, final IPayloadContext context) {
        // Push code to the main server thread
        context.enqueueWork(() -> {
            // Get components
            Player player = context.player();
            ServerLevel level = (ServerLevel) player.level();
            if (!(Sable.HELPER.getTrackingSubLevel(player) instanceof ServerSubLevel standingSubLevel)) return;

            // Validate player
            if (player.onGround() || player.isInWater() || player.getAbilities().flying || player.onClimbable()) {
                // Transform local vectors
                Pose3d pose = standingSubLevel.logicalPose();
                Vector3d localPosition = pose.transformPositionInverse(JOMLConversion.toJOML(player.position()));
                Vector3d localDirection = new Vector3d(data.hitDirection()).normalize();

                // Send to helper
                ServerSubLevelPunchHelper.serverTryAirPunch(level, standingSubLevel, player, localPosition, localDirection);
            }
        });
    }
}
