# M7: Phase 7, calibration runs (8.0.7)

2026-10-19 (the 10-14 start was lost with its session). Live notes: [07_phase_7.scratch.md](07_phase_7.scratch.md).
Result lines are inline in [structure_tuning.md](../../structure_tuning.md) section 8, with a summary under
"8.0.7 Phase 7".

## 1. Setup

- **Server:** a new scratch server in this session's scratchpad. The Trash `CreEngServer` copy is gone, so the
  Forge 1.19.2-43.3.9 libraries came from `~/.local/share/atlauncher/servers/Minecraft1192withForge`. 374 server
  mods are symlinked from `mods/` by `mod_sides.md`, and `vinery_upstream/` is linked too (the Vinery port won't
  load without it).
- **Jar: patcherMod 1.0.7, not 1.0.8.** The deployed 1.0.8 jar crashes a dedicated server at mod loading (3.1). Its
  `worldgen` package is byte-identical to 1.0.7's, so every result here holds for 1.0.8. Against Phases 1-6 (1.0.6),
  the jar adds the stone-base earth tag, the per-chunk census fix, and the LocateLoads, GroundMatch and PoolTuner
  changes.
- **Base toml:** the instance's own file plus `structureDensity` 4.0, earth fill and water fill `log = "ALL"`,
  meta-structure log and placement-priority log on. The 1.0.7 config spec dropped the 1.0.8-only `[time]`,
  `[advancements]` and `ovenIngredientCounts` from the scratch copy, which doesn't matter here. The instance's
  toml and scripts were never edited.
- **Worlds** (seed 8675309, 4 locator threads):

  | World | Config | Area |
  |---|---|---|
  | `st_c` | base | Main square: Chunky radius 800 at -1400 -2592 (10,201 chunks, 29 min) |
  | `st_c2` | base + the tag patch (3.2) | The same main square |
  | `st_c3` | switch boots 2-4 | Small squares radius 160, the inner main square radius 400, a coast square at -1400 -1000 |

- **Method:** each run's `[earth-fill]` / `[water-fill]` lines were digested per start (scratchpad `ef.py`).
  Region files were read for starts, piece boxes, junctions, heightmaps and entities. `/locate` was used to make
  the server build rare starts (mansion, pond, fisherman house) and to check answers.
- **Duplicate lines:** each chunk logs twice, minutes apart, with identical numbers. The placement-priority
  neighbour pass rebuilds its starts. That re-check also runs earth fill again, which costs time but isn't wrong.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-52 | DATA | Per size in 2.1. Default limits cancel 8% of small, 7% of medium and 1 of 12 large starts (prune on); most cancels are underground starts |
