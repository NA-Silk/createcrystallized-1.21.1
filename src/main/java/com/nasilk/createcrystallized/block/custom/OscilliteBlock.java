package com.nasilk.createcrystallized.block.custom;

import com.nasilk.createcrystallized.block.ModBlockEntities;
import com.nasilk.createcrystallized.block.entity.OscilliteBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class OscilliteBlock extends Block implements IBE<OscilliteBlockEntity> {
    public OscilliteBlock(Properties properties) {
        super(properties);
    }

    // ENTITIES
    @Override
    public Class<OscilliteBlockEntity> getBlockEntityClass() {
        return OscilliteBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OscilliteBlockEntity> getBlockEntityType() {
        return ModBlockEntities.OSCILLITE_BLOCK.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //formating is a lie told to you by big forma to sell more spaces
        if (level.isClientSide() || type != getBlockEntityType()) return null;
        return (lvl, bp, bs, be) -> {
            if (be instanceof OscilliteBlockEntity oscillite) oscillite.tick();
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
                ParticleTypes.SCULK_SOUL,
                pos.getX() + 0.5d,
                pos.getY() + 0.5d,
                pos.getZ() + 0.5d,
                8,0.5d,0.5d,0.5d,0.5d
            );
        }
    }
}
