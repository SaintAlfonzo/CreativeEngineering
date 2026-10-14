# M3 scratchpad: Phase 3 (8.0.3, placing and probing)

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End; ignore structure-to-structure rates; correctness only. Sleep an
  hour if the token limit gets close.
- Milestone: M3. Tests: ST-14, 47, 92, 130, 28, 30, 32, 31, 12, 149 (block part), 133 (geodes), 48, 50.
- Server: session 54725a7d scratchpad srv/ (same as M2 session 2; 373 mods; patcherMod build/libs 1.0.6 of 01:28).
- World st_d, seed 8675309, max-tick-time=-1, asyncLocatorThreads 4. Pregenerated: Chunky radius 800 around 0 0
  (76% ocean: geodes/driftwood) and around 2144 -2592 (92% land: villages).
- Scratch toml (srv copy only): earth_fill log ALL, meta log true, placement log true, structureDensity 4.0 (M2,
  soft rule; features unaffected). Phase 3 toml switches (ST-30 mode, ST-31 exempt) to be put back after each.
- M3 DONE 11:40 (writeup 03_phase_3.md, inline results). Running: nothing. Next: M4 (Phase 4).

## Log (append-only)

- 10:41 M3 started. Boot 5 on st_d, Done 10:43 (11 s). Load: `[template-swaps] 874 block swap(s) in 69 template(s)`
  (no "no longer hold"), `[loot-swaps] 12 block(s) are swapped ...`.
- ST-14: template chefsdelight:plains_chef_house has basket 1 + oak_cabinet 4 with LootTable chefsdelight:chests/cooker
  (+3 decorative crates, no loot). `/place template ... 2232 100 -2440`: lootr:lootr_barrel 5, cabinets/baskets 0, crates
  3 untouched; `data get block` on the basket and a cabinet spot = lootr:lootr_barrel with LootTable cooker (Lootr
  converted even the /place copy). Natural villages (starts_d pieces + forceload + zp blocks): village_towers
  [159,-127] plains_chef_house 5 lootr barrels, plains_cook_house 4; village_taiga [106,-182] taiga_cook_house 5;
  village_savanna [110,-144] savanna_cook_house 4 = the templates' loot-container counts exactly (4/5/4), 0 cabinets.

