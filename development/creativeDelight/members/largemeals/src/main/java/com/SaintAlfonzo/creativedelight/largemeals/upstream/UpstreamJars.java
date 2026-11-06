package com.SaintAlfonzo.creativedelight.largemeals.upstream;

import com.SaintAlfonzo.creativedelight.largemeals.LargeMealsMod;
import com.SaintAlfonzo.creativedelight.largemeals.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.largemeals.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jar the port reads its art and loot from (feature_writeup/large_meals.md 4). It sits in
 * {@code <game dir>/largemeals_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1.
 */
public final class UpstreamJars {
    public static final String FOLDER = "largemeals_upstream";

    public static final Jar LARGEMEALS_130 = new Jar("largemeals-1.20.1-1.3.0.jar", "7662138788a75832a0c6bad6b13b006cac383496",
            "CurseForge, Large Meals 1.3.0 for Forge 1.20.1 (largemeals-1.20.1-1.3.0.jar)");

    private static Map<String, byte[]> largemeals130;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Large Meals (Creative Delight)", "the original Large Meals jar", LargeMealsMod.LOGGER::error, LARGEMEALS_130);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> largemeals130() {
        if (largemeals130 == null) {
            largemeals130 = DonorJars.read(DonorJars.path(FOLDER, LARGEMEALS_130), "assets/", "data/");
        }
        return largemeals130;
    }
}
