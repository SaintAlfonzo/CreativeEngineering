package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Per-structure spawn frequency inside a structure_set, which vanilla doesn't have (a set only has
 * member weights). Used by the structure groups (kubejs/server_scripts/worldgen/structure_overrides.js):
 * many structures share one group grid, and each gets its own rate.
 *
 * SEMANTICS: vanilla fills a grid slot by picking members by weight, falling through to the next pick
 * whenever the picked structure can't generate there (wrong biome). A member with frequency f < 1 that
 * does generate is kept with chance f; otherwise the slot is VETOED: nothing else in that set spawns
 * in that slot either. So lowering one member never makes another more common, and a member's rate is
 * frequency x its weight share of the slots it fits, exactly what tools/gen_structure_groups.py fits.
 *
 * DATA FLOW: the script stages {set id: {structure id: frequency}} during KubeJS's world-load datapack
 * wrap (highPriorityData). At ServerAboutToStart, before any chunk generates, the table is resolved
 * against the loaded registries and frozen, so it always matches the structure sets the world got.
 * ServerStopped clears both, so a world whose load didn't stage anything gets no frequencies.
 *
 * ROLL: a hash of (world seed, chunk x, chunk z, structure id), independent of the vanilla RNG. The same
 * roll answers StructureCheck's "could this structure start here?" prediction (/locate, explorer maps),
 * so a search never walks to a slot the roll rejected.
 *
 * META STRUCTURES (worldgen/MetaStructure) with an anchor roll the anchor's own frequency as well as their own,
 * so a village that gets towers is exactly as likely as the same village on its own. The meta tells this class
 * which anchor its start chose ({@link #anchorChosen}); /locate's prediction can't know the anchor yet, so it
 * only rolls the meta's own frequency.
 *
 * BIOME FACTORS (a group's "biome_frequency", 2026-10-14, plan 7.4.5 D4): every member of the set rolls frequency x
 * the factor of the biome at its generation point (the first rule the biome matches, else "default"), the point and
 * biome Structure#generate tests. Generation finds that point again at the roll (findGenerationPoint is seeded from
 * the chunk, so it is the same point); /locate's prediction applies the factor where it already has the point
 * (LocatePrediction#plain). Same roll value, same biome, so both agree. Factors are 0..1, so the frequency-only roll
 * (keepsAt, predictRejects) stays a safe "certainly no".
 *
 * Mixins: ChunkGeneratorStructureFrequencyMixin (the roll and the veto), StructureCheckFrequencyMixin.
 */
public final class StructureFrequencies {

	private static final Logger LOG = LogManager.getLogger("creativeworldgen/structure-frequency");

	private static final Field CHECK_SEED = field(StructureCheck.class, "f_197245_", "seed");

	/** Staged by structure_overrides.js at the latest datapack load: set id -> structure id -> frequency. */
	private static volatile Map<String, Map<String, Float>> staged = Map.of();
	/** Staged with it: set id -> biome rule ("#tag", "biome id" or "default") -> factor, in rule order. */
	private static volatile Map<String, Map<String, Float>> stagedFactors = Map.of();
	/** What generation uses; see the class doc. */
	private static volatile Active active = Active.EMPTY;

	/** One member of a set that has at least one frequency below 1 or biome factors (null when it has none). */
	record Member(StructureSet set, float frequency, int idHash, Factors factors) {
		Member(StructureSet set, float frequency, int idHash) { this(set, frequency, idHash, null); }
	}

	/** One biome_frequency rule: a biome tag or a biome id. */
	record Rule(TagKey<Biome> tag, ResourceLocation biome, float factor) {
		boolean matches(Holder<Biome> b) {
			return tag != null ? b.is(tag) : b.is(biome);
		}
	}

	/** A set's biome factors: the first rule the biome matches, else the fallback ("default", 1 when not given). */
	record Factors(List<Rule> rules, float fallback) {
		float at(Holder<Biome> biome) {
			if (biome == null) return fallback;
			for (Rule r : rules) if (r.matches(biome)) return r.factor;
			return fallback;
		}
		static Factors parse(Map<String, Float> raw) {
			List<Rule> rules = new ArrayList<>();
			float fallback = 1.0F;
			for (Map.Entry<String, Float> e : raw.entrySet()) {
				String k = e.getKey();
				if (k.equals("default")) fallback = e.getValue();
				else if (k.startsWith("#")) rules.add(new Rule(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(k.substring(1))), null, e.getValue()));
				else rules.add(new Rule(null, new ResourceLocation(k), e.getValue()));
			}
			return new Factors(List.copyOf(rules), fallback);
		}
	}

	static final class Active {
		static final Active EMPTY = new Active(new IdentityHashMap<>(), 0, 0);
		final Map<Structure, Member> members;
		final int sets;
		final int rolled;
		Active(Map<Structure, Member> members, int sets, int rolled) {
			this.members = members; this.sets = sets; this.rolled = rolled;
		}
	}

	/** The slot a roll just rejected, on this worldgen thread: that chunk (weakly held) and that set. */
	private record Veto(WeakReference<Object> chunk, StructureSet set) {}
	private static final ThreadLocal<Veto> VETO = new ThreadLocal<>();

	/** The anchor the last meta structure start on this thread chose, for that meta at that chunk. */
	private record Anchor(Structure meta, Structure anchor, int chunkX, int chunkZ) {}
	private static final ThreadLocal<Anchor> ANCHOR = new ThreadLocal<>();

	private StructureFrequencies() {}

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
				throw new IllegalStateException("StructureFrequencies: no field " + srg + "/" + mojmap + " on " + c.getName(), e);
			}
		}
	}

	// ── staging (called from KubeJS) ─────────────────────────────────────────

	/** {"set id": {"structure id": frequency}}; "{}" clears. Bad input stages nothing and is logged. */
	public static void stage(String json) {
		Map<String, Map<String, Float>> table = new LinkedHashMap<>();
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, JsonElement> set : root.entrySet()) {
				Map<String, Float> members = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> m : set.getValue().getAsJsonObject().entrySet()) {
					float f = m.getValue().getAsFloat();
					if (!(f >= 0 && f <= 1)) throw new IllegalArgumentException(m.getKey() + " frequency " + f + " outside 0..1");
					members.put(m.getKey(), f);
				}
				table.put(set.getKey(), members);
			}
		} catch (RuntimeException e) {
			LOG.error("[structure-frequency] bad table from structure_overrides.js, staging nothing: {}", e.toString());
			table.clear();
		}
		staged = table;
	}

	/** {"set id": {"#biome tag" | "biome id" | "default": factor}} (a group's biome_frequency); "{}" clears. */
	public static void stageBiomeFactors(String json) {
		Map<String, Map<String, Float>> table = new LinkedHashMap<>();
		try {
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, JsonElement> set : root.entrySet()) {
				Map<String, Float> rules = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> r : set.getValue().getAsJsonObject().entrySet()) {
					float f = r.getValue().getAsFloat();
					if (!(f >= 0 && f <= 1)) throw new IllegalArgumentException(set.getKey() + " biome factor " + r.getKey() + " = " + f + " outside 0..1");
					String k = r.getKey();
					if (!k.equals("default")) new ResourceLocation(k.startsWith("#") ? k.substring(1) : k);   // throws on a bad id
					rules.put(k, f);
				}
				table.put(set.getKey(), rules);
			}
		} catch (RuntimeException e) {
			LOG.error("[structure-frequency] bad biome factor table from structure_overrides.js, staging none: {}", e.toString());
			table.clear();
		}
		stagedFactors = table;
	}

	// ── lifecycle ────────────────────────────────────────────────────────────

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		activate(event.getServer());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		active = Active.EMPTY;
		staged = Map.of();
		stagedFactors = Map.of();
	}

	static void activate(MinecraftServer server) {
		if (!WorldgenConfig.structureFrequenciesEnabled()) {
			active = Active.EMPTY;
			LOG.info("[structure-frequency] off in CWGConfig [worldgen.systems]: every structure spawns whenever it wins its slot");
			return;
		}
		Registry<StructureSet> sets = server.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		active = resolve(staged, stagedFactors, id -> Optional.ofNullable(sets.get(new ResourceLocation(id))));
		if (active.sets > 0) {
			LOG.info("[structure-frequency] {} structure(s) in {} set(s) roll a frequency below 1", active.rolled, active.sets);
		}
		if (!stagedFactors.isEmpty()) {
			LOG.info("[structure-frequency] biome factors for {} set(s): {}", stagedFactors.size(), stagedFactors);
		}
	}

	/** Resolves a staged table against set lookups. Package-private for headless tests. */
	static Active resolve(Map<String, Map<String, Float>> table, java.util.function.Function<String, Optional<StructureSet>> lookup) {
		return resolve(table, Map.of(), lookup);
	}

	static Active resolve(Map<String, Map<String, Float>> table, Map<String, Map<String, Float>> factorTable,
			java.util.function.Function<String, Optional<StructureSet>> lookup) {
		Map<Structure, Member> members = new IdentityHashMap<>();
		int setCount = 0;
		int rolled = 0;
		Set<String> setIds = new LinkedHashSet<>(table.keySet());
		setIds.addAll(factorTable.keySet());
		for (String setId : setIds) {
			Optional<StructureSet> found = lookup.apply(setId);
			if (found.isEmpty()) {
				LOG.warn("[structure-frequency] structure set {} is not loaded; its frequencies are ignored", setId);
				continue;
			}
			StructureSet set = found.get();
			Map<String, Float> wanted = new LinkedHashMap<>(table.getOrDefault(setId, Map.of()));
			Factors factors = factorTable.containsKey(setId) ? Factors.parse(factorTable.get(setId)) : null;
			setCount++;
			// every member of the set: a slot vetoed by one member's roll must stop the others too
			for (StructureSet.StructureSelectionEntry entry : set.structures()) {
				String id = key(entry.structure());
				if (id == null) continue;
				Float f = wanted.remove(id);
				Structure structure = entry.structure().value();
				if (members.containsKey(structure)) {
					LOG.warn("[structure-frequency] {} is in two sets with frequencies; only the first is used", id);
					continue;
				}
				float freq = f == null ? 1.0F : f;
				if (freq < 1.0F || factors != null) rolled++;
				members.put(structure, new Member(set, freq, LegacyIds.seed(id).hashCode(), factors));
			}
			for (String missing : wanted.keySet()) {
				LOG.warn("[structure-frequency] {} is not a member of {}; its frequency is ignored", missing, setId);
			}
		}
		return new Active(members, setCount, rolled);
	}

	private static String key(Holder<Structure> holder) {
		return holder.unwrapKey().map(ResourceKey::location).map(ResourceLocation::toString).orElse(null);
	}

	// ── the roll ─────────────────────────────────────────────────────────────

	/** Uniform [0, 1) from (seed, chunk, structure). SplitMix64 finalizer per input. */
	static double roll(long seed, int chunkX, int chunkZ, int idHash) {
		long h = mix(seed ^ 0x6A09E667F3BCC909L);
		h = mix(h + chunkX * 0x9E3779B97F4A7C15L);
		h = mix(h + chunkZ * 0xC2B2AE3D27D4EB4FL);
		h = mix(h + idHash * 0x165667B19E3779F9L);
		return (h >>> 11) * 0x1.0p-53;
	}

	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	static boolean keeps(Member m, long seed, int chunkX, int chunkZ) {
		return m.frequency >= 1.0F || roll(seed, chunkX, chunkZ, m.idHash) < m.frequency;
	}

	/** The roll with the biome factor: frequency x factor (factor 1 = keeps). */
	static boolean keeps(Member m, long seed, int chunkX, int chunkZ, float factor) {
		double f = (double) m.frequency * factor;
		return f >= 1.0 || roll(seed, chunkX, chunkZ, m.idHash) < f;
	}

	/** The factor for this member at its generation point's biome (1 without factors; the sample only runs then). */
	static float factorOf(Member m, Supplier<Holder<Biome>> sample) {
		if (m == null || m.factors == null) return 1.0F;
		return m.factors.at(sample == null ? null : sample.get());
	}

	/** The structure has biome factors (only then is a generation point worth finding for the roll). */
	public static boolean hasBiomeFactors(Structure structure) {
		Member m = active.members.get(structure);
		return m != null && m.factors != null;
	}

	/**
	 * The biome at the structure's generation point in that chunk, as Structure#generate tests it; null when it has no
	 * point there. findGenerationPoint's random is seeded from the seed and chunk, so this is the point generate used.
	 */
	public static Holder<Biome> sampleBiome(Structure structure, RegistryAccess access, ChunkGenerator generator, RandomState randomState,
			StructureTemplateManager templates, long seed, ChunkPos pos, ChunkAccess chunk) {
		try {
			Structure.GenerationContext ctx = new Structure.GenerationContext(access, generator, generator.getBiomeSource(), randomState,
					templates, seed, pos, chunk, b -> true);
			return structure.findGenerationPoint(ctx).map(stub -> biomeAt(generator, randomState, stub.position())).orElse(null);
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Holder<Biome> biomeAt(ChunkGenerator generator, RandomState randomState, BlockPos p) {
		return generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(p.getX()), QuartPos.fromBlock(p.getY()), QuartPos.fromBlock(p.getZ()),
				randomState.sampler());
	}

	/**
	 * LocatePrediction#plain: the structure's roll with its biome factor rejects it at this point's biome. Only for
	 * members with factors; the frequency-only part already ran at canCreateStructure's head.
	 */
	public static boolean factorRejects(Structure structure, long seed, ChunkPos pos, Holder<Biome> biome) {
		Member m = active.members.get(structure);
		if (m == null || m.factors == null) return false;
		return !keeps(m, seed, pos.x, pos.z, m.factors.at(biome));
	}

	/** The structure's own roll at that chunk keeps it (true for a structure with no frequency). Ignores biome factors
	 *  (they only lower it), so it is an upper bound: a "no" is certain, a "yes" may still lose the biome roll. */
	static boolean keepsAt(Structure structure, long seed, int chunkX, int chunkZ) {
		Member m = active.members.get(structure);
		return m == null || keeps(m, seed, chunkX, chunkZ);
	}

	// ── mixin entry points ───────────────────────────────────────────────────

	/** ChunkGenerator#tryGenerateStructure HEAD: an earlier roll in this slot already emptied it (never for a companion). */
	public static boolean slotVetoed(StructureSet.StructureSelectionEntry entry, ChunkAccess chunk) {
		return !PlacementPriority.companionMode() && vetoed(entry.structure().value(), chunk);
	}

	/** ChunkGenerator#tryGenerateStructure, the structure generated and is about to be stored: roll it. */
	public static boolean rejects(StructureSet.StructureSelectionEntry entry, long seed, ChunkPos pos, ChunkAccess chunk) {
		return rejects(entry, seed, pos, chunk, null);
	}

	/** The same, with the biome at its generation point for members with biome factors (asked only for those). */
	public static boolean rejects(StructureSet.StructureSelectionEntry entry, long seed, ChunkPos pos, ChunkAccess chunk,
			Supplier<Holder<Biome>> sample) {
		if (PlacementPriority.companionMode()) return false;   // companions (PlacementPriority) take no slot of their own
		return reject(entry.structure().value(), seed, pos.x, pos.z, chunk, sample);
	}

	// The chunk is only an identity here (which slot), so tests can pass any object.
	static boolean vetoed(Structure structure, Object chunk) {
		Veto v = VETO.get();
		if (v == null) return false;
		Member m = active.members.get(structure);
		return m != null && m.set == v.set && v.chunk.get() == chunk;
	}

	static boolean reject(Structure structure, long seed, int chunkX, int chunkZ, Object chunk) {
		return reject(structure, seed, chunkX, chunkZ, chunk, null);
	}

	static boolean reject(Structure structure, long seed, int chunkX, int chunkZ, Object chunk, Supplier<Holder<Biome>> sample) {
		Member m = active.members.get(structure);
		Member a = anchorMember(structure, chunkX, chunkZ);
		if ((m == null || keeps(m, seed, chunkX, chunkZ, factorOf(m, sample))) && (a == null || keeps(a, seed, chunkX, chunkZ))) return false;
		VETO.set(new Veto(new WeakReference<>(chunk), m != null ? m.set : a.set));
		return true;
	}

	/**
	 * MetaStructure#findGenerationPoint: this meta's start at that chunk is built on that anchor (null: no anchor,
	 * or it failed). Read, not consumed, by the roll: earth fill asks before the real roll does.
	 */
	public static void anchorChosen(Structure meta, Structure anchor, int chunkX, int chunkZ) {
		if (anchor == null) ANCHOR.remove();
		else ANCHOR.set(new Anchor(meta, anchor, chunkX, chunkZ));
	}

	/** The rolled member of the anchor a meta start chose, or null. */
	private static Member anchorMember(Structure structure, int chunkX, int chunkZ) {
		if (!(structure instanceof MetaStructure)) return null;
		Anchor p = ANCHOR.get();
		if (p == null || p.meta != structure || p.chunkX != chunkX || p.chunkZ != chunkZ) return null;
		return active.members.get(p.anchor);
	}


	/** PlacementPriority's predictions run generation inside a generation: set this thread's roll state aside. */
	static Object[] saveThreadState() { return new Object[] { VETO.get(), ANCHOR.get() }; }

	static void restoreThreadState(Object[] state) {
		if (state[0] != null) VETO.set((Veto) state[0]); else VETO.remove();
		if (state[1] != null) ANCHOR.set((Anchor) state[1]); else ANCHOR.remove();
	}

	/** Headless tests: install a resolved table directly. */
	static void setActiveForTest(Active a) {
		active = a;
		VETO.remove();
		ANCHOR.remove();
	}

	/** StructureCheck#canCreateStructure HEAD: /locate and map searches skip slots the roll rejects. */
	public static boolean predictRejects(StructureCheck check, Structure structure, ChunkPos pos) {
		Member m = active.members.get(structure);
		if (m == null || m.frequency >= 1.0F) return false;
		try {
			return !keeps(m, CHECK_SEED.getLong(check), pos.x, pos.z);
		} catch (IllegalAccessException e) {
			return false;
		}
	}
}
