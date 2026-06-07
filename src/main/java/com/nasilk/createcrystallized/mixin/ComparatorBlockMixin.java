package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.block.custom.DensiteBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComparatorBlock.class)
public class ComparatorBlockMixin {
    @Inject(method = "getInputSignal", at = @At("HEAD"), cancellable = true)
    private void getInputSignalMixin(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos facePos = pos.relative(direction);
        BlockState faceState = level.getBlockState(facePos);
        if (faceState.is(ModBlocks.DENSITE_BLOCK)) {
            cir.setReturnValue(faceState.getValue(DensiteBlock.POWER));
        }
    }
}
