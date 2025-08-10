package net.MiriaUwU.AnotherTechMod.fluid;

import net.MiriaUwU.AnotherTechMod.entity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CapabilityHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.PRIMATIVEALLOYSTATION_BE.get(),
                (be, context) -> new CombinedFluidHandler(
                        be.getCopperTank(),
                        be.getTinTank(),
                        be.getBronzeTank()
                )
        );
    }

    // Helper class to combine multiple tanks
    public static class CombinedFluidHandler implements IFluidHandler {
        private final FluidTank[] tanks;

        public CombinedFluidHandler(FluidTank... tanks) {
            this.tanks = tanks;
        }

        @Override
        public int getTanks() {
            return tanks.length;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tanks[tank].getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tanks[tank].getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tanks[tank].isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            for (FluidTank tank : tanks) {
                if (tank.isFluidValid(resource)) {
                    return tank.fill(resource, action);
                }
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (FluidTank tank : tanks) {
                if (tank.getFluid().isFluidEqual(resource)) {
                    return tank.drain(resource, action);
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (FluidTank tank : tanks) {
                if (!tank.isEmpty()) {
                    return tank.drain(maxDrain, action);
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
