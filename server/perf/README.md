# Server performance recording

The server keeps a rolling recording of what it spends its time on (Java Flight Recorder, built into Java).
When it lags, save the last minutes to a file and turn that file into a per-mod report on the dev machine.

## What runs

`LaunchServer.sh` / `LaunchServer.bat` start Java with two extra options (the `JVMARGS` / `jvmargs` line):

- `-XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxage=30m,maxsize=512m,dumponexit=true,filename=perf/exit`
  keeps the last 30 minutes, at most 512 MB, in `perf/jfr-repo/`, and writes them to `perf/exit/` whenever the
  server stops, watchdog crashes included. Nothing leaves the machine.
- `-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo` records deep enough stacks for modded
  code (the default of 64 frames cuts 14% of worldgen stacks) and keeps the files here instead of the system
  temp folder.

`ce-server.jfc` lists what is recorded: CPU stack samples every 10 ms, allocation samples, GC, lock waits
over 20 ms, Minecraft's own tick-time and chunk-generation events, and the pack's own `creativecommon.*`
events: every tick, each dimension's tick, chunk waits, KubeJS handlers by script and line, entity and block
entity time by type. Its header comment has the details.

**Cost.** A/B-tested 2026-09-28 on a scratch copy of this server: boot took 51.2 s and a 2601-chunk Chunky
pregen took 1:34 both with and without the recording. The difference was smaller than the run-to-run noise.

To turn it off, delete the two `-XX:` options from the `JVMARGS` line and restart.

## After a crash or a bad stretch of lag: `perf/report.sh`

On the server machine, in the server folder, as the user that runs the server. It works with the server
running or already down:

```
perf/report.sh                 # the last 30 minutes (all the recording keeps)
perf/report.sh 10m             # a shorter window (s, m or h)
perf/report.sh 10m lagspike    # adds a word to the name
```

It takes 2-15 seconds and writes `perf/reports/<date>-<time>[-tag].zip` (`.tar.gz` if `zip` isn't installed),
usually 20-100 MB. Copy that one file to the dev instance and run there:

```
python3 tools/perf/ingest_report.py <file>.zip
```

and open `perf/incoming/<name>/index.html`.

Linux only for now (the pack's server runs Debian). On Windows use `perf\dump.bat` and copy `logs\` and
`crash-reports\` by hand.

What goes in, from an allow-list only:

| In the archive | From |
|---|---|
| `recording.jfr` | the live recording, last `<window>` (server running) |
| `previous-run/` | the run before: its dump on exit from `perf/exit/` (stops and watchdog crashes), or the `perf/jfr-repo/` folder a hard-killed JVM left |
| `logs/` | `latest.log`, `debug.log`, their `*.window.log` excerpts, rotated logs and `logs/kubejs/*.log` from the last 24 h |
| `crash-reports/`, `hs_err_pid*.log` | from the last 24 h, plus the newest either way |
| `jvm/` | three thread dumps 3 s apart, heap, JVM flags, uptime (server running) |
| `system.txt` | load, memory, CPU, disk, CPU/memory/IO pressure, Java version, the server's threads by CPU, OOM kills |
| `server.properties`, `user_jvm_args.txt` | as they are, secrets blanked |
| `files.sha1` | hashes (not contents) of `mods/`, `config/`, `defaultconfigs/`, `kubejs/` and the world's `serverconfig/` |
| `REPORT.md` | what was found and what was missing |

Never copied: the world, `config/` contents, `ops.json`, `whitelist.json`, bans, `usercache.json`. Values of
`password`/`token`/`secret`/`apikey` keys in `server.properties`, `config/*.toml`, `defaultconfigs/*.toml` and
the world's `serverconfig/*.toml` (the Discord bot token lives there) are replaced by `<redacted>` in every
copied text file, and player addresses (`/1.2.3.4:5678`) by `/<ip>`.

## Only the recording: `perf/dump.sh`

```
perf/dump.sh              # Linux: everything kept (up to 30 minutes)
perf/dump.sh 10m          # only the last 10 minutes
perf/dump.sh 10m lagspike # adds a word to the file name
perf\dump.bat 10m         # Windows, same arguments
```

This writes `perf/recordings/server-<date>-<time>.jfr`, tens to a few hundred MB. Dump soon after the lag:
anything older than 30 minutes is gone. Copy it to the dev instance's `perf/recordings/` and run
`python3 tools/perf/perf_report.py perf/recordings/server-<date>-<time>.jfr`; the report is
`perf/reports/<name>.html`.

## From in game: `/ceperf`

For an op (level 3) without shell access to the server:

```
/ceperf status            # is the recording running, which pack events it records, hooks not run yet
/ceperf dump              # saves everything kept (up to 30 minutes) to perf/recordings/server-<date>-<time>.jfr
/ceperf dump lagspike     # adds a word to the file name
```

`/ceperf dump` cannot cut a shorter window; `perf_report.py --last 600` does that on the dev machine.

## If `dump` says there is no jcmd

`jcmd` ships with a full JDK. If the server's Java is a bare JRE, copy the `perf/jfr-repo/<newest folder>/`
folder instead: each `.jfr` in it is a valid recording chunk, and `perf_report.py` accepts the folder.

## After a crash

`report.sh` picks these up by itself; this is what it looks for.

- **Stop or watchdog crash** (a tick hung for 60 s): the JVM exits normally, and `dumponexit` writes the last
  30 minutes to `perf/exit/hotspot-pid-<pid>-...jfr`. `LaunchServer.sh`/`.bat` keep the newest 3. The
  `perf/jfr-repo/` run folder is deleted as usual.
- **Hard kill** (kill -9, the kernel's out-of-memory killer, a JVM crash with an `hs_err_pid*.log`): no exit
  dump, but the run folder under `perf/jfr-repo/` stays, holding the last minutes. Delete old ones by hand
  when you're done with them.

## Don't use `/jfr start`

Vanilla's `/jfr` command records with its own settings, which include a stop-the-world heap census. The
recording above already has everything the report needs.
