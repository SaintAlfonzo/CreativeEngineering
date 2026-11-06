# Locate: cheaper structure searches, miss logging, batch and census commands, deferred deep terrain

Test prefix **LO-** · Last updated **2026-10-19** · Overall status **DEPLOYED, in-game test pending** (went out INCOMPLETE with
pack 1.0.6 on 2026-10-14 as part of the structure tuning, see [plan_1.0.6.md](plan_1.0.6.md); fixes with 1.0.7; offline checks
pass; LO- tests ran in the structure_tuning 8.0 scratch run, finished 2026-10-19; findings come from the decompiled 1.19.2
classes and Async Locator 1.3.0; decisions in 4)

## 0. At a glance

| Part | Status | Where it lives |
|---|---|---|
| 3.1 Fold rules into the "could it start here?" check: stages A (set frequency, exclusion zone) + B (biomes); C-E on hold | BUILT 2026-10-12 | `worldgen/LocatePrediction` (from `StructureCheckFrequencyMixin` and `MetaSlots.predictGenerationPoint`) |
| 3.2 Non-blocking miss loads | BUILT 2026-10-12; still blocked in 1.0.6 (LO-05 failed), FIXED in patcherMod 1.0.7, DEPLOYED 2026-10-18; LO-05 rerun pending | `worldgen/LocateLoads` (redirect in `ChunkGeneratorMetaLocateMixin`, and `MetaSlots.locateAt`) |
| 3.3 StructureCheck thread safety | BUILT 2026-10-12 | `mixin/StructureCheckThreadSafeMixin` |
| 3.4 Miss logging (misses before a hit, per search and per structure) | BUILT 2026-10-12 | `worldgen/LocateStats` + `mixin/ChunkGeneratorMetaLocateMixin` |
| 3.5 `/celocate batch` | BUILT 2026-10-12; `stop` label fixed in 1.0.7 (DEPLOYED 2026-10-18) | `worldgen/LocateCommand`, `compat/AsyncLocatorBridge` |
| 3.6 `/celocate census` (with generation-time miss reasons) | BUILT 2026-10-12; "known" bug (LO-03, LO-14) FIXED in 1.0.7 (DEPLOYED 2026-10-18); biome-tie false negatives FIXED 2026-10-27 (DEPLOYED to the client with 1.1.04 2026-10-29, server on 1.1.03; SQ-TIE-01 not run). SQ-WG30-C02 (2026-10-30, 1.1.04 jar) still logged 5 biome false negatives (old refinery x4, a mineshaft): the fix does not cover them, TODO `structures/testing #4`, `structures/rates-and-groups #13`; 2026-11-01: cause not found from the code (structure_tuning.md ST-218), each biome false negative now carries a diagnosis in `falseNegativeAt` (point again, its biome and nearest entries, the built start piece) | `worldgen/LocateCensus`; reasons from the earth fill, biome rules, priority and frequency mixins |
| 3.7 Distant Horizons generation clipped below y 48 with a fake seabed (deep oceans around y 8, other oceans just under 48); pregen on the final server world | BUILT 2026-10-12 (option A), DEPLOYED with 1.0.6 (2026-10-14; LO-12 passed on that jar); LO-11 [game] open; pregen is a deploy step | `worldgen/DistantClip`, `mixin/NoiseBasedChunkGeneratorDistantClipMixin`, `NoiseChunkDistantClipMixin`, `ConfiguredFeatureDistantClipMixin` |

**Main findings:**
1. **A locate miss** happens when the cheap check says a structure could start in a slot, and the chunk is then
   generated (to `STRUCTURE_STARTS`) to find out, and nothing is there. Each miss costs a chunk's worth of structure starts.
   That includes every set whose slot lands in that chunk, earth-fill terrain sampling and placement-priority
   predictions of nearby slots.
2. **Async Locator does not take misses off the server thread.** The search runs on the locator thread. Each miss
   calls `level.getChunk`, which from another thread hands the call to the server thread and waits. The server thread then
   blocks in `managedBlock` until the chunk has its structure starts. A long run of misses is still a
   string of tick stalls (3.2).
3. **Vanilla's check is thin.** It runs `Structure#findGenerationPoint` and nothing else: no biome test on the
   result, no set-level `frequency`, no `exclusion_zone`. Our 7.1 rules make misses more common still (3.1).
4. **More than one locator thread is not safe yet.** `StructureCheck` keeps its caches in plain `HashMap`s. Each
   search reads and writes them, and the server thread writes them as chunks load (3.3).
5. **Keeping a list of misses to re-check later is not useful.** A slot's outcome is deterministic and is saved
   with the chunk. Counting misses is useful: it shows which structures are expensive to find, and the census
   turns it into measured spawn rates (3.4 to 3.6).

**Knobs:** `config/creativeengineering-common.toml`
- `[worldgen.locate]`: `foldPlacement` (true), `foldBiomes` (true), `nonBlockingLoads` (true), `threadSafeStructureCheck`
  (true, read when a dimension loads), `logMissesAtLeast` (8; 0 = every search, -1 = never), `batchRadius` (100 chunks),
  `batchTimeoutSeconds` (120), `censusRegionChunks` (32), `loadTimeoutSeconds` (300), `searchTimeoutSeconds` (120 since 2026-11-02; 300 from 2026-10-30, 120 before; 3.5; structure_tuning.md 3.27).
- `[worldgen.distant]`: `clip` (true), `clipBelowY` (48, rounded down to a multiple of 16), `oceanFloorY` (48),
  `deepOceanFloorY` (8), `floorVariation` (4), `floorScale` (24 blocks), `skipFeatures` (true).
  The clip and the feature skip apply in the Overworld only (since 2026-10-30, TODO `locate #4`): the Nether and the End
  also build from y 0, so without the gate their LODs got a stone band up to y 47 (no lava sea, a floor under the End void).
  DH caches LODs: a client that already saw a wrong Nether or End LOD keeps it until DH's data for that dimension is deleted.
- Async Locator: `asyncLocatorThreads` in `<world>/serverconfig/asynclocator-server.toml` (pack default 1; 4 on the
  scratch server, see 6).

Every fold and the non-blocking loads apply to the next search without a restart; the thread-safe cache needs a rejoin.

**How to measure:** `/celocate stats` (misses per structure since boot), `/celocate batch` (one locate per
structure, written to `kubejs/exported/worldgen/locate_batch.json`), `/celocate census <radius>` (every slot in an
area, predicted against actual, written to `kubejs/exported/worldgen/locate_census.json`).

## 1. Overview

`/locate`, explorer maps, cartographer and modded map trades, dolphins and Eyes of Ender all call
`ChunkGenerator#findNearestMapStructure` (`m_223037_`). With Async Locator (all six of its feature switches on) it
runs on an `asynclocator-N-thread-M` thread. For every placement of the wanted structures it walks outward:
- **random spread:** ring by ring over grid cells. Each cell gives its one candidate chunk
  (`getPotentialStructureChunk`).
