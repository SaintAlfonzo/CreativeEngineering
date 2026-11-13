# Plan: Structure Catalogue world type

Owner request 2026-11-09 (scratchpad "all structures flatworld generation type"). **BUILT 2026-11-09, jar installed in `mods/`, scratch test queued** (`SQ-SCAT` / `CQ-SCAT`). Never run in the game yet: refusals, build time and meta structures are still unknown. Built notes in section 8.
Home: Creative Devtools, beside Flower Test ([creative_devtools.md](creative_devtools.md)). TODO row `tooling/generators-and-tests #13`.
Every structure and piece by family: [structure_catalogue_map.md](structure_catalogue_map.md).

Dev-only: no player changelog bullet, no new `mod_sides.md` row.

## 1. Goal

A flat world type that lays out every structure in the pack as a browsable catalogue, sorted by family. It builds itself on the first start of such a world.

Owner decisions (2026-11-09):

| Topic | Decision |
|---|---|
| Direction, mapped | Families run +X, variations run +Z |
| Direction, unmapped | Run -X, then +Z |
| Unit placed | Individual pieces for villages and sky villages, whole structures for everything else |
| Streets | Included, and pushed further into +Z than other pieces |
| Untagged structures | All go in the unmapped area, noted as needing a worldgen tag. None are hand-mapped |
| Height | Everything at ground level |
| Ocean structures | Dried out |

Assumptions to overturn at review: unmapped columns are one per mod, each mod running +Z (section 3). Assembled copies of whole villages are not placed, since the decision was pieces only.

## 2. Layout

Tree levels and the gap on either side of each boundary. A boundary uses the gap of the highest level that changes, not a sum.

| Level | Example | Axis | Gap |
|---|---|---|---|
| 1. Category | Village, Pillager, Ruins | +X | 50 |
| 2. Subfamily | Villager Houses, Villager Structures, Decor and Infrastructure | +X | 25 |
| 3. Function | Blacksmith, Shepherd, Leatherworker | +X | 10 |
| 4. Biome type | Plains, Spruce, Desert | +X | 5 |
| 5. Variation | `_1`, `_2`, `_3` | +Z | 5 |

```
 -X <--- unmapped                 spawn (0,0)              mapped ---> +X

 z<0   signs naming mods          |     signs naming each level
 z=8   [ mod B ] 5 [ mod A ]      |  [ Plains ] 5 [ Spruce ] 10 ...
          5           5           |      5            5
       [ mod B ]   [ mod A ]      |  [ variation ] [ variation ]
          v +Z                    |      v +Z

 Mapped along +X:
 |<------- Village ------->| 50 |<- Pillager ->| 50 | Ruins ...
 |<- Houses ->| 25 |<- Structures ->| 25 |<- Decor and Infrastructure ->|
 |<- Armorer ->| 10 |<- Butcher ->| 10 | ...
```

- First mapped column starts at x = +16. First unmapped column ends at x = -16. Both start at z = 8.
- A column is as wide as its widest item, items are left-aligned, and gaps are measured between real bounding boxes.
- Levels with no value are skipped. Most whole structures have no function level.
- Order: categories, subfamilies and functions as listed in the rule file; biome columns and variations in natural sort order (`_2` before `_10`).
- **Streets:** no function split, so one column per biome type. Zombie streets share the column of their base biome. That is 25 columns, the longest with 41 pieces.
- Signs: `z < 0` is free, so signs naming levels 1 to 4 sit there, staggered by level. Each item gets the existing `/cdevplace` sign in the 5-block aisle in front of it.

Size estimate from template files: the Village category is about 7,500 blocks along +X, deepest column about 680 along +Z. Whole structures are not measurable without generating them.

## 3. Membership

| Set | Rule | Count (snapshot) |
|---|---|---|
| Village pieces | Template path holds `village/` or `villages/` (all namespaces) | 1,292 |
| Sky village pieces | Namespace `skyvillages`, under Sky | 36 |
| Whole structures | Registered structure with a `type/*` tag, except the two sets above | 278 |
| Unmapped | Registered structure with no `type/*` tag | 44 |
| Skipped: biome markers | `terralith:zpointer/*` (no blocks) | 133 |
| Skipped: entity pieces | Pieces holding only a villager or golem (1 x 2-3 x 1) | 46 |

- Pieces replace the whole form of the 32 non-meta `type/village` structures and `skyvillages:skyvillage`. If a `type/village` structure has no matching pieces, it is placed whole and reported.
- The 4 meta structures tagged `type/village` are whole structures, under Composite.
- Unmapped is decided only by `type/*`. A structure with just a `dungeon/*` tag is still unmapped. Fixing one means adding its `type/*` tag in `structure_tags.json`, and it moves into a family on the next new world.
- The 76 mapped structures with several `type/*` tags go to one home by the category order in section 4.

## 4. Family rules

These go in a new hand-edited rule file, `kubejs/generation_scripts/worldgen/structure_world.json`, read through the same path as `MassPlacer.TAGS`. It holds gaps, category order, the tables below and per-id overrides.

Category of a whole structure, first match wins:

| # | Test | Category | Subfamily |
|---|---|---|---|
| 1 | id under `meta/` | Composite | from its first type |
| 2 | `sky` | Sky | Balloons and temples |
| 3 | `end_*` | End | Constructed, Environmental |
| 4 | `nether_*` | Nether | Constructed, Environmental |
| 5 | `underwater_ruin`, `shipwreck`, or id holds `underwater` | Underwater | Ruins, Shipwrecks, Fossils |
| 6 | `underground`, `mineshaft` | Underground | Mineshafts, Cabins, Other |
| 7 | `enemy_building` | Pillager | Outposts, Towers, Camps, Ships, Siege and wagons, Witches and mansions |
| 8 | `ruined_portal` | Ruins | Portals |
| 9 | `ruin` | Ruins | Towers, Surface ruins |
| 10 | `ship` | Buildings | Ships |
| 11 | `tower`, `castle` | Buildings | Towers and castles |
| 12 | `camp` | Buildings | Camps |
| 13 | `house` | Buildings | Houses and workplaces, function from the name |
| 14 | `decoration` | Nature and Decor | Statues and markers |
| 15 | otherwise | Nature and Decor | Environmental |

Biome type of a whole structure: a biome word in its name, otherwise its first `land/*` tag, otherwise Any.

Village pieces, first match on the path:

| Subfamily | Function | Path or name holds |
|---|---|---|
| Decor and Infrastructure | Streets | `street`, `road`, `bridge`, `terminator`, `term<n>`, `crossroad`, `turn_`, `corner_`, `straight_`, `/path/` |
| Decor and Infrastructure | Animals | `/animals/`, `cows`, `sheep`, `pigs`, `chickens_` |
| Decor and Infrastructure | Lamps | `lamp`, `lantern`, `torch` |
| Decor and Infrastructure | Trees | `/trees/`, `tree_`, `sapling` |
| Decor and Infrastructure | Decor | `decor`, `deco_`, `accessory`, `/decays/`, `table` (not `stable`), `bench`, `fence`, `statue`, `sign`, `planter`, `plants_`, `waystone`, `wood_pile` |
| Villager Structures | Town centres | `town_cent`, `center`, `meeting`, `fountain`, `well`, `plaza`, `square`, `base_plate`, `start_castle` |
| Villager Houses | Armorer to Weaponsmith (profession words) | `armorer`, `butcher`, `cartograph`, `cleric`, `alchemist`, `farmer`, `fisher`, `fletcher`, `leather`, `tanner`, `librar`, `mason`, `shepherd`, `toolsmith`, `weaponsmith`, `blacksmith`, `forge`, `loom` |
| Villager Structures | Temples | `temple`, `church`, `chapel`, `shrine` |
| Villager Structures | Farms | `_farm`, `greenhouse`, `garden`, `orchard`, `vinery`, `winery` |
| Villager Structures | Animal pens | `animal_pen`, `pen_`, `stable`, `coop`, `barn` |
| Villager Structures | Gates and towers | `gate`, `wall`, `fort`, `tower`, `lighthouse` |
| Villager Structures | Workplaces | `scientist`, `workshop`, `brewery`, `bank`, `shop`, `market`, `_inn`, `mill`, `warehouse`, `depot`, `trader_tent` |
| Villager Houses | Small, Medium, Large, Other homes | `small_`, `hut`, `cottage`; `medium`; `big`, `large`, `manor`; any other `house` |
| Villager Structures | Other | anything left (1 in the snapshot) |

Biome type of a piece: the style folder in its path, such as `plains`, `desert`, `flower_forest_japanese` or T&T's `exclusives/iberian`. Zombie variants are their own column (`Plains zombie`), except streets. Pieces with no folder take a biome word in their name, otherwise Any.

**Drying.** Applies to Underwater, Buildings > Ships, and any whole structure whose id holds `ocean`, `underwater`, `sunken`, `wreck` or `monument`, unmapped ones included (33 in the snapshot). After placement, inside the bounding box: water, bubble columns, kelp and seagrass become air; `waterlogged` becomes false. Wells, fountains and ponds in other families keep their water.

## 5. Build steps

1. **Preset.** `data/creativedevtools/worldgen/world_preset/structure_catalogue.json`, flat, plains, no features, grass top at Y = 64 (bedrock 1, stone 125, dirt 2, grass 1). Add to the `minecraft:normal` preset tag and add the lang key `generator.creativedevtools.structure_catalogue`.
   Y = 64 rather than Flower Test's -60: some structures refuse to start below a height, for example End cities need 60 or more.
