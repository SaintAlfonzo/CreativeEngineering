# Sky Village addon houses (Farmer's Delight, Vinery, Brewery)

Status: BUILT 2026-10-30 (templates, pools, swap table; data only, `/reload` is not enough for new worldgen pieces: restart). Scratch server load and placement PASSED r1031 (2026-10-31); placement in villages, loot and in-game looks open (section 4). TODO row `skyvillages #1`. Test prefix `SVA-`.

Goal: the Sky Villages addon houses spawn in the pack's existing sky villages (`skyvillages:skyvillage`), built from
blocks the pack has, with no new structure, structure set or biome change.

## 1. What is there (read 2026-10-30)

- Sky Villages is a datapack in `kubejs/data/skyvillages/` (ported 2026-09-27 from
  `disabledmods/archive/SkyVillages-1.0.3-1.19.2-1.20.x-forge-release.jar`). Houses come from the pool
  `skyvillage_houses` (13 entries, weight 50 each, all with `skyvillages:house_processor`). `skyvillage_deco` is
  only the empty element (weight 5): the Supplementaries deco entries were dropped at the port.
- The base jar's own houses pool already listed `farmersdelight/sky_village_farmersdelight_farm_0`, `_2`, `_3`
  (dropped at the port, templates missing). The compat zip ships `farm_0`, `farm_1`, `farm_2`: use the zip's names.
- The three addon zips in `disabledmods/` hold templates only (no pools, no loot tables):

| Zip | Templates | DataVersion | Namespace in palette | In the pack? |
| --- | --- | --- | --- | --- |
| `SkyVillages-FarmersDelightCompat-1.20.1-1.0.3` | 3 houses (`farmersdelight/sky_village_farmersdelight_farm_0..2`) | 3120 (1.19.2) | `farmersdelight` (24 ids) | all 24 exist (CD member `repackedFarmers`) |
| `SkyVillages-LetsDoVinery-1.20.1-1.0.4` | 3 houses (`letsdo_vinery/sky_village_letsdo_vinery_0..2`) | 3465 (1.20.1) | `vinery` (32 ids) | 21 exist; 11 are 1.5.5 names the port registers under 1.3.10 ids (2.2) |
| `SkyVillages-LetsDoBrewery-1.20.1-1.0.4.1` | 2 houses + 9 deco pieces (2x2x2) | 3465 | `brewery` (31 ids) | **none**: Let's Do Brewery is not in the pack (Brewin' and Chewin' is a different mod) |

- Every house has the same jigsaws as the base houses (`skyvillages:house` to bridges, `villager_1` to the villager
  pool, and some `deco_part_0` to `skyvillage_deco`), so they drop into the existing pools without edits.
- Chests use existing tables only: `sky_village_weaponsmith` (FD farm_0, Vinery 1), `sky_village_fletcher`
  (FD farm_2), `sky_village_butcher` x2 and `sky_village_shepherd` (Brewery 1). No entities.
- 1.20.1 templates load in 1.19.2 (same format below DataVersion 3837); unknown blocks load as air, so every
  missing id must be remapped before shipping.

## 2. Decisions

### D1. Brewery houses: Let's Do Brewery is not in the pack (DECIDED 2026-10-30: A)

The "brewin" addon is Let's Do Brewery (`brewery:` ids: hops crops, brew oven / whistle / timer, barrels, beer mugs,
bench, table, bar counter, pork knuckle, dumplings). Nothing in the pack carries hops or barley.

- **A (recommended).** Remap every `brewery:` block to a pack block in `BLOCK_REMAP`, the same way the tool
  already maps Diagonal Fences or the decorated pot: brewing station / oven to `brewinandchewin:keg` and
  `heating_cask`, barrels to Vinery `fermentation_barrel` or vanilla barrels, table / bench / cabinet / drawer /
  sideboard to Vinery 1.3.10 furniture (`table`, `chair`, `cherry_cabinet`, `cherry_drawer`), carpet to a vanilla
  carpet, placed foods and drinks to placeable pack foods or air, hops crops to a pack crop on a trellis (Vinery
  grape bush on `grapevine_lattice`, or Corn Delight corn). This is a template remap, not a save conversion, but it
  is cross-mod: confirm it does not fall under the "no cross-mod conversions" rule. The exact table is written in
  step 2 and shown before the build.
