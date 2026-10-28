# Vinery (Let's Do: Vinery 1.3.10)

Test prefix **VN-** · Last updated **2026-10-01** · Overall status **reference from the decompiled jar, in-game checks pending**

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Block behaviour reference (pot, press, barrel, stems, lattice, bushes, vines, leaves, sink, log, storage) | §2, read from the decompiled `letsdo-vinery-forge-1.3.10.jar` | reference; VN-01 to VN-20 check the surprising parts |
| Wine aging and wine effects | §2.10, §2.11 | reference |
| Grape and grape seed flavour lines removed | [tooltips.js](../kubejs/client_scripts/tooltips.js) Vinery section, see [tooltips.md](tooltips.md) | DEPLOYED (client script), TT-14 pending |
| Tooltip lines for the main Vinery blocks | same section, `TODO` placeholders | placeholders only, text not written yet |
| JEI pages (fermentation, press, stomping, aging) | patcherMod, see [vinery_jei.md](vinery_jei.md) | REMOVED 2026-10-27 (the port ships its own) |
| Other pack changes (oil, compost, crates, recipes, tags) | §3 | DEPLOYED |

- **Open first:** this file's §2 for any "how does X work" question. Then [vinery_jei.md](vinery_jei.md) for the JEI pages.
- **Knobs:** `config/vinery/config.json5`. The pack runs the defaults: `year_length_in_days` 16, `years_per_effect_level` 4,
  `enable_wine_maker_set_bonus` true, `probability_to_keep_bone_meal` 100, `probability_for_damage` 30,
  `damage_per_use` 1, `wine_trader_chance` 50.
- **How to re-read the code:** `python3 tools/decompile_mod.py mods/letsdo-vinery-forge-1.3.10.jar <out dir>` (§6).

## 1. Overview

Vinery is a farming and drinks mod. You grow grapes on stems, on lattices or as wild bushes, and pick
apples and cherries from fruiting leaves. Grapes are stomped into juice in the Grapevine Pot. Apples
are pressed into Apple Mash in the Apple Press. Juice, mash and other ingredients go into the Aging
Barrel with an empty wine bottle to make a wine. A wine's effect strength grows with its age in
in-game years. The mod also adds wine storage blocks, cherry/apple wood, decoration, Winemaker armor,
a wandering winemaker trader with a mule, and Winemaker villager trades.

Nothing in the mod gives XP from a machine. The only XP-related thing is Cristel Wine's effect (§2.11).

Random-tick times below assume the default `randomTickSpeed` of 3. At that speed a given block gets a
random tick about every 1365 game ticks (about 68 s) on average.

## 2. Reference

### 2.1 Grapevine Pot (`vinery:grapevine_pot`), grapes to juice

No block entity, no inventory, no GUI. Everything is right-click, so hoppers can't feed it.

