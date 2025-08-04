package net.MiriaUwU.AnotherTechMod.screen.custom;

import net.MiriaUwU.AnotherTechMod.block.ModBlocks;
import net.MiriaUwU.AnotherTechMod.entity.PrimitiveAlloyStationBlockEntity;
import net.MiriaUwU.AnotherTechMod.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PrimitiveAlloyStationMenu extends AbstractContainerMenu {
    private final PrimitiveAlloyStationBlockEntity blockEntity;
    private final ContainerData data;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int TE_INVENTORY_SLOT_COUNT = 10; // your 10 slots from ItemStackHandler

    // Client constructor - reads block pos from buf
    public PrimitiveAlloyStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (PrimitiveAlloyStationBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(2));
    }

    // Server constructor - actual block entity and container data
    public PrimitiveAlloyStationMenu(int containerId, Inventory playerInventory, PrimitiveAlloyStationBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.PRIMITIVEALLOYSTATION_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        // Explicit slot positions matching 3-4-3 layout

        // Top row (3 slots)
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 0, 30, 17));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 1, 48, 17));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 2, 66, 17));

        // Middle row (4 slots)
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 3, 12, 35));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 4, 30, 35));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 5, 48, 35));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 6, 66, 35));

        // Bottom row (3 slots)
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 7, 30, 53));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 8, 48, 53));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 9, 66, 53));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }


    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.PRIMATIVEALLOYSTATIONBLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            int containerSlots = TE_INVENTORY_SLOT_COUNT;
            if (index < containerSlots) {
                // Move from container to player inventory
                if (!moveItemStackTo(stackInSlot, containerSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to container
                if (!moveItemStackTo(stackInSlot, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }

    // Added methods for GUI progress

    public boolean isAlloying() {
        return data.get(0) > 0;
    }

    public int getScaledArrowProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        int arrowPixelSize = 24;

        if (maxProgress == 0 || progress == 0) {
            return 0;
        }
        return progress * arrowPixelSize / maxProgress;
    }
}
