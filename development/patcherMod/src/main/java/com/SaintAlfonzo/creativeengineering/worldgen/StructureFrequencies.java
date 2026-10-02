package com.SaintAlfonzo.creativeengineering.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
 * Mixins: ChunkGeneratorStructureFrequencyMixin (the roll and the veto), StructureCheckFrequencyMixin.
 */
public final class StructureFrequencies {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/structure-frequency");

	private static final Field CHECK_SEED = field(StructureCheck.class, "f_197245_", "seed");

	/** Staged by structure_overrides.js at the latest datapack load: set id -> structure id -> frequency. */
	private static volatile Map<String, Map<String, Float>> staged = Map.of();
	/** What generation uses; see the class doc. */
	private static volatile Active active = Active.EMPTY;

	/** One member of a set that has at least one frequency below 1. */
	record Member(StructureSet set, float frequency, int idHash) {}

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

	// ── lifecycle ────────────────────────────────────────────────────────────

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		activate(event.getServer());
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		active = Active.EMPTY;
		staged = Map.of();
	}

	static void activate(MinecraftServer server) {
		if (!WorldgenConfig.structureFrequenciesEnabled()) {
			active = Active.EMPTY;
			LOG.info("[structure-frequency] off in CEConfig [worldgen.systems]: every structure spawns whenever it wins its slot");
			return;
		}
		Registry<StructureSet> sets = server.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		active = resolve(staged, id -> Optional.ofNullable(sets.get(new ResourceLocation(id))));
		if (active.sets > 0) {
			LOG.info("[structure-frequency] {} structure(s) in {} set(s) roll a frequency below 1", active.rolled, active.sets);
		}
	}

	/** Resolves a staged table against set lookups. Package-private for headless tests. */
	static Active resolve(Map<String, Map<String, Float>> table, java.util.function.Function<String, Optional<StructureSet>> lookup) {
		Map<Structure, Member> members = new IdentityHashMap<>();
		int setCount = 0;
		int rolled = 0;
		for (Map.Entry<String, Map<String, Float>> e : table.entrySet()) {
			Optional<StructureSet> found = lookup.apply(e.getKey());
			if (found.isEmpty()) {
				LOG.warn("[structure-frequency] structure set {} is not loaded; its frequencies are ignored", e.getKey());
				continue;
			}
			StructureSet set = found.get();
			Map<String, Float> wanted = new LinkedHashMap<>(e.getValue());
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
				if (freq < 1.0F) rolled++;
				members.put(structure, new Member(set, freq, id.hashCode()));
			}
			for (String missing : wanted.keySet()) {
				LOG.warn("[structure-frequency] {} is not a member of {}; its frequency is ignored", missing, e.getKey());
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

	// ── mixin entry points ───────────────────────────────────────────────────

	/** ChunkGenerator#tryGenerateStructure HEAD: an earlier roll in this slot already emptied it. */
	public static boolean slotVetoed(StructureSet.StructureSelectionEntry entry, ChunkAccess chunk) {
		return vetoed(entry.structure().value(), chunk);
	}

	/** ChunkGenerator#tryGenerateStructure, the structure generated and is about to be stored: roll it. */
	public static boolean rejects(StructureSet.StructureSelectionEntry entry, long seed, ChunkPos pos, ChunkAccess chunk) {
		return reject(entry.structure().value(), seed, pos.x, pos.z, chunk);
	}

	// The chunk is only an identity here (which slot), so tests can pass any object.
	static boolean vetoed(Structure structure, Object chunk) {
		Veto v = VETO.get();
		if (v == null) return false;
		Member m = active.members.get(structure);
		return m != null && m.set == v.set && v.chunk.get() == chunk;
	}

	static boolean reject(Structure structure, long seed, int chunkX, int chunkZ, Object chunk) {
		Member m = active.members.get(structure);
		if (m == null || keeps(m, seed, chunkX, chunkZ)) return false;
		VETO.set(new Veto(new WeakReference<>(chunk), m.set));
		return true;
	}

	/** Headless tests: install a resolved table directly. */
	static void setActiveForTest(Active a) {
		active = a;
		VETO.remove();
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
