# Pack tooling: dictionaries, recipe and tag helpers, CEConfig, dump commands, generators, tests, builds

The pack's own infrastructure: what every feature script is built on, and the tools used to inspect, generate and
test the pack. This is a hub. Each piece gets its contract, entry points and gotchas here; the feature writeups own
the feature-specific uses. Deploy tooling is in [deploy.md](deploy.md), the performance report in [perf.md](perf.md).

## 0. At a glance

| Part | Status |
|---|---|
| Dictionaries: `global.item` (12,511 keys), `global.blocks`, `global.fluid`, effect and multiblock dicts, `global.removedItems` (309) | in use since the initial commit; startup validation logs missing ids (tag keys: F1, F2) |
| Recipe helpers `global.NCH` (about 60 recipe types, id generation, validation, `strict` mode, `bulk`, `cfg`) | in use |
| Tag helper `global.tags` (add/remove, wildcard `addMatching`, families, mineable, storage blocks, overlaps, mirror) | in use |
| `CEConfig` (patcherMod): one Forge common config, 23 sections, live-editable in Create's config screen; `NCH.cfg` reads recipe values from it | in use ([gear_tuning.md](gear_tuning.md) for `NCH.cfg`) |
| 24 `/dump*` commands writing `kubejs/exported/`, and `/dumpall` | in use |
| `/cegen`: runs `tools/generators.json` (28 Python generators) in game, optionally at startup | in use; startup run off |
| Headless tests: 9 `tools/test_*.sh` suites (Java on patcherMod classes, the pack's Rhino fork, node sims, Python), plus GameTest projects | in use; machine-specific paths (F7) |
| Root Gradle build: one build for the 5 in-tree mods, `ce.forge-mod` convention plugin, `installToMods` | in use |
| Item-loss repro harness (`tools/item_repro/`) | ran 2026-09-30; found the Sophisticated Backpacks wipe ([plan_1.0.3.md](plan_1.0.3.md) 4.7) |
| Slice & Dice `_manual_only` recipe ids | in use (2 recipes) |

- **Pending:** nothing built and unshipped. TL-01 to TL-16 are new. Findings F1 to F10 (section 4); F3 and F4 need
  your call.
- **Files to open:** [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js),
  [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js),
  [tag_helper.js](../kubejs/startup_scripts/func/tag_helper.js),
  [CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java),
  [dump_all.js](../kubejs/server_scripts/_lib/dump_all.js), [generators.json](../tools/generators.json).
- **The rules** (CLAUDE.md): recipes through `global.NCH`, tags through `global.tags`, every id defined once in a
  dictionary and referenced by key, and one writeup per feature.

## 1. Overview

KubeJS 1902.6.2 (Rhino fork) runs three script sets: `startup_scripts` (once, at game start), `server_scripts`
(every datapack load, `/reload`) and `client_scripts`. Every server script shares one top-level JS scope, so a
top-level `const` with the same name in two files kills the second file. Startup scripts load in filesystem-walk
order within a priority, so anything read at top level must come from a higher `// priority:`.

patcherMod (`creativeengineering`) is the pack's Forge coremod. It carries the Java side of most features, the
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
- **State:** 12,511 item keys; 55 keys are assigned twice to the same id (harmless); an "Unsorted (Possible
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
  from [transition_items.js](../kubejs/startup_scripts/changes/transition_items.js)).
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
folders through patcherMod because KubeJS can't).

