// Streams JFR recordings into compact TSV/JSONL files for perf_report.py (which runs this for you).
//   java tools/perf/PerfParse.java <outDir> <recording.jfr | JFR repository dir> [more ...]
// A directory is read recursively in name order: a server's perf/jfr-repo/ holds one folder of chunk
// files per run, and each finished chunk is a valid recording. The chunk still being written (or cut off
// by a crash) fails to parse and is skipped with a note.
//
// Output (stack ids are shared by all files):
//   stacks.tsv    id  truncated(0/1)  frames leaf-first as class#method, ';'-joined
//   samples.tsv   epochMs  thread  stackId                          (jdk.ExecutionSample)
//   alloc.tsv     epochMs  thread  stackId  weightBytes  objectClass  (jdk.ObjectAllocationSample)
//   blocked.tsv   epochMs  thread  eventType  durationMs  stackId      (Server thread only)
//   events.jsonl  selected events field-by-field; durations in ms, timestamps in epoch ms. The pack's own
//                 creativeengineering.* events (patcherMod, perf.md 3.6) are all kept; a ChunkWait also
//                 gets "stack" (a stacks.tsv id: who asked for the chunk).
import jdk.jfr.EventType;
import jdk.jfr.ValueDescriptor;
import jdk.jfr.consumer.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class PerfParse {
    static final Map<String, Integer> stackIds = new HashMap<>();
    static PrintWriter stacksOut, samples, alloc, blocked, events;

    static final Set<String> GENERIC = Set.of(
        "minecraft.ServerTickTime", "minecraft.ChunkGeneration", "minecraft.NetworkSummary", "minecraft.LoadWorld",
        "jdk.GarbageCollection", "jdk.CPULoad", "jdk.GCHeapSummary", "jdk.ExecuteVMOperation",
        "jdk.ExceptionStatistics", "jdk.CPUInformation", "jdk.GCHeapConfiguration", "jdk.JVMInformation",
        "jdk.PhysicalMemory", "jdk.OSInformation");
    static final Set<String> BLOCKING = Set.of("jdk.ThreadPark", "jdk.JavaMonitorEnter", "jdk.JavaMonitorWait", "jdk.ThreadSleep");

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args[0]);
        Files.createDirectories(out);
        stacksOut = writer(out.resolve("stacks.tsv"));
        samples = writer(out.resolve("samples.tsv"));
        alloc = writer(out.resolve("alloc.tsv"));
        blocked = writer(out.resolve("blocked.tsv"));
        events = writer(out.resolve("events.jsonl"));
        List<Path> files = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            Path p = Path.of(args[i]);
            if (Files.isDirectory(p)) {
                try (Stream<Path> s = Files.walk(p)) {
                    s.filter(f -> f.toString().endsWith(".jfr")).sorted().forEach(files::add);
                }
            } else {
                files.add(p);
            }
        }
        long total = 0;
        int ok = 0;
        for (Path f : files) {
            try {
                total += read(f);
                ok++;
            } catch (Exception | AssertionError e) {
                System.out.println("skipped " + f + ": " + e);
            }
        }
        for (PrintWriter w : List.of(stacksOut, samples, alloc, blocked, events)) w.close();
        System.out.println("files=" + ok + "/" + files.size() + " events=" + total + " stacks=" + stackIds.size());
        if (ok == 0) System.exit(1);
    }

    static long read(Path f) throws IOException {
        long n = 0;
        try (RecordingFile rf = new RecordingFile(f)) {
            // Which event-type ids are jdk.ExecutionSample, so its active "period" setting can be reported.
            Set<Long> sampleTypeIds = new HashSet<>();
            for (EventType t : rf.readEventTypes()) {
                if (t.getName().equals("jdk.ExecutionSample")) sampleTypeIds.add(t.getId());
            }
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                String type = e.getEventType().getName();
                long t = e.getStartTime().toEpochMilli();
                n++;
                switch (type) {
                    case "jdk.ExecutionSample" ->
                        samples.println(t + "\t" + name(e.getThread("sampledThread")) + "\t" + stackId(e.getStackTrace()));
                    case "jdk.ObjectAllocationSample" -> {
                        RecordedClass c = e.getClass("objectClass");
                        alloc.println(t + "\t" + name(e.getThread("eventThread")) + "\t" + stackId(e.getStackTrace())
                            + "\t" + e.getLong("weight") + "\t" + (c == null ? "?" : c.getName()));
                    }
                    case "jdk.ActiveSetting" -> {
                        if (sampleTypeIds.contains(e.getLong("id")) && "period".equals(e.getString("name"))) {
                            events.println("{\"type\":\"samplePeriod\",\"t\":" + t + ",\"value\":" + str(e.getString("value")) + "}");
                        }
                    }
                    default -> {
                        if (BLOCKING.contains(type)) {
                            if ("Server thread".equals(name(e.getThread()))) {
                                blocked.println(t + "\t" + name(e.getThread()) + "\t" + type + "\t"
                                    + ms(e.getDuration()) + "\t" + stackId(e.getStackTrace()));
                            }
                        } else if (GENERIC.contains(type) || type.startsWith("creativeengineering.")) {
                            events.println(json(e, type, t));
                        }
                    }
                }
            }
        }
        return n;
    }

    static PrintWriter writer(Path p) throws IOException {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(p), StandardCharsets.UTF_8), 1 << 16));
    }

    static String name(RecordedThread th) {
        if (th == null) return "?";
        String n = th.getJavaName() != null ? th.getJavaName() : th.getOSName();
        return n == null ? "?" : n.replace('\t', ' ');
    }

    static int stackId(RecordedStackTrace st) {
        if (st == null) return -1;
        StringBuilder sb = new StringBuilder();
        for (RecordedFrame f : st.getFrames()) {
            RecordedMethod m = f.getMethod();
            if (m == null) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(m.getType().getName()).append('#').append(m.getName());
        }
        String key = (st.isTruncated() ? "1\t" : "0\t") + sb;
        Integer id = stackIds.get(key);
        if (id == null) {
            id = stackIds.size();
            stackIds.put(key, id);
            stacksOut.println(id + "\t" + key);
        }
        return id;
    }

    static double ms(Duration d) {
        if (d == null) return 0;
        try {
            return d.toNanos() / 1e6;
        } catch (ArithmeticException x) {
            return -1;
        }
    }

    static String json(RecordedEvent e, String type, long t) {
        StringBuilder sb = new StringBuilder("{\"type\":").append(str(type)).append(",\"t\":").append(t)
            .append(",\"dur\":").append(ms(e.getDuration()));
        if (type.equals("creativeengineering.ChunkWait")) sb.append(",\"stack\":").append(stackId(e.getStackTrace()));
        for (ValueDescriptor f : e.getFields()) {
            String k = f.getName();
            if (k.equals("startTime") || k.equals("duration") || k.equals("stackTrace")) continue;
            String ct = f.getContentType();
            Object v;
            // Minecraft's own events report the content type; JDK events only carry the annotation.
            if ("jdk.jfr.Timespan".equals(ct) || f.getAnnotation(jdk.jfr.Timespan.class) != null) v = e.getDuration(k);
            else if ("jdk.jfr.Timestamp".equals(ct) || f.getAnnotation(jdk.jfr.Timestamp.class) != null) v = e.getInstant(k);
            else v = e.getValue(k);
            sb.append(',').append(str(k)).append(':').append(val(v));
        }
        return sb.append('}').toString();
    }

    static String val(Object v) {
        if (v == null) return "null";
        if (v instanceof Number || v instanceof Boolean) {
            String s = v.toString();
            return (s.equals("NaN") || s.contains("Infinity")) ? "null" : s;
        }
        if (v instanceof Duration d) return Double.toString(ms(d));
        if (v instanceof Instant i) return Long.toString(i.toEpochMilli());
        if (v instanceof RecordedThread th) return str(name(th));
        if (v instanceof RecordedClass c) return str(c.getName());
        String s = v.toString();
        return str(s.length() > 300 ? s.substring(0, 300) : s);
    }

    static String str(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
