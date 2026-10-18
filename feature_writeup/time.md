# Time system: day length, seasons, wine aging

One master section, `[time]` in `config/creativeengineering-common.toml`, sets the length of a day (Better Days), the
length of the seasons (Serene Seasons and SeasonHUD) and how fast wine ages (Backported Vinery). It also covers how
all three show up in the GUI. Every length is given in **world days**: one sunrise to the next, as the player sees it.

Test prefix: `TI-`. Related: [alcohol.md](alcohol.md) B4.5 (the vintage clock), [weather.md](weather.md) (season
types, SeasonHUD mixins), [weather_by_biome.md](weather_by_biome.md) (rain per sub-season).

## 0. At a glance

| Part | Status |
|---|---|
| Findings on today's three clocks (2) | VERIFIED 2026-10-18 from the decompiled jars and the config files on disk |
| Master `[time]` section and the push into the other mods (3.1, 3.2) | DESIGNED 2026-10-18, not built |
| Seasons that follow the sun (3.3) | DESIGNED, not built |
| Pause while the server is empty (3.4) | DESIGNED, not built |
| Wine aging lengths and boosts (3.5) | lengths BUILT in the Vinery port (B4.5, never booted); master link and new boosts DESIGNED |
| GUI integration (3.6) | DESIGNED, not built |
| Decisions | T1 to T7 open (section 4) |

**Files to open first:** `config/creativeengineering-common.toml` `[time]` (once built), `config/betterdays-common.toml`,
`<world>/serverconfig/sereneseasons-server.toml`, `<world>/serverconfig/seasonhud-server.toml`,
`config/vinery-common.toml` `[aging]` and `[wine]`.

**Knobs (proposed):** `dayMinutes`, `nightMinutes`, `daysPerSubSeason`, `wine.daysPerYear`, `wine.yearsPerLevel`,
`wine.cellarMultiplier`, `pauseWhenEmpty`.

**How to measure:** `/cetime` (proposed, 3.6) prints every length in ticks, world days and real time. Until it
exists: `/time query daytime` twice, a known number of real minutes apart; `/season` (Serene Seasons) for the sub-season
and day; `/wine time` (Vinery port) for the vintage clock.

## 1. Overview

Three mods each keep their own clock, and each is configured separately:

| Mod | What it counts | Config | Scope |
|---|---|---|---|
| Better Days 3.3.4.3 | the sun: overworld `dayTime`, at its own speed by day and by night | `config/betterdays-common.toml` (WhiteNoise config, not Forge) | per instance |
| Serene Seasons 8.1.0.24 | its own counter, `seasonCycleTicks`, saved per dimension | `<world>/serverconfig/sereneseasons-server.toml` | per world |
| SeasonHUD 2.0.4 | nothing; it reads Serene Seasons, but keeps its own copy of the lengths | `<world>/serverconfig/seasonhud-server.toml` | per world |
| Backported Vinery 1.5.5-ce | the vintage clock: real playtime (alcohol.md B4.5) | `config/vinery-common.toml` | per instance |

Today nothing ties these together. A "day" means something different in each one (section 2), and the per-world
files mean every new single-player world starts on Serene Seasons' defaults.

The plan: one `[time]` section is the only place lengths are set. patcherMod derives every other mod's value from it
and pushes it in at startup, so the four files above stop being edited by hand.

## 2. Reference: what the three clocks do today

### 2.1 Better Days (the sun)

From `betterdays/time/TimeService.java` and `config/ConfigHandler.java`:

- Every server tick, `TimeService.tick()` adds `speed` to `dayTime`, keeping the fraction in an accumulator. It then
  subtracts 1, because vanilla's `tickTime` adds 1 afterwards. It only runs while `doDaylightCycle` is on.
