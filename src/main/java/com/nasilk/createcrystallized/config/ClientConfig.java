package com.nasilk.createcrystallized.config;

import com.nasilk.createcrystallized.config.client.ParticleConfig;
import com.nasilk.createcrystallized.config.client.PonderConfig;
import net.createmod.catnip.config.ConfigBase;

public class ClientConfig extends ConfigBase {
    public final ParticleConfig particleConfig;
    public final PonderConfig ponderConfig;

    public ClientConfig() {
        particleConfig = this.nested(0, ParticleConfig::new, Comments.particleConfig);
        ponderConfig = this.nested(0, PonderConfig::new, Comments.ponderConfig);
    }

    @Override
    public String getName() {
        return "client_config";
    }

    private static class Comments {
        private static final String particleConfig = "Parameters of Create Crystallized particles.";
        private static final String ponderConfig = "Parameters of Create Crystallized Ponder scenes.";
    }
}
