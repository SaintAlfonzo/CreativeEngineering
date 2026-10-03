# Feature writeups

One file per finished (or nearly finished) pack feature, for two readers: you, when you come back to
test or retune something, and Claude, at the start of a later session. Each writeup is the single
up-to-date reference for its feature. The code and config stay the source of truth for values; the
writeup explains how the pieces fit and how to prove they work.

## Index

| Feature | File | Test prefix | Status (see legend) |
|---|---|---|---|
| BlockRunner walk speed by surface (tiers, sticky facing, camo speed, visual sinking) | [blockrunner.md](blockrunner.md) | `BR-` | DEPLOYED, in-game test pending; 1.0.4 mod blocks tiered 2026-10-02 (BR-52) |
| Tooltips (pack lines, removals/rewrites of other mods' lines, framework tooltips) | [tooltips.md](tooltips.md) | `TT-` | DEPLOYED (new removals), in-game test pending |
| Structure tuning (structure groups: shared grids with a minimum gap and per-structure weight/frequency, standalone sets, village-house pool weights, Lootr container swaps, ground matching of grass/dirt bases, CEConfig [worldgen] switches + debug density, /ceplace mass placement, other mods' structure configs) | [structure_tuning.md](structure_tuning.md) | `ST-` | DEPLOYED, in-game test pending (ST-11 passed; groups ST-17 to ST-26 new, ST-01/02/06-09 retired; ground matching, [worldgen] config + debug density, /ceplace DEPLOYED, ST-27 to ST-42) |
| Structures by biome (generated reference: kinds and rates per biome, holes, professions per village style) | [structures_by_biome.md](structures_by_biome.md) | none | reference, regenerate with `python3 tools/gen_structure_biome_table.py` |
| Loot (census of every loot table by mod, chest-loot tuning catalog + runtime, pre-roll framework: bans, tag groups, boosts, packed-food splits, additions, injection restores, aliases; LootJS-only scripts, mod loot configs) | [loot.md](loot.md) | `LT-` | DEPLOYED, in-game test pending (LT-01, LT-06 passed; LT-14 to LT-27 new); findings 1-7 fixed, 8 (legacy dump) open; catalog + census refreshed 2026-10-02 |
| Food (1.0.2: `/dumpfood`, 31 food types as `#creativeengineering:food_type/...` tags, canned map, Spice of Life mechanics, Canned Goods parity, tuning design) | [food.md](food.md) | `FO-` | BUILT, restart pending (FO-01 passed; FO-02 to FO-08); refreshed 2026-10-02 for the 1.0.4 food mods (856 foods); tuning system designed, not built (decisions D1-D6); fried egg + cooking oil + egg stacks BUILT (FO-23 to FO-28); egg tag migration TODO |
| Performance (always-on JFR recording on the server, dump helpers, per-mod HTML report: mods, tick phases, event handlers, busy seconds, allocation, worldgen threads; `perf/report.sh` crash/lag bundle + `ingest_report.py`) | [perf.md](perf.md) | `PF-` | BUILT, ships with the next deploy (PF-01 to PF-12) |
| Trash slot (pack default screens, per-tier backpack positions, creative support, allContainers switch) | [trashslot.md](trashslot.md) | `TS-` | BUILT, in `mods/` (patcherMod 1.0.3), not deployed (TS-01 to TS-13) |
| Deploy pipeline (deployment snapshot, update/install payloads, verify scripts, kubejs file classes, gzipped climate samples) | [deploy.md](deploy.md) | `DP-` | BUILT, offline checks passed; first real update pending (DP-01 to DP-07) |
| Vinery JEI pages (Aging Barrel fermentation, Apple Press, Grape Stomping, Wine Aging) | [vinery_jei.md](vinery_jei.md) | `VJ-` | BUILT, in `mods/` (patcherMod 1.0.3) (VJ-01 to VJ-10); fermentation time wrong (1800 vs about 50 ticks), fix open |
| Vinery (behaviour reference from the decompiled 1.3.10 jar: pot, press, barrel, stems, lattice, bushes, vines, leaves, wine aging and effects, worldgen, hopper jams; pack changes; tooltip removals + placeholders) | [vinery.md](vinery.md) | `VN-` | reference, in-game checks pending (VN-01 to VN-20); tooltip placeholders TODO |
| Food catalog (generated reference: every food's type, hunger, saturation, effects, SoL value, cans, recipe chains, canned parity) | [food_catalog.md](food_catalog.md) | none | reference, regenerate with `python3 tools/food_catalog.py` after `/dumpfood` |
| Stack sizes (generated reference: every item whose max stack is not 64, cooking items first, unstackable gear left out; `/dumpstacks`) | [stack_sizes.md](stack_sizes.md) | none | reference, regenerated 2026-10-02 from `/dumpstacks` (2141 items); rerun `python3 tools/stack_sizes.py` after the dump |
| Exposure cameras and photographs (our 1.9.19 backport in one jar: base mod, Polaroid instant camera, Expanded lenses/filters/palette films; build-time tag merge; Catalog on hold) | [exposure.md](exposure.md) | `EX-` | DEPLOYED (in `mods/`, ships with 1.0.3), in-game test pending (EX-11, EX-17 passed); EX-50 old-world check before deploy |
| Crops (every crop's type, soils incl. Rich Soil / Rich Farmland / Fertile Soil, Quark right-click replant, hoe area right-click and area break; crop parity layer for non-standard crops; wild generation: biomes, rates, heights, side blocks) | [crops.md](crops.md) | `CR-` | reference + tobacco parity and per-hoe areas BUILT (patcherMod 1.0.4 in `mods/`, not deployed); CR-01 to CR-46 pending; G1-G3 fixed, G4-G15 open; 1.0.4 mods' crops added 2026-10-02; wild generation §2.7 added 2026-10-03 (V1-V12 feed the planned vegetation worldgen system) |
| Crops by biome (generated reference: every wild crop / bush / fruit tree feature, its rate, height, biome list and land share; crops per biome) | [crops_by_biome.md](crops_by_biome.md) | none | reference, regenerate with `python3 tools/gen_crop_worldgen_table.py` |
| Names (Display Delight placed-food names, Rustic Delight crop/potted names, missing-name catalog: partial blocks first, then items/effects/potions/enchantments/entities/fluids/professions; `/dumpnames`) | [names.md](names.md) | `NM-` | BUILT, in-game check pending (NM-01 to NM-06); NM-07/08 PASSED: catalog built from the real `/dumpnames` export 2026-10-02, triage redone |
| JEI coverage gaps (catalog: crafting recipes JEI hides, recipe types with no category, world interactions; suggested fix per gap) | [jei_coverage.md](jei_coverage.md) | `JC-` (spot checks) | part 1 (crafting) generated, regenerate with `python3 tools/jei_coverage.py` after `/dumpcraftingaudit`; parts 2-3 not started; refreshed 2026-10-02 (70 hidden) |
| Wandering Trader + Wandering Winemaker (independent tick-based spawn timers, 25 + 5 offers, stock edited through `global.trades`: removals, Wares x20, golden listings, saplings/seeds/plants/blocks/mob buckets, Winemaker additions, no Nether/End items) | [wandering_trader.md](wandering_trader.md) | `WT-` | BUILT, patcherMod 1.0.4 in `mods/` + KubeJS, scratch server checked; WT-01 to WT-16 pending (WT-06 retired) |
| Wandering Trader catalog (generated: every pool entry of both traders with weights, gap check per category) | [wandering_trader_catalog.md](wandering_trader_catalog.md) | none | reference, regenerate with `python3 tools/wandering_trader_report.py` after `/dumptrades` |

## Release plans

| Release | File | Status |
|---|---|---|
| 1.0.3: deploy install/update modes, server performance report, render desync, item-loss monitor, trash slot, join protection; plus everything else added during development (section 0.1) | [plan_1.0.3.md](plan_1.0.3.md) | DEPLOYED 2026-10-02 (server + clients) |
| 1.0.4: fixes for the 1.0.3 join kick and client crash, camera bags moved to Exposure Extras, crop parity, per-hoe harvest areas, dyeable canned golem, new mods, data refresh | [plan_1.0.4.md](plan_1.0.4.md) | IN PROGRESS, not deployed |

## Backlog: the rest of the pack, one small chunk at a time

Not written yet. Each line is one writeup-sized chunk; Claude proposes the next one at natural
stopping points and you pick (or reorder). Prefixes are proposals.

| # | Chunk | Covers | Prefix |
|---|---|---|---|
| 1 | Shields | tier shields, vanilla shield folding, damage-tag layer they read | `SH-` |
| 2 | Armor and tool effects | armor effects framework (incl. mounts), tool effects, echo armor set bonus, magic resistance | `AE-` |
| 3 | Gear tuning | tuneTools / tuneArmor, echo-tier tools, dragon gear autorepair, gear assembly NBT, dragon scale elytra, elytra rocket takeoff, air brake | `GR-` |
| 4 | Potions and brewing | Create automatic brewing, IE refinery lingerification, IE bottler NBT fluids, JEI fluid tooltips, AM / Rustic Delight brewing fixes | `PO-` |
| 5 | Potion buckets and tipped arrows | potion buckets (+ dispenser), machine tipping of arrows | `PB-` |
| 6 | Engineering recipes | sawblade routing, dragon-breathing fan type, CE components and coils, sample syringe | `EN-` |
| 7 | Item physics | ItemPhysic fire/lava split and float/burn lists | `IP-` |
| 8 | ~~Loot~~ | done: [loot.md](loot.md) | `LT-` |
| 9 | Biome tuning | biome frequency solver (`/dumpclimate`), biome merges into structure spawn tags (structure frequency, pools and Lootr swaps are done: [structure_tuning.md](structure_tuning.md)) | `WG-` |
| 10 | Fixes and compat | forgiving void, banner patterns, D&D drill/saw crash, client exit hang, recipe-book desync, axe tag ghosts, armor bar overflow, Create config text field, bonemeal charm | `FX-` |
| 11 | Pack tooling | item_dict, recipe helpers (NCH), tag helper, CEConfig, dump commands, headless test harnesses | `TL-` |

## Status legend

- **VERIFIED**: every test in the suite passed in game (the date is on the file)
- **DEPLOYED**: built and in `mods/` / scripts live, but not (fully) tested in game
- **BUILT**: compiles and passes offline checks, not yet deployed
- **BROKEN**: a test failed; the failing IDs are listed in the file's status block

## Layout every writeup follows

0. **At a glance**: status per part, the files to open, the config knobs. Read this first.
1. **Overview**: what the feature (or the underlying mod) does out of the box.
2. **Reference**: the values and tags, with examples.
3. **What the pack changes**: each customisation, how it's wired and why it was done that way.
4. **Limits and decisions**: known gaps and the options that were rejected, with reasons, so they don't get re-proposed.
5. **File map**
6. **Offline checks**: what can be verified without launching the game.
7. **In-game test suite**: numbered tests (`PREFIX-NN`) with setup, action and expected result.
8. **History**: dated changes.

## Reporting test results

Tick the box in the file, or just tell Claude the ID and what happened. For example:
`BR-12 fail: extended plain piston head still slows me`. Claude updates the checkboxes, the status
block and the History section. A fully ticked suite moves the feature to VERIFIED in the index.

## Keeping them current

When a feature changes, or a bug fix or tweak touches it, its writeup changes in the same session: values, file map, new or retired
tests (retire an ID rather than renumbering it) and a History line. Tooltip changes always go in
[tooltips.md](tooltips.md), with a cross-link from the feature they belong to. Memory entries stay short and
point here instead of repeating the details.
