package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.block.ModBlocks;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockPropertiesTooltip.class, remap = false)
public class FloatingTooltipMixin {
    @Inject(method = "getFloatingComponent", at = @At("HEAD"), cancellable = true)
    private static void getFloatingComponentMixin(BlockStateExtension properties, BlockItem item, boolean showNumbers, CallbackInfoReturnable<Component> cir) {
        if (ModBlocks.PROPULSITE_BLOCK.is(item)) cir.setReturnValue(null);
    }
}
