package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.CreateCrystallized;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SkyPaddlePayload() implements CustomPacketPayload {
    public static final Type<SkyPaddlePayload> TYPE = new Type<>(CreateCrystallized.getRL("sky_paddle"));

    public static final StreamCodec<ByteBuf, SkyPaddlePayload> STREAM_CODEC = StreamCodec.unit(new SkyPaddlePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
