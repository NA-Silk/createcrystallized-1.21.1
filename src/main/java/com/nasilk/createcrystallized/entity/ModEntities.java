package com.nasilk.createcrystallized.entity;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.entity.DensiteCoreEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final EntityEntry<DensiteCoreEntity> THROWN_DENSITE_CORE = REGISTRATE.<DensiteCoreEntity>entity(
        "densite_core_projectile",
        DensiteCoreEntity::new,
        MobCategory.MISC
    ).properties(properties ->
        properties.sized(0.5f, 0.5f).clientTrackingRange(12).updateInterval(1)
    ).register();

    public static void register() {
        CreateCrystallized.LOGGER.info("Entities registered");
    }
}
