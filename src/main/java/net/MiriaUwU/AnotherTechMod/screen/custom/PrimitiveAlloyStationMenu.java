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

        // Add your block entity slots (assuming 10 slots)
        for (int i = 0; i < 10; i++) {
            // Positions here need to be adjusted to your GUI layout
            // For example, arrange slots vertically or in a grid:
            int x = 8 + (i % 5) * 18; // 5 slots per row
            int y = 18 + (i / 5) * 18;
            this.addSlot(new SlotItemHandler(blockEntity.itemHandler, i, x, y));
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMN_COUNT; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < HOTBAR_SLOT_COUNT; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
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