- **concentric rings:** every ring position, nearest first.

For each candidate chunk it calls `getStructureGeneratingAt` (`m_223198_`). That asks
`StructureCheck#checkStart` (`m_226729_`), which answers from:
1. the chunk's structure data, if the chunk is loaded or saved (`START_PRESENT` / `START_NOT_PRESENT`), or
2. a **prediction**, `canCreateStructure` (`m_226755_`), cached per structure and chunk in `featureChecks`:
   "no" -> `START_NOT_PRESENT`, "maybe" -> `CHUNK_LOAD_NEEDED`.

On `CHUNK_LOAD_NEEDED` the search loads the chunk to `ChunkStatus.STRUCTURE_STARTS`, which runs the real
`createStructures` for that chunk, and reads the start. If there is no start, that is a **miss**. The search moves
on to the next cell; it ends at the first hit in the nearest ring that has one.

**Prediction errors only go one way today.** The prediction may say "maybe" wrongly (a miss: wasted work), but it
must never say "no" when the real answer is yes. A false "no" makes `/locate` and maps skip the nearest real
structure and point at a farther one. Nothing would show it, and the census (3.6) is the only check that would
catch it. **Every rule folded into the prediction has to be exact, or conservative: it may only say "no" when
generation would certainly say no.**

## 2. Reference

### 2.1 What the vanilla prediction checks (1.19.2)

| Check | In generation (`createStructures` / `tryGenerateStructure`) | In the search / prediction |
|---|---|---|
| Grid cell -> candidate chunk | yes | yes (`getPotentialStructureChunk`) |
| Set `frequency` (placement-level, with its reduction method) | yes (`StructurePlacement#isStructureChunk`) | **no** |
| `exclusion_zone` (e.g. outposts away from villages) | yes (`isStructureChunk`) | **no** |
| Weighted pick among a set's members | yes | **no**: each wanted structure is checked as if it were alone |
| `findGenerationPoint` (start piece, height sample) | yes | yes, the only check |
| Biome at the generation point | yes (`Structure#isValidBiome`) | **no**: the predicate is passed into the context, but jigsaw `addPieces` never reads it, and the result's biome is never tested |
| Piece assembly, `StructureStart#isValid` | yes | no (jigsaw pieces are assembled lazily in the stub) |

So even vanilla structures miss in every wrong-biome slot. A village search across an ocean generates the
structure starts of every village slot it passes.

### 2.2 What the pack adds, and where each rule runs

