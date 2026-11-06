# Perf suite: scratch-server performance tests with bot players (PB-)

A runner that drives one whole scratch-server session on its own: it builds the server, boots it, sets up each
scenario, measures a fixed window, and keeps going when the server ends without warning. Bot players stand in for
real ones so that random ticks, mob spawning and player-gated code run as they do in play. The general benchmarks
(G-) are built. The cooking (C-) and world interaction (W-) scenarios are proposals waiting for the owner's veto
(sections 8 and 9).

## 0. At a glance

| Part | Status |
|---|---|
| `perfBot` dev mod (`ceperfbot` 0.1.0): bot players, tick log, `/pbot` | BUILT 2026-10-28; chunk-tracking bug fixed 02:24; scratch server PASSED 2026-10-28 (SQ-PB-01 to -04, [testing/scratch_queue_run_2026-10-28.md](testing/scratch_queue_run_2026-10-28.md)) |
| Runner `tools/perf/suite/perf_suite.py` (RCON, end-of-server triage, retries, resume, report) | BUILT 2026-10-28; offline self-test 12/12 PASS (section 6) |
| General benchmarks G01 to G12 (`plans/general.py`) | BUILT 2026-10-28; first run g1 2026-10-28 03:04 to 05:27, every row ok (baseline; results in [testing/scratch_queue_run_2026-10-28.md](testing/scratch_queue_run_2026-10-28.md) "Perf suite g1"); G09 from rerun g1s |
| Cooking scenarios C- | PROPOSED, owner veto pending (section 8) |
| World interaction scenarios W- | PROPOSED, owner veto pending (section 9) |

Dev-only: nothing here ships and no player changelog bullet applies. The jar never goes into the instance's `mods/`
(`installToMods` refuses, like clientCapture).

## 1. Overview

Earlier measurements (perf.md sections 10 and 12) ran a dedicated server with no player. That leaves out most of
what a player causes. With no player near a chunk there are no random ticks (crops, saplings, weathering), no mob
spawning, and no despawning, and the pack's player-gated handlers (`armor_effects.js` horse walk,
`dragon_autorepair.js`) skip the level. A bot player fixes that without a client: it is a real `ServerPlayer`, logged
in through `PlayerList.placeNewPlayer` on a connection with no client behind it.

The runner is written to survive what a long unattended run meets: crashes, watchdog kills, the kernel OOM killer,
a hung server thread, another session's JVM, a busy machine, and the runner itself being killed.

## 2. How to run

```
./gradlew :perfBot:build --offline                         # the bot jar, picked up from build/libs
setsid nohup python3 tools/perf/suite/perf_suite.py run general g1 > /dev/null 2>&1 &
python3 tools/perf/suite/perf_suite.py status g1           # progress
python3 tools/perf/suite/perf_suite.py resume g1           # after the runner was killed or aborted
python3 tools/perf/suite/perf_suite.py report g1 [--baseline g0]   # perf/suite/g1/summary.md (also rewritten after every result)
python3 tools/perf/perf_report.py perf/suite/g1/jfr/G04-1.jfr      # per-mod attribution of one window
```

Options: `--groups S,N`, `--only G03a,G04`, `--scale 2` (doubles the counts in stress scenarios, for linearity),
`--include-disabled` (G05c), `--hang-s 120`, `--keep-srv`. A full `general` run takes about 2 h 15 min: group S
about 25 min, group N about 1 h 50 min (pregen about 15 min, soak 45 min).

The one-game-JVM rule holds: before every boot the runner waits (up to 60 min) while any other game JVM is up
(`tools/scratch_query/game_jvms.sh`), then aborts resumably. Scratch tests still need the owner's go-ahead (memory
`scratch-test-now-or-batch`).

## 3. Design

### 3.1 The bot mod (`development/creativeDevtools/members/perfbot`, nested in Creative Devtools since 2026-11-01)

- `BotConnection` extends `Connection`. Its channel is an `EmbeddedChannel` set by reflection (fields found by
  type). It carries Forge's `fml:netversion` attribute, because `ConnectionType.forVersionFlag(null)` throws. It also
  has a loopback remote address, since some mods read the player's IP. `send` drops packets but runs their
  listeners, so a disconnect still goes through the normal logout path.
