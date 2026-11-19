<!-- SPDX-License-Identifier: GPL-3.0-only -->
# Test row fixes from scratch run r1118 (2026-11-18)

<!-- generated:status_index begin open_work sha1=3239e8ea2d84 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 6 open TODO rows (Planned 3, Testing 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `generators-and-tests #23` | Planned | S | Scratch rows: 1.2.02 upgrade section; drop pre-1.2.02 old-save and legacy-config rows; item-id rows as fresh-world rows |
| `generators-and-tests #24` | Planned | S | Scratch rows: r1118 correct-answer, lost-setup and tool fixes (plan_rowfix_r1118 R1, R2, R5, R6) |
| `generators-and-tests #25` | Planned | H | Scratch driver follow-ups from the r1118 row conversion: Devtools in cc_only and cpp_chain mod sets, perfBot jar source for release_1202, W boot lint false positive on @modset rows, toml for top-level keys |
| `generators-and-tests #20` | Testing | H | Scratch driver: qrun restart verb (stop, wait, edits, boot, reset log offsets; deliberate stop is not a crash) |
| `generators-and-tests #21` | Testing | H | Scratch driver: @boot N row key and @@prep run CMD (boot phases on one world, logged shell prep) |
| `generators-and-tests #22` | Testing | H | Scratch driver: @@modset NAME manifests for chain and reduced jar sets (port_chain, cc_only, cpp_chain, cco_sets) |
<!-- generated:status_index end open_work -->

Status: **planned, not built.** Source: [scratch_run_2026-11-18.md](testing/scratch_run_2026-11-18.md) section 10,
the judge reports in [testing/r1118/](testing/r1118/), and the per-row classification
[testing/r1118/classes.csv](testing/r1118/classes.csv) (469 non-PASS rows: id, result, bucket, status, note). Game bugs
are the sibling plan [plan_bugfix_r1118.md](plan_bugfix_r1118.md). Rows file edits never touch MELT.qb while another
session holds uncommitted work in it.

## 1. Intake

| # | Item (owner's words) | Class | Note |
|---|---|---|---|
| 1 | "1 for fixing the test rows" | clear | this file |
| 2 | "drop old-save tests unless a good reason exists to test them, and lay them out for me" | open | layout in section 4; the keep set needs a ruling |
| 3 | "fix the test rows with 'correct answer' fixes as well" | clear | section 3 (R1) |
| 4 | "break down why multiple boots and a reduced jar set is outside the drivers abilities, and how this can change" | clear | section 5; the option choice is open |

## 2. Where the 469 non-PASS rows stand

Buckets from `classes.csv` (keyword rules over the judges' cause text plus manual overrides; a few mixed rows carry a
second note). "Fixed" = the row's .qb text was corrected in the run (mostly listed in [rerun.txt](testing/r1118/rerun.txt));
multi-boot, mod-set and fixture rows count as not fixed until the boot they need exists.

| Bucket | Fixed in .qb | Not fixed | Plan phase |
|---|---|---|---|
| lost setup (spawn, forceload, build, capture, gamerule) | 131 | 9 | R2 |
| reduced mod set | 1 | 73 | R4 |
| multi-boot | 0 | 52 | R4 |
| stale id or command | 39 | 1 | R1 |
| correct answer (row expected the wrong value) | 31 | 7 | R1 |
| harness / tool | 29 | 7 | R5 |
| old save or legacy config | 0 | 29 | R3 |
| other (row design: timing, sample size, wrong block) | 26 | 5 | R6 |
| wrong world group or y level | 10 | 0 | — |
| unjudged | 0 | 14 | bugfix plan B0 |
| real bug | 0 | 5 | bugfix plan |

## 3. R1 Correct-answer and stale-id fixes

The game is right and the row is wrong. 31 were fixed during the run (`classes.csv` bucket `correct-answer`, status
fixed: CBC-01, CBC-04 count, CBB-07, CBO-11, CBM-04, CBP-05, CC-14, CDJ-09, CDV-03, MD-01, SY-14, HH-1252, MDP-01,
PWG-71, PWG-90, CM-05, CM-06, CM-30, CAL-01, WT-01, SPF-01, FFH-03, CDF-07, CDNS-10, GAP-04, SPFX-02, SPFX-03, CBJ-03,
CDM-00, CDM-05, MDP-03, CIF-03, FDI-04, HH-1546). Left:

| Row | Row expects | Game shows | Why the game is right | Fix |
|---|---|---|---|---|
| SQ-AC-02 (AC.qb) | 36 armor | 40 | `dragonloot-common.toml` sets 8 + 13 + 11 + 8 = 40; `armorCap` is 50 | `@pass 40`, `expect ... is 40`; toughness check reads `Armor Toughness ... is 18.0`, so match that text; delete the superseded SALT.qb copy (duplicate id) |
| SQ-SB-01 | `[spawn-boxes] 15 box(es)` | 17 | check that the 2 new boxes are intended (the spawn-box list in Compat) before changing | 15 -> 17, or `>= 15` if the list grows by design |
| SQ-SLK-03, -04 | salt dust / salt layer refill | Rock Salt refills one stage | refill G2b removed the old path | rewrite both for the Rock Salt one-stage refill |
| SQ-FGD-00 (RD.qb) | 8 mushroom rules | 9 modifiers, 9 tags | confirm the 9th is a deliberate rule | look 8 -> 9 |
| SQ-RSP-09 | copper cup + 250 mB | cups removed 2026-11-02 | cup half obsolete | drop the cup half; the 250 mB check waits on `rdetail` fluid amounts (bugfix B6) |
| SQ-CBB-07 | title and `@pass` say 44 drinks | 51 of 51 | checks already updated in the run | title and `@pass` -> 51 |
| SQ-IDS-02 | 8 unknown commands | 7 | the rename sweep wrote the new names `ccaltime` / `cwglocate` into the old-name list | old names back to `cetime` / `celocate` |
| SQ-CDJ4-03 | Baby Mobs and Incubation Additions standalone | both nested now | premise obsolete | retire (`@superseded`) |
| SQ-GAP-04 | block `forge:storage_blocks/gold` checked as an item tag (judge-8 edit) | block tag gold only | the judge's "gold-only by design" was wrong: owner ruled rose gold joins the block tag (bugfix B8) | revert the judge-8 change: check the block tag again, after B8 is built |
| SQ-CDFW-11 | 2026-10-30 recipe baseline | 3863 LOST, all deliberate reroutes | baseline obsolete | re-baseline from a current dump, or retire |

## 4. R3 Old-save and legacy-config rows: layout

**What counts as a good reason.** The last deployed release is **1.2.02** (client and server 2026-11-09, commit
40ed32be; `deploy/DEPLOYED.json`). Both machines took every release from 1.2.00 on, so every migration that shipped up
to 1.2.02 has already run on every world that exists. A row is worth keeping only when a player upgrading from 1.2.02
would hit it: renames and removals **after** 2026-11-09 (CD namespace move, other jars' namespace moves, golden stews
removed, Rustic fold-in, H&H hops and tilled end stone, `baby_mobs` -> `babymobs`, More Mooblooms and Exposure Extras
ids). Items can be tested without an old save (a fresh world, a chest written with the old id in its NBT, as SQ-EX-01
does); placed blocks, block entities, entities, dimensions and player data cannot, because the chunk stores them by name.

**Recommendation:** one "1.2.02 upgrade" section: build a world on the 1.2.02 jars in `deploy/latest/mods/` (or copy the
deployed server world), then boot it on the current jars and check everything below marked keep. This replaces every
dev-jar fixture (`jar_archive/p4_oldid`, `displaced`, `cdns_step2`, the NSM pre-move jars).

| Row | Tests | Recommendation |
|---|---|---|
| SQ-NSM-07 | player standing in a moved dimension; dimension folders move | **keep** (upgrade section; the most valuable row) |
| SQ-NSM-02 | moved items in chests, worn and dropped | **keep** worn/dropped in the upgrade section; chest half as a fresh-world row |
| SQ-NSM-03 | cardboard box, Botanist Pot, jar, nightstand block entities | **keep** (upgrade section) |
| SQ-NSM-04 | placed paintings, banner pattern (client half) | **keep** (upgrade section, client half later) |
| SQ-NSM-09 | Labels notes and edited labels config after the rename | **keep** (upgrade section) |
| SQ-CDNS-01, -03, -04, -09 | CD namespace swap: scans, repeat boots, CfB corner, remainder buffer | **keep** as one upgrade-section row on 1.2.02 jars; retire the step2-jar variants |
| SQ-HOP-11, SQ-HH-1546b | hops lattice keeps age and facing; tilled end stone -> Ender's | **keep** (upgrade section); drop HH-1546b if 1.2.02 had no tilled end stone |
| SQ-RFX-25, -26, SQ-BM-03 | Worldgen rename, Moobloom variants, baby squid, `baby_mobs` config | **keep** (upgrade section) |
| SQ-RDB-05 | Rustic blocks keep their states | **keep** (upgrade section) |
| SQ-VH-11 | Visceral Heap in new Nether chunks of an existing world | **keep** (copy of the server world) |
| SQ-NSM-05, -10, SQ-OB-11, SQ-EX-01, SQ-CDNS-02 | old item ids (lunchbox, camera bags, wooden bucket) | **rewrite** as fresh-world rows: chest with old-id NBT, read back the new id |
| SQ-GLD-16 | removed golden stews and cans fail quietly | **rewrite**: fresh world `/give` and log checks; jar fluid half in the upgrade section |
| SQ-RDB-06 | stale `rustic_upstream/` beside `mods/` is ignored | **rewrite**: fresh world with the folder put in place |
| SQ-NSM-01 | old seasoning stack loads | **rewrite** new-world half only; drop the old-stack half (loss accepted) |
| SQ-FLR-06 | crushing wheel and saw items after a restart | not old state: moves to R4 (in-row restart) |
| SQ-CDJ-05, SQ-DDP-01, SQ-DDP-07 | ran on the boot-0 world | **rewrite** on a fresh world |
| SQ-CDNS-05 | roll a new save back to 1.2.02 jars | **drop** (no player rolls back) |
| SQ-CDNS-06 | swap json naming a missing target | **drop** (dev error path; a headless swap-list lint covers it, R5) |
| SQ-CEN-02, SQ-CMG-02, SQ-CFR-03, SQ-CFR-04, SQ-CMP-02, SQ-CDJ4-02, SQ-CBJ4-02, SQ-CAL-09 | config migrations out of the CE / Compat files | **drop**: shipped in 1.2.00 to 1.2.02, already ran on both machines (CFR-04 is a skip path no machine took) |
| SQ-CBJ4-01, -03 to -07 | CB section pinned to CE 1.1.07 jars | **drop** the section; re-home any behavior not covered by SMK / CBP as current-jar rows |
| SQ-CDJ-11, SQ-CDFL-03, SQ-CS2-01, -04, -05, SQ-CWG-08 (old-world half), SQ-PSU-08, SQ-CBP-06, SQ-CCO-14, SQ-LBS-05, SQ-GLD-01, SQ-ENM-02, SQ-CNG-03, SQ-RD-03, SQ-DF-07, SQ-CBJ-04, SQ-CDJ-07, SQ-RDX-13, SQ-DDP-06, SQ-MDP-15 | old worlds and ids from 1.1.x to 1.2.02 | **drop** (migrations already ran everywhere) |

Dropped rows get `@superseded` with the reason "old save from <version>, migration shipped in <version> (owner,
2026-11-18)" so history stays; the upgrade section is a new SQ prefix (test_prefix_index.md row first).

