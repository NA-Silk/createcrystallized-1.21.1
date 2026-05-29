package com.nasilk.createfluidsandfixins.block;

import com.nasilk.createfluidsandfixins.CreateFluidsAndFixins;
import com.nasilk.createfluidsandfixins.behavior.DensiteCTBehavior;
import com.nasilk.createfluidsandfixins.behavior.PropulsiteCTBehavior;
import com.nasilk.createfluidsandfixins.block.custom.DensiteBlock;
import com.nasilk.createfluidsandfixins.block.custom.PebbleBlock;
import com.nasilk.createfluidsandfixins.block.custom.PropulsiteBlock;
import com.nasilk.createfluidsandfixins.block.custom.PropulsiteThrusterBlock;
import com.nasilk.createfluidsandfixins.item.ModItems;
import com.nasilk.createfluidsandfixins.item.custom.ChoraCasingItem;
import com.nasilk.createfluidsandfixins.item.custom.PebbleItem;
import com.nasilk.createfluidsandfixins.util.ModSounds;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Function;
import java.util.function.Supplier;
import com.tterrag.registrate.util.entry.BlockEntry;

@SuppressWarnings({"deprecation", "SameParameterValue"})
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(CreateFluidsAndFixins.MOD_ID);

    public static final BlockEntry<Block> DENSITE_BLOCK = registerBlockCT(
        "densite_block",
        (properties) -> new DensiteBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(2.0f, 9.0f)
            .jumpFactor(0.5f)
            .friction(0.8f)
            .lightLevel(state -> state.getValue(DensiteBlock.POWER))
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
        ),
        DensiteCTBehavior::new
    );

    public static final BlockEntry<Block> PROPULSITE_BLOCK = registerBlockCT(
        "propulsite_block",
        (properties) -> new PropulsiteBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .lightLevel(state -> 6)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
                .sound(
                    new SoundType(1.0f, 1.0f,
                        ModSounds.PROPULSITE_BREAK.get(),
                        SoundEvents.AMETHYST_BLOCK_STEP,
                        ModSounds.PROPULSITE_PLACE.get(),
                        SoundEvents.AMETHYST_BLOCK_HIT,
                        SoundEvents.AMETHYST_BLOCK_FALL
                    )
                )
        ),
        PropulsiteCTBehavior::new
    );

    public static final DeferredBlock<Block> PROPULSITE_THRUSTER = registerBlock(
        "propulsite_thruster",
        () -> new PropulsiteThrusterBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .noOcclusion()
            .lightLevel(state -> 6)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    ModSounds.PROPULSITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.PROPULSITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        )
    );

    public static final DeferredBlock<Block> OSCILLITE_BLOCK = registerBlock(
        "oscillite_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .friction(1.05f)
            .lightLevel(state -> 6)
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

    public static final DeferredBlock<Block> CHORA_CASING = registerBlockCustomItem(
        "chora_casing",
        () -> new Block(BlockBehaviour.Properties.of() // TODO change properties
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.STONE_BREAK,
                SoundEvents.STONE_STEP,
                SoundEvents.STONE_PLACE,
                SoundEvents.STONE_HIT,
                SoundEvents.STONE_FALL
            ))
        ),
        (block) -> new ChoraCasingItem(block, new Item.Properties().stacksTo(64))
    );

    public static final DeferredBlock<Block> PEBBLE = registerBlockCustomItem(
        "pebble",
        () -> new PebbleBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.STONE_BREAK,
                SoundEvents.STONE_STEP,
                ModSounds.PEBBLE_PLACE.get(),
                SoundEvents.STONE_HIT,
                SoundEvents.STONE_FALL
            ))
        ),
        (block) -> new PebbleItem(block, new Item.Properties().stacksTo(1))
    );


    // REGISTRY HELPERS
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        setBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockCustomItem(String name, Supplier<T> block, Function<T, Item> itemFactory) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> itemFactory.apply(toReturn.get()));
        return toReturn;
    }

    private static <T extends Block> BlockEntry<T> registerBlockCT(String name, NonNullFunction<BlockBehaviour.Properties, T> factory, Supplier<ConnectedTextureBehaviour> behavior) {
        BlockEntry<T> toReturn = CreateFluidsAndFixins.REGISTRATE.block(name, factory)
            .onRegister(CreateRegistrate.connectedTextures(behavior))
            .register();
        setBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> BlockEntry<T> registerBlockCTCustomItem(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, Supplier<ConnectedTextureBehaviour> behavior, Function<T, Item> itemFactory) {
        BlockEntry<T> toReturn = CreateFluidsAndFixins.REGISTRATE.block(name, blockFactory)
            .onRegister(CreateRegistrate.connectedTextures(behavior))
            .register();
        ModItems.ITEMS.register(name, () -> itemFactory.apply(toReturn.get()));
        return toReturn;
    }

    private static <T extends Block> void setBlockItem(String name, Supplier<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
