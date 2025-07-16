package net.MiriaUwU.AnotherTechMod.client;


import net.MiriaUwU.AnotherTechMod.fluid.OilFluids;
import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public class OilFogHandler {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(OilFogHandler::onClientSetup);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(OilFogHandler::onFogColor);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(OilFogHandler::onFogDensity);
    }

    private static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Camera camera = event.getCamera();
        FluidState fluidState = camera.getEntity().level().getFluidState(camera.getBlockPosition());

        if (fluidState.getType() == OilFluids.OIL_SOURCE.get() || fluidState.getType() == OilFluids.OIL_FLOWING.get()) {
            event.setRed(0.02F);
            event.setGreen(0.02F);
            event.setBlue(0.02F);
        }
    }

    private static void onFogDensity(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        FluidState fluidState = camera.getEntity().level().getFluidState(camera.getBlockPosition());

        if (fluidState.getType() == OilFluids.OIL_SOURCE.get() || fluidState.getType() == OilFluids.OIL_FLOWING.get()) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(2.0F);
            event.setCanceled(true);
        }
    }
}