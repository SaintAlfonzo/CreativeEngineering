# Creative Devtools: the pack's developer tools in one dev-only jar

Written 2026-11-01 (Creative Split wave 1, [creative_split.md](creative_split.md) 2.1, 7.6; TODO `creativesplit/devtools #4`).

**Status: BUILT 2026-11-01 (compile only), not installed, not booted.** `creativedevtools-1.0.0.jar`
(`development/creativeDevtools/build/libs/`) nests perfBot (`ceperfbot`) and clientCapture (`cecapture`). patcherMod
lost the moved code in the same pass, so `/cegen`, `/dumpall`, the `/dump*` commands, `/ceperf`, `/ceplace` and the
probes are gone from the dev instance until this jar is installed beside a rebuilt patcherMod.

Written for: the owner and the split agents, as the execution record of the Devtools carve. Same shape as
[creative_brewing.md](creative_brewing.md); only what is specific to Devtools is here.

## 0. At a glance

| Piece | Gradle project | Folder | Mod id | Ships |
|---|---|---|---|---|
| Creative Devtools (bundle) | `:creativeDevtools` | `development/creativeDevtools` | `creativedevtools` | dev `mods/` only (Q6) |
| CE Perf Bot (member) | `:perfBot` | `development/creativeDevtools/members/perfbot` | `ceperfbot` | nested only |
| CE Client Capture (member) | `:clientCapture` | `development/creativeDevtools/members/capture` | `cecapture` | nested only |

Both members are inert unless their system property is set (`-Dceperfbot.out` on a dedicated server,
`-Dcecapture.plan` on a client), so nesting them in a jar that loads on both sides is safe. Their tools still take the
member's own `build/libs/` jar (`tools/perf/suite/server.py`, `tools/scratch_query/prep_srv.sh`); a top-level jar
always beats a nested copy (creative_delight.md 14.1 point 4).

## 1. Framework

- **Build:** `ce.forge-bundle` like CB. `build.gradle` copies the pack-folder script trees
  (`kubejs/data/creativedevtools/kubejs/`, `kubejs/assets/creativedevtools/kubejs/`) into the jar at the same paths and
  fails if the startup or server tree has no `.js`. `compileOnly project(':patcherMod')` plus patcherMod's `libs/`
  (the IE API for the recycling diagnostics). No mixins (`mixin_configs=none`, no gated plugin).
- **Dependencies (`mods.toml`):** `creativeengineering` mandatory, `AFTER` (becomes `creativecommon` at the Q1 swap);
  `creativecompat` is added when Compat is carved (7.6). Worldgen is an optional dependency once it exists
  (`MassPlacer` reads `WorldgenFiles`, which becomes Common util per 7.2).
- **Config:** `config/creativedevtools-common.toml` (`DevtoolsConfig`). The whole `[debug]` section of
  `creativeengineering-common.toml` moved with its keys and comments (`[debug.generators]`, `[debug.block_state_sync]`,
  `[debug.names]`, `[debug.wthit]`, `[debug.recipe_scan]`, `[debug.level_random]`), and so did
  `[worldgen.mass_place]` (same path in the new file). Every value in the dev instance was the default; the file is in
  `config/` and `configs_edited/root_config/`.
- **Scripts (rule 3.9):** startup core `kubejs_startup/creativedevtools/cdv_core.js` defines `global.CDV`
  (`on(modId)`, `feature(id)` for the `[features]` switches `testRecipes` and `flowerWorld`, `recipeScanAtLoad()`, `recipes(fn)`); server core `_core/cdv_server.js`. Every file is
  headed `// requires: creativedevtools`, so the pack-folder copies are skipped when the jar is not installed.
