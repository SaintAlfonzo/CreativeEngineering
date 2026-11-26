# Create: Oxidized (createoxidized): overlap and integration report

Written 2026-11-25; investigation only, nothing built. Jar: `DecompiledMods/createoxidized-forge-1.20.1-1.0.0.jar`
(Forge 1.20.1, Create 6.0), MIT license, author Mr.Mulle. A port to Forge 1.19.2 / Create 0.5.1 is needed (MIT:
fork allowed, route `development/backported<Name>`, skill `port`). Short name in this file: **Oxidized**. Not to be
confused with the installed **Create: Oxidized 0.1.1** (`create_oxidized`, Pyzpre, MIT), a recipe-only jar with the
same display name; this file calls it **old Oxidized**. Test prefix `OXD-`.

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks by name; upstream bugs are fixed.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=40c8a338fb68 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 6 open TODO rows (Planned 5, Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `mod-integration #31` | Planned | S C | Creative Oxidation P3-P4: staged item drops (keepStageOnBreak), names from stage, honeycomb, axe and steel wool scraping, lightning, Supplementaries soap |
| `mod-integration #32` | Planned | S C | Creative Oxidation P5: bounded quad cache and stage kept across Create block swaps (B3, B4) after live checks |
| `mod-integration #33` | Planned | C | Creative Oxidation P6: oxidized art for 32 Create and Create Deco blocks plus Design n' Decor copper blocks (copied dndecor art) |
| `mod-integration #34` | Planned | S P | Creative Oxidation P7: spout and fan aging for staged blocks, old create_oxidized spout recipes rebuilt and jar removed, copper splashing port moved in, iron rusting by spout and fan |
| `mod-integration #35` | Planned | C | Creative Oxidation P8: JEI Waxing and Scraping, Lightning Conversion and Soap Washing rows for staged blocks; subtype interpreter so all 7 variants per block are listed |
| `mod-integration #28` | Testing | S C P | Creative Oxidation P0-P2: Creative Engineering bundle member creativeoxidation, port of createoxidized core, properties only on the 45 target ids, random ticks fall through |
<!-- generated:status_index end open_work -->

| Part | Where | Status |
|---|---|---|
| Inventory: 0 items, 0 blocks, 2 blockstate properties on 48 block classes, 72 target ids (32 present in the pack), 1 block tag, 312 textures, 21 partial models, 1 config | section 1, 3 | read |
| Upstream bugs B1 to B4 | section 3.5 | found in code, B3 needs a live check |
| Overlap: old Oxidized, Create Copper Splashing port, JEI world tabs, Design n' Decor, everythingcopper, Soap | section 2 | 7 rows |
| Fold-in | section 4 | 14 steps |
| Decisions | section 5 | D1 to D8 open |
| Tests | section 8 | OXD-01 to OXD-12 drafted |

- Jar: `createoxidized-forge-1.20.1-1.0.0.jar` (a jar, not a decompile; decompiled to the session scratchpad with
  `tools/decompile_mod.py`, 24 classes, 1,619 lines).
- License: MIT (`mods.toml`). Fork allowed; our files GPL-3.0-only, upstream notice kept.
- Dependencies: Forge 47, Minecraft 1.20.1, Create `[6.0.0,6.1.0)`; soft compat (no hard dep) with Create Deco,
  Create Diesel Generators, Create Connected, Dreams and Desires Decor (`dndecor`), Dreams and Desires (`dndesires`),
  Waxed Up (`waxedup`).
- Pack references to `createoxidized`: none. Old Oxidized appears in [mod_sides.md](../mod_sides.md),
  [jei_coverage.md](jei_coverage.md) (generic waxing row), [project_pages/pack_art.md](project_pages/pack_art.md).
- Already live in the pack: fan splashing weathers vanilla and Create copper blocks
  (`kubejs/server_scripts/other/feature_ports.js`, Create Copper Splashing port); old Oxidized spout recipes.

## 1. Overview

Oxidized makes Create's copper machinery age like vanilla copper, without new blocks or items. A mixin on
`StateDefinition.Builder.create` adds two properties, `createoxidized_stage` (unaffected / exposed / weathered /
oxidized) and `createoxidized_waxed`, to every block whose class (or a superclass) is in a list of 48 classes. Only
blocks in the tag `createoxidized:oxidizable_create_blocks` actually age; the others carry the properties unused.

- **Aging**: random ticks, vanilla's chance and neighbor rule (`WeatheringCopper` math copied: 4-block Manhattan
  scan, a younger neighbor blocks aging, vanilla copper blocks count as neighbors), scaled by `speedFactor`.
