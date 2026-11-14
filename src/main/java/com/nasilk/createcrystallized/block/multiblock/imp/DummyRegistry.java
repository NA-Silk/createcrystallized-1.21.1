package com.nasilk.createcrystallized.block.multiblock.imp;

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

    public static final BlockEntry<Block> DUMMY_MULTIBLOCK_1_BLOCK = registerBlock(
        "dummy_multiblock_1",
        (properties) -> new DummyMultiblock1Block(properties
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
    public static final BlockEntityEntry<DummyMultiblock1Entity> DUMMY_MULTIBLOCK_1_ENTITY = REGISTRATE.blockEntity(
        "dummy_multiblock_1",
        DummyMultiblock1Entity::new
    ).validBlock(DUMMY_MULTIBLOCK_1_BLOCK).register();

    public static final BlockEntry<Block> DUMMY_MULTIBLOCK_2_BLOCK = registerBlock(
        "dummy_multiblock_2",
        (properties) -> new DummyMultiblock2Block(properties
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
    public static final BlockEntityEntry<DummyMultiblock2Entity> DUMMY_MULTIBLOCK_2_ENTITY = REGISTRATE.blockEntity(
        "dummy_multiblock_2",
        DummyMultiblock2Entity::new
    ).validBlock(DUMMY_MULTIBLOCK_2_BLOCK).register();

    public static void init() {
        CreateCrystallized.LOGGER.info("Dummy initialized");
    }
}
