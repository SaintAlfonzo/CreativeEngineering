package com.SaintAlfonzo.creativeengineering.worldgen.donor;

import com.SaintAlfonzo.creativeengineering.shared.BuiltInPacks;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Farmer's Structures 1.0.6 as a resource donor (feature_writeup/farmers_structures.md 5). The jar is All Rights
 * Reserved and a NeoForge 1.21.1 build, so nothing of it is copied into the pack: it sits unmodified in
 * {@code <game dir>/farmers_upstream/}, never in {@code mods/}, pinned by sha1, and {@link FarmersTranslator} rewrites
 * its worldgen data in memory into what 1.19.2 reads. The installed 1.0.0 jar stays the mod; this built-in data pack
 * sits above it and overrides the files both ship (the pools, structures, sets and templates) and adds the new ones.
 *
 * A missing or different jar adds nothing (one warning), so the pack still runs on the 1.0.0 data alone.
 */
public final class FarmersDonor {
    private static final Logger LOG = LogManager.getLogger("creativeengineering/farmers-donor");

    public static final String FOLDER = "farmers_upstream";
    public static final String FILE = UpstreamJars.FARMERS_STRUCTURES.file();
    public static final String SHA1 = UpstreamJars.FARMERS_STRUCTURES.sha1();
    public static final String SOURCE = UpstreamJars.FARMERS_STRUCTURES.source();
    /** Sorts before KubeJS's own pack ids, so KubeJS data still wins over this one. */
    static final String PACK_ID = "farmers_structures_donor";

    private static Map<String, byte[]> translated;

    private FarmersDonor() {
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        BuiltInPacks.addMemory(event, PackType.SERVER_DATA, PACK_ID, "Farmer's Structures 1.0.6 data, translated in memory from the donor jar",
                Pack.Position.TOP, FarmersDonor::files);
    }

    /** Built once per game start, on the first read (the registries are complete by then). */
    private static synchronized Map<String, byte[]> files() {
        if (translated == null) {
            translated = build();
        }
        return translated;
    }

    private static Map<String, byte[]> build() {
        Path jar = FMLPaths.GAMEDIR.get().resolve(FOLDER).resolve(FILE);
        if (!Files.isRegularFile(jar)) {
            LOG.warn("{} is missing: Farmer's Structures runs on its installed 1.0.0 data. Get {} and put it in '{}'.", jar, SOURCE, FOLDER);
            return Map.of();
        }
        try {
            String sha1 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(jar)));
            if (!SHA1.equals(sha1)) {
                LOG.warn("{} has sha1 {}, expected {}: not read. Get the exact file from {}.", jar, sha1, SHA1, SOURCE);
                return Map.of();
            }
            Map<String, byte[]> source = new HashMap<>();
            try (InputStream in = Files.newInputStream(jar); ZipInputStream zip = new ZipInputStream(in)) {
                for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                    if (!entry.isDirectory() && entry.getName().startsWith("data/")) {
                        source.put(entry.getName(), zip.readAllBytes());
                    }
                }
            }
            return FarmersTranslator.translate(source);
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            LOG.error("Could not translate {}: Farmer's Structures runs on its installed 1.0.0 data", jar, e);
            return Map.of();
        }
    }
}
