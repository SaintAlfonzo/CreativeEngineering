# Bee nests

Where wild bee nests generate, how often, and a tuning file that gives nests to biomes without them and raises
(or lowers) the rate where they exist.

## 0. At a glance

| Part | Status |
|---|---|
| Investigation: every nest source per biome (section 2, [bee_nests_by_biome.md](bee_nests_by_biome.md)) | DONE 2026-10-10 |
| `tools/gen_bee_nests.py` -> [bee_nests_by_biome.md](bee_nests_by_biome.md) (offline before/after + measured table) | BUILT 2026-10-10 |
| Nest tuning: `bee_nests.json` -> `worldgen/bee_nests.js` -> biome modifier `creativeengineering:bee_nests` (patcherMod `worldgen/BeeNests` + `TreeFeatureBeeNestsMixin`, `BeehiveDecoratorBeeNestsMixin`) | BUILT 2026-10-10, offline tests pass, scratch-server verified with the starter rules (4,301 chunks, no crash after the leaf-only fix); in patcherMod 1.0.5 (in `mods/`), DEPLOYED with pack 1.0.6 (2026-10-14); single player 2026-10-11 applied `6 rules, 44 biomes tuned` |
| Extra bee-nest trees (`trees` in a rule: pack placed feature + `forge:add_features`) | BUILT 2026-10-10, offline tests pass |
| `/dumpbeenests [reset]` (trees, nests, chunks counted per biome as chunks generate; `ChunkGeneratorBeeNestsMixin`) | BUILT 2026-10-10; baseline 41,684 chunks / 73 biomes saved as `bee_nests_baseline.json` |
| CEConfig `[worldgen.systems] beeNests` | BUILT, same jar |
| Starter rules (section 9) | IN `bee_nests.json`, calibrated on the baseline; DEPLOYED as they are with pack 1.0.6 (2026-10-14); your review still open |

- **Jar:** patcherMod's `worldgen/BeeNests` (since 1.0.5, first shipped in pack 1.0.6). The scripts load it directly; the
  older-jar fallback was removed 2026-10-27 ([pack_hygiene.md](pack_hygiene.md) finding 4).
- **Knobs:** `kubejs/generation_scripts/worldgen/bee_nests.json` (section 3); CEConfig `[worldgen.systems] beeNests`
  (off = nests as the mods ship them). Rejoin to apply; only chunks generated afterwards change.
- **Preview without the game:** edit the rules, `python3 tools/gen_bee_nests.py` (or `/cegen run bee_nests`).
- **Measure in game:** `/dumpbeenests reset`, generate new terrain (fly, or `chunky` in an unexplored area),
  `/dumpbeenests`, then rerun the generator: section 2 of the report is the real count.
- **Offline checks:** `bash tools/test_bee_nests.sh` (section 6).
- **Tests:** BN-01 to BN-10 (section 7).

## 1. Overview

Bees have no natural mob spawn in any biome (`/dumpspawns`: `minecraft:bee` spawns in 0 biomes). Every wild bee
comes from a bee nest placed by the `minecraft:beehive` tree decorator, with 2 or 3 bees inside. So "where do
bees spawn" is "which trees carry that decorator, and how many of those trees grow per chunk".

In this pack that is decided by four mods' JSON: Terralith (its own trees plus overrides of vanilla's bee trees),
Geophilic and Immersive Weathering (more overrides of vanilla's bee trees) and Vinery (a bee apple tree that no
feature places). Vanilla's own Java trees only matter for Meadow (`minecraft:trees_meadow`). Terralith rebuilt most
biomes' trees without the decorator: Birch Forest, Dark Forest and every taiga, jungle and savanna have no nests.

The tuning works on trees as they generate, per biome, because a tree feature is shared by many biomes: raising
the nest chance of `oak_bees_0002` in one biome would raise it in all of them.

## 2. Reference

### 2.1 How a nest generates (1.19.2)