**Headless backstop:** a check that every id in the pre-2026-11-09 dumps either exists now or is in a swap list
(`replaces` / `removed`). It covers the id-table half of every keep row without a boot (R5).

## 5. R4 Multi-boot and reduced jar sets: why the driver cannot, and how that changes

### How the driver works today

The driver (`tools/scratch_query/qdrive.py`) turns the plan into steps, and **one step is exactly one server boot**:

1. It groups rows by the key `phase # rows file # section # world letter` (qdrive.py:153). The world letter folds every
   `@group` label to A or B (qdrive.py:124-131), so "A1 before the restart", A2 .. A5 all land in one step.
2. Per step it builds one server folder with `prep_srv.sh`, which links **every** non-client jar in the pack's `mods/`
   and copies kubejs and config (scratch_server.sh:56-66, prep_srv.sh:27-72), applies the section's `@@prep` once
   (only `toml`, `copy`, `rm` actions, qdrive.py:186-225), pins the jar set (qpin), boots once, runs all the step's rows
   through `qrun.py run`, and stops in a `finally` (qdrive.py:286-353).

### Why multiple boots fail

- The step key has no boot number, and the prep runs once, before the one boot. There is nowhere to say "now stop,
  change this, boot again".
- `qrun.py` has no restart verb. A row that sends `stop` makes the log watcher treat the exit as a crash
  (qwatch.py:85-86), and the batch ends ABORT (qrun.py:903-905). `--relaunch` exists only for crash recovery and the
  driver never passes it.