| Command | Writes / used by |
|---|---|
| `/dumpbiomes`, `/dumpclimate`, `/dumpclimatecaves`, `/dumpspawns`, `/dumpstructures`, `/dumpstructuregen` | biome, climate, spawn and structure data ([weather.md](weather.md), [mob_spawns.md](mob_spawns.md), [structure_tuning.md](structure_tuning.md)) |
| `/dumpitemsbymod`, `/dumptags`, `/dumprecipes`, `/dumpcraftingaudit`, `/dumpfluids`, `/dumppotions`, `/dumpnames` | registries, tags, recipes, names ([jei_coverage.md](jei_coverage.md), [names.md](names.md)) |
| `/dumpworldjei` | world-interaction JEI facts (patcherMod `devtools/WorldJeiDump`; feeds `tools/gen_jei_world.py`, [jei_world_tabs.md](jei_world_tabs.md)) |
| `/dumpgear`, `/dumpstacks`, `/dumpfood`, `/dumpplaceable`, `/dumpcropgrowth`, `/dumpcompost`, `/dumppaintings` | per-feature tables ([gear_tuning.md](gear_tuning.md), [stack_sizes.md](stack_sizes.md), [food.md](food.md), ...) |
| `/dumploot`, `/dumptrades`, `/loottuning` | loot and trades ([loot.md](loot.md), [wandering_trader.md](wandering_trader.md)) |
| `/dumpbeenests [reset]` | measured nests per biome ([bee_nests.md](bee_nests.md)); **not** in `/dumpall` (a session that generated nothing would overwrite a measurement) |
| `/dumpservermods` (Java) | copies `both`/`server` jars (per `mod_sides.md`) into `server_mods/` |
| `/dumpall` | runs every dump above except `/dumpbeenests` through the real dispatcher, in order (quick registry dumps first, `/dumpclimate`, `/dumpclimatecaves` and `/dumpservermods` last; `/dumpclimate` takes about 4.5 minutes at its 20,000-column default); a failing dump is reported and skipped |

[java_guard.js](../kubejs/server_scripts/_lib/java_guard.js) `guardThrowable(fn)` catches `java.lang.Error`s that
Rhino's `try/catch` can't (a `NoClassDefFoundError` used to kill `/dumprecipes` silently).

### 2.6 Generators and `/cegen`

[tools/generators.json](../tools/generators.json) lists 28 Python generators in run order, each with `groups`
(`core`, `worldgen`, `loot`, `client`, `pack`, `reports`, `names`, `food`, `blockrunner`, `jei`, `slow`), the `dumps` it
reads, its `outputs` and an `apply` hint. patcherMod's `devtools/GeneratorRunner` runs them as child processes from the
instance root, hashes the outputs before and after, and reports changes.

| Command | |
|---|---|
| `/cegen` | list generators, groups, last run |
| `/cegen run [target] [dumps]` | `all`, a group or an id (default `[debug.generators] target` = `core`); `dumps` first runs the `/dump*` commands those scripts read |
| `/cegen status` / `cancel` | progress or last results; kill the running script and skip the rest |

Output goes to chat (status lines and highlighted lines), `latest.log` (`[cegen:id]`) and `logs/cegen/<id>.log`.
`[debug.generators] runAtStartup = true` runs the target in the background from common setup and waits for it at
load complete (up to `startupWaitSeconds`), so the first world load reads fresh files; results go to the first player
to join. Python and timeout are configurable (`python`, `timeoutSeconds`).

About 42 of the 70 Python tools aren't in the manifest; 5 are excluded on purpose (listed in its `_readme`), the rest are run by hand
(F5). Newest manifest entries: `soup_soft_fluids` (`tools/gen_soup_soft_fluids.py`, jar soft fluids, [veggies_delight.md](veggies_delight.md)) and `jei_world` (`tools/gen_jei_world.py` + `tools/jei_world/`, group `jei`, [jei_world_tabs.md](jei_world_tabs.md)).

### 2.7 Headless tests

| Script | Covers | Writeup |
|---|---|---|
| `tools/test_shields.sh` | `shield_check.py` + `ShieldConfigTest` | [shields.md](shields.md) 6 |
| `tools/test_crop_growth.sh` | growth maths + coverage | [crops.md](crops.md) 2.9 |
| `tools/test_wild_crops.sh`, `test_wild_flowers.sh`, `test_bee_nests.sh`, `test_mob_spawns.sh` | generator vs the real KubeJS worldgen script run in the pack's Rhino fork | [crops.md](crops.md), [flowers.md](flowers.md), [bee_nests.md](bee_nests.md), [mob_spawns.md](mob_spawns.md) |
| `tools/test_structure_groups.sh` | node sim vs Rhino, byte-identical | [structure_tuning.md](structure_tuning.md) 6 |
| `tools/test_worldgen_tools.sh` | 9 Java/node/Python checks on the worldgen code | [structure_tuning.md](structure_tuning.md) 6 |
| `tools/test_remainder_buffer.sh` | GameTests in a scratch ForgeGradle project with FD and Respite | [remainder_buffer.md](remainder_buffer.md) 6 |

