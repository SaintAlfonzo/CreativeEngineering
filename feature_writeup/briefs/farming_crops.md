# Brief: farming / crops

Written 2026-10-21 for the open `farming/crops` rows of [TODO.md](../todo/TODO.md); swept 2026-10-31 (every open row
has a section, Testing rows are listed under "Built, needs play"). Reference writeup: [crops.md](../crops.md) (test
prefix **CR-**, last used CR-99). Proposed tests are placeholders `CR-new1`, `CR-new2` per section, numbered when
queued; ids that already exist are kept. Layout: [README.md](README.md).

**Verified against jars** (downloaded and read or decompiled 2026-10-21): Quark 3.4-418 (`SimpleHarvestModule`),
Immersive Weathering 1.2.10 (`WeedsBlock`, `block_growths/weeds*.json`, `BlockGrowthHandler`, `AreaCheck`, tags),
Farmer's Respite 2.0 (`SmallTeaBushBlock`, `TeaBushBlock`), Fruits Delight 0.5.11 (`PineappleBlock`,
`WildPineappleBlock`, `pineapple` loot, `pineapple_grows_on` tag), IE 9.2.4 (`HempBlock`, `hemp` loot), Myrtrees 1.2.0
(`rubberwood_leaves` loot), Fantasy Furniture 6.7.0 (model names). Added 2026-10-31: Immersive Weathering `MulchBlock`
(`use`), Fruits Delight `DoubleFruitBushBlock` (lemon pick) and the lemon recipes and loot, the repacked Oceanic Delight
1.0.1 hoe tag, Supplementaries 2.4.20 tomato blockstates. In-tree code read from `development/` as cited. Anything else
is marked unverified.

## Scope and order

| Row | Change | Work | Group |
|---|---|---|---|
| crops #27 | Sea grape plant mineable with a hoe | KubeJS tag, 1 line | A: quick wins |
| crops #3 | Harvest / replant audit for every crop | KubeJS dump + tool | B: audit first |
| crops #1 | G8, G9/G13, G12, G15 (G14 reject) | data + patcherMod | C: one patcherMod build |
| crops #2 | Parity for hemp, grapes, flax, corn, tea, pineapple, weeds, coffee | data + patcherMod + Vinery port | C |
| crops #4 | Weeds behaviour, none on rich soil | data + patcherMod | C |
| crops #29 | Tomato ropes and sticks in the hoe area harvest | tag (+ patcherMod for tall vines) | C |
| crops #17 | Wet farmland from buckets and water bottles | KubeJS (or patcherMod) | C |
| crops #21 | Melon stems and fertile soil (question first) | data / patcherMod | D: owner answers first |
| crops #15 | Other plants in random growth? | KubeJS / data | D |
| crops #14 | Bone meal in planters | patcherMod / none | D |
| crops #28 | Grasses: winter offseason only (Built 2026-11-02) | Creative Calendar + data | E: seasons follow-up |
| crops #19 | Village farms: Rustic crops by the wild-crop rules | CD data | E |
| crops #24 | Better soils raise nether crop yield | CD KubeJS + CEConfig | E: soil yield follow-up |
| crops #26 | Soul soil upgrade chain | KubeJS + texture | E (with `farming/composting #2`) |
| crops #10 | Melon and pumpkin growth stages | patcherMod blocks + models | F: stem work, one build |
| crops #20 | Stems connect to every fruit they grew | patcherMod + blockstates | F |
| crops #22 | More seed bags | CD blocks + data + textures | G: new content |
| crops #25 | Mini melon crop | CD blocks + data + textures | G |

