package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.custom.DensiteBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.ArrayList;

public record DensiteBlockOverloadPayload(long pos, long[] boundarySamples, int sampleCount) implements CustomPacketPayload {
    public static final Type<DensiteBlockOverloadPayload> TYPE = new Type<>(CreateCrystallized.getRL("densite_block_overload"));

    private static final StreamCodec<ByteBuf, long[]> LONG_ARRAY = ByteBufCodecs.collection(
        ArrayList::new,
        ByteBufCodecs.VAR_LONG,
        DensiteBlock.MAX_BOUNDARY_SAMPLES
    ).map(
        // Decode: List<Long> -> long
        list -> list.stream().mapToLong(Long::longValue).toArray(),

        // Encode: long[] -> List<Long>
        array -> {
            ArrayList<Long> list = new ArrayList<>(array.length);
            for (long longValue : array) list.add(longValue);
            return list;
        }
    );

    public static final StreamCodec<FriendlyByteBuf, DensiteBlockOverloadPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, DensiteBlockOverloadPayload::pos,
        LONG_ARRAY, DensiteBlockOverloadPayload::boundarySamples,
        ByteBufCodecs.INT, DensiteBlockOverloadPayload::sampleCount,
        DensiteBlockOverloadPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
