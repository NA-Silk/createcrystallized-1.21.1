package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.fluid.ModFluids;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.nasilk.createcrystallized.util.setting.MixinSettings.*;

@Mixin(FluidDrainingBehaviour.class)
public class FluidDrainingBehaviourMixin {
    // Can it pull?
    @Inject(method = "pullNext", at = @At("HEAD"), cancellable = true)
    private void pullNextMixin(BlockPos root, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        Level world = ((BlockEntityBehaviour)(Object)this).getWorld();

        // Error check
        if (world == null) return;

        // Handle sublevels
        Vector3d pos = new Vector3d(root.getX(), root.getY(), root.getZ());
        Sable.HELPER.projectOutOfSubLevel(world, pos);

        // VOID SEA SLURRY: check if in END and y < Y_VOID_SEA_SLURRY
        if (world.dimension() == Level.END && pos.y < Y_VOID_SEA_SLURRY) {
            // Pretend there is something to pull so HosePulleyFluidHandler proceeds
            cir.setReturnValue(true);
        }

        // DRIFT CONDENSATE: check if in OVERWORLD and y > Y_DRIFT_CONDENSATE
        if (world.dimension() == Level.OVERWORLD && pos.y > Y_DRIFT_CONDENSATE) {
            // Pretend there is something to pull so HosePulleyFluidHandler proceeds
            cir.setReturnValue(true);
        }
    }

    // What can it pull?
    @Inject(method = "getDrainableFluid", at = @At("HEAD"), cancellable = true)
    private void getDrainableFluidMixin(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir) {
        Level world = ((BlockEntityBehaviour)(Object)this).getWorld();

        // Error check
        if (world == null) return;

        // Handle sublevels
        Vector3d pos = new Vector3d(rootPos.getX(), rootPos.getY(), rootPos.getZ());
        Sable.HELPER.projectOutOfSubLevel(world, pos);

        // VOID SEA SLURRY: check if in END and y < Y_VOID_SEA_SLURRY
        if (world.dimension() == Level.END && pos.y < Y_VOID_SEA_SLURRY) {
            // Return Void Sea Slurry as if it was extracted
            cir.setReturnValue(new FluidStack(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), 250));
        }

        // DRIFT CONDENSATE: check if in OVERWORLD and y > Y_DRIFT_CONDENSATE
        if (world.dimension() == Level.OVERWORLD && pos.y > Y_DRIFT_CONDENSATE) {
            // Return Drift Condensate as if it was extracted
            cir.setReturnValue(new FluidStack(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), 1000));
        }
    }
}
