package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
//For real why do i need this? for minecraft being so open to modding, needing a custom trigger for BREAKING A BLOCK seems kind of crazy.
//Unless i dont need it and i just couldnt find the trigger, making me slightly stupid.
@EventBusSubscriber(modid = CreateCrystallized.MOD_ID) public class SonicShardsAdvancementEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {

        if (!(event.getPlayer() instanceof ServerPlayer player)) {return;
        }

        if (!event.getState().is(ModBlocks.ECHO_CRYSTAL_CLUSTER.get())) {return;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "sonic_shards"
        );

        AdvancementHolder advancement = player.server.getAdvancements().get(id);

        if (advancement != null) {player.getAdvancements().award(advancement, "impossible");
        }
    }
}
