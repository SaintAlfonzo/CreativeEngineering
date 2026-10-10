# Mob spawns by biome

A dump command plus a generator that list which mobs spawn in which biomes, how often, and under which
conditions, as the running game actually has them, after every mod's biome modifiers. On top of it, a tuning
file that changes spawn weights per biome, adds mobs to biomes and removes them.

## 0. At a glance

| Part | Status |
|---|---|
| `/dumpspawns` (KubeJS `kubejs/server_scripts/_lib/dump_spawns.js` -> patcherMod `worldgen/SpawnDump`) | BUILT 2026-10-09, in `/dumpall`; first real dump 2026-10-09 21:58 (`kubejs/exported/spawns.json`) |
| `tools/gen_spawn_table.py` -> [spawns_by_biome.md](spawns_by_biome.md) | BUILT; report generated from the real dump 2026-10-09 |
| Spawn tuning: `mob_spawns.json` -> `worldgen/mob_spawns.js` -> biome modifier `creativeengineering:tune_spawns` (patcherMod `worldgen/SpawnTuning`) | BUILT, offline tests pass; 12 rules for the first block of section 9 (2026-10-09), not run in game |
| Spawn conditions (`light_only`, `water_only`; patcherMod `worldgen/SpawnConditions` + `SpawnPlacementsConditionsMixin`) and spawn ground (every dirt in the animal ground tags + `AnimalWalkTargetMixin`, `MushroomCowWalkTargetMixin`) | BUILT 2026-10-09, offline tests pass, jar in `development/patcherMod/build/libs` (not in `mods/`) |
| Shiba off (Quark `Shiba = false`, plan 9.4) | DONE 2026-10-09, `config/` + `configs_edited/` |
| CEConfig `[worldgen.systems] mobSpawns` | BUILT, same jar |
| Cow table in [mooblooms.md](mooblooms.md) (first consumer) | PLANNED |
| Passive animal analysis (farm animals, duck/goose, Alex's Mobs surface animals) | DONE 2026-10-09, section 10; baseline for the cohesion rules |

- **Install first:** `./gradlew :patcherMod:installToMods` with the game closed. Without the new jar `/dumpspawns`
  says so, and `mob_spawns.js` writes nothing (an unknown modifier type would stop the world loading).
- **Measure:** `/dumpspawns` (or `/dumpall`), then `python3 tools/gen_spawn_table.py` (or `/cegen run spawns_by_biome`).
- **Knobs:** `kubejs/generation_scripts/worldgen/mob_spawns.json` (rules, section 3); CEConfig
  `[worldgen.systems] mobSpawns` (off = every mob as its mod ships it). Rejoin to apply.
- **Preview without the game:** edit the rules, run the generator: it applies them to the last dump's untuned spawners.
- **Offline checks:** `bash tools/test_mob_spawns.sh` (section 6).
- **Tests:** MS-01 to MS-12 (section 7).

## 1. Overview

Spawn data is spread over many places: vanilla biomes are built in code, Terralith and other mods ship biome JSON,
and Forge biome modifiers (`forge:add_spawns`, `forge:remove_spawns`, plus mod-specific modifier types) change
them at load. Reading the jars can't show the final result. The dump reads the live biome registry after all of
that has run, the same way `/dumpbiomes` does.

The tuning runs after all of those modifiers too, as one pack biome modifier with an ordered list of rules. Biome
modifiers are applied at every server start, so unlike worldgen tuning, a change reaches existing worlds (for
mobs that spawn from then on).

The first use of the dump is the More Mooblooms spawn design ([mooblooms.md](mooblooms.md) 4.9): which biomes cows
spawn in and how often. The tuning is for the plans in section 9.

## 2. Reference

### 2.1 How a mob ends up spawning (1.19.2)

| Step | What happens |
|---|---|
| Cycle | Every tick, per loaded chunk near a player, per category. Persistent categories (`creature`) only every 400 ticks |
| Mob cap | Category cap x loaded chunks / 289. Full cap = no attempt for that category |
| Position | A random column in the chunk, random height up to the surface; up to 3 groups of up to 4 tries jittered around it |
| Pick | The biome at that position; one entry from that biome's list for the category, **by weight** (structure spawn overrides and Forge's `PotentialSpawns` event can replace the list there) |
| Group | `min`-`max` of that mob around the position |
| Checks | no player within 24 blocks; placement type (on ground, in water, in lava, none); the mob's **spawn rule** (light, ground block, height...; section 6 of the report); the mob's own `checkSpawnRules` / `checkSpawnObstruction`; Forge's `CheckSpawn` event |
| New chunks | Chunk generation also places creature herds: while a roll is under the biome's `creature_probability` (vanilla 0.1, 0.07 in some), one group from the creature list |

| Category | Cap | Notes |
|---|---|---|
| `monster` | 70 | |
| `creature` | 10 | persistent: spawns every 400 ticks, doesn't despawn |
| `ambient` | 15 | bats |
| `axolotls` | 5 | |
| `underground_water_creature` | 5 | glow squid |
| `water_creature` | 5 | squid, dolphins |
| `water_ambient` | 20 | fish |
| `misc` | none | never spawns naturally |

So the **weight** only decides which mob a successful attempt in that biome picks, among its category. The report's
"share" is that chance. Whether the picked mob appears depends on the rule: an animal on sand in a desert fails
`Animal.checkAnimalSpawnRules` (needs `#animals_spawnable_on`), so a desert cow entry mostly wastes attempts.

### 2.2 What the dump records

Output: `kubejs/exported/spawns.json`. Per biome (every biome in the registry):

| Field | Source |
|---|---|
| `dimensions` | the biome sources that can place it |
| `spawners` per category: entity, weight, min/max group | `biome.getMobSettings().getMobs(category)` (Forge returns the modified settings) |
| `creature_probability` | chunk-generation herds |
| `costs` | spawn costs (charge, energy budget), the soul sand valley style density limit |
| `tags` | every biome tag it is in (the generator resolves `#tag` selectors with them) |

Per entity type (every mob with a spawner, a spawn placement or a spawn egg):

| Field | Source |
|---|---|
| `category`, `spawns_in_biomes`, `spawner_categories`, `spawn_egg` | the type, the biome lists, the spawn egg items |
| `placement`, `heightmap` | `SpawnPlacements` |
| `predicate_source` | the class that registered the spawn rule (the part of the lambda class name before `$$Lambda`); `SpawnPlacementRegisterEvent$MergedSpawnPredicate` when a mod changed it through Forge's event |
| `class`, `superclasses`, `spawn_rules_override`, `obstruction_override` | from an instance created in the overworld and never added (the type doesn't keep its class) |
| `rule_methods` | static methods with the spawn rule shape on the mob's own (mod) classes |

Plus `categories` (caps, persistence, despawn distances) and `tuning`: the rule ids that ran, how many biomes each
matched, and for every biome a rule changed, its spawners before the first rule and one line per change.

A spawn rule is code, so a dump can't read what it checks. Vanilla's rules are named from the bytecode instead:
`tools/spawn_rules_vanilla.json` (72 mobs, from `SpawnPlacements.<clinit>` in the ForgeGradle mapped jar,
`--refresh-vanilla-rules`), and the generator explains the common ones (`Animal.checkAnimalSpawnRules` = grass-type
block below + light above 8, `Monster.checkMonsterSpawnRules` = dark + not Peaceful, and so on).

### 2.3 Examples (vanilla data)

- Plains creatures: sheep 12, pig 10, chicken 10, cow 8, horse 5, donkey 1 (shares 26%, 22%, 22%, 17%, 11%, 2%).
- Mushroom fields: mooshroom only (and its rule needs mycelium).
- Sunflower plains: Buzzier Bees' `remove_animal/cow` takes the cow out; More Mooblooms' pack set switches that
  removal off ([mooblooms.md](mooblooms.md) 4.11), so the cow is back.

### 2.4 The generated report

`tools/gen_spawn_table.py` reads `spawns.json`, `mob_spawns.json`, the biome traits in `wild_crops.json` and the
post-tuning biome areas in `biome_config.json`, and writes [spawns_by_biome.md](spawns_by_biome.md):

1. **Pack tuning:** every rule, the biomes it matched and changed, warnings, the changes per biome.
2. **Mobs by category:** each mob's biome count and **world share** per region (overworld surface, caves, Nether,
   End): its share weighted by biome area, so "how common is this mob across the world" reads off the top.
3. **Creatures by biome type:** each creature's mean share in plains, forest, swamp, jungle, savanna, desert,
   badlands, mountain, peak, snowy, beach, river, sky, flower and shrubland biomes (the biome types section 9 names).
4. **Per mob:** every biome, weight ("(was N)" when the tuning changed it), share, group, area.
5. **Per biome:** every spawner by category with share, herd chance, costs, traits.
6. **Spawn conditions:** placement and rule per mob (vanilla named, mods by registering class and rule methods).
7. **Gaps:** spawn-egg mobs with no biome spawner (with their other source where known), overworld surface biomes
   with no creatures, overworld biomes with no traits.

`--mob <id>` prints one mob's table for pasting into other writeups (`--mob minecraft:cow` for mooblooms.md).
The tables always show `mob_spawns.json` as it is now. When the dump ran different rules (the file was edited
since the last restart), a WARNING says so.

## 3. What the pack changes: spawn tuning

`kubejs/generation_scripts/worldgen/mob_spawns.json`, a list of rules (its `_readme` has the same reference):

```json
{ "rules": [
  { "id": "pig_forests", "mob": "minecraft:pig", "action": "scale", "factor": 2, "where": ["forest", "swamp"],
    "except": ["minecraft:dark_forest"], "notes": "why, for the reader" },
  { "id": "lone_pillager", "mob": "minecraft:pillager", "action": "set", "weight": 2, "min": 1, "max": 1,
    "where": ["dark_oak", "swamp", "badlands"], "add": ["minecraft:ice_spikes"] }
] }
```

(Format examples from the test fixture; the pack's file has no rules yet.)

| Field | Meaning |
|---|---|
| `id` | unique, lowercase `a-z 0-9 _`; names the biome tag `creativeengineering:mob_spawns/<id>` and the log lines |
| `mob` | entity id; a mod that isn't loaded skips the rule with a warning |
| `action` | `set`: spawn at `weight` (and `min`/`max` when given). Where the mob already spawns, its entries become one, in place, keeping its group unless given; where it doesn't, it is added to `category` (default the mob's own), group 1-1 unless given. Weight 0 = remove. `scale`: every existing weight x `factor`, rounded half up, at least 1; 0 = remove; never adds. `remove`: every entry, every category |
| `where` / `add` / `except` | as `wild_crops.json`: `where` clauses OR'ed, traits in a clause AND'ed, `!trait` negated, traits from the `biomes` table in `wild_crops.json` (overworld only). `add`: biome ids or `#tags` (Nether, End). `except`: ids; it can't take members out of an added `#tag`. Unlike crops, ocean and cave biomes match |
| `category`, `min`, `max` | for `set` |
| `disable_modifiers` | a mod's own biome modifiers this rule replaces, each written as `forge:none` |
| `label`, `notes` | shown in the report |

**How it is applied:** `kubejs/server_scripts/worldgen/mob_spawns.js` (pattern of `wild_crops.js`):
- `ServerEvents.tags('worldgen/biome')`: one biome tag per rule with the matched ids and `#tag` references
  (`global.tags`).
