# Scratch testing gotchas

What goes wrong on scratch servers, scratch clients and qrun batches, and what to do instead. Consolidated
2026-11-16 (plan_process_overhaul.md P2.5): the per-run "Found in rXXXX" sections were merged into the topics below,
with run ids or dates in brackets as provenance. New findings go under the matching topic with "(rXXXX)" provenance
(skill step B5.4), never in a per-run section. A line starting "Enforced:" names the check that now catches it; "partly
checked" means the rule still applies beyond what the tool catches. The queue's own "Setup notes" (config keys,
defaultconfigs, no-player gaps, KubeJS renames) are in
[scratch_queue.md](../../../feature_writeup/testing/scratch_queue.md) under "World groups"; read both.

## Server prep, jars and mod sets

- Enforced: own jars hard-linked or symlinked, own jars changed after prep, jars in srv/mods with no mod_sides.md row:
  `qpin.py` (prep_srv.sh pins real copies and writes OUT/pinned.json; `qrun.py run` refuses on `qpin.py verify`).
  Symptoms seen before the pin: ClassNotFoundException / NoClassDefFoundError in RegisterEvent and "rolling back to
  VANILLA" (parallel `installToMods` during boot), `NoSuchFieldError: ENTITY_TYPE_REGISTRY` (parallel Gradle build
  rewrote a hard-linked jar in place), `NoSuchMethodError` on a Mojang name or ClassNotFoundException mid-load (Gradle's
  `jar` step leaves an un-reobfuscated file), a boot-crashing mixin installed mid-run, Immersive Portals 2.3.7 + Serene
  Seasons' ChunkMap wildcard AT crash at world creation (`MixinChunkMap_C ... cannot reduce visibility`) from an
  unlisted jar (2026-10-21, 2026-10-25, 2026-10-27, 2026-11-01, 2026-11-06, r1102b, r1108hh). Earlier workarounds, now covered by the pin:
  copying dev jars (`COPYDEV=1`, `split_w234.sh`, `drive.sh` decouple), checking jar times before each boot or rerun.
- Never `cp` onto a name that already exists in `srv/mods/` before the pin: those files are hard links to the
  instance's `mods/`, so the copy writes THROUGH into the pack's jar (r1106cbb: prep_srv's swap, stale SWAPS row, `cp -l`
  failed on the existing name, plain `cp` fallback, overwrote `mods/creativecommon-1.1.0.jar`). prep_srv and port_chain
  now `rm -f` the target name first (partly checked: qpin.py pin replaces links with copies after the swaps).
- Server base: libraries + run.sh from `~/.local/share/atlauncher/servers/Minecraft1192withForge` (Forge 43.3.9);
  scratch_server.sh / prep_srv.sh handle it. The old Trash CreEngServer is gone (2026-10-19).
- Mods for the server: every `mods/*.jar` except `client` rows in mod_sides.md; never `server_mods/` (stale).
- Every `*_upstream/` donor folder in the instance root (`ls -d *_upstream`) must be linked into srv/ BEFORE the boot;
  ports that refuse to load without theirs (Vinery, Tempad) fail mod construction. A bundle loads every nested member,
  so a donor is needed for any jar that nests its port, not only the port's own jar (until 2026-11-07 Tempad was nested
  in Compat and a Common + Compat boot failed this way; Tempad is its own jar since then).
- prep_srv's SWAPS table has to follow jar renames: after the 2026-11-04 Common swap the `creativeengineering-*` glob is
  the Engineering jar. Rows pick the newest build jar per project (`newest()`); read the `swap:` lines before booting
  (r1106cbb).
- Power Grid: prep_srv.sh drops any `backportedPowerGrid-*.jar` unless PGJAR names one (2026-11-13: the -17 jar sat in the
  instance `mods/` with no mod_sides.md row). PGJAR before r1115 needed an absolute path; a relative one silently booted
  without Power Grid (a client with Power Grid then crashes on join). prep_srv.sh now resolves it and prints "PGJAR NOT
  FOUND".
- No server boots without Creative Compat: 8 own jars, Devtools included, declare it mandatory (FML refuses at once)
  (r1116). The full pack cannot boot without Creative Delight (Farmer's Delight is nested in it), so a jar-out row for
  CD, Calendar or Compat belongs on the port chain, not the full pack (r1106spfx).
- Reduced and chain jar sets are manifests in `tools/scratch_query/modsets/` (qmodset.py; since r1118 R4 B1):
  `@@modset NAME [VARIANT] [+GLOB] [-GLOB] [-@GROUP]` on the section (`@modset` on a row, `@modset none` = full pack)
  and the driver builds the step from it; port_chain.sh, cc_only_srv.sh and cpp_chain_srv.sh are thin wrappers over
  the same build. A set's jars from CEPortTesting or deploy/latest pin without a mod_sides.md row only when its
  manifest sets `unlisted_ok` (OUT/modset.json; owner ruling 3, 2026-11-18). Enforced: `qrun.py lint` E modset and
  the rows-file load (unknown manifest, variant, group or option); `require` / `forbid` fail the build. A `deploy`
  set's release filter drops every jar not in the release except `dev_keep` (default `cdevperfbot-*.jar`: prep_srv.sh's
  perfBot and its `-Dcdevperfbot.out` JVM arg stay); no `@@prep copy` of perfBot (r1118 follow-up, 2026-11-19).
- Never edit a jar in `srv/mods/` in place: the build hard-links the pack's jars (link count in the hundreds), so a
  `zip` or `sed` on it changes the pack's own jar. Write a new file and rename it over the link (`jar_modver.py`).
- A port_chain set without Creative Delight must also drop `kubejsdelight-*.jar` (`-kubejsdelight-*.jar`; r1106cbp).
- The cc_only set must keep the dev perfBot jar (bots) and drop the pack's `kubejs/data/`: that folder holds Terralith
  noise overrides and crashes world creation without Terralith ("unbound value ... density_function") (r1107cco).
  `kubejs = "probes"` keeps only `server_scripts/zz_*_probe.js` and `data/zq`.
- `scratch_server.sh setup` copies the Creative Labels probe (`zz_cl_probe.js`); in a reduced set without Labels it is a
  KubeJS load error that trips `kjsreject error`. Fixed in qmodset.py (not a check): removed in every set without a
  Labels jar (cpp_chain_srv.sh did it since r1114).
- Third-party fix rows: `tools/scratch_query/tpf_srv.sh` builds the srv and writes the Packet Fixer and Debugify configs
  (header there) (r1114).
- Moving Curios out of a full-pack srv takes its hard dependents (Botania and its addons, Charm of Undying, Corpse
  Curios Compat, Elytra Slot, Shulker Box Slot, Simple Hats) plus Wardrobe and Portable Wardrobe, which use Curios
  without declaring it (crash `NoClassDefFoundError ... ICurioItem`); FTB Teams takes FTB Chunks, FTB Quests, Quests
  Additions, FTBQuestsOptimizer. Compute the closure from mods.toml (`mandatory = true`) before booting (r1107tp).
