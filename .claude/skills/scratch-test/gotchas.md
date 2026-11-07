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
- Jars in `mods/` but not in `mod_sides.md` are another session's work in progress: a scratch srv takes them (prep_srv
  copies every non-client jar), and they can crash the boot. 2026-11-06: Immersive Portals 2.3.7 + Serene Seasons' ChunkMap
  wildcard AT crash at world creation (`MixinChunkMap_C ... cannot reduce visibility`). List the unlisted jars before
  booting (`for j in srv/mods/*.jar; do grep -qF $(basename $j) mod_sides.md || echo $j; done`) and leave them out.
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
- A boot crash on our own files blocks the dev client too (r1103 Boot 0): a `@ModifyArg` handler may take only the
  invoked method's arguments, not the enclosing method's params ("targets a method with an invalid signature"); and
  KubeJS refuses an upper-case file name anywhere under `kubejs/data/*/kubejs/` ("Invalid file name: Uppercase",
  reported as "startup script syntax errors"). Report both to the owner as found in the installed jars / pack.

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
- `level.dimension` is a ResourceLocation property in KubeJS: `l.dimension.location()` throws. Outside a JS try in a
  command handler it surfaces only as "An unexpected error occurred trying to execute that command" (no stack trace),
  and every call of that command fails (r1103 zlbs / zsb). Use `server.overworld()`. Avoid `getClass()` in probes.

- JDK private collection classes: calling a member on what `Collections.unmodifiableMap(...).entrySet()` (or any
  `Collections$Unmodifiable*`, `HashMap$KeySet`, ...) returns throws `IllegalAccessException ... cannot access a member
  of class java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet` in the pack's Rhino. Copy into public classes
  first: `new HashMap(map)`, then `new ArrayList(hm.keySet()).forEach(k => hm.get(k))` (`/zq under`, r1103w 11-03).
  A `/reload` to load a fixed probe drops Chunky's commands: fix it in tools/ and rerun its rows at the next boot.
- A `function f() {}` declared inside an `if` / `else if` block of a probe command is undefined inside the
  `forEach` closures of that block ("f is not a function, it is undefined"). Use `let f = function () {}` (`/zq under`
  `kind`, `idAt`, `top`, r1103w). A new probe command gets one live call before its batch relies on it.

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
  then `tools/check_structure_live.py`; `/cdevplace structures @ns ...` proves templates load.
- Perf numbers: per-stage ChunkGeneration means are wall time on contended threads; compare pregen wall time and
  time-to-N-chunks. A run with high idle MSPT = busy machine: discard it.

- Earth fill judges (`efb_judge.py --except`): a bare `mine` in the regex matches every `minecraft:` id and hides all
  vanilla structures; write `:mines?\b|_mine\b` (r1103w). `[zq] under` lines carry no dimension: keep Nether and End
  probes out of an Overworld judge's log window. `/place structure` stores no start and runs no earth fill, so a
  placed structure is invisible to `/zq under`; earth fill rows need natural starts.

## Logs and results

- The system grep is ugrep: long `{0,300}` regexes fail ("exceeds complexity limits"); parse big logs in python.
- `qrun.py` shell steps: `grep -c` printing 0 exits 1 = a false FAIL; output is in `rows/ID.shell.txt`.
- Not ours (ignore): optional-mixin "Error loading class" WARNs, advancement parse errors, the Discord bot JDA error,
  MoreJS "Sell item cannot be empty", `PoolElementStructurePiece ... minecraft:empty is not valid` at save, 7 Create
  Mechanical Extruder recipes with an abstract `getType()` in `dumprecipes`.
- Dedicated-server dumps: recipes, tags, loot, structures, biomes, spawns, trades are good; names, stacks, effects,
  gear, food, crops, compost, paintings need the client.
- `qrun.py --from ID` skips the setup rows before ID, so their captures (`{SBH}`, markers) are missing: every command
  using them fails with "Expected integer" (r1103 boot 4 retry). Use `--rows SETUP,ID,...` instead.
- A driver that deletes a crashed srv dir must copy `logs/kubejs/startup.log`, `logs/latest.log` and `crash-reports/`
  out first (r1103 lost the SQ-ENM-03 startup log and needed a repro boot).
