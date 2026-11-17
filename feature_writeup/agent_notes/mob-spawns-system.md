# mob-spawns-system

> Mob spawns: /dumpspawns + gen_spawn_table.py + mob_spawns.json (rules, conditions light_only/water_only, spawn_ground) via tune_spawns; section 9 first block applied 2026-10-09, scratch-server verified, jar not in mods/

Moved from agent memory 2026-11-16 (last edited there 2026-10-10). Repo status docs win over any status line here.

Built 2026-10-09 from feature_writeup/mob_spawns.md (MS-01..23). 2026-10-09 late: the FIRST block of section 9 is
applied (12 rules, 3 conditions, spawn_ground; Shiba off via Quark `Shiba = false`, user's choice over a remove rule).
"Investigate" and "Later" blocks (AM parity, pillager overhaul, lone villagers) are NOT done.
User decisions: land animals = ground (grass + #minecraft:dirt) + light, no daytime-on-stone; "birds" = flying birds only
(parrot + AM crow/seagull/eagle/sunbird/toucan/blue jay/hummingbird/potoo); lakes/ponds = ducks in grassy land biomes, water_only.
- conditions ride in the tune_spawns modifier (`conditions` field) -> SpawnConditions.install in modify();
  SpawnPlacementsConditionsMixin (m_217074_ HEAD) for NATURAL/CHUNK_GENERATION; Animal/MushroomCow walk value mixins
  (m_5610_) read the spawnable_on tags so night spawns on dirt pass. Block tags only written when SpawnTuning exists.
- Scratch server 2026-10-09: log `12 rules, 94 biomes changed, 286 changes; conditions: ...` = generator preview; probes OK.
  Jar in development/patcherMod/build/libs. 4 mods lack mod_sides.md rows (breedablekillerrabbit, coolrain, MEED, Pretty Rain).

- `/dumpspawns` (kubejs _lib/dump_spawns.js, in /dumpall) is a thin wrapper over patcherMod `worldgen/SpawnDump`
  (Rhino can't read spawn lists, see memory `rhino-fieldandmethods-gotcha` #13). Spawn predicates are lambdas: dump names the
  registering class; vanilla rule names come from tools/spawn_rules_vanilla.json (javap of SpawnPlacements.<clinit>
  in the ForgeGradle mapped jar, `--refresh-vanilla-rules`).
- Tuning: rules (set/scale/remove, where/add/except like wild_crops.json, traits shared from its biome table) ->
  worldgen/mob_spawns.js writes ONE modifier (rule order deterministic) -> `SpawnTuning` runs in AFTER_EVERYTHING and
  records per-biome before/changes for the dump. Applies to existing worlds on rejoin. CEConfig `[worldgen.systems] mobSpawns`.
- Generator applies the file to the dump's untuned lists (offline preview) and warns when the dump ran other rules.
- `bash tools/test_mob_spawns.sh`: Java/Python parity on real vanilla lists (Bootstrap), Rhino run, fixture report. ALL OK 2026-10-09.
- A real /dumpspawns exists (2026-10-09 21:58); spawns_by_biome.md generated from it 2026-10-09. Still not done: cow table in mooblooms.md.
- 2026-10-09 passive-animal analysis (artifact https://claude.ai/artifact/1BPHFNwL5nh9zVzJrqrgWT, builder in session scratchpad only):
  farm block sheep12/pig10/chicken10/cow8 identical in 63 of 78 biomes (Terralith); #animals_spawnable_on = grass_block only;
  herd sizes from Terralith (8 / 4 swamp+dark+birch+sky / 6 wintry / 2-4 meadow); duck+goose only 3 river biomes,
  NO_RESTRICTIONS + always-true predicate + liquid-allowing obstruction (spawn on water, daylight unless on grass);
  AM checkSpawnRules REPLACES vanilla checks, rollSpawn = 1-in-N (only sunbird 15 > 1); AM biome lists in config/alexsmobs/*_spawns.json.
  Next step the user is choosing: cohesion rules for mob_spawns.json.

**How to apply:** for a section 9 plan, add rules to mob_spawns.json, run the generator to preview, rejoin, /dumpspawns.
Related: [vegetation-worldgen-system](vegetation-worldgen-system.md), [moobloom-variants](moobloom-variants.md), memory `feature-writeup-pattern`, memory `kubejs-headless-rhino-harness`.