- `ServerEvents.highPriorityData`: ONE biome modifier, `creativeengineering:forge/biome_modifier/mob_spawns.json`,
  of type `creativeengineering:tune_spawns`, holding every usable rule in file order; plus `forge:none` over each
  `disable_modifiers` id of a loaded mod. Numbers written JS-style (no Gson `2.0`). Nothing is written when CEConfig
  `mobSpawns` is off or the jar has no `SpawnTuning` (logged as an error when rules exist).
- `ServerEvents.loaded`: checks the live tags against the rules and logs `SpawnTuning.summary()`: rules, biomes
  changed, changes, rules that matched no biome.
- patcherMod `worldgen/SpawnTuning` runs the rules per biome in Forge's `AFTER_EVERYTHING` phase, after every mod's
  add/remove modifiers, and records each changed biome for `/dumpspawns` (cleared on server stop). An unknown mob
  id logs one warning and skips the rule instead of failing the world load.

**Rules now** (the first block of section 9, 2026-10-09):

| Rule | What | Plan |
|---|---|---|
| `mooshroom_dark_forests` | mooshroom 8 (2-4) in dark forest and mirage isles | 9.1 |
| `pig_forests` | pig x2 in forest and swamp biomes | 9.3 |
| `sheep_hills` | sheep x2 in mountain biomes (peaks included) | 9.3 |
| `cow_plains` | cow x2 in plains biomes (also more mooblooms in meadow and blooming plateau) | 9.3 |
| `chicken_shores` | chicken 10 (2-4) on beaches and rivers | 9.3 |
| `chicken_sky` | chicken 20 (2-4) in every skylands biome | 9.3 |
| `rabbit_savanna` | rabbit 8 (2-3) in savannas (none before) | 9.3 |
| `rabbit_snowy` | rabbit x2 in snowy biomes that have them | 9.3 |
| `duck_wetlands`, `goose_wetlands` | duck 6 / goose 4 (2-4) in swamps and on beaches, spawning anywhere as on rivers | 9.6 |
| `duck_ponds`, `goose_ponds` | duck 4 / goose 3 (2-4) in grassy land biomes (not swamp, river, beach, sky); water only, see 3.1 | 9.6 |

Shiba (9.4) is off at the source: Quark's `Shiba` module is `false` in `config/quark-common.toml` (and `configs_edited/`).

### 3.1 Spawn conditions and spawn ground

`mob_spawns.json` also has `conditions` (spawn checks, not weights) and `spawn_ground` (ground block tags):

```json
"conditions": [
  { "id": "flying_birds", "condition": "light_only", "mobs": ["minecraft:parrot", "alexsmobs:crow"] },
  { "id": "waterfowl_ponds", "condition": "water_only", "mobs": ["untitledduckmod:duck"], "rules": ["duck_ponds"] }
],
"spawn_ground": { "minecraft:animals_spawnable_on": ["#minecraft:dirt"] }
```

| Field | Meaning |
|---|---|
| `condition` | `light_only`: the mob's spawn rule becomes "light above 8" (raw sky + block light, as vanilla animals) on any block its placement type allows; no ground block. Its own `checkSpawnRules` (Alex's Mobs' 1-in-N roll) still runs. `water_only`: only on the water surface (the top motion-blocking block of the column is water, the position at or above it); its own rule still runs too |
| `mobs` | entity ids; a mod that isn't loaded is left out |
| `where` / `add` / `except` / `rules` | where it applies, as for rules; `rules` = the biomes those rule ids match. None = everywhere. Biome tag `creativeengineering:spawn_conditions/<id>` |

Natural spawns and chunk-generation herds only; spawn eggs, spawners and commands are untouched. The conditions go into
the same `tune_spawns` modifier (field `conditions`), which hands them to `SpawnConditions`; the patcherMod mixin on
`SpawnPlacements.checkSpawnRules` asks it before the mob's registered rule. That call runs for mobs without a
registered rule too (Alex's Mobs' crow).

**Conditions now:**

| Condition | Kind | Mobs | Where | Plan |
|---|---|---|---|---|
| `flying_birds` | light_only | parrot, crow, seagull, bald eagle, sunbird, toucan, blue jay, hummingbird, potoo | everywhere | 9.5 |
| `chicken_shores` | light_only | chicken | the `chicken_shores` rule's biomes (beaches, rivers): sand and gravel work there | 9.3 |
| `waterfowl_ponds` | water_only | duck, goose | the `duck_ponds` / `goose_ponds` biomes: lakes and ponds only | 9.6 |

**Spawn ground** (plan 9.2): `#minecraft:dirt` (dirt, coarse dirt, podzol, rooted dirt, mycelium, moss, mud, muddy
mangrove roots, Immersive Weathering soils, Botania grasses, rich soil...) goes into `#minecraft:animals_spawnable_on`,
`#minecraft:mooshrooms_spawnable_on` and `#minecraft:rabbits_spawnable_on`. Land animals keep "ground + light above 8".
Vanilla checks a second thing after the rule: the mob's walk value (`Animal.getWalkTargetValue`), which hard-codes
grass_block and otherwise uses a light value that goes negative at night, so an animal on podzol would pass the rule
and then fail at night. `AnimalWalkTargetMixin` / `MushroomCowWalkTargetMixin` make it read the same tags. Side
effect: wandering animals favour every spawnable dirt the way they favour grass. With the system off the tags are
vanilla (grass_block, mycelium), so the mixins change nothing.

The block tags are only written when the jar has `SpawnTuning` (an old jar has no walk-value mixins either).

**Interplay:** More Mooblooms turns cows into mooblooms at spawn time, after the pick ([mooblooms.md](mooblooms.md)
4.9), so a cow weight change changes moobloom numbers in the same proportion. `moobloom_cows.json` (cows added to
meadow, blooming plateau, skylands spring) is a plain `forge:add_spawns` and runs before the tuning. The Wandering
Trader, patrols, phantoms and villages don't use biome lists (section 4).

## 4. Limits and decisions

- **Only biome spawners.** Structure spawn overrides (pillager outposts, swamp huts, ocean monuments, Nether
  fortresses) live on structures, not biomes; listed as a later extension. Mod-side spawners outside the biome
  list (patrols, phantoms, the Wandering Trader, MCA villagers, custom tick spawners) are not included; the report's
  gaps section names the known ones by hand (`OTHER_SOURCES` in the generator).
- **Share, not spawns per hour.** Real rates also depend on mob caps, light, ground blocks and player position.
  Category share x biome area is a fair comparison between biomes, not an absolute rate. The biome-type table counts
  biomes without sampled area (sky dimensions) at a weight of 0.01.
- **Dump in Java:** Rhino resolves `WeightedRandomList.unwrap()` to its own wrapper, so a script reads every spawn
  list as empty (found 2026-10-07). The KubeJS command is a thin wrapper.
- **One modifier, rules in order.** Forge runs modifiers of one phase in registry order, so per-rule modifiers would
  make "remove then add back" or two rules on one mob order-dependent in a way the file doesn't show.
- **Own modifier type instead of Forge's.** `forge:remove_spawns` runs after `forge:add_spawns`, so re-adding a mob
  at a new weight is undone, and neither can scale. `AFTER_EVERYTHING` also sees mod-specific modifier types.
- **Mobs created for the dump.** The class chain needs an instance; every mob type with spawn data is created in the
  overworld once per dump and discarded, never added. A constructor that throws is recorded as `class_error`.
- **Rejected:** reading jars and biome modifiers statically. Vanilla 1.19.2 biomes are not JSON, and
  modifier order and mod-specific modifier types make the result unreliable.

**Notes for the section 9 plans** (from the rules in the bytecode; the first block is applied, section 3):

| Plan | What the rule needs |
|---|---|
| Mooshrooms in dark forests | `MushroomCow.checkMushroomSpawnRules` wants `#mooshrooms_spawnable_on` (mycelium) below; on grass every pick fails unless that tag grows |
| Lone pillagers | `PatrollingMonster` rule: block light 8 or less, any sky light, so they would spawn in daylight; `set` with `min`/`max` 1 and `category` monster |
| Lone villagers | `Mob.checkMobSpawnRules` (any light, any spawnable block); MCA replaces vanilla villagers |
| Farm animal biome types | `scale` per trait clause; the report's section 3 shows the current shares by type |
| Flower biomes / mooblooms | cow weights there drive moobloom numbers (section 3 interplay) |

## 5. File map

- [../kubejs/server_scripts/_lib/dump_spawns.js](../kubejs/server_scripts/_lib/dump_spawns.js) (`/dumpspawns`)
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/SpawnConditions.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/SpawnConditions.java), mixins `SpawnPlacementsConditionsMixin`, `AnimalWalkTargetMixin`, `MushroomCowWalkTargetMixin`
- `config/quark-common.toml` (`Shiba = false`), mirrored in `configs_edited/root_config/`
- [../kubejs/server_scripts/_lib/dump_all.js](../kubejs/server_scripts/_lib/dump_all.js) (`dumpspawns` after `dumpbiomes`)
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/SpawnDump.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/SpawnDump.java)
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/SpawnTuning.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/SpawnTuning.java) (modifier type, registered in `CreativeEngineering.java`)
- `CEConfig.java` / `worldgen/WorldgenConfig.java` (`mobSpawns` switch)
- [../kubejs/generation_scripts/worldgen/mob_spawns.json](../kubejs/generation_scripts/worldgen/mob_spawns.json) (the rules)
- [../kubejs/server_scripts/worldgen/mob_spawns.js](../kubejs/server_scripts/worldgen/mob_spawns.js)
- [../tools/gen_spawn_table.py](../tools/gen_spawn_table.py), [../tools/spawn_rules_vanilla.json](../tools/spawn_rules_vanilla.json); `tools/generators.json` entry `spawns_by_biome`
- [../tools/test_mob_spawns.sh](../tools/test_mob_spawns.sh), [../tools/mob_spawns_test/](../tools/mob_spawns_test/) (parity cases, Rhino harness, rule fixtures)
- `kubejs/exported/spawns.json` (output, not in git), [spawns_by_biome.md](spawns_by_biome.md) (generated)

## 6. Offline checks

`bash tools/test_mob_spawns.sh` (needs `./gradlew :patcherMod:compileJava` first; no game):

1. **Java/Python parity:** 20 cases in `tools/mob_spawns_test/cases.json` run through the real `SpawnTuning.apply`
   on vanilla spawn lists (plains, desert, snowy, ocean, mushroom fields, jungle, plains with a duplicate cow entry;
   built by `BiomeDefaultFeatures` after Bootstrap) and through the generator's `apply_rule`; spawners and change
   lines must match. Covers set/scale/remove, add with and without category, weight/factor 0, rounding, duplicates,
   remove-then-add, unchanged sets.
2. **Rhino:** `mob_spawns.js` in the pack's Rhino fork with the fixture rules: every tag equals the generator's
   selection, the modifier holds every rule in order with the right fields and JS-style numbers, `forge:none` for
   `disable_modifiers`.
3. **Rhino edge cases:** bad rules (id, action, missing weight, duplicate id, trait typo, tag in `except`, bad
   category) are reported and left out; switch off, an old jar without `SpawnTuning`, and a missing mod write nothing
   for what they cover.
4. **Generator:** on a fixture dump it writes every section, warns that the dump predates the rules, stops warning
   once the dump shows them, and `--mob` prints a table with "(was N)".

Conditions and spawn ground (2026-10-09): step 2 checks every condition's biome tag against the generator's
`build_conditions`, the modifier's `conditions` entries (no `biomes` field = everywhere) and the block tags; step 3
covers bad conditions (kind, empty mobs, a reference to an unusable rule) and a missing mod's condition mobs and
ground blocks.