- **Waxing**: honeycomb on the block (consumed). **Scraping**: any `AxeItem` removes wax, else steps one stage back
  (durability 1). Waxed Up's copper coating and oxidizer sprays (`waxedup:copper_coating`, `waxedup:oxidiser`) are supported when that mod is present.
- **Lightning**: a strike on a lightning rod or copper resets the anchor Create block to unaffected and walks 3 to 5
  random paths of 1 to 8 steps, cleaning one stage per step (vanilla rod behavior).
- **Items**: broken blocks drop with NBT `CreateOxidizedStage` / `CreateOxidizedWaxed` and a custom name ("Waxed
  Weathered Fluid Pipe"); placing restores the state (both halves of doors). Config `clutterReducer3000` drops
  everything as plain and hides the creative tab entries.
- **Look**: client only. Baked models of the 72 ids (plus 5 D&D sub-models) are wrapped; quads whose sprite is one
  of 107 listed copper textures are remapped to `createoxidized:block/<stage>/...`. Five client mixins swap Create
  partial models (boiler gauge, PSI, fluid valve pointer, valve handle) for staged copies.
- **Creative tab**: "Create: Oxidized", every target in every stage, waxed and not (7 stacks per block).

### Facts

| Field | Value |
|---|---|
| Mod id | `createoxidized` (old Oxidized: `create_oxidized`) |
| Items / blocks | 0 / 0 (variants are NBT on existing items and states on existing blocks) |
| Blockstate properties | 2, on 48 classes (incl. vanilla `IronBarsBlock`, `DoorBlock`: B1) |
| Target block ids | 72 in the tag (19 Create, 18 Create Deco, 20 Diesel Generators, 1 Create Connected, 10 dndecor, 4 dndesires); 32 exist in the pack |
| Effects / potions | none |
| Recipes | none (old Oxidized: 30 `create:filling` recipes, section 2) |
| Loot | none; drops get NBT in a `Block.getDrops` mixin, not a loot function |
| Tags | 1 block tag `createoxidized:oxidizable_create_blocks` (optional entries for addons) |
| Advancements | none (the pack drops all advancements anyway) |
| Lang | 10 keys (`item.createoxidized.variant.*`, tab, 2 config labels) |
| Tooltips | none; the name carries the stage |
| Textures / models | 312 PNG (3 stages x 104 sprites), 21 partial-model JSON; manifest `oxidizable_textures.txt` (107 lines) |
| Config | common: `oxidation.enabled` (true), `oxidation.speedFactor` (1.0, 0 to 100), `clutterReducer3000` (false) |
| Mixins | common 5 (`Block`, `BlockItem`, `BlockStateBase`, `LightningBolt`, `StateDefinition.Builder`), client 5 (Create renderers and Flywheel visuals) |

## 2. Overlap

| Thing | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Mod name | old Oxidized (`create_oxidized`, installed) | Same display name "Create: Oxidized" in JEI, mod list, sides table | D1: keep old Oxidized's recipes or drop the jar; if both stay, rename one display name in our fork |
| Spout aging | old Oxidized: 30 `create:filling` recipes (250 mB water ages vanilla copper, cut copper, Create shingles and tiles one stage) | Two ways to age copper blocks by machine (spout and fan); neither covers Create machines | *overlaps w/ Create Copper Splashing port* D2 |
| Fan aging | `feature_ports.js` Create Copper Splashing (fan splashing ages copper blocks, shingles, tiles, cut copper, Quark vertical slab) | Oxidized has no machine path for its 32 blocks; staged items cannot be recipe outputs without NBT | *overlaps w/ fan splashing* D3: no machine path (scope rule) unless the owner asks |
| JEI world tabs | [jei_world_tabs.md](jei_world_tabs.md): Waxing and Scraping (503 rows), Lightning Conversion, Soap Washing; source `creativeDevtools` `WorldJeiToolPairs` reads `WeatheringCopper` / `HoneycombItem` maps | Oxidized does not use those maps, so its waxing and scraping are invisible in JEI | *overlaps w/ JEI world tabs* fold-in step 10 |
| Soap | Supplementaries soap washes oxidation off vanilla copper (Soap Washing tab) | Soap does nothing to staged Create blocks | *overlaps w/ Soap washing* D5 |
| Copper decor | Design n' Decor (`design_decor`, 1.19.2 forerunner of `dndecor`): 16 copper block textures (boilers, bolt, catwalk, floodlight, floor, gas tank, lamp, chain, light, railing, screw) | The mod's `compat/dndecor` art matches, but the paths differ (`boilers/copper` vs `copper_boiler`), and the class list names `dev.lopyluna.dndecor.*` classes, not `design_decor` ones | *overlaps w/ Design n' Decor* D6: extend with a path map (copy of upstream art, no edit) or leave out |
| Copper doors, bars, ladders | everythingcopper (own `WeatheringCopper` doors, bars, ladder, grate), vanilla copper | Different blocks; no id clash. `DoorBlock` / `IronBarsBlock` in the class list also gives everythingcopper's (and every other mod's) doors and bars the two properties: B1 | B1 fix |
| Stack splitting | NBT variants stack only with the same stage | Players with mixed pipes get up to 7 stacks of one item; `clutterReducer3000` exists for that | D4 |

