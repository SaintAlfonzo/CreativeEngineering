// Port glue (ours, GPLv3), TP-D1: the unmodified upstream jar the port reads its art and data from.
package earth.terrarium.tempad.forge;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.shared.DonorJars;
import earth.terrarium.tempad.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified Tempad 3.0.4 jar (feature_writeup/immersive_portals.md 2.1.1, TP-D1). It sits in
 * {@code <game dir>/tempad_upstream/}, never in {@code mods/}: it is a NeoForge jar with this mod's id. Pinned by sha1,
 * so the translation only ever sees one known input. Its art is All Rights Reserved and is never copied into our jar;
 * its data files are MIT.
 */
public final class UpstreamJars {
    public static final String FOLDER = "tempad_upstream";

    public static final Jar TEMPAD_304 = new Jar("tempad-1.21.1-3.0.4-all.jar", "7a4d6837c6f7a4c9a8f4e7054952df25c148033e", "https://cdn.modrinth.com/data/gKNwt7xu/versions/T26aJH7E/tempad-1.21.1-3.0.4-all.jar (Modrinth, Tempad 3.0.4 for NeoForge 1.21.1)");

    /** Olympus (MIT, Terrarium), the UI library upstream nests in its jar; its GUI art and lang are used by the screens. */
    public static final String OLYMPUS_NESTED = "META-INF/jarjar/olympus-neoforge-1.21-1.0.18.jar";
    private static Map<String, byte[]> tempad304;
    private static Map<String, byte[]> olympus;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Tempad", "the original Tempad 3.0.4 jar", Tempad.LOGGER::error, TEMPAD_304);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> tempad304() {
        if (tempad304 == null) {
            tempad304 = DonorJars.read(DonorJars.path(FOLDER, TEMPAD_304), "assets/", "data/");
        }
        return tempad304;
    }

    /** The {@code assets/olympus/} entries of the Olympus jar nested in the upstream jar, read in memory. */
    public static synchronized Map<String, byte[]> olympus() {
        if (olympus == null) {
            byte[] nested = DonorJars.readEntry(DonorJars.path(FOLDER, TEMPAD_304), OLYMPUS_NESTED);
            olympus = nested == null ? Map.of()
                    : DonorJars.read(new java.io.ByteArrayInputStream(nested), "Could not read " + OLYMPUS_NESTED, "assets/olympus/");
        }
        return olympus;
    }
}
