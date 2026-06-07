package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.util.MixinSettings;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OpenEndedPipe.class)
public class OpenEndedPipeMixin {
    @Shadow private Level world;
    @Shadow private BlockPos outputPos;

    @Inject(method = "removeFluidFromSpace", at = @At("HEAD"), cancellable = true)
    private void removeFluidFromSpaceMixin(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        MixinSettings settings = new MixinSettings();

        // Error check
        if (world == null) return;

        // VOID SEA SLURRY: check if in END and y < yVoidSeaSlurry
        if (world.dimension() == Level.END && outputPos.getY() < settings.yVoidSeaSlurry) {
            System.out.println("Void Sea Slurry extraction triggered at " + outputPos);
            // Return Void Sea Slurry as if it was extracted
            cir.setReturnValue(new FluidStack(ModFluids.SOURCE_VOID_SEA_SLURRY.get(), 250));
        }

        // DRIFT CONDENSATE: check if in OVERWORLD and y > yDriftCondensate
        if (world.dimension() == Level.OVERWORLD && outputPos.getY() > settings.yDriftCondensate) {
            System.out.println("Drift Condensate extraction permitted at " + outputPos);
            // Pretend there is something to pull so HosePulleyFluidHandler proceeds
            cir.setReturnValue(new FluidStack(ModFluids.SOURCE_DRIFT_CONDENSATE.get(), 1000));
        }
    }
}
