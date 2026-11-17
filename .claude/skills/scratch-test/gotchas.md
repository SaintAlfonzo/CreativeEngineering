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
  Every `*_upstream/` donor folder in the instance root (`ls -d *_upstream`) must be linked into srv/ BEFORE the boot;
  ports that refuse to load without theirs (Vinery, Tempad) fail mod construction. A bundle loads every nested
  member, so a donor is needed for any jar that nests its port, not only for the port's own jar. Until 2026-11-07
  Tempad was nested in Compat and a Common + Compat boot failed this way; Tempad is its own jar since then.
- Jars in `mods/` but not in `mod_sides.md` are another session's work in progress: a scratch srv takes them (prep_srv
  copies every non-client jar), and they can crash the boot. 2026-11-06: Immersive Portals 2.3.7 + Serene Seasons' ChunkMap
  wildcard AT crash at world creation (`MixinChunkMap_C ... cannot reduce visibility`). List the unlisted jars before
  booting (`for j in srv/mods/*.jar; do grep -qF $(basename $j) mod_sides.md || echo $j; done`) and leave them out.
- `/` can be nearly full: check `df -h /`; JFR is capped per boot (`JFR_MAX`).
- 2026-11-13: the Power Grid -17 jar sat in the instance `mods/` with no mod_sides.md row; prep_srv.sh now drops any
  `backportedPowerGrid-*.jar` unless PGJAR names one.
- Waiting on qrun: wait on its pid (`kill -0`), not `pgrep -f "qrun.py run"` inside `bash -c` (the wait's own command
  line matches, so it never ends).

## Boot

- Never pipe `scratch_server.sh boot` into `tail`/`head`: the server child keeps the pipe open and the command hangs
  until the JVM exits (r1111, twice). Boot with `> OUT/boot.out 2>&1 < /dev/null &`, then `wait_boot.sh` under
  `timeout 590`.
- About 60 to 100 s to `Done (`. Wait on `[ -f logs/latest.log ] && grep 'Done (' logs/latest.log`; console.log still
  holds the previous boot until the JVM truncates it.
- Known third-party boot flakes, just reboot: ConcurrentModificationException in Create's
  ArmInteractionPointType.register (Crafts & Additions), SecurityCraft ConfigHandler.loadEffects ArrayList race.
- After an FML crash the JVM stays alive: kill its pid (yours only).
- A new own jar's `versionRange` on a third-party mod must use that jar's reported version, often
  `<mc>-<ver>` (`${file.jarVersion}` from the manifest): Raspberry Pie's `[2.2.0]` against Apple Pie's
  `1.19.2-2.2.0` failed mod loading (r1112). Read the dependency's `Implementation-Version` before trusting a range.
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

- A fresh world can start in rain: rain-fed blocks (Hearth and Harvest troughs) fill with water and then refuse items.
  `weather clear` does not stop it at once (the rain level fades over several seconds): wait about 20 s, then place
  (r1112, SQ-HH-223, -233).
- A bot within about 2 blocks picks up item entities and XP orbs, creative mode included: stand it 6+ blocks off when a
  row counts items on the ground or orbs (r1112, SQ-HH-461, -1529).

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

- Forceloaded chunks with no player get no random ticks (`/cdevtest randomtick BOX N` gives them without one). To force a leaf decay check, `fill` stone next to the leaves
  then air. After a restart wait about 30 s after `forceload add` before scheduling block ticks.
- An entity summoned right after `forceload add` in an unloaded dimension is not found by the next command: wait a few
  seconds.
- prep_srv servers run `spawn-animals=false` (peaceful; set `spawn-npcs` too): ServerLevel DISCARDS every `Animal`,
  `WaterAnimal` and `Npc` on its next tick, summoned and NoAI ones too (pigs, cows, wolves, striders, villagers gone a
  tick later: "No entity was found"). Rows with animals or villagers need `prep_srv.sh OUT LEVEL A spawn-animals=true
  spawn-npcs=true` (gamerule doMobSpawning false still stops natural spawns); iron golems survive either way (r1114).
- Chunk-generation animal spawns are sparse in this pack: to test a spawn hook, a scratch-only
  `kubejs/data/zz_test/forge/biome_modifier` forcing the mob. Adding a placed feature to another biome in the same step
  = feature order cycle crash at spawn.
