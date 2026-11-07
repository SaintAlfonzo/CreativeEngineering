package com.SaintAlfonzo.creativecompat.portalgun;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Dyes the Portal Gun's two portal colours (feature_writeup/plan_portal_gun.md 4), with Sophisticated Backpacks' column
 * rule: any grid, one gun plus dyes anywhere. A dye in a column left of the gun or in its column colours the blue portal
 * (left click); right of it or in its column, the orange portal (right click). Several dyes on one side average like
 * leather armour; the side's current colour joins the average only when it already has a custom colour.
 *
 * <p>Writes the gun's own colour data, stack NBT {@code CustomPortalColors {blue:int, orange:int}} (the data the
 * {@code /customportalcolors} command sets; {@code me.Thelnfamous1.portalgun.ColoredPortalGun}), so the gun names no
 * Portal Gun class. All other NBT is kept. No washing (decision D4).
 */
public class PortalGunDyeRecipe extends CustomRecipe {

    public static final ResourceLocation GUN_ID = new ResourceLocation("portalgun", "portal_gun");
    public static final String COLORS_TAG = "CustomPortalColors";
    public static final String BLUE = "blue";
    public static final String ORANGE = "orange";

    public PortalGunDyeRecipe(ResourceLocation id) {
        super(id);
    }

    /** The Portal Gun item, or null when the mod is not loaded. */
    @Nullable
    public static Item gun() {
        Item item = ForgeRegistries.ITEMS.getValue(GUN_ID);
        return item == null || item == Items.AIR ? null : item;
    }

    @Nullable
    private static DyeColor dye(ItemStack stack) {
        return stack.is(Tags.Items.DYES) ? DyeColor.getColor(stack) : null;
    }

    /** The grid read once: the gun's slot and the dyes per side, or null when the grid is not a valid dyeing. */
    private record Layout(int gunSlot, List<DyeColor> blue, List<DyeColor> orange) {}

    @Nullable
    private static Layout layout(CraftingContainer inv) {
        Item gun = gun();
        if (gun == null) return null;
        int width = inv.getWidth();
        int gunSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(gun)) {
                if (gunSlot >= 0) return null;
                gunSlot = i;
            } else if (dye(stack) == null) {
                return null;
            }
        }
        if (gunSlot < 0) return null;
        int gunColumn = gunSlot % width;
        List<DyeColor> blue = new ArrayList<>();
        List<DyeColor> orange = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (i == gunSlot) continue;
            DyeColor color = dye(inv.getItem(i));
            if (color == null) continue;
            int column = i % width;
            if (column <= gunColumn) blue.add(color);
            if (column >= gunColumn) orange.add(color);
        }
        if (blue.isEmpty() && orange.isEmpty()) return null;
        return new Layout(gunSlot, blue, orange);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        return layout(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        Layout layout = layout(inv);
        if (layout == null) return ItemStack.EMPTY;
        ItemStack result = inv.getItem(layout.gunSlot()).copy();
        result.setCount(1);
        dyeSide(result, BLUE, layout.blue());
        dyeSide(result, ORANGE, layout.orange());
        return result;
    }

    /** Applies the dyes to one side of a gun stack; nothing when the list is empty. Used by the JEI examples too. */
    public static void dyeSide(ItemStack gun, String side, List<DyeColor> dyes) {
        if (dyes.isEmpty()) return;
        CompoundTag colors = gun.getTagElement(COLORS_TAG);
        Integer current = colors != null && colors.contains(side, Tag.TAG_INT) ? colors.getInt(side) : null;
        gun.getOrCreateTagElement(COLORS_TAG).putInt(side, mix(current, dyes));
    }

    /** Leather-armour averaging (DyeableLeatherItem.dyeArmor) of the dyes, plus the current colour when there is one. */
    static int mix(@Nullable Integer current, List<DyeColor> dyes) {
        int r = 0, g = 0, b = 0, maxSum = 0, count = 0;
        if (current != null) {
            int cr = current >> 16 & 255, cg = current >> 8 & 255, cb = current & 255;
            r += cr; g += cg; b += cb;
            maxSum += Math.max(cr, Math.max(cg, cb));
            count++;
        }
        for (DyeColor dye : dyes) {
            float[] c = dye.getTextureDiffuseColors();
            int dr = (int) (c[0] * 255.0F), dg = (int) (c[1] * 255.0F), db = (int) (c[2] * 255.0F);
            r += dr; g += dg; b += db;
            maxSum += Math.max(dr, Math.max(dg, db));
            count++;
        }
        int ar = r / count, ag = g / count, ab = b / count;
        float avgMax = (float) maxSum / count;
        float maxOfAvg = Math.max(ar, Math.max(ag, ab));
        if (maxOfAvg > 0) {
            ar = (int) (ar * avgMax / maxOfAvg);
            ag = (int) (ag * avgMax / maxOfAvg);
            ab = (int) (ab * avgMax / maxOfAvg);
        }
        return (ar << 16) + (ag << 8) + ab;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PortalGunRecipes.DYE.get();
    }
}