Suggested order: **A** (a `/reload`), then **B** (the audit gives #1/#2/#4/#29 their checklist and catches crops this
brief misses), then **C** in one patcherMod build (#1, #2, #4 and #29 share `CropParity`, the Quark mixins and the
"Crop parity" tag section; G8 is also #4's main fix), **D** after the owner answers the open questions, **E** as data
follow-ups of built systems (crop seasons, village farms, soil yield), **F** in one patcherMod build (both change the
attached stem), **G** last (new ids, art). The scythe (`cooking/kitchen #10`) already uses `crops/HoeHarvestArea`; keep
it out of C.

---

## crops #3: Harvest and replant audit for new crops

Status Planned · Work data (+ tool) · TODO `farming/crops #3`

### Goal
Every crop block in the pack is covered: how it is harvested (right-click, hoe area, break), whether it replants,
which soils it takes, whether WTHIT shows growth, whether it is seasonal. New mods show up as gaps automatically.

### Now
- crops.md §2.1 is the hand table, written 2026-10-02 from Vinery 1.3.10 and the 1.0.4 mods. Since then the Vinery
  port (now `creativeBrewing/members/vinery`) kept the bush ids (`vinery:red_grape_bush`, `white_grape_bush`,
  `savanna_grape_bush_red/_white`, `taiga_grape_bush_red/_white`; jungle vines `jungle_grape_bush_red/_white`), the
  ten 1.5.5 wood lattices were dropped (alcohol.md 10.10), `vinery:grapevine_lattice` and `grapevine_stem` stay.
  Port bushes still need a full opaque block (`GrapeBushBlock.mayPlaceOn` = `isSolidRender`,
  [GrapeBushBlock.java:122-123](../../development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery/core/block/GrapeBushBlock.java)),
  harvest from stage 2.
- **New crop blocks since 2026-10-21** (in-tree classes extending `CropBlock` / `BushBlock` / `StemBlock`, checked
  2026-10-31): Veggies port (`backportedVeggies`: broccoli, cauliflower, garlic, sweet potato, turnip, zucchini), Vintage
  port (`backportedVintage`: cucumber, ghost pepper, oat, peanut), Miner's port (`backportedMiners`: cave carrot),
  Creative Delight members `rustic` (bell peppers, pale and dark bell peppers, cotton, coffee), `delightful` (Mini
  Cantaloupe plant), `enders` (end plants), `hearth` (raspberry bush). Cucumber, ghost pepper and zucchini are already in
  `#creativeengineering:crops/own_pick` (CD `vintagedelight/tags.js:43`, `veggiesdelight/tags.js:19`); SQ-VP-09 covers
  the Vintage and Veggies soils. None of these is in crops.md §2.1 yet.
- `/dumpcropgrowth` ([kubejs/server_scripts/_lib/dump_crop_growth.js](../../kubejs/server_scripts/_lib/dump_crop_growth.js),
  126 lines) already walks every block with a stage-like property and records its class chain
  (`CROP_NAMES` line 41) to `kubejs/exported/crops/crop_growth_audit.json` (last run 2026-10-30: 311 blocks listed,
  276 missing a growth entry; `kubejs/exported` is not in git). [tools/crop_growth_coverage.py](../../tools/crop_growth_coverage.py) checks WTHIT coverage from it.
- Quark's lists live in static fields of `vazkii.quark.content.tweaks.module.SimpleHarvestModule` (verified):
  `crops` (Map ripe state -> replant state), `cropBlocks` (Set of the replant states' blocks), `rightClickCrops` (Set:
  every non-vanilla `BushBlock`/`GrowingPlantBlock` that is `BonemealableBlock` and not a `CropBlock`, plus the
  config list), `simpleHarvestBlacklistedTag`.

### Build
1. Extend `dump_crop_growth.js` (or add `dump_crop_harvest.js` beside it, registered in `_lib/dump_all.js`) to write
   `kubejs/exported/crops/crop_harvest_audit.json`, one row per audited block:
   - `quark`: `replant` if any `SimpleHarvestModule.crops` key has this block (list the ripe states), `click` if in
     `rightClickCrops`, `blacklisted` if the default state is in `#quark:simple_harvest_blacklisted`;
   - `parity`: in `#creativeengineering:crops/farmland_crops`; `own_pick`: in `#creativeengineering:crops/own_pick`
     (`CropParity.OWN_PICK_CROPS`, read by `mixin/QuarkOwnPickMixin`);
   - `own_soil_check`: true when the block's class (or a non-vanilla superclass) declares its own `canSurvive`
     (`m_7898_`) or `mayPlaceOn` (`m_6266_`), found with `getDeclaredMethods()`. Those crops skip Forge's
     `canSustainPlant`, so Rich Farmland / Fertile Soil may refuse them (tobacco, tea, pineapple, grapes are the known
     ones). Testing real placement needs a level, so the dump only flags them;
   - `season`: the crop season type, i.e. membership of the `creativeengineering:crop_seasons/<type>` block tags that
     `cropseasontags.js` fills from `crop_seasons.json` (`tools/crop_season_audit.py` already checks coverage; reuse
     its type list rather than a second one);
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
`creativeengineering:crops/own_pick`, `creativeengineering:crop_seasons/<type>`. Serene Seasons' crop tags no longer
matter: its crop rule is off since crop seasons (`farming/crops #5`).

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
Decide and apply each open G finding in crops.md §4 (lines 1126-1145). G4, G6, G7, G10, G11 stay as mod design.

### Now and Build, per finding

**G8 Weeds replant on right-click.** Verified: `WeedsBlock extends CropBlock`, so Quark's search records
`immersive_weathering:weeds[age=7] -> age 0`. `#quark:simple_harvest_blacklisted` is checked in `handle()`, in the
hoe `click()` pre-check and in `harvestAndReplant()`, so a blacklisted block is never harvested or replanted by
Quark at all (right-click does nothing; break it). Build: in
[blocktags.js](../../kubejs/server_scripts/tags/blocktags.js) "Crop parity" section (line 501 on) add
`t.add(e, 'quark:simple_harvest_blacklisted', item.weeds)` (`i.weeds`, item_dict.js line 10894; not set anywhere yet,
checked 2026-10-31). Data only,
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
- Tag (blocktags.js, Crop parity section): `b.flax_crop`, `b.corn_crop`. Both keys already exist in
  [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js) (lines 120 and 96, checked 2026-10-31).
- Helper: `crops/CropParity.HARVEST_FROM_BELOW` TagKey + `isHarvestFromBelow(state)`, same style as the two existing
  keys.
Unverified: flax's top-half state property name (`half`), not needed because the mixin compares blocks, not states.

**G12 tea refuses Farmland.** Verified: `SmallTeaBushBlock` and `TeaBushBlock` both override `mayPlaceOn`
(`m_6266_`) to `state.is(BlockTags.DIRT)` and their own `canSurvive` (`m_7898_`) calls it (TeaBush only for the lower
half). Build `mixin/TeaBushSoilMixin` targeting both classes (`@Mixin(value = {SmallTeaBushBlock.class,
TeaBushBlock.class}, remap = false)`), `@Inject(method = "m_6266_", at = @At("RETURN"), cancellable = true)`:
`if (!cir.getReturnValue() && CropParity.isFarmlandSoil(state)) cir.setReturnValue(true)`. Compile jar
`FarmersRespite-1.19-2.0.jar` is already in `development/creativeCommon/libs/` (checked 2026-10-31). Value is low: tea growth is a flat 1/20 (small) and
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
replant, hoe area replant, hoe area break. Follow the §2.6 checklist (crops.md §2.6, from line 327).

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
1. **Hemp right-click** (DONE 2026-11-02 another way, `farming/crops #37`: CD server script
   `kubejs/data/creativedelight/kubejs/immersiveengineering/hemp_harvest.js`, no mixin; the plan below is superseded): a `Harvestable Blocks` line cannot express "break the top": the replant state must be a
   real state, and `minecraft:air` as the replant state puts AIR into `cropBlocks` (every air block then passes
   Quark's first check) while the hemp block itself stays out of `cropBlocks`. Instead add a second rule to the
   crops #1 mixin class: tag `creativeengineering:crops/harvest_top_break` (hemp); in `handle` HEAD (`@Inject`,
   cancellable), if the state at `pos` is in that tag and is its ripe top (`growth=top0`, read the property by name
   so no IE compile dependency is needed), call `level.destroyBlock(pos, true, player)` and return true. The bottom
   (`bottom4`) regrows the top. Hoe area right-click then harvests a hemp row. Add hemp to
   `crops/farmland_crops`? Not needed: it is `IPlantable` (Forge farmland logic and the hoe break already work).
2. **Grapes:** the port is in-tree (`development/creativeBrewing/members/vinery`), so edit `GrapeBushBlock.mayPlaceOn`
   (lines 122-123) instead of a mixin:
   `return floor.isSolidRender(level, pos) || floor.is(GRAPE_SOILS)` with
   `TagKey<Block> GRAPE_SOILS = BlockTags.create(new ResourceLocation("vinery", "grape_bush_soils"))` in the port's
   `TagRegistry`, filled in blocktags.js with `#creativeengineering:crops/farmland_soils`. Taiga bushes keep their
   podzol / coarse dirt / grass-within-4 growth rule. Build through `:creativeBrewing:` only (never the member's own
   task) and bump the member's `-ce-19.2-NN` suffix.
3. **Pineapple replant:** `Harvestable Blocks` line `"fruitsdelight:pineapple[age=4]"` in
   [config/quark-common.toml](../../config/quark-common.toml) line 780 (also `configs_edited/root_config/` and the
   CreEngServer mirror, as for tobacco; the line today ends with the tobacco entry, checked 2026-10-31). The ripe table
   drops only the pineapple, so Quark's "take one block item"
   finds no sapling and the replant is free. Gotcha: pineapple is a non-vanilla `BushBlock` + `BonemealableBlock`,
   so it is also in `rightClickCrops`, and with a hoe `getAction` returns CLICK before HARVEST (the plant has no
   `use`, so nothing happens). Fix in the same mixin class: redirect `getAction`, or in `handle` HEAD treat a state
   that is a key of `SimpleHarvestModule.crops` as HARVEST. Empty-hand replant works without it. Note
   `mixin/QuarkOwnPickMixin` (built since this brief) already intercepts the same Quark decision for
   `#creativeengineering:crops/own_pick`; put the pineapple rule next to it.
4. **Tea, flax, corn, weeds, pineapple soils:** crops #1.
5. Add each crop's §2.1 row update and a CR test; update the §2.6 "Code, written once" table with the new mixins.

### Ids and data
- Blocks without items in [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js): `b.hemp` (line 121) and
  `b.corn_crop` (96) exist; `b.red_grape_bush = 'vinery:red_grape_bush'` and the five other bushes only if a tag needs
  them (the soil tag does not).
- Items in the dict: `i.seed` (IE hemp seed, item_dict.js 4053), `i.pineapple` (13265), `i.pineapple_sapling` (13266).
- New tags: `creativeengineering:crops/harvest_top_break`, `vinery:grape_bush_soils`.

### Decisions
- Which crops? Recommendation: hemp, flax, corn, tea, pineapple, weeds (as G8) and grape soils; reject coffee.
- Grapes on farmland: it breaks Vinery's "full block" rule (G7). Recommendation: allow the three farmland soils only;
  stems and lattices keep their own rules.
- Free pineapple replant (no sapling cost)? Recommendation: yes, like crops whose block has no item (crops.md §2.2).

### Gotchas
- One patcherMod build for crops #1, #2 and #4; register new mixins in
  `development/creativeCommon/src/main/resources/creativecommon.mixins.json` next to `TobaccoPlacementMixin` (line 140)
  and the Quark mixins (148-154).
- `libs/` compile jars are not in git: Farmer's Respite for the tea mixin; the hemp rule reads properties by name.
- The Vinery port change ships in the Creative Brewing jar (`creativebrewing-*.jar` in `mods/`); install it with the
  patcherMod jar, never while the game runs.

### Tests
- **CR-new1 Hemp right-click** (proposed, numbered when queued; was "CR-70", taken by bell peppers): right-click a grown hemp top -> fibre + seeds drop, the bottom stays at full growth and
  grows a new top. Iron hoe right-click on a row -> every top in the 3x3 harvested.
- **CR-new2 Grapes on farmland:** red grape seeds on Rich Farmland and Fertile Soil plant and grow; on plain dirt still
  plant (full block); taiga seeds still need podzol / coarse dirt / grass within 4 to grow.
- **CR-new3 Pineapple replant:** right-click a ripe pineapple -> one pineapple, back to age 0, no sapling used. Iron hoe
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
- **CR-new1 Weeds on rich soils** (proposed; "CR-73" is taken by extra stem fruit): weed item on Rich Soil and Fertile Soil -> refused; on dirt and farmland -> plants.
- **CR-new2 Weeds spawn only on vanilla farmland:** two 9x9 fields of wheat, one on farmland and one on Rich Farmland,
  both with tall grass at the border, `/gamerule randomTickSpeed 300` for 5 minutes in clear weather -> weeds only in
  the farmland field.

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
   a new `crops/StemFruitStages`; there is no `CropRegistry` class, checked 2026-10-31).
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
- Crop seasons (`seasons/CropSeasons`, since 2026-10-30; Serene Seasons' crop rule is off): stems are typed (melon
  warm wet, pumpkin temperate, hamimelon warm dry). The growing block must call `ForgeHooks.onCropsGrowPre` and be
  added to its stem's type in `kubejs/generation_scripts/worldgen/crop_seasons.json`, or it grows at full speed off
  season.
