# M3: Phase 3, placing and probing (8.0.3)

2026-10-14. Live notes: [03_phase_3.scratch.md](03_phase_3.scratch.md). Result lines are inline in
[structure_tuning.md](../../structure_tuning.md) section 8, with a summary above "8.0.4 Phase 4".

## 1. Setup

- Same scratch server as M2: session 54725a7d scratchpad; patcherMod `build/libs/creativeengineering-1.0.6.jar` of
  2026-10-14 01:28; seed 8675309; 4 locator threads.
- World `st_d` (structure density 4, from M2). Two Chunky squares of radius 800 serve as test land:
  - around 0 0: 76% ocean, used for the geodes and driftwood;
  - around 2144 -2592: 92% land, used for the villages.
- Two volcanic-peaks patches were forceloaded for the ground-match tests.
- World `st_e` (same seed) with `oceanGeodes = false` and `templateBlockSwaps = false` in the scratch toml, for the
  two off-switch comparisons.
- Scratch probes (not in the repo):
  - KubeJS commands for block counts, block positions and block entities;
  - a biome-source map and a biome-source point lookup, both without chunk loads;
  - `zp gen`, which runs a structure's own start generation at given chunks and lists the pieces, without placing
    anything.
  - Offline: Python readers for structure starts, geodes, driftwood and top blocks in the region files.
- Scratch toml changes for this phase:
  - `[worldgen.ground_match] log = true`;
  - `mode`, changed for ST-30 and put back to TERRAIN;
  - the two `st_e` switches.

  All were on the server copy only; the instance config is untouched.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-14 | PASSED | `/place template`: 5 Lootr barrels for 1 basket + 4 cabinets with loot; 4 village chef/cook houses: Lootr barrels = template loot containers, no cabinet left |
| ST-47 | PASSED, [game] look left | 40 generated outposts: 34 vanilla with watchtower, 6 IPO each with tower + top; placed IPO complete to y 156 |
| ST-92 | PASSED | Ruin scatter placed in a forest: anchor + 2 forest ruins; 5 generated scatters with weighted anchors |
| ST-130 | PASSED | Placed way sign reads "200", yaw 176.8 = exactly toward a village_towers start 197 blocks off |
| ST-28 | PASSED | Volcanic peaks: grass share 0-34%, pads swapped for basalt/blackstone, no grass/dirt/flowers left |
| ST-30 | PASSED (after a retest) | PALETTE: full sampled pad; REMOVE: 64 of 64 base blocks removed. First tries hit the coverage bug (4.1) |
| ST-32 | PASSED | Plain template keeps its grass as saved, no ground-match line |
| ST-31 | PASSED | Exempt tag: no ground-match line, grass pad kept; entry removed again |
| ST-12 | PASSED, [game] walk left | 0 tech-house pieces in 42 saved + 146 generated villages; pet shops in 83 of 146, warehouses and chef houses present |
| ST-149 | PASSED | All 5 marked spots upright salmon logs; with the switch off, red glazed terracotta again |
| ST-133 | PARTLY | Geodes lowered as designed (amethyst exact per chunk, Geode Plus by distribution), but shells reaching deep sea floors barely change (4.3) |
| ST-48 | PASSED ("than before" not compared) | Free driftwood floats on ocean and lies rarely on beach sand |
| ST-50 | FAILED (shallows) | No floating driftwood in rivers, swamps or beach water (4.2) |

## 3. Notable numbers

- **Geodes:** per chunk, same seed, system on minus off. Ocean amethyst: -10 x10, -9 x10, -11 x5, -12 x4, -8 x4,
  -13 x2, -15, -7, -3, and 0 x4. Land: 0 in all 32. Geode Plus (distributions only): ocean geodes 9-17 blocks
  below land ones with the system on, 0-4 with it off.
- **Driftwood:** free logs in about 20k chunks: 14 floating on ocean, 4 on beach sand, 0 in 611 river and 454 swamp
  chunks. At density 4, the meta flotillas account for most driftwood blocks.
- **Villages:** pet shops in 57% of the generated vanilla villages.

## 4. Open issues (for you)

1. **Ground match gives up silently over deep structures (bug).** `GroundMatch.sample` skips every column covered
   in XZ by any structure piece in the 3x3 chunks, whatever the piece's height. An ancient city at y -51, Philip's
   underground ruins, or a mineshaft can hide nearly all columns. With fewer than 16 samples the palette is null, so
   the template's grass pad stays, and nothing is logged (the log line needs a palette). This hit 3 of 6 volcanic
   spots at density 4; at density 1 it is rarer but real, since mineshafts are common. Fix idea: skip a column only
   when a piece's box overlaps the sampled ground's height range, and log a line when matching is skipped for lack
   of samples.
   **Fixed in source 2026-10-18** (patcherMod 1.0.7, not built or shipped): that fix idea, plus the log line. [structure_tuning.md](../../structure_tuning.md) 3.23 item 3, ST-193.
2. **Beach, river and swamp driftwood water versions never place (bug, 2026-10-03 tuning).** Upgrade Aquatic
   5.1.0's `DriftwoodFeature`, outside `#is_ocean`, only places a log when the block directly under its origin is
   dirt (and water is near) or sand. Our overrides for beach/river/swamp/extra require water under the origin, so they
   can never succeed. Only open-ocean driftwood floats; elsewhere only the land versions (1/10 rate) remain. Fix
   options:
   - drop the water filter outside oceans and keep the 0.25x through rarity (UA then lays logs on banks next to
     water, its own design);
   - or filter for "water adjacent" instead of "water below".

   **Fixed 2026-10-18** (data, 1.0.7, not shipped): the second option, a bank next to water (4 sides, at the
   surface or one lower). [structure_tuning.md](../../structure_tuning.md) 3.23 item 4, ST-195.
3. **The geode drop does little over deep oceans (tuning).** This seed's deep ocean floors sit near y 0. Geodes,
   placed up to y 30, still reach the floor after a 10-block drop: 14 of 80 ocean geode chunks with the system on
   vs 16 of 71 off. Options: drop relative to the floor (keep the top N blocks under OCEAN_FLOOR_WG), or a bigger
   drop for deep oceans.
4. **Note:** Geode Plus geodes are not reproducible per seed across worlds. That isn't ours to fix; it only limits
   world-to-world comparisons.

## 5. Values changed and restored

- Scratch server toml only: ground match log, mode (back to TERRAIN), `oceanGeodes` and `templateBlockSwaps` (in
  `st_e`). The exempt entry in the scratch copy of `ground_match.js` was removed again (identical to the instance
  file). Structure density 4 is from M2.
- Nothing in the instance changed in M3 apart from these writeups and the result lines. No jar installed, nothing
  deployed.

## 6. Notes for later phases

- Ground-match tests need a spot without a big structure underneath. Check coverage first, or test at density 1.
- `zp gen` (start generation without placing) is a fast way to check pool and variant correctness.
- Forge hot-reloads the common toml; the new values apply to the next placement, no restart needed.
- Geode Plus can't be compared chunk by chunk between two worlds.
