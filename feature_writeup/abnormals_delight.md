# Abnormals Delight 6.0.3 against the installed 4.1.2

**Decided 2026-10-25 (owner); all five changes BUILT 2026-10-25 (section 3), Creative Delight jar installed; Shipped in 1.1.03 (deployed to server and players 2026-10-26); scratch SQ-ABD-01 to 05 PASSED 2026-10-26; ABD-01 to 05 not run in game.** Installed: `abnormals_delight-1.19.2-4.1.2.jar` (`mods/`, `server_mods/`), 1.19.2 Forge. Evaluated
2026-10-25 against `devmods/checkNewerVersionForContent/abnormals_delight-1.21.1-6.0.3.jar` (1.21.1 NeoForge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #26` in [todo/TODO.md](todo/TODO.md). Config: `config/abnormals_delight-common.toml` (one key,
`replaceFDItemGroup`, unchanged in 6.0.3).

**Licence:** Abnormals License 1.0 (June 2020, Copyright (c) 2020 Team Abnormals, All Rights Reserved; owner-supplied 2026-10-25).
Use and dependency are allowed. Copying and modifying are private or for contributing only; publishing, distributing,
sublicensing and selling are not. Modpack clarification: a pack may include it only as an unmodified public copy from the
owners' own sources (their CurseForge page) and marked as included in the pack. The notice must stay with copies. Nothing
proposed below copies code or art.

**Result in one line:** no new items. The 9 new items (8 copper knives, Plum Cabinet) need Caverns and Chasms or
Environmental 1.21, neither installed: the knives wait for the Caverns and Chasms port, the cabinet is dropped. Taken from
the update: Boatload / Upgrade Aquatic salvaging, the Adzuki Curry grid craft, two-pickerelweed recipes, Perch / Pike slice
smelting XP, and 4 Milk Bottles per bucket (all built). The rest is the 1.21 / FD 1.3 data format.

## 1. How it was read

- `python3 tools/jar_diff.py` (530 -> 667 files; 416 added, 279 removed, 107 changed; en_us lang **+9 / -1 / ~0**). Most
  added / removed paths are the 1.21 folder renames (`recipes` -> `recipe`, `tags/items` -> `tags/item`) and recipes moved
  from `data/abnormals_delight/recipes/<mod>/` into the host mod's namespace.
- Recipes compared semantically by type and result, after normalising 1.21 formats (`id` / `item`, `c:` / `forge:` tags,
  `tool` as item ability, default cooking time, `recipe_book_tab`). Tags compared by value with the folder renames folded.
- Both jars decompiled with Vineflower. `ADItems$ADFoods` compared line by line (SRG names mapped by hand).
  `ADEvents`, `ADConfig`, `ADItemTiers` / `ADTiers` read.
- Pack side: which host mods are installed. Of the Abnormals family only **Neapolitan, Upgrade Aquatic, Buzzier Bees,
  Boatload and Incubation** are in `mods/`; Environmental, Atmospheric, Autumnity, Caverns and Chasms and Endergetic are
  not. The live recipe dump (`exported/recipes/all.csv`) has 39 `abnormals_delight:` recipes, all for those five mods.
  `item_dict.js`, `removed_items.js` (nectar, soda, dune platter, yucca slice), `removed_woods.js` (river wood) checked.

## 2. Changes and proposed decisions

### 2.1 Items

| Change | Host mod | Proposed |
|---|---|---|
| 8 Copper Knives (plain, exposed, weathered, oxidized, and waxed of each), `WeatheringKnifeItem`; Caverns and Chasms copper tier (Stone tier without it); crafted from a copper block and a stick; oxidise and wax via C&C data maps | Caverns and Chasms | **postponed** (owner 2026-10-25): part of the Caverns and Chasms port ([TODO.md](todo/TODO.md) ports #1); the copper tier and weathering belong to that port, the knives follow it |
| Plum Cabinet | Environmental 1.21 (plum wood) | **no** (owner 2026-10-25): no new cabinets |
| Cherry Cabinet removed (vanilla cherry has an FD cabinet in 1.20) | Environmental | no: no new cabinets, and not obtainable in the pack today |

All 49 old ids still exist. Of them, only the Neapolitan cake slices, perch / pike lines, Pickerelweed Juice, the
driftwood and river cabinets and the pack's own revivals (Cherry Cookie, Mulberry Cookie, Duck Noodles through pack
recipes) are live.

### 2.2 Food values

Unchanged for all 20 `ADFoods` entries. Cherry Cream Soda and Pickerelweed Juice move from Abnormals' `EffectDrinkItem` to
FD's `DrinkableItem` with an always-edible food carrying the same 15 s Slow Falling / Water Breathing; same behaviour.
`meat()` becomes the `minecraft:meat` tag (1.21). Nothing to carry over.

