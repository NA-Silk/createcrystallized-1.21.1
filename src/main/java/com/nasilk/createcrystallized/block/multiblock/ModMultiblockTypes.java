package com.nasilk.createcrystallized.block.multiblock;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.resources.ResourceLocation;

public final class ModMultiblockTypes {
    public static final ResourceLocation DUMMY = CreateCrystallized.getRL("dummy");
    public static final ResourceLocation DENSITE_WELL = CreateCrystallized.getRL("densite_well");

    public static void init() {
        CreateCrystallized.LOGGER.info("Multiblock Types initialized");
    }
}
