# Mineral Delight: overlap and integration survey

> **Archived 2026-10-30: decision, will not be ported.** Mineral Delight is not being ported and none of its features will be added to the pack. Kept for reference only.


Survey written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Mineral Delight part; the other four were reported
the same day: [roll_delight.md](../roll_delight.md), [large_meals.md](../large_meals.md), Barbeque's Delight, Carnivore). Nothing is built. No test prefix yet (proposed `MIN-`).

## 0. At a glance

| | |
|---|---|
| Source | `devmods/toBackport/mineraldelight-2.9.jar` (file name 2.9, `mods.toml` version 1.0.0) |
| Loader | Forge 1.20.1 only (`loaderVersion [47,)`, `minecraft [1.20.1]`); needs Farmer's Delight at runtime (FD `milk_bottle`, `onion`, `pumpkin_slice`, cooking pot and cutting board recipes) but does not declare it |
| Mod id | `mineraldelight`; MCreator generated (`net.mcreator.mineraldelight`) |
| Licence | **Academic Free License 3.0**: permissive, derivative works and redistribution allowed with attribution and the licence notice. Not the ARR donor-jar route that Miner's and Vinery needed |
| Content | 54 items (no blocks), 4 mob effects, 52 recipes, 1 brewing recipe (Java), 3 item tags, 1 advancement, `en_us` + `ko_kr` |
| Theme | Joke food made from minerals: gems, ores, metals, stone, sand and glass eaten directly or cooked into sweets |

Short answer:

- **Direct overlap with Miner's Delight: small.** One shared recipe pattern (Copper Carrot vs Amethyst Apple), one name
  pun (Improvised Barbecue Stick vs Orebecue Stick), and the "cave food" theme. No shared ids, no shared systems.
- **Overlap with the rest of the pack: mostly at the family level, not item level.** Its mineral powders duplicate the
  pack's dusts, its XP effect duplicates Vinery's Experience, and one name collides outright (Vintage Delight
  Century Egg). Most of its 45 foods would sort into existing families (cookie, jello, frozen, candy, pudding, drink, salad).
- **Genuinely new:** edible minerals as a food category, four effect mechanics, and XP-giving food. About 30 foods
  have no counterpart.
- **Upstream defects worth knowing before a port:** 6 items unobtainable in survival, two XP/sugar exploits, a
  `minecraft:` namespace tag that pulls netherite into a snack recipe, an advancement, and 1.20-only damage-type code.

## 1. Inventory

### 1.1 Intermediates (7, not food)

| Item | Made from | Used in |
|---|---|---|
| `amethyst_powder` | cutting board: amethyst shard -> 3 | carrot, crycicle, mochi, emerald essence, sparkling sand, century egg |
| `crushed_diamond` | cutting board: diamond -> 3 | cookie, biscuit, jello, ice cream, milk, crycicle, sparkling sand |
| `lapis_chunk` | cutting board: lapis -> 3 | mochi, bicolor bowl, century egg (lapis) |
| `golden_powder` | cutting board: raw gold -> 2 | golden butter, golden dough, shiny pudding |
| `crushed_emerald` | **no recipe** | only in the `anyore` tag |
| `unrefined_glass` | blasting quartz | smelts to quartz caramel; quartz bake |
| `alloy_chip` | 3 x `#minecraft:anymetal` -> 2 (it is also a food, 4 / 0.5, Resistance) | |

### 1.2 Foods (45) by group

Values are nutrition / saturation modifier as shipped (stack 16 for bowl and drink items, 64 otherwise).
Amplifier III = level 3.

