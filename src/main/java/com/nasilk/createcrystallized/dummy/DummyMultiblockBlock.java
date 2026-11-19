package com.nasilk.createcrystallized.dummy;

import com.nasilk.createcrystallized.block.multiblock.AbstractMultiblockBlock;
import com.nasilk.createcrystallized.block.ModMultiblockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DummyMultiblockBlock extends AbstractMultiblockBlock<DummyMultiblockEntity> {
    public DummyMultiblockBlock(Properties properties) {
        super(properties, ModMultiblockTypes.DUMMY);
    }

    // ENTITIES
    @Override
    public Class<DummyMultiblockEntity> getBlockEntityClass() {
        return DummyMultiblockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DummyMultiblockEntity> getBlockEntityType() {
        return ModDummyRegistry.DUMMY_MULTIBLOCK_ENTITY.get();
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