- Removing a mod jar for a boot: use a fresh `level-name` when the world already holds that mod's blocks (r1116).
- A new own jar's `versionRange` on a third-party mod must use that jar's reported version, often `<mc>-<ver>`
  (`${file.jarVersion}` from the manifest): Raspberry Pie's `[2.2.0]` against Apple Pie's `1.19.2-2.2.0` failed mod
  loading (r1112). Read the dependency's `Implementation-Version` before trusting a range.
- `srv/kubejs/data` is a copy made at prep and outranks the jar: a script fix needs that copy updated or a new prep
  (r1108hh). A probe copied into OUT at setup stays the old copy on a restart of the same OUT (`cbo_run.sh` REUSE): copy
  it in again with PRE after editing it (r1106cbo).
- Chain/instance boots: an empty chain can hide behind a bug that "works" in the full pack (first entry present).
  Compare counts with the full pack (`Found N tags, added X objects`, `removed N recipes`) rather than only checking for
  errors (r1106cbb).
- `/` can be nearly full: check `df -h /`; JFR is capped per boot (`JFR_MAX`).

## Boot and shutdown

- A multi-boot section runs its boots only under the driver (`qrun.py drive`, `@boot N` + `@@prep boot=N:`): a plain
  `qrun.py run` on the file runs every row on the one boot it is given, boot-2 rows included; run it per boot with
  `--rows` and `qrun.py prep FILE SECTION A --boot N --out OUT` between (r1118 R4 A1). A resumed multi-boot step
  needs its OUT from the earlier boots (the driver refuses when it is gone). Enforced: `qrun.py lint` E boot (not a
  whole number), W boot (a lower boot after a higher one in the file, within one driver step: section, world group,
  effective `@modset` and `@phase`; client, draft and superseded rows skipped); `@@prep` boot lines are checked when the rows
  file loads (UPPERCASE prep_srv.sh keys and `fresh` refused where they cannot work).
- A restart inside a row is the `restart [SECS]` directive with `edit CMD` lines before it, not `> stop` and a
  shell boot: a sent `stop` ends the batch, the crash watch reads the exit as the end, a boot wipes `latest.log` (the
  row's offsets go with it) and config edits made while the server runs are often missed (r1118 R4 A2, SQ-WO-10,
  SQ-HH-274). Enforced: `qrun.py lint` E restart (an `edit` with no `restart` after it in the row, `restart` in a
  client batch).
- One game JVM at a time: `game_jvms.sh` empty before every boot. `scratch_server.sh stop` and `stop_wait.sh` can return
  while the JVM is still saving, and a boot then starts a second JVM that hangs in mod loading (2026-10-25, r1108cge2)
  (partly checked: prep_srv.sh and `qrun.py drive` refuse while a game JVM is up; a reboot of an existing OUT through
  `scratch_server.sh boot` does not check).
- Never pipe `scratch_server.sh boot` into `tail`/`head`: the server child keeps the pipe open and the command hangs
  until the JVM exits (r1111 twice, r1113). Boot with `> OUT/boot.out 2>&1 < /dev/null &`, then `wait_boot.sh` under
  `timeout 590`.
- About 60 to 100 s to `Done (`. Wait on `[ -f logs/latest.log ] && grep 'Done (' logs/latest.log`; console.log still
  holds the previous boot until the JVM truncates it.
- Known third-party boot flakes, just reboot: ConcurrentModificationException in Create's
  ArmInteractionPointType.register (Crafts & Additions), SecurityCraft ConfigHandler.loadEffects ArrayList race, and
  "Failed to load datapacks" with an NPE `frame is null` in Rhino `Interpreter.getSourcePositionFromStack` from KubeJS
  `TagLoaderKJS.customTags` console.info on a worker thread (a parallel tag-load race, not our jars; the JVM sits idle:
  stop it and reboot; r1115 phase 4, clean on reboot).
- After an FML crash, a missing-dependency or donor-jar stop, "Failed to load datapacks" or "Failed to start the
  minecraft server", the JVM stays alive, writes its crash report and never reads the fifo, so `scratch_server.sh stop`
  hangs: kill your own pid (cwd under the scratchpad) (r1106cba, r1115). `stop_wait.sh` kills its own pid after 180 s;
  `wait_boot.sh` returns CRASH, MISSING, DATAPACK or FAILED at once instead of waiting out its cap (r1106cbo, r1115).
- `wait_boot.sh` reports CRASH for any old file in `srv/crash-reports/`: move old reports out before the next boot
  (r1102b, r1107tp).
