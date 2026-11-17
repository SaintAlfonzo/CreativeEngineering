# Alcohol: Brewin' & Chewin' and Vinery backports

> **Creative Brewing (2026-10-31):** both ports are now members of the Creative Brewing bundle: sources in
> `development/creativeBrewing/members/vinery` and `.../members/brewin` (Gradle projects `:backportedVinery`, `:backportedBrewin`
> unchanged), shipped only inside `creativebrewing-<v>.jar`, display names "Vinery (Creative Brewing)" and "Brewin' &
> Chewin' (Creative Brewing)" (`foundation`: "Foundation (Creative Brewing)"; both renamed 2026-11-04, plan_cb_publish.md M5). Vinery `-08` and Brewin' `-05` are the first nested builds (built, not installed). Paths
> `development/backportedVinery` / `development/backportedBrewin` below mean those folders. See [creative_brewing.md](creative_brewing.md).

> **Merge note (2026-10-25):** this port is to be merged with the other Farmer's Delight add-on backports into a single jar, **Creative Delight**. The inventory summary, cross-port interactions and the merge process are in [creative_delight.md](creative_delight.md). This file stays the detailed record for Brewin' & Chewin' (Part A) and Vinery (Part B); its registry namespace and test prefix do not change.

Two backports live in this file:

- **Part A (sections 0 to 10, tests `AL-`): Brewin' & Chewin'.** A full port of 4.5.0+1.21.1 as "Backported Brewin";
  `-02` DEPLOYED with pack 1.0.8 (2026-10-19) in place of the 2.0 jar; `-03` (10.13, 10.16, 12) shipped in 1.1.01
  (deployed 2026-10-22). `-04` (effect icons on the HUD for the food effects, TODO `potions/effects #8`) installed in
  `mods/` 2026-10-31 00:30, not yet booted.
