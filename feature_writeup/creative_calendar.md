# Creative Calendar: calendar, day and night, seasons and biome weather in one mod

Written 2026-10-31 (reserved), execution doc since 2026-11-01. Owner, 2026-10-31: "mark the wine clock as part of a
future migration of the calendar, day-night cycles, season and effects by biomes, that both creative delight and creative
brewing would rely on, called creative calendar". Carved in wave 1 of the Creative Split
([creative_split.md](creative_split.md) 7.6), beside Creative Devtools.

**Status: carve INSTALLED 2026-11-01, scratch-verified (SQ-CAL-01 to -07 PASS, run rcs1,
[scratch_run_2026-11-01.md](testing/scratch_run_2026-11-01.md) 6); client CQ-CAL-08 and in-game CAL-01 to -04 open.**
`creativecalendar-1.0.0.jar` (`development/creativeCalendar/build/libs/`) is in `mods/` with the patcherMod, Creative
Delight and Creative Brewing jars of the same pass (section 7).

Written for: the owner and the split agents, as the working reference for Calendar. Same shape as
[creative_brewing.md](creative_brewing.md); the systems themselves stay documented in [time.md](time.md),
[weather.md](weather.md) (season types), [weather_by_biome.md](weather_by_biome.md) and [crops.md](crops.md) 2.12.1
(crop seasons). Same behaviour as before the carve: no player changelog bullet.

## 0. At a glance

| | |
|---|---|
| Gradle project | `:creativeCalendar`, `development/creativeCalendar` (plain `ce.forge-mod`, no members) |
| Mod id, package | `creativecalendar`, `com.SaintAlfonzo.creativecalendar` |
| Jar | `creativecalendar-1.0.0.jar` |
| Config | `config/creativecalendar-common.toml` (`CalendarConfig`) |
| Script global | `global.CAL` (`kubejs_startup/creativecalendar/cal_core.js`) |
| Requires | `creativeengineering` (patcherMod; `creativecommon` after the Q1 swap), later `creativecompat` (creative_split.md 7.6) |
| Required by | `creativedelight`, `creativebrewing` (mandatory, `ordering="AFTER"`) |
| Ids | unchanged: tags, WTHIT option ids and lang keys keep `creativeengineering`; `/cetime` keeps its name |

## 1. Shape

