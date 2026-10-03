# 1.0.3 plan: deploy pipeline, server performance reports, bug fixes

DEPLOYED 2026-10-02 15:55 to the server and the clients (commit `d791885`; planned 2026-09-30). Everything
built after that deploy is 1.0.4: see [plan_1.0.4.md](plan_1.0.4.md), which also covers the two 1.0.3 bugs
found on deploy (join kick, client startup crash). This file covers what was planned for
1.0.3 and what was actually done in it. The planned workstreams are in the table below and sections 1-6;
everything else that went into 1.0.3 during development is in section 0.1. As each piece gets built it moves
into its own writeup (or an existing one), and this file keeps only the status line and a link.

## 0. At a glance

| ID | Workstream | Kind | Size | Status | Lands in |
|---|---|---|---|---|---|
| DP | Deploy: `install` vs `update` payloads, runtime autodumps, kubejs folder review | build | L | BUILT (offline checks), DP-01..07 pending | [deploy.md](deploy.md) (`DP-`) |
| PR | Server performance report script (logs + JFR + system snapshot in one folder/zip) | build | M | BUILT (scratch-tested, Linux), PF-09..12 pending on the real server | [perf.md](perf.md) 3.5 (`PF-09`+) |
| RD | Large block changes render wrong on clients (pasted structures, phantom leaves) | investigate, then fix | M-L | INVESTIGATING (client side cleared statically, 3.5a) | new `fixes` entry (`RD-`) |
| IM | Random item deletion in inventories: toggleable item monitor | build a diagnostic, then fix | M | REPRO DONE (4.7): SB backpack storage wipe found; monitor not built | new (`IM-`) |
| TS | Trash slot missing on most screens (player inventory, Sophisticated Backpacks) | investigate, then fix | S | DEPLOYED in 1.0.3 (patcherMod 1.0.3) | [trashslot.md](trashslot.md) (`TS-`) |
| SP | Join protection 60 s -> 150 s | config | XS | DONE (config), SP-01 pending | this file (`SP-`) |

**Suggested order:** SP, PR, IM, TS, DP, RD (section 7 has the reasons).

**Blocking questions for you** are in section 8. The main ones: which version the server and each client
are on now, which tool pasted the structure, and whether deleted items come back after a relog.

### 0.1 Also in 1.0.3 (added during development, not in the original plan)

