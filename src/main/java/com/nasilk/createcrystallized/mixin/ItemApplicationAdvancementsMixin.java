package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ManualApplicationRecipe.class)
public class ItemApplicationAdvancementsMixin {
    @Inject(method = "awardAdvancements", at = @At("HEAD"))
    private static void addAdvancements(Player player, BlockState placed, CallbackInfo ci) {
        // ADD ADVANCEMENTS HERE
        if (ModBlocks.CHORA_CASING.has(placed)) AdvancementHelper.yoinkerSploinker(player, "the_chora_age");
        if (ModBlocks.ENCASED_DENSITE_BLOCK.has(placed) ||
            ModBlocks.ENCASED_PROPULSITE_BLOCK.has(placed) ||
            ModBlocks.ENCASED_OSCILLITE_BLOCK.has(placed) ||
            ModBlocks.ENCASED_LEVITITE_BLOCK.has(placed)
        ) AdvancementHelper.yoinkerSploinker(player, "handle_with_care");
    }
}
