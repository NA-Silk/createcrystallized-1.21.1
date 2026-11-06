package com.nasilk.createcrystallized.block.dummy;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import static com.nasilk.createcrystallized.util.helper.BlockRegistryHelper.registerBlock;

public class DummyRegistry {
    private static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final BlockEntry<Block> DUMMY_MULTIBLOCK_BLOCK = registerBlock(
        "dummy_multiblock",
        (properties) -> new DummyMultiblockBlock(properties
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((s, g, p, t) -> false)
            .isRedstoneConductor((s, g, p) -> false)
            .isSuffocating((s, g, p) -> false)
            .isViewBlocking((s, g, p) -> false)
        )
    );

    public static final BlockEntityEntry<DummyMultiblockEntity> DUMMY_MULTIBLOCK_ENTITY = REGISTRATE.blockEntity(
        "dummy_multiblock",
        DummyMultiblockEntity::new
    ).validBlock(DUMMY_MULTIBLOCK_BLOCK).register();

    public static void init() {
        CreateCrystallized.LOGGER.info("Dummy initialized");
    }
}
