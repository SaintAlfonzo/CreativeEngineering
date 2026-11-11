# Hearth crow: spawn crash fix and the swap from Alex's Mobs' crow (2026-11-10)

Status: **BUILT 2026-11-10, not installed (the game was running), not run.** Creative Delight and Creative Common jars built; scratch rows SQ-HCS-01 to -12, CQ-HCS-01, rewritten SQ-HH crow rows. Deviations in section 10. Was: PLANNED. One build (owner 2026-11-10): crash fix, H&H crow port, AM crow drop, nest-driven
spawning and the new nest placements land together. Supersedes the "mechanics on Alex's Mobs' crow" route (D2) in
[plan_hearth_crow_salt.md](plan_hearth_crow_salt.md) and [hearth_and_harvest.md](hearth_and_harvest.md) 3.2 / 5.4, and
settles the crow evaluation of N21 / N26-2 in favour of the Hearth and Harvest crow. TODO rows: `creativedelight/members #54`
(crash), `blocks/new #19` (crow mechanics), `largecontent/mod-additions #10` (H&H port), `#60` (nests), `mobs/tweaks #23`
(crows and nests in villages).

## 0. Owner decisions 2026-11-10

| Id | Decision |
|---|---|
| D-1 | Fix the crow spawn crash that stopped scratch run r1110. |
| D-2 | Drop Alex's Mobs' crow; the Hearth and Harvest crow replaces it completely. |
| D-3 | AM crows already in saves are left as they are (was O1). No conversion. |
| D-4 | Crows spawn near nests (replaces the open rule, section 2). |
| D-5 | Nests generate on tree tops in temperate forests, and frequently on top of village houses. |
| D-6 | Nests can generate holding stolen items. |
| D-7 | One build; no separate hotfix (was O5). 1.2.02 worlds keep the crash until this ships. |

## 1. The bug

`CrowEvents.onCheckSpawn` (backportedHearth, `crow/CrowEvents.java:191`) runs on every `LivingSpawnEvent.CheckSpawn` for
`alexsmobs:crow` and reads a 17 x 17 x 13 box (`nearGeneratedNest`) and a 17 x 17 x 5 box (`cropsNearby`) around the spawn.

- **Crash.** Chunk-generation spawns pass a `WorldGenRegion` holding only the new chunk and its neighbours. A crow near the
  region edge reads past it: `We are asking a region for a chunk out of bound`, "Exception generating new chunk"
  (`perf/runs/r1110/crow_crash/crash-2026-11-10_01.40.46-server.txt`). Every normal world dies or hangs in spawn prep.
- **Load risk.** On a live `ServerLevel` the same reads load unloaded chunks synchronously.
- **Cost.** Up to 5,202 block reads per spawn attempt.
- **Shipped in 1.2.02** (changelog lines 33 to 36). Upstream H&H `CrowSpawnRules` has the same scan.

**Fix: removed by design.** Under D-4 nests spawn crows (section 2.2), so no spawn check scans an area any more. The listener,
both scans and upstream's `CrowSpawnRules` scan are not carried over. The one remaining worldgen read pass (village roofs,
5.2) touches only blocks inside the chunk being decorated, the same bound vanilla's own structure placement uses.

## 2. Spawning

### 2.1 Breakdown of today's open rule (AM crow)

Three layers decide whether an AM crow appears:

1. **Biome list.** AM adds `alexsmobs:crow` to the overworld biomes in `config/alexsmobs/crow_spawns.json` (savanna and
   others excluded), weight `crowSpawnWeight = 10` in `config/alexsmobs.toml`, `crowSpawnRolls = 0` (no extra 1 in N
   roll). The game makes spawn attempts from this list, in the creature category, at chunk generation and while players
   are around.
2. **Pack condition `flying_birds`** (`mob_spawns.json`, Creative Worldgen `SpawnConditions`, `light_only`). The crow's
   placement test becomes "light above 8" on any block, with no ground block needed.
