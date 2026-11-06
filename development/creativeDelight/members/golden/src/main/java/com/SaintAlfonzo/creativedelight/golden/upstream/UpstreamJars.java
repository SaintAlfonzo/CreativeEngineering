package com.SaintAlfonzo.creativedelight.golden.upstream;

import com.SaintAlfonzo.creativedelight.golden.GoldenFoodMod;
import com.SaintAlfonzo.creativedelight.golden.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.golden.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jar the rebuild reads its art and data from (feature_writeup/golden_food.md). It sits in
 * {@code <game dir>/goldenfood_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1, so the translation
 * only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "goldenfood_upstream";

    public static final Jar GOLDEN_200 = new Jar("goldenfood-1.19.2-2.0.0.jar", "4109e4d6d484892b8efc201b1a32b9361df930cd",
            "the Golden Food 2.0.0 release for Forge 1.19.2 by GGWPJason (CurseForge, file goldenfood-1.19.2-2.0.0.jar), or the pack's server files");

    private static Map<String, byte[]> golden200;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Golden Food (Creative Delight)", "the original Golden Food jar", GoldenFoodMod.LOGGER::error, GOLDEN_200);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> golden200() {
        if (golden200 == null) {
            golden200 = DonorJars.read(DonorJars.path(FOLDER, GOLDEN_200), "assets/", "data/");
        }
        return golden200;
    }
}
