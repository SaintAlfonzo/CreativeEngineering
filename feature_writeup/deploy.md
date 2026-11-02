# Deploy pipeline: update payloads, deployment snapshots, kubejs classes

BUILT 2026-09-30. Offline checks passed (section 6). Used for real deploys: 1.0.3 (2026-10-02) and 1.0.4 (2026-10-04) went out as
update payloads; 1.0.5 was staged (2026-10-11) but never copied out; 1.0.6 (2026-10-14, update from 1.0.4, folds in 1.0.5),
1.0.7 (2026-10-18), 1.0.8 (2026-10-19 10:20), 1.0.9 (2026-10-19 15:54), 1.1.01 (2026-10-22 22:39) and 1.1.02
(2026-10-23, recorded 2026-10-24) and 1.1.03 (2026-10-26 19:04, server and players) went out as update payloads too;
1.1.05 (update from 1.1.04, 108 files, 18.8 MB) was applied to the CreativeEngineeringClient instance 2026-10-31 12:51 with `--force`
(verify showed EXTRA on four same-named 1.1.04 jars that the payload replaces: backportedVeggies -03, creativedelight, creativeengineering-1.1.04, repackedStorage; old copies in the session scratchpad). The server is recorded at 1.1.04 (user's word, 2026-10-31) and
1.1.05 is not applied there yet ([deploy/DEPLOYED.json](../deploy/DEPLOYED.json)).

## 0. At a glance

| Part | Status | Open |
|---|---|---|
| Deployment snapshot (`--snapshot`, `deploy/DEPLOYED.json`) | BUILT; server 1.1.03 (2026-10-26 19:04), client 1.1.04 (2026-10-29 17:16); 1.1.02 on both sides before (2026-10-23) | [tools/build_deploy.py](../tools/build_deploy.py), [deploy/DEPLOYED.json](../deploy/DEPLOYED.json) |
| `update` / `install` payload modes | BUILT; scratch-tested (DP-01..DP-04 offline) | `select_payload`, `render_readme` |
| `verify.sh` / `verify.ps1` | BUILT; sh tested, ps1 not run (no PowerShell here). 2026-10-22: `--installed` / `-Installed` checks against the new version (`verify/<target>.installed.*`, both modes); ps1 usage line no longer has a tab for `\t` | `VERIFY_SH`, `VERIFY_PS1` |
| `apply.sh` / `apply.ps1`: verify (update; stops on DRIFT unless `--force`), removals, copy each folder to its place per target (`CATEGORIES`), `world_serverconfig/` into the server's `level-name` world or the client's `--world` saves, keeps `MEMORY`/`JAVAPATH` (`.bat`: `maxmemory`/`javapath`), verify `--installed`. Install mode moves old jars to `mods_before_<version>/` | BUILT 2026-10-22; sh scratch-tested (synthetic payload: update server + client, rerun refused as DRIFT, install mode, unsafe removal path skipped), ps1 not run | `APPLY_SH`, `APPLY_PS1`, `write_scripts` |
| kubejs file classes (`KUBEJS_CLASSES`) | BUILT; every kubejs file classified (2026-10-11: 9 new worldgen files, 6 runtime + 3 dev; 2026-10-14: block swap tables, 2 runtime + 1 dev) | `KUBEJS_CLASSES` |
| Mod sides (`mod_sides.md`): a jar with no row stops the build; a row whose side changed is listed under "Mods whose side changed" and the jar moves between `mods/` and `client_mods/` | BUILT; 2026-10-11: 4 rows added (Breedable Killer Rabbit server, Cool Rain both, MEED both, Pretty Rain client), JEED moved client -> both; 2026-10-14: 4 rows (Campsite Structures, restApi, Unlit Campfire both; rsec server), 417 rows | [mod_sides.md](../mod_sides.md) |
| Display Delight item models: preflight warns when `gen_display_delight_models.py --check` fails or the DD export is older than the jar ([wthit_lines.md](wthit_lines.md) F3) | BUILT 2026-10-20; passes | `check_display_delight_models` |
| In-tree mods (`IN_TREE_MODS`): patcherMod, exposureBackport, incubationAdditions, moreMooblooms, backportedVinery and backportedBrewin (2026-10-18; the two ports replaced by the `creativeBrewing` bundle 2026-10-31, which nests them); preflight checks `mods/` equals the build, the shipped jar is archived | BUILT; two jars with one mod id in `mods/` stop the build since 1.0.9 (4) | `IN_TREE_MODS` |
| Vinery donor jars (`vinery_upstream` category, both sides; [alcohol.md](alcohol.md) B4.8, D14): the two unmodified Vinery jars ship in the payload for now (a published pack downloads them from Modrinth / CurseForge instead, not built). Preflight: while the port is in `mods/` (since 2026-10-31: the `creativebrewing` jar that nests it), each jar must exist with the sha1 `UpstreamJars.java` pins. README step: copy `vinery_upstream/` next to `mods/` | BUILT 2026-10-18, not run (the port isn't in `mods/` yet) | `vinery_upstream_jars`, `collect` |
| `world_serverconfig/` payload folder: the full serverconfig set every build, with README steps to copy it into existing worlds (server and single-player saves) | BUILT 2026-10-21; scratch server PASSED 2026-10-22 (SQ-OM-17 scratch build, SQ-OM-18 world boot); a real release build and a single-player save in game (DP-08) | `main` (after `select_payload`), `render_readme` |
| Mod-update checklist: [Mod updates](#mod-updates-version-pinned-fixes) lists the version-pinned fixes; preflight warns when a pinned jar in `mods/` changed (`PINNED_MODS`, `check_pinned_mods`) | BUILT 2026-10-26; the check passes on the current `mods/` and warns on a changed file (offline) | `PINNED_MODS` |
| Climate samples gzipped and moved out of `exported/` | BUILT (patcherMod 1.0.3, copied to `mods/`); tuner output byte-identical | [BiomeLayoutTuner.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/BiomeLayoutTuner.java), [dump_climate.js](../kubejs/server_scripts/_lib/dump_climate.js) |

**Day to day:**

```
python3 tools/build_deploy.py --notes "..."          # update payload from the deployed version
# copy deploy/latest/ out, then on each machine: sh apply.sh server|client <folder> (verifies before and after)
python3 tools/build_deploy.py --snapshot             # record that deploy/latest's version is now deployed
```

**What to measure:** the `- Payload:` line of each DEPLOY_LOG entry (files and MB shipped).

## 1. Overview

Until 1.0.2 every payload was the whole pack (465 MB, 429 MB of it jars) even when four jars had changed.
Now a build diffs against the version the machines actually run, the **deployment snapshot**. You record
that snapshot by hand after copying a payload out. The payload then holds only new or changed files, in
every folder: jars, scripts, datapack files, configs and packs. Deleted files go in the removals lists.

Because a diff is only correct if the machine really is at the base version, every update payload carries
a verify script. It hashes the target's `mods/` against the base version's history before anything is
copied.

The data in `kubejs/` was cut down at the same time: nothing in `kubejs/exported/` ships any more, and
`generation_scripts/` ships only the files the game reads.

## 2. Reference

### Commands

| Command | Does |
|---|---|
| `build_deploy.py` | Builds the next patch version. Uses update mode when DEPLOYED.json has a version, otherwise install mode |
| `build_deploy.py --mode install` | Builds a full payload (new machine, or a machine where verify found drift) |
| `build_deploy.py --from 1.0.1` | Builds an update from a version other than the deployed one. Warns when that version has no snapshot |
| `build_deploy.py --dry-run` | Prints the log entry, including the `- Payload:` size, and writes nothing |
| `build_deploy.py --snapshot [VER] [--target server\|client] [--date ISO] [--notes TEXT]` | Records VER (default: `deploy/latest/VERSION.txt`) as deployed and adds a `- Deployed:` line to its log entry |

### deploy/DEPLOYED.json

```json
{
 "server": {"version": "1.0.2", "date": "2026-09-30", "snapshotSha1": "b786...", "notes": "..."},
 "client": {"version": "1.0.2", "date": "2026-09-30", "snapshotSha1": "b786..."},
 "log": [{"version": "1.0.2", "date": "2026-09-30", "targets": ["server", "client"]}]
}
```

`snapshotSha1` is the hash of `history/<version>.json` at snapshot time. An update refuses to build when
that file has changed since. An update starts from the **older** of the two marks, so one payload brings
both sides up to date.

### Payload (update mode)

```
deploy/latest/
  VERSION.txt              1.0.3 (update from 1.0.2)
  README.md                verify, then server and client steps; this version's changes; removals
  verify.sh  verify.ps1    sh verify.sh server /path/to/server  |  verify.ps1 client "C:\...\instance"
                           --installed / -Installed: the same check against this version, after applying
  verify/server.sha1 verify/client.sha1   the base version's jars per target (sha1sum format)
  verify/<target>.installed.sha1          this version's jars per target (both modes)
  apply.sh  apply.ps1      sh apply.sh server /path/to/server  |  apply.ps1 client "C:\...\instance" -World saves\<w>
  removals_server.txt removals_client.txt
  mods/ client_mods/ kubejs/ config/ defaultconfigs/ server/ resourcepacks/ shaderpacks/ vinery_upstream/
                           only the new and changed files; folders with nothing new are absent
  world_serverconfig/      every pack serverconfig file (defaultconfigs + configs_edited/server_config), complete
                           in both modes; README: copy into <world>/serverconfig/ of existing worlds by hand
```

`defaultconfigs/` reaches a world only when Forge finds that world's serverconfig file missing, so worlds
made before a serverconfig change keep the old values. `world_serverconfig/` is that set again, always
whole (a world can be any number of versions behind), and the README's server and client steps say to
copy it into the existing worlds that need it, listing the files changed since the from-version. It is
not a tracked category: no history entry, removals or verify (which worlds get it is decided by hand).

verify prints `MISSING`, `CHANGED` and `EXTRA` lines and exits 1 on any of them. It exits 0 with `OK`
when the machine matches. It checks `mods/` only: configs and the tuner-written kubejs files change on the
machines by design.
Jars the payload itself copies into `mods/` are left out of `verify/<target>.sha1`: a missing or
disabled jar the update restores is not drift (2026-10-27, Pretty Rain client -> both while clients had it
turned off). The `.installed` lists keep every jar.

### kubejs classes

Every kubejs file matches one rule in `KUBEJS_CLASSES` (first match wins). A file that matches none stops
the build.

| Class | Ships | Paths |
|---|---|---|
| runtime | yes | `startup_scripts/`, `server_scripts/` (incl. `loot/loot_config.json`, `loot_index.json`), `data/`, `config/`, and the allow-listed `generation_scripts/` inputs below |
| client | yes (one kubejs folder for both sides; the server ignores them) | `client_scripts/`, `assets/` |
| dev | never | `exported/`, `archive/`, `quest_working/`, `recipes/` (not a KubeJS load path), `startup_scripts/todo/`, every `*.md`, `README.txt`, `generation_scripts/recipes/`, `generation_scripts/sawmill.js`, `generation_scripts/worldgen/biome_merge_proposals.json` |

Runtime inputs in `generation_scripts/`, with their readers:

| File | Read by |
|---|---|
| `worldgen/climate/*.json.gz` | BiomeLayoutTuner |
| `worldgen/biome_baselines/*.json`, `worldgen/biome_config.json` | BiomeLayoutTuner |
| `worldgen/biome_merges.json` | `server_scripts/worldgen/biome_merges.js` |
| `worldgen/loot_container_swaps.json` | LootContainerSwaps |
| `worldgen/pool_config.json`, `worldgen/pool_tags.json` | PoolTuner |
| `worldgen/structure_config.json`, `structure_groups.json`, `structure_tags.json` | `structure_overrides.js` (+ MassPlacer for tags) |
| `food/food_types.json`, `food/can_servings.json` | `tags/foodtypetags.js`, `tags/cantags.js` |
| `food/can_types.json` | `items/cans.js` (generated per-type cans) |
| `food/mortar_catalog.json` | `cooking/mortar.js` (added 2026-10-22) |
| `food/placed_food_slices.json` | patcherMod `PlacedFoodSol` (added 2026-10-22) |
| `food/placeable.json` | `tags/placeabletags.js` |
| `stacks/stack_sizes.json` | `changes/stack_sizes.js` |
| `worldgen/wild_crops.json` | `worldgen/wild_crops.js` |
| `worldgen/mob_spawns.json` | `worldgen/mob_spawns.js` (SpawnTuning) |
| `worldgen/bee_nests.json` | `worldgen/bee_nests.js` (BeeNests) |
| `worldgen/wild_flowers.json`, `worldgen/flower_features.json` (snapshot) | `worldgen/wild_flowers.js` |
| `worldgen/meta_structures.json`, `worldgen/biome_fits.json` | `structure_overrides.js` (MetaStructure, StructureBiomeFits) |
| `worldgen/season_types.json` | Calendar `sereneseasons/seasontags.js` (SeasonTypes) |

Generator-only files stay home (dev): e.g. `biome_merge_proposals.json`, `bee_nests_baseline.json`,
`soil_compat_manifest.json`, `terrain_adaptation_scan.json`.

### config classes (functional vs full)

`--config functional` (the default) ships the pack's settings and leaves players' own alone; `--config full`
ships every config file. Each file under `config/` matches a rule in `CONFIG_CLASSES` (first match wins);
an unmatched file is functional.

| Class | Ships | Paths |
|---|---|---|
| functional | always | everything not listed below, incl. `jei/blacklist.cfg`, the three JEI sort orders (`recipe-category-sort-order.ini`, `ingredient-list-mod-sort-order.ini`, `ingredient-list-type-sort-order.ini`) and Xaero's server side (`xaero/*/common.cfg`, `xaero/*/server_profiles/`, `*_server.json`) |
| player | `--config full` only | `*-client.*` / `*_client.*` (every mod's client config), `jei/` (options, colours, per-world bookmarks), `xaero/` (minimap and world map), `waila/`, `pneumaticcraft/PneumaticArmorHUDLayout.cfg`, `presencefootsteps/`, `spyglass-improvements/`, `betterf3.toml`, `betterthirdperson-common.toml`, `detailarmorbar.json`, `enchdesc.json`, `MouseTweaks.cfg`, `notenoughanimations.json`, `panorama.json`, `rubidium-options.json`, `sodium-extra-options.json`, `sidebar_buttons.json`, `transition.json`, `trender.json` |
| player, one-off | functional mode too, up to the version in the rule's third field | `trashslot-common.toml` (through 1.0.3) |

- A player file is never a removal. When the pack stops shipping one, the changelog lists it as "no longer
  shipped (player settings, left in place)" and the machine keeps whatever the player has.
- `history/<ver>.json` records only what that version shipped, and `payload.config` records the mode.
- To push a changed default once, add the version as the rule's third field, e.g.
  `('trashslot-common.toml', 'player', '1.0.3')`.

### Player changelog step

From 1.1.01 every payload carries the players' notes for its version ([changelog.md](changelog.md) 3.5,
`check_changelog` / `freeze_changelog` in build_deploy.py).

- `kubejs/assets/creativeengineering/changelog/unreleased.md` is the working list: KUBEJS_CLASSES `dev`, never
  shipped under that name. The other `*.md` files there are `client`.
- Staging `X`: when the working list has bullets, the payload gets `.../changelog/X.md` with `# X` (the
  working list's subtitle kept) and `Released <today>` on top. The tree is not touched, so restaging just
  takes the newer text. No bullets = warning. An `X.md` already in the tree = error.
- `--snapshot X`: the text that shipped (the payload copy, or the working list restamped with the history
  date when the payload is gone) becomes the tree's `X.md`, and `unreleased.md` starts over as `# Unreleased`.
  If the working list changed after staging, it is left alone and the script says to trim it by hand.
- Preflight: `tools/changelog_lint.py` errors are errors; every changelog file a deployed version shipped must
  still match the sha1 in that version's history json (edited or deleted = error).
- `CONFIG_CLASSES`: `creativeengineering/changelog_seen.txt` (the read marker) is `player`; it is git-ignored too.

### Mod updates (version-pinned fixes)

Some patcherMod fixes target one exact third-party jar. Before an updated jar of one of these mods goes into `mods/`,
recheck its fix here, rerun the listed tests, then update `PINNED_MODS` in build_deploy.py (prefix -> pinned file).
Preflight warns while `mods/` holds anything other than the pinned file for a prefix (`check_pinned_mods`), so an
update is flagged at staging. "Loud" = the game stops at launch with a mixin error; "silent" = it starts and the fix
just stops working (at most a log warning) or breaks later in play. The pack's mixin config sets no
`defaultRequire`, so an injector without its own `require` is silent when its target is gone.

| Mod (pinned jar) | Fix and class | On an update | Recheck | Tests |
|---|---|---|---|---|
| Create: Dreams & Desires (`Create-DnDesire-1.19.2-1.2c.Beta Mid-Dev.jar`) | Drill and saw server crash ([fixes_compat.md](fixes_compat.md) 2.3): `ExcavationDrillItemMixin` (2 redirects, `require = 2`), `DeforesterSawItemMixin` (2 redirects, `require = 1`) | Loud if the call sites move. Silent if D&D adds a new client-only call elsewhere: the dedicated server crashes in play instead | Boot the dedicated server; if upstream fixed the crash, drop both mixins | FX-05, FX-06 (dedicated server) |
| Supplementaries (`supplementaries-1.19.2-2.4.20.jar`) + Breezy (`breezy-1.19.2-1.0.8.jar`) | Wind vane with Breezy ([fixes_compat.md](fixes_compat.md) 2.11): `SupplementariesBreezyCompatMixin` (`@Overwrite` of `BreezyCompatImpl.getWindDirection`, named by string) | Silent. A Breezy update that changes `BreezyNetworking.CLIENT_CACHE` or `NewWindSavedData.getWindAtHeight` crashes the client when the first wind vane ticks. A Supplementaries update that renames the class or method leaves its own (possibly broken) code running | Check both names still exist in the new jars (`javap -p`); if Supplementaries now supports Breezy 1.0.8+, drop the mixin | FX-14 (client) |
| Create (`create-1.19.2-0.5.1.i.jar`) | Text fields in the Configs screen ([fixes_compat.md](fixes_compat.md) 2.8): `SubMenuConfigScreenMixin` (`@Inject` into `lambda$init$12`, `CAPTURE_FAILEXCEPTION`) | Silent if the lambda is renumbered (no `require`): text values show "Impl missing" again. A changed local list throws when the screen opens | `javap -c -p` on `SubMenuConfigScreen`: the lambda that builds `ConfigScreenList$LabeledEntry` must still be `lambda$init$12` with the same captured locals | FX-11 (client) |
| Easy Elytra Takeoff (`easyelytratakeoff-1.19.2-4.2.jar`) | Rocket takeoff with the dragon chestplate ([gear_tuning.md](gear_tuning.md) F11): `EasyElytraTakeoffGroundCheckMixin` (`@ModifyVariable` on local slot 8 of `ElytraEvent.onFirework`), `EasyElytraTakeoffHelperMixin` (`@Inject` at the head of `ForgeElytraEventHelper.isWearingAnElytra`) | Silent, both (no `require`). Worse for the slot mixin: if slot 8 is another boolean now, it rewrites the wrong variable | `javap -c -p` on `ElytraEvent`: slot 8 must still be `inAir`, loaded once, right after the scan loop; `isWearingAnElytra` must still exist | GR-23, GR-29 |


## 3. What the pack changes

- **Climate samples.** `/dumpclimate` writes `kubejs/generation_scripts/worldgen/climate/<ns>__<dim>.json.gz`.
  The format is compact JSON, one point per line, integer climate values (the game quantizes to x10000
  anyway), gzipped with no timestamp. All three dimensions take 217 KB, down from 3.08 MB pretty-printed.
  `WorldgenFiles.readGzipText` / `writeGzipText` are new. BiomeLayoutTuner reads the gz first and falls
  back to the old `exported/` JSON. The dump script falls back to the old writer under a pre-1.0.3 jar.
  `biome_config.json`'s `samples.file` now points at `climate/...json.gz`.
- **Sizes, full kubejs:** 1116 files, 9.93 MB (1.0.2) -> 891 files, 6.49 MB. `exported/` 3.08 MB -> 0.
  `data/` stays 1.86 MB: its 120 `.nbt` files are already gzipped (recompressing at level 9 saves 0.2%),
  and compacting its 274 JSON files would save 48 KB. In update mode it ships per changed file instead.
- **Update from 1.0.2 (as built now, without the rest of 1.0.3):** 20 files, 3.0 MB (1 jar, 15 kubejs, 4
  config), 236 removals per side, almost all of them dev-only files leaving the machines.

## 4. Limits and decisions

- **The samples ship rather than being dumped at runtime.** They depend on the seed, but the solver only
  needs the climate distribution, which is the same for any seed at 3000 columns. A runtime dump can't
  serve the first world creation anyway: the tuner runs at datapack load, before any level exists. The
  server's existing world doesn't use them at all, since biome tuning applies only when a world is created.
- **Per-file diff for kubejs/config too, not "copy whole".** A machine's runtime-rewritten files (tuner
  stats, Forge-rewritten tomls) are left alone unless the dev copy changed.
- **verify checks jars only.** A drifted jar breaks the game; a drifted config doesn't, and configs drift
  by design.
- **A deployed version can't be rebuilt.** Its history json is what verify and the next diff read. Fix-ups
  go in the next patch version.
- **Install mode ships every resource and shader pack.** Before, packs shipped only when new or changed,
  even in what was effectively an install.
- **Not done:** zipping the payload (the jars are already compressed, so it only saves a step); separate
  server and client payloads (one payload from the older mark is enough while both sides move together).
- **Two jars with one mod id aren't caught.** Mod changes are keyed by mod id, so a second jar for an id that is
  already shipped is neither listed as added nor stopped by preflight. 1.0.7 shipped `backportedBrewin-...-01.jar`
  beside `BrewinAndChewin-1.19-2.0.jar` this way ([plan_1.0.7.md](plan_1.0.7.md) 2.2). Proposed: a preflight error
  when `mods/` (or `client_mods/`) holds two jars with one mod id. Built 2026-10-19 in `collect()` (dev instance only; an installed folder is recorded as it is).
- `pool_config.json` (1.1 MB) is the largest runtime input left. It's hand-edited and rewritten by
  PoolTuner, so it stays readable.

## 5. File map

- [tools/build_deploy.py](../tools/build_deploy.py): `KUBEJS_CLASSES`, `snapshot`, `load_base`, `select_payload`, `write_scripts`, `render_readme`
- [deploy/DEPLOYED.json](../deploy/DEPLOYED.json), [deploy/DEPLOY_LOG.md](../deploy/DEPLOY_LOG.md), `deploy/history/<ver>.json`
- [WorldgenFiles.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/WorldgenFiles.java): gzip read/write
- [BiomeLayoutTuner.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/BiomeLayoutTuner.java): `readSamples`, `SAMPLE_DIR`, `LEGACY_SAMPLE_DIR`
- [dump_climate.js](../kubejs/server_scripts/_lib/dump_climate.js)
- [kubejs/generation_scripts/worldgen/climate/](../kubejs/generation_scripts/worldgen/climate/)

## 6. Offline checks (2026-09-30)

- `deploy/latest/` (the 1.0.2 payload) matched `history/1.0.2.json` file for file before the snapshot (1974 files, 0 mismatches).
- Headless BiomeLayoutTuner run (scratch copies, gz only vs legacy only): identical override files for all three dimensions, 0 errors, `biome_config.json` stats identical apart from `samples.file`.
- Rhino (the pack's fork): the dump writer keeps integers as integers, `writeGzipText` skips unchanged content, and probing a missing static method reads as absent.
- Scratch build (patched `OUT`/`HISTORY`/`LOG`/`DEPLOYED`): update from 1.0.2 staged, 20 files.
- A fake 1.0.2 server built from the real 1.0.2 payload: verify OK. After applying removals and copying the payload, its `mods/` (340) and `kubejs/` (891) equal `history/1.0.3.json` exactly. One corrupted jar, a missing jar and an extra jar: three lines, DRIFT, exit 1.
- Guards: `--version 1.0.2` refused; a history json edited after its snapshot refused; the base moves to 1.0.3 only after both targets are snapshotted.

## 7. In-game test suite

- [x] **DP-01 install payload:** PASSED 2026-10-11 (`--version 1.0.5 --mode install --dry-run`: 2121 files, 518 MB, every category). Was: `--mode install --dry-run` -> lists every jar, kubejs runtime/client file, config, pack (1792 files at 1.0.3).
- [x] **DP-02 update payload:** PASSED (1.0.3, 1.0.4 and 1.0.5 built as updates; 1.0.5: `update from 1.0.4`, 146 files, removals = the two old in-tree jars + `item_properties.js`, matching Changes). Was: the next real build -> `- Payload: update from 1.0.2`, only changed jars, removals match the Changes section.
- [ ] **DP-03 verify on the real server:** `sh verify.sh server <server folder>` before applying -> `OK ... matches 1.0.2 (340 jars)`.
- [ ] **DP-04 apply on the real server:** removals + copy -> server boots; `[biome-tuning]` logs `override` for 3 dimensions (samples found); no KubeJS errors.
- [ ] **DP-05 client:** `verify.sh client` (or `verify.ps1` on Windows) -> OK; apply; single-player world creation logs the biome overrides.
- [x] **DP-06 snapshot:** PASSED (1.0.3 and 1.0.4 recorded; the 1.0.5 build said `update from 1.0.4`). Was: `--snapshot` after DP-04/05 -> DEPLOYED.json at 1.0.3, `- Deployed:` line in the log, next `--dry-run` says `update from 1.0.3`.
- [ ] **DP-08 world serverconfig:** *Server half: scratch server PASSED 2026-10-22 (SQ-OM-17: a scratch build with `tools/changelog_test/scratch_deploy_build.py` holds 8 files, each equal to its source, also in an update build, both README steps name them; SQ-OM-18: mod defaults 2000 / 160 / 2000 without them, 0 / -1 / 0 read from the world's `serverconfig/` with them).* next build (`--dry-run` is not enough, it writes no folder) -> `deploy/latest/world_serverconfig/` holds every `defaultconfigs` file incl. `configs_edited/server_config` (4 at 1.0.9: brickhopper, seasonhud, sereneseasons, unlitcampfire), in an update too; README server and client steps each have the Existing worlds line naming the changed files. Copy into an existing world's `serverconfig/` -> the world boots with the pack values (e.g. sereneseasons settings in `latest.log`/in game).
- [ ] **DP-09 apply script:** on the real server, `sh apply.sh server <server folder>` -> verify `OK ... matches 1.0.9`, removals deleted, folders copied, `LaunchServer.sh` keeps its `MEMORY`/`JAVAPATH`, world `serverconfig/` refreshed, ends `OK ... is at 1.1.01`; server boots. On the Windows client, `apply.ps1 client <instance> -World saves\<world>` -> the same, ends `OK`; game starts.
- [ ] **DP-10 pinned-mod warning:** first real staging after 2026-10-26 -> no `version-pinned fixes` warning while
  `mods/` holds the pinned jars. *Offline 2026-10-26: quiet on the current `mods/`; a changed pinned name warns.*
- [ ] **DP-07 /dumpclimate:** in a world, `/dumpclimate` -> writes `generation_scripts/worldgen/climate/*.json.gz` (about 0.2 MB total), `/reload` solves with them.

## 8. History

- 2026-09-30: built. Deployment snapshot + update/install modes + verify scripts; kubejs classes; climate samples gzipped into `generation_scripts/worldgen/climate/` (patcherMod 1.0.3 rebuilt and copied to `mods/`). 1.0.2 snapshotted as deployed on both sides ("last night", the 00:14 staging).
- 2026-10-02: the server never got 1.0.2, so DEPLOYED.json marks the server 1.0.1 and the client 1.0.2. Payload
  files come from the older mark, but removals and verify lists are per target (`fromByTarget`,
  `verify/<target>.version`). `deploy/RUNBOOK.md` ships in every payload. Config classes added
  (`--config functional|full`, default functional).
- 2026-10-02: `--snapshot 1.0.3 --target both --date 2026-10-02T15:55` recorded the 1.0.3 deploy (it had been copied out without a snapshot). `--version 1.0.4 --dry-run` then said `update from 1.0.3`: 26 files, 5.7 MB, removals = the two old in-tree jars + the T&T waystones patch zip. In-tree mods bumped to patcherMod 1.0.4 / exposureBackport `-02`.
- 2026-10-11: 1.0.5 restaged (`--version 1.0.5`, it was never copied out): update from 1.0.4, 146 files / 13.7 MB (mods 8,
  client_mods 1, kubejs 126, config 10, resourcepacks 1); removals 3 per side. Preflight first stopped on 4 jars with no
  mod_sides row and 9 unclassified worldgen files; rows and `KUBEJS_CLASSES` rules added (runtime: bee_nests,
  biome_fits, meta_structures, season_types, wild_flowers, flower_features; dev: bee_nests_baseline,
  soil_compat_manifest, terrain_adaptation_scan). JEED moved client -> both so its recipe types (and MEED's data) exist
  on a dedicated server. Afterwards patcherMod `mod_version` -> 1.0.6 for the next build (not built).
- 2026-10-14: **1.0.6 staged** (`--version 1.0.6`): update from 1.0.4 (both sides still run it), 1255 files / 23.2 MB
  (mods 28, client_mods 4, kubejs 1205, config 16, defaultconfigs 1, resourcepacks 1), removals 5 per side. Folds in the
  never-deployed 1.0.5 (its history json stays as an undeployed version; the changelog counts from it). Structure tuning
  ships INCOMPLETE ([plan_1.0.6.md](plan_1.0.6.md)). New `KUBEJS_CLASSES` rules for `meat_block_swaps.json` and
  `nether_block_swaps.json` (runtime, read by TemplateBlockSwaps) and `nether_swap_rules.json` (dev); 4 mod_sides rows;
  `pack/manifest.json` regenerated. Staging archived the shipped 1.0.6 jar over the unshipped 2026-10-11 build of the same
  version (lost nothing: its source is in the shipped jar). Next: copy out, then `--snapshot 1.0.6`.
- 2026-10-14 13:01: 1.0.6 copied out and recorded with `--snapshot 1.0.6` (both sides).
- 2026-10-18: `vinery_upstream` category (both sides, sha1 checked against the port's `UpstreamJars` pins),
  backportedVinery and backportedBrewin in `IN_TREE_MODS`. 1.0.7 staged at 18:20 (from `dc669e2`), went onto the
  server, which crashed on the Vinery port; restaged at 19:29 with the hotfix under the same jar name (allowed: not
  yet snapshotted), deployed and recorded 19:43 (both sides). An 18:36 build had auto-bumped to 1.0.8 and left a log
  section with no history json.
- 2026-10-19: 1.0.8 STAGED 01:16, update from 1.0.7: 38 files, 5.4 MB; the stray 1.0.8 section was replaced.
  `pack/manifest.json` regenerated first. Found while writing [plan_1.0.7.md](plan_1.0.7.md): 1.0.7 shipped two
  Brewin' jars with one mod id unnoticed (4, not fixed). Next: copy out, then `--snapshot 1.0.8`.
- 2026-10-19: preflight error for two jars with one mod id in `mods/` ([plan_1.0.9.md](plan_1.0.9.md)).
- 2026-10-19: 1.0.8 restaged 10:06 (replaces the 01:16 staging, whose patcherMod jar could not boot a dedicated server),
  copied out and recorded 10:20 (both sides). 1.0.9 staged and deployed 15:54 (update from 1.0.8, 35 files, 5.5 MB;
  both sides), with the warning that `pack/manifest.json` is out of date with `mods/` (TODO `pack/release #1`).
- 2026-10-20: preflight warning `check_display_delight_models`: runs `tools/gen_display_delight_models.py --check` and
  warns when DD item models are missing or stale, or the `/dumpitemsbymod displaydelight` export is older than the DD
  jar ([wthit_lines.md](wthit_lines.md) F3). Passes on the current tree.
- 2026-10-21: player changelog step (staging stamp, `--snapshot` freeze, frozen deployed changelogs, lint in
  preflight), see "Player changelog step" above. Checked with `tools/test_changelog.sh` (scratch-folder deploy test)
  and `--version 1.1.01 --dry-run` (payload lists `changelog/1.1.01.md`, not `unreleased.md`).
- 2026-10-22 (second bulk run, SQ-OM-17): the release preflight refused to build because `mortar_catalog.json` and
  `placed_food_slices.json` had no `KUBEJS_CLASSES` rule; both are runtime now. New
  `tools/changelog_test/scratch_deploy_build.py` runs the release build into a scratch folder. DP-08 server half
  passed (SQ-OM-17, SQ-OM-18).
- 2026-10-22: `apply.sh` / `apply.ps1` added to every payload, and `verify --installed` (`write_verify` -> `write_scripts`;
  the four templates now use `@name@` tokens instead of `str.format`). The staged 1.1.01 payload got the new scripts,
  `verify/*.installed.*` and README from its history json without a rebuild (payload files unchanged).
- 2026-10-23: 1.1.02 restaged (undistributed; the server is on 1.1.01). The earlier client `--snapshot` had marked it
  deployed and frozen `changelog/1.1.02.md`; both were undone (client mark back to 1.1.01, the file merged back into
  `unreleased.md`) so `--version 1.1.02` could rebuild. pack/manifest.json regenerated. Update from 1.1.01: 98 files /
  8.2 MB, removals 3 server / 3 client. Next: copy, then `--snapshot 1.1.02`.
- 2026-10-24: `--snapshot 1.1.02 --date 2026-10-23` run after the fact (deployed to players 2026-10-23, exact time not
  recorded; DEPLOYED.json and the DEPLOY_LOG "Deployed" line say 2026-10-23). It froze `changelog/1.1.02.md` (the text that
  shipped) and flagged `unreleased.md` as edited since staging; the shipped bullets were removed from it by hand, the
  post-1.1.02 bullets stay (next version is 1.1.03). Next update builds from 1.1.02.
- 2026-10-26: "Mod updates" checklist for the version-pinned patcherMod fixes (D&D, Supplementaries + Breezy, Create,
  Easy Elytra Takeoff) and preflight `check_pinned_mods` (`PINNED_MODS`): warns when the jar in `mods/` for a pinned
  prefix is not the pinned file ([TODO](todo/TODO.md) `pack/server #7`, closes [fixes_compat.md](fixes_compat.md) F3).
- 2026-10-26: 1.1.03 staged (undistributed; server and players are on 1.1.02). Update from 1.1.02: 267 files / 22.0 MB
  (8 mods, 234 kubejs, 12 config), removals 205 server / 206 client. Preflight fixes: `KUBEJS_CLASSES` rules for
  `food_families.json` (runtime, read by CD `seasoning_tags.js`) and `food_dict_index.json` (dev); the duplicate-mod-id
  check no longer flags third-party Jar-in-Jar libraries nested in two different jars (xaerolib, spectrelib, mixinextras,
  create_dragon_lib), only a clash with a top-level jar or a Creative Delight member. `pack/manifest.json` regenerated.
  Remaining warnings: Display Delight export older than the jar, uncommitted tree, changelog bullets over 300 characters.
  Not booted: SQ-VP-00 and the scratch queue have not run on these jars. Next: copy, then `--snapshot 1.1.03`.
- 2026-10-29: 1.1.04 built as an install payload (3438 files, 543 MB) and applied to the CreativeEngineeringClient instance
  (it failed verify against 1.1.03 with 9 drift jars, so a full install; old jars in `mods_before_1.1.04/`). Snapshot recorded
  for the client only (2026-10-29T17:16). The server is still on 1.1.03: its update payload must be rebuilt with
  `build_deploy.py --version 1.1.04 --mode update --from 1.1.03` before copying. patcherMod is now 1.1.04 (installed in
  `mods/`, old jar archived); two KUBEJS_CLASSES rules added (`wthit/silk_touch_blocks.json` runtime, `physics/item_physics_families.json` dev).
  The changelog gained a WorldEdit start-time bullet before the build; `unreleased.md` was restarted.
- 2026-10-30 00:49: 1.1.04 re-staged as an update from 1.1.03 for the server (DEPLOY_LOG: 410 files, 16.2 MB; notes
  "re-staged from the client instance state actually deployed"; `history/1.1.04.json` rewritten), not copied, no
  server snapshot. Built with warnings (patcherMod and Vintage sources newer than the jars in `mods/`). Found by the
  2026-10-31 TODO scan: the payload's `creativeengineering-1.1.04.jar` (2026-10-29 22:02, 3,283,729 bytes) and
  `creativedelight-1.0.0.jar` (2026-10-29 18:18) are not the jars in the client instance (both 2026-10-29 17:16), so
  this payload would give the server a different 1.1.04 than the client: check before copying ([TODO](todo/TODO.md)
  `pack/release #7`). Since then patcherMod 1.1.04 (2026-10-31 00:44), Creative Delight (00:29), Vinery `-06` (00:41),
  Brewin' `-04` (00:30) and three new third-party jars (Botany Pots, Cozy's Improved Cats, Room for Two) went into
  `mods/`; they belong to the next version.

- 2026-11-02, owner: the Creative Delight cutover goes in the next deployment (unless a fix has to be pushed first); the Creative Split deployment is **1.2.00**. Release numbers are not yet staged.
