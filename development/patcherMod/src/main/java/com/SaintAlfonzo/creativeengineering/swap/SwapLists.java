package com.SaintAlfonzo.creativeengineering.swap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Swap lists (feature_writeup/creative_delight.md 11, creative_split.md 7.6): the retained record of every id a Creative
 * jar replaced. Each loaded mod may ship {@code data/<modid>/swaps/*.json} in its own jar:
 * <pre>
 * {
 *   "namespaces": { "old_namespace": "newnamespace" },
 *   "replaces":   { "newmod:thing": ["oldmod:thing", "oldmod:other_thing"] }
 * }
 * </pre>
 * {@code namespaces} moves every id of the old namespace to the same path in the new one; {@code replaces} names single
 * ids and wins over a namespace rule. The lists are never pruned, so a world last saved on any older version can still be
 * read. Read once, from the jars (not from datapacks: {@link SwapRemapper} needs them before any datapack loads).
 */
public final class SwapLists {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static volatile SwapLists loaded;

	private final Map<String, String> namespaces;
	private final Map<ResourceLocation, ResourceLocation> ids;

	private SwapLists(Map<String, String> namespaces, Map<ResourceLocation, ResourceLocation> ids) {
		this.namespaces = Collections.unmodifiableMap(namespaces);
		this.ids = Collections.unmodifiableMap(ids);
	}

	public static SwapLists get() {
		SwapLists l = loaded;
		if (l == null) {
			synchronized (SwapLists.class) {
				if (loaded == null) loaded = read();
				l = loaded;
			}
		}
		return l;
	}

	/** The id an old id now lives under, or null when no list names it. */
	public ResourceLocation target(ResourceLocation old) {
		ResourceLocation exact = ids.get(old);
		if (exact != null) return exact;
		String ns = namespaces.get(old.getNamespace());
		return ns == null ? null : new ResourceLocation(ns, old.getPath());
	}

	public boolean isEmpty() {
		return namespaces.isEmpty() && ids.isEmpty();
	}

	private static SwapLists read() {
		Map<String, String> namespaces = new HashMap<>();
		Map<ResourceLocation, ResourceLocation> ids = new HashMap<>();
		int files = 0;
		for (IModInfo mod : ModList.get().getMods()) {
			Path dir = mod.getOwningFile().getFile().findResource("data", mod.getModId(), "swaps");
			if (!Files.isDirectory(dir)) continue;
			List<Path> lists;
			try (Stream<Path> s = Files.list(dir)) {
				lists = s.filter(p -> p.toString().endsWith(".json")).sorted().toList();
			} catch (Exception e) {
				LOGGER.error("[swaps] cannot list {} in {}: {}", dir, mod.getModId(), e.toString());
				continue;
			}
			for (Path file : lists) {
				try (Reader r = Files.newBufferedReader(file)) {
					JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
					if (root.has("namespaces")) {
						for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("namespaces").entrySet()) {
							String prev = namespaces.put(e.getKey(), e.getValue().getAsString());
							if (prev != null && !prev.equals(e.getValue().getAsString()))
								LOGGER.warn("[swaps] namespace {} moved twice ({} and {}); {} wins", e.getKey(), prev, e.getValue(), e.getValue());
						}
					}
					if (root.has("replaces")) {
						for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("replaces").entrySet()) {
							ResourceLocation survivor = new ResourceLocation(e.getKey());
							for (JsonElement old : e.getValue().getAsJsonArray()) ids.put(new ResourceLocation(old.getAsString()), survivor);
						}
					}
					files++;
				} catch (Exception e) {
					LOGGER.error("[swaps] skipped {}:{}: {}", mod.getModId(), file.getFileName(), e.toString());
				}
			}
		}
		LOGGER.info("[swaps] {} list file(s): {} namespace move(s), {} single id(s)", files, namespaces.size(), ids.size());
		return new SwapLists(namespaces, ids);
	}
}
