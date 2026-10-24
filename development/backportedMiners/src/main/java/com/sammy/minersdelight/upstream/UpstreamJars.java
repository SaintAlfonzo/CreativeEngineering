package com.sammy.minersdelight.upstream;

import com.sammy.minersdelight.MinersDelightMod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The unmodified upstream jar the port reads its art and data from (feature_writeup/miners_delight.md D2, D12). It sits
 * in {@code <game dir>/miners_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1, so the
 * translation only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "miners_upstream";

    public record Jar(String file, String sha1, String source) {
        public Path path() {
            return FMLPaths.GAMEDIR.get().resolve(FOLDER).resolve(file);
        }
    }

    public static final Jar MINERS_145 = new Jar("miners_delight-1.20.1-1.4.5-backport.jar", "638438679800711939e7a8029ae3d5340f8c17bd",
            "https://cdn.modrinth.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1.20.1-1.4.5-backport.jar (Modrinth, Miner's Delight 1.20.1-1.4.5-backport for Forge 1.20.1)");

    private static Map<String, byte[]> miners145;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        Path path = MINERS_145.path();
        String problem = null;
        if (!Files.isRegularFile(path)) {
            problem = "\n  missing: " + path + "\n    get it from " + MINERS_145.source();
        } else {
            String sha1 = sha1(path);
            if (!MINERS_145.sha1().equals(sha1)) {
                problem = "\n  wrong file: " + path + " (sha1 " + sha1 + ", expected " + MINERS_145.sha1() + ")\n    get the exact file from "
                        + MINERS_145.source();
            }
        }
        if (problem != null) {
            String message = "Miner's Delight (Backport) reads its textures, models and recipes from the original Miner's Delight jar, "
                    + "which must be in the folder '" + FOLDER + "' of the game directory (not in mods/):" + problem;
            MinersDelightMod.LOGGER.error(message);
            throw new IllegalStateException(message);
        }
    }

    public static synchronized Map<String, byte[]> miners145() {
        if (miners145 == null) {
            miners145 = read(MINERS_145.path());
        }
        return miners145;
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    private static Map<String, byte[]> read(Path jar) {
        Map<String, byte[]> entries = new HashMap<>();
        try (InputStream in = Files.newInputStream(jar); ZipInputStream zip = new ZipInputStream(in)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                String name = entry.getName();
                if (!entry.isDirectory() && (name.startsWith("assets/") || name.startsWith("data/"))) {
                    entries.put(name, zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + jar, e);
        }
        return entries;
    }

    private static String sha1(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path)));
        } catch (IOException | NoSuchAlgorithmException e) {
            return "unreadable (" + e.getMessage() + ")";
        }
    }
}
