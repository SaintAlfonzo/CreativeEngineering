package com.SaintAlfonzo.creativelabels;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * {@code config/creativelabels-common.toml} and {@code creativelabels-client.toml} (creative_labels-*.toml before 2026-11-10, carried over by {@link CreativeLabels}) (BRIEF 6). Read through
 * {@link #on}, {@link #num} and {@link #integer}, which fall back to the defaults while the config isn't loaded.
 */
public final class CLConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // [notes]
    public static final ForgeConfigSpec.IntValue NOTE_LENGTH;
    public static final ForgeConfigSpec.BooleanValue KEEP_SPECIAL_COLOR;
    public static final ForgeConfigSpec.DoubleValue GOLD_CHANCE;
    // [item_labels]
    public static final ForgeConfigSpec.IntValue ANVIL_COST;
    public static final ForgeConfigSpec.BooleanValue BLOCK_LABELLED_CRAFTING;
    // [wthit]
    public static final ForgeConfigSpec.BooleanValue WTHIT_LINES;
    // client [labels]
    public static final ForgeConfigSpec.DoubleValue PICTURE_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue TEXT_DISTANCE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("notes");
        NOTE_LENGTH = b.comment("Longest text a sticky note can hold (characters). The server enforces it.")
                .defineInRange("noteLength", 32, 1, 256);
        KEEP_SPECIAL_COLOR = b.comment("A special note (inverted, smiley, creeper, gold) picked up with sneak + empty hand keeps",
                        "its look. Off: it comes back as a plain note. Broken notes always keep their look.")
                .define("keepSpecialColor", true);
        GOLD_CHANCE = b.comment("Chance that a plain yellow note lands as a gold note when placed (server roll).")
                .defineInRange("goldChance", 0.01, 0.0, 1.0);
        b.pop();

        b.push("item_labels");
        ANVIL_COST = b.comment("Level cost of adding a note's text to an item in the anvil (0 can't be taken by vanilla).")
                .defineInRange("anvilCost", 1, 0, 39);
        BLOCK_LABELLED_CRAFTING = b.comment("A crafting grid holding any labelled item gives no result (erase the labels first).")
                .define("blockLabelledCrafting", true);
        b.pop();

        b.push("wthit");
        WTHIT_LINES = b.comment("Show the note or label under the crosshair in WTHIT.")
                .define("lines", true);
        b.pop();

        COMMON_SPEC = b.build();

        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        c.push("labels");
        PICTURE_DISTANCE = c.comment("Label pictures are drawn within this many blocks (farther labels show blank paper).")
                .defineInRange("pictureDistance", 16.0, 0.0, 64.0);
        TEXT_DISTANCE = c.comment("Label item names (feather) are drawn within this many blocks.")
                .defineInRange("textDistance", 6.0, 0.0, 64.0);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    /** A config value, or its default when the config isn't loaded yet (never throws). */
    public static boolean on(ForgeConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }

    public static double num(ForgeConfigSpec.DoubleValue value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }

    public static int integer(ForgeConfigSpec.IntValue value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }

    private CLConfig() {}
}
