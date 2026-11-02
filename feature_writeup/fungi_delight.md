# Fungi Delight: backport investigation and integration plan

> **Merge note (2026-10-25):** this port is to be merged with the other Farmer's Delight add-on backports into a single jar, **Creative Delight**. The inventory summary, cross-port interactions and the merge process are in [creative_delight.md](creative_delight.md). This file stays the detailed record for Fungi Delight; its registry namespace and test prefix do not change.

Investigation written 2026-10-23; **built 2026-10-23, deployed with 1.1.02 (2026-10-23); scratch: SQ-FGD-00 PASS, `/fgdtest all` 135 / 136 (2026-10-25, 2026-10-26; the FAIL is the truffle pie)** (§4.0; TODO `ports/fungiport #1` to `#3`). The owner's plan (2026-10-23) is §5.1 and overrides the §5
defaults where they differ (§5 lists the original questions with a recommendation and a default each). Fungi Delight (Eggmain, the Veggies Delight author: same `net.mehdinoui` code base, same Modrinth owner)
has no 1.19.2 build: Modrinth lists 1.0 to 1.3 for Forge 1.20.1 and 1.3 for NeoForge 1.21.1, with the same content.
Getting it into the pack means our own port, like the Veggies Delight port ([veggies_delight.md](veggies_delight.md))
and the Vintage Delight port ([vintage_delight.md](vintage_delight.md)), and then the same integration every other
crop and food has had.