3. **Hearth check** (`onCheckSpawn`). If the attempt lands within `spawnRadius` 8 of a generated nest or 8 crops, it is
   allowed outright (layers 2 and AM's own check skipped). Otherwise, with `denySpawnsElsewhere` false (the default), the
   attempt is left to layers 1 and 2.

What that adds up to: crows spawn wherever layer 1 lists them and the light is good. Nests and crops only rescue attempts
that would otherwise fail; they never add attempts. A filter can only remove crows: with `denySpawnsElsewhere` on, crows
would be limited to the area near nests and crops but become rarer overall, not more common at nests. Layer 3 is also the
crash. So "crows spawn near nests" cannot be built as a filter. It has to be inverted, with the nests producing the crows.

### 2.2 New rule: nests produce crows

- **No biome spawn entries.** `hearthandharvest:crow` gets none, so the game never rolls crows from the biome list.
  Remove `alexsmobs:crow` from every biome (D-2). Upstream's `biome_modifier/crow.json` is not ported.
- **Flock at generation.** When a nest generates (tree top or roof), 2 to 3 crows are added at it through the
  `WorldGenRegion` (`addFreshEntity`, `finalizeSpawn` as `CHUNK_GENERATION`). New chunks get their crows with their nests.
- **Upkeep.** A generated nest takes random ticks (`isRandomlyTicking` only when `GENERATED`); random ticks only happen
  in chunks near players. On a tick, if a player is within 48 blocks, fewer than 3 crows are within 24 blocks, and the
  nest's cooldown (5 minutes of game time, kept in the block entity) has passed, it spawns 1 crow at a free spot within
  6 blocks with light above 8. This refills killed or tamed crows without crows piling up.
- **Player-placed nests** (`GENERATED` false) do not spawn crows. This stops crow farms. Tame crows still breed on corn.
- **Existing worlds.** Ground nests already generated in 1.2.02 worlds are `GENERATED`, so they start producing H&H crows
  near players. AM crows already there stay (D-3).
- **Config** (`hearthandharvest-common.toml [crow]`): `nestFlockSize` 2 to 3, `nestCrowCap` 3, `nestCrowRange` 24,
  `nestRespawnMinutes` 5, `nestPlayerRange` 48, `playerNestsSpawnCrows` false. Removed: `spawnNearNests`, `spawnRadius`,
  `cropRequirement`, `denySpawnsElsewhere`.
- **Crops** no longer affect spawning. They still draw crows through behaviour (`CrowEatCropsGoal`), and village roof nests
  put crows beside village farms.

## 3. O3 broken down: animations (resolved, no call needed)

Upstream animates the crow with five keyframe animations (`CrowAnimations`: walking, idle, flying, gliding, sitting).
Four are driven by `AnimationState`s on the entity and one by `HierarchicalModel.animateWalk`.

- What 1.19.2 has: the whole keyframe system came in 1.19 for the Warden: `AnimationDefinition`, `AnimationChannel`,
  `Keyframe`, `KeyframeAnimations`, `AnimationState`, `HierarchicalModel.animate`. The Baby Mobs port
  (`creativeWorldgen/members/babymobs`, `PetAnimations`) already uses them on 1.19.2.
- What is missing: `animateWalk` (added in 1.19.4). It turns limb swing into an animation time. About 10 lines, and the same
  thing exists already as Baby Mobs' `MiscBabies.animate`.
- So `CrowAnimations` and `CrowModel` port almost verbatim, and the "own keyframe player versus hand-written" choice in the
  first draft does not exist. The other model classes (`HierarchicalModel`, `LayerDefinition`) are also in 1.19.2.

## 4. O4 broken down: chunk-generation crows (resolved by 2.2)

The question was what to do with crows the game spawns into a chunk once, as it generates (`spawnOriginalMobs`, the source
of the crash):

- (a) a bounded nest or crop scan, limited to the chunks the region holds;
- (b) biome weight only, with no scan at generation.

Both assumed crows come from the biome list. Under 2.2 they do not, so neither applies. The game rolls no crows at
generation, the nest that generates brings its own flock, and no scan runs. The crash path no longer exists rather than
being guarded.

## 5. Nests

### 5.1 Tree tops in temperate forests

- New biome tag `creativedelight:crow_nest_forests` through `global.tags` in `crow_salt_tags.js`. These are the temperate
  forests (not in any non-temperate list in `season_types.json`): `minecraft:forest`, `birch_forest`,
  `old_growth_birch_forest`, `dark_forest`, `flower_forest`, `terralith:lavender_forest`, `moonlight_grove`, `sakura_grove`.
- `NestFeature` gets a `tree_top` mode: heightmap `MOTION_BLOCKING` (stops on leaves); the block below must be in
  `#minecraft:leaves`, open sky above. It runs at the `top_layer_modification` step, after trees. Starting rate: 1 nest in 6
  chunks (tunable in `placed_feature/nest_tree_top.json`).
- If the leaves under a nest decay, the nest breaks and drops its items, as it does today for any support removed.

### 5.2 Village house roofs ("frequently")

Structure processors cannot add a block above a template, and `PoolTuner` only reweights pieces, so roofs get their own
pass in the hearth member:

- A mixin at the tail of `ChunkGenerator.applyBiomeDecoration(WorldGenLevel, ChunkAccess, StructureManager)` reads the
  village starts of this chunk through `structureManager.startsForStructure`, the same call vanilla uses one line earlier.
  Villages are `#minecraft:village`.
- Each `PoolElementStructurePiece` whose template id has `/houses/` in its path counts as a house; that covers vanilla and
  mod houses in the house pools. The template id is read through an accessor on `SinglePoolElement`.
- Per house, one roll at a random generator seeded from the piece's box: chance 25 percent ("frequently", owner 2026-11-10), one column
  inside the box chosen. The column is worked only if it lies in the current chunk, so a house over several chunks still
  gets at most one nest.
- In that column: the top non-air block within the box (and at most 3 below its top). That block must be a full block
  (`isShapeFullBlock`), with open sky above it. The nest goes on top, `GENERATED`, with loot (5.3) and a flock (2.2).
  Every read and write is inside the current chunk.
- Off switch `crow.villageRoofNests` (true), chance `crow.villageRoofNestChance` (0.25).

### 5.3 Stolen items

- `NestBlockEntity` gets a loot table id and seed, unpacked into its 4 slots the first time it is opened, broken or emptied
  by hand (the `RandomizableContainerBlockEntity` pattern; the nest has no menu, so done by hand).
- `hearthandharvest:chests/crow_nest` for tree nests: 1 to 2 rolls. Mostly iron, gold and copper nuggets and the
  shiny-items tag (`#creativedelight:crow_shiny_items`). Rare: emerald, gold ingot. Very rare: diamond. Some empty rolls,
  so many nests hold nothing.
- `hearthandharvest:chests/crow_nest_village` for roof nests: the same table with more emeralds, plus the small things
  villagers own (wheat seeds, bread, a book, a lead).
- Values go in through the pack's loot route (memory `loot-system`); the shiny tag is not expanded unless asked (TODO #60
  mentions it).
