package com.nasilk.createcrystallized.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;

public final class SphereRendererHelper {
    // Vertices
    private static final int RING_SEGMENT_COUNT = 24;     // Each circle is approximated using 24 straight line segments
    private static final int LONGITUDE_RING_COUNT = 6; // Six vertical circles run down the sphere from pole to pole
    private static final int LATITUDE_RING_COUNT = 5;  // Five horizontal circles run between the top and bottom poles
    private static final int SPHERE_SEGMENT_COUNT = (LONGITUDE_RING_COUNT + LATITUDE_RING_COUNT) * RING_SEGMENT_COUNT;

    // Math
    private static final float PI = (float) Math.PI;
    private static final float TWO_PI = PI * 2.0f;

    // Color (RGB)
    private static final int BOUNDARY_RED = 82;
    private static final int BOUNDARY_GREEN = 34;
    private static final int BOUNDARY_BLUE = 112;

    // Pulse animation
    private static final float MIN_ALPHA = 0.025f;  // The baseline transparency
    private static final float PULSE_ALPHA = 0.38f; // Controls how much opacity the pulse adds at its brightest
    private static final float PULSE_SPEED = 0.08f; // Determines how quickly the animation cycles

    // Sphere line segments
    // 0–2  First endpoint: X, Y, Z
    // 3–5  Second endpoint: X, Y, Z
    // 6–8  Line direction
    private static final float[][] SPHERE_LINES = createSphereLines();

    private SphereRendererHelper() {}

    public static void renderSphere(float partialTick, PoseStack stack, MultiBufferSource bufferSource, long gameTime, long positionHash, double radius) {
        // Get current alpha
        float time = (gameTime + partialTick) * PULSE_SPEED; // Scale game time
        float phase = ((positionHash & 0xFFFFL) / 65535.0f) * TWO_PI; // Basically a UUID scaled by 2*PI
        float pulse = 0.5f + 0.5f * Mth.sin(time + phase); // Oscillate over [0, 1]
        float alpha = MIN_ALPHA + PULSE_ALPHA * pulse * pulse; // Oscillate over [MIN_ALPHA, MIN_ALPHA + PULSE_ALPHA]

        // Start drawing
        drawSphere(
            bufferSource.getBuffer(RenderType.lines()), // Lines buffer
            stack.last(), // Pose
            (float) radius, // Field radius
            Math.clamp(Math.round(alpha * 255.0f), 0, 255) // Scale alpha to byte
        );
    }

    private static void drawSphere(VertexConsumer lines, PoseStack.Pose pose, float radius, int alpha) {
        // Iterate over all line segments
        for (float[] line : SPHERE_LINES) {
            // First endpoint
            float x1 = 0.5f + line[0] * radius;
            float y1 = 0.5f + line[1] * radius;
            float z1 = 0.5f + line[2] * radius;

            // Second endpoint
            float x2 = 0.5f + line[3] * radius;
            float y2 = 0.5f + line[4] * radius;
            float z2 = 0.5f + line[5] * radius;

            // Shader direction
            float nx = line[6];
            float ny = line[7];
            float nz = line[8];

            // Draw current line
            lines.addVertex(pose, x1, y1, z1)
                .setColor(BOUNDARY_RED, BOUNDARY_GREEN, BOUNDARY_BLUE, alpha)
                .setNormal(pose, nx, ny, nz);
            lines.addVertex(pose, x2, y2, z2)
                .setColor(BOUNDARY_RED, BOUNDARY_GREEN, BOUNDARY_BLUE, alpha)
                .setNormal(pose, nx, ny, nz);
        }
    }

    private static float[][] createSphereLines() {
        // Initialize lines list
        List<float[]> lines = new ArrayList<>(SPHERE_SEGMENT_COUNT);

        // Compute longitude rings
        for (int r = 0; r < LONGITUDE_RING_COUNT; r++) {
            float longitude = PI * r / LONGITUDE_RING_COUNT; // Current circle angle intersecting the circumference [0, PI)
            float cosR = Mth.cos(longitude); // x unit
            float sinR = Mth.sin(longitude); // z unit

            // Compute longitude segments (angles measured from +y to -y)
            for (int i = 0; i < RING_SEGMENT_COUNT; i++) {
                float angle0 = TWO_PI * i / RING_SEGMENT_COUNT; // First endpoint angle [0, 2*PI)
                float cos0 = Mth.cos(angle0); // y unit
                float sin0 = Mth.sin(angle0); // xz component (radius)
                float angle1 = TWO_PI * (i + 1) / RING_SEGMENT_COUNT; // Second endpoint angle (0, 2*PI]
                float cos1 = Mth.cos(angle1); // y unit
                float sin1 = Mth.sin(angle1); // xz component (radius)
                addLine(lines,
                    sin0 * cosR, cos0, sin0 * sinR,
                    sin1 * cosR, cos1, sin1 * sinR
                );
            }
        }

        // Compute latitude rings
        for (int r = 0; r < LATITUDE_RING_COUNT; r++) {
            float latitude = PI * (r + 1) / (LATITUDE_RING_COUNT + 1); // Current scaled circle parallel to the circumference (+ 1 to avoid poles)
                  latitude -= PI / 2.0f; // Shift to (-PI/2, PI/2)
            float cosR = Mth.cos(latitude); // xz component (radius)
            float sinR = Mth.sin(latitude); // y unit

            // Compute latitude segments (angles measured from )
            for (int i = 0; i < RING_SEGMENT_COUNT; i++) {
                float angle0 = TWO_PI * i / RING_SEGMENT_COUNT; // First endpoint angle [0, 2*PI)
                float cos0 = Mth.cos(angle0); // x unit
                float sin0 = Mth.sin(angle0); // z unit
                float angle1 = TWO_PI * (i + 1) / RING_SEGMENT_COUNT; // Second endpoint angle (0, 2*PI]
                float cos1 = Mth.cos(angle1); // x unit
                float sin1 = Mth.sin(angle1); // z unit
                addLine(lines,
                    cos0 * cosR, sinR, sin0 * cosR,
                    cos1 * cosR, sinR, sin1 * cosR
                );
            }
        }

        return lines.toArray(float[][]::new);
    }

    private static void addLine(List<float[]> lines, float x1, float y1, float z1, float x2, float y2, float z2) {
        // Get point deltas
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;

        // Get endpoint distance
        float d = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (d < 1.0e-8f) return; // Don't divide by zero...

        // Get direction units
        float x = dx / d;
        float y = dy / d;
        float z = dz / d;

        // Store result
        lines.add(new float[]{x1, y1, z1, x2, y2, z2, x, y, z});
    }
}
