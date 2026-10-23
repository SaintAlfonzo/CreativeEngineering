package com.SaintAlfonzo.creativeengineering.backpackskin;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Serializers for the two backpack skin recipes. Smithing keeps vanilla's shared smithing type. */
public final class BackpackSkinSerializers {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeEngineering.MODID);

    public static final RegistryObject<RecipeSerializer<?>> SMITHING =
            SERIALIZERS.register("backpack_skin", BackpackSkinRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<?>> CLEAR =
            SERIALIZERS.register("backpack_skin_clear", () -> new SimpleRecipeSerializer<>(BackpackSkinClearRecipe::new));

    private BackpackSkinSerializers() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }
}
