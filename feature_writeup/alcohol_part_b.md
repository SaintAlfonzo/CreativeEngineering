# Alcohol Part B: Let's Do Vinery (Backported Vinery)

> Split out of [alcohol.md](alcohol.md) on 2026-11-17 (design_rules.md V-52); Part A (Brewin' & Chewin') stays
> there. Section numbers 10 to 13 in this file are sections of [alcohol.md](alcohol.md) (they cover both ports).

Test prefix **VB-** · Written **2026-10-14** · Status **port `-02` DEPLOYED with pack 1.0.8 (2026-10-19) and live in
1.0.9; `-04` in the local `mods/` 2026-10-20 (7.14, 10.9 to 10.19, 11, 13: village houses); none of it tested in game; D13 chosen 2026-10-18 (upstream
jars as resource donors, 4.8)**

## 0. At a glance

<!-- generated:status_index begin open_work sha1=ee138b62f98f -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 10 open TODO rows (Planned 2, Pinned 1, In Progress 1, Testing 6), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `alcohol/vineryport #4` | Planned | none | Upstream jars on publish |
| `alcohol/vineryport #5` | Planned | C | I25 tool reruns and `#missing` models |
| `alcohol/vineryport #3` | Pinned | S C P T | Wine effect reshuffle (7.4) |
| `merges/materials #4` | In Progress | S P | Crate and bag merge |
| `alcohol/vineryport #1` | Testing | S~ C~ P | Vinery 1.5.5 port |
| `alcohol/vineryport #2` | Testing | S~ C~ P | Decisions built with the recommendation |
| `alcohol/vineryport #9` | Testing | S C P T | Grape stomping: 12 grapes, 4 stomps, 4 bottles |
| `alcohol/vineryport #14` | Testing | S+ C P | Vinery village houses |
| `alcohol/vineryport #27` | Testing | S C P | Jellie Wine: the enchanted golden apple set |
| `ui/effects #3` | Testing | C P | Merge backported effect descriptions with JEED |
<!-- generated:status_index end open_work -->

**README index entry, moved here 2026-11-16 (owner ruling D6).** Feature: Alcohol: Vinery backport (Fabric 1.5.5 / MC 1.21.1 -> Forge 1.19.2 as Backported Vinery: juice-level barrel, 2-stage apple press, storage/cellar wine aging, new effects, cherry -> dark_cherry rename, removed blocks, Foundation subset, 75 migration issues; the 1.3.10 old-world conversion was removed 2026-10-27, the winemaker spawner moved into the port). Status: port `-01` DEPLOYED with 1.0.7 (replaces Vinery 1.3.10 + doapi; hotfix for the grass slab crash); `-02` with B7 and section 10 DEPLOYED with 1.0.8 (not tested in game); D3-D5, D7-D10, D12 built with the recommendation; `-03` (10.9 to 10.19: sizes, racks, box looks, tooltips, hopper jams; wines eat as rotten food, section 11) then `-04` (village houses in vanilla villages, section 13) DEPLOYED with 1.1.01, `-05` to the client with 1.1.04; VB-73, VB-74, VB-81 to VB-83, VB-87 to VB-89, VB-91 PASSED 2026-10-20, the rest of VB-01 to VB-94 pending

| Part | Status |
|---|---|
| Gap analysis 1.3.10 -> 1.5.5 (2) | written 2026-10-14 from decompiles of both jars and of the bundled Foundation 1.0.1 |
| Migration issues (3) | 75 issues listed, grouped by kind |
| License (3.6) | code MIT, resources All Rights Reserved (Vinery 2026-10-14, Foundation 2026-10-18: same split). Satisfy refused redistribution of assets and features (3.6) |
| Upstream jars as resource donors (4.8) | CHOSEN 2026-10-18 (D13 B). Feasibility checked 2026-10-18: works; the port reads assets/data from the unmodified 1.5.5 and 1.3.10 jars at runtime. Delivery of the jars (D14) open |
| Decisions (5) | DECIDED 2026-10-18: D3 keep `trippy` (section 10.1). DECIDED 2026-10-14: D1 keep `cherry_*`, D2 keep every 1.3.10 block and add the new ones, D6 WTHIT, D11 winemaker set damage by config. Aging revamped to a playtime clock (4.5). 2026-10-18: D13 option B (4.8). D14 decided 2026-10-18: payload now, download on publish. Worldgen (owner, 2026-10-18): 1.20+ worldgen types become the nearest 1.19.2 vanilla type, nothing backported. Still open, built with the recommendation: D4, D5 (non-aging numbers), D7 to D10, D12 |
| Wine aging (4.5) | DESIGNED 2026-10-14: vintage clock on real playtime, lengths unchanged for now (16 days per year, 2 years per level). 2026-10-18: your 7 list changes this to months on the sun clock (sleep ages wine), x2.5 in the dark: [time.md](time.md) 3.7 |
| Port (`development/backportedVinery`) | BUILT 2026-10-18. `-01` DEPLOYED with 1.0.7 (the first staging crashed the server on the grass slab, fixed under the same name, [fixes_compat.md](fixes_compat.md) 2.23); `-02` DEPLOYED with 1.0.8 (2026-10-19 10:20), loaded in single player 2026-10-19: Foundation subset, 1.5.5, 1.3.10 keepers, vintage clock, upstream-pack layer, WTHIT lines; status and gaps in 4.9. `-03` (7.14 and 10.9 to 10.14) in the local `mods/` since 2026-10-19 22:27; `-03` / `-04` shipped in 1.1.01, `-05` in 1.1.04 (client), `-06` installed 2026-10-31 00:41, not yet booted |
| Pack-side changes (4.4) | DONE 2026-10-18 (jar swap shipped in 1.0.7) except the I25 tool reruns; per-item outcome under 4.4 |
| Deploy (D14) | `tools/build_deploy.py`: `vinery_upstream/` category (both sides) + sha1 check against the port's pins; `backportedVinery` in IN_TREE_MODS; mod_sides row swapped, doapi row removed |
| Changes to make (7) | BUILT 2026-10-18, not tested in game: everything but 7.4 (pinned), plus the port side of [time.md](time.md) 3.7; 7.11 settled by 10.8; 7.14 built 2026-10-19 into `-03`. Per-item outcome in 7 "Build status" |
| Third list and follow-ups (10) | Vinery side BUILT, not tested in game: 10.5 to 10.8 in `-02` (live); 10.9 duplicate-stack fix, 10.10 / 10.11 duplicates and racks removed, 10.12 sizes from the per-block limit, 10.13 storage climate, 10.14 slim bottles (recoloured in memory) in `-03`. 10.15 / 10.17 / 10.18 in the local `-03`: bottle sizes, box looks, Stained Glass and the wine box / flying fixes TESTED 2026-10-20 (10.18) |
| In-game tests | VB-01 to VB-98 drafted (8; VB-95 to VB-98 = village houses, section 13); in-game testing is the user's. PASSED 2026-10-20: VB-73, VB-74, VB-81 to VB-83, VB-87 to VB-89, VB-91 (10.18) |

- **Source jars:**
  - Ours: `mods/letsdo-vinery-forge-1.3.10.jar` (Forge 1.19.2, needs `doapi-1.1.0b`). Behaviour reference:
    [vinery.md](vinery.md).
  - Target: `DecompiledMods/letsdo-vinery-fabric-1.5.5/` (Fabric, Minecraft 1.21.1, needs Architectury 13, Cloth Config
    15 and Foundation 1.0.1, which is bundled at `META-INF/jars/foundation-fabric-1.0.1.jar`).
- **License (3.6):** split.
  - The Java code is **MIT**, so it can be ported.
  - Everything under `src/main/resources/` is **All Rights Reserved**. That covers textures, models, sounds, lang,
    recipes, tags, loot, worldgen and structures.
  - So a port can't ship upstream's assets or data without permission from Satisfy (D13). Satisfy was asked and
    refused.
  - Foundation 1.0.1 has the same split (its `License` file, read 2026-10-18).
  - **Route chosen (D13 B, 4.8):** the port ships only MIT code and our own files. It reads upstream's assets and
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
  - 1.5.5 real sources: Modrinth hosts `letsdo-vinery-fabric-1.5.5-sources.jar` next to the jar (4.8). Port from
    that, not from the decompile.
  - Old jar: `python3 tools/decompile_mod.py mods/letsdo-vinery-forge-1.3.10.jar <out>`.
  - New jar: zip `DecompiledMods/letsdo-vinery-fabric-1.5.5/net` and run
    `java -jar ~/.cache/ce-tools/vineflower-1.10.1.jar <zip> <out>`. Run the same command on the bundled
    `foundation-fabric-1.0.1.jar`. Names come out as Fabric intermediary.

## 1. Overview

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

## 2. Reference: 1.3.10 vs 1.5.5

### 2.1 Registry

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
| Effects | `armor_effect`, `health_effect`, `luck_effect`, `resistance_effect`, `double_jump`, `party_effect`, `climbing_effect`, `frosty_armor`, `lava_walker`, `water_walker` (2.5) |
| Block entities | `grapevine_pot`, `lattice`, `storage_pot`, `stackable_log`, `apple_press`, `cabinet`, `vinery_standard`, `dark_cherry_barrel`, `mod_sign`, `mod_hanging_sign` |
| Recipe types | `vinery:apple_mashing`, `vinery:apple_fermenting` (new); `vinery:wine_fermentation` (new format) |

### 2.2 Aging Barrel (`fermentation_barrel`)

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

`apple_juice` is no longer made in the barrel. The Apple Press makes it now (2.3).

### 2.3 Apple Press (`wine_press` -> `apple_press`)

