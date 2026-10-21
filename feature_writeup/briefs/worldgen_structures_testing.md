# worldgen / structures/testing: implementation brief

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, rows `structures/testing` #1 and #3 (Planned)
and #2 (Testing). Both open rows are play sessions in a client, not builds: this brief turns them into a run sheet
so one sitting covers them. Reference: [structure_tuning.md](../structure_tuning.md) 8.0 (run order) and 8.0.11 (the
client list), the test texts in section 8, [locate.md](../locate.md) 7 (LO- tests),
[testing/structures/](../testing/structures/00_plan.md) (scratch logs with coordinates),
[testing/structures/11_fixes_1_0_8.md](../testing/structures/11_fixes_1_0_8.md) 4, [time.md](../time.md) 3.3 and its
TI- tests.

Nothing here needs code, recipes, tags or dictionary ids. "L" in the test texts is `/locate structure`.

## Scope and order

1. **#3 first** (10 minutes, one Superflat world): it also gives the flat world ST-43 needs.
2. **#1** in the groups below, one world each where possible, cheapest first.
3. **#2** (Open ST- tests) stays at Testing: it is the umbrella for the rest of section 8; tick what #1 covers.

Setup for every group (from 8.0.0, adapted to a client):
- The deployed instance (1.0.9, patcherMod `mods/creativeengineering-1.0.9.jar`), single player, a **new world** (every
  structure change is new-chunks-only). Seed 8675309 gives the scratch logs' coordinates as hints, but the scratch runs
  were mostly at `structureDensity` 4 and on older jars (1.0.6 / 1.0.7), so positions differ: use `/locate`.
- Creative mode, `/gamerule doDaylightCycle false`, Distant Horizons on for LO-11 only (it slows flying tests).
- Log switches go in `config/creativeengineering-common.toml` before the world starts, and back afterwards:
  `[worldgen.ground_match] log`, `[worldgen.meta_structures] log`, `[worldgen.placement_priority] log`. Forge reads
  the file live, but structure tag/switch changes need a rejoin and a value change was seen not to reload for 10
  minutes on a server (12_earth_fill_camps.md 2): restart the game after each toml edit.

---

## structures/testing #3: Superflat and the client clock mixin in a client
Status Planned · Work none · TODO `worldgen/structures/testing #3`

### Goal
Run the two 1.0.8 fixes that only a client can exercise.

### Now
- **Superflat:** `mixin/RegistryLoaderPresetOverworldMixin.java` (patcherMod, 1.0.8+) keeps a non-multi-noise preset
  overworld (flat, debug, single biome) after the data pack `dimension/overworld.json` overrides (ours from
  `biomeLayout`, Terralith's, Tectonic's). Log line (`RegistryLoaderPresetOverworldMixin.java:60`): `[worldgen] kept
  the world's own overworld generator (...): data pack dimension/overworld.json overrides skipped`. Passed on a
  dedicated server with `level-type=minecraft:flat` (11_fixes_1_0_8.md 2, boots 1-2); never run as a single-player
  Superflat world.
- **Client clock:** `mixin/SereneSeasonsClientClockMixin.java` (client list of `creativeengineering.mixins.json`,
  line 123) replaces Serene Seasons' client +1 per tick with the client's own dayTime steps when
  `[time.seasons] clock = "SUN"` (instance toml line 1822: SUN). Without it the client counter ran ahead about 3.7x by
  day and snapped back every second, with a full chunk re-render on each sub-season crossing (time.md 3.3, file map 444).

### Build (the run)
1. Create a world: More World Options -> World Type **Superflat**, default preset. Expected: the world is flat; in
   `logs/latest.log` the "kept the world's own overworld generator (FlatLevelSource)" line; no KubeJS errors.
   Rejoin once: still flat.
