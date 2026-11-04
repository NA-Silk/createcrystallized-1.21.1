package com.nasilk.createcrystallized.common.behavior;

import com.nasilk.createcrystallized.fluid.ModFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;

public class ModFluidInteractions {
    // TODO Register more interactions
    public static void register() {
        // Lava -> Void Sea Slurry: source VSS -> End Stone, flowing VSS -> Air
        registerFluidInteraction(
            Fluids.LAVA.getFluidType(), ModFluids.VOID_SEA_SLURRY.getType(),
            Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState()
        );
    }

    public static void registerFluidInteraction(FluidType convertFluid, FluidType catalystFluid, BlockState sourceBlock, BlockState flowingBlock) {
        FluidInteractionRegistry.InteractionInformation interaction = new FluidInteractionRegistry.InteractionInformation(
            convertFluid,
            fluidState -> fluidState.isSource() ? sourceBlock : flowingBlock
        );
        FluidInteractionRegistry.addInteraction(catalystFluid, interaction);
    }
}
