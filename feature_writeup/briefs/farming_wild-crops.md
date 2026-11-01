# Brief: farming / wild-crops

Written 2026-10-21 for the open `farming/wild-crops` rows of [TODO.md](../todo/TODO.md). Reference writeups:
[crops.md](../crops.md) §2.7, §2.8 and findings V1 to V20 (§4, from line 1150), and the generated
[crops_by_biome.md](../crops_by_biome.md) (regenerated after the 2026-10-28 cantaloupe merge; uncommitted).
Verified and extended 2026-10-31 (rows #5, #6 added; paths moved to Creative Delight, see below). Test prefix **CR-**; the new
tests below start at CR-79 (CR-69 to CR-78 are proposed in [farming_crops.md](farming_crops.md)); take the next free
numbers when adding them. Layout: [README.md](README.md).

**Verified against jars** (2026-10-21): Neapolitan 4.1.0 (`NeapolitanFeatures`: configured + placed `mint_pond`,
rarity 4), Corn Delight 1.0.3 (`WildCornGeneration`: configured + placed `corn_delight:patch_wild_corn`), Fruits Delight
0.5.11 (`FDTrees`: placed `fruitsdelight:tree/<name>_tree` on configured `tree/<name>_tree_wild`, `count_extra(0, 1/N, 1)`;
`FDPineapple`: random patch of 24 tries, xz 5, y 3, needs `minecraft:sand` below, rarity 32; `PineappleBlock` /
`WildPineappleBlock` survive on `#fruitsdelight:pineapple_grows_on` only), Rustic Delight 1.3.0 (`ModConfiguredFeatures`:
the three wild patches use `Feature.FLOWER`; `ModPlacedFeatures`: config-driven rarity), Supplementaries 2.4.20
(`worldgen/placed_feature/wild_flax.json` + configured + `forge/biome_modifier/wild_flax.json` ship as data; wild flax
survives on `#minecraft:sand`, placed next to water), Terralith 2.3.12 (cave biome list), Myrtrees 1.2.0, the Vinery port
source (`AppleLeavesBlock` gives `minecraft:apple`). Farmer's Delight config chances read from
`config/farmersdelight-common.toml`. Everything else is marked unverified.

## Scope and order

| Row | Change | Work | Group |
|---|---|---|---|
| wild-crops #1 | Densities: per-crop targets, pack copies with their own rarity | data + tool | B |
| wild-crops #2 | Ground limits (pineapple sand; cliff cabbages, gravel / ice cane, mud melons) | data | B |
| wild-crops #3 | Biome gaps and choices (V3, V4, V16, V19, V20; missing cave biomes) | data | A (cave biomes), C (choices) |
| wild-crops #4 | Sources and duplicates: left is the trader berries (plus doc text); coffee settled by the `food/drinks #3` merge; flax, RD patches, grapes, white strawberries built 2026-10-22 | KubeJS | A |
| wild-crops #5 | Third, most common cantaloupe patch (normal + mini) | data | B |
| wild-crops #6 | Wild sea grape under the system (Oceanic Delight) | data / KubeJS | A (needs an ocean rule, see section) |

Order: **A** quick data fixes that need no decisions (classify the missing cave biomes, wild flax back under the
system, Rustic Delight patches as plain patches, trader berries); then **B** in one pass because both edit
`wild_crops.json` + `kubejs/data/creativeengineering/worldgen/*/wild/` and both are judged by the generator's density
table (add the target column first); then **C**, the choices that wait on other rows: V16 waits on
`ports/autumnityport #2` (pumpkin patch / autumn biomes) and wild pineapple ground shares a tag with `farming/crops #1`
G15 (V5/V18 coffee was settled by the `food/drinks #3` merge, 2026-10-22). #5 goes with group B (same json, same
density table); #6 needs only the ocean `add` list and a generator sea-floor row.

Every change here is worldgen: **new chunks only**, rejoin to apply (switch `config/creative_delight-common.toml`
`[worldgen] wildCrops`, line 41), and rerun `python3 tools/gen_crop_worldgen_table.py` plus `tools/test_wild_crops.sh`
after each edit. Wild crops moved into Creative Delight on 2026-10-25 (memory `vegetation-worldgen-system`): tags are
`creative_delight:wild_crops/<key>`, pack features `creative_delight:wild/*`. Files (checked 2026-10-31):
- [wild_crops.json](../../kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.json) (404 lines: `_readme`, `crops`
  69 rules; the jar copy is read when the pack folder copy is absent, via `CDResources.text`);
- biome traits moved out to [biome_traits.json](../../kubejs/generation_scripts/worldgen/biome_traits.json) (146 biomes,
  shared with mushrooms, flowers, bees, mob spawns);