- Extra stem fruit (`crops/StemExtraFruit`, crops.md 2.10) places `fruit.defaultBlockState()` directly and counts
  neighbours with `is(fruit)`: route its placement through the same fruit -> growing block map, and count a growing
  block as a fruit for the cap and for `reattach`. Do this in one build with `farming/crops #20`, which changes the
  same attached-stem code.
- New chunks: worldgen pumpkin patches place the full pumpkin block directly, unaffected.

### Tests
- **CR-new1 Fruit stages** (proposed, numbered when queued; "CR-78" was never written to crops.md): grow a pumpkin stem at `randomTickSpeed 100` -> a small pumpkin appears attached, grows two
  steps to a full pumpkin, the stem stays attached throughout. Break the small one -> the stem detaches and later
  grows another.

---

## crops #14: Bone meal in planters
Status **Planned** / Work patcherMod / KubeJS / TODO `farming/crops #14`

### Goal
Bone meal on a crop standing in a Supplementaries planter does what the pack intends (the row names no intent).

### Now
- Planters: `i.planter` (`supplementaries:planter`) and `i.planter_rich` (`supplementaries:planter_rich`), item_dict 3735-3736.
  Supplementaries 2.4.20 `PlanterBlock` (decompiled outline with `javap`) extends Moonlight's `WaterBlock` and has shape,
  `EXTENDED`, `onTreeGrow`, and `canConnect`: no `randomTick`, no `BonemealableBlock`, no `canSustainPlant` in the class. The plant
  is an ordinary block on top of the planter, so vanilla bone meal on the plant works if the crop is bonemealable; the planter
  adds nothing. Whether the rich planter is another class or only a block tag is unverified.
- What the pack does with soils ([crops.md](../crops.md) 2.4): Rich Soil and Rich Farmland give a 20% bone meal step on random
  ticks (Farmer's Delight's own rule, `richSoilBoostChance = 0.2` in `config/farmersdelight-common.toml` line 12); Fertile Soil (`immersive_weathering:mulch_block`) gives a step per soaked random tick (`MulchBlockCropGrowthMixin`).
  `planter_rich` is not in any of those rules.

### Build
1. Test first: bone meal on wheat, a bush and a tomato in a planter and a rich planter, and on dirt next to them.
2. If the wanted rule is "a rich planter acts like Rich Soil" (20% step per random tick on the plant above): the planter has no
   random tick, so a patcherMod mixin on `PlanterBlock` (implement `isRandomlyTicking` + `randomTick`: with
   `richSoilBoostChance` from FD's config, bone-meal the plant above if it is `BonemealableBlock`, skipping
   `#farmersdelight:unaffected_by_rich_soil` as FD does) or a block-tag-driven hook in the existing soil mixins.
3. If the wanted rule is only "bone meal works": nothing to build; document it.

### Decisions
Intended behaviour: (a) bone meal works as on soil (probably true already), (b) rich planter boosts like Rich Soil, (c) planters
block bone meal. Recommend (a)+(b).

### Tests (CR-)
- CR-new1 (proposed, numbered when queued; "CR-89" is taken by crop seasons): bone meal on a planted crop in each planter kind grows it; (b) a rich planter crop grows faster over time
  than a plain one at `randomTickSpeed 100`.

---

## crops #15: Other plants in random growth?
Status **Planned** (question) / Work KubeJS / data / TODO `farming/crops #15`

### Goal
Settle which "random growth" mechanic the scratchpad question means and which plants it should cover.

### Now
The note names no system. Candidates in the pack:
- **Soil boost on random ticks** (crops.md 2.4): Rich Soil's `richSoilBoostChance` (20% per random tick to bone-meal the plant above;
  not for farmland crops on plain Rich Soil) and Fertile Soil via `MulchBlockCropGrowthMixin` (skips `CropBlock`s, IW grows
  those; for the rest it reads `#creativeengineering:crops/farmland_crops`, `CropParity.FARMLAND_CROPS`, checked 2026-10-31).
  Adding plants = adding them to that tag through `global.tags` (it also changes Quark hoe break and farmland drying, see
  blocktags.js line 505).
- **Crop seasons' fertile bonus** (`seasons/CropSeasons`, crops.md 2.12.1): a fertile season gives `fertileBonus` 25 % of
  growth checks a second random tick. Only plants whose growth fires Forge's crop-grow event get it; the others needed
  hooks (`mixin/CropSeasonGrowHookMixin`: hemp, tobacco, fungus colonies). Adding plants = typing them in
  `crop_seasons.json` and, where the event is missing, a hook.
- **Immersive Weathering block growths** (the Weathering and Growth JEI tab, `jei_world/block_growth`): data-driven spread
  (moss, weeds, leaf piles, coral); plants are added by datapack json in `block_growths/`.
- **Stem extra fruit** (`StemExtraFruit`, [crops.md](../crops.md) 2.10, CEConfig `[stem_fruit]`): melons and pumpkins already.
- **Mushroom colonies / Fruits Delight bushes / tea** random-tick growth (crops.md lines 149-150, 191).

### Build
After the user names the system: for (1) add the plants to the soil tag through `global.tags`; for (2) a datapack growth json in
`kubejs/data/immersive_weathering/block_growths/` (see `gameplay/misc #15` for the format); for (3) extend the stem list.

### Decisions
Which system and which plants. Recommend asking; the most likely reading is the soil boost (row `#14` is about the same area).

### Tests (CR-)
- CR-new1 (proposed, numbered when queued; "CR-90" is taken by crop seasons): after the answer, one plant of the new set grows faster on the boosted soil than on plain dirt.

---

## crops #27: Sea grape plant mineable with a hoe
Status **Planned** · Work KubeJS · id `farming/crops #27`

### Goal
A hoe mines the Oceanic Delight sea grape plant at tool speed, as the newer Oceanic jar does (the fruit already is).

### Now
- Decided 2026-10-25 ([ocean_delight.md](../ocean_delight.md) 3.1, table row "sea grape plant mineable with a hoe", test
  DA-15).
- The repacked Oceanic Delight 1.0.1 (`development/creativeDelight/members/oceanic`, upstream classes and data unchanged)
  ships `data/minecraft/tags/blocks/mineable/hoe.json` with only `oceanic_delight:sea_grape_fruit` (read from
  `jar_archive/repackedOceanic-1.0.1-ce-19.2-01.jar` 2026-10-31). Blocks in the jar: `sea_grape_plant`,
  `sea_grape_fruit`, `wild_sea_grape`, `sea_grape_crate`.
- Dict: `i.sea_grape_plant` (item_dict.js 14679). block_dict has `b.t_mineable_axe` / `b.t_mineable_pickaxe` (lines
  150-151) but no hoe key.
- CD has no `oceanic_delight` script folder: the Oceanic tags sit in
  [oceansdelight/tags.js](../../kubejs/data/creativedelight/kubejs/oceansdelight/tags.js) (item tags only, gated by
  `global.CD.on('oceanic_delight')`).