- A boot also wipes `latest.log` (scratch_server.sh:72), so a row reading log lines across a restart would lose its
  offsets.
- Result: rows like SQ-WO-10 run their first half and stop at `save-all flush` (rows/WO.qb:239-255); the restart is only
  in the prose.

### Why reduced jar sets fail

- The jar set is always the full pack: setup links every jar, `PREP` is fixed to prep_srv.sh (qdrive.py:39), and the
  only environment keys are NOSWAP, SKIPSWAP, COPYDEV, PGJAR (qrows.py:234, 274-275).
- `@@prep rm` deletes by glob; there is no "keep only these jars", no way to empty kubejs or config with exceptions, and
  `copy` must come from inside the instance (qdrive.py:205). So the chain sets (port_chain.sh pulls jars from
  `../CEPortTesting`, port_chain.sh:23, 125-131) and the Common-only sets (cc_only_srv.sh, cpp_chain_srv.sh: allowlist,
  wiped config, probe-only kubejs) cannot be described.
- The jar pin refuses any jar without a `mod_sides.md` row (qpin.py:133, qrun.py:840-844), which the CEPortTesting
  and release jars lack.
- When the driver booted these sections anyway, it booted the full pack, where the "optional mod absent" checks cannot
  pass (CBA-03: alexsmobs is present, so the skip line never prints).

