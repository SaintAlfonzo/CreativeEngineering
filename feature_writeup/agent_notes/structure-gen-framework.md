# structure-gen-framework

> History/gotchas of the structure framework (scanner, vanilla table, Terralith/Nullscape overrides, runtime highPriorityData applier). SUPERSEDED 2026-09-27 by structure groups: multipliers/current/splits are gone, see structure-groups

Moved from agent memory 2026-11-16 (last edited there 2026-09-25). Repo status docs win over any status line here.

**2026-09-27 evening: the tuning model below (current edits, tag multipliers, set splits) was replaced by [structure-groups](structure-groups.md); the scanner, vanilla table, runtime-applier and gotcha notes here still hold.**

**Full reference + in-game tests (ST-) and the list of other mods' structure configs: `feature_writeup/structure_tuning.md`.** This memory keeps history/gotchas.

**2026-09-27 coverage pass:** live sets the jar scan can't see (registered in Java) go in the scanner's `CODE_REGISTERED_SETS` (only `ae2:meteorite`, found via `tools/check_structure_live.py` section 1 against `/dumpstructuregen`). Scanner also writes read-only `blocked` notes from `CODE_BLOCKS` + current YUNG configs (vanilla pyramid/jungle temple/witch hut/fortress/stronghold + BD small nether dungeon are stopped in code, so tuning them is moot). Every structure now has a mod/* and type/* tag (new house mods -> `mod/villager_houses` + `type/house`). `node tools/structure_tuning_sim.js` runs the real structure_overrides.js in node and matched the game's report line for line.

Built a pack-wide framework to retune structure spawn frequency (spacing/separation/distance/
spread/count/frequency chance), nudge grid alignment (locate_offset xyz), and remove structures
entirely — vanilla or modded, one place, no per-mod config hunting.

**Files:**
- `tools/gen_structure_config.py` — scans `mods/*.jar` for every `worldgen/structure_set` +
  `worldgen/structure` json (mod-authoritative), plus a hand-maintained `VANILLA_STRUCTURE_SETS`
  table for the ~19 structure_sets vanilla bootstraps in Java (no loose json exists to scan).
  Mod jsons always take priority over the vanilla table when both exist for the same id.
  Re-run-safe: preserves `current`/`enabled` edits already in the config, only refreshes
  `original`/biome info/newly-discovered entries. Writes
  `kubejs/generation_scripts/worldgen/structure_config.json`.
- `kubejs/generation_scripts/worldgen/structure_config.json` — the editable source of truth.
  Per structure_set: `source`, `structures` (id/weight/**enabled**/biomes-readonly), `original`
  (diff baseline), `current` (edit this).
- `kubejs/generation_scripts/worldgen/gen_structure_overrides.js` — `node
  gen_structure_overrides.js`, diffs `current` vs `original` + `enabled`, writes/removes
  `kubejs/data/<ns>/worldgen/structure_set/<path>.json` overrides. Untouched entries get no
  file. Sole-member removal -> whole set gets `frequency: 0` (schema-safe for any placement
  type). Subset removal within a multi-structure set -> that entry dropped from `structures`.
- `kubejs/server_scripts/_lib/dump_structuregen.js` — `/dumpstructuregen` in-world ->
  `kubejs/exported/worldgen/structuregen.json`, live post-merge registry values via
  `server.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY / STRUCTURE_REGISTRY)`.
  **Verification tool only** (diff against `current` after applying), not the primary data
  source — the python scanner + vanilla table already gives accurate values without needing a
  game session.
- `kubejs/generation_scripts/worldgen/README.md` — full workflow + landscape facts.

