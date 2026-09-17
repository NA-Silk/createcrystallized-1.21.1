package com.nasilk.createcrystallized.config.type;

import com.nasilk.createcrystallized.config.client.ParticleConfig;
import net.createmod.catnip.config.ConfigBase;

@SuppressWarnings("unused")
public class ClientConfig extends ConfigBase {
    public final ParticleConfig particleConfig;

    public ClientConfig() {
        particleConfig = this.nested(0, ParticleConfig::new, Comments.particleConfig);
    }

    @Override
    public String getName() {
        return "client_config";
    }

    private static class Comments {
        private static final String particleConfig = "Parameters of Create Crystallized particles.";
    }
}
