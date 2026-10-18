# M5: Phase 5, batch searches (8.0.5)

2026-10-14. Live notes: [05_phase_5.scratch.md](05_phase_5.scratch.md). Result lines are inline in
[locate.md](../../locate.md) section 7, with a summary above "8.0.6 Phase 6" in
[structure_tuning.md](../../structure_tuning.md).

## 1. Setup

- Scratch server built from the instance (373 mods, patcherMod `build/libs` 1.0.6 of 12:44, the same jar as in
  `mods/`), seed 8675309, density 1. Toml = instance defaults plus three log switches (earth fill `ALL`, meta
  structures, placement priority). Every value changed for a test went back to that baseline afterwards; the toml
  was diffed against it at the end.
- Worlds: `st_l1` (folds on, 4 locator threads), `st_l2` (`foldPlacement` + `foldBiomes` off, 4 threads), `st_l3`
  (folds on, 1 locator thread).
- Two sessions. The first ran both full batches (L1 13:09, L2 13:39) and started the ground-truth searches. At 14:38
  its scratchpad was gone, which lost the batch JSONs and the running searches. The batch summaries survived in the
  scratch log. The second session rebuilt the server and redid the ground-truth step from there.
- Searches from spawn (-80 76), Overworld only. Rare structures got longer timeouts (`searchTimeoutSeconds` 900)
  only for the ground-truth checks.
- Tick timing for LO-05 came from a scratch-only KubeJS script in the server copy, which logs the gap between tick
  starts every 5 s. Thread dumps came from `jstack` on the scratch server's JVM.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| LO-02 | PASSED | Same positions with folds on and off for every structure compared (below); misses 2003 vs 10660; stages A/B saved 40670 / 156421 checks |
| LO-01 | PARTLY | 258-row report with misses; 42 rows time out at the 120 s default, so "no task times out" does not hold |
| LO-06 | PASSED (adapted) | A second parallel batch is refused by design. Instead: a 4-thread batch with 21 plain searches on top, no exception or hang, and the 1-thread rows match |
| LO-08 | PASSED | `/celocate stats` = batch rows, 43 of 43, every count equal |
| LO-15 | PASSED (label bug) | 5 s timeout: 21 of 51 rows `timed_out`. `/celocate stop` writes the full report at once; rows that never started say `error: CancellationException`, not `stopped` |
| LO-05 | FAILED | Non-blocking loads still stall the server thread, by seconds per load, no better than vanilla |

### LO-01 / LO-02: folds on vs off

- **Both batches:** 212 structures were found in both. 210 are at the same position; the other 2 are explained in
  section 3.3.
- **21 found only with folds on** (they timed out folds off): searched again with folds off and a 900 s timeout,
  all 21 gave the same position.
- **4 found only with folds off** (they timed out folds on): searched again with folds on, all 4 gave the same
  position.
- **Spot checks:** village_plains (-1792 2784, the same as Phase 1) and igloo, with folds confirmed off (stage
  savings 0).
- So the folds never skipped a real start that a plain search would have found.
- Timeouts are wall-clock and depend on load. With 4 searches running at once, 5 of the 21 above timed out at 120 s
  even with folds on, and a second run found them in 17 to 128 s.

### LO-06: threads

- 4-thread `batch all` (234/258 found, 2031 misses, 1547 s) with 21 plain `/locate` started on top of it: no
  exception and no hang.
- All 21 plain answers, and the 23 batch rows with a known position, were at the expected position.
- 1-thread batches of `philipsruins` (22/22) and `minecraft` (16/18) matched the 4-thread rows. The 2 that differ
  are the villages from section 3.3.
- Miss counts per row differ between the two runs. That's expected: searches running at the same time reuse each
  other's checked chunks.

### LO-05: tick times

Pillager camp searches from far, fresh origins, with 1 locator thread:

| Run | nonBlockingLoads | Loads | Window | TPS | Max tick gap | Gaps > 250 ms |
|---|---|---|---|---|---|---|
| A1 | true | 11 | 55 s | 9.9 | 16.0 s | 8 |
| A2 | true | 66 (timed out) | 124 s | 16.1 | 13.9 s | 39 |
| B | false | 15 | 51 s | 15.2 | 7.9 s | 12 |

Idle baseline: 20 TPS, max gap 51 to 69 ms.

## 3. Bugs and findings

### 3.1 Non-blocking locate loads block the server thread (LO-05, older 7.3 code)

