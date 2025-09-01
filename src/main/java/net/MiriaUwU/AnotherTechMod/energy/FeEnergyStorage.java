package net.MiriaUwU.AnotherTechMod.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public class FeEnergyStorage extends EnergyStorage {

    public FeEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    // Allow direct setting when loading from NBT
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getEnergyInternal() {
        return energy;
    }
}
