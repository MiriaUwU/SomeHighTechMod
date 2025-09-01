package net.MiriaUwU.AnotherTechMod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.entity.PrimitiveAlloyStationBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrimitiveAlloyStationScreen extends AbstractContainerScreen<PrimitiveAlloyStationMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/gui/primitive_alloy/primitive_alloy_station_gui.png");

    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/gui/arrow_progress.png");


    // Tank constants - ALL USE LARGER DIMENSIONS NOW
    private static final int TANK_WIDTH = 52;  // Increased from 16 to 52
    private static final int TANK_HEIGHT = 78; // Height remains 78

    // Tank positions - updated to use larger dimensions
    private static final int COPPER_TANK_X = 119;
    private static final int TIN_TANK_X = 119;
    private static final int BRONZE_TANK_X = 128;
    private static final int TANK_Y = 4;

    // Fallback texture for missing fluids
    private static final ResourceLocation FALLBACK_FLUID_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "textures/fluid/gui/fallback.png");

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

        // Draw fluid tanks - ALL USE LARGER DIMENSIONS
        renderFluidTank(guiGraphics, x + COPPER_TANK_X, y + TANK_Y, menu.getCopperTank());
        renderFluidTank(guiGraphics, x + TIN_TANK_X, y + TANK_Y, menu.getTinTank());
        renderFluidTank(guiGraphics, x + BRONZE_TANK_X, y + TANK_Y, menu.getBronzeTank());
    }

    private void renderFluidTank(GuiGraphics guiGraphics, int x, int y, FluidTank tank) {
        FluidStack fluid = tank.getFluid();
        if (!fluid.isEmpty()) {
            IClientFluidTypeExtensions fluidType = IClientFluidTypeExtensions.of(fluid.getFluid());

            // Get texture with direct path handling
            ResourceLocation stillTexture = getValidFluidTexture(fluid);
            int color = fluidType.getTintColor(fluid);

            // Calculate fluid height based on amount (VERTICAL fill)
            int fluidHeight = (int) (TANK_HEIGHT * ((float) fluid.getAmount() / tank.getCapacity()));

            // Set color with alpha
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            RenderSystem.setShaderColor(r, g, b, 1.0F);

            // Draw fluid texture (tiled vertically)
            drawTiledFluid(guiGraphics, stillTexture,
                    x, y + (TANK_HEIGHT - fluidHeight), // Position from bottom
                    TANK_WIDTH, fluidHeight);

            // Reset color
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        // NOTE: Removed tank overlay since we're using larger dimensions
    }

    // Simplified texture handling
    private ResourceLocation getValidFluidTexture(FluidStack fluid) {
        // First try: Use the fluid's registered texture
        IClientFluidTypeExtensions fluidType = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation stillTexture = fluidType.getStillTexture(fluid);

        // Second try: Use direct path to copper texture
        ResourceLocation copperTexture = ResourceLocation.fromNamespaceAndPath(
                AnotherTechMod.MOD_ID,
                "textures/block/molten_copper_still.png"
        );

        // Third try: Fallback texture
        return textureExists(copperTexture) ? copperTexture :
                textureExists(stillTexture) ? stillTexture :
                        FALLBACK_FLUID_TEXTURE;
    }

    // Improved texture existence check
    private boolean textureExists(ResourceLocation location) {
        try {
            // Convert to resource path
            ResourceLocation resourcePath = ResourceLocation.fromNamespaceAndPath(
                    location.getNamespace(),
                    location.getPath().endsWith(".png") ?
                            location.getPath() :
                            location.getPath() + ".png"
            );

            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(resourcePath);
            return resource.isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to draw tiled fluid textures (bottom-up)
    private void drawTiledFluid(GuiGraphics guiGraphics, ResourceLocation texture,
                                int x, int y, int width, int height) {
        int textureSize = 16; // Standard fluid texture size
        int yPos = y;
        int remainingHeight = height;

        while (remainingHeight > 0) {
            int segmentHeight = Math.min(remainingHeight, textureSize);
            int textureOffset = textureSize - segmentHeight;

            guiGraphics.blit(
                    texture,
                    x,
                    yPos,
                    0,
                    textureOffset,
                    width,
                    segmentHeight,
                    textureSize,
                    textureSize
            );

            yPos += segmentHeight;
            remainingHeight -= segmentHeight;
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

        // Calculate GUI origin on screen
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Add fluid tooltips for all tanks
        renderFluidTooltip(guiGraphics, mouseX, mouseY, x + COPPER_TANK_X, y + TANK_Y, menu.getCopperTank());
        renderFluidTooltip(guiGraphics, mouseX, mouseY, x + TIN_TANK_X, y + TANK_Y, menu.getTinTank());
        renderFluidTooltip(guiGraphics, mouseX, mouseY, x + BRONZE_TANK_X, y + TANK_Y, menu.getBronzeTank());
    }

    private void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int screenX, int screenY, FluidTank tank) {
        if (isHovering(screenX, screenY, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            FluidStack fluid = tank.getFluid();
            if (!fluid.isEmpty()) {
                int amount = fluid.getAmount(); // mB

                // Convert to ingots and blocks
                int ingots = amount / 144;
                int blocks = ingots / 9;
                int ingotRemainder = ingots % 9;
                int mbRemainder = amount % 144;

                // Build parts with proper formatting
                List<MutableComponent> parts = new ArrayList<>();
                if (blocks > 0) {
                    parts.add(Component.literal(blocks + "").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(" Blocks").withStyle(ChatFormatting.GRAY)));
                }
                if (ingotRemainder > 0) {
                    parts.add(Component.literal(ingotRemainder + "").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(" Ingots").withStyle(ChatFormatting.GRAY)));
                }
                if (mbRemainder > 0) {
                    parts.add(Component.literal(mbRemainder + "").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(" mB").withStyle(ChatFormatting.GRAY)));
                }

                // Combine into one line
                MutableComponent prettyAmount;
                if (parts.isEmpty()) {
                    prettyAmount = Component.literal("0 mB").withStyle(ChatFormatting.GRAY);
                } else {
                    prettyAmount = parts.get(0);
                    for (int i = 1; i < parts.size(); i++) {
                        prettyAmount.append(Component.literal(" ")).append(parts.get(i));
                    }
                }


                // Tooltip list
                List<Component> tooltip = List.of(
                        fluid.getDisplayName(),
                        Component.literal(amount + " / " + tank.getCapacity() + " mB").withStyle(ChatFormatting.GRAY),
                        prettyAmount
                );

                guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
            }
        }
    }


    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        // Convert to GUI-relative mouse coords
        int relMouseX = mouseX - leftPos;
        int relMouseY = mouseY - topPos;

        // Tank bounds
        int tankX = 119;
        int tankY = 4;
        int tankWidth = 52;
        int tankHeight = 78;

        // Hover detection
        if (relMouseX >= tankX && relMouseX < tankX + tankWidth &&
                relMouseY >= tankY && relMouseY < tankY + tankHeight) {

            int mb = menu.getCopperTank().getFluidAmount();
            int blocks = mb / 1296;
            int ingots = (mb % 1296) / 144;
            int mbRemainder = mb % 144;

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Molten Copper"));
            tooltip.add(Component.literal(mb + " mB"));
            tooltip.add(Component.literal(blocks + " Blocks " + ingots + " Ingots " + mbRemainder + " mB"));

            // Tooltip follows mouse vertically, fixed beside GUI horizontally
            graphics.renderTooltip(font, tooltip, Optional.empty(),
                    leftPos + tankX + tankWidth -128 , mouseY  -4);
        }
    }
}