# Rustic Delight 1.7.0 against the installed 1.3.0

**Status (2026-10-25): built, scratch test queued.** Owner chose option B (section 3) and the list in section 5: a
donor-jar backport, folded into Creative Delight as member `backportedRustic` (section 5). Not installed: it ships
with the Creative Delight jar. Before that, installed: `rusticdelight-forge-1.19.2-1.3.0.jar` (`mods/`, `server_mods/`), the last 1.19.2 build
(2024-11-15). Evaluated 2026-10-25 against `devmods/checkNewerVersionForContent/rusticdelight-forge-1.20.1-1.7.0.jar`
(1.20.1 / Forge), method in [devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in
`DecompiledMods/`. Plan row: `largecontent/mod-additions #19` in [todo/TODO.md](todo/TODO.md).

**Licence: MIT on Forge** (owner-supplied 2026-10-25). Both jars' `mods.toml` still say All Rights Reserved; get the
upstream licence file confirmed before copying textures, models or code, and until then treat them as All Rights Reserved.
Data facts (a recipe's inputs, a food value) can be rewritten in our own scripts. Getting the new items means either
our own art for KubeJS items, or a donor-jar backport like Vinery and Miner's Delight (upstream jar shipped unmodified,
our jar holds only our own code). See section 3.

Pack context that sets most decisions: Rustic is the coffee survivor ([item_merges.md](item_merges.md) 2.8; its
coffees give Caffeinated through `food_values.js`), the bell pepper survivor over Veggies Delight
([veggies_delight.md](veggies_delight.md)), and its ripe pepper roll is already 1 : 1 : 1 ([crops.md](crops.md),
`farming/crops #9`). Comfort stays on soups (FD decision, [farmers_delight.md](farmers_delight.md) 2.5).

## 1. How it was read

- `python3 tools/jar_diff.py` (1259 files added, 42 removed, 282 changed; lang +173 / -2 / ~2).
- Registry from the lang keys, cross-checked with the `ModItems` / `ModBlocks` strings. **No id is removed**: every 1.3.0
  item and block still exists in 1.7.0.
- Recipes, tags and loot tables compared semantically (wrappers, `group`, `category`, key letters ignored).
- Food values from `FoodValues` bytecode, effects through the lambda bodies. Pancake, village and trade changes from
  `javap` on `PancakeBlock`, `ModVillageStructures`, `ModEvents`.
- Overlap checked against `kubejs/exported/items_by_mod/` and `item_dict.js`.

The 1.20 porting share is small here: advancements (dropped by the pack), lang key renames for the coffee tooltips,
config conditions on every recipe, biome tags in place of `#minecraft:is_jungle`.

## 2. Changes and proposed decisions

### 2.1 New content (about 100 items and blocks)

| Group | 1.7.0 detail | Pack overlap | Proposed |
|---|---|---|---|
| Six more pepper colours | Pale seeds grow white, pink, orange; Dark seeds grow blue, purple, black. Two new crops, seeds, seed bags, crates (6), wild Pale / Dark peppers (jungle, inside the same wild patch feature), potted wilds | none; the colours are a wish already noted in `farming/crops #9` | needs new crops: section 3 |
| Pepper slices, roasted slices, rolls | every pepper cuts into 2 slices (10% seed back), slices roast, 2 slices + cooked rice -> 2 rolls (7 / 0.8); slice -> seed craft | none | section 3 (rolls for the 3 current colours are the cheapest subset) |
| Stuffed peppers in all 9 colours; Bell Pepper Medley, Pale and Dark Medley (feasts, 3 stuffed peppers + bowl); Rice Roll Royale (feast: kelp roll slices + pepper rolls) | | Veggies' Stuffed Bell Peppers feast (marked merge with Rustic's, veggies_delight.md 5.1); FD Rice Roll Medley | section 3; the Medley overlaps the Veggies feast |
| Giant bell pepper blocks (9, from 9 slices) and a giant pepper patch in jungles (48 tries, red / yellow / green 90 each, the others 5) | | none | section 3; would need a [crops_by_biome.md](crops_by_biome.md) and worldgen pass |
| Pancakes rework | plain Pancakes (syrup topping), Pumpkin and Coffee Pancakes added; every stack is now a block you take single Pancake items from (6 per recipe, stack up to 12 by placing pancakes back); pot recipes for each stack | Supplementaries `pancake`; Veggies `sweet_potato_pancakes` | code (block behaviour + 7 serving items): section 3 only |
| Syrup line | Syrup (pot: apple or beetroot + sugar), Syrup Coffee, Syrup Cookie, Syrup Cheesecake, Syrup Sandwich, Sweet Salad (honey or syrup); syrup -> 3 sugar | none (Hostile Delight's slime syrup is unrelated) | section 3 |
| Coffee foods | Coffee Cookie, Coffee Cheesecake + slice, Coffee Pancakes, Pumpkin Coffee, Roasted Coffee Beans bag | Farmer's Respite coffee cake (now on Rustic beans) | section 3; new coffees would also need the Caffeinated swap and the Kettle (`food/drinks #7`) |
| Fried foods | Fried Dough (oil + dough), Fried Dumplings (oil + 2 FD dumplings), Fried Fish (oil + batter + any raw fish) | Fried Fish = the pack's Fish Tempura chain (`dishes/meals #7`) | Fried Fish **skip**; dough and dumplings section 3 |
| Calamari Soup (pot, 10 / 0.8, Nourishment 3600), Calamari Crate | | Oceanic / Ocean's soups | section 3 |
| Cherry blossom foods (coffee, pancake serving, cheesecake slice) | | 1.19.2 has no pink petals or cherry leaves; the 1.3.0 cherry items are not in the pack | **skip** |

### 2.2 Recipes on existing items (data only, possible without new items)

| Change | Proposed |
|---|---|
| Bell Pepper Pasta: any 3 bell peppers (tag) [one green, one yellow, one red] | **carry over** (`NCH` replace; fits the even colour roll) |
| Bell Pepper Soup: 3 peppers [4] | owner's call (cheaper soup) |
| Golden Coffee Beans: raw or roasted bean in the middle [raw only] | **carry over** |
| Honey Coffee: no sugar (grid, from coffee or milk coffee) | carry over, owner's call |
| Coffee-Braised Beef: any coffee food ingredient (Coffee, Dark Coffee) [Coffee] | **carry over** |
| Calamari Roll: any `#forge:raw_fishes/calamari` [calamari slice or Miner's tentacles] | **carry over** (whole squid items from Miner's / Cultural count) |
| Pot coffees take 3 beans, raw or roasted [4 roasted]; Milk Coffee from beans in the pot; new pot routes for every coffee | **skip**: the coffees move to the Kettle (`food/drinks #7`), set amounts there |
| Kettle brewing of roasted beans into Farmer's Respite coffee fluid | **skip** (Respite's coffee is purged, item_merges.md 2.8) |
| Stuffed peppers and Spring Rolls read new `stuffed_bell_pepper_ingredients` / `spring_roll_ingredients` tags (any raw meat, fish, egg, mushroom, vegetable) [FD `cabbage_roll_ingredients` minus peppers] | skip (close to the same set on FD 1.2.4) |
| Cooking oil ingredients add pumpkin, sunflower and canola seeds | none: the pack already adds pumpkin seeds (`cookingtags.js`); no sunflower or canola seeds installed |
| Fried Rice from oil removed; Fried Egg from oil moved, same recipe | skip (pack keeps both, `food/types-and-tuning #6`) |
| Pancake stacks: bowl moved to the bottom row, milk on top, extra filling replaces sugar | only with section 3 |
| Brown dye from roasted beans | optional |

