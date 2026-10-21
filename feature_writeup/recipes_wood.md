# Wood recipes: bark from stripping, sawdust, ash, mulch soils, saw shapes, soot

Part of backlog row 20 (recipe progression), split per folder. This one covers `kubejs/server_scripts/wood/`. The IE
Sawmill's per-blade recipes (which replace IE's own log and plank sawmill recipes) are in
[engineering.md](engineering.md) 2.1; the composter table in [composting.md](composting.md).

## 0. At a glance

| Part | Status |
|---|---|
| Bark: stripping a log on the Create saw or Farmer's Delight cutting board gives that species' Immersive Weathering bark; bark reattaches for 6 species | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Sawdust (IE wood dust) from logs, planks, stairs, fences, slabs and Create scrap wood; ash, creosote sawdust, wood pellets, mud → dirt | DEPLOYED; IE plank crushing missing (F1) |
| Compost, fertile soil and nulch from mulches and bark | DEPLOYED |
| Create saw: planks → every wood shape for 13 species; IE sawmill door and stair recipes removed | DEPLOYED |
| Soot from Sooty Chimneys scraping (75 % / 50 %); torches ×12 from animal fat | DEPLOYED |

- **Pending:** nothing built and unshipped. RW-01 to RW-10 have never been run. Findings F1 to F6 (section 4); none needs
  a decision, F1 is a bug.
- **Files to open:** [bark.js](../kubejs/server_scripts/wood/bark.js), [sawdust.js](../kubejs/server_scripts/wood/sawdust.js),
  [wood_types.js](../kubejs/server_scripts/wood/wood_types.js), [ash.js](../kubejs/server_scripts/wood/ash.js).
- **Knobs:** none. Server scripts (`/reload`).

## 1. Overview

Wood is the first resource every player processes, and the mods each have their own idea of it: Create's saw strips logs
and cuts planks, Farmer's Delight strips logs with an axe on the cutting board and gives generic tree bark, IE's sawmill
does both and gives sawdust, Immersive Weathering adds a bark item per species. The pack routes all of them to one set
of by-products: species bark from stripping, IE sawdust from crushing, and soil and fuel made from those.

## 2. Reference

### 2.1 Bark (`bark.js`)

| Machine | Log → | Wood → |
|---|---|---|
| Create saw | stripped log + 50 % bark | stripped wood + 75 % bark |
| Farmer's Delight cutting board (axe) | stripped log + 50 % bark | stripped wood + 75 % bark |
| Cutting board, Quark hollow log | | that species' bark |
| IE sawmill | [engineering.md](engineering.md) 2.1 | |

Species: oak, spruce, birch, dark oak, acacia, jungle, mangrove, crimson, warped, Architect's Palette twisted, Quark
blossom, ancient, azalea; bamboo on the saw only. Reattach (shapeless bark + stripped log → log, bark + stripped wood →
wood, bark + log → wood) for mangrove, twisted, blossom, ancient, azalea and bamboo, the species that had it before.
Create's, Farmer's Delight's and Quark Delight's own stripping recipes are removed; FD's tree bark → paper is removed.

### 2.2 Sawdust and what it makes (`sawdust.js`)

| Input | Create crushing (also millstone) | IE crusher |
|---|---|---|
| Any log, large scrap wood | 2 + 50 % | 3 (logs: see F1) |
| Planks | 1 + 50 % | none (F1) |
| Stairs, fences, medium and small scrap wood | 1 + 25 % | 2 |
| Slab | 75 % | 1 |

- Sawdust or creosote sawdust → smelt to ash.
- 3 mud + sawdust → 4 dirt.
- Creosote sawdust: spout or Bottler, 250 mB creosote per sawdust.
- Wood pellets: 4 sawdust + 4 creosote sawdust → 4 by hand, 6 by mixing.
- Create's scrap wood crushing and FD's compost from rotten flesh and from tree bark are removed.

### 2.3 Soils

| Output | By hand | By mixing |
|---|---|---|
| Compost ×1 | dirt + 3 brown mulch (or bark) + 3 bone mulch + 2 waste mulch | dirt + 2 brown mulch (or bark) + 2 bone + 2 waste |
| Fertile soil ×6 | 3 rich soil + one of each mulch (brown or bark, bone, waste, green, fungus, fertilizer) | 2 rich soil + the same |
| Fertile soil ×3 | 3 rich soil + 3 of one base mulch (brown or bark, bone, waste) + green, fungus, fertilizer | 2 rich soil + the same |
| Nulch block ×6 (Immersive Weathering) | 3 netherrack, 2 scales, ash, magma cream, nether wart, blaze powder | 2 netherrack, 1 scale, the rest |

Immersive Weathering's mulch block and nulch block recipes are removed. The mulches are `#forge:mulch/*`.

### 2.4 Saw shapes (`wood_types.js`)

The Create saw cuts planks into every shape for the 13 species: vertical planks, stairs, fence, fence gate, door,
trapdoor, ladder, button, pressure plate, sign, railing (1 each), slab and vertical slab (2 each). Shapes a species lacks
are `i.na` in the table and skipped. IE's sawmill door and stairs recipes for the 9 vanilla species and Quark Engineering's
slab, log and door sawmill recipes for the Quark three are removed (its stairs recipes are not: F5).

