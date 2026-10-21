# worldgen / structures/terrain: implementation brief

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, rows `structures/terrain` #1, #2, #3 (all
Planned). Reference: [structure_tuning.md](../structure_tuning.md) 3.13 (earth fill), 3.15 (terrain adaptation by tag,
soft beard, template trim, sunk starts), [terrain_adaptation_scan.md](../terrain_adaptation_scan.md) (the scan),
[locate.md](../locate.md) 3.7 (the bottom band). Code: patcherMod
`development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/TerrainAdaptation.java`,
`TemplateTrim.java`, `EarthFill.java`, `DistantClip.java`; tags `kubejs/server_scripts/worldgen/terrain_adaptation.js`
(through `global.tags.add`); scan `tools/terrain_adaptation_scan.py`.

Proposed test ids ST-215 to ST-219 are provisional (renumber against structure_tuning.md 8 when adding them; other
structure briefs may claim the same numbers).

No recipes, items or `_lib/*_dict.js` ids are involved: these rows are structure tags (data), a test tool and Java.

## Scope and order

| Order | Row | Kind | Note |
|---|---|---|---|
| 1 | #3 skip the bottom band | none left | Already built and shipped (DH clip, 1.0.6). Only LO-11 [game] is open: move the row to Testing |
| 2 | #1 batch 4 | data, after a re-scan and a look | Re-run the scan first (it predates the 2026-10-14 mods); pilot the fish ponds, then the spire and Philip's level-three ruins; villages last, one family at a time |
| 3 | #2 Beardifier fill estimate | tool, then maybe patcherMod | Independent; the tool part answers whether the patcherMod part is needed |

#1's carp-pond change uses the sunk-start tag, so read the earth-fill brief's bug B
([worldgen_structures_earthfill.md](worldgen_structures_earthfill.md)) first: the earth-fill reach of sunk starts is
over-estimated until it is fixed.

---

## structures/terrain #1: Terrain adaptation batch 4
Status Planned · Work data · TODO `worldgen/structures/terrain #1`

### Goal
Tag the last structures the scan flagged (soft beard and/or template trim) after looking at each in game: Terralith's
spire, the fish ponds and the swamp fisherman house, Philip's padded ruins, and decide on the villages.

### Now
- Tags today (97 + 6 sunk): `terrain_adaptation.js:19-101`. Load line `[terrain-adaptation] world load: 103 structures
  tagged, modes {NONE=6, SOFT=91}, 95 trimmed, 6 sunk starts` (12_earth_fill_camps.md 2).
- The scan ([terrain_adaptation_scan.md](../terrain_adaptation_scan.md), data
  `kubejs/generation_scripts/worldgen/terrain_adaptation_scan.json`) was generated **2026-10-09**, before the
  2026-10-14 ports (Camps. Castles. Carriages. `eugene:*`, Campsite Structures, Subterrestrial, Underground Bunkers):
  none of those 4 namespaces is in the JSON. It reads `mods/*.jar` and the tag export
  `kubejs/exported/tags/block_tags.json`; neither is in the repo, so it only runs on the instance machine.
- Batch 4 candidates (scan numbers, structure JSON verified against the jars where noted):

| Structure | Adaptation / start | Box cols / built / pad / air-only | Also in | Scan suggestion |
|---|---|---|---|---|
| `terralith:spire` | none, start 29 over `WORLD_SURFACE` (Terralith 2.3.12 jar) | 23,381 / 13,019 / 841 / 5,555; 180,302 air outside built; 31,008 water (the moat of `spire/base_l`, `base_r`) | `type/tower`, `dungeon/surface_modded`, `land/icy` | template_trim |
| `farmers_structures:aquacultures_common` | beard_thin, start 0 (FarmersStructures-1.0.0 jar) | 400 / 157 / 109 / 50 | `water_fill/ponds`, `meta/farmers_structures` | soft + trim |
| `farmers_structures:aquacultures_carp` | **none, start -4** on `WORLD_SURFACE_WG` (jar) | 400 / 267 / 87 / 1 | same | template_trim (+ soft) |
| `farmers_structures:aquacultures_beach` | absolute y 57, no projection (jar) | skipped (fixed height) | same | none |
| `old_swamp_fisherman_house:fisherman_house` | beard_thin, start 0 (jar) | 476 / 353 / 123 / 0; 258 water | `water_fill/ponds`, `land/swamp` | soft + trim |
| `philipsruins:level_three_ruins` | beard_thin | 3,801 / 3,000 / 801 / 0 (11 templates) | `meta/large_ruins` (anchor), 7 land fits | soft + trim |
| `philipsruins:start_nether_ruin` | beard_thin | 1,803 / 1,291 / 512 / 0 | `plan/move_to_nether` | soft + trim |
| villages: 5 vanilla, 24 T&T, 2 Terralith fortified | beard_thin | per scan section 3 | `earth_fill/prune` (`#type/village`) | review |

