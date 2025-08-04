package net.MiriaUwU.AnotherTechMod.fluid;

import net.minecraft.resources.ResourceLocation;

public record FluidDefinition(
        String name,
        int color,
        int temperature,
        int viscosity,
        int density,
        ResourceLocation stillTexture,
        ResourceLocation flowingTexture
) {}
