package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.block.ModBlockProperties;
import net.MiriaUwU.AnotherTechMod.client.FluidLayer;
import net.MiriaUwU.AnotherTechMod.fluid.ModFluids;
import net.MiriaUwU.AnotherTechMod.fluid.SmeltableFluidsRegistry;
import net.MiriaUwU.AnotherTechMod.item.ModItems;
import net.MiriaUwU.AnotherTechMod.recipes.Alloys.PrimitiveAlloyRecipe;
import net.MiriaUwU.AnotherTechMod.recipes.Alloys.PrimitiveAlloyRecipeInput;
import net.MiriaUwU.AnotherTechMod.recipes.ModRecipes;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class PrimitiveAlloyStationBlockEntity extends BlockEntity implements MenuProvider, Container {
    public final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int LIQUID_PER_INGOT = 144; // mB per ingot
    private static final int MAX_METAL_CAPACITY = 1440; // 10 ingots worth

    // DYNAMIC FLUID SYSTEM - replaces hardcoded tanks
    private final List<FluidTank> allInputTanks = new ArrayList<>();
    private final Map<Fluid, FluidTank> fluidTankMap = new HashMap<>();

    private int alloyingProgress = 0;
    private int maxAlloyingProgress = 100;

    // DYNAMIC CONTAINER DATA - automatically syncs all tanks
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) return alloyingProgress;
            if (index == 1) return maxAlloyingProgress;

            // Dynamically get fluid amounts starting at index 2
            int tankIndex = index - 2;
            if (tankIndex >= 0 && tankIndex < allInputTanks.size()) {
                return allInputTanks.get(tankIndex).getFluidAmount();
            }

            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                alloyingProgress = value;
            } else if (index == 1) {
                maxAlloyingProgress = value;
            }
            // Fluid amounts are synced via get(), not set()
        }

        @Override
        public int getCount() {
            // 2 for progress + number of tanks
            return 2 + allInputTanks.size();
        }
    };

    public PrimitiveAlloyStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMATIVEALLOYSTATION_BE.get(), pos, state);

        // Automatically register all fluids from the registry
        for (String fluidName : SmeltableFluidsRegistry.getAllFluidNames()) {
            registerFluidTank(fluidName);
        }
    }

    /**
     * Registers a new fluid tank dynamically
     */
    private void registerFluidTank(String fluidName) {
        Fluid fluid = ModFluids.getSourceFluid(fluidName);
        if (fluid == null || fluid == Fluids.EMPTY) {
            System.err.println("[Error] Failed to register tank for fluid: " + fluidName);
            return;
        }

        FluidTank tank = createTank(fluid, MAX_METAL_CAPACITY);
        fluidTankMap.put(fluid, tank);
        allInputTanks.add(tank);
    }

    private FluidTank createTank(Fluid validFluid, int capacity) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid() == validFluid;
            }
        };
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    /**
     * Get all non-empty fluid layers for rendering (DYNAMIC)
     * @return List of fluid layers from bottom to top
     */
    public List<FluidLayer> getFluidLayers() {
        List<FluidLayer> layers = new ArrayList<>();

        // Automatically add all non-empty tanks
        for (FluidTank tank : allInputTanks) {
            if (!tank.getFluid().isEmpty() && tank.getFluidAmount() > 0) {
                layers.add(new FluidLayer(tank.getFluid()));
            }
        }

        return layers;
    }

    /**
     * Get total fluid amount across all tanks
     */
    public int getTotalFluidAmount() {
        return allInputTanks.stream()
                .mapToInt(FluidTank::getFluidAmount)
                .sum();
    }

    /**
     * Get maximum total capacity
     */
    public int getTotalCapacity() {
        return allInputTanks.size() * MAX_METAL_CAPACITY;
    }

    /**
     * Get a specific tank by fluid type
     */
    public FluidTank getTankForFluid(Fluid fluid) {
        if (fluid == null) return null;
        return fluidTankMap.get(fluid);
    }

    /**
     * Get all fluid tanks
     */
    public List<FluidTank> getAllTanks() {
        return allInputTanks;
    }

    /**
     * Get tank by index (for syncing)
     */
    public FluidTank getTankByIndex(int index) {
        if (index >= 0 && index < allInputTanks.size()) {
            return allInputTanks.get(index);
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yetanothertechmod.primitive_alloy_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PrimitiveAlloyStationMenu(id, inventory, this, containerData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("inventory", itemHandler.serializeNBT(provider));

        // Save all tanks dynamically
        CompoundTag fluidsTag = new CompoundTag();
        for (int i = 0; i < allInputTanks.size(); i++) {
            FluidTank tank = allInputTanks.get(i);
            fluidsTag.put("Tank_" + i, tank.writeToNBT(provider, new CompoundTag()));
        }
        tag.put("Fluids", fluidsTag);

        tag.putInt("AlloyingProgress", alloyingProgress);
        tag.putInt("MaxAlloyingProgress", maxAlloyingProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        }

        // Load all tanks dynamically
        if (tag.contains("Fluids")) {
            CompoundTag fluidsTag = tag.getCompound("Fluids");
            for (int i = 0; i < allInputTanks.size(); i++) {
                if (fluidsTag.contains("Tank_" + i)) {
                    allInputTanks.get(i).readFromNBT(provider, fluidsTag.getCompound("Tank_" + i));
                }
            }
        }

        if (tag.contains("AlloyingProgress")) {
            alloyingProgress = tag.getInt("AlloyingProgress");
        }
        if (tag.contains("MaxAlloyingProgress")) {
            maxAlloyingProgress = tag.getInt("MaxAlloyingProgress");
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PrimitiveAlloyStationBlockEntity entity) {
        if (level.isClientSide()) return;
        boolean changed = false;

        // Melt metals per-slot
        for (int slot = 0; slot < entity.itemHandler.getSlots(); slot++) {
            ItemStack stack = entity.itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && entity.canMeltItem(stack.getItem())) {
                entity.meltMetal(slot);
                changed = true;
            }
        }

        if (changed) {
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean canMeltItem(Item item) {
        if (level == null) return false;
        return true; // Simplified for testing
    }

    private void meltMetal(int slot) {
        if (level == null) return;
        System.out.println("[DEBUG] Attempting to melt item in slot: " + slot);

        ItemStack currentItem = itemHandler.getStackInSlot(slot);
        PrimitiveAlloyRecipeInput input = new PrimitiveAlloyRecipeInput(currentItem);
        Optional<RecipeHolder<PrimitiveAlloyRecipe>> recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipes.PRIMITIVE_ALLOYING_TYPE.get(), input, level);

        if (recipeOpt.isPresent()) {
            PrimitiveAlloyRecipe recipe = recipeOpt.get().value();
            FluidStack outputFluid = recipe.getOutputFluid();

            // ADD NULL/EMPTY CHECK
            if (outputFluid.isEmpty()) {
                System.out.println("[ERROR] Recipe has empty fluid output: " + recipeOpt.get().id());
                return;
            }

            // Get fluid ID for debugging
            ResourceLocation fluidKey = BuiltInRegistries.FLUID.getKey(outputFluid.getFluid());
            System.out.println("[DEBUG] Found recipe! Output: " + fluidKey + " x " + outputFluid.getAmount());

            ItemStack stack = itemHandler.getStackInSlot(slot);
            FluidTank targetTank = getTankForFluid(outputFluid.getFluid());

            if (targetTank != null) {
                System.out.println("[DEBUG] Found target tank for fluid");
                int simulateFill = targetTank.fill(outputFluid, IFluidHandler.FluidAction.SIMULATE);

                if (simulateFill == outputFluid.getAmount()) {
                    System.out.println("[DEBUG] Filling tank with " + outputFluid.getAmount() + "mB");
                    targetTank.fill(outputFluid, IFluidHandler.FluidAction.EXECUTE);
                    itemHandler.extractItem(slot, 1, false);
                    System.out.println("[DEBUG] Successfully melted item!");
                } else {
                    System.out.println("[DEBUG] Not enough space in tank");
                }
            } else {
                // Get fluid ID for debugging
                ResourceLocation fluidKey2 = BuiltInRegistries.FLUID.getKey(outputFluid.getFluid());
                System.out.println("[DEBUG] No target tank found for fluid: " + fluidKey2);
            }
        } else {
            System.out.println("[DEBUG] No recipe found for items");
        }
    }

    // Debug method to fill tanks
    public void debugFillTanks() {
        FluidTank copperTank = getTankForFluid(ModFluids.getSourceFluid("molten_copper"));
        FluidTank tinTank = getTankForFluid(ModFluids.getSourceFluid("molten_tin"));

        if (copperTank != null) {
            copperTank.fill(new FluidStack(ModFluids.getSourceFluid("molten_copper"), 500), IFluidHandler.FluidAction.EXECUTE);
        }
        if (tinTank != null) {
            tinTank.fill(new FluidStack(ModFluids.getSourceFluid("molten_tin"), 500), IFluidHandler.FluidAction.EXECUTE);
        }
        setChanged();
    }

    // Container implementation
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