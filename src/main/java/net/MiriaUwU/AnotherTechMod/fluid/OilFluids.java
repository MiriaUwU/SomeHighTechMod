package net.MiriaUwU.AnotherTechMod.fluid;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.block.custom.OilBlock;
import net.MiriaUwU.AnotherTechMod.item.custom.FuelBucketItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class OilFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, AnotherTechMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, AnotherTechMod.MOD_ID);
    public static final DeferredRegister<Item> BUCKETS = DeferredRegister.createItems(AnotherTechMod.MOD_ID);
    public static final DeferredRegister<Block> SOURCEBLOCKS = DeferredRegister.createBlocks(AnotherTechMod.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> LIQUID_TYPE = FLUID_TYPES.register("oil", () -> new FluidType(FluidType.Properties.create().descriptionId("oil_liquid")));

    public static final DeferredHolder<Fluid, FlowingFluid> OIL_SOURCE = FLUIDS.register("oil_source", () -> new BaseFlowingFluid.Source(liquidProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> OIL_FLOWING = FLUIDS.register("oil_flowing", () -> new BaseFlowingFluid.Flowing(liquidProperties()));

    public static final DeferredHolder<Item, FuelBucketItem> OIL_BUCKET =
            BUCKETS.register("oil_bucket",
                    () -> new FuelBucketItem(
                            OIL_SOURCE.get(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1),
                            1600
                    )
            );;


    public static final DeferredHolder<Block, LiquidBlock> OIL_BLOCK = SOURCEBLOCKS.register("oil_block", () -> new OilBlock(OIL_SOURCE.get(), BlockBehaviour.Properties.of().noCollission().strength(100f)));

    public static void register(IEventBus modbus) {
        FLUID_TYPES.register(modbus);
        FLUIDS.register(modbus);
        BUCKETS.register(modbus);
        SOURCEBLOCKS.register(modbus);
        modbus.addListener(OilFluids::clientExt);
    }

    private static final IClientFluidTypeExtensions liquidExt = new IClientFluidTypeExtensions() {
        @Override
        public ResourceLocation getStillTexture() {
            return ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/oil_still");
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/oil_flowing");
        }

    };

    private static void clientExt(RegisterClientExtensionsEvent event) {
        event.registerFluidType(liquidExt, LIQUID_TYPE.get());
    }

    private static Properties liquidProperties() {
        return new Properties(LIQUID_TYPE, OIL_SOURCE, OIL_FLOWING).bucket(OIL_BUCKET).block(OIL_BLOCK);
    }



}