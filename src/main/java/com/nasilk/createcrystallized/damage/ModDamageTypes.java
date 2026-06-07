package com.nasilk.createcrystallized.damage;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

@SuppressWarnings("SameParameterValue")
public class ModDamageTypes {
    public static final ResourceKey<DamageType> PROPULSITE_THRUSTER = create("propulsite_thruster");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, name)
        );
    }

    public static DamageSource getSource(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
