package com.sammy.minersdelight.upstream;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.shared.DonorJars;
import com.sammy.minersdelight.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jar the port reads its art and data from (feature_writeup/miners_delight.md D2, D12). It sits
 * in {@code <game dir>/miners_upstream/}, never in {@code mods/}: it has this mod's id. Pinned by sha1, so the
 * translation only ever sees one known input.
 */
public final class UpstreamJars {
    public static final String FOLDER = "miners_upstream";

    public static final Jar MINERS_145 = new Jar("miners_delight-1.20.1-1.4.5-backport.jar", "638438679800711939e7a8029ae3d5340f8c17bd",
            "https://cdn.modrinth.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1.20.1-1.4.5-backport.jar (Modrinth, Miner's Delight 1.20.1-1.4.5-backport for Forge 1.20.1)");

    private static Map<String, byte[]> miners145;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Miner's Delight (Backport)", "the original Miner's Delight jar", MinersDelightMod.LOGGER::error, MINERS_145);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> miners145() {
        if (miners145 == null) {
            miners145 = DonorJars.read(DonorJars.path(FOLDER, MINERS_145), "assets/", "data/");
        }
        return miners145;
    }
}
