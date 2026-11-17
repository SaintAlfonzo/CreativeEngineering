# Scratch testing expansion (inventory, hurdles, proposals)

Survey 2026-11-15. Nothing built from this yet; every proposal below waits for the owner's pick. Sources: skill
`scratch-test` (SKILL.md, gotchas.md), [testing/scratch_queue.md](testing/scratch_queue.md), run docs 2026-10-31 to
2026-11-15, `tools/scratch_query/`, `tools/perf/`, `development/creativeDevtools/members/`,
[client_capture.md](client_capture.md).

## 0. Status

Owner decision 2026-11-15: summary items 1 to 5 are PLANNED TO BE BUILT. Items 6 (GameTests, machine-readable rows,
resumable run driver) stay proposals.

| Item | Scope | Proposals | State |
|---|---|---|---|
| 1 | Quick tooling: batch lint, dry run, client crash watch, per-row verdict JSON | TX-40, TX-41, TX-06, TX-45 | BUILT 2026-11-15, offline-tested: `qrun.py lint` / `dry` (`qlint.py`), crash watch with `--relaunch` (`qwatch.py`), `RES/verdicts/ID.json` + `qrun.py judge` / `table` (`qverdict.py`); lint over the 463 past batches: 94 E, 912 W; live checks SQ-TXQ-01 to -03 queued; usage in skill `scratch-test` B2 to B4 |
| 2 | Client capture and crop pipeline, plus text readback steps | TX-01, TX-03 | BUILT 2026-11-15, not run in game: `tools/client_capture/` (run_captures.py, crop.py, chan.py), driver read steps, first spec set in `testing/capture_specs/`; offline tests pass; in-game checks CQ-TXC-01 to -07 queued ([client_capture.md](client_capture.md) 0, 6) |
| 3 | Java test command in Creative Devtools, tick warp, more bot verbs | TX-20, TX-21, TX-22 | BUILT 2026-11-15 (Devtools and perfBot build/libs 15:38, not installed), harness PASS 27, live rows SQ-TX-01 to -16 queued; verbs in [creative_devtools.md](creative_devtools.md) "/cdevtest and the perfBot test verbs" |
| 4 | In-process client driving: tick-exact movement, widget actions | TX-02, TX-04, TX-05 | BUILT 2026-11-15, not run in game: driver file channel + `run_captures.py serve`, `qrun.py client` driver transport and `do` directive, walk / jump / sneak / fly with per-tick READ lines, widget actions; protocol in [client_capture.md](client_capture.md) 2.11 (item 5 builds on it); CQ-TXC-08 to -10 queued |
| 5 | Paired client + own server batches, mismatch matrix | TX-30, TX-31 | BUILT 2026-11-15, offline-tested with a fake server and fake driver client: `qrun.py pair` (`srv>` / `cli>` lines, `srv:` / `cli:` checks) and `qrun.py matrix SPEC` (`qpair.py`); live rows CQ-TXP-01, -02 queued. Open: the D2 carve-out text for the skill's one-JVM rule and the memory note was refused by the session's permission check and waits for the owner; a stale `GAME/cdev_channel/pid` / `state.json` from an earlier launch can misread a matrix outcome (fix written, refused by the same check); client-side jar drops and client config edits need `run_captures.py serve --drop / --config` (not built) |

**Judgement rule for item 2 (owner, 2026-11-15):** the pipeline captures and crops everything, but no visual verdict is
made by an agent or by the tool. Every capture that needs a visual judgement is written to a temporary judgement folder
(`test_captures/judgement/<run>/`, git-ignored, wiped after the owner has judged it) with its manifest entry (test ID,
what it should show, crop, step log) for a human to review. Text readback (TX-03) may still produce PASS / FAIL by
regex; only pixels go to the folder. Verdicts the owner gives are written back into the queue and writeups as usual.
This replaces "a later session reads the PNGs" in [client_capture.md](client_capture.md) section 0.

**Decisions 2026-11-15 (owner):**
- D1 closed: items 1 to 5 planned, order as in section 4.
- D2 closed: a paired launch (client + own scratch server) is valid when one driver starts and stops both for a single
  scratch test. Under the one-JVM rule that driver is the only game-JVM owner; the skill and the memory note
  `one-game-instance-at-a-time` get the carve-out when TX-30 is built.
