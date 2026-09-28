import dev.latvian.mods.rhino.*;
import java.nio.file.*;
import java.util.*;

/**
 * Runs kubejs/server_scripts/worldgen/structure_overrides.js in the pack's own Rhino fork with the KubeJS
 * globals stubbed, and writes what it would add: every structure_set json, frequencies.json (the table handed
 * to patcherMod) and report.md. Usage: RhinoRun <instance root> <out dir> [nopatcher] [density=X] [groupsoff]
 */
public class RhinoRun {
	static final Map<String, String> files = new TreeMap<>();
	static String report, staged;
	static final List<String> logs = new ArrayList<>();
	static Path root;
	static boolean noPatcher, groupsOff;
	static double density = 1;

	public static class Host {
		public String read(String p) throws Exception { return Files.readString(root.resolve(p)); }
		public void writeText(String p, String t) { report = t; }
		public void stage(String j) { staged = j; }
		public void add(String p, String t) { files.put(p, t); }
		public void log(String level, String msg) { logs.add(level + msg); }
		public boolean noPatcher() { return noPatcher; }
		public double density() { return density; }
		public boolean groups() { return !groupsOff; }
	}

	public static void main(String[] a) throws Exception {
		root = Paths.get(a[0]);
		for (int i = 2; i < a.length; i++) {
			if (a[i].equals("nopatcher")) noPatcher = true;
			else if (a[i].equals("groupsoff")) groupsOff = true;
			else if (a[i].startsWith("density=")) density = Double.parseDouble(a[i].substring(8));
		}
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		ScriptableObject.putProperty(scope, "__HOST", Context.javaToJS(cx, new Host(), scope), cx);
		String prelude = String.join("\n",
			"var __cb = null;",
			"var console = { info: function (m) { __HOST.log('', String(m)) }, log: function (m) { __HOST.log('', String(m)) },",
			"  warn: function (m) { __HOST.log('WARN ', String(m)) }, error: function (m) { __HOST.log('ERROR ', String(m)) } };",
			"var JsonIO = { readString: function (p) { return __HOST.read(p) } };",
			"var Java = { loadClass: function (n) {",
			"  if (n.indexOf('StructureFrequencies') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return { stage: function (j) { __HOST.stage(j) } } }",
			"  if (n.indexOf('WorldgenConfig') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n);",
			"    return { structureDensity: function () { return __HOST.density() }, structureGroupsEnabled: function () { return __HOST.groups() } } }",
			"  return { writeText: function (p, t) { __HOST.writeText(p, t) } } } };",
			"var ServerEvents = { highPriorityData: function (fn) { __cb = fn } };");
		cx.evaluateString(scope, prelude, "prelude", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("kubejs/server_scripts/worldgen/structure_overrides.js")), "structure_overrides.js", 1, null);
		cx.evaluateString(scope, "__cb({ add: function (p, t) { __HOST.add(String(p), String(t)) } });", "fire", 1, null);
		Path out = Paths.get(a[1]);
		Files.createDirectories(out);
		for (Map.Entry<String, String> e : files.entrySet()) {
			Path f = out.resolve(e.getKey().replace(':', '/'));
			Files.createDirectories(f.getParent());
			Files.writeString(f, e.getValue());
		}
		if (staged != null) Files.writeString(out.resolve("frequencies.raw.json"), staged);
		if (report != null) Files.writeString(out.resolve("report.md"), report);
		logs.forEach(System.out::println);
		System.out.println(files.size() + " files, staged " + (staged == null ? "nothing" : staged.length() + " chars"));
	}
}
