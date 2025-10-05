package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.energy.FeEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class BatteryBlockEntity extends BlockEntity {
    private static final int CAPACITY = 100000; // 100k FE capacity
    private static final int MAX_TRANSFER = 1000; // 1k FE/tick max transfer rate

    private final FeEnergyStorage energyStorage;

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BLOCK_ENTITY.get(), pos, state);

        this.energyStorage = new FeEnergyStorage(CAPACITY, MAX_TRANSFER, MAX_TRANSFER) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
                // Sync to client if needed
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        };
    }

    // Server tick method - Let cables handle the energy transfer
    public static void serverTick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity battery) {
        if (level.isClientSide) return;

        // Battery is passive - it only responds to energy requests from cables
        // The cables will pull energy from the battery when needed

    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setEnergy(tag.getInt("Energy"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
    }

    // Register capabilities - add this to your main mod class or capability registration
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.BATTERY_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.energyStorage);
    }

    public FeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }
}




