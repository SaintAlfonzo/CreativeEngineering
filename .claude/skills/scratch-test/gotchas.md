# Scratch testing gotchas

Sorted by topic. Add new ones under their topic with the date (skill step B5.4). The queue's own "Setup notes"
(config keys, defaultconfigs, no-player gaps, KubeJS renames) are in
[scratch_queue.md](../../../feature_writeup/testing/scratch_queue.md) under "World groups"; read both.

## Before the first boot

- `game_jvms.sh` empty, and again before every boot: `scratch_server.sh stop` can return while the JVM is still saving,
  and a boot then starts a second JVM that hangs in mod loading (10-25).
- Server base: libraries + run.sh from `~/.local/share/atlauncher/servers/Minecraft1192withForge` (Forge 43.3.9);
  scratch_server.sh / prep_srv.sh handle it. The old Trash CreEngServer is gone (10-19).
- Use prep_srv.sh, not bare symlinks: it HARD-LINKS jars. Symlinked jars broke when a parallel session ran
  `installToMods` during boot (ClassNotFoundException / NoClassDefFoundError in RegisterEvent, "rolling back to
  VANILLA"). Hard links are the same file as `build/libs`: a parallel gradle build rewrote one in place mid-run
  (`NoSuchFieldError: ENTITY_TYPE_REGISTRY`); when a parallel session is building, COPY the dev jars
  (`split_w234.sh` does) (10-21, 10-25, 11-01).
- Check jar times right before booting, and again before trusting a rerun: parallel sessions have installed jars
  mid-run, once a boot-crashing mixin (10-27).
- Mods for the server: every `mods/*.jar` except `client` rows in mod_sides.md; never `server_mods/` (stale).
  `vinery_upstream/` must be reachable from srv/ (the Vinery port refuses to load without it).
- `/` can be nearly full: check `df -h /`; JFR is capped per boot (`JFR_MAX`).

## Boot

- About 60 to 100 s to `Done (`. Wait on `[ -f logs/latest.log ] && grep 'Done (' logs/latest.log`; console.log still
  holds the previous boot until the JVM truncates it.
- Known third-party boot flakes, just reboot: ConcurrentModificationException in Create's
  ArmInteractionPointType.register (Crafts & Additions), SecurityCraft ConfigHandler.loadEffects ArrayList race.
- After an FML crash the JVM stays alive: kill its pid (yours only).
- "Address already in use": a previous scratch JVM still alive with cwd "srv (deleted)" after srv/ was rebuilt; match
  `readlink /proc/PID/cwd` against your scratchpad, kill -9.
- Boot-to-boot time spread is about 9 s: compare means of 3+ boots for A/B timing (`bench_boot.sh`).
- Mixins: a patcherMod mixin with a wrong target applies silently (no defaultRequire); Creative Delight / Creative
  Brewing set `defaultRequire: 1`, so the same mixin crashes them. Compartment names have `_`: match
  `Mixing [A-Za-z_.]*`.

## Driving the server

- Commands go through the stdin fifo (`scratch_server.sh cmd`); console is op, positioned at world spawn.
  `cmd`/`stop` time out after 10 s with "server not reading" when no JVM reads the fifo.
- Kill by recorded pid only. `pkill -f <text>` / `grep "[x]yz"` on ps also matches your own shell when your command
  line contains that text (exit 144). The fifo feeder pid is in `srv/feeder.pid`; never `pkill -fx 'sleep infinity'`
  (kills every session's feeder).
- `stop` hangs after "All dimensions are saved" while an Async Locator search runs: kill the pid after the save line.
  A `/locate` of a rare meta can run 30+ minutes and cannot be cancelled: restart.
- A console command that throws a `java.lang.Error` crashes the server (see memory `rhino-fieldandmethods-gotcha`).
- Config: editing the srv toml while running does NOT reload WorldgenConfig or other values: restart. `/reload`
  re-solves biome_config and scripts but not worldgen registries, and drops Chunky's commands.
- `/reload` truncates `logs/kubejs/server.log`: read probe results from `logs/latest.log`, and put `kjsexpect` rows
  before any `/reload`.
- Scratchpad wipes happen mid-run (session restarts): guard multi-step commands with `cd "$OUT" || exit 1`, keep
  scripts in `tools/` and positions or batch results in the run doc as well.

## World, ticking, entities

- Forceloaded chunks with no player get no random ticks. To force a leaf decay check, `fill` stone next to the leaves
  then air. After a restart wait about 30 s after `forceload add` before scheduling block ticks.
- An entity summoned right after `forceload add` in an unloaded dimension is not found by the next command: wait a few
  seconds.
- prep_srv servers run `spawn-animals=false`: vanilla discards every Animal / WaterAnimal on its next tick, so a
  summoned pig or squid vanishes. Use iron golems, or set the prop.
- Chunk-generation animal spawns are sparse in this pack: to test a spawn hook, a scratch-only
  `kubejs/data/zz_test/forge/biome_modifier` forcing the mob. Adding a placed feature to another biome in the same step
  = feature order cycle crash at spawn.
- Big `/forceload add` (100+ chunks) generates synchronously: `max-tick-time=-1` (set by scratch_server.sh).

## Fake players and clients

- perfBot (`/pbot spawn NAME X Y Z`, `hold`, `use`) gives rows that "need a player"; bots are invulnerable, so damage
  tests go on mobs. Details: memory `perf-suite-bot-players`.
- Forge FakePlayer from KubeJS: `FakePlayerFactory.get` + `p.gameMode.useItemOn(...)` / `destroyBlock` / `useItem`;
  menus open (check `containerMenu`). FakePlayer does not tick, so `startUsingItem` never completes: eat with
  `finishUsingItem` + `ForgeEventFactory.onItemUseFinish` (`tools/kitchen_probe/serve_items_probe.js`).
- Client: `eval_client.py` hard-links the instance `mods/`, so a jar only in build/libs is not in the client. A
  scratch world goes into the client with `sp_world.py` (allowCommands on). `qrun.py client` types chat via xdotool.

## Rhino / KubeJS probes

- NEVER load an ungenerated chunk from a KubeJS command (`getChunkAt`, `getBiome`, `getHeight` on new land): the
  server thread holds Rhino's lock while waiting for generation and a worldgen worker needs it = deadlock, kill -9.
  `forceload add` first (vanilla, no Rhino), probe, `forceload remove`. Biome without chunk loads:
  `getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(qx, 16, qz, randomState().sampler())`.
- Shared script scope: unique local names; find a level by iterating `server.getAllLevels()` (getLevel overloads are
  ambiguous); `level.dimension` is a property; a dimension string argument stops at `:`.
- Ready probes: `/zq` (recipes, tags, foods: `zq_probe.js`, `zq_batch.sh`), `tools/creative_labels_test/server_probe.js`,
  `tools/leaf_path_probe.js` (place a ConfiguredFeature). The /dumpall recipe dump has only id + type: use `/zq`.
- Tick stalls: a srv-only tick timer script + `jstack <scratch pid>` shows what the server thread waits on.

## Worldgen tests

- New level-name per run = fresh world; any worldgen-registry or biome-layout change needs one. Repeat searches in
  the same world reuse checked chunks: use `execute positioned X 70 Z run locate ...` from a fresh far origin.
- Rare structures: pregen + offline beats `/locate`: `structureDensity` x4 in the srv toml, Chunky square, then read
  starts / pieces / biomes from region files (`tools/seabed_heights.py` nbt reader; it runs main on import, copy its
  `nbt()`; patch it to read type 11 int arrays). Earth-fill `log = ALL` lines also name starts checked during other
  locates. Give pregen waits a timeout (a third-party chunk-save CME once stalled Chunky forever).
- Seed 8675309: spawn area is 76% ocean; 2144 -2592 is 92% land. structure_starts-status chunks have placeholder
  biomes (use getNoiseBiome). Jigsaw biome check point = centre of the start piece.
- `locate structure #tag` logging `NamedSet(...)[[]]` = the tag is empty at runtime.
- Checks: `[structure-tuning]`, `[structure-frequency]`, `[pool-tuning]`, `[biome-merges]` lines; `dumpstructuregen`
  then `tools/check_structure_live.py`; `/ceplace structures @ns ...` proves templates load.
- Perf numbers: per-stage ChunkGeneration means are wall time on contended threads; compare pregen wall time and
  time-to-N-chunks. A run with high idle MSPT = busy machine: discard it.

## Logs and results

- The system grep is ugrep: long `{0,300}` regexes fail ("exceeds complexity limits"); parse big logs in python.
- `qrun.py` shell steps: `grep -c` printing 0 exits 1 = a false FAIL; output is in `rows/ID.shell.txt`.
- Not ours (ignore): optional-mixin "Error loading class" WARNs, advancement parse errors, the Discord bot JDA error,
  MoreJS "Sell item cannot be empty", `PoolElementStructurePiece ... minecraft:empty is not valid` at save, 7 Create
  Mechanical Extruder recipes with an abstract `getType()` in `dumprecipes`.
- Dedicated-server dumps: recipes, tags, loot, structures, biomes, spawns, trades are good; names, stacks, effects,
  gear, food, crops, compost, paintings need the client.

## Probe crashes the server mid-batch (r1102b, 2026-11-02)

- A probe that calls `r.getType()` on every recipe crashed the server tick (`AbstractMethodError`,
  create_mechanical_extruder `ExtrudingRecipe.getType`): use `getAllRecipesFor(RECIPE_TYPE.get(rl))`. qrun.py does
  not notice a dead server: every later row FAILs on timeouts. When a batch shows a run of timeouts, check
  `srv/crash-reports/` first. A leftover crash report also makes `wait_boot.sh` report CRASH on the next boot: move
  old reports out before booting.
- A parallel `installToMods` rewrites hard-linked srv jars (the old inode stays with srv only if the install moves the
  file): copy the jars into srv (`drive.sh` decouple) for long runs.
- Background `run_in_background` shells and Monitor here do not see the game's pids (`kill -0` / `pgrep` fail): wait
  on files (results `totals:` line, drive logs) with a foreground `timeout 590 tail -f | grep -m1`.