Passed 2026-10-09 (ALL OK). Also checked by hand: the pack's real file through the Rhino harness (12 rules, 3
conditions, 3 block tags, no warnings) and the written modifier decoded headless with `SpawnTuning.CODEC` and
`RegistryOps` (12 rules, 3 conditions).

Spot check after the first real dump: plains creatures as in 2.3; mushroom fields = mooshroom only; sunflower plains
has cow (More Mooblooms' set switches Buzzier Bees' removal off).

## 7. In-game test suite

- [ ] **MS-01 dump runs:** `/dumpspawns` -> green chat line "Dumped spawns: N biomes, M mobs", `kubejs/exported/spawns.json` written, no errors in latest.log
- [ ] **MS-02 dumpall:** `/dumpall` -> includes `/dumpspawns`, reports ok
- [ ] **MS-03 vanilla spot check:** plains entry in spawns.json matches section 2.3
- [ ] **MS-04 modifiers applied:** sunflower plains lists `minecraft:cow` (Buzzier Bees' removal switched off by More Mooblooms); meadow lists cow 8 (`moobloom_cows.json`)
- [ ] **MS-05 generator:** `python3 tools/gen_spawn_table.py` writes spawns_by_biome.md without errors or WARNING lines
- [ ] **MS-06 cow table:** `--mob minecraft:cow` output pasted into mooblooms.md; its biome list covers every biome with a rule in mooblooms.md 4.9 except those the rule file adds cows to
- [ ] **MS-07 load log, no rules:** rejoin -> latest.log has `[mob-spawns] 0 of 0 rules` and, after load, `no tune_spawns modifier ran`
- [ ] **MS-08 a test rule applies:** add `{ "id": "test_cow_plains", "mob": "minecraft:cow", "action": "scale", "factor": 2, "add": ["minecraft:plains"] }`, rejoin -> log `1 rules, 1 biomes changed, 1 changes`; `/dumpspawns` -> plains cow 16; generator shows "(was 8)" and no WARNING. Remove the rule afterwards
- [ ] **MS-09 change record:** with the MS-08 rule, spawns.json `tuning.biomes["minecraft:plains"].changes` = `test_cow_plains: scaled x2.0: 8 -> 16` and `before` lists cow 8
- [ ] **MS-10 switch off:** CEConfig `[worldgen.systems] mobSpawns = false` with the MS-08 rule, rejoin -> log says off, plains cow 8
- [ ] **MS-11 conditions:** spawns_by_biome.md section 6 names `Animal.checkAnimalSpawnRules` for cow and a registering class for an Alex's Mobs animal
- [ ] **MS-12 /cegen:** `/cegen run spawns_by_biome dumps` runs `/dumpspawns` then the generator, chat lists spawns_by_biome.md as changed
- [ ] **MS-13 plan rules load:** install the jar, rejoin -> latest.log `[mob-spawns] 12 of 12 rules and 3 of 3 conditions`, and after load `12 rules, N biomes changed ...; conditions: flying_birds, chicken_shores, waterfowl_ponds`; no `[mob-spawns]` errors
- [ ] **MS-14 dump shows them:** `/dumpspawns`, then `python3 tools/gen_spawn_table.py` -> no WARNING lines; spawns.json `tuning.conditions` lists the 3 ids; dark forest lists mooshroom 8; plains cow 16
- [ ] **MS-15 dirt ground:** creative, a fresh world or new chunks in a podzol-heavy biome (old growth taiga) -> cows/pigs/sheep spawn on podzol and coarse dirt; `/execute if block ~ ~-1 ~ #minecraft:animals_spawnable_on` on podzol succeeds
- [ ] **MS-16 night on dirt:** at night, new chunks of a dirt/podzol biome (natural spawns; `/summon` skips the checks) -> animals appear on dirt, not only on grass
- [ ] **MS-17 mooshroom dark forest:** `/locate biome minecraft:dark_forest`, explore new chunks -> mooshroom herds of 2-4 on grass/podzol
- [ ] **MS-18 flying birds on stone/sand:** windswept or beach biome where seagulls / eagles are listed -> they appear on stone and sand in daylight, none in caves or dark spots
- [ ] **MS-19 beach chickens:** new beach chunks -> chicken groups on sand; inland, chickens still only on grass/dirt
- [ ] **MS-20 pond ducks:** a plains or forest lake in daylight -> ducks/geese appear on the water; none wander on plain grass far from water at spawn time (they can walk there later)
- [ ] **MS-21 wetland ducks:** swamp and beach -> ducks/geese spawn on land and water, as on rivers
- [ ] **MS-22 no shiba:** meadow / peaks -> no shibas spawn; JEI shows the Shiba module's items hidden or absent (Quark module off)
- [ ] **MS-23 switch off:** CEConfig `mobSpawns = false`, rejoin -> log says off; `#minecraft:animals_spawnable_on` is grass_block only (`/execute if block` on podzol fails)

## 8. History

- 2026-10-06: planned.
- 2026-10-09: built. `/dumpspawns` reads in Java (patcherMod `SpawnDump`), with biome tags, spawn costs, placement
  sources, class chains and the tuning record; `gen_spawn_table.py` with the biome-type table, vanilla rule names
  from the mapped jar, `--mob`; spawn tuning (`mob_spawns.json`, `mob_spawns.js`, modifier type
  `creativeengineering:tune_spawns`, CEConfig `mobSpawns`) with no rules. Offline tests pass; jar not in `mods/`
  (the game was running); nothing run in game. Section 9 plans not applied, by request.
- 2026-10-09 (night): first real `/dumpspawns`; spawns_by_biome.md generated. Section 10 added: passive overworld
  animal analysis (findings, rule comparison, conditions per animal, share by biome type, per-biome weights, AM selectors).
- 2026-10-09 (late): first block of section 9 applied. 12 rules (mooshrooms, farm-animal biome types, chickens,
  rabbits, ducks/geese); spawn conditions (`light_only` for flying birds and shore chickens, `water_only` for pond
  ducks) via the tune_spawns modifier + `SpawnPlacements` mixin; spawn ground (every dirt) + walk-value mixins;
  Shiba off in Quark's config. Decisions: land animals keep ground + light (no daytime spawns on stone); "birds" =
  flying birds only (not chicken/duck/goose/AM ground birds); lakes and ponds = ducks added to grassy land biomes but
  water-only there. Offline tests extended and passing; jar built, not in `mods/`.


## 9. Plans

First block (down to the ducks) DONE 2026-10-09, see section 3 and 3.1 (not yet tested in game).

- Mooshrooms spawning in dark forests, mirage isles
- make all dirts spawnable for passive mobs
- cow/pig/sheep biome type frequency boosting
  - pigs- forests/swamps
  - sheep- hills/peaks
  - cows- plains/flat
  - chickens- beaches, rivers, sky biomes
  - rabbits- savannah/snow biomes
- disable shiba
- birds spawn in light, land animals spawn in light + pathfind check
- add duck/goose to beaches, lakes, ponds, swamps (not oceans)

# Investigate
- Alexs mobs biome parity? increase frequencies?
- duck and goose parity
- beehive spawning biomes, frequency 

# Later
- Pillager overhaul
  - Lone pillagers in dark forests, mirage isles, swamps, extreme ice biomes, badlands
  - Spawn end pillagers in diving gear
  - pillager structure pool/spawn swap
    - siege equipment- plg, sometimes vind x1
    - camps- plg, vind 0-2
    - towers- plg, vind, evoker/illusioner 0-1
  - remove pillager spawners
- Lone villager in plains, temperate hills, temperate field and meadow biomes 

## 10. Passive overworld animals (analysis 2026-10-09)

Baseline for making creature spawns cohesive, starting with farm animals, the duck and goose, and Alex's Mobs
animals that spawn above ground in the overworld. Snapshot of the 2026-10-09 21:58 `/dumpspawns` with no tuning
rules, overworld surface biomes only (no caves, Nether, End). Spawn rules come from the bytecode (vanilla, mapped jar)
and decompiled jars (Alex's Mobs 1.21.1, untitledduckmod 0.6.1). The passive / neutral / predator split for Alex's Mobs
is a hand classification by behaviour, not something the mod defines. Rerun the dump after rule changes;
[spawns_by_biome.md](spawns_by_biome.md) has the generated per-biome and per-mob lists.

### 10.1 Findings

| Topic | Finding |
|---|---|
| One block everywhere | Sheep 12 / pig 10 / chicken 10 / cow 8 is identical in 63 of the 78 biomes that have farm animals (Terralith's biome files). Jungles differ only by a second chicken entry (chicken 20); wintry forest and lowlands have sheep 8. No biome favours one farm animal over another |
| Share = dilution | What changes per biome is how much other wildlife shares the pool. Cow+pig+sheep+chicken get 53% of creature picks in plains, 38% in forest, 20-29% in jungles, about 20% in savannas, 9% in meadow |
| Grass only | `#animals_spawnable_on` = `minecraft:grass_block` and nothing else. Podzol, coarse dirt, Immersive Weathering soils (fluvisol, humus, vertisol, silt, rooted grass), Botania grasses and stone all fail. Windswept spires and windswept gravelly hills list the full farm block on rock (marked `*` in 10.5) |
| Herd sizes | Set by Terralith per biome, not per animal: 8 in 62 biomes; 4 in swamp, orchid swamp, ice marsh, dark forest, birch forest, old growth birch forest, mirage isles, alpha islands and the skylands; 6 in wintry forest and lowlands; 2-4 in meadow and blooming plateau |
| Gaps | No cow, pig, sheep or chicken in snowy plains, birch taiga, wooded badlands, rocky mountains, scarlet mountains. Meadow and blooming plateau have only cow 8 and sheep 2; skylands winter only sheep 8; skylands autumn sheep and cow; skylands spring pig 12, chicken 10, cow 8 |
| Duck and goose | Only river, frozen river, warm river (weights 6 and 4, groups 2-4); 0.3% and 0.2% of overworld creature picks. Nothing in swamps, mangroves, beaches or lakes |
| Duck and goose rule | Placement `NO_RESTRICTIONS`, spawn predicate always true, obstruction check allows liquid: they spawn on the water. Only remaining check is the pathfinding light value: on grass any time, elsewhere daylight only. Crocodile and platypus in the same river lists need solid ground, so on open water only ducks and geese succeed |
| Alex's Mobs weights | Big weights next to the farm block: gazelle 40, elephant 30, tiger 30, capuchin 28, mudskipper 28, kangaroo 25, gorilla 25, rhinoceros 24, toucan 23. Quark's shiba (40) dominates meadow and peaks |
| Alex's Mobs rules | Every AM animal overrides `checkSpawnRules` with its config roll, which replaces the vanilla pathfinding/light check (gazelle, maned wolf, skunk and banana slug also keep it). Roll N = 1 in N, 0 or 1 = always: only the sunbird (1 in 15) is actually limited. Several need only light (anteater, tiger, bald eagle) or nothing (sunbird, toucan) |
| Where the herds come from | Creatures are persistent, capped at 10 per player area and only spawn every 400 ticks, so most animals are placed at chunk generation: each new chunk rolls herds while a random number stays under the biome's herd chance (0.1; 0.07 in snowy plains, ice spikes, gravel desert, glacial chasm, frozen cliffs), with the same weights and rules (AM rolls included) |

### 10.2 How the rules differ

| Step | Vanilla farm animal | Alex's Mobs animal | Duck and goose |
|---|---|---|---|
| Placement | on ground: solid block below, empty non-fluid space | on ground; birds and monkeys use AM's `am_leaves` (ground or leaves) | none: any empty space, water included |
| Spawn predicate | ground tag + raw light above 8 (`Animal.checkAnimalSpawnRules`) | own static rule per mob (10.3), mostly ground tag + light above 8 | always true |
| Mob's own check | `PathfinderMob`: walk target value >= 0 (grass, or bright enough) | replaced by the config roll (gazelle, maned wolf, skunk, banana slug keep the vanilla check too) | `PathfinderMob`: grass, or light 12+ after time of day |
| Obstruction | no liquid, no collision | no liquid, no collision | no collision (liquid allowed) |
| Night | spawns under open sky (raw light ignores time) | same, light above 8 | only on grass |

### 10.3 Spawn conditions per animal

After the weighted pick, the mob's placement and spawn rule run on the chosen block. "Light above 8" is raw sky + block light with no time-of-day darkening, so open-sky spawns pass at night. Chance = Alex's Mobs config roll (1 in N; 0 or 1 = always). Weights and groups are every distinct value across overworld surface biomes; World = share of overworld surface creature picks by area.

| Animal | Ground under it | Light | Placement | Chance | Weights | Group | Biomes | World % |
|---|---|---|---|---|---|---|---|---|
| **Farm animals** | | | | | | | | |
| Cow | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 8 | 4, 8 | 77 | 2.7 |
| Pig | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 10, 12 | 4, 8 | 74 | 3.3 |
| Sheep | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 2, 8, 12 | 2-4, 4, 6, 8 | 77 | 4.0 |
| Chicken | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 10, 20 | 4, 8 | 74 | 3.5 |
| Rabbit | grass, sand, snow (#rabbits_spawnable_on) | light above 8 | on ground | always | 2, 4, 8, 10 | 2-3, 2-6, 4 | 44 | 0.9 |
| Horse | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 1, 5 | 2-6 | 23 | 0.5 |
| Donkey | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 1 | 1-3, 1, 1-2 | 25 | 0.2 |
| Llama | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 5, 8 | 4-6, 4 | 9 | 0.1 |
| Goat | stone, gravel, snow, packed ice (#goats_spawnable_on) | light above 8 | on ground | always | 4, 5, 10 | 1-3, 2-3, 4 | 6 | <0.1 |
| Mooshroom | mycelium only | light above 8 | on ground | always | 8 | 4-8 | 1 | 0.1 |
| **Duck and goose** | | | | | | | | |
| Duck | anything, including water (no placement rule) | on grass: any time; elsewhere daylight only (light 12+ after time of day) | **none** | always | 6 | 2-4 | 3 | 0.3 |
| Goose | anything, including water (no placement rule) | on grass: any time; elsewhere daylight only (light 12+ after time of day) | **none** | always | 4 | 2-4 | 3 | 0.2 |
| **Alex's Mobs, passive** | | | | | | | | |
| Blue Jay | any spawnable block or leaves | light above 8 | ground or leaves | always | 16 | 2-4 | 35 | 2.8 |
| Crow | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 10 | 3-5 | 44 | 2.5 |
| Raccoon | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 10 | 2-4 | 41 | 2.5 |
| Gazelle | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 40 | 7 | 10 | 2.0 |
| Roadrunner | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | light above 8 | on ground | always | 9 | 2 | 16 | 1.3 |
| Seagull | any non-fluid block | light above 8 | on ground | always | 21 | 3-6 | 9 | 1.2 |
| Seal | natural ground + ice (ice only in frozen oceans) | light above 8 | **none** | always | 20 | 3-8 | 8 | 12 |
| Platypus | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | any; below sea level + 4 | on ground | always | 20 | 1-2 | 2 | 0.9 |
| Hummingbird | leaves, grass, logs, air | light above 8 | on ground | always | 19 | 7 | 24 | 0.8 |
| Sunbird | anything | any | **none** | **1 in 15** | 2 | 1 | 31 | 0.5 |
| Banana Slug | any non-air block | any | on ground | always | 14 | 2-3 | 6 | 0.4 |
| Maned Wolf | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 8 | 1 | 9 | 0.4 |
| Toucan | any spawnable block or leaves | any | ground or leaves | always | 23 | 5 | 8 | 0.4 |
| Potoo | any spawnable block or leaves | light above 8 | ground or leaves | always | 15 | 1 | 1 | 0.2 |
| Sugar Glider | any spawnable block or leaves | light above 8 | ground or leaves | always | 15 | 2-4 | 3 | 0.1 |
| Mungus | any full block | any | on ground | always | 4 | 3-5 | 2 | <0.1 |
| Skunk | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 7 | 1-2 | 15 | 0.5 |
| Bald Eagle | anything | light above 8 | on ground | always | 15 | 2-4 | 18 | 0.4 |
| **Alex's Mobs, neutral** | | | | | | | | |
| Emu | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | light above 8 | on ground | always | 20 | 2-5 | 16 | 2.0 |
| Kangaroo | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | light above 8 | on ground | always | 25 | 3-5 | 16 | 2.5 |
| Elephant | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 30 | 3-5 | 10 | 1.5 |
| Rhinoceros | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 24 | 3-5 | 10 | 1.2 |
| Bison | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 9 | 6-10 | 10 | 0.8 |
| Moose | grass or snow (light check only on snow block) | light above 8 | on ground | always | 9 | 3-4 | 11 | 0.2 |
| Gelada Monkey | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 5 | 9-16 | 7 | 0.2 |
| Capuchin Monkey | leaves, grass, logs, air | light above 8 | ground or leaves | always | 28 | 9-16 | 9 | 0.6 |
| Gorilla | leaves, grass, logs, air | light above 8 | on ground | always | 25 | 7 | 8 | 0.4 |
| Anteater | anything | light above 8 | on ground | always | 7 | 1-3 | 8 | 0.1 |
| Shoebill | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 10 | 1-2 | 4 | <0.1 |
| Mudskipper | mud, muddy mangrove roots | any | on ground | always | 28 | 2-4 | 1 | 0.2 |
| Tusklin | any solid block or snow | light above 8 | on ground | always | 18 | 3-5 | 5 | 0.2 |
| Tasmanian Devil | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 10 | 1-2 | 15 | 0.7 |
| Tarantula Hawk | sand | light above 8 | on ground | always | 6 | 1 | 8 | 0.5 |
| Bunfungus | any full block | any | on ground | always | 3 | 1 | 2 | <0.1 |
| **Alex's Mobs, predators** | | | | | | | | |
| Grizzly Bear | grass block only (#animals_spawnable_on) | sky+block light above 8 (night OK under open sky) | on ground | always | 8 | 2-3 | 35 | 1.4 |
| Snow Leopard | stone, dirt, grass | light above 8 | on ground | always | 18 | 1-2 | 28 | 1.8 |
| Tiger | anything | light above 8 | on ground | always | 30 | 1-3 | 6 | 0.2 |
| Komodo Dragon | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | light above 8 | on ground | always | 16 | 1-2 | 8 | 0.2 |
| Crocodile | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | any; below sea level + 4 | on ground | always | 20 | 1-2 | 7 | 1.2 |
| Alligator Snapping Turtle | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | any; below sea level + 4 | on ground | always | 20 | 1-2 | 3 | 0.1 |
| Anaconda | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | any; below sea level + 4 | on ground | always | 12 | 1 | 7 | 0.2 |
| Rattlesnake | natural ground (#alexsmobs:am_spawns: grass, dirt, IW soils, stone, sand, terracotta...) | light above 8 | on ground | always | 12 | 1-2 | 16 | 1.7 |
| Froststalker | any solid block, ice, snow | light above 8 | on ground | always | 20 | 5-7 | 6 | 0.3 |

### 10.4 Share of creature picks by biome type

Area-weighted mean % of creature spawn picks in biomes with that trait (`wild_crops.json` traits; a biome counts in every trait it has; biomes with no sampled area count at weight 0.01). Blank = never listed in that type. Row order: World % within each group.

| Animal | Biomes | World | plains | flower | forest | swamp | jungle | savanna | shrubland | desert | badlands | mountain | peak | snowy | beach | river | sky | ocean | mushroom |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| *biomes of type* | | | 14 | 13 | 50 | 4 | 9 | 11 | 5 | 9 | 8 | 41 | 7 | 29 | 3 | 3 | 4 | 9 | 1 |
| **Farm animals** | | | | | | | | | | | | | | | | | | | |
| Sheep | 77 | 4.0 | 11 | 6.5 | 10.0 | 5.7 | 6.0 | 7.2 | 8.7 |  |  | 5.8 | 0.2 | 4.6 |  |  | 5.1 |  |  |
| Chicken | 74 | 3.5 | 8.9 | 4.9 | 9.2 | 4.8 | 10.0 | 6.0 | 7.3 |  |  | 4.9 | 0.2 | 4.0 |  |  | 4.1 |  |  |
| Pig | 74 | 3.3 | 8.9 | 4.9 | 8.4 | 4.8 | 5.0 | 6.0 | 7.3 |  |  | 4.7 | 0.2 | 4.0 |  |  | 4.7 |  |  |
| Cow | 77 | 2.7 | 8.2 | 7.1 | 6.7 | 3.8 | 4.0 | 4.8 | 5.8 |  |  | 4.5 | 0.1 | 3.2 |  |  | 4.4 |  |  |
| Rabbit | 44 | 0.9 | 1.4 | 2.3 | 1.8 |  |  |  |  | 12 |  | 1.2 |  | 3.7 |  |  | 4.0 |  |  |
| Horse | 23 | 0.5 | 4.3 | 0.5 | 0.3 |  |  | 1.2 | 2.2 |  |  | 1.4 | <0.1 | 0.4 |  |  |  |  |  |
| Donkey | 25 | 0.2 | 1.0 | 0.5 | <0.1 |  |  | 0.6 | 0.7 |  |  | 0.4 | <0.1 | <0.1 |  |  |  |  |  |
| Mooshroom | 1 | 0.1 |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | 53 |
| Llama | 9 | 0.1 |  |  | <0.1 |  |  | 0.7 |  |  |  | 0.6 |  |  |  |  |  |  |  |
| Goat | 6 | <0.1 |  |  | <0.1 |  |  |  |  |  | 0.2 | 0.8 | 3.8 | 1.3 |  |  | 3.2 |  |  |
| **Duck and goose** | | | | | | | | | | | | | | | | | | | |
| Duck | 3 | 0.3 |  |  |  |  |  |  |  |  |  |  |  | 1.0 |  | 13 |  |  |  |
| Goose | 3 | 0.2 |  |  |  |  |  |  |  |  |  |  |  | 0.7 |  | 8.7 |  |  |  |
| **Alex's Mobs, passive** | | | | | | | | | | | | | | | | | | | |
| Seal | 8 | 12 |  |  |  |  |  |  |  |  |  |  |  | 0.8 | 39 |  |  | 26 |  |
| Blue Jay | 35 | 2.8 | 3.6 | 5.2 | 11 |  |  |  |  |  | 2.2 | 5.4 |  | 3.0 |  |  | 2.2 |  |  |
| Crow | 44 | 2.5 | 10 | 7.2 | 7.0 |  |  | 1.1 | 3.1 |  |  | 4.0 |  | 3.8 |  |  | 7.0 |  |  |
| Raccoon | 41 | 2.5 | 10 | 7.0 | 7.0 |  |  | 1.1 | 3.1 |  |  | 4.0 |  | 3.7 |  |  | 1.4 |  |  |
| Gazelle | 10 | 2.0 | 1.4 |  |  |  |  | 19 | 17 | 1.0 |  | 2.3 | 0.7 |  |  |  |  |  |  |
| Roadrunner | 16 | 1.3 |  |  |  |  |  | 0.2 |  | 27 | 12 | 0.9 |  |  |  |  |  |  |  |
| Seagull | 9 | 1.2 |  | 0.4 | <0.1 |  |  |  |  |  |  |  |  | 0.9 | 41 |  | 9.1 |  |  |
| Platypus | 2 | 0.9 |  |  |  |  |  |  |  |  |  |  |  |  |  | 35 |  |  |  |
| Hummingbird | 24 | 0.8 | 3.1 | 17 | 2.4 | 1.5 | 9.5 |  |  |  |  | 3.3 |  |  |  |  | 10 |  |  |
| Skunk | 15 | 0.5 |  | 2.3 | 2.3 |  |  |  |  |  |  | 0.7 |  | 0.5 |  |  |  |  |  |
| Sunbird | 31 | 0.5 | 0.2 | 0.7 | 0.4 |  | <0.1 | <0.1 |  | 0.4 | 0.3 | 3.7 | 3.8 | 1.0 |  |  | 1.6 |  |  |
| Bald Eagle | 18 | 0.4 | 0.6 | 6.5 | 1.3 |  |  |  |  |  | 2.1 | 2.0 |  | 0.4 |  |  | 10 |  |  |
| Banana Slug | 6 | 0.4 |  |  | 1.8 |  |  |  |  |  |  | 0.9 |  |  |  |  | 1.9 |  |  |
| Maned Wolf | 9 | 0.4 | 0.3 |  |  |  |  | 3.9 | 3.3 |  |  | 0.5 | 0.1 |  |  |  |  |  |  |
| Toucan | 8 | 0.4 |  |  | 1.7 |  | 11 |  |  |  |  | 0.6 |  |  |  |  | 2.6 |  |  |
| Potoo | 1 | 0.2 |  |  | 0.9 |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| Sugar Glider | 3 | 0.1 |  |  | 0.6 |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| Mungus | 2 | <0.1 |  |  | <0.1 |  |  |  |  |  |  |  |  |  |  |  |  |  | 27 |
| **Alex's Mobs, neutral** | | | | | | | | | | | | | | | | | | | |
| Kangaroo | 16 | 2.5 | 0.9 |  |  |  |  | 11 | 8.8 | 0.6 | 34 | 2.1 | 0.4 |  |  |  |  |  |  |
| Emu | 16 | 2.0 | 0.7 |  |  |  |  | 9.1 | 7.0 | 0.5 | 27 | 1.7 | 0.4 |  |  |  |  |  |  |
| Elephant | 10 | 1.5 | 1.1 |  |  |  |  | 15 | 12 | 0.8 |  | 1.7 | 0.5 |  |  |  |  |  |  |
| Rhinoceros | 10 | 1.2 | 0.8 |  |  |  |  | 12 | 10.0 | 0.6 |  | 1.4 | 0.4 |  |  |  |  |  |  |
| Bison | 10 | 0.8 | 9.3 | 3.3 |  |  |  |  | 0.8 |  |  | 2.1 |  | 1.7 |  |  |  |  |  |
| Tasmanian Devil | 15 | 0.7 |  | 3.3 | 3.3 |  |  |  |  |  |  | 0.9 |  | 0.7 |  |  |  |  |  |
| Capuchin Monkey | 9 | 0.6 |  |  | 2.1 | 12 | 13 |  |  |  |  | 0.7 |  |  |  |  | 3.1 |  |  |
| Tarantula Hawk | 8 | 0.5 |  |  |  |  |  |  |  | 18 |  | 0.4 |  |  |  |  |  |  |  |
| Gorilla | 8 | 0.4 |  |  | 1.9 |  | 12 |  |  |  |  | 0.6 |  |  |  |  | 2.8 |  |  |
| Tusklin | 5 | 0.2 | 2.0 |  |  |  |  |  | 0.1 | 0.5 | 0.9 |  |  | 3.5 |  |  |  |  |  |
| Gelada Monkey | 7 | 0.2 | 1.9 | 1.3 |  |  |  | 0.6 | 1.2 |  |  | 1.1 |  | <0.1 |  |  |  |  |  |
| Moose | 11 | 0.2 | 0.8 |  | 0.4 | 0.6 |  |  |  | 0.3 | 0.5 | <0.1 |  | 2.8 |  |  | 2.4 |  |  |
| Mudskipper | 1 | 0.2 |  |  |  | 12 |  |  |  |  |  |  |  |  |  |  |  |  |  |
| Anteater | 8 | 0.1 |  |  | 0.5 |  | 3.2 |  |  |  |  | 0.2 |  |  |  |  | 0.8 |  |  |
| Shoebill | 4 | <0.1 |  | 0.4 |  | 4.8 |  |  |  | 0.3 |  |  |  | 0.1 |  |  |  |  |  |
| Bunfungus | 2 | <0.1 |  |  | <0.1 |  |  |  |  |  |  |  |  |  |  |  |  |  | 20 |
| **Alex's Mobs, predators** | | | | | | | | | | | | | | | | | | | |
| Snow Leopard | 28 | 1.8 | 2.9 |  | 1.0 | 1.1 |  |  | 1.5 | 0.5 | 0.9 | 5.8 | 14 | 21 | 1.7 | 7.4 | 4.7 | 1.1 |  |
| Rattlesnake | 16 | 1.7 |  |  |  |  |  | 0.2 |  | 35 | 16 | 1.2 |  |  |  |  |  |  |  |
| Grizzly Bear | 35 | 1.4 | 1.8 | 2.6 | 5.6 |  |  |  |  |  | 1.1 | 2.7 |  | 1.5 |  |  | 1.1 |  |  |
| Crocodile | 7 | 1.2 |  | 0.8 |  | 18 |  |  |  | 0.5 |  |  |  | 0.3 |  | 35 |  |  |  |
| Froststalker | 6 | 0.3 | 0.4 |  |  |  |  |  |  | 0.6 | 1.0 | 2.1 | 8.1 | 5.3 |  |  |  |  |  |
| Komodo Dragon | 8 | 0.2 |  |  | 0.8 |  | 4.9 |  |  | 1.5 |  | 0.7 |  |  |  |  | 1.8 |  |  |
| Anaconda | 7 | 0.2 |  | 0.5 | 0.2 | 11 | 1.2 |  |  |  |  |  |  | 0.2 |  |  | 1.3 |  |  |
| Tiger | 6 | 0.2 |  | 2.1 | 0.7 |  | 2.9 |  |  |  |  | 0.2 |  |  |  |  | 8.8 |  |  |
| Alligator Snapping Turtle | 3 | 0.1 |  | 0.8 |  | 9.5 |  |  |  |  |  |  |  | 0.3 |  |  |  |  |  |

### 10.5 Biome by biome: farm and waterfowl weights

Cells are spawn **weights** (blank = not listed). `*` = grass-only animal listed in a biome whose surface traits are not grassy, so most of its picks fail. **Split** = share of the biome's creature picks by group: F farm, W duck/goose, A Alex's Mobs passive/neutral, P Alex's Mobs predators, O other (vanilla wild, Quark). **Herd** = group size of the farm entries. **Rest of list** = every other creature entry with its weight. Area = % of the overworld surface (`-` = no sampled area). Chance = chunk-generation herd chance. Bloom = % of cow spawns More Mooblooms turns into bloom variants. Biomes with no farm animals or waterfowl are in 10.6.

**Plains** (4)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Plains | 3.10 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 61/0/39/0/0 | 8 | 2.0 | raccoon 10, crow 10, bison 9 |
| Steppe (terralith) | 0.43 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 57/0/42/0/0 | 8 |  | raccoon 10, crow 10, bison 9, gelada monkey 5 |
| Shield Clearing (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 51/0/40/9/0 | 8 |  | blue jay 16, raccoon 10, crow 10, grizzly bear 8 |
| Valley Clearing (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 37/0/56/7/0 | 8 |  | hummingbird 19, blue jay 16, raccoon 10, crow 10, bison 9, grizzly bear 8, gelada monkey 5 |

**Flower** (6)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Sunflower Plains | 0.27 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 49/0/51/0/0 | 8 | 100 | hummingbird 19, raccoon 10, crow 10, bison 9 |
| Sakura Grove (terralith) | 0.23 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 26/0/51/22/0 | 8 | 65.0 | tiger 30, hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Flower Forest | 0.13 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 35/0/58/6/0 | 8 | 100 | hummingbird 19, blue jay 16, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Moonlight Grove (terralith) | 0.07 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 32/0/63/6/0 | 8 | 45.0 | hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Skylands Spring (terralith) | 0.07 | 0.1 | 8 | 12 |  | 10 | 10 |  |  |  |  |  |  |  | 29/0/49/22/0 | 4 | 50.0 | tiger 30, seagull 21, hummingbird 19, bald eagle 15, crow 10, sunbird 2 |
| Lavender Forest (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 32/0/63/6/0 | 8 | 75.0 | hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |

**Forest** (15)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Yellowstone (terralith) | 3.60 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 42/0/36/8/15 | 8 | 2.0 | blue jay 16, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8, sunbird 2 |
| Forest | 3.43 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 38/0/50/8/5 | 8 | 2.0 | blue jay 16, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7, wolf 5 |
| Dark Forest | 1.50 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 34/0/59/7/0 | 4 | 2.0 | blue jay 16, potoo 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Old Growth Spruce Taiga | 1.00 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 37/0/42/7/14 | 8 | 2.0 | blue jay 16, banana slug 14, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Taiga | 0.97 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 42/0/35/8/15 | 8 | 2.0 | blue jay 16, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Shield (terralith) | 0.87 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 37/0/42/7/14 | 8 | 2.0 | blue jay 16, banana slug 14, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Old Growth Birch Forest | 0.60 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 34/0/59/7/0 | 4 | 35.0 | blue jay 16, sugar glider 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Old Growth Pine Taiga | 0.50 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 37/0/42/7/14 | 8 | 2.0 | blue jay 16, banana slug 14, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Birch Forest | 0.37 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 34/0/59/7/0 | 4 | 25 | blue jay 16, sugar glider 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Windswept Forest | 0.20 | 0.1 | 8 | 10 | 12 | 10 |  |  |  | 5 |  |  |  |  | 75/0/25/0/0 | 8 | 2.0 | bald eagle 15 |
| Lush Valley (terralith) | 0.10 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 42/0/35/8/15 | 8 | 2.0 | blue jay 16, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Mirage Isles (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 48/0/52/0/0 | 4 |  | raccoon 10, crow 10, tasmanian devil 10, skunk 7, mungus 4, bunfungus 3 |
| Skylands Autumn (terralith) | 0.03 | 0.1 | 8 |  | 12 |  |  |  |  |  | 4 |  |  |  | 17/0/75/6/3 | 4 |  | seagull 21, hummingbird 19, blue jay 16, bald eagle 15, banana slug 14, raccoon 10, crow 10, grizzly bear 8, fox 4, sunbird 2 |
| Yosemite Lowlands (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 37/0/42/7/14 | 8 | 2.0 | blue jay 16, banana slug 14, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Alpha Islands (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 100/0/0/0/0 | 4 | 25.0 |  |

**Swamp** (3)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Swamp | 0.53 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 36/0/9/46/9 | 4 |  | crocodile 20, alligator snapping turtle 20, anaconda 12, frog 10, shoebill 10 |
| Orchid Swamp (terralith) | 0.17 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 27/0/20/36/17 | 4 | 65.0 | frog 25, crocodile 20, alligator snapping turtle 20, hummingbird 19, anaconda 12, shoebill 10 |
| Ice Marsh (terralith) | 0.13 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 27/0/13/48/12 | 4 | 2.0 | crocodile 20, alligator snapping turtle 20, frog 18, snow leopard 18, anaconda 12, shoebill 10, moose 9 |

**River** (3)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| River | 2.23 | 0.1 |  |  |  |  |  |  |  |  |  |  | 6 | 4 | 0/20/40/40/0 |  |  | crocodile 20, platypus 20 |
| Frozen River | 0.30 | 0.1 |  |  |  |  |  |  |  |  |  |  | 6 | 4 | 0/36/0/64/0 |  |  | snow leopard 18 |
| Warm River (terralith) | 0.07 | 0.1 |  |  |  |  |  |  |  |  |  |  | 6 | 4 | 0/20/40/40/0 |  |  | crocodile 20, platypus 20 |

**Jungle** (9)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Sparse Jungle | 0.93 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 29/0/59/9/3 | 4, 8 | 2.0 | capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, komodo dragon 16, anteater 7, wolf 5 |
| Jungle | 0.90 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 26/0/53/0/21 | 4, 8 | 2.0 | parrot 40, capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, anteater 7, panda 1 |
| Rocky Jungle (terralith) | 0.43 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 24/0/49/8/20 | 4, 8 | 2.0 | parrot 40, capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, komodo dragon 16, anteater 7, panda 1 |
| Tropical Jungle (terralith) | 0.43 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 24/0/50/6/20 | 4, 8 | 2.0 | parrot 40, capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, anaconda 12, anteater 7, panda 1 |
| Bamboo Jungle | 0.33 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 21/0/8/20/51 | 4, 8 | 2.0 | panda 80, parrot 40, tiger 30, hummingbird 19, komodo dragon 16 |
| Amethyst Rainforest (terralith) | 0.33 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 20/0/41/23/16 | 4, 8 | 2.0 | parrot 40, tiger 30, capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, komodo dragon 16, anaconda 12, anteater 7, panda 1 |
| Amethyst Canyon (terralith) | 0.17 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 21/0/43/19/17 | 4, 8 | 2.0 | parrot 40, tiger 30, capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, komodo dragon 16, anteater 7, panda 1 |
| Jungle Mountains (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 20 |  |  |  |  |  |  |  |  | 26/0/53/0/21 | 4, 8 | 2.0 | parrot 40, capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, anteater 7, sunbird 2, panda 1 |
| Skylands Summer (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 22/0/58/16/3 | 8 | 50.0 | capuchin monkey 28, gorilla 25, toucan 23, hummingbird 19, komodo dragon 16, anaconda 12, anteater 7, parrot 6, sunbird 2 |

**Savanna and shrubland** (11)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Brushland (terralith) | 3.30 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 |  |  |  |  |  | 22/0/78/0/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8 |
| Savanna | 2.60 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 |  |  |  |  |  | 22/0/78/0/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8 |
| Savanna Plateau | 1.23 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 | 8 |  |  |  |  | 25/0/73/0/2 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8, wolf 5 |
| Hot Shrubland (terralith) | 0.80 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 65/0/35/0/0 | 8 | 25 | raccoon 10, crow 10, gelada monkey 5 |
| Arid Highlands (terralith) | 0.57 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 |  |  |  |  |  | 22/0/78/0/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8 |
| Windswept Savanna | 0.47 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 |  |  |  |  |  | 22/0/78/0/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8 |
| Shrubland (terralith) | 0.47 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 31/0/69/0/0 | 8 |  | gazelle 40, elephant 30, rhinoceros 24, maned wolf 8 |
| Savanna Badlands (terralith) | 0.40 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 | 8 |  |  |  |  | 23/0/72/5/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, rattlesnake 12, roadrunner 9, maned wolf 8, sunbird 2 |
| Fractured Savanna (terralith) | 0.07 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 |  |  |  |  |  | 22/0/78/0/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8 |
| Savanna Slopes (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 | 8 |  |  |  |  | 25/0/75/0/0 | 8 | 2.0 | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, emu 20, maned wolf 8, sunbird 2 |
| Ashen Savanna (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 |  | 1 | 1 | 8 |  |  |  |  | 100/0/0/0/0 | 8 | 2.0 |  |

**Desert** (9)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Desert | 2.27 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 13/0/48/39/0 |  |  | rattlesnake 12, roadrunner 9, tarantula hawk 6 |
| Desert Canyon (terralith) | 0.17 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 12/0/52/36/0 |  |  | rattlesnake 12, roadrunner 9, tarantula hawk 6, sunbird 2 |
| Red Oasis (terralith) | 0.17 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 2/0/76/22/0 |  |  | gazelle 40, elephant 30, kangaroo 25, rhinoceros 24, crocodile 20, emu 20, komodo dragon 16, rattlesnake 12, shoebill 10, roadrunner 9, tarantula hawk 6 |
| Desert Oasis (terralith) | 0.10 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 13/0/48/39/0 |  |  | rattlesnake 12, roadrunner 9, tarantula hawk 6 |
| Lush Desert (terralith) | 0.10 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 13/0/48/39/0 |  |  | rattlesnake 12, roadrunner 9, tarantula hawk 6 |
| Sandstone Valley (terralith) | 0.10 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 9/0/32/60/0 |  |  | komodo dragon 16, rattlesnake 12, roadrunner 9, tarantula hawk 6 |
| Gravel Desert (terralith) | 0.07 | 0.07 |  |  |  |  | 10 |  |  |  |  |  |  |  | 13/0/36/50/1 |  |  | froststalker 20, snow leopard 18, tusklin 18, moose 9, polar bear 1 |
| Ancient Sands (terralith) | 0.03 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 13/0/48/39/0 |  |  | rattlesnake 12, roadrunner 9, tarantula hawk 6 |
| Desert Spires (terralith) | 0.03 | 0.1 |  |  |  |  | 4 |  |  |  |  |  |  |  | 12/0/52/36/0 |  |  | rattlesnake 12, roadrunner 9, tarantula hawk 6, sunbird 2 |

**Badlands** (1)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Painted Mountains (terralith) | 0.10 | 0.1 |  |  |  |  |  |  |  |  | 5 |  |  |  | 7/0/77/16/0 |  |  | kangaroo 25, emu 20, rattlesnake 12, roadrunner 9, sunbird 2 |

**Mountain and highland** (17)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Highlands (terralith) | 1.00 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 44/0/48/8/0 | 8 | 25 | blue jay 16, raccoon 10, crow 10, bison 9, grizzly bear 8, gelada monkey 5 |
| Alpine Highlands (terralith) | 0.97 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 44/0/48/8/0 | 8 | 2.0 | blue jay 16, raccoon 10, crow 10, bison 9, grizzly bear 8, gelada monkey 5 |
| Meadow | 0.80 | 0.1 | 8 |  | 2 |  | 2 |  | 1 |  |  |  |  |  | 12/0/51/0/37 | 2-4, 4 | 65.0 | shiba 40, hummingbird 19, raccoon 10, crow 10, bison 9, gelada monkey 5, sunbird 2 |
| Forested Highlands (terralith) | 0.80 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 44/0/48/8/0 | 8 | 25 | blue jay 16, banana slug 14, raccoon 10, crow 10, grizzly bear 8 |
| Temperate Highlands (terralith) | 0.57 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 38/0/56/7/0 | 8 | 25 | blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Moonlight Valley (terralith) | 0.47 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 32/0/63/6/0 | 8 | 75.0 | hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Blooming Valley (terralith) | 0.30 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 31/0/63/6/0 | 8 | 100 | hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7, sunbird 2 |
| Blooming Plateau (terralith) | 0.17 | 0.1 | 8 |  | 2 |  | 2 |  | 1 |  |  |  |  |  | 28/0/72/0/0 | 2-4, 4 | 100 | hummingbird 19, bald eagle 15 |
| Yosemite Cliffs (terralith) | 0.17 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 71/0/3/0/26 | 8 |  | wolf 8, fox 8, sunbird 2 |
| Windswept Hills | 0.13 | 0.1 | 8 | 10 | 12 | 10 |  |  |  | 5 |  |  |  |  | 100/0/0/0/0 | 8 |  |  |
| Windswept Spires (terralith) | 0.10 | 0.1 | 8* | 10* | 12* | 10* |  |  |  | 5* |  |  |  |  | 96/0/4/0/0 | 8 |  | sunbird 2 |
| Lavender Valley (terralith) | 0.07 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 32/0/63/6/0 | 8 | 75.0 | hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |
| Windswept Gravelly Hills | <0.03 | 0.1 | 8* | 10* | 12* | 10* |  |  |  | 5* |  |  |  |  | 100/0/0/0/0 | 8 |  |  |
| Caldera (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 43/0/2/0/55 | 8 |  | shiba 40, wolf 8, fox 8, sunbird 2 |
| Cloud Forest (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 37/0/43/7/13 | 8 | 45.0 | blue jay 16, bald eagle 15, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Haze Mountain (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 |  |  |  | 5 |  |  |  |  | 73/0/27/0/0 | 8 | 2.0 | bald eagle 15, sunbird 2 |
| Sakura Valley (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 26/0/51/22/0 | 8 | 65.0 | tiger 30, hummingbird 19, blue jay 16, bald eagle 15, raccoon 10, crow 10, tasmanian devil 10, grizzly bear 8, skunk 7 |

**Peaks** (2)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Frozen Peaks | 0.70 | 0.1 |  |  |  |  |  |  |  |  | 5 |  |  |  | 6/0/2/45/47 |  |  | shiba 40, froststalker 20, snow leopard 18, sunbird 2 |
| Jagged Peaks | 0.47 | 0.1 |  |  |  |  |  |  |  |  | 5 |  |  |  | 8/0/3/28/62 |  |  | shiba 40, snow leopard 18, sunbird 2 |

**Snowy** (18)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Snowy Plains | 0.67 | 0.07 |  |  |  |  | 10 |  |  |  |  |  |  |  | 12/0/66/21/1 |  |  | snow leopard 18, tusklin 18, raccoon 10, crow 10, moose 9, bison 9, polar bear 1 |
| Siberian Taiga (terralith) | 0.67 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 73/0/0/0/27 | 8 |  | wolf 8, fox 8 |
| Snowy Taiga | 0.57 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 34/0/34/20/12 | 8 |  | snow leopard 18, blue jay 16, raccoon 10, crow 10, moose 9, wolf 8, fox 8, grizzly bear 8 |
| Cold Shrubland (terralith) | 0.37 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 49/0/31/19/0 | 8 | 2.0 | snow leopard 18, raccoon 10, crow 10, bison 9 |
| Wintry Forest (terralith) | 0.23 | 0.1 | 8 | 10 | 8 | 10 | 8 |  |  |  |  |  |  |  | 34/0/34/20/12 | 4, 6 |  | snow leopard 18, blue jay 16, raccoon 10, crow 10, moose 9, wolf 8, fox 8, grizzly bear 8 |
| Glacial Chasm (terralith) | 0.20 | 0.07 |  |  |  |  | 10 |  |  |  |  |  |  |  | 20/0/0/78/2 |  |  | froststalker 20, snow leopard 18, polar bear 1 |
| Grove | 0.10 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 32/0/37/19/12 | 8 |  | snow leopard 18, blue jay 16, bald eagle 15, raccoon 10, crow 10, wolf 8, fox 8, grizzly bear 8 |
| Ice Spikes | 0.10 | 0.07 |  |  |  |  | 10 |  |  |  |  |  |  |  | 15/0/27/57/1 |  |  | froststalker 20, snow leopard 18, tusklin 18, polar bear 1 |
| Wintry Lowlands (terralith) | 0.10 | 0.1 | 8 | 10 | 8 | 10 | 8 |  |  |  |  |  |  |  | 51/0/10/21/18 | 4, 6 |  | snow leopard 18, moose 9, wolf 8, fox 8 |
| Alpine Grove (terralith) | 0.07 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 30/0/41/18/11 | 8 |  | snow leopard 18, blue jay 16, bald eagle 15, raccoon 10, crow 10, moose 9, wolf 8, fox 8, grizzly bear 8 |
| Snowy Slopes | 0.03 | 0.1 |  |  |  |  | 4 |  |  |  | 5 |  |  |  | 13/0/3/26/58 |  |  | shiba 40, snow leopard 18, sunbird 2 |
| Rocky Shrubland (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 40/0/45/16/0 | 8 | 2.0 | snow leopard 18, tusklin 18, raccoon 10, crow 10, bison 9, gelada monkey 5 |
| Skylands Winter (terralith) | 0.03 | 0.1 |  |  | 8 |  | 4 |  |  |  | 10 |  |  |  | 29/0/47/24/0 | 4 |  | snow leopard 18, bald eagle 15, crow 10, moose 9, sunbird 2 |
| Snowy Maple Forest (terralith) | 0.03 | 0.1 | 8 | 10 | 12 | 10 |  | 5 | 1 |  |  |  |  |  | 43/0/33/24/0 | 8 | 2.0 | snow leopard 18, blue jay 16, crow 10, moose 9, grizzly bear 8 |
| Alpha Islands Winter (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 |  |  |  |  |  |  |  |  | 69/0/0/31/0 | 4 | 25.0 | snow leopard 18 |
| Frozen Cliffs (terralith) | <0.03 | 0.07 |  |  |  |  | 10 |  |  |  |  |  |  |  | 20/0/0/78/2 |  |  | froststalker 20, snow leopard 18, polar bear 1 |
| Siberian Grove (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 56/0/0/23/21 | 8 |  | snow leopard 18, wolf 8, fox 8 |
| Snowy Shield (terralith) | <0.03 | 0.1 | 8 | 10 | 12 | 10 | 4 |  |  |  |  |  |  |  | 40/0/23/23/14 | 8 |  | snow leopard 18, blue jay 16, moose 9, wolf 8, fox 8, grizzly bear 8 |

**Mushroom** (1)

| Biome | Area | Chance | Cow | Pig | Sheep | Chick. | Rabbit | Horse | Donkey | Llama | Goat | Moosh. | Duck | Goose | Split F/W/A/P/O % | Herd | Bloom | Rest of list |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Mushroom Fields | 0.23 | 0.1 |  |  |  |  |  |  |  |  |  | 8 |  |  | 53/0/47/0/0 |  |  | mungus 4, bunfungus 3 |

### 10.6 Biomes with no farm animals or waterfowl

| Biome | Type | Surface | Area | Creature list (weight, group) |
|---|---|---|---|---|
| Mangrove Swamp | swamp | muddy | 0.60 | capuchin monkey 28 (9-16), mudskipper 28 (2-4), crocodile 20 (1-2), anaconda 12 (1), frog 10 (2-5) |
| Beach | beach | sandy | 1.43 | seagull 21 (3-6), seal 20 (3-8), turtle 5 (2-5), crab 5 (1-3) |
| Gravel Beach (terralith) | beach | gravelly | 1.20 | seagull 21 (3-6), seal 20 (3-8), turtle 5 (2-5), crab 5 (1-3) |
| Snowy Beach | beach | sandy, snowy | 0.17 | seagull 21 (3-6), seal 20 (3-8), snow leopard 18 (1-2), crab 5 (1-3) |
| Stony Shore | beach | rocky | <0.03 | seagull 21 (3-6), seal 20 (3-8) |
| Basalt Cliffs (terralith) | beach | rocky | <0.03 | seagull 21 (3-6), sunbird 2 (1) |
| Granite Cliffs (terralith) | beach | rocky | <0.03 | seagull 21 (3-6), sunbird 2 (1) |
| White Cliffs (terralith) | beach | rocky | <0.03 | seagull 21 (3-6), sugar glider 15 (2-4), sunbird 2 (1) |
| Badlands | badlands | sandy, terracotta | 1.93 | kangaroo 25 (3-5), emu 20 (2-5), rattlesnake 12 (1-2), roadrunner 9 (2) |
| Bryce Canyon (terralith) | badlands | sandy, terracotta | 0.60 | kangaroo 25 (3-5), emu 20 (2-5), blue jay 16 (2-4), bald eagle 15 (2-4), rattlesnake 12 (1-2), roadrunner 9 (2), grizzly bear 8 (2-3), sunbird 2 (1) |
| Wooded Badlands | badlands | grassy, sandy, terracotta | 0.57 | kangaroo 25 (3-5), emu 20 (2-5), rattlesnake 12 (1-2), roadrunner 9 (2), wolf 5 (4) |
| Eroded Badlands | badlands | sandy, terracotta | 0.53 | kangaroo 25 (3-5), emu 20 (2-5), rattlesnake 12 (1-2), roadrunner 9 (2) |
| Snowy Badlands (terralith) | badlands | snowy, terracotta | 0.13 | froststalker 20 (5-7), snow leopard 18 (1-2), tusklin 18 (3-5), moose 9 (3-4) |
| Warped Mesa (terralith) | badlands | terracotta | 0.10 | kangaroo 25 (3-5), emu 20 (2-5), rattlesnake 12 (1-2), roadrunner 9 (2) |
| White Mesa (terralith) | badlands | terracotta | 0.03 | kangaroo 25 (3-5), emu 20 (2-5), rattlesnake 12 (1-2), roadrunner 9 (2) |
| Stony Spires (terralith) | mountain | rocky | 0.27 | sunbird 2 (1) |
| Stony Peaks | peak | rocky | 0.47 | shiba 40 (1-3), sunbird 2 (1) |
| Volcanic Peaks (terralith) | peak | rocky | 0.30 | shiba 40 (1-3), sunbird 2 (1) |
| Emerald Peaks (terralith) | peak | snowy, rocky | <0.03 | shiba 40 (1-3), snow leopard 18 (1-2), sunbird 2 (1) |
| Volcanic Crater (terralith) | peak | rocky | <0.03 | sunbird 2 (1) |
| Rocky Mountains (terralith) | snowy | grassy, snowy, rocky | 0.77 | shiba 40 (1-3), snow leopard 18 (1-2), sunbird 2 (1) |
| Birch Taiga (terralith) | snowy | grassy, snowy | 0.27 | blue jay 16 (2-4), raccoon 10 (2-4), crow 10 (3-5), tasmanian devil 10 (1-2), grizzly bear 8 (2-3), skunk 7 (1-2) |
| Scarlet Mountains (terralith) | snowy | grassy, snowy | 0.17 | shiba 40 (1-3), snow leopard 18 (1-2), sunbird 2 (1) |
| Deep Ocean | ocean | - | 9.07 | none |
| Deep Lukewarm Ocean | ocean | - | 6.23 | none |
| Ocean | ocean | - | 6.23 | none |
| Lukewarm Ocean | ocean | - | 5.03 | none |
| Deep Cold Ocean | ocean | - | 4.93 | seal 20 (3-8) |
| Warm Ocean | ocean | - | 4.60 | none |
| Cold Ocean | ocean | - | 4.17 | seal 20 (3-8) |
| Deep Frozen Ocean | ocean | - | 1.40 | seal 20 (3-8), polar bear 1 (1-2) |
| Frozen Ocean | ocean | - | 1.03 | seal 20 (3-8), snow leopard 18 (1-2), polar bear 1 (1-2) |

### 10.7 Alex's Mobs biome selectors

From `config/alexsmobs/<mob>_spawns.json`: clauses are OR'ed, terms in a clause AND'ed, `!` negates, `#` = biome tag (`minecraft:` dropped). Biomes O' Plenty entries are inert (mod not installed).

| Animal | Clauses |
|---|---|
| Blue Jay | #is_overworld & #is_forest & !sparse_jungle<br>#is_overworld & #is_taiga<br>terralith:alpine_grove<br>terralith:blooming_valley<br>terralith:lavender_forest<br>terralith:lavender_valley<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:highlands<br>terralith:shield_clearing<br>terralith:valley_clearing<br>terralith:alpine_highlands<br>terralith:cloud_forest<br>terralith:forested_highlands<br>terralith:lush_valley<br>terralith:shield<br>terralith:snowy_maple_forest<br>terralith:snowy_shield<br>terralith:temperate_highlands<br>terralith:wintry_forest<br>terralith:yosemite_lowlands<br>terralith:bryce_canyon<br>terralith:skylands_autumn<br>biomesoplenty:redwood_forest |
| Crow | #is_overworld & !#is_savanna & #forge:is_plains<br>#is_overworld & #is_forest<br>#is_overworld & #is_taiga<br>terralith:cold_shrubland<br>terralith:highlands<br>terralith:hot_shrubland<br>terralith:rocky_shrubland<br>terralith:shield_clearing<br>terralith:steppe<br>terralith:valley_clearing<br>terralith:alpine_grove<br>terralith:birch_taiga<br>terralith:blooming_valley<br>terralith:lavender_forest<br>terralith:lavender_valley<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:highlands<br>terralith:shield_clearing<br>terralith:alpine_highlands<br>terralith:forested_highlands<br>terralith:lush_valley<br>terralith:shield<br>terralith:snowy_maple_forest<br>terralith:temperate_highlands<br>terralith:wintry_forest<br>terralith:yosemite_lowlands<br>terralith:skylands_autumn<br>terralith:skylands_spring<br>terralith:skylands_winter<br>terralith:mirage_isles<br>terralith:cloud_forest |
| Raccoon | #is_overworld & #is_forest<br>#is_overworld & !#is_savanna & #forge:is_plains<br>#is_overworld & #is_taiga<br>terralith:alpine_grove<br>terralith:birch_taiga<br>terralith:blooming_valley<br>terralith:lavender_forest<br>terralith:lavender_valley<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:cold_shrubland<br>terralith:highlands<br>terralith:hot_shrubland<br>terralith:rocky_shrubland<br>terralith:shield_clearing<br>terralith:steppe<br>terralith:valley_clearing<br>terralith:temperate_highlands<br>terralith:skylands_autumn<br>terralith:mirage_isles<br>biomesoplenty:redwood_forest |
| Gazelle | #is_savanna<br>terralith:arid_highlands<br>terralith:brushland<br>terralith:fractured_savanna<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:shrubland<br>terralith:red_oasis |
| Roadrunner | #is_badlands<br>#forge:is_dry/overworld & #forge:is_hot/overworld & #forge:is_sandy<br>terralith:ancient_sands<br>terralith:desert_canyon<br>terralith:desert_oasis<br>terralith:desert_spires<br>terralith:red_oasis<br>terralith:sandstone_valley<br>terralith:warped_mesa<br>terralith:white_mesa<br>terralith:red_oasis |
| Seagull | #is_overworld & #is_beach<br>terralith:basalt_cliffs<br>terralith:granite_cliffs<br>terralith:gravel_beach<br>terralith:white_cliffs<br>terralith:skylands_autumn<br>terralith:skylands_spring<br>biomesoplenty:dune_beach<br>stony_shore |
| Seal | #is_beach<br>#is_overworld & #is_ocean & #forge:is_cold/overworld<br>terralith:gravel_beach<br>biomesoplenty:dune_beach<br>stony_shore |
| Platypus | #is_overworld & #is_river & !#forge:is_cold/overworld<br>biomesoplenty:tundra_bog<br>terralith:warm_river |
| Hummingbird | flower_forest<br>sunflower_plains<br>#is_jungle<br>meadow<br>terralith:blooming_valley<br>terralith:lavender_forest<br>terralith:lavender_valley<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:amethyst_canyon<br>terralith:amethyst_rainforest<br>terralith:jungle_mountains<br>terralith:rocky_jungle<br>terralith:tropical_jungle<br>terralith:blooming_plateau<br>terralith:valley_clearing<br>terralith:orchid_swamp<br>terralith:skylands_autumn<br>terralith:skylands_spring<br>terralith:skylands_summer |
| Sunbird | #is_overworld & #is_mountain<br>snowy_slopes<br>frozen_peaks<br>jagged_peaks<br>terralith:blooming_valley<br>terralith:emerald_peaks<br>terralith:painted_mountains<br>terralith:rocky_mountains<br>terralith:scarlet_mountains<br>terralith:stony_spires<br>terralith:volcanic_crater<br>terralith:volcanic_peaks<br>terralith:skylands_autumn<br>terralith:skylands_spring<br>terralith:skylands_summer<br>terralith:skylands_winter<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:yellowstone<br>terralith:yosemite_cliffs<br>terralith:bryce_canyon<br>terralith:jungle_mountains<br>terralith:haze_mountain<br>terralith:windswept_spires<br>terralith:desert_spires<br>terralith:desert_canyon<br>terralith:basalt_cliffs<br>terralith:granite_cliffs<br>terralith:white_cliffs |
| Banana Slug | old_growth_pine_taiga<br>old_growth_spruce_taiga<br>#is_taiga & #forge:is_dense/overworld & #forge:is_rare<br>terralith:forested_highlands<br>terralith:shield<br>terralith:skylands_autumn<br>terralith:yosemite_lowlands<br>biomesoplenty:redwood_forest<br>biomesoplenty:coniferous_forest<br>biomesoplenty:fir_clearing<br>autumnity:maple_forest |
| Maned Wolf | #is_overworld & #is_savanna<br>terralith:arid_highlands<br>terralith:brushland<br>terralith:fractured_savanna<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:shrubland |
| Toucan | #is_overworld & #is_jungle & !bamboo_jungle<br>terralith:amethyst_canyon<br>terralith:amethyst_rainforest<br>terralith:jungle_mountains<br>terralith:rocky_jungle<br>terralith:tropical_jungle<br>terralith:skylands_summer |
| Potoo | dark_forest |
| Sugar Glider | birch_forest<br>old_growth_birch_forest<br>terralith:white_cliffs |
| Mungus | #is_overworld & #forge:is_mushroom & #forge:is_rare<br>terralith:mirage_isles |
| Skunk | #is_overworld & #is_forest & !#is_savanna & !#forge:is_cold/overworld & !sparse_jungle<br>terralith:birch_taiga<br>terralith:blooming_valley<br>terralith:mirage_isles<br>terralith:lavender_valley<br>terralith:lavender_forest<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:temperate_highlands |
| Bald Eagle | #is_overworld & #is_hill & #forge:is_coniferous<br>grove<br>windswept_forest<br>terralith:blooming_plateau<br>terralith:blooming_valley<br>terralith:bryce_canyon<br>terralith:skylands_autumn<br>terralith:skylands_spring<br>terralith:skylands_winter<br>terralith:lavender_forest<br>terralith:lavender_valley<br>terralith:moonlight_valley<br>terralith:moonlight_grove<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:haze_mountain<br>terralith:temperate_highlands<br>terralith:alpine_grove |
| Emu | #is_overworld & #is_badlands<br>#is_overworld & #is_savanna<br>terralith:arid_highlands<br>terralith:brushland<br>terralith:fractured_savanna<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:warped_mesa<br>terralith:white_mesa<br>terralith:red_oasis<br>biomesoplenty:lush_desert |
| Kangaroo | #is_overworld & #is_badlands<br>#is_overworld & #is_savanna<br>terralith:arid_highlands<br>terralith:brushland<br>terralith:fractured_savanna<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:warped_mesa<br>terralith:white_mesa<br>terralith:red_oasis<br>biomesoplenty:lush_desert |
| Elephant | #is_overworld & #is_savanna<br>terralith:arid_highlands<br>terralith:brushland<br>terralith:fractured_savanna<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:shrubland<br>terralith:red_oasis |
| Rhinoceros | #is_savanna<br>terralith:arid_highlands<br>terralith:brushland<br>terralith:fractured_savanna<br>terralith:savanna_badlands<br>terralith:savanna_slopes<br>terralith:shrubland<br>terralith:red_oasis |
| Bison | #is_overworld & #forge:is_plains & !#is_savanna & !#forge:is_hot/overworld<br>meadow<br>biomesoplenty:field<br>biomesoplenty:forested_field<br>biomesoplenty:grassland<br>biomesoplenty:pasture<br>biomesoplenty:prairie<br>terralith:cold_shrubland<br>terralith:rocky_shrubland<br>terralith:steppe<br>terralith:valley_clearing |
| Moose | #is_overworld & #forge:is_snowy & #forge:is_wasteland<br>#is_overworld & #forge:is_snowy & #is_taiga<br>biomesoplenty:snowy_coniferous_forest<br>biomesoplenty:snowy_fir_clearing<br>snowy_maple_woods<br>terralith:alpine_grove<br>terralith:snowy_badlands<br>terralith:snowy_maple_forest<br>terralith:snowy_shield<br>terralith:wintry_forest<br>terralith:wintry_lowlands<br>terralith:gravel_desert<br>terralith:skylands_winter |
| Gelada Monkey | #is_overworld & #forge:is_plains & #forge:is_plateau<br>terralith:highlands<br>terralith:hot_shrubland<br>terralith:rocky_shrubland<br>terralith:steppe<br>terralith:valley_clearing |
| Capuchin Monkey | #is_jungle & !bamboo_jungle<br>mangrove_swamp<br>terralith:amethyst_canyon<br>terralith:amethyst_rainforest<br>terralith:jungle_mountains<br>terralith:rocky_jungle<br>terralith:tropical_jungle<br>terralith:skylands_summer |
| Gorilla | #is_jungle & !bamboo_jungle<br>terralith:amethyst_canyon<br>terralith:amethyst_rainforest<br>terralith:jungle_mountains<br>terralith:rocky_jungle<br>terralith:tropical_jungle<br>terralith:skylands_summer |
| Anteater | #is_overworld & #is_jungle & !bamboo_jungle<br>terralith:amethyst_canyon<br>terralith:amethyst_rainforest<br>terralith:jungle_mountains<br>terralith:rocky_jungle<br>terralith:tropical_jungle<br>terralith:skylands_summer |
| Shoebill | #is_overworld & #forge:is_swamp & !mangrove_swamp<br>terralith:orchid_swamp<br>terralith:red_oasis |
| Mudskipper | mangrove_swamp<br>terralith:cave/underground_jungle |
| Tusklin | ice_spikes<br>#is_overworld & #forge:is_snowy & #forge:is_plains<br>terralith:snowy_badlands<br>terralith:gravel_desert |
| Tasmanian Devil | #is_overworld & #is_forest & !#is_savanna & !#forge:is_cold/overworld & !sparse_jungle<br>terralith:birch_taiga<br>terralith:blooming_valley<br>terralith:mirage_isles<br>terralith:lavender_valley<br>terralith:lavender_forest<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:temperate_highlands |
| Tarantula Hawk | #forge:is_dry/overworld & #forge:is_hot/overworld & #forge:is_sandy & !#is_badlands<br>terralith:ancient_sands<br>terralith:desert_canyon<br>terralith:desert_oasis<br>terralith:desert_spires<br>terralith:sandstone_valley<br>terralith:red_oasis |
| Bunfungus | #is_overworld & #forge:is_mushroom & #forge:is_rare<br>terralith:mirage_isles |
| Grizzly Bear | #is_overworld & #is_forest & !sparse_jungle<br>#is_overworld & #is_taiga<br>terralith:alpine_grove<br>terralith:blooming_valley<br>terralith:lavender_forest<br>terralith:lavender_valley<br>terralith:moonlight_grove<br>terralith:moonlight_valley<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:highlands<br>terralith:shield_clearing<br>terralith:valley_clearing<br>terralith:alpine_highlands<br>terralith:cloud_forest<br>terralith:forested_highlands<br>terralith:lush_valley<br>terralith:shield<br>terralith:snowy_maple_forest<br>terralith:snowy_shield<br>terralith:temperate_highlands<br>terralith:wintry_forest<br>terralith:yosemite_lowlands<br>terralith:bryce_canyon<br>terralith:skylands_autumn<br>biomesoplenty:redwood_forest |
| Snow Leopard | #is_overworld & #forge:is_snowy<br>snowy_slopes<br>frozen_peaks<br>jagged_peaks<br>terralith:frozen_cliffs<br>terralith:glacial_chasm<br>terralith:snowy_badlands & terralith:snowy_maple_forest<br>terralith:emerald_peaks<br>terralith:rocky_mountains<br>terralith:scarlet_mountains<br>terralith:snowy_shield<br>terralith:skylands_winter |
| Tiger | bamboo_jungle<br>biomesoplenty:bamboo_grove<br>biomesoplenty:cherry_blossom_grove<br>terralith:sakura_grove<br>terralith:sakura_valley<br>terralith:amethyst_canyon<br>terralith:amethyst_rainforest<br>terralith:skylands_spring |
| Komodo Dragon | #is_jungle & !#forge:is_dense/overworld<br>terralith:sandstone_valley<br>terralith:red_oasis<br>terralith:skylands_summer<br>biomesoplenty:tropics |
| Crocodile | #forge:is_swamp<br>mangrove_swamp<br>#is_river & !#forge:is_cold<br>biomesoplenty:tropic_beach<br>terralith:orchid_swamp<br>terralith:red_oasis<br>terralith:warm_river |
| Alligator Snapping Turtle | #is_overworld & #forge:is_swamp & !mangrove_swamp<br>biomesoplenty:tundra_bog<br>terralith:ice_marsh<br>terralith:orchid_swamp |
| Anaconda | #is_overworld & #forge:is_swamp<br>mangrove_swamp<br>terralith:ice_marsh<br>terralith:orchid_swamp<br>terralith:amethyst_rainforest<br>terralith:tropical_jungle<br>terralith:skylands_summer |
| Rattlesnake | #is_badlands<br>#forge:is_dry/overworld & #forge:is_hot/overworld & #forge:is_sandy<br>terralith:ancient_sands<br>terralith:desert_canyon<br>terralith:desert_oasis<br>terralith:desert_spires<br>terralith:red_oasis<br>terralith:sandstone_valley<br>terralith:warped_mesa<br>terralith:white_mesa<br>terralith:red_oasis |
| Froststalker | ice_spikes<br>frozen_peaks<br>terralith:cave/frostfire_caves<br>terralith:frozen_cliffs<br>terralith:glacial_chasm<br>terralith:snowy_badlands<br>terralith:gravel_desert |

