package com.nasilk.createcrystallized.common;

import com.nasilk.createcrystallized.fluid.ModFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;

public final class ModFluidInteractions {
    // TODO Register actual interactions V2
    public static void register() {
        // Lava -> Void Sea Slurry: source VSS -> End Stone, flowing VSS -> Air
      /*  registerFluidInteraction(
            Fluids.LAVA.getFluidType(), ModFluids.VOID_SEA_SLURRY.getType(),
            Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()
        );
      */
    }

    public static void registerFluidInteraction(FluidType catalystFluid, FluidType convertFluid, BlockState sourceBlock, BlockState flowingBlock) {
        FluidInteractionRegistry.InteractionInformation interaction = new FluidInteractionRegistry.InteractionInformation(
            catalystFluid,
            fluidState -> fluidState.isSource() ? sourceBlock : flowingBlock
        );
        FluidInteractionRegistry.addInteraction(convertFluid, interaction);
    }
}
