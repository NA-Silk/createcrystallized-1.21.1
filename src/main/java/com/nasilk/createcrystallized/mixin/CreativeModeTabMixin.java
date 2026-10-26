package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.common.ModCreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    @Shadow private Collection<ItemStack> displayItems;
    @Shadow private Set<ItemStack> displayItemsSearchTab;

    @Inject(method = "buildContents", at = @At("HEAD"), cancellable = true)
    private void buildContents(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
        if ((Object) this != ModCreativeModeTabs.CREATE_CRYSTALLIZED_TAB.get()) return;
        List<ItemStack> tabItems = new LinkedList<>();
        Set<ItemStack> searchItems = new LinkedHashSet<>();
        ModCreativeModeTabs.buildContents(tabItems, searchItems);
        this.displayItems = tabItems;
        this.displayItemsSearchTab = searchItems;
        ci.cancel();
    }
}
