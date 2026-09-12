package com.nasilk.createcrystallized.config.type;

import com.nasilk.createcrystallized.config.server.BlockConfig;
import com.nasilk.createcrystallized.config.server.ItemConfig;
import net.createmod.catnip.config.ConfigBase;

public class ServerConfig extends ConfigBase {
    public final BlockConfig blockConfig;
    public final ItemConfig itemConfig;

    public ServerConfig() {
        blockConfig = this.nested(0, BlockConfig::new, Comments.blockConfig);
        itemConfig = this.nested(0, ItemConfig::new, Comments.itemConfig);
    }

    @Override
    public String getName() {
        return "server_config";
    }

    // Do these even do anything?
    private static class Comments {
        static String blockConfig = "Parameters and abilities of Create Crystallized blocks";
        static String itemConfig = "Parameters and abilities of Create Crystallized items";
    }
}
