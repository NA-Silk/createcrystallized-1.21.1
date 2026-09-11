package com.nasilk.createcrystallized.config.server;

import net.createmod.catnip.config.ConfigBase;

public class BlockConfig extends ConfigBase {
    public final ConfigBase.ConfigBool enableDevItems = this.b(true, "enable_dev_items");

    public BlockConfig() {}

    @Override
    public String getName() {
        return "block_config";
    }
}
