package com.SaintAlfonzo.creativedelight.rustic.upstream;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
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
 * The unmodified upstream jar the port reads its art and data from (feature_writeup/rustic_delight.md 5). It sits in
 * {@code <game dir>/rustic_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1, so the translation
 * only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "rustic_upstream";

    public record Jar(String file, String sha1, String source) {
        public Path path() {
            return FMLPaths.GAMEDIR.get().resolve(FOLDER).resolve(file);
        }
    }

    public static final Jar RUSTIC_170 = new Jar("rusticdelight-forge-1.20.1-1.7.0.jar", "bca9adf60b59951ae5049149185da64a6f7b6f8a",
            "https://cdn.modrinth.com/data/foa4fGIH/versions/z29Gd3ec/rusticdelight-forge-1.20.1-1.7.0.jar (Modrinth, Rustic Delight 1.7.0 for Forge 1.20.1)");

    private static Map<String, byte[]> rustic170;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        Path path = RUSTIC_170.path();
        String problem = null;
        if (!Files.isRegularFile(path)) {
            problem = "\n  missing: " + path + "\n    get it from " + RUSTIC_170.source();
        } else {
            String sha1 = sha1(path);
            if (!RUSTIC_170.sha1().equals(sha1)) {
                problem = "\n  wrong file: " + path + " (sha1 " + sha1 + ", expected " + RUSTIC_170.sha1() + ")\n    get the exact file from "
                        + RUSTIC_170.source();
            }
        }
        if (problem != null) {
            String message = "Rustic Delight (Creative Delight) reads its textures, models and recipes from the original Rustic Delight jar, "
                    + "which must be in the folder '" + FOLDER + "' of the game directory (not in mods/):" + problem;
            RusticDelightMod.LOGGER.error(message);
            throw new IllegalStateException(message);
        }
    }

    public static synchronized Map<String, byte[]> rustic170() {
        if (rustic170 == null) {
            rustic170 = read(RUSTIC_170.path());
        }
        return rustic170;
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
