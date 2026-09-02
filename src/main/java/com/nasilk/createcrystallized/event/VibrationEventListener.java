package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.fluid.flowingfluid.TransformBaseFlowingFluid;
import com.nasilk.createcrystallized.util.setting.FluidTransformSettings;
import com.nasilk.createcrystallized.util.type.FluidTransformTriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import java.util.function.Predicate;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class VibrationEventListener {
    private static final Predicate<BlockState> FLUID_STATE_PREDICATE = blockState -> blockState.getFluidState().getType() instanceof TransformBaseFlowingFluid;
    private static final int MAX_RADIUS = 8;

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        // Skip off-thread worldgen events
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (!serverLevel.getServer().isSameThread()) return;

            // Get the frequency from the Holder<GameEvent>
            Holder<GameEvent> gameEvent = event.getVanillaEvent();
            if (!gameEvent.is(GameEventTags.VIBRATIONS)) return;
            int frequency = VibrationSystem.getGameEventFrequency(gameEvent);
            if (frequency > 2) {
                vibrationEventLevel(serverLevel, BlockPos.containing(event.getEventPosition()), frequency, gameEvent);
            }
        }
    }

    private static void vibrationEventLevel(ServerLevel serverLevel, BlockPos vibrationPos, int frequency, Holder<GameEvent> gameEvent) {
        // Iterate over sections to find fluids
        boolean hasFluid = false;
        for (BlockPos sectionPos : BlockPos.betweenClosed(
                (vibrationPos.getX() - MAX_RADIUS) >> 4, (vibrationPos.getY() - MAX_RADIUS) >> 4, (vibrationPos.getZ() - MAX_RADIUS) >> 4,
                (vibrationPos.getX() + MAX_RADIUS) >> 4, (vibrationPos.getY() + MAX_RADIUS) >> 4, (vibrationPos.getZ() + MAX_RADIUS) >> 4
        )) {
            // Get chunk
            LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(sectionPos.getX(), sectionPos.getZ());
            if (chunk == null) continue;

            // Get section
            int sectionY = sectionPos.getY();
            if (sectionY < serverLevel.getMinSection() || sectionY >= serverLevel.getMaxSection()) continue;
            LevelChunkSection section = chunk.getSections()[chunk.getSectionIndexFromSectionY(sectionY)];
            if (section == null) continue;

            // Confirm fluids
            if (section.maybeHas(FLUID_STATE_PREDICATE)) {
                hasFluid = true;
                break;
            }
        }

        // Run fluidState checks if correct fluids are found
        if (hasFluid) vibrationEventSection(serverLevel, vibrationPos, frequency, gameEvent);
    }

    private static void vibrationEventSection(ServerLevel serverLevel, BlockPos vibrationPos, int frequency, Holder<GameEvent> gameEvent) {
        // Iterate over blocks
        for (BlockPos pos : BlockPos.betweenClosed(
                vibrationPos.offset(-MAX_RADIUS, -MAX_RADIUS, -MAX_RADIUS),
                vibrationPos.offset(MAX_RADIUS, MAX_RADIUS, MAX_RADIUS)
        )) {
            // Get fluid state and run vibration logic
            FluidState state = serverLevel.getFluidState(pos);
            if (state.getType() instanceof TransformBaseFlowingFluid fluid) {
                // Skip self-triggering on placement.
                if (pos.equals(vibrationPos)
                    && (gameEvent.value() == GameEvent.BLOCK_PLACE.value() || gameEvent.value() == GameEvent.FLUID_PLACE.value())
                ) continue;

                // Get settings for current TransformBaseFlowingFluid fluid
                for (FluidTransformSettings settings : fluid.getSettingsList()) {
                    // Skip if vibration is not required for this fluid
                    if (!settings.vibrationSettings().requireVibration()) continue;

                    // Check if the vibration is in the fluid radius and if the frequency matches
                    if (pos.closerThan(vibrationPos, settings.vibrationSettings().radius() + 0.5d)
                        && frequency >= settings.vibrationSettings().minimumFrequency()
                    ) {
                        fluid.tryEventTransform(serverLevel, pos, state, FluidTransformTriggerType.VIBRATION, settings);
                    }
                }
            }
        }
    }
}