| Group | Items |
|---|---|
| Diamond | `diamond_cookie` 3/0.2, `diamond_biscuit` 3/0.3, `diamond_jello` 7/0.8 (bowl), `diamond_ice_cream` 4/0.7 (bowl), `diamond_milk` 5/0.2 (drink, cooking pot), `diamond_crycicle` 5/0.4 (Noble 60 s) |
| Amethyst | `amethyst_apple` 5/0.4 (8 shards round an apple; Brilliance 40 s), `amethyst_carrot` 7/0.2 (Brilliance 50 s), `amethyst_crycicle` 3/0.3, `geode_carrot` 6/0.4 (basalt + calcite + bud/cluster/shard; Resistance + Brilliance 30 s), `century_egg_amethyst` "Century Egg" 6/0.5 (pot; Brilliance 120 s), `sparkling_sand` 3/0.4 (bowl; Brilliance 80 s, Noble 30 s), `shining_broth` 7/0.4 (pot) |
| Lapis | `lapis_mochi` 6/0.5 (Knowledge 45 s, +15 XP), `lapis_mochi_ex` 6/0.5 (mochi + XP bottle; Knowledge 60 s, +50 XP), `bicolor_bowl` 6/0.4 (lapis + redstone + flowers; drops a 20 XP orb), `century_egg_lapis` (pot) |
| Quartz and sugar | `quartz_caramel` 3/0.2 (smelted unrefined glass; Knowledge 90 s), `quartz_saccharin` 0/0.5 (pot, bowl), `saccharin_syrup` 1/0.4 (pot, drink; spawns about 100 XP in orbs), `ivory_amber_sugar` 1/0.3 (x8), `ivory_milk_tea` 3/0.5 (pot; Knowledge 5 min), `quartz_bake` 6/0.6 (Knowledge + Strength 80 s), `mossy_shortcake` 5/0.3 (cobblestone, diorite, quartz bake) |
| Gold | `golden_dough` 3/0.1, `golden_butter` 2/0.3 (drink; Absorption IV 30 s), `dazzling_candy` 3/0.5 (x8; Absorption III 30 s), `shiny_pudding` 4/1.1 (pot; Absorption IV 120 s), `golden_crepe` 2/0.8 (**no recipe**; Brilliance 30 s), `molten_stew` 6/0.7 (pot; Absorption III + Fire Resistance 5 min, sets you on fire 7 s), `lava_bucket_mimic` 6/1.1 (drink; Fire Resistance 7.5 min), `piglin_salad` 7/0.5 (gilded blackstone, basalt; Absorption + Resistance) |
| Copper and metal | `copper_candy` 2/0.5 (x6 from 2 raw copper), `copper_caramel` 3/0.3 (x8 from 8 ingots), `hot_cocopper` 2/0.4 (pot, drink; Speed IV 7.5 s), `alloy_chip` 4/0.5 (Resistance 7.5 s), `orebecue_stick` 7/0.7 (2 x `#anyore` on a stick) |
| Stone, sand, glass | `pebble_in_a_bowl` 5/0.2 (4 cobblestone; deals 3 damage, own death message), `glass_salad` 5/0.1 (3 glass items; deals 4 damage, Speed III 30 s), `sandstone_cookie` / `red_sandstone_cookie` 1/0.2, `sandwich` 7/0.5 (sand + cactus; Speed IV 3 s), `sandscoop` / `red_sandscoop` 4 (**no recipe**; Slowness 30 s), `sandy_lunch` 6/0.7 (needs sandscoop, so unobtainable) |
| Other | `emerald_essence` 0/0.4 (3 emeralds + XP bottle; Hero of the Village 30 s), `garnet` (brewing: potion + amethyst carrot; Signalized + Regeneration 120 s), `hungrymaker` (epic, stack 1, 0 food, always edible, **no recipe**) |

### 1.3 Effects (4, all beneficial)

| Effect | Behaviour |
|---|---|
| Brilliance | +3 XP on every critical hit (CriticalHitEvent) |
| Knowledge | +3 XP every time a shield blocks (ShieldBlockEvent) |
| Noble | Each tick converts a harmful effect into a matching good one: Hunger -> Saturation, Poison -> Regeneration 3 s, Mining Fatigue -> Haste 30 s, Blindness -> Night Vision, and more of the same shape |
| Signalized | When hit below 10 HP: sets health back up, thunder sound and spark particles, then removes itself (a one-shot "last stand") |

### 1.4 Tags and other data

- `minecraft:anymetal` (copper/iron/gold/netherite ingots and raws, **netherite ingot included**),
  `minecraft:anyore` (diamond, emerald, quartz, amethyst, gold, copper, iron and the crushed forms),
  `forge:glass_saladingredients` (glass bottle, glass, pane, tinted glass, unrefined glass).
- Advancement `extra_orb` (eat Lapis Mochi Ex).
- No loot tables, no worldgen, no villager trades, no blocks, no placeable food.

## 2. Overlaps

