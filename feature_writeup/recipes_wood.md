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
| Removed woods: Upgrade Aquatic's river wood merged into oak; its tree is the Mulberry tree (oak trunk, mulberry leaves and vines) | BUILT 2026-10-22 (KubeJS + datapack; patcherMod `MulberrySapling` in the `mods/` jar of 11:06, md5 b5f60533, and the newer `build/libs` one; not in `server_mods/`); not deployed; built, scratch server PASSED 2026-10-22 (SQ-MB-00 to SQ-MB-04, second bulk run); RW-15, RW-16, RW-18 to RW-20 in game |

- **Pending:** removed woods (2.6) built 2026-10-22, needs a restart; scratch server PASSED 2026-10-22 (SQ-MB, `tools/farm_worldgen_probe.js`); RW-15, RW-16, RW-18 to RW-20 in game. RW-01 to RW-10 have never been run. Findings F1 to F6 (section 4); none needs
  a decision, F1 is a bug.
- **Files to open:** [bark.js](../kubejs/server_scripts/wood/bark.js), [sawdust.js](../kubejs/server_scripts/wood/sawdust.js),
  [wood_types.js](../kubejs/server_scripts/wood/wood_types.js), [ash.js](../kubejs/server_scripts/wood/ash.js),
  [removed_woods.js](../kubejs/startup_scripts/_lib/removed_woods.js) (+ [server side](../kubejs/server_scripts/wood/removed_woods.js)).
- **Knobs:** CEConfig `[worldgen.systems] mulberrySapling` (2.6). Server scripts (`/reload`); removed woods need a restart.

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
blossom, ancient, azalea, Upgrade Aquatic driftwood (2026-10-23: Abnormals Delight's cutting-board and Create's compat
saw recipes replaced, IW driftwood bark); bamboo on the saw only. Reattach (shapeless bark + stripped log → log, bark + stripped wood →
wood, bark + log → wood) for mangrove, twisted, blossom, ancient, azalea and bamboo, the species that had it before.
Create's, Farmer's Delight's, Quark Delight's and Abnormals Delight's (driftwood) own stripping recipes are removed; FD's tree bark → paper is removed.

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
slab, log, door and (since 2026-10-23) stairs sawmill recipes for the Quark three are removed (F5); its bookshelf
recipes stay.

### 2.5 Soot and torches (`ash.js`)

- Sooty Chimneys: scraping a dirty chimney with an axe gives Immersive Weathering soot: 75 % for brick, cobblestone, mud
  brick and stone brick chimneys, 50 % for copper, iron and terracotta (the mod's own recipes replaced).
- 12 torches from animal fat over a stick; Delightful's torch from animal oil bottle removed.

### 2.6 Removed woods (`removed_woods.js`)

A removed wood is merged into oak with the item merge system ([item_merges.md](item_merges.md)): every block, item and
other mod's variant of it is listed as purged id -> oak twin in `startup_scripts/_lib/removed_woods.js`
(`global.removedWoods`), which joins `global.itemMerges` and `global.removedItems`. So the purged items are hidden in
JEI, lose their tags, drop their oak twin as loot (old placed blocks too), and turn into it in a player's inventory.
`server_scripts/wood/removed_woods.js` first removes every recipe that makes a purged item (one OR-filter pass), so the
merge's input swap is all that runs on the rest and no duplicate oak recipes appear. Reuse it for the next wood
(driftwood, C&C azalea): one more key in `global.removedWoods`.

**River (Upgrade Aquatic), 2026-10-22, recipes/wood #3.** 92 ids: UA's 28 (logs, woods, planks and shapes, boards,
bookshelf, ladder, chests, posts, beehive, 4 boats), Every Compat's 57 (Another Furniture, Architect's Palette,
Beautify, Create windows, Decorative Blocks, Handcrafted, Lightman's Currency, Quark, Storage Drawers, Twigs),
Dramatic Doors' 2, Supplementaries' 2, Supp Squared's item shelf, Abnormals Delight's cabinet and Immersive
Weathering's bark. Twins: the same shape in oak from the same mod (Every Compat's per mod); bookshelf, ladder,
beehive, UA's chests -> vanilla; UA boards -> Architect's Palette oak boards; the large and furnace boats -> Boatload's
oak ones; the item shelf -> Supplementaries' (oak). Every Compat keeps registering the river blocks
(`config/everycomp-wood_types.toml` unchanged), so worlds that hold them don't lose blocks.

