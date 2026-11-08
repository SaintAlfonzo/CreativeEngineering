package com.SaintAlfonzo.creativecompat.portalgun;

import com.SaintAlfonzo.creativecompat.compat.jei.JeiGate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * JEI examples for the Portal Gun dye recipe (feature_writeup/plan_portal_gun.md 4), as Sophisticated Backpacks shows
 * its backpack dyeing: the recipe is special, so JEI draws nothing for it. One page per dye for each side (dye left of
 * the gun: blue portal; right: orange portal), plus a two-sided page and a same-column page. Nothing without the gun.
 */
@ParametersAreNonnullByDefault
@JeiPlugin
public class PortalGunDyeJEI implements IModPlugin {

    private static final ResourceLocation ID = new ResourceLocation(CreativeCompat.MOD_ID, "portal_gun_dye");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        JeiGate.run("registerRecipes", () -> registerRecipes0(registration));
    }

    private void registerRecipes0(IRecipeRegistration registration) {
        Item gun = PortalGunDyeRecipe.gun();
        if (gun == null) return;
        List<CraftingRecipe> pages = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            pages.add(page(gun, "blue_" + color.getName(), 2, 1, List.of(color), List.of()));
        }
        for (DyeColor color : DyeColor.values()) {
            pages.add(page(gun, "orange_" + color.getName(), 2, 1, List.of(), List.of(color)));
        }
        pages.add(page(gun, "both_sides", 3, 1, List.of(DyeColor.LIGHT_BLUE), List.of(DyeColor.RED)));
        pages.add(page(gun, "same_column", 1, 2, List.of(DyeColor.LIME), List.of(DyeColor.LIME)));
        registration.addRecipes(RecipeTypes.CRAFTING, pages);
    }

    /**
     * One example grid: width 2 puts the dye left (blue) or right (orange) of the gun, width 3 one dye each side, a
     * 1x2 grid the dye under the gun (same column, both sides).
     */
    private static CraftingRecipe page(Item gun, String name, int width, int height, List<DyeColor> blue,
                                       List<DyeColor> orange) {
        Ingredient gunIn = Ingredient.of(gun);
        NonNullList<Ingredient> grid = NonNullList.withSize(width * height, Ingredient.EMPTY);
        if (height == 2) {
            grid.set(0, gunIn);
            grid.set(1, Ingredient.of(DyeItem.byColor(blue.get(0))));
        } else if (width == 3) {
            grid.set(0, Ingredient.of(DyeItem.byColor(blue.get(0))));
            grid.set(1, gunIn);
            grid.set(2, Ingredient.of(DyeItem.byColor(orange.get(0))));
        } else if (!blue.isEmpty()) {
            grid.set(0, Ingredient.of(DyeItem.byColor(blue.get(0))));
            grid.set(1, gunIn);
        } else {
            grid.set(0, gunIn);
            grid.set(1, Ingredient.of(DyeItem.byColor(orange.get(0))));
        }
        ItemStack result = new ItemStack(gun);
        PortalGunDyeRecipe.dyeSide(result, PortalGunDyeRecipe.BLUE, blue);
        PortalGunDyeRecipe.dyeSide(result, PortalGunDyeRecipe.ORANGE, orange);
        ResourceLocation id = new ResourceLocation(CreativeCompat.MOD_ID, "jei/portal_gun_dye/" + name);
        return new ShapedRecipe(id, "creativecompat.portal_gun_dye", width, height, grid, result);
    }
}
