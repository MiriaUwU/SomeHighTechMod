package net.MiriaUwU.AnotherTechMod.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public class FeEnergyStorage extends EnergyStorage {

    public FeEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    // Allow direct setting when loading from NBT
    public void setEnergy(int energy) {
        this.energy = energy;
        onEnergyChanged();
    }

    public int getEnergyInternal() {
        return energy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) {
            onEnergyChanged();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) {
            onEnergyChanged();
        }
        return extracted;
    }

    protected void onEnergyChanged() {
        // Override this method in your block entity
    }
}
