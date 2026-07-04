package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.fluid.flowingfluid.TransformBaseFlowingFluid;
import com.nasilk.createcrystallized.util.FluidTransformationSettings;
import com.nasilk.createcrystallized.util.FluidTransformationTriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class VibrationEventListener {
    static final int maxRadius = 8;

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        // Skip off-thread worldgen events
        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel
            && !serverLevel.getServer().isSameThread()
        ) return;

        // Get the frequency from the Holder<GameEvent>
        int frequency = VibrationSystem.getGameEventFrequency(event.getVanillaEvent());
        if (frequency >= 1) {
            BlockPos vibrationPos = BlockPos.containing(event.getEventPosition());
            vibrationEvent(level, vibrationPos, frequency, event.getVanillaEvent());
        }
    }

    private static void vibrationEvent(Level level, BlockPos vibrationPos, int frequency, Holder<GameEvent> gameEvent) {
        for (BlockPos pos : BlockPos.betweenClosed(
                vibrationPos.offset(-maxRadius, -maxRadius, -maxRadius),
                vibrationPos.offset(maxRadius, maxRadius, maxRadius)
        )) {
            // Skip unloaded chunks
            ChunkAccess chunk = level.getChunkSource().getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
            if (chunk == null) return;

            // Get fluid state and run vibration logic
            FluidState state = level.getFluidState(pos);
            if (state.getType() instanceof TransformBaseFlowingFluid fluid) {
                // Skip self-triggering on placement.
                if (pos.equals(vibrationPos)
                    && (gameEvent.value() == GameEvent.BLOCK_PLACE.value() || gameEvent.value() == GameEvent.FLUID_PLACE.value())
                ) continue;

                // Get settings for current TransformBaseFlowingFluid fluid
                FluidTransformationSettings.VibrationSettings settings = fluid.getSettings().vibrationSettings();

                // Skip if vibration is not required for this fluid
                if (!settings.requireVibration()) continue;

                // Check if the vibration is in the fluid radius and if the frequency matches
                if (pos.closerThan(vibrationPos, settings.radius() + 0.5) && frequency >= settings.minimumFrequency()) {
                    fluid.tryTransform(level, pos, state, FluidTransformationTriggerType.VIBRATION);
                }
            }
        }
    }
}
