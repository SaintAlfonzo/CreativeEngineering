import java.io.FileReader;
import java.util.List;

import com.SaintAlfonzo.creativeengineering.crops.CropGrowth;
import com.SaintAlfonzo.creativeengineering.crops.CropGrowth.Display;
import com.SaintAlfonzo.creativeengineering.crops.CropGrowth.Rule;
import com.SaintAlfonzo.creativeengineering.crops.CropGrowth.Stage;
import com.google.gson.JsonParser;

/** Headless checks for the WTHIT crop growth rules (tools/test_crop_growth.sh). args[0] = crop_growth.json. */
public class CropGrowthTest {
    static int failed, run;

    static void check(String what, boolean ok) {
        run++;
        if (!ok) {
            failed++;
            System.out.println("FAIL " + what);
        }
    }

    static void eval(String what, int v, int max, int harvest, int empty, Integer pct, Stage stage) {
        Display d = CropGrowth.evaluate(v, max, harvest, empty);
        if (pct == null) {
            check(what + " -> no line", d == null);
        } else {
            check(what + " -> " + pct + "% " + stage + " (got " + d + ")", d != null && d.percent() == pct && d.stage() == stage);
        }
    }

    static Rule firstMatch(List<Rule> rules, String id) {
        for (Rule r : rules) {
            if (r.matchesId(id)) {
                return r;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        // growth maths
        eval("age 0 of 4", 0, 4, 4, -1, 0, Stage.GROWING);
        eval("age 2 of 4", 2, 4, 4, -1, 50, Stage.GROWING);
        eval("age 3 of 4, no early harvest", 3, 4, 4, -1, 75, Stage.GROWING);
        eval("age 4 of 4", 4, 4, 4, -1, 100, Stage.MATURE);
        eval("salmonberry stage 2 (harvest 3)", 2, 4, 3, -1, 50, Stage.GROWING);
        eval("salmonberry stage 3 (harvest 3)", 3, 4, 3, -1, 75, Stage.HARVESTABLE);
        eval("salmonberry stage 4", 4, 4, 3, -1, 100, Stage.MATURE);
        eval("grape stem age 0 is empty", 0, 4, 4, 0, null, null);
        eval("grape stem age 1", 1, 4, 4, 0, 25, Stage.GROWING);
        eval("hemp top half (5 over max 4)", 5, 4, 4, -1, 100, Stage.MATURE);
        eval("1 of 3 rounds to 33", 1, 3, 3, -1, 33, Stage.GROWING);
        eval("2 of 3 rounds to 67 like WTHIT", 2, 3, 3, -1, 67, Stage.GROWING);
        eval("1 of 6 rounds to 17", 1, 6, 6, -1, 17, Stage.GROWING);
        eval("never 100 while growing", 399, 400, 400, -1, 99, Stage.GROWING);
        eval("max 0 is nothing", 1, 0, 0, -1, null, null);
        eval("negative value (unknown stage) clamps", -1, 4, 4, -2, 0, Stage.GROWING);

        // glob
        check("glob star", CropGrowth.glob("vinery:savanna_grape_bush_*").matcher("vinery:savanna_grape_bush_red").matches());
        check("glob not prefix of other block", !CropGrowth.glob("vinery:savanna_grape_bush_*").matcher("vinery:red_grape_bush").matches());
        check("glob literal dot", !CropGrowth.glob("a.b:c").matcher("axb:c").matches());
        check("glob exact", CropGrowth.glob("neapolitan:mint").matcher("neapolitan:mint").matches()
                && !CropGrowth.glob("neapolitan:mint").matcher("neapolitan:mint_tea").matches());

        // the shipped rule file
        List<Rule> rules = CropGrowth.parse(JsonParser.parseReader(new FileReader(args[0])).getAsJsonObject());
        check("rules parsed", rules.size() >= 10);
        Rule r = firstMatch(rules, "delightful:salmonberry_bush");
        check("salmonberry harvest 3", r != null && r.harvest() == 3);
        r = firstMatch(rules, "immersiveengineering:hemp");
        check("hemp: growth enum, 6 names, max 4", r != null && r.properties().equals(List.of("growth"))
                && r.stages().size() == 6 && r.max() == 4);
        r = firstMatch(rules, "the_dirty_stuff:tobacco_seeds");
        check("tobacco: blockstate property", r != null && r.properties().equals(List.of("blockstate")));
        r = firstMatch(rules, "farmersrespite:coffee_stem_double");
        check("coffee double stem sums age + age1", r != null && r.properties().equals(List.of("age", "age1")));
        r = firstMatch(rules, "fruitsdelight:durian_leaves");
        check("durian leaves use fruit, before the *_leaves glob", r != null && r.properties().equals(List.of("fruit")));
        r = firstMatch(rules, "fruitsdelight:apple_leaves");
        check("apple leaves use type, natural only", r != null && r.properties().equals(List.of("type"))
                && "false".equals(r.requires().get("persistent")));
        r = firstMatch(rules, "vinery:grapevine_stem");
        check("grapevine stem: age 0 empty", r != null && r.empty() == 0);
        r = firstMatch(rules, "vinery:grapevine_lattice");
        check("lattice: age 0 empty, harvest 3", r != null && r.empty() == 0 && r.harvest() == 3);
        r = firstMatch(rules, "vinery:taiga_grape_bush_white");
        check("taiga grapes harvest 2", r != null && r.harvest() == 2);
        r = firstMatch(rules, "vinery:jungle_grape_bush_red");
        check("jungle grapes harvest only when full", r != null && r.harvest() == -1);
        for (String id : List.of("neapolitan:strawberry_bush", "neapolitan:mint", "fruitsdelight:blueberry_bush",
                "fruitsdelight:cranberry_bush", "fruitsdelight:lemon_tree", "floralench:poppy_bush")) {
            check(id + " has a rule", firstMatch(rules, id) != null);
        }
        check("wheat has no rule (WTHIT covers it)", firstMatch(rules, "minecraft:wheat") == null);

        System.out.println((run - failed) + "/" + run + " checks passed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
