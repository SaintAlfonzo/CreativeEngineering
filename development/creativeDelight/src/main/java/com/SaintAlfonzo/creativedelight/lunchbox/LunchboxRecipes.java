package com.SaintAlfonzo.creativedelight.lunchbox;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Serializers for the lunchbox recipes. Smithing ones keep vanilla's shared smithing type, crafting ones crafting. */
public final class LunchboxRecipes {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeDelight.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> SHAPED =
            SERIALIZERS.register("lunchbox_shaped", LunchboxShapedRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<?>> UPGRADE =
            SERIALIZERS.register("lunchbox_upgrade", LunchboxUpgradeRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<?>> SKIN =
            SERIALIZERS.register("lunchbox_skin", () -> new SimpleRecipeSerializer<>(LunchboxSkinRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> DYE =
            SERIALIZERS.register("lunchbox_dye", () -> new SimpleRecipeSerializer<>(LunchboxDyeRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> SOAP =
            SERIALIZERS.register("lunchbox_soap", () -> new SimpleRecipeSerializer<>(LunchboxSoapRecipe::new));

    private LunchboxRecipes() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
