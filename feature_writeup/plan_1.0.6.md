# 1.0.6: everything since 1.0.4, with structure tuning shipped INCOMPLETE

DEPLOYED 2026-10-14 13:01 to the server and the clients (staged 12:52; snapshot in `deploy/DEPLOYED.json`). Followed by
[plan_1.0.7.md](plan_1.0.7.md), which shipped the structure fixes listed in 2.1. 1.0.6 is everything built after the 1.0.4 deploy
(2026-10-04 13:54, commit `334c76b`) up to the working tree on 2026-10-14. That means the undistributed 1.0.5 (never
copied out, see [plan_1.0.5.md](plan_1.0.5.md)) is folded in whole, plus the work done since its 2026-10-11 staging.
The payload is an update from 1.0.4, because 1.0.4 is still what both sides run.

**Structure tuning is INCOMPLETE in this release** (user, 2026-10-14: "ship all the changes as 1.0.6, just mark the
structure tuning as incomplete"). Plans 7.1 to 7.4 in [structure_tuning.md](structure_tuning.md) are built and ship in
the patcherMod 1.0.6 jar and the worldgen data, but their scratch-server run (structure_tuning 8.0) is only partly
done: Phases 1 to 4 and the 7.4 block (M2) have run, Phase 5 had just started when this was staged, and Phases 6 to 10
plus the client list have not run. The known open bugs are listed in section 2. Every part has a CEConfig switch
(section 2.2) if one misbehaves on the live server.

## 0. At a glance

Kind: feature, fix, tuning, data, config, mods, bookkeeping. Nothing below has been checked on the dedicated server
in this exact combination.

| ID | Change | Kind | State | Writeup |
|---|---|---|---|---|
| R5 | All of 1.0.5 (rows IA to VR in [plan_1.0.5.md](plan_1.0.5.md) section 0): Incubation Additions, More Mooblooms, stack sizes, generated cans, WTHIT growth line, IW soils, ghost-block fix, shields, placeable food, tooltip/JEI fixes, wild flowers, bee nests, mob spawns, season types, water fill, terrain adaptation, meta structures, tower biome fits, Cool Rain / Pretty Rain / MEED / Breedable Killer Rabbit | everything | STAGED; the 1.0.5 single-player boot (2026-10-11) is the last full-client evidence for these parts | [plan_1.0.5.md](plan_1.0.5.md) |
| SN | Seasons: snow line (`snowLineY` 320 with the build-height gauge), `snow_seasons` tags (`seasontags.js`), new **cold** type (22 biomes), permanent seasons for the four skylands, Pretty Rain / Rubidium Extra cloud height 350, `DhCloudHeightMixin` (Distant Horizons clouds follow it) | feature | STAGED in patcherMod 1.0.6; not run in game (WE-26 to WE-36) | [weather.md](weather.md) |
| CN | Can parity: 11 raw cans removed, White Strawberries can split out, Cactus Flesh rename, every remaining row built through `gen_can_types.py` (381 recipes), new can sprites (`assets/canned_goods`, `assets/creativeengineering`) | feature + data | STAGED; node dry run only, not run in game (CP-01 to CP-05) | [can_parity.md](can_parity.md), [food.md](food.md) |
| IP | Immersive Paintings: every painting variant of every pack as an Immersive Paintings datapack painting (475, 32 px; 950 files in `kubejs/data/creativeengineering/paintings`), `/dumppaintings` | feature | STAGED; scratch-verified, painting screen untested | none (tools/gen_immersive_paintings.py header) |
| NM | 23 new mods (section 3) | mods | STAGED; mod_sides rows complete 2026-10-14 | this file 3 |
| **ST** | **Structure tuning 7.1 to 7.4, INCOMPLETE**: biome fits for every land structure (7.2, 29 fits / 163 structures), replacements + witch huts group, entrance dungeons with surface check, folded standalone structures, vanilla temples as a share of YUNG's, way signs group, placement priority + companion villages + peaceful radius, earth fill 7.3.1 (beard-aware, mound columns, size limits, village piece pruning), locate 7.3.2 (dimension check, meta targets, 120 s timeout, `/celocate stop`), locate folding + non-blocking loads + thread-safe StructureCheck ([locate.md](locate.md)), template block swaps (meat + Nether blocks), ocean geodes, Camps. Castles. Carriages. 2.3.4 port with split camp/castle pools, Campsite Structures as real structures, `vehicles` group, fortified-town meta shares, new structure mods (Subterrestrial, Underground Bunkers), YUNG vanilla-structure switches off | worldgen | **INCOMPLETE**: built, offline checks pass, scratch Phases 1 to 4 + M2 run with open bugs (section 2.1); Phases 5 to 10 and client checks not run. New chunks only | [structure_tuning.md](structure_tuning.md) 3.17 to 3.22, 7.1 to 7.4, 8.0; [testing/structures/](testing/structures/00_plan.md) |
| CF | Config: `creativeengineering-common.toml` (new `[worldgen.ocean_geodes]`, `[worldgen.placement_priority]`, earth fill 7.3.1 keys, locate keys, `[seasons]` snow keys, systems switches `structureReplacements`, `oceanGeodes`, `placementPriority`, `templateBlockSwaps`, all on); YUNG desert/jungle temple and witch hut "disable vanilla" = false; `prettyrain.json5` cloud height; new `bottledair.json5`, `cyclepaintings.json5`, `dimpaintings-common.toml`, `unlitcampfire-server.toml` (defaultconfigs); JEI sort orders; rewritten-by-game files (`rare-ice.properties`, AP `world.toml`, WorldEdit) | config | STAGED | this file 1 |
| VR | Version records: patcherMod **1.0.6** (md5 5acd69dc, 2026-10-14 12:44 build, archived), other in-tree mods unchanged; 4 mod_sides rows; 3 `KUBEJS_CLASSES` rules (`meat_block_swaps.json`, `nether_block_swaps.json` runtime; `nether_swap_rules.json` dev); `pack/manifest.json` regenerated; patcherMod `mod_version` -> 1.0.7 for the next build | bookkeeping | DONE 2026-10-14 | [deploy.md](deploy.md), this file 4 |

## 1. What the payload contains

Update from 1.0.4: 1255 files, 23.2 MB (mods 28, client_mods 4, kubejs 1205, config 16, defaultconfigs 1,
resourcepacks 1). The full list is the 1.0.6 entry in [deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md).

- **mods (server + client):** `creativeengineering-1.0.6.jar` (replaces 1.0.4), `exposureBackport-1.9.19-ce-19.2-03.jar`
  (replaces `-02`), `incubationAdditions-1.0.0.jar`, `moreMooblooms-1.0.0.jar`, the 1.0.5 new mods (Breedable Killer
  Rabbit, Cool Rain, MEED, JEED now on the server) and the 20 both/server mods of section 3.
- **client_mods:** Pretty Rain, Just Enough Painting Previews, Lava Clear View, Neko's Enchanted Books.
- **kubejs:** mostly data: 950 painting files, the Camps. Castles. Carriages. 2.3.4 port (`data/eugene`), Campsite
  Structures structures, can sprites, the swap tables; plus every 1.0.5 script and data file.
- **resourcepacks:** `SquirmingSilverfish [1.13 - 26.3]   (v2).zip` (from 1.0.5; clients enable it themselves).
- **removals, both sides:** `mods/creativeengineering-1.0.4.jar`, `mods/exposureBackport-1.9.19-ce-19.2-02.jar`,
  `kubejs/startup_scripts/changes/item_properties.js`, `kubejs/generation_scripts/worldgen/biome_merges.json`,
  `kubejs/server_scripts/worldgen/biome_merges.js` (biome merges retired for biome fits).

## 2. Checks and open items

**Evidence:** preflight clean (structure_config regenerated equal, structure groups in sync, climate samples
readable, mod sides and kubejs classes complete); the in-tree jars equal their builds and no source is newer. The
shipped jar is a 12:44 rebuild of the same source (no patcherMod source changed after 01:28) as the build that ran on
scratch dedicated servers through structure Phases 3 and 4 (2026-10-14) without mixin or KubeJS errors. There has been **no single-player client boot with this jar set**, so client-only parts (SN clouds/HUD,
paintings screen, can sprites, Lava Clear View, JEPP) are unchecked.

Open:
- [x] Copy `deploy/latest/` to the server and clients (`verify.sh` first, it checks they run 1.0.4), apply the
  removals, then `python3 tools/build_deploy.py --snapshot 1.0.6`. Done 2026-10-14 13:01.
- [ ] Recommended before copying: one single-player boot with this jar set (not done), since no client has loaded
  1.0.6.
- [ ] Worldgen changes affect only chunks generated after the update on the server's existing world.
- [ ] Structure tuning test run continues (structure_tuning 8.0, Phase 5 onward); its first fixes shipped in 1.0.7.
- [ ] Older open items carried from [plan_1.0.5.md](plan_1.0.5.md) section 2: Cool Rain vs season types (WE-25),
  5 Terralith cave biomes without wild crops, stale [stack_sizes.md](stack_sizes.md), `kubejs/assets/*bloom.png` (since
  deleted, never shipped), Incubation Additions hay-nest texture license.
- [x] Commit: 111 shipped files uncommitted at staging (committed as `15c43a0`, "1.0.6 deployed").

### 2.1 Structure tuning: known open bugs and decisions (why it is INCOMPLETE)

From the scratch runs in [testing/structures/](testing/structures/00_plan.md) and structure_tuning 8.0:

| Where | Issue | Effect on the live server | Kind |
|---|---|---|---|
| Phase 3 | Ground match gives up silently over deep structures (ancient city, deep ruins, mineshafts cover the sample columns) | some grass pads stay on foreign ground, nothing logged | bug, fixed in 1.0.7 (DEPLOYED 2026-10-18) |
| Phase 3 | Beach, river and swamp driftwood water versions can never place (Upgrade Aquatic needs dirt/sand under the origin) | only ocean driftwood floats; land versions still place | bug (since 1.0.4), fixed in 1.0.7 (DEPLOYED 2026-10-18) (bank filter) |
| Phase 3 | Ocean geodes: the 10-block drop does nothing useful over deep floors near y 0 (ST-133 partly) | geodes behave much like before | tuning |
| Phase 2 | Cave biomes not in `land/any`: skeleton dungeon, bone dungeon, lost soul city never start (7.3.3 waits for the cave pass) | those 3 structures absent | bug, fixed in 1.0.7 (DEPLOYED 2026-10-18) (`land/any/deep`) |
| Phase 2 | ST-135 open: fortified metas rare against the 120 s locate timeout; bastille dropped by peaceful radius | `/locate` of them may time out | open |
| M2 | Undead castle cancelled 4 of 5 times by earth fill on slopes | very rare castle | decision (narrow fits or relax mound limit) |
| M2 | Earth fill counts only earth base blocks, not stone bases (drum tower, piglin camp) | a stone-based structure can overhang | bug, fixed in 1.0.7 (DEPLOYED 2026-10-18) (opt-in tag, drum tower) |
| M2 | `/celocate census` reports every slot "known" | census numbers meaningless (debug tool only) | bug, fixed in 1.0.7 (DEPLOYED 2026-10-18) |
| Phase 4 | ST-65 `templateTrim = false` half not run | none | test gap |
| not run | Phases 5 to 10: batch searches, pregenerated area, calibration, two-world comparisons, flat world, profiling; client list 8.0.11 | performance and rates unmeasured | test gap |

Found later (Phase 5, 2026-10-14), also fixed in 1.0.7 (DEPLOYED 2026-10-18): locate loads still blocked the server
thread (LO-05), Cloud Storage / Domestication Innovation village pieces dropped on reload, village layouts different
after each restart (pool order), `/celocate stop` row label. All of these shipped in patcherMod 1.0.7 (DEPLOYED
2026-10-18, [plan_1.0.7.md](plan_1.0.7.md) SF); [structure_tuning.md](structure_tuning.md) 3.23 and
[locate.md](locate.md) 8 list them.

### 2.2 Switches if a structure part misbehaves live

All in `config/creativeengineering-common.toml` `[worldgen.systems]`, all `true` as shipped; each one turns its part
off for newly generated chunks: `structureBiomeFits`, `structureReplacements`, `placementPriority`, `earthFill`,
`groundMatch`, `metaStructures`, `templateBlockSwaps`, `oceanGeodes`, `terrainAdaptation`, `templateTrim`,
`waterFill`, `structureBiomeRules`.

## 3. New mods and sides

Since 1.0.5 (rows in [mod_sides.md](../mod_sides.md); the first 19 were added 2026-10-13/14 by earlier sessions, the last
four 2026-10-14 for this release):

| Mod | Side | Note |
|---|---|---|
| Jess' Painting Collection, Dark Paintings, Dimensional Paintings, MC style paintings, Rebirth: Paintings, Too Many Paintings! | both | painting variants (Forge registry, synced) |
| Cycle Paintings | both | right-click cycling of `#minecraft:placeable` variants |
| Witch Mobility | both | brooms, witch armor (GeckoLib) |
| Firefly Bush (port), Fortune on Netherite, Splash Milk, Sticky It!, Bottled Air | both | blocks/items/recipes |
| Subterrestrial, Underground Bunkers | both | structure mods (part of ST, INCOMPLETE) |
| Volcanic Caverns | both | lava cavern carver + features |
| Just Enough Painting Previews, Lava Clear View, Neko's Enchanted Books | client | JEI add-on, fog render, book textures |
| Campsite Structures | both | camper entity; its features replaced by real structures (7.4) |
| restApi | both | library |
| Unlit Campfire | both | campfires placed unlit / burn out |
| Rain should extinguish campfires (`rsec`) | server | one campfire tick mixin on ServerLevel |

Side change: Breedable Killer Rabbit server -> both (made before this release; it moves into `mods/` on the client
too, where it already was).

## 4. Version records

- patcherMod **1.0.6** ships (md5 5acd69dc, built 2026-10-14 12:44 with the structure work). It overwrote the unshipped
  2026-10-11 snow-line build of 1.0.6 in `jar_archive/`; that build's changes are all in this one.
- exposureBackport `-03`, incubationAdditions 1.0.0, moreMooblooms 1.0.0: unchanged since 1.0.5.
- 1.0.5 stays in `deploy/history/` and the log as a version that was staged but never deployed.
- After staging: patcherMod `mod_version` = **1.0.7** in `development/patcherMod/gradle.properties` (next build, not
  built), so fixes from the ongoing structure test run never rebuild over the shipped 1.0.6 jar.