- **Part B (sections alcohol_part_b.md 0 to alcohol_part_b.md 8, tests `VB-`): Let's Do Vinery.** The pack ships Forge 1.3.10; ported from Fabric
  1.5.5 (Minecraft 1.21.1) as "Backported Vinery"; `-02` DEPLOYED with pack 1.0.8 (2026-10-19) in place of 1.3.10,
  `-03` (alcohol_part_b.md 7.14, 10.9 to 10.14) and `-04` (village houses, section 13; Grape Planting JEI page) shipped in 1.1.01
  (deployed 2026-10-22); `-05` (sheared grass slab) in 1.1.04 (client 2026-10-29, server still 1.1.03). `-06` (crop
  seasons for grapes and fruit leaves, Jellie Wine's four effects, Double Jump fix) installed in `mods/` 2026-10-31 00:41,
  not yet booted.

# Part A: Brewin' & Chewin' (Backported Brewin)

This pack shipped Brewin' & Chewin' 1.19-2.0 (Forge 1.19.2) up to 1.0.7. It now ships a **full port** of 4.5.0+1.21.1,
built as our own Forge 1.19.2 jar ("Backported Brewin"). Where the two versions differ, the port follows 4.5.0 unless a line below
says otherwise. This part holds the gap analysis (sections 2.1 to 2.6), the port plan (section 3) and the decisions
(section 4). Both versions are MIT (Umpaz).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=618680dfe078 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 28 open TODO rows (Planned 5, Pinned 1, In Progress 3, Testing 19), 5 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `alcohol/pack #3` | Planned | S C P T | Vintage brews |
| `alcohol/vineryport #4` | Planned | none | Upstream jars on publish |
| `alcohol/vineryport #5` | Planned | C | I25 tool reruns and `#missing` models |
| `alcohol/vineryport #22` | Planned | C | Cellar mechanic audit and player docs |
| `licence/cb #3` | Planned | S | Rewrite the grape bush worldgen features |
| `alcohol/vineryport #3` | Pinned | S C P T | Wine effect reshuffle (7.4) |
| `alcohol/brewinport #4` | In Progress | S~ P | Pack-side leftovers |
| `merges/materials #4` | In Progress | S P | Crate and bag merge |
| `pack/release #5` | In Progress | S | `/dumpall` refresh with the ports |
| `alcohol/brewinport #1` | Testing | S+ C~ P | Brewin' & Chewin' 4.5.0 port |
| `alcohol/brewinport #2` | Testing | S C~ P | Heat / cold tag audit |
| `alcohol/brewinport #5` | Testing | C P | Black and purple texture ("plating") |
| `alcohol/brewinport #6` | Testing | S~ C P | Remainder buffer on the keg |
| `alcohol/brewinport #7` | Testing | S+ C P | Brewin' slots and side input |
| `alcohol/brewinport #8` | Testing | S+ C P | Coasters |
| `alcohol/pack #1` | Testing | S+ C P T | Rotten-food hunger type for alcohol |
| `alcohol/pack #2` | Testing | S+ P T | Wine boosts beyond the cellar (T5) |
| `alcohol/vineryport #1` | Testing | S~ C~ P | Vinery 1.5.5 port |
| `alcohol/vineryport #2` | Testing | S~ C~ P | Decisions built with the recommendation |
| `alcohol/vineryport #6` | Testing | C P | Grass slab biome tint |
| `alcohol/vineryport #7` | Testing | S+ C P | Hopper jams |
| `alcohol/vineryport #9` | Testing | S C P T | Grape stomping: 12 grapes, 4 stomps, 4 bottles |
| `alcohol/vineryport #13` | Testing | S C P | Duplicate blocks removed, "Cherry" names |
| `alcohol/vineryport #14` | Testing | S+ C P | Vinery village houses |
| `alcohol/vineryport #16` | Testing | C~ P | Vinery tooltips |
| `dishes/meals #3` | Testing | S+ C P | Sequenced assembly meals |
| `gameplay/blockrunner #2` | Testing | S C P T | Finish the BlockRunner audit |
| `textures/tools #1` | Testing | C | Wine texture dump |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Gap analysis 2.0 -> 4.5.0 | written 2026-10-14 from decompiles of both jars |
| Base fluid choice: honey / milk (4.1) | DECIDED 2026-10-14: the pack's `create:honey` and `minecraft:milk` |
| Kombucha base (4.5) | DECIDED 2026-10-14: `create_central_kitchen:green_tea` |
| Heat / cold block audit (2.7) | written 2026-10-14; applied 2026-10-19 with your list (10.13: furnaces, bonfires, braziers, running machines, fueled burners; snow, powder snow, IW thin ice / permafrost / cryosol) |
| Port (`development/backportedBrewin`) | `-01` shipped in pack 1.0.7. `backportedBrewin-4.5.0-ce-19.2-02.jar` BUILT 2026-10-18 with the section 8 port changes; offline data check passes (6). In `mods/` since 2026-10-18 evening and DEPLOYED with pack 1.0.8 (2026-10-19 10:20), where it replaces both `-01` and the 2.0 jar (1.0.7 shipped the two together; FML loaded `-01` on the server, [plan_1.0.7.md](plan_1.0.7.md) 2.2). Loaded in single player 2026-10-19, no errors. Still the live jar in 1.0.9. `-03` BUILT 2026-10-19 with 10.13 (keg heat / cold); rebuilt with 10.16, 12 and the 10.18 climate line and installed in the local `mods/` 2026-10-20, so Vinery's cold / warm storage is on locally. `-04` (HUD effect icons, `potions/effects #8`) installed 2026-10-31 00:30, not yet booted. Gaps and port decisions: 3.5 |
| Pack-side changes (3.4) | DONE 2026-10-18 except the rows marked open in 3.4. Compost and BlockRunner DONE 2026-10-19 (10.16); food types / stack sizes and the Kimchi can still wait on `/dumpall` in a game with the port |
| Changes to make (8) | DONE 2026-10-18 (port + pack), not tested in game. Q1 to Q5 built with my defaults (8.9) |
| Third list (10) | Brewin' side: 10.3, 10.4 BUILT 2026-10-18 (in `-02`, live); 10.13 and 10.16 BUILT 2026-10-19 (in `-03`, shipped in 1.1.01). Not tested in game |
| Alcoholic drinks (11) | BUILT 2026-10-20 (pack + Vinery `-03` rebuild), not tested in game |
| Coasters (12) | BUILT 2026-10-20 (Brewin' `-03` + Creative Common 1.0.10), shipped in 1.1.01; extended 2026-10-29 to 302 foods and drinks. Scratch server PASSED 2026-10-22 (SQ-OM-02), not tested in game |
| In-game tests | AL-01 to AL-96 drafted (7), none run |

- **Scope:** everything in 4.5.0 except EMI support, which this pack doesn't have.
- **Deviations from 4.5.0, by your choice:**
  - Chat, sign and name-tag scrambling happens only on clients, with client settings (4.3).
  - Dread Nog takes a poison potion in place of the ominous bottle, which 1.19.2 doesn't have (4.4).
  - No jams (8.5); Steel Toe Stout gives Resistance II for 90 s (8.6); the keg has 2.0's fluid slot pair (8.4) and a
    pipe rule (8.7). Recipe changes live in the pack, not the port (8.6).
- **Biggest change:** the keg becomes a fluid tank. A batch makes 1000 mB, poured out 250 mB per tankard, so 4 drinks
  per batch instead of 1.
- **Sizes:**
  - Old mod: about 5.6k lines.
  - New code to port: `common/` 7.9k, `client/` 2.4k, JEI 1.8k.
  - The 2.9k-line Fabric layer is a guide only; Forge equivalents get written in its place.
- **Source used for the port:** the Mojang-named NeoForge decompile in
  `DecompiledMods/BrewinAndChewin-neoforge-4.5.0+1.21.1-src` (code, `assets/`, `data/`).
- **Decompiling:** the decompiles live only in a session scratchpad.
  - Old jar: `python3 tools/decompile_mod.py mods/BrewinAndChewin-1.19-2.0.jar <out>`.
  - New jar: zip `DecompiledMods/BrewinAndChewin-fabric-4.5.0+1.21.1/umpaz` and run
    `java -jar ~/.cache/ce-tools/vineflower-1.10.1.jar <zip> <out>`. Names come out as Fabric intermediary
    (`class_1799` = ItemStack and so on).

## 1. Overview

4.5.0 is a rewrite, not a patch on 2.0. It adds only a handful of new items, but nearly every mechanic changes:

1. The keg holds a fluid tank, and two recipe types (`fermenting` and the new `keg_pouring`) replace the old one.
2. Tipsy no longer causes Nausea or Poison. Instead it holds back damage and deals it later as "cardiac arrest",
   spins the camera, sways the mouse, and scrambles chat, signs and name tags.
3. Two effects are new (Intoxication, Raging), one is gone (Satisfaction), and Sweet Heart is reworked.
4. Keg temperature reads a 5x5x5 area, and the new Heating Cask and Ice Crate blocks act as heat and cold sources.
5. Most drink and food stats are rebalanced. Every fermenting recipe changes its ingredients, time or temperature.

## 2. Reference: 2.0 vs 4.5.0

Every row is a change the port has to make (IDs are used in section 3).

### 2.1 Registry: items, blocks, fluids, effects

| # | Change | Notes |
|---|---|---|
| R1 | Add `heating_cask` block + item | In `#farmersdelight:heat_sources`. Makes steam particles; right-clicking it with a water bucket or water bottle vents steam and empties the container. Recipe: slabs / coal block, blaze powder, coal block / planks, magma, planks |
| R2 | Add `ice_crate` block + item | In `#brewinandchewin:freeze_sources` together with ice, packed ice and blue ice. Makes fog particles. Recipe: planks, string / slabs, packed ice / planks |
| R3 | Add `sweet_berry_jam`, `glow_berry_marmalade`, `apple_jelly` | `JamJarItem`: drink animation, honey sounds, gives back the bottle. Farmer's Delight cooking pot, 3 fruit + sugar, glass bottle as container |
| R4 | Rename `item_coaster` -> `coaster` | New `CoasterBlock`: holds up to 4 items (`size` 0 to 4), 16 rotations, an `invisible` state. The old coaster held 1 |
| R5 | Rename `scarlet_pierogies` -> `scarlet_pierogi` | 4.5.0 handles the rename with a DFU schema; the port uses a Forge missing-mappings remap |
| R6 | Register 18 fluids (+ flowing) | 16 drinks + `flaxen_cheese` + `scarlet_cheese`. Tank only: no bucket, no world block (4.5.0 `BnCFluidFabric` returns air for both). Honey and milk: see 4.1 |
| R7 | Remove `satisfaction` effect | Dropped; no remap (3.3, removed 2026-10-27) |
| R8 | Add `intoxication` effect | 2.4 |
| R9 | Add `raging` effect | 2.4 |
| R10 | Add damage type `cardiac_arrest` | 4.5.0 makes it bypass armor, cooldown, effects, enchantments, resistance, shield and wolf armor |
| R11 | Add particles `drunk_bubble`, `fog`, raging stages 1 to 4 | Tipsy's own particle, Ice Crate fog, one particle per Raging stack |
| R12 | Fluid textures, keg fluid render, coaster render | Assets in `DecompiledMods/.../assets/brewinandchewin`. Client setting `renderFluidInKeg` |

### 2.2 Keg

| # | Change | Old (2.0) | New (4.5.0) |
|---|---|---|---|
| K1 | Inventory | 9 slots: 4 ingredients, fluid item, drink display, container in, output, container out | 6 slots: 4 ingredients, container (4), output (5); plus a fluid tank |
| K2 | Tank | none | 1000 mB (common setting `keg.capacity`). Exposed as a fluid capability, so Create pipes and spouts can fill and drain it |
| K3 | Fermenting output | 1 drink item per batch, into the display slot | Fills the tank with the result fluid (1000 mB) and plays a sound. Food results (jerky, kimchi and so on) still go to the output slot |
| K4 | Base fluid | an item (water bucket, honey bottle, a drink item); its empty container goes to the container-out slot | a fluid in the tank. The tank amount must be an exact multiple of the recipe's `base_fluid.amount`; recipes without a base fluid need an empty tank |
| K5 | Pouring | right-click with the container; the GUI fills from the container slot | `keg_pouring` recipes (2.3), both in the GUI (container slot -> output) and in the world (right-click the keg with a container to take one serving; creative players keep the container). It also refills the tank from a filled item, so a beer tankard can be poured back in |
| K6 | Progress loss | resets to 0 when the recipe can't run; drops by 2 per tick when the keg is empty | drops by 20 per tick when the recipe can't run; resets when the recipe changes |
| K7 | Recipe lookup | tries the cached last recipe first | looks for a new match first, then falls back to the cached one |
| K8 | Item form | the keg item keeps the drink stack and its container | the keg item keeps the tank (`FluidTank` tag) and shows the fluid in its tooltip; stacks to 1 as before |
| K9 | Temperature area | 3x3x3, every tick | 5x5x5, every 80 ticks |
| K10 | Temperature sources | heat sources (lit or unlit), plus a heat source two blocks below with a heat conductor between; freeze sources in any state | heat and freeze sources, both with the lit check; the heat-conductor rule is gone |
| K11 | Biome / dimension | biome temperature <= 0 counts as cold +2; temperature 2.0 counts as heat +2; ultra-warm dimensions force 10 | +-1 instead of +-2 (setting `biomeTemp`); ultra-warm dimensions add +2 (setting `dimTemp`) |
| K12 | Temperature levels | raw < -4 Cold, < -1 Chilly, < 2 Normal, < 5 Warm, else Hot | thresholds are settings; defaults: <= -2 Cold, -1 Chilly, 0 Normal, 1 Warm, >= 2 Hot. One ice block makes a keg Chilly |
| K13 | Temperature matching | recipe temperature 3 means "any"; every other value needs an exact match | ranges: 1 needs Cold; 2 Cold or Chilly; 3 Chilly to Warm; 4 Warm or Hot; 5 Hot |
| K14 | GUI | fluid item slot, drink display | tank gauge with a fluid tooltip (`KegTooltip`), unit shown in mB or L (client setting) |
| K15 | Recipe book | Drinks / Meals tabs | the same two tabs, setting `recipe_book.enabled`, and a packet that syncs book settings to the client |

### 2.3 Recipes

**Recipe types**:

- `brewinandchewin:fermenting` fields:
  - `ingredients` (up to 4)
  - optional `base_fluid {ingredient, amount}`
  - `result`: a fluid stack `{id, amount}` or an item stack
  - `fermenting_time` (default 9600), `temperature` (default 3), `experience` (default 0), `category`, `unit`
- `brewinandchewin:keg_pouring` (new) fields:
  - `fluid {id, amount}`
  - optional `container` (default: the output's container item)
  - `output`
  - `strict`: the item's NBT must match
  - `can_fill`: whether the output can be poured back in; false for cheese
- `brewinandchewin:create_potion_pouring` (new, Create only): fills bottles, splash bottles and lingering bottles from
  `create:potion`, keeping the potion NBT.

**4.5.0 pouring recipes:**
- Each of the 16 drinks: 250 mB -> 1 tankard.
- Cheese: 1000 mB + a honeycomb -> an unripe wheel, and it can't be poured back in.
- Water bucket, water bottle (250 mB, strict), milk bucket, milk bottle, honey bottle.

**Fermenting, old vs new.** Time is in ticks; T is the temperature, 1 Cold to 5 Hot.

| Result | Old: inputs / base / T / time | New: inputs / base / T / time |
|---|---|---|
| Beer | 3 wheat / water bucket / 3 / 12000 | wheat, wheat seeds, brown mushroom / 1000 water / 3 / 9600 |
| Vodka | 3 potato, wheat / water / 3 / 12000 | potato, wheat, wheat seeds / 1000 water / 3 / 9600 |
| Mead | 2 wheat, sweet berries / honey bottle / 3 / 12000 | wheat, wheat seeds, sweet berries / 1000 honey / 3 / 9600 |
| Rice Wine | 4 rice / water / 3 / 12000 | rice, brown mushroom / 1000 water / 3 / 9600 |
| Egg Grog | 2 egg, sugar, cabbage leaf / milk bucket / 3 / 12000 | egg, sugar, cabbage / 1000 milk / 3 / 9600 |
| Glittering Grenadine | 2 glow berries, glow ink, glowstone / water / 2 / 12000 | glow berries, glow ink, glowstone / 1000 water / 2 / 9600 |
| Strongroot Ale | beetroot, potato, carrot, brown mushroom / beer / 3 / 1200 | beetroot, potato, brown mushroom, jerky / 1000 beer / 3 / 4800 |
| Saccharine Rum | melon slice, 2 sugar cane, sweet berries / mead / 4 / 1200 | melon (block), sugar cane, sweet berries / 1000 mead / 4 / 4800 |
| Pale Jane | honey bottle, lily of the valley, sugar, tree bark / rice wine / 4 / 1200 | same / 1000 rice wine / 4 / 4800 |
| Salty Folly | 2 dried kelp, sea pickle, seagrass / vodka / 2 / 1200 | dried kelp, sea pickle, seagrass / 1000 vodka / 2 / 4800 |
| Steel Toe Stout | crimson fungus, iron ingot, nether wart, wheat / strongroot ale / 1 / 1200 | same / 1000 strongroot / 1 / 4800 |
| Bloody Mary | 2 tomato, sweet berries, cabbage leaf / vodka / 4 / 1200 | tomato, cabbage, sweet berries / 1000 vodka / 4 / 4800 |
| Red Rum | crimson fungus, fermented spider eye, nether wart, shroomlight / bloody mary / 5 / 1200 | same / 1000 bloody mary / 5 / 4800 |
| Withering Dross | bone, ink sac, nether wart, wither rose / salty folly / 5 / 12000 | same / 1000 salty folly / 5 / 9600, xp 2.0 |
| Dread Nog | 2 egg, fermented spider eye, turtle egg / egg grog / 1 / 1200 | fermented spider eye, turtle egg, ominous bottle / 1000 egg grog / 1 / 4800. **Port: poison potion (4.4)** |
| Kombucha | 2 vegetables, black tea leaves, sweet berries / green tea / 3 / 6000 | carrot, black tea leaves, sweet berries / 1000 vodka / 3 / 9600. **Port: green tea (4.5)** |
| Flaxen Cheese | 2 brown mushroom, sugar / milk bucket / 4 / 12000 -> unripe wheel (honeycomb) | brown mushroom, pumpkin seeds, sugar / 1000 milk / 4 / 9600 -> 1000 mB cheese fluid |
| Scarlet Cheese | 2 crimson fungus, sugar / milk / 4 / 12000 -> unripe wheel | crimson fungus, nether wart, sugar / 1000 milk / 5 / 9600 -> cheese fluid |
| Cocoa Fudge | 2 cocoa beans, sugar / milk bucket / 5 / 12000 | same / 500 milk / 2 / 9600 |
| Pickled Pickles x2 | 2 sea pickle, glowstone / honey bottle / 2 / 12000 | 2 sea pickle, glow berries / 250 honey / 2 / 9600 |
| Jerky x3 | 3 raw meat / none / 3 / 12000 | 3 `#foods/jerky_meat` (raw meat or rotten flesh) / none / 4 / 9600 |
| Kimchi x2 | vegetable, cabbage leaf, kelp / none / 3 / 12000 | same / none / 4 / 9600 |
| Kippers | 2 raw fish, kelp / none / 3 / 12000, x3 | 2 safe raw fish, dried kelp / none / 4 / 9600, x2 |

All old recipes give 0.6 xp. New ones give 1.0, and Withering Dross gives 2.0.

**Since 2026-10-23** Kimchi and Pickled Pickles are no longer keg recipes: they are made in Vintage Delight's Pickling
Jar (`alcohol.js`, same inputs, 250 mB honey -> a honey bottle, 2400 ticks). Station split ([vintage_delight.md](vintage_delight.md)
S7): Pickling Jar for all pickling, keg for beer fluids, cask (the Vinery port's fermentation barrel) for wine fluids
that age. The wider keg / cask fluid audit is still open. Tests VTD-16.

**Other recipe changes in 4.5.0:**
- Cooking pot:
  - Cheesy pasta swaps cabbage for safe raw fish.
  - Creamy onion soup takes any cheese wedge plus a vegetable, in place of a second onion.
  - Horror lasagna drops the `horror_lasagna_meats` tag and takes any cheese wedge plus the scarlet one.
  - Every cooking recipe names a bowl as its container.
  - Inputs move to `c:` tags, which become `forge:` tags in the port.
- New crafting recipes: `pizza_from_slices`, `quiche_from_slices`, and both cheese wheels from 4 wedges. The pizza recipe
  now uses `#foods/pizza_topping` and tomato sauce, in place of a beef patty and a tomato.
- Knife slicing: breaking a pizza, a quiche or a ripe cheese wheel with a knife drops slices. 4.5.0 does this with a
  global loot modifier; on 1.19.2 the port uses a Forge global loot modifier as well.

### 2.4 Effects and drinks

**Tipsy, old (2.0):**
- Each drink gives Tipsy at its potency for N minutes. Drinking again adds half the duration and adds the potency to
  the level (cap X).
- Tipsy II and up adds Nausea and stops natural regen. V and up pushes you sideways at random. X adds Poison.
- When it runs out it drops one level and lasts 30 more seconds. Sleeping halves it, and a milk bucket removes one level.

**Tipsy, new (4.5.0):**

| # | Change |
|---|---|
| T1 | Remove Nausea, Poison, the random drift, the regen block, the one-level-at-a-time decay, and the sleep and milk-bucket handlers |
| T2 | Stacking: durations add up; the level goes up by potency + 1, cap X |
| T3 | Numbed hearts: damage taken while Tipsy is held back, up to floor((2 + 1.6 x level) / 2) x 2 HP (at least 1, at most max health - 2). The HUD shows those hearts as numbed. The damage lands as `cardiac_arrest` after 200 + 20 x level ticks, or when Tipsy ends. Needs per-entity data and a sync packet |
| T4 | The camera rolls and spins by the level (cap 11), with a culling fix; the mouse sways |
| T5 | Chat (including `/msg`), signs and name tags get scrambled from Tipsy IV. **Port: client only (4.3)** |
| T6 | A Farmer's Delight milk bottle removes Tipsy only after every other effect (`#low_priority/milk_bottle` tag); hot cocoa works the same way with its own tag |

**New and changed effects:**

| # | Effect | Behavior |
|---|---|---|
| E1 | Intoxication | Every drink gives it, hidden, and it lasts a little less than the drink's Tipsy. While it lasts, food gives no saturation (hunger still fills). AppleSkin / HUD overlay on the hunger bar. Undead can't get it (`#immune_to_intoxication`) |
| E2 | Raging | From Bloody Mary and Red Rum. Each melee hit at more than 80% charge adds a stack, up to 4. Each stack adds 5% attack speed, plus 2.5% per extra effect level, capped at 80% (multiplies the total). One stack drops every 2.5 x attack cooldown. Each stage has its own particles. Needs per-entity data and a sync packet |
| E3 | Sweet Heart (reworked) | Old: all healing x1.5, plus 0.5 per extra level, natural regen included. New: turns saturation into health, 1 HP every 20 >> level ticks |
| E4 | Satisfaction (removed) | Old: +1 saturation every 30 s (faster at higher levels), up to the food level. Rice Wine, Pale Jane, Kombucha and Kimchi used it |

**Drink effects, old -> new.** The level and duration are Tipsy's; "+" lists the drink's other effects.

| Drink | Old | New |
|---|---|---|
| Beer | I 8m | I 120s |
| Vodka | I 12m | I 180s |
| Mead | I 8m + Sweet Heart I 5m, 6 hunger | I 120s + Sweet Heart I 120s, no hunger |
| Rice Wine | I 5m + Satisfaction 5m | I 120s + Nourishment 60s |
| Pale Jane | I 5m + Satisfaction II 8m | I 180s + Nourishment 120s |
| Egg Grog | I 1m + Absorption 3m | I 120s + Absorption 30s |
| Glittering Grenadine | I 5m + Glowing, Night Vision 10m | I 120s + Glowing, Night Vision 30s |
| Saccharine Rum | II 8m + Sweet Heart II 8m, 6 hunger | II 180s + Sweet Heart I 180s |
| Salty Folly | II 10m + Water Breathing 8m | II 180s + Water Breathing 90s |
| Strongroot Ale | II 12m + Resistance 5m | II 120s + Resistance 30s |
| Steel Toe Stout | III 10m + Resistance II 10m | III 120s + Resistance 60s |
| Bloody Mary | I 12m + Comfort 5m | II 120s + Raging 60s |
| Red Rum | I 18m + Comfort II 8m | III 120s + Raging 120s |
| Withering Dross | III 20m + Wither 10m; 75% chance each of Blindness, Weakness, Slowness 10m | III 180s + Blindness 10s, Weakness 150s, Slowness 150s, Wither 60s |
| Dread Nog | III 5m + Bad Omen up one level (cap III) 10m | III 240s + Bad Omen 60 min, stacks up to V |
| Kombucha | I 5m + Satisfaction 3m, Haste 5m | I 120s + Haste II 60s |

Every drink also gives Intoxication. The new drinks last much shorter (Tipsy 2-4 min, old 5-20 min).

**Food stats, old -> new** (hunger / saturation modifier, effect):

| Food | Old | New |
|---|---|---|
| Pizza slice | 3 / 1.0 | 5 / 1.0 |
| Quiche slice | 3 / 0.8 fast | 4 / 0.8 fast |
| Vegetable Omelet | 12 / 0.75, Nourishment 4m | 12 / 0.8, Nourishment 3m |
| Cheesy Pasta | 12 / 0.8, Nourishment 4m | 14 / 0.75, Nourishment 5m |
| Creamy Onion Soup | 12 / 0.75, Comfort 4m | 12 / 0.8, Nourishment 3m |
| Scarlet Pierogi | 14 / 0.75, Nourishment 4m | 12 / 1.0, Nourishment 7m |
| Horror Lasagna | 14 / 0.75, Nourishment 6m | 16 / 0.55, Nourishment 5m |
| Fiery Fondue | 16 / 1.0, Nourishment 6m | 14 / 0.75, Nourishment 7m |
| Kimchi | 2 / 0.6, Satisfaction 2m | 2 / 0.6 |
| Pickled Pickles | 4 / 0.3, Night Vision 2m, Luck 5m | 4 / 0.3 |
| Kippers | 6 / 0.5, Water Breathing 30s | 6 / 0.5 |
| Cocoa Fudge | 4 / 0.8, Speed 20s | 4 / 0.8, Speed 40s |
| Jerky, cheese wedges, Ham and Cheese Sandwich | unchanged | unchanged |
| Jams (new) | none | Sweet Berry Jam 6 / 0.4, Glow Berry Marmalade 6 / 0.4, Apple Jelly 10 / 0.6 |

### 2.5 Tags, loot, compat, settings

| # | Change |
|---|---|
| O1 | Tags (4.5.0 `c:` tags become `forge:` in the port): `fermented_drinks`, `cheese_wheels/ripe` + `unripe`, `foods/cheese_wedge`, `foods/jerky_meat`, `foods/horror_meat`, `foods/pizza_topping`, `player_workstations/kegs`, `freeze_sources` (adds ice_crate), 18 fluid tags, `edible_when_placed`. Heat sources gain the cask and the fondue pot |
| O2 | Compostables: kimchi and pickled pickles 0.5, quiche slice 0.85, quiche 1.0 |
| O3 | JEI: one Fermenting page that shows the pouring step too, and one Cheese aging page in place of three. A recipe transfer button, with a server packet |
| O4 | Loot: when the keg breaks, the copy-drink loot function copies the tank, where it used to copy the drink slot. A `copy_meal` function and two loot conditions are added |
| O5 | Settings. Client: display unit, numbed-heart flicker, intoxication overlay, scramble switches, fluid render in the keg. Common: keg capacity and temperature thresholds, recipe book |
| O6 | Remove the old `VillagerEvents` helpers (2.0 never registered trades with them) |

### 2.6 Other mods that touch Brewin' & Chewin'

Found by scanning `mods/` for class and data references.

| Mod | What it does | Port impact |
|---|---|---|
| Create Central Kitchen 1.3.11.c | Mixin `common.brewinandchewin.KegBlockEntityMixin`, in a required config. It edits `KegBlockEntity.updateTemperature` so an empty Blaze Burner (heat level NONE) doesn't count as heat, and it reads the 2.0 "heat source two blocks below" local | The 4.5.0 method no longer has that local. The port renames the method so CCK's injectors match nothing (CCK sets no `defaultRequire`, so a miss only logs a warning), and builds the Blaze Burner rule into its own temperature scan (3.1 step 4). Check the log for the warning at first boot (AL-37) |
| Create Central Kitchen | `create_central_kitchen:green_tea` fluid (250 mB per Farmer's Respite green tea, Create emptying) | Kombucha base, 4.5 |
| Delightful 3.4.1 | `forge:cheese` item tag lists `flaxen_cheese_wedge` | no change |
| Salt 1.2.4.1 | `salt:can_be_salted` lists 8 B&C foods | no renamed ids, no change |
| Farmer's Respite 1.19-2.0 | no dependency on B&C | kombucha only |

### 2.7 Audit: keg heat and cold blocks

**Where the data comes from:** the pack's tag dump (`kubejs/exported/tags/block_tags.json`, last `/dumptags`). The
candidates come from scanning all 13,215 tagged blocks for hot and cold names, then dropping decoration (stairs, slabs,
bricks and so on).

**How the keg counts them (4.5.0, K9 to K13):**
- Each block in the 5x5x5 area that is in `#farmersdelight:heat_sources` adds +1 heat. Each block in
  `#brewinandchewin:freeze_sources` adds +1 cold.
- A block with a `lit` property only counts while it is lit. A block without one always counts.
- The default thresholds are: -2 or below Cold, -1 Chilly, 0 Normal, 1 Warm, 2 or above Hot. So 2 sources already make a
  keg Hot or Cold.
- **Port:** a Create Blaze Burner with `blaze=none` doesn't count, which replaces CCK's mixin (2.6). Blaze burners have no
  `lit` property, so without this rule an empty burner would always count.

**Heat sources today** (`#farmersdelight:heat_sources`, 38 blocks; FD's own tag plus mods that add to it):

| Group | Blocks | Lit-gated |
|---|---|---|
| Fire | `minecraft:fire`, `soul_fire`, `architects_palette:nether_brass_fire`, `mca:infernal_flame`, `creativemagic:dark_fire` | no |
| Campfires | vanilla campfire + soul campfire, 16 `tintedcampfires:*` | yes |
| Lava / magma | `minecraft:lava`, `lava_cauldron`, `magma_block` | no |
| Stoves and ovens | `farmersdelight:stove`, `nethersdelight:blackstone_stove` / `blackstone_furnace` / `blackstone_blast_furnace` / `nether_brick_smoker`, `endersdelight:endstone_stove`, `create_central_kitchen:blaze_stove`, `vinery:stove`, `vinery:wood_fired_oven` | yes where the block has `lit` |
| Blaze | `create:blaze_burner`, `create:lit_blaze_burner`, `quark:blaze_lantern` | no (the port gates the burner by heat level) |
| Port adds | `brewinandchewin:heating_cask`, `brewinandchewin:fiery_fondue_pot` | no |

**Cold sources today** (`#brewinandchewin:freeze_sources`, 7 blocks, from the 2.0 jar): `minecraft:ice`, `packed_ice`,
`blue_ice`, `snow_block`, `powder_snow`, `powder_snow_cauldron`, `snow`.

- **4.5.0 shrinks this tag** to `ice_crate`, `ice`, `packed_ice`, `blue_ice`, dropping all the snow blocks. Under the new
  thresholds, two snow layers within 2 blocks would make a keg Cold, and every keg in a snowy biome would start Cold before
  the -1 biome bonus is even counted.
- **Port:** follow 4.5.0 (no snow). The pack can add back `snow_block` and `powder_snow` (full blocks someone placed on
  purpose) and still leave out snow layers.

**Missing heat sources** (hot blocks in the pack that aren't tagged):

| Add? | Block | Why |
|---|---|---|
| yes | `minecraft:furnace`, `smoker`, `blast_furnace`, `quark:deepslate_furnace`, `quark:blackstone_furnace` | real fire, gated by `lit`. Nether's Delight furnaces are already in, so this matches them |
| yes | `decorative_blocks:bonfire`, `soul_bonfire` (tag `#decorative_blocks:bonfires`) | open fire; no `lit` property, always burning |
| yes | `decorative_blocks:brazier`, `soul_brazier` | open fire; gated by `lit` |
| yes | `blocky_siege:brazier_on` (not `brazier_off`) | separate on/off blocks, so tag only the on one |
| no | `immersiveengineering:coke_oven`, `blast_furnace`, `advanced_blast_furnace`, `alloy_smelter`, `arc_furnace`, `furnace_heater`, `blastfurnace_preheater` | industrial heat. The multiblock parts aren't lit-gated, so an idle oven counts. That's acceptable, or limit it to `furnace_heater` and `coke_oven` |
| yes | `createaddition:liquid_blaze_burner` | a blaze burner fed by liquid fuel; give it the same burner rule (check its heat property name) |
| no | `create_enchantment_industry:blaze_enchanter` | heated by its blaze |
| yes | `compressedcreativity:heater`, `create_dd:furnace_engine` | heaters by design |
| yes | `clayworks:kiln`, `cookingforblockheads:oven` | working ovens; gated by `lit` / active state where they have it |
| yes | `botania:blaze_block` | Blaze Mesh, a fuel block that glows hot |
| no | `handcrafted:oven`, `fantasyfurniture:*/oven` (7) | decorative furniture with no fire |
| no | boilers: `davebuildingmod:*_boiler` (~70), `design_decor:*_boiler*` (~40), `railways:*locometal_boiler` (~65) | decorative shells, no heat |
| no | `minecraft:coal_block`, charcoal blocks, `blocky_siege:fire_bomb_*`, fire coral, `botania:endoflame` / `exoflame` | not burning; named by color or theme only |

**Missing cold sources:**

| Add? | Block | Why |
|---|---|---|
| yes | `brewinandchewin:ice_crate` | 4.5.0 already has it in the tag |
| yes | `minecraft:frosted_ice`, `rare-ice:rare_ice` | already in `#minecraft:ice` |
| yes | `architects_palette:polished_packed_ice`, `chiseled_packed_ice` | packed ice variants (`forge:ices/packedice`) |
| no | `immersive_weathering:thin_ice`, `icicle` | real ice |
| no | `create_dd:freezing_sail` | Create D&D's freezing catalyst |
| maybe | `cookingforblockheads:fridge` | a fridge is cold by design; makes kitchen kegs Chilly |
| maybe | `minecraft:snow_block`, `powder_snow` | see the note above; full blocks only |
| no | snow layers, `architects_palette:coarse_snow`, `quark:permafrost`, `immersive_weathering:permafrost` / `cryosol` / `frosty_grass` / `frosty_fern` / `snowy_stone`, frozen prismarine | world ground; cold biomes already give -1 |
| no | `lightmanscurrency:freezer*` (16) | vending machines named "freezer" |
| no | `securitycraft:reinforced_*` ice and snow | protection blocks; tag them only if the plain ones count |

**Applied 2026-10-19 (10.13, plus the remaining "yes" rows the same night).** Left out: the block-entity-only
machines and the CFB oven (10.13 "Skipped"), and the fridge. Your list also added snow layers, IW thin ice,
permafrost and cryosol, which this table had as "no".

**Apply (as planned):**
- A pack `global.tags` block in `kubejs/server_scripts/tags/blocktags.js`, using the `#decorative_blocks:bonfires` and
  `#minecraft:ice` tags where they cover a group.
- The port's own data ships 4.5.0's tag unchanged.
- Rerun `/dumptags` afterwards and check the counts (AL-41).

## 3. Port plan

### 3.1 The mod

- **Where:** `development/backportedBrewin`, a subproject of the root Gradle build using the `ce.forge-mod` convention,
  the same way as `exposureBackport`.
- **Ids:** `mod_id=brewinandchewin`, the upstream id, so every resource location, recipe and tag ports unchanged and
  worlds keep their items.
- **Version:** `mod_version=4.5.0-ce-19.2-01`. Jar: `backportedBrewin-4.5.0-ce-19.2-01.jar`. It replaces
  `BrewinAndChewin-1.19-2.0.jar` in `mods/` and `server_mods/`.
- **License:** GPLv3 for our code; the upstream MIT notice goes in `NOTICE-upstream-MIT.txt` (license memory).
- **Dependencies:**
  - Required: Forge 43.3.x, Minecraft 1.19.2, Farmer's Delight 1.2.4. FD 1.2.4 has every FD class 4.5.0 uses:
    `SyncedBlockEntity`, `ConsumableItem`, `TextUtils`, `ItemUtils`, `ModEffects`, `HotCocoaItem`, `MilkBottleItem`, the heat
    source tags.
  - Optional: Create (honey, potion pouring, the Blaze Burner rule), JEI 11.8.1, AppleSkin 2.4.2, Farmer's Respite
    (kombucha), Create Central Kitchen (green tea fluid).

Build order. Each step should boot before starting the next:

1. **Registry**:
   - R1 to R12 and the new items.
   - Creative tab: on 1.19.2 this is an item group, not a registry entry.
   - Remaps (3.3): REMOVED 2026-10-27.
   - Assets copied from `DecompiledMods/.../assets`.
2. **Fluids**: 18 Forge `FluidType`s, tank only (no bucket, no block), with client extensions for the still and flow
   textures and tint. Honey and milk per 4.1.
3. **Keg**:
   - `KegBlockEntity` with a Forge `ItemStackHandler` and a `FluidTank` (K1 to K8), and the capability split as in
     4.5.0: items from the top in, from the sides and bottom out; fluid on every side.
   - Menu, screen, tank gauge and tooltip (K14). Recipe book (K15) using 1.19.2's `RecipeBookType` enum extension: the pack
     already patches RecipeBookType sync, see the RecipeBookType memory.
4. **Temperature** (K9 to K13): ForgeConfigSpec common settings keeping the 4.5.0 key names (`keg.cold` and so on). The
   Blaze Burner rule from 2.6 is folded in: when Create is loaded, a burner at heat level NONE doesn't count.
5. **Recipes**:
   - `fermenting`, `keg_pouring`, `create_potion_pouring` serializers, written for 1.19.2 JSON but with the same field
     names as 4.5.0.
   - The 4.5.0 recipe files with `c:` tags changed to `forge:`.
   - Dread Nog per 4.4, kombucha per 4.5.
6. **Effects**: Tipsy (T1 to T6), Intoxication (E1), Raging (E2), Sweet Heart (E3).
   - Forge capabilities `tipsy_hearts` and `raging`, with sync packets on a `SimpleChannel`, sent when a player starts
     tracking the entity and on login.
   - `cardiac_arrest` becomes a `DamageSource` with `bypassArmor().bypassMagic()`, also added to the pack's damage tag
     layer.
7. **Client**:
   - HUD: numbed hearts in the health bar, intoxication overlay in the hunger bar, the AppleSkin mixin.
   - Camera spin and mouse sway mixins (`GameRenderer`, `MouseHandler`).
   - Keg fluid render.
   - Coaster renderer.
   - Particles.
8. **Scrambling, client side only** (4.3).
9. **Loot**:
   - Block loot tables copy the keg tank.
   - Knife slicing as Forge global loot modifiers (2.3).
10. **JEI** (O3), compostables (O2), tags (O1).
11. ~~**Old-world conversion** (3.3).~~ REMOVED 2026-10-27 (pack_hygiene.md finding 9).

**API mapping, 1.21 -> 1.19.2 Forge:**

| 1.21 | 1.19.2 Forge |
|---|---|
| Data components (`BLOCK_ENTITY_DATA`, `potion_contents`) | ItemStack NBT (`BlockEntityTag`, `Potion`) |
| Fabric attachments / NeoForge data attachments | Forge capabilities on `LivingEntity` |
| Codec recipe serializers, `RecipeHolder` | JSON `RecipeSerializer`, `Recipe#getId` |
| Damage type registry + tags | `DamageSource` flags, plus the pack's damage tag layer |
| Fabric Transfer API / NeoForge fluid capabilities | Forge `IFluidHandler`, `FluidType` (exists in 1.19.2) |
| Custom payload packets | `SimpleChannel` |
| DFU schema for renames | `MissingMappingsEvent` |
| Codec JSON settings (`OrderCorrectedRecordCodec`) | ForgeConfigSpec toml, same key names |
| `c:` tags | `forge:` tags |

### 3.2 Things that stay as 4.5.0 even though the old version differs

- Tipsy durations and levels (2.4 tables), Intoxication, and the Sweet Heart rework.
- Keg temperature: the area, the thresholds and the range matching. Old kegs placed next to a single ice block will change
  level.
- Yield: 4 drinks per batch, 9600 or 4800 ticks.
- Recipe ingredients, including jerky in Strongroot Ale and the melon block in Saccharine Rum.
- Withering Dross gives 2.0 xp.
- Exceptions since 2026-10-18 (section 8): Steel Toe Stout's Resistance (II for 90 s, `BnCFoods`), the jams (gone), the
  keg's slots 6 and 7 and its pipe rule. The recipe tweaks are pack recipes over unchanged 4.5.0 data.

### 3.3 Old worlds

**REMOVED 2026-10-27** (CLAUDE.md "no old-version or cross-mod conversions"; [pack_hygiene.md](pack_hygiene.md)
finding 9). The port no longer converts Brewin' & Chewin' 2.0 content: `BnCRemaps` (the `item_coaster`,
`scarlet_pierogies` and `satisfaction` remaps), `BlockEntityLegacyIdMixin` (the `item_coaster` block entity id), the
2.0 keg inventory conversion (250 mB per drink, `BnCInventoryVersion`, the pending-drop list) and the 2.0 coaster
inventory / `legacySizeFix` are gone. A world last saved with the 2.0 jar loses those blocks, items and contents
(Forge's missing-mappings screen). The port has been in the pack since 1.0.7, so only worlds not opened since then
are affected. Ripening cheese wheels never needed a conversion (same block and `age` property).

### 3.4 Pack-side changes

| File | Change |
|---|---|
| [../kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) | DONE 2026-10-18: `i.coaster`, `i.scarlet_pierogi`, `i.heating_cask`, `i.ice_crate`, plus `i.t_cheese_wedge`, `i.t_pizza_topping`, `i.t_metal_dusts`. No jams (8.5). The horror lasagna meat keys stay: `cookingtags.js` fills that tag itself |
| [../kubejs/startup_scripts/_lib/fluid_dict.js](../kubejs/startup_scripts/_lib/fluid_dict.js) | DONE 2026-10-18: the 16 drink fluids and the 2 cheese fluids (`f.beer` ... `f.scarlet_cheese`) |
| [../kubejs/server_scripts/cooking/cooking.js](../kubejs/server_scripts/cooking/cooking.js) | DONE 2026-10-18: the pack's two pierogi recipes are gone (Q4); the beetroot change and the pizza, ham and cheese sandwich and quiche recipes moved here from `alcohol.js` 2026-10-20 (Prepared section; recipe ids unchanged). The dead pierogi recipes in the old commented-out copy were deleted the same day |
| [../kubejs/server_scripts/tags/cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js) | Kept as a pack tag (the shapeless horror lasagna uses it) |
| [../kubejs/generation_scripts/food/can_types.json](../kubejs/generation_scripts/food/can_types.json) | OPEN: Canned Kimchi still copies `brewinandchewin:satisfaction` from the 2026-10-02 food dump. 4.5.0 kimchi has no effect, so after a restart with the port: `/dumpfood`, then `python3 tools/food_catalog.py` and `python3 tools/gen_can_types.py` (hand edits would be overwritten) |
| [../kubejs/assets/creativeengineering/lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json) | DONE 2026-10-18: `tools/gen_effect_descriptions.py` rerun; it pruned the Satisfaction / Sweet Heart / Tipsy placeholders, which hid the port's own descriptions (it also pruned 10 stale non-Brewin placeholders) |
| `config/ftbquests/quests/chapters/progression.snbt` (line 2111) | DONE 2026-10-18 |
| `config/jei/recipe-category-sort-order.ini` + `configs_edited/root_config/jei/` | DONE 2026-10-18: JEI had already added `brewinandchewin:aging` at the end; moved next to `fermenting` in both files |
| [../tools/gen_stack_sizes.py](../tools/gen_stack_sizes.py), [../tools/food_catalog.py](../tools/food_catalog.py), `food_types.json` | OPEN: needs the `/dumpall` refresh with the port (no jams to add now) |
| Compost framework | DONE 2026-10-19 (10.16): `global.compost` clears the table, so the port's four 4.5.0 chances were lost; they are rows in `compost_values.js` now |
| BlockRunner | DONE 2026-10-19 (10.16): `heating_cask` and `ice_crate` in `wood`; `coaster` untiered (1 px thin) |
| Create spout / drain for tankards | DONE 2026-10-18 (8.7) |
| Deploy | DONE 2026-10-18: mod_sides.md row, `backportedBrewin` in build_deploy.py `IN_TREE_MODS`. `server_mods/` still holds the 2.0 jar: it is rebuilt by `tools/dump_server_mods.py` from `mods/`, so rerun that after the jar install. Manifest and `/dumpall` refresh still to do |

### 3.5 Port status (2026-10-18)

Built from the NeoForge decompile, class for class where 1.19.2 allows (133 files, about 10.5k lines). Package layout
follows upstream: `common/` and `client/` as 4.5.0, `forge/` in place of `neoforge/`, `integration/jei/`.

**Done:** every registry (blocks, items, 18 tank-only fluids + Forge fluid types, effects, block entities, menu,
particles, recipe types and serializers, loot functions, slicing loot modifier); the config; the keg (tank, pouring,
temperature), keg screen, keg recipe book (items), keg item tooltip; Tipsy numbed hearts and cardiac
arrest, Raging stacks, Intoxication (saturation block, undead immunity, HUD bar, AppleSkin preview), Sweet Heart, the
milk-bottle low-priority rule; camera spin and mouse sway; chat, `/msg`, sign and name-tag scrambling (client side,
4.3); coaster renderer; particles; JEI (Fermenting page with pouring, Cheese aging page, clickable tank, transfer
button); all assets and data through `tools/port_resources.py`.

**Not ported yet (each works in a reduced form):**

| Upstream | What the port does instead |
|---|---|
| `KegStackedContents` / `KegPlaceRecipe`: the recipe book also fills or empties the tank from fluid containers in the inventory | not ported: the pack removes the vanilla recipe book (your call, 10.3) |
| JEI `FermentingTransfer` + `JEITransferKegRecipeServerboundPacket` | PORTED 2026-10-18 as `KegTransfer` (10.3) |
| AppleSkin hunger-bar wobble while Intoxicated | PORTED 2026-10-18 (10.4) |
| Tipsy camera: a tiny positional drift on top of the spin | PORTED 2026-10-18 (10.4) |

**Port decisions, where 1.19.2 forced a different shape:**

- Config: `brewinandchewin-common.toml` ([keg], [recipe_book]) and `brewinandchewin-client.toml`, ForgeConfigSpec,
  upstream key names. Common settings aren't synced to clients (upstream syncs them); the pack ships one file.
- Per-entity data (Tipsy hearts, Raging) is one Forge capability on every LivingEntity, synced with the upstream packets
  on login and start-tracking. Upstream's recipe-book settings packet is dropped: 1.19.2's own recipe packet already
  carries every RecipeBookType.
- Cardiac arrest is a `DamageSource("brewinandchewin.cardiacArrest")` with bypass-armor and bypass-magic (no damage type
  registry in 1.19.2). `triggers_raging` (mob_attack, player_attack) is a melee check in `BnCDamageSources`.
- Numbed health holds back the damage after armor, enchantments, effects and absorption (1.19.2's LivingDamageEvent).
  Upstream subtracts those reductions a second time.
- Effect particles: 1.19.2 effects have no particle of their own, so `LivingEntityMixin` swaps some of the swirl for
  drunk bubbles or Raging particles. Clients only know their own player's effects (and synced Raging stacks), so other
  players show the plain swirl for Tipsy.
- Camera spin goes through Forge's camera-angle event (culling follows by itself); mouse sway wraps
  `LocalPlayer#turn`; numbed hearts count the heart index on the container call (no local capture), so the mixins stay
  safe without a local variable table.
- Scrambling: the server mixins only announce the sender's Tipsy level and a seed (`broadcastChatMessage`, `/msg`); the
  client scrambles in Forge's `ClientChatReceivedEvent` (no chat-listener mixin, no "modified" tag), name tags in
  `RenderNameTagEvent`, signs by building scrambled lines each frame (the sign's line cache stays clean).
- "Place a temperature block near a keg" is awarded from code (`BnCCommonEvents`): its 1.21 triggers take loot
  conditions, which 1.19.2's `placed_block` can't. Upstream's two loot conditions served only this, so they aren't ported.
- Coaster models are extra baked models drawn quad by quad with the texture-modifier tint (no wrapped model or color
  handler). Fluid item displays copy NBT keys (`copy_nbt`) where upstream remaps data components.
- Create Central Kitchen's required keg mixin (2.6) was checked against the bytecode: both its injectors find their
  targets in the port's `updateTemperature` (first `Level#getBlockState`, second `BlockState#is(TagKey)` with a second
  `BlockState` local), so it applies with no change in behaviour.
- Upstream bugs fixed on the way: a part-full keg overfilled when poured into; the recipe-book filter flag was sent as
  "open"; the JEI page crashed for a fluid result without a base fluid; fluid item displays kept stale entries across
  reloads; kombucha had no fluid tag although its display names one.
- One Farmer's Delight 1.21 texture (`block/tray`, MIT) ships as `brewinandchewin:block/fd_tray` for the coaster tray;
  1.21 pie template models point at FD 1.2.4's `pie` models.

## 4. Decisions

### 4.1 Honey and milk: pack fluids vs B&C's own (DECIDED 2026-10-14: option A, the pack's fluids)

**What 4.5.0 does:**
- **Honey:** the shared code registers `brewinandchewin:honey` on both loaders, because neither Fabric nor NeoForge has a
  honey fluid.
- **Milk:** only the Fabric build registers `brewinandchewin:milk` (`fabric/registry/BnCFluidsImpl`). The shared code has
  no milk, so the NeoForge build uses the loader's milk.
- B&C's fluids are tank only (no bucket, no world block).
- Fermenting recipes match by tag (`#c:honey`, `#c:milk`), so any tagged honey or milk works as a base. Pouring recipes
  name the fluid by id (`brewinandchewin:honey` -> honey bottle, `brewinandchewin:milk` -> milk bottle / bucket).

**What this pack has:**

| | Honey | Milk |
|---|---|---|
| Fluid in the pack | `create:honey` (tags `forge:honey`, `forge:fluid/honey`; `f.honey` in fluid_dict) | `minecraft:milk`, the Forge milk Create switches on (tags `forge:milk`, `forge:fluid/milk`; `f.milk`) |
| Bottle / bucket | Create: honey bottle <-> 250 mB, honey bucket 1000 mB, placeable in the world | Create: milk bottle <-> 250 mB, milk bucket 1000 mB |
| Recipes that use the fluid by tag | Create mixing (gingerdough, honey candy), filling (honeyed apple, honey bottle), compacting; Create Factory fillings (3); Create Crafts & Additions honey cake | Create mixing (chocolate, tea, milkshakes), filling (milk bottle); FD pie crust mixing; Corn Delight batter; Create Factory cake paste; Crafts & Additions cake; CCK cake compacting |
| Recipes that use the fluid by id | `create_generators` limestone generator, Create Misc honey gun, the pack's bandage filling (`misc_and_things` via NCH), Vintage Improvements centrifuge output | the pack's pancakes mixing (`cooking.js:589`, `fluid.milk`) |

**Option A (recommended): the port uses the pack's `create:honey` and `minecraft:milk`.**
- One honey and one milk in the whole pack. Keg, Create tanks, pipes, spouts and every recipe above share them, whether
  they match by tag or by id.
- Pouring recipes point at `create:honey` / `minecraft:milk`. The amounts already match: 250 mB per bottle, 1000 mB per
  bucket.
- Honey piped into the keg from a Create honey tank just works; so does draining keg milk into a mixer.
- Milk matches what the 4.5.0 NeoForge build does anyway (it uses the loader's milk). Honey is the only place we leave
  4.5.0, and only because the pack already has a honey fluid.
- Cost: honey needs Create. The port only registers its own `brewinandchewin:honey` when Create isn't loaded, so the jar
  still works outside this pack.

**Option B: register B&C's own `brewinandchewin:honey` and `brewinandchewin:milk` (Fabric 4.5.0 as written).**
- Two honeys and two milks. A tank or pipe holding one won't take the other.
- Honey bottles poured into a keg become B&C honey. That honey can't feed `create_generators`, the honey gun or the
  bandage recipe, which name `create:honey`.
- A keg can't take Create honey and give honey bottles back, because pouring matches by id.
- Tagging B&C's fluids `forge:honey` / `forge:milk` fixes recipes that match by tag. It doesn't fix the by-id recipes,
  and JEI would list both fluids.
- B&C's fluids have no bucket or world block, so they can't leave a tank except through bottles.

**Option C (rejected): B&C fluids plus a converter (a pouring recipe or Create mixing between the two).** All of
option B's duplicates, plus an extra step for players.

The drinks and both cheeses stay B&C fluids in every option, since the pack has nothing to replace them. They are tank
only, as in 4.5.0.

### 4.2 Scope

Full port, everything in 4.5.0 except EMI (not in the pack). Upstream's Fabric code is only a guide; Forge equivalents
get written in its place (3.1).

### 4.3 Scrambling stays on clients (DECIDED 2026-10-19; differs from 4.5.0)

**4.5.0 is split between server and client.**
- On the server, `ServerGamePacketListenerImplMixin` checks the sender's Tipsy against the common setting
  `levelChatScramble`. It sends every player a `MakeNextPlayerChatTipsy` packet carrying the level and a seed. It also
  logs a scrambled copy to the server console ("Modified by Tipsy"). Clients get the original signed message and
  scramble it themselves.
- Sign and name-tag thresholds are common settings as well.

**The port:**
- Server: sends only the sender's Tipsy level and a seed with the next chat or `/msg`. It doesn't change, re-sign or log
  the message any differently, so the server log and chat signing stay untouched. Other players' effects aren't synced to
  clients, so the level has to travel with the message.
- Client settings (`brewinandchewin-client.toml`):
  - `scrambleChat`, `scrambleSign`, `scrambleName` (on by default, as in 4.5.0).
  - The three thresholds `levelChatScramble` / `levelSignScramble` / `levelNameScramble` (default 3, meaning Tipsy IV),
    moved here from the common settings.
- Each client decides on its own whether and how strongly to scramble. The same seed gives the same scramble on every
  client that has it on.
- Signs and name tags scramble based on the viewing player's own Tipsy, which the client already knows.

### 4.4 Dread Nog: poison potion in place of the ominous bottle (DECIDED 2026-10-19)

- The ingredient is a potion with `Potion` set to `minecraft:poison`, `long_poison` or `strong_poison`: three
  `forge:partial_nbt` alternatives in one slot, in place of `minecraft:ominous_bottle`.
- The keg returns the glass bottle. Vanilla potions have no container item, so the port adds a remainder for potion
  ingredients in its own ingredient-consume step. Without that the bottle would just disappear.
- Everything else as 4.5.0: fermented spider eye, turtle egg, 1000 mB egg grog, temperature 1, 4800 ticks.

### 4.5 Kombucha base (DECIDED 2026-10-14: green tea, differs from 4.5.0)

4.5.0 makes kombucha from vodka, and its recipe file uses the old `fermentingtime` key. That looks like a placeholder:
upstream has no tea fluid, and 2.0 used green tea. The pack has `create_central_kitchen:green_tea` (250 mB per Farmer's
Respite green tea through Create emptying). The port uses 1000 mB of that, with carrot, black tea leaves and sweet berries,
temperature 3, 9600 ticks. The recipe is only active when Farmer's Respite is loaded, as in both versions.

## 5. File map

- [../mods/BrewinAndChewin-1.19-2.0.jar](../mods/BrewinAndChewin-1.19-2.0.jar): the jar we ship today
- [../DecompiledMods/BrewinAndChewin-fabric-4.5.0+1.21.1/](../DecompiledMods/BrewinAndChewin-fabric-4.5.0+1.21.1/): the newer classes, data (`data/brewinandchewin/recipe/`, tags, loot) and assets
- [../tools/decompile_mod.py](../tools/decompile_mod.py): old jar -> Mojang-named source
- [../DecompiledMods/BrewinAndChewin-neoforge-4.5.0+1.21.1-src/](../DecompiledMods/BrewinAndChewin-neoforge-4.5.0+1.21.1-src/): Mojang-named 4.5.0 source, the base of the port
- [../development/backportedBrewin/](../development/backportedBrewin/): the port (Gradle subproject `:backportedBrewin`)
  - `src/main/java/umpaz/brewinandchewin/`: `common/`, `client/`, `forge/` (entry point, platform helper, events,
    network, fluid types, capability, slicing modifier, client setup and HUD), `integration/jei/`
  - [../development/backportedBrewin/tools/port_resources.py](../development/backportedBrewin/tools/port_resources.py): rebuilds `src/main/resources/{assets,data}` from the decompile (format conversion, the 4.1 / 4.4 / 4.5 deviations, the filter-button sheet)
  - [../development/backportedBrewin/tools/check_data.py](../development/backportedBrewin/tools/check_data.py): offline id check (6)
  - [../development/backportedBrewin/tools/gen_at.py](../development/backportedBrewin/tools/gen_at.py) + `tools/accesstransformer.official.cfg`: the access transformer (one entry, `Gui$HeartType`)
- Pack files to change: section 3.4

## 6. Offline checks

- `./gradlew :backportedBrewin:build`: compiles with no errors; the mixin annotation processor resolves every target
  (refmap checked by hand, 2026-10-18).
- `python3 development/backportedBrewin/tools/check_data.py`: every item, fluid and tag that the recipes, tags, loot
  tables, coaster models and fluid displays name exists in the pack's last dump or in the port (0 problems, 2026-10-18).
- Model references: every texture and parent the 189 models use exists in the port, Minecraft 1.19.2 or Farmer's Delight
  1.2.4 (checked once by script, 2026-10-18; the one leftover is upstream's unused `item/beer_tankard`).
- `port_resources.py` is a deterministic conversion, so the planned field-by-field compare against 4.5.0's recipes is
  covered by reading its rules; it prints a warning for anything it can't map (none).
- Still to do: a boot on a scratch server (scratch-server memory) with a log grep for mixin, recipe and remap errors.

## 7. In-game test suite (drafted, run once built)

- [ ] **AL-01 Keg ferment to tank:** 3 wheat, wheat seeds, brown mushroom + 1000 mB water at Normal -> after 9600 ticks the gauge shows 1000 mB beer, ingredients down by 1 each
- [ ] **AL-02 Pour in GUI:** tankards in the container slot -> 4 beers in the output, tank empty
- [ ] **AL-03 Pour in world:** right-click the keg with a tankard -> 1 beer, 250 mB less in the tank
- [ ] **AL-04 Pour back:** right-click with a beer -> tankard back, +250 mB
- [ ] **AL-05 Create pipe in/out:** pipe water from a Create tank into the keg, drain beer into a Create tank
- [ ] **AL-06 Honey from Create:** pipe `create:honey` into the keg -> mead ferments; pour a honey bottle out (option A)
- [ ] **AL-07 Milk:** milk bucket into the keg, egg grog ferments; milk bottle pours out
- [ ] **AL-08 Exact multiple rule:** 750 mB milk + cocoa fudge inputs (500 mB) -> doesn't run; 1000 mB -> runs twice
- [ ] **AL-09 Temperature: ice block:** one ice block within 2 blocks -> Chilly; glittering grenadine runs, beer still runs (3 covers Chilly)
- [ ] **AL-10 Temperature: Hot:** two lit campfires -> Hot; red rum runs, beer doesn't
- [ ] **AL-11 Heating Cask / Ice Crate:** each counts as a source; water bucket on the cask vents steam and empties the bucket
- [ ] **AL-12 Blaze Burner rule:** an empty Blaze Burner next to the keg adds no heat; a fed one does
- [ ] **AL-13 Keg item keeps tank:** break a keg with 500 mB beer -> item tooltip shows it; place -> still there
- [ ] **AL-14 Progress decay:** remove an ingredient mid-ferment -> progress falls 20 per tick
- [ ] **AL-15 Cheese pour:** 1000 mB flaxen cheese + honeycomb -> unripe wheel; can't pour it back; ripens over time
- [ ] **AL-16 Dread Nog:** poison potion (any of 3) + spider eye + turtle egg + egg grog at Cold -> dread nog; glass bottle comes back
- [ ] **AL-17 Kombucha:** green tea fluid via Create emptying -> kombucha ferments
- [ ] **AL-18 Tipsy stacking:** beer twice -> Tipsy II, durations added
- [ ] **AL-67 JEI fill (10.3):** empty keg at the right temperature, the ingredients and enough filled containers (e.g. water bottles or a water bucket) in the inventory, click [+] -> ingredients in the grid, the tank holds exactly one set, the empty containers are back in the inventory
- [ ] **AL-68 JEI shift (10.3):** shift-click [+] -> as many sets as the items and fluid allow, the tank a whole multiple of the recipe's amount
- [ ] **AL-69 JEI empty (10.3):** keg holding beer, a recipe on milk, empty tankards in the inventory -> the beer goes into tankards, then the milk goes in; without enough tankards the button says "Can't Empty Keg" and nothing moves
- [ ] **AL-70 JEI errors (10.3):** wrong temperature -> "Incorrect Temperature"; no fluid containers -> the fluid slot shows red; a full inventory -> "inventory full"
- [ ] **AL-71 Intoxicated wobble (10.4):** with AppleSkin, Intoxicated and holding food -> the saturation / food preview icons wobble; `intoxicationFoodOverlay = false` -> they don't
- [ ] **AL-72 Tipsy drift (10.4):** Tipsy V or higher, stand still -> the view slides slightly as it spins; Distortion Effects 0 -> no spin and no drift
- [ ] **AL-73 Lit only (10.13):** keg next to an unlit furnace -> normal; light it -> warm; same with a Decorative Blocks brazier and a bonfire on firewood
- [ ] **AL-74 Machines running (10.13):** keg within 2 blocks of an IE coke oven's center block -> normal while idle, warm while it cooks; IE furnace heater only while powered and heating; PneumaticCraft air compressor only while burning fuel
- [ ] **AL-75 Fueled blaze (10.13):** keg beside a blaze burner with no fuel -> normal; feed it -> warm; liquid blaze burner the same; blaze enchanter warm only with experience in its tank
- [ ] **AL-76 Cold blocks (10.13):** a temperate-biome keg ringed by snow blocks, thin ice, powder snow, permafrost or cryosol -> chilly/cold; a lit furnace beside them cancels one
- [ ] **AL-77 Tooltips (10.13, split 10.18):** furnace, brazier, blaze burner, campfire -> "Heats the surrounding environment" with "Useful for keg brewing and wine casking" on the next line; snow block, ice, permafrost -> "Cools the surrounding environment" and the same second line; keg -> "Sensitive to hot and cold blocks in the environment"; an FD cooking pot on a lit furnace still doesn't cook
- [ ] **AL-78 Tankard slot icon (10.16):** open an empty keg -> the container slot shows the gray tankard outline, not black and purple
- [ ] **AL-79 Leftover slot (10.16):** ferment a recipe with a honey bottle ingredient -> the glass bottle lands in the new slot right of the output, nothing pops out; it can't be placed into; shift-click moves it to the inventory
- [ ] **AL-80 Leftovers overflow and stall (10.16):** with the leftover slot full of glass bottles (64) -> the next bottle goes to the empty-container slot under the fluid slot; with that slot holding buckets instead (empty milk buckets through the fluid slot first) the next honey-bottle batch waits at 0 progress and resumes once the bottles are taken out; `stallWhenFull = false` -> it ferments and throws the bottle
- [ ] **AL-81 Old keg loads (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* a keg placed with `-02` holding ingredients, a tankard and a drink keeps all of them after the `-03` install
- [ ] **AL-82 Slot rules (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* dirt won't go in the ingredient grid or the container slot by hand or shift-click; a tankard goes in the container slot; a honey bottle goes in the fluid slot by shift-click
- [ ] **AL-83 Input from every face (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* hoppers into the side and the bottom feed wheat to the grid, tankards to the container slot and honey bottles to the fluid slot; a hopper on top still feeds the grid
- [ ] **AL-84 Leftovers by hopper (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* a hopper under the keg pulls finished items, empty bottles from the fluid slot and the leftover slot
- [ ] **AL-85 Drink food values (11):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-04).* hover Beer, Red Wine, the Bellini and Bottle of Wodka -> AppleSkin shows half a rotten (green) shank and no saturation; drink one at full hunger -> it can be drunk, hunger goes up by half a shank, saturation doesn't
- [ ] **AL-86 Intoxication everywhere (11):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-04).* drink Red Wine, the Bellini and Bottle of Wodka (one at a time, after milk) -> the hunger bar turns Intoxicated for 1:30, no Intoxication icon or particles; eat bread -> hunger fills, saturation doesn't
- [ ] **AL-87 Nausea (11):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-04).* Red Wine -> Nausea 0:05; Jellie Wine and Mojang Noir -> 0:10; the Bellini -> one Nausea 0:05 (not two); Beer -> no Nausea; Bottle of Wodka -> its own Nausea 1:00 only
- [ ] **AL-88 Tooltip name (11):** Beer, Vodka, the Bellini, Bottle of Wodka and a wine show Quark's gray saturation word as "Intoxicating Drink" (not "Disgusting Snack"); rotten flesh still says "Disgusting Snack"; the effect lines read "Intoxication (m:ss)"; a wine lists its own effect, then Intoxication and Nausea; `[alcohol] quarkLine = false` brings "Disgusting Snack" back
- [ ] **AL-89 Wine still a wine (11):** drink a wine in survival -> its own effect (aged level and duration), the empty wine bottle back; in creative the bottle stays; sneak-place still works; Apple Cider from Farmer's Delight unchanged (no rotten shank)
- [ ] **AL-90 Log (11):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-00, 44 of 44 drinks).* after the restart `logs/latest.log` has `[alcohol] food values set on 44 of 44 drinks` and no `[alcohol]` warnings
- [ ] **AL-91 Coaster takes modeled items only (12):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-02).* right-click an empty coaster with a Vinery wine, a Farmer's Respite tea and a stone block -> none goes on; with a beer, an apple and FD hot cocoa -> each goes on with its own model; `[coaster] taggedItemsOnly = false` in `brewinandchewin-common.toml` -> the wine goes on again, drawn flat
- [ ] **AL-92 Display Delight foods on a coaster (12):** put a Hamburger, Bacon and Eggs, a Cake Slice and a Corn Delight Taco on one coaster (plain right-click) -> each drawn with its Display Delight model, about bowl size, none of them a flat item, none sticking far past the tray
- [ ] **AL-93 Tooltip follows the tag (12):** "Placeable on a coaster" on the Hamburger and on a beer, not on a Vinery wine or a Farmer's Respite tea
- [ ] **AL-94 WTHIT on a coaster (12):** look at a coaster with beer, beer, apple -> body "Coaster: 3 / 4", "2x Beer", "Apple"; sneak-right-click with an empty hand (coaster put away) -> "Coaster: 3 / 4 (put away)"; an empty coaster -> "Coaster: 0 / 4"; `[coaster] wthit = false` in `creativeengineering-common.toml` -> no lines
- [ ] **AL-95 Old coaster contents (12):** a coaster that already held a Vinery wine before the change keeps it, draws it flat, and gives it back on a right-click
- [ ] **AL-96 Coaster log (12):** after the restart no "Failed to get model" or "coaster model JSON" errors from Brewin' in `logs/latest.log`
- [ ] **VB-64 Banner recipe (10.5):** any banner over a nether star, ringed by Jellie, Mojang Noir, Aegis, Apple, Cristel, Lilitu and Chenet wine (any age) -> one Vinery banner; JEI shows the recipe
- [ ] **VB-65 Stem WTHIT (10.6):** look at a planted grapevine stem -> "Growing: <grape>" plus the growth line; when ripe "Ripe: ready to pick"; a fresh red/white stem shows "Grapevine leaves still to grow" until its leaves appear; an empty lattice -> "Nothing planted"
- [ ] **AL-19 No nausea / poison:** Tipsy X -> no nausea, no poison, no drift, natural regen works
- [ ] **AL-20 Numbed hearts:** take damage while Tipsy -> hearts shown numbed, damage lands later as cardiac arrest (ignores armor)
- [ ] **AL-21 Camera spin and mouse sway:** visible from Tipsy I, stronger at higher levels
- [ ] **AL-22 Chat scramble, on:** player A Tipsy IV chats -> player B (scrambleChat on) sees scrambled text; server log shows the original
- [ ] **AL-23 Chat scramble, off:** player B sets scrambleChat false -> sees the original
- [ ] **AL-24 Client threshold:** B sets levelChatScramble 6 -> A at Tipsy IV shows plain to B
- [ ] **AL-25 /msg scramble:** same as AL-22 through `/msg`
- [ ] **AL-26 Sign and name-tag scramble:** at Tipsy IV, nearby signs and name tags scramble for the tipsy player only
- [ ] **AL-27 Milk bottle priority:** Tipsy + Speed, drink an FD milk bottle -> Speed goes first
- [ ] **AL-28 Intoxication:** drink, then eat bread -> hunger fills, saturation doesn't; AppleSkin overlay shows it
- [ ] **AL-29 Undead immune:** splash or give a zombie Intoxication -> doesn't apply
- [ ] **AL-30 Raging stacks:** Bloody Mary, hit a mob at full charge 4 times -> attack speed up, stage particles change, stacks fall off over time
- [ ] **AL-31 Sweet Heart:** with saturation and missing health -> heals 1 HP per second, saturation goes down
- [ ] **AL-32 Jams:** craft each in the cooking pot, eat -> bottle back, honey sound
- [ ] **AL-33 Coaster:** place up to 4 tankards on one coaster, rotate, take back
- [ ] **AL-34 Knife slicing:** break a pizza with a knife -> slices
- [ ] ~~**AL-35 Old world: coaster and pierogi:**~~ retired 2026-10-27 (old-world conversion removed, pack_hygiene.md finding 9). a 2.0 world with an item coaster and scarlet pierogies -> coaster block holds its item, pierogi item present
- [ ] ~~**AL-36 Old world: keg:**~~ retired 2026-10-27 (old-world conversion removed, pack_hygiene.md finding 9). a 2.0 keg holding 3 beers -> 750 mB beer in the tank
- [ ] **AL-37 CCK mixin:** boot log shows only the CCK warning, no crash
- [ ] **AL-38 JEI:** Fermenting page shows ferment + pour; Cheese aging page; transfer button fills the keg
- [ ] ~~**AL-39 Satisfaction remap:**~~ retired 2026-10-27 (old-world conversion removed, pack_hygiene.md finding 9). a 2.0 save with Satisfaction running -> loads with Nourishment, no error
- [ ] **AL-40 Dedicated server:** boots with the port, client joins, no RecipeBookType or registry desync kick
- [ ] **AL-41 Heat / cold tags:** after the 2.7 additions, `/dumptags` lists them; a lit furnace or bonfire next to a keg reads Warm, an unlit furnace doesn't count, a polished packed ice block reads Chilly, a snow layer doesn't count
- [ ] **AL-42 Boot log:** client and server boot with the port; no mixin apply errors (including CCK's keg mixin), no recipe parse errors for `brewinandchewin:`
- [ ] **AL-43 Recipe book:** in the keg, clicking a beer recipe places wheat, seeds and mushroom; the ghost shows the water gauge and the Normal bar
- [ ] **AL-44 JEI transfer:** the `+` button on a Fermenting page moves the items into the keg's four slots; the fluid slot doesn't block it
- [ ] **AL-45 Numbed hearts HUD:** at Tipsy III, take 6 damage -> 3 hearts show the numbed sprite, flicker in the last 4 s, then cardiac arrest lands; same with absorption hearts
- [ ] **AL-46 Signs sober up:** scrambled signs read normally again the moment Tipsy drops below the threshold
- [ ] **AL-47 /msg to two players:** a Tipsy sender's both echoes and both receivers' copies scramble the same way
- [ ] ~~**AL-48 Old coaster block entity:**~~ retired 2026-10-27 (old-world conversion removed, pack_hygiene.md finding 9). a 2.0 world's item coaster keeps its item after loading (no "Skipping BlockEntity" in the log)
- [ ] **AL-49 JEI effects on the fluid output (8.2):** Fermenting page for beer, hover the result tank -> Tipsy and Intoxication lines with the same durations as the beer tankard's tooltip; the flaxen cheese page shows none
- [ ] **AL-50 Tank tooltip effects (8.2):** keg holding beer, hover the tank in the GUI and the keg item in the inventory -> the same effect lines
- [ ] **AL-51 Cask / crate tooltips (8.3):** Heating Cask and Ice Crate show the keg source line; the cask also shows the stove line and the steam line
- [ ] **AL-52 Coaster placing (8.3):** "Placeable on a coaster" on a beer, a Vinery wine and an FD hot cocoa, not on a stone block; right-click a coaster with each -> placed, the beer with its own model; sneak-right-click a coaster holding a Display Delight food -> note what happens
- [ ] **AL-53 Keg fluid slot (8.4):** water bucket in the fluid slot -> tank +1000 mB, empty bucket in the slot below, output slot untouched; 4 milk bottles -> 1000 mB and 4 bottles below
- [ ] **AL-54 Keg hoppers (8.4):** side hopper with water buckets and tankards -> buckets to the fluid slot, tankards to the container slot; a hopper below takes the drinks and the empty buckets
- [ ] **AL-55 No missing texture (8.4):** with only the port in `mods/`, open the keg GUI, its recipe book and the JEI Fermenting page -> no black and purple anywhere
- [ ] **AL-56 Jams gone (8.5):** JEI search "jam", "marmalade", "jelly" -> only Fruits Delight's; `chef_of_the_ages` can still be earned
- [ ] **AL-57 Fermenting changes (8.6):** JEI shows the new inputs for flaxen cheese, pale jane (any bark), beer, mead, bloody mary, rice wine and steel toe stout (any metal dust), and not the 4.5.0 ones; one batch of each ferments
- [ ] **AL-58 Steel Toe Stout (8.6):** drink one -> Resistance II for 90 s
- [ ] **AL-59 Pierogi (8.6):** JEI lists one scarlet pierogi recipe (cooking pot, beetroot) and no `creativeengineering:` pierogi recipes
- [ ] **AL-60 Pizza (8.6):** crafts with 1 dough on the bottom row; the 3-wheat shape is gone
- [ ] **AL-61 Ham and cheese (8.6):** each of the four variants in the 8.6 table crafts with the listed count; bread slices work where listed
- [ ] **AL-62 Quiche (8.6):** mushroom quiche crafts with a whole cabbage; both sequenced assemblies run on a Create line
- [ ] **AL-63 Spout and drain (8.7):** spout 250 mB of beer into a tankard -> beer; drain a beer -> tankard + 250 mB; no spout or drain recipe for the cheese fluids
- [ ] **AL-64 Keg pipe rule (8.7):** a pump on a fermenting keg takes nothing; after the drink is done it pumps the drink out; piping lava in is refused; the keg tooltip shows the idle-only line; with `pipeDrainOnlyWhenIdle = false` the pump drains mid-ferment and the line is gone
- [ ] ~~**AL-65 Mead merge**~~ retired 2026-10-18 (no conversions); Vinery mead removal is VB-54

## 8. Changes to make (planned 2026-10-18, BUILT 2026-10-18)

**Status:** everything below is built: the port in `backportedBrewin-4.5.0-ce-19.2-02.jar` (in `mods/` and
`server_mods/` since 2026-10-18 evening), the pack side in KubeJS. Nothing tested in game. Each subsection ends with an **Outcome** line where
the build differs from or adds to the plan.

Your list from 2026-10-18, each item with a plan. **Port** means `development/backportedBrewin` (rebuild the jar);
**pack** means KubeJS, tags or tooltips. Recipes go through `global.NCH` and ids through `global.item` /
`global.fluid` (CLAUDE.md). Tests: AL-49 to AL-66 (section 7). Questions for you: 8.9.

**Suggested order:** 8.1, then the port changes (8.2, 8.4, 8.5, the stout effect in 8.6, the pipe rule in 8.7) in one
rebuild, then the pack changes (8.3, 8.6, 8.7) in one KubeJS pass.

### 8.1 First: one Brewin jar, and two log findings

- `mods/` holds both `BrewinAndChewin-1.19-2.0.jar` and `backportedBrewin-4.5.0-ce-19.2-01.jar`. Today's
  `latest.log` shows both found and the port loaded. Two jars with one mod id aren't a supported setup, and the 2.0 jar
  has its own `textures/gui/keg.png`. Remove the 2.0 jar (the 3.4 deploy row) before judging any keg UI or texture bug.
- **Cooking-pot recipes:** the port's 9 `farmersdelight:cooking` files have no `cookingtime`. FD reads them with its
  default of 200, but KubeJS logs "Failed to parse recipe ... Falling back to vanilla". So `R.replaceInput` and friends
  can't see inside them. Fix: `tools/port_resources.py` writes `"cookingtime": 200` into each one.
- **Tag error:** `#minecraft:undead` is a 1.21 tag. The port's copy of it fails to load ("missing following
  references"). Point it at the 1.19.2 entity list, or drop it if nothing reads it.
- **Outcome:** you had already taken the 2.0 jar out of `mods/`; it is still in the generated `server_mods/` folder
  (see the 3.4 Deploy row). `port_resources.py` now writes `cookingtime: 200` into the cooking-pot files
  and turns `#minecraft:undead` into the 13 vanilla undead mobs; `BnCCommonEvents` also treats any `MobType.UNDEAD` mob
  as immune, so modded undead are covered.

### 8.2 JEI: effects on the keg's fluid output (port)

- **Now:** on the Fermenting page, the result tank shows only the fluid name and amount. The effects show only on the
  poured tankard's item slot below it.
- **Plan:**
  - Add a tooltip callback to the result fluid slot in `FermentingRecipeCategory` (about line 174).
  - The callback appends the effect lines of the drink that the fluid pours into. That drink is
    `KegFermentingPouringRecipe#getOutput`, already found from the pouring recipe. The lines come from the same call
    the drink's own tooltip uses (`TextUtils.addFoodEffectTooltip`, `BoozeItem` line 148), so they read exactly like
    the tankard's tooltip.
  - Use the same callback for the keg screen's tank tooltip (`KegScreen` / `KegTooltip`) and the keg item's tooltip,
    so all three places agree.
  - Cheese fluids get no effect lines.
- **Test:** AL-49.
- **Outcome:** `BoozeItem.addFluidEffectLines(fluid, tooltip)` finds the drink by its fluid (every `BoozeItem` knows
  its fluid) and adds the same lines as the tankard. Used by the JEI result tank (visible-tank mode only; with
  `renderFluidInKeg = false` the tankard slot already shows them), `KegScreen`'s tank tooltip and `KegItem`.

### 8.3 Tooltips (pack)

Lines go in [../kubejs/client_scripts/tooltips.js](../kubejs/client_scripts/tooltips.js) with `addTooltips`, and in
[tooltips.md](tooltips.md) (update rule).

- **Heating Cask and Ice Crate** (you wrote "cooling cask"; the block is the Ice Crate):
  - Heating Cask: "Keg heat source within 2 blocks: one makes a keg Warm, two make it Hot". Also "Heats a cooking
    pot like a stove" (it is in `#farmersdelight:heat_sources`) and "Use a water bucket or bottle to vent steam".
  - Ice Crate: "Keg cold source within 2 blocks: one makes a keg Chilly, two make it Cold".
  - The numbers come from the 4.5.0 rules (K9, K12): a 5x5x5 area, each source counts 1, and Normal is 0. Biome
    and dimension bonuses shift this, so the line says "on its own".
- **Placeable on a coaster.** What the code does:
  - The coaster takes **any** item, up to 4 (`CoasterBlockEntity#addItem` has no check).
  - About 110 items have their own coaster model in the port's `assets/brewinandchewin/brewinandchewin/coaster/`.
    These are vanilla foods, Farmer's Delight meals and drinks, and every Brewin drink. Anything else is drawn as a
    plain item.
  - So it works with other mods' items (Vinery wines, Farmer's Respite teas), but only the modeled ones look placed.
- **Display Delight overlap.** A plain right-click on a coaster always goes to the coaster: the block's own use runs
  before the held item's. A **sneak** right-click with something in hand skips the block, so a Display Delight food
  (which places on sneak) would try to put a plate on top of the coaster. Vinery bottles place on a plain click
  anywhere except a coaster. AL-52 checks both.
- **Plan:**
  - Add a fifth line to the placeable pipeline: "Placeable on a coaster", tag
    `creativedelight:placeable_on_coaster`.
  - `tools/placeable_catalog.py` fills the tag from the coaster model files, plus a short allow-list: Vinery wines
    and juices, Farmer's Respite drinks.
  - `placeabletags.js` and `placeable_tooltip.js` gain the new row, and [placeable_food.md](placeable_food.md) gets
    the category.
  - Not every item: all items qualify, so a line on everything would be noise (8.9 Q3).
- **Tests:** AL-50, AL-51, AL-52.
- **Outcome:** as planned. The tool reads the port's 96 coaster model files and adds every Vinery and Farmer's Respite
  drink; on today's live `/dumpplaceable` that is 160 items, including non-food models (bowl, glass bottle, tankard,
  potion). Cask and crate lines are in tooltips.js "Food / Farmers Delight".

### 8.4 Keg UI (port)

- **A fluid slot on the left.**
  - **Now (4.5.0):** a water bucket or milk bottle goes in the container slot (4). It empties into the tank, and the
    empty container drops into the output slot (5), mixed in with poured drinks.
  - **Plan:** 2.0's layout comes back as two slots left of the 2x2 grid: fluid in (6) on top and empty container out
    (7) below.
    - Slot 6 takes only items that can fill the tank: a `keg_pouring` recipe with `can_fill`, or a Forge
      fluid-handler item.
    - The keg empties slot 6 through the existing `fluidExtractFromContainer` path. The empty container goes to
      slot 7, never slot 5.
    - Slot 4 then only takes empty containers, to pour drinks out.
  - **Hoppers (`SidedKegWrapper`):** from the side, filled containers go to 6 and empty ones to 4. From below, hoppers
    take from 5 and 7.
  - **Also changes:**
    - `keg.png` gets the two slot frames. The texture is Brewin's, under MIT.
    - Menu indices move, so check `KegMenu#quickMoveStack`, the recipe book placer and JEI's transfer handler.
- **Black and purple texture on the "plating" UI.** I couldn't find a missing GUI texture offline. Every texture the
  keg screen, JEI page and recipe book name exists in the jar, and the log has no missing `brewinandchewin` texture.
  Candidates, in order:
  1. The 2.0 jar still in `mods/` (8.1).
  2. A drink fluid without a still texture, drawn in the tank or the JEI gauge.
  3. The recipe-book filter button.

  Check again after 8.1. If it is still there, I need to know which screen "plating" is (8.9 Q1).
- **Tests:** AL-53, AL-54, AL-55.
- **Outcome:**
  - Slots: fluid in (6) at x 8, y 17 and empty container out (7) at x 8, y 53, in line with the player inventory's
    first column. The recipe-book button sits between them. `port_resources.py` stamps two copies of the container
    slot's frame into `keg.png`, plus a small down arrow that only shows when the recipe book is off.
  - What goes where is the keg inventory's own `isItemValid`: slot 6 takes items that pour into the keg (a filling
    pouring recipe, such as a beer or water bottle, or a Forge fluid container holding a fluid the keg uses); slot 4
    takes everything else. Menus, shift-click and hoppers all follow it. Side faces insert into 4 and 6 and extract
    from 5 and 7.
  - The keg empties slot 6 one item at a time on its server tick, only while the tank takes it and slot 7 has room.
  - The black and purple texture (Q1) wasn't reproduced offline; check again with only the port loaded (AL-55).

### 8.5 Removals

- **Jams, marmalade, jelly** (`sweet_berry_jam`, `glow_berry_marmalade`, `apple_jelly`). Fruits Delight covers them.
  - These are new in 4.5.0, so no 2.0 world has them. That means they can leave the port outright:
    - Unregister the 3 items and blocks (`BnCItems`, `BnCBlocks`, `JamJarItem`).
    - `port_resources.py` skips their files: cooking recipes, recipe advancements, models, blockstates, coaster
      entries, lang.
    - Take them out of the `chef_of_the_ages` advancement.
  - Then drop the 3.4 rows that add the jams to `item_dict.js`, food types and stack sizes.
  - Worlds opened with today's test jar get the missing-registry prompt once. That's fine for test worlds.
  - **Alternative:** keep them registered, then hide them and remove their recipes in KubeJS. Only worth it if a
    world you care about already has them.
- **Pickled pickles:** no change, as you said. Revisit with the veggie / vintage delight ports (cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19)).
- **Test:** AL-56.
- **Outcome:** done as planned (items, foods, `JamJarItem`, files, lang keys and the advancement criteria). Worlds
  opened with the `-01` jar show the missing-registry prompt once.

### 8.6 Recipes (pack, plus one port change)

- **Where:** a new [../kubejs/server_scripts/cooking/alcohol.js](../kubejs/server_scripts/cooking/alcohol.js) holds
  every Brewin and Vinery tweak (since 2026-10-20 the food recipes are in `cooking.js`, Prepared).
- **The port's data stays 4.5.0**, so `port_resources.py` reruns stay clean. Each tweak removes the upstream id and
  adds the pack recipe.
- **New NCH helper:** `R.bncFermenting(output, inputs, baseFluid, e, idOverride, opts)`. It writes
  `brewinandchewin:fermenting`: up to 4 inputs, a `{id, amount}` fluid or item result, and `base_fluid` with the
  `#tag` form. `opts` takes `temperature`, `time` and `xp`.
- **Fluid ids** come from `global.fluid` (the 3.4 row adds the 18 Brewin fluids).

| Recipe (upstream id) | 4.5.0 | Change |
|---|---|---|
| Flaxen Cheese (`fermenting/flaxen_cheese_from_milk`) | brown mushroom, pumpkin seeds, sugar / 1000 milk | brown mushroom x2, sugar (2.0's shape) |
| Scarlet Pierogi (`cooking/scarlet_pierogi`) | scarlet wedge, potato, dough, nether wart, cabbage | nether wart -> beetroot (`R.replaceInput` once 8.1 adds `cookingtime`). Delete the pack's two pierogi recipes, which were a 2.0 fix (cooking pot copy and the heatless shapeless one): done, removed from `cooking.js` with 1.0.8. See Q4 |
| Pale Jane (`fermenting/pale_jane_from_rice_wine`) | honey bottle, `farmersdelight:tree_bark`, lily of the valley, sugar | tree bark -> `i.any_bark` (`#immersive_weathering:bark`) |
| Beer (`fermenting/beer_from_water`) | wheat, wheat seeds, brown mushroom | wheat seeds -> dandelion |
| Mead (`fermenting/mead_from_honey`) | wheat, wheat seeds, sweet berries / 1000 honey | wheat seeds -> red mushroom. Vinery's mead is removed from that port (alcohol_part_b.md 7.6), so this is the only mead |
| Bloody Mary (`fermenting/bloody_mary_from_vodka`) | tomato, cabbage, sweet berries | sweet berries -> `i.salt` (`#forge:salt`) |
| Rice Wine (`fermenting/rice_wine_from_water`) | rice, brown mushroom | rice x2, brown mushroom |
| Steel Toe Stout (`fermenting/steel_toe_stout_from_strongroot_ale`) | iron ingot, crimson fungus, nether wart, wheat | iron ingot -> any metal dust, wheat -> blaze powder. New tag `creativeengineering:metal_dusts` through `global.tags`: the IE dusts plus the pack's grits (the `i.dust_<metal>` keys), as `i.metal_dust`. `forge:dusts` is too wide: it holds redstone, glowstone and salt |
| Steel Toe Stout effect (port, `BnCFoods.STEEL_TOE_STOUT`) | Resistance I, 60 s | Resistance II, 90 s (Q5). It's code, not data, so it goes in the port and in 3.2's list of deviations |
| Pizza (`pizza`) | pattern `f / ptp / www` | `f / ptp / " d "`: 3 wheat -> 1 `i.dough`. cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19) plans more dough and 8 slices later |
| Ham and Cheese Sandwich (`ham_and_cheese_sandwich`) | 2 bread, smoked ham, flaxen wedge -> 2 | See below |
| Quiche (`quiche_from_bacon`, `quiche_from_mushroom`) | shaped, mushroom one uses `cabbage_leaf` | Mushroom quiche takes `cabbage_leaf` or `i.cabbage` (whole). Add sequenced assembly, below |

**Ham and Cheese Sandwich** (my reading; Q6):

| Variant | Inputs | Output |
|---|---|---|
| Basic, shapeless | 2 bread slices (or 2 bread), any `i.cooked_pork` (`#forge:cooked_pork`: porkchop, bacon), flaxen wedge | 1 |
| Smoked ham, shapeless (upstream, kept) | 2 bread or 2 bread slices, `i.smoked_ham`, flaxen wedge | 2 |
| Sequenced assembly, cooked pork | `i.bread_slice` base; deploy cooked pork, flaxen wedge, bread slice | 2 |
| Sequenced assembly, smoked ham | `i.bread_slice` base; deploy smoked ham, flaxen wedge, flaxen wedge, bread slice | 4 |

- Note on slices: a sandwich side is one `moredelight:bread_slice`. A whole bread is also allowed, but it costs more
  for the same result.
- A smoked ham cuts into 2 cooked porkchops, so the ham recipes give double.

**Sequenced assembly** (Create deployer, like `grilled_salmon` in `cooking.js`). Each needs a row in
`startup_scripts/changes/transition_items.js`:
- `ham_and_cheese_sandwich` (texture `brewinandchewin:item/ham_and_cheese_sandwich`)
- `quiche_bacon` and `quiche_mushroom` (texture `brewinandchewin:item/quiche`)

The quiche steps:
- Base: `farmersdelight:pie_crust`.
- Spout 250 mB of milk (`createFilling` with `f.milk`).
- Deploy 2 eggs, a cheese wedge, cabbage or a cabbage leaf, then bacon or mushrooms.
- Result: 1 quiche.

This also ticks the "quiche: sequenced assembly" item in cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19).

- **Tests:** AL-57 to AL-62.
- **Outcome:**
  - [alcohol.js](../kubejs/server_scripts/cooking/alcohol.js) holds the Brewin changes; `R.bncFermenting` and
    `R.createEmptying` are in `neoCraftHelper.js`. Pack ids are `creativeengineering:fermenting/<upstream name>`.
    Base fluids are the exact fluids (`f.water`, `f.milk`, `f.honey`, `f.vodka`...), not tags.
  - Quiche assembly order changed: the bacon or mushrooms go on first, then milk, eggs, cheese, cabbage. With milk first
    both recipes start with the same step, and Create only ever picks one of them.
  - Pizza uses `i.dough` (`#forge:dough/wheat`). The ham and cheese "side" is one ingredient that takes a bread slice or
    a bread.
  - A node dry run of `alcohol.js` against the real `neoCraftHelper.js` produced the expected recipe JSON.

### 8.7 Fluids

- **Create spout and drain (pack):**
  - `R.createFilling` exists; add `R.createEmptying(outputs, input, e, idOverride)` to `neoCraftHelper.js`.
  - In `alcohol.js`, loop over the 16 drinks. Spout: tankard + 250 mB -> drink. Drain: drink -> tankard + 250 mB.
  - Leave out the 2 cheese fluids (they can't be poured back in 4.5.0 either).
- **Pumping in and out (port):**
  - The keg already gives its tank to every side as a plain Forge `FluidTank` (`KegBlockEntity#getCapability`), so
    pipes, pumps and IE pipes fill and drain it today (AL-05).
  - What's missing is a rule. A pump can pull the water you are brewing with out mid-ferment, and a pipe can push in
    any fluid.
  - **Plan:** wrap the capability.
    - Fill only fluids that some fermenting or pouring recipe uses.
    - Drain only while nothing is fermenting (progress 0), so a pump takes finished drinks, not the base.
  - **Config:** `brewinandchewin-common.toml` `[keg] pipeDrainOnlyWhenIdle = true`. False gives 4.5.0's
    drain-any-time.
  - **Tooltip:** the keg item, and the tank tooltip in the keg GUI, add a line saying "Pipes can only drain it while
    it isn't fermenting". The line only shows while the setting is on.
    - It lives in the port (`KegItem#appendHoverText`, `KegTooltip`), because it reads the config. Mention it in
      [tooltips.md](tooltips.md).
- **Tests:** AL-63, AL-64.
- **Outcome:**
  - Spout and drain: 32 recipes (`creativeengineering:createfilling/<drink>`, `createemptying/<drink>`).
  - Pipe rule: `KegPipeFluidHandlerForge` wraps the tank for every side. Fill: only fluids a pouring recipe pours or a
    fermenting recipe takes as its base. Drain, while `pipeDrainOnlyWhenIdle` is on (default): only while the keg isn't
    fermenting, meaning progress is 0 and the last recipe can't run with what's in the keg. So a keg holding water but
    no ingredients still gives its water up; once the ingredients are in, it doesn't. The keg GUI, the fluid slot and
    in-world pouring use the tank directly.
  - The tooltip line is `brewinandchewin.tooltip.keg.pipe_drain_idle`, added to `en_us.json` by `port_resources.py`.

### 8.8 Mead

- Vinery's mead is removed from that port (alcohol_part_b.md 7.6), with no conversion (your rule, 2026-10-18: neither port keeps old
  items working across versions). Brewin's mead (8.6) is the only one.
- **Test:** VB-54.

### 8.9 Questions for you

| # | Question | My default |
|---|---|---|
| Q1 | The black and purple texture: which screen is "plating"? The keg GUI, the JEI Fermenting page (the pouring row), or the recipe book? | ANSWERED 2026-10-19: the keg GUI's tankard slot icon; fixed in `-03` (10.16) |
| Q2 | Keg fluid slot: two slots (in / empty out, 2.0 style) or one slot where the empty container stays put? | BUILT with the default: two slots |
| Q3 | "Placeable on a coaster": items with a coaster model plus wines and juices, or every item? | BUILT with the default: modeled items plus Vinery and Farmer's Respite drinks |
| Q4 | Pierogi: drop both pack recipes, including the heatless shapeless one? cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19) likes heatless shapeless meals | BUILT with the default: both dropped |
| Q5 | Steel Toe Stout boost size | BUILT with the default: Resistance II, 90 s |
| Q6 | Ham and cheese | DECIDED 2026-10-18: as in the 8.6 table |
| Q7 | Keg pipe rule | DECIDED 2026-10-18: drain only while idle, as a setting (on by default), with a tooltip line (8.7) |

## 9. History

- 2026-10-14: gap analysis from decompiles of 1.19-2.0 (Forge) and 4.5.0+1.21.1 (Fabric). The user chose a full port
  that follows 4.5.0, client-only configurable scrambling, and a poison potion for Dread Nog. Honey/milk broken down in
  4.1, waiting for a pick. Kombucha green tea recommended (4.5). Found CCK's keg mixin (2.6).
  Replaces the earlier `brewinandchewin_parity.md`.
- 2026-10-14: decided: the pack's `create:honey` / `minecraft:milk` (4.1), green tea kombucha (4.5). Added the heat / cold
  block audit (2.7) and AL-41.
- 2026-10-17: port begun in `development/backportedBrewin`: build files, license, about 45 upstream classes (blocks, keg
  block entity, recipes, items, utilities, Forge containers).
- 2026-10-18: port completed to a building jar (3.5): registries, config, fluids, effects, capability, network, menu,
  screen, recipe book, HUD, camera, scrambling, coaster renderer, JEI, remaps, mixins; `tools/port_resources.py` for
  assets and data, `tools/check_data.py` (0 problems). CCK's keg mixin checked against the bytecode. Not booted, not in
  `mods/`; recipe-book and JEI fluid handling not ported. Added AL-42 to AL-48.
- 2026-10-18: planned your "changes to make" list (8): JEI effect lines on the keg's fluid output, cask / crate and
  coaster tooltips, a 2.0-style keg fluid slot, jams removed from the port, eleven recipe tweaks through a new
  `R.bncFermenting` helper, sequenced assembly for the sandwich and quiche, Create spout / drain through a new
  `R.createEmptying`, and a keg pipe rule. Found while planning: the 2.0 jar is still in `mods/` beside the port, the
  port's cooking-pot recipes lack `cookingtime` (KubeJS can't parse them), and `#minecraft:undead` fails to load.
  Questions Q1 to Q7 (8.9). AL-49 to AL-65 added.
- 2026-10-18: built section 8. Port `-02` (version bumped because `-01` shipped in 1.0.7): JEI / keg / keg item effect
  lines, the fluid slot pair with slot rules for menus and hoppers, jams removed, Steel Toe Stout Resistance II 90 s,
  the pipe rule and its setting, `cookingtime` in the cooking-pot files, the undead tag. Pack: `alcohol.js` (7 fermenting
  changes, pierogi, pizza, ham and cheese x4, quiche + 2 assemblies, 32 spout / drain), `R.bncFermenting`,
  `R.createEmptying`, `#creativeengineering:metal_dusts`, cask / crate tooltips, the coaster placeable tag, and most 3.4
  rows. Not installed in `mods/` (game running); not tested in game.
- 2026-10-18 late: 10.8: King Danis Wine removed (a second Lilitu), Praetorian tagged a small bottle and shrunk to fit
  the racks that lay bottles down; alcohol_part_b.md 7.11 settled. VB-59 updated, VB-66 added.
- 2026-10-18 late: 10.5 to 10.7: Vinery banner crafting recipe, WTHIT stem lines (variety, ripe, leaves pending), and
  the effect-description check (nothing to reuse). VB-64, VB-65 added.
- 2026-10-18 late: section 10 built. All advancements removed (Creative Common switch), keg JEI [+] fills and empties
  the tank (`KegTransfer`), Intoxicated AppleSkin wobble and Tipsy camera drift, D3 decided. AL-67 to AL-72 added.
- 2026-10-18 evening: `-02` installed in `mods/` (replacing `-01`) and `server_mods/` regenerated. `item_dict.js` checked
  against the registered items: every port item is mapped, the jams were already gone.
- 2026-10-19: `-02` STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) AL); its removals delete both `-01` and
  `BrewinAndChewin-1.19-2.0.jar`. Found from the server's 1.0.7 debug log: FML had picked `-01` over the 2.0 jar
  ("selecting most recent based on version data"), so the live world has run the port since 1.0.7 and had the jams;
  1.0.8 removes them there. Next build bumped to `-03`.
- 2026-10-19: `-02` DEPLOYED with pack 1.0.8 (10:20, server + clients); the 2.0 jar and `-01` are gone from both sides.
  AL-01 to AL-72 pending.
- 2026-10-19: 10.13 built into `-03` (keg heat tag, running / fueled gates, `isCold` for Vinery, tooltips). AL-73
  to AL-77 added. In `build/libs/` only: the game was running.
- 2026-10-19: pack 1.0.9 DEPLOYED (15:54) with `-02` unchanged. Late: Vinery `-03` and Creative Common 1.0.10 went into the
  local `mods/` (22:27), Brewin' `-03` didn't (`mods/` has `-02`). Until it does, Vinery logs "no
  BnCHeatSources.climate" and wine storage is never cold or warm. Writeup and TODO statuses brought up to date.
- 2026-10-19 late: 10.16 built into `-03` (tankard slot icon, keg leftover slot, slot rules, input from every face) plus
  the pack's compost rows and BlockRunner tiers. `alcohol/brewinport #3` (recipe-book tank filling) dropped from the
  TODO: the pack has no recipe book. AL-78 to AL-84 added.
- 2026-10-20: 11 built: every alcoholic drink is half a rotten shank, no saturation, always drinkable, Intoxicating,
  with Nausea where it has no drunk effect; Vinery `-03` rebuilt (wines eat, food-effect lines) in `build/libs/` and
  `jar_archive/`, not installed (the game was running). AL-85 to AL-90 added.
- 2026-10-20: 12 built (`alcohol/brewinport #8`): coasters take only items with a coaster model, Display Delight foods
  get one, WTHIT lines. Brewin' `-03` and Creative Common 1.0.10 rebuilt into `build/libs/` and `jar_archive/`, not installed
  (the game was running). AL-91 to AL-96 added; AL-52's Vinery wine half is superseded by AL-91.
- 2026-10-20: WTHIT keg lines in the port (`integration/wthit/KegWthit`, its own `waila_plugins.json`,
  `KegBlockEntity#getFermentTime()`): temperature, "Fermenting: Beer 45%", "Beer: Must Be Cold". `-03` rebuilt and
  installed in `mods/` and `server_mods/`. Tests WL-29, WL-30 ([wthit_lines.md](wthit_lines.md) 2.8).
- 2026-10-22 (second bulk run): SQ-OM-00 to SQ-OM-04 PASS on the scratch server (keg slots and faces, old `-02` keg
  NBT, coasters, compost and BlockRunner rows, the 44 drink food values); the logic and server halves of AL-81 to
  AL-87, AL-90, AL-91 recorded, their in-game halves stay open.
- 2026-10-24: planned from the scratchpad: Vintage Delight's six jar brews get their own category and a redesign (`alcohol/pack #3`, section 11).
- 2026-10-31: port `-04`: the food effects of Mead, Pale Jane, Saccharine Rum and the six Brewin' dishes show their
  HUD icon (6-arg `MobEffectInstance`, TODO `potions/effects #8`). Installed in `mods/` 2026-10-31 00:30, not yet booted.
- 2026-11-17: Part B (Vinery) moved to [alcohol_part_b.md](alcohol_part_b.md), sections B0 to B9 renumbered 0 to 9 (design_rules.md V-52).
- 2026-11-17: the Vinery port's own lang namespace `vinery_backport` is `vinerybackport` (plan_rules_fixes.md section 14,
  V-11): `assets/vinerybackport/lang/en_us.json`, the lang keys and the in-memory wine box models; no shown text changed.

## 10. Third list (2026-10-18 late, BUILT, not tested in game)

Your list, covering both ports and the time master ([time.md](time.md) 3.9). 10.1 to 10.8 were built 2026-10-18
into Brewin' `-02`, Vinery `-02` and Creative Common 1.0.8 (rebuilt under the same names, `jar_archive/` copies replaced
by hand). 10.9 onward came later, in your follow-up lists.

