package com.SaintAlfonzo.creativeengineering.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Biome allow-lists for structures: a rule names some biomes (the water biomes) and the only structures
 * that may start in them; every other structure keeps its own biome list minus those biomes. Mods give
 * structures blanket lists like #minecraft:is_overworld, which put pillager towers in the open ocean.
 *
 * DATA FLOW: structure_overrides.js resolves biome_rules in structure_groups.json (allow = a structure tag
 * from structure_tags.json) and stages {"rule": {"biomes": ["#tag" | "id"...], "allow": [structure id...]}}
 * in highPriorityData. At ServerAboutToStart the biome entries are resolved against the loaded registry
 * and tags, and frozen. Every registered structure that isn't allowed is covered, including ones from
 * mods added later. ServerStopped clears both.
 *
 * HOOK: mixin/ChunkGeneratorStructureBiomeRulesMixin wraps the valid-biome predicate that
 * ChunkGenerator#tryGenerateStructure hands to Structure#generate, the same check that rejects a
 * structure's start in a biome outside its list. /place structure passes its own predicate and is not
 * affected. /locate's "could it start here" prediction never checks biomes in 1.19.2, so it loads the
 * chunk and finds no start, the same as for any other wrong-biome slot.
 *
 * SURFACE CHECK: a rule's optional surface_check list names structures that start below ground but reach the
 * surface (YUNG's crypt staircase, the spider cave). Their start biome is read at the start's own height, so a
 * crypt at y 50 under a beach passes the biome check while its staircase comes up in the sea. For those, the
 * noise surface above the start's box (centre and corners) must be outside the rule's biomes and not under a fluid
 * (WORLD_SURFACE_WG above OCEAN_FLOOR_WG), or the start is dropped (mixin/ChunkGeneratorStructureBiomeRulesMixin).
 */
public final class StructureBiomeRules {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/structure-biome-rules");

	record Rule(List<String> biomes, Set<String> allow, Set<String> surfaceCheck) {
		Rule(List<String> biomes, Set<String> allow) { this(biomes, allow, Set.of()); }
	}

	/** Staged by structure_overrides.js at the latest datapack load. */
	private static volatile Map<String, Rule> staged = Map.of();
	/** Structure -> the biomes it may not start in. Identity maps: registry values are unique instances. */
	private static volatile Map<Structure, Set<Biome>> active = Map.of();
	/** Structure -> the biomes the surface above its start may not be in (surface_check). */
	private static volatile Map<Structure, Set<Biome>> surfaceActive = Map.of();

	private StructureBiomeRules() {}

	// ── staging (called from KubeJS) ─────────────────────────────────────────

	/** {"rule": {"biomes": [...], "allow": [...]}}; "{}" clears. Bad input stages nothing and is logged. */
	public static void stage(String json) {
		Map<String, Rule> rules = new LinkedHashMap<>();
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, JsonElement> e : root.entrySet()) {
				JsonObject r = e.getValue().getAsJsonObject();
				List<String> biomes = new ArrayList<>();
				for (JsonElement b : r.getAsJsonArray("biomes")) biomes.add(b.getAsString());
				Set<String> allow = new LinkedHashSet<>();
				for (JsonElement s : r.getAsJsonArray("allow")) allow.add(s.getAsString());
				Set<String> surface = new LinkedHashSet<>();
				if (r.has("surface_check")) for (JsonElement s : r.getAsJsonArray("surface_check")) surface.add(s.getAsString());
				if (biomes.isEmpty()) throw new IllegalArgumentException(e.getKey() + " lists no biomes");
				rules.put(e.getKey(), new Rule(biomes, allow, surface));
			}
		} catch (RuntimeException e) {
			LOG.error("[structure-biome-rules] bad rules from structure_overrides.js, staging nothing: {}", e.toString());
			rules.clear();
		}
		staged = rules;
	}

	// ── lifecycle ────────────────────────────────────────────────────────────

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		activate(event.getServer());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		active = Map.of();
		surfaceActive = Map.of();
		staged = Map.of();
	}

	static void activate(MinecraftServer server) {
		if (!WorldgenConfig.structureBiomeRulesEnabled()) {
			active = Map.of();
			surfaceActive = Map.of();
			LOG.info("[structure-biome-rules] off in CEConfig [worldgen.systems]: every structure uses its own biome list");
			return;
		}
		if (staged.isEmpty()) {
			active = Map.of();
			surfaceActive = Map.of();
			return;
		}
		Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<Structure> structures = server.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		Map<String, Structure> byId = new LinkedHashMap<>();
		for (Map.Entry<ResourceKey<Structure>, Structure> e : structures.entrySet()) byId.put(e.getKey().location().toString(), e.getValue());
		List<String> log = new ArrayList<>();
		Map<Structure, Set<Holder<Biome>>> holders = resolve(staged, entry -> biomeEntry(biomes, entry), byId,
				(structure, biome) -> structure.biomes().contains(biome), log);
		// the filter compares biome values: the holder a biome source hands over need not be the registry's own
		Map<Structure, Set<Biome>> table = new IdentityHashMap<>();
		holders.forEach((s, hs) -> {
			Set<Biome> values = Collections.newSetFromMap(new IdentityHashMap<>());
			hs.forEach(h -> values.add(h.value()));
			table.put(s, values);
		});
		active = table;
		Map<Structure, Set<Biome>> surface = new IdentityHashMap<>();
		for (Map.Entry<String, Rule> e : staged.entrySet()) {
			if (e.getValue().surfaceCheck().isEmpty()) continue;
			Set<Biome> values = Collections.newSetFromMap(new IdentityHashMap<>());
			for (String entry : e.getValue().biomes()) biomeEntry(biomes, entry).forEach(h -> values.add(h.value()));
			List<String> checked = new ArrayList<>();
			for (String id : e.getValue().surfaceCheck()) {
				Structure st = byId.get(id);
				if (st == null) continue;
				surface.computeIfAbsent(st, k -> Collections.newSetFromMap(new IdentityHashMap<>())).addAll(values);
				checked.add(id);
			}
			log.add("[structure-biome-rules] " + e.getKey() + ": surface check for " + String.join(", ", checked));
		}
		surfaceActive = surface;
		log.forEach(LOG::info);
	}

	/** One biome entry: "#tag" (empty with a warning if the tag doesn't exist) or a biome id. */
	private static Collection<Holder<Biome>> biomeEntry(Registry<Biome> biomes, String entry) {
		if (entry.startsWith("#")) {
			TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(entry.substring(1)));
			List<Holder<Biome>> out = new ArrayList<>();
			biomes.getTag(tag).ifPresentOrElse(set -> set.forEach(out::add),
					() -> LOG.warn("[structure-biome-rules] biome tag {} does not exist; skipped", entry));
			return out;
		}
		var holder = biomes.getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(entry)));
		if (holder.isEmpty()) LOG.warn("[structure-biome-rules] biome {} does not exist; skipped", entry);
		return holder.<Collection<Holder<Biome>>>map(List::of).orElse(List.of());
	}

	/**
	 * The pure part, generic so headless tests can use strings. Every structure that isn't allowed by a rule
	 * and could spawn in one of its biomes gets those biomes forbidden. Writes a summary per rule to log.
	 */
	static <S, B> Map<S, Set<B>> resolve(Map<String, Rule> rules, Function<String, Collection<B>> biomeEntry,
			Map<String, S> structures, BiPredicate<S, B> spawnsIn, List<String> log) {
		Map<S, Set<B>> out = new IdentityHashMap<>();
		for (Map.Entry<String, Rule> e : rules.entrySet()) {
			Rule rule = e.getValue();
			Set<B> ruleBiomes = Collections.newSetFromMap(new IdentityHashMap<>());
			for (String entry : rule.biomes()) ruleBiomes.addAll(biomeEntry.apply(entry));
			Set<String> keptOut = new TreeSet<>();
			int allowedHere = 0;
			for (Map.Entry<String, S> s : structures.entrySet()) {
				Set<B> hit = Collections.newSetFromMap(new IdentityHashMap<>());
				for (B b : ruleBiomes) if (spawnsIn.test(s.getValue(), b)) hit.add(b);
				if (hit.isEmpty()) continue;
				if (rule.allow().contains(s.getKey())) {
					allowedHere++;
					continue;
				}
				out.computeIfAbsent(s.getValue(), k -> Collections.newSetFromMap(new IdentityHashMap<>())).addAll(hit);
				keptOut.add(s.getKey());
			}
			log.add("[structure-biome-rules] " + e.getKey() + ": " + ruleBiomes.size() + " biomes, " + allowedHere
					+ " allowed structure(s) can start there, " + keptOut.size() + " kept out: " + String.join(", ", keptOut));
		}
		return out;
	}

	/** Headless tests: install a resolved table directly. */
	@SuppressWarnings("unchecked")
	static void setActiveForTest(Map<?, ?> table) {
		active = (Map<Structure, Set<Biome>>) table;
	}

	// ── mixin entry point ────────────────────────────────────────────────────

	/**
	 * ChunkGenerator#tryGenerateStructure, the generated start: INVALID_START when the structure has a surface check
	 * and the noise surface above its box's centre or a corner is in a rule's biomes or under a fluid.
	 */
	public static StructureStart surfaceCheck(StructureStart start, Structure structure, ChunkGenerator generator, RandomState random,
			LevelHeightAccessor height) {
		Set<Biome> forbidden = surfaceActive.get(structure);
		if (forbidden == null || !start.isValid()) return start;
		BoundingBox b = start.getBoundingBox();
		int[][] points = { { (b.minX() + b.maxX()) / 2, (b.minZ() + b.maxZ()) / 2 }, { b.minX(), b.minZ() }, { b.minX(), b.maxZ() },
				{ b.maxX(), b.minZ() }, { b.maxX(), b.maxZ() } };
		for (int[] p : points) {
			int top = generator.getBaseHeight(p[0], p[1], Heightmap.Types.WORLD_SURFACE_WG, height, random);
			int floor = generator.getBaseHeight(p[0], p[1], Heightmap.Types.OCEAN_FLOOR_WG, height, random);
			Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(p[0]), QuartPos.fromBlock(top), QuartPos.fromBlock(p[1]),
					random.sampler());
			if (top > floor || forbidden.contains(biome.value())) {
				LOG.debug("[structure-biome-rules] surface check dropped a start at {} {}: surface at y {} is {}", p[0], p[1], top,
						top > floor ? "under a fluid" : "in a rule biome");
				return StructureStart.INVALID_START;
			}
		}
		return start;
	}

	/** ChunkGenerator#tryGenerateStructure: the structure's valid-biome check, minus the biomes a rule keeps it out of. */
	public static Predicate<Holder<Biome>> filter(Structure structure, Predicate<Holder<Biome>> valid) {
		Set<Biome> forbidden = active.get(structure);
		if (forbidden == null) return valid;
		return holder -> valid.test(holder) && !forbidden.contains(holder.value());
	}
}
