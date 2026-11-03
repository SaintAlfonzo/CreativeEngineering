# Performance: always-on server recording and the per-mod report

Test prefix **PF-** · Last updated **2026-10-26** · Overall status **BUILT** (the Linux path works end to end on a
scratch copy of the server, the server report included; ships with the next deploy build; the Windows scripts are untested).
The pack's own JFR events (3.6, 2026-10-26) are built, headless-tested, scratch test queued (SQ-PF).

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
| Pack events (exact tick, dimension, chunk-wait, KubeJS-script, entity-type times) | patcherMod `perf/` + 9 mixins, `ce-server.jfc` (3.6) | BUILT 2026-10-26 (patcherMod 1.1.03); JFR logic and report tested headless; scratch test queued (SQ-PF) |
| `/ceperf status`, `/ceperf dump [tag]` | patcherMod `perf/PerfCommand` (3.6) | BUILT 2026-10-26; scratch test queued |

**After a crash or lag (preferred):** on the server, `perf/report.sh` (section 3.5). Copy the zip here and run
`python3 tools/perf/ingest_report.py <zip>`, then open `perf/incoming/<name>/index.html`.

**Recording only:** on the server, `perf/dump.sh 10m` (or in game, as an op: `/ceperf dump`, the whole kept window). Copy the file to `perf/recordings/` here. Then run
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
3. **Measured by the pack's hooks** (recordings from patcherMod 1.1.03 on; section 3.6): exact single-tick times
   (median, p95, p99, longest per second) and busy share, each dimension's share of the tick, chunk waits with
   the mod and call site that asked, KubeJS handlers by script and line, entity and block entity time by registered
   type with the positions of slow single ticks.
4. **Server thread: who does the work**: share of server-thread samples per mod, with a 95% range bar, each
   mod's top methods and who called it.
5. **Tick phases**: entity tick, block entity tick, mob spawning, random ticks, scheduled ticks, block events,
   raids, player packets, chunk system, Forge tick events, tasks between ticks, and so on, with the mods
   inside each.
6. **Event handlers**: the Forge handler each sample ran under, e.g. `LevelTickHandler#onWorldTick`
   (snowundertrees) or `KubeJSWorldEventHandler#levelPostTick` (all KubeJS `LevelEvents.tick` scripts together).
7. **Entities / Block entities**: the class whose tick was running.
8. **Busy seconds**: mod shares in the seconds where the server thread was at least 50% busy, against the
   whole window. This is the lag-spike view.
9. **Server thread waiting**: parks over 50 ms and lock contention over 20 ms.
10. **All threads**: every thread pool's share and its mods: worldgen (`Worker-Main`), Distant Horizons,
   netty, and so on.
11. **Allocation and GC**: MB/s of garbage per mod, collections, stop-the-world VM operations.
12. **Chunk generation**: Minecraft's per-stage timing.
13. **Hottest methods**: self time on the server thread.
14. **Method and limits**, plus any mixin frames that could not be traced to a mod.

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

### 3.6 The pack's own events (patcherMod, 2026-10-26)

Closes the blind spots of sampling (section 4) with measured times. patcherMod defines ten JFR events
(`perf/PerfJfrEvents`), fired by mixins; `ce-server.jfc` enables them all. They show in the report's
"Measured by the pack's hooks" section, and in JDK Mission Control under Creative Engineering / Performance.

| Event | Hook (SRG) | What it records |
|---|---|---|
| `ServerTick` | `MinecraftServer#tickServer` (m_5705_), `ServerTickPerfMixin` | every tick's duration, tick number, players |
| `LevelTick` | `ServerLevel#tick` (m_8793_), `ServerLevelTickPerfMixin` | each dimension's part of the tick, loaded chunks, players |
| `ChunkWait`, `ChunkWaitStats` | `ServerChunkCache#getChunk` (m_7587_) and `getChunkFuture` (m_8431_) around `managedBlock` (m_18701_), `ServerChunkCacheWaitPerfMixin` | waits over 1 ms with the stack that asked; per second: cache misses, time waited, longest |
| `KubeJSHandler`, `KubeJSHandlerStats` | `EventHandler#listen`/`listenJava` and `EventHandlerContainer.<init>` (KubeJS), wrapping each handler in `KubeJSHandlerTiming` | calls over 5 ms (jfc threshold) with event, script and line; per second and handler: calls, time, longest |
| `SlowEntityTick`, `EntityTypeTicks` | `Level#guardEntityTick` (m_46653_), `LevelEntityTickPerfMixin`; totals on the type (`EntityTypePerfStatsMixin`) | ticks over 2 ms with type, dimension, position; per second and registered type: ticks, time, longest |
| `SlowBlockEntityTick`, `BlockEntityTypeTicks` | `LevelChunk$BoundTickingBlockEntity#tick` (m_142224_), `BlockEntityTickPerfMixin`; `BlockEntityTypePerfStatsMixin` | the same for block entities |

- **Switching.** `PerfEvents` reads from JFR once a second (at the end of a tick) which events a running recording
  enables; a hook whose events are off only reads a static boolean. A client or a server without a recording pays
  nothing else. Set `enabled` to `false` in `ce-server.jfc` to drop one; a recording started later with `jcmd` is
  picked up within a second. Events a `.jfc` does not name are on by their defaults (an older server `.jfc` records them).
- **Thresholds.** `ServerTick`, `LevelTick` and `KubeJSHandler` use JFR's own duration, so their jfc threshold
  applies. The slow-tick and chunk-wait events carry their time in a field and use fixed thresholds in `PerfEvents`
  (2 ms entity, 2 ms block entity, 1 ms chunk wait); their JFR duration is 0.
- **Totals.** The per-second totals are plain fields owned by the server thread and written as events at the end
  of the first tick after each second. KubeJS totals count server-thread calls only (client and startup scripts
  would race the flush); the slow-call event covers every thread. Totals are kept per event + script + line, so
  `/reload` reuses them.
- **Cost.** Two `System.nanoTime()` calls and a field update per entity and block entity tick (tens of
  nanoseconds), one event per tick and dimension, one per type and handler per second: an estimated 300 events/s and
  roughly 25 MB per 30-minute window on a busy server, inside `maxsize=512m`. A/B timing is SQ-PF-04.
- **Chunk waits** include the main-thread tasks the server runs while it waits (chunk promotions, queued packets),
  as `managedBlock` does; the report blames each recorded wait on the first mod frame past `getChunk`.
- **Hooks are optional** (`require = 0`): a mod that changes one of these methods leaves that event empty
  instead of crashing the server. `/ceperf status` lists hooks that never ran.
