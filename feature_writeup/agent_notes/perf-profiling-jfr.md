# perf-profiling-jfr

> 2026-09-28 perf-measurement study - JFR always-on + offline per-mod attribution chosen over spark/Observable; A/B-measured overhead below noise; validated facts, first findings, pitfalls; prototype was scratchpad-only

Moved from agent memory 2026-11-16 (last edited there 2026-09-29). Repo status docs win over any status line here.

The user asked for a measurable, analytical view of which mods slow the server (then the client) that doesn't destabilise the game. They "have Observable", but it is NOT in this pack's mods/ (other instances have it). I recommended **JFR (built into Java 17) + an offline analyzer** and prototyped it on a scratch server (memory `scratch-server-verify`) from deploy/latest.

**Validated 2026-09-28:**
- Overhead: A/B on the same seed, Chunky radius 400 (2601 chunks). No JFR: boot 51.2/51.0 s, pregen 1:34/1:34. JFR (10 ms sampling, stackdepth 256): 51.2 s, 1:34. Idle MSPT noise (1.33 vs 1.72 ms between two no-JFR runs) exceeded the JFR difference. A first run looked 20% slower only because I ran the parser and indexer on the same machine mid-run. Never do other work during an A/B.
- `-XX:FlightRecorderOptions=stackdepth=256` is needed. At the default of 64, 14% of Worker-Main and 2% of Server thread stacks lose their root (tick-phase context).
- Vanilla 1.19.2 has `/jfr start|stop`, but its bundled flightrecorder-config.jfc enables jdk.ObjectCount (a stop-the-world heap census every chunk) and FileRead/FileWrite at 0 ms, so don't use it. Minecraft's own events (minecraft.ServerTickTime = 100-tick rolling avg, ChunkGeneration with `status`, NetworkSummary, LoadWorld) fire in ANY recording that enables them (gated on EventType.isEnabled), e.g. a custom .jfc started via JVM args.
- `jcmd <pid> JFR.dump name=ce filename=x.jfr` took 0.5 s and didn't disturb the running server. Both JDKs here (system 17.0.20 and ATLauncher java-runtime-gamma) ship jfr/jcmd.
- Static mixin resolver (mixin configs from MANIFEST/mods.toml, @Mixin targets parsed from class files, `handler$<id>$<name>` matched by target+name): checked against the client's .mixin.out @MixinMerged, 2009/2116 exact, 90 correct-but-shared, 1 wrong, 16 unresolved.
- Limits: CPU sampling can't see Server-thread waiting (managedBlock parks in 0.1 ms slices, below park thresholds). KubeJS time is attributable to the event (e.g. levelPostTick) but not to a script (Rhino is interpreted). Stacks carry no dimension. Distant Horizons threads flood jdk.ThreadPark/ThreadSleep events, which is why the lean .jfc disables Sleep/MonitorWait and sets Park to 50 ms.

