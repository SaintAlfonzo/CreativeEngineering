package com.SaintAlfonzo.solraspberrypie.recipe;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * A shapeless recipe with an excluded tag: book + any food that is not a drink makes the Diet Log, while book + a
 * drink makes the Liquid Diet Log (7); carrot is excluded too (Carrot Edition's Food Book is book + carrot). A plain
 * shapeless subclass, so JEI shows it with the normal crafting recipes.
 * JSON: the shapeless fields plus "exclude" (an item tag no ingredient may be in).
 */
public class DietBookRecipe extends ShapelessRecipe {
    private final TagKey<Item> exclude;

    public DietBookRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> ingredients, TagKey<Item> exclude) {
        super(id, group, result, ingredients);
        this.exclude = exclude;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (!super.matches(inv, level)) return false;
        if (exclude == null) return true;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(exclude)) return false;
        }
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SolRaspberryPie.DIET_BOOK.get();
    }

    public static class Serializer implements RecipeSerializer<DietBookRecipe> {
        @Override
        public DietBookRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            JsonArray arr = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < arr.size(); i++) ingredients.add(Ingredient.fromJson(arr.get(i)));
            ItemStack result = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            TagKey<Item> exclude = json.has("exclude") ? ItemTags.create(new ResourceLocation(GsonHelper.getAsString(json, "exclude"))) : null;
            return new DietBookRecipe(id, group, result, ingredients, exclude);
        }

        @Override
        public DietBookRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            int n = buf.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(n, Ingredient.EMPTY);
            for (int i = 0; i < n; i++) ingredients.set(i, Ingredient.fromNetwork(buf));
            ItemStack result = buf.readItem();
            TagKey<Item> exclude = buf.readBoolean() ? ItemTags.create(buf.readResourceLocation()) : null;
            return new DietBookRecipe(id, group, result, ingredients, exclude);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DietBookRecipe r) {
            buf.writeUtf(r.getGroup());
            buf.writeVarInt(r.getIngredients().size());
            for (Ingredient ing : r.getIngredients()) ing.toNetwork(buf);
            buf.writeItem(r.getResultItem());
            buf.writeBoolean(r.exclude != null);
            if (r.exclude != null) buf.writeResourceLocation(r.exclude.location());
        }
    }
}
