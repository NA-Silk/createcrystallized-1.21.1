package com.nasilk.createcrystallized.common;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Usable via the following boolean methods:
 * <ol>
 *     <li>((BlockState) block).is(ModTags.Blocks.TAG)</li>
 *     <li>((ItemStack) item).is(ModTags.Items.TAG)</li>
 * </ol>
 */
@SuppressWarnings("unused")
public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_AEROLITE_TOOL = createTag("needs_aerolite_tool");
        public static final TagKey<Block> INCORRECT_FOR_AEROLITE_TOOL = createTag("incorrect_for_aerolite_tool");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(CreateCrystallized.getRL(name));
        }
    }

    public static class Items {
        public static final TagKey<Item> AEROLITE_ITEMS = createTag("aerolite_items");
        public static final TagKey<Item> CRYSTAL_ITEMS = createTag("crystal_items");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(CreateCrystallized.getRL(name));
        }
    }

    public static void init() {
        CreateCrystallized.LOGGER.info("Mod Tags initialized");
    }
}
