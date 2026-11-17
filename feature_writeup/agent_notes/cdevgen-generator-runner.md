# cdevgen-generator-runner

> /cdevgen runs the tools/*.py generators in game (patcherMod devtools/GeneratorRunner + GeneratorCommand) from the tools/generators.json manifest; optional startup run via CEConfig [debug.generators]

Moved from agent memory 2026-11-16 (last edited there 2026-09-27). Repo status docs win over any status line here.

Built 2026-09-27, headless-tested (22 checks) yet (the game was running at build time).

- **Manifest `tools/generators.json`** is the single list: id, script, groups, dumps, outputs, apply, warnExit, highlight. Order = run order (gen_structure_config before build_biome_merges, which reads structure_config.json). A new generator script means a new manifest entry, no jar rebuild.
- Groups: `core` (structure_config, biome_merges, biome_config, loot_config, effect_descriptions, pack_manifest, about 10 s total), `reports` (analyzer, structure_live_check, loot_container_audit + loot_census at about 44 s each, blockrunner_scan), plus worldgen/loot/client/pack/slow/blockrunner.
- `/cdevgen run [target] [dumps]`: "dumps" runs the listed /dump* commands on the server thread first. Scripts run sequentially on a daemon thread; chat shows status, changed outputs (SHA-256 before and after), the apply hint and highlighted lines. Full output goes to `logs/cdevgen/<id>.log` and latest.log `[cdevgen:id]`.
- Startup: `[debug.generators] runAtStartup` starts the run in FMLCommonSetupEvent and joins it in FMLLoadCompleteEvent, which comes before any datapack load on both client and server. The first joining op or singleplayer player gets the summary.
- Every tools/*.py generator or report is an entry or named in the manifest `_readme` with its reason (since 2026-10-27, pack_hygiene 44; 45 entries). Out on purpose: dump_server_mods.py (the only server_mods/ route since /dumpservermods was removed 2026-10-27; run by hand, copies ~300 jars), tools needing args, jar-rebuild resource writers (bedspreads/elytra, dd_dishes, moobloom variants, compost values), --check-first hand workflows (gen_can_types, cfb fridge), print-only checks, libraries.
- Shared helpers for new tools: tools/pack_paths.py (paths, item_dict parser, loads_lenient, vanilla/Forge jar locator from instance.json); lang via lang_index.build(); template containers via loot_breakdown_scan.scan().
- Headless test recipe: memory `patchermod-headless-smoke-test`, plus guava 31.0.1, log4j-api 2.17.0, DFU 5.0.28, brigadier, fastutil and commons-lang3 for CEConfig's static init.

Related: [patchermod-coremod](patchermod-coremod.md), memory `feature-writeup-pattern` (loot.md / structure_tuning.md §6 mention it; pack tooling writeup TL- is backlog #11).
