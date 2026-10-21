# worldgen / structures/earthfill: implementation brief

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, rows `structures/earthfill` #2, #3, #4, #6, #7
(open) and #1, #5, #8 (Testing). Reference for how earth fill works today: [structure_tuning.md](../structure_tuning.md)
3.13 (earth fill), 3.14 (water fill), 3.15 (terrain adaptation, sunk starts), 7.5 (the 1.0.9 changes). Code:
`development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/EarthFill.java` (870 lines, below
`EarthFill.java:<line>`), `worldgen/TerrainAdaptation.java`, `worldgen/WorldgenConfig.java`, `CEConfig.java` (class
`EarthFill` at 1929), `mixin/ChunkGeneratorEarthFillMixin.java`. Tags: `kubejs/server_scripts/worldgen/earth_fill.js`,
`terrain_adaptation.js`, `water_fill.js` (all through `global.tags.add`). Instance config:
`config/creativeengineering-common.toml` `[worldgen.earth_fill]` at line 1387 (`overrides` 1405, `maxWaterSurplus` 1449).

Proposed test ids ST-206 to ST-214 are provisional: other structure briefs written the same day may propose the same
numbers, so renumber against structure_tuning.md 8 (highest today: ST-205) when adding them.

None of these rows touches recipes, items or the `global.NCH` / `_lib/*_dict.js` dictionaries: they are worldgen data
(structure tags, which go through `global.tags.add` as today), CEConfig values and patcherMod Java.

## Scope and order

| Order | Row | Kind | Why this order |
|---|---|---|---|
| 1 | Bugs A and B (new, below) + #2 | patcherMod (2 small fixes) + config | Two bugs in the 1.0.9 earth fill found while reading the code. Fix them before any calibration, because bug A changes what a `log = ALL` run cancels |
| 2 | #3 sunk-start candidates | data (+ optional patcherMod) | Uses the sunk-start machinery; bug B changes its earth-fill numbers, so after 1 |
| 3 | #4 undead castle | data / config | Needs a `log = ALL` sample run; do it in the same scratch run as #2 and #3 |
| 4 | #6 modded limits | none left (close it) | Phase 7 (ST-52) already did the calibration; what is left is #2 and #4 |
| 5 | #7 hot spots | patcherMod | Independent; two separate changes (twigs obelisk, terrain cache). Only if worldgen speed matters (decision) |

One scratch dedicated server run can cover 1 to 3: seed 8675309, `structureDensity` 4, `[worldgen.earth_fill] log =
"ALL"` (scratch toml only), Chunky r 400 at 2144 -2592 (the inland square of 12_earth_fill_camps.md) and r 400 at 0 0
(coasts). Recipe for the scratch server: [testing/structures/12_earth_fill_camps.md](../testing/structures/12_earth_fill_camps.md) 2.

## Bugs found while reading the code (not in TODO yet)

**Bug A: `log = ALL` changes which starts are cancelled.** An override with every limit -1 (for example
`"#creativeengineering:type/village=-1,-1,-1"`) is meant to skip earth fill entirely, footprint share included (CEConfig
comment at `CEConfig.java:1974`, structure_tuning 3.13). That only holds at `log = CANCELLED`/`OFF`:
- `EarthFill.java:828` `earthCheck` returns null for an unlimited override only when `log != ALL`.
- `EarthFill.java:727` skips the count only when `limits.none() && surplus < 0 && log != ALL`; with `surplus` 0.5 it
  goes on, and `EarthFill.java:730-732` computes the footprint and cancels when it is too wet.
So with `log = ALL` an unlimited structure can be cancelled by the footprint share; with the shipped `log = CANCELLED`
it never is. ST-54 passed on 1.0.7 (no footprint share yet). Fix: in `EarthCheck.cancels`, compute
`boolean unlimited = earthUnlimited(structure)` and pass it into the decision: `wet = !unlimited && f != null &&
f.tooWet(surplus)` (still log the footprint numbers under ALL). Add an `EarthFillTest` check: unlimited override +
`log = ALL` + a wet footprint -> kept. Rerun ST-54 with `log = ALL` afterwards.