- "Address already in use": a previous scratch JVM still alive with cwd "srv (deleted)" after srv/ was rebuilt; match
  `readlink /proc/PID/cwd` against your scratchpad, kill -9 (partly checked: under `qrun.py drive` the qdrive.py watchdog
  stops the step's server when the driver dies).
- Fixed in scratch_server.sh (not a check): a `stop` left in `in.fifo` by a hung JVM stopping the next boot right after
  `Done` (boot kills the old feeder and makes a fresh fifo; stop's kill line no longer aborts under `set -e`)
  (r1108hh, r1115).
- `stop` hangs after "All dimensions are saved" while an Async Locator search runs: kill the pid after the save line. A
  `/locate` of a rare meta can run 30+ minutes and cannot be cancelled: restart.
- A boot crash on our own files blocks the dev client too (r1103 Boot 0): a `@ModifyArg` handler may take only the
  invoked method's arguments, not the enclosing method's params ("targets a method with an invalid signature"); and
  KubeJS refuses an upper-case file name anywhere under `kubejs/data/*/kubejs/` ("Invalid file name: Uppercase",
  reported as "startup script syntax errors"). Report both to the owner as found in the installed jars / pack.
- Mixins: a Creative Common mixin with a wrong target applies silently (no defaultRequire); Creative Delight / Creative
  Brewing set `defaultRequire: 1`, so the same mixin crashes them. A mixin config plugin's `preApply` runs after Mixin
  conforms visibility (`MixinPreProcessorStandard.attach`): it cannot rescue another mod's `@Overwrite` from an
  access-transformer clash (r1106spfx).
- A data pack that only fails on worldgen registries ("Unbound values in registry worldgen/biome") stops the boot after
  the KubeJS server scripts ran, so their log lines (structure gate, tuning) can still be judged from `latest.log`
  (r1106cbb).
- Never run `scratch_server.sh boot` with captured stdout/stderr: the fifo feeder inherited the pipe and a `subprocess.run(capture_output=True)` never returned (r1118 drive hang; feeder now writes stderr to /dev/null, qdrive logs boot to drive/sNN_boot.txt).

## Driving the server and processes

- Commands go through the stdin fifo (`scratch_server.sh cmd`); console is op, positioned at world spawn. `cmd`/`stop`
  time out after 10 s with "server not reading" when no JVM reads the fifo.
- Kill by recorded pid only. `pkill -f <text>` / `grep "[x]yz"` on ps also matches your own shell when your command line
  contains that text (exit 144); kill qrun by pid from `/proc/PID/cmdline` (r1113). The fifo feeder pid is in
  `srv/feeder.pid`; never `pkill -fx 'sleep infinity'` (kills every session's feeder).
- Waiting on qrun: wait on its pid (`kill -0`) or its output file (`until grep -q "LASTROW \.\.\. [A-Z]" OUT`), never
  `pgrep -f "qrun.py run"` inside `bash -c` / `timeout ... bash -c` (the wait's own command line matches, so it never
  ends) (r1116). Do not wrap `qrun.py run` in `timeout 590`: it kills the batch mid-row; run qrun detached and wait with
  capped waits (r1115). `qrun.py drive` runs detached and resumes after a dead runner (qdrive.py).
- Background `run_in_background` shells and Monitor here do not see the game's pids (`kill -0` / `pgrep` fail): wait on
  files (results `totals:` line, drive logs) with a foreground `timeout 590 tail -f | grep -m1` (r1102b) (partly
  checked: qwatch.py falls back to a files-only crash watch and says so).
- Enforced: a server that died mid-batch making every later row FAIL on timeouts: qwatch.py ends the row CRASH at once
  (dead JVM, new hs_err or crash-reports file). Cause seen: a probe calling `r.getType()` on every recipe (r1102b).
- A console command that throws a `java.lang.Error` crashes the server (see memory `rhino-fieldandmethods-gotcha`).
- `/reload` takes about 50 s here; console commands queued meanwhile all run in the same tick after it (r1115 phase 11).
- `datapack enable "file/x"` answers "Unknown data pack" until a /reload has discovered the pack; a scratch
  `srv/kubejs/data` file is simpler (r1115 phase 11).
- Scratchpad wipes happen mid-run (session restarts): guard multi-step commands with `cd "$OUT" || exit 1`, keep scripts
  in `tools/` and positions or batch results in the run doc as well.
- A driver that deletes a crashed srv dir must copy `logs/kubejs/startup.log`, `logs/latest.log` and `crash-reports/`
  out first (r1103 lost the SQ-ENM-03 startup log and needed a repro boot).
- Tick stalls: a srv-only tick timer script + `jstack <scratch pid>` shows what the server thread waits on.
- `set -o pipefail` with `| grep -q`: grep exits at the first match, the writer dies on SIGPIPE, the pipeline reads as
  "not found". Capture the output first (r1108cge2) (partly checked: qlint W pipefail, batch shell steps only).

## Configs and /reload

- Live toml edits are not picked up on scratch servers: the Forge config watcher thread is parked in ModernFix's
  NightConfigWatchThrottler (same family as SQ-WO-34). An in-place rewrite (`sed > tmp; cat tmp > file`, even a
  same-length `os.write`) is not seen either (no "changed" line in debug.log; r1114 fix_c3a1 FCK-13, PRT-05, superseding
  an r1113 note that it works; not within 30 s in r1116). Set every value with `qrun.py toml` before a boot and split
  value cases across boots (r1108cge, r1114, r1115) (partly checked: qlint W cfgedit).
  Cause (plan_post_scratch_1115b.md E3): night-config's watcher skips MODIFY events with count > 1 (Linux folds
  truncate + write into 2) and never sees a rename. Since the Creative Common build of 2026-11-17 (G11), Common watches
  our own mods' config files itself (MODIFY + CREATE, 1 s debounce, `[creativecommon] <file> reloaded` in the log), so
  an in-place or rename save of an own toml applies about 1 s later; third-party tomls keep the old behavior. Rows on
  an older Common jar keep the qrun.py-before-boot rule.
- `world/serverconfig/*.toml`: edit only while the server is stopped. Forge writes the in-memory config back on
  shutdown, so an edit made while up is overwritten (r1108cge).
- Creative Compat's toml: since 2026-11-15 `/cdevreload compat` (server, op 3) and `/cdevreloadclient compat` (client)
  re-read it at once. The watcher also misses saves by rename (plan_post_scratch_1115b.md E1). For live-reload rows, a
  rename save (`sed -i`, or copy + `qrun.py toml` + `mv` inside `config/` for a key repeated per table) then
  `/cdevreload compat` applies at once (r1116).
- `/reload` does NOT reload WorldgenConfig or other toml values (restart); it re-solves biome_config and scripts but not
  worldgen registries, and drops Chunky's commands (so to load a fixed probe, fix it in tools/ and rerun its rows at the
  next boot; r1103w).
- `/reload` truncates `logs/kubejs/server.log`: read probe results from `logs/latest.log`, and put `kjsexpect` rows
  before any `/reload` (partly checked: qlint W kjs). A manual `/reload` spoils any "as booted" check of that boot
  (r1108cge).
- `ServerEvents.loaded` lines print once per boot, not on /reload: check them with `bootexpect` (r1106cbo).
- KubeJS removed-recipe logging: `srv/local/kubejs/dev.properties` with `logRemovedRecipes=true` (not
  `kubejs/config/`); read at startup only, `/reload` does not pick it up. To find which call removed what, add
  `tools/scratch_query/nch_remove_diag.js` to `srv/kubejs/startup_scripts/` (logs every `global.NCH.remove` filter to
  latest.log, skips filters with an undefined value). A filter like `{output: undefined}` is match-all in KubeJS
  (r1106cba).
- A member config carry (SALT2-08, MemberConfigMove) skips keys the target file already has: the scratch srv copies the
  instance's filled `creativedelight-common.toml`, so strip the target sections before a carry test (r1113).

## Batches: rows, captures and selection

- Enforced: `grep -c` in a shell step without `; true` (0 matches exits 1 = false FAIL; r1106spfx, r1113, r1114):
  `qrun.py lint` E grepc, and `qrun.py run` marks a row HARNESS when a shell exit code is its only failure while its log
  checks pass. Shell output is in `rows/ID.shell.txt`.
- Enforced: a capture (`{SBH}`, markers) set in a row that `--from ID` / `--rows` skips ("Expected integer" on every
  use; r1103 boot 4 retry): `qrun.py lint` E var, and `qrun.py run` marks the row HARNESS. Use `--rows SETUP,ID,...`.
- `qrun.py --rows` also skips setup lines (forceload, bot spawn) that live in an earlier row: put each row's setup in the
  row (r1108cge) (partly checked: qlint W unloaded for forceloads, not bot spawns).
- Reruns of rows written for an older run's world (markers like `s1home`, `rhome` made by earlier boots) need their
  marker setup rows on a fresh world (r1103 phase 2: 13 faults) (partly checked: qlint W marker). Summon a marker only
  after its chunk is forceloaded (partly checked: qlint W unloaded).
- Enforced: a directive (`bootreject`, `bootexpect`) before the first `## ID` row (qrun refuses the batch): `qrun.py lint`
  E parse. Put boot-wide checks inside the first row (r1106cbp).
- Enforced: forceload box over 256 chunks (an unaligned 256-block box spans 17 chunks per axis, "Too many chunks 289";
  r1115): `qrun.py lint` E forceload. Use 240-block boxes.
- Enforced: `execute if biome` (1.19.3+; r1108cge): `qrun.py lint` E cmd119. Read the biome with a probe (`/zgv cfg`,
  `/cdevtest state X Y Z`). Not covered by the lint: 1.19.2 also has no `contents` slot for item entities (`item replace ... from entity
  @e[type=item] contents` = "Unknown slot") and no `execute if loaded` (r1107tp2).
- A big up-front `forceload add` over every test area (SQ-PWG-P11A) breaks rows that unload a chunk on purpose
  (SQ-PWG-21 "chunk 65 loaded=false"): rows that test unloading need an area outside the blanket forceload (r1114 P11).
- A regression batch that copies rows from many gate batches keeps their hidden dependencies: a "boot row" the plan drops
  can be the one that builds a fixture (SQ-PWG-76 built the chain source board for -78 to -82). Grep the later rows for
  coordinates no remaining row places before dropping a row (r1114 P11).
- Moving a row to new coordinates with a regex shift: also shift the coordinates inside the expect regexes (`basin at
  114[26] -60 1890` kept the old X and failed a passing row) (r1114 P11).
- A new probe command gets one live call before its batch relies on it (r1103w).
- Falling block placed with `setblock` schedules its own fall 2 ticks later: a `/cdevtest blocktick` sent at pace 2 sees
  air. Send both under `pace 0`. Same for a 40-tick `/cdevtest watch` (2 s): the change must go in under `pace 0` with a
  short `sleep` (r1116).
- `latest.log` rolls over at midnight; the qrun row running then gets an empty segment (r1108hh).
- Rows written for the r1103 slice batches use `{SAH1..3}` / `{SBH1,3}` home captures that only
  `tools/scratch_query/batches/r1103/sA_*.qb` / `sB_*.qb` set: in a rows-file run they are never set (r1120nv, 20 rows
  HARNESS in the first 10 steps). Each row summons and captures its own `sahome` / `sbhome` marker (model:
  `cdj5home` in CDJ.qb) (checked: qlint W var, `qprescreen.py` class likely).
- Marking a row `@superseded` after a drive was planned: the step keeps the id in its row list; qrun prints
  `SUPERSEDED ... not run` and qdrive drops it, so the step does not end `incomplete` (r1120fix).

## Regexes and log matching

- Enforced: a regex that does not compile, or with `(?i)` after its start (Python 3.10 warns and applies it to the whole
  regex; r1107cco): `qrun.py lint` E regex, and `qrun.py run` marks the row HARNESS.
- Enforced: a waitlog that matches an earlier row's reply, incl. a `/locate` waitlog caught by an EARLIER locate's late
  reply (rerun_b's third corn maze search answered inside c4_b's savanna locate wait; r1114): `qrun.py lint` W waitlog.
  Make every locate waitlog name its own target (`The nearest <id> is at|Could not find a structure of type "<id>"`).
- Fixed in qrun (not a check): the same waitlog pattern twice in one row used to return at once on the first's line
  (r1115); since r1118 (judge-14) a repeat waits for one more matching line than the earlier waitlog saw, so the
  W waitlog2 lint is gone. Two replies landing before the first waitlog returns still leave the repeat waiting.
- `bootreject` matches the whole log, including the replies to the batch's own commands (a `give` of an unknown item
  prints `Unknown item`): exclude the console reply with a lookahead, `^(?!.*MinecraftServer/\]: Unknown item 'x').*`
  (r1113).