- Ground nests (the current `nest.json` biome modifier, 1 in 32 chunks in `#has_crows`) are dropped: tree tops and roofs
  replace them. Already generated ground nests stay.

## 6. Build steps (one build)

### 6.1 Port the H&H crow (CD member `hearth`)

Donor: `DecompiledMods/hearthandharvest-1.21.1-1.4.1-src/alabaster/hearthandharvest/common/entity/crow/` (MIT; the 1.20.1
jar in `downloads/` has no crow). Skill `port`. Ids: `hearthandharvest:crow`, `hearthandharvest:crow_spawn_egg`.

| Step | Work | Notes |
|---|---|---|
| P1 | `CrowEntity` (`ShoulderRidingEntity`, `FlyingAnimal`), `CrowMoveControl`, `CrowFlightPose`, attributes, category creature | 1.21.1 -> 1.19.2: `DeferredHolder` -> `RegistryObject`, data components -> NBT; `SpawnPlacements.register` in common setup with a plain light rule (eggs and commands only, no natural entries) |
| P2 | Goals: keep the member's tuned ports (`CrowWaryTemptGoal`, `CrowFleeGoal`, `CrowAvoidRepellingBlocksGoal`, `CrowEatCropsGoal`, `CrowEatDroppedFoodGoal`, `CrowSeekShinyGoal`, J60 to J67), retarget them from `TamableAnimal` + `CrowAccess` to `CrowEntity`; port `CrowWanderGoal`, `CrowItemGoal`, `CrowRetrieveItemsGoal` | Pack values win over upstream's where they differ |
| P3 | `CrowWariness` capability -> fields on `CrowEntity`, same NBT key names | Capability registration and attach listener removed |
| P4 | Client: `CrowModel`, `CrowAnimations` (verbatim, section 3), walk helper, `CrowRenderer`, `CrowHeldItemLayer`, `CrowOnShoulderLayer`, `CrowShoulderEvents` | |
| P5 | Sounds (4 events, ogg files, `sounds.json`), texture, spawn egg, lang, loot table `entities/crow.json` (Crow Feather) | |
| P6 | Nest stash on the new crow (`CrowStashEvents`, `stashInNests`) | |
| P7 | Not ported: 7 crow advancements and the crow triggers in `HHModTriggers` | CLAUDE.md rule |