- **Where:** `LocateLoads.offThread` runs `cache.getChunkFuture(x, z, STRUCTURE_STARTS, true)` inside a
  `supplyAsync(..., server)` task (LocateLoads.java:80-82). When `getChunkFuture` runs on the server thread, it waits
  in `managedBlock` until the chunk is generated. The thread dumps show the server thread there for the whole
  search.
- **Effect:** every chunk load made by a `/locate`, batch, census or map search stalls the tick for the entire
  generation. That can be several seconds for big jigsaw starts.
- **What it explains:** the earlier leads "9 s lag during a 10-miss locate" (Phase 2) and "31 s to reach `stop`"
  (Phase 2, 7.3 rerun).
- **Fix (not made):** add the ticket in the server task, then call `getChunkFuture` from the search thread (off the
  server thread it only schedules the work and returns its future). Alternatively, call `getChunkFutureMainThread`
  in the task. It's a few lines; candidate for 1.0.7.
- **Fixed in source 2026-10-18** (patcherMod 1.0.7, not built or shipped): the server task only adds the ticket; the search thread calls `getChunkFuture`
  itself and waits on the future. [locate.md](../../locate.md) 3.2. Retest LO-05.

### 3.2 Cloud Storage and Domestication Innovation village pieces dropped on reload (third-party)

- **Where:** both mods add houses to all five vanilla village house pools using the builtin `ProcessorLists`
  holder. The world's registry codec rejects that holder. At save time this logs "Element ... minecraft:empty is
  not valid in current registry set", and the piece is written as `{}`.
- **Effect:** if a structure_starts chunk is saved and loaded again before the village is placed, the reload drops
  those pieces ("Invalid pool element found"). In st_l2 that was 3 of 241 desert village pieces, 2 of 90 snowy, and
  6 of 976 in meta/village_towers. In normal play it happens to villages at the edge of generation (locate, maps,
  travel).
- **What it explains:** the "old noise" in client logs since 2026-04 is this.
- **Possible fix:** PoolTuner (or a mixin) rebinds each pool element's processor holder to the world's registry.
- **Fixed in source 2026-10-18** (patcherMod 1.0.7, not built or shipped): PoolTuner rebinds them at ServerAboutToStart. [structure_tuning.md](../../structure_tuning.md) 3.23 item 2,
  ST-192.

### 3.3 Village layout differs between JVM runs (older, recorded)

- **Symptom:** village_desert and village_savanna, as anchors of meta/village_towers, come out a few blocks
  different in each of 3 server runs, always in the same chunk. Within one run they are stable.
- **Not affected:** the other vanilla villages and 210 other structures.
- **Why it matters:** two-world comparisons (Phase 8) should expect this. Cause not found yet.
- **Cause found 2026-10-18:** the pools reach PoolTuner in an order that changes per JVM run (the 10 versions of
  `pool_tag_report.md` in git list the desert houses in 9 orders), and jigsaw picks by list index. Likely every jigsaw
  structure, not just these two (a plain village's locate answer is its chunk, which hides it). **Fixed in source 2026-10-18** (patcherMod 1.0.7, not built or shipped): 
  a canonical pool order, `[worldgen.pools] canonicalOrder`. [structure_tuning.md](../../structure_tuning.md) 3.23
  item 1, ST-191.

### 3.4 `/celocate stop` row label (cosmetic)

- Batch rows for searches that were still queued say `error: java.util.concurrent.CancellationException` instead
  of `stopped`. The report is complete otherwise.
- **Fixed in source 2026-10-18** (patcherMod 1.0.7, not built or shipped): a cancelled search's row says `stopped`. LO-17.

## 4. Values changed and restored

| Value | Changed to | Restored |
|---|---|---|
| `foldPlacement`, `foldBiomes` | false (L2, ground truth) | true |
| `searchTimeoutSeconds` | 900 (ground truth) | 120 |
| `batchTimeoutSeconds` | 5 (LO-15) | 120 |
| `nonBlockingLoads` | false (LO-05 run B) | true |

All of these were in the scratch server's toml only; the instance toml was not touched. The scratch tick script
exists only in the server copy.

## 5. Follow-ups for you

1. **3.1**: fix the locate loads for 1.0.7 (Phase 6's census loads chunks the same way, so it stalls the server too until
   this is fixed).
2. **3.2**: fix the two mods' village pieces in PoolTuner, or leave it.
3. **3.4**: one-line label fix, with 3.1.
