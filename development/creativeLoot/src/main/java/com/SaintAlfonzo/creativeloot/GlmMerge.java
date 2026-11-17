package com.SaintAlfonzo.creativeloot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * One global loot modifier list for the whole pack (plan_rules_fixes.md section 5, D14; feature_writeup/creative_loot.md).
 *
 * Forge 1.19.2's {@code LootModifierManager.apply} reads every copy of
 * {@code data/forge/loot_modifiers/global_loot_modifiers.json} in the resource stack, but a copy with
 * {@code "replace": true} clears every entry read before it, and the stack order is the mod load order, which nobody
 * chooses. The pack assumes the worst case (only one copy counts) and merges after Forge, on the same reload
 * (mixin/LootModifierManagerMixin, at the end of {@code apply}): every entry of (a) every copy of the Forge list and
 * (b) every fragment {@code data/<namespace>/glm_entries.json} ({@code {"entries": ["ns:name", ...]}}, the form the
 * pack's own jars use instead of the Forge list) that Forge's map does not hold yet is decoded from its definition
 * {@code data/<ns>/loot_modifiers/<name>.json} and appended, in list order then fragment order.
 *
 * Skipped: an entry whose definition is missing, whose serializer ({@code "type"}) is not registered (its mod is gone),
 * or that fails to decode; and a fragment entry whose fragment namespace a registered {@link #gate} turns off (a member
 * whose switch is off, D17). Duplicates are dropped by entry id. One log line per reload:
 * {@code [GlmMerge] N entries (lists: ..., fragments: ...)}.
 */
public final class GlmMerge {

	private static final Logger LOG = LogManager.getLogger("creativeloot/GlmMerge");
	private static final ResourceLocation FORGE_LIST = new ResourceLocation("forge", "loot_modifiers/global_loot_modifiers.json");
	private static final String FRAGMENT = "glm_entries.json";
	private static final String DEFINITIONS = "loot_modifiers/";

	/** Fragment gates: a fragment of namespace ns is read only when every gate accepts ns. */
	private static final List<Predicate<String>> GATES = new CopyOnWriteArrayList<>();

	private GlmMerge() {}

	/**
	 * Adds a gate on fragments by namespace (the fragment's {@code data/<namespace>/}). A jar with member switches
	 * registers one in its constructor, for example Creative Delight: {@code ns -> !CDConfig.isMember(ns) || CDConfig.member(ns)}.
	 */
	public static void gate(Predicate<String> namespaceOn) {
		GATES.add(namespaceOn);
	}

	/**
	 * The merged modifier map: Forge's {@code loaded} (kept first, in its order) plus every listed or fragment entry it
	 * lacks. {@code definitions} is the manager's own map of {@code loot_modifiers/} files (id {@code ns:name}).
	 */
	public static Map<ResourceLocation, IGlobalLootModifier> merge(Map<ResourceLocation, IGlobalLootModifier> loaded,
			Map<ResourceLocation, JsonElement> definitions, ResourceManager resources) {
		Map<ResourceLocation, IGlobalLootModifier> out = new LinkedHashMap<>(loaded);
		Set<ResourceLocation> listed = new LinkedHashSet<>();
		Set<ResourceLocation> fragmentEntries = new LinkedHashSet<>();
		int lists = 0;
		int fragments = 0;
		int gated = 0;
		List<String> skipped = new ArrayList<>();
		try {
			for (Resource r : resources.getResourceStack(FORGE_LIST)) {
				lists++;
				readEntries(r, FORGE_LIST + " in " + r.sourcePackId(), listed);
			}
			for (String ns : new TreeSet<>(resources.getNamespaces())) {
				ResourceLocation frag = new ResourceLocation(ns, FRAGMENT);
				List<Resource> stack = resources.getResourceStack(frag);
				if (stack.isEmpty()) continue;
				if (!open(ns)) {
					gated += stack.size();
					continue;
				}
				for (Resource r : stack) {
					fragments++;
					readEntries(r, frag + " in " + r.sourcePackId(), fragmentEntries);
				}
			}
		} catch (RuntimeException e) {
			LOG.error("[GlmMerge] could not read the modifier lists, Forge's {} entries kept", loaded.size(), e);
			return loaded;
		}
		Set<ResourceLocation> wanted = new LinkedHashSet<>(listed);
		wanted.addAll(fragmentEntries);
		int added = 0;
		for (ResourceLocation id : wanted) {
			if (out.containsKey(id)) continue;
			IGlobalLootModifier m = decode(id, definitions.get(id), skipped);
			if (m == null) continue;
			out.put(id, m);
			added++;
		}
		LOG.info("[GlmMerge] {} entries (lists: {} files, {} entries; fragments: {} files, {} entries, {} gated off; Forge read {}, added {}, skipped {})",
				out.size(), lists, listed.size(), fragments, fragmentEntries.size(), gated, loaded.size(), added, skipped.size());
		if (!skipped.isEmpty()) LOG.info("[GlmMerge] skipped: {}", String.join(", ", skipped));
		return ImmutableMap.copyOf(out);
	}

	private static boolean open(String ns) {
		for (Predicate<String> g : GATES) {
			try {
				if (!g.test(ns)) return false;
			} catch (RuntimeException e) {
				LOG.warn("[GlmMerge] a fragment gate failed on {}, fragment read", ns, e);
			}
		}
		return true;
	}

	private static void readEntries(Resource r, String where, Set<ResourceLocation> into) {
		try (Reader reader = new BufferedReader(new InputStreamReader(r.open(), StandardCharsets.UTF_8))) {
			JsonElement root = JsonParser.parseReader(reader);
			JsonArray entries = root.isJsonObject() && root.getAsJsonObject().has("entries")
					? root.getAsJsonObject().getAsJsonArray("entries") : null;
			if (entries == null) {
				LOG.warn("[GlmMerge] {} has no entries array, skipped", where);
				return;
			}
			for (JsonElement e : entries) {
				ResourceLocation id = ResourceLocation.tryParse(e.getAsString());
				if (id == null) LOG.warn("[GlmMerge] {}: bad entry \"{}\", skipped", where, e);
				else into.add(id);
			}
		} catch (Exception e) {
			LOG.warn("[GlmMerge] could not read {}", where, e);
		}
	}

	private static IGlobalLootModifier decode(ResourceLocation id, JsonElement json, List<String> skipped) {
		if (json == null || !json.isJsonObject()) {
			skipped.add(id + " (no " + id.getNamespace() + ":" + DEFINITIONS + id.getPath() + ".json)");
			return null;
		}
		JsonObject obj = json.getAsJsonObject();
		ResourceLocation type = obj.has("type") ? ResourceLocation.tryParse(obj.get("type").getAsString()) : null;
		if (type == null || !ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().containsKey(type)) {
			skipped.add(id + " (serializer " + type + " not registered)");
			return null;
		}
		return IGlobalLootModifier.DIRECT_CODEC.parse(JsonOps.INSTANCE, json)
				.resultOrPartial(err -> skipped.add(id + " (" + err + ")"))
				.orElse(null);
	}
}
