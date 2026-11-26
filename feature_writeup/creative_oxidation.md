<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Creative Oxidation (Creative Engineering member `creativeoxidation`)

Written 2026-11-25 (plan [plan_creative_oxidation.md](plan_creative_oxidation.md), phases P0-P8b built; report on the
upstream mod: [create_oxidized.md](create_oxidized.md)). Member of the Creative Engineering bundle
([creative_engineering.md](creative_engineering.md)): project `:creativeOxidation`, directory
`development/creativeEngineering/members/creativeoxidation`, nested jar `creativeoxidation-1.0.0-ce-19.2-04`, package
`com.SaintAlfonzo.creativeoxidation`. Port of Create: Oxidized `createoxidized` 1.0.0 (MIT, Mr.Mulle; notice
`NOTICE-upstream-MIT.txt` in the jar), our files GPL-3.0-only. The old `create_oxidized` 0.1.1 jar is gone
(`jar_archive/displaced/`). Test prefix `OXD-`. Status words, jars and install state: [README.md](README.md) and the
generated block below.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=659d3eb4c214 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 6 open TODO rows (Planned 1, Testing 5), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #32` | Planned | S C | Creative Oxidation P5: bounded quad cache and stage kept across Create block swaps (B3, B4) after live checks |
| `mod-integration #28` | Testing | S C P | Creative Oxidation P0-P2: Creative Engineering bundle member creativeoxidation, port of createoxidized core, properties only on the 45 target ids, random ticks fall through |
| `mod-integration #31` | Testing | S C | Creative Oxidation P3-P4: staged item drops (keepStageOnBreak), names from stage, honeycomb, axe and steel wool scraping, lightning, Supplementaries soap |
| `mod-integration #33` | Testing | C | Creative Oxidation P6: oxidized art for 32 Create and Create Deco blocks plus Design n' Decor copper blocks (copied dndecor art) |
| `mod-integration #34` | Testing | S P | Creative Oxidation P7: spout and fan aging for staged blocks, old create_oxidized spout recipes rebuilt and jar removed, copper splashing port moved in, iron rusting by spout and fan |
| `mod-integration #35` | Testing | C | Creative Oxidation P8: JEI Waxing and Scraping, Lightning Conversion and Soap Washing rows for staged blocks; subtype interpreter so all 7 variants per block are listed |
<!-- generated:status_index end open_work -->

| Part | Where | Status |
|---|---|---|
| Target blocks (34) and items (32), properties only on those ids | section 1 | built |
| Aging, wax, scrape, lightning, soap, item stage | section 2 | built |
| Recipes (321, in `aging.js`) | section 3 | built |
| JEI: subtype interpreter, world rows, Oxidized Copper family | section 4 | built, needs game to regenerate |
| WTHIT stage line (R14, plan P8c) | plan section 4 | not built |
| Quad cache bound and stage kept across Create block swaps (P5, B3, B4) | section 7 | planned after live checks |
| Tests OXD-01 to OXD-15 | section 6 | queue rows in [rows/OXD.qb](testing/rows/OXD.qb) |

## 1. What it adds

No new ids. Two blockstate properties, `creativeoxidation_stage` (unaffected, exposed, weathered, oxidized) and
`creativeoxidation_waxed`, exist only on the exact target block ids (upstream put them on 48 classes including vanilla
iron bars and doors; that is fixed, B1). A block that is not a target carries no property and keeps its own random
ticking (B2). Item variants are NBT on the existing items (`CreativeOxidationStage`, `CreativeOxidationWaxed`); a plain
item has neither tag.

