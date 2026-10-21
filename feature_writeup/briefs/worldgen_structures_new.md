# Brief: worldgen / structures/new

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, rows `structures/new` #1 to #3 (all Planned;
no Testing rows). Source of all three: the user's structure notes of 2026-10-19 (folded from
`todo/change_scratchpad.md`, git `c82dd31^`): "greenhouses - wide pool of berry, saplings, fruit tree saplings,
flowers from biome types (snowy and cold in spruce, all jungle crops in jungle, birch flowers/crops(onion) in birch,
etc)"; "more structures - greenhouse variants, camera shop, starer bases, pioneer structures".
How a new structure joins the systems: [structure_tuning.md](../structure_tuning.md) 7.4.1 / 7.4.7 (the Campsite port
is the worked example), 3.17 (biome fits), 3.16 (meta tags), 3.22 (peaceful radius).

Verified (2026-10-21): the existing greenhouse template `small_villager_greenhouse:smallvillagergreenhouse` read with
the NBT reader of `tools/port_structure_jars.py` (9x10x15: oak log / stripped oak / spruce and dark oak stairs, 28
glass, 73 grass blocks, 27 dirt, 13 potted plants (saplings, bamboo, cactus, tulips, azalea...), loose allium /
poppy / orchid / bluet, rose bushes, cave vines, 2 composters, a chest, 2 villagers); Fruits Delight 0.5.11 blockstates
(saplings and `potted_*_sapling` for apple, bayberry, durian, fig, hawberry, kiwi, lychee, mango, mangosteen, orange,
peach, pear, persimmon; `blueberry_bush`, `cranberry_bush`). Other mod block ids below are unverified unless said.

Proposed test ids: **ST-260 to ST-279** (renumber at build time).

## Scope and order

1. #1 greenhouses first: it can start from the existing template (no building needed for the first variant set),
   and its generator gives the pattern (template variants + processor lists) the other two reuse.
2. #2 camera shop and #3 starter bases / pioneer structures need templates built in game; do them together as one
   "new lone houses" batch through the shared steps below.

**Shared steps for a new pack structure** (all three rows):
1. Template: build in a creative single-player world of the pack, save with a structure block (SAVE mode, name
   `creativeengineering:<path>`, include entities if villagers stay); the file lands in
   `saves/<world>/generated/creativeengineering/structures/`; copy to
   `kubejs/data/creativeengineering/structures/<path>.nbt`. Chests: save unopened with a `LootTable` tag set
   (`/data merge block x y z {LootTable:"creativeengineering:chests/<name>"}`), never with baked items.
2. Data in `kubejs/data/creativeengineering/worldgen/`: `structure/<name>.json` (jigsaw, `size` 1,
   `start_height` absolute 0, `project_start_to_heightmap: WORLD_SURFACE_WG`, `terrain_adaptation: beard_thin`,
   `step: surface_structures`, `biomes` = any list, the fit replaces it), `template_pool/<name>.json` (one element per
   variant, processors), and an emptied `structure_set/<name>.json` for the scanner (as the ported MCreator houses in
   `kubejs/data/villager_fish_shop/worldgen/`, and 7.4.1 step 2).
3. Loot: `kubejs/data/creativeengineering/loot_tables/chests/<name>.json`. Chest loot is then tuned through the loot
   framework ([loot.md](../loot.md): catalog `loot_config.json`, rules in `kubejs/server_scripts/loot/07_rules.js`,
   which take `global.item` keys); the static table JSON names items by id like every ported table.
