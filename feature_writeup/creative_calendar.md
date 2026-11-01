# Creative Calendar: calendar, day and night, seasons and biome weather in one mod (reserved)

Written 2026-10-31. **Status: reserved, nothing moved.** Owner, 2026-10-31: "mark the wine clock as part of a future
migration of the calendar, day-night cycles, season and effects by biomes, that both creative delight and creative brewing
would rely on, called creative calendar".

Written for: the owner, as the marker list for a later plan. This file only says what is reserved and the constraints
already known; the systems themselves are documented in [time.md](time.md), [weather.md](weather.md) (season types),
[weather_by_biome.md](weather_by_biome.md) and [crops.md](crops.md) 2.12.1 (crop seasons).

## 1. Shape

A future own mod, **Creative Calendar**, holding the pack's time and season systems. It would sit below the two
bundles: **Creative Delight** (crop seasons, wild crops by season) and **Creative Brewing** (Vinery's wine clock) both
depend on it. Unlike Creative Cultivation (creative_delight.md 29.5), nothing is pre-placed: everything below stays in
patcherMod and the pack folders until a plan is written. Files that are clearly reserved get a "Reserved for Creative
Calendar" line in their header as they are touched (first: `time/WineTime.java`, 2026-10-31).

## 2. Reserved (inventory 2026-10-31, from the package and mixin names; check each before a move)

`PM` = `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`.

| Area | Where today |
|---|---|
| Calendar and the `[time]` master (day length, season length, pause when empty, `/cetime`) | `PM/time/`: CalendarDate, SeasonClock, TimeClient, TimeCommand, TimeDate, TimePause, TimePresets, TimePush, TimeSettings |
| Wine clock (Vinery aging on the sun clock; months = sub-seasons) | `PM/time/WineTime.java` (marked), `[time.wine]`, `[time.wine.boosts]`; read by Creative Brewing's Vinery (`net/satisfy/vinery/forge/PackTime.java`, soft dependency) |
| Day and night cycle (Better Days) | mixins BetterDaysConfig, BetterDaysTimePause, ServerLevelTimePause; IE concrete set time (IEConcreteSetTimeMixin, check) |
| Seasons (Serene Seasons clock, types, colours, melt, time skip; SeasonHUD; Xaero's time line) | `PM/seasons/SeasonTypes.java`; mixins SereneSeasonsClock, SereneSeasonsClientClock, SereneSeasonsMeltThrottle, SereneSeasonsTimeSkip, SeasonBiomeConfig, SeasonColorUtil, SeasonHooksClient, SeasonHooksTypes, SeasonHudCurrentSeason, SeasonHudHelper, XaeroTimeInfo; `[seasons]` |
| Crop seasons (fertile / neutral / off by season type) | `PM/seasons/CropSeasons.java`, `CropSeasonTooltip.java`; mixins CropSeasonGrowHook, SeasonFertility; `[crop_seasons]`; `kubejs/server_scripts/tags/cropseasontags.js` |
| Weather and effects by biome (rain / snow / none per biome and sub-season, rain visuals) | mixins SeasonRainColumn, PrettyRainSeason, CampfireRain (check: campfire rain may stay with campfires); the season type tags |

Not reserved: `SeasonedItemModelMixin` (seasonings, food), the climate samples and biome tuning (worldgen).

## 3. Known constraints for the later plan

- **Load order:** Calendar must load before CD and CB (`ordering="BEFORE"` from their side or a mandatory dependency
  with `AFTER`), and patcherMod must not depend on it.
- **Config keys:** `[time]`, `[seasons]`, `[crop_seasons]` move with their keys and comments (the same rule as
  creative_brewing.md 4.1 and 4.2 phase 3); `configs_edited/` copied in the same session.
- **The Vinery soft dependency** in `PackTime.java` names `com.SaintAlfonzo.creativeengineering.time.WineTime`; it must
  follow the class (or the class keeps a forwarding copy for one release).
- **Seasons are server state** saved by Serene Seasons; moving our mixins changes no saved data.

## 4. History

- 2026-10-31: reserved (owner); WineTime marked; inventory from names only.
