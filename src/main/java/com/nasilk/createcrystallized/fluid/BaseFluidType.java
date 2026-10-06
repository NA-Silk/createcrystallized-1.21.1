package com.nasilk.createcrystallized.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BaseFluidType extends FluidType implements IClientFluidTypeExtensions {
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowTexture;
    private final ResourceLocation overlayTexture;
    private final Vector3f fogColor;
    private final int tintColor;

    public BaseFluidType(ResourceLocation stillTexture, ResourceLocation flowTexture, @Nullable ResourceLocation overlayTexture, @Nullable Integer tintColor, Vector3f fogColor, Properties properties) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowTexture = flowTexture;
        this.overlayTexture = overlayTexture;
        this.tintColor = tintColor == null ? 0xFFFFFFFF : tintColor; // No tint: 0xFFFFFFFF
        this.fogColor = fogColor;
    }

    public IClientFluidTypeExtensions getClientExtensions() {
        return new IClientFluidTypeExtensions() {
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return flowTexture;
            }

            @Override
            public @Nullable ResourceLocation getOverlayTexture() {
                return overlayTexture;
            }

            @Override
            public int getTintColor() {
                return tintColor;
            }

            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return fogColor;
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.0f);
                RenderSystem.setShaderFogEnd(8.0f);
            }
        };
    }
}
