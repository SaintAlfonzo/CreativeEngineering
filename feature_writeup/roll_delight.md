# Roll Delight: overlap and integration report

Investigation written 2026-10-30 for TODO `largecontent/mod-additions #12` (the Roll Delight part). **BUILT 2026-11-01** (TODO
`largecontent/mod-additions #39`, build notes in section 4.2): member `backportedRoll 1.3.1-ce-19.2-01` nested in the CD jar, not
installed, nothing run in game.
Jar: `devmods/toBackport/[1.20.1] Roll Delight-1.3.1-forge-.jar` (file 1.3.1, `mods.toml` version 1.3.0), unpacked to
`DecompiledMods/roll_delight-1.3.1/`, code in `DecompiledMods/roll_delight-1.3.1-src/` (31 classes, MCreator). Forge
1.20.1 only, so a **port is needed**. Licence **All Rights Reserved**: the Vinery / Miner's / Rustic donor-jar route (jar
kept unmodified in `roll_upstream/`, our code written from behaviour). "Roll" in this file is Roll Delight. Test
prefix `ROLL-`.

**Owner rulings, 2026-10-31** (they override the older recommendations in sections 2 to 5 where they differ):

- **Rice Roll and Rice Roll Slice are kept.** They are not an overlap with FD Kelp Roll (D3 reversed: both are registered, B1 name fixed, Tobiko uses the Rice Roll Slice as upstream).
- **Salmon Maki Roll (and slice) is committed** (third round, D12; was held for evaluation). Renamed in round four (D4).
- **Mushroom Roll (and slice): added.**
- **Golden Roll (and slice): added, folded into the golden food system** (D7 settled; see 2.3).
- **Tobiko Roll: added.** Frogspawn input and source: see 2.4 (D9).

**Owner rulings, 2026-10-31 (second round; plans written into sections 2 to 5, 8):**

- **Ancient Roll is built with pack ingredients:** Ancient Fruit (Quark) in place of the pitcher plant, Chorus Flower in place
  of the torchflower. B2 is closed, D2 is settled, the Ancient Roll, its slice and the Mythical Roll Medley are in.
- **Nether Roll meat:** raw pork, raw bacon or Prepared Flesh (3.1).
- **Cooked Nether Wart, Legendary Roll Medley, Mythical Roll Medley and Chopsticks are implemented** (chopsticks redesigned, 3.3).
- **Medleys are built as the FD Rice Roll Medley is** (a `FeastBlock` subclass); rolls are still taken with an empty hand (3.2).
- **Chopsticks:** food goes through the normal eating path; durability 64; no effects of their own, they lengthen the effects
  of the eaten food; spend durability only on a food that was eaten; enchantable with Unbreaking and Mending; two-line tooltip.
- **Fix B9** and the other upstream bugs are applied (3.5).

**Owner rulings, 2026-10-31 (third round, decisions D1 and D11 to D16):**

- **D1: port Roll Delight.**
- **D11: no effects are added to the roll foods now.** A note is kept to add a variety of short effects to seafood later
  (TODO `food/types-and-tuning #20`). Until then chopsticks lengthen only what a food already has.
- **D12: the Salmon Maki Roll is committed** (the Legendary Medley needs its slice).
- **D13: the chopstick whitelist includes** cooked salmon / cod slices, cooked shrimp, calamari, spring rolls, egg rolls and
  noodle soups, on top of the 3.3 list.
- **Chopstick tooltip on every chopstick food** (3.3): `Can be eaten in the offhand w/ chopsticks`, or, when the food has
  effects, `Can be eaten in the offhand w/ chopsticks, lengthens effect length`.
- **D14: Cooked Nether Wart follows Cooked Rice** (4.1): type `veggie/cooked`, stack 64, returns a bowl, canned.
- **D15: a medley leaves its empty plate** at 0 servings.
- **D16: chopsticks repair on an anvil with any planks.**

**Scope rule:** integration adds no recipe and no mechanic that is not in Roll Delight or defined by a pack rule
([recipes_cooking.md](recipes_cooking.md), [food.md](food.md), [food_families.md](food_families.md)), unless the owner
asks for it by name. Upstream bugs are fixed (3.5).

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 14 items + 3 blocks (17 registry ids), no effects, no potions, 17 recipes, 3 block loot tables, 1 tag, no worldgen, no advancements | 1, 3 | read from jar and code |
| Upstream bugs | 3.5 | B1-B9, all with a fix planned (B2 closed by the Ancient Fruit / Chorus Flower recipe) |
| Overlap | 2 | Salmon Maki Roll (upstream "Amazing Salmon Roll") vs FD Salmon Roll (committed, D12); medleys vs FD Rice Roll Medley; Tobiko vs Oceanic Delight Fish Egg Roll. Rice Roll and slice are **not** an overlap (owner, 2026-10-31) |
| Fold-in | 4 | 19 steps planned |
| Decisions | 5 | D1-D17, all settled, D6 values confirmed (2026-11-01) |
| Build | 4.2 | BUILT 2026-11-01 (member + CD scripts); in `mods/` since the CD jar of 11-02 14:01, not played; `/dumpall`, can generator and `/cegen` wait on the game |
| Tests | 8 | ROLL-01..22 in game: none run. Scratch r1102b (2026-11-02): SQ-ROLL-01, -02, -05 to -09 PASS; SQ-ROLL-03, -04 OPEN (probe eats from the main hand only); CQ-ROLL-10 waits for the client round |

- **Jar:** 178 files; bundles `mixinextras-forge-0.5.0` (jarjar), unused by the code.
- **Licence:** ARR (`mods.toml`). No fork; donor jar for art and models, Java written from behaviour.
- **Dependencies:** declares only Minecraft 1.20.1. Needs Farmer's Delight at runtime (recipes use `farmersdelight:cooking`,
  `cutting`, `cooked_rice`, `salmon_slice`, `brown_mushroom_colony`; chopsticks code names 6 FD ids) but does not declare it.
- **Pack references:** none. No script, tag, dict or Display Delight entry names `roll_delight`. The dumps in
  `kubejs/exported/` (items 2026-10-24, names 10-20) cannot contain it; `/dumpall` is step 0 of the fold-in.

## 1. Overview

A small sushi add-on: five rolls that cut into three slices, two standalone rolls, two eight-serving "medley" blocks
that hand out rolls, a pot-cooked nether wart, and chopsticks that eat the off-hand food for a potion effect.

| Fact | Value |
|---|---|
| Mod id | `roll_delight` (package `net.mcreator.rolldelight`) |
| Items | 14 (11 foods, chopsticks, 2 medley block items) + wrapped chopsticks block item |
| Blocks | 3: `legendary_roll_medley`, `mythical_roll_medley` (`blockstate` 0-8, cake-like, wool sound), `wrapped_chopsticks` (`blockstate` 0-3, bamboo sound) |
| Effects / potions | none registered; chopsticks apply vanilla effects (3.3) |
| Recipes | 17: 11 shaped/shapeless crafting, 5 FD cutting, 1 FD cooking |
| Loot | 3 block tables (own blocks); no mob loot, no GLM |
| Tags | 1: `data/minecraft/tags/item/enchantable/durability.json` (1.21 path, dead on 1.20 and 1.19.2) |
| Advancements | none (the pack drops all advancements anyway) |
| Lang | `en_us` only |
| Tooltips | 1 (chopsticks) |
| Config | none |
| Compost | `ancient_roll`, `ancient_roll_slice` at 100 % (`RollDelightModCompostableItems`) |
| Events | chopsticks on FD Rice Roll Medley (`RightClickBlock`); shears break of frogspawn drops frogspawn (`BreakEvent`, class `TestProcedure` but live) |

## 2. Overlap (first)

### 2.1 Items