### Old-save fixtures

These need boot 1 on old jars, a stop, a jar swap, and boot 2 on the same world: the multi-boot limit again, plus a
step that runs a script (`git archive` of an old tree, `c2_legacy_cfg.py`, `cdns_fixture.sh`), which `@@prep` has no
action for.

### How it can change

| Option | What it adds | Covers | Size |
|---|---|---|---|
| A2 `restart` verb in qrun | stop, wait for the JVM, run the row's shell edits, boot, wait for `Done (`, reset log offsets, re-arm the watcher and probes; a deliberate stop is not a crash | in-row restarts: WO-10, HH-274, HH-466, FLR-06, TP-03/05/06, MCL-05 | about 85 lines (qrun, qwatch, qlint) |
| A1 `@boot N` row key + `@@prep boot=N:` | boot number in the step key; the step runs boots 1..N in order on the same folder and world (or `fresh` for a new one), each with its own prep | boot phases: WO A2-A5/B/S, HH Boot D/F, CBA-11..14, CGE, SPF boot 0, PWG-20/-22/-54r, the 1.2.02 upgrade section | about 150 lines (qdrive, qrows, lint, docs) |
| B2 `@@prep builder SCRIPT` | the step's server folder is built by a named script instead of prep_srv.sh | cc_only_srv / cpp_chain_srv sets as they are (stopgap) | about 30 lines |
| B1 `@@modset NAME` manifests | `tools/scratch_query/modsets/NAME.toml`: jar source (pack, CEPortTesting, deploy), keep globs, add/drop, kubejs and config policy, donor folders; part of the step key; qpin allows the source's jars | every chain and reduced set: CBA, CBB, CBC, CBO, CBP, CC, CC12, CCO, CMV, CIF-04, CPP-04/05, DEP-02, ENGM-05, HOP-01 | about 150 lines plus moving the 4 scripts' lists into manifests |
| C1 `@@prep run CMD` | a logged shell step (cwd the server folder, `$INST` / `$OUT` set), usable between boots with A1 | legacy-config fixtures, oldsave swap, the upgrade section's jar swap | about 25 lines |