| Sections | Jars | Where (2026-10-19 late) |
|---|---|---|
| 10.1 to 10.8 | Brewin' `-02`, Vinery `-02`, Creative Common 1.0.8 | DEPLOYED with 1.0.8, still live in 1.0.9 |
| 10.9 to 10.12, 10.14 | Vinery `-03` | local `mods/` (22:27), not deployed |
| 10.15, 10.17, 10.18 | Vinery `-03` rebuilt; Brewin' `-03` (10.18) | local `mods/` since 2026-10-20, not deployed; 10.15 / 10.17 mostly TESTED 2026-10-20 |
| 10.13 | Brewin' `-03`, Vinery `-03`, Creative Common 1.0.10 | Vinery and Creative Common in local `mods/` (22:27); Brewin' `-03` in `build/libs/` only, so cold / warm storage is off until it's installed |

### 10.1 Decisions

- **D3 (Vinery): DECIDED, keep `trippy`.** It stays registered; the echo "discombobulate" tool effect uses it.
- **T1, T3, T4, T7 (time): DECIDED, the recommendations** ([time.md](time.md) section 4).

### 10.2 Advancements: all of them are gone

- **What:** `[advancements] enabled = false` in `creativeengineering-common.toml`, Creative Common
  `ServerAdvancementManagerMixin`. The advancement JSONs (12,940 across vanilla, mods and datapacks) are swapped for an
  empty map before the tree is built. So nothing shows in the advancement screen, toasts, is awarded or rewards.
  `/reload` applies a change; `true` brings them back.
