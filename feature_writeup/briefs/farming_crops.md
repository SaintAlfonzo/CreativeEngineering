# Brief: farming / crops

Written 2026-10-21 for the open `farming/crops` rows of [TODO.md](../todo/TODO.md). Reference writeup:
[crops.md](../crops.md) (test prefix **CR-**, last used CR-68; new tests below are numbered from CR-69, take the next
free number when you add them). Layout: [README.md](README.md).

**Verified against jars** (downloaded and read or decompiled 2026-10-21): Quark 3.4-418 (`SimpleHarvestModule`),
Immersive Weathering 1.2.10 (`WeedsBlock`, `block_growths/weeds*.json`, `BlockGrowthHandler`, `AreaCheck`, tags),
Farmer's Respite 2.0 (`SmallTeaBushBlock`, `TeaBushBlock`), Fruits Delight 0.5.11 (`PineappleBlock`,
`WildPineappleBlock`, `pineapple` loot, `pineapple_grows_on` tag), IE 9.2.4 (`HempBlock`, `hemp` loot), Rustic
Delight 1.3.0 (`bell_peppers` and `wild_bell_peppers` loot), Myrtrees 1.2.0 (`rubberwood_leaves` loot), Serene
Seasons 8.1.0.24 (`ModFertility`, `SeasonalCropGrowthHandler`, its tags), the `sereneseasons` tags shipped by FD,
Delightful, Corn Delight, Fruits Delight, Rustic Delight and Supplementaries, Fantasy Furniture 6.7.0 (model names),
Pickle Tweaks 7.0.3 (not in the pack; `WateringCanItem`, `mods.toml`). The in-tree Vinery port was read from
`development/backportedVinery`. Anything else is marked unverified.

## Scope and order

| Row | Change | Work | Group |
|---|---|---|---|
| crops #6 | Apples only from apple trees (rubberwood leaves) | KubeJS, 1 line | A: loot quick wins |
| crops #9 | Bell pepper colours, even split | KubeJS loot | A |
| crops #3 | Harvest / replant audit for every crop | KubeJS dump + tool | B: audit first |
| crops #1 | G8, G9/G13, G12, G15 (G14 reject) | data + patcherMod | C: one patcherMod build |
| crops #2 | Parity for hemp, grapes, flax, corn, tea, pineapple, weeds, coffee | data + patcherMod + Vinery port | C |
| crops #4 | Weeds behaviour, none on rich soil | data + patcherMod | C |
| crops #5 | Realistic growth seasons | KubeJS tags + config | D |
| crops #8 | Watering can | patcherMod item (or a mod) | E |
| crops #10 | Melon and pumpkin growth stages | patcherMod blocks + models | E |
| crops #11 | Extra stem fruit (up to 4 per stem) | patcherMod | built 2026-10-22, see "Built, needs play" |

Suggested order: **A** (two small loot edits, one `/reload`), then **B** (the audit gives #1/#2/#4 their checklist and
catches crops this brief misses), then **C** in one patcherMod build (#1, #2 and #4 share `CropParity`, the Quark
mixins and the "Crop parity" tag section; G8 is also #4's main fix), then **D**, then **E** (new content; #8 and #10
are independent of each other). `cooking/kitchen #10` (scythe, area harvest by tier) touches the same Quark hoe code as
C; keep the scythe out of C but reuse `HoeHarvestArea` when it is built.

---

## crops #6: Apples only from apple trees

Status Testing (built 2026-10-21, rubberwood added; full loot dump scanned, no other hits) · Work KubeJS · TODO `farming/crops #6`

### Goal
No apples from leaves other than apple trees. Oak and dark oak are done; Myrtrees rubberwood leaves still drop them
(V17).

### Now
- [kubejs/server_scripts/loot/08_leaf_apples.js](../../kubejs/server_scripts/loot/08_leaf_apples.js) lines 18-21:
  `TABLES = ['minecraft:blocks/oak_leaves', 'minecraft:blocks/dark_oak_leaves']`; pre-roll rewrite through
  `ServerEvents.genericLootTables`, apple id from `global.item.apple`. Lines 4-5 of the header say rubberwood is
  left out.
