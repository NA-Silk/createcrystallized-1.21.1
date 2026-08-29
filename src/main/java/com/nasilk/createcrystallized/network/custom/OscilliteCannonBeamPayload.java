package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record OscilliteCannonBeamPayload(Vector3f face, Vector3f direction, double range) implements CustomPacketPayload {
    public static final Type<OscilliteCannonBeamPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "oscillite_cannon_beam"));

    public static final StreamCodec<FriendlyByteBuf, OscilliteCannonBeamPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VECTOR3F, OscilliteCannonBeamPayload::face,
        ByteBufCodecs.VECTOR3F, OscilliteCannonBeamPayload::direction,
        ByteBufCodecs.DOUBLE,   OscilliteCannonBeamPayload::range,
        OscilliteCannonBeamPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
