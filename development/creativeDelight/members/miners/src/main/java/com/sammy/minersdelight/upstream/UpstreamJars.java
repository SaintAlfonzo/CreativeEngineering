package com.sammy.minersdelight.upstream;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.shared.DonorJars;
import com.sammy.minersdelight.shared.DonorJars.Jar;

import java.util.Map;

/**
 * The unmodified upstream jars the port reads its art and data from (feature_writeup/miners_delight.md D2, D12). They sit
 * in {@code <game dir>/miners_upstream/}, never in {@code mods/}: they have this mod's id. Pinned by sha1, so the
 * translation only ever sees known inputs. 1.4.5 gives everything; 1.1.2 gives only the four textures the pack keeps in
 * their old look (TODO creativedelight/members #31, miners_delight.md 5).
 */
public final class UpstreamJars {
    public static final String FOLDER = "miners_upstream";

    public static final Jar MINERS_145 = new Jar("miners_delight-1.20.1-1.4.5-backport.jar", "638438679800711939e7a8029ae3d5340f8c17bd",
            "https://cdn.modrinth.com/data/qMxbM4BQ/versions/p0INUam7/miners_delight-1.20.1-1.4.5-backport.jar (Modrinth, Miner's Delight 1.20.1-1.4.5-backport for Forge 1.20.1)");

    public static final Jar MINERS_112 = new Jar("miners_delight-1.19.2-1.1.2.jar", "23701e0772676ff74c0290387c90fd9365987673",
            "https://mediafilez.forgecdn.net/files/4738/272/miners_delight-1.19.2-1.1.2.jar (CurseForge, Miner's Delight 1.19.2-1.1.2 for Forge 1.19.2, project 689630 file 4738272)");

    private static Map<String, byte[]> miners145;

    private UpstreamJars() {
    }

    /** Stops the game with a clear message when the jar is missing or not the expected file. */
    public static void verifyOrFail() {
        DonorJars.verifyOrFail(FOLDER, "Miner's Delight (Creative Delight)", "the original Miner's Delight jar", MinersDelightMod.LOGGER::error, MINERS_145, MINERS_112);
    }

    /** Only resource entries ({@code assets/}, {@code data/}). */
    public static synchronized Map<String, byte[]> miners145() {
        if (miners145 == null) {
            miners145 = DonorJars.read(DonorJars.path(FOLDER, MINERS_145), "assets/", "data/");
        }
        return miners145;
    }

    /** The given entries of the 1.1.2 jar (exact paths), read on each call; the caller keeps the result. */
    public static Map<String, byte[]> miners112(String... paths) {
        return DonorJars.read(DonorJars.path(FOLDER, MINERS_112), paths);
    }
}
