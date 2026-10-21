# Brief: worldgen / locate

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `locate`. Feature writeup:
[locate.md](../locate.md) (prefix `LO-`). Code: `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`
(paths below are relative to it unless they start with a repo folder).

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| locate #1 | Testing | reruns of LO-03, LO-05, LO-13, LO-14, LO-17 on 1.0.7 or later (listed at the end) | **first**: its census rerun is the data locate #2 decides on |
| locate #2 | Planned (on hold) | fold stages C to E into the prediction | **second**, only if the rerun census says it pays (see the gate in #2) |
| locate #3 | Planned | pregenerate the final server world (Chunky + Distant Horizons) | separate; a deploy step on the day the final world is created. Ties in with `pack/server #1` |

| (new) DH clip in every dimension | proposed Bug | limit `DistantClip` to the Overworld | **before #3's Nether/End pregen**; a few lines, can go in any patcherMod build |

#2 and #3 don't touch each other. #2 is the main code. The new bug is a small fix. #3 is commands and one runbook
section.

---

## locate #2: Fold stages C to E

Status Planned (on hold, decision 1 in locate.md 4) · Work patcherMod · TODO `worldgen / locate #2`

### Goal

Cut the misses that stages A and B leave. A miss is a candidate the prediction calls "maybe", so the search generates
that chunk's structure starts, and no start of the wanted structure is there. The fold must stay **exact**: a "no"
only where generation would certainly say no (locate.md 1, 4).

### Now

- **The prediction.** `StructureCheck#canCreateStructure` (`m_226755_`) is hooked twice:
  - HEAD, `mixin/StructureCheckFrequencyMixin.java:22-26` -> `worldgen/LocatePrediction.rejectsAtHead` (`:45-56`):
    stage A (`placementRejects`, `:59-72`) and the per-member frequency roll.
  - the `findGenerationPoint` call, `mixin/StructureCheckMetaMixin.java:21-27` -> `MetaSlots.predictGenerationPoint`
    (`worldgen/MetaSlots.java:322`) -> `LocatePrediction.plain` (`:79-88`): stage B (`fits`, `:114-133`) and the
    biome factor.
- **The counters.** Stage enum `worldgen/LocateStats.java:44`: `PLACEMENT, BIOME, POINT, FREQUENCY, META, META_TARGET`.
  Miss reasons, `:47`: `BUILT, NO_START, SURFACE_CHECK, EARTH_FILL, PRIORITY, FREQUENCY, VETO, META_ONLY, CONVERTED`.
