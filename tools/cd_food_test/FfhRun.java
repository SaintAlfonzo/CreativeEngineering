import dev.latvian.mods.rhino.*;
import java.nio.file.*;

public class FfhRun {
	public static void main(String[] a) throws Exception {
		Path root = Paths.get(a[0]);
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		cx.evaluateString(scope, Files.readString(Paths.get(a[1])), "prelude.js", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js")), "item_dict.js", 1, null);
		cx.evaluateString(scope, Files.readString(root.resolve("development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/fluid_dict.js")), "fluid_dict.js", 1, null);
		// food dict (global.food), so scripts that read food keys run too (Veggies pilot, framework #4)
		for (String lib : new String[] {"food_grammar.js", "food_dict.js"})
			cx.evaluateString(scope, Files.readString(root.resolve("development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/" + lib)), lib, 1, null);
		cx.evaluateString(scope, "global.CD = { recipes: function (fn) { fn() }, on: function () { return true }, qol: function () { return true } };", "cd", 1, null);
		String cd = "development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_food.js";
		cx.evaluateString(scope, Files.readString(root.resolve(cd)), "cd_food.js", 1, null);
		for (int k = 2; k < a.length; k++) {
			String p = "kubejs/data/creativedelight/kubejs/" + a[k];
			if (a[k].startsWith("parse:")) {
				p = "kubejs/data/creativedelight/kubejs/" + a[k].substring(6);
				cx.compileString(Files.readString(root.resolve(p)), p, 1, null);
				System.out.println("parsed " + p);
				continue;
			}
			cx.evaluateString(scope, "__cbs = []", "reset", 1, null);
			cx.evaluateString(scope, Files.readString(root.resolve(p)), p, 1, null);
			Object out = cx.evaluateString(scope, "__run('" + a[k] + "')", "run", 1, null);
			System.out.println(out);
		}
	}
}