- **Vinery:** `vintage_perfection` is also left out of the upstream layer for good (it checked 1.3.10 NBT the port's
  wines never carry).
- **Nothing else needs them:** no FTB Quests task, KubeJS script or port code depends on an advancement existing. Brewin's
  "temperature block near a keg" award checks for null.
- **Rewards that went with them**, each now without a source unless you give it one:

| Reward | From | Other source |
|---|---|---|
| Vinery banner x2 | `vinery:main/wine_somelier` | crafting recipe since 2026-10-18 (10.5) |
| Lootr trophy | `lootr:100loot` | none |
| MCA books (romance, family, infection, death, blueprint, rose gold, supporters) | MCA advancements | some in MCA villager trades / loot |
| Yuushya guide book on first join | `yuushya:grant_book_on_join` | crafting |
| IE rare / masterwork shader grab bags; IP coker reward | IE / IP advancements | IE loot chests |
| Lightman's Currency chocolate coins (event) | LC event advancements | none |

### 10.3 Keg JEI [+]: fills and empties the tank (port)

- **Was:** JEI's basic handler moved the items only.
- **Now:** `KegTransfer` (common) plans the whole move on copies of the grid, the player inventory and a scratch tank:
  1. The grid goes back to the inventory.
  2. One set of ingredients goes in; shift-click puts in as many sets as fit.
  3. A tank holding a fluid the recipe doesn't use is drained into empty containers from the inventory.
  4. The base fluid is poured in from filled containers, up to one set (or as many as fit). It never goes past a
     whole multiple of the recipe's amount: the keg only ferments whole multiples.
  5. Filled and emptied containers go back to the inventory.

  It uses the keg's own one-item steps (`KegBlockEntity.fillFrom` / `pourInto` / `sourceFluid`), so pouring recipes
  and plain Forge containers (buckets) both work.