- Big `/forceload add` (100+ chunks) generates synchronously: `max-tick-time=-1` (set by scratch_server.sh).
- A restart with no bot or player online gives forceloaded chunks no random ticks: crop/hops rows after a restart need a
  bot beside the site (r1113 rerun_a2). A Create mixer or arm turns only from a meshing cogwheel, not a gearbox under it;
  an undrained millstone deletes outputs once its slots fill (r1113 pwg17, c4_ar).

## Fake players and clients

- perfBot (`/pbot spawn NAME X Y Z`, `hold`, `use`) gives rows that "need a player". Bots are invulnerable unless
  `pbot vulnerable NAME on` (kept across a respawn). Since 2026-11-15 the jar has test verbs (attack, interact, punch,
  break, useitem / eat with a real ticked use, hunger, sneak, look / lookat, moveto with physics, inv get / set /
  select, pickup on / off): prefer them over a KubeJS player probe (creative_devtools.md). Details: memory
  `perf-suite-bot-players`.
- `pbot eat` needs food below 20 (`pbot hunger NAME 10`) unless the food is always edible; its result line comes when
  the use finishes (`verb=pbot.useitem ... end=finish`, 32 ticks for bread), not with the command reply.
- `/cdevtest` without `as=NAME` acts as a Forge fake player of that dimension: no ticking (no use duration), inventory
  cleared every call. Use a bot (`as=NAME`) when the row needs a real player's state.
- Forge FakePlayer from KubeJS: `FakePlayerFactory.get` + `p.gameMode.useItemOn(...)` / `destroyBlock` / `useItem`;
  menus open (check `containerMenu`). FakePlayer does not tick, so `startUsingItem` never completes: eat with
  `finishUsingItem` + `ForgeEventFactory.onItemUseFinish` (`tools/kitchen_probe/serve_items_probe.js`).
- Loading Protection (`protectionDuration = 150` in the pack) cancels every `LivingAttackEvent` whose attacker is a
  player in the first 150 s after that player logs in, bots included: a row that spawns a bot and hits with it does
  nothing ("hit result false"). Set `qrun.py toml OUT/srv/config/loadingprotection-common.toml <section>
  protectionDuration 1` before the boot, or hit with a non-player source (r1111 SQ-HH-424, -425, -453). Since 2026-11-15
  perfBot bots are exempt one tick after `pbot spawn` (login spot set one block above the target, so the spawn
  teleport counts as a move; SQ-TX-12 checks it); real players are not.
- Rhino picks `Entity.distanceToSqr(Vec3)` (`m_20238_`) for a 3-number call and throws: compute the distance from
  `getX/getY/getZ` (r1111 `hh_crow_probe.js`). A goal's `String(goal)` is `Class@hash`; use
  `getClass().getSimpleName()`. A probe that samples running goals misses a flee that ends during a `sleep` before
  the watch: start the watch right after the summon.
- Client: `eval_client.py` hard-links the instance `mods/`, so a jar only in build/libs is not in the client. A
  scratch world goes into the client with `sp_world.py` (allowCommands on). `qrun.py client` types chat via xdotool.

## Rhino / KubeJS probes

- NEVER load an ungenerated chunk from a KubeJS command (`getChunkAt`, `getBiome`, `getHeight` on new land): the
  server thread holds Rhino's lock while waiting for generation and a worldgen worker needs it = deadlock, kill -9.
  `forceload add` first (vanilla, no Rhino), probe, `forceload remove`. Biome without chunk loads:
  `getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(qx, 16, qz, randomState().sampler())`.
- Shared script scope: unique local names; find a level by iterating `server.getAllLevels()` (getLevel overloads are
  ambiguous); `level.dimension` is a property; a dimension string argument stops at `:`.
- Ready probes: `/cdevtest` first (Java, cannot crash the server or deadlock; creative_devtools.md), then `/zq` (recipes, tags, foods: `zq_probe.js`, `zq_batch.sh`), `tools/creative_labels_test/server_probe.js`,
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
- Never call `r.getType()` while looping `getRecipeManager().getRecipes()`: Mechanical Extruder's `ExtrudingRecipe.getType()`
  throws AbstractMethodError, a Java Error that Rhino's try/catch does not stop, and the server crashes (r1113 c4_a). Use
  `ForgeRegistries.RECIPE_SERIALIZERS.getKey(r.getSerializer())`. A `/cmd` argument is a Java String: `String(arg)` before
  a JS `split(/re/)`, or the regex never splits (r1113 zc4).

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
- `qrun.py` shell steps: `grep -c` printing 0 exits 1 = a false FAIL (seen again in r1106spfx, r1113, r1114); end the
  step with `; true`, wrap it (`echo "n $(grep -c ...)"`) or test the number (`test "$(grep -c ...)" = 0`). Output is in
  `rows/ID.shell.txt`.
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
- qrun `kjsexpect` reads `server.log`; startup-script lines (`console.log` in startup scripts) are in
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

