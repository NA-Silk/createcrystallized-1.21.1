package com.nasilk.createcrystallized.item;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.item.entity.DensiteCoreItemEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

public class ModItemEntities {
    private static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final EntityEntry<DensiteCoreItemEntity> THROWN_DENSITE_CORE = REGISTRATE.<DensiteCoreItemEntity>entity(
        "densite_core_projectile",
        DensiteCoreItemEntity::new,
        MobCategory.MISC
    ).properties(properties ->
        properties.sized(0.5f, 0.5f).clientTrackingRange(12).updateInterval(1)
    ).register();

    public static void init() {
        CreateCrystallized.LOGGER.info("Item Entities initialized");
    }
}
