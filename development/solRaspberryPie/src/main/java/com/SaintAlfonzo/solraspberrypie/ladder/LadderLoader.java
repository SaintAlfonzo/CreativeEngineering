package com.SaintAlfonzo.solraspberrypie.ladder;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads serverconfig/solraspberrypie/ladder.json once at server start (3), seeding it from
 * defaultconfigs/solraspberrypie/ladder.json or the built-in default. Applies the validation table (3.3):
 * unregistered effects and attributes are skipped (one warning listing them), unusable unlocks dropped, levels clamped,
 * refresh-unsafe effects refused, a broken file falls back to the built-in default with a loud error.
 */
public final class LadderLoader {
    private static final String BUILT_IN = "/solraspberrypie/default_ladder.json";
    /** V10: a refresh would refill, clamp or re-fire these. Instant effects are refused too. */
    private static final Set<MobEffect> REFRESH_UNSAFE = Set.of(MobEffects.ABSORPTION, MobEffects.HEALTH_BOOST, MobEffects.SATURATION);

    public static Ladder load(MinecraftServer server) {
        Path worldFile = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(SolRaspberryPie.MOD_ID).resolve("ladder.json");
        try {
            if (!Files.exists(worldFile)) {
                Files.createDirectories(worldFile.getParent());
                Path seed = FMLPaths.GAMEDIR.get().resolve("defaultconfigs").resolve(SolRaspberryPie.MOD_ID).resolve("ladder.json");
                if (Files.exists(seed)) {
                    Files.copy(seed, worldFile);
                    SolRaspberryPie.LOG.info("Seeded {} from {}", worldFile, seed);
                } else {
                    try (InputStream in = LadderLoader.class.getResourceAsStream(BUILT_IN)) {
                        Files.copy(in, worldFile);
                    }
                    SolRaspberryPie.LOG.info("Seeded {} from the built-in default ladder", worldFile);
                }
            }
        } catch (Exception e) {
            SolRaspberryPie.LOG.error("Could not seed {}: {}", worldFile, e.toString());
        }
        try (Reader r = Files.newBufferedReader(worldFile, StandardCharsets.UTF_8)) {
            Ladder ladder = parse(JsonParser.parseReader(r).getAsJsonObject(), worldFile.toString());
            SolRaspberryPie.LOG.info("Diet ladder: {} rungs, {} slots, {} pool effects", ladder.rungs.size(), ladder.slotCount(), ladder.pool.size());
            return ladder;
        } catch (Exception e) {
            // V13 (Q-H): fall back to the built-in ladder, loudly.
            SolRaspberryPie.LOG.error("================================================================");
            SolRaspberryPie.LOG.error("DIET LADDER FILE IS BROKEN OR MISSING: {}", worldFile);
            SolRaspberryPie.LOG.error("  {}", e.toString());
            SolRaspberryPie.LOG.error("  Using the built-in default ladder until the file is fixed and the server restarted.");
            SolRaspberryPie.LOG.error("================================================================");
            return builtIn();
        }
    }

    public static Ladder builtIn() {
        try (InputStream in = LadderLoader.class.getResourceAsStream(BUILT_IN);
             Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return parse(JsonParser.parseReader(r).getAsJsonObject(), "built-in default");
        } catch (Exception e) {
            SolRaspberryPie.LOG.error("Built-in ladder unreadable: {}", e.toString());
            return new Ladder(List.of(), Map.of());
        }
    }

