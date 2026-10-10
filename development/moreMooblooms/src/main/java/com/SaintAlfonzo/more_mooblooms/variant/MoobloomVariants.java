package com.SaintAlfonzo.more_mooblooms.variant;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The variant table. The server loads it from {@code data/<ns>/moobloom_variants/*.json} on every data reload;
 * clients get a copy through {@link com.SaintAlfonzo.more_mooblooms.network.SyncVariantsPacket} at login and
 * after /reload. The two sides are kept apart (an integrated server and its client share these statics).

 */
public final class MoobloomVariants {

    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(MoreMooblooms.ID, "moobloom");
    private static final MoobloomVariant FALLBACK = MoobloomVariant.fallback(DEFAULT_ID);

    private static volatile Map<ResourceLocation, MoobloomVariant> server = Map.of();
    private static volatile Map<ResourceLocation, MoobloomVariant> client = Map.of();

    public static Map<ResourceLocation, MoobloomVariant> all(boolean clientSide) {
        return clientSide ? client : server;
    }

    /** True when {@code id} is a variant on this side. */
    public static boolean has(boolean clientSide, @Nullable ResourceLocation id) {
        return id != null && all(clientSide).containsKey(id);
    }

    public static Collection<MoobloomVariant> values(boolean clientSide) {
        return all(clientSide).values();
    }

    /** The variant with this id, else the default ({@code more_mooblooms:moobloom}), else the first, else built-in. */
    public static MoobloomVariant get(boolean clientSide, @Nullable ResourceLocation id) {
        MoobloomVariant v = id == null ? null : all(clientSide).get(id);
        return v != null ? v : getDefault(clientSide);
    }

    public static MoobloomVariant getDefault(boolean clientSide) {
        Map<ResourceLocation, MoobloomVariant> table = all(clientSide);
        MoobloomVariant v = table.get(DEFAULT_ID);
        if (v != null) return v;
        return table.isEmpty() ? FALLBACK : table.values().iterator().next();
    }

    /** Server side: true when any variant may spawn on {@code below} in the dark (the type-level placement check). */
    public static boolean anySpawnsInDarkOn(BlockState below) {
        for (MoobloomVariant v : server.values()) {
            if (v.spawnsInDarkOn(below)) return true;
        }
        return false;
    }

    /** A variant picked by {@code any_weight}; the default when every weight is 0. */
    public static MoobloomVariant pickWeighted(boolean clientSide, RandomSource random) {
        int total = 0;
        for (MoobloomVariant v : values(clientSide)) total += v.anyWeight();
        if (total <= 0) return getDefault(clientSide);
        int roll = random.nextInt(total);
        for (MoobloomVariant v : values(clientSide)) {
            roll -= v.anyWeight();
            if (roll < 0) return v;
        }
        return getDefault(clientSide);
    }

    /** Every variant that counts {@code stack} as one of its flowers (D6). */
    public static List<MoobloomVariant> byFlower(boolean clientSide, ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        List<MoobloomVariant> out = new ArrayList<>();
        for (MoobloomVariant v : values(clientSide)) {
            if (v.isFlower(stack)) out.add(v);
        }
        return out;
    }

    /** Server side: what goes into the sync packet. */
    public static Map<ResourceLocation, String> serverJson() {
        Map<ResourceLocation, String> out = new LinkedHashMap<>();
        server.forEach((id, v) -> out.put(id, v.json()));
        return out;
    }

    /** Client side: replace the table with the server's. Bad entries (a client missing a block) are skipped. */
    public static void acceptFromServer(Map<ResourceLocation, String> json) {
        Map<ResourceLocation, MoobloomVariant> table = new LinkedHashMap<>();
        json.forEach((id, text) -> {
            try {
                table.put(id, MoobloomVariant.parse(id, GsonHelper.convertToJsonObject(JsonParser.parseString(text), "variant")));
            } catch (RuntimeException e) {
                MoreMooblooms.LOGGER.warn("[more_mooblooms] client can't use moobloom variant {} from the server: {}", id, e.getMessage());
            }
        });
        client = Collections.unmodifiableMap(table);
    }

    public static void clearClient() {
        client = Map.of();
    }

    /** {@code data/<ns>/moobloom_variants/<path>.json}; ids are sorted so weighted picks are stable. */
    public static final class Loader extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public Loader() {
            super(GSON, "moobloom_variants");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, MoobloomVariant> table = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> e : new TreeMap<>(files).entrySet()) {
                try {
                    table.put(e.getKey(), MoobloomVariant.parse(e.getKey(), GsonHelper.convertToJsonObject(e.getValue(), "variant")));
                } catch (RuntimeException ex) {
                    MoreMooblooms.LOGGER.warn("[more_mooblooms] skipped moobloom variant {}: {}", e.getKey(), ex.getMessage());
                }
            }
            if (!table.containsKey(DEFAULT_ID)) {
                MoreMooblooms.LOGGER.warn("[more_mooblooms] no {} variant; the fallback variant is {}", DEFAULT_ID,
                        table.isEmpty() ? "built in" : table.keySet().iterator().next());
            }
            server = Collections.unmodifiableMap(table);
            MoreMooblooms.LOGGER.info("[more_mooblooms] loaded {} moobloom variants: {}", table.size(), table.keySet());
        }
    }

    private MoobloomVariants() {}
}