## Found in r1107cc (Creative Common, 2026-11-07)

- **Never pass a raw FluidStack from a probe into a Java method.** KubeJS converts every FluidStack argument and a raw
  one does not survive: `fl.isFluidStackIdentical(fl)` is false, and IE's `FluidTagInput.testIgnoringAmount` /
  `BottlingMachineRecipe.findRecipe` / `RefineryRecipe.findRecipe` reject everything (made every IE fluid recipe look
  broken). Pass `Fluid.of(id, amount, nbtString)` instead (`cc_seqasm_probe.js` `zsaFluid`). `isFluidEqual(fs)` is
  "ambiguous" (ItemStack overload) for the same reason.
- Rhino overloads: `JsonObject.addProperty('k', 400)` and `addProperty('k', Integer.valueOf(400))` both write the
  STRING "400.0". Use `json.add('k', JsonParser.parseString('400'))`. `java.lang.Integer` as a bare global throws an
  uncatchable error inside a command handler ("An unexpected error occurred"); use `Java.loadClass`.
- Mojmap names Rhino does not see here: `Ingredient.getItems` (use `getStacks().toList()`), `ItemStack.getTag` (use
  `getNbt()`); KubeJS `stack.enchant(...)` returns a COPY. Create's `SequencedAssemblyRecipe.advance` is private: walk
  an assembly with the public `SequencedAssemblyRecipe.getRecipe(level, stack, type, ProcessingRecipe)` and the step's
  `rollResults()` (`/zsa <id>`).
- Phantoms (SQ-PSU-04, SQ-CC-10b): the group A superflat reports sea level 63 and the phantom spawner skips every
  player below sea level, so bots at y -60 never get one (r1102b's and r1107cc's first two tries). Put the bots on
  platforms at y 70, with `spawn-monsters=true`. `/zph N` (`cc_phantom_probe.js`) runs N fresh spawner passes at once.

## Found in r1107cco (Common-only sets, 2026-11-07)

- `cc_only_srv.sh` must keep the dev perfBot jar (bots) and drop the pack's `kubejs/data/`: that folder holds Terralith
  noise overrides and crashes world creation without Terralith ("unbound value ... density_function").
- NoAI mobs do not move at all in 1.19 (no fall): use a mob with AI (Silent) for fall or gravity checks.
- `@e[...]` with no x/y/z/distance searches every dimension, even under `execute in`: pin cross-dimension selectors.
- Platforms in unloaded chunks are not placed: `forceload add` before `fill`, or bots fall into caves (phantoms need sky).
- A bot keeps its effects across `pbot remove-all` / respawn (player data persists): `effect clear` before a control pass.
- Mixin's "Static binding violation ... cannot reduce visibiliy" (its typo) is a WARN, not the IP crash: a regex for
  "cannot reduce visibility" must exclude "Static binding violation" lines.
- Create Gravity's low-gravity modifier is added only while an entity falls; on the Common-only set it was never added
  at all (TODO `creativecommon/publish #3`).
- `qrun.py` with a mid-pattern `(?i)` warns on Python 3.10 and applies the flag to the whole regex; put it first.

## Found in r1108cge (Creative Common gravity, 2026-11-08)

- Create Gravity 1.2.0 on a dedicated server was inert until a `/reload` (its biome list is built only on
  TagsUpdatedEvent after its server config loads). Common 11-08 00:49 builds it at server start; with an older Common, a
  gravity row needs a `/reload` first, and a manual `/reload` spoils any "as booted" check of that boot.
- `world/serverconfig/*.toml`: edit only while the server is stopped. Forge writes the in-memory config back on
  shutdown (an edit made while up is overwritten), and a live edit was not picked up within 6 s.
- 1.19.2 has no `execute if biome` (1.19.3+): read the biome with a probe (`/zgv cfg`, `/zq`).
- End main island: y 70 at 0 0 is inside the terrain; NoAI mobs summoned there suffocate within about 10 s (use y 75).
  `cc_only_srv.sh` and `cpp_chain_srv.sh` servers are peaceful unless `difficulty=normal` is passed: hostile mobs vanish.
