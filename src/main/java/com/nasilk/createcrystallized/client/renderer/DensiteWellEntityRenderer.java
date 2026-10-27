package com.nasilk.createcrystallized.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.CreateCrystallized;
import com.nasilk.createcrystallized.block.entity.DensiteWellEntity;
import com.nasilk.createcrystallized.client.model.DensiteWellCubeModel;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DensiteWellEntityRenderer implements BlockEntityRenderer<DensiteWellEntity> {
    private static final ResourceLocation TEXTURE_0 = CreateCrystallized.getRL("textures/block/densite_well_cube.png");
    private static final ResourceLocation TEXTURE_1 = CreateCrystallized.getRL("textures/block/densite_cube_activated/densite_well_cube_1.png");
    private static final ResourceLocation TEXTURE_2 = CreateCrystallized.getRL("textures/block/densite_cube_activated/densite_well_cube_2.png");
    private static final ResourceLocation TEXTURE_3 = CreateCrystallized.getRL("textures/block/densite_cube_activated/densite_well_cube_3.png");
    private static final ResourceLocation TEXTURE_4 = CreateCrystallized.getRL("textures/block/densite_cube_activated/densite_well_cube_4.png");
    private final DensiteWellCubeModel cube;

    public DensiteWellEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.cube = new DensiteWellCubeModel(context.bakeLayer(DensiteWellCubeModel.LAYER_LOCATION));
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
        Level level = densiteWellEntity.getLevel();
        if (level == null) return;
        long gameTime = level.getGameTime();
        float spinny = (gameTime + partialTick) * 1.5f;
        int power = densiteWellEntity.getPower();

        /// CUBE
        stack.pushPose(); //starts the chain
        stack.translate(0.5d, 0.5d, 0.5d); //moves cube to center
        stack.mulPose(Axis.XP.rotationDegrees(30)); //tilts the child
        stack.mulPose(Axis.YP.rotationDegrees(spinny * (power + 1 ))); //rotates the child, faster if powered
        stack.mulPose(Axis.ZP.rotationDegrees(spinny * 0.37f * (power + 1 ))); //also rotates the child, but with gusto and pizzazz

        cube.renderToBuffer( //unspeakable violence
            stack,
            bufferSource.getBuffer(cube.renderType(getTextureForPower(power))), //gets the current texture based on power
            packedLight, //takes the brightness from the existing block entity and passes it though
            packedOverlay, //mostly unneeded, but im afraid if it's not here everything will break and im not willing to test otherwise
            0xFFFFFFFF //color tinting
        );
        stack.popPose(); //ends the chain

        /// SPHERE
        if (isLocalPlayerWearingGoggles()) {
            double radius = densiteWellEntity.getFieldRadius();
            if (isWellDisabled(densiteWellEntity, radius)) return;
            long positionHash = densiteWellEntity.getBlockPos().asLong();
            SphereRendererHelper.renderSphere(partialTick, stack, bufferSource, gameTime, positionHash, radius);
        }
    }

    @Override
    public AABB getRenderBoundingBox(DensiteWellEntity densiteWellEntity) {
        double radius = densiteWellEntity.getFieldRadius();
        if (isWellDisabled(densiteWellEntity, radius)) return BlockEntityRenderer.super.getRenderBoundingBox(densiteWellEntity);
        return new AABB(densiteWellEntity.getBlockPos()).inflate(Math.max(0.0d, radius - 0.25d)); // 1x1 with 0.25 padding
    }

    @Override
    public boolean shouldRender(DensiteWellEntity blockEntity, Vec3 cameraPos) {
        double radius = blockEntity.getFieldRadius();
        if (isWellDisabled(blockEntity, radius)) return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, this.getViewDistance() + radius);

    }

    private static boolean isWellDisabled(DensiteWellEntity densiteWellEntity, double radius) {
        return !densiteWellEntity.isPowered() || !Double.isFinite(radius) || radius <= 0.0d;
    }

    private static boolean isLocalPlayerWearingGoggles() {
        Player player = Minecraft.getInstance().player;
        return player != null && GogglesItem.isWearingGoggles(player);
    }
}
