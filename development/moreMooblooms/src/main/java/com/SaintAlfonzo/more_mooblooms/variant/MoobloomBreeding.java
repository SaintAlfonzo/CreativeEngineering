package com.SaintAlfonzo.more_mooblooms.variant;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Which variant a calf is (R2, feature_writeup/mooblooms.md 4.8). First match wins:
 * <ol>
 *   <li>a combination rule for the unordered pair of parents, from {@code data/<ns>/moobloom_breeding/*.json}
 *       (first rule by file name, then order in the file, same as D11): roll its {@code chance}; on a hit the calf
 *       is a weighted pick from {@code results}, on a miss the next steps apply</li>
 *   <li>two parents of the same variant breed true (D34)</li>
 *   <li>a weighted pick over every variant's {@code breed_weight} (D33; witherbloom is 0)</li>
 * </ol>
 * A file is one rule or {@code {"rules": [...]}}:
 * <pre>{ "parents": ["more_mooblooms:rosebloom", "more_mooblooms:lilybloom"], "chance": 0.5,
 *   "results": [ {"variant": "more_mooblooms:peonybloom", "weight": 1} ] }</pre>
 * Variant ids are looked up when used, so the load order against the variant table doesn't matter. Synced to clients with the variants, for JEI only.
 */
public final class MoobloomBreeding {

    /** D35: a combination's default chance. */
    public static final float DEFAULT_CHANCE = 0.5F;

    public record Result(ResourceLocation variant, int weight) {}

    public record Rule(ResourceLocation source, ResourceLocation parentA, ResourceLocation parentB, float chance,
                       List<Result> results) {

        public boolean matches(ResourceLocation a, ResourceLocation b) {
            return (a.equals(parentA) && b.equals(parentB)) || (a.equals(parentB) && b.equals(parentA));
        }

        /** The results whose variant exists on this side. */
        public List<Result> knownResults(boolean clientSide) {
            return results.stream().filter(r -> MoobloomVariants.has(clientSide, r.variant())).toList();
        }
    }

    private static volatile List<Rule> server = List.of();
    private static volatile List<Rule> client = List.of();
    /** Server side: each file's JSON text, for the sync packet. */
    private static volatile Map<ResourceLocation, String> serverJson = Map.of();

    public static List<Rule> rules(boolean clientSide) {
        return clientSide ? client : server;
    }

    /** Server side: the calf's variant for these parents. */
    public static MoobloomVariant childOf(MoobloomVariant a, MoobloomVariant b, RandomSource random) {
        Rule rule = ruleFor(false, a.id(), b.id());
        if (rule != null && random.nextFloat() < rule.chance()) {
            List<Result> results = rule.knownResults(false);
            int total = 0;
            for (Result r : results) total += r.weight();
            if (total > 0) {
                int roll = random.nextInt(total);
                for (Result r : results) {
                    roll -= r.weight();
                    if (roll < 0) return MoobloomVariants.get(false, r.variant());
                }
            }
        }
        if (a.id().equals(b.id())) return a;
        int total = 0;
        for (MoobloomVariant v : MoobloomVariants.values(false)) total += v.breedWeight();
        if (total <= 0) return random.nextBoolean() ? a : b;
        int roll = random.nextInt(total);
        for (MoobloomVariant v : MoobloomVariants.values(false)) {
            roll -= v.breedWeight();
            if (roll < 0) return v;
        }
        return a;
    }

    /** The combination rule that applies to this pair, or null. */
    @Nullable
    public static Rule ruleFor(boolean clientSide, ResourceLocation a, ResourceLocation b) {
        for (Rule rule : rules(clientSide)) {
            if (rule.matches(a, b)) return rule;
        }
        return null;
    }

    public static Map<ResourceLocation, String> serverJson() {
        return serverJson;
    }

    /** Client side: replace the rules with the server's. A bad file is skipped. */
    public static void acceptFromServer(Map<ResourceLocation, String> json) {
        List<Rule> rules = new ArrayList<>();
        json.forEach((file, text) -> {
            try {
                rules.addAll(parseFile(file, JsonParser.parseString(text)));
            } catch (RuntimeException e) {
                MoreMooblooms.LOGGER.warn("[more_mooblooms] client can't use moobloom breeding file {} from the server: {}", file, e.getMessage());
            }
        });
        client = List.copyOf(rules);
    }

    public static void clearClient() {
        client = List.of();
    }

    static List<Rule> parseFile(ResourceLocation file, JsonElement json) {
        JsonObject root = GsonHelper.convertToJsonObject(json, "moobloom breeding file");
        List<Rule> out = new ArrayList<>();
        if (root.has("rules")) {
            for (JsonElement e : GsonHelper.getAsJsonArray(root, "rules")) {
                out.add(parseRule(file, GsonHelper.convertToJsonObject(e, "rule")));
            }
        } else {
            out.add(parseRule(file, root));
        }
        return out;
    }

    private static Rule parseRule(ResourceLocation file, JsonObject o) {
        JsonArray parents = GsonHelper.getAsJsonArray(o, "parents");
        if (parents.size() != 2) throw new JsonParseException("parents needs two variant ids");
        ResourceLocation a = new ResourceLocation(GsonHelper.convertToString(parents.get(0), "parent"));
        ResourceLocation b = new ResourceLocation(GsonHelper.convertToString(parents.get(1), "parent"));
        float chance = GsonHelper.getAsFloat(o, "chance", DEFAULT_CHANCE);
        if (chance < 0.0F || chance > 1.0F) throw new JsonParseException("chance must be 0-1");
        List<Result> results = new ArrayList<>();
        for (JsonElement e : GsonHelper.getAsJsonArray(o, "results")) {
            if (e.isJsonPrimitive()) {
                results.add(new Result(new ResourceLocation(e.getAsString()), 1));
            } else {
                JsonObject r = GsonHelper.convertToJsonObject(e, "result");
                int weight = GsonHelper.getAsInt(r, "weight", 1);
                if (weight < 1) throw new JsonParseException("result weight must be 1 or more");
                results.add(new Result(new ResourceLocation(GsonHelper.getAsString(r, "variant")), weight));
            }
        }
        if (results.isEmpty()) throw new JsonParseException("results is empty");
        return new Rule(file, a, b, chance, List.copyOf(results));
    }

    /** {@code data/<ns>/moobloom_breeding/<path>.json}, in file name order. */
    public static final class Loader extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public Loader() {
            super(GSON, "moobloom_breeding");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            List<Rule> rules = new ArrayList<>();
            Map<ResourceLocation, String> json = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> e : new TreeMap<>(files).entrySet()) {
                try {
                    rules.addAll(parseFile(e.getKey(), e.getValue()));
                    json.put(e.getKey(), e.getValue().toString());
                } catch (RuntimeException ex) {
                    MoreMooblooms.LOGGER.warn("[more_mooblooms] skipped moobloom breeding file {}: {}", e.getKey(), ex.getMessage());
                }
            }
            if (!MoobloomVariants.all(false).isEmpty()) { // same check as the spawn rules: only once variants loaded
                for (Rule rule : rules) {
                    List<ResourceLocation> ids = new ArrayList<>(List.of(rule.parentA(), rule.parentB()));
                    rule.results().forEach(r -> ids.add(r.variant()));
                    for (ResourceLocation id : ids) {
                        if (!MoobloomVariants.has(false, id)) {
                            MoreMooblooms.LOGGER.warn("[more_mooblooms] {}: unknown variant {}", rule.source(), id);
                        }
                    }
                }
            }
            server = List.copyOf(rules);
            serverJson = Collections.unmodifiableMap(json);
            MoreMooblooms.LOGGER.info("[more_mooblooms] loaded {} moobloom breeding rule(s) from {} file(s)", rules.size(), json.size());
        }
    }

    private MoobloomBreeding() {}
}
