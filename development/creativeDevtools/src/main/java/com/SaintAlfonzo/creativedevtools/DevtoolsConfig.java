package com.SaintAlfonzo.creativedevtools;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Creative Devtools' config, {@code config/creativedevtools-common.toml} (feature_writeup/creative_devtools.md 1):
 * <ul>
 * <li>{@code [debug]}: moved whole from creativeengineering-common.toml, keys and comments unchanged (generators,
 * block-state sync, names, WTHIT icons, recipe scan, Level random probe).</li>
 * <li>{@code [worldgen.mass_place]}: /ceplace's knobs, moved from creativeengineering-common.toml under the same path.</li>
 * <li>{@code [features]}: one switch per Devtools script feature ({@link #FEATURE_ROWS}), read by
 * {@code global.CDV.feature(id)}.</li>
 * </ul>
 * COMMON configs load after mod construction; scripts read it inside event callbacks. Before the file is loaded every
 * value reads as its default.
 */
public final class DevtoolsConfig {

    /** Where /ceplace puts each structure vertically ({@code ceplace/MassPlacer}). */
    public enum VerticalAlign { AUTO, KEEP, GROUND }

    /** Script feature switches: {id, comment lines...}. */
    private static final List<String[]> FEATURE_ROWS = List.of(
            new String[]{"testRecipes", "The Create test and showcase recipes (create/: a mechanical-crafting grid per metal and per",
                    "wood, and one sequenced-assembly chain with every legal step). Server scripts re-read it on /reload."},
            new String[]{"flowerWorld", "The Flower Test world builder (flowerworld/flower_world.js): builds the table on the first start",
                    "of a Flower Test world, and /flowerworld."}
    );

    public static final ForgeConfigSpec SPEC;
    public static final Debug DEBUG;
    public static final MassPlace MASS_PLACE;
    private static final Map<String, ForgeConfigSpec.BooleanValue> FEATURES = new LinkedHashMap<>();

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        DEBUG = new Debug(b);
        b.comment("Worldgen test tools.").push("worldgen");
        MASS_PLACE = new MassPlace(b);
        b.pop(); // worldgen
        b.comment("Creative Devtools script features. Off: that feature's scripts do nothing.").push("features");
        for (String[] row : FEATURE_ROWS) {
            String[] comment = new String[row.length - 1];
            System.arraycopy(row, 1, comment, 0, comment.length);
            FEATURES.put(row[0], b.comment(comment).define(row[0], true));
        }
        b.pop(); // features
        SPEC = b.build();
    }

    private DevtoolsConfig() {}

    /** True if feature switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
    public static boolean feature(String id) {
        ForgeConfigSpec.BooleanValue v = FEATURES.get(id);
        if (v == null) return false;
        return !SPEC.isLoaded() || v.get();
    }

    /** {@code [debug.recipe_scan] runAtLoad} for the server script (dump/recipe_scan.js). */
    public static boolean recipeScanAtLoad() {
        return get(() -> DEBUG.recipeScanAtLoad.get(), false);
    }

    static <T> T get(Supplier<T> value, T fallback) {
        try {
            T v = value.get();
            return v == null ? fallback : v;
        } catch (RuntimeException | LinkageError e) {
            return fallback; // config not loaded yet
        }
    }

    // /ceplace readers (were WorldgenConfig.place*), with the code defaults before the file loads
    public static int placeGap() { return get(() -> MASS_PLACE.gap.get(), 12); }
    public static int placeRowGap() { return get(() -> MASS_PLACE.rowGap.get(), 16); }
    public static boolean placeLabels() { return get(() -> MASS_PLACE.labels.get(), true); }
    public static VerticalAlign placeVerticalAlign() { return get(() -> MASS_PLACE.verticalAlign.get(), VerticalAlign.AUTO); }
    public static int placeAutoAlignBand() { return get(() -> MASS_PLACE.autoAlignBand.get(), 8); }
    public static int placeMaxItems() { return get(() -> MASS_PLACE.maxItems.get(), 1000); }
    public static int placeTickBudgetMs() { return get(() -> MASS_PLACE.tickBudgetMs.get(), 40); }

    public static final class MassPlace {
        public final ForgeConfigSpec.IntValue gap;
        public final ForgeConfigSpec.IntValue rowGap;
        public final ForgeConfigSpec.BooleanValue labels;
        public final ForgeConfigSpec.EnumValue<VerticalAlign> verticalAlign;
        public final ForgeConfigSpec.IntValue autoAlignBand;
        public final ForgeConfigSpec.IntValue maxItems;
        public final ForgeConfigSpec.IntValue tickBudgetMs;

        private MassPlace(ForgeConfigSpec.Builder b) {
            b.comment(
                    "/ceplace structures <filter> and /ceplace templates <glob>: place every match in rows (one row per",
                    "type tag, group, mod or template folder), starting at your position and going +X, rows stacked +Z.",
                    "Meant for a flat world with Generate Structures off. /ceplace preview ... lists without placing."
            ).push("mass_place");
            gap = b.comment("Blocks between two structures in a row.").defineInRange("gap", 12, 0, 512);
            rowGap = b.comment("Blocks between rows (the aisle the labels stand in).").defineInRange("rowGap", 16, 4, 512);
            labels = b.comment("Put a sign with the id in front of each structure and at the start of each row.")
                    .define("labels", true);
            verticalAlign = b.comment(
                    "KEEP: each structure at the height it generated at here. GROUND: its bottom layer at ground level.",
                    "AUTO: GROUND only for structures that generated more than autoAlignBand blocks above or below the",
                    "ground (underground and sky structures); surface ones keep their height."
            ).defineEnum("verticalAlign", VerticalAlign.AUTO);
            autoAlignBand = b.comment("AUTO's tolerance, in blocks.").defineInRange("autoAlignBand", 8, 0, 256);
            maxItems = b.comment("Refuse a run with more matches than this (preview first, or narrow the filter).")
                    .defineInRange("maxItems", 1000, 1, 100000);
            tickBudgetMs = b.comment(
                    "Milliseconds of work per server tick. Each structure is laid out, then its chunks are generated and",
                    "built one at a time until the budget is used, so a big village takes several ticks instead of one",
                    "long one (a dedicated server kills a tick over 60 s). Higher = faster runs, choppier server."
            ).defineInRange("tickBudgetMs", 40, 5, 1000);
            b.pop(); // mass_place
        }
    }

    /** Dev-instance knobs: the Python generator runner ({@code generators/GeneratorRunner}, /cegen) and the probes and dumps. */
    public static final class Debug {
        public final ForgeConfigSpec.BooleanValue runGeneratorsAtStartup;
        public final ForgeConfigSpec.ConfigValue<String> generatorTarget;
        public final ForgeConfigSpec.ConfigValue<String> python;
        public final ForgeConfigSpec.IntValue timeoutSeconds;
        public final ForgeConfigSpec.IntValue startupWaitSeconds;
        public final ForgeConfigSpec.BooleanValue stateSyncEnabled;
        public final ForgeConfigSpec.IntValue stateSyncMaxDetailLines;
        public final ForgeConfigSpec.BooleanValue dumpNamesAtTitle;
        public final ForgeConfigSpec.BooleanValue dumpIconsAtTitle;
        public final ForgeConfigSpec.BooleanValue recipeScanAtLoad;
        public final ForgeConfigSpec.BooleanValue levelRandomProbe;

        private Debug(ForgeConfigSpec.Builder b) {
            b.comment("Developer tools. Only meaningful in the dev instance (the one with tools/ and mods/ source).").push("debug");
            b.comment(
                    "The pack's generator scripts (tools/*.py: structure/biome/loot configs, effect descriptions,",
                    "pack manifest, reports) listed in tools/generators.json. /cegen run [target] runs them in game;",
                    "these knobs also let them run by themselves every time the game starts."
            ).push("generators");

            runGeneratorsAtStartup = b.comment(
                    "Debug mode. When true, the generators run in the background as the game starts (from common",
                    "setup), and loading waits for them before finishing, so the first world load already reads",
                    "fresh files. Results go to latest.log, logs/cegen/<id>.log and /cegen status. Off by default."
            ).define("runAtStartup", false);

            generatorTarget = b.comment(
                    "What the startup run and a bare /cegen run execute: 'all', a group or one generator id",
                    "from tools/generators.json."
            ).define("target", "core");

            python = b.comment("Python interpreter to run the scripts with (a name on PATH or a full path).")
                    .define("python", "python3");

            timeoutSeconds = b.comment("A script still running after this long is killed and reported as TIMEOUT.")
                    .defineInRange("timeoutSeconds", 300, 10, 3600);

            startupWaitSeconds = b.comment(
                    "How long the end of loading waits for the startup run. The core generators take about 10 s and",
                    "run alongside the rest of loading, so this rarely waits at all. 0 = never wait (files may then",
                    "change after the first world has loaded)."
            ).defineInRange("startupWaitSeconds", 120, 0, 3600);

            b.pop(); // generators

            b.comment(
                    "Block-state sync diagnostics, for ghost blocks that only appear on a dedicated server (the client",
                    "throws 'Error executing task on Client' because a block-update packet names a block state it does",
                    "not have, and drops the rest of that packet). Turn it on on the CLIENT, and on the SERVER to get its",
                    "half of the comparison."
            ).push("block_state_sync");

            stateSyncEnabled = b.comment(
                    "Debug mode. Client: logs every block-update packet containing a state id the client cannot resolve",
                    "(the raw id, the block position, the client's state count) and writes",
                    "logs/ce_block_state_ranges_client.txt. Dedicated server: writes logs/ce_block_state_ranges_server.txt",
                    "when it starts. tools/diff_block_state_ranges.py compares the two files and names the block."
            ).define("enabled", false);

            stateSyncMaxDetailLines = b.comment(
                    "Per-packet detail lines to log before falling back to one summary line every 500 bad packets.",
                    "Each distinct bad id is always logged the first time it is seen."
            ).defineInRange("maxDetailLines", 200, 0, 100000);

            b.pop(); // block_state_sync

            b.comment("The names audit (feature_writeup/names.md, tools/name_audit.py).").push("names");
            dumpNamesAtTitle = b.comment(
                    "Client: write kubejs/exported/display_names.json (every block, item, effect... name and the pack",
                    "that set it) and names.json the first time the title screen opens, as /cedumpnames does in game.",
                    "For a scratch client that never joins a world. Off by default."
            ).define("dumpAtTitle", false);
            b.pop(); // names

            b.comment("The WTHIT icon audit (feature_writeup/wthit_lines.md, tools/wthit_icon_audit.py).").push("wthit");
            dumpIconsAtTitle = b.comment(
                    "Client: write kubejs/exported/wthit_icons.json (every block's WTHIT icon verdict and every item",
                    "with a missing model) the first time the title screen opens, as /cedumpicons does in game.",
                    "For a scratch client that never joins a world. Off by default."
            ).define("dumpIconsAtTitle", false);
            b.pop(); // wthit

            b.comment("The removed-item recipe scan (kubejs/data/creativedevtools/kubejs/dump/recipe_scan.js, /recipescan).").push("recipe_scan");
            recipeScanAtLoad = b.comment(
                    "Server: once per session, when the server has loaded (after every recipe change is applied), log",
                    "each recipe that still uses or makes a removed item ([jeiRemove] lines). /recipescan runs it on",
                    "demand whatever this says. Off by default."
            ).define("runAtLoad", false);
            b.pop(); // recipe_scan

            b.comment("Finds the off-thread caller of Level.random (feature_writeup/fixes_compat.md F4).").push("level_random");
            levelRandomProbe = b.comment(
                    "Debug mode. When true, a Level's shared random logs the stack trace of every access from a thread other",
                    "than the one that created the Level ([LevelRandomProbe] lines in latest.log, at most 20 distinct",
                    "callers per session). Read when the world loads: restart to apply. Off by default."
            ).define("probe", false);
            b.pop(); // level_random
            b.pop(); // debug
        }
    }
}