Recommended order: A2 (smallest, unblocks in-row restarts) -> A1 + C1 (boot phases and the upgrade section) -> B1
(mod sets; B2 only if B1 must wait). Every option is backward compatible: rows without the new keys run as now. Each
lands with a `qlint` rule, a gotchas.md line and a SKILL.md grammar line.

## 6. Other phases

| Phase | Rows | Work |
|---|---|---|
| R2 lost setup | SALT2-06b, FGD-13 (captures from FGD-12: give it its own worldgen setup), PTH-07 (really multi-boot: R4); MELT-01 to -05, -07, P1, P1b (blocked: MELT.qb has another session's uncommitted work; hand to that session) | row edits |
| R5 harness / tool | WCT-05 and RSP-09 (bugfix B6), FGD-11 (`install_probes` skips `ztrades.js`), CDS-02 to -04 (`tag_diff.py` needs the 2026-11-09 rename map), SPF-12, the headless old-id check (section 4), the 264 check lines ending in a space (`trail-space` lint: strip them in the .qb files or make qrun keep them; ruling) | tools/scratch_query |
| R6 row design | TP-04 (Tempad cooldown after the API open; check with the Tempad owner), CS-04 (sample too small: 4x), GFB-03 (row design), SQ-CMV-04 (cannot pass in a full pack: advancements dropped; retire or move to a reduced set) | row edits |
| R7 rerun | `/dumpall` first (dumps predate the 2026-11-09 id move); a fresh driver over `rerun.txt` plus every row this plan changes; then steps 151 to 167 (`qrun.py drive resume r1118` or a new run) | owner says when |

Order: R1 and R2 (row text, no tool change) -> R5 tool fixes -> R4 driver options in the ruled order, each followed by
converting its rows -> R3 upgrade section (needs A1 + C1) -> R7.

## 7. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | "drop old-save tests unless a good reason exists" | open | one 1.2.02 upgrade section (section 4 keep rows); item-id rows rewritten as fresh-world rows; the rest dropped with `@superseded` | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 2 | driver option order (section 5) | open | restart verb (A2) -> `@boot N` + `@@prep run` (A1 + C1) -> `@@modset` manifests (B1) | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 3 | release jars from `deploy/latest` as an accepted pin source for the upgrade section | open | accept: a modset naming `deploy/latest` as its jar source may pin jars without a `mod_sides.md` row | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 4 | trailing-space check lines | open | strip the trailing spaces in the .qb files; `trail-space` lint stays an error | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 5 | SB-01 and FGD-00 counts: confirm the new values are intended before changing | clear | agent checks the code first | 2026-11-18 | this plan |
| 6 | SQ-TP-04: does an API open (`/ztp open`, `openTimedoor`) set the Tempad cooldown? | open | REVERSED 2026-11-18 (chat): an API open sets the non-zero 40-tick cooldown; SQ-TP-04 checks `cd true` after the open, SQ-TP-41 the macro path. History: no (API opens cooldown-free) earlier the same day | 2026-11-18 | chat 2026-11-18 |
| 9 | Creative Devtools in the cc_only and cpp_chain mod sets | clear | allowed: `creativedevtools-*` joins both base groups and leaves both forbid lists (it needs only Common); closes the manifest half of `generators-and-tests #25` | 2026-11-18 | chat 2026-11-18 |
| 10 | SQ-CMV-04 advancement grant | clear | dropped: the row keeps the config-default checks only (the pack drops advancements) | 2026-11-18 | chat 2026-11-18 |
| 7 | port_chain jars (CEPortTesting) as a pin source | open | accept: the port_chain mod set keeps `unlisted_ok`, beyond ruling 3's `deploy/latest` | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 8 | old_id_check: 29 pre-2026-11-09 ids neither present nor in a swap list | open | the Display Delight dishes are accepted as conditional (present only with their mods); the other 5 get swap entries (`strong_phantom_bane` replaces into `phantom_bane`; `removed` for `fruit_beignet`, two `incomplete_quiche` ids, `vinery:jellie`; commit 069db1cd; row SQ-SWL-02) | 2026-11-18 | AskUserQuestion 2026-11-18 |

## 8. Phase notes

Row-text phases, 2026-11-18 (agent, row-text executor of the r1118 plans). Row edits only; no tool, skill or game file
changed; nothing booted. Every changed row carries an `r1118 R<n> (2026-11-18)` comment line.

- **R1 done** except SQ-GAP-04 (waits on bugfix B8) and SQ-RSP-09 (waits on B6), both left. AC-02 40 armor and the
  `Armor Toughness ... 18.0` text (and CQ-side SQ-AC-04, the same 36 -> 40); the SALT.qb run-log copy of AC-02 deleted.
  SB-01 15 -> 17: intended, `spawn_boxes.json` gained `end_tower` (mobs/pillagers #2) and `allay_temples` (mobs/spawns #5)
  in the 2026-11-06 split. FGD-00 8 -> 9: intended, the 9th rule is Miner's Delight `cave_carrots` (cave mode, in
  `mushroom_spawns.json` since 2026-10-24); bootexpects on `9 mushroom modifiers` and `9 mushroom tags` added. SLK-03 / -04
  rewritten for Rock Salt (hand, waxed, dispenser) with the G6-10 negatives (layer and salt dust no longer refill); they
  overlap SQ-SALT2-11, which already tests the same refill. CBB-07 51; IDS-02 old names `cetime` / `celocate`;
  CDJ4-03 and CDFW-11 superseded.
- **R2 done** except SQ-PTH-07 (multi-boot, R4). MELT-01 to -05, -07 and SALT2-06b self-contained (P3-00 setup and the
  P3-01 build slice per row; bots `m3` / `m3n` spawned when absent; the retired zmelt probe dropped; MELT-04 watches blue0
  every tick with `cdevtest watch` and checks its tier sequence in a shell step). MELT-P1 / -P1b: snow layers on a
  snow_block course, both boxes cleared first, the cleanup fill split under 32768 blocks. MELT-08b got a `@draft` (no
  steps; `qrows.py check` failed on it). FGD-13: the plan's premise was wrong, SQ-FGD-12 had no captures either (the
  r1102 setup rows FGD-12a to -12e were not migrated): both rows now locate the five squares and pregenerate them.
  Left in MELT.qb: MELT-06, -09, -09b (multi-boot, R4) and MELT-08 still send the retired `zmelt` commands.