**Key design choice — why structure_set only, never the structure file itself:** frequency,
xyz (`locate_offset`), and enable/disable all live on the small 3-field `structure_set` document
(`type`/`placement`/`structures`). The structure's own definition (biomes, jigsaw pools/size) is
only needed to change *where* it spawns, which wasn't the ask — so the framework never
reconstructs a full structure json (jigsaw structures have complex pool/size fields that would
be risky to round-trip; vanilla structures have no source json to round-trip from at all).
`biomes` is still recorded per structure as read-only reference info (from the mod's own
structure json, or minecraft's `has_structure/*` tags for vanilla).

**Gotcha already caught:** this pack's `minecraft:villages` (26 spacing) and
`minecraft:end_cities` are NOT pure vanilla — Terralith and Nullscape respectively already ship
their own structure_set overrides. The scanner's mod-priority-over-vanilla-table design picked
this up automatically; don't assume vanilla numbers for either of these two.

**Pre-existing before this session:** `/dumpstructures` (`kubejs/server_scripts/_lib/
structure_dump.js`) already existed — names-only listing (.nbt templates + worldgen structure
ids), unrelated to this framework's `/dumpstructuregen`.

**Related:** [dump-tags-script](dump-tags-script.md) for the `/dumpX` command convention this
follows; [loot-system](loot-system.md) for the README/workflow style this
mirrors, and its "NOT LootJS" table noting YUNG's/Philip's Ruins/Towns & Towers/Wares already
have their own structure frequency/enable config toggles as an alternative to this framework.

## Biome merge proposals (added 2026-09-25)

`tools/analyze_structure_biome_merges.py` -> `kubejs/generation_scripts/worldgen/biome_merge_suggestions.{md,json}`:
25 numbered proposals (theme × target mod) adding modded biomes to structure spawn pools. User
approves by number; **nothing applied yet** — execution plan is one additive tag file
(`"replace": false`) per tag under `kubejs/data/<ns>/tags/worldgen/biome/`.

- Only 3 mods add biomes: Terralith (149), Nullscape (8), Hearths (3). Quark adds glimmering_weald.
- Vanilla structures' biomes field = `#minecraft:has_structure/<name>` (tag-extensible). Terralith
  already extends ~27 of those tags, so the tool counts them as "current" — structure_config.json's
  own `biomes` field for vanilla structures is STALE (read from the vanilla jar only, missing
  Terralith's additions); the merge tool does its own recursive cross-jar tag resolution instead.
- Theme categories come from Terralith's c:/forge: convention tags + hardcoded vanilla extras.
  Deliberately excluded (caused false positives): c:aquatic (put rivers in ocean), c:tree_*,
  c:wasteland, mountain category (meadow villages matched it).
- Tags can be shared across structures (T&T mimic_desert uses minecraft:has_structure/desert_pyramid),
  so extending one tag affects every structure referencing it.
- Loose proposals worth a second look: forest->woodland_mansion, snowy (gravel_desert/snowy_badlands),
  jungle (amethyst_canyon, cave/underground_jungle).

### Biome merges EXECUTED (2026-09-25 later)

24 of 25 proposals applied -> 52 additive tag files under kubejs/data/*/tags/worldgen/biome/ via
`tools/build_biome_merges.py` (decisions as constants) -> `biome_merges.json` (source of truth) ->
`node gen_biome_merges.js`. Report: `biome_merge_suggestions.md`. Analyzer now writes
`biome_merge_proposals.*` (raw) so it can't clobber the annotated report.
- User rules: NO skylands biomes added anywhere (only listed: mineshaft, stronghold,
  philipsruins:ancient_dungeon already generate there via #terralith:all_terralith_biomes); NO cave
  biomes added to non-cave-native structures (Terralith `underground/*` counts as native);
  P9 (End/philipsruins) HELD for a breakout; P10 mansion = sakura_valley only; P11/P21 variant-matched
  (birch->birch_taiga+white_cliffs, flower->c:flower_forests, snowy grove/slopes/taiga/plains/beach
  split by c:mountain_slope/tree_coniferous/snowy_plains/beach); P17 sunflower got nothing (only
  skylands_summer has sunflowers); P25 minus bryce_canyon.
- User wanted precision: biome-variant structures (birch tower, flower forest tower, sunflower) must
  NOT get blanket category biomes. Executed-but-marked [VARIANT-BROAD]: P2, 14, 15, 22, 23, 25.
- "Nullscape's 8 biomes" = 3 nullscape + 5 vanilla-End overrides; 16 MES structures already spawn in
  all End biomes.

**Ruined-portal fix (2026-09-25):** `tools/gen_structure_config.py` used to invent a `minecraft:ruined_portals_nether` set; real 1.19.2 `BuiltinStructureSets` has ONE `minecraft:ruined_portals` set with 7 members incl. `ruined_portal_nether` (javap-verified). Fixed in the table + config; user chose to remove ALL ruined portals incl. nether -> overrides generated (ruined_portals frequency 0, plus pending cloudstorage big_balloon/sky_temple + ocean_monuments edits). Note: config's sky_temple "original" block (128/96/0.25) doesn't match the jar (40/24/1.0), which suggests it was mis-edited; flagged to the user. Worldgen registries load at world load in 1.19.2, so /reload is not enough (the generator header comment wrongly says it is); rejoin the world, and only new chunks are affected.

**Scanner/table fixes (2026-09-25, later same day):** (1) the vanilla table had 4 wrong STRUCTURE ids; the real 1.19.2 registry ids are `minecraft:ruined_portal` (not _standard), `jungle_pyramid`, `monument`, `mansion`. The has_structure biome TAGS still use the old names, so they're mapped in VANILLA_TAG_OVERRIDES. A structure_set override naming an unregistered structure risks breaking world load, so ALWAYS check ids against `kubejs/exported/worldgen/structuregen.json` (the live registry dump, keys `structure_sets`/`structures`; its salt/frequency are null because of Rhino method-lookup errors, but spacing/separation/members are reliable). (2) The jar scanner used plain json.loads, which silently skipped commented jsons (Beautify's 5 botanist_house sets, philipsruins' biome info). It now uses `loads_lenient()`, which strips comments. Config = 98 sets, matching the dump 1:1. Cloud Storage has only 2 real structures; its 5 balloon_stand_* templates are VILLAGE buildings injected by PoolsMixin (controlled by Cloud Storage's own config `balloon_stand_spawn_weight`, not structure_sets). The 5 unsized themissingvillages:* structures belong to no set on purpose (the mod spawns its small/medium/large variants via its own `minecraft:village` set).

## Structure tags + generator rewrite (2026-09-26)

`structure_tags.json` (hand-maintained) -> multipliers applied by `gen_structure_overrides.js` on top
of `current`; product over every tag containing a structure (each tag once, nested refs deduped).
Values: id / glob `*` / `#tag` / `!` exclude; unknown id/tag/cycle/bad multiplier = abort, no writes.
Families: `mod/<ns>` (+`mod/yungs`), `type/*` (user asked for STRUCTURE TYPE tags — tower, camp,
enemy building, underwater ruin, shipwreck, mineshaft, underground, nether/end environmental vs
constructed, vanilla-or-better dungeon — NOT loot theme/richness, which they rejected), `dungeon/*`
(surface/underground × vanilla/better/modded + unions, `dungeon/classic` = vanilla+better), metas
`dungeons pillager nether end ruins`. Classified by T&T start-pool folder (camps/forts/towers/ships)
and template block palettes (MES = constructed except meteorite/prairie/grove/enderskog).
- Math: grid -> spacing floor(s/√m), frequency absorbs rounding (exact); rings -> count; legacy_type_1
  switches to default when frequency changes. Mixed multipliers in one set -> split into
  `creativeengineering:split/<ns>/<path>_<n>` (exact only if biomes disjoint; report warns).
- Output `structure_tag_report.md`; manifest `.generated_structure_sets.json` for stale cleanup.
- **Bugs fixed:** old generator wrote `type` OUTSIDE `placement` (invalid; the 4 files were never
  loaded in-game, written after the last session); scanner dropped exclusion_zone/spread_type/MES
  min_distance/YUNG ring+enhanced_exclusion fields; vanilla table was wrong (outposts missing
  freq 0.2 legacy_type_1 + villages exclusion 10, mineshaft legacy_type_3, buried treasure
  legacy_type_2 + locate_offset 9,0,9, mansion/end_city triangular, nether_complexes fortress 2 /
  bastion 3, strongholds required preferred_biomes). Scanner now 3-way-merges `current`.
- All YUNG/MES custom placement types extend RandomSpreadStructurePlacement (javap) -> spacing math valid.
- Tested only via node against real config (scenario + error cases); NOT loaded in game yet.

## Runtime migration (2026-09-26): no node step any more

User didn't want to run a script after every edit, so all three worldgen appliers now run at world load:
- `kubejs/server_scripts/worldgen/structure_overrides.js`: KubeJS port of gen_structure_overrides.js, run from `ServerEvents.highPriorityData`. It adds each changed set with `event.add('<ns>:worldgen/structure_set/<p>.json', text)` into KubeJS's top-priority virtual pack. It writes structure_tag_report.md through patcherMod `worldgen/WorldgenFiles.writeText`, because KubeJS's class filter blocks java.io/java.nio. Any config error applies nothing and writes an error report.
- `biome_merges.js`: `ServerEvents.tags('worldgen/biome')` -> `global.tags.add` (NOT addTag, which expands parent tags). It detects unknown biomes by checking whether `event.totalAdded` stays unchanged. KubeJS validates tag ids against the live dynamic biome registry (TagManagerMixin gets the RegistryAccess entry), so Terralith biomes resolve.
- `biome_overrides.js` -> patcherMod `worldgen/BiomeLayoutTuner.run()` (Java port of the solver, getters only, SHA-256 input cache). See [biome-frequency-tuning](biome-frequency-tuning.md).
- Pack order verified via javap: `wrapResourceManager` = lowPriority virtual (addFirst) … mods … `kubejs/data` … zips … highPriority virtual (addLast = top). `DataPackEventJS.add(RL, String)` takes the full path including `.json`; `addJson` appends `.json` itself.
- Node scripts are archived in `kubejs/archive/worldgen/`, their 86 kubejs/data outputs are deleted, and the manifests are gone. Python scanners are still manual, but only needed after mod updates.
- Verification: in real Rhino (see memory `kubejs-headless-rhino-harness`) the outputs are byte-identical to the node oracle on the real config: 34 structure_sets, 237 tag adds, the report, and the biome solve on synthetic samples. The Java solver matched node in 4 scenarios (tuned/removed/replaced/errors/no-samples), including under gson 2.8.9. **Not yet run in game.**

## First in-game run (2026-09-26): two KubeJS bugs found

- **World creation failed** ("Failed to validate datapack ... Input does not contain a key [type]"
  on a philipsruins structure_set). Cause: KubeJS 1902.6.2 `VirtualKubeJSDataPack.getResources`
  filters with `path.startsWith(dir)` (no trailing "/"), so listing `worldgen/structure` also
  returned every `worldgen/structure_set/*` added through highPriorityData/lowPriorityData. Fixed by
  patcherMod `KubeJSVirtualDataPackListMixin` (@Redirect of startsWith in the static lambda
  `lambda$getResources$1`, require=1). That also required adding `kubejs-forge-1902.6.2-build.73.jar`
  to `patcherMod/libs/`. VERIFIED 2026-09-26 (structure_tuning.md section 0). KubeJS's GeneratedResourcePack
  (the kubejs/data folder) already appends "/", so only the virtual pack was affected.
- **biome_merges "0 added, 237 skipped" was a false report.** `ServerEvents.tags` handlers run first in a
  PreTagEventJS pre-pass (add() only queues an action, totalAdded stays 0, getElementIds() is empty) and are
  REPLAYED on the real load without calling the JS again. Any in-handler check such as
  "did totalAdded change" or "does this id exist" is meaningless in this KubeJS build. The verification now
  runs on `ServerEvents.loaded` against `registryOrThrow(BIOME_REGISTRY)` + `getTagOrEmpty`. The stubbed
  node run passes; the Rhino visibility of getTagOrEmpty is unverified (the call is wrapped in try and logs an error).
- biome_overrides.js still skips every dimension until `/dumpclimate` provides climate samples (expected).

**Removing a structure mod (2026-09-26, MineColonies):** the config is a static jar snapshot, so a
removed mod's sets keep getting written and world creation fails with "Unbound values in registry
... worldgen/structure: [<ns>:...]". Fix: rerun `python3 tools/gen_structure_config.py` (it rebuilds
from mods/ only and now prints `dropped <set>`), then delete that mod's EXPLICIT ids from
structure_tags.json (an unknown explicit id aborts all tuning; `ns:*` globs only warn). The mixin
fix above was confirmed working in game: structure-tuning ran and the prefix error was gone.
- **Biome-merge tools fixed (2026-09-26):** `build_biome_merges.py` now keys decisions by (theme, target mod)
  (APPROVED/HELD/VARIANT_MATCHED/... constants). Proposal numbers are display-only, removed mods' proposals are skipped,
  new proposals are reported as undecided and not applied, so re-running after mod changes is safe. The analyzer had
  silently dropped the mansion and jungle_pyramid proposals: its vanilla has_structure lookup ignored the
  id->tag-name mismatch. It now imports VANILLA_TAG_OVERRIDES from gen_structure_config.py (single source).
  Regenerated: 23 proposals, 50 tags, 228 adds, biome lists identical to the previous hand-edited file.