### 2.5 Soot and torches (`ash.js`)

- Sooty Chimneys: scraping a dirty chimney with an axe gives Immersive Weathering soot: 75 % for brick, cobblestone, mud
  brick and stone brick chimneys, 50 % for copper, iron and terracotta (the mod's own recipes replaced).
- 12 torches from animal fat over a stick; Delightful's torch from animal oil bottle removed.

## 3. What the pack changes

Species tables (`species`, `compatSpecies`) drive `R.bulk('remove' | 'createSaw' | 'fdCutting' | 'shapeless', ...)`. Ids
are NCH's generated ones, so they ignore counts (F1).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** FIXED 2026-10-19 ([plan_1.0.9.md](plan_1.0.9.md)). **IE crusher: planks have no recipe and logs probably give 2, not 3.** `sawdust.js` line 28 is
  `R.ieCrushing(Item.of(i.sawdust, 2), i.any_log, e)` where the pattern (and the Create line above it) says planks. NCH ids
  ignore counts, so both log recipes get the same id and the later one (2) most likely replaces the 3. Fix: `i.any_planks`.
  Check with RW-03.
- **F2. Soot scraping is raw code:** `ash.js` uses `e.remove` and `e.custom({type: 'sootychimneys:soot_scraping', ...})`
  directly; an NCH helper would fit the rule. The file is named for ash but holds soot and torches.
- **F3. Define-once:** `ash.js` defines `i.any_animal_fat` locally (not in `item_dict.js`); `item_dict.js` itself defines
  `i.mulch_brown` four times and `i.any_bark` twice (same values).
- **F4. Twisted wood:** its cutting-board recipe is added without removing an original ("the original also had a bug
  here"), and it has no sawmill removal (`sawmill: false`); check JEI for a duplicate. Jar check 2026-10-21: no mod
  in the pack ships a twisted cutting-board recipe and IE has no twisted sawmill recipe, so there is no duplicate; the
  real one is IE's 7 vanilla `stripped_<x>_log` sawmill recipes next to the pack's identical ones
  ([briefs/recipes_wood.md](briefs/recipes_wood.md) #1).
- **F5. Leftover comments:** `wood_types.js` "(whatever the 4th line is below this)" (it is Quark Engineering's
  `sawmill/<name>/<name>_stairs`, still not removed); `sawdust.js` "Integrate nulch,
  move to compost file" ([composting.md](composting.md) F4); `bark.js` "Replace FD bark recipes with IW" only removes
  paper.
- **F6. Sawdust everywhere:** every wooden item in the shape tags crushes to sawdust, so any modded stairs or fence in
  `#minecraft:wooden_*` does too; intended.

### Known limits

- **Bark only 50-75 %** per log by machine; the axe in hand (vanilla stripping) gives none.

### Rejected options

- **FD's generic tree bark:** one bark item per species matches Immersive Weathering's blocks and the reattach recipes.

## 5. File map

- [server_scripts/wood/](../kubejs/server_scripts/wood/): `bark.js`, `sawdust.js`, `wood_types.js`, `ash.js`.
- Related: [engineering.md](engineering.md) (sawmill), [composting.md](composting.md) (composter, mulch in the table).

## 6. Offline checks

None.

## 7. In-game test suite

- [ ] **RW-01 Saw bark:** an oak log on the Create saw → stripped oak log, about half the time oak bark.
- [ ] **RW-02 Cutting board:** a spruce wood on the cutting board with an axe → stripped spruce wood + spruce bark 3 times
  in 4.
- [ ] **RW-03 IE crusher (F1):** an oak log → note 2 or 3 sawdust; oak planks → note whether accepted.
- [ ] **RW-04 Crushing wheels:** 10 planks → about 15 sawdust; a millstone takes them too.
- [ ] **RW-05 Reattach:** mangrove bark + stripped mangrove log → mangrove log; oak bark + stripped oak log → nothing.
- [ ] **RW-06 Creosote sawdust:** spout 250 mB creosote on sawdust → creosote sawdust; mix 4 + 4 → 6 wood pellets.
- [ ] **RW-07 Soils:** mixer recipe for compost and fertile soil ×6 → as 2.3.
- [ ] **RW-08 Saw shapes:** blossom planks on the saw with a filter → each shape in JEI, slabs ×2.
- [ ] **RW-09 Soot:** scrape a dirty brick chimney with an axe 20 times → about 15 soot.
- [ ] **RW-10 Torch:** animal fat over a stick → 12 torches.

## 8. History

- By 2026-09-27 (initial git commit): the folder as it is; shipped with 1.0.3. Unchanged in git since.
- 2026-10-18: this writeup; findings F1 to F6; tests RW-01 to RW-10. No code changed.
- 2026-10-19: F1 fixed, `sawdust.js` crushes `i.any_planks` in the IE crusher ([plan_1.0.9.md](plan_1.0.9.md)). RW-03 checks it.