### Build
1. block_dict.js: `b.t_mineable_hoe = '#minecraft:mineable/hoe'` beside line 151.
2. `oceansdelight/tags.js`: add a `ServerEvents.tags('block', e => { ... })` block in the shape of
   `veggiesdelight/tags.js:16-20`, gated `global.CD.on('oceanic_delight')`:
   `global.tags.add(e, b.t_mineable_hoe, i.sea_grape_plant)`.
3. `/reload`. Update ocean_delight.md 3.1 (row -> built) and its status line.

### Decisions
- Also `wild_sea_grape`? Recommendation: no. The row and the newer jar cover the plant only, and the wild block is a
  forage block that breaks instantly anyway.

### Gotchas
- Mining speed only: the plant's loot has no tool condition, so drops do not change.

### Tests
- **DA-15** (exists, ocean_delight.md): a hoe breaks a sea grape plant and its fruit at hoe speed.

---

## crops #29: Tomato ropes and sticks harvestable with the hoe harvest area
Status **Planned** · Work patcherMod (tag first) · id `farming/crops #29`

**BUILT 2026-11-02** (pre-scratch pass): steps 1-3 done in Creative Delight (crop code moved there with the split):
four tomato blocks tagged own-pick (`farmersdelight/tags.js`, new keys `b.stick_tomatoes`, `b.rope_tomatoes`),
`QuarkHoeAreaHarvestMixin` climbs own-pick columns (`HoeHarvestArea.MAX_CLIMB = 6` above the second level). FD's
`TomatoVineBlock.use` picks with any held item when ripe (checked), and the Supplementaries and treated-stick vines
inherit it. crops.md CR-101, CR-102. `farming/crops #38` (treated sticks placeable, tomatoes on them) was already
built 2026-10-31 ([treated_sticks.md](../treated_sticks.md)).

### Goal
A hoe area right-click also picks ripe tomatoes on ropes, Supplementaries sticks and treated sticks, not only the bottom
vine.

### Now
- Tomato blocks: `farmersdelight:budding_tomatoes` -> `farmersdelight:tomatoes` (`b.tomatoes`, block_dict.js 110; climbs
  ropes through its `ropelogged` state), Supplementaries `supplementaries:stick_tomatoes` and `rope_tomatoes`
  (blockstates in `supplementaries-1.19.2-2.4.20.jar`, verified 2026-10-31), CD `creativedelight:treated_stick_tomatoes`
  (`b.treated_stick_tomatoes`, block_dict.js 113, `treatedstick/TreatedStickTomatoBlock`). All use FD's tomato own pick
  ([crops.md](../crops.md) 2.1 tomato row and lines 111-113).
- Quark records only the default ripe state, so it knows the bottom vine and nothing else (crops.md 2.2 "Exact state
  match"). The hoe area (`mixin/QuarkHoeAreaHarvestMixin`, `crops/HoeHarvestArea`) handles the clicked height and one
  above (`shiftPos.above()`, mixin line 47); rope and stick states are neither Quark crops nor bush clicks, so they are
  skipped.
- `#creativeengineering:crops/own_pick` (`b.t_own_pick_crops`, block_dict.js 143; `CropParity.OWN_PICK_CROPS`) gives a
  crop Quark's berry-bush treatment through `mixin/QuarkOwnPickMixin`: an empty hand or a 1x1 hoe falls through to the
  block's own pick, a hoe area clicks each one. Cucumber, ghost pepper and zucchini use it already.

### Build
1. Tag the four tomato blocks into `b.t_own_pick_crops`: FD tomatoes and the treated stick in CD
   [farmersdelight/tags.js](../../kubejs/data/creativedelight/kubejs/farmersdelight/tags.js) block event (line 34);
   the two Supplementaries blocks need new keys `b.stick_tomatoes`, `b.rope_tomatoes` in block_dict.js. `/reload`.
   The bottom vine changes from Quark replant to FD's pick: both reset to stage 0 (crops.md line 111), so the
   result is the same.
2. Height (patcherMod): vines on ropes run several blocks up and the area reaches only one block above the click. In
   `QuarkHoeAreaHarvestMixin`, for own-pick blocks walk the column up from the area's top while the next block is the
   same block, up to a cap (`HoeHarvestArea.MAX_CLIMB`, say 6), clicking each.
3. crops.md 2.1 tomato row (`Hoe RC` column) and 2.3 (area mechanics).

### Decisions
- Climb the column (step 2) or only the two heights? Recommendation: climb, capped; tomato ropes are the main case.
- Durability: Quark charges per click. Recommendation: keep Quark's cost, no special case.

### Gotchas
- FD's tomato `use` may refuse when the held item is bone meal or another item (unverified for the hoe: check
  `TomatoVineBlock.use` in the FD member jar before relying on step 1 alone).
- Tagging a `CropBlock` as own pick also takes it out of Quark's replant for the empty hand; that is intended here.

### Tests
- **CR-new1** (proposed, numbered when queued): iron hoe right-click on a ripe 3x3 of tomatoes, one column each on rope,
  stick and treated stick: every ripe vine in reach drops tomatoes and stays, no vine broken.
- **CR-new2**: a 5-high rope of ripe tomatoes, hoe area click at the bottom: every ripe level picked (after step 2).

---

## crops #17: Wet farmland from buckets and water bottles
Status **Planned** · Work KubeJS (or patcherMod) · id `farming/crops #17`

### Goal
A water bucket or a water bottle used on Farmland, Rich Farmland or Fertile Soil soaks it (moisture 7).

### Now
- All three soils extend `FarmBlock` and carry `moisture` 0-7: `minecraft:farmland` (`i.farmland`, item_dict 2357),
  `farmersdelight:rich_soil_farmland` (`i.rich_farmland`, 676), `immersive_weathering:mulch_block` (`i.fertile_soil`,
  677).
- **Fertile Soil already takes a bucket** (IW 1.2.10 `MulchBlock.use`, bytecode read 2026-10-31): not sneaking, water
  bucket on a dry mulch -> moisture 7, bucket emptied (creative keeps it), splash sound and particles; empty bucket on a
  soaked mulch -> moisture 0 and a water bucket back. Bottles are not handled.
- Farmland and Rich Farmland: a water bucket places a water source on the clicked face; a water bottle does nothing
  (vanilla makes mud only from dirt).
- Reference behaviour: the watering can (CD `hearth` member,
  [WateringCanItem.java:308-334](../../development/creativeDelight/members/hearth/src/main/java/com/SaintAlfonzo/creativedelight/hearth/item/WateringCanItem.java)):
  any block with `FarmBlock.MOISTURE` is soil, set to `MAX_MOISTURE`. Moisture then dries as vanilla.
- Dict: `i.water_bucket` (201), `i.bucket` (2188), `i.water_bottle` (1305, an nbt ingredient: `minecraft:potion` with
  `Potion: minecraft:water`), `i.glass_bottle` (746).

### Build
1. New server script `kubejs/server_scripts/other/wet_soil.js` (farming is not food, so not a CD tree):
   `BlockEvents.rightClicked` on the three soils; skip when sneaking or when the soil is already at 7.
   - water bucket on Farmland / Rich Farmland: set `moisture` 7, give `i.bucket` back (not in creative), sound
     `minecraft:item.bucket.empty`, cancel the event (stops the water source). Fertile Soil: leave to IW.
   - water bottle (item `i.potion` with the water nbt) on all three: moisture 7, `i.glass_bottle` back, sound
     `minecraft:item.bottle.empty`, cancel.
2. Changelog bullet (Farming family) and a crops.md 2.4 line.
3. Fallback if the client shows a ghost water block (see Gotchas): the same rule as a patcherMod `@SubscribeEvent` on
   `PlayerInteractEvent.RightClickBlock`, which both sides run.

### Decisions
- Which soils: all three (recommended, as the row says).
- Container: returned empty (recommended; matches IW's mulch and the can's refill model).
- Bucket vs placing water: plain click soaks, sneak-click places water as vanilla (recommended; IW uses the same
  secondary-use check).