### 2.3 Food values and effects

| Change | Proposed |
|---|---|
| Bell pepper 2 / 0.2 [1 / 0.1] (the new slice takes 1 / 0.1) | owner's call; a whole pepper at 1 / 0.1 matches a slice, so 2 / 0.2 only makes sense with slices |
| Golden Coffee Beans saturation 1.2 [0.8] | owner's call |
| Fruit Beignet 7 / 0.6 [6 / 0.6] | owner's call |
| Chocolate and Vegetable pancake servings 4 / 0.6 [3 / 0.6]; honey and chocolate Speed 900 [1200]; vegetable Haste 600 [1200] | only with the pancake rework (section 3) |
| Potato Salad Regeneration 100 [120] ticks | skip (2 ticks a heart, noise) |
| Bell Pepper Soup Nourishment [Comfort] | **skip**: Comfort kept (FD decision 2.5) |
| Milk, Honey and Chocolate Coffee effects 1200 [900 for honey and chocolate] | **skip**: the pack's Caffeinated swap reads these durations; changing them is a food tuning question |

### 2.4 Loot, worldgen, villages, trades

| Change | Proposed |
|---|---|
| Chest loot swaps (configurable): jungle temple 50% coffee beans x2 for bamboo; cotton seeds for pumpkin / wheat seeds in savanna, snowy, taiga village houses, mineshafts and shipwreck supply; Pale and Dark seeds in jungle temples | coffee and cotton: owner's call, through the pack's loot scripts ([loot.md](loot.md)); seeds only with section 3 |
| Village farm plots grow Rustic crops (processor rules on `farm_plains`, `desert`, `savanna`, `taiga`) | owner's call; 1.19.2 processor lists are data, so a datapack override can do it without code. Check first what FD and other mods already put in those lists |
| Wandering Trader sells Pale and Dark seeds | only with section 3 |
| Wild plants: shears or silk touch give the plant only; seeds otherwise, red pepper only when not sheared | skip (behaviour almost the same) |
| Wild patches read `rusticdelight:has_wild_*` biome tags [`#minecraft:is_jungle` / `is_forest`] | skip (pack overrides the features already, crops.md V7) |
| Pancake stack block loot: a partial stack drops nothing, a full one drops itself | only with section 3 |

