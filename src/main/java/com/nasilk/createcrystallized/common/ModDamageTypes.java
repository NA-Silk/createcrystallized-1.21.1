package com.nasilk.createcrystallized.common;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> PROPULSITE_THRUSTER = create("propulsite_thruster");
    public static final ResourceKey<DamageType> OSCILLITE_CANNON = create("oscillite_cannon");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, CreateCrystallized.getRL(name));
    }

    public static DamageSource getSource(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
