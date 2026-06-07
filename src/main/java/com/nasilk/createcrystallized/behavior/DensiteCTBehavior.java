package com.nasilk.createcrystallized.behavior;

import com.nasilk.createcrystallized.block.custom.DensiteBlock;
import com.nasilk.createcrystallized.util.ModSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DensiteCTBehavior extends ConnectedTextureBehaviour.Base {
    @Nullable
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        int power = state.getValue(DensiteBlock.POWER);

        if (power == 0) return ModSpriteShifts.DENSITE;
        if (power <= 4) return ModSpriteShifts.DENSITE_1;
        if (power <= 8) return ModSpriteShifts.DENSITE_2;
        if (power <= 12) return ModSpriteShifts.DENSITE_3;

        return ModSpriteShifts.DENSITE_4;
    }
}