| ST-159 | PASSED | Probed cancels: a beach village over sea 21 blocks deep, a Farmer's Delight cooks house on a 66-block cliff, ocean cabins in open sea |
| ST-160 | PARTLY | Overrides PASSED (`=5,7` keeps the size's mound limit 8, `=5,7,3` sets 3). Sizes: ruins `small` 24/8 as expected, but some villages log `small` (2.2) |
| ST-162 | PASSED | `/locate` built 4 mansion starts (3 dropped by peaceful radius, 1 answered at 2560 -5504): no `[earth-fill] minecraft:mansion` line |
| ST-167 | FAILED as shipped; PASSED with the tag fix | `earth_fill/prune` is empty, so nothing is ever pruned (3.2). Patched: village cancels 4 -> 2 in the same square; the probes are clean |
| ST-168 | PASSED (with the tag fix) | 4 pruned lines, all villages; both pruned villages `/locate` at their own chunk |
| ST-169 | PASSED | `prune = false`: 0 pruned lines; the same two villages log CANCELLED with the same numbers as the unpatched run |
| ST-67 | PASSED | Circus tent [-144, -228]: 784 earth with trim off, 520 with it on; the difference is the 264 pad blocks |
| ST-53 | PASSED, one clause wrong | At zero, every start with any gap cancels and none without one. "The slot stays empty" holds only for one-structure sets (2.3) |
| ST-54 | PASSED | `-1,-1,-1` (savanna villages; the square had no plains) never cancels; `0,0,0` ancient ruins cancel at 3 and 7 air; exempt ids never log |
| ST-57 | DATA | 97% of Hopo lines are 0 into ground / 0 into air; the 32 / 16 limits cancel 2 of 164 (2.4) |
| ST-58 | PASSED | At 0 / 0 with no overrides, shore ruins and the swamp house cancel and are missing; every pond on a 41-miss search cancels |
| ST-59 | PASSED | A fish pond (1 water block) and a swamp fisherman house (268 into dry ground) are kept and found by `/locate`; slope copies cancel on 26-108 blocks into air |

### 2.1 ST-52: earth fill per size (main square, default limits, prune working)

Starts judged once each (`st_c2`). **Underground** = Subterrestrial cabins, Terralith's `underground/*`, Philip's
ancient dungeons, the ancient city and YUNG's dungeons.

| Size (limits air / mound) | Starts (underground) | Cancelled | Earth median / p90 / max | Air after beard p90 / max | Mound p90 / max | Over water, nonzero |
|---|---|---|---|---|---|---|
| small (24 / 8) | 1833 (1617) | 160, of which 141 underground | 64 / 100 / 146 | 0 / 94 | 2 / 94 | 104 starts, max 72 |
| medium (64 / 24) | 276 (178) | 22, 10 underground | 186 / 302 / 584 | 25 / 174 | 7 / 170 | 2, max 50 |
| large (160 / 64) | 12 (0) | 1 (carp fish pond, air 209) | 766 / 1028 / 1907 | 24 / 442 | 0 / 213 | 0 |
| huge (320 / 128) | 1 (0) | 1 (drum tower: 1366 air, 239 mound) | 5570 | - | - | 0 |

- **Surface only:** small 216 starts with 19 cancels (9%), medium 98 with 12 (12%). Small cancels are spread out:
  llama stable and ancient ruins 2 each, then single watchtowers, a mini clock tower and a beach village. Medium
  cancels are Farmer's Delight cooks houses, campsites, an OGT outpost, a crypt and the big horse stable.
- **Cancel reasons:** small: water + mound 96 (almost all ocean cabins), air + mound 31, mound 30, water 2, air 1.
  Medium: air + mound 13, mound 7, water 1, air 1. A mound column almost always comes with air or water, so the
  mound limit rarely decides on its own.
- **Underground starts dominate** the small size. Ocean cabins cancel 95 of 181: their fixed height lands in open
  sea water. The kept ones sit under shallow sea floors. That is a correct cancel. Ancient dungeons cancel 35 of
  807, jungle cabins 11 of 157.
- **Drum tower:** with its stone base counted (1.0.7), it is the only `huge` start and is cancelled at 1366 air.
  It is the same structure as the M2 open item (cancelled 4 of 5 on slopes).
- **Unpatched (as shipped):** pruning never runs. Large: 14 starts, 5 cancelled (35%), against 1 with the tag fix.

### 2.2 ST-160: villages that log `small`

Earth is counted per jigsaw start, so a small village is a `small` start:
- `minecraft:village_taiga`: 128 and 135 earth
- T&T `village_beach`: 126
- T&T `exclusives/village_mediterranean` and `village_tudor`: 42-74

They get small limits (24 / 8). The rest of the villages log medium or large. There was no Terralith fortified
village in the square, so that part wasn't seen.

### 2.3 ST-53: "the slot stays empty"

In a set with several structures (all groups, and vanilla's own multi-structure sets), a cancelled start lets the
set try its next member. That is vanilla's selection loop. Seen at zero:
- `subterrestrial:jungle_cabin` -> `deepslate_cabin`
- `village_taiga`, a Terralith fortified village and T&T's Swedish village -> T&T `village_old_growth_taiga`
- several houses, a camp and a watchtower -> `watchtower_building:fresh_watchtower`

The replacement is judged by earth fill too, so nothing hangs. Only the test's wording is wrong.

### 2.4 ST-57: water fill numbers

Main square at default limits, plus the coast square (counts don't depend on the limits):

| Structure | Lines | Into dry ground | Into air |
|---|---|---|---|
| Hopo warm small ruins | 241 | 0, except 1 at 115 | 1-5 in 3 lines |
| Hopo small ruins | 29 | 0 | 0 |
| Hopo ruins | 17 | 0 | 14 and 25 (16,148 water blocks) |
| Hopo fossils | 42 | 111 in 1 line | 4 in 1 line |
| T&T ocean outpost, sushi stall | 3 | 0 | 0 |
| Fish pond (common) | 5 | 0-1 (the template has 1 water block) | 0-1 |
| Carp pond | 4 | 103-144 | 26-67 on slopes |
| Swamp fisherman house | 3 | 160-268 | 0 on flat swamp, 108 on a slope |

The 32 / 16 limits cancel 2 of 164 Hopo starts in the main square: a fossil 111 blocks into ground and a ruin
25 into air.

## 3. Bugs

### 3.1 patcherMod 1.0.8 can't boot a dedicated server (time master, not worldgen)

- **Symptom:** the first boot died in FML with `MixinTransformerError` / `ClassMetadataNotFoundException:
  net.minecraft.client.Minecraft` while loading Serene Seasons.
- **Cause:** `SereneSeasonsClockMixin` is a common mixin on `sereneseasons.handler.season.SeasonHandler`. Its
  `onClientTick` inject calls `Minecraft.getInstance()` and `mc.levelRenderer` directly. Mixin resolves every
  owner class in a mixin method when it applies it, and client classes don't exist on a dedicated server.
- **Impact:** 1.0.8 is staged and [plan_1.0.8.md](../../plan_1.0.8.md) says it hasn't booted on a dedicated
  server. As staged, the server won't start.
- **Fix idea (not made):** move the client body into a client-only helper (for example a static method on
  `time/TimeClient` that returns the new sub-season or null), so the mixin only references patcherMod classes. Or
  split it into a client mixin on the same target.
- After the fix, boot a scratch server on the 1.0.8 set once.

### 3.2 `type/village` is not a real tag, so pruning never runs (ST-167)

- **Proof:** `locate structure #creativeengineering:earth_fill/prune` logs `NamedSet(...)[[]]`: empty.
  `#creativeengineering:type/village` is empty too, while `earth_fill/exempt`, `stone_base` and
  `water_fill/ponds` are filled.
- **Cause:** `earth_fill.js` fills `earth_fill/prune` with `#creativeengineering:type/village`. But the tags
  handler in `structure_overrides.js` only turns the `meta/*` tags (plus the fit tags and way-sign destinations) of
  `structure_tags.json` into real structure tags. The `type/*` tags exist only inside the script.
- **Impact:** pruning has never run in a game since it was built (10-13; only the headless sim exercised it). The
  CEConfig overrides example `#creativeengineering:type/village=-1,-1,-1` would match nothing for the same reason.
- **Tested on the scratch copy only:** the filter also took `t === 'type/village'`. The prune tag then held 37
  structures and pruning worked:

  | Village | Prune off | Prune on |
  |---|---|---|
  | `village_taiga` [-81, -181] | CANCELLED | 5 of 124 pieces dropped (a shepherd's house over 78 air / 58 mound), kept |
  | `village_savanna` [-142, -130] | CANCELLED | 8 of 171 dropped (houses over 27-58 water), kept |

  - In the same square, plain and anchor village cancels went 4 -> 2.
  - Pruned starts: 119 and 163 pieces. No kept junction points at a missing piece (the street ends there).
  - No rigid piece hangs more than 6 blocks over a quarter of its columns.
  - Every villager and the golem stand inside kept pieces, and nothing floats.
- **Fix options:**
  - (a) `structure_overrides.js` adds the `type/*` tags as real tags, like `meta/*`. This also makes `#type/...`
    overrides work.
  - (b) `earth_fill.js` lists the village ids itself.

### 3.3 Pruning can tighten the limits it is judged by (finding, not a crash)

- **What happens:** pruning lowers a start's earth count, which can move it down a size class.
- **Example:** `village_savanna` [-165, -68] lost its pet shop (102 air), dropping from 695 to 583 earth. That is
  large -> medium, so 64 / 24 limits replaced 160 / 64. It was then cancelled on 40 mound columns, which the large
  limit would have allowed.
- **Fix idea:** take the size from the start before pruning.

### 3.4 `/dumpstructuregen` is broken

- **Symptom:** every set gets `_errors` ("Cannot find function locateOffset / frequencyReductionMethod / frequency
  / salt / spacing ...") and an empty structure list.
- **Not this setup:** the instance's own dump from 10-18 18:31 (your client) is the same. The 10-14 Phase 6 dump
  worked, and `dump_structuregen.js` hasn't changed since 09-27, so something else changed between 10-14 and 10-18.
- **Impact:** `tools/check_structure_live.py` reads this file. Not chased.

## 4. Values changed

All of these were in the scratch copy only and are gone with it:
- Base: `structureDensity` 4.0, both fill logs `ALL`, the meta and priority logs on.
- Boot 1b: `pruneMaxHanging` / `pruneMaxMound` 0 / 0 (a diagnostic, before the tag was found).
- Boot 2: `prune = false`, `templateTrim = false`, two test overrides.
- Boot 3: every size `[0, 0]`, `maxOverWater` 0, `beardReach` 0, three overrides.
- Boot 4: water fill `maxIntoGround` / `maxIntoAir` 0, `overrides = []`.
- The `type/village` patch in `srv/kubejs`.

The instance's `config/` and `kubejs/` are unchanged.

## 5. Follow-ups for you

*Resolved 2026-10-19 (user: fix and redeploy as 1.0.8; [11_fixes_1_0_8.md](11_fixes_1_0_8.md)): 3.1 the clock mixin is split into a server and a client mixin; 3.2 fix (a), the `type/*` tags are real tags; 3.3 accepted as is (user: "pruning dropping a size class is fine"); 3.4 the dump's protected placement fields are read by patcherMod `worldgen/PlacementInfo` (the empty structure lists were the emptied sets, not a bug).*


1. **3.1 before deploying 1.0.8:** fix the Serene Seasons mixin, rebuild and restage. Then do one scratch boot on
   the 1.0.8 jar set.
2. **3.2:** pick fix (a) or (b) for the prune tag. With it in, ST-167 and ST-168 pass as tested here.
3. **3.3:** decide whether the size should come from the unpruned start.
4. **ST-52 / ST-57 limits:** the numbers are in 2.1 and 2.4. Defaults cancel about 8-12% of surface starts, nearly
   all on real cliffs or water. Nothing here argues for a change except the drum tower (huge, 1 of 1 cancelled; the
   M2 item).
5. **Test text:** ST-53's "the slot stays empty" should read "the set's next member may take the slot". ST-160's
   "villages medium or large" should allow small villages.
6. **3.4:** `/dumpstructuregen` needs a look before the next `check_structure_live.py` run.