- D4 closed: every probe verb that is reusable moves into the Java `/cdevtest` set in Creative Devtools; the rest stay
  one-off KubeJS probes. Item 3 therefore includes migrating the reusable verbs of `zq_probe.js` and the per-feature
  probes, not only the new ones.
- D3 closed: `xvfb` installed by the owner. Xvfb runs are an EXPERIMENTAL option (`--display xvfb|real`, TX-07), used
  only for tests that cannot be run otherwise (unattended or overnight client blocks), never the default; the real
  window stays the default and is required for colour rows. LOW priority: built after items 1 to 5, and first
  validated by running CC-02 on both displays (llvmpipe may fail for Immersive Portals, Distant Horizons, shaders).

## 1. Inventory

### 1.1 Server side

| Tool | What it does | Limits |
|---|---|---|
| `tools/creative_labels_test/scratch_server.sh` | setup / boot / cmd (stdin fifo) / stop of a throwaway dedicated server | fifo is write-only: command output comes back only through `latest.log` |
| `tools/scratch_query/prep_srv.sh` | builds a scratch srv: hard-linked jars, dev-jar swaps, perfBot, `/zq` probe, group A superflat / B seed 8675309, props | peaceful by default (animals and NPCs discarded unless set) |
| `tools/scratch_query/qrun.py` | batch DSL (`.qb`): console commands, `waitlog` / `expect` / `reject` / `count`, boot and KubeJS-log checks, `capture` vars, `shell`, `repeat`; client mode (driver channel or xdotool); since 2026-11-15 `lint` / `dry` (`qlint.py`), crash watch (`qwatch.py`), per-row verdict files with `judge` / `table` (`qverdict.py`), paired `pair` / `matrix` runs (`qpair.py`) | judges only log lines matched per line by regex; one row = one log segment; lint is static (it cannot see probe errors, wrong expected text, framing) |
| `zq_probe.js` (`/zq`) | general query command: recipes, uses/makes, food, items, blocks, potions, seasons, block counts and ages, structure ground checks | KubeJS/Rhino: Java `Error` crashes the server, chunk loads can deadlock. Reusable verbs moved to `/cdevtest` 2026-11-15 (D4); season, floating, under, nbt stay |
| ~100 per-feature probes (`*_probe.js`, `pwg_p*_probe.js` up to 63 KB, `hh_*`, `zpth`, `ztp`, ...) | feature-specific setup and readback commands | written once per feature; little reuse; each needs a live first call. Generic verbs (state, inv, tank, items, use, break, kill, loot, sneak, eat, randomtick, ...) moved to `/cdevtest` and `/pbot` 2026-11-15; each file's header has a `MIGRATED` line |
| Judges and generators (`efb_judge.py`, `cge_judge.py`, `wo_judge.py`, `hh_queue_to_qb.py`, `pth_gen_a*.py`, `dep_gen_p7.py`, `melt_gen_p3.py`) | offline verdicts from logs; row-to-batch generation for single features | per feature, not general |
| Fixtures (`nsm_fixture.js`, `cdns_fixture.js` + manifest, `oldsave_swap.sh`) | old-save and namespace-move worlds | partial (no paintings or banners in `nsm_fixture.js`) |
| Offline readers (`region_blocks.py`, `region_ids.py`, `tpf_chunkstatus.py`, `tools/seabed_heights.py` nbt) | read region files after pregen | ad hoc copies of an nbt reader |
| perfBot (`cdevperfbot`, Creative Devtools member) | real `ServerPlayer` bots: `spawn`, `hold`, `use`, `path`, `census`, `mark`, `remove`; since 2026-11-15 also `attack`, `interact`, `punch`, `break`, `useitem` / `eat` (ticked), `hunger`, `sneak`, `look`, `lookat`, `moveto` (physics), `inv`, `vulnerable`, `pickup` | invulnerable by default; Loading Protection exemption since 2026-11-15 (one tick after spawn); no menu clicks or crafting grid |
| Forge `FakePlayerFactory` from KubeJS | `useItemOn`, `destroyBlock`, menus | does not tick: no item-use duration (eating needs `finishUsingItem` by hand); cached per profile (wrong level, SQ-SPD-08). `/cdevtest` uses one fake player per dimension |
| `/cdevtest` (Creative Devtools, 2026-11-15) | Java probe verbs with `[cdevtest] key=value` lines: block, BE, inventory, tank, energy, count, ages, place, use / break / kill / loot, items, entities, player, chunk, recipe and tag data, `randomtick`, `blocktick`, `tickwarp`, `watch` | not run live yet (SQ-TX); no worldgen judges (`zq under` stays) |
| perf: `perf_session.sh`, `perf_suite.py` (resume, crash triage, RCON), `wgp_run.sh`, `entity_census.py`, `PerfParse.java` | JFR per row, MSPT, pregen timings, suite with retries | good coverage; the model for a resumable runner |
| Headless | `javac` harness on creativeCommon classes, headless Rhino harness (memories) | logic only; GameTests stale since the carve |
| Bookkeeping | `unrun_rows.py`, `game_jvms.sh`, `wait_boot.sh`, `stop_wait.sh`, RUNNER.md claim, run docs | queue is 7355 lines of markdown tables; RUNNER.md and run docs are hand-written |

