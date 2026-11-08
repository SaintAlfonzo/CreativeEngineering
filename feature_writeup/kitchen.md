# Kitchen: Cooking for Blockheads parts, wood racks, cutting board, fridge, corner storage, bamboo bowl, mortar and pestle

The kitchen blocks and hand tools around cooking: Cooking for Blockheads' (CfB 13.3.4) oven, sink, counter, corner
and fridge made to work and fit in this pack, Farmer's Delight's cutting board on a counter, a bamboo bowl, and the
pack's own mortar and pestle as a hand grinder for food. Test prefix `KT-`. TODO family `food`, sub `kitchen`
([TODO.md](todo/TODO.md)); the plan for each row is in [briefs/cooking_kitchen.md](briefs/cooking_kitchen.md).

The FD-style stations' leftover slots (pot, kettle, oven; rows #1, #3, #4, #13, #14) have their own writeup:
[remainder_buffer.md](remainder_buffer.md). The baking oven is [oven.md](oven.md).

## 0. At a glance

| Part | Status | TODO |
|---|---|---|
| CfB oven: any furnace fuel plus cooking oil; every fuel container comes back | BUILT 2026-10-21 (patcherMod 1.1.01 + config), scratch server PASSED, not in game | `cooking/kitchen #8` |
| CfB sink: infinite water | BUILT 2026-10-21 (config), scratch server PASSED | `#8` |
| FD cutting board on a CfB counter or corner, drawn on the worktop, config offset | BUILT 2026-10-21 (patcherMod), placement and cutting PASSED on a scratch server; the drawing not seen | `#8` |
| Display Delight plates and plated foods, Brewin' coaster on a CfB counter or corner, drawn on the worktop, own offset | BUILT 2026-10-24 (patcherMod 1.1.02 + KubeJS tags), deployed with 1.1.03 (2026-10-26); `/kttest` 30 of 34 on 2026-10-25 / 26 / 27 (KT-15d, KT-17 fail on Display Delight 1.7.0's leg; probe rewritten 2026-10-27); rewritten probe scratch PASSED 2026-11-01 (SQ-KT-25, 34 of 34) and r1102b (SQ-KT-02, KT-17 x4) (deep scan 2026-11-02) | `#20` |
| CfB 2-tall fridge filled to a full block with a white box on top (the 2026-10-21 stretch reverted) | BUILT 2026-10-25 (resource override); top box rendered missing in a client, texture fixed 2026-10-26; since 2026-10-31 drawn by blockheadsAdditions code instead of the override (2.4), not seen in game | `#6`, `#19` |
| CfB spice rack and tool rack in 20 woods (2.7) | BUILT 2026-10-31 (Creative Delight member blockheadsAdditions + KubeJS); scratch server PASSED 2026-11-01 (SQ-KT-22 to -24: 40 recipes, rack holds and drops items); not in game | `#22` |
| CfB code moved out of patcherMod into the Creative Delight member blockheadsAdditions (section 9) | BUILT 2026-10-31; scratch server PASSED 2026-11-01 (SQ-KT-22 boot, SQ-KT-25 `/kttest` 34 of 34, `/kctest` 9 of 9); not in game | `#22` |
| Display Delight blocks, toppers and the coaster break with drops when the block below goes | BUILT 2026-10-25 (patcherMod 1.1.03, deployed 2026-10-26); since Display Delight 1.7.0 only the toppers and the coaster rely on it (2026-10-27); rerun scratch PASSED 2026-11-01 (SQ-KT-25) and r1102b (SQ-KT-02) | `#20` |
| CfB kitchen corner storage (27 slots, chest GUI, hoppers, cooking table) | BUILT 2026-10-21 (patcherMod), scratch server PASSED | `#15` |
| Bamboo bowl | BUILT 2026-10-21 (KubeJS), scratch server PASSED | `#10` |
| Mortar and pestle food grinding (own recipes through `foodGrinding`, other mods' through a catalogue) | BUILT 2026-10-21 (KubeJS + generator), scratch server PASSED | `#10` |
| Returned bowls that wouldn't stack | cause found elsewhere: Sticky It!'s tooltip handler, fixed by Creative Labels ([sticky_notes.md](sticky_notes.md) 3.13, SN-48) | `#5` |
| Coaster contents: only items with a coaster model (tag 304, 208 pack models from `gen_coaster_models.py`), 2.3 | BUILT 2026-10-29 (KubeJS assets + tag), scratch server PASSED r1031 (2026-10-31, SQ-KT-21: tag count logged, coaster accepts and refuses as listed); KT-19, KT-20 in game pending | `#21` |
| Stepstool | BUILT 2026-10-29 as a worn stool that lifts the player one block ([stepstool.md](stepstool.md)); scratch server PASSED r1031 (SQ-STL-01, -02); client and play open | `#17` |
| CfB oven as a stove: Farmer's Delight heat source while it burns fuel; cook surface raised to the block top (2.10) | BUILT 2026-11-03 (blockheadsAdditions; Creative Delight jar built and installed 2026-11-04 with the wood cutting boards); scratch SQ-CFS queued, CQ-CFS-01 in game | `#28` |
| Wood cutting boards: oak, birch, jungle, acacia, dark oak, mangrove, crimson, warped; FD's board is the spruce one (2.11) | BUILT 2026-11-04 (repackedFarmers, KubeJS; Creative Delight jar installed); scratch SQ-CBW-01 to -03 queued, CQ-CBW-01 in game | `#38` |
| CfB additions as an optional Creative Delight compartment (M8): no `blockheads_additions` mod, racks and baskets `creativedelight:*` with old-id swap, CD loads without CfB and Balm (section 9) | BUILT 2026-11-04 (Creative Delight 1.1.0, not installed); scratch SQ-CDFO-01 to -03 queued, CQ-CDFO-01 in game | plan_cb_publish.md M8 |
| Scythe | Planned | `#16` |

- **Pending:** the 1.1.01 parts (oven fuel, sink, board, corner) deployed 2026-10-22, the counter toppers with 1.1.03,
  as did the break hook (2026-10-26). No KT- test has been run in game (checked 2026-10-30). KT-01 to KT-21.
- **Files to open:** Creative Delight member [blockheads/](../development/creativeDelight/members/blockheads/) (section 9):
  `mixin/CfbOvenFuelMixin`, `mixin/CfbOvenSmokingMixin`, `mixin/BlockStateCounterTopperMixin`,
  `mixin/CuttingBoardRendererOffsetMixin`, `mixin/CoasterRendererOffsetMixin`, `kitchen/CounterToppers`,
  `mixin/CfbCornerStorageMixin`, `mixin/CfbCornerUseMixin`, `kitchen/CornerStorage`, `client/FridgeTopFill`, `rack/`;
  [tools/gen_cfb_racks.py](../tools/gen_cfb_racks.py);
  [cookingforblockheads/kitchen.js](../kubejs/data/creativedelight/kubejs/cookingforblockheads/kitchen.js);
  [cooking/mortar.js](../kubejs/data/creativedelight/kubejs/creativeengineering/mortar.js) and NCH `mortar` / `foodGrinding`;
  [tools/gen_mortar_catalog.py](../tools/gen_mortar_catalog.py).
- **Knobs:** `config/creativedelight-common.toml` `[cookingforblockheads] cuttingBoardOffsetX / Y / Z` and `plateOffsetX / Y / Z`
  (pixels; Y default -1; in `blockheads_additions-common.toml [kitchen]` until M8 2026-11-04 and
  `creativeengineering-common.toml` until 2026-10-31, same keys);
  `config/cookingforblockheads-common.toml` `ovenRequiresCookingOil = false`, `sinkRequiresWater = false` (both also
  in `configs_edited/root_config/`).

## 1. Overview

CfB's kitchen arrived half-working in this pack: its oven wanted Pam's cooking oil and found none, its sink only
gave water that was piped in, FD's cutting board couldn't sit on a counter, its corner piece held nothing, and its
2-tall fridge left gaps and a sunken top. Each is fixed at the smallest point: two config values, one tag, and
patcherMod mixins where the mods' own code had to bend. The hand tools follow the same idea: the mortar and pestle
that Old Guns already ships grinds food in the crafting grid, mirroring the machines, so a player without Create can
make flour and cornmeal.

## 2. Reference

### 2.1 CfB oven fuel

- CfB's `OvenBlockEntity.getBurnTime` answers 800 for `#balm:cooking_oil` while `ovenRequiresCookingOil` is on, else
  Balm's furnace burn time. Balm 4.6.0's tag lists `{"id":"forge:cookingoil","required":false}` with no `#`: an
  optional *item*, so `#forge:cookingoil` never reaches it and the tag was empty. The oven took no fuel at all.
- Now: `ovenRequiresCookingOil = false` (any furnace fuel), Rustic Delight's Cooking Oil in `#balm:cooking_oil`
  (`cookingtags.js`, `i.t_balm_cooking_oil`), and `CfbOvenFuelMixin` (RETURN on `getBurnTime`) gives the tag CfB's
  800 when the furnace list has nothing. Burn time is that x `ovenFuelTimeMultiplier` (0.33): oil 264 ticks, coal
  528.
- Fuel containers: CfB kept the fuel's remainder only when the stack ran out (`shrink(1)`; the remainder replaced
  the empty slot), so 64 oils gave back 1 bottle. The mixin redirects that one `shrink` in `serverTick`: while more
  fuel remains, each burned item's container goes to the output slots (merging into a matching stack first), or
  drops above the oven when they are full. The last one still goes back into the fuel slot.
