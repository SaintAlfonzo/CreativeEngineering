import dev.latvian.mods.rhino.*;
import java.nio.file.*;

/**
 * Loads kubejs/startup_scripts/_lib/{item_dict,food_grammar,food_dict}.js in the pack's own Rhino fork, in their
 * priority order, and prints the result of the food dict's load check. With "bad", three broken lines are put in
 * front of the check (a missing general key, a key outside the grammar, an item named twice) and the check must
 * throw naming all three. Usage: RhinoRun <instance root> [bad]
 */
public class RhinoRun {
	public static void main(String[] a) throws Exception {
		Path lib = Paths.get(a[0], "kubejs/startup_scripts/_lib");
		boolean bad = a.length > 1 && a[1].equals("bad");
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		cx.evaluateString(scope, "var global = {}; var StartupEvents = { postInit: function () {} };"
			+ "var console = { log: function () {}, info: function () {}, warn: function () {}, error: function () {} };", "prelude", 1, null);
		cx.evaluateString(scope, Files.readString(lib.resolve("item_dict.js")), "item_dict.js", 1, null);
		cx.evaluateString(scope, Files.readString(lib.resolve("food_grammar.js")), "food_grammar.js", 1, null);
		String food = Files.readString(lib.resolve("food_dict.js"));
		if (bad) {
			food = food.replace("\tglobal.foodKey = ", "\tf.fruit_missing = i.no_such_key;\n\tf.notacategory_apple = i.carrot;\n"
				+ "\tf.fruit_apple_again = i.apple;\n\tglobal.foodKey = ");
		}
		try {
			cx.evaluateString(scope, food, "food_dict.js", 1, null);
			Object out = cx.evaluateString(scope, "var n = 0, m = 0; for (var k in global.food) n++; for (var k in global.foodKey) m++;"
				+ "'keys ' + n + ', index ' + m + ', apple -> ' + global.foodKey['minecraft:apple'] + ', f.veggie_slice_cooked_potato -> '"
				+ " + global.food.veggie_slice_cooked_potato + ', parse ' + JSON.stringify(global.subdict.parse('veggie_slice_cooked_potato', global.foodGrammar))"
				+ " + ', ice_cream ' + JSON.stringify(global.subdict.parse('ice_cream_mint', global.foodGrammar))", "report", 1, null);
			System.out.println("loaded: " + out);
			System.exit(bad ? 1 : 0);
		} catch (RhinoException e) {
			System.out.println("threw: " + e.details());
			System.exit(bad ? 0 : 1);
		}
	}
}
