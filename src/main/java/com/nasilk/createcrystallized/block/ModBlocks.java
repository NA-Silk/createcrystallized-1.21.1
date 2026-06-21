package com.nasilk.createcrystallized.block;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.behavior.*;
import com.nasilk.createcrystallized.block.custom.*;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.custom.ChoraCasingItem;
import com.nasilk.createcrystallized.item.custom.PebbleItem;
import com.nasilk.createcrystallized.util.ModSounds;
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
        DeferredRegister.createBlocks(CreateCrystallized.MOD_ID);


    // BLOCK REGISTRATIONS
    /** RAW FORMS */
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
                    ModSounds.DENSITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_BLOCK_FALL
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

    public static final BlockEntry<Block> OSCILLITE_BLOCK = registerBlockCT(
        "oscillite_block",
        (properties) -> new OscilliteBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .lightLevel(state -> 6)
            .noOcclusion()
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    SoundEvents.AMETHYST_BLOCK_PLACE,
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_CLUSTER_FALL
                )
            )
        ),
        OscilliteCTBehavior::new
    );


    /** ENCASED FORMS */
    public static final BlockEntry<Block> ENCASED_DENSITE_BLOCK = registerBlockCT(
        "encased_densite_block",
        (properties) -> new Block(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(1.0f, 9.0f)
            .lightLevel(state -> 4)
            .hasPostProcess((state, pos, level) -> true)
            .emissiveRendering((state, pos, level) -> true)
            .sound(
                new SoundType(1.0f, 0.1f,
                    ModSounds.DENSITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        ),
        EncasedDensiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_PROPULSITE_BLOCK = registerBlockCT(
        "encased_propulsite_block",
        (properties) -> new Block(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .lightLevel(state -> 4)
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
        EncasedPropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_OSCILLITE_BLOCK = registerBlockCT(
        "encased_oscillite_block",
        (properties) -> new Block(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .lightLevel(state -> 4)
            .noOcclusion()
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    SoundEvents.AMETHYST_BLOCK_PLACE,
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_CLUSTER_FALL
                )
            )
        ),
        EncasedOscilliteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_LEVITITE_BLOCK = registerBlockCT(
        "encased_levitite_block",
        (properties) -> new Block(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .lightLevel(state -> 4)
            .noOcclusion()
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    SoundEvents.AMETHYST_BLOCK_BREAK,
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    SoundEvents.AMETHYST_BLOCK_PLACE,
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_CLUSTER_FALL
                )
            )
        ),
        EncasedLevititeCTBehavior::new
    );


    /** CRAFTED FORMS */
    public static final DeferredBlock<Block> DENSITE_WELL = registerBlock(
        "densite_well",
        () -> new DensiteWellBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(1.0f, 9.0f)
            .lightLevel(state -> 4)
            .hasPostProcess((state, pos, level) -> true)
            .emissiveRendering((state, pos, level) -> true)
            .sound(
                new SoundType(1.0f, 0.1f,
                    ModSounds.DENSITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        )
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

    public static final DeferredBlock<Block> OSCILLITE_CANNON = registerBlock(
            "oscillite_cannon",
            () -> new OscilliteCannonBlock(BlockBehaviour.Properties.of()
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

    /** CASINGS */
    public static final BlockEntry<Block> CHORA_CASING = registerBlockCTCustomItem(
        "chora_casing",
            (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .strength(1.0F, 1.0F)
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        ChoraCasingCTBehavior::new,
        (block) -> new ChoraCasingItem(block, new Item.Properties().stacksTo(64))
    );

    public static final BlockEntry<Block> DENSE_CHORA_CASING = registerBlockCTCustomItem(
        "chora_casing_densite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .strength(1.0F, 1.0F)
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        DenseChoraCasingCTBehavior::new,
        (block) -> new ChoraCasingItem(block, new Item.Properties().stacksTo(64))
    );

    public static final BlockEntry<Block> PROPULSED_CHORA_CASING = registerBlockCTCustomItem(
        "chora_casing_propulsite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .strength(1.0F, 1.0F)
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        PropulsedChoraCasingCTBehavior::new,
        (block) -> new ChoraCasingItem(block, new Item.Properties().stacksTo(64))
    );

    public static final BlockEntry<Block> OSCILLATING_CHORA_CASING = registerBlockCTCustomItem(
        "chora_casing_oscillite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .strength(1.0F, 1.0F)
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        OscillatingChoraCasingCTBehavior::new,
        (block) -> new ChoraCasingItem(block, new Item.Properties().stacksTo(64))
    );

    public static final BlockEntry<Block> LEVITATING_CHORA_CASING = registerBlockCTCustomItem(
        "chora_casing_levitite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .strength(1.0F, 1.0F)
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9F)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0F, 1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        LevitatingChoraCasingCTBehavior::new,
        (block) -> new ChoraCasingItem(block, new Item.Properties().stacksTo(64))
    );


    /** ADDITIONAL BLOCKS */
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

    public static final DeferredBlock<Block> AEROLITE_ORE = registerBlock(
        "aerolite_ore",
        () -> new AeroliteOreBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GRAY)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .noOcclusion()
            .lightLevel(state -> 2)
            .isValidSpawn((state, level, pos, value) -> true)
            .isRedstoneConductor((state, level, pos) -> true)
            .isSuffocating((state, level, pos) -> true)
            .isViewBlocking((state, level, pos) -> true)
            .sound(
                new SoundType(1.0f, 0.9f,
                    SoundEvents.STONE_BREAK,
                    SoundEvents.STONE_STEP,
                    SoundEvents.STONE_PLACE,
                    SoundEvents.STONE_HIT,
                    SoundEvents.STONE_FALL
                )
            )
        )
    );

    public static final DeferredBlock<Block> DEEPSLATE_AEROLITE_ORE = registerBlock(
        "deepslate_aerolite_ore",
        () -> new AeroliteOreBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3F)
            .noOcclusion()
            .lightLevel(state -> 1)
            .isValidSpawn((state, level, pos, value) -> true)
            .isRedstoneConductor((state, level, pos) -> true)
            .isSuffocating((state, level, pos) -> true)
            .isViewBlocking((state, level, pos) -> true)
            .sound(
                new SoundType(1.0f, 0.8f,
                    SoundEvents.STONE_BREAK,
                    SoundEvents.STONE_STEP,
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundEvents.STONE_HIT,
                    SoundEvents.STONE_FALL
                )
            )
        )
    );


    // REGISTRY HELPERS, please thank them before you go, they are very nice.
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
        BlockEntry<T> toReturn = CreateCrystallized.REGISTRATE.block(name, factory)
            .onRegister(CreateRegistrate.connectedTextures(behavior))
            .register();
        setBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> BlockEntry<T> registerBlockCTCustomItem(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, Supplier<ConnectedTextureBehaviour> behavior, Function<T, Item> itemFactory) {
        BlockEntry<T> toReturn = CreateCrystallized.REGISTRATE.block(name, blockFactory)
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