Batch: no other mod is being integrated with it. The pack's Dreams and Desires (`create_dd` 1.2c) is not the
`dndesires` the mod targets; none of the 4 `dndesires` ids exist.

Present targets (32): `create:` copper_casing, fluid_pipe, encased_fluid_pipe, glass_fluid_pipe, mechanical_pump,
smart_fluid_pipe, fluid_valve, copper_valve_handle, fluid_tank, hose_pulley, item_drain, spout,
portable_fluid_interface, steam_engine, copper_ladder, copper_bars, copper_scaffolding, copper_door; `createdeco:`
blue/green/red/yellow_copper_lamp, copper_bars, copper_bars_overlay, copper_catwalk, copper_catwalk_stairs,
copper_coinstack, copper_hull, copper_mesh_fence, copper_sheet_metal, copper_support, locked_copper_door. Absent:
`create:copper_table_cloth` (Create 6), Create Deco catwalk_railing, support_wedge, window, window_pane (classes
`CatwalkRailingBlock`, `MeshFenceBlock`, `SupportWedgeBlock` not in 1.3.3; `copper_mesh_fence` exists under another
class, step 3), all Diesel Generators, Create Connected, dndecor and dndesires ids.

## 3. New content

### 3.1 Blockstates

No new ids. Two properties on existing blocks. Defaults are set by a `Block.registerDefaultState` mixin (unaffected,
not waxed), so existing saves load every placed Create block as unaffected. Removing the mod later drops the two
properties; blocks keep their other properties.

### 3.2 Mechanics

| Mechanic | Trigger | Values |
|---|---|---|
| Aging | random tick | attempt chance `1 - 0.94311111^speedFactor` (vanilla 0.0569 at 1.0), then vanilla neighbor ratio squared, x0.75 from unaffected |
| Waxing | right-click with honeycomb | 1 honeycomb (not in creative), particles 3003 |
| Scraping | right-click with any `AxeItem` | wax off first, then one stage back; 1 durability; vanilla sounds |
| Lightning | strike on a rod or copper | anchor reset to unaffected; 3 to 5 walks of 1 to 8 steps, one stage each |
| Item stage | break -> drop NBT; place -> restore | custom name from lang; `clutterReducer3000` disables |
| Waxed Up sprays | right-click, or place with the spray in off hand | soft compat; Waxed Up is not in the pack |

### 3.3 Client

