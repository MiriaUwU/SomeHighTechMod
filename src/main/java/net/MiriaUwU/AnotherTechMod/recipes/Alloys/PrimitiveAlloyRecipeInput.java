package net.MiriaUwU.AnotherTechMod.recipes.Alloys;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.Arrays;
import java.util.List;

public class PrimitiveAlloyRecipeInput implements RecipeInput {
    private final List<ItemStack> items;

    // Fixed: Now accepts List<ItemStack> instead of ItemStack[]
    public PrimitiveAlloyRecipeInput(List<ItemStack> items) {
        this.items = items;
    }

    // Alternative constructor for single item
    public PrimitiveAlloyRecipeInput(ItemStack item) {
        this.items = Arrays.asList(item);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public ItemStack getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return ItemStack.EMPTY;
    }
}
