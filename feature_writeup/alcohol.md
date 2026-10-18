# Alcohol: Brewin' and Chewin' and Vinery backports

Two backports live in this file:

- **Part A (sections 0 to 8, tests `AL-`): Brewin' and Chewin'.** The pack ships 1.19-2.0; the plan is a full port of
  4.5.0+1.21.1 as "Backported Brewin".
- **Part B (sections B0 to B8, tests `VB-`): Let's Do Vinery.** The pack ships Forge 1.3.10; the plan is a port of Fabric
  1.5.5 (Minecraft 1.21.1) as "Backported Vinery", built 2026-10-18 (not booted yet). It starts after Part A's History.

# Part A: Brewin' and Chewin' (Backported Brewin)

This pack ships Brewin' and Chewin' 1.19-2.0 (Forge 1.19.2). The plan is a **full port** of 4.5.0+1.21.1, built as our
own Forge 1.19.2 jar ("Backported Brewin"). Where the two versions differ, the port follows 4.5.0 unless a line below
says otherwise. This part holds the gap analysis (sections 2.1 to 2.6), the port plan (section 3) and the decisions
(section 4). Both versions are MIT (Umpaz).

## 0. At a glance

| Part | Status |
|---|---|
| Gap analysis 2.0 -> 4.5.0 | written 2026-10-14 from decompiles of both jars |
| Base fluid choice: honey / milk (4.1) | DECIDED 2026-10-14: the pack's `create:honey` and `minecraft:milk` |
| Kombucha base (4.5) | DECIDED 2026-10-14: `create_central_kitchen:green_tea` |
| Heat / cold block audit (2.7) | written 2026-10-14; tag additions proposed, not applied |
| Port (`development/backportedBrewin`) | BUILT 2026-10-18: everything compiles and `backportedBrewin-4.5.0-ce-19.2-01.jar` builds; offline data check passes (6). Never booted (no scratch server yet, by request); not in `mods/`. Gaps and port decisions: 3.5 |
| Pack-side changes (3.4) | not started; they go in together with the jar (they would break the 2.0 jar) |
| In-game tests | AL-01 to AL-48 drafted (7), none run |

- **Scope:** everything in 4.5.0 except EMI support, which this pack doesn't have.
- **Deviations from 4.5.0, by your choice:**
  - Chat, sign and name-tag scrambling happens only on clients, with client settings (4.3).
  - Dread Nog takes a poison potion in place of the ominous bottle, which 1.19.2 doesn't have (4.4).
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
| R7 | Remove `satisfaction` effect | Remap the old id (3.3) |
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

**Apply:**
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
   - Remaps (3.3).
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
11. **Old-world conversion** (3.3).

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

### 3.3 Old worlds

