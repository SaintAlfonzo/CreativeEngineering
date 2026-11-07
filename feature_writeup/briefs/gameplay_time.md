# gameplay / time: implementation brief

Written 2026-10-21 from [TODO](../todo/TODO.md) `# gameplay`, Sub `time`. Feature writeup: [time.md](../time.md)
(test prefix `TI-`, used up to TI-37 on 2026-10-31; new tests here are placeholders `TI-newN`, numbered when queued).
Related: [weather.md](../weather.md) (SeasonHUD mixins), [alcohol.md](../alcohol.md) B7 (the wine side of the master).

Jars checked (decompiled with vineflower or read with `javap`, under `scratchpad/decomp/` and `scratchpad/vanilla/`):
`betterdays-1.19.2-3.3.4.3-FORGE.jar`, `seasonhud-forge-1.19.2-2.0.4.jar`, `xaerominimap-forge-1.19.2-25.3.10.jar`,
the vanilla 1.19.2 client jar with Mojang's mappings, and MCP config `1.19.2-20220805.130853` (`joined.tsrg`, for the
SRG names below). A fact is marked "verified" when it was read from one of them.

Both bugs come from one scratchpad line each (git `2e8687f`, `change_scratchpad.md`): "clock icon while sleeping is
frozen" and "season day and day counter on gui are misaligned". Nothing else describes them.

Re-checked 2026-10-31 (brief sweep): #5 (built 2026-10-23) and #6 (built 2026-10-30) are Testing and listed under
"Built, needs play"; #5's build section was cut (time.md 3.10 holds it). Line refs in #3 and #4 re-read.

## Scope and order

| Row | Change | Status | Size |
|---|---|---|---|
| #4 | Clock icon frozen while sleeping | Bug (offline check 2026-10-23 found no cause; TI-31 next) | unknown cause: a 5-minute in-game diagnosis first, then likely a small client mixin or a config line |
| #3 | Offline checks (`tools/time_table.py`, SUN-counter test) | Planned | small: one Python tool + one headless Java test |
| #5, #6, #1 | Minimap line, `/ccaltime date`, time master | Testing | built, see "Built, needs play" |

Build order:

1. **#4 diagnosis** (TI-31) in the same play session as #5's test (TI-30); both are queued together as client row
   CQ-NEW-01 (scratch_queue.md 979). A fix (config line or a client mixin) goes into the next patcherMod build.
2. **#3** any time; no game needed. Do it before the next `[time]` change so the table catches mistakes.

Related rows: `pack/release #2` (the server preset gotcha, time.md 3.9), `alcohol` Vinery rows (wine clock).

---

## time #4: Clock icon frozen while sleeping
Status Bug / Work patcherMod / TODO `gameplay/time #4`

### Goal
While a player lies in bed, the clock they see (Better Days' bed clock, or a clock in the hotbar) turns with the
fast-forwarding night.

### Now
- **Which clock.** Better Days draws a 64 px clock item in the top-right corner of the bed screen
  (`config/betterdays-common.toml` 129 `displayBedClock = true`; `config/betterdays-client.toml` 5 `clockAlignment =
  "TOP_RIGHT"`, 8 `clockScale = 64`, 16 `preventClockWobble = true`). That is almost certainly "the clock icon".
- **How it renders (verified, `betterdays.client.gui.SleepGui`):** `renderClock` calls
  `ItemRenderer.m_115203_(clock, 0, 0)` = `renderAndDecorateItem(ItemStack,int,int)`, which passes
  `Minecraft.getInstance().player` as the entity (verified in the vanilla bytecode), so the clock's `time` item
  property can read the level. `onClientTick` (START phase) also calls `getModel(clock, level, player, 0)`
  (`m_174264_`) every tick while `player.isSleeping()`, to skip vanilla's wobble.
- **The `time` property (verified, vanilla `ItemProperties$1`):** target = `level.getTimeOfDay(1.0F)` in a natural
  dimension; it then eases toward the target (`wobble`), updating only when `level.getGameTime()` changed since its last
  call. So the face moves only if the client's dayTime **and** gameTime move.
- **Time while sleeping (verified, `betterdays.time.TimeService`):** all-asleep runs at `sleepSpeedMax` 110
  (`config/betterdays-common.toml` 104), so the pack's 9,300-unit night passes in about 85 ticks (4 s): the face should
  spin about a third of a turn. Better Days broadcasts a time packet every tick (`broadcastTime`), and its client
  `TimeInterpolator` smooths the client's dayTime between packets.
- **The master's code (read):** nothing in patcherMod touches client dayTime or gameTime.
  `ServerLevelTimePauseMixin` / `BetterDaysTimePauseMixin` only act on a dedicated server with nobody online
  (`TimePause.paused`), `SereneSeasonsClientClockMixin` only reads dayTime. `BetterDaysConfigMixin` replaces the four
  speed/sunrise getters, which the sleep path doesn't use (it uses `sleepSpeedMax`, and `daySpeed` only for the
  overshoot after morning).
