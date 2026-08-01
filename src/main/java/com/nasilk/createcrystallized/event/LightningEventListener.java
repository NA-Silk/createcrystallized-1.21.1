package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.fluid.flowingfluid.TransformBaseFlowingFluid;
import com.nasilk.createcrystallized.util.setting.FluidTransformSettings;
import com.nasilk.createcrystallized.util.type.FluidTransformationTriggerType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class LightningEventListener {
    static final int maxRadius = 8;

    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LightningBolt bolt && !event.getLevel().isClientSide) {
            Level level = event.getLevel();
            BlockPos lightningPos = bolt.blockPosition();
            lightningEvent(level, lightningPos);
        }
    }

    private static void lightningEvent(Level level, BlockPos lightningPos) {
        for (BlockPos pos : BlockPos.betweenClosed(
                lightningPos.offset(-maxRadius, -maxRadius, -maxRadius),
                lightningPos.offset(maxRadius, maxRadius, maxRadius)
        )) {
            FluidState state = level.getFluidState(pos);
            if (state.getType() instanceof TransformBaseFlowingFluid fluid) {

                // Get settings for current TransformBaseFlowingFluid fluid
                for (FluidTransformSettings settings : fluid.getSettingsList()) {
                    // Skip if lightning is not required for this fluid
                    if (!settings.lightningSettings().requireLightning()) continue;

                    // Check if the lightning is in the fluid radius
                    if (pos.closerThan(lightningPos, settings.lightningSettings().radius() + 0.5)) {
                        fluid.tryTransform(level, pos, state, FluidTransformationTriggerType.LIGHTNING);
                    }
                }
            }
        }
    }
}