- Anchor words with `\b`: `[Cc]ycle` as a bootreject matched Forge's "recycleShowTicks" config line (r1106cbo).
- Mixin's "Static binding violation ... cannot reduce visibiliy" (its typo) is a WARN, not the Immersive Portals crash: a
  regex for "cannot reduce visibility" must exclude "Static binding violation" lines (r1107cco).
- Mixin compartment names have `_`: match `Mixing [A-Za-z_.]*`.
- Goggle tooltips (`addToGoggleTooltip`) on gauges call `Minecraft.getInstance()`: on a dedicated server that is a
  RuntimeDistCleaner ERROR and the probe command aborts. Wrap in try/catch; never use a blanket `/ERROR\]` reject in
  batches that read gauges (`reject ^(?!.*RuntimeDistCleaner).*(Exception|/ERROR\]).*(mod)`) (r1113).
- `kjsexpect` reads `logs/kubejs/server.log` only; startup-script prints (`console.log` in startup scripts, `[NCH]
  removed`, `transition items`, `[compost]`) are in `startup.log`: check them with `shell grep ...
  logs/kubejs/startup.log` (r1106cbo, r1106cbp).
- Output formats: `data get block` prints `Slot: 7` for a byte in some containers and `Slot: 7b` in others (write
  `Slot: 7b?`; r1113). NBT dumps do not sort keys (`{id: ..., Count: 1b}`) and floats print in full (0.3 is
  0.30000001192092896): write row regexes for both (r1108hh). Command output pads "Gave 1    [", and attribute lines read
  "for entity NAME" (r1115). Swap remaps log only a summary line `[swaps] minecraft:item: N id(s) remapped`, not per-id
  lines (r1115).
- The system grep is ugrep: long `{0,300}` regexes fail ("exceeds complexity limits"); parse big logs in python (partly
  checked: qlint W ugrep, batch steps only).
- Not ours (ignore): optional-mixin "Error loading class" WARNs, advancement parse errors, the Discord bot JDA error,
  MoreJS "Sell item cannot be empty", `PoolElementStructurePiece ... minecraft:empty is not valid` at save, 7 Create
  Mechanical Extruder recipes with an abstract `getType()` in `dumprecipes`.
- Dedicated-server dumps: recipes, tags, loot, structures, biomes, spawns, trades are good; names, stacks, effects, gear,
  food, crops, compost, paintings need the client. The /dumpall recipe dump has only id + type: use `/cdevtest rdetail`.
- A reject on `Unknown command` never matches: 1.19.2 prints "Unknown or incomplete command"; reject `Unknown or incomplete command|Incorrect argument` (r1118 judge-2).

## World, chunks and ticking

- Forceloaded chunks with no player get no random ticks (`/cdevtest randomtick BOX N` gives them without one). To force
  a leaf decay check, `fill` stone next to the leaves then air. After a restart wait about 30 s after `forceload add`
  before scheduling block ticks.
- A restart with no bot or player online gives forceloaded chunks no random ticks: crop/hops rows after a restart need a
  bot beside the site (r1113 rerun_a2). After a restart, lanes outside the setup forceload do not tick: forceload again
  after every boot (r1115 phase 11).
- A level with no player and no forced chunk stops ticking entities 300 ticks after start: effects on summoned mobs never
  apply (golems stayed at 100.0). `forceload add` the chunk first (r1106cba).
- An entity summoned right after `forceload add` in an unloaded dimension is not found by the next command: wait a few
  seconds.
- Platforms in unloaded chunks are not placed: `forceload add` before `fill`, or bots fall into caves (r1107cco).
- Big `/forceload add` (100+ chunks) generates synchronously: `max-tick-time=-1` (set by scratch_server.sh).
- A fresh world can start in rain: rain-fed blocks (Hearth and Harvest troughs) fill with water and then refuse items.
  `weather clear` does not stop it at once (the rain level fades over several seconds): wait about 20 s, then place
  (r1112, SQ-HH-223, -233).
- End main island: y 70 at 0 0 is inside the terrain; NoAI mobs summoned there suffocate within about 10 s (use y 75)
  (r1108cge).
- A wide entity at the edge of a cleared column wedges in the terrain (a boat is 1.375 wide, drop 0.000): clear a margin
  (r1108cge2).
- Block drops here scatter more than 8 blocks from a survival break: collect with a radius of about 24 (r1107tp).
- Dropped items and orbs held in webs keep ageing and despawn at 5 minutes: a restart half must read them within 5
  minutes of their summon (r1115 and r1116 both lost a first try). Summon them in the boot before the restart, right
  before `save-all`.
