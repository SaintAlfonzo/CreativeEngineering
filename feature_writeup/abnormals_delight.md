# Abnormals Delight 6.0.3 against the installed 4.1.2

**Nothing built.** Installed: `abnormals_delight-1.19.2-4.1.2.jar` (`mods/`, `server_mods/`), 1.19.2 Forge. Evaluated
2026-10-25 against `devmods/checkNewerVersionForContent/abnormals_delight-1.21.1-6.0.3.jar` (1.21.1 NeoForge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #26` in [todo/TODO.md](todo/TODO.md). Config: `config/abnormals_delight-common.toml` (one key,
`replaceFDItemGroup`, unchanged in 6.0.3).

**Licence:** Abnormals License 1.0 (June 2020, Copyright (c) 2020 Team Abnormals, All Rights Reserved; owner-supplied 2026-10-25).
Use and dependency are allowed. Copying and modifying are private or for contributing only; publishing, distributing,
sublicensing and selling are not. Modpack clarification: a pack may include it only as an unmodified public copy from the
owners' own sources (their CurseForge page) and marked as included in the pack. The notice must stay with copies. Nothing
proposed below copies code or art.

**Result in one line:** nothing worth porting. The 9 new items (8 copper knives, Plum Cabinet) need Caverns and Chasms or
Environmental 1.21, neither installed; everything else is the 1.21 / FD 1.3 data format. The useful part is the salvaging
recipes for Boatload and Upgrade Aquatic woods, which feed the FD salvaging carry-over already planned.

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
| 8 Copper Knives (plain, exposed, weathered, oxidized, and waxed of each), `WeatheringKnifeItem`; Caverns and Chasms copper tier (Stone tier without it); crafted from a copper block and a stick; oxidise and wax via C&C data maps | Caverns and Chasms | skip: C&C not installed, recipe gated on it |
| Plum Cabinet | Environmental 1.21 (plum wood) | skip |
| Cherry Cabinet removed (vanilla cherry has an FD cabinet in 1.20) | Environmental | skip: not obtainable in the pack today |

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
| Salvaging (FD 1.3 cutting-board recycling, 75% plank): Boatload furnace boats -> boat + furnace (hoe, vanilla woods and modded woods); Upgrade Aquatic driftwood and river furniture (door, trapdoor, sign, fence, gate, plate, button, boat, cabinet) -> 1 plank at 75% (axe), driftwood / river chest and furnace boats -> boat + part (hoe) | **carry over** inside the FD salvaging row ([farmers_delight.md](farmers_delight.md) section 3, Salvaging TODO): add the Boatload furnace boats and the driftwood set (incl. Driftwood Cabinet) to the same `NCH.fdCutting` loop. Skip river wood (removed, `removed_woods.js`), hanging signs, bamboo raft, cherry |
| Adzuki Curry grid craft: Banana + Adzuki Beans -> Dried Banana + Roasted Adzuki Beans (both exist in Neapolitan 4.1.0). The Cooking Pot recipe keeps the raw ones | owner's call; small, makes the no-cooking craft take cooked parts |
| Perch / Pike slice smelting, smoking, campfire: 1.0 XP -> 0.35 (also Abnormals' copy of FD's Fried Egg smelting) | proposed: **carry over**; FD's own cod / salmon slices are 0.35 in both 1.2.4 and 1.3.4, so 4.1.2's 1.0 is the outlier |
| Pickerelweed Juice and Pike with Beetroot take 2 `upgrade_aquatic:pickerelweed` (UA 1.21 merged blue and purple) | skip: 1.19.2 UA has the two colours |
| Milk Bottles: 4 per bucket both ways (was 3) | skip here; FD 1.3 change, see [farmers_delight.md](farmers_delight.md) |
| `farmersdelight:pumpkin_slice`, cake slices, perch / pike cutting: knife tag -> item ability | skip: format only |
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

## 3. Open questions for the owner

1. Add the Boatload furnace boats and the driftwood furniture set to the FD salvaging carry-over (proposed yes)?
2. Adzuki Curry grid craft with Dried Banana and Roasted Adzuki Beans?
3. Perch / Pike slice cooking XP 1.0 -> 0.35, in line with FD's slices (proposed yes)?

## 4. Tests (once built)

None yet. Prefix `ABD-`. The salvaging rows would be tested with the FD salvaging tests.