- `qrun.py --rows` skips setup lines (forceload, bot spawn) that live in an earlier row: put each row's setup in the row.
- Rhino: `const` inside a function that has a `try` threw "redeclaration of var server" in a command handler; use `var`.
  `level.dimension()` is ambiguous (use KubeJS `getDimension()`); KubeJS `entity.getType()` returns the id string.
- Create Gravity's low-gravity modifier is transient: `data get entity ... Attributes` never shows it; read the value
  with `attribute <target> forge:entity_gravity get` (0.016 = factor -0.8).

## Found in r1108cge2 (Creative Common low gravity, 2026-11-08)

- `set -o pipefail` with `| grep -q`: grep exits at the first match, the writer dies on SIGPIPE, the pipeline reads as "not found". Capture the output first.
- `qrun.py client`: on a hitch the opening `t` also lands in the chat box and the command goes out as a chat message (`<Player> t/...`), or trailing characters are lost. `cmd` now waits longer and clears the line. A `/reload` in a client batch swallows the next commands typed during its hitch: install datapacks before launch.
- Client batches: `tp @s x y z` stays in the current dimension (use `execute in`); `fill` fails on chunks the player has not loaded, so arrive first; a creative player who is not flying still falls (put the camera on glass); typed summons are about 5 s apart, so use one `/function` for anything that must happen together.
- This client log has no `Resource reload finished` line: do not wait on it to open a world (open by hand or wait on `Sound engine started` plus time).
- A row that restarts a world reloads the bot's saved inventory (SQ-CGE-10 kept the backtank from -10b): clear the slot at the start of the row.
- A wide entity at the edge of a cleared column wedges in the terrain (a boat is 1.375 wide, drop 0.000): clear a margin.
- `stop_wait.sh` can return a few seconds before the JVM is gone: wait for `game_jvms.sh` to be empty before the next boot.
- Rows that need a client AND a dedicated server up together cannot be run by an agent (one game JVM at a time, no exceptions): give them to the owner as test IDs.

## Found in r1108hh (Hearth and Harvest, 2026-11-09)

- Never run Gradle while a scratch JVM is up or between its boots: srv jars hard-linked to `build/libs` get rewritten in place, and the `jar` step leaves an un-reobfuscated file behind (`NoSuchMethodError` on a Mojang name, or `ClassNotFoundException` mid-load).
- `srv/kubejs/data` is a copy made at prep and outranks the jar: a script fix needs that copy updated or a new prep.
- A `stop` sent to a hung JVM stays in `in.fifo` and stops the next boot right after `Done`: kill the feeder and remove the fifo after a hang.
- `latest.log` rolls over at midnight; the qrun row running then gets an empty segment.
- Bots vacuum up items within about 2 blocks: read the bot's `Inventory` as well as `drops`. Bots also get the starter kit a few seconds after spawn, off-hand item included: `clear` after a wait.
- Rhino: `String(args).trim().split(/\s+/)` (a Java string with a JS regex does not split). Level `getGameTime` is not reachable, use `getLevelData().getGameTime()`. A Forge `FluidStack` built in Rhino can reach `fill()` as a non-matching stack (accepted 0): use `Fluid.of(id, n).getFluidStack()`.
- NBT dumps do not sort keys (`{id: ..., Count: 1b}`), and floats print in full (0.3 is 0.30000001192092896): write row regexes for both.
- Create fluid tanks ignore bucket use from a bot, vanilla water included.

## Old-save fixtures (r1111 NSM, 2026-11-11)

- Build an old save from the release jars (`deploy/latest/mods/`) plus the config and kubejs trees of the release
  commit (`git archive`); drop every jar not in `deploy/latest/verify/server.installed.sha1` (Biomes O' Plenty needs
  the TerraBlender stub only the new Worldgen nests; Devtools is built against the new Common).
  `tools/scratch_query/nsm_fixture.sh` does this.
- perfBot cannot sneak: `pbot use` on a chest opens it. Place items onto interactive blocks by calling the item's
  placement code from KubeJS, or write the block entity NBT.
- Alex's Mobs crows pick up ground items and fly off with them; with `spawn-animals=true` a "missing" dropped item may
  be in a crow's HandItems (find it with `region_ids.py` on `entities/`).
- `pbot spawn` loads the saved player (dimension, inventory, respawn point) and then teleports to the given position;
  read the login dimension with a `PlayerEvents.loggedIn` probe (`nsm_probe.js`). A killed bot respawns by logging in
  again, so a bed-respawn walk cannot be judged with a bot.
