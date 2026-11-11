package com.SaintAlfonzo.creativecommon.swap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Swap lists (feature_writeup/creative_delight.md 11, creative_split.md 7.6): the retained record of every id a Creative
 * jar replaced. Each loaded mod may ship {@code data/<modid>/swaps/*.json} in its own jar:
 * <pre>
 * {
 *   "namespaces": { "old_namespace": "newnamespace" },
 *   "replaces":   { "newmod:thing": ["oldmod:thing", "oldmod:other_thing"] },
 *   "removed":    ["oldmod:dropped_thing"],
 *   "merges":     { "mod:survivor": ["othermod:purged"] },
 *   "requires":   ["othermod", "mod"],
 *   "capabilities": { "newmod:cap": ["oldmod:cap"] }
 * }
 * </pre>
 * {@code namespaces} moves every id of the old namespace to the same path in the new one; {@code replaces} names single
 * ids and wins over a namespace rule. {@code removed} names ids dropped with no replacement: an old world loads without the
 * missing-registry warning and those blocks and items are left out (Forge's ignore action), never converted. {@code merges}
 * (item merges, item_merges.md 6) name two items that are both still registered: the purged one gives way to the survivor in
 * recipes, loot and tags ({@link #scriptMerges}, read by a jar's startup script) and in every stack in the world
 * ({@link ItemMerges}); like {@code replaces} they also remap the purged id once it is no longer registered. A file's
 * {@code requires} (mod ids) gates its merges: they apply only while every listed mod is loaded. {@code capabilities} renames
 * saved capability keys ({@link #renameCapabilityKeys}). The lists are never pruned, so a world last saved on any older version can still be
 * read. Read once, from the jars (not from datapacks: {@link SwapRemapper} needs them before any datapack loads).
 */
public final class SwapLists {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static volatile SwapLists loaded;

	private final Map<String, String> namespaces;
	private final Map<ResourceLocation, ResourceLocation> ids;
	private final Set<ResourceLocation> removed;
	private final List<Merge> merges;
	private final Map<ResourceLocation, ResourceLocation> replaces;
	private final Map<String, String> capabilities;

	/** One item merge: {@code old} gives way to {@code survivor}; listed by {@code modId}, applied while {@code requires} are loaded. */
	public record Merge(String modId, ResourceLocation old, ResourceLocation survivor, List<String> requires) {
		public boolean active() {
			for (String id : requires) if (!ModList.get().isLoaded(id)) return false;
			return true;
		}
	}

	private SwapLists(Map<String, String> namespaces, Map<ResourceLocation, ResourceLocation> ids, Set<ResourceLocation> removed, List<Merge> merges,
			Map<ResourceLocation, ResourceLocation> replaces, Map<String, String> capabilities) {
		this.namespaces = Collections.unmodifiableMap(namespaces);
		this.ids = Collections.unmodifiableMap(ids);
		this.removed = Collections.unmodifiableSet(removed);
		this.merges = Collections.unmodifiableList(merges);
		this.replaces = Collections.unmodifiableMap(replaces);
		this.capabilities = Collections.unmodifiableMap(capabilities);
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

	/** True when a list names this id as dropped with no replacement (checked only when {@link #target} is null). */
	public boolean isRemoved(ResourceLocation old) {
		return removed.contains(old);
	}

	/** Every item merge of every jar, in load order (mods, then file names, then list order). */
	public List<Merge> merges() {
		return merges;
	}

	/**
	 * The active merges of one jar's lists as old id -> survivor id strings, list order: what that jar's startup script hands
	 * to {@code global.addItemMerges}. Called from KubeJS startup scripts, before the registries fill, so it checks only
	 * {@code requires}, not whether the items exist.
	 */
	public Map<String, String> scriptMerges(String modId) {
		Map<String, String> out = new LinkedHashMap<>();
		for (Merge m : merges) if (m.modId().equals(modId) && m.active()) out.put(m.old().toString(), m.survivor().toString());
		return out;
	}

	/** The {@code replaces} renames alone, old id -> survivor: what {@link SwapRemapper} keeps as permanent aliases. */
	public Map<ResourceLocation, ResourceLocation> replaces() {
		return replaces;
	}

	/**
	 * Renames saved capability keys in a {@code ForgeCaps} tag to their new ids, in place, before Forge hands the tag to the
	 * providers. A new key that is already there wins and the old one is dropped.
	 */
	public void renameCapabilityKeys(CompoundTag caps) {
		if (caps == null || capabilities.isEmpty() || caps.isEmpty()) return;
		for (Map.Entry<String, String> e : capabilities.entrySet()) {
			Tag old = caps.get(e.getKey());
			if (old == null) continue;
			caps.remove(e.getKey());
			if (!caps.contains(e.getValue())) caps.put(e.getValue(), old);
		}
	}

	public boolean isEmpty() {
		return namespaces.isEmpty() && ids.isEmpty() && removed.isEmpty();
	}

	private static SwapLists read() {
		Map<String, String> namespaces = new HashMap<>();
		Map<ResourceLocation, ResourceLocation> ids = new HashMap<>();
		Set<ResourceLocation> removed = new HashSet<>();
		List<Merge> merges = new ArrayList<>();
		Set<ResourceLocation> merged = new HashSet<>();
		Map<ResourceLocation, ResourceLocation> replaces = new HashMap<>();
		Map<String, String> capabilities = new HashMap<>();
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
							for (JsonElement old : e.getValue().getAsJsonArray()) {
								ids.put(new ResourceLocation(old.getAsString()), survivor);
								replaces.put(new ResourceLocation(old.getAsString()), survivor);
							}
						}
					}
					if (root.has("capabilities")) {
						for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("capabilities").entrySet()) {
							for (JsonElement old : e.getValue().getAsJsonArray()) capabilities.put(old.getAsString(), e.getKey());
						}
					}
					if (root.has("removed")) {
						for (JsonElement old : root.getAsJsonArray("removed")) removed.add(new ResourceLocation(old.getAsString()));
					}
					if (root.has("merges")) {
						List<String> requires = new ArrayList<>();
						if (root.has("requires")) for (JsonElement req : root.getAsJsonArray("requires")) requires.add(req.getAsString());
						for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("merges").entrySet()) {
							ResourceLocation survivor = new ResourceLocation(e.getKey());
							for (JsonElement o : e.getValue().getAsJsonArray()) {
								ResourceLocation old = new ResourceLocation(o.getAsString());
								if (!merged.add(old)) {
									LOGGER.warn("[swaps] {} is merged twice; {}:{} entry skipped", old, mod.getModId(), file.getFileName());
									continue;
								}
								merges.add(new Merge(mod.getModId(), old, survivor, List.copyOf(requires)));
								ids.putIfAbsent(old, survivor);
							}
						}
					}
					files++;
				} catch (Exception e) {
					LOGGER.error("[swaps] skipped {}:{}: {}", mod.getModId(), file.getFileName(), e.toString());
				}
			}
		}
		removed.removeAll(ids.keySet());
		LOGGER.info("[swaps] {} list file(s): {} namespace move(s), {} single id(s), {} removed id(s), {} item merge(s), {} capability key(s)", files, namespaces.size(), ids.size(), removed.size(), merges.size(), capabilities.size());
		return new SwapLists(namespaces, ids, removed, merges, replaces, capabilities);
	}
}
