package com.nasilk.createcrystallized.client;

import com.nasilk.createcrystallized.block.custom.DensiteBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ModCTBehavior {
    public static final class ChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.CHORA_CASING;
        }
    }

    // you like krabby patties dont you squidward
    public static final class DenseChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.DENSE_CHORA_CASING;
        }
    }

    public static final class UmberDenseChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.UMBER_DENSE_CHORA_CASING;
        }
    }

    public static final class DensiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            int power = state.getValue(DensiteBlock.POWER);
            if (power == 0) return ModSpriteShifts.DENSITE;
            if (power <= 4) return ModSpriteShifts.DENSITE_1;
            if (power <= 8) return ModSpriteShifts.DENSITE_2;
            if (power <= 12) return ModSpriteShifts.DENSITE_3;
            return ModSpriteShifts.DENSITE_4;
        }
    }

    public static final class UmberDensiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            int power = state.getValue(DensiteBlock.POWER);
            if (power == 0) return ModSpriteShifts.UMBER_DENSITE;
            if (power <= 4) return ModSpriteShifts.UMBER_DENSITE_1;
            if (power <= 8) return ModSpriteShifts.UMBER_DENSITE_2;
            if (power <= 12) return ModSpriteShifts.UMBER_DENSITE_3;
            return ModSpriteShifts.UMBER_DENSITE_4;
        }
    }

    public static final class EncasedDensiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_DENSITE;
        }
    }

    public static final class EncasedUmberDensiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_UMBER_DENSITE;
        }
    }

    public static final class EncasedLevititeCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_LEVITITE;
        }
    }

    public static final class EncasedPearlescentLevititeCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_PEARLESCENT_LEVITITE;
        }
    }

    public static final class EncasedOscilliteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_OSCILLITE;
        }
    }

    public static final class EncasedAmberOscilliteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_AMBER_OSCILLITE;
        }
    }

    public static final class EncasedPropulsiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_PROPULSITE;
        }
    }

    public static final class EncasedLavenderPropulsiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.ENCASED_LAVENDER_PROPULSITE;
        }
    }

    public static final class LevitatingChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.LEVITATING_CHORA_CASING;
        }
    }

    public static final class PearlescentLevitatingChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.PEARLESCENT_LEVITATING_CHORA_CASING;
        }
    }

    public static final class OscillatingChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.OSCILLATING_CHORA_CASING;
        }
    }

    public static final class AmberOscillatingChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.AMBER_OSCILLATING_CHORA_CASING;
        }
    }

    public static final class OscilliteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.OSCILLITE;
        }
    }

    public static final class AmberOscilliteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.AMBER_OSCILLITE;
        }
    }

    public static final class PropulsedChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.PROPULSED_CHORA_CASING;
        }
    }

    public static final class LavenderPropulsedChoraCasingCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.LAVENDER_PROPULSED_CHORA_CASING;
        }
    }

    public static final class PropulsiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.PROPULSITE;
        }
    }

    public static final class LavenderPropulsiteCTBehavior extends ConnectedTextureBehaviour.Base {
        @Nullable @Override
        public CTSpriteShiftEntry getShift (BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
            return ModSpriteShifts.LAVENDER_PROPULSITE;
        }
    }
}