### 1.2 Client side

| Tool | What it does | Limits |
|---|---|---|
| `tools/perf/eval/eval_client.py` | throwaway client: hard-linked mods, copied config, JFR, `server=` join, `ownsrv=` own scratch server | uses the instance's `mods/` (build/libs-only jars are absent); real window on the desktop |
| `tools/scratch_query/sp_world.py` | scratch server world into client saves with cheats on | no cheats-off variant (CQ-TPF-46 needs one) |
| `qrun.py client` | types chat commands, `key`, `click`, `shot` with xdotool / ImageMagick `import`; since 2026-11-15 drives the capture driver over its file channel when `GAME/cdev_channel/` exists (`do` directive, no xdotool) | xdotool mode: dropped characters (40 ms per key needed), focus steals, a `key` tap is shorter than a tick, no readback except `latest.log`; driver mode not yet run in game |
| `cli_do.py`, `pth_client_drive.py`, `client_drive.sh`, `sp_drive.js` | hand-row helpers: walk holds, mouse moves, title-screen clicks by fixed coordinates | coordinates tied to the 854x480 window; timing by sleeps |
| **Client capture driver `cdevcapture`** (`development/creativeDevtools/members/capture`, nested in `creativedevtools-1.0.0.jar`) | in-process plan runner: capture steps (`cmd`, `use`, `attack`, `look`, `hover`, `key`, screens, JEI, `tooltip`, `capture`, `wait`), since 2026-11-15 read steps (`READ` lines), tick-exact movement, widget actions and a served channel; runner `tools/client_capture/run_captures.py`, `crop.py` and the first specs built 2026-11-15 | never run in game (CQ-TXC rows); all pixels go to `test_captures/judgement/` for the owner |
| `tools/perf/rpack/` | resource pack bench (client) | perf only |

## 2. What cannot run automatically today

Counts from the queue (2026-11-15): 2607 `SQ-` rows, 511 `CQ-` rows, 160 rows with LOOK, 444 rows never run
(`unrun_rows.py`; most are in client or owner-hand sections).