### 2.1 With Miner's Delight (backportedMiners, 69 items)

| Mineral Delight | Miner's Delight | Kind |
|---|---|---|
| `amethyst_apple` (8 amethyst round an apple), `amethyst_carrot` | `copper_carrot` (8 copper ingots round a cave carrot; brews Haste) | Same recipe pattern and idea (metal-coated vegetable, golden carrot style). Pick one family rule for "coated with mineral X" |
| `orebecue_stick` | `improvised_barbecue_stick` | Name pun only; different food (ores vs bat wing and mushroom) |
| `hot_cocopper`, copper candy / caramel | copper cup and copper pot | Theme only (copper). Hot Cocopper could be served in a copper cup if ported, but nothing requires it |
| `mossy_shortcake` | `moss` (food) | Theme only |
| Cave and mining theme overall | cave carrots, cave soup, bat / spider food | Complementary. Both are "food for underground"; Miner's is creatures and crops, Mineral is the rock itself |

Miner's Delight's haste potion and Mineral's Noble (Mining Fatigue -> Haste) are different routes to the same buff; no conflict.

### 2.2 With the other food and cooking mods in the pack

| Mineral Delight | Pack counterpart | Kind |
|---|---|---|
| `century_egg_amethyst` displays as **"Century Egg"** | Vintage Delight `vintagedelight:century_egg` "Century Egg" (fermenting jar, 11 / 0.8, big buff stack) | **Name collision.** Two different items, same name. Must be renamed ("Amethyst Century Egg") or dropped |
| `amethyst_powder` | `creativeengineering:amethyst_dust` (Create / IE crushing, used by echo tools and budding amethyst) | **Item duplicate.** Fold into the pack dust |
| `crushed_diamond` | `createaddition:diamond_grit` (`forge:dusts/diamond`; IE blueprint diamond -> 2) | **Item duplicate.** Fold into diamond grit; note the yield clash (knife gives 3, IE gives 2) |
| `golden_powder` | `immersiveengineering:dust_gold` (`forge:dusts/gold`) and Create crushed raw gold | **Item duplicate.** Fold into `forge:dusts/gold` |
| `lapis_chunk` | Create `milling/lapis_lazuli` (gives lapis back, no dust item) | New item; the pack has no `forge:dusts/lapis` |
| `crushed_emerald` | none (`forge:dusts/emerald` empty) | New, but unobtainable upstream |
| Knowledge, Brilliance (XP gain) | `vinery:experience_effect` "Experience" (Backported Vinery wines) | **Effect role overlap.** Different triggers (shield block, crit) vs Vinery's own; keep, but describe them as one XP family in JEED and the effects writeup |
| XP from food (`lapis_mochi`, `saccharin_syrup`, `bicolor_bowl`) | `quarkdelight:cavern_explorer_stew` is the only other XP food | Near new; Quark Delight is the one precedent |
| `diamond_jello` | Fruits Delight jello family (one jello per fruit, cauldron made) | Family fit; could join the cauldron jello route instead of a crafting recipe |
| `diamond_ice_cream`, `diamond_crycicle`, `amethyst_crycicle` | Neapolitan ice creams, FD / Fruits Delight / More Delight popsicles, Fungi truffle ice cream | Family fit (`frozen`) |
| `diamond_cookie`, `sandstone_cookie`s, `diamond_biscuit` | FD, Abnormals', Fruits Delight, Respite cookies; Golden Food golden cookies | Family fit (`cookie`); canned-cookie rule applies |
| `copper_candy`, `dazzling_candy`, `ivory_amber_sugar`, `quartz_caramel`, `copper_caramel` | Supplementaries candy, Create Confectionery candies and caramel bar, Delightful (CD) Rock Candy (rose quartz, cooking pot) | Family fit (`candy/piece`). Rock Candy is the closest: also a gem candy |
| `shiny_pudding` | Neapolitan vanilla pudding, Fruits Delight fig pudding | Family fit (`dessert/pudding`) |
| `golden_butter`, `golden_dough`, `golden_crepe`, `shiny_pudding`, `dazzling_candy` | Golden Food mod, golden carrot / apple, Vintage golden egg, goose golden egg | `golden` overlay family. Absorption values sit above the golden carrot and golden cookie (Absorption I 120 s) |
| `hot_cocopper` | FD hot cocoa, Respite strong hot cocoa, Confectionery hot chocolate | Name pun; contains no cocoa |
| `ivory_milk_tea` | Respite teas, CD azalea / lavender tea | Family fit (`drink/tea`); uses FD `milk_bottle` (in the CD farmers member) |
| `saccharin_syrup` | Rustic Delight syrup | Same word, different item |
| `glass_salad`, `piglin_salad`, `sparkling_sand`, `pebble_in_a_bowl` | FD / Delightful / Quark Delight salads; Nether salad | Family fit (`salad`); the two damaging ones also fit the `hazard` overlay |
| `sandwich` (sand + cactus, display name "Sandwich") | about 20 real sandwiches (FD, More Delight, Brewin', Veggies, Miner's) | **Name problem**: generic name on a joke item; rename ("Sand Sandwich") for JEI search |
| `molten_stew`, `shining_broth` | FD bone broth, Miner's cave soup, soups and stews | Family fit (`soup`) |
| `orebecue_stick` | Nether's Delight nether skewer, Ender's crispy skewer, Veggies turnip skewer, Barbeque's Delight (candidate, row 12) | Family fit (`skewer`) |
| `geode_carrot` | none | New |