**Bug B: the beard reach is over-estimated for sunk starts.** `TerrainAdaptation.rigid` (`TerrainAdaptation.java:404`)
raises a sunk start's beard ground level by its sink (`groundLevelDelta += sink`). The beard fills `beardReach` (6) below
its ground level, so under the sunk piece's bottom it now fills only `6 - sink` blocks. `EarthCheck.cancels`
(`EarthFill.java:721`) still builds `new EarthPiece(earth, box.minY(), beard)`, so `countEarth` assumes 6 blocks of fill
under the real bottom: for the 6 Campsite camps (sink 4) earth over a 3 to 6 block gap is counted as filled though the
beard reaches 2. The mound test (`openBelow(x, bottom - 1, ...)`) has the same offset. Fix: `new EarthPiece(earth,
box.minY() + sink, beard)` (the `Layer` on the next line already uses `box.minY() + sink`); `countEarth`'s
`deeper = fills + (y - bottom) + 2` then becomes `fills - (bottom - y) + 2` for earth below the raised bottom, which
is the right remaining reach. Test: an `EarthFillTest` case with a sunk piece (bottom 60, sink 4, earth at y 60 over a
4-block gap) -> counted over air (today: not counted). Expect slightly more campsite cancels on slopes; ST-199 should
still pass.

---

## structures/earthfill #2: `maxWaterSurplus` first value
Status Planned · Work config · TODO `worldgen/structures/earthfill #2`

### Goal
Settle the footprint water share threshold (`[worldgen.earth_fill] maxWaterSurplus`, shipped 0.5 in 1.0.9) from real
cancels, and decide what happens to a village on a lake (today it goes as a whole).

### Now
- Rule: `EarthFill.Footprint.tooWet` (`EarthFill.java:249-252`): cancel when water > ground x (1 + surplus).
  Footprint = every column of every earth piece's bounding box at `minY + sink` (`EarthFill.java:259-275`, layer built
  at 722). Not for `water_fill/structures` (`EarthFill.java:836`).
- Config: `CEConfig.java:1966-1975` (default 0.5, range -1 to 1000), read live by `WorldgenConfig.earthMaxWaterSurplus()`
  (`WorldgenConfig.java:112`); instance toml line 1449.
- Data so far (12_earth_fill_camps.md 2, diag3, 2,601 inland chunks, 972 starts): 21 "too wet", 14 by the share alone.
  Cancelled ratios (water / ground): 739/0 (sunflower plains village), 1,691/497 = 3.4 (birch forest village), 2,123/66
  (drum tower), 255/0 (windmill), 505/244 = 2.07 (big horse stable), T&T outposts, desert watchtower, a Mysticrift
  tower. Kept starts with any footprint water: 5 of 926, the wettest 470/425 = 1.11.
- So on this sample any surplus between 0.11 and 1.07 gives the same result. 0.5 sits in the gap.
- Pruning (`EarthFill.pruned`, `EarthFill.java:769-811`) drops village pieces over deep water or mounds, but has no
  share rule, so a village whose houses stand on a shallow lake (beard-filled) is cancelled whole by the share.

### Build
1. Fix bugs A and B above first.
2. Scratch run (setup in "Scope and order"), coast square included (the inland square had almost no shore cases).
   `grep '\[earth-fill\].*footprint' logs/latest.log` and tabulate water/ground for kept and cancelled starts, by
   structure. For every share-only cancel (`too wet` with water/air/mound under their limits), teleport to the chunk
   and look (or probe with the scratch `/zcol` KubeJS probe from 12_earth_fill_camps.md 2): open water or a lake
   island = right; a shore with a small pond = wrong.
3. Keep 0.5 unless a wrong cancel shows a ratio under 1.5 (then raise to 1.0) or a kept island camp shows one over 1.5
   (then lower to 0.25). Edit both the instance toml (line 1449) and the CEConfig default (`CEConfig.java:1975`) so new
   tomls match; the toml alone is enough for the pack.
4. Villages on lakes (decision below): if wanted, add a per-piece share to pruning. In `EarthFill.pruned` (loop at
   `EarthFill.java:778-787`) compute `footprint(List.of(layerOf(piece, sink)), terrain, baseLayers)` per piece and mark
   it offending when `tooWet(surplus)` and `water >= pruneMinWetColumns` (new config, say 16, so a 3x3 well over a
   puddle is not dropped). The `pruned` signature needs the surplus and sink; `EarthCheck.prune` (750) has both.
   Log reason `(... footprint W water / G ground)`.

