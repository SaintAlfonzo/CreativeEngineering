package com.SaintAlfonzo.creativecompat.banners;

import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.SaintAlfonzo.creativecompat.shared.BuiltInPacks;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Serves {@link BannerPatternArt}'s textures as the hidden resource pack {@code creativecompat_banner_patterns} at the
 * bottom of the stack (resource packs still override any path), on the client with Bedspreads or Customizable Elytra
 * installed. {@code compat/BannerPatternAtlasCompat} and the two banner pattern mixins find them through the resource
 * manager at the same paths as when the jar carried them.
 *
 * <p>Sources: every installed mod jar's {@code assets/<ns>/textures/entity/banner/<id>.png}, jars in file name order with
 * Minecraft last; the first jar wins for the same pattern. Built once, on the first resource load.
 */
public final class BannerPatternPack {

    public static final String PACK_ID = CreativeCompat.MOD_ID + "_banner_patterns";
    private static final Pattern ID = Pattern.compile("[a-z0-9_.\\-]+");

    private static Map<String, byte[]> files;

    private BannerPatternPack() {}

    public static void add(AddPackFindersEvent event) {
        BuiltInPacks.addMemory(event, PackType.CLIENT_RESOURCES, PACK_ID,
                "Creative Compat: banner patterns on beds and elytra, built from the installed mods", Pack.Position.BOTTOM,
                BannerPatternPack::files);
    }

    private static Map<String, byte[]> files() {
        synchronized (BannerPatternPack.class) {
            if (files == null) files = build();
            return files;
        }
    }

    private static Map<String, byte[]> build() {
        long start = System.nanoTime();
        IModFile bedspreads = modFile("bedspreads"), elytra = modFile("customizableelytra");
        BiPredicate<String, String> bedCovered = bedspreads == null ? null
                : (ns, id) -> Files.isRegularFile(bedspreads.findResource("assets", "bedspreads", "textures", "entity", ns, id + ".png"));
        Predicate<String> elytraCovered = elytra == null ? null
                : id -> Files.isRegularFile(elytra.findResource("assets", "customizableelytra", "textures", "entity", "elytra_banner", id + ".png"));

        List<IModFileInfo> infos = new ArrayList<>(ModList.get().getModFiles());
        infos.sort(Comparator.comparing((IModFileInfo i) -> i.getMods().stream().anyMatch(m -> m.getModId().equals("minecraft")))
                .thenComparing(i -> i.getFile().getFileName()));
        List<BannerPatternArt.Source> sources = new ArrayList<>();
        for (IModFileInfo info : infos) {
            IModFile file = info.getFile();
            Map<String, Supplier<byte[]>> patterns = patterns(file);
            if (!patterns.isEmpty()) sources.add(new BannerPatternArt.Source(file.getFileName(), patterns));
        }

        BannerPatternArt.Result r = BannerPatternArt.build(sources, bedCovered, elytraCovered);
        CreativeCompat.LOGGER.info("[banner_patterns] {} Bedspreads and {} Customizable Elytra pattern textures built from {}"
                        + " jars in {} ms", r.bedspreads(), r.elytra(), sources.size(), (System.nanoTime() - start) / 1_000_000);
        for (String p : r.problems()) CreativeCompat.LOGGER.warn("[banner_patterns] {}", p);
        return Map.copyOf(r.files());
    }

    private static IModFile modFile(String modId) {
        return ModList.get().getModFileById(modId) == null ? null : ModList.get().getModFileById(modId).getFile();
    }

    /** {@code "<ns>/<id>"} to the bytes of each {@code assets/<ns>/textures/entity/banner/<id>.png} in the jar. */
    private static Map<String, Supplier<byte[]>> patterns(IModFile file) {
        Map<String, Supplier<byte[]>> out = new TreeMap<>();
        Path assets = file.findResource("assets");
        if (!Files.isDirectory(assets)) return out;
        try (Stream<Path> namespaces = Files.list(assets)) {
            for (Path nsDir : (Iterable<Path>) namespaces::iterator) {
                String ns = name(nsDir);
                Path banner = nsDir.resolve("textures").resolve("entity").resolve("banner");
                if (!ID.matcher(ns).matches() || !Files.isDirectory(banner)) continue;
                try (Stream<Path> pngs = Files.list(banner)) {
                    for (Path png : (Iterable<Path>) pngs::iterator) {
                        String n = name(png);
                        if (!n.endsWith(".png") || !Files.isRegularFile(png)) continue;
                        String id = n.substring(0, n.length() - 4);
                        if (!ID.matcher(id).matches()) continue;
                        out.put(ns + "/" + id, () -> {
                            try {
                                return Files.readAllBytes(png);
                            } catch (IOException e) {
                                return null;
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            CreativeCompat.LOGGER.warn("[banner_patterns] {}: {}", file.getFileName(), e.toString());
        }
        return out;
    }

    /** A path's last name without the trailing slash some jar file systems keep on folders. */
    private static String name(Path p) {
        String n = p.getFileName().toString();
        return n.endsWith("/") ? n.substring(0, n.length() - 1) : n;
    }
}
