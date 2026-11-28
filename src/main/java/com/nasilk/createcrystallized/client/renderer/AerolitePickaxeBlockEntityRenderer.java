package com.nasilk.createcrystallized.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nasilk.createcrystallized.block.entity.AerolitePickaxeBlockEntity;
import com.nasilk.createcrystallized.util.helper.AerolitePickaxeRendererHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class AerolitePickaxeBlockEntityRenderer implements BlockEntityRenderer<AerolitePickaxeBlockEntity> {
    private final ItemRenderer itemRenderer;

    public AerolitePickaxeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AerolitePickaxeBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AerolitePickaxeRendererHelper.render(
            itemRenderer,
            entity.getStack(),
            entity.getRelPosition(),
            entity.getNormal(),
            entity.getRoll(),
            poseStack,
            buffer,
            entity.getLevel(),
            packedLight,
            entity.hashCode()
         );
    }
}
