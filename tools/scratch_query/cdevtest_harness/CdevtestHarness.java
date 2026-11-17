import com.SaintAlfonzo.creativedevtools.test.TestArgs;
import com.SaintAlfonzo.creativedevtools.test.TestLine;

import java.util.Map;
import java.util.TreeMap;

/**
 * Headless checks of /cdevtest's argument parser and output line (pure Java, no game). Run: bash run.sh (this folder).
 * Prints "PASS n" or the failing checks and exits 1.
 */
public class CdevtestHarness {
    static int pass, fail;

    static void eq(String what, Object got, Object want) {
        if (String.valueOf(got).equals(String.valueOf(want))) pass++;
        else {
            fail++;
            System.out.println("FAIL " + what + ": got <" + got + "> want <" + want + ">");
        }
    }

    static void bad(String what, Runnable r, String msgPart) {
        try {
            r.run();
            fail++;
            System.out.println("FAIL " + what + ": no error");
        } catch (TestArgs.Bad e) {
            if (e.getMessage().contains(msgPart)) pass++;
            else {
                fail++;
                System.out.println("FAIL " + what + ": message <" + e.getMessage() + ">");
            }
        }
    }

    public static void main(String[] a) {
        // positionals, options, flags
        TestArgs t = TestArgs.parse("1 -2 3 minecraft:wheat[age=7] dim=minecraft:the_nether keep hit=0.5,0.9,0.5", "keep");
        int[] p = t.xyz("pos");
        eq("xyz", p[0] + "," + p[1] + "," + p[2], "1,-2,3");
        eq("state with = inside brackets is positional", t.word("state"), "minecraft:wheat[age=7]");
        eq("dim", t.opt("dim", null), "minecraft:the_nether");
        eq("flag", t.flag("keep"), true);
        double[] h = t.optTriple("hit");
        eq("hit", h[0] + "," + h[1] + "," + h[2], "0.5,0.9,0.5");
        t.end();
        t.checkOpts("dim", "keep", "hit");
        bad("unknown option", () -> t.checkOpts("dim"), "unknown option: keep");
        // a bare word that is not a declared flag stays positional; end() refuses it
        TestArgs u = TestArgs.parse("5 keep");
        eq("int", u.integer("n"), 5);
        bad("extra positional", u::end, "unexpected argument: keep");
        bad("missing", () -> TestArgs.parse("1 2").xyz("pos"), "missing pos z");
        bad("not int", () -> TestArgs.parse("x").integer("n"), "n not an integer: x");
        bad("unclosed quote", () -> TestArgs.parse("a \"b c"), "unclosed quote");
        // quotes keep spaces; single-quoted SNBT passes through untouched
        TestArgs q = TestArgs.parse("\"a b\" held=minecraft:potion{Potion:'minecraft:water'}");
        eq("quoted", q.word("w"), "a b");
        eq("snbt", q.opt("held", null), "minecraft:potion{Potion:'minecraft:water'}");
        // tag ids and stacks with counts
        eq("tag positional", TestArgs.parse("#minecraft:logs").word("b"), "#minecraft:logs");
        eq("optInt default", TestArgs.parse("").optInt("n", 4, 1, 9), 4);
        bad("optInt range", () -> TestArgs.parse("n=0").optInt("n", 1, 1, 500), "out of range");
        eq("flag off", TestArgs.parse("keep=off", "keep").flag("keep"), false);
        eq("volume", TestArgs.volume(new int[]{0, 0, 0}, new int[]{-3, 1, 9}), 4 * 2 * 10);
        // output lines
        eq("line", new TestLine("count").kv("n", 3).kv("block", "#minecraft:logs").toString(),
                "[cdevtest] verb=count n=3 block=#minecraft:logs");
        eq("quote space", new TestLine("x").kv("msg", "no such bot").toString(), "[cdevtest] verb=x msg=\"no such bot\"");
        eq("quote eq and quotes", new TestLine("x").kv("s", "a=\"b\"").toString(), "[cdevtest] verb=x s=\"a=\\\"b\\\"\"");
        eq("empty", new TestLine("x").kv("s", "").toString(), "[cdevtest] verb=x s=\"\"");
        eq("double", new TestLine("x").kv("d", 0.30000000004).kv("w", 2.0).kv("f", 1.5f).toString(), "[cdevtest] verb=x d=0.3 w=2 f=1.5");
        eq("null", new TestLine("x").kv("n", null).toString(), "[cdevtest] verb=x n=null");
        Map<String, Integer> m = new TreeMap<>();
        eq("empty map", new TestLine("x").map("on", m).toString(), "[cdevtest] verb=x on=-");
        m.put("minecraft:dirt", 3);
        m.put("minecraft:farmland", 12);
        eq("map", new TestLine("x").map("on", m).toString(), "[cdevtest] verb=x on=minecraft:dirt:3,minecraft:farmland:12");
        eq("block state value", new TestLine("x").kv("state", "minecraft:wheat[age=7]").toString(), "[cdevtest] verb=x state=\"minecraft:wheat[age=7]\"");
        System.out.println(fail == 0 ? "PASS " + pass : "FAILED " + fail + " of " + (pass + fail));
        System.exit(fail == 0 ? 0 : 1);
    }
}
