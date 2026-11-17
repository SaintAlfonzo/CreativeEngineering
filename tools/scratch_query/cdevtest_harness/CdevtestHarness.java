import com.SaintAlfonzo.creativedevtools.test.GroundJudge;
import com.SaintAlfonzo.creativedevtools.test.GroundJudge.Kind;
import com.SaintAlfonzo.creativedevtools.test.TestArgs;
import com.SaintAlfonzo.creativedevtools.test.TestLine;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
        // GroundJudge (the under / floating / season verbs, moved from /zq 2026-11-16)
        // column: solid 64..70 over air 60..63, solid at 59: bottom 64, air first, gap 4, not deep
        GroundJudge.Under g = GroundJudge.under(60, 70, y -> y >= 64 || y == 59 ? Kind.SOLID : Kind.AIR);
        eq("under bottom", g.bottom(), 64);
        eq("under first", g.first(), Kind.AIR);
        eq("under gap", g.gap() + "/" + g.water() + "/" + g.deep(), "4/0/false");
        // bottom sits on solid: gap 0
        eq("under on ground", GroundJudge.under(60, 70, y -> y >= 50 ? Kind.SOLID : Kind.AIR).gap(), 0);
        // water 50..63 under a floor at 64: 14 deep, all water, deep
        GroundJudge.Under w = GroundJudge.under(64, 70, y -> y >= 64 || y < 50 ? Kind.SOLID : Kind.WATER);
        eq("under water", w.first() + "/" + w.gap() + "/" + w.water() + "/" + w.deep(), "WATER/14/14/true");
        // no solid in the box: not counted
        eq("under empty column", GroundJudge.under(60, 70, y -> Kind.AIR), null);
        // void below: gap stops at REACH
        eq("under reach", GroundJudge.under(0, 5, y -> y == 5 ? Kind.SOLID : Kind.AIR).gap(), GroundJudge.REACH);
        // ring: 1x1 box at 0,0 gives the 4x4 lattice -3..3 step 2 minus the inner -1,1 points
        List<int[]> ring = GroundJudge.ring(0, 0, 0, 0);
        StringBuilder rs = new StringBuilder(" ");
        for (int[] pt : ring) rs.append(pt[0]).append(',').append(pt[1]).append(' ');
        eq("ring size", ring.size(), 12);
        eq("ring skips inner", rs.toString().contains(" -1,-1 ") || rs.toString().contains(" 1,1 ") || !rs.toString().contains(" 3,3 "), false);
        Map<String, Integer> te = new TreeMap<>();
        te.put("b", 5);
        te.put("a", 5);
        te.put("c", 9);
        te.put("d", 1);
        eq("top order", GroundJudge.top(te, 3), "{c=9, a=5, b=5}");
        eq("foreign", GroundJudge.foreign(te, Set.of("a", "c")), "b,d");
        eq("foreign none", GroundJudge.foreign(te, te.keySet()), "-");
        eq("floats sand", GroundJudge.floats("minecraft:sand", "minecraft:cave_air"), true);
        eq("floats on stone", GroundJudge.floats("minecraft:gravel", "minecraft:stone"), false);
        eq("floats first y", GroundJudge.floats("minecraft:sand", null), false);
        eq("list", GroundJudge.list("minecraft:plains, minecraft:desert,,"), "[minecraft:plains, minecraft:desert]");
        bad("list empty", () -> GroundJudge.list(" , "), "missing ids");
        System.out.println(fail == 0 ? "PASS " + pass : "FAILED " + fail + " of " + (pass + fail));
        System.exit(fail == 0 ? 0 : 1);
    }
}