| # | Rule | Generation | Prediction today |
|---|---|---|---|
| R1 | Dead placements (frequency 0) | not generated | removed from the search (`DeadPlacementFilter`) |
| R2 | Per-member frequency roll (`StructureFrequencies`) | at store time | **yes**, exact (`StructureCheckFrequencyMixin`) |
| R3 | Meta conversion and replacements (`MetaSlots`) | `tryGenerateStructure` HEAD | **yes** (`StructureCheckMetaMixin`, anchors' biomes + rules via `MetaSlots.fits`) |
| R4 | Biome rules + biome fits + replacement biome narrowing | valid-biome predicate | **yes**, exact since 2026-10-12: stage B (`LocatePrediction.fits`, `foldBiomes`); metas and replacements through `MetaSlots` |
| R5 | Surface check (`StructureBiomeRules.surfaceCheck`) | on the built start | no |
| R6 | Earth fill (`EarthFill.filter`) | on the built start | no |
| R7 | Placement priority drop + peaceful radius (`PlacementPriority`) | on the built start | no |
| R8 | Slot veto (an earlier member's frequency miss closes the slot) | `tryGenerateStructure` HEAD | no |
| R9 | Weighted group pick (another member took the slot) | vanilla | no |
| R10 | Set frequency, exclusion zone | vanilla | **yes**, exact since 2026-10-12: stage A (`LocatePrediction.placementRejects`, `foldPlacement`); vanilla has neither (2.1) |

### 2.3 Async Locator 1.3.0

- `AsyncLocator.locate(level, HolderSet|TagKey, pos, radius, skipKnown)` submits to a fixed pool of
  `asyncLocatorThreads` threads (default 1, range 1 to unbounded). `/locate` uses radius 100.
- The task calls `findNearestMapStructure` directly. **It catches nothing.** If the search throws, its future never
  completes and `/locate` simply never answers. A batch must use timeouts.
- `LocateTask.then` runs on whichever thread completes the future; `thenOnServerThread` hops to the server.
- The pool is a private static field (`LOCATING_EXECUTOR_SERVICE`), and there is no public way to submit other work.
  The census needs an accessor mixin on it (3.6).
- The pack already routes LC and Cloud Storage map trades through it (`compat/AsyncMapTrades`).

### 2.4 Examples

- **Village from spawn on an ocean seed:** each village slot over water is a miss under vanilla rules (R4/biome).
  With 7.1, one settlements slot can be any of about 30 members. `/locate minecraft:village_plains` also misses
  every slot a town, sky village or T&T village took (R9), and every slot dropped by priority (R7).
- **Pillager siege camps (meta, ~never spawns):** every anchor slot that keeps its frequency and converts is a "maybe".
  The anchor biomes rarely meet the siege parts, so nearly all are misses. This is the dead-structure hang pattern at a smaller scale.
- **Entrance dungeons near water:** the prediction says yes, the surface check (R5) rejects the start. Miss.

## 3. What the pack would change

### 3.1 Folding rules into the prediction

One prediction entry point: the existing `StructureCheckMetaMixin` redirect of `findGenerationPoint` inside
`canCreateStructure`, grown into `worldgen/LocatePrediction`. It runs stages in cost order and stops at the first
certain "no":

| Stage | Rule | Exact? | Cost per candidate | Saves | Tradeoffs / risks | Verdict |
|---|---|---|---|---|---|---|
| A | **R10 set frequency** (`isStructureChunk`'s frequency part) | exact (same hash roll) | a few multiplications | every slot of a set with frequency < 1. That includes every set `scalePlacement` scaled down and every group with a group-level frequency | none found. Needs the placement of the set being searched; the search already has it, so pass it through a ThreadLocal set in `getNearestGeneratedStructure` | **DECIDED: build** |
| A | **R10 exclusion zone** | exact | `hasStructureChunkInRange` for the other set (grid math, no chunk loads) | outposts/camps near villages | the other set's frequency is ignored by vanilla here too, so we copy vanilla's behaviour (no extra exactness needed) | **DECIDED: build** |
| B | **R4 biome + biome rules + fits + narrowing** | exact (same predicate, same generation point) | one `getNoiseBiome` sample (climate sampler, microseconds) | every wrong-biome slot, the biggest single source of misses in vanilla and here | (1) structures that pick their position from a modded `findGenerationPoint` that is not deterministic for the chunk would break exactness. Vanilla seeds it from the chunk, so the census must show 0 false negatives. (2) `MetaSlots.fits` turns an exception into "maybe": keep that. (3) Already written for metas: generalise `MetaSlots.fits` to every structure. | **DECIDED: build** (the "easy one") |
| C | **R8/R9 group pick replay** | exact only if every member picked before the wanted one is predicted exactly | replay the weighted pick order (same `WorldgenRandom` seeding as `createStructures`), then stages A-B for each earlier member | slots another member took; large for big groups (settlements, ruins, surface dungeons) | An earlier member that passes A-B may still fail later (R5-R7, assembly). Then the wanted structure gets the slot, so "the earlier member won" is not certain. **Conservative version:** say "no" only when an earlier member passes A-B *and* is in no R5-R7 tag *and* is a vanilla-style jigsaw/single piece that cannot fail assembly. Otherwise "maybe". This keeps it exact at the cost of a weaker cut. Also needs the slot veto (R8): an earlier member that would build but fails its frequency roll closes the slot. That needs the build to be known, so the conservative version skips it | on hold (decision 1); census decides |
| D | **R5 surface check** | exact if run on the real start | needs the start piece's box: for jigsaw, the stub's start piece is built eagerly, so the box is available without assembly; for others, assembly | entrance dungeons, crypt staircases near water | only the `surface_check` structures (a handful); assembling non-jigsaw starts costs about what the chunk load costs, minus IO and the other sets | on hold (decision 1) |
| E | **R6 earth fill, R7 priority** | exact (PlacementPriority's predictor already replays the full pick on a scratch ProtoChunk with every mixin) | a full start generation per candidate (jigsaw assembly + terrain sampling + nearby predictions). Cheaper than the chunk load: no IO, no main thread, no other sets, cached per (generator, set, chunk) | the remaining misses: water-cancelled ruins, dropped villages/camps | Moves heavy work onto the locator thread, where it is fine. Doing it for **every** candidate, not just the ones A-D let through, would cost more than it saves. Run it last, only on survivors, behind `foldExpensive`. Its results feed the same cache that generation uses, so the real chunk later reuses them. Must run with PlacementPriority's `isolated()` state save. | on hold (decision 1) |

**What folding cannot remove:** misses from assembly failures in other mods' structures (YUNG's, T&T, a
structure's own height checks). Those stay "maybe".

**Interaction with the search:** a "no" is cached in `featureChecks` exactly like vanilla's. The cache is
per server run, so nothing persists wrongly across a config change.

### 3.2 Non-blocking miss loads

In `getStructureGeneratingAt`, when the thread is not the server thread, redirect `level.getChunk(x, z,
STRUCTURE_STARTS)` to `ServerChunkCache#getChunkFuture(x, z, STRUCTURE_STARTS, true)`. That method is public; from
another thread it composes onto the server's executor without `managedBlock`. Then `join` it **on the locator
thread**. The server keeps ticking while the chunk is generated on the worldgen workers. This fixes the stalls for
every Async Locator user (maps, trades, `/locate`), not just our commands. Risk: low. It is the same
ticket path vanilla uses (`TicketType.UNKNOWN` at the status level); the only difference is who waits. Switch:
`nonBlockingLoads`.

**As built in 1.0.6 it still blocked (LO-05, [05_phase_5.md](testing/structures/05_phase_5.md) 3.1).**
`LocateLoads.offThread` added its ticket and called `getChunkFuture` inside one `supplyAsync(..., server)` task.
`getChunkFuture` checks the calling thread: on the server thread it calls `getChunkFutureMainThread` and then waits
in `managedBlock` until the chunk exists. So every load still stalled the tick, as long as the generation took.
**Fixed 2026-10-18 (patcherMod 1.0.7, DEPLOYED 2026-10-18):** the server task now only adds the ticket.
The search thread waits for that task, then calls `getChunkFuture` itself. Off the server thread that call only hands
`getChunkFutureMainThread` to the server's chunk executor and returns the future, and the search thread waits on it
(a quarter second at a time, so a stopped or expired search still ends). The ticket is removed in a server task when
the load ends, as before. Retest: LO-05, LO-13.

### 3.3 StructureCheck thread safety (prerequisite for more threads)

`StructureCheck.loadedChunks` (fastutil `Long2ObjectOpenHashMap`) and `featureChecks` (`HashMap` of
`Long2BooleanOpenHashMap`) are read and written by `checkStart` on every search thread. The server thread writes
them in `ServerLevel#onStructureStartsAvailable` -> `onStructureLoad`, and through `StructureManager#addReference`
-> `incrementReference`. With one locator thread, that already races the server thread (rare,
silent). With several, two searches can `computeIfAbsent` the same map at once. A fastutil open-hash map can then
loop forever or lose entries. **Fix:** a mixin that makes `checkStart`, `onStructureLoad` and `incrementReference`
`synchronized` on the instance. The expensive part of `checkStart` is the prediction, so run the prediction outside
the lock: compute, then `putIfAbsent`. Our own state is already thread-safe: ThreadLocals for the roll, veto,
conversion and priority state; volatile staged snapshots; a synchronized prediction cache.

### 3.4 Miss logging

Cheap counters, one log line per expensive search:
- `findNearestMapStructure` HEAD (the existing `ChunkGeneratorMetaLocateMixin` hook) opens a ThreadLocal
  `Search` (depth-counted, because the meta-expanded search re-enters). RETURN closes it.
- `getStructureGeneratingAt` counts `candidates`. A `CHUNK_LOAD_NEEDED` counts `loads`; a load with no valid start
  counts `misses`. If 3.1 is built, also count which stage said "no" (`savedByStage[A..E]`): the measure of what folding saved.
- On close: if `misses >= logMissesAtLeast` (default 8), one INFO line in `logs/latest.log`, otherwise nothing:
  `[locate] minecraft:village_plains from (120, -340): hit at (1904, 2210) after 23 misses, 41 loads, 310 candidates, 2140 ms [asynclocator-1-thread-2]`.
  A search with no hit logs the same with `no hit`.
- Totals per structure (`ConcurrentHashMap<ResourceLocation, LongAdder[]>`: searches, hits, misses, misses
  before hit, loads, ms) since boot, printed and written by `/celocate stats`.

Cost: a ThreadLocal read and a few increments per candidate, nothing per tick. A miss costs a chunk generation,
which is many orders of magnitude more. Off unless something searches.

**Not stored:** the miss positions themselves. A slot's outcome is fixed once the chunk is saved, so
re-checking them in the same world always gives the same answer. Comparing two configs on the same seed is
what the census is for.

### 3.5 `/celocate batch [<structure|#tag|namespace|all>] [radius] [skipKnown]`

- Op level 2. Builds the target list from the structure registry minus dead placements (R1). By default every live
  structure in the current dimension's generator (`possibleStructureSets`).
- Submits one `AsyncLocator.locate(level, HolderSet.direct(h), origin, radius, skipKnown)` per structure, so all
  work runs on Async Locator's pool and obeys `asyncLocatorThreads`. Each target gets its own `HolderSet`
  instance; the `Search` record from 3.4 is filed under that identity, so results and counters line up even with
  several threads.
- 120 s timeout per task (2.3: a throwing search never completes). A timed-out task is reported, not retried.
  Since 2026-10-13 every search, not just a batch's, has its own deadline (`[worldgen.locate] searchTimeoutSeconds`,
  120): past it the search's candidates and loads return "nothing" at once, so it ends as not found and frees its
  thread instead of being abandoned. `/celocate stop` and the server stopping end running searches the same way
  (structure_tuning.md 7.3.2). Batch rows then say `timed_out` or `stopped`.
- Progress line every 10 s in chat/console; at the end, `kubejs/exported/worldgen/locate_batch.json` (written
  through the same exporter as the other dumps) plus a one-line summary. Per structure: found, position, distance,
  candidates, loads, misses, saved by stage, ms. Sorted by misses.
- Use: "is every structure findable, and which ones are expensive": catches never-spawning structures (the siege
  camps), wrong biome tags and hangs.
- A bias to keep in mind: the first hit ends a search, so a batch samples only the slots between the origin and
  the nearest hit. Rates come from the census.

### 3.6 `/celocate census <radiusChunks> [centerX centerZ]`

- Lists every slot of every live set in the square (grid math per placement, the same as the search; ring positions
  for concentric sets). For each slot and each member, it records the prediction stage by stage (3.1). Then it
  requests the slot chunk to `STRUCTURE_STARTS` with `getChunkFuture` (non-blocking; one chunk at a time per locator
  thread) and reads every start in it.
- Runs on Async Locator's pool through an accessor mixin on `AsyncLocator.LOCATING_EXECUTOR_SERVICE`, split into one
  task per region (32 x 32 chunks), so `asyncLocatorThreads` sets its parallelism too.
- **Reasons:** while a census is active (one volatile flag check per reject point when not), each generation-time
  reject point (R5 surface, R6 earth fill, R7 priority/peaceful, R8 veto, R2 roll, R4 biome, invalid start) records
  `(chunk, structure) -> reason` into the census's map. Chunks that already existed before the census are reported as
  `pre-existing` (their generation already ran).
- Report `kubejs/exported/worldgen/locate_census.json` + a summary:
  - per structure: slots, predicted "maybe", actual starts, misses with reasons, saved by stage, **false negatives
    (must be 0; any non-zero row is a prediction bug)**
  - per group: the measured share of each member (actual starts / group slots), the number needed before
    stripping weights (structure_tuning 7, Todo), since relative frequencies now depend on biomes, priority and
    earth fill and are no longer what the weights say.
- Cost: only `STRUCTURE_STARTS` work for slot chunks (for a 32-spacing set, 1 chunk in 1024), no noise or features.
  Radius 256 chunks is a few thousand chunk loads.
- **One load per chunk, after every prediction (fixed 2026-10-18, DEPLOYED with 1.0.7).** The 1.0.6 census handled one
  slot at a time: predict, then load. Sets with a slot in every chunk (buried treasure, mineshafts) came first, so their
  load generated the chunk, and every later set there found it loaded and counted "known" (142,971 of 179,710
  member-slots in the Phase 6 census; LO-03 and LO-14 failed, [06_phase_6.md](testing/structures/06_phase_6.md) 3.1).
  Now each region's slots are grouped by chunk: every set that tries the chunk is predicted, then the chunk loads
  once, then every slot is scored. Chunks generated before the census still count "known", as designed. Retest:
  LO-03, LO-14.
- **Biome ties (fixed 2026-10-27, DEPLOYED to the client with 1.1.04; TODO `structures/testing #4`).** The 10-27 census (perf.md 10.9) had
  5 / 10 false negatives, all `terralith:underground/old_refinery` and `mining_outpost` at stage biome. Vanilla's
  `Climate.RTree#search` starts from the calling thread's previous result and keeps it when another entry is equally
  near. Terralith's `cave/deep_caves` and vanilla's `deep_dark` overlap at offset 0 (depth 1.05 to 1.2, erosion below
  -0.375), so at those points the biome depends on what the thread looked up before: the census or `/locate` thread
  could get `deep_dark` where the worldgen thread got `deep_caves`. Both structures list `deep_caves`; the other four
  members of the set don't, so they had no false negatives. `worldgen/TiedBiomes` keeps the one-sample test and,
  only when it fails on a `MultiNoiseBiomeSource`, walks the parameter list for every entry at the nearest distance;
  stage B and the biome-factor roll say no only if all of them fail. Generation is unchanged. Not covered (on
  purpose): `MetaStructure`'s own biome tests (they run at generation too) and `MetaSlots.biomeNear` (already a
  sampled heuristic). Harness 2026-10-27 on the real `Climate` classes and Terralith's list: the tie reproduced
  (`deep_dark` after a `deep_dark` lookup, `deep_caves` after a `deep_caves` one), 9 checks PASS, 7 ties in 20000
  random points. Live retest: SQ-TIE-01.

### 3.7 Deferring the bottom 64 to 96 blocks (investigation)

**This world:** the Overworld is y -64 to 320 (384 high). Tectonic's noise settings (`increased_height` false) and
Terralith's agree on `min_y` -64. The band from -64 to 0 (or to 32) holds the deepslate layer, deep ores and noise
ore veins, lava aquifers, the deep dark and ancient cities, most noise caves and aquifers, mineshafts, strongholds,
geodes, Tectonic's underground and lava rivers, and the underground structures (YUNG's, Philip's crypts,
Terralith's underground set).

**What the band costs, per stage** (estimates, to be measured):

| Stage | How cost scales with height | Bottom share (64 / 96 of 384) |
|---|---|---|
| `noise` | per noise cell over the full height; underground cells cost more (caves, aquifers, ore veins are evaluated only below the surface) | more than 17% / 25% |
| `surface` | top-down, stops near the surface | small |
| `carvers` | vanilla cave/canyon carvers span about y -56 to 180 | about a third |
| `features` | ores dominate decoration and are concentrated low; geodes, dripstone, sculk | likely large |
| `light` | dark solid sections are cheap | small |
| save/memory | 4 to 6 of 24 sections | 17% / 25% |

**Measure first:** Minecraft's JFR `ChunkGeneration` events give per-status times, and `tools/perf/perf_report.py`
section 11 reports them. One recording of a Chunky pregen on a scratch server gives the real split before anything
is built.

**Measured 2026-10-19** (LO-10; scratch server, patcherMod 1.0.7 jar = 1.0.8 worldgen, the instance's own toml at
density 1, seed 8675309, fresh world; Chunky square radius 64 chunks at 2144 -2592, 92% land: 16,641 chunks in
24.5 min, about 11 chunks/s; JFR as `server/LaunchServer.sh`; log
[testing/structures/10_phase_10.md](testing/structures/10_phase_10.md)). Worker time per status over the pregen
(6,639 s of worker time, about 4.5 cores busy on average):

| Status | Calls | Total s | Share | Mean ms | Median ms | p95 ms |
|---|---|---|---|---|---|---|
| `noise` | 27,133 | 2,577 | 38.8% | 95.0 | 87.8 | 232 |
| `features` | 21,520 | 1,617 | 24.4% | 75.1 | 52.8 | 217 |
| `light` | 16,641 | 1,249 | 18.8% | 75.0 | 59.2 | 183 |
| `structure_starts` | 358,852 (22,801 chunks) | 422 | 6.4% | 1.2 | 0.0 | 0.04 |
| `surface` | 27,133 | 275 | 4.1% | 10.1 | 10.6 | 25 |
| `full` | 16,641 | 235 | 3.5% | 14.1 | 2.1 | 70 |
| `biomes` | 32,526 | 195 | 2.9% | 6.0 | 6.7 | 20 |
| `carvers` | 27,133 | 66 | 1.0% | 2.4 | 2.3 | 6.4 |
| `spawn`, `structure_references`, `liquid_carvers`, `heightmaps` | | 4 | 0.1% | | | |

- `noise` + `features` + `light` are 82% of chunk generation. The estimate's "light: small" was wrong at the stage
  level (18.8%), though most of a dark section's light is cheap; the split by height is not in these events (they
  carry no Y), so the bottom-band share of each stage is still an estimate.
- `structure_starts` runs about 16 times per chunk (up to 23): the placement-priority neighbour pass and the meta
  predictions rebuild nearby starts. Nearly all repeats return in well under 0.1 ms; the whole stage is 6.4%.
- CPU by mod on the worker threads: vanilla/Forge 75.7%, creativeengineering 6.8% (`EarthFill` terrain prediction
  62% of that, `MetaStructure#generate` 11%), blueprint 3.7% (biome lookup), twigs 3.1% (almost all
  `ObeliskStructure#canGenerate`), copycats 2.9%, tectonic 1.7%, volcanic_caverns 1.6%, ferritecore 1.3%.
  Distant Horizons' LOD builder threads took 34.9% of all CPU samples on their own threads during the pregen.

**Ocean floors measured** (`python3 tools/seabed_heights.py`, OCEAN_FLOOR heightmaps of saved chunks; WorldG is
the current Tectonic setup, Workspace the largest save; coastal chunks add some land columns to the counts):

| Biome (save) | Columns | Min | Median | 90th pct | Below 48 | Below 0 |
|---|---|---|---|---|---|---|
| deep_lukewarm_ocean (WorldG) | 83,712 | -25 | 2 | 12 | 100% | 25% |
| deep_ocean (WorldG) | 18,432 | -5 | 4 | 6 | 100% | 4% |
| deep_cold_ocean (Workspace) | 247,552 | -15 | 5 | 11 | 99% | 12% |
| deep_ocean (Workspace) | 14,848 | -11 | 8 | 10 | 100% | 0.5% |
| deep_frozen_ocean (Workspace) | 13,568 | 2 | 11 | 63 (icebergs) | 61% | 0% |
| ocean (WorldG) | 130,560 | -5 | 48 | 55 | 47% | 0.2% |
| lukewarm_ocean (WorldG) | 267,776 | -3 | 50 | 56 | 25% | 0.2% |
| warm_ocean (Workspace) | 55,040 | -1 | 44 | 53 | 77% | 0% |

So deep-ocean seabeds lie around y 0 to 12 (down to -25), 40 to 60 blocks under a y 48 clip, and about half of
the shallow-ocean floor is below 48 too. Sea level is 63.

**Does it show?** The pack's DH config has `transparency = "DISABLED"`: water LODs are opaque and the seabed is
never drawn, so a flat floor at 48 is invisible. But the DH toml is a player config: a player who picks `FAKE`
(opaque, shaded by what is underneath) sees shallower-shaded deep oceans; `COMPLETE` (transparent) shows the floor itself.

**Ocean-safe clip variants:**

| Variant | How | Oceans | Land savings | Cost |
|---|---|---|---|---|
| Flat y 48 (decided) | fixed cutoff | wrong floor under any transparency other than DISABLED | full | none |
| Per column (not chosen) | cutoff = min(48, preliminary surface - 8). The preliminary surface level is the cheap density function aquifers already use, one value per column | correct | full on land; little in oceans (their columns are mostly water and seabed) | one lookup per column |
| Ocean biomes exempt | no clip where the column's noise biome at sea level is an ocean | correct | full | one biome lookup; coastlines blocky (biome cells are 4 blocks) |
| **Fake seabed by biome** (DECIDED, floors hovering around 48 for oceans and 8 for deep oceans; decision 4) | flat y 48 cutoff, no noise below it anywhere; below 48 the fill depends on the column: if the block at y 48 is water and the column's biome is an ocean, water continues down to a fake floor (oceans: 24 to 28, varied per column; deep oceans: 0), then sand/gravel; any other column gets stone | close to measured medians (ocean floors 44 to 50 median but a long tail down to about 14; deep 2 to 8) | full, oceans included | one biome read per column (the chunk already has its biomes); floors step at biome borders and at shores where the real floor above 48 meets the fake one |
| Fake seabed at the predicted floor (not chosen) | as above, but the fake floor is clamp(preliminary surface, 0, 47) instead of a fixed height per biome | smooth, follows the real seabed within a few blocks | full | one extra lookup per column |
| Lower flat cutoff | e.g. y -16 | correct for nearly all | much smaller (about 13% of the height instead of 29%) | none |

**Options:**

| Option | What it does | Gain | Risk / cost | Verdict |
|---|---|---|---|---|
| A. **Skip the band in Distant Horizons' own generation** | DH (`distantGeneratorMode = FEATURES`) generates throwaway chunks on its own threads for LODs. A mixin on the noise fill and feature placement, active only inside DH's batch generation (thread or `BatchGenerationEnvironment` context), fills the band with plain stone/deepslate (single-value palette sections, nearly free) and skips features placed in it | DH LOD builders were about 28% of all CPU during pregen (perf.md). Cave culling (`caveCullingHeight` 60) already hides most underground LODs, so nobody sees the difference | low: those chunks are discarded. **Cutoff: y 48** (decision 4), so the band is -64 to 47; above the cave-culling height's reach of most dark LODs, below most land surfaces. **Known side effect:** columns whose surface lies below 48 (deep oceans, deeper with Tectonic's `deeper_oceans`; ravines; lava rivers) get a flat floor at y 48 in distant LODs. Measured and handled below the options table: fake seabed by biome (decision 4). To verify: that DH 3.0.2 calls the vanilla `fillFromNoise` / `applyBiomeDecoration`, and how to detect its context. DH's API has a world-generator override register worth checking as the official hook | **DECIDED: build, clip below y 48** |
| B. **Pregenerate** | Chunky (already installed) pregenerates the spawn area on the server before players join | moves the load out of play time entirely | disk space; no code | **DECIDED: pregenerate the final server world** (not the scratch/test worlds) |
| C. **Real deferred band** (retrogen when a player goes deep) | Generate chunks with the band as a solid placeholder and mark them. When a player nears the band (e.g. below y 48 within 3 chunks), replay noise, carvers, features and structure pieces **clipped to the band** on the live chunks, then relight | real chunks: the band's share of noise + features, at the cost of a spike when someone first goes deep | **large**: replay needs a fake `WorldGenRegion` over FULL chunks with their 3 x 3 neighbours; features crossing the boundary (ore blobs, geodes, trees with roots, dripstone) must be split by a block filter in both passes; a feature that tests the world (air above, replaceable target) sees player changes in pass 2; underground structure pieces (ancient city, stronghold, mineshafts) must be placed late with clipped boxes, after their beard has shaped the noise; fluids above the band must never meet air (the placeholder must be solid); lighting and heightmaps must be fixed for columns whose surface lies in the band (Tectonic's deeper oceans and lava rivers make this real); block entities and entities spawn late; DH LODs of the band go stale; every mod that post-processes chunks (Create, Immersive Weathering, structure mods) is untested on a late pass | **not built** unless A + B are not enough and the measurement shows the band dominates |
| D. Raise `min_y` | shrink the world | permanent gain | removes content, changes the world, not a delay | rejected |

