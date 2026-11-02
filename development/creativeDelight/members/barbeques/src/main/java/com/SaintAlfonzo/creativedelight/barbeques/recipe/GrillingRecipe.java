package com.SaintAlfonzo.creativedelight.barbeques.recipe;

import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQRecipes;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * barbequesdelight:grilling (1.3.0 SimpleGrillingRecipe): one raw skewer on a Grill slot becomes {@code output} after
 * {@code barbecuingTime} heated ticks, flipped at half time. JSON as 1.3.0 with the 1.19.2 result form:
 * {@code {"ingredient": {...}, "output": {"item": ..., "count": 1}, "barbecuingTime": 320}}.
 */
public class GrillingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    public final Ingredient ingredient;
    public final ItemStack output;
    public final int barbecuingTime;

    public GrillingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack output, int barbecuingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.output = output;
        this.barbecuingTime = barbecuingTime;
    }

    public boolean matches(ItemStack stack) {
        return ingredient.test(stack);
    }

    @Override
    public boolean matches(Container cont, Level level) {
        return matches(cont.getItem(0));
    }

    @Override
    public ItemStack assemble(Container cont) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BBQRecipes.GRILLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BBQRecipes.GRILLING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<GrillingRecipe> {
        @Override
        public GrillingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new GrillingRecipe(id, Ingredient.fromJson(json.get("ingredient")),
                    CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true),
                    GsonHelper.getAsInt(json, "barbecuingTime"));
        }

        @Override
        public GrillingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new GrillingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, GrillingRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.output);
            buf.writeVarInt(recipe.barbecuingTime);
        }
    }
}