Candidate mods in the same TODO rows: Extra Delight's diamond and emerald items are sugar-cookie and gingerbread
shapes (no minerals), and it has a `pestle_amethyst` tool; Hearth and Harvest has diamond and golden cleavers. Neither
overlaps Mineral Delight's food. Roll Delight's `golden_roll` is the only other mineral-adjacent food in the batch.

### 2.3 With pack systems

| Pack system | Mineral Delight touches it through | Note |
|---|---|---|
| Crushing (Create, IE crusher, `server_scripts/engineering/dust.js`) | knife-on-gem recipes | Upstream crushes on the cutting board only. The pack already crushes gems in machines |
| Brewing / potions (IE potion machines, Create Auto Brewing, potion buckets) | `garnet` brewing recipe in Java | Garnet is a drink item, not a potion; the recipe is hard-coded |
| Effects (`effects-writeup`, JEED texts, `undescribed_effects_queue.json`) | 4 new effects | Each needs a JEED description |
| Advancements | `extra_orb` | Pack drops all advancements at load; harmless but dead data |

## 3. What is new

Content with no pack counterpart, which is the reason to port at all:

1. **Edible minerals** as a category: stone (pebble, mossy shortcake), sand (sandwich, sandscoop, sandstone cookies),
   glass (glass salad), geode (geode carrot), metal (alloy chip, orebecue stick, copper candy). The pack has nothing like this.
2. **The four effects**. Noble (cleanse-into-buff) and Signalized (one-shot last stand) have no counterpart at all.
   Brilliance and Knowledge are XP effects with combat triggers (crit, shield block); Vinery's Experience is the only relative.
3. **XP food**: mochi, saccharin syrup, bicolor bowl, emerald essence (Hero of the Village from food is also new).
4. **A quartz sugar chain**: quartz -> unrefined glass -> quartz caramel -> saccharin -> syrup, with sugar and XP bottles as outputs.
5. **About 30 foods with only a family-level match** (all of 1.2 except the duplicates and collisions in 2.2).

## 4. Upstream problems (fix in any port)

