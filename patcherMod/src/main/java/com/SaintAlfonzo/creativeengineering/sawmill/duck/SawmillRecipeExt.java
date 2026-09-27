package com.SaintAlfonzo.creativeengineering.sawmill.duck;

/**
 * Duck interface mixed into {@code blusunrize.immersiveengineering.api.crafting.SawmillRecipe}
 * to carry the optional {@code "blade"} profile string parsed from the recipe JSON.
 *
 * <p>Empty string ({@code ""}) means "no profile" — the recipe is the profile-less
 * default for its input (what a plain {@code immersiveengineering:sawblade} produces).
 */
public interface SawmillRecipeExt {

    /** The blade profile this recipe requires, or {@code ""} for the profile-less default. Never null. */
    String creativeengineering$getBlade();

    void creativeengineering$setBlade(String blade);
}
