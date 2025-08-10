package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.block.ModBlockProperties;
import net.MiriaUwU.AnotherTechMod.fluid.ModFluids;
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

    // Fluid tanks
    private final FluidTank moltenCopperTank = createTank(ModFluids.getSourceFluid("molten_copper"), MAX_METAL_CAPACITY);
    private final FluidTank moltenTinTank = createTank(ModFluids.getSourceFluid("molten_tin"), MAX_METAL_CAPACITY);
    private final FluidTank moltenBronzeTank = createTank(ModFluids.getSourceFluid("molten_bronze"), MAX_METAL_CAPACITY);

    // Fluid-to-tank map for dynamic lookup
    private final Map<Fluid, FluidTank> fluidTankMap;

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

    private int alloyingProgress = 0;
    private int maxAlloyingProgress = 100;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> alloyingProgress;
                case 1 -> maxAlloyingProgress;
                case 2 -> moltenCopperTank.getFluidAmount();
                case 3 -> moltenTinTank.getFluidAmount();
                case 4 -> moltenBronzeTank.getFluidAmount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> alloyingProgress = value;
                case 1 -> maxAlloyingProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public PrimitiveAlloyStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMATIVEALLOYSTATION_BE.get(), pos, state);

        // Initialize fluidTankMap dynamically
        fluidTankMap = Map.of(
                ModFluids.getSourceFluid("molten_copper"), moltenCopperTank,
                ModFluids.getSourceFluid("molten_tin"), moltenTinTank,
                ModFluids.getSourceFluid("molten_bronze"), moltenBronzeTank
        );
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    private final List<FluidTank> fluidTanks = List.of(moltenCopperTank, moltenTinTank); // add more if needed later */);

    public List<FluidTank> getFluidTanks() {
        return fluidTanks;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yourmod.primitive_alloy_station");
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

        CompoundTag fluidsTag = new CompoundTag();
        fluidsTag.put("Copper", moltenCopperTank.writeToNBT(provider, new CompoundTag()));
        fluidsTag.put("Tin", moltenTinTank.writeToNBT(provider, new CompoundTag()));
        fluidsTag.put("Bronze", moltenBronzeTank.writeToNBT(provider, new CompoundTag()));
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

        if (tag.contains("Fluids")) {
            CompoundTag fluidsTag = tag.getCompound("Fluids");
            moltenCopperTank.readFromNBT(provider, fluidsTag.getCompound("Copper"));
            moltenTinTank.readFromNBT(provider, fluidsTag.getCompound("Tin"));
            moltenBronzeTank.readFromNBT(provider, fluidsTag.getCompound("Bronze"));
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

        // ALLOYING TEMPORARILY DISABLED FOR TESTING
        // We'll re-enable this after melting is confirmed working

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
        if (recipeOpt.isPresent()) {
            PrimitiveAlloyRecipe recipe = recipeOpt.get().value();
            FluidStack outputFluid = recipe.getOutputFluid();

            // ADD NULL/EMPTY CHECK
            if (outputFluid.isEmpty()) {
                System.out.println("[ERROR] Recipe has empty fluid output: " + recipeOpt.get().id());
                return;
            }
        }
    }

    private FluidTank getTankForFluid(Fluid fluid) {
        if (fluid == null) return null;
        return fluidTankMap.get(fluid);
    }

    // Fluid tank getters
    public FluidTank getCopperTank() {
        return moltenCopperTank;
    }

    public FluidTank getTinTank() {
        return moltenTinTank;
    }

    public FluidTank getBronzeTank() {
        return moltenBronzeTank;
    }

    // Debug method to fill tanks
    public void debugFillTanks() {
        moltenCopperTank.fill(new FluidStack(ModFluids.getSourceFluid("molten_copper"), 500), IFluidHandler.FluidAction.EXECUTE);
        moltenTinTank.fill(new FluidStack(ModFluids.getSourceFluid("molten_tin"), 500), IFluidHandler.FluidAction.EXECUTE);
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