| Roll item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Rice Roll (3 cooked rice + 3 dried kelp) | FD Kelp Roll (rice, kelp, carrot) is a different recipe and filling | **Not an overlap** (owner): plain rice-and-kelp maki next to a carrot one | **keep** (D3 reversed); Tobiko takes Rice Roll Slice |
| Rice Roll Slice | none | mis-named upstream (B1) | **keep**, fix the name |
| Salmon Maki Roll / Salmon Maki | *overlaps w/ FD Salmon Roll* (7) and Rustic Rice Roll Royale | Same fish, rice and kelp; differs only in being a whole roll that is cut | **committed** (owner, D12); name D4 and values D6 still open |
| Mushroom Roll / Slice | none (first vegetarian maki) | cabbage + brown mushroom colony in a roll | **added** (owner) |
| Tobiko Roll | *overlaps w/ Oceanic Delight Fish Egg Roll / Slice* and Miner's Weird Caviar (fish roe) | Same idea (roe roll); Tobiko uses frog spawn, Oceanic uses fish eggs | **added** (owner); frogspawn source in 2.4 |
| Golden Roll / Slice | *overlaps w/ Golden Food* (golden carrot, golden cooked cod / mutton) | Made from 2 iron + 3 gold ingots + golden carrot; a gold food outside the pack's golden-food rules | **added, folded into the golden food system** (2.3) |
| Ancient Roll / Slice | none (pitcher plant, torchflower upstream) | Both upstream ingredients are 1.20-only (B2) | **added** (owner): Ancient Fruit + Chorus Flower (3.1) |
| Nether Roll | none (raw porkchop + cooked nether wart upstream) | | **added** (owner): raw pork, raw bacon or Prepared Flesh (3.1) |
| Cooked Nether Wart | Farmer's Respite Nether Wart Sourdough uses wart, no cooked form exists | bowl food | **added** (owner); family and stack in 4.1 |
| Legendary / Mythical Roll Medley | *overlaps w/ FD Rice Roll Medley* (`RiceRollMedleyBlock`, a `FeastBlock`, 8 servings, taken with an empty hand) | The FD medley already hands out rolls with no bowl, so the upstream behaviour is the FD behaviour | **added** (owner), built as the FD medley (3.2); D8 settled |
| Chopsticks, Wrapped Chopsticks | none in the pack (no chopsticks item in any mod dump) | new tool and decoration | **added** (owner), redesigned (3.3) |

### 2.2 Systems

- **Sushi family:** the pack's `roll` family ([food_families.md](food_families.md), `tools/food_families.py`) holds FD
  salmon/cod/kelp rolls, Rustic bell pepper, calamari and cherry blossom rolls, Ocean's fugu and elder guardian rolls,
  Hostile's guardian roll, Oceanic's egg, fish egg, sea pickle and shrimp rolls. Family rules: plated on Food Plate (whole)
  and Small Food Plate (slice), crafted from wrapper + filling, never canned.
- **Roll recipe rule** ([recipes_cooking.md](recipes_cooking.md) 2.3): FD salmon and cod roll have shapeless x2 and a
  sequenced assembly x3; kelp roll and rice roll medley have an assembly. Roll's rolls are crafting-only.
- **Feast Plating JEI tab** reads FD `FeastBlock`s; the Roll medleys are built as FeastBlocks (3.2), so they show.
- **Effects:** no new effect. Chopsticks give no effect of their own; they lengthen the eaten food's effects (3.3). Only 1 of
  the ~36 roll / sashimi foods in the 2026-10-24 food dump carries an effect (Glowing Calamari Roll, Glowing; Hawberry Roll
  is a fruit roll), so the lengthening has little to act on until the seafood-effects note (D11, TODO `food/types-and-tuning #20`) is done.
- **Loot, worldgen, crops, seasons, machines:** none.
- **Same batch** (`mod-additions #12`): Large Meals ([large_meals.md](large_meals.md)) shares no item; Mineral Delight
  ([mineral_delight.md](archive/mineral_delight.md)) has mineral foods next to the Golden Roll's ingots. **Carnivore**
  ([carnivore.md](archive/carnivore.md) D9) adds `carnivore:frogspawn` (proposed name "Frog Roe") from frogs, cut on the board
  into 2 placeable `minecraft:frogspawn`: a second survival source of the item Tobiko needs, and the same roe idea as
  Tobiko. If both ports go ahead, one frogspawn source is enough (D9).

### 2.3 Golden Roll in the golden food system

Settled by the owner: the Golden Roll is treated like the foods the Golden Food mod adds (golden soup, golden steak and
so on), not as a special recipe. The upstream recipe (2 iron + 3 gold ingots + golden carrot) is dropped.

- **Recipe:** `goldenGild` on the base food ([recipes_cooking.md](recipes_cooking.md) 2.4, `golden.js`): base roll surrounded by
  8 gold nuggets by hand, heated mixer base + 6, Arc Furnace base + 5. Base is the Rice Roll (the plain rice-and-kelp roll the
  Golden Roll is built on); the slice comes from cutting the Golden Roll as upstream. Rose gold counts as gold.
- **Type and tags:** food type `golden` (golden apple tier, always-edible allowed), item tag `golden`, values from the golden
  tier in [food.md](food.md), not the upstream 12 / 1.2. Same plating, never-canned and stack rules as the other golden foods.
- **No enchanted tier** unless the Golden Food rules create one for every golden food (`goldenEnchant`); nothing added for the roll.
- Chopsticks row (Night Vision) keeps the slice.

### 2.4 Tobiko Roll: frogspawn source

Question from the owner: does frogspawn need shears to drop, and what is the Carnivore source?

- **Shears: yes, as upstream.** The only survival source in the donor is the `BreakEvent` handler (class `TestProcedure`):
  breaking `minecraft:frogspawn` with shears in the main hand drops the frogspawn item; by hand it drops nothing (vanilla
  1.19.2 cannot pick frogspawn up in survival).
- **Shears source is kept** (D9). Nothing else is built for frogspawn.
- **Carnivore:** its Frog Roe (cut on the FD board into 2 placeable `minecraft:frogspawn`, [carnivore.md](archive/carnivore.md)
  D9) is a valid second source if Carnivore is ever added. Nothing needs to be done for it now: no recipe, no hook, no task.
- **Tobiko input is vanilla `minecraft:frogspawn`**, so any source that yields it works without changes here.
- The JEI info page documents the shears drop ([jei_coverage.md](jei_coverage.md)).


## 3. New content

### 3.1 Foods

Values: hunger / saturation modifier (saturation points = hunger x modifier x 2). Stack 64 unless noted.

| Item | Hunger / mod | From | Slices | Note |
|---|---|---|---|---|
| Rice Roll | 10 / 0.3 | 3 cooked rice + 3 dried kelp | 3 x 5 / 0.3 | kept (not an overlap) |
| Salmon Maki Roll | 15 / 0.6 | shaped: 4 salmon slice, 2 cooked rice, 3 dried kelp | 3 x 9 / 0.6 | **committed (D12)**; *overlaps w/ FD Salmon Roll* |
| Mushroom Roll | 14 / 0.6 | shaped: cabbage, 2 cooked rice, brown mushroom colony, 3 dried kelp | 3 x 8 / 0.6 | |
| Ancient Roll | 16 / 0.6 | shaped: **Ancient Fruit** (`quark:ancient_fruit`) where the pitcher plant was, 2 cooked rice, **Chorus Flower** where the torchflower was, 3 dried kelp | 3 x 10 / 0.6 | owner 2026-10-31; B2 closed. Both inputs are End / Quark-gated, so this is the late roll |
| Golden Roll | 12 / 1.2 | upstream: shaped 2 iron ingot, golden carrot, 3 gold ingot; **port: `goldenGild` on Rice Roll (2.3)** | 3 x 6 / 1.0 | folded into golden food |
| Nether Roll | 7 / 0.7 | shapeless: 2 meat + cooked nether wart -> 2, where meat is any raw pork (`#forge:raw_pork`: raw porkchop and FD raw bacon, both already in the tag), **or** Prepared Flesh (`jrftl:prepared_flesh`) | none | owner 2026-10-31; the three can mix in one craft. Prepared Flesh is not in the raw-pork tag, so the recipe takes a tag of its own (`roll_delight:nether_roll_meat` = `#forge:raw_pork` + Prepared Flesh) |
| Tobiko Roll | 7 / 0.6 | shapeless: frogspawn + 3 rice roll slice -> 3 | none | added; *overlaps w/ Oceanic Fish Egg Roll*; source 2.4 |
| Cooked Nether Wart | 6 / 0.3, stack 64, returns a bowl | Cooking Pot: nether wart (100 ticks), bowl returned as the item's remainder, as Cooked Rice | none | |

Slicing gives more food than the whole roll: three slices are worth 1.5x (Rice, Golden) to 1.9x (Ancient) the
whole roll's hunger. FD's kelp roll does the same at 1.5x (12 -> 3 x 6), so the ratio itself has precedent; the
absolute values do not (an upstream Amazing Salmon Roll Slice, 9, beats an FD Salmon Roll, 7). D6 (settled, 3.1.1).

#### 3.1.1 Value rule (D6, owner round four: input hunger vs output hunger)

Measured from the FD recipes and the 2026-10-24 food dump (hunger / saturation modifier):

