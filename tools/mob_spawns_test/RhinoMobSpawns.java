import dev.latvian.mods.rhino.*;
import java.nio.file.*;
import java.util.*;

/**
 * Runs kubejs/data/creativeworldgen/kubejs/worldgen/mob_spawns.js in the pack's own Rhino fork with the KubeJS globals stubbed
 * (tag_helper.js loaded first for global.tags). Fires the worldgen/biome and block tags handlers and
 * highPriorityData, and writes tags.json (tag -> entries; block tags as "block:<tag>") and every data file the script adds under <out>/data/. ServerEvents.loaded
 * needs the live registry and is not fired.
 * Usage: RhinoMobSpawns <instance root> <out dir> <rules json> [off] [notuning] [missing=<modid>]
 */
public class RhinoMobSpawns {
	static final Map<String, List<String>> tags = new TreeMap<>();
	static final Map<String, String> files = new TreeMap<>();
	static final List<String> logs = new ArrayList<>();
	static Path root;
	static String rules;
	static boolean off, noTuning;
	static final Set<String> missing = new HashSet<>();

	public static class Host {
		public String read(String p) throws Exception {
			if (p.endsWith("worldgen/mob_spawns.json")) return Files.readString(Paths.get(rules));
			return Files.readString(root.resolve(p));
		}
		public void tag(String t, String id) { tags.computeIfAbsent(t, k -> new ArrayList<>()).add(id); }
		public void add(String p, String t) { files.put(p, t); }
		public void log(String level, String msg) { logs.add(level + msg); }
		public boolean enabled() { return !off; }
		public boolean tuning() { return !noTuning; }
		public boolean loaded(String ns) { return !missing.contains(ns); }
	}

	public static void main(String[] a) throws Exception {
		root = Paths.get(a[0]);
		rules = a[2];
		for (int i = 3; i < a.length; i++) {
			if (a[i].equals("off")) off = true;
			else if (a[i].equals("notuning")) noTuning = true;
			else if (a[i].startsWith("missing=")) missing.add(a[i].substring(8));
		}
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		ScriptableObject.putProperty(scope, "__HOST", Context.javaToJS(cx, new Host(), scope), cx);
		String prelude = String.join("\n",
			"var global = {};",
			"var __tags = null, __btags = null, __data = null;",
			"var console = { info: function (m) { __HOST.log('', String(m)) }, log: function (m) { __HOST.log('', String(m)) },",
			"  warn: function (m) { __HOST.log('WARN ', String(m)) }, error: function (m) { __HOST.log('ERROR ', String(m)) } };",
			"var JsonIO = { readString: function (p) { return __HOST.read(p) } };",
			"var Platform = { isLoaded: function (ns) { return __HOST.loaded(ns) } };",
			"var Java = { loadClass: function (n) {",
			"  if (n.indexOf('WorldgenConfig') >= 0) return { mobSpawnsEnabled: function () { return __HOST.enabled() } };",
			"  if (n.indexOf('SpawnTuning') >= 0) { if (!__HOST.tuning()) throw new Error('ClassNotFoundException: ' + n);",
			"    return { summary: function () { return 'stub' } } }",
			"  return {} } };",
			"var ServerEvents = { tags: function (type, fn) { if (type === 'worldgen/biome') __tags = fn; else if (type === 'block') __btags = fn },",
			"  highPriorityData: function (fn) { __data = fn }, loaded: function (fn) {} };");
		cx.evaluateString(scope, prelude, "prelude", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js")), "tag_helper.js", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("kubejs/data/creativeworldgen/kubejs/worldgen/mob_spawns.js")), "mob_spawns.js", 1, null);
		cx.evaluateString(scope, "__tags({ add: function (t, id) { __HOST.tag(String(t), String(id)) } });", "fire_tags", 1, null);
		cx.evaluateString(scope, "if (__btags) __btags({ add: function (t, id) { __HOST.tag('block:' + String(t), String(id)) } });", "fire_block_tags", 1, null);
		cx.evaluateString(scope, "__data({ add: function (p, t) { __HOST.add(String(p), String(t)) } });", "fire_data", 1, null);
		Path out = Paths.get(a[1]);
		Files.createDirectories(out.resolve("data"));
		StringBuilder sb = new StringBuilder("{");
		boolean first = true;
		for (Map.Entry<String, List<String>> e : tags.entrySet()) {
			sb.append(first ? "" : ",").append("\n \"").append(e.getKey()).append("\": [");
			for (int i = 0; i < e.getValue().size(); i++) sb.append(i == 0 ? "\"" : ", \"").append(e.getValue().get(i)).append('"');
			sb.append(']');
			first = false;
		}
		Files.writeString(out.resolve("tags.json"), sb.append("\n}\n").toString());
		for (Map.Entry<String, String> e : files.entrySet()) {
			Path f = out.resolve("data").resolve(e.getKey().replace(':', '/'));
			Files.createDirectories(f.getParent());
			Files.writeString(f, e.getValue());
		}
		logs.forEach(System.out::println);
		System.out.println(tags.size() + " tags, " + files.size() + " data files");
	}
}