## 4. Limits and decisions

- **Exact or conservative only** (1). Any folded rule that cannot be proven exact says "maybe". The census's
  false-negative column is the proof.
- **No miss buffer** (3.4): outcomes are deterministic and persisted. Counting replaces storing.
- **More than one locator thread waits for 3.3.** Raising the thread count before the StructureCheck fix risks a hung
  or corrupted structure cache.
- **The batch uses Async Locator's public API; the census uses its pool through an accessor**, so both respect one
  thread setting. Neither runs a search on the server thread (feedback: Async Locator everywhere).
- **Stage E stays switchable** until the census shows it saves more than it costs.
- **Deep terrain:** measure, then A and B. C is documented here as the rejected-for-now design.

**Decisions (user, 2026-10-12):**
1. Fold the first three rules into the initial check: stage A (set frequency, exclusion zone) and stage B (biome,
   biome rules, fits, replacement narrowing). Stages C (group pick), D (surface check) and E (earth fill, priority)
   stay out for now; the census's saved-by-stage and miss-reason columns decide whether to revisit them.
2. Build 3.2 (non-blocking miss loads), 3.3 (StructureCheck thread safety), 3.4 (miss logging), 3.5
   (`/celocate batch`) and 3.6 (`/celocate census`), all through Async Locator's multithreaded pool.
