package com.SaintAlfonzo.creativedevtools.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 * Runs the pack's Python generator scripts (tools/*.py) as child processes, one after another, in
 * the order tools/generators.json lists them. Backs /cegen and the optional startup run
 * ({@link GeneratorCommand}); has no Minecraft dependencies so it can be tested headless.
 *
 * <p>Each script runs with the instance root as working directory. Its merged stdout/stderr goes to
 * latest.log line by line ("[cegen:id]") and, whole, to logs/cegen/&lt;id&gt;.log. The manifest's
 * "outputs" are hashed before and after, so a result can say which files actually changed.
 */
public final class GeneratorRunner {

    public static final String MANIFEST = "tools/generators.json";
    public static final String LOG_DIR = "logs/cegen";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TAIL_LINES = 8;

    public record Generator(String id, String script, List<String> args, List<String> groups, List<String> dumps,
                            List<String> outputs, String apply, Set<Integer> warnExit, List<Pattern> highlight) {}

    /** OK = exit 0; WARN = an exit code the manifest lists under warnExit (a check with findings). */
    public enum Status { OK, WARN, FAILED, TIMEOUT, CANCELLED }

    /**
     * @param changed   manifest outputs whose content differs after the run (folders end in '/')
     * @param highlight output lines matching a highlight regex
     * @param tail      last output lines, for a failed run
     * @param error     why the process couldn't run at all, else null
     */
    public record Result(Generator gen, Status status, int exitCode, double seconds, List<String> changed,
                         List<String> highlight, List<String> tail, String error) {}

    public interface Listener {
        default void started(Generator gen, int index, int total) {}
        default void finished(Result result) {}
    }

    private final Path root;
    private final String python;
    private final int timeoutSeconds;
    private volatile boolean cancelled;
    private volatile Process process;
    private volatile Generator current;

    public GeneratorRunner(Path root, String python, int timeoutSeconds) {
        this.root = root.toAbsolutePath().normalize();
        this.python = python;
        this.timeoutSeconds = timeoutSeconds;
    }

    // -------------------------------------------------------------------------
    // Manifest
    // -------------------------------------------------------------------------

