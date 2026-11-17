# Creative Devtools: the pack's developer tools in one dev-only jar

Written 2026-11-01 (Creative Split wave 1, [creative_split.md](creative_split.md) 2.1, 7.6; TODO `creativesplit/devtools #4`).

**Status: INSTALLED 2026-11-01 (dev `mods/`), scratch-verified: SQ-CDV-01 to -04 PASS (run rcs1, [scratch_run_2026-11-01.md](testing/scratch_run_2026-11-01.md) 6); CDV-01 to -06 in game open.** `creativedevtools-1.0.0.jar`
(`development/creativeDevtools/build/libs/`) nests perfBot (`cdevperfbot`) and clientCapture (`cdevcapture`). patcherMod
lost the moved code in the same pass, so `/cdevgen`, `/dumpall`, the `/dump*` commands, `/cdevperf`, `/cdevplace` and the
probes are gone from the dev instance until this jar is installed beside a rebuilt patcherMod.

Written for: the owner and the split agents, as the execution record of the Devtools carve. Same shape as
[creative_brewing.md](creative_brewing.md); only what is specific to Devtools is here.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=de8848053419 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Testing 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativesplit/devtools #4` | Testing | none | Carve Creative Devtools |
| `generators-and-tests #18` | Testing | CQ-TXP-01, CQ-TXP-02, CQ-TXC-11, TXQ-OFF-01 to -05 | Paired client + own server runs and mismatch matrix finished; experimental Xvfb display (scratch testing expansion item 5, TX-31, TX-07) |
| `tooling/generators-and-tests #17` | Testing | SQ-TX-19 to -23, CDV-07 | `/cdevtest` probe verbs, tick warp, random ticks and perfBot test verbs (scratch testing expansion item 3) |
<!-- generated:status_index end open_work -->

| Piece | Gradle project | Folder | Mod id | Ships |
|---|---|---|---|---|
| Creative Devtools (bundle) | `:creativeDevtools` | `development/creativeDevtools` | `creativedevtools` | dev `mods/` only (Q6) |
| CE Perf Bot (member) | `:perfBot` | `development/creativeDevtools/members/perfbot` | `cdevperfbot` | nested only |
| CE Client Capture (member) | `:clientCapture` | `development/creativeDevtools/members/capture` | `cdevcapture` | nested only |

Both members are inert unless their system property is set (`-Dcdevperfbot.out` on a dedicated server,
`-Dcdevcapture.plan` on a client), so nesting them in a jar that loads on both sides is safe. Their tools still take the
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
| PM `devtools/GeneratorCommand`, `GeneratorRunner` | `generators/` | `/cdevgen`; mod-bus `onCommonSetup` / `onLoadComplete` now added by `CreativeDevtools` |
| PM `devtools/BlockStateSyncDebug` | `probe/` | listener on `BlockStateLookupHook` |
| PM `devtools/LevelRandomProbe` | `probe/` | factory on `LevelRandomHook` |
| PM `devtools/DisplayNameDump`, `WthitIconDump`, `WorldJeiDump`, `WorldJeiMobBuckets`, `WorldJeiToolPairs` | `dump/` | `/cdevdumpnames`, `/cdevdumpicons`, `/dumpworldjei` |
| PM `worldgen/SpawnDump` | `dump/` | `/dumpspawns` backing class (script loads the new class name) |
| PM `perf/PerfCommand` | `perf/` | `/cdevperf`; `PerfEvents`, `PerfJfrEvents`, `MobCapLevels` stay (Common perf mixins use them) |
| new 2026-11-15 `perf/PassBenchCommand` | `perf/` | `/cdevbench passthrough [calls]` and `... enabled <bool>`: pass-through micro benchmark and master switch (blockrunner.md 3.10, plan_passthrough.md 7.1); registered only with Creative Compat loaded |
| PM `worldgen/MassPlacer` | `cdevplace/` | `/cdevplace`; `CEConfig.VerticalAlign` and the `WorldgenConfig.place*` readers moved into `DevtoolsConfig`; `BiomeLayoutTuner.WORLDGEN_DIR` and `PoolTuner.parseLenient` made public for it; its offline test is `tools/worldgen_test/com/SaintAlfonzo/creativedevtools/cdevplace/MassPlacerTest.java` (`tools/test_worldgen_tools.sh` test 2) |
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

### Structure Catalogue (added 2026-11-09)

