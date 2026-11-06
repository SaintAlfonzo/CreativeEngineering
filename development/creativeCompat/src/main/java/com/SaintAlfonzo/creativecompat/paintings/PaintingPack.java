package com.SaintAlfonzo.creativecompat.paintings;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.SaintAlfonzo.creativecompat.shared.BuiltInPacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Serves {@link PaintingArt}'s set as the hidden data pack {@code creativecompat_paintings} at the bottom of the stack
 * (datapacks and kubejs/data still override any path), only with Immersive Paintings installed. Data pack, so it runs where
 * the data loads: the dedicated server, or the integrated server in single player. Every source mod is installed on both
 * sides, so the server jar set has the art.
 *
 * <p>Sources, lowest priority first: every installed mod jar (by file name), then {@code [paintings] extraSources} in
 * order. Vanilla's own paintings are not built here: the dedicated server has no vanilla textures, and the pack ships
 * them as a datapack (kubejs/data/creativeengineering/paintings/minecraft/, tools/gen_immersive_paintings.py).
 * Built once, on the first data load; rebuilt when the extra source list changes.
 */
public final class PaintingPack {

    public static final String PACK_ID = CreativeCompat.MOD_ID + "_paintings";
    private static final Pattern DISPLAY_NAME = Pattern.compile("(?m)^\\s*displayName\\s*=\\s*\"([^\"]*)\"");

    private static Map<String, byte[]> files;
    private static List<String> builtFor;

    private PaintingPack() {}

    public static void add(AddPackFindersEvent event) {
        BuiltInPacks.addMemory(event, PackType.SERVER_DATA, PACK_ID,
                "Creative Compat: Immersive Paintings entries built from the installed mods", Pack.Position.BOTTOM,
                PaintingPack::files);
    }

    private static Map<String, byte[]> files() {
        synchronized (PaintingPack.class) {
            if (!CompatConfig.Paintings.enabled()) return Map.of();
            List<String> extra = CompatConfig.Paintings.extraSources();
            if (files == null || !extra.equals(builtFor)) {
                files = build(extra);
                builtFor = extra;
            }
            return files;
        }
    }

    private static Map<String, byte[]> build(List<String> extra) {
        long start = System.nanoTime();
        List<PaintingArt.Variant> variants = new ArrayList<>();
        for (Map.Entry<net.minecraft.resources.ResourceKey<PaintingVariant>, PaintingVariant> e
                : ForgeRegistries.PAINTING_VARIANTS.getEntries()) {
            ResourceLocation id = e.getKey().location();
            if (id.getNamespace().equals("minecraft")) continue;
            variants.add(new PaintingArt.Variant(id.getNamespace(), id.getPath(), e.getValue().getWidth(),
                    e.getValue().getHeight()));
        }
        List<PaintingArt.Source> sources = new ArrayList<>(modSources());
        for (String entry : extra) sources.addAll(extraSources(entry));

        PaintingArt.Result r = PaintingArt.build(variants, sources);
        CreativeCompat.LOGGER.info("[paintings] {} of {} modded painting variants built from {} sources ({} scaled up to {} px"
                        + " per block) in {} ms: {}", r.files().size() / 2, variants.size(), sources.size(), r.upscaled(),
                PaintingArt.TARGET_RES, (System.nanoTime() - start) / 1_000_000, r.byNamespace());
        for (String n : r.notes()) CreativeCompat.LOGGER.info("[paintings] {}", n);
        for (String p : r.problems()) CreativeCompat.LOGGER.warn("[paintings] {}", p);
        return Map.copyOf(r.files());
    }

    /** Every loaded mod jar, by file name, read in place. */
    private static List<PaintingArt.Source> modSources() {
        List<IModFileInfo> infos = new ArrayList<>(ModList.get().getModFiles());
        infos.sort(Comparator.comparing(i -> i.getFile().getFileName()));
        List<PaintingArt.Source> out = new ArrayList<>();
        for (IModFileInfo info : infos) {
            IModFile file = info.getFile();
            String display = info.getMods().isEmpty() ? null : info.getMods().get(0).getDisplayName();
            out.add(new PaintingArt.Source(file.getFileName(), display,
                    path -> {
                        Path p = file.findResource(path.split("/"));
                        try {
                            return Files.isRegularFile(p) ? Files.readAllBytes(p) : null;
                        } catch (IOException e) {
                            return null;
                        }
                    },
                    ns -> Files.isDirectory(file.findResource("assets", ns, "textures", "painting"))));
        }
        return out;
    }