Found by diffing the working tree against the shipped 1.0.2 snapshot (`deploy/history/1.0.2.json`, the
archived `creativeengineering-1.0.2.jar`) on 2026-10-02. Nothing here is on the server or the clients yet.
"In `mods/`" and "live" mean the dev instance runs it (the legend's DEPLOYED); none of it has had a full
in-game test.

| ID | Change | State | Writeup |
|---|---|---|---|
| EX | Exposure 1.9.19 backport (base + Polaroid + Expanded) replaces Exposure 1.6.0 | in `mods/`, EX-11/EX-17 passed, rest pending | [exposure.md](exposure.md) (`EX-`) |
| CB | Camera bags (leather, iron, netherite) | DEPLOYED in 1.0.3 (patcherMod), boot-tested only; moved to Exposure Extras in 1.0.4 | none yet |
| VJ | Vinery JEI pages + Vinery behaviour reference | in `mods/`, VJ-/VN- pending, barrel time bug open | [vinery_jei.md](vinery_jei.md), [vinery.md](vinery.md) |
| CU | Culture storage blocks (7 meats) | scripts live, untested | none yet |
| CN | Canning recipe changes: alternative additives, canned cookies and bread | scripts live, untested | [food.md](food.md) 3.6 (cans) |
| NM | Display Delight + Rustic Delight names, three food renames | live, NM-01..08 pending | [names.md](names.md) (`NM-`) |
| FM | New food mods: Farmer's Respite, Corn Delight, More Delight | in `mods/`, blocks the deploy (no `mod_sides.md` rows) | none yet |
| WG | Biome tuning re-solved on the new climate samples; village pool config regenerated | data live, new worlds only | [deploy.md](deploy.md), [structure_tuning.md](structure_tuning.md) |
| CF | Small config changes (JEI sort order and bookmarks, timestamp-only rewrites) | live | none |
| AM | Modded explorer-map trades (Lightman's Currency cashier, Cloud Storage balloon salesman) through Async Locator | in `mods/` (patcherMod 1.0.3), scratch-server tested | [deploy.md](deploy.md) runbook; this entry |
| CC | Deploy config classes: `--config functional` leaves players' keybinds/HUD/minimap/JEI/video settings alone | built, 1.0.3 staged with it | [deploy.md](deploy.md) |
| DV | Dev side: repo reorganisation, root Gradle build, new tools, dump commands, writeups | done, not shipped (except the dump commands) | see the entry |

**EX: Exposure backport.** Our own Forge 1.19.2 port of Exposure 1.9.19 and two addons (Polaroid instant
camera, Expanded lenses/filters/films), in one jar. Addon tags and loot modifiers are merged at build time.
It replaces Exposure 1.6.0 with no compatibility: 1.6.0 cameras and photos in existing worlds don't carry
over (EX-50 is the old-world check to run before deploying). Catalog is on hold.
- Where: [development/exposureBackport/](../development/exposureBackport/), `mods/exposureBackport-1.9.19-ce-19.2-01.jar`,
  `config/exposure-common.toml`, `exposure-client.toml`, `exposure_polaroid-client.toml` (new format).
- State: in `mods/`; the photo-stack and Camera Stand bugs found in game are fixed (EX-11, EX-17 passed);
  Expanded hasn't been tested in game; `tools/check_jar.py` passes.
- Writeup: [exposure.md](exposure.md).

**CB: Camera bags.** Three bag items that hold only `#creativeengineering:camera_equipment` (cameras, the
camera stand, Exposure's lens/filter/film tags and the 10 instant slides: 64 items). The contents live in the
stack's NBT, so a bag can be carried, dropped or upgraded without losing them. Leather bag: shaped recipe;
iron and netherite: smithing upgrades that keep the contents. Slot counts are CEConfig `[camera_bag]`
(5 / 9 / 14).
- Where: patcherMod [camerabag/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/camerabag/),
  [equipment/camera_bag.js](../kubejs/server_scripts/equipment/camera_bag.js) (recipes),
  [tags/equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) (tag), item_dict keys `i.*_camera_bag`.
- State: in `mods/` (patcherMod 1.0.3, rebuilt 2026-10-02). A scratch server boot proved the items, config,
  tag and recipes load. The GUI, slot locking, smithing, tooltip and overflow spill are untested. The iron
  and netherite textures are placeholders. Not committed to git yet.
- Writeup: none yet; the camera-equipment tag belongs next to [exposure.md](exposure.md).

**VJ: Vinery JEI and reference.** JEI pages for Vinery's Aging Barrel fermentation, Apple Press, Grape
Stomping and Wine Aging (Vinery 1.3.10 ships REI support only). Alongside it: a behaviour reference for
Vinery from the decompiled jar, a prefix form of `removeTooltipKey` (`'key.*'`) in
[tooltips.js](../kubejs/client_scripts/tooltips.js), and commented-out Vinery tooltip placeholders.
- Where: patcherMod [compat/vinery/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/vinery/),
  `tools/decompile_mod.py`.
- State: in `mods/` with patcherMod 1.0.3; VJ-01..10 and VN-01..20 pending. Open bug: the barrel page shows
  1800 ticks, but the barrel really takes about 50.
- Writeup: [vinery_jei.md](vinery_jei.md), [vinery.md](vinery.md).

**CU: Culture blocks.** Seven storage blocks for the cultured meats (beef, pork, mutton, poultry, rabbit,
cod, salmon): 9 cultures make 1 block, and 1 block gives back 9. They are soft like the prepared flesh block
(clay material, mud sound, breakable by hand, hoe is the fast tool) and are in BlockRunner's `spongy` speed
tier.
- Where: [items/cultures.js](../kubejs/startup_scripts/items/cultures.js) (blocks),
  [cooking/cultivated_meat.js](../kubejs/server_scripts/cooking/cultivated_meat.js) (recipes),
  [tags/storageblocktags.js](../kubejs/server_scripts/tags/storageblocktags.js),
  [tags/blocktags.js](../kubejs/server_scripts/tags/blocktags.js) (spongy), `kubejs/assets/creativeengineering/`
  (7 blockstates, block and item models, textures), item_dict `i.culture_block_*`.
- State: scripts live, not checked in game.
- Writeup: none yet (backlog chunk 6, engineering recipes, already covers the sample syringe).

**CN: Canning changes.** The multi-serving cans themselves shipped in 1.0.2. In 1.0.3: every "stretched" can
recipe takes a choice of additive. Veggie and meat cans take salt or bone broth; fruit cans take sugar or a
honey bottle (the bottle comes back). Canned pumpkin moved from the salt group to the sugar group. There are
new recipes for canned cookies and canned bread. The file also gained the design notes and a TODO list for
the remaining can groups.
- Where: [cooking/canning.js](../kubejs/server_scripts/cooking/canning.js).
- State: scripts live, untested (the multi-serving cans are still untested from 1.0.2 as well).
- Writeup: [food.md](food.md) 3.6 covers the can mechanics; the additive changes aren't in it yet.

**NM: Names.** 205 generated names for Display Delight placed foods
(`kubejs/assets/displaydelight/lang/en_us.json`, from `tools/gen_display_delight_names.py`) and names for
the Rustic Delight coffee/cotton/bell pepper crops and potted wild plants. Also three renames in
[namechanges.js](../kubejs/client_scripts/namechanges.js): Canned Rotten Flesh to Canned Tough Jerky, Canned
Spider Eyes to Canned Night Berries, Chocolate Spider Eye to Chocolate Night Berries. Includes the
missing-names catalog (offline pass) and the `/dumpnames` command.
- State: live; NM-01..08 pending; `/dumpnames` not run yet. The three renames aren't in names.md yet.
- Writeup: [names.md](names.md).

**FM: New food mods.** Farmer's Respite 2.0, Corn Delight 1.0.3 (with its library, Mysterious Mountain
Lib 1.2.3) and More Delight 25.07.28 are in `mods/`, with their default configs in `config/`.
- State: in `mods/`, not checked in game. **Blocks the next deploy:** `build_deploy.py` stops because the
  four jars have no row in `mod_sides.md`. They aren't in the food types, the food catalog or the names
  catalog yet (names.md still lists Corn Delight as "not installed"). `pack/manifest.json` is out of date.
- Writeup: none yet; [food.md](food.md) and [food_catalog.md](food_catalog.md) need them once the food
  types are assigned.

**WG: Worldgen data.** The climate samples were resampled on 2026-09-30 (a different seed) and now ship
gzipped in `kubejs/generation_scripts/worldgen/climate/` (part of DP). `biome_config.json` was re-solved on
them: the solver's model agreement went from 51% to 99%, and the per-biome target shares shifted. The village
pool tuning (`pool_config.json`, `pool_tag_report.md`, commit `ffdda7e`) was regenerated from a fresh pool
dump: entries reordered, and the Beautify botanist houses' recorded original street weight corrected from 2
to 1 (the current weights are unchanged).
- State: data live; applies to new worlds (biomes) and new chunks (pools) only.
- Writeup: [deploy.md](deploy.md) (gzipped samples), [structure_tuning.md](structure_tuning.md) (pools); the
  biome solver has no writeup yet (backlog chunk 9).

**CF: Small config changes.** JEI's mod and recipe-category sort orders were edited. JEI's world-local
`bookmarks.ini` is in the payload, so everyone would get the dev world's bookmarks (decide whether it should
ship). `rare-ice.properties` and `worldedit.properties` only differ by the timestamp the mods rewrite on load.
`creativeengineering-common.toml` gained the `[trash_slot]` section; `[camera_bag]` isn't written to the
dev instance's copy yet (it appears on the next launch).

**AM: Async map trades.** Lightman's Currency `ItemsForMapTrade` (cashier ocean/woodland maps) and Cloud Storage
`SellingMap` (balloon salesman big balloon/sky temple maps) searched for structures on the server thread: a
scratch server's `/dumptrades` froze 60 s and the watchdog killed it. patcherMod `LCItemsForMapTradeMixin` +
`CSSellingMapMixin` hand off to `compat/AsyncMapTrades`, which returns Async Locator's pending map, searches on
its thread and fills the map in (LC: locked; CS: tinted). The search is decided one server task later and runs
for any trader that has ever been in a world, because MCA builds a vanilla villager's offers, discards it and
hands the same offers to its replacement. Scratch test 2026-10-02: 24 summoned traders, 23 maps filled, 0 left
pending, no lag warning; `/dumptrades` 5 s (was a watchdog kill).

**CC: Config classes.** See [deploy.md](deploy.md) "config classes". 126 player files stopped shipping with 1.0.3
(none removed); `trashslot-common.toml` ships once more with 1.0.3.

**DV: Dev side (not shipped).**
- Reorganisation: patcherMod moved to `development/patcherMod/`, with a root Gradle build
  (`settings.gradle` + `build-logic` convention plugins, `installToMods`). Path mentions in scripts were
  updated.
- New tools: `tools/decompile_mod.py`, `jei_coverage.py`, `missing_names.py`, `lang_index.py`,
  `gen_display_delight_names.py`, `tools/item_repro/` (IM),
  `tools/perf/ingest_report.py` (PR), `development/exposureBackport/tools/check_jar.py`.
- New dump commands (ship with kubejs, operator use): `/dumpcraftingaudit` and `/dumpnames`, both added to
  `/dumpall`.
- New writeups: [crops.md](crops.md) (reference, gaps G1-G10 open), [jei_coverage.md](jei_coverage.md)
  (part 1 done), [names.md](names.md), [exposure.md](exposure.md), [vinery.md](vinery.md),
  [vinery_jei.md](vinery_jei.md), [trashslot.md](trashslot.md), [deploy.md](deploy.md).

**Before staging 1.0.3:**
1. DONE 2026-10-02: `mod_sides.md` rows for the four new jars (all `both`), pack manifest regenerated.
2. DONE 2026-10-02: the camera bag work was committed with the deploy (`d791885`).
3. DONE 2026-10-02: restaged. The server never got 1.0.2, so DEPLOYED.json now marks the server at 1.0.1 and
   the client at 1.0.2. The payload's files are diffed from 1.0.1; each target's removals and verify list come
   from its own version. A scratch dedicated server with the 1.0.3 server jar set booted in 61 s with no
   errors from our code or the new mods. `deploy/RUNBOOK.md` (perf capture + data dumps) ships in the payload.
4. OPEN: `config/jei/world/local/Workspace/bookmarks.ini` still ships (it has shipped since 1.0.0; excluding
   it now would make it a removal).
5. EX-50 (old Exposure items in winecraft) was not run: no copy of the live world here.

## 1. DP: deploy pipeline, install vs update

**Status (2026-09-30): BUILT, see [deploy.md](deploy.md).** Changes from this design: the deployed version is
set by `--snapshot` into `deploy/DEPLOYED.json` (not `--mark-installed`/INSTALLED.json); update payloads diff
every folder per file (kubejs/config too, not copied whole); the climate samples ship gzipped from
`generation_scripts/worldgen/climate/` (217 KB) instead of being autodumped, since they're needed before any
world exists. 1.0.2 is the first snapshot.

### 1.1 Today

[tools/build_deploy.py](../tools/build_deploy.py) stages one payload in `deploy/latest/` and records every shipped file's
sha1 in `deploy/history/<ver>.json`. The payload is always the full pack. The 1.0.2 staging measured:

| Folder | Size | Notes |
|---|---|---|
| `mods/` | 429 MB | every server+client jar, even though 1.0.2 changed 4 jars |
| `client_mods/` | 19 MB | client-only jars |
| `kubejs/` | 15 MB | see 1.4 |
| `config/` | 2.9 MB | |
| `server/`, `defaultconfigs/`, removals, README | < 0.1 MB | |
| **Total** | **465 MB** | an update from 1.0.1 needed about 15 MB of jars |

The history json already holds what an update needs (the full file list and hashes of every earlier
version), so the diff itself is cheap. What's missing is (a) knowing what each machine actually runs, and
(b) a payload shape that contains only the difference.

### 1.2 Design

Two modes:

- `build_deploy.py --mode install`: today's behaviour. Everything, for a fresh server or client.
- `build_deploy.py --mode update [--from <ver>]`: jars that are new or changed since `<ver>`, plus the
  small trees copied whole (`kubejs/` scripts, `config/`, `defaultconfigs/`, `server/`), plus
  `removals_*.txt` computed against `<ver>`.

**Marking what's installed.** Add `deploy/INSTALLED.json`, tracked in git:
`{"server": "1.0.2", "client": "1.0.2"}` with the date each was set. A new command marks a version as
distributed after you copy it: `build_deploy.py --mark-installed server 1.0.3`. `--from` defaults to the
older of the two marks, so one update payload serves both sides. If the server and client drift apart
often, a later option is `--target server|client` with one payload each; not needed now.

**Proving the target matches before copying.** An update is only safe if the machine really is at `<ver>`
(a hand-copied jar or a skipped removal would break the diff). Each update payload ships
`verify.sh` / `verify.ps1`, generated from the `<ver>` history json. It hashes the target's `mods/`
(sha1sum / Get-FileHash, no Python needed on the server) and prints missing, extra and changed files. The
README says: run verify first; if it reports drift, use the install payload instead.

**Payload layout (update):**

```
deploy/latest/
  VERSION.txt            1.0.3 (update from 1.0.2)
  README.md              steps for this mode only
  verify.sh / verify.ps1
  removals_server.txt / removals_client.txt
  mods/                  only added/changed jars
  client_mods/           only added/changed client jars
  kubejs/ config/ defaultconfigs/ server/   copied whole (small), minus the data folders in 1.3
  resourcepacks/         only new/changed (already works this way)
```

Optionally zip it (`CE-1.0.3-update-from-1.0.2.zip`) so one file gets copied. Jars are already compressed,
so the gain is convenience, not size.

**Log and history.** The DEPLOY_LOG entry records the mode and `from` version. The history json stays a
full snapshot of the version (it's what future diffs and verify scripts read), whatever the payload mode.

### 1.3 Data folders: autodump at runtime instead of shipping

Principle: ship what's authored, regenerate what's derived. Anything a script or the mod can rebuild on
the target at startup should not travel.

| Data | Size | Read at runtime by | Plan |
|---|---|---|---|
| `kubejs/exported/worldgen/climate/*.json` | 3.0 MB | BiomeLayoutTuner (biome multipliers are skipped without it) | Java autodump: if the samples are missing or older than the pack version, BiomeLayoutTuner (patcherMod) runs the `/dumpclimate` sampling itself at server start, then tunes. First check whether the samples depend on the world seed: if they do, the server needs its own samples anyway and the dev copies are wrong for it. |
| `kubejs/exported/worldgen/structuregen.json` | small | structure scripts | same treatment if it is read at runtime; otherwise dev-only |
| `kubejs/exported/compost/compost.json`, `loot_runtime_additions.json` etc. | small | check each | review in 1.4; any runtime reader gets an "if missing, dump first" guard |
| rest of `kubejs/exported/` (tags, recipes, loot tables, food, gear, items_by_mod) | about 35 MB | dev tools only | never shipped (already excluded); keep that way |
| `kubejs/data/` | 4.2 MB | datapack (120 structure `.nbt`, 274 `.json`) | mostly authored content (the 43 ported structure jars), so it can't be dumped. Per-file hash diff in update mode instead: unchanged files don't ship. Any json in it that a tool generates is a candidate for runtime generation; the review lists them. |
| `kubejs/generation_scripts/` | 2.9 MB | a handful of files (see 1.4) | ship an allow-list of runtime inputs only; reports, proposals and generator inputs stay home |

The `kubejs/` tree stays "copied whole" in update mode once the large derived parts are out: it'll be a few
MB of scripts and configs, which you asked to keep simple.

### 1.4 kubejs folder review (task)

Classify every kubejs path as **runtime: both**, **runtime: server only**, **runtime: client only**,
**derived (autodump)** or **dev only**, and encode it in build_deploy.py (replacing today's "everything not
git-ignored, minus EXCLUDE"). Starting points found while planning:

- `generation_scripts/`: runtime readers reference `worldgen/structure_config.json`,
  `structure_groups.json`, `pool_config.json` (1 MB), `biome_config.json`, `biome_merges.json`,
  `loot_container_swaps.json`, `food/food_types.json`, `food/can_servings.json`, and possibly
  `sawmill.js`. The `.md` reports, `biome_merge_proposals.json`, `*_suggestions.md`, `*_estimates.md` and
  `recipes/` (868 KB) look dev-only. Confirm each by grep over patcherMod + kubejs scripts.
- `assets/` (1.6 MB, textures and lang): client only. The server payload can skip it.
- `client_scripts/`: client only.
- `startup_scripts/todo/` (120 KB): check whether it's live code or parked notes.
- `archive/`, `quest_working/`: already excluded.
- Every `exported/...` path referenced at runtime (list in 1.3): runtime-read or dump-only?

Output: a table in `deploy.md` and a `KUBEJS_CLASSES` map in build_deploy.py, with a preflight check that
fails when a new top-level kubejs path isn't classified.

### 1.5 Tests (proposed)

- **DP-01** install mode reproduces today's payload (file list equal to the 1.0.2 staging).
- **DP-02** update from 1.0.2 contains only the jars that changed; removals match the diff.
- **DP-03** verify script on a scratch server at 1.0.2 reports no drift; after deleting one jar it reports it.
- **DP-04** scratch server at 1.0.2 + update payload boots, and its `mods/` hashes equal the 1.0.3 history.
- **DP-05** scratch server with no `kubejs/exported/` boots, autodumps climate samples, and biome tuning runs.
- **DP-06** `--mark-installed` updates INSTALLED.json and the next update defaults `--from` to it.

## 2. PR: server performance report

**Status (2026-09-30): BUILT, see [perf.md](perf.md) section 3.5.** Changes from this design:
- Linux only: the server runs Debian (crash reports in `debug/`), so `report.ps1` was not built (PF-13).
- Crash recordings go in `previous-run/`, not `crash-jfr/`. A watchdog crash exits through `System.exit`, and
  JFR then deleted the repo folder, so the launch scripts now add `dumponexit=true,filename=perf/exit`.
- The version comes from mod hashes matched against `deploy/history/` on the dev side (`files.sha1`), not from
  a `VERSION.txt` on the server.
- `ingest_report.py` also rebuilds the exact server jar set by hash for attribution.

### 2.1 Today

`server/perf/dump.sh|bat|ps1` saves the rolling JFR recording (last 30 minutes, `maxage=30m`) to
`perf/recordings/`, and [perf.md](perf.md) covers turning it into an HTML report on the dev machine. Logs,
crash reports and machine state are gathered by hand, if at all.

### 2.2 Design

`server/perf/report.sh` (+ `report.ps1`, `report.bat` wrapper), run from the server folder:

```
perf/report.sh                 # default window 30m, matching the recording's maxage
perf/report.sh 10m lagspike    # shorter window, tag in the folder name
```

It builds `perf/reports/<date>-<time>[-tag]/` and zips it to the same name:

| Item | Source | Notes |
|---|---|---|
| `recording.jfr` | same logic as dump.sh, same window | falls back to copying the newest `perf/jfr-repo/` folder when there's no jcmd |
| `crash-jfr/` | leftover `perf/jfr-repo/` folders from runs that didn't stop cleanly | the last minutes before a crash |
| `logs/latest.log`, `logs/debug.log` | `logs/` | full files, plus `*.window.log` excerpts trimmed to the window |
| `logs/kubejs/` | `logs/kubejs/*.log` (server, startup) | |
| `logs/ce-itemmonitor.log` | if IM is on (section 4) | |
| `crash-reports/` | files newer than the server's start (or the last N) | |
| `hs_err_pid*.log` | server root | JVM hard crashes |
| `jvm/` | `jcmd Thread.print`, `GC.heap_info`, `VM.flags`, `VM.uptime` | when jcmd exists |
| `system.txt` | uptime/load, free memory, CPU count, disk free, java -version | |
| `mods.sha1` + `VERSION.txt` | hash of `mods/` | tells whether the server matches its deploy version (same check as DP's verify) |
| `REPORT.md` | generated | window, tag, what was found or missing |

**Never collected:** anything under `config/` wholesale (`lightmansdiscord` holds a bot token),
`server.properties` without redacting `rcon.password`, `ops.json`, `whitelist.json`, world data. The
script uses an allow-list, not a copy of folders.

**Dev side:** `tools/perf/ingest_report.py <zip>` unpacks into `perf/incoming/<name>/`, runs
`perf_report.py` on the recording, and writes an index page linking the HTML report with a log summary:
"Error occurred while handling event" lines, exception counts by type, "Can't keep up" lines with their
timestamps placed on the JFR timeline, and the crash report heads.

**Stretch (deferred option 3 from perf.md):** a patcherMod `/ceperf dump [window]` command that dumps the
recording in-process through the `jdk.jfr` API (no jcmd, works on a JRE-only Windows server), and an
optional auto-trigger when MSPT stays above a CEConfig threshold for N seconds, with a cooldown. The report
script would then pick up the auto-dumped recordings too.

### 2.3 Tests (proposed, continuing perf.md)

- **PF-09** report.sh on a scratch server produces a zip with every item in the table; the window excerpt
  starts no earlier than 30 minutes back.
- **PF-10** no secrets: grep the zip for the discord token and rcon password: no hits.
- **PF-11** after a `kill -9`, the next report includes the orphaned jfr-repo folder.
- **PF-12** ingest_report.py produces the HTML report plus the log summary page.
- **PF-13** Windows: report.ps1 on a Windows server (untested platform today).

## 3. RD: large block changes render wrong on clients

### 3.1 Symptoms (as reported)

1. A pasted structure appears half unrendered in a checkerboard-like pattern and stays that way until relog.
2. HT's TreeChop (felling, `breakLeaves = true`, `maxBreakLeavesDistance = 7`) together with
   FastLeafDecay leaves phantom leaf blocks: gone on the server, still on the client, and they break
   instantly when mined.

Both look like the client ending up with a different picture than the server after many block changes in a
short time. The first job is to find out which of two layers is wrong, because the fixes don't overlap.

### 3.2 First split: client data vs client rendering

| Check | If yes | If no |
|---|---|---|
| F3+A (reload chunks, client only) fixes it | client **world data is right**, the render cache is stale: Rubidium / Distant Horizons / ImmediatelyFast side | client **world data is wrong**: block updates never arrived or were never sent |
| Walk into a phantom leaf: it has collision, F3 "targeted block" shows leaves | client world data is wrong | rendering only |
| Same thing in single player | the integrated server also uses packets, so it's not "the network" as such | dedicated-server specific: look at networking/threading on the server |
| Screenshot: are the "missing" sections dark (light) or absent (geometry) | lighting packets | geometry |

### 3.3 Hypotheses

- **H1, render rebuild missed (client).** Rubidium 0.6.2c (unmaintained) loses section rebuilds under
  bursts of updates. The checkerboard fits: whole 16x16x16 sections not rebuilt. Test: F3+A. Fix candidates:
  replace Rubidium with Embeddium (its maintained successor, has 1.19.2 builds; check rubidium-extra and
  Oculus compatibility), or a patcherMod client mixin that re-marks neighbouring sections dirty after a
  multi-block update packet.
- **H2, updates never sent (server).** Something sets blocks without the "notify clients" flag: WorldEdit's
  fast/side-effect settings on Forge, TreeChop's leaf breaking, or FastLeafDecay's scheduled removal. The
  phantom leaves fit (collision stays). Test: phantom has collision; read the setBlock flags in the
  suspect mods' bytecode. Fix candidates: WorldEdit side-effect settings or version; a mixin that forces the
  client-notify flag; or a server-side resend after large edits.
- **H3, updates sent but dropped or overwritten on the client.** Distant Horizons 3.0.2 hooks chunk
  handling on both sides; packet order races (a full chunk packet arriving after a section update). Test:
  temporarily remove DH on one client and repeat.
- **H4, light only.** Large pastes without light updates give dark sections that look missing. Test: the
  screenshot and F3 light values.

### 3.4 Tools to build for the investigation (patcherMod, useful afterwards too)

- `/ceresync [radius]` (server, op): resends the full chunk packets around the player. If it fixes the
  glitch, the client's data was wrong (H2/H3). It's also the immediate workaround instead of a relog, and
  `/ceplace` can call it automatically after large placements.
- `/cedesync [radius]` (diagnostic): the client asks the server for per-section block hashes around it,
  compares them with its own ClientLevel, and logs the mismatched positions and block states. Tells exactly
  which blocks the client has wrong and whether they cluster by section (H1/H3) or scatter (H2).
- Client `/cererender`: the same as F3+A, for players who don't know the key combo.

### 3.5 Needed from you

The tool used for the paste (WorldEdit `//paste`, Create schematic cannon, `/ceplace`, vanilla `/place`),
a screenshot of the checkerboard, and the three checks in 3.2.

### 3.5a Client-side investigation of the phantom leaves (2026-09-30, static, not run in-game)

Bytecode review of everything on the client between "packet arrives" and "pixels drawn", plus a scan of
every client log back to July. Result: **no client-side mechanism found that keeps leaves in the client's
world data after the server's removal arrives.** If the phantoms can be targeted, the removal almost
certainly never reached the client (H2/H3, server side).

| Layer | What was checked | Finding |
|---|---|---|
| Packet handling | Every mixin on `ClientPacketListener.handleBlockUpdate/handleChunkBlocksUpdate` and `ClientboundSectionBlocksUpdatePacket` (Xaero minimap + world map, DH, Rubidium, Copycats, badpackets, ...) | Observers only (Xaero reads the packet after `ensureRunningOnSameThread`); none cancels, reorders or drops updates. 0 "Failed to handle packet" in any client log |
| Block prediction | TreeChop's client `MultiPlayerGameModeMixin`; vanilla `BlockStatePredictionHandler`; Forge `ServerPlayerGameMode` | TreeChop only cancels the client's log-break prediction when the chop won't fell; it never touches leaves. Prediction holds back server updates only for positions the client itself changed, and flushes them on the ack, which the server sends after that tick's block broadcasts. No mixin on the prediction handler |
| Leaf block logic | separatedleaves `updateDistance`, immersive_weathering `LeavesMixin`, Rubidium leaves render layer | `updateDistance` is only reached from the server tick in 1.19.2; IW hooks are server-side (`randomTick` after `removeBlock`) or particles. Nothing changes leaf state client-side |
| Rendering (Rubidium 0.6.2c) | `RenderSectionManager`, `RenderSection`, `ChunkBuilder`, `ClonedChunkSectionCache` | All four vanilla dirty-section entry points are routed to `scheduleRebuild`. Two real holes, neither a good fit for TreeChop: (1) `scheduleRebuild` drops the update when the section `!isBuilt()` (a change that arrives while a freshly loaded section's first mesh build is in flight is lost and the stale mesh sticks); (2) `ClonedChunkSectionCache.release` reclaims to the pool without the lock `acquire` holds, so a clone can be reused while a task still reads it (garbage geometry, rare). A failed background build only `printStackTrace`s and leaves the old mesh; the client logs show 0 such failures |
| TreeChop server path (for reference) | `FellTreeResult` / `LevelUtil.harvestBlock` | Logs: `setBlockAndUpdate` (flag 3). Vanilla leaves: `randomTick` on a forced distance-7 state -> `removeBlock` (flag 3). Other leaves: `harvestBlock`. All notify clients, so if updates go missing it is after the setBlock (chunk broadcast) or in another mod |

**Reading the symptom.** Forge's `ServerPlayerGameMode` sends no correction when you start mining a block
that is already air on the server, so a phantom you can aim at and mine is in the client's world data. It
breaks in the normal leaf time and drops nothing. A render-only phantom (Rubidium) has no outline and
can't be targeted; the crosshair passes through it, and it vanishes when anything else in its 16x16x16
section changes (which can look like "broke instantly").

**Decides the next step (3.2 still stands):**
- Outline box / F3 "Targeted Block" shows leaves -> client data is wrong -> server side (H2/H3). Next: the
  `/cedesync` diagnostic and a server-side look at the chunk broadcast and DH 3.0.2.
- No outline, crosshair passes through, F3+A clears it -> Rubidium (H1). Fix: Embeddium, or a patcherMod
  mixin that queues the rebuild instead of dropping it when `!isBuilt()`.

### 3.6 Tests (proposed)

- **RD-01** paste a large structure with the reported tool on a dedicated server: renders fully without relog.
- **RD-02** fell a large tree with TreeChop: no phantom leaves after decay finishes.
- **RD-03** `/cedesync` right after RD-01/RD-02 reports 0 mismatches.
- **RD-04** `/ceresync` clears a forced desync without relog.

## 4. IM: random item deletion, toggleable item monitor

### 4.1 Reported

Moving items between inventories or within one sometimes deletes an item. No pattern observed so far.

### 4.2 Answer first: is it real or visual?

A client/server desync makes an item vanish on screen while it still exists on the server; it comes back
when the screen is reopened or on relog. Real deletion does not come back. These have different causes, so
the monitor has to tell them apart, and your observation (does it come back?) narrows it right away.

### 4.3 Suspects worth checking against the monitor's log

- ~~**TrashSlot.**~~ Cleared: deletions happen with no trash slot visible or nearby (5.5). A trash slot that is active but not drawn (see TS, section 5) would swallow items dropped
  where it sits. Its Delete key (`key.trashslot.delete` = Delete, Shift+Delete deletes every matching
  stack) deletes the hovered item whenever the slot counts as visible. The toggle key is `T`. The monitor
  logs every TrashSlot click and delete message, so this suspect is either confirmed or cleared quickly.
- **Mod-driven moves that bypass vanilla clicks:** Quark sort/transfer buttons, Sophisticated Backpacks
  (own slots, sort, transfer and upgrade messages), MouseTweaks drag and wheel (vanilla clicks, but many in
  a burst), Storage Drawers, Curios / Cosmetic Armor / elytra and shulker-box slots.
- **Closing a screen with an item on the cursor** while the inventory is full (should drop, not vanish).
- **Server-side menu desync** (stale `stateId`) that the server resolves by resending the whole menu.

### 4.4 Design: `[debug.itemMonitor]` in CEConfig, plus a runtime toggle

- CEConfig `enabled = false` by default; `/ceitemmon on|off [player]` (op) toggles it without a restart.
  When off, the hooks return at once (one boolean check).
- **Layer A, vanilla clicks (precise).** Mixin on `AbstractContainerMenu.clicked` (HEAD and RETURN): count
  items per (item, NBT) across the menu's slots, the player inventory and the cursor, before and after.
  Explained changes are dropped: throws (the ItemEntity spawned in the same call is captured), crafting
  result takes (ingredients down, output up), TrashSlot deletions (tagged as such). Anything else that
  loses items is logged with player, menu class, slot, click type, button and the before/after of the
  affected slots.
- **Layer B, everything else (ledger).** Per monitored player, a snapshot of inventory + open menu at the
  end of each server tick in which a container-related or mod packet was handled. A net loss not explained
  by Layer A or known sinks (eating, placing, tool breaking, machine input) is logged with the list of
  packet classes handled since the previous snapshot. This catches Quark, Sophisticated Backpacks and
  anything else with its own messages.
- **Desync detector.** Log when the server resends a full menu because of a stale `stateId`, and on the
  client (optional, client config) when a slot's content changes without a local action. A "deleted"
  item followed by a resync is visual; one with no resync is real.
- **Output:** `logs/ce-itemmonitor.log`, one JSON object per event, so the PR report collects it and a small
  `tools/itemmon_summary.py` can group events by menu class and packet type.
- **Cost:** only players with the monitor on; a snapshot is about 41 + menu slots. Layer B only runs on
  ticks with inventory activity.

### 4.5 How to use it

Turn it on for everyone for a few play sessions. When an item disappears, note the time and the item. The
log line near that time names the path: a vanilla click, a mod packet, a trash action, or a resync.

### 4.6 Tests (proposed)

- **IM-01** monitor off: no log file writes, no measurable tick cost.
- **IM-02** throw an item (Q, drag out of the screen): no loss logged.
- **IM-03** craft, shift-click a crafting result: no loss logged.
- **IM-04** drop an item on the trash slot: logged as a trash deletion, not an unexplained loss.
- **IM-05** Quark sort and Sophisticated Backpacks transfer: no loss logged.
- **IM-06** `/clear` a single item while a menu is open: logged as unexplained (proves Layer B works).

### 4.7 Automated repro results (2026-09-30)

Harness: [tools/item_repro/](../tools/item_repro/README.md). A scratch dedicated server and a throwaway
client, with a lag proxy between them. A client script made about 50,000 random inventory actions: clicks, drags,
swaps, throws, screen closes, Quark sort/transfer, backpack open/sort. A server script counted every item
each tick. Logs are in `tools/item_repro/runs/2026-09-30/`.

| Phase | Rounds | Permanent losses | Client view missing items after settling |
|---|---|---|---|
| no lag | 58 (+104 earlier, A0) | 0 (1 in A0) | 1 |
| 150 ms | 73 | 1 | 4 |
| 150 ms + slow server tick | 72 | 0 | 1 |
| 300 ms + slow server tick | 73 | 1 | 1 |

- **Real loss found: Sophisticated Backpacks storage wipe.** All 3 permanent losses (A0, C1-r81, C3-r229)
  were the same event. The backpack item stays, but its `contentsUuid` tag is removed on one tick and SB
  gives it a fresh, empty storage on the next. The old contents stay in `data/sophisticatedbackpacks.dat`,
  but nothing points to them, so to the player the backpack has emptied. The cause is SB's anti-dupe code:
  `UUIDDeduplicator.checkForDuplicateBackpacksAndRemoveTheirUUID`, which runs when a `BackpackContainer`
  is created, compares stacks by object identity and strips the ID from any *other* backpack stack with
  the same ID. The world-tick `CommonEventHandler.addBackpackIdIfUniqueOrDedupe` does the same. Every wipe
  happened when a backpack screen was re-opened while another backpack screen was already open, with
  clicks on the old screen still in flight. It happened with no added lag too (A0), so lag isn't
  required, though it widens the window.
- **Recovery works:** `/give <player> sophisticatedbackpacks:backpack{contentsUuid:[I;a,b,c,d]}` with the
  old ID brings the contents back (checked on the scratch server). The old IDs are listed in the
  `accessLogRecords` of `sophisticatedbackpacks.dat` (`/sbp list <player>` shows them, one with item name
  "Air" at the wipe time). `/sbp give` reported success but the backpack didn't show up; not investigated.
- **No vanilla click path lost items.** With lag, the client's screen more often shows a different or
  stale menu (12-16 rounds per phase against 1 without lag), and now and then items look missing until
  the screen is reopened. That is the "comes back after reopening" kind.
- **Harness artifacts, not bugs:** thrown items despawning after 6000 ticks because the test player never
  walks (now pulled back to the player); SB's two in-memory copies of a backpack's contents, which can
  disagree for 2-10 ticks while its screen is open.
- **Not covered:** Curios / shulkerboxslot / Cosmetic Armor slots, Storage Drawers, Mouse Tweaks' own
  code (only the clicks it sends), and a real player's timing.

## 5. TS: trash slot shows only on vanilla containers

### 5.1 What was found while planning

- TrashSlot keeps its per-screen state **on the client**, in `TrashSlotSaveState.json` in the instance
  folder (git-ignored, never deployed). Each screen class has `isEnabled`, a position and a lock. New screens
  get defaults; the `T` key toggles the slot on the screen you're looking at.
- This dev client's file has `InventoryScreen` and `sophisticatedbackpacks ... BackpackScreen` **enabled**
  (backpack at x 87, y 147 from the screen centre) and `CuriosScreen` **disabled**. So either the client
  showing the bug has a different file, or the slot is enabled but drawn somewhere hidden (off screen,
  under the JEI overlay or a mod's side panel), or something stops it drawing on those screens.
- TrashSlot reads `TrashSlotSaveState.default.json` when the save state doesn't exist yet and copies it
  into place. That's a clean way to ship pack defaults.

### 5.2 Investigation steps

1. On the affected client, open the player inventory and press `T`. If the slot appears, it was just
   disabled on that screen, and the fix is shipping defaults (5.3).
2. Read that client's `TrashSlotSaveState.json` for the screen classes involved.
3. If it's enabled but invisible: decompile `TrashSlotGuiHandler` / `LayoutManager` / `ContainerSettings`
   for the conditions that hide the slot (recipe book open, screen-size checks, unsupported screen types),
   and check whether the Curios / Cosmetic Armor inventory buttons swap the screen for a different class.
4. For Sophisticated Backpacks: its screen size changes with the backpack tier and upgrades, so a fixed
   offset from the centre can land off the panel or off screen. TrashSlot's API (`IGuiContainerLayout`,
   `Snap`) supports per-screen layouts that snap to the panel edge.
5. Check whether the slot is still clickable while it isn't drawn. If so, this is a direct lead for IM.

### 5.3 Fix candidates

- Ship `TrashSlotSaveState.default.json` (client payload) with the screens you want, enabled, at good
  positions. It only applies where no save state exists yet, so the README tells existing players to press
  `T` once, or the deploy removes their old file (your choice, section 8).
- If positioning is the problem: register layouts from patcherMod's client side for BackpackScreen and any
  other screens that move, snapping to the panel edge instead of a fixed offset.
- If a real render bug turns up: a small client mixin.

### 5.4 Tests (proposed)

- **TS-01** fresh client (no save state): trash slot shows in the player inventory.
- **TS-02** Sophisticated Backpacks, every tier with and without upgrades: slot visible and on the panel.
- **TS-03** chest, large chest, furnace: still shows (no regression).
- **TS-04** hidden slot is not clickable (drop an item where it would be: nothing deleted).

### 5.5 Findings (2026-09-30, from the decompiled TrashSlot 12.1.0 + Balm 4.6.0)

No TrashSlot exception or "not installed on the server" line appears in any client log, so this is a
visibility problem, not a crash. The causes found in the code, in order of likelihood:

1. **Only three screens are on by default.** `TrashSlotClient` registers layouts for `InventoryScreen`,
   `CraftingScreen` and `ContainerScreen` (chests, `_large` for double chests), all enabled by default.
   Every other screen (furnace, Sophisticated Backpacks, Curios, Cosmetic Armor, every modded machine) uses
   the fallback layout, which starts **disabled** and needs `T` once per screen class. So "shows only on
   vanilla containers" is the mod's default. The TS-03 expectation for the furnace is wrong: it's off by
   default too.
2. **`T` toggles are per client and stick.** `T` (toggle) and Shift+T (lock) work on any container screen
   and save straight to `TrashSlotSaveState.json`. On this dev client `BackpackScreen` was enabled while the
   plan was being written and is now disabled (file saved 18:51), so pressing `T` once hides the slot on
   that screen class for good. Nothing ships or resets this file.
3. **Backpack positions don't survive a size change.** The position is stored per screen class as an
   offset from an anchor point (`anchorX`/`anchorY`, 0.5 = the panel centre): x = leftPos + width*anchorX +
   slotX. The saved backpack entry (87, 147) is the default bottom-right spot for a 230x294 panel, which is
   whichever backpack was opened first. Sophisticated Backpacks changes the panel size with tier, upgrades and
   screen height, so on a smaller backpack the slot floats below the panel and on a larger one it sits over
   the bottom rows. Upstream bug: `getAnchoredY` clamps Y against the screen **width**, so a slot below a
   tall panel is never pulled back on screen at high GUI scales.
4. **Creative mode has no inventory trash slot.** It returns early for `InventoryScreen` when
   `abilities.instabuild` is set and clears state on `CreativeModeInventoryScreen` (creative has its own
   destroy slot). Chests and backpacks still show it in creative.

Ruled out:
- **Server handshake race.** `isServerSideInstalled` is reset on Balm's `ConnectedToServerEvent`
  (Forge `ClientPlayerNetworkEvent.LoggingIn`, handled with the login packet) and set by the
  `MessageTrashSlotContent` that the server sends on login. Packet order keeps these in sequence, and the
  mod is `both` in mod_sides.md and in the 1.0.2 payload.
- **The draw hook not firing on backpacks.** Balm fires the draw from Forge
  `ContainerScreenEvent.Render.Background`, and `StorageScreenBase.render` calls `renderSuper`, so it
  fires.
- **A mod replacing the inventory screen class.** The only `InventoryScreen` subclass in `mods/` is Quark's
  `BackpackInventoryScreen`, and Quark's `Backpack` module is `false`.

**First fix idea (superseded by the build below; a default file misses existing players):** ship a `TrashSlotSaveState.default.json` with the screens you want on, and
give the backpack entry `anchorX = 1.0, anchorY = 1.0, slotX = -28, slotY = 0`. With those anchors the slot
sits at the panel's bottom-right corner (render style `ATTACH_BOTTOM_RIGHT`) at every backpack size, with
no code. The default file only applies when no save state exists, so existing players need either the
file deleted in the update (plan question 7) or a small patcherMod client hook that merges the pack's
entries into their save state once per pack version. Only if the anchors aren't enough: register a
`TrashSlotAPI` layout for `BackpackScreen` from patcherMod.

**IM link: cleared (2026-09-30).** The user reports items vanish while moving items with no trash slot
visible or nearby. A hidden slot can't take clicks (`onMouseClick` needs it enabled) and the Delete keys
need it enabled too (`allowDeletionWhileTrashSlotIsInvisible = false`), so TrashSlot isn't the cause.

**Built (2026-09-30):** see [trashslot.md](trashslot.md). Defaults on for vanilla workstations, both
inventories (creative too), chests and Sophisticated Backpacks, with one position per backpack tier; every
other container is one config line away (`allContainers`, off).

## 6. SP: join protection 150 s

`config/loadingprotection-common.toml` (Loading Protection, server-side mod, range 1 to 300 s):
`protectionDuration = 60` -> `150`.

- Edit the instance file (it ships in the deploy's `config/`, and single-player worlds on clients use it
  too).
- Copy to `configs_edited/root_config/loadingprotection-common.toml` and mirror into the CreEngServer
  `configs_edited/`, per the configs-edited rule.
- It's a common config in `config/`, not a per-world serverconfig, so the server picks it up on restart
  after the deploy: no manual serverconfig step.
- Check once whether protection ends early when the player moves or attacks (mod behaviour). If it does,
  150 s is a ceiling, not a guarantee, and the writeup should say so.

**SP-01** join the server, stand still: the protection message and invulnerability last 150 s.

**Status (2026-09-30):** `protectionDuration = 150` set in `config/loadingprotection-common.toml`, copied to
`configs_edited/root_config/` and to the CreEngServer `configs_edited/root_config/`. Ships with the next deploy.

**Protection ends early on movement.** Checked in the bytecode (`ProtectionHandler.hasPlayerMoved`): every
server tick, protection ends with "Loading protection disabled because you moved." once the player is more
than 0.1 blocks (squared distance > 0.01) from the login position. So 150 s is a ceiling. A player who
logs in mid-air or on a moving block (falling, a Create contraption, a boat drifting) loses it within a tick.
While active, it cancels all damage to the player and stops mobs from targeting them.

## 7. Order and reasons

1. **SP**: minutes, no dependencies.
2. **PR**: every investigation below needs server logs; the report makes that one command.
3. **IM**: the monitor has to collect data during normal play for a while before it's useful, so turn it on
   early and let it run while the rest gets built.
4. **TS**: probably small (defaults file or layouts), and it may explain part of IM.
5. **DP**: before shipping 1.0.3, so 1.0.3 itself goes out as the first update payload (from whatever
   version the server is marked at).
6. **RD**: needs your repro checks (3.2, 3.5) first; the diagnostic commands can be built in parallel.

## 8. Open questions

1. Which version is on the server now, and on each client? 1.0.2 was restaged 2026-09-30 00:14; was it
   copied out? This becomes the first INSTALLED.json mark.
2. What pasted the structure: WorldEdit, a Create schematic cannon, `/ceplace`, or vanilla `/place`?
3. Phantom leaves: do they have collision? Does F3+A fix the pasted structure or the leaves?
4. Deleted items: do they ever come back after reopening the inventory or relogging? Single player too, or
   only on the server?
5. Trash slot: is the missing slot on this dev client or another machine? Does pressing `T` in the player
   inventory bring it back?
6. Is the server Linux or Windows? It decides which report script gets tested first.
7. When shipping TrashSlot defaults: delete existing players' save state in the update (they lose custom
   positions), or leave it and tell them to press `T`?

## 9. History

- 2026-10-02: 1.0.3 deployed to the server and the clients (15:55, commit `d791885`; `--snapshot 1.0.3` recorded that evening). Work after it (the join-kick and client-crash fixes, the camera bag move to Exposure Extras, crop parity, hoe areas, canned golem dye, new mods) is 1.0.4: [plan_1.0.4.md](plan_1.0.4.md).
- 2026-10-02: section 0.1 added: everything else in 1.0.3 so far (Exposure backport, camera bags, Vinery JEI, culture blocks, canning changes, names, new food mods, worldgen data, configs, dev side), from a diff against the 1.0.2 snapshot; TS row now in `mods/`. The file now covers planned and done work.
- 2026-09-30: PR built ([perf.md](perf.md) 3.5): report.sh + ingest_report.py, dump on exit; scratch-tested running, crashed, watchdog.

- 2026-09-30: DP built ([deploy.md](deploy.md)); 1.0.2 snapshotted as deployed.
- 2026-09-30: IM automated repro (4.7, tools/item_repro/): SB anti-dupe wipes a backpack's storage ID on re-open; contents recoverable by old UUID; no vanilla click loss found.
- 2026-09-30: RD client side investigated (3.5a): no client mechanism keeps removed leaves; targetable phantoms point server-side; two Rubidium lost-update holes noted.
- 2026-09-30: SP done (config); TS investigated (5.5) and built ([trashslot.md](trashslot.md)); TrashSlot cleared as an IM suspect.
- 2026-09-30: plan written. Findings recorded while planning: payload sizes (465 MB, 429 MB of it jars),
  runtime readers of `generation_scripts/` and `exported/`, TrashSlot per-screen client save state and its
  shipped-defaults file, Loading Protection's 60 s default and 300 s cap.
