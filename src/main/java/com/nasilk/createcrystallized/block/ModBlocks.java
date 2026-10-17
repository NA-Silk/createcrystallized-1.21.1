package com.nasilk.createcrystallized.block;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.client.behavior.*;
import com.nasilk.createcrystallized.block.custom.*;
import com.nasilk.createcrystallized.item.custom.PebbleItem;
import com.nasilk.createcrystallized.common.ModSounds;
import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.util.helper.AdvancementHelper;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import static com.nasilk.createcrystallized.util.helper.BlockRegistryHelper.registerBlock;

public class ModBlocks {
    /// RAW FORMS
    public static final BlockEntry<Block> DENSITE_BLOCK = registerBlock(
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
                new DeferredSoundType(1.0f, 0.1f,
                    ModSounds.DENSITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        ),
        DensiteCTBehavior::new
    );

    public static final BlockEntry<Block> PROPULSITE_BLOCK = registerBlock(
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
                    new DeferredSoundType(1.0f, 1.0f,
                        ModSounds.PROPULSITE_BREAK,
                        () -> SoundEvents.AMETHYST_BLOCK_STEP,
                        ModSounds.PROPULSITE_PLACE,
                        () -> SoundEvents.AMETHYST_BLOCK_HIT,
                        () -> SoundEvents.AMETHYST_BLOCK_FALL
                    )
                )
        ),
        PropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> OSCILLITE_BLOCK = registerBlock(
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
                new DeferredSoundType(1.0f, 1.0f,
                    ModSounds.OSCILLITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.OSCILLITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_CLUSTER_FALL
                )
            )
        ),
        OscilliteCTBehavior::new
    );

    /// ENCASED FORMS
    public static final BlockEntry<Block> ENCASED_DENSITE_BLOCK = registerBlock(
        "encased_densite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(1.0f, 9.0f)
            .lightLevel(state -> 4)
            .hasPostProcess((state, pos, level) -> true)
            .emissiveRendering((state, pos, level) -> true)
            .sound(
                new DeferredSoundType(1.0f, 0.1f,
                    ModSounds.DENSITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            ),
            ModBlocks.DENSE_CHORA_CASING::asItem,
            ModBlocks.DENSITE_BLOCK,
            ModParticleTypes.DENSITE_PARTICLES,
            8,0.3d,0.2d,0.3d,0.05d
        ),
        EncasedDensiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_PROPULSITE_BLOCK = registerBlock(
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
                new DeferredSoundType(1.0f, 1.0f,
                    ModSounds.PROPULSITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.PROPULSITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            ),
            ModBlocks.PROPULSED_CHORA_CASING::asItem,
            ModBlocks.PROPULSITE_BLOCK,
            ModParticleTypes.PROPULSITE_PARTICLES,
            16,0.5d,0.5d,0.5d,0.25d
        ),
        EncasedPropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_OSCILLITE_BLOCK = registerBlock(
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
                new DeferredSoundType(1.0f, 1.0f,
                    ModSounds.OSCILLITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.OSCILLITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_CLUSTER_FALL
                )
            ),
            ModBlocks.OSCILLATING_CHORA_CASING::asItem,
            ModBlocks.OSCILLITE_BLOCK,
            () -> ParticleTypes.SCULK_SOUL,
            8,0.5d,0.5d,0.5d,0.5d
        ),
        EncasedOscilliteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_LEVITITE_BLOCK = registerBlock(
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
                new DeferredSoundType(1.0f, 1.0f,
                    AeroSoundEvents.LEVITITE_BREAK::event,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    AeroSoundEvents.LEVITITE_PLACE::event,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_CLUSTER_FALL
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
        EncasedLevititeCTBehavior::new
    );

    /// MACHINED FORMS
    public static final BlockEntry<Block> DENSITE_WELL = registerBlock(
        "densite_well",
        (properties) -> new DensiteWellBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
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
                new DeferredSoundType(1.0f, 0.1f,
                    ModSounds.DENSITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.DENSITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        )
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
                new DeferredSoundType(1.0f, 1.0f,
                    ModSounds.PROPULSITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.PROPULSITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        )
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
                new DeferredSoundType(1.0f, 1.0f,
                    ModSounds.OSCILLITE_BREAK,
                    () -> SoundEvents.AMETHYST_BLOCK_STEP,
                    ModSounds.OSCILLITE_PLACE,
                    () -> SoundEvents.AMETHYST_BLOCK_HIT,
                    () -> SoundEvents.AMETHYST_BLOCK_FALL
                )
            )
        )
    );

    /// CASINGS
    public static final BlockEntry<Block> CHORA_CASING = registerBlock(
        "chora_casing",
            (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> DENSE_CHORA_CASING = registerBlock(
        "chora_casing_densite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        DenseChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> PROPULSED_CHORA_CASING = registerBlock(
        "chora_casing_propulsite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        PropulsedChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> OSCILLATING_CHORA_CASING = registerBlock(
        "chora_casing_oscillite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        OscillatingChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> LEVITATING_CHORA_CASING = registerBlock(
        "chora_casing_levitite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        LevitatingChoraCasingCTBehavior::new
    );

    /// ECHO BLOCKS - Copied Amethyst registries in Blocks.class
    public static final BlockEntry<Block> ECHO_CRYSTAL_BLOCK = registerBlock(
        "echo_crystal_block",
        (properties) -> new AmethystBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(1.5f)
            .sound(SoundType.AMETHYST)
            .requiresCorrectToolForDrops()
        )
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
        )
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
        ) {
            @Override
            public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
                AdvancementHelper.yoinkerSploinker(player, "sonic_shards");
                return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            }
        }
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
        )
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
        )
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
        )
    );

    /// ADDITIONAL BLOCKS
    public static final BlockEntry<Block> PEBBLE = registerBlock(
        "pebble",
        (properties) -> new PebbleBlock(properties
            .mapColor(MapColor.COLOR_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(0.9f)
            .sound(new DeferredSoundType(
                1.0f, 1.0f,
                () -> SoundEvents.STONE_BREAK,
                () -> SoundEvents.STONE_STEP,
                ModSounds.PEBBLE_PLACE,
                () -> SoundEvents.STONE_HIT,
                () -> SoundEvents.STONE_FALL
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

    public static final BlockEntry<Block> DENSITE_CRYSTAL = registerBlock(
        "densite_crystal",
        (properties) -> new AmethystClusterBlock(5.0f, 3.0f, properties
            .mapColor(MapColor.COLOR_PURPLE)
            .forceSolidOn()
            .noOcclusion()
            .strength(1.5f)
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.LARGE_AMETHYST_BUD)
            .lightLevel((l) -> 8)
        ),
        new Item.Properties().stacksTo(16)
    );

    public static final BlockEntry<Block> LEVITITE_CRYSTAL = registerBlock(
        "levitite_crystal",
        (properties) -> new AmethystClusterBlock(5.0f, 3.0f, properties
            .mapColor(MapColor.COLOR_GREEN)
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
                new DeferredSoundType(1.0f, 0.9f,
                    () -> SoundEvents.STONE_BREAK,
                    () -> SoundEvents.STONE_STEP,
                    () -> SoundEvents.STONE_PLACE,
                    () -> SoundEvents.STONE_HIT,
                    () -> SoundEvents.STONE_FALL
                )
            )
        )
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
                new DeferredSoundType(1.0f, 1.4f,
                    () -> SoundEvents.DEEPSLATE_BREAK,
                    () -> SoundEvents.DEEPSLATE_STEP,
                    () -> SoundEvents.DEEPSLATE_PLACE,
                    () -> SoundEvents.DEEPSLATE_HIT,
                    () -> SoundEvents.DEEPSLATE_FALL
                )
            )
        )
    );

    public static final BlockEntry<Block> AEROLITE_BLOCK = registerBlock(
        "aerolite_block",
        (properties) -> new AeroliteOreBlock(properties
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(
                new DeferredSoundType(1.0f, 1.2f,
                    () -> SoundEvents.METAL_BREAK,
                    () -> SoundEvents.METAL_STEP,
                    () -> SoundEvents.METAL_PLACE,
                    () -> SoundEvents.METAL_HIT,
                    () -> SoundEvents.METAL_FALL
                )
            )
        )
    );

    public static final BlockEntry<Block> CHORA_BLOCK = registerBlock(
        "chora_block",
        (properties) -> new Block(properties
            .mapColor(MapColor.COLOR_RED)
            .strength(5.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(
                new DeferredSoundType(1.0f, 0.8f,
                    () -> SoundEvents.METAL_BREAK,
                    () -> SoundEvents.METAL_STEP,
                    () -> SoundEvents.METAL_PLACE,
                    () -> SoundEvents.METAL_HIT,
                    () -> SoundEvents.METAL_FALL
                )
            )
        )
    );

    public static void init() {
        CreateCrystallized.LOGGER.info("Blocks initialized");
    }
}
