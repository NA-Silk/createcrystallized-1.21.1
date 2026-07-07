package com.nasilk.createcrystallized.util;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(Registries.SOUND_EVENT, CreateCrystallized.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PEBBLE_PLACE = SOUND_EVENTS.register(
        "block.pebble_place",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    CreateCrystallized.MOD_ID, "block.pebble_place"
            )
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PROPULSITE_BREAK = SOUND_EVENTS.register(
            "block.propulsite_break",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    CreateCrystallized.MOD_ID, "block.propulsite_break"
            )
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PROPULSITE_PLACE = SOUND_EVENTS.register(
            "block.propulsite_place",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    CreateCrystallized.MOD_ID, "block.propulsite_place"
            )
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> DENSITE_BREAK = SOUND_EVENTS.register(
            "block.densite_break",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    CreateCrystallized.MOD_ID, "block.densite_break"
            )
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> DENSITE_PLACE = SOUND_EVENTS.register(
            "block.densite_place",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    CreateCrystallized.MOD_ID, "block.densite_place"
            )
        )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> OSCILLITE_PLACE = SOUND_EVENTS.register(
            "block.oscillite_place",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                            CreateCrystallized.MOD_ID, "block.oscillite_place"
                    )
            )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> OSCILLITE_BREAK = SOUND_EVENTS.register(
            "block.oscillite_break",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                            CreateCrystallized.MOD_ID, "block.oscillite_break"
                    )
            )
    );

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
