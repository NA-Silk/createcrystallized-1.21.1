package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.block.entity.OscilliteBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class OscilliteBlock extends Block implements EntityBlock {
    public OscilliteBlock(Properties properties) {
        super(properties);
    }

    // ENTITIES
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OscilliteBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof OscilliteBlockEntity oscillite) oscillite.tick();
        };
    }

    // PARTICLES TODO Make custom effects -- Custom effects?? sculk soul is already REALLY good
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving) addParticles(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void addParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                8,0.5,0.5,0.5,0.5
            );
        }
    }
}
