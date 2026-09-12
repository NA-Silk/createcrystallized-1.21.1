package com.nasilk.createcrystallized.config.server;

import net.createmod.catnip.config.ConfigBase;

public class BlockConfig extends ConfigBase {
    public final ConfigBool enableDevBlocks = this.b(true, "enable_dev_blocks", Comments.enableDevBlocks);

    public BlockConfig() {}

    @Override
    public String getName() {
        return "block_config";
    }

    private static class Comments {
        private static final String enableDevBlocks = "Would you like to enable development blocks?";
    }
}
