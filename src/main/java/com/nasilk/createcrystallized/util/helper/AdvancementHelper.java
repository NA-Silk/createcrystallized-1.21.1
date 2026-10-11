package com.nasilk.createcrystallized.util.helper;

import com.nasilk.createcrystallized.CreateCrystallized;
import dev.ryanhcode.sable.Sable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class AdvancementHelper {
    private static final String CRITERION_KEY = "impossible"; // TODO Custom key
    private static final int RADIUS = 10;
    private static final int RADIUS_SQUARED = RADIUS * RADIUS;

    // Give advancement to nearby players
    public static void hitEmWithTheOlSwitcheroo(Level level, BlockPos pos, String id) {
        if (level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            AdvancementHolder advancement = server.getAdvancements().get(CreateCrystallized.getRL(id));
            if (advancement != null) {
                for (ServerPlayer player : serverLevel.players()) {
                    Vector3d maybePos = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
                    Sable.HELPER.projectOutOfSubLevel(serverLevel, maybePos);
                    BlockPos realPos = BlockPos.containing(maybePos.x, maybePos.y, maybePos.z);
                    if (player.blockPosition().distSqr(realPos) <= RADIUS_SQUARED) {
                        player.getAdvancements().award(advancement, CRITERION_KEY);
                    }
                }
            }
        }
    }

    // Give advancement to specific player
    public static void yoinkerSploinker(Player player, String id) {
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementHolder advancement = serverPlayer.server.getAdvancements().get(CreateCrystallized.getRL(id));
            if (advancement != null) serverPlayer.getAdvancements().award(advancement, CRITERION_KEY);
        }
    }
}
