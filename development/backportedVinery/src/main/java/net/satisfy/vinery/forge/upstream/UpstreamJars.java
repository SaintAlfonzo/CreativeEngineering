package net.satisfy.vinery.forge.upstream;

import net.minecraftforge.fml.loading.FMLPaths;
import net.satisfy.vinery.core.Vinery;

import java.io.ByteArrayInputStream;
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
 * The unmodified upstream jars the port reads its art and data from (feature_writeup/alcohol.md B4.8, D13 B). They sit
 * in {@code <game dir>/vinery_upstream/}, never in {@code mods/}: FML would reject the Fabric jar, and the 1.3.10 jar
 * has this mod's id. Both are pinned by sha1, so the translation only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "vinery_upstream";

    public record Jar(String file, String sha1, String source) {
        public Path path() {
            return FMLPaths.GAMEDIR.get().resolve(FOLDER).resolve(file);
        }
    }

    public static final Jar VINERY_155 = new Jar("letsdo-vinery-fabric-1.5.5.jar", "9d4c7646b91219a75a432b6dea1d7a7c41faaaec",
            "https://cdn.modrinth.com/data/1DWmBJVA/versions/PDi0T5xs/letsdo-vinery-fabric-1.5.5.jar (Modrinth, Let's Do Vinery 1.5.5 for Fabric 1.21.1)");
    public static final Jar VINERY_1310 = new Jar("letsdo-vinery-forge-1.3.10.jar", "fbbd4ae7c10a9d0aa13819e09b70167f3b4ff81c",
            "https://www.curseforge.com/minecraft/mc-mods/lets-do-vinery/files (Let's Do Vinery 1.3.10 for Forge 1.19.2)");
    /** Nested in the 1.5.5 jar. */
    private static final String FOUNDATION_ENTRY = "META-INF/jars/foundation-fabric-1.0.1.jar";

    private static Map<String, byte[]> vinery155;
    private static Map<String, byte[]> foundation;
    private static Map<String, byte[]> vinery1310;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when a jar is missing or not the expected file. */
    public static void verifyOrFail() {
        StringBuilder problems = new StringBuilder();
        for (Jar jar : new Jar[]{VINERY_155, VINERY_1310}) {
            Path path = jar.path();
            if (!Files.isRegularFile(path)) {
                problems.append("\n  missing: ").append(path).append("\n    get it from ").append(jar.source());
                continue;
            }
            String sha1 = sha1(path);
            if (!jar.sha1().equals(sha1)) {
                problems.append("\n  wrong file: ").append(path).append(" (sha1 ").append(sha1).append(", expected ").append(jar.sha1())
                        .append(")\n    get the exact file from ").append(jar.source());
            }
        }
        if (!problems.isEmpty()) {
            String message = "Let's Do Vinery (Backport) reads its textures, models and recipes from the original Vinery jars, "
                    + "which must be in the folder '" + FOLDER + "' of the game directory (not in mods/):" + problems;
            Vinery.LOGGER.error(message);
            throw new IllegalStateException(message);
        }
    }

    public static synchronized Map<String, byte[]> vinery155() {
        if (vinery155 == null) {
            vinery155 = read(VINERY_155.path());
        }
        return vinery155;
    }

    public static synchronized Map<String, byte[]> foundation() {
        if (foundation == null) {
            byte[] nested = read(VINERY_155.path(), FOUNDATION_ENTRY);
            foundation = nested == null ? Map.of() : read(new ByteArrayInputStream(nested));
        }
        return foundation;
    }

    public static synchronized Map<String, byte[]> vinery1310() {
        if (vinery1310 == null) {
            vinery1310 = read(VINERY_1310.path());
        }
        return vinery1310;
    }

    /** Only resource entries ({@code assets/}, {@code data/}, built-in resource packs). */
    private static Map<String, byte[]> read(Path jar) {
        try (InputStream in = Files.newInputStream(jar)) {
            return read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + jar, e);
        }
    }

    private static Map<String, byte[]> read(InputStream raw) {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(raw)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                String name = entry.getName();
                if (!entry.isDirectory() && (name.startsWith("assets/") || name.startsWith("data/") || name.startsWith("resourcepacks/"))) {
                    entries.put(name, zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read an upstream Vinery jar", e);
        }
        return entries;
    }

    private static byte[] read(Path jar, String entryName) {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(jar))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                if (entry.getName().equals(entryName)) {
                    return zip.readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + jar, e);
        }
        return null;
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