4. `python3 tools/gen_structure_config.py` (rescan), tags in `structure_tags.json`: `mod/creativeengineering` (or a
   new `mod/pack_structures`), `type/house` (or `type/camp`), `group/remote` comes from those; `meta/lone_houses` or
   `meta/friendly_camps` so existing metas use them; a `land/*` fit tag. Then `python3 tools/gen_structure_groups.py`
   (a new structure has no mod default: sync gives weight 100 / frequency 1; set it by hand, about the other lone
   houses' 60 to 70), `--fit-rates`, `--check`.
5. Scans: `tools/terrain_adaptation_scan.py` (pad / air; tag `terrain_adaptation/*` or `template_trim` in
   `kubejs/server_scripts/worldgen/terrain_adaptation.js` if it flags one), `tools/audit_loot_containers.py`,
   `tools/nether_blocks_scan.py`, `tools/meat_block_scan.py`. FitTest's hardcoded structure count goes up (7.4.7 step 2).
6. Peaceful radius: `remote` members are peaceful (3 chunks) automatically.
7. Writeup: a new section in structure_tuning.md (or a `pack_structures.md`) with the list, rates and tests.

---

## structures/new #1: Greenhouses
Status Planned · Work data / templates · TODO `worldgen/structures/new #1`

### Goal
Greenhouses whose plants and wood come from the biome they stand in: berries, saplings, fruit-tree saplings, flowers
and crops by biome type (cold/snowy in spruce, jungle crops in jungle, birch flowers and onions in birch...), with
variants.

### Now
- One greenhouse in the pack: `small_villager_greenhouse:small_villager_greenhouse` (MCreator port in
  `kubejs/data/small_villager_greenhouse/`), in `group/remote` (weight 72 / 1.0, 1 per 144 in its biomes,
  `structure_groups.json` line ~1336), fits `land/plains` + `land/forest`, `meta/lone_houses`; fixed oak/spruce wood,
  fixed plants, a chest of flowers and saplings (`loot_tables/chests/fixed/120d23465a.json`).
- Biome traits already exist for crops and flowers: `kubejs/generation_scripts/worldgen/wild_crops.json` `biomes`
  (every Overworld biome -> traits: climate cold/temperate/warm, wet/dry, snowy, ground, shape incl. jungle, trees
  oak/birch/dark_oak/coniferous/cherry) and `crops` (per crop a `where` trait clause, e.g. onion `temperate grassy
  !swamp`, sweet berries `cold grassy`, blueberry `cold grassy snowy`, banana the dense jungles, cocoa `warm wet`);
  `wild_flowers.json` uses the same trait selectors for flowers.
- Plant ids live in `kubejs/startup_scripts/_lib/item_dict.js` (Fruits Delight saplings at lines ~12850 to 12900,
  Vinery `apple_tree_sapling` / `cherry_sapling` ~11114 / 11139); `tools/food_catalog.py` (line ~249) has a parser
  for item_dict.js keys.

### Build
1. **Spec** `kubejs/generation_scripts/worldgen/greenhouses.json`: per family (`spruce`, `birch`, `oak`, `jungle`,
   `dark_oak`, `acacia`, `desert`?, decided below) a `fit` (the `land/*` fit or a new fit name), a wood palette map
   (oak/spruce/dark oak blocks of the template -> the family's), and plant lists as **item_dict keys** (resolved to
   ids by the generator with the `food_catalog.py` parser, so ids stay in one place): `pots` (saplings and flowers
   that have a potted block), `ground` (flowers, bushes), `crops` (crop blocks with an age), picked by trait clauses
   from `wild_crops.json` / `wild_flowers.json` where possible (e.g. the `cold grassy` crops for spruce).
2. **Template markers.** Make one marker template from `smallvillagergreenhouse.nbt` (by hand in game, or by the
   generator): one placeholder block per slot kind that the template doesn't otherwise use, e.g.
   `minecraft:potted_dead_bush` for every pot, `minecraft:dead_bush` for ground plants, farmland +
   `minecraft:wheat[age=0]` for crop rows.
3. **Generator** `tools/gen_greenhouses.py` (NBT functions from `port_structure_jars.py`): per family
   - a wood-swapped copy of the marker template -> `kubejs/data/creativeengineering/structures/greenhouse/<family>.nbt`;
   - a processor list `kubejs/data/creativeengineering/worldgen/processor_list/greenhouse/<family>.json` of vanilla
     `minecraft:rule` processors: for N candidates of a slot kind, chained rules with
     `input_predicate: {predicate_type: random_block_match, block: <placeholder>, probability: 1/N, 1/(N-1), ..., 1}`
     -> `output_state` (crops with a random-looking age: several rules per crop with fixed ages), so each slot gets
     an independent pick (a vanilla rule processor rolls per block; verify the 1.19.2 `RuleTest` JSON names against
     the vanilla jar before writing: unverified here);
   - `structure/greenhouse/<family>.json`, `template_pool/greenhouse/<family>.json` (element `single_pool_element`
     with the processor list), an emptied `structure_set`.
   - a loot table per family (seeds and saplings of the family) in `loot_tables/chests/greenhouse/<family>.json`.
4. Tags: `mod/creativeengineering`, `type/house`, `group/remote` via those, `meta/lone_houses`, the family's
   `land/*` fit tag (spruce -> `land/spruce` or `land/taiga` + `land/snowy`; birch -> `land/birch`; jungle ->
   `land/jungle`; oak -> `land/oak`; dark oak -> `land/dark_forest`; acacia -> `land/savanna`).
5. **The MCreator greenhouse:** turn it off (`enabled: false` in `remote`) once the oak family exists (same template),
   or keep it as the plain oak one and skip the oak family (Decision).
6. Rates: one greenhouse per family about as common as today's one is in its biomes (1 per 144); sync then hand-set
   the weights, `--fit-rates`, `--check`.

### Ids and data
- Verified: `small_villager_greenhouse:smallvillagergreenhouse` contents (above); Fruits Delight saplings and potted
  saplings, `fruitsdelight:blueberry_bush`, `fruitsdelight:cranberry_bush`; `minecraft:sweet_berry_bush`
  (`global.blocks.sweet_berry_bush`).
- To check in the jars before writing the spec (unverified): crop block ids and age ranges of Farmer's Delight
  (`farmersdelight:onions`, `cabbages`, `tomatoes`, `rice`), Neapolitan banana and vanilla, Vinery saplings' potted
  forms, Berry Good, Atmospheric if present; every potted block exists for each sapling picked (a pot rule pointing at
  a non-existent potted block is a load error).
- Placeholder blocks: vanilla, must not occur elsewhere in the template (the generator should assert that).

### Decisions
- **Template source** (the row's decision): wood variants of the existing MCreator greenhouse (no building,
  recommended for the first batch) vs new greenhouse builds per family. Recommend the existing template first, new
  builds later as more pool elements.
- **Families:** recommend spruce (cold), birch, oak (temperate), jungle, dark oak; desert/savanna optional (cactus,
  acacia, dry crops).
- **Keep the MCreator greenhouse** as the oak family or replace it. Recommend replace (one oak family, its chest
  fixed to the family loot).
- **Plants as blocks only, or loot too:** recommend both (blocks for the look, chest for seeds/saplings to take).

### Gotchas
- Rule processors pick per block, not per greenhouse: a family's pots are a mix by design. For one consistent pick per
  greenhouse, make each pick its own pool element.
- Crops need farmland and light; a crop row on grass pops off at the first block update. Use farmland under crop
  placeholders and check the template trim / ground match don't touch them (the base layers rule: ground match only
  swaps the bottom `baseLayers`, 3.8).