### Ids and data
Config keys only: `[worldgen.earth_fill] maxWaterSurplus`, new `pruneMinWetColumns` (if step 4). Structures named
above come from the diag3 log (in-repo summary in 12_earth_fill_camps.md 2). Nothing verified against a jar.

### Decisions
- **Value:** keep 0.5 (recommended) unless step 2 finds a wrong cancel. The sample has a wide gap between kept and
  cancelled ratios.
- **Village on a lake:** prune wet pieces (step 4, recommended: matches how pruning handles deep water) or keep
  cancelling the whole village (today; simpler, villages are common).

### Gotchas
- The footprint is the piece's bounding box, not its built columns: a house with a big yard over a pond reads wetter
  than its walls. For trimmed structures (`template_trim`) consider `TemplateTrim.columns(t).footprint(true)` instead
  of the box (same as the soft beard footprint) if the run shows yard-driven cancels.
- Config is read live, but Forge only re-reads the file on change; the `avoidCoasts` test showed a value not reloading
  for 10 minutes (12_earth_fill_camps.md 2). Restart the scratch server between values.
- Meta structures are judged part by part (`filterParts`, `EarthFill.java:690-705`): a wet part cancels the whole meta
  and the slot falls back to the plain anchor.

### Tests
- ST-206 Share value [scratch]: the run of step 2; every share-only cancel probed; none on a shore with mostly dry
  footprint.
- ST-207 Unlimited override with `log = ALL` (bug A) [scratch]: `overrides = ["#creativeengineering:type/village=-1,-1,-1"]`,
  `log = ALL`: no village logs `CANCELLED`, the footprint numbers still print.
- ST-208 Wet piece pruned (only with step 4) [scratch]: a village at a lake shore logs `pruned K of N` with a
  footprint reason and keeps its dry part.

---

## structures/earthfill #3: Sunk-start candidates not tagged
Status Planned · Work data · TODO `worldgen/structures/earthfill #3`

### Goal
Decide which other structures with a negative start height get the `creativeengineering:terrain_adaptation/sunk_start`
tag (beard levels the ground at the projected surface instead of at the sunk bottom), and tag them.

### Now
- Tag list: `kubejs/server_scripts/worldgen/terrain_adaptation.js:98-101` (the 6 Campsite camps).
- Code: `TerrainAdaptation.sinkOf` (`TerrainAdaptation.java:233-246`) returns `-start_height` only for a
  `JigsawStructure` with a constant absolute start height below 0 and a `project_start_to_heightmap`; anything else is
  logged "has no constant start height below 0 projected to a heightmap; ignored" (161). `rigid()` (395-414) adds the
  sink to every Rigid of the start; earth fill's footprint layer uses it too (`EarthFill.java:722, 838`).
