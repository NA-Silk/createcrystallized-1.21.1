package com.nasilk.createcrystallized.common.behavior;

import com.nasilk.createcrystallized.fluid.ModFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;

public class ModFluidInteractions {
    public static void register() {
        registerFluidInteraction(
            Fluids.WATER.getFluidType(), ModFluids.VOID_SEA_SLURRY.getType(),
            Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()
        );
    }

    public static void registerFluidInteraction(FluidType catalystFluid, FluidType convertFluid, BlockState sourceBlock, BlockState flowingBlock) {
        FluidInteractionRegistry.InteractionInformation interaction = new FluidInteractionRegistry.InteractionInformation(
            convertFluid,
            fluidState -> fluidState.isSource() ? sourceBlock : flowingBlock
        );
        FluidInteractionRegistry.addInteraction(catalystFluid, interaction);
    }
}