3. Testing uses more than one locator thread (4 on the scratch server, after 3.3).
4. Distant Horizons' own generation is clipped below **y 48** (option A): no noise and no features in the band;
   real chunks are untouched. The band gets a **fake seabed** (decided 2026-10-12): in a column whose block at y 48
   is water, water continues down to a fake sand/gravel floor that **hovers around a per-biome height** (revised
   2026-10-12): **deep oceans around y 8** (4 to 12), **other oceans around y 48** (44 to 47 below the cut; where
   the real floor is above 48 it is generated normally). The height comes from a smooth 2D noise seeded from the
   world seed (features about 16 to 32 blocks wide), not per-column random, so the floor rolls instead of
   speckling. Every other column is stone. No predicted-surface lookup: the per-biome depth contrast is wanted.
   Knobs: CEConfig `[worldgen.distant]` `oceanFloorY` 48, `deepOceanFloorY` 8, `floorVariation` 4.
5. The final server world is pregenerated (option B). Option C is not built.

## 5. File map

- Vanilla, read from the decompiled 1.19.2 jar: `StructureCheck` (`checkStart`, `canCreateStructure`),
  `ChunkGenerator` (`findNearestMapStructure`, `getNearestGeneratedStructure`, `getStructureGeneratingAt`,
  `createStructures`), `Structure#generate` / `isValidBiome`, `JigsawPlacement#addPieces`,
  `ServerChunkCache#getChunk` / `getChunkFuture`.
