# Pack tooling: dictionaries, recipe and tag helpers, CEConfig, dump commands, generators, tests, builds

The pack's own infrastructure: what every feature script is built on, and the tools used to inspect, generate and
test the pack. This is a hub. Each piece gets its contract, entry points and gotchas here; the feature writeups own
the feature-specific uses. Deploy tooling is in [deploy.md](deploy.md), the performance report in [perf.md](perf.md).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=5aefd516b4db -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 8 open TODO rows (Planned 3, Testing 5), 1 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `tooling/dictionaries #2` | Planned | S | Block, fluid and NCH fluid tags not validated |
| `tooling/generators-and-tests #2` | Planned | none | Headless tests need this machine's caches |
| `tooling/generators-and-tests #3` | Planned | S | Removed items keep their recipes |
| `tooling/conventions #1` | Testing | S | Raw `e.custom` and `e.add` against the helper rule |
| `tooling/conventions #2` | Testing | S | Define-once violations |
| `tooling/conventions #6` | Testing | S C | Test recipes shipped |
| `tooling/dictionaries #1` | Testing | S~ | item_dict tag check runs before tags load |
| `tooling/generators-and-tests #1` | Testing | S | More tools in `/cdevgen` |
<!-- generated:status_index end open_work -->

| Part | Status |
|---|---|
| Dictionaries: `global.item` (12,511 keys), `global.blocks`, `global.fluid`, effect and multiblock dicts, `global.removedItems` (309) | in use since the initial commit; startup validation logs missing ids (tag keys: F1, F2) |
| Recipe helpers `global.NCH` (about 60 recipe types, id generation, validation, `strict` mode, `bulk`, `cfg`) | in use |
| Tag helper `global.tags` (add/remove, wildcard `addMatching`, families, mineable, storage blocks, overlaps, mirror) | in use |
| `CEConfig` (Creative Common): one Forge common config, 23 sections, live-editable in Create's config screen; `NCH.cfg` reads recipe values from it | in use ([gear_tuning.md](gear_tuning.md) for `NCH.cfg`) |
| 24 `/dump*` commands writing `kubejs/exported/`, and `/dumpall` | in use |
| `/cdevgen`: runs `tools/generators.json` (50 Python generators and reports, 2026-10-31) in game, optionally at startup | in use; startup run off |
| `tools/pack_paths.py`: shared instance paths, item_dict parser, lenient JSON loader, vanilla / Forge jar locator for the Python tools | in use since 2026-10-27 |
| Headless tests: 9 `tools/test_*.sh` suites (Java on Creative Common classes, the pack's Rhino fork, node sims, Python), plus GameTest projects | in use; machine-specific paths (F7) |
| Root Gradle build: one build for the 5 in-tree mods, `ce.forge-mod` convention plugin, `installToMods` | in use |
| Item-loss repro harness (`tools/item_repro/`) | ran 2026-09-30; found the Sophisticated Backpacks wipe ([plan_1.0.3.md](plan_1.0.3.md) 4.7) |
| Slice & Dice `_manual_only` recipe ids | in use (2 recipes) |

- **Pending (2026-10-31):** F5, F8 and F9 built 2026-10-27 and shipped in 1.1.04 (client 2026-10-29), their checks not
  run (TL-09; PH tests in [pack_hygiene.md](pack_hygiene.md)); F1 checked on a scratch server (SQ-INV-02), TL-01's
  empty-tag half open; TL-02 to TL-16 not run. Open findings: F2, F4 (decided, not built), F7, F10; F3 deleted, F6 fixed
  (section 4).
- **Files to open:** [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js),
  [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js),
  [tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js),
  [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java),
  [dump_all.js](../kubejs/server_scripts/_lib/dump_all.js), [generators.json](../tools/generators.json).
- **The rules** (CLAUDE.md): recipes through `global.NCH`, tags through `global.tags`, every id defined once in a
  dictionary and referenced by key, and one writeup per feature.

## 1. Overview

KubeJS 1902.6.2 (Rhino fork) runs three script sets: `startup_scripts` (once, at game start), `server_scripts`
(every datapack load, `/reload`) and `client_scripts`. Every server script shares one top-level JS scope, so a
top-level `const` with the same name in two files kills the second file. Startup scripts load in filesystem-walk
order within a priority, so anything read at top level must come from a higher `// priority:`.

Creative Common (`creativeengineering`) is the pack's Forge coremod. It carries the Java side of most features, the
config, the Java dump and dev commands, and its jar is built by the root Gradle build.

## 2. Reference

### 2.1 Dictionaries (`startup_scripts/_lib/`, priority 1000)

| File | Global | Holds | Validation (`StartupEvents.postInit`) |
|---|---|---|---|
| `item_dict.js` | `global.item` (`i`) | item ids, `#tag` strings, and object ingredients (NBT matchers such as `i.water_bottle`) | missing items logged as `[item_dict] Missing/invalid item IDs`; tag keys checked too, probably too early (F1) |
| `block_dict.js` | `global.blocks` (`b`) | block ids and block tags | missing blocks logged; tag keys only warned "not validated" |
| `fluid_dict.js` | `global.fluid` (`f`) | fluid ids and fluid tags | same as blocks |
| `effect_dict.js`, `multiblock_dict.js` | | effect ids; IE / Create multiblock ids | |
| `removed_items.js` (priority -10000) | `global.removedItems` | items the pack takes out | see below |

- **Conventions:** keys are short, unique, mod-prefixed when two mods clash (`i.ce_copper_shield` vs Everything Copper's
  `i.copper_shield`; `i.q_dragon_scale` vs DragonLoot's). `t_` = a tag key (`i.t_bakeware`). Generated sections are
  marked (`===== create - 502 new =====`, placeable food from `tools/placeable_catalog.py`).
- **One key per id (2026-10-27, pack_hygiene finding 20):** no id or tag is assigned under two different keys; the only
  `i.x = i.y` lines left are the `i.none` / `i.na` placeholders of the metal chains. 77 item ids and 9 tags had 2 to 4
  keys; one was kept per id (most code uses; dynamic uses such as `i['wire_coil_' + m]` count; ties go to the chain
  convention) and every use rewritten, 96 keys dropped. Notable: `wire_coil_<metal>` (not `coil_<metal>`),
  `coil_block_<metal>` (not `wire_coil_block_<metal>`), `ds_ore_<metal>` (not `deepslate_ore_<metal>`),
  `explorers_compass`, `natures_compass`, `bonemeal`, `slime`, `eggs`, `milk`, `planks`, `cabbage`, `ore_netherite`,
  `raw_netherite`. The scan and the full mapping were one-off (session scratchpad); re-check with a parse of the
  `i.key = 'value'` lines grouped by value before adding a key for an id that may already have one.
- **State:** 12,511 item keys (2026-10-18 count); an "Unsorted (Possible
  duplicates)" section near the top; 911 candidate entries and 689 unmapped ids still in
  [startup_scripts/todo/](../kubejs/startup_scripts/todo/) ([TODO](todo/TODO.md) `tooling/dictionaries #3`).
- **Removed items:** hidden in JEI and given a red "REMOVED ITEM: DO NOT USE" line
  ([jeichanges.js](../kubejs/client_scripts/jeichanges.js)), stripped of every item tag, and every recipe still naming
  one can be listed by [recipe_scan.js](../kubejs/server_scripts/_lib/recipe_scan.js) (`/recipescan`, or once per
  session at load with CEConfig `[debug.recipe_scan] runAtLoad`; off by default; feeds `todo/recipes_to_remove.txt`). The recipes themselves are removed by hand in the feature scripts.

### 2.2 Recipe helpers (`global.NCH`, [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js))

- **Shape:** `R.<type>(output, input(s), e, idOverride?, ...)`. Every recipe gets the id
  `creativeengineering:<type>/<idSuffix>`; the suffix is derived from output and input (`toId`) unless given.
- **Validation:** every item, tag and fluid is checked (`_validateAny`). An unknown item, an empty tag or an unset dict
  key (`undefined`) throws with the recipe's context while `strict` is true (default), else warns. Fluid tags aren't
  validated (F2).
- **Types covered:** vanilla (shaped, shapeless with damaged-ingredient and can-servings options, smelting, blasting,
  smoking, campfire, stonecutting, smithing), Farmer's Delight (cooking, cutting), the pack's own (`ceBaking`,
  `dragonBreathing`, `elytraDuplication`, the echo / felsic / dragon assemblies), Nock Enough Arrows fletching, Create
  (crushing, milling, pressing, compacting, mixing, saw, deploying, item application, haunting, splashing, filling,
  mechanical crafting, sequenced assembly + incomplete items), Create Crafts & Additions (rolling, charging),
  Create: Vintage Improvements (vibrating, laser cutting, polishing), extruding, IE (metal press, crusher, alloy, arc
  furnace, squeezer, bottling, blueprint, sawmill), PneumaticCraft (pressure chamber, explosion crafting, thermo
  plant), Farmer's Respite kettle (`frBrewing`), hammering, mortar and food grinding, Sophisticated Backpacks skin
  smithing (`backpackSkin`, `backpackSkinClear`).
- **Utilities:** `remove`, `replaceInput`, `replaceOutput` (option `includeAdded`, below), `bulk(name, rows)` (call a helper once per row),
  `cfg(defaults, 'CRAFTING.X')` (values from CEConfig), `incomplete(key)` / `registerIncomplete` (transitional items
  from [transition_items.js](../development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/changes/transition_items.js)).
- **Known gaps** (documented at each helper): `ieSawmill` can't do secondaries (a datapack JSON instead) (F9).
- **`includeAdded` (2026-10-22):** KubeJS 1902.6's `replaceInput` / `replaceOutput` swap only recipes loaded from
  data, never script-added ones. `R.replaceInput` / `R.replaceOutput` with `includeAdded` also swap the script-added
  recipes (item merges use it: `[itemMerges] 2 swaps in script-added recipes`). Opt-in: a swap replaces the whole
  matching slot, so sugar -> `#forge:sugar` on added recipes turned a `[sugar, honey]` slot into the tag alone.

### 2.3 Tag helper (`global.tags`, [tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js))

Works for any registry's tag event (`ServerEvents.tags('item' | 'block' | 'fluid' | 'entity_type' ...)`):

| Call | Does |
|---|---|
| `add(e, tags, entries)` / `remove(...)` | one or more tags × one or more entries |
| `addMatching(e, tags, entries, { except })` | `*` / `?` wildcards inside an id half (`'minecraft:*_concrete'`), `'#tag'` nested references (live), `'@mod'`, `/regex/`; `except` removes afterwards (and expands nested tags so it can) |
| `addTag`, `addFluid`, `removeTags`, `wipeTag`, `clearAll` | variants with options |
| `tagChain`, `materialFamily`, `metalPart`, `storageBlock`, `mineable(e, tool, level, blocks)` | the forge `materials/x`, `storage_blocks/x`, `mineable/<tool>` + `needs_<level>_tool` patterns |
| `overlaps(e, tags, label)` | logs every id in more than one of the given tags (BlockRunner tiers) |
| `mirror(e, tags, rename)` | copies a tag's members to a renamed tag |

### 2.4 CEConfig (`config/creativeengineering-common.toml`)

One Forge common config built in `CEConfig.java`, 23 sections: `forgiving_void`, `dragon_gear`, `air_brake`,
`crafting`, `magic_resistance`, `armor_enchantability`, `shrapnel`, `armor_overflow`, `shields`, `damage_tags`,
`bonemeal_charm`, `surface`, `debug`, `worldgen`, `canning`, `cooking`, `trash_slot`, `wandering_trader`, `perf`,
`jei`, `crop_growth`, `gui_title`, `seasons`.

- **Live:** Forge reloads the file on save and Create's Configs screen edits it in game (a string-field patch,
  `SubMenuConfigScreenMixin` + `StringEntry`, makes text values editable there). Code reads values each use, so most
  changes need no restart; values baked into recipes need `/reload`, values read at registration need a restart (the
  writeups say which).
