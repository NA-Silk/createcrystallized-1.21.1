package com.nasilk.createcrystallized.network.custom;

import com.nasilk.createcrystallized.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;

public class OscilliteCannonBeamPayloadHandler {
    public static void handleDataOnMain(final OscilliteCannonBeamPayload data, final IPayloadContext context) {
        // Push code to the main client thread
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level instanceof ClientLevel clientLevel) {
                // Fire particles straight outward
                Vector3d beamPosition = new Vector3d();
                for (double i = 0; i <= data.range(); i+=0.5) {
                    beamPosition.set(data.face()).fma(i, data.direction());
                    clientLevel.addParticle(
                        ModParticles.OSCILLITE_CANNON_FIRING_PARTICLES.get(),
                        beamPosition.x, beamPosition.y, beamPosition.z,
                        0.0, 0.0, 0.0
                    );
                }
            }
        });
    }
}
