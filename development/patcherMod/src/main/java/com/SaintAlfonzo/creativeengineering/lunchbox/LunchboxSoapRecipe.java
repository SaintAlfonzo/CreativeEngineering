package com.SaintAlfonzo.creativeengineering.lunchbox;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Crafting grid: one lunchbox + one Supplementaries soap. A dyed box loses its dye; an undyed box with a skin
 * loses the skin (back to its own look). Supplementaries' own soap recipe only cleans leather-style dyeables
 * and block colour sets, so it never matches a lunchbox. Type {@code creativeengineering:lunchbox_soap}.
 */
public class LunchboxSoapRecipe extends CustomRecipe {

    private static final ResourceLocation SOAP = new ResourceLocation("supplementaries", "soap");

    public LunchboxSoapRecipe(ResourceLocation id) {
        super(id);
    }

    private static boolean isSoap(ItemStack s) {
        Item soap = ForgeRegistries.ITEMS.getValue(SOAP);
        return soap != null && soap != Items.AIR && s.is(soap);
    }

    private static ItemStack box(CraftingContainer inv) {
        ItemStack box = ItemStack.EMPTY;
        boolean soap = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (Lunchboxes.isLunchbox(s) && box.isEmpty()) {
                box = s;
            } else if (isSoap(s) && !soap) {
                soap = true;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (!soap || box.isEmpty()) return ItemStack.EMPTY;
        return Lunchboxes.isDyed(box) || Lunchboxes.getSkin(box) != null ? box : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        return !box(inv).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack box = box(inv);
        if (box.isEmpty()) return ItemStack.EMPTY;
        return Lunchboxes.isDyed(box) ? Lunchboxes.withoutColor(box) : Lunchboxes.withoutSkin(box);
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LunchboxRecipes.SOAP.get();
    }
}