- Empty minecarts on rails lose 4 % speed a tick: start them close to the target (r1115 phase 11).
- A salt or snow layer resting ON ice pops as an item the moment the ice melts (support loss in the same `setBlock`), so
  a melt listener that removes the source layer never sees it. Put the source beside the target (r1116 phase 3).
- Melt targets in 1-deep holes (y -61 on the flat world) keep the melt water from flowing; `randomtick` only the target
  cell (`nofluids`) so neighboring salt does not get its own dissolving tick (r1116 phase 3).
- A row that builds at y -60 assumes the superflat (group A); in a group B normal world it builds underground and finds nothing (SQ-BREED-03, r1118). Keep such rows in group A or build relative to the surface.
- With no player online and no forced chunk, entities stop ticking: spawn a bot (or forceload) before entity rows (SQ-EFX-02, -04, r1118). Console command pacing is too slow to catch short-lived entities such as TNT shrapnel: slow them with a `@@prep` config value.
- A big `forceload add` box in a fresh normal world generates every chunk synchronously on the server thread, inside the console command; all later console lines, `stop` included, wait behind it (and SIGTERM's shutdown waits too). Six 169-chunk boxes held a group B server for over 10 minutes after its rows ended (SQ-HCS, r1118). Keep forceload boxes small in group B, or `/cdevtest tickwarp` after each one.

## Difficulty, spawning and mobs

- prep_srv servers run `spawn-animals=false` (peaceful; set `spawn-npcs` too): ServerLevel DISCARDS every `Animal`,
  `WaterAnimal` and `Npc` on its next tick, summoned and NoAI ones too (pigs, cows, wolves, striders, villagers gone a
  tick later: "No entity was found"). Rows with animals or villagers need `prep_srv.sh OUT LEVEL A spawn-animals=true
  spawn-npcs=true` (gamerule doMobSpawning false still stops natural spawns); iron golems survive either way (r1114).
- Peaceful difficulty removes summoned hostiles (witch, husk) (r1113). `cc_only_srv.sh` and `cpp_chain_srv.sh` servers
  are peaceful unless `difficulty=normal` is passed (r1108cge).
- Peaceful refills food (1 per 10 ticks while hungry), players and bots alike: a `hunger 0` then an eat 2 s later reads
  +4 over the item's nutrition (r1113 c2a1 "every eat +4"), and `pbot hunger A 10` is back at 20 before an eat at pace 2
  (`result=FAIL`). Set `gamerule naturalRegeneration false` in the setup row of any row that reads foodLevel deltas
  (r1114), or `difficulty easy` for the row and back to `peaceful` after (r1116 group A).
- Chunk-generation animal spawns are sparse in this pack: to test a spawn hook, a scratch-only
  `kubejs/data/zz_test/forge/biome_modifier` forcing the mob. Adding a placed feature to another biome in the same step =
  feature order cycle crash at spawn.
- Phantoms (SQ-PSU-04, SQ-CC-10b): the group A superflat reports sea level 63 and the phantom spawner skips every player
  below sea level, so bots at y -60 never get one (r1102b's and r1107cc's first two tries). Put the bots on platforms at
  y 70 (sky above), with `spawn-monsters=true`. `/zph N` (`cc_phantom_probe.js`) runs N fresh spawner passes at once.
- NoAI mobs do not move at all in 1.19 (no fall): use a mob with AI (Silent) for fall or gravity checks (r1107cco).
- `summon ... {ActiveEffects:[...]}` is ignored in this pack: a slowness NBT does not slow the mob. For standing mobs set
  base `generic.movement_speed` to 0; push them with a probe motion command when a row needs them moving (r1115 phase
  11).
- Zombies do not chase perfBot bots, and bots are invulnerable by default: damage rows need a mob (100 hp zombie)
  instead (r1115 phase 11).
- `@e[...]` with no x/y/z/distance searches every dimension, even under `execute in`: pin cross-dimension selectors
  (r1107cco).
- `@e` (and `@e[tag=...]`) never matches a dead entity (the selector adds `Entity::isAlive`): select a dying mob by UUID
  string (`data get entity 00014b89-0000-0001-0000-000200000003 DeathTime` for `UUID:[I;84873,1,2,3]`) (r1114).
- `/cdevtest kill` reports `xp=` from the orbs: 1.19.2 drops them inside `die()`, so xp is above 0 for animals (r1116).
- `zpth watchnew` tags every entity of the type in its box that lacks the new tag, so an earlier projectile still hanging
  there joins the new watch and its `count`: use a fresh box or kill the old one first. A per-5-tick `zpth watch` hides
  climb and sink rates in webs; use every 1 tick for rate rows (r1116 phase 2).

## Bots and fake players

- perfBot (`/pbot spawn NAME X Y Z`, `hold`, `use`) gives rows that "need a player". Bots are invulnerable unless `pbot
  vulnerable NAME on` (kept across a respawn). Since 2026-11-15 the jar has test verbs (attack, interact, punch, break,
  useitem / eat with a real ticked use, hunger, sneak, look / lookat, moveto with physics, inv get / set / select, pickup
  on / off): prefer them over a KubeJS player probe (creative_devtools.md). Details: memory `perf-suite-bot-players`.
- perfBot bots are real ticking ServerPlayers: they pick up XP orbs, Mending repairs on them, and
  `tryToStartFallFlying` makes them glide (SQ-QN runs on a bot via `zqn_probe.js`) (r1115).
- A bot within about 2 blocks picks up item entities and XP orbs, creative mode included: stand it 6+ blocks off when a
  row counts items on the ground or orbs, and read the bot's `Inventory` as well as `drops` (r1108hh, r1112, SQ-HH-461,
  -1529). Bots also get the starter kit a few seconds after spawn, off-hand item included: `clear` after a wait (r1108hh).
- `pbot eat` needs food below 20 (`pbot hunger NAME 10`) unless the food is always edible; its result line comes when the
  use finishes (`verb=pbot.useitem ... end=finish`, 32 ticks for bread), not with the command reply.
- `pbot attack ... weak` reads the bot's real charge: 2 s of pace recharges it to `strength=1`. Under `pace 0` the weak
  hit lands in the target's hurt cooldown (no damage), which is vanilla (r1116).
- `pbot hold P ITEM` puts a FRESH stack into slot 0 (replacing what was there); it does not select an inventory stack.
  Charge or tag the held item after the hold (`/ztp setchr P 0 N`), or set the hand with `item replace entity P
  weapon.mainhand with ITEM{nbt}` to re-place a dropped block with its exact NBT (r1107tp2). A `/give` to a bot fills
  slot 0 = the main hand, and `item replace ... weapon.mainhand` overwrites slot 0: keep the item under test (a Tempad)
  in another hotbar slot (r1107tp).
- Sneaking: `pbot use` is a plain use (on a chest it opens it; r1111). Shift-use needs `p.setShiftKeyDown(true)` (`/ztp
  sneak P on`, r1107tp); since 2026-11-15 perfBot has a `sneak` verb. To place items onto interactive blocks, call the
  item's placement code from KubeJS or write the block entity NBT (r1111).