- Reruns of rows written for an older run's world (markers like `s1home`, `rhome` made by earlier boots) need their
  marker setup rows on a fresh world; summon a marker only after its chunk is forceloaded (r1103 phase 2: 13 faults).

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

## Found in r1106cbo (2026-11-06)

- KubeJS's class filter refuses `net.minecraftforge.fml.ModList` in a probe ("Class is not allowed by class filter"):
  the whole script fails to load and its command never registers. Use `Platform.isLoaded(modid)`.
- A probe copied into OUT at setup stays the old copy on a restart of the same OUT (`cbo_run.sh` REUSE): copy it in again
  with PRE after editing it.
- `ServerEvents.loaded` lines print once per boot, not on /reload; check them with `bootexpect`, not after a reload.
- KubeJS 1902.6.2-build.73 never posts `ServerEvents.afterRecipes`: a listener there never runs (no error either).
- `shell grep -c ...` exits 1 when the count is 0, so a qrun shell step that only prints a count FAILs on 0: add `; true`
  or test the number. qrun `kjsexpect` reads `server.log`; startup-script lines (`console.log` in startup scripts) are in
  `startup.log`: use a `shell grep` on that file.
- A server that refuses to boot (missing donor folder, missing dependency) writes its crash report and then hangs; 
  `stop_wait.sh` kills its own pid after 180 s. `wait_boot.sh` returns CRASH at once, so no time is lost waiting for Done.
- `/cbo effects` counts ITEM namespaces of the Armor / Tool Effects rows; a mod that only contributes effect ids never
  appears there.
- `[Cc]ycle` as a bootreject matched Forge's "recycleShowTicks" config line: anchor words with `\b`.

## Found in r1106cbb (2026-11-06)

- **Never `cp` onto a name that already exists in `srv/mods/`**: those files are hard links to the instance's `mods/`, so
  the copy writes THROUGH into the pack's jar. prep_srv's swap did that (stale SWAPS row, `cp -l` failed on the existing
  name, plain `cp` fallback) and overwrote the pack's `mods/creativecommon-1.1.0.jar`. prep_srv and port_chain now `rm -f`
  the target name first. Same family: a Gradle build rewrites a build/libs jar in place, so a scratch server hard-linked
  to it sees the new jar mid-run (stop the server before rebuilding, or accept that its rows are done).
- prep_srv's SWAPS table has to follow jar renames: after the 2026-11-04 Common swap the `creativeengineering-*` glob is
  the Engineering jar. Rows now pick the newest build jar per project (`newest()`); read the `swap:` lines before booting.
- Rhino, two script packs: each has its own `Object.prototype`, so `Object.getPrototypeOf(v) === Object.prototype` in a
  startup helper is false for every object a server script builds. Use `Object.prototype.toString.call(v)`. And a
  loop-body `const` keeps its first value (memory `rhino-fieldandmethods-gotcha`): `tools/rhino_const_loop_check.py`
  lists them. `tools/rhino_scope_test/run.sh` runs a snippet in the pack's Rhino with two scopes.
- Chain/instance boots: an empty chain can hide behind a bug that "works" in the full pack (first entry present). Compare
  counts with the full pack (`Found N tags, added X objects`, `removed N recipes`) rather than only checking for errors.
- A data pack that only fails on worldgen registries ("Unbound values in registry worldgen/biome") stops the boot after
  the KubeJS server scripts ran, so their log lines (structure gate, tuning) can still be judged from `latest.log`.

## Found in r1106cba (2026-11-06)

- A level with no player and no forced chunk stops ticking entities 300 ticks after start: effects on summoned mobs never
  apply (golems stayed at 100.0). `forceload add` the chunk first.
- KubeJS removed-recipe logging: `srv/local/kubejs/dev.properties` with `logRemovedRecipes=true` (not
  `kubejs/config/`); read at startup only, `/reload` does not pick it up. To find which call removed what, add
  `tools/scratch_query/nch_remove_diag.js` to `srv/kubejs/startup_scripts/` (logs every `global.NCH.remove` filter to
  latest.log, skips filters with an undefined value). A filter like `{output: undefined}` is match-all in KubeJS.
- After an FML missing-dependency or donor-jar stop the JVM stays up: kill your pid (cwd under the scratchpad).

## qrun batches and probes (r1106cbp, 2026-11-06)

