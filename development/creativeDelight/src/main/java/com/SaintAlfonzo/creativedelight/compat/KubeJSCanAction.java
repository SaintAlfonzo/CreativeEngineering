package com.SaintAlfonzo.creativedelight.compat;

import com.SaintAlfonzo.creativedelight.food.CanServings;

import com.SaintAlfonzo.creativecommon.compat.kubejs.IngredientActions;

/**
 * Registers the {@value #ID} KubeJS custom ingredient action: a can used in a KubeJS
 * shaped/shapeless recipe loses one serving instead of being used up
 * ({@link CanServings#craftRemainder}). Recipes opt in through global.NCH's {@code cans} argument.
 *
 * <p>Registered once from common setup into Common's {@link IngredientActions}, which NCH re-applies
 * on every server script load (KubeJS clears its action map then). Moves with the cans to
 * Creative Delight; NCH only knows the action id. Only load this class when KubeJS is installed.
 */
public final class KubeJSCanAction {

    public static final String ID = "creativedelight:can_serving";

    private KubeJSCanAction() {}

    /** Registers the action with Common's {@link IngredientActions} (which re-applies it on every script load). */
    public static void register() {
        IngredientActions.register(ID, (stack, index, inventory) -> CanServings.craftRemainder(stack));
    }
}