Model wrapper per target id, quad remap by sprite and UV containment, 5 renderer / visual mixins for partial models.
Item models resolve the stage from NBT. The 1.19.2 port changes: `ModelEvent.ModifyBakingResult` does not exist in
Forge 43 (only `BakingCompleted`, `RegisterAdditional`, `RegisterGeometryLoaders`); Create 0.5.1 bundles Flywheel
0.6.11 (`com.jozufozu.flywheel.core.PartialModel`, `*Instance` classes instead of 1.20 `*Visual`), so
`FluidValveVisualMixin` and `ValveHandleVisualMixin` retarget `FluidValveInstance` and the valve handle's instance.
Common-side Create class names match 0.5.1 (20 of 21 Create classes present; `TableClothBlock` absent).

### 3.4 Recipes, loot, tags

No recipes, no loot tables. One block tag. Old Oxidized's 30 filling recipes are listed in section 2.

### 3.5 Upstream bugs

- **B1 State bloat on vanilla and unrelated blocks.** `ELIGIBLE_CLASSES` names `net.minecraft...IronBarsBlock` and
  `DoorBlock`, and Create's `CasingBlock`, `WindowBlock`, `ConnectedGlassPaneBlock`, `ConnectedPillarBlock`. Every
  subclass gets the two properties: all glass panes, iron bars, every mod's doors, every Create casing, window and
  palette pillar. Each gains x8 blockstates (fluid pipe 128 -> 1,024 already for a real target). In this pack (door
  and pane mods, Create palettes) that is a load-time and memory cost and a larger block-id map. Fix in the port:
  add the properties only to blocks whose id is in `BLOCK_IDS` (the mixin sees the owner object; resolve the id
  after registration or match by class plus a registry-name check at state build time, whichever 1.19.2 allows).
- **B2 Random ticking forced off.** `BlockStateBaseMixin.isRandomlyTicking` returns `isOxidizable(...)` for any state
  that carries the properties, replacing the block's own answer. A door or pane subclass that random-ticks (any mod)
  stops ticking. Fix: only force `true` for an aging state; otherwise fall through to the original.
- **B3 Quad caches grow without bound (needs a live check).** `QuadRemapper.SOURCE_CACHE` and `CACHE` key on
  `BakedQuad` identity. Create's connected-texture models (copper casing, fluid tank, Create Deco windows) can build
  fresh quads on chunk rebuilds; if they do, both maps grow for the session. Check heap after flying over a large
  copper build; fix with a bounded or weak cache keyed on sprite + UVs.
- **B4 Stage lost on conversions (needs a live check).** Create replaces blocks when a pipe is encased, glassed or
  wrenched (`FluidPipeBlock` <-> `GlassFluidPipeBlock` <-> `EncasedPipeBlock`), when a casing is applied, and when
  tanks become boilers; it copies named properties only. The new block likely starts unaffected. Fix: carry the two
  properties across in the port where Create swaps the block.
- Cosmetic: config comment for `clutterReducer3000` has typos ("thsi", "dont") and a meme name; the pack config is
  player-visible (D7).

No `logs/latest.log` evidence: the mod has never been loaded in the pack.

## 4. Fold-in, in build order

Home: an in-tree port `development/backportedCreateOxidized` (mod id `createoxidized` kept, per the `port` skill
rules; no underscore in our package names). It is machinery, not food: no Creative Delight routing.

0. **Dumps.** Not needed before the port: the mod adds no ids. After install, `/dumpall` so `display_names.json`
   and the JEI world-tab dumps see the new behavior ([dumpall-refresh-workflow](agent_notes/dumpall-refresh-workflow.md)).
1. **Port** (skill `port`): Forge 43 / Create 0.5.1 / Flywheel 0.6. `BakingCompleted` in place of
   `ModifyBakingResult`; `Registry.BLOCK` / `Registry.ITEM` for `BuiltInRegistries`; `CreativeModeTab` subclass for
   the 1.20 tab builder; 1.19.2 `CreativeModeTab.fillItemList`; Flywheel 0.6 instance mixins; refmap regenerated.
   Root `settings.gradle`, `installToMods`, `IN_TREE_MODS` in `tools/build_deploy.py`.
2. **Fix B1 and B2** in the port (section 3.5); B3 and B4 after the live checks (OXD-09, OXD-10).
3. **Target list for 1.19.2.** Trim `ORDERED_BLOCK_IDS`, the class list and the tag to the 32 present ids;
   `createdeco:copper_mesh_fence` uses a different class in 1.3.3 (look it up when porting). Optional entries for
   absent mods may stay in the tag (they are `required: false`) but the class list and manifest drop them. D6 decides
   Design n' Decor.
