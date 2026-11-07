package com.SaintAlfonzo.creativecompat.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Who added or removed which ingredients after JEI built its list ({@code addIngredientsAtRuntime} /
 * {@code removeIngredientsAtRuntime}), for the visibility classes in the {@link PackIngredientOrder} report:
 * a removal by KubeJS's plugin ({@code kubejs:jei}, i.e. {@code JEIEvents.hide*}) is the pack's own hide, any
 * other plugin's is a mod's.
 *
 * <p>{@code mixin.JeiPluginCallerMixin} sets the running plugin's uid around each plugin call, and
 * {@code mixin.JeiIngredientManagerMixin} records each change with it. Cleared at every JEI start.
 */
public final class JeiIngredientChanges {

    public static final String KUBEJS_PLUGIN = "kubejs:jei";

    private static final ThreadLocal<String> CURRENT_PLUGIN = new ThreadLocal<>();
    /** type key -> ingredient id -> plugin uids. */
    private static final Map<String, Map<ResourceLocation, Set<String>>> ADDED = new HashMap<>();
    private static final Map<String, Map<ResourceLocation, Set<String>>> REMOVED = new HashMap<>();

    private JeiIngredientChanges() {}

    /** Runs a plugin call with that plugin's uid as the current one (restores the outer one after). */
    public static void runAs(String pluginUid, Runnable call) {
        String outer = CURRENT_PLUGIN.get();
        CURRENT_PLUGIN.set(pluginUid);
        try {
            call.run();
        } finally {
            CURRENT_PLUGIN.set(outer);
        }
    }

    /** The plugin JEI is calling right now, or "unknown" for a change made outside a plugin call. */
    public static String currentPlugin() {
        String uid = CURRENT_PLUGIN.get();
        return uid != null ? uid : "unknown";
    }

    public static synchronized void clear() {
        ADDED.clear();
        REMOVED.clear();
    }

    public static synchronized <V> void record(boolean added, IIngredientManager manager, IIngredientType<V> type,
                                               Collection<V> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) return;
        IIngredientHelper<V> helper = manager.getIngredientHelper(type);
        String typeKey = PackIngredientOrder.typeKey(type);
        String uid = currentPlugin();
        Map<ResourceLocation, Set<String>> byId = (added ? ADDED : REMOVED).computeIfAbsent(typeKey, k -> new HashMap<>());
        for (V ingredient : ingredients) {
            ResourceLocation id = PackIngredientOrder.safeId(helper, ingredient);
            if (id != null) byId.computeIfAbsent(id, k -> new TreeSet<>()).add(uid);
        }
    }

    @Nullable
    public static synchronized Set<String> addedBy(String typeKey, ResourceLocation id) {
        Map<ResourceLocation, Set<String>> byId = ADDED.get(typeKey);
        return byId == null ? null : byId.get(id);
    }

    @Nullable
    public static synchronized Set<String> removedBy(String typeKey, ResourceLocation id) {
        Map<ResourceLocation, Set<String>> byId = REMOVED.get(typeKey);
        return byId == null ? null : byId.get(id);
    }
}
