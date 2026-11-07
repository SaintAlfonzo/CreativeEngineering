package com.SaintAlfonzo.creativecommon.util;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Plain-text file access for the KubeJS worldgen scripts (kubejs/server_scripts/worldgen/).
 * KubeJS's class filter blocks java.io / java.nio, and JsonIO can only write JSON objects, so a
 * script that writes a markdown report goes through here. Confined to the instance's kubejs/
 * folder; paths are relative to the instance root, same as JsonIO's.
 *
 * Generated data (kubejs/generation_scripts/...) also has a jar copy: when the instance file is missing (an instance
 * without the pack's kubejs/ folder), readText and readGzipText fall back to generation_scripts/... inside any loaded
 * mod jar (each Creative jar ships the files it reads). The instance file always wins, so /cdevgen output and hand
 * edits apply without a rebuild.
 */
public final class WorldgenFiles {

	/** Instance root override for headless tests; null = the running game's directory. */
	static Path rootOverride = null;

	private WorldgenFiles() {}

	static Path gameDir() {
		return rootOverride != null ? rootOverride : FMLPaths.GAMEDIR.get();
	}

	static Path resolve(String rel) {
		Path root = gameDir().toAbsolutePath().normalize();
		Path p = root.resolve(rel).normalize();
		if (!p.startsWith(root.resolve("kubejs"))) {
			throw new IllegalArgumentException("WorldgenFiles: " + rel + " is outside kubejs/");
		}
		return p;
	}

	static final String GEN_DIR = "kubejs/generation_scripts/";

	/**
	 * The instance file, else the jar copy of a kubejs/generation_scripts/ file (generation_scripts/... in the first
	 * loaded mod jar that has it), else null.
	 */
	static Path existing(String rel) {
		Path p = resolve(rel);
		if (Files.isRegularFile(p)) return p;
		String norm = rel.replace('\\', '/');
		if (rootOverride != null || !norm.startsWith(GEN_DIR)) return null;
		LoadingModList mods = LoadingModList.get();
		if (mods == null) return null;
		String inJar = "generation_scripts/" + norm.substring(GEN_DIR.length());
		for (ModFileInfo info : mods.getModFiles()) {
			try {
				Path c = info.getFile().findResource(inJar);
				if (Files.isRegularFile(c)) return c;
			} catch (RuntimeException ignored) {
				// a mod file that cannot resolve the path has no copy
			}
		}
		return null;
	}

	/** Whether rel exists in the instance or (for generated data) as a jar copy. */
	public static boolean exists(String rel) {
		return existing(rel) != null;
	}

	/** File contents (instance first, then the jar copy of generated data), or null when neither exists. */
	public static String readText(String rel) throws IOException {
		Path p = existing(rel);
		return p != null ? Files.readString(p, StandardCharsets.UTF_8) : null;
	}

	/** Writes only when the content differs from what's on disk. Returns true if it wrote. */
	public static boolean writeText(String rel, String content) throws IOException {
		Path p = resolve(rel);
		if (Files.isRegularFile(p) && Files.readString(p, StandardCharsets.UTF_8).equals(content)) return false;
		Files.createDirectories(p.getParent());
		Files.writeString(p, content, StandardCharsets.UTF_8);
		return true;
	}

	/** Contents of a gzip-compressed text file, or null when it doesn't exist. */
	public static String readGzipText(String rel) throws IOException {
		Path p = existing(rel);
		if (p == null) return null;
		try (InputStream in = new GZIPInputStream(Files.newInputStream(p))) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Gzip-compressed writeText, for large runtime inputs that ship with the pack (the /dumpclimate
	 * samples). Same "only when the content differs" rule, compared on the uncompressed text. The gzip
	 * header carries no timestamp, so equal text gives an equal file and an equal deploy hash.
	 */
	public static boolean writeGzipText(String rel, String content) throws IOException {
		if (content.equals(readGzipText(rel))) return false;
		Path p = resolve(rel);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try (GZIPOutputStream gz = new GZIPOutputStream(buf)) {
			gz.write(content.getBytes(StandardCharsets.UTF_8));
		}
		Files.createDirectories(p.getParent());
		Files.write(p, buf.toByteArray());
		return true;
	}
}
