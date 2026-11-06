package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Biome fits: a structure's own biome list replaced by lists we choose (biome_fits.json). A fit is a named biome
 * list (land/oak) and the structure tag of the same name; a structure in the tag spawns in its fit's biomes plus the
 * parent fits' (land), never a sibling's. Mods list vanilla biomes only (no Terralith) or every overworld biome (an
 * oak pillager tower in a dark-oak biome).
 *
 * DATA FLOW: structure_overrides.js resolves the fits and their parents and stages
 * {"fits": {"land/oak": [listed biome entries]}, "structures": {"<structure id>": ["land/oak", "land"]}} in
 * highPriorityData; it also adds every fit as the real biome tag creativeengineering:<fit>. At ServerAboutToStart
 * (HIGHEST, so StructureBiomeRules and everything after see the new lists) each fit resolves to that biome tag
 * (so a datapack adding to the tag widens the fit); a fit whose tag is missing falls back to the listed entries.
 * Listed ids the registry lacks are logged. ServerStopped clears everything.
 *
 * HOOK: mixin/StructureBiomesMixin answers Structure#biomes() from this table. That one method is what every
 * consumer reads: ChunkGenerator's valid-biome check and /locate's placement table, StructureCheck,
 * MetaStructure's parts and the biome rules. Forge 43's structure modifiers can't do this: their modified
 * settings are never read by vanilla code in 1.19.2.
 */
public final class StructureBiomeFits {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/structure-biome-fits");
	private static final String NS = "creativeengineering";

	record Staged(Map<String, List<String>> fits, Map<String, List<String>> structures) {
		static final Staged EMPTY = new Staged(Map.of(), Map.of());
	}

	/** Staged by structure_overrides.js at the latest datapack load. */
	private static volatile Staged staged = Staged.EMPTY;
	/** Structure -> its fitted biome list. Identity map: registry values are unique instances. */
	private static volatile Map<Structure, HolderSet<Biome>> active = Map.of();

	private StructureBiomeFits() {}

	// ── staging (called from KubeJS) ─────────────────────────────────────────

