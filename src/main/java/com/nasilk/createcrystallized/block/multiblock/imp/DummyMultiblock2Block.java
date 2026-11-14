package com.nasilk.createcrystallized.block.multiblock.imp;

import com.nasilk.createcrystallized.block.multiblock.AbstractMultiblockBlock;
import com.nasilk.createcrystallized.block.multiblock.MultiblockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DummyMultiblock2Block extends AbstractMultiblockBlock<DummyMultiblock2Entity> {
    public DummyMultiblock2Block(Properties properties) {
        super(properties, MultiblockTypes.DUMMY_MULTIBLOCK_2);
    }

    // ENTITIES
    @Override
    public Class<DummyMultiblock2Entity> getBlockEntityClass() {
        return DummyMultiblock2Entity.class;
    }

    @Override
    public BlockEntityType<? extends DummyMultiblock2Entity> getBlockEntityType() {
        return DummyRegistry.DUMMY_MULTIBLOCK_2_ENTITY.get();
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
