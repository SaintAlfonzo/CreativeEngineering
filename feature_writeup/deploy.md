# Deploy pipeline: update payloads, deployment snapshots, kubejs classes

BUILT 2026-09-30. Offline checks passed (section 6); no real machine has been updated with an update payload yet.

## 0. At a glance

| Part | Status | Open |
|---|---|---|
| Deployment snapshot (`--snapshot`, `deploy/DEPLOYED.json`) | BUILT; 1.0.2 recorded as deployed on both sides (2026-09-30) | [tools/build_deploy.py](../tools/build_deploy.py), [deploy/DEPLOYED.json](../deploy/DEPLOYED.json) |
| `update` / `install` payload modes | BUILT; scratch-tested (DP-01..DP-04 offline) | `select_payload`, `render_readme` |
| `verify.sh` / `verify.ps1` | BUILT; sh tested, ps1 not run (no PowerShell here) | `VERIFY_SH`, `VERIFY_PS1` |
| kubejs file classes (`KUBEJS_CLASSES`) | BUILT; every kubejs file classified | `KUBEJS_CLASSES` |
| Climate samples gzipped and moved out of `exported/` | BUILT (patcherMod 1.0.3, copied to `mods/`); tuner output byte-identical | [BiomeLayoutTuner.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/worldgen/BiomeLayoutTuner.java), [dump_climate.js](../kubejs/server_scripts/_lib/dump_climate.js) |

**Day to day:**

```
python3 tools/build_deploy.py --notes "..."          # update payload from the deployed version
# copy deploy/latest/ out, run verify.sh first, apply
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
  verify/server.sha1 verify/client.sha1   the base version's jars per target (sha1sum format)
  removals_server.txt removals_client.txt
  mods/ client_mods/ kubejs/ config/ defaultconfigs/ server/ resourcepacks/ shaderpacks/
                           only the new and changed files; folders with nothing new are absent
```

verify prints `MISSING`, `CHANGED` and `EXTRA` lines and exits 1 on any of them. It exits 0 with `OK`
when the machine matches. It checks `mods/` only: configs and the tuner-written kubejs files change on the
machines by design.

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
- `pool_config.json` (1.1 MB) is the largest runtime input left. It's hand-edited and rewritten by
  PoolTuner, so it stays readable.

## 5. File map

- [tools/build_deploy.py](../tools/build_deploy.py): `KUBEJS_CLASSES`, `snapshot`, `load_base`, `select_payload`, `write_verify`, `render_readme`
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

- [ ] **DP-01 install payload:** `--mode install --dry-run` -> lists every jar, kubejs runtime/client file, config, pack (1792 files at 1.0.3).
- [ ] **DP-02 update payload:** the next real build -> `- Payload: update from 1.0.2`, only changed jars, removals match the Changes section.
- [ ] **DP-03 verify on the real server:** `sh verify.sh server <server folder>` before applying -> `OK ... matches 1.0.2 (340 jars)`.
- [ ] **DP-04 apply on the real server:** removals + copy -> server boots; `[biome-tuning]` logs `override` for 3 dimensions (samples found); no KubeJS errors.
- [ ] **DP-05 client:** `verify.sh client` (or `verify.ps1` on Windows) -> OK; apply; single-player world creation logs the biome overrides.
- [ ] **DP-06 snapshot:** `--snapshot` after DP-04/05 -> DEPLOYED.json at 1.0.3, `- Deployed:` line in the log, next `--dry-run` says `update from 1.0.3`.
- [ ] **DP-07 /dumpclimate:** in a world, `/dumpclimate` -> writes `generation_scripts/worldgen/climate/*.json.gz` (about 0.2 MB total), `/reload` solves with them.

## 8. History

- 2026-09-30: built. Deployment snapshot + update/install modes + verify scripts; kubejs classes; climate samples gzipped into `generation_scripts/worldgen/climate/` (patcherMod 1.0.3 rebuilt and copied to `mods/`). 1.0.2 snapshotted as deployed on both sides ("last night", the 00:14 staging).