- Investigated offline 2026-10-23 and written up in [time.md](../time.md) 3.10 "Bed clock (open, TI-31)"; no log
  errors. Rechecked 2026-10-31: the configs above unchanged, `enableSleepFeature = true` (common 99), jars the same
  versions. TI-31 is in time.md 7 (655) and queued with TI-30 as client row CQ-NEW-01 (scratch_queue.md 979).
  Rechecked 2026-11-02 (pre-scratch pass): CQ-NEW-01 still not run (no r1031 / r1101 result), no log evidence; the
  code now lives in Creative Calendar (`development/creativeCalendar`), not patcherMod. No change until TI-31 is run.
- So no cause is visible offline. Ranked hypotheses:
  1. **The night isn't fast-forwarding** (`enableSleepFeature` off in a profile, or the sleep ratio not reached;
     `betterdays-common.toml` `[sleep] ratioPlayersForSleep = 0.0` (134) means any one sleeper is enough), so the face moves at the normal night pace: 9,300 units in 15 min, one of the clock's 64
     frames every ~36 s, which looks frozen.
  2. **Client time not reaching the screen while in bed**: then SeasonHUD's "7:30 pm" suffix would also stand still.
  3. **`preventClockWobble`'s per-tick `getModel` call** interacting with the property's once-per-gameTime update.
     Reading the code it shouldn't freeze, but it is the only bed-specific path.
  4. A hotbar clock rather than the bed clock: same property, so the same tests apply.

### Build
1. **Diagnose first (TI-31).** Single player, sleep at night with a clock in the hotbar, and watch: the bed clock,
   the hotbar clock, SeasonHUD's time suffix and how long the night takes.
   - Night takes ~4 s and the HUD time races, but both clocks stand still -> hypothesis 3/property. Set
     `preventClockWobble = false` in `betterdays-client.toml` and repeat. If that fixes it, the fix is that config line
     (and its `configs_edited/root_config/` copy). If not, add a client mixin on `betterdays.client.gui.SleepGui`
     (`@Pseudo`, `remap = false`) that redirects the `m_115203_` call in `renderClock` to
     `renderAndDecorateItem(LivingEntity, ItemStack, int, int, int)` (`m_174229_`) with `mc.player`, and cancel
     `onClientTick`'s `getModel` call; then retest.
   - Night takes 15 min -> hypothesis 1: the clock is fine, sleeping isn't. Check Better Days' `[sleep]` block
     (`enableSleepFeature`, `ratioPlayersForSleep`, `sleepSpeedMax`) and the debug log for `Sleep cycle complete`. That becomes its own bug.
   - The HUD suffix also freezes -> hypothesis 2: client time. Check F3's day counter and `/time query daytime`
     before and after; look at Better Days' `TimeInterpolator` (it rewrites the client's dayTime every render frame) and
     at any mod that pauses the client in bed.
2. Record the result in time.md 3.10's "Bed clock" paragraph (it exists since 2026-10-23, marked open) whatever it is.

### Ids and data
- Better Days classes (verified in 3.3.4.3): `betterdays.client.gui.SleepGui` (`onClientTick`, `renderClock`,
  static field `clock`), `betterdays.client.TimeInterpolator`, `betterdays.time.TimeService`.
- SRG names (verified with MCP `joined.tsrg`): `ItemRenderer.renderAndDecorateItem(ItemStack,int,int)` `m_115203_`,
  `renderAndDecorateItem(LivingEntity,ItemStack,int,int,int)` `m_174229_`, `renderGuiItem(ItemStack,int,int)`
  `m_115123_`, `getModel(ItemStack,Level,LivingEntity,int)` `m_174264_`.
- Config keys: `betterdays-client.toml` `[sleep]`-area `preventClockWobble`, `clockAlignment`, `clockScale`;
  `betterdays-common.toml` `displayBedClock`, `sleepSpeedMax` 110, `sleepSpeedMin` 1, `sleepSpeedAll` -1.