- **Client and server:** `integration/jei/FermentingTransfer` runs the plan to set the button state, with upstream's
  messages: temperature, missing items or fluid (red slots), can't empty, not enough fluid, inventory full. The server
  runs the plan again on its own state and applies it in one go (`JEITransferKegRecipeServerboundPacket` carries only
  the recipe id and shift). Network protocol is now `4.5.0-2`.
- **Changes from upstream:**
  - Upstream sends the client's slot plan and can stop halfway. Here the move is all or nothing.
  - The vanilla recipe book side (`KegPlaceRecipe` / `KegStackedContents`) isn't ported: the pack has no vanilla
    recipe book (your call).
- **Tests:** AL-67 to AL-70.

### 10.4 Tipsy and Intoxicated visuals (port)

- **Intoxication:** AppleSkin's hunger overlay wobbles, with upstream's numbers. AppleSkin 2.4.2 has no offsets cache,
  so `HUDOverlayHandlerMixin` (`@Pseudo`) adds the wobble when `generateHungerBarOffsets` returns. Client setting
  `intoxicationFoodOverlay`, as upstream.
- **Tipsy:** the positional drift on top of the spin. Upstream turns the view around a point half a block off the eye
  and adds a slow slide. `TipsyCameraDriftMixin` moves the camera by that translation at the end of `Camera.setup`,
  using the same clock, strength and Distortion Effects scale as the spin. Up to about 0.2 blocks at Tipsy XI. It is
  the camera that moves, so at that level it can peek a little way into a wall you stand against.