### 2.5 Tags, names, other

| Change | Proposed |
|---|---|
| `brewinandchewin:pizza_toppings` += bell peppers | carry over if our Brewin port reads that tag (check `backportedBrewin`) |
| `forge:cookies` (+ `supplementaries:cookies`), `forge:storage_blocks/*`, `forge:seeds/*` subtags | optional, tag helper |
| Cotton Boll renamed "Cotton", Cotton Boll Crate "Cotton Crate" | owner's call (`namechanges.js`, [names.md](names.md)) |
| Coffee tooltips: "Clears 1 Effect" on honey, pumpkin, syrup, cherry coffees too | follows the new coffees |
| Thirst Was Taken compat, advancements (17), Create `upright_on_belt` for new bottles | skip (Thirst not installed; pack drops advancements) |

## 3. Delivery options for the new content (owner decision)

| Option | Brings | Cost | Notes |
|---|---|---|---|
| **A. Data only** | section 2.2 carry-overs, optional loot and village farm data | small (one KubeJS pass) | no new items; 1.3.0 jar stays |
| **B. Donor-jar backport** (`development/backportedRustic`, mod id `rusticdelight`) | everything in 2.1 | large: crops (3), wild crops, pancake block, 3 feasts, giant pepper blocks, loot modifiers, village processors, trades, written from behaviour; data translated by the port tooling | 1.7.0 keeps every 1.3.0 id, so the swap needs no conversion; the 1.7.0 jar becomes the donor in its own folder, like `miners_upstream/`. Mod id unchanged = no `mod_sides.md` edit |
| **C. Pack items with our own art** | a chosen subset (e.g. slices, rolls, medley for the 3 current colours; syrup line) | medium per group, art needed | no upstream assets; [texture_reuse.md](texture_reuse.md) lists usable textures |

Recommendation: A now in every case. Choose B if the six colours and pancakes are wanted (that is most of the gap);
choose C only for a small subset.

## 4. Open questions for the owner

1. Delivery: A only, A + B, or A + C (which groups)?
2. Bell Pepper Soup at 3 peppers; Honey Coffee without sugar; Cotton renames?
3. Food values: bell pepper 2 / 0.2, golden coffee beans 1.2, fruit beignet 7?
4. Chest loot swaps and Rustic crops in village farms?

## 5. Built 2026-10-25: the port in Creative Delight

Owner list (2026-10-25, scratchpad): new peppers (Dark seeds from ancient cities, Pale seeds from sky villages and cloud
storage, every slice, roast, roll and stuffed pepper, the three medleys and Rice Roll Royale, giant peppers in hot dry
grassy biomes), the pancake rework, syrup, coffee foods, fried foods, calamari soup, cherry blossom foods on the pack's
pink leaves, the 2.2 recipe carry-overs, the 2.3 value changes, Fruit Beignet removed, Rustic crops in village farms by
the wild crop rules, rare Pale and Dark seed trades, peppers on pizza with a Supreme Pizza, the cotton rename, the coaster
cut, a syrup for the maple foods.

### 5.1 The jar