The tree stays and is the **Mulberry tree**: river leaves, sapling, leaf pile, carpet, hedge and Immersive Weathering's
leaf pile are renamed Mulberry (`namechanges.js`; the potted sapling in `name_rules.json`). Its trunk is oak:
`kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json` overrides UA's configured feature (UA's
`RIVER_TREE_CONFIG` with an oak log trunk: straight trunk 3+1+1, blob leaves radius 0). UA's placement
(`trees_river`, every `#minecraft:is_river` biome) is overridden by
`kubejs/data/upgrade_aquatic/worldgen/placed_feature/trees_river.json`: 8 tries per chunk (UA: 1, plus 2 in half the
chunks, about 2), same filters (dry column, OCEAN_FLOOR heightmap, biome), and its feature still hangs mulberry vines under 1 leaf in 6
(they need leaves or logs above, so oak is fine). The sapling: UA's `RiverTreeGrower` returns the built-in holder, which
the datapack doesn't reach in 1.19.2, so patcherMod `MulberrySapling` swaps it on Forge's `SaplingGrowTreeEvent` for
the datapack copy. Switch `[worldgen.systems] mulberrySapling` (off: UA's tree with river logs, which drop oak). The
mulberry hedge's recipe read `#upgrade_aquatic:river_logs` (empty now) and is re-added with an oak log (x2).

**Finding (scratch run 2026-10-22, SQ-MB-04): river trees almost never generate.** The override works through the
holder (UA's `trees_river` grew 90 river leaves, 21 oak logs, 0 river logs), but worldgen placed no river tree at three
sampled rivers: the feature needs dry land inside the river biome, and the samples had none (0 dry columns in 25 x 25).
Natural Mulberry trees are therefore rare (UA's placement, not the merge). Since then the try count is 4x (above); river biomes with no
dry land still get none, since the tree needs an open, dry column on sapling ground.

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
  FIXED 2026-10-27: `R.axeScraping(..., { soot: true })` and `R.remove`, chimneys in the item dict (pack_hygiene.md 14, PH-05).
- **F3. Define-once:** `ash.js` defines `i.any_animal_fat` locally (not in `item_dict.js`); `item_dict.js` itself defines
  `i.mulch_brown` four times and `i.any_bark` twice (same values).
- **F4. Twisted wood:** its cutting-board recipe is added without removing an original ("the original also had a bug
  here"), and it has no sawmill removal (`sawmill: false`); check JEI for a duplicate. Jar check 2026-10-21: no mod
  in the pack ships a twisted cutting-board recipe and IE has no twisted sawmill recipe, so there is no duplicate; the
  real one is IE's 7 vanilla `stripped_<x>_log` sawmill recipes next to the pack's identical ones
  ([briefs/recipes_wood.md](briefs/recipes_wood.md) #1).
- **F5.** FIXED 2026-10-23 (TODO `recipes/wood #2`): stairs removal added, driftwood bark done, comments rewritten;
  the `sawdust.js` note stays with `farming/composting #2`. **Leftover comments:** `wood_types.js` "(whatever the 4th line is below this)" (it is Quark Engineering's
  `sawmill/<name>/<name>_stairs`, still not removed); `sawdust.js` "Integrate nulch,
  move to compost file" ([composting.md](composting.md) F4); `bark.js` "Replace FD bark recipes with IW" only removes
  paper.
- **F6. Sawdust everywhere:** every wooden item in the shape tags crushes to sawdust, so any modded stairs or fence in
  `#minecraft:wooden_*` does too; intended.

### Known limits

- **Bark only 50-75 %** per log by machine; the axe in hand (vanilla stripping) gives none.

### Rejected options

- **FD's generic tree bark:** one bark item per species matches Immersive Weathering's blocks and the reattach recipes.

### Planned from the scratchpad (2026-10-24)

- [TODO](todo/TODO.md) `recipes/wood #4`: any tree bark ferments into leather (new recipes). Open: machine (Vintage Pickling Jar,
  recommended, or the Brewin' keg) and the bark count per leather. Plan: [recipes_wood](briefs/recipes_wood.md).
- [TODO](todo/TODO.md) `recipes/materials #7`: ferns count as grass, tall ferns as tall grass, and leaves match in many crafts.
  Today only the `forge:mulch/green` tag (grass, tall grass, fern, large fern; `wood_tags.js`) treats them alike. Open: which crafts.
  Plan: [recipes_materials](briefs/recipes_materials.md) `materials #7`.

## 5. File map

- [server_scripts/wood/](../kubejs/server_scripts/wood/): `bark.js`, `sawdust.js`, `wood_types.js`, `ash.js`,
  `removed_woods.js`.
- Removed woods: [startup_scripts/_lib/removed_woods.js](../kubejs/startup_scripts/_lib/removed_woods.js) (lists; keys in
  `item_dict.js` section "removed woods"), `kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json`,
  patcherMod `worldgen/MulberrySapling.java` (+ `CEConfig` / `WorldgenConfig` `mulberrySapling`), names in
  `client_scripts/namechanges.js`.
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
- [ ] **RW-15 River set gone [game: JEI and names; the startup log lines: scratch server PASSED 2026-10-22, SQ-MB-00, 92 purged, no recipe makes one]:** JEI search `river` → only Mulberry Leaves, Mulberry Sapling, Pile of Mulberry Leaves,
  Mulberry Leaf Carpet, Mulberry Hedge, Mulberry Leaf Pile (IW); the hedge recipe takes river leaves over an oak log (x2).
  The startup log shows `[removedWoods] 1 woods merged into oak` and no `[item_dict] Missing` line for river or oak ids.
- [ ] **RW-16 Mulberry tree [feature: scratch server PASSED 2026-10-22, SQ-MB-01, SQ-MB-04 through the holder / game: new chunks rarely have a river tree, see the 2.6 finding]:** `/place feature upgrade_aquatic:river_tree` on grass → oak trunk, mulberry leaves, some
  mulberry vines; new chunks along a river show the same trees, about 4x as many as before on river banks (8 tries per
  chunk); `zfw rivertrees R` on a river with dry banks places some.
- [x] **RW-17 Mulberry sapling [scratch server PASSED 2026-10-22, SQ-MB-02, switch off gives river logs]:** bone-meal a Mulberry Sapling → the oak-trunk tree (patcherMod `MulberrySapling`).
- [ ] **RW-18 Old river items [block drops: scratch server PASSED 2026-10-22, SQ-MB-03, x5 each / game for the inventory swap]:** `/setblock ~ ~ ~ upgrade_aquatic:river_log` and break it → an oak log;
  `/give @s upgrade_aquatic:river_planks 4` → 4 oak planks in the inventory; same for an Every Compat one
  (`everycomp:hc/upgrade_aquatic/river_chair` → Handcrafted oak chair).
- [ ] **RW-19 Mulberry names [game]:** the river leaves, sapling, potted sapling, leaf pile, carpet, hedge and the IW
  leaf pile show as Mulberry (Mulberry Leaves, Mulberry Sapling, ...) in hand, JEI and WTHIT; no "River" name left.
- [ ] **RW-20 Hedge recipe [scratch server PASSED 2026-10-22, SQ-MB-00, one recipe, oak log, x2 / game]:** JEI shows one Mulberry Hedge recipe: Mulberry Leaves over an
  Oak Log -> 2; `upgrade_aquatic:river_hedge` (the old recipe id) is gone.
- [ ] **RW-21 Driftwood bark:** a driftwood log on the cutting board with an axe → stripped driftwood log, about half
  the time Driftwood Bark (no Tree Bark); driftwood (wood) → 75 %; the same on the Create saw. JEI shows no Abnormals
  Delight driftwood cutting recipe.
- [ ] **RW-22 Quark stairs:** JEI → blossom planks stairs → U: no Quark Engineering sawmill recipe back to planks.

## 8. History

- By 2026-09-27 (initial git commit): the folder as it is; shipped with 1.0.3. Unchanged in git since.
- 2026-10-18: this writeup; findings F1 to F6; tests RW-01 to RW-10. No code changed.
- 2026-10-19: F1 fixed, `sawdust.js` crushes `i.any_planks` in the IE crusher ([plan_1.0.9.md](plan_1.0.9.md)). RW-03 checks it.
- 2026-10-22: removed woods (2.6), river wood merged into oak, the river tree renamed Mulberry with an oak trunk
  (datapack + patcherMod `MulberrySapling`). Tests RW-15 to RW-18.
- 2026-10-22 (doc audit): jar state corrected (`MulberrySapling` is in the `mods/` jar since 11:06); all 92 river ids
  in `item_dict.js` are in the removed list (checked); tests tagged scratch / game; RW-19, RW-20 added; scratch
  probe `tools/farm_worldgen_probe.js` (`zfw mulberry`, `zfw loot`, `zfw count`).
- 2026-10-22 (second bulk run): SQ-MB-00 to SQ-MB-04 PASS on the scratch server; RW-17 done. SQ-MB-04 found that
  river trees almost never generate (no dry land in the sampled river biomes, 2.6 finding); probe `zfw rivertrees R` added.
- 2026-10-22: `trees_river` placed feature overridden, 8 tries per chunk (was about 2), 4x the Mulberry trees by rivers.
- 2026-10-23: F5 fixed (`recipes/wood #2`): Quark Engineering stairs sawmill recipes removed, driftwood added to
  the bark species. Tests RW-21, RW-22. Not run in game.
- 2026-10-24: planned `recipes/wood #4` and `recipes/materials #7` added; no code changed.