- **B.** Ship Farmer's Delight and Vinery now; leave Brewery for a later Let's Do Brewery backport (ARR, the same
  donor route as Vinery; a separate port, not part of this plan).
- **C.** Skip Brewery for good.

### D2. Weights (DECIDED 2026-10-30: 20 per house)

User chose 20 per house: the 8 addon houses are 160 of 810 (about 20% of house picks). Brewery
deco: weight 1 each, as below. The recommendation was:

Adding 8 houses at the base weight 50 makes addon houses 8 of 21 (38%) of every pick. Recommended: weight 25 each
(FD 3 x 25, Vinery 3 x 25, Brewery 2 x 25 = 200 of 850, 24%), so a village of about 6 houses averages 1 to 2 addon
houses. Brewery deco pieces: weight 1 each next to the empty element (5), as the dropped Supplementaries ones were.
Brewery deco only attaches where a house has a `deco_part_0` jigsaw (FD farm_1, Vinery 0 and 1, Brewery 0 and 1).

### D3. Processors (recommended, no question)

New house entries use `skyvillages:house_processor` like the 13 base houses; deco entries use `minecraft:empty`.

## 3. Build

### 3.1 What was done (2026-10-30)

- `tools/port_structure_jars.py`: `attach` / `unsupported` rule keys (3.2; full blocks read from block models,
  `vinery_upstream/` art included), `--templates-only` (every template in an addon datapack, overwrites, never moves the
  source) and `--remap FILE` (JSON swap table: blocks with keep / set / rename properties and block entity strip, item
  renames inside block entities, `verified_blocks`). Namespaces now include jars nested in mods (Creative Delight's
  members); a modded item is checked against its `/dumpall` item list when there is one.
- Swap table: [tools/skyvillage_addon_remap.json](../tools/skyvillage_addon_remap.json), the file to tweak. Rerun:

      python3 tools/port_structure_jars.py --templates-only --remap tools/skyvillage_addon_remap.json disabledmods/SkyVillages-*.zip
      python3 tools/lootr_fixed_containers.py

  The zips stay in `disabledmods/` as the source for reruns. Result: 17 templates, 0 unknown blocks, 0 dropped items.
- Templates: `kubejs/data/skyvillages/structures/{farmersdelight,letsdo_vinery,letsdo_brewery}/`.
- Pools: `skyvillage_houses` +8 (weight 20, `house_processor`), `skyvillage_deco` +9 Brewery deco (weight 1, no
  processor). Every pool location has a template file.
- Each remapped state was checked against the target block's properties in the port's code (Vinery leaves rename
  `can_have_*` to `can_grow_*`; wine bottles get `facing` from `FacingBlock`).
- Not needed: village farm crops (no new house holds a vanilla crop field, so `village_farms.json` is unchanged), nether
  and meat swaps (no glowstone or redstone blocks), Lootr pass (the new chests already name sky village tables; the
  16 Brewery barrels are empty). `lootr_fixed_containers.py --dry-run` still lists 6 old `eugene` containers, unrelated
  and left as they are.
- Open: rerun the loot generators (`/dumpall`, then `/cegen` for `template_loot`, `loot_breakdown`) so loot counts
  include the new houses.

### 3.2 Swap table (generated from the JSON 2026-10-30, hops and dried bundles revised the same day)

Placed foods are set full. Facing on the Brewery bench to Vinery chair swap is unchecked (SVA-05).

