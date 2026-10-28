package ce.shared;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reading the unmodified upstream jars a port takes its art and data from. They sit in {@code <game dir>/<folder>/},
 * never in {@code mods/}, pinned by sha1 so the translation only ever sees one known input. Each port keeps its own
 * {@code UpstreamJars} with the {@code new Jar("file", "sha1", ...)} lines (tools/build_deploy.py reads them there) and
 * calls this. Shared source (development/shared/upstream, compiled into each jar under its own package;
 * feature_writeup/pack_tooling.md).
 */
public final class DonorJars {
    public record Jar(String file, String sha1, String source) {
    }

    private DonorJars() {
    }

    public static Path path(String folder, Jar jar) {
        return FMLPaths.GAMEDIR.get().resolve(folder).resolve(jar.file());
    }

    /**
     * Stops the game with a clear message when a jar is missing or not the expected file. The message is
     * {@code what + " reads its textures, models and recipes from " + from + ", which must be in the folder ..."}, logged
     * through {@code logError} before the throw.
     */
    public static void verifyOrFail(String folder, String what, String from, Consumer<String> logError, Jar... jars) {
        StringBuilder problems = new StringBuilder();
        for (Jar jar : jars) {
            Path path = path(folder, jar);
            if (!Files.isRegularFile(path)) {
                problems.append("\n  missing: ").append(path).append("\n    get it from ").append(jar.source());
                continue;
            }
            String sha1 = sha1(path);
            if (!jar.sha1().equals(sha1)) {
                problems.append("\n  wrong file: ").append(path).append(" (sha1 ").append(sha1).append(", expected ").append(jar.sha1())
                        .append(")\n    get the exact file from ").append(jar.source());
            }
        }
        if (!problems.isEmpty()) {
            String message = what + " reads its textures, models and recipes from " + from
                    + ", which must be in the folder '" + folder + "' of the game directory (not in mods/):" + problems;
            logError.accept(message);
            throw new IllegalStateException(message);
        }
    }

    /** The entries under the given prefixes (for example {@code assets/}, {@code data/}), directories left out. */
    public static Map<String, byte[]> read(Path jar, String... prefixes) {
        try (InputStream in = Files.newInputStream(jar)) {
            return read(in, "Could not read " + jar, prefixes);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + jar, e);
        }
    }

    /** {@link #read(Path, String...)} over a zip stream (a jar nested in another); {@code failure} is the error text. */
    public static Map<String, byte[]> read(InputStream raw, String failure, String... prefixes) {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(raw)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                String name = entry.getName();
                if (!entry.isDirectory() && startsWithAny(name, prefixes)) {
                    entries.put(name, zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(failure, e);
        }
        return entries;
    }

    /** One entry's bytes, or null when the jar has no such entry. */
    public static byte[] readEntry(Path jar, String entryName) {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(jar))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
                if (entry.getName().equals(entryName)) {
                    return zip.readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + jar, e);
        }
        return null;
    }

    public static String sha1(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path)));
        } catch (IOException | NoSuchAlgorithmException e) {
            return "unreadable (" + e.getMessage() + ")";
        }
    }

    private static boolean startsWithAny(String name, String[] prefixes) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) return true;
        }
        return false;
    }
}
