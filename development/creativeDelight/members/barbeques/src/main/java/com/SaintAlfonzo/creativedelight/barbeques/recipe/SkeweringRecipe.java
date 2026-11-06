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
 * barbequesdelight:skewering (1.3.0 SimpleSkeweringRecipe): right-click an Ingredients Basin slot holding
 * {@code ingredientCount} of {@code ingredient} with {@code tool} (a stick) in the main hand and {@code sideCount} of
 * {@code side} in the off hand (nothing when sideCount is 0). JSON fields as 1.3.0, result in the 1.19.2 form.
 * Container slots: 0 tool, 1 basin stack, 2 off hand.
 */
public class SkeweringRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    public final Ingredient tool;
    public final Ingredient ingredient;
    public final int ingredientCount;
    public final Ingredient side;
    public final int sideCount;
    public final ItemStack output;

    public SkeweringRecipe(ResourceLocation id, Ingredient tool, Ingredient ingredient, int ingredientCount, Ingredient side,
                           int sideCount, ItemStack output) {
        this.id = id;
        this.tool = tool;
        this.ingredient = ingredient;
        this.ingredientCount = ingredientCount;
        this.side = side;
        this.sideCount = sideCount;
        this.output = output;
    }

    public boolean matches(ItemStack stick, ItemStack basin, ItemStack offhand) {
        return tool.test(stick) && ingredient.test(basin) && basin.getCount() >= ingredientCount
                && (sideCount == 0 || side.test(offhand) && offhand.getCount() >= sideCount);
    }

    /** Uses up the stick, the basin items and the side items; returns the skewer. */
    public ItemStack craft(ItemStack stick, ItemStack basin, ItemStack offhand) {
        stick.shrink(1);
        basin.shrink(ingredientCount);
        if (sideCount > 0) {
            offhand.shrink(sideCount);
        }
        return output.copy();
    }

    @Override
    public boolean matches(Container cont, Level level) {
        return matches(cont.getItem(0), cont.getItem(1), cont.getItem(2));
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
        return BBQRecipes.SKEWERING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BBQRecipes.SKEWERING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<SkeweringRecipe> {
        @Override
        public SkeweringRecipe fromJson(ResourceLocation id, JsonObject json) {
            int sideCount = GsonHelper.getAsInt(json, "sideCount", 0);
            Ingredient side = sideCount > 0 && json.has("side") ? Ingredient.fromJson(json.get("side")) : Ingredient.EMPTY;
            return new SkeweringRecipe(id, Ingredient.fromJson(json.get("tool")), Ingredient.fromJson(json.get("ingredient")),
                    GsonHelper.getAsInt(json, "ingredientCount", 1), side, sideCount,
                    CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true));
        }

        @Override
        public SkeweringRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient tool = Ingredient.fromNetwork(buf);
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            int count = buf.readVarInt();
            Ingredient side = Ingredient.fromNetwork(buf);
            int sideCount = buf.readVarInt();
            return new SkeweringRecipe(id, tool, ingredient, count, side, sideCount, buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SkeweringRecipe recipe) {
            recipe.tool.toNetwork(buf);
            recipe.ingredient.toNetwork(buf);
            buf.writeVarInt(recipe.ingredientCount);
            recipe.side.toNetwork(buf);
            buf.writeVarInt(recipe.sideCount);
            buf.writeItem(recipe.output);
        }
    }
}