- **From KubeJS:** `Java.loadClass('com.SaintAlfonzo.creativeengineering.CEConfig')`, then
  `CFG.SECTION.field.get()`; always wrapped so a missing jar degrades to defaults.
- **New keys** appear in the toml only after the new jar has run once.
- **`configs_edited/root_config/`** keeps a copy of every hand-edited config.

### 2.5 Dump commands (op level 2)

All write under `kubejs/exported/` (not committed; [export_io.js](../kubejs/server_scripts/_lib/export_io.js) creates
folders through Creative Common because KubeJS can't).

| Command | Writes / used by |
|---|---|
| `/dumpbiomes`, `/dumpclimate`, `/dumpclimatecaves`, `/dumpspawns`, `/dumpstructures`, `/dumpstructuregen` | biome, climate, spawn and structure data ([weather.md](weather.md), [mob_spawns.md](mob_spawns.md), [structure_tuning.md](structure_tuning.md)) |
| `/dumpitemsbymod`, `/dumptags`, `/dumprecipes`, `/dumpcraftingaudit`, `/dumpfluids`, `/dumppotions`, `/dumpnames` | registries, tags, recipes, names ([jei_coverage.md](jei_coverage.md), [names.md](names.md)) |
| `/dumpworldjei` | world-interaction JEI facts (Creative Common `devtools/WorldJeiDump`; feeds `tools/gen_jei_world.py`, [jei_world_tabs.md](jei_world_tabs.md)) |
| `/dumpgear`, `/dumpstacks`, `/dumpfood`, `/dumpplaceable`, `/dumpcropgrowth`, `/dumpcompost`, `/dumppaintings` | per-feature tables ([gear_tuning.md](gear_tuning.md), [stack_sizes.md](stack_sizes.md), [food.md](food.md), ...) |
| `/dumploot`, `/dumptrades`, `/loottuning` | loot and trades ([loot.md](loot.md), [wandering_trader.md](wandering_trader.md)) |
| `/dumpbeenests [reset]` | measured nests per biome ([bee_nests.md](bee_nests.md)); **not** in `/dumpall` (a session that generated nothing would overwrite a measurement) |
| `/dumpall` | runs every dump above except `/dumpbeenests` through the real dispatcher, in order (quick registry dumps first, `/dumpclimate` and `/dumpclimatecaves` last; `/dumpclimate` takes about 4.5 minutes at its 20,000-column default); a failing dump is reported and skipped |

[java_guard.js](../kubejs/server_scripts/_lib/java_guard.js) `guardThrowable(fn)` catches `java.lang.Error`s that
Rhino's `try/catch` can't (a `NoClassDefFoundError` used to kill `/dumprecipes` silently).

### 2.6 Generators and `/cdevgen`

[tools/generators.json](../tools/generators.json) lists 50 Python generators and reports (45 on 2026-10-27; silk touch blocks, UA coral growths, item physics audit and crop season audit added since) in run order, each with `groups`
(`core`, `worldgen`, `loot`, `client`, `pack`, `reports`, `names`, `food`, `blockrunner`, `jei`, `stacks`, `slow`), the `dumps` it
reads, its `outputs` and an `apply` hint. Creative Common's `devtools/GeneratorRunner` runs them as child processes from the
instance root, hashes the outputs before and after, and reports changes.

| Command | |
|---|---|
| `/cdevgen` | list generators, groups, last run |
| `/cdevgen run [target] [dumps]` | `all`, a group or an id (default `[debug.generators] target` = `core`); `dumps` first runs the `/dump*` commands those scripts read |
| `/cdevgen status` / `cancel` | progress or last results; kill the running script and skip the rest |

Output goes to chat (status lines and highlighted lines), `latest.log` (`[cdevgen:id]`) and `logs/cdevgen/<id>.log`.
`[debug.generators] runAtStartup = true` runs the target in the background from common setup and waits for it at
load complete (up to `startupWaitSeconds`), so the first world load reads fresh files; results go to the first player
to join. Python and timeout are configurable (`python`, `timeoutSeconds`).

Every other `tools/*.py` generator or report is named in the manifest's `_readme` with the reason it runs by hand
(needs arguments or a choice, writes mod resources that need a jar rebuild, a `--check`-first hand workflow, a check
that only prints, a library, or a release step). `dump_server_mods.py` is the only way to fill `server_mods/` and stays
out (it copies ~300 jars). Entries added 2026-10-27 (pack_hygiene 44): `nether_swaps`, `meat_block_swaps`,
`nether_blocks_scan`, `nether_worldgen_scan`, `wood_mismatch_scan`, `loot_breakdown`, `jei_coverage`,
`stack_size_rules` (`gen_stack_sizes.py`), `stack_sizes`, `food_catalog`, `can_audit`, `flower_pot_audit`,
`wandering_trader_report`, `texture_audit`, `jei_order_report`. Earlier: `soup_soft_fluids`
([veggies_delight.md](veggies_delight.md)) and `jei_world` ([jei_world_tabs.md](jei_world_tabs.md)).

**Shared Python module** [tools/pack_paths.py](../tools/pack_paths.py) (standard library only, cheap to import):
`INSTANCE`, `MODS`, `ITEM_DICT`; `parse_item_dict()` (source, key -> value with the last assignment winning, id ->
keys without `#tag` values; any indent, either quote; `food_catalog.parse_dict()` wraps it),
`item_dict_first_keys()`, `item_dict_assignments()`; `loads_lenient()` (BOM, `//` and `/* */` comments outside
strings, trailing commas); `MC_VERSION` / `FORGE_VERSION` from `instance.json`, `vanilla_jar()` (the launcher
libraries' resources-only `-extra` jar first, then the full client jar, `~/.minecraft`, the ForgeGradle cache),
`forge_jar(kind)` (the instance's Forge `universal` / `client` jar) and `forge_mapped_jar()` (ForgeGradle's Mojmap jar,
for `javap`). En_us tables come from `lang_index.build()` (vanilla, Forge universal, upstream jars, mods with their
jar-in-jar members, kubejs, client script renames, resource packs); `effect_audit.py` and
`gen_effect_descriptions.py` use it. Structure template containers come from `loot_breakdown_scan.scan()` (cached),
which `audit_loot_containers.py` uses too.

### 2.7 Headless tests

| Script | Covers | Writeup |
|---|---|---|
| `tools/test_shields.sh` | `shield_check.py` + `ShieldConfigTest` | [shields.md](shields.md) 6 |
| `tools/test_crop_growth.sh` | growth maths + coverage | [crops.md](crops.md) 2.9 |
| `tools/test_wild_crops.sh`, `test_wild_flowers.sh`, `test_bee_nests.sh`, `test_mob_spawns.sh` | generator vs the real KubeJS worldgen script run in the pack's Rhino fork | [crops.md](crops.md), [flowers.md](flowers.md), [bee_nests.md](bee_nests.md), [mob_spawns.md](mob_spawns.md) |
| `tools/test_structure_groups.sh` | node sim vs Rhino, byte-identical | [structure_tuning.md](structure_tuning.md) 6 |
| `tools/test_worldgen_tools.sh` | 9 Java/node/Python checks on the worldgen code | [structure_tuning.md](structure_tuning.md) 6 |
| `tools/test_remainder_buffer.sh` | GameTests in a scratch ForgeGradle project with FD and Respite | [remainder_buffer.md](remainder_buffer.md) 6 |

- **How they run:** Java tests compile against `development/creativeCommon/build/classes` (build first) on a classpath
  from [java_test_cp.sh](../tools/java_test_cp.sh): the mapped Forge jar in the ForgeGradle cache plus libraries
  from ATLauncher's folder (F7). Rhino tests run the real `.js` files in the pack's Rhino fork through a small Java
  runner (`tools/*_test/Rhino*.java`).
- **Node sims** without a runner script: `tools/blockrunner_tag_sim.js`, `loot_tuning_sim.js`,
  `structure_tuning_sim.js`, `structure_density_check.js`.
- **GameTests:** the Gradle runs set `forge.enabledGameTestNamespaces` to the mod id; the oven's 8 tests ran in a
  scratch project ([oven.md](oven.md) 6).

### 2.8 Root Gradle build

- `settings.gradle` includes Creative Common, `exposureBackport`, `incubationAdditions`, `moreMooblooms`,
  `backportedBrewin`, `backportedVinery`, `babyMobs`, `creativeLabels`, `backportedVeggies` from `development/`; project names stay the bare mod names (`:creativeCommon`).
- `build-logic/` holds the `ce.forge-mod` convention plugin (ForgeGradle setup, official 1.19.2 mappings, Java 17,
  mods.toml expansion from `<mod>/gradle.properties`, jar manifest with mixin configs, dev runs, `installToMods`) and
  `ce.forge-mod-refmap` for mods with real Mixin refmaps (below).
- Shared versions in the root `gradle.properties` (Forge 43.3.9, `org.gradle.java.home`); per-mod ids and versions in
  each mod's `gradle.properties` (`replaces_jars` moves a replaced third-party jar out of `mods/`).
- `./gradlew :creativeCommon:installToMods` builds and swaps the jar into `mods/`, moving the old one to
  `development/<mod>/jar_archive/displaced/`. `development/<mod>/libs/` (compile-only copies of mod jars) is
  gitignored. The root [README.md](../README.md) has the commands.
- Creative Common has no refmap and only runs reobfuscated: its mixins on vanilla classes use SRG names with
  `remap = false`, and reflection uses SRG strings (`m_20115_`, `f_43494_`). Mods like it set
  `disable_target_validator=true` (`-AdisableTargetValidator=true` on compileJava; Creative Common, babyMobs,
  clientCapture, incubationAdditions, moreMooblooms, creativeDelight; `ce.forge-repack` always adds it).
- Every `ce.forge-mod` project gets the `libs/` flatDir repository (`local:<file name without -version>:<version>`);
  a build.gradle adds only other repositories (mavenCentral).
- **Refmaps** (`ce.forge-mod-refmap`, 2026-10-27, pack_hygiene finding 41): a mod with Mojang-named mixins sets
  `refmap_name=<mod>.refmap.json` (backportedMiners, backportedFungi, backportedVinery, backportedVintage,
  backportedBrewin, creativeLabels, exposureBackport); exposureBackport adds
  `refmap_source_sets=expanded:...,polaroid:...,catalog:...` for the addon source sets its build.gradle creates. The
  plugin does the MixinGradle `add` and `config` (from `mixin_configs`), declares each refmap as a `mixinRefmap`
  output of its compile task (a missing refmap re-runs the compile), and after `jar` fails if `refmap_name` is not in
  the jar or a mixin config that lists mixins names a refmap the jar lacks.
- **Upstream-resource guard** (`ce.forge-mod`, 2026-10-27, finding 42): `resource_guard_jars` (root-relative jar
  paths, `*` in the file name = first match), `resource_guard_own_files`, `resource_guard_name` and optional
  `resource_guard_arr=true` add `checkNoUpstreamResources` (or `checkNoArrResources`) to `check` and `jar`: the build
  fails if `src/main/resources` has a path that is also in one of the jars, except the own files. Used by
  backportedMiners, backportedVinery (two jars; a clash names its jar) and Ender's (Rustic until its 2026-11-10 bake, which ships the MIT files) (ARR: the installed
  `mods/endersdelight-*.jar`). Not `upstream_jar`, which is the donor or repack jar of `ce.forge-repack` and the port
  tools.
- **Shared Java sources** (`development/shared/<module>/`, 2026-10-27, pack_hygiene findings 36 and 38). Code that
  several in-tree jars need is written once and compiled into each jar under that jar's own package, so no mod needs
  another at runtime. A mod lists `shared_sources=<module>,...` and `shared_base=<its base package>` in its
  `gradle.properties`; `ce.forge-mod`'s `sharedSources` task copies the modules into
  `build/generated/sources/shared/`, moving the template packages `ce.shared` / `ce.mixin` to
  `<shared_base>.shared` / `<shared_base>.mixin` (text replace on package, imports and references), and adds that to
  the main source set. Edit only the templates; the copies are regenerated on every build. Modules:

  | Module | Classes | Used by |
  |---|---|---|
  | `packs` | `BuiltInPacks` (`addFolder`: a `packs/<...>/` folder of the jar; `addMemory`: an in-memory pack of one pack type; `addUpstream`: a port's `<prefix>_assets` / `<prefix>_data` pair at the bottom), `MemoryPackResources` (path -> bytes `PackResources`, description from the caller) | backportedMiners, backportedVinery, Rustic (CD member), creativeLabels (`DonorArt`), Creative Common (`worldgen/donor/FarmersDonor`), moreMooblooms, incubationAdditions |
  | `upstream` | `DonorJars` (`Jar` record, `verifyOrFail`, `read` by entry prefix, `readEntry`, `sha1`). Each port keeps its own `upstream/UpstreamJars.java` with the `new Jar("file", "sha1", ...)` pins, which `tools/build_deploy.py` reads | backportedMiners, backportedVinery, Rustic |
  | `mixinplugin` | `GatedMixinPlugin`: a mixin applies only when its mod is loaded, named by a compartment package (`mixin.<modid>.`, optional exact version) or by a simple-name table (any of several mods, each with an optional tested Maven version range, outside it the mod counts as absent with one WARN; Creative Common's `MOD_RANGE` since 2026-11-05); subclasses add `postApply` edits | creativeDelight (`CDMixinPlugin`, compartments + the Fruits Delight bytecode edits), moreMooblooms, incubationAdditions, creativeLabels (tables) |
  | `stations` | `LeftoverPlan` (where a cooking station's ingredient leftovers go: container slot up to a stack, then the overflow slots, the rest returned), `StationContainers` (a station's plating containers from the live recipe manager, rebuilt on reload; container-and-ingredient items logged and left out) ([remainder_buffer.md](remainder_buffer.md) 3.8) | creativeDelight (FD pot, kettle, oven), backportedMiners (copper pot), backportedVintage (Pickling Jar) |
  | `itemhidden` | `mixin.ItemHiddenMixin` (`Item.allowedIn`, SRG name, no refmap) + `shared.HiddenItems` (the mod sets its test at construction) | moreMooblooms (Buzzier Bees buttercup and egg, only with Buzzier Bees), creativeLabels (`DonorItems`) |

### 2.9 Item-loss repro harness

[tools/item_repro/](../tools/item_repro/README.md): a scratch dedicated server and a throwaway client on one machine
with a lag proxy between them; a client script makes random inventory actions, a server script counts every item
each tick, `summarize.py` reports losses and desyncs per phase. Run 2026-09-30 (about 50,000 actions): 3 permanent
losses, all the Sophisticated Backpacks storage wipe, no vanilla click path lost items. Results and recovery in
[plan_1.0.3.md](plan_1.0.3.md) 4.7.

### 2.10 Slice & Dice `_manual_only`

Slice & Dice copies every Farmer's Delight cooking-pot recipe into a heated Create mixing recipe after KubeJS has run,
so `R.remove` can't reach the copy. It skips recipe ids ending in `_manual_only`. The pack uses that to keep a pot
recipe off the mixer and give the mixer its own ratio: `cooking_oil_manual_only` (pot) next to
`cooking_oil_heated_mixing` ([cooking.js](../kubejs/server_scripts/cooking/cooking.js) line 293), and
`wood_pellets_manual_only` ([sawdust.js](../kubejs/data/creativeengineering/kubejs/wood/sawdust.js) line 57). The crab bars id uses the
suffix too, though it's a shapeless recipe.

## 3. What the pack changes

Everything here is the pack's own; nothing replaces a mod. The conventions and their reasons:

- **Dictionaries first:** an id typo or a mod update that renames an item shows up once, at startup, instead of as a
  silently missing recipe; renaming an item is one line.
- **Helpers own recipe JSON:** one place knows each mod's JSON shape and its quirks (KubeJS schemas that strip NBT,
  IE's tag-only fluid inputs, Create's count fields), and every recipe is validated and gets a predictable id.
- **CEConfig over KubeJS config:** Forge's format is editable in game through Create's screen and readable from Java
  and KubeJS alike.
- **Generators over hand-written data:** structure, biome, loot, flower, bee-nest, spawn and name data is generated
  from dumps, so a mod update is a re-dump and a re-run.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1.** CHECKED 2026-10-26 (SQ-INV-02): not the case, the startup check listed 19 item ids and 0 tag keys; whether an empty tag key is still reported is open ([TODO](todo/TODO.md) `tooling/dictionaries #1`). **`item_dict` probably reports every tag key as missing.** Its tag check runs at `StartupEvents.postInit`, before
  any datapack (and so any tag) has loaded, so `Ingredient.of('#...').itemIds` is empty for all 238 tag keys. Check
  `logs/latest.log` for `[item_dict] Missing/invalid item IDs` (TL-01). If so, move the tag half to a server-side
  check (`ServerEvents.tags` or the first recipe event).
- **F2. Block, fluid and NCH fluid tags aren't validated** (each logs "not validated (tag support not yet
  implemented)"). In [TODO](todo/TODO.md) `tooling/dictionaries #2`.
- **F3.** DELETED 2026-10-22. **`global.config` was dead.** `func/config.js` (a self-healing JSON config under
  `config/creativeengineering/`) has no caller; dragon autorepair, its first user, moved to CEConfig, and the folder
  didn't exist.
- **F4.** DECIDED 2026-10-29: a config option, on for now ([TODO](todo/TODO.md) `tooling/conventions #6`, not built). **Test recipes ship in the pack. Your call.** In `server_scripts/_lib/`:
  - `metal_test_recipes.js`: a mechanical crafting recipe per metal (15 metals + netherite) that takes one of every form
    of the metal (ore to component, up to 20 items) for 1 ingot;
  - `wood_test_recipes.js`: the same idea for wood;
  - `sequence_assembly_processes.js`: a nether star -> nether star sequenced assembly using every legal assembly step.

  They show in JEI and are craftable (each is a loss, so no exploit). Keep as showcases, or gate them.
- **F5.** FIXED 2026-10-27 (pack_hygiene 44). **Many Python tools aren't in `generators.json`:** every generator or
  report is now an entry or named in the `_readme` with its reason (2.6).
- **F6.** FIXED 2026-10-19 (all three). **Stale references:** `test_crop_growth.sh` names `feature_writeup/crop_growth.md` (it's [crops.md](crops.md)
  2.9); `neoCraftHelper.js` line 1 says `kubejs/server_scripts/helpers/recipes.js`; the `NCH.cfg` example uses toml
  keys instead of field names ([gear_tuning.md](gear_tuning.md) F12).
- **F7.** Open ([TODO](todo/TODO.md) `tooling/generators-and-tests #2`). **Headless tests need this machine's caches.** `java_test_cp.sh` reads `~/.local/share/atlauncher/libraries` and
  the ForgeGradle cache's mapped Forge jar; the remainder-buffer GameTests download FD and Respite. A fresh clone (or a
  cloud session) can't run the Java parts until those exist.