- Tooltip on the oven (`tooltips.js`): "Burns any furnace fuel, and Cooking Oil" / "Empty fuel containers (bottles,
  buckets) go to the output slots".

### 2.2 CfB sink

`sinkRequiresWater = false`: the sink gives infinite water (a bucket on it comes back full). Tooltip "Gives infinite
water".

### 2.3 Cutting board, plates and coasters on a counter

- CfB's `CounterBlock.use` returns FAIL for any block item on its top face (and for CfB's cutting board item), which
  lets the item place itself; CfB means boards to sit there. The placed block's `canSurvive` then refused: FD 1.2.4's
  cutting board wants a rigid or center support face below, Display Delight 1.2.0's plates and plated foods
  (`AbstractItemBlock`, `EmptyPlateBlock`, `EmptySmallPlateBlock`, `SmallPlatedFoodBlock`) a center face, the Brewin'
  port's coaster either. CfB's counter and corner are 15 px tall (`Block.box(0.5, 0, 0, 15.5, 15, 16)`, model worktop
  at y 15, backsplash to y 17 at the back), so they have no top face at all.
- "Counter toppers" (`compat/CounterToppers`, TODO `cooking/kitchen #8`, `#20`): a block in
  `#creativeengineering:counter_toppers` (`b.t_counter_toppers` = FD `cutting_board`, Display Delight `food_plate`,
  `small_food_plate`, every `*plated_*` block, `brewinandchewin:coaster`) above a block in
  `#creativeengineering:counter_supports` (`b.t_counter_supports` = `b.cfb_counter`, `b.cfb_corner`; `blocktags.js`;
  was `cutting_board_supports` until 2026-10-24) stays.
- `BlockStateCounterTopperMixin` on `BlockStateBase`: RETURN on `canSurvive` (m_60710_) turns a `false` into `true`
  for a topper on a support. `BlockItem.canPlace` and the board's and coaster's `updateShape` all ask through the
  state, so one hook covers every topper (it replaced the FD-only `CuttingBoardSupportMixin`).
- Breaking the block below: the coaster and the board break with their loot in their own `updateShape` (the coaster
  drops its drinks in `onRemove`). Display Delight blocks stay and grow a wooden leg (`support` state, the 1.7.0 port,
  [display_delight.md](display_delight.md); changelog 1.1.03 "they stand on a wooden leg"). The same mixin's RETURN on
  `updateShape` (m_60728_) answers air for a change from below when a topper or `displaydelight` block can no longer
  survive (`CounterToppers.popsUnsupported`). It was written for Display Delight 1.2.0; the 1.7.0 blocks' `canSurvive`
  is always true, so it never fires for them now (scratch run 2026-10-27, KT-17 rewritten to the leg).
- Drawn on the worktop: `CounterToppers.offset` gives the shift for a topper above a support. The same mixin's HEAD
  on `getOffset` (m_60824_) returns it for a topper, so the chunk mesher (vanilla and Rubidium both call it) moves the
  model; plated foods draw the food inside the block model, so plate and food move together. Whether a state is a
  topper is cached per state for one tag generation (`CounterToppers.init` bumps it on `TagsUpdatedEvent`), so
  `getOffset`, run for every block the mesher draws, does no tag lookup. Block entity renderers:
  `CuttingBoardRendererOffsetMixin` (client) pushes and translates FD's `CuttingBoardRenderer.render`, so the item on
  the board moves too; `CoasterRendererOffsetMixin` (client) does the same for the port's `CoasterBlockEntityRenderer`,
  which draws the coaster itself and its drinks (no block model). The port jar is a compileOnly lib in
  `development/creativeCommon/libs/` (copied from `mods/`). The hitbox and collision stay where they were (1 px above).
- Offset config `[kitchen]` in pixels, -8 to 8: `cuttingBoardOffsetX / Y / Z` for the board, `plateOffsetX / Y / Z`
  for the plates, plated foods and the coaster (Y -1, X and Z 0 for both). X and Z in the frame of a counter facing
  north (+X east, +Z toward its back), turned with the counter's `facing` (south (-x, -z), east (-z, x), west
  (z, -x)). Visual only: F3+A redraws after a restart.
