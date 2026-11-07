# Carnivore: overlap and integration report

> **Archived 2026-10-30: decision, will not be ported.** Carnivore is not being ported and none of its features will be added to the pack. Kept for reference only.


Investigation written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Carnivore part; the Barbeque's Delight
report is its batch sibling). **Nothing is built.** Jar: `devmods/toBackport/carnivore-1.5.0.jar` (Forge 1.20.1).
Licence **All Rights Reserved**, so a port is needed and it takes the donor-jar route (upstream jar unmodified, our own
code written from behaviour). Short name in this file: Carnivore. Test prefix: `CRN-`.

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks for it by name. Upstream bugs are fixed.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 36 food items (+1 tab-icon item), 0 blocks used, 0 effects, 40 working recipes (+4 empty files), 27 drop handlers in code, 41 advancements | 1, 3 | surveyed |
| Upstream bugs B1-B11 | 3.5 | surveyed; B5 (empty recipe JSON) checked in the jar |
| Overlap: 17 of 36 items overlap a pack item or an owner decision (tentacles, guardian, endermite, strider, salmon eggs, bat, silverfish) or need a mob 1.19.2 lacks (sniffer, camel) | 2 | surveyed |
| Fold-in steps F0-F17 | 4 | drafted |
| Decisions D1-D12 | 5 | open |
| Tests CRN-01..CRN-16 | 8 | drafted |

- **Jar:** mod id `carnivore`, version 1.5.0, authors dominicus0830 and bluemoon8377, package `org.domi` (classes
  prefixed `PMF`). `loaderVersion [47,)`, `minecraft [1.20.1,1.21)`. Decompiled to `DecompiledMods/carnivore-1.5.0/`.
- **Licence:** `license="All Rights Reserved"` in `mods.toml`. Route as for Miner's Delight (memory
  `miners-delight-backport-plan`, skill `port` step 2): donor jar kept unmodified in `carnivore_upstream/`, our code
  written from the behaviour described here, never translated from the decompile, no upstream files shipped in our jar.
- **Dependencies:** declares only Forge and Minecraft. Uses Farmer's Delight without declaring it (cutting board and
  cooking pot recipe types, knife ids) and Alex's Mobs entity ids as plain strings (no hard dependency). Inside Creative
  Delight both are present.
- **Pack references:** none (`carnivore` appears nowhere in `kubejs/`, `development/*/src` or the dicts).
- **Dumps:** `kubejs/exported/display_names.json` is from 2026-10-20 and predates the Miner's, Hostile Delight and CD
  member changes. The overlap in section 2 also uses `food_dict.js` / `item_dict.js` and the member sources. `/dumpall` is
  step F0.
- **Already-live effects:** none. The mod is not installed.

## 1. Overview

Carnivore adds raw and cooked meat from mobs that drop no meat in vanilla (horse, turtle, axolotl, bat, parrot, dolphin,
squid, guardian, strider and others), plus meat from 8 Alex's Mobs mobs. Almost everything is a drop. The only dishes are
two cooking pot bowls (Horse Mince, Insect Powder) and one golden craft (Golden Squid Tentacle). Drops come from one Forge
`LivingDropsEvent` handler per mob. There are no loot tables, no loot modifiers and no tags. Fire kills drop the cooked
item instead of the raw one.

### Facts