- A tree feature (`minecraft:tree`) lists decorators; `{"type": "minecraft:beehive", "probability": p}` is the nest.
- Each tree rolls `p` once. On success the decorator takes the height one below the lowest leaf (at least one
  above the first log), looks at the four sides of the trunk at that height (not north), shuffles them and takes the
  first spot that is air with air on its south side. No spot: no nest. The nest faces south and gets 2-3 bees.
- So bushes (a one-log trunk) never get a nest, and dense foliage hugging the trunk lowers the real rate a little.
- The same decorator runs when a player grows a sapling (`AbstractTreeGrower`: oak and birch saplings use the
  5 % bee variants when flowers are within 2 blocks). The pack's tuning leaves saplings alone.

### 2.2 Where nests generate before tuning

Offline estimate from the jars (full table: [bee_nests_by_biome.md](bee_nests_by_biome.md) section 1). "1 in N" =
one nest per N chunks of that biome.

| Biome | Nests | Source |
|---|---|---|
| Old Growth Birch Forest | 1 in 6 | Terralith `birch/birch_tall` (tall birches, p 0.005-0.05) |
| Flower Forest | 1 in 8 | vanilla `forest_flower_trees` (oak/birch/fancy oak, p 0.02) |
| Lavender Forest, Lavender Valley | 1 in 11 | Terralith lavender trees (p 0.01) |
| Sakura Grove, Sakura Valley | 1 in 12 | Terralith cherry trees (p 0.05) and birches |
| Blooming Valley | 1 in 21 | Terralith flower-forest trees (p 0.003-0.01) |
| Forest | 1 in 50 | vanilla `birch_other` (p 0.002) |
| Meadow, Blooming Plateau | 1 in 100 | vanilla `trees_meadow`: one tree per 100 chunks, every one with a nest |
| Skylands Spring | 1 in 100 | Terralith cherry birches |
| Plains, Sunflower Plains, the four Shrublands | 1 in 400 | vanilla `plain_vegetation` (one tree per 20 chunks, p 0.05) |
| Underground Jungle (cave) | 1 in 15 | Terralith cave jungle trees |
| Every other biome (about 130) | none | |

Area-weighted over the overworld's land: about one nest per 400 chunks.

Vinery ships `vinery:apple_bee` (a nest on every tree), but its wild apple trees (`vinery:tree_apple`, placed in
`oak forest` biomes by wild_crops.json) use the bee-less `vinery:apple`; no feature places `apple_bee`. It can be
used in a `trees` rule.

### 2.3 Why the offline tree counts are only an upper bound

Tree placements are counted as attempts. Terralith uses large counts with filters (Yellowstone tries 200
birches per chunk between Y 107 and 160 and keeps the few that survive), and the generator doesn't simulate
filters, ground or the biome check. Nest chances per tree are exact. That is why the tuning file states a chance
per tree and section 2 of the report measures the real trees and nests per chunk.

## 3. What the pack changes

### 3.1 Rules (`bee_nests.json`)

```json
{ "id": "temperate_woods", "where": ["temperate forest !flower"], "chance": 0.004 }
{ "id": "flower_boost", "where": ["flower"], "scale": 2 }
{ "id": "meadow_trees", "where": ["flower plains"], "trees": { "feature": "minecraft:fancy_oak_bees", "rate": "1/12" } }
```

- **Selector:** `where` clauses OR'ed, traits in a clause AND'ed, `!trait` negated, traits from wild_crops.json's
  biome table (same as wild crops and mob spawns). Biomes marked `ocean` or `cave` never match a clause; `add`
  (biome ids or `#tags`) and `except` (ids) are applied after.
- **`chance`** (0-1): every tree generated in the biome has at least this chance of a nest. A tree without a beehive
  decorator gets one; trees without logs or without leaves (fallen logs, dead trunks, Terralith's leaf-only
  bushes) are skipped.
- **`scale`** (0-1000): the tree's own beehive chances times this. `scale: 0` removes nests (unless `chance` > 0).
  The result is `min(1, max(p * scale, chance))` per tree.
- **Order:** rules fold in file order per biome, and each rule sets only the fields it names. A later rule
  overrides an earlier one's `chance` or `scale` for the biomes they share.
