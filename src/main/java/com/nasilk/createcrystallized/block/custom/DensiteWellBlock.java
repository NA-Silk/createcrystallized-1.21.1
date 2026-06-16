package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class DensiteWellBlock extends Block implements EntityBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public DensiteWellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DensiteWellEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
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
                ModParticles.DENSITE_PARTICLES.get(),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                8,0.3,0.2,0.3,0.05
            );
        }
    }
}