| FD roll | Input hunger | Output hunger | Ratio |
|---|---|---|---|
| Kelp Roll (2 rice 12 + carrot 3 + 3 dried kelp 3) | 18 | whole 12 / 0.6; cut to 3 x 6 / 0.5 = 18 | whole 0.67 of input; **slices together = 1.0 of input**; each slice = input / 3, whole = 2/3 input, slice = whole / 2 |
| Salmon Roll, Cod Roll (2 slices 2 + rice 6 -> 2 rolls) | 8 | 2 x 7 = 14 | 1.75 (cheap raw slices, rice carries it) |

**Rule used:** for a roll that cuts into slices, whole = 2/3 of its input hunger and slice = 1/3 of it (the kelp roll
pattern: the slices add up to the input); saturation modifier 0.6 whole / 0.5 slice as the kelp roll. For a standalone
craft of already-cooked food, output total = input total (ratio 1.0), not the 1.75 of the raw-slice fish rolls.

| Item | Input hunger | Upstream | **Value (rounded)** |
|---|---|---|---|
| Rice Roll (3 rice 18 + 3 dried kelp 3) | 21 | 10 / 0.3, slices 5 | **14 / 0.6**, slices **7 / 0.5** |
| Salmon Maki Roll (4 salmon slice 4 + 2 rice 12 + 3 kelp 3) | 19 | 15 / 0.6, slices 9 | **13 / 0.6**, slices **6 / 0.5** |
| Mushroom Roll (cabbage 2 + 2 rice 12 + colony 0 + 3 kelp 3) | 17 | 14 / 0.6, slices 8 | **11 / 0.6**, slices **6 / 0.5** |
| Ancient Roll (ancient fruit 4 + 2 rice 12 + chorus flower 0 + 3 kelp 3) | 19 | 16 / 0.6, slices 10 | **13 / 0.6**, slices **6 / 0.5** |
| Golden Roll | golden tier ([food.md](food.md)), base Rice Roll | 12 / 1.2, slices 6 | golden tier copied from Golden Dried Kelp: **4 / 1.2**, always edible, Regeneration II 5 s, Absorption I 120 s, Nourishment 120 s, stack 16; slice **2 / 1.2**, always edible, **no effects** (built 2026-11-01; owner check: effects on the slice would be handed out 3 times per roll) |
| Tobiko Roll (3 Rice Roll Slice 21 + frogspawn 0 -> 3) | 21 | 7 / 0.6 | **7 / 0.6** (ratio 1.0: unchanged) |
| Nether Roll (2 porkchop 6 + cooked nether wart 6 -> 2) | 12 | 7 / 0.7 | **6 / 0.7** (bacon or Prepared Flesh inputs give less raw hunger; the roll stays 6) |
| Cooked Nether Wart | wart 0 + bowl | 6 / 0.3 | 6 / 0.3 (as Cooked Rice 6 / 0.4; saturation kept) |

Two consequences to know: the Rice Roll goes **up** (3 cooked rice is a heavy input), and the Ancient Roll comes out level with
the Salmon Maki Roll (its chorus flower adds no hunger). Both follow from the rule as stated; a rarity bonus for the Ancient
Roll would be a separate decision. The ratio check against the "Veggie Delight" rolls (owner, 2026-11-01) means Rustic Delight's bell pepper and cherry blossom
rolls (the Veggies port has no rolls). They are 2 slices + 1 Cooked Rice -> 2 rolls, the same craft as the FD Salmon and Cod
Rolls: bell pepper (slice 1) input 8, output 2 x 7 = 14 (1.75, same as FD); cherry blossom (petals, leaves and sapling carry no
hunger) input 6, output 2 x 6 = 12. So they already sit on the FD ratio and add no new rule: they are cheap-raw-slice rolls
like the fish rolls, while the Roll Delight rolls are cooked-food rolls, so the rule above (whole = 2/3 input, standalone
cooked = 1.0) stands unchanged.

### 3.2 Blocks

| Block | Recipe | Servings | Behaviour |
|---|---|---|---|
| Legendary Roll Medley (stack 1) | 3 mushroom roll slice, 3 salmon maki, 2 tobiko roll, bowl | 8: an empty hand takes 3 mushroom slices, 3 salmon slices, 2 tobiko, in that order | breaking part-eaten drops what is left plus the bowl |
| Mythical Roll Medley (stack 1) | 3 ancient roll slice, 3 golden roll slice, 2 nether roll, bowl | 8, same pattern | Ancient Roll is back (owner) |
| Wrapped Chopsticks (stack 16) | chopsticks, paper, red dye, yellow dye | holds up to 4 chopsticks; empty hand takes one out, wrapped chopsticks in hand adds one | decoration |

A medley returns exactly what went in (no food gained); it is storage and display.

**Build (owner 2026-10-31, D8 settled).** The Roll medleys are written as the FD Rice Roll Medley is: a `FeastBlock`
subclass with an 8-entry serving list (`riceRollServings` pattern: the serving item is picked by the remaining-servings
index) and an `IntegerProperty` of servings. FD's block takes its servings with an empty hand (its servings have no
container), which is the behaviour the owner wants kept, and being a `FeastBlock` it shows in the Feast Plating tab and
WTHIT with no extra work. Rules:

- **Taking:** empty hand or chopsticks only. Any other held item, and any held block item, returns `PASS` so the held block can
  be placed against the medley (**B9 fixed**; `SUCCESS` is returned only when a serving was handed out).
- **End state (D15 settled):** at 0 servings the FD leftover rule applies: the empty plate stays; breaking it drops the bowl.
- **Chopsticks on any medley** (the two Roll ones and the FD Rice Roll Medley): the serving is taken and eaten in place through
  the normal eating path (3.3), not added to the inventory.
- **Recipe:** crafting as upstream plus one sequenced assembly on the bowl, as the FD rice roll medley has
  ([recipes_cooking.md](recipes_cooking.md) 2.3).
- **Dependency:** the Legendary Medley needs Salmon Maki, which is now committed (D12).

### 3.3 Chopsticks (redesigned, owner 2026-10-31)

Upstream: durability 8, 32-tick bow-animation use, the off-hand food is removed, hunger and saturation are added directly,
and a fixed potion effect is applied from a 14-row table. **That design is replaced.**

| Property | Value |
|---|---|
| Durability | **64** (upstream 8), stack 1 |
| Own effects | **none**; the 14-row effect table is dropped |
| What they do | Hold in the main hand with a listed food in the off hand: the food is eaten normally and its **effects last longer** |
| Tooltip | two lines: `Lengthens effects of a food when eaten in offhand` / `Get the juices and sauces in your mouth, not on your hands` |
| Enchanting | Unbreaking and Mending (below); anvil repair with any planks (`#minecraft:planks`, D16) |

**Eating path (fixes B3, B4).** Chopsticks do not eat anything themselves and do not call `setFoodLevel` / `setSaturation`.
Their `use` returns `PASS` (no use animation of their own), so the off-hand food is started by vanilla as an ordinary
off-hand use: the food's own use time and animation, hunger gate, `FoodData.eat` (both caps), the item's `finishUsingItem`,
the `LivingEntityUseItemEvent.Finish` event, Spice of Life diversity, Diet-style trackers and the AppleSkin preview all see a
normal meal, because it is one. Chopsticks only hook that event: if the finished item is in the chopstick food tag (below) and
the player's main hand is chopsticks, then (1) every non-instant, non-ambient effect the player just received from that
food has its duration lengthened, (2) the chopsticks take 1 damage. Both happen only when a food was actually eaten (**B5**
fixed); a block, a tool, a non-listed food, an interrupted meal or a failed hunger gate costs nothing.

**Which foods (the "sushi / seafood" list).** A new item tag `creativeengineering:chopstick_foods`, built through `global.tags`
from an explicit id list (3.3.1; the `roll` family tag is not used as it holds foods that are excluded). Realistic chopstick foods found in the pack (ids from `item_dict.js` and the
2026-10-24 food dump):

