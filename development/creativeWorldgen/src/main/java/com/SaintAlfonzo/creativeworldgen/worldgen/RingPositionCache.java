package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stronghold ring positions kept in the world's data (feature_writeup/perf.md 11.2 #2).
 *
 * <p>1.19.2 recomputes every concentric_rings structure set (vanilla strongholds, Better Strongholds) on each server
 * start: {@code ChunkGenerator#generateRingPositions} runs one {@code findBiomeHorizontal} per ring entry (128) through
 * Blueprint's modded biome source and Tectonic's climate noise, 12-15 s of worker CPU, and the first chunk that needs
 * structure starts waits on it. The result depends only on the generator, so {@code ChunkGeneratorRingCacheMixin}
 * stores it in {@code <world>/data/creativeengineering_ring_positions.json} and reads it back on the next start.
 *
 * <p>Key, all hashed together: the seed (0 on a flat world, as vanilla), the structure set id, the placement
 * (distance, spread, count, preferred biome ids), the biome source class, its codec encoding and possible biomes, the
 * mod list with versions, the selected data packs, and a probe: the biome and the raw climate point at 64 points on
 * every ring radius. Blueprint's codec writes only the wrapped vanilla source, so its slices and Tectonic's noise are
 * covered by the probe. Any change in those inputs is a new key and a normal compute. Off the server (no current
 * server) nothing is cached.
 */
public final class RingPositionCache {

	private static final Logger LOG = LogManager.getLogger("creativeengineering/worldgen");
	private static final String FILE = "creativeengineering_ring_positions.json";
	private static final int PROBE_ANGLES = 64;
	private static final int MAX_ENTRIES = 32;
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	private static final ThreadLocal<String> pendingKey = new ThreadLocal<>();

	private RingPositionCache() {}

	/** HEAD of generateRingPositions: the stored positions, or null (then the key waits for {@link #store}). */
	public static List<ChunkPos> lookup(ChunkGenerator generator, BiomeSource biomeSource, Holder<StructureSet> set,
			RandomState randomState, ConcentricRingsStructurePlacement placement) {
		pendingKey.remove();
		if (placement.count() == 0) return null;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) return null;
		long start = System.nanoTime();
		String setId = set.unwrapKey().map(k -> k.location().toString()).orElse("<direct>");
		String key;
		try {
			key = key(server, generator, biomeSource, setId, randomState, placement);
		} catch (Exception e) {
			LOG.warn("[rings] {}: no cache key, computing as vanilla", setId, e);
			return null;
		}
		List<ChunkPos> stored = read(server, key);
		long ms = (System.nanoTime() - start) / 1_000_000L;
		if (stored != null && stored.size() == placement.count()) {
			LOG.info("[rings] {}: {} positions from the world's data ({} ms)", setId, stored.size(), ms);
			return stored;
		}
		LOG.info("[rings] {}: not stored for this world and pack, computing ({} ms key)", setId, ms);
		pendingKey.set(key);
		return null;
	}

	/** RETURN of generateRingPositions: the key left by {@link #lookup} on a miss, or null. */
	public static String takePendingKey() {
		String key = pendingKey.get();
		pendingKey.remove();
		return key;
	}

	public static void store(String key, String setId, List<ChunkPos> positions) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null || positions == null) return;
		JsonArray xz = new JsonArray();
		for (ChunkPos p : positions) {
			xz.add(p.x);
			xz.add(p.z);
		}
		JsonObject entry = new JsonObject();
		entry.addProperty("set", setId);
		entry.add("xz", xz);
		synchronized (RingPositionCache.class) {
			Path file = file(server);
			try {
				JsonObject root = readRoot(file);
				root.remove(key);
				root.add(key, entry);
				Iterator<Map.Entry<String, JsonElement>> it = root.entrySet().iterator();
				for (int extra = root.size() - MAX_ENTRIES; extra > 0 && it.hasNext(); extra--) {
					it.next();
					it.remove();
				}
				Files.createDirectories(file.getParent());
				Path tmp = file.resolveSibling(FILE + ".tmp");
				Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
				Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				LOG.info("[rings] {}: {} positions stored in the world's data", setId, positions.size());
			} catch (Exception e) {
				LOG.warn("[rings] {}: could not store positions", setId, e);
			}
		}
	}

	private static List<ChunkPos> read(MinecraftServer server, String key) {
		synchronized (RingPositionCache.class) {
			try {
				JsonElement entry = readRoot(file(server)).get(key);
				if (entry == null || !entry.isJsonObject()) return null;
				JsonArray xz = entry.getAsJsonObject().getAsJsonArray("xz");
				if (xz == null || xz.size() % 2 != 0) return null;
				List<ChunkPos> out = new ArrayList<>(xz.size() / 2);
				for (int i = 0; i < xz.size(); i += 2) out.add(new ChunkPos(xz.get(i).getAsInt(), xz.get(i + 1).getAsInt()));
				return out;
			} catch (Exception e) {
				LOG.warn("[rings] unreadable {}, computing as vanilla", FILE, e);
				return null;
			}
		}
	}

	private static JsonObject readRoot(Path file) throws IOException {
		if (!Files.isRegularFile(file)) return new JsonObject();
		JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
		return root.isJsonObject() ? root.getAsJsonObject() : new JsonObject();
	}

	private static Path file(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FILE).normalize();
	}

	private static String key(MinecraftServer server, ChunkGenerator generator, BiomeSource biomeSource, String setId,
			RandomState randomState, ConcentricRingsStructurePlacement placement) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		StringBuilder sb = new StringBuilder(4096);
		boolean flat = generator instanceof FlatLevelSource;
		sb.append("v1|seed=").append(flat ? 0L : randomState.legacyLevelSeed())
				.append("|set=").append(setId)
				.append("|d=").append(placement.distance())
				.append("|s=").append(placement.spread())
				.append("|c=").append(placement.count())
				.append("|pref=").append(holderIds(placement.preferredBiomes().stream().toList()))
				.append("|src=").append(biomeSource.getClass().getName())
				.append("|possible=").append(holderIds(new ArrayList<>(biomeSource.possibleBiomes())));
		sb.append("|mods=");
		ModList.get().getMods().stream().map(m -> m.getModId() + "@" + m.getVersion()).sorted().forEach(s -> sb.append(s).append(','));
		sb.append("|packs=").append(String.join(",", server.getPackRepository().getSelectedIds()));
		md.update(sb.toString().getBytes(StandardCharsets.UTF_8));

		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
		String encoded = BiomeSource.CODEC.encodeStart(ops, biomeSource).result().map(JsonElement::toString).orElse("<unencodable>");
		md.update(encoded.getBytes(StandardCharsets.UTF_8));

		probe(md, biomeSource, randomState.sampler(), placement);
		return HexFormat.of().formatHex(md.digest());
	}

	/** Biome and climate point at PROBE_ANGLES points on each ring radius vanilla uses (same ring walk, no RNG). */
	private static void probe(MessageDigest md, BiomeSource biomeSource, Climate.Sampler sampler, ConcentricRingsStructurePlacement placement) {
		int distance = placement.distance();
		int count = placement.count();
		int spread = placement.spread();
		int ring = 0;
		int inRing = 0;
		List<Integer> radii = new ArrayList<>();
		radii.add(4 * distance);
		for (int i = 0; i < count; i++) {
			if (++inRing == spread) {
				ring++;
				inRing = 0;
				spread = Math.min(spread + 2 * spread / (ring + 1), count - i);
				if (i + 1 < count) radii.add(4 * distance + distance * ring * 6);
			}
		}
		StringBuilder sb = new StringBuilder(radii.size() * PROBE_ANGLES * 48);
		for (int r : radii) {
			for (int a = 0; a < PROBE_ANGLES; a++) {
				double angle = a * (Math.PI * 2.0) / PROBE_ANGLES;
				int qx = QuartPos.fromBlock((int) Math.round(Math.cos(angle) * r) * 16 + 8);
				int qz = QuartPos.fromBlock((int) Math.round(Math.sin(angle) * r) * 16 + 8);
				Climate.TargetPoint t = sampler.sample(qx, 0, qz);
				Holder<Biome> biome = biomeSource.getNoiseBiome(qx, 0, qz, sampler);
				sb.append(biome.unwrapKey().map(k -> k.location().toString()).orElse("?"))
						.append(t.temperature()).append(',').append(t.humidity()).append(',').append(t.continentalness()).append(',')
						.append(t.erosion()).append(',').append(t.depth()).append(',').append(t.weirdness()).append(';');
			}
		}
		md.update(sb.toString().getBytes(StandardCharsets.UTF_8));
	}

	private static <T> String holderIds(List<Holder<T>> holders) {
		return holders.stream().map(h -> h.unwrapKey().map(ResourceKey::location).map(Object::toString).orElse("?"))
				.sorted().reduce((a, b) -> a + "," + b).orElse("");
	}
}