### 2.3 Recipes on installed host mods

| Change | Proposed |
|---|---|
| Salvaging (FD 1.3 cutting-board recycling, 75% plank): Boatload furnace boats -> boat + furnace (hoe, vanilla woods and modded woods); Upgrade Aquatic driftwood and river furniture (door, trapdoor, sign, fence, gate, plate, button, boat, cabinet) -> 1 plank at 75% (axe), driftwood / river chest and furnace boats -> boat + part (hoe) | **BUILT 2026-10-25 (3.1)**: inside the FD salvaging row ([farmers_delight.md](farmers_delight.md) section 3, Salvaging TODO): add the Boatload furnace boats and the driftwood set (incl. Driftwood Cabinet) to the same `NCH.fdCutting` loop. Skip river wood (removed, `removed_woods.js`), hanging signs, bamboo raft, cherry |
| Adzuki Curry grid craft: Banana + Adzuki Beans -> Dried Banana + Roasted Adzuki Beans (both exist in Neapolitan 4.1.0). The Cooking Pot recipe keeps the raw ones | **BUILT 2026-10-25 (3.2)** |
| Perch / Pike slice smelting, smoking, campfire: 1.0 XP -> 0.35 (also Abnormals' copy of FD's Fried Egg smelting) | **BUILT 2026-10-25 (3.5)**. Only the two furnace recipes were at 1.0 in 4.1.2 (smoking and campfire were already 0.35, as in 6.0.3); FD's own cod / salmon slices are 0.35 in 1.2.4 and 1.3.4 |
| Pickerelweed Juice and Pike with Beetroot take 2 `upgrade_aquatic:pickerelweed` (UA 1.21 merged blue and purple) | **BUILT 2026-10-25 (3.3)**: 1.19.2 UA has the two colours, so the two slots take any two pickerelweed |
| Milk Bottles: 4 per bucket both ways (was 3) | **BUILT 2026-10-25 (3.4)**: the pack gets 3 from Neapolitan's recipes, not from this mod's override |
| `farmersdelight:pumpkin_slice`, cake slices, perch / pike cutting: knife tag -> item ability | skip: format only, see 3.6 |
| Old overrides of FD recipes dropped (Cooking Pot with any water bucket, Wheat Dough from water, Barbecue Stick with `abnormals_delight:barbecue_stick_ingredients`) because FD 1.3 does it itself | skip: the pack keeps 4.1.2's versions; dough is owned by the pack's dough rules |
| New cutting recipes for Atmospheric (firethorn, forsythia, tasselflower, watchful aspen), Environmental (pine, plum) | skip: hosts absent |
| Duck and venison cutting no longer give Bone Meal; Dune Platter takes vanilla cooked rabbit; Glazed Duck takes a baked potato; Truffle Mash pot recipe moved to Environmental; Necromium Knife 1.20 smithing | skip: hosts absent (or removed in the pack) |

### 2.4 Tags, loot, code

| Change | Proposed |
|---|---|
| Foods added to FD 1.3 `meals` / `drinks` and `minecraft:meat`; `c:foods/*` convention tags replace `forge:raw_fishes/perch` etc. | skip: FD 1.2.4 reads none of them ([farmers_delight.md](farmers_delight.md) tag notes); the pack's food-type tags cover it |
| `farmersdelight:mineable/knife` names the cake slice block tags instead of the 6 Neapolitan cakes | skip: same blocks |
| Knife scavenging (feather, leather) adds deer, reindeer, duck, turkey (optional), `killer` -> `attacker` | skip: hosts absent; 1.21 loot context |
| Violet from wild potatoes loot modifier dropped (Environmental) | skip |
| Cake slicing (`ADEvents`): reads any `bites` property, Yucca Gateau checked first | skip: code-only, same result on Neapolitan cakes |
| Data maps for compost (10 sweets at 85%, the pack's `compost_values.js` already sets these) and cabinet fuel (300 ticks) | skip |
| Autumnity snail food / Zebra horse feed / rich soil exclusions for Atmospheric and Endergetic | skip: hosts absent |

## 3. Plans

All data, in Creative Delight's non-member files (`kubejs/data/creativedelight/kubejs/`, recipe ids
`creativedelight:<type>/<suffix>`, helpers `global.NCH`). Each needs the CD jar rebuilt and installed; never while the
game runs. Built 2026-10-25: `:creativeDelight:build` and `installToMods` done with the game closed. New dict keys
(`item_dict.js`, section "Abnormals Delight 6.0.3 salvaging and recipes") need a full restart.