- Supplementaries jars refuse Forge water through the fluid handler; write `FluidHolder:{Fluid:"minecraft:water",Count:4}`.


## Found in r1113 (2026-11-13)

- Rhino: `const` declared inside a `try` block of a nested function throws `InternalError: TypeError: redeclaration
  of var X` at call time (pg_solver_probe.js, pwg_probe.js). Use `let` inside functions.
- `net.minecraftforge.fml.ModList` is blocked by the KubeJS class filter; use `Platform.getMods()` (map id -> mod
  info with `getVersion()`).
- `bootreject` matches the whole log, including the replies to the batch's own commands (a `give` of an unknown item
  prints `Unknown item`): exclude the console reply with a lookahead, `^(?!.*MinecraftServer/\]: Unknown item 'x').*`.
- `scratch_server.sh boot OUT | tail` never returns (the JVM keeps the pipe open): redirect boot output to a file,
  `boot OUT > OUT/boot.txt 2>&1 < /dev/null`, then `wait_boot.sh`.
- Block data in `data get block` prints `Slot: 7` for a byte in some containers, `Slot: 7b` in others: write `Slot: 7b?`.
- A member config carry (SALT2-08, MemberConfigMove) skips keys the target file already has: the scratch srv copies
  the instance's filled `creativedelight-common.toml`, so strip the target sections before a carry test.
- `ForgeRegistries.ENTITY_TYPES.getKey(e.getType())` (and `Registry.ENTITY_TYPE.getKey`, r1114 `zc3 portals` read 0)
  returned `minecraft:pig` for every entity from Rhino (r1113 pwg10b):
  use `e.getEncodeId()`. A contraption scan built on it reported "contraptions 0" for a working bearing.
- A Forge `FluidStack` built in a script arrives EMPTY in Java (KubeJS wraps it): fill tanks by writing their NBT
  (`TankContent`, `InputTanks`, `Tanks`) instead. Static getters like `ModdedFluids.acid()` are often unreachable: look
  fluids and items up by registry id.
- KubeJS class filter blocks `io.netty.*` (no `Unpooled` buffers: packet round trips cannot run in a probe) and
  `ModList` (use `Platform.isLoaded` / `Platform.getMods`), and `getClass()` (use `instanceof`).
- Ambiguous overloads throw `The choice of Java method ... is ambiguous`: `Entity.setDeltaMovement(3 numbers)` (use the
  8-argument ItemEntity constructor), `moveTo(5 numbers)` resolves to `teleportTo` (use `absMoveTo`), `setYRot` is not
  found on a FakePlayer, `BlockPos.relative(dir, n)` and `Direction.getStepX` (step by axis by hand),
  `ItemStack.is(TagKey)` (use `ForgeRegistries.ITEMS.tags().getTag(key).contains(item)`), `new ItemStack(item)` (use
  `item.getDefaultInstance()`), `AllDisplayBehaviours.sourcesOf` (call by signature `obj['m(Type)'](x)`).
  Also unreachable by name: `level.getGameTime` (use `getLevelData().getGameTime()`), `getStringUUID`,
  `DamageSource.getMsgId` (read the field).
- Create 0.5.1's value box input handler skips `testHit` for a FakePlayer and replays the click as `BlockState.use` at
  the block centre: a probe clicking a value-box block (modular display slots) must call `BlockState.use` itself.
- Goggle tooltips (`addToGoggleTooltip`) on gauges call `Minecraft.getInstance()`: on a dedicated server that is a
  RuntimeDistCleaner ERROR and the probe command aborts. Wrap in try/catch; never use a blanket `/ERROR\]` reject
  in batches that read gauges (`reject ^(?!.*RuntimeDistCleaner).*(Exception|/ERROR\]).*(mod)`).
- Peaceful difficulty removes summoned hostiles (witch, husk); MCA replaces summoned villagers, so `kill
  @e[type=minecraft:villager]` misses them (kill by box) and they regenerate (compare health as deltas).
- Create machines placed before their creative motor often stay at speed 0: place the motor (and shaft or gearbox)
  first, then the machine, and read `speed` before relying on it. Creative motor default is 16 RPM.
- `pkill -f <pattern>` can match your own shell when the pattern is in the command line: kill qrun by pid from
  `/proc/PID/cmdline`.

## Found in r1114 (SQ-TPF, 2026-11-14)

