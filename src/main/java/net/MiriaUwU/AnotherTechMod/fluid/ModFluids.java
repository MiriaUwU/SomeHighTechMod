package net.MiriaUwU.AnotherTechMod.fluid;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.*;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, AnotherTechMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, AnotherTechMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, AnotherTechMod.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AnotherTechMod.MOD_ID);

    private static final Map<String, DeferredHolder<Fluid, BaseFlowingFluid>> SOURCE_FLUIDS = new HashMap<>();
    private static final Map<String, DeferredHolder<Item, BucketItem>> BUCKET_ITEMS = new HashMap<>();

    private static final List<FluidDefinition> FLUIDS_TO_REGISTER = List.of(
            new FluidDefinition(
                    "molten_copper",
                    0xFFAA5500,
                    1000,
                    6000,
                    10000,
                    ResourceLocation.fromNamespaceAndPath (AnotherTechMod.MOD_ID, "block/molten_copper_still"),
                    ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "block/molten_copper_flowing")
            ),
            new FluidDefinition(
                    "molten_tin",
                    0xFFCCCCCC,
                    900,
                    5000,
                    9500,
                    ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/molten_tin_still"),
                    ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/molten_tin_flowing")
            ),
            new FluidDefinition(
                    "molten_iron",
                    0xFFFF4500,
                    1100,
                    7000,
                    12000,
                    ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/molten_iron_still"),
                    ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/molten_iron_flowing")
            ),
            new FluidDefinition(
                    "molten_bronze",
                    0xFFCD7F32,
                    1050,
                    5500,
                    9500,
                    ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/molten_bronze_still"),
                    ResourceLocation.fromNamespaceAndPath("yetanothertechmod", "block/molten_bronze_flowing")
            )
            // Add more here
    );

    public static void register(IEventBus bus) {
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
        ITEMS.register(bus);
        BLOCKS.register(bus);
        bus.addListener(ModFluids::registerClientExtensions);

        for (FluidDefinition def : FLUIDS_TO_REGISTER) {
            registerFluid(def);
        }
    }

    private static void registerFluid(FluidDefinition def) {
        String name = def.name();

        DeferredHolder<FluidType, FluidType> fluidType = FLUID_TYPES.register(name, () ->
                new FluidType(FluidType.Properties.create()
                        .descriptionId("fluid." + name)
                        .density(def.density())
                        .viscosity(def.viscosity())
                        .temperature(def.temperature())
                        .lightLevel(2)
                        .canConvertToSource(true)
                ) {}
        );

        final DeferredHolder<Fluid, BaseFlowingFluid>[] holders = new DeferredHolder[2];

// Register block first; pass fluid source supplier to LiquidBlock constructor
        DeferredHolder<Block, LiquidBlock> block = BLOCKS.register(name + "_block", () ->
                new LiquidBlock(holders[0].get(), BlockBehaviour.Properties.of()
                        .noCollission()
                        .replaceable()
                        .strength(100f)
                        .noLootTable())
        );

        BaseFlowingFluid.Properties fluidProperties = new BaseFlowingFluid.Properties(
                fluidType,
                () -> holders[0].get(),
                () -> holders[1].get()
        )
                .tickRate(5)
                .levelDecreasePerBlock(1)
                .explosionResistance(100f)
                .block(() -> block.get());// <-- pass block instance here, NOT () -> block.get()

        holders[0] = FLUIDS.register(name + "_source", () ->
                new BaseFlowingFluid.Source(fluidProperties)
        );
        holders[1] = FLUIDS.register(name + "_flowing", () ->
                new BaseFlowingFluid.Flowing(fluidProperties)
        );

// Pass the actual Fluid instance to BucketItem constructor, not a supplier
        DeferredHolder<Item, BucketItem> bucket = ITEMS.register(name + "_bucket", () ->
                new BucketItem(holders[0].get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
        );

        BUCKET_ITEMS.put(name, bucket);
        SOURCE_FLUIDS.put(name, holders[0]);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (FluidDefinition def : FLUIDS_TO_REGISTER) {
            DeferredHolder<FluidType, ? extends FluidType> typeHolder = FLUID_TYPES.getEntries()
                    .stream()
                    .filter(entry -> entry.getId().getPath().equals(def.name()))
                    .findFirst()
                    .orElse(null);

            if (typeHolder != null) {
                event.registerFluidType(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        System.out.println("[DEBUG] Loading still texture: " + def.stillTexture());
                        return def.stillTexture();
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        System.out.println("[DEBUG] Loading flowing texture: " + def.flowingTexture());
                        return def.flowingTexture();
                    }

                    @Override
                    public int getTintColor() {
                        return def.color();
                    }
                }, typeHolder.get());
            } else {
                System.err.println("[ERROR] Could not find fluid type for: " + def.name());
            }
        }
    }

    public static Fluid getSourceFluid(String name) {
        DeferredHolder<Fluid, BaseFlowingFluid> holder = SOURCE_FLUIDS.get(name);
        if (holder == null) {
            System.err.println("[ModFluids] ERROR: Fluid '" + name + "' not found! Available fluids: " + SOURCE_FLUIDS.keySet());
            return Fluids.EMPTY;
        }
        return holder.get();
    }

    public static Collection<DeferredHolder<Item, BucketItem>> getBuckets() {
        return BUCKET_ITEMS.values();
    }
}