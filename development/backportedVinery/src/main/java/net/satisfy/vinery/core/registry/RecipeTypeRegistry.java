package net.satisfy.vinery.core.registry;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.recipe.ApplePressFermentingRecipe;
import net.satisfy.vinery.core.recipe.ApplePressMashingRecipe;
import net.satisfy.vinery.core.recipe.FermentationBarrelRecipe;

import java.util.function.Supplier;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Vinery.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Vinery.MOD_ID);

    public static final RegistryObject<RecipeType<FermentationBarrelRecipe>> FERMENTATION_BARREL_RECIPE_TYPE = create("wine_fermentation");
    public static final RegistryObject<RecipeSerializer<FermentationBarrelRecipe>> FERMENTATION_BARREL_RECIPE_SERIALIZER = create("wine_fermentation", FermentationBarrelRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<ApplePressMashingRecipe>> APPLE_PRESS_MASHING_RECIPE_TYPE = create("apple_mashing");
    public static final RegistryObject<RecipeSerializer<ApplePressMashingRecipe>> APPLE_PRESS_MASHING_RECIPE_SERIALIZER = create("apple_mashing", ApplePressMashingRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<ApplePressFermentingRecipe>> APPLE_PRESS_FERMENTING_RECIPE_TYPE = create("apple_fermenting");
    public static final RegistryObject<RecipeSerializer<ApplePressFermentingRecipe>> APPLE_PRESS_FERMENTING_RECIPE_SERIALIZER = create("apple_fermenting", ApplePressFermentingRecipe.Serializer::new);

    private static <T extends Recipe<?>> RegistryObject<RecipeSerializer<T>> create(String name, Supplier<RecipeSerializer<T>> serializer) {
        return RECIPE_SERIALIZERS.register(name, serializer);
    }

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> create(String name) {
        return RECIPE_TYPES.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return Vinery.MOD_ID + ":" + name;
            }
        });
    }
}
