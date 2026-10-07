package com.nasilk.createcrystallized.client;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;

public class ModSpriteShifts {
    public static final CTSpriteShiftEntry PROPULSITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/propulsite_block"),
        CreateCrystallized.getRL("block/propulsite_block_connected")
    );

    public static final CTSpriteShiftEntry OSCILLITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/oscillite_block"),
        CreateCrystallized.getRL("block/oscillite_block_connected")
    );

    public static final CTSpriteShiftEntry DENSITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/densite_block"),
        CreateCrystallized.getRL("block/densite_block_connected")
    );

    public static final CTSpriteShiftEntry DENSITE_1 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/densite_block"),
        CreateCrystallized.getRL("block/densite_connected_activated/densite_block_connected_1")
    );

    public static final CTSpriteShiftEntry DENSITE_2 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/densite_block"),
        CreateCrystallized.getRL("block/densite_connected_activated/densite_block_connected_2")
    );

    public static final CTSpriteShiftEntry DENSITE_3 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/densite_block"),
        CreateCrystallized.getRL("block/densite_connected_activated/densite_block_connected_3")
    );

    public static final CTSpriteShiftEntry DENSITE_4 = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/densite_block"),
        CreateCrystallized.getRL("block/densite_connected_activated/densite_block_connected_4")
    );

    public static final CTSpriteShiftEntry ENCASED_PROPULSITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/encased_propulsite_block"),
        CreateCrystallized.getRL("block/encased_propulsite_block_connected")
    );

    public static final CTSpriteShiftEntry ENCASED_OSCILLITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/encased_oscillite_block"),
        CreateCrystallized.getRL("block/encased_oscillite_block_connected")
    );

    public static final CTSpriteShiftEntry ENCASED_DENSITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/encased_densite_block"),
        CreateCrystallized.getRL("block/encased_densite_block_connected")
    );

    public static final CTSpriteShiftEntry ENCASED_LEVITITE = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/encased_levitite_block"),
        CreateCrystallized.getRL("block/encased_levitite_block_connected")
    );

    public static final CTSpriteShiftEntry CHORA_CASING = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/chora_casing"),
        CreateCrystallized.getRL("block/chora_casing_connected")
    );

    public static final CTSpriteShiftEntry DENSE_CHORA_CASING = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/chora_casing_densite"),
        CreateCrystallized.getRL("block/chora_casing_densite_connected")
    );

    public static final CTSpriteShiftEntry PROPULSED_CHORA_CASING = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/chora_casing_propulsite"),
        CreateCrystallized.getRL("block/chora_casing_propulsite_connected")
    );

    public static final CTSpriteShiftEntry OSCILLATING_CHORA_CASING = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/chora_casing_oscillite"),
        CreateCrystallized.getRL("block/chora_casing_oscillite_connected")
    );

    public static final CTSpriteShiftEntry LEVITATING_CHORA_CASING = CTSpriteShifter.getCT(
        AllCTTypes.OMNIDIRECTIONAL,
        CreateCrystallized.getRL("block/chora_casing_levitite"),
        CreateCrystallized.getRL("block/chora_casing_levitite_connected")
    );

    public static void register() {
        CreateCrystallized.LOGGER.info("Sprite Shifts registered");
    }
}
