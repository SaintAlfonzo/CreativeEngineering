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
| CfB 2-tall fridge reshaped to a full block | BUILT 2026-10-21 (resource override), not seen in a client | `#6` |
| CfB kitchen corner storage (27 slots, chest GUI, hoppers, cooking table) | BUILT 2026-10-21 (patcherMod), scratch server PASSED | `#15` |
| Bamboo bowl | BUILT 2026-10-21 (KubeJS), scratch server PASSED | `#10` |
| Mortar and pestle food grinding (own recipes through `foodGrinding`, other mods' through a catalogue) | BUILT 2026-10-21 (KubeJS + generator), scratch server PASSED | `#10` |
| Returned bowls that wouldn't stack | cause found elsewhere: Sticky It!'s tooltip handler, fixed by Creative Labels ([sticky_notes.md](sticky_notes.md) 3.13, SN-48) | `#5` |
| Scythe, stepstool | Planned | `#16`, `#17` |

- **Pending:** nothing here has been deployed (patcherMod 1.1.01 is in `mods/` and `server_mods/`). KT-01 to KT-13.
- **Files to open:** patcherMod `mixin/CfbOvenFuelMixin`, `mixin/CuttingBoardSupportMixin`,
  `mixin/BlockStateCuttingBoardOffsetMixin`, `mixin/CuttingBoardRendererOffsetMixin`, `compat/CuttingBoardOnCounter`,
  `mixin/CfbCornerStorageMixin`, `mixin/CfbCornerUseMixin`, `compat/CfbCornerStorage`;
  [tools/gen_cfb_fridge_models.py](../tools/gen_cfb_fridge_models.py);
  [cooking/mortar.js](../kubejs/server_scripts/cooking/mortar.js) and NCH `mortar` / `foodGrinding`;
  [tools/gen_mortar_catalog.py](../tools/gen_mortar_catalog.py).
- **Knobs:** `config/creativeengineering-common.toml` `[kitchen] cuttingBoardOffsetX / Y / Z` (pixels; Y default -1);
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

### 2.3 Cutting board on a counter

- CfB's `CounterBlock.use` returns FAIL for a block item on its top face (and for CfB's cutting board item), which
  lets the item place itself; CfB means boards to sit there. FD 1.2.4's `CuttingBoardBlock.canSurvive` then refused:
  it wants a rigid or center support face below, and CfB's counter and corner are 15 px tall (`Block.box(0.5, 0, 0,
  15.5, 15, 16)`, model worktop at y 15, backsplash to y 17 at the back).
- `CuttingBoardSupportMixin` (RETURN on `m_7898_`) also accepts a block in `#creativeengineering:cutting_board_supports`
  (`b.t_cutting_board_supports` = `b.cfb_counter`, `b.cfb_corner`, `blocktags.js`).
- Drawn on the worktop: `CuttingBoardOnCounter.offset` gives the shift for a board above a support block.
  `BlockStateCuttingBoardOffsetMixin` (HEAD on `BlockStateBase.getOffset`, m_60824_) returns it for the board, so the
  chunk mesher (vanilla and Rubidium both call it) moves the model; for every other block it is one reference compare.
  `CuttingBoardRendererOffsetMixin` (client) pushes and translates FD's `CuttingBoardRenderer.render`, so the item on
  the board moves too. The hitbox and collision stay where they were (1 px above the drawn board).
- Offset config `[kitchen] cuttingBoardOffsetX / Y / Z` in pixels, -8 to 8 (Y -1, X and Z 0): X and Z in the frame
  of a counter facing north (+X east, +Z toward its back), turned with the counter's `facing` (south (-x, -z), east
  (-z, x), west (z, -x)). Visual only: F3+A redraws after a restart.

### 2.4 Fridge

- The 2-tall fridge (two fridges stacked) was x 1-15, z 3-15 with its upper top at y 13. `tools/gen_cfb_fridge_models.py`
  reads CfB's own models from the jar and writes six overrides to
  `kubejs/assets/cookingforblockheads/models/block/`: `fridge_large_upper`, `fridge_large_lower` and the four large
  door models. CfB loads them by id in code (`client/ModModels`), not through `blockstates/fridge.json`.
