package net.MiriaUwU.AnotherTechMod.recipes.Alloys;

import net.MiriaUwU.AnotherTechMod.recipes.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;



public record PrimitiveAlloyRecipe(
        Ingredient input,
        ResourceLocation outputFluidId,
        int outputAmount,
        int processingTime
) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput container, Level level) {
        // We only care about the first item since we're processing slot by slot
        return input.test(container.getItem(0));
    }


    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    public FluidStack getOutputFluid() {
        Fluid fluid = BuiltInRegistries.FLUID.get(outputFluidId);
        if (fluid == null) {
            throw new IllegalStateException("Unknown fluid: " + outputFluidId);
        }
        return new FluidStack(fluid, outputAmount);
    }

    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRIMITIVE_ALLOYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PRIMITIVE_ALLOYING_TYPE.get();
    }

    // ========== SERIALIZER ==========
    public static class Serializer implements RecipeSerializer<PrimitiveAlloyRecipe> {
        public static final MapCodec<PrimitiveAlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(PrimitiveAlloyRecipe::input),
                        ResourceLocation.CODEC.fieldOf("fluid").forGetter(PrimitiveAlloyRecipe::outputFluidId),
                        ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(PrimitiveAlloyRecipe::outputAmount),
                        ExtraCodecs.POSITIVE_INT.fieldOf("processingTime").forGetter(PrimitiveAlloyRecipe::processingTime)
                ).apply(instance, PrimitiveAlloyRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, PrimitiveAlloyRecipe> STREAM_CODEC =
                StreamCodec.of(
                        Serializer::toNetwork,
                        Serializer::fromNetwork
                );

        @Override
        public MapCodec<PrimitiveAlloyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PrimitiveAlloyRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, PrimitiveAlloyRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input());
            buffer.writeResourceLocation(recipe.outputFluidId());
            buffer.writeVarInt(recipe.outputAmount());
            buffer.writeVarInt(recipe.processingTime());
        }

        private static PrimitiveAlloyRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ResourceLocation fluidId = buffer.readResourceLocation();
            int amount = buffer.readVarInt();
            int time = buffer.readVarInt();
            return new PrimitiveAlloyRecipe(input, fluidId, amount, time);
        }
    }
}