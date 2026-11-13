package com.nasilk.createcrystallized.block.dummy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DummyMultiblockBlock extends AbstractMultiblockBlock<DummyMultiblockEntity> {
    public DummyMultiblockBlock(Properties properties) {
        super(properties);
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
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return false;
    }
}
