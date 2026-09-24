package com.nasilk.createcrystallized.config.server;

import com.nasilk.createcrystallized.item.entity.DensiteCoreEntity;
import net.createmod.catnip.config.ConfigBase;

public class ItemConfig extends ConfigBase {
    public final ConfigFloat coreFieldRadius = this.f(Constants.coreFieldRadius, 0.0f, "core_field_radius", Comments.coreFieldRadius);
    public final ConfigFloat coreImpactRadius = this.f(Constants.coreImpactRadius, 0.0f, "core_impact_radius", Comments.coreImpactRadius);
    public final ConfigFloat coreSublevelStrength = this.f(Constants.coreSublevelStrength, 0.0f, "core_sublevel_strength", Comments.coreSublevelStrength);
    public final ConfigFloat coreEntityStrength = this.f(Constants.coreEntityStrength, 0.0f, "core_entity_strength", Comments.coreEntityStrength);

    public ItemConfig() {}

    @Override
    public void onLoad() {
        super.onLoad();
        DensiteCoreEntity.updateConstants();
    }

    @Override
    public void onReload() {
        super.onReload();
        DensiteCoreEntity.updateConstants();
    }

    @Override
    public String getName() {
        return "item_config";
    }

    private static class Constants {
        private static final float coreFieldRadius = 10.0f;
        private static final float coreImpactRadius = 1.75f;
        private static final float coreSublevelStrength = 64.0f;
        private static final float coreEntityStrength = 16.0f;
    }

    private static class Comments {
        private static final String coreFieldRadius = "Densite Core gravity effect radius.";
        private static final String coreImpactRadius = "Densite Well gravity effect impact radius.";
        private static final String coreSublevelStrength = "Densite Well gravity effect sublevel strength.";
        private static final String coreEntityStrength = "Densite Well gravity effect entity strength.";
    }
}
