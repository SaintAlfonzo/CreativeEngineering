# Creative Worldgen

Written 2026-11-02 (Creative Split wave 5, [creative_split.md](creative_split.md) 7.6 and 7.10; TODO `creativesplit/worldgen #8`).
Status: **carved 2026-11-02, built, installed 2026-11-02 23:16 (`mods/creativeworldgen-1.0.0.jar`, with patcherMod 1.1.08 and the CD jar; moreMooblooms 1.1.0 displaced); scratch SQ-CWG queued, nothing booted.** Creative Delight's Miner's drops need this jar (`loot/09_drops.js`, miners_delight.md 8.1).

Written for: the owner and agents working on the split. Own mod `creativeworldgen` (development/creativeWorldgen,
`creativeworldgen-1.0.0.jar`, side both): the pack's world generation and fauna. A bundle like Creative Delight: its
own Java plus two nested members. Same behavior as before the carve; every registry id keeps `creativeengineering:`.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=67f614f8ae9e -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativebrewing/publish #6` | Testing | S | Creative Worldgen breaks world creation without Terralith |
| `creativesplit/worldgen #8` | Testing | S~ C | Carve Creative Worldgen with fauna |
<!-- generated:status_index end open_work -->

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativeworldgen`, same subpackages as in patcherMod; 59 classes plus 4 new):
  - `worldgen/` whole (structures, meta structures with the punch piece and meteorite heights, locate, earth and
    water fill, terrain adaptation, ground match, sky islands, bee nests, biome layout tuner, pool tuner, spawn
    tuning and conditions, spawn boxes, ocean geodes, distant clip, `donor/` Farmer's Structures donor).
  - `compat/AsyncLocatorBridge`, `AsyncMapTrades`; `mobs/NamedMobs`; `mooshroom/` (all mooshroom spawning and
    breeding, 7.6); `forgivingvoid/`; `loot/BiomeTagCondition`; `perf/MobCapLevels`; `block/FastLeafDecayPort`,
    `LeafDecayPiles`, `LeafDecayRange`.
  - New: `CreativeWorldgen` (main class, every registration that left `CreativeEngineering.java`; the
    `DeferredRegister`s use namespace `creativeengineering`, `CreativeWorldgen.NS`), `CWGConfig`,
    `WorldgenMixinPlugin` (a `GatedMixinPlugin`). `CWGResources` (the loot framework's json reader) moved to Creative
    Loot as `CLResources` on 2026-11-17 ([creative_loot.md](creative_loot.md)).
- **Mixins:** `creativeworldgen.mixins.json`, 60 common, none client, SRG with `remap = false`, member prefixes still
  `creativeengineering$`. The 54 sweep B Worldgen rows, the two mooshroom mating mixins, `NaturalSpawnerBoxHeightMixin`,
  `StructureStartMetaMonumentMixin`, `QuarkSpiralSpireFootprintMixin`, `AeMeteoriteHeightMixin` (the last four
  postdate the audit), and `BubbleColumnImmunityMixin` (its entity tag moved with `entitytags.js`). Gated on their
  mod: LC and Cloud Storage map trades, Forgiving Void, Bushier Flowers, two Quark, two MCA, AE2. The rest target
  vanilla. `QuarkSpiralSpiresTeleportMixin` stays in Common (a perf fix).
- **Config:** `config/creativeworldgen-common.toml` (`CWGConfig`): `[forgiving_void]` (3 subtables), `[leaf_decay]`,
  `[raids]`, `[perf]` (the 13 mob cap and spawn chunk / band keys only), `[worldgen]` with every subtable,
  `[named_mobs]`. Keys, comments, defaults and field names unchanged. First boot after the carve: Common's
  `ConfigRename.carrySections` copies the tables with the player's values from `creativeengineering-common.toml`
  (only when the new file does not exist); Forge then drops them from the CE file. NCH finds the sections through
  `global.NCH.registerConfig('creativeworldgen', ...CWGConfig)` (startup `cwg_core.js`).
- **Resources in the jar** (same paths, `creativeengineering` namespace): `data/creativeengineering/dimension` (11),
  `dimension_type` (1), `worldgen/noise_settings` (3), the named-mob chat lang key; the data tree
  `data/moremooblooms/moobloom_spawns/` (authored in the pack folder, copied at build).
- **Scripts:** startup `kubejs_startup/creativeworldgen/cwg_core.js` (`global.CWG`: `modId`, `on()`, `recipes()`);
  server `kubejs/data/creativeworldgen/kubejs/`: `worldgen/` (11), `other/removed_mobs.js`, `tags/entitytags.js`, the Worldgen
  half of `blocktags.js` (villager ladders, ground match, earth fill, template trim, wild plant soils),
  `mobs/end_divers.js`; opened by `_worldgen_core.js`. No client tree. No recipes, so no recipe id changes. The loot
  framework (`loot/`, its json tables, `loottags.js`, `global.CWG.lootRules`) moved to Creative Loot on 2026-11-17
  ([creative_loot.md](creative_loot.md)), as `global.LOOT.rules`.
- **Members** (`development/creativeWorldgen/members/`, mod ids unchanged, no swap list): More Mooblooms
  (`moremooblooms`, a standalone jar before) and Baby Mobs (`babymobs`, in Creative Delight 2026-11-01 to 2026-11-02).

## 2. Edges

- Common never imports Worldgen: after the move only `CreativeEngineering.java` and `CEConfig` named moved classes, and
  both were trimmed. No new Common hook was needed. `WthitHive` reads no Worldgen class.
- Devtools (`PerfCommand` mobcap, `SpawnDump` tuning block, `MassPlacer`) calls Worldgen behind
  `ModList.isLoaded("creativeworldgen")`; optional dependency in its mods.toml.
- **Loot** is Creative Loot's since 2026-11-17 ([creative_loot.md](creative_loot.md)): Worldgen requires `creativeloot`
  (mandatory, AFTER) and ships no loot script of its own.
- `global.biomeRules` (Worldgen `_biome_rules.js`): CD's `wild_crops.js` reads it at event time and warns once and
  skips when it is missing.
- Requires `creativecommon [1.1.0,)`, `creativecompat [1.0.0,)` and `creativeloot [1.0.0,)` (since 2026-11-17), mandatory AFTER.

## 3. Left in the pack folder or elsewhere on purpose

- The structure datapacks (49 namespaces), Eugene, `watchtower_building` and the `creativeengineering` worldgen data
  (incl. `waystone_ruin_part`, `moobloom_cows.json`): the structure generators (`tools/gen_structure_config.py` and
  the scanners) tell jar sets from kubejs/data sets by source, so moving them changes their output.
- Overrides of loaded mods' data (`campsite_structures`, `ipo`, `philipsruins`, `lightmanscurrency` coin loot,
  `terralith`, `minecraft/` and the rest of E 3.2): a mod jar cannot sort above the target mod's jar.
- `dimension/PortalFailureHandler` (Creative Magic, wave 6); `trader/` and `[wandering_trader]` (Common, 7.6);
  `WorldgenFiles` (Common `util/`).
- Deleted: `watchtower_building/advancements/watchtower_found.json` (the pack drops advancements).

## 4. Known effects of the move

- The biome layout disk cache rebuilds once (the solver class bytes changed).
- Event listener order shifts a little: Worldgen's registrations run in its constructor, after patcherMod and Compat.
- LootJS modifier order: Worldgen's post-roll modifier now registers after the pack's and CD's item-merge modifiers.
- `95_tag_capture` sees tag additions from packs that load before Worldgen (more than before, not less).

### 4.1 Fixes of 2026-11-06 night (after scratch r1106spf)

- `compat/ModDataPacks`: a `packs/<modA>+<modB>/` folder is added only while every listed mod is loaded (same change in
  Equipment).
- `creativeengineering:sky_island` has a structure set (`structure_set/sky_island.json`, baseline 64 / 16) and is a member
  of group `sky` (weight 100, frequency 0.985; structure_tuning.md "Floating islands"), so lone islands generate as well as
  the sky village's. `structure_config.json` regenerated with the new jar (only that set added).
- Every Worldgen script already had `// requires: creativeworldgen`; Devtools' `dump_bee_nests.js` and
  `dump_structuregen.js` now also require Worldgen.
