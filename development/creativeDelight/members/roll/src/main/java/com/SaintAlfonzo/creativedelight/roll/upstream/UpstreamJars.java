package com.SaintAlfonzo.creativedelight.roll.upstream;

import com.SaintAlfonzo.creativedelight.roll.RollDelightMod;
import com.SaintAlfonzo.creativedelight.roll.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.roll.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jar the port reads its art from (feature_writeup/roll_delight.md 4). It sits in
 * {@code <game dir>/roll_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1.
 */
public final class UpstreamJars {
    public static final String FOLDER = "roll_upstream";

    public static final Jar ROLL_131 = new Jar("roll_delight-1.3.1-forge-1.20.1.jar", "e23d48373f2a62c51533d15cd99eb24397f8d8d8",
            "CurseForge, Roll Delight 1.3.1 for Forge 1.20.1 (file \"[1.20.1] Roll Delight-1.3.1-forge-.jar\", renamed)");

    private static Map<String, byte[]> roll131;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Roll Delight (Creative Delight)", "the original Roll Delight jar", RollDelightMod.LOGGER::error, ROLL_131);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> roll131() {
        if (roll131 == null) {
            roll131 = DonorJars.read(DonorJars.path(FOLDER, ROLL_131), "assets/", "data/");
        }
        return roll131;
    }
}