### 3.1 Salvaging (BUILT 2026-10-25)

Extend the `NCH.fdCutting` salvaging block in `farmersdelight/recipes.js` ([farmers_delight.md](farmers_delight.md) 4.3):

- Boatload furnace boats (vanilla woods; `boatload:<wood>_furnace_boat`) -> the wood's boat + Furnace, hoe (as 6.0.3).
- Upgrade Aquatic driftwood: door, trapdoor, sign, fence, gate, plate, button, boat and Driftwood Cabinet -> 1 driftwood
  plank at 75% (axe); driftwood chest boat -> boat + Chest, furnace boat -> boat + Furnace.
- Skip: river wood (removed, `removed_woods.js`), hanging signs, bamboo raft, cherry, absent hosts. Check each item key
  exists in `item_dict.js` first; add missing keys there (define once). The 4.1.2 driftwood door / sign / trapdoor cutting
  recipes (`abnormals_delight:upgrade_aquatic/cutting/driftwood_*`) are removed in favour of the one recipe.

As built (`farmersdelight/recipes.js`, after the minecarts; ids `salvaging/<wood>_furnace_boat`,
`salvaging/driftwood_furniture`, `salvaging/driftwood_chest_boat`, `salvaging/driftwood_furnace_boat`):

- Furnace boats with a hoe, as 6.0.3. All 9 vanilla woods: Boatload 1.19.2 has crimson and warped boats of its own, so
  those two give `boatload:crimson_boat` / `warped_boat`. Guarded by `Platform.isLoaded('boatload')`.
- Boatload's crimson and warped boat and chest boat are salvaged too (6.0.3 has the chest boats): boat -> plank at 75%
  (axe), chest boat -> boat + Chest.
- Chest boats (driftwood, crimson, warped) use the axe, like the pack's vanilla chest boats above them (6.0.3 and FD
  1.3.4 use a hoe; the pack's vanilla rows already chose the axe).
- New dict keys: the driftwood set (`planks_driftwood`, `door_driftwood`, ... `driftwood_cabinet`, `driftwood_boat`,
  `driftwood_chest_boat`, `driftwood_furnace_boat`), the 8 other Boatload furnace boats and `boatload_<crimson|warped>_
  (chest_)boat`. The Boatload boats get a `boatload_` prefix so the vanilla-wood loop above (boat -> plank) is unchanged.

### 3.2 Adzuki Curry grid craft (BUILT 2026-10-25)

Remove `abnormals_delight:neapolitan/adzuki_curry`; `R.shapeless` Roasted Adzuki Beans, Dried Banana, Carrot, Pumpkin Slice,
Bowl -> Adzuki Curry (Neapolitan 4.1.0 has both cooked parts). The Cooking Pot recipe keeps the raw beans and banana.
Heatless craft, so the id ends `_manual_only` like the pack's other grid crafts of pot dishes.