- Carp pond template (read from the jar, `aquaculture/aquaculture_carp_spawn`, 20x14x20): y 0-3 a dirt bowl (177 to 291
  dirt per layer), y 4 the grass rim and water. Start -4 puts y 4 at the surface: the same layout as the Campsite camps.
  With `none` it gets no beard, and earth fill counts every earth block (Phase 7: `large`, cancelled once on 209 air;
  water fill 103-144 into ground and 26-67 into air on slopes, 07_phase_7.md 2.4).

### Build
1. **Re-scan** on the instance machine (the one with `mods/`): `/dumpall` or `/dumptags` for a fresh tag export, then
   `python3 tools/terrain_adaptation_scan.py --fresh`. It rewrites terrain_adaptation_scan.md and the JSON. Check the
   new rows for the eugene, Campsite, Subterrestrial and Underground Bunkers structures (the undead castle reads as a
   walled compound: 1,091 built of 1,935 columns, see the earth-fill brief #4).
2. **Fish ponds and fisherman house** (pilot, small and common). In `terrain_adaptation.js` add a batch-4 list:
   ```js
   // Batch 4 (2026-10-2x, looked at in game): ponds. Water is a built block for the trim, so the pond stays whole;
   // the pad around it goes. The carp pond is sunk (start -4, grass rim at y 4) like the camps.
   var ponds = ['farmers_structures:aquacultures_common', 'old_swamp_fisherman_house:fisherman_house']
   global.tags.add(e, 'creativeengineering:terrain_adaptation/soft', ponds.concat(['farmers_structures:aquacultures_carp']))
   global.tags.add(e, 'creativeengineering:template_trim', ponds.concat(['farmers_structures:aquacultures_carp']))
   global.tags.add(e, 'creativeengineering:terrain_adaptation/sunk_start', ['farmers_structures:aquacultures_carp'])
   ```
   (`sinkOf` gives 4 for the carp pond: constant -4, projected.) Check with `/place structure` on a slope before and
   after: the ponds must not spill (water fill keeps judging them: the default override
   `#creativeengineering:water_fill/ponds=-1,16` only lifts the ground limit).
3. **Spire:** `template_trim` only (keep `none`). Place one with `/place structure terralith:spire` on a hillside in an
   ice biome before and after: the hills round it should no longer be cut by the 94x98 `layer1` air box. Watch for snow:
   `snow_block` is pad and snow layers are plants, so snow on the spire's ledges that sits in a column with no ice /
   stone of its own template (stacked on another template's stone) would be dropped (trim is per template). If that
   leaves bare stone patches, drop the idea or add a `template_trim/keep` exception (needs a new block tag and a check
   in `TemplateTrim.kind`).
4. **Philip's level-three ruins:** soft + trim, after placing two variants (`/ceplace structures philipsruins:level_three_ruins`
   on a flat world shows all 11). Ground matching already reskins their grass pads (3.8); trim removes pad columns
   instead. It is a `meta/large_ruins` anchor: meta parts beard and trim as their own structure
   (`TerrainAdaptation.rigid` 397-401, `TemplateTrim.apply` 152), so the cluster follows.
5. **Leave out** `philipsruins:start_nether_ruin` until the Nether move (`structures/contents #1`); in the Nether the
   terrain and beard look differ.
6. **Villages:** not as a batch. If wanted, one T&T family at a time (start with the most unbuilt: `village_forest` 63%,
   `village_beach` 59%, `village_mushroom_fields` 36%), soft + trim on the whole structure (tags apply per structure,
   so every house in it). Compare a village before/after with `/ceplace structures` on a flat world and once on hills.
7. Update the scan's "Choosing tags" note and structure_tuning.md 3.15 "Tagged now" (count and list) and the batches
   paragraph ("batch 4 is open").

### Ids and data
All ids above are structure ids as in `/dumpstructuregen`; JSON fields verified in `FarmersStructures-1.0.0-1.19.2.jar`,
`old_swamp_fisherman_house-1.0.2-forge-1.19.2.jar`, `Terralith_1.19.3_v2.3.12.jar`, `PhilipsRuins1.19.2-10.8.jar`
(scratchpad jars). Block tags involved: `creativeengineering:template_trim/pad`, `template_trim/plants` (built in
`kubejs/server_scripts/tags/blocktags.js`).

### Decisions
- **Which:** ponds + fisherman house (recommended, clear win, common), spire trim (recommended after the snow check),
  level-three ruins (recommended), start_nether_ruin (no, wait for the move), villages (not now; pilot one T&T family
  if the user wants).
- **Carp pond sunk_start:** recommended; it fixes the slope spill the same way the camp plinth fix did.

### Gotchas
- Trimmed structures change earth-fill counts: only earth still placed counts (`EarthFill.localEarth` with `trimmed`,
  `EarthFill.java:346-353`), so a structure can drop a size class and get tighter limits (the same as pruning).
  Check the `[earth-fill]` lines after tagging.
- Villages are in `earth_fill/prune`; with trim, pruning judges pieces by placed earth only, so fewer houses get dropped.
- Soft beard + `sunk_start` is fine together: `rigid()` adds the sink before building the `SoftBox`
  (`TerrainAdaptation.java:404-406`).
- The soft footprint reads the raw template, first palette; the ponds' crates come from a processor
  (`farmers_structures:random_crates`) and are not seen. Harmless.
- New chunks only; rejoin after changing tags.

### Tests
- ST-215 Ponds on a slope [scratch]: `/place structure farmers_structures:aquacultures_common` and `_carp` on a hillside:
  no square pad, no air notch, the water stays in the pond, the rim flush with the slope (carp: no 4-block dip or
  plinth).
- ST-216 Spire in the hills [scratch]: a placed spire leaves the surrounding hills uncut; its snow cover is whole.
- ST-217 Level-three ruins [scratch]: two variants on a slope, no grass slab around them; a `meta/large_ruins` cluster
  with one as anchor looks the same.
- ST-218 Load line [scratch]: `[terrain-adaptation] world load:` counts go up by the tagged number, `7 sunk starts`.

---

## structures/terrain #2: Beardifier fill estimate
Status Planned · Work tool · TODO `worldgen/structures/terrain #2`

### Goal
Earth fill only sees a structure's own earth blocks (dirt, sand, gravel, clay, path, plus the opt-in stone base). For a
terrain-adapted structure whose bottom is mostly built blocks (stone, planks, bricks), the beard builds the mound under
it out of terrain, which earth fill never counts: a tower on a cliff edge can stand on a 15-block earth pillar and log
"0 earth". Measure how often that happens, and if it matters, count the beard's own fill.

### Now
- Counting: `EarthFill.countEarth` (`EarthFill.java:219-241`) walks earth blocks only; mound columns are counted only
  for columns that hold earth (`EarthFill.java:236`). A piece with no earth in its bottom `baseLayers` is skipped
  (`EarthCheck.cancels`, 719).
- Workaround in place: the opt-in `earth_fill/stone_base` structure tag (`earth_fill.js:33-35`, drum tower only; row
  `structures/earthfill #5`).
- Tool: `tools/earth_fill_sim.sh` -> `tools/worldgen_test/.../EarthFillSim.java` (278 lines). It generates vanilla
  structures on vanilla terrain and already beards them with vanilla's real `Beardifier` in its `beard` mode
  (`beardReach`, lines 188-250). It needs the gradle caches (`~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.19.2`)
  and the ATLauncher libraries, so it runs on the dev machine, not in a cloud session.

### Build
1. **Measure (tool):** add a `footprint` mode to `EarthFillSim`: for every bearded rigid piece of each simulated start,
   take its footprint columns (box columns; for soft structures `TerrainAdaptation.templateField` /
   `TemplateTrim.Columns.footprint(trimmed)`), and for each column read the noise ground under the piece's bottom
   (`Terrain.openBelow(x, bottom - 1, z, 64)`). Report per structure: columns with a gap over `beardReach` (6) and over
   `maxMoundHeight` (8) that hold no template earth, i.e. what earth fill misses, next to what it counts today.
   Run it on the vanilla structures the sim knows (5 villages, outpost, igloo); modded structures can't run there, so
   add a scratch-server variant: a temporary `log = ALL` line field "beard columns" (step 2 behind a switch) and read it
   in a Chunky run.
2. **Count it (patcherMod), only if step 1 shows missed mounds on more than a few % of starts:** in `EarthCheck.cancels`
   also collect each bearded piece's footprint columns (not just earth columns) and count a column toward `mound` when
   its ground lies more than `maxMoundHeight` below the piece bottom (+ sink). Gate it with a new
   `[worldgen.earth_fill] beardFootprint` (default false until calibrated) and give pieces without earth a size from
   their footprint area (or keep them `small`). Then the stone-base tag can stay as is (it adds the base to the size).
3. Calibrate the new count with a `log = ALL` scratch run before turning the switch on.

### Ids and data
Config: new `beardFootprint` (boolean) in `CEConfig.EarthFill` (class at `CEConfig.java:1929`) and
`WorldgenConfig.earthBeardFootprint()`. No ids.

### Decisions
- Whether step 2 is needed: decide on step 1's numbers. Recommended default: build step 1 only.
- Mound only (recommended) or also air over the gap: air is about earth blocks hanging, which a built bottom doesn't
  have; the visible problem is the pillar, i.e. mound columns.

### Gotchas
- YUNG's structures answer their own adaptation; their pieces are judged by what they answer
  (structure_tuning 3.13 limits).
- Jigsaw junction beards (vanilla's small blend where pieces join) are not in the footprint; they are short.
- Counting more mounds lowers rates on slopes; every rate solved by `gen_structure_groups.py` ignores earth fill, so
  watch structures that are already rare (undead castle, drum tower).

### Tests
- Offline: a `footprint` sim run printed per structure (no pass/fail; numbers go into 3.13).
- ST-219 Beard pillar caught [scratch] (if step 2): with `beardFootprint = true`, a stone tower on a cliff edge logs
  mound columns and is cancelled; the same tower on flat ground is kept.

---

## structures/terrain #3: Skip the bottom 64-96 blocks
Status Planned (should be Testing) · Work patcherMod · TODO `worldgen/structures/terrain #3`

### Goal
Cut the cost of generating the bottom of the world.

### Now
**Already built and shipped.** The investigation (locate.md 3.7) chose option A, "skip the band in Distant Horizons'
own generation", clip below y 48 with a fake seabed by biome, plus option B, pregenerate the final server world; option
C (a real deferred band for real chunks) was "not built unless A + B are not enough". A is built:
- `worldgen/DistantClip.java` (159 lines), mixins `NoiseBasedChunkGeneratorDistantClipMixin`,
  `NoiseChunkDistantClipMixin`, `ConfiguredFeatureDistantClipMixin`.
- Config `[worldgen.distant]` in `config/creativeengineering-common.toml:1586-1605`: `clip = true`, `clipBelowY = 48`,
  `oceanFloorY = 48`, `deepOceanFloorY = 8`, `floorVariation = 4`, `floorScale = 24`, `skipFeatures = true`.
- It is in the 1.0.6 jar (LO-12 passed on "1.0.6 jar of 12:44", the jar deployed 2026-10-14) and every jar since.
- Measured (LO-10, 2026-10-19): noise 38.8%, features 24.4%, light 18.8% of chunk generation; the events carry no Y, so
  the band's share of each stage is still an estimate.
- Open: LO-11 [game] (fly over fresh terrain with DH generating: distant land unchanged, DH worldgen CPU lower);
  pregen of the final server world is a deploy step.

### Build
Nothing for A. If more is ever wanted for real chunks: option C is in locate.md 3.7 with its risk list; not recommended.
A lower-risk middle step, if real-chunk generation is still too slow after pregen: measure the band's share by Y with a
JFR of `NoiseChunk` / feature placement grouped by section (custom JFR event in `DistantClip`-style mixins), before
anything else.

### Decisions
- None left for the row. Recommended: set the TODO row to Testing (LO-11) and drop "Measure first" from its details.

### Tests
- LO-11 [game] (locate.md 7) is the remaining test.

---

## Built, needs play

None of this subfamily's rows is at Testing / Complete today; #3 should be (above).

## Writeup corrections made

- [locate.md](../locate.md) 0 "At a glance", row 3.7: "BUILT 2026-10-12 (option A)" -> also DEPLOYED with 1.0.6
  (2026-10-14), LO-11 [game] open.
- [structure_tuning.md](../structure_tuning.md) 7 Todo, "Chunk generation: leaving out the bottom 64 to 96 blocks":
  added that the DH clip (option A) is built and shipped (1.0.6) and LO-11 is what's left.