- Area: one block (recommended; the can is the area tool).

### Gotchas
- KubeJS server scripts cancel only on the server; the client may predict the bucket use and flash a water block. Test
  it; use the patcherMod fallback if it shows.
- Dry-out is vanilla: without water within 4 blocks or rain, the soil loses moisture again, as after the can.

### Tests
- **CR-new1** (proposed, numbered when queued): water bucket on dry Farmland and Rich Farmland -> soaked, empty bucket
  back, no water placed; sneaking places water.
- **CR-new2**: water bottle on dry Farmland, Rich Farmland and Fertile Soil -> soaked, glass bottle back; creative keeps
  the items.

---

## crops #21: Melon stems not on fertile soil; check other melon-like crops
Status **Planned** · Work KubeJS / data (or patcherMod) · id `farming/crops #21`

**BUILT 2026-11-02, reading 2** (plan_prescratch_fixes.md: "drop the fertile-soil rule"): stems no longer get IW's
10 extra random ticks on soaked Fertile Soil (CD `MulchBlockCropGrowthMixin` redirect, `[stem_fruit] fertileSoilBoost`,
default false). Audit: the rule was IW's `instanceof StemBlock` branch, so it covered melon, pumpkin and cantaloupe
stems alike; all three drop it. The Mini Cantaloupe (sand by water) never had it. Reading 1 (stems on plain soil) not
built. crops.md CR-104.

### Goal
Settle the rule the scratchpad note means ("Melon stems do not need fertile soil; check melon-like crops", scratchpad.md
line 121) and apply it to every melon-like crop.

### Now (checked 2026-10-31)
- Stem soils ([crops.md](../crops.md) 2.1 "Stems (melon-like)"): pumpkin, melon and hamimelon stems plant on Farmland,
  Rich Farmland and Fertile Soil, not on dirt or untilled Rich Soil (vanilla `StemBlock.mayPlaceOn` = farmland, Forge's
  crop plant type). The fruit can land on any soil next to the stem, plain dirt and grass included.
- Fertile Soil boosts stems: Immersive Weathering's `mulch_growth_random_ticks = 10`
  (`config/immersive_weathering-common.toml` line 87) gives a soaked mulch's stem 10 extra random ticks (crops.md 2.4).
- Other melon-like crops: Fruits Delight's hamimelon stem (now named Cantaloupe, item_merges.md 2.10) follows the
  vanilla stem rule; the Mini Cantaloupe plant (`delightful:cantaloupe_plant`, CD `delightful` member
  `CantaloupePlantBlock`, lines 75-76 and 107-113) plants only on `#forge:sand` with water or frosted ice beside it;
  the planned mini melon (`farming/crops #25`) would copy that.
- Crop seasons call a good season "fertile" (crops.md 2.12.1); melon stems are `warm_wet`.

### Readings (ask the owner which one)
1. **Melon stems should plant on plain soil** (dirt, grass, untilled Rich Soil), not only tilled soils. Build: a
   patcherMod mixin on `StemBlock.mayPlaceOn` (`m_6266_`) RETURN, true when the soil is in a new tag
   `creativeengineering:crops/stem_soils` (filled through `global.tags` in blocktags.js Crop parity section). Applies to
   all three stems at once.
2. **Melon stems should not be boosted by Fertile Soil** (the 10 extra random ticks make stems on mulch outgrow
   everything). Build: IW's setting is global, so either lower `mulch_growth_random_ticks` (config, also
   `configs_edited/root_config/`), or a patcherMod hook that skips the extra ticks for `StemBlock`s.
3. **Seasons wording:** melons grow at full speed outside their fertile season (no "off" penalty). Build: a season type
   change in `crop_seasons.json`, no code.
Recommendation: reading 1 is the most literal ("do not need" a special soil). Ask before building.

### Ids and data
`minecraft:melon_stem`, `pumpkin_stem`, `fruitsdelight:hamimelon_stem`, `delightful:cantaloupe_plant`; new tag
`creativeengineering:crops/stem_soils` (reading 1).

### Decisions
- The reading (above).
- Mini Cantaloupe's sand-and-water rule: keep (Delightful design, the plant is a beach crop) or relax with the same tag?
  Recommendation: keep, and give the mini melon the same rule.

### Gotchas
- Reading 1 also lets stems grow on grass in villages and wild patches; worldgen places fruit blocks, not stems, so
  nothing changes there.
- Vanilla `StemBlock.randomTick` growth speed reads the farmland moisture under the stem; on dirt it is slower (no
  moisture bonus), which is the natural penalty.

### Tests
- **CR-new1** (proposed, numbered when queued): reading 1: melon, pumpkin and cantaloupe seeds plant on dirt, grass and
  Rich Soil and still on the three farmland soils; the Mini Cantaloupe still needs sand by water.

---

## crops #28: Grasses: winter offseason only
Status **Built 2026-11-02** (Creative Calendar: `GRASS` type, `GrassSpreadSeasonMixin`, `IWGrowthSeasonMixin`; json list; crops.md 2.12.1 "Grasses" and "Spreading plants"; modded grasses typed and hooked 2026-11-02 by owner decision: `ModdedGrassSpreadSeasonMixin`) · Work Creative Calendar / data · id `farming/crops #28`

### Goal
Grass gets a season type with only a winter offseason (off = 25 % slower spreading, other seasons neutral), and the
crop season types are checked for every plant that spreads.

### Now (checked 2026-10-31)
- Crop seasons: `seasons/CropSeasons` reads the block tags `creativeengineering:crop_seasons/<type>` that
  `cropseasontags.js` fills from
  [crop_seasons.json](../../kubejs/generation_scripts/worldgen/crop_seasons.json). Types are an enum
  (`CropSeasons.Type`, lines 83-97) with a fertility per season (spring, summer, autumn, winter) and per tropical
  season (wet, dry). Off refuses `offPenalty` (0.25) of Forge crop-grow checks, fertile adds `fertileBonus` (0.25)
  (`config/creativeengineering-common.toml` lines 2591-2594).
- No grass is typed: no `grass`, `fern` or grass block entry in any type list. The spreading plants that are typed
  (mint, mulberry and vanilla vines, cactus, sugar cane, bamboo, the Nether vines) grow through the crop-grow event.
- Grass spreads through code that fires no crop-grow event: vanilla `SpreadingSnowyDirtBlock.randomTick` (`m_213898_`,
  grass block onto dirt) and Immersive Weathering's block growths (`grass_block_*.json`, `tall_grass.json`,
  `large_fern.json` in the IW jar, run by `BlockGrowthHandler`; owner block = grass block). So a type alone does nothing.

### Build
1. `CropSeasons.Type`: add `GRASS("grass", {N, N, N, O}, {N, N})` (winter off; tropical neutral both seasons), its
   readme line in `crop_seasons.json` and an empty `"grass"` list.
2. Hooks (patcherMod, `mixin/CropSeasonGrowHookMixin` style): `SpreadingSnowyDirtBlock.randomTick` HEAD, and IW's
   growth tick for grass-block owners: when `CropSeasons.fertility(level, pos, Type.GRASS)` is off, skip the tick with
   chance `offPenalty`. Read the type from the block (`minecraft:grass_block`, and the tall grass / fern plants if they
   are typed) so the json stays the switch.
3. Fill the list (`minecraft:grass_block`, `minecraft:grass`, `tall_grass`, `fern`, `large_fern`; decide on modded
   grasses), run `tools/crop_season_audit.py`, update crops.md 2.12.1 (type table) and the item tooltip / WTHIT text
   if they list types.