- Tests SQ-SPFX-04, -05, -07: PASS r1106spfx (sky_island 1120 blocks out, floating; scratch_run_2026-11-06.md 7).

## 5. Tests

- **r1103 2026-11-03** (scratch server; verdicts in `perf/runs/r1103/` FINAL.md and the slice judged.md): PASS SQ-CWG-01, -02, -02b, -03, -04, -05, -09, -10, -08b, -07; FAIL SQ-CWG-06: the punch meta `meteorite_desert_pyramid` is dropped: `META_PART_KEYS` lacks `punch` (`structure_overrides.js` line 110); SQ-CWG-08o BATCH-FAULT: the forceload does not cover a plains village; rerun sA_R.qb part 2 (old 1.1.06 world); SQ-CWG-08 OPEN: mobs, breeding, leaf decay, mobcap, dumpspawns pass; the village read-back waits for SQ-CWG-08o.
- r1103 boot fix (runner, 2026-11-03): `kubejs/data/creativeworldgen/kubejs/loot/README.md` renamed `readme.md`; KubeJS refused the upper-case file name and stopped startup. Also found: the jar ships older `loot_config.json` / `loot_index.json` than `kubejs/data`, so rebuild creativeworldgen before a release.

Scratch SQ-CWG-01 to -10 ([scratch_queue.md](testing/scratch_queue.md)). In game (client rows pinned until wave 8+,
7.8): CWG-01 More Mooblooms and baby squid render with models; CWG-02 `/cwglocate` and a map trade from a cartographer.

