package com.nasilk.createcrystallized.common;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.ModBlocks;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> CREATECRYSTALLIZED_TAB = REGISTRATE.simple(
        "createcrystallized_tab",
        Registries.CREATIVE_MODE_TAB,
        () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModBlocks.DENSITE_BLOCK.get()))
            .title(Component.translatable("creativetab.createcrystallized.createcrystallized_tab"))
            .build()
    );

    public static void register() {
        CreateCrystallized.LOGGER.info("Creative Mode Tabs registered");
    }
}