4. Spreading-plant check (the row's second half): add a column to `crop_season_audit.py` output or a crops.md 2.12.1
   note listing which typed plants spread and whether their spread runs through the event (mint, vines, bamboo, weeds).

### Decisions
- Which blocks count as "grasses": grass block spread only, or also the tall grass / fern growths? Recommendation: both
  (the note says "spreading").
- Tropical dry season: off or neutral? Recommendation: neutral (the note says winter only).
- Mycelium and nylium spread through the same vanilla class: leave them out (recommended; the hook checks the type).
- Modded grasses (owner 2026-11-02): slowed by winter too. Typed: IW rooted grass block, Vinery grass slab, Vintage
  lush grass block, Botania dry / golden / vivid / scorched / infused / mutated grass; hooked by
  `ModdedGrassSpreadSeasonMixin` (IW `ModGrassBlock` extra spread, Botania `BotaniaGrassBlock.tick`, Vinery
  `SpreadableGrassSlabBlock`, Vintage `LushGrassBlock`). Left out: IW frosty grass / fern, Upgrade Aquatic beachgrass,
  SecurityCraft reinforced grass (reasons in crops.md 2.12.1 "Grasses").

### Gotchas
- `SpreadingSnowyDirtBlock` also handles grass turning back to dirt under a block; skip only the spread part, or the
  off chance also slows grass death (harmless but note it).
- Greenhouse glass makes any season neutral per column (`glassAbove`); grass under glass then never slows.

### Tests
- **CR-new1** (proposed, numbered when queued): in a temperate biome in winter (`/season set mid_winter`), bare dirt next to
  grass at `randomTickSpeed 100`: fewer conversions than the same setup in spring over the same time (rough, about
  25 % fewer).
- **CR-new2**: WTHIT / tooltip on a grass block shows the grass type line.

---

## crops #19: Village farms grow Rustic crops by the wild-crop rules
Status **Built 2026-11-02** (village_farms.json + crops.md 2.11 table edited; pale and dark peppers left out) · Work Creative Delight / data · id `farming/crops #19`

### Goal
Rustic Delight crops appear in village farms only in the climates where their wild patches grow (extends
`farming/crops #16`).

### Now (checked 2026-10-31)
- [village_farms.json](../../development/creativeDelight/src/main/resources/creativedelight/village_farms.json): Rustic
  `bell_peppers`, `cotton` and `coffee` sit in both `warm_dry` (lines 54-56) and `warm_wet` (69-71). The review copy is
  the crops.md 2.11 "Crops per group" table (Bell Peppers x / W, Cotton W / x, Coffee x / W). The roll ignores biome
  rules: group membership is the only rule.
- Wild rules ([wild_crops.json](../../kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json)): `cotton` warm dry
  grassy; `bell_pepper` warm wet `!cliff`; `coffee` warm wet `!cliff` (replaces Farmer's Respite's coffee bush);
  `giant_bell_pepper` warm dry grassy (a decoration block, not a farm crop).
- Pale and dark bell peppers (`rusticdelight:pale_bell_peppers`, `dark_bell_peppers`, CD `rustic` member
  `RDBlocks.java` line 82 on) are in no group; their wild forms come inside the bell pepper patch
  ([rustic_delight.md](../rustic_delight.md) 2.1, "Six more pepper colours").

### Build
1. Edit the crops.md 2.11 table first (it is the review copy): bell peppers and coffee warm wet only, cotton warm dry
   only (each stays W).
2. Regenerate `village_farms.json` from the table (the writeup says "ask for it"), rebuild `:creativeDelight:`.
3. Changelog: amend the existing village farm bullet if one is still unreleased, else a new line under the village farm
   family.

### Decisions
- Pale and dark peppers in farms? The owner list (rustic_delight.md section 5, line 123) puts Dark seeds in ancient cities and Pale
  seeds in sky villages. Recommendation: leave them out of farms; sky villages use the `auto` group, so Pale peppers
  there would need a per-template rule (not available today).
- A general "farm crop follows its wild rule" lookup: belongs to the planned ruins / pillager system (crops.md 2.11
  "Planned"). Recommendation: data edit now, no code.

### Gotchas
- New villages only (the processor runs at placement).

### Tests
- **SQ-VF-new1** (proposed, numbered when queued): `/place` a savanna farm and a jungle-style warm wet farm 20 times each:
  cotton only in savanna fields, bell peppers and coffee only in the warm wet ones.

---

## crops #24: Better soils raise nether crop yield
Status **Planned** · Work KubeJS / patcherMod · id `farming/crops #24`

### Goal
The soil yield of `farming/crops #23` for nether crops: extra produce on Nulch (Fertile Soul Soil) and Rich Soul Soil.

### Now
- The outline is [crop_yields.md](../crop_yields.md) 4 (owner: outline only): nether wart, ghost pepper, propelplant,
  crimson and warped fungus colonies; a second soil list `[crop_drops.soil_yield.nether]`, crop switches all off.
- Overworld code: CD
  [creativeengineering/soil_yield.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/soil_yield.js),
  CEConfig `[crop_drops.soil_yield]` (`CEConfig.java` around lines 688-743; `CROPS` table order = config keys).
- Dict keys (checked 2026-10-31): `i.nether_wart` (642), `i.nulch` (678, `immersive_weathering:nulch_block`),
  `i.soul_sand` (1288), `i.soul_soil` (1289), `i.crimson_fungus_colony` / `i.warped_fungus_colony` (13340, 13342),
  `i.propelplant_cane` (13341); `b.soul_soil` (84), `b.rich_soul_soil` (85), `b.ghost_pepper_crop` (101). Missing:
  `b.nether_wart` (block), the propelplant block keys.
- Ghost pepper soils: `#vintagedelight:ghost_pepper_soil` = soul soil, Rich Soul Soil, Nulch (CD
  `vintagedelight/tags.js:48`).

### Build
Follow crop_yields.md 4 steps 1 to 4; the per-crop choices there are this row's decisions. Order: nether wart first (a
plain table pool on age 3), then ghost pepper (break only; its right-click is an own pick), then the colonies after the
ripe-age choice. Propelplant needs the pick decision first.

### Decisions
- Propelplant: table pool only (reaches only a broken cane) or a patcherMod hook on its pick? Recommendation: table only
  for now; a hook breaks the data-only design.
- Colony ripe age and whether shears and break both count. Recommendation: max age only, break and shears both.
- Soil values: recommend Rich Soul Soil = Rich Farmland's (2 x 35 %), Nulch = Fertile Soil's (2 x 25 %), soul sand and
  soul soil 0.

