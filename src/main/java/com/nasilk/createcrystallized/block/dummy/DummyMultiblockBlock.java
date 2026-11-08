package com.nasilk.createcrystallized.block.dummy;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class DummyMultiblockBlock extends TransparentBlock implements IBE<DummyMultiblockEntity> {
    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");

    public DummyMultiblockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(CONTROLLER, true)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTROLLER);
    }

    // ENTITIES
    @Override
    public Class<DummyMultiblockEntity> getBlockEntityClass() {
        return DummyMultiblockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DummyMultiblockEntity> getBlockEntityType() {
        return DummyRegistry.DUMMY_MULTIBLOCK_ENTITY.get();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        //if (level.isClientSide || type != getBlockEntityType()) return null;
        if (type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof DummyMultiblockEntity dummy) dummy.tick();
        };
    }

    // TRANSPARENCY
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return false;
    }
}
