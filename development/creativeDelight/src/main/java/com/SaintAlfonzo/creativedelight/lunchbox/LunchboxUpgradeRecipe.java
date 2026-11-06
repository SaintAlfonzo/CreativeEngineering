package com.SaintAlfonzo.creativedelight.lunchbox;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

/**
 * Smithing tier upgrade (iron -> netherite) that keeps the lunchbox's contents. Vanilla smithing copies the
 * base's tag but not its capabilities, which is where Spice of Life keeps the food. Armor Trims' smithing table
 * calls this the same way as a vanilla recipe, so its Netherite Upgrade template rule applies.
 * Type {@code creativeengineering:lunchbox_upgrade} (vanilla smithing JSON); emitted by
 * {@code global.NCH.lunchboxSmithing}.
 */
public class LunchboxUpgradeRecipe extends UpgradeRecipe {

    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public LunchboxUpgradeRecipe(ResourceLocation id, Ingredient base, Ingredient addition, ItemStack result) {
        super(id, base, addition, result);
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return super.matches(inv, level) && Lunchboxes.contentsFit(inv.getItem(0), result);
    }

    @Override
    public ItemStack assemble(Container inv) {
        return Lunchboxes.upgraded(inv.getItem(0), result);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LunchboxRecipes.UPGRADE.get();
    }

    public static final class Serializer implements RecipeSerializer<LunchboxUpgradeRecipe> {
        @Override
        public LunchboxUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new LunchboxUpgradeRecipe(id,
                    Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "base")),
                    Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition")),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")));
        }

        @Override
        public LunchboxUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new LunchboxUpgradeRecipe(id, Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf), buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LunchboxUpgradeRecipe recipe) {
            recipe.base.toNetwork(buf);
            recipe.addition.toNetwork(buf);
            buf.writeItem(recipe.result);
        }
    }
}
