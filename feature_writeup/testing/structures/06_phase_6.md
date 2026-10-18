# M6: Phase 6, a pregenerated area (8.0.6)

2026-10-14. Live notes: [06_phase_6.scratch.md](06_phase_6.scratch.md). Result lines are inline in
[structure_tuning.md](../../structure_tuning.md) section 8 and [locate.md](../../locate.md) section 7, with a
summary above "8.0.7 Phase 7".

## 1. Setup

- Same scratch server as M5 (patcherMod `build/libs` 1.0.6 of 12:44, seed 8675309, density 1, baseline toml with
  the three log switches). A new world, `st_p`, with 4 locator threads.
- **Area:** Chunky square, radius 96 chunks, centred on 2144 -2592 (92% land) instead of spawn (76% ocean). That's
  37,249 chunks in 64 min.
- **Census:** run on a separate fresh square (radius 96 at -1700 1500, 90% land), not over the pregenerated one. Its
  own docs say a chunk generated before the census only counts as "known".
- **Picking land:** a scratch-only `/zbmap` command read the land share from the noise biome source, without loading
  chunks.
- **Checks:** most were read offline from the region files (every structure start with its piece boxes, grid
  spacing per set, blocks under structures, section palettes below y 48) and from the pregeneration's log.
- **Out of scope:** your constraints (correctness only, no structure-to-structure rates) skip ST-72, 91, 98, 101 and
  ST-106's ratio.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-141 | PASSED | 15 rank drops, loser ranked below the winner in all 15 |
| ST-154 | PASSED | 24 peaceful drops, all hostile losers, radius = the peaceful structure's (3 or 5) |
| ST-142 | PASSED (scratch part) | At all 22 distinct drops the loser's start is gone and the winner's is there |
| ST-114 | PARTLY | No entrance dungeon (11 checked) has water above it; the "surface check dropped" count was not taken |
| ST-107 | PASSED | 6 witch-group starts, none closer than 9 chunks, no grid cell with two |
| ST-20 | PASSED | Settlements >= 13 chunks apart, ruins >= 9, surface dungeons >= 11; all 30 sets with starts clean |
| ST-74 | PASSED | 7 flotillas with 11-16 logs, every log over water 6+ deep, none on a beach |
| LO-12 | PASSED | Real chunks below y 48: ores in 97% of sections, deepslate 73%, caves 76% |
| LO-04 | PARTLY | Per-set shares written and valid; comparison with the estimates skipped (rate) |
| LO-03 | FAILED | Census bug (3.1): predictions only cover the first set at each chunk |
| LO-14 | FAILED | Same bug: 1 miss reported, while the log shows 127 generation rejects in the square |
| ST-106 | SKIPPED (ratio) | Rate; its "no slot holds both" part holds |
| ST-72, 91, 98, 101 | SKIPPED | Rates, out of scope |

Notes:
- **ST-74:** two flotillas sit in a `plains` biome. Generating those chunks showed every log floating on water at
  least 6 blocks deep: a plains-biome lake or coast, not a beach.
- **LO-12:** the 91 sections below y 48 that are entirely air are scattered over every Y level, 1 to 3 per chunk in
  neighbouring chunks. That's big caves, not the distant-terrain clip leaking into real chunks.
- **ST-20:** its text names 5 chunks for ruins; the ruins grid is 16/8 now, so the real minimum is 9.

## 3. Bugs

### 3.1 The census counts most slots as "known" (LO-03, LO-14; older locate code)

- **Cause:** `LocateCensus.enumerate` fills each region's list set by set, and `runRegion` handles one slot at a
  time: predict the members, then load the chunk to STRUCTURE_STARTS. Sets with a slot in every chunk
  (`buried_treasures`, `mineshafts`) come early, so the first set loads every chunk of the square. Every later set at
  that chunk then finds it loaded and records "known".
- **Proof:** a small census on untouched land (radius 4 at -9000 9000) gives real predictions only for
  buried_treasure (81 slots) and one ancient city; everything else is "known". The big census has 142,971 of 179,710
  member-slots "known", including every group slot.
- **What still holds:** "built" and the per-set shares come from the real starts, so they are fine.
- **What it breaks:** the predictions, misses, miss reasons and the false-negative check only cover the first set
  per chunk. The reported "0 false negatives" proves little beyond buried treasure.
- **Fix (not made):** group a region's slots by chunk; predict every member of every set that tries the chunk; then
  load once and score them all. Chunks generated before the census stay "known" as designed.
- **Fixed in source 2026-10-18** (patcherMod 1.0.7, not built or shipped): exactly that: per chunk, every set predicted, then one load, then all scored.
  [locate.md](../../locate.md) 3.6. Retest LO-03, LO-14.
- **Related:** the Phase 5 bug (locate loads block the server thread) also applies here. The census still ran at
  ~160 slots/s, because most loads are quick STRUCTURE_STARTS-only generations.

## 4. Values changed

- No toml values changed in this phase; the server toml was diffed against the baseline at the end.
- The world-gen area and both scratch scripts (`zz_scratch_tick.js`, `zz_scratch_bmap.js`) exist only in the scratch
  server's copy.

## 5. Follow-ups for you

1. **3.1 census:** fix it together with the Phase 5 locate-load fix (1.0.7). Then rerun LO-03 and LO-14 with a
   fresh census. The per-set shares (LO-04) are usable now if the weight-stripping Todo needs them; they are listed at the end of the
   scratchpad.
2. **ST-114:** the "surface check dropped" count can come from the fixed census (`surface_check` reasons).
