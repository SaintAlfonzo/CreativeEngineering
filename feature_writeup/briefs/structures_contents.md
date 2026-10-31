# Brief: structures / contents

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# structures`, Sub `contents`. Feature writeups:
[structure_tuning.md](../structure_tuning.md) (section 7 Todo "Structure contents", prefix `ST-`),
[nether_worldgen.md](../nether_worldgen.md) (S1, S2, S5, S28), [mob_spawns.md](../mob_spawns.md) 9 ("Later: pillager
overhaul"). Java paths are relative to `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`.

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| #3 pillager spawners | Built 2026-10-30 | as built: [structure_tuning.md](../structure_tuning.md) 3.32 (data-driven boxes in `spawn_boxes.json` instead of JSON copies; meta parts per (a)); scratch SQ-SB | done |
| #2 river tree and driftwood cleanup | Planned | worldgen override (data) + item removal (KubeJS) | **second**, independent; needs one decision on what "remove driftwood" means |
| #1 soul dungeons to the Nether | Planned | biome fit, tags, two structure json overrides, group moves | **last**: do it inside the Nether grouping pass (`structures/rates-and-groups #1`), as the row says |

No row depends on another. #1 touches `structure_tags.json`, `biome_fits.json` and `structure_groups.json`, which
the grouping pass edits too: one session, one `gen_structure_groups.py` run. Proposed test ids below are ST-290 to
ST-296; renumber at build time if another brief took them first.

---

## structures/contents #3: Pillager spawners in structures

Status Testing (built 2026-10-30) (as built: [structure_tuning.md](../structure_tuning.md) 3.32; the Build and Decisions below are
the plan, superseded where 3.32 differs: one data file instead of structure JSON copies, mixes by class, no End tower
box, box evokers drop no totem) · Work patcherMod / data · TODO `worldgen / structures/contents #3` · revised 2026-10-30 (verified
vanilla mechanics, full audit, owner decision)

### Goal

No pillager mob spawners in structures (switchable). **Every pillager structure gets a vanilla-style spawn box**
(owner, 2026-10-30), with a mob pool per structure type that `mobs/pillagers #1` tunes. This row builds the filter and
the boxes with pillagers only; #1 then fills in the pools.

### How vanilla pillagers spawn at outposts (verified 2026-10-30)

Read from Forge 1.19.2-43.3.9 (the Mojmap jar in `~/.gradle/caches/forge_gradle/minecraft_user_repo`, decompiled with
the ForgeFlower jar in the same cache): `Structures`, `ChunkGenerator.getMobsAt`, `NaturalSpawner`,
`StructureSpawnOverride`, `SpawnPlacements`, `PatrollingMonster`, `BaseSpawner`.

- **The outpost has no spawner and no saved pillagers.** `Structures.PILLAGER_OUTPOST` is defined in code (1.19.2 has
  no vanilla structure JSON) with one spawn override: category `monster`, box type `STRUCTURE` (JSON name `full`),
  one entry `pillager` weight 1, count 1 to 1. Its templates (vanilla `client.jar`) hold only the caged iron golem and
  the 2 allays. Every outpost pillager comes from that override.
- **How a box works.** Each natural spawn attempt (`NaturalSpawner.spawnCategoryForPosition`) picks a random column in a
  loaded chunk and a random height from the world bottom up to the surface top (`getRandomPosWithin`,
  `WORLD_SURFACE` + 1), then tries a group of up to 4 mobs within 5 blocks of it. The mob type is picked once per
  group, at the group's first valid position, by `ChunkGenerator.getMobsAt`: it walks the structures referenced at that
  position, and the first one that has an override for the category and contains the position (`full`: the start's
  whole bounding box; `piece`: inside one of its pieces) supplies its list **instead of** the biome's. Every later mob
  of the group is checked again (`canSpawnMobAt`), so a group stops at the box edge.
- **What follows:**
  - A box replaces the biome's monsters: no zombies or creepers inside it. An override with an empty list
    (`"spawns": []`) makes a no-monster zone (Philip's Ruins and Sky Villages use this).
  - A box changes what spawns, not how often: the monster cap, the 24-block minimum distance from players and from
    world spawn, and despawning at 128 blocks all apply as usual. Box mobs count toward the monster cap.
  - `full` covers the whole bounding box, including the ground and caves under the structure and the air around it;
    `piece` keeps to the pieces. Attempts land at random heights, so a small, tall box catches few of them.
  - Each mob still needs its own spawn rule. Pillager (`PatrollingMonster.checkPatrollingMonsterSpawnRules`): block
    light 8 or less, any sky light, so they spawn by day. Vindicator, evoker, illusioner, witch, ravager
    (`Monster.checkMonsterSpawnRules`): dark only. **A mixed pool spawns only its pillagers in daylight.**
  - 6% of pillagers (and other illagers) from a box or from a spawner roll captain, with an ominous banner
    (`PatrollingMonster.finalizeSpawn` skips the roll only for patrol, event and structure spawns). Killing one gives
    Bad Omen. Mobs saved in templates never roll.
  - Box mobs are not patrolling, so they despawn like any monster (`removeWhenFarAway`).
- Besides overrides, only the Nether fortress has a hard-coded spawn path (`isInNetherFortressBounds`, on nether
  bricks). Forge's `LevelEvent.PotentialSpawns` fires on the final list; patcherMod does not use it today. Patrols
  (`PatrolSpawner`) and raids are separate, not tied to structures. The pack's spawn rules and conditions
  (`mob_spawns.json`, `SpawnConditions`) have no illager entries and do not touch overrides.

### Spawner blocks in this pack

Vanilla `BaseSpawner`: activates within 16 blocks of a player, stops at 6 mobs of its type nearby, and the mob's own
spawn rule applies (pillager: block light 8 or less). **Survivality** changes every spawner in the pack
(`config/survivality-common.toml`): `spawner_required_player_range = 32`, and `unrestricted_spawners = true` ("Spawners
will spawn regardless of how many mobs are nearby"). So a pillager spawner keeps adding pillagers, without a cap,
while a player is within 32 blocks. A box is held by the monster cap. Survivality's `variant_spawners` (its
`MonsterRoomFeatureMixin`) also gives vanilla dungeon spawners biome variants of their mobs.

### Pillager structures today (scan 2026-10-30)

Scan of every structure JSON, template pool and template in `mods/` (with jar-in-jar) and `kubejs/data` (which wins
on the same path). Structures are mapped to templates by walking their jigsaw pools. Script:
`python3 tools/audit_structure_spawners.py [out.json]` (about 80 s; 401 structures, 1,284 pools, 4,832 templates).
Mob counts are per template; T&T and IPO fill jigsaw slots from mob pools (per-slot counts in
[mobs_pillagers.md](mobs_pillagers.md), "The pillager structures today").

| Structure | Spawn box | Pillager spawners | Mobs saved in templates or placed by code | In a pillager meta as |
|---|---|---:|---|---|
| Vanilla `minecraft:pillager_outpost` (+ IPO tower variant, 1 in 8) | **yes**: `full`, pillager 1-1 (code) | 0 | none (IPO: `ipo:pillager_outpost/entities`, a vindicator per slot) | anchor, `pillager_tower_siege` (35%) |
| T&T outposts, 27 (`towns_and_towers:pillager_outpost_*`, `exclusives/pillager_outpost_*`) | **yes**: `full`, pillager 1-2 | 0 | grunt pool (pillager 1 : vindicator 1), captain pool | anchors: `pillager_tower_siege` 35% (14 towers), `pillager_stronghold_siege` 40% (forts), camp metas 20 + 15 + 15% (beach, snowy beach, sparse jungle, wooded badlands), `pillager_fleet` 35% (ocean ship) |
| T&T `pillager_outpost_mushroom_fields` | **no** (the only T&T outpost without one) | 0 | grunt and captain pools | anchor, `pillager_stronghold_siege` |
| Mysticrift towers, 7 Overworld (`mysticrift_pillagers_towers_aris:*`) | no | 1 each | 5-8 pillagers | anchor, `pillager_great_tower_siege` (50%) |
| Mysticrift End tower (`end_pillager_tower`) | no | 3 (+ 2 spawners with no mob, which spawn nothing) | none | none (metas are Overworld only) |
| Illager Siege Weapons x6 (`illager_siege_weapons:*`) | no | 1 each, siege tower 2 (7) | vindicators 2-7; siege tower also 6 pillagers | parts (`meta/pillager_outworks`) |
| `illagerwarship:warship` | no | 1 | 15 pillagers, 3 vindicators, 2 illusioners | anchor or part, `pillager_fleet` |
| `illager_viking_longship:illager_longship` | no | 2 | 3 pillagers, 5 vindicators, 1 evoker | anchor or part, `pillager_fleet` |
| `blocky_siege:pillager_ship` (jar template) | no | 2 | none | anchor or part, `pillager_fleet` |
| `eugene:pillager_camp` | no | 0 | 1 pillager, 1 vindicator | anchor or part, camp metas |
| `eugene:pillager_loot_carriage` (3 variants) | no | 0 | 11 pillagers, 2 ravagers across the variants | none |
| `takesapillage:pillager_camp`, `takesapillage:bastille` | no | 0 | placed features `takesapillage:mob_feature_*` (code) | camp metas; bastille anchors `pillager_stronghold_siege` |
| `unwrecked_ships:ship`, `nether_ship` | no | 0 | pillager crew pools (`unwrecked_ships:sailors/pillager`, `pillager_captain`) | none |

22 pillager spawners in 17 templates, unchanged since 2026-10-21. All are `minecraft:spawner`. Every template except
Blocky Siege's is a port in `kubejs/data/<ns>/structures/`. No vindicator, evoker or illusioner spawners exist, and no
processor list or mod code places a pillager spawner.

**Metas drop the anchor's box.** A meta is one structure: its anchor and parts are all pieces of the meta's start
([MetaStructure.java:62-64](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/MetaStructure.java#L62-L64)),
and `getMobsAt` looks up overrides by structure. Every meta in `meta_structures.json` has `"spawn_overrides": {}`. So
a vanilla or T&T outpost that became a meta anchor (35-50% of their slots, see the table) has **no pillager respawn
today**, and a box added to a member's own JSON would not reach it inside a meta either. The same applies to Philip's
Ruins inside the ruin metas: their zombie/skeleton boxes and no-monster boxes are lost there.

### Other spawner blocks (audit 2026-10-30)

Templates (by mod; counts are spawner blocks per mob, summed over templates):

| Mod | Structures | Mobs |
|---|---|---|
| YUNG's Better Dungeons | skeleton, small, small nether, zombie dungeons | the templates hold `pig` placeholders; its processors (`betterdungeons:*` processor lists, `MobSpawnerProcessor` and friends) set the real mob: skeleton, zombie, spider, blaze, wither skeleton, zombified piglin. 1 real zombie spawner in a zombie dungeon cubby |
| YUNG's Better Strongholds | stronghold rooms | zombie 8, spider 6, silverfish 2 (portal room, junction), skeleton 1; the grand library and large prison also set spawners by processor |
| YUNG's Better Nether Fortresses | `betterfortresses:blaze/blaze_0` | blaze 1 |
| Philip's Ruins | about 45 templates in ruins, crypts, pyramids, ocean and Nether ruins | zombie, husk, skeleton, stray, wither skeleton, cave spider, witch, creeper, drowned, guardian, magma cube, zoglin, ghast, endermite, silverfish |
| Quark big dungeon | 20 templates | zombie, skeleton |
| Quark Nether obsidian spikes | placed feature (code), `Big Spike Spawners = true` | blaze (Quark default; not read from code) |
| Terralith | `spire` bases, `cave/dungeon1`, `underground/dungeon1` (frosted dungeon), desert village cartographer | zombie, stray, silverfish, drowned, cave spider, skeleton |
| C.C.C. (eugene) | `plains_undead_castle` (castle, undead castle) | skeleton 3 |
| Hearths | crimson and warped tower interiors | blaze 1 each |
| Towns & Towers | desert temple mimic passage | husk 1 |
| MES | `mes:phantom_citadel` | enderman 2, phantom 2 |
| Nullscape | `phantom_nest` hives | phantom |
| Underground Bunkers | bunker spawner rooms | zombie 3 |
| Cloud Storage | sky temple (code piece) | bloviator, badloon |
| Vanilla (code) | mineshaft corridors, stronghold portal room, fortress blaze throne, dungeon feature (`monster_room`) | cave spider, silverfish, blaze, zombie/skeleton/spider |

Whether the vanilla stronghold and fortress still generate next to YUNG's was not checked here (YUNG replaces them in
code). Quark's Spawner Replacer module is off. Botania, PneumaticCraft, Create and WTHIT only read or move spawners.

### Build

1. **Config** (`CEConfig.java`, `WorldgenSystems`, next to `templateBlockSwaps`): under `[worldgen.systems]` (or a new
   `[worldgen.spawners]` section):
   - `removeSpawners` list of entity ids, default `["minecraft:pillager"]` (an empty list keeps every spawner);
   - `spawnerReplacement` block id, default `"minecraft:air"`.
   Accessors in `worldgen/WorldgenConfig.java` next to `templateBlockSwapsEnabled`. Comment: "rejoin; new chunks only".
2. **Filter** in `worldgen/TemplateBlockSwaps.apply` (runs once per loaded template from
   `mixin/StructureTemplateManagerSwapMixin.java`), before its "no swaps listed" early return, so it also covers jar
   templates (Blocky Siege): for each palette, each `StructureBlockInfo` whose state is `Blocks.SPAWNER` and whose
   `nbt` has `SpawnData.entity.id` (or any `SpawnPotentials[].data.entity.id`) in the list is replaced with the
   replacement state and null NBT. Also drop the End tower's 2 empty spawners (no `SpawnData.entity.id`) when the
   template loses a listed one. One info line on first world load (`[template-swaps] N pillager spawner(s) removed in
   M template(s)`, 22 expected). Sketch:
   ```java
   static boolean filtered(StructureTemplate.StructureBlockInfo info, Set<String> ids) {
       if (!info.state.is(Blocks.SPAWNER) || info.nbt == null) return false;
       CompoundTag e = info.nbt.getCompound("SpawnData").getCompound("entity");
       if (ids.contains(e.getString("id"))) return true;
       ListTag pots = info.nbt.getList("SpawnPotentials", Tag.TAG_COMPOUND);
       for (int i = 0; i < pots.size(); i++)
           if (ids.contains(pots.getCompound(i).getCompound("data").getCompound("entity").getString("id"))) return true;
       return false;
   }
   ```
   Most spawners sit on a floor or a deck, so `air` is fine; look at each kind in ST-290.
3. **Boxes on every pillager structure** (owner decision). A pool per structure type, pillagers only for now:
   ```json
   "spawn_overrides": {"monster": {"bounding_box": "piece",
     "spawns": [{"type": "minecraft:pillager", "weight": 1, "minCount": 1, "maxCount": 2}]}}
   ```
   - JSON-defined structures, override files in `kubejs/data/<ns>/worldgen/structure/`: Mysticrift x8 (the End tower
     too, for `mobs/pillagers #2`), siege weapons x6, warship, longship (all already ported there), plus copies of
     the jar JSONs for `blocky_siege:pillager_ship`, `towns_and_towers:pillager_outpost_mushroom_fields`,
     `eugene:pillager_camp` and `pillager_loot_carriage` (ported, already in `kubejs/data/eugene`),
     `takesapillage:pillager_camp` / `bastille`, `unwrecked_ships:ship` (only if its pillager crew is wanted to
     respawn; it is a mixed ship). T&T's 27 boxes can stay as they are or get the tuned pool via jar JSON copies.
   - Vanilla `minecraft:pillager_outpost` is code-defined: its pool changes through a JSON override at
     `kubejs/data/minecraft/worldgen/structure/pillager_outpost.json` (a full copy of the vanilla structure, codec
     form), or through a Java hook (below).
   - `tools/port_structure_jars.py` rewrites the ported folders when re-run: give it a `SPAWN_OVERRIDES` table so a
     re-port keeps the boxes. Run `python3 tools/check_worldgen_json.py` after.
4. **Metas pass their members' boxes on.** Without this, 35-50% of outposts (and every siege weapon or camp that is
   a meta part) get no box. Two ways:
   - (a) A mixin on `ChunkGenerator.getMobsAt` (or a `LevelEvent.PotentialSpawns` handler): when the structure at the
     position is a `MetaStructure`, find the member whose pieces contain the position (the meta's `Index` piece records
     each member's piece range) and use that member's override with `piece` semantics. Members keep their own boxes,
     ruins included. Recommended.
   - (b) Give each pillager meta its own `piece` override in `meta_structures.json`. Simpler, but one pool for the whole
     meta (tower, camps and siege weapons alike), and the ruin metas stay wrong.
5. **Pools** come from `mobs/pillagers #1` (camps 0-2 vindicators, towers + 0-1 evoker / illusioner, ...). Never put
   evokers in a repeating box (a Totem of Undying every time, see [mobs_pillagers.md](mobs_pillagers.md) Gotchas).
6. Document in structure_tuning.md (a 3.x subsection "Spawners and spawn boxes") and in mob_spawns.md 9 (strike "remove
   pillager spawners" once built). patcherMod rebuild; ships with the next pack version (jar on server and clients;
   the config key is common). Player changelog bullet then (spawners gone, pillagers respawn in all pillager
   structures).

### Ids and data

- Spawner block `minecraft:spawner`; NBT path `SpawnData.entity.id` (1.19 format), plus `SpawnPotentials`.
- Override JSON: `spawn_overrides.<category>.bounding_box` (`piece` | `full`) and `spawns` entries with `type`,
  `weight`, `minCount`, `maxCount` (as in T&T's outpost JSONs and Philip's Ruins).

### Decisions

- **Remove and box everything** (owner, 2026-10-30): spawners out by default, every pillager structure gets a box.
  This replaces the 2026-10-21 recommendation (boxes only on the siege weapons and Mysticrift towers).
- **Open: meta handling**, (a) per-member mixin (recommended) or (b) one box per meta.
- **Open: box type.** Recommendation `piece` everywhere except vanilla and T&T outposts (keep `full`, as they are):
  `piece` keeps ships' boxes to the hull, not the sea around, and keeps camps' boxes to the tents.
- **Open: ships and the warship.** A box on a ship keeps spawning pillagers on the deck forever (what the spawners
  did, now held by the monster cap). Fine for the "garrison" feel; say so if ships should stay one-off.
- **Open: daylight.** Pillagers spawn by day, the rest of a mixed pool only in the dark. If tuned pools should give
  vindicators by day, that needs a spawn-rule change for illagers inside boxes (a `SpawnConditions` kind), not just
  weights.

### Gotchas

- A box replaces the whole monster list inside it (no zombies or creepers in a pillager structure, as in vanilla).
- Box mobs count toward the monster cap: with several boxed structures loaded, fewer other monsters spawn nearby.
- 6% of box pillagers are captains: more boxes mean more Bad Omen sources (spawners had the same roll).
- Structures already generated keep their spawners (templates are swapped at load, new chunks only). Their missing
  boxes, though, apply at once in old chunks too: overrides are read from the structure JSON when mobs spawn, not when
  the chunk was made (the meta mixin too).
- Do not filter in the port tool alone: it misses jar templates (Blocky Siege) and mod updates.

### Tests

- **ST-290 Pillager spawners gone:** new world; `/ceplace` (or `/locate`) a catapult, a Mysticrift oak tower and the
  Blocky Siege ship -> no spawner where the template had one; log line with the removed count (22 if every template
  loads). `removeSpawners = []` brings them back in new chunks.
- **ST-291 Pillager spawn box:** stand near a boxed Mysticrift tower at night and by day for a few minutes on Normal
  -> pillagers spawn inside the box, no zombies inside it.
- **ST-296 Box inside a meta:** `/locate` a `meta/pillager_tower_siege`; stand by the anchor tower and by one of its
  siege weapons -> pillagers keep spawning at both (fails today: no pillager respawn at a meta anchor).

---

## structures/contents #2: River tree and driftwood cleanup

Status In Progress (river half done under `recipes/wood #3`) · Work data / KubeJS · TODO `worldgen / structures/contents #2`

### Goal

Fewer redundant woods: Upgrade Aquatic's river trees grow oak logs; the river wood set is gone from play; driftwood
crafts into oak planks and its own wood set is gone. (structure_tuning.md 7 Todo: "swap river tree to oak log, remove
river log/wood, swap driftwood craft to oak planks, remove driftwood/logs".)

### Now

- **River tree** (verified by decompiling `upgrade_aquatic-1.19.2-5.1.0.jar`): biome modifier
  `upgrade_aquatic:add_feature/river_tree` adds placed feature `upgrade_aquatic:trees_river` to `#minecraft:is_river`
  (vegetal decoration). Placed and configured features are registered in Java (`UAFeatures$UAPlacedFeatures`,
  `UAFeatures$UAConfiguredFeatures.RIVER_TREE`): feature type `upgrade_aquatic:river_tree` (`RiverTreeFeature`, codec
  `TreeConfiguration.CODEC`), config `UAFeatures.Configs.RIVER_TREE_CONFIG` = trunk `river_log`, `straight_trunk_placer`
  (3, 1, 1), foliage `river_leaves`, `blob_foliage_placer` (0, 0, 0), `two_layers_feature_size` (0, 0, 0), ignore vines,
  force dirt. The feature reads the trunk from the config (`config.trunkPlacer.placeTrunk`), so a json override of the
  configured feature changes the log; leaves and the mulberry vines it hangs stay.
- **River sapling** grows through `RiverTreeGrower`, which returns `UAConfiguredFeatures.RIVER_TREE.getHolder()`: the
  built-in (Java) holder, not the datapack registry entry. A json override does **not** reach player-grown trees
  (1.19.2 datapack worldgen registries are separate from the built-in ones). The pack sells river saplings
  (`kubejs/server_scripts/economy/wandering_trades.js:28`) and composts them (`kubejs/startup_scripts/compost/compost_values.js:94`).
- **Driftwood** generates only as Upgrade Aquatic's `upgrade_aquatic:driftwood` feature (`DriftwoodFeature` places
  `driftwood_log` with its axis), through the pack's own placed features: `kubejs/data/upgrade_aquatic/worldgen/placed_feature/driftwood_{beach,extra,ocean,river,swamp}.json`,
  `kubejs/data/creativeengineering/worldgen/placed_feature/driftwood_land/*.json` and
  `kubejs/data/creativeengineering/worldgen/placed_feature/meta/flotilla_driftwood.json` (the driftwood flotilla and
  shipwreck debris metas, `meta_structures.json:38-57`, `:174-197`). Driftwood logs are part of the pack's sea look
  and stay in the world in every reading of the row.
- **Recipes** (jar, verified): `upgrade_aquatic:driftwood_planks` and `upgrade_aquatic:river_planks` are shapeless,
  `#upgrade_aquatic:driftwood_logs` / `#upgrade_aquatic:river_logs` -> 4 planks. The log tags hold the log, wood and
  both stripped forms. The rest of each set (73 item models across the two): slab, stairs, vertical slab, vertical
  planks, fence, fence gate, door, trapdoor, button, pressure plate, sign, ladder, boards, bookshelf, beehive, chest,
  trapped chest, post, stripped post, boats (boat, chest boat, furnace boat, large boat), plus river hedge, leaf
  carpet / pile.
- **Compat sets**: Every Compat makes blocks for both woods (`config/everycomp-wood_types.toml:72-73`
  `[types.wood_type.upgrade_aquatic] driftwood = true, river = true`; leaves `:26-27`); Immersive Weathering bark
  `immersive_weathering:upgrade_aquatic/driftwood_bark` and `river_bark` (item_dict.js:12924-12925, composted in
  compost_values.js:507-508); Supplementaries hanging signs / sign posts (`stack_sizes.json:1031`).
- **Dict**: `kubejs/startup_scripts/_lib/item_dict.js` has only `river_leaf_carpet`, `river_leaves`, `river_sapling`
  (:13023-13025) and the barks. No log, wood or planks key for either wood. Wood species keys elsewhere follow
  `planks_<species>`, `log_<species>`, `wood_<species>`, `log_<species>_stripped`, `wood_<species>_stripped`
  (:200-457), and `server_scripts/wood/wood_types.js` lists species (neither wood is in it).
- **Removal convention**: `global.removedItems` (`kubejs/startup_scripts/_lib/removed_items.js`) hides from JEI
  (`client_scripts/jeichanges.js:5`), strips tags (`server_scripts/_lib/recipe_scan.js:51`) and logs the recipes that
  still mention an item; recipes are removed separately with `R.remove` / `R.bulk('remove', ...)`.

### Build

1. **Dict keys** (`item_dict.js`, in the wood block after the azalea / bamboo species, :452-460): `log_river`,
   `wood_river`, `log_river_stripped`, `wood_river_stripped`, `planks_river`, `log_driftwood`, `wood_driftwood`,
   `log_driftwood_stripped`, `wood_driftwood_stripped`, `planks_driftwood`, and the set pieces the removal list needs
   (`slab_river`, `stairs_river`, ... in the same `form_species` style). Ids are `upgrade_aquatic:<name>` as in the
   jar (`river_log`, `stripped_river_wood`, `river_planks`, `driftwood`, `stripped_driftwood_log`,
   `driftwood_planks`, `vertical_driftwood_planks`, `driftwood_vertical_slab`, ...).
2. **River tree to oak** (data): `kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json`:
   ```json
   {"type": "upgrade_aquatic:river_tree", "config": {
     "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
     "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:oak_log", "Properties": {"axis": "y"}}},
     "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 3, "height_rand_a": 1, "height_rand_b": 1},
     "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "upgrade_aquatic:river_leaves"}},
     "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": 0, "offset": 0, "height": 0},
     "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 0, "lower_size": 0, "upper_size": 0},
     "decorators": [], "ignore_vines": true, "force_dirt": true}}
   ```
   The placed feature `upgrade_aquatic:trees_river` (Java) references this id, so it picks the override up in new
   chunks. Run `python3 tools/check_worldgen_json.py`.
3. **Saplings** (the grower bypasses step 2): pick one (see Decisions). Recommended: river leaves drop
   `minecraft:oak_sapling` instead of the river sapling (override `kubejs/data/upgrade_aquatic/loot_tables/blocks/river_leaves.json`,
   the jar's table with the sapling entry renamed), `i.river_sapling` into `global.removedItems`, and out of
   `wandering_trades.js:28` and `compost_values.js:94`.
4. **Old river logs keep a use**: loot overrides so `river_log`, `river_wood` and the stripped forms drop the oak
   equivalents (`kubejs/data/upgrade_aquatic/loot_tables/blocks/<id>.json`), for river trees generated before the
   change. Same for the driftwood log if the user removes it as an item (Decisions).
5. **Recipes** (a new `kubejs/server_scripts/wood/upgrade_aquatic_woods.js`, IIFE, `var R = global.NCH, i = global.item`):
   - `R.replaceOutput({ output: i.planks_driftwood }, i.planks_driftwood, i.planks_oak, e)` and the same for
     `planks_river`: catches the jar's shapeless recipe and any sawmill / cutting compat recipe in one step (check in JEI
     that the count stays 4: unverified whether KubeJS 6's replaceOutput keeps the original count).
   - `R.bulk('remove', [...])` by output for every other set piece (`{ output: i.slab_river }` ...), and the boats.
   - River logs: with the tree now oak, river logs only come from old chunks; leave them craftable to oak planks
     through the replaced recipe.
6. **Removed items**: every set piece except `driftwood_log` (and, per decision, its stripped form) into
   `global.removedItems`, via the new dict keys. `recipe_scan.js` prints any recipe still naming them on the next
   reload: clear the log.
7. **Every Compat**: set `driftwood = false` and `river = false` in `config/everycomp-wood_types.toml:72-73` (and
   `river = false` under leaves, `:27`, if the river hedge goes too). This unregisters the compat blocks: a registry
   change, so server and clients must update together; any such block already placed in a world disappears.
8. Docs: a short "Upgrade Aquatic woods" note in structure_tuning.md 7 Done (or the wood writeup if one is made), and
   strike the Todo line.

### Ids and data

All `upgrade_aquatic:*` ids above verified in `upgrade_aquatic-1.19.2-5.1.0.jar` (blockstates, recipes, item tags
`upgrade_aquatic:driftwood_logs` and `river_logs`). Oak ids are dict keys already (`i.planks_oak` and the oak log keys,
item_dict.js:198-211, :380-385). The configured-feature JSON field names are vanilla 1.19.2 `TreeConfiguration` (not
checked against a jar here; `check_worldgen_json.py` and a scratch world catch a typo).

### Decisions

- **What "remove driftwood/logs" means.** The row says both "driftwood crafts to oak planks" and "remove driftwood
  logs". Recommendation: keep `driftwood_log` as the one driftwood item (it is what the beaches, flotillas and wreck
  debris place, and what breaking them gives), make it (and the stripped log) craft into oak planks, and remove the
  rest of the driftwood set (`driftwood` wood block, `stripped_driftwood`, planks and everything made from planks).
  The alternative, removing the log item too, needs a loot override so broken driftwood drops oak logs; then the
  "crafts to oak planks" part is moot. Ask the user.
- **River saplings.** Recommendation: retire them (step 3). Alternatives: a mixin on `RiverTreeGrower`
  (`m_213888_`) returning the datapack holder (`level.registryAccess()`), more work for little gain.
- **River leaves stay?** Recommendation: yes (with mulberry vines they are what makes a river tree a river tree).
- **Every Compat off for both woods?** Recommendation: yes, at a version bump where a registry change is expected
  anyway.

### Gotchas

- New chunks only for the tree; old river trees keep river logs (step 4 covers their drops).
- Immersive Weathering strips river / driftwood logs into bark items (`river_bark`, `driftwood_bark`); if the
  stripped forms go into `removedItems`, also remove the bark items and their compost entries.
- `removedItems` strips item tags: do not put `driftwood_log` in it if it should still match
  `#upgrade_aquatic:driftwood_logs` and `#minecraft:logs` (fuel, crafting).
- WTHIT's hive line (`compat/WthitHive.java:28`) mentions the driftwood and river beehives: harmless if they go.

### Tests

- **ST-292 River trees:** new world, a river -> trees with oak trunks, river leaves and mulberry vines.
- **ST-293 Woods out of play:** JEI shows no river or driftwood planks / slabs / boats; a driftwood log crafts into 4
  oak planks; breaking an old river log drops an oak log; the log has no `[jeiRemove]` line for these after `/reload`.

---

## structures/contents #1: Move soul dungeons to the Nether

Status Planned · Work data · TODO `worldgen / structures/contents #1`

### Goal

Philip's four Nether-built structures spawn in the Nether instead of the Overworld: the lost soul dungeon, the lost
soul city, the rare ruin and the blackstone Nether ruin.

### Now

- **Tag** `plan/move_to_nether` (`kubejs/generation_scripts/worldgen/structure_tags.json:1938-1946`): the four ids;
  only `gen_nether_swaps.py` reads it (`MOVE_TAG`, :32, :87), to exempt them from every swap rule.
- **Where they are now** (structure_groups.json / structure_tags.json):

  | Structure | Placement now | Tags now | Start (jar json) |
  |---|---|---|---|
  | `philipsruins:lost_soul_dungeon` | standalone set `philipsruins:underground_structures` (structure_groups.json:2282ff, with the bone dungeon and Philip's underground ruins) | `dungeon/underground_modded` | `absolute -27`, step `underground_decoration`, beard_thin; spawn override wither skeletons (piece, 1-5) |
  | `philipsruins:lost_soul_city` | own standalone set (:2267-2281, spacing 37, separation 30) | `type/ruin`, `type/underground`, `land/any/deep` (fit) | `absolute -40`, step `underground_structures`; spawn override wither skeletons (full, 1-2) |
  | `philipsruins:rare_ruin` | group `ruins` (:726) | `type/ruin`, `land/jungle`, `land/swamp` (fits), `meta/large_ruins` (anchor of `meta/ruin_cluster`, part of `meta/pillager_camp_ruin`) | `absolute -1` on `WORLD_SURFACE_WG` |
  | `philipsruins:start_nether_ruin` | group `ruins` (:734) | `type/ruin` | `absolute 0` on `WORLD_SURFACE_WG` |

- **Template sizes** (jar): lost soul city pieces 30-37 x **31** x 27-47 (5 pieces, size 7); lost soul dungeon start
  15 x 10 x 16, walkway 32 x 8 x 9; rare ruin 19 x 12 x 20; Nether ruin start 21 x 16 x 21, pieces 13-25 x 12-13.
- **How Philip's own Nether structures sit**: `philipsruins:nether_lava_ruins` and `start_nether_dungeon` use
  `"start_height": {"absolute": -95}` with `WORLD_SURFACE_WG`: in the Nether that heightmap is the bedrock roof (128),
  so they always start at y 33, just above the y 31 lava sea (verified in the jar). They are in group `nether_ruins`
  (structure_groups.json:1698ff, spacing 14 / separation 7, priority rank 60).
- **Biome fits** (`biome_fits.json`): a fit replaces a structure's own biome list; `land/end` shows a fit outside the
  Overworld with `inherit: false`; `ocean/deep` shows a fit with no parent fit. No Nether fit exists yet. Nether
  biomes in the pack: `minecraft:nether_wastes`, `crimson_forest`, `warped_forest`, `soul_sand_valley`,
  `basalt_deltas` (biome_config.json).
- **Swaps after the move**: the generator never swaps a template a Nether structure owns (`other_dim`), so dropping
  the tag changes nothing in `nether_block_swaps.json` (their templates are not shared with Overworld structures).

### Build (inside the Nether grouping pass)

1. **Biome fits** in `biome_fits.json`, `inherit: false`, plus same-named structure tags in `structure_tags.json`:
   - `nether/soul`: `minecraft:soul_sand_valley` -> lost soul dungeon, lost soul city;
   - `nether/forest`: `minecraft:crimson_forest`, `minecraft:warped_forest` -> rare ruin (twisting and weeping vines,
     crimson trapdoors);
   - `nether/wastes`: `minecraft:nether_wastes`, `minecraft:basalt_deltas` -> Nether ruin (blackstone, nether bricks).
2. **Tags out**: remove the four from `type/ruin`, `type/underground`, `dungeon/underground_modded`, `land/any/deep`,
   `land/jungle`, `land/swamp`, `meta/large_ruins` (update its note: the rare ruin was one of four anchors). **Tags in**:
   `type/nether_constructed` (all four). Then remove the `plan/move_to_nether` tag (and leave `MOVE_TAG` in the
   generator, harmless, or drop it).
3. **Groups**: rare ruin and Nether ruin into `group/nether_ruins` (structure tag values, then
   `python3 tools/gen_structure_groups.py`, which adds them with fitted rates); remove their entries from `ruins`.
   Lost soul dungeon: out of the `philipsruins:underground_structures` set (that set stays Overworld) into
   `group/nether_ruins` too, or a new Nether dungeon group if the grouping pass makes one. Lost soul city: keep its
   own set (it is big), or put it in a Nether group with a low weight; the grouping pass decides spacing.
4. **Start heights**: copy the jar jsons to `kubejs/data/philipsruins/worldgen/structure/<id>.json` (strip the
   comments; keep spawn overrides) and change only `start_height` / `project_start_to_heightmap`:
   - rare ruin and Nether ruin: `{"absolute": -95}` on `WORLD_SURFACE_WG` (y 33), Philip's own Nether convention;
   - lost soul dungeon: fixed `{"absolute": 12}`, no heightmap (buried in netherrack under the lava sea, clear of the
     bedrock floor at y 0-4);
   - lost soul city: fixed `{"absolute": 5}` (31 tall: y 5 to 36), no heightmap; see Decisions.
   The pack's `land/any/deep` note (structure_tags.json:1715) then loses the city: update it.
5. **Checks**: `python3 tools/check_worldgen_json.py`; `python3 tools/gen_nether_swaps.py` (swap count unchanged,
   1084 + whatever the netherblockswap rows add); `python3 tools/nether_worldgen_scan.py` (S1, S2, S5, S28 leave the
   Overworld table); `python3 tools/gen_structure_groups.py --check` (estimates); structure_tuning.md 7: move the Todo
   line to Done.

### Ids and data

Structure ids, pools, template sizes and the jar jsons verified in `PhilipsRuins1.19.2-10.8.jar`. The Nether's
`WORLD_SURFACE_WG` = roof behaviour is how Philip's existing Nether structures work (their -95 offset only makes
sense that way); confirm with `/locate` + F3 in game (ST-294).

### Decisions

- **Which Nether biomes.** Recommendation as in step 1 (soul sand valley for the soul builds; the forests for the vine
  ruin; wastes and deltas for the blackstone ruin). Simpler alternative: all five Nether biomes for all four.
- **The lost soul city's height.** It is 31 tall and was buried at y -40. In the Nether there is no depth that is both
  above the bedrock floor and below the lava sea for all 31 blocks. Options: y 5 (top at 36, may meet lava pockets),
  or y 33 on the "surface" like Philip's ruins (its walls stand in the open). Recommendation: y 5, then look at three
  cities in a scratch world; if lava floods them, move it to y 33.
- **Wither skeleton spawn overrides.** Both soul structures spawn wither skeletons in their box (the city in the whole
  box). Today that already happens in the Overworld; in the Nether it makes a second wither-skull source next to
  fortresses. Recommendation: keep (the city's box is underground and rare).
- **Lava under the y 33 ruins** (the piglin camp concern, structure_tuning.md 7.4.6): earth fill treats water, not
  lava. Recommendation: accept for now (Philip's own Nether ruins do the same); revisit with the grouping pass.

### Gotchas

- Moving the rare ruin removes one of the four `meta/large_ruins` anchors: the ruin cluster and the pillager camp
  ruin metas get rarer in the jungle and swamp. Re-fit (`gen_structure_groups.py`) and check
  `structure_group_estimates.md`.
- The lost soul city's chests (`philipsruins:chest/lost_soul_city_loot`, tuned in
  `kubejs/server_scripts/loot/loot_config.json`) move with it; loot tiers are per table, so nothing breaks, but the
  loot census (`tools/loot_census.py`) will list it under the Nether next run.
- Existing worlds: Overworld copies already generated stay; new Nether chunks get the new ones.

### Tests

- **ST-294 Soul builds in the Nether:** new world, in the Nether `/locate structure philipsruins:lost_soul_city`,
  `lost_soul_dungeon`, `rare_ruin`, `start_nether_ruin` -> each found, in the planned biomes, at the planned height,
  not flooded by lava; in the Overworld `/locate` finds none of them.
- **ST-295 Ruin metas without the rare ruin:** in the Overworld, `/locate structure creativeengineering:meta/ruin_cluster`
  -> found, anchored on a level-three ruin, ancient towers or forgotten church.

---

## Built, needs play

None: all three rows are Planned.

## Writeup corrections made

None for this subfamily.
