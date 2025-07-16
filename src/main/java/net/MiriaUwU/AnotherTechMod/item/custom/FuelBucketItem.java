package net.MiriaUwU.AnotherTechMod.item.custom;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import javax.annotation.Nullable;


public class FuelBucketItem extends BucketItem {
    private final int burnTime;

    public FuelBucketItem(Fluid fluid, Item.Properties properties, int burnTime) {
        super(fluid, properties);
        this.burnTime = burnTime;
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return burnTime;
    }
}