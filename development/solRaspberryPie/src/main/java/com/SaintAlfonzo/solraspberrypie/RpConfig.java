package com.SaintAlfonzo.solraspberrypie;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/** Server settings are per world (serverconfig/solraspberrypie-server.toml); the ladder is its own JSON file (3). */
public final class RpConfig {
	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		var s = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER = s.getLeft();
		SERVER_SPEC = s.getRight();
		var c = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT = c.getLeft();
		CLIENT_SPEC = c.getRight();
	}

	public static final class Server {
		public final ForgeConfigSpec.IntValue respecFoods;
		public final ForgeConfigSpec.IntValue effectDuration;
		public final ForgeConfigSpec.IntValue refreshInterval;
		public final ForgeConfigSpec.BooleanValue pauseEndsOnRelog;
		public final ForgeConfigSpec.BooleanValue giveBooksOnFirstJoin;
		public final ForgeConfigSpec.BooleanValue uniqueFoodsToActivate;

		Server(ForgeConfigSpec.Builder b) {
			b.push("diet");
			uniqueFoodsToActivate = b.comment("If true, Apple Pie's minFoodsToActivate counts different foods eaten since the counter last reset",
							"(death, /solrp reset, /foodlist clear); a food eaten twice counts once. If false, every food eaten counts (Apple Pie's own rule).")
					.define("uniqueFoodsToActivate", true);
			respecFoods = b.comment("Foods aged out of the decay window (oldest first) when a slot is changed to a different effect.")
					.defineInRange("respecFoods", 5, 1, 256);
			effectDuration = b.comment("Duration in ticks of every diet effect instance (flat bonuses and slots).")
					.defineInRange("effectDuration", 800, 100, 72000);
			refreshInterval = b.comment("Every diet effect is re-added unconditionally this often, in ticks (staggered per player).",
							"Keep it well below effectDuration.")
					.defineInRange("refreshInterval", 400, 20, 72000);
			pauseEndsOnRelog = b.comment("If true, /solrp pause ends when the player logs out; by default it persists across relog and restart.")
					.define("pauseEndsOnRelog", false);
			giveBooksOnFirstJoin = b.comment("Give a new player the Diet Log and the Liquid Diet Log on their first join.")
					.define("giveBooksOnFirstJoin", true);
			b.pop();
		}
	}

	public static final class Client {
		public final ForgeConfigSpec.BooleanValue widgetEnabled;
		public final ForgeConfigSpec.BooleanValue widgetLocked;
		public final ForgeConfigSpec.IntValue widgetX;
		public final ForgeConfigSpec.IntValue widgetY;
		public final ForgeConfigSpec.DoubleValue bookScale;
		public final ForgeConfigSpec.IntValue extraWidth;
		public final PageLayout diversityPage;
		public final PageLayout mealsPage;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> diversityIcons;
		public final PageLayout ladderPage;
		public final PageLayout milestonesPage;
		public final ForgeConfigSpec.EnumValue<UneatenSort> uneatenSort;
		public final ForgeConfigSpec.EnumValue<EatenSort> eatenSort;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> unlockIcon;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> flatIcon;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> slotIcon;

		Client(ForgeConfigSpec.Builder b) {
			b.push("dietLog");
			bookScale = b.comment("Diet Log size: 1.4 draws the book at 140%. Shrinks to fit a small window, never below 100%",
							"(or below this value when it is under 1.0).")
					.defineInRange("bookScale", 1.4, 0.5, 3.0);
			extraWidth = b.comment("Diet Log background stretched sideways by this many pixels: the book's center column is repeated,",
							"the pages widen and the page buttons move out by half of it. 0 = Apple Pie's own width.")
					.defineInRange("extraWidth", 0, 0, 400);
			diversityIcons = b.comment("Diet Log Overview: foods shown left of the diversity value when no food you have eaten fits the",
							"current diversity. Each entry is \"minimum diversity=item,item,...\"; the entry with the highest minimum at or",
							"below the diversity is used, one of its loaded items shown.")
					.defineList("diversityIcons", List.of(
							"0=minecraft:apple,minecraft:bread,minecraft:carrot,minecraft:sweet_berries",
							"10=minecraft:baked_potato,minecraft:cooked_chicken,minecraft:mushroom_stew,minecraft:cookie",
							"25=minecraft:cooked_beef,farmersdelight:mixed_salad,farmersdelight:vegetable_soup,farmersdelight:bacon_and_eggs",
							"40=farmersdelight:beef_stew,farmersdelight:fruit_salad,farmersdelight:ratatouille,minecraft:rabbit_stew",
							"60=farmersdelight:steak_and_potatoes,farmersdelight:pasta_with_meatballs,farmersdelight:roast_chicken,minecraft:golden_carrot",
							"80=farmersdelight:honey_glazed_ham,farmersdelight:shepherds_pie,farmersdelight:stuffed_pumpkin,minecraft:golden_apple",
							"100=minecraft:enchanted_golden_apple"), o -> o instanceof String);
			b.comment("Position (x, y: book pixels added to the page's top left corner) and size (scale, relative to the book)",
					"of each Diet Log page's content.").push("pages");
			diversityPage = new PageLayout(b, "diversity", "Apple Pie's diversity page.", 0, 1.0);
			mealsPage = new PageLayout(b, "meals", "The last meals grid on the Overview page (x, y move it from its place above the diversity line).", 0, 0.85);
			ladderPage = new PageLayout(b, "ladder", "The Diet Ladder page (progress bar and key).", 10, 0.85);
			milestonesPage = new PageLayout(b, "milestones", "The Milestones pages.", 10, 0.85);
			b.pop();
			b.pop();
			b.push("foodDictionary");
			uneatenSort = b.comment("Food Dictionary (Spice of Life: Carrot Edition) order of the uneaten foods: MOD_NAME (mod, then name),",
							"NAME (A-Z), EASIEST (lowest Apple Pie complexity first, then nutrition), HARDEST (the reverse).")
					.defineEnum("uneatenSort", UneatenSort.MOD_NAME);
			eatenSort = b.comment("Food Dictionary order of the eaten foods, also set by the sort button on those pages: NEWEST_UNLOCK,",
							"LATEST_EATEN (Apple Pie's meal memory), UNEATEN_ORDER (the uneatenSort order).")
					.defineEnum("eatenSort", EatenSort.NEWEST_UNLOCK);
			b.pop();
			b.push("icons");
			unlockIcon = b.comment("Diet Log icon for one-time unlocks: the first of these items that is loaded. An item may carry NBT",
							"in braces, e.g. minecraft:potion{Potion:\"minecraft:awkward\"}.")
					.defineList("unlock", List.of("monobank:replacement_lock", "minecraft:potion{Potion:\"minecraft:awkward\"}"), o -> o instanceof String);
			flatIcon = b.comment("Diet Log icon for permanent buffs: the first of these items that is loaded.")
					.defineList("permanentBuff", List.of("create_crush_everything:diamond_shard", "minecraft:diamond"), o -> o instanceof String);
			slotIcon = b.comment("Diet Log icon for effect slots (the Diet Log never shows the chosen effect): the first of these items that is loaded.")
					.defineList("slot", List.of("suppsquared:metal_frame", "minecraft:item_frame"), o -> o instanceof String);
			b.pop();
			b.push("widget");
			widgetEnabled = b.comment("Show the diet bonus bar on the survival inventory. When off, diet effects show like any other effect.")
					.define("enabled", true);
			widgetLocked = b.comment("Locked: the bar stays put and a click opens the diet bonus screen. Toggle in game with Shift+T.")
					.define("locked", true);
			widgetX = b.comment("Bar position relative to the inventory's left edge.").defineInRange("x", 178, -2000, 2000);
			widgetY = b.comment("Bar position relative to the inventory's top edge.").defineInRange("y", 0, -2000, 2000);
			b.pop();
		}
	}

	/** One Diet Log page type's content placement (plan_diet_books.md 1.4). */
	public static final class PageLayout {
		public final ForgeConfigSpec.IntValue x;
		public final ForgeConfigSpec.IntValue y;
		public final ForgeConfigSpec.DoubleValue scale;

		PageLayout(ForgeConfigSpec.Builder b, String name, String what, int defaultY, double defaultScale) {
			b.comment(what).push(name);
			x = b.defineInRange("x", 0, -200, 200);
			y = b.defineInRange("y", defaultY, -200, 200);
			scale = b.defineInRange("scale", defaultScale, 0.25, 2.0);
			b.pop();
		}
	}

	public enum UneatenSort { MOD_NAME, NAME, EASIEST, HARDEST }

	public enum EatenSort {
		NEWEST_UNLOCK, LATEST_EATEN, UNEATEN_ORDER;

		public EatenSort next() {
			return values()[(ordinal() + 1) % values().length];
		}
	}

	private RpConfig() {}
}
