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
| Master `[time]` section and the push into the other mods (3.1, 3.2) | BUILT 2026-10-18 in patcherMod 1.0.8, DEPLOYED with pack 1.0.8 (2026-10-19 10:20); the push and the sun migration ran in single player 2026-10-19 (`[time]` log lines for every mod and dimension), not tested in game; changes from the design in 3.8 |
| Seasons that follow the sun (3.3) | BUILT 2026-10-18, DEPLOYED with 1.0.8, not tested |
| Pause while the server is empty (3.4) | BUILT 2026-10-18, DEPLOYED with 1.0.8, not tested |
| Wine aging lengths and boosts (3.5) | Lengths BUILT and pushed from the master. Boosts beyond the cellar (T5): cold x2.0 / warm x0.75 from Brewin' keg temperature BUILT 2026-10-19 ([alcohol.md](alcohol.md) 10.13, cellar now x2.0; Brewin' `-03` in the local `mods/` since 2026-10-20), not deployed, not tested; deep cellar and storage type not built (TODO `alcohol/pack #2`) |
| Wine months, sun clock, x2.5 dark, calendar (3.7) | BUILT 2026-10-18, DEPLOYED with 1.0.8, not tested: port side in the Vinery port `-02` ([alcohol.md](alcohol.md) B7), calendar tooltip and "7:30 pm" clock in patcherMod |
| GUI integration (3.6) | BUILT 2026-10-18, DEPLOYED with 1.0.8, not tested: `[time]` in Create's Configs screen, SeasonHUD "Day x/24" + time of day, calendar tooltip and action bar, `/cetime` |
| Offline checks (6) | not built: `tools/time_table.py`, the SUN-counter GameTest |
| Presets (3.9) | BUILT 2026-10-18, DEPLOYED with 1.0.8, not tested: SINGLEPLAYER (default) / SERVER / CUSTOM |
| Admin date commands, year fix (3.11) | BUILT 2026-10-30 in patcherMod, installed 2026-10-31 00:44; scratch server PASSED r1031 (2026-10-31, SQ-TD-01 to -03, -05, SQ-GF31-04 = TI-37 server half); SQ-TD-04 (wine clock, `/wine time` prints bare lang keys on a dedicated server) client check pending; `add` takes singular units only, TI-32 writes `days`; TI-32 to TI-37 in game pending (TODO `gameplay/time #6`) |
| Decisions | DECIDED: T1 (A), T2, T3 (SUN), T4 (on), T6, T7 (rejoin), T8, T9, T10, T12, T5 (2026-10-19: cold / warm, 3.5); T11 dropped |

**Files to open first:** `config/creativeengineering-common.toml` `[time]` (once built), `config/betterdays-common.toml`,
`<world>/serverconfig/sereneseasons-server.toml`, `<world>/serverconfig/seasonhud-server.toml`,
`config/vinery-common.toml` `[aging]` and `[wine]`.

**Knobs (proposed):** `dayMinutes`, `nightMinutes`, `daysPerSubSeason` (also the length of a wine month),
`wine.monthsPerLevel`, `wine.cellarMultiplier`, `pauseWhenEmpty`.

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
		dayMinutes = 45.0
		nightMinutes = 15.0
		#dayTime of sunrise and sunset. Read once at startup (Better Days caches them).
		dayStart = 22300.0
		nightStart = 13000.0

	[time.seasons]
		#SUN: the seasons count world days, sleeping and /time included (3.3). TICKS: Serene Seasons' own counter.
		clock = "SUN"
		#World days per sub-season. 12 sub-seasons per year.
		daysPerSubSeason = 8
		#1 = Early Spring ... 12 = Late Winter, for new worlds.
		startingSubSeason = 1

	[time.wine]
		#SUN: ages with the world days, sleeping and /time included (3.7). PLAYTIME: ages while a player is online.
		clock = "SUN"
		#A wine month is one sub-season (daysPerSubSeason world days). Months per effect level.
		monthsPerLevel = 3
		maxLevel = 5

		[time.wine.boosts]
			#Storage with no sky light.
			cellarMultiplier = 2.0
			#Cold storage (alcohol.md 10.13); multiplies with the cellar.
			coldMultiplier = 2.0
			warmMultiplier = 0.75
			#More boosts: see 3.5 and decision T5.
