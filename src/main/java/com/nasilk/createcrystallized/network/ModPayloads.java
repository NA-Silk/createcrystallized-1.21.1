package com.nasilk.createcrystallized.network;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.network.custom.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CreateCrystallized.MOD_ID)
public class ModPayloads {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CreateCrystallized.MOD_ID);

        registrar.playToClient(
            OscilliteCannonBeamPayload.TYPE,
            OscilliteCannonBeamPayload.STREAM_CODEC,
            OscilliteCannonBeamPayloadHandler::handleDataOnMain
        );

        registrar.playToClient(
            DensiteBlockOverloadPayload.TYPE,
            DensiteBlockOverloadPayload.STREAM_CODEC,
            DensiteBlockOverloadPayloadHandler::handleDataOnMain
        );

        registrar.playToServer(
            SkyPaddlePayload.TYPE,
            SkyPaddlePayload.STREAM_CODEC,
            SkyPaddlePayloadHandler::handleDataOnMain
        );
    }
}
