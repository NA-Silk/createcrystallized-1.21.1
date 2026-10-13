package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PhysicsCubedAdvancementEvent { private static final String CRITERION_KEY = "impossible";

    @SubscribeEvent
    public static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        AdvancementHolder physicsCubed = player.server.getAdvancements().get(
            CreateCrystallized.getRL("physics_cubed"));

        if (physicsCubed == null || player.getAdvancements().getOrStartProgress(physicsCubed).isDone()) return;

        if (hasAdvancement(player, "createcrystallized", "a_stable_arrangement")
            && hasAdvancement(player, "createcrystallized", "mass_production")
            && hasAdvancement(player, "createcrystallized", "motion_made_manifest")
            && hasAdvancement(player, "aeronautics", "unidentified_floating_object")) {
            player.getAdvancements().award(physicsCubed, CRITERION_KEY);
        }
    }

    private static boolean hasAdvancement(ServerPlayer player, String namespace, String path) {
        AdvancementHolder advancement = player.server.getAdvancements().get(
            ResourceLocation.fromNamespaceAndPath(namespace, path));

        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }
}