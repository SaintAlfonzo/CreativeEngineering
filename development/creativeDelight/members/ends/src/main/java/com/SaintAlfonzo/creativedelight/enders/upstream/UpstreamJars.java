package com.SaintAlfonzo.creativedelight.enders.upstream;

import com.SaintAlfonzo.creativedelight.enders.EndersAdditionsMod;
import com.SaintAlfonzo.creativedelight.enders.shared.DonorJars;
import com.SaintAlfonzo.creativedelight.enders.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified Ender's Delight 1.1.4 jar (Forge 1.20.1, MIT) this member reads its art and data from
 * (feature_writeup/ends_delight.md 5.3). It sits in {@code <game dir>/enders_upstream/}, never in {@code mods/}: it has
 * the mod id of the installed Ender's Delight 1.2.2. Pinned by sha1, so the translation only ever sees one known input.
 * Donor route chosen by the owner 2026-11-01 (TODO creativedelight/members #9) in case of licence issues.
 */
public final class UpstreamJars {
    public static final String FOLDER = "enders_upstream";

    public static final Jar ENDERS_114 = new Jar("endersdelight-1.1.4.jar", "d1541d70c7dbae71c2a7e1693f512f2166908802",
            "https://cdn.modrinth.com/data/2uEhdGKt/versions/Jaf9wxnO/endersdelight-1.1.4.jar (Modrinth, Ender's Delight 1.1.4 for Forge 1.20.1)");

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Ender's Delight Additions (Creative Delight)", "the original Ender's Delight 1.1.4 jar",
                EndersAdditionsMod.LOGGER::error, ENDERS_114);
    }

    /** The given entries of the 1.1.4 jar (exact paths), read on each call; the caller keeps the result. */
    public static Map<String, byte[]> enders114(String... paths) {
        return DonorJars.read(DonorJars.path(FOLDER, ENDERS_114), paths);
    }
}
