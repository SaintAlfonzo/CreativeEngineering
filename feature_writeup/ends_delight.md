# End's Delight and Ender's Delight

**Nothing integrated.** End's Delight (`ends_delight-1.19.2-2.1.jar`, native 1.19.2, so no port) was dropped into `mods/` on
2026-10-23 beside the existing Ender's Delight (`endersdelight-1.19-1.2.2`). It has a `mod_sides.md` row but no tags, recipe
rules, food tuning or names/JEI pass. Decision rows: `largecontent/mod-additions #4` and `#15` in [todo/TODO.md](todo/TODO.md);
plan: [briefs/largecontent_mod-additions.md](briefs/largecontent_mod-additions.md). Test prefix: none yet (boot check `SQ-AD-00`).

Split out of the former combined add-ons survey (survey written 2026-10-23, planned rows added 2026-10-24).
"Pack" means everything installed in `mods/` plus the Veggies and Vintage ports. Item lists were read from the jars' `assets/*/models/item`, recipes and loot modifiers from `data/`.

**Licence:** End's Delight is MIT; Ender's Delight is All Rights Reserved (owner-supplied 2026-10-25, matching the installed
jars' `mods.toml` as described in 3.2).

Related pages: [ocean_delight.md](ocean_delight.md), [hostile_delight.md](hostile_delight.md), [miners_delight.md](miners_delight.md).

## 0. At a glance

| Mod | Jar | Version | Items | Loot / worldgen | Verdict so far |
|---|---|---|---|---|---|
| End's Delight | `ends_delight-1.19.2-2.1.jar` | 2.1 | 64 | Dragon, shulker, enderman, endermite drops (loot modifiers); 1 worldgen file | Overlaps Ender's Delight on chorus and shulker; see section 1 |
| Ender's Delight | `endersdelight-1.19-1.2.2` | 1.2.2 | 28 | knife / loot drops | existing |

Needs Farmer's Delight only.

## 1. End's Delight vs Ender's Delight (installed: `endersdelight-1.19-1.2.2`)

Both are "End food" add-ons. Neither depends on the other, they do not share ids, and each brings its own stove.

| Theme | Ender's Delight (existing, 28 items) | End's Delight (new, 64 items) | Overlap |
|---|---|---|---|
| Stove | `endstone_stove` | `end_stove` | Duplicate function, two blocks. Pick one or let both exist |
| Chorus | `chorus_crate`, `chorus_juice`, `chorus_pie` + slice, `chorus_stew` | `chorus_fruit_crate`, `chorus_fruit_pie` + slice, `chorus_flower_pie`, `chorus_fruit_milk_tea`, `chorus_fruit_popsicle`, `chorus_fruit_wine`, `chorus_sauce`, `chorus_cookie`, `chorus_fruit_grain`, dried / succulent / flower tea | Direct: crate, pie + slice. Pie slice is the same concept twice. Fruits Delight chorus jam / jello and Create Central Kitchen chorus pies (recipe-only) also exist |
| Shulker | `shulker_filet`, `shulker_mollusk`, `shulker_bowl`, `stuffed_shulker` (+ bowl) | `shulker_meat` + slice, `roasted_shulker_meat` + slice, `grilled_shulker`, `shulker_omelette`, `shulker_soup`, `stir_fried_shulker_meat` | Direct: both turn shulkers into meat and dishes. Needs a shared tag |
| Enderman | `enderman_sight`, `sight_fragment`, `uncanny_cookies` | `enderman_gristle`, `enderman_gristle_stew`, `ender_noodle`, `ender_sausage` | Same mob, different drop; both are knife/loot-modifier drops |
| Endermite | `mite_crust`, `endermite_stew` (+ wood) | `raw_ender_mite_meat`, `dried_endermite_meat` | Same mob, two drops; tag `forge:raw_ender_mite_meat` is End's only |
| Pearl | `pearl_pasta` (+ wood), `ender_paella` (+ wood), `strange_eclair`, `crispy_skewer`, `crawling_sandwich`, `twisted_cereal` | `ender_pearl_grain`, `ender_bamboo_rice`, `ender_congee`, `stuffed_rice_cake` | Rice-style dishes overlap the pearl pasta / paella idea |
| Unique to End's | | Dragon meat line (`raw_dragon_meat`, `roasted_dragon_meat`, `dragon_leg`, `dragon_meat_stew`, steak, smoked leg), dragon egg line (`fried_dragon_egg`, `steamed_dragon_egg`, `liquid_dragon_egg`), dragon tooth / egg shell / end stone / purpur knives, bubble tea, dragon breath soda / soup | Dragon meat has no equivalent in the pack. Dropped by the Ender Dragon when killed with a tool from the `ends_delight:dragon_loot_tools` tag, or with a Nether's Delight machete (the `nd_add_*` modifiers) |
| Unique to Ender's | `shulker_bowl` set, wooden-bowl variants of every stew | | |

Files in End's Delight touch `create` (belt-upright tag) and `forge:tools/knives`; it adds knife tags, so its four
knives join the knife set the pack tunes (`tuneTools`). Its loot modifiers come in two sets (`add_*` and `nd_add_*`),
the `nd_` set being the same drops for Nether's Delight machetes (installed), so machetes can harvest dragon, shulker, endermite and enderman parts too.

Open decision: keep both, keep one, or keep both and merge the duplicated ingredient lines (chorus crate, shulker meat
tags). Recommendation: keep both (little real clash beyond the stove and chorus pie), tag the shared ingredients, and
let the owner pick which chorus pie the pack's fruit/jam recipes use.

## 2. Integration checklist (End's rows of `mod-additions #4` / `#9`)

## 3. Integration work common to all five (checklist for `mod-additions #4`)

| `mod_sides.md` rows | One row per jar: all five are `both`, none has a library dependency beyond Farmer's Delight (Oceanic: check AppleSkin / JEED / JEI entries) |
| Tag unification | Squid tentacles, shulker meat, sea pickles, shrimp, chorus pie slices; add through `global.tags` |
| Food tuning | Run the new items through the food-tuning rules and the saturation word; the pack's `food_types.json` generator reads `/dumpall` output |
| Cans | Decide which new foods get a can type (`gen_can_types.py`) |
| JEI / names | JEI coverage and missing-names audit |
| Loot | Several loot modifiers target the same mobs (squid x3, shulker x2, enderman x2); check the loot system and the mob-drop rules |
| Advancements | None of these mods may use advancements for unlocks (pack drops them) |
| Done docs | Writeup, test list, changelog bullet (`unreleased.md`) on each integration step |

## 3. Planned (scratchpad 2026-10-24)

Rows added to [TODO](todo/TODO.md); nothing is built, so the test list stays empty.

- [TODO](todo/TODO.md) `largecontent/mod-additions #4`: gains the succulent and fungus options, tracked as
  `largecontent/mod-additions #15`.
- [TODO](todo/TODO.md) `largecontent/mod-additions #15`: decide whether `chorus_succulent` (a worldgen plant, 9 food recipes) is kept
  or replaced, and widen the fungus inputs (crimson fungus today) to more End and Nether fungi with `ports/fungiport`.
  Open: keep or replace the succulent.

## 3.1 Newer jars reviewed (2026-10-25)

Plans only; nothing here is built. The newer jars sit in `DecompiledMods/` as references and are **not** 1.19.2 builds, so
nothing is installed from them: `endersdelight-1.1.4.jar` (1.20.1; installed is 1.2.2, whose number is higher only because the
1.20 line is numbered separately) and `ends_delight-2.6.1+forge.1.20.1.jar` (installed is 2.1). Both newer jars declare MIT
(`mods.toml`, no LICENSE file); the installed Ender's 1.2.2 declares **All rights reserved** (see 3.2). Anything adopted is
rebuilt in the pack, same behaviour, no old-version or cross-mod conversions. The diff was made against the installed jars.

Real new content is in Ender's Delight only (4 End plants, 6 items, 1 structure, 2 block loot tables). End's Delight 2.6.1 has
no new items; its changes are drops, recipes and tags.

### Decided 2026-10-25

**Ender's Delight: plants and the Void Pepper**

| Change | Decision | Plan |
|---|---|---|
| 4 new End plants: **Amberveils** (`amberveil_mushroom`, drops `amberveil`), **Ethereal Saffron** (`ethereal_saffron_bush`, drops `ethereal_saffron`), **Chorusflame** (`chorus_flame_block`, age 0 to 2, drops `chorusflame`: 1 / 2 / 3 by age, Fortune adds), **Void Pepper Sprout** (`voidpepper_block`, drops `voidpepper`) | Fold into the pack's wild plant generation | One rule per plant in `wild_crops.json` ([briefs/farming_wild-crops.md](briefs/farming_wild-crops.md), applier `wild_crops.js`), End land biomes only (the mod used barrens, highlands, midlands, small islands; chance 1 in 10 / 15 / 15 / 25 per chunk for amberveil / chorusflame / void pepper / saffron). Do not ship the mod's four biome modifiers (they would double the generation). Densities go in the generator's table like the other wild crops |
| **Void Pepper** is a spice | Mark it for the spice work | `crushed_voidpepper` (and `voidpepper`) are the first members of a `#creativeengineering:spices` tag, created with the spice work in [briefs/food_seasoning.md](briefs/food_seasoning.md) (it says there is no pepper or spice item in the dictionary today). Until then it is only an ingredient. Saffron is a spice candidate too: see "needs a decision" |
| Void Pepper **behaviour** | Implement it | The sprout is a multiface block (one boolean per face, glow-lichen style, per-face collision shape) with `spreading` and `pepper` states. Random tick while spreading: 2/3 spread to a free spot within 7 blocks that touches end stone (up to 15 tries), 1/3 stop spreading and fruit. A fruiting sprout drops 3 `voidpepper`, a non-fruiting one 1 (Fortune adds to both). Needs Java (multiface shapes, a block entity that stores its origin), so a small in-tree mod in the `incubationAdditions` pattern, e.g. `development/endersDelightAdditions`, plus a `mod_sides.md` row. The `end_flames` particle (Chorusflame) comes with it. Confirm the in-tree mod route before building |
| **Ground rule** for all 4 plants | Survive and grow only on **end stone** (slower) and **tilled end stone** (faster) | Tilled end stone is Endercrop's `endercrop:tilled_end_stone` (installed; a `FarmBlock`, dry and moist textures). The mod's bush base class also allowed any farmland: drop that. Put the two soils in tags (e.g. `end_plant_soil_slow`, `end_plant_soil_fast`) so a third soil can be added without code, and the growth chance per tier in CEConfig. Amberveils and Ethereal Saffron have **no growth stage** in the mod (place and pick), so "grow" needs a design: give them an age stage with a random-tick growth like Chorusflame, wild ones spawning mature. Open: does Endercrop's moist / dry state count as two speeds |
| **Fertile end stone** | Mark as future | A new block above tilled end stone. **Craft and texture are not designed**; role beyond "best soil" is open. Listed here so the soil tags leave room for it |

**Ender's Delight: structure, loot and recipes**

| Change | Decision | Plan |
|---|---|---|
| New structure **End House** (`end_house`: jigsaw pool, 25 / 15 spacing, End highlands and midlands, `beard_thin`; the template `end_house.nbt` has no chests, barrels or spawners, so it needs no loot table) | Same pool as the End pillager towers | Add it to the `type/end_constructed` tag in `structure_tags.json` and give it a biome fit; the pack's group grid and spacing replace the mod's own `structure_set`. Note: the Mysticrift End pillager tower is today in `land/end` and in `group/pillager_settlements`, **not** in `type/end_constructed` (that tag holds end cities, MES, Philip's End ruins and gate fortress). Which of the two the user means is a decision below. The mod uses its own structure type (`EndHouseStructure`, not read yet): check whether it is only `minecraft:jigsaw` with a surface check, and if so use plain jigsaw so no Java is needed. Terrain: the soft-beard adaptation system |
| **Uncanny Cookies**: newer recipe is 1 mite crust + sugar + 2 wheat for 1 (was mite crust + 2 wheat for 8), plus the `supplementaries:cookies` tag | Keep the current implementation | No change. The cookie is already in the mixed jar families ([mixed_jars.md](mixed_jars.md)) |
| **Chorus Crate and End Stone Stove** loot tables | Checked | See "Loot table check" below: the installed 1.2.2 jar has none, so both drop nothing today. Add self-drop tables (the newer jar's two files are plain self drops) |
| Mob loot that needs a knife | JEI info lines and tooltips | See "Knife drops" below |
| 5 new **dishes** | Not mentioned; listed so none is lost | See the dish table below |

*Loot table check (installed `endersdelight-1.19-1.2.2`).* The jar ships `loot_modifiers` and tags only; it has **no
`loot_tables`** directory, and the pack has none for it either (`kubejs/data` has no `endersdelight`). Of its four blocks
(`chorus_crate`, `chorus_pie`, `endstone_stove`, `stuffed_shulker_block`) the newer jar adds tables for the crate and the
stove only (both drop themselves). So in survival the crate and the stove drop nothing when broken, by the jar's contents;
**not yet seen in game** (queue the check). Fix: two self-drop loot tables in `kubejs/data/endersdelight/loot_tables/blocks/`,
and mining tags (the newer jar tags the crate for axes and the stove for pickaxes; the pack's `blocktags.js` lists
`chorus_crate` only in the BlockRunner wood list, which is a different tag). The pie and the stuffed shulker block behave
like Farmer's Delight feast blocks and got no table in the newer jar either.

*Knife drops (JEI info lines and tooltips).* Mob loot that needs a knife (`farmersdelight:tools/knives`):

| Item | Mod | Mob | Newer jar's info line |
|---|---|---|---|
| Enderman Sight | Ender's | Enderman | "Obtained by killing an Enderman with a Knife" |
| Mite Crust | Ender's | Endermite | "Obtained by killing an Endermite with a Knife" |
| Shulker Mollusk | Ender's | Shulker | "Obtained by killing a Shulker with a Knife" |
| Enderman Gristle | End's | Enderman | none in the mod |
| Raw Ender Mite Meat, Dried Endermite Meat | End's | Endermite | none in the mod |
| Shulker Meat | End's | Shulker | none in the mod |

Add a JEI info page and an item tooltip for each (same pattern as the shrimp and caviar pages in
[ocean_delight.md](ocean_delight.md) section 5; tooltips per [tooltips.md](tooltips.md)). The "Knife and Machete Drops"
JEI tab ([jei_world_tabs.md](jei_world_tabs.md)) lists the active loot modifiers separately and is unaffected.

**End's Delight: dragon, tea, knife**

| Change | Decision | Plan |
|---|---|---|
| **Dragon drops**: in 2.1, raw dragon meat, dragon leg and dragon tooth drop only when the Ender Dragon is killed with a tool in the `ends_delight:dragon_loot_tools` tag, or with a Nether's Delight machete. 2.6.1 drops them with **no tool** and adds extra meat and an extra tooth when killed with a knife | Adopt: no tool needed, tooltip about the knife extras | Override the three `add_dragon_*` loot modifiers in `kubejs/data` without the killer tool condition, and add two extras gated on `farmersdelight:tools/knives` (use `farmersdelight:add_item`; the newer meat extra uses an `ends_delight:dragon_leg` serializer type, check which types 2.1 registers). Tooltip on Raw Dragon Meat, Dragon Leg and Dragon Tooth: extra drops when the dragon is killed with a knife. Add the extras to the JEI info page. The `dragon_loot_tools` tag becomes unused. There is no existing "no tool needed" row in the TODO; this is its own change |
| **Machete drops** (the seven `nd_add_*` modifiers: dragon meat / leg / tooth, endermite meat, dried endermite meat, shulker meat, enderman gristle, for Nether's Delight machetes) | Mark for a future machete review | Leave them active as in 2.1 (2.6.1 removed them all). The review decides, together with the other machete drops, whether machetes keep harvesting End parts |
| **Dragon Tooth Knife** recipe: 2.6.1 smiths the Dragon Egg Shell Knife with a Dragon Tooth (plus the 1.20 netherite upgrade template); 2.1 crafts them shapeless | Adopt the smithing recipe; mark for the End gear system | 1.19.2 smithing has two inputs, so: base = egg shell knife, addition = dragon tooth, no template. Through `global.NCH.smithing(...)`, replacing the shapeless craft. **Future:** fold into the End gear system with the other above-netherite recipes, values from CEConfig ([TODO](todo/TODO.md) `gear #12` "End gear recipes configurable", dragon gear) |
| **Chorus Succulent** (worldgen plant, 9 food recipes): 2.1 has a malformed single file (`worldgen/configured-feature/configured-feature.json`, a folder name the game does not read) plus a Java feature; 2.6.1 has a proper configured + placed feature and a biome modifier | Add to the current world generation system | A `wild_crops.json` rule like the Ender's plants, End biomes. First confirm whether 2.1 generates any succulent in the End at all (scratch check). Answers the "keep or replace the succulent" half of `largecontent/mod-additions #15` with **keep** (the row itself is not edited yet) |
| **Bubble Tea** (and its pot / Respite recipes) | Mark: original recipe in the Respite brewing, a more expensive version in the Cooking Pot; leave current for now | The Respite brewing recipe stays as 2.1 has it: Chorus Fruit Milk Tea + Ender Pearl Grain. 2.1 already has a Cooking Pot recipe too (milk + chorus fruit or grain + ender pearl grain, unchanged in 2.6.1, so it is live in the pack today); 2.6.1 only drops the Respite one (3.3). The pot version should cost **more** than the Respite one, so the live pot recipe changes; amount to decide. Nothing changes now. Related: [TODO](todo/TODO.md) `food/drinks #7` (teas in both the Kettle and the pot; the pot uses 2 fruit per tea) |

**Ender's Delight: the 5 new dishes** (all served in the Shulker Bowl; no wooden-bowl variants exist for them, and the
newer jar dropped the Twisted Cereal wooden-bowl recipe, an item the pack already removes)

| Dish | Pot recipe | Food (hunger / saturation) and effect |
|---|---|---|
| Amberveil Stew | amberveil, crushed void pepper, ethereal saffron, chorus flower | 4 / 0.5, Regeneration 0:30 |
| Amberveiled Curry | amberveil, crushed void pepper, chorus flower | 5 / 0.5, Absorption 3:00 |
| Veil of Flames Risotto | amberveil, ethereal saffron, chorus flower | 7 / 0.9, Fire Resistance 2:00 |
| Chicken Curry | amberveil, ethereal saffron, crushed void pepper, chicken, chicken cuts, rice | 7 / 0.9, Nourishment 5:00 |
| Steak With French Fries | crushed void pepper, beef, milk bottle, potato | 6 / 0.7 |

Crushed Void Pepper: Farmer's Delight knife, voidpepper to 2; Create milling, voidpepper to 2 with a 30% chance of a third.
Through the item dictionary, `global.NCH`, food tuning and a can decision like any new food (the section 3 checklist). The item dictionary has no other saffron, risotto or fries; it has three other curries (coconut, tea, adzuki) under different names, so no merge.

### Other End changes that need a decision

| Change | Why it needs a decision | Recommendation |
|---|---|---|
| **Which pool for the End House** | The tower is in `group/pillager_settlements` (47 members, one shared grid with the outposts) and `land/end`; `type/end_constructed` does not list it. The house is a house, not a pillager camp | `type/end_constructed` plus its own biome fit; do not put it on the pillager grid. Resolve with the pending "Nether / End / underground grouping pass" of 7.1. Also decide whether the tower should join `type/end_constructed` |
| **House biomes**: mod uses End highlands and midlands only | The tower uses all `land/end` biomes | Highlands and midlands (as the mod), unless the biome-fit pass says otherwise |
| **Saffron as a spice** | It is the classic spice but the user named only Void Pepper | Yes, add Ethereal Saffron to the spice tag with the spice work |
| **Tea moves** (End's 2.6.1 drops the Respite brewing of Chorus Flower Tea and Chorus Fruit Milk Tea, like the Bubble Tea; their pot recipes are in 2.1 already) | Same pattern as the Bubble Tea decision | Same rule: Respite keeps the original, the pot gets a pricier version, with `food/drinks #7` |
| **`forge:storage_blocks/chorus_fruit`** tag (new in 2.6.1: End's `chorus_fruit_crate`) | Would join End's crate with Ender's `chorus_crate`; the TODO "chorus crate machine unpacking missing" is the same crate | Yes: tag both crates through `global.tags`, one unpack route for both |
| **Mortar / spice route for Crushed Void Pepper** | The pack has a mortar catalog; the mod offers knife and Create milling only | Add mortar if the spice work uses it for other spices; otherwise knife and milling only |
| **Stuffed Shulker message**: "You need a Shulker Bowl to eat this" (was "a bowl") | One-line lang fix | Adopt through a lang override |
| **Chorusflame, Amberveils, Saffron as farmable crops** | No seeds exist: the items place the block. Do the plants replant, or only generate wild | Plant from the item (as the mod), farmable on the two soils; confirm |
| **1.20 data-only changes**: `was_watered` damage type and renamed death message key, `enderman_gristle_teleport` damage type, Create belt tag, 1.20 loot / tag folder renames | Not applicable to 1.19.2 (damage sources are Java here) | No action |

### Proposed checks (queue when built)

| ID | Check |
|---|---|
| EN-01 | A broken Chorus Crate and End Stone Stove drop themselves (first run on the unchanged pack to confirm they drop nothing today) |
| EN-02 | The 4 plants generate wild in the End on end stone only, at the table's density; none generates twice |
| EN-03 | Each plant places and survives only on end stone and tilled end stone; breaks on farmland, dirt, grass |
| EN-04 | Growth is slower on end stone than on tilled end stone (measure over 20 random-tick samples) |
| EN-05 | Void Pepper Sprout spreads to end-stone-adjacent spots within 7 blocks, then fruits; drops 3 when fruiting, 1 when not |
| EN-06 | End House generates in the chosen biomes, on the chosen pool's spacing; no chests; never over the void or below y 50 |
| EN-07 | Dragon killed bare-handed drops meat, leg and tooth; with a knife it drops extra meat and an extra tooth |
| EN-08 | Tooltips and JEI pages on the knife drops (6 items) and on the dragon drops |
| EN-09 | Dragon Tooth Knife smithed from the egg shell knife and a tooth; the shapeless craft is gone |
| EN-10 | Chorus Succulent generates in the End (and whether it did before) |

Changelog: no bullet yet (plans only). The bullets come with the build.

## 3.2 Ender's Delight devmods evaluation (method pass, 2026-10-25)

The [devmods_evaluation.md](devmods_evaluation.md) method run on `endersdelight-1.19-1.2.2` -> `endersdelight-1.1.4`
(`tools/jar_diff.py`, `javap` on `ModFoods` / `ModItems` / `ModBlocks` / `EndHouseStructure`, template NBT read). It confirms
3.1 (77 files added, 1 removed, 17 changed; the 14 new lang keys are the 4 plants, 6 items, 3 JEI info lines and the death
message) and adds the following. TODO: `largecontent/mod-additions #28`.

| Finding | Class | Decision |
|---|---|---|
| **Licence:** 1.1.4 declares `license="MIT"` (author Axedgaming); the installed 1.2.2 declares "All rights reserved" (Ax3dgaming and Furti Two). The MIT grant covers what 1.1.4 ships, which includes art for all 28 existing items | licence | owner's call: this may open a third route beside [creative_delight.md](creative_delight.md) 2.1 (a) / (b): a 1.19.2 build made from the MIT 1.1.4 sources and assets. Open question: does Furti Two's part of 1.2.2 appear in 1.1.4 (1.1.4 lists only Axedgaming)? Ask upstream before relying on it |
| Food values of the 13 existing food definitions (finger, meal, feast, pasta, diet and the rest) | value change | none: identical. Only the 5 new dish foods are added (values in the dish table above) |
| The 5 wooden-bowl variants (`chorus_stew_wood`, `endermite_stew_wood`, `ender_paella_wood`, `pearl_pasta_wood`, `twisted_cereal_wood`) are no longer registered | removal | none: the pack already removes all five (`removed_items.js`, `cooking.js`) |
| 12 retextured items: Strange Eclair, Mite Crust, Shulker Bowl, Pearl Pasta, Ender Paella, Chorus Stew, Stuffed Shulker (block item and bowl), Sight Fragment, Enderman Sight, Twisted Cereal, Endermite Stew. Most are small touch-ups; Mite Crust is reshaped, and the Stuffed Shulker bowl item goes from a wooden bowl to a Shulker Bowl | art | proposed: carry over at least the Stuffed Shulker bowl (matches the "Shulker Bowl" lang fix above) as a resource-pack override, MIT notice with it; the other 11 owner's call |
| **End House structure type** (open in 3.1): `EndHouseStructure` is plain jigsaw (same fields as `minecraft:jigsaw`, size 1, max distance 64) plus one check: skip unless the world-surface height at the chunk centre is 50 or more, so it never spawns over the void | code | use `minecraft:jigsaw` and rebuild the height check in the pack (ground matching, [structure_tuning.md](structure_tuning.md) 3.8, only swaps base blocks and does not reject a void start): check first whether an existing biome-fit or placement filter already covers it, else a small patcherMod check; add the void case to EN-06 |
| End House template: `end_house.nbt` is a 1.20.1 save (DataVersion 3465), 9 x 6 x 16, no entities, no containers. It places `amberveil_mushroom`, `chorus_flame_block`, `voidpepper_block` and `endstone_stove`, plus a Farmer's Delight skillet; all vanilla blocks exist in 1.19.2 | format | the house ships only after the plants exist (else those blocks load as air); re-save at 1.19.2 DataVersion through `port_structure_jars.py` |
| `examplemod.mixins.json` (empty, its one client mixin class is not in the jar), `accesstransformer.cfg`, particle and block entity registries, `ModTags` dropped (tags now data-only) | code-only | skip |

## 3.3 End's Delight devmods evaluation (method pass, 2026-10-25)

The [devmods_evaluation.md](devmods_evaluation.md) method run on `ends_delight-1.19.2-2.1` -> `ends_delight-2.6.1+forge.1.20.1`
(`tools/jar_diff.py`, both jars decompiled with Vineflower: `FoodList`, item and material registries, item and block
classes). 8 files added, 11 removed, 37 changed; no item, block or lang name added or removed (the only lang change is
3 tooltip keys renamed for FD 1.3's `TextUtils`). It confirms 3.1 (dragon drops, `nd_` machete modifiers removed, smithed
Dragon Tooth Knife, succulent worldgen, `forge:storage_blocks/chorus_fruit`) and adds the following. Both jars are MIT,
author FoggyHillside. TODO: `largecontent/mod-additions #29`.

| Finding | Class | Decision |
|---|---|---|
| **Tea recipes** (correction to 3.1): 2.1 already ships Cooking Pot recipes for Bubble Tea, Chorus Fruit Milk Tea and Chorus Flower Tea, identical in 2.6.1; 2.6.1 only removes the three Respite brewing recipes. The pot Bubble Tea takes milk + chorus fruit or grain + ender pearl grain, so it does not need a Milk Tea as the Respite one does | data | 3.1 decision stands (Respite keeps the original); "pot costs more" means changing the live pot recipes, with `food/drinks #7` |
| **Knife tiers:** Dragon Egg Shell mining speed 4 -> 6, enchantability 5 -> 14. Dragon Tooth mining speed 4 -> 8, harvest level 2 -> 3, enchantability 5 -> 10, repair item Bone Block -> Dragon Tooth (the Bone Block looks like an upstream slip). Durability and damage unchanged; End Stone and Purpur unchanged. `tuneTools` does not touch these knives today | value change (Java tier) | proposed: carry over the Dragon Tooth repair fix; the speed / level / enchantability values go to the End gear review (`gear #12`) with the smithing recipe |
| **Dragon Breath and Chorus Soup:** 2.1 teleports and clears one milk-curable harmful effect; 2.6.1 only teleports (now a drink), while its tooltip still says it clears a debuff | behaviour | keep 2.1 (2.6.1 looks like a regression) |
| 13 retextures: Shulker Meat and Slice, Roasted Shulker Meat and Slice, Stir-Fried Shulker Meat, Shulker Omelette, Raw Ender Sausage, Ender Sausage, Dragon Leg, Smoked Dragon Leg, Chorus Fruit Wine, and the Dragon Meat Stew / Steamed Dragon Egg feast detail. Redraws of the same items (sausages and the omelette reshaped, the rest touch-ups) | art | owner's call; MIT, so a resource-pack override with the notice is possible |
| Food values (all 46 definitions), stack sizes, bowl / bottle remainders, sneak-to-teleport on the cookie, rice cake and pies, Enderman Gristle teleport, config options | none | unchanged (the gristle items gain FD's effect tooltip only) |
| End Stove rebuilt on FD 1.3's `AbstractStoveBlockEntity` (same 6 slots, campfire recipes); chorus pie model parent `pie` -> `template_pie`; `enderman_gristle_teleport` damage type; package rename; `zh_tw` lang; advancement root | code-only / format | skip (same behaviour, or 1.20 only) |

## 4. Status

| Item | Status |
|---|---|
| Newer-jar review (sections 3.1, 3.2, 3.3) | Written 2026-10-25; decisions recorded, none built; TODO `largecontent/mod-additions #28` (Ender's), `#29` (End's) |
| Overlap survey (section 1) | Written 2026-10-23 |
| Integration | Not started; `largecontent/mod-additions #4`, `#15` |
| Test list | None yet; boot check `SQ-AD-00` in [testing/scratch_queue.md](testing/scratch_queue.md) |
