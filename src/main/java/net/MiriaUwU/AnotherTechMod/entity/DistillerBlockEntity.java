package net.MiriaUwU.AnotherTechMod.entity;

import net.MiriaUwU.AnotherTechMod.fluid.OilFluids;
import net.MiriaUwU.AnotherTechMod.item.ModItems;
import net.MiriaUwU.AnotherTechMod.screen.custom.DistillerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class DistillerBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int BYPRODUCT_OUTPUT_SLOT = 2;


    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 72;

    public DistillerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DISTILLER_BE.get(), pos, blockState);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> DistillerBlockEntity.this.progress;
                    case 1 -> DistillerBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> DistillerBlockEntity.this.progress = value;
                    case 1 -> DistillerBlockEntity.this.maxProgress = value;
                }

            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blick.yetanothertechmod.distiller");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DistillerMenu(i, inventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);


    }


    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("Distiller.progress", progress);
        pTag.putInt("Distiller.max_progress", maxProgress);

        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("Distiller.progress");
        maxProgress = pTag.getInt("Distiller.max_progress");
    }


    public void tick(Level level1, BlockPos blockPos, BlockState blockState) {
        if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(level, blockPos, blockState);

            if(hasCraftingFinished()) {
                craftItem();
                craftByproduct();
                resetprogress();
            }

        } else {
            resetprogress();
            
        }
    }

    private void craftByproduct() {
        ItemStack output = new ItemStack(ModItems.Tin.get(), 5);

        itemHandler.extractItem(INPUT_SLOT, 1, false);
        itemHandler.setStackInSlot(BYPRODUCT_OUTPUT_SLOT, new ItemStack(output.getItem(),
                itemHandler.getStackInSlot(BYPRODUCT_OUTPUT_SLOT).getCount() + output.getCount()));

    }

    private void craftItem() {
        ItemStack output = new ItemStack(ModItems.Brass.get(), 8);

        itemHandler.extractItem(INPUT_SLOT, 1, false);
        itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(output.getItem(),
            itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + output.getCount()));
    }


    private void resetprogress() {
        progress = 0;
        maxProgress = 72;
    }


    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }


    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        ItemStack output = new ItemStack(ModItems.Brass.get(), 8);
        ItemStack byproduct_output = new ItemStack(ModItems.Tin.get(), 5);
        return itemHandler.getStackInSlot(INPUT_SLOT).is(OilFluids.OIL_BUCKET) &&
                canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
        

    }

    private boolean canInsertItemIntoByProductOutputslot(ItemStack byproductOutput) {
        return itemHandler.getStackInSlot(BYPRODUCT_OUTPUT_SLOT).isEmpty() ||
                itemHandler.getStackInSlot(BYPRODUCT_OUTPUT_SLOT).getItem() == byproductOutput.getItem();
    }

    private boolean canInsertAmountIntoByProductOutputslot(int count) {
        int maxCount = itemHandler.getStackInSlot(BYPRODUCT_OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(BYPRODUCT_OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(BYPRODUCT_OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;

    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();
        // example max is 64 >= 62 + 4, = 66, this prevents that from happening
        return maxCount >= currentCount + count;

    }


    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


}
