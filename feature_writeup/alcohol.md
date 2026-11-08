# Alcohol: Brewin' and Chewin' and Vinery backports

> **Creative Brewing (2026-10-31):** both ports are now members of the Creative Brewing bundle: sources in
> `development/creativeBrewing/members/vinery` and `.../members/brewin` (Gradle projects `:backportedVinery`, `:backportedBrewin`
> unchanged), shipped only inside `creativebrewing-<v>.jar`, display names "Vinery (Creative Brewing)" and "Brewin' &
> Chewin' (Creative Brewing)" (`foundation`: "Foundation (Creative Brewing)"; both renamed 2026-11-04, plan_cb_publish.md M5). Vinery `-08` and Brewin' `-05` are the first nested builds (built, not installed). Paths
> `development/backportedVinery` / `development/backportedBrewin` below mean those folders. See [creative_brewing.md](creative_brewing.md).

> **Merge note (2026-10-25):** this port is to be merged with the other Farmer's Delight add-on backports into a single jar, **Creative Delight**. The inventory summary, cross-port interactions and the merge process are in [creative_delight.md](creative_delight.md). This file stays the detailed record for Brewin' and Chewin' (Part A) and Vinery (Part B); its registry namespace and test prefix do not change.

Two backports live in this file:

- **Part A (sections 0 to 10, tests `AL-`): Brewin' and Chewin'.** A full port of 4.5.0+1.21.1 as "Backported Brewin";
  `-02` DEPLOYED with pack 1.0.8 (2026-10-19) in place of the 2.0 jar; `-03` (10.13, 10.16, 12) shipped in 1.1.01
  (deployed 2026-10-22). `-04` (effect icons on the HUD for the food effects, TODO `potions/effects #8`) installed in
  `mods/` 2026-10-31 00:30, not yet booted.
- **Part B (sections B0 to B8, tests `VB-`): Let's Do Vinery.** The pack ships Forge 1.3.10; ported from Fabric
  1.5.5 (Minecraft 1.21.1) as "Backported Vinery"; `-02` DEPLOYED with pack 1.0.8 (2026-10-19) in place of 1.3.10,
  `-03` (B7.14, 10.9 to 10.14) and `-04` (village houses, section 13; Grape Planting JEI page) shipped in 1.1.01
  (deployed 2026-10-22); `-05` (sheared grass slab) in 1.1.04 (client 2026-10-29, server still 1.1.03). `-06` (crop
  seasons for grapes and fruit leaves, Jellie Wine's four effects, Double Jump fix) installed in `mods/` 2026-10-31 00:41,
  not yet booted. It starts after Part A's History.

# Part A: Brewin' and Chewin' (Backported Brewin)

This pack shipped Brewin' and Chewin' 1.19-2.0 (Forge 1.19.2) up to 1.0.7. It now ships a **full port** of 4.5.0+1.21.1,
built as our own Forge 1.19.2 jar ("Backported Brewin"). Where the two versions differ, the port follows 4.5.0 unless a line below
says otherwise. This part holds the gap analysis (sections 2.1 to 2.6), the port plan (section 3) and the decisions
(section 4). Both versions are MIT (Umpaz).

## 0. At a glance

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
| Coasters (12) | BUILT 2026-10-20 (Brewin' `-03` + patcherMod 1.0.10), shipped in 1.1.01; extended 2026-10-29 to 302 foods and drinks. Scratch server PASSED 2026-10-22 (SQ-OM-02), not tested in game |
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

| # | Effect | Behaviour |
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

### 2.6 Other mods that touch Brewin' and Chewin'

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
| Fire | `minecraft:fire`, `soul_fire`, `architects_palette:nether_brass_fire`, `mca:infernal_flame`, `creativeengineering:dark_fire` | no |
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
| no | `minecraft:coal_block`, charcoal blocks, `blocky_siege:fire_bomb_*`, fire coral, `botania:endoflame` / `exoflame` | not burning; named by colour or theme only |

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
finding 9). The port no longer converts Brewin' and Chewin' 2.0 content: `BnCRemaps` (the `item_coaster`,
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
| [../kubejs/generation_scripts/food/can_types.json](../kubejs/generation_scripts/food/can_types.json) | OPEN: Canned Kimchi still copies `brewinandchewin:satisfaction` from the 10-02 food dump. 4.5.0 kimchi has no effect, so after a restart with the port: `/dumpfood`, then `python3 tools/food_catalog.py` and `python3 tools/gen_can_types.py` (hand edits would be overwritten) |
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
- Coaster models are extra baked models drawn quad by quad with the texture-modifier tint (no wrapped model or colour
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
- [ ] **AL-74 Machines running (10.13):** keg within 2 blocks of an IE coke oven's centre block -> normal while idle, warm while it cooks; IE furnace heater only while powered and heating; PneumaticCraft air compressor only while burning fuel
- [ ] **AL-75 Fueled blaze (10.13):** keg beside a blaze burner with no fuel -> normal; feed it -> warm; liquid blaze burner the same; blaze enchanter warm only with experience in its tank
- [ ] **AL-76 Cold blocks (10.13):** a temperate-biome keg ringed by snow blocks, thin ice, powder snow, permafrost or cryosol -> chilly/cold; a lit furnace beside them cancels one
- [ ] **AL-77 Tooltips (10.13, split 10.18):** furnace, brazier, blaze burner, campfire -> "Heats the surrounding environment" with "Useful for keg brewing and wine casking" on the next line; snow block, ice, permafrost -> "Cools the surrounding environment" and the same second line; keg -> "Sensitive to hot and cold blocks in the environment"; an FD cooking pot on a lit furnace still doesn't cook
- [ ] **AL-78 Tankard slot icon (10.16):** open an empty keg -> the container slot shows the grey tankard outline, not black and purple
- [ ] **AL-79 Leftover slot (10.16):** ferment a recipe with a honey bottle ingredient -> the glass bottle lands in the new slot right of the output, nothing pops out; it can't be placed into; shift-click moves it to the inventory
- [ ] **AL-80 Leftovers overflow and stall (10.16):** with the leftover slot full of glass bottles (64) -> the next bottle goes to the empty-container slot under the fluid slot; with that slot holding buckets instead (empty milk buckets through the fluid slot first) the next honey-bottle batch waits at 0 progress and resumes once the bottles are taken out; `stallWhenFull = false` -> it ferments and throws the bottle
- [ ] **AL-81 Old keg loads (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* a keg placed with `-02` holding ingredients, a tankard and a drink keeps all of them after the `-03` install
- [ ] **AL-82 Slot rules (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* dirt won't go in the ingredient grid or the container slot by hand or shift-click; a tankard goes in the container slot; a honey bottle goes in the fluid slot by shift-click
- [ ] **AL-83 Input from every face (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* hoppers into the side and the bottom feed wheat to the grid, tankards to the container slot and honey bottles to the fluid slot; a hopper on top still feeds the grid
- [ ] **AL-84 Leftovers by hopper (10.16):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-01).* a hopper under the keg pulls finished items, empty bottles from the fluid slot and the leftover slot
- [ ] **AL-85 Drink food values (11):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-04).* hover Beer, Red Wine, the Bellini and Bottle of Wodka -> AppleSkin shows half a rotten (green) shank and no saturation; drink one at full hunger -> it can be drunk, hunger goes up by half a shank, saturation doesn't
- [ ] **AL-86 Intoxication everywhere (11):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-04).* drink Red Wine, the Bellini and Bottle of Wodka (one at a time, after milk) -> the hunger bar turns Intoxicated for 1:30, no Intoxication icon or particles; eat bread -> hunger fills, saturation doesn't
- [ ] **AL-87 Nausea (11):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-04).* Red Wine -> Nausea 0:05; Jellie Wine and Mojang Noir -> 0:10; the Bellini -> one Nausea 0:05 (not two); Beer -> no Nausea; Bottle of Wodka -> its own Nausea 1:00 only
- [ ] **AL-88 Tooltip name (11):** Beer, Vodka, the Bellini, Bottle of Wodka and a wine show Quark's grey saturation word as "Intoxicating Drink" (not "Disgusting Snack"); rotten flesh still says "Disgusting Snack"; the effect lines read "Intoxication (m:ss)"; a wine lists its own effect, then Intoxication and Nausea; `[alcohol] quarkLine = false` brings "Disgusting Snack" back
- [ ] **AL-89 Wine still a wine (11):** drink a wine in survival -> its own effect (aged level and duration), the empty wine bottle back; in creative the bottle stays; sneak-place still works; Apple Cider from Farmer's Delight unchanged (no rotten shank)
- [ ] **AL-90 Log (11):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-00, 44 of 44 drinks).* after the restart `logs/latest.log` has `[alcohol] food values set on 44 of 44 drinks` and no `[alcohol]` warnings
- [ ] **AL-91 Coaster takes modelled items only (12):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-02).* right-click an empty coaster with a Vinery wine, a Farmer's Respite tea and a stone block -> none goes on; with a beer, an apple and FD hot cocoa -> each goes on with its own model; `[coaster] taggedItemsOnly = false` in `brewinandchewin-common.toml` -> the wine goes on again, drawn flat
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
  - So it works with other mods' items (Vinery wines, Farmer's Respite teas), but only the modelled ones look placed.
- **Display Delight overlap.** A plain right-click on a coaster always goes to the coaster: the block's own use runs
  before the held item's. A **sneak** right-click with something in hand skips the block, so a Display Delight food
  (which places on sneak) would try to put a plate on top of the coaster. Vinery bottles place on a plain click
  anywhere except a coaster. AL-52 checks both.
- **Plan:**
  - Add a fifth line to the placeable pipeline: "Placeable on a coaster", tag
    `creativeengineering:placeable_on_coaster`.
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
| Mead (`fermenting/mead_from_honey`) | wheat, wheat seeds, sweet berries / 1000 honey | wheat seeds -> red mushroom. Vinery's mead is removed from that port (B7.6), so this is the only mead |
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

- Vinery's mead is removed from that port (B7.6), with no conversion (your rule, 2026-10-18: neither port keeps old
  items working across versions). Brewin's mead (8.6) is the only one.
- **Test:** VB-54.

### 8.9 Questions for you

| # | Question | My default |
|---|---|---|
| Q1 | The black and purple texture: which screen is "plating"? The keg GUI, the JEI Fermenting page (the pouring row), or the recipe book? | ANSWERED 2026-10-19: the keg GUI's tankard slot icon; fixed in `-03` (10.16) |
| Q2 | Keg fluid slot: two slots (in / empty out, 2.0 style) or one slot where the empty container stays put? | BUILT with the default: two slots |
| Q3 | "Placeable on a coaster": items with a coaster model plus wines and juices, or every item? | BUILT with the default: modelled items plus Vinery and Farmer's Respite drinks |
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
- 2026-10-17: port begun in `development/backportedBrewin`: build files, licence, about 45 upstream classes (blocks, keg
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
  the racks that lay bottles down; B7.11 settled. VB-59 updated, VB-66 added.
- 2026-10-18 late: 10.5 to 10.7: Vinery banner crafting recipe, WTHIT stem lines (variety, ripe, leaves pending), and
  the effect-description check (nothing to reuse). VB-64, VB-65 added.
- 2026-10-18 late: section 10 built. All advancements removed (patcherMod switch), keg JEI [+] fills and empties
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
- 2026-10-19: pack 1.0.9 DEPLOYED (15:54) with `-02` unchanged. Late: Vinery `-03` and patcherMod 1.0.10 went into the
  local `mods/` (22:27), Brewin' `-03` didn't (`mods/` has `-02`). Until it does, Vinery logs "no
  BnCHeatSources.climate" and wine storage is never cold or warm. Writeup and TODO statuses brought up to date.
- 2026-10-19 late: 10.16 built into `-03` (tankard slot icon, keg leftover slot, slot rules, input from every face) plus
  the pack's compost rows and BlockRunner tiers. `alcohol/brewinport #3` (recipe-book tank filling) dropped from the
  TODO: the pack has no recipe book. AL-78 to AL-84 added.
- 2026-10-20: 11 built: every alcoholic drink is half a rotten shank, no saturation, always drinkable, Intoxicating,
  with Nausea where it has no drunk effect; Vinery `-03` rebuilt (wines eat, food-effect lines) in `build/libs/` and
  `jar_archive/`, not installed (the game was running). AL-85 to AL-90 added.
- 2026-10-20: 12 built (`alcohol/brewinport #8`): coasters take only items with a coaster model, Display Delight foods
  get one, WTHIT lines. Brewin' `-03` and patcherMod 1.0.10 rebuilt into `build/libs/` and `jar_archive/`, not installed
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

## 10. Third list (2026-10-18 late, BUILT, not tested in game)

Your list, covering both ports and the time master ([time.md](time.md) 3.9). 10.1 to 10.8 were built 2026-10-18
into Brewin' `-02`, Vinery `-02` and patcherMod 1.0.8 (rebuilt under the same names, `jar_archive/` copies replaced
by hand). 10.9 onward came later, in your follow-up lists.

| Sections | Jars | Where (2026-10-19 late) |
|---|---|---|
| 10.1 to 10.8 | Brewin' `-02`, Vinery `-02`, patcherMod 1.0.8 | DEPLOYED with 1.0.8, still live in 1.0.9 |
| 10.9 to 10.12, 10.14 | Vinery `-03` | local `mods/` (22:27), not deployed |
| 10.15, 10.17, 10.18 | Vinery `-03` rebuilt; Brewin' `-03` (10.18) | local `mods/` since 2026-10-20, not deployed; 10.15 / 10.17 mostly TESTED 2026-10-20 |
| 10.13 | Brewin' `-03`, Vinery `-03`, patcherMod 1.0.10 | Vinery and patcherMod in local `mods/` (22:27); Brewin' `-03` in `build/libs/` only, so cold / warm storage is off until it's installed |

### 10.1 Decisions

- **D3 (Vinery): DECIDED, keep `trippy`.** It stays registered; the echo "discombobulate" tool effect uses it.
- **T1, T3, T4, T7 (time): DECIDED, the recommendations** ([time.md](time.md) section 4).

### 10.2 Advancements: all of them are gone

- **What:** `[advancements] enabled = false` in `creativeengineering-common.toml`, patcherMod
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
| Chenet | 5 | jungle juice; tied with King Danis at 12 juice. Chenet kept: a 1.5.5 wine, while King Danis is a 1.3.10 keeper on hold (B7.11) |

- Upstream's smithing recipe (banner + netherite ingot -> 2 banners) is still there, minus the 1.20 template.
- The banner tooltip's first line now reads "Crafted from the seven finest wines" (port override).
- **Test:** VB-64.

### 10.6 WTHIT on grapevine stems and lattices (port)

- `VineryWthitClient.stem`, for every `StemBlock` (grapevine stem, the ten lattices, 1.3.10's lattice stem). From the
  block state, so no server data:
  - **"Growing: Taiga Red Grapes"**: the variety, named by its grape item;
  - **"Ripe: ready to pick"** at the last stage;
  - **"Grapevine leaves still to grow"** while a grapevine stem's leaves are pending (the B4.6 gap);
  - **"Nothing planted"** on an empty stem or lattice.
- Growth % stays patcherMod's crop line (`crop_growth.json` already lists the stem and all ten lattices), so nothing
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
- **Where JEED could help later:** B7.4 (pinned). JEED's bundle describes many food-mod effects, which the
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
- B7.11 is settled by this (VQ3: small racks).
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

Your calls, 2026-10-19 (after the B4.1 look-alike list and the 10.9 legacy-block outline):

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
  bags; compost rows for the crates removed (the bags already had them); patcherMod `crop_growth.json` lists only
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
  overlap a neighbour in the 4x rack until their models are tweaked. The single-bottle rack stands bottles upright, as
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
  port reads these from patcherMod's `time.WineTime` while `[time]` is on ([time.md](time.md) 3.7).
- **Text:** every block item in either tag: "Heats/Cools the surrounding environment", then "Useful for keg brewing
  and wine casking" on its own line (split 2026-10-20, 10.18); keg: "Sensitive to hot and cold blocks in the environment"; wine racks (9x, 4x, single-bottle) and the wine
  box: "Wine aging is sped up by dark and cold environments, slowed by warm ones" (the last three words added with
  warm). WTHIT on storage, the stored wine's tooltip ("In storage: ...") and `/wine storage` ("Storage here: ...") use
  the table's labels; JEI Wine Aging adds the cold and warm lines.
- **Tests:** AL-73 to AL-77, VB-76 to VB-80.

### 10.14 Slim bottles in the 4x rack and wine box; Stal, Sun-kissed, Magnetic medium (port) (BUILT 2026-10-19; sizes TESTED 2026-10-20, slim bottles replaced by 10.17)

Your list, 2026-10-19: Stal and Sun-kissed medium and slimmed in the 4x rack; Strad, Aegis, Apple Cider, Chenet,
Mellohi, Creeper's Crush, Eiswein slimmed in the 4x rack; Magnetic medium with a new wine box look (Strad's shape in
Magnetic's colours, made with the palette swap tool) and a slim 4x look; slim bottles in the wine box too; Praetorian
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
  all 10 wines) and adds `vinery_backport:block/wine_slim/<id>` and `.../wine_box/<id>` (models and textures) to the
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
- **Vinery grass slab tint (pack, `alcohol/vineryport #6`):** the slab used the same grass colour as grass blocks, but
  Rubidium blends the vanilla grass block's tint per vertex across its neighbours (biome blend above 0) and colours a
  modded block flat, one colour per block, so the slab read a shade off. `blocktags.js` puts `vinery:grass_slab` in
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
  the 8 px Strad body comes out 6 px and the diagonal neighbours, 0.35 / 0.33 apart, don't overlap. No model change.
  Smalls, Praetorian and every other rack draw as before.