| | 1.3.10 | 1.5.5 |
|---|---|---|
| Shape | 1 block | 2 blocks tall (upper and lower half) |
| Stage 1 (mashing) | 1 apple -> 1 apple mash, hardcoded, 72 ticks | `apple_mashing` recipes: apple -> mash; honeycomb block -> 4 honeycomb. **600 ticks** (`applePressMashingTime`) |
| Stage 2 (fermenting) | none (the barrel made apple juice) | `apple_fermenting` recipes: mash + wine bottle -> apple juice; honeycomb + wine bottle -> honey bottle. **800 ticks** (`applePressFermentationTime`) |
| Slots | 0 in, 1 out | 0 apple, 1 mash (stage 1 output and stage 2 input), 2 bottle, 3 output |
| Hoppers | any item into either slot (jams) | sides insert by recipe; output from the sides or bottom |

### 2.4 Grapes, pot, stems, lattices, leaves, logs

| Thing | 1.3.10 | 1.5.5 |
|---|---|---|
| Grapevine Pot | no block entity; 6 grapes, 3 landings on it, 2 juice bottles | block entity; still 6 grapes (one type only now, where the first grape used to set the type) and 3 per bottle. It takes **12 stomps** (`grapevinePotStomps`), and an entity with 15 armor or more counts 10% more (`grapevinePotHeavyArmorBonus`). Coloured splash particles. Breaking an unstomped pot drops its grapes. **Pack (7.14):** 12 grapes, 4 stomps, 4 bottles |
| Grapevine stem | ages 0-4; shears prune it | same, plus **grapevine leaves**: a red or white stem on a non-stem block grows up to 11 leaf blocks around it 4800-9600 ticks after planting (`grapevineLeavesEnabled`) |
| Lattice | one `grapevine_lattice` | 10 woods; block entity; connects left/right, floor or wall placement, an axe toggles the support post, shears prune, hanging-grape model on 15%; leaf particles |
| Wild bushes | grow by vanilla random tick | growth chance 0.5 x `grapeGrowthMultiplier`; min light 10 (savanna 14, taiga 5 and needs podzol / coarse dirt / grass within 4); foxes eat ripe grapes (mixin) |
| Apple / cherry leaves | `can_have_*` / `has_*` properties; rotten cherry chance | Foundation `FruitLeavesBlock`: `can_grow_*` / `has_*` / `age` 0-3, 30% of placed leaves can fruit, growth chance 0.4 each; picking drops 1-4 fruit. **No rotten cherry source anywhere in 1.5.5** (no loot table, recipe or code) |
| Grape juice | drink | drinking it also **cures Poison** and gives the wine bottle back |
| Stackable log | stack of logs that can be lit | a campfire: 4 food slots, campfire recipes, burns 12000 ticks, refuel with burnable logs, put out with a shovel or water bottle, lit by flint and steel / fire charge / burning projectiles, hurts entities on it, light 13 |
| Grass / dirt slabs | `grass_slab`, `dirt_slab` | grass slab spreads to dirt (mixin on vanilla grass spreading), plants can't sit on a bottom grass slab, shovels can't path a slab, `dirt_path_slab` added |

### 2.5 Wine aging and effects

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

### 2.6 Entities, trades, worldgen, commands, client

| | 1.3.10 | 1.5.5 |
|---|---|---|
| Wandering Winemaker + 2 mules | replaces `wine_trader_chance`% of trader spawns | `spawnChance` 0.5, `spawnWithMules`, `spawnDelay` 48000; same mixin approach |
| Winemaker villager | POI on the barrel, hardcoded trades | trades are a config list per level (BUY/SELL, price, count, max uses, xp) |
| Worldgen | biome modifiers per grape type and tree (`forge/biome_modifier/add_*`) | same placed-feature ids (`tree_apple`, `tree_cherry`, `*_grape_bush_chance`), biome tags `spawns_*`; new apple / dark cherry tree variants |
| Village houses | none | `data/vinery/structure/vinery_{desert,plains,savanna,taiga}.nbt` ship, but **nothing adds them to a pool**, and their palettes still name 1.3-era ids (`vinery:basket`, `chair`, `table`, `loam`, `cherry_*`). Dead data. Per-house breakdown (checked 2026-10-20 from the NBTs): 5 D8 |
| Commands | none | `/vinery dump grapes|juices|effects`, `grow`, `barrel finish`, `press finish`, `pot fill`, `winemaker spawn|locate`, `effect`, `reload`; `/wine info|time|storage|level|mature|fresh|years|days` (op 2 for edits) |
| JEI | none (REI only); the pack has its own plugin in patcherMod ([vinery_jei.md](vinery_jei.md)) | built-in: barrel, press mashing, press fermenting, barrel recipe transfer |
| HUD | none | Foundation `BlockInfoOverlay`: pot contents/stomps, storage slot contents (`infoOverlay.mode`) |
| Creative | one tab | one tab with 4 side tabs (essentials, wines, dark cherry, decoration) |
| Advancements | 60 files | 20 in `main/` + recipe unlocks; `wine_somelier` gives 800 xp + 2 banners |

### 2.7 Settings, 1.3.10 -> 1.5.5

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

## 3. Migration issues

Each row is something the port has to handle. The ID is used in 4 and 5.

### 3.1 Platform: Fabric 1.21.1 -> Forge 1.19.2

| # | Issue | Port approach |
|---|---|---|
| P1 | Foundation 1.0.1 is a Fabric/NeoForge 1.21 library; 1.19.2 only has doapi 1.1.0b, a different API | Port the ~6k lines of Foundation that Vinery uses into the jar, keeping the package `net.satisfy.foundation` (5 D7). doapi can leave the pack afterwards (only Vinery needs it) |
| P2 | Data components (`WINE_YEAR`, `CUSTOM_FOOD`, `potion_contents`, `fireworks`) | ItemStack NBT. `WINE_YEAR` -> a `WineYear` compound with the same 11 field names |
| P3 | `ItemInteractionResult` / `useItemOn` split (1.20.5) | fold back into `Block#use` |
| P4 | `MapCodec` block codecs, `RecordCodecBuilder` recipe serializers, `StreamCodec` | drop the codecs; JSON `RecipeSerializer` + `FriendlyByteBuf` |
| P5 | `RecipeHolder`, `RecipeInput` classes (`FermentationBarrelRecipeInput` etc.) | `Recipe<Container>` with a small wrapper container |
| P6 | `MobEffect#applyEffectTick` returns boolean; `onEffectStarted` / `onMobRemoved`; attribute modifiers keyed by `ResourceLocation` | void tick; `addAttributeModifiers` / `removeAttributeModifiers` overrides (Jellie absorption); UUID-keyed modifiers |
| P7 | `ColorParticleOption` (Foundation `COLORED_DRIP`, `DYE_SPLASH`, `LEAF`) | a custom `ParticleType` with its own options class and codec-less deserializer |
| P8 | `TreeGrower` record (1.20.5) for saplings | `AbstractTreeGrower` subclasses (apple has a variant feature) |
| P9 | `BlockSetType` / `WoodType` registration for doors, trapdoors, buttons, plates, fence gates, signs | 1.19.2 constructors (Properties only), `WoodType.create` + `Sheets` material for the sign |
| P10 | Hanging signs (1.20) | 1.19.2 has none. Drop `dark_cherry_hanging_sign` (5 D9). The pack already has Supplementaries' `vinery/hanging_sign_cherry` |
| P11 | Vanilla 1.20 blocks used as materials: `cherry_planks` (cherry lattice and racks), `bamboo_planks` (bamboo lattice and racks), `netherite_upgrade_smithing_template` (banner copy) | cherry: see D1. bamboo: use `quark:bamboo_planks` (in the pack) or drop. Banner copy: 1.19.2 legacy smithing (banner + netherite ingot) |
| P12 | Boats via `WoodBoat` / `PlatformHelper.registerBoatType` | 1.19.2 `Boat` subclass with its own type enum (1.3.10 used doapi Terraform boats: keep the `cherry_boat` entity data compatible) |
| P13 | Fabric entrypoints for plugins (`VineryPlugin` via `getPlugins()`), JEI and REI | Forge: `ServiceLoader` or `ModFileScanData` for `@VineryPlugin.Entry`; `@JeiPlugin` |
| P14 | Cloth AutoConfig on Fabric | ForgeConfigSpec (`vinery-common.toml`, `vinery-client.toml` for the overlay), same key names, the pack's values seeded (4.4) |
| P15 | Fabric `BiomeModifications` | Forge `biome_modifier` JSONs with the **1.3.10 ids** (`add_apple_tree`, `add_cherry_tree`, `add_*_grape`) so `wild_crops.json` `disable_modifiers` keeps matching |
| P16 | Fabric villager / POI helpers, `TradeOfferHelper` | Forge `VillagerProfession` / `PoiType` registry + `VillagerTradesEvent` reading the config list |
| P17 | Mixins written against 1.21 code: `LivingEntity#eat(Level, ItemStack, FoodProperties)` (1.21 signature), `SpreadingSnowyDirtBlock#randomTick` with `CAPTURE_FAILHARD` locals, `WanderingTraderSpawner#spawn` target, `Leashable#setLeashedTo` | rewrite each against 1.19.2: `eat(Level, ItemStack)` + `addEatEffect`; new local capture or `@ModifyVariable`; leash via `Mob#setLeashedTo` |
| P18 | `ItemTags.SHOVELS` and the 1.20.5 `fox_food` tag | `forge:tools/shovels`; foxes keep the mixin path |
| P19 | `DamageSources` holder API (creeper effect), `Explosion.BlockInteraction` (`level.explode(... TNT)`) | `DamageSource.GENERIC`, `Explosion.BlockInteraction.BREAK` |
| P20 | Tooltip context / `TooltipFlag` signature, `Item#getUseDuration(stack, entity)` | 1.19.2 signatures; the wine tooltip reads the client level as today |
| P21 | Armor: `ArmorMaterial` holder + Foundation `TexturedArmorItem` / `ArmorModels` (custom straw-hat / apron models) | `ArmorItem` + `IClientItemExtensions#getHumanoidArmorModel`; the 1.3.10 jar already does this for the hat |
| P22 | Creative side tabs (Foundation client UI) | 1.19.2 `CreativeModeTab` with one tab; side tabs are optional, port later or never |
| P23 | Double jump runs only on the client (sets motion locally) | works on 1.19.2 as written. The server sees normal movement; check the "flying is not enabled" kick never fires on a dedicated server (VB-22) |