"Fungi" in this file is Fungi Delight; "Veggies" and "Vintage" are the other two ports. Test prefix `FGD-` (`FD`
reads as Farmer's Delight).

**Scope rule (as for Vintage):** the port adds no recipe and no mechanic that is not in Fungi Delight or defined by a
food rule ([recipes_cooking.md](recipes_cooking.md), the `food/` briefs), unless the owner asks for it by name.
Upstream bugs are fixed (§2.9).

**Overlap marker:** every Fungi item that duplicates or closely mirrors something the pack has carries
`*overlaps w/ <what>*` in the tables (§3).

This file has three parts:

- **Part 1 (section 2):** everything in the mod, from the 1.20.1 jar and its decompiled code.
- **Part 2 (section 3):** the overlaps with the pack and the other two ports.
- **Part 3 (section 4):** the integration list in build order.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Mod inventory (2 mushrooms, 2 colonies, truffle and truffle dirt, 2 huge mushrooms, inky goo veil, 32 items, 17 blocks, 2 cow entities, 2 effects, 3 potions, 34 recipe files, pig / wolf / hoglin / sniffer hooks) | §2 | reference, from `FungiDelight-1.20.1-1.3.jar` |
| Overlap list | §3 | analysis; small (no other truffle, morel or inky cap in the pack) |
| Integration list in build order | §4, build record §4.0 | built 2026-10-23, deployed with 1.1.02; dump-driven steps open (TODO `ports/fungiport #2`) |
| Decisions for the owner | §5, answered in §5.1 | D1 to D12, each with a recommendation; owner plan 2026-10-23 |
| Tests | §8 | FGD-01 to FGD-40 not ticked; server side by `/fgdtest all` 135 / 136 (2026-10-26); open: SQ-FGD-12 to -15 (group B), CQ-FGD client rows in [testing/scratch_queue.md](testing/scratch_queue.md) |

- **Source to port from:** `~/.local/share/atlauncher/downloads/FungiDelight-1.20.1-1.3.jar` (Forge 1.20.1, closest
  API; downloaded 2026-10-23 from Modrinth, sha1 checked). 53 classes; decompile with
  `python3 tools/decompile_mod.py <jar> <out dir>`. `DecompiledMods/FungiDelight-1.21.1-1.3.jar` (NeoForge) is the same
  content: identical lang keys and recipes, except that it lacks the 4 Create / Botany Pots integration files.
- **Licence:** MIT in `mods.toml` and on Modrinth (source: github.com/MehdiNoui/FungiDelight). It fits a GPLv3 port;
  keep the MIT notice, as for Veggies.
- **Dependencies:** Forge and Farmer's Delight only (`farmersdelight [1.2.3,)`). FD 1.19.2-1.2.4 has every type it
  uses, checked with `javap`: `MushroomColonyBlock(Properties, Supplier<Item>)`, `MushroomColonyItem`,
  `ConsumableItem`, the `wild_crop` feature, the `farmersdelight:biome_tag` filter, `ModEffects` Comfort and
  Nourishment, the tags `MUSHROOM_COLONY_GROWABLE_ON`, `COMPOST_ACTIVATORS`, `UNAFFECTED_BY_RICH_SOIL`.
- **Nothing in the pack references it today.** The rows waiting on it are `dishes/ingredients #12` (the three ports)
  and `#9` (cheese), and **Fungi Delight has no cheese** (§2.4), so `#9` does not depend on it. Neither the Veggies
  nor the Vintage jar ships Fungi compat recipes.

## 1. Overview

A small content mod. The two new mushrooms (inky cap, morel) work like FD's brown and red ones: a placed mushroom, a
colony that grows on rich soil, a huge mushroom from bone meal, and a mooshroom-style cow that carries them. Truffles
are dug, not planted: truffle dirt generates inside rooted dirt blobs in taiga and dark forests. Pigs find truffles by
themselves now and then, and a tamed wolf fed a truffle sniffs out the nearest truffle dirt. Inky caps carry a
real-world joke (coprine): eating one gives Weak Stomach, and drinking alcohol or a potion while it lasts gives
nausea, poison and weakness. Truffles brew a Burrowing potion (faster shovel mining). 10 dishes, 5 crates.

## 2. Part 1: everything in the mod

### 2.1 Mushrooms and truffles

| Plant | Placed block | Colony | Huge form | Bone meal | Source in the world |
|---|---|---|---|---|---|
| Inky cap | `inky_cap_mushroom` (vanilla `MushroomBlock`, shape 6x10x6) | `inky_cap_mushroom_colony` (FD colony, cut into 5 caps) | Huge Inky Cap: stem 5 to 7, cap block, cap edge, Inky Goo Veil hanging 1 to 2 under the rim | grows the huge form (vanilla rules) | patches, colonies, huge form, the Inky Cap Cow |
| Morel | `morel_mushroom` (shape 6x13x6) | `morel_mushroom_colony` (5 morels) | Huge Morel: stem 9 to 12, layered cap | as above | as above, Morel Cow |
| Truffle | none (an item) | none | none | none | Truffle Dirt (1 to 2 truffles, Fortune; Silk Touch keeps the block), pig digging, wolf hunt, farmer trade |

- A placed inky cap or morel on `#farmersdelight:mushroom_colony_growable_on` (rich soil) turns into its colony on
  1 in 4 random ticks, as FD's do. Otherwise it spreads like a vanilla mushroom.
- **Inky Goo Veil** (`InkyGooVeilBlock`): hangs from a sturdy face or another veil, light level 5, honey sound, breaks
  instantly. When the block below the chain is air it adds a segment on 1 in 16 random ticks, and bone meal adds one
  every time: **no length cap**, so a veil left alone reaches the floor. Ships in `enchantment_power_transmitter`
  (1.20 tag; no effect on 1.19.2, where the bookshelf check is hard-coded), `replaceable`, `replaceable_by_trees` and
  `sword_efficient` (all 1.20 tags).
- Huge mushrooms are built in code (`CustomHugeMushroomFeature` and two subclasses), not from structure files; they
  need dirt below that is not rich soil. They use `BlockState.canBeReplaced()` (1.20), which becomes
  `getMaterial().isReplaceable()` on 1.19.2.
- Seasons: both mushrooms in Serene Seasons `year_round_crops` and `unbreakable_infertile_crops` (shipped tags).

### 2.2 Worldgen

All 14 biome modifiers are plain `forge:add_features` / `forge:add_spawns`. Rarity comes from a custom
`fungidelight:configurable_rarity_filter` that reads the config (1 in N chunks; 0 = off).

| Feature | Biomes (upstream) | Rarity | Pack biome words (`wild_crops.json`) |
|---|---|---|---|
| Inky cap patch (46 tries) | all overworld | 1 in 768 | everywhere |
| | `#forge:is_swamp`, birch forest, old growth birch | 1 in 64 | `swamp`, `birch` (7 biomes) |
| | mushroom fields | 1 in 16 | `mushroom` |
| Morel patch | all overworld | 1 in 384 | everywhere |
| | `#forge:is_coniferous`, dark forest | 1 in 32 | `coniferous`, `dark_oak` (23 biomes) |
| | mushroom fields | 1 in 12 | `mushroom` |
| Inky cap / morel colony (FD `wild_crop`, mycelium only) | mushroom fields | 1 in 15 each | `mushroom` |
| Huge inky cap / huge morel | mushroom fields | 1 in 3 / 1 in 4 | `mushroom` |
| Rooted dirt blob (size 20, replaces dirt, surface height) | `#minecraft:is_taiga`, dark forest | 1 in 3 | `coniferous`, `dark_oak` |
| Truffle dirt (size 6, replaces rooted dirt, 5 tries) | same | every chunk | same |
| Inky Cap Cow, Morel Cow spawns (weight 8 each, 3 to 4) | mushroom fields | | `mob_spawns.json` |

Patches need air and a block from `#fungidelight:morel_placeable_on` below (dirt, logs, mushroom grow blocks,
cobblestone, stone). Upstream bug: the inky cap patch also reads the morel tag, so `inky_cap_placeable_on`, the only
tag that includes sculk, is never used.

### 2.3 Animals and mechanics

| Mechanic | What it does upstream | 1.19.2 note |
|---|---|---|
| Inky Cap Cow, Morel Cow (`FDMCow extends MushroomCow`) | Mooshroom-style cow with its mushroom on its back. Shears: becomes a cow, drops 5 of its mushroom. Bowl: plain mushroom stew (inherited, red variant). Not changed by lightning. Breeds with any mooshroom (`MushroomCowMixin.canMate`); the calf takes the type of the parent whose breed goal spawns it (a Fungi parent gives its own type, a mooshroom parent a mooshroom). Two cows of the same type: 1 in 1024 calves is the other type. Tempted by wheat. Drops leather and beef as a cow. Spawn rule: vanilla mooshroom rule (mycelium below) | `MushroomCow` is the same shape in 1.19.2; the renderer layer reads `getMushroomBlockState()`. patcherMod's `MushroomCowWalkTargetMixin` covers subclasses |
| Pig food | Brown and red mushrooms, inky caps, morels, truffles added to `Pig.FOOD_ITEMS` (AT, `CompoundIngredient` wrap) | Veggies and Vintage wrap the same field; all three combine with the current value |
| Truffle breeding | A pig bred with a truffle is flagged; on birth 50 % of litters get extra piglets (40 % one, 10 % two) | `BabyEntitySpawnEvent` exists in 1.19.2 |
| Pig digging (`TruffleDiggingGoal`, goal 5) | An adult pig on the ground tries on 1 in 4000 ticks (`chancePigDigging` 2.5e-4). It targets the nearest podzol, mud, rooted dirt or truffle dirt with air above within 6 blocks, digs for 3 s, then: truffle dirt gives 1 to 3 truffles; anything else rolls `gameplay/pig_digging` (truffle 15, stick 35, bone meal 30, clay ball 20). **The block becomes coarse dirt either way, and `mobGriefing` is not checked.** Cooldown 2 min after a dig, 10 s after a give-up | The loot call uses 1.20's `LootDataManager` and `LootParams`; 1.19.2 has `getLootTables().get()` and `LootContext.Builder` |
| Wolf truffle hunt (`DogTruffleHuntGoal`, goal 6) | Right-click a tamed wolf with a truffle (the truffle is not used up): for 2 min it searches 12 blocks out and 1 to 4 below for `#fungidelight:truffle_ore`, walks to the surface over it, barks and scratches for 2 s, then stops. Mycelium particles while it hunts | Plain Forge events and goals |
| Hoglin food | Truffles breed hoglins (`HoglinMixin` on `isFood`, HEAD) | 1.19.2 `Hoglin.isFood` is the same (crimson fungus only) |
| Piglins | Truffle in `#minecraft:piglin_food` | same tag in 1.19.2 |
| Sniffer | Truffles tempt sniffers (`SnifferAiMixin`); `sniffer_food` tag; `enableSnifferTruffle` config | **1.20 only: no Sniffer in 1.19.2.** Dropped |
| Weak Stomach (harmful effect) | Eating anything in `#fungidelight:alcohol_reaction` gives Weak Stomach 1:00. While it lasts, finishing an item that is alcohol (`#forge:alcohol`), any non-water potion, or an id in the config list gives Nausea II, Poison II and Weakness II for 0:10. Red tooltip line "Causes Weak Stomach" on the reaction items | The upstream alcohol tag lists 78 ids from 7 mods, mostly absent; see §3.2 |
| Burrowing (beneficial effect) | Shovel-mineable blocks break 1.5x faster (level I) or 2x (level II), via `BreakSpeed`. Tooltip line on potions | |
| Burrowing potions | Awkward + truffle -> Burrowing 3:00; + redstone -> 8:00; + glowstone -> Burrowing II 1:30. Registered as Forge `BrewingRecipe`s, not `PotionBrewing` mixes | See §3.2: Create's mixer misses Forge brewing recipes |
| Trades | Farmer level 3: 4 inky cap colonies, 4 morel colonies, 4 brown colonies, 4 red colonies, 4 truffles, each for 1 emerald (the villager buys them). Wandering trader sells inky cap, morel, inky goo veil for 1 emerald | Java `VillagerTradesEvent` |
| Compost | 14 items put into `ComposterBlock.COMPOSTABLES` in common setup (0.5 to 1.0) | The pack's compost engine clears that table (§4 step 13) |
| Sounds | 6 events: pig happy (own ogg), pig dig, pig drop, 3 wolf sounds (vanilla sounds re-used) | |

### 2.4 Items and blocks (32 items, 17 blocks)

| Group | Items |
|---|---|
| Ingredients | Cleaned Inky Cap, Cooked Inky Cap, Truffle, Truffle Slice |
| Dishes (bowl, stack 16) | Inky Caps Salad, Creamy Morel Soup, Truffle Ice Cream, Inky Cap Scrambled Eggs, Truffle Pasta, Stuffed Morels, Steak With Mushrooms, Mutton Chops with Truffle, Rabbit with Morels, Pork Marsala With Mushrooms |
| Plants | Inky Cap Mushroom, Morel Mushroom, Inky Cap Mushroom Colony, Morel Mushroom Colony |
| Huge mushroom blocks | Inky Cap Mushroom Block, Edge, Stem; Morel Mushroom Block, Stem; Inky Goo Veil |
| Crates (3x3, 9 back) | Brown Mushroom Crate, Red Mushroom Crate, Inky Cap Mushroom Crate, Morel Mushroom Crate, Truffle Crate |
| Other | Truffle Dirt; Inky Cap Cow and Morel Cow spawn eggs |

No cheese, no drink, no fluid, no placeable feast block.

### 2.5 Food values (nutrition / saturation modifier, effects)

| Food | Values | Effect |
|---|---|---|
| Cleaned Inky Cap | 4 / 0.4 | (Weak Stomach on eating) |
| Cooked Inky Cap | 6 / 0.6 | (Weak Stomach) |
| Truffle | 6 / 0.6 | |
| Truffle Slice | 2 / 0.2 | |
| Inky Caps Salad | 8 / 0.75 | Luck 0:30 (Weak Stomach) |
| Creamy Morel Soup | 10 / 0.7 | Comfort 0:30 |
| Truffle Ice Cream | 7 / 0.5 | Haste 0:10 |
| Inky Cap Scrambled Eggs | 10 / 0.9 | Nourishment 3:00 (Weak Stomach) |
| Truffle Pasta, Stuffed Morels, Steak With Mushrooms | 12 / 0.8 | Nourishment 3:00 |
| Mutton Chops with Truffle, Rabbit with Morels, Pork Marsala With Mushrooms | 14 / 0.75 | Nourishment 5:00 |

The raw inky cap and morel are not food. The pack tunes against its type bands, not these numbers (§4 step 7).

### 2.6 Recipes (34 files)

| Kind | Recipes |
|---|---|
| Cooking pot (bowl) | Creamy Morel Soup (2 morel, milk, onion); Mutton Chops with Truffle (raw mutton, truffle, potato, onion); Pork Marsala (raw pork, onion, 2 red mushroom, tomato sauce); Rabbit with Morels (rabbit, 2 morel, carrot, onion); Steak With Mushrooms (beef, brown mushroom, onion, milk); Stuffed Morels (2 morel, rice, tomato, cooked chicken); Truffle Pasta (pasta, truffle, milk, onion) |
| Cutting board | inky cap -> cleaned cap + ink sac + 20 % ink sac; truffle -> 3 slices; each colony -> 5 of its mushroom |
| Grid | Inky Caps Salad (salad ingredient, cleaned cap, bowl); Inky Cap Scrambled Eggs (2 cooked eggs, cooked cap, bowl); Truffle Ice Cream (slice, snowball, sugar, milk bucket, bowl); 3 suspicious stews (bowl, brown, red, and an inky cap / morel / truffle); 5 crates and 5 uncrafts |
| Furnace, smoker, campfire | cleaned cap -> cooked cap |
| Create milling (gated) | inky cap -> 1 black dye + 50 % 1; colony -> 5 + 50 % 7 |
| Botany Pots (gated) | 2 colony crops; mod not in the pack |

Every input tag resolves in the pack today (`#forge:pasta`, `#forge:salad_ingredients`, `#forge:cooked_eggs`,
`#forge:raw_mutton`, `#forge:raw_pork`, `#forge:cooked_chicken`, `#forge:milk`, `#forge:crops/onion`,
`#forge:crops/tomato`, `#forge:grain/rice`, `#forge:vegetables/potato`, `#forge:vegetables/carrot`,
`#forge:tools/knives`; checked in `kubejs/exported/tags/item_tags.json`).

### 2.7 Tags, loot, advancements

- Tags: `#forge:mushrooms` gains `#forge:mushrooms/inky_cap` (inky cap, **cleaned cap**), `/morel`, `/truffle` (truffle,
  **truffle slice**); `#forge:storage_blocks/*` for the 5 crates; truffle dirt in `#minecraft:dirt` and shovel
  mineable; mushrooms and colonies in FD compost activators; colonies unaffected by rich soil; FD `meals` holds 8 of the
  dishes; Create Crafts & Additions `plants`; Autumnity `snail_snacks` and Spawn `snail_feeds` (mods not in the pack).
- Loot: 16 block tables, both cows (cow drops), `gameplay/pig_digging`. No chest loot, no GLM.
- Advancements: 23 (the pack drops all advancements at load).

### 2.8 Config (`fungidelight-common.toml`)

Switches for pig foods, pig digging (and its chance), truffle twins, sniffer, wolf hunt, both effects, the Weak
Stomach item list, farmer trades, wandering trader trades, and 12 rarity values for the features in §2.2.

### 2.9 Upstream bugs found

| Bug | Fix in the port |
|---|---|
| The 3 suspicious stew recipes store `EffectId:35`. 1.19.2 vanilla numbers stop at 33 (darkness); 35 is whatever modded effect registered at that slot, so in this pack it is an arbitrary effect | Drop the recipes, or name a vanilla effect; owner call (D10) |
| Inky cap patches read `morel_placeable_on` | Point at `inky_cap_placeable_on` |
| The access transformer opens `Chicken.FOOD_ITEMS`, `Villager.WANTED_ITEMS`, `Parrot.TAME_FOOD` and the structure pool fields, all unused (copied from Veggies) | Keep only `Pig.FOOD_ITEMS` |
| The 1.21 `neoforge.mods.toml` declares the FD dependency under `dependencies.veggiesdelight` | n/a (we port from 1.20.1) |
| Pigs turn podzol, mud and rooted dirt into coarse dirt without checking `mobGriefing` | D4 |
| Inky Goo Veil grows without a length cap | D5 |

## 3. Part 2: overlap with what the pack already has

The pack has no truffle, morel or inky cap item (checked in `display_names.json`: 94 mushroom, truffle and fungus ids,
none from these). The overlaps are dish families and systems.

### 3.1 Item overlaps

| Fungi item | Overlap |
|---|---|
| Truffle | *overlaps w/ Create Central Kitchen's Truffle Pie* (`create_central_kitchen:truffle_pie`, slice, incomplete pie): the items are registered, but every recipe is gated on Environmental, which is not in the pack, so the pie is a dead item today (0 truffle recipes in `recipes/all.csv`). A pie recipe from Fungi's truffle would be a new recipe (D11) |
| Truffle Ice Cream | *overlaps w/ Neapolitan's ice creams and Delightful's matcha / salmonberry ice creams* (same bowl dessert family; Neapolitan has ice cream blocks, Fungi does not) |
| Inky Cap Scrambled Eggs | *overlaps w/ Incubation's Scrambled Eggs* |
| Creamy Morel Soup | mirrors Brewin's Creamy Onion Soup (different dish, same shape) |
| Steak With Mushrooms | sits next to FD's Steak and Potatoes (different dish) |
| Brown / Red Mushroom Crate | no mushroom storage block exists in the pack; goes through the storage audit ([storage_audit.md](storage_audit.md)) |
| Suspicious stew recipes | the vanilla suspicious stew from flowers; these three give a fixed effect (and the wrong one, §2.9) |

### 3.2 System overlaps

| System | Overlap | Proposal |
|---|---|---|
| Alcohol and Weak Stomach | The shipped `#forge:alcohol` lists 78 ids from alcocraftplus, brewery, create_winery, okaypy (absent), brewinandchewin (14) and vinery (26). The pack's own list is `#creativeengineering:food_type/drink/alcohol` (43 items, from the Brewin' and Vinery ports and the rest) | Point Weak Stomach at the pack tag (D2). It sits beside Brewin's Tipsy / Intoxication rework ([alcohol.md](alcohol.md)) |
| Weak Stomach on durian | `#fungidelight:alcohol_reaction` also lists 5 Fruits Delight durian items (in the pack) and `fruitsdelight:buried_truffle` (not in 0.5.11) | Keep as shipped (real ids, optional entry) |
| Potions | Burrowing registers through Forge's `BrewingRecipeRegistry`. Create's Automatic Brewing (and the pack's late rebuild of it, memory `create-automatic-brewing`) reads only `PotionBrewing` mixes, so Burrowing would brew in a stand but not in a mixer | Register the three as `PotionBrewing` mixes in the port (D3); then the bottling, IE and lingering routes pick them up as for any potion |
| Effect text | Both effects need JEED descriptions and rows in [potions.md](potions.md); the mod's own tooltip lines ("Causes Weak Stomach", "+50% Mining Speed on soil") sit beside the pack's tooltip code ([tooltips.md](tooltips.md)) | D8 |
| Mob food | Pig food edit (5 items) and wolf / hoglin / piglin truffle use, on top of the Veggies and Vintage edits | Accept, fold into `tweaks #13` (mob food overhaul) as Vintage S12 did |
| Mooshroom variants | The pack's own More Mooblooms adds 11 cow variants with a flower pool; Fungi adds 2 mushroom cows through a separate entity class | Keep Fungi's two entities under their own ids (no conversion into More Mooblooms) |
| Spawns | Fungi cows spawn in mushroom fields at weight 8 each next to the mooshroom (8): two thirds of mushroom cows become Fungi cows. The pack spawns mooshrooms in dark forests (`mooshroom_dark_forests` rule) | `mob_spawns.json` rules with `disable_modifiers` (D7) |
| Wild plants | FD's colonies are already in `wild_crops.json` (mushroom fields, dark forest) | Fungi's patches and colonies go in the same file (D6) |
| `#forge:mushrooms` | Adding truffles, truffle slices and cleaned caps makes them valid wherever a recipe takes any mushroom | Check the users of the tag after `/dumpall` (step 2) |
| Compost | Fungi writes the compost table directly; the pack's engine rebuilds it from its own rules and values | Drop the hook, add the rows through `gen_compost_values.py --add-new` (step 13) |
| Trades | Java trades | Drop them, re-add through the pack's trade tools if wanted, as for Veggies (step 14) |

### 3.3 Overlap with the Veggies and Vintage ports

- Same author and code base as Veggies (package layout, `ModMobsEvents`-style food edits, `configurable_rarity_filter`,
  the same AT lines). The Veggies port's choices apply directly: no config, rarity in placed-feature json, no trades or
  compost hook, advancements dropped.
- All three ports wrap `Pig.FOOD_ITEMS`; each combines with the current value, so order does not matter.
- No recipe in either jar names Fungi items, and Fungi names neither.

## 4. Part 3: integration list in build order

Steps that match the Veggies or Vintage ports are only named; see [veggies_delight.md](veggies_delight.md) §4 and
[vintage_delight.md](vintage_delight.md) §4 for the detail.

1. **Port (new jar).** `development/creativeDelight/members/fungi`, mod id `fungidelight` (ids unchanged), version
   `1.3-ce-19.2-01`, root `settings.gradle`, `installToMods`, `mod_sides.md` row (both sides: entities and a client
   renderer). Resources through a copy of `development/creativeDelight/members/veggies/tools/port_resources.py`, MIT notice kept. 1.20.1 -> 1.19.2: block `Material`,
   `CreativeModeTab` subclass, recipe JSON without `category`, `MushroomBlock(Properties, Supplier<Holder<...>>)`,
   `canBeReplaced()` -> material check, 1.19.2 loot table call in the pig goal, placement modifier codec, the
   1.20-only block tags left out. **Dropped:** `SnifferAiMixin`, the `sniffer_food` tag and the sniffer config;
   `Configuration` and `ConfigurableRarityFilter` (rarity written into the placed features); the Java trades; the
   compost hook; the Botany Pots recipes; the advancements (D1); the unused AT lines. **Kept in code:** the cows and
   `MushroomCowMixin`, `HoglinMixin`, pig food and truffle twins, pig digging (D4), wolf hunt, both effects, Inky Goo
   Veil (D5). Fixes from §2.9.
2. **Ids and tags in one place.** Every item in `item_dict.js`, blocks in the block dict; `#forge:mushrooms/*` and
   storage block tags as shipped; check the `#forge:mushrooms` users.
3. **Mushroom parity.** Colonies like FD's (rich soil growth, cutting board, `#farmersdelight:mushroom_colony_growable_on`);
   rows in [crops.md](crops.md); Quark and hoe-area behaviour as for FD colonies; WTHIT growth line for colonies.
4. **Wild placement.** Fungi's 12 feature modifiers off through `disable_modifiers`; patches and colonies in
   `wild_crops.json` with the biome words in §2.2; huge mushrooms in `mushroom`; rooted dirt and truffle dirt in
   `coniferous` and `dark_oak`. `wild_crops.js` only places in `vegetal_decoration`, so it gains a per-rule `step`
   (default `vegetal_decoration`) for the two `underground_ores` features: extend the tool, no separate modifier.
   `gen_soil_compat.py`, regenerate [crops_by_biome.md](crops_by_biome.md), `tools/test_wild_crops.sh`.
5. **Spawns.** Inky Cap Cow and Morel Cow rules in `mob_spawns.json` with Fungi's 2 spawn modifiers disabled (D7).
6. **Seasons.** Ship the year-round tags; the overhaul stays in `farming/crops #5`.
7. **Food types and values.** `/dumpfood`, `food_catalog.py`; tune against the pack's type bands.
8. **Cans.** `gen_can_types.py`: mushroom, truffle and the 10 dishes by type.
9. **Cooking rules.** The grid meals (salad, scrambled eggs, ice cream) take cooked or prepared inputs: heatless rule
   and `_manual_only` ids where the rule says; pot meals get their machine routes by the pack rules; ice cream in the
   ice cream family next to Neapolitan's. Everything through `global.NCH`.
10. **Potions and effects.** Burrowing as `PotionBrewing` mixes (D3); check against `PotionMixDedup` and the Create /
    IE mixers; Burrowing and Weak Stomach in [potions.md](potions.md), JEED text, effect export.
11. **Weak Stomach.** Alcohol tag -> pack tag (D2); decide the "any potion" trigger (D2); config item list empty.
12. **Placeables and stack sizes.** Dishes at 16 by the stack size policy; `/dumpplaceable` (none expected).
13. **Compost.** `gen_compost_values.py --add-new` after the first dump; upstream values as the starting point
    (truffle, colonies 1.0; huge blocks 0.85; mushrooms, stems, veil, cooked cap 0.65; cleaned cap, slice 0.5).
14. **Trades.** Farmer and wandering trader rows through the pack's trade tools, or none (D9).
15. **Loot.** Ship the block, cow and pig digging tables. **Crops loot tables TODO** (`vintageport #5`): add truffle,
    inky cap and morel as single items and as crates.
16. **Machines.** Create milling to black dye as shipped.
17. **Mob audit.** Pig, wolf, hoglin, piglin food against [mob_block_tweaks.md](mob_block_tweaks.md) and
    [baby_mobs.md](baby_mobs.md) (calf model for the Fungi cows); `tweaks #13` row.
18. **UI.** Names audit ("Steak With Mushrooms" and "Pork Marsala With Mushrooms" to "with", "Inky Caps Salad",
    "Cleaned Inky Cap"), JEI info pages for pig digging, wolf hunt and Weak Stomach, JEI coverage, BlockRunner tiers
    for the huge mushroom blocks, WTHIT icons.
19. **Release.** `/dumpall` and the generators in order, changelog bullets under the farming and food headings,
    `build_deploy.py` `IN_TREE_MODS`, a `plan_*.md` row.

## 4.0 Build record (2026-10-23)

Built in one session from §5.1. Installed and deployed with 1.1.02 (2026-10-23). **Hard dependency:** the pack's placed
features in `kubejs/data` reference `fungidelight:*` configured features and blocks, so a world without the port jar
fails to load. Game closed: `./gradlew :backportedFungi:installToMods :patcherMod:installToMods --offline`.

| Part | Where | Notes |
|---|---|---|
| Port jar `backportedFungi-1.3-ce-19.2-01` | `development/creativeDelight/members/fungi` (README lists every deviation) | No config (defaults hard-coded), no sniffer, advancements, Botany Pots, compost hook, Autumnity / Spawn tags, `has_*` biome tags, `#forge:alcohol` file; no biome modifiers or placed features (8 configured features ship). Fixes: inky patch on `inky_cap_placeable_on`; pig digging asks `getMobGriefingEvent` before digging and before changing the block (loot table call on the CHEST parameter set); veil capped at 16 (bone meal refused at the cap); Weak Stomach reads `#creativeengineering:food_type/drink/alcohol`; Burrowing as `PotionBrewing.addMix` (AT); stews as `fungidelight:effect_stew` (Luck / Comfort / Haste, 160 ticks; upstream's numeric ids were Blindness, Saturation and 35). `MushroomCowMixin` deleted, `FDMCow` keeps lightning immunity, shearing, renderer. Trades and sounds kept |
| Mooshroom family | patcherMod `mooshroom/MooshroomFamily`, `MushroomTypeSetter`, mixins `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin`; `SpawnTuning` `brown_chance` | Brown on NATURAL / CHUNK_GENERATION `SpecialSpawn` of `minecraft:mooshroom` by the biome's `brown_chance`; any two of red, brown, inky cap cow, morel cow breed, calf 50 % either parent's kind (vanilla 1/1024 mutation only for two same-type vanilla parents); the calf is the chosen parent's entity type. Fungi cows are found by entity id, so patcherMod does not depend on the port. Headless `pickCalf` test passed (400k rolls per pair). Mooblooms extend `Cow`, untouched |
| Cow spawns | `mob_spawns.json` | `mooshroom_dark_forests` gains `brown_chance` 0.5; new `mooshroom_mushroom_fields` (factor 1, `brown_chance` 0.25); `inky_cap_cow_dark_forests` (2), `inky_cap_cow_mushroom_fields` (8), `morel_cow_dark_forests_swamps` (2), `morel_cow_mushroom_fields` (8), groups 3 to 4. `spawn_ground` is already met (`#minecraft:mooshrooms_spawnable_on` holds `#minecraft:dirt`). `spawns_by_biome.md` regenerated from the 10-23 dump |
| Mushroom spawn system (§5.1.1) | `mushroom_spawns.json` / `.js`, shared code `worldgen/_biome_rules.js`, CEConfig `[worldgen.systems] mushroomSpawns`, `tools/test_mushroom_spawns.sh` | Table below. FD brown / red colony rules moved here unchanged. Pack copies of the colony feature for inky cap and morel (dirt and podzol, not mycelium only). Cave mode: no light predicate in 1.19.2 placement, so vanilla's underground test (`surface_relative_threshold_filter` OCEAN_FLOOR_WG at most -13) after an `environment_scan` to a floor; colonies only over `#minecraft:mushroom_grow_block` or `#minecraft:dirt`; noise caves only (features accept `air`, carvers leave `cave_air`) |
| Truffle dirt | `wild_crops.json` rules `rooted_dirt` (1 in 3) and `truffle_dirt` (every chunk, count 5), new per-rule `step` (`underground_ores`), `dark_oak` or `cold forest` (25 biomes) | Order relies on modifier id order (rooted before truffle): FGD-32 |
| Items, tags | `item_dict.js` (32 + truffle pie + 3 tags), `cookingtags.js` | `#forge:mushrooms/inky_cap` (+ cooked cap, colony), `/morel` (+ colony), `/truffle`; all in `#forge:mushrooms`; 5 `#forge:storage_blocks/*`. Pack users of `#forge:mushrooms`: bone broth, veg wrap filling (fine) |
| Recipes | `cooking.js`, `baking.js`; since 2026-10-25 the meal rows below are Creative Delight QoL-5 (`kubejs/data/creativedelight/kubejs/fungidelight/cooking.js`) | Grid meals re-id'd `_manual_only` (ice cream marked placeholder for `sweets #4`); heatless routes for the 7 pot meals; truffle pie in the oven pie list (crust + 2 truffles + 2 sugar, no can) |
| Food, cans, compost | `food_types.json` (16 foods), `gen_can_types.py` (truffle, cooked inky cap, truffle pasta; soup and salad by the type loops; `AWAIT_DUMP` holds `fungidelight`), `compost_values.js` (19 rows, upstream values) | Values are tuned after `/dumpfood` (CQ-FGD-DUMP) |
| Effects, JEI | `kubejs/assets/creativeengineering/lang/en_us.json` (JEED text), `kubejs/assets/creativedelight/kubejs/fungidelight/jei_info.js` (Creative Delight QoL-6 since 2026-10-25) (the pack's first JEI info pages: mushroom colonies, truffle, Weak Stomach foods) | [potions.md](potions.md) 2.3 row, PO-27 |

Mushroom rules (1 in N chunks; brown / red rates read from vanilla, Terralith and FD data; a Fungi form compared
with the mean of brown and red):

| Rule (mode) | Where | Patch / colony / huge |
|---|---|---|
| brown / red colony (surface) | `mushroom`, `dark_oak forest !mountain` | - / 15 / - (moved unchanged) |
| inky cap (surface) | `dark_oak forest !mountain` | 1365 / 60 / 7 |
| morel (surface) | same + `swamp` | 1365 / 60 / 7 |
| inky cap, morel (surface) | `mushroom` | 16 / 15 / 3; 12 / 15 / 4 (upstream) |
| cave patches (cave) | every overworld biome | inky 1024, morel 1024 (8 floor tries per rolled chunk) |
| cave colonies (cave) | every overworld biome | 120 each, all four |

Open values: pairing by colour instead of the mean (inky 1/1024, morel 1/2048 singles); swamp morel colonies borrow
the dark-forest 1/60 (swamps have no FD colony); cave count 8 and 16-block scan are guesses, and `crops_by_biome.md`
counts all 8 tries, so its cave rows read high; Terralith Mirage Isles has no `mushroom` / `dark_oak` word, so no
Fungi mushrooms there (its mooshroom and Fungi cow spawns do apply). Surface heightmap MOTION_BLOCKING_NO_LEAVES
(upstream MOTION_BLOCKING) so tries do not land on the canopy.

Still open (TODO `fungiport #2`): dump-driven steps (food values, cans, stack sizes, placeables, names, JEI coverage,
BlockRunner tiers for the huge blocks, WTHIT icons, loot TODO `vintageport #5` rows), trade review after play.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Port at all, and how much? | Full port as `backportedFungi` (it registers blocks and entities, so tier 4 in [feature_porting.md](feature_porting.md) terms). Small: 53 classes, a day's work on the Veggies template. Advancements dropped as Veggies (they are dropped at load anyway) | port all except the sniffer |
| D2 | Weak Stomach triggers | The pack's alcohol tag; keep "any non-water potion" as shipped (it is the joke) or limit it to alcohol | pack alcohol tag + potions, as shipped |
| D3 | Burrowing potion | Keep; register as `PotionBrewing` mixes so the mixers and bottling see it | keep, as mixes |
| D4 | Pig digging | Keep, add a `mobGriefing` check; the dug block still becomes coarse dirt. Alternative: leave the block | keep + `mobGriefing` |
| D5 | Inky Goo Veil growth | Cap the chain (for example 8 blocks, as cave vines stop at the floor) | as shipped (no cap) |
| D6 | Wild rates | Upstream rates in the pack biome words (§2.2); the "everywhere" patches at 1 in 768 / 384 stay | upstream rates |
| D7 | Cow spawns | Mushroom fields as shipped (weight 8 each); morel cows also in the dark forest mooshroom rule | mushroom fields only |
| D8 | Tooltip lines | Keep the mod's two lines; check they do not double the pack's effect lines | keep |
| D9 | Trades | None at first; add through the pack's tools after play | none |
| D10 | Suspicious stew recipes (broken effect id) | Drop them; vanilla flower stews remain | drop |
| D11 | Truffle Pie (CCK's dead item) | Owner's call: a new recipe is outside the scope rule unless asked for | leave dead |
| D12 | Marked overlaps (ice cream, scrambled eggs) | Ship both sides, review after play as for Vintage | ship both |

## 5.1 Owner plan (2026-10-23)

The owner's answers. They replace the defaults in §5 where they differ (the table at the end of this section lists
each D). Still nothing built. Rates below are relative to the vanilla/FD mushroom they sit next to and are tuned
after the first `/dumpall` with `tools/test_wild_crops.sh`; they are starting values, not measured ones.

### 5.1.1 Mushroom spawn system (new, own system)

*Added 2026-10-24: the Miner's Delight cave carrots use this system's cave mode (rule `cave_carrots`, three Y bands, nothing above Y 48; unlike the mushrooms they accept `cave_air`, so they grow in carver caves too): [miners_delight.md](miners_delight.md) 4.3.*

Mushroom plant spawning gets its own system instead of being spread over `wild_crops.json` rules, because the
wild-crops rules never match cave biomes (`NEVER = {ocean, cave}`) and mushrooms need a cave mode.

- **Files:** `kubejs/generation_scripts/worldgen/mushroom_spawns.json` + `kubejs/server_scripts/worldgen/mushroom_spawns.js`,
  built on the `wild_crops.js` machinery (same biome words from the `biomes` table, same clause rules, same
  forge:none replacement of the mods' own modifiers, same off switch in CEConfig `[worldgen.systems]`). It covers
  brown and red (vanilla + FD colonies, which move out of `wild_crops.json`), inky cap and morel (single patch,
  colony, huge form). Mooshroom animals stay in `mob_spawns.json` (§5.1.2); rooted dirt and truffle dirt stay in
  `wild_crops.json` (§5.1.3).
- **Two modes per rule:** `surface` (`vegetal_decoration`, biome words as in `wild_crops.json`) and `cave`
  (`underground_decoration`, matches the biomes `NEVER` skips plus any biome with a cave-carved floor: the
  placed feature is the existing patch with `height_range` below sea level, `block_predicate_filter` air above a
  `#fungidelight:morel_placeable_on` / `#minecraft:mushroom_grow_block` block, and a light filter of at most 7 so
  they sit in the dark). Rarity is written into the placed feature (`rarity_filter`), no config.

| Where | Inky cap | Morel | Brown / red | Rate |
|---|---|---|---|---|
| Dark forest | single, colony, huge | single, colony, huge | as vanilla / FD | each Fungi form at a small fraction (start: 1 in 4) of the vanilla brown/red form it matches: single patch vs patch, colony vs FD colony, huge vs vanilla dark-forest huge |
| Any swamp (`swamp`) | no | single, colony, huge | as vanilla / FD | morel forms at the same fraction as the swamp's brown/red patch and colony; huge morel at the dark-forest huge rate |
| Mushroom fields (`mushroom`) | upstream (1 in 16 patch, 1 in 15 colony, 1 in 3 huge) | upstream (1 in 12, 1 in 15, 1 in 4) | unchanged | upstream |
| Caves | single, rare | single, rare | vanilla cave mushrooms unchanged | inky and morel singles rarer than the vanilla cave brown/red (start: 1 in 3 of it); **rare colonies of all four** (start: 1 in 8 of the surface colony rate, never on stone-only floors without a growable block) |
| Everywhere else | none | none | unchanged | the upstream "all overworld" patches (1 in 768 / 1 in 384) are dropped; the upstream swamp, birch and taiga/coniferous patches are replaced by the rows above |

"Inky only in dark forest, mushroom fields and caves; morel in dark forest, swamps, mushroom fields and caves"
is the result. Terralith's own mushroom biomes (`dark/patch_mushroom`, enchanted `small_shrooms_*`) count as dark
forest / mushroom by their biome words; the table in [crops_by_biome.md](crops_by_biome.md) is regenerated.

**Growth seasons mirror the existing mushrooms.** Inky cap and morel go into the Serene Seasons tags
(year-round, unbreakable-infertile), read from the live tag files at build time; no season of their own. The
colonies stay out: FD's own colonies are in neither tag (live check 2026-10-25), so the mirror adds none. Open
question from that check: `year_round_crops` holds only the two Fungi mushrooms, not vanilla brown and red.

**JEI:** a mushroom on rich soil becomes its colony (random-tick chance as FD's). Add a JEI info page "Mushroom
colonies" listing brown, red, inky cap, morel: "A mushroom placed on rich soil slowly grows into a colony",
using the existing info-page route (step 18).

### 5.1.2 Mooshrooms

- **Brown mooshrooms first:** 1.19.2 has `MushroomCow.MushroomType.BROWN` (vanilla makes one only from lightning
  on a red one). Check it in the pack's classes with `javap` and a lightning test (FGD-27) before anything else:
  patcherMod and More Mooblooms must not strip it, and the Fungi cows (`FDMCow`) must still ignore lightning.
- **Natural brown mooshrooms** in dark forests and mushroom fields: vanilla spawns only the entity type, so
  patcherMod gets a `FinalizeSpawn` hook on `minecraft:mooshroom` (natural and chunk-generation spawns only, not
  eggs or breeding) that sets the type to BROWN with the chance of a `mob_spawns.json` field
  (`brown_chance`, per rule). Start values: dark forest 1 in 2, mushroom fields 1 in 4. The existing
  `mooshroom_dark_forests` rule (weight 8) keeps its herd sizes.
- **Fungi cows:** Inky Cap Cow in dark forests and mushroom fields; Morel Cow in dark forests, any swamp and
  mushroom fields, all at a low weight next to the mooshroom (start: 2 each against the mooshroom's 8 in the dark
  forest and swamp, upstream 8 against 8 in mushroom fields). Fungi's two spawn modifiers are disabled and replaced
  by `mob_spawns.json` rules (D7). Swamp cows need `spawn_ground` for `#mooshrooms_spawnable_on` as the dark
  forest rule does; the mushroom-fields mycelium rule stays.
- **Breeding:** every mooshroom type breeds with every other (red, brown, inky cap, morel). Calf type: **50 % the
  type of each parent**, rolled in `getBreedOffspring` through a patcherMod mixin that replaces both vanilla's
  red/brown rule and upstream's `MushroomCowMixin.canMate` + "parent whose goal fires" rule. The calf entity is
  the Fungi cow class or the vanilla one by the chosen type. Vanilla's 1 in 1024 red/brown mutation stays for two
  parents of the same vanilla type only; upstream's Fungi mutation is dropped. Mooblooms are a different class and
  do not breed with these.

### 5.1.3 Truffles, rooted dirt, pigs, wolves

- **Placement folded into `wild_crops.json`:** rooted dirt blob and truffle dirt are one `wild_crops.json` rule
  (`rooted_dirt`, step `underground_ores`, features blob then truffle ore, so one modifier places them in that order; as
  two rules no truffle dirt generated, r1102 SQ-FGD-14, `creativedelight/members #42`, merged 2026-11-02). **Biomes: dark
  forest, cold forest and birch forest** (clauses `dark_oak`, `cold forest` or `birch`; birch added by the owner
  2026-11-02, no longer all of `coniferous`); the other taiga and conifer biomes lose them. Truffle dirt keeps "every chunk the blob is there".
- **Pig food:** the Fungi additions (brown and red mushroom, inky cap, morel, truffle) go onto `Pig.FOOD_ITEMS`
  as in §2.3 and are listed **for review with the whole mob-food system** (`tweaks #13`): marker in the TODO row
  and in [mob_block_tweaks.md](mob_block_tweaks.md); no other pig-food change until then.
- **Truffle breeding kept** (extra piglets in 50 % of litters).
- **Pig digging stays, and its block change is gated by the griefing system.** The pack already has Friendly
  Griefing (`FriendlyGriefing-1.19-1.0.2.jar`): `EntityMobGriefingEvent` allows villagers and denies everything else when
  `mobGriefing` is false ([feature_porting.md](feature_porting.md), FP-04). Pigs are "non-friendly". The digging
  goal asks the event (`ForgeEventFactory.getMobGriefingEvent`) instead of reading the gamerule itself, so
  the pig-side check is the same one every other mob goes through and a later change to friendly vs non-friendly
  lists lands here for free. With griefing denied the pig neither digs nor changes the block. Replaces D4's
  separate `mobGriefing` read. If FP-04 shows the live gamerule is true, the digging simply runs.
- **Wolf truffle hunt, hoglin and piglin truffle use kept** as in §2.3.

### 5.1.4 Effects, potions, Weak Stomach

- **Weak Stomach kept**; the durian items stay in `#fungidelight:alcohol_reaction` so eating durian applies it;
  "any non-water potion" keeps triggering the reaction (D2 as shipped); the alcohol side reads the pack's
  `#creativeengineering:food_type/drink/alcohol`.
- **Burrowing kept** (levels I and II, shovel-block break speed).
- **Potions fold into the brewing systems:** the three Burrowing potions are `PotionBrewing` mixes (D3), so brewing
  stand, Create automatic brewing, IE potion machines, bottling, splash/lingering and potion buckets treat them
  like any other potion; rows in [potions.md](potions.md), the buckets table in [potion_buckets.md](potion_buckets.md).
- **Effect descriptions in JEI** come from the mod's own strings: `tooltip.fungidelight.burrowing_desc`
  ("+%s Mining Speed on soil") and `tooltip.fungidelight.weak_stomach_desc` ("Causes Weak Stomach") are fed to
  JEED as the two effects' descriptions (level substituted for burrowing: 50 % / 100 %), and the on-item tooltip
  lines stay as the mod ships them unless they double the pack's effect lines (D8).

### 5.1.5 Bugs

- **Suspicious stew effect-id bug (design):** the recipes store a numeric `EffectId`, which is a registry slot and
  wrong here. The three stews become one small recipe class in the port (`fungidelight:effect_stew`, a shapeless
  recipe) whose JSON names the effect by registry name (`"effect": "farmersdelight:comfort"`) and whose
  `assemble()` writes the stew NBT at craft time with vanilla's own saver (`SuspiciousStewItem.saveMobEffect`,
  which stores the live id). No numeric ids in data, so registry order cannot break it. Effects follow each
  mushroom's dish in §2.5 so no new mechanic appears: inky cap -> Luck, morel -> Comfort, truffle -> Haste, all
  at 0:08 like the flower stews (confirm against the upstream lang/advancement text at port time; the choice is
  the one to review after play). JEI shows the output through the stew's own effect tooltip.
- **Inky cap patch placement bug:** the inky patch feature reads `inky_cap_placeable_on` (the tag with sculk), not
  the morel tag. Applied in the cave and surface features of §5.1.1, so inky caps can also grow on sculk in caves.
- **Inky Goo Veil:** natural growth capped at **16** segments. `InkyGooVeilBlock` counts the chain upward from
  the tick position (stops at the first non-veil block) and does not add a segment at 16, for random ticks
  and bone meal alike. Hard-coded constant, no config.

### 5.1.6 Items, tags, food, recipes

- **Everything ships:** all 32 items, 17 blocks, 10 dishes, 5 crates, 34 recipe files (Create milling gated as
  shipped, Botany Pots left out, the three suspicious stews as in §5.1.5). Cooking by the pack rules through
  `global.NCH` ([recipes_cooking.md](recipes_cooking.md)).
- **Mushroom tag on each item:** every mushroom-type item (inky cap, morel, cleaned cap, cooked cap, truffle,
  truffle slice, and the two colonies) is in `#forge:mushrooms` and its `#forge:mushrooms/<type>` tag through the
  `global.tags` helper, not a hand-written event. Dishes and crates are food/storage, not mushrooms; they are
  not tagged (a dish must not satisfy a recipe that asks for "any mushroom"). Step 2 audit of `#forge:mushrooms`
  users covers the new members.
- **Truffle Pie:** Fungi's truffle goes into Create Central Kitchen's `truffle_pie` (and slice): a baking recipe in
  the pack's pie system (`baking.js`, through `global.NCH`) with truffle as the filling, so the dead item
  becomes obtainable ([baked-goods #14](todo/TODO.md) loses a row). This overrides D11's "leave dead".
  Built 2026-10-31 (`ports/fungiport #3`): CCK has the block but no item without Environmental, so the pack registers
  `create_central_kitchen:truffle_pie` as a block item (CD `kubejs_startup/create_central_kitchen/truffle_pie_item.js`),
  placeholder sprite Farmer's Delight's chocolate pie (own art: `largecontent/mod-additions #3`), name in CD
  `create_central_kitchen/names.js`. `/fgdtest` now FAILs on a missing oven truffle pie (was a NOTE). Test SQ-CM-36.
- **Truffle Ice Cream:** registered and kept; its grid recipe stays as a placeholder (marked) until the ice cream
  family is decided (`sweets #4`), then it becomes one of that family's flavours.
- **Inky Cap Scrambled Eggs:** fine as shipped; both it and Incubation's version ship (D12).
- **Villager trades kept** in code as upstream (farmer level 3, wandering trader) and **sounds kept** (pig happy,
  dig, drop, wolf); the trades are reviewed against the pack's trade tooling after play (replaces D9's "none").
- **Compost folds into the pack's engine:** upstream hook dropped; values merged with
  `gen_compost_values.py --add-new` into `compost_values.js` (step 13).

### 5.1.7 Decisions after this plan

| # | Result |
|---|---|
| D1 | Full port, sniffer dropped (as recommended) |
| D2 | Weak Stomach: pack alcohol tag + durian + any non-water potion (kept) |
| D3 | Burrowing as `PotionBrewing` mixes, folded into the brewing systems (kept) |
| D4 | Pig digging kept; block change goes through the Friendly Griefing event, not a direct gamerule read |
| D5 | Veil capped at 16 (was recommended 8) |
| D6 | Replaced: own mushroom spawn system with caves (§5.1.1); "everywhere" patches dropped |
| D7 | Replaced: §5.1.2 (brown mooshrooms, Fungi cows in dark forest, swamp, mushroom fields) |
| D8 | Mod description strings used in JEI/JEED; on-item lines kept unless doubled |
| D9 | Replaced: trades kept |
| D10 | Replaced: stews kept with the effect-by-name design (§5.1.5) |
| D11 | Replaced: truffle pie recipe from Fungi truffle |
| D12 | Ship both; ice cream folds into the family later |

Open items this adds for the build: confirm brown mooshroom code path (FGD-27), choose the cave "light at most 7"
filter value against the live cave biomes, and the starting rates above (FGD-28).

## 6. File map (planned)

| Path | What |
|---|---|
| `development/creativeDelight/members/fungi/` | the port (README with every deviation, `NOTICE-upstream-MIT.txt`) |
| `kubejs/startup_scripts/_lib/item_dict.js`, block dict | ids |
| `kubejs/generation_scripts/worldgen/wild_crops.json`, `kubejs/server_scripts/worldgen/wild_crops.js` | wild rules, `step` field |
| `kubejs/generation_scripts/worldgen/mob_spawns.json` | cow spawns, `brown_chance` |
| `kubejs/generation_scripts/worldgen/mushroom_spawns.json`, `kubejs/server_scripts/worldgen/mushroom_spawns.js` | mushroom spawn system with cave mode (§5.1.1) |
| `kubejs/server_scripts/` (cooking, potions), CD `kubejs_startup/fungidelight/compost.js` (was `compost_values.js`) | recipes and compost |
| `mod_sides.md`, `tools/build_deploy.py` | release |
| `kubejs/server_scripts/worldgen/_biome_rules.js` | code shared by wild crops and mushroom spawns |
| `development/patcherMod/.../mooshroom/`, `mixin/MushroomCowBreedingMixin.java`, `mixin/AnimalCanMateMooshroomMixin.java`, `worldgen/SpawnTuning.java` | brown mooshrooms, breeding |
| `kubejs/assets/creativedelight/kubejs/fungidelight/jei_info.js` (CD), `kubejs/assets/creativeengineering/lang/en_us.json` | JEI info pages, JEED text |
| `tools/test_mushroom_spawns.sh`, `tools/test_wild_crops.sh`, `tools/gen_crop_worldgen_table.py` | offline checks, [crops_by_biome.md](crops_by_biome.md) |
| `kubejs/assets/creativeengineering/changelog/unreleased.md` | bullets when built |

## 7. Offline checks (once built)

- The jar registers no sniffer class, no `configurable_rarity_filter`, no config file.
- `unzip -l` shows no advancement folder; recipe JSON has no `category` field.
- Burrowing appears in `PotionBrewing` mixes (headless harness), not in `BrewingRecipeRegistry`.
- `tools/test_wild_crops.sh` and `tools/test_mushroom_spawns.sh` pass (2026-10-23). Jar checks pass (no advancements,
  sniffer, rarity filter, placed features, biome modifiers or `category`). Not run yet: Burrowing in `PotionBrewing` mixes
  (headless harness), queued as SQ-FGD-05.

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| FGD-01 | Game loads with the port; `/dumpall` lists 32 items, 17 blocks, 2 entities | no errors in `latest.log` |
| ~~FGD-02~~ | moved to FGD-28 (biomes replaced by §5.1.1) | |
| FGD-03 | Mushroom fields: both colonies, both huge mushrooms, both cows | present |
| FGD-04 | Bone meal on a placed inky cap and morel in low light | huge form grows; veil hangs under the inky cap |
| FGD-05 | Inky cap / morel on rich soil | becomes a colony |
| FGD-06 | Colony on the cutting board; inky cap on the cutting board | 5 mushrooms; cleaned cap + ink sacs |
| FGD-07 | Truffle dirt in a dark or cold forest under rooted dirt; mine with a shovel, with Fortune, with Silk Touch | 1 to 2 truffles, more with Fortune, the block with Silk Touch |
| FGD-08 | Wolf truffle hunt: feed a tamed wolf a truffle near truffle dirt | walks to it, barks, scratches; truffle not used up |
| FGD-09 | Pig digging (raise the chance in a test build or wait) on podzol | loot or truffles pop; block becomes coarse dirt; no "Parameters not allowed" loot error in `latest.log`; griefing case is FGD-33 |
| FGD-10 | Breed pigs with a truffle 10 times | extra piglets in about half the litters |
| FGD-11 | Pigs follow brown / red mushrooms, inky caps, morels, truffles | tempted and bred |
| FGD-12 | Hoglin bred with truffles; piglin picks up a truffle | both work |
| FGD-13 | Shear an Inky Cap Cow and a Morel Cow | cow left, 5 mushrooms |
| FGD-14 | Bowl on a Fungi cow | mushroom stew |
| ~~FGD-15~~ | moved to FGD-30 (breeding is 50 % either parent, §5.1.2) | |
| FGD-16 | Eat a Cleaned Inky Cap, then drink a pack alcohol | Weak Stomach, then Nausea II, Poison II, Weakness II for 0:10 |
| FGD-17 | Eat a Cleaned Inky Cap, then drink a water bottle and a potion | water: nothing; potion: the reaction |
| FGD-18 | Brew Burrowing in a stand and in a Create mixer (awkward + truffle, + redstone, + glowstone) | all three in both |
| FGD-19 | Burrowing I and II on dirt, gravel, sand vs stone | 1.5x and 2x on shovel blocks only |
| FGD-20 | Each pot meal cooks; JEI shows every recipe | 7 pot meals, 3 grid meals |
| FGD-21 | Eat each dish | values and effects as tuned (step 7) |
| FGD-22 | Crates craft and uncraft; crate breaks drop the crate | 9 each way |
| ~~FGD-23~~ | moved to FGD-35 (cap 16) | |
| FGD-24 | Compost each item | chances as in `compost_values.js` |
| FGD-25 | Mill an inky cap and a colony in Create | black dye as §2.6 |
| FGD-26 | Dedicated server with the port, client joins | no missing-class or registry mismatch |
| FGD-27 | Lightning on a red mooshroom; on an Inky Cap Cow; `javap` for BROWN | red becomes brown; Fungi cow unchanged |
| FGD-28 | Explore dark forest, swamp, mushroom fields, caves (`/ceplace`) | inky only in dark forest, mushroom fields, caves; morel also in swamps; Fungi forms rarer than brown/red; rare colonies of all four in caves |
| FGD-29 | Dark forest and mushroom fields herds | brown mooshrooms at the set shares; Fungi cows rare; morel cow in swamps |
| FGD-30 | Breed red x brown, inky x morel, red x inky 10 times each | calves are either parent's type, about 50 % each |
| FGD-31 | Mushroom on rich soil; JEI info page | becomes the colony; page lists the four mushrooms |
| FGD-32 | Truffle dirt in dark and cold forests; taiga that is neither | present only in the first two |
| FGD-33 | Pig digging with `mobGriefing` true and false (Friendly Griefing loaded) | digs when true; nothing when false |
| FGD-34 | Craft each of the 3 suspicious stews, eat | the named effect, not an arbitrary one; survives a restart with changed mod list |
| FGD-35 | Inky Goo Veil under a tall overhang, bone meal repeatedly | stops at 16 segments |
| FGD-36 | Eat durian, drink a potion, then an alcohol | Weak Stomach, reaction on the potion |
| FGD-37 | JEI on Burrowing and Weak Stomach | mod description text shown |
| FGD-38 | Bake a truffle pie | recipe in JEI, pie obtainable |
| FGD-39 | Farmer level 3 and wandering trader; pig and wolf sounds | trades appear; sounds play |
| FGD-40 | Seasons: inky cap, morel and colonies in winter | same behaviour as brown/red |

## 9. History

- 2026-10-23: investigation written from the Forge 1.20.1 1.3 jar (downloaded from Modrinth, decompiled, 53 classes)
  and the NeoForge 1.21.1 1.3 jar already in `DecompiledMods/` (same content), checked against the pack's
  display-name, tag, biome and recipe exports, the FD 1.19.2-1.2.4 classes and the Forge 1.19.2 source. Found: no
  1.19.2 build; no cheese in the mod; four upstream bugs (§2.9); Burrowing invisible to the Create mixer as shipped;
  CCK's truffle pie is dead without Environmental. Nothing built.
- 2026-10-23 (later): owner plan written (§5.1): own mushroom spawn system with cave mode, brown mooshrooms, 50 %
  mooshroom breeding, truffle placement in dark and cold forests, Friendly Griefing for pig digging, suspicious stew
  by effect name, veil cap 16, truffle pie, trades and sounds kept. Tests FGD-27 to FGD-40 added. Nothing built.
- 2026-10-23 (build): §5.1 built (§4.0): port jar, patcherMod mooshroom family and `brown_chance`, cow spawn rules,
  mushroom spawn system with cave mode, truffle dirt rules, ids, tags, recipes, truffle pie, food types, cans (waiting
  on the dump), compost, JEED text, JEI info pages, changelog. FGD-02, FGD-15, FGD-23 retired into FGD-28, -30, -35.
  Offline checks (§7) pass. Live checks queued as SQ-FGD at the top of the scratch queue. Jars not installed.
- 2026-11-01: `creativedelight/members #8` (move the mooshroom family into More Mooblooms) not built: creative_split.md
  7.6 (owner) sends `mooshroom/`, `MooshroomFamily` and More Mooblooms to Creative Worldgen as fauna, not CD. The
  move rides the Worldgen carve (`creativesplit/worldgen #8`); the classes stay in patcherMod until then.
- **2026-11-02:** nested in Creative Delight (`creativedelight/members #1`): `bundled_in=creativeDelight`, jar `backportedFungi-1.3-ce-19.2-02` inside the CD jar, "Fungi Delight (Creative Delight)"; folder stays `development/creativeDelight/members/fungi`; build and install through `:creativeDelight:` only.
- **2026-11-02 (afternoon):** folder moved to `development/creativeDelight/members/fungi` (plan_creative_delight.md I8).
  Truffle dirt fix (I6): the blob and truffle ore rules merged into one, birch forests added (5.1.3); crops_by_biome.md
  regenerated. Rerun SQ-FGD-14 in the SQ-FGDC world.
