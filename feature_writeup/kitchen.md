# Kitchen: Cooking for Blockheads parts, cutting board, fridge, corner storage, bamboo bowl, mortar and pestle

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
| Display Delight plates and plated foods, Brewin' coaster on a CfB counter or corner, drawn on the worktop, own offset | BUILT 2026-10-24 (patcherMod 1.1.02 + KubeJS tags), scratch test queued (SQ-KT) | `#20` |
| CfB 2-tall fridge filled to a full block with a white box on top (the 2026-10-21 stretch reverted) | BUILT 2026-10-25 (resource override), not seen in a client | `#6`, `#19` |
| Display Delight blocks, toppers and the coaster break with drops when the block below goes | BUILT 2026-10-25 (patcherMod 1.1.02, not in `mods/`), scratch test queued (SQ-KT) | `#20` |
| CfB kitchen corner storage (27 slots, chest GUI, hoppers, cooking table) | BUILT 2026-10-21 (patcherMod), scratch server PASSED | `#15` |
| Bamboo bowl | BUILT 2026-10-21 (KubeJS), scratch server PASSED | `#10` |
| Mortar and pestle food grinding (own recipes through `foodGrinding`, other mods' through a catalogue) | BUILT 2026-10-21 (KubeJS + generator), scratch server PASSED | `#10` |
| Returned bowls that wouldn't stack | cause found elsewhere: Sticky It!'s tooltip handler, fixed by Creative Labels ([sticky_notes.md](sticky_notes.md) 3.13, SN-48) | `#5` |
| Scythe, stepstool | Planned | `#16`, `#17` |

- **Pending:** nothing here has been deployed (patcherMod 1.1.01 is in `mods/` and `server_mods/`; 1.1.02 with the
  counter toppers is in `mods/` only). KT-01 to KT-16.
- **Files to open:** patcherMod `mixin/CfbOvenFuelMixin`, `mixin/BlockStateCounterTopperMixin`,
  `mixin/CuttingBoardRendererOffsetMixin`, `mixin/CoasterRendererOffsetMixin`, `compat/CounterToppers`,
  `mixin/CfbCornerStorageMixin`, `mixin/CfbCornerUseMixin`, `compat/CfbCornerStorage`;
  [tools/gen_cfb_fridge_models.py](../tools/gen_cfb_fridge_models.py);
  [cooking/mortar.js](../kubejs/server_scripts/cooking/mortar.js) and NCH `mortar` / `foodGrinding`;
  [tools/gen_mortar_catalog.py](../tools/gen_mortar_catalog.py).
- **Knobs:** `config/creativeengineering-common.toml` `[kitchen] cuttingBoardOffsetX / Y / Z` and `plateOffsetX / Y / Z`
  (pixels; Y default -1);
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
- Breaking the block below: Display Delight's blocks check survival only when placed, so they stayed in the air when
  the counter (or any block) under them was broken. The same mixin's RETURN on `updateShape` (m_60728_) answers air for
  a change from below when the state can no longer survive, for every topper and every `displaydelight` block (bowls and
  drinks too; `CounterToppers.popsUnsupported`, cached per state with the topper flag). Vanilla's `updateOrDestroy`
  then breaks it with its loot. The board and the coaster already did this in their own `updateShape` (the coaster
  drops its drinks in `onRemove`).
- Drawn on the worktop: `CounterToppers.offset` gives the shift for a topper above a support. The same mixin's HEAD
  on `getOffset` (m_60824_) returns it for a topper, so the chunk mesher (vanilla and Rubidium both call it) moves the
  model; plated foods draw the food inside the block model, so plate and food move together. Whether a state is a
  topper is cached per state for one tag generation (`CounterToppers.init` bumps it on `TagsUpdatedEvent`), so
  `getOffset`, run for every block the mesher draws, does no tag lookup. Block entity renderers:
  `CuttingBoardRendererOffsetMixin` (client) pushes and translates FD's `CuttingBoardRenderer.render`, so the item on
  the board moves too; `CoasterRendererOffsetMixin` (client) does the same for the port's `CoasterBlockEntityRenderer`,
  which draws the coaster itself and its drinks (no block model). The port jar is a compileOnly lib in
  `development/patcherMod/libs/` (copied from `mods/`). The hitbox and collision stay where they were (1 px above).
- Offset config `[kitchen]` in pixels, -8 to 8: `cuttingBoardOffsetX / Y / Z` for the board, `plateOffsetX / Y / Z`
  for the plates, plated foods and the coaster (Y -1, X and Z 0 for both). X and Z in the frame of a counter facing
  north (+X east, +Z toward its back), turned with the counter's `facing` (south (-x, -z), east (-z, x), west
  (z, -x)). Visual only: F3+A redraws after a restart.