- Body: walls x 0-2 / 14-16 out to z 16, top at y 16, bottom x 0-16; seals, shelves and the inside move out with the
  walls. Doors: x 0-16 and full height, at their old depth (z 1-2, handle in front), because `FridgeRenderer` swings
  them about a hard-coded hinge at x 14.5 (1.5 flipped), z 1.5; at 90 degrees the 1.5 px past the hinge turns into the
  side wall, out of sight.
- Stretched faces take position UVs; seals and handles keep their single-pixel UVs; `tintindex` stays, so dyed
  fridges colour. `--check` reports overrides out of step with the jar.

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
- Tags: `#balm:cooking_oil` + Rustic Delight oil; `#creativeengineering:cutting_board_supports`.
- patcherMod mixins (all `remap = false`, SRG names): CfB oven fuel, FD cutting board support, `BlockStateBase#getOffset`,
  FD board renderer (client), CfB corner block entity and use. CfB 13.3.4's jar is in `development/patcherMod/libs/`
  to compile against.
- Resource overrides: six CfB fridge models.
- Recipes: bamboo bowl; mortar crafts (own and catalogue).
- Tooltips: CfB oven, sink.

## 4. Limits and decisions

- Oven fuel: any furnace fuel plus oil (user, 2026-10-21), not CfB's oil-only design the brief first recommended.
- Cutting board: drawn 1 px lower with a config offset (user); the hitbox is not moved, so a VoxelShape outside its
  cell can't upset clicks on the board.
- Fridge: only the 2-tall form (user); the 1-tall fridge and the item model are unchanged; collision was already a
  full block.
- Corner storage: the floor corner only; a chest GUI (vanilla `ChestMenu`) rather than CfB's counter screen, whose
  menu is tied to `CounterBlockEntity`.
- Mortar: hand yield = machine average rounded down, no bonus rolls or byproducts; the mortar is the pack's own item
  (user, 2026-10-21), so food grinding doesn't need Old Guns' Gunsmith's Bench. IE crusher recipes can't be catalogued (the export has no ingredients for them).
- Rejected: an inventory sweep stripping empty tags from returned bowls (row #5) was built and removed (user); the
  cause was Sticky It!'s tooltip handler, fixed in Creative Labels.

## 5. File map

- patcherMod [mixin/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/):
  `CfbOvenFuelMixin`, `CuttingBoardSupportMixin`, `BlockStateCuttingBoardOffsetMixin`,
  `CuttingBoardRendererOffsetMixin`, `CfbCornerStorageMixin`, `CfbCornerUseMixin`;
  [compat/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/):
  `CuttingBoardOnCounter`, `CfbCornerStorage`; `CEConfig.Kitchen`.
- KubeJS: [cooking.js](../kubejs/server_scripts/cooking/cooking.js) (bamboo bowl, `foodGrinding` calls),
  [mortar.js](../kubejs/server_scripts/cooking/mortar.js), [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js),
  [blocktags.js](../kubejs/server_scripts/tags/blocktags.js), [tooltips.js](../kubejs/client_scripts/tooltips.js),
  NCH in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js); dict keys `i.mortar_and_pestle`, `i.powder_pestle`,
  `i.t_balm_cooking_oil`, `i.cfb_*`, `b.cfb_counter`, `b.cfb_corner`, `b.t_cutting_board_supports`.
- Generated: [mortar_catalog.json](../kubejs/generation_scripts/food/mortar_catalog.json) by
  [gen_mortar_catalog.py](../tools/gen_mortar_catalog.py); fridge models in
  `kubejs/assets/cookingforblockheads/models/block/` by [gen_cfb_fridge_models.py](../tools/gen_cfb_fridge_models.py).
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
- [ ] **KT-06 Fridge look:** a 2-tall fridge between two counters: no gaps at the sides, top flush with a full block
  next to it, a candle on top sits on it; the door opens and closes cleanly on both hinge sides; a dyed fridge keeps
  its colour.
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

## 8. History

- 2026-10-21: writeup started from the kitchen brief's built sections. Built the same day (patcherMod 1.1.01, not
  deployed): CfB oven fuel and bottles, sink, cutting board support and offset (`cooking/kitchen #8`), fridge models
  (`#6`), corner storage (`#15`), bamboo bowl and mortar grinding (`#10`); scythe and stepstool split to `#16`,
  `#17`. Row `#5`'s cause found in Sticky It!'s tooltip handler (fixed by Creative Labels). Corner tests renamed from
  the brief's KC-01 to KC-04 to KT-09 to KT-12.
