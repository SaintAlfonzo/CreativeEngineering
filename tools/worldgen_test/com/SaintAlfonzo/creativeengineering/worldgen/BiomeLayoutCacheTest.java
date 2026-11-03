package com.SaintAlfonzo.creativeengineering.worldgen;

import java.lang.reflect.Field;
import com.SaintAlfonzo.creativeengineering.util.WorldgenFiles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Headless checks of BiomeLayoutTuner's disk cache (perf.md 11.2 #3) on a copy of the real biome inputs: a solve
 * writes biome_layout_cache.json.gz; a new JVM (the in-memory cache cleared) reads it back with the same files and
 * leaves biome_config.json alone; a wrong key, a corrupt file or an edited input solves again. Args: instance root.
 */
public class BiomeLayoutCacheTest {
	static int fails = 0, checks = 0;

	static void check(boolean ok, String what) {
		checks++;
		if (!ok) fails++;
		System.out.println((ok ? "ok   " : "FAIL ") + what);
	}

	/** What a fresh JVM sees: no in-memory cache. */
	// WorldgenFiles moved to the util package (2026-11-01): its package-private root override is set by reflection.
	static void setRootOverride(Path root) throws Exception {
		Field f = WorldgenFiles.class.getDeclaredField("rootOverride");
		f.setAccessible(true);
		f.set(null, root);
	}

	static void clearMemory() throws Exception {
		for (String name : new String[] {"cacheKey", "cached"}) {
			Field f = BiomeLayoutTuner.class.getDeclaredField(name);
			f.setAccessible(true);
			f.set(null, null);
		}
	}

	static void copyTree(Path from, Path to) throws Exception {
		try (Stream<Path> s = Files.walk(from)) {
			for (Path p : (Iterable<Path>) s::iterator) {
				Path q = to.resolve(from.relativize(p).toString());
				if (Files.isDirectory(p)) Files.createDirectories(q);
				else Files.copy(p, q);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Path instance = Path.of(args[0]);
		Path root = Files.createTempDirectory("biome_cache_test");
		try {
			String wg = BiomeLayoutTuner.WORLDGEN_DIR;
			Files.createDirectories(root.resolve(wg));
			Files.copy(instance.resolve(BiomeLayoutTuner.CONFIG), root.resolve(BiomeLayoutTuner.CONFIG));
			copyTree(instance.resolve(wg + "biome_baselines"), root.resolve(wg + "biome_baselines"));
			copyTree(instance.resolve(BiomeLayoutTuner.SAMPLE_DIR), root.resolve(BiomeLayoutTuner.SAMPLE_DIR));
			setRootOverride(root);
			Path cache = root.resolve(BiomeLayoutTuner.DISK_CACHE);
			Path cfg = root.resolve(BiomeLayoutTuner.CONFIG);

			// A solve may rewrite the config's stats (and logs "updated stats" into the cache's messages): settle the
			// copied config with one throwaway solve, then start from no cache so later re-solves compare like for like.
			clearMemory();
			BiomeLayoutTuner.run();
			Files.deleteIfExists(cache);
			clearMemory();
			BiomeLayoutTuner.Result first = BiomeLayoutTuner.run();
			check(first.getErrors().isEmpty(), "real inputs solve without errors " + first.getErrors());
			check(!first.isFromCache() && first.getCacheSource().isEmpty(), "first run solves");
			check(Files.isRegularFile(cache), "solve writes the disk cache");
			check(!first.getFiles().isEmpty(), "solve gives " + first.getFiles().size() + " dimension override(s)");
			Map<String, String> solved = first.getFiles();

			clearMemory();
			String cfgBefore = Files.readString(cfg, StandardCharsets.UTF_8);
			long cfgTime = Files.getLastModifiedTime(cfg).toMillis();
			long t0 = System.nanoTime();
			BiomeLayoutTuner.Result disk = BiomeLayoutTuner.run();
			long ms = (System.nanoTime() - t0) / 1_000_000;
			check(disk.isFromCache() && "disk".equals(disk.getCacheSource()), "new JVM reads the disk cache (" + ms + " ms)");
			check(disk.getFiles().equals(solved), "disk files equal the solved files");
			check(disk.getErrors().isEmpty(), "disk hit has no errors");
			check(Files.readString(cfg, StandardCharsets.UTF_8).equals(cfgBefore) && Files.getLastModifiedTime(cfg).toMillis() == cfgTime,
				"disk hit leaves biome_config.json untouched");

			BiomeLayoutTuner.Result mem = BiomeLayoutTuner.run();
			check(mem.isFromCache() && "memory".equals(mem.getCacheSource()), "next run in the same JVM uses memory");

			// Wrong key: same file, key altered.
			String text = WorldgenFiles.readGzipText(BiomeLayoutTuner.DISK_CACHE);
			String keyed = text.replaceFirst("\"key\":\"[0-9a-f]", "\"key\":\"x");
			check(!keyed.equals(text), "key found in the cache file");
			WorldgenFiles.writeGzipText(BiomeLayoutTuner.DISK_CACHE, keyed);
			clearMemory();
			BiomeLayoutTuner.Result rekey = BiomeLayoutTuner.run();
			check(!rekey.isFromCache() && rekey.getFiles().equals(solved), "other key: solves again, same files");
			check(text.equals(WorldgenFiles.readGzipText(BiomeLayoutTuner.DISK_CACHE)), "re-solve rewrites the same cache");

			// Corrupt file.
			Files.write(cache, new byte[] {1, 2, 3});
			clearMemory();
			BiomeLayoutTuner.Result corrupt = BiomeLayoutTuner.run();
			check(!corrupt.isFromCache() && corrupt.getErrors().isEmpty() && corrupt.getFiles().equals(solved), "corrupt file: solves, no error");

			// Edited input: a changed sample file is a new key.
			Path sample;
			try (Stream<Path> s = Files.list(root.resolve(BiomeLayoutTuner.SAMPLE_DIR))) {
				sample = s.filter(p -> p.toString().endsWith(".json.gz")).sorted().findFirst().orElseThrow();
			}
			String rel = BiomeLayoutTuner.SAMPLE_DIR + sample.getFileName();
			WorldgenFiles.writeGzipText(rel, WorldgenFiles.readGzipText(rel) + " ");
			clearMemory();
			BiomeLayoutTuner.Result edited = BiomeLayoutTuner.run();
			check(!edited.isFromCache(), "edited sample: solves again");
		} finally {
			setRootOverride(null);
			try (Stream<Path> s = Files.walk(root)) {
				s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
			}
		}
		System.out.println(fails == 0 ? "ALL " + checks + " PASSED" : fails + " FAILED of " + checks);
		System.exit(fails == 0 ? 0 : 1);
	}
}
