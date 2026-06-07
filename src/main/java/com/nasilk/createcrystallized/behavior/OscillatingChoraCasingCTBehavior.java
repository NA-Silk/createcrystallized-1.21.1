package com.nasilk.createcrystallized.behavior;

import com.nasilk.createcrystallized.util.ModSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OscillatingChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
    @Nullable
    @Override
    public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        return ModSpriteShifts.OSCILLATING_CHORA_CASING;
    }
}