**Targets: 34 blocks, 32 items.** Create (18): copper casing, fluid pipe, encased fluid pipe, glass fluid pipe,
mechanical pump, smart fluid pipe, fluid valve, copper valve handle, fluid tank, hose pulley, item drain, spout,
portable fluid interface, steam engine, copper ladder, copper bars, copper scaffolding, copper door. Create Deco (14):
the four colored copper lamps, copper bars, bars overlay, catwalk, catwalk stairs, coin stack (block
`copper_coinstack_block`, item `copper_coinstack`), hull, mesh fence, sheet metal, support, locked copper door. Design
n' Decor (2): copper light and copper large chain. Glass and encased fluid pipes have no item of their own (they drop
the fluid pipe item), so 32 items. The plan counted 45 targets (32 plus 13 Design n' Decor); only two Design n' Decor
blocks have upstream art with a matching layout, the other 11 left the list (plan section 6, P6-P7 note). Open owner
item: confirm this cut of ruling R4 (section 7).

**Config** `config/creativeoxidation-common.toml`: `oxidation.enabled` (true), `oxidation.speedFactor` (1.0, 0 to 100),
`keepStageOnBreak` (true; false drops everything plain; replaces upstream's `clutterReducer3000`).

## 2. Interactions

| Interaction | Result |
|---|---|
| Random tick | vanilla copper chance and neighbor rule scaled by `speedFactor`; a younger neighbor blocks aging; waxed never ages |
| Honeycomb | waxes (one honeycomb, not in creative) |
| Axe (any item with the axe scrape action) | wax off first, then one stage back, 1 durability |
| Immersive Weathering steel wool | same as an axe (R6) |
| Lightning on a rod or copper | anchor block reset to unaffected, 3 to 5 walks of 1 to 8 steps, one stage each |
| Supplementaries soap | removes wax and all oxidation (R9); hook is HEAD of `tryWashWithInterface`, gated to Supplementaries 1.19.2-2.4.20 |
| Spout (250 mB water) and fan washing | one stage per use, waxed not accepted (R2) |
| Break | drop keeps stage and wax when `keepStageOnBreak`; placing restores it, both door halves, coin stack through the place event |
| Name | computed from the stage on display, no stored name; an anvil rename shows (R3) |
| Crafting and machines | staged and waxed items count as the plain item (R15); only the member's own aging recipes match the stage (R10) |

Stage and wax carry across contraptions and schematics (OXD-11). Not kept yet: Create block swaps (pipe encase, glass,
wrench, tank to boiler; B4, planned P5). Adding a coin to a coin stack resets the stack to the held coin's stage.

## 3. Recipes (321, `kubejs/data/creativeengineering/kubejs/creativeoxidation/aging.js`)

| Group | Count | Notes |
|---|---|---|
| Old-jar spout recipes | 30 | copper block, cut copper block, slab, stairs, Create shingles and tiles block, slab, stairs; 250 mB water, one stage |
| Copper Splashing (fan) | 33 | moved from `feature_ports.js`; ids now `creativeoxidation:createsplashing/` (were `creativecompat:`) |
| Staged blocks | 192 | 32 items x 3 stages x spout and fan; ids `staged/<namespace>/<path>_<stage>`; ingredient type `creativeoxidation:staged` {item, stage, waxed}; unaffected matches only items without a stage tag |
| Iron rusting | 66 | IW chain base, exposed, weathered, rusted for cut iron (and slab, stairs, Quark vertical slab), plate iron (same), vanilla iron bars, iron door, iron trapdoor; 33 x spout and fan; vertical slabs guarded by `global.has` |

`NCH.createFilling` and `createSplashing` route raw ingredient objects and NBT results through `e.custom`
(`_createNbtRoute`), because kubejs-create takes neither.

## 4. JEI

- Subtype interpreter (`compat/jei/OxidationJEI`, JEI optional) on stage and wax for every target item, so all 7
  variants per block are listed (R16).
- World rows: `OxidationRules.transitions()` is the one source (honeycomb 4, scrape 7, lightning 3, soap 7);
  `OxidationJeiData.dump()` writes it, Creative Devtools' `WorldJeiToolPairs` reads it by reflection as section
  `creativeoxidation`; `tools/jei_world/common.py`, `tab_tools.py` (Waxing and Scraping, Soap Washing) and
  `tab_weathering.py` (Lightning Conversion) render it: 28 rows, one per state change across all items (not per block).
- Order tree: plain items keep their place; every staged or waxed variant sits in the "Oxidized Copper" family
  (`oxidized_copper`: block order, then exposed, weathered, oxidized, then waxed) through NBT-aware `nbt` tree entries
  ([jei_ordering.md](jei_ordering.md) 12.7).
- Spout and fan recipes show in Create's own JEI categories.

## 5. Art and files

186 upstream PNGs copied unchanged (3 stages x 62 sprites) under `assets/creativeoxidation/textures/`, manifest
`oxidizable_textures.txt`, 18 staged partial models for Create 0.5.1's texture keys, rows in
`texture_sources/sources.csv` (copy). `OxidationClient` adds every staged PNG to the atlas on `TextureStitchEvent.Pre`
(1.19.2 stitches only sprites a model names). Offline checks: `tools/test_creative_oxidation.sh` (88 checks),
`tools/test_nch_blueprint.sh` (17, Rhino run of `aging.js` on the real NCH).

## 6. Tests

Queue rows in [rows/OXD.qb](testing/rows/OXD.qb) (SQ-OXD-01 to -16, CQ-OXD-01 to -04); P8c row CQ-OXD-05 comes with
the WTHIT line. Fresh worlds only (P2 changed the state ids of the targets). Set `speedFactor` 100 for the aging
tests.

- [ ] **OXD-01 Aging:** place a Fluid Pipe and Copper Casing in the open, wait ~2 minutes -> both step through
  exposed, weathered, oxidized; textures change each stage. (SQ-OXD-01)
- [ ] **OXD-02 Waxing:** right-click an exposed Fluid Tank with a honeycomb -> one honeycomb used, wax particles; it
  stays exposed. (SQ-OXD-02)
- [ ] **OXD-03 Scraping:** right-click a waxed weathered Spout with an axe -> wax off, again -> exposed; the axe loses
  1 durability each time; steel wool does the same. (SQ-OXD-03)
- [ ] **OXD-04 Lightning:** lightning on a rod on top of an oxidized pipe line -> the pipe under the rod is
  unaffected, a few nearby pipes step back. (SQ-OXD-04)
- [ ] **OXD-05 Item stage:** break a waxed weathered Mechanical Pump -> drop "Waxed Weathered Mechanical Pump"; place
  it -> same stage and wax; with `keepStageOnBreak` false it drops plain. (SQ-OXD-06)
- [ ] **OXD-06 Door halves:** place an oxidized Copper Door item -> both halves oxidized. (SQ-OXD-06)
- [ ] **OXD-07 Partial models:** oxidized Steam Engine on a boiler, Fluid Valve, Copper Valve Handle, Portable Fluid
  Interface, and the two Design n' Decor blocks -> all parts show the oxidized texture. (CQ-OXD-01)
- [ ] **OXD-08 Unrelated blocks:** a glass pane, iron bars, an oak door and an Andesite Casing carry no
  `creativeoxidation_*` property (B1); a mod door that random-ticks still does (B2). (SQ-OXD-07, -08)
- [ ] **OXD-09 Quad cache:** fly over a 32x32 copper casing and tank build for 5 minutes with F3 memory -> heap
  settles (B3). (SQ-OXD-09, written with P5)
- [ ] **OXD-10 Conversions:** wrench an oxidized Fluid Pipe to glass and back, encase it, make a weathered tank into a
  boiler -> the stage is kept (B4). (SQ-OXD-10, written with P5)
- [ ] **OXD-11 Contraption and schematic:** move a weathered Fluid Tank on a contraption, print a schematic of waxed
  pipes -> stages kept. (SQ-OXD-11)
- [ ] **OXD-12 Server and client:** dedicated server plus client join -> no state-id mismatch, staged blocks render.
  (SQ-OXD-15)
- [ ] **OXD-13 Machines:** a plain pipe by spout and fan -> exposed; weathered -> oxidized; waxed and staged items not
  taken by the plain recipe; the 30 old-jar recipes and Copper Splashing work; cut iron and an iron door rust one
  stage. (SQ-OXD-12 to -14)
- [ ] **OXD-14 JEI:** Waxing and Scraping, Lightning Conversion and Soap Washing show staged rows; all 7 variants per
  block listed, plain items at their usual place, variants in Oxidized Copper. (CQ-OXD-03, -04)
- [ ] **OXD-15 Recipes use plain:** a weathered waxed Fluid Pipe and an oxidized Copper Casing in their crafting and
  Create recipes work like plain items (R15). (SQ-OXD-16)

## 7. Known limits and open items

- **Design n' Decor:** only copper light and copper large chain age (owner ruling R18, 2026-11-25); the other 11
  blocks have no upstream sprite with a matching layout and stay plain (no new art, texture rule).
- Coin stack: adding a coin resets the stack to the held coin's stage (Create Deco rebuilds the state).
- Large chain art is offset 2 px (upstream's link pattern sits 2 px lower; periodic, so it tiles).
- P5 open (TODO `mod-integration #32`): B3 bounded quad cache and B4 stage kept across Create block swaps, after the
  live checks.
- WTHIT stage line (R14, plan P8c) not built.
- Recipes built at runtime by other mods' code were not scanned for strict-NBT ingredients (R15 check covered the
  jars, `kubejs/data`, `data/` and scripts).
- Waxed Up compat dropped (mod not in the pack).

Needs the game (nothing run yet): `/dumpall` (recipe dumps still list `create_oxidized:`; display names), then
`/dumpworldjei`, then `/cdevgen jei_world` (waxing, soaping, lightning rows in `generated.json`), then a client
restart. `pack/manifest.json` regeneration is left to the owner.

## 8. History

- 2026-11-25: report and plan; P0-P2 core, P3-P4 items and interactions, P6-P7 art and recipes, P8-P8b JEI built;
  P9 docs. Per-phase detail: [plan_creative_oxidation.md](plan_creative_oxidation.md) section 6.
