import dev.latvian.mods.rhino.*;
import java.nio.file.*;
import java.util.*;

/**
 * Runs kubejs/server_scripts/worldgen/structure_overrides.js in the pack's own Rhino fork with the KubeJS
 * globals stubbed, and writes what it would add: every structure_set json, frequencies.json (the table handed
 * to patcherMod), rules.raw.json / fits.raw.json (the biome rules and biome fits staged), report.md, tags.raw.json
 * (the creativeengineering:meta/* and fit structure tags its ServerEvents.tags handler adds) and biome_tags.raw.json
 * (the fit biome tags), replacements.raw.json (the replacement table staged to MetaSlots), biome_frequency.raw.json
 * (the group biome factors staged to StructureFrequencies). Usage: RhinoRun <instance root> <out dir> [nopatcher] [density=X] [groupsoff]
 */
public class RhinoRun {
	static final Map<String, String> files = new TreeMap<>();
	static String report, staged, biomeFactors, rules, fits, replacements, priority;
	static final List<String> logs = new ArrayList<>();
	static final Map<String, List<String>> tags = new TreeMap<>();
	static final Map<String, List<String>> biomeTags = new TreeMap<>();
	static Path root;
	static boolean noPatcher, groupsOff;
	static double density = 1;

	public static class Host {
		public String read(String p) throws Exception { return Files.readString(root.resolve(p)); }
		public void writeText(String p, String t) { report = t; }
		public void stage(String j) { staged = j; }
		public void stageBiomeFactors(String j) { biomeFactors = j; }
		public void stageRules(String j) { rules = j; }
		public void stageFits(String j) { fits = j; }
		public void stageReplacements(String j) { replacements = j; }
		public void stagePriority(String j) { priority = j; }
		public void add(String p, String t) { files.put(p, t); }
		public void tag(String t, String id) { tags.computeIfAbsent(t, k -> new ArrayList<>()).add(id); }
		public void biomeTag(String t, String id) { biomeTags.computeIfAbsent(t, k -> new ArrayList<>()).add(id); }
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
			"var __cb = null, __tagsCb = null, __biomeTagsCb = null;",
			"var global = { tags: { add: function (e, t, ids) { [].concat(ids).forEach(function (id) { e.add(t, id) }) } } };",
			"var console = { info: function (m) { __HOST.log('', String(m)) }, log: function (m) { __HOST.log('', String(m)) },",
			"  warn: function (m) { __HOST.log('WARN ', String(m)) }, error: function (m) { __HOST.log('ERROR ', String(m)) } };",
			"var JsonIO = { readString: function (p) { return __HOST.read(p) } };",
			"var Java = { loadClass: function (n) {",
			"  if (n.indexOf('StructureBiomeFits') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return { stage: function (j) { __HOST.stageFits(j) } } }",
			"  if (n.indexOf('StructureBiomeRules') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return { stage: function (j) { __HOST.stageRules(j) } } }",
			"  if (n.indexOf('StructureFrequencies') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return { stage: function (j) { __HOST.stage(j) }, stageBiomeFactors: function (j) { __HOST.stageBiomeFactors(j) } } }",
			"  if (n.indexOf('MetaStructure') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return {} }",
			"  if (n.indexOf('PlacementPriority') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return { stage: function (j) { __HOST.stagePriority(j) } } }",
			"  if (n.indexOf('MetaSlots') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n); return { stageReplacements: function (j) { __HOST.stageReplacements(j) } } }",
			"  if (n.indexOf('WorldgenConfig') >= 0) { if (__HOST.noPatcher()) throw new Error('Class not found ' + n);",
			"    return { structureDensity: function () { return __HOST.density() }, structureGroupsEnabled: function () { return __HOST.groups() },",
			"      metaStructuresEnabled: function () { return true }, structureReplacementsEnabled: function () { return true } } }",
			"  return { writeText: function (p, t) { __HOST.writeText(p, t) } } } };",
			"var ServerEvents = { highPriorityData: function (fn) { __cb = fn }, tags: function (t, fn) { if (t == 'worldgen/structure') __tagsCb = fn; if (t == 'worldgen/biome') __biomeTagsCb = fn } };");
		cx.evaluateString(scope, prelude, "prelude", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("kubejs/server_scripts/worldgen/structure_overrides.js")), "structure_overrides.js", 1, null);
		cx.evaluateString(scope, "__cb({ add: function (p, t) { __HOST.add(String(p), String(t)) } });", "fire", 1, null);
		cx.evaluateString(scope, "if (__tagsCb) __tagsCb({ add: function (t, id) { __HOST.tag(String(t), String(id)) } });", "fire tags", 1, null);
		cx.evaluateString(scope, "if (__biomeTagsCb) __biomeTagsCb({ add: function (t, id) { __HOST.biomeTag(String(t), String(id)) } });", "fire biome tags", 1, null);
		Path out = Paths.get(a[1]);
		Files.createDirectories(out);
		for (Map.Entry<String, String> e : files.entrySet()) {
			Path f = out.resolve(e.getKey().replace(':', '/'));
			Files.createDirectories(f.getParent());
			Files.writeString(f, e.getValue());
		}
		if (staged != null) Files.writeString(out.resolve("frequencies.raw.json"), staged);
		if (biomeFactors != null) Files.writeString(out.resolve("biome_frequency.raw.json"), biomeFactors);
		if (rules != null) Files.writeString(out.resolve("rules.raw.json"), rules);
		if (report != null) Files.writeString(out.resolve("report.md"), report);
		if (fits != null) Files.writeString(out.resolve("fits.raw.json"), fits);
		if (replacements != null) Files.writeString(out.resolve("replacements.raw.json"), replacements);
		if (priority != null) Files.writeString(out.resolve("priority.raw.json"), priority);
		if (!tags.isEmpty()) Files.writeString(out.resolve("tags.raw.json"), tagJson(tags));
		if (!biomeTags.isEmpty()) Files.writeString(out.resolve("biome_tags.raw.json"), tagJson(biomeTags));
		logs.forEach(System.out::println);
		System.out.println(files.size() + " files, staged " + (staged == null ? "nothing" : staged.length() + " chars"));
	}

	static String tagJson(Map<String, List<String>> tags) {
		StringBuilder j = new StringBuilder("{");
		for (Map.Entry<String, List<String>> e : tags.entrySet()) {
			if (j.length() > 1) j.append(',');
			j.append('"').append(e.getKey()).append("\":[");
			for (int i = 0; i < e.getValue().size(); i++) j.append(i > 0 ? "," : "").append('"').append(e.getValue().get(i)).append('"');
			j.append(']');
		}
		return j.append('}').toString();
	}
}
