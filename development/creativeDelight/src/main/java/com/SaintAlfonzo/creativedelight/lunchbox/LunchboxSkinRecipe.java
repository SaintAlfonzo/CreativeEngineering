package com.SaintAlfonzo.creativedelight.lunchbox;

import java.util.List;
import java.util.Objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Smithing: lunchbox + a skin item gives the same box (contents, name and dye kept) wearing that item's look.
 * The skin item is used up. Allowed skins: a lower-tier, empty lunchbox, or a Fantasy Furniture berry basket
 * ({@link Lunchboxes#canTakeSkin}). Type {@code creativedelight:lunchbox_skin}; emitted by
 * {@code global.NCH.lunchboxSkin}. Soap takes the skin off again ({@link LunchboxSoapRecipe}).
 */
public class LunchboxSkinRecipe extends UpgradeRecipe {

    public LunchboxSkinRecipe(ResourceLocation id) {
        super(id, ingredient(Lunchboxes.TIERS), ingredient(Lunchboxes.SKINS), example());
    }

    private static Ingredient ingredient(List<ResourceLocation> ids) {
        return Ingredient.of(items(ids).stream().map(ItemStack::new));
    }

    private static List<Item> items(List<ResourceLocation> ids) {
        return ids.stream().filter(ForgeRegistries.ITEMS::containsKey).map(ForgeRegistries.ITEMS::getValue)
                .filter(Objects::nonNull).toList();
    }

    private static ItemStack example() {
        List<Item> boxes = items(Lunchboxes.TIERS);
        return boxes.isEmpty() ? ItemStack.EMPTY : new ItemStack(boxes.get(boxes.size() - 1));
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return Lunchboxes.canTakeSkin(inv.getItem(0), inv.getItem(1));
    }

    @Override
    public ItemStack assemble(Container inv) {
        ResourceLocation skin = ForgeRegistries.ITEMS.getKey(inv.getItem(1).getItem());
        return skin == null ? ItemStack.EMPTY : Lunchboxes.withSkin(inv.getItem(0), skin);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LunchboxRecipes.SKIN.get();
    }
}