- **How they run:** Java tests compile against `development/patcherMod/build/classes` (build first) on a classpath
  from [java_test_cp.sh](../tools/java_test_cp.sh): the mapped Forge jar in the ForgeGradle cache plus libraries
  from ATLauncher's folder (F7). Rhino tests run the real `.js` files in the pack's Rhino fork through a small Java
  runner (`tools/*_test/Rhino*.java`).
- **Node sims** without a runner script: `tools/blockrunner_tag_sim.js`, `loot_tuning_sim.js`,
  `structure_tuning_sim.js`, `structure_density_check.js`.
- **GameTests:** the Gradle runs set `forge.enabledGameTestNamespaces` to the mod id; the oven's 8 tests ran in a
  scratch project ([oven.md](oven.md) 6).

### 2.8 Root Gradle build

- `settings.gradle` includes `patcherMod`, `exposureBackport`, `incubationAdditions`, `moreMooblooms`,
  `backportedBrewin`, `backportedVinery`, `babyMobs`, `creativeLabels`, `backportedVeggies` from `development/`; project names stay the bare mod names (`:patcherMod`).
- `build-logic/` holds the `ce.forge-mod` convention plugin (ForgeGradle setup, official 1.19.2 mappings, Java 17,
  mods.toml expansion from `<mod>/gradle.properties`, jar manifest with mixin configs, dev runs, `installToMods`) and
  `ce.forge-mod-refmap` for mods with real Mixin refmaps (exposureBackport).
- Shared versions in the root `gradle.properties` (Forge 43.3.9, `org.gradle.java.home`); per-mod ids and versions in
  each mod's `gradle.properties` (`replaces_jars` moves a replaced third-party jar out of `mods/`).
- `./gradlew :patcherMod:installToMods` builds and swaps the jar into `mods/`, moving the old one to
  `development/<mod>/jar_archive/displaced/`. `development/<mod>/libs/` (compile-only copies of mod jars) is
  gitignored. The root [README.md](../README.md) has the commands.
- patcherMod has no refmap and only runs reobfuscated: its mixins on vanilla classes use SRG names with
  `remap = false`, and reflection uses SRG strings (`m_20115_`, `f_43494_`).

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
`wood_pellets_manual_only` ([sawdust.js](../kubejs/server_scripts/wood/sawdust.js) line 57). The crab bars id uses the
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

- **F1. `item_dict` probably reports every tag key as missing.** Its tag check runs at `StartupEvents.postInit`, before
  any datapack (and so any tag) has loaded, so `Ingredient.of('#...').itemIds` is empty for all 238 tag keys. Check
  `logs/latest.log` for `[item_dict] Missing/invalid item IDs` (TL-01). If so, move the tag half to a server-side
  check (`ServerEvents.tags` or the first recipe event).
- **F2. Block, fluid and NCH fluid tags aren't validated** (each logs "not validated (tag support not yet
  implemented)"). In [TODO](todo/TODO.md) `tooling/dictionaries #2`.
- **F3.** DELETED 2026-10-22. **`global.config` was dead.** `func/config.js` (a self-healing JSON config under
  `config/creativeengineering/`) has no caller; dragon autorepair, its first user, moved to CEConfig, and the folder
  didn't exist.
- **F4. Test recipes ship in the pack. Your call.** In `server_scripts/_lib/`:
  - `metal_test_recipes.js`: a mechanical crafting recipe per metal (15 metals + netherite) that takes one of every form
    of the metal (ore to component, up to 20 items) for 1 ingot;
  - `wood_test_recipes.js`: the same idea for wood;
  - `sequence_assembly_processes.js`: a nether star → nether star sequenced assembly using every legal assembly step.

  They show in JEI and are craftable (each is a loss, so no exploit). Keep as showcases, or gate them.
- **F5. Many Python tools aren't in `generators.json`.** 33 of 55: some on purpose (the `_readme` names 5), but reports
  like `food_catalog.py`, `stack_sizes.py`, `jei_coverage.py`, `wandering_trader_report.py`, `loot_breakdown.py` and
  `gen_crop_worldgen_table.py` could be `/cegen` entries with their dumps.
- **F6.** FIXED 2026-10-19 (all three). **Stale references:** `test_crop_growth.sh` names `feature_writeup/crop_growth.md` (it's [crops.md](crops.md)
  2.9); `neoCraftHelper.js` line 1 says `kubejs/server_scripts/helpers/recipes.js`; the `NCH.cfg` example uses toml
  keys instead of field names ([gear_tuning.md](gear_tuning.md) F12).
