package com.nasilk.createcrystallized.util.helper;

import dev.ryanhcode.sable.SableConfig;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.index.SableAttributes;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import java.util.Objects;

public final class ServerSubLevelPunchHelper {
    private static final double ADDITIONAL_STRENGTH_MULTIPLIER = 2.5d;

    // IMPORTANT: position and direction must be in the sublevel's local plot coordinate frame
    public static void serverTryAirPunch(ServerLevel level, ServerSubLevel standingSubLevel, Player player, Vector3d localPosition, Vector3d localDirection) {
        // Input validation
        if (level == null || player == null || standingSubLevel == null) return;
        if (localDirection.lengthSquared() < 1.0e-8d) return;

        /// Replicating ServerboundPunchSubLevelPacket physics
        // Get physics system
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return;
        SubLevelPhysicsSystem physicsSystem = container.physicsSystem();

        // Sable's physics system handles the punch cooldown
        // This makes it really inconsistent, who cares about tick operation counts from player inputs anyway?
        //int customCooldown = SableAttributes.getPushCooldownTicks(player);
        //if (!physicsSystem.tryPunch(player.getGameProfile().getId(), customCooldown)) return;

        // Air punching pushes the standing sublevel backwards
        localDirection.negate();

        // Reuse the existing Sable mass scaling implementation
        double attributeStrength = Objects.requireNonNull(player.getAttribute(SableAttributes.PUNCH_STRENGTH)).getValue();
        double strengthScalar = computeStrengthScalar(
            standingSubLevel,
            localPosition,
            localDirection
        );
        localDirection.mul(attributeStrength * strengthScalar);
        physicsSystem.getPipeline().applyImpulse(
            standingSubLevel,
            localPosition,
            localDirection
        );
    }

    /**
     * <a href="https://www.desmos.com/calculator/ziovwc5a2v">Tuning Desmos page</a>
     * @author Eriksonn
     */
    private static double computeStrengthScalar(ServerSubLevel standingSubLevel, final Vector3dc localPosition, final Vector3dc localDirection) {
        double mass = 1.0d / standingSubLevel.getMassTracker().getInverseNormalMass(localPosition, localDirection);
        double punchCurve = mass < 1.0d ?
            0.55d * mass * mass * mass - 1.3d * mass * mass + 1.75d * mass :
            2.0d * Math.pow(mass + 0.5625d, 0.5d) - 1.5d;
        return punchCurve * SableConfig.SUB_LEVEL_PUNCH_STRENGTH_MULTIPLIER.getAsDouble() * ADDITIONAL_STRENGTH_MULTIPLIER;
    }
}