| # | Test kind | Why it is stuck | Evidence |
|---|---|---|---|
| H1 | **Visual checks** (textures, tooltips, JEI pages, WTHIT lines, GUIs, creative tabs, HUD) | screenshots by `import`, judged by a person or an agent reading PNGs; no GUI bounds; framing errors | CQ-PWG-24 shot taken after a teleport (BATCH-FAULT), CQ-PWG-28 tour aimed at bare ground |
| H2 | **Player movement and physics** (walk speed through crops/ropes, elytra, sneak, jump, portals) | xdotool holds are wall-clock, not ticks; a `key space` is shorter than a tick; speeds measured by hand | CQ-PTH-01, -03 "PASS (partial)"; SQ-PG-01b player stuck at y -61 found only by hand |
| H3 | **Editor GUIs and multi-click flows** (circuit design table, sticky note writing, anvil typing, chat focus bugs) | no API to click a slot or type into a widget except screen coordinates | CQ-PWG-22/-24 left to the owner; CQ-TPF-31 to -91 never run |
| H4 | **Client-only data** (names from client lang scripts, tooltips, stack sizes shown, effects text, AppleSkin) | dedicated-server dumps cannot see them | SQ-OB-02 reclassed as client; gotchas "names, stacks, effects, gear, food ... need the client" |
| H5 | **Player-gated server mechanics** (eating over time, damage to a player, sleeping, crows stealing from a hotbar, Loading Protection) | FakePlayer does not tick; perfBot is invulnerable and has few verbs | SQ-HH-424/-425/-453; SQ-ROLL-11 INCONCLUSIVE (food values off by 4) |
| H6 | **Client + server together** (config sync, registry mismatch refusal, TradeSync, multiplayer location broadcaster) | one-game-JVM rule; `ownsrv=` exists but no batch form drives both sides | Power Grid client joining a server without it crashed instead of refusing (found by chance in phase 10) |
| H7 | **Time-scaled behaviour** (crop growth, random ticks, decay, melt, seasons) | 1.19.2 has no `/tick`; random ticks need a player near; counts too small for statistics | SQ-FCK-08b INCONCLUSIVE (4 vs 6), SQ-HH-1534 INCONCLUSIVE, rerun_a2 (no random ticks after restart) |
| H8 | **Rare worldgen** (rare structures, wild crops, nests) | `/locate` of a rare meta runs 30+ min and cannot be cancelled; pregen + region reads per feature | SQ-HOP-06 0 hops over 740 chunks; SQ-RD-26 0 bell peppers |
| H9 | **Old-save / migration** | fixtures hand-written per feature, incomplete | SQ-NSM-04 not run (no painting or banner step) |
| H10 | **Live config reload** | config watcher parked on scratch servers | SQ-MELT-P1b clause not judgeable |
| H11 | **Client crashes** | qrun client mode does not watch for a dead JVM or `hs_err` (2026-11-15: crash watch TX-06 built, live check SQ-TXQ-02 queued) | SQ-PG-01c SIGSEGV found by watching |
| H12 | **Design-feel questions** ("is the flicker acceptable") | not automatable; owner judgment | CQ-RP-21, SQ-QN-02 |

Cross-cutting costs (not a test kind, but where the time goes):

- **Batch faults.** r1113: 25 BATCH-FAULT of 124 judged rows; r1111 SQ-HH: 67 BATCH-FAULT against 233 PASS. Causes
  repeat across runs: `grep -c` exit 1, `--from` dropping captures, stale coordinates and markers, row text out of
  date with the behaviour, `waitlog` matching a previous row's reply.
- **Probe fragility.** Rhino `Error`s crash the server, chunk-load deadlocks, ambiguous overloads, private JDK
  collections; each new probe needs a live trial call before its batch can rely on it.
- **INCONCLUSIVE from missing readback.** Probes report an action but not the end state (SQ-SALT2-06).
- **Orchestration.** A runner subagent died on a network error and its server sat idle for 4 hours (r1114 phase 3);
  RUNNER.md, run docs and verdict tables are hand-maintained.
- **Serial boots.** One JVM at a time; every fresh world or config change is a boot.

## 3. Proposals

Effort: S (hours), M (a session), L (several sessions). Ranked by rows unblocked per effort inside each group.

### 3.1 Client automation

| ID | Proposal | Unblocks | Effort |
|---|---|---|---|
| TX-01 | **Finish client capture**: write `run_captures.py` (plan from spec, launch via `eval_client.py`, relaunch on crash), `crop.py`, a first spec set from the CQ rows; run CC-01 to CC-12; fix the stale status table in [client_capture.md](client_capture.md) | H1, H4, most LOOK rows; the driver already exists | M |
| TX-02 | **`qrun.py client` on the capture driver** instead of xdotool: send steps to the driver (a file or socket the driver polls each tick), so `> cmd`, `key`, `click`, `shot` run in-process and tick-exact, with no focus or typing loss | H2, H3; removes the xdotool gotchas | M |
| TX-03 | **Client readback steps** in the driver: `assert_screen <class>`, `slot_item <i>`, `tooltip_text <item>` (lines as text, not pixels), `player_pos`, `player_motion`, `held`, `effects`, logged as `[cdevcapture] READ ...` so qrun regexes judge them | turns many LOOKs into PASS/FAIL (tooltips, names, GUI contents, walk speed) | M |
| TX-04 | **Tick-exact movement steps**: `walk <ticks> <keys>`, `jump`, `sneak <ticks>`, `fly`, `look yaw pitch`, with position sampled per tick into the log | H2: CQ-PTH speeds, elytra, ropes, portal exits | S after TX-02 |
| TX-05 | **Widget actions**: `click_slot <i>`, `click_widget <label or index>`, `type_into <field>`, `scroll` resolved from the open screen's widget list, not screen pixels | H3: editor GUIs, anvils, sticky notes, JEI search, creative tabs | M |
| TX-06 | **Client crash watch** in qrun / the runner: dead pid, `hs_err_pid*.log`, `crash-reports/` = row CRASH, copy evidence, relaunch | H11 | S |
| TX-07 | **(experimental, low priority) Xvfb display** (client_capture D2): runs unattended off the owner's desktop; software rendering is slower and colours may differ, so keep the real window for colour rows | runs at night without stealing the desktop | S (owner installs `xvfb`) |
| TX-08 | `sp_world.py --no-cheats` and a client config profile switch (`--fresh-config`) | CQ-TPF-46, pack-default checks | S |
| TX-09 | **Client data dump** through the capture driver: names, tooltips (shift and plain), stack sizes, effect text for every item into `kubejs/exported/`, diffed between runs | H4 at scale; feeds the names/tooltip catalogs | M |

