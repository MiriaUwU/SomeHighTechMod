package net.MiriaUwU.AnotherTechMod.screen.custom;

import net.MiriaUwU.AnotherTechMod.entity.PrimitiveAlloyStationBlockEntity;
import net.MiriaUwU.AnotherTechMod.fluid.ModFluids;
import net.MiriaUwU.AnotherTechMod.screen.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.SlotItemHandler;


public class PrimitiveAlloyStationMenu extends AbstractContainerMenu {
private final PrimitiveAlloyStationBlockEntity blockEntity;
private final ContainerData containerData;

private static final int HOTBAR_SLOT_COUNT = 9;
private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
private static final int TE_INVENTORY_SLOT_COUNT = 10;

// SERVER constructor
public PrimitiveAlloyStationMenu(int containerId, Inventory playerInventory, PrimitiveAlloyStationBlockEntity blockEntity, ContainerData data) {
    super(ModMenuTypes.PRIMITIVEALLOYSTATION_MENU.get(), containerId);
    this.blockEntity = blockEntity;
    this.containerData = data;

    // Add machine slots
    addMachineSlots(blockEntity);

    addPlayerInventory(playerInventory);
    addPlayerHotbar(playerInventory);

    // Sync container data
    addDataSlots(data);
}

// CLIENT constructor
public PrimitiveAlloyStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
    super(ModMenuTypes.PRIMITIVEALLOYSTATION_MENU.get(), containerId);

    BlockPos pos = extraData.readBlockPos();
    Level level = playerInventory.player.level();
    this.blockEntity = (PrimitiveAlloyStationBlockEntity) level.getBlockEntity(pos);
    this.containerData = blockEntity.getContainerData();

    // Add machine slots
    addMachineSlots(blockEntity);

    addPlayerInventory(playerInventory);
    addPlayerHotbar(playerInventory);

    // Sync container data
    addDataSlots(this.containerData);
}

// Add fluid synchronization handler
@Override
public void setData(int id, int data) {
    super.setData(id, data);
    if (blockEntity != null) {
        if (id == 2) {
            Fluid fluid = ModFluids.getSourceFluid("molten_copper");
            blockEntity.getCopperTank().setFluid(new FluidStack(fluid, data));
        }
        if (id == 3) {
            Fluid fluid = ModFluids.getSourceFluid("molten_tin");
            blockEntity.getTinTank().setFluid(new FluidStack(fluid, data));
        }
        if (id == 4) {
            Fluid fluid = ModFluids.getSourceFluid("molten_bronze");
            if (fluid == null) {
                fluid = Fluids.EMPTY; // Fallback if bronze isn't defined
            }
            blockEntity.getBronzeTank().setFluid(new FluidStack(fluid, data));
        }
    }
}

private void addMachineSlots(PrimitiveAlloyStationBlockEntity be) {
    this.addSlot(new SlotItemHandler(be.itemHandler, 0, 30, 17));
    this.addSlot(new SlotItemHandler(be.itemHandler, 1, 48, 17));
    this.addSlot(new SlotItemHandler(be.itemHandler, 2, 66, 17));

    this.addSlot(new SlotItemHandler(be.itemHandler, 3, 12, 35));
    this.addSlot(new SlotItemHandler(be.itemHandler, 4, 30, 35));
    this.addSlot(new SlotItemHandler(be.itemHandler, 5, 48, 35));
    this.addSlot(new SlotItemHandler(be.itemHandler, 6, 66, 35));

    this.addSlot(new SlotItemHandler(be.itemHandler, 7, 30, 53));
    this.addSlot(new SlotItemHandler(be.itemHandler, 8, 48, 53));
    this.addSlot(new SlotItemHandler(be.itemHandler, 9, 66, 53));
}

private void addPlayerInventory(Inventory playerInventory) {
    for (int i = 0; i < PLAYER_INVENTORY_ROW_COUNT; ++i) {
        for (int l = 0; l < PLAYER_INVENTORY_COLUMN_COUNT; ++l) {
            this.addSlot(new Slot(playerInventory, l + i * PLAYER_INVENTORY_COLUMN_COUNT + 9,
                    8 + l * 18, 84 + i * 18));
        }
    }
}

private void addPlayerHotbar(Inventory playerInventory) {
    for (int i = 0; i < HOTBAR_SLOT_COUNT; ++i) {
        this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
    }
}

@Override
public boolean stillValid(Player player) {
    return blockEntity.stillValid(player);
}

@Override
public ItemStack quickMoveStack(Player player, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot != null && slot.hasItem()) {
        ItemStack stackInSlot = slot.getItem();
        itemstack = stackInSlot.copy();

        if (index < TE_INVENTORY_SLOT_COUNT) {
            if (!this.moveItemStackTo(stackInSlot, TE_INVENTORY_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stackInSlot, 0, TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
    }

    return itemstack;
}

public boolean isAlloying() {
    return containerData.get(0) > 0;
}

public int getScaledArrowProgress() {
    int progress = containerData.get(0);
    int maxProgress = containerData.get(1);
    int arrowPixelSize = 24;

    if (maxProgress == 0 || progress == 0) {
        return 0;
    }
    return progress * arrowPixelSize / maxProgress;
}

// Add fluid getters for the screen
public FluidTank getCopperTank() {
    return blockEntity.getCopperTank();
}

public FluidTank getTinTank() {
    return blockEntity.getTinTank();
}

public FluidTank getBronzeTank() {
    return blockEntity.getBronzeTank();
}

// Add debug method
public void debugFluidAmounts() {
    System.out.println("[Menu Debug] Copper: " + getCopperTank().getFluidAmount() +
            " | Tin: " + getTinTank().getFluidAmount() +
            " | Bronze: " + getBronzeTank().getFluidAmount());
}
}