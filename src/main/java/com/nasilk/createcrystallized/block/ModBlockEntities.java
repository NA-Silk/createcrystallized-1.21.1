package com.nasilk.createcrystallized.block;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.block.entity.OscilliteEntity;
import com.nasilk.createcrystallized.block.entity.PropulsiteThrusterEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateCrystallized.MOD_ID);

    public static final Supplier<BlockEntityType<DensiteWellEntity>> DENSITE_WELL = BLOCK_ENTITIES.register(
        "densite_well",
        () -> BlockEntityType.Builder.of(
            DensiteWellEntity::new,
            ModBlocks.DENSITE_WELL.get()
        )
        .build(null)
    );

    public static final Supplier<BlockEntityType<PropulsiteThrusterEntity>> PROPULSITE_THRUSTER = BLOCK_ENTITIES.register(
        "propulsite_thruster",
        () -> BlockEntityType.Builder.of(
            PropulsiteThrusterEntity::new,
            ModBlocks.PROPULSITE_THRUSTER.get()
        )
        .build(null)
    );

    public static final Supplier<BlockEntityType<OscilliteEntity>> OSCILLITE_BLOCK = BLOCK_ENTITIES.register(
        "oscillite_block",
        () -> BlockEntityType.Builder.of(
            OscilliteEntity::new,
            ModBlocks.OSCILLITE_BLOCK.get()
        )
        .build(null)
    );

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
