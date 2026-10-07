package com.nasilk.createcrystallized.block;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.block.entity.OscilliteBlockEntity;
import com.nasilk.createcrystallized.block.entity.OscilliteCannonEntity;
import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class ModBlockEntities {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final BlockEntityEntry<DensiteWellEntity> DENSITE_WELL = REGISTRATE.blockEntity(
        "densite_well",
        DensiteWellEntity::new
    ).validBlock(ModBlocks.DENSITE_WELL).register();

    public static final BlockEntityEntry<PropulsiteThrusterEntity> PROPULSITE_THRUSTER = REGISTRATE.blockEntity(
        "propulsite_thruster",
        PropulsiteThrusterEntity::new
    ).validBlock(ModBlocks.PROPULSITE_THRUSTER).register();

    public static final BlockEntityEntry<OscilliteBlockEntity> OSCILLITE_BLOCK = REGISTRATE.blockEntity(
        "oscillite_block",
        OscilliteBlockEntity::new
    ).validBlock(ModBlocks.OSCILLITE_BLOCK).register();

    public static final BlockEntityEntry<OscilliteCannonEntity> OSCILLITE_CANNON = REGISTRATE.blockEntity(
        "oscillite_cannon",
        OscilliteCannonEntity::new
    ).validBlock(ModBlocks.OSCILLITE_CANNON).register();

    public static void register() {
        CreateCrystallized.LOGGER.info("Block Entities registered");
    }
}
