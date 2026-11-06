package com.SaintAlfonzo.creativecommon.changelog;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The changelog's version files and the player's read marker (feature_writeup/changelog.md 3.1, 3.3).
 *
 * <p>Version files are {@code assets/creativecommon/changelog/<version>.md} in any resource pack (the pack
 * ships them in {@code kubejs/assets/}); the file name is the version. {@code unreleased.md} is the dev working
 * list: it is listed as "In development" when it has bullets and never counts as a version. The list of names is
 * cached until the next resource reload (F3+T); the text is read fresh each time the screen opens.
 *
 * <p>The read marker {@code config/creativecommon/changelog_seen.txt} holds the newest version the player has
 * opened. A newest version above it (or no marker at all) means unread: the update popup shows.
 */
public final class ChangelogStore {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String FOLDER = "changelog";
    public static final String UNRELEASED = "unreleased";
    private static final Pattern VERSION = Pattern.compile("\\d+(\\.\\d+)*");
    private static final Pattern BULLET = Pattern.compile("(?m)^\\s*[-*+]\\s+\\S");

    /** Version names newest first, plus whether unreleased.md has bullets; null until first asked after a reload. */
    @Nullable private static List<String> versions;
    private static boolean devList;
    @Nullable private static String seen;
    private static boolean seenLoaded;

    private ChangelogStore() {}

    /**
     * Dotted versions compared segment by segment as integers: 1.1.01 > 1.0.10 > 1.0.9, and 1.1.01 equals
     * 1.1.1 (the lint rejects that pair). A missing segment counts as 0.
     */
    public static int compare(String a, String b) {
        String[] x = a.split("\\."), y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            long p = i < x.length ? Long.parseLong(x[i]) : 0, q = i < y.length ? Long.parseLong(y[i]) : 0;
            if (p != q) return Long.compare(p, q);
        }
        return 0;
    }

    public static boolean isVersion(String name) {
        return VERSION.matcher(name).matches();
    }

    public static boolean hasBullets(String markdown) {
        return BULLET.matcher(markdown).find();
    }

    /** Released versions, newest first. */
    public static List<String> versions(ResourceManager manager) {
        if (versions == null) scan(manager);
        return versions;
    }

    /** True when unreleased.md exists and has at least one bullet (dev instance only: it is never shipped). */
    public static boolean hasDevList(ResourceManager manager) {
        if (versions == null) scan(manager);
        return devList;
    }

    @Nullable
    public static String newest(ResourceManager manager) {
        List<String> list = versions(manager);
        return list.isEmpty() ? null : list.get(0);
    }

    /** The newest version is above the read marker (or there is no marker yet). */
    public static boolean unread(ResourceManager manager) {
        String newest = newest(manager);
        return newest != null && isUnread(newest);
    }

    public static boolean isUnread(String version) {
        String marker = seen();
        return marker == null || compare(version, marker) > 0;
    }

    /** Text of a version file (or "unreleased"), or null if it can't be read. */
    @Nullable
    public static String read(ResourceManager manager, String version) {
        ResourceLocation id = new ResourceLocation(CreativeCommon.NS, FOLDER + "/" + version + ".md");
        Resource resource = manager.getResource(id).orElse(null);
        if (resource == null) return null;
        try (BufferedReader reader = resource.openAsReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOGGER.warn("Changelog: could not read {}", id, e);
            return null;
        }
    }

    private static void scan(ResourceManager manager) {
        Map<ResourceLocation, Resource> found = manager.listResources(FOLDER, id -> id.getPath().endsWith(".md"));
        List<String> sorted = new ArrayList<>();
        boolean dev = false;
        for (ResourceLocation id : found.keySet()) {
            if (!id.getNamespace().equals(CreativeCommon.NS)) continue;
            String name = id.getPath().substring(FOLDER.length() + 1, id.getPath().length() - 3);
            if (name.equals(UNRELEASED)) {
                String text = read(manager, UNRELEASED);
                dev = text != null && hasBullets(text);
            } else if (isVersion(name)) {
                sorted.stream().filter(n -> compare(n, name) == 0).findFirst().ifPresent(clash ->
                        LOGGER.warn("Changelog: {}.md and {}.md are the same version; showing both", clash, name));
                sorted.add(name);
            } else {
                LOGGER.warn("Changelog: ignoring {} (the file name must be a version like 1.1.01)", id);
            }
        }
        sorted.sort((a, b) -> compare(b, a));
        versions = sorted;
        devList = dev;
    }

    // ---- read marker ----------------------------------------------------------------------------

    private static Path markerFile() {
        return FMLPaths.CONFIGDIR.get().resolve("creativecommon").resolve("changelog_seen.txt");
    }

    @Nullable
    public static String seen() {
        if (!seenLoaded) {
            seenLoaded = true;
            Path file = markerFile();
            try {
                String text = Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8).trim() : "";
                seen = isVersion(text) ? text : null;
            } catch (IOException e) {
                LOGGER.warn("Changelog: could not read {}", file, e);
                seen = null;
            }
        }
        return seen;
    }

    /** Record that the player opened the changelog with {@code version} as the newest; never moves backwards. */
    public static void markSeen(String version) {
        String old = seen();
        if (old != null && compare(version, old) <= 0) return;
        seen = version;
        Path file = markerFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, version + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Changelog: could not write {}", file, e);
        }
    }

    @Mod.EventBusSubscriber(modid = CreativeCommon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Reload {
        private Reload() {}

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> versions = null);
        }
    }
}