	/** {"fits": {...}, "structures": {...}}; "{}" clears. Bad input stages nothing and is logged. */
	public static void stage(String json) {
		Map<String, List<String>> fits = new LinkedHashMap<>();
		Map<String, List<String>> structures = new LinkedHashMap<>();
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			if (root.has("fits")) for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("fits").entrySet()) {
				List<String> biomes = new ArrayList<>();
				for (JsonElement b : e.getValue().getAsJsonArray()) biomes.add(b.getAsString());
				fits.put(e.getKey(), biomes);
			}
			if (root.has("structures")) for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("structures").entrySet()) {
				List<String> names = new ArrayList<>();
				for (JsonElement f : e.getValue().getAsJsonArray()) {
					String name = f.getAsString();
					if (!fits.containsKey(name)) throw new IllegalArgumentException(e.getKey() + " names unknown fit " + name);
					names.add(name);
				}
				if (names.isEmpty()) throw new IllegalArgumentException(e.getKey() + " names no fit");
				structures.put(e.getKey(), names);
			}
		} catch (RuntimeException e) {
			LOG.error("[structure-biome-fits] bad fits from structure_overrides.js, staging nothing: {}", e.toString());
			fits.clear();
			structures.clear();
		}
		staged = new Staged(fits, structures);
	}

	// ── lifecycle ────────────────────────────────────────────────────────────

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		activate(event.getServer());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		active = Map.of();
		staged = Staged.EMPTY;
	}

	static void activate(MinecraftServer server) {
		active = Map.of();
		if (!WorldgenConfig.structureBiomeFitsEnabled()) {
			LOG.info("[structure-biome-fits] off in CWGConfig [worldgen.systems]: every structure uses its own biome list");
			return;
		}
		Staged s = staged;
		if (s.structures().isEmpty()) return;
		Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<Structure> structures = server.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		Map<String, Structure> byId = new LinkedHashMap<>();
		for (Map.Entry<ResourceKey<Structure>, Structure> e : structures.entrySet()) byId.put(e.getKey().location().toString(), e.getValue());
		List<String> log = new ArrayList<>();
		Map<String, Collection<Holder<Biome>>> fitBiomes = new LinkedHashMap<>();
		s.fits().forEach((name, listed) -> fitBiomes.put(name, fitEntry(biomes, name, listed, log)));
		Map<Structure, Set<Holder<Biome>>> table = resolve(s.structures(), fitBiomes, byId, structure -> structure.biomes().size(), log);
		Map<Structure, HolderSet<Biome>> out = new IdentityHashMap<>();
		table.forEach((structure, hs) -> out.put(structure, new FitBiomes(new ArrayList<>(hs))));
		active = out;
		log.forEach(LOG::info);
	}

	/** One fit's biomes: the real tag creativeengineering:<fit>, else (tag missing) its listed entries. Logs unknown ids. */
	private static Collection<Holder<Biome>> fitEntry(Registry<Biome> biomes, String fit, List<String> listed, List<String> log) {
		if (listed.isEmpty()) return List.of();   // an empty fit (land) gets no biome tag
		List<String> unknown = new ArrayList<>();
		for (String entry : listed) {
			if (!entry.startsWith("#") && !biomes.containsKey(new ResourceLocation(entry))) unknown.add(entry);
		}
		if (!unknown.isEmpty()) log.add("[structure-biome-fits] " + fit + ": no such biome, skipped: " + String.join(", ", unknown));
		TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(NS, fit));
		var named = biomes.getTag(tag);
		if (named.isPresent()) {
			List<Holder<Biome>> out = new ArrayList<>();
			named.get().forEach(out::add);
			return out;
		}
		log.add("[structure-biome-fits] " + fit + ": biome tag #" + tag.location() + " is missing (structure_overrides.js tags handler?), using the listed biomes");
		Set<Holder<Biome>> out = new LinkedHashSet<>();
		for (String entry : listed) {
			if (entry.startsWith("#")) {
				biomes.getTag(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(entry.substring(1))))
						.ifPresent(set -> set.forEach(out::add));
			} else {
				biomes.getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(entry))).ifPresent(out::add);
			}
		}
		return out;
	}

	/**
	 * The pure part, generic so headless tests can use strings: each staged structure that is registered gets the union
	 * of its fits' biomes (fit order, no duplicates). Unregistered ids and empty results are logged; an empty list is
	 * still applied (the structure then never spawns, which is what an empty fit says). oldSize = its own list's size.
	 */
	static <S, B> Map<S, Set<B>> resolve(Map<String, List<String>> structureFits, Map<String, ? extends Collection<B>> fitBiomes,
			Map<String, S> structures, Function<S, Integer> oldSize, List<String> log) {
		Map<S, Set<B>> out = new IdentityHashMap<>();
		Set<String> missing = new TreeSet<>();
		List<String> applied = new ArrayList<>();
		for (Map.Entry<String, List<String>> e : structureFits.entrySet()) {
			S structure = structures.get(e.getKey());
			if (structure == null) {
				missing.add(e.getKey());
				continue;
			}
			Set<B> union = new LinkedHashSet<>();
			for (String fit : e.getValue()) {
				Collection<B> biomes = fitBiomes.get(fit);
				if (biomes != null) union.addAll(biomes);
			}
			if (union.isEmpty()) log.add("[structure-biome-fits] " + e.getKey() + ": its fits (" + String.join(", ", e.getValue())
					+ ") hold no biome, so it never spawns");
			out.put(structure, union);
			applied.add(e.getKey() + " " + oldSize.apply(structure) + " -> " + union.size());
		}
		log.add("[structure-biome-fits] " + fitBiomes.size() + " fit(s), " + applied.size() + " structure(s) given fitted biome lists: "
				+ String.join(", ", applied));
		if (!missing.isEmpty()) log.add("[structure-biome-fits] not registered, skipped: " + String.join(", ", missing));
		return out;
	}

	/** Headless tests: what the last stage() kept. */
	static Staged stagedForTest() {
		return staged;
	}

	/** Headless tests: install a table directly. */
	@SuppressWarnings("unchecked")
	static void setActiveForTest(Map<?, ?> table) {
		active = (Map<Structure, HolderSet<Biome>>) table;
	}

	// ── mixin entry point ────────────────────────────────────────────────────

	/** Structure#biomes(): the fitted list, or null for the structure's own. */
	public static HolderSet<Biome> biomesFor(Structure structure) {
		Map<Structure, HolderSet<Biome>> table = active;
		return table.isEmpty() ? null : table.get(structure);
	}

	/**
	 * A fitted biome list. Membership compares biome values, not holders: the holder a biome source hands over need
	 * not be the registry's own instance (same reason as StructureBiomeRules), and HolderSet.Direct compares holders.
	 */
	static final class FitBiomes extends HolderSet.ListBacked<Biome> {
		private final List<Holder<Biome>> contents;
		private final Set<Biome> values = Collections.newSetFromMap(new IdentityHashMap<>());

		FitBiomes(List<Holder<Biome>> contents) {
			this.contents = List.copyOf(contents);
			for (Holder<Biome> h : this.contents) values.add(h.value());
		}

		@Override
		protected List<Holder<Biome>> contents() { return contents; }

		@Override
		public Either<TagKey<Biome>, List<Holder<Biome>>> unwrap() { return Either.right(contents); }

		@Override
		public boolean contains(Holder<Biome> holder) { return holder.isBound() && values.contains(holder.value()); }

		@Override
		public String toString() { return "FitBiomes[" + contents.size() + "]"; }
	}
}