Hops (user, 2026-10-30): ripe jungle red grape lattices. A lattice only survives with a full face behind it (opposite
its `facing`; any neighbour update otherwise breaks it and drops grapes), so the `attach` rule sets facing per block:
away from an adjacent full block, else back to back with a neighbouring lattice (each panel holds the other; a maximum
matching, wall-backed lattices may partner the rest), else air. In `letsdo_brewery_0`: 24 hops blocks, 20 lattices
(16 back to back, 4 on a wall), 4 air (7 lattices in the y 18 layer leave one over; three are boxed in by air or the
keg). Set `"unsupported": "keep"` to keep those as fragile lattices. Every lattice in the new templates (the Vinery
houses' too) passes the survival rule offline.

Dried barley and corn (user, 2026-10-30): hanging herbs wanted, from Dusty Decorations if it is in the pack. It is not
(`disabledmods/`), and it has no herb block anyway (its hanging pieces are cod, salmon, knives, pans and a lasso); no
installed mod has one. Air until a mod with hanging herbs is added: then point both entries at it and rerun.

| From | To | Properties | Why |
| --- | --- | --- | --- |
| `vinery:dark_cherry_leaves` | `vinery:cherry_leaves` | keep all; rename can_have_cherries to can_grow_cherries | port registers dark_cherry_* as cherry_*; property renamed in the port |
| `vinery:apple_leaves` | `vinery:apple_leaves` | keep all; rename can_have_apples to can_grow_apples | same block; property renamed in the port |
| `vinery:dark_cherry_stairs` | `vinery:cherry_stairs` | keep all | dark_cherry_* is cherry_* in the port |
| `vinery:dark_cherry_trapdoor` | `vinery:cherry_trapdoor` | keep all | dark_cherry_* is cherry_* in the port |
| `vinery:dark_cherry_drawer` | `vinery:cherry_drawer` | keep all | dark_cherry_* is cherry_* in the port |
| `vinery:dark_cherry_table` | `vinery:table` | keep all | 1.5.5 table removed in the port; 1.3.10 table has the same facing/type/waterlogged |
| `vinery:dark_cherry_big_table` | `vinery:big_table` | keep all | 1.5.5 big table removed; 1.3.10 big table has the same part/facing |
| `vinery:dark_cherry_chair` | `vinery:chair` | keep facing | 1.5.5 chair removed; 1.3.10 chair |
| `vinery:dark_cherry_shelf` | `vinery:shelf` | keep facing | 1.5.5 shelf removed; 1.3.10 shelf (bottles kept, items below) |
| `vinery:acacia_lattice` | `vinery:grapevine_lattice` | keep facing, age, grape | 1.5.5 wood lattices removed; grapevine lattice keeps the grape and age |
| `vinery:dark_oak_lattice` | `vinery:grapevine_lattice` | keep facing, age, grape | as acacia_lattice |
| `vinery:apple_press` | `vinery:wine_press` | keep all | port keeps the 1.3.10 id wine_press |
| `vinery:mead` | `vinery:cherry_wine` | keep all | no mead in the port; placed bottle becomes cherry wine (item below) |
| `brewery:wooden_brewingstation` | `brewinandchewin:keg` | keep facing; set vertical=false; block entity dropped | brewing station core |
| `brewery:brew_oven` | `brewinandchewin:heating_cask` | keep facing; block entity dropped | brewing station oven part |
| `brewery:brew_timer` | air |  | brewing station timer part, no pack equivalent |
| `brewery:brew_whistle` | air |  | brewing station whistle (2 tall), no pack equivalent |
| `brewery:barrel_main` | `minecraft:barrel` | keep facing; set open=false; block entity dropped | 2x2 big barrel part; empty vanilla barrel |
| `brewery:barrel_main_head` | `minecraft:barrel` | keep facing; set open=false; block entity dropped | big barrel part |
| `brewery:barrel_right` | `minecraft:barrel` | keep facing; set open=false; block entity dropped | big barrel part |
| `brewery:barrel_head_right` | `minecraft:barrel` | keep facing; set open=false; block entity dropped | big barrel part |
| `brewery:silo_wood` | `minecraft:barrel` | keep none; set facing=up, open=false; block entity dropped | grain silo; barrel standing up |
| `brewery:hops_crop_body` | `vinery:grapevine_lattice` | keep none; set grape=jungle_red, age=4; facing per block (attach), alone: air | hops lower half; no hops in the pack: a ripe grape lattice facing away from the nearest block |
| `brewery:hops_crop` | `vinery:grapevine_lattice` | keep none; set grape=jungle_red, age=4; facing per block (attach), alone: air | hops upper half, as the lower half |
| `brewery:dried_barley` | air |  | hanging dried bundle; no hanging herbs in the pack (Dusty Decorations, disabled, has none either) |
| `brewery:dried_corn` | air |  | hanging dried bundle; no hanging herbs in the pack (Dusty Decorations, disabled, has none either) |
| `brewery:table` | `vinery:table` | keep all | same facing/type/waterlogged connecting table |
| `brewery:bench` | `vinery:chair` | keep facing | bench seat; check facing in game |
| `brewery:bar_counter` | `farmersdelight:dark_oak_cabinet` | keep facing; set open=false | counter with storage; houses are dark oak |
| `brewery:cabinet` | `farmersdelight:dark_oak_cabinet` | keep facing, open; block entity dropped | cabinet |
| `brewery:sideboard` | `farmersdelight:dark_oak_cabinet` | keep facing; set open=false; block entity dropped | sideboard |
| `brewery:drawer` | `vinery:cherry_drawer` | keep facing, open; block entity dropped | drawer |
| `brewery:patterned_carpet` | `farmersdelight:canvas_rug` | keep none | rug |
| `brewery:beer_mug` | `minecraft:flower_pot` | keep none; block entity dropped | mug used as an (empty) flower pot |
| `brewery:beer_barley` | `brewinandchewin:beer` | keep none; block entity dropped | placed beer |
| `brewery:beer_wheat` | `brewinandchewin:beer` | keep none; block entity dropped | placed beer |
| `brewery:beer_hops` | `brewinandchewin:steel_toe_stout` | keep none; block entity dropped | placed beer |
| `brewery:beer_haley` | `brewinandchewin:strongroot_ale` | keep none; block entity dropped | placed beer |
| `brewery:whiskey_maggoallan` | `brewinandchewin:saccharine_rum` | keep none; block entity dropped | placed whiskey bottle |
| `brewery:fried_chicken` | `farmersdelight:roast_chicken_block` | keep facing; set servings=4 | placed meal |
| `brewery:pork_knuckle` | `farmersdelight:honey_glazed_ham_block` | keep facing; set servings=4 | placed meal |
| `brewery:mashed_potatoes` | `farmersdelight:shepherds_pie_block` | keep facing; set servings=4 | placed meal |
| `brewery:potato_salad` | `brewinandchewin:quiche` | keep facing; set bites=0 | placed meal |
| `brewery:dumplings` | `farmersdelight:rice_roll_medley_block` | keep facing; set servings=8 | placed meal |

| Item in a block entity | Becomes |
| --- | --- |
| `vinery:mead` | `vinery:cherry_wine` |
| `vinery:white_grapejuice` | `vinery:white_grapejuice_wine_bottle` |
| `vinery:red_grapejuice` | `vinery:red_grapejuice_wine_bottle` |
| `vinery:red_jungle_grapejuice` | `vinery:jungle_red_grapejuice_bottle` |
| `vinery:white_jungle_grapejuice` | `vinery:jungle_white_grapejuice_bottle` |

### 3.3 Original build steps (plan)

1. **Tool.** `tools/port_structure_jars.py` copies only templates its source's own pools reference, and the zips
   have none. Add an addon mode (`--into skyvillages` or a `TEMPLATES_ONLY` list) that converts every `.nbt` in the
   zip with the same `convert_template` and writes it under
   `kubejs/data/skyvillages/structures/<same sub path>`, without touching the pack's pools. Keep `--dry-run`.
2. **Remaps** (in `BLOCK_REMAP`, same-mod first):
   - Vinery 1.5.5 to port ids: `dark_cherry_{leaves,stairs,trapdoor,drawer}` to `cherry_*` (props kept);
     `dark_cherry_table` / `dark_cherry_big_table` / `dark_cherry_chair` / `dark_cherry_shelf` to the 1.3.10
     `table` / `big_table` / `chair` / `shelf` (facing kept, other props dropped); `acacia_lattice`,
     `dark_oak_lattice` to `grapevine_lattice`; `mead` to a port wine (`cherry_wine`) or air.
   - Brewery: per D1.
   - Check each remapped block's properties against the port's blockstate (unknown properties fall back to the
     default state silently), and every block entity `id` in the templates against the port (memory: storage
     BEs had an id rewrite; wine racks, `wine_box`, `flower_box`, placed wines).
   - Script output: a per-template list of every id that still resolves to air; the build target is zero.
3. **Pools** (hand JSON, `kubejs/data/skyvillages/worldgen/template_pool/`): add the houses to
   `skyvillage_houses.json` and the Brewery deco to `skyvillage_deco.json` at the D2 weights.
4. **Pack systems that list sky village templates:**
   - Village farm crops: add the 3 FD farms and the Vinery houses as `"auto"` in
     `development/creativeDelight/src/main/resources/creative_delight/village_farms.json` only if they hold vanilla
     crops (the swap rolls vanilla crop fields only); needs a CD rebuild.
   - Loot: no new tables. Rerun the loot generators with `/cegen` after `/dumpall` (`template_loot` cache,
     `loot_breakdown`, `audit_loot_containers`, `lootr_fixed_containers`) so the counts include the new houses.
   - Rerun the template scans that list sky village pieces (`meat_blocks`, `nether_swaps`, `wood_mismatch`,
     `terrain_adaptation_scan`) and check nothing new needs a swap (glowstone, redstone blocks).
5. ~~Archive the zips~~: kept in `disabledmods/` as the rerun source (3.1). Versions: FarmersDelightCompat 1.0.3,
   LetsDoVinery 1.0.4, LetsDoBrewery 1.0.4.1 (all 1.20.1 builds).

## 4. Tests

SVA-01 PASSED offline 2026-10-30 (3.1). Scratch server r1031 (2026-10-31): SQ-SVA-01 (SVA-02) and SQ-SVA-02 (all 17
templates place, block entities present) PASSED; SQ-SVA-03 (SVA-06) and SQ-SVA-04 (SVA-07) not run (deferred). Queue:
[scratch_queue.md](testing/scratch_queue.md); the looks need the user in game.

| ID | Check |
| --- | --- |
| SVA-01 | **PASS 2026-10-30** Offline: the tool reports zero unknown blocks for all 17 templates; pools parse; every pool location has a template file |
| SVA-02 | **PASS r1031 (2026-10-31, scratch server)** Server start: no `Error loading template` / `Unknown block` / codec errors for `skyvillages` in the log |
| SVA-03 | `/place template skyvillages:farmersdelight/sky_village_farmersdelight_farm_0` (and `_1`, `_2`): no air holes, crops and FD blocks look right |
| SVA-04 | Same for `letsdo_vinery/sky_village_letsdo_vinery_0..2`: furniture faces correctly, grape bushes and lattices, apple and cherry leaves with fruit, wine racks and presses open, shelf shows juice bottles |
| SVA-05 | Same for `letsdo_brewery/sky_village_letsdo_brewery_0..1` and `_deco_0..8`: keg and heating cask, chairs (bench swap) face the tables, grape lattices stand and stay when one is harvested, placed meals and drinks, barrels |
| SVA-06 | `/locate structure skyvillages:skyvillage` over several villages: addon houses appear (about 1 in 5 houses), bridges and villagers attach |
| SVA-07 | Chests in the new houses roll the existing sky village tables (Lootr where the pack uses it) |

## 5. Changelog

Bullet added 2026-10-30 under `## World Generation` in `unreleased.md`.