### Decisions
- None until the diagnosis. If hypothesis 3 holds, prefer the config line over a mixin.

### Gotchas
- Better Days' sleep only fast-forwards while `!sleepStatus.allAwake()` and the sleeping ratio is at least
  `ratioPlayersForSleep` (`ConfigHandler.Common.percentPlayersForSleep()` reads it); the pack's 0.0 means one sleeper
  is enough.
- The bed clock only draws on `InBedChatScreen` (`ClientEventListener.onGuiEvent`), so a test must stay in bed, not
  lie down and get up.

### Tests
- **TI-31 Bed clock turns** (exists, time.md 655; it says about 5 s): single player, sleep at dusk with a clock in the hotbar -> the night passes in about 4-5 s,
  Better Days' bed clock (top right) and the hotbar clock both turn from night to morning, SeasonHUD's time suffix
  races to about 7:42 am (Better Days wakes at vanilla morning, see #5).

---

## time #3: Offline checks
Status Planned / Work tool / TODO `gameplay/time #3`

### Goal
The two offline checks time.md 6 promises: a table of every derived time value from the toml, and a headless test
that the SUN season counter moves exactly as far as the sun.

### Now
- Neither exists: no `tools/time_table.py`, no `time_table` entry in [tools/generators.json](../../tools/generators.json)
  (50 entries on 2026-10-31, none for time), no `tools/test_time.sh` or time test sources under `tools/`.
- The derivations live in [TimeSettings.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/time/TimeSettings.java)
  (`dayUnits` 28, `daySpeed` 34, `nightSpeed` 38, `seasonDayDuration` 51, `yearDays` 59, `clock` 69, `clock24` 78,
  `dayNumber` 94, `realMinutes` 114) and the push targets in
  [TimePush.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/time/TimePush.java)
  (74-87: Serene Seasons `day_duration` / `sub_season_duration` / `progress_season_while_offline`, SeasonHUD
  `day_length` / `sub_season_length`). Vinery is no longer pushed: the port reads `WineTime` itself (TimePush.java
  23-24, time.md 3.7), so the wine rows come from `[time.wine]` directly.
- New since the first brief: `TimeDate.java` (`/ccaltime date`, row #6) does calendar math (Early Spring day 1 6:00 am
  = 0, time.md 3.11) inside command code; `CalendarDate.java` has `worldDay` / `year` helpers that need a `Level`.
- The `[time]` values: [config/creativeengineering-common.toml](../../config/creativeengineering-common.toml) from 2012.
- The SUN counter: `SeasonClock.onLevelTick` ([SeasonClock.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/time/SeasonClock.java)
  63-90, the step at 85) adds every forward dayTime step to `seasonCycleTicks`, mod the cycle; it reads `SeasonSavedData` and
  `SeasonHandler`, so a test can't call it without Serene Seasons on the classpath.
- Headless pattern to copy: [tools/test_shields.sh](../../tools/test_shields.sh) + `tools/shield_test/` (sources
  [tools/java_test_cp.sh](../../tools/java_test_cp.sh) for the mapped Forge classpath, an `FMLEnvironment` stub, runs
  `ShieldConfigTest` against `development/creativeCommon/build/classes`).

### Build
1. **`tools/time_table.py`** (stdlib only; `tomllib` on Python 3.11+, else a tiny reader for the `[time]` block).
   Read `[time]`, `[time.day]`, `[time.seasons]`, `[time.wine]`, `[time.wine.boosts]` and print:
   - Better Days: `dayUnits = (nightStart - dayStart) mod 24000` (0 -> 12000), `daySpeed = dayUnits / (dayMinutes*1200)`,
     `nightSpeed = (24000 - dayUnits) / (nightMinutes*1200)`;
   - Serene Seasons / SeasonHUD: `day_duration` (24000 on SUN, else world-day ticks), `sub_season_duration`,
     `progress_season_while_offline`;
   - real time per sub-season, season, year (`daysPerSubSeason * 12` days), with and without sleeping (night skipped);
   - wine: real time per month and per level, times `cellarMultiplier`, `coldMultiplier`, cold x cellar, warm;
   - the clock reading at sunrise / sunset (6:00 am / 8:42 pm for 22300 / 13000), using the same formula as
     `TimeSettings.clock`.
   Exit 1 when a value is out of range (a speed <= 0 or > 24000, `sub_season_duration < 1`, `dayStart == nightStart`).
   Optional `--world <saves/x>`: also read `serverconfig/sereneseasons-server.toml` and `seasonhud-server.toml` and flag
   any value that differs from what the push would write.
2. **generators.json entry** `time_table` (groups `["reports"]`, no `dumps`, `outputs: []`, `warnExit: [1]`,
   `apply: "nothing; a report"`).
3. **SUN-counter test.** Refactor the step math out of `SeasonClock.onLevelTick` into a pure static
   `static int advance(int cycleTicks, long lastDayTime, long dayTime, int cycle)` (returns the new `seasonCycleTicks`;
   backward steps return `cycleTicks`), and call it from `onLevelTick`. Then `tools/time_test/.../TimeClockTest.java`
   + `tools/test_time.sh` (copy `test_shields.sh`): feed dayTime sequences stepping 0.125-equivalent (1 unit every 8
   ticks), 0.272, 0.517, 1, 110 (sleep) and a backward `/time set`, and check the counter moved by exactly the summed
   forward steps, wraps at `cycle`, and never moves backward. Also check `TimeSettings.clock` at 22300, 13000, 11800 (the
   TI-23 values), `TimeSettings.dayNumber` at the sunrise step (TI-30's -1700 offset), and `TimeDate`'s date-to-dayTime
   math if it is pulled into a pure helper (the plural-units fix of #6 could land in the same refactor).
4. time.md 6: replace "(planned)" with the commands; add the two to the File map (5).

### Ids and data
- None from mods. `SeasonTime.ZERO.getCycleDuration()` (Serene Seasons) is `day_duration * sub_season_duration * 12`;
  the test passes `cycle` in, so it needs no Serene Seasons class.

### Decisions
- **Java test or a Python re-implementation of the counter?** Recommendation: Java, against the real
  `SeasonClock.advance`, so the test can't drift from the code.

### Gotchas
- `java_test_cp.sh` expects the Forge Gradle cache and ATLauncher libraries of the dev machine (`$HOME/.gradle/...`,
  `$HOME/.local/share/atlauncher/libraries`); they are not in a cloud container.
- The toml's `[time]` block sits at line 2012 of a 2624-line file (it moves); read by section name, never by line.

### Tests
- `python3 tools/time_table.py` prints `daySpeed 0.272`, `nightSpeed 0.517`, sub-season 8 h, year 96 h, sunrise
  6:00 am, sunset 8:42 pm for the shipped values (time.md 3.2 / 3.7) and exits 0.
- `tools/test_time.sh` prints `ALL ... ok`.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #1 | Time master | Testing | Code matches time.md 3.8 / 3.9. Fixed stale test text and values in time.md (below): the pushed sub-season is 8, not 10, and the cellar boost is x2.0, not x2.5. The 3.9 deploy gotcha (server preset) is still open as `pack/release #2`. TI-01 to TI-29 open |
| #5 | Season day and day counter misaligned on the GUI | Testing | Built 2026-10-23, shipped 1.1.03: `XaeroTimeInfoMixin` puts Xaero's minimap time line on the pack clock, day steps at sunrise; Better Days still wakes at vanilla morning (7:42 am). Open: TI-30 in game (client row CQ-NEW-01). Writeup [time.md](../time.md) 3.10 |
| #6 | Admin time commands | Testing | Built 2026-10-30 as `/ccaltime date` (`TimeDate.java`; reset / set / next / previous / restart / add). Scratch r1031 PASSED SQ-TD-01 to -03, -05; open: SQ-TD-04 client half, TI-32 to TI-37 in game, and the plural-units decision (`add 40 days` refused, TI-32 and the docs write plurals). Writeup [time.md](../time.md) 3.11 |

## Writeup corrections made

- [time.md](../time.md) 3.2 table: SeasonHUD pack value `24000, 10` -> `24000, 8`; Vinery pack values `8, 3, 5, 2.5`
  -> `8, 1 or 3 (preset, 3.9), 5, 2.0` (presets and the 2026-10-19 cellar change).
- [time.md](../time.md) TI-07: the test now sets `daysPerSubSeason = 10` first (at 8 the pushed value equals Serene
  Seasons' default, so the old expectation "24000 / 10" was wrong and the test couldn't tell the two apart).
- [time.md](../time.md) TI-08: "120-day year" -> "96-day year, 12 x 8".
- [time.md](../time.md) TI-12 and TI-20: cellar x2.5 -> x2.0 (changed 2026-10-19, time.md 3.5).
