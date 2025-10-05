package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.block.custom.EnergyCableBlock;
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

import java.util.ArrayList;
import java.util.List;

public class EnergyCableBlockEntity extends BlockEntity {
    private final FeEnergyStorage energyStorage = new FeEnergyStorage(1000, 200, 200); // Small buffer
    private static final int TRANSFER_RATE = 100; // FE per tick per connection

    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE.get(), pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ENERGY_CABLE.get(),
                (blockEntity, direction) -> blockEntity.energyStorage);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyCableBlockEntity blockEntity) {
        blockEntity.transferEnergy(level, pos, state);
    }

    private void transferEnergy(Level level, BlockPos pos, BlockState state) {
        if (energyStorage.getEnergyStored() <= 0) return;

        // List to store all connected energy storages with their directions
        List<IEnergyStorage> receivers = new ArrayList<>();

        // Find all connected receivers
        for (Direction direction : Direction.values()) {
            if (isConnectedInDirection(state, direction)) {
                BlockPos neighborPos = pos.relative(direction);
                IEnergyStorage neighborEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());

                if (neighborEnergy != null && neighborEnergy.canReceive() && neighborEnergy != energyStorage) {
                    receivers.add(neighborEnergy);
                }
            }
        }

        if (receivers.isEmpty()) return;

        // Distribute energy evenly among receivers
        int energyPerReceiver = Math.min(energyStorage.getEnergyStored() / receivers.size(), TRANSFER_RATE);
        boolean energyTransferred = false;

        for (IEnergyStorage receiver : receivers) {
            int transferred = receiver.receiveEnergy(energyPerReceiver, false);
            if (transferred > 0) {
                energyStorage.extractEnergy(transferred, false);
                energyTransferred = true;
            }
        }

        if (energyTransferred) {
            setChanged();
        }
    }

    private boolean isConnectedInDirection(BlockState state, Direction direction) {
        return switch (direction) {
            case NORTH -> state.getValue(EnergyCableBlock.NORTH);
            case SOUTH -> state.getValue(EnergyCableBlock.SOUTH);
            case EAST -> state.getValue(EnergyCableBlock.EAST);
            case WEST -> state.getValue(EnergyCableBlock.WEST);
            case UP -> state.getValue(EnergyCableBlock.UP);
            case DOWN -> state.getValue(EnergyCableBlock.DOWN);
        };
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
