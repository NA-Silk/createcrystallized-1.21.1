package com.nasilk.createcrystallized.mixin;

import com.nasilk.createcrystallized.fluid.ModFluids;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import com.simibubi.create.content.fluids.OpenEndedPipe;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.nasilk.createcrystallized.util.setting.MixinSettings.*;

@Mixin(OpenEndedPipe.class)
public class OpenEndedPipeMixin {
    @Shadow private Level world;
    @Shadow private BlockPos outputPos;

    @Inject(method = "removeFluidFromSpace", at = @At("HEAD"), cancellable = true)
    private void removeFluidFromSpaceMixin(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        // Error check
        if (world == null) return;

        // Handle sublevels
        Vector3d pos = new Vector3d(outputPos.getX(), outputPos.getY(), outputPos.getZ());
        Sable.HELPER.projectOutOfSubLevel(world, pos);

        // VOID SEA SLURRY: check if in END and y < yVoidSeaSlurry
        if (world.dimension() == Level.END && pos.y < Y_VOID_SEA_SLURRY) {
            // Return Void Sea Slurry as if it was extracted
            if (!simulate) AdvancementHelper.hitEmWithTheOlSwitcheroo(world, outputPos, "the_fluid_in_the_sea_of_the_end");
            cir.setReturnValue(new FluidStack(ModFluids.VOID_SEA_SLURRY.get(), 250));
        }

        // DRIFT CONDENSATE: check if in OVERWORLD and y > yDriftCondensate
        if (world.dimension() == Level.OVERWORLD && pos.y > Y_DRIFT_CONDENSATE) {
            // Pretend there is something to pull so HosePulleyFluidHandler proceeds
            if (!simulate) AdvancementHelper.hitEmWithTheOlSwitcheroo(world, outputPos, "out_of_thin_air");
            cir.setReturnValue(new FluidStack(ModFluids.DRIFT_CONDENSATE.get(), 1000));
        }
    }
}