- **Edges into Common (hooks, patcherMod never references Devtools):**
  - `util/BlockStateLookupHook`: `SectionUpdateNullStateMixin` and `BlockUpdateNullStateMixin` call it;
    `BlockStateSyncDebug` registers its listener from the Devtools constructor. No listener = plain `byId`.
  - `util/LevelRandomHook`: `LevelThreadSafeRandomMixin` calls it; `LevelRandomProbe` registers a factory that
    returns the probe when `[debug.level_random] probe` is on. No factory = `RandomSource.createThreadSafe()`.
  - `global.guardThrowable` and `global.writeExportJson`: `java_guard.js` and `export_io.js` stay in the pack's
    `server_scripts/_lib/` (runtime users in `loot/05_tuning.js`) and also export themselves as globals; the
    Devtools server core wraps them for its scripts.
- **Dev-only:** `mod_sides.md` side `dev` (new value): `tools/dump_server_mods.py` and `tools/build_deploy.py` skip it,
  `tools/gen_pack_manifest.py` leaves it out. The script trees are `dev` in `KUBEJS_CLASSES`. The scratch server setup
  only drops `client` rows, so scratch servers get Devtools.

## 2. What moved

Package `com.SaintAlfonzo.creativedevtools` (PM = patcherMod's `com.SaintAlfonzo.creativeengineering`).

| From | To | Notes |
|---|---|---|
| PM `devtools/GeneratorCommand`, `GeneratorRunner` | `generators/` | `/cegen`; mod-bus `onCommonSetup` / `onLoadComplete` now added by `CreativeDevtools` |
| PM `devtools/BlockStateSyncDebug` | `probe/` | listener on `BlockStateLookupHook` |
| PM `devtools/LevelRandomProbe` | `probe/` | factory on `LevelRandomHook` |
| PM `devtools/DisplayNameDump`, `WthitIconDump`, `WorldJeiDump`, `WorldJeiMobBuckets`, `WorldJeiToolPairs` | `dump/` | `/cedumpnames`, `/cedumpicons`, `/dumpworldjei` |
| PM `worldgen/SpawnDump` | `dump/` | `/dumpspawns` backing class (script loads the new class name) |
| PM `perf/PerfCommand` | `perf/` | `/ceperf`; `PerfEvents`, `PerfJfrEvents`, `MobCapLevels` stay (Common perf mixins use them) |
| PM `worldgen/MassPlacer` | `ceplace/` | `/ceplace`; `CEConfig.VerticalAlign` and the `WorldgenConfig.place*` readers moved into `DevtoolsConfig`; `BiomeLayoutTuner.WORLDGEN_DIR` and `PoolTuner.parseLenient` made public for it; its offline test is `tools/worldgen_test/com/SaintAlfonzo/creativedevtools/ceplace/MassPlacerTest.java` (`tools/test_worldgen_tools.sh` test 2) |
| `CreativeEngineering.onServerStarted` IE recycling log | `ie/IeRecyclingDiagnostics` | same log lines, registered only when IE is loaded |
| `kubejs/server_scripts/_lib/dump_*.js`, `loot_dump.js`, `structure_dump.js`, `villager_trades_dump.js` (25 files) | `kubejs/data/creativedevtools/kubejs/dump/` | command names unchanged |
| `_lib/recipe_scan.js` scan half (`scanRemovedItemRecipes`, `/recipescan`, run-at-load) | `dump/recipe_scan.js` | the tag strip stays in the pack as `_lib/removed_item_tags.js` |
| `_lib/metal_test_recipes.js`, `wood_test_recipes.js`, `sequence_assembly_processes.js` | `create/` | recipe ids now `creativedevtools:` (inside `global.CDV.recipes`) |
| `_lib/flower_world.js` | `flowerworld/` | with the preset `data/creativeengineering/worldgen/world_preset/flower_test.json`, the `minecraft:normal` preset tag and the lang key (ids unchanged) |
| `client_scripts/texture_tool.js` | `kubejs/assets/creativedevtools/kubejs/dump/` | console texture probe |
| `development/perfBot`, `development/clientCapture` | `development/creativeDevtools/members/{perfbot,capture}` | `bundled_in=creativeDevtools`; project names unchanged |

Doubtful rows (moved, owner may revisit):
- `_lib/flower_world.js` and the Flower Test preset: shipped to players in 1.1.06 ("Flower Test" world type). Moving
  them removes the world type from player installs (changelog bullet written).
- The three test-recipe files: they loaded in normal play and their recipes showed in JEI; moving them removes them
  for players (changelog bullet written).
- `PackOrderReport` (JEI order report) stays Common (part of the ordering code), as sweep A suggested.
- The operator runbook (`deploy/RUNBOOK.md` 5) tells the live-server operator to run `/dumpstructuregen`, `/dumploot`
  and other dumps there. With Q6 those commands are no longer on the live server. Not changed here (owner).

Not moved: `java_guard.js`, `export_io.js` (runtime users, section 1), `_lib/removed_item_tags.js` (runtime strip CD
calls), `PerfEvents` / `PerfJfrEvents` / `MobCapLevels`, `PackOrderReport`.

Counts: 12 classes moved plus the IE log extracted into a class; 31 scripts moved (25 dumps, the scan half, 3 test
recipe files, the flower world, the texture tool) plus the two core files; 3 resources (preset, preset tag, lang key).
Tooling repointed: `tools/generators.json` readme, `tools/perf/suite/server.py` and `tools/scratch_query/prep_srv.sh`
(perfBot path; prep_srv also swaps in the Devtools build jar), `tools/test_worldgen_tools.sh`, the docstrings of
`name_audit.py`, `wthit_icon_audit.py`, `diff_block_state_ranges.py`.

## 3. Rules

- Commands keep their names; config keys keep their names and comments.
- patcherMod never imports Devtools. A Common class that needs a probe exposes a hook (section 1).
- Devtools never ships: no `IN_TREE_MODS` row, no payload, side `dev`.
- Recipe ids of the test recipes changed namespace (rule 3.9); item ids did not.

## 4. Tests

Scratch server (SQ-CDV, [scratch_queue.md](testing/scratch_queue.md)) and in game:

- [ ] **CDV-01 Load:** `logs/latest.log` has `[creativedevtools] loaded` with the member list `[ceperfbot, cecapture]`
  and no ERROR for `creativedevtools`; `config/creativedevtools-common.toml` has `[debug.*]` and
  `[worldgen.mass_place]`; `creativeengineering-common.toml` has neither after a start.
- [ ] **CDV-02 Commands:** `/cegen list`, `/ceperf status`, `/ceplace status`, `/recipescan`, `/dumpworldjei`,
  `/dumpspawns`, `/flowerworld` exist (tab completion) and run; `/dumpall` reports every dump ok.
- [ ] **CDV-03 Scripts:** `logs/kubejs/server.log` has `[creativedevtools] server scripts ready` and no
  `writeExportJson` / `guardThrowable` ReferenceError; the test recipes show in JEI under `creativedevtools:` ids.
- [ ] **CDV-04 Probes:** with `[debug.block_state_sync] enabled = true` a dedicated server writes
  `logs/ce_block_state_ranges_server.txt`; with `[debug.level_random] probe = true` the log shows no
  `[LevelRandomProbe]` error at world load. Both off again afterwards.
- [ ] **CDV-05 Without Devtools:** with only patcherMod (no Devtools jar), the server starts, no `/dumpall`, no
  `/cegen`, no Flower Test world type, removed items still lose their tags (`/recipescan` gone).
- [ ] **CDV-06 Flower Test:** the world type "Flower Test" is listed on a dev client and builds its table.

## 5. Install (game closed)

`./gradlew :patcherMod:installToMods :creativeDevtools:installToMods --offline` together: patcherMod without Devtools
loses the tools, Devtools without the new patcherMod fails on the missing hooks. Not part of any player payload.

## 6. History

- 2026-11-01: carved (wave 1, beside Calendar). Framework, Java and script moves, hooks in Common, dev-only side,
  perfBot and clientCapture nested. Compile-verified; not installed or booted.
