package com.nasilk.createcrystallized.config.server.block;

import net.createmod.catnip.config.ConfigBase;

public class ThrusterConfig extends ConfigBase {
    public final ConfigFloat thrusterNormStandardDeviation = this.f(Constants.thrusterNormStandardDeviation, 0.0f, "thruster_norm_standard_deviation", Comments.thrusterNormStandardDeviation);
    public final ConfigFloat thrusterNormMean              = this.f(Constants.thrusterNormMean,              0.0f, "thruster_norm_mean",               Comments.thrusterNormMean);
    public final ConfigFloat thrusterFiringAmplitude       = this.f(Constants.thrusterFiringAmplitude,       0.0f, "thruster_firing_amplitude",        Comments.thrusterFiringAmplitude);
    public final ConfigFloat thrusterVelocitySensitivity   = this.f(Constants.thrusterVelocitySensitivity,   0.0f, "thruster_velocity_sensitivity",    Comments.thrusterVelocitySensitivity);
    public final ConfigFloat thrusterVelocityThreshold     = this.f(Constants.thrusterVelocityThreshold,     0.0f, "thruster_velocity_threshold",      Comments.thrusterVelocityThreshold);
    public final ConfigFloat thrusterClusterBonusScale     = this.f(Constants.thrusterClusterBonusScale,     0.0f, "thruster_cluster_bonus_scale",     Comments.thrusterClusterBonusScale);
    public final ConfigFloat thrusterMaxEntityKnockback    = this.f(Constants.thrusterMaxEntityKnockback,    0.0f, "thruster_max_entity_knockback",    Comments.thrusterMaxEntityKnockback);
    public final ConfigFloat thrusterMaxPushRange          = this.f(Constants.thrusterMaxPushRange,          0.0f, "thruster_max_push_range",          Comments.thrusterMaxPushRange);
    public final ConfigFloat thrusterMaxPushRadius         = this.f(Constants.thrusterMaxPushRadius,         0.0f, "thruster_max_push_radius",         Comments.thrusterMaxPushRadius);
    public final ConfigFloat thrusterPushScale             = this.f(Constants.thrusterPushScale,             0.0f, "thruster_push_scale",              Comments.thrusterPushScale);
    public final ConfigFloat thrusterPushShiftReduction    = this.f(Constants.thrusterPushShiftReduction,    0.0f, "thruster_push_shift_reduction",    Comments.thrusterPushShiftReduction);
    public final ConfigFloat thrusterDamageScale           = this.f(Constants.thrusterDamageScale,           0.0f, "thruster_damage_scale",            Comments.thrusterDamageScale);

    public ThrusterConfig() {}

    @Override
    public String getName() {
        return "thruster_config";
    }

    private static class Constants {
        private static final float thrusterNormStandardDeviation = 1.5f;
        private static final float thrusterNormMean = 3.0f;
        private static final float thrusterFiringAmplitude = 100.0f;
        private static final float thrusterVelocitySensitivity = 15.0f;
        private static final float thrusterVelocityThreshold = 1.0f;
        private static final float thrusterClusterBonusScale = 2.0f;
        private static final float thrusterMaxEntityKnockback = 6.0f;
        private static final float thrusterMaxPushRange = 8.0f;
        private static final float thrusterMaxPushRadius = 0.75f;
        private static final float thrusterPushScale = 0.1f;
        private static final float thrusterPushShiftReduction = 8.0f;
        private static final float thrusterDamageScale = 5.0f;
    }

    private static class Comments {
        private static final String thrusterNormStandardDeviation = "Propulsite Thruster firing curve spread.";
        private static final String thrusterNormMean = "Propulsite Thruster firing curve center.";
        private static final String thrusterFiringAmplitude = "Propulsite Thruster firing curve total thrust output.";
        private static final String thrusterVelocitySensitivity = "Propulsite Thruster charging velocity sensitivity.";
        private static final String thrusterVelocityThreshold = "Propulsite Thruster charging velocity threshold.";
        private static final String thrusterClusterBonusScale = "Propulsite Thruster cluster bonus multiplier.";
        private static final String thrusterMaxEntityKnockback = "Propulsite Thruster maximum entity knockback.";
        private static final String thrusterMaxPushRange = "Propulsite Thruster maximum effective push range.";
        private static final String thrusterMaxPushRadius = "Propulsite Thruster maximum effective push radius.";
        private static final String thrusterPushScale = "Propulsite Thruster push strength multiplier.";
        private static final String thrusterPushShiftReduction = "Propulsite Thruster push strength reduction while holding shift.";
        private static final String thrusterDamageScale = "Propulsite Thruster damage multiplier.";
    }
}