Calendar holds the pack's time and season systems and sits below the two bundles: **Creative Delight** (crops grow by
crop season) and **Creative Brewing** (Vinery's wine clock) both require it. patcherMod does not depend on it.

## 2. Inventory before the carve (2026-10-31, kept for reference)

`PM` = `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`. Section 5 is what actually moved.

| Area | Where it was |
|---|---|
| Calendar and the `[time]` master (day length, season length, pause when empty, `/cetime`) | `PM/time/`: CalendarDate, SeasonClock, TimeClient, TimeCommand, TimeDate, TimePause, TimePresets, TimePush, TimeSettings |
| Wine clock | `PM/time/WineTime.java`, `[time.wine]`, `[time.wine.boosts]`; read by CB's Vinery (`net/satisfy/vinery/forge/PackTime.java`) |
| Day and night cycle (Better Days) | mixins BetterDaysConfig, BetterDaysTimePause, ServerLevelTimePause |
| Seasons (Serene Seasons clock, types, colours, melt, time skip; SeasonHUD; Xaero's time line) | `PM/seasons/SeasonTypes.java`; 11 Season* / SereneSeasons* / SeasonHud* / XaeroTimeInfo mixins; `[seasons]` |
| Crop seasons | `PM/seasons/CropSeasons.java`, `CropSeasonTooltip.java`, `compat/WthitCropSeasons`; mixins CropSeasonGrowHook, SeasonFertility; `[crop_seasons]`; `server_scripts/tags/cropseasontags.js` |
| Weather by biome | mixins SeasonRainColumn, PrettyRainSeason; `server_scripts/tags/seasontags.js` |

## 3. Rules (the creative_split.md 3 rules as they apply here)

1. **Ids stay** (3.1, no conversions). Calendar registers nothing in a registry. Its tags
   (`creativeengineering:seasons/*`, `snow/no_*`, `crop_seasons/*`), the WTHIT option ids
   (`creativeengineering:crop_seasons`, `crop_out_of_season`), the WTHIT plugin id and every lang key keep the
   `creativeengineering` namespace: `CreativeCalendar.NAMESPACE`. `CreativeCalendar.MOD_ID` is only for the
   `@Mod.EventBusSubscriber` annotations, the config file and the script packs.
2. **Dependencies point down.** Calendar calls no patcherMod Java (it keeps `compileOnly project(':patcherMod')` for the
   later Common APIs). CD and CB call down into Calendar; Calendar never names CD or CB.
3. **Config keys moved with their feature**, names and comments unchanged (4.3).
4. **Mixins moved unchanged** apart from their package (SRG names, `remap = false`), each on the side it had.
5. **Scripts** follow rule 3.9: startup in the jar, server tree authored in `kubejs/data/creativecalendar/kubejs/`,
   one folder per mod, `// requires: <modid>` headers, no underscore in folder names.
6. **Seasons are Serene Seasons' saved state**; moving the mixins changes no world data.

## 4. The framework

### 4.1 Build and Java

`ce.forge-mod` with `shared_sources=mixinplugin` (`GatedMixinPlugin` compiled in as
`com.SaintAlfonzo.creativecalendar.shared.GatedMixinPlugin`), `disable_target_validator=true`, no refmap.
`build.gradle` compiles against patcherMod and the Serene Seasons, SeasonHUD and WTHIT jars in `development/patcherMod/libs/`,
copies the server script tree into the jar, and fails the jar task when `kubejs_startup/creativecalendar/` or
`data/creativecalendar/kubejs/` has no `.js`.

- `CreativeCalendar` (the `@Mod`): registers the config, the mod-bus listeners `TimePush::onConfigReload` and
  `TimePresets::onConfigLoading`, and on the Forge bus `TimePush`, `SeasonClock`, `TimeCommand` (the registrations that
  were at `CreativeEngineering.java` 225-231). `SeasonTypes`, `CropSeasons`, `CropSeasonTooltip` (client) and `TimeClient`
  (client) subscribe themselves (`@Mod.EventBusSubscriber(modid = creativecalendar)`). Logs
  `[creativecalendar] time, seasons and crop seasons loaded`; `CalendarConfigMigration` moves the old keys once (4.3).
- `compat/CalendarMixinPlugin`: the table form of `GatedMixinPlugin`. Each mixin applies only when one of its mods is
  loaded: Serene Seasons for the 10 Serene Seasons mixins and `SeasonRainColumnMixin` (vanilla target, reads
  `SeasonTypes`), `betterdays`, `seasonhud`, `xaerominimap`, `particlerain`; `CropSeasonGrowHookMixin` on any of
  `immersiveengineering`, `the_dirty_stuff`, `nethersdelight`. `ServerLevelTimePauseMixin` (vanilla) always applies.
- `creativecalendar.mixins.json`: 10 common, 8 client, no `injectors.defaultRequire` (as patcherMod's, so every
  injector's `require` is what it was).
- `waila_plugins.json`: the `creativeengineering:crop_seasons` entry (required `sereneseasons`), now pointing at
  `com.SaintAlfonzo.creativecalendar.compat.WthitCropSeasons`.
- `assets/creativeengineering/lang/en_us.json`: the 31 Calendar lang keys (crop season lines, calendar tooltip,
  SeasonHUD "Arid" / "Frozen", the WTHIT plugin name). Minecraft merges every jar's file for the namespace.

### 4.2 Load order and dependencies

`mods.toml`: `creativeengineering` mandatory `[1.1.04,)` AFTER; `sereneseasons`, `betterdays`, `seasonhud` optional, no
ordering (patcherMod never declared them, so the load order is the one it had). CD's and CB's `mods.toml` require
`creativecalendar` `[1.0.0,)` AFTER, so the startup script packs run: patcherMod, `startup_scripts/`, Calendar, CD, CB.

The Vinery member compiles against `project(':creativeCalendar')`. `PackTime` imports
`com.SaintAlfonzo.creativecalendar.time.WineTime` and calls it directly: the `ModList.isLoaded` guard is gone because
CB requires Calendar. Creative Delight has no Java or script edge into Calendar: its wild crops and the Hearth and
Harvest tags read the pack file `kubejs/generation_scripts/worldgen/crop_seasons.json` (and `biome_traits.json`), which
stays in the pack folder; the dependency is load order and the crop-season growth CD crops get.

### 4.3 Config: `config/creativecalendar-common.toml` (CalendarConfig)

| Section | Keys | From |
|---|---|---|
| `[seasons]` | `enabled`, `adaptiveRadius`, `snowLineY` | `CEConfig.Seasons` |
| `[crop_seasons]` | `enabled`, `fertileBonus`, `offPenalty`, `grassOffPenalty` (new 2026-11-03, 0.5: grass spread in summer and tropical seasons), `greenhouse`, `caveFertileBelowY`, `caveOffAboveY` | `CEConfig.CropSeasons` |
| `[time]`, `.day`, `.seasons`, `.wine`, `.wine.boosts` | the 16 `[time]` keys | `CEConfig.Time` |
| `[perf]` | `seasonsMeltIntervalTicks` (read by `SereneSeasonsMeltThrottleMixin`) | `CEConfig.Perf` |
| `[wthit]` | `cropSeasons`, `cropOutOfSeason` (read by `WthitCropSeasons`) | `CEConfig.Wthit` |

The feature switches are the three `enabled` keys: `CalendarConfig.feature("seasons" | "crop_seasons" | "time")`, in
scripts `global.CAL.feature(id)`. Every edited value in `config/creativeengineering-common.toml` equalled the default on
2026-11-01; the new file was written by hand with Forge's comments (`config/` and
`configs_edited/root_config/creativecalendar-common.toml`). The moved sections were deleted from
`configs_edited/root_config/creativeengineering-common.toml`. Forge's correction never dropped them from the live file (r1103 still had them), so 2026-11-03 (plan_scratch_bugfix_1103 7.7): the 29 values were diffed against the new file (all equal), and the three sections plus the three strays were deleted from `config/creativeengineering-common.toml` by hand. `CalendarConfigMigration` does the same on any other install (the server): at construction, before configs load, it reads the moved keys from the CE file and strips them (with their comments) from it; when `creativecalendar-common.toml` first loads, each kept value whose new key still holds the default is written in and saved, logged as `[creativecalendar] moved edited values into ...`. A value already edited in the new file wins.

### 4.4 Scripts

| Type | Authored in | In the jar | Core file |
|---|---|---|---|
| Startup | `development/creativeCalendar/src/main/resources/kubejs_startup/creativecalendar/` | same path | `cal_core.js`: `global.CAL` (`on`, `feature`, `recipes`) |
| Server | `kubejs/data/creativecalendar/kubejs/<modid>/` | `data/creativecalendar/kubejs/` | `core/cal_server.js` (checks `global.CAL` on load) |
| Client | none yet | | |

`sereneseasons/seasontags.js` (season type and snow biome tags) and `sereneseasons/cropseasontags.js` (crop season tags,
greenhouse glass), both `// requires: sereneseasons`, moved from `kubejs/server_scripts/tags/` (those files are left
holding a one-line pointer, as CB's moved files were). `global.CAL.recipes(fn)` writes `creativecalendar:` recipe ids;
Calendar has no recipes yet. Server scripts load from the pack folder on `/reload`; the startup script needs a rebuild
and restart.

## 5. Move list (2026-11-01)

| What | From | To |
|---|---|---|
| 10 classes | `PM/time/` | `creativecalendar/time/` |
| 3 classes | `PM/seasons/` | `creativecalendar/seasons/` |
| `WthitCropSeasons` | `PM/compat/` | `creativecalendar/compat/` (Interface row showing only Calendar data, creative_split.md 2.4) |
| 18 mixins (10 common: CropSeasonGrowHook, SereneSeasonsMeltThrottle, SereneSeasonsClock, SereneSeasonsTimeSkip, BetterDaysConfig, BetterDaysTimePause, ServerLevelTimePause, SeasonBiomeConfig, SeasonHooksTypes, SeasonFertility; 8 client: SereneSeasonsClientClock, XaeroTimeInfo, SeasonHooksClient, SeasonRainColumn, SeasonColorUtil, SeasonHudHelper, SeasonHudCurrentSeason, PrettyRainSeason) | `PM/mixin/`, `creativeengineering.mixins.json` | `creativecalendar/mixin/`, `creativecalendar.mixins.json` |
| Config sections and keys (4.3) | `CEConfig` | `CalendarConfig` |
| Registrations | `CreativeEngineering.java` 225-231 | `CreativeCalendar` |
| WTHIT plugin entry, 31 lang keys | patcherMod resources | Calendar resources |
| 2 server scripts | `kubejs/server_scripts/tags/` | `kubejs/data/creativecalendar/kubejs/sereneseasons/` |

**Doubtful rows, decided in this carve:**

- `IEConcreteSetTimeMixin` (listed "check" in 2): **stays in patcherMod** (Engineering later). It lowers IE liquid
  concrete's set time from 32 to 30 s for the JEI Concrete Hardening tab and reads no `[time]` value; the earlier brief's
  "tied to day length" reason does not hold.
- `CampfireRain` and `campfire/`: **stay** (owner 7.6: Compat).
- `SeasonedItemModelMixin`: **stays** (seasonings, food).
- `server_scripts/_lib/dump_crop_growth.js`: dev dump, Devtools' row.
- The pack data files `crop_seasons.json`, `season_types.json`, `biome_traits.json` stay in
  `kubejs/generation_scripts/worldgen/` (read at runtime from the game folder, shipped by `build_deploy.py` as before).
- Sweep B named the Dirty Stuff id `thedirtystuff`; the real id is `the_dirty_stuff` (the gate uses that).

**Not moved, still correct:** the third-party configs the push writes (`betterdays-common.toml`, the world's
`sereneseasons-server.toml`, `seasonhud-server.toml`); `config/sereneseasons/fertility.toml`; Vinery's own
`vinery-common.toml`.

## 6. Tests

Scratch server: SQ-CAL-01 to -07 and client CQ-CAL-08 in [scratch_queue.md](testing/scratch_queue.md) (section "SQ-CAL").
The moved features keep their own suites: TI- ([time.md](time.md)), WE- ([weather.md](weather.md)), CR-89 to CR-97
([crops.md](crops.md)), SQ-TD and SQ-CS. In game (after install):

- [ ] **CAL-01 Mod list:** "Creative Calendar" is listed; Creative Delight and Creative Brewing name it as a dependency.
- [ ] **CAL-02 Old world:** the season, day and wine ages of an existing world are where they were; `/cetime date` agrees
  with SeasonHUD.
- [ ] **CAL-03 Config:** `config/creativecalendar-common.toml` holds the sections of 4.3 with the values from before; no
  `[time]`, `[seasons]` or `[crop_seasons]` left in `creativeengineering-common.toml`, and on a copy of the server's old CE file an edited
  `[time.day] dayMinutes` lands in the new file (`moved edited values` line);
  editing `[time.day] dayMinutes` in Create's Configs screen pushes again (`[time]` lines in latest.log).
- [ ] **CAL-04 Log:** latest.log has `[creativecalendar] time, seasons and crop seasons loaded`; startup.log lists
  `creativecalendar` before `creativedelight` and `creativebrewing`.

## 7. Install (game closed)

All four in one go (a new patcherMod without the Calendar jar loses `/cetime` and the seasons; the old patcherMod with it
applies every season mixin twice): `./gradlew :patcherMod:installToMods :creativeCalendar:installToMods
:creativeDelight:installToMods :creativeBrewing:installToMods --offline`. The dedicated server (CreEngServer) needs
the same four jars at the next deploy. Calendar's `creativeengineering` range `[1.1.04,)` cannot tell the old patcherMod
1.1.04 from the new one: raise it with the next patcherMod version bump (8, Q1).

## 8. Open questions

| # | Question | Recommendation |
|---|---|---|
| Q1 | patcherMod's version: the carve leaves it at 1.1.04 | Bump with the wave-1 commit and set Calendar's range to that version, so an old patcherMod beside Calendar refuses to load instead of doubling the mixins |
| Q2 | Calendar's `creativecompat` dependency | Added when Compat is carved (creative_split.md 7.6) |
| Q3 | Licence audit for publishing (Q7: CB's chain) | Calendar is all own code (GPLv3); no donor files, no ARR content |

## 9. History

- 2026-10-31: reserved (owner); WineTime marked; inventory from names only.
- 2026-11-01: carved (creative_split.md 7.6 wave 1): 13 classes, `WthitCropSeasons`, 18 mixins, 3 config sections and 3
  keys, 2 server scripts, 31 lang keys and the WTHIT entry moved; CD and CB require Calendar; Vinery's `PackTime` calls
  it directly. patcherMod, Calendar, CD and CB compile; SQ-CAL rows queued; not installed.
