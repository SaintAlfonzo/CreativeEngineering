package com.SaintAlfonzo.creativecommon.compat.jei;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecommon.compat.jei.PackIngredientOrder.Definition;
import com.SaintAlfonzo.creativecommon.compat.jei.PackIngredientOrder.Placement;
import com.SaintAlfonzo.creativecommon.compat.jei.PackIngredientOrder.Placements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IngredientFilter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * kubejs/exported/jei_order.json, written at each JEI start when {@code CommonConfig [jei] orderReport} is on: every
 * ingredient id of the item, fluid and effect registries plus everything JEI listed, with its place in the pack
 * order and its visibility class, and the definition problems. {@code python3 tools/jei_order_report.py} turns it
 * into feature_writeup/jei_order_catalog.md.
 *
 * <p>Visibility classes (feature_writeup/jei_ordering.md section 6), per id (NBT variants of one item together):
 * <ul>
 *   <li>{@code shown}: at least one variant is in the list (placed or not, see {@code path});</li>
 *   <li>{@code hidden_pack}: hidden by KubeJS ({@code JEIEvents.hide*}, plugin {@code kubejs:jei}), in the
 *       definition's {@code packHidden}, or in JEED's {@code hidden_effects};</li>
 *   <li>{@code hidden_mod}: removed at runtime by another plugin ({@code removedBy} names it);</li>
 *   <li>{@code hidden_player}: JEI has it but hides it, and nobody removed it: JEI's edit mode
 *       (config/jei/blacklist.cfg);</li>
 *   <li>{@code not_shown}: registered in the game but never in JEI (no creative tab, no plugin added it).</li>
 * </ul>
 */
public final class PackOrderReport {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String OUT = "kubejs/exported/jei_order.json";
    private static final Pattern JEED_HIDDEN = Pattern.compile("hidden_effects\\s*=\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]+)\"");

    private PackOrderReport() {}

    /** One ingredient id: its JEI elements (all NBT variants) and where it sits. */
    private static final class Row {
        final String type;
        final ResourceLocation id;
        String mod;
        int elements;
        int visible;
        int position = Integer.MAX_VALUE;
        String name;

        Row(String type, ResourceLocation id) {
            this.type = type;
            this.id = id;
            this.mod = id.getNamespace();
        }
    }

