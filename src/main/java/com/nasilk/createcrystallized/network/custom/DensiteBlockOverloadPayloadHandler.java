package com.nasilk.createcrystallized.network.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public class DensiteBlockOverloadPayloadHandler {
    private static final DustParticleOptions OVERFLOW_DUST = new DustParticleOptions(new Vector3f(0.20f, 0.0f, 0.310f),1.0f);

    public static void handleDataOnMain(final DensiteBlockOverloadPayload data, final IPayloadContext context) {
        // Push code to the main client thread
        context.enqueueWork(() -> {
            // Validate level
            if (!(Minecraft.getInstance().level instanceof ClientLevel clientLevel)) return;

            // Spawn 2 particles on boundaries
            long[] boundarySamples = data.boundarySamples();
            int sampleCount = data.sampleCount();
            for (int i = 0; i < sampleCount; i++) {
                BlockPos currentPos = BlockPos.of(boundarySamples[i]);
                for (int j = 0; j < 2; j++) clientLevel.addParticle(
                    OVERFLOW_DUST,
                    currentPos.getX() + 0.5d, currentPos.getY() + 0.5d, currentPos.getZ() + 0.5d,
                    0.2,0.2d,0.2d
                );
            }

            // Spawn 5 particles on current position too
            BlockPos pos = BlockPos.of(data.pos());
            for (int j = 0; j < 5; j++) clientLevel.addParticle(
                OVERFLOW_DUST,
                pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
                0.2,0.2d,0.2d
            );
            clientLevel.playLocalSound(
                pos,
                SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
                1.0f, 0.5f, true
            );
        });
    }
}
