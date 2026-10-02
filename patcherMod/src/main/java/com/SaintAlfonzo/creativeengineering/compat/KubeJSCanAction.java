package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.food.CanServings;

import dev.latvian.mods.kubejs.recipe.ingredientaction.CustomIngredientAction;

/**
 * Registers the {@value #ID} KubeJS custom ingredient action: a can used in a KubeJS
 * shaped/shapeless recipe loses one serving instead of being used up
 * ({@link CanServings#craftRemainder}). Recipes opt in through global.NCH's {@code cans} argument.
 *
 * <p>KubeJS clears {@code CustomIngredientAction.MAP} at the start of every server script load,
 * so NCH calls {@link #register()} again on each reload; the call from common setup covers
 * clients of a dedicated server, which never run server scripts. Only load this class when
 * KubeJS is installed.
 */
public final class KubeJSCanAction {

    public static final String ID = "creativeengineering:can_serving";

    private KubeJSCanAction() {}

    public static void register() {
        CustomIngredientAction.MAP.put(ID, (stack, index, inventory) -> CanServings.craftRemainder(stack));
    }
}
