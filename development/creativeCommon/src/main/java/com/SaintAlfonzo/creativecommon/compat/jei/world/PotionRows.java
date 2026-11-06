package com.SaintAlfonzo.creativecommon.compat.jei.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Turns a row marked {@code "per_potion": true} into one row per potion (TODO jei/jei-coverage #23). The generator
 * (tools/jei_world/tab_drops.py) writes the jar rows for the potion soft fluid once, with capacity and notes; this
 * fills in each potion:
 * <ul>
 *   <li>a slot of potion bottles ({@code minecraft:potion}, splash, lingering) becomes those bottles holding the potion;</li>
 *   <li>a jar slot holding {@code moonlight:potion} becomes one jar per bottle kind in the row, in the same order, with
 *       Moonlight's {@code NBT:{Potion, Bottle}} and the potion's colour as {@code CachedColor} (the jar item renderer
 *       draws that colour);</li>
 *   <li>a {@code create:potion} fluid gets the potion's {@code Potion} / {@code Bottle} tag.</li>
 * </ul>
 * The row is linked, so the bottle kind and the jar cycle together. Water and the base potions (awkward, thick,
 * mundane) and potions with no effect are left out.
 */
final class PotionRows {
    private static final Map<String, String> BOTTLES = Map.of(
            "minecraft:potion", "REGULAR",
            "minecraft:splash_potion", "SPLASH",
            "minecraft:lingering_potion", "LINGERING");
    private static final Set<Potion> SKIP = Set.of(Potions.EMPTY, Potions.WATER, Potions.AWKWARD, Potions.THICK, Potions.MUNDANE);
    private static final String JAR = "supplementaries:jar";
    private static final String POTION_FLUID = "moonlight:potion";

    private PotionRows() {}

    /** The rows with every {@code per_potion} row replaced by its potions' rows. */
    static List<JsonObject> expand(List<JsonObject> rows) {
        List<JsonObject> out = new ArrayList<>(rows.size());
        for (JsonObject row : rows) {
            if (!row.has("per_potion") || !row.get("per_potion").getAsBoolean()) {
                out.add(row);
                continue;
            }
            for (Potion potion : ForgeRegistries.POTIONS) {
                if (SKIP.contains(potion) || potion.getEffects().isEmpty()) continue;
                out.add(forPotion(row, ForgeRegistries.POTIONS.getKey(potion).toString(),
                        0xFF000000 | PotionUtils.getColor(potion)));
            }
        }
        return out;
    }

    private static JsonObject forPotion(JsonObject template, String potion, int color) {
        JsonObject row = template.deepCopy();
        row.remove("per_potion");
        List<String> kinds = bottleKinds(row);
        for (String side : new String[] {"inputs", "outputs"}) {
            JsonArray slots = row.getAsJsonArray(side);
            if (slots == null) continue;
            for (JsonElement element : slots) fill(element.getAsJsonObject(), potion, color, kinds);
        }
        row.addProperty("linked", true);
        return row;
    }

    /** The bottle kinds of the row's first potion bottle slot, in slot order; just REGULAR when it has none. */
    private static List<String> bottleKinds(JsonObject row) {
        for (String side : new String[] {"inputs", "outputs"}) {
            JsonArray slots = row.getAsJsonArray(side);
            if (slots == null) continue;
            for (JsonElement element : slots) {
                List<String> ids = ids(element.getAsJsonObject());
                if (!ids.isEmpty() && BOTTLES.keySet().containsAll(ids)) return ids.stream().map(BOTTLES::get).toList();
            }
        }
        return List.of("REGULAR");
    }

    private static void fill(JsonObject ing, String potion, int color, List<String> kinds) {
        List<String> ids = ids(ing);
        if (!ids.isEmpty() && BOTTLES.keySet().containsAll(ids)) {
            JsonArray stacks = new JsonArray();
            for (String id : ids) stacks.add(stack(id, "{Potion:\"" + potion + "\"}"));
            replaceItems(ing, stacks);
            ing.remove("label");
        } else if (ids.equals(List.of(JAR)) && ing.has("nbt") && ing.get("nbt").getAsString().contains(POTION_FLUID)) {
            JsonArray stacks = new JsonArray();
            for (String kind : kinds) stacks.add(stack(JAR, jarNbt(ing.get("nbt").getAsString(), potion, kind, color)));
            replaceItems(ing, stacks);
        } else if (ing.has("fluid") && ing.get("fluid").getAsString().equals("create:potion")) {
            ing.addProperty("nbt", "{Potion:\"" + potion + "\",Bottle:\"" + kinds.get(0) + "\"}");
        }
    }

    private static String jarNbt(String template, String potion, String kind, int color) {
        try {
            CompoundTag tag = TagParser.parseTag(template);
            CompoundTag holder = tag.getCompound("BlockEntityTag").getCompound("FluidHolder");
            CompoundTag fluidNbt = new CompoundTag();
            fluidNbt.putString("Potion", potion);
            fluidNbt.putString("Bottle", kind);
            holder.put("NBT", fluidNbt);
            holder.putInt("CachedColor", color);
            return tag.toString();
        } catch (CommandSyntaxException e) {
            return template;
        }
    }

    private static List<String> ids(JsonObject ing) {
        List<String> ids = new ArrayList<>();
        if (ing.has("item")) ids.add(ing.get("item").getAsString());
        if (ing.has("items")) for (JsonElement id : ing.getAsJsonArray("items")) ids.add(id.getAsString());
        return ids;
    }

    private static JsonObject stack(String item, String nbt) {
        JsonObject stack = new JsonObject();
        stack.addProperty("item", item);
        stack.addProperty("nbt", nbt);
        return stack;
    }

    private static void replaceItems(JsonObject ing, JsonArray stacks) {
        ing.remove("item");
        ing.remove("items");
        ing.remove("nbt");
        ing.add("stacks", stacks);
    }
}