- New probe `/zp gen <structure> <cx> <cz> <n> <step>`: Structure.generate at n chunks along x, no placement, prints the
  piece list (needs /reload to register; reload is fine now that Chunky isn't needed).
- ST-47: `zp gen minecraft:pillager_outpost 130 -160 40 1` (40 starts along z -160): 34 vanilla (base_plate list element
  with watchtower / watchtower_overgrown inside it) + 6 IPO (exp. 5 of 40 at 7:1). Every IPO: ipo base_plate +
  ipo tower (regular/overgrown/damaged/overgrown_damaged, y +1..+48) + tower/top right above (+48..+75); no bare plate.
  `/place structure minecraft:pillager_outpost 2272 80 -2560` (= chunk [142,-160], an IPO regular): blocks y80-100 base +
  feature plates, y101-127 tower, y128-156 top (dark_oak_planks 668, stairs 186...). Visual = [game].
- ST-92: `zp gen creativeengineering:meta/ruin_scatter -30 42 5 1` (minecraft:forest, area 1): 5 scatters, anchors
  forest_ruins:ruin_4/ruin_2, philipsruins field_stone/ancient_ruins..., 2-3 small_ruins parts each. `/place` at
  -432 100 680: meta log anchor forest_ruins:ruin_2 + ruin_1 + ruin_3 (2/2), "Generated structure"; probe: mossy stone
  bricks 182, cobblestone walls 37, cracked 35, 1 lootr chest.

- ST-130: village_towers metas in st_d: [169,-114], [159,-127], [-23,15], [133,-137]. `/place structure
  supplementaries:way_sign 2130 100 -2400` (blooming_plateau, 2 forceloaded chunks): sign_post at 2139 93 -2389, SignUp
  active, Text1 road_sign "200", Yaw 176.80; target = meta/village_towers start chunk [133,-137] (2128,-2192): dx -11,
  dz 197 -> 197 blocks, atan2 gives yaw 180 + (-3.2) = 176.8 exactly. (New probe /zp findbe box substring.)
- Volcanic peaks: none in the y64 biome map (3D biome: peaks sit high). /zp biomemap takes an optional y now; y180 map
  over -8000..8000 step 160: 119 volcanic_peaks samples, nearest 0..160 x -1600..-2080. Forceloaded patch A 32..160 x
  -1904..-1776 (81 chunks, surface 150-260, basalt/blackstone/smooth_basalt) and patch B -64..16 x -1990..-1910.
- Toml: [worldgen.ground_match] log false -> true in the srv copy (test logging; Forge hot-reloads the common toml,
  confirmed by PALETTE taking effect without a restart). mode changed for ST-30 and put back to TERRAIN.
- ST-28: `/place structure philipsruins:ancient_ruins 80 236 -1856` (ruin_45): 4 [ground-match] lines, grass 0% of
  37/53/17/39 columns, replaced 37+6+1+0, kept terrain 51+3+8+1, top {basalt, blackstone, smooth_basalt}. Box probe:
  0 grass/dirt/flowers. `/place ... ancient_towers 128 200 -1808`: grass 34% and 16% (both < 35), replaced 44+5,
  probe 0 grass, 0 dirt, 4 coarse_dirt (tower templates have none: natural, jungle edge nearby).
- ST-30 PALETTE (48 236 -1888, ruin_39): kept terrain 0 in all 4 chunks, replaced 47+8+7+1 = the full pad from the
  sampled blocks. REMOVE: first two tries (64 256 -1792 = ruin_35, -24 215 -1950 = ruin_28) left the template's whole
  grass/dirt base and logged nothing; a TERRAIN control on patch B (8 221 -1930) also logged nothing -> NOT the mode.
  Cause (see Open issues): sampling skips columns under any structure piece in XZ; an ancient city at y -51 covered
  95-99% of the sample points there. REMOVE retest at 100 200 -1848 (chunk [6,-116], ruin_41, 64 base blocks):
  removed 48+7+8+1 = 64, replaced 0, kept 0; footprint probe 0 grass/dirt, walls on basalt. PASSED.
- ST-32: `/place template philipsruins:ancient_ruin_1 130 188 -1790`: layer 0 grass 67 / dirt 52 = template exactly,
  no [ground-match] line (gravel 17 -> 9 + 8 air: falling gravel, unrelated).
- ST-31: 'philipsruins:ancient_ruins' added to the SRV copy of worldgen/ground_match.js, /reload, `/place structure
  ... 104 200 -1864` (chunk [6,-117], ruin_9, coverage 0.53 = a spot where matching runs): no [ground-match] line;
  layer 0 (y199, found by its 5 light gray terracotta) grass 36 + dirt 30 (template 55 + 31; its own processors vary
  blocks). Entry removed (srv file = instance file again), /reload.

- ST-12: tech houses are removed by pool_tags.json tags mod/immersiveengineering, mod/dynamicvillage, mod/pneumaticcraft,
  mod/advancedperipherals (multiplier 0, "tech"). 42 saved village-ish starts in st_d: 0 pieces from those 4
  namespaces; wares warehouses 11, chef/cook houses 7. `zp gen minecraft:village_<plains|desert|savanna|snowy|taiga>
  100 -300 30 3` (146 starts generated, no placement): 0 with a tech piece, 83 with a domesticationinnovation pet
  shop (plains 20, desert 11, savanna 15, snowy 22, taiga 15), chefsdelight 127 pieces. Walk-through look = [game].
- ST-149 block part: meat_block_swaps.json = 5 marks in 4 templates (T&T birch butcher, Mediterranean butcher, mushroom
  butcher x2, skyvillages:farm_2). `/place template` each at y300 over patch A: all 5 marked spots `execute if block
  ... architects_palette:salmon_log[axis=y]` passed, red_glazed_terracotta failed. Natural skyvillage farm_2 at
  [165,-150] and [85,-140]: salmon_log 1, red glazed 0 each. The `templateBlockSwaps = false` part needs a restart:
  do it at the next reboot.

- ST-133 (part 1, one world): geodes.py over area 1 (-50..50 chunks, 76% ocean): 1097 chunks with budding + calcite.
  budding_amethyst: OCEAN n=577 shell top mean -12.9 (median -13), 9% reach the floor (floor-1), floor mean 24;
  LAND n=213 top mean 4.5 (median 5), 0% reach the floor (floor mean 94). Geode Plus types (vanilla Feature.GEODE):
  OCEAN n=214 top mean -0.6, LAND n=71 top mean 23.6. budding_prismarine: 27 chunks, all ocean, top mean -17.8. Placement
  height is biome-independent, so ocean ~17 lower than land fits drop 10 (plus land-sample noise); an exact check
  needs the same chunks with oceanGeodes = false (part 2, second world).
- ST-48/50: drift.py (driftwood_log blocks per chunk, block below, biome at the block, structure boxes excluded):
  area 1: 828 blocks, 733 inside meta/driftwood_flotilla etc. (density x4); free logs 13: 12 floating on ocean water
  (lukewarm/deep), 1 on dry beach sand; 0 in 136 river chunks. Area 2: 57 blocks (28 in structures); free: ocean water
  2, beach on sand 3; 0 in 475 river chunks, 0 in 454 swamp chunks (mangrove), 0 beach-water logs anywhere.
  No driftwood parse error of ours (44 lines = EveryCompat driftwood_post recipe noise).
  Cause, from decompiled UA 5.1.0 DriftwoodFeature.place: outside #is_ocean a log is placed only when the block
  UNDER the origin is dirt (+ isNearWater) or sand (+ direction open, ground mostly suitable); the standing variant
  also needs dirt/sand below. Our water versions (driftwood_beach/river/swamp/extra overrides) filter for water at
  -1, so they can NEVER place. Ocean: origin moved down 1, needs water below -> works. So beach/river/swamp driftwood
  now = only the land versions (1/10 rate). (UA scratch decompile in scratchpad ua_src/.)

- 11:23 boot 6: NEW world st_e (same seed) with srv toml [worldgen.systems] oceanGeodes = false and templateBlockSwaps =
  false (both scratch-only, for the comparisons; put back = srv only). Boot 5 log kept as scratchpad boot5_latest.log.
  Load line: `[template-swaps] 874 block swap(s) in 69 template(s) (off in CEConfig [worldgen.systems])`.
- ST-149 switch part: `/place template` of the 4 templates (5 marks) in st_e: every marked spot is
  minecraft:red_glazed_terracotta again. PASSED.
- 11:29 Chunky radius 248 around 0 0 in st_e (31x31 chunks of area 1, mostly ocean) for the geode comparison.

- ST-133 (part 2, two worlds): st_e Chunky radius 248 (1089 chunks, 1:4 min). geodes.py now also records the top y of
  each budding block type. Per chunk, same type, st_d (on) minus st_e (off), chunks -15..15:
  amethyst LAND {0: 32}; amethyst OCEAN n=42: -10 x10, -9 x10, -11 x5, -12 x4, -8 x4, -13 x2, -15, -7, -3, and 0 x4
  (stay share ~10%). Geode Plus: no chunk matched by type in either direction, and totals differ even where nothing
  should change (land GP blocks 969 vs 1260; untouched prismarine 57 vs 276; land amethyst identical 671 = 671): Geode
  Plus geodes are NOT reproducible across boots for a seed (most likely its biome-modifier features get a different
  index in the step per run -> different feature seeds). Not ours; it only rules out per-chunk checks for them.
  Distributions instead (top y of budding): ON (st_d area 1) GP stone-level ocean mean 2.1 vs land 18.7 (-16.6), GP
  deepslate ocean -27.4 vs land -18.6 (-8.8), amethyst ocean -19.5 vs land -4.0; OFF (st_e) GP stone ocean 9.5 vs land
  6.5, amethyst ocean -8.5 vs land -4.5 -> GP geodes are lowered too. Prismarine: ON mean -15.0 (n137), OFF -10.3 (n12):
  too few to judge; untouched path is a simple id check.
  Floor breaking, same 31x31: ocean geode chunks with a calcite shell at/above floor-1: ON 14 of 80, OFF 16 of 71;
  budding block at/above floor-1: ON 3, OFF 7. Deep-ocean floors here sit near y 0 (OCEAN_FLOOR -3..5), so geodes
  (placed up to y 30) still reach the floor after a 10-block drop: "far fewer shells break the floor" is NOT met.
- 11:35 server stopped (boot 6, st_e). Log kept as boot6_latest.log.

## Results

| Test | Result | Evidence |
|---|---|---|
| ST-47 | PASSED ([game] look left) | 40 generated: 34 vanilla with watchtower, 6 IPO each with tower + top; placed IPO complete to y156 |
| ST-92 | PASSED | /place in a forest: anchor ruin_2 + 2 forest ruins; 5 generated scatters with weighted anchors |
| ST-130 | PASSED | placed way sign: "200", yaw 176.8 = exactly toward the village_towers start 197 blocks off |
| ST-28 | PASSED | ruins + towers on volcanic peaks: grass share 0-34% < 35, pads replaced with basalt/blackstone mix, 0 grass/dirt left, plants none |
| ST-30 | PASSED (after a retest) | PALETTE: kept terrain 0, full sampled pad; REMOVE: removed 64 of 64 base blocks, walls on basalt. First REMOVE tries hit the coverage bug |
| ST-32 | PASSED | /place template: grass 67 / dirt 52 as saved, no ground-match line |
| ST-31 | PASSED | exempt tag: no ground-match line, grass pad kept; entry removed again |
| ST-12 | PASSED ([game] walk left) | 0 tech pieces in 42 saved + 146 generated villages; pet shops in 83 of 146, warehouses + chef houses present |
| ST-149 | PASSED | all 5 marked spots upright salmon logs (template placements + 2 natural sky-village farms); templateBlockSwaps = false brings the terracotta back (st_e) |
| ST-48 | PASSED (appears; "than before" not compared) | 14 free logs float on ocean water, 4 on beach sand in ~20k chunks; flotilla metas add most driftwood at density 4 |
| ST-50 | FAILED (shallows) | no parse errors; dry-land logs rare (4 in 1055 beach chunks), no river-bank logs, BUT 0 floating logs in 611 river / 454 swamp chunks / any beach water: the water versions can't place outside oceans (UA feature needs dirt/sand under the origin) |
| ST-133 | PARTLY | ocean amethyst lowered 7-15 (mostly 9-11) in 38 of 42 chunks, 4 stay, land 0; Geode Plus lowered (ocean-land gap 9-17 on vs 0-4 off); BUT shells reaching the floor 14/80 on vs 16/71 off (deep floors near y 0) |
| ST-14 | PASSED | /place: 5 Lootr barrels for 1 basket + 4 cabinets, crates untouched; 4 village chef/cook houses: Lootr barrels = template loot containers, 0 cabinets |

## Open issues
- BUG (older system, GroundMatch 3.x, not fixed): sample() excludes every column covered in XZ by ANY structure piece
  in the 3x3 chunks, whatever its height. A deep structure (ancient city at y -51, Philip's underground ruins,
  mineshafts) under a surface structure can hide nearly all columns -> < minSamples (16) -> palette null -> grass pad
  kept AND nothing logged (the log line needs a palette). Seen at 3 of 6 volcanic spots (coverage 95-99%); density 4
  makes it likelier (x4 ancient cities), but mineshafts alone can do it at density 1. Fix idea: exclude a column only
  if a piece box overlaps the sampled ground's y range (ground y - maxDig - subDepth .. ground y + a few), and log a
  "skipped: N samples" line when the palette is null for lack of samples. For the user.
- Logging gap: no [ground-match] line when grass is native or samples are too few (palette null). Folded into the bug above.
- BUG (older system, driftwood 2026-10-03, not fixed): the water versions of beach/river/swamp/extra driftwood never
  place: Upgrade Aquatic's DriftwoodFeature, outside oceans, requires dirt or sand directly under its origin, and
  our overrides filter for water there. Net effect: no floating driftwood in rivers, swamps or beach shallows; only the
  land versions (1/10 rate) and open ocean remain. Fix options: drop the water filter for non-ocean biomes and keep
  0.25x via rarity (UA then places on banks next to water, as designed), or filter "water adjacent" instead of
  "water below". For the user.

- ST-133 tuning (not a code bug): a fixed 10-block drop does little where the sea floor is at y 0..10 (Terralith deep
  oceans in this seed): geodes still reach it. Options: drop relative to the floor (keep the geode top N blocks under
  OCEAN_FLOOR_WG), or a larger drop for deep oceans. For the user.
- Note: Geode Plus geodes differ between two worlds of the same seed (not reproducible per seed). Not ours; relevant
  only to tests that compare worlds.