| Group | Foods | Verdict |
|---|---|---|
| Sushi rolls and slices (the `roll` family) | FD Salmon Roll, Cod Roll, Kelp Roll Slice (+ the whole Kelp Roll); Rice, Mushroom, Ancient, Golden, Salmon Maki Roll slices and the whole rolls; Tobiko Roll; Nether Roll; Oceanic Caviar Roll / Slice, Sea Pickle Roll / Slice, Shrimp Roll; Rustic Calamari Roll, Cherry Blossom Roll; Ocean's Fugu Roll, Elder Guardian Roll; Hostile Guardian Roll | include |
| Sashimi | FD Salmon Slice, Cod Slice (raw cut fish); Hostile Guardian Sashimi | include |
| Rice | FD Cooked Rice (upstream had it) | include |
| Whole squid (owner, round four) | Miner's Delight Squid, Glow Squid, Baked Squid, Bowl of Stuffed Squid | include (3.3.1) |
| Other Asian-style seafood | Salmon Tempura (PneumatiCraft), FD Dumplings | include |
| Cooked Nether Wart | upstream had it | include |
| Whitelisted by the owner (D13) | cooked salmon / cod slices, cooked shrimp, every calamari / squid cut and calamari food, Rustic Spring Rolls, Oceanic Egg Roll, the noodle soups (Caviar Noodle Soup and the like) | include (ids resolved by name audit when the tag is built) |
| Not chopstick foods | Cabbage Rolls, Bat Rolls, sweet / chocolate rolls (Create Factory, Create), Ghast Roll, Lasagna, Gnocchi and Creamy Gnocchi, Acacia Blossom Soup, canned versions of any listed food | excluded; the tag is an explicit id list (3.3.1), **not** the `roll` family tag, because the family holds these |
| Dropped from upstream | Raw Porkchop (Nausea III): no realistic chopstick use and it only existed for the effect | removed |

#### 3.3.1 Resolved whitelist (owner round five, 2026-11-01)