| Step | Rule |
|---|---|
| Fill | Right-click with any grape: 1 grape per click, up to 6. Grapes are refused once the pot holds 6 or stomping has started (stage above 3). Creative doesn't use the grape. |
| Type | The **first** grape sets the juice type. Later grapes of any type are accepted and count as the first type. |
| Stages | 1st grape: stage 1. 3rd grape: stage 2. 6th grape: stage 3. At stage 3 the floor drops (new collision shape). |
| Stomp | Every landing by **any living entity** (a jump counts, mobs count) raises the stage by 1, from 3 to 6. Squeeze sound on each landing, also at 6. No timer. |
| Collect | At stage 6, right-click with an empty **`vinery:wine_bottle`** (glass bottles don't work). Each bottle uses 3 grapes' worth. 6 grapes give **2 juice bottles**. The second bottle resets the pot to empty. |

The juice is the matching `*_grapejuice_*_bottle` for the 8 grape types. An unset type would give red juice, but you can't reach that state in play.

### 2.2 Apple Press (`vinery:wine_press`), apples to Apple Mash

| | |
|---|---|
| Slots | 0 = input, 1 = output. In the GUI the input accepts **any item**, but only vanilla `minecraft:apple` is processed. The output takes nothing in the GUI. |
| Recipe | 1 apple → 1 `vinery:apple_mash`, hardcoded (no recipe type, no datapack control). |
| Time | **72 ticks (3.6 s)** per apple. Runs while slot 0 holds an apple and the output is empty or Apple Mash below 64. |
| Progress | Goes back to 0 the moment the recipe stops being valid (input removed, output full). Saved in the block's NBT. |
| Hoppers | The block exposes both slots on every side and accepts any item into either slot. A hopper can put items straight into the output: a non-apple, or an apple that overflows a full input stack, lands in slot 1 and **jams the press**. Hoppers can also pull apples back out of the input. |
| XP | None (the output slot is a furnace-style slot, but it only pays XP for real furnaces). |
| Break | Drops its contents. |

### 2.3 Aging Barrel (`vinery:fermentation_barrel`), wine

The Vinery port calls this block the **Cask** (alcohol.md B7.9). This section describes 1.3.10.

| | |
|---|---|
| Slots (GUI) | 0: only `vinery:wine_bottle`. 1-4: only items that appear in some `vinery:wine_fermentation` recipe. 5: output, nothing can be placed. |
| Recipe match | The number of **filled** slots among 1-4 must equal the recipe's ingredient count exactly (recipes have 1-4 ingredients). A stray extra item stops it. Order doesn't matter. |
| Craft | Uses 1 wine bottle and 1 from each ingredient slot. Stock it only ran when slot 5 was empty (one wine at a time). **Patched (patcherMod `FermentationBarrelStackOutputMixin`):** it also runs while slot 5 holds the same wine (same item, same `Year` tag) with room left, so the output stacks. A wine from a different year blocks it until taken out, because the two would not stack. |
| Time | **About 50 ticks (2.5 s)**, not 1800. See below. |
| Output | Gets an NBT `Year` tag (the current wine year, §2.10). |
| Hoppers | The barrel is a plain container with no side rules: hoppers can put **any item into any slot** (the first empty one is the bottle slot) and pull from any slot, including the ingredient slots. |
| XP | None. |

**Why 50 ticks:** the class has `COOKING_TIME_IN_TICKS = 1800`, but nothing reads it. The real target
is a field that `setItem()` sets to 50 whenever slot 0, 2, 3, 4 or 5 gets a different stack (slot 1
is left out, a Vinery bug). That field isn't saved to NBT. After a chunk reload it is 0, so a barrel that
was waiting finishes on its next tick.

### 2.4 Grapevine Stem (`vinery:grapevine_stem`), non-jungle grapes

| | |
|---|---|
| Placing | The block below must be a full solid block (`isRedstoneConductor`) or another stem. Placing it on a non-stem while holding **2 or more** builds a 2-high stem and uses both. |
| Planting | Right-click with seeds, only on the **upper** stem (a stem below it), only while it's empty. Any non-jungle seed works, and **savanna and taiga seeds ignore their bush requirements** on a stem. |
| Growth | +1 stage on **every** random tick (no chance roll) from 1 to 4, which takes about 3.4 min. Needs a stem below and raw light 9 or more at the stem. |
| Harvest | Main-hand right-click only works at stage 4: drops 3-4 grapes and drops back to stage 2 (about 2.3 min to regrow). |
| Shears | Right-click: drops grapes (2 at stage 3, 3-4 at stage 4) plus the seed, and empties the stem. The shears lose no durability. |
| Bone meal | Works at stages 1-3 with a stem below: +1 or +2 stages, capped at 4. Never fails. |
| Breaking | Player break: grapes if stage 3 or more, plus the stem item. **The seed is lost.** It's only returned when the stem pops because its support is gone (seed if planted, grapes if stage 3+, and the stem item). |
| Off hand | Uses the base rule (harvest at stage 4 only, back to stage 2). |

### 2.5 Grapevine Lattice (`vinery:grapevine_lattice`), jungle grapes

| | |
|---|---|
| Placing | On a wall face. Clicking a top or bottom face faces it toward you. The block behind needs a **full face**. |
| Planting | Jungle seeds only, on an empty lattice. No lattice below needed. |
| Growth | Same as the stem: every random tick, raw light 9 or more, stage 1 → 4. |
| Harvest | Main-hand right-click with an empty hand or any non-shears, non-seed item at stage 3 or more. Stage 3 drops 2, stage 4 drops 3-4. Resets to **stage 1**. The held item loses 1 durability if it has any. |
| Shears | Drop the grapes (if 3+) and the seed, empty the lattice, and cost 1 durability. |
| Bone meal | Only works when **another lattice sits directly below** (the stem's "trunk" check is reused). A single lattice can't be bone-mealed. |
| Breaking | Same as the stem (seed lost on a player break). |

### 2.6 Wild grape bushes (seeds planted on the ground)

The seed items are what place the bushes (red, white, savanna and taiga types). The bushes have no item of their own, and pick-block gives the seed.

| | Red / White | Savanna | Taiga |
|---|---|---|---|
| Raw light to place, survive and grow | 10 or more | **14 or more** | 5 or more |
| Extra need | none | none | podzol, coarse dirt **or grass block** within ±4 horizontally and −2..+1 vertically |
| Growth chance per random tick | 1/5 (about 5.7 min per stage) | 1/3 (about 3.4 min) | 1/5 |

- **Light:** this is the block's raw light level, so time of day doesn't matter. Open sky is always 15.
- **Ground:** the block below must be a full opaque block (`isSolidRender`). Farmland doesn't count.
- **Survival:** the light and ground rules are also the survival rules. A bush pops when a neighbour update finds them broken.
- **Stages:** 0 → 3.
- **Harvest:** right-click at stage 2 or more. Stage 2 drops 0-1 grapes and stage 3 drops 1-2. Resets to stage 1.
- **Bone meal:** +1 stage, always works. Right-clicking an unripe bush with bone meal lets the bone meal act.
- **Breaking:** stage 2 or 3 drops 1 seed. Stage 0-1 drops **nothing**.
- **Taiga thorns:** taiga bushes slow you (0.8 horizontal, 0.75 vertical) and deal 1 damage (sweet berry damage) per tick while you move through them at stage 1 or more. Foxes and bees are immune.
- **Collision:** none (copies the sweet berry bush properties).

### 2.7 Jungle grape vines (jungle seeds placed on a wall)

- **Vine behaviour:** a vanilla `VineBlock` subclass, so it attaches and spreads like vines.
- **Growth:** 1/5 chance per random tick, needing raw light 9 or more at the block **above**, stage 0 → 3.
- **Harvest, bone meal and loot:** the same as the bushes (§2.6).
- **Shears:** right-click toggles `sterilized`. A sterilized vine gets **no random ticks**: it neither ripens nor spreads. The shears lose no durability.

### 2.8 Grapes and seeds (items)

| | Red, white, savanna, taiga grapes | Jungle grapes |
|---|---|---|
| Food values | sweet berries (2 hunger, 0.1 saturation modifier) | baked potato (5 hunger, 0.6 saturation modifier) |
| Seed back on eating | 20% | 20% |

Vinery's per-variant flavour lines on grapes and seeds are removed by the pack (§3). Those lines said:
- grapes: "can be found in temperate / warm / cold / jungle biomes"
- seeds: the bush requirements, with "needs Coarse Dirt or Podzol" for taiga. That claim was incomplete, because grass blocks also count.

### 2.9 Apple Leaves and Cherry Leaves

- **Which leaves fruit:** only the "variant" leaves (`can_have_apples` / `can_have_cherries` = true) ever fruit.
  - Saplings pick the variant tree 50% of the time. Worldgen trees come from the same feature files.
  - **Leaves you place never fruit in survival.** The variant is only set when a creative player places them while sneaking.
- **Growth:** a variant leaf without fruit has a 40% chance per random tick to grow fruit (about 2.8 min on average).
- **Picking:** right-click gives 1-3 fruit half the time and exactly 1 otherwise, popped from the clicked face.
  - Cherry leaves have a 1-in-8 chance that the whole drop is rotten cherries instead.
- **Decay:** leaves still decay like vanilla leaves.

### 2.10 Wine aging (happens on the item, anywhere)

- **Wine year:** `dayTime / 24000 / year_length_in_days`. With 16-day years, one year is 384 000 ticks (5 h 20 min of play). This uses world day time, so `/time set` moves it.
- **Stamping:** the barrel writes `Year` once. A wine with no `Year` gets the current year the first time it's read.
- **Effect level:** `min(5, (now - Year) / years_per_effect_level)`. It sets the effect's amplifier, so level 0 is effect I.
  - +1 level every 64 in-game days (about 21 h 20 min of play).
  - The cap (effect VI) is reached after 320 days (about 107 h).
- **Duration:** fixed by the wine and does **not** grow with age. Most wines last 900 ticks (45 s).
- **Food values:** 1 hunger, 0 saturation, can be drunk on a full hunger bar. Drinking returns an empty wine bottle.
- **Instant effects:** vanilla Instant Health / Instant Damage are forced to 1 tick.

### 2.11 Wine effects

| Effect | Wines | What it does (code) |
|---|---|---|
| Improved Fire Resistance | red_wine, mellohi_wine | immune to fire damage; your hits set the target on fire for 5 s |
| Improved Regeneration | stal_wine, apple_cider (15 ticks) | +1 HP every `50 >> amplifier` ticks, +4 max health per level |
| Improved Water Breathing | kelp_cider | water breathing, faster swimming |
| Improved Absorption | strad_wine | gives Absorption + Resistance for 30 s at the same level |
| Improved Strength | solaris_wine | +3 attack damage and +10% attack speed per level |
| Improved Haste | bolvar_wine | counts as Haste for mining, +1 luck, +10% attack speed |
| Improved Jump Boost | clark_wine | jump boost plus reduced fall damage (client mixin also touches jumping) |
| Improved Speed | chenet_wine | +20% speed per level, **walk on water** |
| Improved Instant Health | apple_wine (10 ticks), king_danis_wine (45 ticks) | heals `6 << amplifier` **every tick** for the duration (an instant effect with a duration > 1 reapplies each tick) |
| Jellie | jellie_wine | absorption +4 per level, +1 HP every `50 >> amplifier` ticks |
| Magnet | magnetic_wine | items within 5 + level blocks are picked up instantly (pulled toward you if the inventory is full); off while sneaking |
| Teleport | chorus_wine (1 tick) | teleports up to 12 blocks where you look |
| Trippy | jo_special_mixture | client shader only (the pack also uses it in the echo tool "discombobulate" on-hit list) |
| Experience | cristel_wine | XP orbs worth +50% per level |
| Creeper | creepers_crush (1 tick) | a power-4 explosion at you that breaks blocks (not in creative) |
| vanilla | aegis_wine (Night Vision), noir_wine (Instant Health, 5 ticks), cherry_wine (Regeneration), praetorian_wine and glowing_wine (Glowing) | vanilla behaviour |

Improved Night Vision exists, but no wine gives it. Its mixin would make you invisible.

### 2.12 Utility and storage blocks

- **Kitchen Sink** (`vinery:kitchen_sink`), marked "Deprecated, can't be crafted":
  - Every sink is placed with a faucet. An empty-hand click on an empty sink fills it for free, so it's an **infinite water source**.
  - On a full sink, a bucket gives a water bucket and a glass bottle gives a water bottle. Either empties the sink.
  - Using a water bucket on an empty sink fills it and returns a bucket. Using a glass bottle on an empty sink also fills it, and you keep the bottle.
- **Stackable Log** (`vinery:stackable_log`):
  - Slab-like: place a second one to make a double stack.
  - Flint and steel on a double stack lights it with no fuel and no durability cost. It burns forever at light 13.
  - A shovel puts it out.
  - Standing on a lit stack deals 1 fire damage per tick, unless you're fire-immune or wear Frost Walker.
  - Hay below makes signal smoke. Decoration only: it doesn't cook.
- **Wine storage:**

  | Block | Holds |
  |---|---|
  | `wine_rack_1` | 9 small bottles, placed and taken by hand |
  | `wine_rack_2` | 4 small bottles, by hand |
  | `*_wine_rack_mid` | 1 big bottle |
  | `wine_rack_3` (Cherry Cabinet), `wine_rack_5` (Cherry Drawer) | 18-slot chest GUI that takes any item, with comparator output |
  | `wine_box` | 1 small bottle |

  For the `wine_box`, shift-right-click with an empty hand opens and closes it, and it can only be used while open. Wines can also be placed as blocks unless you're sneaking.
- **Winemaker armor set bonus:** with all four pieces worn, bone meal that took effect is refunded (100% in this pack). 30% of the time each piece then takes 1 damage. The pack's `BonemealCharmMixin` generalizes this trick to `#bonemeal_charms` (memory `bonemeal-charm-tool-effect`).

### 2.13 Worldgen

| Feature | Biome tag (Vinery's own, note the "spanws" spelling) | Density |
|---|---|---|
| Red / white grape bushes | `#vinery:spanws_grape`: forest, plains, swamp, birch forest, meadow, sunflower plains, river | 1 in 92 chunks, random patch of 36 tries |
| Savanna bushes | `#vinery:spanws_savanna_grape`: savanna, savanna plateau, windswept savanna, wooded badlands | 1 in 92 chunks |
| Taiga bushes | `#vinery:spanws_taiga_grape`: taiga, both old-growth taigas, snowy taiga | 1 in 92 chunks |
| Jungle vines | `#vinery:spanws_jungle_grape`: jungle, sparse jungle, bamboo jungle, wooded badlands | 1 in 5 chunks, 12 tries, hanging vines up to 12 long |
| Apple and cherry trees | `#vinery:spanws_cherry_tree` (both): plains, sunflower plains, meadow, several Terralith biomes | 1% of chunks get one tree |

Wild jungle vines spawn at a random stage from 0 to 2.

## 3. What the pack changes

- **Tooltips** ([tooltips.js](../kubejs/client_scripts/tooltips.js), Vinery section; recorded in [tooltips.md](tooltips.md)):
  - Removed Vinery's per-variant grape and grape seed lines (`item.vinery.grape.tooltip.*`, `item.vinery.grapeseed.tooltip.*`). `removeTooltipKey` now accepts a key ending in `*` as a prefix, because Vinery builds the key from the item id.
  - Added `TODO` placeholder lines for the pot, press, barrel, stem, lattice, apple/cherry leaves and saplings, kitchen sink, stackable log and the wine storage blocks. **The placeholders show in game until written.** Vinery's own grey lines on those blocks are kept.
- **JEI:** patcherMod's four 1.3.10 pages were removed 2026-10-27 ([vinery_jei.md](vinery_jei.md)); the port ships its own.
- **Plant oil:** every grape seed squeezes into 50 mB of plant oil in the IE Squeezer at 6400 energy ([oil.js](../kubejs/server_scripts/engineering/oil.js)).
- **Composting:**
  - grapes and seeds 0.4
  - grape and fruit crates 1.0
  - grapevine leaves 0.4
  - Everycomp / Immersive Weathering grapevine leaf carpet and pile

  Source: [compost_values.js](../kubejs/startup_scripts/compost/compost_values.js).
- **Canning:** `#creativeengineering:canning/grapes` (the six small grapes) and `canning/jungle_grapes`, because Vinery's own grape tags mix the two sizes ([cantags.js](../kubejs/data/creative_delight/kubejs/creativeengineering/can_tags.js)).
  - `creativeengineering:canned_grape` exists with 3 servings. Canned cherries are craftable from 6 cherries, or 4 + sugar/honey.
  - Grape canning status: see [food.md](food.md).
- **Loot:** chest loot packs and unpacks the apple, cherry, red grape and white grape crates against the fruit ([07_rules.js](../kubejs/server_scripts/loot/07_rules.js), [loot.md](loot.md)).
- **Recipes:**
  - Vinemaker Leggings accept white wool or IE hemp fabric.
  - The Everycomp cherry picture frame accepts wool, hemp fabric, paper or canvas.

  Source: [misc_mods.js](../kubejs/server_scripts/other/misc_mods.js), [fabric.js](../kubejs/server_scripts/engineering/fabric.js).
- **Tags:** `minecraft:barrel` added to `vinery:barrel` and `vinery:wooden_barrels` ([wood_tags.js](../kubejs/server_scripts/tags/wood_tags.js)).
- **BlockRunner surfaces** ([blocktags.js](../kubejs/server_scripts/tags/blocktags.js), [blockrunner.md](blockrunner.md)):
  - wood tier: cherry fence gate, cherry beams and floorboards, stackable log, crates
  - dirt tier: Vinery dirt/grass slabs and loam
- **BlockRunner display:** the taiga bush is listed as a 0.8 slow block in patcherMod `StuckInBlock` (display only).
- **Textures:** `vinery:item/mash` is the generic fallback texture for incomplete sequenced-assembly items ([transition_items.js](../kubejs/startup_scripts/changes/transition_items.js)).

## 4. Limits and decisions

- **JEI shows the wrong barrel time.** `VineryIds.FERMENTATION_TICKS = 1800` (hover "90 seconds") copies Vinery's dead constant. The real time is about 50 ticks (§2.3). Open; fix the constant and VJ-04 together.
- **Hopper jams were Vinery 1.3.10's.** The press output slot and every barrel slot took hopper input.
  - Gone with the 1.5.5 port: every slot takes only what a recipe uses there and outputs are take-only (B7.2).
  - The jams the port still had (cask ingredient overflow and mixed recipes, other-type juice, mismatched press
    inputs) are fixed in the port's face rules since 2026-10-20 ([alcohol.md](alcohol.md) 10.19, VB-93, VB-94).
- **Breaking a planted stem loses the seed.** Vinery behaviour, left as is. The stem and lattice tooltips say so since 2026-10-19 ([alcohol.md](alcohol.md) 10.15).
- **Seed tooltips removed rather than corrected.** The bush requirements now live here. A short pack line on the seeds can come later if wanted.
- **Placeholder tooltips** were written for the 1.5.5 port on 2026-10-19 ([alcohol.md](alcohol.md) 10.15). Bushes have no item of their own, so any bush text has to go on the seed items.
- **This file is a code reading of 1.3.10.** Numbers come from the decompiled jar, not from play. Re-check after a Vinery update (rerun §6).

## 5. File map

- Mod jar: `mods/letsdo-vinery-forge-1.3.10.jar`; config `config/vinery/config.json5`
- Pack tooltips: [kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js) (Vinery section, `removeTooltipKey`)
- JEI pages: none in patcherMod since 2026-10-27 ([vinery_jei.md](vinery_jei.md))
- Item keys: Vinery block of `global.item` in [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) (`i.grapevine_pot`, `i.wine_press`, `i.fermentation_barrel`, `i.grape_seeds_*`, ...)
- Pack scripts touching Vinery: [oil.js](../kubejs/server_scripts/engineering/oil.js), [compost_values.js](../kubejs/startup_scripts/compost/compost_values.js), [cantags.js](../kubejs/data/creative_delight/kubejs/creativeengineering/can_tags.js), [07_rules.js](../kubejs/server_scripts/loot/07_rules.js), [misc_mods.js](../kubejs/server_scripts/other/misc_mods.js), [fabric.js](../kubejs/server_scripts/engineering/fabric.js), [wood_tags.js](../kubejs/server_scripts/tags/wood_tags.js), [blocktags.js](../kubejs/server_scripts/tags/blocktags.js)
- Decompile helper: [tools/decompile_mod.py](../tools/decompile_mod.py)
- Vinery classes behind §2:
  - `block/GrapevinePotBlock`
  - `block/entity/WinePressBlockEntity`, `client/gui/handler/WinePressGuiHandler`
  - `block/entity/FermentationBarrelBlockEntity`, `client/gui/handler/FermentationBarrelGuiHandler`, `recipe/FermentationBarrelRecipe`
  - `block/stem/{StemBlock,PaleStemBlock,LatticeStemBlock}`
  - `block/grape/{GrapeBush,SavannaGrapeBush,TaigaGrapeBush,GrapeVineBlock}`
  - `block/{AppleLeaves,CherryLeaves,KitchenSinkBlock,StackableLogBlock}`
  - `util/WineYears`, `mixin/LivingEntityMixin`, `registry/VineryEffects`, `registry/ObjectRegistry`

## 6. Offline checks

- **Re-read the code:** `python3 tools/decompile_mod.py mods/letsdo-vinery-forge-1.3.10.jar <scratch dir>`. It runs Vineflower, then renames SRG members to Mojang names. Done 2026-10-01: 159 classes.
- **Tooltip script syntax:** `node --check kubejs/client_scripts/tooltips.js`. Passed 2026-10-01.
- **Dict keys:** every `i.*` key used in the Vinery tooltip section resolves to a `vinery:` id in item_dict.js. Checked 2026-10-01.

## 7. In-game test suite

Tooltip tests are TT-14 and TT-15 in [tooltips.md](tooltips.md). JEI tests are VJ- in [vinery_jei.md](vinery_jei.md).

**Machines**
- [ ] **VN-01 Pot fill and stomp:** put 6 red grapes in a Grapevine Pot one at a time → the floor drops on the 6th. A 7th grape is refused. Jump on it 3 times → 2 empty wine bottles give 2 red grape juices, and the pot is empty afterwards.
- [ ] **VN-02 Pot type lock:** 1 white grape, then 5 red → the juice is white.
- [ ] **VN-03 Mob stomp:** fill a pot, push a mob onto it from 1 block up 3 times → the pot reaches the bottling stage.
- [ ] **VN-04 Press timing:** 10 apples in the Apple Press → 10 Apple Mash after about 36 s (3.6 s each).
- [ ] ~~**VN-05 Press hopper jam**~~ retired 2026-10-20: 1.3.10 behaviour; the port refuses junk, see VB-93.
- [ ] **VN-06 Barrel time:** fill an Aging Barrel with a valid recipe → the wine appears in about 2.5 s, not 90 s.
- [ ] **VN-07 Barrel output stacks:** 8 wine bottles and 8 of each ingredient, leave the wine in the output → it keeps crafting and the output stack grows to 8 with no one taking it. A wine with a different `Year` in the output stops it.
- [ ] ~~**VN-08 Barrel hopper**~~ retired 2026-10-20: 1.3.10 behaviour; the port refuses junk, see VB-94.

**Growing**
- [ ] **VN-09 Stem pair:** place a stem with 2+ in hand on grass → 2-high stem, 2 used. Red seeds on the lower stem are refused; on the upper stem they plant.
- [ ] **VN-10 Stem harvest:** grow to stage 4 → right-click drops 3-4 grapes and the stem shows the stage-2 model. Shears on a planted stem drop the seed back and keep full durability.
- [ ] **VN-11 Stem seed loss:** break a planted stem by hand → no seed drops.
- [ ] **VN-12 Taiga seed on stem:** taiga seeds on an upper stem with no podzol, coarse dirt or grass nearby → plants and grows.
- [ ] **VN-13 Lattice bone meal:** jungle seeds on a single lattice → bone meal does nothing. On a lattice with another lattice below → it grows.
- [ ] **VN-14 Taiga bush grass:** taiga seeds on dirt with only grass blocks nearby (no podzol or coarse dirt) → it can be planted.
- [ ] **VN-15 Savanna light:** savanna seeds under a 1-block roof at night with no torches → can't be planted.
- [ ] **VN-16 Bush seed drop:** break a stage-1 bush → nothing. Break a stage-3 bush → 1 seed.
- [ ] **VN-17 Sterilized vine:** shear a jungle grape vine → it stops ripening and spreading (watch several minutes). Shear again → it resumes.
- [ ] **VN-18 Placed leaves:** place apple leaves in survival and wait → no apples ever. Fruiting leaves on a sapling-grown variant tree regrow apples after picking.

**Aging and utility**
- [ ] **VN-19 Aging:** take a fresh wine and note its potency, then `/time add 1536000` (64 days) → its tooltip potency is one level higher.
- [ ] **VN-20 Sink:** place a Kitchen Sink (creative) → an empty-hand click fills it. A bucket gives a water bucket. An empty hand refills it, so it's endless.

## 8. History

- **2026-10-01:**
  - Writeup created from the decompiled 1.3.10 jar (`tools/decompile_mod.py` added).
  - Grape and grape seed flavour lines removed. `removeTooltipKey` takes a `*` prefix key. Vinery tooltip placeholders added.
  - Found the 50-tick barrel time (the JEI page says 1800, open).
- 2026-10-19: this file describes 1.3.10, which left the pack in 1.0.7 (DEPLOYED 2026-10-18) for the port; the port's
  behaviour is in [alcohol.md](alcohol.md) Part B. Kept as the reference for what changed.
