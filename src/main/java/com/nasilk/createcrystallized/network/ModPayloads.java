package com.nasilk.createcrystallized.network;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.network.custom.OscilliteCannonBeamPayload;
import com.nasilk.createcrystallized.network.custom.OscilliteCannonBeamPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = CreateCrystallized.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModPayloads {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CreateCrystallized.MOD_ID);

        registrar.playToClient(
            OscilliteCannonBeamPayload.TYPE,
            OscilliteCannonBeamPayload.STREAM_CODEC,
            OscilliteCannonBeamPayloadHandler::handleDataOnMain
        );
    }
}
