# Brief: worldgen / structures/contents

Written 2026-10-21 from [todo/TODO.md](../todo/TODO.md) `# worldgen`, Sub `structures/contents`. Feature writeups:
[structure_tuning.md](../structure_tuning.md) (section 7 Todo "Structure contents", prefix `ST-`),
[nether_worldgen.md](../nether_worldgen.md) (S1, S2, S5, S28), [mob_spawns.md](../mob_spawns.md) 9 ("Later: pillager
overhaul"). Java paths are relative to `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`.

## Scope and order

| Row | Status | What is left | Build order |
|---|---|---|---|
| #3 pillager spawners | Planned | a spawner filter in patcherMod + optional pillager spawn boxes (data) | **first**: self-contained, small, and `mobs/pillagers #1` (the pillager overhaul) builds on it |
| #2 river tree and driftwood cleanup | Planned | worldgen override (data) + item removal (KubeJS) | **second**, independent; needs one decision on what "remove driftwood" means |
| #1 soul dungeons to the Nether | Planned | biome fit, tags, two structure json overrides, group moves | **last**: do it inside the Nether grouping pass (`worldgen/structures/rates-and-groups #1`), as the row says |

No row depends on another. #1 touches `structure_tags.json`, `biome_fits.json` and `structure_groups.json`, which
the grouping pass edits too: one session, one `gen_structure_groups.py` run. Proposed test ids below are ST-290 to
ST-295; renumber at build time if another brief took them first.

---

## structures/contents #3: Pillager spawners in structures

Status Planned · Work data / config · TODO `worldgen / structures/contents #3`

### Goal

No pillager mob spawners in structures (switchable), optionally replaced by a vanilla-style "pillager spawn box": the
structure's bounding box spawns pillagers naturally, like a pillager outpost.

### Now

- **Where pillager spawners are** (scanned for this brief: every `kubejs/data` template plus the Towns & Towers, IPO,
  Villages&Pillages, It Takes a Pillage, Philip's Ruins and Blocky Siege jars; script kept in the session scratchpad):
  22 spawners with `SpawnData.entity.id = minecraft:pillager` in 17 templates, all `minecraft:spawner`:

  | Structure | Template(s) | Spawners |
  |---|---|---:|
  | `illager_siege_weapons:*` (6, group `vehicles`) | `illager_siege_weapons:ballista`, `betteringram`, `catapult`, `reforcedbatteringram`, `trebuchet` 1 each, `siegetower` 2 | 7 |
  | `mysticrift_pillagers_towers_aris:*` (8 towers, `pillager_settlements`) | `*_pillager_tower_new` (acacia, birch, cherry, desert, mangrove, oak_wood) 1 each, `jungle_pillager_tower` 1, `end_new_pillager_tower` 3 (End) | 10 |
  | `illager_viking_longship:illager_longship` (boats) | `illager_viking_longship:longship_modded` | 2 |
  | `illagerwarship:warship` (boats) | `illagerwarship:illager-warship` | 1 |
  | `blocky_siege:pillager_ship` (boats) | `blocky_siege:pillager_pirate_ship` (in `blocky_siege-6.1.91-1.19.2.jar`) | 2 |

  Everything except Blocky Siege is a port in `kubejs/data/<ns>/structures/` (tools/port_structure_jars.py). No
  vindicator, evoker or illusioner spawners exist. Towns & Towers, IPO, Eugene's camps and vehicles use template
  **entities** instead (one-off mobs saved in the template, e.g. `kaisyn:outpost/pillagers/pillager`,
  `eugene:pillager_camp` 1 pillager + 1 vindicator, `illagerwarship:illager-warship` 15 pillagers, 3 vindicators, 2
  illusioners): those are not spawners and are out of scope.
- **How vanilla pillagers spawn** (1.19.2; from knowledge of the vanilla code, verify by reading the decompiled
  classes if it matters): (1) **outpost spawn override**: `minecraft:pillager_outpost` has a structure spawn override,
  monster category, bounding box `full` (STRUCTURE), only pillagers, 1 to 1, so natural monster spawning inside the
  outpost's box picks pillagers instead of the biome's monsters; pillager placement (`PatrollingMonster`
  spawn rules) allows block light up to 8 at any sky light, so they spawn by day; (2) **patrols**
  (`PatrolSpawner`, every 10-11 minutes after day 5, not in villages' vicinity); (3) **raids** (Bad Omen). Template
  entities are the mods' own way. The pack's spawn tuning (mob_spawns.md 4, "Only biome spawners") does not touch
  structure spawn overrides.
- **The pack's own structures have the field but empty**: every ported structure json has `"spawn_overrides": {}`
  (e.g. `kubejs/data/illager_siege_weapons/worldgen/structure/catapult.json`,
  `kubejs/data/mysticrift_pillagers_towers_aris/worldgen/structure/jungle_pillager_tower.json`); the meta structures
  in `meta_structures.json` (pillager camp siege, tower siege, fleet ...) have `"spawn_overrides": {}` too.
- **The hook to use**: `worldgen/TemplateBlockSwaps.apply` (:130-159) runs once per loaded template from
  `mixin/StructureTemplateManagerSwapMixin.java`. It returns early when the template has no listed swaps (:132), and it
  replaces a block with `new StructureBlockInfo(pos, state, null)` (:142), which drops the block entity (the
  spawner's NBT) as needed.

### Build

1. **Config** (`CEConfig.java`, `WorldgenSystems`, next to `templateBlockSwaps` :1714 / :1784): two keys under
   `[worldgen.systems]` (or a new `[worldgen.spawners]` section if more knobs follow):
   - `removeSpawners` list of entity ids, default `["minecraft:pillager"]` (an empty list keeps every spawner);
   - `spawnerReplacement` block id, default `"minecraft:air"`.
   Accessors in `worldgen/WorldgenConfig.java` next to `templateBlockSwapsEnabled` (:51). Comment: "rejoin; new
   chunks only".
2. **Filter** in `TemplateBlockSwaps.apply`, before the `list == null` return (so it works on templates with no
   swaps, including jar templates like Blocky Siege's): for each palette, for each `StructureBlockInfo` whose state is
   `Blocks.SPAWNER` and whose `nbt` has `SpawnData.entity.id` (or any `SpawnPotentials[].data.entity.id`) in the list,
   replace it with the replacement state and null NBT. Count and log once per template at debug, and a one-line total
   at info on the first world load (`[template-swaps] N pillager spawner(s) removed in M template(s)`). Keep the
   `DONE` guard so it runs once per template. Sketch:
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
   A spawner in a floor leaves a hole with `air`; most of these sit on a floor or a deck (siege weapons, tower tops),
   so air is fine; look at each kind in ST-290.
3. **Spawn boxes (optional, the "maybe" in the row):** for the structures that lose their spawner, set
   `spawn_overrides` in their structure json, outpost-style:
   ```json
   "spawn_overrides": {"monster": {"bounding_box": "full",
     "spawns": [{"type": "minecraft:pillager", "weight": 1, "minCount": 1, "maxCount": 1}]}}
   ```
   Files: `kubejs/data/illager_siege_weapons/worldgen/structure/{ballista,battering_ram,catapult,reinforced_battering_ram,siege_tower,trebuchet}.json`,
   `kubejs/data/mysticrift_pillagers_towers_aris/worldgen/structure/*_pillager_tower.json` (7 Overworld; the End one
   too if wanted), `kubejs/data/illager_viking_longship/worldgen/structure/illager_longship.json`,
   `kubejs/data/illagerwarship/worldgen/structure/warship.json`; Blocky Siege needs a copy of its jar json under
   `kubejs/data/blocky_siege/worldgen/structure/pillager_ship.json`. Run `python3 tools/check_worldgen_json.py` after.
   Note `tools/port_structure_jars.py` rewrites the ported folders when re-run: re-apply (or teach the port tool a
   `SPAWN_OVERRIDES` table) if a port is redone.
4. Document in structure_tuning.md (a 3.x subsection "Spawners") and in mob_spawns.md 9 (strike "remove pillager
   spawners" once built). patcherMod rebuild; ships with the next pack version (jar on server and clients; the config
   key is common).

### Ids and data

- Spawner block `minecraft:spawner`; NBT path `SpawnData.entity.id` (1.19 format), plus `SpawnPotentials`. Verified on
  the templates listed above.
- Pillager `minecraft:pillager` (vanilla). Spawn override JSON keys `bounding_box` (`piece` | `full`) and `spawns`
  with `type`, `weight`, `minCount`, `maxCount`: the same shape as Philip's lost soul dungeon json (verified in
  `PhilipsRuins1.19.2-10.8.jar`).

### Decisions

- **Remove or replace?** Recommendation: remove (the Java filter, default on for pillagers), and add spawn boxes only
  to the siege weapons and the Mysticrift towers, where a lone spawner was the only source of pillagers. The ships and
  the warship already carry many template pillagers; a box over open sea would keep spawning pillagers on the deck,
  which is what a spawner did, so leave them without a box unless the user wants a permanent garrison.
- **Spawn box size**: `full` (the whole structure box, like the outpost) vs `piece`. Recommendation: `full` for
  towers, `piece` for the siege weapons (their box with the meta parts can be large).
- **Interaction with `mobs/pillagers #1`** (the overhaul's "structure pool / spawn swap" per structure type): the box
  is where its per-type mix (camps 0-2 vindicators, towers + evoker...) would go later, as weighted `spawns` entries.
  Build #3 first with pillagers only.

### Gotchas

- Spawn overrides replace the whole monster list inside the box: no zombies or creepers in a pillager tower, as in
  vanilla outposts.
- Pillagers from a box count toward the monster mob cap; with several boxed structures loaded, fewer other monsters
  spawn nearby.
- Structures already generated keep their spawners (templates are swapped at load, new chunks only).
- Do not filter in the port tool alone: it misses jar templates (Blocky Siege) and a mod update.

### Tests

- **ST-290 Pillager spawners gone:** new world; `/ceplace` (or `/locate`) a catapult, a Mysticrift oak tower and the
  Blocky Siege ship -> no spawner where the template had one; log line with the removed count (22 if every template
  loads). `removeSpawners = []` brings them back in new chunks.
- **ST-291 Pillager spawn box:** stand near a boxed tower at night and by day for a few minutes on Normal -> pillagers
  spawn inside the box, no zombies inside it.

---

## structures/contents #2: River tree and driftwood cleanup

Status Planned · Work data / KubeJS · TODO `worldgen / structures/contents #2`

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