- Ground match may swap the greenhouse's grass floor to the biome's ground (desert sand) if it is in the bottom
  layers; add the greenhouse to `ground_match/exempt` (`kubejs/server_scripts/worldgen/ground_match.js`) if that
  looks wrong.
- Two villagers live in the template; a lone house with villagers is in `mod/villager_houses` style (fine).

### Tests
- ST-260 Load: every family's structure and processor list loads (no "unknown block" / codec errors);
  `/ceplace structures creativeengineering:greenhouse/*` builds one of each in rows.
- ST-261 Plants: the spruce greenhouse holds only spruce wood and cold-family plants; 5 placements of one family give
  different pot contents.
- ST-262 Biomes: `/locate structure creativeengineering:greenhouse/jungle` answers in a jungle-family biome.

---

## structures/new #2: Camera shop
Status Planned · Work data / templates · TODO `worldgen/structures/new #2`

### Goal
A camera shop structure for the Exposure cameras ([exposure.md](../exposure.md): camera, films, lightroom, albums,
frames, Polaroid instant camera, Expanded lenses and filters).

### Now
- Nothing exists. Exposure's own loot comes from its `exposure:add_table` global loot modifier (village houses,
  shipwreck map chests, dungeons; exposure.md "Loot"); Expanded's filters are loot-only.
- No photographer villager profession in the pack (none found in exposure.md).

### Build
1. Template (user): a small shop (counter, darkroom corner with a lightroom, frames with photos on the walls, a camera
   stand, item frames with films). Frames showing photos need exposure ids that exist in the world, so hang empty
   frames or aged photographs only (Gotchas).
2. Shared steps 1 to 7: `creativeengineering:camera_shop`, `type/house`, `group/remote`, `meta/lone_houses`, fit
   `land/plains` + `land/forest` (town-like biomes) or every land fit (Decision).
3. Loot table `creativeengineering:chests/camera_shop`: film rolls (`exposure:black_and_white_film`,
   `exposure:color_film`), photographic paper, developing items, a rare camera, Expanded filters/lenses as rare
   entries (ids from item_dict; check the backport's registry names in `development/exposureBackport` before
   writing). Then add the table to the loot catalog (`tools/gen_loot_config.py`, loot.md).