4. **Ids in dicts.** None: no new items or blocks. The tag goes through `global.tags` only if the pack edits it
   (the mod's own data tag ships in the jar).
5. **Upstream recipe removals.** Old Oxidized: per D1/D2, remove its 30 filling recipes through `global.NCH` or
   remove the jar.
6. **Loot, item merges, potions, effects, food, food families, cans, compost, crops, worldgen, trades.** None (no
   items, drops or effects; drop NBT comes from the mixin).
7. **Stack sizes.** None: the items keep their stack sizes; NBT variants split stacks (D4).
8. **Names and lang** ([names.md](names.md)): 10 keys, already American spelling; "Oxidized %s" matches vanilla
   wording. Our lang copy under `assets/createoxidized/lang/` (Forge serves the first jar's copy; note
   `forge-shared-asset-path` memory). Display name per D1.
9. **Block audit.** BlockRunner tiers, mineable tags, Item Physic, WTHIT unchanged (same blocks). WTHIT: the block
   name does not show the stage; D8 adds a line through Creative Common's WTHIT plugin.
10. **JEI** ([jei_coverage.md](jei_coverage.md), [jei_world_tabs.md](jei_world_tabs.md)): Waxing and Scraping and
    Lightning Conversion read vanilla maps. Add rows for the 32 blocks (input stage NBT stack -> output stage NBT
    stack) in `WorldJeiToolPairs` (creativeDevtools) or the tab's data source. Hide or keep the creative-tab NBT
    variants in JEI's item list (7 per block; D4).
11. **Tooltips** ([tooltips.md](tooltips.md)): none needed; the stage is in the name.
12. **Textures.** Upstream art copied as is (human art, MIT). Every file is a copy, so no row in
    `texture_sources/sources.csv` unless D6 adds a path map for Design n' Decor (copy rows, tool none).
13. **Config.** `createoxidized-common.toml` defaults per D4 and D7; copy any hand-edited config to
    `configs_edited/root_config/config/`.
14. **Release.** `mod_sides.md` row (both sides: blockstates must match), changelog bullets (new player-visible
    mechanic: family `## Copper Machine Oxidation`), TODO row, README index row, project page license line
    ([project_pages/pack_art.md](project_pages/pack_art.md): 312 MIT textures).

Order: 1 before 2 and 3; 2 before any live test (B1 changes saves' state ids, so test only on fresh worlds until it
is in); 5 after D1/D2; 10 after the jar is installed and `/dumpall` (step 0) has run; 14 last.

## 5. Decisions for the owner

Ruled 2026-11-25 (owner): D1 drop the old jar, D2 its recipes rebuilt in the member, D3 spout and fan for staged blocks, D4 stage kept with a config toggle, D5 soap takes off wax and all stages, D6 Design n' Decor in the first build; D7 agent default (`keepStageOnBreak`); D8 still open. Rulings, the port home (Creative Engineering member `creativeoxidation`) and the build order: [plan_creative_oxidation.md](plan_creative_oxidation.md) section 3.

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Keep old Oxidized (`create_oxidized`, spout aging recipes) next to the port? | Drop the jar; its spout recipes duplicate fan splashing | Keep both; rename the port's display name "Create: Oxidized Machines" |
| D2 | If old Oxidized stays: keep its 30 spout recipes? | Remove (fan splashing covers the same blocks) | Keep |
| D3 | A machine path (fan splashing or spout) for staged Create blocks? | No (scope rule); aging stays by time and lightning | No |
| D4 | Staged items: keep NBT drops (up to 7 stacks per item) or `clutterReducer3000` (always drop plain)? | Keep NBT drops, hide the variants from JEI's item list | Keep NBT drops |
| D5 | Soap cleans staged Create blocks like vanilla copper? | Yes, one stage per use as Soap Washing does (pack rule: soap removes oxidation) | No |
| D6 | Extend to Design n' Decor copper blocks with the matching `dndecor` art (path map, 16 textures)? | Yes, later phase | No |
| D7 | Rename and reword the `clutterReducer3000` option in our fork? | Rename to `plainDrops`, plain comment | Keep upstream |
| D8 | WTHIT line showing the stage and waxed state? | Yes (name already shows it on items, not on placed blocks) | No |

## 6. File map (planned)

- `development/backportedCreateOxidized/` (new): `build.gradle`, `src/main/java/dev/mrmulle/createoxidized/**`
  (ported classes, upstream MIT notice in `LICENSE-upstream`), `src/main/resources/assets/createoxidized/**` (copied
  textures, models, lang, manifest trimmed per step 3), `data/createoxidized/tags/blocks/oxidizable_create_blocks.json`,
  `createoxidized.mixins.json`.
- `settings.gradle`, `tools/build_deploy.py` (`IN_TREE_MODS`), `mod_sides.md`.
- `development/creativeDevtools/.../WorldJeiToolPairs.java` (step 10), Creative Common WTHIT plugin (D8).
- `kubejs/server_scripts/...` only if D2 removes old Oxidized recipes.
- `kubejs/assets/creativecommon/changelog/unreleased.md` (step 14).

## 7. Offline checks (once built)

- `./gradlew :backportedCreateOxidized:build --offline`; `python3 tools/check_all.py --fast --changed`.
- Javac harness: `OxidationStage.next/previous`, aging chance at speedFactor 0 / 1 / 2, the item NBT adapter
  round trip, and the B1 fix (a vanilla `IronBarsBlock` and a `DoorBlock` state definition carry no
  `createoxidized_*` property; `create:fluid_pipe` does).
- `python3 tools/privacy_lint.py`, `python3 tools/test_prefix_lint.py`.

## 8. In-game test suite (drafted)

- [ ] **OXD-01 Aging:** place a Fluid Pipe and Copper Casing in the open, `speedFactor` 100, wait ~2 minutes -> both
  step through exposed, weathered, oxidized; textures change each stage.
- [ ] **OXD-02 Waxing:** right-click an exposed Fluid Tank with a honeycomb -> one honeycomb used, wax particles; with
  `speedFactor` 100 it stays exposed.
- [ ] **OXD-03 Scraping:** right-click a waxed weathered Spout with an axe -> wax off (sound), again -> exposed; axe
  loses 1 durability each time.
- [ ] **OXD-04 Lightning:** summon lightning on a lightning rod on top of an oxidized pipe line -> the pipe under the
  rod is unaffected, a few nearby pipes step back.
- [ ] **OXD-05 Item stage:** break a waxed weathered Mechanical Pump -> drop named "Waxed Weathered Mechanical Pump";
  place it -> same stage and wax.
- [ ] **OXD-06 Door halves:** place an oxidized Copper Door item -> both halves oxidized.
- [ ] **OXD-07 Partial models:** oxidized Steam Engine on a boiler, Fluid Valve, Copper Valve Handle, Portable Fluid
  Interface -> gauge, pointer, handle and PSI parts all show the oxidized texture.
- [ ] **OXD-08 Unrelated blocks:** a glass pane, iron bars, an oak door and an Andesite Casing -> F3 shows no
  `createoxidized_*` property (B1); a mod door that random-ticks still does (B2).
- [ ] **OXD-09 Quad cache:** fly over a 32x32 copper casing and tank build for 5 minutes with F3 memory -> heap
  settles (B3).
- [ ] **OXD-10 Conversions:** wrench an oxidized Fluid Pipe to glass and back, encase it with a casing, make a
  weathered tank into a boiler -> the stage is kept (B4).
- [ ] **OXD-11 Contraption and schematic:** move a weathered Fluid Tank on a contraption, and print a schematic of
  waxed pipes -> stages kept.
- [ ] **OXD-12 Server and client:** dedicated server plus client join -> no state-id mismatch kick, staged blocks
  render on the client.

## 9. History

- 2026-11-25: investigation written from `createoxidized-forge-1.20.1-1.0.0.jar`, nothing built.
- 2026-11-25: owner rulings and plan in [plan_creative_oxidation.md](plan_creative_oxidation.md); the port becomes the Creative Engineering member `creativeoxidation`, with iron rusting recipes added. Section 4 home and namespace superseded by the plan.