- qrun refuses a batch with any directive (`bootreject`, `bootexpect`) before the first `## ID` row ("directive before
  the first row"); put boot-wide checks inside the first row.
- `kjsexpect` reads `logs/kubejs/server.log` only; startup prints (`[NCH] removed`, `transition items`, `[compost]`)
  are in `startup.log`: check them with `shell grep ... logs/kubejs/startup.log`.
- `port_chain.sh` boots without Creative Delight must also DROP `kubejsdelight-*.jar` (Farmer's Delight comes nested in
  the CD jar). `/zq` loads the Calendar class lazily now; any new probe class import from an optional Creative jar must
  be lazy too, or the probe fails to register in boots without that jar.

## Probes and batches (r1106spfx)

- `grep -c` exits 1 when the count is 0, so a `shell` step that ends on it FAILs on the pass case: end with `; true`
  or test the number (`test "$(grep -c ...)" = 0`).
- `zq count` takes an optional 8th argument, the dimension (`... <block> minecraft:the_end`); without it the overworld.
- The full pack cannot boot without Creative Delight (Farmer's Delight is nested in it), so a jar-out row for CD,
  Calendar or Compat belongs on the port chain, not the full pack.
- A mixin config plugin's `preApply` runs after Mixin conforms visibility (`MixinPreProcessorStandard.attach`): it
  cannot rescue another mod's `@Overwrite` from an access-transformer clash.

## Found in r1107tp (Tempad, 2026-11-07)

- perfBot relogs a bot that dies (`/kill`: remove + spawn, no `PlayerEvent.Clone`). Never call
  `PlayerList.respawn` on a bot as well: perfBot keeps ticking the stale object and the server crashes
  (`DistanceManager` NPE in `Bots.onServerTick`). Clone-path checks need a client death.
- Bots never close a screen: after a block or item use opened a menu, `containerMenu` stays that menu, so inventory
  clicks go nowhere. Click `p.inventoryMenu` directly (`/ztp click P SLOT BUTTON inv`); `closeContainer` is not
  callable from Rhino on a BotPlayer.
- Shift-use needs `p.setShiftKeyDown(true)` (`/ztp sneak P on`); `pbot use` is a plain use.
- A `/give` to a bot fills slot 0 = the main hand, and `item replace ... weapon.mainhand` overwrites slot 0: keep
  the item under test (a Tempad) in another hotbar slot.
- Block drops here scatter more than 8 blocks from a survival break: collect with a radius of about 24.
- Player-only command trees (FTB Teams `party create / invite / join`) do not parse for `execute as <bot>`
  ("Incorrect argument"): call the mod's API from a probe (`/ztp ftbparty`).
- Rhino: `ChrononHandler.of(blockEntity)` is ambiguous between overloads; pick one with
  `$Cls['of(net.minecraft.world.level.block.entity.BlockEntity)'](be)`. `ItemStack#getTag` on a stack from
  `getMainHandItem()` or `ItemEntity#getItem` throws "Cannot find function getTag": save the stack to a CompoundTag.
- Moving Curios out of a full-pack srv takes its hard dependents (Botania and its addons, Charm of Undying, Corpse
  Curios Compat, Elytra Slot, Shulker Box Slot, Simple Hats) plus Wardrobe and Portable Wardrobe, which use Curios
  without declaring it (crash `NoClassDefFoundError ... ICurioItem`); FTB Teams takes FTB Chunks, FTB Quests, Quests
  Additions, FTBQuestsOptimizer. Compute the closure from mods.toml (`mandatory = true`) before booting.
- `wait_boot.sh` reports CRASH for any old file in `srv/crash-reports/`: move old reports out before the next boot.
- Translated donor data can vanish silently: a port that converts upstream data at load may drop whole files (debug.log
  only). `/loot spawn ~ ~ ~ loot <ns>:blocks/<block>` = "Dropped 0 items" is the quick check.
- `pbot hold P ITEM` puts a FRESH stack into slot 0 (replacing what was there); it does not select an inventory stack.
  Charge or tag the held item after the hold (`/ztp setchr P 0 N`), or set the hand with `item replace entity P
  weapon.mainhand with ITEM{nbt}` to re-place a dropped block with its exact NBT (r1107tp2).
- 1.19.2 has no `contents` slot for item entities (`item replace ... from entity @e[type=item] contents` = "Unknown
  slot") and no `execute if loaded` (r1107tp2).
