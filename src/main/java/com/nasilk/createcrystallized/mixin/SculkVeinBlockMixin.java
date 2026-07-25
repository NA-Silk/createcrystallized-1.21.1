package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.util.SculkVeinBlockMixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkVeinBlock.class)
public class SculkVeinBlockMixin {
    @Inject(method = "attemptPlaceSculk", at = @At("HEAD"), cancellable = true)
    private void attemptPlaceSculkHeadMixin(SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        // Check attempted position and convert if a cluster
        if (SculkVeinBlockMixinHelper.tryConvertCluster(level, pos)) {
            cir.setReturnValue(true);
            return;
        }

        // Find attached block
        for (Direction direction : Direction.allShuffled(random)) {
            // Convert a cluster
            BlockPos targetPos = pos.relative(direction);
            if (SculkVeinBlockMixinHelper.tryConvertCluster(level, targetPos)) {
                cir.setReturnValue(true);
                return;
            }

            // Convert a full block and nearby clusters
            BlockState targetState = SculkVeinBlockMixinHelper.getConvertedBlockState(level.getBlockState(targetPos));
            if (targetState != null) {
                level.setBlock(targetPos, targetState, 3);
                level.playSound(null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
                SculkVeinBlockMixinHelper.convertAdjacentClusters(level, targetPos);
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "attemptPlaceSculk", at = @At("RETURN"))
    private void attemptPlaceSculkReturnMixin(SculkSpreader spreader, LevelAccessor level, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        // Check if a conversion occurred
        if (cir.getReturnValue()) {
            // Find attached block
            for (Direction direction : Direction.allShuffled(random)) {
                // Convert nearby clusters if the attached block is Sculk
                BlockPos targetPos = pos.relative(direction);
                if (level.getBlockState(targetPos).is(Blocks.SCULK)) SculkVeinBlockMixinHelper.convertAdjacentClusters(level, targetPos);
            }
        }
    }
}
