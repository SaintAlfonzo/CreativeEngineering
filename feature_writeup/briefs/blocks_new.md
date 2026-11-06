# blocks / new: implementation brief

Written 2026-10-24 from [todo/TODO.md](../todo/TODO.md) `# blocks`, Sub `new` (rows 11 and 12, from the scratchpad of
2026-10-24); rows 1 to 6 and 8 to 10 added 2026-10-31 (row 7, mushroom crates, is Complete: Fungi Delight). Layout per
[README.md](README.md). There is no feature writeup for new blocks yet: tests use a new prefix `BL-` (BL-01, then
placeholders `BL-new1` to `BL-new9`, numbered when queued) until one exists; rare ice tests go in
[structure_tuning.md](../structure_tuning.md) (`ST-`). Related:
[recipes_materials.md](recipes_materials.md) (bale recipes), [loot.md](../loot.md), [crops.md](../crops.md) (soils).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `new #12` rare ice | data + config | Smallest; a biome-modifier override and the existing property |
| 2 | `new #5` lit fel pumpkin | KubeJS | One `cardinal` block in decor_blocks.js, one texture |
| 3 | `new #11` flower bale | KubeJS + texture | One block, recipe, compost entry (Creative Delight tree) |
| 4 | `new #9` more seashells | KubeJS + textures | Twigs is ARR: own blocks and own drawings, not recolours of its pngs |
| 5 | `new #10` wet sponge | patcherMod | Two small mixins (vanilla farmland, Farmer's Delight rich farmland) |
| 6 | `new #6` wall mushrooms | mods/ or KubeJS + patcherMod | Search first; no wall mushroom exists in the pack's 415 jars |
| 7 | `new #1` 3-long curtains | patcherMod | Subclass of Another Furniture's curtain (MIT) |
| 8 | `new #2` collapsible ladders | patcherMod | Own blocks; climbable only when extended |
| 9 | `new #8` scarecrow | patcherMod (entity) | From the dummy mod's public-domain code, own model; Alex's Mobs crow tags |
| 10 | `new #3` chiseled bookshelf | patcherMod (port skill) | Block + block entity; framed / copycat later |
| 11 | `new #4` sculk sensor | patcherMod (port skill) | Calibrated sensor only; 1.19.2 already has the plain sensor |

Steps 5, 7 to 11 are patcherMod and can share one build. Every new player-visible block needs a changelog bullet, a
`mod_sides.md` row only if a new jar is added (#6), and its WTHIT name check.

---

## new #1: 3-long curtain blocks
Status **Planned** / Work patcherMod / TODO `blocks/new #1`

### Goal
Curtains that hang three blocks tall (top with the rod, middle, bottom), opening and closing as one.

### Now
- The pack's only curtains are Another Furniture's: `another_furniture-forge-1.19.2-2.1.4.jar` (licence MIT), 16
  `another_furniture:<colour>_curtain`, class `com.starfish_studios.another_furniture.block.CurtainBlock` (properties
  `facing`, `type` single / left / middle / right, `open`, `powered`, `facing_vertical`, `waterlogged`). Placement
  (`setPlacedBy`) adds a second block below, so one curtain item is two blocks: the top has `facing_vertical=down` (it
  is the one that drops the item, loot table condition), the lower one `up`. Models `curtain/<colour>_{closed,left,right}_{top,bottom}`
  and `<colour>_middle`; `toggleVertical` / `toggleHorizontal` open the pair and side neighbours. Recipe: 2 sticks +
  4 wool -> 3 curtains; tags `another_furniture:curtains` (block and item).
- `kubejs/data/creativeengineering/kubejs/engineering/fabric.js` line 11 lets hemp fabric stand in for white wool in the white curtain.
  Line 17 targets `handcrafted:white_curtain`, which does not exist (Handcrafted 2.0.6 has no curtains): dead line.
- Not in item_dict: no `i.*_curtain` keys for Another Furniture (only `i.strip_curtain`, IE's strip curtain, a
  different block).
- The Another Furniture jar is not in `development/creativeCommon/libs/` (patcherMod's `AnotherFurniturePlanterRenderMixin`
  targets it by name only).

### Build
1. Copy the jar to `development/creativeCommon/libs/` (compileOnly `fileTree`, build.gradle line 22).
2. patcherMod `block/curtain/LongCurtainBlock extends CurtainBlock`: an own `EnumProperty<Part>` `part` = top /
   middle / bottom (keep `facing_vertical` fixed, it is shared through Another Furniture's `ModBlockStateProperties`).
   Override placement (needs two free blocks below), `setPlacedBy` (place middle and bottom), `updateShape` (survive
   only with the partner above / below), `playerWillDestroy` (break all three, drop once from the top), and
   `toggleVertical` (open or close all three).
3. Register 16 `creativeengineering:<colour>_long_curtain` blocks and block items (`CreativeEngineering.BLOCKS`, as
   `CreativeEngineeringBlocks` does). Blockstates and models in patcherMod resources reuse Another Furniture's
   curtain textures by id (MIT: add its notice to patcherMod's NOTICE); the middle piece uses the bottom textures.
4. KubeJS: `i.<colour>_long_curtain` keys in item_dict (and the 16 Another Furniture curtains as `i.<colour>_curtain`),
   recipe through `global.NCH` (`R.shapeless`: 3 curtains of a colour -> 2 long curtains, same cloth), tags through
   `global.tags` (`another_furniture:curtains` block and item, `minecraft:mineable/axe` like the source), lang names
   "Long <Colour> Curtain".
5. Drop the dead `handcrafted:white_curtain` line in fabric.js.

### Ids and data
New: 16 `creativeengineering:<colour>_long_curtain`. Uses `another_furniture:<colour>_curtain` (verified in the jar).

### Decisions
- Source mod: Another Furniture (the only curtains in the pack, MIT). **Recommend a subclass in patcherMod**, not a
  mixin that turns every curtain into three blocks (the property cannot gain a value, and old two-block curtains would
  break).
- Fixed three tall, or "as tall as the space" (2 to 4)? **Recommend fixed three** (one item = one shape).

### Gotchas
- `updateShape` in the parent checks `facing_vertical` partners; the subclass must replace that check, or the middle
  block pops.
- Pistons: `getPistonPushReaction` is overridden in the parent (destroy); keep it.

### Tests (BL-, proposed)
- BL-new1 Place a long curtain under a ceiling: three blocks; right-click any part opens all three and its side
  neighbours; breaking the bottom drops one item; placing with only one free block below fails.

---

## new #2: Collapsible ladders
Status **Planned** / Work patcherMod / TODO `blocks/new #2`

### Goal
A ladder that is placed folded at the top and unrolls downward (like a curtain opening); climbable only when
extended. The iron one extends and retracts by redstone.

### Now
- Ladders in the pack: vanilla, Quark's 12 wood ladders plus `quark:iron_ladder` (`i.iron_ladder`, item_dict line
  3193), Engineer's Decor `engineersdecor:metal_rung_ladder`, Copycats+ `copycat_ladder`. None folds.
- Nearest mechanic: Supplementaries rope (`[functional.rope] enabled = true`, `config/supplementaries-common.toml` line
  85): right-click a rope with rope to extend it down. Another Furniture's curtain toggle (new #1) is the "extend" model
  the row means.

### Build
1. patcherMod `block/ladder/CollapsibleLadderBlock` (ladder shape, `facing`, `extended`, `head` booleans). The head is
   the placed block; extending places segment blocks downward until a non-replaceable block or the length limit;
   retracting removes them back to the head.
2. Wooden `creativeengineering:collapsible_ladder`: right-click the head toggles. Iron
   `creativeengineering:iron_collapsible_ladder`: `neighborChanged` on the head extends while powered, retracts when
   not (right-click does nothing).
3. Climbing: override Forge `isLadder(state, level, pos, entity)` to return `extended`. Do **not** add the blocks to
   `#minecraft:climbable` (a tag cannot depend on state).
4. Config `[blocks] collapsibleLadderLength = 8` in `CEConfig` (and the shipped toml + `configs_edited` copy after the
   jar has run once).
5. KubeJS: item_dict keys, recipes through `global.NCH` (wooden: 8 ladders + string; iron: 8 iron ladders + redstone
   dust; recommendation), tags through `global.tags` (`minecraft:mineable/axe` / `pickaxe`), lang.

### Ids and data
New: `creativeengineering:collapsible_ladder`, `creativeengineering:iron_collapsible_ladder`. Textures: own (a
folded-ladder head, segments reuse the ladder look).

### Decisions
- Length: fixed per item from config (recommend, 8) or consumes ladders from the inventory like rope. Recommend fixed.
- Wood variants per Quark wood: recommend no (one wooden, one iron).

### Gotchas
- Segments are separate blocks: Create contraptions and pistons would split them; make segments `PushReaction.BLOCK`
  and have a missing head retract the rest.
- The head must stay climbable-false when folded, or the folded stub works as a one-block ladder.

### Tests (BL-, proposed)
- BL-new2 Wooden: place, right-click: 8 segments down, climbable; right-click: folds, not climbable. Iron: lever on,
  extends; lever off, retracts; right-click does nothing. Stops at the first solid block.

---

## new #3: Chiseled bookshelf backport
Status **Planned** / Work patcherMod / TODO `blocks/new #3`

### Goal
The 1.20 chiseled bookshelf on 1.19.2: six book slots, picked by where you click, comparator output for the last
slot used. Decide whether framed and copycat versions come too.

### Now
- No chiseled bookshelf in any jar (only a leftover texture in Every Compat). Bookshelves: vanilla `i.bookshelf`,
  Quark's wood bookshelves (`i.acacia_bookshelf` ... item_dict line 2911 on).
- FramedBlocks 6.8.6 (LGPL v3) has no framed bookshelf of either kind; Copycats+ 2.2.2 is all rights reserved (own
  code only, on Create's MIT copycat base).
- patcherMod precedent for a block with a block entity: `oven/OvenBlock.java`, `oven/OvenRegistry.java`.

### Build
Follow the `port` skill (licence route: Mojang's 1.20 code and textures are not copied; own code, own or user-drawn
textures).
1. patcherMod `block/bookshelf/ChiseledBookshelfBlock` + block entity (6-slot `NonNullList`, one book each), the
   1.20 slot-from-hit-position rule (2 rows x 3 columns on the front face), `facing`, six `slot_N_occupied` booleans,
   comparator = last slot touched + 1.
2. Multipart blockstate (one model per slot, occupied / empty), so no block entity renderer.
3. Book tag `creativeengineering:bookshelf_books` through `global.tags` (book, writable book, written book, enchanted
   book, knowledge book; 1.19.2 has no `minecraft:bookshelf_books`).
4. KubeJS: `i.chiseled_bookshelf`, recipe through `global.NCH` (6 planks + 3 wooden slabs, as 1.20), lang, mineable
   axe tag.
5. Hopper and Create funnel access: expose an `IItemHandler` with the six slots (vanilla 1.20 lets hoppers insert and
   extract; verify against the 1.20 changelog), per the decision below.

### Ids and data
New: `creativeengineering:chiseled_bookshelf` (not the `minecraft` namespace).

### Decisions
- Framed / copycat versions: **recommend not in the first pass**; a framed one later is a FramedBlocks-style block in
  patcherMod (LGPL upstream code may be ported with notice), a copycat one is own code on Create's copycat base.
- Enchanting power: vanilla gives none; recommend none.
- Per-wood variants (Quark woods): recommend oak only first.
- Hopper access: recommend yes (matches vanilla 1.20).

### Gotchas
- Slot pick from the hit vector must use the face-local coordinates per `facing`, or east / west shelves pick mirrored
  slots.
- Books with NBT (written, enchanted) must keep it through the block entity save.

### Tests (BL-, proposed)
- BL-new3 Insert a book in each slot by clicking its spot; take one out; comparator reads the slot; hopper below pulls
  books; breaking drops the shelf and all books.

---

## new #4: Sculk sensor backport
Status **Planned** / Work patcherMod / TODO `blocks/new #4`

### Goal
The 1.20 calibrated sculk sensor (listens only to the frequency set by a redstone signal on its back), if the scope
decision keeps it.

### Now
- 1.19.2 already has `minecraft:sculk_sensor` and the frequency per game event (`SculkSensorBlock`, vibration
  frequency table; exact field name unverified). No mod in the pack adds a calibrated sensor (searched every jar).
- Sculk Transporting 1.1 (MIT) moves items by vibrations; Create Deep Dark crushes sculk sensors (recipe only).

### Build
Follow the `port` skill (own code, no Mojang 1.20 code or textures copied).
1. patcherMod `block/sculk/CalibratedSculkSensorBlock` (+ block entity whose vibration listener config rejects events
   whose frequency differs from the input strength on the back face; 0 = listen to all), `facing`, the sensor's own
   phase / power properties.
2. Own textures (amethyst-topped sensor), blockstates, models.
3. KubeJS: `i.calibrated_sculk_sensor`, recipe through `global.NCH` (sculk sensor + 3 amethyst shards, as 1.20),
   lang, mineable hoe tag.

### Ids and data
New: `creativeengineering:calibrated_sculk_sensor`.

### Decisions
- Scope: calibrated sensor only (**recommend**), or also 1.20's amethyst resonance and the 1.20 sensor timing changes
  (those alter the vanilla sensor every Sculk Transporting build relies on: no).

### Gotchas
- Sculk Transporting listens to vibrations too; check its blocks are not triggered by the calibrated sensor's own
  output.

### Tests (BL-, proposed)
- BL-new4 Calibrated sensor with a redstone strength 3 on its back: reacts to frequency 3 events only; strength 0 reacts
  to all; plain sculk sensors unchanged.

---

## new #5: Lit Botania fel pumpkin
Status **Planned** / Work KubeJS / TODO `blocks/new #5`

### Goal
A lit fel pumpkin, as the jack o'lantern is to the carved pumpkin.

### Now
- `botania:fel_pumpkin` (`i.fel_pumpkin`, item_dict line 5800): orientable model (front `botania:block/fel_pumpkin`,
  sides and top vanilla pumpkin), four `facing` variants, mineable axe; recipe pumpkin + string + bone + rotten flesh
  + gunpowder. Its use: on two iron bars it summons a blaze. Botania licence: "Botania License".
- Pack blocks: [decor_blocks.js](../../kubejs/startup_scripts/items/decor_blocks.js) (`StartupEvents.registry('block')`,
  hand-written models under `kubejs/assets/creativeengineering/`). KubeJS 1902.6.2 has the `cardinal` block type
  (horizontal facing, verified in the jar) and `lightLevel(float)`; no pack script uses `cardinal` yet.
- Alex's Mobs `alexsmobs:crow_fears` (block tag) holds `minecraft:carved_pumpkin` and `minecraft:jack_o_lantern`.

### Build
1. decor_blocks.js: `e.create('creativeengineering:lit_fel_pumpkin', 'cardinal')`, material / sound like the pumpkin,
   hardness 1, `.lightLevel(1.0)` (KubeJS scales 0-1 to 0-15; check in game), mineable axe.
2. Model `orientable` with front `creativeengineering:block/lit_fel_pumpkin` (a lit face, drawn in the user's texture
   flow; base it on the Botania face only if its licence allows: check), sides and top vanilla pumpkin.
3. `i.lit_fel_pumpkin` in item_dict; recipe through `global.NCH`: fel pumpkin + torch -> lit fel pumpkin (shapeless,
   like the jack o'lantern); tags through `global.tags`: `alexsmobs:crow_fears` (recommend), `minecraft:mineable/axe`.

### Decisions
None in the row. Recommend: decorative only, no blaze summoning (Botania checks its own block).

### Gotchas
The `cardinal` type places facing away from the player (check the model's front faces the player as the pumpkin
does; add `y` rotations in the blockstate if not).

### Tests (BL-, proposed)
- BL-new5 Craft a lit fel pumpkin; it faces the player, gives light 15, breaks with an axe; on two iron bars it summons
  nothing.

---

## new #6: Wall mushrooms
Status **Planned** / Work mods/ / TODO `blocks/new #6`

### Goal
Mushrooms that grow off the sides of blocks (logs, cave walls), with slightly altered vanilla mushroom textures.

### Now
- No wall or shelf mushroom in any of the pack's 415 jars (searched block states for wall / shelf mushroom and fungus).
- Mushrooms in worldgen: the pack's mushroom spawn system
  ([mushroom_spawns.json](../../kubejs/generation_scripts/worldgen/mushroom_spawns.json), `rules`, read by patcherMod
  `CEConfig`; `tools/test_mushroom_spawns.sh`), Fungi Delight's inky cap and morel (`backportedFungi`).
- Web leads (2026-10-31, unverified): Mushroom Quest (Forge, a 1.19.2 build "v4.1.0", MCreator, adds bracket and shelf
  fungi, each edible with effects); Extended Mushrooms 1.19.2-3.0.3.0 (behaviour changes, no wall mushrooms).

### Build
1. Check Mushroom Quest (licence, block list, whether its fungi attach to sides, food values). Its edible fungi would
   go through the `integration-report` skill (food overlap, Spice of Life).
2. If no mod fits (recommend own blocks): KubeJS `cardinal` blocks `creativeengineering:wall_brown_mushroom` and
   `wall_red_mushroom` in decor_blocks.js (no collision, a thin box per facing, light like vanilla mushrooms, drop the
   vanilla mushroom); patcherMod survival rule (attached face solid, light 12 or less, as vanilla mushrooms) and
   placement (a mushroom item used on a block side places the wall form).
3. Worldgen: a rule beside the mushroom spawn rules (cave walls, dark forest and swamp logs), so the counts stay in one
   file.
4. Textures: altered vanilla mushroom sprites, drawn in the user's flow.

### Decisions
- Which mod: **check Mushroom Quest first**, else own blocks (recommended fallback; also allows inky cap / morel
  wall forms later).

### Gotchas
A wall form must not count as the mushroom for recipes or compost (it drops the normal item instead).

### Tests (BL-, proposed)
- BL-new6 Place a brown mushroom on a log side in the dark: wall form; light 13+ pops it; breaking drops a brown
  mushroom; new chunks show wall mushrooms on cave walls.

---

## new #8: Scarecrow
Status **Planned** / Work new jar / patcherMod / TODO `blocks/new #8`

### Goal
An armor-stand-like scarecrow for fields: placed from an item, dressable with armour and a pumpkin head, with a default
clothes look.

### Now
- MmmMmmMmmMmm `dummmmmmy-1.19.2-1.7.1.jar` (target dummy; licence "code: Public Domain, assets: ARR",
  `mod_sides.md` line 204): an armour-stand-like entity placed from an item. Its code may be reused, its model and
  textures may not.
- Alex's Mobs crows (`crowSpawnWeight = 10`, `config/alexsmobs.toml` line 314) eat `#minecraft:crops`
  (`alexsmobs:crow_foodblocks`), fear blocks in `alexsmobs:crow_fears` (carved pumpkin, jack o'lantern) and scatter
  from entity types in `alexsmobs:scatters_crows` (`#alexsmobs:neutral_land_animals`).
- Rebirth Paintings has a "Scarecrow" painting; some Kaisyn village farms have scarecrow decorations (pool_config.json).

### Build
1. patcherMod `entity/scarecrow/Scarecrow extends ArmorStand` (or the dummy's approach): placed by
   `creativeengineering:scarecrow` item on farmland or a fence post, holds armour slots and a head slot, no gravity
   when on a fence.
2. Default clothes: an own model layer (straw body, burlap shirt, hat) drawn when the slots are empty; armour renders
   over it. Model built in Blockbench, own textures.
3. Entity type tag `alexsmobs:scatters_crows` through `global.tags` (entity type tags), so crows flee from it.
4. KubeJS: `i.scarecrow`, recipe through `global.NCH` (sticks, hay bale, carved pumpkin; recommendation), lang.

### Ids and data
New: entity `creativeengineering:scarecrow`, item `creativeengineering:scarecrow`.

### Decisions
- Model: armour-stand proportions (recommend; armour fits) or the target dummy's shape (its model is ARR: own redraw
  only).
- Function beyond decor: crows scatter (recommend, one tag line); no mob spawn blocking.

### Gotchas
- A new entity needs a renderer registration on the client side only (patcherMod client class), and a WTHIT name.
- If it is an `ArmorStand` subclass, other mods' armour-stand handling (Quark's armour stand arms, MCA) may apply.

### Tests (BL-, proposed)
- BL-new7 Place a scarecrow on farmland and on a fence; dress it (helmet, pumpkin); empty slots show the clothes; a crow
  near the field flees; breaking drops the item and the armour.

---

## new #9: More seashells
Status **Planned** / Work resource pack / data / TODO `blocks/new #9`

### Goal
More seashell variety: recoloured shells beside Twigs' four.

### Now
- Twigs 3.0.1 (licence all rights reserved): `twigs:{bronzed,opaline,roseate,tangerine}_seashell` (item_dict lines
  619-622), class `SeashellBlock` (waterloggable), item tag `twigs:seashells`, bone meal recipe from the tag, worldgen
  `twigs:patch_seashell` (weights opaline 20, tangerine 20, roseate 15, bronzed 8) placed by
  `data/twigs/forge/biome_modifier/add_land_sea_shells.json`.
- Pack use: Create vibrating sand -> shells (`kubejs/data/creativeengineering/kubejs/engineering/vibrating.js` lines 10-13, 0.225
  each).

### Build
1. KubeJS blocks in decor_blocks.js: `creativeengineering:<name>_seashell` (4 to 6 new colours), `.waterlogged()`,
   `.noCollision()`, a flat `box`, instant break; models shaped like a shell, own textures (drawn fresh; Twigs' pngs
   are ARR, so no recolours of them).
2. Tags through `global.tags`: `twigs:seashells` (so the bone meal recipe and any shell use takes them).
3. Worldgen: a pack `random_patch` feature with the new shells, added by a pack biome modifier for the same beaches
   (or override Twigs' configured feature path with all eight weights).
4. vibrating.js: add the new shells through `R.viVibrating` at a share of the 0.225 chances.
5. item_dict keys, lang names.

### Decisions
Recolours of Twigs' textures are not allowed (ARR); **recommend own drawings**. How many: recommend 4.

### Gotchas
Overriding Twigs' `patch_seashell` changes its weights for every beach; a separate pack feature leaves Twigs' as is.

### Tests (BL-, proposed)
- BL-new8 New beach chunks show the new shells; each places waterlogged; bone meal from any shell; vibrating sand gives
  them.

---

## new #10: Wet sponge
Status **Planned** / Work patcherMod / TODO `blocks/new #10`

### Goal
A wet sponge keeps the farmland above it moist, permanently.

### Now
- Vanilla farmland asks `FarmBlock.isNearWater` (water within 4, or a Forge `FarmlandWaterManager` ticket). The pack
  already mixes into `FarmBlock` (`mixin/FarmBlockUnderCropsMixin.java`, `isUnderCrops` `m_53250_`).
- Farmer's Delight rich farmland (repacked in the Creative Delight `farmers` member) has its own private static
  `RichSoilFarmlandBlock.hasWater(LevelReader, BlockPos)` (verified in the repack jar), which also asks
  `FarmlandWaterManager`.
- `i.wet_sponge` = `minecraft:wet_sponge` (item_dict line 2868).

### Build
1. patcherMod `FarmBlockWetSpongeMixin`: inject at HEAD of `isNearWater` (SRG name to check, likely `m_53258_`),
   return true when the block below is in a new block tag `creativeengineering:moistens_soil_above` (holds
   `minecraft:wet_sponge`).
2. Same for `RichSoilFarmlandBlock.hasWater` (string target, `remap = false`). Check the other soils in
   [crops.md](../crops.md) (Fertile Soil, Rich Soil, Immersive Weathering soils) for their own water checks.
3. Tag through `global.tags`. Optional WTHIT line "Kept moist by wet sponge": skip.

### Decisions
- Range: **recommend the one block directly above** (the row says "the soil above it").
- Does it dry: **recommend never** (vanilla wet sponges only dry in the Nether, where it is placed dry anyway).

### Gotchas
Do not use `FarmlandWaterManager` tickets: they are not saved and would have to be rebuilt on every chunk load.

### Tests (BL-, proposed)
- BL-new9 Farmland on a wet sponge, no water within 4: stays moist through a night of random ticks; on a dry sponge it
  dries; Rich Farmland on a wet sponge stays moist.

---

## new #11: Flower bale
Status **Planned** / Work KubeJS / resource pack / TODO `blocks/new #11`

### Goal
A flower bale block beside the straw, rice and hay bales: a decorative (or compostable) block made of flowers.

### Now
- Existing bales: `i.straw_bale` (`farmersdelight:straw_bale`), `i.rice_bale`, `i.hay_bale` (`minecraft:hay_block`), `i.oat_bale`
  (`vintagedelight:oat_bale`) in item_dict. There is no grass bale yet; `merges/materials #2` (Merge rope) makes the
  flax bale a grass bale (misc_mods.js lines 25-27 swap `i.flax_block` for rice and straw bales in the fodder recipe).
  Pack-own blocks are registered in [decor_blocks.js](../../kubejs/startup_scripts/items/decor_blocks.js)
  (`StartupEvents.registry('block')`, hand-written blockstate and models under `kubejs/assets/creativeengineering/`,
  texture `creativeengineering:block/<name>`).
- Compost chances live in the Creative Delight tree since 2026-10-25: rice bale 0.85 at
  `development/creativeDelight/src/main/resources/kubejs_startup/farmersdelight/compost.js` line 59; there is no
  `creativeengineering/compost_values.js` yet (only `compost_rules.js` in that folder).
- Bale family tags: `kubejs/server_scripts/tags/blocktags.js` lines 365-367 (sacks, bags and bales).

### Build
1. Register `creativeengineering:flower_bale` in decor_blocks.js (hay-like material, `mineable/hoe`), model with an
   axis rotation like the hay block, a new texture (a resource pack job; recolour of the hay texture with petals).
2. Recipe through `global.NCH`: 9 flowers in a square -> 1 bale (and the reverse 1 -> 9); the flower input is a tag
   (`#minecraft:small_flowers`; the pack's own flower tag if the wild flowers add modded ones).
3. `global.item` key, lang name, compost value 0.85 in a new
   `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/compost_values.js` (Creative
   Delight rebuild and restart), `blocktags.js` entry beside the other bales.

### Decisions
Recipe shape and whether it is decorative only or also a compost / fuel block. Recommend a 3x3 craft with reverse, compost
0.85, no other use.

### Gotchas
- Mixed flowers in one craft: output is the same block, so a tag recipe is fine; bees and Bushier Flowers tags are unaffected.
- The reverse craft cannot give back mixed flowers: recommend it returns 9 of one tag member only if the user wants a
  reverse at all (or drop the reverse).

### Tests
- BL-01 (proposed): craft, uncraft, place on each axis, compost it, break with a hoe.

---

## new #12: Rare ice: hide it, make it configurable
Status **Planned** / Work KubeJS / config / TODO `blocks/new #12`

### Goal
Rare Ice is harder to stumble on (its visible sources removed) and its generation can be configured.

### Now
- Mod `rare-ice-0.5.1.jar` (licence CC BY-NC 4.0): block `rare-ice:rare_ice` with **no item** (the loot table gives
  `minecraft:ice` with Silk Touch), a block entity holding an item, a chest loot table `chests/rare_ice`. Its block tag
  `minecraft:ice` adds `rare-ice:rare_ice`.
- **Generation (read from the classes 2026-10-31):** the configured and placed features `rare-ice:rare_ice` are
  registered in code, not as data. Placement = `CountPlacement(probabilityOfRareIce)` + a uniform height range from 32
  above the bottom to 32 below the top; no in-square spread and no biome filter. The feature is an ore-like vein
  (`RareIceFeature.generateVeinPart`, `RareIceConfig.size`). `data/rare-ice/forge/biome_modifier/add_rare_ice.json`
  adds it to `#minecraft:is_overworld` at step `underground_ores`.
- `config/rare-ice.properties`: `probabilityOfRareIce=3` (= placement attempts per chunk), `allowInsertingItemsToIce=true`.
  No `configs_edited/` copy exists (the file is not hand-edited yet; the git diff is only its timestamp line).
- Visible sources: the block sits in `#minecraft:ice` (`b.t_ice`, block_dict line 146), so tag-based listings show it;
  the JEI world tabs drew it as a barrier until `jei/jei-coverage #19` (1.1.04) drew block models; WTHIT shows it;
  [loot.md](../loot.md) line 172 lists its chest table (untyped). The tab generators in `tools/jei_world/` do not name it.

### Build
1. Decide "hidden" (Decisions).
2. Rarity and place: override the biome modifier at the same path,
   `kubejs/data/rare-ice/forge/biome_modifier/add_rare_ice.json`, pointing at a pack placed feature
   `kubejs/data/creativeengineering/worldgen/placed_feature/rare_ice.json` that reuses the configured feature
   `rare-ice:rare_ice` with `count`, `in_square`, `height_range` and a `biomes` list of cold biomes (or a
   `#creativeengineering:rare_ice_biomes` tag through `global.tags`). Then `probabilityOfRareIce` no longer applies
   (the mod's placed feature is not added); say so in the writeup, or keep the mod's feature and only narrow the
   biomes.
3. Config: if `probabilityOfRareIce` is changed, copy the file to `configs_edited/root_config/rare-ice.properties`.
4. Optional: a `[worldgen]` key in `CEConfig` only if rarity should sit with the pack's other worldgen keys (not with
   step 2's data file).

### Decisions
- What "hidden" means: out of tag listings, rarer, deeper, or only in cold biomes. Recommend: cold biomes only plus
  spread in the chunk (`in_square`), through the step 2 override; the block stays in `#minecraft:ice`.
- Config location: the mod's own property (works today, but only scales count) or the pack's placed feature file.
  Recommend the placed feature file (it can also set biomes and heights).

### Gotchas
- Removing the block from `#minecraft:ice` would stop it counting as ice (melting, frost walker, mineable rules): do not.
- Without `in_square` every attempt lands on the chunk's corner column (the mod's own placement does this today).
- Worldgen changes show only in new chunks.

### Tests (ST-)
- ST-new1 (proposed, numbered when queued): with the override, a 10 x 10 chunk dig in a cold biome finds rare ice
  spread across chunks, a temperate biome finds none; `probabilityOfRareIce = 0` without the override gives none.
