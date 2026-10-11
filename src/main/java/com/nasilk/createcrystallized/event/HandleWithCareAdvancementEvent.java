package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID) public class HandleWithCareAdvancementEvent {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {return;
        }

        var clickedBlock = player.level().getBlockState(event.getPos()).getBlock();

        boolean clickedTargetBlock = clickedBlock
                        == ModBlocks.OSCILLITE_BLOCK.get()
                        || clickedBlock == ModBlocks.DENSITE_BLOCK.get()
                        || clickedBlock == ModBlocks.PROPULSITE_BLOCK.get()
                        || clickedBlock == AeroBlocks.LEVITITE.get();

        if (!clickedTargetBlock) {return;
        }

        if (!event.getItemStack().is(ModBlocks.CHORA_CASING.get().asItem())) {return;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "handle_with_care"
        );

        AdvancementHolder advancement = player.server.getAdvancements().get(id);

        if (advancement != null) {player.getAdvancements().award(advancement, "impossible");
        }
    }
}