package net.MiriaUwU.AnotherTechMod.client;

import net.neoforged.neoforge.fluids.FluidStack;

public class FluidLayer {
    private final FluidStack fluidStack;
    private final int amount;

    public FluidLayer(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        this.amount = fluidStack.getAmount();
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isEmpty() {
        return fluidStack.isEmpty() || amount <= 0;
    }
}