### 6.2 Spawning and nests (CD member `hearth`)

| Step | Work |
|---|---|
| S1 | Delete `onCheckSpawn` and both scans; config keys per 2.2 |
| S2 | `NestBlock`: random ticks when `GENERATED`; `NestBlockEntity`: cooldown, loot table and seed; flock helper used by S3, S4 and the tick |
| S3 | `NestFeature` `tree_top` mode, `placed_feature/nest_tree_top.json`, biome modifier on `#creativedelight:crow_nest_forests`; delete ground `nest.json` modifier |
| S4 | Village roof pass: `ChunkGeneratorCrowNestMixin`, `SinglePoolElement` accessor, both in `hearthandharvest.ce.mixins.json` |
| S5 | Loot tables `chests/crow_nest`, `chests/crow_nest_village` |
| S6 | Lang rename to Crow Nest; straw recipes (craft 2 straw, mill 3) in CD `hearthandharvest/recipes.js` via `global.NCH`; changelog bullet "Crow Nest gives 2 Straw crafted, 3 milled" |

### 6.3 Drop the AM crow

| Step | Work | Files |
|---|---|---|
| C1 | `mob_spawns.json`: rule `remove` for `alexsmobs:crow`; drop it from `flying_birds`. AM `crowSpawnWeight = 0` as a second guard (copy to `configs_edited/root_config/`) | `kubejs/generation_scripts/worldgen/mob_spawns.json`, `config/alexsmobs.toml` |
| C2 | `alexsmobs:spawn_egg_crow` into `removed_items.js` | Creative Common `_lib/removed_items.js` |
| C3 | Delete `EntityCrowFoodMixin` (and its mixin json entry), `CrowAccess`, the `crow_feather.json` loot modifier on `alexsmobs:entities/crow` and its `global_loot_modifiers.json` line. AM is no longer needed by the member: drop it from the member's `build.gradle`, check the CD `mods.toml` entry | `hearth/` member |
| C4 | Tags: `does_not_poop` swaps `alexsmobs:crow` for `hearthandharvest:crow`; drop the `alexsmobs:crow_fears` line; add `crow_nest_forests`; `has_crows` kept only if still read, else removed | `tags.js`, `crow_salt_tags.js` |
| C5 | AM crows in saves: nothing (D-3). Their old capability data is dropped by Forge on load | |
| C6 | `item_dict.js` key for the spawn egg | Common |

### 6.4 Build, data, docs

- `./gradlew :creativeDelight:build :creativeCommon:build --offline`, then both `installToMods` with the game closed; restart.
- `/dumpall`, `/cdevgen`; `python3 tools/gen_spawn_table.py`; recopy `hearthandharvest-common.toml` to `configs_edited/`.
- Docs: plan_hearth_crow_salt.md (status; D2 reversed; section 5 superseded), hearth_and_harvest.md (rows 142, 150, 199,
  370, 593, J13, J61, J64, J65; new 4.x section), [mob_spawns.md](mob_spawns.md) (flying_birds row), TODO rows at the top
  (`#54` and `#60` to Testing, `mobs/tweaks #23` covered by 5.2).

## 7. Numbers (owner 2026-11-10: roof chance 25 percent, straw recipes in, defaults for the rest)

| Id | Question | Default |
|---|---|---|
| N1 | Numbers: flock 2 to 3, cap 3 within 24, refill every 5 min, player within 48 | as listed |
| N2 | Tree nest rate 1 in 6 chunks of temperate forest; roof chance 25 percent per house | as listed |
| N3 | Loot: two tables as 5.3, about half the nests empty | as listed |
| N4 | TODO #60 rest: rename "Nest" to "Crow Nest" (lang) and the straw recipes in this build: crafting a nest gives 2 `i.straw`, milling it gives 3 (recipes through `global.NCH`, one-way, no nest from straw) | both in (owner 2026-11-10) |

## 8. Scratch queue rows (add when built)

