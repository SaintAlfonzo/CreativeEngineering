import dev.latvian.mods.rhino.*;
import java.nio.file.*;
import java.util.*;

/**
 * Runs Creative Delight's kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.js (or the pack's
 * kubejs/server_scripts/worldgen/mushroom_spawns.js with script=mushroom_spawns) in the pack's own Rhino fork with the
 * KubeJS globals and global.CD stubbed (tag_helper.js and _biome_rules.js loaded first). Fires the worldgen/biome tags handler and highPriorityData, and
 * writes tags.json (tag -> biomes) and every data file the script adds under <out>/data/. ServerEvents.loaded
 * needs the live registry and is not fired.
 * Usage: RhinoWildCrops <instance root> <out dir> [off] [missing=<modid>] [script=<name>]
 */
public class RhinoWildCrops {
	static final Map<String, List<String>> tags = new TreeMap<>();
	static final Map<String, String> files = new TreeMap<>();
	static final List<String> logs = new ArrayList<>();
	static Path root;
	static boolean off;
	static final Set<String> missing = new HashSet<>();
	static String script = "wild_crops";

	public static class Host {
		public String read(String p) throws Exception { return Files.readString(root.resolve(p)); }
		public void tag(String t, String id) { tags.computeIfAbsent(t, k -> new ArrayList<>()).add(id); }
		public void add(String p, String t) { files.put(p, t); }
		public void log(String level, String msg) { logs.add(level + msg); }
		public boolean enabled() { return !off; }
		public boolean loaded(String ns) { return !missing.contains(ns); }
	}

	public static void main(String[] a) throws Exception {
		root = Paths.get(a[0]);
		for (int i = 2; i < a.length; i++) {
			if (a[i].equals("off")) off = true;
			else if (a[i].startsWith("missing=")) missing.add(a[i].substring(8));
			else if (a[i].startsWith("script=")) script = a[i].substring(7);
		}
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		ScriptableObject.putProperty(scope, "__HOST", Context.javaToJS(cx, new Host(), scope), cx);
		String prelude = String.join("\n",
			"var global = { CD: { worldgen: function (id) { return __HOST.enabled() } } };",
			"var __tags = null, __data = null;",
			"var console = { info: function (m) { __HOST.log('', String(m)) }, log: function (m) { __HOST.log('', String(m)) },",
			"  warn: function (m) { __HOST.log('WARN ', String(m)) }, error: function (m) { __HOST.log('ERROR ', String(m)) } };",
			"var JsonIO = { readString: function (p) { return __HOST.read(p) } };",
			"var Platform = { isLoaded: function (ns) { return __HOST.loaded(ns) } };",
			"var Java = { loadClass: function (n) {",
			"  if (n.indexOf('CEConfig') >= 0) return { WORLDGEN: { systems: { mushroomSpawns: { get: function () { return __HOST.enabled() } } } } };",
			"  return {} } };",
			"var ServerEvents = { tags: function (type, fn) { if (type === 'worldgen/biome') __tags = fn },",
			"  highPriorityData: function (fn) { __data = fn }, loaded: function (fn) {} };");
		cx.evaluateString(scope, prelude, "prelude", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js")), "tag_helper.js", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("kubejs/data/creativeworldgen/kubejs/worldgen/_biome_rules.js")), "_biome_rules.js", 1, null);
		String path = script.equals("wild_crops") ? "kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.js"
				: "kubejs/data/creativeworldgen/kubejs/worldgen/" + script + ".js";
		cx.evaluateString(scope, Files.readString(root.resolve(path)), script + ".js", 1, null);
		cx.evaluateString(scope, "__tags({ add: function (t, id) { __HOST.tag(String(t), String(id)) } });", "fire_tags", 1, null);
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
