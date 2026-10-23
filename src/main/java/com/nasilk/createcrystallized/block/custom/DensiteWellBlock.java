package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DensiteWellBlock extends Block implements IBE<DensiteWellEntity> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private static final VoxelShape BASE_SHAPE = Block.box(1.0d, 1.0d, 1.0d, 15.0d, 15.0d, 15.0d);

    public DensiteWellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    // NO PLACE ON WELL
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    public static VoxelShape getBaseShape() {
        return BASE_SHAPE;
    }

    // POWER LEVELS
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            int currentPower = state.getValue(BlockStateProperties.POWER);
            int newPower = level.getBestNeighborSignal(pos);
            if (currentPower != newPower) level.setBlock(pos, state.setValue(BlockStateProperties.POWER, newPower), 3);
        }
    }

    // ENTITIES
    @Override
    public Class<DensiteWellEntity> getBlockEntityClass() {
        return DensiteWellEntity.class;
    }

    @Override
    public BlockEntityType<? extends DensiteWellEntity> getBlockEntityType() {
        return ModBlockEntities.DENSITE_WELL.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        if (level.isClientSide() || type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof DensiteWellEntity well) well.tick();
        };
    }

    // PARTICLES
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving) addParticles(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ModParticleTypes.DENSITE_PARTICLES.get(),
                pos.getX() + 0.5d,
                pos.getY() + 0.5d,
                pos.getZ() + 0.5d,
                8,0.3d,0.2d,0.3d,0.05d
            );
        }
    }
}
