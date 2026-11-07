// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/java/earth/terrarium/tempad/common/config/CommonConfig.java.
// Translated for Forge 1.19.2 (TP-D4): Resourceful Config -> ForgeConfigSpec, file tempad-common.toml, same keys,
// categories, comments and defaults (upstream declares no ranges, so none are enforced beyond the int type). Added
// (TP-D5): require_location_upgrade, which replaces upstream's "required_location_upgrade" feature flag (default
// false = the flag off, upstream's default).
package earth.terrarium.tempad.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue ALLOW_INTERDIMENSIONAL_TRAVEL;
    private static final ForgeConfigSpec.BooleanValue ALLOW_INTRADIMENSIONAL_TRAVEL;
    private static final ForgeConfigSpec.BooleanValue ALLOW_LOCATION_SAVING;
    private static final ForgeConfigSpec.IntValue MAX_HISTORY_SIZE;
    private static final ForgeConfigSpec.BooleanValue REQUIRE_LOCATION_UPGRADE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        ALLOW_INTERDIMENSIONAL_TRAVEL = b.comment("Whether or not Time Doors are allowed to be opened to dimensions other than the one they are in.")
                .define("allow_interdimensional_travel", true);
        ALLOW_INTRADIMENSIONAL_TRAVEL = b.comment("Whether or not Time Doors are allowed to be opened to the same dimension they are in.")
                .define("allow_intradimensional_travel", true);
        ALLOW_LOCATION_SAVING = b.comment("Whether or not locations can be saved to the Tempad.")
                .define("allow_location_saving", true);
        MAX_HISTORY_SIZE = b.comment("The maximum amount of locations that the Time Twister will save location history for.")
                .defineInRange("max_history_size", 512, Integer.MIN_VALUE, Integer.MAX_VALUE);
        REQUIRE_LOCATION_UPGRADE = b.comment("Whether the New Location app needs the New Location Upgrade installed in the Tempad.",
                        "Replaces upstream Tempad's \"required_location_upgrade\" data pack (off by default, as upstream).",
                        "When off, the New Location Upgrade item is hidden from the creative tab and its recipes are not loaded.")
                .define("require_location_upgrade", false);
        TimeDoor.define(b);
        Chronometer.define(b);
        ChrononGenerator.define(b);
        Cell.define(b);
        Battery.define(b);
        RudimentaryTempad.define(b);
        Tempad.define(b);
        TimeTwister.define(b);
        Metronome.define(b);
        Chronomark.define(b);
        SPEC = b.build();
    }

    private CommonConfig() {
    }

    private static ForgeConfigSpec.IntValue anInt(ForgeConfigSpec.Builder b, String key, int def, String comment) {
        return b.comment(comment).defineInRange(key, def, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static boolean allowInterdimensionalTravel() {
        return ALLOW_INTERDIMENSIONAL_TRAVEL.get();
    }

    public static boolean allowIntradimensionalTravel() {
        return ALLOW_INTRADIMENSIONAL_TRAVEL.get();
    }

    public static boolean allowLocationSaving() {
        return ALLOW_LOCATION_SAVING.get();
    }

    public static int maxHistorySize() {
        return MAX_HISTORY_SIZE.get();
    }

    /** The local file's value; game logic reads {@link CommonConfigCache#requireLocationUpgrade()} (synced). */
    public static boolean requireLocationUpgrade() {
        return REQUIRE_LOCATION_UPGRADE.get();
    }

    public static final class TimeDoor {
        private static ForgeConfigSpec.IntValue placementDistance;
        private static ForgeConfigSpec.BooleanValue logWhenOpen;
        private static ForgeConfigSpec.IntValue costPerDoor;
        private static ForgeConfigSpec.IntValue costToPersist;
        private static ForgeConfigSpec.IntValue timeInWorld;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("timedoor");
            placementDistance = anInt(b, "placement_distance", 3, "The distance between the Time Door and the player when placed.");
            logWhenOpen = b.comment("Whether or not the Time Door should log when it is opened in the console.").define("log_when_open", false);
            costPerDoor = anInt(b, "cost_per_door", 1000, "The cost that any device requires to open a timedoor for 10 seconds");
            costToPersist = anInt(b, "cost_to_persist", 10, "The cost that any device requires to open add 1 second to a timedoor's life");
            timeInWorld = anInt(b, "time_in_world", 200, "The amount of time in ticks that a Time Door will remain open");
            b.pop();
        }

        public static int placementDistance() {
            return placementDistance.get();
        }

        public static boolean logWhenOpen() {
            return logWhenOpen.get();
        }

        public static int costPerDoor() {
            return costPerDoor.get();
        }

        public static int costToPersist() {
            return costToPersist.get();
        }

        public static int timeInWorld() {
            return timeInWorld.get();
        }
    }

    public static final class RudimentaryTempad {
        static ForgeConfigSpec.IntValue capacityRudi;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("timedoor_projector");
            capacityRudi = anInt(b, "capacity", 4000, "The amount of fuel that the Rudimentary Tempad can store.");
            b.pop();
        }
    }

    public static final class Tempad {
        static ForgeConfigSpec.IntValue capacityTempad;
        static ForgeConfigSpec.IntValue maxOffsetTempad;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("tempad");
            capacityTempad = anInt(b, "capacity", 6000, "The amount of fuel that the Tempad can store (without the time twister being installed).");
            maxOffsetTempad = anInt(b, "max_offset", 5, "The maximum range that you can set the portal position to be");
            b.pop();
        }
    }

    public static final class TimeTwister {
        static ForgeConfigSpec.IntValue capacityTimeTwister;
        private static ForgeConfigSpec.IntValue costToBacktrack;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("time_twister");
            capacityTimeTwister = anInt(b, "capacity", 3000, "The amount of fuel that the Time Twister can store.");
            costToBacktrack = anInt(b, "cost_to_backtrack", 500, "The cost that the Time Twister consumes to backtrack to a location");
            b.pop();
        }

        public static int costToBacktrack() {
            return costToBacktrack.get();
        }
    }

    public static final class ChrononGenerator {
        static ForgeConfigSpec.IntValue capacitorGenerator;
        private static ForgeConfigSpec.IntValue generationRate;
        private static ForgeConfigSpec.IntValue generationAmount;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("chronon_generator");
            capacitorGenerator = anInt(b, "capacity", 0, "The amount of fuel that the Chronometer can store.");
            generationRate = anInt(b, "generation_rate", 48, "The amount of time it takes for the Chronometer to generate 1 chronon (in ticks).");
            generationAmount = anInt(b, "generation_amount", 1, "The amount of chronons generated everytime the Chronometer does a tick of work.");
            b.pop();
        }

        public static int generationRate() {
            return generationRate.get();
        }

        public static int generationAmount() {
            return generationAmount.get();
        }
    }

    public static final class Chronometer {
        static ForgeConfigSpec.IntValue capacityChronometer;
        private static ForgeConfigSpec.IntValue generationRate;
        private static ForgeConfigSpec.IntValue generationAmount;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("chronometer");
            capacityChronometer = anInt(b, "capacity", 0, "The amount of fuel that the Chronometer can store.");
            generationRate = anInt(b, "generation_rate", 36, "The amount of time it takes for the Chronometer to generate 1 chronon (in ticks).");
            generationAmount = anInt(b, "generation_amount", 1, "The amount of chronons generated everytime the Chronometer does a tick of work.");
            b.pop();
        }

        public static int generationRate() {
            return generationRate.get();
        }

        public static int generationAmount() {
            return generationAmount.get();
        }
    }

    public static final class Metronome {
        static ForgeConfigSpec.IntValue capacityMetronome;
        private static ForgeConfigSpec.IntValue generationRate;
        private static ForgeConfigSpec.IntValue generationAmount;
        static ForgeConfigSpec.BooleanValue scaleGeneration;
        private static ForgeConfigSpec.IntValue transferRate;
        private static ForgeConfigSpec.IntValue jumpStartAmount;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("metronome");
            capacityMetronome = anInt(b, "capacity", 6000, "The amount of fuel that the Chronometer can store.");
            generationRate = anInt(b, "generation_rate", 24, "The amount of time it takes for the Chronometer to generate 1 chronon (in ticks).");
            generationAmount = anInt(b, "generation_amount", 1, "The number of chronons generated everytime the Chronometer does a tick of work.");
            scaleGeneration = b.comment("Whether or not the number of chronons generated per cycle is scaled to the number of metronomes placed")
                    .define("should_scale_generation", true);
            transferRate = anInt(b, "transfer_rate", 20, "The number of chronons thats transferred to each internal slot or to a neighboring block per tick.");
            jumpStartAmount = anInt(b, "jump_start_amount", 2000, "Whether or not the number of chronons generated per cycle is scaled to the number of metronomes placed");
            b.pop();
        }

        public static int generationRate() {
            return generationRate.get();
        }

        public static int generationAmount() {
            return generationAmount.get();
        }

        public static int transferRate() {
            return transferRate.get();
        }

        public static int jumpStartAmount() {
            return jumpStartAmount.get();
        }
    }

    public static final class Cell {
        static ForgeConfigSpec.IntValue capacityCapacitor;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("chronon_cell");
            capacityCapacitor = anInt(b, "capacity", 2000, "The amount of fuel that the Capacitor can store.");
            b.pop();
        }
    }

    public static final class Battery {
        static ForgeConfigSpec.IntValue capacityBattery;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("chronon_battery");
            capacityBattery = anInt(b, "capacity", 4000, "The amount of fuel that the Battery can store.");
            b.pop();
        }
    }

    public static final class Chronomark {
        static ForgeConfigSpec.IntValue maxOffsetChronomark;

        static void define(ForgeConfigSpec.Builder b) {
            b.push("chronomark");
            maxOffsetChronomark = anInt(b, "max_offset", 5, "The maximum range that you can set the portal position to be");
            b.pop();
        }
    }
}
