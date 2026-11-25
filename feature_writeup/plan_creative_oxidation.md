<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Creative Oxidation: copper machine aging and iron rusting (plan)

<!-- generated:status_index begin open_work sha1=0135222cd403 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 7 open TODO rows (Planned 7), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `jei-ordering #9` | Planned | C | Oxidized copper family: plain Create copper items stay in place, every oxidized or waxed state in its own family (NBT-aware tree entry) |
| `mod-integration #28` | Planned | S C P | Creative Oxidation P0-P2: Creative Engineering bundle member creativeoxidation, port of createoxidized core, properties only on the 45 target ids, random ticks fall through |
| `mod-integration #31` | Planned | S C | Creative Oxidation P3-P4: staged item drops (keepStageOnBreak), names from stage, honeycomb, axe and steel wool scraping, lightning, Supplementaries soap |
| `mod-integration #32` | Planned | S C | Creative Oxidation P5: bounded quad cache and stage kept across Create block swaps (B3, B4) after live checks |
| `mod-integration #33` | Planned | C | Creative Oxidation P6: oxidized art for 32 Create and Create Deco blocks plus Design n' Decor copper blocks (copied dndecor art) |
| `mod-integration #34` | Planned | S P | Creative Oxidation P7: spout and fan aging for staged blocks, old create_oxidized spout recipes rebuilt and jar removed, copper splashing port moved in, iron rusting by spout and fan |
| `mod-integration #35` | Planned | C | Creative Oxidation P8: JEI Waxing and Scraping, Lightning Conversion and Soap Washing rows for staged blocks; subtype interpreter so all 7 variants per block are listed |
<!-- generated:status_index end open_work -->

