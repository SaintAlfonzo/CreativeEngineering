package com.SaintAlfonzo.creativedelight;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creative Delight's config, {@code config/creativedelight-common.toml} (creative_delight.md 8.1):
 * <ul>
 * <li>{@code [members]}: one switch per nested member, default on; off makes CD's scripts skip that member's rows
 * ({@code global.CD.on(id)}), the member's own data stays. The base members ({@link #BASE_MEMBERS}, Farmer's
 * Delight) get no switch: every other member needs them, so they are always on.</li>
 * <li>{@code [qol]}: one switch per QoL feature of section 8 ({@link #QOL}), default on; scripts read it through
 * {@code global.CD.qol(id)}, Java through {@link #qol}.</li>
 * <li>{@code [worldgen]}: one switch per worldgen system CD owns ({@link #WORLDGEN}, creative_delight.md 24), default on;
 * scripts read it through {@code global.CD.worldgen(id)}, Java through {@link #worldgen}.</li>
 * <li>{@code [display]}, {@code [vintage]}: the settings of the Display Delight and Vintage Delight ports, one section per
 * member (plan_creative_delight.md R3, B1; 2026-11-02). Defined here, read by the member through its shared
 * {@code CdSettings} by path; the ports' old files are read once ({@link #MEMBER_FILES}) and renamed.</li>
 * </ul>
 * The {@code integrations} and {@code swappers} sections of 8.1 are added with the first feature that uses them.
 *
 * Scripts read it through {@code global.CD} (kubejs_startup/creativedelight/cd_core.js). COMMON configs load after
 * mod construction, so a startup script must read it inside an event callback, not at top level; before the file
 * is loaded every switch reads as its default.
 */
public final class CDConfig {

	/** Mod ids of the nested members, from creativedelight_members.txt (generated from bundle_members). */
	public static final List<String> MEMBERS = readMembers();

	/** Members every other member depends on: no switch, always on while loaded. */
	public static final List<String> BASE_MEMBERS = List.of("farmersdelight");

	/** QoL switch ids (creative_delight.md 8). QoL-3 ({@code foodTypes}) gets its key when it moves. */
	public static final String REMAINDER_BUFFER = "remainderBuffer";
	public static final String PLACEABLE_TOOLTIPS = "placeableTooltips";
	public static final String COMPOST = "compost";
	public static final String MANUAL_ONLY_ROUTES = "manualOnlyRoutes";
	public static final String WTHIT_CROP_LINE = "wthitCropLine";
	public static final String JEI_INFO_PAGES = "jeiInfoPages";

	/** Worldgen switch ids (creative_delight.md 24). */
	public static final String WILD_CROPS = "wildCrops";

	/** This config's file, and patcherMod's, which held the sections moved here at the CD carve. */
	public static final String FILE = "creativedelight-common.toml";
	public static final String OLD_FILE = "creativeengineering-common.toml";

	/**
	 * Sections moved here from creativeengineering-common.toml (2026-11-01, creative_delight.md 34), for the carry-over
	 * and the leftover WARN. {@code perf} carries only {@code troughMissCooldownTicks}: Forge drops the other old keys
	 * when it corrects this file.
	 */
	public static final List<String> MOVED_SECTIONS = List.of("crop_growth", "dandelion_maturing", "crop_drops", "stem_fruit",
			"canning", "cooking", "perf");

	public static final ForgeConfigSpec SPEC;
	public static final CropGrowth CROP_GROWTH;
	public static final CropDrops CROP_DROPS;
	public static final StemFruit STEM_FRUIT;
	public static final DandelionMaturing DANDELION_MATURING;
	public static final Cooking COOKING;
	public static final Canning CANNING;
	public static final Perf PERF;
	private static final Map<String, ForgeConfigSpec.BooleanValue> MEMBER_SWITCHES = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> QOL = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> WORLDGEN = new LinkedHashMap<>();

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment("One switch per Creative Delight member. Off: Creative Delight's scripts skip that member's recipe,",
				"tag and loot rows (the member's own data is untouched). Server scripts re-read it on /reload.").push("members");
		for (String id : MEMBERS) {
			if (BASE_MEMBERS.contains(id)) continue;
			MEMBER_SWITCHES.put(id, b.define(id, true));
		}
		b.pop();

		b.comment("One switch per Creative Delight QoL feature (feature_writeup/creative_delight.md 8). Off: that feature",
				"is skipped entirely. Startup and Java parts need a restart, server scripts re-read on /reload, client",
				"scripts on F3+T.").push("qol");
		qolSwitch(b, REMAINDER_BUFFER, "QoL-1: ingredient leftovers (bottles, bowls, buckets) stay in the Farmer's Delight cooking",
				"pot and the Farmer's Respite kettle (two take-only slots). Off: they are thrown out as in the original mods.",
				"The stall and recycle dials are [cooking] below; the oven is its own block there.");
		qolSwitch(b, PLACEABLE_TOOLTIPS, "QoL-2: the 'Placeable' / 'Placeable on plate' tooltip lines, in place of the mods' own.");
		qolSwitch(b, COMPOST, "QoL-4: Creative Delight's composter chances for member foods. Off: those foods fall to the pack's",
				"compost rules.");
		qolSwitch(b, MANUAL_ONLY_ROUTES, "QoL-5: grid meals on cooked food re-made as manual-only crafts, heatless routes for pot",
				"meals, tortilla wraps (Veggies, Vintage and Fungi Delight). Off: the mods' own recipes stay.");
		qolSwitch(b, WTHIT_CROP_LINE, "QoL-6: the WTHIT 'Growth' line for plants WTHIT doesn't cover (only with WTHIT).",
				"Dials: [crop_growth] below.");
		qolSwitch(b, JEI_INFO_PAGES, "QoL-6: JEI information pages for member mechanics (only with JEI).");
		b.pop();

		b.comment("One switch per Creative Delight worldgen system (feature_writeup/creative_delight.md 24). Rejoin the world",
				"to apply; only chunks generated afterwards change.").push("worldgen");
		WORLDGEN.put(WILD_CROPS, b.comment("Which biomes each wild crop and fruit tree spawns in (_worldgen/wild_crops.json).",
				"Off: every crop as its mod ships it.").define(WILD_CROPS, true));
		b.pop();
		defineMemberSettings(b);
		// Moved from creativeengineering-common.toml at the CD carve (2026-11-01, creative_delight.md 34): same keys,
		// comments and defaults, in the old order; ConfigRename carries the player's values over on the first boot.
		CROP_GROWTH = new CropGrowth(b);
		DANDELION_MATURING = new DandelionMaturing(b);
		CROP_DROPS = new CropDrops(b);
		STEM_FRUIT = new StemFruit(b);
		CANNING = new Canning(b);
		COOKING = new Cooking(b);
		PERF = new Perf(b);
		SPEC = b.build();
	}

	private CDConfig() {}

	/** Lines of creativeengineering-common.toml that held the sections or the key moved here. */
	private static final Pattern OLD_KEY = Pattern.compile(
			"^\\s*(\\[(?:crop_growth|dandelion_maturing|crop_drops|stem_fruit|canning|cooking)(?:\\.[a-z_.]+)?\\]|troughMissCooldownTicks\\s*=)",
			Pattern.MULTILINE);

	/**
	 * When no carry-over ran (this file already had the sections, or patcherMod's file had none), Forge drops the old
	 * sections from patcherMod's file on its next correction, so an edited value left there is lost: say which ones were
	 * still there. Runs at construction, before Forge loads and corrects the COMMON configs.
	 */
	public static void warnOldKeys() {
		Path old = FMLPaths.CONFIGDIR.get().resolve(OLD_FILE);
		if (!Files.isRegularFile(old)) return;
		String text;
		try {
			text = Files.readString(old, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return;
		}
		List<String> found = new ArrayList<>();
		Matcher m = OLD_KEY.matcher(text);
		while (m.find()) found.add(m.group(1).trim());
		if (found.isEmpty()) return;
		LogUtils.getLogger().warn("[creativedelight] config/{} still has entries that moved to config/{}: {}. "
				+ "Creative Delight reads only its own file; copy any edited value over (patcherMod drops these from its file).",
				OLD_FILE, FILE, found);
	}

	/**
	 * Old config files of members whose settings moved here (plan_creative_delight.md A6): file -> {old path -> new path},
	 * a new path starting with {@code !} takes the old value inverted (the "Disable..." keys became positive switches).
	 * Read once by {@code compat.MemberConfigMove} at construction, then the old file is renamed to {@code <file>.moved}.
	 */
	public static final Map<String, Map<String, String>> MEMBER_FILES = Map.of(
			"displaydelight-common.toml", Map.of(
					"Features.Display-Message", "display.placeMessage",
					"Features.Disable-Vanila-Food-Placement", "!display.vanillaFoodPlacement",
					"Features.Disable-Eating-Animation-Fix", "!display.eatingAnimationFix",
					"Features.SilkTouchDropFoodBlock", "display.silkTouchDropsFoodBlock",
					"Features.DisableCreativeTabItems", "!display.creativeTabItems",
					"Features.DisablePlaceAllInteratction", "!display.placeAll",
					"Features.DisableTakeAllInteratction", "!display.takeAll",
					"Features.DisableHidePlateInteratction", "!display.axeHidesPlates",
					"Features.DisablePutPlateInteratction", "!display.platesPutBack"),
			"vintagedelight-common.toml", Map.of(
					"worldgen.magicVineConvertsDirtToLushGrass", "vintage.worldgen.magicVineConvertsDirtToLushGrass",
					"pickling_jar.remainderBuffer", "vintage.pickling_jar.remainderBuffer",
					"pickling_jar.stallWhenFull", "vintage.pickling_jar.stallWhenFull"));

	/** The member sections (R3): keys the ports read by path (their CdSettings); names must match those reads. */
	private static void defineMemberSettings(ForgeConfigSpec.Builder b) {
		b.comment("Display Delight (Creative Delight): placing food on the floor and on plates. Was displaydelight-common.toml",
				"(its [Features] values are carried over once). Changes apply at once.").push("display");
		b.comment("A chat message when a food cannot be placed.").define("placeMessage", true);
		b.comment("Mushroom stew, rabbit stew and beetroot soup can be placed. Off if that clashes with another mod.")
				.define("vanillaFoodPlacement", true);
		b.comment("Skip the eating animation while plating some foods. Off if foods can't be eaten or plated.")
				.define("eatingAnimationFix", true);
		b.comment("A placed food broken with Silk Touch drops the food block item.").define("silkTouchDropsFoodBlock", true);
		b.comment("Display Delight's items in its creative tab (and so in JEI's list).").define("creativeTabItems", true);
		b.comment("Shift right-click places all held items at once.").define("placeAll", true);
		b.comment("Shift right-click takes all items at once.").define("takeAll", true);
		b.comment("Right-click with an axe hides a food's plate.").define("axeHidesPlates", true);
		b.comment("Right-click with a plate puts a hidden plate back.").define("platesPutBack", true);
		b.pop();

		b.comment("Vintage Delight (Creative Delight). Was vintagedelight-common.toml (its values are carried over once).")
				.push("vintage");
		b.push("worldgen");
		b.comment("Magic vine trees turn nearby dirt-like blocks into lush grass while they generate.")
				.define("magicVineConvertsDirtToLushGrass", true);
		b.pop();
		b.push("pickling_jar");
		b.comment("Keep ingredient leftovers (glass bottles, bowls, buckets) in the Pickling Jar's leftover slot instead of",
				"throwing them out.").define("remainderBuffer", true);
		b.comment("With remainderBuffer on, the jar waits instead of pickling when a batch's leftovers would not fit.",
				"Off pickles anyway and throws what does not fit.").define("stallWhenFull", true);
		b.comment("Ticks a leftover the jar throws out (remainderBuffer off, or the slot full) waits before it leaves the jar.",
				"0 throws it on the same tick as the batch.").defineInRange("overflowEjectDelayTicks", 20, 0, 1200);
		b.pop();
		b.pop();
	}

	private static void qolSwitch(ForgeConfigSpec.Builder b, String id, String... comment) {
		QOL.put(id, b.comment(comment).define(id, true));
	}

	/** True if QoL switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
	public static boolean qol(String id) {
		ForgeConfigSpec.BooleanValue v = QOL.get(id);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	/** True if worldgen switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
	public static boolean worldgen(String id) {
		ForgeConfigSpec.BooleanValue v = WORLDGEN.get(id);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	/** True if {@code modId} is one of the nested members. */
	public static boolean isMember(String modId) {
		return MEMBERS.contains(modId);
	}

	/** True if {@code modId} is a member and its switch is on (or the config is not loaded yet, or it is a base member). */
	public static boolean member(String modId) {
		if (BASE_MEMBERS.contains(modId)) return MEMBERS.contains(modId);
		ForgeConfigSpec.BooleanValue v = MEMBER_SWITCHES.get(modId);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	public static List<String> members() {
		return MEMBERS;
	}

	private static List<String> readMembers() {
		List<String> ids = new ArrayList<>();
		try (InputStream in = CDConfig.class.getResourceAsStream("/creativedelight_members.txt")) {
			if (in == null) return List.of();
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String line; (line = r.readLine()) != null; ) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) ids.add(line);
			}
		} catch (IOException e) {
			throw new IllegalStateException("creativedelight_members.txt unreadable", e);
		}
		return Collections.unmodifiableList(ids);
	}

    /**
     * Dials for Creative Delight's {@code compat.wthit.WthitCropGrowth} (client-side, display only): the "Growth" line WTHIT
     * leaves off bushes, vines, hemp, tobacco and fruit-tree leaves.
     */
    public static final class CropGrowth {
        public final ForgeConfigSpec.BooleanValue fallback;
        public final ForgeConfigSpec.BooleanValue showHarvestable;

        private CropGrowth(ForgeConfigSpec.Builder b) {
            b.comment(
                    "WTHIT shows 'Growth: 60%' / 'Growth: Mature' only for CropBlock subclasses plus a few vanilla",
                    "plants. This adds the same line, with the same wording and the same 'Crop progress' toggle in",
                    "WTHIT's own config, to every other growing plant. The blocks and their stages are in",
                    "assets/creativedelight/crop_growth.json (override it from kubejs/assets/creativedelight/).",
                    "Client-side only; reads the block state the client already has. The line is Creative Delight's:",
                    "its on/off switch is creativedelight-common.toml [qol] wthitCropLine."
            ).push("crop_growth");

            fallback = b.comment(
                    "true: a bush-type plant (BushBlock, not a sapling) with an integer 'age' that no rule in",
                    "crop_growth.json lists still gets a line, fully grown at its top age. Catches plants from",
                    "mods added later. false: only the plants in the file."
            ).define("fallback", true);

            showHarvestable = b.comment(
                    "true: plants that can be picked before they are fully grown (salmonberries from stage 3,",
                    "grapes from stage 2) read 'Growth: 75% (Harvestable)' until they finish. false: the",
                    "percentage only."
            ).define("showHarvestable", true);

            b.pop(); // crop_growth
        }
    }

    /**
     * Crop harvest table values read by Creative Delight's {@code rusticdelight/crop_drops.js} and
     * {@code creativeengineering/soil_yield.js} (server side); feature_writeup/crops.md 3. A change needs /reload.
     */
    public static final class CropDrops {
        public final ForgeConfigSpec.BooleanValue bellPeppers;
        public final ForgeConfigSpec.IntValue redWeight;
        public final ForgeConfigSpec.IntValue greenWeight;
        public final ForgeConfigSpec.IntValue yellowWeight;
        public final ForgeConfigSpec.DoubleValue redBonusChance;
        public final ForgeConfigSpec.DoubleValue greenBonusChance;
        public final ForgeConfigSpec.DoubleValue yellowBonusChance;
        public final SoilYield SOIL_YIELD;

        private CropDrops(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Pack changes to crop harvest drops (server side, applied on /reload)."
            ).push("crop_drops");

            b.comment(
                    "Rustic Delight bell peppers. A ripe plant drops one pepper picked by weight, then each",
                    "colour has its own bonus chance for one extra. Each colour's share of the picked pepper is",
                    "weight / (sum of weights). Per harvest, colour X averages",
                    "  weightX / (sum of weights) + bonusChanceX",
                    "Defaults (1:1:1, 0.15 each) give 0.483 of every colour. The mod ships 6:1:1 with no red",
                    "bonus (red about 58% of peppers). A weight of 0 takes the colour out of the main roll."
            ).push("bell_peppers");

            bellPeppers = b.comment("true: use the values below. false: the mod's own table, untouched.")
                    .define("enabled", true);

            redWeight = b.comment("Weight of red in the main roll.")
                    .defineInRange("redWeight", 1, 0, 100);
            greenWeight = b.comment("Weight of green in the main roll.")
                    .defineInRange("greenWeight", 1, 0, 100);
            yellowWeight = b.comment("Weight of yellow in the main roll.")
                    .defineInRange("yellowWeight", 1, 0, 100);

            redBonusChance = b.comment("Chance [0-1] of one extra red pepper (0.15 = 15%).")
                    .defineInRange("redBonusChance", 0.15, 0.0, 1.0);
            greenBonusChance = b.comment("Chance [0-1] of one extra green pepper.")
                    .defineInRange("greenBonusChance", 0.15, 0.0, 1.0);
            yellowBonusChance = b.comment("Chance [0-1] of one extra yellow pepper.")
                    .defineInRange("yellowBonusChance", 0.15, 0.0, 1.0);

            b.pop(); // bell_peppers
            SOIL_YIELD = new SoilYield(b);
            b.pop(); // crop_drops
        }
    }

    /**
     * Extra produce from crops grown on better soil (CD {@code creativeengineering/soil_yield.js}, applied on /reload).
     * Each soil adds a binomial pool to a ripe crop's harvest; each crop is switched on on its own (all off by default).
     * feature_writeup/crop_yields.md.
     */
    public static final class SoilYield {
        /** Crop keys of soil_yield.js's CROPS table, in the same order. */
        public static final String[] CROPS = {
                "wheat", "carrots", "potatoes", "beetroots", "cabbages", "onions", "tomatoes", "rice",
                "bell_peppers", "coffee", "cotton", "ender_crop", "tobacco", "flax", "hemp", "corn", "cave_carrots",
                "broccoli", "cauliflower", "garlic", "sweet_potato", "turnip", "zucchini", "cucumber", "oats",
                "peanuts", "adzuki"
        };

        public final ForgeConfigSpec.IntValue farmlandTries;
        public final ForgeConfigSpec.DoubleValue farmlandChance;
        public final ForgeConfigSpec.IntValue richFarmlandTries;
        public final ForgeConfigSpec.DoubleValue richFarmlandChance;
        public final ForgeConfigSpec.IntValue fertileSoilTries;
        public final ForgeConfigSpec.DoubleValue fertileSoilChance;
        public final ForgeConfigSpec.IntValue richSoilTries;
        public final ForgeConfigSpec.DoubleValue richSoilChance;
        public final Map<String, ForgeConfigSpec.BooleanValue> crop = new java.util.LinkedHashMap<>();

        private SoilYield(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Extra produce on better soil. A ripe crop whose block below is the soil gets 'tries' extra",
                    "chances at one more of its produce (seeds are not included), each at 'chance'. Average extra per",
                    "harvest = tries x chance. The crop's own drops are unchanged. 0 tries = nothing for that soil.",
                    "Each crop is switched on in [crop_drops.soil_yield.crops]; all are off by default.",
                    "feature_writeup/crop_yields.md lists every crop's drops."
            ).push("soil_yield");

            farmlandTries = b.comment("Vanilla farmland: extra tries.").defineInRange("farmlandTries", 0, 0, 8);
            farmlandChance = b.comment("Vanilla farmland: chance [0-1] per try.").defineInRange("farmlandChance", 0.0, 0.0, 1.0);
            richFarmlandTries = b.comment("Rich Soil Farmland: extra tries.").defineInRange("richFarmlandTries", 2, 0, 8);
            richFarmlandChance = b.comment("Rich Soil Farmland: chance [0-1] per try.").defineInRange("richFarmlandChance", 0.35, 0.0, 1.0);
            fertileSoilTries = b.comment("Fertile Soil (Immersive Weathering mulch): extra tries.").defineInRange("fertileSoilTries", 2, 0, 8);
            fertileSoilChance = b.comment("Fertile Soil: chance [0-1] per try.").defineInRange("fertileSoilChance", 0.25, 0.0, 1.0);
            richSoilTries = b.comment("Untilled Rich Soil (only rice and adzuki grow on it): extra tries.").defineInRange("richSoilTries", 2, 0, 8);
            richSoilChance = b.comment("Untilled Rich Soil: chance [0-1] per try.").defineInRange("richSoilChance", 0.35, 0.0, 1.0);

            b.comment("true: the crop gets the soil pools above. false: its drops are the same on every soil.").push("crops");
            for (String key : CROPS) {
                crop.put(key, b.define(key, false));
            }
            b.pop(2); // crops, soil_yield
        }

        /** The crop's switch, or null for a key the config does not have. */
        public Boolean cropOn(String key) {
            ForgeConfigSpec.BooleanValue v = crop.get(key);
            return v == null ? null : v.get();
        }
    }

    /**
     * Extra fruit on melon, pumpkin and other stems ({@code crops.StemExtraFruit},
     * {@code mixin/AttachedStemExtraFruitMixin}); feature_writeup/crops.md 2.10. Read on every random tick.
     */
    public static final class StemFruit {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue maxFruits;
        public final ForgeConfigSpec.IntValue secondFruitMinutes;
        public final ForgeConfigSpec.IntValue thirdFruitMinutes;
        public final ForgeConfigSpec.IntValue fourthFruitMinutes;

        private StemFruit(ForgeConfigSpec.Builder b) {
            b.comment(
                    "A stem that has grown its fruit (melon, pumpkin, hamimelon) can grow more on its other free",
                    "sides, one at a time. Each extra fruit uses vanilla's stem growth roll (light, soil and",
                    "hydration count the same) and also waits a minimum time since the stem's last fruit. The",
                    "wait counts from the first random tick after a fruit grows. When the fruit the stem faces is",
                    "taken, the stem turns to another of its fruits instead of back into a plain stem. Server side."
            ).push("stem_fruit");

            enabled = b.comment("Master on/off switch. Off = vanilla (one fruit per stem).")
                    .define("enabled", true);
            maxFruits = b.comment("Most fruits next to one stem (1 = vanilla, 4 = every side).")
                    .defineInRange("maxFruits", 4, 1, 4);
            secondFruitMinutes = b.comment("Minimum game minutes (1200 ticks) after the first fruit before a 2nd can grow.")
                    .defineInRange("secondFruitMinutes", 10, 0, 1440);
            thirdFruitMinutes = b.comment("Minimum game minutes after the 2nd fruit before a 3rd can grow.")
                    .defineInRange("thirdFruitMinutes", 20, 0, 1440);
            fourthFruitMinutes = b.comment("Minimum game minutes after the 3rd fruit before a 4th can grow.")
                    .defineInRange("fourthFruitMinutes", 40, 0, 1440);

            b.pop(); // stem_fruit
        }
    }

    /**
     * A plain Dandelion planted on Rich Soil, Rich Soil Farmland or Fertile Soil turns into a Mature Dandelion
     * (Veggies Delight) on the soil's random ticks ({@code crops.DandelionMaturing}). Each value is "on average one
     * conversion per N random ticks of the soil block"; a block gets roughly one random tick a minute at the default
     * randomTickSpeed, so 10 is about ten minutes. 0 turns that soil off.
     */
    public static final class DandelionMaturing {
        public final ForgeConfigSpec.IntValue richSoilTicks;
        public final ForgeConfigSpec.IntValue fertileSoilTicks;

        private DandelionMaturing(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Plain Dandelions mature into Mature Dandelions when planted on Rich Soil / Rich Soil Farmland",
                    "or Fertile Soil. Values are the average number of soil random ticks per conversion",
                    "(about one random tick a minute per block at the default randomTickSpeed). 0 = off for that soil.",
                    "Takes effect immediately."
            ).push("dandelion_maturing");

            richSoilTicks = b.comment("Rich Soil and Rich Soil Farmland (Farmer's Delight).")
                    .defineInRange("richSoilTicks", 20, 0, 100000);
            fertileSoilTicks = b.comment("Fertile Soil (Immersive Weathering's mulch block).")
                    .defineInRange("fertileSoilTicks", 10, 0, 100000);

            b.pop(); // dandelion_maturing
        }
    }

    /**
     * Remainder buffer on FD-style stations (cooking pot, Farmer's Respite kettle, oven): ingredient leftovers stay
     * in the station. {@code cooking.RemainderBuffer}; feature_writeup/remainder_buffer.md. The pot and kettle hooks
     * are Creative Delight's (QoL-1, switch {@code [qol] remainderBuffer}); {@code remainderBuffer} here is the oven's,
     * the other dials are shared.
     */
    public static final class Cooking {
        public final ForgeConfigSpec.BooleanValue remainderBuffer;
        public final ForgeConfigSpec.BooleanValue stallWhenFull;
        public final ForgeConfigSpec.BooleanValue ovenIngredientCounts;
        public final ForgeConfigSpec.IntValue recycleShowTicks;

        private Cooking(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Leftover containers from ingredients (glass bottles, bowls, buckets) on the FD cooking pot, the",
                    "Farmer's Respite kettle and the oven. A leftover the waiting meal is served in goes to the",
                    "container slot; the rest goes to two leftover slots right of the output, which hoppers and",
                    "funnels on the output faces can empty."
            ).push("cooking");

            remainderBuffer = b.comment("Oven only. Off = its leftovers are thrown out of the front. The pot and kettle",
                    "switch is creativedelight-common.toml [qol] remainderBuffer.")
                    .define("remainderBuffer", true);

            stallWhenFull = b.comment(
                    "On = a station whose leftover slots can't take a craft's leftovers waits (like a full output).",
                    "Off = it crafts anyway and throws what doesn't fit."
            ).define("stallWhenFull", true);

            ovenIngredientCounts = b.comment(
                    "On = an oven recipe ingredient with a count takes that many from one slot (3 wheat from a wheat",
                    "stack), so oven copies of 9-item crafting recipes cost the same as the craft. Off = every",
                    "ingredient takes one item from its own slot, and recipes that don't fit the 6 slots drop some of",
                    "their repeated items (feature_writeup/oven.md 2.6). Recipes are built at /reload."
            ).define("ovenIngredientCounts", false);

            recycleShowTicks = b.comment(
                    "Ticks a leftover recycled into the container slot (a bowl-food ingredient's bowl, a milk bottle's",
                    "bottle) stays visible there before the meal is plated with it, so the step can be seen.",
                    "0 = plate at once. Containers a player or hopper puts in plate at once either way."
            ).defineInRange("recycleShowTicks", 10, 0, 100);

            b.pop(); // cooking
        }
    }

    /**
     * Dials for multi-serving cans ({@code food.CanServings}). How many servings a can holds is
     * NOT here: it comes from the {@code #creativeengineering:cans/servings/<n>} item tags, set in
     * KubeJS from the {@code kubejs_startup/<mod>/can_servings.js} member scripts (tools/gen_can_types.py).
     */
    public static final class Canning {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue emptyCanChance;
        public final ForgeConfigSpec.DoubleValue crushedCanChance;
        public final ForgeConfigSpec.ConfigValue<String> emptyCanItem;
        public final ForgeConfigSpec.ConfigValue<String> crushedCanItem;

        private Canning(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Cans in #creativeengineering:cans/servings/<n> hold n servings. Eating or crafting with",
                    "one takes a serving; the servings left show as a durability bar and a tooltip line. The",
                    "last serving leaves an empty can, a crushed can or nothing, rolled with the chances below."
            ).push("canning");

            enabled = b.comment("Off = every can behaves like the Canned Goods original (one serving, always an empty can).")
                    .define("enabled", true);

            emptyCanChance = b.comment("Chance [0-1] that an emptied can comes back as an empty can.")
                    .defineInRange("emptyCanChance", 0.7, 0.0, 1.0);

            crushedCanChance = b.comment(
                    "Chance [0-1] that it comes back as a crushed can instead. Whatever is left after both",
                    "chances gives nothing; if the two add up past 1, the crushed chance is cut to fit."
            ).defineInRange("crushedCanChance", 0.25, 0.0, 1.0);

            emptyCanItem = b.comment("Item id handed back as the empty can.")
                    .define("emptyCanItem", "canned_goods:empty_can");

            crushedCanItem = b.comment("Item id handed back as the crushed can.")
                    .define("crushedCanItem", "canned_goods:crushed_empty_can");

            b.pop(); // canning
        }
    }

    /**
     * The Quark feeding trough throttle, the one {@code [perf]} key that is Creative Delight's (it was in patcherMod's
     * {@code [perf]}; same name and comment). Read live.
     */
    public static final class Perf {
        public final ForgeConfigSpec.IntValue troughMissCooldownTicks;

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            troughMissCooldownTicks = b.comment(
                    "Quark feeding trough: after an animal's trough search finds nothing, it does not search again for",
                    "this many ticks (600 = 30 s). Each search is a point-of-interest scan plus a ray cast, and a",
                    "hungry animal with no trough nearby repeated it every few ticks. Cost: a trough placed or",
                    "refilled next to an animal is noticed up to this long late. 0 = off (Quark's own behaviour)."
            ).defineInRange("troughMissCooldownTicks", 600, 0, 72000);

            b.pop(); // perf
        }
    }
}