- `BotPlayer` extends `ServerPlayer`. With no client, nothing calls `doTick()` or the chunk tracker's
  `move(player)`. Its `tick()` calls `doTick()`; `Bots` calls `move` for every bot from the server tick event and
  right after a spawn's teleport. Not from `tick()` (the Carpet pattern): the level ticks an entity only inside
  entity-ticking chunks, and a bot teleported away from its login tickets would never tick again (fixed 2026-10-28;
  before it, a bot spawned outside spawn chunks or a forceload loaded chunks at its login spot). Bots are invulnerable survival
  players. If one dies anyway, it logs in again at the same spot. Spectator mode is refused, because spectators
  don't count for chunk ticking or spawning.
- `TickLog` writes `perf/bot/ticks-<epoch ms>.csv`: every tick's duration in µs; once a second a status line with
  players, entities, loaded chunks and heap in use; and the runner's markers. It is flushed every second, so a killed
  server loses at most one second. The file's modification time is the server thread's heartbeat.
- `/pbot`: `spawn NAME X Y Z [DIM] [MODE]`, `remove NAME`, `remove-all`, `list`, `path NAME TOX Y TOZ SPEED`,
  `path-stop`, `hold NAME ITEM [COUNT]`, `use NAME X Y Z [FACE]`, `mark LABEL`, `census TAG`, `status`. This set is
  the contract shared with the feature-test session (2026-10-28); change it only with a note here.

### 3.2 Talking to the server: RCON

`scratch_server.sh` sends commands through a stdin fifo. Writing to it blocks forever once the JVM is gone (fixed
there on 2026-10-28 with `timeout 10`). The suite uses RCON instead (`rcon.py`, port 25598, a random password in
`<group>/rcon.secret`):

- Command output comes back, so `/pbot census` and `save-all flush` can be checked and timed.
- A call returns when the main thread has run the command.
- A dead server shows up as a refused connection.

Commands must stay under 1400 characters (the server reads 1460 bytes).

### 3.3 When the server ends

Every wait (settle, window, pregen, reload) checks once a second that the JVM is alive and that the tick log moved
within `hang_s`. Any ending raises `ServerEnded` with a triage written to `<group>/endings/<time>-<tag>/triage.txt`,
along with copies of `latest.log`, `console.log`, any new crash report and `hs_err_pid*.log`:

| Ending | How it is told apart |
|---|---|
| Crash with a report | new file in `crash-reports/` |
| Watchdog | `Considering it to be crashed` in the log |
| Java heap exhausted | `OutOfMemoryError` in the log or console |
| Kernel OOM killer | `journalctl -k` "Killed process PID" (when readable) |
| kill -9 from outside | exit 137 / -9 with none of the above |
| Hung server thread | tick log still for `hang_s` (120 s, above the 60 s watchdog): two `jstack`s, then kill -9 |
| JVM fatal error | `hs_err_pid*.log` |

The JFR recording is kept either way. A JVM that exited leaves its `dumponexit` file in `perf/exit/`. After a kill
-9, the disk repository left in `perf/jfr-repo/` is joined with `jfr assemble`. The part of the window measured
before the end is kept (`window.cut_short`). The server is then rebooted on the same world, `init` (gamerules) runs
again, and the scenario is retried (`retries`, default 1). A scenario that kills the server twice ends `failed`, and
that is itself a finding. Three failed boots in a row skip the rest of the group. A boot that crashes in mod loading
(the Create and SecurityCraft parallel-load races) is retried.

The runner saves `state.json` after every step and appends each result to `results.jsonl` with an fsync. It runs in
its own session (`setsid`), and so does the JVM, so ending the Claude session ends neither. If the runner is killed,
`resume` finds the JVM by its working directory and adopts it once RCON answers. A JVM that never answers is killed
and rebooted. On SIGTERM, SIGINT or SIGHUP the runner stops the server cleanly and saves state. `stop` kills a JVM
that hangs after "All dimensions are saved" (Async Locator threads).