- **`trees`**: extra trees that carry nests, for biomes with too few trees for a per-tree chance to matter
  (Meadow has one tree per 100 chunks). `feature` = a configured tree feature, `rate` = trees per chunk
  (`"1/12"` = one tree in 12 chunks, or a number), `sapling` (default `minecraft:oak_sapling`) = the
  survival check (dry ground only, like vanilla trees). Trees rules add up; they don't fold. Bee-carrying
  configured trees in the pack: `minecraft:fancy_oak_bees` and `minecraft:super_birch_bees` (p 1, Terralith /
  Immersive Weathering JSON), `vinery:apple_bee` (p 1).

### 3.2 How it is applied

- `bee_nests.js` writes the biome tag `creativeengineering:bee_nests/<id>` per rule (via `global.tags`), ONE
  biome modifier `creativeengineering:forge/biome_modifier/bee_nests.json` of type `creativeengineering:bee_nests`
  with the chance/scale rules in file order, and for each `trees` rule a placed feature
  `creativeengineering:bee_nests/<id>` plus a `forge:add_features` modifier (step `vegetal_decoration`).
- patcherMod `BeeNests` folds the rules per biome when Forge runs the modifier (phase AFTER_EVERYTHING).
- `TreeFeatureBeeNestsMixin`: at `TreeFeature.place` HEAD the biome at the tree's origin is looked up (worldgen
  only: the level must be a `WorldGenRegion`); both reads of `config.decorators` return the tuned list.
- `BeehiveDecoratorBeeNestsMixin` exposes the decorator's private probability and counts a nest when one is placed;
  `ChunkGeneratorBeeNestsMixin` counts chunks per biome (centre surface) for `/dumpbeenests`.

### 3.3 Calibration baseline

`kubejs/generation_scripts/worldgen/bee_nests_baseline.json` is a `/dumpbeenests` taken with NO rules (scratch
server, seed 424242, 49 squares of 441 chunks every 4000 blocks across +-12000, Distant Horizons removed). The
generator uses it for every biome with 50+ measured chunks: real trees per chunk and nest rate. Refresh it only
with the rules emptied (the generator refuses a baseline taken with rules).

## 4. Limits and decisions