| Problem | Detail | Proposed fix |
|---|---|---|
| Unobtainable items | `golden_crepe`, `sandscoop`, `red_sandscoop`, `crushed_emerald`, `hungrymaker` have no recipe; `sandy_lunch` needs a sandscoop so it is unobtainable too | Add recipes (sandscoop: bowl on sand; golden crepe: golden dough in a skillet; crushed emerald: same as the other gems) or drop. Hungrymaker has no effect at all: drop |
| XP bottle exploit | 4 saccharin syrup -> **12** XP bottles; 16 quartz make 4 syrups, and drinking a syrup also spawns about 100 XP | Drop `expbott`, or cut it to 1 bottle per syrup and drop the orbs on drinking |
| Sugar from quartz | 1 quartz caramel -> 8 sugar | Drop or 1:1 |
| Netherite in a snack | `#minecraft:anymetal` includes netherite ingot and scrap; 3 of any make 2 alloy chips | Replace with an own `forge:` style tag of plain metals; never `minecraft:` namespace |
| `minecraft:` namespace tags | `anymetal`, `anyore` pollute vanilla's namespace | Move to the port's namespace, built through `global.tags` |
| Glass bottle in salad | `forge:glass_saladingredients` accepts glass bottles | Glass blocks and panes only |
| Advancement | `extra_orb` | Drop (pack rule) |
| 1.20 APIs | damage via `Registries.DAMAGE_TYPE` / `DamageTypes` (pebble, glass salad), custom `death.attack.teeth` | 1.19.2 uses `DamageSource` objects: rewrite |
| Recipe types | `cooking` and `cutting` are FD 1.20 JSON | Re-express through `global.NCH` (FD cooking pot, cutting board) |
| No FD dependency | uses FD items and recipe types without declaring FD | Moot inside Creative Delight (FD is a member) |
| Over-strong values | saturation 1.1 on two items; Absorption IV; Fire Resistance 7.5 min from a craft table drink | Food tuning pass (pack values) |

## 5. Integration work (if ported)

Assumed route: a Creative Delight member under the `mineraldelight` namespace (CD routing rule: all food goes in CD
trees), own GPLv3 code plus the AFL 3.0 notice for upstream art and lang. Licence allows shipping upstream textures directly.

| # | Pack system | Work |
|---|---|---|
| I1 | Items (`item_dict`) | Register the 45 foods and the intermediates that survive I2; ids in the dict, no literals |
| I2 | Dusts | Drop `amethyst_powder`, `crushed_diamond`, `golden_powder`; recipes take `creativeengineering:amethyst_dust`, `forge:dusts/diamond`, `forge:dusts/gold`. Keep `lapis_chunk` and `crushed_emerald` as `forge:dusts/lapis` / `forge:dusts/emerald` (rename to "Lapis Dust", "Emerald Dust") and add Create milling + IE crushing for them through NCH, beside the cutting-board route |
| I3 | Recipes (`global.NCH`) | All 52 re-written through NCH; cooking pot recipes mirror to Slice and Dice mixing unless `manual_only` |
| I4 | Tags (`global.tags`) | Replace the three upstream tags; add `food_type`, `forge:foods/*`, family tags |
| I5 | Food families | Place every food (2.2 column 3); the two damaging foods in `hazard`; golden items in `golden` |
| I6 | Food tuning and Spice of Life | Retune values (4); SOL complexity entries |
| I7 | Effects | Port the 4 effects as Java (event hooks); JEED text; effects writeup; decide whether Knowledge and Brilliance stay separate from Vinery Experience |
| I8 | Brewing | Garnet: recipe through the pack's brewing route instead of hard-coded Java |
| I9 | Names | Rename `century_egg_amethyst` and `sandwich`; check `century_egg_lapis` naming matches |
| I10 | Containers | Bowl and bottle remainders (`quartz_saccharin`, salads, jello, ice cream, drinks) through the remainder buffer; stack sizes per the stack-size policy |
| I11 | Cans | Per-type cans for the bowl foods if the can rules cover them |
| I12 | Display Delight / placeable | Optional; no upstream placeables |
| I13 | Compost, AppleSkin, WTHIT, JEI coverage, item physics | Standard new-food passes after `/dumpall` |
| I14 | Fixes from 4 | All rows |
| I15 | Docs | This file, `creative_delight.md` member row, `mod_sides.md` note, changelog bullets per family |

## 6. Open questions (owner)

1. Port at all, and if so all 45 foods or a curated subset (the joke items: pebble, sand, glass)?
2. Dusts: fold into the pack dusts as in I2, or keep Mineral's own powders as food-only ingredients?
3. XP: keep XP food and the two XP effects, given Vinery's Experience already exists?
4. Copper Carrot (Miner's) and Amethyst Apple / Carrot: one shared "mineral-coated" rule, or leave both?
5. Century Egg: rename the amethyst one, or drop it in favour of Vintage's?

## 7. History

- 2026-10-30: survey written from the jar (lang, recipes, `javap` of item, effect and procedure classes) and the pack
  dumps (`display_names.json` of 10-20, `food/catalog.json` of 10-25, item tags, recipe ids) plus the Creative Delight
  member sources for items added after the dumps.
