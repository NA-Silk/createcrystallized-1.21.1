package com.nasilk.createcrystallized.item.custom;

import com.nasilk.createcrystallized.util.helper.TransformItemHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import java.util.HashMap;

public class TransformItem extends Item {
    public final HashMap<Block, Block> BLOCK_MAP = new HashMap<>();

    public TransformItem(Properties properties, HashMap<Block, Block> BLOCK_MAP) {
        super(properties);
        this.BLOCK_MAP.putAll(BLOCK_MAP);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return TransformItemHelper.tryTransform(context, BLOCK_MAP).orElseGet(() -> super.useOn(context));
    }
}
