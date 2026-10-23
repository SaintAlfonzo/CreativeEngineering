# Brief: farming / wild-crops

Written 2026-10-21 for the open `farming/wild-crops` rows of [TODO.md](../todo/TODO.md). Reference writeups:
[crops.md](../crops.md) §2.7, §2.8 and findings V1 to V20 (lines 824-891), and the generated
[crops_by_biome.md](../crops_by_biome.md) (generated 2026-10-10 from the current rules). Test prefix **CR-**; the new
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
| wild-crops #4 | Sources and duplicates (V1, V5, V6, V7, V18, white strawberries, trader berries) | data / KubeJS / config | A (trader, flax, RD patches), C (coffee) |

Order: **A** quick data fixes that need no decisions (classify the missing cave biomes, wild flax back under the
system, Rustic Delight patches as plain patches, trader berries); then **B** in one pass because both edit
`wild_crops.json` + `kubejs/data/creativeengineering/worldgen/*/wild/` and both are judged by the generator's density
table (add the target column first); then **C**, the choices that wait on other rows: V16 waits on
`ports/autumnityport #2` (pumpkin patch / autumn biomes), V5/V18 coffee waits on `food/drinks #3` (coffee
merge), wild pineapple ground shares a tag with `farming/crops #1` G15.

Every change here is worldgen: **new chunks only**, rejoin to apply (CEConfig `[worldgen.systems] wildCrops`), and rerun
`python3 tools/gen_crop_worldgen_table.py` plus `tools/test_wild_crops.sh` after each edit. Files:
- [kubejs/generation_scripts/worldgen/wild_crops.json](../../kubejs/generation_scripts/worldgen/wild_crops.json)
  (413 lines: `_readme`, `biomes` trait map of 146 biomes, `crops` 48 rules);
- [kubejs/server_scripts/worldgen/wild_crops.js](../../kubejs/server_scripts/worldgen/wild_crops.js) (applier; tags via
  `global.tags.add` line 137, modifiers via `ServerEvents.highPriorityData` lines 141-187, unclassified-biome warning
  lines 240-244);
- pack features in `kubejs/data/creativeengineering/worldgen/{configured,placed}_feature/wild/` (6 configured,
  11 placed today);
- [tools/gen_crop_worldgen_table.py](../../tools/gen_crop_worldgen_table.py) (766 lines; `JAVA_PLACED` line 104 restates
  the Java-registered placed features; FD chances via `fd()` line 91).

---

## wild-crops #1: Densities moved by the placement system

Status Planned · Work data · TODO `farming/wild-crops #1`