    /** Every generator in the manifest, in run order. Throws IllegalStateException on a malformed manifest. */
    public static List<Generator> load(Path root) throws IOException {
        Path file = root.resolve(MANIFEST);
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException(MANIFEST + " not found in " + root + " - generators only run from the dev instance");
        }
        JsonObject json;
        try {
            json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IllegalStateException(MANIFEST + ": " + e.getMessage(), e);
        }
        List<Pattern> global = patterns(strings(json, "highlight"), "highlight");
        List<Generator> out = new ArrayList<>();
        Set<String> ids = new TreeSet<>();
        for (JsonElement el : array(json, "generators")) {
            JsonObject g = el.getAsJsonObject();
            String id = string(g, "id");
            String script = string(g, "script");
            if (id == null || script == null) throw new IllegalStateException(MANIFEST + ": every generator needs an id and a script");
            if (!ids.add(id)) throw new IllegalStateException(MANIFEST + ": duplicate id " + id);
            List<Pattern> highlight = new ArrayList<>(global);
            highlight.addAll(patterns(strings(g, "highlight"), id + ".highlight"));
            Set<Integer> warnExit = new TreeSet<>();
            for (JsonElement code : array(g, "warnExit")) warnExit.add(code.getAsInt());
            String apply = string(g, "apply");
            out.add(new Generator(id, script, strings(g, "args"), strings(g, "groups"), strings(g, "dumps"),
                    strings(g, "outputs"), apply == null ? "" : apply, warnExit, highlight));
        }
        for (Generator g : out) {
            if (g.groups().contains(g.id()) || g.id().equals("all")) {
                throw new IllegalStateException(MANIFEST + ": id " + g.id() + " clashes with a group name");
            }
        }
        return out;
    }

    /** Generators matching a target: "all", a group name, or one id. Manifest order; empty if nothing matches. */
    public static List<Generator> select(List<Generator> all, String target) {
        if (target.equals("all")) return all;
        return all.stream().filter(g -> g.id().equals(target) || g.groups().contains(target)).toList();
    }

    /** Every valid target: "all", the groups, then the ids. */
    public static List<String> targets(List<Generator> all) {
        Set<String> out = new LinkedHashSet<>();
        out.add("all");
        all.forEach(g -> out.addAll(g.groups()));
        all.forEach(g -> out.add(g.id()));
        return new ArrayList<>(out);
    }

    /** The in-game dump commands the selection reads, deduplicated, first-use order. */
    public static List<String> dumps(List<Generator> selection) {
        Set<String> out = new LinkedHashSet<>();
        selection.forEach(g -> out.addAll(g.dumps()));
        return new ArrayList<>(out);
    }

    // -------------------------------------------------------------------------
    // Running
    // -------------------------------------------------------------------------

    /** Runs the selection in order on the calling thread. Stops early only when cancelled. */
    public List<Result> run(List<Generator> selection, Listener listener) {
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < selection.size() && !cancelled; i++) {
            Generator gen = selection.get(i);
            current = gen;
            listener.started(gen, i, selection.size());
            Result r = runOne(gen);
            results.add(r);
            listener.finished(r);
        }
        current = null;
        return results;
    }

    /** Kills the running script; the rest of the selection is skipped. */
    public void cancel() {
        cancelled = true;
        Process p = process;
        if (p != null) p.destroyForcibly();
    }

    /** The generator running right now, or null between scripts. */
    public Generator current() {
        return current;
    }

    private Result runOne(Generator gen) {
        long start = System.nanoTime();
        Path script = root.resolve(gen.script()).normalize();
        if (!Files.isRegularFile(script)) {
            return failed(gen, start, "script not found: " + gen.script());
        }
        Map<String, String> before = snapshot(gen.outputs());

        List<String> cmd = new ArrayList<>();
        cmd.add(python);
        cmd.add(script.toString());
        cmd.addAll(gen.args());
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(root.toFile()).redirectErrorStream(true);
        pb.environment().put("PYTHONUNBUFFERED", "1");
        pb.environment().put("PYTHONIOENCODING", "utf-8");

        List<String> lines = Collections.synchronizedList(new ArrayList<>());
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            return failed(gen, start, "could not start '" + python + "': " + e.getMessage());
        }
        process = p;
        Thread reader = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    lines.add(line);
                    LOGGER.info("[cegen:{}] {}", gen.id(), line);
                }
            } catch (IOException ignored) {
                // stream closed by destroyForcibly on timeout/cancel
            }
        }, "cegen-output-" + gen.id());
        reader.setDaemon(true);
        reader.start();

        Status status;
        int exit = -1;
        try {
            if (!p.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                status = cancelled ? Status.CANCELLED : Status.TIMEOUT;
            } else {
                exit = p.exitValue();
                status = cancelled ? Status.CANCELLED
                        : exit == 0 ? Status.OK
                        : gen.warnExit().contains(exit) ? Status.WARN : Status.FAILED;
            }
            reader.join(5000);
        } catch (InterruptedException e) {
            p.destroyForcibly();
            Thread.currentThread().interrupt();
            status = Status.CANCELLED;
        } finally {
            process = null;
        }

        List<String> out;
        synchronized (lines) {
            out = new ArrayList<>(lines);
        }
        double seconds = (System.nanoTime() - start) / 1e9;
        List<String> changed = changed(gen.outputs(), before, snapshot(gen.outputs()));
        List<String> highlight = out.stream().filter(l -> gen.highlight().stream().anyMatch(pt -> pt.matcher(l).find())).toList();
        List<String> tail = out.subList(Math.max(0, out.size() - TAIL_LINES), out.size());
        Result r = new Result(gen, status, exit, seconds, changed, highlight, List.copyOf(tail), null);
        writeLog(gen, cmd, out, r);
        LOGGER.info("[cegen:{}] {} (exit {}) in {}s; changed: {}", gen.id(), status, exit,
                String.format("%.1f", seconds), changed.isEmpty() ? "nothing" : String.join(", ", changed));
        return r;
    }

    private Result failed(Generator gen, long start, String error) {
        LOGGER.error("[cegen:{}] {}", gen.id(), error);
        return new Result(gen, Status.FAILED, -1, (System.nanoTime() - start) / 1e9, List.of(), List.of(), List.of(), error);
    }

    private void writeLog(Generator gen, List<String> cmd, List<String> out, Result r) {
        try {
            Path dir = root.resolve(LOG_DIR);
            Files.createDirectories(dir);
            List<String> text = new ArrayList<>();
            text.add("# " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "  " + String.join(" ", cmd));
            text.add("# " + r.status() + ", exit " + r.exitCode() + ", " + String.format("%.1f", r.seconds()) + "s, changed: "
                    + (r.changed().isEmpty() ? "nothing" : String.join(", ", r.changed())));
            text.addAll(out);
            Files.write(dir.resolve(gen.id() + ".log"), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("[cegen:{}] could not write {}/{}.log: {}", gen.id(), LOG_DIR, gen.id(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Output change detection
    // -------------------------------------------------------------------------

    /** relative file path -> content hash, for every file an output names (folders walked). */
    private Map<String, String> snapshot(List<String> outputs) {
        Map<String, String> out = new TreeMap<>();
        for (String rel : outputs) {
            Path p = root.resolve(rel).normalize();
            if (Files.isRegularFile(p)) {
                out.put(rel, hash(p));
            } else if (Files.isDirectory(p)) {
                try (Stream<Path> walk = Files.walk(p)) {
                    walk.filter(Files::isRegularFile).forEach(f -> out.put(root.relativize(f).toString(), hash(f)));
                } catch (IOException e) {
                    out.put(rel, "unreadable");
                }
            }
        }
        return out;
    }

    /** Output entries (not individual files) with any added, removed or modified file under them. */
    private List<String> changed(List<String> outputs, Map<String, String> before, Map<String, String> after) {
        Set<String> files = new TreeSet<>(before.keySet());
        files.addAll(after.keySet());
        files.removeIf(f -> java.util.Objects.equals(before.get(f), after.get(f)));
        List<String> out = new ArrayList<>();
        for (String rel : outputs) {
            boolean dir = Files.isDirectory(root.resolve(rel));
            String prefix = rel.endsWith("/") ? rel : rel + "/";
            if (files.stream().anyMatch(f -> f.equals(rel) || f.startsWith(prefix))) {
                String name = Path.of(rel).getFileName().toString();
                out.add(dir ? name + "/" : name);
            }
        }
        return out;
    }

    private static String hash(Path file) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
        } catch (IOException e) {
            return "unreadable";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // -------------------------------------------------------------------------
    // JSON helpers
    // -------------------------------------------------------------------------

    private static JsonArray array(JsonObject o, String key) {
        JsonElement el = o.get(key);
        return el != null && el.isJsonArray() ? el.getAsJsonArray() : new JsonArray();
    }

    private static List<String> strings(JsonObject o, String key) {
        List<String> out = new ArrayList<>();
        for (JsonElement el : array(o, key)) out.add(el.getAsString());
        return List.copyOf(out);
    }

    private static String string(JsonObject o, String key) {
        JsonElement el = o.get(key);
        return el != null && el.isJsonPrimitive() ? el.getAsString() : null;
    }

    private static List<Pattern> patterns(List<String> regexes, String where) {
        List<Pattern> out = new ArrayList<>();
        for (String re : regexes) {
            try {
                out.add(Pattern.compile(re));
            } catch (PatternSyntaxException e) {
                throw new IllegalStateException(MANIFEST + ": bad regex in " + where + ": " + re, e);
            }
        }
        return out;
    }
}