- **The census.** `worldgen/LocateCensus.java` predicts through `level.structureManager().checkStructurePresence`
  (`predict`, `:196-216`), so any new stage is measured by the census without changing it. It scores misses by
  reason (`score`, `:247-254`; `slot_taken` + `takenBy` when another member won, which is stage C's target).
- **A full replay already exists.** `worldgen/PlacementPriority.predictSet` (`:331-367`) replays one set at one chunk on
  a scratch `ProtoChunk`: the weighted pick (`createStructures`' seeding, `WorldgenRandom.setLargeFeatureSeed`), each
  member through the real `tryGenerateStructure`
  (`mixin/ChunkGeneratorPlacementPriorityMixin.java:47-50`, `Generator.creativeengineering$tryGenerate`). Every pack
  mixin runs in it: frequency, veto, meta conversion, biome rules, surface check, earth fill and priority. The result
  is cached in `CACHE` (`:316`, 16,384 entries, key `(generator, set, x, z)`), and it runs inside `isolated()`
  (`:287-305`), which saves and restores the generation thread-locals and adds 1 to `DEPTH`.
  So C, D and E don't need three new stages. **One replay stage covers all three exactly.** This is the main finding of
  this brief: the design table in locate.md 3.1 predates `predictSet`.
- **What the folds saved so far** (LO-02, 1.0.6 jar, [05_phase_5.md](../testing/structures/05_phase_5.md)): misses
  2,003 with A+B against 10,660 without them; A said "no" 40,670 times and B 156,421 times. **Nothing measures what is
  left:** the 1.0.6 census reasons were broken (LO-03, LO-14). The fix shipped in 1.0.7, and the census hasn't been rerun.
- Config: `CEConfig.LocateCfg` (`CEConfig.java:2180-2236`), accessors `worldgen/WorldgenConfig.java:57-59`.

### Gate (decide before building)

1. Rerun the census on 1.0.9 (locate #1: LO-03 + LO-14). On a scratch server: `/celocate census 96` on a fresh world,
   seed 8675309, centre 2144 -2592 as in Phase 6. Read `kubejs/exported/worldgen/locate_census.json`:
   `missReasons` per structure.
2. Build only if `slot_taken + surface_check + earth_fill + priority` is a large part of all misses, say over half,
   **or** if the rare metas from `structures/rates-and-groups #6` (fortified metas, bastille) still time out at 120 s.
   Their misses are R7 peaceful-radius drops and slot losses, which only a replay can see.
3. Otherwise close the row: A+B are enough.

### Build (if the gate passes)

1. **Config.** Add `foldReplay` (default **false**) to `LocateCfg` after `foldBiomes`. Comment: "Stage C-E: replay the
   structure's set at the candidate chunk (weighted pick, surface check, earth fill, priority) before loading it.
   Exact. Costs one structure-start generation per surviving candidate." Add the accessor
   `WorldgenConfig.locateFoldReplay()` next to `:58`.
2. **Stage.** Add `REPLAY` to `LocateStats.Stage` (`:44`). `LocateCommand` (`:308`) and `LocateCensus` (`:212, 257, 296`)
   loop over `Stage.values()`, so they pick it up. Also add a `LocateTest` check
   (`tools/worldgen_test/.../LocateTest.java`, step 8 of `tools/test_worldgen_tools.sh`).
3. **Hook point: RETURN of `canCreateStructure`, only when it answers true.** A new `@Inject(at = RETURN,
   cancellable = true)` on `m_226755_`. Put it in `StructureCheckFrequencyMixin`, or in a new
   `StructureCheckReplayMixin` added to `creativeengineering.mixins.json` "mixins". It runs after A and B. Its "no" goes
   into `featureChecks` like vanilla's (locate.md 3.1, "interaction with the search").
4. **Context.** `predictSet` needs a `Ctx` (`PlacementPriority.java:327-329`): generator, StructureManager, registry
   access, RandomState, StructureTemplateManager, seed, a height accessor, Active.
   - Generator, RandomState, seed and registry access: `LocateReflect` already has them (`:28-41`).
   - Add getters for StructureCheck's template manager and `LevelHeightAccessor` fields. Look up their SRG names in the
     mapped 1.19.2 jar; the existing getters read SRG-or-mojmap names by reflection, see `LocateReflect.getter`.
   - **StructureManager.** StructureCheck has none. Set a ThreadLocal `ServerLevel` in
     `ChunkGeneratorMetaLocateMixin.creativeengineering$searchMetaParts` (HEAD of `findNearestMapStructure`, which has
     the level), next to `LocateStats.begin` (`:52`), and clear it at RETURN. Do the same around `LocateCensus.chunk`
     (`:183`). The stage then uses `level.structureManager()`. With no level set, the stage is off (vanilla callers
     other than search and census).
   - Change `Ctx.heights` from `ChunkAccess` to `LevelHeightAccessor`. `scratch()` (`:369-371`) only passes it to the
     `ProtoChunk` constructor, which takes a `LevelHeightAccessor`.
5. **New entry point** `PlacementPriority.replayFor(Ctx, Structure wanted, ChunkPos)`. It returns `true` = "certainly no
   start of `wanted` here":
   - For each set the wanted structure can come from (its own placements plus its anchors' sets, the same list
     `LocatePrediction.placementRejects` builds, `:63-66`) whose placement `isStructureChunk` at the chunk: replay the
     set. If any replay's `Predicted.structure() == wanted`, return false.
   - **Run the replay at generation depth, not prediction depth.** `predictSet` runs inside `isolated()`, which adds 1 to
     `DEPTH`, and `filter` skips its neighbour checks once `DEPTH >= MAX_DEPTH` (3, `:92, :500`). A replay at depth 1
     sees one level less than real generation (depth 0), so its answer can differ. Use a variant that saves the
     thread-locals like `isolated()` but leaves `DEPTH` alone. **Don't read or write `CACHE` under the set's key**:
     entries there were computed at depth 1 or more. Cache the replay's own answer in the vanilla `featureChecks` only.
   - **Exclusions** (answer "maybe", no replay):
     - (a) structures that can be companions: `Active.companions` specs. Companions start in chunks of their own at the
       RETURN of `createStructures` (`ChunkGeneratorPlacementPriorityMixin.java:68-76`), not through their set's pick,
       so a set replay can't see them.
     - (b) structures that can be meta parts or `meta_target`s, while `MetaSlots.locating()` is set (they are found
       inside meta starts, `MetaSlots.locateAt`).
     - (c) any replay that throws (log once per structure at DEBUG).
   - Count `LocateStats.predictedNo(Stage.REPLAY)` on a "no".
6. **Census.** Nothing to change in `LocateCensus`. Its `predict` goes through `checkStructurePresence`, so REPLAY
   "no"s show up in `no` and any wrong one in `falseNegatives`.
7. Offline: `tools/test_worldgen_tools.sh` (LocateTest + steps 1-7). In game: LO-18 to LO-20 below.

Sketch of the hook (shape only; check names against the code):

```java
@Inject(method = "m_226755_(...)Z", at = @At("RETURN"), cancellable = true, require = 1, remap = false)
private void creativeengineering$replay(ChunkPos pos, Structure s, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValueZ() && LocatePrediction.replayRejects((StructureCheck) (Object) this, s, pos)) cir.setReturnValue(false);
}
```

`LocatePrediction.replayRejects` checks `foldReplay` and the ThreadLocal level, builds the `Ctx` and calls
`PlacementPriority.replayFor`.

### Ids and data

No items or tags. Structure lists come from the live registries. Mixin target: vanilla `StructureCheck#m_226755_`
(`(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Z`), the same
descriptor the two existing mixins use (verified in source). The StructureCheck field SRG names for the template
manager and height accessor are **unverified**: read them from the mapped 1.19.2 Forge jar the worldgen build compiles
against.

### Decisions

- **Build at all?** The gate above. Recommendation: rerun the census first. Build only if the gate passes. The cost
  is about 150 lines of Java plus one config key.
- **One replay stage, or C/D separately?** Recommendation: one replay stage. The conservative stage C in locate.md 3.1
  ("no" only when an earlier member surely builds) cuts less and is a second copy of pick logic that `predictSet`
  already has.
- **Default on or off?** Recommendation: off in the shipped toml until a census shows `falseNegatives` 0 with it on and
  a batch shows fewer misses for the same wall time. Then on.
- **Where to cache?** Recommendation: only in `featureChecks`. That is per server run, so a config change can't leave a
  wrong answer behind.

### Gotchas

- **Exactness is bounded by generation's own determinism.** Neighbour predictions in real generation read `CACHE`
  entries computed at whatever depth came first. Meta layouts can differ between JVM runs (LO-02: village_desert and
  village_savanna a few blocks apart). Within one run the replay and the real chunk see the same state, unless the
  cache evicts in between. The census false-negative column is the only proof. Run it twice, with different origins.
- **Cost moves onto the locator threads.** A replay runs jigsaw assembly, earth-fill terrain sampling (62 % of the
  pack's worldgen CPU in LO-10) and neighbour predictions. That is cheaper than a chunk load: no IO, no ticket, no
  other sets, no proto-chunk saved. Every miss load today also writes a STRUCTURE_STARTS proto-chunk to the region
  files, and the replay doesn't. Run it only after A and B said "maybe".
- **Thread safety.** `PlacementPriority` state is ThreadLocal or synchronized (`CACHE`, `SETS`). EarthFill and MetaSlots
  must be checked the same way before the stage runs on 4 locator threads. LO-06 ran 4 threads on the existing
  predictions only.
- **Search deadline.** A replay can take tens of ms. Check `LocateStats.expired()` before starting one, so a stopped
  search still ends quickly (structure_tuning.md 7.3.2).

### Tests

- **LO-18 replay census [scratch]:** `foldReplay` true, `/celocate census 96` on a fresh world -> `falseNegatives` 0
  for every structure; `no.replay` non-zero; `missReasons` `slot_taken`, `earth_fill`, `priority`, `surface_check`
  lower than in the same census with it off.
- **LO-19 replay batch [scratch]:** `/celocate batch all` with `foldReplay` off, then on, same seed and origin -> same
  positions for every structure found in both, fewer misses, `savedByStage.replay` > 0, total wall time not higher.
- **LO-20 rare metas [scratch]:** `foldReplay` on, `/locate structure takesapillage:bastille` and the fortified metas
  (`structures/rates-and-groups #6`) -> answered within `searchTimeoutSeconds` 120.

---

## locate #3: Distant Horizons pregen on the final server world

Status Planned · Work none (deploy step) · TODO `worldgen / locate #3`

### Goal

When the final server world is created, generate it ahead of play so the cost isn't paid while people are online.
Decision 5 and option B in locate.md 3.7.

### Now

- **Two different pregens are meant.** Option B in locate.md 3.7 names **Chunky**: real chunks, written to region files.
  The TODO row and the clip need **Distant Horizons'** own pregen: LOD data only, generated by DH's world generator,
  where the pack's clip applies (`worldgen/DistantClip.java`, marks a chunk when the region is
  `DhLitWorldGenRegion`, `:47-71`). Do both.
- **DH has a pregen command (verified in `DistantHorizons-3.0.2-b-1.19.2-fabric-forge.jar`, decompiled
  `common/commands/PregenCommand_forge`, `CommandInitializer_forge`, `core/generation/PregenManager`):**
  - `/dh pregen start <dimension> <x z> <chunkRadius>`: radius at least 32 chunks; origin is a block column.
  - `/dh pregen status`, `/dh pregen stop`.
  - Op level 4 (the console has it). Progress goes to the server console every
    `generationProgressDisplayIntervalInSeconds` (2 s).
  - It spirals outward, keeping at most `numberOfThreads` sections in flight.
  - It fills DH's full-data provider through the same generator queue as normal distant generation. So the clip
    applies, assuming the queue is the same; this is inferred from `GeneratedFullDataSourceProvider`, not traced to
    `DhLitWorldGenRegion`.
- **Chunky 1.3.38 is installed** (`pack/MODLIST.md:72`). `config/chunky/config.json` has `continueOnRestart` false.
  `config/chunky/tasks/minecraft/overworld.properties` and `the_end.properties` are leftover cancelled tasks from a dev
  run (centre 1000 -800, radius 500), so they do nothing.
- **DH config** (`config/DistantHorizons.toml`, shipped to both sides):
  - `distantGeneratorMode = "FEATURES"` (`:234`), `enableDistantGeneration = true` (`:271`),
    `enableServerGeneration = true` (`:32`), `maxGenerationRequestDistance = 4096` (`:27`).
  - `lodChunkRenderDistanceRadius = 192` (`:661`) and `numberOfThreads = 3` (`:120`).
  - `configs_edited/root_config/DistantHorizons.toml` differs: `numberOfThreads` 2, `realTimeUpdateDistanceRadiusInChunks` 256.
    `pack/server #1` reports "2 threads" on the live server, so the live toml may be the edited copy. Check it on the
    server before the pregen.
- **Measured speeds:**
  - Chunky on the scratch server: about 11 chunks/s at density 1, 16,641 chunks in 24.5 min (LO-10, locate.md 3.7).
  - DH's LOD builders were 28 to 35 % of all CPU during that pregen (perf.md; locate.md 3.7).
  - DH pregen speed with the clip: not measured.
- The deploy runbook (`deploy/RUNBOOK.md`, copied into every payload) has no pregen step.
- **"Final server world"** isn't defined anywhere. Biome tuning (1.0.9) and structure tuning only apply to new worlds,
  so it is the world created after those settle.

### Build (deploy steps; no code)

1. **Add a section to `deploy/RUNBOOK.md`:** "7. New world: pregenerate", with the steps below. It is part of the
   payload, so write it on the dev machine.
2. **Before the first join**, on the new world with nobody online:
   - Back up `serverconfig/`.
   - Temporarily set `max-tick-time=-1` in `server.properties`. Long chunk jobs have hit the watchdog (`pack/server #1`).
3. **Chunky (real chunks, spawn area).** Server console:

   ```
   chunky world minecraft:overworld
   chunky spawn
   chunky shape square
   chunky radius 2000
   chunky start
   ```

   Radius 2000 blocks is about 63,000 chunks, about 1.6 h at 11 chunks/s. Disk: measure it from the scratch saves (see
   Decisions). Set `continueOnRestart` true in `config/chunky/config.json` for the run, so a restart resumes it.
   Delete the two stale task files first.
4. **DH pregen (LODs, wider).** After Chunky, so DH imports real chunks near spawn instead of generating them:

   ```
   dh pregen start minecraft:overworld <spawnX> <spawnZ> 192
   ```

   192 chunks matches `lodChunkRenderDistanceRadius`. Use 256 if the live toml's realtime radius is 256. Follow
   `dh pregen status`. Pregenerate the Nether and End (smaller radius) **only after the clip is limited to the
   Overworld** (see "Found: the DH clip runs in every dimension" below). Otherwise their LODs are generated with the
   stone band and stay that way in DH's database.
5. Put `max-tick-time` back, restart, and run the RUNBOOK 3 checks.
6. **Measure once:** record the DH pregen wall time and `du -sh world/data/DistantHorizons*` (or the DH folder DH
   creates) in locate.md 3.7.

### Ids and data

Commands verified as above: DH's from the jar. Chunky's commands are its documented ones, **unverified against the
jar**; `chunky help` in the console lists them. Config files: `config/DistantHorizons.toml`,
`config/chunky/config.json`, `config/chunky/tasks/`.

### Decisions

- **Chunky radius.** Recommendation: 2000 blocks, or the planned border if the pack sets one. Real chunks cost disk and
  carry the full worldgen, structures included. DH covers the distance.
- **DH radius.** Recommendation: `lodChunkRenderDistanceRadius` (192) around spawn. Players far from spawn still make
  DH generate on demand. That is the load `pack/server #1` worries about, so a later pregen around the main bases may
  be worth it.
- **Keep DH server generation on during play?** That belongs to `pack/server #1`. The pregen makes "off" cheaper, since
  the area is already done.
- **Pregen before or after the clip fix below?** Recommendation: Overworld any time; Nether and End only after it.

### Gotchas

- With `FEATURES`, DH's pregen writes **no** region files: only LOD data. The Chunky step is what makes real chunks.
- DH pregen is LOD only, so its LODs miss structures (`FEATURES` = "everything except structures"). Areas Chunky made
  get structures in their LODs.
- The pregen fills the 6 GB heap. Do it with nobody online, and restart after it.
- `generationRequestRateLimit` and the bandwidth limits only apply to client requests. Pregen isn't throttled by them.

### Tests

- **LO-21 DH pregen [server, deploy]:** on the new world, `dh pregen start minecraft:overworld <spawn> 192` ->
  completes ("Pregen is complete"). A client joining afterwards sees LODs out to its render distance at once, with no
  server distant-generation burst in the first minutes (perf capture, `perf/report.sh`).
---

## Found: the DH clip runs in every dimension (proposed Bug row)

Not a TODO row yet; it belongs with the DH clip (locate #1). Found while reading `worldgen/DistantClip.java` for #3.
Not seen in game.

- **Code.** `DistantClip.mark` (`:66-71`) marks any DH chunk whose build range contains the clip height. There is no
  dimension check. `fillBand` (`:93-136`) then fills every non-ocean column from the bottom up to the clip with stone,
  or deepslate below y 0. `skipFeature` (`:87-90`) has no dimension check either.
- **The Nether and the End both build from y 0** (noise min_y 0), so y 48 is inside their range and they get clipped.
- **Expected effect in DH LODs, not in real chunks:**
  - End: a solid stone floor at y 0-47 under the void, everywhere.
  - Nether: the lava sea (y 31) and everything under y 48 turn to stone.
  - The pack's own noise dimensions are affected the same way.
- **Fix.** In `mark`, return unless the chunk is in the Overworld. The DH region's `getLevel()` is the `ServerLevel`;
  compare `dimension()` with `Level.OVERWORLD`. A cheaper equivalent is `chunk.getMinBuildHeight() < 0`.
  Recommendation: a config list `[worldgen.distant] clipDimensions = ["minecraft:overworld"]`, read in `mark`, so a
  modded deep dimension can opt in. Also gate `skipFeature` on the chunk being marked: `CLIPPED.containsKey`, which
  needs the chunk; `ConfiguredFeatureDistantClipMixin` has the `WorldGenLevel`, from which the chunk is
  `getChunk(origin)`. A dimension check alone also works.
- **DH caches LODs.** Clients that already visited the End or Nether keep wrong LODs until DH's data for that dimension
  is deleted (client `Distant_Horizons` folder) or regenerated.
- **Test LO-22 [game]:** with the fix, fly over the End's outer islands and the Nether with DH generating -> no stone
  floor at y 47 in LODs; the Overworld clip still works (LO-11).

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| locate #1 Locate folding, non-blocking loads, census, DH clip | Testing | The TODO's Details are out of date (see "TODO corrections" in the final report): LO-07, LO-10 and LO-16 passed too. LO-13 passed on 1.0.6 but its `nonBlockingLoads` half has to be redone with LO-05, which is why its box stays open; locate.md 3.2 says "Retest: LO-05, LO-13". Still to run: LO-03, LO-05, LO-13, LO-14, LO-17 (scratch), LO-01 and LO-04 (partly), LO-09 and LO-11 (client game). |

## Writeup corrections made

- `feature_writeup/locate.md` 0 Knobs: added `searchTimeoutSeconds` (120), which existed only in 3.5.
- `feature_writeup/locate.md` 2.2 table, "Prediction today" column: R4 and R10 said "fits only" / "no" (written before
  stages A and B were built). Now: R4 stage B, R10 stage A, both exact.
- `feature_writeup/locate.md` 3.7 option A row: "per-column clip recommended" replaced by the decided fake seabed
  (decision 4).
- `feature_writeup/locate.md` 5 File map: added the files built on 2026-10-12, which the map didn't list.