- The 3.5 "not ported" table is now empty, apart from the vanilla recipe book.
- **Tests:** AL-71, AL-72.

### 10.5 Vinery banner recipe (pack)

- **Recipe:** shaped, `kubejs/server_scripts/cooking/alcohol.js` (`creativeengineering:shaped/vinery_standard`): any
  banner (`#minecraft:banners`) in the middle, a nether star below it, the 7 most complex wines around them. A wine's
  age doesn't matter (item ingredients ignore NBT).

  ```
  Jellie   Mojang Noir  Aegis
  Apple    banner       Cristel
  Lilitu   nether star  Chenet
  ```
- **"Most complex", scored per wine:** 1 for the fermentation step; 1 for general juice, 2 for biome juice or apple juice
  (two press steps); 1 per plain ingredient, 2 per Nether or End ingredient (blaze rod, chorus fruit, glowstone); the
  full score of any wine or apple juice used as an ingredient.

| Wine | Score | Why |
|---|---|---|
| Jellie | 18 | made from Apple Wine, Chenet and Bolvar |
| Bottle Mojang Noir | 7 | Red Wine + honey + cherry |
| Aegis | 6 | taiga juice, 3 ingredients |
| Apple Wine | 6 | apple juice twice (press chain) |
| Cristel | 6 | 3 ingredients, one a blaze rod |
| Lilitu | 5 | savanna juice; first of seven at 5 by juice used (14) |
| Chenet | 5 | jungle juice; tied with King Danis at 12 juice. Chenet kept: a 1.5.5 wine, while King Danis is a 1.3.10 keeper on hold (alcohol_part_b.md 7.11) |

- Upstream's smithing recipe (banner + netherite ingot -> 2 banners) is still there, minus the 1.20 template.
- The banner tooltip's first line now reads "Crafted from the seven finest wines" (port override).
- **Test:** VB-64.

### 10.6 WTHIT on grapevine stems and lattices (port)

- `VineryWthitClient.stem`, for every `StemBlock` (grapevine stem, the ten lattices, 1.3.10's lattice stem). From the
  block state, so no server data:
  - **"Growing: Taiga Red Grapes"**: the variety, named by its grape item;
  - **"Ripe: ready to pick"** at the last stage;
  - **"Grapevine leaves still to grow"** while a grapevine stem's leaves are pending (the alcohol_part_b.md 4.6 gap);
  - **"Nothing planted"** on an empty stem or lattice.
- Growth % stays Creative Common's crop line (`crop_growth.json` already lists the stem and all ten lattices), so nothing
  shows twice. Toggle: WTHIT config, Vinery page, "Grapevine stems and lattices".
- **Test:** VB-65.

### 10.7 Effect descriptions: nothing to reuse

- The pack has one effect-description mod, **JEED** 2.1.4. The other one, **Enchantment Descriptions**, covers
  enchantments only, and neither port adds any.
- JEED's bundled text has no `vinery` or `brewinandchewin` keys.
- Every effect the ports register already has a description:
  - the 12 Vinery effects from the 1.5.5 jar's lang (read through the upstream layer);
  - `trippy` from the port's own lang;
  - the 4 Brewin' effects from 4.5.0's lang.
- The texts match what the port's effects do (checked: frosty armor, jellie, creeper). The pack's overlay
  (`kubejs/assets/creativeengineering/lang`) has no leftover keys for either mod, and `tools/gen_effect_descriptions.py`
  already reads the 1.5.5 jar.
- **Where JEED could help later:** alcohol_part_b.md 7.4 (pinned). JEED's bundle describes many food-mod effects, which the
  `wine_effect_candidates.py` list would read.

### 10.8 King Danis out, Praetorian into every rack (port)

- **King Danis Wine removed.** 1.3.10 named it "Miss Lilitus Wine", and its model is Lilitu's exactly (same 15
  elements, same bounds). So it was a second Lilitu, and the 1.5.5 Lilitu already places in racks correctly.
  - Removed: block, item, storage lists, render layer, effect default, keeper data (`REMOVED` in the upstream layer),
    `data/vinery/recipes/port/king_danis_wine.json`.
  - Pack side: `vinery-common.toml` effects entry, `i.king_danis_wine`, and the generated placeable, stack-size and
    food-type lists.
  - No conversion (your rule).
