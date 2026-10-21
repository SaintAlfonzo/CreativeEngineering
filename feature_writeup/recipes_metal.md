# Metal recipes: one material per metal, ore processing, plates, rods, wires, bars

Part of backlog row 20 (recipe progression), split per folder. This one covers `kubejs/server_scripts/metal/` and the
metal half of [tags/metaltags.js](../kubejs/server_scripts/tags/metaltags.js). Wire coils and coil blocks (the end of
`metal_base_materials.js`) are in [engineering.md](engineering.md) 2.4; steel and compressed iron in
[recipes_engineering.md](recipes_engineering.md) 2.1.

## 0. At a glance

| Part | Status |
|---|---|
| Unification: IE's nuggets, plates, rods and wires are the pack's parts; Create / Everything Copper / Create Compressed / Create Crafts & Additions duplicates swapped out of recipes | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3) |
| Ore processing for 9 metals: ore and raw ore → crushed ore → dust → ingot, Create and IE routes with set yields | DEPLOYED, complete for all 9 |
| Dust, plate and crushed-ore storage blocks; block smelting | DEPLOYED (copper plate block one-way, F2) |
| Plates, rods, wires: Create rolling and IE press standard across the metals; plates only partly (F1) | DEPLOYED, plates uneven |
| Bars (Create Deco, Quark, Everything Copper): laser cutting ×64, assembly ×12, crafting ×9 from rods | DEPLOYED |
| Misc: red electrum, rust washing, gilded blackstone, spools, chains, steel wool, Dustrial Decor billets | DEPLOYED |

- **Pending:** nothing built and unshipped. RM-01 to RM-14 have never been run. Findings F1 to F7 (section 4); F1 and F4
  need your call.
- **Files to open:** [metal_base_materials.js](../kubejs/server_scripts/metal/metal_base_materials.js) (the header
  comment lines 3-41 is an earlier audit of the gaps, still accurate), [metal_building.js](../kubejs/server_scripts/metal/metal_building.js),
  [misc_metal_crafts.js](../kubejs/server_scripts/metal/misc_metal_crafts.js).
- **Knobs:** none. Server scripts (`/reload`).

## 1. Overview

