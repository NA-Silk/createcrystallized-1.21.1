package com.nasilk.createcrystallized.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nasilk.createcrystallized.item.ModItems;
import com.nasilk.createcrystallized.item.entity.AerolitePickaxeItemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.ItemRenderer;

    // TODO
public class AerolitePickaxeRenderer extends EntityRenderer<AerolitePickaxeItemEntity> {
    private final ItemRenderer itemRenderer;

    @Override
    public ResourceLocation getTextureLocation(AerolitePickaxeItemEntity aerolitePickaxeItemEntity) {return null;
    }

    public AerolitePickaxeRenderer(EntityRendererProvider.Context context) {super(context); this.itemRenderer = context.getItemRenderer();
    }

        @Override
        public void render(AerolitePickaxeItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

            poseStack.pushPose();

            poseStack.scale(2.0f, 2.0f, 2.0f);

            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

            itemRenderer.renderStatic(
                ModItems.AEROLITE_PICKAXE.get().getDefaultInstance(),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
             );

            poseStack.popPose();
    }
}
