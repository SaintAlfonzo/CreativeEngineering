package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain-text file access for the KubeJS worldgen scripts (kubejs/server_scripts/worldgen/).
 * KubeJS's class filter blocks java.io / java.nio, and JsonIO can only write JSON objects, so a
 * script that writes a markdown report goes through here. Confined to the instance's kubejs/
 * folder; paths are relative to the instance root, same as JsonIO's.
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

	/** File contents, or null when it doesn't exist. */
	public static String readText(String rel) throws IOException {
		Path p = resolve(rel);
		return Files.isRegularFile(p) ? Files.readString(p, StandardCharsets.UTF_8) : null;
	}

	/** Writes only when the content differs from what's on disk. Returns true if it wrote. */
	public static boolean writeText(String rel, String content) throws IOException {
		Path p = resolve(rel);
		if (Files.isRegularFile(p) && Files.readString(p, StandardCharsets.UTF_8).equals(content)) return false;
		Files.createDirectories(p.getParent());
		Files.writeString(p, content, StandardCharsets.UTF_8);
		return true;
	}
}