- Day = from `dayStart` to `nightStart` (wrapping past 24000); the rest is night.
- `speedMethod`:
  - `RATIO` (the pack's setting) uses `daySpeed` and `nightSpeed` as given.
  - `MINUTES` works out the speed as `10 / minutes`. That assumes a 12,000-unit half day, so with any other
    `dayStart` / `nightStart` the real length is off. With the pack's 14,700-unit day, `daySpeedMinutes = 98` would
    give 120 min, not 98. The master therefore always writes `RATIO` speeds (3.2).
- `dayStart` and `nightStart` are read **once**, when `TimeService` loads (static final). The speeds are read every tick.
- Sleeping runs time at `sleepSpeedMax` (110) until morning.

**Pack values:** `dayStart` 22300, `nightStart` 13000, `daySpeed` 0.125, `nightSpeed` 1.0.

| Half | dayTime units | Speed | Ticks | Real time |
|---|---|---|---|---|
| Day | 14,700 | 0.125 | 117,600 | 98.0 min |
| Night | 9,300 | 1.0 | 9,300 | 7.75 min |
| **World day** | 24,000 | | **126,900** | **105.75 min (1.76 h)** |

### 2.2 Serene Seasons (the season counter)

From `sereneseasons/handler/season/SeasonHandler.java` and `TimeSkipHandler.java`:

- `seasonCycleTicks` goes up by **1 per tick**, in every dimension, at END phase. It stops when
  `progress_season_while_offline = false` and nobody is online, or when the `doSeasonCycle` gamerule is off.
- `TimeSkipHandler` also adds any `dayTime` jump **bigger than 1 per tick** (START phase). That is how sleeping and
  `/time add` move the seasons.
- Day number = `seasonCycleTicks / day_duration`. Sub-season = `day_duration x sub_season_duration` ticks, 12
  sub-seasons per year.
- So Serene Seasons' "day" is a fixed number of real ticks. It never looks at the sun's speed.

**Gotcha found:** with a Better Days speed above 1, `dayTime` moves more than 1 per tick, so `TimeSkipHandler` adds the
jump on top of the normal +1. At `daySpeed` 2.0 the seasons would run 1.5 times as fast as the sun. The pack's speeds
are 1.0 or less today, so it doesn't happen yet. The design in 3.3 removes it.

**Pack values (they differ per world):**

| World | `day_duration` | `sub_season_duration` | Offline progress | One SS day | Sub-season | Year |
|---|---|---|---|---|---|---|
| Server, `Workspace` (`configs_edited/server_config`) | 192,000 | 10 | false | 160 min | 26.7 h | 320 h |
| Single-player `WorldA` to `WorldG` (SS defaults) | 24,000 | 8 | true | 20 min | 2.7 h | 32 h |

Against the world day of 105.75 min:

| World | SS days per world day | Sub-season in world days | Year in world days |
|---|---|---|---|
| Server | 0.66 | 15.1 | 181.6 |
| Single player | 5.29 | 1.5 | 18.2 |

So SeasonHUD's "Day 3" means a different thing on the server and in single player, and neither one matches the sun.
On the server, SS days are longer than world days. In single player, about five SS days pass each world day. 192,000
looks like it came from Better Days' `LUNAR_CYCLE_TICKS` (8 x 24,000), or from a day speed of 0.125 on both halves.

There is no `defaultconfigs/sereneseasons-server.toml`, which is why new single-player worlds get 24,000 / 8.

### 2.3 SeasonHUD

From `club/iananderson/seasonhud/forge/platform/ForgeSeasonHelper.java`:

- The season and day come from Serene Seasons' state (`ISeasonState.getDay()`).
- The **length** comes from SeasonHUD's own `sub_season_length` whenever it differs from Serene Seasons'
  `sub_season_duration`. If the two disagree, "Day N" and the totals on the HUD are wrong.
- `day_length` is only used for Fabric Seasons, but the pack keeps it in step anyway.
- `need_calendar = false`: the HUD shows without a calendar in the inventory.
- The pack's `SeasonHudHelperMixin` / `SeasonHudCurrentSeasonMixin` (weather.md) replace the name, icon and day for
  arid, frozen and permanent-season biomes. They read the same lengths, so they follow the master too.

### 2.4 Vinery port (the vintage clock)

From `development/backportedVinery/.../core/wine/VintageClock.java` and `WineYears.java` (alcohol.md B4.5):

- `agingTicks` goes up by 1 per tick while a player is online (`clockMode = PLAYTIME`). Sleeping, `/time` and Better
  Days don't touch it.
- One aging day = `playtimeHoursPerYear x 72000 / daysPerYear` ticks (`dayUnit = AGING`), or a world day
  (`dayUnit = WORLD`): `worldDayMinutes`, or measured from the `dayTime` pace when that is 0.
- Effect level = years / `yearsPerEffectLevel`, at most `maxLevel`.
- **The one boost today:** a wine in a storage block with **no sky light** at its position ages at
  `cellarAgingMultiplier` (1.5). The rate is fixed when the bottle goes in (`storageRate` in the `WineYear` NBT).
- Clients get the clock and settings in `VintageClockPacket` (on login and every 5 s), so tooltips, JEI and WTHIT
  use the server's numbers.

**Pack values:** 16 days per year, 2 years per level, max level 5, `playtimeHoursPerYear` 28.2, `dayUnit` AGING.

| Step | Normal | Cellar (x1.5) |
|---|---|---|
| One aging day | 105.75 min | 70.5 min |
| One year (16 days) | 28.2 h | 18.8 h |
| One level (2 years) | 56.4 h | 37.6 h |
| Max level 5 (10 years) | 282 h | 188 h |

28.2 h was picked to match one world day exactly (16 x 105.75 min), so the vintage clock is the only one of the three
that already agrees with the sun. It agrees only as long as nobody changes Better Days' speeds.

## 3. What the pack changes (design)

### 3.1 The master section

One section in `creativeengineering-common.toml`. It shows in Create's Configs screen like the rest of CEConfig
(3.6). Every length is in world days, and one world day is `dayMinutes + nightMinutes`.

```toml
[time]
	#Master switch. Off = Better Days, Serene Seasons, SeasonHUD and the Vinery port use their own files again.
	enabled = true
	#No player online on a dedicated server: the sun, the seasons and wine aging all stop.
	pauseWhenEmpty = true

	[time.day]
		#Real minutes from dayStart to nightStart, and from nightStart to the next dayStart.
		dayMinutes = 98.0
		nightMinutes = 7.75
		#dayTime of sunrise and sunset. Read once at startup (Better Days caches them).
		dayStart = 22300.0
		nightStart = 13000.0

	[time.seasons]
		#SUN: the seasons count world days, sleeping and /time included (3.3). TICKS: Serene Seasons' own counter.
		clock = "SUN"
		#World days per sub-season. 12 sub-seasons per year.
		daysPerSubSeason = 10
		#1 = Early Spring ... 12 = Late Winter, for new worlds.
		startingSubSeason = 1

	[time.wine]
		#PLAYTIME: ages while a player is online, sleeping doesn't age wine. SUN: ages with the world days.
		clock = "PLAYTIME"
		#World days per wine year, and wine years per effect level.
		daysPerYear = 16
		yearsPerLevel = 2
		maxLevel = 5

		[time.wine.boosts]
			#Storage with no sky light.
			cellarMultiplier = 1.5
			#More boosts: see 3.5 and decision T5.
```

Keys that aren't about time (sleep speeds and messages, Better Days' time effects, season aesthetics, biome lists, wine
effects) stay in their own mods' files.

