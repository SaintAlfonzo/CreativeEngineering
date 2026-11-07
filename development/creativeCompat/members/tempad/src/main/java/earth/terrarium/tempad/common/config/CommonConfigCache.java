// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/config/CommonConfigCache.kt
// and ConfigCache.kt. Translated to Java for Forge 1.19.2: the values the client must agree on (capacities, offsets,
// metronome scaling) are read from the server's tempad-common.toml and sent to each player on login and to everyone when
// the file is reloaded (upstream's Observable listeners), as one ConfigSyncPacket. Added: requireLocationUpgrade (the
// replacement of the feature flag, which upstream's clients learn from the server's enabled features).
package earth.terrarium.tempad.common.config;

import earth.terrarium.tempad.common.network.s2c.ConfigSyncPacket;

public final class CommonConfigCache {
    private static int chrononCellCapacity = 2000;
    private static int batteryCapacity = 4000;
    private static int chrononGeneratorCapacity = 0;
    private static int chronometerCapacity = 0;
    private static int chronomarkMaxOffset = 5;
    private static int metronomeCapacity = 6000;
    private static boolean metronomeScaleGeneration = true;
    private static int rudimentaryTempadCapacity = 4000;
    private static int tempadCapacity = 6000;
    private static int tempadMaxOffset = 5;
    private static int timeTwisterCapacity = 3000;
    private static boolean requireLocationUpgrade = false;

    private CommonConfigCache() {
    }

    /** Reads the local file (ModConfigEvent.Loading / Reloading, both sides). */
    public static void refreshFromConfig() {
        if (!CommonConfig.SPEC.isLoaded()) return;
        chrononCellCapacity = CommonConfig.Cell.capacityCapacitor.get();
        batteryCapacity = CommonConfig.Battery.capacityBattery.get();
        chrononGeneratorCapacity = CommonConfig.ChrononGenerator.capacitorGenerator.get();
        chronometerCapacity = CommonConfig.Chronometer.capacityChronometer.get();
        chronomarkMaxOffset = CommonConfig.Chronomark.maxOffsetChronomark.get();
        metronomeCapacity = CommonConfig.Metronome.capacityMetronome.get();
        metronomeScaleGeneration = CommonConfig.Metronome.scaleGeneration.get();
        rudimentaryTempadCapacity = CommonConfig.RudimentaryTempad.capacityRudi.get();
        tempadCapacity = CommonConfig.Tempad.capacityTempad.get();
        tempadMaxOffset = CommonConfig.Tempad.maxOffsetTempad.get();
        timeTwisterCapacity = CommonConfig.TimeTwister.capacityTimeTwister.get();
        requireLocationUpgrade = CommonConfig.requireLocationUpgrade();
    }

    public static ConfigSyncPacket createPacket() {
        return new ConfigSyncPacket(chrononCellCapacity, batteryCapacity, chrononGeneratorCapacity, chronometerCapacity,
                chronomarkMaxOffset, metronomeCapacity, metronomeScaleGeneration, rudimentaryTempadCapacity, tempadCapacity,
                tempadMaxOffset, timeTwisterCapacity, requireLocationUpgrade);
    }

    /** Client side: the server's values. */
    public static void apply(ConfigSyncPacket packet) {
        chrononCellCapacity = packet.chrononCellCapacity();
        batteryCapacity = packet.batteryCapacity();
        chrononGeneratorCapacity = packet.chrononGeneratorCapacity();
        chronometerCapacity = packet.chronometerCapacity();
        chronomarkMaxOffset = packet.chronomarkMaxOffset();
        metronomeCapacity = packet.metronomeCapacity();
        metronomeScaleGeneration = packet.metronomeScaleGeneration();
        rudimentaryTempadCapacity = packet.rudimentaryTempadCapacity();
        tempadCapacity = packet.tempadCapacity();
        tempadMaxOffset = packet.tempadMaxOffset();
        timeTwisterCapacity = packet.timeTwisterCapacity();
        requireLocationUpgrade = packet.requireLocationUpgrade();
    }

    public static final class ChrononCell {
        public static int capacity() {
            return chrononCellCapacity;
        }
    }

    public static final class Battery {
        public static int capacity() {
            return batteryCapacity;
        }
    }

    public static final class ChrononGenerator {
        public static int capacity() {
            return chrononGeneratorCapacity;
        }
    }

    public static final class Chronometer {
        public static int capacity() {
            return chronometerCapacity;
        }
    }

    public static final class Chronomark {
        public static int maxOffset() {
            return chronomarkMaxOffset;
        }
    }

    public static final class Metronome {
        public static int capacity() {
            return metronomeCapacity;
        }

        public static boolean scaleGeneration() {
            return metronomeScaleGeneration;
        }
    }

    public static final class RudimentaryTempad {
        public static int capacity() {
            return rudimentaryTempadCapacity;
        }
    }

    public static final class Tempad {
        public static int capacity() {
            return tempadCapacity;
        }

        public static int maxOffset() {
            return tempadMaxOffset;
        }
    }

    public static final class TimeTwister {
        public static int capacity() {
            return timeTwisterCapacity;
        }
    }

    public static boolean requireLocationUpgrade() {
        return requireLocationUpgrade;
    }
}
