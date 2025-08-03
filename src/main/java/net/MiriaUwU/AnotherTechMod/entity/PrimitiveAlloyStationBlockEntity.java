package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.block.ModBlockProperties;
import net.MiriaUwU.AnotherTechMod.item.ModItems;
import net.MiriaUwU.AnotherTechMod.screen.custom.PrimitiveAlloyStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PrimitiveAlloyStationBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final DirectionProperty FACING = ModBlockProperties.FACING;
    public final ItemStackHandler itemHandler = new ItemStackHandler(10) { // 10 input slots
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Liquid amounts stored as "units" (e.g. 100 units per ingot melted)
    private final Map<Item, Integer> liquidMetals = new HashMap<>();
    private static final int LIQUID_PER_INGOT = 100;
    private static final int LIQUID_NEEDED_FOR_ALLOY = 100; // 1 ingot worth of liquid needed to alloy

    // Output liquid bronze amount
    private int liquidBronze = 0;

    public PrimitiveAlloyStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMATIVEALLOYSTATION_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yourmod.primitive_alloy_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PrimitiveAlloyStationMenu(id, inventory, this, new SimpleContainerData(2));
    }
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
        // Optionally drop liquid bronze as item or something else on block break
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("inventory", itemHandler.serializeNBT(provider));

        // Save liquids
        ListTag liquidList = new ListTag();
        for (Map.Entry<Item, Integer> entry : liquidMetals.entrySet()) {
            CompoundTag metalTag = new CompoundTag();
            metalTag.putString("item", BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
            metalTag.putInt("amount", entry.getValue());
            liquidList.add(metalTag);
        }
        tag.put("liquidMetals", liquidList);

        tag.putInt("liquidBronze", liquidBronze);
        super.saveAdditional(tag, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));

        liquidMetals.clear();
        ListTag liquidList = tag.getList("liquidMetals", Tag.TAG_COMPOUND);
        for (int i = 0; i < liquidList.size(); i++) {
            CompoundTag metalTag = liquidList.getCompound(i);
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(metalTag.getString("item")));
            int amount = metalTag.getInt("amount");
            liquidMetals.put(item, amount);

        }
        liquidBronze = tag.getInt("liquidBronze");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PrimitiveAlloyStationBlockEntity entity) {
        if (level.isClientSide()) return;

        boolean changed = false;

        // Melt all input ingots into their liquid forms
        for (int slot = 0; slot < entity.itemHandler.getSlots(); slot++) {
            ItemStack stack = entity.itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && isMeltableMetal(stack.getItem())) {
                entity.meltMetal(slot);
                changed = true;
            }
        }

        // Alloy liquid copper + tin into bronze
        int copperAmount = entity.liquidMetals.getOrDefault(Items.COPPER_INGOT, 0);
        int tinAmount = entity.liquidMetals.getOrDefault(ModItems.Tin.get(), 0);

        if (copperAmount >= LIQUID_NEEDED_FOR_ALLOY && tinAmount >= LIQUID_NEEDED_FOR_ALLOY) {
            // Remove 1 ingot worth of liquid copper and tin
            entity.liquidMetals.put(Items.COPPER_INGOT, copperAmount - LIQUID_NEEDED_FOR_ALLOY);
            entity.liquidMetals.put(ModItems.Tin.get(), tinAmount - LIQUID_NEEDED_FOR_ALLOY);

            // Add 1 ingot worth liquid bronze
            entity.liquidBronze += LIQUID_NEEDED_FOR_ALLOY;

            changed = true;
        }

        if (changed) {
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static boolean isMeltableMetal(Item item) {
        // Check if this item can melt into liquid metal form (you can add more)
        return item == Items.COPPER_INGOT || item == ModItems.Tin.get();
        // You can add more metals like iron, nickel, zinc etc.
    }

    private void meltMetal(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) return;

        Item metal = stack.getItem();
        int currentAmount = liquidMetals.getOrDefault(metal, 0);

        // Add liquid only if not exceeding a max limit (optional)
        int maxLiquid = 1000;
        if (currentAmount + LIQUID_PER_INGOT <= maxLiquid) {
            liquidMetals.put(metal, currentAmount + LIQUID_PER_INGOT);
            itemHandler.extractItem(slot, 1, false);
        }
    }
    @Override
    public int getContainerSize() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack stack = itemHandler.extractItem(index, count, false);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = itemHandler.getStackInSlot(index);
        if (!stack.isEmpty()) {
            itemHandler.setStackInSlot(index, ItemStack.EMPTY);
            setChanged();
        }
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
        setChanged();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64;
    }
}