### 3.2 Derived values and how they get in

| Target | Value | Formula | Pack value | How it is set |
|---|---|---|---|---|
| Better Days | `speedMethod` | always RATIO | RATIO | mixin on the getters (below) |
| Better Days | `daySpeed` | `dayUnits / (dayMinutes x 1200)`, `dayUnits = (nightStart - dayStart) mod 24000` | 14,700 / 117,600 = **0.125** | mixin |
| Better Days | `nightSpeed` | `(24000 - dayUnits) / (nightMinutes x 1200)` | 9,300 / 9,300 = **1.0** | mixin |
| Better Days | `dayStart`, `nightStart` | copied | 22300, 13000 | mixin |
| Serene Seasons | `day_duration` | SUN: 24000 (one `dayTime` day). TICKS: `(dayMinutes + nightMinutes) x 1200` | 24000 (SUN) / 126,900 (TICKS) | `ServerConfig.dayDuration.set()` |
| Serene Seasons | `sub_season_duration` | `daysPerSubSeason` | 10 | `ServerConfig.subSeasonDuration.set()` |
| Serene Seasons | `starting_sub_season` | copied | 1 | set |
| Serene Seasons | `progress_season_while_offline` | `!pauseWhenEmpty` | false | set |
| SeasonHUD | `day_length`, `sub_season_length` | same as Serene Seasons | 24000, 10 | `SeasonHudServer` values `.set()` |
| Vinery | `clockMode` | from `wine.clock`: PLAYTIME or WORLD_TIME | PLAYTIME | `VineryConfig.CLOCK_MODE.set()` |
| Vinery | `dayUnit`, `worldDayMinutes` | WORLD, `dayMinutes + nightMinutes` (exact, never measured) | WORLD, 105.75 | set |
| Vinery | `daysPerYear`, `yearsPerEffectLevel`, `maxLevel`, `cellarAgingMultiplier` | copied | 16, 2, 5, 1.5 | set |

