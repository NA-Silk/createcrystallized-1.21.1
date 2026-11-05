package com.nasilk.createcrystallized.block.dummy;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DummyBlock extends Block implements IBE<DummyEntity> {
    public DummyBlock(Properties properties) {
        super(properties);
    }

    // ENTITIES
    @Override
    public Class<DummyEntity> getBlockEntityClass() {
        return DummyEntity.class;
    }

    @Override
    public BlockEntityType<? extends DummyEntity> getBlockEntityType() {
        return DummyRegistry.DUMMY_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        if (level.isClientSide || type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof DummyEntity dummy) dummy.tick();
        };
    }
}
