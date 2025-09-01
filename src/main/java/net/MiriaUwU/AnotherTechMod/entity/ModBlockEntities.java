package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AnotherTechMod.MOD_ID);

    public static final Supplier<BlockEntityType<DistillerBlockEntity>> DISTILLER_BE =
            BLOCK_ENTITIES.register("distiller_be", () -> BlockEntityType.Builder.of(
                    DistillerBlockEntity::new, ModBlocks.Distiller.get()).build(null));


    public static final Supplier<BlockEntityType<FabricatorBlockEntity>> FABRICATOR_BE =
            BLOCK_ENTITIES.register("fabricator_be", () -> BlockEntityType.Builder.of(
                    FabricatorBlockEntity::new, ModBlocks.Fabricator.get()).build(null));


    public static final Supplier<BlockEntityType<PrimitiveAlloyStationBlockEntity>> PRIMATIVEALLOYSTATION_BE =
            BLOCK_ENTITIES.register("primativealloystation_be", () -> BlockEntityType.Builder.of(
                    PrimitiveAlloyStationBlockEntity::new, ModBlocks.PRIMATIVEALLOYSTATIONBLOCK.get()).build(null));

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL =
            BLOCK_ENTITIES.register("solar_panel", () ->
                    BlockEntityType.Builder.of(SolarPanelBlockEntity::new,
                            ModBlocks.SOLAR_PANEL.get()).build(null));





    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