### 3.2 Server automation

| ID | Proposal | Unblocks | Effort |
|---|---|---|---|
| TX-20 | **Java test command in Creative Devtools** (`/cdevtest`): the common probe verbs (place, use item on block as a player, read block state/BE NBT/inventory, count blocks, entity query, run N random ticks on a box, force a block tick) in Java, structured `[cdevtest] key=value` output; KubeJS probes stay for one-offs | probe fragility (no Rhino crashes or deadlocks), INCONCLUSIVE from missing readback | M |
| TX-21 | **Tick warp**: `/cdevtest tickwarp N` runs N server ticks as fast as the CPU allows (vanilla 1.19.2 has no `/tick`), plus `randomtick <box> <n>` to apply random ticks without a player | H7: growth, melt, decay, salt; turns small counts into statistics | M |
| TX-22 | **More bot verbs**: `attack`, `eat` (ticking use duration), `sneak`, `look`, `move to`, `inv set/get`, `vulnerable on/off`, `pickup off`; bots exempt from Loading Protection | H5 | S to M |
| TX-23 | **Forge GameTests** for own jars: structure-template tests registered in a dev-only member, run by `runGameTestServer` (no window, exit code = result); good for machines, block interactions, recipes in a world | regression tests that need no queue row or agent; runs in CI-like fashion on every build | M to start, then S per test |
| TX-24 | **Seed statistics rows**: a `stat` directive or judge that runs a check over K seeds or K boxes and reports rate with a confidence bound against the expected rate | H8, SQ-FCK-08b, SQ-HOP-06 style "0 in N" questions | S |
| TX-25 | **Locate with a budget**: a devtools locate that stops after N chunks or T seconds and reports what it checked (no 30-min uncancellable locate) | H8; stall-watch waits | S to M |
| TX-26 | **Shared region reader** `tools/scratch_query/nbtlib.py` (int arrays included) used by every offline reader | H8 offline checks | S |
| TX-27 | **Fixture library**: one fixture script per content kind (chests, offhand, curios, ground items, paintings, banners, BEs, entities with NBT) composed per old-save test, plus stored fixture worlds per pack version | H9 | M |
| TX-28 | **Config live-reload switch** for scratch servers (keep the watcher on when a row asks) | H10 | S |

### 3.3 Client and server together