### 2.4 Fridge

- The 2-tall fridge (two fridges stacked) is x 1-15, z 3-15 (door front at z 1) with its upper top at y 13, so decor
  on top floated 3 px above it. `tools/gen_cfb_fridge_models.py` reads CfB's `fridge_large_upper` from the jar and
  writes it to `kubejs/assets/cookingforblockheads/models/block/` unchanged plus one element `CE_TopFill`: x 1-15,
  y 13-16, z 1-15 (the body and the closed door's front), all six faces (the bottom shows over the door's gap when it
  swings open). CfB loads the model by id in code (`client/ModModels`), not through `blockstates/fridge.json`.
- Texture `creativeengineering:block/fridge_top_fill` (written by the script): flat white 246, the body's own white;
  `tintindex` 0, so a dyed fridge's box takes the dye like the body.
- Was (2026-10-21 to 2026-10-25): six overrides stretched the body walls out to x 0-16 / z 16 and the doors to full
  width and height, with position UVs on the stretched faces. The textures looked wrong (`#19`), so it was reverted
  (user): the script deletes those overrides, and the lower half and doors are CfB's own again. `--check` reports
  the override or texture out of step with the jar, and any old override left behind.

### 2.5 Corner storage

- CfB's floor corner (`cookingforblockheads:corner`, "Kitchen Corner") had only a kitchen connector.
  `CfbCornerStorageMixin` makes `CornerBlockEntity` a Balm `BalmContainerProvider` holding a
  `CfbCornerStorage.CornerContainer` (27 slots, 54 with CfB's `largeCounters`, like a counter). Through Balm's
  `buildProviders` that gives an item handler (hoppers, pipes), and CfB's `BlockKitchen.onRemove` drops the contents.
  Its `getProviders` gains a `DefaultKitchenItemProvider`, so a connected Cooking Table uses what's inside.
- `CfbCornerUseMixin`: CfB's `use` recolours with dye and answers FAIL otherwise; FAIL now opens a vanilla chest menu
  titled with the block's name, except for a block item on the top face (placement, e.g. the cutting board).
- Saved as an attached capability (`ForgeCaps/creativeengineering:corner_storage`); the container marks the block
  entity changed and closes for a player more than 8 blocks away or when the corner goes.
- Not covered: CfB's hanging corner (its `newBlockEntity` returns null) and Farmhouse Decorations' corner cabinets
  (no block entity); both would need new blocks.

### 2.6 Bamboo bowl, mortar and pestle

- **Bamboo bowl:** 3 bamboo in the bowl's V -> 1 bowl (`cooking.js`, `shaped/bowl_from_bamboo`). Planks give 4, so
  bamboo isn't the cheap route.
- **Mortar and pestle** (`creativeengineering:mortar_and_pestle`, `i.mortar_and_pestle`, KubeJS item in
  `items/misc_items.js`, 512 durability, texture copied from Old Guns' (AGPL-3.0)). Crafting table: lever over
  polished granite in Old Guns' bench shape (`cooking.js`, `shaped/mortar_and_pestle`). NCH
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

## 3. What the pack changes

- Config: `cookingforblockheads-common.toml` (`ovenRequiresCookingOil`, `sinkRequiresWater`); `creativeengineering-common.toml`
  `[kitchen]`.
- Tags: `#balm:cooking_oil` + Rustic Delight oil; `#creativeengineering:counter_supports`, `#creativeengineering:counter_toppers`.
- patcherMod mixins (all `remap = false`, SRG names): CfB oven fuel, `BlockStateBase#canSurvive` / `#getOffset`
  (counter toppers), FD board and Brewin' coaster renderers (client), CfB corner block entity and use. CfB 13.3.4's and
  the Brewin' port's jars are in `development/patcherMod/libs/` to compile against.
- Resource overrides: CfB's `fridge_large_upper` (top fill box) and its texture.
- Recipes: bamboo bowl; mortar crafts (own and catalogue).
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

- patcherMod [mixin/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/):
  `CfbOvenFuelMixin`, `BlockStateCounterTopperMixin`, `CuttingBoardRendererOffsetMixin`, `CoasterRendererOffsetMixin`,
  `CfbCornerStorageMixin`, `CfbCornerUseMixin`;
  [compat/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/):
  `CounterToppers`, `CfbCornerStorage`; `CEConfig.Kitchen`.
- KubeJS: [cooking.js](../kubejs/server_scripts/cooking/cooking.js) (bamboo bowl, `foodGrinding` calls),
  [mortar.js](../kubejs/server_scripts/cooking/mortar.js), [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js),
  [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), [tooltips.js](../kubejs/client_scripts/tooltips.js),
  NCH in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js); dict keys `i.mortar_and_pestle`, `i.powder_pestle`,
  `i.t_balm_cooking_oil`, `i.cfb_*`, `b.cfb_counter`, `b.cfb_corner`, `b.dd_food_plate`,
  `b.dd_small_food_plate`, `b.dd_plated_foods`, `b.coaster`, `b.t_counter_supports`, `b.t_counter_toppers`.
- Generated: [mortar_catalog.json](../kubejs/generation_scripts/food/mortar_catalog.json) by
  [gen_mortar_catalog.py](../tools/gen_mortar_catalog.py); fridge model in
  `kubejs/assets/cookingforblockheads/models/block/` and `textures/block/fridge_top_fill.png` by [gen_cfb_fridge_models.py](../tools/gen_cfb_fridge_models.py).
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
- `python3 tools/gen_cfb_fridge_models.py --check`; `python3 tools/gen_mortar_catalog.py` (prints the crafts).

Queued (SQ-KT, not run): `/kttest` now also places a plate, a small plate and a coaster on a counter and a plate on a
corner (KT-15a/b, KT-16a), sets a plated hamburger on a counter (KT-15c), moves it with `plateOffsetY` (KT-16b) and
checks a plate over air is still refused (KT-15d); and breaks the block under a plate and a plated hamburger on
stone and a plate and a coaster on a counter, each must pop (KT-17).

Not covered offline: anything drawn (board offset, fridge), the corner's GUI on a client, JEI.

## 7. In-game test suite

- [ ] **KT-01 CfB oven fuel:** coal starts a Cooking for Blockheads Oven, and so does Cooking Oil.
- [ ] **KT-02 Sink:** right-click the Sink with an empty bucket → a water bucket, with nothing piped in.
- [ ] **KT-03 Board on a counter:** a Cutting Board places on a Kitchen Counter's top (and a Kitchen Corner's) and
  cuts.
