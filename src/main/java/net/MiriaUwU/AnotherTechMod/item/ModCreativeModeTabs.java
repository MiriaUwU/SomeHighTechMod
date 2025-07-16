package net.MiriaUwU.AnotherTechMod.item;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.block.ModBlocks;
import net.MiriaUwU.AnotherTechMod.fluid.OilFluids;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {

     public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
             DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AnotherTechMod.MOD_ID);

  public static final Supplier<CreativeModeTab> INGOTS_TAB = CREATIVE_MODE_TAB.register("ingots_tab",
          () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.Brass.get()))
                  .title(Component.translatable("creativetab.yetanothertechmod.ingots_items"))
                  .displayItems((itemDisplayParameters, output) -> {
                      output.accept(ModItems.Brass);
                      output.accept(ModItems.Tin);
                      output.accept(ModItems.RawTin);
                  }) .build());



 public static final Supplier<CreativeModeTab> Block_TAB = CREATIVE_MODE_TAB.register("blocks_tab",
          () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.Brass_block.get()))
                  .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "ingots_tab"))
                  .title(Component.translatable("creativetab.yetanothertechmod.blocks"))
                  .displayItems((itemDisplayParameters, output) -> {
                      output.accept(ModBlocks.Brass_block);
                      output.accept(ModBlocks.Tin_Block);
                      output.accept(ModBlocks.Tin_ore);
                  }) .build());



    public static final Supplier<CreativeModeTab> FLUID_TAB = CREATIVE_MODE_TAB.register("fluid_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(OilFluids.OIL_BUCKET.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "blocks_tab"))
                    .title(Component.translatable("creativetab.yetanothertechmod.fluids"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept((ItemLike) OilFluids.OIL_BUCKET);
                    }) .build());






public static void register(IEventBus eventBus) {
         CREATIVE_MODE_TAB.register(eventBus);
}
}


