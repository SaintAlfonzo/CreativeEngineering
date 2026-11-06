package com.SaintAlfonzo.creativedelight.lunchbox;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Crafting grid: one lunchbox + any number of dyes. Colours mix exactly like leather armour (vanilla
 * {@code DyeableLeatherItem.dyeArmor}, which only takes leather items, so it is repeated here). Only the base
 * is recoloured; the outline and latches keep theirs (the dyed models). Refused while the box wears a basket,
 * which has no dyeable base. Type {@code creativeengineering:lunchbox_dye}.
 */
public class LunchboxDyeRecipe extends CustomRecipe {

    public LunchboxDyeRecipe(ResourceLocation id) {
        super(id);
    }

    /** The box and the dyes in the grid, or null when the grid holds anything else. */
    private static List<ItemStack> parts(CraftingContainer inv) {
        List<ItemStack> parts = new ArrayList<>();
        ItemStack box = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (Lunchboxes.isLunchbox(s)) {
                if (!box.isEmpty()) return null;
                box = s;
            } else if (s.getItem() instanceof DyeItem) {
                parts.add(s);
            } else {
                return null;
            }
        }
        if (box.isEmpty() || parts.isEmpty() || !Lunchboxes.showsLunchboxLook(box)) return null;
        parts.add(0, box);
        return parts;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        return parts(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        List<ItemStack> parts = parts(inv);
        if (parts == null) return ItemStack.EMPTY;
        ItemStack box = parts.get(0);
        int[] sum = new int[3];
        int brightness = 0;
        int count = 0;
        int old = Lunchboxes.getColor(box);
        if (old >= 0) {
            int r = old >> 16 & 255, g = old >> 8 & 255, b = old & 255;
            brightness += Math.max(r, Math.max(g, b));
            sum[0] += r;
            sum[1] += g;
            sum[2] += b;
            count++;
        }
        for (int p = 1; p < parts.size(); p++) {
            float[] c = ((DyeItem) parts.get(p).getItem()).getDyeColor().getTextureDiffuseColors();
            int r = (int) (c[0] * 255.0F), g = (int) (c[1] * 255.0F), b = (int) (c[2] * 255.0F);
            brightness += Math.max(r, Math.max(g, b));
            sum[0] += r;
            sum[1] += g;
            sum[2] += b;
            count++;
        }
        int r = sum[0] / count, g = sum[1] / count, b = sum[2] / count;
        float avg = (float) brightness / (float) count;
        float max = (float) Math.max(r, Math.max(g, b));
        r = (int) ((float) r * avg / max);
        g = (int) ((float) g * avg / max);
        b = (int) ((float) b * avg / max);
        return Lunchboxes.withColor(box, (r << 16) + (g << 8) + b);
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LunchboxRecipes.DYE.get();
    }
}