- **Per tree, not per chunk.** A chance per tree keeps nests on the biome's own trees and in proportion to how
  wooded it is. Rejected: rewriting the shared tree features' decorators (changes every biome using them; clones per
  biome risk the feature-order cycle crash, see crops.md 2.8); adding nest-only features (vanilla has no "attach a
  nest to an existing tree" feature, and a free-floating nest looks wrong).
- **Treeless biomes stay nest-free** unless a `trees` rule adds trees. Deserts, badlands, peaks and oceans are
  left alone on purpose.
- **Saplings are untouched**, so boosted biomes don't become nest farms.
- **New chunks only.** Existing terrain keeps its trees.
- **Counters are per server session** (or since `/dumpbeenests reset`), by the biome at the tree's origin and at
  each chunk's centre surface: a few edge chunks land in a neighbour's row. Biomes with fewer than ~50 chunks in a
  dump are noisy.
- Distant Horizons generates its own LOD terrain on the server; the scratch-server measurement ran without it.

### Planned from the scratchpad (2026-10-24)

Brief: [briefs/mobs_bees.md](briefs/mobs_bees.md). Nothing here is built.

- [TODO](todo/TODO.md) `mobs/bees #3`: shearing a hive gives about 2.5 honeycombs on average (vanilla gives a fixed 3 from `BeehiveBlock.dropHoneycomb`,
  unverified in the jar), Looting adds more. Needs a patcherMod mixin and config values (base 2, +1 at 50%). Open: exact numbers, and
  what Looting means on shears (it only counts when something puts it on the shears). See `dishes/ingredients #22` for honeycomb as sugar.

## 5. File map

- [kubejs/generation_scripts/worldgen/bee_nests.json](../kubejs/generation_scripts/worldgen/bee_nests.json): the rules
- [kubejs/server_scripts/worldgen/bee_nests.js](../kubejs/server_scripts/worldgen/bee_nests.js): tags, modifier, tree features, load check
- [kubejs/server_scripts/_lib/dump_bee_nests.js](../kubejs/server_scripts/_lib/dump_bee_nests.js): `/dumpbeenests [reset]`
- [development/creativeCommon/.../worldgen/BeeNests.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/worldgen/BeeNests.java): modifier type, rule fold, decorator swap, counters, dump writer
- [TreeFeatureBeeNestsMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/TreeFeatureBeeNestsMixin.java),
  [BeehiveDecoratorBeeNestsMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/BeehiveDecoratorBeeNestsMixin.java),
  [ChunkGeneratorBeeNestsMixin.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/ChunkGeneratorBeeNestsMixin.java)
- [tools/gen_bee_nests.py](../tools/gen_bee_nests.py) -> [bee_nests_by_biome.md](bee_nests_by_biome.md); `/cegen run bee_nests`
- [tools/test_bee_nests.sh](../tools/test_bee_nests.sh), [tools/bee_nests_test/](../tools/bee_nests_test/)
- CEConfig `[worldgen.systems] beeNests` (`CEConfig.java`, `WorldgenConfig.beeNestsEnabled()`)

## 6. Offline checks

`bash tools/test_bee_nests.sh`:

1. the generator runs on the real config with no problems;
2. the real script in the pack's Rhino: every rule tag equals the generator's biome list, the modifier holds the
   chance/scale rules in file order, every `trees` rule has its placed feature (rate tier and sapling as the
   generator computes them) and add modifier;
3. the same on a fixture with every rule shape, and each of 7 bad rules is reported and skipped;
4. CEConfig switch off: nothing written;
5. (retired 2026-10-27: the old-jar fallback is gone)
6. a mod missing (Vinery): its tree rule writes nothing, the rest unchanged.

## 7. In-game tests

Install the jar first (section 0). Use a new world or unexplored terrain.

- [ ] **BN-01 load:** rejoin -> log has `[bee-nests] N chance/scale rules ..., M bee-tree features` and, at world
  load, `[bee-nests] P rule/biome pairs checked; R rules, B biomes tuned` with no `problems`.
- [ ] **BN-02 counters:** `/dumpbeenests reset`, fly through new terrain for a minute, `/dumpbeenests` -> chat line
  with non-zero chunks/trees; `kubejs/exported/bee_nests.json` lists the biomes you flew over.
- [ ] **BN-03 missing biome gets nests:** in a tuned biome that had none (Birch Forest, Dark Forest, Taiga) ->
  nests on its own trees, under the lowest leaves, facing south, 2-3 bees inside.
- [ ] **BN-04 rate:** after generating ~2000 chunks (`chunky radius 400` somewhere new), `python3 tools/gen_bee_nests.py`
  -> section 2 "nests: 1 in N" per biome is close to section 1's "after" where trees/chunk agree.
- [ ] **BN-05 bee trees:** Meadow -> the odd fancy oak or tall birch with a nest, on grass, never in water.
- [ ] **BN-06 no bushes / logs:** no nests on bushes, fallen logs or on the ground beside a log; a Birch Forest
  (Terralith leaf-only bushes) generates without a crash.
- [ ] **BN-07 untouched biomes:** a biome no rule names (Desert, Jagged Peaks) -> no nests; `/dumpbeenests` shows 0.
- [ ] **BN-08 saplings:** grow oak saplings in a boosted biome without flowers nearby -> no nests (vanilla).
- [ ] **BN-09 switch off:** CEConfig `[worldgen.systems] beeNests = false`, rejoin -> log says off; new chunks get
  the mods' nests only (`/dumpbeenests` nests per tree back to the "before" rates).
- [ ] **BN-10 existing world:** rejoin an old world -> nothing in explored terrain changes.

## 8. History

- 2026-10-10: user tuning: Blooming Valley/Plateau, Skylands Spring and Sunflower Plains raised to ~1 nest in 10
  chunks (bloom_woods, bloom_bee_trees); jungles taken out of warm_woods (no nests in any surface jungle).
- 2026-10-10: scratch-server run with the starter rules crashed worldgen: Terralith places leaf-only "trees"
  (`terralith:birch/trees_forest_negative`) and vanilla's beehive code reads the first log unguarded. The added
  decorator now needs logs and leaves; rebuilt and re-run.
- 2026-10-10: investigated (no natural bee spawns; nests only from tree decorators; four mods' JSON; Terralith
  removed nests from most forests), built the tuning (rules, Java tree hook, extra trees, counters + dump,
  generator, offline tests), measured a scratch-server baseline.
- 2026-10-11: ships in pack 1.0.5 (staged, not deployed) with the starter rules: patcherMod 1.0.5, `bee_nests.json`,
  `bee_nests.js`; `bee_nests_baseline.json` stays home (dev-only in `KUBEJS_CLASSES`). Single player with the staged
  files: `6 rules, 44 biomes tuned`, 2 bee-tree features, no errors. Only new chunks change. BN-01 to BN-10 pending.
- 2026-10-14: pack 1.0.5 was never deployed; everything above that was staged for it ships in pack **1.0.6** (STAGED
  2026-10-14, update from 1.0.4, [plan_1.0.6.md](plan_1.0.6.md)), unchanged.
- 2026-10-24: planned from the scratchpad: honeycomb shearing yield (`mobs/bees #3`). No code changed.

## 9. Starter rules

In `bee_nests.json` (2026-10-10), chosen from measured trees per chunk. Whole overworld land: about 1 nest per 785
chunks -> 1 per 170; biomes with nests 17 -> 47 of 126 (after the 2026-10-10 bloom/jungle changes). Full table: [bee_nests_by_biome.md](bee_nests_by_biome.md).

| Rule | Selector | Setting | Effect (calibrated estimate) |
|---|---|---|---|
| temperate_woods | `temperate forest` | chance 0.003 | Birch Forest none -> 1 in 23, Dark Forest none -> 1 in 33, Forest 1 in 100 -> 1 in 56 |
| warm_woods | `warm savanna`, `warm swamp flower` | chance 0.003 | Brushland 1 in 42, Savanna 1 in 160; jungles left out on purpose (no nests, as before) |
| cold_woods | `cold forest !snowy` | chance 0.001 | Taiga 1 in 230, Shield 1 in 260, Yellowstone 1 in 580; snowy biomes stay nest-free |
| grassland | `temperate grassy plains !dry` | chance 0.02 | Plains 1 in 985 -> 1 in 183 |
| flower_boost | `flower` | scale 2 | Flower Forest 1 in 8 -> 1 in 4, lavender/sakura 1 in 11-12 -> 1 in 5 |
| bloom_woods | add Blooming Valley, Skylands Spring | chance 0.02 | Blooming Valley 1 in 159 -> 1 in 10, Skylands Spring 1 in 100 -> 1 in 11 |
| meadow_bee_trees | `temperate plains flower` except Blooming Plateau, Sunflower Plains | trees `minecraft:fancy_oak_bees` 1/16 | Meadow 1 in 100 -> 1 in 15 |
| bloom_bee_trees | add Blooming Plateau, Sunflower Plains | trees `minecraft:fancy_oak_bees` 1/10 | Blooming Plateau 1 in 130 -> 1 in 9, Sunflower Plains 1 in 400 -> 1 in 10 |

Target for the showcase bee biomes (bloom_woods, bloom_bee_trees; user, 2026-10-10): about as rich as Sakura
(1 in 5) and Old Growth Birch (1 in 16 measured), i.e. ~1 nest in 10 chunks.

Left alone: jungles (user, 2026-10-10: no bees in jungles), deserts, badlands, peaks, beaches, oceans, snowy and dry-cold biomes, swamps other than Orchid Swamp.

Small check with these rules (scratch server, 9 squares, 4,301 chunks, 2026-10-10): Meadow 4 nests in 72 chunks
(1 in 18; baseline 0 in 236), Forest 2 in 246 (1 in 123; estimate 1 in 56, within noise at these counts). The
jungle, savanna and birch/dark forest rules were not in that area; BN-04 covers them.
