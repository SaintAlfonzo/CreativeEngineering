# Performance: always-on server recording and the per-mod report

Test prefix **PF-** · Last updated **2026-09-30** · Overall status **BUILT** (the Linux path works end to end on a
scratch copy of the server, the server report included; ships with the next deploy build; the Windows scripts are untested)

## 0. At a glance

The server records what it spends CPU time on, all the time, at a cost too small to measure: Java Flight
Recorder (JFR), built into Java, no mod. When it lags, `perf/dump.sh` on the server saves the last minutes to a
`.jfr` file. On the dev machine, `tools/perf/perf_report.py` turns that file into one HTML page that answers
**which mod's code the server thread runs, in which part of the tick, and how sure that is.**

| Part | Where | Status |
|---|---|---|
| Recording settings | `server/perf/ce-server.jfc` | BUILT; A/B-tested (section 6) |
| Launch options (+ arg-passing fix) | `server/LaunchServer.sh`, `server/LaunchServer.bat` (`JVMARGS` line) | BUILT; `.sh` tested with the real starter jar; `.bat` untested |
| Dump helpers | `server/perf/dump.sh`, `dump.bat` + `dump.ps1` | BUILT; `.sh` tested; Windows untested |
| Shipping | `tools/build_deploy.py` category `server` -> server root | BUILT; dry run lists the 7 files |
| Report | `tools/perf/perf_report.py` (+ `PerfParse.java`, `modindex.py`, `report_html.py`) | BUILT; run on scratch recordings |
| Server docs | `server/perf/README.md` (ships with the server) | written |
| Server report (crash/lag bundle) | `server/perf/report.sh` | BUILT 2026-09-30; live, crashed and watchdog paths tested on a scratch server; Linux only |
| Report intake | `tools/perf/ingest_report.py` | BUILT 2026-09-30; tested on the three scratch archives |
| Dump on exit | `dumponexit=true,filename=perf/exit` in the `JVMARGS` line | BUILT 2026-09-30; watchdog crash wrote a 254 MB recording |

**After a crash or lag (preferred):** on the server, `perf/report.sh` (section 3.5). Copy the zip here and run
`python3 tools/perf/ingest_report.py <zip>`, then open `perf/incoming/<name>/index.html`.

**Recording only:** on the server, `perf/dump.sh 10m`. Copy the file to `perf/recordings/` here. Then run
`python3 tools/perf/perf_report.py perf/recordings/<file>.jfr` and open `perf/reports/<file>.html`. Add
`--baseline perf/reports/<older>.summary.json` to see what changed between two recordings.

**Knobs:** the `JVMARGS` line (`maxage=30m`, `maxsize=512m`; delete the two `-XX:` options to turn it off) and
the sample period in `ce-server.jfc` (10 ms).

## 1. Overview

JFR is a profiler inside HotSpot, made for production use. Every 10 ms it takes the stack of a few running
threads, and it logs GC, allocation, lock waits and any custom events. Minecraft 1.19.2 defines its own JFR
events (`minecraft.ServerTickTime`, `ChunkGeneration`, `NetworkSummary`, `LoadWorld`). They fire in any
recording that enables them, not only in vanilla's `/jfr`.

A recording started with `-XX:StartFlightRecording` keeps a rolling window on disk (here the last 30 minutes,
at most 512 MB). `jcmd <pid> JFR.dump` copies it out without stopping anything; it took 0.5 s in testing.

## 2. Reference: what the report shows

Sections, in page order:

1. **Tiles**: tick time (median, p95, max of Minecraft's 100-tick average), estimated server-thread busy share,
   sample count, GC pauses, allocation rate.
2. **Timelines**: tick time, per-second busy share (estimate), CPU (JVM vs whole machine), heap after GC.
3. **Server thread: who does the work**: share of server-thread samples per mod, with a 95% range bar, each
   mod's top methods and who called it.
4. **Tick phases**: entity tick, block entity tick, mob spawning, random ticks, scheduled ticks, block events,
   raids, player packets, chunk system, Forge tick events, tasks between ticks, and so on, with the mods
   inside each.
5. **Event handlers**: the Forge handler each sample ran under, e.g. `LevelTickHandler#onWorldTick`
   (snowundertrees) or `KubeJSWorldEventHandler#levelPostTick` (all KubeJS `LevelEvents.tick` scripts together).
6. **Entities / Block entities**: the class whose tick was running.
7. **Busy seconds**: mod shares in the seconds where the server thread was at least 50% busy, against the
   whole window. This is the lag-spike view.
8. **Server thread waiting**: parks over 50 ms and lock contention over 20 ms.
9. **All threads**: every thread pool's share and its mods: worldgen (`Worker-Main`), Distant Horizons,
   netty, and so on.
10. **Allocation and GC**: MB/s of garbage per mod, collections, stop-the-world VM operations.
11. **Chunk generation**: Minecraft's per-stage timing.
12. **Hottest methods**: self time on the server thread.
13. **Method and limits**, plus any mixin frames that could not be traced to a mod.

Example (scratch server, 60 s idle, no players, 137 samples, so each share is +/- 5-8 points): KubeJS 21%
(`levelPostTick`), snowundertrees 12% (`SereneSeasonsHandler#tryMeltSnowUnderTrees`), sereneseasons 9%
(`RandomUpdateHandler#onWorldTick`), lootr 4% (`TileTicker#serverTick`).

## 3. What the pack does

### 3.1 Recording settings (`server/perf/ce-server.jfc`)

It is the JDK 17 `default` preset with these changes:

- CPU sampling every 10 ms (the preset uses 20).
- `OldObjectSample` off.
- `ThreadSleep` and `JavaMonitorWait` off, and `ThreadPark` only over 50 ms. Distant Horizons' idle threads
  otherwise log about 400,000 of these in 5 minutes, and the server thread barely uses them.
- The four Minecraft events above on; per-packet events off.

### 3.2 Launch options

- `-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo`
  - At the default depth of 64, 14% of worldgen stacks and 2% of server-thread stacks lose their root, which
    is the part that says which tick phase they belong to.
  - The repository lives in the server folder, so a crash leaves the last minutes there.
- `-XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxage=30m,maxsize=512m`

**Arg-passing fix, found while wiring this.** `server.jar` is NeoForge's ServerStarterJar. It runs Forge in its
own JVM and drops every `-X` option from `user_jvm_args.txt`, so JVM options only work on the `java` command
line.

- The pack's `LaunchServer.bat` cleared `finaljavaargs` before launching, so Windows servers started without
  `-Xmx6G`, or any other option at all.
- `LaunchServer.sh` meant to do the same, but its line `$FINALJAVAARGS=` only printed "command not found", so
  the options survived by accident.

Both scripts now keep the options on the command line on purpose.

### 3.3 Dumping

`dump.sh` finds the java process whose command line has `StartFlightRecording=name=ce` and whose working
folder is this server. It prints PIDs only, never command lines, because a client's command line holds its
login token. It then runs `jcmd JFR.dump` with that JVM's own `jcmd`. `dump.ps1` does the same on Windows by
command line alone; with several servers running it asks for `-ServerPid`.

### 3.4 Report pipeline

1. `PerfParse.java` streams the recording through `jdk.jfr.consumer` into TSV/JSONL files, cached in
   `perf/parsed/<name>/`. It accepts a file or a repository folder; a chunk that won't parse is skipped with a
   note.
2. `modindex.py` maps every class in the mod jars to its mod. It also reads each mod's mixin configs and
   `@Mixin` targets, so a merged method like `BlockStateBase#handler$cej000$customOcclusion` is credited to
   copycats. Checked against the client's `-Dmixin.debug.export` dump: 2009 of 2116 injected methods exact,
   90 shared by two candidates (both listed), 1 wrong, 16 unresolved. The index is cached in `perf/.cache/`.
3. `perf_report.py` walks each stack from the running method outward:
   - The first frame owned by a mod gets the sample. Library mods (Rhino, Architectury, ...) pass it on to
     their caller, and the outermost mod is recorded as the entry point.
   - Tick phases come from vanilla marker frames, with SRG names built in so no mappings file is needed. The
     most specific marker wins.
   - The window starts at the first tick (after `ServerStartedEvent` handlers such as WorldEdit's registry
     build) and ends when Forge starts stopping the server. `--include-startup` keeps both ends.
4. `report_html.py` writes one self-contained page: inline SVG and JS, light and dark mode, a table under
   every chart.

### 3.5 Server report: `report.sh` on the server, `ingest_report.py` here

One command on the server gathers everything a crash or lag investigation needs into one archive; one command
here turns it into a start page. Designed in [plan_1.0.3.md](plan_1.0.3.md) section 2 (PR).

**`server/perf/report.sh [window] [tag]`** (default window 30m) -> `perf/reports/<date>-<time>[-tag].zip`
(`.tar.gz` without `zip`). The table of contents is in `server/perf/README.md`. Behaviour:

- **Finding the server.** Any `java` process whose working folder is the server folder; PIDs only, never
  command lines. Running: the window ends now. Not running: it ends at the last write to `logs/latest.log`, so
  after a crash the excerpts hold the minutes before it.
- **Recording.** Running: `jcmd JFR.dump maxage=<window>`, or a copy of this run's `perf/jfr-repo/*_<pid>/`
  without jcmd. Previous run: the newest `perf/exit/*.jfr` from the last 24 h and the newest leftover
  `perf/jfr-repo/` folder from the last 24 h go to `previous-run/`.
- **JVM shutting down.** The watchdog's `System.exit` can leave the JVM alive for up to 10 s.
  `JFR.dump` then says "No recordings", and REPORT.md says the JVM is shutting down.
- **Logs.** `*.window.log` excerpts parse the Forge timestamp (`[30Sep2026 22:39:38.778]`). Lines without a
  timestamp (stack traces) go with the line above.
- **Redaction.** Every copied text file goes through one awk pass. It replaces, literally, the values of
  `password`/`token`/`secret`/`apikey` keys (4+ characters) from `server.properties`, `config/*.toml`,
  `defaultconfigs/*.toml` and `<level-name>/serverconfig/*.toml`. It also masks `/a.b.c.d[:port]`. A Forge
  "Incorrect key ... was corrected from {token=...}" line was caught too, since it holds the same value.
- **`files.sha1`.** Hashes (`find -L`, so symlinked jars count) let the intake name the version and list config
  drift without shipping any config.

**Dump on exit.** Found while testing: a watchdog crash exits through `System.exit`, JFR's shutdown hook then
deletes the repository folder, and the minutes before the hang were lost. Before this, the README said the
folder stays after a crash, which was only true for hard kills. `dumponexit=true,filename=perf/exit` writes the
recording on every exit. Checked on Java 17.0.20 and 21.0.7: `System.exit` writes
`hotspot-pid-<pid>-id-1-<time>.jfr`, and `Runtime.halt` leaves the repo folder instead. Gotcha: if `perf/exit`
doesn't exist, JFR writes a *file* named `exit`. The launch scripts create the folder and keep the newest 3.

**`tools/perf/ingest_report.py <zip | tar.gz | folder>`** -> `perf/incoming/<name>/index.html`:

- **Version.** The server's `mods/` hashes are matched against every `deploy/history/*.json`. The best version
  is shown, with "mods differ" when the match isn't exact. Changed / missing / extra files are listed for
  `mods`, `config`, `defaultconfigs` and `kubejs`.
- **Jars for attribution.** Each server jar is found by sha1 in `mods/`, `deploy/latest/mods`,
  `server_mods/`, `development/patcherMod/jar_archive` and `development/patcherMod/build/libs`, and symlinked into
  `perf/incoming/<name>/mods/`. `perf_report.py` then attributes with the jars that server actually ran;
  `deploy/latest/mods` is only an update payload now. Jars with no local copy are listed. Hashes are cached in
  `perf/.cache/jar_sha1.json`.
- **perf_report runs.** `perf.html` for the live recording and `previous-<run>.html` for each `previous-run/`
  entry, with `--name incoming-<name>[...]`. `--name` is new, so the parse caches don't collide on
  `recording`.
  - If the window after boot is under 30 s, or comes out empty, the run is redone with
    `--include-startup`. The tick-time event reads 0 for a while after boot, so a server that lagged
    straight after starting showed a 5 s window.
- **Timeline.** Tick time from the recording, with "Can't keep up" lines (orange) and crash reports (red) on
  the same axis. Server-local log times are converted with the `%z` offset in REPORT.md. A hung tick never
  completes, so a watchdog hang shows as a flat line up to the red mark.
- **Logs and crashes.**
  - Lag lines.
  - Crash report heads: description, exception plus 16 frames with the transformer lists stripped, and the
    mod jars named in the stack. On the 7 external-server reports in `debug/` this named Regrowth for the RNG
    crash and Collective + nohostilesaroundcampfire for the 09-28 hang, as the hand triage did.
  - `hs_err` heads, KubeJS "Error occurred while handling event" grouped, exceptions by type, ERROR lines by
    logger with numbers normalised.
  - The Server thread from each live thread dump, and system.txt.

## 4. Limits and decisions

**Blind spots:**

- **Waiting.** Waiting on the server thread isn't sampled. It waits for chunks in 0.1 ms parks, which also stay
  under the park threshold.
- **KubeJS.** KubeJS is visible per event, not per script: Rhino interprets scripts, so no script name
  appears on the stack.
- **No dimension, entity or position.** The runtime class is also not visible when a mob inherits `tick()`.
- **Busy-seconds undercount.** JFR samples a few threads per period, so the busy-share estimate reads low
  while many threads are busy.
- **Smoothed tick time.** The tick-time series is Minecraft's 100-tick average, which smears short spikes.

**Rejected or deferred options:**

- **spark.** An interactive call tree rather than a ranked report, uploads to spark.lucko.me by default, and
  its fast engine is a native agent. Still worth using for bug reports to mod authors.
- **Observable.** Only covers entities and block entities. It remains the tool for "which one, at which
  position", and it is not in this pack's mods.
- **Vanilla `/jfr start`.** Its settings take a stop-the-world heap census (`jdk.ObjectCount`) and log every
  file read and write.
- **patcherMod JFR events (deferred, the user's option 3).** Per-tick and per-dimension timing, chunk-wait
  events with stacks, KubeJS handler timing, and a `/ceperf dump` command. These would close the waiting and
  per-dimension gaps, and they cost nothing while not recording.

## 5. File map

- [../server/LaunchServer.sh](../server/LaunchServer.sh), [../server/LaunchServer.bat](../server/LaunchServer.bat)
- [../server/perf/ce-server.jfc](../server/perf/ce-server.jfc), [../server/perf/README.md](../server/perf/README.md)
- [../server/perf/dump.sh](../server/perf/dump.sh), [../server/perf/dump.bat](../server/perf/dump.bat), [../server/perf/dump.ps1](../server/perf/dump.ps1)
- [../server/perf/report.sh](../server/perf/report.sh), [../tools/perf/ingest_report.py](../tools/perf/ingest_report.py)
- [../tools/perf/perf_report.py](../tools/perf/perf_report.py), [../tools/perf/PerfParse.java](../tools/perf/PerfParse.java), [../tools/perf/modindex.py](../tools/perf/modindex.py), [../tools/perf/report_html.py](../tools/perf/report_html.py)
- [../tools/build_deploy.py](../tools/build_deploy.py) (`server` category)
- Untracked output: `perf/recordings/`, `perf/parsed/`, `perf/reports/`, `perf/incoming/`, `perf/.cache/`

## 6. Offline checks (done 2026-09-28, scratch server from `deploy/latest`, 338 jars)

- **Overhead A/B.** Same seed, Chunky radius 400 (2601 chunks):

  | Run | Boot | Pregen | Idle tick |
  |---|---|---|---|
  | No recording | 51.2 s | 1:34 | 1.33 ms |
  | Recording | 51.2 s | 1:34 | 1.36 ms |
  | No recording | 51.0 s | 1:34 | 1.72 ms |

  A first recorded run looked 20% slower only because analysis ran on the same machine at the same time. Don't
  do that during an A/B.
- **Launch path.** `LaunchServer.sh` with the starter jar showed `Started recording 1` and created
  `perf/jfr-repo/<run>/`. `dump.sh` and `dump.sh 20s idle` both wrote files. A clean stop removed the run
  folder.
- **Report inputs.** `perf_report.py` read a dumped file and the live repository folder, including the chunk
  still being written.
- **Mixin resolver.** Checked against the `.mixin.out` ground truth (numbers in 3.4).

## 7. In-game test suite

- [ ] **PF-01 recording starts:** deploy the build that includes `server/` and start the server with
  `LaunchServer.sh`/`.bat` -> the console shows `Started recording 1`, `perf/jfr-repo/<date>_<pid>/` exists,
  and the launch line shows `-Xmx6G -XX:FlightRecorderOptions=...`.
- [ ] **PF-02 no slowdown:** play normally for 30 minutes -> `/forge tps` and the feel match the previous
  session, and there are no new errors in the log.
- [ ] **PF-03 disk stays bounded:** after an hour or more of play -> `perf/jfr-repo/` stays under about 512 MB.
- [ ] **PF-04 dump:** `perf/dump.sh 10m` (or `perf\dump.bat 10m`) -> prints `Saved ... to perf/recordings/server-...jfr`.
- [ ] **PF-05 report:** copy the file here and run `perf_report.py` on it -> the page opens, the server thread
  has several thousand samples, and the mods table and tick phases are filled in.
- [ ] **PF-06 lag capture:** when the server lags, dump within a few minutes -> "Busy seconds" lists the lag
  seconds and the mods whose share jumps.
- [ ] **PF-07 clean stop:** `stop` the server -> the run folder under `perf/jfr-repo/` is removed and
  `perf/exit/hotspot-pid-<pid>-...jfr` appears (at most 3 kept after the next launch).
- [ ] **PF-08 compare:** run a later recording with `--baseline <earlier>.summary.json` -> "change" columns
  appear.

- [ ] **PF-09 report, server running:** `perf/report.sh 10m` -> prints the window summary and
  `Saved ... perf/reports/<name>.zip`; the zip has `recording.jfr`, `jvm/threads-1..3.txt`, `logs/`,
  `system.txt`, `files.sha1`, `REPORT.md`. (Scratch: done, 13.5 s, 28 MB zip for a 5 min window.)
- [ ] **PF-10 no secrets:** `unzip -p <zip> '*' | grep -c '<discord token>\|<rcon password>'` -> 0, and a
  player's IP from a login line shows as `/<ip>`. (Scratch with fake values: 0 hits.)
- [ ] **PF-11 after a crash:** after a watchdog crash (or a stop), `perf/report.sh` -> the zip has
  `previous-run/hotspot-pid-...jfr` and the crash report. After a `kill -9` it has the leftover
  `perf/jfr-repo/<folder>/` instead. (Scratch: the watchdog case was done; kill -9 was not tested.)
- [ ] **PF-12 intake:** `python3 tools/perf/ingest_report.py <zip>` -> `perf/incoming/<name>/index.html` shows
  the right pack version, the timeline with lag/crash marks, and links to `perf.html` / `previous-*.html`
  with no "unattributed jars" warning. (Scratch: done; 1.0.2 "mods differ", as expected for dev jars.)
- [ ] **PF-13 Windows:** no `report.ps1` yet; build it if a Windows server ever needs it.

## 8. History

- 2026-09-30: Server report built (3.5): `server/perf/report.sh`, `tools/perf/ingest_report.py`,
  `perf_report.py --name`, and the dump on exit in both launch scripts. That fixes the recording a watchdog
  crash used to delete. Tested on a scratch server running, crashed (before the fix: no recording) and after a
  watchdog crash (254 MB exit dump).

- 2026-09-28: Studied the options (JFR vs spark vs Observable vs vanilla `/jfr`), prototyped on a scratch
  server, and A/B-tested the overhead. Built `tools/perf/`, `server/` (launch scripts with the recording and the
  arg-passing fix, dump helpers, settings, README) and the deploy category. BUILT, not deployed.
  - First findings on a playerless scratch server, all to confirm on the live server:
    - idle: snowundertrees, KubeJS `LevelEvents.tick` (the `armor_effects.js` horse walk runs
      `getAllEntities()` every tick in every dimension), sereneseasons
    - worldgen: blueprint's biome wrapper 8.8%, the copycats `customOcclusion` mixin 8.3%, immersive_weathering
      icicles 3.3%
    - Distant Horizons' server-side LOD builders: about 28% of all CPU samples and 23% of allocation during pregen