- Bots never close a screen: after a block or item use opened a menu, `containerMenu` stays that menu, so inventory
  clicks go nowhere. Click `p.inventoryMenu` directly (`/ztp click P SLOT BUTTON inv`); `closeContainer` is not callable
  from Rhino on a BotPlayer (r1107tp).
- Turning bots: Rhino finds no `setYRot` on perfBot's BotPlayer (nor on a FakePlayer, r1113): use `tp <bot> ~ ~ ~ <yaw>
  <pitch>` (r1115 phase 11).
- perfBot relogs a bot that dies (`/kill`: remove + spawn, no `PlayerEvent.Clone`). Never call `PlayerList.respawn` on a
  bot as well: perfBot keeps ticking the stale object and the server crashes (`DistanceManager` NPE in
  `Bots.onServerTick`). Clone-path checks need a client death (r1107tp).
- `pbot spawn` loads the saved player (dimension, inventory, respawn point) and then teleports to the given position;
  read the login dimension with a `PlayerEvents.loggedIn` probe (`nsm_probe.js`). A killed bot respawns by logging in
  again, so a bed-respawn walk cannot be judged with a bot (r1111). A row that restarts a world reloads the bot's saved
  inventory (SQ-CGE-10 kept the backtank from -10b): clear the slot at the start of the row (r1108cge2). A bot keeps its
  effects across `pbot remove-all` / respawn: `effect clear` before a control pass (r1107cco).
- Loading Protection (`protectionDuration = 150` in the pack) cancels every `LivingAttackEvent` whose attacker is a
  player in the first 150 s after that player logs in: a row that spawns a bot and hits with it does nothing ("hit result
  false"). Set `qrun.py toml OUT/srv/config/loadingprotection-common.toml <section> protectionDuration 1` before the
  boot, or hit with a non-player source (r1111 SQ-HH-424, -425, -453). Since 2026-11-15 perfBot bots are exempt one tick
  after `pbot spawn` (login spot set one block above the target, so the spawn teleport counts as a move; SQ-TX-12 checks
  it); real players are not.
- Player-only command trees (FTB Teams `party create / invite / join`) do not parse for `execute as <bot>` ("Incorrect
  argument"): call the mod's API from a probe (`/ztp ftbparty`) (r1107tp).
- `/cdevtest` without `as=NAME` acts as a Forge fake player of that dimension: no ticking (no use duration), inventory
  cleared every call. Use a bot (`as=NAME`) when the row needs a real player's state.
- Forge FakePlayer from KubeJS: `FakePlayerFactory.get` + `p.gameMode.useItemOn(...)` / `destroyBlock` / `useItem`;
  menus open (check `containerMenu`). FakePlayer does not tick, so `startUsingItem` never completes: eat with
  `finishUsingItem` + `ForgeEventFactory.onItemUseFinish` (`tools/kitchen_probe/serve_items_probe.js`).

## Rhino and KubeJS probes

- NEVER load an ungenerated chunk from a KubeJS command (`getChunkAt`, `getBiome`, `getHeight` on new land): the server
  thread holds Rhino's lock while waiting for generation and a worldgen worker needs it = deadlock, kill -9. `forceload
  add` first (vanilla, no Rhino), probe, `forceload remove`. Biome without chunk loads:
  `getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(qx, 16, qz, randomState().sampler())`.
- Ready probes: `/cdevtest` first (Java, cannot crash the server or deadlock; creative_devtools.md), then
  `tools/creative_labels_test/server_probe.js`, `tools/leaf_path_probe.js`
  (place a ConfiguredFeature).
- Never call `r.getType()` while looping `getRecipeManager().getRecipes()`: Mechanical Extruder's
  `ExtrudingRecipe.getType()` throws AbstractMethodError, a Java Error that Rhino's try/catch does not stop, and the
  server crashes (r1102b, r1113 c4_a). Use `getAllRecipesFor(RECIPE_TYPE.get(rl))` or
  `ForgeRegistries.RECIPE_SERIALIZERS.getKey(r.getSerializer())`.
- KubeJS class filter ("Class is not allowed by class filter": the whole script fails to load and its command never
  registers): `net.minecraftforge.fml.ModList` (use `Platform.isLoaded(modid)` / `Platform.getMods()`, a map id -> mod
  info with `getVersion()`), `io.netty.*` (no `Unpooled` buffers: packet round trips cannot run in a probe),
  `getClass()` (use `instanceof`), `java.nio.file` (write files with `JsonIO.write`) (r1106cbo, r1113, r1115).
- Any probe class import from an optional Creative jar must be lazy (the retired `/zq` loaded the Calendar class lazily; `/cdevtest season` reads it reflectively), or the probe
  fails to register in boots without that jar (r1106cbp).
- Shared script scope: unique local names; find a level by iterating `server.getAllLevels()` (getLevel overloads are
  ambiguous); a dimension string argument stops at `:`.
- `level.dimension` is a ResourceLocation property in KubeJS: `l.dimension.location()` throws, `level.dimension()` is
  ambiguous (use KubeJS `getDimension()`; r1108cge), and on a level from `server.getAllLevels()` `level.dimension()`
  throws "not a function" (read it in a try, fall back to `level.dimension`; r1114). Outside a JS try in a command
  handler such an error surfaces only as "An unexpected error occurred trying to execute that command" (no stack trace),
  and every call of that command fails (r1103 zlbs / zsb). Use `server.overworld()`.
- Declarations: a loop-body `const` keeps its first value (memory `rhino-fieldandmethods-gotcha`;
  `tools/rhino_const_loop_check.py` lists them). A `const` inside a function that has a `try` (or inside the `try` of a
  nested function) throws `InternalError: TypeError: redeclaration of var X` at call time: use `let` or `var` (r1108cge,
  r1113 pg_solver_probe.js, pwg_probe.js). A `function f() {}` declared inside an `if` / `else if` block of a probe
  command is undefined inside that block's `forEach` closures ("f is not a function, it is undefined"): use `let f =
  function () {}` (r1103w `/zq under`).
- Two script packs each have their own `Object.prototype`, so `Object.getPrototypeOf(v) === Object.prototype` in a
  startup helper is false for every object a server script builds: use `Object.prototype.toString.call(v)`.
  `tools/rhino_scope_test/run.sh` runs a snippet in the pack's Rhino with two scopes (r1106cbb).
- JDK private collection classes: calling a member on what `Collections.unmodifiableMap(...).entrySet()` (or any
  `Collections$Unmodifiable*`, `HashMap$KeySet`, ...) returns throws `IllegalAccessException ... cannot access a member
  of class java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet`. Copy into public classes first: `new
  HashMap(map)`, then `new ArrayList(hm.keySet()).forEach(k => hm.get(k))` (r1103w `/zq under`).
- A `/cmd` argument is a Java String: `String(arg).trim().split(/\s+/)` before a JS regex split, or it never splits
  (r1108hh, r1113 zc4).
- Ambiguous overloads (`The choice of Java method ... is ambiguous`) and fixes: `Entity.setDeltaMovement(3 numbers)` (use
  the 8-argument ItemEntity constructor); `moveTo(5 numbers)` resolves to `teleportTo` (use `absMoveTo`);
  `BlockPos.relative(dir, n)` and `Direction.getStepX` (step by axis by hand); `ItemStack.is(TagKey)` and
  `BlockState.is(TagKey)`, whose explicit-signature form is not remapped either (use
  `ForgeRegistries.X.tags().getTag(key).contains(...)`); `new ItemStack(item)` (use `item.getDefaultInstance()`);
  `AllDisplayBehaviours.sourcesOf` and `ChrononHandler.of(blockEntity)` (call by signature, e.g.
  `$Cls['of(net.minecraft.world.level.block.entity.BlockEntity)'](be)`); `isFluidEqual(fs)` (ItemStack overload)
  (r1107cc, r1107tp, r1113, r1115). `Entity.distanceToSqr` with 3 numbers picks the `Vec3` overload (`m_20238_`) and
  throws: compute the distance from `getX/getY/getZ` (r1111 `hh_crow_probe.js`). `new SimpleContainer(2)` resolves to
  the varargs constructor (size 1) (r1115). `JsonObject.addProperty('k', 400)` and `addProperty('k',
  Integer.valueOf(400))` both write the STRING "400.0": use `json.add('k', JsonParser.parseString('400'))` (r1107cc).
- Names Rhino does not reach: `Ingredient.getItems` (use `getStacks().toList()`); `ItemStack.getTag` (use `getNbt()` /
  `.nbt`; on a stack from `getMainHandItem()` or `ItemEntity#getItem` "Cannot find function getTag": save the stack to a
  CompoundTag); `ItemStack.hasTag()` (read `.nbt`); `level.getGameTime` (use `getLevelData().getGameTime()`);
  `getStringUUID`; `DamageSource.getMsgId` (read the field); static getters like `ModdedFluids.acid()` (look fluids and
  items up by registry id) (r1107cc, r1107tp, r1108hh, r1113, r1115). `java.lang.Integer` as a bare global throws an
  uncatchable error inside a command handler: use `Java.loadClass` (r1107cc).