### 3.4 One scenario

`pbot remove-all` -> `setup` commands -> settle (`settle` s) -> marker `start:ID#attempt` -> `start` commands ->
window (`measure` s; `kind` save / reload / restart time their action instead) -> marker `end:` -> window stats from
the tick log -> JFR dump of the window (`jcmd JFR.dump maxage=`) -> `pbot census ID` -> `teardown`.

Result row: tick count, real TPS, MSPT mean / p50 / p95 / p99 / max, ticks over 50 ms, entities (mean, max), loaded
chunks, heap floor and peak (the floor is the lowest heap-in-use reading, a stand-in for live heap after GC; JFR has
the real numbers), load average at start and end, `contended`. The census file has entities by type per dimension
and block entities by type near the bots.

### 3.5 Noise

Each window samples the machine every 10 s. Another game JVM, a running Gradle daemon or wrapper, or the load
average is recorded, and either of the first two marks the row `CONTENDED` in the report: rerun it. Both worlds run
fixed noon with no day or weather cycle (except G03b at night), so runs are comparable. The setup records the
modification times of our own jars (`state.json` group `setup.our_jars`). Compare runs only on the same builds,
the same `--scale` and the same world age.

## 4. Limits and decisions

- **Packets are not encoded.** Bots drop packets before Netty encoding and compression, so the cost of sending the
  world to players is not measured. The objects are still built: entity tracking, chunk packets, block updates.
  Network cost needs a real client (`tools/perf/eval/client_drive.sh`).
- **No client side**: FPS, chunk meshing and client mods are out of scope (perf.md 12 client runs).
- **Bots don't walk or fall.** A server-side player gets no movement physics without a client: a bot stays at the
  height it was spawned at (`spreadplayers X Z 0 1 false NAME` puts it on the ground) or follows `/pbot path` at a
  fixed height (teleport steps, as fast flight). Hostile mobs still target them.
- **No GUI clicks.** Anything a client packet starts (Quark sort button, menu clicks) has to be called server-side
  from a probe script, as `tools/kitchen_probe` does.
- **Worldgen is light by request (2026-10-28).** The play area is pregenerated in setup. Only the boot (G01) and the
  disabled G05c touch new land; pregen speed stays with perf.md 10.9's kit.
- `max-tick-time` stays at 60 s as on the real server, so a watchdog kill is a recorded result, not a setup error.
- Each flat-lab scenario has its own site, 2000 blocks from the last, so earlier sites unload when the bot leaves.
  Nothing is torn down.

## 5. General benchmarks (G-, built)

