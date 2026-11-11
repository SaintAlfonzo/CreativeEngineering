package com.SaintAlfonzo.creativeequipment.gear;

import com.SaintAlfonzo.creativeequipment.CreativeEquipment;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers the {@code creativeequipment:elytra_duplication} recipe serializer.
 * The recipe TYPE is left as vanilla's shared {@code minecraft:crafting} (the
 * {@code CraftingRecipe} interface's default {@code getType()}) - only the serializer
 * is ours, the same way vanilla's own {@code ArmorDyeRecipe}/{@code BookCloningRecipe}
 * etc. are just extra serializers on the ordinary crafting table/grid.
 */
public final class ElytraDuplicationSerializers {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeEquipment.NS);

    public static final RegistryObject<RecipeSerializer<?>> SERIALIZER =
            SERIALIZERS.register("elytra_duplication", Serializer::new);

    private ElytraDuplicationSerializers() {}

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }

    private static final class Serializer implements RecipeSerializer<ElytraDuplicationRecipe> {
        @Override
        public ElytraDuplicationRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient catalyst = CraftingHelper.getIngredient(json.get("catalyst"));
            ItemStack result = CraftingHelper.getItemStack(json.getAsJsonObject("result"), false);
            return new ElytraDuplicationRecipe(id, catalyst, result);
        }

        @Override
        public ElytraDuplicationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient catalyst = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            return new ElytraDuplicationRecipe(id, catalyst, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ElytraDuplicationRecipe recipe) {
            recipe.getCatalyst().toNetwork(buf);
            buf.writeItem(recipe.getResultItem());
        }
    }
}
