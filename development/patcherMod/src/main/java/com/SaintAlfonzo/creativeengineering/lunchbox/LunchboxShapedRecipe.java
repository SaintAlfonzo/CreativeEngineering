package com.SaintAlfonzo.creativeengineering.lunchbox;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/**
 * A shaped tier upgrade that keeps the lunchbox: the result takes the grid box's name, dye, skin and contents.
 * A vanilla shaped recipe would hand back an empty box and delete what was inside. Also refuses a bundle that
 * still holds items. Type {@code creativeengineering:lunchbox_shaped} (plain shaped JSON); emitted by
 * {@code global.NCH.lunchboxShaped}.
 */
public class LunchboxShapedRecipe extends ShapedRecipe {

    public LunchboxShapedRecipe(ShapedRecipe shape) {
        super(shape.getId(), shape.getGroup(), shape.getWidth(), shape.getHeight(), shape.getIngredients(),
                shape.getResultItem());
    }

    private static ItemStack sourceBox(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (Lunchboxes.isLunchbox(s)) return s;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (!super.matches(inv, level)) return false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (Lunchboxes.isFilledBundle(inv.getItem(i))) return false;
        }
        ItemStack box = sourceBox(inv);
        return box.isEmpty() || Lunchboxes.contentsFit(box, getResultItem());
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack box = sourceBox(inv);
        return box.isEmpty() ? super.assemble(inv) : Lunchboxes.upgraded(box, getResultItem());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LunchboxRecipes.SHAPED.get();
    }

    public static final class Serializer implements RecipeSerializer<LunchboxShapedRecipe> {
        @Override
        public LunchboxShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new LunchboxShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
        }

        @Override
        public LunchboxShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new LunchboxShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LunchboxShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
        }
    }
}
