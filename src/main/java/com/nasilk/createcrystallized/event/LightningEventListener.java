package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.fluid.flowingfluid.TransformBaseFlowingFluid;
import com.nasilk.createcrystallized.util.setting.FluidTransformSettings;
import com.nasilk.createcrystallized.util.type.FluidTransformTriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class LightningEventListener {
    private static final int MAX_RADIUS = 8;

    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LightningBolt bolt && event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos lightningPos = bolt.blockPosition();
            lightningEvent(serverLevel, lightningPos);
        }
    }

    private static void lightningEvent(ServerLevel serverLevel, BlockPos lightningPos) {
        for (BlockPos pos : BlockPos.betweenClosed(
                lightningPos.offset(-MAX_RADIUS, -MAX_RADIUS, -MAX_RADIUS),
                lightningPos.offset(MAX_RADIUS, MAX_RADIUS, MAX_RADIUS)
        )) {
            FluidState state = serverLevel.getFluidState(pos);
            if (state.getType() instanceof TransformBaseFlowingFluid fluid) {

                // Get settings for current TransformBaseFlowingFluid fluid
                for (FluidTransformSettings settings : fluid.getSettingsList()) {
                    // Skip if lightning is not required for this fluid
                    if (!settings.lightningSettings().requireLightning()) continue;

                    // Check if the lightning is in the fluid radius
                    if (pos.closerThan(lightningPos, settings.lightningSettings().radius() + 0.5)) {
                        fluid.tryEventTransform(serverLevel, pos, state, FluidTransformTriggerType.LIGHTNING, settings);
                    }
                }
            }
        }
    }
}