Create, IE, Create Crafts & Additions, Vintage Improvements, Everything Copper and Create Compressed each add their own
nuggets, sheets, rods and wires, and their own ore recipes, with different yields. The pack picks one item per part
(mostly IE's) and one table of yields, and removes or redirects the rest, so every machine tree processes ore the same
way and gives a better yield the further up it goes.

## 2. Reference

### 2.1 Metals

The 9 ore metals: copper, iron, gold, zinc, aluminium, lead, silver, nickel, uranium. Alloys (no ore): brass, steel,
constantan, electrum, cast iron, rose gold; they get dust blocks and coils, not the ore chain.

### 2.2 Ore processing (per metal, all 9)

| Step | Create | IE |
|---|---|---|
| Ore → crushed ore | crushing wheels: 1 + 50 % (stone ore), 2 + 25 % (deepslate ore), 75 % XP nugget, 12 % cobblestone | crusher: 2 per ore |
| Raw ore → crushed ore | Create's own recipe (kept) | 1 + 33 %; raw block → 12 |
| Crushed ore → dust | | crusher: 1 + 33 %; crushed block → 12 |
| Crushed ore → ingot | smelt (1, Create's) | arc furnace: 1 + 50 %; crushed block → 13 + 50 % |
| Dust → ingot | | IE's own (smelting, arc furnace) |
| Dust / crushed block → storage block | smelt or blast | |

The design yields (comment in `resource_blocks.js`): smelting crushed ore 1×, IE crusher 1.33×, arc furnace 1.5×. Create
and IE's original ore crushing, IE's hand-hammer ore crushing and the IE raw-ore crushing are removed for every metal
(zinc's hammer crushing is not, F3). Gold ingots crush back to gold dust.

### 2.3 Parts

| Part | Item | Recipes |
|---|---|---|
| Nugget | IE (copper: IE's, replacing Create's and Everything Copper's in every recipe) | vanilla / IE 9 ↔ 1 |
| Plate | IE plate (Create's copper, iron and gold sheets replaced everywhere) | see F1: Create pressing for copper, iron, gold, zinc; IE press for all (8 by IE's own recipe, gold by the pack's); hammer for all (7 by IE's own, gold and zinc by the pack's) |
| Plate block | Create Compressed sheet blocks | 9 plates ↔ 1 block (copper one-way, F2) |
| Rod | IE rods (and the pack's) | Create rolling (Create Crafts & Additions) ×4 and IE press ×4 for all 9; crafting: zinc 2 ingots → 4, iron and aluminium by IE's own (F4) |
| Wire | IE wires | wire cutter + plate → 2; rolling ×4; IE press ×4; 8 metals (no uranium wire) |
| Dust block | the pack's dust piles (`creativeengineering:<metal>_dust_pile`) | 9 dust ↔ 1 for the 9 metals, the 6 alloys and diamond; crushed ore blocks are Create Compressed's piles |
| Spool | the pack's (`creativeengineering:spool`) | 2 iron plates + iron nugget → 8; assembly ×12; blueprint ×16. Create Crafts & Additions' copper spool is replaced by the copper wire coil in that mod's recipes |

`metaltags.js` fills the forge tags the dict's `any_*` keys read: rods, wires and the zinc plate under
`forge:<type>/<metal>` and `forge:<type>/all_metal`; rose gold rods, nuggets and block under gold (F5); IE steel as
`forge:ingots/compressed_iron`, the PNC block as `forge:storage_blocks/steel`, the PNC gear as `forge:gears/steel`;
`aluminium` spellings; Vintage Improvements springs as `forge:springs/<metal>`, `forge:springs_stiff` / `_weak`.

### 2.4 Bars (`metal_building.js`)

| Bars | Laser cut a block → 64 | Assembly on a rod → 12 (5 loops) | 6 rods → 9 | Variants |
|---|---|---|---|---|
| Copper | yes | yes | yes | ornate ×3, raw ×5, and back |
| Iron, cast iron, zinc | yes | yes | yes | |
| Gold, brass | yes | yes | yes | ornate ×3 and back |
| Andesite | yes | | | ornate ×3 and back |
| Netherite | yes | | | |
| Framed bars (×8 from a framed cube) | yes | | | |

The mods' stonecutter bar recipes (Create, Create Deco), Quark's gold bars and Everything Copper's copper bars are
removed.

### 2.5 Misc (`misc_metal_crafts.js`)

- **Red electrum** (Project Red's red alloy ingot, `projectred_core:red_ingot`): electrum + 8 redstone shapeless, 4 heated mixing, 3 arc
  furnace. Project Red's red iron compound removed.
- **Rust:** Dustrial Decor's rusty iron and rusty sheet metal recipes all removed; Create washing turns every rusty block
  back into the clean one (11 pairs).
- **Billets:** Dustrial Decor's industrial and cast iron billets removed; its recipes take the industrial iron block and
  a cast iron ingot instead.
- **Gilded blackstone:** deploy a gold block on blackstone; IE crusher → 18 gold nuggets.
- **Chain** ×4 by assembly from an iron ingot (nugget, ingot, nugget). **Steel wool** from 6 steel wire, or 4 by mixing.
- Wind vane takes an iron rod instead of redstone; Supplementaries Squared's metal frame an iron rod instead of nuggets.

## 3. What the pack changes

All through `global.NCH`, largely with `R.bulk(helper, rows)`: `remove`, `replaceInput` / `replaceOutput` by mod or by
input (the Create sheets and nuggets are swapped out of every recipe that uses them), then the processing tables.
`COIL_METALS` drives the coil recipes. Tags through `global.tags` (`t.add`, `t.addTag`, which also adds the parent).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. Plates are made three different ways per metal. Your call.** Create pressing: copper, iron, gold, zinc only;
  aluminium, lead, silver, nickel and uranium have no Create plate recipe (Create Crafts & Additions' and Vintage
  Improvements' rolling are removed, nothing replaces them). IE press: gold by the pack, the other 8 by IE's own recipes.
  Hammer: gold and zinc by the pack, 7 by IE's own. The header audit in the file asks the same question: keep the IE
  press for the 8 and add Create pressing for the missing 5, or move every metal onto the pack's recipes.
- **F2.** FIXED 2026-10-19 ([plan_1.0.9.md](plan_1.0.9.md)). **Copper plate block is one-way:** block → 9 plates exists, 9 plates → block doesn't (Create Compressed's recipe
  covers it today).
- **F3.** FIXED 2026-10-19 ([plan_1.0.9.md](plan_1.0.9.md)). **Zinc can still be hammer-crushed:** IE's `hammercrushing_zinc` and `raw_hammercrushing_zinc` aren't removed
  (every other metal's are).
- **F4. Rods by hand only for zinc, iron and aluminium. Your call.** Zinc 2 ingots → 4 by the pack; iron and aluminium by
  IE's own (2 ingots → 1 rod, per the header audit); copper, gold, lead, silver, nickel, uranium have no crafting-table rod. Rolling
  and the IE press cover all 9.
- **F5. Rose gold counts as gold in tags** (`forge:rods/gold`, `nuggets/gold`, `storage_blocks/gold`). Any recipe asking
  for `#forge:rods/gold` (powered rails, for instance) takes rose gold. Probably intended for rods; the block tag means
  any recipe on `#forge:storage_blocks/gold` (IE's storage crusher recipe, beacon bases) also takes it. Check with RM-11.
- **F6. Dust → ingot recycling is the pack's for zinc only;** the other 8 use IE's own arc and smelting recipes. Works,
  but uneven.
- **F7.** PARTLY FIXED 2026-10-19: the two `EVENT:` lines are gone; the rest stays ([plan_1.0.9.md](plan_1.0.9.md)). **Leftovers:** `console.log('EVENT:', e)` in both base files (prints the event object every reload);
  `misc_metal_crafts.js` logs "The metal base event has started!"; the header audit is written as a chat reply ("Want
  me to fill these in?"); the tall rusty iron door washing row says "Broken? Item still craftable".

### Known limits

- **Raw storage blocks** smelt to a storage block for zinc only (Quark's flag); other raw blocks go through crushing.
- **Uranium has no wire,** on purpose (`i.wire_uranium = i.none`).

### Rejected options

- **Keeping each mod's own parts:** a recipe asking for an IE plate wouldn't take a Create sheet; one item per part
  keeps every recipe working with one stock.

## 5. File map

- [server_scripts/metal/](../kubejs/server_scripts/metal/): `metal_base_materials.js` (762 lines: unification, ore
  tables, parts, coils), `metal_building.js` (bars), `misc_metal_crafts.js`.
- Tags: [tags/metaltags.js](../kubejs/server_scripts/tags/metaltags.js).
- Ore stones (Create's veridium etc.) are in [recipes_engineering.md](recipes_engineering.md) 2.5.

## 6. Offline checks

None.

## 7. In-game test suite

- [ ] **RM-01 Log:** `/reload` → no validation errors from the metal scripts.
- [ ] **RM-02 Copper unified:** JEI copper nugget → one item (IE's); a Create recipe that used Create's copper sheet shows
  the IE copper plate.
- [ ] **RM-03 Crushing wheels:** 10 iron ore → about 15 crushed iron, about 7 XP nuggets; 10 deepslate iron ore → about
  22.
- [ ] **RM-04 IE ore chain:** IE crusher on raw iron → about 1.33 crushed; on crushed iron → about 1.33 dust; arc
  furnace on crushed iron → about 1.5 ingots.
- [ ] **RM-05 Crushed block:** crushed iron block in the arc furnace → 13 ingots (+50 % one more).
- [ ] **RM-06 Dust blocks:** 9 copper dust ↔ dust block; smelt the dust block → copper block.
- [ ] **RM-07 Plates (F1):** JEI aluminium plate → note which machines make it (expect IE press and hammer, no Create
  press).
- [ ] **RM-08 Copper plate block (F2):** 9 copper plates in the grid → note result.
- [ ] **RM-09 Rods:** rolling mill with a lead ingot → 4 lead rods; IE press with the rod mold → 4.
- [ ] **RM-10 Wires:** wire cutter + copper plate → 2 copper wire, cutter damaged not used up.
- [ ] **RM-11 Rose gold (F5):** powered rail with rose gold rods → note whether it crafts.
- [ ] **RM-12 Bars:** laser cut an iron block → 64 iron bars; 6 iron rods → 9.
- [ ] **RM-13 Rust:** wash a rusty iron block with a fan through water → iron block.
- [ ] **RM-14 Red electrum:** heated mixer, electrum + 4 redstone → red electrum ingot.

## 8. History

- By 2026-09-27 (initial git commit): unification, ore tables, parts, coils, bars and misc crafts; shipped with 1.0.3.
  The folder hasn't changed in git since.
- 2026-10-18: this writeup; findings F1 to F7; tests RM-01 to RM-14. No code changed.
- 2026-10-19: F2 (9 copper plates -> plate block), F3 (IE zinc hammer-crushing removed) and the `EVENT:` log lines of F7 fixed ([plan_1.0.9.md](plan_1.0.9.md)). Header audit items 4 and 5 marked done.