**Better Days:** its config is WhiteNoise, not Forge, so values can't be set from outside the normal way. A `@Pseudo`
mixin on the static getters of `betterdays.config.ConfigHandler$Common` (`daySpeed`, `nightSpeed`, `dayStart`,
`nightStart`) returns the derived values while `[time] enabled`. `betterdays-common.toml` still shows its own numbers,
which are ignored. The startup log says so, with both sets of values. `dayStart` / `nightStart` changes need a
restart, because `TimeService` caches them.

**Serene Seasons and SeasonHUD:** both are Forge SERVER configs, loaded per world just before `ServerAboutToStartEvent`.
In that event patcherMod calls `.set()` on each value. That saves the world's file, and Forge sends the saved file to
every client at login, so the HUD sees the same numbers. The fields are public statics (`sereneseasons.config.ServerConfig`,
`club.iananderson.seasonhud.config.SeasonHudServer`), reached by reflection, so patcherMod gets no compile dependency.
Old worlds get the master values on their next start, and new worlds get them on their first.

**Vinery port:** the same `.set()` on `net.satisfy.vinery.forge.VineryConfig` values on `ServerAboutToStartEvent`,
again by reflection, like `VineryJEI` already does. The port's `VintageClockPacket` carries them to clients.

**Live edits:** a change through Create's Configs screen fires CEConfig's reload, which pushes again. Better Days
speeds and every Vinery value apply at once. Serene Seasons / SeasonHUD lengths apply at once on the server, but a
connected client keeps the values it got at login until it rejoins (Forge doesn't re-sync server configs). Decision T7.

Each push logs any value it changed, old -> new, the same way `global.NCH.cfg` does.

### 3.3 Seasons that follow the sun (`seasons.clock = SUN`)

Serene Seasons' +1 per tick can't match a day whose speed changes between day and night, and it double counts above
speed 1 (2.2). In SUN mode:

- A mixin cancels the +1 in `SeasonHandler.onWorldTick`. `TimeSkipHandler.onWorldTick`'s `difference > 1` becomes
  `difference > 0`, so **every** forward `dayTime` step goes into `seasonCycleTicks`. Backward steps (`/time set` to
  an earlier time) are ignored, as Serene Seasons already does.
- `day_duration` is pushed as 24000, so one Serene Seasons day is exactly one sun day, whatever the speeds.
  Sleeping, `/time add` and Better Days' speeds all move the seasons exactly as far as they move the sun.
- The wrap, clamp and the 20-tick `sendSeasonUpdate` stay as Serene Seasons has them.
- Serene Seasons' day ticks over at `dayTime` 0 (6:00), about 1,700 units after Better Days' sunrise at 22300. An
  offset of `24000 - dayStart` lines the day change up with sunrise. Proposed in, low risk.
- **Existing worlds:** on the first start in SUN mode, the counter is set to keep the current sub-season and day
  number, with the time inside the day taken from `dayTime`. The world is marked in SavedData, so this runs only once.
  Changing `daysPerSubSeason` later re-scales the date (day 15 of 30 becomes day 5 of 10 when 30 -> 10) because Serene
  Seasons stores ticks, not days. That is the same as editing its toml today.
- `TICKS` mode keeps Serene Seasons' own counter, with `day_duration` = the world day in ticks (126,900). It stays in
  step only while nobody sleeps, and it has the speed-above-1 gotcha.

**Season lengths in real time (SUN, world day 105.75 min):**

| `daysPerSubSeason` | Sub-season | Season | Year | Note |
|---|---|---|---|---|
| 3 | 5.3 h | 15.9 h | 63.5 h | |
| 5 | 8.8 h | 26.4 h | 105.8 h | a season is about one wine year |
| 8 | 14.1 h | 42.3 h | 169.2 h | Serene Seasons' default count |
| **10** | **17.6 h** | **52.9 h** | **211.5 h** | today's server count, now in world days |
| 15 | 26.4 h | 79.3 h | 317.3 h | today's server real-time pace (26.7 h per sub-season) |

Sleeping cuts at most the night, 7.75 of 105.75 min, so a player who sleeps every night sees a year about 7% shorter.
Rain lengths and frequencies (weather.md 4.2) are per real time and don't depend on this.

### 3.4 Pause while the server is empty

Vanilla 1.19.2 keeps ticking `dayTime` on an empty dedicated server, and so does Better Days. Serene Seasons stops
(`progress_season_while_offline = false`) and so does the vintage clock (PLAYTIME). On a server that sits empty
overnight, the sun runs ahead of both.

With `pauseWhenEmpty = true`: while `server.getPlayerCount() == 0`, a mixin cancels Better Days' `TimeService.tick()`
and vanilla's `ServerLevel.tickTime()` for every level. No gamerule is changed, so a crash can't leave
`doDaylightCycle` off. Weather and random ticks keep running. Single player is unaffected: the integrated server pauses
with the game.

### 3.5 Wine aging: lengths and boosts

**Lengths:** with `dayUnit = WORLD` and `worldDayMinutes` pushed as the exact world day, the port's numbers become
world days, and a year is `daysPerYear` sun days long however the day speeds change. Today's values give the same
28.2 h per year as now (2.4 table), so no wine changes age when this ships.

`wine.clock`:
- `PLAYTIME` (recommended, B4.5): ages while a player is online. Sleeping doesn't age wine. A year of 16 "days" is 16
  world days of play, about 15 days of sun for a player who sleeps every night.
- `SUN` (the port's `WORLD_TIME`): ages with `dayTime` like the seasons. Sleeping and `/time` age wine. This is the
  1.3.10 behaviour, and the reason B4.5 moved away from it.

**Boosts:** the boost is fixed when the bottle goes into storage (`storageRate`), so a boost has to depend only on the
position, not on something that changes while the bottle sits there.

| Boost | Rule | Default | Fits the fixed-rate model | Recommendation |
|---|---|---|---|---|
| Cellar (built) | no sky light at the storage position | x1.5 | yes | keep |
| Cold cellar | biome base temperature below 0.3 at the position | x1.25 | yes | optional (T5) |
| Deep cellar | cellar and y below 0 | x1.25 | yes | optional (T5) |
| Storage type | big wine rack / wine box faster than a placed bottle or shelf | x1.1 | yes | optional (T5) |
| Season | autumn / winter faster | x1.25 | **no**: the season changes while the bottle sits, so the rate would have to be split per sub-season and unloaded chunks would need to replay the calendar | rejected for now |
| Better Days sleep / time speed | none | | | rejected: B4.5 keeps sleep out of aging |

Boosts multiply, with a cap of `maxStorageMultiplier` (proposed 2.5). `ageOnlyInStorage` (false) stays in the port's
own file. The WTHIT line, `/wine info` and the tooltip already show "cellar x1.5". They would show the combined rate
and name each boost: "cellar x1.5, cold x1.25 = x1.88".

### 3.6 GUI integration

| Where | Today | Planned |
|---|---|---|
| **Create's Configs screen** (CEConfig, Common tab) | no time settings | the `[time]` section, edited in game. Single player applies at once (3.2 live edits); on a server, Serene Seasons / SeasonHUD numbers reach clients on rejoin |
| **SeasonHUD** (top left) | "Early Spring (Day 3)", `SHOW_DAY` | `enable_show_day = "SHOW_WITH_TOTAL_DAYS"` ("Day 3/30"), since the length is now a pack setting. Days are world days, so the count changes at sunrise. Optional: the time of day next to it (T6) |
| **Serene Seasons calendar** item | its own season screen; not needed for the HUD (`need_calendar = false`) | tooltip lines: "Day 3 of 30, Early Spring. Mid Spring in 7 days (about 12 h of play)". Real time comes from the same formulas as `/cetime` |
| **Better Days bed clock** | shown while sleeping (`displayBedClock`) | unchanged |
| **Wine tooltip, JEI Wine Aging page, WTHIT lines** (port) | read the vintage clock snapshot; "days" are aging days | unchanged code. Once the master pushes `dayUnit = WORLD`, "4 days" means four sunrises of play. The JEI page shows the year and level in real time from the pushed values |
| **`/cetime`** (new, op 0) | none | one chat table: world day (day / night minutes, speeds, current time of day), season (sub-season, day x/y, real time to the next sub-season and to the next year, clock mode), wine (days per year, real time per year and per level, cellar and other boosts, clock mode), and which values the master pushed and which file each went to |
| **`/cetime set day|season|wine ...`** (op 2) | none | shortcut that edits the `[time]` values and pushes them, for testing |

## 4. Limits and decisions

| # | Decision | Options | Recommendation |
|---|---|---|---|
| T1 | Where the master lives | A `[time]` in `creativeengineering-common.toml`; B its own `config/creativeengineering-time.toml` | **A**: it already shows in Create's Configs screen and is backed up with the rest of CEConfig. B is the same code with a second spec, and Create's screen shows only one common file per mod |
| T2 | Sub-season length | 3.3 table | **10 world days** (17.6 h, 211.5 h a year) keeps the server's count. 15 keeps its real-time pace. Single-player worlds go from 32 h years to the chosen length either way |
| T3 | Season clock | SUN / TICKS | **SUN**: the HUD day matches the sun, sleeping counts, no speed-above-1 bug |
| T4 | Pause when empty | on / off | **on** for the dedicated server: sun, seasons and wine stop together |
| T5 | Wine boosts beyond the cellar | cold, deep, storage type; season rejected | the user's call. Recommended: keep the cellar only until the base lengths have been played, then add cold cellar |
| T6 | Time of day on the HUD | none / "14:20" after the season / a separate line | none for now. Optional: a mixin on SeasonHUD's text, the clock read from `dayTime` (6:00 at 0) |
| T7 | Season length edited mid-session on a server | rejoin needed / send our own sync packet | **rejoin**, said in the config comment. A packet would also have to update SeasonHUD's client-side copy |

**Rejected:**
- *Generate the four files from a JSON with a `tools/` script:* Serene Seasons and SeasonHUD are per-world files, so
  every existing world would need its own rewrite, and a value edited in the toml would quietly drift from the JSON.
  Pushing at server start covers old and new worlds and a running game.
- *Better Days `MINUTES` mode:* wrong length with the pack's `dayStart` / `nightStart` (2.1).
- *Compute the season straight from `dayTime`:* `/time set` to an earlier time would run the seasons backwards, and
  `/season set` would stop working.
- *Tie the wine year to the Serene Seasons year:* one wine year would be 120 world days at T2 = 10, and the max level
  would need over 2,000 h. Left as the "later" `SEASONS` mode from B4.5.

**Not covered here (other time-based systems):**
- Wandering Trader / Winemaker intervals: real server ticks, CEConfig `[wandering_trader]`, unaffected by day length.
- Crop growth: random ticks per real tick (`randomTickEffect = NEVER`). A world day of 105.75 min gives crops about
  5.3 times vanilla's growth per day. Serene Seasons' fertility is per sub-season.
- Brewin' keg fermentation: machine ticks, no calendar.

## 5. File map

| File | Role |
|---|---|
| [../config/creativeengineering-common.toml](../config/creativeengineering-common.toml) | master `[time]` (planned) |
| [../config/betterdays-common.toml](../config/betterdays-common.toml) | Better Days; time values ignored while `[time] enabled` |
| [../config/betterdays-client.toml](../config/betterdays-client.toml) | bed clock position |
| [../config/seasonhud-client.toml](../config/seasonhud-client.toml) | HUD position and day display |
| `../saves/<world>/serverconfig/sereneseasons-server.toml`, `seasonhud-server.toml` | per-world, written by the push |
| [../configs_edited/server_config/sereneseasons-server.toml](../configs_edited/server_config/sereneseasons-server.toml), [seasonhud-server.toml](../configs_edited/server_config/seasonhud-server.toml) | backups; become reference copies of what the push writes |
| [../config/vinery-common.toml](../config/vinery-common.toml) | Vinery port `[aging]`, `[wine]` |
| [../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/VintageClock.java](../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/VintageClock.java) | vintage clock |
| [../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/WineYears.java](../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/WineYears.java) | wine age, storage rate, cellar boost |
| [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java) | `Time` section (planned) |
| `../development/patcherMod/.../time/TimeSystem.java` | push, SUN counter, pause, `/cetime` (planned) |
| `../development/patcherMod/.../mixin/BetterDaysConfigMixin.java`, `SeasonCounterMixin.java`, `TimePauseMixin.java` | planned |
| [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SeasonHudHelperMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SeasonHudHelperMixin.java) | season-type HUD day; reads the same lengths |

## 6. Offline checks

- `tools/time_table.py` (planned): reads `[time]` from the toml, prints the 2.x / 3.x tables (Better Days speeds,
  Serene Seasons and SeasonHUD values, wine real times) and exits 1 if a derived value is out of a target's range
  (Better Days speed 0 to 24000, `sub_season_duration` at least 1). Add it to `tools/generators.json` as `time_table`.
- Headless Rhino is not needed: it is all Java. A GameTest for the SUN counter: set `dayTime` steps of 0.125, 1 and 110
  per tick and check that `seasonCycleTicks` moves by exactly the same amount.

## 7. In-game test suite

All pending: nothing is built yet.

- [ ] **TI-01 Push on start:** set `[time]` to the pack values, start a world -> the log lists the pushed values;
  `serverconfig/sereneseasons-server.toml` shows `day_duration = 24000`, `sub_season_duration = 10`;
  `seasonhud-server.toml` matches; `/wine time` shows 105.75 min per day
- [ ] **TI-02 Day length:** `/time set 22300`, time real minutes until `/time query daytime` reads 13000 -> 98 min (±1);
  then to 22300 again -> 7.75 min
- [ ] **TI-03 Speed change:** set `dayMinutes = 49` in Create's Configs screen -> the sun moves twice as fast at once;
  `/cetime` shows `daySpeed` 0.25
- [ ] **TI-04 Seasons follow the sun:** `/season set early_spring`, sleep through a night -> SeasonHUD day goes up by
  exactly 1 at sunrise; `/time add 24000` -> one more day
- [ ] **TI-05 No double count:** `dayMinutes = 5` (speed above 1), play one world day -> SeasonHUD day goes up by
  exactly 1
- [ ] **TI-06 Old world migration:** open `Workspace` (192000 / 10) after the change -> same sub-season and day number
  as before; the second start doesn't move it again
- [ ] **TI-07 New single-player world:** create a world -> 24000 / 10, not Serene Seasons' 24000 / 8
- [ ] **TI-08 HUD totals:** `enable_show_day = SHOW_WITH_TOTAL_DAYS` -> "Day x/30" in temperate biomes; arid / frozen
  biomes count through the 120-day year (SeasonHudHelperMixin)
- [ ] **TI-09 Pause when empty:** dedicated server, note `/time query daytime`, `/season`, `/wine time`, everyone leaves
  for 5 min -> all three unchanged on rejoin
- [ ] **TI-10 Wine lengths unchanged:** a wine aged before the change shows the same age and level after it
- [ ] **TI-11 Wine in world days:** `/wine clock add 1 days` -> the tooltip age goes up by one day; the JEI Wine Aging
  page shows 28.2 h per year
- [ ] **TI-12 Cellar boost:** wine in a rack with no sky light -> WTHIT and tooltip show x1.5, `/wine info` shows the
  shorter time to the next level
- [ ] **TI-13 Master off:** `[time] enabled = false`, restart -> Better Days uses its own toml again (check with TI-02),
  Serene Seasons files are left as last written
- [ ] **TI-14 Server sync:** dedicated server with `daysPerSubSeason = 5`, fresh client joins -> HUD shows "Day x/15"
- [ ] **TI-15 Calendar tooltip:** hover the Serene Seasons calendar -> day, sub-season and the real time to the next
  sub-season, matching `/cetime`
- [ ] **TI-16 `/cetime`:** run it -> every row matches the 3.2 table for the current values

## 8. History

- 2026-10-18: writeup created. Read Better Days 3.3.4.3, Serene Seasons 8.1.0.24 and SeasonHUD 2.0.4 decompiled, plus the
  Vinery port's vintage clock. Findings: the world day is 105.75 min; Serene Seasons' day is 160 min on the server and
  20 min in single player; SeasonHUD keeps its own length copy; speeds above 1 double count seasons; Better Days'
  MINUTES mode is wrong with the pack's sunrise / sunset; nothing pauses the sun on an empty server. Designed the
  `[time]` master, the push, SUN seasons, pause-when-empty, wine boost options and GUI integration. Decisions T1 to T7
  open. Nothing built.
