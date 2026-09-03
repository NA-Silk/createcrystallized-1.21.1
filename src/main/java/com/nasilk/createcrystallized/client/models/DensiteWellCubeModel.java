package com.nasilk.createcrystallized.client.models; // Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nasilk.createcrystallized.CreateCrystallized;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class DensiteWellCubeModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CreateCrystallized.MOD_ID, "densite_well_cube"), "main");
	private final ModelPart bb_main;

	public DensiteWellCubeModel(ModelPart root) {
		super(RenderType::entitySolid);
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild(
			"bb_main",
			CubeListBuilder
				.create()
				.texOffs(0, 0)
				.addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)),
			PartPose.offset(0.0f, 0.0f, 0.0f)
		);
		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		this.bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
