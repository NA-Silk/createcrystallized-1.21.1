package com.nasilk.createcrystallized.entity;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.entity.ThrownDensiteCoreEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, CreateCrystallized.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownDensiteCoreEntity>> THROWN_DENSITE_CORE = ENTITY_TYPES.register(
        "densite_core_projectile",
        () -> EntityType.Builder.<ThrownDensiteCoreEntity>of(ThrownDensiteCoreEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build("densite_core_projectile")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