### 3.2 Data format

| # | Issue | Port approach |
|---|---|---|
| F1 | 1.21 folder names: `recipe/`, `loot_table/`, `advancement/`, `tags/item`, `tags/block` | rename to `recipes/`, `loot_tables/`, `advancements/`, `tags/items`, `tags/blocks` |
| F2 | Result stacks `{"id": ..., "count": ...}` | `{"item": ..., "count": ...}` |
| F3 | `c:` tags (`c:grapes`, `c:berries`, `c:seeds`, `c:milk`, stripped logs/wood) | `forge:` equivalents (1.3.10 already shipped both `c:` and `forge:`; keep both) |
| F4 | Advancement JSON: `"items": ["vinery:x"]` predicates, `inventory_changed`, loot reward table | 1.19.2 predicate shape `{"items": ["vinery:x"]}` inside `conditions.items`; reward loot table moves to `loot_tables/advancements/` |
| F5 | Smithing transform recipe with template (banner) | legacy `minecraft:smithing` (base + addition) |
| F6 | Block loot tables use `copy_components` for the storage pot / barrel | `copy_nbt` from `BlockEntityTag` |
| F7 | Worldgen JSON: 1.21 `tree` / `random_patch` configs | mostly the same in 1.19.2. The pack overrides 6 grape-bush configured features in `kubejs/data/vinery/worldgen/configured_feature/`; keep those ids and formats |
| F8 | Village structure NBTs (2.6) | not ported (dead upstream, stale palettes). If wanted later: remap palettes with the 4.1 table and add them through the pack's pool tooling |

### 3.3 Old worlds and old items