4. Optional later: a village version (a house piece in the vanilla village pools) needs a pool injection, which
   PoolTuner can't do today (it only reweights); a small patcherMod addition "inject" in `pool_config.json`, or the
   Waystones approach. Not recommended for the first build.

### Ids and data
Exposure item ids: in `development/exposureBackport/src/main/java/io/github/mortuusars/exposure/Exposure.java`
(base), `.../ExposureExpanded.java`, `.../ExposurePolaroid.java`; check `item_dict.js` for existing keys and add
missing ones under the dict's `exposure_*` / mod-prefixed key style (repo, not re-read for this brief).

### Decisions
- **Template source** (the row's decision): user-built (recommended; no mod ships one).
- Lone house vs village piece: recommend lone house now.
- Biomes: town-like land families (recommended) or anywhere.

### Gotchas
- A photo in a frame is an exposure id resolved on the server; a template can't carry a real photo between worlds.
  Use empty frames, `exposure:aged_photograph` without data, or prints made in the build world only if Exposure
  stores the image with the item (check before saving).
- Lootr swaps vanilla chests per player automatically; a modded container in the template needs a
  `loot_container_swaps.json` entry (`tools/audit_loot_containers.py` flags it).

### Tests
- ST-263 `/ceplace structures creativeengineering:camera_shop` builds it intact; its chest rolls from the new table
  (`/loot spawn ~ ~ ~ loot creativeengineering:chests/camera_shop`).
- ST-264 `/locate structure creativeengineering:camera_shop` answers in a fitted biome.

---

## structures/new #3: Starter bases and pioneer structures
Status Planned · Work data / templates · TODO `worldgen/structures/new #3`

### Goal
New structures: "starter bases" (a first-night shelter someone left behind) and "pioneer structures" (settlers
working new land). The row's decision is what they hold.

### Now
- Nothing exists. Closest kin: friendly camps (`meta/friendly_camps`: villager tents, Campsite Structures' 6 camps,
  C.C.C. trader camp), lone houses in `remote`, Farmer's Structures (`meta/farmers_structures`). Metas that combine
  camps and houses: `farmers_camp`, `house_camp`, `waysign_camps`, `waysign_house_camp`.

### Build
1. Content proposal for the user (Decision): 
   - starter bases: a hillside dugout (door, bed, crafting table, furnace, a chest with stone tools, torches, food),
     a dirt hut, a tree platform; 2 to 4 designs, neutral (no villagers), small loot.
   - pioneer structures: a covered wagon, a half-built log cabin with a sawhorse, a clearing with stumps and a
     woodpile, a fenced field with a scarecrow; 3 to 5 designs, a villager or two.
2. Templates (user), then shared steps 1 to 7. Ids `creativeengineering:starter_base/<name>` and
   `creativeengineering:pioneer/<name>` (one structure per kind with a pool of designs keeps the rates simple: one
   group member each). Starter bases: `type/house`, `group/remote`, `meta/lone_houses`; pioneers: `type/camp`,
   `group/remote`, `meta/friendly_camps` (so `waysign_camps`, `farmers_camp` and `fortified_camps` use them).
3. A pioneer meta (optional): `meta/pioneer_settlement` anchored on the pioneer structure (share 0.3) with 1 to 2
   more pioneer pieces and a way sign; data only.
4. Loot tables per kind (`creativeengineering:chests/starter_base`, `.../pioneer`), catalogued through the loot
   framework.

### Ids and data
New ids above; loot items through `global.item` keys in loot rules. Nothing to verify in jars until the contents are
chosen.

### Decisions
- **What they hold** (the row's decision): the proposal above; the loot level (early-game tools vs only supplies).
  Recommend supplies and worn stone/iron tools, no diamonds.
- Starter bases spawn anywhere on land (any land fit) vs biome-matched wood. Recommend biome-matched (wood variants
  through the same template variant tool as the greenhouses and rafts).

### Gotchas
- A pioneer structure with a villager counts toward the villager-house rules (`mod/villager_houses` style); keep
  the peaceful radius in mind: as `remote` members they keep pillagers 3 chunks away.
- Dugouts into slopes: terrain adaptation `bury` or `beard_box` (3.15), and earth fill limits (`[earth-fill]`
  size class read once with `log = ALL`, 7.4.7 step 4).

### Tests
- ST-265 Both kinds build intact with `/ceplace`, `/locate` answers each, chests roll the new tables.
- ST-266 A `waysign_camps` meta can pick a pioneer camp as a part (log line names it).

---

## Built, needs play (Testing / Complete)

None in this subfamily.

## Writeup corrections made

None.