### Gotchas
- A switch change needs a restart, not `/reload` (crop_yields.md, as for #23).
- Everything off by default: no player-visible change until a crop is switched on.

### Tests
- **SQ-SY-08** onwards (crop_yields.md 4 step 4 names them; numbered when queued): per crop, ripe yield on soul sand vs
  Nulch vs Rich Soul Soil with the switch on, the base table unchanged with it off.

---

## crops #26: Soul soil upgrade chain
Status **Planned** · Work KubeJS / data / resource pack · id `farming/crops #26`

### Goal
A nether soil ladder like the overworld one (dirt -> Rich Soil -> Fertile Soil): soul soil -> Rich Soul Soil -> Fertile
Soul Soil (Nulch), with Rich Soul Soil an input of the Nulch recipe. Maybe recolour Rich Soul Soil.

### Now (checked 2026-10-31)
- Overworld: Fertile Soil recipes take Rich Soil plus mulches
  ([sawdust.js](../../kubejs/data/creativeengineering/kubejs/wood/sawdust.js) lines 67-81, `R.shaped` / `R.createMixing`).
- Nulch (`i.nulch`, renamed Fertile Soul Soil): `sawdust.js` lines 83-86, 3 netherrack, 2 scales, ash, magma cream,
  nether wart, blaze powder -> 6 (Create mixing: 2 netherrack, 1 scale). No soul soil, no Rich Soul Soil. Line 83 is the
  note "Integrate nulch, move to compost file".
- Rich Soul Soil: `nethersdelight:rich_soul_soil` (`b.rich_soul_soil`, block_dict 85; no item key), made by Nether's
  Delight's soul compost (`i.soul_compost`, item_dict 12219) maturing; texture
  `assets/nethersdelight/textures/block/rich_soul_soil.png` (`NethersDelight-1.19-3.1.jar`).
- **Overlap:** `farming/composting #2` reviews the same Nulch recipe ("its new job as a ghost pepper soil", same
  `sawdust.js` line 83). This row's chain is that review's answer; do both in one change.

### Build
1. Item key `i.rich_soul_soil = 'nethersdelight:rich_soul_soil'` in item_dict.js.
2. With `composting #2`: new Nulch recipes in the compost file (move them out of `sawdust.js`), with Rich Soul Soil in
   the place Rich Soil has in the Fertile Soil recipes (e.g. 3 Rich Soul Soil + the nether mulch set -> 3 or 6 Nulch),
   shaped and Create mixing through `global.NCH`; `R.remove` stays for the IW recipe.
3. Optional recolour: a texture override at `kubejs/assets/nethersdelight/textures/block/rich_soul_soil.png` (client;
   from Nether's Delight's own texture, licence check first).
4. crops.md 2.4 soil table and composting.md F4.

### Decisions
- Recolour or not. Recommendation: yes if Rich Soul Soil and Nulch look alike in play; check both side by side first.
- Keep a netherrack-based Nulch route without Rich Soul Soil? Recommendation: no, the chain is the point (as Fertile
  Soil has no route without Rich Soil).

### Gotchas
- Ghost pepper soils already accept all three soul soils; the chain changes recipes only.

### Tests
- **CR-new1** (proposed, numbered when queued): JEI shows Rich Soul Soil in every Nulch recipe; the old netherrack
  recipe is gone; Create mixing gives the same output.

---

## crops #20: Melon and pumpkin stems connect to their own fruit
Status **Planned** · Work patcherMod · id `farming/crops #20`

**BUILT 2026-11-02** (pre-scratch pass, Creative Delight): steps 1-4 and 6 as written (vanilla block properties, not
a new block); `<init>` re-registers the default with all sides false (a boolean's first value is true). Step 5: no
per-neighbour chance (the waits stay; all same-kind neighbours still count toward the cap). Blockstates in
`kubejs/assets/` (above every mod jar). crops.md CR-103. `#10` (growth stages) not built.

### Goal
An attached stem shows a connection to every fruit it grew (the first and the extra ones, `farming/crops #11`), never to
a fruit it did not grow. Extra-fruit growth stays slowed by the fruits around it.

### Now (checked 2026-10-31)
- Vanilla `AttachedStemBlock` has one `facing` property and one bent model per side; it points at one fruit only.
- Extra stem fruit ([crops.md](../crops.md) 2.10; patcherMod
  [StemExtraFruit.java](../../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/crops/StemExtraFruit.java)
  `randomTick` lines 53-92, `reattach` from line 99, and `mixin/AttachedStemExtraFruitMixin`): grows up to 4 fruits on
  free sides; counts every same-kind fruit beside it toward the cap (hand-placed ones too); on harvest re-points
  `facing` at any remaining same-kind neighbour. Saved data holds only the last-fruit time per stem
  (`creativeengineering_stem_fruit.dat`).
- The pacing is the minimum waits (10 / 20 / 40 min, `[stem_fruit]`) plus vanilla's growth speed; there is no "chance
  per neighbouring melon" today. Vanilla's row penalty (same crop in rows) is inside `growthSpeed`.
- Stems: `minecraft:attached_melon_stem`, `attached_pumpkin_stem`, `fruitsdelight:attached_hamimelon_stem` (blockstate
  in `fruitsdelight-0.5.11.jar`). No resource pack in `resourcepacks/` overrides these blockstates (checked).

### Build
1. Mixin on `AttachedStemBlock.createBlockStateDefinition` (`m_7926_`) TAIL: add four booleans `north/east/south/west`
   ("grown here"), default false. Check the hamimelon attached stem calls super (Fruits Delight subclass).
2. `StemExtraFruit.randomTick`: after placing an extra fruit, set the bit for that side on the stem's state. The first
   fruit (vanilla `StemBlock.randomTick`) sets `facing`; treat `facing` as connected.
3. `reattach` / `updateShape`: when a connected neighbour is no longer the fruit, clear its bit; when the `facing` fruit
   goes, re-point only at a side whose bit is set (today: any same-kind neighbour), and clear that bit. No bit left:
   vanilla (back to a plain stem).
4. Blockstate overrides in patcherMod assets for the three attached stems: multipart, the vanilla attached model per
   side `when` `{"OR": [{"facing": "<side>"}, {"<side>": "true"}]}`, rotated like vanilla.
5. Cap and pacing: keep counting every same-kind neighbour toward the cap (the row: "hampered by neighbouring grown
   melons"); optionally lower the roll per neighbour (`[stem_fruit] neighbourPenalty`, e.g. x0.75 per fruit) if the
   owner wants a chance rather than the waits.
6. crops.md 2.10 (Harvesting, Own fruit only) and CR-72 (re-point) text.

### Decisions
- Per-neighbour chance on top of the waits? Recommendation: no; the waits already slow the 2nd to 4th fruit. Ask.
- Properties on the vanilla block (recommended; state ids are rebuilt at load, both sides run patcherMod) or a new
  `creativeengineering:` attached stem block? The new block avoids touching vanilla states but needs swaps in every
  stem path (vanilla, extra fruit, Fruits Delight).

### Gotchas
- patcherMod is on both sides, so the added properties match; a client without patcherMod could not join anyway.
- Existing attached stems load with all bits false: they keep pointing at their `facing` fruit, extra fruits already
  grown show unconnected until harvested once.
- Do this in one build with `farming/crops #10` (growing fruit blocks change the same "is this my fruit" checks).

### Tests
- **CR-new1** (proposed, numbered when queued): grow a stem to 3 melons (`[stem_fruit]` waits at 1 / 2 / 3 min): the
  stem shows three connections; place a melon by hand on the 4th side: no connection to it.
- **CR-new2**: break the `facing` melon: the stem keeps the other two connections and points at one of them, never at the
  hand-placed melon (extends CR-72).

---

## crops #22: More seed bags
Status **Planned** · Work KubeJS / data · id `farming/crops #22`

### Goal
Seed bags (storage blocks: 9 seeds <-> 1 bag) for the crops that have none, from the Creative Delight change list
([creative_delight.md](../creative_delight.md) 31, "more seed bags").

### Now (checked 2026-10-31)
- Existing seed and bean bags: Rustic's `cotton_seeds_bag`, `bell_pepper_seeds_bag`, `pale_bell_pepper_seeds_bag`,
  `dark_bell_pepper_seeds_bag`, `coffee_beans_bag`, `roasted_coffee_beans_bag` (CD `rustic` member,
  [RDBlocks.java:39-44](../../development/creativeDelight/members/rustic/src/main/java/com/SaintAlfonzo/creativedelight/rustic/registry/RDBlocks.java),
  `bag()` = a plain block copying white wool, line 99; recipes and art come from the upstream donor jar at runtime);
  also `farmersdelight:rice_bag`, `corn_delight:corn_kernel_bag`, Farmer's Respite `coffee_beans_sack`.
- Bags are packed into loot by `L.pack` (CD `rusticdelight/loot.js` lines 16-20) and composted
  (`kubejs_startup/rusticdelight/compost_values.js` lines 35, 39).
- Seeds without a bag (item_dict): wheat, beetroot, melon, pumpkin (vanilla); cabbage, tomato (FD); tea (FR); hemp
  (`immersiveengineering:seed`); flax; tobacco; corn seeds; Cantaloupe and Mini Cantaloupe seeds; lemon seeds; Vinery
  red and white grape seeds; Veggies broccoli, cauliflower, turnip, zucchini; Vintage cucumber, ghost pepper, oat;
  pips (sweet berry, glow berry, salmonberry, strawberry, raspberry); adzuki beans.

### Build
1. Owner picks the list (decision). Ids `creativedelight:<seed>_seeds_bag` (CD core, not a member: these are not one
   mod's items), registered beside `treatedstick/TreatedSticks.java`'s registry, same block properties as Rustic's
   `bag()`.
2. Art: one bag texture recoloured per seed (client, CD assets), blockstate, item model, lang names "<Seed> Bag".
3. Recipes through `global.NCH` in the CD tree of the seed's mod (`kubejs/data/creativedelight/kubejs/<mod>/recipes.js`):
   `R.shaped(bag, ['SSS', 'SSS', 'SSS'], { S: seed }, e, '<seed>_seeds_bag')` and `R.shapeless(Item.of(seed, 9), [bag], e,
   ...)`; loot table drops itself.
4. Dict keys in item_dict.js; compost value (same as Rustic's bags, 1.0); `L.pack` lines where a loot table gives many
   seeds; tag `#forge:storage_blocks/<seed>` through `global.tags` if the pack uses such tags for the existing bags
   (check).
5. Changelog bullet.

### Decisions
- Which seeds. Recommendation: the vanilla four, FD cabbage and tomato, and the CD-port seeds (Veggies, Vintage); skip
  pips and single-use seeds (lemon, cantaloupe) unless asked.
- Namespace: `creativedelight:` (recommended) vs the seed's own mod namespace (would need one member per mod).

### Gotchas
- Quark's seed pouch takes `#forge:seeds` (crops #13); the bag must not be in that tag.
- Stack size and placement sound follow the wool copy; check `tools/stack_sizes.py` after.

### Tests
- **CR-new1** (proposed, numbered when queued): craft 9 wheat seeds -> bag -> 9 seeds; place and break the bag -> the bag
  drops; JEI lists both recipes.

---

## crops #25: Mini melon crop
Status **Planned** · Work KubeJS / data / resource pack · id `farming/crops #25`

### Goal
A mini melon crop that works like the Mini Cantaloupe: its own seeds, a small plant, a small fruit made of and cut into
melon slices, own textures.

### Now (checked 2026-10-31)
- The model: the Mini Cantaloupe ([item_merges.md](../item_merges.md) 2.10): Delightful's cantaloupe, the small fruit
  `delightful:cantaloupe` (`i.cantaloupe`, item_dict 11354), seeds `delightful:cantaloupe_seeds` (`i.cantaloupe_seeds`,
  2006), plant `delightful:cantaloupe_plant`. The plant is CD `delightful` member code
  ([CantaloupePlantBlock.java](../../development/creativeDelight/members/delightful/src/main/java/com/SaintAlfonzo/creativedelight/delightful/block/CantaloupePlantBlock.java)):
  `BushBlock` + `BonemealableBlock`, age 0-3, on `#forge:sand` with water or frosted ice beside it, light 8+, ripe
  right-click picks one fruit and resets to 0, break drops seeds (+ fruit when ripe). Registered in
  `DelightfulBackportRegistry.java` lines 46-61.
- Recipes: 4 cantaloupe slices (2x2) -> Mini Cantaloupe (CD `delightful/recipes.js` line 21, `mini_cantaloupe`); seeds
  1:1 both ways with Cantaloupe Seeds. Crop seasons type the plant warm dry (`crop_seasons.json`, with
  `delightful:cantaloupe_plant`).
- Melon dict keys: `i.melon` is the **slice** (`minecraft:melon_slice`, item_dict 1741), `i.melon_seeds` (2535).

### Build
1. CD core (namespace `creativedelight`): `mini_melon` (fruit, block item like the Mini Cantaloupe), `mini_melon_seeds`,
   `mini_melon_plant`. Make the plant a copy of `CantaloupePlantBlock` taking the fruit and seed suppliers (do not reach
   into the member's class from core).
2. Recipes in CD `minecraft/recipes.js` (the melon is vanilla's) through `global.NCH`: `R.shaped(i.mini_melon, ['mm',
   'mm'], { m: i.melon }, e, 'mini_melon')`; knife: `R.fdCutting(i.mini_melon, i.any_knife, Item.of(i.melon, 4), e,
   'mini_melon_cutting')`; seeds `R.shapeless(i.mini_melon_seeds, [i.melon_seeds], ...)` and back.
3. Data: loot tables (plant, fruit), `#forge:seeds` (pouch), crop seasons `warm_wet` (as the melon stem), WTHIT
   [crop_growth.json](../../development/creativeDelight/src/main/resources/assets/creativedelight/crop_growth.json)
   entry, compost values, food values (match the Mini Cantaloupe), dict keys.
4. Art (resource pack, client): plant stages 0-3, fruit block and item, seeds; lang.
5. Changelog bullet; crops.md 2.1 row.

### Decisions
- Slice count: 4, matching the Mini Cantaloupe (the row's decision; recommended).
- Soil: sand by water like the Mini Cantaloupe, or ordinary soil? Recommendation: same as the Mini Cantaloupe, settled
  together with `farming/crops #21`.
- Source of the seeds: from melon seeds (1:1, recommended) or a wild patch (that is `farming/wild-crops`).

### Gotchas
- `i.melon` is the slice, not the block; name the new keys `mini_melon*` to avoid confusion.
- New block ids need a restart and the CD jar rebuilt (`:creativeDelight:`), not `/reload`.

### Tests
- **CR-new1** (proposed, numbered when queued): plant mini melon seeds on sand by water, grow to age 3, right-click: one
  mini melon, plant back to 0; 4 melon slices craft one, a knife cuts it into 4.
- **CR-new2**: the seeds go into a seed pouch; WTHIT shows the growth line; the tooltip shows the warm wet season line.

---

## Built, needs play

Rows at Testing: nothing to build. Open tiers from the TODO Tests cell; the feature writeup is the reference.

| Row | Status | Note |
|---|---|---|
| crops #5 Realistic growth seasons | Testing (S C P T) | Built 2026-10-30, [crops.md](../crops.md) 2.12.1; patcherMod installed 2026-10-31, not yet booted; SQ-CS-01 to -09, CQ-CS-07 (2026-10-31 bulk run), CR-89 to CR-99, CR-76. Open owner points in the TODO row (Floral Enchantment bushes, temperate grapes, greenhouse column rule, bone meal) |
| crops #6 Apples only from apple trees | Testing (S+ P) | Shipped 1.1.01 (`loot/08_leaf_apples.js`, rubberwood added), [crops.md](../crops.md) V17; scratch SQ-CD-02 PASSED 2026-10-22; CR-57 in game |
| crops #8 Watering can | Testing (S C P) | Built 2026-10-30, moved into the CD `hearth` member with six tiers 2026-10-31, [hearth_and_harvest.md](../hearth_and_harvest.md) 4.0; patcherMod and CD built, not installed (install together); SQ-WC-01 to -05, CQ-WC-04, SQ-WCT-01 to -05; HNH-25 to HNH-33, HNH-38 to HNH-44 in game |
| crops #9 Bell pepper colours | Testing (S+ P T) | Shipped 1.1.01, now CD `rusticdelight/crop_drops.js`, [crops.md](../crops.md) 3; scratch SQ-CD-01 and SQ-SY-06 PASSED; CR-69, CR-70 in game |
| crops #11 Extra stem fruit | Testing (S+ P T) | Shipped 1.1.01, [crops.md](../crops.md) 2.10; SQ-SF 17 of 17 PASSED 2026-10-22; CR-71 to CR-75 in game |
| crops #12 Extra stem fruit ignores seasons | Testing (S P) | Fixed 2026-10-30 by crop seasons (attached stems typed), [crops.md](../crops.md) 4 F-SS1; SQ-CS (bulk run), CR-76 |
| crops #13 All seeds in seed pouches | Testing (S P) | Built 2026-10-29 (`tags/misctags.js:58-59`: tobacco, hamimelon, lemon seeds into `#forge:seeds`); SQ-SPC-01, -02, CQ-SPC-01 (bulk run). No writeup section yet; fold a line into crops.md 3 when tested |
| crops #16 Village farm crops audit | Testing (S P T) | Shipped 1.1.03, [crops.md](../crops.md) 2.11; SQ-VF-01 to -05 not run. Follow-ups: `farming/crops #19` and the planned ruins / pillager system in 2.11 |
| crops #23 Better soils raise crop yield | Testing (S+ P T) | Built 2026-10-28, [crop_yields.md](../crop_yields.md); SQ-SY-01 to -07 PASSED 2026-10-28; all 27 switches still off; nether follow-up `farming/crops #24` |
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
