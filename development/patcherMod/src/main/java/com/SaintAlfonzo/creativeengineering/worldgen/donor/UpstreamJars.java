package com.SaintAlfonzo.creativeengineering.worldgen.donor;

/**
 * The unmodified donor jars, pinned by sha1. tools/build_deploy.py reads the {@code new Jar("file", "sha1"} lines, so
 * keep that shape.
 */
final class UpstreamJars {
    record Jar(String file, String sha1, String source) {
    }

    static final Jar FARMERS_STRUCTURES = new Jar("FarmersStructures-1.0.6-1.21.1_neoforge.jar", "94262398153d7ca5dc9ffb93e49acdbad85af223",
            "Farmer's Structures 1.0.6 for NeoForge 1.21.1 (CurseForge / Modrinth)");

    private UpstreamJars() {
    }
}
