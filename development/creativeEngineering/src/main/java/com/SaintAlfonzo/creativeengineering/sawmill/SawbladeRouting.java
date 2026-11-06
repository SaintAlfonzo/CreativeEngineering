package com.SaintAlfonzo.creativeengineering.sawmill;

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import com.SaintAlfonzo.creativeengineering.sawmill.duck.SawmillRecipeExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Blade-aware recipe selection for the Immersive Engineering Sawmill.
 *
 * <p>Vanilla IE ({@code SawmillRecipe.findRecipe}) returns the <em>first</em> recipe whose
 * {@code input} ingredient matches the stack and stops — so when several recipes share one
 * input (e.g. {@code oak_planks -> stairs|door|slab|...}) only one is ever reachable, and
 * which one is non-deterministic recipe-manager map order.
 *
 * <p>Here the sawblade installed in the machine carries an NBT {@code {Profile:"<shape>"}}
 * string. Among the recipes that match the input we prefer the one whose {@code "blade"}
 * field equals that profile; failing that we fall back to the profile-less default
 * ({@code "blade"} absent / empty), which is what a plain {@code immersiveengineering:sawblade}
 * always gets. This also makes selection deterministic.
 *
 * @see com.SaintAlfonzo.creativeengineering.mixin.SawmillProcessMixin
 */
public final class SawbladeRouting {

    /** NBT key on the sawblade stack holding the profile string. */
    public static final String PROFILE_KEY = "Profile";

    private SawbladeRouting() {}

    /** The profile programmed onto a sawblade stack, or {@code ""} if none / not a programmed blade. */
    public static String profileOf(ItemStack blade) {
        if (blade == null || blade.isEmpty()) return "";
        CompoundTag tag = blade.getTag();
        if (tag == null) return "";
        return tag.getString(PROFILE_KEY);
    }

    /**
     * Pick the sawmill recipe for {@code input} given the installed blade's {@code profile}.
     *
     * <ol>
     *   <li>if {@code profile} is set and a matching recipe has {@code blade == profile} → that one;</li>
     *   <li>otherwise the first input-matching recipe with no blade profile (the default);</li>
     *   <li>otherwise {@code null} → IE passes the item through unchanged (no crash, no loss).</li>
     * </ol>
     */
    public static SawmillRecipe route(Level level, ItemStack input, String profile) {
        if (level == null || input == null || input.isEmpty()) return null;
        String wanted = profile == null ? "" : profile;

        SawmillRecipe fallback = null;
        for (SawmillRecipe recipe : SawmillRecipe.RECIPES.getRecipes(level)) {
            if (recipe == null || !recipe.input.test(input)) continue;

            String recipeBlade = bladeOf(recipe);
            if (!wanted.isEmpty() && wanted.equals(recipeBlade)) {
                return recipe;
            }
            if (recipeBlade.isEmpty() && fallback == null) {
                fallback = recipe;
            }
        }
        return fallback;
    }

    private static String bladeOf(SawmillRecipe recipe) {
        String blade = ((SawmillRecipeExt) recipe).creativeengineering$getBlade();
        return blade == null ? "" : blade;
    }
}
