package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.block.ModBlocks;
import com.nasilk.createcrystallized.util.TransformItemUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.HashMap;

public class ChoraIngotItem extends Item {
    public final HashMap<Block, Block> BLOCK_MAP = new HashMap<>();

    public ChoraIngotItem(Properties properties) {
        super(properties);
        BLOCK_MAP.put(Blocks.GLASS, ModBlocks.CHORA_CASING.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return TransformItemUtil.tryTransform(context, BLOCK_MAP).orElseGet(() -> super.useOn(context));
    }
}
