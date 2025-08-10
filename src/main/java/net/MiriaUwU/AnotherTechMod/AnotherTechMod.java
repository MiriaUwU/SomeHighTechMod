package net.MiriaUwU.AnotherTechMod;

import net.MiriaUwU.AnotherTechMod.block.ModBlocks;
import net.MiriaUwU.AnotherTechMod.client.OilFogHandler;
import net.MiriaUwU.AnotherTechMod.entity.ModBlockEntities;
import net.MiriaUwU.AnotherTechMod.fluid.CapabilityHandler;
import net.MiriaUwU.AnotherTechMod.fluid.ModFluids;
import net.MiriaUwU.AnotherTechMod.fluid.OilFluids;
import net.MiriaUwU.AnotherTechMod.init.ModFlammables;
import net.MiriaUwU.AnotherTechMod.item.ModCreativeModeTabs;
import net.MiriaUwU.AnotherTechMod.item.ModItems;

import net.MiriaUwU.AnotherTechMod.recipes.ModRecipes;
import net.MiriaUwU.AnotherTechMod.screen.ModMenuTypes;
import net.MiriaUwU.AnotherTechMod.screen.custom.DistillerScreen;
import net.MiriaUwU.AnotherTechMod.screen.custom.FabricatorScreen;
import net.MiriaUwU.AnotherTechMod.screen.custom.PrimitiveAlloyStationScreen;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static net.MiriaUwU.AnotherTechMod.item.ModItems.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AnotherTechMod.MOD_ID)
public class AnotherTechMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "yetanothertechmod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();



    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AnotherTechMod(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);


        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        OilFluids.register(modEventBus);
        OilFogHandler.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModRecipes.register(modEventBus);
        modEventBus.addListener(CapabilityHandler::registerCapabilities);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModFlammables::registerflammables);


    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
          event.accept(Tin);
          event.accept(Brass);
          event.accept(RawTin);
          event.accept(Bronze);
      }

      if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
          event.accept(ModBlocks.Tin_Block);
          event.accept(ModBlocks.Tin_ore);
          event.accept(ModBlocks.Brass_block);
          event.accept(ModBlocks.Bronze_block);

      }

      if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
        event.accept((ItemLike) OilFluids.OIL_BUCKET);

      }



    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = AnotherTechMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {


        }

      @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
          event.register(ModMenuTypes.DISTILLER__MENU.get(), DistillerScreen::new);
          event.register(ModMenuTypes.FABRICATOR_MENU.get(), FabricatorScreen::new);
          event.register(ModMenuTypes.PRIMITIVEALLOYSTATION_MENU.get(), PrimitiveAlloyStationScreen::new);


      }







    }
}
