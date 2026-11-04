package com.nasilk.createcrystallized.util.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface IHaveLongs {
    default boolean shiftUpdateLongs(ServerLevel level, BlockState state, BlockPos pos) {
        return false;
    }

    default boolean defaultUpdateLongs(ServerLevel level, BlockState state, BlockPos pos) {
        return false;
    }
}
