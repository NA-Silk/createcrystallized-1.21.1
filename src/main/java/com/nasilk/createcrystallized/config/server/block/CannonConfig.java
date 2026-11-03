package com.nasilk.createcrystallized.config.server.block;

import com.nasilk.createcrystallized.block.behavior.OscilliteCannonBehavior;
import net.createmod.catnip.config.ConfigBase;

public class CannonConfig extends ConfigBase {
    public final ConfigInt   cannonCrystalRadius     = this.i(Constants.cannonCrystalRadius,                             "cannon_crystal_radius",       Comments.cannonCrystalRadius);
    public final ConfigFloat cannonDamage            = this.f(Constants.cannonDamage,            0.0f,              "cannon_damage",               Comments.cannonDamage);
    public final ConfigFloat cannonRecoil            = this.f(Constants.cannonRecoil,            0.0f,              "cannon_recoil",               Comments.cannonRecoil);
    public final ConfigFloat cannonEntityKnockback   = this.f(Constants.cannonEntityKnockback,   0.0f,              "cannon_entity_knockback",     Comments.cannonEntityKnockback);
    public final ConfigFloat cannonSublevelKnockback = this.f(Constants.cannonSublevelKnockback, 0.0f,              "cannon_sublevel_knockback",   Comments.cannonSublevelKnockback);
    public final ConfigFloat cannonMaxRange          = this.f(Constants.cannonMaxRange,          0.0f, 255.0f, "cannon_max_range",            Comments.cannonMaxRange);

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
        private static final int   cannonCrystalRadius = 5;
        private static final float cannonDamage = 50.0f;
        private static final float cannonRecoil = 25.0f;
        private static final float cannonEntityKnockback = 3.0f;
        private static final float cannonSublevelKnockback = 500.0f;
        private static final float cannonMaxRange = 80.0f;
    }

    private static class Comments {
        private static final String cannonCrystalRadius = "How far away the Cannon can consume crystals for ammo.";
        private static final String cannonDamage = "How much damage the Cannon deals.";
        private static final String cannonRecoil = "How much Sublevel recoil the Cannon Has upon firing.";
        private static final String cannonEntityKnockback = "How much entity knockback the Cannon deals.";
        private static final String cannonSublevelKnockback = "How much Sublevel knockback the Cannon deals.";
        private static final String cannonMaxRange = "The max range of the cannon blast (extremely high values can be performance heavy).";
    }
}
