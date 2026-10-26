# Veggies Delight: backport and implementation description

> **Merge note (2026-10-25):** this port is to be merged with the other Farmer's Delight add-on backports into a single jar, **Creative Delight**. The inventory summary, cross-port interactions and the merge process are in [creative_delight.md](creative_delight.md). This file stays the detailed record for Veggies Delight; its registry namespace and test prefix do not change.

Plan written 2026-10-22; implementation decided by the user the same day (§4, §5). **BUILT 2026-10-22** (port jar in
`mods/`, pack integration in scripts and data; §4.0 lists what is done and what waits for an in-game dump). Veggies Delight
(mehdinoui) has no 1.19.2 build: the newest are 1.9.2 for Forge 1.20.1 and 1.9.3 for NeoForge 1.21.1, with identical
content (same lang keys, same blockstates). So getting it into the pack means our own port, like Backported Vinery and
Backported Brewin ([alcohol.md](alcohol.md)), and then the same integration every other crop and food in the pack has had.

This file has four parts:

- **Part 1 (section 2):** everything in the mod, from the 1.20.1 jar and its decompiled code (reference).
- **Part 2 (section 3):** where it overlaps the pack: what the user decided for each overlap, and the overlaps that
  remain after those decisions.
- **Part 3 (section 4):** the implementation description: what the port keeps, changes and drops, and how each piece
  goes into the pack through the existing tools, in build order.
- **Part 4 (section 5):** marked merges and pinned work. The marked merges stay in game, unmerged, so they can be
  compared before deciding.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Mod inventory (7 crops, 8 wild plants, 72 items, 29 blocks, 215 recipe files, worldgen, loot, village houses, trades, mob food, config) | §2 | reference 2026-10-22, from `VeggiesDelight-1.20.1-1.9.2.jar` |
| Overlap decisions and the overlaps that remain | §3 | decided 2026-10-22 (user); 10 remaining overlaps listed in §3.3 |
| Port jar `backportedVeggies-1.9.2-ce-19.2-01.jar` | §4.1 | BUILT 2026-10-22, installed in `mods/`; not yet loaded in game |
| Implementation description (port, wild plants, mature dandelion, seasons, food, cooking rules, baking, drinks, jars, mob food, loot, houses, trades, compost, UI) | §4 | BUILT 2026-10-22 except the dump-driven steps (§4.0) |
| Marked merges and pins (Fish and Chips, Mashed Potatoes, Stuffed Bell Peppers; Cultural Delight; Vintage and Cultural recipes; doughs; mob food; seasons; tea audit) | §5 | marked 2026-10-22 |
| Tests | §8 | VD-01 to VD-41 drafted (not all numbers used) |

- **Source to port from:** `~/.local/share/atlauncher/downloads/VeggiesDelight-1.20.1-1.9.2.jar` (Forge, closest API to
  1.19.2). `DecompiledMods/VeggiesDelight-1.21.1-1.9.3.jar` is the NeoForge build, not decompiled, same content.
  Re-decompile with `python3 tools/decompile_mod.py <jar> <out dir>` (36 classes).
- **Licence:** MIT (owner-supplied 2026-10-25; `mods.toml` agrees) for the whole mod (code and assets; unlike Vinery's split licence there is no
  separate assets clause). A GPLv3 port may include MIT code and assets as long as the MIT notice is kept.
- **Dependencies:** Forge and Farmer's Delight only (`farmersdelight [1.2.3,)`). Every FD type it uses exists in the
  pack's FD 1.19.2-1.2.4: `WildCropBlock` / `wild_crop` feature, `add_features_by_filter` biome modifier,
  `add_loot_table` and pastry-slicing loot modifiers, `PieBlock`, `FeastBlock`, cutting and cooking recipes.
- **Nothing in the pack references it today:** no jar in `mods/` ships `veggiesdelight` data or recipes.

## 1. Overview

Veggies Delight is a Farmer's Delight add-on: seven new crops (bell pepper, broccoli, cauliflower, garlic, sweet potato,
turnip, zucchini), their wild forms, a harvestable "mature dandelion", about 60 foods built on FD's pot, cutting board
and feast blocks, crates, a depot house in each vanilla village style, chest loot, farmer and wandering trader trades,
and mob food. All its crops are plain `CropBlock`s, so most of the pack's crop machinery already applies to them as is.