2. **Rule file and resolver.** `structure_world.json` plus a pure Java class turning ids and tags into tree paths. No game classes, so it runs in the plain `javac` harness.
3. **Layout engine.** Pure Java: tree plus item sizes to coordinates, including gap rules, level skipping, the street rule and the mirrored unmapped side.
4. **Placer.** New job in `cdevplace/`, reusing `MassPlacer`'s tick budget, chunk tickets, `templateIds`, `packTags` and signs. Works one column at a time: measure every item (template size, or `Structure.generate` for whole ones), set the column width, place, release. Nothing is held for the whole world, given the 6 GB scratch heap (`pack/server #12`). Always uses ground alignment, whatever `verticalAlign` says.
5. **Drying pass.** One more per-chunk step after placement, inside the tick budget.
6. **World start.** On server start, detect the flat stack of this preset, check a saved "built" marker, set spawn at (0, 65, 0), set `doMobSpawning`, `doFireTick` and `doWeatherCycle` false, start the job. Switch: `[features] structureCatalogue`, as for `flowerWorld`.
7. **Commands.** `/cdevplace catalogue build | preview | status | stop`. Preview prints counts and the tree without placing.
8. **Report.** `kubejs/exported/structure_catalogue_report.json`: the unmapped list marked "needs type tag", refusals with reasons, skipped items and category extents. A one-line summary goes to chat.

## 6. Risks

- **Refusals on flat ground.** `generate` can return nothing, as the Nether fossil probably will (it looks for air inside the ground). Each gets a failure sign and a report line. Fix per structure through an override in the rule file, such as placing its main template directly.
- **Meta structures.** Whether all 34 tagged ones generate through `Structure.generate` is unproven until the first run.
- **Raw pieces differ slightly from real villages.** Pool processors do not run on individual templates, so zombie decay and street block swaps will not show.
- **Water returning.** Check that the dried areas stay dry after chunks first tick, since block post-processing on a chunk's first tick is known to bring water back in real worldgen.
- **Build time.** Unknown until run. The tick budget protects the watchdog, but the strip is long.

## 7. Tests

**Run r1111 (2026-11-12):** SQ-SCAT-06, -07, -08 PASS: plan counts match the preview, build 281 s, 1619 of 1651 placed, 32 refused (27 creativeworldgen meta structures, monument, ocean village, witch village, way sign, small nether dungeon: all "did not generate on flat ground"), no watchdog. Client rows CQ-SCAT not run. Verdicts `perf/runs/r1111/verdicts/scat.md`.

Logic, in the plain Java harness:

- [x] **SCAT-01** (2026-11-09, `tools/test_worldgen_tools.sh` 2b): resolver gives the section 4 home for one id per rule, and multi-type ids land by priority.
- [x] **SCAT-02** (2026-11-09): an id with no `type/*` tag is unmapped; a `zpointer` id is skipped.
- [x] **SCAT-03** (2026-11-09): layout gaps of 50, 25, 10, 5 and 5 at the right boundaries, with no overlapping boxes on a random tree.
- [x] **SCAT-04** (2026-11-09): skipped levels and the street rule (no function split, zombie merged).

Live, queued in [scratch_queue.md](testing/scratch_queue.md) as `SQ-SCAT` / `CQ-SCAT`:

- [ ] **SCAT-05:** the world type is listed on a dev client and absent without Devtools.
- [ ] **SCAT-06:** a new world builds once. A restart does not build it again.
- [ ] **SCAT-07:** preview counts match the live registry and the report.
- [ ] **SCAT-08:** full build finishes without a watchdog kill or out-of-memory error. Record time and refusal count.
- [ ] **SCAT-09:** in game, walk the mapped +X strip and confirm gaps and order. Confirm every family has signs.
- [ ] **SCAT-10:** in game, the unmapped area runs -X then +Z, and each item is listed as needing a tag.
- [ ] **SCAT-11:** in game, a monument, a shipwreck and an ocean ruin have no water after 5 minutes. A village well still has water.

## 8. Built notes (2026-11-09)

Files: `cdevplace/CatalogueRules` (rules, no game classes), `cdevplace/CatalogueLayout` (coordinates, no game classes), `cdevplace/StructureCatalogue` (job, commands, world start, report), all in Creative Devtools; rules in `kubejs/generation_scripts/worldgen/structure_world.json` (dev only in `build_deploy.py`). `MassPlacer`'s tag resolver moved into `CatalogueRules` so the test can run it; `/cdevplace` behaves the same.

The resolver was compared item by item with the script that wrote [structure_catalogue_map.md](structure_catalogue_map.md): all 1,650 items in both land in the same place. Differences from the snapshot counts, both following the plan's rules:

- **`towns_and_towers:village_ocean` is placed whole** (279 whole structures, not 278): its pieces are under `kaisyn:ships/`, not `village/`, so the "no matching pieces" rule applies. It is listed in the report under `placedWholeNoPieces`, and dried.
- **35 structures are dried, not 33:** that village, and `terralith:underground/sunken_tower`, which the map's script dropped because a template has the same id.

Choices made while building:

- **Streets "further into +Z"** is section 2's form: no function split, so long columns of up to 41 pieces. Street columns start at z = 8 like every other column.
- **Drying runs in the same step as placing a chunk**, not a later pass, so water never gets a tick to flow out of the box. It also clears any non-waterloggable block holding water (modded water plants), and "Ships" covers every subfamily of that name (as the map does), not only Buildings.
- `/cdevplace catalogue stop` marks the world as handled so a restart does not start over; a server that dies mid-build rebuilds on the next start. `/cdevplace catalogue build` always starts over.
- A failed item takes a 1 x 3 footprint with a failure sign. No overrides are set yet: the first run's report says which structures need one.
