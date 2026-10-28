package com.SaintAlfonzo.creativedelight.rustic.upstream;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import com.SaintAlfonzo.creativedelight.rustic.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.rustic.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jar the port reads its art and data from (feature_writeup/rustic_delight.md 5). It sits in
 * {@code <game dir>/rustic_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1, so the translation
 * only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "rustic_upstream";

    public static final Jar RUSTIC_170 = new Jar("rusticdelight-forge-1.20.1-1.7.0.jar", "bca9adf60b59951ae5049149185da64a6f7b6f8a",
            "https://cdn.modrinth.com/data/foa4fGIH/versions/z29Gd3ec/rusticdelight-forge-1.20.1-1.7.0.jar (Modrinth, Rustic Delight 1.7.0 for Forge 1.20.1)");

    private static Map<String, byte[]> rustic170;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Rustic Delight (Creative Delight)", "the original Rustic Delight jar", RusticDelightMod.LOGGER::error, RUSTIC_170);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> rustic170() {
        if (rustic170 == null) {
            rustic170 = DonorJars.read(DonorJars.path(FOLDER, RUSTIC_170), "assets/", "data/");
        }
        return rustic170;
    }
}
