# Scratch run 2026-11-04: boot check (b1104)

Owner request: build the recently changed mods and run a brief scratch server to confirm the server starts.
Targeted run, no queue rows, no perf session (single boot).

## Jars

Built 00:38 with `./gradlew :patcherMod:build :creativeDelight:build :creativeBrewing:build :creativeCalendar:build
:creativeCompat:build :creativeWorldgen:build --offline` (all passed). Swapped in by prep_srv.sh: creativeengineering-1.1.08,
creativedelight, creativebrewing, creativecompat, creativecalendar, creativeworldgen (bundle jars unchanged in content;
Gradle kept them up to date), creativedevtools, repackedStorage. Not installed to `mods/`.

## Run notes

- 00:39 prep_srv.sh group A (superflat, peaceful), 366 jars. Boot.
- 00:40 `Done (10.411s)`. No crash report. 359 ERROR lines, none from our jars: Every Compat recipe templates,
  Steam 'n' Rails track loot tables, Redeco mineable tags, Create Goggles Mekanism tag, client-class DISTXFORM, Lightman's
  Discord JDA. KubeJS server.log: recipe parse WARNs from third-party recipes only; startup.log clean.
- 00:42 stopped, JVMs clear.

## Verdict

| ID | Result | Evidence |
|---|---|---|
| boot | PASS | `Done (10.411s)` in srv/logs/latest.log, no errors from creative* or member jars |