**2026-10-27: the conversions W2, W3, W8, W9 and W10 are REMOVED from the port** (CLAUDE.md "no old-version or
cross-mod conversions"; [pack_hygiene.md](pack_hygiene.md) finding 10): `BlockEntityLegacyIdMixin` (`doapi:storage`),
`FermentationBarrelBlockEntity.convertLegacyLayout`, `WineYears.convertLegacy` / `hasLegacyYear` with the
`legacyYearLengthDays` key, and the `improved_*` effect / `chair` entity entries in `VineryCommonEvents`. A world last
saved with 1.3.10 (before 1.0.7) loses 1.3.10 storage contents, old barrel layouts and old wines' ages. The rows below
are the original plan, kept for the record. W1 (ids kept, D1 / D2) and W4 (the press keeps its block entity id) need
no code.

| # | Issue | Port approach |
|---|---|---|
| W1 | ~60 ids renamed or gone (2.1); Forge drops unknown blocks/items with a missing-mappings warning | **Mostly gone after D1 / D2 (4.1):** every 1.3.10 id stays registered. The port has no `MissingMappingsEvent` handler (W9 removed 2026-10-27) |
| W2 | **REMOVED 2026-10-27.** `doapi:storage` block entities in placed racks, shelves, wine bottles, the wine box, storage pots (1.3.10) | rewrite the `id` on load (hook `BlockEntity.loadStatic`) to `vinery:storage`, `vinery:storage_pot` or the cabinet type per block. Block-entity types are not covered by missing mappings. The slot layouts differ per storage type (old `four_bottle` / `nine_bottle` / `wine_box` vs new layouts): map slot by slot, drop the overflow |
| W3 | **REMOVED 2026-10-27.** Aging Barrel contents (old layout 0 bottle, 1-4 ingredients, 5 output) | on load: old slot 0 -> 4 (bottles); old juice items in 1-4 -> poured into the fluid level when the type matches (25 each, up to 100), the rest drop; other ingredients keep 1-3, a fourth drops |
| W4 | Apple Press contents (old 0 in, 1 out) | old 0 -> 0, old 1 (mash) -> 1. The block is 2 tall now: if the space above is blocked, keep a 1-tall legacy state or drop the press as an item |
| W5 | Grapevine pots mid-stomp (no block entity, stage 3-6) | block entity created on first touch; stage > 3 gives progress `(stage-3) x points/3` (1.5.5 already does this) |
| W6 | Apple / cherry leaves: `can_have_apples` / `can_have_cherries` -> `can_grow_*`; new `age` | blockstate fixer: unknown properties fall back to the default (no fruit). Better: a `ChunkDataEvent.Load` palette pass that maps `can_have_*` -> `can_grow_*`, and `has_*=true` -> `age=3` |
| W7 | `grapevine_lattice` with jungle grapes on it (age/grape in state, no block entity) | remap to the lattice from D1, facing north, keep `age` and `grape`, block entity created on load |
| W8 | **REMOVED 2026-10-27.** Wines with `Year` NBT on the day-time clock | convert once, when the stack is first seen, to the vintage clock (4.5): `ageTicks = (oldYearNow - Year) x 16 x ticksPerDay`, then `brewedAt = clock - ageTicks`. Do not use the upstream `year x daysPerYear` fallback, which would make some wines "brewed in the future" after lots of sleeping |
| W9 | **REMOVED 2026-10-27.** Active instances of removed effects (`trippy`, `improved_*`) | remap each improved effect to its vanilla effect (`improved_speed` -> `speed` and so on), and `improved_absorbtion` -> `absorption` (D3) |
| W10 | **REMOVED 2026-10-27.** `vinery:chair` seat entities | drop on load, as the player just stands up; or remap to `foundation:chair` |
| W11 | Every Compat, Dramatic Doors, Lightman's Currency, Supplementaries and Supplementaries Squared generate 65 items from Vinery's `cherry` wood type (`everycomp:*/vinery/cherry_*`, `dramaticdoors:*_vinery_cherry_door`, `lightmanscurrency:auction_stand_cherry`, `supplementaries:vinery/*_cherry`) | renaming the wood to `dark_cherry` renames all 65, and their placed blocks are lost. **Main reason for D1** |

### 3.4 Pack integration

| # | Pack file / feature | Impact |
|---|---|---|
| I1 | License | code MIT, resources All Rights Reserved (3.6). Settle D13 before building anything that ships upstream files |
| I2 | patcherMod `FermentationBarrelStackOutputMixin` | targets the 1.3.10 class and slots; 1.5.5 stacks by itself. **Remove** |
| I3 | patcherMod `compat/vinery` JEI plugin (VineryJEI, 4 pages, [vinery_jei.md](vinery_jei.md); deleted 2026-10-27, pack_hygiene.md finding 6) | the port ships JEI for the barrel and both press stages. Keep only Grape Stomping (12 stomps now) and Wine Aging (rewrite for storage aging and the cellar multiplier); retire Fermentation and Wine Press; update `config/jei/recipe-category-sort-order.ini` (+ `configs_edited/`) |
| I4 | patcherMod `WinemakerTrades` | reflects on `satisfyu.vinery.entity.WanderingWinemakerEntity`'s static trade map. The class moves to `net.satisfy.vinery.core.entity` and its trades become config-driven: rewrite against the port's API (a direct dependency is fine now, since we own the jar) |
| I5 | patcherMod `WinemakerSpawner` (moved into the port 2026-10-27: `forge/WinemakerSpawner`, `[trader] ownSpawner`) | reads `config/vinery/config.json5 wine_trader_chance`: switch to the port's `trader.spawnChance` (pack: 0) |
| I6 | patcherMod `StuckInBlock` | rule keyed by class name `satisfyu.vinery.block.grape.TaigaGrapeBush`: becomes `net.satisfy.vinery.core.block.GrapeBushBlock$TaigaGrapeBush` |
| I7 | patcherMod `BonemealCharmMixin` | modelled on Vinery's bone-meal mixin. 1.5.5's version changes the return to PASS, so the charm (which checks for CONSUME) and the winemaker set no longer double-refund. The order still matters if both mixins hit the same call: check (VB-27) |
| I8 | patcherMod `crop_growth.json` (WTHIT growth line) | `vinery:grapevine_lattice` -> the 10 lattice ids (`vinery:*_lattice`); lattice age now lives in the block entity too, but the state `age` still drives it |
| I9 | Foundation `BlockInfoOverlay` HUD | not ported; the lines go into WTHIT (D6, decided) |
| I10 | KubeJS `item_dict.js` (144 Vinery entries) | rename keys/values per 4.1; add the new ids (lattices, racks, bags, cabinet, drawer, window block, path slab, banner, lilitu wine, new juices). Supplementaries / Every Compat entries follow D1 |
| I11 | `server_scripts/tags/wood_tags.js` | `vinery:barrel` / `vinery:wooden_barrels` -> the D2 barrel id |
| I12 | `server_scripts/tags/blocktags.js` (BlockRunner) | regexes name `vinery:cherry_*`, `*crate`, `loam*`, `dirt_slab`, `grass_slab`: add lattices, racks, bags, `dirt_path_slab`, `window_block`, `stackable_log` (now a campfire) |
| I13 | `server_scripts/engineering/fabric.js` + `other/misc_mods.js` | `replaceInput({ id: 'vinery:vinemaker_leggings' })` -> `winemaker_leggings` |
| I14 | `server_scripts/loot/07_rules.js` | `L.pack(i.vinery_apple_crate, ...)` -> `apple_bag` |
| I15 | `startup_scripts/compost/compost_values.js` | `vinery_apple_crate` 1.0 -> bags; 1.5.5's own compost list adds `apple_mash`, `straw_hat`, all seeds and grapes at 0.4: check it doesn't fight `global.compost` |
| I16 | `startup_scripts/func/tool_effects.js` | echo "discombobulate" uses `vinery:trippy`, which 1.5.5 removes. D3 |
| I17 | `kubejs/assets/creativeengineering/lang/en_us.json` | 17 JEED descriptions for old effects (all TODO); rewrite for the 2.5 set |
| I18 | `generation_scripts/worldgen/wild_crops.json` | names `vinery:tree_apple`, `vinery:tree_cherry`, `add_apple_tree`, `add_cherry_tree`: same ids kept (P15), so no change |
| I19 | `kubejs/data/vinery/worldgen/configured_feature/*grape_bush.json` (6 overrides) | same ids in 1.5.5. Re-diff against the new defaults |
| I20 | `config/ftbquests/quests/chapters/progression.snbt` | `vinery:flower_pot` (icon + task) and `vinery:flower_box` task: removed upstream. D2 |
| I21 | Alcohol Part A 2.7 heat audit | `vinery:stove` and `vinery:wood_fired_oven` are FD heat sources today; 1.5.5 removes both and has no `farmersdelight` tag at all. D2. The new fired `stackable_log` is a heat-source candidate |
| I22 | `config/everycomp-wood_types.toml` | `[types.wood_type.vinery]` / `leaves_type` keys follow D1 |
| I23 | Tooltips (`client_scripts/tooltips.js` Vinery section, [tooltips.md](tooltips.md)) | the grape / seed flavour keys may change; rewrite the placeholders for the new blocks |
| I24 | Placeable food ([placeable_food.md](placeable_food.md)) | wine bottles still need sneaking: no change; new juice ids |
| I25 | `tools/` (`crop_growth_coverage.py`, `placeable_catalog.py`, `stack_sizes.py`, `gen_spawn_table.py`, `food_catalog.py`) and `/dumpall` | rerun after the swap |
| I26 | Deploy | replace the jar in `mods/` and `server_mods/`, drop `doapi-1.1.0b.jar` (both sides; nothing else needs it: check with a scan first), update `mod_sides.md`, regenerate `pack/` |

### 3.5 Upstream quirks found while reading 1.5.5

| # | Quirk | Port |
|---|---|---|
| Q1 | No rotten cherry source (compost list and creative tab only) | keep 1.3.10's leaf drop (D10) |
| Q2 | `mead` needs **1** apple unit, so one apple juice bottle (25 units) makes 25 meads | keep, or set it to 25 in the port's recipe file (D5) |
| Q3 | Teleport (10 t) and Creeper (100 t) are instant effects, which run every tick while they last: up to 10 teleports, and 100 explosions plus 50 damage each (the first one kills you anyway) | 1.3.10 had the same kind of thing (vinery.md 2.11). Keep, or set the durations to 1 in the default list |
| Q4 | `chenet_wine` and `jo_special_mixture` both give Climbing; `luck_effect` and `resistance_effect` have no wine | keep (config can change it) |
| Q5 | Village structure files are unused and stale (2.6) | skip (F8) |
| Q6 | The winemaker set bonus has no settings and damages all 4 pieces by 2 on every use | D11 |
| Q7 | The barrel juice type is stored as a string; the GUI syncs it as an index into the registration order, so plugin juices must register in the same order on client and server | fine for a single jar; note for the API |

### 3.6 License

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
- The **code side** (3.1, 4.2 steps 1 to 12, 4.3) is fine as written. That includes the Foundation subset:
  Foundation's `License` (github.com/Let-s-Do-Collection/Foundation, `master`, read 2026-10-18) has the same split as
  Vinery's: `*/src/main/resources/` All Rights Reserved, everything else MIT, Copyright (c) 2024 satisfy. Its
  `fabric.mod.json` "Custom" means this split.
- The **resources side** is blocked as written:
  - 4.2 assumes upstream assets get copied, and 3.2 (F1 to F7) converts upstream data files. Both ship All Rights
    Reserved files.
  - That applies to 1.3.10's resources as well (`/forge/src/main/resources/` is listed).
  - Today the pack ships the official 1.3.10 jar unchanged, which is normal modpack use.
  - Repackaging 1.3.10 textures inside our jar is not normal modpack use.
- The pack's own existing overrides (`kubejs/data/vinery/worldgen/configured_feature/*.json`, 6 files) are edited
  copies of upstream data. Shipping them is the same kind of use the pack already makes of many mods' data. They're
  noted here only so the question isn't forgotten.
- doapi 1.1.0b declares `license = "ARR"`. The port no longer needs it, so nothing of doapi gets copied.

**Satisfy's answer (asked by the user, noted 2026-10-18):** they refused redistribution of "assets and features".
- **Assets:** a refusal of option A. 4.8 never copies, changes on disk or repackages an upstream file. Each machine
  reads the unmodified jars at runtime, the same way any resource pack overrides textures. How those jars reach each
  machine is D14.
- **Features:** the MIT grant covers the code as published. A grant on a published version can't be taken back, and
  game mechanics as such aren't something copyright covers. So the port of the code stands on the license text.
  It still goes against the author's stated wish. That matters little for a private pack copied by hand and more if the
  port is ever published (Modrinth, CurseForge, a public jar): then any All Rights Reserved file that slips in is an
  easy takedown. 6 adds a check that keeps upstream files out of the port.

This is a reading of the license text, not legal advice. D13 lists the ways forward; B was chosen (4.8).

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

## 4. Port plan

### 4.1 Id map (D1 and D2 as decided)

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

**Effect on 3.3:**
- W1 shrinks to the effects (W9) and the `vinery:chair` seat entity (W10).
- No item or block remaps are needed.
- W2 (storage block entity ids) and W3 / W4 (barrel and press contents) still apply. (W2 and W3 were removed again
  on 2026-10-27, see 3.3.)
- W7 no longer applies, since `grapevine_lattice` stays.

**Possible look-alike pairs to prune later:** old `chair` / `table` / `big_table` / `shelf` vs the new `cherry_*`
furniture; `*_crate` vs `*_bag`; `barrel` vs `cherry_barrel`; `wine_rack_1` / `_2` vs the wood racks. Compare them in
game (VB-38) before deciding whether to hide any from the creative tab and JEI. Hiding needs no id change.

### 4.2 The mod

- **Where:** `development/backportedVinery`, a subproject of the root Gradle build (`ce.forge-mod` convention), next to
  `backportedBrewin` and `exposureBackport`.
- **Ids:** `mod_id=vinery`, so recipes, tags, structures and worlds keep resolving.
- **Version:** `mod_version=1.5.5-ce-19.2-01`. It replaces `letsdo-vinery-forge-1.3.10.jar` and `doapi-1.1.0b.jar`
  in `mods/`. The 1.3.10 jar moves to `vinery_upstream/` as a resource donor (4.8).
- **What the jar holds (D13 B):** MIT code ported from upstream, our own code, and small files we write ourselves
  (shims, `mods.toml`, mixin configs). No upstream asset or data file.
- **Dependencies:**
  - Required: Forge 43.3.x, Architectury 6.6 (already in the pack). Architectury can be dropped if the port uses
    plain Forge registries.
  - Optional: JEI 11.8.1, WTHIT 5.36 (D6), Farmer's Delight (heat-source tag, D2).
  - Cloth Config is not needed: the port uses ForgeConfigSpec.

Build order. Each step should boot before starting the next:

0. **Upstream packs (4.8):** the jar check, the donor packs and the path and JSON translation. Everything after this
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
   - Blocks, items and block entities per 4.1.
   - The 1.3.10 keepers (D2).
   - The creative tab.
   - Remaps (W1, W9, W10).
3. **Barrel** (2.2): fluid level, juice types, the recipe format, the GUI with a juice gauge, hopper sides. Old
   contents conversion (W3): REMOVED 2026-10-27.
4. **Apple Press** (2.3): 2-tall block, both recipe types, GUI, old contents (W4).
5. **Grapes** (2.4):
   - Pot block entity (W5).
   - Stems and grapevine leaves.
   - Lattices + block entity + renderer (W7).
   - Bushes and the taiga soil rule.
   - Fox mixin.
   - Fruit leaves (W6).
   - Stackable-log campfire.
   - Slabs and their mixins (P17).
6. **Wine** (2.5, **4.5**): the vintage clock, `WineYear` NBT in clock ticks, storage aging hooked straight into the
   ported storage block entity (no mixin needed), tooltip, effect list setting. (`Year` conversion, W8: REMOVED 2026-10-27.)
7. **Effects** (2.5, P6). Experience orb mixin, fall-damage / jump mixins, the double-jump client handler.
8. **Entities and trades:** mule, Wandering Winemaker (the port's own spawner since 2026-10-27, I4, I5), villager profession +
   config trades (P16).
9. **Worldgen** (P15, F7). **Commands** (2.6). **Data** (F1 to F6, done in memory by the 4.8 translation). **Config** (P14).
10. **Client:** renderers (lattice, pot, storage pot, log, bottles, racks, banner), screens, colours, armor models.
    WTHIT lines (D6, 4.6) in place of the info overlay.
11. **JEI** (I3).
12. ~~**Old-world conversion pass** (W2, W3, W6) and a scratch-server boot on a copy of a 1.3.10 world.~~ REMOVED 2026-10-27 (3.3).

### 4.3 API mapping, 1.21.1 Fabric -> 1.19.2 Forge

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

### 4.4 Pack-side changes

| File | Change |
|---|---|
| patcherMod | Remove `FermentationBarrelStackOutputMixin` (I2). Trim `compat/vinery` to the stomping and aging pages (I3). Rewrite `WinemakerTrades` (I4). Repoint `WinemakerSpawner` (I5) and `StuckInBlock` (I6). Update `crop_growth.json` (I8) |
| [../kubejs/startup_scripts/_lib/item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) | I10 |
| [../kubejs/data/creativeengineering/kubejs/tags/wood_tags.js](../kubejs/data/creativeengineering/kubejs/tags/wood_tags.js), [../kubejs/server_scripts/tags/blocktags.js](../kubejs/server_scripts/tags/blocktags.js) | I11, I12 |
| [../kubejs/data/creativeengineering/kubejs/engineering/fabric.js](../kubejs/data/creativeengineering/kubejs/engineering/fabric.js), [../kubejs/server_scripts/other/misc_mods.js](../kubejs/server_scripts/other/misc_mods.js) | I13 |
| [../kubejs/server_scripts/loot/07_rules.js](../kubejs/server_scripts/loot/07_rules.js), [../kubejs/startup_scripts/compost/compost_values.js](../kubejs/startup_scripts/compost/compost_values.js) | I14, I15 |
| [../kubejs/startup_scripts/func/tool_effects.js](../kubejs/startup_scripts/func/tool_effects.js) | I16 |
| [../kubejs/assets/creativeengineering/lang/en_us.json](../kubejs/assets/creativeengineering/lang/en_us.json) | I17 |
| `config/vinery-common.toml` (new) + `configs_edited/` | `[aging]` per 4.5 (`daysPerYear` 16, `yearsPerEffectLevel` 2, `playtimeHoursPerYear` set to today's pace); `[winemaker_set]` per 4.7 (the pack's values today); `trader.spawnChance` 0 (2.7); the remaining values per D5 |
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

### 4.5 Wine aging revamp: playtime clock (decided 2026-10-14)

**2026-10-18: partly replaced.** Your 7 list asks for sleeping to age wine, ages in months (one Serene Seasons
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

### 4.6 WTHIT lines (D6, decided 2026-10-14)

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

### 4.7 Winemaker set bonus settings (D11, decided 2026-10-14)

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

### 4.8 Upstream jars as resource donors (D13 option B, chosen 2026-10-18)

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
- **Worldgen stays inside 1.19.2's feature set (owner, 2026-10-18):** no new worldgen code is backported. A 1.20+ type
  in upstream worldgen data is rewritten to the nearest 1.19.2 vanilla type. The only one is
  `minecraft:cherry_foliage_placer` (dark cherry and apple trees) -> `minecraft:fancy_foliage_placer` with the same
  `radius`, `offset` and `height` (it pairs with their `fancy_trunk_placer`, like vanilla's big oak); the hole and
  hanging-leaf chances are dropped. Biome placement uses 1.3.10's own `forge/biome_modifier` files (1.3.10 ids, so
  `wild_crops.json` keeps matching), which point at the same placed features 1.5.5 has.
- **Optional:** 1.5.5's built-in `resourcepacks/bushy_leaves` can be offered as an optional resource pack from the same
  jar.

**What changes in the plan**

- 4.2 step 0 (above) comes first. The old "copy upstream assets into the port" assumption is gone.
- F1 to F6 (3.2) are the same rules but run in Java at load time instead of in a build script like Backported
  Brewin's `port_resources.py`. Estimate: 1 to 1.5k lines for the packs, the translation and the check.
- Deploy (deploy.md): a `vinery_upstream/` category in `build_deploy.py`, `verify.sh` / `.ps1` hash lines, mod_sides
  rows for both jars, and removal of the 1.3.10 and doapi jars from `mods/`. The rest depends on D14.

**Risks**

- **Author relations:** see 3.6. The plan stays inside the license texts, not inside the author's wish.
- **Leaks into the port:** an upstream file copied into `src/main/resources` by habit would break D13 B. 6 adds a
  build check against both jars.
- **Dangling and missing art:** the 21 dangling references and any keeper without art show the missing texture. The
  first boot will list them in the log (7 VB-42).

### 4.9 Port status (2026-10-18)

`development/backportedVinery` (Gradle `:backportedVinery`, jar `backportedVinery-1.5.5-ce-19.2-01.jar`). Compiles; not
booted yet.

- **Sources:** 1.5.5's real sources (Modrinth `-sources.jar`), Foundation 1.0.1 from its GitHub tag `v1.0.1-1.21.1`,
  the 1.3.10 decompile for the D2 keepers. Kept in `DecompiledMods/` (`vinery-1.5.5-src`, `foundation-1.0.1-src`,
  `vinery-1.3.10-src`).
- **Tools:** `tools/port_java.py` (first mechanical 1.21 -> 1.19.2 pass, copies only files the port doesn't have),
  `tools/port_fixups.py` (second pass, in place), `tools/gen_at.py` (unused so far: no access transformer needed).
- **Layout:** `net.satisfy.foundation` (own mod id `foundation` in the same jar, so its ids stay upstream's),
  `net.satisfy.vinery.core|client|api` (1.5.5), `net.satisfy.vinery.legacy` (1.3.10 keepers),
  `net.satisfy.vinery.forge` (entry, config, events, villagers, network, client, `upstream/` = 4.8).
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
  aging per 4.5), D7 (Foundation bundled), D8 (no village houses), D9 (no hanging sign), D10 (rotten cherry kept,
  setting `rottenCherryChance` 0.15), D12 (bamboo racks / lattice from `quark:bamboo_planks`).
- **Done since (2026-10-18):** W2, W3, W4, W6, W8 to W10 (W2, W3, W8 to W10 removed again 2026-10-27); praetorian / king danis barrel recipes
  (`data/vinery/recipes/port/`); WTHIT lines (4.6); pack-side changes (4.4 outcome table); deploy category.
- **Fixes found by review, no boot yet:**
  - `mods.toml` declared only `vinery`. Foundation is a second `@Mod`, and FML rejects an `@Mod` class with no entry,
    so the game would not have started. Added `[[mods]] foundation` and a vinery -> foundation dependency.
  - Advancements: 1.21's location predicate `"biomes"` (also inside `entity_properties`) is now rewritten to 1.19.2's
    `"biome"`. Without that, `fruits_of_the_field` fired in any biome.
- **Data survey (2026-10-18):** every type, function, condition and trigger in 1.5.5's and Foundation's data was listed
  against what the translator handles. Nothing else needs 1.21 features. No Fabric or NeoForge load conditions, no
  item components. The 1.3.10 biome modifiers name placed features that exist in 1.5.5. The port registers every
  1.5.5 id through the 4.1 map.
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

## 5. Decisions (recommendation first; DECIDED ones say so)

### 5 D13. Upstream resources are All Rights Reserved (DECIDED 2026-10-18: B, see 4.8)

Option A was tried: Satisfy refused redistribution of assets and features (3.6). The user chose B: the pack comes
with the unmodified upstream jar, and the port reads it. How the jar gets onto each machine is D14.

3.6 explains why. The code can be ported; the textures, models, lang and data files can't be shipped as they are.

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
  - Recipes, tags, loot, lang and worldgen are small enough to write fresh from the behaviour described in 2.
  - Textures and models are the expensive part: about 170 item models plus blocks, entities and armor.
  - Only makes sense combined with B for the art.
- **D: don't port 1.5.5 as a mod.**
  - Keep the official 1.3.10 jar.
  - Bring over only behaviour that needs no new assets, using patcherMod mixins: barrel juice level, press stages, wine
    aging rules, effect changes.
  - New blocks (lattices, racks, bags, cabinet, banner) and the new effects' icons would be left out.

### 5 D14. How the upstream jars reach each machine (DECIDED 2026-10-18: B for now, A on publish)

The user's call: for now both jars travel in the deploy payload (B). When the pack is published, each machine
downloads them from the official pages instead (A).

Both jars go in `vinery_upstream/` on every client and the server (4.8).

- **A (recommended): each machine downloads them from the official pages.**
  - `verify.sh` / `.ps1` (or a small fetch step in the runbook) downloads the 1.5.5 jar from the Modrinth CDN
    (`https://cdn.modrinth.com/data/1DWmBJVA/versions/PDi0T5xs/letsdo-vinery-fabric-1.5.5.jar`) and checks the sha1.
    The 1.3.10 jar comes from CurseForge, which has no stable direct link, so that one is a manual download.
  - The pack never passes on a copy of the author's files, which is what their refusal was about.
- **B: put both jars in the deploy payload, like every other jar.**
  - Simplest. Today the pack already copies the 1.3.10 jar by hand.
  - It is a copy of their files to other machines, which is closer to what they refused.

### 5 D1. Cherry wood ids (DECIDED 2026-10-14: keep `cherry_*`)

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

### 5 D2. Blocks 1.5.5 removed (DECIDED 2026-10-14: keep every 1.3.10 block, add the new ones; see 4.1)

The decision goes further than the recommendation below: blocks that have a 1.5.5 successor stay as well.


- **Recommended:**
  - Remap where 1.5.5 has a successor (4.1).
  - Keep the 1.3.10 blocks that have no successor and that the pack uses:
    - stove and wood-fired oven: Farmer's Delight heat sources (I21).
    - kitchen sink: infinite water source.
    - flower box and flower pot: quest tasks (I20).
    - calendar, basket, loam set.
  - Keep `praetorian_wine` and `king_danis_wine`, with barrel recipes rewritten into the juice format and their 1.3.10
    effects mapped to the closest new ones.
- **Alternative:** follow 1.5.5 and drop them. Placed blocks disappear, the quests need new items, and the B&C keg
  loses two heat sources.

### 5 D3. Removed effects (DECIDED 2026-10-18: keep `trippy`, section 10.1)

- **Recommended:**
  - Keep `trippy` registered. The pack's echo "discombobulate" uses it (I16), and it's only a client shader.
  - Remap the 10 `improved_*` effects to vanilla on load (W9). Removed 2026-10-27 (3.3).
- **Alternative:** drop `trippy` and replace it in `tool_effects.js` with Nausea.

### 5 D4. Wine effects (DECIDED 2026-10-19: 1.5.5 list)

- **Recommended:** follow the 1.5.5 default list. It's one setting, so the pack can retune any wine later without
  code.
- **Alternative:** seed the setting with the 1.3.10 effects where a counterpart exists. That doesn't fully work,
  because the `improved_*` effects are gone.

### 5 D5. Balance numbers (DECIDED 2026-10-19: as recommended)

- **Aging is decided separately (4.5)**: keep the lengths (16 days per year, 2 years per level) on the new playtime
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

### 5 D6. Block info HUD (DECIDED 2026-10-14: WTHIT, see 4.6)

- **Recommended:** don't port Foundation's `BlockInfoOverlay`. Put the same lines (pot grapes and stomps, barrel juice
  and progress, rack contents) into the patcherMod WTHIT plugin, next to the Run Speed and Growth lines.
- **Alternative:** port the overlay with `mode` OFF by default.

### 5 D7. Foundation (DECIDED 2026-10-19: bundled)

- **Recommended:** bundle the Foundation subset inside the Vinery jar, keeping its packages.
- **Alternative:** a separate `foundation` mod jar. That only pays off if more Let's Do mods get backported. Vinery is
  the only one in the pack today.

### 5 D8. Village houses (DECIDED 2026-10-19: skipped; REVERSED 2026-10-20: added, section 13)

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

### 5 D9. Hanging sign (DECIDED 2026-10-19: dropped)

- **Recommended:** drop it (P10). Supplementaries already gives a Vinery cherry hanging sign.

### 5 D10. Rotten cherry (DECIDED 2026-10-19: kept)

- **Recommended:** keep 1.3.10's chance for cherry leaves to drop a rotten cherry. 1.5.5 left the item with no source
  (Q1).

### 5 D11. Winemaker set bonus (DECIDED 2026-10-14: settings for no damage or a set amount, see 4.7)

- **Recommended:** keep the 1.3.10 settings: keep chance 100, damage chance 30, damage 1 (the pack's values today).
- **Alternative:** follow 1.5.5: always keep the bone meal, and every use damages all 4 pieces by 2.

### 5 D12. Bamboo lattice and racks (DECIDED 2026-10-19: kept)

- **Recommended:** keep them, built from `quark:bamboo_planks` (P11).
- **Alternative:** drop them.

## 6. Offline checks

- **Clean port (D13 B): BUILT.** `checkNoUpstreamResources` in the port's build.gradle fails the build if any file
  under `src/main/resources` has a path that is in an upstream jar. Passes.
- **Data survey: DONE 2026-10-18 (4.9).** It replaces the dry run below for now.
- **Deploy check: BUILT.** `build_deploy.py` refuses to stage while the port is in `mods/` and a `vinery_upstream/`
  jar is missing or has another sha1 than `UpstreamJars.java` pins.
- **Translation dry run:** a headless test that opens both jars through the 4.8 packs and parses every converted
  recipe, loot table, advancement and tag with 1.19.2's own parsers; it lists anything that fails.
- A script that compares the port's `data/vinery/` tree against 1.5.5's after the F1 to F3 renames and the 4.1 id
  map. It should report only the D-decision deviations.
- An id audit: every id in `kubejs/exported/items_by_mod/vinery.json` (1.3.10) is registered in the port or appears in
  the remap table.
- A scratch-server boot on a copy of a world with 1.3.10 Vinery blocks placed (scratch-server memory). Grep the log
  for missing mappings and unknown block entities.

## 7 Changes to make (planned 2026-10-18)

Your lists from 2026-10-18, each item with a plan. **Port** means `development/backportedVinery` (rebuild the jar);
**pack** means KubeJS / tags. Upstream assets and data stay untouched (D13 B). Where wording changes, it goes in the
port's **own** lang file, which is our text and layers above the donor jars (4.8). Tests: VB-48 to VB-60 (8); the
time items have their own tests in [time.md](time.md). Questions for you: 7.12.

**No conversions (your rule, 2026-10-18):** both ports are in development, so nothing in this list keeps old items,
effects or ids working across versions. That covers old Vinery to new Vinery, and Vinery to Brewin. Removed things
are simply removed.

### 7 Build status (2026-10-18, not tested in game)

Port `backportedVinery-1.5.5-ce-19.2-02.jar` is built and in `mods/`, and `server_mods/` is regenerated. The version
went up because `-01` shipped in 1.0.7, and a rebuild under the same name wouldn't reach clients.
`config/vinery-common.toml` has the new keys and values.

| Item | Outcome |
|---|---|
| 7.1 JEI | Built. The Cask page cycles the recipe's juice bottles in slot 0 with the bottles needed (`JuiceUtil.juiceStacks`). `FermentationBarrelGui.isOverJuiceBar` is the hover box for the screen and JEI: the gauge plus its frame. The Wine Aging text wraps to 146 px in `0x404040` with no shadow, and the page height comes from the line count. **Also fixed:** the JEI transfer handler paired the page's slots in the wrong order, so ingredient 1 went to the juice slot. It now lists juice, the ingredients, then the bottle (`FermentationTransferInfo`). Transfer needs the juice bottles in your inventory |
| 7.2 Press slots | Built. `ApplePressBlockEntity.isValidForSlot` backs the menu slots (`PressSlot`), `canPlaceItem`, hoppers and shift-click. **Deviations:** slot 1 takes any fermenting-recipe input; today that is apple mash and honeycomb, the same set as "mashing outputs that fermenting takes". There is no cache: it asks the recipe manager, and there are 2 recipes per type. Honey's glass bottle needed a recipe change. Fermenting recipes now carry a `bottle` ingredient (upstream's `wine_bottle.required` still means a wine bottle), and the upstream-pack layer gives `apple_fermenting/honey_bottle` a glass bottle |
| 7.3 Merged effects | Built. `luck_effect`, `resistance_effect` and `health_effect` are no longer registered (`ResistanceEffect.java` deleted). Stal Wine defaults to `minecraft:health_boost` |
| 7.4 Effect reshuffle | PINNED, not built |
| 7.5 JEED providers | Built. Port: `WineEffects.all()` (wine -> effect) and `winesByEffect()` (effect id -> wine ids, strings only, for Rhino). Pack: `R.jeedProvider(effect, providers, e, idOverride)` in NCH, called once per effect at the end of `kubejs/server_scripts/cooking/alcohol.js` (`creativeengineering:jeed/vinery_<effect>`). NCH is a startup script, so it needs a game restart, not just `/reload` |
| 7.6 Mead out | Built. Block, item, storage lists, render layer and effect default are gone. The upstream-pack layer has a `REMOVED` set: tag entries naming `vinery:mead` are dropped, and so are advancement criteria whose item list it empties. **Found:** `wine_somelier` (the advancement that gives the banner) and `the_noble_drop` list mead. The old "leave out the whole file" rule would have dropped both, banner reward included. Now mead is taken out of their lists: 25 -> 24 criteria (checked headless against the 1.5.5 file). Villager trades never had it |
| 7.7 Banner | Built. `[banner] effect / amplifier / radius / durationSeconds / refreshBelowSeconds` (Health Boost II, 8, 60, 10). Below the threshold the running effect is extended in place with `MobEffectInstance.update` plus a `ClientboundUpdateMobEffectPacket`, never re-added, so the extra hearts aren't clamped. The tooltip line comes from the settings (`tooltip.foundation.banner.effect`) |
| 7.8 Time | patcherMod side BUILT too (patcherMod 1.0.8, [time.md](time.md) 3.8): the `[time]` master pushes `daysPerMonth`, the clock, the cellar value and the rest into `vinery-common.toml` (since 2026-10-27 the port reads them from `time.WineTime` instead, time.md 3.7); Serene Seasons calendar tooltip; "7:30 pm" clock. Port side built: `[wine] daysPerMonth` 8, `monthsPerEffectLevel` 3, `durationPerMonth` 200, `cellarAgingMultiplier` 2.5; `[aging] clockMode` WORLD_TIME, a new `dayUnit` DAYTIME (24000 clock units a day, forced on WORLD_TIME), `playtimeHoursPerMonth`. Real-time text divides by the measured day-time pace (`Snapshot.realTicks`). "Month" in the tooltip, `/wine` (literal `months`), JEI, WTHIT and the upstream keys. The Vinery calendar is removed (block, item, classes, keeper data; `i.calendar` / `i.mead` out of `item_dict.js` and the generated food / stack JSONs) |
| 7.9 Cask | Built. **Deviation:** the plan put our wording in the port's `assets/vinery/lang/en_us.json`. The jar can't carry upstream's path (`checkNoUpstreamResources`), and the order between the mod's own pack and the upstream pack isn't guaranteed. So the lines live in `src/main/resources/vinery_port/upstream_lang/en_us.json`, and the upstream-pack layer applies them on top of the merged upstream lang (checked headless). Resource packs still override. `dark_cherry_barrel` and 1.3.10's `barrel` keep their names (VQ2) |
| 7.10 Apple wine | Built (10.12 later made its size come from its per-block limit, 3 = small). `BottleSize.SMALL`. The upstream-pack layer moves `vinery:apple_wine` from `large_bottle` to `small_bottle`, in place of an own tag file (same reason as 7.9). Its block still holds 3 |
| 7.11 Praetorian | SETTLED by 10.8 (small racks, shrunk); medium since 10.12, so the 4x rack, the wine box and the single-bottle rack, drawn with its own model (no slim, 10.14) |
| 7.14 Stomping yield | Built 2026-10-19 into `-03` (in the local `mods/` since 22:27): 12 grapes, 4 stomps, 4 bottles |

### 7.1 JEI (port)

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

### 7.2 Apple Press: valid items only (port)

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

### 7.3 Merge duplicate effects (port)

| Vinery effect | What it does | Merged into | Used by |
|---|---|---|---|
| `luck_effect` | +2 luck | `minecraft:luck` | no wine |
| `resistance_effect` | +0.2 knockback resistance, +2 armor toughness | `minecraft:resistance` | no wine |
| `health_effect` | +4 max health | `minecraft:health_boost` (Health Boost I is also +4) | Stal Wine |

- **Plan:** stop registering the three (`MobEffectRegistry`), point Stal Wine's default at `minecraft:health_boost`,
  and drop their JEED description keys.
- No remap of the old ids (no-conversions rule).
- **Test:** VB-51.

### 7.4 Wine effect reshuffle: PINNED (future plan)

- Parked by you on 2026-10-18; nothing here is built in this round.
- **When it comes back:**
  - Wines may apply **two** effects. The `[wine] effects` entry then takes a list of effects.
  - Food-mod effects (Fruits Delight's 18, Neapolitan, Farmer's Delight, Farmer's Respite, Alex's Mobs and others)
    become candidates, each with a vanilla or Vinery fallback for when its mod is missing.
  - A `tools/wine_effect_candidates.py` lists every effect with its JEED description, to pick from.
- Today's one-effect list stays as is, apart from 7.3 and 7.6.

### 7.5 JEED providers for wine effects (pack, small port accessor)

- JEED lists which items give an effect from `jeed:effect_provider` recipes: `{effect, providers: [items or tags]}`
  (format from its jar, `data/jeed/recipes/*.json`).
- Wine effects come from config, so the recipes are made at load time, not written as files:
  - **Port:** `WineEffects.all()` returns the parsed map (wine id -> effect). When the 7.4 reshuffle lands, it maps
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

### 7.6 Remove Vinery's mead from the port (Brewin's covers it)

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

### 7.7 Banner: Health Boost II for 60 s (port)

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

### 7.8 Time: seasons, day length, wine months, calendar -> [time.md](time.md)

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

This replaces 4.5's "sleeping doesn't age wine" (marked there).

### 7.9 Fermentation barrel -> "Cask" (port)

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

### 7.10 Apple wine: 2 to 3 bottles per block again (port + tag)

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

### 7.11 Praetorian wine fits a wine rack again (port tag) (SETTLED 2026-10-18, section 10.8)

- **Why:**
  - Praetorian Wine (and King Danis Wine) are 1.3.10 keepers that 1.5.5 dropped (D2).
  - So they are in neither upstream bottle tag, and every rack checks those tags (big racks: `large_bottle`).
  - The port registers them as `BottleSize.BIG` with a 2-bottle block.
- **Plan:** the port's own `data/vinery/tags/items/large_bottle.json` (`replace: false`) adds both, so they fit the
  big racks. If you want them stacking 2 per placed block as in 1.3.10, they go in `small_bottle` instead and fit
  the small racks (7.12 VQ3).
- **Gap check while there:** list every wine item in neither tag. Any that turn up get the same fix.
- **Test:** VB-59.

## 7.12 Crate merging

Audited 2026-10-19 with bottle sizes and racks: [storage_audit.md](storage_audit.md).

- merge crates and bags w/ other mods versions, ensure no crates and bags are duplicated during backport


### 7.14 Grape stomping yield: 12 grapes, 4 stomps, 4 bottles (port) (BUILT 2026-10-19)

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

### 7.13 Questions for you

| # | Question | My default |
|---|---|---|
| VQ1 | Wine Aging page text colour | DECIDED 2026-10-18: dark grey `0x404040` |
| VQ2 | Cask: the 1.3.10 decorative `barrel` keeps its name? | Yes, only the wine block is renamed |
| VQ3 | Praetorian / King Danis: big racks (1 per placed block), or small racks with 2 per placed block as in 1.3.10? | DECIDED 2026-10-18 (10.8): small racks, shrunk to fit; King Danis removed |
| VQ4 | Time questions | [time.md](time.md) section 4 |

### 7.15 Next asks (scratchpad 2026-10-21)

- **Cellar mechanic:** audit how wine storage actually ages (cellar / sunny, cold / warm, 10.13) and write it up where
  players see it (guide book, tooltip or JEI). [TODO](todo/TODO.md) `alcohol/vineryport #22`.
- **JEI tab for grape planting types:** which grapes grow where. `alcohol/vineryport #23`.
- **Apple cider merge:** merge the apple ciders (Farmer's Delight, Farmer's Respite). Vinery's Apple Cider is a wine and
  alcoholic (section 11), the other two aren't, so the merge has to pick which one survives. `food/drinks #3`.

## 8. In-game test suite (drafted, run once built)

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
- [ ] **VB-19 Wine aging in a cellar:** wine in a rack with no sky light ages x2.5 (`/wine info`; was x1.5 before 7.8)
- [ ] ~~**VB-20 Aging pace:**~~ superseded 2026-10-18 by the sun clock in months (7.8; time.md TI-18, TI-19). default settings -> `/wine time` shows about 28 h per year and 16 days per year; `/wine info` on a new wine says level up after 32 days
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
- [ ] ~~**VB-33 Clock only on playtime:**~~ superseded 2026-10-18 (clock is WORLD_TIME, 7.8): dedicated server, note `/wine time`, log everyone off for 10 min, log back in -> clock moved by about 0
- [ ] ~~**VB-34 Today's pace measured:**~~ superseded 2026-10-18 (7.8): play through one full world day with `dayUnit = WORLD`, `worldDayMinutes = 0` -> the measured day length is about 106 min, close to the 28.2 h-per-year default
- [ ] ~~**VB-35 Sleep and /time don't age:**~~ reversed 2026-10-18: sleeping ages wine (time.md TI-17): `/wine info` on a wine, sleep through a night and `/time add 24000` -> the age is unchanged
- [ ] ~~**VB-36 Rescale:**~~ superseded 2026-10-18 (keys are months now, 7.8): set `playtimeHoursPerYear` 5, `daysPerYear` 15, reload -> an existing wine's age and level change at once; `/wine clock add 5h` -> exactly 1 more year
- [ ] **VB-37 Cellar:** two fresh wines, one in a rack in a dark cellar, one in a rack under sky; after the same time the cellar one is about 2.5x older
- [ ] **VB-38 Old and new blocks side by side:** place old chair / table / crate / barrel / wine_rack_1 next to their new counterparts -> both work; note any look-alikes to hide
- [ ] **VB-39 WTHIT lines:** look at a pot mid-stomp, a cask mid-ferment, a press, a rack of wines in a cellar -> lines per 4.6
- [ ] **VB-40 Winemaker set settings:** `damageMode = NONE` -> 50 bone meal uses, no durability lost; `ONE_PIECE` + `damageAmount 3` -> one piece loses 3 per damaging use
- [ ] **VB-41 Upstream jar check:** remove `vinery_upstream/letsdo-vinery-fabric-1.5.5.jar` -> client and server both stop with the file name and download page; a renamed or different jar is refused the same way
- [ ] **VB-42 Missing art sweep:** first boot, grep the log for missing models and textures under `vinery:` and `foundation:` -> only the 21 known upstream dangling references (4.8)
- [ ] **VB-43 Donor layering:** stove, kitchen sink and flower box show their 1.3.10 art; `cherry_planks` shows the 1.5.5 dark-cherry art; the lang says "Dark Cherry"
- [ ] **VB-44 Overrides still win:** the pack's `kubejs/data/vinery/worldgen/configured_feature/*` grape-bush configs are the ones in use (`/dumpall` or a locate), not upstream's
- [ ] **VB-45 Boots:** client and dedicated server start with the port and both mod ids (`vinery`, `foundation`) in the mod list; log has `[vinery-upstream] N data files ...` and no `vinery` / `foundation` errors
- [ ] **VB-46 Plains advancement:** `fruits_of_the_field` is granted on entering plains, not in other biomes
- [ ] **VB-47 WTHIT config:** the WTHIT config has a Vinery page with 5 toggles; turning one off hides only its lines; a fresh storage pot in a structure shows nothing and still has its loot when opened
- [ ] **VB-48 JEI juice slot (7.1):** a wine's Cask page shows its juice bottle in the juice slot with the needed count; looking up the uses of red grape juice lists the red wines
- [ ] **VB-49 Full-bar hover (7.1):** a quarter-full cask, hover the empty top of the gauge -> the juice tooltip; the same on the JEI page
- [ ] **VB-50 Apple press slots (7.2):** by hand, slot 1 (left) takes apple mash and honeycomb only, slot 2 wine bottles and glass bottles only; a hopper feeds glass bottles into slot 2; shift-click puts mash in slot 1; honeycomb + glass bottle -> honey bottle, and the JEI page shows the glass bottle
- [ ] **VB-51 Merged effects (7.3):** Stal Wine gives Health Boost; `/effect give @s vinery:luck_effect` fails (no such effect)
- [ ] ~~**VB-52 Effect fallbacks**~~ pinned with 7.4 (future plan)
- [ ] **VB-53 JEED providers (7.5):** the JEI page of a wine's effect lists that wine as a provider; change the config entry, `/reload` -> the provider moves
- [ ] **VB-54 Mead gone (7.6):** no `vinery:mead` in JEI, the creative tab or `/give`; no cask recipe for it; the log has no errors about it
- [ ] **VB-55 Banner Health Boost II (7.7):** stand by a Vinery banner at full health -> Health Boost II for 60 s, 4 extra hearts that stay full while you stand there for 2+ min; the effect timer only jumps back up when it falls under 10 s; walk away -> it runs out within 60 s
- [ ] **VB-56 Months everywhere (7.8, [time.md](time.md) 3.7):** wine tooltip, `/wine info`, `/wine time`, the JEI Wine Aging page, WTHIT and the config screen all say months, none say years
- [ ] **VB-57 Cask name (7.9):** the block, its item, its GUI title, the JEI category, tooltips and WTHIT all say Cask; the decorative barrel and the dark-cherry chest barrel keep their names
- [ ] **VB-58 Apple wine stacks (7.10):** right-click a placed apple wine with two more -> 3 bottles in one block; it fits a small wine rack
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
- [ ] **VB-60 Wine Aging page text (7.1):** every line stays inside the grey page, wrapped, in dark text without shadow
- [ ] **VB-61 JEI transfer (7.1):** with juice bottles, ingredients and a wine bottle in your inventory, click [+] on a Cask page -> juice in slot 0, ingredients in 1-3, the bottle in 4; without the juice bottles the [+] is red
- [ ] **VB-62 Calendar gone (7.8):** no `vinery:calendar` in JEI, the creative tab or `/give`; the log has no recipe or loot errors about it
- [ ] **VB-63 JEED providers move (7.5):** after editing `[wine] effects` and `/reload`, the JEI page of the old effect no longer lists the wine

## 9. History

- 2026-10-14: migration analysis of Vinery 1.3.10 (Forge) -> 1.5.5 (Fabric 1.21.1) from decompiles of both jars and
  of Foundation 1.0.1. 75 issues in 3, 12 decisions open in 5, VB-01 to VB-32 drafted.
  Found:
  - The 65 cross-mod cherry items that depend on the wood name (W11).
  - The dead village structures (Q5).
  - The orphaned rotten cherry (Q1).
  - The license is "Custom", not MIT (I1).
  - The pack's barrel stacking mixin is now redundant (I2).
- 2026-10-14: added the upstream license text (3.6): code MIT, resources All Rights Reserved. Shipping upstream
  assets or data needs permission. Added D13 (ask Satisfy, read the player's own copy of the upstream jar, write our own
  resources, or behaviour-only mixins); 13 decisions open.
- 2026-10-14: decided:
  - D1: keep `cherry_*`.
  - D2: keep every 1.3.10 block and id and add the new ones. Renamed things keep their old id with the new
    behaviour; replaced things exist side by side. 4.1 rewritten.
  - D6: WTHIT lines (4.6).
  - D11: winemaker set damage set in config, including none (4.7).
  - Wine aging is redesigned onto a playtime vintage clock with a configurable real-time scale, a `WORLD` day unit for
    Better Days, and ages that re-scale when the settings change (4.5). Lengths are unchanged for now
    (16 days per year, 2 years per level, about 28.2 h per year).
  - Tests VB-33 to VB-40 added; VB-20, VB-27 and VB-29 updated.
- 2026-10-18: Satisfy refused redistribution of assets and features (3.6). The user chose D13 B: ship the
  unmodified upstream jar and have the port read it. Feasibility checked (4.8):
  - FML 43 rejects the Fabric jar in `mods/`, so both upstream jars go in their own folder.
  - 1.5.5's 418 models need nothing from 1.21; their only outside parents are Foundation templates in the nested jar.
  - 31 kept blocks need the 1.3.10 jar as a second donor; 20 `cherry_*` need an alias to `dark_cherry_*`.
  - Data needs F1 to F6 in memory.
  - Foundation's license is the same split as Vinery's.
  - Modrinth hosts the same 1.5.5 jar and its sources.
  Added D14 (how the jars reach each machine), 6 checks, VB-41 to VB-44.
- 2026-10-18: implemented. D14 decided (upstream jars in the deploy payload for now, downloaded from source on
  publish). User: wrap 1.20+ worldgen into 1.19.2's types, no worldgen backports (cherry foliage placer -> fancy).
  The port builds (4.9). Pack-side changes done (4.4 outcome table). WTHIT lines built in the port (4.6). Deploy
  category and mod_sides done. Review fixes without a boot: missing `foundation` mods.toml entry (would not start),
  advancement biome predicate. The jar swap in `mods/` waits for the game to be closed. Tests VB-45 to VB-47 added;
  VB-28 updated.
- 2026-10-18: planned your "changes to make" list (7): JEI juice slot and full-bar hover, recipe-driven apple press
  slots, luck / resistance / health effects merged into vanilla, a fallback syntax for wine effects plus a candidates
  tool, JEED providers from the live config, Vinery mead retired into Brewin's, and a configurable banner effect
  (Health Boost, with the re-apply gotcha). The time items (calendar, months, sleep ages wine, x2.5 dark) moved to
  [time.md](time.md) 3.7; 4.5 marked. VB-48 to VB-56 added.
- 2026-10-18: your second list, folded into 7 and section 8:
  - No conversions between versions or between the two ports. Vinery mead is removed from the port; AL-65 retired.
  - Effect reshuffle pinned for later, with two effects per wine possible (7.4, VB-52 pinned).
  - New items: Cask rename (7.9), apple wine stacking (7.10), Praetorian / King Danis in racks (7.11), JEI Wine
    Aging text wrapped in dark text (7.1). 7.2 now says the left press slot accepts nothing today.
  - Banner: Health Boost II, 60 s, refreshed under 10 s.
  - Keg pipe rule made a setting with a tooltip line; ham and cheese table confirmed (Q6, Q7 decided).
  - Time values settled in [time.md](time.md) 3.7. VB-57 to VB-60 added.
- 2026-10-18: 7 built (not tested in game), apart from 7.4 (pinned) and 7.11 (on hold). Port `-02` in `mods/`,
  `server_mods/` regenerated, `config/vinery-common.toml` moved to the new keys. Built with 7.8: the port side of
  [time.md](time.md) 3.7 (months, sun clock with a DAYTIME day, x2.5 dark, calendar removed). Changes from the plan:
  - Cask wording and the apple-wine tag move go through the upstream-pack layer, not files at upstream's paths.
  - The press's slot 1 takes any fermenting input.
  - Honey's glass bottle needed a per-recipe `bottle` ingredient.
  Found and fixed:
  - Dropping every file that names mead would have dropped `wine_somelier`, the banner advancement. Mead is taken
    out of its criteria instead.
  - The JEI Cask transfer paired slots in the wrong order.
  VB-20 and VB-33 to VB-36 superseded by the sun clock. VB-61 to VB-63 added.
- 2026-10-18 evening: the patcherMod half of 7.8 built ([time.md](time.md) 3.8, patcherMod 1.0.8 in `mods/`).
  `i.mead` and `i.calendar` removed from `item_dict.js`, and `mead` from the generated placeable, stack-size and
  food-type lists. Every other port item was already mapped.
- 2026-10-19: `-01` (with the grass slab hotfix) had gone out in pack 1.0.7, DEPLOYED 2026-10-18 19:43. `-02` (7,
  section 10) STAGED in pack 1.0.8 ([plan_1.0.8.md](plan_1.0.8.md) VB). On the live world it removes `vinery:mead`,
  `vinery:king_danis_wine` and `vinery:calendar` (all present in `-01`). Next build bumped to `-03`.
- 2026-10-19: `-02` DEPLOYED with pack 1.0.8 (10:20, server + clients). VB-01 to VB-66 pending.
- 2026-10-19: 7.14 built into `-03`: grape pot 12 grapes, 4 stomps, 4 bottles (TODO alcohol-decisions #4 option A).
  VB-10 rewritten. The jar is in `build/libs/`; the game had `-03` open, so it isn't in `mods/` yet.
- 2026-10-19: 10.13 built into `-03` (cold / cellar / cold cellar storage, rack tooltips, network version 2) with
  patcherMod 1.0.10 (`[time.wine.boosts]` cellar 2.0, cold 2.0). VB-76 to VB-79 added. Same day: warm x0.75 and the
  sunny x1.0 label added; VB-76 to VB-78 rewritten, VB-80 added. `build/libs/` only: the game
  was running.
- 2026-10-19: 10.14 built into `-03` (Stal, Sun-kissed, Magnetic medium; slim 4x rack / wine box models; Magnetic box
  look in Strad's shape). VB-81 to VB-83 added. The recolour then moved in memory (`SlimBottles`, no upstream-derived
  art in the jar). `build/libs/` only: the game had `-03` open.
- 2026-10-19: pack 1.0.9 DEPLOYED (15:54) with `-02` unchanged. Late: `-03` (7.14, 10.9 to 10.14, in-memory slim
  bottles) and patcherMod 1.0.10 in the local `mods/` (22:27); Brewin' `-03` not yet, so VB-76 to VB-80's cold and warm
  labels need it installed first. Statuses in 0, section 10, 7 and the older rack tests (VB-59, VB-71, VB-73,
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
