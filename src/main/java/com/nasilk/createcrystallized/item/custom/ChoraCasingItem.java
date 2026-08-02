package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.util.helper.TransformItemHelper;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import java.util.HashMap;

public class ChoraCasingItem extends BlockItem {
    public final HashMap<Block, Block> BLOCK_MAP = new HashMap<>();

    public ChoraCasingItem(Block block, Properties properties) {
        super(block, properties);
        BLOCK_MAP.put(ModBlocks.DENSITE_BLOCK.get(), ModBlocks.ENCASED_DENSITE_BLOCK.get());
        BLOCK_MAP.put(ModBlocks.PROPULSITE_BLOCK.get(), ModBlocks.ENCASED_PROPULSITE_BLOCK.get());
        BLOCK_MAP.put(ModBlocks.OSCILLITE_BLOCK.get(), ModBlocks.ENCASED_OSCILLITE_BLOCK.get());
        BLOCK_MAP.put(AeroBlocks.LEVITITE.get(), ModBlocks.ENCASED_LEVITITE_BLOCK.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return TransformItemHelper.tryTransform(context, BLOCK_MAP).orElseGet(() -> super.useOn(context));
    }
}