- `@e` (and `@e[tag=...]`) never matches a dead entity (the selector adds `Entity::isAlive`): select a dying mob by
  UUID string (`data get entity 00014b89-0000-0001-0000-000200000003 DeathTime` for `UUID:[I;84873,1,2,3]`).
- MCA (`overwriteOriginalZombieVillagers` true) replaces a summoned `minecraft:zombie_villager` on join with an MCA one
  that has lost its vehicle and `ConversionTime` (-1). Summon `mca:male_zombie_villager` directly; it converts to
  `mca:male_villager` (match `type=#zq:villagers`). Tags do not survive the conversion.
- Third-party fix rows: `tools/scratch_query/tpf_srv.sh` builds the srv and writes the Packet Fixer and Debugify
  configs (header there).
- Eat rows on the scratch srv (peaceful): Peaceful refills 1 food per 10 ticks while the player is hungry, so a
  `hunger 0` then an eat 2 s later reads +4 over the item's nutrition (r1113 c2a1 "every eat +4"). Set `gamerule
  naturalRegeneration false` in the setup row of any row that reads foodLevel deltas.
- Live Forge config edits do not work on the scratch srv at all: an in-place rewrite (`sed > tmp; cat tmp > file`) is
  not seen either (no "changed" line in debug.log, the file is in the "Watching TOML" list; r1114 fix_c3a1 FCK-13,
  IP-05; supersedes the r1113 note that an in-place rewrite works). Set every value with `qrun.py toml` before a boot
  and split value cases across boots.
- `level.dimension()` on a level from
  `server.getAllLevels()` throws "not a function" in Rhino: read it in a try and fall back to `level.dimension`.
- A gun portal heartbeat is phased per portal and the tick count restarts at load, so a long `heartbeatSeconds` beat can
  fire seconds after Done: rows that read saved portals after a restart need a beat longer than the read (600).
- CLIENT: 420 or more jars in the client's mods/ kill it natively at window creation (`*** stack smashing detected ***:
  terminated` in launch.out, about 23 s after launch, right after KubeJS "Loaded client.properties"); 419 start. Any
  jar tips it (Packet Fixer alone, Debugify alone, both swapped against Clear Void tested r1114). Count
  `ls GAME/mods/*.jar | wc -l` before a client launch; the instance mods/ had 421 on 2026-11-14.
- `eval_client.py setup` always copies `zz_perfeval_drive.js` into the client's server_scripts: it sets spectator and
  teleports the player every 30 s for 3 min after the join. Delete it before any gate or queue client batch.
- A throwaway client needs `config/debugify.json` (tpf_debugify_cfg.py): the instance has none, so Debugify would
  run all 69 fixes, including MC-121706 that crashes the integrated server boot.
- A `waitlog` for a `/locate` reply can match an EARLIER locate's late reply: a locate that outlived its batch (rerun_b's
  third corn maze search) answered inside c4_b's savanna locate wait, so the capture ran on the wrong line. Make every
  locate waitlog name its own target (`The nearest <id> is at|Could not find a structure of type "<id>"`).
- Creative Worldgen ends every structure search after `[worldgen.locate] searchTimeoutSeconds` (120 s shipped) whatever
  the batch's waitlog says: a row that needs a longer search sets that key in the srv toml before the boot (corn maze
  from the c4b spawn took 424 s).
- `creativeworldgen:meta/*` are structure ids and `creativeworldgen:group/*` structure sets, not structure tags:
  `/locate structure creativeworldgen:meta/waystone_ruins` (no `#`); a group is checked through a member or
  `/cwglocate census <radius>` (the radius is required).
- `scratch_server.sh setup` copies the Creative Labels probe (`zz_cl_probe.js`); in a reduced set without Labels it is
  a KubeJS load error that trips `kjsreject error`. cpp_chain_srv.sh removes it since r1114; other reduced-set builders
  (cc_only_srv.sh deletes every script but zq) are fine.


## Found in r1114 (P11 Power Grid, phase 5)

- A regression batch that copies rows from many gate batches keeps their hidden dependencies: a "boot row" the plan
  drops can be the one that builds a fixture (SQ-PWG-76 built the chain source board for -78 to -82). Grep the later
  rows for coordinates no remaining row places before dropping a row.
- A big up-front `forceload add` over every test area (SQ-PWG-P11A) breaks rows that unload a chunk on purpose
  (SQ-PWG-21 "chunk 65 loaded=false"): rows that test unloading need an area outside the blanket forceload.
- Moving a row to new coordinates with a regex shift: also shift the coordinates inside the expect regexes
  (`basin at 114[26] -60 1890` kept the old X and failed a passing row).
- `zpwg ns` printed only the first 20 ids per registry; `zpwg ns <ns> all` prints every id (pwg_probe.js).

## Found in r1115 (SQ-MELT, 2026-11-15)

- Serene Seasons melts open-sky ice and snow at any randomTickSpeed (seasonal surface melt), which confounds every melt
  row: no-source controls melt too. Put a glass block right above every ice or snow target (Serene Seasons skips
  covered blocks) and keep one roofed no-source control per batch. Generators: `tools/scratch_query/melt_gen_p3r.py`.
- Do not wrap `qrun.py run` in `timeout 590`: it kills the batch mid-row. Run qrun detached and wait on its pid with
  capped waits.
- Intermittent boot failure "Failed to load datapacks" with an NPE `frame is null` in Rhino
  `Interpreter.getSourcePositionFromStack` from KubeJS `TagLoaderKJS.customTags` console.info on a worker thread: a
  parallel tag-load race, not our jars. The JVM sits idle; stop it and reboot (r1115 phase 4, clean on reboot).
- scratch_server.sh (fixed r1115): a `stop` sent to a hung boot could stay in the fifo and stop the next boot 2 s after
  Done; boot now kills the old feeder and makes a fresh fifo, and stop's kill line no longer aborts under `set -e`.
- Forceload boxes: an unaligned 256-block box spans 17 chunks per axis and is refused ("Too many chunks 289"); use
  240-block boxes.
- Live toml edits are not picked up on scratch servers: the Forge config watcher thread is parked in ModernFix's
  NightConfigWatchThrottler (same family as SQ-WO-34). Restart to apply config values. Creative Compat's toml: since
  2026-11-15 `/cdevreload compat` (server, op 3) and `/cdevreloadclient compat` (client) re-read it at once (the watcher
  also misses saves by rename, `sed -i`: plan_post_scratch_1115b.md E1).
- Bare tilled end stone (Ender Crop's after the swap) turns back into end stone on its first random tick.
- A bare `setblock rare-ice:rare_ice` becomes plain ice; it needs an item in its block entity.
- `/syt` with a tool string carrying NBT (`id{...}`) throws "An unexpected error occurred": use `<id>@fortune=N`.
- Botany Pots 9.0.44: right-clicking a pot opens its GUI (nothing inserted); `getRequiredGrowthTime` is on
  `be.getInventory()`. Use `/syt potset` and `/syt pot`.
- A server stuck after "Failed to load datapacks" or "Failed to start the minecraft server" never reads the fifo, so
  `scratch_server.sh stop` hangs: kill your own pid (check its cwd). `tools/scratch_query/wait_boot.sh` now reports
  MISSING / DATAPACK / FAILED instead of waiting out its cap.
- perfBot bots are real ticking ServerPlayers: they pick up XP orbs, Mending repairs on them, and
  `tryToStartFallFlying` makes them glide (SQ-QN runs on a bot via `zqn_probe.js`).
- KubeJS's class filter blocks java.nio.file: write files with JsonIO.write.
- Rhino: `BlockState.is(TagKey)` / `ItemStack.is(TagKey)` are ambiguous and the explicit-signature form is not remapped;
  use `ForgeRegistries.X.tags().getTag(tag).contains(...)`. `ItemStack.hasTag()` is not remapped either (read `.nbt`).
  `new SimpleContainer(2)` resolves to the varargs constructor (size 1).
- Command output pads "Gave 1    [", and attribute lines read "for entity NAME".
- qrun `waitlog` matches earlier lines in the same row: repeated counts in one row need distinct patterns.
- Swap remaps log only a summary line `[swaps] minecraft:item: N id(s) remapped`, not per-id lines.
- Client (r1115 phase 10): xdotool `key space` is too short for the elytra jump, hold 0.3 s; a command typed right
  after closing a screen or during a join hitch can lose its opening `t` (letters become hotkeys or chat). Gun portals
  despawn on reload. Plain ignition fails by design: light frames with `creativemagic:flint_and_terrasteel`. A walkable
  wall portal from `zc3 shoot`: 2.5 blocks from the wall, pitch 20. Rows needing your own server plus a client:
  `eval_client.py launch ... ownsrv=PID`. One-off client steps: `tools/scratch_query/cli_do.py`.
- PGJAR before r1115 needed an absolute path; a relative one silently booted without Power Grid (a client with Power
  Grid then crashes on join). prep_srv.sh now resolves it and prints "PGJAR NOT FOUND".

## Found in r1115 phase 11 (SQ-PTH, 2026-11-15)

- `summon ... {ActiveEffects:[...]}` is ignored in this pack: a slowness NBT does not slow the mob. For standing mobs
  set base `generic.movement_speed` to 0; push them with a probe motion command when a row needs them moving.
- Zombies do not chase perfBot bots. perfBot bots are invulnerable: damage rows need a mob (100 hp zombie) instead.
- Rhino finds no `setYRot` on perfBot's BotPlayer: turn bots with `tp <bot> ~ ~ ~ <yaw> <pitch>`.
- `/reload` takes about 50 s here; console commands queued meanwhile all run in the same tick after it.
- `datapack enable "file/x"` answers "Unknown data pack" until a /reload has discovered the pack; a scratch
  `srv/kubejs/data` file is simpler.
- Empty minecarts on rails lose 4 % speed a tick: start them close to the target.
- Dropped items still despawn at 5 minutes while held in a web; after a restart, lanes outside the setup forceload do
  not tick (forceload again after every boot).
- The perf suite measures `mods/` jars only; pass `PERF_JARS="a.jar b.jar"` (tools/perf/suite/server.py) for build
  jars, and copy the jar under test aside first (another session may rebuild build/libs mid-run).
- Client speeds: read them from client position steps; server-side motion values for a real player are not the client's.

## Found in r1116 phase 1 (SQ-TX, SQ-TXQ, 2026-11-16)

- Group A is peaceful: a bot's food refills (+1 every 10 ticks), so `pbot hunger A 10` is back at 20 before an eat
  at pace 2 (`result=FAIL`). Set `difficulty easy` for the row and back to `peaceful` after.
- A falling block placed with `setblock` schedules its own fall 2 ticks later: a `/cdevtest blocktick` sent at pace 2
  sees air. Send both under `pace 0`. Same for a 40-tick `/cdevtest watch` (2 s): the change must go in under
  `pace 0` with a short `sleep`.
- `pbot attack ... weak` reads the bot's real charge: 2 s of pace recharges it to `strength=1`. Under `pace 0` the
  weak hit lands in the target's hurt cooldown (no damage), which is vanilla.
- `/cdevtest kill` reports `xp=` from the orbs: 1.19.2 drops them inside `die()`, so xp is above 0 for animals.
- No server boots without Creative Compat: 8 own jars, Devtools included, declare it mandatory (FML refuses at once).

## Found in r1116 phase 2 (SQ-PS15B, 2026-11-16)

- `zpth watchnew` tags every entity of the type in its box that lacks the new tag, so an earlier projectile still
  hanging there joins the new watch and its `count`: use a fresh box or kill the old one first.
- Items and orbs held in webs keep ageing: a restart half must read them within 5 minutes of their summon (r1115 and
  r1116 both lost a first try). Summon them in the boot before the restart, right before `save-all`.
- A per-5-tick `zpth watch` hides climb and sink rates in webs; use every 1 tick for rate rows.
- `pgrep -f PATTERN` inside `timeout ... bash -c` matches its own command line (never ends); wait on qrun's output file
  instead (`until grep -q "LASTROW \.\.\. [A-Z]" OUT`).
- Toml switches for live-reload rows: a rename save (`sed -i`, or copy + `qrun.py toml` + `mv` inside `config/` for a
  key repeated per table) then `/cdevreload compat` applies at once; an in-place write (even one same-length
  `os.write`) was not picked up in 30 s on these servers.

## Found in r1116 phase 3 (SQ-PS15, 2026-11-16)

- Rare Ice (`config/rare-ice.properties` `allowInsertingItemsToIce=true`) takes a right click on `minecraft:ice` with
  any item and turns the ice into `rare-ice:rare_ice` holding it. A row that clicks plain ice tests Rare Ice, not the
  item; set `allowInsertingItemsToIce=false` in the srv copy (read at common setup: restart) to test the item's own code.
- A salt or snow layer resting ON ice pops as an item the moment the ice melts (support loss in the same `setBlock`),
  so a melt listener that removes the source layer never sees it. Put the source beside the target to test such rules.
- Melt targets in 1-deep holes (y -61 on the flat world) keep the melt water from flowing; `randomtick` only the target
  cell (`nofluids`) so neighbouring salt does not get its own dissolving tick.
- Removing a mod jar for a boot: use a fresh `level-name` when the world already holds that mod's blocks.
