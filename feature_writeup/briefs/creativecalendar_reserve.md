# creativecalendar / reserve: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `# Creative Calendar`, Sub `reserve` (row 1 open). Feature writeup:
[creative_calendar.md](../creative_calendar.md) (no test prefix yet; the row's Tests cell is `none`). The systems
themselves: [time.md](../time.md), [weather.md](../weather.md), [weather_by_biome.md](../weather_by_biome.md),
[crops.md](../crops.md) 2.12.1.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `reserve #1` check the reserved list, mark the files | docs + one header line per file | No code moves |
| 2 | (next row) Creative Calendar plan | doc | The row's "Next"; a new TODO row when written |

---

## reserve #1: Reserve the time, season and biome weather systems for Creative Calendar
Status Planned / Work Java / new jar / TODO `creativecalendar/reserve #1` (Tests none)

### Goal
A checked list of everything that will move into Creative Calendar, each file marked in its header, so no other move
(Creative Delight, Creative Brewing) takes it first; then a plan.

### Now (checked 2026-10-31)
- Marked: only `time/WineTime.java` (line 10, "Reserved for Creative Calendar (owner, 2026-10-31): moves with the whole
  time/ package ...").
- patcherMod (`development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/`), all present:
  - `time/`: CalendarDate, SeasonClock, TimeClient, TimeCommand, TimeDate, TimePause, TimePresets, TimePush,
    TimeSettings, WineTime.
  - `seasons/`: CropSeasons, CropSeasonTooltip, SeasonTypes.
  - Mixins: BetterDaysConfig, BetterDaysTimePause, ServerLevelTimePause, IEConcreteSetTime, SereneSeasonsClock,
    SereneSeasonsClientClock, SereneSeasonsMeltThrottle, SereneSeasonsTimeSkip, SeasonBiomeConfig, SeasonColorUtil,
    SeasonHooksClient, SeasonHooksTypes, SeasonHudCurrentSeason, SeasonHudHelper, XaeroTimeInfo, CropSeasonGrowHook,
    SeasonFertility, SeasonRainColumn, PrettyRainSeason, CampfireRain (check). Not reserved: SeasonedItemModel (food).
- Config sections in `config/creativeengineering-common.toml`: `[seasons]` (line 1991), `[time]` (2012, with
  `[time.wine]`, `[time.wine.boosts]`), `[crop_seasons]` (2586).
- KubeJS: `server_scripts/tags/cropseasontags.js` (in the row) and **`server_scripts/tags/seasontags.js`** (the season
  type tags; creative_calendar.md names "the season type tags" but not the file, the TODO row omits it).
  `server_scripts/_lib/dump_crop_growth.js` is a dev dump (can stay).
- Data the reserved code reads: `kubejs/generation_scripts/worldgen/crop_seasons.json`, `biome_traits.json` (also read
  by CD's `_worldgen/wild_crops.js`: CD keeps reading them, the files stay where they are).
- Third-party configs the mixins tune (stay where they are): `config/betterdays-common.toml`, `betterdays-client.toml`,
  `config/sereneseasons/`, `seasonhud-client.toml`.
- Soft dependency: Creative Brewing's Vinery `net/satisfy/vinery/forge/PackTime.java` names
  `com.SaintAlfonzo.creativeengineering.time.WineTime`.

### Build
1. Add the one-line "Reserved for Creative Calendar (owner, 2026-10-31)" header to each Java file and script listed
   above as it is next touched, or in one pass now (comment only; patcherMod jar unchanged until its next build).
2. creative_calendar.md 2: add `seasontags.js`, the generation data line and the third-party config line; settle the
   two "check" entries (IEConcreteSetTime: tied to day length, reserve; CampfireRain: campfires, recommend it stays).
3. Write the plan (creative_calendar.md new section 5): mod project `development/creativeCalendar` (plain mod, not a
   bundle: no members), mod id, load order below CD and CB, config key move (`[time]`, `[seasons]`, `[crop_seasons]`
   with keys and comments; `configs_edited/` copied the same session), the `PackTime` reference, and the phases (Java,
   then KubeJS, then config, as creative_brewing.md 4.2). File the plan as a new `creativecalendar` TODO row.

### Ids and data
No registry ids (the reserved systems register only commands and config). Command `/cetime` keeps its name.

### Decisions
- CampfireRain: **stays with the campfire features** in patcherMod (it is about campfires going out, not weather rules).
- IEConcreteSetTime: **reserve** (it exists because of the day-length master).
- Mark now or as touched: **now, one pass** (cheap, and it stops another move from taking a file).

### Gotchas
- patcherMod must not depend on Creative Calendar, but CD and CB will; CD's wild crops read crop seasons through
  `global` or the JSON, so the move must keep that interface.
- Seasons are Serene Seasons' saved state; moving our mixins changes no world data.
- A forwarding `WineTime` in patcherMod for one release, or a `PackTime` update in the same build.

### Tests
None for the reserve (docs and comments). The plan row adds a prefix and boot checks.