- `ForgeRegistries.ENTITY_TYPES.getKey(e.getType())` and `Registry.ENTITY_TYPE.getKey` return `minecraft:pig` for every
  entity from Rhino (a contraption scan reported "contraptions 0" for a working bearing; r1113 pwg10b, r1114 `zc3
  portals` read 0): use `e.getEncodeId()`. KubeJS `entity.getType()` returns the id string (r1108cge). A goal's
  `String(goal)` is `Class@hash`; use `getClass().getSimpleName()` (r1111; note `getClass()` is class-filtered in other
  probes, r1103, r1113). Avoid `getClass()` in probes.
- FluidStacks: KubeJS converts every FluidStack argument and a raw one does not survive (`fl.isFluidStackIdentical(fl)`
  is false; IE's `FluidTagInput.testIgnoringAmount` / `BottlingMachineRecipe.findRecipe` / `RefineryRecipe.findRecipe`
  rejected everything, which made every IE fluid recipe look broken): pass `Fluid.of(id, amount, nbtString)`
  (`cc_seqasm_probe.js` `zsaFluid`; r1107cc). A Forge `FluidStack` built in a script can reach `fill()` as a
  non-matching stack (accepted 0; use `Fluid.of(id, n).getFluidStack()`, r1108hh) or arrive EMPTY in Java (r1113): fill
  tanks by writing their NBT (`TankContent`, `InputTanks`, `Tanks`) instead.
- KubeJS `stack.enchant(...)` returns a COPY. Create's `SequencedAssemblyRecipe.advance` is private: walk an assembly
  with the public `SequencedAssemblyRecipe.getRecipe(level, stack, type, ProcessingRecipe)` and the step's
  `rollResults()` (`/zsa <id>`) (r1107cc).
- KubeJS 1902.6.2-build.73 never posts `ServerEvents.afterRecipes`: a listener there never runs (no error either)
  (r1106cbo).
- A probe that samples running goals misses a flee that ends during a `sleep` before the watch: start the watch right
  after the summon (r1111).
- Probe command arguments: `/cdevtest` box verbs take `dim=` (the retired `zq count` took an 8th argument, the
  dimension), default the caller's level, overworld from the console (r1106spfx). `zpwg ns` prints only the first 20 ids per registry; `zpwg ns <ns> all` prints every id
  (r1114). `/syt` with a tool string carrying NBT (`id{...}`) throws "An unexpected error occurred": use
  `<id>@fortune=N` (r1115). `/cbo effects` counts ITEM namespaces of the Armor / Tool Effects rows; a mod that only
  contributes effect ids never appears there (r1106cbo).

## Mod-specific behavior

- Create: machines placed before their creative motor often stay at speed 0: place the motor (and shaft or gearbox)
  first, then the machine, and read `speed` before relying on it; creative motor default is 16 RPM (r1113). A mixer or
  arm turns only from a meshing cogwheel, not a gearbox under it; an undrained millstone deletes outputs once its slots
  fill (r1113 pwg17, c4_ar). Fluid tanks ignore bucket use from a bot, vanilla water included (r1108hh). Create 0.5.1's
  value box input handler skips `testHit` for a FakePlayer and replays the click as `BlockState.use` at the block
  centre: a probe clicking a value-box block (modular display slots) must call `BlockState.use` itself (r1113).
- Create Gravity 1.2.0 on a dedicated server was inert until a `/reload` (its biome list is built only on
  TagsUpdatedEvent after its server config loads); Common 2026-11-08 00:49 builds it at server start, so with an older Common
  a gravity row needs a `/reload` first (r1108cge). The low-gravity modifier is added only while an entity falls (on the
  Common-only set it was never added at all, TODO `creativecommon/publish #3`; r1107cco) and is transient: `data get
  entity ... Attributes` never shows it; read it with `attribute <target> forge:entity_gravity get` (0.016 = factor
  -0.8) (r1108cge).
- MCA replaces summoned villagers, so `kill @e[type=minecraft:villager]` misses them (kill by box) and they regenerate
  (compare health as deltas) (r1113). With `overwriteOriginalZombieVillagers` true a summoned
  `minecraft:zombie_villager` is replaced on join by an MCA one that has lost its vehicle and `ConversionTime` (-1):
  summon `mca:male_zombie_villager` directly; it converts to `mca:male_villager` (match `type=#zq:villagers`). Tags do
  not survive the conversion (r1114).
- Alex's Mobs crows pick up ground items and fly off with them; with `spawn-animals=true` a "missing" dropped item may be
  in a crow's HandItems (find it with `region_ids.py` on `entities/`) (r1111).
- Serene Seasons melts open-sky ice and snow at any randomTickSpeed (seasonal surface melt), which confounds every melt
  row: no-source controls melt too. Put a glass block right above every ice or snow target (Serene Seasons skips covered
  blocks) and keep one roofed no-source control per batch. Generators: `tools/scratch_query/melt_gen_p3r.py` (r1115).
- Rare Ice (`config/rare-ice.properties` `allowInsertingItemsToIce=true`) takes a right click on `minecraft:ice` with any
  item and turns the ice into `rare-ice:rare_ice` holding it: a row that clicks plain ice tests Rare Ice, not the item;
  set `allowInsertingItemsToIce=false` in the srv copy (read at common setup: restart) to test the item's own code
  (r1116 phase 3). A bare `setblock rare-ice:rare_ice` becomes plain ice; it needs an item in its block entity (r1115).
