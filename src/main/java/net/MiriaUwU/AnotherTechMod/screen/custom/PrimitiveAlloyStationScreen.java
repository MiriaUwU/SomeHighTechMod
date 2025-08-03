package net.MiriaUwU.AnotherTechMod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PrimitiveAlloyStationScreen extends AbstractContainerScreen<PrimitiveAlloyStationMenu> {

    private static final ResourceLocation GUI_TEXTURE =  ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/gui/primitive_alloy/primitive_alloy_station_gui.png");
    private static final ResourceLocation ARROW_TEXTURE =  ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/gui/arrow_progress.png");

    public PrimitiveAlloyStationScreen(PrimitiveAlloyStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isAlloying()) {
            int progressWidth = menu.getScaledArrowProgress();
            guiGraphics.blit(ARROW_TEXTURE, x + 90, y + 35, 0, 0, progressWidth, 16, 24, 16);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
