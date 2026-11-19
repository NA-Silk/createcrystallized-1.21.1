package com.nasilk.createcrystallized.config.server.block;

import com.nasilk.createcrystallized.block.entity.OscilliteBlockEntity;
import net.createmod.catnip.config.ConfigBase;

public class OscilliteConfig extends ConfigBase {
    public final ConfigInt   gyroFunction     = this.i(Constants.gyroFunction, 1, 2, "gyro_function", Comments.gyroFunction);
    /// G1
    public final ConfigFloat torqueScale      = this.f(Constants.torqueScale, 0.0f, 1.0f, "torque_scale", Comments.torqueScale);
    public final ConfigFloat dampingScale     = this.f(Constants.dampingScale, 0.0f, 1.0f, "damping_scale", Comments.dampingScale);
    /// G2
    public final ConfigFloat massCapacity     = this.f(Constants.massCapacity, 4.0f, 16.0f, "mass_capacity", Comments.massCapacity);
    public final ConfigFloat naturalFrequency = this.f(Constants.naturalFrequency, 0.0f, 10.0f, "natural_frequency", Comments.naturalFrequency);
    public final ConfigFloat dampingRatio     = this.f(Constants.dampingRatio, 0.0f, 2.0f, "damping_ratio", Comments.dampingRatio);

    public OscilliteConfig() {}

    @Override
    public void onLoad() {
        super.onLoad();
        OscilliteBlockEntity.updateConstants();
    }

    @Override
    public void onReload() {
        super.onReload();
        OscilliteBlockEntity.updateConstants();
    }

    @Override
    public String getName() {
        return "oscillite_config";
    }

    private static class Constants {
        private static final int   gyroFunction = 2;
        private static final float torqueScale = 0.76f;
        private static final float dampingScale = 0.96f;
        private static final float massCapacity = 8.0f;
        private static final float naturalFrequency = 3.0f;
        private static final float dampingRatio = 1.0f;
    }

    private static class Comments {
        private static final String gyroFunction = "...";
        private static final String torqueScale = "...";
        private static final String dampingScale = "...";
        private static final String massCapacity = "Maximum mass corrected per Oscillite Block, in kpg.";
        private static final String naturalFrequency = "...";
        private static final String dampingRatio = "...";
    }
}
