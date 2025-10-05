package net.MiriaUwU.AnotherTechMod.block;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.block.custom.*;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.MiriaUwU.AnotherTechMod.item.ModItems.ITEMS;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AnotherTechMod.MOD_ID);

    public static final DeferredBlock<Block> Tin_Block = registerblock("tin_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops().sound(SoundType.COPPER)));

public  static final DeferredBlock<Block> Tin_ore = registerblock("tin_ore",
        () -> new DropExperienceBlock(UniformInt.of(2, 4),
  BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

public static final DeferredBlock<Block> Brass_block = registerblock("brass_block",
        () -> new Block(BlockBehaviour.Properties.of()
                .strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));


    public static final DeferredBlock<Block> Bronze_block = registerblock("bronze_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));


    public static final DeferredBlock<Block> Distiller = registerblock("distiller",
            () -> new DistillerBlock(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<Block> Fabricator = registerblock("fabricator",
            () -> new FabricatorBlock(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops().sound(SoundType.METAL)));


    public static final DeferredRegister<Block> SOURCEBLOCKS = DeferredRegister.createBlocks(AnotherTechMod.MOD_ID);

    public static final DeferredBlock<Block> PRIMATIVEALLOYSTATIONBLOCK = registerblock("primitive_alloy_station_block",
            () -> new PrimitiveAlloyStationBlock(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops().sound(SoundType.METAL).mapColor(MapColor.STONE)));


    public static final DeferredBlock<Block> SOLAR_PANEL = registerblock("solar_panel",
            () -> new SolarPanelBlock(BlockBehaviour.Properties.of()
                    .strength(4F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).mapColor(MapColor.STONE)));

    public static final DeferredBlock<Block>  ENERGY_CABLE = BLOCKS.register("energy_cable",
            () -> new EnergyCableBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));


    public  static final DeferredBlock<Block> BATTERY_BLOCK =
            BLOCKS.register("battery_block", () -> new BatteryBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));







    private static <T extends Block> DeferredBlock<T> registerblock(String name, Supplier<T> block) {
      DeferredBlock<T> toreturn = BLOCKS.register(name, block);
      registerBlockItem(name, toreturn);
      return toreturn;
  }













 private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
     ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
 }


    public static void register (IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}