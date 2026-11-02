package com.nasilk.createcrystallized.network.handler;

import com.nasilk.createcrystallized.common.ModParticleTypes;
import com.nasilk.createcrystallized.network.payload.OscilliteCannonBeamPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class OscilliteCannonBeamPayloadHandler {
    public static void handleDataOnMain(final OscilliteCannonBeamPayload data, final IPayloadContext context) {
        // Push code to the main client thread
        context.enqueueWork(() -> {
            // Validate level
            if (!(Minecraft.getInstance().level instanceof ClientLevel clientLevel)) return;

            // Validate data
            double range = data.range();
            Vector3f face = data.face();
            Vector3f direction = data.direction();
            if (!Double.isFinite(range)      || range < 0.0d                 || range > 255.0d               ||
                !Float.isFinite(face.x)      || !Float.isFinite(face.y)      || !Float.isFinite(face.z)      ||
                !Float.isFinite(direction.x) || !Float.isFinite(direction.y) || !Float.isFinite(direction.z)
            ) return;

            // Fire particles straight outward
            Vector3d beamPosition = new Vector3d();
            for (double i = 0.0d; i <= range; i += 0.5d) {
                beamPosition.set(face).fma(i, direction);
                clientLevel.addParticle(
                    ModParticleTypes.OSCILLITE_CANNON_FIRING_PARTICLES.get(),
                    beamPosition.x, beamPosition.y, beamPosition.z,
                    0.0d, 0.0d, 0.0d
                );
            }
        });
    }
}
