package com.nasilk.createcrystallized.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nasilk.createcrystallized.item.entity.AerolitePickaxeItemEntity;
import com.nasilk.createcrystallized.util.helper.AerolitePickaxeRendererHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector3d;

public class AerolitePickaxeItemEntityRenderer extends EntityRenderer<AerolitePickaxeItemEntity> {
    private final ItemRenderer itemRenderer;

    @Override
    public ResourceLocation getTextureLocation(AerolitePickaxeItemEntity aerolitePickaxeItemEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public AerolitePickaxeItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AerolitePickaxeItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        AerolitePickaxeRendererHelper.render(
            itemRenderer,
            entity.getPickupItemStackOrigin().copy(),
            new Vector3d(),
            AerolitePickaxeRendererHelper.getNormal(entity.getDeltaMovement()),
            0.0f,
            poseStack,
            buffer,
            entity.level(),
            packedLight,
            entity.getId()
        );
    }
}