The tag `creativeengineering:chopstick_foods` is this explicit list, built through `global.tags`, plus the Roll Delight foods and the
rows of the 3.3 table (Roll Delight rolls and slices, FD Salmon Slice / Cod Slice and their cooked slices, Salmon Tempura,
Guardian Roll / Sashimi, Ocean's Fugu Roll / Elder Guardian Roll, Kelp Roll, Cooked Nether Wart). Every id below was checked
against the 2026-10-24 food dump (84 of 84 after the round-six correction). Canned forms are not in the tag.

- **Rustic Delight (18, owner list) (18):** `rusticdelight:bell_pepper_roll_black`, `rusticdelight:bell_pepper_roll_blue`, `rusticdelight:bell_pepper_roll_green`, `rusticdelight:bell_pepper_roll_orange`, `rusticdelight:bell_pepper_roll_pink`, `rusticdelight:bell_pepper_roll_purple`, `rusticdelight:bell_pepper_roll_red`, `rusticdelight:bell_pepper_roll_white`, `rusticdelight:bell_pepper_roll_yellow`, `rusticdelight:cherry_blossom_roll`, `rusticdelight:calamari_roll`, `rusticdelight:calamari`, `rusticdelight:cooked_calamari`, `rusticdelight:calamari_slice`, `rusticdelight:cooked_calamari_slice`, `rusticdelight:spring_rolls`, `rusticdelight:fried_calamari`, `rusticdelight:calamari_soup`
- **Oceanic Delight (18):** `oceanic_delight:egg_roll`, `oceanic_delight:shrimp_roll`, `oceanic_delight:glow_squid_stew`, `oceanic_delight:squid_and_pickles`, `oceanic_delight:ancient_fish_eggs`, `oceanic_delight:plate_of_stuffed_pufferfish`, `oceanic_delight:sea_pickle_roll`, `oceanic_delight:sea_pickle_roll_slice`, `oceanic_delight:salmon_eggs`, `oceanic_delight:fish_egg_roll`, `oceanic_delight:fish_egg_roll_slice`, `oceanic_delight:sea_pickle_slices`, `oceanic_delight:fish_egg_noodle_soup`, `oceanic_delight:shrimp`, `oceanic_delight:shrimp_slices`, `oceanic_delight:fried_shrimp`, `oceanic_delight:elder_guardian_eye`, `oceanic_delight:pasta_with_eyeball`
- **Miner's Delight (6):** `miners_delight:glow_ink_pasta`, `miners_delight:bowl_of_stuffed_squid`, `miners_delight:takoyaki`, `miners_delight:squid`, `miners_delight:glow_squid`, `miners_delight:baked_squid`
- **Pack calamari line (3):** `creativeengineering:glowing_calamari`, `creativeengineering:glowing_calamari_slice`, `creativeengineering:glowing_calamari_roll`
- **Noodle and pasta sweep (owner) (25):** `abnormals_delight:duck_noodles`, `alexsdelight:kangaroo_pasta`, `alexsdelight:lobster_pasta`, `brewinandchewin:cheesy_pasta`, `endersdelight:pearl_pasta`, `farmersdelight:pasta_with_meatballs`, `farmersdelight:pasta_with_mutton_chop`, `farmersdelight:raw_pasta`, `farmersdelight:squid_ink_pasta`, `farmersdelight:vegetable_noodles`, `farmersdelight:noodle_soup`, `fungidelight:truffle_pasta`, `hostiledelight:glowpasta`, `miners_delight:noodle_soup_cup`, `miners_delight:pasta_with_veggieballs`, `moredelight:creamy_pasta_with_chicken_cuts`, `moredelight:creamy_pasta_with_ham`, `quarkdelight:crab_pasta`, `rusticdelight:bell_pepper_pasta`, `veggiesdelight:pasta_with_broccoli`, `vintagedelight:cheese_pasta`, `vintagedelight:cucumber_noodles`, `vintagedelight:pad_thai`, `ends_delight:ender_noodle`
- **Rice, FD and others (owner) (16):** `farmersdelight:kelp_roll`, `farmersdelight:kelp_roll_slice`, `farmersdelight:salmon_roll`, `farmersdelight:cod_roll`, `farmersdelight:dumplings`, `farmersdelight:cooked_rice`, `farmersdelight:fried_rice`, `veggiesdelight:garlic_rice_with_cauliflower`, `alexsmobs:shrimp_fried_rice`, `endersdelight:ender_paella`, `ends_delight:ender_congee`, `ends_delight:ender_bamboo_rice`, `ends_delight:stir_fried_shulker_meat`, `ends_delight:raw_dragon_meat_cuts`, `ends_delight:roasted_dragon_meat_cuts`

How the owner's wording was resolved (say if any is wrong):

- **"Cognee"** is Ender Congee (`ends_delight:ender_congee`). Ender Paella and Pearl Pasta have `_wood` variants registered upstream, but the pack removes them (`removed_items.js`), so only the plain ids are listed (round six corrected this).
- **"Egg fried rice"** has no item of that name; the closest is FD **Fried Rice** (made with egg), listed. **"Shrimp fried rice"** is Alex's Mobs Shrimp-Fried Rice.
- **"Caviar"** is `oceanic_delight:salmon_eggs` (renamed Caviar); **"Glowing calamari / slice / roll"** are the pack's own items.
- **"Cooked shrimp"** (round three) is Fried Shrimp; there is no other cooked shrimp item.
- **Noodle sweep:** every food with noodle or pasta in its id or name, minus Lasagna Slice, Horror Lasagna and Acacia Blossom Soup (the owner's exceptions) and minus Gnocchi / Creamy Gnocchi (the owner's "exclusively gnocchi" exception). Raw Pasta and Veggie Noodles (cucumber) count as raw noodles. Non-food noodle ingredients are not listed.
- **Not listed, so out unless added (candidates seen in the dump):** Bowl of Paella, Shrimp Rice, Shrimp Chips, Shrimp Stew (Oceanic); Pineapple Fried Rice; the Moredelight "Cooked Rice with ..." dishes; Squid Rings; Stuffed Squid (the plain Miner's item); Shulker Filet / Shulker Soup and the other shulker dishes; Dragon Meat (whole) and the other dragon dishes; Caviar Toast; Pasta with Cheese's siblings in other mods not in the sweep.

Effects of this on the tooltip rule: nothing changes; every id in the tag gets the line, with the longer wording if the item has a
lengthenable effect (Comfort on Cooked Rice, Glowing on the Glowing Calamari line, and so on). The tag is large (about 110 ids with the
Roll Delight and 3.3 table rows), so it lives in a generated file (`tools/` generator or a CD `chopstick_tags.js` list) and not inline in the recipe script.

#### 3.3.2 Unlisted candidates found by a sweep (2026-11-01, awaiting the owner)

Swept the 2026-10-24 food dump for fish, shellfish, squid, rice, noodle and roll-like foods not in 3.3.1 (canned forms ignored). Tiers
by how closely they match what the owner already listed. **None is in the tag until the owner says so.**

- **A. Same pattern as a listed food (cuts and slices of listed raw / cooked fish; siblings of listed dishes):**
  - Raw slices: `abnormals_delight:perch_slice`, `pike_slice`; `alexsdelight:raw_catfish_slice`; `hostiledelight:guardianfillet`; `oceansdelight:elder_guardian_slice`, `fugu_slice`, `cut_tentacles`, `tentacles`; `miners_delight:tentacles`; `oceanic_delight:squid_tentacles`, `glow_squid_tentacles`.
  - Cooked slices: `abnormals_delight:cooked_perch_slice`, `cooked_pike_slice`; `alexsdelight:cooked_catfish_slice`; `hostiledelight:cookedguardianfillet`; `oceansdelight:cooked_elder_guardian_slice`; `oceanic_delight:grilled_squid_tentacles`; `miners_delight:baked_tentacles`.
  - Rice and squid dishes: `oceanic_delight:shrimp_rice`, `bowl_of_paella`, `globular_rice`; `fruitsdelight:bowl_of_pineapple_fried_rice`; `farmersdelight:mushroom_rice`; `veggiesdelight:rice_and_vegetables`; `moredelight:cooked_rice_with_beef`, `_chicken_cuts`, `_porkchop`; `oceansdelight:squid_rings`; `miners_delight:stuffed_squid`; `oceanic_delight:squid_salad`.
  - Shulker cuts and dishes (next to the listed dragon cuts and stir-fried shulker): `ends_delight:shulker_meat`, `shulker_meat_slice`, `roasted_shulker_meat`, `roasted_shulker_meat_slice`, `grilled_shulker`, `shulker_soup`; `endersdelight:shulker_filet`, `shulker_mollusk`, `stuffed_shulker_bowl`.
- **B. Plausible, a judgement call:** `delightful:crab_rangoon` (a fried wonton), `ends_delight:stuffed_rice_cake`, `miners_delight:weird_caviar`, `oceanic_delight:caviar_toast`, `oceanic_delight:sea_grape`, `oceanic_delight:sea_salad`, `oceansdelight:braised_sea_pickle`, `honey_fried_kelp`, `minecraft:dried_kelp`; shellfish: `quark:crab_leg`, `cooked_crab_leg`, `cooked_whole_crab`, `alexsmobs:lobster_tail`, `cooked_lobster_tail`; plated fish: `farmersrespite:black_cod`, `farmersdelight:grilled_salmon`, `veggiesdelight:garlic_baked_cod`, `oceansdelight:stuffed_cod`, `cooked_stuffed_cod`, `hostiledelight:guardiantartar`, `oceansdelight:cabbage_wrapped_elder_guardian`; cured fish: `vintagedelight:salted_cod`, `salted_salmon`, `surstromming`, `brewinandchewin:kippers`.
- **C. Fish soups and stews** (owner whitelisted noodle soups, not stews): `farmersdelight:fish_stew`, `baked_cod_stew`; `miners_delight:fish_stew_cup`, `baked_cod_stew_cup`; `oceanic_delight:shrimp_stew`; `hostiledelight:guardiansoup`; `oceansdelight:bowl_of_guardian_soup`.
- **Not candidates:** whole raw or cooked fish (cod, salmon, perch, pike, lionfish, catfish, lobster-free whole catches), tentacle-on-a-stick items, squid / guardian sandwiches and burgers, shrimp chips, crab bars, fish and chips, golden fish, Cosmic Cod, jellies, sweet rolls, Rice Wine.

#### 3.3.3 Round six (owner, 2026-11-01): what is accepted from 3.3.2

**By rule (family tags, so ids that later leave the pack drop out by themselves):** the tag `creativeengineering:chopstick_foods`
includes the generated tags `#creativeengineering:food_family/fish/raw_cut` (any raw fish slice, including the overlapping guardian
slices, some of which will be removed), `#creativeengineering:food_family/fish/cooked_cut` (any cooked slice) and
`#creativeengineering:food_family/fish/raw` (any raw whole fish) and `#creativeengineering:food_family/fish/cooked` (any cooked whole fish, round seven). Currently 16 raw slices, 10 cooked slices and 22 raw whole items; the
raw tag also holds the raw shellfish, squid, tentacle and egg items (lobster tail, crab leg, calamari, shrimp, squid, glow squid,
Caviar, Ancient Fish Eggs, Elder Guardian Eye / Slab, Guardian Tail, Lionfish, Perch, Pike, Cod, Salmon, Tropical Fish, Blobfish, Flying
Fish, Shulker Mollusk). Enders Delight's Shulker Filet (a raw slice) and Shulker Mollusk (raw) come in through these tags: the owner's
"shulker meat (only Enders Delight)".

**By id (22, all checked against the dump):** `oceanic_delight:shrimp_rice`, `oceanic_delight:bowl_of_paella`, `oceanic_delight:globular_rice`, `fruitsdelight:bowl_of_pineapple_fried_rice`, `farmersdelight:mushroom_rice`, `veggiesdelight:rice_and_vegetables`, `moredelight:cooked_rice_with_beef`, `moredelight:cooked_rice_with_chicken_cuts`, `moredelight:cooked_rice_with_porkchop`, `oceansdelight:squid_rings`, `endersdelight:stuffed_shulker_bowl`, `delightful:crab_rangoon`, `ends_delight:stuffed_rice_cake`, `miners_delight:weird_caviar`, `oceanic_delight:sea_grape`, `oceansdelight:braised_sea_pickle`, `oceansdelight:honey_fried_kelp`, `minecraft:dried_kelp`, `quark:cooked_crab_leg`, `quarkdelight:cooked_whole_crab`, `alexsmobs:cooked_lobster_tail`, `oceanic_delight:shrimp_stew`.

Notes and flags:

- "Stuffed squid bowl" is the Bowl of Stuffed Squid, already in 3.3.1; the plain Stuffed Squid item stays out.
- **End's Delight (`ends_delight`) is to be removed (owner).** Every `ends_delight` id in the tag becomes dead then: Ender Noodle, Ender Congee, Ender Bamboo Rice, Stir Fried Shulker Meat, Raw / Roasted Dragon Meat Cuts (all 3.3.1) and **Stuffed Rice Cake** (the "rice cake", this round), and no `endersdelight` food is a rice cake or a dragon dish. The tag builder lists them as optional entries (ignored while the mod is absent) and the removal pass prunes them; the owner should say whether any of them gets a replacement in the pack (the noodle, congee and bamboo rice are the same kind of food as the pack's other rice and noodle dishes).
- **Still excluded:** `ends_delight` Shulker Meat and its cuts (the mod is going), the other shulker dishes, Squid Salad, Sea Salad, Caviar Toast, plated fish (Black Cod, Grilled Salmon, Garlic Baked Cod, the Stuffed Cods, Guardian Tartar, Cabbage-Wrapped Elder Guardian), cured fish (salted cod / salmon, surstromming, kippers), the other stews and Guardian Soups, the remaining candidates of 3.3.2.
- **Two gaps, settled (owner, round seven):** (1) **cooked whole fish** is in, through the tag `#creativeengineering:food_family/fish/cooked` (13 items today: Cooked Cod, Salmon, Perch, Pike, Catfish, Lionfish, Guardian Tail, Lobster Tail, Crab Leg, Calamari, Baked Squid, plus Kippers and Surstromming, which the rule brings in although 3.3.2 had listed cured fish as out; say if those two should be excluded); (2) **`minecraft:pufferfish`** is in by id (typed `hazard`, so no fish tag holds it). It keeps its poison; chopsticks only lengthen the effects it gives (Poison, Hunger, Nausea), which is a downside for the player and is noted in ROLL-19's checks.

**Lengthening.** Duration x 1.5 (built as `roll_delight-common.toml` `[chopsticks] lengthenFactor`, the member's own config, as the other CD members; D17) on each qualifying effect.
Instant effects (Saturation, Instant Health) and effects the player already had longer are left alone. The amplifier never
changes. Only 1 of ~36 roll and sashimi foods carries an effect today (2.2; Cooked Rice has Comfort, Glowing Calamari Roll has
Glowing), so chopsticks matter little until seafood has effects. **D11 (owner): no effects are added in this port.** A note is
kept (TODO `food/types-and-tuning #20`) to add a variety of short effects to seafood afterwards; the upstream table (Water
Breathing on Salmon Roll, Dolphin's Grace on Cod Roll, Speed on Kelp Roll Slice, Regeneration, Absorption, Night Vision, Fire
Resistance) is the starting list for that later pass.

**Tooltips on every chopstick food (owner).** Each item in `creativeengineering:chopstick_foods` gets a gray tooltip line from a
client tooltip handler in the CD member:

- food with at least one non-instant effect: `Can be eaten in the offhand w/ chopsticks, lengthens effect length`
- any other chopstick food: `Can be eaten in the offhand w/ chopsticks`

The choice reads the item's food properties at tooltip time, so it switches by itself when seafood later gains effects.
Lang keys, no hard-coded strings; no line on non-listed foods.

**Chopsticks on a medley.** Right-click a medley (the two Roll ones and the FD Rice Roll Medley) with chopsticks: one serving
is taken from the block and `finishUsingItem` is called on it (with the Finish event posted, so counts as a meal for Spice of
Life), subject to the hunger gate; the serving is not put in the inventory; the chopsticks take 1 damage and the lengthening
applies as above.

**Enchanting (Unbreaking, Mending).** 1.19.2 has no enchantable item tag (the upstream `enchantable/durability` file is the
1.21 mechanism, dead here; B8). On 1.19.2 Unbreaking and Mending use the BREAKABLE category, which accepts any stackable-1
damageable item, so the chopsticks are valid for both as soon as they have durability 64 and stack size 1. The enchanting table
also needs a non-zero enchantment value, so the item sets one (15). Verify in game that the anvil, the table and a Mending
orb all accept them (ROLL-16); if the pack's enchantment config (Quark / Apotheosis-style filters) blocks them, the fix is an
entry in that config, not a tag.

### 3.4 Other mechanics

- Breaking frogspawn with shears in main hand drops a frogspawn item (the only survival source for Tobiko's input).
- Compost: Ancient Roll and slice at 100 %.

### 3.5 Upstream bugs

Not seen in `logs/latest.log` (the owner has not played since the member went in); found in data and code.

| # | Bug | Evidence | Fix in port |
|---|---|---|---|
| B1 | Rice Roll Slice is named "Rice Roll" | `en_us.json` key `item.roll_delight.rice_roll_slice` | correct name (moot if D3 merges it) |
| B2 | Ancient Roll needs `minecraft:pitcher_plant` and `minecraft:torchflower`, which do not exist on 1.19.2; the Ancient Roll, its slice and the Mythical Medley become uncraftable | `ancient_roll_recipe.json`; no pack item dump has either id | **closed (owner)**: Ancient Fruit and Chorus Flower (3.1) |
| B3 | Chopsticks bypass `FoodData.eat`: hunger is not capped (19 + 10 = 29) and saturation is not capped at hunger, so repeated use stacks saturation without limit | `ChopsticksPlayerFinishesUsingItemProcedure` uses `setFoodLevel(get + n)` / `setSaturation(get + n)` | **fixed by design (3.3)**: the food is eaten by vanilla, so `FoodData.eat` caps both |
| B4 | The chopsticks path does not count as eating: no Spice of Life diversity, no Diet, no `LivingEntityUseItemEvent.Finish` for the food, no AppleSkin preview | same | **fixed by design (3.3)**: the off-hand food is used by vanilla; chopsticks only hook the Finish event |
| B5 | Chopsticks lose durability with any non-empty off-hand item, even one with no branch (whole rolls, Rice Roll and slice, any block) | durability `hurt` happens before the item checks | **fixed (3.3)**: durability spent only when a listed food finished |
| B6 | The tooltip promises "a roll, slice, or rice"; whole Roll rolls and Rice Roll / slice have no branch | tooltip vs branch list | **moot**: the tooltip is replaced (3.3) and the food list is the chopstick tag |
| B7 | Empty off-hand logs an empty INFO line on every use | `RollDelightMod.LOGGER.info("")` | **fixed**: no logging |
| B8 | Enchantability tag sits at the 1.21 path `tags/item/` | file path | **fixed (3.3)**: tag dropped; durability 64, stack 1 and enchantment value 15 make Unbreaking and Mending valid |
| B9 | Medley `use` returns `SUCCESS` for every right-click, so a held block cannot be placed against a medley | `MythicalRollMedleyBlock.use` | **fixed (3.2)**: `PASS` unless a serving was taken; held blocks never take one |

## 4. Fold-in, in build order

Code goes into a Creative Delight member (memory `creative-delight-routing`, Rustic precedent in
[rustic_delight.md](rustic_delight.md) section 5): `development/creativeDelight/members/roll` (Gradle `:backportedRoll`,
`bundled_in=creativeDelight`, mod id `roll_delight`), donor `roll_upstream/<jar>` read as hidden built-in packs
(`UpstreamJars` pin, `DonorJars`). KubeJS rows in `kubejs/data/creativedelight/kubejs/roll_delight/` (server) and
`development/creativeDelight/src/main/resources/kubejs_startup/roll_delight/` (startup).

0. **Port (Java):** register the ids kept by D3 (the Ancient Roll ids are back); foods as plain items with the values D6 fixes; Cooked Nether Wart
   as a bowl food (stack per 4.1); chopsticks (3.3) and wrapped chopsticks; medleys as `FeastBlock` subclasses (3.2); B1-B9 fixed.
   Donor translation: 1.20 data to 1.19.2 (the recipe `category` fields are ignored on 1.19.2; drop the 1.21 tag).
1. **Dumps and generators:** after install, `/dumpall`, then `/cegen` (memory `dumpall-refresh-workflow`).
2. **Ids:** every kept id in `item_dict.js` (and the block dict for the three blocks) before any script uses them.
3. **Tags** via `global.tags`: slices and rolls into the `roll` food type; `#forge:cooked_rice`-style inputs where the
   pack has one; Cooked Nether Wart into the vegetable / side food type.
4. **Upstream recipe removals and tag inputs:** Nether Roll meat -> tag `roll_delight:nether_roll_meat` (`#forge:raw_pork`, which
   already holds raw porkchop and FD raw bacon, plus `jrftl:prepared_flesh`); Ancient Roll takes `quark:ancient_fruit` and
   `minecraft:chorus_flower` through their dict keys; `forge:crops/nether_wart` and cabbage are tags already, FD ids (`salmon_slice`, `cooked_rice`,
   `brown_mushroom_colony`) through their dict keys. Rewrite every kept recipe through `global.NCH` under the CD
   `roll_delight` folder (the donor's recipe JSONs are not loaded).
5. **Mob loot:** none.
6. **Item merges:** none (Rice Roll and slice are kept, D3 reversed; Tobiko keeps the upstream Rice Roll Slice input).
7. **Potions:** none.
8. **Recipes by pack rules** ([recipes_cooking.md](recipes_cooking.md)): rolls follow the FD roll rule (shapeless kept,
   one sequenced assembly per roll on cooked rice, as salmon/kelp roll); medleys get an assembly on a bowl like the rice
   roll medley; cutting recipes kept; Cooked Nether Wart's pot recipe mirrored by the Mixer by the pack's existing
   mirror. Golden Roll through the golden food system (2.3, [recipes_cooking.md](recipes_cooking.md) 2.4). Salmon Maki Roll is committed (D12).
9. **Effects:** no new effect and no effect added to any roll food (D11). Chopsticks lengthening is a Finish-event hook with a config factor.
10. **Food types and values:** add to `food_types.json` (`roll`), values per D6 ([food.md](food.md)).
11. **Food families:** `python3 tools/food_families.py --check` after the types are in.
12. **Stack sizes** ([stack_sizes.md](stack_sizes.md)): rolls and slices by the handheld rule; medleys 1 (as upstream).
13. **Cans and jars:** rolls and slices none (handhelds are never canned, `roll` family). Cooked Nether Wart follows the
    cooked-vegetable can rule ([can_parity.md](can_parity.md) 2.6): a generated canned form (4.1).
14. **Compost:** CD `compost.js` values by the pack's compost framework (drop the 100 % entries). Chopsticks tag: the
    `creativeengineering:chopstick_foods` tag via `global.tags` (3.3 list).
15. **Names and tooltips:** B1; chopsticks two-line tooltip and the per-food chopstick tooltip (3.3, lang keys, no logging on any path); D4: display names "Salmon Maki Roll" and "Salmon Maki" (slice) in lang; registry ids keep the upstream
    `amazing_salmon_roll` / `amazing_salmon_roll_slice` so the donor art and models resolve; add a [names.md](names.md) row when built. JEI info page for the shears-on-frogspawn drop ([jei_coverage.md](jei_coverage.md)).
16. **Mob and block audit:** medleys and wrapped chopsticks: mineable tag (axe or none, cake-like), BlockRunner tier,
    Item Physic, WTHIT servings line. Chopsticks: enchantment value 15, Unbreaking and Mending check (ROLL-16).
17. **Placeables:** Food Plate / Small Food Plate per the `roll` family; Display Delight has no Roll Delight blocks.
18. **Worldgen, trades, chests:** none.
19. **Release:** `mod_sides.md` row (both sides), player changelog bullets under a "Roll Delight" family, TODO row,
    plan row.

### 4.1 Families and pattern check (owner 2026-10-31)

Each new food is folded in as the family it fits ([food_families.md](food_families.md) 3.1), then compared with the foods
already in that family. "Different" below means a pattern the family does not yet have, not just a different number.

| Food | Family / type | Same pattern as the family? |
|---|---|---|
| Rice, Mushroom, Ancient, Salmon Maki Roll, Golden Roll (+ slices) | `roll` (Golden also type `golden`, tag `golden`) | **Structure: same** (wrapper + filling, plated whole / slice, never canned, crafting + assembly). **Values: different.** Family today is hunger 4 to 15 (median 7), saturation 2.4 to 30 (median 8.4); upstream whole rolls are 10 to 16 hunger (Ancient 16 is above the family maximum) and each slice is as filling as a whole FD roll. D6 stands: scale to the FD roll |
| Tobiko Roll, Nether Roll | `roll` | **Same.** Standalone rolls with no slice (as Shrimp / Sea Pickle whole rolls); the shapeless x2 / x3 yield matches the FD cod / salmon roll craft. Nether Roll is the first `roll` with a land-meat filling that is not cooked, but raw meat appears in 26 roll recipes already |
| Cooked Nether Wart | `veggie/cooked`, exactly as Cooked Rice (D14) | **Same as Cooked Rice, the family's own bowl food:** type `cooked/veggie`, stack 64, returns a bowl, boiled in the Cooking Pot, generated can (x2 x 4 servings, 6 or 5 + salt, [can_parity.md](can_parity.md) 2.6; upstream had none), Display Delight placed / coaster. Hunger 6 / saturation 3.6 sits inside the type's range. Only new thing: the first fungus / Nether crop in the type, and no effect (Cooked Rice has Comfort; none added here). The earlier claim that no cooked vegetable returns a bowl was wrong |
| Legendary / Mythical Roll Medley | not a family; a feast block like the FD Rice Roll Medley (`food_blocks`, Feast Plating tab) | **Same as the FD medley:** 8 servings, rolls in a fixed order, taken by an empty hand. Differences: it is crafted from a bowl and the servings are mixed (3 + 3 + 2), as the FD medley mixes three kinds (3 + 2 + 3) |
| Chopsticks, Wrapped Chopsticks | not food | tool and decoration; no family |

Ordering: 0 before everything; 1 before 2-4 (ids must exist in the dump); 2 before 3, 4, 8, 10; 6 before 8; 10 before 11.

### 4.2 Build notes (2026-11-01)

- **Java** (`development/creativeDelight/members/roll`, package `com.SaintAlfonzo.creativedelight.roll`): `RollItems` (17 ids, D6 values),
  `RollBlocks`, `RollMedleyBlock` (FeastBlock, `servings` 0-8, main hand only: empty takes, chopsticks eat in place, anything else
  `PASS`), `WrappedChopsticksBlock` (`chopsticks` 1-4), `ChopsticksItem` (64, enchantment value 15, planks repair, two-line tooltip),
  `logic/Chopsticks` (Start snapshot of effect durations when the off-hand item is a chopstick food and the main hand chopsticks;
  Finish lengthens every effect that is new or longer, skips instant, ambient and Health Boost, 1 damage; FD Rice Roll Medley
  through `RightClickBlock`), `logic/FrogspawnShears`, `client/RollClient` (per-food tooltip), `RollConfig`.
- **Translator** (`upstream/RollTranslator`): upstream assets only; lang patched (B1, D4, tab name, our tooltip keys, upstream
  description dropped); blockstates re-keyed (`servings = 8 - blockstate`, `chopsticks = blockstate + 1`). Upstream data is not
  loaded at all; the data pack holds our three loot tables (medley: itself when full, else the bowl plus every serving left;
  wrapped chopsticks: one per pair).
- **Donor:** `roll_upstream/roll_delight-1.3.1-forge-1.20.1.jar` (upstream file renamed, bytes unchanged, sha1 `e23d4837...`);
  `build_deploy.py` ships the folder (`UPSTREAM_DONORS`, `roll_upstream`).
- **Scripts** (CD): `kubejs/data/creativedelight/kubejs/roll_delight/recipes.js` (all recipes via NCH: shaped / shapeless as upstream
  with the port inputs, one assembly per roll and medley, five cuttings, pot recipe, `goldenGild`; the Slice & Dice pot copy covers
  the Mixer), `tags.js` (`chopstick_foods` = 4 fish family tags + 122 ids, missing ids filtered and logged once;
  `nether_roll_meat`; medleys knife-mineable and BlockRunner confectionery as FD feasts; wrapped chopsticks axe), startup
  `roll_delight/food_values.js` (golden tier onto the Golden Roll), `compost.js` (rice / mushroom / ancient rolls 0.85, slices 0.5,
  as the FD Kelp Roll), client `roll_delight/jei_info.js` (frogspawn shears page). Dicts, `transition_items.js`, `food_types.json`,
  `gen_can_types.py` (Cooked Nether Wart can, waits on `/dumpall`), [names.md](names.md) 3.3, [can_parity.md](can_parity.md) 2.6.
- **Not built (open):** Display Delight plates and the Cooked Nether Wart placed / coaster look (need art and `display_blocks.json`
  rows in `backportedDisplay`); Item Physic and BlockRunner rows for the wrapped chopsticks. Waits on the game: `/dumpall`, then
  `python3 tools/gen_can_types.py` (empty its `AWAIT_DUMP`), then `/cegen` (stack sizes, placeables, food families).

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | ~~Port Roll Delight at all?~~ | **Settled 2026-10-31: port it** | build |
| D2 | ~~Ancient Roll needs 1.20-only pitcher plant and torchflower~~ | **Settled 2026-10-31: Ancient Fruit and Chorus Flower (3.1)** | build |
| D3 | ~~Rice Roll duplicates FD Kelp Roll~~ | **Settled 2026-10-31: not an overlap, register Rice Roll / slice** (fix B1 name) | keep |
| D4 | ~~"Amazing Salmon Roll" next to FD "Salmon Roll"~~ | **Settled 2026-10-31: renamed. Display names "Salmon Maki Roll" (whole) and "Salmon Maki" (slice); ids unchanged** | build |
| D5 | ~~Chopsticks~~ | **Settled 2026-10-31: redesigned (3.3)**: normal eating, durability 64, no own effects, lengthen the food's, two-line tooltip, Unbreaking and Mending | build |
| D6 | ~~Food values~~ | **Settled 2026-10-31: scale to the FD rolls by input hunger vs output hunger (3.1.1)**; checked against the Rustic bell pepper / cherry blossom rolls, which already match the FD ratio, no change (3.1.1) | build |
| D7 | ~~Golden Roll eats 3 gold + 2 iron ingots~~ | **Settled 2026-10-31: treated like Golden Food's added foods (soup, steak), via `goldenGild` (2.3)**, iron dropped | golden food |
| D8 | ~~Medleys hand out servings with no bowl~~ | **Settled 2026-10-31: built as the FD Rice Roll Medley (`FeastBlock` subclass), empty hand takes (3.2)** | build |
| D9 | Frogspawn from shears (Carnivore's Frog Roe cut on the board is a second source; **Settled 2026-10-31: keep the shears source; a Carnivore source is valid if added, nothing to do (2.4)**) | Keep the shears drop and add a JEI info page; if Carnivore is ported too, keep both (different routes: placed block vs frog kill) and let Tobiko take vanilla frogspawn only | keep |
| D10 | ~~Effects through chopsticks~~ | **Settled 2026-10-31: native chopstick effects removed** (3.3); effect levels belong to the seafood-effects note (D11) | done |
| D11 | ~~Roll foods carry no effects~~ | **Settled 2026-10-31: add none now; note kept** to add a variety of short effects to seafood later (TODO `food/types-and-tuning #20`) | note only |
| D12 | ~~Legendary Medley needs Salmon Maki~~ | **Settled 2026-10-31: commit the Salmon Maki Roll** | build |
| D13 | ~~Chopstick food list~~ | **Settled 2026-10-31: tempura, dumplings, cooked slices, cooked shrimp, calamari, spring rolls, egg rolls, noodle soups all whitelisted; tooltip on each (3.3)** | build |
| D14 | ~~Cooked Nether Wart stack and bowl~~ | **Settled 2026-10-31: follows Cooked Rice (stack 64, returns a bowl, canned; 4.1)** | build |
| D15 | ~~Medley end state~~ | **Settled 2026-10-31: the empty plate stays** | build |
| D16 | ~~Chopsticks anvil repair material~~ | **Settled 2026-10-31: any planks (`#minecraft:planks`)** | build |
| D17 | ~~Lengthening factor~~ | **Settled 2026-10-31: x1.5, configurable** | build |

Blocking a build: none. All of D1 to D17 are settled. No question remains on the value rule.

## 6. File map (planned)

| Path | What |
|---|---|
| `roll_upstream/roll_delight-1.3.1-forge-1.20.1.jar` | donor, unmodified (renamed), sha1-pinned |
| `development/creativeDelight/members/roll/` | Java (items, blocks, chopsticks, events), translator, NOTICE |
| `development/creativeDelight/src/main/resources/kubejs_startup/roll_delight/` | golden food values, compost |
| `kubejs/data/creativedelight/kubejs/roll_delight/tags.js`, `kubejs/assets/creativedelight/kubejs/roll_delight/jei_info.js` | tags, JEI info page |
| `kubejs/data/creativedelight/kubejs/roll_delight/recipes.js` | NCH recipes, assemblies, removals |
| `kubejs/startup_scripts/` `item_dict.js` | ids |
| `feature_writeup/roll_delight.md` | this file |

## 7. Offline checks (once built)

- `./gradlew :backportedRoll:build --offline` and `:creativeDelight:build`; build guard finds no ARR path in our jar.
- Headless harness: chopsticks finish with hunger 19 + Ancient-equivalent row ends at hunger 20 and saturation <= 20 (B3).
- `python3 tools/food_families.py --check`; `python3 tools/changelog_lint.py`.

## 8. In-game test suite (drafted)

Scratch r1102b (2026-11-02, `perf/runs/r1102b/s3/judged.md`): SQ-ROLL-01 (real PASS: the asset line is client-only,
the server loads `roll_upstream_data` clean), -02, -05 to -09 PASS: server half of ROLL-01, 05, 10, 11, 12, 16, 18, 22.
SQ-ROLL-03 and -04 (ROLL-07, 08, 09) OPEN: `zq useitem` tries the main hand only, chopsticks have no use of their own,
so nothing was eaten; rerun after the off-hand fallback in the probe.

| ID | Test | Pass |
|---|---|---|
| ROLL-01 | Boot with the CD jar | no `Failed to parse` naming `roll_delight` in `latest.log` |
| ROLL-02 | JEI search "roll" | every kept Roll item has a recipe; Rice Roll present (D3), no Ancient Roll (D2), Salmon Maki Roll present (D12) |
| ROLL-03 | Cut a Rice Roll and a Mushroom Roll on the board plus Salmon Maki Roll | 3 slices each, values per 3.1.1 |
| ROLL-04 | Craft Tobiko from frogspawn + 3 Rice Roll Slices | 3 Tobiko Rolls |
| ROLL-05 | Break frogspawn with shears, then with a hand | shears drop frogspawn; hand drops nothing |
| ROLL-06 | Cook nether wart in the Cooking Pot with a bowl | Cooked Nether Wart; no bowl, no output |
| ROLL-07 | Chopsticks main hand, Mushroom Roll Slice in off-hand, hunger 19 | slice eaten by the normal animation, hunger 20 (not 27), saturation <= 20, the food's effects last longer (D11), chopsticks lose 1 durability |
| ROLL-08 | Chopsticks with a dirt block, a non-listed food, or a listed food at full hunger in off-hand | no durability lost (B5), nothing eaten extra |
| ROLL-09 | Chopsticks eat 10 slices in a row from hunger 10 | saturation never exceeds hunger; Spice of Life counts the food |
| ROLL-10 | Place a Legendary Medley, take all servings | 3 + 3 + 2 items, then the bowl; block gone |
| ROLL-11 | Break a half-eaten medley | remaining items + bowl drop |
| ROLL-12 | Wrapped Chopsticks: add 3, take 4 | stacks to 4 visually; 4 chopsticks back, block gone |
| ROLL-13 | Golden Roll recipe in JEI and its food tags | `goldenGild` on Rice Roll (nugget ring, mixer, Arc Furnace), `golden` type and tag, no iron |
| ROLL-14 | Chopsticks on an FD Rice Roll Medley and on a Roll medley | one serving eaten in place through the normal path, same caps as ROLL-07, 1 durability |
| ROLL-15 | Nether Roll with raw porkchop, with raw bacon, with Prepared Flesh, and mixed | 2 Nether Rolls each time; JEI shows all three |
| ROLL-16 | Anvil and enchanting table with chopsticks | Unbreaking and Mending offered and applied; a Mending orb repairs them; durability 64 |
| ROLL-17 | Chopsticks tooltip; empty off-hand right-click | two lines as in 3.3; `latest.log` gets no empty INFO line (B7) |
| ROLL-18 | Medley with an empty hand, then with a held block and a held non-block | empty hand takes servings in order; the held block places against the medley (B9); no other held item takes one |
| ROLL-19 | Eat 5 different chopstick foods with chopsticks, then Spice of Life screen | each counts as eaten, as if by hand (diversity, AppleSkin preview, no extra hunger) |
| ROLL-20 | Ancient Roll in JEI and craft; Cooked Nether Wart can | recipe shows Ancient Fruit and Chorus Flower; the cooked wart can exists and opens to 4 servings |
| ROLL-21 | Hover a chopstick food without effects (Salmon Slice), one with effects (Glowing Calamari Roll, Cooked Rice), and a non-listed food | first: `Can be eaten in the offhand w/ chopsticks`; the effect foods: `..., lengthens effect length`; non-listed: no line |
| ROLL-22 | Anvil repair of damaged chopsticks with oak, spruce and a modded plank | each repairs |

## 9. History

- 2026-10-30: investigation written (jar inventory, decompiled code, overlap against the 10-24 item dump), nothing built.
- 2026-10-31: owner rulings recorded: Rice Roll kept (not an overlap), Amazing Salmon Roll held for evaluation, Mushroom, Golden (golden food system) and Tobiko added; frogspawn source defined (shears plus Carnivore Frog Roe).
- 2026-10-31: D9 settled (shears kept, Carnivore source optional, no work); Golden Roll handled like Golden Food's added foods (`goldenGild`).
- 2026-10-31 (second round): plans written in: Ancient Roll on Ancient Fruit + Chorus Flower (B2, D2 closed); Nether Roll meat tag; Cooked Nether Wart, both medleys (FeastBlock subclasses, D8) and redesigned chopsticks (3.3, D5) added; B1-B9 fixes recorded; families and pattern check (4.1); D11-D16 opened; tests ROLL-15..20 drafted.
- 2026-10-31 (third round): D1 port, D11 note only, D12 commit, D13 whitelist widened plus per-food tooltip, D14 Cooked Nether Wart as Cooked Rice, D15 empty plate, D16 planks; D10 parked, D17 (factor) opened; tests ROLL-21..22.
- 2026-10-31 (fourth round): whole squid (Miner's Squid, Glow Squid, Baked Squid) added to the chopstick whitelist; D4 executed (display names Salmon Maki Roll / Salmon Maki, ids unchanged); D6 settled to the FD input-vs-output rule (3.1.1; Vintage Delight has no rolls, question open); D17 x1.5 configurable; D10 closed.
- 2026-11-01 (fifth round): chopstick whitelist resolved to an explicit id list (3.3.1, 86 ids checked against the dump): 18 Rustic Delight foods, the Oceanic list, Miner's squid line, the pack's glowing calamari line, the noodle and pasta sweep (minus lasagna, gnocchi, acacia soup), rice dishes, dragon and shulker cuts; roll-family tag no longer used.
- 2026-11-01 (sixth round): chopstick tag now takes the fish family tags (raw, raw_cut, cooked_cut) plus 22 ids (3.3.3); `_wood` Ender Paella / Pearl Pasta ids dropped (the pack removes them); End's Delight removal flagged for 7 listed ids.
- 2026-11-01 (seventh round): cooked whole fish (family tag `fish/cooked`) and Pufferfish added to the chopstick tag (3.3.3); Kippers and Surstromming come in through the cooked tag.
- 2026-11-01: BUILT (4.2): `backportedRoll` member nested in CD, CD scripts, donor folder in build_deploy; Golden Roll on the golden tier, slice 2 / 1.2 without effects; lengthen factor in `roll_delight-common.toml`. Not installed; SQ-ROLL queued. Open: Display Delight plates, wrapped chopsticks Item Physic / BlockRunner, dump-driven generators.
- 2026-11-02 (deep scan): CD jar with the member in `mods/` since 11-02 14:01; scratch r1102b: SQ-ROLL-01, -02, -05 to -09 PASS, -03 / -04 OPEN (probe fault).
