package net.ribs.vintagedelight.config;

import net.ribs.vintagedelight.shared.CdSettings;

// From Vintage Delight KOTS 0.3.1: the magic vine tree's ground conversion can be switched off.
// Port: the Pickling Jar's leftover slot (remainder_buffer.md, as the keg's).
// Since the port became a Creative Delight member (2026-11-02, plan_creative_delight.md R3) the three settings are keys
// of Creative Delight's config, section [vintage] (same sub-tables and key names as the port's old
// vintagedelight-common.toml, which Creative Delight reads once and renames); CD's CDConfig defines them.
public final class VintageDelightCommonConfig {
    private VintageDelightCommonConfig() {
    }

    public static boolean magicVineConvertsDirtToLushGrass() {
        return CdSettings.flag("vintage.worldgen.magicVineConvertsDirtToLushGrass", true);
    }

    public static boolean remainderBuffer() {
        return CdSettings.flag("vintage.pickling_jar.remainderBuffer", true);
    }

    public static boolean stallWhenFull() {
        return CdSettings.flag("vintage.pickling_jar.stallWhenFull", true);
    }

    /** Ticks before a leftover the jar throws out leaves it (creativedelight members #37); 0 throws at once. */
    public static int overflowEjectDelayTicks() {
        return Math.max(0, CdSettings.number("vintage.pickling_jar.overflowEjectDelayTicks", 20));
    }
}
