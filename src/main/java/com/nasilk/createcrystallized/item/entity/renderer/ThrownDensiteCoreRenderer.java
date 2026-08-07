package com.nasilk.createcrystallized.item.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.client.models.DensiteWellCubeModel;
import com.nasilk.createcrystallized.item.entity.ThrownDensiteCoreEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ThrownDensiteCoreRenderer extends EntityRenderer<ThrownDensiteCoreEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_well_cube.png");

    private final DensiteWellCubeModel model;

    public ThrownDensiteCoreRenderer(EntityRendererProvider.Context context) {super(context);this.model = new DensiteWellCubeModel(context.bakeLayer(DensiteWellCubeModel.LAYER_LOCATION));
    }

    @Override
    public void render(ThrownDensiteCoreEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, 0.15D, 0.0D); //uhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh  he go

        //all temp mulPoses
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getViewYRot(partialTicks) - 90.0F)); //spin
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getViewXRot(partialTicks))); // s p i n

        poseStack.mulPose(Axis.XP.rotationDegrees(entity.tickCount + partialTicks * 20.0F)); //s  p  i  n

        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(getTextureLocation(entity)));

        this.model.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownDensiteCoreEntity entity) {return TEXTURE;
    }
}