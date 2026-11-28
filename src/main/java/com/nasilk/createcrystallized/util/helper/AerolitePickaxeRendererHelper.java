package com.nasilk.createcrystallized.util.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

public final class AerolitePickaxeRendererHelper {
    public static Vector3d getNormal(Vec3 velocity) {
        Vector3d normal = JOMLConversion.toJOML(velocity);
        if (normal.lengthSquared() > 1e-7d) return normal.normalize().negate();
        return normal.set(0.0d, 0.0d, 1.0d);
    }

    public static Vector3d getNormal(Vector3d velocity) {
        Vector3d normal = new Vector3d(velocity);
        if (normal.lengthSquared() > 1e-7d) return normal.normalize().negate();
        return normal.set(0.0d, 0.0d, 1.0d);
    }

    public static void render (
        ItemRenderer itemRenderer,
        ItemStack itemStack,
        Vector3dc position,
        Vector3dc normal,
        float roll,
        PoseStack poseStack,
        MultiBufferSource buffer,
        Level level,
        int packedLight,
        int seed
    ) {
        // Validate stack and level
        if (itemStack.isEmpty() || level == null) return;

        // Get normal (front) direction
        Vector3f normalF = new Vector3f((float) normal.x(), (float) normal.y(), (float) normal.z());
        if (normal.lengthSquared() < 1e-7d) normalF.set(0.0f, 0.0f, 1.0f);
        normalF.normalize();

        // Apply rotation
        Quaternionf rotation = new Quaternionf();
        rotation.rotationTo(0.0f, 0.0f, 1.0f, normalF.x, normalF.y, normalF.z);
        rotation.rotateZ((float) Math.toRadians(roll));

        // Handle pose and render
        poseStack.pushPose();
        poseStack.translate(position.x(), position.y(), position.z());
        poseStack.mulPose(rotation);
        itemRenderer.renderStatic(
            itemStack,
            ItemDisplayContext.FIXED,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            level,
            seed
        );
        poseStack.popPose();
    }
}