```

Keys that aren't about time (sleep speeds and messages, Better Days' time effects, season aesthetics, biome lists, wine
effects) stay in their own mods' files.

### 3.2 Derived values and how they get in

| Target | Value | Formula | Pack value | How it is set |
|---|---|---|---|---|
| Better Days | `speedMethod` | always RATIO | RATIO | mixin on the getters (below) |
| Better Days | `daySpeed` | `dayUnits / (dayMinutes x 1200)`, `dayUnits = (nightStart - dayStart) mod 24000` | 14,700 / 54,000 = **0.272** | mixin |
| Better Days | `nightSpeed` | `(24000 - dayUnits) / (nightMinutes x 1200)` | 9,300 / 18,000 = **0.517** | mixin |
| Better Days | `dayStart`, `nightStart` | copied | 22300, 13000 | mixin |
| Serene Seasons | `day_duration` | SUN: 24000 (one `dayTime` day). TICKS: `(dayMinutes + nightMinutes) x 1200` | 24000 (SUN) / 72,000 (TICKS) | `ServerConfig.dayDuration.set()` |
| Serene Seasons | `sub_season_duration` | `daysPerSubSeason` | 8 | `ServerConfig.subSeasonDuration.set()` |
| Serene Seasons | `starting_sub_season` | copied | 1 | set |
| Serene Seasons | `progress_season_while_offline` | `!pauseWhenEmpty` | false | set |
| SeasonHUD | `day_length`, `sub_season_length` | same as Serene Seasons | 24000, 8 | `SeasonHudServer` values `.set()` |
| Vinery | `clockMode` | from `wine.clock`: SUN -> WORLD_TIME, PLAYTIME -> PLAYTIME | WORLD_TIME | the port reads `time.WineTime` (since 2026-10-27; was `VineryConfig.CLOCK_MODE.set()`) |
| Vinery | `dayUnit`, `worldDayMinutes` | WORLD, `dayMinutes + nightMinutes` (exact, never measured) | DAYTIME (3.7) | read by the port |
| Vinery | `daysPerMonth` (was `daysPerYear`), `monthsPerEffectLevel`, `maxLevel`, `cellarAgingMultiplier` | `daysPerSubSeason`, then copied (3.7) | 8, 1 or 3 (preset, 3.9), 5, 2.0 | read by the port (also `cold` / `warmAgingMultiplier`) |

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

**Vinery port (since 2026-10-27, [pack_hygiene.md](pack_hygiene.md) finding 39):** nothing is pushed. patcherMod has
a small public class `time/WineTime` (`active()`, `sunClock()`, `worldDayMinutes()`, `daysPerMonth()`,
`monthsPerLevel()`, `maxLevel()`, `cellar` / `cold` / `warmMultiplier()`), and the port's `forge/PackTime` calls it when
patcherMod is loaded (soft dependency, `compileOnly project(':patcherMod')`). The port's `VineryConfig` getters
(`clockMode()`, `dayUnit()`, `daysPerYear()` and on) return the master's values while `[time] enabled`, else the port's
own keys. So `vinery-common.toml` is never written by patcherMod, and its matching `[wine]` / `[aging]` keys are only
used with the master off or without patcherMod (their comments say so). The port's `VintageClockPacket` still carries
the values to clients. Until 2026-10-27 patcherMod set the `VineryConfig` values by reflection (`TimePush.pushVinery`).

**Live edits:** a change through Create's Configs screen fires CEConfig's reload, which pushes again. Better Days
speeds and every Vinery value apply at once (the port reads them live). Serene Seasons / SeasonHUD lengths apply at once on the server, but a
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
- `TICKS` mode keeps Serene Seasons' own counter, with `day_duration` = the world day in ticks (72,000 at 60 min). It stays in
  step only while nobody sleeps, and it has the speed-above-1 gotcha.

**Season lengths in real time (SUN, world day 60 min, your 3.7 values):**

| `daysPerSubSeason` | Sub-season | Season | Year | Note |
|---|---|---|---|---|
| 5 | 5 h | 15 h | 60 h | |
| **8** | **8 h** | **24 h** | **96 h** | your value: a season is one real day |
| 10 | 10 h | 30 h | 120 h | |

Sleeping cuts at most the night, 15 of 60 min, so a player who sleeps every night sees a year up to 25% shorter.
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

**2026-10-18:** your B7 list changes the lengths and the clock: months, the sun clock and x2.5 in the dark (3.7).
The boost rules below still apply; the PLAYTIME recommendation and the 16-day year are replaced.

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
| Cellar (built) | no sky light at the storage position | x2.0 since 10.13 (x2.5 from your B7 list; was x1.5) | yes | keep |
| Cold cellar | biome base temperature below 0.3 at the position | x1.25 | yes | optional (T5) |
| Deep cellar | cellar and y below 0 | x1.25 | yes | optional (T5) |
| Storage type | big wine rack / wine box faster than a placed bottle or shelf | x1.1 | yes | optional (T5) |
| Season | autumn / winter faster | x1.25 | **no**: the season changes while the bottle sits, so the rate would have to be split per sub-season and unloaded chunks would need to replay the calendar | rejected for now |
| Better Days sleep / time speed | none | | | rejected: B4.5 keeps sleep out of aging |

Boosts multiply, with a cap of `maxStorageMultiplier` (proposed 2.5). `ageOnlyInStorage` (false) stays in the port's
own file. The WTHIT line, `/wine info` and the tooltip already show "cellar x1.5". They would show the combined rate
and name each boost: "cellar x1.5, cold x1.25 = x1.88".

**2026-10-19 (your list, [alcohol.md](alcohol.md) 10.13), BUILT:** cellar is now x2.0 and a *cold* boost x2.0 exists,
but from blocks, not the biome: Brewin's keg temperature at the storage block (freeze sources minus heat sources
within 2 blocks, plus the biome and dimension) at chilly or colder. Both multiply with no cap (cold cellar x4.0), and
the labels name both: "sunny x1.0", "cellar x2.0", "cold sunny x2.0", "cold cellar x4.0". Warm (heat outweighs cold)
slows it: x0.75, "warm sunny x0.75", "warm cellar x1.5". `[time.wine.boosts] coldMultiplier`, `warmMultiplier`.
Deep cellar and storage type stay optional (T5).

### 3.6 GUI integration

| Where | Today | Planned |
|---|---|---|
| **Create's Configs screen** (CEConfig, Common tab) | no time settings | the `[time]` section, edited in game. Single player applies at once (3.2 live edits); on a server, Serene Seasons / SeasonHUD numbers reach clients on rejoin |
| **SeasonHUD** (top left) | "Early Spring (Day 3)", `SHOW_DAY` | `enable_show_day = "SHOW_WITH_TOTAL_DAYS"` ("Day 3/24"), since the length is now a pack setting. Days are world days, so the count changes at sunrise. Optional: the time of day next to it (T6) |
| **Serene Seasons calendar** item | its own season screen; not needed for the HUD (`need_calendar = false`) | tooltip and held action bar with season, day, year and time of day, replacing the removed Vinery calendar (3.7) |
| **Better Days bed clock** | shown while sleeping (`displayBedClock`) | unchanged |
| **Wine tooltip, JEI Wine Aging page, WTHIT lines** (port) | read the vintage clock snapshot; "days" are aging days | unchanged code. Once the master pushes `dayUnit = WORLD`, "4 days" means four sunrises of play. The JEI page shows the year and level in real time from the pushed values |
| **`/cetime`** (new, op 0) | none | one chat table: world day (day / night minutes, speeds, current time of day), season (sub-season, day x/y, real time to the next sub-season and to the next year, clock mode), wine (days per year, real time per year and per level, cellar and other boosts, clock mode), and which values the master pushed and which file each went to |
| **`/cetime set day|season|wine ...`** (op 2) | none | shortcut that edits the `[time]` values and pushes them, for testing |

### 3.7 Wine months and the calendar (alcohol.md B7.8)

Your Vinery lists of 2026-10-18 ([alcohol.md](alcohol.md) B7) set the time scale:

| What | Value | Where |
|---|---|---|
| Day length | about 45 min daylight + 15 min night: a 60 min world day | `dayMinutes = 45`, `nightMinutes = 15` |
| Sub-season | 8 world days = 8 h | `daysPerSubSeason = 8` |
| Season | 3 sub-seasons = 24 world days = one real day | derived |
| Year | 96 world days = 4 real days | derived |
| Wine month | one sub-season | derived |
| Wine level | +1 every 3 months (one season) | `wine.monthsPerLevel = 3` |
| Dark aging | x2.0 (x2.5 until 2026-10-19) | `wine.boosts.cellarMultiplier = 2.0` |
| Cold aging | x2.0, x4.0 with dark | `wine.boosts.coldMultiplier = 2.0` |
| Warm aging | x0.75, x1.5 with dark | `wine.boosts.warmMultiplier = 0.75` |
| Sleeping | ages wine and moves the seasons | `wine.clock = SUN`, `seasons.clock = SUN` |

Also asked for:
- "Year" becomes "month" in every wine text.
- Time of day shows as "6:00 am" / "7:30 pm".
- The Vinery calendar is removed, and the Serene Seasons calendar gets a season, day, year and time tooltip like the
  one the Vinery calendar had.
- Every time value in one file: the `[time]` master (3.1).

No conversions between versions: the ports are in development, so nothing below keeps old wines' ages or old items.

**Day length.**
- With the pack's `dayStart` 22300 / `nightStart` 13000 (14,700 day units, 9,300 night units), the push (3.2) gives
  Better Days `daySpeed` 14,700 / 54,000 = **0.272** and `nightSpeed` 9,300 / 18,000 = **0.517**.
- Both stay below 1, so the speed-above-1 double count (2.2) can't happen even in TICKS mode.
- Sleeping skips at most the 15 min night, so a player who sleeps every night sees days, seasons and wine run up to
  25% faster.

**A wine month is one Serene Seasons sub-season.**
- The year has 12 sub-seasons (Early Spring ... Late Winter), so they are the natural months.
- The port's `daysPerYear` / `yearsPerEffectLevel` / `durationPerYear` become `daysPerMonth` (pushed from
  `daysPerSubSeason`, not set by hand), `monthsPerEffectLevel` and `durationPerMonth`.
- `durationPerMonth` keeps today's number (200 ticks), so a max-level wine (15 months) reaches
  1800 + 15 x 200 = 4800 ticks. Raise `maxDuration` if 15000 should stay reachable.

**Wine ages on the sun clock** (`wine.clock = SUN`, the port's `WORLD_TIME`):
- `VintageClock.tick` already adds every forward `dayTime` step in `WORLD_TIME` mode. That is the same rule as the
  SUN season counter in 3.3, so one sunrise is one season day and one wine day.
- **Port change needed:** in `WORLD_TIME` the counter moves in `dayTime` units, so one day must be 24,000 units. Today
  the day length still comes from `dayUnit` in real ticks, which would make wine age about 3 times too slowly. Add a
  `DAYTIME` day unit, and use it whenever the clock is `WORLD_TIME`.
- `pauseWhenEmpty` (3.4) stops the sun, the seasons and wine together.
- This replaces B4.5's "sleeping doesn't age wine" and 3.5's PLAYTIME recommendation.

**Real time per step** (60 min world day, no sleeping):

| `daysPerSubSeason` | One month | +1 level (3 months) | +1 level in the dark (x2.5) | Max level 5 (15 months) | Max level in the dark |
|---|---|---|---|---|---|
| **8** (your value) | **8 h** | **24 h** | **9.6 h** | **120 h** | **48 h** |
| today (B4.5, 16-day years, 2 per level, x1.5) | | 56.4 h | 37.6 h | 282 h | 188 h |

- At your values a wine levels up once per real day, about 2.3 times as fast as today, and 3.9 times as fast in the
  dark.
- **Dark storage:** the rule stays "no sky light at the storage position", fixed when the bottle goes in. The other
  boosts in 3.5 (T5) stay optional. The cellar alone reaches the proposed `maxStorageMultiplier` of 2.5, so raise the
  cap if any are added.

**"Year" -> "month" everywhere.** Only display text changes; NBT field names and Java names stay.

| Where | Keys / place | How |
|---|---|---|
| Port's own lang (`vinery_backport`) | `tooltip.vinery_backport.age`, `command.vinery_backport.wine.time.units`, `jei.vinery_backport.wine_aging.year` / `.level`, `wthit.vinery_backport.wine` | edit the text |
| Upstream lang (in the 1.5.5 jar, can't be edited) | `hud.vinery.wine.age` ("Aged %s Years"), `command.vinery.wine.info.age`, `command.vinery.wine.time`, `vinery.configuration.daysPerYear` / `yearsPerEffectLevel` / `durationPerYear`, `advancement.vinery.vintage_perfection.desc` ("aged ... for 15 years") | same keys in the port's own `assets/vinery/lang/en_us.json`, our own wording, which layers above the donor jar (B4.8, check with VB-43) |
| Config | `[wine] daysPerYear`, `yearsPerEffectLevel`, `durationPerYear` | renamed as above; the master supplies `daysPerMonth` and `monthsPerEffectLevel` (the port reads `time.WineTime`) |
| `/wine clock add <n> years` | command argument | `months` (keep `days`) |
| `vintage_perfection` advancement | its age condition | read it from the 1.5.5 jar. If it checks years, the translator rewrites it to months |

**Time of day: "6:00 am" / "7:30 pm".**
- The clock is anchored to sunrise (T12, decided): Better Days' `dayStart` reads **6:00 am**. 1000 `dayTime` units are
  one hour, so `nightStart` (14,700 units later) reads **8:42 pm**.
- Formula: clock hours = ((`dayTime` - `dayStart`) mod 24000) / 1000 + 6. With the pack's `dayStart` 22300, that is
  vanilla's reading + 1:42, so `/time set 0` reads 7:42 am, not 6:00 am.
- The offset comes from the master's `dayStart`, so moving sunrise in `[time.day]` keeps sunrise at 6:00 am.
- The 45 real minutes of daylight cover 6:00 am to 8:42 pm, and the 15 min night covers the rest.
- 12-hour format, lower-case am / pm, to the minute. One clock minute passes about every 3 real seconds by day and
  every 1.5 s at night.
- One formatter in patcherMod, used by:
  - the SeasonHUD line (T6): "Mid Spring (Day 3/24) 7:30 pm"
  - the calendar tooltip (below)
  - `/cetime`
- Better Days' own bed clock and `/time query` keep vanilla's numbers. Only the pack's displays use this clock.

**Calendar.**
- **The Vinery calendar is removed from the port** (`LegacyCalendarBlock` / `LegacyCalendarItem`, a 1.3.10 keeper).
  - Unregister both, and drop them from the port's lists.
  - The upstream-pack layer leaves out the 1.3.10 data that names it: recipe, loot table, tags.
  - No replacement item; nothing converts.
  - `legacyYearLengthDays` was then only used for reading 1.3.10 wine `Year` NBT; that conversion and the key were
    removed 2026-10-27 (pack_hygiene.md finding 10), so a 1.3.10 wine's `Year` is ignored and it ages as a fresh wine.
- **The Serene Seasons calendar takes over its tooltip** (patcherMod, `ItemTooltipEvent` on `sereneseasons:calendar`):
  - Line 1: season and day: "Mid Spring, day 3 of 8".
  - Line 2: year and time: "Year 2, 7:30 pm".
  - Line 3 (grey): "Next: Late Spring in 5 days (about 5 h)".
  - The same two lines show in the action bar while the calendar is held, as the Vinery calendar did.
  - The date comes from Serene Seasons' state (`SeasonHelper.getSeasonState`): sub-season, day within it, and
    year = cycle ticks / year length + 1. Arid, frozen and permanent-season biomes use the names from
    `SeasonHudHelperMixin` (weather.md), so the calendar and the HUD agree.
- A wine "3 months old" is three calendar sub-seasons, one season.

### 3.8 Build status (2026-10-18, not tested in game)

patcherMod **1.0.8** (bumped: 1.0.7 shipped in the pack's 1.0.7, so a rebuild under that name wouldn't reach clients)
is in `mods/` and `server_mods/`. `creativeengineering-common.toml` gets `[time]` with the defaults on the next start.

| Part | Where | Changes from the design |
|---|---|---|
| `[time]` | `CEConfig.Time` | As 3.1, plus `hudTimeOfDay` (T6 as a switch). `startingSubSeason` 0 = random, as Serene Seasons |
| Better Days | `BetterDaysConfigMixin` (`@Pseudo`, the four getters), log line at server start | none |
| Push | `time/TimePush`: `ServerAboutToStartEvent` + every save of the toml (`ModConfigEvent.Reloading`), `/cetime push` | Serene Seasons and SeasonHUD are compiled against (both jars are in `libs/`); the Vinery port is not pushed to since 2026-10-27 (it reads `time/WineTime`). Each change is logged old -> new |
| SUN seasons | `time/SeasonClock`, `SereneSeasonsClockMixin` (server), `SereneSeasonsClientClockMixin` (client mixin since 2026-10-19: as one common mixin its `Minecraft` reference crashed dedicated servers), `SereneSeasonsTimeSkipMixin` | Serene Seasons' +1 per tick **and** its time-skip handler stand down, and patcherMod counts every forward dayTime step itself (the design changed the skip threshold to 0, which needs the same cancel anyway). It syncs every second and at once after a jump. **Client:** between syncs Serene Seasons' client counter also ran +1 per tick, so by day it ran ahead about 3.7 times and snapped back each second, with a full chunk re-render whenever that crossed a sub-season. In SUN mode it follows the client's own dayTime |
| Sunrise offset | `SeasonClock.migrate` | the counter is lined up with `dayStart` once per level, so the day changes at 6:00 am |
| Migration | `SeasonClock.Marker` (`data/creativeengineering_season_clock.dat` per level) | keeps sub-season and day number, read with Serene Seasons' lengths from **before** this start's push. Switching back to TICKS clears the marker, so a later switch migrates again |
| Pause | `BetterDaysTimePauseMixin` (whole tick) + `ServerLevelTimePauseMixin` | Vanilla's `tickTime` isn't cancelled, only its `setDayTime`: cancelling it would also stop game time, and scheduled block ticks run on game time |
| Time of day | `TimeSettings.clock` | as 3.7 (T12) |
| SeasonHUD | `SeasonHudCurrentSeasonMixin` appends the time; `config/seasonhud-client.toml` `enable_show_day = "SHOW_WITH_TOTAL_DAYS"` (backed up in `configs_edited/root_config/`) | not shown in dimensions with a fixed time (Nether, End) |
| Calendar | `time/TimeClient`, `time/CalendarDate` | Serene Seasons' own two lines stay (sub-season, "Day x/8"); added: "Year N, 7:30 pm", "Next: Late Spring in 5 days (about 5 h)", and "Here: Arid, no seasons" style lines for the season types. The action bar shows the same while it is held. **Year:** Serene Seasons wraps its counter every year and keeps no year count, so the year is the world's sunrises since dayTime 0, less the day of the year. After a `/season set` it can read one off until the next new year. It shows only on the SUN clock |
| `/cetime` | `time/TimeCommand` | as 3.6, plus `/cetime push` |
| Reference copies | `configs_edited/server_config/sereneseasons-server.toml` and `seasonhud-server.toml` | now hold the pushed values (24000 / 8), which new server worlds start from |

### 3.9 Presets (built 2026-10-18)

`[time] preset`: **SINGLEPLAYER** (the default), **SERVER** or **CUSTOM**. Your rule: the label follows the values.

| Preset | Values |
|---|---|
| SINGLEPLAYER | every `[time]` default, and `wine.monthsPerLevel = 1`: a wine levels up every sub-season (8 h at 60-min days) |
| SERVER | the same, but `wine.monthsPerLevel = 3`: one level per season (24 h) |
| CUSTOM | a label only: shown when the values match neither preset |

How it behaves (`time/TimePresets`, run when the toml loads, on every save and after `/cetime set`):
- **Picking** SINGLEPLAYER or SERVER (Configs screen, or the file while the game runs) writes that preset's values.
- **Editing a value** relabels the config: to the preset it now matches, or to CUSTOM. Changing `monthsPerLevel` from
  1 to 3 under SINGLEPLAYER therefore reads SERVER.
- **Picking CUSTOM** changes no value; if the values still match a preset, the label goes back to that preset.
- **At startup** the last state isn't known. If the field names a preset while the values match the other one, the
  field was changed with the game closed, so its values are written. Values matching no preset make it CUSTOM.
- Every change is logged (`[time] preset SINGLEPLAYER -> CUSTOM`, `[time] wine.monthsPerLevel 1 -> 3`). `/cetime`
  shows the preset.

**Deploy gotcha (open):** `tools/build_deploy.py` ships the same `config/` to the server and to the clients. A server
set to SERVER is put back to SINGLEPLAYER by the next payload, and the server's Vinery port then reads 1 month per
level from it. It needs a server-only copy of `creativeengineering-common.toml` in the payload, or a
per-side default. Not built.

### 3.10 Minimap time line and bed clock (2026-10-23, TODO time #4 / #5)

**Minimap line (built, TI-30).** Xaero's minimap info display shows SeasonHUD's line ("Mid Spring (Day 3/24) 7:30 pm")
and, one line below, Xaero's own time ("Day 57, 17:48"). Xaero used vanilla's clock (midnight at dayTime 18000, day
change there), so the two read 1:42 apart and changed day at different hours. `XaeroTimeInfoMixin` (client only)
rewrites all four Xaero formats with `TimeSettings.clock` / `clock24` and `TimeSettings.dayNumber`: the day number
steps at the pack's sunrise like SeasonHUD's, and is 1 at dayTime 0 like Xaero's (it counts from the sunrise nearest
dayTime 0, -1700 for the pack's 22300). Xaero's text is kept in the Nether / End, with `[time] enabled = false`, and if
the Xaero lambda (`lambda$static$14` in 25.3.10) moves after an update (`require = 0`).

Left as is: Better Days still wakes players at vanilla morning (dayTime % 24000 == 0), which the pack clock reads as
7:42 am, 1:42 after the season day changed.

**Bed clock (open, TI-31).** "The clock icon freezes while sleeping." Better Days' bed clock (top right of the bed
screen) and any hotbar clock share vanilla's `time` item property, which eases toward `getTimeOfDay` once per game
tick. Read from Better Days 3.3.4.3 (`SleepGui`, `TimeInterpolator`, `TimeService`), vanilla 1.19.2 and the pack's
mixins, nothing stops it: sleeping runs at 110 units per tick, Better Days sends time every tick, no mod overrides the
clock model, and no pack code touches client time. No log errors. The cause needs one in-game look (TI-31) to tell
apart: night not fast-forwarding, client time standing still in bed, or the clock property itself.

### 3.11 Admin date commands (`/cetime date`, built 2026-10-30)

Op-level (2) commands that move the calendar and keep every clock on the same date. They need Serene Seasons and
`[time.seasons] clock = SUN` (both presets); on TICKS they refuse.

| Command | Goes to |
|---|---|
| `/cetime date` | prints the date: "Year 1, Early Spring, day 1 of 8, 7:42 am (world day 1)" |
| `/cetime date reset` | the world's start: year 1, day 1 of Serene Seasons' `starting_sub_season`, at dayTime 0's time of day (7:42 am) |
| `/cetime date set <year> <month> [day] [time]` | that date; `month` is `early_spring` ... `late_winter`, day defaults to 1, time to 6:00 am; time reads "7:30 pm", "7pm" or "19:30" |
| `/cetime date next <unit>` | sunrise on the first day of the next day / month / season / year |
| `/cetime date previous <unit>` | sunrise on the first day of the previous one (not the current) |
| `/cetime date restart <unit>` | sunrise on the first day of the current one |
| `/cetime date add <n> <unit>` | `n` units on (negative: back), same time of day |

`<unit>` is `day`, `month` (one sub-season), `season` (3 months) or `year` (12 months). Months, seasons and years line
up with the calendar: a season starts on Early Spring / Summer / Autumn / Winter, a year on Early Spring.

**One position, every clock.** A date is one number: day time units since the first sunrise
(`TimeSettings.firstSunrise()`, -1700 for the pack's 22300, the same sunrise the minimap's "Day N" counts from). Year 1,
Early Spring, day 1, 6:00 am is 0. `TimeDate.apply` sets:

- **Sun:** `dayTime` = first sunrise + position, in every level (as `/time set`), with the time packet sent at once.
  Clamped at dayTime 0, so year 1 Early Spring day 1 can't go earlier than 7:42 am.
- **Seasons:** `seasonCycleTicks` = position within the year, in every level that has seasons, then
  `sendSeasonUpdate` (after the time packet, so the client's own sun step is overwritten).
  `SeasonClock.jumped` resets the step tracker, so the jump isn't added a second time.
- **Year:** not stored. `CalendarDate.year` reads it from the world day minus the day of the year, which the two above
  set together.
- **Wine** (`[time.wine] clock = SUN`): the vintage clock moves by the same amount as the sun. Forward it does this
  itself on its next tick; backward (it ignores backward steps) `TimeDate` calls `VintageClock.add` by reflection.
  So wines age or un-age with the calendar. A wine put away after the target date reads age 0 until the clock passes
  its storage time again. PLAYTIME wine is not moved.
- **Better Days, SeasonHUD, calendar tooltip, minimap:** read `dayTime` and Serene Seasons, nothing of their own to move.

**Year fix (same build).** The year used to count from the sunrise after dayTime 0 (22300) and round down. On a fresh
world the world day ran one behind the season day, so year 1 lasted until the second Early Spring (almost two years).
It now counts from the first sunrise and rounds up: a fresh world reaches year 2 at its first Early Spring, also when
it starts after Early Spring. Worlds whose date was already off (migrated, `/season set`) may read one year higher;
`/cetime date set` puts them on a clean date.

## 4. Limits and decisions

| # | Decision | Options | Recommendation |
|---|---|---|---|
| T1 (DECIDED 2026-10-18: A) | Where the master lives | A `[time]` in `creativeengineering-common.toml`; B its own `config/creativeengineering-time.toml` | **A**: it already shows in Create's Configs screen and is backed up with the rest of CEConfig. B is the same code with a second spec, and Create's screen shows only one common file per mod |
| T2 | Sub-season length | 3.3 table | **DECIDED 2026-10-18 (your list): 8 world days**, a season is 24 days = one real day |
| T3 (DECIDED 2026-10-18: SUN) | Season clock | SUN / TICKS | **SUN**: the HUD day matches the sun, sleeping counts, no speed-above-1 bug |
| T4 (DECIDED 2026-10-18: on) | Pause when empty | on / off | **on** for the dedicated server: sun, seasons and wine stop together |
| T5 (DECIDED 2026-10-19: cold / warm from Brewin' keg temperature, BUILT, 3.5) | Wine boosts beyond the cellar | cold, deep, storage type; season rejected | the user's call. Recommended: keep the cellar only until the base lengths have been played, then add cold cellar |
| T6 | Time of day on the HUD | none / after the season / a separate line | **DECIDED 2026-10-18 (your list): after the season, "7:30 pm" style** (3.7) |
| T7 (DECIDED 2026-10-18: rejoin) | Season length edited mid-session on a server | rejoin needed / send our own sync packet | **rejoin**, said in the config comment. A packet would also have to update SeasonHUD's client-side copy |
| T8 | What a wine month is (3.7) | A one sub-season; B its own length | **DECIDED 2026-10-18 (your list): A**, tied to the sub-season length |
| T9 | Wine clock (3.5, 3.7) | SUN / PLAYTIME | **DECIDED 2026-10-18 by your B7 list: SUN** (sleeping ages wine). Replaces B4.5's PLAYTIME |
| T10 | Calendar (3.7) | | **DECIDED 2026-10-18 (your list):** Vinery calendar removed; the Serene Seasons calendar gets its tooltip |
| T11 | Existing wines when months land | | **DROPPED 2026-10-18:** no conversions while the ports are in development |
| T12 | Clock reading at sunrise (3.7) | A vanilla convention: sunrise 4:18 am, sunset 7:00 pm; B shift by +1:42 so sunrise reads 6:00 am: sunset 8:42 pm. Either way the clock day is 14 h 42 min and the clock night 9 h 18 min | **DECIDED 2026-10-18 (you): B**, sunrise 6:00 am, sunset 8:42 pm |

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
- Crop growth: random ticks per real tick (`randomTickEffect = NEVER`). A world day of 60 min gives crops about
  3 times vanilla's growth per day. Serene Seasons' fertility is per sub-season.
- Brewin' keg fermentation: machine ticks, no calendar.

## 5. File map

| File | Role |
|---|---|
| [../config/creativeengineering-common.toml](../config/creativeengineering-common.toml) | master `[time]` |
| [../config/betterdays-common.toml](../config/betterdays-common.toml) | Better Days; time values ignored while `[time] enabled` |
| [../config/betterdays-client.toml](../config/betterdays-client.toml) | bed clock position |
| [../config/seasonhud-client.toml](../config/seasonhud-client.toml) | HUD position and day display |
| `../saves/<world>/serverconfig/sereneseasons-server.toml`, `seasonhud-server.toml` | per-world, written by the push |
| [../configs_edited/server_config/sereneseasons-server.toml](../configs_edited/server_config/sereneseasons-server.toml), [seasonhud-server.toml](../configs_edited/server_config/seasonhud-server.toml) | backups; become reference copies of what the push writes |
| [../config/vinery-common.toml](../config/vinery-common.toml) | Vinery port `[aging]`, `[wine]` (the keys the master supplies are unused while it is on) |
| [../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/VintageClock.java](../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/VintageClock.java) | vintage clock |
| [../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/WineYears.java](../development/backportedVinery/src/main/java/net/satisfy/vinery/core/wine/WineYears.java) | wine age, storage rate, cellar boost |
| [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java) | `Time` section |
| [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/time/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/time/) | `TimeSettings` (derived values, clock text), `TimePush`, `SeasonClock` (SUN counter, migration), `TimePause`, `TimeCommand` (`/cetime`), `TimeDate` (`/cetime date`, 3.11), `CalendarDate`, `TimeClient` (calendar), `WineTime` (public, read by the Vinery port's `forge/PackTime`) |
| `../development/patcherMod/.../mixin/BetterDaysConfigMixin.java`, `BetterDaysTimePauseMixin.java`, `ServerLevelTimePauseMixin.java`, `SereneSeasonsClockMixin.java`, `SereneSeasonsClientClockMixin.java`, `SereneSeasonsTimeSkipMixin.java`, `SeasonHudCurrentSeasonMixin.java`, `XaeroTimeInfoMixin.java` | the mixins (3.8, 3.10) |
| [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SeasonHudHelperMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/SeasonHudHelperMixin.java) | season-type HUD day; reads the same lengths |

## 6. Offline checks

- `tools/time_table.py` (planned): reads `[time]` from the toml, prints the 2.x / 3.x tables (Better Days speeds,
  Serene Seasons and SeasonHUD values, wine real times) and exits 1 if a derived value is out of a target's range
  (Better Days speed 0 to 24000, `sub_season_duration` at least 1). Add it to `tools/generators.json` as `time_table`.
- Headless Rhino is not needed: it is all Java. A GameTest for the SUN counter: set `dayTime` steps of 0.125, 1 and 110
  per tick and check that `seasonCycleTicks` moves by exactly the same amount.

## 7. In-game test suite

Everything is built (2026-10-18), so all of these can run. TI-15 and TI-22 test the same calendar tooltip; TI-25 to
TI-27 cover what the build added (3.8).

- [ ] **TI-01 Push on start:** set `[time]` to the pack values, start a world -> the log lists the pushed values;
  `serverconfig/sereneseasons-server.toml` shows `day_duration = 24000`, `sub_season_duration = 8`;
  `seasonhud-server.toml` matches; `/wine time` shows 60 min per day
- [ ] **TI-02 Day length:** `/time set 22300`, time real minutes until `/time query daytime` reads 13000 -> 45 min (±1);
  then to 22300 again -> 15 min
- [ ] **TI-03 Speed change:** set `dayMinutes = 22.5` in Create's Configs screen -> the sun moves twice as fast by day at once;
  `/cetime` shows `daySpeed` 0.544
- [ ] **TI-04 Seasons follow the sun:** `/season set early_spring`, sleep through a night -> SeasonHUD day goes up by
  exactly 1 at sunrise; `/time add 24000` -> one more day
- [ ] **TI-05 No double count:** `dayMinutes = 5` (speed above 1), play one world day -> SeasonHUD day goes up by
  exactly 1
- [ ] **TI-06 Old world migration:** open `Workspace` (192000 / 10) after the change -> same sub-season and day number
  as before; the second start doesn't move it again
- [ ] **TI-07 New single-player world:** set `daysPerSubSeason = 10`, create a world -> 24000 / 10, not Serene Seasons' 24000 / 8 (the pack's own 8 matches Serene Seasons' default, so it can't tell the two apart); set it back to 8
- [ ] **TI-08 HUD totals:** `enable_show_day = SHOW_WITH_TOTAL_DAYS` -> "Day x/24" in temperate biomes; arid / frozen
  biomes count through the 96-day year, 12 x 8 (SeasonHudHelperMixin)
- [ ] **TI-09 Pause when empty:** dedicated server, note `/time query daytime`, `/season`, `/wine time`, everyone leaves
  for 5 min -> all three unchanged on rejoin
- [ ] ~~**TI-10 Wine lengths unchanged**~~ retired 2026-10-18: lengths change on purpose (3.7), no conversions
- [ ] **TI-11 Wine in world days:** `/wine clock add 1 days` -> the tooltip age goes up by one day; the JEI Wine Aging
  page shows 8 h per month
- [ ] **TI-12 Cellar boost:** wine in a rack with no sky light -> WTHIT and tooltip show x2.0 (x2.5 until 2026-10-19, 3.5), `/wine info` shows the
  shorter time to the next level
- [ ] **TI-13 Master off:** `[time] enabled = false`, restart -> Better Days uses its own toml again (check with TI-02),
  Serene Seasons files are left as last written
- [ ] **TI-14 Server sync:** dedicated server with `daysPerSubSeason = 5`, fresh client joins -> HUD shows "Day x/15"
- [ ] **TI-15 Calendar tooltip:** hover the Serene Seasons calendar -> day, sub-season and the real time to the next
  sub-season, matching `/cetime`
- [ ] **TI-16 `/cetime`:** run it -> every row matches the 3.2 table for the current values
- [ ] **TI-17 Sleep ages wine (3.7):** `/wine info` on a wine, sleep through a night -> the age goes up by the same fraction of a day as the SeasonHUD day count
- [ ] **TI-18 Month = sub-season (3.7):** `/wine clock add 8 days` -> the wine is 1 month older and the calendar is one sub-season on; the JEI Wine Aging page shows 8 h per month
- [ ] **TI-19 Level every 3 months (3.7):** a fresh wine, `/wine clock add 24 days` -> level 1; the tooltip says months, not years
- [ ] **TI-20 Dark x2.0 (3.7, x2.5 until 2026-10-19):** two fresh wines, one in a rack with no sky light, one under sky; after the same time the dark one is 2.0 times older; WTHIT shows x2.0
- [ ] ~~**TI-21 Switch keeps ages**~~ retired 2026-10-18: no conversions
- [ ] **TI-22 Calendar (3.7):** no Vinery calendar in JEI or `/give`; hover and hold the Serene Seasons calendar -> season, day x of 8, year and time of day, matching SeasonHUD and `/cetime`; next sub-season line counts down
- [ ] **TI-23 Clock format (3.7):** `/time set 22300` -> 6:00 am; `/time set 13000` -> 8:42 pm; `/time set 11800` -> 7:30 pm; the same on SeasonHUD, the calendar and `/cetime`
- [ ] **TI-24 Day split (3.7):** time one sunrise to sunset and sunset to sunrise -> about 45 and 15 real minutes
- [ ] **TI-25 Client day doesn't run ahead (3.8):** SUN seasons, watch SeasonHUD across a sub-season change by day -> the day number steps once at sunrise, and the world re-tints once (no repeated chunk reload)
- [ ] **TI-26 Game time keeps running when paused (3.8):** dedicated server, nobody online for a minute -> `/time query gametime` moved on, `/time query daytime` didn't; a hopper clock left loaded still ran
- [ ] **TI-28 Presets (3.9):** fresh config -> `preset = "SINGLEPLAYER"`, `monthsPerLevel = 1`; pick SERVER in Create's Configs screen -> `monthsPerLevel` becomes 3 and `/wine time` shows 3 months per level (`vinery-common.toml` is not written since 2026-10-27); set `daysPerSubSeason` to 10 -> the preset reads CUSTOM; set it back to 8 -> SERVER again
- [ ] **TI-29 Preset picked offline (3.9):** game closed, change only `preset` to SERVER in the file, start -> the log writes `monthsPerLevel 1 -> 3`, no CUSTOM
- [ ] **TI-27 Push log (3.8):** first start of `Workspace` -> latest.log lists `[time] sereneseasons day_duration 192000 -> 24000` and `sub_season_duration 10 -> 8`, then `[time] minecraft:overworld: seasons now follow the sun; sub-season N day M kept`; the second start lists no changes
- [ ] **TI-30 Minimap lines agree (3.10):** overworld, Xaero's time line on (default profile) -> `/time set 11800`: the SeasonHUD line ends "7:30 pm" and Xaero's reads "Day N, 19:30"; `/time set 22250`, wait for sunrise -> SeasonHUD's "Day x/24" and Xaero's "Day N" step together at 6:00 am; in the Nether Xaero's line is unchanged
- [ ] **TI-31 Bed clock turns (3.10):** single player, clock in the hotbar, sleep at dusk -> note how long the night takes (about 5 s expected), whether Better Days' bed clock (top right) and the hotbar clock turn, and whether SeasonHUD's time races. Report which of the three moved

- [ ] **TI-32 Date reset (3.11):** *r1031: `add` refuses plural units ("Unknown unit days"); use `40 day` or change the command.* `/cetime date add 40 days`, then `/cetime date reset` -> `/cetime date` reads "Year 1, Early Spring, day 1 of 8, 7:42 am (world day 1)"; `/time query daytime` 0; SeasonHUD and the calendar tooltip agree
- [ ] **TI-33 Month and season steps (3.11):** after TI-32, `/cetime date next month` three times -> Mid Spring, Late Spring, Early Summer, each day 1 at 6:00 am; `/cetime date previous season` -> Early Spring day 1; `/cetime date restart month` mid-month -> day 1 of the same month; the world re-tints once per sub-season change
- [ ] **TI-34 Year and set (3.11):** `/cetime date next year` -> "Year 2, Early Spring, day 1"; `/cetime date set 3 mid_autumn 5 7:30 pm` -> exactly that on `/cetime date`, SeasonHUD ("Mid Autumn (Day 13/24) 7:30 pm" with totals) and the calendar; minimap "Day N" = (3-1) x 96 + 7 x 8 + 5 = 253
- [ ] **TI-35 Wine follows (3.11):** a fresh wine in storage, `/wine info`; `/cetime date add 1 month` -> 1 month older (cellar boosts apply); `/cetime date add -1 month` -> back to the first reading; the chat line says "wine clock moved back"
- [ ] **TI-36 No double count (3.11):** `/cetime date next day`, wait 10 s -> SeasonHUD day went up by exactly 1, not 2; `/season` in every whitelisted dimension shows the same sub-season and day
- [ ] **TI-37 Fresh world year (3.11):** new single-player world, `/cetime date next year` -> "Year 2" (before the fix it read Year 1)
## 8. History

- 2026-10-18: writeup created. Read Better Days 3.3.4.3, Serene Seasons 8.1.0.24 and SeasonHUD 2.0.4 decompiled, plus the
  Vinery port's vintage clock. Findings: the world day is 105.75 min; Serene Seasons' day is 160 min on the server and
  20 min in single player; SeasonHUD keeps its own length copy; speeds above 1 double count seasons; Better Days'
  MINUTES mode is wrong with the pack's sunrise / sunset; nothing pauses the sun on an empty server. Designed the
  `[time]` master, the push, SUN seasons, pause-when-empty, wine boost options and GUI integration. Decisions T1 to T7
  open. Nothing built.
- 2026-10-18: added 3.7 from your Vinery list (alcohol.md B7): a wine month is one sub-season, wine ages on the sun
  clock (sleeping counts; replaces B4.5 and 3.5's PLAYTIME), +1 level every 2 months, x2.5 in the dark, "year"
  -> "month" in every wine text, and the Vinery calendar showing the Serene Seasons date. 3.1, 3.2 and 3.5 updated;
  T8 to T11 added (T9 decided by your list); TI-17 to TI-22 added.
- 2026-10-18: your second Vinery list set the numbers: 45 min day + 15 min night, 8-day sub-seasons (a season is
  one real day), wine month = sub-season, +1 level every 3 months, x2.5 dark, clock shown as "7:30 pm". The Vinery
  calendar is removed and the Serene Seasons calendar gets its tooltip. No conversions (T11 dropped). Found: the
  port's WORLD_TIME clock needs a DAYTIME day unit. T2, T6, T8, T10 decided; T12 added; TI-10 and TI-21 retired;
  TI-23, TI-24 added.
- 2026-10-18: T12 decided (you): the clock reads sunrise as 6:00 am, so sunset reads 8:42 pm (3.7, TI-23).
- 2026-10-18: built in patcherMod 1.0.8 (3.8): the `[time]` master, the push, SUN seasons with the sunrise offset
  and migration, pause when empty, SeasonHUD totals and time of day, the calendar tooltip and action bar, `/cetime`.
  T1, T3, T4 and T7 built with the recommendation. Found while building:
  - Serene Seasons' client counter would run ahead between syncs, so in SUN mode it follows the client's dayTime.
  - Cancelling `tickTime` would also stop game time, so only its dayTime step is skipped.
  - Serene Seasons keeps no year count, so the year comes from the world's day count.
  TI-25 to TI-27 added. Not built: `tools/time_table.py`, the GameTest, T5 boosts.
- 2026-10-18 late: presets built (3.9): SINGLEPLAYER (default, 1 month per wine level) / SERVER (3) / CUSTOM, the
  label derived from the values. T1, T3, T4 and T7 decided as recommended. Found: the deploy ships one `config/` to
  both sides, so a SERVER preset doesn't survive a payload (open). TI-28, TI-29 added.
- 2026-10-18: port side of 3.7 built in the Vinery port (alcohol.md B7): `[wine] daysPerMonth` 8,
  `monthsPerEffectLevel` 3, `durationPerMonth`, `cellarAgingMultiplier` 2.5; `[aging] clockMode` WORLD_TIME with the new
  `dayUnit` DAYTIME (24000 units a day); real-time text uses the measured day-time pace; "month" in every wine text;
  the Vinery calendar removed. The master, the push, the Serene Seasons calendar tooltip and the clock format are
  still to build.
- 2026-10-19: STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) TI). Single-player evidence (00:57): Better Days speeds
  from `[time]`, Serene Seasons / SeasonHUD 192000 -> 24000 and 10 -> 8, Vinery `WORLD_DAY_MINUTES` 0 -> 60 and
  `YEARS_PER_EFFECT_LEVEL` 3 -> 1, and every dimension "seasons now follow the sun; sub-season 2 day 3 kept". The
  3.9 deploy gotcha is still open: the payload ships SINGLEPLAYER to the server too (plan_1.0.8.md 2.3 lists
  options).
- 2026-10-19: DEPLOYED with pack 1.0.8 (10:20, server + clients). The server preset is still open (TODO `pack/release #2`); TI-01 to TI-29 pending.
- 2026-10-23: 3.10 built: Xaero minimap time line on the pack clock (`XaeroTimeInfoMixin`, `TimeSettings.clock24` / `dayNumber`). Bed clock freeze (TODO time #4) investigated offline, no cause found; TI-30, TI-31 added.
- 2026-10-27: the Vinery port owns its config (pack_hygiene.md finding 39). `TimePush.pushVinery` (reflection into
  `VineryConfig`) is gone; the port reads the master through patcherMod's public `time/WineTime` (3.2). Same values
  and behaviour. The 1.3.10 wine `Year` conversion and `legacyYearLengthDays` were removed (finding 10).
- 2026-10-30: 3.11 built in patcherMod (`time/TimeDate`): `/cetime date` reset / set / next / previous / restart / add,
  moving the sun, Serene Seasons in every level and the vintage clock together. The year now counts from the first
  sunrise and rounds up (`CalendarDate.year`), fixing a fresh world's two-year year 1. Compiles; TI-32 to TI-37 not run.
