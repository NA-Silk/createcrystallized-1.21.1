package com.nasilk.createcrystallized.network.payload;

import com.nasilk.createcrystallized.CreateCrystallized;
import dev.ryanhcode.sable.util.SableBufferUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3d;

public record SubLevelAirPunchPayload(Vector3d hitDirection) implements CustomPacketPayload {
    public static final Type<SubLevelAirPunchPayload> TYPE = new Type<>(CreateCrystallized.getRL("sublevel_air_punch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubLevelAirPunchPayload> STREAM_CODEC = StreamCodec.of(
        (buf, value) ->
            value.write(buf),
            SubLevelAirPunchPayload::read
    );

    private static SubLevelAirPunchPayload read(final FriendlyByteBuf buf) {
        return new SubLevelAirPunchPayload(
            SableBufferUtils.read(buf, new Vector3d())
        );
    }

    private void write(final FriendlyByteBuf buf) {
        SableBufferUtils.write(buf, this.hitDirection);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
