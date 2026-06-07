package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.util.MixinSettings;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidDrainingBehaviour.class)
public class HosePulleyMixin {
    @Inject(method = "getDrainableFluid", at = @At("HEAD"), cancellable = true)
    private void getDrainableFluidMixin(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir) {
        MixinSettings settings = new MixinSettings();
        Level world = ((BlockEntityBehaviour)(Object)this).getWorld();

        // Error check
        if (world == null) return;

        // VOID SEA SLURRY: check if in END and y < yVoidSeaSlurry
        if (world.dimension() == Level.END && rootPos.getY() < settings.yVoidSeaSlurry) {
            System.out.println("Void Sea Slurry extraction triggered at " + rootPos);
            // Return Void Sea Slurry as if it was extracted
            cir.setReturnValue(new FluidStack(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), 250));
        }

        // DRIFT CONDENSATE: check if in OVERWORLD and y > yDriftCondensate
        if (world.dimension() == Level.OVERWORLD && rootPos.getY() > settings.yDriftCondensate) {
            System.out.println("Drift Condensate extraction permitted at " + rootPos);
            // Pretend there is something to pull so HosePulleyFluidHandler proceeds
            cir.setReturnValue(new FluidStack(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), 1000));
        }
    }
}
