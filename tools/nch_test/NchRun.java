import dev.latvian.mods.rhino.*;
import java.nio.file.*;

// Evaluates each file argument (relative to the pack root, a[0]) in order in one Rhino scope and prints the value of
// the last one. Used by tools/test_nch_blueprint.sh to run the real neoCraftHelper.js headless.
public class NchRun {
	public static void main(String[] a) throws Exception {
		Path root = Paths.get(a[0]);
		Context cx = Context.enter();
		ScriptableObject scope = cx.initStandardObjects();
		Object out = null;
		for (int k = 1; k < a.length; k++)
			out = cx.evaluateString(scope, Files.readString(root.resolve(a[k])), a[k], 1, null);
		System.out.println(String.valueOf(out));
	}
}