- applier [wild_crops.js](../../kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.js) (59 lines) builds on the
  shared [_biome_rules.js](../../kubejs/server_scripts/worldgen/_biome_rules.js) (`resolve` line 56: `never` traits
  skip a biome, but `add` biomes bypass it, line 66; tags via `global.tags.add` line 147; modifiers lines 167-197;
  unclassified-biome warning line 245);
- pack features in `kubejs/data/creative_delight/worldgen/{configured,placed}_feature/wild/` (11 configured,
  18 placed today), copied into the CD jar by its `dataTrees`;
- [tools/gen_crop_worldgen_table.py](../../tools/gen_crop_worldgen_table.py) (844 lines; `JAVA_PLACED` line 114 restates
  the Java-registered placed features; FD chances via `fd()` line 101; `NEVER = {'ocean', 'cave'}` line 486).

---

## wild-crops #1: Densities moved by the placement system

Status Planned · Work data · TODO `farming/wild-crops #1`

### Goal
Each wild crop has a target density ("patch attempts per 1000 overworld chunks", the generator's unit), and a knob
that reaches it without touching the biome rule.

### Now
From [crops_by_biome.md](../crops_by_biome.md) §1 (rechecked 2026-10-31; nothing in this row is built: pumpkin is still
chance 300, sugar cane 6, mint / corn / persimmon still the mods' Java placed features; the TODO's "mint x5.6" is
**x3.2** in the current table because the area figures changed):

| Crop | Now | Mod default | Change | Feature in `wild_crops.json` | Knob |
|---|---|---|---|---|---|
| Mint pond | 13.1 | 4.1 | x3.2 | `neapolitan:mint_pond` (Java, rarity 4) | pack placed copy |
| Pumpkin | 0.3 | 1.8 | x0.19 | `creative_delight:wild/pumpkin` (chance 300) | edit the json |
| Sugar cane | 9.3 | 132.2 | x0.07 | `creative_delight:wild/sugar_cane` (chance 6) | edit the json (+ count) or widen the rule |
| Persimmon tree | 0.5 | 1.3 | x0.37 | `fruitsdelight:tree/persimmon_tree` (Java, 1/50) | pack placed copy |
| Rice | 1.6 | 6.8 | x0.24 | `farmersdelight:patch_wild_rice` | FD config `[world.wild_rice] chance = 20` (farmersdelight-common.toml line 98) |
| Corn | 2.2 | 7.8 | x0.28 | `corn_delight:patch_wild_corn` (Java, rarity 30 hard-coded) | pack placed copy |
| Also low | lemon 0.7 (x0.50), mini melon 0.9 (x0.52), carrot / onion 1.0 (x0.50), kiwi / orange 1.1 (x0.55) | | | | FD / Delightful configs, pack copies |
| Also high | tea 75.8 (x9.0, by choice, #3), fig 4.5 (x2.3), Cantaloupe 1.0 (x2.5), surface mushroom colonies 1.5 each plus the cave colonies (39.7, mushroom_spawns, not this row) | | | | |

Per 1000 = attempts per chunk x share of land area x 1000; it is an upper bound (ground tests can still fail).

### Build
1. **Targets in the config.** Add an optional `"target_per_1000"` to each crop in `wild_crops.json` (document it in
   `_readme`). In the generator, print a "Target" and "Chance for target" column in §1: for a single rarity-filtered
   feature, `chance_needed = current_chance x now / target`. `wild_crops.js` ignores the key.
2. **Pack placed copies** for Java placed features (one json each in
   `kubejs/data/creative_delight/worldgen/placed_feature/wild/`), then swap the id in the crop's `features`:
   - `wild/mint_pond.json`: `{"feature": "neapolitan:mint_pond", "placement": [rarity_filter N, in_square,
     heightmap WORLD_SURFACE_WG, biome]}` (the generator records Neapolitan's heightmap as WORLD surface; the
     original uses `PlacementUtils` constant `f_195354_`, check it maps to WORLD_SURFACE_WG).
   - `wild/corn.json`: feature `corn_delight:patch_wild_corn`, rarity N, `in_square`, heightmap MOTION_BLOCKING,
     `biome`.
   - `wild/persimmon_tree.json`: feature `fruitsdelight:tree/persimmon_tree_wild`, placement like
     `wild/rubber_tree.json` (count or rarity, in_square, surface_water_depth_filter 0, heightmap OCEAN_FLOOR, biome,
     `would_survive` `fruitsdelight:persimmon_sapling`).
   The crop's mod modifier is already `forge:none` through `disable_modifiers`, so only the pack copy places it.
3. **Edit existing pack copies:** `wild/pumpkin.json` `chance` (300 -> ~55 hits the mod default 1.7);
   `wild/sugar_cane.json` (6 -> 1 is the most a rarity can do, about 40 per 1000; beyond that add a `count`
   placement or widen the rule, see Decisions).
4. **Configs:** rice `chance` 20 -> ~6 in `config/farmersdelight-common.toml` (and the `configs_edited/` copy, as the
   other hand-edited configs). The generator reads it (`fd('wild_rice')`).
5. **Generator gotcha:** a pack placed feature that points at a **Java-registered configured feature** has no JSON for
   the generator to resolve, so `crops_in_value` cannot name the crop. Add a small `JAVA_CONFIGURED = {
   'neapolitan:mint_pond': 'Mint', 'corn_delight:patch_wild_corn': 'Corn', 'fruitsdelight:tree/persimmon_tree_wild':
   'Persimmon (tree)', ...}` fallback next to `JAVA_PLACED` (line 114) and consult it in `crops_in_value`.
6. Rerun the generator and `tools/test_wild_crops.sh`; update crops.md V13 to the new numbers and §2.8's "Pack
   features" table (add the new copies).

### Ids and data
Configured features (verified ids): `neapolitan:mint_pond`, `corn_delight:patch_wild_corn`,
`fruitsdelight:tree/persimmon_tree_wild` (pattern `tree/<name>_tree_wild`). Placed ids today: `neapolitan:mint_pond`,
`corn_delight:patch_wild_corn`, `fruitsdelight:tree/persimmon_tree`. New pack ids `creative_delight:wild/mint_pond`,
`wild/corn`, `wild/persimmon_tree`.

### Decisions
- Targets: recommendation "mod default x1 to x2" for every crop (the mods' numbers were tuned for their own biome
  sets; the pack's sets are smaller, so slightly denser is fair): mint 3, pumpkin 2, sugar cane 40, persimmon 1.5,
  rice 5, corn 6; leave the "high" crops unless play says they are too common.
- Sugar cane: rarity 1 in the 6 river / beach biomes gives ~40; to get nearer the old 116, widen the rule to swamps and
  desert oases (`"swamp", "oasis"` clauses) rather than stacking counts. Recommendation: widen.

### Gotchas
- Changing a rarity changes every biome the crop is in; the rule stays the place for "where", the placed json for "how
  often".
- After editing a FD config chance, the pack copies of cabbages / beetroots are not affected (they have their own json,
  crops.md §2.8).
- `test_wild_crops.sh` checks ids and modifier counts; a new pack feature whose `feature` id is unknown to the stubs
  may need adding to its known-feature list (`tools/wild_crops_test/RhinoWildCrops.java`).

### Tests
- **CR-79 Densities** (proposed, not yet in crops.md): new world, `/locate biome terralith:highlands` and fly 20 chunks -> mint ponds are occasional
  (a few per few hundred chunks), not every few chunks. `taiga`: pumpkin patches seen within ~100 chunks. A river:
  sugar cane on most sandy banks.

---

## wild-crops #2: Ground limits

Status Planned · Work data · TODO `farming/wild-crops #2`

### Goal
Fix or accept each place where a crop's rule puts it in a biome whose ground it cannot use (V14, V15).

### Now
- **Pineapple (V14):** rule `["warm beach", "warm wet coastal"]` = beach, mangrove swamp, granite cliffs, tropical
  jungle; feature `fruitsdelight:pineapple` (Java) tests `minecraft:sand` below; the wild block itself survives only on
  `#fruitsdelight:pineapple_grows_on` (sand, red sand, coarse dirt). Off the beach it lands only on sandy coast.
- **Cabbage / sea beet (V15):** pack features `wild/cabbages`, `wild/beetroots` take sand or `#minecraft:dirt`; the
  cliffs in the rule (`terralith:basalt_cliffs`, `frozen_cliffs`, `granite_cliffs`, `white_cliffs`, `stony_shore`) are
  mostly stone or snow on top, so little grows; the Sandy Shrub companions need sand.
- **Sugar cane (V15):** rule `["river", "beach"]` includes `frozen_river` (ice, no liquid water beside the bank) and
  `terralith:gravel_beach` (gravel: cane needs dirt or sand).
- **Melon / mini melon (V15):** `warm wet !cliff` includes `mangrove_swamp`; the melon patch tests grass (plus
  `#creativeengineering:worldgen/grass_like_soils` through the soil-compat overrides, crops.md §2.8.1), not mud.

### Build
1. **Pineapple:** pack features
   - `configured_feature/wild/pineapple.json`: `minecraft:random_patch`, tries 24, xz_spread 5, y_spread 3, feature
     `simple_block` `fruitsdelight:wild_pineapple` with `block_predicate_filter` `all_of(replaceable,
     matching_block_tag offset [0,-1,0] tag fruitsdelight:pineapple_grows_on)` (copy the shape of `wild/hamimelon.json`);
   - `placed_feature/wild/pineapple.json`: rarity_filter 32, in_square, heightmap (MOTION_BLOCKING like the other pack
     copies), biome;
   - `wild_crops.json` key `pineapple`: `features` -> `creative_delight:wild/pineapple` (keep
     `disable_modifiers: ["fruitsdelight:pineapple"]`), update its `notes`.
   - The wild block still needs the tag to survive, so to reach jungle grass or mangrove mud the tag must grow:
     `t.add(e, 'fruitsdelight:pineapple_grows_on', [item.grass_block, item.mud])` in
     [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) (keys exist: `i.grass_block` item_dict.js 2397, `i.mud` 681;
the tag is not extended anywhere today). This
     also lets players plant pineapple saplings on grass and mud: decide together with `farming/crops #1` G15.
2. **Cabbage / beet cliffs:** either drop the snowy and basalt cliffs from the rule (`"cliff !snowy"` and
   `except: ["terralith:basalt_cliffs"]`), or accept. Check the trait names in `biomes` first.
3. **Sugar cane:** `except: ["minecraft:frozen_river", "terralith:gravel_beach"]` on the `sugar_cane` rule (pair it with
   the #1 widening to swamps / oases). The `river` set is now `frozen_river`, `river`, `terralith:warm_river`; `beach`
   is `beach`, `snowy_beach`, `terralith:gravel_beach` (biome_traits.json, 2026-10-31).
4. **Melons:** accept (mangrove has some grass), or `except: ["minecraft:mangrove_swamp"]` for `melon` and
   `mini_melon`.
5. Regenerate; update crops.md V14 / V15 and the §2.8 tables.

### Ids and data
`fruitsdelight:wild_pineapple` = `i.wild_pineapple` (item_dict.js 13268), tag `fruitsdelight:pineapple_grows_on`
(verified contents), new `creative_delight:wild/pineapple`. Biome ids as listed (in `biome_traits.json`).

### Decisions
- Pineapple on grass / mud: recommendation yes for the wild block only is impossible (one tag serves both), so: add
  grass + mud to the tag and accept saplings planting there; it is a tropical bush, grass is believable.
- Cliffs, cane, melons: recommendation prune cane (zero yield there), prune frozen + basalt cliffs, accept melons.

### Gotchas
- `simple_block` checks `canSurvive` before placing, so the predicate filter alone is not enough: the tag is what lets
  the wild pineapple stand on grass.
- Removing biomes from a rule changes the density numbers of #1; do #1 and #2 in one regeneration.

### Tests
- **CR-80 Pineapple ground** (proposed): `terralith:tropical_jungle` and `mangrove_swamp` -> wild pineapples on grass / mud as
  well as sand. `/place feature creative_delight:wild/pineapple` on stone -> fails.
- **CR-81 Cane banks** (proposed): `frozen_river` and `terralith:gravel_beach` -> no cane attempts (generator shows them gone);
  `river` -> cane.

---

## wild-crops #3: Biome gaps and choices

Status Planned · Work data · TODO `farming/wild-crops #3`

### Goal
Every land biome either has wild crops or is a deliberate "none"; the choices in V3, V4, V16, V20 are made.

### Now
- **Unclassified biomes (plan_1.0.5.md §2):** the shared applier warns about overworld biomes missing from
  `biome_traits.json`. The user's `logs/latest.log` (checked 2026-10-31) names all five:
  `[wild-crops] 5 overworld biomes are not in kubejs/generation_scripts/worldgen/biome_traits.json and match no rule:
  terralith:cave/crystal_caves, terralith:cave/desert_caves, terralith:cave/ice_caves, terralith:mountain_steppe,
  terralith:skylands` (from `_biome_rules.js#245`). Three are caves; `mountain_steppe` is a surface biome and
  `skylands` the base skylands biome (the four seasonal skylands are classified, lines 142-145). The generator says
  "every overworld biome classified" because `kubejs/exported/biome_data.json` (from `/dumpbiomes`) lacks them;
  refresh it.
- **V19:** 13 land biomes match no rule (frozen / jagged / stony / emerald / volcanic peaks, volcanic crater, snowy slopes,
  ice spikes, glacial chasm, gravel desert, windswept gravelly hills, stony and windswept spires), crops_by_biome.md §6.
  All rock, ice or gravel.
- **V16:** pumpkin "orchard" = Terralith Skylands Autumn only. A real pumpkin patch / autumn biome is
  `ports/autumnityport #2`.
- **V20: DONE 2026-10-22, shipped 1.1.01** (see Built, needs play): tea is in 20 biomes. Nothing left here.
- **V4:** jungles carry the whole warm-wet group plus bananas, lemons and five fruit trees, plus rubber trees.
- **V3:** carrot and onion share one rule (`temperate grassy !swamp`, 25 biomes).

### Build
1. Add the five biomes to [biome_traits.json](../../kubejs/generation_scripts/worldgen/biome_traits.json): the three
   caves with the trait `cave` (e.g. `"terralith:cave/ice_caves": "cave cold icy"`, `"terralith:cave/desert_caves":
   "cave warm dry sandy"`, `"terralith:cave/crystal_caves": "cave"`; caves never match a wild-crop rule, wild_crops.js
   `never`); `terralith:mountain_steppe` with real traits (it is a grassy, cold-temperate mountain biome: e.g.
   `"temperate grassy mountain"`, check its grass in game) so it gets crops; `terralith:skylands` like
   `skylands_spring` (`"temperate grassy forest sky"`). The file is shared: the mushroom, flower, bee and mob-spawn
   rules pick the new biomes up too (check their logs for the same warning going away).
2. V19: mark them deliberate in `_readme` (one line) and in crops.md V19. Optional: windswept gravelly hills could take
   the cold grassy set if Terralith gives it grass patches (check §5 of crops_by_biome.md); recommendation leave.
3. (V20 tea: done 2026-10-22.)
4. V3: split, e.g. carrot `temperate grassy plains` + `temperate grassy forest !dark_oak`, onion `temperate grassy` minus
   forests. Recommendation: carrots `temperate grassy forest !swamp`, onions `temperate grassy plains` +
   `temperate shrubland` (traits that exist in `biomes` today: plains, forest, shrubland, flower, mountain, ...; there
   is no `meadow` trait, so add `minecraft:meadow` by `add` if wanted).
5. V4: recommendation move two crops out of the jungles: bell pepper to `warm dry grassy` (savannas, like tomato) and
   RD coffee stays (V5 decides coffee). Mini melon to `warm grassy plains`.
6. V16: pumpkin rule gets the new biome(s) when autumnityport #2 lands; until then add `minecraft:flower_forest`? No:
   wait for that row.
7. Regenerate, update crops.md V3, V4, V16, V19, V20 and the §2.8 tables.

### Ids and data
Biome ids above (Terralith verified). No item ids.

### Decisions
- V3 and V4 moves (above): user's call, recommendations given.
- V16: wait for `ports/autumnityport #2`.

### Gotchas
- A biome tagged `cave` gets nothing even if it is reachable from the surface (Terralith Underground Jungle is
  already `cave`).
- Moving a crop changes its density; do it in the same regeneration as #1.

### Tests
- **CR-82 No warning** (proposed): single-player new world -> `latest.log` has no `[wild-crops] ... not in` warning.
- CR-83 Tea spread: in crops.md (scratch PASSED 2026-10-22), game part open; see Built, needs play.

---

## wild-crops #4: Sources and duplicates

Status Planned · Work data / KubeJS / config · TODO `farming/wild-crops #4`

### Goal
Decide each duplicate or missing source: two coffees, two apples (V5), wild flax (V6), Rustic Delight flower patches
(V7), FR coffee with no source (V18), Vinery grape bushes in vanilla biomes only (V1), white strawberries, berries in
the wandering trader.

### Now
- **Apples (V5):** both trees drop the same item, `minecraft:apple` (Vinery port `AppleLeavesBlock` returns
  `Items.APPLE`; Fruits Delight `apple_leaves` loot has `minecraft:apple`). Only the Vinery tree spawns
  (`wild_crops.json` key `apple` disables `fruitsdelight:apple`); the Fruits Delight sapling is sold by the Winemaker.
  Nothing to merge item-wise: the "two apples" half of V5 is done unless the user wants the FD sapling gone too.
- **Coffee (V5 / V18): settled by the merge.** `food/drinks #3` purged Farmer's Respite's coffee berries, beans, sack,
  wild bush and drinks into Rustic Delight's (built 2026-10-22, scratch PASSED, [item_merges.md](../item_merges.md) 2.8;
  confirmed in game 2026-10-27). FR beans need no source; the `coffee` key's `disable_modifiers:
  ["farmersrespite:coffee_bush"]` stays so the purged bush never spawns. Left: the stale comment at
  wandering_trades.js 35-36 ("its wild coffee only grows in the Nether") and crops.md V5 / V18 text.
- **Built 2026-10-22, shipped 1.1.01** (V6, V7, V1, white strawberries; Built, needs play): the facts below are kept as
  reference only. Wild flax (V6): `config/supplementaries-common.toml` `[functional.flax.wild_flax] enabled = false`
  (hand-edited copy in `configs_edited/`). Verified: Supplementaries still ships `placed_feature/wild_flax.json`
  (rarity 7, Y 29-120 above bottom, WORLD_SURFACE), `configured_feature/wild_flax.json` (35 tries, xz 4, air + survives +
  water beside), and `forge/biome_modifier/wild_flax.json` on `#supplementaries:has_wild_flax` (filled at runtime,
  empty while the config is off). Wild flax survives on `#minecraft:sand`.
- **Rustic Delight patches (V7):** verified `Feature.FLOWER` for `wild_cotton_feature` (32 tries, xz 6, y 2),
  `wild_bell_peppers_feature` (48, 4, 4), `wild_coffee_feature` (48, 6, 4); placed `rusticdelight:wild_*_placed` with
  the config chances (`config/rusticdelight-common.toml` lines 11-17: cotton 96, peppers 15, coffee 15). Bone meal on
  grass in those biomes picks from FLOWER features, so it grows them.
- **Vinery grape bushes (V1):** not in `wild_crops.json` (crops.md §2.8 "Untouched: the grapes"); the port keeps the
  1.3.10 modifier and placed ids (alcohol.md P15): placed `vinery:red_grape_bush_chance`, `white_...`,
  `savanna_red_...`, `savanna_white_...`, `taiga_red_...`, `taiga_white_...`, `jungle_red_...`, `jungle_white_...`
  (crops_by_biome.md §2, generated from 1.3.10; unverified for the port jar). The pack's 6 configured overrides in
  `kubejs/data/vinery/worldgen/configured_feature/*grape_bush.json` (soil test) stay.
- **White strawberries:** done, `config/neapolitan-common.toml` line 18 is now `192` (was 256), plus a tooltip (CR-88).
- **Trader berries (still open, 2026-10-31):** [wandering_trades.js](../../kubejs/server_scripts/economy/wandering_trades.js)
  lines 37-43 sell blueberry, cranberry, sweet and glow berry pips, mulberry; not strawberry pips (`i.strawberry_pips`,
  item_dict.js 13333) or salmonberry pips (`i.salmonberry_pips`, 11434).

### Build
1. **Trader:** add `i.strawberry_pips, i.salmonberry_pips` to the seeds `t.sell` (line 37-43), same price (1 emerald,
  12 uses). Update [wandering_trader.md](../wandering_trader.md) "Seeds, crop starters" row.
2. (Done 2026-10-22: wild flax stays off through a disable-only `flax` key; RD patches overridden on RD's own
   configured ids; Vinery grapes as keys `grape`, `grape_savanna`, `grape_taiga`, `grape_jungle`.)
5. **Coffee:** nothing to build (merged, item_merges.md 2.8); drop FR coffee from the wandering_trades.js comment and
   mark V5 / V18 resolved in crops.md.
6. (White strawberries: done 2026-10-22, height 192.)
7. Update crops.md V1, V5, V6, V7, V18 and §2.7 "Others" (flax row).

### Ids and data
Verified 2026-10-31: `i.strawberry_pips` = `neapolitan:strawberry_pips` (13333), `i.salmonberry_pips` =
`delightful:salmonberry_pips` (11434), `i.coffee_beans` (13350), `i.farmersrespite_coffee_beans` (13549). Built-part ids
(reference): `i.wild_flax` 3816, `i.wild_bell_peppers` / `wild_coffee` / `wild_cotton` 13356-13358.

### Decisions
- FR coffee: decided 2026-10-22 (merged into Rustic's).
- Two apples (V5): nothing to merge (same `minecraft:apple`); recommendation keep the FD apple sapling at the
  Winemaker and close the apple half.

### Gotchas
- `wild_crops.js` skips a feature whose mod is not loaded, but a typo in a placed id stops the world loading: run
  `tools/test_wild_crops.sh` (it knows the feature ids) before rejoining.
- Removing a trader exclusion: keep the comment at wandering_trades.js 34-36 in step with what is sold.

### Tests
- **CR-84 Trader berries** (proposed): `/summon wandering_trader` a few times -> strawberry and salmonberry pips appear in the
  common pool.
- CR-86, CR-87, CR-88 (built parts) are in crops.md; CR-85 was dropped with the flax decision.

---

## wild-crops #5: Third, more common cantaloupe wild patch

Status Planned · Work data · TODO `farming/wild-crops #5`

### Goal
A third cantaloupe patch, the most common of the three, that places both the Cantaloupe and the Mini Cantaloupe.

### Now
- After the cantaloupe merge (`merges/fruit #1`, [item_merges.md](../item_merges.md) 2.10, built 2026-10-28) two rules
  share one biome set (`warm desert` + `warm badlands`: 8 deserts, 7 badlands in biome_traits.json):
  - key `hamimelon`, label "Cantaloupe": `creative_delight:wild/hamimelon` (random_patch 24 tries, xz 5, y 3,
    `simple_block fruitsdelight:hamimelon` on sand / red sand / terracotta / `#creativeengineering:worldgen/grass_like_soils`),
    placed rarity 64, MOTION_BLOCKING; disables `fruitsdelight:hamimelon`.
  - key `cantaloupe`, label "Mini Cantaloupe": `creative_delight:wild/mini_cantaloupe` (same shape, places
    `delightful:cantaloupe`), placed rarity 64; disables `delightful:cantaloupe`.
- crops_by_biome.md §1: Cantaloupe 1.0 per 1000 (x2.47), Mini Cantaloupe 1.0 (new). The Mini Cantaloupe used to be
  `warm dry grassy` (12 savanna / shrubland biomes) with Delightful's own patch.

### Build
1. Configured `kubejs/data/creative_delight/worldgen/configured_feature/wild/cantaloupe_mixed.json`: copy of
   `wild/hamimelon.json` with `to_place` a `minecraft:weighted_state_provider` of `fruitsdelight:hamimelon` and
   `delightful:cantaloupe` (weights per Decisions), maybe more tries (32) so it reads as a field.
2. Placed `placed_feature/wild/cantaloupe_mixed.json`: rarity below 64 (Decisions), in_square, MOTION_BLOCKING, biome.
3. Rule `cantaloupe_mixed` in `wild_crops.json` (label "Cantaloupe (mixed)", `features` the new id, no
   `disable_modifiers`), `where` per Decisions; one `notes` line.
4. Generator: `crops_in_value` (gen_crop_worldgen_table.py line 380) walks every `Name` key (line 407), so a weighted
   provider counts for both crops (both blocks are mapped at line 252). While there: `JAVA_PLACED` line 204 still labels
   `fruitsdelight:hamimelon` "Hamimelon", which is why crops_by_biome.md §1 shows a "Hamimelon ... gone" row next to
   Cantaloupe; relabel it "Cantaloupe".
5. Rerun the generator and `tools/test_wild_crops.sh`; update crops.md §2.8 and item_merges.md 2.10 worldgen line.

### Ids and data
`fruitsdelight:hamimelon` (block), `delightful:cantaloupe` (block, Delightful is a CD member); new
`creative_delight:wild/cantaloupe_mixed`, tag `creative_delight:wild_crops/cantaloupe_mixed`.

### Decisions
- Biomes: recommendation the old Mini Cantaloupe range `warm dry grassy` (savannas, shrublands) so the common patch
  is where players meet it first, leaving the two single patches in deserts and badlands. Alternative: same 15 biomes,
  just denser.
- Rarity: recommendation 24 (about 2.5x the single patches); weights 1 Cantaloupe : 2 Mini.
- Whether "normal and mini variants" means one mixed patch (above) or two new patches: recommendation one mixed patch.

### Gotchas
- The Mini Cantaloupe Plant still needs sand next to water to be planted (Delightful's rule, item_merges.md 2.10); the
  patch places fruit blocks, not plants.
- New chunks only.

### Tests
- CR-new1 (proposed, numbered when queued): `/place feature creative_delight:wild/cantaloupe_mixed` on savanna grass x5
  -> both blocks appear; biome tag `creative_delight:wild_crops/cantaloupe_mixed` lists the chosen biomes.
- CR-new2 (proposed): fly a new savanna 30 chunks -> mixed patches more often than the desert single patches.

---

## wild-crops #6: Wild sea grape under the pack's placement system

Status Planned · Work data / KubeJS · TODO `farming/wild-crops #6` (decided 2026-10-25, [ocean_delight.md](../ocean_delight.md) 3.1)

### Goal
Wild sea grape is placed by a `wild_crops.json` rule with a density target, using the newer jar's 64 / 6 patch, and
the mod's own modifier is off so it never generates twice (DA-14).

### Now
- Installed Oceanic Delight 1.0.1 is repacked inside Creative Delight (`development/creativeDelight/members/oceanic`,
  upstream `development/creativeDelight/upstream/oceanic_delight-1.0.1-forge-1.19.2.jar`). Its data (verified in the jar):
  - modifier `oceanic_delight:patch_wild_sea_grape_biome_modifier` (`forge:add_features`, biomes `deep_lukewarm_ocean`,
    `warm_ocean`, `lukewarm_ocean`, step vegetal_decoration);
  - configured `oceanic_delight:patch_wild_sea_grape`: `farmersdelight:wild_crop`, tries 32, xz 5, y 3, primary
    `oceanic_delight:wild_sea_grape` (waterlogged) on `#minecraft:sand`, secondary seagrass, floor clay;
  - placed `oceanic_delight:patch_wild_sea_grape`: rarity 30, in_square, OCEAN_FLOOR_WG, count 1, biome.
- The newer reference jar `devmods/createiveDelight/oceanic_delight-1.0.4-forge-1.20.1.jar` has the same modifier and
  placed feature; only the configured patch changes to tries 64, xz 6, y 3.
- No sea grape rule in `wild_crops.json`; the generator does not list sea grapes (crops_by_biome.md has no row).
- Ocean biomes carry only the trait `ocean` in biome_traits.json (9 vanilla oceans), and wild_crops.js `never` skips
  `ocean` and `cave`, so a `where` clause cannot reach them; `add` bypasses `never` (_biome_rules.js line 66).

### Build
1. Configured `kubejs/data/creative_delight/worldgen/configured_feature/wild/sea_grape.json`: the 1.0.1 json with
   `tries` 64, `xz_spread` 6 (written fresh in the pack; no conversion).
2. Placed `placed_feature/wild/sea_grape.json`: rarity N (start 30, the mod's), in_square, heightmap OCEAN_FLOOR_WG,
   biome (drop the `count 1`).
3. Rule `sea_grape` in `wild_crops.json`: `"label": "Sea Grape", "where": [], "add": ["minecraft:warm_ocean",
   "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"], "features": ["creative_delight:wild/sea_grape"],
   "disable_modifiers": ["oceanic_delight:patch_wild_sea_grape_biome_modifier"]`, plus `target_per_1000` once #1 adds
   the key. Using `add` avoids touching `never`.
4. Generator: oceans are dropped from the share (`NEVER` line 486, `oceans` line 617; "surface mode skips ocean").
   Give an OCEAN_FLOOR_WG feature a `sea floor` layer that counts ocean area, or the row shows 0 per 1000.
5. Run `tools/test_wild_crops.sh` (RhinoWildCrops.java runs the applier; it has no feature id list to extend).
6. Rerun the generator; update ocean_delight.md 3.1 (row built) and crops.md §2.8.

### Ids and data
Verified: `oceanic_delight:wild_sea_grape` (block), `oceanic_delight:patch_wild_sea_grape` (configured + placed),
modifier id above; `farmersdelight:wild_crop` feature type (FD 1.19.2). New `creative_delight:wild/sea_grape`, tag
`creative_delight:wild_crops/sea_grape`.

### Decisions
- Density target: recommendation keep about the mod's rate (rarity 30) with the bigger patch; set the number after the
  generator has a sea-floor row.
- Ocean fit: recommendation warm and lukewarm only (the mod's set); cold oceans have no sand-floor sea grape lore.

### Gotchas
- A `disable_modifiers` id must be the modifier file's id (`..._biome_modifier`), not the feature id.
- `farmersdelight:wild_crop` places a clay floor and seagrass around the primary block; the pack's
  `#creativeengineering:worldgen/grass_like_soils` overrides do not apply (sand only).

### Tests
- DA-14 (ocean_delight.md 3.1, proposed there): fresh warm ocean -> sea grape patches once per spot, at the rule's
  density; `/place feature oceanic_delight:patch_wild_sea_grape` still works (the feature exists, only the modifier is
  off).
- CR-new3 (proposed, numbered when queued): biome tag `creative_delight:wild_crops/sea_grape` lists the three oceans;
  no `[wild-crops]` error lines.

---

## Built, needs play

2026-10-22 (user's picks, not the recommendations above where they differ): tea to 20 biomes (#3 V20: every swamp
and jungle, the `cherry` trait, `add` dark forest, Lush Valley, Cloud Forest; density now 59 per 1000, see #1); Vinery
grapes as four keys `grape`, `grape_savanna`, `grape_taiga`, `grape_jungle` (#4 V1, modifier ids `vinery:add_*_grape`
from the 1.3.10 upstream jar; jungle grapes are the vines); wild flax **stays off** (#4 build step 2 replaced by a
disable-only `flax` key, so CR-85 is dropped); white strawberry height 192, not 160, plus a tooltip. CR-83, CR-87 in
crops.md, CR-88 new (white strawberries). Rustic Delight patches (V7) converted by overriding RD's own configured feature ids (not pack copies), so the RD placed features and config chances stay; CR-86 added. Generator now reads
`vinery_upstream/`.

Test state (doc audit 2026-10-22): `tools/test_wild_crops.sh` passes on the 11:09 `wild_crops.json` (53 crop
tags, 52 add modifiers, 54 `forge:none`); the user's last log still shows 48 crop tags, so no game has loaded the
batch. Not in the 2026-10-22 scratch run. Scratch queue SQ-WC (`tools/farm_worldgen_probe.js` `zfw wild` / `zfw straw`:
biome feature lists, bone-meal flower lists, no flax, white strawberries at Y 192); CR-83, CR-86 to CR-88 in game.

## Writeup corrections made

- [crops.md](../crops.md) §2.8 status line, "Where each crop goes" table, "Choices made" bullets, V13 note, §3 bullet,
  CR-50 and CR-55: brought in line with the 2026-10-10 `wild_crops.json` (listed in detail in
  [farming_crops.md](farming_crops.md) "Writeup corrections made"; same edits, made once).
- [wandering_trader.md](../wandering_trader.md): FR coffee "only grows in the Nether" -> its spawn is off, no source.