2. In that world run ST-43's placement: `/ceplace structures @mysticrift_pillagers_towers_aris @skyvillages @saloon
   @villager_forge @illagerwarship` (all placed; urns where 1.20 pots were, Vinery cherry wood on the cherry tower, no
   gaps in Sky Villages), and `/ceplace templates kaisyn:village/meadow_swiss/houses/*` (17 of 17, no "Not a flat
   world").
3. Clock, in a normal world: `/season set late_spring` close to a sub-season end (`/time set 22000`, then let the day
   run, or `/time add` in small steps). Watch SeasonHUD's day number and the grass colour: the day goes up once at
   sunrise and never jumps back; the chunk re-render (a visible flicker of every chunk) happens once at the
   sub-season change, not every second. Then TI-04 and TI-05 (time.md) in the same world: sleeping moves the
   SeasonHUD day by one; at `dayMinutes = 5` one world day moves it by one.
4. Tick the results into 11_fixes_1_0_8.md 4 ("Still open") and ST-43 / TI-04 / TI-05.

### Decisions
None.

### Gotchas
- A Superflat preset with a custom biome is still a flat generator: fine. A "Single Biome" world type is kept too
  (by design: not multi-noise).
- The clock test needs the client and server halves together; in single player both run in one JVM, which is the
  normal case. A dedicated-server client is the other case (TI-14 covers sync).

### Tests
ST-43, TI-04, TI-05 (existing); the Superflat check has no id: add **ST-220 Superflat in a client [game]** (provisional
id; other structure briefs may claim it, renumber against section 8) next to ST-43 when ticking.

---

## structures/testing #1: Client test list 8.0.11
Status Planned · Work none · TODO `worldgen/structures/testing #1`

### Goal
Run the 34 [game] checks of 8.0.11 (structure_tuning.md 8.0.11): ST-27, 29, 43, 62, 63, 64, 66, 68, 68b, 71, 88,
89, 90, 70, 73, 45, 10, 110, 111, 116, 122, 123, 126, 137, 138, 139, 146, 147, 151, 152, 155, 156, LO-09, LO-11.
All 34 are still unticked (checked 2026-10-21).

### Run sheet (groups by setup; full texts: `grep -n '\*\*ST-<n> ' structure_tuning.md`)

| Group | Setup | Tests | Hints from the scratch logs |
|---|---|---|---|
| A. Load and log | New normal world, `[worldgen.ground_match] log = true` | ST-27: no mixin error, `/place structure philipsruins:ancient_ruins` in plains keeps its pad; ST-29: `[ground-match]` lines while flying a desert/badlands and a mountain range, then `/tp` to a named chunk | ST-27 has a scratch phase 1 note (no ground-match error on the server); the client half is what's left |
| B. Terrain looks | Same world, hilly area (an L of each, fly there) | ST-62 ballista, ST-63 soft house, ST-66 fresh watchtower, ST-68 others unchanged, ST-68b T&T and vanilla outposts, ST-89 siege camps, ST-71 towers at a village | 12_earth_fill_camps.md: armorer campsite at chunk [-7, -9] (seed 8675309) for the camp plinth look; it is in this group's spirit (ST-199 was scratch-only) |
| C. skirtBump | `[worldgen.terrain_adaptation] skirtBump = 4`, restart, new chunks | ST-64; then back to 0 | - |
| D. Metas | Normal world, `[worldgen.meta_structures] log = true` | ST-70 village towers, ST-73 flotilla by boat, ST-88 ruin clusters, ST-90 fleets and convoys, ST-137 way sign, ST-138 tweaks, ST-147 villages stop at a town | Phase 2: `meta/village_towers` village_snowy at 5039 156 4803; flotilla at 240 -464; ruin_cluster -704 608; fleet 304 128; raft convoy 1552 1840 (1.0.6, density 1). Phase 8 / 11_fixes: companion villages near towns at [201, -68], [210, -70], [231, -53] (density 4) |
| E. Peace and priority | `[worldgen.placement_priority] log = true` | ST-155 camps clear, ST-156 villages and towns clear | Phase 8 (08_phase_8.md): drops logged around a town at -3727 -3564 |
| F. Looks of single structures | Normal world | ST-10 eye of ender to a YUNG's stronghold, ST-110 vanilla witch hut, ST-116 crypt / spider cave staircase on dry land, ST-126 vanilla pyramid and jungle temple whole, ST-151 swapped blocks, ST-152 untouched Nether-block structures | Phase 2: `betterstrongholds:stronghold` at 1728 -2192 from spawn |
| G. Ocean | Boat or flight over 2,000+ blocks of new ocean | ST-45 no towers at sea | - |
| H. Rates by feel | Long flights, plains / forest / ice marsh | ST-111 spire, ST-122 statues, ST-123 pillagers, ST-139 meta rates, ST-146 generation speed | Phase 10: structure starts 6.4% of chunk generation; "no slower" is the bar |
| I. Locate and DH | DH generating, `transparency` as shipped (`DISABLED`) | LO-09 explorer map and LC map trade, LO-11 DH clip (locate.md 7) | - |

### Build (the run)
1. #3 first (above).
2. Groups A to I in order; tick each test in structure_tuning.md 8 with a dated italic note as the scratch runs did
   ("*2026-10-2x client (1.0.9, seed ..., world ...): PASSED ...*"), and LO-09 / LO-11 in locate.md 7.
3. Failures: write them up as a new testing/structures/13_client_8_0_11.md (same layout as 12_*.md: changes, results,
   still open) and add TODO rows.

### Decisions
- None for the run. If a test's wording no longer fits the shipped behaviour (as ST-53 and ST-160 needed after
  Phase 7), correct the wording in the same edit.

### Gotchas
- **Changes since the test texts were written** that change what you should see:
  - ST-152: the "move to the Nether" plan (`structures/contents #1`) is not built, so Philip's lost soul dungeon,
    rare ruin and blackstone Nether ruin still spawn in the Overworld and stay unswapped: the test stands as written
    until that row is built.
  - ST-63 / ST-68b / ST-89: since 1.0.9 the footprint water share can cancel a tagged house or outpost on a lake;
    a missing structure near water is not a terrain-adaptation failure.
  - ST-111: the spire's rate is a fit-solved value; if `structures/terrain #1` tags it `template_trim`, its look
    changes (no cut hills), not its rate.
  - Campsite camps since 1.0.9: grass rim flush with the ground (start height -4); a 1-block dip means the old
    data, a 4-block plinth the pre-1.0.9 jar.
- Use new chunks for every group (`/tp` far out, 5,000+ blocks between groups) so an earlier group's toml values
  don't decide what you see.

### Tests
The 34 listed; no new ids.

---

## Built, needs play

| Row | Status | Notes from this pass |
|---|---|---|
| #2 Open ST- tests | Testing | `grep -c '^- \[ \] \*\*ST-' structure_tuning.md` gives **81** open ST- tests on 2026-10-21 (the row says about 100). ST-191 to ST-198 (the 1.0.7 fixes) are all still open, as the row says; ST-65's trim half too. 34 of the 81 are the 8.0.11 client list (#1); most others are [scratch] checks of 1.0.7 fixes and rate tests skipped in Phase 6 (ST-72, 91, 98, 101, 106) |

## Writeup corrections made

None in this subfamily's writeups (8.0.11 and 11_fixes_1_0_8.md 4 are current).
