package com.nasilk.createcrystallized.dummy;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.custom.DensiteWellBlock;
import com.nasilk.createcrystallized.common.ModSounds;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import static com.nasilk.createcrystallized.util.helper.BlockRegistryHelper.registerBlock;

public final class ModDummyRegistry {
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

    public static final BlockEntry<Block> DENSITE_WELL_MULTIBLOCK_BLOCK = registerBlock(
        "densite_well_multiblock",
        (properties) -> new DensiteWellMultiblockBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 6.0f)
            .noOcclusion()
            .lightLevel(state -> state.getValue(DensiteWellBlock.POWER))
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .hasPostProcess((state, pos, level) -> true)
            .emissiveRendering((state, pos, level) -> true)
            .sound(
                new DeferredSoundType(1.0f, 0.1f,
                    ModSounds.DENSITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        )
    );

    public static final BlockEntityEntry<DensiteWellMultiblockEntity> DENSITE_WELL_MULTIBLOCK_ENTITY = REGISTRATE.blockEntity(
        "densite_well_multiblock",
        DensiteWellMultiblockEntity::new
    ).validBlock(DENSITE_WELL_MULTIBLOCK_BLOCK).register();

    public static void init() {
        CreateCrystallized.LOGGER.info("Dummy initialized");
    }
}