| ID | Group | Check |
|---|---|---|
| SQ-HCS-01 | B | Fresh normal world: reaches `Done`; no `out of bound`, no `Exception generating new chunk`; no line from `CrowEvents` |
| SQ-HCS-02 | B | `/tp` through 40 new chunks of forest and plains: no crash, no tick over 200 ms naming a crow or nest class |
| SQ-HCS-03 | B | Chunky 30 x 30 chunks of `minecraft:forest`: nests only on leaves with sky above, count near 1 in 6 chunks; none on the ground |
| SQ-HCS-04 | B | `/place structure minecraft:village_plains` x 5: about a quarter of houses have one roof nest on a full block, never two per house, none in a street |
| SQ-HCS-05 | A | A generated nest has 2 to 3 crows at it on generation; kill them; within about 5 min of game time with a player near, crows return, never above 3 within 24 |
| SQ-HCS-06 | A | A player-placed nest never spawns crows; `/summon hearthandharvest:crow` and the spawn egg work |
| SQ-HCS-07 | A | Generated nests: first open rolls loot from the right table; reopen gives the same items; breaking drops them |
| SQ-HCS-08 | B | 20 minutes in crow biomes: no `alexsmobs:crow` spawns; `/summon alexsmobs:crow` still works |
| SQ-HCS-09 | A | 1.2.02 save with a tamed and a carrying AM crow and a ground nest: boots, both crows kept, the ground nest starts producing H&H crows |
| SQ-HCS-10 | A | H&H crow mechanics: the rewritten SQ-HH-401 to -502 (tame, shoulder, fetch, theft, wariness, scarecrow, pecking, feather) |
| CQ-HCS-01 | client | Crow renders and animates walking, idle, flying, gliding, sitting, on the shoulder; held item shows |

Also: rewrite SQ-HH-401 to -502, CQ-HH-401 to -405, `tools/scratch_query/hh_crow_probe.js`, SQ-HH-1127, -1304 for
`hearthandharvest:crow`; mark SQ-HSC-87 to -91 superseded; unblock SQ-WO-35, -36, SQ-CDNS-10, -11 in the r1110 resume point.

## 9. Changelog (family "Crows"; never edit 1.2.02.md)

- **New:** Hearth and Harvest Crow, replacing Alex's Mobs' Crow.
- **New:** Crows spawn from nests: a nest generates with a small flock and refills it while players are near.
- **New:** Nests generate on tree tops in temperate forests and on village house roofs, some holding stolen items.
- **Changed:** Nests no longer generate on the ground [ground in grassy biomes].
- **Removed:** Alex's Mobs Crows no longer spawn; their spawn egg is removed.
- **New:** Crow Nest can be crafted into 2 Straw or milled into 3 Straw.
- **Fixed:** a crash when new chunks generated near crows.
- Run `python3 tools/changelog_lint.py`.

## 10. As built (2026-11-10)

Built as sections 1 to 6 describe, with these differences:

- **Ground mode removed rather than kept beside `tree_top`.** `NestFeature` is now the tree-top feature only (same feature id
  `hearthandharvest:nest`); `placed_feature/nest.json` and the ground biome modifier are deleted, `nest_tree_top` added.
- **Flock size is two keys,** `nestFlockMin` 2 and `nestFlockMax` 3 (section 2.2 named one `nestFlockSize`).
- **Roof column:** chosen in the middle third of the house box (the ridge of most roofs), not anywhere in the box, so more
  rolls meet a roof; the depth limit stays 3. SQ-HCS-04 checks the rate against 25 percent.
- **Template id read through `SinglePoolElement.toString()`** ("Single[Left[id]]") instead of an accessor: accessors on
  vanilla SRG names fail at compile in this build (memory `srg-mixin-compile-gotchas`).
- **Straw recipes** sit beside the nest recipe in `crow_salt_recipes.js`, not `recipes.js`.
- **Kept the member's own `CrowWariness`, `CrowFleeGoal` and logic** (upstream's `CrowWariness`, `CrowFleeEntityGoal` and
  the threat methods on its entity are not ported; ours were the tuned versions). Wariness ticks in `CrowEvents`, not `aiStep`.
- **Upstream's hand-feeding branch in `mobInteract` dropped**: the member's event does it (with `handFeeding`); the entity
  keeps the sit toggle and the cookie poisoning.
- **Wheat seeds** are now eaten from the ground like other crow food (the exclusion was for AM's own seed taming).
- **Fetch goal:** 1.19.2 items keep no owner entity, so loot a crow let go of is marked `hearthandharvest:crow_dropped`
  in its persistent data and tame crows skip it; thrown items are matched by thrower UUID.
- **Shoulder hop-off** (`crow/CrowShoulder`) calls the protected 1.19.2 shoulder setters by reflection (`m_36362_`, `m_36364_`).
- **Not ported:** `CrowStashEvents` (advancement trigger only) and the advancement lines in `CrowShoulderEvents`.
- SQ-HSC-89 (the salt cave) stays; only -87, -88, -90, -91 are superseded.

