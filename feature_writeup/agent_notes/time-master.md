# time-master

> CEConfig [time] master (patcherMod time/ package) drives Better Days, Serene Seasons, SeasonHUD and the Vinery port; feature_writeup/time.md (TI-)

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-10-18: `[time]` in creativeengineering-common.toml is the only place day length, seasons and wine aging are set. Design + build status: feature_writeup/time.md (3.8 lists every change from the design). Pack values: 45 min day + 15 min night, 8-day sub-seasons, wine month = sub-season, +1 level per 1 month (SINGLEPLAYER preset, default) or 3 (SERVER preset), cellar x2.0, cold x2.0, warm x0.75 (10-19; patcherMod 1.0.10), sun clock for seasons and wine, clock reads sunrise (dayStart 22300) as 6:00 am.

How it plugs in: Better Days via `@Pseudo` getter mixin (its WhiteNoise config can't be set); Serene Seasons + SeasonHUD SERVER configs `.set()` + `SPEC.save()` at ServerAboutToStartEvent (Forge loads SERVER configs just before that event) and on every toml save; Vinery port by reflection on `net.satisfy.vinery.forge.VineryConfig`.

Gotchas found:
- Serene Seasons' client counter runs +1/tick between server syncs; with a sun-driven server counter it ran ahead and snapped back each second (sub-season crossings re-render all chunks). SUN mode makes the client follow its own dayTime (SereneSeasonsClockMixin).
- Never cancel `ServerLevel.tickTime` (m_8809_) to pause the sun: it also advances game time, which scheduled block ticks use. Redirect only its `setDayTime` (m_8615_).
- Serene Seasons wraps seasonCycleTicks each year and stores no year; the calendar year comes from world day count minus day of year.
- SUN migration reads Serene Seasons' lengths recorded BEFORE the push; a per-level marker SavedData makes it run once.

Presets (time.md 3.9): `[time] preset` SINGLEPLAYER/SERVER/CUSTOM, label derived from the values (TimePresets). Gotcha: build_deploy ships one config/ to both sides, so a server set to SERVER reverts on the next payload (decided 2026-10-26: the server stays on SINGLEPLAYER, TODO `pack/release #2`). T1/T3/T4/T7 DECIDED as recommended; T5 (extra wine boosts) open. Not built: tools/time_table.py, SUN GameTest. Related: [backported-vinery](backported-vinery.md), [weather-by-biome](weather-by-biome.md), [patchermod-coremod](patchermod-coremod.md).
