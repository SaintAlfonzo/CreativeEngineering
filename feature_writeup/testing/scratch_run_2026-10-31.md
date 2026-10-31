# Scratch run 2026-10-31: everything queued since the 10-28 night run

Owner request (2026-10-31): comprehensive scratch tests for every feature added since the last scratch test, fake
player and client testing included. Order: server checks, then the worldgen changes, then the client block, then the
longest operations. Capture performance the whole way through, so a later debugging session can cut the recordings to
any row. Status: **built, not run** (no server booted while writing this; a parallel session was building at the time).
Up to date with the tree as of 2026-10-31 00:50, including the effects batch the parallel session landed at 00:44
(SQ-EFX) and the Dreams & Desires wood removal (SQ-RW-28). Before running, repeat the check of section 3 and look for
queue sections newer than SQ-EFX.

## 0. Performance capture (do this first, keep it on to the end)

- `bash tools/perf/session/perf_session.sh start r1031` before the first boot. scratch_server.sh writes every boot,
  command and stop to its timeline; `qrun.py` marks every row ID as a segment by itself.
- Every scratch server records JFR from launch (scratch_server.sh setup; `JFR_MAX` 1g per boot). Never set `CE_PERF=0`.
- `qrun.py` also writes `RES/timeline.tsv` (wall-clock start and end of each row, server or client), so the client
  recording can be cut to a row too.
- Client block: `eval_client.py launch` records JFR from JVM start; before killing the client run
  `jcmd <pid> JFR.dump name=ce filename=$RES/client.jfr` (as `tools/perf/eval/client_drive.sh` does).
- At the end: `perf_session.sh stop`, then `perf_session.sh report r1031` **without `--prune`**: the raw recordings
  are the debugging material. `/` is nearly full: check `perf_session.sh status` and `df -h /` between blocks; if
  space runs short, delete the raw recordings of boots whose rows all PASSED first, never those with a FAIL.
- Add the session name and `perf/sessions/r1031/summary.md` to the Run log line in [scratch_queue.md](scratch_queue.md).

## 1. Scope

Every queue row added after the 10-28 night run started (2026-10-28 01:10) that has no result, plus the retest of
that run's SQ-SALT-01 fix (SQ-CM-10), the Botanist workbench row SQ-POT-06 (10-29) and new rows for changelog bullets
that had none (SQ-GF31-01 to -06, SQ-RW-28). 202 rows in 11 batch files.

Not in this run:
- SQ-WG30 rows that PASSED on 10-30; SQ-WG30-D03 FAILED and has no fix yet (TODO `structures/terrain #7`); the
  PARTIAL A02, A03, C02, E03 stay as they are.
- Rows queued before 10-28 01:10 and never run (SQ-PR, SQ-OPEN, the 10-25 to 10-27 Creative Delight, flower pot,
  perf-fix and hygiene sections): the older backlog, a separate run.
- Bugs found but not fixed: `food/drinks #12` (Kettle spout fillings), `baking/baked-goods #37` (zucchini quiche).
- SQ-EFX-12 (jump height) has no server probe: its check is in the client block (CQ-EFX), as are SQ-EFX-13 and -14
  (eating and the tea pipe need a real player).

## 2. Tools

| Tool | What |
|---|---|
| [tools/scratch_query/prep_srv.sh](../../tools/scratch_query/prep_srv.sh) | `prep_srv.sh OUT LEVEL A\|B [prop=value ...]`: scratch_server.sh setup, then hard-linked jars (safe against a parallel `installToMods`), dev jars newer than `mods/` swapped in, perfBot added, `/zq` probe copied, group B normal world on seed 8675309. Refuses while a game JVM is up |
| [tools/scratch_query/qrun.py](../../tools/scratch_query/qrun.py) | batch runner: `run OUT BATCH RES` (server fifo) or `client GAME BATCH RES` (xdotool chat + screenshots); `toml FILE SECTION KEY VALUE`; `list BATCH`. Directives in its docstring. Results in `RES/results.md` (PASS / FAIL / LOOK), each row's log segment in `RES/rows/`, captured coordinates in `RES/vars.json` |
| [tools/scratch_query/sp_world.py](../../tools/scratch_query/sp_world.py) | copies a scratch world into the client's `saves/` with `allowCommands` on |
| `tools/scratch_query/batches/r1031/*.qb` | the batch files below |
| `/zq` ([zq_probe.js](../../tools/scratch_query/zq_probe.js)), perfBot `/pbot` | recipe / tag / food queries; fake players (`spawn`, `hold`, `use`; armor and off hand via vanilla `/item replace`) |

LOOK = the row ran and its automatic checks passed, but a person or agent must judge part of it (counts, positions,
screenshots); the look line says what. Judge LOOK rows from `RES/rows/<ID>.log` and `RES/shots/`, then write the
final PASS / FAIL into the queue row.

## 3. Jars (check right before A1)

prep_srv.sh swaps a build jar in only when it is newer than the `mods/` jar it replaces. At writing time:

| Jar | `mods/` | build/libs | Rows that need the build |
|---|---|---|---|
| patcherMod 1.1.04 | 10-31 00:44 | same | all patcherMod rows (effects batch included) |
| creative_delight 1.0.0 | 10-31 00:29 | same | CD rows, monkey merge, Baklava icon |
| backportedBrewin -04 | 10-31 00:30 | **00:43** | SQ-EFX-13 food icons |
| backportedVintage | -08 | **-09** (10-29 20:24) | SQ-BJ-01 to -03 |
| backportedVeggies -03 | 10-25 | **10-30 18:32** | SQ-DM (mature dandelion) |
| repackedStorage | 10-25 | **10-29 23:54** | SQ-CM-34 (client mod list) |
| backportedVinery -06 | 10-31 00:41 | same | crop seasons, vine shearing, SQ-EFX-10, -12 |
| backportedDisplay -01 | 10-27 | 10-31 00:43 | none queued: not swapped (ask the owner before adding it) |
| perfBot 0.1.0 | never in `mods/` | 10-28 02:24 | every bot row |

The client block uses the instance's `mods/` (eval_client.py hard-links them): the three bold jars are only in it if
the owner installs them first. Otherwise SQ-BJ looks, the mature dandelion look and CQ-CM-33's mod name wait.

## 4. Order

