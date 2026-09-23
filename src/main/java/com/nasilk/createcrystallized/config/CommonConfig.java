package com.nasilk.createcrystallized.config;

import com.nasilk.createcrystallized.config.common.CreativeConfig;
import net.createmod.catnip.config.ConfigBase;

@SuppressWarnings("unused")
public class CommonConfig extends ConfigBase {
    public final CreativeConfig creativeConfig;

    public CommonConfig() {
        creativeConfig = this.nested(0, CreativeConfig::new, Comments.creativeConfig);
    }

    @Override
    public String getName() {
        return "common_config";
    }

    private static class Comments {
        private static final String creativeConfig = "Parameters of Create Crystallized Creative menu items.";
    }
}
