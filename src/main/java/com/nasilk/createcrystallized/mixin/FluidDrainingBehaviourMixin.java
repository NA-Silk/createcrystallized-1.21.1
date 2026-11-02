package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
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
        // Get level
        Level level = ((BlockEntityBehaviour) (Object) this).getWorld();
        if (level == null) return;

        // Handle sublevels
        Vector3d pos = new Vector3d(root.getX(), root.getY(), root.getZ());
        Sable.HELPER.projectOutOfSubLevel(level, pos);

        // VOID SEA SLURRY: check if in END and y < Y_VOID_SEA_SLURRY
        if (level.dimension() == Level.END && pos.y < Y_VOID_SEA_SLURRY)
            cir.setReturnValue(true);

        // DRIFT CONDENSATE: check if in OVERWORLD and y > Y_DRIFT_CONDENSATE
        if (level.dimension() == Level.OVERWORLD && pos.y > Y_DRIFT_CONDENSATE)
            cir.setReturnValue(true);
    }

    // What can it pull?
    @Inject(method = "getDrainableFluid", at = @At("HEAD"), cancellable = true)
    private void getDrainableFluidMixin(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir) {
        // Get level
        Level level = ((BlockEntityBehaviour) (Object) this).getWorld();
        if (level == null) return;

        // Handle sublevels
        Vector3d pos = new Vector3d(rootPos.getX(), rootPos.getY(), rootPos.getZ());
        Sable.HELPER.projectOutOfSubLevel(level, pos);

        // VOID SEA SLURRY: check if in END and y < Y_VOID_SEA_SLURRY
        if (level.dimension() == Level.END && pos.y < Y_VOID_SEA_SLURRY) {
            // Return Void Sea Slurry as if it was extracted
            AdvancementHelper.hitEmWithTheOlSwitcheroo(level, rootPos, "the_fluid_in_the_sea_of_the_end");
            cir.setReturnValue(new FluidStack(ModFluids.VOID_SEA_SLURRY.get(), 250));
        }

        // DRIFT CONDENSATE: check if in OVERWORLD and y > Y_DRIFT_CONDENSATE
        if (level.dimension() == Level.OVERWORLD && pos.y > Y_DRIFT_CONDENSATE) {
            // Return Drift Condensate as if it was extracted
            AdvancementHelper.hitEmWithTheOlSwitcheroo(level, rootPos, "out_of_thin_air");
            cir.setReturnValue(new FluidStack(ModFluids.DRIFT_CONDENSATE.get(), 1000));
        }
    }
}
