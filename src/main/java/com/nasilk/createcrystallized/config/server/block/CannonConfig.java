package com.nasilk.createcrystallized.config.server.block;

import com.nasilk.createcrystallized.block.behavior.OscilliteCannonBehavior;
import net.createmod.catnip.config.ConfigBase;

public class CannonConfig extends ConfigBase {
    public final ConfigFloat cannonDamage            = this.f(Constants.cannonDamage,            0.0f, "cannon_damage",             Comments.cannonDamage);
    public final ConfigFloat cannonRecoil            = this.f(Constants.cannonRecoil,            0.0f, "cannon_recoil",             Comments.cannonRecoil);
    public final ConfigFloat cannonEntityKnockback   = this.f(Constants.cannonEntityKnockback,   0.0f, "cannon_entity_knockback",   Comments.cannonEntityKnockback);
    public final ConfigFloat cannonSublevelKnockback = this.f(Constants.cannonSublevelKnockback, 0.0f, "cannon_sublevel_knockback", Comments.cannonSublevelKnockback);
    public final ConfigFloat cannonMaxRange          = this.f(Constants.cannonMaxRange,          0.0f, "cannon_max_range",          Comments.cannonMaxRange);
    public final ConfigFloat cannonMaxRadius         = this.f(Constants.cannonMaxRadius,         0.0f, "cannon_max_radius",         Comments.cannonMaxRadius);

    public CannonConfig() {}

    @Override
    public void onLoad() {
        super.onLoad();
        OscilliteCannonBehavior.updateConstants();
    }

    @Override
    public void onReload() {
        super.onReload();
        OscilliteCannonBehavior.updateConstants();
    }

    @Override
    public String getName() {
        return "cannon_config";
    }

    private static class Constants {
        private static final float cannonDamage = 50.0f;
        private static final float cannonRecoil = 25.0f;
        private static final float cannonEntityKnockback = 3.0f;
        private static final float cannonSublevelKnockback = 500.0f;
        private static final float cannonMaxRange = 80.0f;
        private static final float cannonMaxRadius = 2.12f;
    }

    private static class Comments {
        private static final String cannonDamage = "Oscillite Cannon firing effect damage.";
        private static final String cannonRecoil = "Oscillite Cannon firing effect recoil.";
        private static final String cannonEntityKnockback = "Oscillite Cannon firing effect entity knockback.";
        private static final String cannonSublevelKnockback = "Oscillite Cannon firing effect sublevel knockback.";
        private static final String cannonMaxRange = "Oscillite Cannon firing effect maximum range.";
        private static final String cannonMaxRadius = "Oscillite Cannon firing effect maximum radius.";
    }
}
