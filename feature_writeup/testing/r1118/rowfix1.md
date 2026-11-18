# r1118 rowfix-1: unspawned bots and the section `@@prep` key (2026-11-18)

Agent rowfix-1, spawned by the r1118 orchestrator (883b649d) after judge-1 ([j1.md](j1.md)). Rows and
tools/scratch_query only; nothing booted, no commits. Not redone: judge-1's probe install, BBQ2 bot spawns, BBQF SALT2-06.

## 1. Rows that used a bot their section never spawned

Scan: every `pbot <verb> NAME` in a row whose row has no `pbot spawn NAME` and no earlier row of its section spawns it,
plus planned rows whose section spawner is not in the same driver step (HH-1500 and TX-12 are not in the r1118 plan).
Fix: a block before the row's first step, marked `# self-contained since r1118 (rowfix-1, 2026-11-18): bot spawn from
<source setup row>`, holding the source batch's forceload and `> execute unless entity NAME run pbot spawn NAME X Y Z
dim survival`, then `sleep 3`. The `execute unless entity` form spawns only when the bot is not online, so a chain of rows
keeps the bot where the previous row left it (old batch semantics) and it does nothing when the section already spawned
it. Positions come from the setup row of the `@from` batch (tools/scratch_query/batches/). Applied by
`tools/scratch_query/rowfix_spawns.py` (idempotent; the table in it is the full list). 81 rows:

| File | Rows | Bot, source |
|---|---|---|
| BBQW | SQ-BBQW-02, -03 | c6b (r1115 p6_a1 303.5 -60 366.5); c2b (r1113 SQ-C2-00, 300.5 -60 296.5) |
| CBW | SQ-CBW-02, -03 | c2b; -03 also G (r1115 p4_a1: c2b 303.5 -60 366.5, G 500 -60 490; its `pbot remove G` would log done=0 against `reject ...done=0`) |
| BBQS, CEQ, CFS, PJL | SQ-BBQS-02, -03, SQ-CEQ-02, SQ-CFS-03, SQ-PJL-02 | c2b (r1113 SQ-C2-00) |
| CIF | SQ-CIF-02, -03 | cb (r1113 c3_b1 setup: forceload 0 0, stone at 0 199 0 / 0 199 2, bot at y 200) |
| CM, HR, WC, WCT | SQ-KT-21, SQ-HR-02, -03, SQ-WC-05, SQ-WCT-02 to -05 | qa 0 -60 0 (r1101 p1_server / r1031 a1_world) |
| EFX | SQ-EFX-13 | qe -50 -60 -50 (r1031 a1_followup) |
| FCK | SQ-FCK-01, -10, -11, -12, -14, -15, -17, -18, SQ-AZW-02, SQ-MG-23, SQ-BOT-03, SQ-FCB-10 | G 1000 -60 1000 (r1113 c3_a1 setup) |
| FCK | SQ-AZW-01 | m3 2060 -60 2060 (r1115 p3_a1 P3-00) |
| IP | SQ-PRT-01, -02, -03 | qa 1350 -60 1030; qm 1200 -60 1065 (r1113 c3 setups) |
| HOP | SQ-HOP-02, -04, -05, -07, -09, -10 | G 615 -60 512 (r1115 p5_a1 P5-setup) |
| HOP | SQ-HH-266 (rerun), SQ-HH-266b, -266c | hb1 178 -60 204 (P5-setup) |
| HH | SQ-HH-1505, -1506, -1507, -1509, -1516, -1522, -1534 to -1537, -1539, -1540, -1546, -1559, -1560, -1566, -1569, -1603, -1604, -1606 | G 500 -60 490 (SQ-HH-1500, not planned) |
| PTH | SQ-PTH-04 (P, Q), -08 to -13 (P) | r1115 pth_a0 P11-setup: P 1000.5 -60 990.5, Q 1002.5 -60 990.5 |
| PS15B | SQ-PS15B-03, -04, -08, -11 | P (r1116 ps15b_a P2-setup) |
| SY | SQ-SY-21 | c6t 1000.5 -60 1050.5 (r1115 p6_a3) |
| TX | SQ-TX-13, -14, -15 | A 960 -60 900 plus the NoAI golem when absent (SQ-TX-12, not planned) |

Also fixed: PS15B.qb SQ-PS15B-27 lint E (`grep -c` exit status, `; true` added). Every edited .qb lints 0 E.

Not fixed (pass up):
- MELT.qb (left alone, another session's uncommitted work): SQ-SALT2-06b (m3n, planned, Nether bot 8 129 8 of r1115
  P3-00) and SQ-MELT-08 (m3); SQ-MELT-01 to -07 still lack their P3-00/P3-01 build (j1.md item 2). `qrows.py check`
  fails on MELT.qb:249 (SQ-MELT-08b has no steps and no @draft), the same session's work.
- PSU.qb SQ-FCB-04 (s5knf), SQ-PSF-08 (s5f8): spawn positions are `{S5H..}` offsets of a home marker that a lost setup
  row placed; RD.qb SQ-VTD6-02, SQ-FDB-05, -11 spawn their own bots but read the `s1home` marker the same way. Not
  planned in r1118; they need the marker setup rewritten into the rows.
- Lost setup rows also set gamerules (randomTickSpeed 0, doFireTick false), `season set MID_AUTUMN` (HOP), `time set 18000`
  (PTH zombies): the spawn blocks do not restore those; rows that need them should set them in the row.

## 2. Section `@@prep` key (pre-boot setup the driver applies)

Format (qrows.py docstring): `@@prep [A:|B:] SPEC`, lines add up, a leading `A:` / `B:` limits a line to that group's step.
SPEC (shlex-quoted) is `KEY=VALUE ...` (UPPERCASE = prep_srv.sh environment, only NOSWAP, SKIPSWAP, COPYDEV, PGJAR;
lowercase = server.properties via prep_srv.sh `prop=value`), `toml FILE SECTION KEY VALUE` (`qrun.py toml` on OUT/srv/FILE,
bare FILE = config/FILE, created when absent; SECTION with spaces quoted as `'"Loading Protection Configuration"'`;
VALUE verbatim), `copy SRC DEST` (repo path -> OUT/srv/DEST) or `rm PATH` (glob under OUT/srv). A bad line is a load error
(lint, check, status_index). Rendered in the queue as a "Driver prep" line.

Code: qrows.py `prep_line`, `prep_of(sec, group)`, `section_of`, load / render / write_qb; qdrive.py `section_prep`,
`prep_srv` (env + props), `prep_actions` (toml / copy / rm; re-pins with `qpin.py pin` when an action touches mods/),
`run_step` uses them and logs to sNN_prep.txt; `qrun.py prep FILE SECTION [A|B] [--out OUT --level LEVEL]` prints a
section's prep, or runs prep_srv.sh plus the actions without a boot. The step reads the .qb when it starts, so .qb edits
reach later steps; state.json is unchanged (no new keys). Headless test: prep_line on 12 good and bad lines, prep_of for
HH 2 / RD 1 A and B / a section without prep / a missing file, prep_srv with a stub prep script (env and props passed),
prep_actions on a fake OUT (six HH tomls, the quoted Loading Protection section, copy file and dir, rm glob); tools/
test_queue_rows.sh 13/14 (PROC-20 fails only on the MELT.qb check above).

**The running driver needs stop + resume to apply `@@prep`** (qdrive.py is loaded once). qrun.py and qrows.py are
backward compatible (qrun parse ignores `@@` lines).

Sections filled (pending steps; values from the `@from` batch headers and section texts; NOSWAP / SKIPSWAP of past runs
not copied, since r1118 tests the current builds):

| Section | @@prep | Rows it serves |
|---|---|---|
| HH 2 (s64) | spawn-animals=true difficulty=normal spawn-monsters=false; HH crow stealChance 1.0, carryTicks 200; salt saltPlayerLickChance 1.0, saltAnimalStageChance 1.0, saltLickInterval 200, saltSearchInterval 20 ("Boot F" of hh_crow / p4_a1); Loading Protection 1 s | 76 |
| HH 3 (s65, s66) | spawn-animals=true difficulty=normal; salt lick 200 / search 20 / stage 1.0; LP 1 s | 16 |
| PWG 1, 2, 3 (s93, s94, s95) | PGJAR=jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-20.jar (newest gate jar); 2 and 3 also spawn-animals=true spawn-npcs=true and LP 1 s | 77 (plus s152) |
| PTH 1 to 5 (s88 to s92) | difficulty=normal spawn-animals=true spawn-npcs=true; 1, 2: compat surface.passthrough arrowLifeTicks 3600, arrowLongLifeCap 3; 3: leaf_piles slowFromLayers 6; 5: debug.passthrough timing true | 24 |
| PS15 1 (s85) | salt lick 200 / stage 1.0 / player lick 1.0; LP 1 s | 8 |
| FCK 1 (s54, s139) | difficulty=normal spawn-animals=true spawn-npcs=true | 13 |
| RD 1 (s97, s98, s128, s153, s165) | spawn-animals=true; B: creativeworldgen named_mobs chance 1.0 (SQ-NM) | 48 |
| HCS 1 | spawn-animals=true; crow stealChance / carryTicks; LP 1 s | 5 |
| RDX 1, CBW 1 | spawn-animals=true; LP 1 s | 4 |
| PJL, HR, KT, SSP, FFH, CM 3, EFX, GF31, FLR, RDB | spawn-animals=true | about 40 |
| GAP, PG | difficulty=normal spawn-animals=true spawn-npcs=true | 3 |
| MCL | difficulty=normal spawn-monsters=true | 4 |
| IWF, MRD | B: difficulty=normal spawn-animals=true spawn-monsters=true | 3 |
| SB 1, CIF 1, ENGM 1, IDS 1 | B: difficulty=normal | 8 |
| DEP 2 | difficulty=normal | 1 |

Survey: 67 planned sections carry pre-boot prose. The rest cannot be one step-wide prep and stay prose (pass up):
- several boots in one step: WO 1 A (A1 defaults for WO-01 to -13, A2 wise_oak tomls for WO-14 to -30), HH 2 tranche
  restarts (SQ-HH-1324 to -1326, -274), CBA / CBO / CFR / CGE / TP / PSU toml-and-restart rows, SPF boot 0 baseline. Needs a
  row-level `@prep` that splits a driver step (build_steps), not done.
- other prep scripts: CCO and CC12 (`cc_only_srv.sh`), CMV (rewritten armortrims mods.toml), DEP 09b (one boot per removed
  jar).
- legacy-config fixtures: CAL, CBJ4, CDJ, CDJ4, CEN, CEQ, CFR, CMP, CS2, CDNS (old config files or old jars staged by hand).
- PWG 2's ponder-structure datapack (extracted from the gate jar) and PTH 1 to 3's tier datapack (PTH 4 says the Compat jar
  now carries the tags; if the swapped Compat jar predates phase 4, PTH 1 to 3 need it back as a `copy`).
- MELT (not edited): its text asks for difficulty=normal and `[melting]` tomls.
