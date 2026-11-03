package com.nasilk.createcrystallized.config.server.block;

import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import net.createmod.catnip.config.ConfigBase;

public class WellConfig extends ConfigBase {
    public final ConfigFloat wellStrengthScale = this.f(Constants.wellStrengthScale, 0.0f, "well_strength_scale", Comments.wellStrengthScale);
    public final ConfigFloat wellRadiusScale   = this.f(Constants.wellRadiusScale,   0.0f, "well_radius_scale",  Comments.wellRadiusScale);

    public WellConfig() {}

    @Override
    public void onLoad() {
        super.onLoad();
        DensiteWellEntity.updateConstants();
    }

    @Override
    public void onReload() {
        super.onReload();
        DensiteWellEntity.updateConstants();
    }

    @Override
    public String getName() {
        return "well_config";
    }

    private static class Constants {
        private static final float wellStrengthScale = 2.5f;
        private static final float wellRadiusScale = 1.3f;
    }

    private static class Comments {
        private static final String wellStrengthScale = "Pull effect strength scale.";
        private static final String wellRadiusScale = "Pull effect radius scale.";
    }
}
