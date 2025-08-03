package net.MiriaUwU.AnotherTechMod.screen.custom.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ItemStackHandlerSlot extends Slot {
    private final ItemStackHandler itemHandler;
    private final int index;

    public ItemStackHandlerSlot(ItemStackHandler itemHandler, int index, int x, int y) {
        // Pass a dummy Container since vanilla Slot requires one; we'll override methods anyway
        super(new SimpleContainer(1), 0, x, y);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public ItemStack getItem() {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    @Override
    public void set(ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        // Notify your container/menu if needed
        // This is usually fine as empty
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = itemHandler.extractItem(index, amount, false);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Customize slot input rules here, or just return true
        return true;
    }
}
