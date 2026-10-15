# Alcohol: Brewin' and Chewin' backport (Backported Brewin)

This pack ships Brewin' and Chewin' 1.19-2.0 (Forge 1.19.2). The plan is a **full port** of 4.5.0+1.21.1, built as our
own Forge 1.19.2 jar ("Backported Brewin"). Where the two versions differ, the port follows 4.5.0 unless a line below
says otherwise. This file holds the gap analysis (sections 2.1 to 2.6), the port plan (section 3) and the decisions
(section 4). Both versions are MIT (Umpaz).

## 0. At a glance

| Part | Status |
|---|---|
| Gap analysis 2.0 -> 4.5.0 | written 2026-10-14 from decompiles of both jars |
| Base fluid choice: honey / milk (4.1) | DECIDED 2026-10-14: the pack's `create:honey` and `minecraft:milk` |
| Kombucha base (4.5) | DECIDED 2026-10-14: `create_central_kitchen:green_tea` |
| Heat / cold block audit (2.7) | written 2026-10-14; tag additions proposed, not applied |
| Port (`development/backportedBrewin`) | not started |
| Pack-side changes (3.4) | not started |
| In-game tests | AL-01 to AL-41 drafted (7), run once built |

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
| yes | `immersiveengineering:coke_oven`, `blast_furnace`, `advanced_blast_furnace`, `alloy_smelter`, `arc_furnace`, `furnace_heater`, `blastfurnace_preheater` | industrial heat. The multiblock parts aren't lit-gated, so an idle oven counts. That's acceptable, or limit it to `furnace_heater` and `coke_oven` |
| yes | `createaddition:liquid_blaze_burner` | a blaze burner fed by liquid fuel; give it the same burner rule (check its heat property name) |
| yes | `create_enchantment_industry:blaze_enchanter` | heated by its blaze |
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
| yes | `immersive_weathering:thin_ice`, `icicle` | real ice |
| yes | `create_dd:freezing_sail` | Create D&D's freezing catalyst |
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
- `development/backportedBrewin/`: the port (not created yet)
- Pack files to change: section 3.4

## 6. Offline checks

None yet. Once built:
- A script that compares the port's `data/brewinandchewin/recipe/` tree against 4.5.0's, field by field, after mapping
  `c:` to `forge:`. It should report only the 4.4 / 4.5 deviations.
- A boot on a scratch server (scratch-server memory), and a log grep for missing remaps and the CCK mixin warning.

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

## 8. History

- 2026-10-14: gap analysis from decompiles of 1.19-2.0 (Forge) and 4.5.0+1.21.1 (Fabric). The user chose a full port
  that follows 4.5.0, client-only configurable scrambling, and a poison potion for Dread Nog. Honey/milk broken down in
  4.1, waiting for a pick. Kombucha green tea recommended (4.5). Found CCK's keg mixin (2.6).
  Replaces the earlier `brewinandchewin_parity.md`.
- 2026-10-14: decided: the pack's `create:honey` / `minecraft:milk` (4.1), green tea kombucha (4.5). Added the heat / cold
  block audit (2.7) and AL-41.