    static Ladder parse(JsonObject root, String source) {
        List<String> skipped = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Map<ResourceLocation, Integer> pool = new LinkedHashMap<>();
        for (JsonElement el : arr(root, "pool")) {
            JsonObject o = el.getAsJsonObject();
            ResourceLocation id = new ResourceLocation(o.get("effect").getAsString());
            int cap = o.has("max_level") ? Math.max(1, o.get("max_level").getAsInt()) : 1;
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
            if (!ForgeRegistries.MOB_EFFECTS.containsKey(id) || effect == null) {
                skipped.add("pool " + id);
                continue;
            }
            if (refreshUnsafe(effect)) {
                warnings.add("V10: " + id + " cannot be refreshed safely; removed from the pool");
                continue;
            }
            pool.put(id, cap);
        }

        List<Rung> rungs = new ArrayList<>();
        for (JsonElement el : arr(root, "rungs")) {
            JsonObject o = el.getAsJsonObject();
            double t = o.get("threshold").getAsDouble();
            String type = o.get("type").getAsString().toLowerCase(Locale.ROOT);
            switch (type) {
                case "health" -> rungs.add(new Rung(Rung.Kind.HEALTH, t, null, o.get("amount").getAsDouble(), 0));
                case "attribute" -> {
                    ResourceLocation id = new ResourceLocation(o.get("attribute").getAsString());
                    if (!ForgeRegistries.ATTRIBUTES.containsKey(id)) {
                        skipped.add("attribute " + id + " at " + t);
                        continue;
                    }
                    rungs.add(new Rung(Rung.Kind.ATTRIBUTE, t, id, o.get("amount").getAsDouble(), 0));
                }
                case "slot" -> rungs.add(new Rung(Rung.Kind.SLOT, t, null, 0, 0));
                case "flat", "unlock" -> {
                    ResourceLocation id = new ResourceLocation(o.get("effect").getAsString());
                    int level = o.has("level") ? Math.max(1, o.get("level").getAsInt()) : 1;
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
                    if (!ForgeRegistries.MOB_EFFECTS.containsKey(id) || effect == null) {
                        skipped.add(type + " " + id + " at " + t);
                        continue;
                    }
                    if (type.equals("flat")) {
                        if (refreshUnsafe(effect)) {
                            warnings.add("V10: flat " + id + " at " + t + " cannot be refreshed safely; skipped");
                            continue;
                        }
                        rungs.add(new Rung(Rung.Kind.FLAT, t, id, 0, level));
                    } else {
                        Integer cap = pool.get(id);
                        if (cap == null) {
                            warnings.add("V3: unlock " + id + " at " + t + " is not in the slot pool; skipped");
                            continue;
                        }
                        if (level > cap) {
                            warnings.add("V5: unlock " + id + " level " + level + " at " + t + " is above the pool cap " + cap + "; clamped");
                            level = cap;
                        }
                        rungs.add(new Rung(Rung.Kind.UNLOCK, t, id, 0, level));
                    }
                }
                default -> warnings.add("unknown rung type '" + type + "' at " + t + "; skipped");
            }
        }

        Ladder ladder = new Ladder(rungs, pool);

        // V4: pool effects nobody can unlock.
        for (ResourceLocation id : pool.keySet()) {
            if (ladder.rungs.stream().noneMatch(r -> r.kind == Rung.Kind.UNLOCK && id.equals(r.id))) {
                warnings.add("V4: pool effect " + id + " has no unlock rung and can never be picked");
            }
        }
        // V6: a higher level unlocked before a lower one (the higher one implies the lower ones, so it still works).
        Map<ResourceLocation, Map<Integer, Double>> firstAt = new LinkedHashMap<>();
        for (Rung r : ladder.rungs) {
            if (r.kind == Rung.Kind.UNLOCK) firstAt.computeIfAbsent(r.id, k -> new LinkedHashMap<>()).putIfAbsent(r.level, r.threshold);
        }
        firstAt.forEach((id, levels) -> levels.forEach((lvl, at) -> levels.forEach((lvl2, at2) -> {
            if (lvl2 < lvl && at2 > at) {
                warnings.add("V6: " + id + " level " + lvl + " unlocks at " + at + " before level " + lvl2 + " at " + at2
                        + "; the higher unlock also grants the lower levels");
            }
        })));
        // V7: slot milestones on the same threshold.
        Set<Double> slotThresholds = new HashSet<>();
        for (Rung s : ladder.slots) {
            if (!slotThresholds.add(s.threshold)) warnings.add("V7: two slot milestones at " + s.threshold + "; numbered in file order");
        }
        // V12: unreachable thresholds.
        double reach = maxReachableDiversity();
        if (reach > 0) {
            for (Rung r : ladder.rungs) {
                if (r.threshold > reach) warnings.add("V12: " + r + " is above the highest reachable diversity (about " + String.format(Locale.ROOT, "%.1f", reach) + ")");
            }
        }

        if (!skipped.isEmpty()) {
            SolRaspberryPie.LOG.warn("Diet ladder ({}): skipped {} entries naming effects or attributes that are not registered: {}",
                    source, skipped.size(), String.join(", ", skipped));
        }
        for (String w : warnings) SolRaspberryPie.LOG.warn("Diet ladder ({}): {}", source, w);
        return ladder;
    }

    public static boolean refreshUnsafe(MobEffect effect) {
        return effect.isInstantenous() || REFRESH_UNSAFE.contains(effect);
    }

    /** Upper bound: the most complex food in every position of the decay window. */
    private static double maxReachableDiversity() {
        try {
            Item best = null;
            double bestC = -1;
            for (Item item : ForgeRegistries.ITEMS) {
                if (!item.isEdible()) continue;
                double c = FoodList.getComplexity(new FoodInstance(item));
                if (c > bestC) {
                    bestC = c;
                    best = item;
                }
            }
            if (best == null) return 0;
            FoodInstance food = new FoodInstance(best);
            double sum = 0;
            int size = SOLSweetPotatoConfig.size();
            for (int k = 0; k < size; k++) sum += FoodList.calculateDiversityContribution(food, k);
            return sum;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static JsonArray arr(JsonObject root, String key) {
        return root.has(key) ? root.getAsJsonArray(key) : new JsonArray();
    }

    private LadderLoader() {}
}
