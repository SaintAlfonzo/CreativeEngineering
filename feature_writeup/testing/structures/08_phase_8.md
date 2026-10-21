# M8: Phase 8, two-world comparisons (8.0.8)

2026-10-19. Live notes: [08_phase_8.scratch.md](08_phase_8.scratch.md). Result lines are inline in
[structure_tuning.md](../../structure_tuning.md) section 8, with a summary under "8.0.8 Phase 8".

## 1. Setup

- **Server:** the M7 scratch server (patcherMod 1.0.7 jar, whose worldgen is byte-identical to 1.0.8), seed 8675309,
  `structureDensity` 4, placement-priority and meta logs on, 4 locator threads.
- **As shipped:** M7's scratch-only `type/village` patch was reverted, so pruning is dead here, as in the release.
- **Switches:** every switch was set in the scratch copy only and put back afterwards. The instance's files were
  not touched.
- **Areas:**

  | Area | Square | Contents |
  |---|---|---|
  | S1 | radius 400 at -1400 -2592 | The inner M7 square: villages and peaceful drops |
  | S2 | radius 256 at -3727 -3564 | Two towns (`meta/town_villages`) with 4 companion villages |
  | S3 | radius 400 at 3450 -1100 | Four or five towns with 5 to 7 companions |

- **Worlds:** seven, each fresh and with the same seed:

  | World | Change from the baseline | Areas |
  |---|---|---|
  | `st_8a` | none (baseline); a west strip first | S1, S2, S3 |
  | `st_8b` | none; an east strip first | S1 |
  | `st_8cd` | `priority.shape = []` and `priority.peaceful` removed | S1, S2, S3 |
  | `st_8e` | `[worldgen.systems] placementPriority = false` | S1, S3 |
  | `st_8f` | companion distance 64-96 (soft rule loosened), shape on | S3 |
  | `st_8g` | the same distance, shape off | S3 |

- **Method:** the starts were read from the region files, full chunks of each square only: id, chunk and every
  piece box (scratchpad `p8.py`, `towns.py`), plus the log's drop and companion lines.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-144 | PASSED | S1 approached from the west and from the east, in two JVM runs: 674 = 674 starts, same chunks, identical piece boxes |
| ST-157 (2nd half) | PASSED | Peaceful off: each of the 7 slots where `st_8a` logged peaceful drops holds a hostile start |
| ST-145 | PASSED (rate skipped) | Priority off: 2 of 7 settlements overlap a ruin. Priority on: 0 of 30 settlements across S1, S2, S3 and M7's main square |
| ST-148 | PASSED, with the companion distance loosened | Shape on: 3 companions run up to their town with 0 overlapping pieces. Shape off: those 3 slots log `dropped <village> ... overlaps meta/town_villages` |

Notes:
- **ST-157:** a drop line is logged for each candidate a slot tries, so one slot gives several lines. Counted per
  slot:
  - S1: a zombie dungeon and a pillager stronghold siege. Both had been dropped near a viking house (radius 3).
  - S2: an OGT outpost and a Tudor outpost near the town (radius 8), an OGT outpost near a house camp, the undead
    castle near a sawmill, and a zombie dungeon near a forge (radius 3 each).
  - With peaceful on, one slot near the town ended with a `eugene:fake_trader_camp`. That camp is hostile, but its
    small pieces stay more than 8 chunks from the town's pieces, while the bigger outposts tried in the same slot
    reach within 8. The radius is measured piece to piece, so this is correct.
- **ST-145:** the overlaps with priority off were:
  - a savanna village with a field-stone ruin;
  - the town at [205, -69] with a ruin scatter.

  Priority off also drops every companion village: they belong to the priority system, so S3 has 4 settlements
  instead of 9. Spawn prep took 189 s against about 300 s with priority on.
- **ST-148, first try:** with the real companion distance (112-160 blocks), shaping never acted. In S2 and S3
  (8 towns, 9 companions, about 15 grid villages), every village was piece-identical with shaping on and off,
  and no village was dropped with it off: none reached a town.
- **ST-148, forced:** I pulled the companions in to 64-96 blocks (a soft spacing rule, allowed for rare cases).
  - Shape on: three companions end 0-1 blocks from their town with no overlapping piece: taiga [210, -70], OGT
    [201, -68], flower forest [231, -53].
  - Shape off: those three slots log the drops. Each slot then tries other villages; only [201, -68] gets one, a
    Swedish village 5 blocks clear. The other two stay empty.
  - The companions that never reached a town are identical in both worlds.

## 3. Bugs

None in Phase 8. Two side notes:
- **Beautify pool weights:** the dedicated server writes `original` weight 2 for the 5 `beautify:botanist_house_*`
  pool entries in `pool_config.json`. Your client's file of 10-18 says 1, and `current` is 1 in both. Beautify
  seems to inject them twice on a dedicated server. The effective weight is the same, so nothing to do unless
  `original` feeds a later calculation.
- **Phase 7's "no companions":** that wasn't a bug. Companions generate in their own chunks around a town, and
  M7's towns had only come from `/locate`, which builds just the town's chunk.

## 4. Values changed

All of these were in the scratch copy and have been put back:
- `priority.shape` and `priority.peaceful` (`st_8cd`, `st_8g`)
- `placementPriority` (`st_8e`)
- the `town_villages` companion distance (`st_8f`, `st_8g`)

## 5. Follow-ups for you

*Resolved 2026-10-19 ([11_fixes_1_0_8.md](11_fixes_1_0_8.md)): companion distance is now 64-96 blocks in `meta_structures.json` (user: keep them pulled closer).*


1. **Companion reach:** at the real distance (112-160) no companion reached its town in 8 towns, so shaping is
   rarely exercised. That is fine for correctness (it works when needed). It also means ST-147's "streets end at a
   town" is hard to see in game without the loosened distance.
2. **Next phase (9):** the flat-world `/ceplace` run (ST-39, 40, 41, 42). The open items from Phase 7 are still
   yours: the 1.0.8 server crash, the prune tag fix, pruning shrinking the size class, and `/dumpstructuregen`.
