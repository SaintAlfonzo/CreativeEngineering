package com.SaintAlfonzo.creativeengineering.worldgen;

import com.SaintAlfonzo.creativeengineering.util.WorldgenFiles;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/**
 * Jigsaw pool tuning: remove, weaken or boost individual pieces of jigsaw structures (village
 * houses above all), which the structure_set framework can't reach because a house is not a
 * structure, it's one weighted entry in a template pool such as minecraft:village/plains/houses.
 *
 * Mods add those entries in code (IE at mod init; Dynamic Village, PneumaticCraft, Farmer's
 * Delight, Monobank, Wares... in ServerAboutToStartEvent at normal priority; Cloud Storage and
 * Domestication Innovation through a mixin on Pools.register), so no jar scan can list them. This
 * runs in the same event at LOWEST priority, sees every pool exactly as generation will, and:
 *
 *  1. rewrites pool_config.json from what it found (new pools/pieces added with current = original,
 *     your current/enabled edits kept, pieces that vanished kept only if you had edited them);
 *  2. resolves pool_tags.json (same value syntax as structure_tags.json) into per-piece multipliers;
 *  3. rebuilds every pool whose weights changed, and writes pool_tag_report.md.
 *
 * Pool order: the pools reach this event in an order that changes from one JVM run to the next (some mod rebuilds
 * them through an identity-hashed collection; the 10 versions of pool_tag_report.md in git list the desert houses in
 * 9 orders), and jigsaw picks a piece by its index in the pool's list, so a seed's villages came out different
 * after each restart (testing/structures/05_phase_5.md 3.3). With [worldgen.pools] canonicalOrder (default) every
 * pool is sorted by piece key, a stable order, before anything else.
 *
 * Singleplayer caveat handled here: builtin pools (vanilla + everything mods inject into them) are
 * the same Java objects in every world you open in one game session, and several mods append their
 * pieces again on each world load. The first weights seen for each pool object are remembered, so
 * edits never compound and re-injected duplicates are dropped.
 *
 * Any error in either file applies no tuning at all (every pool is left at its mod/vanilla weights)
 * and is listed in the report, so a typo never half-applies.
 */
public final class PoolTuner {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/pool-tuning");

	static final String CONFIG = BiomeLayoutTuner.WORLDGEN_DIR + "pool_config.json";
	static final String TAGS = BiomeLayoutTuner.WORLDGEN_DIR + "pool_tags.json";
	static final String REPORT = BiomeLayoutTuner.WORLDGEN_DIR + "pool_tag_report.md";

	// Fractional weights are realised by scaling the whole pool by up to CEConfig [worldgen.pools]
	// maxFractionScale. Jigsaw tries every copy of a piece in a pool's list, so a bigger list is slower to fail.
	private static final int SIZE_UNSET = Integer.MIN_VALUE;

	private static final Field RAW_TEMPLATES = field(StructureTemplatePool.class, "f_210559_", "rawTemplates");
	private static final Field TEMPLATES = field(StructureTemplatePool.class, "f_210560_", "templates");
	private static final Field MAX_SIZE = field(StructureTemplatePool.class, "f_210562_", "maxSize");
	private static final Field FALLBACK = field(StructureTemplatePool.class, "f_210561_", "fallback");
	private static final Field LIST_ELEMENTS = field(ListPoolElement.class, "f_210360_", "elements");
	private static final Field PROCESSORS = holderField(SinglePoolElement.class);

	/** SRG name in the game; the Mojmap name when run headless against the mapped jar (tests). */
	private static Field field(Class<?> c, String srg, String mojmap) {
		try {
			return ObfuscationReflectionHelper.findField(c, srg);
		} catch (RuntimeException | LinkageError e) {
			try {
				Field f = c.getDeclaredField(mojmap);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException e2) {
				throw new IllegalStateException("PoolTuner: no field " + srg + "/" + mojmap + " on " + c.getName(), e);
			}
		}
	}

	/** The one Holder field of the class (SinglePoolElement#processors), found by type so no SRG name is needed. */
	private static Field holderField(Class<?> c) {
		for (Field f : c.getDeclaredFields()) {
			if (f.getType() == Holder.class && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				return f;
			}
		}
		throw new IllegalStateException("PoolTuner: no Holder field on " + c.getName());
	}

	/** First-seen weights per pool object, plus the list we last wrote into it (see class doc). */
	private static final Map<StructureTemplatePool, Snapshot> SNAPSHOTS = Collections.synchronizedMap(new WeakHashMap<>());

	private static final class Snapshot {
		final List<Pair<StructurePoolElement, Integer>> original;
		List<Pair<StructurePoolElement, Integer>> written;
		Snapshot(List<Pair<StructurePoolElement, Integer>> original) { this.original = original; }
	}

	/** One piece of one pool. */
	private static final class Piece {
		final String key;
		final StructurePoolElement element;
		final int original;
		double current;
		boolean enabled = true;
		final List<String> chain = new ArrayList<>();
		double effective;
		int result;
		Piece(String key, StructurePoolElement element, int original) {
			this.key = key; this.element = element; this.original = original; this.current = original;
		}
	}

