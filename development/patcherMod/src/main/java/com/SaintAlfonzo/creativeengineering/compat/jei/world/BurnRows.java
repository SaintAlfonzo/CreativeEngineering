package com.SaintAlfonzo.creativeengineering.compat.jei.world;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.SaintAlfonzo.creativeengineering.itemphysic.ItemBurnTransform;
import com.SaintAlfonzo.creativeengineering.itemphysic.ItemPhysicSplit;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Burning tab: what a dropped item leaves behind when fire or lava destroys it ({@link ItemBurnTransform} REPLACE rules,
 * set by kubejs/startup_scripts/func/item_physics.js {@code burnReplace}; the burnExplode rules are not shown), plus
 * which items survive fire or lava ({@link ItemPhysicSplit} tags) and Supplementaries' ash from burned blocks.
 *
 * <p>Built when JEI starts: the rules are registered by KubeJS startup on the client too, and tag membership (synced
 * by then) decides which items each rule catches and whether fire, lava or both destroy them. Rows in the
 * {@link WorldRow} JSON format.
 */
final class BurnRows {
    private BurnRows() {}

    static List<JsonObject> rows() {
        List<JsonObject> rows = new ArrayList<>();
        rows.addAll(transformRows());
        rows.addAll(survivalRows());
        JsonObject ash = ashRow();
        if (ash != null) rows.add(ash);
        return rows;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Burn transforms

    /** One group: a rule plus where the items burn. Items in a group share a row. */
    private record Group(ItemBurnTransform.Rule rule, boolean fire, boolean lava) {}

    private static List<JsonObject> transformRows() {
        Map<Group, List<String>> groups = new LinkedHashMap<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ItemStack stack = new ItemStack(item);
            ItemBurnTransform.Rule rule = ItemBurnTransform.ruleFor(stack);
            if (rule == null || rule.kind != ItemBurnTransform.Kind.REPLACE) continue;
            // null = no pack rule: ItemPhysic's own list decides, and the pack's config burns everything (blacklist, empty)
            boolean fire = !Boolean.FALSE.equals(ItemPhysicSplit.burnDecision(stack, false));
            boolean lava = !Boolean.FALSE.equals(ItemPhysicSplit.burnDecision(stack, true));
            if (!fire && !lava) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            groups.computeIfAbsent(new Group(rule, fire, lava), k -> new ArrayList<>()).add(Objects.requireNonNull(id).toString());
        }
        List<JsonObject> rows = new ArrayList<>();
        for (Map.Entry<Group, List<String>> entry : groups.entrySet()) {
            Group g = entry.getKey();
            JsonObject input = new JsonObject();
            input.add("items", array(entry.getValue()));

            JsonObject action;
            String where;
            if (g.fire && g.lava) {
                action = ing("block", "minecraft:fire", "Open fire or lava");
                where = "fire or lava";
            } else if (g.lava) {
                action = ing("fluid", "minecraft:lava", "Lava only: survives open fire");
                where = "lava";
            } else {
                action = ing("block", "minecraft:fire", "Open fire only: survives lava");
                where = "open fire";
            }

            JsonObject output = ing("item", g.rule.result.toString(),
                    g.rule.keepCount ? "Rolled for each item in the stack" : "Rolled once for the whole stack");
            if (g.rule.chance < 1f) output.addProperty("chance", g.rule.chance);

            List<String> notes = new ArrayList<>();
            notes.add("Burned by " + where + ": leaves this.");
            notes.add("The result burns if dropped back in.");
            rows.add(row(List.of(input), action, List.of(output), notes, false));
        }
        return rows;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Fire vs lava survival (ItemPhysicSplit tags)

    private static List<JsonObject> survivalRows() {
        List<JsonObject> rows = new ArrayList<>();
        JsonObject fireproof = new JsonObject();
        fireproof.addProperty("tag", ItemPhysicSplit.FIREPROOF.location().toString());
        rows.add(row(List.of(fireproof), ing("block", "minecraft:fire", "Open fire"), List.of(fireproof.deepCopy()),
                List.of("Dropped, these survive open fire.", "Lava destroys them."), true));

        JsonObject lavaproof = new JsonObject();
        lavaproof.addProperty("tag", ItemPhysicSplit.LAVAPROOF.location().toString());
        rows.add(row(List.of(lavaproof), ing("fluid", "minecraft:lava", "Fire or lava"), List.of(lavaproof.deepCopy()),
                List.of("Dropped, these survive fire and lava.", "Other items burn in both."), true));
        return rows;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Supplementaries ash (AshLayerBlock.tryConvertToAsh, read from the 2.4.20 jar): a block that fire burns away
    // becomes ash layers, clamp(random(burnTime / 100, +2 for #minecraft:logs_that_burn), 1, 8). Logs (300) give
    // 1 to 4, planks (300) 1 to 2, anything with a fuel value of 100 to 199 one layer; no fuel value, no ash.

    private static JsonObject ashRow() {
        if (!ModList.get().isLoaded("supplementaries")) return null;
        Path file = FMLPaths.CONFIGDIR.get().resolve("supplementaries-common.toml");
        if (!tomlBool(file, "building.ash.enabled", true) || !tomlBool(file, "building.ash.ash_from_fire", true)) return null;

        JsonObject logs = ing("block_tag", "minecraft:logs_that_burn", "1 to 4 layers");
        JsonObject planks = ing("block_tag", "minecraft:planks", "1 to 2 layers");
        JsonObject ash = ing("item", "supplementaries:ash", "Ash layers; each layer drops one ash");
        List<String> notes = new ArrayList<>();
        notes.add("A burned block leaves 1 to 8 ash layers, by fuel value.");
        if (tomlBool(file, "building.ash.rain_wash_ash", true)) notes.add("Rain washes ash away.");
        return row(List.of(logs, planks), ing("block", "minecraft:fire", "Fire burns the block"), List.of(ash), notes, false);
    }

    /** A boolean from a TOML config file, or {@code fallback} when the file or key is missing. */
    static boolean tomlBool(Path file, String path, boolean fallback) {
        Object value = tomlValue(file, path);
        return value instanceof Boolean b ? b : fallback;
    }

    /** A raw value from a TOML config file, or null. */
    static Object tomlValue(Path file, String path) {
        if (!Files.isRegularFile(file)) return null;
        try (CommentedFileConfig config = CommentedFileConfig.of(file)) {
            config.load();
            return config.get(path);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // JSON helpers (shared with CampfireRows)

    static JsonObject ing(String kind, String id, String label) {
        JsonObject o = new JsonObject();
        o.addProperty(kind, id);
        if (kind.equals("fluid")) o.addProperty("amount", 1000);
        if (label != null) o.addProperty("label", label);
        return o;
    }

    static JsonArray array(List<String> values) {
        JsonArray a = new JsonArray();
        values.forEach(a::add);
        return a;
    }

    static JsonObject row(List<JsonObject> inputs, JsonObject action, List<JsonObject> outputs, List<String> notes, boolean linked) {
        JsonObject r = new JsonObject();
        JsonArray in = new JsonArray();
        inputs.forEach(in::add);
        r.add("inputs", in);
        if (action != null) r.add("action", action);
        JsonArray out = new JsonArray();
        outputs.forEach(out::add);
        r.add("outputs", out);
        if (notes != null && !notes.isEmpty()) r.add("notes", array(notes));
        if (linked) r.addProperty("linked", true);
        return r;
    }
}
