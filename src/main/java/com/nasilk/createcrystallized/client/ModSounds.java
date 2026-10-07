package com.nasilk.createcrystallized.client;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final CreateRegistrate REGISTRATE = CreateCrystallized.getRegistrate();

    public static final RegistryEntry<SoundEvent, SoundEvent> PEBBLE_PLACE = registerSound(
        "block.pebble_place"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> PROPULSITE_BREAK = registerSound(
        "block.propulsite_break"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> PROPULSITE_PLACE = registerSound(
        "block.propulsite_place"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> PROPULSITE_THRUSTER_FIRE = registerSound(
        "block.propulsite_thruster_fire"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> DENSITE_BREAK = registerSound(
        "block.densite_break"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> DENSITE_PLACE = registerSound(
        "block.densite_place"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> OSCILLITE_PLACE = registerSound(
        "block.oscillite_place"
    );

    public static final RegistryEntry<SoundEvent, SoundEvent> OSCILLITE_BREAK = registerSound(
        "block.oscillite_break"
    );

    private static RegistryEntry<SoundEvent, SoundEvent> registerSound(String name) {
        return REGISTRATE.simple(
            name,
            Registries.SOUND_EVENT,
            () -> SoundEvent.createVariableRangeEvent(CreateCrystallized.getRL(name))
        );
    }

    public static void register() {
        CreateCrystallized.LOGGER.info("Sounds registered");
    }
}
