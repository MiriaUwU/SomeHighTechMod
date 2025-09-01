package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.energy.FeEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class SolarPanelBlockEntity extends BlockEntity {
    private final FeEnergyStorage energyStorage = createEnergyStorage();
    private static final int GENERATION_RATE = 20; // FE per tick in sunlight
    private static final int CAPACITY = 10000;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.SOLAR_PANEL.get(),
                (blockEntity, direction) -> blockEntity.energyStorage);
    }

    private FeEnergyStorage createEnergyStorage() {
        return new FeEnergyStorage(CAPACITY, 100, 100);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity blockEntity) {
        boolean changed = false;

        if (blockEntity.canGenerateEnergy(level, pos)) {
            int received = blockEntity.energyStorage.receiveEnergy(GENERATION_RATE, false);
            if (received > 0) changed = true;
        }

        // Distribute energy to adjacent blocks
        if (blockEntity.distributeEnergy(level, pos)) {
            changed = true;
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    private boolean canGenerateEnergy(Level level, BlockPos pos) {
        // Check if it's day time and sky is visible
        return level.isDay() &&
                level.canSeeSky(pos.above()) &&
                !level.isRaining() &&
                !level.isThundering();
    }

    private boolean distributeEnergy(Level level, BlockPos pos) {
        if (energyStorage.getEnergyStored() <= 0) return false;

        boolean energyTransferred = false;
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);

            IEnergyStorage adjacentEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, adjacentPos, direction.getOpposite());
            if (adjacentEnergy != null && adjacentEnergy.canReceive()) {
                int toTransfer = Math.min(energyStorage.getEnergyStored(), 100);
                int transferred = adjacentEnergy.receiveEnergy(toTransfer, false);
                if (transferred > 0) {
                    energyStorage.extractEnergy(transferred, false);
                    energyTransferred = true;
                }
            }
        }
        return energyTransferred;
    }

    public FeEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Energy")) {
            energyStorage.setEnergy(tag.getInt("Energy"));
        }
    }
}