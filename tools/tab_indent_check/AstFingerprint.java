import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * Whitespace-blind fingerprints for tools/tab_indent.py: one line per file, "<sha256> <path>" or "ERR <path> <why>".
 *
 *   java tools/tab_indent_check/AstFingerprint.java java FILE...       javac parse tree, printed (no classpath needed)
 *   java -cp RHINO_JAR tools/tab_indent_check/AstFingerprint.java js FILE...
 *                                                                        Rhino (the KubeJS fork) interpreter code:
 *                                                                        icode, string, number, regexp and template
 *                                                                        tables of every function
 *
 * Both fingerprints ignore indentation and trailing whitespace outside literals and keep every literal's text, so a
 * file whose fingerprint changes had literal content (or code) changed.
 */
public class AstFingerprint {
	public static void main(String[] args) throws Exception {
		String mode = args[0];
		for (String p : Arrays.asList(args).subList(1, args.length)) {
			try {
				String src = Files.readString(Path.of(p), StandardCharsets.UTF_8);
				String print = mode.equals("java") ? javaPrint(p, src) : jsPrint(p, src);
				System.out.println(sha(print) + " " + p);
			} catch (Throwable t) {
				System.out.println("ERR " + p + " " + String.valueOf(t).replace('\n', ' '));
			}
		}
	}

	static String sha(String s) throws Exception {
		byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
		StringBuilder b = new StringBuilder();
		for (byte x : d) b.append(String.format("%02x", x));
		return b.toString();
	}

	static String javaPrint(String path, String src) throws Exception {
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		JavaFileObject fo = new SimpleJavaFileObject(URI.create("string:///" + path.replace('\\', '/')), JavaFileObject.Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(boolean ignore) {
				return src;
			}
		};
		DiagnosticCollector<JavaFileObject> diag = new DiagnosticCollector<>();
		JavacTask task = (JavacTask) jc.getTask(null, null, diag, List.of("-proc:none"), null, List.of(fo));
		StringBuilder b = new StringBuilder();
		for (CompilationUnitTree cu : task.parse()) b.append(cu.toString());
		for (var d : diag.getDiagnostics())
			if (d.getKind() == javax.tools.Diagnostic.Kind.ERROR) throw new IllegalStateException("parse: " + d.getMessage(null));
		return b.toString();
	}

	static String jsPrint(String path, String src) throws Exception {
		Class<?> ctxC = Class.forName("dev.latvian.mods.rhino.Context");
		if (cx == null) cx = ctxC.getMethod("enter").invoke(null);
		Method compile = ctxC.getMethod("compileString", String.class, String.class, int.class, Object.class);
		Object script;
		try {
			script = compile.invoke(cx, src, "f.js", 1, null);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new IllegalStateException("compile: " + e.getCause());
		}
		StringBuilder b = new StringBuilder();
		idata(field(script, "idata"), b);
		return b.toString();
	}

	static Object cx;

	static void idata(Object d, StringBuilder b) throws Exception {
		b.append("{name=").append(field(d, "itsName")).append(" type=").append(field(d, "itsFunctionType"));
		b.append(" icode=").append(Arrays.toString((byte[]) field(d, "itsICode")));
		b.append(" strings=").append(Arrays.deepToString((Object[]) field(d, "itsStringTable")));
		b.append(" doubles=").append(Arrays.toString((double[]) field(d, "itsDoubleTable")));
		b.append(" regexps=[");
		Object[] res = (Object[]) field(d, "itsRegExpLiterals");
		if (res != null)
			for (Object re : res)
				b.append(new String((char[]) field(re, "source"))).append('/').append(field(re, "flags")).append(',');
		b.append(']');
		b.append(" templates=").append(Arrays.deepToString((Object[]) field(d, "itsTemplateLiterals")));
		b.append(" exceptions=").append(Arrays.toString((int[]) field(d, "itsExceptionTable")));
		Object[] nested = (Object[]) field(d, "itsNestedFunctions");
		if (nested != null) for (Object n : nested) idata(n, b);
		b.append('}');
	}

	static Object field(Object o, String name) throws Exception {
		for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
			try {
				Field f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f.get(o);
			} catch (NoSuchFieldException ignored) {
			}
		}
		throw new NoSuchFieldException(name);
	}
}
