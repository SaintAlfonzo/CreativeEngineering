# Server runbook: deploy, performance capture, data dumps

The build copies this page into every payload (`deploy/latest/RUNBOOK.md`). The full step list and the changelog
for this version are in the payload's `README.md`. Commands run from the server folder unless a step says
otherwise. In the server console, type commands without the leading `/`.

## 1. Before you copy anything

- Stop the server and back up the world folder.
- 1.0.3: the Exposure backport replaces Exposure 1.6.0, and old cameras and photos don't carry over. Expect blank
  photos, empty cameras and missing wall photographs (EX-50 in `feature_writeup/exposure.md`).
- Check that the machine is on the version this payload expects: `sh /path/to/payload/verify.sh server .`
  Continue only if it prints `OK`.

## 2. Apply the update (server)

```
xargs -d '\n' rm -f < /path/to/payload/removals_server.txt   # run from the server folder
cp -r /path/to/payload/mods/. mods/                          # NOT client_mods/
cp -r /path/to/payload/kubejs/. kubejs/
cp -r /path/to/payload/config/. config/
cp -r /path/to/payload/server/. .                            # LaunchServer.sh/.bat + perf/
chmod +x LaunchServer.sh perf/*.sh
```

If you changed `MEMORY` (6G) or `JAVAPATH` in the old `LaunchServer.sh`, set them again. Always start the
server with `./LaunchServer.sh`: `run.sh` skips the performance recording.

## 3. First boot checks

```
grep -E "Done \(|Crash report saved" logs/latest.log
grep -E "\[loot-tuning\]|\[structure-tuning\]" logs/kubejs/server.log    # expect "rewrote 176 tables"
grep -c "Error occurred while handling event" logs/kubejs/server.log    # expect 0
ls perf/jfr-repo/                                                        # one folder = the recording is running
```

## 4. Performance capture (Linux)

| When | Run (server folder, as the server's user) | Gives |
|---|---|---|
| Lag right now, or just after | `perf/report.sh 10m lagspike` | `perf/reports/<date>-<time>-lagspike.zip` |
| After a crash or watchdog kill | `perf/report.sh` | the same zip, built from the recording the crashed run left behind |
| Only the recording | `perf/dump.sh 10m` | `perf/recordings/server-<date>-<time>.jfr` |

The recording keeps only the last 30 minutes, so run the script soon after the lag. Copy the zip to the
dev instance, then on the dev machine:

```
python3 tools/perf/ingest_report.py <file>.zip                     # -> perf/incoming/<name>/index.html
python3 tools/perf/perf_report.py perf/recordings/<file>.jfr       # a bare .jfr -> perf/reports/<name>.html
```

Don't use vanilla `/jfr start`. It runs a stop-the-world heap census.

## 5. Data dumps (server console, op level 2)

Everything is written under `kubejs/exported/`. Bring it home with
`tar czf exported-$(date +%F).tgz kubejs/exported` and unpack it over the dev instance's `kubejs/exported/`
(back up the dev copy first).

| Command | Writes | Used on the dev machine by |
|---|---|---|
| `dumpstructuregen` | `worldgen/structuregen.json` | `python3 tools/check_structure_live.py` (live structure tuning vs config) |
| `dumploot` | `loot_tables/` | chest-loot tuning review |
| `dumptags` | `tags/` | tag checks |
| `dumprecipes` / `dumpcraftingaudit` | `recipes/` | `python3 tools/jei_coverage.py` |
| `dumpfood` | `food/` | `python3 tools/food_catalog.py` |
| `dumpcompost` | `compost/compost.json` | `python3 tools/gen_compost_values.py` |
| `dumpgear` | `gear/` | tool/armor stat tuning |
| `dumptrades` | `villager_trades.json` | trade review. Holds the tick for 2-5 s and queues vanilla cartographer map searches in the background, so run it when few players are on |

**Do not run these on the live server:**
- `dumpall`: it includes `dumpclimate` and `dumpservermods` (below).
- `dumpclimate`: overwrites the shipped climate samples in `generation_scripts/`, which biome tuning reads.
- `dumpnames`: single player only (says so on a dedicated server).
- `dumpservermods`: copies about 340 jars into `server_mods/`.

## 6. Afterwards (dev machine)

```
python3 tools/build_deploy.py --snapshot                  # both machines run the staged version
python3 tools/build_deploy.py --snapshot --target server  # only the server was updated
```