### Goal
Each wild crop has a target density ("patch attempts per 1000 overworld chunks", the generator's unit), and a knob
that reaches it without touching the biome rule.

### Now
From [crops_by_biome.md](../crops_by_biome.md) §1 (2026-10-10 rules; the row's "mint x11" was the 2026-10-03 figure,
it is **x5.6** now):

| Crop | Now | Mod default | Change | Feature in `wild_crops.json` | Knob |
|---|---|---|---|---|---|
| Mint pond | 12.5 | 2.2 | x5.6 | `neapolitan:mint_pond` (Java, rarity 4) | pack placed copy |
| Pumpkin | 0.3 | 1.7 | x0.17 | `creativeengineering:wild/pumpkin` (chance 300) | edit the json |
| Sugar cane | 6.7 | 116.4 | x0.06 | `creativeengineering:wild/sugar_cane` (chance 6) | edit the json (+ count) or widen the rule |
| Persimmon tree | 0.6 | 1.4 | x0.40 | `fruitsdelight:tree/persimmon_tree` (Java, 1/50) | pack placed copy |
| Rice | 1.8 | 5.7 | x0.31 | `farmersdelight:patch_wild_rice` | FD config `[world.wild_rice] chance = 20` (farmersdelight-common.toml line 98) |
| Corn | 3.1 | 7.1 | x0.43 | `corn_delight:patch_wild_corn` (Java, rarity 30 hard-coded) | pack placed copy |
| Also low | lemon 0.7 (x0.47), mini melon 1.0 (x0.5), carrot / onion 1.0 (x0.57), kiwi / orange 1.1 (x0.59) | | | | FD / Delightful configs, pack copies |
| Also high | mushroom colonies 1.2 (x7), cantaloupe 1.7 (x5.4), fig 4.5 (x2.3), hamimelon 0.9 (x2.5) | | | | |

Per 1000 = attempts per chunk x share of land area x 1000; it is an upper bound (ground tests can still fail).

### Build
1. **Targets in the config.** Add an optional `"target_per_1000"` to each crop in `wild_crops.json` (document it in
   `_readme`). In the generator, print a "Target" and "Chance for target" column in §1: for a single rarity-filtered
   feature, `chance_needed = current_chance x now / target`. `wild_crops.js` ignores the key.
2. **Pack placed copies** for Java placed features (one json each in
   `kubejs/data/creativeengineering/worldgen/placed_feature/wild/`), then swap the id in the crop's `features`:
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
   'Persimmon (tree)', ...}` fallback next to `JAVA_PLACED` (line 104) and consult it in `crops_in_value`.
6. Rerun the generator and `tools/test_wild_crops.sh`; update crops.md V13 to the new numbers and §2.8's "Pack
   features" table (add the new copies).

### Ids and data
Configured features (verified ids): `neapolitan:mint_pond`, `corn_delight:patch_wild_corn`,
`fruitsdelight:tree/persimmon_tree_wild` (pattern `tree/<name>_tree_wild`). Placed ids today: `neapolitan:mint_pond`,
`corn_delight:patch_wild_corn`, `fruitsdelight:tree/persimmon_tree`. New pack ids `creativeengineering:wild/mint_pond`,
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
- **CR-79 Densities:** new world, `/locate biome terralith:highlands` and fly 20 chunks -> mint ponds are occasional
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
   - `wild_crops.json` key `pineapple`: `features` -> `creativeengineering:wild/pineapple` (keep
     `disable_modifiers: ["fruitsdelight:pineapple"]`), update its `notes`.
   - The wild block still needs the tag to survive, so to reach jungle grass or mangrove mud the tag must grow:
     `t.add(e, 'fruitsdelight:pineapple_grows_on', [item.grass_block, item.mud])` in
     [blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) (keys exist: `i.grass_block` 2308, `i.mud` 645). This
     also lets players plant pineapple saplings on grass and mud: decide together with `farming/crops #1` G15.
2. **Cabbage / beet cliffs:** either drop the snowy and basalt cliffs from the rule (`"cliff !snowy"` and
   `except: ["terralith:basalt_cliffs"]`), or accept. Check the trait names in `biomes` first.