| Piece | Where | Notes |
|---|---|---|
| Project | `development/creativeDelight/members/rustic` (Gradle `:backportedRustic`, `bundled_in=creativeDelight`) | Mod id `rusticdelight`, display name "Rustic Delight (Creative Delight)", version `1.7.0-ce-19.2-01`. Own package `com.SaintAlfonzo.creativedelight.rustic` (nothing in the pack names upstream classes). GPLv3, our code only |
| Donor | `rustic_upstream/rusticdelight-forge-1.20.1-1.7.0.jar`, sha1 `bca9adf6...`, Modrinth `foa4fGIH/z29Gd3ec` | Read at runtime as two hidden built-in packs (`UpstreamJars` pins, on the shared `DonorJars` / `BuiltInPacks.addUpstream` / `MemoryPackResources`, [pack_tooling.md](pack_tooling.md) 2.8; the Miner's method). Missing or wrong file stops the game with the URL. `build_deploy.py` ships the folder (`UPSTREAM_DONORS`) |
| Registry | `registry/RDBlocks`, `RDItems`, `RDFoods`, `RDPotions`, `RDLoot`, `RDTab` | Every 1.7.0 id (162) but `fruit_beignet`; every 1.3.0 id kept, so old worlds need no conversion. `tools/check_donor.py` compares the ids with the donor's lang, blockstates and data (0 problems) |
| Blocks written from behaviour | `content/` | `PancakeStackBlock` (6 per placed plate, take one, sneak-use puts one back up to 12; keeps upstream's `servings` numbering so 1.3.0 plates load as they were), `PepperMedleyBlock` and `RiceRollRoyaleBlock` (FD `FeastBlock` with their own `servings` range), crops (bell peppers 8 stages, coffee 6, cotton 4), wild crops on `#minecraft:dirt`, coffees that clear one effect (`EffectClearingDrinkItem`) |
| Translator | `upstream/RusticTranslator` | Drops advancements, the biome modifiers (the pack places every patch, 5.4), upstream's edits of five FD recipes, the 8 chest seed and bean swaps, Fruit Beignet; renames 1.20 loot `any_of` and rewrites `all_of`; makes other-namespace tag entries optional; drops a recipe whose item or type this game lacks; cherry blossom tag to `vinery:cherry_leaves` + `quark:pink_blossom_leaves`; wild and giant patches to the three base colours; in models, FD 1.20's `template_crop_cross` to 1.19.2's `crop_cross` (same model; coffee and the three pepper crops rendered missing without it, fixed 2026-10-26). FD 1.20's `platter` texture (the three medleys) ships in repackedFarmers at FD's own path |
| Data types | `logic/` | `squids_drop_calamari_modifier`, `config_boolean` (always on: no families config), `configurable_rarity_filter` (fixed N: cotton 96 as the pack's 1.3.0 config, peppers 15, coffee 15, giant 64 [1.7.0: 16]). The village farm processor is Creative Delight's, not Rustic's (5.4) |
| Not ported | | Config and item families, Thirst compat, advancements, the brewing steps (the pack's `potions.js` mixes brew the three Haste potions), upstream's Pale and Dark trader offers (pack trade, 5.4) |

### 5.2 Recipes

The port loads 1.7.0's recipes, so the 2.2 carry-overs come with them: Bell Pepper Pasta and Bell Pepper Soup take any 3
`#forge:vegetables/bell_pepper` (soup [4]), Golden Coffee Beans take a raw or roasted bean, Honey Coffee has no sugar,
Coffee-Braised Beef takes any coffee food ingredient, Calamari Roll takes `#forge:raw_fishes/calamari`, roasted beans
craft to brown dye. 1.7.0's pot coffees (3 beans, raw or roasted) come too; the Kettle move (`food/drinks #7`) is open.

Pack rows (`kubejs/data/creative_delight/kubejs/rusticdelight/recipes.js` unless noted):

- Baking overhaul: syrup, coffee and cherry blossom cookies oven only (cookie dough + 2 syrup / roasted beans / pink
  leaves -> 12); the three cheesecakes as pies (pie crust + 2 flavour + 2 sweet). Maple Cookie: dough + syrup + honey
  (so it does not clash with the Syrup Cookie). Maple Glazed Bacon: cooked bacon + syrup.
- `cooking.js`: the heatless Bell Pepper Pasta takes any 3 roasted peppers [one of each colour]; Brewin's pizza is the
  Supreme Pizza: bell pepper, cheese, onion, mushroom, tomato sauce, cooked meat on a dough [cheese, 2 toppings, sauce].
- Stuffed peppers (2026-10-28, scratchpad): a second pot recipe per colour, roasted pepper + rice + one stuffed pepper
  filling -> the stuffed pepper of that colour (9 recipes; upstream's raw pepper recipe stays). The roasted pepper is an
  ingredient, not the container: the pot matches ingredients only, so 9 recipes differing by container would all match
  the first colour. Time and experience as upstream; Slice & Dice copies each. RD-33.
- Tags: bell peppers in `#brewinandchewin:foods/pizza_topping` (`cookingtags.js`); the six new roasted colours in the
  roasted pepper and cooked veggie tags (`rusticdelight/tags.js`).

### 5.3 Values

1.7.0's food values (2.3), except Bell Pepper Soup keeps Comfort (FD decision) and Honey and Chocolate Coffee keep 900
ticks (the Caffeinated swap reads them). So: bell pepper 2 / 0.2 [1 / 0.1], slice 1 / 0.1, Golden Coffee Beans 1.2
saturation [0.8], Chocolate and Vegetable pancake servings 4 / 0.6 [3 / 0.6], honey and chocolate pancake Speed 900
[1200], vegetable Haste 600 [1200], Potato Salad Regeneration 100 [120].

### 5.4 Loot, worldgen, villages, trades, names

- Dark seeds: `minecraft:chests/ancient_city` and `ancient_city_ice_box`, share 0.04, 1-3. Pale seeds: the 11
  `skyvillages:chests/sky_village_*` tables and `cloudstorage:chests/loot_balloon`, same share (`rusticdelight/loot.js`;
  marked for the loot overhaul, TODO). Crates and bags of the new colours unpack like the base ones.
- Rare Wandering Trader offer: Pale or Dark seeds, 5 emeralds, 3 uses (`rusticdelight/trades.js`). The port keeps
  1.3.0's farmer, fisherman and common trader offers.
- Giant peppers: 1 in 64 chunks (2026-10-25, owner: fairly rare; upstream 1 in 16). `wild_crops.json` row `giant_bell_pepper`, `warm dry grassy`, with the new `requires: creative_delight`
  key (`_biome_rules.js`), so the 1.3.0 jar never sees the feature. Wild bell pepper patches lose upstream's 5% Pale and
  5% Dark wild peppers, giant patches the six new colours: the new seeds only come from chests and the trader.
- Village farms: the crop processor is Creative Delight's, not Rustic's (`VillageFarmCrops`, rewritten 2026-10-25 as the village farm audit: [crops.md](crops.md) 2.11). Bell peppers, cotton and coffee are entries in its climate groups (cotton and bell peppers warm only, coffee temperate and warm); no code dependency, and an entry does nothing when Rustic is absent.
- Names: Cotton Boll shows "Cotton", its crate "Cotton Crate" (`namechanges.js`, also the donor's lang).
- Coaster (food_families.md 4.4): produce without a coaster model is out of `#creativeengineering:placeable_on_coaster`
  again (`tools/placeable_catalog.py`); adding models instead stays open.

### 5.5 Overlaps for review after play (owner)

| New | Overlaps |
|---|---|
| Pancakes, 7 stacks | Supplementaries `pancake`; Veggies `sweet_potato_pancakes` |
| Fried Fish | Fish Tempura (`pneumaticcraft:salmon_tempura`, `dishes/meals #7`) |
| Calamari Soup | Oceanic and Ocean's soups |
| Syrup Cookie | Maple Cookie (both on syrup now) |
| Stuffed peppers, Bell Pepper Medley | Veggies' Stuffed Bell Peppers feast (veggies_delight.md 5.1) |

### 5.6 Tests (RD-)

Offline 2026-10-25: `:backportedRustic:build`; `tools/check_donor.py` 0 problems (162 ids both ways, every data
reference resolves, no 1.20-only loot type left, no recipe item missing from the last `/dumpall`); `gen_crop_worldgen_table.py`
resolves the new row; `build_deploy.py` reads the donor pin. 2026-10-26: all 30 donor models naming `template_crop_cross`
match the translator's rename; `platter.png` is in the built repackedFarmers. Live: SQ-RD-01 to SQ-RD-32 in
[testing/scratch_queue.md](testing/scratch_queue.md) (RD-n = SQ-RD-n), queued. RD-01 to RD-12 cover the boot, old
worlds and the pack rows; RD-13 to RD-31 (added 2026-10-25, owner: every scratch test the port needs) cover each new
content group, the worldgen and village rows, the client side and the cutover. RD-32 (2026-10-26): crop and medley textures. RD-33 (2026-10-28): the roasted-pepper stuffed pepper recipes.

### 5.7 Cutover (when the Creative Delight jar is installed)

`./gradlew :patcherMod:installToMods :creativeDelight:installToMods --offline` moves `rusticdelight-forge-1.19.2-1.3.0.jar`
out (`replaces_jars`); copy `rustic_upstream/` beside `mods/` on every client and server. Then drop the 1.3.0-only lines:
`i.fruit_beignet` (item_dict, `removed_items.js`, `cooking.js` 817) and the 1.3.0 note in `potions.js`; run `/dumpall`
and the generators (food types, cans, compost, placeable) for the about 100 new items.

### 5.8 Owner notes after the build (2026-10-25)

**Giant bell peppers: placed and crafted.** Nine blocks, `bell_pepper_<colour>_block` (`RDBlocks.GIANT_PEPPERS`, a plain
block with the melon's properties: same hardness and sound, axe is the fast tool through upstream's `mineable/axe` tag).
No behaviour of their own; they are a storage block for slices.

| Route | Data (donor, loaded as is) |
|---|---|
| Craft | 9 slices of one colour in a 3x3 -> 1 giant pepper (`bell_pepper_<colour>_block_from_bell_pepper_slice_<colour>`), all 9 colours |
| Cut | giant pepper on the Cutting Board with a knife -> 9 slices (`cutting/bell_pepper_<colour>_block`) |
| Break | 1 to 9 slices (uniform, explosion decay); never the block itself, silk touch included. Breaking loses about half; cut it instead |
| Worldgen | `rusticdelight:bell_pepper_block_patch_placed`: 1 in 64 chunks [upstream 16], 48 tries in a 7-block spread, only on `minecraft:grass_block`; pack row `giant_bell_pepper` (`warm dry grassy`), red, yellow and green only (translator) |

**Obtainable without natural spawning: yes, every colour.** Grow peppers (base seeds from wild patches;
Pale and Dark seeds from chests and the trader, 5.4), cut each into 2 slices, craft 9 slices into the block: 4.5 peppers
per giant pepper. The six rare colours are craft-only by design (the patch keeps the three base colours), so the craft is
their only source. No pack script removes or changes these recipes (checked: `cooking.js` only removes Rustic's cooking
oil). Tests: RD-13, RD-24.

**Village farms: audit done, built 2026-10-25 (crops.md 2.11).** Was: only savanna farms changed (5.4). Owner: every village farm type should grow more
crop options, and the wild-crop biome rules may be loosened for some crops when they choose village farms. Row
[TODO](todo/TODO.md) `farming/crops #16`.

**Coffee: unification marked as Creative Delight work, Rustic's coffee recipes kept.** Row [TODO](todo/TODO.md)
`creativedelight/members #11`. Found while checking: Syrup, Pumpkin and Cherry Blossom Coffee (new in 1.7.0) are not in the
`food_values.js` Caffeinated swap, so they still give upstream's Speed and Haste (RD-20 records it).

### 5.9 Owner change list (scratchpad 2026-10-26)

Changes the owner wants on top of the port. Each line is a TODO row in `creativedelight/members`; check the donor first, since
several of these exist in 1.7.0 (pancakes, bell pepper pasta and soup, fried fish, stuffed peppers, coffee variants).

| Row | Change |
|---|---|
| #14 | Peppers: dark seeds in ancient city and sculk chests, pale seeds in sky village and cloud storage loot (`loot/tables #13`); all slices, roasted slices, rolls, stuffed peppers; bell pepper medley, dark and pale medley, rice roll medley; giant pepper blocks and patches in hot dry grassy biomes; new bell pepper and slice values |
| #15 | Pancake rework: new pancakes, new recipes, evaluate against Supplementaries pancakes |
| #16 | Syrup changes; Maple Glazed Bacon and Maple Cookie stay, made with a Rustic syrup |
| #17 | Coffee foods including the cake, gold and honey coffee, coffee beef, golden coffee bean; brown dye from roasted beans |
| #18 | Fried foods including fried fish (evaluate), calamari pot (evaluate), calamari roll |
| #19 | Cherry blossom foods with Vinery cherry leaves or Quark pink blossom leaves |
| #20 | Bell pepper pasta and soup; bell peppers as a pizza topping, default brewing pizza renamed Supreme with many ingredients |
| #21 | Effects: golden coffee bean, chocolate and veggie pancake, honey and chocolate Speed, vegetable Haste, potato salad Regeneration |
| #22 | Remove Fruit Beignet; follow the cotton rename |

Also: village farms grow Rustic crops by the wild-crop rules (`farming/crops #19`); rare Rustic pepper seeds from the wandering
trader (`loot/trader #4`).

