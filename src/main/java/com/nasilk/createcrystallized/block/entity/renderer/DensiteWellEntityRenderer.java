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

public class DensiteWellEntityRenderer implements BlockEntityRenderer<DensiteWellEntity> {
    public static final ResourceLocation TEXTURE_0 = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_well_cube.png");
    public static final ResourceLocation TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_cube_activated/densite_well_cube_1.png");
    public static final ResourceLocation TEXTURE_2 = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_cube_activated/densite_well_cube_2.png");
    public static final ResourceLocation TEXTURE_3 = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_cube_activated/densite_well_cube_3.png"); //TODO iterate on these textures a bit, center is messy
    public static final ResourceLocation TEXTURE_4 = ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "textures/block/densite_cube_activated/densite_well_cube_4.png"); //TODO iterate on these textures a bit, center is messy
    private final DensiteWellCubeModel cube;

    public DensiteWellEntityRenderer(BlockEntityRendererProvider.Context context) {
        cube = new DensiteWellCubeModel(context.bakeLayer(DensiteWellCubeModel.LAYER_LOCATION));
    }

    private ResourceLocation getTextureForPower(int power) { //texture change based on power
        if (power == 0) return TEXTURE_0;
        if (power <= 4) return TEXTURE_1;
        if (power <= 8) return TEXTURE_2;
        if (power <= 12) return TEXTURE_3;
        return TEXTURE_4;
    }

    @Override
    public void render(DensiteWellEntity densiteWellEntity, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (densiteWellEntity.getLevel() == null) return;
        float spinny = (densiteWellEntity.getLevel().getGameTime() + partialTick) * 1.5f;
        int power = densiteWellEntity.getLevel().getBestNeighborSignal(densiteWellEntity.getBlockPos());

        // CUBE
        stack.pushPose(); //starts the chain
        stack.translate(0.5, 0.5, 0.5); //moves cube to center
        stack.mulPose(Axis.XP.rotationDegrees(30)); //tilts the child
        stack.mulPose(Axis.YP.rotationDegrees(spinny * (power + 1 ))); //rotates the child, faster if powered
        stack.mulPose(Axis.ZP.rotationDegrees(spinny * 0.37F * (power + 1 ))); //also rotates the child, but with gusto and pizzaz

        cube.renderToBuffer( //unspeakable violence
            stack,
            bufferSource.getBuffer(cube.renderType(getTextureForPower(power))), //gets the current texture based on power
            packedLight, //takes the brightness from the existing block entity and passes it though
            packedOverlay, //mostly unneeded, but im afraid if it's not here everything will break and im not willing to test otherwise
            0xFFFFFFFF //color tinting
        );
        stack.popPose(); //ends the chain
    }
}
