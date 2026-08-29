package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.CreateCrystallized;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SkyPaddlePayload() implements CustomPacketPayload {
    public static final Type<SkyPaddlePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "sky_paddle"));

    public static final StreamCodec<ByteBuf, SkyPaddlePayload> STREAM_CODEC = StreamCodec.unit(new SkyPaddlePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
