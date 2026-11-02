package com.SaintAlfonzo.creativedelight.oceanic.upstream;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.SaintAlfonzo.creativedelight.oceanic.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.oceanic.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jar the rebuild reads its art and data from (feature_writeup/ocean_delight.md 5.6). It sits in
 * {@code <game dir>/oceanic_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1, so the translation
 * only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "oceanic_upstream";

    public static final Jar OCEANIC_101 = new Jar("oceanic_delight-1.0.1-forge-1.19.2.jar", "91fd8842a5bf9b55c2d9fb39f721129bc6cae1b5",
            "https://cdn.modrinth.com/data/O1qboAbB/versions/9JkMCuAa/oceanic_delight-1.0.1-forge-1.19.2.jar (Modrinth, Oceanic Delight 1.0.1 for Forge 1.19.2)");

    private static Map<String, byte[]> oceanic101;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Oceanic Delight (Creative Delight)", "the original Oceanic Delight jar", OceanicDelightMod.LOGGER::error, OCEANIC_101);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> oceanic101() {
        if (oceanic101 == null) {
            oceanic101 = DonorJars.read(DonorJars.path(FOLDER, OCEANIC_101), "assets/", "data/");
        }
        return oceanic101;
    }
}
