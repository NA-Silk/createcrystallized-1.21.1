package com.nasilk.createcrystallized.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("unused")
public interface IHaveLongs {
    default boolean shiftUpdateLongs(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        return false;
    }

    default boolean defaultUpdateLongs(ServerLevel serverLevel, BlockState state, BlockPos pos) {
        return false;
    }
}