- **`/ceperf status`** (op level 3): the running recording, which pack events it records, hooks that have not run
  yet, and how many KubeJS handlers are wrapped. **`/ceperf dump [tag]`** saves the whole kept window (up to 30
  minutes) to `perf/recordings/server-<time>[-tag].jfr` on a background thread, like `dump.sh`, for admins without
  shell access. It cannot cut a shorter window (Java 17's API has no `maxage` for a dump); use
  `perf_report.py --last` instead.
- **Tests.** Headless (2026-10-26, scratchpad harness on the built classes and a real JFR recording): flags follow
  the recording, the KubeJS wrapper times and names handlers (3 slow calls, 3 and 100 calls in totals), nested and
  unbalanced chunk waits, `PerfParse` passes the events and the ChunkWait stack, and the report renders every
  table on a synthetic 20 s recording and still renders a 1.0.x recording with a note. Mixin application, the SRG
  targets in a live JVM and the overhead need the scratch server (SQ-PF).

## 4. Limits and decisions

**Blind spots of sampling, and what patches them (3.6, patcherMod 1.1.03 on):**

- **Waiting.** Waiting on the server thread isn't sampled. It waits for chunks in 0.1 ms parks, which also stay
  under the park threshold. Patched for chunk waits (`ChunkWait`); other waits (a mod's `join()` on a future)
  still only show as parks over 50 ms.
- **KubeJS.** Sampling sees KubeJS per event, not per script: Rhino interprets scripts, so no script name
  appears on the stack. Patched: `KubeJSHandlerStats` times each handler by script and line. Time inside one
  handler is still not split further.
- **No dimension, entity or position.** The runtime class is also not visible when a mob inherits `tick()`.
  Patched: `LevelTick` per dimension, totals per registered entity and block entity type, positions for single
  ticks over 2 ms. Many cheap ticks of one type at one place (a hopper wall) show as a type total, not a place.
- **Busy-seconds undercount.** JFR samples a few threads per period, so the busy-share estimate reads low
  while many threads are busy. Patched: the measured busy share is tick time over wall time.
- **Smoothed tick time.** The tick-time series is Minecraft's 100-tick average, which smears short spikes.
  Patched: `ServerTick` records every tick.
- **Still open:** per-mod shares stay samples (with their error bars); the client is not covered (no recording
  there); Windows scripts are untested.

**Rejected or deferred options:**

- **spark.** An interactive call tree rather than a ranked report, uploads to spark.lucko.me by default, and
  its fast engine is a native agent. Still worth using for bug reports to mod authors.
- **Observable.** Only covers entities and block entities. It remains the tool for "which one, at which
  position", and it is not in this pack's mods.
- **Vanilla `/jfr start`.** Its settings take a stop-the-world heap census (`jdk.ObjectCount`) and log every
  file read and write.
- **patcherMod JFR events (the user's option 3).** Deferred on 2026-09-28, built 2026-10-26 (3.6): per-tick
  and per-dimension timing, chunk-wait events with stacks, KubeJS handler timing by script, entity and block
  entity totals by type, `/ceperf status` and `/ceperf dump`.

## 5. File map

- [../server/LaunchServer.sh](../server/LaunchServer.sh), [../server/LaunchServer.bat](../server/LaunchServer.bat)
- [../server/perf/ce-server.jfc](../server/perf/ce-server.jfc), [../server/perf/README.md](../server/perf/README.md)
- [../server/perf/dump.sh](../server/perf/dump.sh), [../server/perf/dump.bat](../server/perf/dump.bat), [../server/perf/dump.ps1](../server/perf/dump.ps1)
- [../server/perf/report.sh](../server/perf/report.sh), [../tools/perf/ingest_report.py](../tools/perf/ingest_report.py)
- [../tools/perf/perf_report.py](../tools/perf/perf_report.py), [../tools/perf/PerfParse.java](../tools/perf/PerfParse.java), [../tools/perf/modindex.py](../tools/perf/modindex.py), [../tools/perf/report_html.py](../tools/perf/report_html.py)
- [../tools/build_deploy.py](../tools/build_deploy.py) (`server` category)
- patcherMod: [perf/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/perf/) (`PerfJfrEvents`,
  `PerfEvents`, `PerfStatsHolder`, `KubeJSHandlerTiming`, `PerfCommand`) and the mixins `ServerTickPerfMixin`,
  `ServerLevelTickPerfMixin`, `ServerChunkCacheWaitPerfMixin`, `LevelEntityTickPerfMixin`, `BlockEntityTickPerfMixin`,
  `EntityTypePerfStatsMixin`, `BlockEntityTypePerfStatsMixin`, `KubeJSEventHandlerListenMixin`,
  `KubeJSEventHandlerContainerMixin`
- Pack evaluation (section 10): [../tools/perf/eval/eval_server.sh](../tools/perf/eval/eval_server.sh),
  [../tools/perf/eval/eval_client.py](../tools/perf/eval/eval_client.py) (+ scratch-only `sp_drive.js`, `client_fps_log.js`),
  [../tools/perf/eval/entity_census.py](../tools/perf/eval/entity_census.py), [../tools/perf/eval/report_text.py](../tools/perf/eval/report_text.py),
  [../tools/perf/log_timings.py](../tools/perf/log_timings.py) (startup and join times from any `logs/` folder)
- Perf session (section 14): [../tools/perf/session/perf_session.sh](../tools/perf/session/perf_session.sh),
  [../tools/perf/session/collect.sh](../tools/perf/session/collect.sh), [../tools/perf/session/session_report.py](../tools/perf/session/session_report.py)
- Untracked output: `perf/recordings/`, `perf/parsed/`, `perf/reports/`, `perf/incoming/`, `perf/.cache/`, `perf/sessions/`

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

## 7. Known issues: third-party hot spots (fixed 2026-10-03)

Found in the first server profile. Each is a throttle in patcherMod (the jar, 1.0.4, built, not in `mods/`) or
a KubeJS change, with its dial in `config/creativeengineering-common.toml` under `[perf]` (read live).
Checked on a scratch dedicated server (all 347 server jars, new jar): it booted, the mixin export shows every
one applied, and there are no errors from them in the log. None of it is tested under real load.

| Issue | Fix | Where | Dial |
|---|---|---|---|
| Horse armor effect used too many resources, was disabled | Back on, but the all-entities walk runs once per interval (not every tick) and effects last interval + 40 ticks. **Also fixed:** the registration used `minecraft:strong_regeneration`, a potion id, so the horse never got any effect even before it was disabled; now `minecraft:regeneration`; since 2026-10-31 Regeneration II, Health Boost II and Resistance II (armor_effects.md F9) | `server_scripts/magic/armor_effects.js`, `startup_scripts/func/armor_effects.js` | `mountEffectIntervalTicks` = 100 |
| Quark feeding trough re-searched constantly | After a miss (no trough, no real player tempting) the goal skips the search for 30 s. Hits are never stamped, so cached troughs are unchanged | `FeedingTroughModuleMixin`, `TemptGoalTroughRetryMixin`, `compat/TroughRetryAccess` | `troughMissCooldownTicks` = 600 |
| Create: Gravity ran for every entity in every dimension | `ForgeEvents#handleLogic` is cancelled outside the listed dimensions (the End). An entity still carrying the low-gravity modifier runs it once more so the modifier is removed | `CreateGravityDimensionMixin` | `gravityLimitDimensions` = true, `gravityDimensions` = the End |
| Serene Seasons melt check every tick | `RandomUpdateHandler#onWorldTick` runs every 4th tick and the melt chance roll gets 4x the chance (`nextInt(bound / 4)`); same average rate for the mod's bounds 16/12/8/4 | `SereneSeasonsMeltThrottleMixin` | `seasonsMeltIntervalTicks` = 4 |
| Fruits Delight leaves tested the tag on every collision query | The handler's `is(BlockTags.LEAVES)` is rewritten to a projectile-first test (`FruitsDelightHooks.isLeafForProjectile`); same result. Mixin cannot inject into another mixin's handler, so this is a bytecode edit in a mixin config plugin. Moved to Creative Delight's `fruitsdelight` compartment 2026-10-27 (applies with Fruits Delight 0.5.11 only) | CD `mixin/fruitsdelight/FruitsDelightLeafCheckMixin` (empty anchor), CD `compat/CDMixinPlugin`, CD `compat/FruitsDelightHooks` | none (applies at launch) |

Notes:

- Snow Under Trees (the other half of the original melt-check finding) was removed from the pack in 1.0.2; only
  Serene Seasons' own handler is left.
- The gravity filter assumes gravity biomes exist only in the End dimension. The pack's `biome_configs` is
  `#minecraft:is_end` only. Add a dimension to `gravityDimensions` if that changes.
- The Fruits Delight patch does nothing if the handler is missing or looks different, so a Fruits Delight update
  can only lose the saving, never break the game. Check the mixin export for `isLeafForProjectile` after updating it.
- Trough: an ineligible animal (baby, in love) also counts as a miss, so its first search comes up to 30 s after it
  becomes eligible.

## 8. In-game test suite

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-ATC-02, -03, -04, SQ-PF-02, -05, SQ-WE-03, -04; not run SQ-WE-04b.

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

Pack events (3.6), scratch-queue rows SQ-PF-01 to -06:

- [x] **PF-14 hooks load:** PASSED 2026-10-26 after a fix (the 9 mixins sat in the client list and never applied on a
  dedicated server; moved to `mixins`): all hooks run, 320 KubeJS handlers wrapped, no mixin warning. boot with the recording -> no mixin warning for the 9 perf mixins; `/ceperf status`
  shows recording `ce`, all 10 events recorded, every hook run but chunk wait, and some KubeJS handlers wrapped.
- [x] **PF-15 dump and report:** PASSED 2026-10-26 (scratch nb2). `/ceperf dump smoke` -> `perf/recordings/server-...-smoke.jfr`; its report has the
  measured section: ticks, one row per dimension, KubeJS handlers named `server_scripts:...:line`, entity and
  block entity types.
- [ ] **PF-16 chunk waits:** partial 2026-10-26: one sync load (`spreadplayers`, no player to `/tp`) listed with
  caller `Level#getChunk` and phase; the player-teleport caller is still unchecked. in a fresh normal world, `/tp` into unexplored land a few times, then dump -> Chunk
  waits lists them with the caller (the teleport / player move).
- [ ] **PF-17 overhead:** partial 2026-10-27 (section 10): 5:26 with the events, 5:12 without, one run each; repeat the pair. Chunky pregen of 2601 chunks with the pack events on and with them off (copy of the jfc)
  -> the same time within noise (1:34 in the 2026-09-28 A/B).
- [x] **PF-18 reload:** PASSED 2026-10-26 (320 before and after). `/reload` twice, `/ceperf status` -> the wrapped-handler count does not grow; no KubeJS errors.
- [ ] **PF-19 client:** a client (no recording) boots and plays as before; `/ceperf status` in single player says
  no recording.

## 9. History

- 2026-10-27: Pack evaluation (section 10): server boot, pregen and idle on three scratch servers (DH on / events off /
  DH off), a throwaway client for startup, world join, FPS and single-player exploring. Biggest findings: Armor Trims'
  JEI page costs 58 s per join; Distant Horizons' server and client threads; heap near the cap; worldgen 3.5x slower than
  2026-09-28. PF-17 partial.

- 2026-10-26: Pack events built (3.6, TODO `server #9`): ten `creativeengineering.*` JFR events from 9 patcherMod
  mixins, `/ceperf status|dump`, the events in `ce-server.jfc`, `PerfParse` and `perf_report.py` read them (new
  "Measured by the pack's hooks" section). Headless JFR and report tests pass; patcherMod 1.1.03 rebuilt;
  live checks queued as SQ-PF-01 to -06.
- 2026-10-26 (scratch run nb2): the 9 perf mixins were in the `client` list of `creativeengineering.mixins.json`, so a
  dedicated server never applied them (`/ceperf status`: no hook run, 0 handlers wrapped); moved to the common list,
  patcherMod 1.1.03 rebuilt and installed 15:54. PF-14, PF-15, PF-18 PASSED, PF-16 partial; PF-17, PF-19 open.

- 2026-10-03: Known issues (section 7) fixed: 5 throttles in patcherMod `[perf]` plus the horse armor pass, and a
  wrong effect id found while doing it. Scratch-server boot and mixin export checked. patcherMod 1.0.4 jar built, not
  installed to `mods/`.

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

## 10. Pack evaluation, 2026-10-27

One session, the `mods/` jars of 2026-10-27 00:14 (patcherMod 1.1.03 23:36, CD 22:30; 368 server jars, 413 client
jars), Ryzen 5 5600H (12 threads), 14.5 GB RAM, Java 17. Server: three scratch dedicated servers on seed
-8188900049893920259 (`tools/perf/eval/eval_server.sh`, normal world with structures, `-Xmx6G` as shipped, JFR with
`ce-server.jfc`). Client: a throwaway client with the user's options (windowed, FPS uncapped, no pause on lost focus),
`-Xmx10240M` and G1 flags from the real launch, JFR from JVM start, the pe3 world copied into `saves/`; a scratch
server script idled at spawn, then teleported the player 6 times 1500 blocks apart in spectator
(`tools/perf/eval/eval_client.py`). Reports: `perf/reports/eval1-*`, `eval3-*`, `eval-client` (untracked).

### 10.1 Numbers

| Measure | Result | Earlier |
|---|---|---|
| Server boot, launch to `Done` (wall) | 225 s, 224 s (DH on); 212 s (DH off) | |
| of which: JVM start to datapacks loaded | 91 s | |
| of which: "Preparing level" to spawn region | 52 s (EarthFill structure veto 31% of worker samples, stronghold-ring biome search 29%) | |
| of which: spawn region (441 chunks) | 65 s (DH on), 62 s (DH off) | |
| `Done (...)` figure | 118.7 s, 118.6 s, 114.3 s | 51.2 s (2026-09-28, 338 jars) |
| Server `/reload` | 28 s | |
| Idle MSPT, no player (`/forge tps`, after 4 min) | 5.5 ms (events on), 4.8 ms (events off), 5.1 ms (DH off) | 1.3-1.7 ms |
| Pregen, Chunky radius 400 at 4000 4000 (2601 chunks) | 5:26 (events on), 5:12 (events off), 4:21 (DH off) | 1:34 |
| Pregen: heap after GC, `-Xmx6G` | 4.7-5.5 GB; 696 young GCs, 10.2 s of pauses, longest 1.19 s | |
| Pregen: per-chunk stage means | noise 177 ms, light 93 ms, features 81 ms, full 31 ms, biomes 18 ms | |
| Client launch to "Sound engine started" | 100 s; 97 s without ATLauncher's debug log config | user's logs: 63 s (May), 81 s (Jul), 82 s (Sep), 92 s (Oct median) |
| Client launch to usable title menu | about 139 s; about 133 s without the debug log config | |
| World join, Play clicked to playable (existing world) | 95 s to "joined", about 190 s to playable (cold); 180 s (second launch) | user's logs (integrated server start to joined only): 28 s (May), 31 s (Aug), 58 s (Oct median) |
| of which: datapack load before the integrated server starts | 62 s, 62 s | |
| of which: JEI start after joining | 89 s, 77 s; Armor Trims' trim page 58.7 s, 58.2 s | |
| Client FPS, uncapped (render distance 8) | 156-481, averages 174-403 at spawn and in new land | user caps at 30 |
| Single player integrated server, idle at spawn | median 26.5 ms, p95 48.5 ms, 73 ticks over 50 ms in 95 s | |
| Single player, exploring (6 teleports to new land) | 11.8 TPS, median 23.9 ms, 129 ticks over 50 ms, one 29 s tick; heap after GC 6.6-9.0 GB of 10 GB | |
| Single player, back at spawn | 20 TPS, median 12.0 ms, p95 14.3 ms | |
| Probe suites run (`/fdtest all`, `/cdjtest`, `/fgdtest all`, `/kttest`, `/sntest`, `/vtdtest all`, `/mgtest`) | median tick 15.7 ms, 15 ticks over 50 ms, longest 5.7 s (`/vtdtest` serialising 12,683 loot tables) | |
| Block states | 770,959 of 1,048,576 | same as 2026-10-26 |

Client startup and join times come from `python3 tools/perf/log_timings.py --months logs` (user's real launches, by
month). The log's join number starts at "Starting integrated minecraft server", so it leaves out the 62 s datapack
load and the JEI start: a real join is about 3x what the log says.

### 10.2 Findings, biggest first

1. **Armor Trims JEI page: 58 s of every world join (client).** `SmithingTemplateJEI#registerRecipes` calls Armor
   Trims' `ArmortrimRecipeMaker.getTrimmingRecipes`; each `TrimmableItem#applyTrim` calls
   `LargeItemLists.getAllTrimmable` -> `getAllItemsOfType` -> `getAllItems`, which rebuilds a list of every registered
   item each time (92% of the render thread in the JEI window; 110 MB/s allocation). It makes only 288 trim recipes.
   Fix: cache `LargeItemLists.getAllItems` / `getAllItemsOfType` (the item registry is frozen by then) with a
   patcherMod mixin, or build the 288 recipes from the pack's own list. Expected: join about 60 s shorter.
2. **Distant Horizons.** Server, no player: the DH LOD builder takes 71% of all CPU samples and 72 MB/s of
   allocation while idle; during pregen 37% of samples, 211 MB/s, and the longest tick 664 ms (62 ms without DH);
   pregen 25% slower (5:26 vs 4:21); boot 12 s slower. Single player: DH's world-gen thread is 42% of all client JVM
   samples, more than vanilla worldgen (20%); the integrated server fell to 11.8 TPS while exploring. `mod_sides.md`
   lists DH as optional on the server. Options: drop it from the server jar set, or cap its server threads; on the
   client, lower DH's generation threads / distant generation.
3. **Heap close to the cap.** Server `-Xmx6G`: 4.7-5.5 GB live after GC during pregen (the 1.19 s young pause and
   10 s of pauses per 5 min come from that). Client `-Xmx10240M`: 6.6-9.0 GB live while exploring (6.1 s of GC pauses
   in 5 min). Same pattern as the 2026-10-03 G1Full spiral (section 7 notes). The machine has 14.5 GB, so the client
   cannot simply get more; cutting DH and the item-list churn above lowers it.
4. **Worldgen cost grew 3.5x since 2026-09-28** (1:34 -> 5:26 for the same pregen; DH explains about 1:05 of it).
   Noise stage 177 ms per chunk (Tectonic density functions, Blueprint's modded biome source 6.9% of worker samples);
   the pack's own EarthFill (`EarthFill$1#column` from `EarthCheck#cancels` / `countEarth`) 7.6% of worker samples in
   pregen and 31% of the pre-spawn gap; PlacementPriority `predictSet` 14% of that; Twigs `ObeliskStructure#canGenerate`
   5.8%; copycats `customOcclusion` 2.5%. A column-height cache for EarthFill (same columns are sampled by `footprint`
   and `countEarth`) is the cheapest pack-side win.
5. **Cave plants pop off as items when a chunk first ticks.** After boot the spawn area held 672 item entities, 98%
   below y 50: 299 moss carpet, 65 wheat seeds, 58 cocoa beans, 53 azalea, 42 IE hemp seeds, 35 flowering azalea,
   glow berry pips, Miner's Delight gossypium and cave carrots, 16 torches; plus 227 falling sand / gravel entities.
   Chunks that were only pregenerated hold none, so it happens when chunks become ticking around a player. With
   them ticking, item entities were 29% of tick time in the probe window (458 per tick, 14 µs each with ItemPhysic).
   Cause traced 2026-10-27, fix built: section 10.6.
6. **KubeJS per-level tick handlers.** `armor_effects.js:81` and `dragon_autorepair.js:73` (`LevelEvents.tick`) run
   in every one of 16 dimensions every tick: 66,272 calls each in 211 s, 0.6 ms per tick together, single calls up to
   555 ms. One `ServerEvents.tick` handler that checks the interval first would cut this to one cheap call per tick.
7. **MCA villager spawn stalled the server 5.1 s** during pregen: `forge.net.mca.util.WorldUtils#spawnEntity` loaded an
   ungenerated chunk on the server thread (ChunkWait event).
8. **Client startup:** CTM's `TextureMetadataHandler` is 14% of all startup samples; ATLauncher's debug log config adds
   about 5 s and writes a 162 MB `debug.log` and 5,575 dumped class files (44 MB, `/tmp/classDump*`, 173 MB there now) per
   session.
9. **Pack events overhead (PF-17, SQ-PF-04):** pregen 5:26 with the events on, 5:12 off (one run each, 4%); idle 5.5 vs
   4.8 ms. Not decided: needs a repeat pair before calling it noise.

Not a problem: client frame rate (uncapped 156-481 FPS); the dimension count (15 empty dimensions about 0.07 ms each).

### 10.3 Fix for finding 1: Armor Trims list cache (built 2026-10-27, scratch server PASSED 2026-10-27, client check open)

Bytecode read of Armor Trims 1.3.3 `LargeItemLists`: `getAllItemsOfType(Class)` copies the whole item registry
(`getAllItems`) and grows its result one `ArrayUtils.add` at a time; `getAllTrimmable` calls it for each class in
`allTrimmableClasses` (only `ArmorItem` in this pack) and adds the items of `allTrimmableTags`. Callers besides the
JEI maker (`TrimmableItem#applyTrim`): the trimmed-item decorator mixin (every rendered stack), the smithing screen
(every frame) and `UntrimmingSpecialRecipe#matches` / `assemble` (every crafting grid change). All only read the
list or call `contains`.

`mixin/ArmorTrimsLargeItemListsMixin` (HEAD + RETURN injects on both methods) and `compat/ArmorTrimsItemCache`:

- nothing is cached until `ForgeRegistry#isLocked` on the item registry, so Armor Trims' common-setup calls run as before;
- `getAllItemsOfType`: one read-only copy per class for the session;
- `getAllTrimmable`: one read-only list per tag generation (bumped on `TagsUpdatedEvent`) and per size of the two
  Armor Trims lists; `contains` answers from a set. Same entries and order as Armor Trims builds.

Tests: SQ-ATC-01 to -04 (scratch server, probe `tools/armor_trims_cache_test/atc_probe.js`), CQ-ATC-01 to -03
(client, JEI start time and trim rendering) in [scratch_queue.md](testing/scratch_queue.md).

### 10.4 Fix for finding 6: horse walks (built 2026-10-27; scratch 2026-10-27 found `level.players()` broken, fixed; player check PASSED on a bot 2026-10-28, SQ-HT-02 / -03)

The horse passes of `server_scripts/magic/armor_effects.js` (mount effects) and
`server_scripts/equipment/dragon_autorepair.js` (horse armor repair) moved from `LevelEvents.tick` to one
`ServerEvents.tick` each. The handler checks its interval first (`[perf] mountEffectIntervalTicks`,
`[dragon_gear.autorepair] intervalTicks`), then walks `getAllEntities()` only in levels whose `players()` is not empty.
Before: 16 Rhino calls per tick per script, and a full entity walk of every loaded dimension each interval. After:
1 call per tick per script, which returns at the modulo check on all but one tick per interval.
Side effect: a horse in a loaded dimension with no player in it (spawn chunks, chunk loaders) no longer gets the
effect or the repair until a player enters. The player-slot passes (`PlayerEvents.tick`) are unchanged.
Tests: SQ-HT-01 to -03 in [scratch_queue.md](testing/scratch_queue.md), AE-11 / AE-12 in game.

### 10.5 CTM model bake (finding 8, investigated 2026-10-27; CTM removed 2026-10-27, out of 1.1.04)

`eval-client.jfr`: 1,547 samples in `team.chisel.ctm.client.util.TextureMetadataHandler#onModelBake`, all on the
render thread, from 00:54:36 to 00:54:56 (about 20 s of startup; the handler also runs on every resource reload).
For every baked model it calls `getMaterials` on the root unbaked model, then on each dependency, and reads the CTM
metadata of each texture. All states of a multipart block share one `MultiPart`, so the whole texture walk repeats
once per state: `MultiPart#getMaterials` / `getDependencies` are 741 of the 1,547 samples, `MultiVariant` 131,
`BlockModel` parent resolution most of the rest. The metadata lookup itself is cached (`ResourceUtil.metadataCache`,
about 160 samples).

What uses CTM in this pack:
- Hard dependencies: none. Fantasy Furniture (optional, `side BOTH`) and FramedBlocks (optional, client; lets framed
  blocks connect when camouflaged with a CTM block) only.
- Model or blockstate JSON with a CTM loader: none in any jar or resource pack.
- CTM texture metadata that renders: Pneumaticcraft's pressure chamber window, FTB Quests' open barrier. Every
  Compat ships CTM metadata for Chipped variants (Chipped is not installed); Fantasy Furniture's
  `mod_support/ctm` pack is not enabled in `options.txt`.

Options were: remove CTM (those two blocks lose connected textures, about 20 s back), or a patcherMod mixin caching
`getMaterials` / `getDependencies` per unbaked model inside `onModelBake`. No 1.19.2 Forge replacement found.

**Done 2026-10-27: CTM removed.** Jar moved to `disabledmods/`, `mod_sides.md` row deleted, `pack/manifest.json` and
`MODLIST.md` regenerated (412 mods). Test: CQ-CTM-01 in [scratch_queue.md](testing/scratch_queue.md).

### 10.6 Fix for finding 5: no drops from first-tick water (built 2026-10-27, scratch server PASSED 2026-10-27)

Cause: generated water flowing for the first time. Vanilla `ChunkMap.prepareTickingChunk` calls
`LevelChunk.postProcessGeneration` when a chunk first becomes ticking (not when it reaches FULL, so pregen never runs
it). That ticks every fluid in the chunk's `PostProcessing` marks (aquifer and carver water edges), and the fluid ticks
that springs scheduled during generation fire on the first ticks. Water that was standing as a wall then flows into
caves: `FlowingFluid.spreadTo` replaces moss carpet, grass (wheat and hemp seeds), azalea, cave vines (glow berries),
torches, cocoa and Miner's Delight plants with drops, and the block updates of the flow make gravel and sand fall.

Evidence, read-only scan of the pregenerated, never-inhabited FULL chunks of `saves/WorldG` (603 chunks, 30 region
files; scripts in the session scratchpad):
- about 900 `PostProcessing` marks per chunk, almost all water; plus 13,155 pending water `fluid_ticks` below y 50;
- of 21,000 cave plants below y 50 only 11 stand on air, so placement is not the cause;
- 389 water marks or ticks border open space (367 below y 50); a flood fill from them inside each chunk reaches
  100 grass, 62 moss carpet, 22 azalea / flowering azalea, 10 cave vines: the same mix as the eval's items.

Fix (patcherMod `worldgen/FirstTickWater`, `LevelChunkFirstTickWaterMixin`, `FlowingFluidSettleMixin`,
`BlockPopResourceSettleMixin`). The water moves as in vanilla; only the items are dropped:
- `postProcessGeneration` HEAD opens a settling window on the chunk when it has `PostProcessing` marks or
  `InhabitedTime` 0;
- a fluid tick in a settling chunk, or at a position settling water spread to (carried by `spreadTo` and refreshed on
  each spread, so a flood keeps the rule past the chunk edge), is settling;
- while a settling fluid tick runs, `Block.popResource` (the private `Supplier` overload every block drop goes through)
  spawns nothing. That covers the blocks the water replaces and the plants that lose their support in the same tick
  (cave vines, carpets on a washed block, torches). Lava drops nothing in vanilla anyway;
- nothing runs during generation (no startup or pregen cost); outside a window a fluid tick costs one map lookup.
- Config: `[worldgen.systems] firstTickWater` (default on; off = vanilla drops) and `[worldgen.first_tick_water]
  windowTicks` (default 600 = 30 s, 0 to 72000; 0 = no window). Both apply at once.

Changed 2026-10-27 at the owner's request: the first build made settling water flow around plants (`canHoldFluid`
refused them) so they stayed in place; now the water flows as in vanilla and the plants it reaches are removed without
items.

Side effects: inside the window a player's water, or any block a fluid tick breaks there, also drops nothing (only in
never-visited land). Not covered: drops from later scheduled block ticks (sugar cane, cactus, bamboo checking their
support a tick after the flow), and the falling gravel and sand from generated `block_ticks`.
Tests: SQ-FTW-01 to -05, CQ-FTW-01 in [scratch_queue.md](testing/scratch_queue.md); count with
`python3 tools/perf/eval/entity_census.py WORLD`.

### 10.7 Finding 4 traced: EarthFill and the noise stage (investigated 2026-10-27; options 1-4 built, 10.8)

Source: `perf/recordings/eval1-pregen.jfr` (Overworld only, 52,720 worker samples), stacks read from
`perf/parsed/eval1-pregen`; density functions read from the Tectonic 2.3.5a and Terralith 2.3.12 jars. Shares are
of all worldgen worker samples.

**One root cause under both.** `ChunkGenerator#getBaseColumn` / `getBaseHeight` (`iterateNoiseColumn`) is 19.7% of
worker CPU. Callers: EarthFill 8.7%, Twigs' obelisk 5.1%, vanilla 2.6%, MES 1.1%, PlacementPriority 1.0%,
MetaStructure 0.8%. Each call builds a new `NoiseChunk` (its constructor re-wraps the whole noise router and hashes
every density-function record, `Objects#hashCode` + `HashMap$TreeNode`: 31% of an EarthFill column read), then
evaluates the full 384-block column with 2x2 interpolation corners (about 40x more corner work per column than a
chunk fill).

**Noise stage (fill 27.9% of worker CPU, 168 ms per chunk):**

| Part | Share of worker | Cause |
|---|---|---|
| Tectonic `legacy/cliffs` per block | 9.9% in fill + 3.6% in column reads | terratonic's `final_density` ends in `min(..., tectonic:overworld/legacy/cliffs)`, outside `interpolated`, so vanilla's per-block `cache_all_in_cell` evaluates it for every block. Its inner spline's coordinate is `y + 2 * (noise offset/1 + noise offset/2)` and its nested points read `noise cliffs/rarity`: three 2D noises (`y_scale 0`) with no `flat_cache`, sampled per block. 35% of fill |
| Aquifer `computeSubstance` | about 4% | vanilla aquifer, per block |
| Material rules (aquifer + ore veins) | about 2.7% | vanilla |
| Beardifier | about 1.5% | vanilla + soft beard (`TerrainAdaptation#addSoft` 0.4%) + YUNG |
| Tectonic Java DFs | 2.4% | `ErosionNoiseDensityFunction` 1.5%, `DynamicReferenceDensityFunction` 0.8% (Terralith extras; its `mapAll` does not visit its targets, so NoiseChunk caches never reach inside) |

The pack's `kubejs/data/minecraft/worldgen/noise_settings/overworld.json` (a copy of Terralith's, written by
`gen_nether_worldgen.py` for the surface rules) has `size_vertical 1`; Tectonic's own has `2`. That only doubles
interpolation corners (`advanceCellX` + `initializeForFirstCellX`, 7% of fill), so it is not the main cost.

**EarthFill (8.8% of worker CPU, 31% of the pre-spawn gap):** 99.7% is column reads (`predicted` -> `getBaseColumn`);
the counting itself is negligible. Paths: real starts 72%, PlacementPriority predictions 18%, `skipVetoedSlot`
meta re-tries 10%. The column cache lives for one `filter` call, so each start, each prediction and the later real
start of the same chunk read their columns again. Inside a read: NoiseChunk build 31%, the cliffs term 15%.

**Structures that cannot place in this dimension.** 1.19.2 runs every structure set in every noise dimension and
checks the biome only after `findGenerationPoint` (1.19.3 added the up-front filter). In this Overworld-only pregen:
Twigs' bloodstone obelisk (Nether, 36 `getBaseColumn` per try) 5.1%, `NetherFossilStructure` 1.3%, MES End
structures 1.1%, and an unknown part of `JigsawStructure` (2.1%, assembles before the biome check).

**Options, best first:**

1. **Skip impossible structures (patcherMod, exact).** HEAD of `tryGenerateStructure` (beside `skipVetoedSlot`):
   return false when the structure's biome set has no biome in the generator's `possibleBiomes()`. The weighted pick
   still rolls the same randoms, so every world generates the same. About 7.5% of worker CPU, plus jigsaw time.
2. **Cache the cliffs noise (datapack override of `tectonic:overworld/legacy/cliffs`).** Wrap the offset sum and the
   rarity noise in `interpolated(flat_cache(...))`, keep `y` exact. 2D noise drops from per block to per column;
   up to 13.5% of worker CPU, about a third of the noise stage. Not exact: cliff tops move by a fraction of a block,
   so new chunks next to old ones can show a seam at cliffs. Needs a decision for existing worlds.
3. **Shared EarthFill column cache (patcherMod).** One bounded cache per `RandomState` and grid cell, shared by
   predictions, retries and the real start, storing only what `Terrain` needs (top, top-is-water, air/water/solid
   runs) instead of a 384-entry `NoiseColumn`. Exact. Removes the repeat reads (at least the 28% from predictions
   and retries). Twigs / MES `getBaseColumn` could go through the same cache with a redirect.
4. **`sampleGrid` 2 -> 4** (config only). 4x fewer cold reads; terrain between 4-block noise corners is linear except
   the cliffs term. Rerun `tools/earth_fill_sim.sh` first to check cancel rates.
5. **Reuse predicted starts (NOT TO BE BUILT, owner decision 2026-10-27).** PlacementPriority's prediction already builds the start the chunk will later make;
   keeping the `StructureStart` (not only boxes) and handing it to the real `tryGenerateStructure` would skip the
   second assembly and EarthFill pass. Larger change; touches the determinism contract above.
6. Not worth it alone: `size_vertical` 2 (7% of fill, changes terrain); a NoiseChunk wrap cache (record hashing,
   about 2% of worker; options 1 and 3 cut the calls instead).

### 10.8 Worldgen hot spots built: options 1-4 of 10.7 (2026-10-27)

Owner decision 2026-10-27: build options 1-4, the cliffs change to terrain accepted. Option 5 is NOT TO BE BUILT (owner decision 2026-10-27: small gain, touches the determinism contract; TODO
`perf #13`). Status: BUILT, installed 2026-10-27 15:14, scratch server PASSED 2026-10-27 except SQ-WGP-06 (cliffs alone) and the client rows
(SQ-WGP-01 to -08, CQ-WGP-01/-02 in [scratch_queue.md](testing/scratch_queue.md)).

| Option | What was built | Exact? | Switch |
|---|---|---|---|
| 1. Skip impossible structures | `worldgen/DimensionFilter` + the end of `ChunkGeneratorStructureFrequencyMixin#skipVetoedSlot` (HEAD of `tryGenerateStructure`, after the veto and the meta-slot decision): returns false when no biome in the generator's `possibleBiomes()` passes `MetaSlots.canSpawnAmong` (biome rules and fits applied). Meta structures are not filtered. Cached per (biome source, structure), cleared on `TagsUpdatedEvent` and `ServerStoppedEvent`. Census reason `DIMENSION` | Yes: the biome rules and the replacement narrowing only narrow the biome test, the pick's randoms are rolled by the caller | `[worldgen.systems] dimensionFilter` (true) |
| 2. Cache the cliffs noise | `kubejs/data/tectonic/worldgen/density_function/overworld/legacy/cliffs.json`: Tectonic 2.3.5a's file (MIT, the same in its `tectonic` and `terratonic` packs) with the `2 * (offset/1 + offset/2)` term of the y spline's coordinate and both `cliffs/rarity` coordinates wrapped in `interpolated(flat_cache(...))`; `y` stays exact. 3 noises per block become 3 per 4x4 column corner | No: those terms are bilinear across each 4x4 cell, so cliff faces move by a fraction of a block; new chunks next to old ones can show a step (CQ-WGP-01) | delete the file (pack data; it outranks Tectonic only if KubeJS's folder pack sits above `tectonic/terratonic`, SQ-WGP-02 checks) |
| 3. Shared terrain cache | `EarthFill.predicted` reads through a Guava cache per (generator, RandomState, grid, min y, height), 65,536 columns each, cleared on `ServerStoppedEvent`; a per-start map still fronts it. Columns are packed 2 bits per y (air / water / other) with top and top-is-water, about 100 bytes. `getBaseColumn` is not touched by any pack mixin (DistantClip only clamps `fillFromNoise` and `NoiseChunk#forChunk`), so a cached column is what a fresh read returns | Yes (offline: EarthFillTest 7, 2000 random columns, 0 mismatches) | `[worldgen.systems] sharedTerrainCache` (true) |
| 4. `sampleGrid` 4 | CEConfig default 2 -> 4, the instance toml too; `tools/earth_fill_sim.sh` takes `GRID=n` (default 4) | No: earth/water counts at shores and cliffs move by a few blocks; cancel rates below | `[worldgen.earth_fill] sampleGrid` |

Expected gain from the 10.7 shares (of worker CPU in the eval pregen): option 1 about 7.5% plus jigsaw time, option 2
up to 13.5%, option 3 at least the 28% of EarthFill's 8.8% that predictions and retries re-read, option 4 most of the
rest of EarthFill's cold reads. Measured 2026-10-27 (10.9): pregen 5:26 -> 3:50, `Done` 119 s -> 86 s.

**`sampleGrid` 4 vs 2 offline** (`GRID=2|4 tools/earth_fill_sim.sh 4242 60`, vanilla terrain, 60 sites each, 2026-10-27):
cancel rates at every candidate limit pair moved by at most 4 points (1-2 starts of 60); medians and p90 of earth over
water / air moved by at most 7 blocks. Cancelled % at (24, 64) / (48, 128), grid 2 -> 4: plains 67/35 -> 70/33, desert
77/48 -> 77/52, savanna 95/88 -> 95/90, snowy 77/48 -> 78/45, taiga 78/45 -> 75/45, mansion 13/0 -> 12/0; outpost and
igloo 0 in both. No family shifts one way, so grid 4 is kept.

### 10.9 Measured on scratch servers: did the fixes help? (2026-10-27 evening)

Yes. Same seed, square, jar set and machine as the 10.1 evaluation; one scratch server per world, one at a time
(`tools/perf/eval/wgp_run.sh`, series and group A run in [testing/scratch_queue.md](testing/scratch_queue.md) Run log).
Jar: patcherMod 1.1.03 built 13:45 (options 1-4, Armor Trims cache, first-tick water), installed 15:14 and hard-linked
into every world, so all seven ran the same build. DH on in all. `wgp_old` = every new option off and first-tick water
off: the same-day "before"; `wgp_new` = as shipped.

| Measure | Eval (10.1, morning) | `wgp_old` (all off) | `wgp_new` (as shipped) | `wgp_new_eo` (as shipped, pack events off) |
|---|---|---|---|---|
| `Done (...)` | 118.7 s | 119.3 s | **85.8 s** | 101.0 s |
| Launch to `Done` (wall) | 224 s | 222 s | **190 s** | 180 s |
| Idle MSPT at Done + 4 min (`/forge tps`) | 5.5 ms | 4.8 ms | **4.5 ms** | 4.9 ms |
| Pregen, Chunky radius 400 at 4000 4000 (2601 chunks) | 5:26 | 4:53 | **3:50** | 3:48 |
| Item entities at spawn, Done + 2 min | 672 | 593 (FTW off) | **72** | 72 |
| MSPT right after the pregen | | 14.1 ms | 11.2 ms | 10.2 ms |
| `/celocate census 96` (100,765 slot chunks) | | 886 s | **424 s** | not run |
| Pack share of worldgen CPU (Worker-Main, JFR) | 7.6% (EarthFill) | 11.2% | **5.3%** | |

Pregen 21% faster than the same-day all-off world and 29% faster than the morning eval; `Done` 28% faster. The
4:53 vs 5:26 gap between the eval and `wgp_old` (same options) is the horse-walk change (10.4) and run-to-run noise.

**Per option** (each world turns on one option against `wgp_old`; time to 500 / 2601 chunks from Chunky's progress lines):

| World | Option | `Done` | to 500 | Pregen | Idle MSPT | Exact? |
|---|---|---|---|---|---|---|
| `wgp_old` | none | 119.3 s | 79 s | 4:53 | 4.8 ms | |
| `wgp_df` | 1. DimensionFilter | 113.4 s | 64 s | **4:16** | 4.4 ms | yes: fill lines a subset of `wgp_old`'s; no structure lost (SQ-WGP-03) |
| `wgp_tc` | 3. shared terrain cache | 116.4 s | 78 s | 4:50 | 4.5 ms | yes (SQ-WGP-04) |
| `wgp_g4` | 4. `sampleGrid` 4 | **92.6 s** | 72 s | 4:33 | 4.3 ms | no (as designed) |
| `wgp_cl` | 2. cliffs override | 118.0 s | 90 s | stalled (below) | 6.4 ms | no |
| `wgp_new` | 1-4 | 85.8 s | 51 s | 3:50 | 4.5 ms | |

- The DimensionFilter gives the most pregen time (37 s); `sampleGrid` 4 gives most of the boot gain (27 s of `Done`,
  the EarthFill pre-spawn work). The terrain cache alone is within noise for pregen (it saves re-reads that grid 4
  already makes cheap).
- Cliffs alone is NOT measured: its world stalled (below) and ran with a high idle MSPT (6.4 ms against 4.3-4.9 in the
  rest), so the machine was likely busy (a parallel session's build). Indirect evidence that it helps: the others add
  up to about 56 s to 500 chunks, `wgp_new` took 51 s; Tectonic's `ImprovedNoise#p` was the top Worker-Main frame in
  `wgp_old` (14%) and is gone from the top in `wgp_new`. Rerun `wgp_cl` (SQ-WGP-06) for a number.
- Per-stage means from `minecraft.ChunkGeneration` are wall times on contended threads: `wgp_new` and `wgp_new_eo`
  (same pregen time) read noise 96 vs 223 ms. Use pregen wall time and Worker-Main shares, not stage means.
- Pack events (PF-17, SQ-PF-04): 3:50 on, 3:48 off. With the eval's 5:26 / 5:12 the pair says no measurable cost.
- Structures: census `wgp_old` 2858 starts, `wgp_new` 2855; 6 structures differ by 1-5 (options 2 and 4 move terrain).
  The census shows 5 / 10 false negatives in both worlds, all Terralith underground (`old_refinery`,
  `mining_outpost`, reason biome): a prediction gap that predates these options (TODO `worldgen/locate`).
- The DimensionFilter skips 68 of 466 structures in the Overworld (Nether-only, End-only and never-placeable ones such
  as Beautify's botanist houses, 66 slots and 0 starts in the filter-off census), 421 in the Nether, 415 in the End,
  328-429 in the pack's own dimensions.

**Other fixes, same run:**

- Armor Trims cache (10.3): 288 calls of `getAllTrimmable` / `getAllItemsOfType` take 0-2 ms in total (uncached about
  0.2 s each); lists match a registry scan (226 items), rebuilt after `/reload` (SQ-ATC-01 to -04 PASS). The join-time
  gain itself needs a client (CQ-ATC-01).
- First-tick water (10.6): 593 -> 72 items at spawn; what is left is mostly gravel from falling blocks (245 falling
  blocks remain, as expected), 14 moss carpet, glow berries. Outside the window a washed torch drops as in vanilla;
  inside it, no drop; `windowTicks = 0` drops again (SQ-FTW-01, -02, -04, -05, -06 PASS).
- Horse walks (10.4): the new handlers threw `TypeError: Cannot call property players` every interval in every world
  (`level.players` is a KubeJS bean property, not a method), so mount effects and the dragon horse armor repair never
  ran. Fixed to `level.getPlayers()` in both scripts; no error after the fix (SQ-HT-01). SQ-HT-02 and -03 need a player.

**Problems found:**

1. **Chunk saved corrupt (third party, rare).** In `wgp_cl` the IO worker hit a `ConcurrentModificationException` in
   `CompoundTag.write` while storing chunk [254, 261]: another thread changed the chunk's NBT while it was being
   written. The chunk file was then unreadable (`Failed to read chunk`) and Chunky waited on it forever (stalled at
   513 of 2601). Once in 7 worlds, never in the user's logs; no pack code hooks chunk saving. Suspects: DH's server-side
   LOD builder, mods that write into the chunk tag on save. Trace in `perf/wgp/wgp_cl_stalled/latest.log.gz`. TODO `perf #23`.
2. The parallel session's patcherMod installed 17:47 crashes at mod loading: `ImmersivePostsValidItemMixin` uses
   `@Redirect(method = "*")` with a static handler, which also matches instance methods of `PostBaseBlock`. Reported to
   that work (TODO `perf #21`); it blocked the `wgp_cl` rerun.

## 11. Load-time study: pack load, world open, server start (2026-10-27)

Section 10 measured load times; this section says what fills them. Sources: the user's real launch of 2026-10-27
00:31 (`logs/latest.log`, world `Workspace`, before CTM was removed), the eval recordings `eval-client.jfr` and
`eval1-startup.jfr` (parsed in `perf/parsed/`). Seconds are the wall span of a method's samples in the recording
(first to last sample). The method was checked against Armor Trims: 58.7 s, the same as 10.2 #1. Shares are of the
named threads in the window. Nothing was changed; status: INVESTIGATED.

### 11.1 Where a single player join goes (user's run, Play clicked to "joined" = 255 s)

| Phase | Wall | What fills it |
|---|---|---|
| Registry snapshot from the world | 12 s | `GameData#injectSnapshot` -> `ForgeRegistry#bake` -> `BlockCallbacks#onBake`: the state-id map of 770,959 states rebuilt (10.5 s in the eval) |
| Generated packs | 23 s | Every Compat server data 12.75 s (+1.8 s the second time), Immersive Weathering 1.5 s; the Vinery / Miner's / Farmer's Structures / Rustic donor reads 3 s |
| `biome_overrides.js` | 12 s | `BiomeLayoutTuner#run` re-solves the biome layout (10 s in the eval, 11 s on a dedicated boot) |
| Worldgen registries | 10 s | datapack worldgen load (`RegistryLoader`) |
| Reload listeners | 28 s | TagManager 17.3 s, RecipeManager 21.5 s (KubeJS recipe events 12.2 s), LootTables 8.4 s, advancements 7.0 s for 0 kept (run in parallel; 20.2 s blocking) |
| After the reload | 23 s | unattributed in the log; vanilla `BlockStateBase#initCache` for all states after tags load is 22% of the eval's datapack window (9.8% of a dedicated boot) |
| Level creation | 32 s | silent in the log; in the eval this window is 67% stronghold ring placement (11.3) |
| Spawn region and server start | 87 s | 44 s silent between the FTB Quests load and the next line: the point where `ServerStartedEvent` runs, which is where WorldEdit's init sits (11.2; 15 s in the eval) |

### 11.2 Findings, by seconds saved per start

1. **WorldEdit platform init: 13 s per server boot, 15 s per single player world open.** On `ServerStartedEvent`,
   WorldEdit 7.2.12's `Capability.WORLD_EDITING` init walks `getAllStates` of every block type to register internal
   ids (`BlockState#generateStateMap` / `populate`): 63% of the server thread in the eval's start window. It scales
   with the state count. Options: A, leave WorldEdit out of the player and server jar sets and keep it in the dev
   instance (`mod_sides.md` lists it `both`); B, a patcherMod mixin that defers the registration to first use.
   B BUILT 2026-10-27 (TODO server #17). How the init works: `Capability$4.ready` loops `BlockType.REGISTRY`,
   calls `getAllStates()` (which runs `BlockState.generateStateMap`: a cartesian product of property values, a
   `TreeMap` and an `ImmutableMap` key and a `populate` neighbour table per state), then for each state
   `ForgeBlockRegistry.getInternalBlockStateId`, which maps it back to the vanilla state through
   `ForgeTransmogrifier` and returns `Block.getId`. The stored id is only vanilla's `BLOCK_STATE_REGISTRY` id, so
   the table can be filled on demand. Both `ForgeAdapter.adapt` directions already fall back to
   `ForgeTransmogrifier` when an id is missing. The fix: `WorldEditSkipStateIdInitMixin` cancels `ready`;
   `WorldEditBlockStateIdAccessMixin` gives `getBlockStateId` a lazy `Block.getId(transmogToMinecraft(state))` and
   records it in a `ConcurrentHashMap` read by `getBlockStateById` and emptied by `clear()`. WorldEdit state objects
   are then only built for block types an edit touches. Open: `ForgeWorldNativeAccess` also reads
   `BlockStateIdAccess`; investigate whether it needs the same path. Tests: scratch queue SQ-WE-01 to -04.
2. **Stronghold ring placement: 12 s (dedicated) and 15 s (client) on worker threads, every server start.** 1.19.2
   recomputes the 128 ring positions on each start (`ChunkGenerator#generateRingPositions`, a `findBiomeHorizontal`
   per stronghold through Blueprint's `ModdedBiomeSource` and the Tectonic climate noise). It blocks the first chunk
   that needs structure starts, so a new world's "Preparing level" waits on it (29% of that gap in 10.1). Fix: a
   patcherMod cache of the positions in the world's data, keyed by seed, the stronghold structure set and a biome
   source fingerprint. Exact. BUILT 2026-10-27 (TODO server #18): `ChunkGeneratorRingCacheMixin` on
   `generateRingPositions` (`m_223148_`); HEAD returns a completed future when `RingPositionCache` has the key,
   RETURN chains a store onto vanilla's future on a miss. File: `<world>/data/creativeengineering_ring_positions.json`
   (SHA-256 key -> set id + chunk x,z pairs, last 32 keys, written via a temp file). Key: seed (0 when flat), set id,
   distance / spread / count, preferred biome ids, biome source class, its codec encoding (Blueprint's codec writes
   only the wrapped vanilla source) and possible biomes, mod list with versions, selected data packs, and a probe of
   biome + raw climate point at 64 angles on every ring radius (covers Blueprint's slices and Tectonic's noise). The
   key costs tens of ms (the multi-noise encoding is the largest part). A changed input is a new key and a normal
   compute. No server (`ServerLifecycleHooks`) or a key error: vanilla path. Each lookup logs one `[rings]` line in
   `creativeengineering/worldgen`. Tests: scratch queue SQ-RING-01 to -03.
3. **`BiomeLayoutTuner` solve: 10-11 s on the first datapack load of every JVM** (server boot, each single player
   launch). The result is cached in memory only (`cacheKey`), and its inputs (config, baselines, climate samples)
   rarely change. Fix: keep the result files on disk under the same fingerprint and read them back when it
   matches. Exact. BUILT 2026-10-27 (TODO server #19): `kubejs/generation_scripts/worldgen/biome_layout_cache.json.gz`
   holds the last clean solve (format, key, the dimension files, the messages); `run()` checks memory, then the
   file, then solves. Key: the existing fingerprint (solver settings, config, baselines, samples) plus a SHA-256 of
   the solver's own class files (`BiomeLayoutTuner`, `$Quant`, `$DimResult`), so a changed solver re-solves. The hash
   is of the reobf'd classes in the jar, which differ from `build/classes`: a cache seeded headless must use the
   jar's classes. A run with errors is not written; a missing, corrupt or other-key file is a solve. The file ships
   (`build_deploy.py` runtime row), so players skip the solve too while the inputs and jar match; else they solve
   once and the file is rewritten in their instance. Headless (`tools/test_worldgen_tools.sh` #11, 14 checks): disk
   hit equals the solve, config untouched, wrong key / corrupt / edited sample re-solve. Seeded with the installed
   jar's classes: solve 9.3 s, disk hit 0.68 s in a cold JVM (the input hash included). Tests: scratch queue
   SQ-BLC-01 to -03.
4. **Item merge swaps: about 4 s + up to 5 s of the 12 s recipe event, every datapack load and `/reload`.**
   `global.itemMergeSwaps` runs twice (pack, then Creative Delight). Each run does 15 pairs x 2 sides, and each one is a
   KubeJS filtered scan of all data recipes plus a Rhino walk of every added recipe (`NCH._replaceInAdded`). The
   Creative Delight run repeats the data-recipe scan the pack run already swapped. Fix: the second run walks only the added
   recipes; both walk the added recipes once with an id lookup instead of 30 filtered scans. Exact. The 5 s
   estimate is a log gap that also holds Creative Delight's other late scripts. BUILT 2026-10-27 (TODO server #20):
   the merges are split into a `pack` group (rocky dirt, the 92 removed-wood pairs) and a `creativedelight` group (the
   14 food pairs), each swapped by one run only; `NCH.replaceItems` does one filtered pass over the data recipes and
   one prefiltered walk of the added ones per run (item_merges.md 1.1). The pair count had grown to 107 since the
   measurement, so the old cost was higher than above. Tests: scratch queue SQ-IMS-01 to -04.
5. **Block state count (770,959) multiplies the rest.** It drives the snapshot bake (10.5 s), `initCache` after
   every tag load (boot, join, `/reload`), WorldEdit (finding 1) and FerriteCore's maps. Largest holders (state dump of
   2026-10-04): Immersive Posts 75k (15 materials, each a truss at 3,072 and a post at 1,920; cut to 6 materials,
   30k, on 2026-10-27, see 11.3), Every Compat 50k, Create Deco 43k, Immersive
   Weathering 41k (3 sandy walls at 7,128 each), Quark 33k, Create 31k, Redeco 28k; single blocks: Pretty Pipes and
   PP Fluids pipes 8,192 each, ComputerCraft cable 6,400. Each 10% of states cut is roughly 1 s off the snapshot bake.
6. **Every Compat: 12.75 s of server data on the first load,** 1.8 s on the second, 1.45 s of client assets, 50k
   states; 113 wood types x 8 mod modules all on in `everycomp-wood_types.toml`; 44 "recipe template returned null"
   errors (Botania, Myrtrees, Upgrade Aquatic woods). Option: turn off the entries or wood types the pack does not
   want (content decision; cuts states too).
7. **Client startup reload: 109 s** (user's run with CTM still in; `Resource reload finished after 109402 ms`).
   ModelManager 88.9 s (33.3 s preparing, 55.6 s applying; CTM was about 20 s of the applying part, removed in
   10.5). Other preparers over 5 s: Forge client mod loader 25.7 s, particles 8.6 s, paintings 8.3 s, mob effect
   sprites 7.9 s, JEI sprites 7.1 s, Upgrade Aquatic glow squid sprites 7.3 s, Dimensional Paintings 5.7 s (these
   run in parallel). In the JFR: Forge resource listing (`PathPackResources#getResources`) 11.4%, mixin transforms
   4.4%, access transformer lookups 3.7% of startup samples.
8. **ATLauncher debug log config is still in the instance's Java arguments**
   (`-Dlog4j.configurationFile=.../client-1.19-debug.xml`): about 5 s per launch (10.2 #8), a 162 MB `debug.log`
   and 44 MB of dumped classes per launch in `/tmp/classDump*` (4 folders there now). Fix: remove that argument in
   ATLauncher's instance settings (user setting, not shipped).
   Decision 2026-10-27: the debug log config stays active; no change.
9. **ModernFix is not in the pack.** It has a Forge 1.19.2 build (LGPL-3.0) aimed at exactly these costs: lazy
   model loading (ModelManager and client heap, 10.2 #3), fewer block-state cache rebuilds, skipping the duplicate
   datapack load at world open (the generated packs and tag events above run twice), faster registry and resource
   handling. Untested here: needs a version pick, a compat pass (model loaders, FramedBlocks, Rubidium) and an
   A/B on the client startup and join.

### 11.3 Immersive Posts materials cut (built 2026-10-27, TODO `perf #21`)

Immersive Posts 4.3.0 registers a post (waterlogged 2 x facing 4 x flipstate 3 x type 5 x four arms 16 = 1,920
states) and a horizontal truss (waterlogged 2 x facing 4 x type 6 x four panels 16 x two connector points 4 = 3,072)
for each of its 15 materials: 74,880 states. Every property is read by the blockstate models, so none can be dropped
without rewriting the mod's models; the material count was the lever. The mod has no config for it (its only option
is `maxTrussLength`).

Kept: wood, aluminium, steel, iron, copper, nickel (29,952 states). Dropped: nether brick, gold, lead, silver,
constantan, electrum, uranium, concrete, leaded concrete (44,928 states, about 5.8% of the pack's 770,959).

patcherMod 1.1.03:
- `ImmersivePostsRegistrationMixin` redirects `EnumPostMaterial.values()` in the static-init lambdas of
  `IPOContent$Blocks$Posts` and `$HorizontalTruss` to the kept list, so the dropped blocks are never registered.
  `Posts.get` / `HorizontalTruss.get` already return null for a material missing from their map.
- `ImmersivePostsValidItemMixin` redirects `IPostMaterial.isValidItem` in `PostBaseBlock` and `PostBlock`: a dropped
  material's fence is not a post material. Without it, using that fence on a post base calls
  `getPostState(material)`, gets a null block and throws.
- The kept list is `compat/ImmersivePostsMaterials.KEPT`, in code: the mod registers in its own class init, before
  any config is read.

Block ids, items, recipes: posts have no items of their own (a post base is extended by using a fence on it), and
no pack script names a post or truss id. Loot tables and blockstate files of the dropped blocks stay in the jar and
are ignored. Existing worlds: placed posts of a dropped material are missing registry entries and load as air.
Tests: scratch queue SQ-IPO-01 to -03.

Fixed 2026-10-27 night (section 12.2): the valid-item mixin's handler was static on an instance method (crash at
load), and the mod's own tag files listing the dropped blocks made `minecraft:mineable/pickaxe`, `needs_stone_tool`,
`needs_iron_tool` and IE's `mineable/drill` fail to load; `TagLoaderImmersivePostsMixin` removes those entries.

Not measured yet: the `Workspace` world's 32 s and 44 s silent stretches are attributed above by phase only. A
client JFR of the next real launch would confirm them (`tools/perf/eval/eval_client.py` flags).

Worldgen and runtime: section 10 (options 1-4 of 10.7 built, Distant Horizons decision open in TODO
`pack/server #11`).

## 12. Overall measurement, 2026-10-27 night (all changes together)

Owner request: one overall before/after, no per-fix worlds. Same seed (-8188900049893920259), square, machine and
`-Xmx6G` / client arguments as 10.1. Jars: `mods/` at 18:29 for the first boot (patcherMod with the fixes below,
Creative Delight 17:20), then the 18:44 patcherMod and 18:43 Creative Delight (same tree plus a parallel session's
changes) for boots 2-5 and the client. Includes everything in 10.3-10.8 and 11.2-11.3: ModernFix 5.18.1, Distant
Horizons `numberOfThreads = 1` / request distances 256, CTM removed, WorldEdit lazy ids, stored ring positions, biome
tuner disk cache, item merge split, Immersive Posts cut. Kit: `tools/perf/eval/overall_run.sh` (boot, idle, pregen,
`/reload`, leaves the server up), `overall_reboot.sh`, `client_drive.sh` (drives `eval_client.py`'s client: title
screen, Play, sp_drive phases, JFR dump). Results in `perf/overall/ov1/` (untracked); reports `perf/reports/ov1-idle`,
`ov1-pregen`.

### 12.1 Numbers

| Measure | Eval 10.1 (morning) | `wgp_new` (10.9) | Now |
|---|---|---|---|
| `Done (...)`, new world | 118.7 s | 85.8 s | **78.9 s** |
| Launch to `Done` (wall), new world | 224 s | 190 s | **133 s** |
| Restart of the same world: `Done` / wall | | | 5.3-6.3 s / 59-60 s (69 s with a biome tuner solve) |
| Idle MSPT, Done + 4 min | 5.5 ms (p95 41 ms, 8 ticks over 50 ms) | 4.5 ms | 5.7 ms (boot 1, builds running); 6.0 ms steady on boot 2 (p95 6.5 ms, 0 over 50 ms) |
| Idle allocation | 92 MB/s (DH 72) | | 10 MB/s (DH 0.8) |
| Pregen, Chunky radius 400 (2601 chunks) | 5:26 | 3:50 | **2:46** |
| MSPT right after the pregen | | 11.2 ms | 9.1 ms |
| Item entities at spawn, Done + 2 min | 672 | 72 | 73 |
| `/reload` | 28 s | | **9-10 s** (three runs) |
| KubeJS recipe event ("Found" to "Added") | 12.2 s | 9.7 s | **2.7 s** |
| Block states | 770,959 | | 726,031 |
| Client launch to "Sound engine started" | 100 s | | **66 / 64 / 66 s** (three launches) |
| Startup resource reload | 109 s (user's run, with CTM) | | **28.5 / 27.7 / 29.6 s** |
| Launch to title screen usable | about 139 s | | about 76 s |
| Play clicked to "joined" | 95 s | | **30 / 29 / 27 s** |
| Play clicked to playable (JEI done) | about 190 s cold, 180 s | | **48 / 48 / 47 s** |
| JEI start | 89 s, 77 s | | **15.8 / 18.6 / 17.0 s** |
| Single player idle at spawn | median 26.5 ms, p95 48.5 ms, 73 ticks over 50 ms in 95 s | | median 13.3 ms, p95 24.3 ms, 9 over 50 ms in 114 s (one 4.3 s tick right after the join) |
| Single player exploring (6 teleports) | 11.8 TPS, median 23.9 ms, 129 over 50 ms, one 29 s tick | | **19.8 TPS**, median 17.4 ms, 62 over 50 ms, longest 1.7 s |
| Single player back at spawn | 20 TPS, median 12.0 ms, p95 14.3 ms | | 20 TPS, median 11.3 ms, p95 13.7 ms |
| Client heap after GC (10 GB) | 6.6-9.0 GB exploring | | at most 5.0 GB |
| Client FPS, uncapped, render distance 8 | averages 174-403 | | medians 292 (spawn), 298 (exploring), 317 (back); p5 66 / 122 / 205 |

Single player ticks are run 2's `creativeengineering.ServerTick` events; FPS is run 3 (the logger was broken in runs
1-2, below).

Reading it:
- Everything a player waits on is 2-4x shorter: a client is playable about 2 minutes after launch (eval about 5.5).
- Pregen: Distant Horizons' LOD builders fell from 37.6% to 20.4% of sampled CPU (one thread), worldgen workers rose
  from 50.6% to 70.0%. That is most of 3:50 -> 2:46.
- Idle MSPT did not improve on a dedicated server. The idle JFR (`ov1-idle` against `eval1-idle`): pack code
  0.72 -> 0.32 ms/tick, ItemPhysic 0.21 -> 0.04 (first-tick water), but entity ticking and mob spawning (Alex's Mobs
  +0.32, ducks +0.21, Domestication Innovation +0.12, mob spawning 12.7% vs 2.8% of the thread) rose: boot 2 ran after
  about 20 minutes of spawning around spawn, the eval reading on a fresh world. The spikes are gone (p95 41 -> 6.5 ms).
  ModernFix shows 0.20 ms/tick, but that is vanilla structure-template reading under ModernFix's replacement reader.

### 12.2 Problems found and fixed in this run

1. **Immersive Posts mixin crashed at mod loading** (10.9 problem 2): `ImmersivePostsValidItemMixin` had a static
   `@Redirect` handler with `method = "*"`; both call sites are in the instance method `use` (`m_6227_`). Now
   `method = "m_6227_"`, handler not static.
2. **The Immersive Posts cut broke three vanilla tags.** The mod's own `minecraft:mineable/pickaxe`,
   `needs_stone_tool` and `needs_iron_tool` files list every post and truss; a required entry naming an unregistered
   block makes 1.19.2 drop the whole tag ("Couldn't load tag ... missing following references"), and IE's
   `mineable/drill` (which includes `#minecraft:mineable/pickaxe`) failed with it: pickaxes would have lost every
   block. Fix: `TagLoaderImmersivePostsMixin` on `TagLoader#load` (`m_144495_`) RETURN drops the dropped materials' ids
   (`ImmersivePostsMaterials.isDroppedBlock`, matched on `TagEntry.toString()`, which is not remapped) before the tags
   are built. Checked: no IPO tag error; the iron post and iron ore are in `mineable/pickaxe`; IE's drill tag loads.
3. **ModernFix pre-empted the ring cache.** Its `mixin.perf.cache_strongholds` hooks the same
   `generateRingPositions` HEAD and RETURN; at RETURN it calls `setReturnValue`, which returns at once, so the pack's
   store never ran (it saves its own `mfix_strongholds.dat`, keyed by nothing: stale after a biome change).
   `config/modernfix-mixins.properties` now sets `mixin.perf.cache_strongholds=false` (copy in `configs_edited`).
4. Kit fixes: `client_fps_log.js` used KubeJS 5's `java()` and then the class-filtered `java.lang.Runtime` (the 10.1
   FPS column did not come from it); now FPS only, heap from JFR. `client_drive.sh` resolves its results path
   (jcmd writes relative to the client's cwd); `eval_client.py` adds `dumponexit`. `merge_core_probe.js` expectations
   updated (107 merges, 4-bottle milk recipes, `creativedelight:` cake cutting id).

### 12.3 Open

- SQ-IMS-04: `vintagedelight:salt_block` is still an input of 3 Vintage fermenting recipes (ochre, pearlescent,
  verdant froglight) and `vintagedelight:salt_bucket` the output of `tactical_fishing:fishing_vintagedelight_salt_bucket`
  (the salt merge, SQ-SALT-01).
- ModernFix warns "outdated structure file, can cause worldgen lag" for 18 namespaces (Terralith, YUNG's, Subterrestrial,
  Domestication Innovation, Chef's Delight, ...): NBT templates of old data versions run through DataFixerUpper on
  every load. A one-time upgrade of those templates (a pack data overlay) would remove it; not measured.
- Idle MSPT on a populated world: mob counts, not the pack (12.1). A fair before/after needs the same world age.
- Resource packs were not measured here (the eval client ran them all, unsplit). Per-pack cost: [perf_rpack.md](perf_rpack.md)
  (built 2026-10-29, client run CQ-RP queued).

## 13. Follow-ups to the overall run (built 2026-10-27 late, patcherMod 1.1.03, installed 20:44)

From the ranking of the overall run (`ov1-idle`, `ov1-pregen`, and `ov1-client` made from `perf/overall/ov1/client.jfr`
with `--mods mods`). Scratch tests SQ-STC-01/02, SQ-GFB-01 to -04, SQ-HRU-01/02; client check CQ-STC-01. TODO
`server #24` to `#26`.

### 13.1 Season type cache (client chunk meshing)

`SeasonTypes.tagged` was 20% of the client's `Chunk Render Task Executor` (95% of the pack's share there) and showed on
the render thread: Serene Seasons' grass and foliage colour handlers reach it through `SeasonColorUtilMixin` and
`SeasonBiomeConfigMixin` once per tinted block, and each call ran up to ten biome tag lookups. Now the result is cached
per biome holder (`TYPE_CACHE`, a `ConcurrentHashMap` keyed by registry references; direct holders are not cached).
`TagsUpdatedEvent` (server reload and the client's tag packet) swaps in a new map, so a lookup racing the reload lands
in the old one. `enabled()` is still read on every call. Same results; nothing changes for players but the cost.

### 13.2 Generated falling blocks wait for a real update

The entity census of the overall run found 193 `minecraft:falling_block` (69 sand, 66 gravel) of 339 entities around
spawn. Generation never makes them fall; they fall when the chunk first ticks, and in the outer ring of loaded chunks
block ticks run but entities do not, so the falling entity never lands and stays saved. Three triggers:
`LevelChunk.postProcessGeneration` re-running `updateShape` on marked positions (blocks above carvers and lakes),
generated water settling (10.6 window) and updating its neighbours, and ticks a feature scheduled during generation.

`worldgen/GeneratedFallingBlocks` (`[worldgen.systems] holdGeneratedFallingBlocks`, default on):
- `FallingBlockHoldMixin`: `FallingBlock.updateShape` (m_7417_) HEAD returns the state without scheduling the fall
  while post-processing runs (flag set by `LevelChunkFirstTickWaterMixin` HEAD to RETURN) or a settling fluid tick
  runs (`FirstTickWater.settlingTick`). Concrete powder's override calls super and is covered.
- `ProtoChunkTicksFallingMixin`: `ProtoChunkTicks.schedule` (m_183393_) drops ticks for a `FallingBlock` scheduled
  during generation.
- `FirstTickWater` now opens its window when either switch is on; drops are skipped only with `firstTickWater` on.

Any later update next to the block (digging under it, water outside the window, a piston) calls `updateShape` again and
it falls as in vanilla. Not covered: a mod's falling block that does not extend `FallingBlock`.

Measured 2026-10-27 late (ov2, same seed and square as ov1): 102 falling blocks (61 sand, 41 gravel, no red sand)
against 193. Half fixed; the trigger behind the remaining sand is open (scratch_queue.md run log).

### 13.3 Copycats `customOcclusion`: unkeyed holders

Copycats' `BlockStateBaseMixin.customOcclusion` runs at HEAD of `canOcclude` (light engine, DH LOD builder: almost every
block) and asks `getBlockHolder().is(create:copycat_base)` inside `catch (IllegalStateException)`. For a holder with no
registry key, vanilla `Holder.Reference.key()` builds `"Trying to access unbound value '" + block + "' from registry " +
registry` and throws (Forge 43.2.3 `Holder$Reference` line 143): 558 of Copycats' 936 pregen samples were that string
concat. `HolderReferenceUnboundMixin` makes `Holder.Reference.is(ResourceLocation)` (m_203373_) return false when the
key is null: Copycats' result is unchanged (it read the exception as false), other callers get false instead of a
throw. It logs the class of the first 20 unkeyed values once ("Holder with no registry key asked is(...)"), which
answers which blocks have no key; that cause is still unknown (not the Immersive Posts cut: Copycats showed the cost in
September).

ov2 run: the 20 logged classes are MoreWires connectors, Immersive Petroleum and IE multiblock / cloth / concrete
blocks, all logged on `main` during mod loading; Copycats is gone from the pregen profile (ov1 0.04%, 0.52 MB/s).

- 2026-10-28: planned from the scratchpad: measure the resource pack's cost to the client (TODO `pack/server #27`). No code changed.

## 14. Perf session: every scratch run recorded (built 2026-10-28)

Each bulk scratch-queue run is also a performance run: every server records, and each queue entry is a measured
segment. Benchmarks with fixed windows (boot, idle, pregen, reload) are the perf suite's G scenarios
([perf_suite.md](perf_suite.md), its own runner, bot players from `development/creativeDevtools/members/perfbot`); this section covers
queue runs driven through `scratch_server.sh` only. Servers the suite owns (`srv/perf_suite.owner`) are skipped.

| Part | Status |
|---|---|
| `scratch_server.sh setup` adds the recording to every scratch server (`ce-server.jfc`, no `maxage`, `maxsize=${JFR_MAX:-1g}`, exit dump to `perf/exit`; `CE_PERF=0` turns it off) | BUILT |
| `scratch_server.sh` boot / cmd / stop write a timeline line while a session is open; `cmd` and `stop` give up after 10 s when no JVM reads the fifo ("server not reading"); `stop` kills only its own fifo feeder (`srv/feeder.pid`) | BUILT; the timeout tested on a fifo with no reader |
| `eval_server.sh wait` prints `DEAD` (exit 3) when no java has its cwd in `srv/` | BUILT |
| `tools/perf/session/perf_session.sh start [NAME]`, `mark TEXT`, `status`, `stop`, `process`, `report [--baseline DIR] [--prune]` | BUILT; start / mark / status / stop tested with no server |
| `collect.sh` (background, every 5 s): per boot `boots/NN_level/meta.env` (start, level, type, seed, jar-list hash, JVM args), `proc.tsv` (CPU seconds, RSS, threads), `latest.log` every 30 s; at exit it moves `perf/exit/hotspot-pid-PID-*.jfr` in (or copies the left-over `jfr-repo` folder), with the console tail and crash reports | BUILT; first live use is the next bulk run |
| `session_report.py`: per boot `perf_report.py` HTML and `ticks.tsv` (ServerTick, ChunkWait, slow KubeJS); `summary.md` / `summary.json` with boots (launch to Done, `Done (`, KubeJS recipe event, RSS, how it ended), segments by mark (tick median / p95 / max / over 50 ms, busy %, CPU cores, chunk waits, slow KubeJS calls, change against a baseline session) and every tick over 50 ms with the commands sent in the 60 s before (`summary.json`, `worst_ticks.tsv`; `summary.md` shows the 50 longest; the cap of 25 was lifted 2026-10-28, owner: summaries never capped); malformed `proc.tsv` rows skipped | BUILT; first live run 2026-10-28 (session `q1028`, 8 boots: collector, exit-recording move, killed-boot `repo/` copy and segment stats all worked; run `report` between boots to keep the summary current) |

How to use it in a bulk run: [testing/scratch_queue.md](testing/scratch_queue.md), "Perf session". Output in
`perf/sessions/<name>/` (untracked). The collector never parses while a server is up (that would load the CPU it
measures); run `report` after the last server stops. Segments assume one scratch server at a time (the
one-JVM rule); a mark applies to whichever server is up.

Known limit: when a boot is followed at once by a reboot of the same `srv/`, the collector skips the final `latest.log`
copy (the new boot owns the file), so the earlier boot's Ended column reads "no clean stop in the log". Cosmetic: the
real end is in the rotated `srv/logs/*.log.gz`.

Tests (live, next bulk run):
- [x] **PS-01** *(PASSED 2026-10-28, session q1028)* `perf_session.sh start`, run the queue, `stop`, `report --prune` -> one `boots/` folder per boot, each
  with a recording before `report` and a `report.html` after; `summary.md` lists every entry that was marked.
- [x] **PS-02** *(PASSED 2026-10-28)* a boot stopped with `scratch_server.sh stop` -> "clean stop"; the `hotspot-pid-*.jfr` was moved out
  of `srv/perf/exit/`.
- [x] **PS-03** *(PASSED 2026-10-28; the earlier boot's Ended column reads "no clean stop" because its final latest.log copy is skipped, cosmetic)* two boots of one `srv/` back to back -> two boot folders, each with its own `latest.log`.

## 15. Immersive Weathering cost in perf suite g1 (studied 2026-10-28)

Source: the suite run `g1` recordings (`perf/suite/g1/jfr/`, parsed to `perf/parsed/g1_<scenario>/`), Immersive
Weathering 1.2.10. Shares are of Server thread CPU samples (JFR only samples running code, so a share of a scenario's
mean MSPT is a fair ms estimate). Classes read with vineflower from the jar.

### 15.1 Numbers

| Scenario | MSPT | tickChunk | IW total | IW `tickBlock` (random tick) | sky tick | `MossyBlock` isRandomlyTicking | Growth work done | Mossable neighbour cascade |
|---|---|---|---|---|---|---|---|---|
| G03a one bot, day | 18.5 | 16.8% | 3.31% | 2.57% | 0.10% | 0.22% | 0.08% | 0 |
| G03b one bot, night | 16.3 | 19.5% | 3.00% | 2.26% | 0.16% | 0.31% | 0.07% | 0 |
| G04 four bots | 50.1 | 15.9% | 2.70% | 1.93% | 0.11% | 0.20% | 0.06% | 0 |
| G05a travel 30 b/s | 29.0 | 4.3% | 2.44% | 0.58% | 0.08% | 0.13% | 0.03% | 1.35% |
| G06 save-all | 49.9 | 15.1% | 2.34% | 1.77% | 0.06% | 0.32% | 0.04% | 0 |
| G12 soak 45 min | 32.0 | 16.6% | 2.85% | 2.23% | 0.15% | 0.23% | 0.09% | 0.01% |

IW is about 17% of the random-tick phase in every scenario with players, about 0.6 ms of an 18 ms tick (G03a) to
1.35 ms of a 50 ms tick (G04). It is also 5.5 to 6.4% of all Server thread allocation (G12: 18.7 GB sampled over
45 min, 17.4 GB of it two objects, see below). Worldgen threads: icicle feature only, negligible. Entity AI
(`FollowLeafCrownGoal`) 0.02%.

### 15.2 Where it goes

1. **`BlockGrowthHandler.tickBlock` dispatch, 75 to 80% of IW's time, almost none of it growth.** IW's
   `ServerLevelMixin.callTick` injects into vanilla's random-tick loop at `getFluidState`, so it runs for every
   random-tick position in a ticking section whatever the block (stone, air, dirt), re-reads the block state from
   the palette, and calls `tickBlock`. Every call: `CommonConfigs.BLOCK_GROWTHS.get()` (Forge 43 does not cache
   config values: `ConfigValue#getRaw` walks the night-config path, 21% of IW leaf time), allocates a
   `Suppliers.memoize` and its lambda for a lazy biome lookup (the two objects above), an `EnumMap` lookup, a
   `HashMap` iterator for the source's universal set, an `Optional` and an `IdentityHashMap` lookup (15% of leaf
   time) for the block's own growths. The block has no growth on nearly every call: frames inside a growth class
   are 0.03 to 0.09% of the Server thread, 3% of IW's time. For `block_tick` there are no universal growths
   (only `block_sandcover`, `block_snowcover`, `icicle_from_snow`, `lightning_vitrified_sand`, on rain, snow and
   lightning), so every block without an owner-listed growth can return before any of this.
2. **`MossyBlock#isRandomlyTicking` (0.2 to 0.3%).** Vanilla asks it on every random-tick position that lands on a
   mossy block; IW answers with `hasProperty` + `getValue` (FerriteCore `FastMap#getKeyFor`) instead of a cached flag.
3. **Mossable neighbour cascade (1.35% in G05a only).** Scheduled ticks left in pregenerated chunks fire when a
   player first loads them: `MossableBlock#tick` calls `updateNeighborsAt`, each mossable neighbour runs
   `updateWeatheredStateOnNeighborChanged` -> `PatchSpreader#getWantedWeatheringState` (scans faces), and a state
   change schedules the next tick. A one-off per chunk; gone in G05b and the soak.
4. **Sky-access tick (0.1%), grass spreading (`ModGrassBlock` randomTick, mostly the vanilla `super` call),
   fluid generators (0.2% in G05a):** not worth touching.

### 15.3 Candidates

| # | Change | Expected | Behaviour change |
|---|---|---|---|
| A | patcherMod mixin on `BlockGrowthHandler.tickBlock`: cache `block_growths` (refresh on config load), look up the block's growth set and the source's universal set first, return when both are empty, only then build the biome supplier | most of item 1: about 1.5 to 2% of the Server thread (0.5 to 1 ms per tick with players), most of IW's allocation | none |
| B | A plus a precomputed per-block flag (an `IdentityHashMap` set at rebuild, or a field on the block) so the early return skips the hash lookup too | a further share of the 15% lookup | none |
| C | Replace IW's `callTick` injection (mixin plugin cancel in the coremod) with one that only calls when the block has growths for `block_tick`, skipping the palette re-read | the remaining PalettedContainer re-read (2 to 4% of IW) | none if the re-read is kept for blocks that have growths |
| D | `MossyBlock` / `MossableBlock` `isRandomlyTicking`: read a precomputed per-state boolean | item 2, 0.2 to 0.3% | none |
| E | Mossable cascade: leave | one-off per chunk | |

A is the clear first step: small, no behaviour change, and it applies to every scenario with players. Measure with
the suite (G03a, G04, G12) against `g1`. TODO `server #29`.

### 15.4 Built: A, B and C (2026-10-28, patcherMod 1.1.03, installed in `mods/` 2026-10-28)

- `perf/IwGrowthFast`: `tickBlock` reads the block's growth sets from a field on the block (B), returns when neither
  the block nor the tick source has growths, reads `block_growths` once per game tick, and only then builds the
  memoized biome supplier (A). Universal sets first, then the block's, the same `Set` objects IW iterates, so the
  same growths run in the same order.
- `mixin/IwBlockGrowthHandlerMixin`: `@Overwrite` of `BlockGrowthHandler.tickBlock` (no CallbackInfo per call),
  keeping IW 1.2.10's body for the off switch; a RETURN inject on `rebuild` refills the tables from IW's maps
  (`GROWTH_FOR_BLOCK`, `UNIVERSAL_GROWTHS`) after every tag update. `mixin/IwBlockGrowthHolderMixin` adds the field
  to `Block` (`perf/IwGrowthHolder`).
- C: `perf/CEMixinPlugin` is now the mixin config plugin of `creativeengineering.mixins.json`. On the post-apply of
  the empty marker `mixin/IwServerLevelTickMixin` (ServerLevel, priority 1500) it finds IW's merged handler (name
  ends `$callTick`, `@MixinMerged` from IW's `ServerLevelMixin` or the exact 1.2.10 descriptor) and replaces its
  body with a call to `IwGrowthFast.blockTick`, which uses the state vanilla already read unless the block
  random-ticked (only its random tick runs between vanilla's read and the hook). An unknown descriptor or a missing
  handler logs a warning and leaves IW's code as it is.
- Off switch for A/B measurements: JVM property `-Dcreativeengineering.iwFastTick=false` (all three parts).
- Checked headless (scratchpad harness): the rewrite on IW's real `callTick` bytecode gives a 9-instruction body that
  passes ASM's type-aware verifier as a ServerLevel method; another mixin's handler and other post-applies are left
  alone. Mixin application, growth behaviour and the gain need a server: SQ-IWF-01 to -05 (scratch_queue.md).
  Scratch r1031 (2026-10-31): SQ-IWF-01, -02, -02b, -03, -04b PASSED (fast path on and off grow the same: 15 / 14 leaf
  piles; growths off grows nothing; caveat: -04b used persistent leaves). SQ-IWF-05 (the gain) not run.

## 16. Four spread-out players: whistle goal and underground bees (built 2026-10-28, patcherMod 1.1.03 installed)

Suite `g1` G04 (four bots 600 apart, 50.1 ms mean) had the server at its tick budget (scratch run 2026-10-28,
finding 10). Inclusive shares of the 18,725 Server thread samples in `perf/suite/g1/jfr/G04-1.jfr`:

| What | Share | Per 50 ms tick | Note |
|---|---|---|---|
| Hostile mobs (`world.entity.monster`) | 22.0% | about 11 ms | about 310 hostiles = four monster caps of 70; G03b (one bot, 75 hostiles) runs at 16.3 ms |
| Natural spawning | 5.3% | about 2.7 ms | |
| Bees (49 in the census) | 5.9% | about 3 ms | `Mob.serverAiStep` 710 of 1107 samples (wander goal flight pathing), travel 212 |
| Nock Enough Arrows `TemptByWhistleGoal#canUse` | 1.9% | about 1 ms | an entity query for `WhistlingArrow` in 12 blocks, on every mob, with no arrow anywhere |

The mob cap is not changed here; section 17 adds cap levels.

### 16.1 Whistle goal skip (no behaviour change)

`perf.WhistleArrowTracker` keeps the server's live whistling arrows (weak set, join and leave events, matched by class
name). `NeaWhistleGoalSkipMixin` (Pseudo, `m_8036_` HEAD) returns false while the set is empty, before the query.
`canContinueToUse` ends with `canUse`, so a tempted mob still stops when the last arrow goes. Expected: the whole
1.9%.

### 16.2 Bees below y 32 idle without a player

`MobBeeIdleMixin` (`Mob.serverAiStep`, `m_6140_` HEAD): a `Bee` below y 32 with no living player within 32 blocks
skips its AI step (goals, sensing, navigation, move and look controls). Travel, gravity and damage still tick; the
bee resumes when a player comes within 32 blocks. Behaviour change: an underground bee farm below y 32 does not
pollinate or return to its hive while nobody is near. Bees inside hives are block entity data and unaffected.
Expected: up to about 65% of the bee cost for bees below y 32 (the census has no bee heights, so the share of
underground bees is not known).

Tests: scratch queue "Whistle goal skip and underground bees" (SQ-PF16-01 to 04). TODO `server #30`. Scratch r1031
(2026-10-31): SQ-PF16-01 (mixins), -03 (underground bee frozen with no player near, moves again) PASSED; -02 (whistling
arrow still pulls) client check pending; -04 not run.

## 17. Mob cap levels by spawnable chunks, spawn edge and vertical limits (BUILT 2026-10-28, patcherMod 1.1.03 installed; BROKEN: SQ-MCL-04 FAIL r1031)

Follows section 16 and finding 10: in G04 four spread-out players ran four full monster caps (about 310 hostiles, 22.0%
of the Server thread, plus 5.3% natural spawning). Nothing in the pack changes caps or despawn today; they are vanilla.
Built 2026-10-28 as cap levels (17.2), a spawn radius per level (17.3) and a vertical band per level (17.4), all
driven by the same level. Code: `perf.MobCapLevels`, `MobCapLevelTickMixin` (ServerChunkCache `m_8490_`, ModifyArg on
`NaturalSpawner.createState`'s chunk count, unchanged), `MobCapMixin` (Redirect of `getMaxInstancesPerChunk` in
`SpawnState.canSpawnForCategory` `m_186548_` and `LocalMobCapCalculator$MobCounts.canSpawn` `m_186522_`),
`MobCapSpawnAreaMixin` (NaturalSpawner `m_47045_` spawnCategoryForChunk HEAD for the radius, `m_220421_`
isValidSpawnPostitionForType HEAD for the band). Config `[perf]` in the common config, read live. `/ceperf mobcap`
prints each dimension's spawnable chunks and level. Botania's `NaturalSpawnerMixin` touches the same class (not the
same methods).

### 17.1 Vanilla facts this rests on (1.19.2)

| Fact | Detail |
|---|---|
| Cap | `category cap x spawnable chunks / 289` (`NaturalSpawner$SpawnState`). Spawnable chunks = the union of the chunks within 8 chunks of any player. Overlapping players share cap; spread-out players each add a whole one |
| Caps | monster 70, creature 10 (persistent), ambient 15, water_creature 5, water_ambient 20, underground_water_creature 5, axolotls 5 |
| Despawn | per mob, by the nearest player: instant beyond 128 blocks, a 1/800 chance per tick between 32 and 128 after 30 s of life. Persistent mobs (`creature`, named, leashed) skip it |
| Spawn reach | spawn positions come from chunks within 8 chunks of a player, so a diagonal chunk is up to about 181 blocks away, past the 128-block instant-despawn line |
| Shipped distances | view 10, simulation 8 (128 blocks). Mobs past simulation distance do not tick |

### 17.2 Cap levels, driven by total spawnable chunks

The player count is not the input; the spawnable chunk total is (what vanilla already counts, so two players standing
together are not penalised). A mixin on the cap read (`MobCategory#getMaxInstancesPerChunk` at return, or
`SpawnState#canSpawnForCategory`) picks a level from the current total and returns the reduced per-chunk cap.

| Level | Monster cap | Other capped categories | Total monsters at 1 / 2 / 4 areas (289 chunks each) |
|---|---|---|---|
| 0 (vanilla) | 70 | vanilla | 70 / 140 / 280 |
| 1 | 50 | scaled by 50/70 | 50 / 100 / 200 |
| 2 | 25 | scaled by 25/70 | 25 / 50 / 100 |

Rules:
- `creature` is never scaled (persistent, cap 10, herds are placed at chunk generation).
- Lowering a cap removes no mobs; it only blocks new spawns until the count falls below it.
- The chunk thresholds for level 1 and level 2 are not decided. Starting guess: level 0 up to about 1.5 areas
  (about 430 chunks), level 1 up to about 3 areas (about 870), level 2 above. To be set from G03/G04 and a run with
  two and three bots; add a hysteresis margin so the level does not flip each tick when a player walks.
- A single player alone (289 chunks) stays at vanilla. Whether an open-world player in one area should ever drop
  below 70 is not planned.
- Config in `CEConfig` `[perf]`: `mobCapLevels` (off switch), `mobCapForceLevel` (-1 auto, 0 to 2 forced, for the
  suite runs), `mobCapLevel1Chunks` 430, `mobCapLevel2Chunks` 870, `mobCapHysteresisChunks` 30 (a level is left only
  30 chunks below its threshold), `mobCapLevel1Monster` 50, `mobCapLevel2Monster` 25, `mobCapScaledCategories`
  (all capped categories except `creature`). Other categories get `round(vanilla x cap / 70)`, at least 1.
- Built: both the dimension cap and the per-player cap (`LocalMobCapCalculator`) read the level's value. The level
  is per dimension, from that dimension's count. Thresholds are the starting guesses above, still to tune (17.5).

### 17.3 Spawn edge and spawn radius per level

Result of the investigation (bytecode, 1.19.2): vanilla already rejects a natural spawn position farther than the
category's despawn distance (128) from the nearest player, in `isValidSpawnPostitionForType`, unless the type has
`canSpawnFarFromPlayer`. So the instant pop in and out below does not happen and needs no code. Random despawn only
starts after 30 s without action, so spawns at 100 to 128 blocks are not wasted at once either.

Built instead (owner, 2026-10-28): a spawn radius per level. At level 1 the scaled categories only spawn in chunks
within 6 chunks of some non-spectator player (Chebyshev distance on chunk coordinates), at level 2 within 5 (vanilla
8). The check is at the head of `spawnCategoryForChunk`, so a chunk outside it skips the position roll and every
check after it. Config `spawnChunksLevel1` 6, `spawnChunksLevel2` 5 (0 = vanilla). The cap is still computed from
vanilla's spawnable chunk count, so the level's cap is filled from fewer chunks (13 x 13 = 169 or 11 x 11 = 121
instead of 289 per player): mobs spawn closer, not fewer than the cap. The chunk count that picks the level is not
changed either.

Original investigation notes:

Problem: a spawn rolled in a chunk farther than 128 blocks from every player is removed by the instant-despawn check
within a tick or so, wasting the spawn attempt, the placement checks and the despawn check. Mobs spawned between about
100 and 128 blocks also despawn at random as soon as a player moves a little, and they are outside simulation
distance when beyond 128.

To investigate:
- How many spawns in G04 land beyond 128 blocks (log in the existing `CheckSpawn` path, `SpawnPlacementsConditionsMixin`
  is the nearest hook) and how many despawn within, say, 100 ticks of spawning. This decides whether it matters.
- Reject a spawn position when the nearest player is farther than a limit (128, or a margin such as 120) in the same
  `CheckSpawn` event or the mixin; the attempt then costs one distance check.
- A smaller radius also shrinks the effective spawnable chunk count the cap uses, which interacts with 17.2: if only
  chunks within the radius spawn, the cap should be computed from those, or the same cap would be spread over the
  same chunks anyway. Decide whether to leave the vanilla chunk count alone.
- Whether the lower bound (24 blocks, vanilla no-spawn zone) should rise per level, so close-in surface spawns are
  also thinner at higher levels.

### 17.4 Vertical spawn limits per level (built: monster only, 64 / 32)

Built as proposed: `spawnBandLevel1` 64, `spawnBandLevel2` 32 (0 = no limit), `spawnBandCategories` `monster`. The
band is measured from the nearest player (3D, spectators included as vanilla's own lookup does) at the head of
`isValidSpawnPostitionForType`, so a rejected position costs one player lookup. Spawners and structure spawns are
not natural spawns and are not affected.


Vanilla lets a monster attempt pick any height from the world bottom up to the surface of the chosen column, so a
player on the surface has caves 200 blocks below counted in the cap and ticked. Proposal: a vertical band around the
player per level, enforced in the same check as 17.3 (reject the position when `abs(spawn y - player y)` exceeds the
band, nearest player).

| Level | Vertical band (to decide) |
|---|---|
| 0 | vanilla (no limit) |
| 1 | wide, for example 64 blocks up and down |
| 2 | narrow, for example 32 blocks up and down |

Open questions: whether the band applies to monsters only (underground water categories and bats have their own
heights), how it treats players in deep caves with the surface far above (band is relative to the player, so fine),
and whether it affects spawner-like structure spawns (those are not natural spawns and skip it). Values above are
placeholders to tune.

### 17.5 Measurement plan

Re-run suite `g1` G04 (four bots) with level 1 and level 2 forced on, compare mean and p95 tick, the monster entity
share and the natural spawning share to section 16's numbers, and add a two-bot and three-bot plan for the thresholds.
A level also needs a play check on the real server: how empty an area feels at 25. Queue: scratch queue "Mob cap
levels" (SQ-MCL-01 to 06). TODO `server #31`.

Scratch r1031 (2026-10-31): SQ-MCL-01 (mixins), -02 (one player: level 0), -03 (two players apart: level 1, back to 0),
-05 (forced level 2, normal world: monsters only inside the 32 band) PASSED. SQ-MCL-04 FAILED: forced level 2 on the
superflat spawned 0 monsters in 8 min for one bot (level 0: 13 zombies); suspects `MobCapSpawnAreaMixin.outsideRadius`
and the local-cap path in `MobCapMixin`. Note: `#forge:monsters` does not hold zombies in this pack. SQ-MCL-06 not run.