- **Magnetic inverted:** its recolour uses the swap tool's `--invert` mapping (the shape's darkest colour takes the
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
  Aegis use the inverted mapping like Magnetic. `DROPS` leaves colour classes out of a wine's palette before the swap:
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
  item. In the port each slot takes only what a recipe uses there (press B7.2; the cask's own 1.5.5 face rules) and
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
- **Tooltip (patcherMod `food/IntoxicatingDrinkTooltip`, `[alcohol] quarkLine`, 2026-10-20):** Quark's food tooltip
  ends with a saturation word chosen from six buckets of saturation x nutrition x 2 (19+: "Full-Course Meal", 10 to
  19: "Fulfilling Meal", 8: "Hearty", 6: "Meal", 2: "Light Meal", under 2: "Light Snack"), with a "bad" set
  ("Disgusting Snack" for the lowest) when a food effect is harmful. Every drink here is hunger 1 / saturation 0 with
  a harmful Intoxication, so all 44 read "Disgusting Snack", together with rotten flesh and raw chicken. No saturation
  value gives a bucket of its own (the user's "half a shank of saturation" idea: 1 point is still the lowest bucket,
  and a lang override of any bucket renames every other food in it), so the listener runs after Quark's on
  `RenderTooltipEvent.GatherComponents` and swaps that word for "Intoxicating Drink" (grey, lang key
  `tooltip.creativeengineering.intoxicating_drink`) on any food whose effects hold Intoxication: the drink list in
  `global.alcohol` stays the single source. The earlier KubeJS rename of the effect line (`alcohol_tooltip.js`,
  "Intoxicating Drink (m:ss)") is retired so the two lines don't repeat each other: the effect line reads
  "Intoxication (m:ss)" like every other food effect. Not renamed: the keg tank and JEI Fermenting tooltips (8.2),
  the effect itself (HUD, JEED).
- **Gotcha:** Fruits Delight's `FDFoodItem.getFoodProperties(stack, entity)` builds its own values for items with
  fruit NBT and falls back to the item's base values otherwise; the Bellini has none, so ours apply.
- **Tests:** AL-85 to AL-90.

- **History:** 2026-10-20 built (KubeJS food values + port `-03`). 2026-10-20 later: Quark's saturation word
  replaced from patcherMod (1.1.01), effect-line rename retired, AL-88 rewritten.

### Planned from the scratchpad (2026-10-24)

- [TODO](todo/TODO.md) `alcohol/pack #3` **Vintage brews.** The six Pickling Jar brews (Goblin's Piss, Berserker's Brew,
  Brashing Tonic, Dragon's Belch, Frost Fermenta, Oily Oaf; [vintage_delight.md](vintage_delight.md) §2.7a) all carry
  vanilla effects in the NBT of a `minecraft:potion`. Plan: put them in their own category and redesign their effects and
  crafts. They are potions, not registered items, so they are outside the 44 drinks of this section. Open: what "own
  category" means (JEI category, real items with a tab group, or a tooltip line) and the effects per brew. Brief:
  [briefs/alcohol_pack.md](briefs/alcohol_pack.md).

## 12. Coasters: modelled items only, Display Delight models, WTHIT (port + pack + patcherMod) (BUILT 2026-10-20, scratch server PASSED 2026-10-22 (SQ-OM-02), not tested in game)

Your scratchpad note (2026-10-19, TODO `alcohol/brewinport #8`): remove anything without a model from coasters; add
Display Delight models to the coaster; add WTHIT info.

- **Only modelled items (port):** `CoasterBlockEntity#addItem` now checks `CoasterBlockEntity.accepts`: with the new
  `[coaster] taggedItemsOnly` (`brewinandchewin-common.toml`, default true) a stack must be in
  `#brewinandchewin:placeable_on_coaster`. False takes any item, as 4.5.0 does. A rejected item falls through to
  4.5.0's empty-hand path, so it takes the top item off a coaster that has one, the same as clicking a full coaster.
  Items already on a coaster stay, and come off as before.
  - The tag ships in the port, written by `tools/port_resources.py` from its 96 coaster files (the other mods' ids
    optional, so 1.21-only Farmer's Delight foods don't break it).
  - The pack adds `#creativeengineering:placeable_on_coaster` into it
    ([placeable_tags.js](../kubejs/data/creativedelight/kubejs/creativeengineering/placeable_tags.js), key `i.t_bnc_placeable_on_coaster`).
- **Display Delight models (pack + port):** [tools/gen_coaster_models.py](../tools/gen_coaster_models.py) writes 56 coaster
  files to `kubejs/assets/creativeengineering/brewinandchewin/coaster/`, one per food DD links that the port has no
  model for (the other 29 keep the port's). Each points at the DD block's default model: the food on its own first,
  then on a Small Food Plate, then on a Food Plate. DD's models are full blocks (a plate is 14 px), so the file has a
  `"scale"` that brings the widest side to 10 px (the port's bowls are 12); 33 are scaled (0.63 to 0.95), 23 already
  fit. `"scale"` is new in the port's coaster renderer, applied around the bottom centre, default 1.
  - Generator `coaster_models` (groups client, food) runs before `placeable_catalog`.
- **Tooltip line:** `tools/placeable_catalog.py` now tags only modelled items, the port's plus the generated ones:
  152 (was 158). Out: the Vinery wines and juices and Farmer's Respite teas (60), which have no coaster model.
  In: the 56 Display Delight foods. This replaces the 8.3 allow-list (8.9 Q3).
  **Superseded 2026-10-29:** the generator now reads DD 1.7.0 multipart blockstates, Vinery wine bottles and
  Vintage mason jars, and borrows models for same-sprite drinks; the tag is 304. Current state: [kitchen.md](kitchen.md) 2.3.
- **WTHIT (patcherMod `compat/WthitCoaster`, `waila_plugins.json`, needs `brewinandchewin`):** the header already shows
  the top item (the coaster's clone stack). The body adds "Coaster: 3 / 4", with "(put away)" once a sneak-use took
  the coaster itself back, and one line per item in the order they went on, repeats folded ("2x Beer"). It reads the
  client's synced slots through the block entity's `getItems()` by reflection, so patcherMod doesn't compile against the
  port. CEConfig `[coaster] wthit` (default true).
- **Not done:** coaster models for the Vinery and Farmer's Respite drinks (they can't go on any more); a
  `/dumpplaceable` rerun (the catalog used the last dump).
- **Tests:** AL-91 to AL-96.

## 13. Village houses in vanilla villages (port `-04`) (BUILT 2026-10-20, scratch server PASSED 2026-10-22 (SQ-OM-00, SQ-OM-08, SQ-OM-14, SQ-OM-15), not tested in game)

User, 2026-10-20: wire up all of the Vinery village houses to spawn in their respective village, as often as the
flower houses (without competing; boost if rarer than the average house type), blocks remapped to the port.
Reverses B5 D8 (skipped 2026-10-19). [TODO](todo/TODO.md) `alcohol/vineryport #14`.

- **Pieces (`forge/upstream/VillageHouses`):** the four 1.5.5 NBTs (B5 D8 table) are read from the donor jar like every
  other upstream file (ARR, nothing shipped: D13), their palettes rewritten in memory and served as
  `vinery:vinery_plains`, `vinery_taiga`, `vinery_savanna`, `vinery_desert` (`data/vinery/structures/`). Rewrites:
  `apple_press` -> `wine_press`, `red_grape_crate` / `white_grape_crate` -> the red / white grape bag (the crates were
  removed, 10.10), `cherry_wall_hanging_sign` -> a chain stub (`minecraft:chain`, axis across the sign's facing; no hanging
  signs in 1.19.2, D9; a wall hanging sign is held by the blocks beside it, so a wall sign (needs a block behind) and a
  hanging lantern (needs one above) both popped on the scratch server, a chain needs no support), then the B4.1 id
  map. `DataVersion` is set to this game's (the files say 1.20.1; the fixer only upgrades). Every other palette entry
  already exists (the 1.3-era `basket`, `chair`, `table`, `loam`, `cherry_*` ids are the ones the port kept).
- **Pools:** at `ServerAboutToStartEvent` (normal priority) each house is appended as a legacy rigid piece to
  `minecraft:village/<plains|taiga|savanna|desert>/houses` (the jigsaw in every NBT is a `minecraft:building_entrance`,
  the name village streets look for; the desert one also keeps vanilla's cat jigsaw). A piece already in the pool is
  skipped (singleplayer keeps builtin pools across world loads). The log line
  `[vinery-houses] vinery:vinery_plains added to minecraft:village/plains/houses at weight N (...)` is the evidence.
  patcherMod's PoolTuner runs after it (lowest priority), rebinds the processor list to the world's and records the
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
  as B5 D8 noted. Houses hold placed wines (2 to 3 each, 1.5.5 stock) and stocked containers (apples, cherries, grapes,
  apple sapling, apple juice; jungle grape seeds in the desert drawer); no loot tables, so Lootr isn't involved.
- **Config (`vinery-common.toml`):** `[village_houses] enabled` (true), `weight` (0 = pool average). A world needs a
  restart for either (pools are read at server start).
- **Tests:** VB-95 to VB-98.

# Part B: Let's Do Vinery (Backported Vinery)

Test prefix **VB-** · Written **2026-10-14** · Status **port `-02` DEPLOYED with pack 1.0.8 (2026-10-19) and live in
1.0.9; `-04` in the local `mods/` 2026-10-20 (B7.14, 10.9 to 10.19, 11, 13: village houses); none of it tested in game; D13 chosen 2026-10-18 (upstream
jars as resource donors, B4.8)**

## B0. At a glance

| Part | Status |
|---|---|
| Gap analysis 1.3.10 -> 1.5.5 (B2) | written 2026-10-14 from decompiles of both jars and of the bundled Foundation 1.0.1 |
| Migration issues (B3) | 75 issues listed, grouped by kind |
| License (B3.6) | code MIT, resources All Rights Reserved (Vinery 2026-10-14, Foundation 2026-10-18: same split). Satisfy refused redistribution of assets and features (B3.6) |
| Upstream jars as resource donors (B4.8) | CHOSEN 2026-10-18 (D13 B). Feasibility checked 2026-10-18: works; the port reads assets/data from the unmodified 1.5.5 and 1.3.10 jars at runtime. Delivery of the jars (D14) open |
| Decisions (B5) | DECIDED 2026-10-18: D3 keep `trippy` (section 10.1). DECIDED 2026-10-14: D1 keep `cherry_*`, D2 keep every 1.3.10 block and add the new ones, D6 WTHIT, D11 winemaker set damage by config. Aging revamped to a playtime clock (B4.5). 2026-10-18: D13 option B (B4.8). D14 decided 2026-10-18: payload now, download on publish. Worldgen (user, 2026-10-18): 1.20+ worldgen types become the nearest 1.19.2 vanilla type, nothing backported. Still open, built with the recommendation: D4, D5 (non-aging numbers), D7 to D10, D12 |
| Wine aging (B4.5) | DESIGNED 2026-10-14: vintage clock on real playtime, lengths unchanged for now (16 days per year, 2 years per level). 2026-10-18: your B7 list changes this to months on the sun clock (sleep ages wine), x2.5 in the dark: [time.md](time.md) 3.7 |
| Port (`development/backportedVinery`) | BUILT 2026-10-18. `-01` DEPLOYED with 1.0.7 (the first staging crashed the server on the grass slab, fixed under the same name, [fixes_compat.md](fixes_compat.md) 2.23); `-02` DEPLOYED with 1.0.8 (2026-10-19 10:20), loaded in single player 2026-10-19: Foundation subset, 1.5.5, 1.3.10 keepers, vintage clock, upstream-pack layer, WTHIT lines; status and gaps in B4.9. `-03` (B7.14 and 10.9 to 10.14) in the local `mods/` since 2026-10-19 22:27; `-03` / `-04` shipped in 1.1.01, `-05` in 1.1.04 (client), `-06` installed 2026-10-31 00:41, not yet booted |
| Pack-side changes (B4.4) | DONE 2026-10-18 (jar swap shipped in 1.0.7) except the I25 tool reruns; per-item outcome under B4.4 |
| Deploy (D14) | `tools/build_deploy.py`: `vinery_upstream/` category (both sides) + sha1 check against the port's pins; `backportedVinery` in IN_TREE_MODS; mod_sides row swapped, doapi row removed |
| Changes to make (B7) | BUILT 2026-10-18, not tested in game: everything but B7.4 (pinned), plus the port side of [time.md](time.md) 3.7; B7.11 settled by 10.8; B7.14 built 2026-10-19 into `-03`. Per-item outcome in B7 "Build status" |
| Third list and follow-ups (10) | Vinery side BUILT, not tested in game: 10.5 to 10.8 in `-02` (live); 10.9 duplicate-stack fix, 10.10 / 10.11 duplicates and racks removed, 10.12 sizes from the per-block limit, 10.13 storage climate, 10.14 slim bottles (recoloured in memory) in `-03`. 10.15 / 10.17 / 10.18 in the local `-03`: bottle sizes, box looks, Stained Glass and the wine box / flying fixes TESTED 2026-10-20 (10.18) |
| In-game tests | VB-01 to VB-98 drafted (B8; VB-95 to VB-98 = village houses, section 13); in-game testing is the user's. PASSED 2026-10-20: VB-73, VB-74, VB-81 to VB-83, VB-87 to VB-89, VB-91 (10.18) |

- **Source jars:**
  - Ours: `mods/letsdo-vinery-forge-1.3.10.jar` (Forge 1.19.2, needs `doapi-1.1.0b`). Behaviour reference:
    [vinery.md](vinery.md).
  - Target: `DecompiledMods/letsdo-vinery-fabric-1.5.5/` (Fabric, Minecraft 1.21.1, needs Architectury 13, Cloth Config
    15 and Foundation 1.0.1, which is bundled at `META-INF/jars/foundation-fabric-1.0.1.jar`).
- **License (B3.6):** split.
  - The Java code is **MIT**, so it can be ported.
  - Everything under `src/main/resources/` is **All Rights Reserved**. That covers textures, models, sounds, lang,
    recipes, tags, loot, worldgen and structures.
  - So a port can't ship upstream's assets or data without permission from Satisfy (D13). Satisfy was asked and
    refused.
  - Foundation 1.0.1 has the same split (its `License` file, read 2026-10-18).
  - **Route chosen (D13 B, B4.8):** the port ships only MIT code and our own files. It reads upstream's assets and
    data at runtime from the unmodified 1.5.5 and 1.3.10 jars, kept outside `mods/`.
- **Sizes:**
  - 1.3.10: 11.2k lines.
  - 1.5.5: `core/` 11.2k, `client/` 2.1k. The Fabric layer (0.5k) is a guide only.
  - The Foundation classes 1.5.5 imports directly come to 4.5k lines. With what they pull in (particles, seats, mixins)
    it is about 6k.
- **Biggest changes:**
  1. Cherry wood is renamed to `dark_cherry`, because 1.20 added a vanilla cherry. That touches 65 items from other
     mods in this pack.
  2. The Aging Barrel holds juice as a fluid level. Grape juice is poured in and stays there, and a wine takes
     6000 ticks. Today a wine takes about 50 ticks.
  3. The Apple Press works in two stages: apples to mash, then mash plus a bottle to apple juice.
  4. Wines age while they sit in storage blocks, and faster in the dark. The effect for each wine comes from a config
     list.
  5. 15 effects are replaced with 15 new ones, and every wine changes its recipe and its effect.
  6. 1.5.5 removes ~25 decoration and utility blocks: stove, kitchen sink, oven, calendar, basket, flower box, flower
     pot, loam, and others.
- **How to re-read the code:**
  - 1.5.5 real sources: Modrinth hosts `letsdo-vinery-fabric-1.5.5-sources.jar` next to the jar (B4.8). Port from
    that, not from the decompile.
  - Old jar: `python3 tools/decompile_mod.py mods/letsdo-vinery-forge-1.3.10.jar <out>`.
  - New jar: zip `DecompiledMods/letsdo-vinery-fabric-1.5.5/net` and run
    `java -jar ~/.cache/ce-tools/vineflower-1.10.1.jar <zip> <out>`. Run the same command on the bundled
    `foundation-fabric-1.0.1.jar`. Names come out as Fabric intermediary.

## B1. Overview

1.5.5 rebuilds Vinery on top of Foundation (the 1.21 successor of doapi). The way you grow grapes is about the same.
The machines, wine aging, effects, wood set and decoration all change, and a lot of new content arrives:

- **New content:**
  - Lattices in 10 woods.
  - Wine racks in 9 woods.
  - Grape bags (8 grape types plus apple and cherry).
  - A cabinet, a drawer and a dark-cherry chest barrel.
  - Window block, dirt-path slab, a Vinery banner that gives Regeneration II nearby, and a hanging sign.
  - One new wine (Lilitu Wine) and 10 new effects.
  - `/vinery` and `/wine` admin commands.
  - A built-in JEI plugin, an on-screen block info overlay, and creative side tabs.
  - The stackable log becomes a campfire-style cooker.
- **API:** other mods can add grape types and juices (`VineryApi`, `VineryPlugin`).

## B2. Reference: 1.3.10 vs 1.5.5

### B2.1 Registry

**Renamed** (1.3.10 id -> 1.5.5 id):

| Group | Renames |
|---|---|
| Cherry wood (wood set, 1.20 clash) | `cherry_{log,wood,planks,stairs,slab,fence,fence_gate,button,pressure_plate,door,trapdoor,sign,wall_sign,beam,floorboard,leaves,sapling,boat,chest_boat}`, `stripped_cherry_{log,wood}`, `potted_cherry_tree_sapling` -> `dark_cherry_*`. `cherry` (fruit) and `cherry_wine` keep their names |
| Grape juice | `red_grapejuice_wine_bottle` -> `red_grapejuice`; `white_grapejuice_wine_bottle` -> `white_grapejuice`; `{savanna,taiga,jungle}_{red,white}_grapejuice_bottle` -> `{red,white}_{savanna,taiga,jungle}_grapejuice` |
| Machines | `wine_press` -> `apple_press` (2 blocks tall now) |
| Armor | `vinemaker_apron` / `_leggings` / `_boots` -> `winemaker_*` (`straw_hat` unchanged) |
| Successor blocks (no 1:1 rename upstream; closest match) | `grapevine_lattice` -> `dark_cherry_lattice`; `apple_crate` / `cherry_crate` / `red_grape_crate` / `white_grape_crate` -> `*_bag`; `barrel` -> `dark_cherry_barrel`; `chair` / `table` / `big_table` / `shelf` -> `dark_cherry_*`; `wine_rack_1` (9 bottles) -> `dark_cherry_wine_rack_big`; `wine_rack_2` (4 bottles) -> `dark_cherry_wine_rack_small`; `wine_rack_3` / `wine_rack_5` (opening cabinets) -> `dark_cherry_cabinet` / `dark_cherry_drawer` |

**Removed, with nothing to replace them:**
- Blocks: `stove`, `wood_fired_oven`, `kitchen_sink`, `calendar`, `basket`, `flower_box`, `flower_pot`, `loam`,
  `loam_slab`, `loam_stairs`.
- Wines: `praetorian_wine`, `king_danis_wine`.
- Effects: `trippy`, `improved_{speed, jump_boost, strength, instant_health, regeneration, fire_resistance,
  water_breathing, night_vision, haste}`, `improved_absorbtion`.
- Entity: `vinery:chair`. Foundation's seat entity `foundation:chair` replaces it.
- Block entity types: `wine_press`, `wine_rack`, `wine_rack_storage`, `flower_box`, `flower_pot`.
- The `doapi:storage` block entity, used by racks, shelves, wine bottles and the wine box, becomes `vinery:storage`.

**Added:**

| Group | Ids |
|---|---|
| Lattices (new block entity `lattice`) | `{oak,spruce,birch,jungle,acacia,dark_oak,mangrove,cherry,bamboo,dark_cherry}_lattice`. `cherry` and `bamboo` use vanilla 1.20 planks |
| Wine racks (`_small` 4 bottles, `_big` 9, `_mid` big bottle) | 9 woods, the same list as the lattices minus `dark_cherry`, which already has its own set. `cherry` and `bamboo` use vanilla 1.20 planks |
| Bags | `{white,red}_grape_bag`, `{white,red}_{savanna,taiga,jungle}_grape_bag`, `apple_bag`, `cherry_bag` |
| Furniture / building | `dark_cherry_{cabinet,drawer,barrel,hanging_sign,wall_hanging_sign}`, `window_block`, `dirt_path_slab` |
| Banner | `vinery_standard` (+ wall): Regeneration II (amplifier 1) to players within 8 blocks, refreshed every second. Reward for the `wine_somelier` advancement (2 banners); a smithing recipe copies it (netherite template + netherite ingot) |
| Wine | `lilitu_wine` |
| Effects | `armor_effect`, `health_effect`, `luck_effect`, `resistance_effect`, `double_jump`, `party_effect`, `climbing_effect`, `frosty_armor`, `lava_walker`, `water_walker` (B2.5) |
| Block entities | `grapevine_pot`, `lattice`, `storage_pot`, `stackable_log`, `apple_press`, `cabinet`, `vinery_standard`, `dark_cherry_barrel`, `mod_sign`, `mod_hanging_sign` |
| Recipe types | `vinery:apple_mashing`, `vinery:apple_fermenting` (new); `vinery:wine_fermentation` (new format) |

### B2.2 Aging Barrel (`fermentation_barrel`)

| | 1.3.10 | 1.5.5 |
|---|---|---|
| Slots | 0 wine bottle, 1-4 ingredients (juice is an ingredient item), 5 output | 0 juice input, 1-3 ingredients, 4 wine bottle, 5 output |
| Juice | an ingredient that gets used up | Pouring juice into slot 0 turns each bottle into **25 juice units** (setting `maxFluidIncrease`). The barrel holds up to **100** (`maxFluidLevel`) of **one juice type** (`red_general`, `white_general`, `{red,white}_{savanna,taiga,jungle}`, `apple`). The empty wine bottles go to slot 4 (or drop on top). Sneak-right-click with an empty hand empties the juice. A dripping tap shows the juice colour |
| Recipe | ingredients only | `juice {type, amount}` + 1-3 ingredients + `wine_bottle.required`. The amount is in units, so `red_wine` needs 10 units: 2.5 wines per juice bottle |
| Time | 1800 is in the code but never used; the real time is about 50 ticks and becomes 0 after a reload (vinery.md 2.3) | **6000 ticks** (setting `totalFermentationTime`) |
| Output | stalls until slot 5 is empty (the pack's `FermentationBarrelStackOutputMixin` fixes that) | stacks on its own (`fits()`); the pack mixin is no longer needed |
| Progress | n/a | resets to 0 when the recipe stops matching. The recipe is checked again every 20 ticks |
| Hoppers | every slot, every side (jams, vinery.md 2.2) | top: juice and bottles; sides: ingredients and bottles, and output can be pulled; bottom: output only |

**Wines, old -> new.** Old inputs are 1.3.10 ingredients. New is juice type x units | ingredients | bottle used.
Effects are the 1.5.5 defaults (the `effects` setting, `wine|effect|amplifier|ticks`). Every new wine lasts 1800 ticks,
except chorus (10), creepers crush (100) and mellohi (1).

| Wine | Old inputs | Old effect | New recipe | New effect |
|---|---|---|---|---|
| aegis | savanna white juice, sugar, kelp | Night Vision | white_taiga x7 \| sugar, kelp, iron ingot \| bottle | armor_effect (+2 armor, +3 toughness) |
| apple_cider | apple juice, sugar | Improved Regeneration (15 t) | apple x20 \| sugar \| bottle | Strength |
| apple_wine | apple juice, honey bottle | Improved Instant Health (10 t) | apple x15 \| apple juice \| no bottle | Resistance |
| bolvar | taiga red juice, honey, cherry | Improved Haste | red_taiga x6 \| honey, cherry \| no bottle | Lava Walker |
| bottle_mojang_noir | white juice, beetroot | Strength | red_general x20 \| honey, cherry, red wine \| no bottle | Experience |
| chenet | savanna red juice, sugar, honey | Improved Speed (water walking) | red_jungle x12 \| spider eye, honey \| no bottle | Climbing |
| cherry_wine | red juice, cherry | Regeneration | red_general x15 \| cherry \| bottle | Invisibility |
| chorus | white juice, chorus fruit | Teleport | red_taiga x8 \| chorus fruit \| bottle | Teleport (10 t: up to 10 hops) |
| clark | savanna white juice, sugar | Improved Jump Boost | white_jungle x10 \| sugar \| bottle | Double Jump |
| creepers_crush | any juice, gunpowder | Creeper (1 t) | white_savanna x10 \| gunpowder \| bottle | Creeper (100 t) |
| cristel | any juice, sugar, feather, blaze rod | Experience | red_general x25 \| sugar, feather, blaze rod \| bottle | Water Breathing |
| eiswein | any juice, snowball | Instant Health (5 t) | white_taiga x10 \| snowball \| bottle | Frosty Armor |
| glowing | white juice, glow berries | Glowing | white_general x10 \| glow berries \| bottle | Glowing |
| jellie | apple, chorus, chenet, bolvar wines | Jellie | white_general x25 \| apple, chenet, bolvar wines \| bottle | Jellie |
| jo_special_mixture | red + white juice, redstone | Trippy | red_savanna x10 \| fermented spider eye \| bottle | Climbing |
| kelp_cider | savanna red juice, kelp | Improved Water Breathing | white_savanna x14 \| kelp \| bottle | Water Walker |
| lilitu (new) | - | - | red_savanna x14 \| honey, cherry \| no bottle | Party |
| magnetic | white juice, iron ingot | Magnet | red_jungle x10 \| iron ingot \| no bottle | Magnet |
| mead | honey bottle, sugar | Saturation | apple x1 \| honey bottle, sugar \| bottle | Haste |
| mellohi | jungle white juice, sugar | Improved Fire Resistance | white_general x5 \| sugar, glowstone \| bottle | Instant Health (1 t) |
| noir | red juice | Instant Health (5 t) | red_general x12 \| sweet berries \| bottle | Jump Boost |
| red_wine | red juice, sugar | Improved Fire Resistance | red_general x10 \| sugar \| bottle | Slow Falling |
| solaris | taiga white juice, sweet berries, honey | Improved Strength | white_general x15 \| honey, sweet berries \| no bottle | Health Boost |
| stal | jungle red juice, cocoa, sugar | Improved Regeneration | red_jungle x8 \| cocoa, sugar \| bottle | health_effect (+4 max health) |
| strad | jungle white juice, cocoa, sugar | Improved Absorption | red_general x8 \| cocoa, sugar \| bottle | Night Vision |
| villagers_fright | any juice, 2 emeralds | Bad Omen (1200 t) | white_jungle x17 \| arrow \| bottle | Bad Omen |
| praetorian (removed) | white juice, glow berries, sugar | Glowing | - | - |
| king_danis (removed) | jungle red juice, honey, cherry | Improved Instant Health (45 t) | - | - |

`apple_juice` is no longer made in the barrel. The Apple Press makes it now (B2.3).

### B2.3 Apple Press (`wine_press` -> `apple_press`)

| | 1.3.10 | 1.5.5 |
|---|---|---|
| Shape | 1 block | 2 blocks tall (upper and lower half) |
| Stage 1 (mashing) | 1 apple -> 1 apple mash, hardcoded, 72 ticks | `apple_mashing` recipes: apple -> mash; honeycomb block -> 4 honeycomb. **600 ticks** (`applePressMashingTime`) |
| Stage 2 (fermenting) | none (the barrel made apple juice) | `apple_fermenting` recipes: mash + wine bottle -> apple juice; honeycomb + wine bottle -> honey bottle. **800 ticks** (`applePressFermentationTime`) |
| Slots | 0 in, 1 out | 0 apple, 1 mash (stage 1 output and stage 2 input), 2 bottle, 3 output |
| Hoppers | any item into either slot (jams) | sides insert by recipe; output from the sides or bottom |

### B2.4 Grapes, pot, stems, lattices, leaves, logs

| Thing | 1.3.10 | 1.5.5 |
|---|---|---|
| Grapevine Pot | no block entity; 6 grapes, 3 landings on it, 2 juice bottles | block entity; still 6 grapes (one type only now, where the first grape used to set the type) and 3 per bottle. It takes **12 stomps** (`grapevinePotStomps`), and an entity with 15 armor or more counts 10% more (`grapevinePotHeavyArmorBonus`). Coloured splash particles. Breaking an unstomped pot drops its grapes. **Pack (B7.14):** 12 grapes, 4 stomps, 4 bottles |
| Grapevine stem | ages 0-4; shears prune it | same, plus **grapevine leaves**: a red or white stem on a non-stem block grows up to 11 leaf blocks around it 4800-9600 ticks after planting (`grapevineLeavesEnabled`) |
| Lattice | one `grapevine_lattice` | 10 woods; block entity; connects left/right, floor or wall placement, an axe toggles the support post, shears prune, hanging-grape model on 15%; leaf particles |
| Wild bushes | grow by vanilla random tick | growth chance 0.5 x `grapeGrowthMultiplier`; min light 10 (savanna 14, taiga 5 and needs podzol / coarse dirt / grass within 4); foxes eat ripe grapes (mixin) |
| Apple / cherry leaves | `can_have_*` / `has_*` properties; rotten cherry chance | Foundation `FruitLeavesBlock`: `can_grow_*` / `has_*` / `age` 0-3, 30% of placed leaves can fruit, growth chance 0.4 each; picking drops 1-4 fruit. **No rotten cherry source anywhere in 1.5.5** (no loot table, recipe or code) |
| Grape juice | drink | drinking it also **cures Poison** and gives the wine bottle back |
| Stackable log | stack of logs that can be lit | a campfire: 4 food slots, campfire recipes, burns 12000 ticks, refuel with burnable logs, put out with a shovel or water bottle, lit by flint and steel / fire charge / burning projectiles, hurts entities on it, light 13 |
| Grass / dirt slabs | `grass_slab`, `dirt_slab` | grass slab spreads to dirt (mixin on vanilla grass spreading), plants can't sit on a bottom grass slab, shovels can't path a slab, `dirt_path_slab` added |

### B2.5 Wine aging and effects

| | 1.3.10 (pack config) | 1.5.5 |
|---|---|---|
| Clock | `Year` NBT = `dayTime / 24000 / year_length_in_days` (sleeping and `/time set` age wine) | `WINE_YEAR` component: `brewedDay` from **game time** (sleeping no longer ages wine) |
| Year length | 16 days (pack) | `daysPerYear` 24 |
| Years per level | 2 (pack) | `yearsPerEffectLevel` 6; `maxLevel` 5 |
| Duration | fixed per wine | 1800 + 200 per year, max 15000 (`durationPerYear`, `maxDuration`) |
| Storage | ages anywhere | ages anywhere, or only in storage (`ageOnlyInStorage`). In any storage block with **no sky light** it ages x1.5 (`cellarAgingMultiplier`) |
| Tooltip | year | effect + level + duration, age in years, days to the next level, "fully aged" |
| Placing | sneak to place | same; the placed bottle keeps the stack in a `vinery:storage` block entity |
| Old stacks | - | the 1.5.5 codec reads a legacy `year` field as `brewedDay = year x daysPerYear` |

**New effects (1.5.5):**

| Effect | What it does |
|---|---|
| armor_effect / health_effect / luck_effect | +2 armor and +3 toughness / +4 max health / +2 luck (attribute modifiers). luck_effect has no wine |
| resistance_effect | +0.2 knockback resistance, +2 toughness. No wine uses it |
| double_jump | **client-side** second jump in the air (not with elytra, riding, swimming, levitation); jump power 0.1 per level; fall damage -1 |
| party_effect | every hit spawns a firework with a random colour at the target; hits on non-mob, non-player entities are cancelled |
| climbing_effect | walk up walls (horizontal collision -> 0.2 up, no fall damage) |
| frosty_armor | freezes you (powder-snow freeze ticks) for +4 armor and +2 attack damage, -5% speed |
| lava_walker / water_walker | stand and walk on lava / water, float up when inside |
| jellie, magnet, teleport, creeper, experience | as before, rewritten. Teleport hops randomly within 8 blocks onto a safe floor. Experience: orbs give `amount x (1 + (1 + level) x 0.5)` |

### B2.6 Entities, trades, worldgen, commands, client

| | 1.3.10 | 1.5.5 |
|---|---|---|
| Wandering Winemaker + 2 mules | replaces `wine_trader_chance`% of trader spawns | `spawnChance` 0.5, `spawnWithMules`, `spawnDelay` 48000; same mixin approach |
| Winemaker villager | POI on the barrel, hardcoded trades | trades are a config list per level (BUY/SELL, price, count, max uses, xp) |
| Worldgen | biome modifiers per grape type and tree (`forge/biome_modifier/add_*`) | same placed-feature ids (`tree_apple`, `tree_cherry`, `*_grape_bush_chance`), biome tags `spawns_*`; new apple / dark cherry tree variants |
| Village houses | none | `data/vinery/structure/vinery_{desert,plains,savanna,taiga}.nbt` ship, but **nothing adds them to a pool**, and their palettes still name 1.3-era ids (`vinery:basket`, `chair`, `table`, `loam`, `cherry_*`). Dead data. Per-house breakdown (checked 2026-10-20 from the NBTs): B5 D8 |
| Commands | none | `/vinery dump grapes|juices|effects`, `grow`, `barrel finish`, `press finish`, `pot fill`, `winemaker spawn|locate`, `effect`, `reload`; `/wine info|time|storage|level|mature|fresh|years|days` (op 2 for edits) |
| JEI | none (REI only); the pack has its own plugin in patcherMod ([vinery_jei.md](vinery_jei.md)) | built-in: barrel, press mashing, press fermenting, barrel recipe transfer |
| HUD | none | Foundation `BlockInfoOverlay`: pot contents/stomps, storage slot contents (`infoOverlay.mode`) |
| Creative | one tab | one tab with 4 side tabs (essentials, wines, dark cherry, decoration) |
| Advancements | 60 files | 20 in `main/` + recipe unlocks; `wine_somelier` gives 800 xp + 2 banners |

### B2.7 Settings, 1.3.10 -> 1.5.5

1.3.10 keeps 7 settings in `config/vinery/config.json5`, written by doapi's Jankson config. 1.5.5 has about 35 settings,
using Cloth AutoConfig on Fabric. The 1.5.5 groups are:

- `blocks`: times, fluid, growth, the pot and the leaves.
- `items.wine`: aging and effects.
- `items.banner`.
- `villager.level1-5`: trades.
- `trader`.
- `infoOverlay`.

Mapping the old keys:

| Old key | New key | Notes |
|---|---|---|
| `year_length_in_days` | `daysPerYear` | pack sets 16 |
| `years_per_effect_level` | `yearsPerEffectLevel` | pack sets 2 |
| `wine_trader_chance` | `trader.spawnChance` | pack sets 0; the port's own spawner (`[trader] ownSpawner`, 96000 ticks, moved from patcherMod 2026-10-27) spawns the Winemaker |
| `enable_wine_maker_set_bonus` | none | the set bonus is always on |
| `probability_to_keep_bone_meal` | none | always 100% |
| `probability_for_damage` | none | always 100% |
| `damage_per_use` | none | always 2 to **every** armor piece |

## B3. Migration issues

Each row is something the port has to handle. The ID is used in B4 and B5.

### B3.1 Platform: Fabric 1.21.1 -> Forge 1.19.2

| # | Issue | Port approach |
|---|---|---|
| P1 | Foundation 1.0.1 is a Fabric/NeoForge 1.21 library; 1.19.2 only has doapi 1.1.0b, a different API | Port the ~6k lines of Foundation that Vinery uses into the jar, keeping the package `net.satisfy.foundation` (B5 D7). doapi can leave the pack afterwards (only Vinery needs it) |
| P2 | Data components (`WINE_YEAR`, `CUSTOM_FOOD`, `potion_contents`, `fireworks`) | ItemStack NBT. `WINE_YEAR` -> a `WineYear` compound with the same 11 field names |
| P3 | `ItemInteractionResult` / `useItemOn` split (1.20.5) | fold back into `Block#use` |
| P4 | `MapCodec` block codecs, `RecordCodecBuilder` recipe serializers, `StreamCodec` | drop the codecs; JSON `RecipeSerializer` + `FriendlyByteBuf` |
| P5 | `RecipeHolder`, `RecipeInput` classes (`FermentationBarrelRecipeInput` etc.) | `Recipe<Container>` with a small wrapper container |
| P6 | `MobEffect#applyEffectTick` returns boolean; `onEffectStarted` / `onMobRemoved`; attribute modifiers keyed by `ResourceLocation` | void tick; `addAttributeModifiers` / `removeAttributeModifiers` overrides (Jellie absorption); UUID-keyed modifiers |
| P7 | `ColorParticleOption` (Foundation `COLORED_DRIP`, `DYE_SPLASH`, `LEAF`) | a custom `ParticleType` with its own options class and codec-less deserializer |
| P8 | `TreeGrower` record (1.20.5) for saplings | `AbstractTreeGrower` subclasses (apple has a variant feature) |
| P9 | `BlockSetType` / `WoodType` registration for doors, trapdoors, buttons, plates, fence gates, signs | 1.19.2 constructors (Properties only), `WoodType.create` + `Sheets` material for the sign |
| P10 | Hanging signs (1.20) | 1.19.2 has none. Drop `dark_cherry_hanging_sign` (B5 D9). The pack already has Supplementaries' `vinery/hanging_sign_cherry` |
| P11 | Vanilla 1.20 blocks used as materials: `cherry_planks` (cherry lattice and racks), `bamboo_planks` (bamboo lattice and racks), `netherite_upgrade_smithing_template` (banner copy) | cherry: see D1. bamboo: use `quark:bamboo_planks` (in the pack) or drop. Banner copy: 1.19.2 legacy smithing (banner + netherite ingot) |
| P12 | Boats via `WoodBoat` / `PlatformHelper.registerBoatType` | 1.19.2 `Boat` subclass with its own type enum (1.3.10 used doapi Terraform boats: keep the `cherry_boat` entity data compatible) |
| P13 | Fabric entrypoints for plugins (`VineryPlugin` via `getPlugins()`), JEI and REI | Forge: `ServiceLoader` or `ModFileScanData` for `@VineryPlugin.Entry`; `@JeiPlugin` |
| P14 | Cloth AutoConfig on Fabric | ForgeConfigSpec (`vinery-common.toml`, `vinery-client.toml` for the overlay), same key names, the pack's values seeded (B4.4) |
| P15 | Fabric `BiomeModifications` | Forge `biome_modifier` JSONs with the **1.3.10 ids** (`add_apple_tree`, `add_cherry_tree`, `add_*_grape`) so `wild_crops.json` `disable_modifiers` keeps matching |
| P16 | Fabric villager / POI helpers, `TradeOfferHelper` | Forge `VillagerProfession` / `PoiType` registry + `VillagerTradesEvent` reading the config list |
| P17 | Mixins written against 1.21 code: `LivingEntity#eat(Level, ItemStack, FoodProperties)` (1.21 signature), `SpreadingSnowyDirtBlock#randomTick` with `CAPTURE_FAILHARD` locals, `WanderingTraderSpawner#spawn` target, `Leashable#setLeashedTo` | rewrite each against 1.19.2: `eat(Level, ItemStack)` + `addEatEffect`; new local capture or `@ModifyVariable`; leash via `Mob#setLeashedTo` |
| P18 | `ItemTags.SHOVELS` and the 1.20.5 `fox_food` tag | `forge:tools/shovels`; foxes keep the mixin path |
| P19 | `DamageSources` holder API (creeper effect), `Explosion.BlockInteraction` (`level.explode(... TNT)`) | `DamageSource.GENERIC`, `Explosion.BlockInteraction.BREAK` |
| P20 | Tooltip context / `TooltipFlag` signature, `Item#getUseDuration(stack, entity)` | 1.19.2 signatures; the wine tooltip reads the client level as today |
| P21 | Armor: `ArmorMaterial` holder + Foundation `TexturedArmorItem` / `ArmorModels` (custom straw-hat / apron models) | `ArmorItem` + `IClientItemExtensions#getHumanoidArmorModel`; the 1.3.10 jar already does this for the hat |
| P22 | Creative side tabs (Foundation client UI) | 1.19.2 `CreativeModeTab` with one tab; side tabs are optional, port later or never |
| P23 | Double jump runs only on the client (sets motion locally) | works on 1.19.2 as written. The server sees normal movement; check the "flying is not enabled" kick never fires on a dedicated server (VB-22) |

### B3.2 Data format

| # | Issue | Port approach |
|---|---|---|
| F1 | 1.21 folder names: `recipe/`, `loot_table/`, `advancement/`, `tags/item`, `tags/block` | rename to `recipes/`, `loot_tables/`, `advancements/`, `tags/items`, `tags/blocks` |
| F2 | Result stacks `{"id": ..., "count": ...}` | `{"item": ..., "count": ...}` |
| F3 | `c:` tags (`c:grapes`, `c:berries`, `c:seeds`, `c:milk`, stripped logs/wood) | `forge:` equivalents (1.3.10 already shipped both `c:` and `forge:`; keep both) |
| F4 | Advancement JSON: `"items": ["vinery:x"]` predicates, `inventory_changed`, loot reward table | 1.19.2 predicate shape `{"items": ["vinery:x"]}` inside `conditions.items`; reward loot table moves to `loot_tables/advancements/` |
| F5 | Smithing transform recipe with template (banner) | legacy `minecraft:smithing` (base + addition) |
| F6 | Block loot tables use `copy_components` for the storage pot / barrel | `copy_nbt` from `BlockEntityTag` |
| F7 | Worldgen JSON: 1.21 `tree` / `random_patch` configs | mostly the same in 1.19.2. The pack overrides 6 grape-bush configured features in `kubejs/data/vinery/worldgen/configured_feature/`; keep those ids and formats |
| F8 | Village structure NBTs (B2.6) | not ported (dead upstream, stale palettes). If wanted later: remap palettes with the B4.1 table and add them through the pack's pool tooling |

### B3.3 Old worlds and old items

**2026-10-27: the conversions W2, W3, W8, W9 and W10 are REMOVED from the port** (CLAUDE.md "no old-version or
cross-mod conversions"; [pack_hygiene.md](pack_hygiene.md) finding 10): `BlockEntityLegacyIdMixin` (`doapi:storage`),
`FermentationBarrelBlockEntity.convertLegacyLayout`, `WineYears.convertLegacy` / `hasLegacyYear` with the
`legacyYearLengthDays` key, and the `improved_*` effect / `chair` entity entries in `VineryCommonEvents`. A world last
saved with 1.3.10 (before 1.0.7) loses 1.3.10 storage contents, old barrel layouts and old wines' ages. The rows below
are the original plan, kept for the record. W1 (ids kept, D1 / D2) and W4 (the press keeps its block entity id) need
no code.

| # | Issue | Port approach |
|---|---|---|
| W1 | ~60 ids renamed or gone (B2.1); Forge drops unknown blocks/items with a missing-mappings warning | **Mostly gone after D1 / D2 (B4.1):** every 1.3.10 id stays registered. The port has no `MissingMappingsEvent` handler (W9 removed 2026-10-27) |
| W2 | **REMOVED 2026-10-27.** `doapi:storage` block entities in placed racks, shelves, wine bottles, the wine box, storage pots (1.3.10) | rewrite the `id` on load (hook `BlockEntity.loadStatic`) to `vinery:storage`, `vinery:storage_pot` or the cabinet type per block. Block-entity types are not covered by missing mappings. The slot layouts differ per storage type (old `four_bottle` / `nine_bottle` / `wine_box` vs new layouts): map slot by slot, drop the overflow |
| W3 | **REMOVED 2026-10-27.** Aging Barrel contents (old layout 0 bottle, 1-4 ingredients, 5 output) | on load: old slot 0 -> 4 (bottles); old juice items in 1-4 -> poured into the fluid level when the type matches (25 each, up to 100), the rest drop; other ingredients keep 1-3, a fourth drops |
| W4 | Apple Press contents (old 0 in, 1 out) | old 0 -> 0, old 1 (mash) -> 1. The block is 2 tall now: if the space above is blocked, keep a 1-tall legacy state or drop the press as an item |
| W5 | Grapevine pots mid-stomp (no block entity, stage 3-6) | block entity created on first touch; stage > 3 gives progress `(stage-3) x points/3` (1.5.5 already does this) |
| W6 | Apple / cherry leaves: `can_have_apples` / `can_have_cherries` -> `can_grow_*`; new `age` | blockstate fixer: unknown properties fall back to the default (no fruit). Better: a `ChunkDataEvent.Load` palette pass that maps `can_have_*` -> `can_grow_*`, and `has_*=true` -> `age=3` |
| W7 | `grapevine_lattice` with jungle grapes on it (age/grape in state, no block entity) | remap to the lattice from D1, facing north, keep `age` and `grape`, block entity created on load |
| W8 | **REMOVED 2026-10-27.** Wines with `Year` NBT on the day-time clock | convert once, when the stack is first seen, to the vintage clock (B4.5): `ageTicks = (oldYearNow - Year) x 16 x ticksPerDay`, then `brewedAt = clock - ageTicks`. Do not use the upstream `year x daysPerYear` fallback, which would make some wines "brewed in the future" after lots of sleeping |
| W9 | **REMOVED 2026-10-27.** Active instances of removed effects (`trippy`, `improved_*`) | remap each improved effect to its vanilla effect (`improved_speed` -> `speed` and so on), and `improved_absorbtion` -> `absorption` (D3) |
| W10 | **REMOVED 2026-10-27.** `vinery:chair` seat entities | drop on load, as the player just stands up; or remap to `foundation:chair` |
| W11 | Every Compat, Dramatic Doors, Lightman's Currency, Supplementaries and Supplementaries Squared generate 65 items from Vinery's `cherry` wood type (`everycomp:*/vinery/cherry_*`, `dramaticdoors:*_vinery_cherry_door`, `lightmanscurrency:auction_stand_cherry`, `supplementaries:vinery/*_cherry`) | renaming the wood to `dark_cherry` renames all 65, and their placed blocks are lost. **Main reason for D1** |

### B3.4 Pack integration

| # | Pack file / feature | Impact |
|---|---|---|
| I1 | License | code MIT, resources All Rights Reserved (B3.6). Settle D13 before building anything that ships upstream files |
| I2 | patcherMod `FermentationBarrelStackOutputMixin` | targets the 1.3.10 class and slots; 1.5.5 stacks by itself. **Remove** |
| I3 | patcherMod `compat/vinery` JEI plugin (VineryJEI, 4 pages, [vinery_jei.md](vinery_jei.md); deleted 2026-10-27, pack_hygiene.md finding 6) | the port ships JEI for the barrel and both press stages. Keep only Grape Stomping (12 stomps now) and Wine Aging (rewrite for storage aging and the cellar multiplier); retire Fermentation and Wine Press; update `config/jei/recipe-category-sort-order.ini` (+ `configs_edited/`) |
| I4 | patcherMod `WinemakerTrades` | reflects on `satisfyu.vinery.entity.WanderingWinemakerEntity`'s static trade map. The class moves to `net.satisfy.vinery.core.entity` and its trades become config-driven: rewrite against the port's API (a direct dependency is fine now, since we own the jar) |
| I5 | patcherMod `WinemakerSpawner` (moved into the port 2026-10-27: `forge/WinemakerSpawner`, `[trader] ownSpawner`) | reads `config/vinery/config.json5 wine_trader_chance`: switch to the port's `trader.spawnChance` (pack: 0) |
| I6 | patcherMod `StuckInBlock` | rule keyed by class name `satisfyu.vinery.block.grape.TaigaGrapeBush`: becomes `net.satisfy.vinery.core.block.GrapeBushBlock$TaigaGrapeBush` |
| I7 | patcherMod `BonemealCharmMixin` | modelled on Vinery's bone-meal mixin. 1.5.5's version changes the return to PASS, so the charm (which checks for CONSUME) and the winemaker set no longer double-refund. The order still matters if both mixins hit the same call: check (VB-27) |
| I8 | patcherMod `crop_growth.json` (WTHIT growth line) | `vinery:grapevine_lattice` -> the 10 lattice ids (`vinery:*_lattice`); lattice age now lives in the block entity too, but the state `age` still drives it |
| I9 | Foundation `BlockInfoOverlay` HUD | not ported; the lines go into WTHIT (D6, decided) |
| I10 | KubeJS `item_dict.js` (144 Vinery entries) | rename keys/values per B4.1; add the new ids (lattices, racks, bags, cabinet, drawer, window block, path slab, banner, lilitu wine, new juices). Supplementaries / Every Compat entries follow D1 |
| I11 | `server_scripts/tags/wood_tags.js` | `vinery:barrel` / `vinery:wooden_barrels` -> the D2 barrel id |
| I12 | `server_scripts/tags/blocktags.js` (BlockRunner) | regexes name `vinery:cherry_*`, `*crate`, `loam*`, `dirt_slab`, `grass_slab`: add lattices, racks, bags, `dirt_path_slab`, `window_block`, `stackable_log` (now a campfire) |
| I13 | `server_scripts/engineering/fabric.js` + `other/misc_mods.js` | `replaceInput({ id: 'vinery:vinemaker_leggings' })` -> `winemaker_leggings` |
| I14 | `server_scripts/loot/07_rules.js` | `L.pack(i.vinery_apple_crate, ...)` -> `apple_bag` |
| I15 | `startup_scripts/compost/compost_values.js` | `vinery_apple_crate` 1.0 -> bags; 1.5.5's own compost list adds `apple_mash`, `straw_hat`, all seeds and grapes at 0.4: check it doesn't fight `global.compost` |
| I16 | `startup_scripts/func/tool_effects.js` | echo "discombobulate" uses `vinery:trippy`, which 1.5.5 removes. D3 |
| I17 | `kubejs/assets/creativeengineering/lang/en_us.json` | 17 JEED descriptions for old effects (all TODO); rewrite for the B2.5 set |
| I18 | `generation_scripts/worldgen/wild_crops.json` | names `vinery:tree_apple`, `vinery:tree_cherry`, `add_apple_tree`, `add_cherry_tree`: same ids kept (P15), so no change |
| I19 | `kubejs/data/vinery/worldgen/configured_feature/*grape_bush.json` (6 overrides) | same ids in 1.5.5. Re-diff against the new defaults |
| I20 | `config/ftbquests/quests/chapters/progression.snbt` | `vinery:flower_pot` (icon + task) and `vinery:flower_box` task: removed upstream. D2 |
| I21 | Alcohol Part A 2.7 heat audit | `vinery:stove` and `vinery:wood_fired_oven` are FD heat sources today; 1.5.5 removes both and has no `farmersdelight` tag at all. D2. The new fired `stackable_log` is a heat-source candidate |
| I22 | `config/everycomp-wood_types.toml` | `[types.wood_type.vinery]` / `leaves_type` keys follow D1 |
| I23 | Tooltips (`client_scripts/tooltips.js` Vinery section, [tooltips.md](tooltips.md)) | the grape / seed flavour keys may change; rewrite the placeholders for the new blocks |
| I24 | Placeable food ([placeable_food.md](placeable_food.md)) | wine bottles still need sneaking: no change; new juice ids |
| I25 | `tools/` (`crop_growth_coverage.py`, `placeable_catalog.py`, `stack_sizes.py`, `gen_spawn_table.py`, `food_catalog.py`) and `/dumpall` | rerun after the swap |
| I26 | Deploy | replace the jar in `mods/` and `server_mods/`, drop `doapi-1.1.0b.jar` (both sides; nothing else needs it: check with a scan first), update `mod_sides.md`, regenerate `pack/` |

### B3.5 Upstream quirks found while reading 1.5.5

| # | Quirk | Port |
|---|---|---|
| Q1 | No rotten cherry source (compost list and creative tab only) | keep 1.3.10's leaf drop (D10) |
| Q2 | `mead` needs **1** apple unit, so one apple juice bottle (25 units) makes 25 meads | keep, or set it to 25 in the port's recipe file (D5) |
| Q3 | Teleport (10 t) and Creeper (100 t) are instant effects, which run every tick while they last: up to 10 teleports, and 100 explosions plus 50 damage each (the first one kills you anyway) | 1.3.10 had the same kind of thing (vinery.md 2.11). Keep, or set the durations to 1 in the default list |
| Q4 | `chenet_wine` and `jo_special_mixture` both give Climbing; `luck_effect` and `resistance_effect` have no wine | keep (config can change it) |
| Q5 | Village structure files are unused and stale (B2.6) | skip (F8) |
| Q6 | The winemaker set bonus has no settings and damages all 4 pieces by 2 on every use | D11 |
| Q7 | The barrel juice type is stored as a string; the GUI syncs it as an index into the registration order, so plugin juices must register in the same order on client and server | fine for a single jar; note for the API |

### B3.6 License

The upstream license (text from the Vinery repository, received 2026-10-14, in full below) splits the repository in
two.

**What is All Rights Reserved:** every file under these three folders, and their subfolders:
- `/common/src/main/resources/`
- `/fabric/src/main/resources/`
- `/forge/src/main/resources/`

**What is MIT:** every other file in the repository. In practice that means the Java sources.

Both parts are Copyright (c) 2022 Satisfy.

**What each part covers in the jars.** The resources folders end up as these files in a jar:

| Ends up in the jar as | License | Port can |
|---|---|---|
| Java classes (`net/satisfy/vinery/**`) | MIT | port, change and ship, keeping the copyright and permission notice (a `NOTICE-upstream-MIT.txt`, as planned for Backported Brewin). Our own code stays GPLv3 (license memory); MIT code can be included in a GPLv3 work |
| `assets/vinery/**`: textures, models, blockstates, sounds, `sounds.json`, lang | All Rights Reserved | not copy, change or ship without permission |
| `data/**`: recipes, tags, loot tables, advancements, worldgen, structures, biome modifiers | All Rights Reserved | same |
| `fabric.mod.json` / `mods.toml`, mixin configs, refmaps, `pack.png`, `vinery.accesswidener`, the `bushy_leaves` resource pack | All Rights Reserved (they live in the resources folders) | write our own; they are short |

**What it means for this plan:**
- The **code side** (B3.1, B4.2 steps 1 to 12, B4.3) is fine as written. That includes the Foundation subset:
  Foundation's `License` (github.com/Let-s-Do-Collection/Foundation, `master`, read 2026-10-18) has the same split as
  Vinery's: `*/src/main/resources/` All Rights Reserved, everything else MIT, Copyright (c) 2024 satisfy. Its
  `fabric.mod.json` "Custom" means this split.
- The **resources side** is blocked as written:
  - B4.2 assumes upstream assets get copied, and B3.2 (F1 to F7) converts upstream data files. Both ship All Rights
    Reserved files.
  - That applies to 1.3.10's resources as well (`/forge/src/main/resources/` is listed).
  - Today the pack ships the official 1.3.10 jar unchanged, which is normal modpack use.
  - Repackaging 1.3.10 textures inside our jar is not normal modpack use.
- The pack's own existing overrides (`kubejs/data/vinery/worldgen/configured_feature/*.json`, 6 files) are edited
  copies of upstream data. Shipping them is the same kind of use the pack already makes of many mods' data. They're
  noted here only so the question isn't forgotten.
- doapi 1.1.0b declares `license = "ARR"`. The port no longer needs it, so nothing of doapi gets copied.

**Satisfy's answer (asked by the user, noted 2026-10-18):** they refused redistribution of "assets and features".
- **Assets:** a refusal of option A. B4.8 never copies, changes on disk or repackages an upstream file. Each machine
  reads the unmodified jars at runtime, the same way any resource pack overrides textures. How those jars reach each
  machine is D14.
- **Features:** the MIT grant covers the code as published. A grant on a published version can't be taken back, and
  game mechanics as such aren't something copyright covers. So the port of the code stands on the license text.
  It still goes against the author's stated wish. That matters little for a private pack copied by hand and more if the
  port is ever published (Modrinth, CurseForge, a public jar): then any All Rights Reserved file that slips in is an
  easy takedown. B6 adds a check that keeps upstream files out of the port.

This is a reading of the license text, not legal advice. D13 lists the ways forward; B was chosen (B4.8).

<details>
<summary>Full license text (as received)</summary>

```
The following directories, their subdirectories, and any files inside said directories or subdirectories are subject to
the license below.

/common/src/main/resources/
/fabric/src/main/resources/
/forge/src/main/resources/

All Rights Reserved
Copyright (c) 2022 Satisfy

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

All other files within this repository are subject to the license below.

MIT License

Copyright (c) 2022 Satisfy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

</details>

## B4. Port plan

### B4.1 Id map (D1 and D2 as decided)

**Rule (D2, decided 2026-10-14):** every 1.3.10 id stays registered, and 1.5.5's new content is added next to it.
- Where 1.5.5 only **renamed** a thing, the 1.3.10 id is kept and gets the 1.5.5 behaviour. There's one item, not
  two, and nothing to remap.
- Where 1.5.5 **replaced** a block with a different one, both exist: the old block as it was in 1.3.10, plus the new
  one.

| 1.3.10 id | Port |
|---|---|
| `cherry_*` wood set | **unchanged** (D1). The display name may become "Dark Cherry" through lang only |
| 1.5.5 `dark_cherry_*` furniture (`cabinet`, `drawer`, `barrel`, `chair`, `table`, `big_table`, `shelf`, `lattice`, `wine_rack_{small,mid,big}`) | added as `cherry_*` (D1). 1.5.5's vanilla-cherry `cherry_lattice` and `cherry_wine_rack_*` merge into these |
| `wine_press` | **kept**, with the 1.5.5 Apple Press behaviour (2 tall, two stages). There's no separate `apple_press` id. Lang: "Apple Press" |
| grape juice (`red_grapejuice_wine_bottle`, `savanna_red_grapejuice_bottle`, ...) | **kept**. The 1.5.5 juice ids aren't registered; recipes and juice tags point at the old ids |
| `vinemaker_apron` / `_leggings` / `_boots` | **kept** (lang can say "Winemaker") |
| `apple_crate`, `cherry_crate`, `red_grape_crate`, `white_grape_crate` | kept. The 10 `*_bag` blocks are added |
| `barrel` | kept. `cherry_barrel` (1.5.5's dark cherry barrel) is added |
| `chair`, `table`, `big_table`, `shelf` | kept as the 1.3.10 blocks. `cherry_chair`, `cherry_table`, `cherry_big_table`, `cherry_shelf` are added |
| `wine_rack_1`, `_2`, `_3`, `_5` | kept. The wood racks and `cherry_cabinet` / `cherry_drawer` are added |
| `grapevine_lattice` | kept, so jungle grapes already growing on it stay. The 10 wood lattices are added |
| `stove`, `wood_fired_oven`, `kitchen_sink`, `calendar`, `basket`, `flower_box`, `flower_pot`, `loam`, `loam_slab`, `loam_stairs`, `praetorian_wine`, `king_danis_wine` | kept as 1.3.10 blocks / items |
| `trippy` | kept (D3) |
| `improved_*` effects | remapped to vanilla (W9) unless D3 decides to keep them |

**Effect on B3.3:**
- W1 shrinks to the effects (W9) and the `vinery:chair` seat entity (W10).
- No item or block remaps are needed.
- W2 (storage block entity ids) and W3 / W4 (barrel and press contents) still apply. (W2 and W3 were removed again
  on 2026-10-27, see B3.3.)
- W7 no longer applies, since `grapevine_lattice` stays.

**Possible look-alike pairs to prune later:** old `chair` / `table` / `big_table` / `shelf` vs the new `cherry_*`
furniture; `*_crate` vs `*_bag`; `barrel` vs `cherry_barrel`; `wine_rack_1` / `_2` vs the wood racks. Compare them in
game (VB-38) before deciding whether to hide any from the creative tab and JEI. Hiding needs no id change.

### B4.2 The mod

- **Where:** `development/backportedVinery`, a subproject of the root Gradle build (`ce.forge-mod` convention), next to
  `backportedBrewin` and `exposureBackport`.
- **Ids:** `mod_id=vinery`, so recipes, tags, structures and worlds keep resolving.
- **Version:** `mod_version=1.5.5-ce-19.2-01`. It replaces `letsdo-vinery-forge-1.3.10.jar` and `doapi-1.1.0b.jar`
  in `mods/`. The 1.3.10 jar moves to `vinery_upstream/` as a resource donor (B4.8).
- **What the jar holds (D13 B):** MIT code ported from upstream, our own code, and small files we write ourselves
  (shims, `mods.toml`, mixin configs). No upstream asset or data file.
- **Dependencies:**
  - Required: Forge 43.3.x, Architectury 6.6 (already in the pack). Architectury can be dropped if the port uses
    plain Forge registries.
  - Optional: JEI 11.8.1, WTHIT 5.36 (D6), Farmer's Delight (heat-source tag, D2).
  - Cloth Config is not needed: the port uses ForgeConfigSpec.

Build order. Each step should boot before starting the next:

0. **Upstream packs (B4.8):** the jar check, the donor packs and the path and JSON translation. Everything after this
   step depends on it for its textures and recipes.
1. **Foundation subset (P1, P7, P9, P21):**
   - Storage blocks and the renderer.
   - Cabinet, chair + seat, table, big table, facing block, window, line-connecting blocks.
   - Fruit leaves.
   - Banner.
   - Particles.
   - Wood: boat, sign.
   - Recipe-book station screen.
   - Armor set tooltip.
   - Rarity.
2. **Registry:**
   - Blocks, items and block entities per B4.1.
   - The 1.3.10 keepers (D2).
   - The creative tab.
   - Remaps (W1, W9, W10).
3. **Barrel** (B2.2): fluid level, juice types, the recipe format, the GUI with a juice gauge, hopper sides. Old
   contents conversion (W3): REMOVED 2026-10-27.
4. **Apple Press** (B2.3): 2-tall block, both recipe types, GUI, old contents (W4).
5. **Grapes** (B2.4):
   - Pot block entity (W5).
   - Stems and grapevine leaves.
   - Lattices + block entity + renderer (W7).
   - Bushes and the taiga soil rule.
   - Fox mixin.
   - Fruit leaves (W6).
   - Stackable-log campfire.
   - Slabs and their mixins (P17).
6. **Wine** (B2.5, **B4.5**): the vintage clock, `WineYear` NBT in clock ticks, storage aging hooked straight into the
   ported storage block entity (no mixin needed), tooltip, effect list setting. (`Year` conversion, W8: REMOVED 2026-10-27.)
7. **Effects** (B2.5, P6). Experience orb mixin, fall-damage / jump mixins, the double-jump client handler.
8. **Entities and trades:** mule, Wandering Winemaker (the port's own spawner since 2026-10-27, I4, I5), villager profession +
   config trades (P16).
9. **Worldgen** (P15, F7). **Commands** (B2.6). **Data** (F1 to F6, done in memory by the B4.8 translation). **Config** (P14).
10. **Client:** renderers (lattice, pot, storage pot, log, bottles, racks, banner), screens, colours, armor models.
    WTHIT lines (D6, B4.6) in place of the info overlay.
11. **JEI** (I3).
12. ~~**Old-world conversion pass** (W2, W3, W6) and a scratch-server boot on a copy of a 1.3.10 world.~~ REMOVED 2026-10-27 (B3.3).

### B4.3 API mapping, 1.21.1 Fabric -> 1.19.2 Forge

Rows shared with Part A 3.1 (data components, codecs, packets, `c:` tags) apply as written there. Vinery-specific:

| 1.21.1 | 1.19.2 Forge |
|---|---|
| `ItemInteractionResult useItemOn` / `InteractionResult useWithoutItem` | `InteractionResult use` |
| `ColorParticleOption` | own `ParticleOptions` + `ParticleType` with a deserializer |
| `TreeGrower` | `AbstractTreeGrower` |
| `BlockSetType` / hanging signs | 1.19.2 door/button constructors; no hanging signs |
| `MobEffect#applyEffectTick` boolean, `onEffectStarted`, `onMobRemoved` | void tick, `addAttributeModifiers`, `removeAttributeModifiers` |
| Attribute modifier `ResourceLocation` ids | UUIDs, one constant per effect |
| `Leashable#setLeashedTo` | `Mob#setLeashedTo` |
| Architectury `CommandRegistrationEvent` | `RegisterCommandsEvent` |
| Fabric `BiomeModifications` | `forge:add_features` biome modifier JSONs |

### B4.4 Pack-side changes

| File | Change |
|---|---|
| patcherMod | Remove `FermentationBarrelStackOutputMixin` (I2). Trim `compat/vinery` to the stomping and aging pages (I3). Rewrite `WinemakerTrades` (I4). Repoint `WinemakerSpawner` (I5) and `StuckInBlock` (I6). Update `crop_growth.json` (I8) |
| [../kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) | I10 |
| [../kubejs/data/creativeengineering/kubejs/tags/wood_tags.js](../kubejs/data/creativeengineering/kubejs/tags/wood_tags.js), [../kubejs/server_scripts/tags/blocktags.js](../kubejs/server_scripts/tags/blocktags.js) | I11, I12 |
| [../kubejs/data/creativeengineering/kubejs/engineering/fabric.js](../kubejs/data/creativeengineering/kubejs/engineering/fabric.js), [../kubejs/server_scripts/other/misc_mods.js](../kubejs/server_scripts/other/misc_mods.js) | I13 |
| [../kubejs/server_scripts/loot/07_rules.js](../kubejs/server_scripts/loot/07_rules.js), [../kubejs/startup_scripts/compost/compost_values.js](../kubejs/startup_scripts/compost/compost_values.js) | I14, I15 |
| [../kubejs/startup_scripts/func/tool_effects.js](../kubejs/startup_scripts/func/tool_effects.js) | I16 |
| [../kubejs/assets/creativeengineering/lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json) | I17 |
| `config/vinery-common.toml` (new) + `configs_edited/` | `[aging]` per B4.5 (`daysPerYear` 16, `yearsPerEffectLevel` 2, `playtimeHoursPerYear` set to today's pace); `[winemaker_set]` per B4.7 (the pack's values today); `trader.spawnChance` 0 (B2.7); the remaining values per D5 |
| `config/jei/recipe-category-sort-order.ini` + `configs_edited/root_config/jei/` | I3 |
| `config/ftbquests/quests/chapters/progression.snbt` | I20 (no change if D2 keeps the flower box and pot) |
| Part A 2.7 heat audit | I21 |
| [vinery.md](vinery.md), [vinery_jei.md](vinery_jei.md), [tooltips.md](tooltips.md) | rewrite vinery.md 2.x for 1.5.5 behaviour once built; retire VJ tests that cover removed pages |
| Deploy | I26 |

**Outcome (2026-10-18):**

| Item | Result |
|---|---|
| I2 | `FermentationBarrelStackOutputMixin` deleted, entry removed from `creativeengineering.mixins.json` |
| I3 | patcherMod's `VineryJEI` stands down entirely when `net.satisfy.vinery.forge.VineryForge` is present: the port ships all five pages (barrel, both press stages, and its own Grape Stomping and Wine Aging). JEI sort order (`config/jei/` + `configs_edited/root_config/jei/`): the four `creativeengineering:vinery_*` lines became `vinery:wine_fermentation`, `apple_press_mashing`, `apple_press_fermenting`, `grape_stomping`, `wine_aging` (in configs_edited after the Brewin' lines) |
| I4 | `WinemakerTrades` tries `net.satisfy.vinery.core.entity.WanderingWinemakerEntity` first, then 1.3.10's class; the port keeps the static `TRADES` map. `/dumptrades` (`villager_trades_dump.js`) does the same |
| I5 | `WinemakerSpawner` reads `config/vinery-common.toml` `[trader] spawnChance` when that file exists, else 1.3.10's json5; CEConfig comment names both. 2026-10-27: the spawner moved into the port (`[trader] ownSpawner`, `ownSpawnerInterval`); CEConfig `winemakerEnabled` / `winemakerIntervalTicks` and the toml check are gone |
| I6 | `StuckInBlock` has a rule for the port's `GrapeBushBlock$TaigaGrapeBush` next to the 1.3.10 one |
| I7 | unchanged; still VB-27 |
| I8 | `crop_growth.json`: the 10 `vinery:*_lattice` ids (listed one by one) |
| I10 | all 144 dict ids still exist (D1/D2). 35 new items added in a block after the vinery section; `vinery_` prefix where a key was taken (`vinery_cherry_cabinet`, `_chair`, `_drawer`, `_shelf`, `_table`) |
| I11 | no change: the pack only adds `minecraft:barrel` to `vinery:barrel` / `vinery:wooden_barrels`, which still load from 1.3.10's tags |
| I12 | BlockRunner: racks and lattices with wood, `vinery:*_bag` with the sacks (soft), `dirt_path_slab` with paths, `window_block` with glass |
| I13 | recipe id `vinery:vinemaker_leggings` -> `vinery:winemaker_leggings` (1.5.5's file name; the result is still `vinery:vinemaker_leggings`) in fabric.js and misc_mods.js |
| I14 | no change: `apple_crate` is a D2 keeper |
| I15 | the 10 bags at 1.0 next to the crates. `global.compost` rebuilds the table, so the port's own 0.4 list never fights it; its items already have rows |
| I16 | no change (D3 keeps trippy) |
| I17 | the 17 `effect.vinery.*` TODO placeholders removed (they would hide 1.5.5's real descriptions); trippy's description is in the port's lang. `tools/gen_effect_descriptions.py` now also reads the 1.5.5 donor jar and drops placeholders whose effect is gone |
| I18 | no change (same ids) |
| I19 | re-diffed: 1.5.5 only lowers `tries` (36 -> 32 or 33) on four bushes; the pack's overrides (soil test) stay as they are |
| I20 | no change (D2 keeps the flower pot and box) |
| I21 | no change: 1.3.10's `farmersdelight:heat_sources` tag still loads with the kept stove and oven. The fired stackable log stays a 2.7 candidate |
| I22 | no change (D1 keeps the wood ids) |
| I23 | no change: the Vinery lines in tooltips.js are commented-out placeholders |
| I24 | `tools/placeable_catalog.py` knows the port's `DrinkBlockItem` (sneak to place) |
| I25 | NOT DONE: rerun the generators and `/dumpall` after the jar swap |
| I26 | mod_sides row swapped, doapi row removed (nothing else needs doapi: scanned every mods.toml); `vinery_upstream/` deploy category. Swap in `mods/` DONE 2026-10-18 (port `-02`, patcherMod 1.0.8) |
| Config | none to write: the port's spec defaults are the pack values (aging 16 days per year, 2 years per level, 28.2 h per year; `[winemaker_set]` as today; `trader.spawnChance` 0). Forge writes `config/vinery-common.toml` on first boot. 1.3.10's `config/vinery/config.json5` becomes unused |

### B4.5 Wine aging revamp: playtime clock (decided 2026-10-14)

**2026-10-18: partly replaced.** Your B7 list asks for sleeping to age wine, ages in months (one Serene Seasons
sub-season each), +1 level every 3 months and x2.5 in the dark. The plan is in [time.md](time.md) 3.7. What stays
from this section: the clock is a counter in the world's saved data, ages re-scale when the settings change, and
clients get the clock in `VintageClockPacket`.

**Goal:**
- A wine's age is measured against real playtime, shown in in-game style days and years.
- The scale is set in config, for example (made-up numbers) "5 hours of play = 15 days = 1 year".
- The lengths stay as today for now (16 days per year, 2 years per effect level), with a default rate that matches
  today's pace. The pack can tune them later without touching any wine stacks.

**Why neither the old clock nor the 1.5.5 clock works in this pack:**

| Clock | Problem here |
|---|---|
| 1.3.10: `dayTime / 24000 / yearLength` | Better Days stretches the day (`daySpeed` 0.125, `nightSpeed` 1.0, RATIO): one world day is about 126,900 ticks, about 106 min. Sleeping and `/time set` move `dayTime`, so they age wine |
| 1.5.5: `gameTime / 24000` | A "day" is always 20 min, so it doesn't match the stretched world day the player sees. It also runs while nobody is online on a dedicated server |

**The vintage clock:**
- One counter per server, `agingTicks` (a long), saved in overworld SavedData `vinery_aging`.
- It goes up by 1 per server tick when the setting `aging.clockMode` allows it:
  - `PLAYTIME` (default): at least one player is online, in any dimension. An empty server doesn't age wine.
  - `SERVER_TICKS`: every tick, like 1.5.5.
  - `WORLD_TIME`: by the overworld `dayTime` delta, like 1.3.10 (for comparison or rollback).
- Nothing else moves the clock. Sleeping, `/time` and Better Days' speeds don't touch it. `/wine clock` (below) can.

**Settings (`[aging]` in `vinery-common.toml`):**

| Key | Default | Meaning |
|---|---|---|
| `clockMode` | `PLAYTIME` | above |
| `playtimeHoursPerYear` | ~28.2 (today's pace, see below) | real hours of clock time per wine year |
| `daysPerYear` | 16 (kept) | how many aging days make a year. One aging day = `playtimeHoursPerYear / daysPerYear` real hours |
| `dayUnit` | `AGING` | `AGING`: days are the unit above. `WORLD`: days are real world days (see below) |
| `worldDayMinutes` | 0 (auto) | for `dayUnit = WORLD`: the length of one world day in real minutes; 0 = measure it |
| `yearsPerEffectLevel` | 2 (kept) | as today |
| `maxLevel` | 5 | as 1.5.5 |
| `cellarAgingMultiplier` | 1.5 | 1.5.5 storage bonus with no sky light, applied to clock ticks |
| `ageOnlyInStorage` | false | 1.5.5 setting, kept |
| `durationPerYear`, `maxDuration` | 200, 15000 | 1.5.5 effect length growth, kept |

**Units:**
- `ticksPerYear = playtimeHoursPerYear x 72000`.
- `ticksPerDay = ticksPerYear / daysPerYear`.
- Age in days = `(agingTicks - brewedAt + storage bonus) / ticksPerDay`. Years and the effect level follow from that.
- **Worked example (made-up numbers):** `playtimeHoursPerYear = 5`, `daysPerYear = 15`. Then:
  - One aging day = 20 min of play.
  - One year = 5 h.
  - Level II (2 years per level) needs 10 h of play, or about 6.7 h in a dark cellar.

**`dayUnit = WORLD`** makes "days" match the days players actually see under Better Days:
- `playtimeHoursPerYear` is then worked out as `daysPerYear x worldDayMinutes / 60`.
- With `worldDayMinutes = 0`, the clock measures it: it keeps a running average of overworld `dayTime` advance per
  played tick over the last few in-game days, ignoring jumps from sleeping or commands.
- This is the "time scaling" option: change Better Days' speeds and wine aging follows.
- `AGING` stays the default because it's predictable and easy to state ("5 hours = 1 year").

**Today's pace (the default, so nothing changes for players yet):**
- 16 world days per year, and a world day of about 106 min from `config/betterdays-common.toml`. The day half is
  14,700 dayTime units at 0.125 per tick = 117,600 ticks; the night half is 9,300 ticks.
- So `playtimeHoursPerYear` ≈ **28.2**.
- Sleeping through every night makes today's real year about 26 h. The difference is small.
- Measure it once in game before trusting the number (VB-34).

**Wine NBT (replaces 1.5.5's `WINE_YEAR` component):**
- `WineYear { brewedAt, storedSince, storedTicks, bonusTicks, storageRate }`, all in clock ticks.
- `brewedAt` is rounded down to the start of the current aging day. That way wines from the same barrel on the same
  day stack (1.5.5 does the same with days).
- 1.5.5 copies `daysPerYear`, `yearsPerEffectLevel`, `maxLevel` and the duration settings **into every stack** at brew
  time. The port does not. Those are read from config when needed, so changing the scale re-scales every existing wine
  at once. That's what lets the lengths be tuned later.
- Storage: the 1.5.5 start/stop logic, in ticks. A wine in any storage block counts its stored ticks at `storageRate`:
  100%, or ×1.5 with no sky light.

**Commands (`/wine`, from 1.5.5, adapted):**
- `/wine time`: clock value, mode, ticks per day / year, the real time for a day, a year and a level, and the measured
  world-day length.
- `/wine info`: the held wine's age (days / years), level, real playtime left to the next level (cellar and normal).
- `/wine clock add|set <ticks|days|years>` (op 2): moves the vintage clock, for testing.
- `/wine years|days|mature|fresh`: as 1.5.5, working on the stack's `brewedAt`.

**Tooltip:**
- "Age: 1 year, 3 days".
- "Next level in 4 days (about 1 h 20 min of play; 53 min in a cellar)".
- "Fully aged".
- "In storage: cellar ×1.5" while it's in a rack.

**Later, not now:** a `SEASONS` mode that ties the wine year to the Serene Seasons year.

### B4.6 WTHIT lines (D6, decided 2026-10-14)

These go in patcherMod's WTHIT plugin (next to Run Speed and Growth), not Foundation's overlay:

| Block | Lines |
|---|---|
| Grapevine Pot | grape type and count; stomps done / needed (heavy-armor bonus shown when it applies); juice bottles left |
| Aging Barrel | juice type and level (x / 100); current recipe and progress %; bottles in the bottle slot |
| Apple Press | mashing % and fermenting %, each with its input |
| Racks, shelves, wine box, placed wine bottle, storage pot | contents. Wines also show their age, level, and "cellar ×1.5" when there's no sky light |
| Stackable log | lit or not; burn time left; cooking slots |
| Grapevine stem / lattice | growth already comes from the crop-growth line (`crop_growth.json`, I8); add "leaves pending" for stems |

Client settings: a WTHIT toggle per line group, using WTHIT's own config.

**Built 2026-10-18, in the port, not patcherMod:** `net.satisfy.vinery.forge.compat.wthit` (`VineryWthit` common:
toggles + server data; `VineryWthitClient`: body lines; `waila_plugins.json` in the port jar). The lines need the
port's block entities and its vintage clock, which patcherMod could only reach by reflection or by compiling against
our own jar. The D6 decision (WTHIT, not Foundation's overlay) is unchanged.
- Server data for the barrel (juice, level %, recipe and %, bottles), press (both stages with input and %),
  stackable log (fire left, cooking %), and storage contents. Storage pots are read from their saved tag, so looking at
  one never rolls a loot table. Wine age, level and cellar rate are computed on the server.
- The grapevine pot syncs to clients by itself: grapes x/6, stomps x/needed, the heavy-armor line when the player
  wears 15+ armor, bottles left.
- Toggles: `vinery:grapevine_pot`, `fermentation_barrel`, `apple_press`, `storage`, `stackable_log` (WTHIT config,
  "Vinery" page). Lines reuse upstream's `hud.vinery.*` / `tooltip.vinery.fermentation_barrel.*` strings where they
  exist; the rest are `wthit.vinery_backport.*` in the port's lang.
- Not built: "leaves pending" for stems, and the 1.3.10 legacy wine racks (only the port's storage blocks and pots).

### B4.7 Winemaker set bonus settings (D11, decided 2026-10-14)

`[winemaker_set]` in `vinery-common.toml`:

| Key | Default (= the pack today) | Meaning |
|---|---|---|
| `enabled` | true | the set bonus at all |
| `keepChance` | 1.0 | chance the bone meal isn't used up |
| `damageMode` | `ALL_PIECES` | `NONE` (no damage ever), `ALL_PIECES` (1.3.10 and 1.5.5), `ONE_PIECE` (one random piece) |
| `damageChance` | 0.30 | chance a use deals damage (ignored for `NONE`) |
| `damageAmount` | 1 | damage per piece per use (1.5.5 hardcodes 2) |

- Setting `damageMode = NONE` or `damageAmount = 0` both mean no damage.
- The bonus runs only when vanilla returned CONSUME. The refund and the patcherMod charm (I7) must never both apply
  to the same use: the port's mixin marks the use as refunded, and the charm skips it (VB-27).

### B4.8 Upstream jars as resource donors (D13 option B, chosen 2026-10-18)

**The user's plan:** ship the unmodified 1.5.5 jar with the pack even though it can't run on 1.19.2, and have the port
read its assets and data from that jar. **Verdict: feasible.** Checked 2026-10-18 against
`~/Downloads/letsdo-vinery-fabric-1.5.5.jar`, its extracted copy in `DecompiledMods/` and the 1.19.2 client jar.

**Findings**

| Question | Answer |
|---|---|
| Can the 1.5.5 jar sit in `mods/`? | No. FML 43.3.9 (`AbstractModProvider.createMod`) takes a jar only with `META-INF/mods.toml` or an `FMLModType` manifest entry. The Fabric jar has neither, so FML rejects it as an invalid mod file. It goes in its own folder, `vinery_upstream/` (name not final) |
| And the 1.3.10 jar? | It leaves `mods/` too: its mod id `vinery` collides with the port. It becomes a second donor (next rows) |
| Do 1.5.5's models load on 1.19.2? | Yes, nearly as they are. Every vanilla parent and texture the 418 models name exists in 1.19.2. `render_type` (6 models) is supported by Forge 43. No 1.21-only model keys, no custom loaders. Blockstates are plain `variants` / `multipart` |
| What do the models need from elsewhere? | 7 `foundation:block/template_*` parents, which come from the Foundation jar nested at `META-INF/jars/foundation-fabric-1.0.1.jar` (89 asset files). 21 references point to Vinery files that don't exist; they're broken in 1.5.5 too |
| Does 1.5.5 art cover every block we keep (D1, D2)? | Not all of it. Of 51 blockstates only 1.3.10 has, 20 `cherry_*` blocks have 1.5.5 art under `dark_cherry_*` (alias). The other 31 (stove, kitchen sink, oven, calendar, basket, flower box and pot, loam set, old chair / table / big table / shelf / barrel / crates, `wine_press`, `wine_rack_1` to `_5`, old juice bottles, praetorian / king danis, `vinery_vine`, `grapevine_lattice`, `cooking_pot`) exist only in the 1.3.10 jar |
| Data | 147 recipes: 116 vanilla crafting and 1 smithing transform need F2 / F5; the 30 Vinery-type recipes are read by the port's own serializers, which can take the 1.21 shape directly. 109 loot tables (6 with 1.21 `match_tool` predicates, F6), 70 advancements (F4), 30 Vinery tags + 46 other-namespace tags (F1, F3). 1.3.10's 373 data files would clash or fail to parse under the new recipe types, so only an allow-list (the D2 keepers' recipes and loot) is read from it |
| Which machines need the jars? | Both. Clients for assets, the server (and every single-player world) for data |
| Is the source on hand? | Modrinth version `PDi0T5xs` (project `1DWmBJVA`) is the same file (sha1 `9d4c7646b91219a75a432b6dea1d7a7c41faaaec`) and has a `-sources.jar` with the real MIT sources. The 1.3.10 jar (sha1 `fbbd4ae7c10a9d0aa13819e09b70167f3b4ff81c`) isn't on Modrinth; CurseForge only |

**Design**

- **Packs:** the port adds packs in `AddPackFindersEvent` for `CLIENT_RESOURCES` and `SERVER_DATA`: required, fixed,
  position bottom. Resource packs, KubeJS (`kubejs/data/vinery/worldgen/configured_feature/*`) and datapacks still
  override them.
- **Layers, top first:**
  1. The port's own files: shims we write (the 20 `cherry_*` -> `dark_cherry_*` blockstates and item models) and any
     data we decide to write ourselves.
  2. The 1.5.5 jar.
  3. Foundation, opened straight from the nested jar (a zip file system inside the zip, no extraction).
  4. The 1.3.10 jar: all assets, data only through the allow-list.
  Lang merges across the layers by key, as it does across resource packs. 1.5.5 names win, and 1.3.10 fills in the
  keepers.
- **Translation layer:** one `PackResources` (1.19.2 `AbstractPackResources`) over each jar's zip file system.
  - Maps 1.19.2 paths to the 1.21 ones (F1: `recipes/` -> `recipe/` and the rest), both for single lookups and for
    `getResources` listings.
  - Rewrites JSON as it reads it (F2 to F6). The result is kept in memory, so each file is converted once per reload.
  - Supplies its own pack metadata (resource format 9, data format 10). Nothing is ever written to disk.
- **Pinned input:** the port checks both jars' sha1 on startup. Missing or different -> it stops with a message naming
  the file, the folder and the download page (a client error screen, a server log line before the world loads). A
  silent start would leave a server with no Vinery recipes or loot. The pin also means the translation only ever sees
  one known input, so it doesn't need to handle any other upstream version.
- **Worldgen stays inside 1.19.2's feature set (user, 2026-10-18):** no new worldgen code is backported. A 1.20+ type
  in upstream worldgen data is rewritten to the nearest 1.19.2 vanilla type. The only one is
  `minecraft:cherry_foliage_placer` (dark cherry and apple trees) -> `minecraft:fancy_foliage_placer` with the same
  `radius`, `offset` and `height` (it pairs with their `fancy_trunk_placer`, like vanilla's big oak); the hole and
  hanging-leaf chances are dropped. Biome placement uses 1.3.10's own `forge/biome_modifier` files (1.3.10 ids, so
  `wild_crops.json` keeps matching), which point at the same placed features 1.5.5 has.
- **Optional:** 1.5.5's built-in `resourcepacks/bushy_leaves` can be offered as an optional resource pack from the same
  jar.

**What changes in the plan**

- B4.2 step 0 (above) comes first. The old "copy upstream assets into the port" assumption is gone.
- F1 to F6 (B3.2) are the same rules but run in Java at load time instead of in a build script like Backported
  Brewin's `port_resources.py`. Estimate: 1 to 1.5k lines for the packs, the translation and the check.
- Deploy (deploy.md): a `vinery_upstream/` category in `build_deploy.py`, `verify.sh` / `.ps1` hash lines, mod_sides
  rows for both jars, and removal of the 1.3.10 and doapi jars from `mods/`. The rest depends on D14.

**Risks**

- **Author relations:** see B3.6. The plan stays inside the license texts, not inside the author's wish.
- **Leaks into the port:** an upstream file copied into `src/main/resources` by habit would break D13 B. B6 adds a
  build check against both jars.
- **Dangling and missing art:** the 21 dangling references and any keeper without art show the missing texture. The
  first boot will list them in the log (B7 VB-42).

### B4.9 Port status (2026-10-18)

`development/backportedVinery` (Gradle `:backportedVinery`, jar `backportedVinery-1.5.5-ce-19.2-01.jar`). Compiles; not
booted yet.

- **Sources:** 1.5.5's real sources (Modrinth `-sources.jar`), Foundation 1.0.1 from its GitHub tag `v1.0.1-1.21.1`,
  the 1.3.10 decompile for the D2 keepers. Kept in `DecompiledMods/` (`vinery-1.5.5-src`, `foundation-1.0.1-src`,
  `vinery-1.3.10-src`).
- **Tools:** `tools/port_java.py` (first mechanical 1.21 -> 1.19.2 pass, copies only files the port doesn't have),
  `tools/port_fixups.py` (second pass, in place), `tools/gen_at.py` (unused so far: no access transformer needed).
- **Layout:** `net.satisfy.foundation` (own mod id `foundation` in the same jar, so its ids stay upstream's),
  `net.satisfy.vinery.core|client|api` (1.5.5), `net.satisfy.vinery.legacy` (1.3.10 keepers),
  `net.satisfy.vinery.forge` (entry, config, events, villagers, network, client, `upstream/` = B4.8).
- **Port decisions (1.19.2 forced a different shape):**
  - Blocks keep upstream's `useItemOn` / `useWithoutItem`; `foundation.util.ItemInteractionResult.dispatch` routes
    1.19.2's single `use` through them in 1.21's order.
  - Recipe inputs are small `SimpleContainer`s; the serializers read upstream's 1.21 JSON (`"id"` or `"item"` stacks).
  - Effects: UUID attribute modifiers, one stable UUID per upstream id. Jellie adds its absorption on start and takes
    it back on end (1.21's start / add pair cancelled out).
  - Storage aging hooks: a listener on Foundation's storage block entity instead of upstream's mixin.
  - Mixins avoid local capture: grass-to-slab spreading wraps the loop's block lookup; the experience effect modifies
    the call argument.
  - `FireBlock#setFlammable` and `GameRenderer#loadEffect` (trippy) are called by SRG name (no access transformer).
  - Signs: block entity `vinery:mod_sign`; old 1.3.10 signs keep `minecraft:sign`. Wood type name `vinery:dark_cherry`
    (1.5.5's sign and boat textures). Chest boat built on vanilla `ChestBoat`. Boats placed in 1.3.10 worlds
    (`doapi:boat` entities) are lost with doapi.
  - Press block entity id is 1.3.10's `wine_press`: old presses load straight into the new one (W4).
  - Barrel: a 1.3.10 barrel (no `FluidLevel` key) converted on load per W3 (removed 2026-10-27).
  - Winemaker job site: the Aging Barrel (1.5.5); 1.3.10's was the wine press.
  - Port-only text lives in `assets/vinery_backport/lang/` (keys are global; the path never matches upstream's).
- **Open decisions built with the recommendation:** D3 (trippy kept as a client post shader, `improved_*` remapped
  to vanilla, remap removed 2026-10-27), D4 (1.5.5 effect list, plus praetorian Glowing and king danis Instant Health), D5 (1.5.5 numbers;
  aging per B4.5), D7 (Foundation bundled), D8 (no village houses), D9 (no hanging sign), D10 (rotten cherry kept,
  setting `rottenCherryChance` 0.15), D12 (bamboo racks / lattice from `quark:bamboo_planks`).
- **Done since (2026-10-18):** W2, W3, W4, W6, W8 to W10 (W2, W3, W8 to W10 removed again 2026-10-27); praetorian / king danis barrel recipes
  (`data/vinery/recipes/port/`); WTHIT lines (B4.6); pack-side changes (B4.4 outcome table); deploy category.
- **Fixes found by review, no boot yet:**
  - `mods.toml` declared only `vinery`. Foundation is a second `@Mod`, and FML rejects an `@Mod` class with no entry,
    so the game would not have started. Added `[[mods]] foundation` and a vinery -> foundation dependency.
  - Advancements: 1.21's location predicate `"biomes"` (also inside `entity_properties`) is now rewritten to 1.19.2's
    `"biome"`. Without that, `fruits_of_the_field` fired in any biome.
- **Data survey (2026-10-18):** every type, function, condition and trigger in 1.5.5's and Foundation's data was listed
  against what the translator handles. Nothing else needs 1.21 features. No Fabric or NeoForge load conditions, no
  item components. The 1.3.10 biome modifiers name placed features that exist in 1.5.5. The port registers every
  1.5.5 id through the B4.1 map.
- **First boot (2026-10-18, user):** crashed during mod construction: `FoxEatBerriesGoalMixin` shadowed the inner
  class's synthetic outer field as `this$0` with `remap = false`. That name only exists in dev mappings; in SRG the
  field is `f_28672_`, so Mixin threw `InvalidMixinException`. Fixed by dropping the shadow and casting the inherited
  `MoveToBlockGoal.mob` (`f_25598_`, remapped normally) to `Fox`. Rebuilt and copied into `mods/`. No other port
  mixin uses `remap = false` or synthetic names. Rule for later ports: never shadow `this$0`/`val$x`; use a mapped
  field or an accessor.
- **Second boot (2026-10-18, user):** `LegacyFlowerPotBlock` (1.3.10 keeper) threw an NPE in its static init, which
  aborted block registration; the IE / salt / geode / fireflybushport "Registry Object not present" errors after it
  were knock-on. The decompile declared `SHAPE = voxelShapeSupplier.get()` above the supplier (static fields
  initialise in source order). Moved `SHAPE` below it. A scan of the whole port for static forward references found
  no other case.
- **Third boot (2026-10-18, scratch client, main menu only):** reaches the main menu with no Vinery/Foundation errors.
  Only Vinery lines: first-boot `vinery-common.toml` corrections, and 4 `#missing` texture references in upstream
  1.5.5's own models (`stal_wine`, `aegis_wine`, `strad_wine`, `glowing_wine`: faces point at an undefined
  `#missing`; 1.3.10's models don't). Cosmetic; a port-written model override would fix it. Since 10.14 the 4x rack
  and the wine box draw slim models for all four, so the faces only show placed or in the 9x / single-bottle rack.
- **Fourth boot (2026-10-18, user, world `Workspace`):** "Failed to load datapacks": `Unbound values in registry
  worldgen/configured_feature: [vinery:apple]`. 1.5.5's `apple` / `apple_variant` tree configs carry a stale
  `sapling_provider` naming `vinery:apple_sapling`; the port's sapling is `apple_tree_sapling`, so `keep()` dropped
  both files (DEBUG line only) and `placed_feature/tree_apple` pointed at nothing. 1.19.2's `TreeConfiguration` has no
  `sapling_provider`, so `convertWorldgen` now strips it from tree configs. Dropped worldgen / biome-modifier files
  now log at WARN. The other 5 dropped files are the expected cherry-wood and hanging-sign recipes.
- **Not done yet:** a world join and the VB- tests (the user's); I25 reruns. Stem "leaves pending" line: built 2026-10-18 (10.6).

## B5. Decisions (recommendation first; DECIDED ones say so)

### B5 D13. Upstream resources are All Rights Reserved (DECIDED 2026-10-18: B, see B4.8)

Option A was tried: Satisfy refused redistribution of assets and features (B3.6). The user chose B: the pack comes
with the unmodified upstream jar, and the port reads it. How the jar gets onto each machine is D14.

B3.6 explains why. The code can be ported; the textures, models, lang and data files can't be shipped as they are.

- **A (recommended): ask Satisfy for permission.**
  - Ask to ship 1.5.5's resources in a Forge 1.19.2 port that is used only inside this pack.
  - Where to ask: the Let's Do Discord, or `github.com/satisfyu/Let-s-Do-Hub` issues.
  - Ask about Foundation's license at the same time.
  - If they say yes, the plan stands as written. Keep their reply in `development/backportedVinery/`.
- **B: a code-only jar that reads the player's own copy of the upstream jar.**
  - The port contains only MIT code and files we write ourselves.
  - At startup it finds `letsdo-vinery-fabric-1.5.5.jar` in a known folder.
  - It adds that jar's `assets/` as a built-in resource pack and its `data/` as a built-in datapack
    (`AddPackFindersEvent`).
  - The 1.21 -> 1.19.2 conversions (F1 to F5) are done in code while the files load.
  - Costs:
    - Every client and the server need the upstream jar, downloaded from Modrinth or CurseForge by the player or by
      the pack manifest.
    - We have to write the conversion code.
    - Anything 1.5.5 doesn't have, such as the D2 keepers, still needs art. That art could come from the 1.3.10 jar in
      the same way.
- **C: write our own resources.**
  - Recipes, tags, loot, lang and worldgen are small enough to write fresh from the behaviour described in B2.
  - Textures and models are the expensive part: about 170 item models plus blocks, entities and armor.
  - Only makes sense combined with B for the art.
- **D: don't port 1.5.5 as a mod.**
  - Keep the official 1.3.10 jar.
  - Bring over only behaviour that needs no new assets, using patcherMod mixins: barrel juice level, press stages, wine
    aging rules, effect changes.
  - New blocks (lattices, racks, bags, cabinet, banner) and the new effects' icons would be left out.

### B5 D14. How the upstream jars reach each machine (DECIDED 2026-10-18: B for now, A on publish)

The user's call: for now both jars travel in the deploy payload (B). When the pack is published, each machine
downloads them from the official pages instead (A).

Both jars go in `vinery_upstream/` on every client and the server (B4.8).

- **A (recommended): each machine downloads them from the official pages.**
  - `verify.sh` / `.ps1` (or a small fetch step in the runbook) downloads the 1.5.5 jar from the Modrinth CDN
    (`https://cdn.modrinth.com/data/1DWmBJVA/versions/PDi0T5xs/letsdo-vinery-fabric-1.5.5.jar`) and checks the sha1.
    The 1.3.10 jar comes from CurseForge, which has no stable direct link, so that one is a manual download.
  - The pack never passes on a copy of the author's files, which is what their refusal was about.
- **B: put both jars in the deploy payload, like every other jar.**
  - Simplest. Today the pack already copies the 1.3.10 jar by hand.
  - It is a copy of their files to other machines, which is closer to what they refused.

### B5 D1. Cherry wood ids (DECIDED 2026-10-14: keep `cherry_*`)

- **Keep `cherry_*`:**
  - 1.19.2 has no vanilla cherry, so the reason upstream renamed doesn't apply here.
  - Keeps the 65 items other mods generate from this wood (W11), every placed cherry block, and the pack's tags and
    dict keys.
  - 1.5.5's furniture that is named `dark_cherry_*` registers as `cherry_*`. 1.5.5's vanilla-cherry lattice and racks
    merge into ours, since there is no vanilla cherry.
  - The lang can still say "Dark Cherry".
- **Follow 1.5.5:**
  - Remap 22 ids for Vinery's own blocks.
  - The 65 cross-mod items still change ids, because Every Compat and the others build their ids from the wood name.
    We can't remap those from our jar without a remap in each of those mods, so their placed blocks would be lost.

### B5 D2. Blocks 1.5.5 removed (DECIDED 2026-10-14: keep every 1.3.10 block, add the new ones; see B4.1)

The decision goes further than the recommendation below: blocks that have a 1.5.5 successor stay as well.


- **Recommended:**
  - Remap where 1.5.5 has a successor (B4.1).
  - Keep the 1.3.10 blocks that have no successor and that the pack uses:
    - stove and wood-fired oven: Farmer's Delight heat sources (I21).
    - kitchen sink: infinite water source.
    - flower box and flower pot: quest tasks (I20).
    - calendar, basket, loam set.
  - Keep `praetorian_wine` and `king_danis_wine`, with barrel recipes rewritten into the juice format and their 1.3.10
    effects mapped to the closest new ones.
- **Alternative:** follow 1.5.5 and drop them. Placed blocks disappear, the quests need new items, and the B&C keg
  loses two heat sources.

### B5 D3. Removed effects (DECIDED 2026-10-18: keep `trippy`, section 10.1)

- **Recommended:**
  - Keep `trippy` registered. The pack's echo "discombobulate" uses it (I16), and it's only a client shader.
  - Remap the 10 `improved_*` effects to vanilla on load (W9). Removed 2026-10-27 (B3.3).
- **Alternative:** drop `trippy` and replace it in `tool_effects.js` with Nausea.

### B5 D4. Wine effects (DECIDED 2026-10-19: 1.5.5 list)

- **Recommended:** follow the 1.5.5 default list. It's one setting, so the pack can retune any wine later without
  code.
- **Alternative:** seed the setting with the 1.3.10 effects where a counterpart exists. That doesn't fully work,
  because the `improved_*` effects are gone.

### B5 D5. Balance numbers (DECIDED 2026-10-19: as recommended)

- **Aging is decided separately (B4.5)**: keep the lengths (16 days per year, 2 years per level) on the new playtime
  clock. The rest is still open.
- **Recommended:**
  - Use the 1.5.5 defaults for the barrel (6000 ticks, 25 units per juice, 100 max), the press (600 / 800), the pot
    (12 stomps) and growth.
  - Carry over the pack's aging pace: 16 days per year, 2 years per level.
  - Mead's 1-unit juice cost (Q2) is the one value worth changing.
- **Why:**
  - The 1.3.10 barrel's 50 ticks was a bug, not a design (vinery.md 2.3).
  - With the pack's pace, a wine reaches level II in 32 in-game days, or about 22 in a dark cellar. The 1.5.5 pace
    would take 144 days.

### B5 D6. Block info HUD (DECIDED 2026-10-14: WTHIT, see B4.6)

- **Recommended:** don't port Foundation's `BlockInfoOverlay`. Put the same lines (pot grapes and stomps, barrel juice
  and progress, rack contents) into the patcherMod WTHIT plugin, next to the Run Speed and Growth lines.
- **Alternative:** port the overlay with `mode` OFF by default.

### B5 D7. Foundation (DECIDED 2026-10-19: bundled)

- **Recommended:** bundle the Foundation subset inside the Vinery jar, keeping its packages.
- **Alternative:** a separate `foundation` mod jar. That only pays off if more Let's Do mods get backported. Vinery is
  the only one in the pack today.

### B5 D8. Village houses (DECIDED 2026-10-19: skipped; REVERSED 2026-10-20: added, section 13)

- **Recommended (2026-10-19):** skip them (F8, Q5).
- **Alternative (chosen by the user 2026-10-20):** remap their palettes and add them to village pools: built as
  `forge/upstream/VillageHouses`, section 13 (in-memory palette rewrite, pool append at server start, weight = pool
  average by default).
- **What they are (read from the 1.5.5 jar's NBTs 2026-10-20; DataVersion 3465 = 1.20.1, no entities, no loot tables):**

  | File | Size (x y z) | What it is | Jigsaws | Stocked with |
  |---|---|---|---|---|
  | `vinery_plains` | 11 x 15 x 9 | Oak-log and plank cottage on a loam / dirt plot: 4 grapevine stems, apple press, cask, big table x2, cabinet, 9x rack, storage pot, flower boxes, 4 windows, cherry floorboards | entrance only | cabinet: apples, cherries, grapes, apple sapling, apple juice; rack: 3 Mojang Noir; 2 Noir Wine placed; flower boxes |
  | `vinery_taiga` | 11 x 14 x 9 | The plains house rebuilt in spruce logs and planks; same fittings and stock | entrance only | as plains |
  | `vinery_savanna` | 5 x 7 x 9 | Small open acacia vineyard pen: fences and gates, 4 grapevine stems, grapevine pot, cask, acacia 9x and single rack | entrance only | 2 Solaris, 1 Sun-kissed |
  | `vinery_desert` | 8 x 9 x 8 | Smooth / cut sandstone house: cherry door and buttons, chair, table, basket, cherry drawer, 9x rack, hanging sign, grape crates | entrance + a vanilla cat-spawn jigsaw (`village/common/cats`, like vanilla desert houses) | 2 Cherry Wine placed + 2 in the rack; drawer: jungle grape seeds |

- **Against the port's ids today:** most names exist (the stale 1.3-era `cherry_*`, `chair`, `table`, `loam` ids
  happen to be the ones the port keeps, D1 / D2). Would come out as air: `vinery:apple_press` (the port keeps
  `wine_press`), `red_grape_crate` / `white_grape_crate` (removed, 10.10), `cherry_wall_hanging_sign` (D9). Every
  other piece is vanilla 1.19.2-safe (`minecraft:grass` still has its old name in 1.19.2).
- **What adding them would take:** a remap of those 4 ids (to the press, a grape bag or barrel, a plain sign), the NBTs
  kept from the donor jar by `UpstreamTranslator` with those ids patched in memory (they're ARR data like the rest, so
  no copy can ship: D13), and a code step that appends them
  to `minecraft:village/{plains,taiga,savanna,desert}/houses`. PoolTuner only reweights or removes pieces it finds,
  so it can't add them; the winemaker's POI is the cask, so the houses would also bring winemaker villagers.

### B5 D9. Hanging sign (DECIDED 2026-10-19: dropped)

- **Recommended:** drop it (P10). Supplementaries already gives a Vinery cherry hanging sign.

### B5 D10. Rotten cherry (DECIDED 2026-10-19: kept)

- **Recommended:** keep 1.3.10's chance for cherry leaves to drop a rotten cherry. 1.5.5 left the item with no source
  (Q1).

### B5 D11. Winemaker set bonus (DECIDED 2026-10-14: settings for no damage or a set amount, see B4.7)

- **Recommended:** keep the 1.3.10 settings: keep chance 100, damage chance 30, damage 1 (the pack's values today).
- **Alternative:** follow 1.5.5: always keep the bone meal, and every use damages all 4 pieces by 2.

### B5 D12. Bamboo lattice and racks (DECIDED 2026-10-19: kept)

- **Recommended:** keep them, built from `quark:bamboo_planks` (P11).
- **Alternative:** drop them.

## B6. Offline checks

- **Clean port (D13 B): BUILT.** `checkNoUpstreamResources` in the port's build.gradle fails the build if any file
  under `src/main/resources` has a path that is in an upstream jar. Passes.
- **Data survey: DONE 2026-10-18 (B4.9).** It replaces the dry run below for now.
- **Deploy check: BUILT.** `build_deploy.py` refuses to stage while the port is in `mods/` and a `vinery_upstream/`
  jar is missing or has another sha1 than `UpstreamJars.java` pins.
- **Translation dry run:** a headless test that opens both jars through the B4.8 packs and parses every converted
  recipe, loot table, advancement and tag with 1.19.2's own parsers; it lists anything that fails.
- A script that compares the port's `data/vinery/` tree against 1.5.5's after the F1 to F3 renames and the B4.1 id
  map. It should report only the D-decision deviations.
- An id audit: every id in `kubejs/exported/items_by_mod/vinery.json` (1.3.10) is registered in the port or appears in
  the remap table.
- A scratch-server boot on a copy of a world with 1.3.10 Vinery blocks placed (scratch-server memory). Grep the log
  for missing mappings and unknown block entities.

## B7 Changes to make (planned 2026-10-18)

Your lists from 2026-10-18, each item with a plan. **Port** means `development/backportedVinery` (rebuild the jar);
**pack** means KubeJS / tags. Upstream assets and data stay untouched (D13 B). Where wording changes, it goes in the
port's **own** lang file, which is our text and layers above the donor jars (B4.8). Tests: VB-48 to VB-60 (B8); the
time items have their own tests in [time.md](time.md). Questions for you: B7.12.

**No conversions (your rule, 2026-10-18):** both ports are in development, so nothing in this list keeps old items,
effects or ids working across versions. That covers old Vinery to new Vinery, and Vinery to Brewin. Removed things
are simply removed.

### B7 Build status (2026-10-18, not tested in game)

Port `backportedVinery-1.5.5-ce-19.2-02.jar` is built and in `mods/`, and `server_mods/` is regenerated. The version
went up because `-01` shipped in 1.0.7, and a rebuild under the same name wouldn't reach clients.
`config/vinery-common.toml` has the new keys and values.

| Item | Outcome |
|---|---|
| B7.1 JEI | Built. The Cask page cycles the recipe's juice bottles in slot 0 with the bottles needed (`JuiceUtil.juiceStacks`). `FermentationBarrelGui.isOverJuiceBar` is the hover box for the screen and JEI: the gauge plus its frame. The Wine Aging text wraps to 146 px in `0x404040` with no shadow, and the page height comes from the line count. **Also fixed:** the JEI transfer handler paired the page's slots in the wrong order, so ingredient 1 went to the juice slot. It now lists juice, the ingredients, then the bottle (`FermentationTransferInfo`). Transfer needs the juice bottles in your inventory |
| B7.2 Press slots | Built. `ApplePressBlockEntity.isValidForSlot` backs the menu slots (`PressSlot`), `canPlaceItem`, hoppers and shift-click. **Deviations:** slot 1 takes any fermenting-recipe input; today that is apple mash and honeycomb, the same set as "mashing outputs that fermenting takes". There is no cache: it asks the recipe manager, and there are 2 recipes per type. Honey's glass bottle needed a recipe change. Fermenting recipes now carry a `bottle` ingredient (upstream's `wine_bottle.required` still means a wine bottle), and the upstream-pack layer gives `apple_fermenting/honey_bottle` a glass bottle |
| B7.3 Merged effects | Built. `luck_effect`, `resistance_effect` and `health_effect` are no longer registered (`ResistanceEffect.java` deleted). Stal Wine defaults to `minecraft:health_boost` |
| B7.4 Effect reshuffle | PINNED, not built |
| B7.5 JEED providers | Built. Port: `WineEffects.all()` (wine -> effect) and `winesByEffect()` (effect id -> wine ids, strings only, for Rhino). Pack: `R.jeedProvider(effect, providers, e, idOverride)` in NCH, called once per effect at the end of `kubejs/server_scripts/cooking/alcohol.js` (`creativeengineering:jeed/vinery_<effect>`). NCH is a startup script, so it needs a game restart, not just `/reload` |
| B7.6 Mead out | Built. Block, item, storage lists, render layer and effect default are gone. The upstream-pack layer has a `REMOVED` set: tag entries naming `vinery:mead` are dropped, and so are advancement criteria whose item list it empties. **Found:** `wine_somelier` (the advancement that gives the banner) and `the_noble_drop` list mead. The old "leave out the whole file" rule would have dropped both, banner reward included. Now mead is taken out of their lists: 25 -> 24 criteria (checked headless against the 1.5.5 file). Villager trades never had it |
| B7.7 Banner | Built. `[banner] effect / amplifier / radius / durationSeconds / refreshBelowSeconds` (Health Boost II, 8, 60, 10). Below the threshold the running effect is extended in place with `MobEffectInstance.update` plus a `ClientboundUpdateMobEffectPacket`, never re-added, so the extra hearts aren't clamped. The tooltip line comes from the settings (`tooltip.foundation.banner.effect`) |
| B7.8 Time | patcherMod side BUILT too (patcherMod 1.0.8, [time.md](time.md) 3.8): the `[time]` master pushes `daysPerMonth`, the clock, the cellar value and the rest into `vinery-common.toml` (since 2026-10-27 the port reads them from `time.WineTime` instead, time.md 3.7); Serene Seasons calendar tooltip; "7:30 pm" clock. Port side built: `[wine] daysPerMonth` 8, `monthsPerEffectLevel` 3, `durationPerMonth` 200, `cellarAgingMultiplier` 2.5; `[aging] clockMode` WORLD_TIME, a new `dayUnit` DAYTIME (24000 clock units a day, forced on WORLD_TIME), `playtimeHoursPerMonth`. Real-time text divides by the measured day-time pace (`Snapshot.realTicks`). "Month" in the tooltip, `/wine` (literal `months`), JEI, WTHIT and the upstream keys. The Vinery calendar is removed (block, item, classes, keeper data; `i.calendar` / `i.mead` out of `item_dict.js` and the generated food / stack JSONs) |
| B7.9 Cask | Built. **Deviation:** the plan put our wording in the port's `assets/vinery/lang/en_us.json`. The jar can't carry upstream's path (`checkNoUpstreamResources`), and the order between the mod's own pack and the upstream pack isn't guaranteed. So the lines live in `src/main/resources/vinery_port/upstream_lang/en_us.json`, and the upstream-pack layer applies them on top of the merged upstream lang (checked headless). Resource packs still override. `dark_cherry_barrel` and 1.3.10's `barrel` keep their names (VQ2) |
| B7.10 Apple wine | Built (10.12 later made its size come from its per-block limit, 3 = small). `BottleSize.SMALL`. The upstream-pack layer moves `vinery:apple_wine` from `large_bottle` to `small_bottle`, in place of an own tag file (same reason as B7.9). Its block still holds 3 |
| B7.11 Praetorian | SETTLED by 10.8 (small racks, shrunk); medium since 10.12, so the 4x rack, the wine box and the single-bottle rack, drawn with its own model (no slim, 10.14) |
| B7.14 Stomping yield | Built 2026-10-19 into `-03` (in the local `mods/` since 22:27): 12 grapes, 4 stomps, 4 bottles |

### B7.1 JEI (port)

- **Aging Barrel page, juice item in the input slot:**
  - **Now:** the page shows the juice only as a gauge (`VineryJEIPlugin.buildSlotsFromRecipe`).
  - **Plan:**
    - Add an INPUT slot where the barrel's juice slot (0) sits.
    - It cycles the juice bottles of the recipe's juice type (red / white per region, apple; from `JuiceUtil`).
    - The count is the bottles needed: units / `maxFluidIncrease` (25), rounded up.
    - Looking up the uses of a juice bottle then lists the wines it makes.
- **Aging Barrel page, hover text over the whole bar:**
  - JEI's hover check (`FermentationBarrelCategory#isMouseOverFluidArea`) is a 2 x 10 px box around x 56.
  - The barrel screen's check (`FermentationBarrelGui`) spans the width but only from 5 above to 10 below
    `FLUID_Y`.
  - **Plan:** both use the gauge's full rectangle, empty part included, so a quarter-full bar shows its tooltip
    anywhere on the bar.
- **Wine Aging page text:**
  - **Now:** `WineAgingCategory#draw` writes three single lines centred on a 150 px page (`drawCentered`). They run
    past the grey background, in white.
  - **Plan:**
    - Wrap each line to the page width (`font.split`).
    - Draw in dark grey with no shadow, like JEI's own text (`0x404040`, decided 2026-10-18).
    - Size the page height from the number of wrapped lines.
- **Tests:** VB-48, VB-49, VB-60.

### B7.2 Apple Press: valid items only (port)

- **Now:**
  - The left slot of the second stage (slot 1) accepts nothing: `canPlaceItem` returns false for it.
  - The right slot (2) takes only a wine bottle, so honey's glass bottle can't go in, by hand or by hopper
    (`canPlaceItemThroughFace`).
  - The menu's slots don't all ask the block entity, so the GUI and hoppers disagree.
- **Plan:** one rule, built from the recipes and cached per reload:
  - Slot 0 takes the input of a mashing recipe.
  - Slot 1 (left) takes the **intermediates**: whatever a mashing recipe outputs and a fermenting recipe takes first.
    That is apple mash and honeycomb.
  - Slot 2 (right) takes a fermenting recipe's second ingredient: wine bottle and glass bottle.
  - The same rule backs the menu's slot classes (`mayPlace`), `canPlaceItem`, `canPlaceItemThroughFace` and
    shift-click (`quickMoveStack`).
- **Test:** VB-50.

### B7.3 Merge duplicate effects (port)

| Vinery effect | What it does | Merged into | Used by |
|---|---|---|---|
| `luck_effect` | +2 luck | `minecraft:luck` | no wine |
| `resistance_effect` | +0.2 knockback resistance, +2 armor toughness | `minecraft:resistance` | no wine |
| `health_effect` | +4 max health | `minecraft:health_boost` (Health Boost I is also +4) | Stal Wine |

- **Plan:** stop registering the three (`MobEffectRegistry`), point Stal Wine's default at `minecraft:health_boost`,
  and drop their JEED description keys.
- No remap of the old ids (no-conversions rule).
- **Test:** VB-51.

### B7.4 Wine effect reshuffle: PINNED (future plan)

- Parked by you on 2026-10-18; nothing here is built in this round.
- **When it comes back:**
  - Wines may apply **two** effects. The `[wine] effects` entry then takes a list of effects.
  - Food-mod effects (Fruits Delight's 18, Neapolitan, Farmer's Delight, Farmer's Respite, Alex's Mobs and others)
    become candidates, each with a vanilla or Vinery fallback for when its mod is missing.
  - A `tools/wine_effect_candidates.py` lists every effect with its JEED description, to pick from.
- Today's one-effect list stays as is, apart from B7.3 and B7.6.

### B7.5 JEED providers for wine effects (pack, small port accessor)

- JEED lists which items give an effect from `jeed:effect_provider` recipes: `{effect, providers: [items or tags]}`
  (format from its jar, `data/jeed/recipes/*.json`).
- Wine effects come from config, so the recipes are made at load time, not written as files:
  - **Port:** `WineEffects.all()` returns the parsed map (wine id -> effect). When the B7.4 reshuffle lands, it maps
    to a list.
  - **Pack:** a new `R.jeedProvider(effect, providers, e, idOverride)` in NCH. `alcohol.js` calls it once per effect
    with every wine that gives it, reading the map with `Java.loadClass('net.satisfy.vinery.core.wine.WineEffects')`.
  - Changing the config and running `/reload` rebuilds them.
  - 2026-10-20: the loop moved to `kubejs/server_scripts/other/jeed_providers.js`, which writes the providers for
    every effect ([effects.md](effects.md) 3). It also lists the Vinery standard banner for its configured effect.
- **Optional, same helper:** Brewin's drinks (Tipsy, Intoxication, Raging, Sweet Heart) and the pack's own effects.
  This ties in with "merge backported effect descriptions w/ existing jeed mods" in `todo/change_scratchpad.md`.
  Done 2026-10-20: JEED lists the Brewin' drinks by itself (their effects are `FoodProperties`), and the pack's gear
  comes from the armor / tool registries.
- **Test:** VB-53.

### B7.6 Remove Vinery's mead from the port (Brewin's covers it)

- **Now:** the port registers `vinery:mead` (`ObjectRegistry.MEAD` / `MEAD_ITEM`). It is also listed in
  `StorageTypeRegistry` (twice), the client render layers (`VineryClientForge`) and the `WineEffects` defaults
  (Haste). Its barrel recipe comes from the 1.5.5 jar (`wine_fermentation/mead`).
- **Plan (port):**
  - Unregister the block and the item, and drop it from those lists and the effect defaults.
  - The upstream-pack layer leaves out every data file that names `vinery:mead`: the barrel recipe, its recipe
    advancement, and any tag or loot entry. That is the same mechanism that already leaves out 1.20+ content.
  - Check the `[villager]` trade lists for it too.
- No conversion to Brewin's mead; old bottles just disappear (no-conversions rule).
- **Test:** VB-54.

### B7.7 Banner: Health Boost II for 60 s (port)

- **Now:**
  - The Vinery banner gives Regeneration II within 8 blocks.
  - It re-applies a 3 s effect to every player in range every second.
  - `[banner]` has only `giveEffect` and `showTooltip`.
- **Plan:**
  - New `VineryConfig` keys under `[banner]`: `effect = "minecraft:health_boost"`, `amplifier = 1` (Health Boost II,
    +4 hearts), `radius = 8` and `durationSeconds = 60`.
  - Plus `refreshBelowSeconds = 10`: a player whose banner effect has 10 s or more left is skipped.
  - An unknown effect id falls back to Regeneration. The tooltip line is built from the config (new lang key in the
    port's own file).
- **Why the refresh threshold:**
  - **Performance:** the check stays once a second, but it is a lookup in the player's effect map. The effect
    update and its packet go out about once per 50 s per player instead of every second. The player scan around the
    banner (one AABB query per banner per second) is unchanged. It could also drop to every 5 s, since nothing
    expires in under 10 s.
  - **Correctness:** in 1.19.2, re-applying an effect that is already running calls `onEffectUpdated`, which removes
    and re-adds the attribute modifiers. Removing Health Boost clamps health to the base max, so every refresh would
    empty the extra hearts.
  - The threshold cuts that to once per cycle. The full fix is to extend the duration in place at the refresh
    (set the instance's duration and send the effect packet, with no remove and re-add). Do both.
- **Side effect:** with 60 s effects, the hearts stay up to 60 s after a player walks away from the banner.
- **Test:** VB-55.

### B7.8 Time: seasons, day length, wine months, calendar -> [time.md](time.md)

The time requests are planned in [time.md section 3.7](time.md#37-wine-months-and-the-calendar-alcoholmd-b78), on
top of the `[time]` master (3.1). In short:

| What | Value |
|---|---|
| Day length | about 45 min daylight + 15 min night, so one world day is 60 min |
| Sub-season | 8 world days (8 h) |
| Season | 3 sub-seasons, 24 world days, one real day |
| Year | 4 real days |
| Wine month | one sub-season |
| Wine level | +1 every 3 months (one season) |
| Dark aging | x2.5 |
| Sleeping | ages wine; wine runs on the same sun clock as the seasons |

Also:
- "Year" becomes "month" in every wine text.
- Time of day shows as "6:00 am" / "7:30 pm".
- The Vinery calendar is removed. The Serene Seasons calendar gets a season, day, year and time tooltip in its
  place.

This replaces B4.5's "sleeping doesn't age wine" (marked there).

### B7.9 Fermentation barrel -> "Cask" (port)

- **What:** every name that says Fermentation Barrel / Aging Barrel / barrel for Vinery's wine block becomes "Cask".
  - The block and item name.
  - The GUI title, and the JEI category (`rei.vinery.fermentation_barrel_category`).
  - Tooltips (`tooltip.vinery.fermentation_barrel.*`) and WTHIT lines.
  - The pack's own text: [vinery.md](vinery.md), this file's player-facing tests, `names.md` if it lists it.
- **How:** the same keys in the port's own `assets/vinery/lang/en_us.json` (upstream lang can't be edited).
  - Find every key by grepping the 1.5.5 and 1.3.10 jars' `en_us.json` and the port's `vinery_backport` lang for
    "arrel".
  - Not the dark-cherry chest barrel or the 1.3.10 decorative barrel, which are storage, not wine.
- **Ids stay** (`vinery:fermentation_barrel`). Display names only.
- **Test:** VB-57.

### B7.10 Apple wine: 2 to 3 bottles per block again (port + tag)

- **Why it broke:**
  - Placed bottles stack only if the item is in `#vinery:small_bottle` (`WineBottleBlock#canInsertStack`).
  - 1.5.5 moved apple wine to `#vinery:large_bottle` and made its item `BottleSize.BIG`.
  - The port's block still allows 3, but nothing can be added after the first.
- **Plan:**
  - Apple wine goes back to small: the port's own `data/vinery/tags/items/small_bottle.json` (`replace: false`) adds
    it, and the upstream-pack layer drops it from `large_bottle.json`.
  - Its item becomes `BottleSize.SMALL`, and its block count stays 3, as in 1.3.10.
- **Side effect:** it then fits the small wine racks, not the big ones.
- **Test:** VB-58.

### B7.11 Praetorian wine fits a wine rack again (port tag) (SETTLED 2026-10-18, section 10.8)

- **Why:**
  - Praetorian Wine (and King Danis Wine) are 1.3.10 keepers that 1.5.5 dropped (D2).
  - So they are in neither upstream bottle tag, and every rack checks those tags (big racks: `large_bottle`).
  - The port registers them as `BottleSize.BIG` with a 2-bottle block.
- **Plan:** the port's own `data/vinery/tags/items/large_bottle.json` (`replace: false`) adds both, so they fit the
  big racks. If you want them stacking 2 per placed block as in 1.3.10, they go in `small_bottle` instead and fit
  the small racks (B7.12 VQ3).
- **Gap check while there:** list every wine item in neither tag. Any that turn up get the same fix.
- **Test:** VB-59.

## B7.12 Crate merging

Audited 2026-10-19 with bottle sizes and racks: [storage_audit.md](storage_audit.md).

- merge crates and bags w/ other mods versions, ensure no crates and bags are duplicated during backport


### B7.14 Grape stomping yield: 12 grapes, 4 stomps, 4 bottles (port) (BUILT 2026-10-19)

- **Was:** 1.5.5 holds 6 grapes, 3 per bottle (2 juices), and takes 12 stomps (`grapevinePotStomps`).
- **Now:**
  - `GrapevinePotBlock.MAX_GRAPES` 12 (`STORAGE` 0 to 12); `GRAPES_PER_BOTTLE` stays 3, so 4 juices.
  - The half-full model stage shows from 6 grapes.
  - `grapevinePotStomps` default 4 in `VineryConfig`, and `config/vinery-common.toml` set to 4 (it matches the
    port's default, so there is no `configs_edited/` copy).
  - JEI page, WTHIT lines and `/vinery` pot command read the constants and the setting, so they follow.
- **Block states:** `STORAGE` 0 to 12 adds 378 pot states (441 to 819). The 1.5.5 blockstate is a single variant
  and the renderer reads only `STAGE`, so no model changes.
- **Old pots:** a pot already holding 6 grapes is at the full stage and can't take more; stomping it gives 2 juices.
- **Heavy armor:** at 4 stomps (40 points) the 10% bonus (11 per stomp) no longer saves a stomp. 34% or more would
  make it 3 stomps; left at 10% for now.
- **Test:** VB-10.

### B7.13 Questions for you

| # | Question | My default |
|---|---|---|
| VQ1 | Wine Aging page text colour | DECIDED 2026-10-18: dark grey `0x404040` |
| VQ2 | Cask: the 1.3.10 decorative `barrel` keeps its name? | Yes, only the wine block is renamed |
| VQ3 | Praetorian / King Danis: big racks (1 per placed block), or small racks with 2 per placed block as in 1.3.10? | DECIDED 2026-10-18 (10.8): small racks, shrunk to fit; King Danis removed |
| VQ4 | Time questions | [time.md](time.md) section 4 |

### B7.15 Next asks (scratchpad 2026-10-21)

- **Cellar mechanic:** audit how wine storage actually ages (cellar / sunny, cold / warm, 10.13) and write it up where
  players see it (guide book, tooltip or JEI). [TODO](todo/TODO.md) `alcohol/vineryport #22`.
- **JEI tab for grape planting types:** which grapes grow where. `alcohol/vineryport #23`.
- **Apple cider merge:** merge the apple ciders (Farmer's Delight, Farmer's Respite). Vinery's Apple Cider is a wine and
  alcoholic (section 11), the other two aren't, so the merge has to pick which one survives. `food/drinks #3`.

## B8. In-game test suite (drafted, run once built)

- [ ] **VB-01 Juice into cask:** 4 red grape juice in slot 0 -> gauge 100, 4 wine bottles in slot 4
- [ ] **VB-02 One juice type:** cask holding red juice, put white juice in slot 0 -> it stays in the slot
- [ ] **VB-03 Red wine:** 10 red units + sugar + wine bottle -> red wine after 6000 ticks, 10 units used
- [ ] **VB-04 No-bottle wine:** solaris (honey, sweet berries, 15 white units, no bottle) -> crafts without a bottle
- [ ] **VB-05 Output stacks:** two red wines in a row stack in slot 5 (same day)
- [ ] **VB-06 Empty juice:** sneak-right-click with an empty hand -> juice level 0, splash sound
- [ ] **VB-07 Cask hoppers:** juice and bottles from the top, ingredients from the side, output from below; nothing jams
- [ ] **VB-08 Apple press:** apples -> mash (600 t); mash + wine bottle -> apple juice (800 t); honeycomb block -> 4 honeycomb -> honey bottle
- [ ] **VB-09 Press is 2 tall:** breaking either half drops one press and the contents
- [ ] **VB-10 Pot stomping:** 12 red grapes (a 13th refused; half-full look from the 6th), 4 jumps -> 4 red juice, pot empty after; JEI page and WTHIT say 12 grapes / 4 stomps
- [ ] **VB-11 Pot one type:** red grape then white grape -> white refused
- [ ] **VB-12 Grapevine leaves:** plant red seeds on a stem on dirt -> leaves around it within 4800-9600 ticks
- [ ] **VB-13 Lattice:** place oak lattice on a wall, plant jungle seeds, grows; axe toggles support; shears prune
- [ ] **VB-14 Lattice connect:** 3 in a row -> left/middle/right models
- [ ] **VB-15 Taiga bush soil:** taiga bush with no podzol/coarse dirt/grass within 4 -> doesn't grow
- [ ] **VB-16 Fox:** fox eats ripe grapes from a bush
- [ ] **VB-17 Fruit leaves:** apple leaves fruit and drop 1-4 apples; cherry leaves sometimes drop a rotten cherry (D10)
- [ ] **VB-18 Stackable log:** light with flint and steel, cook 4 raw beef, refuel with a log, put out with a shovel
- [ ] **VB-19 Wine aging in a cellar:** wine in a rack with no sky light ages x2.5 (`/wine info`; was x1.5 before B7.8)
- [ ] ~~**VB-20 Aging pace:**~~ superseded 2026-10-18 by the sun clock in months (B7.8; time.md TI-18, TI-19). default settings -> `/wine time` shows about 28 h per year and 16 days per year; `/wine info` on a new wine says level up after 32 days
- [ ] **VB-21 Grape juice cures poison:** poisoned, drink juice -> poison gone, wine bottle back
- [ ] **VB-22 Double jump (clark wine):** second jump in the air on a dedicated server, no kick
- [ ] **VB-23 Walkers:** kelp cider walks on water, bolvar on lava
- [ ] **VB-24 Climbing, frosty, armor, health:** jo special climbs walls; eiswein freezes + armor; aegis +2 armor; stal +4 max health
- [ ] **VB-25 Party:** lilitu wine, hit a zombie -> firework at the target
- [ ] ~~**VB-26 Banner from the advancement**~~ retired 2026-10-18: advancements are removed (section 10.2); the banner is crafted now (VB-64)
- [ ] **VB-27 Winemaker set and charm:** full set + bone meal -> bone meal kept, 30% of uses damage all 4 pieces by 1; set + bonemeal charm -> exactly one refund
- [ ] **VB-28 JEI:** all five Vinery pages (cask, press mashing, press fermenting, grape stomping, wine aging) come from the port, in that order after Brewin's; none of patcherMod's `creativeengineering:vinery_*` pages
- [ ] **VB-29 Old world: ids kept:** a 1.3.10 world with cherry blocks, crates, wine_press, juice bottles, vinemaker armor, stove, sink, flower box -> all present under the same ids; nothing in the missing-mappings log except effects
- [ ] ~~**VB-30 Old world: storage:**~~ retired 2026-10-27 (old-world conversion removed, pack_hygiene.md finding 10). 1.3.10 wine racks, shelves and placed wine bottles keep their bottles
- [ ] ~~**VB-31 Old world: barrel and wines:**~~ retired 2026-10-27 (old-world conversion removed, pack_hygiene.md finding 10). 1.3.10 barrel with juice and bottles converts per W3; an old 2-year wine shows its age, not "brewed in the future"
- [ ] **VB-32 Old world: cross-mod cherry:** Every Compat / Dramatic Doors cherry blocks still there (D1)
- [ ] ~~**VB-33 Clock only on playtime:**~~ superseded 2026-10-18 (clock is WORLD_TIME, B7.8): dedicated server, note `/wine time`, log everyone off for 10 min, log back in -> clock moved by about 0
- [ ] ~~**VB-34 Today's pace measured:**~~ superseded 2026-10-18 (B7.8): play through one full world day with `dayUnit = WORLD`, `worldDayMinutes = 0` -> the measured day length is about 106 min, close to the 28.2 h-per-year default
- [ ] ~~**VB-35 Sleep and /time don't age:**~~ reversed 2026-10-18: sleeping ages wine (time.md TI-17): `/wine info` on a wine, sleep through a night and `/time add 24000` -> the age is unchanged
- [ ] ~~**VB-36 Rescale:**~~ superseded 2026-10-18 (keys are months now, B7.8): set `playtimeHoursPerYear` 5, `daysPerYear` 15, reload -> an existing wine's age and level change at once; `/wine clock add 5h` -> exactly 1 more year
- [ ] **VB-37 Cellar:** two fresh wines, one in a rack in a dark cellar, one in a rack under sky; after the same time the cellar one is about 2.5x older
- [ ] **VB-38 Old and new blocks side by side:** place old chair / table / crate / barrel / wine_rack_1 next to their new counterparts -> both work; note any look-alikes to hide
- [ ] **VB-39 WTHIT lines:** look at a pot mid-stomp, a cask mid-ferment, a press, a rack of wines in a cellar -> lines per B4.6
- [ ] **VB-40 Winemaker set settings:** `damageMode = NONE` -> 50 bone meal uses, no durability lost; `ONE_PIECE` + `damageAmount 3` -> one piece loses 3 per damaging use
- [ ] **VB-41 Upstream jar check:** remove `vinery_upstream/letsdo-vinery-fabric-1.5.5.jar` -> client and server both stop with the file name and download page; a renamed or different jar is refused the same way
- [ ] **VB-42 Missing art sweep:** first boot, grep the log for missing models and textures under `vinery:` and `foundation:` -> only the 21 known upstream dangling references (B4.8)
- [ ] **VB-43 Donor layering:** stove, kitchen sink and flower box show their 1.3.10 art; `cherry_planks` shows the 1.5.5 dark-cherry art; the lang says "Dark Cherry"
- [ ] **VB-44 Overrides still win:** the pack's `kubejs/data/vinery/worldgen/configured_feature/*` grape-bush configs are the ones in use (`/dumpall` or a locate), not upstream's
- [ ] **VB-45 Boots:** client and dedicated server start with the port and both mod ids (`vinery`, `foundation`) in the mod list; log has `[vinery-upstream] N data files ...` and no `vinery` / `foundation` errors
- [ ] **VB-46 Plains advancement:** `fruits_of_the_field` is granted on entering plains, not in other biomes
- [ ] **VB-47 WTHIT config:** the WTHIT config has a Vinery page with 5 toggles; turning one off hides only its lines; a fresh storage pot in a structure shows nothing and still has its loot when opened
- [ ] **VB-48 JEI juice slot (B7.1):** a wine's Cask page shows its juice bottle in the juice slot with the needed count; looking up the uses of red grape juice lists the red wines
- [ ] **VB-49 Full-bar hover (B7.1):** a quarter-full cask, hover the empty top of the gauge -> the juice tooltip; the same on the JEI page
- [ ] **VB-50 Apple press slots (B7.2):** by hand, slot 1 (left) takes apple mash and honeycomb only, slot 2 wine bottles and glass bottles only; a hopper feeds glass bottles into slot 2; shift-click puts mash in slot 1; honeycomb + glass bottle -> honey bottle, and the JEI page shows the glass bottle
- [ ] **VB-51 Merged effects (B7.3):** Stal Wine gives Health Boost; `/effect give @s vinery:luck_effect` fails (no such effect)
- [ ] ~~**VB-52 Effect fallbacks**~~ pinned with B7.4 (future plan)
- [ ] **VB-53 JEED providers (B7.5):** the JEI page of a wine's effect lists that wine as a provider; change the config entry, `/reload` -> the provider moves
- [ ] **VB-54 Mead gone (B7.6):** no `vinery:mead` in JEI, the creative tab or `/give`; no cask recipe for it; the log has no errors about it
- [ ] **VB-55 Banner Health Boost II (B7.7):** stand by a Vinery banner at full health -> Health Boost II for 60 s, 4 extra hearts that stay full while you stand there for 2+ min; the effect timer only jumps back up when it falls under 10 s; walk away -> it runs out within 60 s
- [ ] **VB-56 Months everywhere (B7.8, [time.md](time.md) 3.7):** wine tooltip, `/wine info`, `/wine time`, the JEI Wine Aging page, WTHIT and the config screen all say months, none say years
- [ ] **VB-57 Cask name (B7.9):** the block, its item, its GUI title, the JEI category, tooltips and WTHIT all say Cask; the decorative barrel and the dark-cherry chest barrel keep their names
- [ ] **VB-58 Apple wine stacks (B7.10):** right-click a placed apple wine with two more -> 3 bottles in one block; it fits a small wine rack
- [ ] **VB-59 Praetorian in a rack (10.8, sizes 10.12):** Praetorian Wine goes into a 4x rack, the wine box and the single-bottle rack, and a 9x rack refuses it (medium since 10.12); lying in a 4x rack or the box it keeps its own model, shrunk inside its slot (no slim one, 10.14); standing in the single-bottle rack it looks like Stal Wine; two stack in one placed block
- [ ] **VB-66 King Danis gone (10.8):** no `vinery:king_danis_wine` in JEI, the creative tab or `/give`; only one "Miss Lilitus Wine"; no log errors about it
- [ ] **VB-67 Large wines stack (10.9):** sneak-place Eiswein, then use another Eiswein on it -> 2 in the block, a third is refused; Lilitu takes no second bottle; Red Wine on a placed Eiswein -> 2, then refused
- [ ] **VB-68 Removed blocks gone (10.10):** no `vinery:*_crate`, `barrel`, `stove`, `wood_fired_oven`, `kitchen_sink`, `cherry_chair` / `_table` / `_big_table` / `_shelf` or wood `*_lattice` in JEI, the creative tab or `/give`; the 10 bags, `cherry_barrel`, `cherry_cabinet`, `cherry_drawer`, the 1.3.10 chair / table / big table / shelf and `grapevine_lattice` are there; no log errors naming them
- [ ] **VB-69 Cherry names (10.10):** JEI search "dark cherry" finds nothing; planks, log, barrel, cabinet, drawer, racks, boats and the Dramatic Doors short / tall door say "Cherry"; the creative tab side label says "Cherry Wood"
- [ ] **VB-70 Jungle grapes on the grapevine lattice (10.10):** jungle red / white grape seeds plant on `grapevine_lattice`, grow, show the WTHIT stem line and the Growth % line, and harvest; a chest packed by the loot rules holds an apple / cherry / grape bag, not a crate
- [ ] **VB-71 Racks merged (10.11):** no `vinery:wine_rack_1/2/3/5` in JEI or `/give`; "Big / Small Cherry Wine Rack" are the 1.5.5 cherry racks; oak and cherry 4x / 9x racks take wines by size (10.12, see VB-73); juice bottles are refused
- [ ] **VB-72 Cherry mid rack window (10.11):** a closed cherry single-bottle rack shows its bottle through two glass panes (same look as the other woods' windows); opening and closing still animates; the Cherry Cabinet's door stays solid
- [x] **VB-73 Rack fit by size (10.12, 10.14):** PASSED 2026-10-20 ("all vinery bottle shape and case size"). Was: 9x rack takes Red, refuses Stal, Sun-kissed, Eiswein and Cristel; 4x rack and wine box take Red and Eiswein, refuse Cristel; the single-bottle rack takes all three; juice bottles go nowhere
- [x] **VB-74 Shrink to slot (10.12):** PASSED 2026-10-20 ("all vinery bottle shape and case size"). Was: Praetorian (no box look) lies inside its 4x slot and the wine box without poking out the back or front; Red Wine draws at full size; the box-look wines are VB-82 / VB-83
- [ ] **VB-75 One place to change (10.12):** set Red Wine's limit to 1 in `ObjectRegistry`, build -> Red stacks 1 per block, goes only in the single-bottle rack, and `/kubejs` or JEI shows it under `#vinery:large_bottle`; set it back
- [ ] **VB-76 Storage labels (10.13):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-05).* wine put in a rack in daylight -> WTHIT "sunny x1.0"; ringed by snow blocks -> "cold sunny x2.0"; next to a lit furnace -> "warm sunny x0.75"; underground with nothing -> "cellar x2.0"; underground with ice -> "cold cellar x4.0"; underground with a lit furnace -> "warm cellar x1.5"
- [ ] **VB-77 Wine tooltip + command (10.13):** the stored wine's tooltip says "In storage: <label> x<rate>" as in VB-76; `/wine storage` says "Storage here: <label>, wine ages at <percent>%" (green above 100, gold below)
- [ ] **VB-78 Rack tooltips (10.13):** every 9x / 4x / single-bottle rack and the wine box -> "Wine aging is sped up by dark and cold environments, slowed by warm ones"; JEI Wine Aging page shows the cold and warm lines
- [ ] **VB-80 Warm slows (10.13):** a fresh wine racked by a lit furnace in daylight -> its tooltip's next-level estimate is 4/3 of an unracked one; `/wine info` on it over time -> the age only goes up
- [ ] **VB-79 Old stored wine (10.13):** a wine racked before the update in a cellar still shows "cellar x2.5" (its stored rate) until taken out and put back
- [x] **VB-81 New sizes (10.14):** PASSED 2026-10-20 ("all vinery bottle shape and case size"). Was: Stal and Sun-kissed stack 2 per placed block and are refused by a 9x rack; Magnetic stacks 2 per placed block and goes into a 4x rack and the wine box
- [x] **VB-82 Box looks in the 4x rack (10.17, was 10.14's slim bottles):** PASSED 2026-10-20 ("all vinery bottle shape and case size"). Was: Stal and Strad lie as their own bottles, a little smaller; Sun-kissed, Aegis, Apple Cider, Chenet, Mellohi, Creeper's Crush, Eiswein and Magnetic lie as Strad-shaped bottles in their own colours, the same size; four fit without overlapping; Praetorian and Red Wine look as before; no missing-texture (purple/black) bottles; the same wines placed on the ground or in the single-bottle rack keep their own models
- [x] **VB-83 Wine box (10.17):** PASSED 2026-10-20 ("all vinery bottle shape and case size", with the 10.18 recolours). Was: the same 10 wines show the same looks as VB-82 at full box size; Magnetic's bottle is inverted with a salmon outline (no white, 10.18); F3+T (resource reload) keeps all of it
- [ ] **VB-84 Wine size lines (10.15, reworded 10.18):** hover Red Wine -> "Small bottle: 3 per placed block" and "Fits in Single, Small, and Big Wine Rack, and Wine Box"; Eiswein -> "Medium bottle: 2 per placed block", "Fits in Single and Small Wine Rack, and Wine Box"; Cristel -> "Large bottle: 1 per placed block", "Fits in Single Wine Rack"; each matches what VB-73 shows the racks accept. First run 2026-10-20: TWEAK (the old "Small Wine Bottle Storage, the Wine Rack..." wording was clunky)
- [ ] **VB-85 Capacity lines (10.15):** an oak 9x rack says "Holds 9 wines (small only)", the 4x rack "Holds 4 wines (small or medium)", the single-bottle rack "Holds 1 wine (any size)", the Wine Box "Holds 1 wine (small or medium)" and the sneak line; the aging hint still follows
- [ ] **VB-86 Vinery block lines (10.15):** Grapevine Pot, Apple Press, Cask, Grapevine Stem, Grapevine Lattice, apple / cherry leaves and saplings, Stackable Log and Chorus Wine show their gray (Chorus: light purple) lines, no "TODO" anywhere; `logs/latest.log` has no tooltip errors
- [x] **VB-87 Stained Glass (10.15):** PASSED 2026-10-20 ("stained glass rename"). Was: `vinery:window_block` is "Stained Glass", `vinery:window` "Stained Glass Pane" (JEI, hover, WTHIT); JEI shows 6 Stained Glass -> 16 panes beside upstream's three recipes
- [x] **VB-88 Wine box with an item in hand (10.15):** PASSED 2026-10-20 ("vinery qol"). Was: holding a torch, sneak right-click a closed Wine Box -> it opens; again -> it closes; same holding a wine; with an empty main hand and a shield in the off hand -> it still toggles; not sneaking with a wine at an open box -> the wine goes in
- [x] **VB-89 Place wine while flying (10.15):** PASSED 2026-10-20 ("vinery qol"). Was: in creative, fly, hold sneak (descending) and right-click a block with a wine -> the wine is placed; not sneaking -> it is drunk; on the ground sneak-placing works as before
- [ ] **VB-90 Wine texture dump (10.17):** `/dumpwinetextures` -> chat says how many were written; `kubejs/exported/wine_textures/upstream/block/wine/` holds every wine's textures (Praetorian's too), `generated/block/wine_box/` 8 PNGs (Magnetic's the inverted one), `index.tsv` one line per wine
- [x] **VB-91 Recolour tweaks (10.18):** PASSED 2026-10-20 (in the jar that was tested, reported with "all vinery bottle shape and case size"). Was: in the wine box and the 4x rack, Sun-kissed and Aegis are inverted (dark glass, light outline) like Magnetic; Magnetic has no white; Mellohi, Apple Cider and Creeper's Crush show no brown
- [ ] **VB-92 Rack names (10.18):** JEI search "wine rack": every wood has "Single <Wood> Wine Rack" (1 bottle), "Small <Wood> Wine Rack" (4x) and "Big <Wood> Wine Rack" (9x), cherry included; no "Wine Bottle Storage" or "Dark Cherry" left; WTHIT shows the same names
- [ ] **VB-93 Cask automation (10.19):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-07).* a hopper above with red grape juice, one beside with a recipe's ingredients in 128s -> the first ingredient fills one slot only and the rest waits in the hopper; add an ingredient of another recipe to the side hopper -> it stays in the hopper; a white juice bottle with red juice in the cask stays in the hopper; the wine comes out the bottom
- [x] **VB-95 Houses served (13):** `logs/latest.log` after a world load has four `[vinery-houses] vinery:vinery_<biome> added to minecraft:village/<biome>/houses at weight 2 (... pool average)` lines and no `[vinery-houses]` warning; `kubejs/generation_scripts/worldgen/pool_config.json` lists the four pieces under their pools (current 3 / 3.5 / 3 / 3); `pool_tag_report.md` shows them retuned. *PASSED on the scratch server 2026-10-20 (weight 2 lines; second boot: report rows 2 -> 9 / 11 "current 3 / 3.5", shares 2.2 to 2.9 %)*
- [x] **VB-96 Houses load (13):** `/place template vinery:vinery_plains` (and taiga, savanna, desert) places the house with a wine press, cask, grapevine stems, cherry furniture, a Big Wine Rack holding wines and, in the desert one, red and white grape bags and a chain stub under the eave where the sign hung; nothing is missing or air where the press, crates or sign were; *PASSED scratch 2026-10-20 (3 boots): all four placed and counted (plains: 2 wine press blocks, cask, 4 stems, cabinet, big rack, Noir wine, storage pot, 4 windows ...; desert: 4 red + 2 white grape bags, Cherry wine, rack, drawer, basket, chair, table, 1 chain); a wall sign and then a lantern popped, the chain stays*; `/place jigsaw minecraft:village/plains/houses minecraft:building_entrance 1 ~ ~ ~` can place a Vinery house among the vanilla ones
- [ ] **VB-97 Houses in villages (13):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-08, 2 of 8 placed villages have a winery).* `/locate structure minecraft:village_plains` a few times and walk the villages: about one village in two or three has a winery house, which fits the street grid (entrance on the road) and sits on the ground like its neighbours; the cask gives an unemployed villager the Winemaker job
- [ ] **VB-98 Houses config (13):** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-14 / SQ-OM-15, weight 20 read; `enabled = false`: no `[vinery-houses]` line, template unknown).* `[village_houses] weight = 20` + restart -> the log line says weight 20 (configured) and the house turns up in most villages; `enabled = false` -> no log lines and `/place template vinery:vinery_plains` says the template is missing
- [ ] **VB-94 Press automation (10.19):** *Logic: scratch server PASSED 2026-10-22 (SQ-OM-07).* hopper beside the press with apples and wine bottles -> apple mash and cider as before; put honeycomb in that hopper -> it waits until the apples are done and the output emptied; glass bottles wait while wine bottles' recipe is queued; by hand honeycomb still goes in
- [ ] **VB-60 Wine Aging page text (B7.1):** every line stays inside the grey page, wrapped, in dark text without shadow
- [ ] **VB-61 JEI transfer (B7.1):** with juice bottles, ingredients and a wine bottle in your inventory, click [+] on a Cask page -> juice in slot 0, ingredients in 1-3, the bottle in 4; without the juice bottles the [+] is red
- [ ] **VB-62 Calendar gone (B7.8):** no `vinery:calendar` in JEI, the creative tab or `/give`; the log has no recipe or loot errors about it
- [ ] **VB-63 JEED providers move (B7.5):** after editing `[wine] effects` and `/reload`, the JEI page of the old effect no longer lists the wine

## B9. History

- 2026-10-14: migration analysis of Vinery 1.3.10 (Forge) -> 1.5.5 (Fabric 1.21.1) from decompiles of both jars and
  of Foundation 1.0.1. 75 issues in B3, 12 decisions open in B5, VB-01 to VB-32 drafted.
  Found:
  - The 65 cross-mod cherry items that depend on the wood name (W11).
  - The dead village structures (Q5).
  - The orphaned rotten cherry (Q1).
  - The license is "Custom", not MIT (I1).
  - The pack's barrel stacking mixin is now redundant (I2).
- 2026-10-14: added the upstream license text (B3.6): code MIT, resources All Rights Reserved. Shipping upstream
  assets or data needs permission. Added D13 (ask Satisfy, read the player's own copy of the upstream jar, write our own
  resources, or behaviour-only mixins); 13 decisions open.
- 2026-10-14: decided:
  - D1: keep `cherry_*`.
  - D2: keep every 1.3.10 block and id and add the new ones. Renamed things keep their old id with the new
    behaviour; replaced things exist side by side. B4.1 rewritten.
  - D6: WTHIT lines (B4.6).
  - D11: winemaker set damage set in config, including none (B4.7).
  - Wine aging is redesigned onto a playtime vintage clock with a configurable real-time scale, a `WORLD` day unit for
    Better Days, and ages that re-scale when the settings change (B4.5). Lengths are unchanged for now
    (16 days per year, 2 years per level, about 28.2 h per year).
  - Tests VB-33 to VB-40 added; VB-20, VB-27 and VB-29 updated.
- 2026-10-18: Satisfy refused redistribution of assets and features (B3.6). The user chose D13 B: ship the
  unmodified upstream jar and have the port read it. Feasibility checked (B4.8):
  - FML 43 rejects the Fabric jar in `mods/`, so both upstream jars go in their own folder.
  - 1.5.5's 418 models need nothing from 1.21; their only outside parents are Foundation templates in the nested jar.
  - 31 kept blocks need the 1.3.10 jar as a second donor; 20 `cherry_*` need an alias to `dark_cherry_*`.
  - Data needs F1 to F6 in memory.
  - Foundation's license is the same split as Vinery's.
  - Modrinth hosts the same 1.5.5 jar and its sources.
  Added D14 (how the jars reach each machine), B6 checks, VB-41 to VB-44.
- 2026-10-18: implemented. D14 decided (upstream jars in the deploy payload for now, downloaded from source on
  publish). User: wrap 1.20+ worldgen into 1.19.2's types, no worldgen backports (cherry foliage placer -> fancy).
  The port builds (B4.9). Pack-side changes done (B4.4 outcome table). WTHIT lines built in the port (B4.6). Deploy
  category and mod_sides done. Review fixes without a boot: missing `foundation` mods.toml entry (would not start),
  advancement biome predicate. The jar swap in `mods/` waits for the game to be closed. Tests VB-45 to VB-47 added;
  VB-28 updated.
- 2026-10-18: planned your "changes to make" list (B7): JEI juice slot and full-bar hover, recipe-driven apple press
  slots, luck / resistance / health effects merged into vanilla, a fallback syntax for wine effects plus a candidates
  tool, JEED providers from the live config, Vinery mead retired into Brewin's, and a configurable banner effect
  (Health Boost, with the re-apply gotcha). The time items (calendar, months, sleep ages wine, x2.5 dark) moved to
  [time.md](time.md) 3.7; B4.5 marked. VB-48 to VB-56 added.
- 2026-10-18: your second list, folded into B7 and section 8:
  - No conversions between versions or between the two ports. Vinery mead is removed from the port; AL-65 retired.
  - Effect reshuffle pinned for later, with two effects per wine possible (B7.4, VB-52 pinned).
  - New items: Cask rename (B7.9), apple wine stacking (B7.10), Praetorian / King Danis in racks (B7.11), JEI Wine
    Aging text wrapped in dark text (B7.1). B7.2 now says the left press slot accepts nothing today.
  - Banner: Health Boost II, 60 s, refreshed under 10 s.
  - Keg pipe rule made a setting with a tooltip line; ham and cheese table confirmed (Q6, Q7 decided).
  - Time values settled in [time.md](time.md) 3.7. VB-57 to VB-60 added.
- 2026-10-18: B7 built (not tested in game), apart from B7.4 (pinned) and B7.11 (on hold). Port `-02` in `mods/`,
  `server_mods/` regenerated, `config/vinery-common.toml` moved to the new keys. Built with B7.8: the port side of
  [time.md](time.md) 3.7 (months, sun clock with a DAYTIME day, x2.5 dark, calendar removed). Changes from the plan:
  - Cask wording and the apple-wine tag move go through the upstream-pack layer, not files at upstream's paths.
  - The press's slot 1 takes any fermenting input.
  - Honey's glass bottle needed a per-recipe `bottle` ingredient.
  Found and fixed:
  - Dropping every file that names mead would have dropped `wine_somelier`, the banner advancement. Mead is taken
    out of its criteria instead.
  - The JEI Cask transfer paired slots in the wrong order.
  VB-20 and VB-33 to VB-36 superseded by the sun clock. VB-61 to VB-63 added.
- 2026-10-18 evening: the patcherMod half of B7.8 built ([time.md](time.md) 3.8, patcherMod 1.0.8 in `mods/`).
  `i.mead` and `i.calendar` removed from `item_dict.js`, and `mead` from the generated placeable, stack-size and
  food-type lists. Every other port item was already mapped.
- 2026-10-19: `-01` (with the grass slab hotfix) had gone out in pack 1.0.7, DEPLOYED 2026-10-18 19:43. `-02` (B7,
  section 10) STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) VB). On the live world it removes `vinery:mead`,
  `vinery:king_danis_wine` and `vinery:calendar` (all present in `-01`). Next build bumped to `-03`.
- 2026-10-19: `-02` DEPLOYED with pack 1.0.8 (10:20, server + clients). VB-01 to VB-66 pending.
- 2026-10-19: B7.14 built into `-03`: grape pot 12 grapes, 4 stomps, 4 bottles (TODO alcohol-decisions #4 option A).
  VB-10 rewritten. The jar is in `build/libs/`; the game had `-03` open, so it isn't in `mods/` yet.
- 2026-10-19: 10.13 built into `-03` (cold / cellar / cold cellar storage, rack tooltips, network version 2) with
  patcherMod 1.0.10 (`[time.wine.boosts]` cellar 2.0, cold 2.0). VB-76 to VB-79 added. Same day: warm x0.75 and the
  sunny x1.0 label added; VB-76 to VB-78 rewritten, VB-80 added. `build/libs/` only: the game
  was running.
- 2026-10-19: 10.14 built into `-03` (Stal, Sun-kissed, Magnetic medium; slim 4x rack / wine box models; Magnetic box
  look in Strad's shape). VB-81 to VB-83 added. The recolour then moved in memory (`SlimBottles`, no upstream-derived
  art in the jar). `build/libs/` only: the game had `-03` open.
- 2026-10-19: pack 1.0.9 DEPLOYED (15:54) with `-02` unchanged. Late: `-03` (B7.14, 10.9 to 10.14, in-memory slim
  bottles) and patcherMod 1.0.10 in the local `mods/` (22:27); Brewin' `-03` not yet, so VB-76 to VB-80's cold and warm
  labels need it installed first. Statuses in B0, section 10, B7 and the older rack tests (VB-59, VB-71, VB-73,
  VB-74) brought in line with 10.12 to 10.14.
- 2026-10-19 late: 10.15 built into `-03` (TODO `alcohol/vineryport #16` to `#18`): wine size and rack capacity
  lines, Vinery block lines in `tooltips.js`, "Stained Glass" names and the 6 -> 16 pane recipe, the wine box toggles
  with an item in hand, wines place while flying. VB-84 to VB-89 added. `build/libs/` only: the game had `-03` open.
- 2026-10-19 late: 10.17 built into `-03` (TODO `alcohol/vineryport #19`, `#20`): slim bottles replaced by the
  Strad / Stal box shape (smaller in the 4x rack), Magnetic's recolour inverted, `/dumpwinetextures`. `SlimBottles`
  renamed `BottleLooks`. VB-82, VB-83 rewritten, VB-90 added. `build/libs/` only: the game had `-03` open.
- 2026-10-20: test round on the local `-03` (10.18): VB-73, VB-74, VB-81 to VB-83, VB-87 to VB-89 and the new VB-91
  (recolour tweaks: Sun-kissed and Aegis inverted, brown / white left out of four palettes) PASSED. Tweaks built the
  same day: racks renamed Single / Small / Big Wine Rack, the wines' "Fits in" lines reworded (VB-84), VB-92 added;
  Brewin's heat / cold line split (AL-77). Vinery and Brewin' `-03` installed in the local `mods/`.
- 2026-10-20: 10.19 built: the press and cask take from automation only what they can finish with what they hold
  (cask ingredient overflow, mixed recipes, other-type juice; press bottle / input / output mismatches). VB-93, VB-94
  added; VN-05 / VN-08 retired. `-03` installed in the local `mods/`.
- 2026-10-22 (second bulk run): SQ-OM-05 (wine storage kinds and rates), SQ-OM-07 (cask and press hoppers), SQ-OM-08
  (2 of 8 placed villages with a winery), SQ-OM-14 and SQ-OM-15 (weight 20; off: no `[vinery-houses]` line, template
  unknown) PASS on the scratch server; logic and server halves of VB-76, VB-93, VB-94, VB-97, VB-98 recorded.
- 2026-10-28: port `-05`: shears on a grass slab clear its new `fertile` state and stop it spreading (pack
  addition, [wthit_lines.md](wthit_lines.md) 2.10, WL-34 to WL-36). Installed in `mods/`.
- 2026-10-30 / 31: port `-06`: grape bushes, vines, pale stems, lattice stems and fruit leaves fire Forge's crop-grow
  event, so the pack's crop seasons apply ([crops.md](crops.md) 2.12.1, TODO `farming/crops #5`); wine effect entries
  take several effects (`wine|a+b+c|level|ticks`), Jellie Wine gives Regeneration, Absorption, Resistance and Fire
  Resistance and `vinery:jellie` is unregistered ([effects.md](effects.md) 2.7, EF-29); Double Jump keeps the higher of
  its power and Jump Boost's (TODO `potions/effects #14`). Installed in `mods/` 2026-10-31 00:41, not yet booted.

## Owner notes (scratchpad 2026-11-07)

- Stomping: expand the heavy armour list; stomping moves to the H&H stomping basin. Row `alcohol/vineryport #29`.