**First findings (scratch server, no players):** idle Server thread (1.3-1.7 ms MSPT): snowundertrees ~21%, KubeJS LevelEvents.tick ~17% (armor_effects.js horse-armor walk calls getAllEntities() every tick in every level, unthrottled; dragon_autorepair.js's walk is interval-gated), sereneseasons ~5%, lootr/betterdays ~3%. Worldgen workers: blueprint BiomeUtil 8.8%, copycats `customOcclusion` mixin on BlockStateBase 8.3%, immersive_weathering icicles 3.3%, tectonic 2%. DH 3.0.2 server LOD builders: ~28% of all CPU samples and 23% of allocation (220 MB/s) during pregen. Unconfirmed until a live-server recording.

**Client measurement caveat (resolved 2026-11-07):** the debug log config (client-1.19-debug.xml, Root level=all, class dumps to /tmp/classDump*) was removed from the instance javaArguments on 2026-11-07 (perf.md 18). If it is back, it skews client timings.

**Built 2026-09-28 (user chose options 1+2; option 3, Creative Common JFR events + /cdevperf dump, deferred):** see feature_writeup/perf.md (PF-). tools/perf/ (perf_report.py CLI -> perf/reports/<name>.html + .summary.json for --baseline; PerfParse.java, modindex.py, report_html.py; perf/ output untracked). server/ (new tracked folder, new build_deploy.py category `server` -> server root, server target only): LaunchServer.sh/.bat with the JFR JVMARGS, perf/ce-server.jfc, dump.sh/.bat/.ps1, README. Also fixed: the starter jar drops -X options from args files, so the .bat's `set finaljavaargs=` meant Windows servers ran without -Xmx; the .sh's `$FINALJAVAARGS=` was a no-op "command not found". Linux path tested on a scratch server; Windows scripts untested. Not deployed: ships with the next build (1.0.2).

**Pitfall:** `pgrep -f <pattern>` inside a `bash -c` script matches its own wrapper shell (the loop never ends). Use `ps -eo comm=,args= | awk '$1=="java" && /unix_args/'`.

**Server report built 2026-09-30 (plan PR, perf.md 3.5, PF-09..13):** `server/perf/report.sh [window] [tag]` bundles live JFR dump / previous-run recording, logs + window excerpts, crash reports, 3 thread dumps, system.txt, files.sha1 (hashes only) into perf/reports/<name>.zip with secrets (password/token/secret keys incl. world serverconfig discord token) and player IPs redacted. `tools/perf/ingest_report.py <zip>` -> perf/incoming/<name>/index.html: version by mod hashes vs deploy/history, exact jar set rebuilt by sha1 for attribution, timeline with lag/crash marks, crash heads with jars-in-stack. Linux only (server is Debian, Java 21). Scratch-tested running/crashed/watchdog.
- **Watchdog crashes used to lose the recording**: System.exit -> JFR's shutdown hook deletes the repo folder. Fixed with `dumponexit=true,filename=perf/exit` (launch scripts mkdir it - a missing dir makes JFR write a FILE named exit - and keep the newest 3). Runtime.halt/kill -9 still leaves the repo folder.
- minecraft.ServerTickTime reads 0 for a while after boot, so perf_report's startup cut can leave a tiny window; ingest reruns with --include-startup when < 30 s.
- Snap Firefox headless screenshots can't read ~/.local or /tmp: stage the html in ~/snap/firefox/common/ (delete after).

**Pack evaluation 2026-10-27** (results in feature_writeup/perf.md section 10, TODO server #10-#15). Repeatable kit in `tools/perf/eval/` (eval_server.sh setup/boot/wait, eval_client.py setup/launch [nodebuglog], entity_census.py) and `tools/perf/log_timings.py --months logs`. Gotchas:
- "Sound engine started" is ~35 s before the title menu is usable; the log's join time (integrated server start -> joined) leaves out ~62 s of datapack load before it and the JEI start after it. Time real joins from the Play click.
- In client JFR the render thread is named `main`, not "Render thread". `perf_report.py --last` takes SECONDS.
- Driving the client: xdotool on the 854x480 window: Singleplayer at (425,235), first world (340,133), Play Selected World (267,395) relative to the window; FPS readout is drawn top-left (crop 420x20). Set pauseOnLostFocus:false or the integrated server pauses.
- Rhino `let` in a `for` loop is shared by closures (all scheduled callbacks saw the last i).

**Overall run 2026-10-27 night** (perf.md 12; kit `tools/perf/eval/overall_run.sh`, `overall_reboot.sh`, `client_drive.sh`). Gotchas:
- Two mods' `@Inject` at RETURN of one method: the first that calls `setReturnValue` returns at once, the rest never run. ModernFix's `cache_strongholds` silently disabled the pack's ring cache this way; check new perf mods for overlapping targets (refmap grep for the SRG name).
- 1.19.2 drops a WHOLE tag when one required entry names an unregistered id: cutting a mod's blocks (Immersive Posts) took `minecraft:mineable/pickaxe` down. Grep `Couldn't load tag` after any registry cut.
- `jcmd PID JFR.dump filename=` resolves relative paths against the target JVM's cwd: pass absolute paths.
- KubeJS client scripts log to logs/kubejs/client.log, not latest.log; `java()` is gone in KubeJS 6 and `java.lang.Runtime` is class-filtered.
- WorldEdit on Forge ignores console `//` commands (players only): test through its API (`tools/worldedit_probe/we_probe.js`; Rhino needs named overloads like `ForgeAdapter['adapt(net.minecraft.server.level.ServerLevel)']`).
- Piping a reboot script that starts the server into `| tail` hangs the shell (the JVM inherits the pipe): redirect to a file.
- Idle MSPT depends on world age (mob population): compare fresh worlds at the same Done + N.