## 5a. Namespace move (2026-11-10, BUILT, not installed)

Every id this jar owns moved from `creativeengineering:` to `creativeworldgen:` ([namespace_move.md](namespace_move.md)
step 6): dimensions, `hub_type`, noise settings, structures, sets, pools, placed features, biome modifiers and their
codecs, the `meta` structure and piece types, all tags (`mod/creativeengineering` is now `mod/creativeworldgen`) and
config defaults. How old worlds carry over:
- Biome modifier codecs: swap list `data/creativeworldgen/swaps/owner_namespace.json`.
- Dimension, dimension type and noise keys: `mixin/ResourceKeyLegacyDimensionMixin` (HEAD of `ResourceKey.create`)
  through `worldgen/LegacyIds.keyAlias`; only the listed ids, so nothing else is aliased. Covers level.dat, player data,
  Waystones, FTB Chunks, portal targets and typed `/execute in`.
- Dimension folders: `mixin/LevelStorageAccessDimensionMoveMixin` runs `worldgen/DimensionMove` after the session lock
  and before level.dat is read; if both folders exist it leaves both and warns.
- Structure starts, references and pieces in chunk NBT: `ChunkSerializerLegacyStructuresMixin`,
  `StructureCheckLegacyStructuresMixin` (`LegacyIds.upgradeStructures`); renamed only when the old id is unregistered
  and the new one registered. Our meta pieces read old ids too (`MetaStructure`, `MetaFeaturePiece`).
- Old config values: `LegacyIds.configIds`.
- Generation stays identical: hash seeds keep the old strings (`LegacyIds.seed`, `DistantClip.FLOOR_RANDOM` under
  `LEGACY_NS`, `group_salt`, the overworld `surface_mix/*` names).
- Not migrated: client caches keyed by dimension (Xaero maps, Distant Horizons LODs), structure ids other mods keep in
  their own data. Tests SQ-NSM-07, -08.

## 6. History

- 2026-11-17: members renamed (plan_rules_fixes.md section 14): `moremooblooms` and `babymobs`; the constructor carries
  their old config files over (`ConfigRename.carryOver`); the pack data tree is `data/moremooblooms/moobloom_spawns`.
- 2026-11-17: the loot framework moved to Creative Loot (plan_rules_fixes.md section 5b): `loot/`, its json tables,
  `tags/loottags.js`, `CWGResources` (now `CLResources`) and `global.CWG.lootRules` (now `global.LOOT.rules`); the LootJS
  dependency went with it, Worldgen requires `creativeloot`. Rebuilt and installed with `creativeloot-1.0.0.jar`.

- 2026-11-02: carved (wave 5). patcherMod 1.1.08 loses the classes, 60 mixins, 6 sections and the resources; More
  Mooblooms and Baby Mobs nested; Devtools gains the optional dependency. Edits that the worldgen edit pass
  ([plan_worldgen_edit.md](plan_worldgen_edit.md), same day) made to `MetaStructure` and its new classes moved with it.