OUT = a scratchpad dir per world (`$S/a1`, ...), RES = `perf/runs/r1031` (one results dir for the whole run, so
`vars.json` carries locate results from B1 to B1b). Q = `python3 tools/scratch_query/qrun.py`, B =
`tools/scratch_query/batches/r1031`. Before every boot: `bash tools/scratch_query/game_jvms.sh` prints nothing.
Boot: `bash tools/creative_labels_test/scratch_server.sh boot OUT`, wait for `Done (` in `OUT/srv/logs/latest.log`.
Stop: `scratch_server.sh stop OUT`. One game JVM at a time throughout.

### Server block (fake players, about 1 h 30)

| Step | Setup | Run | About |
|---|---|---|---|
| A1 | `prep_srv.sh $S/a1 r31a1 A`; T = `$S/a1/srv/config/creativeengineering-common.toml`: `$Q toml $T monster_box cooldownTicks 200`, `$Q toml $T copper_poisoning intervalSeconds 10`, `... jitterSeconds 0`, `... chancePerPiece 1.0`; boot | `$Q run $S/a1 $B/a1_boot.qb RES`, then `a1_data.qb`, `a1_effects.qb`, `a1_world.qb` | 40 min |
| A2 | stop; `$Q toml ... monster_box vaultMode false`; `$Q toml ... dandelion_maturing richSoilTicks 0`; in `srv/server.properties` `spawn-monsters=true`, `difficulty=normal`; boot (same world) | `a2_variants.qb` | 20 min |
| B1 | stop A; `prep_srv.sh $S/b1 r31b1 B spawn-animals=true spawn-monsters=true difficulty=normal`; `$Q toml $S/b1/srv/config/creativeengineering-common.toml named_mobs chance 1.0`; boot | `b1_gameplay.qb` | 35 min |
| B1b | stop; add `-Dcreativeengineering.iwFastTick=false` to `srv/user_jvm_args.txt`; boot | `b1_iwf.qb --rows SQ-IWF-02b` | 5 min |
| B1c | stop; remove the flag; `block_growths = false` in `srv/config/immersive_weathering-common.toml`; boot | `b1_iwf.qb --rows SQ-IWF-04`; stop | 5 min |

### Worldgen block (about 2 to 3 h; every world fresh, seed 8675309)

Common srv toml for W1: `$Q toml .../creativeengineering-common.toml worldgen.debug structureDensity 4.0`,
`worldgen.earth_fill log '"ALL"'`, `worldgen.meta_structures log true`.

| Step | Setup | Run | About |
|---|---|---|---|
| W1a | `prep_srv.sh $S/w1a r31w1a B`; common toml; `worldgen.earth_fill overrides '["minecraft:village_plains=-1,-1", "village_savanna=-1,-1,-1"]'`; boot | `w1_earthfill.qb --rows SQ-EF-01` | 15 min |
| W1b | `prep_srv.sh $S/w1b r31w1b B`; common toml (no overrides); boot | `w1_earthfill.qb --rows SQ-EF-02,SQ-EF-11,SQ-EF-03,SQ-EF-04,SQ-EF-05,SQ-EF-06,SQ-EF-08,SQ-EF-09,SQ-EF-10`; then the SQ-EF-08 restart half (save-all, stop, boot, locate) | 45 min |
| W1c | `prep_srv.sh $S/w1c r31w1c B`; common toml + `worldgen.earth_fill beardFootprint true`; boot | `w1_earthfill.qb --rows SQ-TER-05` | 10 min |
| W2 | `prep_srv.sh $S/w2 r31w2 B difficulty=normal spawn-monsters=true spawn-animals=true`; `worldgen.meta_structures log true`; boot | `w2_metas_spawns.qb` (each locate up to 300 s) | 60 to 120 min |
| W3a | `prep_srv.sh $S/w3a r31w3a B`; boot | `w3_terrain.qb --rows SQ-TER-02,SQ-TER-03,SQ-TER-04,SQ-TER-06,SQ-SVA-03,SQ-SVA-04` | 20 min |
| W3b | `prep_srv.sh $S/w3b r31w3b B`; `worldgen.ocean_geodes floorPatch '"CUT"'`; boot | `w3_terrain.qb --rows SQ-TER-06b`; stop | 10 min |

A `/locate` that times out ends its row LOOK with "type ... timed out" in the segment: rerun that row on a world
with `[worldgen.debug] testOverrides = true` (share 1 for the meta), as the META31 rows say.

### Client block (after worldgen, about 1 h 30, the agent drives it)

1. No game JVM up. `python3 tools/perf/eval/eval_client.py setup $G` (G = a scratchpad dir), then
   `python3 tools/scratch_query/sp_world.py $S/a1/srv/r31a1 $G QA_A1`, same for `$S/w1b/srv/r31w1b` (QA_W1),
   `$S/w2/srv/r31w2` (QA_W2), `$S/w3a/srv/r31w3a` (QA_W3). Delete `saves/PerfEval` so QA_A1 sorts first.
2. `python3 tools/perf/eval/eval_client.py launch $G`; at the title screen open QA_A1 (Singleplayer, first world,
   Play: the click path of `client_drive.sh` for an 854x480 window); wait until in game.
3. `$Q client $G $B/c1_client.qb RES`. Then do the `hand:` steps of each LOOK row with xdotool and `import -window`
   screenshots, and the CQ-WGLOOK tours in QA_W1 / QA_W2 / QA_W3 at the positions in `RES/rows/*.log` and
   `RES/vars.json`.
4. `jcmd <client pid> JFR.dump name=ce filename=RES/client.jfr`, then close the client (kill the pid matched by its
   cwd, as client_drive.sh does).

### Longest operations last (unattended, about 4 h)

