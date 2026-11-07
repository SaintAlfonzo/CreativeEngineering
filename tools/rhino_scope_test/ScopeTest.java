// Two Rhino scopes (as two KubeJS script packs): a function from scope A walks plain objects made in scope B.
// Checks which "plain object" test survives the scope boundary. Usage: tools/rhino_scope_test/run.sh
import dev.latvian.mods.rhino.*;
public class ScopeTest {
  public static void main(String[] a) throws Exception {
    Context cx = Context.enter();
    ScriptableObject A = cx.initStandardObjects();
    ScriptableObject B = cx.initStandardObjects();
    ScriptableObject G = (ScriptableObject) cx.newObject(A);
    ScriptableObject.putProperty(A, "global", G, cx);
    ScriptableObject.putProperty(B, "global", G, cx);
    ScriptableObject.putProperty(A, "jobj", Context.javaToJS(cx, new java.util.ArrayList<String>(), A), cx);
    String src = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(a[0])));
    cx.evaluateString(A, src, "a.js", 1, null);
    Object r = cx.evaluateString(B, "var o = {output: undefined}; var out = [];"
      + "out.push('protoSame=' + (Object.getPrototypeOf(o) === global.protoA));"
      + "out.push('toString=' + Object.prototype.toString.call(o));"
      + "out.push('walk1=' + global.walk({output: undefined}, 'filter'));"
      + "out.push('walk2=' + global.walk({input: null}, 'filter'));"
      + "out.push('walk3=' + global.walk({output: 'x', not: [{id: undefined}]}, 'filter'));"
      + "out.push('walk4=' + global.walk({output: 'x'}, 'filter'));"
      + "out.push('walk5=' + global.walk([{a:1},{b:undefined}], 'filter'));"
      + "out.push('walkJava=' + global.walk(global.jobjA, 'filter'));"
      + "out.push('walkRe=' + global.walk(/bread/, 'filter'));"
      + "out.join('\\n')", "b.js", 1, null);
    System.out.println(String.valueOf(r));
  }
}
