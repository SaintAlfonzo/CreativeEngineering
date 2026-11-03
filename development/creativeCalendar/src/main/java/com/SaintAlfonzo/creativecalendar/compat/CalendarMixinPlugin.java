package com.SaintAlfonzo.creativecalendar.compat;

import com.SaintAlfonzo.creativecalendar.shared.GatedMixinPlugin;

import java.util.List;
import java.util.Map;

/**
 * Mixin config plugin for creativecalendar.mixins.json (feature_writeup/creative_calendar.md 4.1): the table form of the
 * shared {@link GatedMixinPlugin} (development/shared/mixinplugin). A listed mixin applies only when one of its mods is
 * loaded, so Calendar boots the same without Serene Seasons, SeasonHUD, Better Days, Xaero's Minimap or Pretty Rain.
 * Mixins not listed (vanilla targets: ServerLevelTimePauseMixin) always apply. The mixins moved from patcherMod
 * unchanged apart from their package (SRG names, remap = false).
 */
public class CalendarMixinPlugin extends GatedMixinPlugin {

    private static final List<String> SERENE = List.of("sereneseasons");

    private static final Map<String, List<String>> NEEDS_MOD = Map.ofEntries(
            Map.entry("CropSeasonGrowHookMixin", List.of("immersiveengineering", "the_dirty_stuff", "nethersdelight")),
            Map.entry("SereneSeasonsMeltThrottleMixin", SERENE),
            Map.entry("SereneSeasonsClockMixin", SERENE),
            Map.entry("SereneSeasonsTimeSkipMixin", SERENE),
            Map.entry("SereneSeasonsClientClockMixin", SERENE),
            Map.entry("SeasonBiomeConfigMixin", SERENE),
            Map.entry("SeasonHooksTypesMixin", SERENE),
            Map.entry("SeasonHooksClientMixin", SERENE),
            Map.entry("SeasonFertilityMixin", SERENE),
            // crop seasons for grass spread (CropSeasons reads Serene Seasons); the IW one also needs Immersive Weathering
            Map.entry("GrassSpreadSeasonMixin", SERENE),
            Map.entry("IWGrowthSeasonMixin", List.of("immersive_weathering")),
            // modded grass blocks that spread outside the vanilla random tick; Serene Seasons checked at runtime
            Map.entry("ModdedGrassSpreadSeasonMixin", List.of("immersive_weathering", "botania", "vinery", "vintagedelight")),
            Map.entry("SeasonColorUtilMixin", SERENE),
            // vanilla target, but it reads SeasonTypes, which needs Serene Seasons' classes
            Map.entry("SeasonRainColumnMixin", SERENE),
            Map.entry("BetterDaysConfigMixin", List.of("betterdays")),
            Map.entry("BetterDaysTimePauseMixin", List.of("betterdays")),
            Map.entry("XaeroTimeInfoMixin", List.of("xaerominimap")),
            Map.entry("SeasonHudHelperMixin", List.of("seasonhud")),
            Map.entry("SeasonHudCurrentSeasonMixin", List.of("seasonhud")),
            Map.entry("PrettyRainSeasonMixin", List.of("particlerain")),
            Map.entry("CoolRainSeasonMixin", List.of("coolrain")));

    public CalendarMixinPlugin() {
        super("[creativecalendar]", NEEDS_MOD);
    }
}
