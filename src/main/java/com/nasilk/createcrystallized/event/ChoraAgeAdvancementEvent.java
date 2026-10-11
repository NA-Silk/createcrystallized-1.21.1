
package com.nasilk.createcrystallized.event;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(
    modid = CreateCrystallized.MOD_ID,
    bus = EventBusSubscriber.Bus.GAME
)
public class ChoraAgeAdvancementEvent { //fuck you create mod god said it's my turn on the free will

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

    if (!(event.getEntity() instanceof ServerPlayer player)) {return;
        }

    boolean clickedGlass = player.level().getBlockState(event.getPos()).is(Blocks.GLASS);

        if (!clickedGlass) {return;
        }

        if (!event.getItemStack().is(ModItems.CHORA_INGOT.get())) {return;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "the_chora_age"
        );

        AdvancementHolder advancement = player.server.getAdvancements().get(id);

        if (advancement != null) {player.getAdvancements().award(advancement, "impossible");
        }
    }
}