    static void write(IngredientFilter filter, IIngredientManager manager) {
        long start = System.nanoTime();
        Definition def = PackIngredientOrder.definition();
        Placements placements = PackIngredientOrder.placements();

        // type -> id -> row; the registries first (so never-listed ids show up), then JEI's elements
        Map<String, Map<ResourceLocation, Row>> rows = new LinkedHashMap<>();
        for (String type : def.types) rows.put(type, new TreeMap<>());
        Map<ResourceLocation, Row> items = rows.computeIfAbsent("item", k -> new TreeMap<>());
        Registry.ITEM.keySet().forEach(id -> items.put(id, new Row("item", id)));
        items.remove(Registry.ITEM.getKey(Items.AIR));
        Map<ResourceLocation, Row> fluids = rows.computeIfAbsent("fluid", k -> new TreeMap<>());
        for (Fluid fluid : Registry.FLUID) {
            if (fluid == Fluids.EMPTY || !fluid.defaultFluidState().isSource()) continue;
            ResourceLocation id = Registry.FLUID.getKey(fluid);
            fluids.put(id, new Row("fluid", id));
        }
        Map<ResourceLocation, Row> effects = rows.computeIfAbsent("effect", k -> new TreeMap<>());
        Registry.MOB_EFFECT.keySet().forEach(id -> effects.put(id, new Row("effect", id)));

        for (IListElement<?> element : PackIngredientOrder.allElements(filter)) {
            addElement(rows, manager, element);
        }

        Set<String> jeedHidden = jeedHiddenEffects();
        JsonArray entries = new JsonArray();
        Map<String, Map<String, Integer>> summary = new LinkedHashMap<>();
        JsonArray hiddenButPlaced = new JsonArray();
        Map<String, Integer> nodeMembers = new HashMap<>();
        Set<String> seenIds = new HashSet<>();

        for (Map.Entry<String, Map<ResourceLocation, Row>> byType : rows.entrySet()) {
            String type = byType.getKey();
            Set<ResourceLocation> packHidden = new HashSet<>(def.packHidden.getOrDefault(type, List.of()));
            for (Row row : byType.getValue().values()) {
                seenIds.add(type + ":" + row.id);
                Set<String> removedBy = JeiIngredientChanges.removedBy(type, row.id);
                Set<String> addedBy = JeiIngredientChanges.addedBy(type, row.id);
                String cls;
                if (row.visible > 0) cls = "shown";
                else if (packHidden.contains(row.id)
                        || (removedBy != null && removedBy.contains(JeiIngredientChanges.KUBEJS_PLUGIN))
                        || (type.equals("effect") && jeedHidden.contains(row.id.toString()))) cls = "hidden_pack";
                else if (removedBy != null) cls = "hidden_mod";
                else if (row.elements > 0) cls = "hidden_player";
                else cls = "not_shown";
                summary.computeIfAbsent(type, k -> new LinkedHashMap<>()).merge(cls, 1, Integer::sum);

                Placement placement = placements.get(type, row.id);
                JsonObject e = new JsonObject();
                e.addProperty("type", type);
                e.addProperty("id", row.id.toString());
                e.addProperty("mod", row.mod);
                if (row.name != null) e.addProperty("name", row.name);
                e.addProperty("class", cls);
                if (placement != null) {
                    e.addProperty("path", placement.pathString());
                    e.addProperty("placedBy", placement.source);
                    if (placement.node == null) e.addProperty("undefinedPath", true);
                    nodeMembers.merge(type + ":" + placement.pathString(), 1, Integer::sum);
                    if (!cls.equals("shown")) {
                        JsonObject h = new JsonObject();
                        h.addProperty("type", type);
                        h.addProperty("id", row.id.toString());
                        h.addProperty("path", placement.pathString());
                        h.addProperty("class", cls);
                        hiddenButPlaced.add(h);
                    }
                }
                if (row.position != Integer.MAX_VALUE) e.addProperty("position", row.position);
                if (row.elements > 1) e.addProperty("variants", row.elements);
                if (removedBy != null) e.add("removedBy", GSON.toJsonTree(removedBy));
                if (addedBy != null) e.add("addedBy", GSON.toJsonTree(addedBy));
                entries.add(e);
            }
        }

        JsonObject problems = new JsonObject();
        JsonArray conflicts = new JsonArray();
        for (String[] c : placements.conflicts) {
            JsonObject o = new JsonObject();
            o.addProperty("type", c[0]);
            o.addProperty("id", c[1]);
            o.addProperty("kept", c[2]);
            o.addProperty("other", c[3]);
            conflicts.add(o);
        }
        problems.add("conflicts", conflicts);
        problems.add("undefinedPaths", GSON.toJsonTree(new ArrayList<>(placements.undefinedPaths)));
        JsonArray emptyNodes = new JsonArray();
        JsonArray missingListEntries = new JsonArray();
        def.roots.values().forEach(root -> root.forEach(node -> {
            if (node.path.isEmpty()) return;
            boolean hasMembers = nodeMembers.keySet().stream()
                    .anyMatch(k -> k.equals(node.type + ":" + node.pathString()) || k.startsWith(node.type + ":" + node.pathString() + "/"));
            if (!hasMembers) emptyNodes.add(node.type + ":" + node.pathString());
            for (ResourceLocation id : node.listIndex.keySet()) {
                if (!seenIds.contains(node.type + ":" + id)) missingListEntries.add(node.type + ":" + node.pathString() + " -> " + id);
            }
        }));
        problems.add("emptyNodes", emptyNodes);
        problems.add("missingListEntries", missingListEntries);
        problems.add("hiddenButPlaced", hiddenButPlaced);

        JsonObject root = new JsonObject();
        root.addProperty("generated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        root.addProperty("packOrder", CommonConfig.JEI.packOrder.get());
        root.addProperty("definitionLoaded", PackIngredientOrder.hasDefinition());
        root.add("types", GSON.toJsonTree(def.types));
        root.add("summary", GSON.toJsonTree(summary));
        root.add("problems", problems);
        root.add("entries", entries);

        Path out = FMLPaths.GAMEDIR.get().resolve(OUT);
        try {
            Files.createDirectories(out.getParent());
            Files.writeString(out, GSON.toJson(root), StandardCharsets.UTF_8);
            LOGGER.info("[jei order] report: {} ids -> {} in {} ms", entries.size(), OUT, (System.nanoTime() - start) / 1_000_000);
        } catch (IOException e) {
            LOGGER.error("[jei order] couldn't write {}", OUT, e);
        }
    }

    private static <V> void addElement(Map<String, Map<ResourceLocation, Row>> rows, IIngredientManager manager, IListElement<V> element) {
        ITypedIngredient<V> typed = element.getTypedIngredient();
        IIngredientHelper<V> helper = manager.getIngredientHelper(typed.getType());
        ResourceLocation id = PackIngredientOrder.safeId(helper, typed.getIngredient());
        if (id == null) return;
        String type = PackIngredientOrder.typeKey(typed.getType());
        Row row = rows.computeIfAbsent(type, k -> new TreeMap<>()).computeIfAbsent(id, k -> new Row(type, id));
        row.elements++;
        if (row.name == null) {
            try {
                row.name = helper.getDisplayName(typed.getIngredient());
            } catch (RuntimeException | LinkageError ignored) {
                // name stays unset
            }
        }
        row.mod = PackIngredientOrder.safeModId(helper, typed.getIngredient(), id);
        if (element.isVisible()) {
            row.visible++;
            row.position = Math.min(row.position, element.getSortedIndex());
        }
    }

    /** JEED's {@code hidden_effects} (config/jeed-client.toml): effects JEED never registers with JEI. */
    private static Set<String> jeedHiddenEffects() {
        Set<String> out = new HashSet<>();
        Path config = FMLPaths.CONFIGDIR.get().resolve("jeed-client.toml");
        try {
            if (!Files.exists(config)) return out;
            Matcher m = JEED_HIDDEN.matcher(Files.readString(config, StandardCharsets.UTF_8));
            if (m.find()) {
                Matcher q = QUOTED.matcher(m.group(1));
                while (q.find()) out.add(q.group(1));
            }
        } catch (IOException e) {
            LOGGER.warn("[jei order] couldn't read {}", config, e);
        }
        return out;
    }
}
