# Barbeque's Delight: overlap and integration report

Investigation written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Barbeque's Delight part). **Port built
2026-11-01** (section 4.0); in `mods/` since the Creative Delight jar of 11-02 14:01, scratch run r1102b (8), not played yet. Jar: `devmods/toBackport/barbequesdelight-1.3.0.jar` (NeoForge 1.21.1), unpacked and decompiled in
`DecompiledMods/barbequesdelight-1.3.0/` (`jar/`, `src/`). Licence **LGPL-2.1** (`neoforge.mods.toml`), so the fork route
applies: a port is needed (1.21.1 NeoForge -> 1.19.2 Forge) and may reuse upstream code and assets with the notice kept.
Short name in this file: **BBQ**. Test prefix: **BBQ-** (unused in `feature_writeup/`).

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks for it by name. Upstream bugs are fixed.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory | 36 items (33 plus 3 block items), 3 blocks, 0 effects, 0 potions, 26 recipes, 6 butcher trades, 1 mixin | surveyed (1, 3) |
| Upstream bugs | B1-B5 | found (3.6) |
| Overlap | skewer family (8 pack skewers), pack seasonings framework, wrap/sandwich rules, Create basin name | surveyed (2) |
| Fold-in | steps 0-19 | built 2026-11-01 (4.0); in `mods/` (CD jar 11-02 14:01), not played |
| Decisions | O1-O9 (owner, 2026-10-31); D1-D10 | O1-O9 recorded (0.1); D1-D4, D7 answered by them, D5, D6, D8-D10 defaults stand (5) |
| Fold (4.5) | seasoning fold, `grilled`, trades, furnace routes, cooked-ingredient craft, names | BUILT 2026-11-03, installed in the CD jar 2026-11-03 00:08, not booted; SQ-BBQF-01 to -07 queued (SQ-CDJ7) |
| Tests | BBQ-01..BBQ-29; scratch SQ-BBQ-01..08, SQ-BBQF-01..07 | in game: none run. Scratch r1102b (2026-11-02): SQ-BBQ-01, -03, -04, -05 PASS; -02, -06, -07 OPEN (batch faults); -08 not run (needs a player) |

- **Jar:** `barbequesdelight-1.3.0`, mod id `barbequesdelight`, authors lcy0x1 (xkmc), package `com.mao.barbequesdelight`
  (forked from Mao's original). 60 Java classes, about 1,600 lines of logic.
- **Licence:** LGPL-2.1, GPL-compatible: fork into the pack, our changes GPLv3, upstream notice kept.
- **Dependencies:** NeoForge 21.1.61+, MC 1.21.1, Farmer's Delight 1.2.4+ (blocks, sounds, particles, `NOURISHMENT`,
  `HeatableBlockEntity`, cutting board). Jar-in-jar: **Registrate MC1.21 1.3.0, L2Core 3.0.8, L2ModularBlocks 3.0.0,
  L2Serial 3.0.9**. None of these is in the pack, and none has a usable 1.19.2 build at these APIs.
- **Pack references:** none to `barbequesdelight` in `kubejs/`, `item_dict.js` or `development/*/src`. The `barbecue`
  hits in `backportedDisplay` are FD's and End's Delight's sticks. Dumps (2026-10-20) predate nothing here: the jar is
  not installed, so `/dumpall` comes after the port (step 0).
- **Already live:** nothing played. Built 2026-11-01: the Creative Delight member `backportedBarbeques` (4.0), in `mods/` since 11-02 14:01.

## 0.1 Owner decisions (2026-10-31)

Recorded from the owner's notes. They set the scope of the port; where they differ from a recommendation in sections
2 to 5, they win. Built as recorded here on 2026-11-01 (4.0).

