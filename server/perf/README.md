# Server performance recording

The server keeps a rolling recording of what it spends its time on (Java Flight Recorder, built into Java).
When it lags, save the last minutes to a file and turn that file into a per-mod report on the dev machine.

## What runs

`LaunchServer.sh` / `LaunchServer.bat` start Java with two extra options (the `JVMARGS` / `jvmargs` line):

- `-XX:StartFlightRecording=name=ce,settings=perf/ce-server.jfc,disk=true,maxage=30m,maxsize=512m`
  keeps the last 30 minutes, at most 512 MB, in `perf/jfr-repo/`. Nothing leaves the machine.
- `-XX:FlightRecorderOptions=stackdepth=256,repository=perf/jfr-repo` records deep enough stacks for modded
  code (the default of 64 frames cuts 14% of worldgen stacks) and keeps the files here instead of the system
  temp folder.

`ce-server.jfc` lists what is recorded: CPU stack samples every 10 ms, allocation samples, GC, lock waits
over 20 ms, and Minecraft's own tick-time and chunk-generation events. Its header comment has the details.

**Cost.** A/B-tested 2026-09-28 on a scratch copy of this server: boot took 51.2 s and a 2601-chunk Chunky
pregen took 1:34 both with and without the recording. The difference was smaller than the run-to-run noise.

To turn it off, delete the two `-XX:` options from the `JVMARGS` line and restart.

## When the server lags

On the server machine, in the server folder, while the server is running and as the same user:

```
perf/dump.sh              # Linux: everything kept (up to 30 minutes)
perf/dump.sh 10m          # only the last 10 minutes
perf/dump.sh 10m lagspike # adds a word to the file name
perf\dump.bat 10m         # Windows, same arguments
```

This writes `perf/recordings/server-<date>-<time>.jfr`, tens to a few hundred MB. Dump soon after the lag:
anything older than 30 minutes is gone.

Then copy the file to the dev instance's `perf/recordings/` and run there:

```
python3 tools/perf/perf_report.py perf/recordings/server-<date>-<time>.jfr
```

The report opens in any browser: `perf/reports/<name>.html`.

## If `dump` says there is no jcmd

`jcmd` ships with a full JDK. If the server's Java is a bare JRE, copy the `perf/jfr-repo/<newest folder>/`
folder instead: each `.jfr` in it is a valid recording chunk, and `perf_report.py` accepts the folder.

## After a crash

A clean stop deletes the run's folder under `perf/jfr-repo/`. After a crash or a watchdog kill it stays, holding
the last minutes before the crash: copy it the same way. Delete old folders by hand when you are done with them.

## Don't use `/jfr start`

Vanilla's `/jfr` command records with its own settings, which include a stop-the-world heap census. The
recording above already has everything the report needs.