- [ ] **KT-04 Oil bottles:** 5 Cooking Oil in the oven's fuel slot, cook until 2 are used → 2 glass bottles in the
  output slots; when the last oil burns, its bottle is in the fuel slot.
- [ ] **KT-05 Board drawn on the worktop:** the board and an item on it sit on the counter top with no gap, on
  counters facing all four ways; set `cuttingBoardOffsetZ = -2`, restart, F3+A → it moves toward the door on each.
- [ ] **KT-06 Fridge look:** a 2-tall fridge: CfB's own body and doors (not stretched), a white box on top level
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
- [ ] **KT-17 Plates pop when the block below goes:** a Food Plate, a plated food and a Display Delight bowl on stone,
  and a plate and a Coaster with drinks on a counter: break the block under each → it breaks and drops (the plated
  food its food, the coaster its drinks); nothing is left floating.

## 8. History

- 2026-10-21: writeup started from the kitchen brief's built sections. Built the same day (patcherMod 1.1.01, not
  deployed): CfB oven fuel and bottles, sink, cutting board support and offset (`cooking/kitchen #8`), fridge models
  (`#6`), corner storage (`#15`), bamboo bowl and mortar grinding (`#10`); scythe and stepstool split to `#16`,
  `#17`. Row `#5`'s cause found in Sticky It!'s tooltip handler (fixed by Creative Labels). Corner tests renamed from
  the brief's KC-01 to KC-04 to KT-09 to KT-12.
- 2026-10-24: planned from the scratchpad: fridge texture padding, plates and coasters on the counter (`cooking/kitchen #19`, `#20`). No code changed.
- 2026-10-24: built `#20` (patcherMod 1.1.02, in `mods/` only): the cutting board route generalised to counter toppers
  (2.3), `plateOffset*` config, coaster renderer mixin; `cutting_board_supports` renamed `counter_supports`. KT-15, KT-16
  added; `/kttest` extended, scratch run queued (SQ-KT).
- 2026-10-25: Display Delight blocks, toppers and the coaster break when the block below goes (2.3, KT-17, patcherMod
  1.1.02 rebuilt, not in `mods/`). Fridge stretch reverted, white top fill box instead (2.4, `#6`, `#19`); KT-06
  rewritten.
