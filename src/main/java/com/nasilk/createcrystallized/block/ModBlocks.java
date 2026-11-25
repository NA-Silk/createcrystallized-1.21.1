package com.nasilk.createcrystallized.block;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.client.ModCTBehavior;
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

public final class ModBlocks {
    /// RAW FORMS
    public static final BlockEntry<Block> DENSITE_BLOCK = registerBlock(
        "densite_block",
        (properties) -> new DensiteBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(7.0f, 20.0f)
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
        ModCTBehavior.DensiteCTBehavior::new
    );

    public static final BlockEntry<Block> UMBER_DENSITE_BLOCK = registerBlock(
        "umber_densite_block",
        (properties) -> new UmberDensiteBlock(properties
            .mapColor(MapColor.COLOR_BROWN)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(7.0f, 20.0f)
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
        ModCTBehavior.UmberDensiteCTBehavior::new
    );

    public static final BlockEntry<Block> PROPULSITE_BLOCK = registerBlock(
        "propulsite_block",
        (properties) -> new PropulsiteBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(7.0f, 20.0f)
            .lightLevel(state -> 10)
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
        ModCTBehavior.PropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> LAVENDER_PROPULSITE_BLOCK = registerBlock(
        "lavender_propulsite_block",
        (properties) -> new LavenderPropulsiteBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(7.0f, 20.0f)
            .lightLevel(state -> 10)
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
        ModCTBehavior.LavenderPropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> OSCILLITE_BLOCK = registerBlock(
        "oscillite_block",
        (properties) -> new OscilliteBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(7.0f, 20.0f)
            .lightLevel(state -> 10)
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
        ModCTBehavior.OscilliteCTBehavior::new
    );

    public static final BlockEntry<Block> AMBER_OSCILLITE_BLOCK = registerBlock(
        "amber_oscillite_block",
        (properties) -> new AmberOscilliteBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(7.0f, 20.0f)
            .lightLevel(state -> 10)
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
        ModCTBehavior.AmberOscilliteCTBehavior::new
    );

    /// ENCASED FORMS
    public static final BlockEntry<Block> ENCASED_DENSITE_BLOCK = registerBlock(
        "encased_densite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 4)
            .requiresCorrectToolForDrops()
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
        ModCTBehavior.EncasedDensiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_UMBER_DENSITE_BLOCK = registerBlock(
        "encased_umber_densite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_BROWN)
            .isRedstoneConductor((state, level, pos) -> true)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 4)
            .requiresCorrectToolForDrops()
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
            ModBlocks.UMBER_DENSE_CHORA_CASING::asItem,
            ModBlocks.UMBER_DENSITE_BLOCK,
            ModParticleTypes.DENSITE_PARTICLES,
            8,0.3d,0.2d,0.3d,0.05d
        ),
        ModCTBehavior.EncasedUmberDensiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_PROPULSITE_BLOCK = registerBlock(
        "encased_propulsite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 6)
            .requiresCorrectToolForDrops()
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
        ModCTBehavior.EncasedPropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_LAVENDER_PROPULSITE_BLOCK = registerBlock(
        "encased_lavender_propulsite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 6)
            .requiresCorrectToolForDrops()
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
            ModBlocks.LAVENDER_PROPULSED_CHORA_CASING::asItem,
            ModBlocks.LAVENDER_PROPULSITE_BLOCK,
            ModParticleTypes.PROPULSITE_PARTICLES,
            16,0.5d,0.5d,0.5d,0.25d
        ),
        ModCTBehavior.EncasedLavenderPropulsiteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_OSCILLITE_BLOCK = registerBlock(
        "encased_oscillite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 6)
            .requiresCorrectToolForDrops()
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
        ModCTBehavior.EncasedOscilliteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_AMBER_OSCILLITE_BLOCK = registerBlock(
        "encased_amber_oscillite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_ORANGE)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 6)
            .requiresCorrectToolForDrops()
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
            ModBlocks.AMBER_OSCILLATING_CHORA_CASING::asItem,
            ModBlocks.AMBER_OSCILLITE_BLOCK,
            () -> ParticleTypes.SCULK_SOUL,
            8,0.5d,0.5d,0.5d,0.5d
        ),
        ModCTBehavior.EncasedAmberOscilliteCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_LEVITITE_BLOCK = registerBlock(
        "encased_levitite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_GREEN)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 6)
            .requiresCorrectToolForDrops()
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
        ModCTBehavior.EncasedLevititeCTBehavior::new
    );

    public static final BlockEntry<Block> ENCASED_PEARLESCENT_LEVITITE_BLOCK = registerBlock(
        "encased_pearlescent_levitite_block",
        (properties) -> new EncasedBlock(properties
            .mapColor(MapColor.COLOR_PINK)
            .instrument(NoteBlockInstrument.HAT)
            .strength(5.0f, 20.0f)
            .lightLevel(state -> 6)
            .requiresCorrectToolForDrops()
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
            ModBlocks.PEARLESCENT_LEVITATING_CHORA_CASING::asItem,
            AeroBlocks.PEARLESCENT_LEVITITE,
            () -> new BlockParticleOption(
                ParticleTypes.BLOCK,
                AeroBlocks.PEARLESCENT_LEVITITE.get().defaultBlockState()
            ),
            0,0.0d,0.0d,0.0d,0.0d
        ),
        ModCTBehavior.EncasedPearlescentLevititeCTBehavior::new
    );

    /// MACHINED FORMS
    public static final BlockEntry<Block> DENSITE_WELL = registerBlock(
        "densite_well",
        (properties) -> new DensiteWellBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0f, 6.0f)
            .noOcclusion()
            .lightLevel(state -> state.getValue(DensiteWellBlock.POWER))
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
            .strength(3.0f, 6.0f)
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
            .strength(3.0f, 6.0f)
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
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.ChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> DENSE_CHORA_CASING = registerBlock(
        "chora_casing_densite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.DenseChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> UMBER_DENSE_CHORA_CASING = registerBlock(
        "chora_casing_umber_densite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BROWN) //im gonna BROWN - NoteBlockIntrument.𝓑𝓐𝓝𝓙𝓞.wav
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.UmberDenseChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> PROPULSED_CHORA_CASING = registerBlock(
        "chora_casing_propulsite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.PropulsedChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> LAVENDER_PROPULSED_CHORA_CASING = registerBlock(
        "chora_casing_lavender_propulsite",
        (properties) -> new ChoraCasingBlock(properties
                .mapColor(MapColor.COLOR_PURPLE)
                .instrument(NoteBlockInstrument.BANJO)
                .noOcclusion()
                .isViewBlocking((s,l,p) -> false)
                .strength(2.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.GLASS)
        ),
        ModCTBehavior.LavenderPropulsedChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> OSCILLATING_CHORA_CASING = registerBlock(
        "chora_casing_oscillite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.OscillatingChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> AMBER_OSCILLATING_CHORA_CASING = registerBlock(
        "chora_casing_amber_oscillite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.AmberOscillatingChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> LEVITATING_CHORA_CASING = registerBlock(
        "chora_casing_levitite",
        (properties) -> new ChoraCasingBlock(properties
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BANJO)
            .noOcclusion()
            .isViewBlocking((s,l,p) -> false)
            .strength(2.0f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.GLASS)
        ),
        ModCTBehavior.LevitatingChoraCasingCTBehavior::new
    );

    public static final BlockEntry<Block> PEARLESCENT_LEVITATING_CHORA_CASING = registerBlock(
            "chora_casing_pearlescent_levitite",
            (properties) -> new ChoraCasingBlock(properties
                    .mapColor(MapColor.COLOR_PINK)
                    .instrument(NoteBlockInstrument.BANJO)
                    .noOcclusion()
                    .isViewBlocking((s,l,p) -> false)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
            ),
            ModCTBehavior.PearlescentLevitatingChoraCasingCTBehavior::new
    );

    /// ECHO BLOCKS - Copied Amethyst registries in {@link Blocks}
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
            .requiresCorrectToolForDrops()
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
            .strength(0.9f, 1000)
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
            .strength(4.5f, 3.0f)
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

    /// Exists to occupy the creative tab icon, unobtainable
    public static final BlockEntry<Block> CREATIVE_TAB_ICON = registerBlock(
            "creative_tab_icon", Block::new
    );

    public static void init() {
        CreateCrystallized.LOGGER.info("Blocks initialized");
    }
}