- **F7. Headless tests need this machine's caches.** `java_test_cp.sh` reads `~/.local/share/atlauncher/libraries` and
  the ForgeGradle cache's mapped Forge jar; the remainder-buffer GameTests download FD and Respite. A fresh clone (or a
  cloud session) can't run the Java parts until those exist.
- **F8. Duplicate dict keys:** 55 keys are assigned twice (same id each time). Harmless; the "Unsorted (Possible
  duplicates)" section is where to start.
- **F9. Convention drift found by the writeups:** raw `e.custom` in `potion_bottling.js` and `tipped_arrows.js`
  (`NCH.ieBottling` exists; a `createEmptying` helper doesn't), raw tag `e.add` in `equipmenttags.js` (44 lines) and
  literal entity ids in `entitytags.js` (no entity dict). See [potions.md](potions.md) F6, [archery.md](archery.md)
  F4, [mob_block_tweaks.md](mob_block_tweaks.md) F1.
- **F10. Removed items don't remove their recipes.** `recipe_scan.js` only lists them; each removal is by hand. Fine
  as a design, but a removed item's recipe survives until someone acts on the list.

### Known limits

- **Rhino gotchas** (each documented where it bit): shared server-script scope; loop-body `const` / `let` behave like
  `var`; same-arity Java overloads are ambiguous (`PotionUtils.getColor`, `ItemStack.is`); some Mojang names are
  hidden (`getTag`, `getUUID`) and some fields shadow getters (`level`, `x`); `.class` doesn't exist
  (`__javaObject__`); `try/catch` misses `java.lang.Error`.
- **KubeJS class filter** blocks `java.io` / `java.nio`, so file copies and folder creation go through patcherMod.
- **Startup changes need a restart;** server scripts and tags reload with `/reload`.

### Rejected options

- **Per-feature JSON configs (`global.config`):** replaced by CEConfig for the in-game editor (F3).
- **Running generators only by hand:** `/cegen` keeps the dumps and generators in step and shows what changed.

## 5. File map

- Dictionaries: [startup_scripts/_lib/](../kubejs/startup_scripts/_lib/) (`item_dict.js`, `block_dict.js`, `fluid_dict.js`, `effect_dict.js`, `multiblock_dict.js`, `removed_items.js`); worklists in [startup_scripts/todo/](../kubejs/startup_scripts/todo/).
- Helpers: [startup_scripts/func/](../kubejs/startup_scripts/func/) (`neoCraftHelper.js`, `tag_helper.js`, plus the feature helpers: `tune_tools.js`, `armor_effects.js`, `compost.js`, `trade_helper.js`, `jei_order.js`, `item_physics.js`, ...); [changes/transition_items.js](../kubejs/startup_scripts/changes/transition_items.js).
- Server `_lib/`: the `dump_*.js` commands, `dump_all.js`, `export_io.js`, `java_guard.js`, `recipe_scan.js`, `loot_dump.js`, `structure_dump.js`, `villager_trades_dump.js`, and the test recipes (F4).
- patcherMod: [CEConfig.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java), [devtools/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/devtools/) (`GeneratorCommand`, `GeneratorRunner`, `ServerModsDump`, `BlockStateSyncDebug`).
- Tools: [tools/generators.json](../tools/generators.json), `tools/*.py` (generators, reports, scans), `tools/test_*.sh` + `tools/*_test/`, [java_test_cp.sh](../tools/java_test_cp.sh), [lang_index.py](../tools/lang_index.py) (shared paths and jar/lang lookups for the Python tools), [tools/item_repro/](../tools/item_repro/README.md), [tools/perf/](../tools/perf/) ([perf.md](perf.md)), [build_deploy.py](../tools/build_deploy.py) ([deploy.md](deploy.md)), [gen_pack_manifest.py](../tools/gen_pack_manifest.py), [dump_server_mods.py](../tools/dump_server_mods.py), [decompile_mod.py](../tools/decompile_mod.py).
- Build: [settings.gradle](../settings.gradle), [gradle.properties](../gradle.properties), [build-logic/](../build-logic/), [README.md](../README.md).

## 6. Offline checks

- The `tools/test_*.sh` suites (2.7), each against its writeup.
- `node tools/blockrunner_tag_sim.js` runs the real `tag_helper.js` in node against the last `/dumptags` export, so it
  also exercises the tag helper.
- Most generators have a `--check` mode that exits non-zero when their output is stale.
- The dictionary checks only run in game (F1).

## 7. In-game test suite

- [ ] **TL-01 Dict validation (F1):** boot, open `logs/latest.log` → note whether `[item_dict] Missing/invalid item IDs`
  lists tag keys (`... (tag not found or empty)`), and which real item ids it lists.
- [ ] **TL-02 NCH strict:** add `R.shaped('minecraft:stone', ['A'], { A: 'minecraft:not_an_item' }, e)` to a scratch
  server script, `/reload` → the script errors with `[NCH] ... does not exist`; with `global.NCH.strict = false` it warns
  instead. Remove the line.
- [ ] **TL-03 Recipe ids:** JEI on any pack recipe, hover the recipe id (advanced tooltips) → `creativeengineering:<type>/<suffix>`.
- [ ] **TL-04 Removed items:** a removed item (`/give` one, e.g. a NAND gate) → hidden from JEI, red "REMOVED ITEM" line,
  no item tags (F3+H shows none).
- [ ] **TL-05 CEConfig live edit:** change `[dragon_gear.autorepair] intervalTicks` in Create's config screen → the
  toml changes on disk without restart. Put it back.
- [ ] **TL-06 String field:** in Create's config screen, `[crafting.felsic_sword] potion` is an editable text field, not
  "Impl missing".
- [ ] **TL-07 `/dumpall`:** run it → chat ends with `[dumpall] all 22 dumps done`; `kubejs/exported/` timestamps
  updated; `server_mods/` refreshed.
- [ ] **TL-08 A dump failing:** (if one fails) `/dumpall` reports it and carries on with the rest.
- [ ] **TL-09 `/cegen` list:** `/cegen` → the 22 generators, the groups and the last run.
- [ ] **TL-10 `/cegen run core`:** runs the core group in the background, one chat line per script, changed outputs
  named; `logs/cegen/structure_config.log` exists.
- [ ] **TL-11 `/cegen run loot dumps`:** runs `/dumploot` and `/dumptags item` first, then the loot generators.
- [ ] **TL-12 `/cegen cancel`:** start `/cegen run all`, cancel → the running script is killed, the rest skipped,
  `/cegen status` says so.
- [ ] **TL-13 Startup run:** `[debug.generators] runAtStartup = true`, restart → the log shows the core generators
  running during load; the first player to join gets the results. Turn it off.
- [ ] **TL-14 `/dumpservermods`:** copies only `both` / `server` jars into `server_mods/`; with a jar missing from
  `mod_sides.md` it writes nothing and names the jar.
- [ ] **TL-15 Gradle:** `./gradlew :patcherMod:build` succeeds (libs populated); `./gradlew :patcherMod:installToMods`
  moves the old jar to `jar_archive/displaced/`.
- [ ] **TL-16 `_manual_only`:** JEI on cooking oil → the cooking-pot recipe (2 oil from 6 ingredients) and a separate
  heated mixing recipe (1 oil from 3 + a bottle); no mixer copy of the pot recipe.

## 8. History

- Before 2026-09-27: dictionaries, NCH (migrated from the older `global.CE`, whose commented-out blocks in
  three files were deleted 2026-10-22, [TODO](todo/TODO.md) `tooling/conventions #3`), tag helper, CEConfig, the first dump commands.
- 2026-09-27 (initial git commit): root Gradle build and manifest tooling.
- 2026-09-30: item-loss repro harness run ([plan_1.0.3.md](plan_1.0.3.md) 4.7).
- 2026-09-27 to 10-02 (first commits): `/dumpall` and `generators.json` (09-27), `java_test_cp.sh` (09-28), the
  `/cegen` command (10-02). The Rhino, node and GameTest harnesses and more dumps came with their features (each
  dated in its feature writeup).
- 2026-10-18: this writeup; findings F1 to F10; tests TL-01 to TL-16. No code changed.
- 2026-10-19: F6 fixed (test_crop_growth.sh, neoCraftHelper.js line 1, NCH.cfg example).
- 2026-10-22: `R.replaceInput` / `R.replaceOutput` take `includeAdded` (2.2), found in the second scratch bulk run
  (SQ-CO-01: Canned Coffee Berries still took Respite's berries).
