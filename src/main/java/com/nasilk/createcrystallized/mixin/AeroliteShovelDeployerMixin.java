package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.AeroliteShovelItem;
import com.nasilk.createcrystallized.util.helper.ServerSubLevelPunchHelper;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerHandler;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeployerHandler.class)
public class AeroliteShovelDeployerMixin {
    @Inject(
        method = "activateInner",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;blockBreakingProgress:Lorg/apache/commons/lang3/tuple/Pair;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0
        )
    )
    private static void useAeroliteShoveMixin(DeployerFakePlayer player, Vec3 vec, BlockPos clickedPos, Vec3 extensionVector, @Coerce Object mode, CallbackInfo ci) {
        if (player == null || !ModItems.AEROLITE_SHOVEL.isIn(player.getMainHandItem())) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Who needs the client anyway?
        if (!(Sable.HELPER.getContaining(level, BlockPos.containing(vec)) instanceof ServerSubLevel standingSubLevel)) return;
        Vector3d localPosition = JOMLConversion.toJOML(player.getEyePosition());
        Vector3d localDirection = JOMLConversion.toJOML(extensionVector).normalize();
        ServerSubLevelPunchHelper.serverTryAirPunch(level, standingSubLevel, player, localPosition, localDirection);

        // Particles
        double distance = 31d / 32d;
        AeroliteShovelItem.doSkyPaddleEffects(level, localPosition, JOMLConversion.toJOML(extensionVector), distance);
    }
}
