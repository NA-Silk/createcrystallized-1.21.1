package com.nasilk.createcrystallized.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.client.models.DensiteWellCubeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

//ALL WIP, DO NOT CLEAN CODE

public class DensiteWellEntityRenderer implements BlockEntityRenderer<DensiteWellEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_well_cube.png");

    private final DensiteWellCubeModel cube;

    public DensiteWellEntityRenderer(BlockEntityRendererProvider.Context context) {

        cube = new DensiteWellCubeModel(context.bakeLayer(DensiteWellCubeModel.LAYER_LOCATION));
    }

    @Override
    public void render(DensiteWellEntity densiteWellEntity, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (densiteWellEntity.getLevel() == null) return;
        float spinny = (densiteWellEntity.getLevel().getGameTime() + partialTick) * 1.5f;
        int power = densiteWellEntity.getLevel().getBestNeighborSignal(densiteWellEntity.getBlockPos());

        //CUBE
        stack.pushPose(); //starts the chain

        stack.translate(0.5, 0.5, 0.5); //moves cube to center

        stack.mulPose(Axis.XP.rotationDegrees(30)); //tilts the child
        stack.mulPose(Axis.YP.rotationDegrees(spinny * (power + 1 ))); //rotates the child, faster if powered
        stack.mulPose(Axis.ZP.rotationDegrees(spinny * 0.37F * (power + 1 ))); //also rotates the child, but with gusto and pizzaz


        cube.renderToBuffer( //unspeakable violence
                stack,
                bufferSource.getBuffer(cube.renderType(TEXTURE)),
                packedLight, //takes the brightness from the existing block entity and passes it though
                packedOverlay, //mostly unneeded, but im afraid if it's not here everything will break and im not willing to test otherwise
                0xFFFFFFFF); //color tinting

        stack.popPose(); //ends the chain
    }
}