World type "Structure Catalogue" (`data/creativedevtools/worldgen/world_preset/structure_catalogue.json`): on first start it lays out every structure by family. `/cdevplace catalogue build | preview | status | stop`, switch `[features] structureCatalogue`, rules `kubejs/generation_scripts/worldgen/structure_world.json`, report `kubejs/exported/structure_catalogue_report.json`. Details and tests: [plan_structure_catalogue.md](plan_structure_catalogue.md).

### /cdevtest and the perfBot test verbs (added 2026-11-15)

[plan_scratch_testing_expansion.md](plan_scratch_testing_expansion.md) item 3 (TX-20, TX-21, TX-22, owner decision
D4). Built 2026-11-15, harness-tested (`bash tools/scratch_query/cdevtest_harness/run.sh`: parser and line format),
not booted; live rows SQ-TX-01 to -16 in the queue.

**`/cdevtest <verb> [args] [key=value ...]`** (permission 2; code `src/main/java/.../creativedevtools/test/`). Every
line is `[cdevtest] verb=<verb> key=value ...` in `logs/latest.log` (and the reply to a player or RCON caller); a value
with a space, `"` or `=` is double-quoted (a block state prints `state="minecraft:wheat[age=7]"`). Every call ends with
`verb=<verb> done=1`, or `error=... done=0` (bad argument, unloaded position, exception: every Throwable is caught, so
a verb cannot crash the server). Positions are integers; `dim=<id>` picks the level (default: the caller's, the
console's is the overworld). Nothing loads a chunk: an unloaded position is an error (`forceload add` first), box scans
skip unloaded columns and report `skipped_columns`. Item arguments: `id`, `id*N`, `id{snbt}` with single-quoted SNBT
strings (the splitter strips double quotes), `none`. Unknown options and extra words are errors, not ignored.

| Verb | Syntax | Prints |
|---|---|---|
| state | `state X Y Z` | state, fluid, block entity type, shape top, random ticking, block and sky light, biome, game time |
| be | `be X Y Z` | block entity NBT |
| inv / tank / energy | `inv X Y Z [face=up\|...\|null]` (same for tank, energy) | item handler (else Container) slots; fluid tanks with capacity; Forge Energy |
| count | `count X1 Y1 Z1 X2 Y2 Z2 BLOCK\|#TAG` | `n`, `on` (block below each match), `ids` for a tag |
| ages | `ages X1 Y1 Z1 X2 Y2 Z2 BLOCK\|#TAG [prop=age]` | `n`, `mean`, `hist` of an integer property |
| place | `place X Y Z STATE [flags=3]` | setBlock with those flags (18 = no updates) |
| items | `items X Y Z R [keep]` | item entities summed by id, XP orb value; discarded unless keep |
| entities | `entities X Y Z R [type=ID\|#TAG] [nbt]` | one line per entity (type, uuid, pos, health, name, NBT), then `n`, `by_type` |
| use | `use X Y Z [as=NAME] [held=ITEM] [face=up] [hit=dx,dy,dz] [hand=main\|off\|both\|client] [sneak]` | block right click per hand, then both hands and the block state |
| useair | `useair [as=NAME] [held=ITEM] [hand=both]` | the item's air use (`zq useitem`) |
| break | `break X Y Z [as=NAME] [held=ITEM] [keep]` | survival break (BreakEvent, harvest check, tool damage), drops within 3 |
| kill | `kill X Y Z TYPE [n=1] [held=ITEM] [as=NAME] [fire] [keep]` | N mobs summoned as /summon and killed by a player hit of 1000; drops within 4 (XP orbs come 20 ticks later) |
| loot | `loot X Y Z [tool=ITEM] [rolls=1]` | Block.getDrops rolled N times, summed (the block stays) |
| player | `player NAME` | position, rotation, health, food, saturation, exhaustion, air, XP, mode, shift, pose, sprint, using, hands, effects |
| chunk | `chunk X Z` | ticket level, status, tickets |
| randomtick | `randomtick X1 Y1 Z1 X2 Y2 Z2 N [nofluids]` | each randomly ticking block and fluid in the box gets N random ticks, no player needed; `changed`, `equiv_game_ticks` (N x 4096 / randomTickSpeed) |
| blocktick | `blocktick X Y Z [fluid]` | the scheduled tick run now; state before and after |
| tickwarp | `tickwarp N` / `stop` / `status` | the next N server ticks back to back (full ticks: only the wait goes); end line `end=done ticks= ms= tps=` |
| watch | `watch X Y Z TICKS EVERY [what=state\|inv\|tank\|be] [face=]` | a `t=` line every EVERY ticks, then `end=done` |
| recipes, rdetail, bytype, uses, makes | as `/zq` (`TEXT`, `SERIALIZER`, `ITEM`; `max=`) | recipe lines (`part=results\|start\|fields\|stepN` extras in rdetail) |
| food, item, attrs | `food ITEM` etc. | food values read stack-sensitive; item class, stack size, use ticks; attribute modifiers per slot |
| reg, tag, tagsof | `reg KIND REGEX`, `tag KIND TAG`, `tagsof ID` | KIND item, block, fluid, entity, block_entity_type, effect, potion, enchantment, recipe_serializer, menu, sound, attribute, biome |
| potion, states | `potion ID [ING,ING]`, `states` | registered and brewing outputs; block-state count against 2^20 |
| season | `season BIOME[,BIOME...]` | one line per biome: `type` (Creative Calendar's cached season type, `SeasonTypes.tagged`, read reflectively; `null` = untagged), `enabled`; error `Creative Calendar not loaded` without the jar |
| floating | `floating X Z R [dim=]` (R up to 64) | sand, red sand and gravel with air or cave air right below, y 0 to 140: up to 10 `block pos` lines, then `n`, `shown`, `skipped_columns` |
| under | `under X Z RCHUNKS [IDTEXT\|-] [DIM] [earth=TAG]` (RCHUNKS up to 32) | per structure start in the loaded chunks (first 30, sorted by id in each chunk; the SQ-EFB judge, `efb_judge.py under` reads it): `id chunk box pieces cols unloaded_cols air_below air_max over6 water_below water_max base_water base_earth ring foreign`; then `starts`, `skipped_chunks`. Earth tag default `creativeworldgen:earth_fill/earth` |
| nbt | `nbt ITEM` | `item has_tag tag share_tag potion`; a bare id gets `/zq nbt`'s fixed tag `{Potion:"minecraft:water",CustomPotionColor:1}`, `id{snbt}` its own |
| layerdump | `layerdump` | plan_post_scratch_1115b.md D0: every block with an integer `layers` / `layer` / `height` / `snow_layers` / `thickness` property, min, max, collision and outline height per value (pixels), class `in_tag` (`creativecompat:passthrough_layers`), `d1_out` (D1's out rows) or `new` (owner question); `tag_without_property` lines; file `kubejs/exported/passthrough_layers_dump.json` |

Acting player: `as=NAME` is an online player or perfBot bot; without it a Forge fake player of that dimension (profile
`[cdevtest:<dim>]`, one per dimension: a cached fake player keeps its first level) stands above the target, inventory
cleared each call. `tickwarp` pulls `MinecraftServer.nextTickTime` (SRG `f_129726_`, reflection) back to now at the end
of each tick; random ticks near players, block ticks and every tick event run as normal. Random ticks still need a
player near the chunk (bot or real): for a box without one, `randomtick`.

**perfBot test verbs** (`/pbot`, member `members/perfbot`, code `BotTestVerbs.java`, lines `[cdevtest] verb=pbot.<verb>`):
`attack NAME TARGET [weak]` (Player.attack, charged unless weak), `interact NAME TARGET [main|off]`, `punch NAME POS
[FACE]` (left click on a block), `break NAME POS`, `useitem NAME [main|off] [TICKS]` and `eat NAME [main|off]` (a real
ticked use: the bot's own tick counts the use down; end lines `end=finish` with food and saturation, or `end=released`),
`hunger NAME FOOD [SAT]`, `sneak NAME on|off`, `look NAME YAW PITCH`, `lookat NAME X Y Z`, `moveto NAME X Z
[walk|sprint] [TIMEOUT]` (forward input and yaw each tick, the server's physics, a jump when blocked; end line
`end=reached|stuck|timeout|stopped` with ticks, distance and speed; `moveto-stop NAME`), `inv NAME get | set SLOT ITEM
[COUNT] | clear | select SLOT` (0-8 hotbar, 9-35 main, 36-39 armour feet to head, 40 off hand), `vulnerable NAME on|off`
(default off), `pickup NAME on|off` (items and XP orbs; default on). Both flags survive a respawn. **Loading Protection:**
every bot is exempt one tick after it spawns: a HIGHEST-priority login listener puts the bot one block above its target
before that mod records the login spot, so the spawn teleport counts as a move (the mod is not read or patched).

**Creative Compat reload** (plan_post_scratch_1115b.md E2; registered only when `creativecompat` is loaded, classes in
`creativedevtools/compat/`): `/cdevreload compat` (server, permission 3) and `/cdevreloadclient compat` (client
command, the client's own config; registered on a client only) call `CompatConfigReload.reload()` and print
`[cdevreload] compat: <result>`. Forge's watcher misses a save by rename (`sed -i`, atomic-save editors). `/cdevbench
passthrough` now names `Neighbours.SIDES` / `FACES` (Compat keeps `RING` / `CUBE` only as deprecated aliases).

Moved from the KubeJS probes (D4): the reusable verbs of `zq_probe.js`, `c2`, `c3`, `zact`, `wo`, `zp`, `zsneak`,
`leaf_pile`, `leaf_decay` and the generic verbs of the `hh_*`, `s5`, `zc4`, `zp9`, `pwg_p6/7/8`, `zpth`, `ztp` probes;
each file's header names the replacing verb and what stays (a `MIGRATED 2026-11-15` line). `season`, `floating`,
`under` and `nbt` followed on 2026-11-16 (pure parts in `GroundJudge.java`, harness-tested; the verbs in
`TestJudges.java`), and `zq_probe.js` is retired: `prep_srv.sh` no longer copies it, its header maps each `/zq` verb to
its replacement. `nbt` prints the stack's tag as the game holds it; Rhino's view of a stack (`typeof getTag`) has no Java
equivalent and stays a per-feature probe question. Every feature reader stays KubeJS.

## 3. Rules

- Commands keep their names; config keys keep their names and comments.
- patcherMod never imports Devtools. A Common class that needs a probe exposes a hook (section 1).
- Devtools never ships: no `IN_TREE_MODS` row, no payload, side `dev`.
- Recipe ids of the test recipes changed namespace (rule 3.9); item ids did not.

## 4. Tests

Scratch server (SQ-CDV, [scratch_queue.md](testing/scratch_queue.md)) and in game:

- [ ] **CDV-01 Load:** `logs/latest.log` has `[creativedevtools] loaded` with the member list `[cdevperfbot, cdevcapture]`
  and no ERROR for `creativedevtools`; `config/creativedevtools-common.toml` has `[debug.*]` and
  `[worldgen.mass_place]`; `creativeengineering-common.toml` has neither after a start.
- [ ] **CDV-02 Commands:** `/cdevgen list`, `/cdevperf status`, `/cdevplace status`, `/recipescan`, `/dumpworldjei`,
  `/dumpspawns`, `/flowerworld` exist (tab completion) and run; `/dumpall` reports every dump ok.
- [ ] **CDV-03 Scripts:** `logs/kubejs/server.log` has `[creativedevtools] server scripts ready` and no
  `writeExportJson` / `guardThrowable` ReferenceError; the test recipes show in JEI under `creativedevtools:` ids.
- [ ] **CDV-04 Probes:** with `[debug.block_state_sync] enabled = true` a dedicated server writes
  `logs/cdev_block_state_ranges_server.txt`; with `[debug.level_random] probe = true` the log shows no
  `[LevelRandomProbe]` error at world load. Both off again afterwards.
- [ ] **CDV-05 Without Devtools:** with only patcherMod (no Devtools jar), the server starts, no `/dumpall`, no
  `/cdevgen`, no Flower Test world type, removed items still lose their tags (`/recipescan` gone).
- [ ] **CDV-06 Flower Test:** the world type "Flower Test" is listed on a dev client and builds its table.
- [ ] **CDV-07 /cdevtest and bot verbs:** scratch rows SQ-TX-01 to -18, CQ-TXR-01 (layer dump, Compat reload) (queue section "Scratch testing tools, item 3").
  Headless: `bash tools/scratch_query/cdevtest_harness/run.sh` prints `PASS 27` (2026-11-15).

## 5. Install (game closed)

`./gradlew :creativeCommon:installToMods :creativeDevtools:installToMods --offline` together: patcherMod without Devtools
loses the tools, Devtools without the new patcherMod fails on the missing hooks. Not part of any player payload.

## 6. History

- 2026-11-01: carved (wave 1, beside Calendar). Framework, Java and script moves, hooks in Common, dev-only side,
  perfBot and clientCapture nested. Compile-verified; not installed or booted.
- 2026-11-15: `/cdevtest` (world, data, tick warp and random tick verbs) and the perfBot test verbs with the Loading
  Protection exemption (scratch testing expansion item 3). Built, harness PASS, not installed or booted; SQ-TX queued.
  Same day 15:50: `/cdevtest layerdump` (pass-through D0), `/cdevreload compat` and `/cdevreloadclient compat` (E2),
  `/cdevbench` on `Neighbours.SIDES` / `FACES`.
