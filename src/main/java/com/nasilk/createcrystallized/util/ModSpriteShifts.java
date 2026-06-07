package com.nasilk.createcrystallized.util;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class ModSpriteShifts {
    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(
            CreateCrystallized.MOD_ID,
            path
        );
    }

    public static final CTSpriteShiftEntry PROPULSITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/propulsite_block"),
        rl("block/propulsite_block_connected")
    );

    public static final CTSpriteShiftEntry OSCILLITE = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            rl("block/oscillite_block"),
            rl("block/oscillite_block_connected")
    );

    public static final CTSpriteShiftEntry DENSITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/densite_block"),
        rl("block/densite_block_connected")
    );

    public static final CTSpriteShiftEntry DENSITE_1 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/densite_block"),
        rl("block/densite_connected_activated/densite_block_connected_1")
    );

    public static final CTSpriteShiftEntry DENSITE_2 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/densite_block"),
        rl("block/densite_connected_activated/densite_block_connected_2")
    );

    public static final CTSpriteShiftEntry DENSITE_3 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/densite_block"),
        rl("block/densite_connected_activated/densite_block_connected_3")
    );

    public static final CTSpriteShiftEntry DENSITE_4 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/densite_block"),
        rl("block/densite_connected_activated/densite_block_connected_4")
    );

    public static final CTSpriteShiftEntry CHORA_CASING = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            rl("block/chora_casing"),
            rl("block/chora_casing_connected")
    );

    public static final CTSpriteShiftEntry DENSE_CHORA_CASING = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            rl("block/chora_casing_densite"),
            rl("block/chora_casing_densite_connected")
    );

    public static final CTSpriteShiftEntry PROPULSED_CHORA_CASING = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        rl("block/chora_casing_propulsite"),
        rl("block/chora_casing_propulsite_connected")
    );

    public static final CTSpriteShiftEntry OSCILLATING_CHORA_CASING = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            rl("block/chora_casing_oscillite"),
            rl("block/chora_casing_oscillite_connected")
    );

    public static final CTSpriteShiftEntry LEVITATING_CHORA_CASING = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            rl("block/chora_casing_levitite"),
            rl("block/chora_casing_levitite_connected")
    );

    public static void init() {
        CreateCrystallized.LOGGER.info("Connected textures initialized");
    }
}
