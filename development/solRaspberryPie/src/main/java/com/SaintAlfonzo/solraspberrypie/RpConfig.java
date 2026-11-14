package com.SaintAlfonzo.solraspberrypie;

import net.minecraftforge.common.ForgeConfigSpec;

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
                    .defineInRange("respecFoods", 8, 1, 256);
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
        public final ForgeConfigSpec.IntValue progressAhead;
        public final ForgeConfigSpec.BooleanValue doublePage;

        Client(ForgeConfigSpec.Builder b) {
            b.push("dietLog");
            progressAhead = b.comment("Diet Log progress bar: how many milestones ahead of the last one reached it shows.")
                    .defineInRange("progressAhead", 3, 1, 50);
            doublePage = b.comment("The Diet Log opens as two pages, foods still in the window on the left and the first page of",
                            "foods out of the window on the right. Needs a screen wide enough for both pages.")
                    .define("doublePage", true);
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

    private RpConfig() {}
}