Written 2026-11-25 from the owner's notes on [create_oxidized.md](create_oxidized.md) (the integration report of
Create: Oxidized `createoxidized` 1.0.0 for 1.20.1, MIT). Nothing built. The mod is ported as **Creative Oxidation**
(`creativeoxidation`), a member of the Creative Engineering bundle, and every oxidizing and rusting recipe and
mechanic moves into it. The old `create_oxidized` 0.1.1 jar is dropped. Test prefix `OXD-` (owner doc
create_oxidized.md until the member's writeup exists; the tests move there when it is built).

## 1. Intake

| # | Item (owner's words) | Class | Note |
|---|---|---|---|
| I1 | "break down the blocks covered and the ones not existing in this version with oxidized versions" | clear | section 2 |
| I2 | "limit the extra block states" | clear | upstream B1; P2 |
| I3 | "add the random ticks where needed" | clear | upstream B2; P2 |
| I4 | "plan live checks" | clear | section 5; rows go to `rows/OXD.qb` in the build session |
| I5 | "move copper spout recipes (drop old jar) to creative engineering" | premise | checked: old jar holds 30 `create:filling` recipes, 250 mB water, one stage each, for copper block, cut copper block/slab/stairs, Create shingles and tiles block/slab/stairs; nothing in the pack references the jar (create_oxidized.md section 2) |
| I6 | "add oxidizing recipes for all of the new blocks with states there too" | open -> R2 | routes asked |
| I7 | "extend jei waxing scraping and lightning" | premise | checked: the tabs read `/dumpworldjei` `tool_pairs` (`WorldJeiToolPairs.java`, creativeDevtools) and IW data (`tools/jei_world/tab_tools.py`, `tab_weathering.py`); rows accept NBT stacks (`jar_interaction/manual.json:15`) |
| I8 | "as well as soap mechanics" | premise | checked: Supplementaries soap takes off wax and all oxidation of vanilla copper (`WorldJeiToolPairs.java:225-255`, Soap Washing tab note); staged Create blocks are not touched today |
| I9 | "for now broken copper items retain their oxidation but this can be config toggled for the new oxidized blocks" | clear | upstream has the toggle (`clutterReducer3000`); renamed, default keeps the stage |
| I10 | (answer to Home) "submod within creative engineering, fold all oxidizing recipes and mechanics in here" | clear | includes the Create Copper Splashing port now in `kubejs/server_scripts/other/feature_ports.js:33-50` |
| I11 | (answer to Home) "add iron rusting recipes to mirror copper washing/spouting" | premise | checked: IW rust chain in the pack: base -> exposed -> weathered -> rusted, 11 shapes (section 4 P7d); no rusting recipe exists today |

## 2. Block breakdown

Upstream targets 72 block ids. In this pack: 32 present and covered, 40 absent; Design n' Decor adds 13 (R4).

### 2.1 Covered: present, with upstream oxidized art (32)

| Mod | Blocks |
|---|---|
| Create (18) | copper_casing, fluid_pipe, encased_fluid_pipe, glass_fluid_pipe, mechanical_pump, smart_fluid_pipe, fluid_valve, copper_valve_handle, fluid_tank, hose_pulley, item_drain, spout, portable_fluid_interface, steam_engine, copper_ladder, copper_bars, copper_scaffolding, copper_door |
| Create Deco 1.3.3 (14) | blue/green/red/yellow_copper_lamp, copper_bars, copper_bars_overlay, copper_catwalk, copper_catwalk_stairs, copper_coinstack, copper_hull, copper_mesh_fence, copper_sheet_metal, copper_support, locked_copper_door |

### 2.2 Added by ruling R4: Design n' Decor (13, art copied from upstream's `compat/dndecor` set)

copper_boiler, copper_boiler_large, copper_boiler_structure, copper_bolt, copper_catwalk, copper_floodlight,
copper_floor, copper_gas_tank, copper_lamp, copper_large_chain, copper_light, copper_railing, copper_screw. The
upstream dndecor art covers boilers, bolts, catwalk, frontlight, lamp, sheet metal and large chain; each Design n'
Decor block is mapped in P6, and a block with no matching sprite is left out and listed (no new art, texture rule).

### 2.3 Not in this pack (40, dropped from the port)

| Mod | Ids | Why |
|---|---|---|
| Create | copper_table_cloth | Create 6 block |
| Create Deco | copper_catwalk_railing, copper_support_wedge, copper_window, copper_window_pane | not in 1.3.3 |
| Create Diesel Generators (20) | chemical_turret, diesel_engine, distillation_tank, pumpjack_hole, 16 concrete-encased pipes | mod not installed |
| Create Connected | fluid_vessel | mod not installed |
| Dreams and Desires Decor (10) | dndecor:* | 1.20 mod; its 1.19.2 forerunner is Design n' Decor (2.2) |
| Dreams and Desires (4) | fluid_gauge, fluid_hatch, hydraulic_press, spud_sentry | 1.20 namespace; the pack's `create_dd` 1.2c has no copper machines |

### 2.4 Copper blocks in the pack with no oxidized art (not covered)

Listed so nothing is assumed covered. No art means no oxidation (texture rule: never drawn or generated).

| Mod | Blocks |
|---|---|
| Create | copper_backtank (placeable); shingles and tiles already have vanilla-style stages |
| Create Deco | copper_door, copper_trapdoor, copper_sheet_slab, copper_sheet_slab_vert, copper_sheet_stairs, copper_coinstack_block |
| Create Crystal Clear | copper_glass_casing, copper_clear_glass_casing |
| Create: Things and Misc | copper_casing_trapdoor |
| Steam 'n' Rails | 40 copper-wrapped locometal and smokestack blocks |
| Bells and Whistles | copper_bogie_steps, copper_door_step, copper_grab_rails, copper_pilot |
| Dramatic Doors | short/tall copper casing, cd and locked copper doors (everythingcopper's doors already have stages) |
| Others | create_compressed copper_sheet_block, sootychimneys copper chimneys, securitycraft, immersiveposts, metalfences, Lightman's Currency |

## 3. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| R1 | Home of the port | open | Submod (bundle member) of Creative Engineering; all oxidizing and rusting recipes and mechanics live in it; iron rusting recipes added to mirror copper | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R2 | Routes for the staged blocks (I6) | open | Spout (250 mB water) and fan splashing, one stage each; waxed items never age | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R3 | Item names | open | Name computed from the stage tag on display; no stored custom name | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R4 | Design n' Decor copper blocks | open | In the first build (art copied from upstream's dndecor set to Design n' Decor paths) | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R5 | Member mod id | open | `creativeoxidation` ("Creative Oxidation") | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R6 | Steel wool on staged blocks | open | Scrapes like an axe (wax off, then one stage back) | 2026-11-25 | AskUserQuestion 2026-11-25 |
| R7 | Old `create_oxidized` jar (report D1, D2) | clear | Dropped; its 30 spout recipes rebuilt in the member | 2026-11-25 | chat 2026-11-25 |
| R8 | Broken staged blocks (report D4) | clear | Keep their stage by default; config toggle drops them plain | 2026-11-25 | chat 2026-11-25 |
| R9 | Soap (report D5) | clear | Soap takes off wax and all oxidation, as on vanilla copper | 2026-11-25 | chat 2026-11-25 |
| R10 | Unaffected input matching | clear | agent default, unconfirmed: a member ingredient type `creativeoxidation:staged` {item, stage, waxed}; "unaffected" matches only items with no stage tag, so a plain-item recipe never accepts a staged item | 2026-11-25 | agent |
| R11 | State limiting method | clear | agent default, unconfirmed: properties only on the exact target ids, read from Registrate's block builder name (P2) | 2026-11-25 | agent |
| R12 | Config option name | clear | agent default, unconfirmed: `keepStageOnBreak` (true) in `creativeoxidation-common.toml`, replacing `clutterReducer3000` (report D7) | 2026-11-25 | agent |
| R13 | Staged variants in JEI's item list | conflict | Owner 2026-11-25 overrides the agent default (hidden): every added variant is viewable in JEI, see R16 | 2026-11-25 | chat 2026-11-25 |
| R14 | WTHIT stage line (report D8) | open | not asked yet; not in this plan | | |
| R15 | "waxed and oxidized components can be used in crafting recipes just the same" | premise | Staged and waxed items count as the plain item in every crafting and machine recipe that uses it. Checked: plain item ingredients ignore NBT, so this holds by default; the only exceptions are strict-NBT ingredients (`forge:nbt`) and the member's own aging recipes (R10), which match the stage on purpose | 2026-11-25 | chat 2026-11-25 |
| R16 | "all oxidized added should be viewable in jei" | premise | All 7 variants per block (exposed, weathered, oxidized, and waxed of each stage plus waxed unaffected) are listed in JEI. Checked: JEI merges stacks that differ only in NBT unless a subtype interpreter is registered (jei_ordering.md:235); the member registers one on the stage and wax tags for the 45 items | 2026-11-25 | chat 2026-11-25 |
| R17 | "jei families: unoxidized unwaxed items sit where that block logically sits, and all oxidized states sit in another family" | premise | The plain item stays at its place in the JEI order tree; every staged or waxed variant goes to a separate family (working name "Oxidized Copper Machines", ordered by block, then stage, then waxed). Checked: the order tree places by item id and NBT variants follow their item (jei_ordering.md:235, 411), so the tree definition needs an NBT-aware entry (item id + stage predicate) | 2026-11-25 | chat 2026-11-25 |

## 4. Phases (build order)

Code owner: one `impl` session at a time. Each phase ends with `python3 tools/check_all.py --fast --changed`, a
commit, and its TODO row logged.

- **P0 Bundle.** Creative Engineering switches from `ce.forge-mod` to `ce.forge-bundle` with member
  `development/creativeEngineering/members/creativeoxidation` (layout as Creative Brewing's members). Build and install
  through `:creativeEngineering:` only. Upstream MIT notice in the member (`LICENSE-upstream-createoxidized.txt`),
  our files GPL-3.0-only. `mod_sides.md` row (both), `IN_TREE_MODS` unchanged (the bundle is listed). Check the
  bundle's existing jar consumers (`creativeengineering` mod id unchanged).
- **P1 Port the core** (skill `port`). Forge 43 / Create 0.5.1 / Flywheel 0.6.11: `Registry.BLOCK` for
  `BuiltInRegistries`; `ModelEvent.BakingCompleted` for `ModifyBakingResult`; `CreativeModeTab` subclass; Flywheel
  `FluidValveInstance` and the valve handle's instance for the two `*Visual` mixins; refmap. Namespace
  `creativeoxidation` for the properties (`creativeoxidation_stage`, `creativeoxidation_waxed`), the NBT keys, the
  tag `creativeoxidation:oxidizable_blocks`, lang, config. Target list = section 2.1 + 2.2 (45 ids).
- **P2 States and ticks (I2, I3; B1, B2).**
  - Properties only on the 45 ids. A mixin on Registrate's `BlockBuilder` sets a thread-local block id around the
    factory call; `StateDefinitionBuilderMixin` adds the properties only when that id is a target. Create, Create
    Deco and Design n' Decor all register through Registrate (checked 2026-11-25). The class list goes. Vanilla
    `IronBarsBlock` / `DoorBlock`, all panes, doors, casings, windows and pillars keep their own state count.
  - Random ticks: `isRandomlyTicking` returns `true` for an aging state (target, not waxed, not oxidized) and
    otherwise falls through to the block's own answer (upstream replaced it).
  - Harness test: an `IronBarsBlock`, a `DoorBlock` and `create:andesite_casing` carry no property; `create:fluid_pipe`
    and `createdeco:copper_bars` do.
- **P3 Items (I9, R3, R8, R15).** Staged and waxed items work as the plain item in every crafting and machine recipe (no recipe changes; a check over the recipe dump lists any strict-NBT ingredient that names a target item). Drops keep stage and wax in NBT when `keepStageOnBreak` is on; off drops plain; the
  variants stay listed in JEI and the creative tab (spout and fan still make them, R16). Name from the stage on display ("Waxed Weathered Fluid Pipe"); no `display.Name` written; an
  anvil rename shows the rename. Placing restores the stage (both door halves).
- **P4 Interactions (R6, R9).** Honeycomb wax; axe and IW steel wool scrape (wax first, then one stage); lightning
  clean as vanilla; Supplementaries soap: wax and all stages off in one use (mixin on `SoapWashableHelper.tryWash`,
  optional, only with Supplementaries). Waxed Up support dropped (mod not in the pack).
- **P5 Fixes needing a live look (B3, B4).** Quad caches keyed by sprite and UVs, bounded; Create's block swaps
  (pipe <-> glass pipe <-> encased pipe, casing applied, tank -> boiler) carry the two properties. Built after
  SQ-OXD-09 and -10 confirm the upstream behavior.
- **P6 Client and art (R4).** Copy the upstream PNGs for the 32 blocks (paths unchanged under
  `assets/creativeoxidation/textures/block/<stage>/`) and map the dndecor sprites to Design n' Decor's texture paths
  (copy, no edit). Every mapped copy gets a row in `feature_writeup/texture_sources/sources.csv` (tool: copy), then
  `python3 tools/texture_provenance.py --write`. Manifest of oxidizable sprites rebuilt for the pack's ids.
- **P7 Recipes** (member server scripts, through `global.NCH`; ids under `creativeoxidation`):
  - a. The 30 old-jar spout recipes rebuilt (250 mB water, one stage each). Remove the jar (skill `add-mod`, remove
    path: `mod_sides.md`, `project_pages/pack_art.md`, dumps).
  - b. The Create Copper Splashing port moved from `feature_ports.js:33-50` into the member, recipes unchanged.
  - c. Staged blocks (45): spout and fan splashing, unaffected -> exposed -> weathered -> oxidized, waxed excluded:
    270 recipes. Needs the R10 ingredient and NBT outputs; extend `NCH.createFilling` and the splashing helper to
    take them (CLAUDE.md: extend the helper).
  - d. Iron rusting (I11): IW chain base -> exposed -> weathered -> rusted for cut_iron, cut_iron_slab,
    cut_iron_stairs, cut_iron_vertical_slab (Quark), plate_iron, plate_iron_slab, plate_iron_stairs,
    plate_iron_vertical_slab (Quark), and vanilla iron_bars, iron_door, iron_trapdoor: spout 250 mB water and fan
    splashing, one stage each, 33 x 2 = 66 recipes (vertical slabs guarded by `global.has`). Ids in `item_dict.js`.
- **P8 JEI (I7, I8, R13, R16, R17).** Waxing and Scraping: honeycomb, axe and steel wool rows per staged block (NBT
  stacks); Lightning Conversion: staged -> unaffected row per block; Soap Washing: staged or waxed -> plain row per
  block. Source: `WorldJeiToolPairs` gains a `creativeoxidation` section, read by `tab_tools.py` / `tab_weathering.py`;
  `/dumpworldjei` and `/cdevgen` regenerate. Spout and fan recipes show through Create's own JEI categories. JEI subtype
  interpreter on the stage and wax tags so all 7 variants per block are listed (R16).
- **P8b JEI families (R17; [jei_ordering.md](jei_ordering.md)).** Plain items keep their place in the order tree;
  every staged or waxed variant sits in a new family (working name "Oxidized Copper Machines": by block in the tree's
  order, then exposed, weathered, oxidized, then the waxed set). Needs the tree to accept an NBT-aware entry (item id
  plus a stage predicate), today it places by item id only; built in Creative Compat's ordering helper, family added
  to the tree.
- **P9 Docs and release.** Member writeup `creative_oxidation.md` (tests move from create_oxidized.md),
  [creative_engineering.md](creative_engineering.md) member line, `features.csv`, changelog family `## Copper and Iron
  Aging` (Create copper machines age; spout and fan aging; iron rusting by spout and fan; old jar removed),
  `python3 tools/changelog_lint.py`.

Order: P0 -> P1 -> P2 (before any live test: P2 changes state ids, so test on fresh worlds only) -> P3, P4 -> P6 ->
P7 (P7c needs P1's ingredient type, P7a with the jar removal in one install) -> P8 (after install and
`/dumpall` / `/dumpworldjei`) -> P5 after the live checks -> P9.

## 5. Live checks (planned queue rows `SQ-OXD-` / `CQ-OXD-`, written to `rows/OXD.qb` in the build session)

| Row | Checks | Phase |
|---|---|---|
| SQ-OXD-01 | aging at `speedFactor` 100: pipe and casing step through all stages (OXD-01) | P1 |
| SQ-OXD-02 | honeycomb wax; waxed block does not age (OXD-02) | P4 |
| SQ-OXD-03 | axe and steel wool scrape: wax off, then one stage; durability (OXD-03) | P4 |
| SQ-OXD-04 | lightning on a rod over oxidized pipes (OXD-04) | P4 |
| SQ-OXD-05 | soap on waxed oxidized tank -> plain, one soap | P4 |
| SQ-OXD-06 | break with `keepStageOnBreak` on and off; place restores; door halves (OXD-05, OXD-06) | P3 |
| SQ-OXD-07 | state counts: iron bars, glass pane, oak door, andesite casing carry no property (OXD-08) | P2 |
| SQ-OXD-08 | random ticks: an aging pipe ticks, a waxed one and a non-target do not change; a ticking modded door still ticks | P2 |
| SQ-OXD-09 | quad cache: heap over a copper casing and tank build (OXD-09) | P5 |
| SQ-OXD-10 | conversions keep stage: wrench pipe, encase, boiler (OXD-10) | P5 |
| SQ-OXD-11 | contraption and schematic keep stages (OXD-11) | P1 |
| SQ-OXD-12 | spout and fan: plain pipe -> exposed; weathered -> oxidized; waxed not accepted; a staged item not taken by the plain recipe | P7 |
| SQ-OXD-13 | old-jar recipes rebuilt: copper block, cut copper, shingles, tiles by spout | P7 |
| SQ-OXD-14 | iron rusting: cut iron and iron door by spout and fan, one stage each | P7 |
| CQ-OXD-01 | staged textures and partial models render (OXD-07), Design n' Decor blocks included | P6 |
| CQ-OXD-02 | names: "Waxed Weathered Fluid Pipe"; anvil rename shows the rename | P3 |
| CQ-OXD-03 | JEI: Waxing and Scraping, Lightning Conversion, Soap Washing rows for staged blocks; variants hidden in the item list | P8 |
| SQ-OXD-15 | dedicated server and client join, no state mismatch (OXD-12) | P1 |
| SQ-OXD-16 | a weathered waxed Fluid Pipe and an oxidized Copper Casing used in their crafting and Create recipes (encased pipe, mechanical crafting) work like plain items (R15) | P3 |
| CQ-OXD-04 | JEI lists all 7 variants per staged block; plain items at their usual place, variants in the oxidized family in block, stage, waxed order (R16, R17) | P8b |

## 6. Phase notes

(empty until building starts)

## 7. History

- 2026-11-25: plan written from the owner's notes on create_oxidized.md; rulings R1 to R9 asked and recorded,
  R10 to R13 agent defaults, R14 open. Nothing built.
- 2026-11-25: owner notes added R15 (staged items craft as plain), R16 (all variants viewable in JEI; overrides the
  R13 default) and R17 (JEI family for oxidized states), phase P8b, rows SQ-OXD-16, CQ-OXD-04.
