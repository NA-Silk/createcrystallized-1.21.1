package com.nasilk.createcrystallized.config.server.block;

import net.createmod.catnip.config.ConfigBase;

public class WellConfig extends ConfigBase {
    public final ConfigFloat wellMinRadius    = this.f(Constants.wellMinRadius,    0.0f, "well_min_radius",    Comments.wellMinRadius);
    public final ConfigFloat wellRadiusScale  = this.f(Constants.wellRadiusScale,  0.0f, "well_radius_scale",  Comments.wellRadiusScale);
    public final ConfigFloat wellFieldScale   = this.f(Constants.wellFieldScale,   0.0f, "well_field_scale",   Comments.wellFieldScale);
    public final ConfigFloat wellImpactRadius = this.f(Constants.wellImpactRadius, 0.0f, "well_impact_radius", Comments.wellImpactRadius);
    public final ConfigFloat wellDampenRadius = this.f(Constants.wellDampenRadius, 0.0f, "well_dampen_radius", Comments.wellDampenRadius);
    public final ConfigFloat wellDampenScale  = this.f(Constants.wellDampenScale,  0.0f, "well_dampen_scale",  Comments.wellDampenScale);

    public WellConfig() {}

    @Override
    public String getName() {
        return "well_config";
    }

    private static class Constants {
        private static final float wellMinRadius = 0.0f;
        private static final float wellRadiusScale = 2.0f;
        private static final float wellFieldScale = 0.5f;
        private static final float wellImpactRadius = 0.5f;
        private static final float wellDampenRadius = 1.5f;
        private static final float wellDampenScale = 0.2f;
    }

    private static class Comments {
        private static final String wellMinRadius = "Densite Well gravity effect minimum radius.";
        private static final String wellRadiusScale = "Densite Well gravity effect radius scale.";
        private static final String wellFieldScale = "Densite Well gravity effect strength scale.";
        private static final String wellImpactRadius = "Densite Well gravity effect impact radius.";
        private static final String wellDampenRadius = "Densite Well gravity effect dampen radius.";
        private static final String wellDampenScale = "Densite Well gravity effect dampen scale.";
    }
}