- What goes on a coaster (2026-10-29): with `[coaster] taggedItemsOnly` (on) the coaster takes only
  `#brewinandchewin:placeable_on_coaster`. An item "has a block model" when a coaster JSON
  (`assets/<ns>/brewinandchewin/coaster/*.json`) names it and its model loads; the renderer draws anything else as a
  flat item sprite, and a JSON whose model is missing draws nothing (`checkModel`). The tag is the port's 96 files plus
  every file in `kubejs/assets/creativeengineering/brewinandchewin/coaster/` (`tools/placeable_catalog.py`
  `add_coaster`), so the tag can only hold modelled items. Since 2026-10-27 the pack folder had been empty
  (`gen_coaster_models.py` read only plain-variant blockstates; Display Delight 1.7.0's are multipart) and the
  in-game tag was the port's 96. `gen_coaster_models.py` now writes 208 files from three sources: Display Delight
  1.7.0's default-state model for every food the port registers a block for (155, all links from the jar, not the
  10-24 dump), the edible block items whose block is Vinery's `WineBottleBlock` or Vintage Delight's `MasonJarBlock`
  (34: 26 wines, 8 jars; Vinery's art read from `vinery_upstream/`), and same-sprite borrows (19: Farmer's Respite
  long/strong drinks take the base drink's model, Create Central Kitchen's pumpkin pie slice Delightful's, the
  Enchanted Golden Carrot the golden carrot's with the glint). Each model is scaled to 10 px wide and 16 px tall at
  most. Tag 304. Left out: Fruits Delight jellies (their contents are block-tinted, the coaster draws one colour per
  model so they would show white; needs a tint modifier in the port) and the colour-shifted look-alikes in
  [coaster_model_candidates.md](coaster_model_candidates.md) (a borrowed model would show the wrong colour). Foods
  the dumps have not seen (Creative Delight's newer foods) join after `/dumpall`, then the two generators.

### 2.4 Fridge

- The 2-tall fridge (two fridges stacked) is x 1-15, z 3-15 (door front at z 1) with its upper top at y 13, so decor
  on top floated 3 px above it. A box fills it: x 1-15, y 13-16, z 1-15 (the body and the closed door's front), all six
  faces (the bottom shows over the door's gap when it swings open), textured with the white inside of CfB's own
  `fridge_side` (uv 1-15 on top and bottom, a 14 x 3 strip on the sides) with `tintindex` 0, so a dyed fridge's box
  takes the dye from CfB's block colour like the body.
- Since 2026-10-31: the box is our own model `creativedelight:block/fridge_top_fill` (`blockheads_additions:` until M8; written by
  `tools/gen_cfb_racks.py`), registered as an additional model (so its texture is stitched).
  `client/FridgeTopFill` wraps the fridge's baked model for every `model_type=large_upper` state in
  `ModelEvent.BakingCompleted` and adds the box's quads in the fridge's first render pass. CfB's fridge model is a
  Balm dynamic model that Balm puts in the model map in CfB's own `BakingCompleted` listener; the member loads
  after CfB (mods.toml `ordering="AFTER"`), so it sees Balm's model. No CfB file is copied or overridden.
- Was (2026-10-25 to 2026-10-31): `tools/gen_cfb_fridge_models.py` copied CfB's `fridge_large_upper` into
  `kubejs/assets/cookingforblockheads/models/block/` plus the box. CfB is All Rights Reserved and its author forbids
  publishing modified files, so the copy and the script were removed. The first texture of that version
  (`creativeengineering:block/fridge_top_fill`) was never put on the atlas, as Balm's dynamic models stitch nothing
  of their own (seen 2026-10-26).
- Was (2026-10-21 to 2026-10-25): six overrides stretched the body walls out to x 0-16 / z 16 and the doors to full
  width and height. The textures looked wrong (`#19`), so it was reverted (user).

### 2.5 Corner storage

- CfB's floor corner (`cookingforblockheads:corner`, "Kitchen Corner") had only a kitchen connector.
  `CfbCornerStorageMixin` makes `CornerBlockEntity` a Balm `BalmContainerProvider` holding a
  `CfbCornerStorage.CornerContainer` (27 slots, 54 with CfB's `largeCounters`, like a counter). Through Balm's
  `buildProviders` that gives an item handler (hoppers, pipes), and CfB's `BlockKitchen.onRemove` drops the contents.
  Its `getProviders` gains a `DefaultKitchenItemProvider`, so a connected Cooking Table uses what's inside.
- `CfbCornerUseMixin`: CfB's `use` recolours with dye and answers FAIL otherwise; FAIL now opens a vanilla chest menu
  titled with the block's name, except for a block item on the top face (placement, e.g. the cutting board).
- Saved as an attached capability (`ForgeCaps/creativeengineering:corner_storage`); the container marks the block
  entity changed and closes for a player more than 8 blocks away or when the corner goes. The key kept its
  `creativeengineering` name when the code moved to blockheadsAdditions (2026-10-31), so filled corners keep their
  contents.
- Not covered: CfB's hanging corner (its `newBlockEntity` returns null) and Farmhouse Decorations' corner cabinets
  (no block entity); both would need new blocks.

### 2.6 Bamboo bowl, mortar and pestle

- **Bamboo bowl:** 3 bamboo in the bowl's V -> 1 bowl (Creative Delight `minecraft/recipes.js` since 2026-10-25, was `cooking.js`; `shaped/bowl_from_bamboo`). Planks give 4, so
  bamboo isn't the cheap route.
- **Mortar and pestle** (`creativeengineering:mortar_and_pestle`, `i.mortar_and_pestle`, KubeJS item in
  `items/misc_items.js`, 512 durability, texture copied from Old Guns' (AGPL-3.0)). Crafting table: lever over
  polished granite in Old Guns' bench shape (Creative Delight `creativeengineering/recipes.js`, was `cooking.js`; `shaped/mortar_and_pestle`). NCH
  `mortar(output, input, e, id, inputCount)` writes a shapeless craft with `damageIngredient` (1 durability, the
  mortar comes back), id `mortar/<id>`, up to 8 inputs.
- **Powder Pestle:** Old Guns' own `oldguns:mortar_and_pestle` (`i.powder_pestle`) is renamed in `namechanges.js`
  and keeps only Old Guns' gunpowder crafts; it is not used for food. Old Guns integration is later work.
- **The pack's own grinding** goes through NCH `foodGrinding(outputs, input, e, id, opts)`: crushing wheels and
  Millstone with the same outputs, plus a mortar mirror. The mortar gives the main output's machine average E (sum of
  count x chance) rounded down from one input; under 1, one output from ceil(1 / E) inputs (0.02 slack, so 0.333
  takes 3). Byproducts stay machine-only. `opts.mortar = { count, inputs }` overrides, `false` skips. In use: corn
  and wild corn 2 -> 3 cornmeal (set by `opts.mortar` to the machines' exact 1.5 average, `dishes/ingredients #14`),
  kernels 3 -> 1 (corn crate and kernel bag: `mortar: false`, machine-only), hay bale 1 -> 13 wheat flour (which also
  gave the hay bale a Millstone recipe; [recipes_cooking.md](recipes_cooking.md) 2.1).
- **Other mods' grinding:** `tools/gen_mortar_catalog.py` (generator `mortar_catalog`, dump `/dumpfood`) reads
  `kubejs/exported/food/recipes.json` and keeps Create milling and crushing from other mods whose main output is a
  food ingredient: a forge flour / sugar / salt / grain / crops tag, or its ALLOW list (coffee beans, cocoa powder),
  minus its EXCLUDE list (ice cubes, nether wart) and scrap recycling. It writes
  `kubejs/generation_scripts/food/mortar_catalog.json` (item_dict keys where they exist, every skipped recipe with a
  reason); `cooking/mortar.js` loops it through `R.mortar`. Today: wheat -> 1 wheat flour, sugar cane -> 2 sugar,
  raw rock salt -> salt, coffee berries -> coffee beans, crushed cocoa -> cocoa powder, rice panicle and wild rice
  -> rice.

### 2.7 Wood spice racks and tool racks

- 40 blocks: `cookingforblockheads:<wood>_spice_rack` and `<wood>_tool_rack` for oak, spruce, birch, jungle, acacia,
  dark oak, mangrove, crimson, warped, twisted (Architects Palette), dreamwood, livingwood (Botania), smoked (Create:
  Dreams & Desires), treated wood and vertical treated wood (Immersive Engineering), rubberwood (myrtrees), ancient,
  azalea, bamboo (Quark), driftwood (Upgrade Aquatic). CfB's own Spice Rack and Tool Rack stay.
- Each is CfB's block (`WoodSpiceRackBlock extends SpiceRackBlock`, `WoodToolRackBlock extends ToolRackBlock`): same
  shape, storage, kitchen connection, tooltip and renderer. They use CfB's spice rack and tool rack block entity
  types; `BARacks.allowOnBlockEntityTypes` (common setup) adds them to those types' valid blocks (`f_58915_`). In
  CfB's creative tab. Model: CfB's `spice_rack` / `tool_rack` model as parent with the texture swapped, cutout.
- Art: the owner's, made in `textureswapper/rack/` (`swapped_spicerack/`, `toolrack_final/`), crops of each wood's
  planks with the rack's hooks; `tools/gen_cfb_racks.py` copies it to `textures/block/rack/` and writes the
  blockstates, models, loot tables (drop itself, keep a custom name, as CfB's) and lang. `treated_wood.png` in those
  folders is the same picture as `_horizontal` (IE has no plain treated wood planks block), so it is not used.
- Recipes (`cookingforblockheads/kitchen.js`, ids `creativedelight:shaped/<rack>`): CfB's shapes in the wood's slab
  (spice rack 2 slabs in a row; tool rack 3 slabs over 2 iron nuggets, `#balm:iron_nuggets`); rubberwood has no slab,
  so its racks take planks. CfB's own two recipes now take `#creativedelight:rack_slabs/other` (`#minecraft:wooden_slabs`
  minus the rack woods' slabs) instead of any wooden slab, so no grid matches two recipes.
- Axe-mineable (`global.tags.mineable`).
- Fruit baskets (added 2026-10-31): `<wood>_fruit_basket` in the same 20 woods, `WoodFruitBasketBlock extends FruitBasketBlock`,
  added to the fruit basket block entity type's valid blocks. CfB's model uses the plain planks texture, so the art is the
  plain planks png from `textureswapper/rack/` copied to `textures/block/fruit_basket/<wood>.png` by `gen_cfb_racks.py`.
  Recipe: slab, any wooden pressure plate, slab (rubberwood: planks); CfB's own basket takes `#creativedelight:rack_slabs/other`.

### 2.8 Stone cutting boards (built 2026-11-02; r1102b SQ-FCB-07 partial: block entity, placing, reload PASS, drops and knife cut unproven (batch fault); CQ-FCB-02; TODO `cooking/kitchen #25`)

- `farmersdelight:granite_cutting_board`, `andesite_cutting_board`, `diorite_cutting_board`: Farmer's Delight's
  `CuttingBoardBlock` registered by the repackedFarmers member (`FDBackportRegistry.STONE_CUTTING_BOARDS`, -03), stone
  material and sound, hardness 1.5. Common setup adds them to the cutting board block entity type's valid blocks
  (`allowStoneBoards`, as the CfB wood racks do), so they cut, render and hammer bakeware like the wooden board.
- Model: FD's `cutting_board` model as parent with the texture swapped. Art: `members/farmers/tools/stone_cutting_boards.py`
  paints the polished stone's inside (its 1 px block edge left out) onto FD's board texture, keeping the board's seams,
  rim and handle slot.
- Recipe (CD `farmersdelight/stone_cutting_boards.js`): FD's shape, 2 sticks and 4 of the polished stone. Block tags:
  pickaxe, counter toppers (blockheads_additions), hammer no-rotate.
- 2.7 update 2026-11-02: CfB's own Spice Rack, Tool Rack and Fruit Basket are merged into the oak versions
  ([item_merges.md](item_merges.md) 2.16); their recipes are gone. Scratch PASSED r1102b (SQ-FCB-05: each turns into its oak survivor).

### 2.9 Cookware pickup and punch toggle (built 2026-11-02, reworked 2026-11-03; r1102b PASS covers the 11-02 version only; tests SQ-FCB-08, -08b, CQ-FCB-04, -04b; TODO `cooking/kitchen #27`)

- Blocks: `#creativedelight:cookware_pickup` (Farmer's Delight cooking pot and skillet, Miner's copper pot, Farmer's
  Respite kettle; CD `creativeengineering/kitchen_tags.js`). Code: CD Java `cooking/CookwareHandling`, switch
  `[qol] cookwarePickup` (QoL-7); WTHIT line `compat/wthit/WthitCookwareToggle`.
- Sneak right-click, empty main hand: every slot of the block's inventory (ingredients, stored meal, containers,
  output) goes into the player's inventory, overflow drops at the player (owner 2026-11-03). The hand gets the station
  as a plain item: the loot drop with `BlockEntityTag` removed, so no meal or kettle tank rides along, but the
  skillet keeps its own enchantments and a renamed station its name. The kettle's water / tank is lost. A
  `BreakEvent` is posted first, so claims and spawn protection stop it. Upstream's sneak right-click toggles are no
  longer reached.
- Punch (left-click, empty main hand, sneaking or not): toggles the state instead of breaking, also in creative.
  Kettle: `lid`. Cooking pot and copper pot: `support` handle against the tray state upstream gives (tray on a
  `#farmersdelight:tray_heat_sources` block, else none). The kettle also has a handle, only from placement (the punch
  is its lid). Skillet: no toggle, breaks as normal. A held button re-sends the click every tick (survival) or 6 ticks
  (creative); clicks under 7 ticks apart at the same block count as one punch, so holding toggles once.
- Placement: placed against the bottom face of a block, the pots and the kettle get the handle (upstream
  `getStateForPlacement`), and keep it after.
- WTHIT: "Punch to toggle lid" / "Punch to toggle handle" in the body, hidden with the switch off.
- Kettle handle: placement only (under a block), confirmed by owner 2026-11-03; a punch on the kettle toggles the lid.
- Since 2026-11-07 with QoL-8 on (2.12) the pickup keeps the contents in the item instead of emptying them into the
  inventory; only the leftover slots drop at the block.
- History: 11-02 the handle toggle was sneak left-click; on 11-03 it was briefly placement-only, then the punch toggle
  replaced it (owner).

### 2.11 Wood cutting boards (built 2026-11-04; scratch SQ-CBW-01 to -03, CQ-CBW-01; TODO `cooking/kitchen #38`)

- `farmersdelight:{oak,birch,jungle,acacia,dark_oak,mangrove,crimson,warped}_cutting_board`: FD's `CuttingBoardBlock`
  registered by the repackedFarmers member (`FDBackportRegistry.WOOD_CUTTING_BOARDS`), properties copied from the
  wood's planks (map colour; crimson and warped not flammable) with FD's strength 2.0 and wood sound. Common setup
  (`allowExtraBoards`, was `allowStoneBoards`) adds them and the stone boards to the cutting board block entity type.
- FD's own `farmersdelight:cutting_board` is the spruce board (its art is spruce): renamed "Spruce Cutting Board" in
  the member's lang, id unchanged.
- Art: `members/farmers/tools/wood_cutting_boards.py` swaps the board texture's 5 shades for the wood's plank palette
  sorted by brightness (sampled at 0.2 / 0.42 / 0.6 / 0.8 / 1.0 of the ramp), so seams, rim and handle slot stay.
  Models: FD's `cutting_board` as parent with the texture swapped, as 2.8.
- Recipes (CD `farmersdelight/cutting_boards.js`, the stone script renamed): 2 sticks + 4 of the wood's planks. FD's
  `farmersdelight:cutting_board` recipe is removed and re-added as `shaped/cutting_board` on
  `#creativedelight:cutting_board_planks` = `#minecraft:planks` minus the 8 woods' planks (modded, vertical and
  polished planks stay, so no plank loses its board). Block tags: axe, counter toppers, hammer no-rotate.
- Counter offset: `CounterToppers.isBoard` now matches FD's board class by name instead of the `cutting_board` id, so
  the stone and wood boards take the `cuttingBoardOffset*` shift on a counter (the stone boards took the plate offset
  before).
- Other board checks: the vanilla pumpkin pie placement (`FDBackportEvents`) refuses any `CuttingBoardBlock`, not
  only FD's board. The renderer offset mixin already applies to every cutting board block entity.

### 2.12 Cookware keeps its contents (built 2026-11-07, not tested; scratch SQ-FCB-09, client CQ-FCB-05; TODO `cooking/kitchen #40`)

- Switch `[qol] cookwareKeepContents` (QoL-8, on). Cooking pot, copper pot, kettle.
- A survival player's break (one that drops the item) and the QoL-7 pickup (2.9) keep every slot in the item's
  `BlockEntityTag`: pot slots 0-8 (ingredients, meal, container slot, output) plus cook progress; copper pot slots 0-6;
  kettle slots 0-4 plus brew progress, and its tank (already kept, `farmers_respite.md` K11). The leftover slots drop at
  the block: remainder buffer (pot, kettle), the kettle's two bottle boxes, the copper pot's leftover slot.
- Placing the item restores them (vanilla `BlockItem` loads `BlockEntityTag`).
- Only marked breaks keep: `CookwareContents` marks the block entity (Forge persistent data, key
  `creativedelight:keep_contents`, valid for the current game tick) from a lowest-priority `BreakEvent` (not cancelled,
  not creative, `canHarvestBlock`) or the pickup. Creative breaks, explosions, Create drills and `/setblock` drop the
  contents as before, so nothing is lost when no item follows.
- QoL-7 pickup with QoL-8 on: no longer empties the station into the inventory; the hand gets the stocked item.
  QoL-8 off: the 2.9 behaviour.
- Item: enchantment glint while it holds an item or kettle fluid (`ItemStack.hasFoil` mixin); tooltip "Holds:" with one
  line per stored stack except the meal (the mods' own "Serves" line shows that). All three items already stack to 1,
  so no stack-size change was needed.
- Code: CD `cooking/CookwareContents`, mixins `farmersdelight.CookingPotKeepContentsMixin`,
  `farmersrespite.KettleKeepContentsMixin`, `minecraft.ItemStackCookwareGlintMixin`; copper pot in the Miner's member
  (`CopperPotBlockEntity.writeMeal` / `getDroppableInventory`); `cooking/CookwareHandling` (pickup).
- Risk: creative pick-block (middle click) of a pot is unchanged (meal only). A pot broken in creative drops its
  contents loose, as before.

### 2.13 Hung skillet (built 2026-11-07, not tested; scratch SQ-FCB-10, client CQ-FCB-06; TODO `cooking/kitchen #39`)

- Block `farmersdelight:hanging_skillet` (FD member, `block/HangingSkilletBlock`), no item of its own; name "Skillet".
- Placing: the skillet's own placement (sneak right-click, upstream) on the side of a block hangs it on that wall when
  the wall face is sturdy and the block above the hanging spot is air or replaceable (the handle reaches into it).
  Anything else places the standing skillet as upstream. Mixin `SkilletHangMixin` (member config).
- Keeps the skillet stack (name, enchantments, damage) in FD's skillet block entity (the hanging block is added to its
  valid blocks at common setup); breaking it drops that skillet (loot `copy_skillet`), pick block gives it, right-click
  with an empty main hand takes it down into the hand. Pops off (drops) when the wall goes.
- Does not cook: no ticker, food right-click does nothing.
- Model: FD's skillet model (MIT) turned upright against the wall, handle up into the block above
  (`models/block/hanging_skillet.json`), one blockstate per facing. Hardness 0.5, no tool tag (a tag file collides with
  FD's and Cooking for Blockheads' own `mineable/pickaxe.json` paths in the build guards); breaks fast by hand.
- Not in `#creativedelight:cookware_pickup`.

### 2.14 Faucet fits containers (built 2026-11-07, not tested; client CQ-FAU-01, -02; TODO `cooking/kitchen #26`)

- Supplementaries 2.4.20 faucet. CD `compat/supplementaries/FaucetFit` (shape math), client `FaucetFitModel` (wrapper on
  every faucet block-state model, from `ModelEvent.BakingCompleted` at lowest priority) and mixin
  `supplementaries.FaucetStreamFitMixin` (the tile renderer's stream).
- Horizontal: the faucet model and its stream move toward the block it drains by how far that block's shape stands back
  from the shared face (a Supplementaries jar 3 px, a kettle or a pot by their shapes), at most 6 px; held so the 4 px
  spout stays over the block below when there is one.
- Vertical: the stream ends on the top of the block below (its shape top) instead of 5 px above that block's floor; it
  reaches down into a low block (a carpet) and stops at a full block's top. Nothing below: upstream length.
- When the faucet is connected below (a jar, a fluid tank: Supplementaries' `has_jar` model, spout at the block's
  floor) no stream is drawn, as upstream; the jar's neck top is at y 16, so the spout already meets it.
- Owner's "start with the jar, then other containers and the kettle": all go through the same shape rule; no per-block
  table yet. If a container looks wrong in game, a table row (block -> inset, floor) is the next step.

## 3. What the pack changes

- Config: `cookingforblockheads-common.toml` (`ovenRequiresCookingOil`, `sinkRequiresWater`); `creativeengineering-common.toml`
  `[kitchen]`.
- Tags: `#balm:cooking_oil` + Rustic Delight oil; `#creativeengineering:counter_supports`, `#creativeengineering:counter_toppers`.
- Creative Delight member blockheadsAdditions (section 9) mixins (all `remap = false`, SRG names): CfB oven fuel and
  smoking, `BlockStateBase#canSurvive` / `#updateShape` / `#getOffset` (counter toppers), FD board and Brewin' coaster
  renderers (client), CfB corner block entity and use, CfB oven `lit` state, sync, shape and renderer (2.10). Fridge
  fill and raised oven by baked-model wrappers (2.4, 2.10). Wood racks (2.7).
- Resource overrides: none since 2026-10-31 (the fridge override was removed, 2.4).
- Recipes: bamboo bowl; mortar crafts (own and catalogue); slime block from a dough block + green dye in a heated mixer
  (`cooking/kitchen #12`, Creative Delight script `kubejs/data/creativedelight/kubejs/create/slime.js`, id
  `creativedelight:createmixing/slime_block_from_dough_block`; it replaces the Pickling Jar's slime block).
- Tooltips: CfB oven, sink.

## 4. Limits and decisions

- Oven fuel: any furnace fuel plus oil (user, 2026-10-21), not CfB's oil-only design the brief first recommended.
- Cutting board: drawn 1 px lower with a config offset (user); the hitbox is not moved, so a VoxelShape outside its
  cell can't upset clicks on the board.
- Fridge: only the 2-tall form (user); the 1-tall fridge and the item model are unchanged; collision was already a
  full block. A white box on top instead of stretching the model (user, 2026-10-25); it does not reach the block's
  sides, so the 1 px gaps beside counters stay.
- Corner storage: the floor corner only; a chest GUI (vanilla `ChestMenu`) rather than CfB's counter screen, whose
  menu is tied to `CounterBlockEntity`.
- Mortar: hand yield = machine average rounded down, no bonus rolls or byproducts; the mortar is the pack's own item
  (user, 2026-10-21), so food grinding doesn't need Old Guns' Gunsmith's Bench. IE crusher recipes can't be catalogued (the export has no ingredients for them).
- Rejected: an inventory sweep stripping empty tags from returned bowls (row #5) was built and removed (user); the
  cause was Sticky It!'s tooltip handler, fixed in Creative Labels.

### Planned from the scratchpad (2026-10-24)

Brief: [briefs/cooking_kitchen.md](briefs/cooking_kitchen.md). Nothing here is built.

- [TODO](todo/TODO.md) `cooking/kitchen #18` (lava bottles in jars) is folded into `items/jars #3`; probable cause: Moonlight's lava soft fluid has an
  invalid empty container (see [mixed_jars.md](mixed_jars.md) planned block).

## 5. File map

- Creative Delight member [blockheads/](../development/creativeDelight/members/blockheads/) (section 9), package
  `com.SaintAlfonzo.creativedelight.blockheads`: `mixin/` (`CfbOvenFuelMixin`, `CfbOvenSmokingMixin`,
  `BlockStateCounterTopperMixin`, `CuttingBoardRendererOffsetMixin`, `CoasterRendererOffsetMixin`,
  `CfbCornerStorageMixin`, `CfbCornerUseMixin`, `CfbOvenLitMixin`, `CfbOvenLitSyncMixin`, `CfbOvenShapeMixin`,
  `CfbOvenRendererMixin`), `kitchen/` (`CounterToppers`, `CornerStorage`), `rack/` (`RackWoods`,
  `BARacks`, `WoodSpiceRackBlock`, `WoodToolRackBlock`), `client/FridgeTopFill`, `client/OvenRaise`, `BAConfig`.
- KubeJS: [cooking.js](../kubejs/server_scripts/cooking/cooking.js) (bamboo bowl, `foodGrinding` calls),
  [mortar.js](../kubejs/data/creativedelight/kubejs/creativeengineering/mortar.js), [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js),
  [cookingforblockheads/kitchen.js](../kubejs/data/creativedelight/kubejs/cookingforblockheads/kitchen.js) (rack
  recipes and tags, counter tags, oven heat source tag), [client.js](../kubejs/assets/creativedelight/kubejs/cookingforblockheads/client.js) (oven, sink tooltips),
  NCH in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js); dict keys `i.mortar_and_pestle`, `i.powder_pestle`,
  `i.t_balm_cooking_oil`, `i.cfb_*`, `i.<wood>_spice_rack`, `i.<wood>_tool_rack`, `i.t_rack_slabs_other`,
  `i.t_balm_iron_nuggets`, `b.cfb_counter`, `b.cfb_corner`, `b.dd_food_plate`,
  `b.dd_small_food_plate`, `b.dd_plated_foods`, `b.coaster`, `b.t_counter_supports`, `b.t_counter_toppers`.
- Generated: [mortar_catalog.json](../kubejs/generation_scripts/food/mortar_catalog.json) by
  [gen_mortar_catalog.py](../tools/gen_mortar_catalog.py); the racks' resources and the fridge box model in the member
  by [gen_cfb_racks.py](../tools/gen_cfb_racks.py).
- Probes: [tools/kitchen_probe/](../tools/kitchen_probe/).

## 6. Offline checks

Scratch dedicated server (`tools/creative_labels_test/scratch_server.sh`, the probe copied into
`srv/kubejs/server_scripts/`), 2026-10-21, all PASSED:

- `cfb_kitchen_probe.js` (`/kttest`, 17 checks): the oil is in `#balm:cooking_oil`; coal and oil are oven fuel; an
  oven with 3 oils burns 264 ticks each with 1 bottle in its output after the first and 2 after the second; a bucket
  on the sink comes back full; a board placed on a counter stays and cuts beef; its offset is (0, -1, 0) px, and
  with X = 1, Z = 2 turns with a north / south / east / west counter; zero off a counter.
- `cfb_corner_probe.js` (`/kctest`, 9 checks): 27 slots; right-click opens a chest; saved and loaded back; item
  handler; kitchen item provider; a board placed on top with no GUI; dye still recolours; contents drop.
- `mortar_probe.js` (runs at server load, 33 checks, all pass 2026-10-21 with the own mortar item and the cornmeal fixes): every new recipe
  id loads; the mortar crafts from a lever and polished granite; wheat + Powder Pestle has no recipe; wheat + mortar -> 1 flour and the
  mortar back at 1 damage; 3 kernels -> 1 cornmeal; 2 corn -> 3 cornmeal and 1 corn -> nothing (KM-08, KM-09); no crate or bag mortar craft (KM-12); 2 wild corn -> 3 (KM-13); raw
  tortilla from 3 cornmeal + water bottle or bucket, container back (KM-10, KM-11); sugar cane -> 2 sugar; 3 bamboo -> 1 bowl.
- `python3 tools/gen_cfb_racks.py --check`; `python3 tools/gen_mortar_catalog.py` (prints the crafts).
- 2026-10-31: `:blockheadsAdditions:build` passes `checkNoArrResources` (no path of the CfB jar in ours); the SRG
  field `BlockEntityType.f_58915_` checked against the 1.19.2 srg client jar.

Run 2026-11-01 (SQ-KT-25, 34 of 34) and r1102b (SQ-KT-02 PASS); was queued: `/kttest` now also places a plate, a small plate and a coaster on a counter and a plate on a
corner (KT-15a/b, KT-16a), sets a plated hamburger on a counter (KT-15c), moves it with `plateOffsetY` (KT-16b) and
checks a plate over air stays with its wooden leg (KT-15d); and breaks the block under a plate and a plated hamburger
on stone and a plate and a coaster on a counter: the coaster pops, the Display Delight blocks keep a leg (KT-17;
rewritten 2026-10-27 for the 1.7.0 port, which the old pop expectation predated).

Not covered offline: anything drawn (board offset, fridge), the corner's GUI on a client, JEI.

## 7. In-game test suite

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-KT-02.
- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-FCB-07, -08b.

- [ ] **KT-01 CfB oven fuel:** coal starts a Cooking for Blockheads Oven, and so does Cooking Oil.
- [ ] **KT-02 Sink:** right-click the Sink with an empty bucket → a water bucket, with nothing piped in.
- [ ] **KT-03 Board on a counter:** a Cutting Board places on a Kitchen Counter's top (and a Kitchen Corner's) and
  cuts.
- [ ] **KT-04 Oil bottles:** 5 Cooking Oil in the oven's fuel slot, cook until 2 are used → 2 glass bottles in the
  output slots; when the last oil burns, its bottle is in the fuel slot.
- [ ] **KT-05 Board drawn on the worktop:** the board and an item on it sit on the counter top with no gap, on
  counters facing all four ways; set `cuttingBoardOffsetZ = -2`, restart, F3+A → it moves toward the door on each.
- [ ] **KT-06 Fridge look:** a 2-tall fridge: CfB's own body and doors (not stretched), a white box on top (textured, not
  purple-black) level
  with a full block next to it, a candle on top sits on it; the door opens and closes cleanly on both hinge sides and
  passes under the box; a dyed fridge's body and box both take the colour.
- [ ] **KT-07 Bamboo bowl:** 3 bamboo in a V → 1 bowl.
- [ ] **KT-08 Mortar:** wheat + Mortar and Pestle in the crafting table → wheat flour, the mortar back with 1 damage;
  JEI's uses page for the mortar lists the grinding crafts.
- [ ] **KT-09 Corner GUI:** right-click a Kitchen Corner → a 3-row chest titled "Kitchen Corner".
- [ ] **KT-10 Corner keeps items:** put items in, relog → still there.
- [ ] **KT-11 Corner drops:** break it → its contents drop.
- [ ] **KT-12 Corner feeds the Cooking Table:** a Cooking Table connected to the corner (through counters) lists
  recipes from ingredients kept only in the corner.
- [ ] **KT-13 Mortar item:** lever over 3 polished granite (Old Guns' shape) in the crafting table → Mortar and
  Pestle with the Old Guns texture; Old Guns' item reads "Powder Pestle" and wheat + Powder Pestle gives nothing.
- [ ] **KT-15 Plates and coasters on a counter:** a Food Plate, a Small Food Plate and a Coaster each place on a
  Kitchen Counter's top and a Kitchen Corner's; put a hamburger on the plate (plated hamburger) and drinks on the
  coaster: both stay; reload the world: still there. Placing a coaster on a counter, then breaking the counter, pops
  the coaster.
- [ ] **KT-16 Plates drawn on the worktop:** the plate, its food, the coaster and its drinks sit on the counter top with
  no gap, on counters facing all four ways; set `plateOffsetY = -3`, restart, F3+A → plates and coaster sink 2 px
  more, the cutting board does not move.
- [ ] **KT-17 Block below goes:** a Food Plate, a plated food and a Display Delight bowl on stone, and a plate and a
  Coaster with drinks on a counter: break the block under each → the Display Delight blocks stay on a wooden leg; the
  coaster breaks and drops its drinks; nothing floats without a leg.
- [ ] **KT-18 Slime block in the mixer:** a dough block + green dye in a heated basin → slime block; JEI lists it under
  Mixing (needs the Creative Delight jar).
- [ ] **KT-19 Block-modelled foods on a coaster:** on a Coaster put a Display Delight food (Apple Cider, Hamburger, a
  Farmer's Respite tea), a Vinery wine (Red Wine, Stal Wine), a Vintage Delight jar (Honey Jar) and an Enchanted
  Golden Carrot → each shows its small 3D model (wine as a bottle, jar as a jar, carrot with glint), none lies flat as
  a sprite or is invisible, four fit without overlapping much; the tooltip says "Placeable on a coaster"; no
  `Failed to get model` in `latest.log`.
- [ ] **KT-22 Wood racks:** JEI lists 20 spice racks and 20 tool racks in CfB's tab; 2 oak slabs in a row craft an
  Oak Spice Rack (not CfB's Spice Rack), 2 slabs of a wood with no rack (e.g. Quark's Blossom) still craft CfB's Spice Rack;
  3 Crimson slabs over 2 iron nuggets -> Crimson Tool Rack; 2 rubberwood planks -> Rubberwood Spice Rack. Place one of
  each on a wall facing all four ways: drawn in its wood with no purple-black, an axe breaks it fast and it drops itself.
- [ ] **KT-23 Wood racks work:** put spices in a Birch Spice Rack and a knife in a Warped Tool Rack next to a Cooking
  Table: both show their items on the rack, the table uses them; break and replace the racks: contents drop; relog with
  items inside: still there.
- [ ] **KT-25 Wood fruit baskets:** JEI lists 20 fruit baskets; oak slab, any pressure plate, oak slab -> Oak Fruit Basket (not
  CfB's); a Quark Blossom slab version still crafts the plain one; rubberwood planks + plate -> Rubberwood Fruit Basket. Place
  one: drawn in its wood, axe breaks it and it drops itself; put fruit in, open, relog: contents kept, renderer shows them.
- [ ] **KT-24 After the move:** an existing Kitchen Corner still holds what it held before the update; KT-04, KT-05,
  KT-06 (fridge box textured, dyed fridge's box dyed), KT-16 pass again; `config/blockheads_additions-common.toml` has
  `[kitchen]`.
- [ ] **KT-20 Unmodelled items refused:** right click a Coaster with a Fruits Delight jelly, a Golden Cookie and Cooked
  Goose (no coaster model) → not taken (the item is used or
  placed as normal); a port drink (Beer) is still taken.

- [ ] **KT-26 Stocked pot (cooking/kitchen #40):** put ingredients, bowls and a finished meal in a Cooking Pot, break it
  in survival → one glinted pot item, tooltip "Holds:" lists the ingredients and bowls; place it → all slots as before.
  Same for a Copper Pot and a Kettle with tea leaves and water (the tank comes back too). Bottles in the leftover slots
  drop on the ground. Sneak right-click pickup with an empty hand → the same stocked item in the hand. Break one in
  creative → contents drop loose.
- [ ] **KT-27 Hung skillet (cooking/kitchen #39):** sneak right-click the side of a stone wall with an enchanted, renamed
  Skillet, air above → it hangs on the wall, handle up; with a block above → a standing skillet as before. Food in the
  other hand does not cook on it. Break it → the same enchanted, renamed Skillet; empty-hand right-click → into the hand;
  break the wall → it drops.
- [ ] **KT-28 Faucet fit (cooking/kitchen #26):** a faucet on the side of a Supplementaries Jar sits against the jar's
  glass (no 3 px gap); open it over a carpet and over a Cooking Pot and a Kettle → the stream ends on the carpet / the
  rim; faucet on a full block over a full block → stream stops at the top of the lower block.

## 8. History

- 2026-10-21: writeup started from the kitchen brief's built sections. Built the same day (patcherMod 1.1.01, not
  deployed): CfB oven fuel and bottles, sink, cutting board support and offset (`cooking/kitchen #8`), fridge models
  (`#6`), corner storage (`#15`), bamboo bowl and mortar grinding (`#10`); scythe and stepstool split to `#16`,
  `#17`. Row `#5`'s cause found in Sticky It!'s tooltip handler (fixed by Creative Labels). Corner tests renamed from
  the brief's KC-01 to KC-04 to KT-09 to KT-12.
- 2026-10-24: planned from the scratchpad: fridge texture padding, plates and coasters on the counter (`textures/art #1`, `cooking/kitchen #20`). No code changed.
- 2026-10-24: built `#20` (patcherMod 1.1.02, in `mods/` only): the cutting board route generalised to counter toppers
  (2.3), `plateOffset*` config, coaster renderer mixin; `cutting_board_supports` renamed `counter_supports`. KT-15, KT-16
  added; `/kttest` extended, scratch run queued (SQ-KT).
- 2026-10-25: Display Delight blocks, toppers and the coaster break when the block below goes (2.3, KT-17, patcherMod
  1.1.02 rebuilt, not in `mods/`). Fridge stretch reverted, white top fill box instead (2.4, `#6`, `#19`); KT-06
  rewritten.
- 2026-10-25: slime block mixing (`cooking/kitchen #12`, KT-18; the brief's KT-08 number was taken), written as a
  Creative Delight script.
- 2026-10-27: KT-15d and KT-17 rewritten for Display Delight 1.7.0 (plates over air or on a broken block get a wooden
  leg, only the coaster pops); `/kttest` updated. The 2026-10-27 scratch run's 4 kt fails were these expectations.
- 2026-10-29: coaster contents (2.3): `tools/gen_coaster_models.py` reads Display Delight 1.7.0's multipart
  blockstates and all its jar links, and adds Vinery wine bottles, Vintage Delight jars and same-sprite borrows; 208
  pack coaster files, tag 96 to 304 (`placeable_catalog.py` rerun). Data only, no build. KT-19, KT-20.
- 2026-10-31: deep scan: bamboo bowl and mortar recipe file paths updated (moved into Creative Delight 2026-10-25); stepstool row: patcherMod 1.1.04 installed 2026-10-31 00:44, not yet booted. No code changed.
- 2026-10-31: Cooking for Blockheads folded into Creative Delight as the member blockheadsAdditions (section 9): the CfB
  mixins, corner storage and counter toppers moved out of patcherMod (`[kitchen]` config with them), the fridge
  override replaced by code (2.4), 40 wood racks from the owner's art (2.7, `cooking/kitchen #22`). KT-22 to KT-24.
- 2026-11-02: deep scan 2026-11-02: At a glance and 2.7 to 2.9 updated from the scratch runs (SQ-KT-21 r1031;
  SQ-KT-22 to -25 2026-11-01; SQ-KT-02, SQ-FCB-05, -07, -08 r1102b). No KT- test run in game yet.
- 2026-11-04: wood cutting boards built (2.11, `cooking/kitchen #38`); counter offset now covers the stone boards too.
- 2026-11-04: M8 (plan_cb_publish.md 3b): blockheadsAdditions folded into Creative Delight's own jar as the optional
  Cooking for Blockheads compartment (section 9); racks and baskets renamed to `creativedelight:*` (swap list
  `compartments.json`), config moved to `[cookingforblockheads]`. CD 1.1.0 built, not installed. SQ-CDFO-01 to -03, CQ-CDFO-01.

## 9. Creative Delight's Cooking for Blockheads compartment (was the blockheadsAdditions member)

- **Since M8 (2026-11-04, plan_cb_publish.md 3b, decisions D3 and D6):** an optional compartment of Creative Delight's own
  jar, no longer a nested mod. The mod id `blockheads_additions` and the project `:blockheadsAdditions`
  (`development/creativeDelight/members/blockheads/`) are gone. Creative Delight loads without Cooking for Blockheads or
  Balm; with both (and `[compartments] cookingforblockheads = true` in `creativedelight-common.toml`, read at startup)
  everything works as before.
  - Code: `compat/cookingforblockheads/` (`CfbCompartment` entry, `kitchen/`, `rack/`, `client/`) and
    `mixin/cookingforblockheads/` (the oven, corner and `BlockStateCounterTopperMixin`), `mixin/farmersdelight/
    CuttingBoardRendererOffsetMixin`, `mixin/brewinandchewin/CoasterRendererOffsetMixin` (Brewin' is Creative Brewing's).
  - Load guard: `CreativeDelight.registerCompartments` calls `CfbCompartment.init` only when `compat/Compartments` says
    the compartment is on (CfB and Balm loaded, switch on). Only those classes name CfB types; their listeners are
    registered from `init` (no `@Mod.EventBusSubscriber`). `CDMixinPlugin` applies the mixins only with the compartment on.
  - Assets and loot tables: the built-in pack `packs/cookingforblockheads/` in the jar (`compat/CompartmentPacks`, added
    only with the compartment on), written by `tools/gen_cfb_racks.py`.
  - Ids: the racks and baskets are `creativedelight:<wood>_spice_rack`, `_tool_rack`, `_fruit_basket` (were
    `cookingforblockheads:<wood>_*`); `data/creativedelight/swaps/compartments.json` remaps old saves, and the merges of
    CfB's own rack, tool rack and basket name the new oak ids. CfB's namespace is no longer used for any of our files.
  - Config: `[cookingforblockheads]` in `creativedelight-common.toml` (the six counter offsets; were
    `blockheads_additions-common.toml [kitchen]`, carried once by `MemberConfigMove`). The old `[members]
    blockheads_additions` switch became `[compartments] cookingforblockheads` (value carried once).
  - Scripts: `global.CD.on('cookingforblockheads')` is the compartment test (`kitchen.js`, the oven tooltip lines,
    `swap_merges.js`); rack recipes skip a wood whose slab mod is absent.
- **Licence route:** Cooking for Blockheads 13.3.4 is All Rights Reserved (its `LICENSE`: "All Rights Reserved,
  Copyright (c) 2023 BlayTheNinth"); its permissions page (mods.twelveiterations.com/permissions, read 2026-10-31)
  allows modpack use but no rehosting and no publicly published modified versions. It cannot be repacked like the MIT
  members, and a Miner's-style rewrite of the whole mod (with Balm) is out of proportion, so CfB stays its own jar in
  `mods/` (optional dependency of CD, `ordering="AFTER"`) and the compartment ships only our code (GPLv3), our art and
  our data. The build guards `checkNoArrResources` (jar root, `resource_guard_jars=mods/cookingforblockheads-*.jar` in
  CD's `gradle.properties`) and `checkNoArrPackResources` (CD `build.gradle`, the files under `packs/*/`) fail on any
  path the CfB jar also ships (tag files excepted: they merge by path). Models name CfB's models as parents and its
  textures by id, never copy them.
- **Moved from patcherMod (2026-10-31), behaviour unchanged:** `CfbOvenFuelMixin`, `CfbOvenSmokingMixin`,
  `CfbCornerStorageMixin`, `CfbCornerUseMixin`, `CfbCornerStorage` (now `kitchen/CornerStorage`),
  `CounterToppers`, `BlockStateCounterTopperMixin`, `CuttingBoardRendererOffsetMixin`, `CoasterRendererOffsetMixin`,
  `CEConfig.Kitchen` (then `BAConfig`, now `CDConfig.CFB`, same keys). Kept ids: the corner capability
  `creativeengineering:corner_storage` and the tags `#creativeengineering:counter_supports` / `counter_toppers`. The
  pack's own Display Delight dishes are recognised by their Display Delight block class. The counter tags live in
  `cookingforblockheads/kitchen.js`.
- **New:** wood racks (2.7), fridge fill in code (2.4), oven as a stove (2.10).
- **Build:** `./gradlew :creativeDelight:build --offline`, install `:creativeDelight:installToMods`. Compiles against
  CD's `libs/` (CfB 13.3.4 and Balm 4.6.0 deobf, git-ignored, copied from `creativeCommon/libs/`), Creative Common's
  `libs/` and `:backportedBrewin`.
- Still in the pack, not moved: CfB configs (`cookingforblockheads-common.toml`), milk jar bottles
  (`cookingforblockheads/milk_jar.js`) and tooltips (`assets/creativedelight/kubejs/cookingforblockheads/client.js`)
  were already in the Creative Delight trees.

2026-11-05: the 19 fruit basket and 38 rack textures no longer ship; Creative Delight builds them on the client from the installed planks and Cooking for Blockheads' own rack art ([creative_delight.md](creative_delight.md) 40.1). `gen_cfb_racks.py` writes models, blockstates, loot and lang only. Tests CQ-LCA-01, -02.

## Owner notes (scratchpad 2026-11-07)

- FD pot, kettle and copper pot keep contents on pickup: kettle keeps fluid, ingredients and output; pots keep input and output; overflow slots drop on break; a stocked item is glinted and does not stack. Row `cooking/kitchen #40`.
