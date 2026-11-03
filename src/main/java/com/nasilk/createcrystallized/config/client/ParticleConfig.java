package com.nasilk.createcrystallized.config.client;

import net.createmod.catnip.config.ConfigBase;

public class ParticleConfig extends ConfigBase {
    public final ConfigInt wellParticleTravelTicks = this.i(Constants.wellParticleTravelTicks, "well_particle_travel_ticks", Comments.wellParticleTravelTicks);
    public final ConfigFloat coreParticleSpeedScale = this.f(Constants.coreParticleSpeedScale, 0.5f, 5.0f, "core_particle_speed_scale", Comments.coreParticleSpeedScale);

    public ParticleConfig() {}

    @Override
    public String getName() {
        return "particle_config";
    }

    private static class Constants {
        private static final int wellParticleTravelTicks = 100;
        private static final float coreParticleSpeedScale = 1.5f;
    }

    private static class Comments {
        private static final String wellParticleTravelTicks = "Densite Well pulling particle travel time in ticks.";
        private static final String coreParticleSpeedScale = "Densite Core particle speed scale.";
    }
}
