package com.nasilk.createcrystallized.block.dummy;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class DummyMultiblockBlock extends TransparentBlock implements IBE<DummyMultiblockEntity> {
    public static final BooleanProperty CONTROLLER = BooleanProperty.create("controller");
    public static final IntegerProperty MULTIBLOCK_SIZE = IntegerProperty.create("multiblock_size", 1, 3);

    public DummyMultiblockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(CONTROLLER, true)
            .setValue(MULTIBLOCK_SIZE, 1)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTROLLER).add(MULTIBLOCK_SIZE);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DummyMultiblockEntity be && be.isMultiblock()) {
                DummyMultiblockEntity.propagateMultiblockUpdate(level, be.getControllerPos(), be.getMultiblockSize().getSize());
            }
            // Fallback if the old BE is unavailable
            else if (state.getValue(CONTROLLER) && state.getValue(MULTIBLOCK_SIZE) > 1) {
                DummyMultiblockEntity.propagateMultiblockUpdate(level, pos, state.getValue(MULTIBLOCK_SIZE));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DummyMultiblockEntity be) {
            if (be.isController()) be.propagateMultiblockUpdate();
            else if (be.isMultiblockMember()) DummyMultiblockEntity.propagateMultiblockUpdate(level, be.getControllerPos(), be.getMultiblockSize().getSize());
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
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