- Async Locator: `brightspark.asynclocator.AsyncLocator`, `AsyncLocatorConfigForge`, `logic/LocateCommandLogic`.
- Pack, existing: [StructureCheckFrequencyMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureCheckFrequencyMixin.java),
  [StructureCheckMetaMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/StructureCheckMetaMixin.java),
  [ChunkGeneratorMetaLocateMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ChunkGeneratorMetaLocateMixin.java),
  [ChunkGeneratorLocateMixin](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ChunkGeneratorLocateMixin.java),
  [MetaSlots](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/MetaSlots.java) (`predictGenerationPoint`, `fits`, `locateAt`),
  [StructureBiomeRules](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/StructureBiomeRules.java),
  [EarthFill](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/EarthFill.java),
  [PlacementPriority](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/PlacementPriority.java),
  [AsyncMapTrades](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/AsyncMapTrades.java).
- Pack, built 2026-10-12 (under `development/creativeCommon/.../creativeengineering/`): `worldgen/LocatePrediction`, `LocateLoads`,
  `LocateStats`, `LocateCommand`, `LocateCensus`, `LocateReflect`, `DistantClip`, `compat/AsyncLocatorBridge`;
  `mixin/StructureCheckThreadSafeMixin`, `NoiseBasedChunkGeneratorDistantClipMixin`, `NoiseChunkDistantClipMixin`,
  `ConfiguredFeatureDistantClipMixin`. Open work: [briefs/worldgen_locate.md](briefs/worldgen_locate.md).
- Related writeups: [structure_tuning.md](structure_tuning.md) (rules, groups, metas, priority, 7 Todo),
  [perf.md](perf.md) (JFR, worldgen threads).

## 6. Offline checks

- `tools/test_worldgen_tools.sh` step 8, `LocateTest` (20 checks): misses before a hit, loads, candidates, stage
  counts, batch capture by HolderSet identity, totals, a stale search replaced, the fake seabed roll (range,
  smoothness, fixed by seed) and Distant Horizons region detection. Steps 1-7 still pass after the MetaSlots changes.
- Not covered offline (needs a server): the mixins themselves, stage A/B answers on real placements, the chunk
  loads, the census and the clipped noise. The census's false-negative column is the real check of A/B (LO-03).

**Scratch-server setup for LO- tests:** set `asyncLocatorThreads = 4` in the scratch world's
`serverconfig/asynclocator-server.toml` (only after 3.3 is built; until then 1). Use `max-tick-time=-1`. Run on a
fresh level-name.

## 7. Test suite

- [ ] **LO-01 fold off, baseline [scratch]:** all `fold*` false, `/celocate batch all` -> report written, every
  live structure has a row, misses recorded, no task times out. *2026-10-14 scratch phase 5 (structure_tuning 8.0.5, seed 8675309, 1.0.6 jar of 12:44, density 1, worlds st_l1/st_l2/st_l3; log testing/structures/05_phase_5.md): PARTLY: report written with 258 rows and misses (10660 in total, 2018 s, 4 threads); 42 rows `timed_out` at the 120 s batch timeout, so "no task times out" does not hold at the default. The 21 of them that folds on found came back at the same position folds off with a 900 s timeout.*
