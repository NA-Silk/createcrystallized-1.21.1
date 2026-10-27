package com.nasilk.createcrystallized.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nasilk.createcrystallized.common.ModCreativeModeTabs;
import dev.simulated_team.simulated.client.sections.SimulatedSection;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.mixin.accessor.CreativeModeInventoryScreenAccessor;
import dev.simulated_team.simulated.registrate.simulated_tab.SimulatedCreativeTab;
import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.color.Colorc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ModCreativeTabRenderer {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 18;

    public static void render(CreativeModeInventoryScreen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        // Validate section
        SimulatedSection section = SimResourceManagers.SIMULATED_SECTION.get(ModCreativeModeTabs.CRYSTALLIZED_SECTION);
        if (section == null) return;

        // Validate scroll position
        int row = -SimulatedCreativeTab.CURRENT_ROW;
        if (row < 0 || row > 4) return;

        // Start stack
        PoseStack stack = graphics.pose();
        stack.pushPose();

        // Setup render system
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Get bounds
        CreativeModeInventoryScreenAccessor screenAccessor = (CreativeModeInventoryScreenAccessor) screen;
        int left = screenAccessor.getLeftPos() + 8;
        int top = screenAccessor.getTopPos() + 17;
        stack.translate(left, top, 0);

        // Begin rendering
        renderSection(section, graphics, mouseX, mouseY, left, top, row * HEIGHT);

        // End stack and render system
        stack.popPose();
        RenderSystem.disableDepthTest();
    }

    private static void renderSection(SimulatedSection section, GuiGraphics graphics, int mouseX, int mouseY, int left, int top, int rowY) {
        // Set animation texture
        ResourceLocation bannerTexture = section.sprite();
        if (section.animateOnHover()) SimulatedCreativeTab.setPlaying(
            bannerTexture,
            mouseX >= left && mouseX <= left + WIDTH &&
            mouseY >= top + rowY && mouseY <= top + rowY + HEIGHT
        );
        graphics.blitSprite(bannerTexture, 0, rowY, WIDTH, HEIGHT);

        // Get text and color
        SimulatedSection.Title title = section.title();
        Component text = title.text();
        Colorc background = title.background();
        Colorc light = title.color();
        Colorc dark = title.secondaryColor().orElse(light.darken(0.2f, new Color()));

        // Handle text
        graphics.fill(2, rowY + 2, Minecraft.getInstance().font.width(text) + 8, rowY + HEIGHT - 2, background.argb());
        SimulatedCreativeTab.drawAuraText(graphics, text, dark.argb(), light.argb(), 5, rowY + 5);
    }
}