In the pack: six of the seven crops (bell pepper is Rustic Delight's), every food, the mature dandelion and the depot
houses. Its own worldgen, loot, trade, house-weight and compost hooks are replaced by the pack's tools; advancements
are dropped.

## 2. Part 1: everything in the mod

### 2.1 Crops

| Crop | Planted from | Block, max age | Harvest drops (ripe) | Seasons (Serene Seasons tags it ships) |
|---|---|---|---|---|
| Bell pepper | Bell Pepper Seeds | `bellpepper_crop`, 7 | seeds + bell pepper (binomial, fortune) | summer, autumn |
| Broccoli | Broccoli Seeds | `broccoli_crop`, 5 | seeds + broccoli | spring, autumn |
| Cauliflower | Cauliflower Seeds | `cauliflower_crop`, 7 | seeds + cauliflower | autumn, winter |
| Garlic | Garlic Clove (no seeds) | `garlic_crop`, 5 | garlic | spring, summer, autumn |
| Sweet potato | Sweet Potato (no seeds) | `sweet_potato_crop`, 5 | sweet potato | autumn, winter |
| Turnip | Turnip Seeds | `turnip_crop`, 5 | seeds + turnip | spring, autumn, winter |
| Zucchini | Zucchini Seeds | `zucchini_crop`, 7 | seeds + zucchini | summer |

- Every crop class `extends CropBlock` and overrides only the age property: no own soil check. Zucchini is the one
  exception with its own right-click: a ripe plant pops 1 or 2 zucchinis and drops back 3 ages, like a berry bush
  (found in the port, 2026-10-22). They use Forge `canSustainPlant`, so Rich Farmland and Fertile Soil already take them, Quark replants
  them on right-click and with the hoe, and WTHIT's growth line covers them natively.
- Seeds from produce: bell pepper and zucchini give seeds by hand crafting. All seed and produce items get
  `#forge:seeds/*`, `#forge:crops/*`, `#forge:vegetables/*` tags. Note the spelling `bellpepper` in every VD tag.

### 2.2 Wild plants and worldgen

Eight wild plants. Seven `wild_*` blocks are `FlowerBlock`s; the mature dandelion is its own class. Each patch is an FD
`wild_crop` configured feature (the wild plant on a floor of Coarse Dirt over `#minecraft:dirt`) placed with VD's own
`veggiesdelight:configurable_rarity_filter`, whose chance comes from the VD config (`chance*` keys).

| Wild plant | Biome modifier (FD `add_features_by_filter` unless noted) | Planting item it drops / cutting board |
|---|---|---|
| Wild Bell Peppers | `#forge:is_hot/overworld` | seeds; knife: seeds |
| Wild Broccoli | overworld, temperature 0.4 to 0.7 | seeds; knife: seeds |
| Wild Cauliflowers | overworld, 0.2 to 0.69 | seeds; knife: seeds |
| Wild Garlic | overworld, 0.3 to 0.8 | garlic clove; knife: cloves; white dye by hand |
| Wild Sweet Potatoes | overworld, 0.2 to 0.6 | sweet potato; knife: sweet potato |
| Wild Turnips | `minecraft:beach` only (`forge:add_features`) | seeds; knife: seeds |
| Wild Zucchinis | `#forge:is_hot/overworld` | seeds; knife: seeds |
| Mature Dandelion | `#forge:is_plains` | dandelion leaf; knife: leaves; yellow dye by hand |

Breaking one (checked for turnip, garlic, sweet potato and dandelion) drops the planting item, plus a 20 % roll of the
produce (turnip, garlic clove, sweet potato, leaf); shears give the block itself instead. All exclude `#forge:is_underground` and generate at `vegetal_decoration`. Bone meal on a **vanilla dandelion is not
touched**; bone meal on a Mature Dandelion pops another Mature Dandelion item (`DandelionBlock.performBonemeal`, always
succeeds). The mature dandelion also gives Luck as a suspicious-stew flower.

### 2.3 Items and blocks (72 items, 29 blocks)

**Produce and ingredients (24):** bell pepper, broccoli, cauliflower, cauliflower floret, garlic, garlic clove,
sweet potato, turnip, zucchini, zucchini slice, dandelion leaf; seeds for bell pepper, broccoli, cauliflower, turnip,
zucchini; roasted cauliflower floret, roasted garlic clove, roasted zucchini, baked sweet potato,
sweet potato dough, potato noodle, raw vegetarian patty, cooked vegetarian patty.

**Meals and food (40):**

| Group | Items |
|---|---|
| Bowl meals (FD pot) | broccoli soup, cauliflower soup, garlic chicken stew, turnip beef stew, cacciatore, mashed potatoes, potato noodles, cauliflower kuku, turnip cake, rice and vegetables, roasted vegetables, shakshouka, pasta with broccoli, stuffed zucchinis, fish and chips |
| Bowl meals (grid) | broccoli salad, Cesar salad, coleslaw, turnip salad, dandelion greens and eggs, garlic rice with cauliflower, steak and broccoli, garlic baked cod |
| Handheld (grid) | garlic bread, cauliflower bread, garlic stuffed mushrooms, turnip mutton skewer, chicken fajitas wrap, vegetables wrap, vegetarian burger, zucchini sandwich, stuffed zucchini boat, uncooked mhadjeb, mhadjeb (smelted) |
| Sweets | beetroot brownie, sweet potato cupcake, sweet potato pancakes |
| Drinks (pot, glass bottle) | carrot juice (Night Vision 20 s), dandelion juice (Regeneration 20 s), turnip water (Resistance 20 s), fermented garlic honey (grid, Regeneration + Health Boost 5 s) |
| Slices and servings | carrot cake slice, sweet potato pie slice, vegan pizza slice, zucchini quiche slice, lasagna slice, stuffed bell pepper |

**Placed blocks (29):** 7 crops, 8 wild plants, 7 crates (FD style, 9 produce each), and 7 placeable foods:
Carrot Cake (`CakeBlock`, 7 slices), Sweet Potato Pie and Zucchini Quiche (`PieBlock`), Vegan Pizza (`PieBlock`
subclass), Beetroot Brownie Tray (`PieBlock`), Lasagna Pan and Stuffed Bell Peppers (`FeastBlock`, served with a bowl).
Pie, cake and tray slicing also works with a knife on the placed block through FD's pastry-slicing loot modifier.

**Food values (nutrition / saturation modifier, effects).** Raw produce 1 to 4 / 0.1 to 0.4; garlic clove, cauliflower
floret and the roasted bits are fast-eating snacks. Handhelds 5 to 12 / 0.4 to 0.8. Bowl meals 8 to 14 / 0.6 to 0.8 with
FD Comfort (30 s to 5 min) or Nourishment (30 s to 5 min); stuffed bell pepper 8 / 0.65 with 5 min Nourishment. Sweet
potato pancakes are 12 / 0.8 with Speed and 3 min Comfort. Exact values: `ModFoods.java` in the decompile.

### 2.4 Recipes (215 files)

| Set | Count | Types |
|---|---|---|
| Core | 113 | 38 shapeless, 14 shaped, 23 FD cooking pot, 16 FD cutting board, 7 smelting, 8 smoking, 7 campfire |
| Compat, mods in the pack | 18 | Corn Delight 5 (cornbread stuffing, turnip cake, wraps on the tortilla), More Delight 12 (garlic in its creamy pastas and diced potato meals, veggie patty in its burgers, garlic bread on toast, bread slicing), FD 1 (a second vegetable soup recipe) |
| Compat, mods not in the pack | 38 | Vintage Delight 22 (live if Vintage is ported: [vintage_delight.md](vintage_delight.md) §3.3), Cultural Delights 15, Ube's Delight 1 |
| Integration, mods in the pack | 31 | Create 12 (milling wild plants to dye and seeds, mixing), IE 19 (cloche 7, crusher 7, squeezer 5 for plant oil) |
| Integration, not in the pack | 14 | Botany Pots |

Recipe habits worth knowing for §3: pot meals take raw meat and raw vegetables; pies take FD pie crust; breads and
dough take `#forge:grain/wheat`; sweet potato dough has a water-bucket and an egg route; drinks are pot recipes with a
glass bottle; fish and chips is a pot recipe from raw fish, potato and egg.

### 2.5 Loot, structures, trades, mobs

- **Chest loot:** `farmersdelight:add_loot_table` modifiers add VD tables to abandoned mineshaft, pillager outpost,
  shipwreck supply, simple dungeon and the plains, taiga, snowy, desert and savanna village houses. The five
  `vd_wagon_*` tables are the depot houses' own chests (corrected 2026-10-22: the earlier note said four, unused).
- **Village houses:** a "depot house" in each of the 5 vanilla village styles, added to the house pools at
  `ServerAboutToStartEvent` (`VillageStructures`, config switch). Compat houses ship for Windswept, Nature's Spirit,
  Explorify, Atmospheric and Repurposed Structures; none of those mods are in the pack.
- **Trades (config switch):** farmer buys 16 to 26 of each produce for 1 emerald; the wandering trader sells each seed,
  the garlic clove, the sweet potato and the dandelion leaf for 1 emerald.
- **Mobs:** chickens eat the five seeds, parrots tame with four of them, pigs eat bell pepper, cauliflower (and
  floret), sweet potato, turnip, zucchini (and slice); villagers pick up the produce, seeds and cauliflower bread
  (`WANTED_ITEMS`). Whether 1.19.2 farmers then plant VD seeds is unchecked.
- **Compost:** values for every crop, seed and food at common setup (`ModComposterEvents`).
- **Advancements:** 10 (root, first veggie, all veggies, one per crop). The pack drops every advancement at load.
- **Tags for other mods:** Diet, Naturalist, Tinkers', Alex's Mobs, Create Crafts & Additions, Serene Seasons, FD
  (`farmersdelight:wild_crops`, knife `mineable`). Only Serene Seasons, Alex's Mobs and C&A are in the pack.

### 2.6 Config (`veggiesdelight-common.toml`)

`chance<Crop>` per wild patch (rarity), `generateVanillaVillageStructures` plus one switch per compat village mod,
`enableVillagerTrades`, `enableWanderingTraderTrades`.

### 2.7 Upstream 1.9.3 against the ported 1.9.2 (evaluated 2026-10-25)

Jars: `devmods/checkNewerVersionForContent/VeggiesDelight-1.20.1-1.9.3.jar` against the port source
`VeggiesDelight-1.20.1-1.9.2.jar` (`python3 tools/jar_diff.py`, bytecode by `javap`). 1.9.3 requires Farmer's Delight
`[1.3.1,)` (was `[1.2.3,)`); most of the delta follows FD 1.3. No item, block, lang key, food value or worldgen change.
The 1.21.1 build of 1.9.3 has the same content as the 1.20.1 one apart from the item icons in the last row.

| Kind | Change in 1.9.3 | Decision (proposed) |
|---|---|---|
| Food effect | 9 foods give FD Nourishment instead of Comfort, same durations: sweet potato pancakes (the Comfort half; Speed stays), coleslaw, broccoli soup, cauliflower soup, garlic chicken stew, turnip beef stew, cacciatore, mashed potatoes, potato noodles. FD 1.3.4 has no Comfort in any of its own foods, so this follows FD 1.3 | **skip, decided 2026-10-25** ([farmers_delight.md](farmers_delight.md) 2.5): Comfort stays on the pack's FD soups, so the port keeps Comfort. The 9 foods are Comfort in the port and in the food dump. Cans copy these effects, so a change reruns `gen_can_types.py` |
| Recipe (code) | Sweet potato dough from water is a custom recipe (`SweetPotatoDoughRecipe`, `ModRecipeSerializers`): 2 sweet potato + wheat + any `#forge:buckets/water`, gives 3 dough and returns the empty bucket. Same inputs and output as the 1.9.2 shapeless recipe | skip (no gameplay change; the pack's dough rules, §5.4, own this recipe) |
| Tags | New `farmersdelight:feasts` (blocks), `pies` (blocks, items), `snacks`, `sweets` (items); pie and feast blocks move from `forge:mineable/knife` to `minecraft:mineable/axe` | skip: FD 1.2.4 defines none of these tags and nothing in the pack reads them; knife stays right for FD 1.2.4 |
| Create milling | 10 recipes rename `processingTime` to `processing_time` (Create 6 field) | skip: the pack's Create reads `processingTime` |
| Models | 24 crop stage models use `farmersdelight:block/template_crop_cross` (FD 1.3 only) | skip |
| Models | Lasagna pan models use FD 1.3's `block/tray` texture | skip (FD 1.2.4 has only `tray_top`) |
| Models | Carrot cake slices 1 to 6 show `carrot_cake_inner` on the cut face (1.9.2 showed the top texture). The texture is already in the port jar | **carry over** (data, `port_resources.py`): an upstream visual fix |
| Structures | The 4 depot houses (plains and 3 Repurposed Structures variants) replace the vanilla horse-pool jigsaw with 2 or 3 placed untamed horses of fixed variants | skip: the pool jigsaw already spawns a horse |
| Item icons (1.21.1 build only) | The 8 wild plants and the mature dandelion get their own `item/` textures (1.20.1 uses the block texture) | optional, visual only; owner's call |

Carrot cake cut face: built 2026-10-25 (`veggiesport #3`): `port_resources.py` writes `carrot_cake_inner` into slices 1 to 6,
port `1.9.2-ce-19.2-03` (built, not in `mods/`); VD-41.

## 3. Part 2: overlap with what the pack already has

### 3.1 Item overlaps, as decided (user 2026-10-22)

| VD item(s) | Pack item(s) already there | Decision |
|---|---|---|
| Bell pepper, seeds, crop, crate, wild bell peppers | Rustic Delight red / green / yellow peppers, seeds, crop, crates, wild bell peppers (`#forge:crops/bell_pepper`); colour split CEConfig `[crop_drops.bell_peppers]` ([crops.md](crops.md) §3) | **Dropped** (overlap). Every VD input of `#forge:crops/bellpepper` or `veggiesdelight:bellpepper` takes Rustic's `#forge:crops/bell_pepper` |
| Smoked Bell Pepper | Rustic Roasted Bell Pepper (3 colours) | **Dropped** (decided 2026-10-22, user). Every VD input of the smoked pepper takes Rustic's roasted pepper of any colour |
| Stuffed Bell Peppers feast + Stuffed Bell Pepper serving | Rustic Stuffed Bell Pepper (3 colours, single items) | **Kept** (placed feast and serving), made from Rustic peppers. **Marked merge** with Rustic's (§5.1) |
| Fish and Chips | PneumaticCraft Cod 'n' Chips, named Fish and Chips by the pack, fried in the pot with cooking oil ([recipes_cooking.md](recipes_cooking.md) §2.3) | **Kept, marked merge** (§5.1) |
| Mashed Potatoes | More Delight Mashed Potatoes | **Pinned, marked merge** (§5.1); both stay |
| Sweet Potato Dough | Create dough, `#forge:dough` | **Kept as is** (VD recipes); marked for the dough unification (§5.2) |
| Carrot Juice, Dandelion Juice, Turnip Water | FD Melon Juice, Fruits Delight juices, the pack's chorus juice fluid | **Kept**, merged into the juice family as fluids like chorus juice (§4.9); marked for the tea audit (§5.2) |
| Fermented Garlic Honey | (none) | **Kept, in Vintage Delight's Pickling Jar** since 2026-10-23 (was the cooking pot; VD makes it by hand: 4 garlic cloves + honey bottle) |
| Mature Dandelion, Dandelion Leaf | Vanilla dandelion | **Kept**; new drops and placement (§4.4) |
| Carrot Cake, Sweet Potato Pie | The baking overhaul's cake and pie families ([oven.md](oven.md) §2.6) | **Folded into baking** (§4.8) |
| Chicken Fajitas Wrap, Vegetables Wrap | FD Mutton Wrap on a tortilla (pack, `dishes/meals #2`) | **Kept, tortilla required** (§4.7) |
| Vegan Pizza, Zucchini Quiche, Lasagna Pan, Vegetarian Burger, breads, Cesar Salad, crates | Brewin' Pizza / Quiche / Horror Lasagna, burger family, bread chain, FD salads, FD / Rustic crates | Kept as distinct dishes; cooking rules folded in where possible (§4.7). Remaining near-overlaps in §3.3 |
| VD Vegetable Soup recipe (FD compat) | FD Vegetable Soup, pack heatless grid route | Recipe dropped (third route to the same item) |

No pack item provides broccoli, cauliflower, garlic, sweet potato, turnip, zucchini or the dandelion leaf.

### 3.2 System overlaps, as decided

| Pack system | What VD brings | Decision |
|---|---|---|
| Wild crops (`wild_crops.json`, [crops.md](crops.md) §2.8) | 8 biome modifiers by temperature, own rarity config | Pack rules (§4.3); VD's modifiers go to `disable_modifiers` |
| Wild flowers (`wild_flowers.json`, [flowers.md](flowers.md)) | Mature dandelion patch in `#forge:is_plains` | A `flowers` entry following the dandelion (§4.4) |
| Seasons (Serene Seasons) | Its own `sereneseasons:*_crops` tags | Copied over for now; the season overhaul is still needed (§5.2) |
| Chest loot ([loot.md](loot.md)) | 9 GLM injections; 5 wagon tables for the depot houses' chests | Same additions through the pack's loot pipeline (§4.11) |
| Village houses ([structure_tuning.md](structure_tuning.md)) | 5 depot houses added in code, plus compat houses | Vanilla 5 kept, weighted by the pack's pool tools (§4.12) |
| Trades | Hard-coded farmer and wanderer trades | Same lists through `trades.js` / `wandering_trades.js` (§4.13) |
| Compost | Own values at common setup | Pack's compost rules (§4.14) |
| Advancements | 10 | Dropped from the port |
| Mob food | Chicken, pig, parrot food and villager wanted items | Copied as is; mob food overhaul noted (§5.2) |
| Recipes for absent mods | 22 Vintage, 15 Cultural, 1 Ube's, 14 Botany Pots | Not ported; Vintage and Cultural noted for later (§5.3) |

### 3.3 Overlaps that remain after the decisions

Found 2026-10-22 against the item names export (`kubejs/exported/display_names.json`). None is acted on; listed for
decision.

| # | VD item | Pack item | Overlap |
|---|---|---|---|
| R2 | Mature Dandelion | Bushier Flowers Grown Dandelion (`bushierflowers:grown_dandelion`, growth off in the pack); Flower Patch Dandelion Patch | Three "several dandelions" blocks. Only the mature one drops leaves |
| R3 | Dandelion Juice | Farmer's Respite Dandelion Tea (and its long form) | Same plant, same bottled-drink role; the tea audit (§5.2) covers both |
| R4 | Zucchini Quiche | Brewin' Quiche (the pack's one recipe cycling bacon / mushroom, [recipes_cooking.md](recipes_cooking.md) RC-61) | Same block type; zucchini could be a third quiche filling instead of its own quiche |
| R5 | Vegan Pizza | Brewin' Pizza (feast plating, `dishes/meals #5`) | Same block type, different topping; the pizza work in `meals #5` should cover both |
| R6 | Stuffed Zucchinis (pot) | Stuffed Zucchini Boat (grid), both VD | Near-duplicate inside VD itself |
| R7 | Sweet Potato Pancakes | Rustic Vegetable / Honey / Chocolate Pancakes, Supplementaries Pancake | One pancake family across three mods |
| R8 | Fermented Garlic Honey (now a pot recipe) | Brewin' keg fermentation | Named "fermented" but made without fermenting |
| R9 | Fish and Chips (VD) | Fish and Chips (PneumaticCraft, pack name) | Marked merge; until decided both show as "Fish and Chips" in JEI and are told apart only by the mod line |
| R10 | Turnip Mutton Skewer | Ender's Delight Crispy Skewer, Nether's Delight Nether Skewer | Skewer family; pattern only |
| R11 | Raw / Cooked Vegetarian Patty | FD Beef Patty, Alex's Delight Bison Patty | Patty family; burger assembly rule applies (§4.7) |

## 4. Part 3: implementation description

### 4.0 Build status (2026-10-22)

| Part | State | Where / note |
|---|---|---|
| 4.1 Port | BUILT | `development/backportedVeggies`: 89 items, Java ported from the decompile; resources from the upstream jar through `tools/port_resources.py` (MIT, kept in the jar). `./gradlew :backportedVeggies:build` passes; installed with `installToMods`. Package and ids unchanged (`net.mehdinoui.veggiesdelight`) |
| 4.2 Ids, names | BUILT | 89 keys in `item_dict.js` (Veggies Delight section; `veggiesdelight_mashed_potatoes` because More Delight's has the bare key). `name_rules.json`: Caesar Salad. Case fixes (Zucchini Seeds, "with", "and") come from the next name dump and `tools/name_audit.py` |
| 4.3 Wild crops | BUILT | 6 rules in `wild_crops.json`. No `disable_modifiers`: the port ships no biome modifiers. Turnip patch takes sand or dirt below (the pack's sea beet feature does the same). `gen_crop_worldgen_table.py` knows the 6 wild blocks; `test_wild_crops.sh` passes. `gen_soil_compat.py`: nothing to do (VD's features test `#minecraft:dirt`) |
| 4.4 Mature dandelion | BUILT | `wild_flowers.json` entry with `follows`; the generator writes the resolved list into `flower_features.json` (`follows`), `wild_flowers.js` reads it. 61 biomes (no snowy, desert, bare badlands, swamp). New loot table in the port. `test_wild_flowers.sh` passes |
| 4.5 Crops, seasons | seasons BUILT (VD's tags, bell pepper entries stripped); crops.md rows and coverage OPEN | WTHIT coverage (`/dumpcropgrowth`) needs the game |
| 4.6 Food types, cans, jars, stacks | types, jars, cans BUILT; stacks, placeables OPEN | 64 foods typed by hand in `food_types.json` (checked 2026-10-23: none missing); the 5 soups have soft fluids (`gen_soup_soft_fluids.py`). 2026-10-23: `food_catalog.py` rerun from the 10-22 dump, 24 cans generated (broccoli, cauliflower with florets, sweet potato, turnip, zucchini with slices; baked sweet potato, roasted zucchini, roasted florets; 2 pastas; 3 breads, 4 salads, 5 soups by type; sweet potato pie filling, also baked into the pie). Not canned: garlic, dandelion leaf, patties, roasted garlic, handhelds, sweets, drinks, other meals ([can_parity.md](can_parity.md), CP-27 to CP-29). `gen_stack_sizes.py`, `placeable_catalog.py` still to run |
| 4.7 Cooking rules | BUILT | (Since 2026-10-25 the grid-meal moves, heatless routes and wraps are Creative Delight QoL-5: `kubejs/data/creative_delight/kubejs/veggiesdelight/cooking.js`, ids `creative_delight:`, switch `[qol] manualOnlyRoutes`.) `cooking.js` Meals: 12 VD grid meals re-id'd `_manual_only`, 15 heatless pot-meal routes; wraps on `#forge:tortilla` (manual only) plus two sequenced assemblies; cauliflower bread in the oven (`baking.js`, on dough). Tag `#creativeengineering:roasted_bell_pepper`; baked sweet potato and roasted zucchini joined `soup_veggie_cooked`. 2026-10-23, with the Vintage port: its compat grid recipes folded in (Cesar salad garlic or cheese, zucchini boat milk or cheese, vegetarian patty in Vintage's burgers, roasted pepper in the burrito), the dough fajitas wrap removed; the pot versions stay ([vintage_delight.md](vintage_delight.md) step 9) |
| 4.8 Baking | BUILT | Carrot cake in `CAKES` (2 carrots), sweet potato pie in `PIES` (2 baked sweet potatoes, no filling can). The removal sweep takes VD's own recipes, slices-to-whole stay |
| 4.9 Drinks | BUILT | Fluids `creativeengineering:carrot_juice`, `dandelion_juice`, `turnip_water`; pot route `_manual_only` (VD's inputs), heated mixer = the same inputs + 250 mB water, spout, drain, bottler at 250 mB. Jar soft fluids (2026-10-23) for the 3 juices (tank fluid as equivalent) and fermented garlic honey, hand-written beside `nut_milk.json` |
| 4.10 Mob food | BUILT | In the port (`ModMobsEvents`), Rustic's 3 peppers and seeds in place of VD's |
| 4.11 Loot | BUILT | `loot/07_rules.js`: VD's 9 injections as `L.add` entries in each table's main pool (0.015 to 0.04 a roll per item), and `L.pack` for the 6 VD crates. Wagon tables kept in the port |
| 4.12 Houses | BUILT | Port injects the 5 vanilla depot houses at weight 4 (normal priority, before PoolTuner at LOWEST). `pool_tags.json` `mod/veggiesdelight` multiplier 0.5. They appear in `pool_config.json` after the next world load. Lootr converts their chests (wooden chests) |
| 4.13 Trades | BUILT | `trades.js` farmer level 1 (VD's counts); `wandering_trades.js` common pool, 7 items |
| 4.14 Compost | BUILT, check OPEN | The rules cover its produce, seeds and cooked veggies; its 6 wild crops (0.65) and 6 crates (1.0) are rows in Creative Delight's `kubejs_startup/veggiesdelight/compost.js` (2026-10-23; moved from `compost_values.js` 2026-10-25, QoL-4). `/dumpcompost` to confirm |
| 4.15 UI, release | changelog BUILT; JEI, BlockRunner, WTHIT icons OPEN | Changelog bullets under Farming & Nature, Food & Cooking, World Generation. No `plan_1.1.01.md` exists; the release notes are `unreleased.md` |

Port fixes to upstream data (`port_resources.py`): the IE crusher recipe for wild broccoli named
`fveggiesdelight:wild_broccolis` (typo); the More Delight compat recipes used `#forge:sliced_potato`, which More
Delight for 1.19 calls `#forge:sliced_potato`.

Build order. Every recipe through `global.NCH`, every tag through `global.tags`, every id through the item and block
dicts (CLAUDE.md).

### 4.1 The port (new jar)

- `development/backportedVeggies`, mod id `veggiesdelight` (ids unchanged so the data and textures carry over), root
  `settings.gradle` entry, `installToMods`, `mod_sides.md` row (both sides). GPLv3 for our code, MIT notice kept.
- 1.20.1 to 1.19.2: block properties need a `Material`; the creative tab is a `CreativeModeTab` subclass (no
  `BuildCreativeModeTabContentsEvent`); recipe JSON drops the `category` field; loot and worldgen JSON in 1.19.2 form.
- **Dropped:** the bell pepper item, seeds, crop, crate and wild block; all 10 advancements; the 9 loot GLMs and their 9
  tables (the 5 wagon tables stay: the depot houses' chests use them); `ModComposterEvents`; the Java trades; the compat village houses and their config switches; the
  `configurable_rarity_filter` and the `chance*` config keys (rarity moves into the placed feature json, §4.3); the 52
  recipes for absent mods and the FD compat vegetable soup.
- **Added:** the pot recipe for fermented garlic honey (§4.7); replaced 2026-10-23 by VD's own Vintage Delight jar recipe
  (`-02` ships the 22 `compat/vintagedelight` recipes, [vintage_delight.md](vintage_delight.md) §3.4).
- **Kept in code:** the 5 vanilla depot houses' pool injection (§4.12), the mob food edits (§4.10), the mature
  dandelion's bone meal behaviour.
- Tags: VD's `bellpepper` tags are not shipped; inputs point at Rustic's `bell_pepper` tags.

### 4.2 Ids, names and tooltips

- Every VD item in `item_dict.js`, crates and placeables in the block dict.
- Names and tooltips copied from VD's lang, then held to the pack's rules through the names layer
  ([names.md](names.md): `name_rules.json`, `name_fixes.js`): Title Case ("Zucchini Seeds"), "Caesar Salad", placed
  blocks "Placed ..." by the Display Delight rule where one applies. Placeable and effect lines come from the pack's
  own tooltip code ([tooltips.md](tooltips.md)); VD ships no tooltip keys of its own besides advancements.

### 4.3 Wild crops (`wild_crops.json`)

Five new rules, VD's own modifiers in `disable_modifiers`. Rarity is a vanilla `rarity_filter` in each placed feature
json of the port, like the pack's own `wild/*` features ([crops.md](crops.md) §2.8). Starting chances below
(smaller = more frequent; FD's carrots and onions run at 120 in the same biomes, tomatoes at 100, sea beets at 30).

| Rule | `where` | Chance (1 in N chunks) | Why |
|---|---|---|---|
| Broccoli | `temperate grassy !swamp` (carrot and onion's rule) | 240 | Small chance in the temperate wild plant pool |
| Cauliflower | `temperate grassy !swamp` | 240 | same |
| Garlic | `temperate grassy !swamp` | 240 | same |
| Sweet potato | `warm dry grassy` (tomato, cotton, corn's rule) | 100 | Hot dry grassy biomes |
| Zucchini | `warm dry grassy` | 100 | same |
| Turnip | `sandy beach`, `cliff` (the beetroot / sea beet rule) | 30 | Wherever sea beets spawn |

Then `tools/gen_soil_compat.py` over the new features (Immersive Weathering soils), regenerate
[crops_by_biome.md](crops_by_biome.md) (`tools/gen_crop_worldgen_table.py`), `tools/test_wild_crops.sh`.

Breaking a wild plant keeps VD's drops: the planting item plus a 20 % roll of the produce; shears give the block.

### 4.4 Mature dandelion

- **Placement:** an entry in `wild_flowers.json` `flowers`, not VD's plains modifier (into `disable_modifiers`).
  Biomes: every biome where the dandelion generates after the pack's flower rules (poppies and dandelions are gone from
  snow, desert and bare badlands, [flowers.md](flowers.md)). New key `follows: "minecraft:dandelion"`:
  `tools/gen_wild_flowers.py` resolves the dandelion's biomes and writes them into the generated data;
  `wild_flowers.js` places the entry from that list. Rate 1/8 (one patch per 8 chunks), above every wild crop above.
  No Flower Patch share (no patch block exists for it).
- **Drops (new loot table):** always 1 dandelion and 0 to 3 dandelion leaves (uniform), any tool. Shears give the
  block, as for every other wild plant (VD's own behaviour).
- Bone meal on one pops another Mature Dandelion item (VD, kept). Luck in suspicious stew (VD, kept).

### 4.5 Crops and seasons

- Rows in [crops.md](crops.md) §2.1, the harvest / replant audit (`farming/crops #3`), WTHIT growth coverage
  (`/dumpcropgrowth`, `tools/crop_growth_coverage.py`). Plain `CropBlock`s: soils, Quark replant, hoe area and Fertile
  Soil already work.
- **Seasons:** VD's Serene Seasons tags copied over unchanged (bell pepper's dropped with the crop). The overhaul is
  still needed (§5.2).

### 4.6 Food types, values, cans, jars

- `/dumpfood`, `python3 tools/food_catalog.py`: every VD food gets a food type and that type's values (not VD's).
- Cans: `tools/gen_can_types.py` picks up the new raw and cooked veggies by type.
- **Every soup and stew jarable:** broccoli soup, cauliflower soup, garlic chicken stew, turnip beef stew and
  cacciatore get `food_type/soup`; `/cegen run soup_soft_fluids` (`tools/gen_soup_soft_fluids.py`) then gives each a
  Supplementaries soft fluid with its own colour ([mixed_jars.md](mixed_jars.md), `dishes/ingredients #13`).
- Stack sizes by `gen_stack_sizes.py` rules (bowl meals 32, drinks 32, placed cakes and feasts 8); placeables through
  `/dumpplaceable` + `placeable_catalog.py`; feasts in the Feast Plating JEI tab.

### 4.7 Cooking rules folded in

Where a pack rule exists ([recipes_cooking.md](recipes_cooking.md)), the VD recipe follows it:

- **Bell pepper inputs:** raw -> `#forge:crops/bell_pepper` (Rustic); VD's smoked pepper (dropped) -> Rustic's roasted
  bell peppers (any colour). Stuffed Bell Peppers feast: 3 Rustic peppers.
- **Wraps require a tortilla:** Chicken Fajitas Wrap and Vegetables Wrap take `corn_delight:tortilla` in place of
  `#forge:dough` (VD's Corn Delight compat recipes, made the only ones), plus a sequenced assembly like the Mutton Wrap.
- **Fermented garlic honey:** Vintage Delight's Pickling Jar, 4 garlic cloves + honey bottle, 600 ticks (VD's compat
  recipe, since `-02`, 2026-10-23; was the FD cooking pot); the hand recipe is removed.
- **Heatless grid routes** from cooked ingredients for pot meals, `_manual_only` where Slice and Dice must not copy;
  **bone broth** routes for the soups and stews; burger, sandwich and wrap assemblies; fish frying stays the pack's;
  `#forge:sugar`, egg and milk tags; seasoning (salt) where the pack seasons.
- **Breads** (garlic bread, cauliflower bread): flour and oven by the bread chain where they take grain today.
- **Doughs as is:** sweet potato dough and uncooked mhadjeb keep VD's recipes (§5.2).

### 4.8 Baking (`baking.js`, [oven.md](oven.md) §2.6)

- **Carrot Cake** joins `CAKES`: baked cake base + frosting + 2 carrots (manual craft), or raw base + frosting + 2
  carrots (oven, cake pan). VD's 9-item grid recipe is removed; `carrot_cake_from_slices` stays (reassembly).
- **Sweet Potato Pie** joins `PIES`: pie crust + 2 baked sweet potatoes + 2 sugar (honey allowed) in the oven, pie tin;
  a filling can like the other pies if `can_types.json` names it. VD's shaped recipe is removed;
  `sweet_potato_pie_from_slices` stays.
- Vegan pizza, zucchini quiche, beetroot brownie tray and the cupcakes: oven where the overhaul's family rules reach
  them; otherwise VD's recipes on the pack's crust, flour and sugar tags.

### 4.9 Drinks

Carrot Juice, Dandelion Juice and Turnip Water join the juice family the way chorus juice did
([recipes_cooking.md](recipes_cooking.md) §2.2): a fluid each in `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js` (no block, no
bucket), heated mixer route, FD pot route `_manual_only`, spout / drain / IE Bottling Machine at 250 mB, effects shown on
the fluid in JEI. Marked for the tea audit (§5.2).

### 4.10 Mob food

Copied as VD has it: chickens eat the four kept seeds, parrots tame with them, pigs eat cauliflower (and floret), sweet
potato, turnip, zucchini (and slice); villagers pick up the produce, seeds and cauliflower bread. Bell pepper entries
point at Rustic's peppers. The mob food system as a whole is due an overhaul (§5.2).

### 4.11 Loot

VD's 9 additions (mineshaft, pillager outpost, shipwreck supply, simple dungeon, five village house types) rebuilt
through the pack's loot pipeline (pre-roll `loot_config.json`, [loot.md](loot.md)), within the pack's chest budgets. The
depot houses' own chests keep their wagon tables and get the Lootr swap like other
structure chests.

### 4.12 Village houses

No pack tool adds a new piece to a pool: `PoolTuner` reweights the pieces mods put in, after they do. So the port keeps
VD's `VillageStructures` injection for the 5 vanilla styles only (no config switch, no compat houses). The pack then
weights them like every other modded house: they appear in `pool_config.json` on the next world load, a
`mod/veggiesdelight` tag in `pool_tags.json` (and `type/house`), a low starting weight.

### 4.13 Trades

VD's lists through the pack's files, not the port: the farmer buys 16 to 26 of each produce for 1 emerald
(`economy/trades.js`, `e.addTrade` like the other farmer trades); the wandering trader sells each seed, the garlic
clove, the sweet potato and the dandelion leaf for 1 emerald (`economy/wandering_trades.js`, `global.trades`, inside the
pack's common / rare counts). The bell pepper trades are dropped with the crop.

### 4.14 Compost

`compost_rules.js` rebuilds the composter table from tags on every tag sync, so the new produce, seeds and foods fall
into the existing categories through their tags. Any item left out gets a rule there, not a value in the port.

### 4.15 UI and release

JEI coverage, BlockRunner tiers for crates and wild plants (`tools/blockrunner_untagged_blocks.json`), WTHIT icons;
`/dumpall` and the generators in the usual order; changelog bullets under the farming and food headings; a `plan_*.md`
row.

## 5. Part 4: marked merges, pins and notes

### 5.1 Marked merges (left in game for decision)

Both items stay in the pack, unmerged, until the user decides. When decided, the merge goes through
[item_merges.md](item_merges.md) (`global.itemMerges`).

| Merge | Items | Note |
|---|---|---|
| M1 Fish and Chips | `veggiesdelight:fish_and_chips`, `pneumaticcraft:cod_n_chips` | Same name in JEI meanwhile (§3.3 R9) |
| M2 Mashed Potatoes (pinned) | `veggiesdelight:mashed_potatoes`, `moredelight:mashed_potatoes` | VD's also takes sweet potato |
| M3 Stuffed Bell Peppers | VD's placed feast and serving, Rustic's three stuffed peppers | The placed feast is kept either way |

### 5.2 Notes for later work (each has a TODO row)

- **Grow season overhaul:** VD's seasons are copied, not reviewed (`farming/crops #5`).
- **Tea audit:** the three VD juices go through the tea cleanup and the juice / tea rule with the teas
  (`food/drinks #2`, `#8`).
- **Dough unification:** sweet potato dough and uncooked mhadjeb stay as VD has them until doughs are unified.
- **Mob food overhaul:** VD's mob food is copied as is; the whole mob food system needs an overhaul.

### 5.3 Recipes for mods that may be added later

Not ported now; kept in the source jar for when either mod comes in.

- **Vintage Delight (22):** cesar salad, cheese burger, chicken fajitas wrap, deluxe burger, stuffed burrito, stuffed
  zucchini boat, uncooked mhadjeb, zucchini quiche; pot: broccoli soup, cacciatore, cauliflower kuku, cauliflower soup,
  garlic chicken stew, ghostly chili, mashed potatoes, pad thai, pasta with broccoli, pickle soup, potato noodles,
  roasted veggies, shakshouka; fermenting: fermented garlic. Port plan: [vintage_delight.md](vintage_delight.md) §3.3.
- **Cultural Delights (15):** avocado toast, beef burrito, broccoli salad, cesar salad, chicken fajitas wrap, chicken
  taco, eggplant burger, hearty salad, mutton sandwich, vegetable wrap; pot: empanada, roasted veggies, shakshouka,
  spicy curry, turnip cake. **Cultural Delights itself is pinned as a possible `largecontent` addition.**

### 5.4 Planned from the scratchpad (2026-10-24)

Rows added to [TODO](todo/TODO.md) on 2026-10-24; nothing is built. Plans: [dishes_ingredients](briefs/dishes_ingredients.md),
[dishes_meals](briefs/dishes_meals.md), [baking_baked-goods](briefs/baking_baked-goods.md),
[ports_veggiesport](briefs/ports_veggiesport.md). No tests are drafted until a row is built.

- [TODO](todo/TODO.md) `baking/baked-goods #17` (folded): the dough unification also gives each dough a texture and a recoloured
  dough block (`baking/baked-goods #27`). Open: which doughs stay distinct. Note `dishes/meals #32` removes the mhadjeb, so
  the "uncooked mhadjeb stays" rule above ends.
- [TODO](todo/TODO.md) `dishes/ingredients #20`: a cut sweet potato gives 3 sweet potato slices (a new item; the potato slice is
  Rustic Delight's).
- [TODO](todo/TODO.md) `dishes/ingredients #21`: recipes that take a potato also take a sweet potato, and the cooked and sliced
  forms. Open: which recipes.
- [TODO](todo/TODO.md) `dishes/meals #25`: new cauliflower soup texture.
- [TODO](todo/TODO.md) `dishes/meals #33`: potato noodles renamed Creamy Gnocchi (milk, gnocchi, onion or garlic, either may be
  pickled); cutting potato dough and sweet potato dough gives gnocchi. There is no potato dough item today.
- [TODO](todo/TODO.md) `dishes/meals #34`: fries from any potato slice, sweet potato slices included (the fries recipe already
  reads `#forge:sliced_potato`; the work is the slice item).
- [TODO](todo/TODO.md) `dishes/meals #35`: steak and broccoli accepts rice, mashed potatoes, any baked potato plus a bowl, and other
  baked veggies plus a bowl.
- [TODO](todo/TODO.md) `ports/veggiesport #2` (folded): merge fish and cod and chips (marked merge M1 above).

## 6. File map

| File | Role |
|---|---|
| `development/backportedVeggies/` | The port. `tools/port_resources.py` rewrites `src/main/resources/{assets,data}` from the upstream jar (run it after changing it) |
| `kubejs/startup_scripts/_lib/item_dict.js` | VD keys |
| `kubejs/generation_scripts/worldgen/wild_crops.json`, `wild_flowers.json`; `tools/gen_wild_flowers.py`, `server_scripts/worldgen/wild_flowers.js` (`follows`) | Wild placement |
| `kubejs/server_scripts/cooking/` (`cooking.js`, `baking.js`), `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js` | Recipes, juice fluids |
| `kubejs/generation_scripts/food/food_types.json`, `can_types.json`, `stacks/stack_sizes.json`; soup soft fluids | Generated food data |
| `kubejs/server_scripts/loot/07_rules.js` | Chest loot (`L.add`, `L.pack`) |
| `kubejs/generation_scripts/worldgen/pool_tags.json` (`pool_config.json` after a world load) | Depot house weights |
| `kubejs/startup_scripts/changes/transition_items.js`, `server_scripts/tags/cookingtags.js`, `fluidtags.js`, `_lib/fluid_dict.js` | Wrap assembly items, cooking tags, juice fluid ids and tags |
| `tools/gen_crop_worldgen_table.py` (`CROP_BLOCKS`), `tools/test_wild_flowers.sh` (`follows`) | Tool support |
| `kubejs/server_scripts/economy/trades.js`, `wandering_trades.js` | Trades |
| `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/compost_rules.js` | Compost (only for items no rule covers) |
| names layer (`name_rules.json`, `client_scripts/name_fixes.js`) | Names |
| `mod_sides.md` | Jar row |

## 7. Offline checks

2026-10-22: the build, `test_wild_crops.sh` and `test_wild_flowers.sh` pass; `node --check` on every edited script;
`port_resources.py` reports no unknown item or tag in the port's recipes. Still to run once dumps exist: `food_catalog.py
--check`, `crop_growth_coverage.py`.

- `./gradlew :backportedVeggies:build --offline` passes.
- `tools/test_wild_crops.sh` passes with the new rules and disabled modifiers.
- `python3 tools/food_catalog.py --check` lists no unclassified VD food.
- `python3 tools/crop_growth_coverage.py` lists no VD crop.
- `python3 tools/gen_wild_flowers.py` lists the mature dandelion in exactly the dandelion's biomes.

## 8. In-game test suite (drafted)

Crops and worldgen

- [ ] VD-01 Each of the 6 kept crops plants on farmland, Rich Farmland and Fertile Soil, grows to max age, and drops
  produce (and seeds where it has them).
- [ ] VD-02 Right-click on a ripe VD crop harvests and replants; a hoe does the same over its area.
- [ ] VD-03 WTHIT shows a growth percentage on every VD crop.
- [ ] VD-04 Wild broccoli, cauliflower and garlic appear (rarely) only in temperate grassy biomes; wild sweet potato and
  zucchini only in warm dry grassy biomes; wild turnips only where sea beets grow (`/ceplace` or locate).
- [ ] VD-05 Wild VD crops also generate on Immersive Weathering soils.
- [ ] VD-06 Mature dandelions generate in every biome with dandelions and nowhere else, more often than the wild crops.
- [ ] VD-07 Breaking a mature dandelion drops 1 dandelion and 0 to 3 leaves (any tool but shears); shears give the
  block; bone meal pops another.
- [ ] VD-08 No VD bell pepper, seeds, crate or wild patch exists; Rustic peppers work in every VD recipe.
- [ ] VD-09 VD crops follow the copied seasons.

Food and recipes

- [ ] VD-10 Every VD food has a food type tag and the type's values.
- [ ] VD-11 No smoked bell pepper exists; VD recipes that used it take any Rustic roasted bell pepper.
- [ ] VD-12 Carrot cake and sweet potato pie use the baking families (base / crust, oven); VD's own recipes are gone.
- [ ] VD-13 Each VD pot meal has a heatless grid route and no Slice and Dice mixer copy where the rule says so.
- [ ] VD-14 Carrot juice, dandelion juice and turnip water come from the heated mixer, spout, drain and bottler as fluids.
- [ ] VD-15 Fish and Chips (both), Mashed Potatoes (both) and the stuffed peppers (VD feast, Rustic's three) all exist
  and work.
- [ ] VD-16 Fermented garlic honey is made in the Pickling Jar (VTD-15); not in the cooking pot; no hand recipe.
- [ ] VD-17 Placed VD pies, cake, tray, lasagna and stuffed peppers show the placeable tooltip and serve correctly.
- [ ] VD-18 Stack sizes follow the policy (bowl meals 32, drinks 32, placed cakes and feasts 8).
- [ ] VD-19 New raw and cooked veggies have cans.
- [ ] VD-20 Both wraps need a tortilla; no dough route.
- [ ] VD-21 Every VD soup and stew goes into a Supplementaries jar with its own colour.
- [ ] VD-22 Sweet potato dough and uncooked mhadjeb craft as in VD.
- [ ] VD-23 Cauliflower Bread bakes in the Oven from dough and 2 cauliflowers; no grid recipe.
- [ ] VD-24 VD grid meals and the heatless pot-meal routes are not offered by the Create mixer or mechanical crafter.
- [ ] VD-25 A ripe zucchini plant gives 1 or 2 zucchinis on right-click and drops back 3 ages.

World and economy

- [ ] VD-30 Depot houses appear in the 5 vanilla village styles at the pool weight set; their chests are Lootr chests.
- [ ] VD-31 VD produce appears in the 9 chest types only as the pack's loot config adds it.
- [ ] VD-32 The farmer buys VD produce; the wandering trader sells VD seeds, clove, sweet potato and leaf.
- [ ] VD-33 Pigs and chickens breed with VD produce and seeds; parrots tame with VD seeds; villagers pick up VD produce.
- [ ] VD-34 Every VD crop, seed and food composts.
- [ ] VD-35 No VD advancement exists.

UI

- [ ] VD-40 Names follow the pack's rules (Caesar Salad, Zucchini Seeds); every VD item in JEI has a
  recipe or a use.
- [ ] VD-41 Place a carrot cake and eat one slice, then more: the cut face shows the cake's inside texture, not the
  frosted top.

## 9. History

- 2026-10-22: built. Port jar (`development/backportedVeggies`), pack integration (§4.0). Found while building: zucchini
  has its own right-click harvest; the five wagon tables are the depot houses' chests; two upstream recipe mistakes
  fixed in the port. Open: the dump-driven steps (cans, stacks, placeables, compost check, crop rows, WTHIT, JEI).

- 2026-10-22: implementation decided (user): bell pepper line dropped, smoked pepper dropped, wild rules (temperate
  grassy, warm dry grassy, sea beet), mature dandelion everywhere dandelions grow with new drops, seasons copied, juices
  as fluids and in the tea audit, soups jarable, tortilla wraps, cake and pie in the baking families, garlic honey in the
  pot, injections through the pack's tools, advancements dropped; merges marked (Fish and Chips, Mashed Potatoes,
  Stuffed Bell Peppers); Cultural Delights pinned; remaining overlaps listed (§3.3). Decisions D1 to D10 closed.
- 2026-10-22: cross-references to the Vintage Delight plan (22 compat recipes, D6).
- 2026-10-22: plan written from the 1.20.1 1.9.2 jar (decompiled, 36 classes) and the 1.21.1 1.9.3 lang and
  blockstates (same content). Nothing built.
- 2026-10-23: `-02`: the 22 Vintage Delight compat recipes ship (the Vintage port is built); fermented garlic honey moves
  from the pot to the Pickling Jar.
- 2026-10-24: planned scratchpad rows added (5.4: doughs, sweet potato slices, gnocchi, fries, steak and broccoli sides, soup texture, fish and chips merge); no code changed.
- 2026-10-25: upstream 1.9.3 evaluated against 1.9.2 (§2.7, `tools/jar_diff.py`). Proposed: carry the carrot cake cut
  face over; Nourishment on 9 foods waits for the FD 1.3.4 survey; the rest skipped. No code changed.
- 2026-10-25: carrot cake cut face carried over from 1.9.3 (§2.7, port `-03`, VD-41).
