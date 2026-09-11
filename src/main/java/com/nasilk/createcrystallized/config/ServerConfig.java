package com.nasilk.createcrystallized.config;

import com.nasilk.createcrystallized.config.server.BlockConfig;
import net.createmod.catnip.config.ConfigBase;

public class ServerConfig extends ConfigBase {
    public final BlockConfig blockConfig;

    public ServerConfig() {
        blockConfig = this.nested(0, BlockConfig::new);
    }

    @Override
    public String getName() {
        return "server_config";
    }
}
