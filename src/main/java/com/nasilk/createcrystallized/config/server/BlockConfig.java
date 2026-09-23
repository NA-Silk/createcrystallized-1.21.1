package com.nasilk.createcrystallized.config.server;

import com.nasilk.createcrystallized.config.server.block.CannonConfig;
import com.nasilk.createcrystallized.config.server.block.ThrusterConfig;
import com.nasilk.createcrystallized.config.server.block.WellConfig;
import net.createmod.catnip.config.ConfigBase;

public class BlockConfig extends ConfigBase {
    public final CannonConfig cannonConfig;
    public final ThrusterConfig thrusterConfig;
    public final WellConfig wellConfig;

    public BlockConfig() {
        cannonConfig = this.nested(0, CannonConfig::new, Comments.cannonConfig);
        thrusterConfig = this.nested(0, ThrusterConfig::new, Comments.thrusterConfig);
        wellConfig = this.nested(0, WellConfig::new, Comments.wellConfig);
    }

    @Override
    public String getName() {
        return "block_config";
    }

    private static class Comments {
        private static final String cannonConfig = "Parameters and abilities of Create Crystallized Oscillite Cannon.";
        private static final String thrusterConfig = "Parameters and abilities of Create Crystallized Propulsite Thruster.";
        private static final String wellConfig = "Parameters and abilities of Create Crystallized Densite Well.";
    }
}
