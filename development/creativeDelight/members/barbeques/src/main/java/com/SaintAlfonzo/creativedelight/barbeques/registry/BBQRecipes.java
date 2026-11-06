package com.SaintAlfonzo.creativedelight.barbeques.registry;

import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.barbeques.recipe.CombineItemRecipe;
import com.SaintAlfonzo.creativedelight.barbeques.recipe.GrillingRecipe;
import com.SaintAlfonzo.creativedelight.barbeques.recipe.SkeweringRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Recipe types grilling and skewering, serializers grilling, skewering and combine (ids as 1.3.0). */
public final class BBQRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BarbequesDelight.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, BarbequesDelight.MODID);

    public static final RegistryObject<RecipeType<GrillingRecipe>> GRILLING = TYPES.register("grilling",
            () -> RecipeType.simple(BarbequesDelight.loc("grilling")));
    public static final RegistryObject<RecipeType<SkeweringRecipe>> SKEWERING = TYPES.register("skewering",
            () -> RecipeType.simple(BarbequesDelight.loc("skewering")));

    public static final RegistryObject<RecipeSerializer<GrillingRecipe>> GRILLING_SERIALIZER =
            SERIALIZERS.register("grilling", GrillingRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<SkeweringRecipe>> SKEWERING_SERIALIZER =
            SERIALIZERS.register("skewering", SkeweringRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<CombineItemRecipe>> COMBINE_SERIALIZER =
            SERIALIZERS.register("combine", CombineItemRecipe.Serializer::new);

    private BBQRecipes() {
    }
}