- Verified: `myrtrees:blocks/rubberwood_leaves` has three pools, the third is `minecraft:apple` (oak copy).
- Scanned for other leaf tables with `minecraft:apple` in the jars at hand (Quark, IW incl. its leaf piles,
  Architect's Palette, Upgrade Aquatic, Mystical Oak Tree, Fruits Delight, Myrtrees and the FD add-ons): only
  `fruitsdelight:blocks/apple_leaves` (an apple tree, keep) and rubberwood. Not scanned: the other ~390 jars.
- Apples still come from: Vinery apple tree (worldgen, `wild_crops.json` key `apple`), Fruits Delight apple
  saplings (Winemaker trade), vanilla chests and the farmer trade. Those are intended; the row is about leaves.

### Build
1. Add `'myrtrees:blocks/rubberwood_leaves'` to `TABLES` (line 20) and update the header comment (lines 3-5).
2. Expected log line after `/reload`: `[leaf-apples] apples removed from 3 of 3 leaf tables`.
3. Completeness check in game: `/dumploot`, then
   `grep -l '"minecraft:apple"' kubejs/exported/loot_tables/*leaves*.json` (the export path `tools/loot_census.py`
   reads). Any hit that is not an apple tree goes into `TABLES`.
4. Writeups: crops.md §2.8 "Side changes" line "Myrtrees rubberwood leaves still do (V17)", V17 -> FIXED, and
   [loot.md](../loot.md) if it lists `08_leaf_apples.js`.

### Ids and data
`myrtrees:blocks/rubberwood_leaves` (loot table id, not an item; tables stay literal in `TABLES` like the two
existing ones). Item `minecraft:apple` = `global.item.apple`.

### Decisions
- Treat Quark / other "fruit" leaves? None found that drop apples. Recommendation: rubberwood only, plus whatever the
  `/dumploot` grep finds.

### Gotchas
- `modify()` resets the table type, the script already sets `minecraft:block` back (line 42).
- CR-57 already tests oak/dark oak; extend it rather than adding a new test.

### Tests
- **CR-57** (extend): also break 200 rubberwood leaves -> saplings and sticks, no apples. Log says `3 of 3`.

---

## crops #9: Bell pepper colours

Status BUILT 2026-10-21 (`kubejs/server_scripts/loot/09_crop_drops.js`, weights from CEConfig `[crop_drops.bell_peppers]`, wild left red-only; extra colours are Rustic Delight 1.7.0 only; CR-69, CR-70 pending) · Work data (loot) · TODO `farming/crops #9`

### Goal
Harvests give red, green and yellow peppers in an even split. Check whether the mod has more colours.

### Now
Verified `rusticdelight:blocks/bell_peppers` (Rustic Delight 1.3.0), ripe = `age 7`:
- pool 3: one roll, `bell_pepper_red` weight 6, `bell_pepper_green` 1, `bell_pepper_yellow` 1;
- pools 4 and 5: 15% extra green, 15% extra yellow.
- Expected per ripe harvest: red 0.75, green 0.275, yellow 0.275 (red 58% of all peppers).
- `rusticdelight:blocks/wild_bell_peppers`: 20% `bell_pepper_red` (not with shears), else seeds; never green/yellow.
- More colours: the Modrinth page (checked 2026-10-21) says "Available in Red, Yellow and Green". No newer variety;
  the "more varieties" note in the row is not true for this mod.
- Items in the dict: `i.bell_pepper_red/green/yellow` (item_dict.js 12456-12460), `i.bell_pepper_seeds` (12979),
  `i.any_bell_pepper = '#forge:crops/bell_pepper'` (12560).

### Build
1. New pre-roll script `kubejs/server_scripts/loot/09_crop_drops.js` in the shape of `08_leaf_apples.js` (IIFE,
   `ServerEvents.genericLootTables`, `event.modify(table, b => ...)`, set `b.type = 'minecraft:block'` back), so later
   crop loot fixes have a home. Rewrite `rusticdelight:blocks/bell_peppers`:
   - pool 3 entries: red, green, yellow, weight 1 each;
   - pools 4 and 5 stay, add a third 15% bonus pool for red (copy pool 4, swap the item), so each colour averages
     0.483 per harvest.
   Match pools by their entry item names, not by index (Forge names pools by position; keep the count of the existing
   pools, only append).
2. Optional (decision below): wild bell peppers drop a random colour instead of always red.
3. Item ids from `global.item` keys (above); the table id stays literal.

```js
// sketch, inside modify(): pools = JSON.parse(String(b.pools.toString()))
var colours = [i.bell_pepper_red, i.bell_pepper_green, i.bell_pepper_yellow]
var main = pools.filter(p => (p.entries || []).length === 3)[0]
main.entries.forEach(e => { e.weight = 1 })
var bonus = JSON.parse(JSON.stringify(pools.filter(p => p.entries.length === 1 && p.entries[0].name === i.bell_pepper_green && p.conditions.length === 2)[0]))
bonus.entries[0].name = i.bell_pepper_red
pools.push(bonus)
b.pools = JsonIO.parseRaw(JSON.stringify(pools))
```

### Ids and data
`rusticdelight:blocks/bell_peppers`, `rusticdelight:blocks/wild_bell_peppers` (verified). Botany Pots or Create
harvesters use the same block loot, so they follow.

### Decisions
- Even split by weight (recommended) or by forcing one of each colour per harvest? Recommendation: weights; keeps the
  harvest size.
- Wild peppers: recommendation leave red-only (it is a forage hint, seeds are the real drop).

### Gotchas
- The table has `random_sequence`; harmless on 1.19.2, keep it.
- JEI / WTHIT drop previews read the live table, so they update with `/reload`.

### Tests
- **CR-69 Bell pepper split:** `/loot spawn ~ ~ ~ mine <ripe pepper>` 200 times (a command block loop or the
  `loot` command on `rusticdelight:bell_peppers[age=7]` placed with `/setblock`) -> red, green and yellow each about a
  third.

---

## crops #3: Harvest and replant audit for new crops

Status Planned · Work data (+ tool) · TODO `farming/crops #3`

### Goal
Every crop block in the pack is covered: how it is harvested (right-click, hoe area, break), whether it replants,
which soils it takes, whether WTHIT shows growth, whether it is seasonal. New mods show up as gaps automatically.

### Now
- crops.md §2.1 is the hand table, written 2026-10-02 from Vinery 1.3.10 and the 1.0.4 mods. Since then only the
  Vinery port changed crops: bush ids are the same (`vinery:red_grape_bush`, `white_grape_bush`,
  `savanna_grape_bush_red/_white`, `taiga_grape_bush_red/_white`; jungle vines `jungle_grape_bush_red/_white`), the
  ten 1.5.5 wood lattices were dropped (alcohol.md 10.10), `vinery:grapevine_lattice` and `grapevine_stem` stay.
  Port bushes still need a full opaque block (`GrapeBushBlock.mayPlaceOn` = `isSolidRender`,
  `development/backportedVinery/src/main/java/net/satisfy/vinery/core/block/GrapeBushBlock.java` line 119), harvest
  from stage 2. Brewin' port and the 1.0.5-1.0.9 mods add no crop blocks (checked against the plan files' mod lists;
  Firefly Bush is decoration).
- `/dumpcropgrowth` ([kubejs/server_scripts/_lib/dump_crop_growth.js](../../kubejs/server_scripts/_lib/dump_crop_growth.js),
  126 lines) already walks every block with a stage-like property and records its class chain
  (`CROP_NAMES` line 39) to `kubejs/exported/crops/crop_growth_audit.json` (300 blocks on 2026-10-18; `kubejs/exported`
  is not in git). [tools/crop_growth_coverage.py](../../tools/crop_growth_coverage.py) checks WTHIT coverage from it.
- Quark's lists live in static fields of `vazkii.quark.content.tweaks.module.SimpleHarvestModule` (verified):
  `crops` (Map ripe state -> replant state), `cropBlocks` (Set of the replant states' blocks), `rightClickCrops` (Set:
  every non-vanilla `BushBlock`/`GrowingPlantBlock` that is `BonemealableBlock` and not a `CropBlock`, plus the
  config list), `simpleHarvestBlacklistedTag`.

