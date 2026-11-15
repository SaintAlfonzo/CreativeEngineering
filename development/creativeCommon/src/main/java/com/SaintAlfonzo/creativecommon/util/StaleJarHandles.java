package com.SaintAlfonzo.creativecommon.util;

import com.mojang.logging.LogUtils;
import cpw.mods.niofs.union.UnionFileSystem;
import cpw.mods.niofs.union.UnionFileSystemProvider;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Closes the file handles Forge leaks for every mod jar, so a client with about 420 or more jars can open its window
 * (feature_writeup/jar_handle_leak.md).
 *
 * <p>FML opens each jar in {@code mods/} twice: once in {@code ModDirTransformerDiscoverer} (to look for services; the
 * jar object is dropped right after) and once in the mod scan (the copy the game uses). Each open is a securejarhandler
 * {@code UnionFileSystem} over its own zip filesystem, and {@code UnionFileSystemProvider} keeps every one in a private
 * map for good; {@code UnionFileSystem.close()} only removes it from that map and never closes the zip. So each mod jar
 * holds two descriptors, and 420 jars plus the libraries pass 1024 before the window opens. GLFW then gets descriptors
 * of 1024 or higher for its X11 connection and wakeup pipe, and LWJGL 3.3.1's GLFW waits for the window with
 * {@code select()}, whose {@code fd_set} only holds 0 to 1023: "stack smashing detected" or a SIGSEGV in libglfw.
 *
 * <p>{@link #release()} runs before glfwInit ({@code FdReleaseMixin}). It closes the discovery-pass filesystem of each
 * jar whose game copy is in the mod list (same jar path, different filesystem object): first its zip filesystem
 * (that frees the descriptor), then the union filesystem (that drops it from the provider's map). Something still
 * references those objects, so a GC alone frees nothing (tested 2026-11-14). About 420 low descriptor numbers come
 * free and GLFW reuses them. Jars with no game copy in the mod list (service jars, plain libraries) are left alone.
 * The private fields are read with {@code sun.misc.Unsafe}: securejarhandler does not open its package to reflection
 * (same idiom as {@link ModDisplayNames}).
 */
public final class StaleJarHandles {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean done;

    private StaleJarHandles() {}

    public static synchronized void release() {
        if (done) {
            return;
        }
        done = true;
        try {
            int before = openCount();
            Map<FileSystem, Boolean> live = new IdentityHashMap<>();
            Set<Path> livePaths = new HashSet<>();
            UnionFileSystemProvider provider = null;
            for (ModFileInfo info : LoadingModList.get().getModFiles()) {
                FileSystem fs = info.getFile().getSecureJar().getRootPath().getFileSystem();
                if (fs instanceof UnionFileSystem ufs) {
                    live.put(ufs, Boolean.TRUE);
                    livePaths.add(ufs.getPrimaryPath().toAbsolutePath().normalize());
                    provider = (UnionFileSystemProvider) ufs.provider();
                }
            }
            if (provider == null) {
                return;
            }
            Path modsDir = FMLPaths.MODSDIR.get().toAbsolutePath().normalize();
            List<UnionFileSystem> stale = new ArrayList<>();
            for (UnionFileSystem ufs : snapshot(provider)) {
                if (live.containsKey(ufs)) {
                    continue;
                }
                Path p = ufs.getPrimaryPath().toAbsolutePath().normalize();
                if (modsDir.equals(p.getParent()) && livePaths.contains(p) && Files.isRegularFile(p)) {
                    stale.add(ufs);
                }
            }
            int zips = 0;
            for (UnionFileSystem ufs : stale) {
                zips += closeEmbedded(ufs);
                ufs.close();
            }
            if (stale.isEmpty()) {
                LOGGER.info("[jar handles] no stale mod jar filesystems ({} descriptors open)", before);
                return;
            }
            LOGGER.info("[jar handles] closed {} stale mod jar filesystems ({} zip files): {} -> {} descriptors open",
                    stale.size(), zips, before, openCount());
        } catch (Throwable t) {
            LOGGER.warn("[jar handles] could not close the stale mod jar filesystems", t);
        }
    }

    /**
     * Same field layout as {@code UnionFileSystem.EmbeddedFileSystemMetadata(Path path, FileSystem fs)}. That class is
     * a private record, and Unsafe refuses record fields, so the offsets come from this twin (two reference fields in
     * the same order, no superclass fields: HotSpot lays both out the same). Every read is checked: the twin's
     * {@code path} must be the very object the map is keyed by before {@code fs} is trusted.
     */
    @SuppressWarnings("unused")
    private static final class EmbeddedTwin {
        Path path;
        FileSystem fs;
    }

    /** Closes the zip filesystems under one union filesystem; returns how many. */
    @SuppressWarnings("unchecked")
    private static int closeEmbedded(UnionFileSystem ufs) throws Exception {
        sun.misc.Unsafe unsafe = unsafe();
        Field mapField = UnionFileSystem.class.getDeclaredField("embeddedFileSystems");
        Map<Path, Object> embedded = (Map<Path, Object>) unsafe.getObject(ufs, unsafe.objectFieldOffset(mapField));
        if (embedded == null) {
            return 0;
        }
        Field[] recordFields = embedded.isEmpty() ? new Field[0]
                : embedded.values().iterator().next().getClass().getDeclaredFields();
        if (recordFields.length != 2 || recordFields[0].getType() != Path.class || recordFields[1].getType() != FileSystem.class) {
            LOGGER.warn("[jar handles] unexpected securejarhandler layout, skipping");
            return 0;
        }
        long pathOff = unsafe.objectFieldOffset(EmbeddedTwin.class.getDeclaredField("path"));
        long fsOff = unsafe.objectFieldOffset(EmbeddedTwin.class.getDeclaredField("fs"));
        int n = 0;
        for (Map.Entry<Path, Object> e : embedded.entrySet()) {
            if (unsafe.getObject(e.getValue(), pathOff) != e.getKey()) {
                LOGGER.warn("[jar handles] field layout check failed, skipping");
                return n;
            }
            if (unsafe.getObject(e.getValue(), fsOff) instanceof FileSystem zip && zip != FileSystems.getDefault()
                    && zip.isOpen()) {
                zip.close();
                n++;
            }
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    private static List<UnionFileSystem> snapshot(UnionFileSystemProvider provider) throws ReflectiveOperationException {
        Field field = UnionFileSystemProvider.class.getDeclaredField("fileSystems");
        sun.misc.Unsafe unsafe = unsafe();
        Map<String, UnionFileSystem> map = (Map<String, UnionFileSystem>) unsafe.getObject(provider, unsafe.objectFieldOffset(field));
        if (map == null) {
            return Collections.emptyList();
        }
        synchronized (map) {
            return new ArrayList<>(map.values());
        }
    }

    /** Open descriptor count from /proc, or -1 where there is no /proc. */
    private static int openCount() {
        try (var s = Files.list(Path.of("/proc/self/fd"))) {
            return (int) s.count();
        } catch (Exception e) {
            return -1;
        }
    }

    private static sun.misc.Unsafe unsafe() throws ReflectiveOperationException {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
}