- Vanilla's Beardifier only adds a beard for **rigid** pool pieces (and junctions); terrain-matching pieces get none.
  JigsawPlacement places a rigid child of a terrain-matching parent at the surface height of its jigsaw (not relative
  to the parent). So for a structure whose start piece is terrain-matching, nothing is sunk under a beard, and raising
  every rigid child by the sink would bury their floors by that many blocks. (1.19.2 JigsawPlacement/Beardifier
  behaviour from memory of the vanilla source; not re-read here, the vanilla jar isn't in this session.)

Candidates, read from the jars (verified: `PhilipsRuins1.19.2-10.8.jar`, `Towns-and-Towers-v.1.10-_FORGE-1.19.2_.jar`;
none is overridden in `kubejs/data`):

| Structure | start_height / heightmap | adaptation | Start piece | Template bottom | In earth_fill/exempt | Verdict |
|---|---|---|---|---|---|---|
| `philipsruins:ancient_crypt` | -5 / OCEAN_FLOOR_WG | beard_box | `philipsruins:crypt_start`, rigid (pool json has comments, lenient parse) | 8x7x9 entrance, only y 5-6 above ground when sunk 5 | no | **tag** (beard_box at the sunk bottom digs a 5-deep pit around the entrance; tagged, the entrance roof sits at the surface) |
| `philipsruins:antiquus_crypta` | -5 / OCEAN_FLOOR_WG | beard_box | `philipsruins:antiquus_crypta_main`, **terrain_matching** | - | no | **don't tag** (start piece has no beard; children would be buried) |
| `philipsruins:ocean_fortress` | -5 / OCEAN_FLOOR_WG | beard_box | `ocean_fortress_main`, rigid, 24x21x43, y 0 = 909 sand | sand plate meant to sit in the seabed | yes | tag after a look (seabed pit vs half-buried) |
| `philipsruins:ocean_ruins` | -3 / OCEAN_FLOOR_WG | beard_thin | 12 rigid ruins, y 0 dirt/gravel/sand | buried base | yes | tag after a look (same) |
| `philipsruins:ancient_ruins` | -1 / OCEAN_FLOOR_WG | beard_thin | 22 rigid ruins, y 0 = grass + dirt | 1 layer sunk | no | optional (a 1-block dip ring today; tag if it shows) |
| `philipsruins:rare_ruin` | -1 / WORLD_SURFACE_WG | beard_thin | **terrain_matching** | - | no | **don't tag** (also `plan/move_to_nether`) |
| `philipsruins:end_gate_fortress` | -1 / MOTION_BLOCKING | beard_thin | (End) | - | no | optional, End only, low value |
| `towns_and_towers:exclusives/village_nilotic` | -1 / WORLD_SURFACE_WG | beard_thin | `kaisyn:village/exclusives/nilotic/base_plate`, legacy, **terrain_matching** | - | no | **don't tag** (houses are surface-projected children; tagging buries every house floor 1 block) |

### Build
1. Optional hardening (patcherMod, recommended so a wrong tag can't bury houses): in `sinkOf`, read the start pool's
   elements (`JigsawStructure` field `startPool`, SRG `f_227607_`; read it the same way `START_HEIGHT` is read at 230)
   and return 0 with a warning when any element's `getProjection()` is `TERRAIN_MATCHING`. Further option: apply the
   sink only to pieces whose jigsaw chain to the start is rigid-to-rigid (EarthFill already builds the tree with
   `EarthFill.links`, 600-625); not needed for the tags recommended here.
2. Look in game first (`/place structure philipsruins:ancient_crypt` on a hillside and on flat ground, the same for
   `ocean_fortress` and `ocean_ruins` on a seabed; F3 heights of the ground ring against the template's top layer).
3. Add the chosen ids to the `sunk_start` list in `terrain_adaptation.js:98-101`, with a comment per group (crypt
   entrance; seabed ruins). Rejoin, check the load line `[terrain-adaptation] world load: ... N sunk starts` (ST-200's
   line) has the new count and no "ignored" warning.
4. Update structure_tuning.md 3.15 "Sunk starts" paragraph (the "Opt-in, because ... were not looked at" list).

### Ids and data
Structure ids above (verified in the jars). Tag `creativeengineering:terrain_adaptation/sunk_start`. Fields:
`JigsawStructure.startHeight` `f_227608_`, `projectStartToHeightmap` `f_227610_` (used today); `startPool`
`f_227607_` (unverified SRG name: check with `javap -p` on the mapped class or the mappings before using it).

### Decisions
- **Which to tag:** `ancient_crypt` yes; `ocean_ruins` and `ocean_fortress` after the look (recommend yes: their sand /
  gravel bottoms are made to be buried, and the beard pit just widens the hole); `ancient_ruins` only if the 1-block
  ring shows; never the three terrain-matching starts.
- **Hardening (step 1):** recommended, small.

### Gotchas
- Bug B (top of this file) matters more as sinks grow: at sink 5 the beard fills only 1 block under the crypt's
  bottom, and earth fill today assumes 6.
- The seabed ruins are in `earth_fill/exempt` (`earth_fill.js:16`), so earth fill never judges them; the tag changes
  only their beard.
- Sink raises the beard of every rigid child too; the crypt's underground rooms are rigid children of a rigid start,
  so they get a beard level 5 above their floor. Underground that fills a little around the rooms (ground that is
  solid anyway); check one crypt's rooms for filled corridors next to them.
- New chunks only.

### Tests
- ST-209 Crypt entrance flush [scratch]: a tagged `ancient_crypt` on a slope: the ground around the entrance is at the
  entrance roof's level, no pit; its rooms open.
- ST-210 Seabed ruins half buried [scratch] (if tagged): the seabed around an `ocean_ruins` piece is at its original
  height, the ruin's bottom 3 layers in the sand.
- ST-211 Terrain-matching start refused [scratch] (with step 1): tag `village_nilotic` on a scratch server only -> the
  load log warns and ignores it.

---

## structures/earthfill #4: Undead castle cancelled by earth fill
Status Planned · Work data · TODO `worldgen/structures/earthfill #4`

### Goal
Get `eugene:undead_castle` to survive often enough to be found, without floating pads.

### Now
- Structure: `kubejs/data/eugene/worldgen/structure/undead_castle.json` (jigsaw, `beard_box`, start height 0 on
  `WORLD_SURFACE_WG`, one-piece pool `kubejs/data/eugene/worldgen/template_pool/undead_castle.json` ->
  `eugene:plains_undead_castle`). Group `surface_dungeons` (`kubejs/generation_scripts/worldgen/structure_groups.json`
  929-936: weight 1, frequency 0.026314, `fit_base` 1 / 0.0255). Fits: `land/plains`, `land/highlands`, `land/taiga`
  (`structure_tags.json`, e.g. `land/highlands` from line 1177); also `dungeon/surface_modded`.
- Template (read from `kubejs/data/eugene/structures/plains_undead_castle.nbt`): 43x28x45, all 1,935 columns hold a
  block at y 0; 2,495 earth blocks in the bottom 3 layers (y 0: 1,563 dirt/grass), so it is `huge` (bounds 2,000).
  1,091 columns have a built block, 844 are pad only (the courtyard and an apron round the walls): a walled compound.
- Scratch M2 (02_new_mods_tests.scratch.md 142-146): 5 rolls, 4 cancelled: over air after the beard 683 / 314 / 407 /
  514 (huge limit 320), mound columns 448 / 264 / 0 / 341 (limit 128). Only one ([184, -158]) was cancelled on the
  mound limit alone (air 314). Terrain relief under two of them 30 and 12 to 15 blocks. The fifth was kept and then
  dropped by the peaceful radius. Phase 7 (07_phase_7.md 2.1) adds that a mound column nearly always comes with air
  or water, so relaxing the mound limit alone would have saved 1 of the 4.

### Build
Option set (pick in Decisions):
1. **Override** (config): `[worldgen.earth_fill] overrides = ["eugene:undead_castle=24,320,400"]` in
   `config/creativeengineering-common.toml:1405` (water 24 = the global default, air 320 = the huge default, mound 400).
   Saves mound-only cancels; leaves real overhangs cancelled.
2. **Narrow the fits** (data): remove `eugene:undead_castle` from `land/highlands` in `structure_tags.json`, then
   `python3 tools/gen_structure_groups.py --fit-rates` to re-solve its frequency over the remaining fits (it has a
   `fit_base`, so the run starts from 1 / 0.0255; check the diff of every other fitted member, as for the fresh
   watchtower row in rates-and-groups), then `tools/test_structure_groups.sh`.
3. **Compensate the rate** (data): raise its frequency by the measured survival (e.g. x2.5 at 40% survival, capped at
   1.0). The fit solver can't see earth fill, so add a note in its `structure_groups.json` entry and keep `fit_base`
   as is; `--fit-rates` would overwrite a hand value, so set `fit_keep`-style protection or re-apply after each run
   (check `tools/gen_structure_groups.py` 1265-1420 for how hand values survive).
4. Before choosing, measure: scratch run with `log = ALL` at density 4 over two r 400 squares in plains/taiga/highlands,
   `grep 'eugene:undead_castle' latest.log`. The log line has no biome, and chunks stopped at `structure_starts` hold
   placeholder biomes, so read the biome source at each chunk with a scratch KubeJS probe calling `getNoiseBiome` (the
   trick from 02_new_mods_tests.md 6), and tabulate cancels per fit. Optional small patcherMod help: add the start's
   biome to the `[earth-fill]` log line under `log = ALL` (`EarthFill.java:734-742`).

### Ids and data
`eugene:undead_castle`, `eugene:plains_undead_castle` (kubejs/data, verified by reading the files). Override syntax:
`"<id>=<water>,<air>[,<mound>]"` (`EarthFill.parseOverrides`, `EarthFill.java:514-528`).

### Decisions
- **Recommended:** 4 (measure), then 1 (mound 400) + 2 (drop `land/highlands` if most cancels are there). Don't relax
  the air limit: over-air 400 to 700 is a floating pad you can see.
- Alternative: accept the rarity (it is a hostile surface dungeon with a boss; rare is fine) and close the row.

### Gotchas
- The kept M2 castle stood in `alpine_highlands`, so highlands is not hopeless; decide on the per-fit numbers.
- Switching the castle to `terrain_adaptation/soft` does not help earth fill: the soft beard shares `beardReach`, and its
  footprint is still the whole box (every column has a block). `template_trim` would drop the 844 pad columns (the
  courtyard becomes natural slope) and move it to `large` (1,338 earth in built columns), whose limits (160 / 64) are
  tighter: worse.
- The drum tower (`eugene:ruined_drum_tower`, stone-base tag, row #5) has the same problem in Phase 7 (huge, 1 of 1
  cancelled at 1,366 air) and in diag3 (2 cancelled too wet); look at both in the same run.

### Tests
- ST-212 Castle survives [scratch]: after the change, in the measurement squares at least half of the rolled castles
  are kept, and none of the kept ones has earth over more than 6 blocks of air (probe one on a slope).

---

## structures/earthfill #6: Earth-fill limits for modded structures
Status Planned · Work config · TODO `worldgen/structures/earthfill #6`

### Goal
Calibrate `[worldgen.earth_fill.sizes]`, `maxOverWater` and overrides against modded structures.

### Now
Already done: ST-52 ran on the scratch server 2026-10-19 (Phase 7, [07_phase_7.md](../testing/structures/07_phase_7.md)
2.1, structure_tuning.md ST-52): main square r 800, density 4, `log = ALL`, prune working. Default limits
cancel 9% of small and 12% of medium surface starts, 1 of 12 large (carp fish pond, air 209) and the one huge (drum
tower); underground cancels are mostly Subterrestrial ocean cabins in open sea (correct). The log's conclusion:
"Nothing here argues for a change except the drum tower". The only things left are the 1.0.9 footprint share (#2) and
the two huge structures (#4, #5).

### Build
Nothing to build. Close the row, or fold it into #2 (the next `log = ALL` run). If kept: in the #2 run, also tabulate
per-size percentiles again (the 1.0.9 jar changed the count only through the footprint), and look at the carp pond
(`farmers_structures:aquacultures_carp`, `none` adaptation, start height -4): it is `large` and was cancelled on air
because `none` counts every earth block of its sunk dirt bowl; the terrain brief
([worldgen_structures_terrain.md](worldgen_structures_terrain.md) #1) proposes soft + trim + sunk_start for it.

### Decisions
- Close (recommended) or merge into #2.

### Tests
- ST-52 itself is still unticked in structure_tuning.md (it is a DATA test); tick it when the row closes.

---

## structures/earthfill #7: Worldgen hot spots
Status Planned · Work patcherMod · TODO `worldgen/structures/earthfill #7`

### Goal
Remove the two pack-side worldgen costs Phase 10 found: twigs' obelisk check (3.1% of worldgen CPU) and the repeated
earth-fill terrain prediction (about 4%: 62% of creativeengineering's 6.8%).

### Now
- Phase 10 ([10_phase_10.md](../testing/structures/10_phase_10.md) 2-3, locate.md 3.7): JFR of an Overworld Chunky
  pregen. `structure_starts` runs about 16 times per chunk (placement-priority predictions and meta predictions rebuild
  nearby starts); each rebuild reruns `EarthFill.predicted` -> `ChunkGenerator#getBaseColumn` per 2x2 cell.
- `EarthFill.predicted` (`EarthFill.java:451-472`) caches columns per start only (a new `Long2ObjectOpenHashMap` per
  call, from `filter` 678 and `filterParts` 699). `PlacementPriority` caches its predictions per (generator, set,
  chunk) (`PlacementPriority.java:316-320`), but the real generation of that chunk later builds the start again and
  reads the terrain again.
- Twigs (verified: `twigs-forge-1.19.2-3.0.1.jar`, `com/ninni/twigs/world/gen/structures/ObeliskStructure`, decompiled
  to scratchpad `decomp/twigs_obelisk/`): `findGenerationPoint` (`m_214086_`) calls `canGenerate` first, which reads
  `getBaseColumn` for all 121 columns of an 11x11 square (and scans y 80 down to sea level in each) **before** vanilla's
  biome test. Its biomes are `minecraft:nether_wastes`, `minecraft:crimson_forest`
  (`data/twigs/tags/worldgen/biome/has_structure/bloodstone_obelisk.json`). In the pack it is in `group/nether_ruins`
  (`structure_groups.json` 1718: weight 194, frequency 1.0; spacing 14, separation 7). The profile was an Overworld
  pregen, so the set's slots are tried in the Overworld and every obelisk pick pays the 121 columns (1.19.2's
  `ChunkGenerator#createStructures` walks every structure set; inferred from the profile, the vanilla source wasn't
  re-read here).
- A helper already answers "can this structure spawn in this dimension": `MetaSlots.canSpawnAmong(Structure,
  Set<Holder<Biome>>)` (`MetaSlots.java:391-395`, biome rules applied; fits are inside `Structure#biomes()` via
  `StructureBiomesMixin`).

### Build
**A. Skip structures that can't spawn in this dimension** (generic; fixes the obelisk and every other Nether/End
member tried in the Overworld):
1. New `worldgen/DimensionFilter` (or a method in `StructureFrequencies`): `static boolean impossible(ChunkGenerator g,
   Structure s)`, cached in a `Map<BiomeSource, Map<Structure, Boolean>>` (WeakHashMap outer, IdentityHashMap inner),
   value `!MetaSlots.canSpawnAmong(s, g.getBiomeSource().possibleBiomes())`. Clear on `ServerStoppedEvent` and
   `TagsUpdatedEvent` (biome rules and fits are rebuilt at ServerAboutToStart).
2. Hook: a `@Inject(method = TRY_GENERATE, at = @At("HEAD"), cancellable = true)` returning false, in
   `ChunkGeneratorStructureFrequencyMixin` (it already has a cancellable HEAD inject at line 52) or a new mixin with the
   same `TRY_GENERATE` descriptor string as `ChunkGeneratorEarthFillMixin.java:29-32`. Return false = the pick failed,
   exactly what the biome test does later.
3. Exclusions: `MetaStructure` (its biomes are the anchor's; `canSpawnAmong` on the meta may be wrong) and the
   replacements of `MetaSlots` (they take another structure's biomes: `MetaSlots.narrowBiomes`). Skip the shortcut for
   both (`s instanceof MetaStructure || MetaSlots.isReplacement(s)`; add that accessor if missing).
4. Narrow fallback if A is judged too broad: a mixin on `com.ninni.twigs.world.gen.structures.ObeliskStructure`
   `m_214086_` HEAD that returns `Optional.empty()` when the noise biome at the chunk's middle (quart coordinates, y at
   sea level) is not in `this.biomes()`. Needs `"twigs"` as a soft target (`@Pseudo` or a mixin plugin check) so the jar
   doesn't hard-depend on twigs.

**B. Cache the predicted terrain across starts:**
1. In `EarthFill`, replace the per-start map with a bounded cache per `(ChunkGenerator, RandomState)`: key
   `ChunkPos.asLong(cx, cz)` of the grid cell centre, value a compact column: `byte[]` of `Kind` per y (384 bytes for
   -64..320) plus `top` and `topWater`. `Kind.SEABED` is derived at read time from `topWater` as today.
2. Bounded: Guava `CacheBuilder.newBuilder().maximumSize(65_536).build()` (Guava ships with Minecraft; about 26 MB at
   400 bytes a column) inside a `WeakHashMap<RandomState, Cache>` guarded by `synchronized`, or a striped
   `ConcurrentHashMap` with a clear-when-full rule. Worldgen runs on several threads: the cache must be thread-safe.
3. Key the cache on `sampleGrid` too (a config change mid-run must not mix grids) and clear it on `ServerStoppedEvent`.
4. `predicted(...)` keeps its signature; it reads through the cache. The `LevelHeightAccessor` passed today is the chunk
   (min/max build height); the cache must store min y with the column (prediction scratch chunks share the level's
   height).
5. Don't use it for DH's clipped chunks: `getBaseColumn` is not clipped (DistantClip only clamps noise for marked
   chunks), so sharing is safe, but check that `DistantClip.clamp` doesn't see the prediction's accessor as a marked
   chunk.

### Ids and data
Twigs: class `com.ninni.twigs.world.gen.structures.ObeliskStructure`, method `m_214086_(Structure.GenerationContext)`
(SRG, verified in the decompile), `canGenerate(GenerationContext, BlockPos)` (static). Structure `twigs:bloodstone_obelisk`.
Vanilla: `ChunkGenerator#tryGenerateStructure` `m_223104_`, `getBaseColumn` `m_214184_` (as used by twigs).

### Decisions
- **Whether worldgen speed matters** (the row's own decision): the two together are about 7% of worldgen CPU on the
  worker threads, while DH's LOD threads take 35% of all CPU during a pregen. Recommended: do A (small, generic, also
  cuts other-dimension structures from the Overworld's 16x rebuilds), do B only if a later profile still shows
  `EarthFill.predicted` near the top.
- A generic (recommended) vs the twigs-only mixin.

### Gotchas
- A must not change results: anything it skips would have failed the biome test anyway. Verify with
  `/celocate census` counts before/after on the same seed, and `tools/test_structure_groups.sh`.
- `StructureCheck`/`/locate` don't go through `tryGenerateStructure`; the locate path has its own dimension check
  (locate.md 7.3.2 dimension check).
- Twigs' `canGenerate` also runs in the Nether, where it is needed; A leaves that alone.
- B: the prediction must be exactly what the uncached version returns, or placement-priority predictions and real
  generation disagree (structure_tuning 3.22 relies on predictions being exact). It is, as long as the key includes
  the generator, RandomState and grid.

### Tests
- ST-213 Same starts with the filter [scratch]: two fresh worlds, same seed, Chunky r 64 at 2144 -2592, filter on/off:
  identical `/celocate census` member counts; the JFR shows no `ObeliskStructure#canGenerate` in the Overworld.
- ST-214 Terrain cache exact [scratch] (if B): `log = ALL` earth-fill lines identical with the cache on and off over
  the same square; JFR share of `EarthFill.predicted` down.
- Offline: an `EarthFillTest` case that the cached and uncached `Terrain` answer the same for a fake generator.

---

## Built, needs play

| Row | Status | Notes from this pass |
|---|---|---|
| #1 Earth fill on water, camp plinths, `avoidCoasts` | Testing | DEPLOYED with 1.0.9 (2026-10-19). Two bugs in it, A and B at the top of this file |
| #5 Stone bases are opt-in | Testing | Only `eugene:ruined_drum_tower` (`earth_fill.js:33-35`). The drum tower is cancelled often (Phase 7: huge, 1 of 1; diag3: 2 too wet): look at it with #4. Candidate for the tag later: `eugene:piglin_camp` (Nether; plan_1.0.6.md 2.1) |
| #8 Water fill | Testing | ST-56, 58, 59, 60 passed; ST-57 has data (07_phase_7.md 2.4) and is still unticked |

## Writeup corrections made

- [structure_tuning.md](../structure_tuning.md) 3.13 footprint paragraph: added that an all -1 override skips the share
  only below `log = ALL` (bug A).
- [structure_tuning.md](../structure_tuning.md) 7.5 heading: added "DEPLOYED with 1.0.9 2026-10-19".
- [structure_tuning.md](../structure_tuning.md) 7 Todo, "Terrain adaptation leftovers": the modded earth-fill
  calibration ran as ST-52 in Phase 7; only the 1.0.9 footprint share needs its own run.
- [testing/structures/02_new_mods_tests.md](../testing/structures/02_new_mods_tests.md) 4 item 2: "(patcherMod 1.0.7,
  not built or shipped)" -> "(patcherMod 1.0.7, DEPLOYED 2026-10-18)".
- (structure_tuning.md 3.23's "None is in `mods/`" and 12_earth_fill_camps.md 3's "Not deployed" were stale too; another
  pass corrected both the same day before this one got to them.)