### Build
1. Extend `dump_crop_growth.js` (or add `dump_crop_harvest.js` beside it, registered in `_lib/dump_all.js`) to write
   `kubejs/exported/crops/crop_harvest_audit.json`, one row per audited block:
   - `quark`: `replant` if any `SimpleHarvestModule.crops` key has this block (list the ripe states), `click` if in
     `rightClickCrops`, `blacklisted` if the default state is in `#quark:simple_harvest_blacklisted`;
   - `parity`: in `#creativeengineering:crops/farmland_crops`;
   - `own_soil_check`: true when the block's class (or a non-vanilla superclass) declares its own `canSurvive`
     (`m_7898_`) or `mayPlaceOn` (`m_6266_`), found with `getDeclaredMethods()`. Those crops skip Forge's
     `canSustainPlant`, so Rich Farmland / Fertile Soil may refuse them (tobacco, tea, pineapple, grapes are the known
     ones). Testing real placement needs a level, so the dump only flags them;
   - `seasons`: membership of the four `sereneseasons:*_crops` block tags (feeds #5);
   - `wthit`: reuse the existing field.
    Reading the Quark statics from KubeJS: `Java.loadClass('vazkii.quark.content.tweaks.module.SimpleHarvestModule').crops`
   (public static). If KubeJS's class filter blocks the class, add a rule the way the pack's other `KUBEJS_CLASSES`
   rules were added (plan_1.0.5.md row "VR"; unverified where the rule file lives today).
2. New `tools/crop_harvest_coverage.py`: reads the audit plus a hand rule file
   `tools/crop_harvest_rules.json` (`{ "<block id>": {"harvest": "replant|own_pick|shears|break|top_break",
   "why": "..."} }`) and prints UNKNOWN for any audited crop with no rule, and MISMATCH where the audit disagrees
   (e.g. rule says replant, audit has no Quark entry). Same shape as `crop_growth_coverage.py`.
3. Fill the rule file from crops.md §2.1; every UNKNOWN is a crop to add to §2.1.
4. Fold the outcome into crops.md §2.1 (fix the grape rows for the port) and add a §6 bullet for the new tool.

### Ids and data
No new item ids. Tags read: `quark:simple_harvest_blacklisted`, `creativeengineering:crops/farmland_crops`,
`sereneseasons:{spring,summer,autumn,winter}_crops`.

### Decisions
- One dump or two? Recommendation: a second command `/dumpcropharvest` that reuses the growth dump's block walk
  (export the walk as a function), so `/dumpall` runs both.

### Gotchas
- Quark fills its maps in `configChanged()`; dump after the world loads, not at startup.
- `cropBlocks` holds the replant states' blocks; a `Harvestable Blocks` line whose replant state is another block (or
  air) adds that block, not the crop (see crops #2 hemp).
- Bushes that are in `rightClickCrops` get CLICKed by a hoe even when they also have a `Harvestable Blocks` line
  (`getAction` checks `rightClickCrops` first). The audit should flag "replant + click" (pineapple, crops #2).

### Tests
Offline: the coverage tool exits 0 with no UNKNOWN after the rule file is filled. In game: run `/dumpcropharvest`
after each mod change (add to the crops.md §6 checklist).

---

## crops #1: Crop findings G8 to G15

Status Planned · Work data / patcherMod · TODO `farming/crops #1`

### Goal
Decide and apply each open G finding in crops.md §4 (lines 800-821). G4, G6, G7, G10, G11 stay as mod design.

### Now and Build, per finding

**G8 Weeds replant on right-click.** Verified: `WeedsBlock extends CropBlock`, so Quark's search records
`immersive_weathering:weeds[age=7] -> age 0`. `#quark:simple_harvest_blacklisted` is checked in `handle()`, in the
hoe `click()` pre-check and in `harvestAndReplant()`, so a blacklisted block is never harvested or replanted by
Quark at all (right-click does nothing; break it). Build: in
[blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) "Crop parity" section (lines 482-489) add
`t.add(e, 'quark:simple_harvest_blacklisted', item.weeds)` (`i.weeds`, item_dict.js line 10811). Data only,
`/reload`. Also fixes villagers replanting weeds.

**G9 flax and G13 corn top halves ignore right-click.** Verified in `SimpleHarvestModule.handle(player, hand, pos,
doRightClick, isHoe)`: the clicked block passes `cropBlocks.contains(block)` (flax and corn are in it), but
`getAction` only matches the exact ripe lower state, so the top gives `NONE`. Quark posts `SimpleHarvestEvent` with a
settable target pos, but then calls `harvestAndReplant(level, pos, ...)` with the **original** pos, so redirecting
through the event would harvest the wrong block. Build a patcherMod mixin instead:
- `mixin/QuarkTwoHighHarvestMixin` on `SimpleHarvestModule` (`remap = false`), `@ModifyVariable(method = "handle",
  at = @At("HEAD"), argsOnly = true, ordinal = 0)` on the `BlockPos` argument: if the block at `pos` is in a new tag
  `creativeengineering:crops/harvest_from_below` and the block below is the same block, return `pos.below()`.
  The hoe loop calls `handle` for each area position and the one above, so the area works too; after the first
  replant the lower half is age 0 and the second call finds nothing.
- Tag (blocktags.js, Crop parity section): `supplementaries:flax`, `corn_delight:corn_crop`. Block-only ids go in
  [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js) as `b.flax_crop = 'supplementaries:flax'` (the
  item `i.flax` at item_dict.js 3618 is the same id, reuse it) and `b.corn_crop = 'corn_delight:corn_crop'` (new key).
- Helper: `crops/CropParity.HARVEST_FROM_BELOW` TagKey + `isHarvestFromBelow(state)`, same style as the two existing
  keys.
Unverified: flax's top-half state property name (`half`), not needed because the mixin compares blocks, not states.

**G12 tea refuses Farmland.** Verified: `SmallTeaBushBlock` and `TeaBushBlock` both override `mayPlaceOn`
(`m_6266_`) to `state.is(BlockTags.DIRT)` and their own `canSurvive` (`m_7898_`) calls it (TeaBush only for the lower
half). Build `mixin/TeaBushSoilMixin` targeting both classes (`@Mixin(value = {SmallTeaBushBlock.class,
TeaBushBlock.class}, remap = false)`), `@Inject(method = "m_6266_", at = @At("RETURN"), cancellable = true)`:
`if (!cir.getReturnValue() && CropParity.isFarmlandSoil(state)) cir.setReturnValue(true)`. Compile jar
`FarmersRespite-1.19-2.0.jar` into `development/patcherMod/libs/`. Value is low: tea growth is a flat 1/20 (small) and
1/12 (aging) per random tick through `onCropsGrowPre`, it ignores moisture, and bone meal is off
(`enableBoneMealTeaBush = false`), so Rich Farmland's boost does nothing. It is consistency only.

**G14 coffee eats crops.** Farmer's Respite design, and FR coffee has no source in the pack (V18,
`farming/wild-crops #4`). Recommendation: reject, document only.

**G15 pineapple only on sand.** Verified: `PineappleBlock` and `WildPineappleBlock` override both `mayPlaceOn` and
`canSurvive` with `#fruitsdelight:pineapple_grows_on` (jar: sand, red sand, coarse dirt). Pure tag. Build in
blocktags.js: `t.add(e, 'fruitsdelight:pineapple_grows_on', '#creativeengineering:crops/farmland_soils')` (a nested
tag reference, so it follows the parity soils). See crops #2 for the replant side and `farming/wild-crops #2` for the
worldgen side (same tag, decide together).

### Ids and data
Tags: `quark:simple_harvest_blacklisted` (Quark, empty in the pack), new `creativeengineering:crops/harvest_from_below`,
`fruitsdelight:pineapple_grows_on`. Mixin targets (verified names in the jars): `vazkii.quark.content.tweaks.module.
SimpleHarvestModule.handle(Player, InteractionHand, BlockPos, boolean, boolean)` (private static),
`umpaz.farmersrespite.common.block.SmallTeaBushBlock` / `TeaBushBlock` `m_6266_`.

### Decisions
- G8 blacklist (recommended) or leave replanting? Blacklist.
- G12: do it (cheap, same build) or reject? Recommendation: do it with the C build.
- G15: farmland soils only (recommended), or also grass? Grass belongs to the wild-crops decision.

### Gotchas
- `handle` is private static: `@ModifyVariable` on a static method needs `argsOnly = true`; check the descriptor with
  the decompiled class (Quark mixins already compile against `libs/` Quark).
- Corn: replanting the lower half makes the top fail survival and drop its own corn (crops.md §2.1). With the redirect
  the result is the same as clicking the lower half today.
- Blacklisting weeds also stops the hoe area right-click on weeds; the hoe break still clears them.

### Tests
- **CR-16** (change): right-click ripe weeds -> nothing happens; break -> dandelions + weeds.
- **CR-09 / CR-31** (change): right-click the top half of ripe flax / corn -> same result as the lower half.
- **CR-33** (change): tea seeds plant on Farmland, Rich Farmland and Fertile Soil.
- **CR-36** (change): pineapple sapling plants on Farmland, Rich Farmland, Fertile Soil; still refused on grass.

---

## crops #2: Crop parity for the G-list crops

Status Planned · Work patcherMod / data · TODO `farming/crops #2`

### Goal
Each crop behaves like wheat where it makes sense: soils (Farmland, Rich Farmland, Fertile Soil), right-click
replant, hoe area replant, hoe area break. Follow the §2.6 checklist (crops.md lines 339-350).

### Now (per crop, verified unless noted)

| Crop | Soil today | Right-click | Missing |
|---|---|---|---|
| Hemp `immersiveengineering:hemp` | `BushBlock`, `getPlantType` CROP; placement and survival go through Forge `canSustainPlant` (its own `mayPlaceOn` = vanilla farmland is only used for a foreign state), so all three farmland soils work | none; break the top (`growth=top0`), the bottom regrows | right-click harvest of the top |
| Grapes (port bushes) | full opaque block only | own pick from stage 2 | farmland soils (G7, "Vinery design") |
| Flax, corn | fine | lower half only | top half (crops #1 G9/G13) |
| Tea | `#minecraft:dirt` only | shears only | farmland (crops #1 G12); shears stay |
| Pineapple | tag (crops #1 G15) | none, break at age 4 (drops 1 pineapple, no sapling) | soils; replant |
| Weeds | n/a | replants | blacklist (crops #1 G8) |
| Coffee (FR) | basalt / magma | own pick | reject (G14) |

### Build
1. **Hemp right-click:** a `Harvestable Blocks` line cannot express "break the top": the replant state must be a
   real state, and `minecraft:air` as the replant state puts AIR into `cropBlocks` (every air block then passes
   Quark's first check) while the hemp block itself stays out of `cropBlocks`. Instead add a second rule to the
   crops #1 mixin class: tag `creativeengineering:crops/harvest_top_break` (hemp); in `handle` HEAD (`@Inject`,
   cancellable), if the state at `pos` is in that tag and is its ripe top (`growth=top0`, read the property by name
   so no IE compile dependency is needed), call `level.destroyBlock(pos, true, player)` and return true. The bottom
   (`bottom4`) regrows the top. Hoe area right-click then harvests a hemp row. Add hemp to
   `crops/farmland_crops`? Not needed: it is `IPlantable` (Forge farmland logic and the hoe break already work).
2. **Grapes:** the port is in-tree, so edit `GrapeBushBlock.mayPlaceOn` (line 119) instead of a mixin:
   `return floor.isSolidRender(level, pos) || floor.is(GRAPE_SOILS)` with
   `TagKey<Block> GRAPE_SOILS = BlockTags.create(new ResourceLocation("vinery", "grape_bush_soils"))` in the port's
   `TagRegistry`, filled in blocktags.js with `#creativeengineering:crops/farmland_soils`. Taiga bushes keep their
   podzol / coarse dirt / grass-within-4 growth rule. Bump the port's `-ce-19.2-NN` suffix (vinery.md deploy notes).
3. **Pineapple replant:** `Harvestable Blocks` line `"fruitsdelight:pineapple[age=4]"` in
   [config/quark-common.toml](../../config/quark-common.toml) line 780 (also `configs_edited/root_config/` and the
   CreEngServer mirror, as for tobacco). The ripe table drops only the pineapple, so Quark's "take one block item"
   finds no sapling and the replant is free. Gotcha: pineapple is a non-vanilla `BushBlock` + `BonemealableBlock`,
   so it is also in `rightClickCrops`, and with a hoe `getAction` returns CLICK before HARVEST (the plant has no
   `use`, so nothing happens). Fix in the same mixin class: redirect `getAction`, or in `handle` HEAD treat a state
   that is a key of `SimpleHarvestModule.crops` as HARVEST. Empty-hand replant works without it.
4. **Tea, flax, corn, weeds, pineapple soils:** crops #1.
5. Add each crop's §2.1 row update and a CR test; update the §2.6 "Code, written once" table with the new mixins.

### Ids and data
- Blocks without items (add to [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js), key = path):
  `b.hemp = 'immersiveengineering:hemp'`, `b.corn_crop = 'corn_delight:corn_crop'`,
  `b.red_grape_bush = 'vinery:red_grape_bush'` and the five other bushes if a tag needs them (the soil tag does not).
- Items in the dict: `i.seed` (IE hemp seed, item_dict.js 3964), `i.pineapple` (12899), `i.pineapple_sapling` (12900).
- New tags: `creativeengineering:crops/harvest_top_break`, `vinery:grape_bush_soils`.

### Decisions
- Which crops? Recommendation: hemp, flax, corn, tea, pineapple, weeds (as G8) and grape soils; reject coffee.
- Grapes on farmland: it breaks Vinery's "full block" rule (G7). Recommendation: allow the three farmland soils only;
  stems and lattices keep their own rules.
- Free pineapple replant (no sapling cost)? Recommendation: yes, like crops whose block has no item (crops.md §2.2).

### Gotchas
- One patcherMod build for crops #1, #2 and #4; register new mixins in
  `development/patcherMod/src/main/resources/creativeengineering.mixins.json` next to `TobaccoPlacementMixin`
  (lines 78-85).
- `libs/` compile jars are not in git: Farmer's Respite for the tea mixin; the hemp rule reads properties by name.
- The Vinery port change needs its own jar build and the `mods/` swap.

### Tests
- **CR-70 Hemp right-click:** right-click a grown hemp top -> fibre + seeds drop, the bottom stays at full growth and
  grows a new top. Iron hoe right-click on a row -> every top in the 3x3 harvested.
- **CR-71 Grapes on farmland:** red grape seeds on Rich Farmland and Fertile Soil plant and grow; on plain dirt still
  plant (full block); taiga seeds still need podzol / coarse dirt / grass within 4 to grow.
- **CR-72 Pineapple replant:** right-click a ripe pineapple -> one pineapple, back to age 0, no sapling used. Iron hoe
  right-click on a ripe 3x3 -> all replanted.

---

## crops #4: Weeds behaviour

Status Planned · Work data / patcherMod · TODO `farming/crops #4`

### Goal
Audit weeds; no weeds on rich soil.

### Now (verified in IW 1.2.10)
- `WeedsBlock extends CropBlock`, age 0-7, grows 1 stage per `random(25 / growthSpeed + 1) == 0` random tick, burns
  (flammability 100), hurts a moving player without boots 1 in 10 ticks (config `leggings_prevents_thorn_damage`).
- Survival (`mayPlaceOn`): `#immersive_weathering:fertile_blocks` (farmland, `immersive_weathering:mulch_block` =
  Fertile Soil), `#minecraft:dirt` (includes Rich Soil and Fertile Soil), `#immersive_weathering:cracked`,
  `#immersive_weathering:mossy`. Rich Farmland is in none of them, so a weed cannot even be placed there.
- Spawning is data, `data/immersive_weathering/block_growths/`:
  - `weeds.json`: owner block `minecraft:farmland`, tick source `clear_sky` (IW's sky-access tick picks the top
    motion-blocking column, so the farmland under a crop), chance 0.001, needs a grass / tall grass / fern / weed within
    2 blocks and fewer than 4 weeds there; puts weeds age 0 on the block above if it is air or `#minecraft:crops`
    (probability 0.165), destroying the crop.
  - `weeds_spread.json`: same owner, chance 0.1, needs a ripe weed within 3/2/3 and fewer than 5; probability 0.65.
- Owners are matched by block identity (`HolderSet`, tags allowed), so Rich Farmland (`farmersdelight:rich_soil_farmland`)
  and Fertile Soil are not owners: **weeds never appear on their own on rich soils already**. They only reach Rich Soil
  or Fertile Soil if a player plants the weed item.
- Config: `config/immersive_weathering-common.toml` line 3 `block_growths = true`, line 5 `block_growth_blacklist = []`.
- crops.md §2.1 claimed weeds "spread onto" all these soils; corrected 2026-10-21 (see the bottom of this brief).

### Build
1. G8 blacklist (crops #1): the right-click no longer replants weeds.
2. Hand-planting on rich soils: `mixin/WeedsSoilMixin` on `com.ordana.immersive_weathering.blocks.WeedsBlock`
   `m_6266_` RETURN: false when `soil.is(#creativeengineering:crops/weed_free_soils)`. Tag in blocktags.js Crop parity
   section: `item.rich_soil`, `item.fertile_soil` (Rich Farmland already refuses). IW compile jar is already in `libs/`
   for `MulchBlockCropGrowthMixin`.
3. Optional: protect crops not in `#minecraft:crops` from being replaced? Tobacco is not in `#minecraft:crops`, so
   weeds never replace it; nothing to do.
4. Optional data tuning by override files (preferred over the config blacklist, per IW's own comment):
   `kubejs/data/immersive_weathering/block_growths/weeds.json` with a lower `growth_chance` or higher
   `requiredAmount`, if weeds are too common in play.
5. crops.md: a short "Weeds" subsection under §2.4 with the facts above, and G8 -> FIXED.

### Ids and data
`immersive_weathering:weeds` = `i.weeds`; tags `immersive_weathering:fertile_blocks`, `immersive_weathering:weeds_replaceable`
(`#minecraft:crops` + air), new `creativeengineering:crops/weed_free_soils`.

### Decisions
- Is "rich soil" Rich Soil + Fertile Soil (recommended; both are the premium soils) or Rich Soil only?
- Tune spawn rate? Recommendation: not until someone reports weeds as a nuisance; the in-game check below measures it.

### Gotchas
- `clear_sky` ticks only fire while it is not raining (rain and snow are separate tick sources).
- The mixin also blocks weeds that IW's own spreading could put on those soils, but none can (owners), so it only
  changes hand-planting.

### Tests
- **CR-73 Weeds on rich soils:** weed item on Rich Soil and Fertile Soil -> refused; on dirt and farmland -> plants.
- **CR-74 Weeds spawn only on vanilla farmland:** two 9x9 fields of wheat, one on farmland and one on Rich Farmland,
  both with tall grass at the border, `/gamerule randomTickSpeed 300` for 5 minutes in clear weather -> weeds only in
  the farmland field.

---

## crops #5: Realistic growth seasons

Status Planned · Work config / KubeJS tags · TODO `farming/crops #5`

### Goal
Every crop has believable, fair growing seasons under Serene Seasons (SS).

### Now (verified in SS 8.1.0.24)
- [config/sereneseasons/fertility.toml](../../config/sereneseasons/fertility.toml): `seasonal_crops = true`,
  `crop_tooltips = true`, `out_of_season_crop_behavior = 0` (grow slowly), `underground_fertility_level = 32`.
- Rule (`ModFertility.isCropFertile`): a crop in **no** season tag is fertile all year. Below Y 32 without sky:
  always fertile. Too cold to rain at the spot: winter crops only. Tropical biomes: summer crops (and unlisted) all
  year. Otherwise the current season's tag. patcherMod `mixin/SeasonFertilityMixin` adds the pack's types (arid =
  tropical rule, frozen = winter only, permanent seasons; weather.md).
- Out of season with behaviour 0: the `CropGrowEvent.Pre` is denied 5 times in 6 (1/6 speed). Bone meal is **not**
  limited with behaviour 0 (the handler sets `Result.DEFAULT`, a no-op); behaviour 1 cancels it. Glass
  (`#sereneseasons:greenhouse_glass` = `#forge:glass`) within 16 blocks above makes any crop fertile.
- Only growth that fires Forge's `onCropsGrowPre` is affected: `CropBlock`s, FD bushes, FR tea, Fruits Delight bushes
  and pineapple. Tobacco (no Forge growth event, crops.md §2.5) and the Vinery port's bushes (`GrapeBushBlock.randomTick`
  has no event) ignore seasons whatever the tags say.
- Season tags in the jars (block tags; the item tags drive tooltips):
  - SS itself: wheat summer+autumn, carrots spring+autumn, potatoes spring, beetroots autumn, melon stem summer,
    pumpkin stem autumn, cocoa summer, sugar cane / cactus summer, sweet berries spring-autumn, saplings and Nether
    plants all seasons.
  - FD: cabbages autumn+winter, onions spring+autumn, rice summer+autumn, tomatoes summer.
  - Delightful: salmonberry bush summer. Corn Delight: corn summer+autumn. Rustic Delight: bell peppers and RD coffee
    summer+autumn, cotton spring+summer. Supplementaries: flax spring, rope/stick tomatoes summer. Fruits Delight
    (saplings and leaves): spring pear, peach, blueberry; summer lychee, mango, mangosteen, bayberry, durian, lemon,
    hamimelon stem, pineapple; autumn orange, apple, fig, cranberry; winter hawberry, persimmon, kiwi.
  Fruits Delight's summer list also has `fruitsdelight:pineapple`.
- Unlisted, so all-year today (from the jars read; the crops #3 audit gives the full list): hemp, tobacco, tea, FR
  coffee, strawberries, adzuki, mint, vanilla, mulberries, grapes, Floral Enchantment bushes, Ender Crop, Nether's
  Delight crops.
- No `sereneseasons` tags in `kubejs/` today. [seasontags.js](../../kubejs/server_scripts/tags/seasontags.js) only
  builds biome tags.

### Build
1. Run the crops #3 audit (it records season tag membership) to get the exact current table; paste it into a new
   crops.md section "2.10 Growing seasons".
2. New config `kubejs/generation_scripts/worldgen/crop_seasons.json` (`{ "<block id>": ["spring", "summer"], ...,
   "_readme": [...] }`, the season_types.json pattern) with `replace` semantics per crop: the pack's list wins over
   the mods' tags for crops it names.
3. Loader in a new `kubejs/server_scripts/tags/cropseasontags.js` (IIFE, read with `JsonIO.readString` like
   seasontags.js lines 34-40): `ServerEvents.tags('block', ...)` removes the named block from all four
   `sereneseasons:*_crops` tags (`global.tags.remove`) and adds it to its listed ones (`global.tags.add`);
   `ServerEvents.tags('item', ...)` does the same for the seed item so the SS tooltip matches. Block ids from
   block_dict.js, item ids from item_dict.js keys; the json holds raw ids because it is data (as `wild_crops.json` does).
4. Fill the json. Starting proposal (temperate-climate realism, every season has something):
   - spring: carrots, potatoes, onions, cabbages, flax, strawberries, adzuki, mint, tea;
   - summer: wheat, corn, tomatoes, bell peppers, melon / pumpkin / hamimelon stems, rice, cotton, hemp, tobacco,
     grapes, salmonberries, blueberries, vanilla, sugar cane, cocoa, coffee;
   - autumn: wheat, pumpkin stem, beetroots, carrots, potatoes, cabbages, onions, corn, grapes, cranberries, apples;
   - winter: cabbages, beetroots (sea beet is hardy), onions, mint (keeps), Nether and colony crops (all year).
   Keep tropical crops (cocoa, coffee, vanilla, bananas, pineapple) summer-only so the tropical rule grows them all year
   in warm biomes and only in summer elsewhere.
5. Tobacco and grapes: decide whether they should be seasonal (decision 2). If yes, patcherMod: tobacco needs a call to
   `ForgeHooks.onCropsGrowPre` in its tick (extend `TobaccoPlacementMixin`'s target set with a mixin on
   `TobaccoSeedsBlock` `m_213898_`), grapes need the same call in `GrapeBushBlock.randomTick` (in-tree port).
6. Config: decide behaviour (decision 1). If 1, the bone-meal gap closes too.
7. Writeups: crops.md new §2.10; weather.md cross-link (its WE-16 test covers wheat only).

### Ids and data
Tags `sereneseasons:spring_crops`, `summer_crops`, `autumn_crops`, `winter_crops` (block and item),
`sereneseasons:unbreakable_infertile_crops` (only matters for behaviour 2), `sereneseasons:greenhouse_glass`.

### Decisions
1. Out-of-season behaviour: 0 slow (today, and bone meal bypasses it), 1 no growth, 2 break. Recommendation: keep 0
   but close the bone-meal bypass? Not possible by config; so either keep 0 as "fair" or use 1 as "realistic".
   Recommendation **0** (fair for a server where people log in for short sessions), plus greenhouse glass as the
   all-year route, documented in the tooltip.
2. Seasonal tobacco and grapes (needs patcherMod / port code)? Recommendation: grapes yes (they are the wine clock's
   crop), tobacco no.
3. Unlisted crops stay all-year by default? Recommendation: list every overworld farm crop; leave Nether, End and
   colony crops unlisted.

### Gotchas
- SS rebuilds its lists on `TagsUpdatedEvent`, so `/reload` applies tag edits.
- Item tags drive the tooltip only; a crop whose seed is also a food (carrot, potato) shows the tooltip on the food.
- The pack's arid / frozen / permanent season types change what "in season" means (weather.md table at line 177).

### Tests
- **CR-75 Seasons tooltip:** hover tobacco seeds, tea seeds and corn seeds -> "Fertile seasons" lists the json's
  seasons.
- **CR-76 Out of season:** `/season set early_winter` in a plains biome, wheat on farmland at `randomTickSpeed 100` ->
  grows at about 1/6 the speed of wheat under glass next to it.

---

## crops #8: Watering can

Status Planned · Work KubeJS (row) -> patcherMod or mods/ · TODO `farming/crops #8`

### Goal
A watering can that hydrates farmland and speeds crops a little.

### Now
Nothing in the pack. Candidates checked 2026-10-21 (Modrinth API, 1.19.2 Forge):
- **Pickle Tweaks 7.0.3** (verified jar): `pickletweaks:watering_can` (3x3, 25% chance per use to random-tick plants)
  and `reinforced_watering_can`; fill from a water source, then use on a block: sets every `FarmBlock` in range to
  moisture 7 (Rich Farmland too, it subclasses `FarmBlock`) and, on the chance, calls `randomTick` on every
  `BonemealableBlock` / `IPlantable` / sugar cane / cactus in range. Cooldown per use. Needs **Cucumber** (not in the
  pack). Also adds paxels, sickles, scythes, magnet, gear, tool repair in the grid; each has a toggle in its config
  (`wateringCan`, `sickles`, `scythes`, `paxels`, ...).
- Farming for Blockheads 11.3.5 (sprinklers, market, feeding trough). Sprinklerz 0.3.2 (sprinkler blocks).
- No 1.19.2 Forge "watering can only" mod found.

### Build (recommended: own item in patcherMod)
1. `crops/WateringCanItem.java` + registration in a new `crops/CropRegistry.java` (DeferredRegister like
   `oven/OvenRegistry.java`), id `creativeengineering:watering_can`. Behaviour: right-click water source to fill
   (NBT or damage-bar "water" 0..N uses), right-click a block to water a 3x3 (tier 2 brass can 5x5):
   - farmland (any `FarmBlock`, so Rich Farmland too) -> moisture 7;
   - Fertile Soil -> IW's soaked state (read IW `MulchBlock` property by name; unverified property name, check the
     decompiled class);
   - plants: one `randomTick` per plant per use with a configurable chance (`[crop_growth] wateringCanTickChance`,
     default 0.25). Random ticks fire `onCropsGrowPre`, so Serene Seasons still applies (no season bypass).
   - particles + sound; cooldown 10 ticks.
2. Model / texture under `assets/creativeengineering/`, lang key, tooltip line.
3. KubeJS: `i.watering_can = 'creativeengineering:watering_can'` in item_dict.js (custom items section), recipe via
   `global.NCH.shaped(...)` in the farming recipe file (copper ingots + bucket; brass tier from Create brass),
   tags via `global.tags` if a tool tag is wanted.
4. JEI info page (the pack's JEI info pattern) and crops.md §2.4 row.

### Ids and data
New: `creativeengineering:watering_can` (+ optional `brass_watering_can`). Ingredients from the dict
(`i.copper_ingot`-style keys; check names before use).

### Decisions
- Own item (recommended: small, one class, no extra library, tuned to the soils and seasons) or Pickle Tweaks +
  Cucumber (also gives the scythe of `cooking/kitchen #10` and sickles, but brings paxels and grid repair that overlap
  the pack's tool balance; all toggleable)?
- Growth boost or hydration only? Recommendation: hydration + a small random-tick chance, below bone meal.

### Gotchas
- Calling `randomTick` directly on a crop bypasses nothing (the crop's own tick fires the Forge event); calling
  `performBonemeal` would bypass seasons. Use `randomTick`.
- Fake players (Create deployers) can use items: decide whether a deployer can water (config, default on).

### Tests
- **CR-77 Watering can:** fill at a water source; use on dry farmland 3x3 -> moist; on a 3x3 of wheat a few times ->
  some stages advance; out of season (`/season set early_winter`) -> advances about 1/6 as often.

---

## crops #10: Melon and pumpkin growth stages

Status Planned · Work patcherMod / resource pack · TODO `farming/crops #10`

### Goal
A pumpkin or melon starts small beside the stem and grows to full size, instead of appearing full-size.

### Now
- Vanilla `StemBlock.randomTick` (`m_213898_`, 1.19.2): at age 7, picks a side and, if the soil there is dirt/
  farmland and the spot is air, sets `this.fruit.defaultBlockState()` there and turns itself into
  `fruit.getAttachedStem()` facing it. `AttachedStemBlock.updateShape` (`m_7417_`) reverts the stem to a plain stem
  when the faced block is no longer `this.fruit`. (Vanilla mojmap; check the SRG names against the Forge mapped jar.)
- Fruits Delight `hamimelon_stem` is a `StemBlock` subclass (crops.md §2.1), same path.
- Models: Fantasy Furniture 6.7.0 ships `fantasyfurniture:block/decorations/stackable_pumpkins_0/1/2` (1, 2 or 3
  small pumpkins; verified names). A model in the pack can `"parent"` those by id while Fantasy Furniture is
  installed; do not copy its textures (licence not checked).
- Large 2x2 / 3x3 pumpkins are `ports/autumnityport #3`; this row only adds the stages before full size.

### Build
1. patcherMod blocks `creativeengineering:growing_pumpkin` and `growing_melon` (and `growing_hamimelon` if wanted),
   `age` 0-2, random tick at the stem's growth speed, at max age they replace themselves with the vanilla fruit.
   Shape grows with age (small, medium, near full). Loot: age 0-1 drop nothing or one seed (decision), age 2 drops a
   few slices / the fruit. Not `BonemealableBlock` (or yes, decision).
2. Mixin `StemGrowthStagesMixin` on `StemBlock` `m_213898_`: redirect the `setBlockAndUpdate(fruitPos,
   fruit.defaultBlockState())` call to place the growing block for that fruit (map fruit -> growing block in
   `crops/CropRegistry`).
3. Mixin on `AttachedStemBlock` `m_7417_`: treat the matching growing block as the fruit so the stem stays attached.
4. Models: age 0/1 parent the Fantasy Furniture small-pumpkin models for pumpkins (with a fallback scaled cube model in
   the pack if FF is absent); melons need their own small cube models from vanilla textures (`minecraft:block/melon_side`
   etc.). Put them in patcherMod assets (the pack's "resource pack" option only reskins, it cannot add stages).
5. WTHIT: add the growing blocks to `crop_growth.json` (age 0-2). Hoe area break: they are not plants, so the hoe break
   ignores them (good).
6. Config switch `[crop_growth] fruitStages` (off = vanilla behaviour) in `CEConfig`.
7. Dict: `b.growing_pumpkin`, `b.growing_melon` in block_dict.js.

### Ids and data
New blocks above. Vanilla `minecraft:pumpkin_stem`, `attached_pumpkin_stem`, `melon_stem`, `attached_melon_stem`,
`fruitsdelight:hamimelon_stem`.

### Decisions
- Whether at all (row says "whether"). Recommendation: yes, pumpkin + melon, hamimelon later; small effort compared to
  autumnityport #3, and that row can reuse the growing block as its first stage.
- Harvest a young fruit? Recommendation: age 2 only drops slices (melon) or nothing useful (pumpkin), so players wait.

### Gotchas
- Villager farmers and Create harvesters break pumpkins by block id; the growing block must not be in
  `#minecraft:crops` or harvesters break it early.
- Serene Seasons: the stem's growth is seasonal (pumpkin stem autumn, melon stem summer); the growing block should call
  `ForgeHooks.onCropsGrowPre` too, so it follows the same season.
- New chunks: worldgen pumpkin patches place the full pumpkin block directly, unaffected.

### Tests
- **CR-78 Fruit stages:** grow a pumpkin stem at `randomTickSpeed 100` -> a small pumpkin appears attached, grows two
  steps to a full pumpkin, the stem stays attached throughout. Break the small one -> the stem detaches and later
  grows another.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| crops #6 Apples only from apple trees | Testing | Built 2026-10-21 (`08_leaf_apples.js`); not in the 2026-10-22 scratch run; CR-57, scratch SQ-CD-02 (`tools/farm_worldgen_probe.js` `zfw loot`) |
| crops #9 Bell pepper colours | Testing | Built 2026-10-21 (`09_crop_drops.js`, CEConfig `[crop_drops.bell_peppers]`, in every 1.1.01 jar incl. `mods/`); not scratch-run; CR-69, CR-70, scratch SQ-CD-01 |
| crops #11 Extra stem fruit | Testing | Built 2026-10-22 11:07, only in the `build/libs` jar (md5 4daad5bb), not in `mods/`; scratch SQ-SF queued; CR-71 to CR-77; finding F-SS1 (extra fruit ignores Serene Seasons, [crops.md](../crops.md) §4) needs a decision |
| crops #7 Crop tests (CR-01 to CR-68) | Complete | Several §2.8 tests had stale expectations after the 2026-10-10 rule change (CR-50, CR-55); corrected. CR-16 and CR-09/CR-31/CR-33/CR-36 change when crops #1 is built |

## Writeup corrections made

- [crops.md](../crops.md) §2.1 Weeds row: "spreads onto Dirt, Farmland, Rich, Fertile, ..." -> survives on those
  (not Rich Farmland), appears and spreads on its own only on open-sky vanilla Farmland (IW block growths, verified).
- crops.md §2.1 jungle grapes block id `vinery:jungle_grapes_*` -> `vinery:jungle_grape_bush_red`, `_white`.
- crops.md §2.8 status line: "not run in game" -> DEPLOYED with 1.0.4, rules revised 2026-10-10 (1.0.6).
- crops.md §2.8 "Where each crop goes": rows corrected to the current `wild_crops.json` (2026-10-10): carrot/onion 25,
  sweet berries 33 split from potato/salmonberry (`!snowy`, 15) and blueberry (`snowy`, 18), pumpkin 11, strawberries
  `cold grassy mountain !snowy` 6, adzuki 18, mint `cold grassy mountain` 11, bayberry/mangosteen/durian `swamp !cold`
  3, apple tree Vinery only, fig/hawberry `!snowy` 10, persimmon 13; added rubber tree and Vinery cherry rows.
- crops.md §2.8 "Choices made": the `!snowy`, strawberry-mountain and Ice Marsh bullets updated to the same rules.
- crops.md V13: appended the 2026-10-10 densities (mint x5.6 not x11, strawberries x1.1, ...).
- crops.md §3: "Wild crop placement (built 2026-10-03, not run in game)" -> DEPLOYED with 1.0.4, revised for 1.0.6.
- crops.md CR-50 and CR-55: expectations updated to the 2026-10-10 rules (no strawberries/blueberries in taiga; mint
  in grove / highlands, not windswept hills / meadow).
- [wandering_trader.md](../wandering_trader.md) "Never sold" note: Farmer's Respite coffee's wild spawn is off since
  1.0.4, so the beans have no source (V18).