3. **Sugar cane:** `except: ["minecraft:frozen_river", "terralith:gravel_beach"]` on the `sugar_cane` rule (pair it with
   the #1 widening to swamps / oases).
4. **Melons:** accept (mangrove has some grass), or `except: ["minecraft:mangrove_swamp"]` for `melon` and
   `mini_melon`.
5. Regenerate; update crops.md V14 / V15 and the §2.8 tables.

### Ids and data
`fruitsdelight:wild_pineapple` = `i.wild_pineapple` (item_dict.js 12902), tag `fruitsdelight:pineapple_grows_on`
(verified contents), new `creativeengineering:wild/pineapple`. Biome ids as listed (in `wild_crops.json` `biomes`).

### Decisions
- Pineapple on grass / mud: recommendation yes for the wild block only is impossible (one tag serves both), so: add
  grass + mud to the tag and accept saplings planting there; it is a tropical bush, grass is believable.
- Cliffs, cane, melons: recommendation prune cane (zero yield there), prune frozen + basalt cliffs, accept melons.

### Gotchas
- `simple_block` checks `canSurvive` before placing, so the predicate filter alone is not enough: the tag is what lets
  the wild pineapple stand on grass.
- Removing biomes from a rule changes the density numbers of #1; do #1 and #2 in one regeneration.

### Tests
- **CR-80 Pineapple ground:** `terralith:tropical_jungle` and `mangrove_swamp` -> wild pineapples on grass / mud as
  well as sand. `/place feature creativeengineering:wild/pineapple` on stone -> fails.
- **CR-81 Cane banks:** `frozen_river` and `terralith:gravel_beach` -> no cane attempts (generator shows them gone);
  `river` -> cane.

---

## wild-crops #3: Biome gaps and choices

Status Planned · Work data · TODO `farming/wild-crops #3`

### Goal
Every land biome either has wild crops or is a deliberate "none"; the choices in V3, V4, V16, V20 are made.

### Now
- **Unclassified cave biomes (plan_1.0.5.md §2):** `wild_crops.js` warns about overworld biomes missing from `biomes`.
  Terralith 2.3.12 ships 14 cave biomes; `wild_crops.json` lists 11 of them. Missing (verified by diff with the jar):
  `terralith:cave/crystal_caves`, `terralith:cave/desert_caves`, `terralith:cave/ice_caves`. The 1.0.5 note said 5; the
  other two are not Terralith caves (read the warning line in `logs/latest.log`:
  `[wild-crops] N overworld biomes are not in ... and get no wild crops: ...`). The generator says "every overworld
  biome classified" because `kubejs/exported/biome_data.json` (from `/dumpbiomes`) predates them or lacks them;
  refresh it.
- **V19:** 13 land biomes match no rule (frozen / jagged / stony / emerald / volcanic peaks, volcanic crater, snowy slopes,
  ice spikes, glacial chasm, gravel desert, windswept gravelly hills, stony and windswept spires), crops_by_biome.md §6.
  All rock, ice or gravel.
- **V16:** pumpkin "orchard" = Terralith Skylands Autumn only. A real pumpkin patch / autumn biome is
  `ports/autumnityport #2`.
- **V20:** tea `temperate wet grassy` = swamp, Blooming Valley, Mirage Isles.
- **V4:** jungles carry the whole warm-wet group plus bananas, lemons and five fruit trees, plus rubber trees.
- **V3:** carrot and onion share one rule (`temperate grassy !swamp`, 25 biomes).

### Build
1. Add the three cave biomes to `biomes` with the trait `cave` (and their climate, for consistency): e.g.
   `"terralith:cave/ice_caves": "cave cold icy"`, `"terralith:cave/desert_caves": "cave warm dry sandy"`,
   `"terralith:cave/crystal_caves": "cave"`; add the other two from the log. Caves never match a rule (`NEVER`, line 35).
2. V19: mark them deliberate in `_readme` (one line) and in crops.md V19. Optional: windswept gravelly hills could take
   the cold grassy set if Terralith gives it grass patches (check §5 of crops_by_biome.md); recommendation leave.
3. V20 tea: `add: ["terralith:lush_valley", "minecraft:dark_forest"]` to the `tea` rule (the two downfall-0.8 biomes
   V20 names), or a new `"temperate grassy forest flower"` clause. Recommendation: the explicit `add`.
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
- V20 tea biomes: recommendation add Lush Valley and Dark Forest.
- V16: wait for `ports/autumnityport #2`.

### Gotchas
- A biome tagged `cave` gets nothing even if it is reachable from the surface (Terralith Underground Jungle is
  already `cave`).
- Moving a crop changes its density; do it in the same regeneration as #1.

### Tests
- **CR-82 No warning:** single-player new world -> `latest.log` has no `[wild-crops] ... not in` warning.
- **CR-83 Tea spread:** `terralith:lush_valley` -> wild tea bushes with ferns.

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
- **Coffee (V5 / V18):** `rusticdelight:coffee_beans` (`i.coffee_beans`, item_dict.js 12982; wild in the 11 warm-wet
  biomes) and `farmersrespite:coffee_beans` (`i.farmersrespite_coffee_beans`, 13125). FR's only source was its Basalt
  Deltas bush, switched off by the `coffee` key's `disable_modifiers: [..., "farmersrespite:coffee_bush"]`; the trader
  never sells it (wandering_trades.js line 35). The item merge is `food/drinks #3` ("Coffee (Rustic Delight vs Farmer's
  Respite)").
- **Wild flax (V6):** `config/supplementaries-common.toml` `[functional.flax.wild_flax] enabled = false`
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
- **White strawberries:** `config/neapolitan-common.toml` line 18 `"White Strawberry minimum height" = 256`. Strawberries
  now spawn only in 6 Terralith highland biomes (`cold grassy mountain !snowy`), so white ones essentially never exist.
- **Trader berries:** [wandering_trades.js](../../kubejs/server_scripts/economy/wandering_trades.js) lines 34-43 sell
  blueberry, cranberry, sweet and glow berry pips, mulberry; not strawberry pips (`i.strawberry_pips`, item_dict.js
  12965) or salmonberry pips (`i.salmonberry_pips`, 11351).

### Build
1. **Trader:** add `i.strawberry_pips, i.salmonberry_pips` to the seeds `t.sell` (line 37-43), same price (1 emerald,
  12 uses). Update [wandering_trader.md](../wandering_trader.md) "Seeds, crop starters" row.
2. **Wild flax under the system:** keep the Supplementaries config off; add a `flax` key to `wild_crops.json`:
   `{"label": "Flax", "where": ["temperate river", "temperate sandy beach"], "features": ["supplementaries:wild_flax"],
   "disable_modifiers": ["supplementaries:wild_flax"]}` (check trait names; the feature needs sand next to water).
   Disabling the modifier is harmless while the tag is empty and keeps it off if someone flips the config.
3. **Rustic Delight as plain patches (V7):** pack configured `wild/cotton`, `wild/bell_peppers`, `wild/coffee`
   (`minecraft:random_patch`, the tries / spreads above, `simple_block` of `rusticdelight:wild_cotton` /
   `wild_bell_peppers` / `wild_coffee`) and placed copies (rarity_filter 96 / 15 / 15, in_square, heightmap
   MOTION_BLOCKING, biome); swap the three `features` in `wild_crops.json`. Note in §2.8 that the RD config chances no
   longer apply (same as the FD cabbage note).
4. **Vinery grapes (V1):** add keys `grape_red`, `grape_white` (temperate grassy), `grape_savanna_red/_white`
   (warm dry grassy), `grape_taiga_red/_white` (cold forest), `grape_jungle_red/_white` (jungle) with the placed ids
   above in `features` and the port's modifier ids in `disable_modifiers`. Read the exact modifier ids from the built
   port jar first: `unzip -l mods/backportedVinery-*.jar | grep forge/biome_modifier` (the port's data is generated from
   upstream by `UpstreamTranslator`, so it is not in the source tree).
5. **Coffee (V5 / V18):** if `food/drinks #3` merges FR coffee into RD coffee (recommended there), FR beans become a
   recipe alias and no spawn is needed; else remove `farmersrespite:coffee_bush` from the `coffee` key's
   `disable_modifiers` to bring back the Basalt Deltas bushes (Nether), and drop the trader exclusion.
6. **White strawberries:** lower the Neapolitan height to the highland peaks (e.g. 160) in
   `config/neapolitan-common.toml` (+ `configs_edited/` copy); this is a growth rule, not worldgen, so it also applies to
   farmed bushes.
7. Update crops.md V1, V5, V6, V7, V18 and §2.7 "Others" (flax row).

### Ids and data
Verified: `supplementaries:wild_flax` (placed, configured, block; `i.wild_flax` 3727), `rusticdelight:wild_cotton`,
`wild_bell_peppers`, `wild_coffee` blocks (`i.wild_cotton`, `i.wild_bell_peppers`, `i.wild_coffee`, item_dict.js
12988-12990), RD placed `rusticdelight:wild_cotton_placed`,
`wild_bell_peppers_placed`, `wild_coffee_placed`. Unverified for the port: the Vinery placed / modifier ids.

### Decisions
- FR coffee: merge (wait for `food/drinks #3`, recommended) or respawn in Basalt Deltas.
- Wild flax biomes: recommendation temperate rivers and sandy beaches (it needs sand beside water).
- RD patches: recommendation convert to plain patches (bone-meal farming of wild seeds is not needed; the seeds are
  farmable).
- Grapes into the system: recommendation yes, with Terralith look-alikes; keep the mod rates.
- White strawberry height: recommendation 160 (reachable on the highland biomes that now carry strawberries).

### Gotchas
- `wild_crops.js` skips a feature whose mod is not loaded, but a typo in a placed id stops the world loading: run
  `tools/test_wild_crops.sh` (it knows the feature ids) before rejoining.
- The generator only sees Supplementaries' runtime tag as empty; with the `flax` key it will finally count flax (the
  "flax reaches no biome" note goes away).

### Tests
- **CR-84 Trader berries:** `/summon wandering_trader` a few times -> strawberry and salmonberry pips appear in the
  common pool.
- **CR-85 Wild flax:** temperate river with sandy banks -> wild flax beside the water.
- **CR-86 No bone-meal cotton:** bone-meal a large grass area in `savanna` many times -> flowers only, no wild cotton.
- **CR-87 Grapes in Terralith:** a Terralith temperate biome (e.g. `terralith:lavender_forest`) -> red / white grape bushes.
- CR-46 ("no wild flax") and CR-43 (bone-meal peppers) change meaning once 2 and 3 are built; update them.

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