- [x] **LO-02 stages A+B [scratch]:** `foldPlacement` + `foldBiomes` on, same seed, same origin, batch again ->
  same found positions as LO-01 for every structure, total misses lower, `savedByStage` A/B non-zero. *2026-10-14 scratch phase 5 (structure_tuning 8.0.5, seed 8675309, 1.0.6 jar of 12:44, density 1, worlds st_l1/st_l2/st_l3; log testing/structures/05_phase_5.md): PASSED: 212 structures found in both batches: 210 at the same position, 2 (village_desert, village_savanna) in the same chunk but a few blocks apart = the meta layout difference between JVM runs, not the folds. The 21 found only folds on and the 4 found only folds off give the same position in the other mode at a 900 s timeout. Misses 2003 vs 10660; savedByStage placement 40670, biome 156421.*
- [ ] **LO-03 census, no false negatives [scratch]:** `/celocate census 128` with stages A + B on -> false
  negatives 0 for every structure. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): FAILED (census bug): the report says 0 false negatives, but the census only predicts the first set that tries each chunk. That set loads the chunk, so every later set there counts as "known" (142971 of 179710 slots on fresh terrain; mineshaft 37249/37249 known). Fix: per chunk, predict all sets first, then load once.*
- [ ] **LO-04 census rates [scratch]:** the same census -> per-group member shares written; compare with
  `gen_structure_groups.py` estimates. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): per-set shares written and valid (they come from the real starts, e.g. settlements 16 slots: village_savanna 0.19, village_towers 0.13); the comparison with the estimates is skipped (rates out of scope).*
- [ ] **LO-05 non-blocking loads [scratch]:** `/locate` a structure with many misses (siege camp) while watching
  tick times -> no tick over 100 ms caused by the search (compare with `nonBlockingLoads` false). *2026-10-14 scratch phase 5 (structure_tuning 8.0.5, seed 8675309, 1.0.6 jar of 12:44, density 1, worlds st_l1/st_l2/st_l3; log testing/structures/05_phase_5.md): FAILED: with `nonBlockingLoads` true the server thread still stalls for every load (pillager camp from far origins: max tick gap 16.0 s and 13.9 s, 9.9 and 16.1 TPS), no better than false (7.9 s, 15.2 TPS). jstack: LocateLoads.java:82 calls `getChunkFuture` inside a server task, and on the server thread that call waits in managedBlock until the chunk is generated. Fix not made (older code), see 05_phase_5.md.*
- [x] **LO-06 four threads [scratch]:** `asyncLocatorThreads = 4`, batch all twice in parallel -> no exception,
  no hang, results identical to the single-thread run. *2026-10-14 scratch phase 5 (structure_tuning 8.0.5, seed 8675309, 1.0.6 jar of 12:44, density 1, worlds st_l1/st_l2/st_l3; log testing/structures/05_phase_5.md): PASSED (adapted): a second parallel batch is refused by design ("A batch is already running"), so one 4-thread `batch all` ran with 21 plain `/locate` on top: no exception, no hang, 21/21 plain and 23/23 batch rows at the known positions. 1-thread batches (philipsruins, minecraft) vs the 4-thread rows: 38/40 same position, the 2 = the meta layout difference above.*
- [x] **LO-07 miss log line [scratch]:** a search with at least 8 misses -> one `[locate]` INFO line with
  misses, loads, candidates, ms and thread; a search with fewer -> no line. *2026-10-13 scratch phase 2 (structure_tuning 8.0.2, world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED: e.g. `[locate] takesapillage:pillager_camp from (-368, 396): hit at (4464, -5040) after 178 misses, 179 loads, 419 candidates, 269035 ms [asynclocator-1-thread-2]`; searches with fewer misses (witch village 5, jungle temple, monument) logged nothing.*
- [x] **LO-08 stats [scratch]:** `/celocate stats` -> totals match the sum of the batch rows. *2026-10-13 scratch phase 2 (structure_tuning 8.0.2, world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): `/locate` part PASSED: `locate_stats.json` and `/celocate stats` count every search since boot (35 in boot 1, 7 in boot 4), each row's misses equal its `[locate]` line. The batch-row comparison waits for LO-02 (Phase 5).* *2026-10-14 scratch phase 5 (structure_tuning 8.0.5, seed 8675309, 1.0.6 jar of 12:44, density 1, worlds st_l1/st_l2/st_l3; log testing/structures/05_phase_5.md): batch part PASSED: after two batches on a fresh boot, `/celocate stats` has 43 rows = the 43 batch rows, and misses, loads, candidates and every savedByStage value are equal row by row.*
- [ ] **LO-09 maps and trades [game]:** a cartographer explorer map and an LC map trade still point at real
  structures with folding on.
- [x] **LO-10 deep-terrain measurement [scratch]:** JFR recording of a Chunky pregen (radius 64) -> per-status
  chunk generation times written into 3.7. *2026-10-19 scratch phase 10 (patcherMod 1.0.7 jar = 1.0.8 worldgen, instance toml, Chunky r 64 chunks at 2144 -2592, 16,641 chunks in 24.5 min; log testing/structures/10_phase_10.md): PASSED: per-status times written into 3.7 (noise 38.8%, features 24.4%, light 18.8%, structure starts 6.4%).*
- [ ] **LO-11 DH clip [game]:** fly over a fresh area with DH generating -> distant land looks unchanged from
  above (no holes, no walls at cave openings above y 48); with DH `transparency = "COMPLETE"` (test only), deep
  oceans show a rolling floor around y 8 (4 to 12) and other oceans one just under 48 (44 to 47), or their real
  floor where it is higher; a DH pregen JFR recording shows lower DH worldgen CPU
  than before the clip.
- [x] **LO-12 real chunks unaffected [scratch]:** with the clip on, normally generated chunks have ores, caves and
  deepslate below y 48 as before (sample a few columns). *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): PASSED: 37778 full chunks, sections y -64..47: ores in 97%, deepslate 73%, air (caves) 76%, fluids 28%; the 91 all-air sections are scattered big caves.*
- [ ] **LO-13 config switches [scratch]:** `nonBlockingLoads` false -> searches still work (vanilla loads, ticks stall
  again); `logMissesAtLeast` 0 -> one `[locate]` line per search; `logMissesAtLeast` -1 -> none. *2026-10-13 scratch phase 2 (structure_tuning 8.0.2, world st_a, seed 8675309, 1.0.6 build jar, 4 locator threads): PASSED, all live (toml edited while running): at 0 a 0-miss search logs its line; at -1 searches with 33 and 49 misses log none; at 8 a 37-miss search logs. `nonBlockingLoads = false`: searches still answer, the server fell up to 7.9 s behind. Edit the toml in one write: a truncate-then-write raced Forge's config watcher, which re-saved the file with the jar's defaults.*
