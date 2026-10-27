package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.client.renderer.ModCreativeTabRenderer;
import com.nasilk.createcrystallized.common.ModCreativeModeTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
    @Shadow private static CreativeModeTab selectedTab;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderSections(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (selectedTab != ModCreativeModeTabs.CREATE_CRYSTALLIZED_TAB.get()) return;
        ModCreativeTabRenderer.render((CreativeModeInventoryScreen) (Object) this, graphics, mouseX, mouseY);
    }
}
