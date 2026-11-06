package com.SaintAlfonzo.creativecommon.damage;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.SaintAlfonzo.creativecommon.damage.DamageTagSnapshot.Raw;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.slf4j.Logger;

/**
 * Loads {@code data/<namespace>/damage_tags/<path>.json} into {@link DamageTags} on server start
 * and on every {@code /reload}. The tag id is {@code <namespace>:<path>}, so
 * {@code data/creativecommon/damage_tags/electric.json} is {@code creativecommon:electric}.
 * Registered on the Forge bus in {@code CreativeCommon} (the static {@link SubscribeEvent} below).
 *
 * <p>File format - vanilla tag syntax, ids being {@link net.minecraft.world.damagesource.DamageSource#getMsgId()}:
 * <pre>
 * {
 *   "description": "free text, ignored",
 *   "replace": false,
 *   "values": ["ieTesla", "ieRevolver_*", "#creativecommon:fire"]
 * }
 * </pre>
 * Every pack's copy of a file is merged, lowest priority first; {@code "replace": true} discards
 * what earlier packs added. A malformed file or entry is logged and skipped, never fatal.
 */
public final class DamageTagLoader extends SimplePreparableReloadListener<Map<ResourceLocation, List<Raw>>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    static final String DIRECTORY = "damage_tags";
    private static final String SUFFIX = ".json";

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DamageTagLoader());
    }

    @Override
    protected Map<ResourceLocation, List<Raw>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<Raw>> out = new TreeMap<>();
        for (Map.Entry<ResourceLocation, List<Resource>> entry
                : manager.listResourceStacks(DIRECTORY, rl -> rl.getPath().endsWith(SUFFIX)).entrySet()) {
            ResourceLocation file = entry.getKey();
            String path = file.getPath();
            ResourceLocation tag = new ResourceLocation(file.getNamespace(),
                    path.substring(DIRECTORY.length() + 1, path.length() - SUFFIX.length()));

            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {
                    // Parse before touching `out`: a bad file must not leave an empty entry behind
                    // (that would define the tag with no members).
                    Raw raw = parse(resource.sourcePackId(), tag, JsonParser.parseReader(reader));
                    out.computeIfAbsent(tag, k -> new ArrayList<>()).add(raw);
                } catch (Exception e) {
                    // Includes a malformed-JSON exception: skip just this pack's copy of just this file.
                    LOGGER.error("[damage-tags] could not read {} from pack '{}': {}", file, resource.sourcePackId(), e.toString());
                }
            }
        }
        return out;
    }

    static Raw parse(String pack, ResourceLocation tag, JsonElement json) {
        JsonObject root = GsonHelper.convertToJsonObject(json, "damage tag " + tag);
        boolean replace = GsonHelper.getAsBoolean(root, "replace", false);
        JsonArray array = GsonHelper.getAsJsonArray(root, "values", new JsonArray());

        List<String> values = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                values.add(element.getAsString());
            } else {
                LOGGER.warn("[damage-tags] {} (pack '{}'): ignoring non-string entry {} in \"values\"", tag, pack, element);
            }
        }
        return new Raw(pack, replace, values);
    }

    @Override
    protected void apply(Map<ResourceLocation, List<Raw>> prepared, ResourceManager manager, ProfilerFiller profiler) {
        DamageTagSnapshot snapshot = DamageTagSnapshot.build(prepared, msg -> LOGGER.warn("[damage-tags] {}", msg));
        DamageTags.install(snapshot);

        StringBuilder summary = new StringBuilder();
        snapshot.defs().forEach((id, def) -> summary
                .append(summary.length() == 0 ? "" : ", ")
                .append(DamageTags.NAMESPACE.equals(id.getNamespace()) ? id.getPath() : id.toString())
                .append('(').append(def.entryCount()).append(')'));
        LOGGER.info("[damage-tags] loaded {} data-defined tag(s): {}", snapshot.defs().size(), summary);
    }
}