- Bare tilled end stone (Ender Crop's after the swap) turns back into end stone on its first random tick (r1115).
- Botany Pots 9.0.44: right-clicking a pot opens its GUI (nothing inserted); `getRequiredGrowthTime` is on
  `be.getInventory()`. Use `/syt potset` and `/syt pot` (r1115).
- Supplementaries jars refuse Forge water through the fluid handler; write
  `FluidHolder:{Fluid:"minecraft:water",Count:4}` (r1111).
- Gun portals: the heartbeat is phased per portal and the tick count restarts at load, so a long `heartbeatSeconds` beat
  can fire seconds after Done: rows that read saved portals after a restart need a beat longer than the read (600)
  (r1114). Gun portals despawn on reload. Plain ignition fails by design: light frames with
  `creativemagic:flint_and_terrasteel`. A walkable wall portal from `zc3 shoot`: 2.5 blocks from the wall, pitch 20
  (r1115 phase 10).
- Translated donor data can vanish silently: a port that converts upstream data at load may drop whole files (debug.log
  only). `/loot spawn ~ ~ ~ loot <ns>:blocks/<block>` = "Dropped 0 items" is the quick check (r1107tp).

## Worldgen and structures

- New level-name per run = fresh world; any worldgen-registry or biome-layout change needs one. Repeat searches in the
  same world reuse checked chunks: use `execute positioned X 70 Z run locate ...` from a fresh far origin.
- Rare structures: pregen + offline beats `/locate`: `structureDensity` x4 in the srv toml, Chunky square, then read
  starts / pieces / biomes from region files (`tools/seabed_heights.py` nbt reader; it runs main on import, copy its
  `nbt()`; patch it to read type 11 int arrays). Earth-fill `log = ALL` lines also name starts checked during other
  locates. Give pregen waits a timeout (a third-party chunk-save CME once stalled Chunky forever).
- Creative Worldgen ends every structure search after `[worldgen.locate] searchTimeoutSeconds` (120 s shipped) whatever
  the batch's waitlog says: a row that needs a longer search sets that key in the srv toml before the boot (corn maze
  from the c4b spawn took 424 s) (r1114).
- `creativeworldgen:meta/*` are structure ids and `creativeworldgen:group/*` structure sets, not structure tags:
  `/locate structure creativeworldgen:meta/waystone_ruins` (no `#`); a group is checked through a member or `/cwglocate
  census <radius>` (the radius is required) (r1114) (partly checked: qrowlint.py flags an unknown `#tag` in row
  commands).
- Seed 8675309: spawn area is 76% ocean; 2144 -2592 is 92% land. structure_starts-status chunks have placeholder biomes
  (use getNoiseBiome). Jigsaw biome check point = center of the start piece.
- `locate structure #tag` logging `NamedSet(...)[[]]` = the tag is empty at runtime.
- Checks: `[structure-tuning]`, `[structure-frequency]`, `[pool-tuning]`, `[biome-merges]` lines; `dumpstructuregen` then
  `tools/check_structure_live.py`; `/cdevplace structures @ns ...` proves templates load.
- Earth fill judges (`efb_judge.py --except`): a bare `mine` in the regex matches every `minecraft:` id and hides all
  vanilla structures; write `:mines?\b|_mine\b` (r1103w). `/cdevtest under` lines (and the retired `[zq] under` lines) carry no dimension: keep Nether and End
  probes out of an Overworld judge's log window. `/place structure` stores no start and runs no earth fill, so a placed
  structure is invisible to `/cdevtest under`; earth fill rows need natural starts.

## Clients

- Rows that need a client AND a dedicated server up together are run by the agent (owner ruling 2026-11-16; the
  r1108cge2 note that they go to the owner is void): boot the run's own server, then the client with
  `eval_client.py launch ... ownsrv=PID` (r1115 phase 10), or `qrun.py pair` (qpair.py). Both JVMs are the run's own;
  nothing else may be up. One-off client steps: `tools/scratch_query/cli_do.py`.
- `eval_client.py` hard-links the instance `mods/`, so a jar only in build/libs is not in the client. A scratch world goes
  into the client with `sp_world.py` (allowCommands on). `qrun.py client` types chat via xdotool.
- 420 or more jars in the client's mods/ kill it natively at window creation (`*** stack smashing detected ***:
  terminated` in launch.out, about 23 s after launch, right after KubeJS "Loaded client.properties"); 419 start. Any jar
  tips it (Packet Fixer alone, Debugify alone, both swapped against Clear Void tested r1114). Count `ls GAME/mods/*.jar
  | wc -l` before a client launch; the instance mods/ had 421 on 2026-11-14.
- `eval_client.py setup` always copies `zz_perfeval_drive.js` into the client's server_scripts: it sets spectator and
  teleports the player every 30 s for 3 min after the join. Delete it before any gate or queue client batch (r1114).
- A throwaway client needs `config/debugify.json` (tpf_debugify_cfg.py): the instance has none, so Debugify would run all
  69 fixes, including MC-121706 that crashes the integrated server boot (r1114).
- This client log has no `Resource reload finished` line: do not wait on it to open a world (open by hand or wait on
  `Sound engine started` plus time) (r1108cge2).
- Typing: on a hitch, right after closing a screen or during a join hitch the opening `t` can land in the chat box (the
  command goes out as a chat message, `<Player> t/...`), letters can become hotkeys, or trailing characters are lost;
  `cmd` now waits longer and clears the line (r1108cge2, r1115 phase 10). A `/reload` in a client batch swallows the
  next commands typed during its hitch: install datapacks before launch (r1108cge2). xdotool `key space` is too short
  for the elytra jump: hold 0.3 s (r1115 phase 10).
- Client batches: `tp @s x y z` stays in the current dimension (use `execute in`); `fill` fails on chunks the player has
  not loaded, so arrive first; a creative player who is not flying still falls (put the camera on glass); typed summons
  are about 5 s apart, so use one `/function` for anything that must happen together (r1108cge2).
- Client speeds: read them from client position steps; server-side motion values for a real player are not the client's
  (r1115 phase 11).

## Perf and timing

- Boot-to-boot time spread is about 9 s: compare means of 3+ boots for A/B timing (`bench_boot.sh`).
- Perf numbers: per-stage ChunkGeneration means are wall time on contended threads; compare pregen wall time and
  time-to-N-chunks. A run with high idle MSPT = busy machine: discard it.
- The perf suite measures `mods/` jars only; pass `PERF_JARS="a.jar b.jar"` (tools/perf/suite/server.py) for build jars,
  and copy the jar under test aside first (another session may rebuild build/libs mid-run) (r1115 phase 11).

## Old-save fixtures

- Build an old save from the release jars (`deploy/latest/mods/`) plus the config and kubejs trees of the release commit
  (`git archive`); drop every jar not in `deploy/latest/verify/server.installed.sha1` (Biomes O' Plenty needs the
  TerraBlender stub only the new Worldgen nests; Devtools is built against the new Common).
  `tools/scratch_query/nsm_fixture.sh` does this (r1111 NSM).
