package com.nasilk.createfluidsandfixins.block;

import com.nasilk.createfluidsandfixins.CreateFluidsAndFixins;
import com.nasilk.createfluidsandfixins.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(CreateFluidsAndFixins.MOD_ID);

    public static final DeferredBlock<Block> DENSITE_BLOCK = registerBlock(
        "densite_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(2.0f, 9.0f)
            .jumpFactor(0.5f)
            .friction(0.9f)
            .lightLevel(state -> 2) // Add separate glow layer texture later
            .hasPostProcess((state, pos, level) -> true)
            .emissiveRendering((state, pos, level) -> true)
            .sound(
                new SoundType(1.0f, 0.1f,
                    SoundEvents.ENDER_EYE_DEATH,
                    SoundEvents.NETHERITE_BLOCK_STEP,
                    SoundEvents.ENDER_EYE_DEATH, // Might need another sound or pitch override
                    SoundEvents.NETHERITE_BLOCK_HIT,
                    SoundEvents.NETHERITE_BLOCK_FALL
                )
            )
        )
    );

    public static final DeferredBlock<Block> PROPULSITE_BLOCK = registerBlock(
        "propulsite_block",
        () -> new TransparentBlock(BlockBehaviour.Properties.of()
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    SoundEvents.GLASS_BREAK,
                    SoundEvents.GLASS_STEP,
                    SoundEvents.GLASS_PLACE,
                    SoundEvents.GLASS_HIT,
                    SoundEvents.GLASS_FALL
                )
            )
        )
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
