package net.satisfy.vinery.forge.upstream;

import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.forge.shared.DonorJars;
import net.satisfy.vinery.forge.shared.DonorJars.Jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * The unmodified upstream jars the port reads its art and data from (feature_writeup/alcohol.md B4.8, D13 B). They sit
 * in {@code <game dir>/vinery_upstream/}, never in {@code mods/}: FML would reject the Fabric jar, and the 1.3.10 jar
 * has this mod's id. Both are pinned by sha1, so the translation only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "vinery_upstream";

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
        DonorJars.verifyOrFail(FOLDER, "Let's Do Vinery (Backport)", "the original Vinery jars", Vinery.LOGGER::error, VINERY_155, VINERY_1310);
    }

    public static synchronized Map<String, byte[]> vinery155() {
        if (vinery155 == null) {
            vinery155 = read(DonorJars.path(FOLDER, VINERY_155));
        }
        return vinery155;
    }

    public static synchronized Map<String, byte[]> foundation() {
        if (foundation == null) {
            byte[] nested = DonorJars.readEntry(DonorJars.path(FOLDER, VINERY_155), FOUNDATION_ENTRY);
            foundation = nested == null ? Map.of() : read(new ByteArrayInputStream(nested));
        }
        return foundation;
    }

    public static synchronized Map<String, byte[]> vinery1310() {
        if (vinery1310 == null) {
            vinery1310 = read(DonorJars.path(FOLDER, VINERY_1310));
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
        return DonorJars.read(raw, "Could not read an upstream Vinery jar", "assets/", "data/", "resourcepacks/");
    }
}