| Step | What | Rows |
|---|---|---|
| L1 | SQ-WGP-07 pregen recipe (2601 chunks) on the current jar, JFR on: pregen time against 3:50, `[earth-fill]` CANCELLED count against SQ-WG30-E01 (15 inland) | SQ-EF-07 |
| L2 | Perf suite on the current jars: `python3 tools/perf/suite/perf_suite.py selftest` style check first (`tools/perf/suite/selftest/selftest.py`, 12/12), then `setsid nohup python3 tools/perf/suite/perf_suite.py run general g2 > /dev/null 2>&1 &` (about 2 h 15), `perf_suite.py report g2`, compare with `g1`. The G04 four-bot scenario covers SQ-PF16-04 and SQ-MCL-06 (run G04 again with `mobCapForceLevel` 1 and 2 for MCL-06's per-level numbers), G03a / G04 / G12 cover SQ-IWF-05 | SQ-PF16-04, SQ-MCL-06, SQ-IWF-05 |
| L3 | Resource pack bench (client, about 1 h): `python3 tools/perf/rpack/selftest/run_selftest.py` (4/4), then CQ-RP-01 to -03 as queued | CQ-RP-01 to -03 |
| L4 | DH flight in the Nether and the End with DH generating (client) | CQ-WG30-A04 |

## 5. Row map

| Queue section (scratch_queue.md) | Rows | Batch |
|---|---|---|
| Lunchboxes | SQ-LB-01, -02; client LB-01 to -14 | a1_boot, a1_world; c1 CQ-LB-01 |
| Food batch | SQ-PS-05, SQ-CM-20 to -24, SQ-KT-21, SQ-BJ-01 to -03 | a1_data, a1_world (KT-21 bot) |
| Stepstools | SQ-STL-01, -02; STL-03 to -10 | a1_boot, a1_data; c1 CQ-STL |
| CD members 22 to 24 | SQ-CM-30 to -34 | a1_data; c1 CQ-CM-33 |
| Buckets | RE-28; RE-29, -30 | a1_data; c1 CQ-RE-29 |
| Rose quartz | EN-21; EN-19, -20 | a1_boot, a1_data; c1 CQ-EN-19 |
| SQ-EFX effect changes (10-31 00:44) | SQ-EFX-01 to -11; -12 to -14 and EF-22 to EF-30 in the client | a1_effects; c1 CQ-EFX |
| D&D woods removed (recipes_wood.md 2.6) | SQ-RW-28 (new row); RW-28 | a1_effects; c1 CQ-RW-28 |
| Gap fill | SQ-SPC-01, -02, SQ-RW-23 to -27, SQ-DD-08, SQ-CP-01; CQ-SPC-01, CQ-RW-23 | a1_data; c1 |
| 10-28: WTHIT silk touch, whistle and bees, mob cap, IW growth, UA coral, sheared grass, falling block splash | SQ-WT-01, SQ-PF16-01 to -04, SQ-MCL-01 to -06, SQ-IWF-01 to -05, SQ-UC-01 to -03, SQ-SG-01 to -03, CQ-FBS-01 to -03 | a1_boot, a1_world, a2, b1, b1_iwf, L2; c1 CQ-FBS |
| 10-28 retest | SQ-CM-10 (salt); SALT-02/-03 halves | a1_data; c1 CQ-SALT |
| Flower potting | SQ-POT-06; CQ-POT-02 | a1_boot, a1_world; c1 |
| Named mobs, gates, ladders | SQ-NM-01 to -03, SQ-VG-00 to -03, SQ-VL-00 to -02 | b1, a1_boot, a1_world |
| Sky Village addons | SQ-SVA-01 to -04 | a1_boot, a1_world, w3; c1 CQ-SVA |
| SQ-EF earth fill | SQ-EF-01 to -11 (EF-07 in L1) | w1 |
| Terrain batch 4 | SQ-TER-01 to -06 | a1_boot, w1c, w3 |
| SQ-META31 | SQ-META31-01 to -09 | w2 |
| SQ-SB | SQ-SB-01 to -08 | a1_boot, w2; c1 CQ-SB-06 |
| Dandelions | SQ-DM-01 to -03 | a1_world, a2 |
| UI renames | CQ-UI-01 to -04 | c1 |
| Firefly bushes | CQ-FF-01 (server) | b1 SQ-FF-01 |
| Monkey merge | SQ-MG-17 to -20 | a1_boot, b1; c1 CQ-MG-20 |
| Admin date commands | SQ-TD-01 to -05, CQ-TD-06 | b1; c1 |
| Armor cap | SQ-AC-01 to -04 | a1_boot, a1_world (bot); c1 CQ-AC-04 |
| Crop seasons | SQ-CS-01 to -09, CQ-CS-07 | a1_boot, a1_world, a2, b1; c1 |
| Watering can | SQ-WC-01 to -05 | a1_boot, a1_data, a1_world (bot); c1 CQ-WC-04 |
| Monster boxes | SQ-MB-01 to -07, CQ-MB-08 | a1_boot, a1_world (bot), a2; c1 |
| Vine shearing | SQ-VS-01 to -08 (05 to 08 new: the shears interaction on a bot) | a1_boot, a1_world; c1 CQ-VS-09 |
| Resource pack bench | CQ-RP-01 to -03 | L3 |
| New gap rows | SQ-GF31-01 to -06, SQ-RW-28 (section "Gap fill, 2026-10-31" in the queue) | a1_data, a1_world, a1_effects, b1; c1 |

Rows moved from their queued world: SQ-TER-02 to -04 need a hillside (queued A, run in W3a); SQ-CS-03 to -05 run on
the superflat (plains biome, same seasons) instead of a normal world; SQ-VG and SQ-VL run on the superflat (flat
ground, no monsters); SQ-MCL-05 and SQ-PF16-03 need caves (B1).

## 6. After the run

1. Turn every LOOK into PASS or FAIL in its queue row (result in the ID cell, as the SQ-WG30 rows do) and add one
   Run log line naming this file, the perf session and the result counts.
2. Move results into each feature writeup's test list (the IDs in brackets in each row) and the status words
   ("built, scratch server PASSED") in `feature_writeup/README.md`.
3. Copy the server-safe dumps back if a `/dumpall` was taken (setup notes in the queue).
4. FAILs: debug from `RES/rows/<ID>.log` and the perf segment of that row; a fix that changes behaviour goes to the
   owner first.

## 7. Run notes (execution, started 2026-10-31 01:05)

Running log of the execution: steps done, problems met, decisions. Results per row are in `perf/runs/r1031/results.md`.

- 01:05 Pre-check: no game JVM, `/` 37 GB free, jars as in section 3 (no newer builds since writing). No queue section
  newer than "Gap fill, 2026-10-31". Perf session `r1031` opened.
- 01:06 A1 (`$S/a1`, r31a1) booted in 10 s after launch (JFR on). a1_boot: PASS 18, LOOK 5, FAIL 1.
  - SQ-TER-01 FAIL (real, not the runner): the load line warns `philipsruins:ancient_crypt is in
    terrain_adaptation/sunk_start but its start pool has a terrain_matching piece; ignored`. The crypt was added to
    `sunk_start` in batch 3 (structure_tuning.md "Sunk starts (#3)") and is now refused by the same jar's guard, so
    the crypt is not sunk (ST-238 expects no warning). Counts: 109 tagged, 99 trimmed, 1 air-trimmed, 7 sunk starts.
    Not fixed here (worldgen behaviour, owner decides: drop the tag or narrow the guard to the start element).
- 01:20 a1_data: PASS 31 after re-judging, LOOK 17 (judged at the end). SQ-STL-02 and SQ-DD-08 failed on batch
  regexes (recipe ids contain a digit, `_seat_to_1_`; `/zq` prints output ids without namespace): fixed in
  `a1_data.qb`, rerun PASS (16 stepstool recipes, both tags 16; 16 painted asphalt + soap recipe, no D&D decor asphalt).
- 01:25 a1_effects: SQ-EFX-10 failed on a per-line count (ActiveEffects prints on one line); the line has Ids 10, 11,
  12, 22 at Amplifier 0: PASS. SQ-EFX-04: the horse vanished at once. Cause: vanilla `ServerLevel.tick` discards
  every Animal while `spawn-animals=false` (A1 setting). Decision: A2 also sets `spawn-animals=true` (server.properties
  only) and reruns SQ-EFX-04 there; the A2 cow/sheep row needed it too. Count lines in EFX-04/-10 changed to expects.
- 01:35 Loading Protection (`protectionDuration = 150`) covers a bot for 150 s after `pbot spawn`: SQ-EFX-02 and -03
  ran 30 to 60 s after qe joined, so their Health reads (all 20.0f, and Poison II never applied) are void. Rerun after
  a1_world with qe online for minutes. Batches: any bot row that checks damage or harmful effects needs `sleep 150`
  after the spawn (added to a1_effects.qb after its `pbot spawn`).
- 01:50 a1_world first pass: PASS 50, LOOK 39, FAIL 12. Triage:
  - Real: SQ-AC-02 armor 33, not 36. The cap mixin works (33 > 30, SQ-AC-03 PASS) but DragonLoot's own
    `dragonloot-common.toml` [dragonloot.armor] (7 / 10 / 9 / 7) wins over the KubeJS tune (7 / 12 / 10 / 7). Measured
    per piece on the bot: helmet 7, chest 10, legs 9, boots 7. Not changed (owner: set the toml to 12 / 10 or accept 33).
  - Batch errors, fixed in `a1_world.qb` and judged from the logs: SQ-SVA-02 (log says `Loaded template`; 17 of 17,
    keg 1, wine press 6: PASS), SQ-VS-03 (a grown weeping vine's block below the head is `weeping_vines_plant`; age 0
    grew 9 blocks, age 25 none: PASS), SQ-VS-02 (unsheared poppy vine spread to 19 blocks: PASS).
  - SQ-GF31-03: the bot's use on the barrel opened it (no sneak), nothing placed. Row now places the pumpkin by
    `setblock` (same `CarvedPumpkinBlock.onPlace` pattern check). Rerun.
  - SQ-MB-03: 3 items = mob loot from the killed wave; rerun with `doMobLoot false` found 0 because the bot, still
    beside the box, picked the reward up (its inventory: raw gold 4, raw copper 5 = one reward per wave). Reward
    works: PASS on judgment. SQ-MB-04: cooldown (200 ticks) ran out with the bot beside the box -> re-armed; row now
    moves the bot away before the kill: rerun PASS.
  - SQ-VG-01 PASS was void (`fill ... hollow` one block high is solid: villager inside a fence square). SQ-VG-02 / -03:
    Dramatic Doors tall doors have `third=lower|middle|upper`, not `half`; both setblocks failed (open doorway, no
    door). Rows now build the ring with an inside clear, place the three door parts, sample the door with `say`.
  - SQ-WC-03 / -05: see next note (bot use path).
- 02:10 More batch fixes, all rerun on A1:
  - Bot use path: `pbot use` is a block use only (`useItemOn`); the client follows a PASS with an air use, the bot
    does not. The watering can loads fertiliser in `Item.use` (air use), so SQ-WC-03 never loaded. Added
    `/zq useitem <player>` to `tools/scratch_query/zq_probe.js` (scratch-only; perfBot is another session's tool, left
    alone). SQ-WC-03 rerun: BonemealLevel 3, bone meal 61 (LOOK -> PASS).
  - SQ-WC-05: mature wheat takes the right-click as a harvest (CONSUME) before the can's `useOn`; the row now uses
    age 0 wheat (no fertiliser in the can, so age is irrelevant). Dry farmland drops 7 -> 6 by random tick within
    seconds (vanilla), so the check now runs at `randomTickSpeed 0`; the count was also wrong (4 setup fills also log
    "filled 9"). Rerun: 9 farmland and 9 mulch soaked, WaterLevel 15, second use spends nothing: PASS.
  - SQ-VG-01 / -02: zombies from the SQ-MB waves killed the villager; VG rows now set Peaceful (villager AI is the
    same) and clear mobs, select `#zq:villagers` (new scratch-only tag in `tools/scratch_query/datapack/zq`, copied by
    prep_srv.sh: vanilla + MCA villagers). VG-01 PASS (stayed in with the gate closed and open), VG-03 PASS (iron door
    kept it out). VG-02: the villager started outside (z -58) and ended at its bed inside: it went through the closed
    tall oak door: PASS; the open state was never caught in 1 s samples (now a look line, not an expect).
  - Bots are invulnerable (perfBot `Bots.java` `setInvulnerable(true)`): no Health read on a bot means anything.
    SQ-EFX-02, -03, -09 now read Health on a NoAI pillager (queue setup note: no Poison on undead). Results: EFX-02
    24 -> 24 PASS; EFX-03 Poison II control drop 13.0, with Magic Resistance 10.4 (0.80 x) PASS, Wither I 24 -> 24
    PASS; EFX-09 six ticks after the give: Poison I, Duration 155 (160 at start), Health 24 (no direct damage) PASS.
  - SQ-MB rows on A1 ran at Normal (SQ-MB-02 sets it); the A1 world is now Normal again after the VG rows.
- 02:40 A1 LOOK rows judged offline (subagent; table in `perf/runs/r1031/judged_a1.md`): PASS 17, FAIL 6, OPEN 9.
  Real pack problems (not fixed here: recipe and tag behaviour, owner decides):
  - SQ-CM-32 (+ SQ-R31-BOOT / -KJS): `kubejs/data/creative_delight/kubejs/oceansdelight/recipes.js` removes every
    recipe with a tentacle input (line 39) BEFORE the tentacle -> calamari `replaceInput`s (41 to 47): squid_rings,
    glow_squid_stew, seafood_skewer, squid_and_pickles are gone; sea_pickle_roll, shrimp_rice, squid_salad,
    oceanic_delight:takoyaki keep an empty `#forge:squid_tentacles` slot; `miners_delight:cooking/takoyaki` has two
    empty slots. `[NCH] replaceInput ... "#forge:squid_tentacles" does not exist or is empty` 16 times in the A1 log
    (new against 10-28). Checked the line order myself.
  - SQ-RW-23: `#creativeengineering:smokable/planks` holds 9 Botania planks and `upgrade_aquatic:driftwood_planks`;
    `tags/wood_tags.js` excludes them only from the log and wood tags.
  - Seen in passing: six moredelight potato recipes (mashed_potatoes, potato_salad, four diced_potatoes_with_*) have
    an empty `#forge:sliced_potatoes` slot; kitchen.md 2.3 says 304 coasters, log and changelog say 302.
  Test-script FAILs: SQ-R31-KJS (NCH errors go to latest.log, not kubejs/server.log: needs a `bootreject \[NCH\]`),
  SQ-VL-01 / -02 (the VG beds and villagers nearby pulled the villager away; rerun with VG cleared).
  OPEN, need a probe or the client: PS-05, BJ-01, BJ-02, RW-24, RW-27, EFX-08, DM-03 (3 dandelions of 20 uses
  against 50 %), POT-06, MB-05 (queue says hardness 50, changelog 25).
- 03:30 A2 (same world, vaultMode false, richSoilTicks 0, Normal, monsters on, plus `spawn-animals=true`):
  a2_variants PASS 3 (A2-BOOT, MB-07, DM-02). Judged / rerun:
  - SQ-MCL-02 PASS (289 chunks, level 0); SQ-MCL-03 PASS (578 apart, level 1; 306 together, level 0).
  - SQ-MCL-04 void: a toml edit on a running server is not reloaded (`ceperf mobcap` still level 0; zombies +
    skeletons + spiders + creepers = 63). Rerun with `mobCapForceLevel 2` set before a restart (next note).
  - SQ-PF16-02: a summoned (not shot) whistling arrow pulls nothing: moves to the client block (CQ-PF16-02), as its
    look line says.
  - Season command takes upper case (`MID_WINTER`); every `season set` in a2_variants.qb was lower case, so CS-03 to
    -05 first ran in the default season. Fixed batch-wide.
  - SQ-CS-05 ids: hemp is `growth=bottom0`, tobacco `blockstate=0`, the Ender crop is `endercrop:ender_crop`; the
    Nether's Delight fungus colony grows only on `nethersdelight:rich_soul_soil` and both it and the Ender crop need
    a dark box (stone box in the row now). Mid Winter: hemp, tobacco, red grape bush, Ender crop, colony (age 3 on
    rich soul soil) all grew: PASS. Control: colony on nylium or Rich Soil does not grow even in summer (mod rule).
  - SQ-CS-03 / -04: ripe counts after 2 min at speed 30 were 0 to 3 (noise). New `/zq ages` (mean age per field) in
    zq_probe.js; rows rewritten. Winter open 2.30 vs glass 2.84 (0.81, expected 0.75): PASS. Spring vs summer, open
    fields, 6 and 5 field runs: mean 3.27 vs 2.78 (1.17; the clean replicate 3.30 vs 2.73 = 1.21, expected 1.25):
    PASS. Spring glass 2.95 (neutral, about the summer level): bonus cancelled under glass.
  - SQ-EFX-04 (rerun here, animals kept): Regeneration II, Resistance II, Health Boost II; Health 61.0 before and
    after the 5 s refresh: PASS.
  - Follow-ups (`a1_followup.qb`, new): RW-23b confirms the RW-23 FAIL (13 smoked-shape recipes; botania, driftwood,
    crimson and warped inputs in most shapes). BJ-01b: no vintagedelight jam or relish bottle made by anything
    (relish 0; `jam_bottle` only matches Upgrade Aquatic's mulberry jam bottle, not removed): PASS on the server half,
    note for RC-172's JEI search. BJ-02b: More Delight sandwiches take #forge:bread (garlic bread, cantaloupe bread,
    toast...): PASS; `canned_goods:canned_bread` has no recipe by that id (client check). PS-05b: vacuum bucket + 4
    sugar, mortar 3 sugar -> 4: PASS; the 0.5 second results need JEI (client). RW-27b: one fermenting recipe, 4 bark
    + salt: PASS (2400 ticks not printed). EFX-08b: largest Poison Duration 197: PASS. DM-03b: bot 4 blocks away,
    20 uses: 7 dandelions, 51 leaves, shears Damage 20: PASS (the A1 count of 3 was the bot picking up drops).
  - SQ-VL-01 / -02: the ladder ended one block below the platform (wall 6 high, platform on a 7th layer): no path,
    bed never claimed. Ladder now reaches the platform layer. Rerun: climbed the ladder column to the bed (lying at
    y -52.5) PASS; came down the column in the morning, no fall damage PASS.
- 04:05 SQ-MCL-04 (four extra A2 boots, 03:29 to 04:00): `#forge:monsters` does NOT hold zombies in this pack
  (13 zombies alive while `@e[type=#forge:monsters]` found none), so the row's furthest-monster read and SQ-MCL-05's
  selectors matched nothing. New scratch tag `#zq:monsters` (tools/scratch_query/datapack/zq), batches switched.
  Also: on a fresh boot no monster spawned near a just-joined bot for about 2.5 min even at level 0 (Loading
  Protection window, cause not chased); MCL-04 now waits 160 s first. Mob cap level is read only at boot (toml set
  before the restart; shell edits removed from the rows). Result, same timing, superflat, one survival bot at 0 0,
  night, Normal: level 0: 13 zombies about 5 min after the join (and 63 hostiles in 120 s in the long-running A2
  session); forced level 2: zero monsters of any `#zq:monsters` type 8 min after the join (`ceperf mobcap`: 289
  chunks, level 2). SQ-MCL-04 FAIL, looks real: forced level 2 stops monster spawning for a lone player instead of
  capping it at 25. Suspects (perf.md 17): `MobCapSpawnAreaMixin.outsideRadius` (`level.players()` vs the bot), the
  local-cap path in `MobCapMixin`. Forced level 1 was tried with the bad tag only (no result). Toml back to -1.
- 04:45 B1 (`$S/b1`, r31b1, seed 8675309, named_mobs chance 1.0; boot Done 77.6 s): b1_gameplay without MCL-05
  (moved to the B1b boot: needs mobCapForceLevel 2 at boot). PASS 4, LOOK 11, FAIL 4 first pass. Batch fixes:
  `/cetime date add` takes singular units only (`40 days` refused with "Unknown unit days; use day, month, season or
  year"; TI-32 and the queue row both write `days`: doc or command, owner's choice), bare `/season` is not a command,
  `/time query daytime` reads 22 after the 2 s pace; a failed `data get` inside `execute as` prints nothing (NM-02).
  Reruns: SQ-TD-01 PASS, SQ-TD-02 PASS (Mid Spring, Late Spring, Early Summer day 1 6:00 am, Early Spring day 1,
  day 4, day 1), SQ-NM-02 PASS (no CustomName), SQ-TD-05 PASS on judgment (9 `[time] /cetime date` lines, one per
  change; the row counted only its own empty segment). B1 log kept as `perf/runs/r1031/b1_latest.log`.
- 05:00 B1b boot (same world, `-Dcreativeengineering.iwFastTick=false`, `mobCapForceLevel 2` at boot):
  SQ-MCL-05 PASS (forced level 2, normal world: 4 monsters at Y 82, 45, 83, 82 around qm at Y 70, all inside the
  32 band). So level 2 does spawn in a normal world: SQ-MCL-04's zero (superflat, bot at Y -60) stays FAIL, cause
  to find, superflat-specific so far.
  B1 LOOK rows judged offline (subagent; `perf/runs/r1031/judged_b1.md`): PASS NM-01 (2 cows, 8 sheep named; no pigs
  in range), CS-09 (sapling merged into Vinery's, 19 CD merges), IWF-03 (6 icicles, 8 snowy stone in thunder).
  OPEN TD-04 (`/wine time` prints bare lang keys on a dedicated server: client). Script mistakes, corrected in the
  batches from the subagent's lines and rerun on this boot: PF16-03 (console `sort=nearest` measured from 0 0, picked
  a spawn bee; bot sent 100 blocks away with simulation distance 4), UC-02 / -03 (growth chance 0.0005: 120 s gave
  0.3 expected growths; now 5 blocks, 240 s), IWF-02 / -02b (leaves were `persistent=true`, which IW never grows
  piles from; grass spread is vanilla), MG-18 / -19 (`place feature` and `if entity ...nbt` left no log line: data
  get probes added), firefly row renamed CQ-FF-01 (SQ-FF-01 is the food-families row) and the three Terralith biomes
  added. IWF-02 (fast path on) reruns on a restart without the flag before B1c.
- 05:20 Corrected B1 rows on the B1b boot: CQ-FF-01 PASS (firefly bushes: savanna 26, mangrove 22, shrubland 32,
  brushland 11, orchid swamp 14). SQ-PF16-03 PASS (bee moves with qa near, frozen with qa 128 above, moves again).
  SQ-MG-18 / -19 OPEN: `place feature neapolitan:banana_plant` at the bot placed no `banana_stalk` (bot in the air /
  on leaves after spreadplayers), so no banana source; gorillas and gliders held nothing. No chimpanzees: PASS part.
  Hand check in the client block (gorilla / glider near a banana plant). SQ-UC-02: 0 acan growths from 5 blocks in
  240 s (about 2.3 expected, 10 % chance of 0); SQ-UC-03: 1 prismarine wall fan, 0 elder, 0 acan (cold ocean): weak
  PASS. Control row SQ-UC-02c added (5 acan beside 5 vanilla brain coral blocks, 300 s, fast path on).
  SQ-IWF-02b (fast path off): 14 leaf piles, 0 grass / tall grass, all 72 oak leaves gone (decayed despite the log
  column; compare with IWF-02). B1b log kept as `perf/runs/r1031/b1b_latest.log`.

**09:25.** B1 restart (iwFastTick on, mobCapForceLevel -1) finished its rows at 05:04, but the background task never
exited: the server was booted inside the `( ... ) | grep` pipe, so the server JVM kept the pipe open. About 4 h idle
until noticed. Decision: from here, boot outside any pipe and pipe only the qrun output.
- SQ-IWF-02 PASS: 15 leaf piles, 0 grass, leaves all gone, against 14 / 0 / gone for IWF-02b (fast path off).
  Fast path and normal path give the same growth.
- SQ-UC-02c: acan 0 growths, brain coral (vanilla control) 0 growths in 300 s. The setup grows nothing,
  even for vanilla coral, so the 0 on SQ-UC-02 is not evidence against acan. SQ-UC-02 moves to the client block
  (hand check: bonemeal acan in a warm ocean).

**09:40. B1c done** (block_growths = false in the scratch copy only).
- SQ-IWF-04: grass_block spread to 289 (vanilla spread, expected), 0 leaf piles. But this row's leaf block is the old
  setup that grew nothing even with growths on, so the 0 decides nothing. Added SQ-IWF-04b (b1_iwf.qb): the corrected
  block at y 190, with persistent leaves and a log column so decay cannot drop piles.
- SQ-IWF-04b PASS: grass 0, tall grass 0, leaf piles 0. Caveat: IWF-02 (15 piles) used non-persistent leaves. If IW
  growth skips persistent leaves, the 0 here would hold even with growths on. Good enough for a config-off check;
  noted for the owner.
- B1 server stopped; log kept as perf/runs/r1031/b1c_latest.log. Next: worldgen block W1a.

**09:55. W1a.** SQ-EF-01 PASS (overrides village_plains=-1,-1 and village_savanna=-1,-1,-1): 4 plains lines show water
and air limit -1 and mound limit 24 (the size's limit kept); 7 savanna lines show all three limits -1 and
"-> kept", including one with a 742-water footprint and 12 mound columns. No savanna village pruned or cancelled.
Log kept as perf/runs/r1031/w1a_latest.log.

**10:40. W1b judged** (seed 8675309; runner said FAIL 7 of 9, most are batch faults; batch fixed for next time).
- SQ-EF-02 LOOK, SQ-EF-03 LOOK: screenshots / positions for the client block.
- SQ-EF-11 PASS on content, FAIL on timeout only: the radius-96 census takes 1457 s on one locator thread; the row
  waited 900 s (now 2400) and the census kept running under EF-03 to EF-06. Census: EARTH_FILL 302 and PRIORITY 420
  rejects shown; 267 "too wet" lines, smallest wet footprint 16 (>= 9); 2 "under 9 water -> kept", none cancelled.
  **Finding:** 11 false negatives, all biome predictions for underground structures (terralith old_refinery 3,
  mining_outpost 5, mineshaft 1, ancient_city 2), the deep-biome tie class (memory biome-lookup-ties).
  Census copy: perf/runs/r1031/w1b_locate_census.json.
- SQ-EF-04 PASS: the reject regex caught terralith:underground/witch_hut (a different, underground structure, kept).
  No earth-fill line for minecraft:swamp_hut, betterwitchhuts:witch_hut or a mangrove tower; both huts locate
  (279 and 93 blocks). Regex anchored to the three ids.
- SQ-EF-05 FAIL, same cause as SQ-TER-01 (ancient_crypt "ignored" at boot); "7 sunk starts" present. Crypt and
  ocean_ruins placed; ocean_fortress printed no "Generated" line. Seabed look stays for the client block.
- SQ-EF-06 LOOK, below the bar: the highlands square had no undead castle; across the world 3 castles, 1 kept
  (mound 275), 2 CANCELLED for over-air-after-beard 708 and 562 (limit 320). Row wants at least half kept: owner call
  whether the 320 air limit is too tight for huge castles.
- SQ-EF-08 PASS after a rerun: the row's coordinates (x -80..-40, z -16..100) are ocean and beach on this seed, so
  all four "no anchor fits" were correct. Rerun on plains near 1762 -3249: farmers_camp, house_camp, ruin_scatter
  all build; at the beach 1792 -2208 "no anchor fits". Natural lines carry "short:" (75). Restart half: no
  MetaStructure error on boot; locate finds a natural farmers_camp (2896 -3264). The placed one is not locatable:
  vanilla /place structure records no start, so the row's "finds the built one" was wrong. A farmers_camp slot at
  chunk [109, -204] tried about 20 anchors, all vetoed by an overlapping village_plains (correct). Batch rewritten.
- SQ-EF-09 PASS from the whole-world log: the river square was already generated (Chunky 0 s, no lines in the row
  window). 44 "pruned N of M pieces (wet only)" lines with footprints, 0 villages among them (villages: 40
  "(village)" prunes).
- SQ-EF-10 OPEN: it placed the camps on the river itself (no anchor fits, correct). No natural meta with an anchor
  pruned at a shore found in this world; needs a hand-made shore case in the client block.
- **Finding:** "Empty or non-existent pool: minecraft:cook_additions_1..4_pool" (51/51/44/51) and
  minecraft:cook_end_1_pool (65), angler_additions_3_pool (7): Farmer's Structures cook (and angler) pieces point at
  pools under the minecraft namespace that do not exist, so those buildings generate without their additions. Not
  in farmers_structures.md. Also seen: kaisyn meadow_swiss villagers pool (17), plains unemployed villagers (18).
- Logs: perf/runs/r1031/w1b_latest.log (first boot), restart log in S/w1b. Next: W1c (beardFootprint true).

**10:55. W1c.** SQ-TER-05 LOOK, calibration table (beardFootprint = true, windswept hills square, unique real starts;
"newly over" = mound columns over the limit only because of the columns under built floors):
| family | starts | cancelled | newly over by floors | sim (tools/earth_fill_sim.sh) |
|---|---|---|---|---|
| outposts / pillager towers | 51 | 6 | 3 (6%) | 23% |
| villages | 21 | 17 | 1 (5%) | 2 to 18% |
| other | 669 | 53 | 24 (4%) | - |
88 "under built floors" lines. Outposts come in well under the sim's 23%; villages inside its range. 17 of 21
villages cancelled in this mountain square (mostly over the mound limit even without the floor columns). The 3
cancelled + 3 kept look goes to the client block. Calibration only; the switch lived only in the W1c scratch copy.

**11:00. Scope cut (user, relayed by the main session).** Client block (c1_client.qb, 32 rows) and long operations
L1 to L4 (SQ-WGP-07, the perf suite, the rpack bench, CQ-WG30-A04 DH flight) are DEFERRED, not run in r1031. Rows
that were moved to the client block for a hand check (PF16-02, TD-04, MG-18/19, PS-05 JEI, RW-24, POT-06, MB-05,
UC-02, EF-02/03/05/06 looks, EF-10 shore case, TER-05 look) stay OPEN and deferred with it. Remaining in scope:
W2 (running), W3a, W3b, then the perf report and the write-back.

**11:10. Second scope cut (user, relayed by the main session).** The rest of the worldgen block is DEFERRED as well.
W2 stopped at a row boundary during SQ-SB-04 (that row is discarded and counts as not run); the W2 server was stopped
cleanly and its log kept as perf/runs/r1031/w2_latest.log. Ran in W2: SQ-SB-01b, META31-01 to -09 (10 rows),
SQ-SB-01, -02, -03 (13 rows; judged offline in perf/runs/r1031/judged_w2.md). Deferred: SQ-SB-04 to -08, SQ-SB-01e
(W2, 6 rows) and all of W3a / W3b (SQ-TER-02, -03, -04, -06, -06b, SQ-SVA-03, -04; 7 rows). Wrap-up now: perf report
(no --prune), write-back into scratch_queue.md, run log line, writeups, README status words.

## 8. Final verdicts (r1031, 2026-10-31 11:40)

Merged: runner results.md, then perf/runs/r1031/judged_a1.md / judged_b1.md / judged_w2.md, then the decisions in section 7.
OPEN = needs the deferred client block or the owner's call. BATCH-FAULT = the row's check was wrong; the fixed row is in its batch or judged file.

Counts: BATCH-FAULT 5, FAIL 10, OPEN 18, PASS 134 (total 167 row results, reruns and follow-up rows included)

| Verdict | Rows | Note |
|---|---|---|
| FAIL | SQ-AC-02 | dragon set 33 armor not 36: dragonloot-common.toml 7/10/9/7 overrides the KubeJS tune |
| FAIL | SQ-CM-32 | oceansdelight recipes.js removes tentacle recipes before the calamari replaceInputs |
| FAIL | SQ-EF-05 | same cause as SQ-TER-01 |
| FAIL | SQ-MCL-04 | forced mob-cap level 2 spawns 0 monsters on superflat in 8 min (normal world OK, MCL-05) |
| FAIL | SQ-META31-04 | meteorite_desert_pyramid placed with 0/1 desert_pyramid (short): crater, no pyramid; MetaStructure keepShortPa |
| FAIL | SQ-R31-BOOT | NCH squid_tentacles error = SQ-CM-32 |
| FAIL | SQ-RW-23 | smokable plank shape tags include Botania, driftwood, crimson, warped |
| FAIL | SQ-RW-23b | evidence for SQ-RW-23 |
| FAIL | SQ-SB-03 | judged_w2.md: birch tower located at [-2016, 6672]; qa at -1985.5 100 6672.5, night: pillager, vindicator, evo |
| FAIL | SQ-TER-01 | philipsruins:ancient_crypt refused by terrain adaptation (terrain_matching start piece) |
| OPEN | SQ-EF-02 | client look deferred |
| OPEN | SQ-EF-03 | client look deferred |
| OPEN | SQ-EF-06 | 1 of 3 castles kept; 2 cancelled over-air 708/562 vs limit 320: owner call |
| OPEN | SQ-EF-10 | needs a hand-made shore case (deferred) |
| OPEN | SQ-MB-05 | judged_a1.md: `#minecraft:mineable/pickaxe` and `#minecraft:needs_diamond_tool` both Test passed. |
| OPEN | SQ-MG-18 | judged_b1.md: Kill -> `No entity was found` (no chimpanzees: that half passes). 3 gorillas summoned, time set. |
| OPEN | SQ-MG-19 | judged_b1.md: 3 sugar gliders summoned; both `execute at qa if entity` checks sent, no output logged (same ano |
| OPEN | SQ-PF16-02 | client look deferred |
| OPEN | SQ-POT-06 | judged_a1.md: Both houses generated; `count beautify:botanist_workbench: 3; on: minecraft:air 3`. Setblock ben |
| OPEN | SQ-PS-05 | second results at 0.5 need JEI (deferred) |
| OPEN | SQ-PS-05b | vacuum PASS; 0.5 second results need JEI (deferred) |
| OPEN | SQ-RW-24 | judged_a1.md: `creativeengineering:createfilling/treated_wood_planks`, `createfilling/treated_wood_smoked_plan |
| OPEN | SQ-TD-04 | judged_b1.md: `/cetime date add 1 month` -> `(+8.0 days; wine clock follows)`; `-1 month` -> `(-8.0 days; wine |
| OPEN | SQ-UC-02 | vanilla coral control also 0 (UC-02c): setup grows nothing; hand check deferred |
| OPEN | SQ-META31-02 | judged_w2.md: All 4 located (count 4 passed). cloud_cluster [-784, 1104]: 2 sky temples. balloon_fleet [-480,  |
| OPEN | SQ-META31-03 | judged_w2.md: witch_cart_caravan [-304, 3472]: 3 wagons, `0/1 [caravan_witch_huts]` (hut optional, min 0). pil |
| OPEN | SQ-META31-05 | judged_w2.md: desert_temple_ruins [-4496, -1424]: YUNG desert temple + 3 ruins (2 Philip's badlands_structures |
| OPEN | SQ-META31-06 | judged_w2.md: meteor_shower [-9552, 8752]: 2 meteorites (anchor + 1). struck_monument [-4304, 3760]: monument  |
| BATCH-FAULT | SQ-IWF-04 | old leaf block grows nothing; decided by IWF-04b |
| BATCH-FAULT | SQ-META31-07 | judged_w2.md: witch_coven [-2000, 1120]: YUNG witch circle, swamp hut at -2013 78 1140, 1 wagon. `Killed 3 ent |
| BATCH-FAULT | SQ-R31-KJS | NCH errors log to latest.log, not server.log; needs bootreject [NCH] |
| BATCH-FAULT | SQ-SB-02 | judged_w2.md: Row window: `[ceplace] done: 3 of 3 placed ... 0 failed` (catapult, Mysticrift oak tower, Blocky |
| BATCH-FAULT | SQ-UC-02c | control: brain coral 0 too |
| PASS | CQ-FF-01, SQ-A2-BOOT, SQ-AC-01, SQ-AC-03, SQ-B1-BOOT, SQ-BJ-01, SQ-BJ-01b, SQ-BJ-02, SQ-BJ-02b, SQ-BJ-03, SQ-CM-10, SQ-CM-20, SQ-CM-21, SQ-CM-22, SQ-CM-23, SQ-CM-24, SQ-CM-30, SQ-CM-31, SQ-CP-01, SQ-CS-01, SQ-CS-02, SQ-CS-03, SQ-CS-04, SQ-CS-05, SQ-CS-06, SQ-CS-08, SQ-CS-09, SQ-DD-08, SQ-DM-01, SQ-DM-02, SQ-DM-03, SQ-DM-03b, SQ-EF-01, SQ-EF-04, SQ-EF-08, SQ-EF-09, SQ-EF-11, SQ-EFX-01, SQ-EFX-02, SQ-EFX-03, SQ-EFX-04, SQ-EFX-05, SQ-EFX-06, SQ-EFX-07, SQ-EFX-08, SQ-EFX-08b, SQ-EFX-09, SQ-EFX-10, SQ-EFX-11, SQ-EN-21, SQ-EN-21b, SQ-FF-01, SQ-GF31-01, SQ-GF31-02, SQ-GF31-03, SQ-GF31-04, SQ-IWF-01, SQ-IWF-02, SQ-IWF-02b, SQ-IWF-03, SQ-IWF-04b, SQ-KT-21, SQ-KT-21b, SQ-LB-01, SQ-LB-01b, SQ-LB-02, SQ-MB-01, SQ-MB-02, SQ-MB-03, SQ-MB-04, SQ-MB-06, SQ-MB-07, SQ-MCL-01, SQ-MCL-02, SQ-MCL-03, SQ-MCL-05, SQ-META31-01, SQ-META31-04b, SQ-META31-08, SQ-META31-09, SQ-MG-17, SQ-NM-01, SQ-NM-02, SQ-NM-03, SQ-PF16-01, SQ-PF16-03, SQ-POT-06b, SQ-R31-BOT, SQ-RE-28, SQ-RW-25, SQ-RW-26, SQ-RW-27, SQ-RW-27b, SQ-RW-28, SQ-SB-01, SQ-SB-01b, SQ-SG-01, SQ-SG-02, SQ-SG-03, SQ-SPC-01, SQ-SPC-02, SQ-STL-01, SQ-STL-02, SQ-SVA-01, SQ-SVA-02, SQ-TD-01, SQ-TD-02, SQ-TD-03, SQ-TD-05, SQ-TER-05, SQ-UC-01, SQ-UC-03, SQ-VG-00, SQ-VG-01, SQ-VG-02, SQ-VG-03, SQ-VL-00, SQ-VL-00b, SQ-VL-01, SQ-VL-02, SQ-VS-01, SQ-VS-02, SQ-VS-03, SQ-VS-04, SQ-VS-05, SQ-VS-06, SQ-VS-07, SQ-VS-08, SQ-WC-01, SQ-WC-01b, SQ-WC-02, SQ-WC-03, SQ-WC-05, SQ-WT-01 | |

**12:10. Wrap-up done.** Perf report written (no prune). Results moved into the feature writeups, README status
words (BROKEN where a FAIL applies) and TODO.md (8 new Bug rows). Open point for the owner: SQ-RW-23 recorded as
Botania + driftwood wrongly smokable; whether crimson / warped should smoke is the owner's call (RW-23 expects crimson).
All scratch servers stopped; scratch worlds stay in the session scratchpad until it is cleared.
