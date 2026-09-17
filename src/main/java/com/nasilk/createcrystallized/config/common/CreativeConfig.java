package com.nasilk.createcrystallized.config.common;

import net.createmod.catnip.config.ConfigBase;

public class CreativeConfig extends ConfigBase {
    public final ConfigBool enableDevItems = this.b(Constants.enableDevItems, "enable_dev_items", Comments.enableDevItems);
    public final ConfigBool enableUnusedItems = this.b(Constants.enableUnusedItems, "enable_unused_items", Comments.enableUnusedItems);

    public CreativeConfig() {}

    @Override
    public String getName() {
        return "creative_config";
    }

    private static class Constants {
        private static final boolean enableDevItems = true;
        private static final boolean enableUnusedItems = false;
    }

    private static class Comments {
        private static final String enableDevItems = "Would you like to enable our super cool development items?";
        private static final String enableUnusedItems = "Would you like to enable our fancy but kinda useless items?";
    }
}
