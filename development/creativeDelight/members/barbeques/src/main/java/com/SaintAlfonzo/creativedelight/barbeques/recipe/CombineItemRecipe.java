package com.SaintAlfonzo.creativedelight.barbeques.recipe;

import com.SaintAlfonzo.creativedelight.barbeques.item.BBQSandwichItem;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQItems;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQRecipes;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * barbequesdelight:combine (1.3.0 CombineItemRecipe): a shapeless craft whose result stores every cooked skewer of the
 * grid, seasoning included, in its Skewers list. JSON is crafting_shapeless's. Sticks come back by the skewers' craft
 * remainder.
 */
public class CombineItemRecipe extends ShapelessRecipe {

    public CombineItemRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, result, ingredients);
    }

    @Override
    public ItemStack assemble(CraftingContainer cont) {
        ItemStack ans = super.assemble(cont);
        List<ItemStack> skewers = new ArrayList<>();
        for (int i = 0; i < cont.getContainerSize(); i++) {
            ItemStack s = cont.getItem(i);
            if (s.is(BBQItems.GRILLED_SKEWERS)) {
                ItemStack one = s.copy();
                one.setCount(1);
                skewers.add(one);
            }
        }
        BBQSandwichItem.setSkewers(ans, skewers);
        return ans;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BBQRecipes.COMBINE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<CombineItemRecipe> {
        private static CombineItemRecipe of(ShapelessRecipe base) {
            return new CombineItemRecipe(base.getId(), base.getGroup(), base.getResultItem(), base.getIngredients());
        }

        @Override
        public CombineItemRecipe fromJson(ResourceLocation id, JsonObject json) {
            return of(RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json));
        }

        @Override
        public CombineItemRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return of(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CombineItemRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
        }
    }
}
