package com.SaintAlfonzo.creativeengineering.sawmill.duck;

/**
 * Duck interface mixed into
 * {@code blusunrize.immersiveengineering.common.blocks.metal.SawmillBlockEntity$SawmillProcess}.
 *
 * <p>{@code SawmillProcess} holds no reference to its owning block entity (it is a
 * {@code static} nested class), and its recipe lookup — {@code getRecipeDependentData} —
 * only receives a {@code Level}. So we stash the installed blade's profile onto the
 * process (server-side, at the first {@code processStep}), and the recipe id we resolved
 * from it, and persist both through {@code writeToNBT}/{@code readFromNBT} so the client
 * (which never runs {@code processStep}) resolves the identical recipe for its render.
 */
public interface SawmillProcessExt {

    /** Blade profile captured for this process, or {@code ""}. Never null. */
    String creativeengineering$getBladeProfile();

    void creativeengineering$setBladeProfile(String profile);

    /** Full id of the recipe the server routed this process to, or {@code ""}. Never null. */
    String creativeengineering$getResolvedRecipeId();

    void creativeengineering$setResolvedRecipeId(String id);
}
