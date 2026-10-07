package com.nasilk.createcrystallized.config.client;

import net.createmod.catnip.config.ConfigBase;

public class PonderConfig extends ConfigBase {
    public final ConfigBool enableExperimentalPonders = this.b(Constants.enableExperimentalPonders, "enable_experimental_ponders", Comments.enableExperimentalPonders);

    public PonderConfig() {}

    @Override
    public String getName() {
        return "ponder_config";
    }

    private static class Constants {
        private static final boolean enableExperimentalPonders = false;
    }

    private static class Comments {
        private static final String enableExperimentalPonders = "Whether to register the Densite Well Ponder scene.";
    }
}