- [ ] **LO-14 census miss reasons [scratch]:** in the LO-03 census, misses carry reasons (`earth_fill`,
  `priority`, `surface_check`, `slot_taken` with `takenBy`, `no_start`...), and `sets[].shareOfSlots` lists each
  group member. *2026-10-14 scratch phase 6 (structure_tuning 8.0.6, seed 8675309, 1.0.6 jar of 12:44, density 1, world st_p: Chunky square radius 96 chunks at 2144 -2592, census at -1700 1500; log testing/structures/06_phase_6.md): FAILED (same census bug): 1 miss (no_start) in the report while the log shows 79 placement drops and 48 earth-fill cancels in the square during the census; `sets[].shareOfSlots` is written for every set.*
- [x] **LO-15 batch timeout and stop [scratch]:** `batchTimeoutSeconds` 5, `/celocate batch all` -> slow searches
  reported `timed_out`; a second run stopped with `/celocate stop` still writes its report. *2026-10-14 scratch phase 5 (structure_tuning 8.0.5, seed 8675309, 1.0.6 jar of 12:44, density 1, worlds st_l1/st_l2/st_l3; log testing/structures/05_phase_5.md): PASSED: at 5 s, 21 of 51 towns_and_towers rows `timed_out` (a load in flight still finishes: worst 31.5 s); `/celocate stop` during `batch all` wrote the 258-row report in the same second. Label bug: the 245 rows never started say `error: java.util.concurrent.CancellationException` instead of `stopped`.*
- [ ] **LO-17 `/celocate stop` label [scratch]:** `/celocate batch all`, then `/celocate stop` while rows are
  still queued -> the queued rows say `stopped`, none says `error: java.util.concurrent.CancellationException`
  (05_phase_5.md 3.4; fixed 2026-10-18, DEPLOYED with 1.0.7). Rows that were running when stopped say `stopped` too.
- [x] **LO-16 boot and mixins [scratch]:** server boots with the 1.0.6 jar, no mixin apply errors for
  `StructureCheckThreadSafeMixin`, `ChunkGeneratorMetaLocateMixin`, `NoiseBasedChunkGeneratorDistantClipMixin`,
  `NoiseChunkDistantClipMixin`, `ConfiguredFeatureDistantClipMixin`; a plain `/locate` works. *2026-10-13 scratch phase 1 (seed 8675309, 1.0.6 build jar): PASSED: no mixin apply errors at all; plain /locate minecraft:village_plains answered (-1792, 2784), 10 misses, 38.8 s.*

- [ ] **LO-22 DH clip Overworld only [game]:** fly over the End's outer islands and the Nether with DH generating
  (delete the client's DH data for those dimensions first) -> no stone floor at y 47 in LODs, the Nether lava sea is
  drawn; the Overworld clip still works (LO-11). DH generation needs a client, so the scratch half (SQ-WG30-A04) only
  checks the boot and that real Nether chunks keep their lava sea and ores. Scratch half PASSED 2026-10-30 (no mixin
  or `DistantClip` error, Nether lava 31..24, End chunks with no y 47 floor); the client half is still open.

## 8. History

- 2026-10-12: Written. Investigation from the decompiled 1.19.2 classes and Async Locator 1.3.0: the miss
  definition, the vanilla prediction's gaps (biome, set frequency, exclusion zone), blocking miss loads under Async
  Locator, the StructureCheck race, the per-rule fold analysis, the miss-log, batch and census designs, and
  the deep-terrain options. Nothing built.
- 2026-10-12: Decisions 1-5 (section 4): stages A + B approved, C-E on hold; 3.2-3.6 to build; DH generation
  clipped below y 48; pregeneration on the final server world.
- 2026-10-12: Ocean floors measured (WorldG, Workspace; `tools/seabed_heights.py`): deep oceans y 0 to 12, min -25.
  Clip variants added; per-column clip recommended, awaiting decision.
- 2026-10-12: Fake-seabed variants added (water continued below the y 48 cut in ocean columns, floor 24-28 /
  deep 0, or at the predicted floor); awaiting decision.
- 2026-10-12: Decided: fake seabed, deep oceans water to y 0, other oceans floor at the y 48 cut, no
  predicted-surface lookup (decision 4).
- 2026-10-12: Revised: floors hover (smooth seeded noise, +-4) around y 48 for oceans and y 8 for deep oceans.
- 2026-10-12: BUILT (patcherMod 1.0.6 in build/libs, not in mods/): stages A + B, non-blocking loads, StructureCheck
  thread safety, miss counters and log line, `/celocate batch | census | stats | status | stop` on Async Locator's
  pool, census miss reasons from the generation reject points, the Distant Horizons clip with the fake seabed.
  CEConfig `[worldgen.locate]` and `[worldgen.distant]`. Private vanilla members are read by reflection
  (`LocateReflect`): SRG-named @Accessor/@Invoker fail the mixin annotation processor. Offline: LocateTest 20/20,
  steps 1-7 unchanged. Tests LO-13 to LO-16 added.
- 2026-10-14: ships INCOMPLETE in pack 1.0.6 with the rest of the structure tuning (patcherMod 1.0.6, update from 1.0.4).
  The 7.3.2 locate changes (dimension check, `meta_target`, `searchTimeoutSeconds` 120, `/celocate stop`) are in the same
  jar. LO- tests still pending.
- 2026-10-18: Three locate bugs from the scratch run fixed in source for patcherMod 1.0.7 (not built, not shipped):
  the non-blocking loads ran `getChunkFuture` on the server thread and still stalled every tick (3.2, LO-05);
  the census predicted only the first set at each chunk and counted the rest "known" (3.6, LO-03, LO-14); a
  `/celocate stop` row still queued said `error: ...CancellationException` instead of `stopped` (LO-17 added).
  Offline: the worldgen package compiles against the mapped 1.19.2 Forge jar, LocateTest 28/28; the loads and the
  census only run in game, so LO-03, LO-05, LO-14, LO-17 are the checks.
- 2026-10-19: the three fixes above went out in pack 1.0.7 (DEPLOYED 2026-10-18 19:43, [plan_1.0.7.md](plan_1.0.7.md) SF).
  Rerun LO-03, LO-05, LO-14 and LO-17 on 1.0.7 or later.
- 2026-10-30: DH clip gated to the Overworld (`DistantClip.clipsDimension`, used by `mark` and `skipFeature`; TODO
  `locate #4`, found in [briefs/worldgen_locate.md](briefs/worldgen_locate.md)). A dimension check, not the brief's
  `clipDimensions` config list. patcherMod compiles; test LO-22 (CQ-WG30-A04 in game; boot check SQ-WG30-A04). SQ-WG30-A04 PASSED 2026-10-30; CQ-WG30-A04 not run.
