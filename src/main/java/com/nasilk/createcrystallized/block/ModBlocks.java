package com.nasilk.createcrystallized.block;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.ctbehavior.*;
import com.nasilk.createcrystallized.block.custom.*;
import com.nasilk.createcrystallized.item.custom.PebbleItem;
import com.nasilk.createcrystallized.client.ModSounds;
import com.nasilk.createcrystallized.particle.ModParticles;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "SameParameterValue", "unused"})
public class ModBlocks {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();


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
        DensiteCTBehavior::new, null
    );

    public static final BlockEntry<Block> PROPULSITE_BLOCK = registerBlockCT(
        "propulsite_block",
        (properties) -> new PropulsiteBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
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
        PropulsiteCTBehavior::new, null
    );

    public static final BlockEntry<Block> OSCILLITE_BLOCK = registerBlockCT(
        "oscillite_block",
        (properties) -> new OscilliteBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
            .lightLevel(state -> 6)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    ModSounds.OSCILLITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.OSCILLITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_CLUSTER_FALL
                )
            )
        ),
        OscilliteCTBehavior::new, null
    );


    /** ENCASED FORMS */
    public static final BlockEntry<Block> ENCASED_DENSITE_BLOCK = registerBlockCT(
        "encased_densite_block",
        (properties) -> new EncasedBlock(properties
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
            ),
            ModBlocks.DENSE_CHORA_CASING::asItem,
            ModBlocks.DENSITE_BLOCK,
            ModParticles.DENSITE_PARTICLES,
            8,0.3d,0.2d,0.3d,0.05d
        ),
        EncasedDensiteCTBehavior::new, null
    );

    public static final BlockEntry<Block> ENCASED_PROPULSITE_BLOCK = registerBlockCT(
        "encased_propulsite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
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
            ),
            ModBlocks.PROPULSED_CHORA_CASING::asItem,
            ModBlocks.PROPULSITE_BLOCK,
            ModParticles.PROPULSITE_PARTICLES,
            16,0.5d,0.5d,0.5d,0.25d
        ),
        EncasedPropulsiteCTBehavior::new, null
    );

    public static final BlockEntry<Block> ENCASED_OSCILLITE_BLOCK = registerBlockCT(
        "encased_oscillite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
            .lightLevel(state -> 4)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    ModSounds.OSCILLITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.OSCILLITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_CLUSTER_FALL
                )
            ),
            ModBlocks.OSCILLATING_CHORA_CASING::asItem,
            ModBlocks.OSCILLITE_BLOCK,
            () -> ParticleTypes.SCULK_SOUL,
            8,0.5d,0.5d,0.5d,0.5d
        ),
        EncasedOscilliteCTBehavior::new, null
    );

    public static final BlockEntry<Block> ENCASED_LEVITITE_BLOCK = registerBlockCT(
        "encased_levitite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
            .lightLevel(state -> 4)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    AeroSoundEvents.LEVITITE_BREAK.event(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    AeroSoundEvents.LEVITITE_PLACE.event(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_CLUSTER_FALL
                )
            ),
            ModBlocks.LEVITATING_CHORA_CASING::asItem,
            AeroBlocks.LEVITITE,
            () -> new BlockParticleOption(
                ParticleTypes.BLOCK,
                AeroBlocks.LEVITITE.get().defaultBlockState()
            ),
            0,0.0d,0.0d,0.0d,0.0d
        ),
        EncasedLevititeCTBehavior::new, null
    );


    /** MACHINED FORMS */
    public static final BlockEntry<Block> DENSITE_WELL = registerBlock(
        "densite_well",
        (properties) -> new DensiteWellBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(1.0f, 9.0f)
            .noOcclusion()
            .lightLevel(state -> 4)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
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
        ), null
    );

    public static final BlockEntry<Block> PROPULSITE_THRUSTER = registerBlock(
        "propulsite_thruster",
        (properties) -> new PropulsiteThrusterBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
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
        ), null
    );

    public static final BlockEntry<Block> OSCILLITE_CANNON = registerBlock(
        "oscillite_cannon",
        (properties) -> new OscilliteCannonBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(0.3f)
            .noOcclusion()
            .lightLevel(state -> 6)
            .isValidSpawn((state, level, pos, value) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .sound(
                new SoundType(1.0f, 1.0f,
                    ModSounds.OSCILLITE_BREAK.get(),
                    SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.OSCILLITE_PLACE.get(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        ), null
    );


    /** CASINGS */
    public static final BlockEntry<Block> CHORA_CASING = registerBlockCT(
        "chora_casing",
            (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0f, 1.0f,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        ChoraCasingCTBehavior::new, null
    );

    public static final BlockEntry<Block> DENSE_CHORA_CASING = registerBlockCT(
        "chora_casing_densite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0f, 1.0f,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        DenseChoraCasingCTBehavior::new, null
    );

    public static final BlockEntry<Block> PROPULSED_CHORA_CASING = registerBlockCT(
        "chora_casing_propulsite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0f, 1.0f,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        PropulsedChoraCasingCTBehavior::new, null
    );

    public static final BlockEntry<Block> OSCILLATING_CHORA_CASING = registerBlockCT(
        "chora_casing_oscillite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0f, 1.0f,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        OscillatingChoraCasingCTBehavior::new, null
    );

    public static final BlockEntry<Block> LEVITATING_CHORA_CASING = registerBlockCT(
        "chora_casing_levitite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(new SoundType(
                1.0f, 1.0f,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
            ))
        ),
        LevitatingChoraCasingCTBehavior::new, null
    );


    /** ECHO BLOCKS - Copied Amethyst registries in Blocks.class */
    public static final BlockEntry<Block> ECHO_CRYSTAL_BLOCK = registerBlock(
        "echo_crystal_block",
        (properties) -> new AmethystBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(1.5f)
            .sound(SoundType.AMETHYST)
            .requiresCorrectToolForDrops()
        ), null
    );

    public static final BlockEntry<Block> BUDDING_ECHO_CRYSTAL = registerBlock(
        "budding_echo_crystal",
        (properties) -> new BuddingEchoCrystalBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .randomTicks()
            .strength(1.5f)
            .sound(SoundType.AMETHYST)
            .requiresCorrectToolForDrops()
            .pushReaction(PushReaction.DESTROY)
        ), null
    );

    public static final BlockEntry<Block> ECHO_CRYSTAL_CLUSTER = registerBlock(
        "echo_crystal_cluster",
        (properties) -> new AmethystClusterBlock(7.0f, 3.0f, properties
            .mapColor(MapColor.COLOR_PURPLE)
            .forceSolidOn()
            .noOcclusion()
            .strength(1.5f)
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.AMETHYST_CLUSTER)
            .lightLevel((l) -> 5)
        ), null
    );

    public static final BlockEntry<Block> LARGE_ECHO_CRYSTAL_BUD = registerBlock(
        "large_echo_crystal_bud",
        (properties) -> new AmethystClusterBlock(5.0f, 3.0f, properties
            .mapColor(MapColor.COLOR_PURPLE)
            .forceSolidOn()
            .noOcclusion()
            .strength(1.5f)
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.LARGE_AMETHYST_BUD)
            .lightLevel((l) -> 4)
        ), null
    );

    public static final BlockEntry<Block> MEDIUM_ECHO_CRYSTAL_BUD = registerBlock(
        "medium_echo_crystal_bud",
        (properties) -> new AmethystClusterBlock(4.0f, 3.0f, properties
            .mapColor(MapColor.COLOR_PURPLE)
            .forceSolidOn()
            .noOcclusion()
            .strength(1.5f)
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.MEDIUM_AMETHYST_BUD)
            .lightLevel((l) -> 2)
        ), null
    );

    public static final BlockEntry<Block> SMALL_ECHO_CRYSTAL_BUD = registerBlock(
        "small_echo_crystal_bud",
        (properties) -> new AmethystClusterBlock(3.0f, 4.0f, properties
            .mapColor(MapColor.COLOR_PURPLE)
            .forceSolidOn()
            .noOcclusion()
            .strength(1.5f)
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.SMALL_AMETHYST_BUD)
            .lightLevel((l) -> 1)
        ), null
    );


    /** ADDITIONAL BLOCKS */
    public static final BlockEntry<Block> PEBBLE = registerBlockCustomItem(
        "pebble",
        (properties) -> new PebbleBlock(properties
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .sound(new SoundType(
                1.0f, 1.0f,
                SoundEvents.STONE_BREAK,
                SoundEvents.STONE_STEP,
                ModSounds.PEBBLE_PLACE.get(),
                SoundEvents.STONE_HIT,
                SoundEvents.STONE_FALL
            ))
        ),
        (block) -> new PebbleItem(block, new Item.Properties().stacksTo(1))
    );

    public static final BlockEntry<Block> PROPULSITE_CRYSTAL = registerBlock(
        "propulsite_crystal",
        (properties) -> new AmethystClusterBlock(5.0f, 3.0f, properties
            .mapColor(MapColor.COLOR_YELLOW)
            .forceSolidOn()
            .noOcclusion()
            .strength(1.5f)
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.LARGE_AMETHYST_BUD)
            .lightLevel((l) -> 8)
        ),
        new Item.Properties().stacksTo(16)
    );

    public static final BlockEntry<Block> AEROLITE_ORE = registerBlock(
        "aerolite_ore",
        (properties) -> new AeroliteOreBlock(properties
            .mapColor(MapColor.COLOR_LIGHT_GRAY)
            .instrument(NoteBlockInstrument.HAT)
            .requiresCorrectToolForDrops()
            .strength(3.0f)
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
        ), null
    );

    public static final BlockEntry<Block> DEEPSLATE_AEROLITE_ORE = registerBlock(
        "deepslate_aerolite_ore",
        (properties) -> new AeroliteOreBlock(properties
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.HAT)
            .requiresCorrectToolForDrops()
            .strength(3.0f)
            .isValidSpawn((state, level, pos, value) -> true)
            .isRedstoneConductor((state, level, pos) -> true)
            .isSuffocating((state, level, pos) -> true)
            .isViewBlocking((state, level, pos) -> true)
            .sound(
                new SoundType(1.0f, 1.4f,
                    SoundEvents.DEEPSLATE_BREAK,
                    SoundEvents.DEEPSLATE_STEP,
                    SoundEvents.DEEPSLATE_PLACE,
                    SoundEvents.DEEPSLATE_HIT,
                    SoundEvents.DEEPSLATE_FALL
                )
            )
        ), null
    );

    public static final BlockEntry<Block> AEROLITE_BLOCK = registerBlock(
        "aerolite_block",
        (properties) -> new AeroliteOreBlock(properties
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(
                new SoundType(1.0f, 1.2f,
                    SoundEvents.METAL_BREAK,
                    SoundEvents.METAL_STEP,
                    SoundEvents.METAL_PLACE,
                    SoundEvents.METAL_HIT,
                    SoundEvents.METAL_FALL
                )
            )
        ), null
    );

    public static final BlockEntry<Block> CHORA_BLOCK = registerBlock(
        "chora_block",
        (properties) -> new Block(properties
            .mapColor(MapColor.COLOR_RED)
            .strength(5.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(
                new SoundType(1.0f, 1.0f,
                    SoundEvents.METAL_BREAK,
                    SoundEvents.METAL_STEP,
                    SoundEvents.METAL_PLACE,
                    SoundEvents.METAL_HIT,
                    SoundEvents.METAL_FALL
                )
            )
        ), null
    );


    // REGISTRY HELPERS, please thank them before you go, they are very nice.
    private static <T extends Block> BlockEntry<T> registerBlock(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, @Nullable Item.Properties itemProperties) {
        BlockEntry<T> blockEntry = REGISTRATE.block(name, blockFactory).register();
        setBlockItem(name, blockEntry, itemProperties);
        return blockEntry;
    }

    private static <T extends Block> BlockEntry<T> registerBlockCustomItem(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, Function<T, Item> itemFactory) {
        BlockEntry<T> blockEntry = REGISTRATE.block(name, blockFactory).register();
        setCustomBlockItem(name, blockEntry, itemFactory);
        return blockEntry;
    }

    private static <T extends Block> BlockEntry<T> registerBlockCT(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, Supplier<ConnectedTextureBehaviour> behavior, @Nullable Item.Properties itemProperties) {
        BlockEntry<T> blockEntry = REGISTRATE.block(name, blockFactory)
            .onRegister(CreateRegistrate.connectedTextures(behavior))
            .register();
        setBlockItem(name, blockEntry, itemProperties);
        return blockEntry;
    }

    private static <T extends Block> BlockEntry<T> registerBlockCTCustomItem(String name, NonNullFunction<BlockBehaviour.Properties, T> blockFactory, Supplier<ConnectedTextureBehaviour> behavior, Function<T, Item> itemFactory) {
        BlockEntry<T> blockEntry = REGISTRATE.block(name, blockFactory)
            .onRegister(CreateRegistrate.connectedTextures(behavior))
            .register();
        setCustomBlockItem(name, blockEntry, itemFactory);
        return blockEntry;
    }

    private static <T extends Block> void setBlockItem(String name, BlockEntry<T> blockEntry, @Nullable Item.Properties itemProperties) {
        REGISTRATE.item(name, (properties) -> new BlockItem(blockEntry.get(), Objects.requireNonNullElse(itemProperties, properties))).register();
    }

    private static <T extends Block> void setCustomBlockItem(String name, BlockEntry<T> blockEntry, Function<T, Item> itemFactory) {
        REGISTRATE.item(name, (properties) -> itemFactory.apply(blockEntry.get())).register();
    }

    public static void register() {}
}