- **Praetorian: the asset isn't misplaced.** Its 1.3.10 model is a large bottle, 6 x 18 px, like Stal Wine's. The
  1.5.5 small template is 4 x 15 px.
  - It fit no 1.5.5 rack because it was in neither bottle tag. 1.3.10 had no such tags, and its own racks took any wine.
  - **Done:**
    - The upstream layer adds it to `#vinery:small_bottle`. So it fits the small rack, the Wine Rack and the big rack,
      the wine box, and stacks 2 per placed block, as in 1.3.10.
    - `BottleRenderer` (the racks that lay bottles down) shrinks a BIG-size bottle to 15/18 about its own axis, so it
      lies in its slot without poking out. Praetorian is the only BIG bottle in the small tag, so nothing else changes.
    - The big rack stands it upright at its usual 0.8, where an 18 px bottle already fits (Stal Wine).
  - The shrink is done in code, not with an edited copy of the model (the model is upstream's, All Rights Reserved).
- alcohol_part_b.md 7.11 is settled by this (VQ3: small racks).
- **Tests:** VB-59 (updated), VB-66.

### 10.9 Large wines stack 2 per placed block again (port)

- **Cause:** 1.5.5 changed the placed wine's `canInsertStack` from "any wine" (1.3.10) to `#vinery:small_bottle`. The
  2-per-block wines (Creeper's Crush, Eiswein, Chenet, Aegis, Mellohi, Strad, Apple Cider) are all large bottles, so
  their `maxCount` of 2 could never be reached. Upstream bug, not a model or texture issue: the placed-bottle renderer
  draws 2 and 3 bottles exactly as 1.3.10 did.
- **Done:** `WineBottleBlock.canInsertStack` takes any wine again. `willFitStack` still caps a block at the smallest
  `maxCount` of the wines in it (a 1-per wine never takes a second bottle; a 3-per wine joining a 2-per wine stops at 2).
- **Legacy racks (`wine_rack_1` to `_5`):** take any wine, as in 1.3.10. There is no shrunk model per wine in either
  version: both racks lay the wine's own block model down at 0.9 scale. The 1.5.5 models are mostly the same size as
  1.3.10's; the ones that grew are Stal (4 -> 8 px wide, 15 -> 18 px tall), Strad (13.5 -> 16 px), Glowing (14 -> 16 px).
  Cristel, Aegis, Jellie, Magnetic and Lilitu were already wider than a slot in 1.3.10.
- **Tests:** VB-67.

### 10.10 Duplicate storage, furniture and lattices removed; "Cherry" names (port)

Your calls, 2026-10-19 (after the alcohol_part_b.md 4.1 look-alike list and the 10.9 legacy-block outline):

| Pair | Kept | Removed |
|---|---|---|
| Crates vs bags | the 1.5.5 bags (`apple_bag`, `cherry_bag`, `red_grape_bag`, `white_grape_bag`, 6 biome bags) | 1.3.10 `apple_crate`, `cherry_crate`, `red_grape_crate`, `white_grape_crate` |
| Furniture | 1.3.10 `chair`, `table`, `big_table`, `shelf` ("Cherry ...") | 1.5.5 `cherry_chair`, `cherry_table`, `cherry_big_table`, `cherry_shelf` |
| Cabinet / drawer | both: 1.5.5 `cherry_cabinet` / `cherry_drawer` and 1.3.10 `wine_rack_3` / `_5` | none |
| Barrel | 1.5.5 `cherry_barrel` | 1.3.10 `barrel` |
| Lattices | 1.3.10 `grapevine_lattice` (takes lattice grape seeds, so jungle grapes still grow) | the 9 1.5.5 wood lattices (`oak`, `spruce`, `birch`, `dark_oak`, `acacia`, `bamboo`, `jungle`, `mangrove`, `cherry`) |
| Kitchen | none | `stove`, `wood_fired_oven`, `kitchen_sink` (already uncraftable, no creative tab) |
| Wine racks | all, unchanged for now | none |

- **Names:** every "Dark Cherry" in the port's en_us lang reads "Cherry" (`UpstreamTranslator.renameDarkCherry`): wood
  set, cabinet, drawer, barrel, racks, boats, creative tab. The ids were already `cherry_*` (D1). Dramatic Doors'
  `short_` / `tall_vinery_cherry_door` are renamed in `namechanges.js`.
- **Port:** registrations gone; `LatticeBlock`, `LatticeBlockEntity`, `LatticeRenderer` (the `vinery:lattice` block
  entity type), `LegacyStoveBlock`, `LegacyKitchenSinkBlock` and the faucet sound deleted. The ids are in
  `UpstreamTranslator.REMOVED` (tag entries dropped) and out of `KEEPERS` (their 1.3.10 recipes and loot not read).
  Upstream recipes and loot naming them are left out by `keep()`.
- **Pack:** 21 `item_dict.js` keys removed; `loot/07_rules.js` packs apples, cherries and red / white grapes into the
  bags; compost rows for the crates removed (the bags already had them); Creative Common `crop_growth.json` lists only
  `grapevine_lattice`. No structure, datapack or other mod's data names any removed id (scanned 2026-10-19).
- **Old worlds:** no conversion (your rule). Placed removed blocks become air and the items vanish, as with King Danis.
- **Name clashes left:** "Cherry Cabinet" is also `abnormals_delight:cherry_cabinet` (the 1.3.10 cabinet and drawer
  went in 10.11); "Short / Tall Cherry Door" are also Dramatic Doors'
  `short_` / `tall_cherry_door`, for a vanilla cherry door 1.19.2 doesn't have.
- **Still one-way:** the bags have no unpack recipe (storage_audit.md C2), and loot now packs fruit into them.
- **Tests:** VB-68 to VB-70.

### 10.11 Wine racks merged; cherry single-bottle rack gets its glass door (port)

Your calls, 2026-10-19:

- **Removed:** 1.3.10 `wine_rack_3` (Cherry Cabinet) and `wine_rack_5` (Cherry Drawer). The 1.5.5 `cherry_cabinet` /
  `cherry_drawer` stay, so the name clash from 10.10 is gone.
- **Racks merged:** 1.3.10 `wine_rack_1` (9x) and `wine_rack_2` (4x) removed. The 1.5.5 `cherry_wine_rack_big` /
  `_small` stay: named "Big / Small Cherry Wine Rack" (10.10), 1.5.5 art, slide-in animation, BIG bottles drawn shrunk.
- **Any wine in the 4x / 9x racks**, every wood (`BottleStorageBlock.canInsertStack`: any `DrinkBlockItem`), as the
  1.3.10 racks allowed. 1.5.5 took only `#vinery:small_bottle`. Wide bottles (Cristel, Aegis, Lilitu, Jellie,
  Magnetic...) clip until the bottle models are tweaked ([storage_audit.md](storage_audit.md) 3, alcohol/decisions #6).
  Later: 10.12 limits the racks by size, and 10.14 draws mediums slim in the 4x rack.
  The wine box still takes only small bottles; the mid rack takes small and large.
- **Port code removed:** `LegacyNineBottleStorageBlock`, `LegacyFourBottleStorageBlock`, `LegacyWineRackStorageBlock`
  and its block entity (`vinery:wine_rack_storage`), both legacy rack renderers and storage types, the 4 rack sounds.
  The ids are in `REMOVED`, out of `KEEPERS`. Pack: 4 `item_dict.js` keys.
- **Old worlds:** no conversion; placed `wine_rack_1/2/3/5` become air with their contents.

**Cherry single-bottle rack had no window (found):**
- Every 1.5.5 mid rack's door is the wood's `*_cabinet_front` texture. Each wood's has a clear pane, except
  `dark_cherry_cabinet_front.png`: 0 clear pixels, a solid panelled double door, because upstream reuses the dark
  cherry cabinet's door. The render layer (cutout) is the same as upstream's, so it isn't a port bug.
- 1.3.10's `cherry_cabinet_front.png` is the same wood palette with two clear windows (64 clear pixels).
- **Done:** `UpstreamTranslator.patchAssets` points `dark_cherry_wine_rack_3_closed` / `_open` (the port's
  `cherry_wine_rack_mid`) at `vinery:block/cherry_cabinet_front`, in memory. The cherry cabinet keeps its solid door.
- **Tests:** VB-71, VB-72.

### 10.12 Bottle sizes from the per-block limit (port)

Your rule, 2026-10-19: a wine's size is how many fit in one placed block, and that number is set in one place.

| Size | Per block | 9x rack | 4x rack | Wine box | Single-bottle rack | Wines (today's limits) |
|---|---|---|---|---|---|---|
| Small | 3 | yes | yes | yes | yes | Cherry, Sun-kissed (`glowing`), Red, Stal, Noir, Bolvar, Solaris, Kelp Cider, Clark, Apple Wine, Mojang Noir, Villagers Fright |
| Medium | 2 | no | yes | yes | yes | Creeper's Crush, Eiswein, Chenet, Aegis, Mellohi, Strad, Apple Cider, Praetorian |
| Large | 1 | no | no | no | yes | Chorus, Magnetic, Jo's Special Mixture, Cristel, Jellie, Lilitu |

- **Since changed (10.14):** Stal and Sun-kissed moved to medium, Magnetic from large to medium; the current table is
  in 10.14.
- **The one place:** the number in each `new WineBottleBlock(getWineSettings(), N)` in `ObjectRegistry` (commented
  there). Change it and everything below follows on the next build.
- `DrinkBlockItem.BottleSize` (SMALL / MEDIUM / LARGE) is derived from it; the registry no longer passes a size per
  item, so the old SMALL / BIG enum and its mismatches are gone (stal was SMALL, sun-kissed BIG, Praetorian BIG).
- **Containers** check the size, not tags: `BottleStorageBlock.Layout` names its largest size (NINE small, FOUR
  medium), the wine box medium, the single-bottle rack large. This replaces 10.11's "any wine in the racks".
- **Tags:** `#vinery:small_bottle`, `#vinery:medium_bottle` (new) and `#vinery:large_bottle` are rebuilt by
  `UpstreamTranslator.patchData` from the sizes, replacing upstream's lists and the apple wine / Praetorian patches.
  Nothing in the port reads them now; they're for other mods and scripts.
- **Drawing:** a bottle lying in a 4x / 9x rack or the wine box is shrunk to the 15 px slot length, measured from its
  own baked model (`BottleRenderer.fitToSlot`). That replaces 10.8's fixed 15/18 for BIG items, and covers stal (18 px)
  and sun-kissed (16 px), now small. Width isn't fitted, so wide mediums (Aegis, Creeper's Crush, Mellohi) still
  overlap a neighbor in the 4x rack until their models are tweaked. The single-bottle rack stands bottles upright, as
  before.
- **Changes with today's numbers:** stal and sun-kissed move from large to small (9x racks); the 8 mediums leave the
  9x rack and gain the wine box; the 6 larges leave every 4x / 9x rack (10.11 had let them in).
- **Tests:** VB-73 to VB-75.

### 10.13 Keg and wine storage temperature (Brewin + Vinery ports, pack tags) (BUILT 2026-10-19, wine storage kinds and rates scratch server PASSED 2026-10-22 (SQ-OM-05), not tested in game)

Your list, 2026-10-19: furnaces, bonfires and braziers heat kegs while lit; industrial heat only while the machine
runs (skip machines that aren't as easy); blaze burners, liquid burners and the blaze enchanter only when fueled;
thin ice, snow, powder snow, IW permafrost and cryosol are cold; tooltips on every source, the keg and the racks;
WTHIT shows `cold x2.0`, `cellar x2.0`, `cold cellar x4.0`.

- **Keg heat tag (port):** the keg now reads `#brewinandchewin:heat_sources` (`BnCTags.Blocks.HEAT_SOURCES`), shipped
  as `#farmersdelight:heat_sources` (written by `tools/port_resources.py`). The pack's additions go there, so a
  furnace warms a keg without letting an FD cooking pot cook on it. `updateTemperature` keeps the shape CCK's
  required mixin needs (it targets `Level.getBlockState` and the second `BlockState.is(TagKey)`, not the FD field;
  checked against CCK 1.3.11.c's bytecode).
- **Running gates (port, `BnCHeatSources`):** a boolean `lit`, `active` or `on` property must be true (furnaces,
  braziers, IE machines, AE2 vibration chamber, PneumaticCraft compressors). A `blaze` property at `none` or
  `smouldering` means no fuel (Create blaze burner, CC&A liquid blaze burner, CEI blaze enchanter, which goes to
  KINDLED once its tank holds experience). Before this, a smouldering (unfueled) burner counted as heat. IE only flips
  `active` on a multiblock's master block, so a running coke oven counts once, not 27 times.
- **Pack tags (`tags/blocktags.js`, ids in `block_dict.js`):**

  | Heat (`#brewinandchewin:heat_sources`) | Blocks |
  |---|---|
  | Furnaces | vanilla furnace, blast furnace, smoker; Quark deepslate / blackstone furnace; SecurityCraft keypad furnace / blast furnace / smoker (Nether's Delight ones were already in via FD) |
  | Bonfires, braziers | Decorative Blocks bonfire, soul bonfire (fire blocks: only there while burning), brazier, soul brazier; Blocky Siege `brazier_on` |
  | Industrial, while running | IE coke oven, crude blast furnace, alloy smelter, furnace heater; Engineer's Decor lab furnace, electrical furnace, waste incinerator; Clayworks kiln; PneumaticCraft air compressor, advanced air compressor; AE2 vibration chamber |
  | Blaze, when fueled | CC&A liquid blaze burner, CEI blaze enchanter (Create's burners were already in via FD) |
  | Always hot | Botania blaze block (added 2026-10-19 late, from the 2.7 "yes" rows) |

  Cold (`#brewinandchewin:freeze_sources`, beside ice / packed / blue ice and the ice crate): snow layers, snow
  block, powder snow, IW thin ice, IW permafrost, IW cryosol. Added 2026-10-19 late from the 2.7 "yes" rows:
  `#minecraft:ice` (adds frosted ice and rare ice) and Architects Palette polished / chiseled packed ice.
- **Skipped (running state only in the block entity):** IE arc furnace, improved blast furnace, blast furnace
  preheater; PneumaticCraft heat blocks (vortex tube, heat sinks...); Compressed Creativity heater; D&D furnace
  engine; Create boilers; Cooking for Blockheads oven (no block state at all, so it would count while idle). The
  CFB fridge (a 2.7 "maybe") is left out. Decorative ovens (Handcrafted, Fantasy Furniture) left out too: their `lit` is a toggle.
- **Wine storage (Vinery port, `WineYears.storageKind`):** a storage block is a *cellar* with no sky light and *cold*
  when `BnCHeatSources.isCold` says so: the keg's own count (5x5x5 sources, biome, dimension) at chilly or colder, found
  by reflection, so Vinery runs without Brewin. Bonuses multiply: cellar x2.0, cold x2.0, cold cellar x4.0. The kind
  is stored on the wine (`WineYear.storageKind`); wines stored before this read as cellar when their rate is above
  100.
- **Warm and sunny (your follow-up, same day):** a spot at the keg's *warm* or hotter (`BnCHeatSources.climate` returns
  -1 / 0 / 1; it replaced `isCold`) ages wine at x0.75. Every stored wine now names its light, *sunny* (sky light,
  x1.0) or *cellar*, after its temperature word if any, and the factors multiply:

  | | sunny | cellar |
  |---|---|---|
  | neither | sunny x1.0 | cellar x2.0 |
  | cold | cold sunny x2.0 | cold cellar x4.0 |
  | warm | warm sunny x0.75 | warm cellar x1.5 |

  So the earlier "cold x2.0" reads "cold sunny x2.0". Rates show as 1.0 / 0.75 / 1.5 (`WineYears.formatRate`).
  Below x1.0 the stored-time bonus goes negative (it was clamped at 0); the age itself is still clamped at 0. Like the cellar check, it's measured when the wine goes in, so moving a furnace later needs the wine put in
  again.
- **Values:** CEConfig `[time.wine.boosts]` `cellarMultiplier` 2.5 -> 2.0, new `coldMultiplier` 2.0 and
  `warmMultiplier` 0.75 (0.1 to 1.0), pushed into `vinery-common.toml` (`cellarAgingMultiplier`, new
  `coldAgingMultiplier`, `warmAgingMultiplier`). The running game re-saved both tomls and dropped the new keys; Forge
  writes them back at their defaults on the next start, so the SINGLEPLAYER preset still matches. The vintage clock
  packet carries the cold and warm values (Vinery network version 1 -> 2). Since 2026-10-27 nothing is pushed: the
  port reads these from Creative Common's `time.WineTime` while `[time]` is on ([time.md](time.md) 3.7).
- **Text:** every block item in either tag: "Heats/Cools the surrounding environment", then "Useful for keg brewing
  and wine casking" on its own line (split 2026-10-20, 10.18); keg: "Sensitive to hot and cold blocks in the environment"; wine racks (9x, 4x, single-bottle) and the wine
  box: "Wine aging is sped up by dark and cold environments, slowed by warm ones" (the last three words added with
  warm). WTHIT on storage, the stored wine's tooltip ("In storage: ...") and `/wine storage` ("Storage here: ...") use
  the table's labels; JEI Wine Aging adds the cold and warm lines.
- **Tests:** AL-73 to AL-77, VB-76 to VB-80.

### 10.14 Slim bottles in the 4x rack and wine box; Stal, Sun-kissed, Magnetic medium (port) (BUILT 2026-10-19; sizes TESTED 2026-10-20, slim bottles replaced by 10.17)

Your list, 2026-10-19: Stal and Sun-kissed medium and slimmed in the 4x rack; Strad, Aegis, Apple Cider, Chenet,
Mellohi, Creeper's Crush, Eiswein slimmed in the 4x rack; Magnetic medium with a new wine box look (Strad's shape in
Magnetic's colors, made with the palette swap tool) and a slim 4x look; slim bottles in the wine box too; Praetorian
stays as it is.

- **Sizes (`ObjectRegistry`):** `stal_wine` 3 -> 2, `glowing_wine` (Sun-kissed) 3 -> 2, `magnetic_wine` 1 -> 2. So Stal
  and Sun-kissed leave the 9x rack, and Magnetic gains the 4x rack and the wine box and stacks 2 per placed block. The
  bottle tags follow on their own (10.12).

  | Size | Wines now |
  |---|---|
  | Small (3) | Cherry, Red, Noir, Bolvar, Solaris, Kelp Cider, Clark, Apple Wine, Mojang Noir, Villagers Fright |
  | Medium (2) | Creeper's Crush, Eiswein, Chenet, Aegis, Mellohi, Strad, Apple Cider, Praetorian, Stal, Sun-kissed, Magnetic |
  | Large (1) | Chorus, Jo's Special Mixture, Cristel, Jellie, Lilitu |

- **Slim models:** every medium except Praetorian (10 wines). Each is the 4 x 15 px small template (Red Wine's shape)
  with `red_wine.png` recoloured in the wine's own palette (its model's particle texture), using the plain mapping of
  `textureswapper/palette_swap.py` (`--invert` washed the glass out).
- **Magnetic's box look:** Strad's model with `strad_wine.png` recoloured in Magnetic's palette, the same way.
- **Built in memory, nothing shipped (your call, same day):** `forge.upstream.SlimBottles`, called from
  `UpstreamTranslator.patchAssets`, ports the swap tool's plain mapping to Java (checked pixel-identical to the tool on
  all 10 wines) and adds `vinerybackport:block/wine_slim/<id>` and `.../wine_box/<id>` (models and textures) to the
  upstream asset pack at load. The recolours come from the donor jars like all other upstream art (D13), so the port
  jar holds none. Wines come from the registry (medium size), so a size change needs no regeneration.
  `SLIM_SKIP` (Praetorian) and `BOX_SHAPES` (magnetic -> strad) are at the top of the class. A first version wrote
  PNGs into the jar with a Python generator; it was removed.
- **Drawing:** `ExtraModels` registers every model in those two folders. The 4x rack draws a wine's slim model if it
  has one; the wine box tries the box model, then the slim one. Anything else draws its own model shrunk to the slot
  length as before (`BottleRenderer.renderLying`). The 9x rack and the single-bottle rack are unchanged.
- **Tests:** VB-81 to VB-83.

### 10.15 Vinery tooltips, Stained Glass, wine box and flying placement (port + pack) (BUILT 2026-10-19; Stained Glass, wine box and flying TESTED 2026-10-20; tooltips reworded, 10.18)

[TODO](todo/TODO.md) `alcohol/vineryport #16`, `#17`, `#18` (scratchpad 2026-10-19).

- **Wine size lines (port, `DrinkBlockItem.BottleSize.addSizeLines`):** under the effect line every wine says its size
  and how many stand in one placed block ("Small bottle: 3 per placed block"), then where it can be stored: small
  "Fits every wine rack and the Wine Box", medium "Fits the Small Wine Bottle Storage, the Wine Rack and the Wine Box",
  large "Fits the Wine Rack only" (reworded 2026-10-20 with the rack names, 10.18). The size comes from the per-block limit (10.12), so a size change moves the line.
- **Capacity lines (port, `BottleSize.addHoldsLine`):** 9x rack "Holds 9 wines (small only)", 4x rack "Holds 4 wines
  (small or medium)", single-bottle rack "Holds 1 wine (any size)", wine box "Holds 1 wine (small or medium)" plus
  "Sneak right-click to open or close, with or without an item in hand". Each block's `canInsertStack` and its line
  read the same largest size, so they can't disagree. The 10.13 aging hint stays below.
- **Pack lines (`tooltips.js`, Vinery section):** the 2026-10-01 `TODO` placeholders replaced: Grapevine Pot, Apple
  Press, Cask (plus the sneak-to-empty line), Grapevine Stem and Lattice (with the seed line: shears give the seed
  back, breaking a planted stem or lattice loses it, vinery.md 4), apple / cherry leaves and saplings, Stackable Log;
  Chorus Wine "Makes you wake up in strange places" (light purple). The commented-out grape / seed key removals were
  dropped: 1.5.5's lang has no `item.vinery.grape(seed).tooltip.*` keys.
- **Stained Glass (port lang + pack recipe):** `vinery:window_block` -> "Stained Glass", `vinery:window` -> "Stained
  Glass Pane" through `vinery_port/upstream_lang/en_us.json` (same route as "Cask"); no mod in `mods/` uses either
  name. Upstream's three recipes stay; `alcohol.js` adds the vanilla pane shape, 6 Stained Glass -> 16 panes
  (`creativeengineering:shaped/vinery_window_from_block`).
- **Wine box with an item in hand (port):** `WineBoxBlock` toggles on sneak whatever is held (1.5.5 needed an empty
  hand). Vanilla never calls the block for a sneaking player holding anything (either hand), so
  `VineryCommonEvents.onRightClickBlock` sets `useBlock` ALLOW for the wine box. Sneaking at an open box with a wine
  now closes it instead of placing the wine on top.
- **Placing wine while flying (port):** `DrinkBlockItem.getPlacementState` checks the sneak key (`isShiftKeyDown`), not
  the crouch pose (`isCrouching`), which a flying player never takes.
- **Not changed:** the single-bottle rack's door (`BigBottleStorageBlock`) still opens only with an empty hand.
- **Tests:** VB-84 to VB-89.

### 10.16 Keg fixes, leftover slot, slot rules, any-face input; compost and BlockRunner (port + pack) (BUILT 2026-10-19, scratch server PASSED 2026-10-22 (SQ-OM-01, SQ-OM-03), not tested in game)

[TODO](todo/TODO.md) `alcohol/brewinport #4`, `#5`, `#6`, `#7`; `alcohol/vineryport #6`.

- **Tankard slot icon (port, Q1):** the black and purple square on the keg's container slot was
  `item/empty_container_slot_tankard`: the texture ships, but 1.19.2 has no `atlases/*.json`, so a slot icon must be
  stitched into the block atlas by hand. `BrewinAndChewinForgeClient.stitchTextures` (`TextureStitchEvent.Pre`) adds it.
  It is the keg's only slot icon.
- **Leftover slot (port, [remainder_buffer.md](remainder_buffer.md) 3.6):** a fermenting batch's ingredient leftovers
  (the honey bottle's glass bottle, any potion's bottle) no longer fly out. They go to the new take-only slot 8, right
  of the output, then to the empty-container slot (7) under the fluid slot; a matching stack first, an empty slot after,
  slot 8 before 7 so the fluid slot keeps room for its own empties. No "recycle into the container slot" step as on the
  pot: the keg's container slot is for pouring, not for plating one meal. Only one new slot: the right bubbles fill the
  space above the output, and the empty-container slot already holds the same kind of item. Settings in
  `brewinandchewin-common.toml [keg]`: `remainderBuffer` (default true; false throws them, as in 4.5.0) and
  `stallWhenFull` (default true: `canFerment` refuses a batch whose leftovers wouldn't fit). The slot sits after the
  player inventory in `KegMenu` (`REMAINDER_MENU_SLOT` 44), so `KegTransfer`'s slot numbers are unchanged
  (`KEG_MENU_SLOTS` 8 replaces its use of `INVENTORY_SIZE`).
- **Saves (port):** the keg now saves 9 slots plus `BnCInventoryVersion`, since a 2.0 keg also saved 9 slots and is
  told apart by having no version key. `KegItemHandlerForge.readFromNbt` copies a save of any size into the 9 slots;
  the old code called `setSize`, which empties every slot, so an 8-slot `-02` keg would have lost its items.
- **Slot rules (port, `isItemValidForSlot`):** ingredient slots 0-3 take only what some fermenting recipe uses as an
  ingredient; the container slot only an item some pouring recipe pours into (and not a filled container); the fluid
  slot only filled containers (as before). By hand, shift-click and automation alike. The output, empty-container and
  leftover slots are filled by the keg; the GUI and the sided wrapper keep players and hoppers out of them.
- **Input from every face (port, `SidedKegWrapper`):** every face now inserts into the ingredient grid, the container
  slot and the fluid slot (2.0 and 4.5.0: grid from the top only, containers from the sides). On the sides and bottom a
  filled container skips the grid and lands in the fluid slot; from the top it may still go in as an ingredient. The
  top still pulls ingredients back out; the sides and bottom pull the output, the fluid slot's empties and the leftovers.
- **Compost (pack):** `global.compost` empties `ComposterBlock.COMPOSTABLES` and rebuilds it from its rules and
  values, so the four chances the port registers (kimchi 0.5, pickled pickles 0.5, quiche and quiche slice 0.85, from
  4.5.0's data map) were lost: no rule matches them. They are rows in `compost_values.js` (`other`, `brewinandchewin`
  group), added by hand the way `gen_compost_values.py --merge` would, because `/dumpcompost` predates the port.
- **BlockRunner (pack):** `heating_cask` and `ice_crate` copy oak planks: `wood`, beside the other wooden crates. The
  coaster stays untiered: it is 1 px thin and BlockRunner reads the block about 0.2 below the feet, so the block under
  it counts, as with carpets.
- **Still open in 3.4:** food types, stack sizes and the Canned Kimchi `satisfaction` copy need `/dumpall` (or
  `/dumpfood` + `/dumpstacks`) in a game running the port, then `tools/food_catalog.py`, `tools/gen_can_types.py` and
  `tools/gen_stack_sizes.py`. The 4.5.0 kimchi has no effect (2 hunger, 0.6 saturation).
- **Vinery grass slab tint (pack, `alcohol/vineryport #6`):** the slab used the same grass color as grass blocks, but
  Rubidium blends the vanilla grass block's tint per vertex across its neighbors (biome blend above 0) and colors a
  modded block flat, one color per block, so the slab read a shade off. `blocktags.js` puts `vinery:grass_slab` in
  Rubidium's `#c:blendable_blocks` (`b.t_blendable_blocks`), which opts it into the same blending.
- **Tests:** AL-78 to AL-84.

### 10.17 Wine box looks in Strad's shape, Magnetic inverted, wine texture dump (port) (BUILT 2026-10-19; looks TESTED 2026-10-20, dump untested)

[TODO](todo/TODO.md) `alcohol/vineryport #19`, `#20` (scratchpad 2026-10-19). Replaces 10.14's slim bottles.

- **Box shape (`forge.upstream.BottleLooks`, was `SlimBottles`):** the 10 mediums (every medium but Praetorian:
  Stal, Strad, Magnetic, Aegis, Eiswein, Creeper's Crush, Sun-kissed, Mellohi, Chenet, Apple Cider) lie in the wine box
  as the upstream Strad / Stal bottle (the two share one wide shape; Stal's neck is 2 px longer). Stal and Strad draw
  their own model; the other 8 take Strad's model with `strad_wine.png` recoloured in their own palette (plain mapping,
  as before). The 4 x 15 px slim template (Red Wine's shape) is gone: nothing draws `wine_slim` now.
  `OWN_SHAPE`, `BOX_SHAPE` (strad) and `INVERT` sit at the top of the class, so a wine can move to Stal's shape or
  change mapping in one line.
- **4x rack:** draws the same box model, only smaller: at most 0.75 scale (`BottleRenderer.FOUR_RACK_BOX_SCALE`), so
  the 8 px Strad body comes out 6 px and the diagonal neighbors, 0.35 / 0.33 apart, don't overlap. No model change.
  Smalls, Praetorian and every other rack draw as before.
- **Magnetic inverted:** its recolour uses the swap tool's `--invert` mapping (the shape's darkest color takes the
  palette's brightest): a light outline round dark red glass. Checked against `palette_swap.py --invert` output.
- **Dump (`/dumpwinetextures`, client command):** writes every wine texture the last resource load saw to
  `kubejs/exported/wine_textures/`: `upstream/` as the donor jars have it (every wine's own textures, parts included,
  1.3.10 keepers too), `generated/` the box recolours, and `index.tsv` (wine, size, its placed textures, its box / 4x
  rack look). For hand texturing; nothing reads the files back yet, and hand-painted recolours of upstream art can't
  ship in the jar (D13), so using them would need a local override route.
- **Tests:** VB-82, VB-83 rewritten; VB-90.

### 10.18 Test results 2026-10-20, recolour tweaks, rack names, climate line (Vinery + Brewin' ports)

[TODO](todo/TODO.md) `alcohol/vineryport #16`, `#21`; `alcohol/brewinport #2` (scratchpad 2026-10-20). Your test
round on the local `-03` jars (10.15, 10.17 and the recolour tweaks below were all in it):

| Result | What | Tests |
|---|---|---|
| Success | Stained Glass rename | VB-87 |
| Success | Wine box with an item in hand, wine placement while flying | VB-88, VB-89 |
| Success | Every bottle shape and case size: rack fit by size, shrink to slot, the 10.14 sizes, the box looks in the 4x rack and the wine box, the recolour tweaks | VB-73, VB-74, VB-81 to VB-83, VB-91 |
| Tweak | Wine "Fits" lines clunky: name the racks Single, Small, Big | VB-84 reworded, VB-92 added |
| Tweak | Heat / cold source line: "Useful for..." on its own line | AL-77 reworded |

- **Recolour tweaks (port, `BottleLooks`, built 2026-10-20 before the test round):** Sun-kissed (`glowing_wine`) and
  Aegis use the inverted mapping like Magnetic. `DROPS` leaves color classes out of a wine's palette before the swap:
  brown (hue 10 to 45, saturation 0.4 or more, value 0.7 or less) for Mellohi, Apple Cider and Creeper's Crush; white
  (saturation 0.15 or less, value 0.85 or more) for Magnetic, whose outline takes its salmon instead. The swap tool has
  the same filter as `--drop brown,white` (`textureswapper/palette_swap.py`). Still built in memory (D13).
- **Rack names (port, `UpstreamTranslator.renameWineRacks`):** every `block.vinery.<wood>_wine_rack_<size>` in the
  merged en_us lang becomes "Single / Small / Big <Wood> Wine Rack" (mid / small / big: 1, 4, 9 bottles). Upstream
  said "<Wood> Wine Rack", "Small <Wood> Wine Bottle Storage", "Big <Wood> Wine Bottle Storage". It runs before
  `renameDarkCherry`, so cherry reads "Cherry". No pack script names the racks.
- **Fits lines (port lang):** small "Fits in Single, Small, and Big Wine Rack, and Wine Box"; medium "Fits in Single
  and Small Wine Rack, and Wine Box"; large "Fits in Single Wine Rack". The capacity lines ("Holds 4 wines (small or
  medium)") are unchanged.
- **Climate line (Brewin' port):** `brewinandchewin.tooltip.heat_source` / `freeze_source` end at "environment"; the
  new `tooltip.climate_source.use` "Useful for keg brewing and wine casking" follows on its own line, in the same gold /
  aqua.
- **Jars:** Vinery `-03` and Brewin' `-03` rebuilt and installed in the local `mods/` 2026-10-20 (the game was closed).
  Brewin' `-03` carries 10.13, 10.16 and the port side of 12, so cold / warm wine storage is live locally now.
- **Tests:** VB-73, VB-74, VB-81 to VB-83, VB-87 to VB-89, VB-91 PASSED; VB-84 reworded; VB-92 added; AL-77 reworded.

### 10.19 Hopper jams on the press and the cask (port) (BUILT 2026-10-20, scratch server PASSED 2026-10-22 (SQ-OM-07), not tested in game)

[TODO](todo/TODO.md) `alcohol/vineryport #7`, `alcohol/decisions #11`.

- **The 1.3.10 jams are already gone.** vinery.md 4 described 1.3.10: the press output and every barrel slot took any
  item. In the port each slot takes only what a recipe uses there (press alcohol_part_b.md 7.2; the cask's own 1.5.5 face rules) and
  only the outputs can be pulled. So VN-05 / VN-08 (cobblestone, dirt) don't apply to the port and were retired.
- **What still jammed (automation only):**

  | Block | Jam | Why it stuck |
  |---|---|---|
  | Cask | A hopper fills slot 1 with an ingredient, then spills the same item into slot 2 | a recipe needs exactly as many filled slots as it has ingredients, so two stacks of one item match nothing; only the output can be pulled, so nothing clears it |
  | Cask | Ingredients of two recipes in the three slots | no recipe matches the mix |
  | Cask | Juice of another type in the juice slot | it only drains once the cask is empty, so leftover juice below a recipe's amount stays forever |
  | Press | Honeycomb pushed in while apple mash or wine bottles are in | the queued input and the bottle in slot 2 belong to different recipes; mashing can't output into a slot holding another item |
  | Both | A new input whose result differs from what sits in the output | the output check fails until someone empties it |

- **Fix (port, face rules only; by hand anything valid still goes in):**
  - Cask, `ingredientKeepsWorking`: an ingredient goes only into the slot that already holds it; a new one only when
    one recipe for the cask's juice (any juice while it's empty) uses it together with everything already in, each
    stack by its own ingredient, and makes what's already in the output.
  - Cask, `juiceKeepsWorking`: juice from above only of the cask's own type (any while empty), and only one some
    recipe can use with the ingredients already in.
  - Press, `keepsWorking`: the item queued for fermenting is slot 1, else what slot 0 mashes into. Slot 0 and slot 1
    take only that item (or anything when nothing is queued) whose recipe takes the bottle in slot 2 and makes what's
    in the output; slot 2 takes only the bottle the queued item needs.
  - A refused item waits in the hopper (or funnel, or Create arm) until the block can take it.
- **Not changed:** the press still takes nothing from above (1.5.5: sides in, bottom out). Empty wine bottles the cask
  gets back from juice still drop on top when its bottle slot is full.
- **Jar:** Vinery `-03` rebuilt and installed in the local `mods/` 2026-10-20.
- **Tests:** VB-93, VB-94.

## 11. Every alcoholic drink: half a rotten shank, Intoxication, Nausea (pack + Vinery port) (BUILT 2026-10-20, scratch server PASSED 2026-10-22 (SQ-OM-00, SQ-OM-04), not tested in game)

User, 2026-10-20: every alcoholic drink gets the alcohol food effect (Intoxication), Fruits Delight's Bellini
included; every drink gives half a shank of rotten hunger and no saturation and can be drunk at any time; drinks
without a Tipsy / drunk effect also give 5 s of Nausea (10 s for drinks made from other drinks); the tooltip line
reads "Intoxicating Drink".

- **Which drinks (44):** the 16 Brewin' drinks, the 26 Vinery wines and ciders, Fruits Delight's Bellini Cocktail and
  Engineer's Delight's Bottle of Wodka. Farmer's Delight / Farmer's Respite apple ciders are left out: they aren't
  alcoholic (no Intoxication, no Nausea, their food values unchanged).
- **Food values ([alcohol_drinks.js](../kubejs/startup_scripts/cooking/alcohol_drinks.js), `global.alcohol`):**
  hunger 1, saturation modifier 0, `alwaysEat`, the drink's own effects kept. AppleSkin draws a food's shanks rotten
  when one of its food effects is harmful (`FoodHelper.isRotten`), and Intoxication is, so every drink shows half a
  rotten shank. Built with vanilla's `FoodProperties.Builder` rather than KubeJS's `FoodBuilder`, whose `effect()`
  can't make a hidden effect; the two-overload `effect(...)` call was checked in the pack's Rhino build with a mock
  class (the `MobEffectInstance` overload is picked).
- **Intoxication:** Brewin's drinks keep theirs (90 to 210 s, hidden). The others get 90 s (Beer's), hidden the same
  way (no icon, no particles). While it lasts, food gives no saturation (E1).
- **Nausea:** none when the drink's food effects hold Tipsy (all of Brewin's), or when it's in `ownDrunkEffect`
  (Bottle of Wodka: its own code gives Nausea 60 s). Otherwise 5 s, or 10 s for `mixed`: Jellie Wine (fermented from
  Apple, Chenet and Bolvar wine) and Mojang Noir (from Red Wine). Every wine starts from grape juice, which doesn't
  count. The Bellini's own Nausea 5 s is replaced, not doubled.
- **Vinery port (`DrinkBlockItem`, `-03` rebuilt):** a wine used to only shrink the stack, so food values never
  applied. It now calls `eat()` when edible (hunger, food effects; a creative player keeps the bottle, as with any
  food) and plays the drink sound at the end. The tooltip lists the food effects under the wine's own effect, in the
  same format; "No Effects" only shows when there are neither.
- **Tooltip (Creative Common `food/IntoxicatingDrinkTooltip`, `[alcohol] quarkLine`, 2026-10-20):** Quark's food tooltip
  ends with a saturation word chosen from six buckets of saturation x nutrition x 2 (19+: "Full-Course Meal", 10 to
  19: "Fulfilling Meal", 8: "Hearty", 6: "Meal", 2: "Light Meal", under 2: "Light Snack"), with a "bad" set
  ("Disgusting Snack" for the lowest) when a food effect is harmful. Every drink here is hunger 1 / saturation 0 with
  a harmful Intoxication, so all 44 read "Disgusting Snack", together with rotten flesh and raw chicken. No saturation
  value gives a bucket of its own (the user's "half a shank of saturation" idea: 1 point is still the lowest bucket,
  and a lang override of any bucket renames every other food in it), so the listener runs after Quark's on
  `RenderTooltipEvent.GatherComponents` and swaps that word for "Intoxicating Drink" (gray, lang key
  `tooltip.creativeengineering.intoxicating_drink`) on any food whose effects hold Intoxication: the drink list in
  `global.alcohol` stays the single source. The earlier KubeJS rename of the effect line (`alcohol_tooltip.js`,
  "Intoxicating Drink (m:ss)") is retired so the two lines don't repeat each other: the effect line reads
  "Intoxication (m:ss)" like every other food effect. Not renamed: the keg tank and JEI Fermenting tooltips (8.2),
  the effect itself (HUD, JEED).
- **Gotcha:** Fruits Delight's `FDFoodItem.getFoodProperties(stack, entity)` builds its own values for items with
  fruit NBT and falls back to the item's base values otherwise; the Bellini has none, so ours apply.
- **Tests:** AL-85 to AL-90.

- **History:** 2026-10-20 built (KubeJS food values + port `-03`). 2026-10-20 later: Quark's saturation word
  replaced from Creative Common (1.1.01), effect-line rename retired, AL-88 rewritten.

### Planned from the scratchpad (2026-10-24)

- [TODO](todo/TODO.md) `alcohol/pack #3` **Vintage brews.** The six Pickling Jar brews (Goblin's Piss, Berserker's Brew,
  Brashing Tonic, Dragon's Belch, Frost Fermenta, Oily Oaf; [vintage_delight.md](vintage_delight.md) §2.7a) all carry
  vanilla effects in the NBT of a `minecraft:potion`. Plan: put them in their own category and redesign their effects and
  crafts. They are potions, not registered items, so they are outside the 44 drinks of this section. Open: what "own
  category" means (JEI category, real items with a tab group, or a tooltip line) and the effects per brew. Brief:
  [briefs/alcohol_pack.md](briefs/alcohol_pack.md).

## 12. Coasters: modeled items only, Display Delight models, WTHIT (port + pack + Creative Common) (BUILT 2026-10-20, scratch server PASSED 2026-10-22 (SQ-OM-02), not tested in game)

Your scratchpad note (2026-10-19, TODO `alcohol/brewinport #8`): remove anything without a model from coasters; add
Display Delight models to the coaster; add WTHIT info.

- **Only modeled items (port):** `CoasterBlockEntity#addItem` now checks `CoasterBlockEntity.accepts`: with the new
  `[coaster] taggedItemsOnly` (`brewinandchewin-common.toml`, default true) a stack must be in
  `#brewinandchewin:placeable_on_coaster`. False takes any item, as 4.5.0 does. A rejected item falls through to
  4.5.0's empty-hand path, so it takes the top item off a coaster that has one, the same as clicking a full coaster.
  Items already on a coaster stay, and come off as before.
  - The tag ships in the port, written by `tools/port_resources.py` from its 96 coaster files (the other mods' ids
    optional, so 1.21-only Farmer's Delight foods don't break it).
  - The pack adds `#creativedelight:placeable_on_coaster` into it
    ([placeable_tags.js](../kubejs/data/creativedelight/kubejs/creativedelight/placeable_tags.js), key `i.t_bnc_placeable_on_coaster`).
- **Display Delight models (pack + port):** [tools/gen_coaster_models.py](../tools/gen_coaster_models.py) writes 56 coaster
  files to `kubejs/assets/creativeengineering/brewinandchewin/coaster/`, one per food DD links that the port has no
  model for (the other 29 keep the port's). Each points at the DD block's default model: the food on its own first,
  then on a Small Food Plate, then on a Food Plate. DD's models are full blocks (a plate is 14 px), so the file has a
  `"scale"` that brings the widest side to 10 px (the port's bowls are 12); 33 are scaled (0.63 to 0.95), 23 already
  fit. `"scale"` is new in the port's coaster renderer, applied around the bottom center, default 1.
  - Generator `coaster_models` (groups client, food) runs before `placeable_catalog`.
- **Tooltip line:** `tools/placeable_catalog.py` now tags only modeled items, the port's plus the generated ones:
  152 (was 158). Out: the Vinery wines and juices and Farmer's Respite teas (60), which have no coaster model.
  In: the 56 Display Delight foods. This replaces the 8.3 allow-list (8.9 Q3).
  **Superseded 2026-10-29:** the generator now reads DD 1.7.0 multipart blockstates, Vinery wine bottles and
  Vintage mason jars, and borrows models for same-sprite drinks; the tag is 304. Current state: [kitchen.md](kitchen.md) 2.3.
- **WTHIT (Creative Common `compat/WthitCoaster`, `waila_plugins.json`, needs `brewinandchewin`):** the header already shows
  the top item (the coaster's clone stack). The body adds "Coaster: 3 / 4", with "(put away)" once a sneak-use took
  the coaster itself back, and one line per item in the order they went on, repeats folded ("2x Beer"). It reads the
  client's synced slots through the block entity's `getItems()` by reflection, so Creative Common doesn't compile against the
  port. CEConfig `[coaster] wthit` (default true).
- **Not done:** coaster models for the Vinery and Farmer's Respite drinks (they can't go on any more); a
  `/dumpplaceable` rerun (the catalog used the last dump).
- **Tests:** AL-91 to AL-96.

## 13. Village houses in vanilla villages (port `-04`) (BUILT 2026-10-20, scratch server PASSED 2026-10-22 (SQ-OM-00, SQ-OM-08, SQ-OM-14, SQ-OM-15), not tested in game)

User, 2026-10-20: wire up all of the Vinery village houses to spawn in their respective village, as often as the
flower houses (without competing; boost if rarer than the average house type), blocks remapped to the port.
Reverses alcohol_part_b.md 5 D8 (skipped 2026-10-19). [TODO](todo/TODO.md) `alcohol/vineryport #14`.

- **Pieces (`forge/upstream/VillageHouses`):** the four 1.5.5 NBTs (alcohol_part_b.md 5 D8 table) are read from the donor jar like every
  other upstream file (ARR, nothing shipped: D13), their palettes rewritten in memory and served as
  `vinery:vinery_plains`, `vinery_taiga`, `vinery_savanna`, `vinery_desert` (`data/vinery/structures/`). Rewrites:
  `apple_press` -> `wine_press`, `red_grape_crate` / `white_grape_crate` -> the red / white grape bag (the crates were
  removed, 10.10), `cherry_wall_hanging_sign` -> a chain stub (`minecraft:chain`, axis across the sign's facing; no hanging
  signs in 1.19.2, D9; a wall hanging sign is held by the blocks beside it, so a wall sign (needs a block behind) and a
  hanging lantern (needs one above) both popped on the scratch server, a chain needs no support), then the alcohol_part_b.md 4.1 id
  map. `DataVersion` is set to this game's (the files say 1.20.1; the fixer only upgrades). Every other palette entry
  already exists (the 1.3-era `basket`, `chair`, `table`, `loam`, `cherry_*` ids are the ones the port kept).
- **Pools:** at `ServerAboutToStartEvent` (normal priority) each house is appended as a legacy rigid piece to
  `minecraft:village/<plains|taiga|savanna|desert>/houses` (the jigsaw in every NBT is a `minecraft:building_entrance`,
  the name village streets look for; the desert one also keeps vanilla's cat jigsaw). A piece already in the pool is
  skipped (singleplayer keeps builtin pools across world loads). The log line
  `[vinery-houses] vinery:vinery_plains added to minecraft:village/plains/houses at weight N (...)` is the evidence.
  Creative Common's PoolTuner runs after it (lowest priority), rebinds the processor list to the world's and records the
  piece in `pool_config.json` like any mod-injected house, so the pack can reweight or disable it there.
- **Frequency ("same as the flower houses"):** the pack's flower house (`villager_flower_house:flower_house`) is a
  standalone structure in the settlements group (frequency 0.27 on a 48/12 grid), not a village piece, so there is no
  pool weight that equals it. Read as "an average house". Two layers: `[village_houses] weight = 0` (default) appends
  at the average weight of the pieces in the pool at that moment, which is the vanilla pool before the other mods'
  houses and PoolTuner's retuning (plains 96/44 -> 2, the others 2 too; the scratch log of 2026-10-20 shows it). Then
  `pool_config.json` (PoolTuner, the pack's one place for house weights) lifts them to the average of the finished
  pool: `current` 3 (plains, savanna, desert) and 3.5 (taiga), from the 2026-10-20 scratch report (plains 47 live
  pieces, effective total 410 -> average 8.7 against the house's 6 at weight 2; taiga 38 / 380; savanna 42 / 398;
  desert 39 / 362), so each house takes about 2.2 to 2.7 % of its village's house slots, the mean share of a piece
  (roughly one winery per two villages). A positive config value is a fixed weight for the append; `pool_config.json`
  still wins. "Without competing" = the houses join the pool rather than the structure group, so they never veto
  another structure; the other houses lose about 2 % of their share.
- **Winemaker:** the cask is the Winemaker POI, so a house with a cask turns one unemployed villager into a Winemaker,
  as alcohol_part_b.md 5 D8 noted. Houses hold placed wines (2 to 3 each, 1.5.5 stock) and stocked containers (apples, cherries, grapes,
  apple sapling, apple juice; jungle grape seeds in the desert drawer); no loot tables, so Lootr isn't involved.
- **Config (`vinery-common.toml`):** `[village_houses] enabled` (true), `weight` (0 = pool average). A world needs a
  restart for either (pools are read at server start).
- **Tests:** VB-95 to VB-98.

