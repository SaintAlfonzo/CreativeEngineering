package com.SaintAlfonzo.creativecommon;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Central Forge-native config spec for {@code creativecommon}, backing
 * {@code config/creativecommon-common.toml}. Unlike the KubeJS {@code global.config}
 * JSON framework, values defined here show up in Create's in-game Configs screen
 * (reachable from the pause menu), are editable live, and hot-reload without a restart.
 *
 * <p>Registered once, in {@link CreativeCommon}'s constructor - registration must
 * happen during mod construction, since Forge loads every mod's COMMON/CLIENT config
 * immediately after construction finishes for every mod (any later, e.g. from
 * {@code FMLCommonSetupEvent}, is silently too late and the file is never read).
 */
public final class CommonConfig {

    private CommonConfig() {}

    /** The toml tables of this file, carried over from creativeengineering-common.toml on the first boot after the swap. */
    public static final List<String> SECTIONS = List.of("armor_overflow", "jei", "gui_title", "toasts", "tooltips", "changelog", "damage_tags", "coaster", "wthit", "perf", "wandering_trader", "advancements", "mod_names");

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ArmorOverflow ARMOR_OVERFLOW;
    public static final Jei JEI;
    public static final GuiTitle GUI_TITLE;
    public static final Toasts TOASTS;
    public static final Tooltips TOOLTIPS;
    public static final Changelog CHANGELOG;
    public static final DamageTagConfig DAMAGE_TAGS;
    public static final Coaster COASTER;
    public static final Wthit WTHIT;
    public static final Perf PERF;
    public static final WanderingTrader WANDERING_TRADER;
    public static final Advancements ADVANCEMENTS;
    public static final ModNames MOD_NAMES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        // [forgiving_void], [leaf_decay], [raids], [worldgen], [named_mobs] and the [perf] mob cap keys:
        // creativeworldgen-common.toml (Creative Worldgen, 2026-11-02)
        // [botania]: creativemagic-common.toml (Creative Magic, 2026-11-04)
        // [shrapnel], [surface], [vein_undo]: creativeengineering-common.toml (Creative Engineering, 2026-11-04)
        ARMOR_OVERFLOW = new ArmorOverflow(builder);
        JEI = new Jei(builder);
        GUI_TITLE = new GuiTitle(builder);
        TOASTS = new Toasts(builder);
        TOOLTIPS = new Tooltips(builder);
        CHANGELOG = new Changelog(builder);
        DAMAGE_TAGS = new DamageTagConfig(builder);
        // [leaf_piles], [moss], [vine_shearing], [campfire], [monster_box], [trash_slot]: creativecompat-common.toml
        // (Creative Compat, 2026-11-01)
        COASTER = new Coaster(builder);
        WTHIT = new Wthit(builder);
        PERF = new Perf(builder);
        // [seasons], [crop_seasons], [time]: creativecalendar-common.toml (Creative Calendar, 2026-11-01)
        WANDERING_TRADER = new WanderingTrader(builder);
        ADVANCEMENTS = new Advancements(builder);
        MOD_NAMES = new ModNames(builder);
        // [dragon_gear], [air_brake], [stepstool], [crafting], [armor_enchantability], [shields], [bonemeal_charm],
        // [bottled_air], [backpackSkins]: creativeequipment-common.toml (Creative Equipment, 2026-11-04)
        COMMON_SPEC = builder.build();
    }

    /**
     * Throttles for third-party mod code that the server profile (feature_writeup/perf.md section 7) found
     * hot. Each one is a mixin in {@code mixin/} that skips or spaces out work the mod does every tick;
     * the comments say what is traded away. Read live, no rejoin needed.
     */
    public static final class Perf {
        public final ForgeConfigSpec.BooleanValue gravityLimitDimensions;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> gravityDimensions;
        public final ForgeConfigSpec.IntValue mountEffectIntervalTicks;

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            // troughMissCooldownTicks: creativedelight-common.toml [perf] (Creative Delight, 2026-11-01)

            // seasonsMeltIntervalTicks: creativecalendar-common.toml [perf] (Creative Calendar, 2026-11-01)

            // mobCapLevels, mobCapForceLevel, mobCapLevel1/2Chunks, mobCapHysteresisChunks, mobCapLevel1/2Monster,
            // mobCapScaledCategories, spawnChunksLevel1/2, spawnBandLevel1/2, spawnBandCategories:
            // creativeworldgen-common.toml [perf] (Creative Worldgen, 2026-11-02)

            gravityLimitDimensions = b.comment(
                    "Create: Gravity runs its biome lookup and gravity-attribute update for every living entity in",
                    "every dimension on every tick. On: only entities in the dimensions below run it (entities that",
                    "still carry the low-gravity modifier from one run it once more to drop it). Off: the mod's own",
                    "behaviour."
            ).define("gravityLimitDimensions", true);
            gravityDimensions = b.comment(
                    "Dimensions Create: Gravity is allowed to run in. The pack's biome_configs lists only",
                    "#minecraft:is_end, so the End is enough. Add a dimension here if you add a gravity biome",
                    "outside the End."
            ).defineList("gravityDimensions", java.util.List.of("minecraft:the_end"), o -> o instanceof String);

            mountEffectIntervalTicks = b.comment(
                    "Ticks between passes that apply a worn horse armor's effects (dragon horse armor: Regeneration).",
                    "The pass walks every entity in the level, which cost too much every tick. Effects last",
                    "this + 40 ticks, so they stay on. A newly equipped armor starts working within one interval."
            ).defineInRange("mountEffectIntervalTicks", 100, 1, 1200);

            b.pop(); // perf
        }
    }

    // [kitchen] (counter-top offsets) moved to Creative Delight's blockheadsAdditions config (2026-10-31).

    /**
     * Brewin' and Chewin' coaster (the port) extras: {@code compat.WthitCoaster}. Which items go on a coaster is the
     * port's own {@code [coaster] taggedItemsOnly} in brewinandchewin-common.toml; feature_writeup/alcohol.md 12.
     */
    public static final class Coaster {
        public final ForgeConfigSpec.BooleanValue wthit;

        private Coaster(ForgeConfigSpec.Builder b) {
            b.comment("Brewin' and Chewin' coasters (client side).").push("coaster");

            wthit = b.comment(
                    "WTHIT lines on a coaster: 'Coaster: 3 / 4' ('(put away)' once the coaster itself was taken",
                    "back) and one line per item on it, in the order they went on."
            ).define("wthit", true);

            b.pop(); // coaster
        }
    }

    /**
     * The pack's WTHIT lines that have no feature section of their own (client side, display only):
     * {@code compat.WthitBlockModelIcon}, {@code compat.WthitHolderContents}, {@code compat.WthitHive},
     * {@code compat.WthitStateLines}, {@code compat.WthitCampfire}, {@code compat.WthitSheetmetalTank},
     * {@code compat.WthitKitchenStation}, {@code compat.WthitSheared}, {@code compat.WthitSilkTouch},
     * {@code compat.WthitCropSeasons}, {@code compat.WthitOperatorBlocks}. Every change to WTHIT's overlay has a switch here.
     * feature_writeup/wthit_lines.md. Each is also a WTHIT plugin toggle (config/waila/plugin_toggle.json5).
     */
    public static final class Wthit {
        public final ForgeConfigSpec.BooleanValue iconBlockModel;
        public final ForgeConfigSpec.BooleanValue iconForgePick;
        public final ForgeConfigSpec.BooleanValue iconSubstitutes;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> iconExclude;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> iconItems;
        public final ForgeConfigSpec.BooleanValue holderContents;
        public final ForgeConfigSpec.BooleanValue potContents;
        public final ForgeConfigSpec.BooleanValue holderPotionEffects;
        public final ForgeConfigSpec.BooleanValue holderSingleName;
        public final ForgeConfigSpec.BooleanValue hiveBees;
        public final ForgeConfigSpec.BooleanValue hiveBeesOnTop;
        public final ForgeConfigSpec.BooleanValue hiveHoneyMax;
        public final ForgeConfigSpec.BooleanValue stateLines;
        public final Map<String, ForgeConfigSpec.BooleanValue> stateLine = new java.util.LinkedHashMap<>();
        public final ForgeConfigSpec.BooleanValue campfireBurn;
        public final ForgeConfigSpec.BooleanValue campfireRain;
        public final ForgeConfigSpec.BooleanValue sheetmetalTank;
        public final ForgeConfigSpec.BooleanValue kitchenStation;
        public final ForgeConfigSpec.BooleanValue remainderBuffer;
        public final ForgeConfigSpec.BooleanValue sheared;
        public final ForgeConfigSpec.BooleanValue silkTouch;
        public final ForgeConfigSpec.BooleanValue silkTouchShowFalse;
        public final ForgeConfigSpec.BooleanValue operatorBlocks;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> operatorBlockList;
        public final ForgeConfigSpec.IntValue operatorBlockLevel;

        private Wthit(ForgeConfigSpec.Builder b) {
            b.comment("WTHIT look-at overlay lines (client side). See feature_writeup/wthit_lines.md.").push("wthit");

            iconBlockModel = b.comment(
                    "Block model icons: a block with no item (or whose item draws as the purple/black missing model)",
                    "shows its own block model in WTHIT's icon slot. Off = WTHIT's empty icon, Display Delight included."
            ).define("iconBlockModel", true);

            iconForgePick = b.comment(
                    "Block model icons: before drawing the model, ask Forge's hit-aware pick block (what middle-click",
                    "gives), so a block holding several things shows the part looked at (Supplementaries' skull pile)."
            ).define("iconForgePick", true);

            iconSubstitutes = b.comment("Block model icons: use the iconItems list below.").define("iconSubstitutes", true);

            iconExclude = b.comment(
                    "Block model icons: when the looked-at block has no item, or its item draws as the purple/black",
                    "missing model, WTHIT's icon slot shows the block's own model instead. Blocks listed here keep",
                    "WTHIT's normal icon. Entries: a namespace ('create') or a block id ('create:belt').",
                    "/cedumpicons + tools/wthit_icon_audit.py list every block the icon applies to."
            ).defineListAllowEmpty(List.of("iconExclude"), () -> List.of(), o -> o instanceof String s && !s.isBlank());

            iconItems = b.comment(
                    "Block model icons: an item to show for a block that has none and draws no model of its own",
                    "(block-entity rendered). Entries 'block=item'. D&D's powered flywheel is Create's flywheel."
            ).defineListAllowEmpty(List.of("iconItems"), () -> List.of("create_dd:powered_flywheel=create:flywheel"),
                    o -> o instanceof String s && s.contains("="));

            holderContents = b.comment(
                    "Supplementaries jars, goblets and cages: 'Contents: Honey (6 / 12)' (fluid in bottles, with the",
                    "jar's capacity; a goblet just 'Water'), 'Contents: Cookie (5 / 12)', or the mob inside."
            ).define("holderContents", true);

            potContents = b.comment(
                    "Flower pots, flower boxes and planter boxes: 'Contains: Poppy' (every plant inside, a box's plants",
                    "in slot order, '2 Poppy' for a double); a Twigs silt pot 'Rooted Dirt: Yes / No'. A Beautify hanging",
                    "pot standing on the ground is named 'Rich Pot'."
            ).define("potContents", true);

            holderPotionEffects = b.comment(
                    "Under a potion in a jar or goblet, one line per effect with its strength and length, as the",
                    "potion's own tooltip shows them ('Instant Health II', 'Regeneration (0:45)')."
            ).define("holderPotionEffects", true);

            holderSingleName = b.comment(
                    "A one-bottle holder (goblet) shows just the fluid ('Contents: Water'). Off = 'Water (1 / 1)'."
            ).define("holderSingleName", true);

            hiveBees = b.comment(
                    "Beehives and bee nests: 'Bees: 2 / 3'. The count comes from the server (WTHIT and the pack's",
                    "mod on both sides); without it the line is left out."
            ).define("hiveBees", true);

            hiveBeesOnTop = b.comment(
                    "The Bees line takes the place of WTHIT's occupant-names line ('2 Bee') and sits above Honey Level.",
                    "Off = WTHIT's names line stays and Bees goes at the end."
            ).define("hiveBeesOnTop", true);

            hiveHoneyMax = b.comment(
                    "Beehives and bee nests: WTHIT's 'Honey Level: 3' becomes 'Honey Level: 3 / 5'. Still follows",
                    "WTHIT's own 'Show Beehive Honey Level' option."
            ).define("hiveHoneyMax", true);

            stateLines = b.comment(
                    "Numbers kept in block state: 'Servings: 3 / 4' (feasts), 'Slices: 5 / 7' (cakes, pies), 'Eggs' and",
                    "'Hatching' (nests, turtle eggs), 'Level' (cauldrons WTHIT has no fluid bar for), anchor charges,",
                    "sea pickles. Rules in assets/creativecommon/state_lines.json (override from kubejs/assets)."
            ).define("stateLines", true);
            b.comment("One switch per state line (a label added by an overriding state_lines.json is always on).").push("state_lines");
            for (String[] line : new String[][] {
                    {"servings", "'Servings: 3 / 4' (feasts, Rustic Delight pancakes)"},
                    {"slices", "'Slices: 5 / 7' (cakes, pies, Brewin' cheese wheels and pizza)"},
                    {"eggs", "'Eggs: 3 / 6' (nests, turtle eggs)"},
                    {"hatch", "'Hatching: 1 / 2' (turtle, crocodile, terrapin eggs)"},
                    {"level", "'Level: 2 / 3' (cauldrons)"},
                    {"charges", "'Charges: 2 / 4' (respawn anchor)"},
                    {"pickles", "'Pickles: 3 / 4' (sea pickles)"}}) {
                stateLine.put(line[0], b.comment(line[1]).define(line[0], true));
            }
            b.pop(); // state_lines

            campfireBurn = b.comment(
                    "Lit campfires: Unlit Campfire's burn line ('Burns forever' / 'Burn time: 42s'). Server data."
            ).define("campfireBurn", true);

            campfireRain = b.comment(
                    "Lit plain campfires: the pack's rain rule ('Rain: Puts it out', 'Going out in 6s', 'Safe (fuel",
                    "block below)'). Server data."
            ).define("campfireRain", true);

            sheetmetalTank = b.comment(
                    "IE sheetmetal tanks: the tank's fluid bar on every part, not only the two fluid ports. Server side."
            ).define("sheetmetalTank", true);

            kitchenStation = b.comment(
                    "Applied Cooking's ME Kitchen Station: online / offline and the security terminal it is tied to."
            ).define("kitchenStation", true);

            remainderBuffer = b.comment(
                    "FD cooking pot, Farmer's Respite kettle and oven: 'Holding: 3 Glass Bottle, 1 Bucket' for the two",
                    "leftover slots, and 'Waiting: leftover slots full' while a craft waits for room ([cooking] stallWhenFull)."
            ).define("remainderBuffer", true);

            sheared = b.comment(
                    "Grass-like blocks cut with shears (fertile=false: grass, mycelium, lush grass, grass slabs):",
                    "'Sheared, prevents spreading and converting to dirt'. Sheared vines ([vine_shearing]) and Vinery's",
                    "sterilized jungle grape vine: 'Sheared, stops growing'. Weeping, twisting and cave vine and kelp",
                    "tips and Immersive Weathering ivy at their last age: 'Stopped growing'."
            ).define("sheared", true);

            silkTouch = b.comment(
                    "'Silk Touch Required' line: True (green) when nothing drops without Silk Touch, Drops Differently",
                    "(yellow) when something else drops (block tags creativecommon:wthit/needs_silk_touch and",
                    "wthit/silk_touch_differs, from tools/gen_silk_touch_blocks.py). Hidden while the main-hand tool has Silk Touch."
            ).define("silkTouch", true);
            silkTouchShowFalse = b.comment(
                    "Also show 'Silk Touch Required: False' (red) on every other block. Off by default."
            ).define("silkTouchShowFalse", false);

            // cropSeasons, cropOutOfSeason: creativecalendar-common.toml [wthit] (Creative Calendar, 2026-11-01)

            operatorBlocks = b.comment(
                    "WTHIT's blacklist hides barriers, structure voids and light blocks from everyone. With this on, players",
                    "with operatorBlockLevel see the blocks in operatorBlockList; everyone else still sees through them."
            ).define("operatorBlocks", true);

            operatorBlockList = b.comment("Blocks shown to operators only (block ids).")
                    .defineListAllowEmpty(List.of("operatorBlockList"),
                            () -> List.of("minecraft:barrier", "minecraft:structure_void", "minecraft:light"),
                            o -> o instanceof String s && !s.isBlank());

            operatorBlockLevel = b.comment(
                    "Permission level that sees them: 2 = operators who can use /give and command blocks (also single",
                    "player with cheats on), 4 = server owners only, 0 = every player."
            ).defineInRange("operatorBlockLevel", 2, 0, 4);

            b.pop(); // wthit
        }
    }

    // Season types ([seasons]), crop seasons ([crop_seasons]) and the [time] master: Creative Calendar's
    // CalendarConfig (creativecalendar-common.toml), moved with their keys and comments on 2026-11-01.

    /**
     * Dials for {@code compat.ArmorOverflowRenderer} - Detail Armor Bar (detailab) only ever
     * draws one row of 10 armor-point icons; this instead draws full extra rows of the same
     * textured/recolored icons stacked above the first row. Detailab's own fallback for overflow
     * - tiny 3px tick marks to the left of the bar - is permanently suppressed by
     * {@code mixin.ArmorBarRendererOverflowTickMixin} regardless of this section's settings, since
     * it visually conflicts with real rows and has no purpose once this feature exists.
     */
    public static final class ArmorOverflow {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue compact;
        public final ForgeConfigSpec.IntValue compactRowSpacing;

        private ArmorOverflow(ForgeConfigSpec.Builder b) {
            b.comment(
                    "No detailab-compatible armor overflow mod exists, so this adds one: extra rows",
                    "of the same registered armor-bar icons/colors stacked above the normal 10-icon row,",
                    "once a player has more than 20 armor points. Requires detailab; no-ops without it."
            ).push("armor_overflow");

            enabled = b.comment("Master on/off switch. When false, overflow past 20 armor points isn't shown at all.")
                    .define("enabled", true);

            compact = b.comment(
                    "false (default): overflow rows are spaced a full icon height apart, like separate rows.",
                    "true: rows are packed tighter (compactRowSpacing px apart) and tucked mostly behind",
                    "the row below - the row closer to the hotbar is drawn on top of the one above it."
            ).define("compact", false);

            compactRowSpacing = b.comment("Vertical pixels between overflow rows when compact is true (icons are 9px tall).")
                    .defineInRange("compactRowSpacing", 4, 1, 9);

            b.pop(); // armor_overflow
        }
    }

    /**
     * Dials for {@code compat.jei.CraftableFirstRecipeLayoutList}, a backport of JEI 19's
     * "Show craftable recipes first" sorting to JEI 11.8.1 (port notes:
     * feature_writeup/jei_craftable_first_port.md), and for {@code compat.jei.PackIngredientOrder}, the pack's
     * order of the ingredient list (feature_writeup/jei_ordering.md). Client-side only; a server ignores it.
     */
    public static final class Jei {
        public final ForgeConfigSpec.BooleanValue craftableFirst;
        public final ForgeConfigSpec.BooleanValue packOrder;
        public final ForgeConfigSpec.BooleanValue orderReport;
        public final ForgeConfigSpec.BooleanValue seasoningCategory;
        public final ForgeConfigSpec.BooleanValue potionBucketCategory;
        public final ForgeConfigSpec.BooleanValue tippedArrowCategory;
        public final ForgeConfigSpec.BooleanValue canCategories;
        public final ForgeConfigSpec.EnumValue<FluidScale> fluidAmountScale;

        /** How JEI tanks turn a fluid amount into a fill height ({@code mixin.JeiFluidTankScaleMixin}). */
        public enum FluidScale { LOG, SQRT, LINEAR }

        private Jei(ForgeConfigSpec.Builder b) {
            b.comment("JEI recipe screen tweaks (client side).").push("jei");

            craftableFirst = b.comment(
                    "When the recipe screen is opened over a screen that can take recipe transfers (crafting table,",
                    "inventory, modded crafting GUIs), list the recipes whose + button is enabled first. The rest follow,",
                    "most-complete first (the share of inputs you already have). Other screens keep JEI's own order.",
                    "Takes effect the next time a recipe lookup opens."
            ).define("craftableFirst", true);

            packOrder = b.comment(
                    "Order JEI's ingredient list by the pack's definition (kubejs/startup_scripts/jei/order.js) and its",
                    "creativecommon:jei/... tags: by type (items, multiblocks, fluids, effects), then category, then",
                    "each category's own order. Ingredients with no place keep JEI's own order ([sorting] in",
                    "config/jei/jei-client.ini) after the placed ones of their type. Off: JEI's own order only.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("packOrder", true);

            orderReport = b.comment(
                    "Write kubejs/exported/jei_order.json at every JEI start: every item, fluid, effect and multiblock with",
                    "its place in the pack order and whether JEI shows it (shown / hidden by the pack / hidden by a mod /",
                    "hidden in JEI's edit mode / never in JEI), plus problems in the definition. Render it with",
                    "python3 tools/jei_order_report.py. Off by default: it takes a moment and a few MB."
            ).define("orderReport", false);

            seasoningCategory = b.comment(
                    "Show seasoning crafts (Salt's food + salt, Create: The Salt's salting mixes, and crafting recipes",
                    "with ids creativeengineering:shapeless/season/...) on their own Seasoning page instead of the",
                    "Crafting and Mixing pages. Off: they stay where their mods put them.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("seasoningCategory", true);

            potionBucketCategory = b.comment(
                    "Show the potion bucket recipes on one Potion Buckets page per potion and bucket: filling and",
                    "draining by hand, the Spout, the Item Drain and the Bottling Machine together. Off: they stay on the",
                    "Crafting, Spout Filling, Item Draining and Bottling Machine pages.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("potionBucketCategory", true);

            tippedArrowCategory = b.comment(
                    "Show tipped arrow recipes on one Tipped Arrows page per potion: by hand (8 arrows + a lingering",
                    "potion), the Spout and the Bottling Machine together. Off: they stay on the Crafting, Spout Filling",
                    "and Bottling Machine pages.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("tippedArrowCategory", true);

            canCategories = b.comment(
                    "Show can crafts on two pages of their own: Canning (food + an empty can -> a can) and Using Cans",
                    "(crafts that take a can). Off: they stay on the Crafting page.",
                    "Takes effect the next time JEI starts (rejoin or /reload)."
            ).define("canCategories", true);

            fluidAmountScale = b.comment(
                    "How full a fluid looks in JEI tanks (every mod's recipe pages and the pack's own) for its amount.",
                    "LOG: small amounts stay visible and 250, 1000 and 8000 mB look different. SQRT: a milder curve.",
                    "LINEAR: JEI's own (fill = amount / tank size, so small amounts are a sliver).",
                    "Takes effect at once."
            ).defineEnum("fluidAmountScale", FluidScale.LOG);

            b.pop(); // jei
        }
    }

    /**
     * Dials for {@code compat.guititle.TitlePlaques}: stretches a GUI resource pack's title plaque
     * (Engineers GUIs) to fit long container titles. Client-side only; a server ignores it.
     */
    public static final class GuiTitle {
        public final ForgeConfigSpec.BooleanValue stretchPlaque;
        public final ForgeConfigSpec.BooleanValue logOverflow;

        private GuiTitle(ForgeConfigSpec.Builder b) {
            b.comment("Container title plaques from GUI resource packs (client side).").push("gui_title");

            stretchPlaque = b.comment(
                    "When a container title is wider than the plaque the resource pack painted behind it, stretch the",
                    "plaque to fit. Titles wider than the whole GUI are cut short with \"...\". Only screens that draw",
                    "their title the vanilla way (chests, barrels, shulker boxes, hoppers, horses, dispensers, furnaces)."
            ).define("stretchPlaque", true);

            logOverflow = b.comment(
                    "Record every title that needed a stretch in kubejs/exported/gui_title_overflow.json",
                    "(screen class, texture, title and widths; kept across sessions)."
            ).define("logOverflow", true);

            b.pop(); // gui_title
        }
    }

    /**
     * Dials for {@code mixin/ToastFilterMixin}: drops whole families of toasts before they reach the
     * toast queue. Client-side only; a server ignores it. Covers every player, unlike Rubidium Extra's
     * own toggles (config/sodium-extra-options.json is a player file the deploy doesn't ship).
     */
    public static final class Toasts {
        public final ForgeConfigSpec.BooleanValue tutorial;
        public final ForgeConfigSpec.BooleanValue advancement;

        private Toasts(ForgeConfigSpec.Builder b) {
            b.comment("Pop-up toasts in the top-right corner (client side).").push("toasts");

            tutorial = b.comment(
                    "Show tutorial toasts: vanilla's (move, find a tree, punch, craft planks, open inventory,",
                    "social interactions, bundles), Exposure's camera/lightroom hints and Fairy Lights' Clippy",
                    "balloons. Takes effect for the next toast."
            ).define("tutorial", false);

            advancement = b.comment(
                    "Show advancement toasts (vanilla's \"Advancement Made!\" and Patchouli's book-unlocked toast).",
                    "The pack loads no advancements ([advancements] enabled), so this only matters if that is on."
            ).define("advancement", false);

            b.pop(); // toasts
        }
    }

    /**
     * Item tooltip dials (client side; a server ignores them): {@code mixin/ScreenTooltipTopMixin} and
     * {@code compat/CuriosSlotTooltip} (feature_writeup/tooltips.md 3).
     */
    public static final class Tooltips {
        public final ForgeConfigSpec.BooleanValue keepTopOnScreen;

        private Tooltips(ForgeConfigSpec.Builder b) {
            b.comment("Item tooltips (client side).").push("tooltips");

            keepTopOnScreen = b.comment(
                    "A tooltip taller than the screen starts at the top and runs off the bottom (vanilla keeps the",
                    "bottom on screen and cuts off the name). Takes effect on the next frame."
            ).define("keepTopOnScreen", true);

            b.pop(); // tooltips
        }
    }

    /** Player changelog, "What's New" ({@code changelog/} package, feature_writeup/changelog.md). */
    public static final class Changelog {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue titleButton;
        public final ForgeConfigSpec.BooleanValue pauseButton;
        public final ForgeConfigSpec.BooleanValue updatePopup;

        private Changelog(ForgeConfigSpec.Builder b) {
            b.comment("Player changelog, \"What's New\" (client side). The notes are the pack's",
                    "assets/creativecommon/changelog/<version>.md files. Takes effect the next time a menu opens."
            ).push("changelog");

            enabled = b.comment("Master switch: off removes the book buttons and the update popup.")
                    .define("enabled", true);
            titleButton = b.comment("Book button on the title screen, right of Singleplayer.")
                    .define("titleButton", true);
            pauseButton = b.comment("Book button on the pause menu, right of Back to Game.")
                    .define("pauseButton", true);
            updatePopup = b.comment(
                    "Speech bubble beside the book button while the newest version's notes are unread. Opening the",
                    "changelog once acknowledges it (config/creativecommon/changelog_seen.txt)."
            ).define("updatePopup", true);

            b.pop(); // changelog
        }
    }

    /** Debug knob for the damage-tag layer ({@code damage/} package). */
    public static final class DamageTagConfig {
        public final ForgeConfigSpec.BooleanValue logSeenSources;

        private DamageTagConfig(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Damage tags classify every hit (fire, magic, electric, ...) so shields - and later armour -",
                    "can treat kinds of damage differently. Every tag is a data/creativecommon/damage_tags/",
                    "<name>.json file, merged across datapacks and reloaded by /reload: most list message ids",
                    "directly (fire.json, magic.json, ... - these mirror vanilla's own damage flags, but as a",
                    "visible, editable file instead of a hidden check), and a couple are pure META tags that",
                    "only #include other tags (elemental.json = fire+cold+electric+chemical; physical.json =",
                    "melee+machinery+piercing+firearm+explosion+projectile). Shields read only these tags -",
                    "never a raw damage flag - so reclassifying a source, or moving a whole kind of damage",
                    "between shield columns, is always a data edit here, never Java."
            ).push("damage_tags");

            logSeenSources = b.comment(
                    "Discovery aid. When true, the first time each distinct damage source (message id + flags)",
                    "hurts anything on the server it is logged once as '[damage-tags] seen msgId=...'.",
                    "'tags=NONE' means no damage_tags file claims that source yet. Off by default.",
                    "Live: takes effect immediately."
            ).define("logSeenSources", false);

            b.pop(); // damage_tags
        }
    }

    /**
     * Dials for {@code trader.WanderingTraderTweaks} (Wandering Trader wait and offer counts, through
     * {@code mixin.WanderingTraderSpawnerMixin} / {@code mixin.WanderingTraderTradesMixin}). The Wandering
     * Winemaker's spawner is the Vinery port's own (vinery-common.toml [trader]). What the pools hold is
     * set in KubeJS ({@code kubejs/server_scripts/economy/wandering_trades.js}).
     */
    public static final class WanderingTrader {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue traderIntervalTicks;
        public final ForgeConfigSpec.IntValue commonTrades;
        public final ForgeConfigSpec.IntValue rareTrades;

        private WanderingTrader(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Wandering Trader spawning and offer counts (the Wandering Winemaker: vinery-common.toml [trader]).",
                    "The spawner rolls once per interval, counted in server ticks (20 = 1 second) while the",
                    "overworld runs with doMobSpawning on. Day length (Better Days), sleeping and /time do not",
                    "change it. A roll is vanilla's: 25/50/75% chance (rising until a spawn), then 1 in 10, then",
                    "a free spot near a random player; on average about 14 rolls per spawn.",
                    "/gamerule doTraderSpawning false stops it and the Winemaker."
            ).push("wandering_trader");

            enabled = b.comment("Off = vanilla Wandering Trader (24000-tick interval, 5 + 1 offers).")
                    .define("enabled", true);

            traderIntervalTicks = b.comment(
                    "Ticks between Wandering Trader spawn rolls. Vanilla: 24000 (20 minutes). 96000 = 80 minutes.",
                    "A change takes effect after the wait already counting down."
            ).defineInRange("traderIntervalTicks", 96000, 1200, 100_000_000);

            commonTrades = b.comment(
                    "Offers drawn from the common pool. Vanilla: 5. Each offer is a different listing; a pool",
                    "smaller than this gives every listing once."
            ).defineInRange("commonTrades", 25, 0, 200);

            rareTrades = b.comment("Offers drawn from the rare pool, all different items. Vanilla: 1.")
                    .defineInRange("rareTrades", 5, 1, 50);

            b.pop(); // wandering_trader
        }
    }

    /** {@code mixin/ServerAdvancementManagerMixin}: the pack has no advancements at all (user, 2026-10-18). */
    public static final class Advancements {
        public final ForgeConfigSpec.BooleanValue enabled;

        private Advancements(ForgeConfigSpec.Builder b) {
            b.comment("Advancements (vanilla, every mod, every datapack).").push("advancements");
            enabled = b.comment(
                    "false (the pack's setting): none are loaded, so none show, toast or give rewards.",
                    "Read when data loads: a change applies on the next /reload or world start."
            ).define("enabled", false);
            b.pop();
        }
    }

    /** Mod display names ({@code util.ModDisplayNames}): what JEI, WTHIT and the Mods screen call a mod. */
    public static final class ModNames {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;

        private ModNames(ForgeConfigSpec.Builder b) {
            b.comment("Mod display names (client side: JEI's mod line and @ search, WTHIT, the Mods screen).").push("mod_names");

            overrides = b.comment(
                    "\"modid=Name\" entries replacing the name from the mod's mods.toml. Applied at startup and when this",
                    "file is saved; JEI and WTHIT keep the name they read first, so a change shows after a restart.",
                    "Removing an entry gives the mod its own name back. The mod id stays the same (JEI @<modid> still works).",
                    "Pack style (feature_writeup/names.md 12): words spaced, possessive apostrophes, '&' for and / 'n',",
                    "Title Case, Create addons as \"Create: <name>\", no author handles, the pack's own mods end in (Alfonzo)."
            ).defineList("overrides", List.of(
                    "abnormals_delight=Abnormals' Delight",
                    "apexcore=Apex Core",
                    "appleskin=Apple Skin",
                    "astikorcarts=Astikor Carts",
                    "autoreglib=Auto Reg Lib",
                    "baby_mobs=Baby Mobs (Alfonzo)",
                    "betterdays=Better Days",
                    "betterf3=Better F3",
                    "boatview360=Boat View 360",
                    "bobberdetector=Create: Bobber Detector",
                    "brewinandchewin=Brewin' & Chewin' Backport (Alfonzo)",
                    "canned_goods=Canned Goods (Creative Delight)",
                    "cb_microblock=CB Microblock",
                    "cb_multipart=CB Multipart",
                    "cb_multipart_minecraft=CB Multipart Minecraft",
                    "cccbridge=Create: CC Bridge",
                    "chefsdelight=Chef's Delight",
                    "cherisheditems=Cherished Items",
                    "codechickenlib=Code Chicken Lib",
                    "compressedcreativity=Create: Compressed Creativity",
                    "conditional_mixin=Conditional Mixin",
                    "cookingforblockheads=Cooking for Blockheads",
                    "corpsecurioscompat=Corpse Curios Compat",
                    "cosmeticarmorreworked=Cosmetic Armor Reworked",
                    "create__the_salt=Create: The Salt (Creative Delight)",
                    "create_compressed=Create: Compressed",
                    "create_confectionery=Create: Confectionery",
                    "create_crush_everything=Create: Recycling",
                    "create_enchantment_industry=Create: Enchantment Industry",
                    "create_mechanical_extruder=Create: Mechanical Extruder",
                    "create_questing=Create: Questing",
                    "create_things_and_misc=Create: Things & Misc",
                    "createaddition=Create: Crafts & Additions",
                    "createappliedkinetics=Create: Applied Kinetics",
                    "createdeco=Create: Deco",
                    "creategbd=Create: Guardian Beam Defense",
                    "creategoggles=Create: Goggles",
                    "createliquidfuel=Create: Liquid Fuel",
                    "creative_labels=Creative Labels (Alfonzo)",
                    "creativecore=Creative Core",
                    "creativeengineering=Creative Engineering (Alfonzo)",
                    "ctm=Connected Textures Mod",
                    "darkpaintings=Dark Paintings",
                    "deathknell=Death Knell",
                    "design_decor=Create: Design & Decor",
                    "do_a_barrel_roll=Do a Barrel Roll",
                    "dont_void_arrows=Don't Void Arrows",
                    "dragonloot=Dragon Loot",
                    "dummmmmmy=Target Dummy",
                    "enchdesc=Enchantment Descriptions",
                    "enderstorage=Ender Storage",
                    "entityculling=Entity Culling",
                    "everythingcopper=Everything Is Copper",
                    "exposure=Exposure Backport & Extras (Alfonzo)",
                    "exposure_catalog=Exposure Catalog Backport (Alfonzo)",
                    "exposure_expanded=Exposure: Expanded Backport (Alfonzo)",
                    "exposure_extras=Exposure Extras (Alfonzo)",
                    "exposure_polaroid=Exposure Polaroid Backport (Alfonzo)",
                    "extendedgears=Create: Extended Cogwheels",
                    "farmers_structures=Farmer's Structures",
                    "fastfurnace=Fast Furnace",
                    "fireflybushport=Firefly Bush Port",
                    "fishontheline=Fish on the Line",
                    "fortuneonneterite_i_ig_z=Fortune on Netherite",
                    "foundation=Foundation Vinery Backport (Alfonzo)",
                    "framedblocks=Framed Blocks",
                    "geckolib3=Gecko Lib",
                    "hobbit_house_=Hobbit House",
                    "hopour=Hopo Better Underwater Ruins",
                    "immediatelyfast=Immediately Fast",
                    "ipo=Improved Pillager Outpost",
                    "itemphysic=Item Physic",
                    "just_enough_beacons=Just Enough Beacons",
                    "kotlinforforge=Kotlin for Forge",
                    "kubejsdelight=KubeJS Delight",
                    "lightmansdiscord=Lightman's Discord Integration",
                    "littlecontraptions=Create: Little Contraptions",
                    "minecraft_style_paintings=Minecraft Style Paintings",
                    "mixinsquared=Mixin Squared",
                    "more_mooblooms=More Mooblooms (Alfonzo)",
                    "netherportalfix=Nether Portal Fix",
                    "nocubescreateexp=Create: Compact Exp",
                    "notenoughanimations=Not Enough Animations",
                    "old_swamp_fisherman_house=Old Swamp Fisherman House",
                    "oreexcavation=Ore Excavation",
                    "portablewardrobe=Portable Wardrobe",
                    "projectred_core=Project Red Core",
                    "projectred_fabrication=Project Red Fabrication",
                    "projectred_illumination=Project Red Illumination",
                    "projectred_integration=Project Red Integration",
                    "projectred_transmission=Project Red Transmission",
                    "railways=Create: Steam & Rails",
                    "restapi=REST API",
                    "sawmillhouse=Sawmill House",
                    "seasonhud=Season HUD",
                    "simplehats=Simple Hats",
                    "sliceanddice=Create: Slice & Dice",
                    "spectrelib=Spectre Lib",
                    "supermartijn642configlib=Config Library",
                    "supermartijn642corelib=Core Lib",
                    "swingthroughgrass=Swing Through Grass",
                    "t_and_t=Towns & Towers",
                    "takesapillage=It Takes a Pillage",
                    "tmted=Engineer's Delight",
                    "transition=Transition",
                    "trashslot=Trash Slot",
                    "treechop=HT's Tree Chop",
                    "villagersellanimals=Villagers Sell Animals",
                    "vinery=Let's Do Vinery Backport (Alfonzo)",
                    "w2w2=Waystones 2 Waypoints 2",
                    "waila=Waila",
                    "whitenoise=White Noise",
                    "worldedit=World Edit",
                    "wthit=WTHIT",
                    "xaerolib=Xaero Lib",
                    "xaerozoomout=Xaero Zoomout"
            ), o -> o instanceof String str && str.indexOf('=') > 0);

            b.pop(); // mod_names
        }
    }

}
