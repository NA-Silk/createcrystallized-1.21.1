package com.nasilk.createcrystallized.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.client.models.DensiteWellCubeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class DensiteWellEntityRenderer implements BlockEntityRenderer<DensiteWellEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_well.png");

    private final DensiteWellCubeModel cube;

    public DensiteWellEntityRenderer(BlockEntityRendererProvider.Context context) {

        cube = new DensiteWellCubeModel(context.bakeLayer(DensiteWellCubeModel.LAYER_LOCATION));
    }

    @Override
    public void render(DensiteWellEntity densiteWellEntity, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (densiteWellEntity.getLevel() == null) return;

        stack.pushPose();

        stack.translate(0.5, 0.0, 0.5);

        float spinny = (densiteWellEntity.getLevel().getGameTime() + partialTick) * 2; //dont un-declare this one i might need s p i n later
        stack.mulPose(Axis.YP.rotationDegrees(spinny));

        VertexConsumer consumer = bufferSource.getBuffer(cube.renderType(TEXTURE));

        cube.renderToBuffer(
                stack,
                consumer,
                packedLight,
                packedOverlay,
                0xFFFFFFFF);

        stack.popPose();

    }
}
