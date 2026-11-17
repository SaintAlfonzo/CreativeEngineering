package com.SaintAlfonzo.creativedevtools.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The argument string of one {@code /cdevtest} call: positional tokens, then {@code key=value} options in any order
 * (a token with '=' is an option; a bare option word such as {@code keep} is {@code keep=true}). Parsed by hand rather
 * than by Brigadier argument types so a bad argument comes back as a {@code [cdevtest] ... error=} line that a batch
 * can match, instead of a Brigadier "Incorrect argument" with no verb in it. Pure Java (no Minecraft classes): the
 * headless harness tests it (creative_devtools.md, /cdevtest).
 */
public final class TestArgs {

	/** Thrown for a missing or malformed argument; the message becomes the {@code error=} value. */
	public static final class Bad extends RuntimeException {
		public Bad(String msg) {
			super(msg);
		}
	}

	private final List<String> pos = new ArrayList<>();
	private final Map<String, String> opts = new LinkedHashMap<>();
	private int next;

	private TestArgs() {
	}

	/** Splits on whitespace; a double-quoted run keeps its spaces (for SNBT values). Bare words in
	 * {@code flags} are options, everything else without '=' is positional. */
	public static TestArgs parse(String raw, String... flags) {
		TestArgs a = new TestArgs();
		for (String tok : tokens(raw == null ? "" : raw)) {
			int eq = tok.indexOf('=');
			// "id[age=7]" and "{a:1}" carry '=' or ':' inside brackets: only a key made of word characters is an option
			if (eq > 0 && tok.substring(0, eq).matches("[A-Za-z][A-Za-z0-9_]*")) {
				a.opts.put(tok.substring(0, eq).toLowerCase(Locale.ROOT), tok.substring(eq + 1));
			} else if (isFlag(tok, flags)) {
				a.opts.put(tok.toLowerCase(Locale.ROOT), "true");
			} else {
				a.pos.add(tok);
			}
		}
		return a;
	}

	private static boolean isFlag(String tok, String[] flags) {
		for (String f : flags) if (f.equalsIgnoreCase(tok)) return true;
		return false;
	}

	static List<String> tokens(String raw) {
		List<String> out = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean quoted = false, any = false;
		for (int i = 0; i < raw.length(); i++) {
			char ch = raw.charAt(i);
			if (ch == '"') {
				quoted = !quoted;
				any = true;
			} else if (Character.isWhitespace(ch) && !quoted) {
				if (any) out.add(cur.toString());
				cur.setLength(0);
				any = false;
			} else {
				cur.append(ch);
				any = true;
			}
		}
		if (quoted) throw new Bad("unclosed quote");
		if (any) out.add(cur.toString());
		return out;
	}

	public int remaining() {
		return pos.size() - next;
	}

	public boolean hasNext() {
		return next < pos.size();
	}

	public String word(String what) {
		if (next >= pos.size()) throw new Bad("missing " + what);
		return pos.get(next++);
	}

	public String wordOr(String fallback) {
		return next < pos.size() ? pos.get(next++) : fallback;
	}

	public int integer(String what) {
		String w = word(what);
		try {
			return Integer.parseInt(w);
		} catch (NumberFormatException e) {
			throw new Bad(what + " not an integer: " + w);
		}
	}

	public double number(String what) {
		String w = word(what);
		try {
			return Double.parseDouble(w);
		} catch (NumberFormatException e) {
			throw new Bad(what + " not a number: " + w);
		}
	}

	/** Three integers (a block position). */
	public int[] xyz(String what) {
		return new int[]{integer(what + " x"), integer(what + " y"), integer(what + " z")};
	}

	/** Positional tokens left over: a call with extra words is refused, so a typo does not pass silently. */
	public void end() {
		if (next < pos.size()) throw new Bad("unexpected argument: " + pos.get(next));
	}

	public boolean has(String key) {
		return opts.containsKey(key);
	}

	public String opt(String key, String fallback) {
		String v = opts.get(key);
		return v == null ? fallback : v;
	}

	public boolean flag(String key) {
		String v = opts.get(key);
		return v != null && !v.equals("false") && !v.equals("0") && !v.equals("off");
	}

	public int optInt(String key, int fallback, int min, int max) {
		String v = opts.get(key);
		if (v == null) return fallback;
		int n;
		try {
			n = Integer.parseInt(v);
		} catch (NumberFormatException e) {
			throw new Bad(key + " not an integer: " + v);
		}
		if (n < min || n > max) throw new Bad(key + " out of range " + min + ".." + max + ": " + n);
		return n;
	}

	public double optNum(String key, double fallback) {
		String v = opts.get(key);
		if (v == null) return fallback;
		try {
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			throw new Bad(key + " not a number: " + v);
		}
	}

	/** "a,b,c" as three numbers, or null when the option is absent. */
	public double[] optTriple(String key) {
		String v = opts.get(key);
		if (v == null) return null;
		String[] p = v.split(",");
		if (p.length != 3) throw new Bad(key + " needs three comma-separated numbers: " + v);
		double[] d = new double[3];
		for (int i = 0; i < 3; i++) {
			try {
				d[i] = Double.parseDouble(p[i]);
			} catch (NumberFormatException e) {
				throw new Bad(key + " not a number: " + p[i]);
			}
		}
		return d;
	}

	/** Options nobody read are refused for the same reason as extra positionals. */
	public void checkOpts(String... known) {
		for (String k : opts.keySet()) {
			boolean ok = false;
			for (String n : known) if (n.equals(k)) ok = true;
			if (!ok) throw new Bad("unknown option: " + k);
		}
	}

	/** Box volume of two corners, inclusive; used to refuse runaway scans before they start. */
	public static long volume(int[] a, int[] b) {
		long v = 1;
		for (int i = 0; i < 3; i++) v *= Math.abs((long) a[i] - b[i]) + 1;
		return v;
	}
}