- **F8.** FIXED 2026-10-27 (pack_hygiene 20, [TODO](todo/TODO.md) `tooling/conventions #2`). **Duplicate dict keys:** 55 keys are assigned twice (same id each time). Harmless; the "Unsorted (Possible
  duplicates)" section is where to start.
- **F9.** BUILT 2026-10-27 (pack_hygiene 13 to 17, [TODO](todo/TODO.md) `tooling/conventions #1`; leftovers listed there). **Convention drift found by the writeups:** raw `e.custom` in `potion_bottling.js` and `tipped_arrows.js`
  (`NCH.ieBottling` exists; a `createEmptying` helper doesn't), raw tag `e.add` in `equipmenttags.js` (44 lines) and
  literal entity ids in `entitytags.js` (no entity dict). See [potions.md](potions.md) F6, [archery.md](archery.md)
  F4, [mob_block_tweaks.md](mob_block_tweaks.md) F1.
- **F10.** Open by design ([TODO](todo/TODO.md) `tooling/generators-and-tests #3`). **Removed items don't remove their recipes.** `recipe_scan.js` only lists them; each removal is by hand. Fine
  as a design, but a removed item's recipe survives until someone acts on the list.

### Known limits

- **Rhino gotchas** (each documented where it bit): shared server-script scope; loop-body `const` / `let` behave like
  `var`; same-arity Java overloads are ambiguous (`PotionUtils.getColor`, `ItemStack.is`); some Mojang names are
  hidden (`getTag`, `getUUID`) and some fields shadow getters (`level`, `x`); `.class` doesn't exist
  (`__javaObject__`); `try/catch` misses `java.lang.Error`.
- **KubeJS class filter** blocks `java.io` / `java.nio`, so file copies and folder creation go through Creative Common.
- **Startup changes need a restart;** server scripts and tags reload with `/reload`.

### Rejected options

- **Per-feature JSON configs (`global.config`):** replaced by CEConfig for the in-game editor (F3).
- **Running generators only by hand:** `/cdevgen` keeps the dumps and generators in step and shows what changed.

## 5. File map

- Dictionaries: [startup_scripts/_lib/](../kubejs/startup_scripts/_lib/) (`item_dict.js`, `block_dict.js`, `fluid_dict.js`, `effect_dict.js`, `multiblock_dict.js`, `removed_items.js`); worklists in [startup_scripts/todo/](../kubejs/startup_scripts/todo/).
- Helpers: [startup_scripts/func/](../kubejs/startup_scripts/func/) (`neoCraftHelper.js`, `tag_helper.js`, plus the feature helpers: `tune_tools.js`, `armor_effects.js`, `compost.js`, `trade_helper.js`, `jei_order.js`, `item_physics.js`, ...); [changes/transition_items.js](../development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/changes/transition_items.js).
- Server `_lib/`: the `dump_*.js` commands, `dump_all.js`, `export_io.js`, `java_guard.js`, `recipe_scan.js`, `loot_dump.js`, `structure_dump.js`, `villager_trades_dump.js`, and the test recipes (F4).
- patcherMod: [CEConfig.java](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CEConfig.java), [devtools/](../development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/devtools/) (`GeneratorCommand`, `GeneratorRunner`, `BlockStateSyncDebug`).
- Tools: [tools/generators.json](../tools/generators.json), `tools/*.py` (generators, reports, scans), `tools/test_*.sh` + `tools/*_test/`, [java_test_cp.sh](../tools/java_test_cp.sh), [pack_paths.py](../tools/pack_paths.py) (shared paths, item_dict parser, lenient JSON, jar locator), [lang_index.py](../tools/lang_index.py) (every en_us table), [tools/item_repro/](../tools/item_repro/README.md), [tools/perf/](../tools/perf/) ([perf.md](perf.md)), [build_deploy.py](../tools/build_deploy.py) ([deploy.md](deploy.md)), [gen_pack_manifest.py](../tools/gen_pack_manifest.py), [dump_server_mods.py](../tools/dump_server_mods.py), [decompile_mod.py](../tools/decompile_mod.py).
- Build: [settings.gradle](../settings.gradle), [gradle.properties](../gradle.properties), [build-logic/](../build-logic/), [README.md](../README.md).

## 6. Offline checks

- The `tools/test_*.sh` suites (2.7), each against its writeup.
- `node tools/blockrunner_tag_sim.js` runs the real `tag_helper.js` in node against the last `/dumptags` export, so it
  also exercises the tag helper.
- Most generators have a `--check` mode that exits non-zero when their output is stale.
- The dictionary checks only run in game (F1).
- `python3 tools/tab_indent.py` keeps own source files tab-indented (DR-C21, DR-C22): dry run by default, `--apply`
  writes a file only when its parse is equal before and after (`ast.dump`, parsed JSON values, the javac parse tree and
  Rhino's interpreter code through `tools/tab_indent_check/AstFingerprint.java`, acorn under node for node-only JS),
  `--scope` lists what is in scope and why the rest is not, `--lint` is the check_all check `tab_indent`.

## 7. In-game test suite

- [ ] **TL-01 Dict validation (F1):** boot, open `logs/latest.log` -> note whether `[item_dict] Missing/invalid item IDs`
  lists tag keys (`... (tag not found or empty)`), and which real item ids it lists.
- [ ] **TL-02 NCH strict:** add `R.shaped('minecraft:stone', ['A'], { A: 'minecraft:not_an_item' }, e)` to a scratch
  server script, `/reload` -> the script errors with `[NCH] ... does not exist`; with `global.NCH.strict = false` it warns
  instead. Remove the line.
- [ ] **TL-03 Recipe ids:** JEI on any pack recipe, hover the recipe id (advanced tooltips) -> `creativeengineering:<type>/<suffix>`.
- [ ] **TL-04 Removed items:** a removed item (`/give` one, e.g. a NAND gate) -> hidden from JEI, red "REMOVED ITEM" line,
  no item tags (F3+H shows none).
- [ ] **TL-05 CEConfig live edit:** change `[dragon_gear.autorepair] intervalTicks` in Create's config screen -> the
  toml changes on disk without restart. Put it back.
- [ ] **TL-06 String field:** in Create's config screen, `[crafting.felsic_sword] potion` is an editable text field, not
  "Impl missing".
- [ ] **TL-07 `/dumpall`:** run it -> chat ends with `[dumpall] all 22 dumps done`; `kubejs/exported/` timestamps
  updated; `server_mods/` refreshed.
- [ ] **TL-08 A dump failing:** (if one fails) `/dumpall` reports it and carries on with the rest.
- [ ] **TL-09 `/cdevgen` list:** `/cdevgen` -> the 50 generators, the groups (`stacks` among them) and the last run.
- [ ] **TL-10 `/cdevgen run core`:** runs the core group in the background, one chat line per script, changed outputs
  named; `logs/cdevgen/structure_config.log` exists.
- [ ] **TL-11 `/cdevgen run loot dumps`:** runs `/dumploot` and `/dumptags item` first, then the loot generators.
- [ ] **TL-12 `/cdevgen cancel`:** start `/cdevgen run all`, cancel -> the running script is killed, the rest skipped,
  `/cdevgen status` says so.
- [ ] **TL-13 Startup run:** `[debug.generators] runAtStartup = true`, restart -> the log shows the core generators
  running during load; the first player to join gets the results. Turn it off.
- [ ] **TL-14 `tools/dump_server_mods.py`:** copies only `both` / `server` jars into `server_mods/`; with a jar missing
  from `mod_sides.md` it writes nothing and names the jar. (`/dumpservermods` was removed 2026-10-27; `/dumpall` no
  longer copies jars.)
- [ ] **TL-15 Gradle:** `./gradlew :creativeCommon:build` succeeds (libs populated); `./gradlew :creativeCommon:installToMods`
  moves the old jar to `jar_archive/displaced/`.
- [ ] **TL-16 `_manual_only`:** JEI on cooking oil -> the cooking-pot recipe (2 oil from 6 ingredients) and a separate
  heated mixing recipe (1 oil from 3 + a bottle); no mixer copy of the pot recipe.

## 8. History

- Before 2026-09-27: dictionaries, NCH (migrated from the older `global.CE`, whose commented-out blocks in
  three files were deleted 2026-10-22, [TODO](todo/TODO.md) `tooling/conventions #3`), tag helper, CEConfig, the first dump commands.
- 2026-09-27 (initial git commit): root Gradle build and manifest tooling.
- 2026-09-30: item-loss repro harness run ([plan_1.0.3.md](plan_1.0.3.md) 4.7).
- 2026-09-27 to 2026-10-02 (first commits): `/dumpall` and `generators.json` (2026-09-27), `java_test_cp.sh` (2026-09-28), the
  `/cdevgen` command (2026-10-02). The Rhino, node and GameTest harnesses and more dumps came with their features (each
  dated in its feature writeup).
- 2026-10-18: this writeup; findings F1 to F10; tests TL-01 to TL-16. No code changed.
- 2026-10-19: F6 fixed (test_crop_growth.sh, neoCraftHelper.js line 1, NCH.cfg example).
- 2026-10-22: `R.replaceInput` / `R.replaceOutput` take `includeAdded` (2.2), found in the second scratch bulk run
  (SQ-CO-01: Canned Coffee Berries still took Respite's berries).
- 2026-10-27 (pack_hygiene 36-38, 40): shared Java sources (2.8) replace the per-jar copies of `MemoryPackResources`,
  `UpstreamPacks`, the `UpstreamJars` reading code, the three `NEEDS_MOD` mixin plugins, the two `ItemHiddenMixin`s
  and the built-in `packs/` registration; patcherMod's two potion registrars became one table (`potion/CommonPotions`,
  same ids); `/dumpservermods` (`devtools/ServerModsDump`) removed, `tools/dump_server_mods.py` is the only route
  (TL-14 now tests it). No id or behavior change.
- 2026-10-27 (pack_hygiene 44-49): `tools/pack_paths.py` (2.6) replaces the per-tool item_dict regexes
  (`gen_mortar_catalog`, `gen_stack_sizes`, `lang_index`; `food_catalog.parse_dict` wraps it), the four lenient JSON
  loaders (and `nether_worldgen_scan`'s monkeypatch) and the 13 hard-coded vanilla / Forge jar paths (Forge no longer
  pinned to 43.3.9: read from `instance.json`; `lang_index` now reads Forge's own lang from the universal jar, the
  client jar has none). `effect_audit` and `gen_effect_descriptions` read lang through `lang_index.build()`;
  `audit_loot_containers` reads `loot_breakdown_scan.scan()`. 15 manifest entries added, the rest named in the
  `_readme` (F5 fixed).
- 2026-10-27: item dict down to one key per id (finding 20, 2.1 above): 96 extra keys dropped and their uses rewritten
  across the pack, Creative Delight, Creative Common's `neoCraftHelper.js` (`felsic_ingot` -> `ingot_felsic`), the food JSON
  (`food_types.json`, `can_types.json`, `placeable.json`, `stack_sizes.json`) and `tools/gen_can_types.py` (`cabbage`,
  `crab`). No generator change needed: none of the dropped keys was in a generated section, and the generators pick an
  id's existing key (`best_key`).
- 2026-11-17: text sweeps of plan_rules_fixes.md P5: `tools/history_extract.py` (dated comments to writeup History,
  owner map `tools/writeup_owner.json`, check_all `comment_dates`), `tools/us_spelling.py --scope code` and
  `tools/iso_dates.py --scope code` (lints `spelling_docs`, `spelling_code`, `iso_dates_docs`, `iso_dates_code` fail in
  check_all), `tools/rename_patchermod.py` (patcherMod to Creative Common in text; hits left as written in
  [patchermod_left.csv](patchermod_left.csv)).
- 2026-11-17: tab sweep of plan_rules_fixes.md P5 (section 16): `tools/tab_indent.py` converted 235 Python, 173 JS,
  1032 Java, 31 Gradle and 58 JSON files, one commit per language; lint `tab_indent` fails in check_all. Upstream donor
  trees (a `NOTICE-upstream-*` file), generated files and JSON outside the own namespaces keep their style.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-09-25: --------------------------------------------------------------------------- Vanilla 1.19.2 structure_set defaults, javap-verified against StructureSets.bootstrap (forge-1.19.2-43.3.9_mapped_official_1.19.2.jar) on 2026-09-25 (tools/gen_structure_config.py)
- 2026-09-27: The groups the pack starts with (2026-09-27). --migrate writes these as group/<name> tags; after that the tags in structure_tags.json are the source of truth and this table is only a record (tools/gen_structure_groups.py)
- 2026-09-27: (structure, jar glob, config file under config/, key, value that blocks it or None = always, who) All javap-checked 2026-09-27: YUNG's Disable*Mixin cancel ChunkGenerator.tryGenerateStructure by StructureType; SmallNetherDungeonStructure.findGenerationPoint returns empty when disabled (tools/gen_structure_config.py)
- 2026-09-28: Effects Canned Goods applies in code on top of the food properties (javap of its procedures, 2026-09-28; every one runs in finishUsingItem) (tools/food_catalog.py)
- 2026-10-05: Java-registered configured features (decompiled 2026-10-05) (tools/gen_soil_compat.py)
- 2026-10-13: Patties, bacon, bison, smoked ham, chevon, hoglin, kangaroo, moose, bunfungus, lobster and crab are not canned cooked (can_parity.md Want /, 2026-10-13) (tools/gen_can_types.py)
- 2026-10-14: Carriages. 2.3.4 (2026-10-14): archaeology blocks lose their loot (D6); a hanging sign under a chain becomes a hanging lantern (a wall sign there would have no wall); Diagonal Fences / Diagonal Windows are not in the pack (tools/port_structure_jars.py)
- 2026-10-19: The last /dumppotions ran before the Vinery port went into mods/ (2026-10-19): it dropped these three (alcohol.md B7.3) (tools/effect_audit.py)
- 2026-10-19: Older food builds probe (scratch queue SQ-OF: food builds of 2026-10-19 to 2026-10-21 never checked on a server) (tools/older_food_probe.js)
- 2026-10-19: the footprint's water share (2026-10-19): a lake (water at y 62, its bed below) for x >= 6, dry ground (top y 62) for x < 6 (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFillTest.java)
- 2026-10-19: Older misc builds probe (scratch queue SQ-OM: alcohol ports, feature ports, baby mobs, BlockRunner leaf piles, campfire rain, JEED providers and the world serverconfig payload, built 2026-10-19 to 2026-10-21 and never checked on a server) (tools/older_misc_probe.js)
- 2026-10-20: Other effect-description mods (not installed; their lang only). tools/.cache is git-ignored: the jars there were fetched 2026-10-20 (Modrinth: JEED 1.21-2.3.5, MEED 8.1.0, Effect Insights 26.3.1, Potion Descriptions 1.8) or copied from the ATLauncher download cache (JEED 1.16.5-1.6 / 1.19.2-1.18 / 1.20-2.2.5, Effect Descriptions 8.0.2) (tools/gen_effect_descriptions.py)
- 2026-10-21: 2026-10-21 (can_parity 2.20-2.23): cheese is processed in the Cooking Pot; tomato sauce and dough keep their value; desserts like soups; cake: 1 cake -> 4 servings of 1.75 slices (7 slices a cake), see the cake recipes below (tools/gen_can_types.py)
- 2026-10-21: Sandwich bread shape by sprite: briefs/baking_baked-goods.md (baked-goods #9) audit, 2026-10-21 (tools/food_families.py)
- 2026-10-22: Changelog claims probe (scratch queue SQ-CL-01: bullets of kubejs/assets/creativecommon/changelog/unreleased.md fixed in the 2026-10-22 audit) (tools/changelog_claims_probe.js)
- 2026-10-22: Scratch-server probe: farming / wood / worldgen features of 2026-10-22 (queue entries SQ-SF, SQ-WC, SQ-MB, SQ-GE in feature_writeup/testing/scratch_queue.md) (tools/farm_worldgen_probe.js)
- 2026-10-22: Scratch-server probe for the 2026-10-22 gear / mobs / storage / misc work (queue entries SQ-GM-* in feature_writeup/testing/scratch_queue.md) (tools/gear_misc_probe.js)
- 2026-10-23: Veggies Delight and Vintage Delight pastas and noodles (2026-10-23) (tools/gen_can_types.py)
- 2026-10-23: Veggies Delight and Vintage Delight (2026-10-23): a whole veggie and its 2 cut pieces share the slots, as cabbage (tools/gen_can_types.py)
- 2026-10-23: Veggies Delight and Vintage Delight (2026-10-23); roasted florets alone (no whole roasted cauliflower) (tools/gen_can_types.py)
- 2026-10-25: Compiles the remainder buffer straight from Creative Common's sources (cooking/, oven/ without JEI and client classes) and Creative Delight's (the pot and kettle BE + menu mixins, QoL-1 since 2026-10-25, their switch class and CDConfig) next to the stand-in main class/config and the tests in src/, with Farmer's Delight and Farmer's Respite deobfuscated at runtime (jars in libs/, fetched by the test script) (tools/remainder_buffer_test/build.gradle)
- 2026-10-25: Grid and pot meals moved into Creative Delight's tree (creativedelight: ids) on 2026-10-25; the oven pie is still C (tools/fungi_probe/fungi_probe.js)
- 2026-10-25: emptied 2026-10-25 (Vintage, Fungi Delight); Roll Delight port added 2026-11-01; Large Meals 2026-11-02 (its 4 soup cans) (tools/gen_can_types.py)
- 2026-10-25: the pack's food recipes are under C or, since the move into Creative Delight (2026-10-25), CDN (tools/vintage_probe/vintage_probe.js)
- 2026-10-25: Food recipes moved into Creative Delight's tree on 2026-10-25 and brewing into Creative Brewing's on 2026-10-31 (creativedelight: / creativebrewing: ids, same paths): a creativeengineering: id the checks name also finds its twin under either namespace (tools/vintage_probe/vintage_probe.js)
- 2026-10-25: Food recipes moved into Creative Delight's tree on 2026-10-25 (creativedelight: ids, same paths): a creativeengineering: id the checks name also finds its creativedelight: twin (tools/food_drinks_probe/food_drinks_probe.js)
- 2026-10-25: colonies: FD's own colonies are in neither Serene Seasons tag, so the port leaves the Fungi ones out too (2026-10-25 run) (tools/fungi_probe/fungi_probe.js)
- 2026-10-25: VTD-55: Brewin's pickled pickles merged into Pickled Sea Pickle (2026-10-25); its honey jar recipe is gone. merges/dishes #9 (item_merges.md 2.19, owner 2026-11-02): Brewin's kimchi is removed, its jar recipe too (tools/vintage_probe/vintage_probe.js)
- 2026-10-25: Only items with a coaster model go on a coaster (owner 2026-10-25: the produce of every raw, cooked, egg and dairy food type had joined the tag and lay flat without a model; those were cut, food_families.md 4.4) (tools/placeable_catalog.py)
- 2026-10-25: Kitchen ingredients that are not edible themselves (audit 2026-10-25: inputs of food recipes and the food mods' item lists). id -> key (tools/food_dict.py)
- 2026-10-25: 2. tools/crop_growth_coverage.py: every growing block in the /dumpcropgrowth audit is ruled, fallback or skipped CropGrowth lives in Creative Delight since 2026-10-25: build first with ./gradlew :creativeCommon:compileJava :creativeDelight:compileJava --offline (tools/test_crop_growth.sh)
- 2026-10-27: The encoder inlines referenced functions, so the counts are of the whole tree: 10 flat_cache / 5 interpolated with the pack override in kubejs/data, 7 / 2 with Tectonic 2.3.5a's own file (measured 2026-10-27, wgp_new vs wgp_old) wgp dims     per dimension: how many structures DimensionFilter skips there, and the first few ids (all 0 with [worldgen.systems] dimensionFilter = false) Every result is a `[wgp] ...` line in logs/latest.log; checks print PASS / FAIL (tools/worldgen_perf_probe/wgp_probe.js)
- 2026-10-27: Scratch-server probe: worldgen hot-spot changes of 2026-10-27 (perf.md 10.7 options 1-4, queue entries SQ-WGP in feature_writeup/testing/scratch_queue.md) (tools/worldgen_perf_probe/wgp_probe.js)
- 2026-10-27: The pack folder first, then the client script trees the Creative jars ship (kubejs/assets/<modid>/kubejs/; their packs run after the pack folder; Compat, CD and CB names moved there 2026-10-27 to 2026-11-01) (tools/lang_index.py)
- 2026-10-28: rain (SaltDissolvingInRain = false since 2026-10-28, SQ-SALT-06 / -08): nothing dissolves in open-sky rain (tools/salt_probe/salt_probe.js)
- 2026-10-30: Planting items for dumps written before /dumpcropgrowth recorded `item` (checked by hand 2026-10-30) (tools/crop_season_audit.py)
- 2026-10-30: D4 (2026-10-30): pruning drops a piece whose own footprint is too wet, once it has pruneMinWetColumns water columns (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFillTest.java)
- 2026-10-30: earthfill #15 (phase 4, 2026-10-30) (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFillTest.java)
- 2026-10-31: Brewin' dishes: Creative Delight brewinandchewin/recipes.js since 2026-10-31 (tools/food_drinks_probe/food_drinks_probe.js)
- 2026-10-31: TODO fungiport #3: the pack registers CCK's truffle_pie item (2026-10-31), so the oven recipe must load (tools/fungi_probe/fungi_probe.js)
- 2026-10-31: TODO cooking/kitchen #15 probe: storage in Cooking for Blockheads' kitchen corner (Creative Delight blockheads_additions kitchen/CornerStorage; patcherMod CfbCornerStorage until 2026-10-31) (tools/kitchen_probe/cfb_corner_probe.js)
- 2026-11-01: Creative Delight: the bundle jar; its members (development/creativeDelight/members/, Baby Mobs and Incubation Additions included since 2026-11-01; the Veggies, Vintage, Fungi, Miner's and Display ports since 2026-11-02, creativedelight members #1 / #35) never ship on their own (tools/build_deploy.py)
- 2026-11-01: Creative Delight and Creative Brewing had underscored ids up to 2026-11-01 (creative_split.md 7.7): drop the old jars (tools/scratch_query/prep_srv.sh)
- 2026-11-01: The next five are generator records since framework #7 (2026-11-01): the game reads the CD member scripts (<mod>/food_types.js, placeable.js, mortar_catalog.js in data/creativedelight/kubejs, kubejs_startup/<mod>/ can_types.js and can_servings.js in the CD jar), which the generators write from them (tools/build_deploy.py)
- 2026-11-01: Miner's Delight (owner, 2026-11-01): these four only; Miner's stays in UNDECIDED_MODS so no other food is canned and the cups stay cups (miners_delight.md I3) (tools/gen_can_types.py)
- 2026-11-01: Take a mod out once its cans are decided (Miner's: decided 2026-11-01, four explicit cans only, so it stays listed) (tools/gen_can_types.py)
- 2026-11-01: WorldgenFiles moved to the util package (2026-11-01): its package-private root override is set by reflection (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/BiomeLayoutCacheTest.java)
- 2026-11-01: Namespaces whose file goes to Creative Brewing's startup tree instead (creative_brewing.md 4.2 phase 1, 2026-11-01) (tools/gen_compost_values.py)
- 2026-11-01: 15 ms per key dropped characters in the 2026-11-01 run; 40 ms and a pause after Return keep chat in step (tools/scratch_query/qrun.py)
- 2026-11-01: plain copies: a hard link shares the file with build/libs, and a parallel build rewrites it in place (seen 2026-11-01) (tools/scratch_query/split_w234.sh)
- 2026-11-01: bowl food (owner 2026-11-01: must be 32) (tools/gen_stack_sizes.py)
- 2026-11-01: mashed potatoes, split (merges/dishes #2, owner 2026-11-01: both cannable and saltable) (tools/gen_can_types.py)
- 2026-11-02: Creative Worldgen: world generation and fauna (feature_writeup/creative_worldgen.md); nests More Mooblooms and Baby Mobs (development/creativeWorldgen/members/; More Mooblooms shipped on its own before 2026-11-02) (tools/build_deploy.py)
- 2026-11-02: sand_base (2026-11-02): the base tags combine per structure (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFillTest.java)
- 2026-11-02: Veggies, Vintage, Fungi, Miner's and Display are nested in the CD jar since 2026-11-02 (plan_creative_delight.md H): no standalone swap rows for them (a rebuilt member reaches the scratch server through a rebuilt CD jar) (tools/scratch_query/prep_srv.sh)
- 2026-11-02: members nested in a bundle jar (Creative Delight's META-INF/jarjar/*.jar since 2026-11-02) (tools/gen_dd_dishes.py)
- 2026-11-02: waterReach (2026-11-02): a basin, water from y 60 - x up to 60, so a pad at y 61 has x water blocks under its bottom (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFillTest.java)
- 2026-11-02: Sourced by tools/test_*.sh: sets CP to the classpath for headless patcherMod tests (dev/Mojmap names: the mapped Forge jar + development/creativeCommon/build/classes + development/creativeWorldgen/build/classes (the worldgen classes since 2026-11-02) + what Bootstrap.bootStrap() needs) (tools/java_test_cp.sh)
- 2026-11-02: tool and misc dicts (2026-11-02): same check, load order tool (979) then misc (978) (tools/food_dict_test/RhinoRun.java)
- 2026-11-02: End's Delight's meat and slice since 2026-11-02 (Ender's mollusk and filet merged into them, TODO dishes/ingredients #32) (tools/gen_can_types.py)
- 2026-11-02: 2026-11-02 (structures/contents #1): Philip's Nether ruin moved to the Nether, on its own fit (no land parent) (tools/worldgen_test/com/SaintAlfonzo/creativeworldgen/worldgen/FitTest.java)
- 2026-11-02: air-below    columns whose first block under the built bottom is air; max gap; gaps over 6 (beardReach) water-below  same for water (waterlogged counts); max depth base water   water inside the bottom 3 layers of the structure (a flooded floor) base earth   ids of earth_fill/earth blocks left in the bottom 3 layers (after ground match) ring         top blocks (OCEAN_FLOOR) every 2 blocks on a ring 3 out from the start's box: the native ground foreign      base earth ids that never occur in the ring (ground match should have swapped them) Untested before its first scratch run (written 2026-11-02 for SQ-EFB); first 30 starts only (tools/scratch_query/zq_probe.js)
- 2026-11-02: syrup is a cooking ingredient like honey (owner, 2026-11-02) (tools/gen_stack_sizes.py)
- 2026-11-02: Farmer's Delight's Ratatouille (TODO cans/canning #18, 2026-11-02): a vegetable stew, canned like the soups (tools/gen_can_types.py)
- 2026-11-02: in the Creative Worldgen jar since the wave 5 carve (2026-11-02; Creative Common before) (tools/build_deploy.py)
- 2026-11-02: The Veggies, Vintage, Fungi, Miner's and Display ports are members too (bundle_members, 2026-11-02; folders moved here the same day) (settings.gradle)
- 2026-11-02: Label surface test and centered notes (sticky_notes.md 3.3a), on the real shapes of the modded hosts (read from their jars 2026-11-02) and on vanilla blocks (tools/creative_labels_test/SpotsTest.java)
- 2026-11-02: nested in the Creative Delight jar since 2026-11-02 (plan_creative_delight.md A2) (tools/build_deploy.py)
- 2026-11-02: Added 2026-11-02 after the member stopped shipping the files (it read them from the donor since 2026-11-01, so the regenerated config had dropped the set) (tools/gen_structure_config.py)
- 2026-11-02: Salt, Create: The Salt, Canned Goods (repacks) and Golden Food (donor port) joined 2026-11-02 (plan_creative_delight.md J2 to J4) (settings.gradle)
- 2026-11-02: Kimchi merged (merges/dishes #9, owner 2026-11-02): Brewin's kimchi is removed, Vintage Delight's fills the can (tools/gen_can_types.py)
- 2026-11-02: Creative Delight nests Baby Mobs and Incubation Additions since wave 4 (creative_split.md 7.8) and the five ports (Veggies, Vintage, Fungi, Miner's, Display) since 2026-11-02: a standalone jar would win over the nested one (tools/scratch_query/prep_srv.sh)
- 2026-11-03: Barbeque's Delight cooked skewers joined the skewer family at the skewer fold (2026-11-03, dishes/meals #46: handheld + "skewer" in the name); the burnt skewer is in hazard by its type (tools/food_families.py)
- 2026-11-04: Creative Common: patcherMod until the Creative Split swap (2026-11-04, creative_split.md 7.13); the shared core every Creative jar requires (tools/build_deploy.py)
- 2026-11-04: Creative Common since 2026-11-04 (tools/build_deploy.py)
- 2026-11-04: Common was patcherMod (jar creativeengineering-*) until the 2026-11-04 swap; the Engineering jar now owns that name (tools/scratch_query/prep_srv.sh)
- 2026-11-04: [shields] moved to Creative Equipment's file on 2026-11-04; its first boot carries the section over, so read the old file only until then (tools/shield_check.py)
- 2026-11-04: The dicts (global.item, food, fluid, blocks, ...) ship in Creative Common's jar since plan_cb_publish.md M1 (2026-11-04) (tools/pack_paths.py)
- 2026-11-04: [bottled_air]: Creative Equipment since 2026-11-04 (tools/older_misc_probe.js)
- 2026-11-06: Hard dependencies of Creative Brewing, all the way down (plan_cb_publish.md 1.2 and 14; nested members' mods.toml checked 2026-11-06: Vinery needs creativecalendar + foundation, Brewin' farmersdelight, both nested; MoreJS required by Creative Brewing since 2026-11-06) (tools/scratch_query/port_chain.sh)
- 2026-11-06: Checks the global.NCH.remove guard (2026-11-06, TODO creativedelight/framework #15): filters holding undefined or null must be refused with "remove refused: <path>" and remove nothing; a normal filter still removes (tools/scratch_query/nch_guard_probe.js)
- 2026-11-06: gear rows: Creative Equipment since 2026-11-06 (plan_cb_polish.md 1); the frameworks are Creative Common's (tools/effect_audit.py)
- 2026-11-06: Creative Worldgen's jar (moved from kubejs/data on 2026-11-06; ids unchanged) (tools/gen_nether_worldgen.py)
- 2026-11-06: A KubeJS item has no model in a jar: its generated model's layer0 is <ns>:item/<path>, the sprite in kubejs/assets (or in an in-tree jar's assets: Creative Delight holds the cooking sprites since 2026-11-06) (tools/gen_dd_dishes.py)
- 2026-11-06: Cooking mods CD / CB integrate with but do not nest or repack (optional dependencies in creativedelight's mods.toml, 2026-11-06), then their own required mods (tools/scratch_query/port_chain.sh)
- 2026-11-06: /cbo mods      each optional dependency of Creative Brewing: loaded or absent /cbo effects   Armor / Tool Effects tables (global.armorEffects, global.toolEffects): entries per item namespace, then every entry whose item or effect is not registered ("leak" lines: a guard let an absent mod's row in) /cbo potions   creativecommon potions and mob effects (Creative Common since 2026-11-06) registered, and the potions of the optional mods CB touches (long_reach, phantom_bane, Buzzier Bees luck, Alex's Mobs, Quark / Upgrade Aquatic removed) Output: "[cbo] ..." lines in logs/latest.log, then "[cbo] done" (tools/scratch_query/cbo_probe.js)
- 2026-11-06: Creative Brewing's optional dependencies (its mods.toml, mandatory=false) and their own required jars (SQ-CBO, 2026-11-06) (tools/scratch_query/port_chain.sh)
- 2026-11-06: Pack items (kubejs:, creativeengineering:) go to creativedelight/ (CD's own script folder; renamed from creativeengineering/ 2026-11-06, plan_cb_polish.md 4) (tools/gen_compost_values.py)
- 2026-11-06: Hard dependencies of Creative Brewing, all the way down (plan_cb_publish.md 1.2 and 14; nested members' mods.toml checked 2026-11-06: Vinery needs creativecalendar + foundation, Brewin' farmersdelight, both nested; MoreJS required by Creative Brewing) (tools/scratch_query/port_chain.sh)
- 2026-11-07: owner rewrites 2026-11-07 (plan_creative_tips.md section 7), pinned by exact upstream text (tools/loading_tips_migrate.py)
- 2026-11-07: Tempad 3.0.4 port: its own jar (nested in Creative Compat until 2026-11-07; planned to fold into Creative Computing) (settings.gradle)
- 2026-11-07: Tempad 3.0.4 port (feature_writeup/immersive_portals.md 2.1.1): its own jar since 2026-11-07 (nested in Creative Compat before, so Compat ships without a donor jar); planned to fold into Creative Computing (tools/build_deploy.py)
- 2026-11-07: Creative Common dict checks of 2026-11-07 (SQ-CC-06): the bark / metal dust tags moved to creativecommon:, the Create Framed and SecurityCraft tinted glass keys, and the mirror glass block tags (tools/scratch_query/cc_dict_probe.js)
- 2026-11-07: its own jar since 2026-11-07 (nested in Creative Compat before) (tools/build_deploy.py)
- 2026-11-08: Jars: Common and Compat are COPIED from build/libs into every srv (a parallel build cannot rewrite them mid-run) and must be the 2026-11-08 builds this part targets (Compat with "// phase: helpers" on jei_order.js, Common with the LowGravity mixins); the run stops when they are missing (tools/scratch_query/cge2_run.sh)
- 2026-11-08: Written 2026-11-08, not run yet: one live call each before a batch relies on it (scratch-test gotchas) (tools/scratch_query/hh_t12_probe.js)
- 2026-11-09: Written 2026-11-09, not run yet: one live call each before a batch relies on it (tools/scratch_query/wo_probe.js)
- 2026-11-09: Owner decision 2026-11-09: these list an oak tree only on a technicality (island oaks, river banks, rare surface spots) (tools/gen_wise_oak_biomes.py)
- 2026-11-10: A dump from before the 2026-11-10 namespace move (e09e4795) lists the pack's own structures (meta/*, sky_island) under creativeengineering; structure_tags.json names them creativeworldgen (tools/worldgen_test/com/SaintAlfonzo/creativedevtools/cdevplace/StructureCatalogueTest.java)
- 2026-11-10: cfgdump                     every [salt] and [crow] value as the game reads it cfg KEY [true|false|D]      read a key, or live-set a boolean / chance key of [crow] or [salt] (ints: toml before boot) spawn TYPE N X Y Z [SPREAD] [noai]   summon N of TYPE around X Y Z (random within SPREAD horizontally) log TYPE X Y Z R [REF]      one line per entity within R of X Y Z, with wariness state and goals (crows: wariness; others: restriction) watch TYPE X Y Z R N EVERY TAG [REF]   sample like log N times, EVERY ticks apart; REF = @BOTNAME or x,y,z for the distance column assert TAG STAT OP V        compare a watch statistic: "[zhh3] assert TAG STAT=v OP V PASS|FAIL" seen TAG GOAL               did goal class GOAL run in any sample of TAG: "[zhh3] seen TAG GOAL yes|no" goals TYPE X Y Z R          the goal list of each entity with priorities (available goals, running ones marked) tally X Y Z R               "tally crows=N tamed=T carrying=C shoulder=S" for the crows in the box; crows on the shoulders of players in the box count too (S of them) target X Y Z R ITEM        CrowEntity.isFood(ITEM) for each crow (#creativedelight:crow_food): "[zhh3] isFood ITEM true|false wild=B" block X Y Z                 block id and properties poi X Y Z                   POI type at the position, or none items X Y Z R [clear]       item entities (id, count, Age, Invulnerable, Thrower present, PickupDelay), then "items total=N id xN ..." inv NAME [ITEM]             hotbar, main, offhand of a player; with ITEM also "invcount NAME ITEM n" throw NAME X Y Z ITEM N [SPREAD]   N item entities (1 item each) with player NAME as thrower, no pickup delay hit NAME X Y Z R [DMG]      player NAME hits the nearest crow to X Y Z (DamageSource.playerAttack, default 1) feed NAME X Y Z R           player NAME right-clicks every wild crow in the box once (Player.interactOn, main hand) interact NAME X Y Z R       player NAME right-clicks every crow in the box (wild or tame), main hand tame NAME X Y Z R           tames every crow in the box to player NAME (TamableAnimal.tame) sneak NAME on|off / sprint NAME on|off   set the flag directly; bots have no input loot X Y Z R ITEM [TICKS]   CrowLogic.holdLoot on every crow in the box (TICKS overrides the carry timer) leash NAME X Y Z R TYPE     leash every TYPE in the box to player NAME enttag TAG                  entity types in an entity type tag nests X Z R                 nests at the surface (tree tops, roofs, ground) in a loaded square: generated / placed counts, first 5 with biome kill X Y Z TYPE N LOOT      a fake player with an iron sword of Looting LOOT kills N fresh TYPE at X Y Z; sums the drops TYPE defaults to hearthandharvest:crow when written "crow" (the Hearth and Harvest crow since 2026-11-10; wariness is the entity's own field, NBT "Wariness") (tools/scratch_query/hh_crow_probe.js)
- 2026-11-10: Seed string, not an id: kept as it was before the 2026-11-10 namespace move so salts never change (tools/gen_structure_groups.py)
- 2026-11-10: owner 2026-11-10 (tools/cd_namespace_ids.py)
- 2026-11-10: Rustic Delight port (CD members/rustic, json baked since 2026-11-10; farming/wild-crops #11): its placed features are json (tools/gen_crop_worldgen_table.py)
- 2026-11-13: The -17 jar sat in the instance mods/ on 2026-11-13 with no mod_sides.md row (r1113 section 11): drop any copy prep linked in, so queue servers never carry an unlisted port jar (tools/scratch_query/prep_srv.sh)
- 2026-11-13: Written 2026-11-13, NOT yet run: the first runner fixes it as a tooling blocker if a call does not resolve (run default 1) (tools/scratch_query/tpf_probe.js)
- 2026-11-13: Written 2026-11-13, not run yet: one live call each before a batch relies on it (scratch-test gotchas) (tools/scratch_query/c3_probe.js)
- 2026-11-13: Scratch-only probe for the Create: Power Grid port, phase P5 rows (SQ-PWG-45 onward, plan_power_grid.md P5: ponder); since P6 (2026-11-13) it carries the 55 structures of the -07 jar (P5's 36 plus 19 P6 ones; SQ-PWG-46); since P10 (r1113 pwg17) also ceiling_tile and wire/string_lights from the -17 jar (57 in all; generated the same way) (tools/scratch_query/pwg_p5_probe.js)
- 2026-11-14: MM-DD with no year: not glued to digits, words, dots, slashes or hyphens (so 2026-11-14, 1.19.2, v2-11 never match) (tools/iso_dates.py)
- 2026-11-14: BOP inserts its own rules one later (after that netherrack rule); the Heap goes first because it is flesh at every depth (plan_bugfix_1114.md 6.4, owner 2026-11-14) (tools/gen_visceral_heap.py)
- 2026-11-15: MIGRATED 2026-11-15 (creative_devtools.md): sneak -> /pbot sneak, eat -> /pbot eat (ticked use, not finishUsingItem), hunger -> /pbot hunger, effects and food -> /cdevtest player, inv -> /pbot inv NAME get (tools/scratch_query/zp_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): interact -> /pbot interact (sneak via /pbot sneak), drops -> /cdevtest items, finish -> /pbot eat or useitem (a ticked use) (tools/scratch_query/hh_t12_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): zrt -> /cdevtest randomtick BOX 1, zcount -> /cdevtest count BOX #minecraft:leaves. zplace, zchop, zleaftag stay here (tools/leaf_decay_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 to /cdevtest and /pbot (creative_devtools.md): aim -> pbot look, bstate -> cdevtest state, items -> cdevtest items [keep], loot -> cdevtest loot tool= rolls=, rest -> cdevtest player (no stat; stays here). shoot, portals, craft, pot stay one-offs (tools/scratch_query/c3_probe.js)
- 2026-11-15: tiers gained after the row was written (owner, 2026-11-15) (tools/scratch_query/pth_gen_a1.py)
- 2026-11-15: MIGRATED 2026-11-15 (creative_devtools.md): state -> /cdevtest state, punch -> /pbot punch, drops -> /cdevtest items, stacks -> /cdevtest items keep (summed, not per stack: use /cdevtest entities type=minecraft:item nbt). skip stays here (tools/scratch_query/wo_probe.js)
- 2026-11-15: cmd a,b,c                 root command registered or missing, one line each ("[zc4] cmd NAME registered|missing") put X Y Z ITEM N          insert N of ITEM into the block's item handler (no side): "[zc4] put ... inserted=K" fill X Y Z FLUID MB       fill the block's fluid handler: "[zc4] fill ... filled=K" inv X Y Z                 item handler slots and fluid tanks: "[zc4] inv X Y Z total=N id xN, ..." and "[zc4] tanks ..." pools X Y Z R             item entities within R summed into the Visceral Heap pools (plan_flesh_recipes.md 3): "[zc4] pools meat=N bone=N eye=N misc=N slime=N string=N other=N total=N"; R < 0 = clear the counted entities after printing (radius |R|) poolsinv X Y Z            the same sums over the block's item handler fuel X Y Z ITEM           Create BlazeBurnerBlock.tryInsert with ITEM (no overflow), then the block's blaze property cooktime TEXT             Cooking Pot recipes whose id or result contains TEXT: "[zc4] cook ID -> N RESULT time=T" eat NAME ITEM             player NAME eats ITEM as a client use would finish it (finishUsingItem plus Forge's LivingEntityUseItemEvent.Finish, which both Spice of Life books listen to) hunger NAME N             set food level N (saturation 0), so placed cakes and pies accept a bite sol NAME [ITEM]           "[zc4] sol NAME carrot=N applepie=N maxhealth=H" (+ "carrotHas=b applepieHas=b" for ITEM) isfood TYPE ITEM          Animal.isFood of a fresh (not added) entity of TYPE: "[zc4] isfood TYPE ITEM true|false" bees X Y Z R              per bee: FlowerPos, HivePos, HasNectar ("[zc4] bee ... flower=x,y,z|none ...") MIGRATED 2026-11-15 in part (creative_devtools.md): inv -> /cdevtest inv + tank, eat -> /pbot eat, hunger -> /pbot hunger (tools/scratch_query/zc4_probe.js)
- 2026-11-15: /zhh2 tank x y z                      fluid and amount of a machine's tank (jug, trough, sprinkler, basin) /zhh2 fill x y z fluid amount [tagged]   FLUID_HANDLER fill (what a pipe does); "tagged" adds NBT {hh:1b}; prints accepted mB /zhh2 drain x y z amount              FLUID_HANDLER drain; prints what came out /zhh2 insert x y z item n [side]      ITEM_HANDLER insert (what a hopper does); prints the remainder /zhh2 extract x y z slot n [side]     ITEM_HANDLER extract; prints what came out /zhh2 cmp x y z                       comparator output of the block /zhh2 basin x y z                     basin role, controller, tank and slots /zhh2 stomping                        every hearthandharvest:stomping recipe: id, fluid, amount, ingredient count, inputs /zhh2 stomp x y z                     basin.tryProcess with the nearest living entity within 3 blocks (cooldown 5 ticks per entity) /zhh2 sneak name true|false           setShiftKeyDown on a bot or player /zhh2 useat name x y z face hx hy hz  block use with the hit at x+hx y+hy z+hz (pbot use always hits the face center) /zhh2 countset x1 y1 z1 x2 y2 z2 a,b,c    how many of each block id in the box, one scan (loaded chunks only), and in how many chunks /zhh2 chests x1 y1 z1 x2 y2 z2        every chest in the box: position and LootTable /zhh2 unpack x y z                    rolls a chest's loot table now and lists its items /zhh2 lootroll table n                rolls a chest loot table n times: item histogram MIGRATED 2026-11-15 in part (creative_devtools.md): tank -> /cdevtest tank, sneak -> /pbot sneak, useat -> /cdevtest use as=NAME hit=hx,hy,hz face=F. basin, stomping, unpack and the rest stay here (tools/scratch_query/hh_mw_probe.js)
- 2026-11-15: /zc2 state x y z                     block state, block entity type id and the shape's max y (empty shape: none) /zc2 inv x y z                       block entity type, every item list in its NBT ("Items" lists at the top level, in "Inventory" / "inventory" / "ItemHandler" / "Items" compounds and in each ForgeCaps entry) as "key[slot:count id ...]", and its numeric top-level tags (CookTime, BurnTime, fermenting_jar.progress, ...) /zc2 watch x y z ticks every         /zc2 inv every <every> server ticks until <ticks>, lines "[zc2] t=N ..." /zc2 useat NAME x y z hx hy hz [face] [both]   NAME uses the main-hand item on the block at x y z with the hit point at x+hx y+hy z+hz (perfBot's /pbot use always hits the face center); "both" then tries the off hand when the main hand did not consume the click (what the client does); prints the results and both hands after /zc2 attrs <item id>                 attribute modifiers of a fresh stack per equipment slot (tune_tools / tune_armor) /zc2 cap x y z face|null             the block entity's item handler as automation sees it from that face: slot count, then each non-empty slot with what a 64 extract would take (simulated) /zc2 air NAME N                      air supply set to N (max 300); N = -1 only reads it /zc2 reg item|block|fluid|block_entity_type <regex>   count of registry ids matching the regex, first 40 listed MIGRATED 2026-11-15 to /cdevtest (creative_devtools.md): state -> state + be, inv -> inv (face=null) / be, watch -> watch what=inv, useat -> use as=NAME hit=dx,dy,dz hand=both, attrs -> attrs, cap -> inv face=..., reg -> reg. air stays here (tools/scratch_query/c2_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): fp + hold + dig -> /cdevtest break X Y Z held=ITEM (no dig speed), drops -> /cdevtest entities type=minecraft:item nbt (tools/scratch_query/pwg_p7_probe.js)
- 2026-11-15: /ztp pdata P                   P's tempad:player_data capability NBT (pinned location, travel history) /ztp portal P X Y Z ANGLE UPRIGHT [PROVIDER NAME]   Portal Setup close (SyncPortalSettingsPacket) on the workstation context P's open menu holds (open it first with a use on the terminal) /ztp card P PROVIDER NAME      Teleport app: write the selected location onto the carried blank card (WriteToCardPacket) MIGRATED 2026-11-15 in part (creative_devtools.md): break -> /pbot break, sneak -> /pbot sneak, blocks -> /cdevtest state per position (tools/scratch_query/ztp_probe.js)
- 2026-11-15: /zpth mod TAG             one line per tagged entity: type, pass-through modifier (amount or none), pos, motion, health, NoGravity, onGround, removed /zpth player NAME         the same for a player or perfBot bot /zpth watch TAG TICKS EVERY   a line every EVERY ticks for TICKS ticks ("w TAG t=..."), "gone" once when the entity is removed, "watch done TAG" at the end /zpth launch TAG OWNER VX VY VZ   owner (bot/player name or none), NoGravity off, motion set, for every tagged entity /zpth motion TAG VX VY VZ  motion only /zpth input NAME jump|sneak|none   sets a bot's jumping / shift flag (held until changed) /zpth reel NAME           retrieves NAME's fishing hook with the held rod /zpth near TYPE X Y Z R   every entity of TYPE (full id) within R: tags, pos, age-like fields /zpth watchnew TYPE TAG TICKS EVERY X Y Z R   as watch, for entities of TYPE that appear in the box (tagged TAG) /zpth fly NAME true|false  sets a creative player's flying flag (synced to the client) /zpth count TAG           "count TAG n=.." (alive tagged entities) /zpth states ID ...       block state properties of each block id /zpth face NAME YAW PITCH  turns a bot; /zpth use NAME = right-click in air with the main hand item; /zpth release NAME TICKS  releases a held use (bow, trident) as if charged TICKS ticks /zpth attrs NAME|TAG      every movement-speed modifier of a player/bot (or of each tagged entity): uuid, name, amount, operation, then the attribute value (r1116 SQ-PS15B-11) MIGRATED 2026-11-15 in part (creative_devtools.md): input NAME sneak -> /pbot sneak, states -> /cdevtest state (tools/scratch_query/zpth_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): fp + hold + sneak + use -> /cdevtest use X Y Z held=ITEM sneak, drops -> /cdevtest entities type=minecraft:item nbt (tools/scratch_query/pwg_p8_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): sneak -> /pbot sneak, hand -> /cdevtest player, break -> /pbot break, items -> /cdevtest items, loot -> /cdevtest loot (needs the block placed) (tools/scratch_query/hh_gear_probe.js)
- 2026-11-15: /s5 sneak <player> on|off          setShiftKeyDown on a bot (perfBot `pbot use` then runs as a sneaking use) /s5 lclick <player> x y z          posts Forge's LeftClickBlock for the bot at the block (top face) /s5 break <player> x y z           the bot breaks the block (ServerPlayerGameMode.destroyBlock, held tool) /s5 eat <player> <item>            the bot eats one item in the main hand: finishUsingItem + onItemUseFinish (the order LivingEntity.completeUsingItem uses); logs food level and glass bottles /s5 rest <player> <ticks>          sets the bot's time-since-rest stat (phantom spawning) /s5 trim <item>                    Armor Trims TrimmableItem.applyTrim(item, coast, gold ingot): trimmed or empty /s5 remainder <item>               crafting remainder of one item /s5 spout <fluid> <mb> <item>      Create spout logic: can the item be filled, mB needed, what it becomes /s5 drain <item>                   Create item drain logic: fluid and amount out, item left /s5 containers <text>              farmersdelight:cooking recipes with a result id containing text: serving container /s5 selfloop <item>                recipes that make the item and also take it /s5 fluidtag <tag>                 fluids in a fluid tag /s5 potrecipe                      farmersdelight:cooking recipes served in a bowl with an ingredient that leaves a bowl; prints Copper Pot NBT for 1 and for 3 meals (SQ-RB-21) /s5 jarrecipe                      vintagedelight:fermenting recipes with a container whose ingredient leaves that container; prints Pickling Jar NBT for 3 batches + 1 container (SQ-RB-22) MIGRATED 2026-11-15 in part (creative_devtools.md): sneak -> /pbot sneak, break -> /pbot break, eat -> /pbot inv set + /pbot eat (a ticked use) (tools/scratch_query/s5_probe.js)
- 2026-11-15: Scratch fixes 2026-11-15 (scratch run r1115 phase 11): SQ-PTH-03 (vanilla's fast-fall reset clip on webs), SQ-PTH-11 (break projectiles stalled in any web), the 7.2 performance fallback (region counts, per-tick count cache) (tools/passthrough_test/com/SaintAlfonzo/creativecompat/surface/PassFixesTest.java)
- 2026-11-15: Written 2026-11-15 (r1115 runner-7) (tools/scratch_query/zqn_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part: the rt path (random ticks without a player) is /cdevtest randomtick BOX N (creative_devtools.md); tick and tree stay here (they build their own fixtures) (tools/leaf_pile_probe.js)
- 2026-11-15: Offline checks for pass-through blocks, phases 1, 2, 3 and 5 (feature_writeup/plan_passthrough.md 8): tier defaults, rule results (ring 0/5/6/8, cube 0/5/6/25/26), neighbor counts, growth rules (one- and two-block crops, hemp enum, rice panicles), minLayers, fall distance per block passed, modifier / wading / field of view math, item stick point, tag ids, the phase 2 pure logic (multi-cell item stick path, surrounded climb, arrow despawn limit and cap pick, projectile group defaults and tag ids), phases 3 and 5 (the [leaf_piles] -> [surface.passthrough.leaf_pile] minLayers move, on fixtures and a copy of the live config; the WTHIT Walk Through / Fall Through / Neighbors format; StuckInBlock untiered only), [surface.passthrough] spec and the sync packet; scratch fixes 2026-11-15 (fast-fall reset clip, break stall, region counts, per-tick count cache); the 1115b rework (plan_post_scratch_1115b.md: lone / stacked / dense web fall, breakStallTicks, 4 / 6 neighbor model, baked chunk counts and their invalidation keying, growth vs neighbors, config migration, sync format 2, layer height rule) (tools/test_passthrough.sh)
- 2026-11-15: what Forge then does: correct the file against the spec; the carried value must survive then the 2026-11-15 move (PassConfigMigration): leaf_pile -> layers, minLayers 5 carried (tools/passthrough_test/com/SaintAlfonzo/creativecompat/surface/PassPhase35Test.java)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): items -> /cdevtest items | entities type=minecraft:item nbt, inv -> /pbot inv NAME get, hit -> /pbot attack NAME @e[...] (or /cdevtest kill) (tools/scratch_query/hh_crow_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 in part (creative_devtools.md): items -> /cdevtest entities type=minecraft:item nbt, tank -> /cdevtest tank, inv -> /cdevtest inv, age -> /cdevtest ages (tools/scratch_query/pwg_p6_probe.js)
- 2026-11-15: lbfill NAME BOX FOOD N     put BOX (a lunchbox id) in NAME's main hand with N of FOOD inside (item handler cap) held NAME                  main-hand item id, count, NBT and the item handler's contents heldeat NAME               NAME eats from the main-hand item as a client use would finish it: finishUsingItem of the held stack, then ForgeEventFactory.onItemUseFinish (LivingEntity.completeUsingItem) dye NAME DYE               2x2 crafting grid (NAME's inventory menu): held item + DYE; matching recipe id, result, NBT skin NAME SKIN             smithing container: held item + SKIN; matching recipe id, result, NBT breed                      SQ-BREED-01 dump: Animal.isFood of every Animal type against every item, FlowerBlock / TallFlowerBlock blocks with their #minecraft:flowers membership, and the F-B7 candidates by class; writes kubejs/exported/breeding_dump.json (one entity type per tick) bstate X Y Z               block state string and block entity id at a position (loaded chunks only) MIGRATED 2026-11-15 in part (creative_devtools.md): held -> /cdevtest player, heldeat -> /pbot eat, bstate -> /cdevtest state (tools/scratch_query/zp9_probe.js)
- 2026-11-15: Written 2026-11-15 (runner-7) (tools/scratch_query/dep_p7_boots.sh)
- 2026-11-15: MIGRATED 2026-11-15 to Creative Devtools' Java /cdevtest (feature_writeup/creative_devtools.md, /cdevtest); new rows use those verbs, old batches keep /zq: recipes, rdetail, bytype, uses, makes, food, item, items/blocks -> reg item|block, potion, count, ages, chunk, biometag/itemtag/blocktag -> tag biome|item|block, tagsof, useitem -> useair as=NAME hand=both, states. season, floating, under and nbt followed on 2026-11-16 (header above) (tools/scratch_query/zq_probe.js)
- 2026-11-15: MIGRATED 2026-11-15 to /cdevtest (creative_devtools.md): kill -> kill X Y Z TYPE n= held= [fire], break -> break X Y Z held= [keep], useon -> use X Y Z as=NAME hit=0.5,HITY,0.5 (tools/scratch_query/zact_probe.js)
- 2026-11-15: /zsneak NAME        "[zsneak] NAME shift=true|false crouching=true|false pose=... pos=x y z" MIGRATED 2026-11-15: /cdevtest player NAME prints shift, pose and position (creative_devtools.md) (tools/scratch_query/zsneak_probe.js)
- 2026-11-16: recipes, rdetail, bytype, uses, makes, food, item, potion, count, ages, chunk, tagsof, states -> same verb name items / blocks -> reg item|block;  biometag / itemtag / blocktag -> tag biome|item|block useitem P -> useair as=P hand=both season, floating, under, nbt -> same verb name (moved 2026-11-16; same arguments, [cdevtest] key=value output; nbt prints the stack's tag as the game holds it, not Rhino's typeof view; efb_judge.py reads both under forms) (original header) Scratch-only query command for bulk queue runs (feature_writeup/testing/scratch_queue.md) (tools/scratch_query/zq_probe.js)
- 2026-11-16: Checks over FAST_LIMIT on their own (measured alone, 2026-11-16); --fast skips them (tools/check_all.py)
- 2026-11-16: The old-jar case (no BeeNests class) was removed 2026-11-16: bee_nests.js loads the class at the top since 2026-10-28 (463d2342) and ships in the same Creative Worldgen jar as the class, so the case cannot happen (tools/test_bee_nests.sh)
- 2026-11-16: GroundJudge (the under / floating / season verbs, moved from /zq 2026-11-16) column: solid 64..70 over air 60..63, solid at 59: bottom 64, air first, gap 4, not deep (tools/scratch_query/cdevtest_harness/CdevtestHarness.java)
- 2026-11-16: The old-jar cases (no StructureFrequencies, no MetaStructure class) were removed 2026-11-16: structure_overrides.js loads its classes at the top since 2026-10-28 (463d2342) and ships in the same Creative Worldgen jar as them (tools/test_structure_groups.sh)
- 2026-11-16: Runs a file of console commands (one per line, # comments skipped) against a scratch server and prints the [zq]/[cdevtest] probe lines they produced (/zq is retired since 2026-11-16; /cdevtest verbs replace it).   zq_batch.sh OUT COMMANDS_FILE [WAIT_SECONDS_PER_COMMAND] (tools/scratch_query/zq_batch.sh)
- 2026-11-16: The old-jar case (no SpawnTuning class) was removed 2026-11-16: mob_spawns.js loads the class at the top since 2026-10-28 (463d2342) and ships in the same Creative Worldgen jar as the class, so the case cannot happen (tools/test_mob_spawns.sh)
- 2026-11-16: zq_probe.js retired 2026-11-16 (its verbs are /cdevtest in Creative Devtools); not copied any more scratch-only data (zq namespace): entity tag #zq:villagers (vanilla + MCA) for selectors (tools/scratch_query/prep_srv.sh)
- 2026-11-16: - tools/scratch_query/datapack/zq (tag #zq:villagers) copied in; the /zq probe (zq_probe.js) is retired since 2026-11-16 and no longer copied: every reusable probe verb is /cdevtest in the Creative Devtools jar (tools/scratch_query/prep_srv.sh)
- 2026-11-16: The old-jar cases (no StructureFrequencies, no MetaStructure class) were removed 2026-11-16: structure_overrides.js loads its classes at the top (463d2342) and ships in the same Creative Worldgen jar as them (tools/test_structure_groups.sh)
- 2026-11-16: RETIRED 2026-11-16: prep_srv.sh no longer copies this file into scratch servers; every reusable verb is a Java /cdevtest verb in Creative Devtools (feature_writeup/creative_devtools.md, /cdevtest; plan_scratch_testing_expansion.md item 3) (tools/scratch_query/zq_probe.js)
- 2026-11-17: Creative Loot: the loot engine (chest tuning, drop rules, the LootJS layer) and the global loot modifier merge (feature_writeup/creative_loot.md); split out of Creative Worldgen 2026-11-17 (tools/build_deploy.py)
- 2026-11-17: Creative Common was patcherMod until the Creative Split swap (creative_split.md 7.13); the Tempad port was nested in Creative Compat before it became its own jar; farmers_upstream moved from Creative Common to the Creative Worldgen jar in the wave 5 carve (build_deploy.py)