| | |
|---|---|
| Mod id | `carnivore` |
| Items | 36 food items + `tab_icon_item` (creative tab icon, registered as a real item) |
| Blocks | 0 registered. `PMFConsumableBlock` (a 4-bite block) is in the code but unused |
| Effects / potions | 0 / 0. Foods give vanilla effects |
| Recipes | 44 files: 39 smelting / smoking / campfire (13 inputs x 3, of which **4 files are 0 bytes**, B5), 2 crafting shapeless, 1 FD `cutting`, 2 FD `cooking` |
| Loot | none as data; 27 `LivingDropsEvent` handlers (19 vanilla mobs, 8 Alex's Mobs), section 3.2 |
| Tags | none shipped |
| Advancements | 41 recipe advancements (the pack drops all advancements) |
| Lang | `en_us` only |
| Tooltips | none |
| Config | none |
| Compost | `PMFComposterList` exists with an empty `register()` |
| Mixins | `carnivore.mixins.json` with an empty mixin list |
| Textures | 40 item textures |

## 2. Overlap (first)

### 2.1 Items

| Carnivore item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| `squid_tentacle`, `cooked_squid_tentacle` | Miner's `tentacles`, Ocean's `tentacles`, Oceanic `squid_tentacles`; **the owner removed all tentacles 2026-10-29** (`dishes/ocean #4`, `creativedelight/members #24`); squids drop one Miner's squid item per kill and are cut into calamari (`dishes/ocean #3`) | A fourth tentacle item against a standing decision. Its drop is a `LivingDropsEvent`, so the pack's LootJS squid filter (`kubejs/data/creativedelight/kubejs/miners_delight/loot.js`) would not remove it | Do not register (D3) |
| `glow_squid_tentacle` | Oceanic `glow_squid_tentacles` (removed), Glowing Calamari (CD) | Same as above | Do not register (D3) |
| `golden_squid_tentacle` | none (Water Breathing + Absorption II food) | Its only input is a glow squid tentacle | Do not register with D3, or rebase on Glowing Calamari if the owner asks (D3) |
| `guardian_meat` | Hostile Delight `guardianfillet` (+ cooked), Ocean's `guardian_tail` (+ cooked) | A third guardian meat | Do not register; the guardian already gives two meats (D4) |
| `guardian_eye` | Oceanic `elder_guardian_eye` (elder only) | Close name, different mob | Keep (D4) |
| `mite_chunk`, `cooked_mite_chunk` | Hostile Delight `rawendermite` / `cookendermite`, End's `raw_ender_mite_meat` / `dried_endermite_meat`, Ender's `mite_crust` | Unobtainable upstream (B4); endermite already gives three meats | Do not register (D5) |
| `strider_meat` | Nether's Delight `strider_slice` (Nether's replaces `minecraft:entities/strider`, loot.md 2.6) | Same mob, same role; no cooking recipe upstream | Do not register (D6) |
| `salmon_roe` | Oceanic `salmon_eggs` (renamed Caviar, `dishes/ocean #5`), Miner's `weird_caviar` | Same food under another id; no use upstream | Do not register (D6) |
| `raw_bat`, `cooked_bat` | Miner's `bat_wing`, `smoked_bat_wing` (knife drop from bats) | Two drops from one small mob | Do not register; bat wing is the bat meat (D7) |
| `silverfish` | Miner's `silverfish_eggs`, `arthropod` (silverfish knife drops), Buzzier Bees `silverfish_bottle` | A third silverfish drop | Do not register; Insect Powder takes Miner's `arthropod` (D7) |
| `insect_powder` | Miner's insect line (`insect_stew`, `insect_sandwich`, `insect_wrap`, `seasoned_arthropods`) | Family-level only | Keep, input moved to `arthropod` (D7) |
| `raw_sniffer_meat`, `cooked_sniffer_meat` | none, but **the sniffer does not exist in 1.19.2** | No source | Do not register (D2) |
| `raw_camel_meat`, `cooked_camel_meat` | none, but **the camel does not exist in 1.19.2** | No source | Do not register (D2) |
| `squid_eyeball` | Oceanic `pasta_with_eyeball` (name only) | No source upstream (B4) | Do not register (D8) |
| `frogspawn` (`carnivore:frogspawn`) | **Name collides** with vanilla `minecraft:frogspawn` ("Frogspawn") | Two items named Frogspawn | Keep, rename (F13), D9 |
| `raw_mycelium_beef`, `mycelium_steak` | vanilla beef (it replaces the mooshroom's beef); More Mooblooms cows keep beef | Not a merge: a new cut | Keep |
| `raw_horse_meat`, `horse_meat_steak`, `horse_mince` | FD `horse_feed` (name only) | none | Keep |
| `raw_turtle`, `cooked_turtle` | Hostile `friedturtleegg`, Incubation `turtle_egg_crate` (eggs, not meat) | none | Keep |
| `raw_axolotl`, `cooked_axolotl`, `tadpole`, `cooked_tadpole` | none | none | Keep (D10: babies and bucketed mobs) |
| `raw_bird`, `cooked_bird` | Abnormals' Delight `duck_fillet` (bird meat from another mob) | Family-level only | Keep |
| `raw_whale_meat`, `cooked_whale_meat` | Alex's Mobs `cachalot_whale_tooth` (same mob, not food) | A dolphin is a whale-meat source (B10) | Keep (D11) |
| `cooked_rabbit_foot` | vanilla `rabbit_foot` (potion ingredient) | Cooking a potion ingredient into a 10-hunger food | Keep with pack values (F8) |

Count: 17 items proposed for no registration (tentacles 4, guardian meat 1, mite 2, strider 1, roe 1, bat 2, silverfish 1,
sniffer 2, camel 2, eyeball 1), 19 kept.

### 2.2 Mob drops (by mob, against what the pack already drops)

| Mob | Pack already drops (food) | Carnivore adds | Note |
|---|---|---|---|
| Squid / glow squid | Miner's squid / glow squid item every kill (CD loot.js) | tentacles | D3 |
| Guardian | Hostile guardian fillet, Ocean's guardian tail | guardian eye, guardian meat | D4 |
| Endermite | Hostile, End's, Ender's endermite items | (nothing: B4) | D5 |
| Strider | Nether's strider slice | strider meat | D6 |
| Salmon | Oceanic salmon eggs | salmon roe | D6 |
| Bat | Miner's bat wing | raw bat | D7 |
| Silverfish | Miner's silverfish eggs, arthropod | silverfish | D7 |
| Mooshroom | beef | mycelium beef replaces beef | keep |
| Horse, donkey, mule, turtle, axolotl, tadpole, frog, parrot, dolphin | nothing | new meat | keep |
| AM cachalot whale, orca, giant squid, bald eagle, blue jay, potoo, terrapin, alligator snapping turtle | AM's own non-food drops | whale meat, tentacles, bird, turtle | giant squid tentacles fall under D3 |

### 2.3 Systems

- **Loot:** the pack handles mob food drops with LootJS in the CD tree (`kubejs/data/creativedelight/kubejs/<member>/loot.js`,
  `i.any_knife` for knife kills) and records them in [loot.md](../loot.md). Carnivore's `LivingDropsEvent` runs outside
  loot tables: LootJS cannot see or filter it, the loot census misses it, and it fires on every death. The port
  re-expresses the drops as LootJS entity modifiers (F4).
- **Effects:** vanilla effects only; nothing new for [effects.md](../effects.md) beyond the food tooltip text.
- **Worldgen, crops, seasons, machines:** none.
- **Baby Mobs** (in-tree `babyMobs`): adds babies for squid, glow squid and dolphin. Upstream has no baby check, so baby
  mobs would drop full meat (B3).
- **Create / mob farms:** upstream drops on any death (no player-kill check), so a crusher or lava farm yields meat and
  a lava death yields cooked meat (B3).

### 2.4 Overlap with Barbeque's Delight (same batch)

No shared items. Barbeque's skewers cover beef, chicken, cod, lamb, mushroom, pork sausage, potato, rabbit, salmon and
vegetables; Carnivore's meats have no skewer upstream, and the scope rule adds none. If the Barbeque's Delight report
makes a pack rule that campfire recipes also run on its grill, Carnivore's campfire recipes follow that rule (F6).

## 3. New content

### 3.1 Items (all stack 64, upstream values)

Upstream saturation values are about 10 times the vanilla scale (B7); the pack retunes them anyway (F8).

| Item | Hunger / sat. mod. | Effect on eating | Source |
|---|---|---|---|
| Raw Mycelium Beef / Mycelium Steak | 4 / 1.2, Weakness 30 s; 8 / 12.8 | | mooshroom 1-3 (replaces beef); cook |
| Raw / Cooked Sniffer Meat | 5 / 2.0; 10 / 15.0 | | sniffer 2-3 (absent in 1.19.2) |
| Tadpole / Cooked Tadpole | 1 / 1.0, Nausea 30 s; 4 / 1.4 | | tadpole 1; cook |
| Squid Tentacle / Cooked | 2 / 1.0; 4 / 5.1 | | squid 1-3, AM giant squid 1-3 (knife only, B1); smoker only (B5) |
| Glow Squid Tentacle | 2 / 1.0 | Glowing 15 s | glow squid 1-3, every kill |
| Golden Squid Tentacle | 4 / 9.0 | Water Breathing 2 min, Absorption II 2 min | craft: glow squid tentacle + 8 gold ingots |
| Cooked Rabbit Foot | 10 / 0.6 | | cook vanilla rabbit foot (not in the smoker, B5) |
| Raw Bat / Cooked Bat | 1 / 1.0, Weakness 30 s; 4 / 3.5 | | bat 1 |
| Raw Bird / Cooked Bird | 1 / 0.8, Hunger 30 s (80 %); 5 / 6.8 | | parrot 1; AM bald eagle, blue jay 1-2 (knife only, B1); AM potoo 1-2 |
| Raw / Cooked Turtle | 3 / 1.8; 7 / 7.5 | | turtle 1-2; AM terrapin, alligator snapping turtle 1-2 (knife only, B1) |
| Strider Meat | 2 / 1.0 | | strider 1-3; no recipe |
| Salmon Roe | 1 / 2.0 | | salmon 1-3 (knife only, B1); no recipe |
| Frogspawn | 0 / 0.3 | Nausea 60 s | frog 1-2 (knife only, B1); craft from 2 vanilla frogspawn |
| Squid Eyeball | 2 / 3.0 | Nausea 30 s | **none** (B4) |
| Raw / Cooked Camel Meat | 3 / 1.9; 8 / 8.0 | | camel 1-3 (absent in 1.19.2) |
| Raw / Cooked Axolotl | 2 / 1.0; 6 / 6.0 | | axolotl 1-2 |
| Raw Horse Meat / Horse Meat Steak | 3 / 2.0; 9 / 8.5 | | horse, donkey, mule 2-4 |
| Horse Mince (bowl) | 16 / 14.3 | Jump Boost III 6 min (50 %) | cooking pot: 3 raw horse meat + bowl |
| Raw / Cooked Whale Meat | 4 / 0.5; 9 / 6.0 | | dolphin 1-2; AM cachalot whale, orca 3-5 |
| Silverfish | 2 / 0.5 | | silverfish 1-2 |
| Insect Powder (bowl) | 5 / 3.0 | Weakness III 3 min, Speed IV 2 min | cooking pot: 2 silverfish + sugar + bowl |
| Mite Chunk / Cooked Mite Chunk | 1 / 0.0, teleports like chorus fruit; 4 / 0.3 | | **none** (B4) |
| Guardian Eye | 3 / 2.4 | Mining Fatigue III 5 min | guardian 1-3 |
| Guardian Meat | 4 / 0.3 | | guardian 1-2 at 50 %; no recipe |

### 3.2 Drop logic (code, `org.domi.entityDrops`)

Each handler matches the entity's registry id, rolls `min + rand(0..add)` items, and adds them to the drop list.
"Fire" means `entity.isOnFire()` or a fire damage type; the cooked item drops instead of the raw one.

| Mob (id) | Drop, amount, chance | Fire variant | Knife-gated |
|---|---|---|---|
| `minecraft:mooshroom` | raw mycelium beef 1-3 and removes vanilla beef | mycelium steak | no |
| `minecraft:sniffer` | raw sniffer meat 2-3 | cooked | no |
| `minecraft:camel` | raw camel meat 1-3 | cooked | no |
| `minecraft:horse`, `donkey`, `mule` | raw horse meat 2-4 | horse meat steak | no |
| `minecraft:turtle` | raw turtle 1-2 | cooked | no |
| `minecraft:axolotl` | raw axolotl 1-2 | cooked | no |
| `minecraft:tadpole` | tadpole 1 | cooked | no |
| `minecraft:bat` | raw bat 1 | cooked | no |
| `minecraft:parrot` | raw bird 1 | cooked | no |
| `minecraft:dolphin` | raw whale meat 1-2 | cooked | no |
| `minecraft:squid` | squid tentacle 1-3 | cooked | **yes** (B1) |
| `minecraft:glow_squid` | glow squid tentacle 1-3 | none | no |
| `minecraft:guardian` | guardian eye 1-3; guardian meat 1-2 at 50 % | none | no |
| `minecraft:strider` | strider meat 1-3 | none | no |
| `minecraft:silverfish` | silverfish 1-2 | none | no |
| `minecraft:salmon` | salmon roe 1-3 | none | **yes** (B1) |
| `minecraft:frog` | frogspawn 1-2 | none | **yes** (B1) |
| `alexsmobs:cachalot_whale`, `orca` | raw whale meat 3-5 | cooked | no |
| `alexsmobs:potoo` | raw bird 1-2 | cooked | no |
| `alexsmobs:bald_eagle`, `blue_jay` | raw bird 1-2 | cooked | **yes** (B1) |
| `alexsmobs:terrapin`, `alligator_snapping_turtle` | raw turtle 1-2 | cooked | **yes** (B1) |
| `alexsmobs:giant_squid` | squid tentacle 1-3 | cooked | **yes** (B1) |

The elder guardian, endermite and rabbit have no handler. "Whale" is the dolphin plus Alex's Mobs' cachalot whale and
orca. "Bird" is the parrot plus Alex's Mobs' bald eagle, blue jay and potoo. "Mite" was meant to be the endermite but
has no handler.

### 3.3 Recipes

- Smelting, smoking and campfire for 13 raw items (sniffer, bat, bird, camel, rabbit foot, squid tentacle, tadpole,
  turtle, mycelium beef, axolotl, horse, whale, mite chunk). Cook times 200 / 100 / 600 ticks, 0.35 XP.
- Crafting: Golden Squid Tentacle (glow tentacle + 8 gold); `carnivore:frogspawn` from 2 `minecraft:frogspawn`.
- FD cutting board: `carnivore:frogspawn` + knife -> 2 `minecraft:frogspawn`. In 1.19.2, `minecraft:frogspawn` cannot
  be picked up in survival, so this recipe is a survival source of placeable frogspawn (D9).
- FD cooking pot: Horse Mince (3 raw horse meat, bowl, 200 ticks), Insect Powder (2 silverfish + sugar, bowl, 100 ticks).

### 3.4 Mechanics

- Mite Chunk teleports the eater like chorus fruit (32 tries in a 20 x 21 x 20 box, Enderman sound).
- Fire kill drops cooked meat; mooshroom beef is swapped for mycelium beef.

### 3.5 Upstream bugs

| # | Bug | Evidence | Fix in the port |
|---|---|---|---|
| B1 | Handlers that check `isKnifeKill \|\| "<registry id>".equals(entity.getType().getDescriptionId())` compare a registry id with a description id (`entity.minecraft.squid`), which is never equal. 8 mobs (squid, salmon, frog, AM giant squid, bald eagle, blue jay, terrapin, alligator snapping turtle) drop **only on knife kills**, which looks unintended | `entityDrops/minecraft/Squid.java`, `Frog.java`, `Salmon.java`, 5 `alexmobs/*` | Choose the gate per mob on purpose (D10) |
| B2 | Knife detection compares description ids against the 5 FD knives only; every other knife in the pack (Delightful, Nether's machete and so on) does not count | `KnifeKillDetector.java` | `#forge:tools/knives` (`i.any_knife`) |
| B3 | No baby, player-kill or Looting check: babies (vanilla and Baby Mobs) drop full meat, mob farms and lava kill for meat (lava gives cooked) | `EntityDropUtil.java` | Adult only; player kill per D10; Looting per pack loot rules |
| B4 | Unobtainable items: `mite_chunk`, `cooked_mite_chunk` (no endermite handler), `squid_eyeball` (no source) | grep of `PMFItemList` users | Not registered (D5, D8) |
| B5 | 4 recipe files are 0 bytes: `cooked_camel_meat_from_smoking`, `cooked_rabbit_foot_from_smoking`, `cooked_squid_tentacle_from_campfire`, `cooked_squid_tentacle_from_smelting`. Each would fail to load (not run: the mod was never installed) | `unzip -l` sizes 0 | Rewritten through `global.NCH` with the full cooking set |
| B6 | Mooshroom handler removes all `minecraft:beef` from the drops, but on a fire kill vanilla drops cooked beef, which stays, plus the mycelium steak | `Mycelium.java` | Replace beef and cooked beef alike |
| B7 | Saturation modifiers on the wrong scale (sniffer 15.0, mycelium 12.8, horse mince 14.3; vanilla steak 0.8) | `PMFItemList` | Pack values (F8) |
| B8 | `tab_icon_item` registered as a real item | `PMFItemList` | Not registered |
| B9 | Strider meat, guardian meat and salmon roe have no recipe or use | recipes | Moot if D4/D6 drop them |
| B10 | A dolphin drops whale meat | `Dolphin.java` | Owner call (D11) |
| B11 | 1.20 APIs: `DamageTypeTags.IS_FIRE` (`m_269533_`), 1.20 creative-tab event | decompile | 1.19.2: `DamageSource.isFire()`; LootJS does the fire check in the port |

## 4. Fold-in, in build order

Route (D1, recommended): no Java. The items are plain foods plus one on-eat teleport, and all drops can be expressed
as loot, so the port is the feature_porting tier "KubeJS / data" inside the Creative Delight tree (memory
`creative-delight-routing`): items under the `carnivore` namespace in `development/creativeDelight/src/main/resources/kubejs_startup/carnivore/`,
server scripts in `kubejs/data/creativedelight/kubejs/carnivore/`. Textures and lang come from the donor jar in
`carnivore_upstream/` through patcherMod's donor reader (as Farmer's Structures, memory `farmers-structures-donor`).
If that reader cannot serve item textures, the fallback is a CD Java member with the Miner's donor setup (D1).

| # | Step | Pack system and doc | Files |
|---|---|---|---|
| F0 | `/dumpall`, then `/cdevgen` after F1 (dumps predate Miner's / Hostile) | memory `dumpall-refresh-workflow` | `kubejs/exported/` |
| F1 | Register the kept items (19 under the recommended answers to D2-D8) under `carnivore:`; mite teleport only if D5 keeps it | CD startup scripts | `kubejs_startup/carnivore/carnivore_items.js` |
| F2 | Ids in `item_dict.js` / `food_dict.js` under meat family names (`f` names, creative_delight.md 13: exotic mob meats keep their family name) | memory `item-dict-mechanics` | `_lib/item_dict.js`, `_lib/food_dict.js` |
| F3 | Tags via `global.tags`: `forge:raw_meats` / `food_type/raw/meat` / `food_type/cooked/meat` (not `forge:raw_meat`, memory `forge-raw-meat-tag-gotcha`), bird and fish tags where they apply | [tag_helper.js](../../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js) | `kubejs_startup/carnivore/` |
| F4 | Mob drops as LootJS entity modifiers, adult only, fire -> cooked, gate per D10 with `i.any_knife`; mooshroom swaps beef and cooked beef (B6); add the rows to the loot census | [loot.md](../loot.md), CD `miners_delight/loot.js` as the model | `kubejs/data/creativedelight/kubejs/carnivore/loot.js` |
| F5 | Item merges: none needed if D3-D7 hold (the duplicates are never registered). If the owner keeps any, add them to [item_merges.md](../item_merges.md) and `_core/item_merges.js` | item_merges.md | |
| F6 | Recipes through `global.NCH`: full cook set for every kept raw item (fixes B5); cooking pot Horse Mince and Insect Powder (input `arthropod` per D7) with S&D / Central Kitchen mirrors unless `_manual_only`; cutting board frogspawn; bowl remainders | [recipes_cooking.md](../recipes_cooking.md) | `kubejs/data/creativedelight/kubejs/carnivore/recipes.js` |
| F7 | Potions: none (no potions, no brewing) | | |
| F8 | Food values: `food_types.json` and the family rules (meat/raw, meat/cooked); retune B7 values; Spice of Life entries | [food.md](../food.md), [food_families.md](../food_families.md) | `kubejs/generation_scripts/food/food_types.json` |
| F9 | Food families: `python3 tools/food_families.py --check` | food_families.md | |
| F10 | Effects: no new effects; food effect tooltips only. Check Speed IV / Weakness III (Insect Powder) and Jump Boost III 6 min (Horse Mince) against the family rules | [effects.md](../effects.md) | |
| F11 | Stack sizes per policy (bowl foods) | [stack_sizes.md](../stack_sizes.md) | |
| F12 | Cans: raw and cooked meats under the per-type can rules if the parity rules include exotic meats | [cans.md](../cans.md) | generator |
| F13 | Names: rename `carnivore:frogspawn` (collides with vanilla Frogspawn); "Raw Bird" / "Cooked Bird" fine; check against the names audit | [names.md](../names.md) | CD lang overlay |
| F14 | Compost values for raw meats per the compost framework (upstream list empty) | [composting.md](../composting.md) | CD `compost.js` |
| F15 | Item audit: Item Physic, WTHIT, JEI coverage (new drops show on the mobs' loot pages) | [jei_coverage.md](../jei_coverage.md), [item_physics.md](../item_physics.md) | |
| F16 | Placeables / Display Delight: none upstream; none added | [display_delight.md](../display_delight.md) | |
| F17 | Release: `mod_sides.md` note (no new jar if F1 is KubeJS; donor folder is server and client), changelog bullets under a new `## Carnivore` family, TODO row, plan row | [changelog.md](../changelog.md) 3.6 | `unreleased.md` |

Crops, worldgen, structures, trades, chests, advancements: none (the mod has none; its 41 advancements are dropped).

Ordering: F0 before F2-F3 and F15. F1-F2 before F3-F6. D1 before F1. D2-D8 before F1 (they decide which items exist).
D10 before F4. F8 before F12.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Delivery: KubeJS items in the CD tree with donor textures, or a CD Java member like Miner's? | KubeJS in the CD tree (nothing needs Java) | KubeJS |
| D2 | Sniffer and camel meat (mobs absent in 1.19.2) | Do not register; no other source invented | Not registered |
| D3 | Squid, glow squid and golden tentacles, against the 2026-10-29 "no tentacles" decision | Do not register any; Golden Squid Tentacle only if the owner names a calamari version | Not registered |
| D4 | Guardian: eye and meat, with Hostile fillet and Ocean's tail already dropping | Keep Guardian Eye, drop Guardian Meat | As recommended |
| D5 | Mite chunk (unobtainable; endermite already gives three items) | Do not register | Not registered |
| D6 | Strider meat (Nether's strider slice) and salmon roe (Oceanic caviar) | Do not register | Not registered |
| D7 | Bat (Miner's bat wing) and silverfish (Miner's arthropod) | Do not register; Insect Powder takes `arthropod` | As recommended |
| D8 | Squid eyeball (no source) | Do not register | Not registered |
| D9 | Frogspawn: keep the item and the cutting recipe (a survival source of placeable frogspawn; Roll Delight's shears drop is another, [roll_delight.md](../roll_delight.md) D9)? New name? | Keep; name it "Frog Roe" | Keep, rename |
| D10 | Drop gate: every kill, knife only, or player kill only? Upstream is mixed by accident (B1) | Every adult kill by a player for the large mobs; knife only for the small ones (frog, salmon, birds, turtles), matching Miner's knife drops | As recommended |
| D11 | Dolphin as a whale meat source | Keep (upstream) | Keep |
| D12 | Food values: the upstream numbers or the pack family rules | Family rules (B7) | Family rules |

## 6. File map (planned)

| Path | Content |
|---|---|
| `carnivore_upstream/carnivore-1.5.0.jar` | donor jar, unmodified (textures, lang) |
| `development/creativeDelight/src/main/resources/kubejs_startup/carnivore/carnivore_items.js` | item registration, values, mite teleport (if kept) |
| `kubejs/data/creativedelight/kubejs/carnivore/loot.js` | LootJS mob drops |
| `kubejs/data/creativedelight/kubejs/carnivore/recipes.js` | NCH recipes |
| `kubejs/startup_scripts/_lib/item_dict.js`, `food_dict.js` | ids |
| `kubejs/generation_scripts/food/food_types.json` | food values |
| CD lang overlay | renames (F13) |
| `feature_writeup/carnivore.md` | this file |

## 7. Offline checks (once built)

- `grep -c carnivore: kubejs/startup_scripts/_lib/item_dict.js` equals the kept item count; no literal `carnivore:` ids in scripts.
- After `/dumpall`: every kept item in `display_names.json` with a name, a texture (no missing-texture in `/dumpfood`),
  and a `food_type` tag.
- `python3 tools/food_families.py --check` clean.
- `logs/latest.log`: no `Failed to parse` / `carnivore` recipe errors; no dropped item ids (D2-D8 items never referenced).
- Loot census lists the new entity modifiers.

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| CRN-01 | Creative search `@carnivore` | Only the kept items; correct textures and names; no tab icon item |
| CRN-02 | Kill an adult horse, donkey and mule with a sword | 2-4 Raw Horse Meat each |
| CRN-03 | Kill a horse with Fire Aspect | Horse Meat Steak, no raw meat |
| CRN-04 | Kill a baby horse, baby turtle and baby squid (Baby Mobs) | No Carnivore meat |
| CRN-05 | Kill a mooshroom, normal and with Fire Aspect | Raw Mycelium Beef / Mycelium Steak; no beef and no cooked beef |
| CRN-06 | Kill a guardian | Guardian Eye 1-3; no Guardian Meat; Hostile and Ocean's guardian drops unchanged |
| CRN-07 | Kill a squid and a glow squid, with and without a knife | Only the Miner's squid item (squid chain unchanged); no tentacles |
| CRN-08 | Kill a frog and a salmon with a non-FD knife (for example Delightful steel) and with a sword | Drops per D10; any knife counts |
| CRN-09 | Kill a dolphin, an AM orca and an AM cachalot whale | Whale meat 1-2 / 3-5 / 3-5 |
| CRN-10 | Kill a parrot, an AM potoo and an AM blue jay | Raw Bird per D10 |
| CRN-11 | Cook every kept raw item in a furnace, smoker and campfire | Each gives its cooked item in all three |
| CRN-12 | Cooking pot: Horse Mince, Insect Powder (with arthropod) | Bowl foods made, bowl returned on eating |
| CRN-13 | Cut Frog Roe on a cutting board, then place the result | 2 placeable Frogspawn that hatch tadpoles |
| CRN-14 | Eat Horse Mince, Insect Powder, Guardian Eye | Effects and values per F8 / F10 |
| CRN-15 | JEI: uses of each mob's drops; recipe tree of Horse Mince | Recipes shown; the drops show on the JEI loot page if the pack lists entity loot |
| CRN-16 | Kill a horse in lava (no player) | Per D10: nothing if player kill is required |

## 9. History

- 2026-10-30: investigation written from the decompiled jar (`org.domi`, 42 classes) and its data, the pack dicts,
  member sources and writeups (ocean_delight, miners_delight, ends_delight, hostile_delight, creativedelight). Nothing built.
