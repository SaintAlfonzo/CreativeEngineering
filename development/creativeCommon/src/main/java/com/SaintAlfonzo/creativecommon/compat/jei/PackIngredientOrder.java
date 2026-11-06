package com.SaintAlfonzo.creativecommon.compat.jei;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecommon.mixin.JeiIngredientFilterAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IngredientFilter;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The pack's own order for JEI's ingredient list (feature_writeup/jei_ordering.md). JEI 11.8.1 sorts its list
 * once, writes each element's position into {@code IListElement.setSortedIndex}, and from then on orders the list
 * (and every search result) by that index. This class rewrites those indices from the pack order, so JEI's own
 * comparator carries it everywhere; nothing else in JEI changes.
 *
 * <p>Inputs:
 * <ul>
 *   <li>the ordering definition, {@code global.jeiOrder} from kubejs/startup_scripts/jei/order.js, handed over as
 *       JSON by kubejs/assets/creativecommon/kubejs/_lib/jei_order.js through {@link #define};</li>
 *   <li>the {@code creativecommon:jei/<path>} item, fluid and mob_effect tags (made from the same definition
 *       by kubejs/server_scripts/tags/jei_order_tags.js), read from the client's synced registries. An
 *       ingredient's deepest jei tag is its place; a node's {@code list} places ids that carry no jei tag (that is
 *       how multiblocks, which have no registry, get placed).</li>
 * </ul>
 *
 * <p>Order: ingredient type (definition {@code types}), then placed before unplaced, then the path (defined nodes in
 * definition order, undefined path segments after them by name; a node's own members before its children's), then
 * the leaf's sort chain ({@code list}, {@code mod}, {@code name}, {@code creative}), then JEI's own order. Unplaced
 * ingredients keep JEI's own order (its {@code IngredientSortStages}), after every placed one of their type.
 *
 * <p>Sorted (by {@code mixin.JeiStarterMixin} / {@code JeiIngredientFilterMixin}) when JEI has finished starting,
 * which is after KubeJS's runtime adds and hides, and again when a mod adds ingredients later; JEI's own sort runs
 * first in both cases and stays as the fallback order. Off: {@code CommonConfig [jei] packOrder}.
 */
public final class PackIngredientOrder {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG_NAMESPACE = "creativecommon";
    public static final String TAG_PREFIX = "jei/";
    private static final List<String> DEFAULT_SORT = List.of("mod", "creative");
    private static final Object[] NO_LEAF = new Object[0];

    @Nullable private static String definitionJson;
    private static Definition definition = Definition.EMPTY;

    @Nullable private static IngredientFilter filter;
    /** JEI's own position of each element before the pack sort (the fallback order). */
    private static final Map<IListElement<?>, Integer> jeiIndex = new IdentityHashMap<>();
    private static final Map<IListElement<?>, Key> keys = new IdentityHashMap<>();
    @Nullable private static Placements placements;
    private static boolean starting;
    /** The filter was built during the current start (JEI bails out early when there is no level). */
    private static boolean filterFromThisStart;

    private PackIngredientOrder() {}

    // ------------------------------------------------------------------------------------------------
    // Entry points (KubeJS client script, mixins)
    // ------------------------------------------------------------------------------------------------

    /**
     * Called by kubejs/assets/creativecommon/kubejs/_lib/jei_order.js with {@code JSON.stringify(global.jeiOrder.definition())}.
     * Re-sorts right away when JEI is already running (client script reload).
     */
    public static synchronized void define(String json) {
        try {
            definition = Definition.parse(json);
            definitionJson = json;
            LOGGER.info("[jei order] definition: {} types, {} nodes", definition.types.size(), definition.nodeCount());
        } catch (RuntimeException e) {
            LOGGER.error("[jei order] bad definition from kubejs, using an empty one", e);
            definition = Definition.EMPTY;
            definitionJson = null;
        }
        invalidate();
        if (filter != null && !starting) resort(filter);
    }

    public static synchronized void onJeiStarting() {
        starting = true;
        filterFromThisStart = false;
        JeiIngredientChanges.clear();
        invalidate();
    }

    public static synchronized void onFilterCreated(IngredientFilter created) {
        filter = created;
        filterFromThisStart = true;
        jeiIndex.clear();
        keys.clear();
        for (IListElement<?> element : allElements(created)) {
            jeiIndex.put(element, element.getSortedIndex());
        }
    }

    public static synchronized void onJeiStarted() {
        starting = false;
        if (filter == null || !filterFromThisStart) return;
        resort(filter);
        if (CommonConfig.JEI.orderReport.get()) {
            try {
                PackOrderReport.write(filter, accessor(filter).creativecommon$getIngredientManager());
            } catch (RuntimeException e) {
                LOGGER.error("[jei order] report failed", e);
            }
        }
    }

    /** JEI stopped (logout, or a restart on /reload): drop the old list. */
    public static synchronized void onJeiStopped() {
        filter = null;
        jeiIndex.clear();
        invalidate();
    }

    public static synchronized void onIngredientsAdded(IngredientFilter changed) {
        if (!starting && changed == filter) resort(changed);
    }

    // ------------------------------------------------------------------------------------------------
    // Sorting
    // ------------------------------------------------------------------------------------------------

    private static void invalidate() {
        placements = null;
        keys.clear();
    }

    private static void resort(IngredientFilter target) {
        if (!CommonConfig.JEI.packOrder.get()) return;
        long start = System.nanoTime();
        try {
            IIngredientManager manager = accessor(target).creativecommon$getIngredientManager();
            Placements placed = placements();
            List<IListElement<?>> elements = new ArrayList<>(allElements(target));
            for (IListElement<?> element : elements) {
                keys.computeIfAbsent(element, e -> keyOf(e, manager, placed));
            }
            elements.sort((a, b) -> keys.get(a).compareTo(keys.get(b)));
            for (int i = 0; i < elements.size(); i++) {
                elements.get(i).setSortedIndex(i);
            }
            target.invalidateCache();
            accessor(target).creativecommon$notifyListenersOfChange();
            LOGGER.info("[jei order] sorted {} ingredients in {} ms", elements.size(), (System.nanoTime() - start) / 1_000_000);
        } catch (RuntimeException e) {
            LOGGER.error("[jei order] sort failed, JEI keeps its own order", e);
        }
    }

    static Collection<IListElement<?>> allElements(IngredientFilter target) {
        return accessor(target).creativecommon$getElementSearch().getAllIngredients();
    }

    static JeiIngredientFilterAccessor accessor(IngredientFilter target) {
        return (JeiIngredientFilterAccessor) (Object) target;
    }

    static synchronized Placements placements() {
        if (placements == null) placements = Placements.build(definition);
        return placements;
    }

    static Definition definition() {
        return definition;
    }

    static boolean hasDefinition() {
        return definitionJson != null;
    }

    private static <V> Key keyOf(IListElement<V> element, IIngredientManager manager, Placements placed) {
        ITypedIngredient<V> typed = element.getTypedIngredient();
        IIngredientType<V> type = typed.getType();
        V ingredient = typed.getIngredient();
        IIngredientHelper<V> helper = manager.getIngredientHelper(type);
        String typeKey = typeKey(type);
        int typeRank = definition.typeRank(typeKey);
        Integer jei = jeiIndex.get(element);
        long fallback = jei != null ? jei : (long) Integer.MAX_VALUE + element.getCreatedIndex();

        ResourceLocation id = safeId(helper, ingredient);
        Placement placement = id == null ? null : placed.get(typeKey, id);
        if (placement == null) return new Key(typeRank, null, null, NO_LEAF, fallback);

        Node node = placement.node;
        List<String> sort = node != null && node.sort != null ? node.sort : DEFAULT_SORT;
        Object[] leaf = new Object[sort.size() * 2];
        int n = 0;
        for (String mode : sort) {
            switch (mode) {
                case "list" -> {
                    Integer at = node != null ? node.listIndex.get(id) : null;
                    leaf[n++] = at != null ? at : Integer.MAX_VALUE;
                }
                case "mod" -> {
                    String mod = safeModId(helper, ingredient, id);
                    List<String> modOrder = node != null && node.modOrder != null ? node.modOrder : definition.modOrder;
                    int at = modOrder.indexOf(mod);
                    leaf[n++] = at >= 0 ? at : Integer.MAX_VALUE;
                    leaf[n++] = mod;
                }
                case "name" -> leaf[n++] = safeName(helper, ingredient);
                case "creative" -> leaf[n++] = element.getCreatedIndex();
                default -> { }
            }
        }
        return new Key(typeRank, placement.ranks, placement.names, Arrays.copyOf(leaf, n), fallback);
    }

    // ------------------------------------------------------------------------------------------------
    // Ingredient identity
    // ------------------------------------------------------------------------------------------------

    /** "item", "fluid", "effect", "multiblock", or the ingredient class's simple name for any other type. */
    public static String typeKey(IIngredientType<?> type) {
        if (type == VanillaTypes.ITEM_STACK) return "item";
        if (type == ForgeTypes.FLUID_STACK) return "fluid";
        Class<?> cls = type.getIngredientClass();
        if (MobEffectInstance.class.isAssignableFrom(cls)) return "effect";
        // Just Enough Immersive Multiblocks: IE's MultiblockHandler.IMultiblock (no compile-time IE dependency)
        if (cls.getName().endsWith("MultiblockHandler$IMultiblock")) return "multiblock";
        return cls.getSimpleName().toLowerCase(Locale.ROOT);
    }

    @Nullable
    public static <V> ResourceLocation safeId(IIngredientHelper<V> helper, V ingredient) {
        try {
            return helper.getResourceLocation(ingredient);
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    static <V> String safeModId(IIngredientHelper<V> helper, V ingredient, ResourceLocation id) {
        try {
            return helper.getDisplayModId(ingredient);
        } catch (RuntimeException | LinkageError e) {
            return id.getNamespace();
        }
    }

    static <V> String safeName(IIngredientHelper<V> helper, V ingredient) {
        try {
            return helper.getDisplayName(ingredient).toLowerCase(Locale.ROOT);
        } catch (RuntimeException | LinkageError e) {
            return "";
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Sort key
    // ------------------------------------------------------------------------------------------------

    private record Key(int typeRank, @Nullable int[] ranks, @Nullable String[] names, Object[] leaf, long fallback)
            implements Comparable<Key> {

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int compareTo(Key o) {
            int c = Integer.compare(typeRank, o.typeRank);
            if (c != 0) return c;
            if (ranks == null || o.ranks == null) {
                if (ranks != o.ranks) return ranks == null ? 1 : -1; // placed first
                return Long.compare(fallback, o.fallback);
            }
            int len = Math.min(ranks.length, o.ranks.length);
            for (int i = 0; i < len; i++) {
                c = Integer.compare(ranks[i], o.ranks[i]);
                if (c != 0) return c;
                c = names[i].compareTo(o.names[i]);
                if (c != 0) return c;
            }
            c = Integer.compare(ranks.length, o.ranks.length); // a node's own members before its children's
            if (c != 0) return c;
            int leafLen = Math.min(leaf.length, o.leaf.length);
            for (int i = 0; i < leafLen; i++) {
                c = ((Comparable) leaf[i]).compareTo(o.leaf[i]);
                if (c != 0) return c;
            }
            return Long.compare(fallback, o.fallback);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Definition
    // ------------------------------------------------------------------------------------------------

    /** One node of a type's category tree. The root of each type is a node with an empty path. */
    static final class Node {
        final String type;
        final List<String> path;
        @Nullable final List<String> sort;
        @Nullable final List<String> modOrder;
        final Map<ResourceLocation, Integer> listIndex = new LinkedHashMap<>();
        final Map<String, Node> children = new LinkedHashMap<>();

        Node(String type, List<String> path, @Nullable List<String> sort, @Nullable List<String> modOrder) {
            this.type = type;
            this.path = path;
            this.sort = sort;
            this.modOrder = modOrder;
        }

        String pathString() {
            return String.join("/", path);
        }

        int rankOf(String childId) {
            int i = 0;
            for (String id : children.keySet()) {
                if (id.equals(childId)) return i;
                i++;
            }
            return Integer.MAX_VALUE;
        }

        void forEach(Consumer<Node> action) {
            action.accept(this);
            children.values().forEach(c -> c.forEach(action));
        }
    }

    static final class Definition {
        static final Definition EMPTY = new Definition(List.of(), List.of(), Map.of(), Map.of());

        final List<String> types;
        final List<String> modOrder;
        /** type key -> root node. */
        final Map<String, Node> roots;
        /** type key -> ids the pack leaves out of JEI on purpose (never added, so not a runtime hide). */
        final Map<String, List<ResourceLocation>> packHidden;

        Definition(List<String> types, List<String> modOrder, Map<String, Node> roots,
                   Map<String, List<ResourceLocation>> packHidden) {
            this.types = types;
            this.modOrder = modOrder;
            this.roots = roots;
            this.packHidden = packHidden;
        }

        int typeRank(String typeKey) {
            int i = types.indexOf(typeKey);
            return i >= 0 ? i : types.size();
        }

        int nodeCount() {
            int[] n = {0};
            roots.values().forEach(r -> r.forEach(x -> n[0]++));
            return n[0] - roots.size();
        }

        static Definition parse(String json) {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            List<String> types = strings(obj.get("types"));
            List<String> modOrder = strings(obj.get("modOrder"));
            Map<String, Node> roots = new LinkedHashMap<>();
            JsonObject trees = obj.has("trees") ? obj.getAsJsonObject("trees") : new JsonObject();
            for (Map.Entry<String, JsonElement> tree : trees.entrySet()) {
                Node root = new Node(tree.getKey(), List.of(), null, null);
                addChildren(root, tree.getValue().getAsJsonArray());
                roots.put(tree.getKey(), root);
            }
            Map<String, List<ResourceLocation>> packHidden = new HashMap<>();
            if (obj.has("packHidden")) {
                for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("packHidden").entrySet()) {
                    packHidden.put(e.getKey(), strings(e.getValue()).stream().map(ResourceLocation::new).toList());
                }
            }
            return new Definition(types, modOrder, roots, packHidden);
        }

        private static void addChildren(Node parent, JsonArray children) {
            for (JsonElement el : children) {
                JsonObject o = el.getAsJsonObject();
                String id = o.get("id").getAsString();
                List<String> path = new ArrayList<>(parent.path);
                path.add(id);
                List<String> sort = o.has("sort") ? strings(o.get("sort")) : null;
                if (sort != null) sort = sort.stream().map(s -> s.equals("alphabetical") ? "name" : s).toList();
                Node node = new Node(parent.type, List.copyOf(path), sort, o.has("modOrder") ? strings(o.get("modOrder")) : null);
                for (String entry : strings(o.get("list"))) {
                    ResourceLocation rl = ResourceLocation.tryParse(entry);
                    if (rl != null) node.listIndex.putIfAbsent(rl, node.listIndex.size());
                }
                if (parent.children.putIfAbsent(id, node) != null) {
                    LOGGER.warn("[jei order] duplicate node {}/{}, the first one is kept", parent.type, node.pathString());
                    continue;
                }
                if (o.has("children")) addChildren(node, o.getAsJsonArray("children"));
            }
        }

        private static List<String> strings(@Nullable JsonElement el) {
            if (el == null || !el.isJsonArray()) return List.of();
            List<String> out = new ArrayList<>();
            el.getAsJsonArray().forEach(e -> out.add(e.getAsString()));
            return out;
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Placement
    // ------------------------------------------------------------------------------------------------

    /** Where one ingredient id sits: its path (with ranks for sorting) and the defined node at it, if any. */
    static final class Placement {
        final List<String> segs;
        final int[] ranks;
        final String[] names;
        @Nullable final Node node;
        final String source; // "tag" or "list"

        Placement(List<String> segs, int[] ranks, String[] names, @Nullable Node node, String source) {
            this.segs = segs;
            this.ranks = ranks;
            this.names = names;
            this.node = node;
            this.source = source;
        }

        String pathString() {
            return String.join("/", segs);
        }

        boolean isUnder(Placement other) {
            return segs.size() >= other.segs.size() && segs.subList(0, other.segs.size()).equals(other.segs);
        }
    }

    /** All placements of one JEI start, plus what the report lists as problems. */
    static final class Placements {
        /** type key -> id -> placement. */
        final Map<String, Map<ResourceLocation, Placement>> byType = new HashMap<>();
        final List<String[]> conflicts = new ArrayList<>();          // type, id, kept path, other path
        final Set<String> undefinedPaths = new LinkedHashSet<>(); // "type:path" of tags/lists the definition lacks

        @Nullable
        Placement get(String typeKey, ResourceLocation id) {
            Map<ResourceLocation, Placement> m = byType.get(typeKey);
            return m == null ? null : m.get(id);
        }

        static Placements build(Definition def) {
            Placements p = new Placements();
            p.scanTags("item", Registry.ITEM, def);
            p.scanTags("fluid", Registry.FLUID, def);
            p.scanTags("effect", Registry.MOB_EFFECT, def);
            // list entries place the ids no jei tag placed (multiblocks always come this way)
            def.roots.values().forEach(root -> root.forEach(node -> {
                for (ResourceLocation id : node.listIndex.keySet()) {
                    Placement existing = p.get(node.type, id);
                    if (existing == null) {
                        p.byType.computeIfAbsent(node.type, k -> new HashMap<>()).put(id, p.placementFor(def, node.type, node.path, "list"));
                    } else if (!existing.segs.equals(node.path)) {
                        p.conflicts.add(new String[]{node.type, id.toString(), existing.pathString(), node.pathString() + " (list)"});
                    }
                }
            }));
            return p;
        }

        private <T> void scanTags(String typeKey, Registry<T> registry, Definition def) {
            Map<ResourceLocation, Placement> byId = byType.computeIfAbsent(typeKey, k -> new HashMap<>());
            registry.getTags().forEach(pair -> {
                ResourceLocation tag = pair.getFirst().location();
                if (!tag.getNamespace().equals(TAG_NAMESPACE) || !tag.getPath().startsWith(TAG_PREFIX)) return;
                List<String> segs = List.of(tag.getPath().substring(TAG_PREFIX.length()).split("/"));
                Placement placement = placementFor(def, typeKey, segs, "tag");
                for (Holder<T> holder : pair.getSecond()) {
                    ResourceLocation id = registry.getKey(holder.value());
                    if (id == null) continue;
                    Placement existing = byId.get(id);
                    if (existing == null || placement.isUnder(existing)) {
                        byId.put(id, placement); // deeper tag on the same branch wins
                    } else if (!existing.isUnder(placement)) {
                        // different branches: keep the one that sorts first, report both
                        boolean newFirst = comparePaths(placement, existing) < 0;
                        Placement kept = newFirst ? placement : existing;
                        Placement other = newFirst ? existing : placement;
                        byId.put(id, kept);
                        conflicts.add(new String[]{typeKey, id.toString(), kept.pathString(), other.pathString()});
                    }
                }
            });
        }

        private Placement placementFor(Definition def, String typeKey, List<String> segs, String source) {
            int[] ranks = new int[segs.size()];
            String[] names = new String[segs.size()];
            Node node = def.roots.get(typeKey);
            for (int i = 0; i < segs.size(); i++) {
                names[i] = segs.get(i);
                if (node != null) {
                    ranks[i] = node.rankOf(segs.get(i));
                    node = node.children.get(segs.get(i));
                } else {
                    ranks[i] = Integer.MAX_VALUE;
                }
            }
            if (node == null) undefinedPaths.add(typeKey + ":" + String.join("/", segs));
            return new Placement(segs, ranks, names, node, source);
        }

        private static int comparePaths(Placement a, Placement b) {
            int len = Math.min(a.ranks.length, b.ranks.length);
            for (int i = 0; i < len; i++) {
                int c = Integer.compare(a.ranks[i], b.ranks[i]);
                if (c != 0) return c;
                c = a.names[i].compareTo(b.names[i]);
                if (c != 0) return c;
            }
            return Integer.compare(a.ranks.length, b.ranks.length);
        }
    }
}