As built: `abnormals_delight/neapolitan.js` (requires `abnormals_delight`, `neapolitan`), id
`shapeless/adzuki_curry_cooked_manual_only`. The carrot is `#forge:crops/carrot` (`i.carrot`, the pack's usual key).

### 3.3 Pickerelweed Juice and Pike with Beetroot (BUILT 2026-10-25)

6.0.3 takes `2 x pickerelweed` because 1.21 Upgrade Aquatic merged the colours. 1.19.2 has blue and purple and 4.1.2 needs
one of each. Plan: a tag of the two (`global.tags`, in `upgrade_aquatic/tags.js`), and the two pot recipes
(`R.fdCooking`) take that tag twice, so two blues, two purples or one of each all work. Remove
`abnormals_delight:pickerelweed_juice` and `abnormals_delight:pike_with_beetroot`; keep results, 1.0 xp, 200 ticks, the
drinks / meals tab, the sugar, the `#forge:raw_fishes/pike` and the beetroot as they are.

As built: tag `#creativeengineering:pickerelweed` (`i.t_pickerelweed`) in `upgrade_aquatic/tags.js`; recipes in
`abnormals_delight/upgrade_aquatic.js`, ids `farmersdelightcooking/pickerelweed_juice` and `.../pike_with_beetroot`. New
key `i.raw_pike` (`#forge:raw_fishes/pike`). The beetroot is `i.beetroot` (`#forge:crops/beetroot`). **Not kept: the
recipe book tab.** `NCH.fdCooking` has no tab argument (no pack Cooking Pot recipe sets one), so both land on FD's default
tab instead of drinks / meals; adding a tab option to the helper needs a patcherMod rebuild.

### 3.4 Milk Bottles: 4 per bucket (BUILT 2026-10-25)

Farmer's Delight 1.2.4 and 1.3.4 both make 4 bottles per bucket, but Abnormals Delight turns FD's two recipes off while
Neapolitan is loaded, so the pack runs Neapolitan's: bucket + 3 glass bottles -> 3 bottles, and bucket + 3
`#forge:bottles/milk` -> bucket (`item_merges.js`, comment at the top of the milk block). Plan: remove
`neapolitan:milk/milk_bottles_from_bucket` and `neapolitan:milk/milk_bucket_from_bottles`, add with `R.shapeless`:
milk bucket + 4 glass bottles -> 4 Farmer's Delight Milk Bottles, and bucket + 4 `#forge:bottles/milk` -> milk bucket. Create's
spout and drain already use 250 mB per bottle (4 per bucket). Update the `item_merges.js` comment, MG-06
([item_merges.md](item_merges.md)) and AL-53 only if it names 3. Player-visible: changelog bullet.

As built: `abnormals_delight/neapolitan.js`, ids `shapeless/milk_bottles_from_bucket` (`#forge:buckets/milk` + 4 glass
bottles, as Neapolitan's took the tag) and `shapeless/milk_bucket_from_bottles`. `item_merges.js` comment, item_merges.md
2.2 and MG-06 updated; AL-53 already says 4. Changelog bullet added (Food & Cooking).

### 3.5 Perch and Pike slice XP (BUILT 2026-10-25)

`kubejs/data/creativedelight/kubejs/abnormals_delight/upgrade_aquatic.js` (requires `abnormals_delight` and
`upgrade_aquatic`): removes `abnormals_delight:upgrade_aquatic/cooked_perch_slice` and `.../cooked_pike_slice` and re-adds
them with `NCH.smelting` at 0.35 xp, 200 ticks. Changelog bullet added (Food & Cooking). Test SQ-ABD-01 queued.

### 3.6 What the `pumpkin_slice` / cake slice line meant (no change)

The row was about the cutting-board recipes' tool field, not about what they make. Farmer's Delight 1.2.4 writes the tool
as a tag (`"tool": {"tag": "forge:tools/knives"}`); 1.3 writes it as an ability (`farmersdelight:item_ability`
`knife_dig`, plus the `c:tools/knife` tag), so any item that can dig like a knife works. 6.0.3 switched its own cutting
recipes to that form: Neapolitan's cake -> cake slices (the six cakes), Perch and Pike -> slices, and Autumnity's pumpkin ->
`large_pumpkin_slice`. The recipes still turn the same input into the same output, and the 1.2.4 tag form is the only one
this FD reads, so nothing carries over. (`farmersdelight:pumpkin_slice` also appears in the Adzuki Curry recipes, unchanged.)

## 4. Tests (once built)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-ABD-02, -03, -04, -05.

Prefix `ABD-`. SQ-ABD-01 to 05 PASSED by `/zq` in the 2026-10-26 scratch run ([scratch_queue.md](testing/scratch_queue.md)), -02 to -05 again r1102b 2026-11-02; the in-game ABD rows stay pending. Client half: CQ-ABD-01 (JEI pages, deep scan 2026-11-02).

- [ ] **ABD-01 Slice XP:** smelt a Perch Slice and a Pike Slice in a furnace -> 0.35 xp each; smoker and campfire unchanged.
- [ ] **ABD-02 Salvaging (3.1):** driftwood door / sign / trapdoor / fence / gate / plate / button /
  boat / cabinet on the cutting board with an axe -> plank at about 75%; a furnace boat (oak, crimson, driftwood) with a hoe
  -> boat + Furnace; driftwood and crimson chest boat with an axe -> boat + Chest; Boatload Crimson Boat -> crimson plank.
- [ ] **ABD-03 Adzuki Curry (3.2):** crafting grid with the cooked parts, bowl in -> curry; the raw beans and banana no longer
  craft it in the grid; the Cooking Pot still takes them.
- [ ] **ABD-04 Pickerelweed (3.3):** Cooking Pot with two blue, two purple and one of each -> juice; same for Pike
  with Beetroot.
- [ ] **ABD-05 Milk (3.4):** bucket + 4 bottles -> 4 FD bottles; 4 bottles + bucket -> milk bucket; JEI shows no
  3-bottle recipe.

## Owner ideas (scratchpad 2026-10-26)

- Re-add Cherry Cream Soda (removed 2026-10-23, N-04): find the removed recipe first. Possibly a fruit soda line, mostly in glass bottles, some in cans, with dyes, cheap and worse than proper drinks: `food/drinks #11`.
