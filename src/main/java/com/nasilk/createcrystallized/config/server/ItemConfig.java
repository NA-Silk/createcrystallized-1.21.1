package com.nasilk.createcrystallized.config.server;

import net.createmod.catnip.config.ConfigBase;

public class ItemConfig extends ConfigBase {
    public final ConfigBool enableDevItems = this.b(true, "enable_dev_items", Comments.enableDevItems);

    public ItemConfig() {}

    @Override
    public String getName() {
        return "item_config";
    }

    private static class Comments {
        private static final String enableDevItems = "Would you like to enable development items?";
    }
}
