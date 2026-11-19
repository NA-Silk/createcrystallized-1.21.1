package com.nasilk.createcrystallized.config.view;

import com.nasilk.createcrystallized.config.view.common.CreativeConfig;
import net.createmod.catnip.config.ConfigBase;

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
