package com.SaintAlfonzo.creativeengineering.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.ingredientaction.CustomIngredientAction;
import dev.latvian.mods.kubejs.recipe.ingredientaction.CustomIngredientActionCallback;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KubeJS custom ingredient actions that any Creative jar provides (creative_split.md 7.2, Common API). A jar registers
 * its action once ({@link #register}, from its own setup); KubeJS empties {@code CustomIngredientAction.MAP} at the start
 * of every server script load, so NCH calls {@link #reapply} before it names an action in a recipe, and Common's setup
 * calls it for the clients of a dedicated server, which never run server scripts. Only load this class when KubeJS is
 * installed.
 */
public final class IngredientActions {
    private static final Map<String, CustomIngredientActionCallback> ACTIONS = new LinkedHashMap<>();

    private IngredientActions() {}

    public static synchronized void register(String id, CustomIngredientActionCallback action) {
        ACTIONS.put(id, action);
        CustomIngredientAction.MAP.put(id, action);
    }

    /** Puts every registered action back into KubeJS's map. */
    public static synchronized void reapply() {
        CustomIngredientAction.MAP.putAll(ACTIONS);
    }

    public static synchronized boolean has(String id) {
        return ACTIONS.containsKey(id);
    }
}