| ID | Proposal | Unblocks | Effort |
|---|---|---|---|
| TX-30 | **Paired batch mode** in qrun: one batch with `srv>` and `cli>` lines (server console + capture driver), using `eval_client.py ownsrv=`; the pair counts as one "game run" for the one-JVM rule (needs the owner's OK to read the rule that way) | H6: sync, refusal screens, multiplayer features | M |
| TX-31 | **Registry/config mismatch matrix**: scripted join attempts with one jar missing or one config differing on each side, judged on the refusal text vs crash | the Power Grid join crash class of bug | S after TX-30 |

### 3.4 Queue, batches, runs

| ID | Proposal | Unblocks | Effort |
|---|---|---|---|
| TX-40 | **Batch lint** (`qrun.py lint BATCH`): `grep -c` without `; true`, uses of captures set in skipped rows, `waitlog` patterns that also match the previous row's reply, unknown markers, coordinates outside forceloaded boxes | most BATCH-FAULTs before boot | S |
| TX-41 | **`qrun.py dry`**: parse, substitute vars, print every command and check without a server | batch review by a second agent | S |
| TX-42 | **Machine-readable rows**: a fenced `qb` block under each queue row (or one `.qb` per section next to the queue) so batches are extracted, not rewritten each run; generalise `hh_queue_to_qb.py` | batch writing time, stale batches | M |
| TX-43 | **Per-row isolation**: a `plot` directive that gives each row its own forceloaded area at a fixed offset and clears it, so rows do not share markers or coordinates | BATCH-FAULTs from shared world state, reruns on a fresh world | S |
| TX-44 | **Resumable run driver** on the `perf_suite.py` pattern: state file, auto RUNNER.md updates, resume after a dead runner, server stopped when the runner dies | orchestration losses (r1114 phase 3) | M |
| TX-45 | **Verdict file** written by qrun (JSON per row with checks, LOOK text, shots), and a judge step that only fills LOOKs; run doc tables generated from it | write-back time | S |
| TX-46 | **Row staleness check**: flag rows whose writeup section or source files changed after the row was written | "row text out of date" findings | S |

### 3.5 Not worth automating

- H12 design-feel rows stay owner rows; mark them `OWNER` in the queue so runs skip them instead of deferring them.
- Pixel-diff golden images (client_capture 4.2 keeps them out; still the right call).

## 3.6 Follow-up plan steps, owner decisions 2026-11-16 (planned, not built)

- **Judge reclassification (TX-45).** Planned, not built: `qrun.py judge RES ID --reclassify BATCH-FAULT --cause "text"`
  may turn a row whose check FAILed into BATCH-FAULT. A non-empty recorded cause is required (who, when, why); the original
  failed check stays in `checks` with `ok: false`, and the row records `reclassified: {from, to, cause, by, at}` so tables
  show both. The tool refuses without a cause and never edits `checks`.
- **Refusal text for the mismatch matrix (TX-31).** Planned, not built: replace the guessed `CLI_REFUSAL` / `SRV_REFUSAL` in
  `tools/scratch_query/qpair.py` with Forge 1.19.2 (43.3.9) strings found in the Forge jar (`HandshakeHandler`,
  `assets/forge/lang/en_us.json`): log lines "Terminating connection with server, mismatched mod list" and
  "Connection closed - mismatched mod channel list"; screen texts "Incompatible FML modded server", "Your client is missing
  the following mods ...", "The server is missing the following mods, remove these mods from your client ...",
  "Server mod list is not compatible"; and the registry refusal seen in run r1111 (SQ-CDNS-12), "Failed to synchronize
  registry data from server". The current regexes (`Mod rejections` etc.) do not contain these and are wrong. The Power Grid
  client-without-server case is not a handshake refusal at all: the client joins and crashes in
  `ShootableGadgetRenderHandler.onRenderPlayerHand` (TODO #90), so the matrix must expect JOINED then no crash, or a client
  crash file, not REFUSED.

## 4. Suggested order

1. TX-40, TX-41, TX-06, TX-45 (S each): fewer BATCH-FAULTs and crashes noticed at once, with no game change.
2. TX-01 then TX-03: the capture driver exists; a runner plus text readback clears most of the 511 client rows.
3. TX-20 + TX-21 + TX-22: one devtools command set for the server side, with tick warp and better bots.
4. TX-02, TX-04, TX-05: move client driving in-process and retire xdotool for new rows.
5. TX-30 (owner decision on the one-JVM rule for a paired run), then TX-31.
6. TX-23 GameTests as the long-term regression layer for own jars; TX-42 / TX-44 when the queue grows again.

## 5. Owner decisions

- D1. Which items to build, and in what order (section 4 is a suggestion).
- D2. Paired client + own scratch server as one "game run" under the one-JVM rule (TX-30)?
- D3. Install `xvfb` for unattended client runs (TX-07)?
- D4. Devtools test command in Java (TX-20) vs keep growing KubeJS probes.
- Item 3 open (2026-11-15, after the build): (a) retire the migrated `/zq` verbs and stop copying `zq_probe.js` in
  `prep_srv.sh` once SQ-TX passes, or keep both while old batches exist? (b) `zq season` (Creative Calendar) into
  `/cdevtest` through an optional reflective read, or keep it KubeJS? (c) install the Devtools jar into dev `mods/`
  after SQ-TX, or keep it build/libs-only (prep_srv swaps it in either way)?