Group S = flat lab (difficulty normal, no natural spawning: `doMobSpawning false`; `spawn-animals` stays true, since false makes vanilla discard every animal, which emptied g1's G09 pen). Group N = normal world on the eval seed
(-8188900049893920259), structures on, spawn square r800 plus a 3400 x 320 corridor east of spawn pregenerated in
setup. View distance 10, simulation distance 8, `-Xmx6G`, JFR on, as shipped.

| ID | Group | Scenario | Settle / window |
|---|---|---|---|
| G01-S, G01-N | both | Boot of a new world: wall time to `Done` and the `Done (...)` figure | |
| S00 | S | Baseline: one bot on an empty flat site | 60 / 120 s |
| G08 | S | 500 item entities on the ground (ItemPhysic), never despawning, apart so they don't merge | 30 / 180 s |
| G09 | S | 200 vanilla farm animals (cow, sheep, pig, chicken) in one 40 x 40 pen | 60 / 180 s |
| G10 | S | 100 hostile mobs in a dark stone box, bot outside | 60 / 180 s |
| G11a | S | 1024 hoppers moving cobblestone barrel to barrel (27 stacks each, active for the whole window) | 30 / 180 s |
| G11b | S | 256 furnaces smelting raw iron | 30 / 180 s |
| G02 | N | Idle, no player (comparable to perf.md 10.1 / 12.1 idle) | 240 / 120 s |
| G03a | N | One bot at spawn, day; mobs populate during the 10 min settle | 600 / 300 s |
| G03b | N | Same, night (hostile spawning and AI) | 180 / 300 s |
| G04 | N | Four bots, 600 blocks apart (a small multiplayer server) | 300 / 300 s |
| G06 | N | `save-all flush` with four bots' chunks loaded: flush time and the tick spike | 30 / 60 s |
| G05a | N | Travel at 30 blocks/s (elytra) for 3000 blocks through pregenerated land: chunk load, unload, entity load | 30 / 110 s |
| G05b | N | Travel at 14 blocks/s (horse), 1400 blocks | 60 / 105 s |
| G05c | N | Travel at 10 blocks/s into new land, 2000 blocks. **Disabled** (worldgen) | 30 / 210 s |
| G07 | N | `/reload` with one bot: command time, time to a quiet log, the long tick | 30 / 90 s |
| G12 | N | Soak: two bots for 45 min; 5-minute slices show MSPT drift and the heap floor trend (leaks, the G1Full spiral of perf.md 7) | 60 / 2700 s |
| G01b | N | Restart of the same world, twice | |

## 6. Offline checks

`python3 tools/perf/suite/selftest/selftest.py [WORKDIR]` (2026-10-28: 12/12 PASS, twice) runs the real runner
against `selftest/fake_server.py`: a python copy named `java` that speaks RCON, writes the tick log and ends on
command. Only `Server.setup` is swapped, and `jstack` / `jcmd` are stubbed (their attach sends SIGQUIT, which kills a
non-JVM). Checked:

- a clean window;
- kill -9 mid-window: retried, dies again, ends `failed`, partial window kept, triage names the SIGKILL;
- a hung tick log: detected, killed, named as killed by the runner;
- an OOM exit;
- a crash on the next boot: recorded as `boot_failed`, retried;
- the runner killed mid-scenario: `resume` adopts the left-over server and finishes;
- no fake server left behind.

Also checked: `./gradlew :perfBot:build --offline` builds; every plan command expands under the RCON length limit
(longest 1072 characters, G11a).

## 7. Live test list (scratch queue SQ-PB-)

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-SK-01, -02.

Results 2026-10-28 night ([testing/scratch_queue_run_2026-10-28.md](testing/scratch_queue_run_2026-10-28.md)): PB-01 to -04 PASS (PB-02 wheat half: 17 of 81 grew with a bot, 0 without;
the night-mob half is G03b). PB-05 is the g1 run.

- [x] **PB-01 bot logs in:** boot with the jar and `-Dceperfbot.out=perf/bot` -> `pbot status` answers. Then `pbot
  spawn bot1 ~ ~ ~` -> latest.log shows a normal join (no exception from Forge networking, FTB, KubeJS login
  handlers, Lightman's), `list` names bot1, `forge tps` shows the overworld with loaded chunks near spawn.
- [x] **PB-02 bot counts as a player:** flat world, bot beside a 9 x 9 wheat field (age 0) with `randomTickSpeed 3`
  -> some wheat grows within 5 min (none grows with the bot removed); in a normal world at night, hostile mobs
  spawn near the bot (`census`).
- [x] **PB-03 path and census:** `pbot path bot1 <x+500> 200 <z> 1.5` -> `path_done:bot1` in the tick log after about
  334 ticks; loaded chunks move with the bot (status lines); `census t` writes `perf/bot/census-t.json`.
- [x] **PB-04 tick log and endings:** after a kill -9 of the scratch JVM the tick file ends with complete lines up to
  the last second; `jfr assemble` of the repo works (the runner's salvage).
- [x] **PB-05 general run:** *(PASSED 2026-10-28, run g1: every row ok, G02 5.55 ms; G09 rerun in g1s after the group S fix)* `perf_suite.py run general g1` end to end -> every G row `ok` or a triaged ending;
  `report g1` writes `summary.md`; the G02 idle reading matches perf.md 12.1 (about 5-6 ms on a fresh world).

## 8. Proposed: cooking and farming scenarios (C-), owner veto pending

Vetoed 2026-10-28 (ids not reused): C15, C16 (Create / AE2 engineering).

Owner's items first (marked **owner**), then suggestions. Each would be a flat-lab site, one bot beside it, a
180 s window, `--scale` for size. A probe script (`tools/perf/suite/plans/*.js`, copied in like the existing
probes) does the setup that commands can't.

| ID | Scenario | What it measures |
|---|---|---|
| C01 | **owner** Cooking for Blockheads kitchen: 40 fridges / counters / cabinets full (about 1000 distinct food stacks), cooking table connected, idle | Kitchen multiblock scans, block entity ticks |
| C02 | **owner** Same kitchen, the bot opens the cooking table 20 times in the window (server builds the available-recipe list) | Recipe list build against full storage; the per-open spike |
| C03 | **owner** Fridge autosort: Quark sort (pack's `QuarkSortingHandlerMixin`) on 40 full 2-tall fridges, called from a probe, 10 rounds | Sort cost per inventory |
| C04 | **owner** Crop growth: 4096 farmland crops, mixed vanilla and modded (Farmer's, Veggies, Vintage, Corn, Fruits trees, Vinery vines), bot present, `randomTickSpeed` 3; companion with wheat only | Random-tick cost per crop type; Serene Seasons and `icepreventscropgrowth` checks |
| C05 | **owner** Farm mobs idle: 300 animals in pens, mixed vanilla and modded (Domestication Innovation, ducks, Alex's, Moobloom variants, Neapolitan, Buzzier Bees cows, baby mobs) | Per-type AI cost (ducks were +0.21 ms idle in perf.md 12.1) |
| C06 | **owner** Breeding: the bot feeds every pen each minute (probe), babies grow; Quark feeding troughs in half the pens | Breeding AI, trough search (perf.md 7 fix), population growth |
| C07 | **owner** Incubation: 50 nests with eggs, chickens laying (Incubation + Incubation Additions) | Nest block entities, egg hatching |
| C08 | **owner** Incubation at scale: 500 nests | Linearity of C07 |
| C09 | **owner** Vinery: 50 fermentation barrels, apple presses, grapevine stems and lattices with grapes | Station ticks, vine random ticks |
| C10 | **owner** Brewin' and Chewin': 50 kegs fermenting, coasters with drinks | Keg ticks |
| C11 | **owner** Horses with dragon and copper armor: 100 horses with `dragonloot:dragon_horse_armor`, 100 with copper armor, 100 plain | `armor_effects.js` and `dragon_autorepair.js` handlers (perf.md 10.4) against plain horses |
| C12 | **owner** Hoe harvest area: the bot right-clicks ripe crops with a hoe across a 64 x 64 field, one use per tick (`/pbot use`) | Per-use cost of the area harvest, replant, drops |
| C13 | **owner** Cooking stations in use: 40 Farmer's Delight cooking pots on stoves cooking, 20 skillets, 20 cutting boards fed by deployers | Station ticks, recipe matching |
| C14 | Cooking pot recipe lookup: hoppers feeding 40 pots with ingredients that match no recipe | Repeated failed recipe matching against the pack's large recipe count |
| C17 | Rich soil and organic compost: 1024 compost blocks maturing, rich soil under crops | Random ticks of Farmer's Delight soil blocks |
| C18 | Bees: 40 hives (Buzzier Bees, pack bee nests) with full bee counts, flowers around | Bee AI and pathfinding |
| C19 | Eating: the bot eats 500 different foods in the window (probe) | KubeJS food handlers, Spice of Life tracking, Comfort / Nourishment effects |
| C20 | Kettles and drinks: 40 Farmer's Respite kettles brewing, Rustic Delight stations | Station ticks |
| C21 | Placed food: 1000 Display Delight / feast / pie blocks placed (block entities) | Chunk load and save cost of placed food |
| C22 | Fishing nets (Oceanic Delight) and Iron Fishing Rods: 40 nets in water | Net ticks |
| C23 | Seasons flip with a full farm: `season set` across spring to winter over the C04 field | Season change work, crop checks, snow and ice |
| C24 | Salt: 10,000 salt blocks and layers (The Salt and Vintage salt random ticks, SQ-SALT-07 at play scale) | Random-tick cost of salt |
| C25 | Campfires in rain: 100 campfires and cooking pots under rain (campfire rain feature, Unlit Campfires) | Weather and campfire checks |
| C26 | Wild crops and flowers spreading: the C04 field's wild-crop neighbours (Creative Delight wild crops) | Spread random ticks |
| C27 | Storage Delight and repacked storage: 100 cabinets full of food, hoppers pulling | Container block entities |
| C28 | Moobloom and mooshroom herds: 100 mooblooms placing flowers | Moobloom flower placement |

## 9. Proposed: world interaction scenarios (W-), owner veto pending

Vetoed 2026-10-28 (ids not reused): W05 to W07 (Create, AE2, IE), W08 (pipes and barges), W22 (CC computers), W23 (Pneumaticcraft drones), W24 (Botania). No engineering-mod scenarios.

| ID | Scenario | What it measures |
|---|---|---|
| W01 | **owner** Quark: feeding troughs, magnets moving blocks with pistons, variant chests and hoppers, Quark mobs (crabs, toretoises, stonelings, foxhounds) | Quark's world features |
| W02 | **owner** Supplementaries: faucets draining into jars, turn tables, bellows, pulleys, wind vanes (the old crash), clock blocks, cages, flower boxes | Supplementaries' block entities, Moonlight soft fluids |
| W03 | **owner** Vanilla-like: hopper item-sorter wall, observer and piston sugar-cane farm, redstone clocks | Block updates, pistons, redstone |
| W04 | **owner** Crafting stations: the bot crafts through crafting table menus (probe: menu + slot changes), FastWorkbench path, stonecutter, smithing with 288 trims, Polymorph conflicts | Recipe lookup per slot change |
| W09 | Storage Drawers: a controller with 200 drawers and hoppers in and out | Drawer lookups |
| W10 | Sophisticated Backpacks: the bot carries 3 backpacks with feeding, magnet, smelting and refill upgrades | Upgrade ticks per player |
| W11 | MCA village: 40 MCA villagers with families, jobs and gossip around the bot (MCA once stalled 5.1 s, perf.md 10.2 #7) | MCA villager AI |
| W12 | Iron farm and trading hall: villagers, golems, zombie scare, 50 villager trades by the bot | Villager AI, trade rebuilds |
| W13 | Raid: a pillager raid on a village with the bot present (difficulty normal) | Raid wave AI, Takes a Pillage, Blocky Siege |
| W14 | Immersive Weathering on a large build: copper, rust, moss and frost spreading over 50,000 blocks | Weathering random ticks |
| W15 | Lighting: place and remove 2000 torches in one tick (fill), repeated every 10 s | Light engine |
| W16 | Fluids: a large water and lava cascade (Create pipes and vanilla) | Fluid ticks |
| W17 | TNT and shrapnel: 50 TNT blasts in a stone area (the pack's TNT shrapnel feature) | Explosion and shrapnel cost |
| W18 | TreeChop and Ore Excavation: the bot fells 50 large trees and vein-mines 50 veins (probe) | Per-action spikes |
| W19 | Lootr: the bot opens 100 Lootr chests in structures | Per-player loot generation |
| W20 | Waystones and teleports: the bot teleports between 10 waystones every 10 s across dimensions | Dimension change and chunk load spikes |
| W21 | Decorative entities: 1000 item frames, armor stands with cosmetic armor, Immersive Paintings | Entity count with little AI |
| W25 | Thunderstorm with the full world: rain, lightning, Pretty Rain and Cool Rain server parts, snow under Serene Seasons winter | Weather work |
| W26 | Player death: the bot dies with a full inventory 20 times (Corpse, Second Chance, death handlers) | Death and corpse spikes |
| W27 | FTB Quests: the bot carries many quest items (quest task scans per player) | Quest detection cost |
| W28 | Mob variants and pets: Domestication Innovation pets following the bot, More Mob Variants, Alex's Mobs near spawn | Pet AI |

## 10. Queue review: what the bulk queue run changes for the suite

The queue in [testing/scratch_queue.md](testing/scratch_queue.md) runs first. Read 2026-10-28:

1. **Baseline timing.** The queue run installs and tests the open builds (patcherMod 1.1.03 with the GFB / HRU /
   season cache mixins, Creative Delight, the Vintage and Veggies ports, ModernFix). Take the first suite run (g1)
   after it, on the jars it leaves in `mods/`, and treat that run as the baseline. Earlier perf.md numbers are on
   other builds.
2. **Queue rows the bot can unblock** (once SQ-PB-01/-02 pass): SQ-HT-02 / -03 (horse armor handlers skip levels
   with no player), SQ-SALT-07 (random ticks), SQ-PF-03 (chunk waits from a player teleport), SQ-SK (starter kit on
   join, server half), SQ-IPO-02 (arm and truss), SQ-SV-01's viaduct, and the Moonlight soft-fluid map that stays
   empty until a player joins.
3. **Perf rows still in the queue that the suite does not replace:**
   - SQ-PERFCFG-03 (ModernFix with and without: a jar A/B the suite can't toggle; two `--only G02,G03a,G07` runs
     with the jar moved would do it).
   - SQ-GFB-03 / -04 (falling blocks, worldgen).
   - SQ-WGP-06 (cliffs, worldgen).
   - SQ-PF-06 (client).
4. **Shared tooling fixed by the parallel session (2026-10-28).**
   - `scratch_server.sh cmd` / `stop` no longer hang on a dead server.
   - `stop` kills only its own fifo feeder (it used to `pkill` every session's).
   - `eval_server.sh wait` prints DEAD (exit 3) when the JVM is gone.

   `overall_run.sh` / `overall_reboot.sh` still branch only on CRASH and TIMEOUT. A queue row that uses them
   after a silent kill goes on with a dead server. Read their `timeline.txt` for DEAD.
5. **Collector overlap.** The parallel session's `tools/perf/session/collect.sh` records queue-run JVMs and skips
   any server whose directory has `perf_suite.owner`, so it never moves the suite's JFR files.
6. **Group mapping.** Suite group S matches queue group A (superflat) but with difficulty normal and the watchdog
   on. Group N matches group B on the eval seed. Results don't mix: the suite's worlds are its own.

## 11. File map

| File | Role |
|---|---|
| `development/creativeDevtools/members/perfbot/` (nested in Creative Devtools) | the dev-only mod (`CePerfBot`, `BotConnection`, `BotPlayer`, `Bots`, `TickLog`, `BotCommands`) |
| `tools/perf/suite/perf_suite.py` | runner: run / resume / status / report |
| `tools/perf/suite/server.py` | setup on `eval_server.sh`, boot, liveness, RCON calls, stop, triage, JFR salvage |
| `tools/perf/suite/rcon.py`, `ticklog.py`, `builders.py` | RCON client, tick log reader and window stats, command builders |
| `tools/perf/suite/plans/general.py` | the G- scenarios |
| `tools/perf/suite/selftest/` | offline self-test and the fake server |
| `perf/suite/<run>/` (untracked) | state, results, runner log, tick logs, JFR windows, census, endings |

## 12. History

- 2026-10-28 night: run g1 (baseline): G02 5.55, G03a 18.5, G04 50.1 ms (four spread bots reach the budget), G12
  no drift but heap floor 5.4-5.8 GB of 6 GB with 1.7 s GC-pause ticks; group S animal fix, G09 rerun g1s 16.2 ms.
- 2026-10-28 night: live tests PB-01 to -04 PASS; bots now move their chunk tickets from the server tick (bug: a bot
  spawned away from its login spot never ticked); runner rewrites `summary.md` after every result (owner: summaries
  saved regularly, never capped); first run g1 started 03:04.
- 2026-10-28: built the mod, the runner, the G- plan and the self-test; C- and W- proposed for veto. Split agreed with
  the parallel "Scratch server performance tests 2" session. This session owns perfBot, `tools/perf/suite/` and this
  file. That session owns `scratch_server.sh`, its session collector (queue runs) and feature tests that use
  `/pbot`. Its benchmark rows were dropped in favour of the G- ids.