| What | Conversion |
|---|---|
| Item / block `item_coaster` | remap to `coaster`; its single stored item goes to slot 0 with `size` 1 |
| Item `scarlet_pierogies` | remap to `scarlet_pierogi` |
| Effect `satisfaction` | remap to `farmersdelight:nourishment` (the closest effect, so running instances don't vanish with an error) |
| Placed kegs (2.0 NBT: 9-slot `Inventory`, drink in slot 5) | on load: the drink stack of N becomes 250 x N mB of that drink's fluid (up to the capacity; any extra drops as items). Slots 0 to 3 stay. The old fluid item (slot 4) and slots 6 to 8 go to the container / output slots, or drop if those are full |
| Keg items carrying 2.0 `BlockEntityTag` | the same conversion when the item is placed, and in the item tooltip |
| Ripening cheese wheels | unchanged (same block, same `age` property, 10% per random tick in both versions) |

### 3.4 Pack-side changes

| File | Change |
|---|---|
| [../kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) | Rename `i.item_coaster` -> `i.coaster` and `i.scarlet_pierogies` -> `i.scarlet_pierogi`. Add `heating_cask`, `ice_crate`, the 3 jams. Drop `i.horror_lasagna_meat` / `i.cooked_horror_lasagna_meat` (the tags are gone) |
| [../kubejs/startup_scripts/_lib/fluid_dict.js](../kubejs/startup_scripts/_lib/fluid_dict.js) | Add the 16 drink fluids and the 2 cheese fluids, so scripts never spell out the ids |
| [../kubejs/server_scripts/cooking/cooking.js](../kubejs/server_scripts/cooking/cooking.js) | Lines 125 and 580-582: the remove targets `brewinandchewin:cooking/scarlet_pierogi`. Rebuild the pack's pierogi recipes against the new id. Lines 409-435: drop the horror lasagna meat tag keys |
| [../kubejs/server_scripts/tags/cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js) | Line 23: the `cooked_horror_lasagna_meats` tag no longer exists upstream; keep it as a pack tag or drop it |
| [../kubejs/generation_scripts/food/can_types.json](../kubejs/generation_scripts/food/can_types.json) | Line 3100: a can uses `brewinandchewin:satisfaction`. Switch it to Nourishment and rerun `gen_can_types.py` |
| [../kubejs/assets/creativeengineering/lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json) | JEED descriptions: drop Satisfaction, write Tipsy / Sweet Heart / Intoxication / Raging from 2.4 |
| `config/ftbquests/quests/chapters/progression.snbt` (line 2111) | `scarlet_pierogies` -> `scarlet_pierogi` |
| `config/jei/recipe-category-sort-order.ini` + `configs_edited/root_config/jei/` | Replace the 3 old category ids with the new ones (configs_edited backup rule) |
| [../tools/gen_stack_sizes.py](../tools/gen_stack_sizes.py), [../tools/food_catalog.py](../tools/food_catalog.py), `food_types.json` | Add the jams to the right food types; the stack policy covers the new blocks |
| Compost framework | Check that the 4.5.0 compost values (O2) don't fight `global.compost` |
| BlockRunner | Speed tiers for `heating_cask`, `ice_crate`, `coaster` (blocktags.js) |
| Create spout / drain for tankards (optional, not in 4.5.0) | Through `global.NCH` createFilling / createEmptying: tankard + 250 mB drink <-> drink. Without it, kegs are the only way to bottle |
| Deploy | Remove the old jar from `mods/` and `server_mods/`, update mod_sides.md, regenerate the pack manifest, `/dumpall` refresh |

### 3.5 Port status (2026-10-18)

Built from the NeoForge decompile, class for class where 1.19.2 allows (133 files, about 10.5k lines). Package layout
follows upstream: `common/` and `client/` as 4.5.0, `forge/` in place of `neoforge/`, `integration/jei/`.

**Done:** every registry (blocks, items, 18 tank-only fluids + Forge fluid types, effects, block entities, menu,
particles, recipe types and serializers, loot functions, slicing loot modifier); the config; the keg (tank, pouring,
temperature, 2.0 keg conversion), keg screen, keg recipe book (items), keg item tooltip; Tipsy numbed hearts and cardiac
arrest, Raging stacks, Intoxication (saturation block, undead immunity, HUD bar, AppleSkin preview), Sweet Heart, the
milk-bottle low-priority rule; camera spin and mouse sway; chat, `/msg`, sign and name-tag scrambling (client side,
4.3); coaster renderer; particles; JEI (Fermenting page with pouring, Cheese aging page, clickable tank, transfer
button); old-world remaps (`item_coaster`, `scarlet_pierogies`, `satisfaction`, 2.0 coaster and keg contents); all
assets and data through `tools/port_resources.py`.

**Not ported yet (each works in a reduced form):**

| Upstream | What the port does instead |
|---|---|
| `KegStackedContents` / `KegPlaceRecipe`: the recipe book also fills or empties the tank from fluid containers in the inventory | the recipe book places the item ingredients only (vanilla `ServerPlaceRecipe`) |
| JEI `FermentingTransfer` + `JEITransferKegRecipeServerboundPacket` (about 1,100 lines): the same fluid handling for the JEI `+` button | JEI's basic handler, items only (the fluid slot is filtered out) |
| AppleSkin hunger-bar wobble while Intoxicated (mixin into AppleSkin's offsets) | AppleSkin only previews no saturation |
| Tipsy camera: a tiny positional drift on top of the spin | spin only |

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
- The 2.0 coaster block entity id is rewritten on load (`BlockEntityLegacyIdMixin`): block entity ids aren't in Forge's
  registry snapshot, so a remap can't reach them.
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

### 4.3 Scrambling stays on clients (your call; differs from 4.5.0)

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

### 4.4 Dread Nog: poison potion in place of the ominous bottle (your call)

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
- [ ] **AL-35 Old world: coaster and pierogi:** a 2.0 world with an item coaster and scarlet pierogies -> coaster block holds its item, pierogi item present
- [ ] **AL-36 Old world: keg:** a 2.0 keg holding 3 beers -> 750 mB beer in the tank
- [ ] **AL-37 CCK mixin:** boot log shows only the CCK warning, no crash
- [ ] **AL-38 JEI:** Fermenting page shows ferment + pour; Cheese aging page; transfer button fills the keg
- [ ] **AL-39 Satisfaction remap:** a 2.0 save with Satisfaction running -> loads with Nourishment, no error
- [ ] **AL-40 Dedicated server:** boots with the port, client joins, no RecipeBookType or registry desync kick
- [ ] **AL-41 Heat / cold tags:** after the 2.7 additions, `/dumptags` lists them; a lit furnace or bonfire next to a keg reads Warm, an unlit furnace doesn't count, a polished packed ice block reads Chilly, a snow layer doesn't count
- [ ] **AL-42 Boot log:** client and server boot with the port; no mixin apply errors (including CCK's keg mixin), no recipe parse errors for `brewinandchewin:`
- [ ] **AL-43 Recipe book:** in the keg, clicking a beer recipe places wheat, seeds and mushroom; the ghost shows the water gauge and the Normal bar
- [ ] **AL-44 JEI transfer:** the `+` button on a Fermenting page moves the items into the keg's four slots; the fluid slot doesn't block it
- [ ] **AL-45 Numbed hearts HUD:** at Tipsy III, take 6 damage -> 3 hearts show the numbed sprite, flicker in the last 4 s, then cardiac arrest lands; same with absorption hearts
- [ ] **AL-46 Signs sober up:** scrambled signs read normally again the moment Tipsy drops below the threshold
- [ ] **AL-47 /msg to two players:** a Tipsy sender's both echoes and both receivers' copies scramble the same way
- [ ] **AL-48 Old coaster block entity:** a 2.0 world's item coaster keeps its item after loading (no "Skipping BlockEntity" in the log)

## 8. Changes to make
- jei
  - add effect tooltips to fluid output on keg
- tooltips
  - heating cask, cooling cask
  - placeable on coaster 
    - does it work w/ other mods?
    - does it overlap w/ display delight?
- ui
  - keg missing left input fluid slot, fluids pass into output slot
  - keg plating ui has black/purple missing texture
- remove jams, marmalades, jelly
  - handled by fruit delight
- remove picked pickles
  - handles by veggies/vintage delight when ported? leave enabled for now
- cheese
  - remove pumpkin seed from flaxen cheese, replace with brown mushroom
- food
  - change scarlet pierogi nether wart to beetroot
  - remove creativeengineering kubejs recipe
- pale jane
  - swap tree bark for immersive weathering
- beer
  - swap seeds to dandelion
- mead
  - swap seeds to red muchroom
  - merge mead from vinery into brewin when added
- bloody mary
  - sweet berries to salt
- rice wine
  - add 2nd rice ingredient
- steel-toe stout
  - change ingot to any metal dust, wheat to blaze powder, boost resistance effect
- pizza
  - 3 wheat to 1 dough
- ham and cheese
  - allow bread slices
  - efficient smoked ham recipe, allow other cooked pork
  - sequenced assembly for both efficiencies
- quiche
  - sequence assembly
  - allow full cabbage in mushroom quiche
- fluids
  - create filling and draining recipes for drinks (not cheese)
  - pumping fluids in and out of kegs

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

# Part B: Let's Do Vinery (Backported Vinery)

Test prefix **VB-** · Written **2026-10-14** · Status **analysis + plan; D13 chosen 2026-10-18 (upstream jars as
resource donors, B4.8, feasible); not built**

## B0. At a glance

| Part | Status |
|---|---|
| Gap analysis 1.3.10 -> 1.5.5 (B2) | written 2026-10-14 from decompiles of both jars and of the bundled Foundation 1.0.1 |
| Migration issues (B3) | 75 issues listed, grouped by kind |
| License (B3.6) | code MIT, resources All Rights Reserved (Vinery 2026-10-14, Foundation 2026-10-18: same split). Satisfy refused redistribution of assets and features (B3.6) |
| Upstream jars as resource donors (B4.8) | CHOSEN 2026-10-18 (D13 B). Feasibility checked 2026-10-18: works; the port reads assets/data from the unmodified 1.5.5 and 1.3.10 jars at runtime. Delivery of the jars (D14) open |
| Decisions (B5) | DECIDED 2026-10-14: D1 keep `cherry_*`, D2 keep every 1.3.10 block and add the new ones, D6 WTHIT, D11 winemaker set damage by config. Aging revamped to a playtime clock (B4.5). 2026-10-18: D13 option B (B4.8). D14 decided 2026-10-18: payload now, download on publish. Worldgen (user, 2026-10-18): 1.20+ worldgen types become the nearest 1.19.2 vanilla type, nothing backported. Still open, built with the recommendation: D3, D4, D5 (non-aging numbers), D7 to D10, D12 |
| Wine aging (B4.5) | DESIGNED 2026-10-14: vintage clock on real playtime, lengths unchanged for now (16 days per year, 2 years per level) |
| Port (`development/backportedVinery`) | BUILT 2026-10-18, never booted: Foundation subset, 1.5.5, 1.3.10 keepers, vintage clock, upstream-pack layer, WTHIT lines; status and gaps in B4.9 |
| Pack-side changes (B4.4) | DONE 2026-10-18 except the jar swap in `mods/` (the game was running) and the I25 tool reruns; per-item outcome under B4.4 |
| Deploy (D14) | `tools/build_deploy.py`: `vinery_upstream/` category (both sides) + sha1 check against the port's pins; `backportedVinery` in IN_TREE_MODS; mod_sides row swapped, doapi row removed |
| In-game tests | VB-01 to VB-47 drafted (B7); in-game testing is the user's |

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
| Grapevine Pot | no block entity; 6 grapes, 3 landings on it, 2 juice bottles | block entity; still 6 grapes (one type only now, where the first grape used to set the type) and 3 per bottle. It takes **12 stomps** (`grapevinePotStomps`), and an entity with 15 armor or more counts 10% more (`grapevinePotHeavyArmorBonus`). Coloured splash particles. Breaking an unstomped pot drops its grapes |
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
| Village houses | none | `data/vinery/structure/vinery_{desert,plains,savanna,taiga}.nbt` ship, but **nothing adds them to a pool**, and their palettes still name 1.3-era ids (`doapi:storage`, `vinery:basket`, `chair`, `table`, `loam`, `wine_press`, `cherry_*`). Dead data |
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
| `wine_trader_chance` | `trader.spawnChance` | pack sets 0, because the pack's own `WinemakerSpawner` handles it |
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

| # | Issue | Port approach |
|---|---|---|
| W1 | ~60 ids renamed or gone (B2.1); Forge drops unknown blocks/items with a missing-mappings warning | **Mostly gone after D1 / D2 (B4.1):** every 1.3.10 id stays registered. `MissingMappingsEvent` is left only for the effects (W9) |
| W2 | `doapi:storage` block entities in placed racks, shelves, wine bottles, the wine box, storage pots (1.3.10) | rewrite the `id` on load (hook `BlockEntity.loadStatic`) to `vinery:storage`, `vinery:storage_pot` or the cabinet type per block. Block-entity types are not covered by missing mappings. The slot layouts differ per storage type (old `four_bottle` / `nine_bottle` / `wine_box` vs new layouts): map slot by slot, drop the overflow |
| W3 | Aging Barrel contents (old layout 0 bottle, 1-4 ingredients, 5 output) | on load: old slot 0 -> 4 (bottles); old juice items in 1-4 -> poured into the fluid level when the type matches (25 each, up to 100), the rest drop; other ingredients keep 1-3, a fourth drops |
| W4 | Apple Press contents (old 0 in, 1 out) | old 0 -> 0, old 1 (mash) -> 1. The block is 2 tall now: if the space above is blocked, keep a 1-tall legacy state or drop the press as an item |
| W5 | Grapevine pots mid-stomp (no block entity, stage 3-6) | block entity created on first touch; stage > 3 gives progress `(stage-3) x points/3` (1.5.5 already does this) |
| W6 | Apple / cherry leaves: `can_have_apples` / `can_have_cherries` -> `can_grow_*`; new `age` | blockstate fixer: unknown properties fall back to the default (no fruit). Better: a `ChunkDataEvent.Load` palette pass that maps `can_have_*` -> `can_grow_*`, and `has_*=true` -> `age=3` |
| W7 | `grapevine_lattice` with jungle grapes on it (age/grape in state, no block entity) | remap to the lattice from D1, facing north, keep `age` and `grape`, block entity created on load |
| W8 | Wines with `Year` NBT on the day-time clock | convert once, when the stack is first seen, to the vintage clock (B4.5): `ageTicks = (oldYearNow - Year) x 16 x ticksPerDay`, then `brewedAt = clock - ageTicks`. Do not use the upstream `year x daysPerYear` fallback, which would make some wines "brewed in the future" after lots of sleeping |
| W9 | Active instances of removed effects (`trippy`, `improved_*`) | remap each improved effect to its vanilla effect (`improved_speed` -> `speed` and so on), and `improved_absorbtion` -> `absorption` (D3) |
| W10 | `vinery:chair` seat entities | drop on load, as the player just stands up; or remap to `foundation:chair` |
| W11 | Every Compat, Dramatic Doors, Lightman's Currency, Supplementaries and Supplementaries Squared generate 65 items from Vinery's `cherry` wood type (`everycomp:*/vinery/cherry_*`, `dramaticdoors:*_vinery_cherry_door`, `lightmanscurrency:auction_stand_cherry`, `supplementaries:vinery/*_cherry`) | renaming the wood to `dark_cherry` renames all 65, and their placed blocks are lost. **Main reason for D1** |

### B3.4 Pack integration

| # | Pack file / feature | Impact |
|---|---|---|
| I1 | License | code MIT, resources All Rights Reserved (B3.6). Settle D13 before building anything that ships upstream files |
| I2 | patcherMod `FermentationBarrelStackOutputMixin` | targets the 1.3.10 class and slots; 1.5.5 stacks by itself. **Remove** |
| I3 | patcherMod `compat/vinery` JEI plugin (VineryJEI, 4 pages, [vinery_jei.md](vinery_jei.md)) | the port ships JEI for the barrel and both press stages. Keep only Grape Stomping (12 stomps now) and Wine Aging (rewrite for storage aging and the cellar multiplier); retire Fermentation and Wine Press; update `config/jei/recipe-category-sort-order.ini` (+ `configs_edited/`) |
| I4 | patcherMod `WinemakerTrades` | reflects on `satisfyu.vinery.entity.WanderingWinemakerEntity`'s static trade map. The class moves to `net.satisfy.vinery.core.entity` and its trades become config-driven: rewrite against the port's API (a direct dependency is fine now, since we own the jar) |
| I5 | patcherMod `WinemakerSpawner` | reads `config/vinery/config.json5 wine_trader_chance`: switch to the port's `trader.spawnChance` (pack: 0) |
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
- W2 (storage block entity ids) and W3 / W4 (barrel and press contents) still apply.
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
   contents conversion (W3).
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
   ported storage block entity (no mixin needed), tooltip, effect list setting, `Year` conversion (W8).
7. **Effects** (B2.5, P6). Experience orb mixin, fall-damage / jump mixins, the double-jump client handler.
8. **Entities and trades:** mule, Wandering Winemaker (spawn via the pack's spawner, I4, I5), villager profession +
   config trades (P16).
9. **Worldgen** (P15, F7). **Commands** (B2.6). **Data** (F1 to F6, done in memory by the B4.8 translation). **Config** (P14).
10. **Client:** renderers (lattice, pot, storage pot, log, bottles, racks, banner), screens, colours, armor models.
    WTHIT lines (D6, B4.6) in place of the info overlay.
11. **JEI** (I3).
12. **Old-world conversion pass** (W2, W3, W6) and a scratch-server boot on a copy of a 1.3.10 world.

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
| [../kubejs/server_scripts/tags/wood_tags.js](../kubejs/server_scripts/tags/wood_tags.js), [../kubejs/server_scripts/tags/blocktags.js](../kubejs/server_scripts/tags/blocktags.js) | I11, I12 |
| [../kubejs/server_scripts/engineering/fabric.js](../kubejs/server_scripts/engineering/fabric.js), [../kubejs/server_scripts/other/misc_mods.js](../kubejs/server_scripts/other/misc_mods.js) | I13 |
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
| I5 | `WinemakerSpawner` reads `config/vinery-common.toml` `[trader] spawnChance` when that file exists, else 1.3.10's json5; CEConfig comment names both |
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
| I26 | mod_sides row swapped, doapi row removed (nothing else needs doapi: scanned every mods.toml); `vinery_upstream/` deploy category. NOT DONE: the swap in `mods/` (run `./gradlew :patcherMod:installToMods :backportedVinery:installToMods` with the game closed) |
| Config | none to write: the port's spec defaults are the pack values (aging 16 days per year, 2 years per level, 28.2 h per year; `[winemaker_set]` as today; `trader.spawnChance` 0). Forge writes `config/vinery-common.toml` on first boot. 1.3.10's `config/vinery/config.json5` becomes unused |

### B4.5 Wine aging revamp: playtime clock (decided 2026-10-14)

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
  - Barrel: a 1.3.10 barrel (no `FluidLevel` key) converts on load per W3.
  - Winemaker job site: the Aging Barrel (1.5.5); 1.3.10's was the wine press.
  - Port-only text lives in `assets/vinery_backport/lang/` (keys are global; the path never matches upstream's).
- **Open decisions built with the recommendation:** D3 (trippy kept as a client post shader, `improved_*` remapped
  to vanilla), D4 (1.5.5 effect list, plus praetorian Glowing and king danis Instant Health), D5 (1.5.5 numbers;
  aging per B4.5), D7 (Foundation bundled), D8 (no village houses), D9 (no hanging sign), D10 (rotten cherry kept,
  setting `rottenCherryChance` 0.15), D12 (bamboo racks / lattice from `quark:bamboo_planks`).
- **Done since (2026-10-18):** W2, W3, W4, W6, W8 to W10; praetorian / king danis barrel recipes
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
  `#missing`; 1.3.10's models don't). Cosmetic; a port-written model override would fix it.
- **Fourth boot (2026-10-18, user, world `Workspace`):** "Failed to load datapacks": `Unbound values in registry
  worldgen/configured_feature: [vinery:apple]`. 1.5.5's `apple` / `apple_variant` tree configs carry a stale
  `sapling_provider` naming `vinery:apple_sapling`; the port's sapling is `apple_tree_sapling`, so `keep()` dropped
  both files (DEBUG line only) and `placed_feature/tree_apple` pointed at nothing. 1.19.2's `TreeConfiguration` has no
  `sapling_provider`, so `convertWorldgen` now strips it from tree configs. Dropped worldgen / biome-modifier files
  now log at WARN. The other 5 dropped files are the expected cherry-wood and hanging-sign recipes.
- **Not done yet:** a world join and the VB- tests (the user's); I25 reruns; stem "leaves pending" line.

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

### B5 D3. Removed effects

- **Recommended:**
  - Keep `trippy` registered. The pack's echo "discombobulate" uses it (I16), and it's only a client shader.
  - Remap the 10 `improved_*` effects to vanilla on load (W9).
- **Alternative:** drop `trippy` and replace it in `tool_effects.js` with Nausea.

### B5 D4. Wine effects

- **Recommended:** follow the 1.5.5 default list. It's one setting, so the pack can retune any wine later without
  code.
- **Alternative:** seed the setting with the 1.3.10 effects where a counterpart exists. That doesn't fully work,
  because the `improved_*` effects are gone.

### B5 D5. Balance numbers

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

### B5 D7. Foundation

- **Recommended:** bundle the Foundation subset inside the Vinery jar, keeping its packages.
- **Alternative:** a separate `foundation` mod jar. That only pays off if more Let's Do mods get backported. Vinery is
  the only one in the pack today.

### B5 D8. Village houses

- **Recommended:** skip them (F8, Q5).
- **Alternative:** remap their palettes and add them to village pools through the pack's jigsaw tooling as a later,
  separate task.

### B5 D9. Hanging sign

- **Recommended:** drop it (P10). Supplementaries already gives a Vinery cherry hanging sign.

### B5 D10. Rotten cherry

- **Recommended:** keep 1.3.10's chance for cherry leaves to drop a rotten cherry. 1.5.5 left the item with no source
  (Q1).

### B5 D11. Winemaker set bonus (DECIDED 2026-10-14: settings for no damage or a set amount, see B4.7)

- **Recommended:** keep the 1.3.10 settings: keep chance 100, damage chance 30, damage 1 (the pack's values today).
- **Alternative:** follow 1.5.5: always keep the bone meal, and every use damages all 4 pieces by 2.

### B5 D12. Bamboo lattice and racks

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

## B7 Changes to make

- jei
  - add input fluid item in input slot for fermentation barrel
  - expand fluid bar hover text to cover entire bar even when partially full on fermentation barrel
- apple press
  - only allow valid items in each 2nd input slot (apple, honeycomb for left, wine bottle, bottle for right)
- merge effects
  - luck
  - resistance
  - health effect?
- shuffle wine effects, integrate w/ fruits delight and other existing mod effects
  - fall back to default (alt for removed/merged effects) when other mods not added
- add providers to wine effects
- remove mead, brewin covers mead
- change completion banner effect to health boost, configurable
- merge vinery calendar w/ serene seasons
  - ensure season, custom day length, and vinery age all work off the same scale. set all time values in one file sleeping should speed up wine age. 
  - rename wine 'year' description to month everywhere
  - +1 level every 2 months, 2.5x age in the dark

## B8. In-game test suite (drafted, run once built)

- [ ] **VB-01 Juice into barrel:** 4 red grape juice in slot 0 -> gauge 100, 4 wine bottles in slot 4
- [ ] **VB-02 One juice type:** barrel holding red juice, put white juice in slot 0 -> it stays in the slot
- [ ] **VB-03 Red wine:** 10 red units + sugar + wine bottle -> red wine after 6000 ticks, 10 units used
- [ ] **VB-04 No-bottle wine:** solaris (honey, sweet berries, 15 white units, no bottle) -> crafts without a bottle
- [ ] **VB-05 Output stacks:** two red wines in a row stack in slot 5 (same day)
- [ ] **VB-06 Empty juice:** sneak-right-click with an empty hand -> juice level 0, splash sound
- [ ] **VB-07 Barrel hoppers:** juice and bottles from the top, ingredients from the side, output from below; nothing jams
- [ ] **VB-08 Apple press:** apples -> mash (600 t); mash + wine bottle -> apple juice (800 t); honeycomb block -> 4 honeycomb -> honey bottle
- [ ] **VB-09 Press is 2 tall:** breaking either half drops one press and the contents
- [ ] **VB-10 Pot stomping:** 6 red grapes, 12 jumps -> 2 red juice; with 15+ armor it takes fewer
- [ ] **VB-11 Pot one type:** red grape then white grape -> white refused
- [ ] **VB-12 Grapevine leaves:** plant red seeds on a stem on dirt -> leaves around it within 4800-9600 ticks
- [ ] **VB-13 Lattice:** place oak lattice on a wall, plant jungle seeds, grows; axe toggles support; shears prune
- [ ] **VB-14 Lattice connect:** 3 in a row -> left/middle/right models
- [ ] **VB-15 Taiga bush soil:** taiga bush with no podzol/coarse dirt/grass within 4 -> doesn't grow
- [ ] **VB-16 Fox:** fox eats ripe grapes from a bush
- [ ] **VB-17 Fruit leaves:** apple leaves fruit and drop 1-4 apples; cherry leaves sometimes drop a rotten cherry (D10)
- [ ] **VB-18 Stackable log:** light with flint and steel, cook 4 raw beef, refuel with a log, put out with a shovel
- [ ] **VB-19 Wine aging in a cellar:** wine in a rack with no sky light ages x1.5 (`/wine info`)
- [ ] **VB-20 Aging pace:** default settings -> `/wine time` shows about 28 h per year and 16 days per year; `/wine info` on a new wine says level up after 32 days
- [ ] **VB-21 Grape juice cures poison:** poisoned, drink juice -> poison gone, wine bottle back
- [ ] **VB-22 Double jump (clark wine):** second jump in the air on a dedicated server, no kick
- [ ] **VB-23 Walkers:** kelp cider walks on water, bolvar on lava
- [ ] **VB-24 Climbing, frosty, armor, health:** jo special climbs walls; eiswein freezes + armor; aegis +2 armor; stal +4 max health
- [ ] **VB-25 Party:** lilitu wine, hit a zombie -> firework at the target
- [ ] **VB-26 Banner:** wine_somelier advancement gives 2 banners; standing within 8 blocks gives Regeneration II
- [ ] **VB-27 Winemaker set and charm:** full set + bone meal -> bone meal kept, 30% of uses damage all 4 pieces by 1; set + bonemeal charm -> exactly one refund
- [ ] **VB-28 JEI:** all five Vinery pages (barrel, press mashing, press fermenting, grape stomping, wine aging) come from the port, in that order after Brewin's; none of patcherMod's `creativeengineering:vinery_*` pages
- [ ] **VB-29 Old world: ids kept:** a 1.3.10 world with cherry blocks, crates, wine_press, juice bottles, vinemaker armor, stove, sink, flower box -> all present under the same ids; nothing in the missing-mappings log except effects
- [ ] **VB-30 Old world: storage:** 1.3.10 wine racks, shelves and placed wine bottles keep their bottles
- [ ] **VB-31 Old world: barrel and wines:** 1.3.10 barrel with juice and bottles converts per W3; an old 2-year wine shows its age, not "brewed in the future"
- [ ] **VB-32 Old world: cross-mod cherry:** Every Compat / Dramatic Doors cherry blocks still there (D1)
- [ ] **VB-33 Clock only on playtime:** dedicated server, note `/wine time`, log everyone off for 10 min, log back in -> clock moved by about 0
- [ ] **VB-34 Today's pace measured:** play through one full world day with `dayUnit = WORLD`, `worldDayMinutes = 0` -> the measured day length is about 106 min, close to the 28.2 h-per-year default
- [ ] **VB-35 Sleep and /time don't age:** `/wine info` on a wine, sleep through a night and `/time add 24000` -> the age is unchanged
- [ ] **VB-36 Rescale:** set `playtimeHoursPerYear` 5, `daysPerYear` 15, reload -> an existing wine's age and level change at once; `/wine clock add 5h` -> exactly 1 more year
- [ ] **VB-37 Cellar:** two fresh wines, one in a rack in a dark cellar, one in a rack under sky; after the same playtime the cellar one is about 1.5x older
- [ ] **VB-38 Old and new blocks side by side:** place old chair / table / crate / barrel / wine_rack_1 next to their new counterparts -> both work; note any look-alikes to hide
- [ ] **VB-39 WTHIT lines:** look at a pot mid-stomp, a barrel mid-ferment, a press, a rack of wines in a cellar -> lines per B4.6
- [ ] **VB-40 Winemaker set settings:** `damageMode = NONE` -> 50 bone meal uses, no durability lost; `ONE_PIECE` + `damageAmount 3` -> one piece loses 3 per damaging use
- [ ] **VB-41 Upstream jar check:** remove `vinery_upstream/letsdo-vinery-fabric-1.5.5.jar` -> client and server both stop with the file name and download page; a renamed or different jar is refused the same way
- [ ] **VB-42 Missing art sweep:** first boot, grep the log for missing models and textures under `vinery:` and `foundation:` -> only the 21 known upstream dangling references (B4.8)
- [ ] **VB-43 Donor layering:** stove, kitchen sink and flower box show their 1.3.10 art; `cherry_planks` shows the 1.5.5 dark-cherry art; the lang says "Dark Cherry"
- [ ] **VB-44 Overrides still win:** the pack's `kubejs/data/vinery/worldgen/configured_feature/*` grape-bush configs are the ones in use (`/dumpall` or a locate), not upstream's
- [ ] **VB-45 Boots:** client and dedicated server start with the port and both mod ids (`vinery`, `foundation`) in the mod list; log has `[vinery-upstream] N data files ...` and no `vinery` / `foundation` errors
- [ ] **VB-46 Plains advancement:** `fruits_of_the_field` is granted on entering plains, not in other biomes
- [ ] **VB-47 WTHIT config:** the WTHIT config has a Vinery page with 5 toggles; turning one off hides only its lines; a fresh storage pot in a structure shows nothing and still has its loot when opened

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