- 2026-11-03 (plan_scratch_bugfix_1103 4.1, 4.5, 7.3): `structure_overrides.js` accepts and checks the meta part key
  `punch` (boolean, structure parts with `overlap` only, as `MetaStructure.Part`), so `meta/meteorite_desert_pyramid`
  loads (SQ-CWG-06, SQ-WGE-12). `FirstTickWater`: the settling flag is cleared every level tick and checked per thread
  (perf.md 10.6). Loot: Botania and CC:Tweaked out of village chests (`07_rules.js` restores only Chalk, `10_removals.js`
  removes their native injections). Rebuilt `creativeworldgen-1.0.0.jar` 2026-11-03, which also carries the current
  `loot_config.json` / `loot_index.json`; not installed.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-10-02: In unexplored terrain that loads chunks synchronously up to 100 chunks out: a scratch server's /dumptrades froze the tick for 60 s until the watchdog killed it (2026-10-02, LC), and Cloud Storage's pair alone held a tick for ~20 s (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/compat/AsyncMapTrades.java)
- 2026-10-08: Which templates carry water (2026-10-08 scan of mods/*.jar): Hopo's ruins and fossils (30 to 7,700 water blocks each), the T&T ocean outpost plate (10,000, y 57-62, flush with the sea), the T&T ocean village escort ships, Farmer's Structures sushi stall and fish ponds, the swamp fisherman house (a pond under it) (kubejs/data/creativeworldgen/kubejs/worldgen/water_fill.js)
- 2026-10-11: The cache is also kept on disk (biome_layout_cache.json.gz, ships with the pack), so the first datapack load of a new JVM reads it back instead of re-solving (2026-10-11 s) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/BiomeLayoutTuner.java)
- 2026-10-14: BIOME FACTORS (a group's "biome_frequency", 2026-10-14, plan 7.4.5 D4): every member of the set rolls frequency x the factor of the biome at its generation point (the first rule the biome matches, else "default"), the point and biome Structure#generate tests (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/StructureFrequencies.java)
- 2026-10-14: BIOME FREQUENCY (a group's "biome_frequency", 2026-10-14): {"#biome tag" | "biome id": factor 0..1, ..., "default": factor} (kubejs/data/creativeworldgen/kubejs/worldgen/structure_overrides.js)
- 2026-10-19: Sunk starts (2026-10-19): start_height below 0 sinks these templates into the ground on purpose, and the beard levels the ground at the surface they were projected to instead of at their sunk bottom layer (patcherMod TerrainAdaptation#sink) (kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js)
- 2026-10-20: Glowing Illagers port (feature_writeup/feature_porting.md 2.1, jar retired 2026-10-20): every raider a raid adds to a wave ({@code Raid#addWaveMob}, {@code m_37718_}) glows for good, so a raid can be followed through walls (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/mixin/RaidGlowingMixin.java)
- 2026-10-25: The End House (Ender's Delight, 2026-10-25) stands on a purpur and end stone floor (layer 0: 101 of its 144 columns) (kubejs/data/creativeworldgen/kubejs/worldgen/earth_fill.js)
- 2026-10-25: End builds (2026-10-25): Ender's Delight's End House (9x16 purpur and end stone floor, shipped by Creative Delight's enders member) (kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js)
- 2026-10-30: Batch 4 (2026-10-30, TODO structures/terrain #1) (kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js)
- 2026-10-30: Seabed clearance (2026-10-30, TODO structures/terrain #7; patcherMod SeabedClearance): underground structures at a fixed height (Terralith's caves y -40..40, Subterrestrial's land cabins y 6..48) whose box top comes within 8 blocks of a floor with water on it are dropped; their beard carved the seabed open and the sea flooded them (kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js)
- 2026-10-30: COASTS AND RIVERS (CWGConfig [worldgen.meta_structures] avoidCoasts, on by default since 2026-10-30): no part may start in, or reach with its box (center and corners) into, a biome of creativeworldgen:coast_river (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/MetaStructure.java)
- 2026-10-30: Structures that stand in water on purpose (stilts in a swamp or mangrove): no footprint water share, their other earth-fill limits as usual (2026-10-30, TODO structures/earthfill #14: every surface piece now joins the footprint, not only pieces with earth) (kubejs/data/creativeworldgen/kubejs/worldgen/earth_fill.js)
- 2026-10-30: A part short of its minimum count keeps what fitted (CWGConfig [worldgen.meta_structures] keepShortParts, since 2026-10-30); the meta fails only when no satellite fitted at all (without the switch: whenever a part is short), and the group's weighted pick falls through to its next member, as for a wrong biome (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/MetaStructure.java)
- 2026-10-30: Every other surface jigsaw structure drops only its too-wet pieces (creativeworldgen-common.toml pruneWetJigsaws, 2026-10-30); meta structure parts and anchors are pruned the same way, part by part (kubejs/data/creativeworldgen/kubejs/worldgen/earth_fill.js)
- 2026-11-01: Their start element is terrain matching (patcherMod refuses them), so they keep their own placement (owner call 2026-11-01, TODO structures/earthfill #17). the carp pond (batch 4): a 4-layer dirt bowl under its grass rim, start height -4 (kubejs/data/creativeworldgen/kubejs/worldgen/terrain_adaptation.js)
- 2026-11-02: Before 2026-11-02 the stored current always won, so 223 duplicates in 27 pools played another piece's weight (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/PoolTuner.java)
- 2026-11-02: Moved from kubejs/server_scripts/ on 2026-11-02; priorities unchanged, and they order only within this tree (kubejs/data/creativeworldgen/kubejs/_worldgen_core.js)
- 2026-11-02: Sandstone and terracotta floors, counted as earth only for structures in the structure tag of the same name (worldgen/earth_fill.js): desert and badlands builds that stand on them instead of sand (2026-11-02, tools/earth_base_scan.py: Towns and Towers badlands villages 0 % earth, Better Desert Temples 0 %) (kubejs/data/creativeworldgen/kubejs/tags/blocktags.js)
- 2026-11-02: Desert and badlands structures on sandstone or terracotta floors: those blocks count as earth too (2026-11-02) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFill.java)
- 2026-11-02: Creative Worldgen: block tags of the worldgen and fauna systems; moved from kubejs/server_scripts/tags/blocktags.js on 2026-11-02 (villager ladders, structure ground match, earth fill, template trim, wild plant soils) (kubejs/data/creativeworldgen/kubejs/tags/blocktags.js)
- 2026-11-02: The sections moved out of patcherMod's creativeengineering-common.toml on 2026-11-02 (creative_split.md 7.6, wave 5) with keys, comments and defaults unchanged: {@code [forgiving_void]}, {@code [leaf_decay]}, {@code [raids]}, the mob cap and spawn area keys of {@code [perf]}, {@code [worldgen]} with every subtable, and {@code [named_mobs]} (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/CWGConfig.java)
- 2026-11-02: creativeWorldgen: Creative Worldgen (mod id creativeworldgen, feature_writeup/creative_worldgen.md), carved out of patcherMod on 2026-11-02 (creative_split.md 7.6, wave 5) (development/creativeWorldgen/build.gradle)
- 2026-11-02: open down to the piece's bottom (y - bottom of its own layers), then more than 'fills' below it; water by its own, shorter reach (waterReach, 2026-11-02) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFill.java)
- 2026-11-02: Moved from patcherMod's [perf] on 2026-11-02 with keys, comments and defaults unchanged; the table keeps patcherMod's comment (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/CWGConfig.java)
- 2026-11-02: The mixins moved from patcherMod on 2026-11-02 unchanged apart from their package (SRG names, remap = false, creativeengineering$ member prefixes) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/WorldgenMixinPlugin.java)
- 2026-11-02: Creative Worldgen (feature_writeup/creative_worldgen.md): the pack's world generation and fauna, carved out of patcherMod on 2026-11-02 (creative_split.md 7.6, wave 5) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/CreativeWorldgen.java)
- 2026-11-02: a stone base counts too: its size and its overhang (02_new_mods_tests.md 4.2: the drum tower's 49x45 andesite plate was "small, 10 earth" and could hang over a cliff unchecked) and a sandstone or terracotta floor in the desert and badlands (2026-11-02, earth_base_scan.py) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/EarthFill.java)
- 2026-11-02: Desert and badlands structures on sandstone or terracotta floors: those blocks (block tag creativeworldgen:earth_fill/sand_base) count as earth, so a house floor over a river or a cliff is seen (2026-11-02, tools/earth_base_scan.py, bottom 3 layers): T&T badlands village 0 % earth (red sandstone 56 %, terracotta 14 %), wooded badlands village 0 % (terracotta 37 %), badlands outpost 0 %, Philip's badlands dungeon 0 %, vanilla desert village 24 % (sandstone 54 %), fortified desert village 43 % (37 %), T&T desert outpost 49 %, desert village temple 22 %, Philip's desert ruins 42 % (kubejs/data/creativeworldgen/kubejs/worldgen/earth_fill.js)
- 2026-11-02: Height of a free-standing AE2 meteorite (plan_worldgen_edit C6, owner 2026-11-02: free-standing meteorites only; the meteorite metas keep surface or anchor placement) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/MeteoriteHeights.java)
- 2026-11-06: The creativedelight:biome_tag loot condition moved to Creative Delight (loot/BiomeTagCondition) 2026-11-06 (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/CreativeWorldgen.java)
- 2026-11-06: World generation mods: required (owner, 2026-11-06), so a world is never created without the biomes and structures the layout, structure and spawn config name. ids from each jar's mods.toml (development/creativeWorldgen/src/main/resources/META-INF/mods.toml)
- 2026-11-06: Moved from the pack's kubejs/data/creativeengineering on 2026-11-06, ids under creativeworldgen: since 2026-11-10: upgrade_aquatic (driftwood on land, the flotilla driftwood meta part), farmersdelight and fungidelight (wild mushroom colonies and patches), miners_delight (cave carrots), vintagedelight (wild ghost peppers) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/compat/ModDataPacks.java)
- 2026-11-06: Only paths no other jar ships, so the pack folder copy (dev) and the jar copy share one resource id and load once: data/moremooblooms/moobloom_spawns/  the pack's moobloom spawn rules (More Mooblooms, a Worldgen member, combines every file of that folder; creativeengineering.json is ours) The pack's creativeengineering worldgen data is in src/main/resources (data/, and packs/<modid>/ for files naming a mod this jar does not require, compat/ModDataPacks; moved 2026-11-06) (development/creativeWorldgen/build.gradle)
- 2026-11-06: Geophilic Reforged (owner 2026-11-06: required with the other world generation mods) (development/creativeWorldgen/src/main/resources/META-INF/mods.toml)
- 2026-11-10: a piece saved before the 2026-11-10 namespace move (LegacyIds) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/MetaFeaturePiece.java)
- 2026-11-10: saved before the 2026-11-10 move (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/MetaStructure.java)
- 2026-11-10: D-DIM (namespace_move.md section 4): the pack's own dimensions moved from creativeengineering: to creativeworldgen: on 2026-11-10 (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/DimensionMove.java)
- 2026-11-10: Load-only aliases for the ids this jar had under {@code creativeengineering:} until 2026-11-10 (namespace_move.md step 6, D-DIM) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/LegacyIds.java)
- 2026-11-10: Ids: the biome modifier codecs, the meta structure and piece types, the data (structures, dimensions, noise settings, placed features, biome modifiers) and the tags are under this jar's own namespace ({@link #NS}) since 2026-11-10 (namespace_move.md step 6); they were {@code creativeengineering:} until then (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/CreativeWorldgen.java)
- 2026-11-10: Structure ids moved to creativeworldgen: on 2026-11-10 (worldgen/LegacyIds) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/mixin/ChunkSerializerLegacyStructuresMixin.java)
- 2026-11-10: The namespace these ids had until 2026-11-10 (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/worldgen/LegacyIds.java)
- 2026-11-10: The namespace of every id this mod registers or keys (its own mod id since 2026-11-10) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/CreativeWorldgen.java)
- 2026-11-15: Since 2026-11-15 also Upgrade Aquatic's own overridden ids (upgrade_aquatic:river_tree configured feature, trees_river and driftwood_* placed features), moved from kubejs/data/upgrade_aquatic/worldgen so a pack without Upgrade Aquatic boots (plan_post_scratch_1115.md D5) (development/creativeWorldgen/src/main/java/com/SaintAlfonzo/creativeworldgen/compat/ModDataPacks.java)
- 2026-11-17: Creative Loot: the loot engine (global.LOOT, split out of this jar 2026-11-17, plan_rules_fixes.md section 5b) (development/creativeWorldgen/src/main/resources/META-INF/mods.toml)
- 2026-11-17: - the Worldgen server script tree, authored in the pack folder so /reload picks it up without a rebuild, copied into the jar at the same resource path: kubejs/data/creativeworldgen/kubejs/    -> data/creativeworldgen/kubejs/    (server scripts; the loot framework is Creative Loot's since 2026-11-17, development/creativeLoot) No client tree: no client script is Worldgen's (development/creativeWorldgen/build.gradle)
- 2026-11-17: The loot framework moved to Creative Loot (kubejs/data/creativeloot/kubejs/) on 2026-11-17 (kubejs/data/creativeworldgen/kubejs/_worldgen_core.js)
