package net.MiriaUwU.AnotherTechMod.recipes;

import net.MiriaUwU.AnotherTechMod.AnotherTechMod;
import net.MiriaUwU.AnotherTechMod.recipes.Alloys.PrimitiveAlloyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AnotherTechMod.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AnotherTechMod.MOD_ID);

    public static final ResourceLocation PRIMITIVE_ALLOYING_ID =
            ResourceLocation.fromNamespaceAndPath(AnotherTechMod.MOD_ID, "primitive_alloying");

    public static final DeferredHolder<RecipeType<?>, RecipeType<PrimitiveAlloyRecipe>> PRIMITIVE_ALLOYING_TYPE =
            RECIPE_TYPES.register("primitive_alloying", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return PRIMITIVE_ALLOYING_ID.toString();
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PrimitiveAlloyRecipe>> PRIMITIVE_ALLOYING_SERIALIZER =
            SERIALIZERS.register("primitive_alloying", PrimitiveAlloyRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }
}