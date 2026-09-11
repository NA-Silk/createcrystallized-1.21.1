package com.nasilk.createcrystallized;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    // TODO Figure out how Create does this
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_ITEMS;
    static {
        BUILDER.push("ModConfig");
        ENABLE_ITEMS = BUILDER
            .comment("Would you like to enable development items?")
            .define("EnableItems", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
