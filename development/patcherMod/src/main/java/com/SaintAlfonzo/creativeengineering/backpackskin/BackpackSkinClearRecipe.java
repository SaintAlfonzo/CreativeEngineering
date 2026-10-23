package com.SaintAlfonzo.creativeengineering.backpackskin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Crafting grid: a skinned backpack alone gives the same backpack without its skin (the skin block is not returned). */
public class BackpackSkinClearRecipe extends CustomRecipe {

    public BackpackSkinClearRecipe(ResourceLocation id) {
        super(id);
    }

    private static ItemStack onlyStack(CraftingContainer inv) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (!found.isEmpty()) return ItemStack.EMPTY;
            found = s;
        }
        return found;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack s = onlyStack(inv);
        return !s.isEmpty() && BackpackSkins.isBackpack(s) && BackpackSkins.getSkin(s) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        return BackpackSkins.withoutSkin(onlyStack(inv));
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BackpackSkinSerializers.CLEAR.get();
    }
}
