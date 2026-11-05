package com.nasilk.createcrystallized.block.dummy;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.util.DeferredSoundType;

import static com.nasilk.createcrystallized.util.helper.BlockRegistryHelper.registerBlock;

public class DummyRegistry {
    private static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final BlockEntry<Block> DUMMY_BLOCK = registerBlock(
            "dummy",
            (properties) -> new DummyBlock(properties
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .strength(3.0f, 6.0f)
                .noOcclusion()
                .isValidSpawn((state, level, pos, value) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .hasPostProcess((state, pos, level) -> true)
                .emissiveRendering((state, pos, level) -> true)
                .sound(DeferredSoundType.STONE)
            )
    );

    public static final BlockEntityEntry<DummyEntity> DUMMY_ENTITY = REGISTRATE.blockEntity(
        "dummy",
        DummyEntity::new
    ).validBlock(DUMMY_BLOCK).register();

    public static void init() {
        CreateCrystallized.LOGGER.info("Dummy initialized");
    }
}