	private static final class PoolInfo {
		final String id;
		final StructureTemplatePool pool;
		final String fallback;
		final List<Piece> pieces = new ArrayList<>();
		final List<Pair<StructurePoolElement, Integer>> live;
		int scale = 1;
		final List<String> notes = new ArrayList<>();
		PoolInfo(String id, StructureTemplatePool pool, String fallback, List<Pair<StructurePoolElement, Integer>> live) {
			this.id = id; this.pool = pool; this.fallback = fallback; this.live = live;
		}
	}

	private PoolTuner() {}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		LootContainerSwaps.load();
		try {
			rebindProcessors(event.getServer().registryAccess());
		} catch (Throwable t) {
			LOG.error("[pool-tuning] processor lists not rebound; pieces added in code may be dropped when a chunk reloads", t);
		}
		try {
			Registry<StructureTemplatePool> registry = event.getServer().registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
			run(registry);
		} catch (Throwable t) {
			LOG.error("[pool-tuning] failed, pools left at their mod/vanilla weights", t);
		}
	}

	/**
	 * Pieces that mods add in code (Cloud Storage and Domestication Innovation houses in the five village house pools)
	 * carry the builtin ProcessorLists holder, which the world's registry codec rejects: saving a structure_starts
	 * chunk logs "Element ... minecraft:empty is not valid in current registry set" and writes the piece as {}, and
	 * the reload drops it ("Invalid pool element found") before the village is placed (05_phase_5.md 3.2). Every
	 * pool piece whose processor list isn't the world's own gets the world's holder of the same key. A list the
	 * world doesn't have is left alone and counted.
	 */
	static void rebindProcessors(RegistryAccess access) throws IllegalAccessException {
		Registry<StructureProcessorList> lists = access.registryOrThrow(Registry.PROCESSOR_LIST_REGISTRY);
		Set<StructurePoolElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		int[] counts = new int[2];   // rebound, unknown
		for (StructureTemplatePool pool : access.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)) {
			for (Pair<StructurePoolElement, Integer> pair : rawTemplates(pool)) rebind(pair.getFirst(), lists, seen, counts);
			Object live = TEMPLATES.get(pool);
			if (live instanceof List<?> elements) for (Object el : elements) if (el instanceof StructurePoolElement e) rebind(e, lists, seen, counts);
		}
		if (counts[0] > 0 || counts[1] > 0)
			LOG.info("[pool-tuning] {} pool piece(s) moved to the world's processor lists; {} name a list the world doesn't have", counts[0], counts[1]);
	}

	@SuppressWarnings("unchecked")
	private static void rebind(StructurePoolElement element, Registry<StructureProcessorList> lists, Set<StructurePoolElement> seen, int[] counts)
			throws IllegalAccessException {
		if (!seen.add(element)) return;
		if (element instanceof ListPoolElement list) {
			for (StructurePoolElement sub : (List<StructurePoolElement>) LIST_ELEMENTS.get(list)) rebind(sub, lists, seen, counts);
			return;
		}
		if (!(element instanceof SinglePoolElement single)) return;
		Holder<StructureProcessorList> holder = (Holder<StructureProcessorList>) PROCESSORS.get(single);
		if (holder == null || holder.kind() != Holder.Kind.REFERENCE || holder.isValidInRegistry(lists)) return;
		Optional<Holder<StructureProcessorList>> own = holder.unwrapKey().flatMap(lists::getHolder);
		if (own.isPresent()) {
			PROCESSORS.set(single, own.get());
			counts[0]++;
		} else counts[1]++;
	}

	static synchronized void run(Registry<StructureTemplatePool> registry) throws Exception {
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		// ── discover ─────────────────────────────────────────────────────────
		boolean canonical = WorldgenConfig.poolCanonicalOrder();
		Map<String, PoolInfo> pools = new TreeMap<>();
		for (Map.Entry<ResourceKey<StructureTemplatePool>, StructureTemplatePool> e : registry.entrySet()) {
			StructureTemplatePool pool = e.getValue();
			String id = e.getKey().location().toString();
			Object fb = FALLBACK.get(pool);
			PoolInfo info = new PoolInfo(id, pool, fb == null ? "" : fb.toString(), rawTemplates(pool));
			Map<String, Integer> seen = new LinkedHashMap<>();
			List<Pair<StructurePoolElement, Integer>> base = baseline(pool, info.live);
			if (canonical) base = canonical(base);
			for (Pair<StructurePoolElement, Integer> pair : base) {
				String key = elementKey(pair.getFirst());
				int n = seen.merge(key, 1, Integer::sum);
				if (n > 1) key = key + " #" + n;
				info.pieces.add(new Piece(key, pair.getFirst(), pair.getSecond()));
			}
			pools.put(id, info);
		}

		// ── merge the config ─────────────────────────────────────────────────
		JsonObject oldPools = new JsonObject();
		boolean configBroken = false;
		String cfgText = WorldgenFiles.readText(CONFIG);
		if (cfgText != null) {
			try {
				oldPools = optObject(parseLenient(cfgText), "pools");
			} catch (Exception ex) {
				configBroken = true;   // never overwrite a file the user is mid-way through editing
				errors.add(CONFIG + " is not valid JSON (" + ex.getMessage() + "); fix it or delete it to regenerate. The file was not rewritten.");
			}
		}
		Map<String, Map<String, JsonObject>> stale = new TreeMap<>();
		for (PoolInfo info : pools.values()) {
			JsonObject oldElems = oldPools.has(info.id) && oldPools.get(info.id).isJsonObject()
					? optObject(oldPools.getAsJsonObject(info.id), "elements") : new JsonObject();
			for (Piece p : info.pieces) {
				if (!oldElems.has(p.key) || !oldElems.get(p.key).isJsonObject()) continue;
				JsonObject o = oldElems.getAsJsonObject(p.key);
				if (o.has("current")) {
					JsonElement c = o.get("current");
					if (c.isJsonPrimitive() && c.getAsJsonPrimitive().isNumber() && c.getAsDouble() >= 0 && Double.isFinite(c.getAsDouble())) {
						JsonElement oo = o.get("original");
						double oldOriginal = oo != null && oo.isJsonPrimitive() && oo.getAsJsonPrimitive().isNumber() ? oo.getAsDouble() : Double.NaN;
						p.current = mergeCurrent(info.id, p.key, p.original, oldOriginal, c.getAsDouble(), warnings);
					} else {
						errors.add(info.id + " / " + p.key + ": current must be a number >= 0 (got " + c + ")");
					}
				}
				if (o.has("enabled")) {
					JsonElement en = o.get("enabled");
					if (en.isJsonPrimitive() && en.getAsJsonPrimitive().isBoolean()) p.enabled = en.getAsBoolean();
					else errors.add(info.id + " / " + p.key + ": enabled must be true or false (got " + en + ")");
				}
			}
		}
		// Edited entries whose pool/piece is gone (mod removed or renamed): keep them so the edit
		// isn't lost, flagged missing. Unedited ones are dropped.
		for (String poolId : oldPools.keySet()) {
			if (!oldPools.get(poolId).isJsonObject()) continue;
			JsonObject oldElems = optObject(oldPools.getAsJsonObject(poolId), "elements");
			PoolInfo info = pools.get(poolId);
			Set<String> live = new LinkedHashSet<>();
			if (info != null) for (Piece p : info.pieces) live.add(p.key);
			for (String key : oldElems.keySet()) {
				if (live.contains(key) || !oldElems.get(key).isJsonObject()) continue;
				JsonObject o = oldElems.getAsJsonObject(key);
				if (isEdited(o)) stale.computeIfAbsent(poolId, k -> new LinkedHashMap<>()).put(key, o);
			}
		}

		// ── tags ─────────────────────────────────────────────────────────────
		Map<String, TagDef> tagDefs = new LinkedHashMap<>();
		String tagText = WorldgenFiles.readText(TAGS);
		if (tagText != null) {
			try {
				JsonObject tagsDoc = parseLenient(tagText);
				JsonObject tags = optObject(tagsDoc, "tags");
				for (String name : tags.keySet()) tagDefs.put(name, TagDef.parse(name, tags.get(name), errors));
			} catch (Exception ex) {
				errors.add(TAGS + " is not valid JSON (" + ex.getMessage() + ")");
			}
		}
		Set<String> allKeys = new TreeSet<>();
		for (PoolInfo info : pools.values()) for (Piece p : info.pieces) allKeys.add(p.key);
		Map<String, Set<String>> members = resolveTags(tagDefs, allKeys, errors, warnings);
		Map<String, List<Pattern>> tagPools = new LinkedHashMap<>();
		for (TagDef def : tagDefs.values()) {
			if (def.pools == null) continue;
			List<Pattern> pats = new ArrayList<>();
			for (String g : def.pools) {
				Pattern pat = globToRegex(g);
				boolean any = pools.keySet().stream().anyMatch(id -> pat.matcher(id).matches());
				if (!any) {
					if (g.contains("*")) warnings.add(def.name + ": pools entry \"" + g + "\" matches no pool");
					else errors.add(def.name + ": unknown pool \"" + g + "\"");
				}
				pats.add(pat);
			}
			tagPools.put(def.name, pats);
		}

		// ── plan ─────────────────────────────────────────────────────────────
		// Switched off: every pool goes back to its first-seen weights, exactly like an error would do.
		boolean enabled = WorldgenConfig.poolTuningEnabled();
		if (!enabled) warnings.add("pool tuning is off in CEConfig [worldgen.systems] poolTuning: every pool is at its mod/vanilla weights");
		boolean apply = enabled && errors.isEmpty();
		List<PoolInfo> changed = new ArrayList<>();
		int restored = 0, reordered = 0;
		for (PoolInfo info : pools.values()) {
			boolean differs = false;
			for (Piece p : info.pieces) {
				double m = 1;
				if (apply) {
					for (TagDef def : tagDefs.values()) {
						Set<String> mem = members.get(def.name);
						if (mem == null || !mem.contains(p.key)) continue;
						List<Pattern> pats = tagPools.get(def.name);
						if (pats != null && pats.stream().noneMatch(pt -> pt.matcher(info.id).matches())) continue;
						if (def.multiplier != 1) p.chain.add(def.name + " x" + fmt(def.multiplier));
						m *= def.multiplier;
					}
				}
				p.effective = !apply ? p.original : (p.enabled ? p.current * m : 0);
				if (p.effective != p.original) differs = true;
			}
			if (differs) {
				toIntWeights(info);
				changed.add(info);
			} else {
				for (Piece p : info.pieces) p.result = p.original;
			}
			List<Pair<StructurePoolElement, Integer>> target = buildList(info);
			if (!sameList(target, info.live)) {
				writePool(info.pool, target);
				if (!differs) {
					if (canonical && sameList(target, canonical(info.live))) reordered++;   // the order was all that changed
					else restored++;
				}
			}
		}

		// ── write config + report ────────────────────────────────────────────
		if (!configBroken) WorldgenFiles.writeText(CONFIG, configText(pools, stale));
		WorldgenFiles.writeText(REPORT, report(pools, changed, tagDefs, members, errors, warnings, stale, restored));

		int pieces = pools.values().stream().mapToInt(i -> i.pieces.size()).sum();
		if (!errors.isEmpty()) {
			LOG.error("[pool-tuning] {} error(s), NO pool tuning applied (see {}): {}", errors.size(), REPORT, errors.get(0));
		} else {
			LOG.info("[pool-tuning] {} pools / {} pieces, {} pool(s) retuned, {} restored to their first-seen weights, {} put in canonical order, {} warning(s); report: {}",
					pools.size(), pieces, changed.size(), restored, reordered, warnings.size(), REPORT);
		}
	}

	// ── pool access ──────────────────────────────────────────────────────────

	@SuppressWarnings("unchecked")
	private static List<Pair<StructurePoolElement, Integer>> rawTemplates(StructureTemplatePool pool) throws IllegalAccessException {
		return new ArrayList<>((List<Pair<StructurePoolElement, Integer>>) RAW_TEMPLATES.get(pool));
	}

	/** The pool's weights as first seen this game session, plus pieces added since that aren't re-adds. */
	private static List<Pair<StructurePoolElement, Integer>> baseline(StructureTemplatePool pool, List<Pair<StructurePoolElement, Integer>> live) {
		Snapshot s = SNAPSHOTS.get(pool);
		if (s == null) {
			SNAPSHOTS.put(pool, new Snapshot(new ArrayList<>(live)));
			return live;
		}
		Set<StructurePoolElement> known = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<String> knownKeys = new LinkedHashSet<>();
		for (Pair<StructurePoolElement, Integer> p : s.original) { known.add(p.getFirst()); knownKeys.add(elementKey(p.getFirst())); }
		if (s.written != null) for (Pair<StructurePoolElement, Integer> p : s.written) known.add(p.getFirst());
		for (Pair<StructurePoolElement, Integer> p : live) {
			if (known.contains(p.getFirst())) continue;
			String key = elementKey(p.getFirst());
			if (knownKeys.add(key)) s.original.add(p);   // a genuinely new piece
			known.add(p.getFirst());                      // else: a mod re-adding its piece on this world load
		}
		return new ArrayList<>(s.original);
	}

	/** The pieces sorted by key, then weight: the same order on every run (a stable sort keeps exact ties as they came). */
	static List<Pair<StructurePoolElement, Integer>> canonical(List<Pair<StructurePoolElement, Integer>> list) {
		List<Pair<StructurePoolElement, Integer>> out = new ArrayList<>(list);
		out.sort(Comparator.comparing((Pair<StructurePoolElement, Integer> p) -> elementKey(p.getFirst())).thenComparingInt(Pair::getSecond));
		return out;
	}

	private static List<Pair<StructurePoolElement, Integer>> buildList(PoolInfo info) {
		List<Pair<StructurePoolElement, Integer>> out = new ArrayList<>();
		for (Piece p : info.pieces) if (p.result > 0) out.add(Pair.of(p.element, p.result));
		if (out.isEmpty()) out.add(Pair.of(EmptyPoolElement.INSTANCE, 1));
		return out;
	}

	private static boolean sameList(List<Pair<StructurePoolElement, Integer>> a, List<Pair<StructurePoolElement, Integer>> b) {
		if (a.size() != b.size()) return false;
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i).getFirst() != b.get(i).getFirst() || !a.get(i).getSecond().equals(b.get(i).getSecond())) return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static void writePool(StructureTemplatePool pool, List<Pair<StructurePoolElement, Integer>> list) throws IllegalAccessException {
		List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>(list);
		RAW_TEMPLATES.set(pool, raw);
		ObjectArrayList<StructurePoolElement> templates = (ObjectArrayList<StructurePoolElement>) TEMPLATES.get(pool);
		templates.clear();
		for (Pair<StructurePoolElement, Integer> p : raw) for (int i = 0; i < p.getSecond(); i++) templates.add(p.getFirst());
		MAX_SIZE.setInt(pool, SIZE_UNSET);
		Snapshot s = SNAPSHOTS.get(pool);
		if (s != null) s.written = raw;
	}

	/** Effective (possibly fractional) weights -> integers, scaling the whole pool when needed. */
	private static void toIntWeights(PoolInfo info) {
		boolean allInt = true;
		for (Piece p : info.pieces) if (p.effective > 0 && p.effective != Math.rint(p.effective)) allInt = false;
		int k = 1;
		if (!allInt) {
			double bestErr = Double.MAX_VALUE;
			for (int s = 1, max = WorldgenConfig.poolMaxFractionScale(); s <= max; s++) {
				double err = 0;
				for (Piece p : info.pieces) {
					if (p.effective <= 0) continue;
					double want = s * p.effective;
					err = Math.max(err, Math.abs(Math.max(1, Math.round(want)) - want) / want);
				}
				if (err < bestErr - 1e-9) { bestErr = err; k = s; }
				if (err <= 0.05) break;
			}
			if (bestErr > 0.05) info.notes.add(String.format("weights rounded (up to %.0f%% off the exact ratio, pool scaled x%d, the maximum)", bestErr * 100, k));
		}
		int g = 0;
		for (Piece p : info.pieces) {
			p.result = p.effective > 0 ? (int) Math.max(1, Math.round(k * p.effective)) : 0;
			if (p.result > 0) g = gcd(g, p.result);
		}
		if (k > 1 && g > 1) for (Piece p : info.pieces) p.result /= g;
		info.scale = g > 1 && k > 1 ? k / gcd(k, g) : k;
		boolean any = info.pieces.stream().anyMatch(p -> p.result > 0);
		if (!any) info.notes.add("every piece removed; the pool now holds only minecraft:empty (a start pool that is empty generates nothing, a houses pool leaves empty lots)");
		long total = info.pieces.stream().mapToLong(p -> p.result).sum();
		if (total > 5000) info.notes.add("total weight " + total + " is very large; jigsaw walks one list entry per weight point, so generation of this structure gets slower");
	}

	private static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }

	// ── element keys ─────────────────────────────────────────────────────────

	/** "ns:path" of the piece's template (or feature:<id>, list:[...], minecraft:empty). */
	static String elementKey(StructurePoolElement el) {
		try {
			if (el instanceof EmptyPoolElement) return "minecraft:empty";
			if (el instanceof ListPoolElement) {
				@SuppressWarnings("unchecked")
				List<StructurePoolElement> sub = (List<StructurePoolElement>) LIST_ELEMENTS.get(el);
				List<String> keys = new ArrayList<>();
				for (StructurePoolElement s : sub) keys.add(elementKey(s));
				return "list:[" + String.join(",", keys) + "]";
			}
			// Found by field type, not name, so mod subclasses (YUNG's, MES) key the same way:
			// a template piece holds Either<ResourceLocation, StructureTemplate>, a feature piece a
			// Holder<PlacedFeature>.
			String feature = null;
			for (Class<?> c = el.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
				for (Field f : c.getDeclaredFields()) {
					if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
					if (Either.class.isAssignableFrom(f.getType())) {
						f.setAccessible(true);
						if (f.get(el) instanceof Either<?, ?> either && either.left().orElse(null) instanceof ResourceLocation rl) return rl.toString();
					} else if (feature == null && Holder.class.isAssignableFrom(f.getType())) {
						f.setAccessible(true);
						if (f.get(el) instanceof Holder<?> h && h.unwrapKey().isPresent()
								&& h.unwrapKey().get().registry().equals(Registry.PLACED_FEATURE_REGISTRY.location())) {
							feature = "feature:" + h.unwrapKey().get().location();
						}
					}
				}
			}
			if (feature != null) return feature;
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			// fall through to toString
		}
		return el.getClass().getSimpleName() + ":" + el;
	}

	// ── tags ─────────────────────────────────────────────────────────────────

	private static final class TagDef {
		final String name;
		double multiplier = 1;
		final List<String> values = new ArrayList<>();
		List<String> pools;

		TagDef(String name) { this.name = name; }

		static TagDef parse(String name, JsonElement el, List<String> errors) {
			TagDef d = new TagDef(name);
			if (!el.isJsonObject()) { errors.add(name + ": must be an object"); return d; }
			JsonObject o = el.getAsJsonObject();
			JsonElement m = o.get("multiplier");
			if (m == null || !m.isJsonPrimitive() || !m.getAsJsonPrimitive().isNumber() || !Double.isFinite(m.getAsDouble()) || m.getAsDouble() < 0) {
				errors.add(name + ": multiplier must be a number >= 0 (got " + m + ")");
			} else {
				d.multiplier = m.getAsDouble();
			}
			d.values.addAll(stringList(o.get("values"), name + ".values", errors));
			if (o.has("pools")) d.pools = stringList(o.get("pools"), name + ".pools", errors);
			return d;
		}
	}

	private static List<String> stringList(JsonElement el, String where, List<String> errors) {
		List<String> out = new ArrayList<>();
		if (el == null) return out;
		if (!el.isJsonArray()) { errors.add(where + " must be a list of strings"); return out; }
		for (JsonElement v : el.getAsJsonArray()) {
			if (v.isJsonPrimitive() && v.getAsJsonPrimitive().isString()) out.add(v.getAsString());
			else errors.add(where + ": " + v + " is not a string");
		}
		return out;
	}

	/** Same rules as structure_overrides.js: ids, globs, #tags, ! excludes (exclusions win). */
	private static Map<String, Set<String>> resolveTags(Map<String, TagDef> defs, Set<String> allKeys, List<String> errors, List<String> warnings) {
		Map<String, Set<String>> done = new LinkedHashMap<>();
		Set<String> resolving = new LinkedHashSet<>();
		for (String name : defs.keySet()) resolveTag(name, name, defs, allKeys, done, resolving, errors, warnings);
		return done;
	}

	private static Set<String> resolveTag(String name, String from, Map<String, TagDef> defs, Set<String> allKeys,
			Map<String, Set<String>> done, Set<String> resolving, List<String> errors, List<String> warnings) {
		if (done.containsKey(name)) return done.get(name);
		TagDef def = defs.get(name);
		if (def == null) { errors.add(from + ": unknown tag \"#" + name + "\""); return Set.of(); }
		if (!resolving.add(name)) { errors.add(from + ": tag cycle through \"#" + name + "\""); return Set.of(); }
		Set<String> include = new LinkedHashSet<>();
		Set<String> exclude = new LinkedHashSet<>();
		for (String raw : def.values) {
			boolean negate = raw.startsWith("!");
			String v = negate ? raw.substring(1) : raw;
			Set<String> ids;
			if (v.startsWith("#")) {
				ids = resolveTag(v.substring(1), name, defs, allKeys, done, resolving, errors, warnings);
			} else if (v.contains("*")) {
				Pattern pat = globToRegex(v);
				ids = new LinkedHashSet<>();
				for (String k : allKeys) if (pat.matcher(k).matches()) ids.add(k);
				if (ids.isEmpty()) warnings.add(name + ": \"" + raw + "\" matches no pool piece");
			} else if (allKeys.contains(v)) {
				ids = Set.of(v);
			} else {
				errors.add(name + ": unknown pool piece \"" + v + "\"");
				ids = Set.of();
			}
			(negate ? exclude : include).addAll(ids);
		}
		resolving.remove(name);
		include.removeAll(exclude);
		done.put(name, include);
		return include;
	}

	private static Pattern globToRegex(String glob) {
		StringBuilder sb = new StringBuilder("^");
		String[] parts = glob.split("\\*", -1);
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) sb.append(".*");
			sb.append(Pattern.quote(parts[i]));
		}
		return Pattern.compile(sb.append('$').toString());
	}

	// ── json ─────────────────────────────────────────────────────────────────

	public static JsonObject parseLenient(String text) {
		JsonReader reader = new JsonReader(new StringReader(text));
		reader.setLenient(true);
		return JsonParser.parseReader(reader).getAsJsonObject();
	}

	private static JsonObject optObject(JsonObject o, String key) {
		return o.has(key) && o.get(key).isJsonObject() ? o.getAsJsonObject(key) : new JsonObject();
	}

	/**
	 * The weight a piece plays with, from its stored entry. Keys of duplicate pieces ({@code #2}, {@code #3}) follow
	 * list order, so a reordered pool (canonicalOrder, a mod update) can hand a key to a piece with another weight.
	 * The stored {@code original} tells: when it differs from the live piece's weight, an unedited entry
	 * ({@code current == original}) follows the live weight; an edited one keeps its value and is reported.
	 * Before 2026-11-02 the stored current always won, so 223 duplicates in 27 pools played another piece's weight.
	 */
	static double mergeCurrent(String poolId, String key, int liveOriginal, double oldOriginal, double oldCurrent, List<String> warnings) {
		if (Double.isNaN(oldOriginal) || oldOriginal == liveOriginal) return oldCurrent;
		if (oldCurrent == oldOriginal) return liveOriginal;
		warnings.add(poolId + " / " + key + ": " + (key.contains(" #") ? "piece order changed under an edited duplicate" : "the mod changed this edited piece's weight")
				+ " (stored original " + fmt(oldOriginal) + ", live " + liveOriginal + "); current " + fmt(oldCurrent) + " kept: check it");
		return oldCurrent;
	}

	private static boolean isEdited(JsonObject o) {
		try {
			boolean disabled = o.has("enabled") && !o.get("enabled").getAsBoolean();
			boolean retuned = o.has("current") && o.has("original") && o.get("current").getAsDouble() != o.get("original").getAsDouble();
			return disabled || retuned;
		} catch (RuntimeException e) {
			return true;
		}
	}

	static String fmt(double v) {
		if (v == Math.rint(v) && Math.abs(v) < 1e15) return Long.toString((long) v);
		String s = String.format(java.util.Locale.ROOT, "%.6f", v).replaceAll("0+$", "");
		return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
	}

	private static String q(String s) { return new JsonPrimitive(s).toString(); }

	private static final String[][] README = {
		{"what", "Per-piece weights for every jigsaw template pool in the game (village houses, streets, bastion rooms, T&T buildings...). A village house is not a structure, it is one weighted entry in a pool such as minecraft:village/plains/houses, which is why it isn't in structure_config.json. Mods add many of these pieces in code (IE, Dynamic Village, PneumaticCraft...), so this file is written BY THE GAME at every world load from the live pools; you only edit it."},
		{"edit", "\"enabled\": false removes a piece. \"current\" is its weight: the chance it's picked each time the pool is drawn is current / (sum of the pool's weights). Leave \"original\" alone (the mod/vanilla weight, refreshed every load). For a category of pieces (all IE houses, all tech houses) use pool_tags.json, whose multipliers apply on top of current."},
		{"apply", "Rejoin the world (or restart the server). patcherMod worldgen/PoolTuner applies this at world load, after every mod has added its pieces, and writes pool_tag_report.md. Only chunks generated afterwards are affected."},
		{"weights", "Fractional results (current 0.5, or a 0.5 multiplier on a weight-1 piece) are fine: the whole pool is scaled up (at most x10) so the ratios hold. A pool whose pieces are all removed keeps a single minecraft:empty entry, so nothing crashes; a start pool like that just generates no structure (use structure_config.json to remove whole structures instead)."},
		{"keys", "Pieces are keyed by template id (\"immersiveengineering:village/houses/plains_engineer\"). feature:<id> = a placed-feature piece (hay piles, lamp posts), list:[...] = several templates placed together, minecraft:empty = the empty lot, \" #2\" = a second entry for the same template. \"missing\": true = you edited it but this load didn't find it (mod removed?); it's kept so the edit isn't lost."},
		{"houses", "Village houses live in minecraft:village/<plains|desert|savanna|snowy|taiga>/houses (and .../zombie/houses for abandoned villages). pool_tag_report.md lists, per pool, every piece another mod injected."},
	};

	private static String configText(Map<String, PoolInfo> pools, Map<String, Map<String, JsonObject>> stale) {
		StringBuilder sb = new StringBuilder("{\n  \"_readme\": {\n");
		for (int i = 0; i < README.length; i++) {
			sb.append("    ").append(q(README[i][0])).append(": ").append(q(README[i][1])).append(i < README.length - 1 ? ",\n" : "\n");
		}
		sb.append("  },\n  \"pools\": {\n");
		Set<String> ids = new TreeSet<>(pools.keySet());
		ids.addAll(stale.keySet());
		int n = 0;
		for (String id : ids) {
			PoolInfo info = pools.get(id);
			sb.append("    ").append(q(id)).append(": {\n");
			if (info != null) sb.append("      \"fallback\": ").append(q(info.fallback)).append(",\n");
			sb.append("      \"elements\": {\n");
			List<String> lines = new ArrayList<>();
			if (info != null) {
				for (Piece p : info.pieces) {
					lines.add("        " + q(p.key) + ": { \"original\": " + p.original + ", \"current\": " + fmt(p.current) + ", \"enabled\": " + p.enabled + " }");
				}
			}
			Map<String, JsonObject> st = stale.getOrDefault(id, Map.of());
			for (Map.Entry<String, JsonObject> e : st.entrySet()) {
				JsonObject o = e.getValue();
				String orig = o.has("original") ? o.get("original").toString() : "0";
				String cur = o.has("current") ? o.get("current").toString() : orig;
				String en = o.has("enabled") ? o.get("enabled").toString() : "true";
				lines.add("        " + q(e.getKey()) + ": { \"original\": " + orig + ", \"current\": " + cur + ", \"enabled\": " + en + ", \"missing\": true }");
			}
			sb.append(String.join(",\n", lines)).append(lines.isEmpty() ? "" : "\n");
			sb.append("      }\n    }").append(++n < ids.size() ? ",\n" : "\n");
		}
		sb.append("  }\n}\n");
		return sb.toString();
	}

	// ── report ───────────────────────────────────────────────────────────────

	private static String ns(String key) {
		String k = key.startsWith("feature:") ? key.substring(8) : key;
		int c = k.indexOf(':');
		return c > 0 && !k.startsWith("list:") ? k.substring(0, c) : "";
	}

	private static String report(Map<String, PoolInfo> pools, List<PoolInfo> changed, Map<String, TagDef> tagDefs,
			Map<String, Set<String>> members, List<String> errors, List<String> warnings,
			Map<String, Map<String, JsonObject>> stale, int restored) {
		StringBuilder sb = new StringBuilder();
		sb.append("# Pool tuning report\n\n");
		sb.append("Rewritten by patcherMod `worldgen/PoolTuner` at every world load from `pool_config.json` + `pool_tags.json`. Don't edit.\n\n");
		int pieces = pools.values().stream().mapToInt(i -> i.pieces.size()).sum();
		sb.append("- ").append(pools.size()).append(" pools, ").append(pieces).append(" pieces\n");
		if (!errors.isEmpty()) {
			sb.append("\n## Errors: NO tuning applied\n\nEvery pool is at its mod/vanilla weights until these are fixed:\n\n");
			for (String e : errors) sb.append("- ").append(e).append('\n');
		} else {
			sb.append("- ").append(changed.size()).append(" pool(s) retuned\n");
		}
		if (restored > 0) sb.append("- ").append(restored).append(" pool(s) restored to first-seen weights (a mod re-added its pieces on this world load, or a previous edit was reverted)\n");
		if (!warnings.isEmpty()) {
			sb.append("\n## Warnings\n\n");
			for (String w : warnings) sb.append("- ").append(w).append('\n');
		}
		if (!stale.isEmpty()) {
			sb.append("\n## Edited pieces not found this load (kept in the config as \"missing\")\n\n");
			stale.forEach((pool, m) -> m.keySet().forEach(k -> sb.append("- `").append(pool).append("` / `").append(k).append("`\n")));
		}

		if (!changed.isEmpty()) {
			sb.append("\n## Retuned pools\n\nChance = share of the pool's total weight (each draw).\n");
			for (PoolInfo info : changed) {
				long before = info.pieces.stream().mapToLong(p -> p.original).sum();
				long after = info.pieces.stream().mapToLong(p -> p.result).sum();
				sb.append("\n### `").append(info.id).append("`\n\n");
				if (info.scale > 1) sb.append("Pool scaled x").append(info.scale).append(" to hold fractional weights.\n\n");
				for (String note : info.notes) sb.append("> ").append(note).append("\n\n");
				sb.append("| piece | original | chance | final | chance | why |\n|---|---:|---:|---:|---:|---|\n");
				for (Piece p : info.pieces) {
					List<String> why = new ArrayList<>();
					if (!p.enabled) why.add("enabled: false");
					else {
						if (p.current != p.original) why.add("current " + fmt(p.current));
						why.addAll(p.chain);
					}
					sb.append("| `").append(p.key).append("` | ").append(p.original).append(" | ").append(pct(p.original, before))
							.append(" | ").append(p.result == 0 ? "**removed**" : String.valueOf(p.result)).append(" | ").append(pct(p.result, after))
							.append(" | ").append(String.join(", ", why)).append(" |\n");
				}
			}
		}

		sb.append("\n## Pieces injected by other mods\n\nPieces whose namespace differs from their pool's, per pool. This is where mod village houses show up.\n");
		Map<String, Map<String, List<Piece>>> byNs = new TreeMap<>();
		for (PoolInfo info : pools.values()) {
			String poolNs = ns(info.id);
			for (Piece p : info.pieces) {
				String n = ns(p.key);
				if (n.isEmpty() || n.equals(poolNs)) continue;
				byNs.computeIfAbsent(n, k -> new TreeMap<>()).computeIfAbsent(info.id, k -> new ArrayList<>()).add(p);
			}
		}
		for (Map.Entry<String, Map<String, List<Piece>>> e : byNs.entrySet()) {
			sb.append("\n### ").append(e.getKey()).append("\n\n");
			for (Map.Entry<String, List<Piece>> pe : e.getValue().entrySet()) {
				PoolInfo info = pools.get(pe.getKey());
				long total = info.pieces.stream().mapToLong(p -> p.original).sum();
				sb.append("- `").append(pe.getKey()).append("` (pool total weight ").append(total).append("): ");
				List<String> parts = new ArrayList<>();
				for (Piece p : pe.getValue()) parts.add("`" + p.key + "` " + p.original + (p.result != p.original ? " -> " + p.result : ""));
				sb.append(String.join(", ", parts)).append('\n');
			}
		}

		if (!tagDefs.isEmpty()) {
			sb.append("\n## Tags\n");
			for (TagDef def : tagDefs.values()) {
				Set<String> mem = members.getOrDefault(def.name, Set.of());
				sb.append("\n### ").append(def.name).append(" x").append(fmt(def.multiplier)).append(" (").append(mem.size()).append(" pieces)\n\n");
				if (def.pools != null) sb.append("Only in pools: ").append(String.join(", ", def.pools)).append("\n\n");
				if (!mem.isEmpty()) {
					List<String> list = new ArrayList<>();
					for (String k : mem) list.add("`" + k + "`");
					sb.append(String.join(", ", list)).append('\n');
				}
			}
		}
		return sb.toString();
	}

	private static String pct(long w, long total) {
		return total <= 0 ? "-" : String.format(java.util.Locale.ROOT, "%.1f%%", 100.0 * w / total);
	}
}
