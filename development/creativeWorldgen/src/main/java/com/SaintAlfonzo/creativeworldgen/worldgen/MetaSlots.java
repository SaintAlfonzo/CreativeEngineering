package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Where a meta structure with an anchor comes from: a group slot one of its anchors has already won.
 *
 * SLOT CONVERSION: vanilla fills a group slot by its weighted pick; when the pick lands on an anchor X (a ruin, a
 * camp, a village) and X's own frequency roll keeps it, the slot becomes meta M with chance M's "share" (a hash
 * roll of seed, chunk and X). If M can't be assembled (too few parts fit), the slot is plain X after all. The
 * meta's own entry in the set is never picked directly. So every anchor keeps exactly its rate, and the slot
 * count of every group (ruins vs villages vs pillagers) is unchanged: a meta only adds structures around an anchor
 * that was going to spawn anyway. Several metas may share an anchor; their shares stack (cut at 1, in id order).
 *
 * /LOCATE: StructureCheck predicts a converted meta cheaply (one of its anchors fits here, keeps its frequency and
 * its conversion roll lands on this meta) instead of assembling it; the chunk load that follows confirms.
 * A search for a structure that metas can contain also searches those metas, and a hit reports the part's own
 * position (the tower beside the village, not the village's start chunk). Explorer maps, dolphins and Async Locator
 * all go through the same search. Only targets that can spawn in the searched dimension bring their metas in, and a
 * meta slot is only loaded when an asked-for structure could be in it (structure_tuning.md 7.3.2).
 *
 * REPLACEMENTS: the same conversion with a plain structure as the target. A group member with "replaces": X,
 * "share": s in structure_groups.json (staged by structure_overrides.js, {@link #stageReplacements}) takes share s
 * of the slots X wins and keeps, like a meta does (the vanilla swamp hut in YUNG's witch hut slots). It is never
 * picked by its own weight. It may only start where X's biomes (with the biome rules) also allow X
 * ({@link #narrowBiomes}); if it can't start, the slot is plain X. So X's slots are split, never added to.
 *
 * Mixins: ChunkGeneratorStructureFrequencyMixin (the conversion, at tryGenerateStructure HEAD),
 * ChunkGeneratorStructureBiomeRulesMixin (a replacement's biomes), StructureCheckMetaMixin (the prediction),
 * ChunkGeneratorMetaLocateMixin (the search).
 */
public final class MetaSlots {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/meta-structures");

	private MetaSlots() {}

	// ── the index: which metas each anchor can turn into ─────────────────────

	/** One structure (a meta or a replacement) an anchor can become: the share interval [from, to) of that anchor's conversion roll. */
	record Conversion(Structure target, StructureSet.StructureSelectionEntry entry, double from, double to) {}

	/** A staged replacement: takes {@code share} of the slots {@code anchor} wins in their common set. */
	record Replacement(String anchor, double share) {}

	static final class Index {
		final Map<Structure, List<Conversion>> byAnchor = new IdentityHashMap<>();
		/** Every converted meta or replacement and its anchors inside its own set(s), in set order. */
		final Map<Structure, List<Structure>> anchorsOf = new IdentityHashMap<>();
		int replacements;
		final Map<Structure, Integer> rollHash = new IdentityHashMap<>();
		final List<String> warnings = new ArrayList<>();
	}

	static Index build(Iterable<StructureSet> sets, Function<Structure, String> idOf, Function<MetaStructure, List<Structure>> anchorCandidates) {
		return build(sets, idOf, anchorCandidates, s -> null, id -> null);
	}

	/**
	 * Builds the index from the loaded structure sets. A meta with an anchor converts the members of its own set
	 * that its anchor entries name, and so does a replacement (replacementOf gives its anchor id and share,
	 * structureOf looks the id up); anchors outside that set can't be converted (nothing would pick them there).
	 */
	static Index build(Iterable<StructureSet> sets, Function<Structure, String> idOf, Function<MetaStructure, List<Structure>> anchorCandidates,
			Function<Structure, Replacement> replacementOf, Function<String, Structure> structureOf) {
		Index index = new Index();
		Map<Structure, List<Conversion>> raw = new IdentityHashMap<>();
		for (StructureSet set : sets) {
			List<Structure> members = new ArrayList<>();
			for (StructureSet.StructureSelectionEntry e : set.structures()) members.add(e.structure().value());
			for (StructureSet.StructureSelectionEntry e : set.structures()) {
				Structure target = e.structure().value();
				List<Structure> candidates;
				double share;
				Replacement r = replacementOf.apply(target);
				if (target instanceof MetaStructure meta && meta.anchored()) {
					candidates = anchorCandidates.apply(meta);
					share = meta.share();
				} else if (r != null) {
					Structure a = structureOf.apply(r.anchor());
					candidates = a == null ? List.of() : List.of(a);
					share = r.share();
				} else continue;
				List<Structure> anchors = new ArrayList<>();
				for (Structure c : candidates) {
					if (c != target && containsSame(members, c) && !containsSame(anchors, c)) anchors.add(c);
				}
				List<Structure> known = index.anchorsOf.computeIfAbsent(target, k -> new ArrayList<>());
				for (Structure a : anchors) {
					if (containsSame(known, a)) continue;
					known.add(a);
					raw.computeIfAbsent(a, k -> new ArrayList<>()).add(new Conversion(target, e, 0, share));
				}
				if (r != null && !(target instanceof MetaStructure)) index.replacements++;
				if (anchors.isEmpty()) index.warnings.add(idOf.apply(target) + (r != null && !(target instanceof MetaStructure)
						? ": replaces " + r.anchor() + ", which is not in its set, so it never generates"
						: ": none of its anchors is in its set, so it never generates"));
			}
		}
		for (Map.Entry<Structure, List<Conversion>> e : raw.entrySet()) {
			List<Conversion> list = new ArrayList<>(e.getValue());
			list.sort(Comparator.comparing(c -> idOf.apply(c.target())));
			List<Conversion> out = new ArrayList<>();
			double at = 0;
			for (Conversion c : list) {
				double to = Math.min(1.0, at + c.to());
				if (to > at) out.add(new Conversion(c.target(), c.entry(), at, to));
				at = to;
			}
			double total = list.stream().mapToDouble(Conversion::to).sum();
			if (total > 1.0 + 1e-9) index.warnings.add(idOf.apply(e.getKey()) + ": the metas and replacements it anchors share " + Math.round(total * 100)
					+ "% of its slots; cut to 100% in id order");
			index.byAnchor.put(e.getKey(), List.copyOf(out));
			index.rollHash.put(e.getKey(), ("meta-share:" + idOf.apply(e.getKey())).hashCode());
		}
		return index;
	}

	private static boolean containsSame(List<Structure> list, Structure s) {
		for (Structure x : list) if (x == s) return true;
		return false;
	}

	private static volatile WeakReference<Registry<StructureSet>> builtFor = new WeakReference<>(null);
	private static volatile Index current = new Index();
	/** Staged by structure_overrides.js at the latest datapack load: replacement id -> its anchor and share. */
	private static volatile Map<String, Replacement> stagedReplacements = Map.of();

	/** {"replacement id": {"replaces": "anchor id", "share": 0..1}}; "{}" clears. Bad input stages nothing and is logged. */
	public static void stageReplacements(String json) {
		Map<String, Replacement> table = new java.util.LinkedHashMap<>();
		try {
			com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, com.google.gson.JsonElement> e : root.entrySet()) {
				com.google.gson.JsonObject o = e.getValue().getAsJsonObject();
				String anchor = o.get("replaces").getAsString();
				double share = o.get("share").getAsDouble();
				if (!(share >= 0 && share <= 1)) throw new IllegalArgumentException(e.getKey() + " share " + share + " outside 0..1");
				if (anchor.equals(e.getKey())) throw new IllegalArgumentException(e.getKey() + " replaces itself");
				table.put(e.getKey(), new Replacement(anchor, share));
			}
		} catch (RuntimeException e) {
			LOG.error("[meta-structures] bad replacement table from structure_overrides.js, staging none: {}", e.toString());
			table.clear();
		}
		stagedReplacements = table;
		synchronized (MetaSlots.class) {
			builtFor = new WeakReference<>(null);   // rebuilt against the new table on next use
		}
	}

	/** The index for these registries (one per world load; built on first use, when tags are bound). */
	static Index index(RegistryAccess access) {
		Registry<StructureSet> sets = access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		if (builtFor.get() == sets) return current;
		synchronized (MetaSlots.class) {
			if (builtFor.get() == sets) return current;
			Registry<Structure> structures = access.registryOrThrow(Registry.STRUCTURE_REGISTRY);
			Map<String, Replacement> replacements = stagedReplacements;
			Function<Structure, String> idOf = s -> {
				ResourceLocation id = structures.getKey(s);
				return id != null ? id.toString() : String.valueOf(s);
			};
			for (String id : replacements.keySet()) {
				ResourceLocation key = ResourceLocation.tryParse(id);
				if (key == null || !structures.containsKey(key)) LOG.warn("[meta-structures] replacement {} is not a loaded structure; ignored", id);
			}
			Index index = build(sets, idOf, m -> m.anchorCandidates(structures),
					s -> replacements.get(idOf.apply(s)),
					id -> {
						ResourceLocation key = ResourceLocation.tryParse(id);
						return key == null ? null : structures.get(key);
					});
			index.warnings.forEach(w -> LOG.warn("[meta-structures] {}", w));
			int metas = index.anchorsOf.size() - index.replacements;
			if (metas > 0) LOG.info("[meta-structures] {} meta structure(s) take slots from {} anchor structure(s)", metas, index.byAnchor.size());
			if (index.replacements > 0) LOG.info("[meta-structures] {} replacement(s) take a share of another structure's slots", index.replacements);
			current = index;
			builtFor = new WeakReference<>(sets);
			return index;
		}
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		synchronized (MetaSlots.class) {
			current = new Index();
			builtFor = new WeakReference<>(null);
			stagedReplacements = Map.of();
		}
	}

	/** Headless tests. */
	static void setIndexForTest(Index index) {
		current = index;
		builtFor = new WeakReference<>(null);
	}

	/** Which meta or replacement (or none) the anchor's slot at that chunk becomes. */
	static Conversion conversion(Index index, Structure anchor, long seed, int chunkX, int chunkZ) {
		List<Conversion> list = index.byAnchor.get(anchor);
		if (list == null || list.isEmpty()) return null;
		double u = StructureFrequencies.roll(seed, chunkX, chunkZ, index.rollHash.get(anchor));
		for (Conversion c : list) if (u >= c.from() && u < c.to()) return c;
		return null;
	}

	// ── generation: tryGenerateStructure HEAD ────────────────────────────────

	/**
	 * What tryGenerateStructure does with an entry: as usual, nothing (a converted meta or replacement picked
	 * directly), or the conversion target first.
	 */
	public record Decision(boolean skip, StructureSet.StructureSelectionEntry targetEntry) {
		static final Decision PROCEED = new Decision(false, null);
		static final Decision SKIP = new Decision(true, null);
		public boolean convert() { return targetEntry != null; }
	}

	private record Forced(Structure target, Structure anchor, int chunkX, int chunkZ) {}
	private static final ThreadLocal<Forced> FORCED = new ThreadLocal<>();

	public static Decision decide(StructureSet.StructureSelectionEntry entry, RegistryAccess access, long seed, ChunkPos pos) {
		if (PlacementPriority.companionMode()) return Decision.PROCEED;   // a companion is itself, never a conversion
		return decide(index(access), entry.structure().value(), seed, pos.x, pos.z);
	}

	static Decision decide(Index index, Structure s, long seed, int chunkX, int chunkZ) {
		if (index.anchorsOf.containsKey(s)) {
			Forced f = FORCED.get();
			return f != null && f.target == s && f.chunkX == chunkX && f.chunkZ == chunkZ ? Decision.PROCEED : Decision.SKIP;
		}
		Conversion c = conversion(index, s, seed, chunkX, chunkZ);
		if (c == null || (c.target() instanceof MetaStructure && !WorldgenConfig.metaStructuresEnabled())) return Decision.PROCEED;
		if (!StructureFrequencies.keepsAt(s, seed, chunkX, chunkZ)) return Decision.PROCEED;   // a rejected anchor: the plain path vetoes
		return new Decision(false, c.entry());
	}

	/** Around the target's own tryGenerateStructure: it takes this anchor's slot (a meta builds on it). */
	public static void beginConversion(StructureSet.StructureSelectionEntry targetEntry, StructureSet.StructureSelectionEntry anchorEntry, ChunkPos pos) {
		FORCED.set(new Forced(targetEntry.structure().value(), anchorEntry.structure().value(), pos.x, pos.z));
	}

	public static void endConversion() { FORCED.remove(); }

	/** PlacementPriority's predictions: set this thread's forced conversion aside and back. */
	static Object saveForced() { return FORCED.get(); }

	static void restoreForced(Object forced) {
		if (forced != null) FORCED.set((Forced) forced); else FORCED.remove();
	}

	/** MetaStructure#findGenerationPoint: the anchor this slot's conversion chose, or null (a /place, say). */
	static Structure forcedAnchor(MetaStructure meta, ChunkPos pos) {
		Forced f = FORCED.get();
		return f != null && f.target == meta && f.chunkX == pos.x && f.chunkZ == pos.z ? f.anchor : null;
	}

	/**
	 * tryGenerateStructure's biome predicate (ChunkGeneratorStructureBiomeRulesMixin, after the biome rules): a
	 * replacement taking an anchor's slot may only start where the anchor could have (its biomes and biome rules),
	 * so it never fills a slot the anchor would have passed on to another member.
	 */
	public static Predicate<Holder<Biome>> narrowBiomes(Structure s, Predicate<Holder<Biome>> valid) {
		Forced f = FORCED.get();
		if (f == null || f.target != s || s instanceof MetaStructure) return valid;
		return valid.and(anchorBiomes(f.anchor));
	}

	private static Predicate<Holder<Biome>> anchorBiomes(Structure anchor) {
		return StructureBiomeRules.filter(anchor, anchor.biomes()::contains);
	}

	// ── prediction: StructureCheck#canCreateStructure ────────────────────────

	/**
	 * The generation point StructureCheck asks for. A converted meta answers "maybe here" when one of its anchors
	 * fits the chunk, keeps its frequency and converts to this meta; assembling the whole meta is left to the chunk
	 * load that follows. A replacement answers with its own point when an anchor keeps its frequency there, converts
	 * to it, and the replacement fits (its biomes and the anchor's). Everything else answers as usual.
	 */
	public static Optional<Structure.GenerationStub> predictGenerationPoint(Structure structure, Structure.GenerationContext ctx) {
		Index index = index(ctx.registryAccess());
		List<Structure> anchors = index.anchorsOf.get(structure);
		if (anchors == null) return LocatePrediction.plain(structure, ctx);
		boolean meta = structure instanceof MetaStructure;
		if (meta && !WorldgenConfig.metaStructuresEnabled()) return Optional.empty();
		int x = ctx.chunkPos().x, z = ctx.chunkPos().z;
		for (Structure a : anchors) {
			Conversion c = conversion(index, a, ctx.seed(), x, z);
			if (c == null || c.target() != structure || !StructureFrequencies.keepsAt(a, ctx.seed(), x, z)) continue;
			if (meta) {
				if (LocatePrediction.fits(a, ctx, b -> true, false).isPresent()) return Optional.of(LocatePrediction.dummy(ctx.chunkPos()));
			} else {
				Optional<Structure.GenerationStub> stub = LocatePrediction.fits(structure, ctx, anchorBiomes(a), false);
				if (stub.isPresent()) return stub;
			}
		}
		LocateStats.predictedNo(LocateStats.Stage.META);
		return Optional.empty();
	}

	/** The anchors whose slots can turn into this meta or replacement; empty for anything else. */
	public static List<Structure> anchorsOf(RegistryAccess access, Structure structure) {
		List<Structure> anchors = index(access).anchorsOf.get(structure);
		return anchors == null ? List.of() : anchors;
	}

	// ── the search: findNearestMapStructure / getStructureGeneratingAt ───────

	/** A search that also looks inside metas: what it asked for, and those of them that can spawn in its level. */
	private record Locating(Set<Structure> asked, Set<Structure> targets) {}

	private static final ThreadLocal<Locating> LOCATING = new ThreadLocal<>();

	/**
	 * findNearestMapStructure HEAD: the asked-for structures plus every meta that can contain one of them, or null
	 * when no meta can (or this is the inner, expanded search). Only asked-for structures that can spawn in this
	 * level (their biomes, with fits and biome rules, meet the level's possible biomes) bring metas in: one that
	 * can't (the End tower searched from the Overworld) gets the plain search, which vanilla ends at once.
	 */
	public static HolderSet<Structure> beginLocate(ServerLevel level, HolderSet<Structure> wanted) {
		if (LOCATING.get() != null || !WorldgenConfig.metaStructuresEnabled()) return null;
		Registry<Structure> reg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		Set<Holder<Biome>> possible = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
		Set<Structure> asked = Collections.newSetFromMap(new IdentityHashMap<>()), targets = Collections.newSetFromMap(new IdentityHashMap<>());
		List<Holder<Structure>> holders = new ArrayList<>();
		for (Holder<Structure> h : wanted) {
			holders.add(h);
			asked.add(h.value());
			if (canSpawnAmong(h.value(), possible)) targets.add(h.value());
		}
		if (targets.isEmpty()) return null;
		Set<Holder<Structure>> metas = new LinkedHashSet<>();
		for (Map.Entry<net.minecraft.resources.ResourceKey<Structure>, Structure> e : reg.entrySet()) {
			if (!(e.getValue() instanceof MetaStructure meta) || asked.contains(meta)) continue;
			for (Structure s : meta.contents(reg)) {
				if (targets.contains(s)) {
					reg.getHolder(e.getKey()).ifPresent(metas::add);
					break;
				}
			}
		}
		if (metas.isEmpty()) return null;
		holders.addAll(metas);
		LOCATING.set(new Locating(asked, targets));
		return HolderSet.direct(holders);
	}

	/** Some biome the level can make passes the structure's biomes (fits applied) and its biome rules. */
	static boolean canSpawnAmong(Structure s, Set<Holder<Biome>> possible) {
		Predicate<Holder<Biome>> ok = StructureBiomeRules.filter(s, s.biomes()::contains);
		for (Holder<Biome> b : possible) if (ok.test(b)) return true;
		return false;
	}

	public static void endLocate() { LOCATING.remove(); }

	public static boolean locating() { return LOCATING.get() != null; }

	/**
	 * getStructureGeneratingAt during an expanded search: vanilla's check for the asked-for structures; for a meta
	 * (only there because it can contain one), its real start in that chunk and the first part that was asked for.
	 */
	public static Pair<BlockPos, Holder<Structure>> locateAt(Set<Holder<Structure>> holders, LevelReader level, StructureManager manager,
			boolean skipKnown, StructurePlacement placement, ChunkPos pos) {
		Locating locating = LOCATING.get();
		Set<Structure> targets = locating.targets();
		for (Holder<Structure> h : holders) {
			Structure s = h.value();
			StructureCheckResult r = manager.checkStructurePresence(pos, s, skipKnown);
			if (r == StructureCheckResult.START_NOT_PRESENT) continue;
			boolean alias = !locating.asked().contains(s);
			if (!alias && !skipKnown && r == StructureCheckResult.START_PRESENT) return Pair.of(placement.getLocatePos(pos), h);
			// a meta slot not generated yet: only worth a load when an asked-for structure could be in it
			if (alias && r == StructureCheckResult.CHUNK_LOAD_NEEDED && s instanceof MetaStructure meta && level instanceof ServerLevel serverLevel
					&& !targetMayBeIn(meta, targets, serverLevel, pos)) {
				LocateStats.predictedNo(LocateStats.Stage.META_TARGET);
				continue;
			}
			if (LocateStats.expired()) return null;
			ChunkAccess chunk = LocateLoads.structureStarts(level, pos.x, pos.z);
			StructureStart start = manager.getStartForStructure(SectionPos.bottomOf(chunk), s, chunk);
			if (start == null || !start.isValid()) continue;
			if (!alias) {
				if (!skipKnown || tryAddReference(manager, start)) return Pair.of(placement.getLocatePos(start.getChunkPos()), h);
				continue;
			}
			MetaStructure.Found part = MetaStructure.findPart(start, targets);
			if (part == null) continue;
			Registry<Structure> reg = manager.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
			Optional<Holder<Structure>> partHolder = reg.getResourceKey(part.structure()).flatMap(reg::getHolder);
			if (partHolder.isEmpty() || (skipKnown && !tryAddReference(manager, start))) continue;
			BoundingBox b = part.box();
			return Pair.of(new BlockPos((b.minX() + b.maxX()) / 2, 0, (b.minZ() + b.maxZ()) / 2), partHolder.get());
		}
		return null;
	}

	/**
	 * Could an asked-for structure be inside this meta if it forms in this slot? The meta's own prediction ("one of
	 * its anchors fits here") has already said yes. A target that is one of the meta's anchors must pass the same
	 * test for itself: fit the slot, keep its frequency, and convert to this meta here. A target that is only a part
	 * needs a biome of its own somewhere in the parts' reach: biomes sampled at quart resolution over every chunk
	 * within MetaStructure.REACH of the slot, at that chunk's surface height and 16 blocks above and below. Every
	 * part in meta_structures.json is a surface or sea structure, whose start point lies in that band; a sampled
	 * biome that lets a target in is enough for a yes, so the check only says no where no part could stand.
	 */
	static boolean targetMayBeIn(MetaStructure meta, Set<Structure> targets, ServerLevel level, ChunkPos pos) {
		Index index = index(level.registryAccess());
		Registry<Structure> reg = level.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		RandomState randomState = level.getChunkSource().randomState();
		long seed = level.getSeed();
		List<Structure> anchors = index.anchorsOf.getOrDefault(meta, List.of());
		Set<Structure> parts = meta.partStructures(reg);
		List<Predicate<Holder<Biome>>> partBiomes = new ArrayList<>();
		Structure.GenerationContext ctx = null;
		for (Structure t : targets) {
			if (containsSame(anchors, t)) {
				Conversion c = conversion(index, t, seed, pos.x, pos.z);
				if (c != null && c.target() == meta && StructureFrequencies.keepsAt(t, seed, pos.x, pos.z)) {
					if (ctx == null) ctx = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(), randomState,
							level.getStructureManager(), seed, pos, level, b -> true);
					if (LocatePrediction.fits(t, ctx, b -> true, false).isPresent()) return true;
				}
			}
			if (parts.contains(t)) partBiomes.add(StructureBiomeRules.filter(t, t.biomes()::contains));
		}
		return !partBiomes.isEmpty() && biomeNear(partBiomes, generator, randomState, level, pos);
	}

	/** Any of the predicates passes a biome sampled within MetaStructure.REACH chunks of pos (nearest chunks first). */
	private static boolean biomeNear(List<Predicate<Holder<Biome>>> wanted, ChunkGenerator generator, RandomState randomState, ServerLevel level, ChunkPos pos) {
		Map<Holder<Biome>, Boolean> seen = new IdentityHashMap<>();
		for (int ring = 0; ring <= MetaStructure.REACH; ring++) {
			for (int dx = -ring; dx <= ring; dx++) {
				for (int dz = -ring; dz <= ring; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
					int x0 = (pos.x + dx) << 4, z0 = (pos.z + dz) << 4;
					int y = generator.getBaseHeight(x0 + 8, z0 + 8, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
					for (int qx = 0; qx < 4; qx++) for (int qz = 0; qz < 4; qz++) for (int dy = -16; dy <= 16; dy += 16) {
						Holder<Biome> b = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x0 + qx * 4), QuartPos.fromBlock(y + dy),
								QuartPos.fromBlock(z0 + qz * 4), randomState.sampler());
						if (seen.computeIfAbsent(b, k -> wanted.stream().anyMatch(w -> w.test(k)))) return true;
					}
				}
			}
		}
		return false;
	}

	/** ChunkGenerator#tryAddReference (private static there). */
	private static boolean tryAddReference(StructureManager manager, StructureStart start) {
		if (!start.canBeReferenced()) return false;
		manager.addReference(start);
		return true;
	}
}
