import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.RhinoException;
import dev.latvian.mods.rhino.ScriptableObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Dict baseline for the dict and helper move into Creative Common (feature_writeup/plan_cb_publish.md 2.2 steps 1 and 8).
 * Loads every .js file under the given folders in KubeJS's order (priority header descending, then path), in the
 * pack's Rhino fork, with no game: Java, Item and the event objects are inert stubs, so only what the dicts build at
 * load time is compared. Prints one JSON object: per global, its key count and a hash of its JSON form (functions
 * are left out by JSON.stringify), plus every file that threw.
 * Usage: DictBaseline [--dump=<file>] <folder> [<folder> ...]   (run tools/dict_baseline/run.sh)
 * --dump writes the resolved dicts (item, fluid, blocks, effect, food, tool, misc: key -> id) as JSON for
 * tools/guard_check.py.
 */
public class DictBaseline {
	static final Pattern PRIORITY = Pattern.compile("^//\\s*priority:\\s*(-?\\d+)", Pattern.MULTILINE);
	static final String[] GLOBALS = {"item", "subdict", "food", "foodGrammar", "foodKey", "fluid", "blocks", "effect",
		"tool", "toolKey", "misc", "miscKey", "multiblock", "removedItems", "removedWoods", "removedWoodTracks",
		"itemMerges", "itemMergeGroups", "itemMergeRun", "addItemMerges", "trades", "jeiOrder"};

	record Script(Path path, String rel, int priority, String src) {}

	public static void main(String[] a) throws Exception {
		List<Script> scripts = new ArrayList<>();
		String dump = null;
		for (String dir : a) {
			if (dir.startsWith("--dump=")) { dump = dir.substring(7); continue; }
			Path root = Paths.get(dir);
			try (Stream<Path> s = Files.walk(root)) {
				for (Path p : (Iterable<Path>) s.filter(f -> f.toString().endsWith(".js")).sorted()::iterator) {
					String src = Files.readString(p);
					Matcher m = PRIORITY.matcher(src.substring(0, Math.min(src.length(), 400)));
					scripts.add(new Script(p, root.relativize(p).toString(), m.find() ? Integer.parseInt(m.group(1)) : 0, src));
				}
			}
		}
		scripts.sort(Comparator.comparingInt((Script s) -> -s.priority).thenComparing(Script::rel));

		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		cx.evaluateString(scope, String.join("\n",
			"var global = {};",
			"var console = { log: function () {}, info: function () {}, warn: function () {}, error: function () {} };",
			"var __noop = function () {};",
			"var StartupEvents = { postInit: __noop, init: __noop, registry: __noop };",
			"var ItemEvents = { modification: __noop, toolTierRegistry: __noop, armorTierRegistry: __noop };",
			"var BlockEvents = { modification: __noop }; var JEIEvents = {}; var ForgeEvents = { onEvent: __noop };",
			"var Platform = { isLoaded: function () { return true; }, getMods: function () { return {}; } };",
			"var Java = { loadClass: function () { return {}; } };",
			"var Item = { of: function () { return {}; } }; var Ingredient = { of: function () { return { itemIds: [] }; } };",
			""), "prelude", 1, null);

		StringBuilder errors = new StringBuilder();
		StringBuilder order = new StringBuilder();
		for (Script s : scripts) {
			order.append(order.length() == 0 ? "" : ",").append('"').append(s.rel).append('"');
			try {
				cx.evaluateString(scope, s.src, s.rel, 1, null);
			} catch (RhinoException e) {
				errors.append(errors.length() == 0 ? "" : ",").append('"').append(s.rel).append(": ")
					.append(e.details().replace("\\", "\\\\").replace("\"", "'")).append('"');
			}
		}

		StringBuilder js = new StringBuilder("var __out = {};");
		js.append("function __hash(s) { var h = 0x811c9dc5; for (var k = 0; k < s.length; k++) { h ^= s.charCodeAt(k);")
			.append(" h = (h + (h << 1) + (h << 4) + (h << 7) + (h << 8) + (h << 24)) >>> 0; } return ('00000000' + h.toString(16)).slice(-8); };");
		for (String g : GLOBALS) {
			js.append("if (global['").append(g).append("'] !== undefined) { var v = global['").append(g).append("'];")
				.append(" var j = JSON.stringify(v) || String(typeof v);")
				.append(" __out['").append(g).append("'] = { type: typeof v, keys: (typeof v === 'object' && v) ? Object.keys(v).length : -1,")
				.append(" hash: __hash(String(j)), bytes: String(j).length }; }");
		}
		js.append("JSON.stringify(__out)");
		Object out = cx.evaluateString(scope, js.toString(), "report", 1, null);
		if (dump != null) {
			Object dicts = cx.evaluateString(scope, "JSON.stringify({ item: global.item, fluid: global.fluid, blocks: global.blocks,"
				+ " effect: global.effect, food: global.food, tool: global.tool, misc: global.misc })", "dump", 1, null);
			Files.writeString(Paths.get(dump), String.valueOf(dicts));
		}
		System.out.println("{\"files\": " + scripts.size() + ", \"order\": [" + order + "], \"errors\": [" + errors
			+ "], \"globals\": " + out + "}");
	}
}