    /**
     * One {@code extraSources} entry, relative to the game folder (or absolute): a jar or zip; a folder with
     * {@code assets/} in it (an unzipped resource pack); or a folder of jars and zips, each one a source by file name.
     */
    private static List<PaintingArt.Source> extraSources(String entry) {
        Path path = FMLPaths.GAMEDIR.get().resolve(entry.trim()).normalize();
        List<PaintingArt.Source> out = new ArrayList<>();
        if (Files.isRegularFile(path)) {
            PaintingArt.Source s = zipSource(path);
            if (s != null) out.add(s);
        } else if (Files.isDirectory(path.resolve("assets"))) {
            out.add(mapSource(path.getFileName().toString(), null, folderFiles(path)));
        } else if (Files.isDirectory(path)) {
            try (Stream<Path> list = Files.list(path)) {
                list.filter(p -> {
                    String n = p.getFileName().toString().toLowerCase();
                    return Files.isRegularFile(p) && (n.endsWith(".jar") || n.endsWith(".zip"));
                }).sorted().map(PaintingPack::zipSource).filter(Objects::nonNull).forEach(out::add);
            } catch (IOException e) {
                CreativeCompat.LOGGER.warn("[paintings] extraSources {}: {}", entry, e.toString());
            }
        } else {
            CreativeCompat.LOGGER.warn("[paintings] extraSources {}: no such file or folder ({})", entry, path);
        }
        return out;
    }

    private static boolean wanted(String name) {
        return name.startsWith("assets/") && (name.contains("/textures/painting/") && name.endsWith(".png")
                || name.endsWith("/lang/en_us.json"));
    }

    /** A jar or zip, its painting art and en_us lang copied into memory; a mod jar keeps its display name. */
    private static PaintingArt.Source zipSource(Path path) {
        Map<String, byte[]> files = new HashMap<>();
        String display = null;
        try (ZipFile zip = new ZipFile(path.toFile())) {
            for (ZipEntry e : java.util.Collections.list(zip.entries())) {
                if (e.isDirectory()) continue;
                String name = e.getName();
                if (wanted(name)) {
                    try (InputStream in = zip.getInputStream(e)) {
                        files.put(name, in.readAllBytes());
                    }
                } else if (name.equals("META-INF/mods.toml")) {
                    try (InputStream in = zip.getInputStream(e)) {
                        Matcher m = DISPLAY_NAME.matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                        if (m.find()) display = m.group(1);
                    }
                }
            }
        } catch (IOException e) {
            CreativeCompat.LOGGER.warn("[paintings] extraSources {}: unreadable ({})", path, e.toString());
            return null;
        }
        return mapSource(path.getFileName().toString(), display, files);
    }

    private static Map<String, byte[]> folderFiles(Path root) {
        Map<String, byte[]> files = new HashMap<>();
        try (Stream<Path> walk = Files.walk(root.resolve("assets"))) {
            for (Path p : (Iterable<Path>) walk.filter(Files::isRegularFile)::iterator) {
                String name = root.relativize(p).toString().replace('\\', '/');
                if (wanted(name)) files.put(name, Files.readAllBytes(p));
            }
        } catch (IOException e) {
            CreativeCompat.LOGGER.warn("[paintings] extraSources {}: {}", root, e.toString());
        }
        return files;
    }

    private static PaintingArt.Source mapSource(String label, String display, Map<String, byte[]> files) {
        return new PaintingArt.Source(label, display, files::get, ns -> {
            String prefix = "assets/" + ns + "/textures/painting/";
            return files.keySet().stream().anyMatch(k -> k.startsWith(prefix));
        });
    }
}
