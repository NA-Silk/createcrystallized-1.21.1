package com.nasilk.createcrystallized.util;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateCrystallized.MOD_ID);

    public static final Supplier<CreativeModeTab> FLUIDSANDFIXINS_TAB = CREATIVE_MODE_TAB.register("createcrystallized_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DENSITE_BLOCK.get()))
            .title(Component.translatable("creativetab.createcrystallized.createcrystallized_tab")).build());

    public static void register(IEventBus bus) {CREATIVE_MODE_TAB.register(bus);}
}