| # | Decision | Effect on this report | TODO |
|---|---|---|---|
| O1 | **Add all the skewers** (10 raw, 10 cooked, Burnt Skewer) as upstream, **without folding them into the skewer family yet** | Stations are the basin and the grill only: no family mirrors, no furnace route, no `skewer` family tags, no skewer food type, no saltable-by-family. D1 answered | `dishes/meals #46` (fold), overlap table below it |
| O2 | **Overlapping skewers get a row in an overlap evaluation table** | Five rows: beef, lamb, salmon, cod, pork sausage (2.1). Verdicts provisional (keep both) | table "BBQ skewer overlap evaluation" in TODO |
| O3 | **Add Kebab Wrap, Kebab Sandwich and Bibimbap** as upstream (combine recipes, skewer-effect NBT). **Note: use a tortilla in the Kebab Wrap** | Wrap ships on bread now; the tortilla switch is a later row. Sandwich and bibimbap keep upstream recipes (D3 sandwich/bowl part not applied now) | `dishes/meals #47` |
| O4 | **Add all spices and sauces** (3 powders, 3 sauces) with BBQ's own mechanic, **marked for later folding into the pack seasoning system**; **document how that system works** | D4 answered: keep BBQ's mechanic for now. Pack system described in 3.3.1, with the gaps the fold must close | `food/seasoning #11` |
| O5 | **Keep Ingredients Basin, Grill and Tray as is** | No renames, no recipe changes, no pack-side stations; D6 (built-in campfire) and D10 (tray) defaults stand: ported as upstream | none |
| O6 | **Remove the seasonings from villager trades**; integrate seasoning in trades better and comprehensively later | The 6 butcher trades are not ported (also removes B2). **The six seasonings then have no survival source** (3.5). D2 answered | `food/seasoning #12` |
| O7 | **Update the compost map** | 24 entries as a hand `barbequesdelight/compost.js` (step 13, table 3.7) | in `largecontent/mod-additions #37` |
| O8 | **No display placeables exist** in the mod; **mark them for a model recolor review** | No Food Plate or plate-tag entries now. D7 answered: nothing added | `food/placeable #5` |
| O9 | **Port the Jade tooltip to WTHIT** | Grill progress provider in the port (step 17) | `ui/wthit #13` |
| O10 | **Plain Forge rewrite, as part of Creative Delight** | D8 answered. No library jars. Built as a CD member (step 0), not a standalone mod | `largecontent/mod-additions #37` |
| O11 | **Grill campfire acts exactly as a vanilla campfire, with extinguishing and weather** | D6 answered, B4 fixed by design: 3.4.1 | `#37` |
| O12 | **Port the Tray for evaluation** | D10 answered: ported, marked for a keep / drop review | `largecontent/mod-additions #38` |
| O13 | **Keep the D5 renames** (Lamb to Mutton in lang, "Ingredients Basin" kept) | D5 answered | none |
| O14 | **Table of every seasoning with its effects; the owner adds a temporary source** | Table in 3.8. The six BBQ seasonings are obtainable only by that temporary source | `food/seasoning #12` |
| O15 | **Sandwich and Bibimbap: integrate later, in the integration stage** | Upstream recipes ship; pack-rule rework is its own row | `dishes/meals #48` |
| O16 | **Keep bug B5** (a burnt skewer loses its seasoning; a cold grill erases progress) | By design; tooltip text only | none |
| O17 | **Skewers marked for a later food and saturation investigation** | Upstream values ship; D9 becomes this row | `food/types-and-tuning #19` |
| O18 | **Seasoning fold design** (4.1): the pack system gains the new effect kinds; food is seasoned by shapeless crafting; max seasonings per food is configurable, 2 for now | `food/seasoning #11` rewritten | `#11` |
| O19 | **Skewer fold design** (4.1, revised): BBQ system kept, other mods' skewers added to it; family recipes later; overlaps kept; raw skewers stay (worse, sometimes poisonous) and grill into the cooked version, never cooked in furnaces; cooked skewers craftable shapelessly from cooked ingredients; grilling adds a `grilled` seasoning that does not count toward the cap (configurable) | `dishes/meals #46`, `food/seasoning #13` | `#46`, `#13` |
| O20 | **Seasoning use before and after the fold.** Before: using a BBQ seasoning on a skewer costs 1 durability (upstream). After: each shapeless craft consumes one item, and the crafts and trades that give BBQ seasonings are **scaled to give enough individual items to replicate a similar usable amount** | Q4 answered. Ratio of items per old durable item is a new decision (4.3 #1) | `food/seasoning #11`, `#12` |
| O21 | **`grilled` is added only by grilling on the Grill, not by the shapeless cooked recipe** | Q6 answered: cooked-ingredient crafts give a skewer without `grilled`, so the two cooked skewers differ (4.3 #9) | `food/seasoning #13` |
| O22 | **Note: decide whether raw skewers can be smelted or smoked** into a cooked skewer without the `grilled` effect | Supersedes "never cooked in furnaces" in O19: now an open decision, not a rule | `dishes/meals #49` |
| O23 | **Review skewer names; every cooked skewer says "Cooked", not "Grilled"** (several cooking routes) | Lang only, ids stay `grilled_*`. Name table in 3.9 | `largecontent/mod-additions #37` |
| O24 | **Other mods' skewers get a raw version** | Q7 answered: new raw items for the 8 pack skewers, grilled on the Grill into the existing items | `dishes/meals #50` |
| O25 | **`grilled` gives +2 saturation** | Its own effect; read as +2 saturation points in the pack's unit (4.3 #3) | `food/seasoning #13` |
| O26 | **Only chicken or other bad meats give the "nasty" food type** | Raw skewers are not nasty by default. The term "nasty" is not defined in the pack docs (4.3 #4) | `food/types-and-tuning #19` |
| O27 | **Nasty food type default confirmed**: a hazard-style downside for chicken only; other raw meats are worse food but not nasty | 4.3 #4 answered | `food/types-and-tuning #19` |
| O28 | **Raw skewers take seasonings, not sauces.** Seasonings on a raw skewer **copy over to the cooked skewer** when it is grilled | 4.3 #5 answered. The port refuses the three sauces on raw skewers (a change from upstream, which allows any seasoning on any BBQ skewer) | `largecontent/mod-additions #37`, `food/seasoning #11` |
| O29 | **Cooked-ingredient skewers follow the skewer family rule: shaped plus one sequenced assembly** | Replaces the "shapeless" wording of O19. Not an exception: the same rule as Crispy Skewer ([recipes_cooking.md](recipes_cooking.md) 2.1). 4.3 #8 answered | `dishes/meals #46` |
| O30 | **A grilled and a crafted cooked skewer of the same line are the same item but do not stack**, because their seasonings differ (`grilled` present or absent) | Pack framework behaviour: seasonings are NBT, sorted, and stacks split on NBT. 4.3 #9 answered, no special code | none |
| O31 | **Seasoning conversion (4.3 #1): 64 powders and 16 sauces.** One old durable powder becomes 64 single-use items and one sauce 16. When default crafting recipes for the seasonings are added, one craft gives 64 powders or 16 sauces | 4.3 #1 answered | `food/seasoning #11`, `#17` |
| O32 | **Raw meat and raw skewers follow raw chicken** for the Hunger chance and the AppleSkin shank colour. **Chicken alone has the extra poison chance.** **No new "nasty" food type**: the downside is folded into the vanilla raw-chicken system | Supersedes O26 and O27 and 4.3 #4. Raw skewers carry the chance of their meat | `food/types-and-tuning #19`, [food.md](food.md) 3.17 |
| O33 | **Raw skewers can be smelted, smoked or campfire-cooked** (cooked skewer, no `grilled`, so no +2 saturation); the campfire counts (owner 2026-11-02) | 4.3 #6 answered, O22 settled | `dishes/meals #49` |
| O34 | **Names.** A skewer is named **"Raw TYPE"** when uncooked and plain **"TYPE"** when cooked (no "Cooked" and no "Grilled" in the name). Seasoned items take the prefixes **Chili Flavored, Salted, Peppered, Garlic**. **"Grilled" appears only as a seasoning tooltip line, and only on a skewer that was grilled** | Supersedes O23 and the 3.9 "Cooked" column: the "Grilled X" names become plain "X". 4.3 #7 answered | `dishes/meals #50`, `#46` |
| O35 | **Skewers as ingredients** (wrap, sandwich, bibimbap, other recipes) **do not carry `grilled`**, and are accepted grilled or not | Supersedes the 4.3 #10 default (combos no longer carry `grilled` saturation) | `dishes/meals #46`, `#48` |
| O36 | **The `grilled` tooltip is shown exactly like the salt and other seasoning tooltips**: the same system, same line style and AppleSkin entry | 4.3 #12 answered | `food/seasoning #13` |
| O37 | **4.3 #2, #3, #5, #8, #9, #11, #13, #14 follow their defaults** | Raw pack skewers: CD member under the original namespace, one basin recipe per skewer, stack 64. `grilled` +2 saturation points, capped. Sauces refused on raw skewers. `[food]` config section. Grilling JEI category name kept, raw pack skewers added. Tests BBQ-20 to BBQ-24 to be added | `dishes/meals #50`, `food/seasoning #13` |
| O38 | **Section 5 answers.** D1: basin + grill, basin + furnace or smoker (no `grilled`), and an **expensive** shaped cooked-ingredient craft. D2: **re-add the six butcher seasoning trades** (reverses O6, 2026-11-02 second pass) and **mark an item for a crafting recipe for each seasoning**. D3: recommended (wrap on tortilla, shaped sandwich + one assembly, bibimbap takes and returns a bowl). D4: **map the six seasonings onto the pack seasoning framework**. D5: Mutton, Ingredients Basin kept. D6: the pack campfire rules (3.4.1: rain does not put out a signal fire; coal and charcoal blocks make one; no item fuelling). D7: **no placeables until models exist; pinned as a Display Delight item, not a Barbeque's Delight one**. D8: plain Forge rewrite. D9: upstream values until the skewer type lands. D10: tray marked for evaluation | O6 is reversed: the trades come back (row `food/seasoning #18`), so the source gap closes | `food/seasoning #12`, `#17`, `#18`, `food/placeable #5` |
| O39 | **A basin and a tray in every vanilla wood** (2026-11-04) | Basin from the wood's stripped log texture, tray from Farmer's Delight's crate bottom palette-swapped to the wood's planks. Partly reverses O5 ("no recipe changes"): one stonecutter pair per wood. Oak keeps the 1.3.0 ids. Built, see 3.11 | none |

## 1. Overview

A grilling station for Farmer's Delight. Ingredients go in an **Ingredients Basin**; right-clicking a basin slot with a
stick in the main hand (and a side ingredient in the off hand) makes a raw skewer. Raw skewers go on a **Grill** over a
heat source; each side needs half the grilling time, the player flips with sneak + right click, and a skewer left on too
long becomes a Burnt Skewer. Six seasonings (3 powders, 3 sauces, durability items) are sprinkled on a skewer on the
grill or on a cutting board and change its name and effects. Cooked skewers combine with bread, cabbage, onion or rice
into a Kebab Wrap, Kebab Sandwich or Bibimbap that carry the skewers' effects. A **Tray** is plain 3-slot display storage.

### Facts

| Field | Value |
|---|---|
| Mod id | `barbequesdelight` |
| Items | 36: 10 raw skewers, 10 cooked skewers, burnt skewer, kebab wrap, kebab sandwich, bibimbap, 3 powders, 3 sauces, 3 block items |
| Blocks | 3: `grill` (block entity, ticking), `basin` "Ingredients Basin" (2 slots), `tray` (3 slots, stacks on itself) |
| Effects / potions | 0 / 0 (uses vanilla Strength, Regeneration, Jump Boost, Resistance, Poison, Nausea and FD Nourishment) |
| Recipes (26) | `barbequesdelight:skewering` 10, `barbequesdelight:grilling` 10, `barbequesdelight:combine` 3 (shapeless with NBT), `crafting_shaped` 1 (grill), `stonecutting` 2 (logs -> 2 basins, logs -> 4 trays) |
| Trades | 6, butcher level 1, all seasonings: 12 emeralds each, 4 uses, 4 XP (Java `VillagerTradesEvent`) |
| Loot | 3 block tables (grill also drops its built-in campfire); no mob or chest loot, no GLM |
| Tags | item `raw_skewers`, `grilled_skewers`; block `mineable/pickaxe` (grill), `mineable/axe` (basin, tray); `carryon:block_blacklist` (grill, basin) |
| Data maps | `neoforge:compostables` 24 entries (raw 0.25, grilled 0.5, wrap 0.5, sandwich 0.75, bibimbap 1.0, burnt 0.25) |
| Advancements | 26 recipe advancements; the pack drops all advancements |
| Mixins | 1: `CuttingBoardBlockMixin` (FD cutting board accepts seasonings) |
| Data components | 2: `seasoning` (enum), `content` (list of skewer stacks) |
| Lang | `en_us`, `en_ud`, `uk_ua`, `zh_cn`; 63 `en_us` keys |
| Tooltips | seasoning info line, chance effect lines (gated on FD's food effect tooltip config), sandwich contents |
| Compat | JEI (Skewering and Grilling categories), Jade (grill progress) |
| Config | none of its own (reads FD's `ENABLE_FOOD_EFFECT_TOOLTIP`) |

## 2. Overlap

### 2.1 Items

| Item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| all 10 cooked skewers | pack **skewer** family (8 members: FD Barbecue on a Stick 8/14.4, End's Delight End Barbecue Stick 10/14, Miner's Improvised Barbecue Stick 7/9.8, Nether's Delight Nether Skewer 7/7, Oceanic Seafood Skewer 11/15.4, Veggies Turnip Mutton Skewer 7/9.8, Hostile Cave Skewer 6/8.4, Ender's Crispy Skewer 2/0.8) | Not the same dishes; same family. Family rule is "shaped only + one sequenced assembly, stick kept, Food Plate" ([food_families.md](food_families.md) Skewer row) and BBQ's skewers come from a basin and a grill | D1: keep basin + grill as the stations and add the family mirrors; family tag membership |
| `grilled_beef_skewer` | *overlaps w/* FD Barbecue on a Stick (beef, stick) | Closest pair: both beef on a stick, 8 hunger each. BBQ version needs cabbage and grilling, gives Strength 50% | Keep both (different recipe and effect); note in family table |
| `grilled_lamb_skewer` | *overlaps w/* Veggies Turnip Mutton Skewer | Both mutton on a stick; BBQ's 12 hunger / 19.2 saturation is the strongest skewer in the pack | Keep; D5 name (pack says Mutton, never Lamb); value pass |
| `grilled_salmon_skewer`, `grilled_cod_skewer` | *overlaps w/* Oceanic Seafood Skewer, FD Grilled Salmon | Seafood skewer is a mixed fish stick; Grilled Salmon is a plated fillet. Different items | Keep |
| `grilled_pork_sausage_skewer` | *overlaps w/* End's Delight Ender Sausage (sausage line) | Only the word; no pack pork sausage item exists | Keep |
| `burnt_skewer` | none (pack has no burnt food item) | - | Keep, `hazard` overlay family |
| `kebab_wrap` | *overlaps w/* FD Mutton Wrap (wrap family) | Uses bread; pack wrap rule is "a tortilla in place of bread" ([food_families.md](food_families.md) Wrap row, TODO dishes/meals #2) | D3: `forge:tortilla` instead of `forge:bread` |
| `kebab_sandwich` | sandwich family | Shapeless `combine`; sandwich rule is shaped + one sequenced assembly | D3 |
| `bibimbap` | meal family (bowl meals) | No bowl in or out, no Cooking Pot; name is new to the pack | D3: bowl + pot or heatless `_manual_only` route |
| `chili_powder` | *overlaps w/* Vintage Ghost Pepper (`forge:chilipepper`, only member), Rustic bell peppers | No pack chili powder; the pack's only chili crop is the ghost pepper | D2 (source); keep item |
| `pepper_powder` | *overlaps w/* Ender's Crushed Voidpepper (pepper powder from an End crop) | Different flavour; no black pepper crop in the pack | Keep |
| `cumin_powder` | none | No cumin crop in the pack | Keep |
| `honey_mustard_sauce`, `buffalo_sauce`, `barbeque_sauce` | FD Tomato Sauce, Hostile Glowing/Ardent Sauce, End's Chorus Sauce, Vintage Apple Sauce | Those are food ingredients; BBQ's are durability seasoning tools | Keep |
| `basin` "Ingredients Basin" | *overlaps w/* Create **Basin** (`create:basin`) | Name nearly the same; different block | D5: keep the "Ingredients Basin" name, never shorten |
| `grill` | none (no grill block in the pack; FD Skillet and Stove are the closest heat stations) | - | Keep |
| `tray` | Display Delight plates, Wardrobe Silkworm Tray (name only) | Plain storage, no food logic | Keep |

### 2.2 Mob drops, dishes, ingredients

- No new raw ingredients: every skewer input is a pack tag (`forge:raw_beef` includes FD Minced Beef, `forge:raw_pork`
  includes FD Bacon, `forge:raw_mutton` includes Mutton Chops). Two minced beef make a beef skewer: fine.
- Same-food-different-id merges: **none**. Every BBQ food is a new dish.

### 2.3 Systems

| System | Overlap | Problem | Proposal |
|---|---|---|---|
| **Pack seasonings framework** ([food.md](food.md) 3.12, `seasonings.js`, `NCH.season`) | BBQ has a second, separate seasoning system: a data component, applied by click with a durability item, that *transforms* the food's existing effects (x2 duration, +1 amplifier, x2 chance) and adds an on-eat hit or heal | The pack's seasonings are NBT lists added by craft and give additive bonuses; they cannot express "double the effect duration". Two systems on one skewer would both show and both apply | Keep BBQ's own for now (O4); fold into the pack framework later (`food/seasoning #11`, 3.3.1) |
| Salt (`food/seasoning #1`) | Salt goes on every valid food, every meat dish | BBQ skewers are meat dishes, so saltable | Left for the family fold (O1, `dishes/meals #46`); not added now |
| Skewer food type (`dishes/meals #13`, in progress) | BBQ doubles the family to 18 skewers | BBQ is a better template for the type than the current mixed set | Not folded now (O1); fold when `dishes/meals #46` is done |
| Cooking heat rule (Smoker + Furnace + Campfire for cooked meat) | Grill is the only BBQ cook route | Raw skewers in a furnace are not covered by any rule; adding them removes the grill's reason to exist | Grill only (O1) |
| Heat sources | Grill uses FD `HeatableBlockEntity` (`farmersdelight:heat_sources` below) **or** a built-in campfire | Built-in campfire is permanent light 15 and heat, cannot be doused | B4 |
| Villager trades (`economy/trades.js`, MoreJS) | 6 seasonings added to the butcher's level-1 pool in Java | Pack trades are edited in KubeJS; Java trades are only visible to `forEachTrades` filters (precedent: the Canned Goods butcher filter) | Not ported (O6); a comprehensive seasoning trade pass comes later (`food/seasoning #12`) |
| Compost ([composting.md](composting.md)) | `neoforge:compostables` data map | 1.21 only | Re-express as a hand `barbequesdelight/compost.js` (O7, 3.7) |
| Display / placeable ([display_delight.md](display_delight.md)) | Skewer and sandwich families say Food Plate; FD and End's sticks already have plated models | BBQ has no placeables | None added (O8); recolor review in `food/placeable #5` |
| Jade vs WTHIT | Jade grill tooltip | Pack runs WTHIT, not Jade | Port as a WTHIT provider in the port (O9, step 17; memory `wthit-plugin-patchermod`) |
| CarryOn tag | `carryon:block_blacklist` | CarryOn not in the pack | Drop |
| Cans | handhelds never canned | - | None |

### 2.4 Batch sibling: Carnivore

Carnivore (same TODO row, `carnivore.md`) adds mob meats (camel, horse, whale, sniffer, turtle, bird, bat, strider,
squid tentacle). BBQ's skewering recipes read `forge:raw_beef`, `forge:raw_chicken`, `forge:raw_mutton`, `forge:raw_pork`
and the cod/salmon tags, so any Carnivore meat placed in those tags would silently become a skewer input. Rule for both
reports: Carnivore meats go into their own `food_type` tags, not the vanilla-animal `forge:raw_*` tags, unless the owner
asks for "camel kebab" by name. No item-level overlap between the two mods.

## 3. New content

### 3.1 Skewers (10 raw, 10 cooked)

Raw skewers are not edible. Cooked skewers return the stick on eating and in crafting (`craftRemainder`). Stack 64.
Saturation below is in points (nutrition x modifier x 2), as in the pack's food dump.

| Line | Skewering (basin item x n, off hand x n) | Grill time (ticks, both sides) | Hunger / sat | Effect |
|---|---|---|---|---|
| beef | `raw_beef` x2, `cabbage` x2 | 320 | 8 / 11.2 | Strength 1:30, 50% |
| chicken | `raw_chicken` x2, `onion` x2 | 280 | 7 / 9.8 | - |
| cod | `raw_cod` x2, `tomato` x2 | 240 | 7 / 14 | - |
| salmon | `raw_salmon` x2, `tomato` x2 | 240 | 7 / 14 | - |
| lamb | `raw_mutton` x4, nothing | 320 | 12 / 19.2 | Regeneration 1:30, 50% |
| mushroom | brown mushroom x4, nothing | 240 | 6 / 4.8 | - |
| pork sausage | `raw_pork` x4, nothing | 360 | 8 / 11.2 | Resistance 1:30, 50% |
| potato | potato x2, `raw_bacon` x2 | 280 | 6 / 7.2 | Nourishment 1:30, 50% |
| rabbit | rabbit x2, `cabbage` x2 | 280 | 10 / 16 | Jump Boost 1:30, 100% |
| vegetable | `foods/vegetable` x2, `foods/fruit` x2 | 160 | 5 / 5 | Regeneration 1:00, 25% |

All tags are `c:` in the jar; the port maps them to the pack's `forge:` names (`forge:raw_beef`, `forge:crops/cabbage`,
`forge:crops/onion`, `forge:crops/tomato`, `forge:raw_fishes/cod`, `forge:vegetables` 38 items, `forge:fruits` 26 items).

### 3.2 Other foods

| Item | Hunger / sat | Effects | Made by |
|---|---|---|---|
| `burnt_skewer` | 4 / 1.6, always edible | Poison II 0:10, Nausea II 0:10 (100%) | a skewer left on a hot grill to 2x its time |
| `kebab_wrap` | 8 / 11.2 | Nourishment 1:00, 50%; plus every distinct effect of the skewer inside | combine: bread, 1 cooked skewer, cabbage |
| `kebab_sandwich` | 14 / 19.6 | effects of the 2 skewers inside | combine: bread, 2 cooked skewers, cabbage, onion |
| `bibimbap` | 16 / 22.4 | Nourishment 2:00, 100%; plus skewer effects | combine: cooked rice, onion, carrot, 3 cooked skewers |

Combine stores the skewer stacks (seasoning included) in the `content` component; the tooltip lists them, the food takes
each distinct effect once, and the skewers' seasoning on-eat actions run too.

### 3.3 Seasonings (6)

Durability items, applied by right-clicking a skewer on the grill or on an FD cutting board (mixin). One seasoning per
skewer. The name gains a prefix ("Chili Flavored Cooked Beef Skewer").

| Item | Uses | Effect on a seasoned skewer |
|---|---|---|
| `cumin_powder` | 64 | heals 2 health on eating |
| `pepper_powder` | 64 | fast eating |
| `chili_powder` | 64 | always edible; 2 fire damage on eating |
| `honey_mustard_sauce` | 16 | each effect: chance x2, duration /2 |
| `buffalo_sauce` | 16 | each effect: amplifier +1, duration /2; 2 fire damage on eating |
| `barbeque_sauce` | 16 | each effect: duration x2 |

Sauces do nothing on the 4 skewers without effects (cod, salmon, chicken, mushroom) beyond the name (B3). The only
upstream source of all six is the butcher trade, which the port drops (O6), so until `food/seasoning #12` lands they
are creative or command only.

All six are ported as upstream and marked for folding into the pack framework (O4, `food/seasoning #11`).
**Folded 2026-11-03 (4.5):** they are pack seasonings now (crafted on, single use, stack 64); the table above is the
upstream mechanic.

#### 3.3.1 How the pack seasoning system works (for the fold)

Source: [food.md](food.md) 3.12 (built 2026-10-23, live since 1.1.03). It is separate from Salt's `Salted`.

| Part | How it works | Where |
|---|---|---|
| Storage | NBT on the food stack, `Seasonings:["creativeengineering:garlic", ...]`, kept sorted so application order does not split stacks | patcherMod `food/Seasonings` |
| Definition | Each seasoning is written once: id, display name, grid item, **bonus** (`nutrition` hunger points, `saturation` points capped at the food's, `heal` health points, timed `effects`), `combines` (keys it may share a food with; one side listing the other is enough; unlisted pairs do not combine), `families`, `add`, `remove`, optional `needs` (member mod) | `creativeengineering/seasonings.js` (`global.seasoning`, startup, both sides) |
| Craft | Special shapeless recipe type `creativeengineering:seasoning`: one food + one of each seasoning item gives the same food with those seasonings added, all other NBT kept. Refused if the food already has the seasoning, a pair does not combine, or the food is outside the seasoning's tag. Emitted by `NCH.season`; one craft per seasoning and per combining pair | `seasoning.js` (server), `SeasoningRecipe` |
| Which foods | Item tag `#<ns>:seasonable/<path>` per seasoning, built from `families` (every member of each food family and subfamily), then `add`, then `remove`. A seasoning with no foods gets no craft. This is why skewers must join the skewer family before they can be seasoned by the pack system | `seasoning_tags.js`, `food_families.json` |
| Eating | `Seasonings.onEaten` (`LivingEntityUseItemEvent.Finish`, server, players) applies each bonus after the food's own | patcherMod |
| Display | Tooltip line per seasoning ("Garlic: Speed (0:10)"), AppleSkin hunger and saturation, Seasoned icon overlay, one JEI Seasoning page per craft | `SeasoningTooltip`, `AppleSkinSeasonings`, `SeasoningJEI` |

Differences from BBQ's mechanic, which the fold has to close:

| BBQ | Pack framework | Fold work |
|---|---|---|
| Applied by right-click with a durability item (64 or 16 uses) on the grill or a cutting board | Applied by a crafting-grid recipe that consumes the seasoning item | Decide: keep the click-and-durability use, or switch to crafts |
| One seasoning per skewer | Several, subject to `combines` | Pack rule wins unless the owner says otherwise |
| Cumin: heal 2 on eating | `heal` bonus | Maps directly |
| Pepper: fast eating | No field | New bonus field |
| Chili: always edible, 2 fire damage on eating | No field | New bonus fields |
| Honey mustard / buffalo / barbeque sauce: rewrite the food's own effects (chance x2, amplifier +1, duration x2 or /2); buffalo also 2 fire damage | Bonuses add to the food, they never rewrite its effects | New "transform" kind, or keep BBQ's mechanic for the sauces |
| Name prefix ("Chili Flavored ...") | Tooltip line, no rename | Pack style |
| Skewer-effect NBT in the combos copies seasoning on-eat actions | Combos have no seasoning logic | Combine would need to carry the `Seasonings` list |

### 3.4 Blocks and mechanics

- **Grill** (iron trapdoor + 7 iron ingots; pickaxe): 2 slots. Heated by an FD heat source below or by a campfire
  merged into it (upstream: place the grill on a lit campfire, or click its side with a campfire: state `has_campfire`,
  light 15, drops the campfire when broken; the port changes this, see 3.4.1). Per slot: progress counts up while hot and **down by 2 per tick while cold**. Flip is
  allowed from half time; the skewer turns into its grilled form at full time after a flip; at 2x time it becomes a
  Burnt Skewer. Unflipped skewers never cook, they only burn. Sneak + empty hand flips; empty hand takes. FD skillet
  sounds, steam and smoke particles.
- **Ingredients Basin** (stonecutter: log -> 2; axe): 2 slots, click to insert, sneak to insert one; stick in main hand
  + side in off hand on a slot makes a skewer.
- **Tray** (stonecutter: log -> 4; axe): 3 display slots, no logic; stacks on itself (any wood since O39), breaks unsupported. Ported **for evaluation** (O12): keep or drop is decided in `largecontent/mod-additions #38` after play.

#### 3.4.1 Grill campfire in the port (O11)

The port replaces the permanent `has_campfire` flag with a vanilla campfire's rules, so the grill with a campfire behaves
as the pack's campfires do:

| Rule | Behaviour |
|---|---|
| State | `lit` and `waterlogged` as a vanilla campfire; heat and light only while lit (light 15) |
| Placed / merged | Unlit when placed, as the pack's campfires (Unlit Campfire default, kept); lit by flint and steel, fire charge or a fire arrow |
| Extinguish | Water (waterlogged), a splash water potion, a shovel, as vanilla |
| Weather (rule revised 2026-11-02: rain does not douse a campfire in the signal-fire state; coal and charcoal blocks are signal-fire sources; see briefs/gameplay_misc.md) | The pack rain rule (patcherMod `campfire/CampfireRain`, tag `creativeengineering:campfire/rain_douses`): lit grill-campfire goes out in rain unless the block under it is in `campfire/rain_proof_fuel` (coal block, charcoal blocks); burns forever otherwise |
| Break | Drops the grill and the campfire (upstream) |
| Cold grill | Unchanged: progress falls by 2 per tick (B5 kept) |

Implementation check for the port: `CampfireRain` mixes into `CampfireBlockEntity` cook tick, which the grill's block
entity is not, so the grill must call the same rule class directly (config `[campfire] rainDouses`, `rainOutTicks`).
Soul campfire merging: only if upstream supports it; otherwise plain campfire only.

### 3.5 Recipes, loot, trades

26 recipes as in 1 (Facts), all ported. No mob or chest loot. The six level-1 butcher trades were not ported at first
(O6); **restored 2026-11-03** as upstream sold them (O38, `food/seasoning #18`): butcher level 1, 12 emeralds, 4 uses,
4 XP, price multiplier 0.05, now giving 64 of a powder or 16 of a sauce (O31). B2 (the novice pool flood) is kept by
that decision. CD server script `barbequesdelight/trades.js`. The comprehensive pass for every seasoning stays
`food/seasoning #12`.

### 3.6 Upstream bugs

Not installed, so no log evidence; all from code (`DecompiledMods/barbequesdelight-1.3.0/src`).

| # | Bug | Evidence | Fix |
|---|---|---|---|
| B1 | A burnt skewer re-syncs its grill every tick until removed | `GrillBlockEntity.ItemEntry.tick`: `time >= duration * 2` stays true after burning; each tick re-sets the stack and calls `inventoryChanged()` (setChanged + block update packet) | Burn only when `!burnt`; stop ticking a burnt entry |
| B2 | Six trades flood the novice butcher pool | `BBQGeneralEventHandlers`: 6 listings added to level 1 (vanilla has 4); novice offers are picked from that list, so most new butchers sell seasonings | Move to `trades.js` with the pack's spread (D2) |
| B3 | Sauces have no effect on 4 skewers | `BBQSeasoning.appendEffect` only rewrites existing effects; cod, salmon, chicken, mushroom have none | Document, or restrict sauces to skewers with effects (D4) |
| B4 | Built-in campfire never goes out | `GrillPlace`: `has_campfire` is a bool state, always heat and light 15; rain and water do nothing | Fixed in the port: vanilla campfire rules with the pack rain rule (3.4.1, O11) |
| B5 | A skewer burnt from seasoned loses its seasoning; a cold grill erases progress silently | `tick`: replaced stack has no components; `time -= 2` while cold | B5a by design; B5b by design. Note in tooltip text only |

### 3.7 Compost map (O7)

The jar's `neoforge:compostables` data map has no 1.19.2 form; the port writes the same chances into the pack's
composter table ([composting.md](composting.md) 2.3: explicit values beat the category rules, which skip meat and meals).
Hand file `development/creativeDelight/src/main/resources/kubejs_startup/barbequesdelight/compost.js` (hand rows win by
load order and `tools/gen_compost_values.py` skips it).

| Items | Count | Chance |
|---|---|---|
| 10 raw skewers | 10 | 0.25 |
| 10 cooked skewers | 10 | 0.5 |
| `burnt_skewer` | 1 | 0.25 |
| `kebab_wrap` | 1 | 0.5 |
| `kebab_sandwich` | 1 | 0.75 |
| `bibimbap` | 1 | 1.0 |

Total 24. The seasonings and the three blocks are not compostable. Keys come from the item dict (step 1). After the
port, `/dumpcompost` and `gen_compost_values.py --check` confirm the 24 rows and no warning for missing ids.

### 3.8 Every seasoning, its effect and its source (O14)

The full catalog (every pack and BBQ seasoning and sauce: type, system, mutex, complementary, source) is [seasonings.md](seasonings.md). This section keeps the BBQ detail.

**Source column:** none of the six BBQ seasonings has a source (trades dropped, O6). Fill the last column with your
temporary source. Ids are the port's (`barbequesdelight:` upstream ids kept). Values are the jar's, read from the code
(`BBQSeasoning`); example durations use the beef skewer (Strength 1:30, 50% chance). 2 health points = one heart.

**BBQ seasonings (item, applied on a skewer):**

| Seasoning | Id | Uses | Effect on the skewer | Example | Source |
|---|---|---|---|---|---|
| Cumin Powder | `barbequesdelight:cumin_powder` | 64 | Heals 2 health (one heart) on eating | - | owner: temporary |
| Pepper Powder | `barbequesdelight:pepper_powder` | 64 | Fast eating (half the eating time) | - | owner: temporary |
| Chili Powder | `barbequesdelight:chili_powder` | 64 | Always edible (eat when full); 2 fire damage on eating | - | owner: temporary |
| Honey Mustard Sauce | `barbequesdelight:honey_mustard_sauce` | 16 | Each of the skewer's effects: chance x2, duration /2 | Strength 0:45, 100% | owner: temporary |
| Buffalo Sauce | `barbequesdelight:buffalo_sauce` | 16 | Each effect: amplifier +1, duration /2; 2 fire damage on eating | Strength II 0:45, 50% | owner: temporary |
| Barbeque Sauce | `barbequesdelight:barbeque_sauce` | 16 | Each effect: duration x2 | Strength 3:00, 50% | owner: temporary |

All six also prefix the skewer's name ("Chili Flavored ..."); one per skewer (upstream). Sauces change nothing but the name
on cod, salmon, chicken and mushroom skewers, which have no effects (B3, kept as upstream). Seasoning is lost when a
skewer burns (B5).

**New seasoning (skewer fold, `food/seasoning #13`):**

| Seasoning | Applied by | Effect | Status |
|---|---|---|---|
| Grilled | the grill, when a raw skewer finishes cooking after a flip; also present on a skewer crafted from cooked ingredients | its own effect, **values not set** | design in 4.1; effect to be tuned in `food/types-and-tuning #19` |

**Pack seasonings already live** (for reference, `seasonings.js`; applied by shapeless crafts, see [food.md](food.md) 3.12):

| Seasoning | Item | Bonus | Combines with | Foods now |
|---|---|---|---|---|
| Cheese | any cheese wedge | +2 saturation | nothing | popcorn |
| Onion | any onion | +2 nutrition | garlic | popcorn |
| Garlic | garlic | Speed I 10 s | onion | popcorn |
| Sugar | sugar, powdered sugar | +1 nutrition (placeholder) | - | none yet |
| Void Pepper | Crushed Void Pepper (Ender's) | +2 nutrition, +0.4 saturation | nothing | popcorn |
| Salt | Salt mod | own `Salted` tag, +2 nutrition (separate system) | any | every valid food |

### 3.9 Skewer names (O23, REVISED by O34 2026-11-02)

**Built 2026-11-03 (4.5):** cooked skewers read plain "Beef Skewer", "Mutton Skewer" and so on (en_us only; the other
three lang files keep upstream text); raw ones keep "Raw ...". Name prefixes on a skewer: Salted, Garlic, Cumin Flavored,
Peppered (was Pepper Flavored), Chili Flavored, Honey Mustard Flavored, Buffalo Flavored, Barbeque Flavored. Grilled is
a tooltip line only.

**Superseded:** cooked skewers get no "Cooked" prefix, uncooked ones say "Raw", seasoned items take the prefixes Chili Flavored, Salted, Peppered and Garlic. The table below is the 2026-10-31 "Cooked" plan and is not to be built; redo it from O34: cooked skewers drop "Grilled" and "Cooked" and read plain "Beef Skewer" and so on; raw ones read "Raw Beef Skewer"; mutton stays (D5).

Lang only; ids stay `grilled_*` and `raw_*` (port rule: keep upstream ids). Items cooked by any route say "Cooked".

| Id | Upstream name | Pack name |
|---|---|---|
| `raw_beef_skewer` | Raw Beef Skewer | unchanged |
| `grilled_beef_skewer` | Grilled Beef Skewer | Cooked Beef Skewer |
| `raw_chicken_skewer` / `grilled_chicken_skewer` | Raw / Grilled Chicken Skewer | Raw Chicken Skewer / Cooked Chicken Skewer |
| `raw_cod_skewer` / `grilled_cod_skewer` | Raw / Grilled Cod Skewer | Raw Cod Skewer / Cooked Cod Skewer |
| `raw_lamb_skewer` / `grilled_lamb_skewer` | Raw / Grilled Lamb Skewer | Raw Mutton Skewer / Cooked Mutton Skewer (D5) |
| `raw_mushroom_skewer` / `grilled_mushroom_skewer` | Raw / Grilled Mushroom Skewer | Raw Mushroom Skewer / Cooked Mushroom Skewer |
| `raw_pork_sausage_skewer` / `grilled_pork_sausage_skewer` | Raw / Grilled Pork Sausage Skewer | Raw Pork Sausage Skewer / Cooked Pork Sausage Skewer |
| `raw_potato_skewer` / `grilled_potato_skewer` | Raw / Grilled Potato Skewer | Raw Potato Skewer / Cooked Potato Skewer |
| `raw_rabbit_skewer` / `grilled_rabbit_skewer` | Raw / Grilled Rabbit Skewer | Raw Rabbit Skewer / Cooked Rabbit Skewer |
| `raw_salmon_skewer` / `grilled_salmon_skewer` | Raw / Grilled Salmon Skewer | Raw Salmon Skewer / Cooked Salmon Skewer |
| `raw_vegetable_skewer` / `grilled_vegetable_skewer` | Raw / Grilled Vegetable Skewer | Raw Vegetable Skewer / Cooked Vegetable Skewer |
| `burnt_skewer` | Burnt Skewer | unchanged |
| the `grilled` seasoning | (new) | Grilled (a state the Grill adds, not an item) |

The raw names of the 8 pack skewers are decision 7 in 4.3. The Grill block, the "Grilling" JEI category and the recipe
type `grilling` keep their names.

### 3.11 Wood variants (O39, 2026-11-04)

A basin and a tray in each vanilla 1.19.2 wood: oak, spruce, birch, jungle, acacia, dark oak, mangrove, crimson,
warped. Built 2026-11-04 (Creative Delight jar, not installed yet).

- **Ids.** Oak keeps 1.3.0's `barbequesdelight:basin` / `tray` (placed blocks in saves stay); the rest are
  `<wood>_basin` / `<wood>_tray`. Names "<Wood> Ingredients Basin", "<Wood> Tray"; oak's are "Oak Ingredients Basin"
  ["Ingredients Basin"] and "Oak Tray" ["Tray"]. Dict keys `bbq_<wood>_basin` / `bbq_<wood>_tray` (item and block).
- **Code.** `BBQBlocks.Wood` enum; `BASINS` / `TRAYS` maps (blocks and items), `BASIN` / `TRAY` are the oak entries.
  One block entity type each, valid for every wood. Each copies its planks' properties (nether woods do not burn).
  Trays stack on any tray (was the same block only). JEI lists every basin as a skewering catalyst.
- **Textures.** Basin: the vanilla stripped log (`stripped_<wood>_log`, `_stem` for the nether woods) on the
  unchanged `custom/basin` model. Tray: `textures/block/<wood>_tray.png`, Farmer's Delight's `crate_bottom` with its
  4 wood tones mapped to the planks' 7-colour ramp (by brightness, positions 0, 2, 4, 6) and the frame the darkest plank
  colour darkened by the crate's frame/wood ratio. The oak tray uses `oak_tray.png` too (the shared `custom/tray` and
  `custom/tray_support` models point at it), so no tray reads Farmer's Delight's crate any more. Generator:
  one-off palette swap, not in `tools/` (rerun from this description if the crate or a plank changes).
- **Recipes.** Stonecutter, that wood's logs -> 2 basins / 4 trays (`#minecraft:<wood>_logs`, `_stems` for the nether
  woods). The oak pair takes `#creativedelight:bbq_oak_logs`: `#minecraft:logs` minus the 8 other woods, so modded
  logs still make the oak pair (as FD's spruce cutting board, kitchen.md 2.11). Ids
  `creativedelight:stonecutting/bbq_<wood>_basin_from_logs`, `bbq_<wood>_tray_from_logs` (oak included; the old
  `bbq_basin_from_logs` / `bbq_tray_from_logs` ids are gone).
- **Tags.** All 18 in `mineable/axe` (member data) and `blockrunner:surface/wood` (`tags.js`).
- Tests: SQ-BBQW-01 to -03, CQ-BBQW-01.

### 3.10 Skewer inputs and basin handling (2026-11-04)

**Skewer input tags.** Every basin and off-hand input of a skewering recipe is a pack tag
`creativedelight:skewering/<input>` (dict keys `i.t_skewering_<input>`), seeded in `barbequesdelight/tags.js` and read by
`recipes.js`. Adding a variant to an input = one entry in its tag; every skewer using that input takes it.

| Tag | Seeded with | Used by |
|---|---|---|
| `beef` | `#forge:raw_beef` | beef skewer (basin) |
| `chicken` | `#forge:raw_chicken` | chicken skewer (basin) |
| `cod` / `salmon` | `#forge:raw_fishes/cod` / `salmon` | cod / salmon skewer (basin) |
| `mutton` | `#forge:raw_mutton` | lamb skewer (basin) |
| `mushroom` | brown mushroom | mushroom skewer (basin) |
| `pork` | `#forge:raw_pork` | pork sausage skewer (basin) |
| `potato` | `#forge:crops/potato` + Veggies sweet potato (whole only: no Rustic potato slices) | potato skewer (basin) |
| `rabbit` | raw rabbit | rabbit skewer (basin) |
| `vegetables` | `#forge:vegetables` | vegetable skewer (basin) |
| `cabbage` | `#forge:crops/cabbage` (cabbage, cabbage leaf) | beef, rabbit skewer (off hand) |
| `onion` / `tomato` | `#forge:crops/onion` / `tomato` | chicken / cod, salmon skewer (off hand) |
| `bacon` | `#forge:raw_bacon` | potato skewer (off hand) |
| `fruit` | `#forge:fruits` | vegetable skewer (off hand) |

Bug fixed with it: the recipes passed `Item.of('#tag', n)`, which KubeJS turns into the tag's first item, so each skewer
took only one item per input (one beef, one cabbage, ...). Inputs are now `{ id: tag, count: n }`. The same bug locked the
Kebab Sandwich and Bibimbap to one cooked skewer; fixed the same way. A basin well still holds one item kind, so the n
items of an input are one stack of one variant.

**Sneak grab with full hands.** Forge skips `Block#use` when the player sneaks with anything in either hand, so the
basin's sneak take-one only worked empty-handed. `BasinBlock.onSneakGrab` (RightClickBlock listener) takes one item from
the clicked well into the inventory (dropped when full) whenever the player sneaks with an item in either hand. Empty
hands keep the old path (one item into the hand). Sneak-insert-one never worked on the basin and is now replaced by this.
The Tray is unchanged.

**JEI.** Skewering: background padded 8px on the left; the vanilla empty off-hand slot sprite is drawn left of the
off-hand slot. Grilling: the result shows the `grilled` seasoning (`PackSeasonings.addGrilled` on a copy, as the Grill
does).

## 4. Fold-in, in build order

### 4.0 Build record (2026-11-01)

Built in one session, steps 0-19. Not installed (the game may be running): the Creative Delight jar and the patcherMod
jar go to `mods/` together. Offline checks done: member, bundle and patcherMod compile; the bundle nests
`backportedBarbeques-1.3.0-ce-19.2-01.jar` and carries the scripts; `recipes.js` run headless through the real NCH gives
the 26 recipes with the inputs and times of 3.1; the item, block and food dicts load (food grammar check passes). The
javac logic harness of 7 was not written: the grill tick needs a Level and recipe manager, so it is covered by SQ-BBQ-05
to -08 instead.

| Step | Done | Where / note |
|---|---|---|
| 0 Port | yes | `development/creativeDelight/members/barbeques` (project `backportedBarbeques`, mod id `barbequesdelight`, package `com.SaintAlfonzo.creativedelight.barbeques`), nested last in `bundle_members`. Not `development/backportedBarbeques` as planned: CD members live under `members/`. LGPL notice `NOTICE-upstream-LGPL.txt`. B1 fixed. Stack NBT: seasoning `BBQSeasoning` (string), combo contents `Skewers` (list), grill `Entries` |
| 1 Ids | yes | `item_dict.js` (36 items + `t_raw_skewers`, `t_grilled_skewers`, `t_raw_cod`, `t_raw_bacon`; the blocks are `bbq_grill`, `bbq_basin`, `bbq_tray` because `i.basin` is Create's), `block_dict.js` (3 blocks), `food_dict.js` (34 keys) |
| 2 Tags | yes | `kubejs/data/creativedelight/kubejs/barbequesdelight/tags.js`; skewer tags also in the port's data; mineable tags in the port's data; CarryOn tag dropped |
| 3, 7 Recipes | yes | All 26 through `global.NCH` (`recipes.js`): new emitters `bbqSkewering`, `bbqGrilling`, `bbqCombine` in patcherMod's `neoCraftHelper.js`. The port ships **no** recipe data (step 3's "23 stay as data" dropped in favour of step 7) |
| 8 Effects | none needed | |
| 9 Food types | yes | `food_types.json`: 10 cooked skewers, wrap, sandwich `handheld` (where the pack skewers are; no skewer type yet), bibimbap `meal`, burnt skewer `hazard`; raw skewers are not food. SoL complexity: nothing to add (the tuning system of food.md 3.5 is not built, defaults apply) |
| 10 Families | yes | `tools/food_families.py`: cooked skewers in `NO_FAMILY` (closest: skewer, waits for `dishes/meals #46`), bibimbap override `meal/rice`; wrap and sandwich by name (`sandwich/wrap`, `sandwich/roll`), burnt by type (`hazard`). `food_families.json` regenerates after `/dumpall` |
| 11 Stack sizes | after `/dumpall` | upstream sizes now (skewers and combos 64, seasonings 1); `gen_stack_sizes.py` reads the families after the dump |
| 13 Compost | yes | CD startup `barbequesdelight/compost.js`, 24 rows |
| 16 Lang | yes | 3.9 names ("Cooked ...", Lamb shown as Mutton); `en_ud`, `uk_ua`, `zh_cn` carried; Jade and tab keys dropped |
| 17 Audit | yes | BlockRunner: grill `metal`, basin and tray `wood` (`tags.js`, dict keys `t_blockrunner_metal` / `_wood`); WTHIT provider `compat/wthit` (`barbequesdelight:grill`, server data + client lines); Item Physic audit after `/dumpall` |
| 18 JEI | yes | Grilling and Skewering categories; fixes: Skewering page has its own title (1.3.0 showed "Grilling" on both), time line shows each side (half the recipe time) |
| 19 Release | partly | `mod_sides.md` note on the CD row, changelog family `## Barbeque's Delight`; plan row at the next release |

Other port choices (no owner decision needed, recorded here):
- **Cutting board:** a `PlayerInteractEvent.RightClickBlock` listener (priority HIGH) instead of 1.3.0's mixin.
- **Campfire merge:** any dry plain campfire is taken in (1.3.0 needed a lit one, but the pack places campfires
  unlit), and the grill starts unlit (3.4.1). Splash water bottles put it out through a `ProjectileImpactEvent`
  listener; fire arrows light it; the shovel test is `ShovelItem` (1.19.2 has no douse tool action).
- **Creative tab:** Farmer's Delight's, as every CD member; 1.3.0's own tab is not registered.
- **Grill and storage blocks drop their contents** when broken (1.3.0 did through its library).
- **Recipes are `isSpecial`** (kept out of the recipe book, as the pack does for custom types).

Route (O10: plain Forge rewrite, part of Creative Delight): fork into `development/backportedBarbeques` (LGPL-2.1 upstream notice + GPLv3 for our code), ids under
`barbequesdelight:` (port rule: keep upstream ids), then nest as a Creative Delight member like the other Delight ports.
KubeJS rows go in CD trees: startup `development/creativeDelight/src/main/resources/kubejs_startup/barbequesdelight/`,
server `kubejs/data/creativedelight/kubejs/barbequesdelight/` (memory `creative-delight-routing`).

0. **Port the code** (the large step). Replace the four bundled libraries with plain Forge 1.19.2 (D8):
   - Registrate MC1.21 -> `DeferredRegister` + hand-written models and blockstates (copy the generated JSON from the jar).
   - L2ModularBlocks `DelegateBlock` + `BlockMethod` -> three ordinary `Block` subclasses (`GrillBlock`, `BasinBlock`,
     `TrayBlock`) with `use()`.
   - L2Serial `@SerialClass` / `@SerialField` -> `saveAdditional` / `load` and `getUpdateTag`.
   - L2Core `BaseBlockEntity`, `TickableBlockEntity`, `DCReg` -> `BlockEntity` + ticker, NBT.
   1.21 -> 1.19.2 format: data components `seasoning` and `content` -> stack NBT; `FoodProperties` record ->
   1.19.2 builder (`saturationMod`, `effect(Supplier, float)`, no `eatDurationTicks`: fast via `fast()` only);
   `ItemInteractionResult` + `useItemOn` / `useWithoutItem` -> `use()`; `RecipeInput` (`SingleRecipeInput`,
   `SkeweringInput`) -> `Container`; `RecipeHolder` -> `Recipe#getId`; result JSON `id` -> `item`; `recipe/` ->
   `recipes/`, `loot_table/` -> `loot_tables/`, `tags/item` -> `tags/items`, `tags/block` -> `tags/blocks`; drop
   `category` in recipe JSON; `damageSources().inFire()` -> `DamageSource.IN_FIRE`; `hurtAndBreak(slot)` -> consumer
   form; `Holder<MobEffect>` -> `MobEffect`; `TooltipContext` -> `Level`; `ResourceLocation.withDefaultNamespace` ->
   `new ResourceLocation`. Mixin target: FD 1.19.2 `CuttingBoardBlock.use`. Fix B1. Then `/dumpall` (memory
   `dumpall-refresh-workflow`) before 1-19.
1. **Ids:** 36 items into `item_dict.js` (and the 3 blocks in the block dict); scripts use the keys.
2. **Tags** via `global.tags`: `raw_skewers`, `grilled_skewers` (built in data by the port, mirrored as pack tags);
   `food_type` tags only. **Not now (O1):** the skewer family tag and the saltable tag (`dishes/meals #46`). Drop the CarryOn tag.
3. **Upstream recipe removals and tag inputs:** the 23 BBQ-type recipes stay as data in the port with `forge:` tags;
   the 3 crafting/stonecutting ones are re-emitted through `global.NCH` (grill shaped; basin and tray stonecutting).
4. **Mob loot:** none (no drops).
5. **Item merges:** none (2.2).
6. **Potions:** none.
7. **Recipes by pack rules** ([recipes_cooking.md](recipes_cooking.md)): skewers by basin and grill only (O1): no
   family mirrors, no furnace route. Combos as upstream (O3): Kebab Wrap on bread with a note to move it to
   `forge:tortilla` (`dishes/meals #47`), Kebab Sandwich and Bibimbap as the upstream `combine` recipes (no bowl).
   Extend `global.NCH` with `bbqSkewering` / `bbqGrilling` emitters so the 20 data recipes go through the helper. S&D and
   Central Kitchen: no mirrors (no mixing or pot recipes). Container remainders: stick (skewers, burnt).
8. **Effects:** no new effects. JEED text: none needed. Seasoning text as upstream (O4); pack style later (`food/seasoning #11`).
9. **Food types and values** (`food_types.json`, [food.md](food.md)): `meat/cooked` / `fish/cooked` sub-entries under
   the skewer type; value pass on lamb (12 / 19.2 is above every pack skewer) and rabbit; SoL complexity entries.
   **Deferred with the family fold (O1):** upstream values stand until `dishes/meals #46`; only the `food_type` tags and
   SoL entries are added now.
10. **Food families:** wrap -> `sandwich/wrap`; sandwich -> `sandwich`; bibimbap -> `meal`. **Skewers deferred (O1):** the
    10 cooked skewers -> `skewer` and burnt -> `skewer` + `hazard` wait for `dishes/meals #46`, so they are not
    seasonable by the pack system yet. Run `tools/food_families.py --check`.
11. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): skewers and combos per the family columns (skewer 64, wrap 64,
    meal 16/32 if bowled); seasonings already 1 (durability).
12. **Cans and jars:** none (handhelds never canned).
13. **Compost (O7):** 24 entries in the hand file `barbequesdelight/compost.js` (table 3.7); verify with `/dumpcompost`.
14. **Crops, wild crops, seasons:** none (no crops). If D2 adds a powder recipe, its source crop (ghost pepper) is
    already seasonal.
15. **Worldgen / structures:** none.
16. **Names and lang** ([names.md](names.md)): D5 (Lamb -> Mutton in lang only; Ingredients Basin kept); "Barbeque"
    spelling kept as the mod's own; `en_ud`, `uk_ua`, `zh_cn` carried.
17. **Mob and block audit:** BlockRunner tier for grill (metal) and basin/tray (wood); mineable tags as upstream; Item
    Physic fire behaviour for skewers; **WTHIT provider for grill progress, porting the Jade tooltip (O9, `ui/wthit #13`)**: `waila_plugins.json` entry, server half writes each slot's progress and flip state into the data tag, client half draws the countdown and Ready to Flip at half time (pattern: the hive plugin, memory `wthit-plugin-patchermod`).
18. **Tooltips and JEI** ([tooltips.md](tooltips.md), [jei_coverage.md](jei_coverage.md)): keep BBQ's Skewering and
    Grilling JEI categories; seasoning tooltip line as upstream (O4), pack style at the fold; FD effect-tooltip gate kept.
19. **Placeables and trades:** none now. BBQ has no placeables and none are added (O8); `food/placeable #5` reviews
    recoloring the FD / End's stick plated models. The seasoning trades are not ported (O6); `food/seasoning #12`
    designs them for all seasonings. **Release:** `mod_sides.md` note under the CD row, changelog
    bullets under a `## Barbeque's Delight` family, TODO row, plan row.

Ordering: 0 before everything (ids exist only after the port); `/dumpall` after 0 and before 9-13 and 16-18; 1 before
2-3; 2 before 7 and 10. D1-D4 and D7 are answered (0.1); the deferred rows `dishes/meals #46`, `#47`, `food/seasoning #11`, `#12`
and `food/placeable #5` need the port first.

### 4.1 Fold design (O18, O19), later work

Not part of the port. The port ships BBQ as upstream (basin, grill, upstream skewer items, click-applied seasonings).
The fold happens afterward, in the order **seasoning system first** (`food/seasoning #11`), then the skewers
(`dishes/meals #46`, `food/seasoning #13`), because the skewer change rides on the new seasoning kinds.

**Seasoning fold (`food/seasoning #11`)**
- The pack seasoning system gains the new effect kinds it lacks: fast eating, always edible, damage on eating
  (fire), and an effect **transform** (chance multiplier, duration multiplier or divisor, amplifier add) that rewrites the
  food's own effects. Cumin's heal already exists. These are defined once in `seasonings.js`, so any seasoning can use them.
- Food is seasoned by **shapeless crafting** (`NCH.season`), as every pack seasoning. The click-with-a-durability-item
  use is retired at the fold.
- **Max seasonings per food is configurable**, default **2** (proposed key `[food] maxSeasonings` in CEConfig, config
  backup in `configs_edited/`); `grilled` does not count, configurable (`[food] grilledCountsTowardMax`, default false). It replaces BBQ's one-per-skewer; the `combines` lists still decide which pairs are allowed.
- Skewers become seasonable foods through the skewer food family (`seasonable/` tags).

**Skewer fold (`dishes/meals #46`, `#49`, `#50`, `food/seasoning #13`), revised 2026-10-31 (third batch)**
- **The BBQ system stays** (basin, grill, upstream raw and cooked skewer items). Nothing of BBQ is folded into pack skewers.
- **The 8 pack skewers are added to it with a raw version each** (new items); the Grill cooks a raw version into the
  existing item. Their existing cooked recipes stay.
- **Raw skewers stay**: edible, worse food, sometimes poisonous, grilled into the cooked version. The "nasty" food type
  goes only to chicken (O27).
- **Seasonings** (powders and pack seasonings) may go on a raw skewer and **copy over to the cooked skewer** when it is grilled; **sauces may not** go on a raw one (O28).
- **`grilled`** is added **only** by cooking on the Grill. It gives **+2 saturation** and **does not count** toward the
  seasoning cap (configurable, proposed `[food] grilledCountsTowardMax`, default false).
- **Cooked skewers are craftable from cooked ingredients by the skewer family rule (shaped plus one sequenced assembly, O29)** and do **not** get `grilled`.
- **Smelting or smoking a raw skewer** (cooked skewer without `grilled`) is a noted open decision (`dishes/meals #49`).
  Until it is decided, only the Grill cooks raw skewers.
- Family recipes later, not now. Overlaps kept (overlap table in TODO).

**Seasoning use (O20):** before the fold a BBQ seasoning loses 1 durability per application. After the fold crafting
consumes one item, and the crafts and trades that give BBQ seasonings give several items so the total number of uses is
about what a 64-use powder or 16-use sauce gave.

Full seasoning catalog, cap and pairs: [seasonings.md](seasonings.md).

### 4.2 Questions from the fold design (all answered)

| # | Question | Answer |
|---|---|---|
| Q1 | Which skewers fold | BBQ system kept; other mods' skewers join it |
| Q2 | Raw skewer edible | Edible, worse food, sometimes poisonous |
| Q3 | Does `grilled` count toward the cap | No, configurable |
| Q4 | Consume or damage the seasoning item in a craft | Before the fold 1 durability; after the fold one item per craft, sources scaled (O20) |
| Q5 | `grilled` effect | +2 saturation (O25); raw and cooked values in `food/types-and-tuning #19` |
| Q6 | Does `grilled` still exist beside the cooked item | Yes, added only by the Grill (O21) |
| Q7 | Other mods' skewers: seasoning only, or raw versions | Raw versions (O24) |

### 4.3 Further open decisions (found 2026-10-31)

**All answered 2026-11-02** (owner decisions O31 to O38 in 0.1); the table below keeps the original wording. Answers: #1 64 and 16; #2 default; #3 default; #4 no nasty type, folded into raw chicken (O32); #5 default; #6 yes, no `grilled` (O33); #7 O34; #8 default; #9 default; #10 O35; #11 default; #12 O36; #13 default; #14 default.

Each has a default that applies if unanswered.

| # | Decision | Size | Default |
|---|---|---|---|
| 1 | Items per old durable item after the fold (O20): how many seasoning items replace one 64-use powder and one 16-use sauce, and does the craft or trade yield that many | big | The same number of uses as upstream: one craft or trade gives 64 powders or 16 sauces ("a similar usable amount"); tune in play |
| 2 | Raw versions of the 8 pack skewers (O24): item namespace (the pack skewer's own mod id via a CD member, or `barbequesdelight`), the recipe that makes them (basin with raw ingredients? what ingredients), grill time, stack size, and a recolored texture for each | big | CD member under the original mod's namespace, one basin recipe per skewer from its raw ingredients, grill time by the closest BBQ line, stack 64, recolor review in `food/placeable #5` |
| 3 | `grilled` +2 saturation (O25): confirm this means +2 saturation points in the pack's unit (capped at the food's hunger, like cheese's +2), not +2 hunger shanks | small | +2 saturation points, capped |
| 4 | "Nasty" food type | big | **Answered (O27):** hazard-style downside for chicken only; other raw meats worse but not nasty. Poison chance and values: `food/types-and-tuning #19` |
| 5 | Sauces and raw skewers | big | **Answered (O28):** no sauces on raw skewers; seasonings allowed and copied to the cooked skewer |
| 6 | Smelting or smoking raw skewers (O22): result item, whether the smoker and campfire count, and whether the result is worse than grilled | big | Open (noted only) |
| 7 | Names (O23): the raw names of the pack skewers ("Raw Barbecue on a Stick", "Raw Crispy Skewer" read badly); whether the seasoned prefix "Chili Flavored Cooked Beef Skewer" is reordered; whether the 8 pack skewers get a "Cooked" label (they are not renamed now) | small | Prefix "Raw " on the 8 (Crispy Skewer: "Uncooked Crispy Skewer"); prefix order kept; pack skewers not renamed |
| 8 | Cooked-ingredient craft | big | **Answered (O29):** shaped plus one sequenced assembly (the skewer family rule). Still to set: the ingredients per line for the 10 BBQ and 8 pack skewers, and the yield (default: cooked meat in place of raw, vegetables and stick unchanged) |
| 9 | Two cooked skewers that do not stack | small | **Answered (O30):** same item, split by seasoning, no special code. Still small: tooltip line "Grilled" (default yes); the Grill takes raw skewers only (default) |
| 10 | Combos (wrap, sandwich, bibimbap) and `grilled`: do they carry the +2 saturation of the skewers inside, and does a crafted (no-`grilled`) skewer count as an ingredient | small | Both skewers count; the combo carries each skewer's `grilled` saturation |
| 11 | Config keys and files for the cap and `grilledCountsTowardMax`: CEConfig section name, and `configs_edited/` copy | small | `[food]` section |
| 12 | Tooltip and AppleSkin line for `grilled` (+2 saturation) in the pack seasoning style | small | One line "Grilled: +2 saturation", AppleSkin adds it |
| 13 | JEI: the Grilling category keeps its name though the item is "Cooked"; add the 8 raw pack skewers to it | small | Keep "Grilling" (the station) |
| 14 | Tests: names, raw skewer food and poison, `grilled` saturation, crafted skewer without `grilled`, sauce on a raw skewer | small | Added as BBQ-20 to BBQ-24 when 4.3 is answered |

### 4.4 Third batch (2026-11-02)

- Shaped cooked-ingredient skewer craft: cooked ingredients, plus one extra input of the largest-value ingredient on the line.
- Lamb (Mutton) 9 / 0.8 and rabbit 8 / 0.8 accepted (`food/types-and-tuning #19`); the rest tuned later.
- Butcher trades restored as upstream sold them: six, level 1, 12 emeralds, 4 uses, 4 XP (`food/seasoning #18`).
- Kebab Sandwich and Bibimbap recipes: defaults (`dishes/meals #48`). Tray: still in evaluation (`largecontent/mod-additions #38`).
- Pinned: raw pack skewers (`dishes/meals #50`, waits on textures) and seasoning crafts (`food/seasoning #17`).

### 4.5 Fold build (2026-11-03)

Built in one session for `food/seasoning #11`, `#13`, `#18`, `dishes/meals #46` (part) and `#49`. Compiled
(`:backportedBarbeques:compileJava`, `:creativeDelight:compileJava`), then `:creativeDelight:build`; installed with the CD jar
2026-11-03 00:08 (member `backportedBarbeques-1.3.0-ce-19.2-01` nested; framework Java, `seasonings.js` and lang are in
the CD jar), not run in game (last boot 2026-10-31). Scratch rows SQ-BBQF-01 to -07 (queue section SQ-CDJ7); client
tests BBQ-17, BBQ-22 to -29.

| Part | Built | Where |
|---|---|---|
| New seasoning kinds | `fast`, `alwaysEdible`, `damage` (+ `damageType` fire / magic / generic), `transform` {chance, duration, amplifier}, `station`, `combinesAll` | CD `food/Seasonings` (`Kinds`, `Transform`, `foodProperties`, `addFromStation`, `without`, `applyEaten`), parsed from `seasonings.js` |
| Cap | `[food] maxSeasonings` (default 2, 1-16) and `[food] grilledCountsTowardMax` (default false) in **creativedelight-common.toml** (CD's `CDConfig.Food`, not CEConfig: the framework moved to CD at the carve). `Seasonings.canAdd` refuses a craft past the cap | `CDConfig`, `Seasonings` |
| Six seasonings | defined once in `seasonings.js` as `barbequesdelight:cumin` (heal 2), `pepper` (fast), `chili` (always edible, 2 fire damage), `honey_mustard` (chance x2, duration x0.5), `buffalo` (2 fire damage, duration x0.5, amplifier +1), `barbeque` (duration x2). Powders on `#barbequesdelight:raw_skewers` + `grilled_skewers`, sauces on `grilled_skewers` only (O28 by tag). No pairs among them (not decided) | `seasonings.js` |
| Crafts | the existing `seasoning.js` loop emits one `NCH.season` craft per seasoning; one item consumed | no change needed |
| Click use retired | grill and cutting board seasoning removed; seasoning items lose durability, stack 64; old `BBQSeasoning` NBT is not converted (no conversions) | member `GrillBlock`, `BarbequesDelight`, `SeasoningItem`, `BBQItems` |
| Fast / always edible / transform on the food | the skewers and combos ask `Seasonings.foodProperties` (member `compat/PackSeasonings`, reflection: members do not compile against CD). Other foods do not get these three kinds until their item asks too | member `BBQSkewerItem`, `BBQSandwichItem` |
| `grilled` | station seasoning `barbequesdelight:grilled`, +2 saturation points (capped at the food level), no craft, combines with any. The Grill adds it when a flipped skewer finishes; furnace, smoker, campfire and shaped crafts do not. Tooltip "Grilled: +2 saturation" and AppleSkin by the existing seasoning code | `seasonings.js`, member `GrillBlockEntity` |
| Raw to cooked | the Grill copies the raw skewer's NBT (seasonings included) as before | unchanged |
| Combos | the combine recipe drops `grilled` from the skewers it stores (O35); eating runs each stored skewer's seasoning bonuses | member `CombineItemRecipe`, `BBQSandwichItem` |
| Trades | six butcher level-1 trades (3.5) | `barbequesdelight/trades.js` |
| Furnace routes (O33) | smelting (200 ticks), smoking (100), campfire (600), 0.35 XP, for all 10 lines. Vanilla cooking keeps no NBT: seasonings on a raw skewer are lost on these routes | `recipes.js` |
| Cooked-ingredient craft (O38, 4.4) | shaped, `_manual_only`, 10 lines: `[' AB', 'ABA', 'S  ']` with 3 A + 2 B (A = the larger: cooked beef / chicken / cod / salmon, baked potato over cooked bacon, cooked rabbit, vegetables over fruits) or `[' AA', 'AAA', 'S  ']` with 5 cooked mutton / brown mushroom / cooked pork. Mushroom and vegetable lines have no cooked form, so raw inputs. The family's sequenced assembly is not built (family recipes later) | `recipes.js` |
| Family | cooked skewers leave `NO_FAMILY` in `tools/food_families.py` and classify as `skewer` (handheld, "skewer" in the name) at the next `/dumpall` + `food_families` run; that also makes them saltable | `tools/food_families.py` |
| Names (O34) | 3.9 | member `en_us.json`, `BBQSkewerItem`, `BBQLang` |
| WTHIT (`ui/wthit #13`) | already built 2026-11-01 (step 17): per-slot countdown, Ready to Flip at half time, Cooked, Burnt | member `compat/wthit` |

Not built: raw pack skewers (`dishes/meals #50`, pinned), seasoning crafting recipes (`food/seasoning #17`, pinned),
seasoned-icon overlays for the seven new seasonings (no textures; the model lookup finds nothing and draws none, the
model loader may log a missing-model line per id), the 8 pack skewers as seasonable foods (with `#50`).

### 4.6 Owner fixes (2026-11-03, after the 1.2.00 play)

- Seasoned icons: the seven new seasonings (`cumin`, `pepper`, `chili`, `honey_mustard`, `buffalo`, `barbeque`, `grilled`)
  get overlay models `barbequesdelight:item/seasoned/<id>` in the member, all on Salt's `salt:item/salted_overlay` texture
  until own textures exist (owner: "use the salt icon for now"). Fixes the seven `Unable to load model` lines.
- WTHIT: `barbequesdelight.jade.burnt` reads "Burnt" (was "Burnt!!").
- JEI titles: the Seasoning page title was lost to the shared lang path, not a BBQ fault ([creative_split.md](creative_split.md) 3 rule 11).
- Trades: the six seasonings were missing from JER only on the dedicated server ([trade_sync.md](trade_sync.md)).

Built into the CD jar and installed 2026-11-03 (member still `-01`). Tests: CQ-BBQF-02, CQ-LNG-01, CQ-TSY-01.

## 5. Decisions for the owner

All answered 2026-11-02 (O38 in 0.1).

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Skewer recipes: basin + grill only, or also the skewer family's shaped grid + sequenced assembly, and furnace/campfire cooking of raw skewers? | Basin + grill as the main route; add the family's shaped `_manual_only` grid from **cooked** meat + stick and one assembly (pack rule); **no** furnace route for raw skewers | Basin + grill + family mirrors, no furnace |
| D2 | Seasoning source: butcher trade only? | Move the 6 trades to `trades.js`, spread over butcher levels 1-3, fewer at level 1; add powder crafts only if asked by name (chili from ghost pepper is the obvious one) | Trades only, moved to `trades.js` |
| D3 | Combos: wrap on bread or tortilla; sandwich shapeless or shaped; bibimbap bowl? | Wrap on `forge:tortilla`; shaped sandwich + one assembly; bibimbap takes and returns a bowl, Cooking Pot + heatless `_manual_only` | as recommended |
| D4 | Two seasoning systems: keep BBQ's own (click, durability, transforms effects), or map the six onto the pack framework? | Keep BBQ's mechanic for skewers (the framework cannot express "x2 duration"); render its tooltip line in the pack seasoning style; allow pack seasonings (salt) to stack on top | Keep BBQ's, pack style tooltip |
| D5 | Names: "Grilled Lamb Skewer" vs pack "Mutton"; "Ingredients Basin" vs Create Basin | Rename to "Mutton" in lang (id unchanged); keep "Ingredients Basin" | as recommended |
| D6 | Built-in grill campfire (permanent heat, light 15) | Keep; let water put it out (state false, drops the campfire) | Keep as upstream |
| D7 | Placeables for 13 foods | Food Plate entries for the 10 skewers + 3 combos, models later | Add to the plate tag (flat draw) |
| D8 | Port with plain Forge, or ship 1.19.2 L2Library + Registrate | Plain Forge rewrite (60 classes, small; no new library jars) | Plain Forge |
| D9 | Values: lamb 12/19.2 and rabbit 10/16 outrank every pack skewer | Lamb 9/0.8, rabbit 8/0.8 after the skewer food type lands | Upstream values until the type lands |
| D10 | Tray (no food logic): port or drop | Port (cheap, same storage code as the basin) | Port |

Status after the owner's notes (0.1): **D1** answered (basin + grill only, no mirrors, no furnace); **D2** answered (no
trades; the source gap is open in `food/seasoning #12`); **D3** partly (wrap on bread now, tortilla later; sandwich and
bibimbap as upstream, rework in `dishes/meals #48`); **D4** answered (BBQ's mechanic now, fold later, design in 4.1);
**D5** answered (renames kept); **D6** answered (vanilla campfire rules, 3.4.1); **D7** answered (nothing added); **D8**
answered (plain Forge, CD member); **D9** answered (upstream values, tuned in `food/types-and-tuning #19`); **D10**
answered (tray ported for evaluation). **No decision is open** apart from the questions in 4.2.

## 6. File map (planned)

| Path | What |
|---|---|
| `development/creativeDelight/members/barbeques/` | The port (built 2026-11-01): Java (blocks, block entities, recipes, items, cutting board event, JEI, WTHIT grill provider), data (loot, tags), assets, `NOTICE-upstream-LGPL.txt` |
| `development/creativeDelight/gradle.properties`, root `settings.gradle` | Member nesting (`bundle_members`, project `backportedBarbeques`) |
| `kubejs/startup_scripts/_lib/item_dict.js`, `block_dict.js`, `food_dict.js` | Ids |
| `kubejs/data/creativedelight/kubejs/barbequesdelight/` | `recipes.js` (all 26), `tags.js` |
| `development/patcherMod/.../kubejs_startup/creativeengineering/func/neoCraftHelper.js` | `bbqSkewering`, `bbqGrilling`, `bbqCombine` emitters |
| `kubejs/generation_scripts/food/food_types.json`, `tools/food_families.py` | Food types, family overrides |
| `development/creativeDelight/src/main/resources/kubejs_startup/barbequesdelight/compost.js` | 24 compost entries (hand file) |
| `feature_writeup/barbeques_delight.md` | this file; plus rows in `food_families.md`, `creative_delight.md`, `mod_sides.md` |

## 7. Offline checks (once built)

- `./gradlew :creativeDelight:build --offline` clean; jar contains `barbequesdelight` classes, no `META-INF/jarjar`.
- javac harness: grill entry tick (flip at half, cook at full, burn at 2x, cold decay), B1 (one sync on burn), seasoning
  `appendEffect` maths, combine NBT round trip.
- Scratch server (queue, [scratch_queue.md](testing/scratch_queue.md)): all 23 BBQ recipes load (no `Failed to parse` for
  `barbequesdelight`), tags resolve, trades present at the intended levels.
- `tools/food_families.py --check`, `python3 tools/changelog_lint.py`.

## 8. In-game test suite (drafted)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-BBQ-01, -03, -04, -05; rerun pending (batch fault) SQ-BBQ-02, -06, -07.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-BBQF-01, -05, -02, -03, -04, -06, -07, SQ-BBQ-02, -06, -07.

Scratch r1102b (2026-11-02, `perf/runs/r1102b/s3/judged.md`): SQ-BBQ-01 (boot), -03 (tags), -04 (24 compost rows by
tier), -05 (grill on a campfire, rain on stone / coal, drops grill + campfire: server half of BBQ-07, BBQ-18) PASS.
OPEN, batch faults: SQ-BBQ-02 (skewering 10 and grilling 10 present; the combine recipes are `creativedelight:shapeless/*`,
the row's id pattern is stale), SQ-BBQ-06 and -07 (the bot's click lands outside the grill slot, nothing reached the
grill). SQ-BBQ-08 not run (bots cannot craft).

| ID | Test | Pass |
|---|---|---|
| BBQ-01 | Craft grill; stonecut a log into 2 basins and 4 trays | items appear |
| BBQ-02 | Basin: beef x2 in a slot, stick in main hand, cabbage x2 in off hand, click | Raw Beef Skewer; 2 beef, 2 cabbage, 1 stick used |
| BBQ-03 | Basin: mutton x4, off hand empty | Raw Lamb (Mutton) Skewer |
| BBQ-04 | Grill over a lit FD stove; place raw skewer; WTHIT shows countdown (see BBQ-17) | "Ready to Flip" at half time |
| BBQ-05 | Sneak-flip, wait | cooked skewer at full time |
| BBQ-06 | Leave a skewer to 2x time | Burnt Skewer; no further block updates (B1, F3 debug / network) |
| BBQ-07 | Place grill on a campfire, light it, break it | unlit when placed; lit gives light 15 and heat; drops grill + campfire |
| BBQ-18 | Lit grill-campfire: water, splash water potion, shovel, rain (on stone, then on a coal block) | extinguished by the first three; out in rain on stone, stays lit on a coal block; relit by flint and steel |
| BBQ-19 | Tray: place, fill 3 slots, stack trays, break the support | storage works, unsupported tray breaks (for the evaluation row) |
| BBQ-08 | Grill with no heat after half cooking | progress falls back |
| BBQ-09 | (retired 2026-11-03 by the fold, see BBQ-22, BBQ-28) | - |
| BBQ-10 | Eat chili, buffalo, cumin, pepper seasoned skewers | 2 fire damage; 2 fire damage + amplifier +1; heal 2; fast eat |
| BBQ-11 | Barbeque sauce on beef skewer | Strength 3:00 |
| BBQ-12 | Eat a cooked skewer | stick returned |
| BBQ-13 | Kebab wrap (on bread), sandwich, bibimbap by the upstream combine recipes | contents tooltip; skewer effects applied on eating; sticks returned from crafting |
| BBQ-14 | JEI: Skewering and Grilling categories | pages present with times "each side"; no family mirror recipes |
| BBQ-15 | (replaced 2026-11-03 by BBQ-27: trades restored) | - |
| BBQ-16 | Compost a raw skewer, a cooked skewer, the sandwich, bibimbap; seasoning and tray | 25%, 50%, 75%, 100% chance; seasoning and tray not accepted; JEI/`/dumpcompost` lists 24 rows |
| BBQ-20 | Sauce on a raw skewer by craft; powder on a raw skewer | sauce refused, powder accepted (O28, by seasonable tag since 4.5) |
| BBQ-21 | Season a raw skewer with a powder, grill it | the cooked skewer keeps the seasoning and name prefix |
| BBQ-17 | WTHIT on the grill with a skewer on each slot | countdown per slot, Ready to Flip at half time; no Jade needed |
| BBQ-22 | Craft a cooked beef skewer with chili powder, then with honey mustard sauce; try a sauce on a raw skewer | one powder consumed, "Chili Flavored Beef Skewer", tooltip "Chili: edible when full, 1 heart of burn damage"; sauce craft on raw refused (4.5) |
| BBQ-23 | Chili skewer + pepper craft; set `[food] maxSeasonings = 1` and retry | refused (no pair defined); with onion + garlic popcorn at cap 1 the pair craft is refused |
| BBQ-24 | Grill a raw skewer seasoned with cumin | cooked skewer has Cumin and Grilled lines, AppleSkin +2 saturation; eating heals 1 heart |
| BBQ-25 | Smelt, smoke and campfire-cook a raw beef skewer; craft one from 3 cooked beef, 2 cabbage, stick | Beef Skewer without a Grilled line; does not stack with a grilled one |
| BBQ-26 | Kebab wrap from a grilled skewer | wrap's stored skewer has no Grilled line; a crafted (not grilled) skewer is accepted too |
| BBQ-27 | Butcher level 1 offers | BBQ seasonings at 12 emeralds, 64 powders or 16 sauces, 4 uses |
| BBQ-28 | Right-click a skewer on the grill or a cutting board with a seasoning | nothing happens (use retired); seasonings stack to 64 |
| BBQ-29 | Eat pepper, buffalo, barbeque sauce beef skewers | eaten fast; Strength II 0:45 and 1 heart burn; Strength 3:00 |
| BBQ-30 | Basin: 2 sweet potatoes, stick in main hand, 2 bacon in off hand; then 2 FD minced beef + 2 cabbage leaves | Raw Potato Skewer; Raw Beef Skewer |
| BBQ-31 | Sneak + right click a filled basin well holding a stick (and an off-hand item); then with empty hands | one item into the inventory, hands unchanged; empty-handed: one item into the hand |
| BBQ-32 | JEI Skewering page | off-hand shield icon left of the lower input slot; slots line up with their frames |
| BBQ-33 | JEI Grilling page result; Kebab Sandwich with two different cooked skewers | result tooltip has the Grilled line; sandwich crafts |

## 9. History

- 2026-10-30: investigation written (jar decompiled to `DecompiledMods/barbequesdelight-1.3.0/`), nothing built.
- 2026-10-31: owner decisions O1-O9 recorded (0.1): all skewers and the three combos added without the family fold,
  all six seasonings kept as upstream and marked for the pack framework (3.3.1 describes it), seasoning trades dropped,
  compost map written (3.7), no placeables, Jade grill tooltip to WTHIT. TODO rows `largecontent/mod-additions #37`,
  `dishes/meals #46` / `#47` (plus the skewer overlap evaluation table), `food/seasoning #11` / `#12`,
  `food/placeable #5`, `ui/wthit #13`. Still nothing built.
- 2026-10-31 (later): second batch O10-O19 recorded: plain Forge in Creative Delight, vanilla-rule grill campfire (3.4.1), tray for evaluation, renames kept, seasoning table (3.8), sandwich / bibimbap integration row, B5 kept, skewer tuning row, fold design (4.1) with questions Q1-Q5 (4.2). Nothing built.
- 2026-10-31 (third batch): skewer fold revised (BBQ system kept, other mods' skewers added to it, raw skewers stay as worse and sometimes poisonous food); `grilled` does not count toward the cap, configurable; catalog written in [seasonings.md](seasonings.md). Questions Q1-Q3 answered, Q6-Q7 new.
- 2026-10-31 (fourth batch): O20-O26 recorded (seasoning use, `grilled` only from the Grill, smelt note, Cooked names, raw pack skewers, +2 saturation, nasty only for bad meats). 4.2 answered, 4.3 lists 14 further decisions, 3.9 has the name table.
- 2026-10-31 (fifth batch): O27-O30 recorded (nasty default confirmed, sauces refused on raw skewers and seasonings copied to the cooked skewer, cooked-ingredient crafts follow shaped plus one assembly, grilled and crafted skewers split by seasoning). 4.3 #4, #5, #8, #9 answered. Tests BBQ-20, BBQ-21 added; the fold-stage tests (assembly craft, stack split) come with `dishes/meals #46`.
- 2026-11-01: port built (4.0): Creative Delight member `backportedBarbeques`, NCH emitters, 26 recipes, tags, dicts,
  compost, food types, family overrides, BlockRunner tiers, WTHIT and JEI. Scratch rows SQ-BBQ-01 to -08 queued. Not
  installed.
- 2026-11-02: owner decisions O31-O38 recorded (0.1): 64 / 16 seasoning conversion, raw meat follows raw chicken with no nasty type, raw skewers smeltable or smokable, new naming rule, skewers as ingredients carry no `grilled`, 4.3 and section 5 answered. Nothing built.
- 2026-11-02 (deep scan): member in `mods/` since the CD jar of 11-02 14:01; scratch r1102b: SQ-BBQ-01, -03 to -05 PASS, -02, -06, -07 OPEN (batch faults), -08 not run.
- 2026-11-03: fold built (4.5): BBQ seasonings in the pack framework (new kinds, cap config, crafts, click use retired), `grilled`, butcher trades restored, furnace / smoker / campfire routes, shaped cooked-ingredient craft, O34 names, skewer family membership. Compiled; installed in the CD jar 2026-11-03 00:08, not booted. Tests BBQ-22 to BBQ-29, scratch SQ-BBQF-01 to -07.
- 2026-11-04: skewer input tags (3.10) with sweet potato in `potato`; fixed `Item.of(tag, n)` collapsing every skewering
  input and the sandwich / bibimbap skewer to one item; basin sneak grab with items in hand; JEI off-hand icon and
  grilled result. Built and installed in the CD jar 2026-11-04. Tests BBQ-30 to BBQ-33, scratch SQ-BBQS-01 to -03.