- **R6 done** for CS-04 (four 9x9 open fields measured as one box, n=320, own setup, shell prints the ratio) and GFB-03
  (normal world: a generated floating hit from `cdevtest floating`, held, then falls after a neighbor update). Left:
  SQ-CMV-04 for an R4 B1 Common-only modset (in the full pack the pack drops every advancement, so the grant can not
  pass); **SQ-TP-04 for the Tempad owner**: `/ztp open` calls `openTimedoor` directly and leaves the item cooldown at 0
  (`cd false percent 0`); r1107 saw the cooldown through the item use / macro path. Question for the owner: should an API
  open set the cooldown? If not, the `cd true` check moves to a macro or item-use row (`/ztp macro` needs a pin).
- **R3 drops and rewrites done; upgrade section and keep rows not started** (they need A1 + C1). `@superseded` with the
  section 4 reason on CDNS-05, -06, CEN-02, CMG-02, CFR-03, -04, CMP-02, CDJ4-02, CBJ4-01 to -07, CAL-09, CDJ-11,
  CDFL-03, CS2-01, -04, -05, PSU-08, CBP-06, CCO-14, LBS-05, GLD-01, ENM-02, CNG-03, RD-03, DF-07, CBJ-04, CDJ-07,
  RDX-13, DDP-06, MDP-15 (versions from `deploy/history/*.json`: Common, Equipment and Magic jars first shipped in 1.2.02,
  Creative Brewing in 1.1.06, Labels 1.0.1 in 1.2.00). CBJ4 behavior is covered on current jars by SMK-37 to -40, CBA-02,
  -07, -08, CBO-05, -13, -15, EFX-01, -08, -09, so nothing was re-homed. Fresh-world rewrites: EX-01, OB-11, CDNS-02,
  NSM-01, -05, -10, GLD-16, RDB-06, CDJ-05, DDP-01, DDP-07, and new SQ-NSM-11 (the chest half of keep row NSM-02).
  RDB-06 relies on `scratch_server.sh` linking every `*_upstream` folder, so its folder is there by default; SQ-RDB-01
  ("no rustic_upstream folder") therefore needs its own `@@prep rm rustic_upstream` section. Not superseded: SQ-CWG-08
  (only its old-world half drops; the rest needs boot 1's captures, so the split goes with R4), and the CDNS step2
  variants (they become the one upgrade-section row). Prefix `UPG-` added to test_prefix_index.md for the upgrade section.
- **Tool findings for R5** (not changed): `qlint` `waitlog2` still warns that a repeated waitlog returns at once, but
  qrun waits for a new line since judge-14; `cdevtest watch` lines carry no position, so two watches at once can not be
  told apart (MELT-04 watches one block for that reason); SQ-CWG-08 still sends `ceperf` / `ceplace`.
- **R4 conversions and the upgrade section, 2026-11-18** (driver keys from commits 54ab001d, 3e43388e, 78cd52e4): two
  forks converted WO, HH, FLR, TP, MCL, PTH, PWG, MELT, CWG (dfd34c7f) and CBA, CBB, CBC, CBO, CBP, CC, CC12, CCO, CMV,
  CPP, CDFL, CIF, ENGM, CGE, SPF, CDS (1cbff999). New rows file UPG.qb: section 1 (SQ-UPG-01 boot 1 on `@@modset
  release_1202` with the dev perfBot jar copied in and the old config edits as `@@prep run`; SQ-UPG-02 to -13 `@boot 2`
  after `@@prep boot=2: modset pack`) and section 2 (CDNS fixture: SQ-UPG-14 boot 1 and baseline scan, -15 boot 2
  compare, -16 boot 3). The keep rows they replace are superseded. SQ-HOP-01 and SQ-RDB-01 moved into sections of their
  own with `@@prep rm`. SQ-DEP-02 moved the same way (DEP.qb section 3). Not expressible: DEP-09b (ten boots, one per removed jar),
  CIF-04 Equipment half and CMV-03 (no manifest), WO-37 (strace launch), and the cc_only / cpp_chain manifests forbid
  Creative Devtools, so their `/cdevtest` rows can not pass until the manifests allow it (tools owner).
