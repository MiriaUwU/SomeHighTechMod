package net.MiriaUwU.AnotherTechMod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.client.FluidLayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PrimitiveAlloyStationScreen extends AbstractContainerScreen<PrimitiveAlloyStationMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/gui/primitive_alloy/primitive_alloy_station_gui.png");

    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/gui/arrow_progress.png");

    // Tank constants - for the LAYERED tank display
    private static final int TANK_WIDTH = 52;
    private static final int TANK_HEIGHT = 78;
    private static final int TANK_X = 119; // Single tank position
    private static final int TANK_Y = 4;

    public PrimitiveAlloyStationScreen(PrimitiveAlloyStationMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Calculate GUI origin on screen
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Draw main background
        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Draw arrow progress
        if (menu.isAlloying()) {
            int arrowProgress = menu.getScaledArrowProgress();
            guiGraphics.blit(ARROW_TEXTURE, x + 88, y + 35, 0, 0, arrowProgress + 1, 16);
        }

        // Draw LAYERED fluid tank (single tank with all fluids stacked)
        renderLayeredFluidTank(guiGraphics, x + TANK_X, y + TANK_Y, TANK_WIDTH, TANK_HEIGHT);
    }

    /**
     * Renders multiple fluid layers stacked on top of each other (Tinkers' style)
     */
    private void renderLayeredFluidTank(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        List<FluidLayer> layers = menu.getFluidLayers();

        if (layers.isEmpty()) {
            return;
        }

        int totalCapacity = menu.getTotalCapacity();
        int currentY = y + height; // Start from bottom

        // Render each layer from bottom to top
        for (FluidLayer layer : layers) {
            if (layer.isEmpty()) {
                continue;
            }

            FluidStack fluid = layer.getFluidStack();

            // Calculate this layer's height based on its proportion of total capacity
            int layerHeight = (int) (height * ((float) layer.getAmount() / totalCapacity));

            if (layerHeight <= 0) {
                continue;
            }

            // Move up by this layer's height
            currentY -= layerHeight;

            // Get fluid rendering info
            IClientFluidTypeExtensions fluidType = IClientFluidTypeExtensions.of(fluid.getFluid());
            ResourceLocation stillTexture = fluidType.getStillTexture(fluid);
            int color = fluidType.getTintColor(fluid);

            // Apply color tint
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            float a = ((color >> 24) & 0xFF) / 255.0F;

            RenderSystem.setShaderColor(r, g, b, a);
            RenderSystem.enableBlend();

            // Draw this fluid layer
            drawTiledFluid(guiGraphics, stillTexture, x, currentY, width, layerHeight);

            // Reset color
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }

    /**
     * Helper method to draw tiled fluid textures
     */
    private void drawTiledFluid(GuiGraphics guiGraphics, ResourceLocation texture,
                                int x, int y, int width, int height) {
        int textureSize = 16;

        // Get the sprite from the block atlas
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(texture);

        // Tile vertically
        for (int yOffset = 0; yOffset < height; yOffset += textureSize) {
            int remainingHeight = Math.min(textureSize, height - yOffset);

            // Tile horizontally
            for (int xOffset = 0; xOffset < width; xOffset += textureSize) {
                int remainingWidth = Math.min(textureSize, width - xOffset);

                guiGraphics.blit(
                        x + xOffset,
                        y + yOffset,
                        0, // blitOffset/z
                        remainingWidth,
                        remainingHeight,
                        sprite
                );
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render layered tank tooltip
        renderLayeredTankTooltip(guiGraphics, mouseX, mouseY,
                x + TANK_X, y + TANK_Y, TANK_WIDTH, TANK_HEIGHT);
    }

    /**
     * Renders tooltip for the layered tank showing all fluids
     */
    private void renderLayeredTankTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                          int screenX, int screenY, int width, int height) {
        // Check if mouse is within the tank bounds (screen coordinates)
        if (mouseX < screenX || mouseX >= screenX + width ||
                mouseY < screenY || mouseY >= screenY + height) {
            return;
        }

        List<FluidLayer> layers = menu.getFluidLayers();
        if (layers.isEmpty()) {
            guiGraphics.renderTooltip(font,
                    List.of(Component.literal("Empty").withStyle(ChatFormatting.GRAY)),
                    Optional.empty(), mouseX, mouseY);
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Smeltery Tank").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.empty());

        // Add each fluid layer to tooltip
        for (FluidLayer layer : layers) {
            if (layer.isEmpty()) continue;

            FluidStack fluid = layer.getFluidStack();
            int amount = layer.getAmount();

            tooltip.add(fluid.getDisplayName());
            tooltip.add(Component.literal("  " + amount + " mB")
                    .withStyle(ChatFormatting.GRAY));

            int ingots = amount / 144;
            int mbRemainder = amount % 144;

            if (ingots > 0 || mbRemainder > 0) {
                MutableComponent prettyAmount = Component.literal("  ");

                if (ingots > 0) {
                    prettyAmount.append(Component.literal(ingots + " ")
                            .withStyle(ChatFormatting.GOLD));
                    prettyAmount.append(Component.literal("Ingots ")
                            .withStyle(ChatFormatting.GRAY));
                }

                if (mbRemainder > 0) {
                    prettyAmount.append(Component.literal(mbRemainder + " ")
                            .withStyle(ChatFormatting.GOLD));
                    prettyAmount.append(Component.literal("mB")
                            .withStyle(ChatFormatting.GRAY));
                }

                tooltip.add(prettyAmount);
            }

            tooltip.add(Component.empty());
        }

        int totalAmount = layers.stream().mapToInt(FluidLayer::getAmount).sum();
        tooltip.add(Component.literal("Total: " + totalAmount + " / " + menu.getTotalCapacity() + " mB")
                .withStyle(ChatFormatting.DARK_GRAY));

        guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        // Removed old tank-specific labels since we're using the dynamic tooltip now
    }
}