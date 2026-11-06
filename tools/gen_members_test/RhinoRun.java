import dev.latvian.mods.rhino.*;
import java.nio.file.*;
import java.util.*;

/**
 * Generator member scripts in the pack's own Rhino fork (creative_delight.md 28.2, framework #7, test CDT-14).
 * Loads item_dict.js, CD's cd_core.js (stubbed Java), every generated member script given, then the readers
 * (cans.js, food_type_tags.js, placeable_tags.js, can_tags.js, mortar.js), fires their events with recording stubs
 * and prints one JSON object: the merged data per name, the tags added, the cans created / modified, the mortar crafts
 * and every console warning or error. Usage: RhinoRun <instance root> <file> ... (instance-relative paths, in order)
 */
public class RhinoRun {
	public static void main(String[] a) throws Exception {
		Path root = Paths.get(a[0]);
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		cx.evaluateString(scope, String.join("\n",
			"var global = {}; var __log = []; var __cb = { startup: [], modify: [], tags: [], recipes: [] };",
			"var console = { log: function () {}, info: function () {},",
			"  warn: function (m) { __log.push('WARN ' + m) }, error: function (m) { __log.push('ERROR ' + m) } };",
			"var Platform = { isLoaded: function () { return true } };",
			"var Java = { loadClass: function () { return { members: function () { return [] }, isMember: function () { return false },",
			"  member: function () { return true }, qol: function () { return true }, worldgen: function () { return true } } } };",
			"var StartupEvents = { registry: function (t, f) { if (t == 'item') __cb.startup.push(f) }, postInit: function () {} };",
			"var ItemEvents = { modification: function (f) { __cb.modify.push(f) } };",
			"var ServerEvents = { tags: function (t, f) { if (t == 'item') __cb.tags.push(f) }, recipes: function (f) { __cb.recipes.push(f) },",
			"  loaded: function () {} };",
			"var Item = { of: function (id, n) { return { id: String(id), count: n } } };",
			""), "prelude", 1, null);
		for (int k = 1; k < a.length; k++) {
			String f = a[k];
			try {
				cx.evaluateString(scope, Files.readString(root.resolve(f)), f, 1, null);
			} catch (RhinoException e) {
				System.out.println("{\"loadError\": " + quote(f + ": " + e.details()) + "}");
				System.exit(1);
			}
			if (f.endsWith("item_dict.js")) {
				// after the dict: the stubs the readers call (cd_core.js comes next and wants global.NCH)
				cx.evaluateString(scope, String.join("\n",
					"var __tags = {}, __mortar = [], __created = {}, __modified = [];",
					"function __tag(e, tag, ids) { tag = String(tag); if (!__tags[tag]) __tags[tag] = [];",
					"  (Array.isArray(ids) ? ids : [ids]).forEach(function (x) { __tags[tag].push(String(x)) }) }",
					"global.tags = { addTag: __tag, add: __tag };",
					"global.removedItems = [];",
					"global.NCH = { registerConfig: function () {}, withNamespace: function (ns, fn) { return fn() },",
					"  mortar: function (out, inp, e, id, n) { __mortar.push({ id: String(id), output: out.id, count: out.count, input: String(inp), inputs: n }) } };",
					""), "stubs", 1, null);
			}
		}
		Object out = cx.evaluateString(scope, String.join("\n",
			"function __food() { var r = {}; var b = {",
			"  hunger: function (x) { r.hunger = x; return b }, saturation: function (x) { r.modifier = x; return b },",
			"  meat: function () { return b }, fastToEat: function () { return b }, alwaysEdible: function () { return b },",
			"  effect: function () { r.effects = (r.effects || 0) + 1; return b }, eaten: function () { return b } }; return [r, b] }",
			"__cb.startup.forEach(function (f) { f({ create: function (id) { var rec = { id: String(id) }; __created[String(id)] = rec; var b = {",
			"  displayName: function (n) { rec.name = String(n); return b }, texture: function () { return b }, glow: function () { return b },",
			"  food: function (fn) { var fb = __food(); fn(fb[1]); rec.food = fb[0]; return b } }; return b } }) });",
			"__cb.modify.forEach(function (f) { f({ modify: function (id, fn) { __modified.push(String(id)) } }) });",
			"__cb.tags.forEach(function (f) { f({}) });",
			"__cb.recipes.forEach(function (f) { f({}) });",
			"JSON.stringify({ merged: { can_types: global.CD.genMerge('can_types'), can_servings: global.CD.genMerge('can_servings'),",
			"  food_types: global.CD.genMerge('food_types'), placeable: global.CD.genMerge('placeable'),",
			"  mortar_catalog: global.CD.genMerge('mortar_catalog') },",
			"  tags: __tags, created: __created, modified: __modified, mortar: __mortar, log: __log })",
			""), "report", 1, null);
		System.out.println(out);
	}

	static String quote(String s) {
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}
}
