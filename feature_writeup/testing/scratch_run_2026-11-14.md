# Scratch run 2026-11-14 (r1114)

Owner request 00:00: "execute the scratch test queue", main session as orchestrator; each phase runs in one runner
subagent (one at a time, one game JVM at a time). Runners append to section 7 and 8; the orchestrator keeps section 1
and 4 current.

## 0. Performance capture

Perf session `r1114`, opened by the phase 1 runner before the first boot; left open between phases; closed and
reported (no `--prune`) by the last runner.

Closed 12:08 by the phase 5 runner; report (no `--prune`): [perf/sessions/r1114/summary.md](../../perf/sessions/r1114/summary.md)
(50 boots, per-boot reports in `perf/sessions/r1114/boots/`).

## 1. Scope

Taken, in order:
1. SQ-TPF Third-party bug fixes (Packet Fixer 3.3.2, Debugify Reforged 1.1.0), batches `batches/r1113tpf/`.
2. r1113 resume steps 1 to 3 ([scratch_run_2026-11-13.md](scratch_run_2026-11-13.md) 12): fix_c2a1, fix_c3a1, c4_ar2,
   pwg17r, cq_pwg17.
3. r1113 resume step 4: c2_l1 onward through group B (rerun_b + c4_b).
4. SQ-CC12 Creative Common 1.2.0 publish re-check (build/libs 11-13 23:12, scratch mods only).
5. P11 Power Grid regression ([scratch_run_2026-11-13.md](scratch_run_2026-11-13.md) 9) with the pending batch fixes
   from its section 10.

Not taken: client rows pinned until wave 8+ (queue Priority run note); SQ-WO-34 (inotify limit, owner).

## 2. Tools

As r1113 (section 2 there), plus the TPF scripts `tools/scratch_query/tpf_*.{py,js}`.

## 3. Jars

`mods/` vs build/libs at 00:00: build newer than mods for creativecommon (1.2.0 23:12 vs 1.1.0), creativecompat
(11-13 17:58), creativebrewing and creativecalendar (11-12 20:47), creativeengineering (11-10 23:06),
creativeworldgen-own (11-10 22:38). prep_srv.sh swaps newer build jars into scratch srv only. Power Grid -17 in
`mods/` (dropped by prep_srv.sh unless PGJAR set).

## 4. Order

| Phase | Content | Estimate | State |
|---|---|---|---|
| 1 | SQ-TPF | 1 h | done 01:14 (1 h 13 min; server rows only) |
| 2 | r1113 steps 1 to 3 | 1.5 h | done 03:16 (2 h; client gate ran with 419 jars) |
| 3 | r1113 step 4 | 4.5 h | done 09:57 (runner died 05:24 after the last boot; judged and c4_br rerun by runner 2) |
| 4 | SQ-CC12 | 0.5 h | done 10:02 (3 boots, PASS 3) |
| 5 | P11 regression | 2 h | done 12:08 (2 h; R1, R2, R3 and P1 perf boots plus two rerun batches; C1 client not run) |

## 5. Row map

Per phase, written by its runner.

- Phase 1: queue SQ-TPF; `batches/r1113tpf/s1_base.qb` (P1), `s1_fix.qb` (P2 @s1, P3 all minus MC-121706),
  `s19.qb` (P3b, two group B levels); results `perf/runs/r1114/tpf/{p1_base,p2_fix,p3_all,p3b_base,p3b_fix}`.
- Phase 2: r1113 resume steps 1 to 3, batches in `batches/r1113/`: `fix_c2a1.qb` + `fix_c2a1r.qb` (SQ-PJL-04, CFS-02b,
  STN-02k, -03, -06, CBW-02, BBQW-02, -02s, -03, ROLL-11, -12b, BBQ2-03, -05, -06), `fix_c3a1.qb` + `c3_a2.qb --rows
  SQ-PG-01b,SQ-IP-05b (now SQ-PRT-05b)` + `fix_c3a1r.qb` (SQ-MG-22, AZW-03, BOT-03, FCK-09, -11, -13, -16, FCB-09, -10, IP-01, -04, -05,
  -05b, PG-01a, -01b), `c4_a.qb --rows SQ-FDI-01` + `c4_ar.qb --rows SQ-FDI-02` (setup) + `c4_ar2.qb` + `c4_ar3.qb`
  (SQ-FLR-02b to -05, BREED-03, FDI-02, -03), `pwg17r.qb` + `pwg17r2.qb` + `pwg17.qb --rows SQ-PWG-SHOW17` (SQ-PWG-73,
  -76, -78 to -82, -90, -91, -94, -95), `cq_pwg17.qb` (CQ-PWG-22, -24, -26 to -29); results
  `perf/runs/r1114/{fix_c2a1,fix_c2a1r,fix_c3a1,fix_c3a1_restart,fix_c3a1r,c4_setup,c4_ar2,c4_ar3,pwg17r,pwg17r_show,pwg17r2,cq_pwg17}`,
  verdict files `perf/runs/r1114/verdicts/`.
- Phase 3: r1113 step 4, batches in `batches/r1113/`: `c2_l1.qb`, `c2_l1b.qb`, `c2_l2.qb` (SQ-CMG-02, -02b, CEN-02, -02b,
  -05s, CFR-03, -04), `c2_nobrew.qb` (DEP-02), `c1_g1.qb` + `c1_g2.qb` (GLD-16), `c3_co.qb`, `c3_co2.qb` (CMV-02, -03),
  `c3_cc1.qb`, `c3_cc2.qb` (CIF-04, -04b), `c3_ch1.qb`, `c3_ch2.qb`, `c3_ch2b.qb`, `c3_ch3.qb` (ENGM-05, CDFO-02, -03,
  -03b, -03c, FCK-02b, MIN-01, -03, -06), `c3_b1.qb` + `c3_b1r.qb` + `c3_b2.qb` (ENGM-01 to -04, IDS-01, -02, CIF-02, -03,
  CDW-02, CBP-11, MIN-07, FCK-07, -08, -08b, HSC-89), `c1_b1.qb` (PST-12), `c3_cdw1.qb` to `c3_cdw3.qb` (CDW-01, -01b,
  -01c, -03, -03b, -03c), `rerun_b.qb` + `c4_b.qb` + `c4_br.qb` (HH-328, NSM-08, -08a, GAP-03, RD-10); results
  `perf/runs/r1114/<batch>/`.

- Phase 4: queue SQ-CC12; `batches/r1114cc12/cc12_01.qb` (SQ-CC12-01), `cc12_02.qb` (-02), `cc12_03.qb` (-03); results
  `perf/runs/r1114/{cc12_01,cc12_02,cc12_03}`.

- Phase 5: P11 Power Grid regression on jar -17, batches in `batches/r1113pwg/`: `pwgr1.qb` (R1: SQ-PWG-P11A, P2 to
  P10 rows), `pwgr2.qb` (R2 restart: -20, -18b, -15) + `pwgr1r.qb` (reruns -21, -23, -54, chain -76 to -82), `pwgr3.qb`
  (R3 restart: -22, -18c) + `pwgr3r.qb` (-43 rerun), `pwgperf.qb` (SQ-PWG-01 to -05, -07, -08); results
  `perf/runs/r1114/{pwgr1,pwgr2,pwgr1r,pwgr3,pwgr3r,pwgperf}`, verdict file `perf/runs/r1114/verdicts/pwgr1.md`.

## 6. After the run

Write-back per skill B5 (queue Run log, writeups, TODO rows for FAILs, gotchas), by the orchestrator after phase 5.

Done 2026-11-14 by the write-back agent (documentation only, nothing booted):
- Queue: Run log entry r1114 and header line; "Latest" pointer moved to r1114. Stale row texts fixed: SQ-PWG-13 (6 wire
  types; measurement present when P2 solves run first), SQ-PWG-70 (cap 185, overheat 175), SQ-PWG-94 (connector reads as a
  line middle, T0=0), SQ-CDW-01 (endersdelight has no pack to list); SQ-PST-12 marked "owner: row text stale, see run r1114".
- Writeups (results line in each test list): thirdparty_fixes.md 0 and 7, plan_power_grid.md P11, creative_engineering.md
  5 and 7.5, recipes_cooking, kitchen, remainder_buffer, barbeques_delight, roll_delight, plan_meals_rolls_bbq,
  plan_farming_cooking_batch, plan_food_cooking_batch, plan_portal_changes, plan_portal_gun, plan_flesh_recipes,
  breeding_audit, food_dictionary, creative_magic, creative_split, plan_creative_delight, plan_cb_publish,
  creative_delight 24.4, plan_cb_polish 9, plan_owner_ids, plan_hearth_crow_salt, namespace_move, hearth_and_harvest,
  rustic_delight. README status notes on 18 rows; "built, scratch server PASSED" only for Creative Common 1.2.0
  (SQ-CC12) and the Engineering carve (SQ-ENGM-01 to -05).
- TODO rows added: `pack/server #39` (URGENT 420+ jar client crash), `largecontent/mod-additions #81` (Debugify config),
  `#82` (acid vs water-to-salt), `#83` (SQ-PWG-82), `#84` (SQ-PWG-06), `#85` (SQ-PWG-43), `#86` (SQ-PWG-66), `#87`
  (solar contraption after restart), `dishes/meals #67` (SQ-BBQW-02s), `food/types-and-tuning #39` (SQ-FDI-03),
  `creativedelight/framework #16` (SQ-CDW-01), `#17` (SQ-CDW-03b), `farming/wild-crops #8` (SQ-CDW-02),
  `creativedelight/members #57` (SQ-DEP-02), `#58` (SQ-MIN-03), `locate #6` (SQ-HH-328), `gear/tuning #23`
  (SQ-CBP-11), `tooling/generators-and-tests #15` (SQ-FCK-08b, SQ-PWG-51, -53 batch fixes). Updated:
  `largecontent/mod-additions #80` (SQ-TPF results), `#79` (SQ-PWG-91), `#39` (SQ-ROLL-12b owner pick),
  `gameplay/tnt-and-portal #15` (SQ-PG-01a, -01b; back to Bug), `food/types-and-tuning #16` (SQ-FDI-01, -02, pumpkin
  pie batch note), `creativedelight/framework #13` (SQ-CDW results), `creativedelight/members #45` (SQ-RD-10).
- Changelog: no bullet; the run changed no gameplay file (only batches, probes and docs).
- Gotchas: the "Found in r1114" section of `.claude/skills/scratch-test/gotchas.md` checked for duplicates.

## 7. Run notes

### Phase 1 SQ-TPF

- 00:01 perf session r1114 opened (collector pid 518597). Results under `perf/runs/r1114/tpf/`.
- 00:02 TPF configs into a scratch dir: new `tools/scratch_query/tpf_srv.sh OUT LEVEL A|B PF [debugify args] [-- prop=v]`
  wraps prep_srv.sh, then copies both jars from `jar_archive/thirdparty_fixes/` if missing, writes
  `srv/config/packetfixer.properties` per PF (`gen` = deleted so Packet Fixer writes its defaults; `shipped` = the
  instance file; `vanilla|client|server` = `tools/scratch_query/tpf_pf/*.properties`, the SQ-TPF-30 variants;
  `none` = jar removed), writes `srv/config/debugify.json` with `tpf_debugify_cfg.py` (no args = every fix off;
  `NODEBUGIFY=1` removes the jar), copies `tpf_probe.js` as `zz_tpf_probe.js`, and lists srv jars without a
  mod_sides.md row (expected: debugify, cdevperfbot, the swapped creativecommon-1.2.0 build).
- 00:02 P1 base: `tpf_srv.sh $S/tpf_p1 tpf_a A gen` (group A flat, Debugify 0/69, Packet Fixer generated defaults).
  Boot 00:02:43, Done 00:04:02. Log: Debugify "Enabled 0/34 bugs" (34 = the server side of its 69), Packet Fixer
  "applied successfully", the expected IP payload `@ModifyConstant conflict` warning; two benign mixin ERRORs
  "does not specify minVersion" (debugify configs). Generated defaults: `allSizesUnlimited=true`, timeout 120, sizes at
  vanilla; kept as `perf/runs/r1114/tpf/packetfixer_generated_defaults.properties`.
- 00:04 tooling: `tpf_probe.js` failed to load (`io.netty.buffer.Unpooled` blocked by the class filter). Fixed: netty
  loaded lazily inside `bigpayload` only (that command reports the error; a client row), `new ItemStack(item)` ->
  `getDefaultInstance()`. Copied in, `/reload`: `ztpf sound` -> `sound=stone`.
- 00:06 `s1_base.qb` started.
- 00:15 `s1_base.qb` try 1 (kept as `p1_base_try1/`): 00 10 13 16 17 PASS, 11 12 14 15 18 FAIL, all BATCH-FAULT:
  (a) the scratch srv has `spawn-animals=false`, so ServerLevel discards every Animal/Npc each tick (cows, wolf,
  chicken, striders vanished: "No entity was found"; row 10's pass was hollow); (b) row 11: `@e` skips dead entities
  (selector adds `Entity::isAlive`), so the dead cow never matched; (c) row 12: MCA replaces the zombie villager and
  the cure gives `mca:*_villager`. Fixes in `s1_common.inc` (regenerated): header notes the props, row 11 selects the
  cow by UUID and reads `DeathTime`, row 12 uses `#zq:villagers` and `data get ... Passengers[0].id`.
- 00:17 P1 rebuilt fresh: `tpf_srv.sh $S/tpf_p1 tpf_a A gen -- spawn-animals=true spawn-npcs=true` (every later TPF
  server uses these two props). Done 00:18:33; `s1_base.qb` rerun 00:18:39.
- 00:27 `s1_base.qb` try 2: 00 11 13 14 15 16 17 PASS, 18 LOOK (AABB absent; inside counts 24, 24, 24, 24, 23, 23, 22:
  bug reproduced), 10 FAIL (one cow survived: it walked off its strike point), 12 FAIL. Row 12 live check: MCA
  (`overwriteOriginalZombieVillagers`) replaces a summoned `minecraft:zombie_villager` on join with an MCA one that has
  lost its vehicle and `ConversionTime` (-1); summoning `mca:male_zombie_villager` directly keeps both and converts to
  `mca:male_villager`, still riding (bug reproduced). Batch: cows `NoAI:1b`; row 12 summons the MCA zombie villager
  (the pack's only real path). Rows 10 and 12 rerun 00:30 (`--rows`): both PASS. Base: all nine bugs reproduce.
- 00:30 P1 stopped (no packetfixer/debugify errors besides the minVersion lines). P2 fix: fresh dir
  `tpf_srv.sh $S/tpf_p2 tpf_a A shipped @s1 -- spawn-animals=true spawn-npcs=true` (fresh flat level instead of a
  world copy: group A is generated identically). Done 00:31:54, "Enabled 10/34 bugs". `s1_fix.qb` 00:31:58.
- 00:41 `s1_fix.qb` (P2, @s1): 00 11 12 13 14 15 16 17 PASS, 10 FAIL (cows killed, no drops), 18 LOOK. Row 18
  in both runs: no cow ever outside the pen (final total = last inside count: base 22, fix 24); the losses are deaths
  (beef and leather by the pen; Common's damage-tag log: `cramming`, then with cramming off `inWall`). MC-2025 not
  reproduced with this setup; the fix saves `AABB` (seen). Batch: row 18 now sets `maxEntityCramming 0` for the row and
  prints `p2025-out` per escaped cow each cycle (look text updated). Row 18 fix rerun 00:42 with cramming off: inside 25
  for 5 cycles, then 24 (one more death, no escape).
- 00:47 MC-206922 live (P2): a stone item struck 2 s after spawning is destroyed with the fix on. Export check (P3,
  `-Dmixin.debug.export=true` in srv `user_jvm_args.txt`): Debugify's head injection is in `Entity.thunderHit` and
  ItemEntity's handler cancels only while `tickCount <= 8`; so the mixin applies, and the drops still go (ItemPhysic
  1.6.7 is loaded and handles item burning: the likely second path). Fix ineffective in this pack.
- 00:50 P3 `tpf_srv.sh $S/tpf_p3 tpf_a A shipped all -- spawn-animals=true spawn-npcs=true`: BOOT CRASH, FATAL
  `Mixin apply failed debugify-common.mixins.json:basic.server.mc121706.BowAttackGoalMixin -> RangedBowAttackGoal:
  @Shadow field f_25782_ was not located` (the field and descriptor exist in the vanilla srg jar, so another
  transformer changed the class first; Old Guns 3.7.3 has mixins on RangedBowAttackGoal / AbstractSkeleton, the only
  other jar that does). Log kept as `perf/runs/r1114/tpf/p3_all_crash/latest.log`. MC-121706 is "off" in the plan
  already. Rebooted 00:51 with all 68 others (`tpf_debugify_cfg.py ... <ALL minus MC-121706>`): "Enabled 33/34",
  Done 00:52:54; `s1_fix.qb` 00:53.
- 01:03 P3 `s1_fix.qb` (68 fixes): 00 10 11 12 13 14 15 16 17 PASS, 18 LOOK (inside 25 x6, then 24: one death, no
  `p2025-out`). Row 10 rerun 3 times (`p3_all_r10x1..3`): PASS, FAIL, PASS. So MC-206922 is partial: 3 of 4 runs on
  all-on, 0 of 1 on @s1; matches the export (only items with `tickCount <= 8` are spared; a bolt that flashes longer
  still destroys the drops). No ERROR from `dev.isxander.debugify` besides the two minVersion lines.
- 01:04 P3b level 1 `tpf_srv.sh $S/tpf_b1 tpf_b1 B vanilla -- spawn-animals=true spawn-npcs=true` (all off, Packet
  Fixer `vanilla` file: SQ-TPF-30), Done 01:07:00. `s19.qb` row SQ-TPF-19 (sleep 40), stop without save-all:
  `full=25 proto=416`. Base saves proto chunks; per the row, shortened: new row SQ-TPF-19s8 (chunk 375,375, sleep 8)
  on a reboot of the same level: `full=3 proto=438`.
- 01:10 P3b level 2 `tpf_srv.sh $S/tpf_b2 tpf_b2 B server @s1 -- ...` (Packet Fixer `server` file), Done 01:12:33,
  SQ-TPF-19s8: `full=16 proto=425`. MC-224729 not reproduced (base and fix both save partial chunks); fix stays off.
- 01:14 every JVM stopped, `game_jvms.sh` empty. Client rows (CQ-TPF-31 to -91): not run; no client batch exists yet
  (the queue says the runner writes them) and most need hand steps or shot judging; left for the owner / a client
  phase. Perf session r1114 left open.

### Phase 2 r1113 resume steps 1 to 3

- 01:15 phase 2 runner start. Debugify set: thirdparty_fixes.md still lists `@s1` as the planned shipped tranche
  (c1+ are client rows), so every phase 2 server is `tpf_srv.sh OUT LEVEL A shipped @s1 -- spawn-animals=true
  spawn-npcs=true ...` (Packet Fixer = the instance file), COPYDEV=1. Results under `perf/runs/r1114/<batch>/`.
- 01:16 fix_c2a1 boot (`tpf_srv.sh $S/fixc2a1 fixc2a1 A shipped @s1 -- spawn-animals=true spawn-npcs=true`, probes c2, zp,
  zact): Debugify "Enabled 10/34", Packet Fixer applied, Done 10.0 s. SoL Carrot is now in the instance mods/ (11-13 17:57,
  has a mod_sides.md row), so every srv carries it. fix_c2a1.qb 01:17 to 01:24: PASS 2, FAIL 2, LOOK 11. Stopped.
- 01:27 judged by a subagent (`perf/runs/r1114/verdicts/fix_c2a1.md`): PASS 11, FAIL 2 (BBQW-02s stick loss, ROLL-12b
  medley cans exist), BATCH-FAULT 2 (ROLL-11, BBQ2-05: the "+4 food per eat" of c2a1 is peaceful regen, 1 food per 10
  ticks during the 2 s pace; fix: `gamerule naturalRegeneration false`, rerun batch `fix_c2a1r.qb`, same world).
- 01:25 fix_c3a1 boot (`tpf_srv.sh $S/fixc3a1 fixc3a1 A shipped @s1 -- difficulty=normal`, probes c3, zp, zact with keep),
  Done 10.0 s; fix_c3a1.qb from 01:27.
- 01:35 fix_c3a1.qb done: FAIL 2 (FCK-13, PG-01a), LOOK 12. PG-01a/IP-04 (now PRT-04)/IP-05 (now PRT-05) read "portals: 0" while the console
  counted 4 `portalgun:custom_portal`: c3_probe.js matched the type with `Registry.ENTITY_TYPE.getKey(e.getType())`
  (KubeJS remaps getType: r1113 gotcha) and then threw on `level.dimension()`. Probe fixed (getEncodeId, dimension
  fallback), /reload, `zc3 portals` lists 4. First try kept in `perf/runs/r1114/fix_c3a1_try1/`; rerun 01:39 of
  PG-01a, IP-04, IP-05 (--rows, same world).
- 01:40 rerun done; batch expect `portals: 2` was wrong (qa's spawn pair counts too: 4), fixed in fix_c3a1.qb. 01:41
  stop, `heartbeatSeconds 60`, reboot Done 3.8 s (01:42:00), c3_a2.qb `--rows SQ-PG-01b,SQ-IP-05b` into
  `perf/runs/r1114/fix_c3a1_restart/`.
- 01:50 fix_c3a1 judged by a subagent (`perf/runs/r1114/verdicts/fix_c3a1.md`): PASS 12, FAIL 2 (SQ-PG-01a, -01b: Creative
  Compat portal gun exit-lift flag; PortalGunExitLift.java rotates an already rotated content direction, and
  PortalGunExitLiftMixin misses portals linked in PortalGunItem, so orange reads 1b and the wall portal 0b), BATCH-FAULT
  2 (FCK-13, IP-05: live toml edits, even in-place rewrites, are never seen by Forge's watcher on the scratch srv).
  Rerun batch `fix_c3a1r.qb` (values set before each boot, two boots, same world).
- 01:51 fix_c3a1r: stop, toml (honeycombMultiplier 1, heartbeat 2, despawnDistance 128), boot r1, rows; stop,
  despawnDistance 0, boot r2, rows; pack values restored after (script `$S/c3r.sh`).
- 01:54 fix_c3a1r r1 + r2 done (FCK-13r void: the fix_c3a1 dispenser was still in place, setblock "Could not set the
  block"; batch now clears it first). 01:55 boot r3 (honeycombMultiplier 1), SQ-C3R-setup + FCK-13r: honeycomb x3. Pack
  values restored in the srv toml (2 / 10 / 128). fixc3a1 stopped.
- 01:57 fixc2a1 reboot (same world), fix_c2a1r.qb with naturalRegeneration false: bread 5, slice 10, golden roll 20,
  wrap +10, sandwich +16, bibimbap +16, only the Raspberry Pie Speed 740t. Stopped 01:59.
- 02:00 c4a: the r1113 c4a world was gone; fresh `tpf_srv.sh $S/c4a r1114c4a A shipped @s1 -- spawn-animals=true
  spawn-npcs=true`, c4_a.qb header setup (Loading Protection 1 s, probes zc4, hh_crow, zact, zcount, ztrades; SoL Carrot
  is in mods/ now). Done 9.6 s. Setup rows c4_a SQ-FDI-01 and c4_ar SQ-FDI-02 (bot fdj eats 20: carrot=19 applepie=19,
  max health 22) into `perf/runs/r1114/c4_setup/`, then c4_ar2.qb 02:04 to 02:15: FAIL 3 (FLR-02b, FLR-04: the crusher rig
  is built by c4_ar's FLR-02b, absent on a fresh world; BREED-03 count), LOOK 4. c4_ar2 FLR-02b now builds the rig when
  absent; 02:16 rig area cleared, FLR-02b + FLR-04 rerun: valid ok, slime 20 of 100, string 42 of 100, nothing else.
  First try kept in `perf/runs/r1114/c4_ar2_try1/`.
- 02:22 c4_ar2 judged by a subagent (`perf/runs/r1114/verdicts/c4_ar2.md`); its c4_ar3.qb (SQ-FDI-02b) run: pumpkin pie
  eaten from food 4 stays 4 (not edible in this pack: a placed pie in block_dict), bread control 4 -> 9 and counted.
  c4a stopped 02:24.
- 02:23 pwg17r: `COPYDEV=1 PGJAR=jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-17.jar tpf_srv.sh $S/pwg17r pwg17r A
  shipped @s1 -- spawn-animals=true spawn-npcs=true` (sha1 cbc2ddee37ca; 13 probes, current copies: cogdrive 3, node 1,
  doCloseContainer 4; Loading Protection 1 s; cc-tweaked is a listed pack jar). Done 10.2 s; pwg17r.qb from 02:24.
- 02:31 pwg17r.qb done: FAIL 5, LOOK 5, PASS 4. SQ-PWG-SHOW17 (pwg17.qb `--rows`) built on the same world for the client
  tour (lit cords, plating wall, lit ceiling; the -91 lamp at 1100 -58 2020 is absent in this world). Judging by a
  subagent (`perf/runs/r1114/verdicts/pwg17r.md`): PASS 10, FAIL 1 (SQ-PWG-91, B36 loot tables), BATCH-FAULT 3 (-90 probe
  counted blank pattern cells as unresolved and missed the empty #forge:cardboard barrier: pwg_p10_probe.js fixed; -80
  water went in before the items and Creative Delight's heated water-to-salt mixing ate it; -82 part depots full before
  the circuit). -94 PASS against upstream design (connector without internals is a line middle). Rerun `pwg17r2.qb`.
- 02:40 pwg17r stopped (world kept for the client copy). 02:41 pwg17r2: fresh level, same prep as pwg17r (sha1
  cbc2ddee37ca, 13 current probes with `blank=`), Done 10.1 s; pwg17r2.qb from 02:41.
- 02:45 pwg17r2.qb done (FAIL 1, LOOK 4, PASS 2), judged by the runner: -90 PASS (blank= separate, ceiling_tile
  deploying `unresolved=[1]` = the empty #forge:cardboard, as the row expects), -76 PASS, -78 PASS, -79 PASS (unetched
  circuit at progress 0.5 then done), -80 PASS (items first: acid 250 + water 750, etch to incomplete_circuit with
  Schematic, acid 1000 -> 750), -82 INCONCLUSIVE (assembly on the belt done: circuit_board x1 Schematic components=3,
  B25 stone depot skipped; the arm left the part stacks loose on the belt, x6 connectors and x7 resistors, instead of in
  their depots: upstream Create arm behaviour not checked). pwg17r2 stopped 02:47.
- 02:48 cq_pwg17 client: `eval_client.py setup $S/cli17` (instance mods/: Common 1.1.0 on the client), -17 jar copied into
  the client's mods only, `config/debugify.json` = @s1 (without it Debugify runs all 69, MC-121706 crashed the server
  boot), sp copy of the pwg17r world with the SHOW17 showcase (`sp_world.py`), launch with JFR.
- 02:49 CLIENT CRASH x3 at window creation: `*** stack smashing detected ***: terminated` right after KubeJS "Loaded
  client.properties" (native, 23 s after launch; outside the sandbox too). Third launch with debugify-1.1.0 and
  packetfixer-3.3.2 moved out of the client's mods: title screen at 60 s. One of the two kills the client at window
  creation in this pack (both sit in the instance mods/: the owner's next client launch would hit it). Which one is
  isolated after the batch. Launch logs `$S/cli17/launch_try1..3.out`.
- 02:52 Singleplayer -> pwg17r -> Play by xdotool clicks (client_drive.sh coordinates); in world 02:54;
  `qrun.py client` cq_pwg17.qb from 02:55 (client without Debugify and Packet Fixer).
- 03:06 cq_pwg17.qb done (LOOK 11, 51 shots), client JFR dumped to `perf/runs/r1114/cq_pwg17/client.jfr`, client closed.
  Judged by a subagent (`perf/runs/r1114/verdicts/cq_pwg17.md`): no port FAIL; 6 shots spoiled by eval_client's
  `zz_perfeval_drive.js` (spectator + teleports for 3 min after the join): BATCH-FAULT, header of cq_pwg17.qb now says
  to delete it before launch.
- 03:10 to 03:16 client crash isolated with four more launches (each to "Sound engine started" or death): Packet Fixer
  only (420 jars) DEAD 23 s; Debugify only (420) DEAD 22 s; neither (419) title 59 s; Packet Fixer in and Clear Void
  out (419) title 58 s. So it is the JAR COUNT, not either mod: 419 jars start, 420 or more die with `*** stack smashing
  detected ***` right after KubeJS "Loaded client.properties" (window creation). The instance `mods/` has 421 jars
  (Debugify, Packet Fixer, and the unlisted Power Grid -17 jar), so the owner's next client launch from this instance
  very likely dies the same way. Not fixed (not the runner's to change mods/). Launch logs `$S/cli17/launch_*.out`.
- 03:16 phase 2 done: every JVM stopped (`game_jvms.sh` empty), perf session r1114 left open. Scratch dirs kept:
  fixc2a1, fixc3a1, c4a, pwg17r (SHOW17), pwg17r2, cli17.

### Phase 3 r1113 step 4

- 03:17 phase 3 runner start. Every server `tpf_srv.sh OUT LEVEL A|B shipped @s1 -- <props>`; helper `$S/p3.sh` (boot/run/stop). Results `perf/runs/r1114/<batch>/`.
- 03:16 c2_l1 (`tpf_srv.sh $S/c2l1 c2l1 A shipped @s1`, `c2_legacy_cfg.py l1`), Done 9.5 s: CMG-02, CEN-02, CFR-04 LOOK
  (all carry lines, values kept); CEN-05s "No entity" = BATCH-FAULT (pace 1 put the kill 1.4 s after the summon,
  shrapnel discards on first hit): batch now `pace 0`, TNT at y -40, kill 0.15 s after; rerun "Killed 12". 03:19
  c2_l1b restart Done 3.9 s: no carry lines, values kept (Compat WARNs that the old Engineering file still holds moved
  sections: inert, as the row says).
- 03:20 c2_l2 Done 9.9 s: carry line before merge, CD rainOutTicks = 100, Compat [campfire] count 0; qrun FAIL was the
  `grep -c` exit 1 on the expected 0 (BATCH-FAULT, `; true` added).
- 03:22 c2_nobrew (creativebrewing jar removed): DATAPACK LOAD FAILED ("Failed to load datapacks, can't proceed"):
  `Can't find block vinery:cherry_leaves` from Creative Delight's Hearth and Harvest member loot modifier
  `hearthandharvest:cherries_from_cherry_leaves` (unguarded vinery block and item). JVM hung after the failure; stopped.
  Log `$S/c2nb/latest_try1.log`. 03:28 reboot with a scratch-only override of that file in srv/kubejs/data (impossible
  condition): Done 10.5 s, no other blocker; only Brewin' class miss is Create Central Kitchen's own optional mixin
  WARN (qrun's bootreject matched it); worldgen grape rules and the Raspberry Pie ladder skip vinery entries cleanly.
- 03:30 c1_g1 old-save pair: the header's premise is stale (mods/ CD is now the 11-13 jar without the stews). Boot 1
  `NOSWAP=1 tpf_srv.sh $S/c1g c1g A shipped @s1 -- spawn-animals=false` with the CD jar swapped for
  `jar_archive/cdns_full/creativedelight-1.1.0.jar` (11-10 01:04, has the stews and jar fluid but NOT the golden cans,
  so the can expect FAILed: fixture has stews, keepers and a filled jar, no cans). 03:32 `oldsave_swap.sh`, boot 2 on
  build jars (Common 1.2.0, CD 11-13): c1_g2 FAIL was the bootreject matching the batch's own `give` "Unknown item"
  reply (BATCH-FAULT; regex now excludes console replies). Substance: chest keeps golden bread, beef, canned bread,
  mushroom stew only; jar `moonlight:empty`; bot inventory empty; both gives Unknown item; no missing-id lines.
- 03:35 c3_co (`cc_only_srv.sh alone`, Common 1.2.0 + KubeJS stack), Done 24 s, reload clean, clean stop. 03:36 c3_co2
  (Armor Trims relabelled 1.4.0): one WARN, PASS. Reduced sets (cc_only, cpp_chain) carry no Debugify / Packet Fixer.
- 03:37 c3_cc1 (Common + Compat + Calendar + Serene Seasons) Done 24 s clean; 03:38 c3_cc2 (+ Equipment, nothing else
  needed) Done 24 s clean.
- 03:39 c3_ch1 FML stop: Creative Worldgen declares 18 worldgen mods mandatory (owner 11-06; Terralith, YUNG's,
  Towns and Towers ...). Rebuilt with them in EXTRA (header updated), Done 52 s. ENGM-05 shell FAIL was Worldgen's
  `[wild-crops] could not read ... or kubejs/generation_scripts/worldgen/biome...` (the biome list is an instance
  file, absent in a jar-only set; the line names the creativedelight path); transition items 86, failed recipes 1.
  SQ-MIN-03 FAIL: `Couldn't parse loot table creativedelight:blocks/placed_corn_popsicle` (unknown item
  creativedelight:corn_popsicle: the item script is `requires: corn_delight`, the Display dish block and its loot
  table are not gated).
- 03:43 c3_ch2 (+ CfB, Balm): rack BlockItem, 1 recipe, 8 CfB mixins; 03:44 ch2b (compartment off): rack AirItem, 0
  recipes, 0 CfB mixins (WARN "Missing data pack creativedelight:cookingforblockheads" = world remembers it).
- 03:45 c3_ch3: FML stop, Incubation requires Blueprint (header fixed); 03:46 rebuilt, Done 25 s: compartments CfB off
  incubation on; emu, duck nests and rack AirItem, no error naming the missing mods.
- 03:48 c3_b1 (`tpf_srv.sh $S/c3b1 c3b1 B shipped @s1 -- difficulty=normal`, probes c3, zp, zact), Done 78 s; c3_b1.qb
  03:50 to 04:09: PASS 1, FAIL 5, LOOK 10. Judged by a subagent (`perf/runs/r1114/verdicts/c3_b1.md`): no real FAIL among
  the five (ENGM-01 grep of Mixing lines in latest.log, IDS-01 regex hit an AE2 INFO line, IDS-02 `/cdevdumpnames` is
  client-only, MIN-07 `/loot spawn POS` rolled at the console origin, FCK-08 counted `delightful:cantaloupe_plant`
  instead of `delightful:cantaloupe`); batch fixed in place, c3_b2's cantaloupe id too. Rerun `c3_b1r.qb` same world
  04:15: SQ-CDW-02r jungle box has 1135 jungle logs and 0 rubberwood log/leaves; SQ-CBP-11r felsic block 50 rolls = 68
  scrap (table 1-2, its `__comment` and the row say 2-4); SQ-MIN-07r ocean (in #creativedelight:fishing_net/shrimp_ocean,
  checked by locate) 5 shrimp of 30, river (checked) 0 salmon eggs of 30 (zc3 items scan misses dropped items: count
  the "Dropped" lines); IDS-02r quoted `/cwglocate batch` answers, status and stop work; FCK-08r cantaloupe 9.
- 04:25 c1_b1 (SQ-PST-12) on the same c3b1 world (c3_b1 leaves daylight off, noon; the row sets its own ticks and seasons).
- 04:24 c1_b1 done: plains spring mean 270, jungle mid summer 262 (0.97), jungle mid winter 206 (0.76) against the row's
  0.35 to 0.70. The row's rule (half speed in tropical biomes) is superseded by the code: CropSeasons.Type.GRASS is "off
  in winter only, neutral in every other season and in wet/dry biomes" (farming/crops #28), so jungle near 1.0 is the
  current design; row text stale (owner).
- 04:24 c3_b2 control (`tpf_srv.sh $S/c3b2 c3b2 B shipped @s1 -- difficulty=normal`, lichen files out, _common features
  out), Done 79 s, same locate boxes as c3_b1: glow lichen 83 (c3_b1 1048, 12.6x); desert hamimelon 1 (17), cantaloupe 2
  (9); badlands hamimelon 6 (4), cantaloupe 0 (c3_b1r printed no badlands count). Setup row FAIL = grep -c exit 1 on the
  expected 0 (fixed `; true`). Stopped.
- 04:36 c3_cdw1 to 3 started in sequence (`$S/cdw.sh`: tpf_srv B, drop myrtrees / fruitsdelight / endersdelight).
- 04:36 c3_cdw1 (myrtrees removed): DATAPACK LOAD FAILED, boot hung to the 600 s wait, own pid killed: `Unknown registry
  key ... trunk_placer_type: myrtrees:rubber_trunk_placer`. CD's myrtrees pack was correctly off; the failing copy is the
  INSTANCE `kubejs/data/creativedelight/worldgen/{configured,placed}_feature/wild/rubber_tree.json` (identical to the
  jar's packs/worldgen/myrtrees copy, ungated; kubejs/data also still holds other wild/* features). Log kept in
  `$S/c3cdw1_try1`. 04:57 rerun with those two files removed from the scratch srv only: Done 80 s, off [myrtrees], one
  skip line `rubber: needs myrtrees`.
- 04:50 c3_cdw2 (fruitsdelight removed) Done 79 s: off [fruitsdelight]; 17 `[wild-crops] mod not loaded, skipped` lines
  (16 fruit tree / bush rules name fruitsdelight features without `requires`, plus `hamimelon: needs fruitsdelight`).
  04:53 c3_cdw3 (endersdelight removed) Done 79 s: no skip line; endersdelight has no own pack (enders_additions stays
  on); only a loot-tuning skip of endersdelight:chorus_crate. qrun FAILs on CDW-01b/-01c = bootreject matched the
  corn_delight GLM "Unknown registry key" WARNs that the full pack logs too (regex fixed in all three batches).
- 04:59 c4b: `tpf_srv.sh $S/c4b r1113c4b B shipped @s1 -- spawn-animals=true spawn-npcs=true`, Loading Protection 1 s,
  hh_mw probe; rerun_b.qb then c4_b.qb in one boot (`$S/c4b.sh`).

- 09:40 (orchestrator) Phase 3 runner died on an API network error (ENOTFOUND) after starting c4b; rerun_b and c4_b had finished at 05:24 (results.md written, not judged); the c4b server sat idle to 09:40 and was stopped by the orchestrator (`scratch_server.sh stop`). Resumed by a new runner: judge rerun_b + c4_b, then phase 4.

### Phase 3 judging

- 09:42 runner 2 start (phase 3 judging, then phase 4). rerun_b SQ-HH-328: three corn maze locates timed out at the
  server's own 120 s search deadline (`[worldgen.locate] searchTimeoutSeconds`; 9 misses / 459 candidates from spawn,
  5 / 942 from 2144 -2592, 8 / 653 from -3000 3000), so the batch's waitlog 600 could not give the longer budget the row
  wants (BATCH-FAULT); the third locate's waitlog matched the second's reply and its answer landed in c4_b's SQ-RD-10.
  c4_b: NSM-08a PASS, GAP-03 PASS; RD-10 capture ran on that stale corn maze reply (village found 3 s later at 272
  -1312: BATCH-FAULT); NSM-08 used `#creativeworldgen:meta/waystone_ruins` (a structure id, not a tag) and
  `/cwglocate census` without its radius (BATCH-FAULT); level.dat's 641 `creativeengineering:` strings are all Forge
  registry aliases (old id -> new id), no worldgen/calendar id. Rerun batch `batches/r1113/c4_br.qb` on the kept c4b
  world with searchTimeoutSeconds 600 in the scratch srv only.
- 09:43 c4_br boot (same c4b world, searchTimeoutSeconds 600 in srv only), Done 6.4 s. SQ-RD-10r: farm line "added to
  63 pool elements of 54 listed farm templates"; village 272 -1312: 482 wheat plus warm_dry crops 107 (cabbages 28,
  peanut 30, tomatoes 15, onions 15, corn 15, tea 4), 0 rusticdelight:cotton; a second village (located from 2000 2000,
  at 880 2640, by console): tomatoes 69, corn 45, cabbages 8, 0 cotton (only wild cotton and bell pepper blocks). Cotton
  is 1 of 11 warm_dry crops on a 0.35 field roll, so two villages cannot show it; `rusticdelight:cotton` parses as a
  block. SQ-NSM-08r: meta/waystone_ruins at -464 -192 (422 blocks); `/cwglocate census 32` done in 10 s, 273 starts,
  0 false negatives, 16 creativeworldgen:group/* sets, none under creativeengineering. SQ-HH-328r: corn maze found from
  spawn after 424 s (16 misses, 1681 candidates) at 7457 7455, 10612 blocks away: corn 350, oak fence 8, two Lootr
  chests, centre table hearthandharvest:chests/corn_maze_center. With the shipped 120 s deadline a player's /locate
  from spawn fails. 09:57 stopped; game_jvms.sh empty.
- Coverage check: every row of the phase 3 batches (c2_l1, c2_l1b, c2_l2, c2_nobrew, c1_g1/g2, c3_co, c3_co2, c3_cc1,
  c3_cc2, c3_ch1, ch2, ch2b, ch3, c3_b1, c3_b1r, c1_b1, c3_b2, c3_cdw1 to 3, rerun_b, c4_b, c4_br) has a verdict in
  section 8 (setup rows and the c3_b2 control rows are folded into SQ-FCK-07/-08/-08b).

### Phase 4 SQ-CC12

- Jar: `development/creativeCommon/build/libs/creativecommon-1.2.0.jar` (11-13 23:12), in scratch srv only (instance
  `mods/` keeps 1.1.0). Batches `batches/r1114cc12/cc12_01.qb` to `cc12_03.qb`, results `perf/runs/r1114/cc12_0N/`
  (`latest_stop.log` = the full log after the stop). Short targeted boots inside perf session r1114.
- 09:57 cc12a (`cc_only_srv.sh $S/cc12a cc12a full spawn-monsters=true`), Done 27.4 s: SQ-CC12-01. Swap summary
  "1 list file(s): ... 66 single id(s) ... 0 capability key(s)" (Common's own list only), 68 permanent aliases,
  "creativecommon: 22 of 22 files loaded", 0 startup errors; on stop "load report: every listed id resolved". The
  RuntimeDistCleaner AbstractClientPlayer / PoseStack ERRORs during server script load are also in r1107cco's 1.1.0
  logs (not new). 09:58 stopped.
- 09:59 cc12b (`cpp_chain_srv.sh $S/cc12b cc12b compat`: Common, Compat, Calendar, Serene Seasons, KubeJS stack; no
  config/, empty kubejs/), Done 23.4 s, `/reload`, stop clean: SQ-CC12-02. qrun FAIL = the only KubeJS error is
  `zz_cl_probe.js` (Creative Labels probe that scratch_server.sh setup copies) loading a Labels class in a set without
  Labels: tooling; cpp_chain_srv.sh now deletes it when the Labels jar is absent. config/creativecommon-common.toml
  written; no error from plantabletags.js or smokable_tags.js.
- 10:01 cc12c (`tpf_srv.sh $S/cc12c cc12c A shipped @s1`, 378 jars, Common 1.2.0 only), Done 9.7 s: SQ-CC12-03. 0
  Mixin apply failed / InvalidMixinException / out-of-range; ClassNotFound lines are third-party optional mixin targets
  and D&D's client XrayParticle event handler; swap summary "32 list file(s): 4 namespace move(s), 686 single id(s), 22
  removed id(s), 43 item merge(s), 4 capability key(s)", 739 permanent aliases, no unresolved / no-survivor line; on
  stop "every listed id resolved"; Common 22 of 22 files, 0 startup ERROR. 10:02 stopped, game_jvms.sh empty.

### Phase 5 Power Grid P11

- 10:04 phase 5 runner start (P11 plan [scratch_run_2026-11-13.md](scratch_run_2026-11-13.md) 9). Jar
  `jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-17.jar` sha1 cbc2ddee37ca, scratch srv only (PGJAR). Results
  `perf/runs/r1114/{pwgr1,pwgr2,pwgr3,pwgperf}`. Prep helper `$S/p5/prep_pwg.sh` (tpf_srv.sh shipped @s1 with COPYDEV=1 and
  PGJAR, 13 current probes, 79-file ponder datapack, CC computer 7 startup, Loading Protection 1 s).
- 10:05 pwgr1.qb rebuilt before the boot (header lists the edits): rows -63, -73, -76, -78 to -83, -85, -88, -89
  replaced by their latest passing versions (pwg17, pwg17r, pwg17r2: -80 items before water, -82 parts after the lock,
  cogwheel drives; the plan had copied the pwg16 blocks); P10 rows SETUP17, -91, -95, -92, -93, -94, -96 appended (no
  separate P10 run); -94 parity expects (connector without internals: T0=0, index=-1, like the wire_connector control);
  P11A expects `blank=` lines and `nbt summary files=57`; -49 mixer driven by a meshing cogwheel (the "motor and gearbox
  first" fix cannot work on Create 0.5.1); -66 motor and shaft before the display board; -69 100 ohm load per loop; -70
  look cap 185; -74 / -75 formatted values (`100.00`, `1.00k`, toboard line); -88 pig max health 10; -92 enum case.
- 10:08 pwgr prep (`spawn-animals=true spawn-npcs=true`, 379 jars, Common 1.2.0 build swapped in as phases 2 to 4),
  boot Done 11.5 s; pwgr1.qb from 10:09.
- 10:10 to 11:08 pwgr1.qb: PASS 16, LOOK 58, FAIL 12 (qrun). FAILs on the way: P11A shell step (`grep -c` exits 1 on 0
  mixin errors: batch, now `echo "mixin errors $(...)"`); -21 (P11A's forceload of 990 990 1060 1090 kept chunk 65
  loaded); -23 (`zpwg ns` lists only the first 20 ids, multimeter not among them in -17: pwg_probe.js `ns <ns> all` added);
  -13 (row expects "no measurement yet" and 5 wire types: P11 runs it after the P2 solves, -17 has 6 wire types; content
  PASS, batch updated); -54 (pwg_p6_probe.js entityLine: `getUUID` not found on ControlledContraptionEntity, now in a
  try); -71 (version regex -12, now -17); -78 to -82 cascade: the plan dropped SQ-PWG-76 as a boot row, but -76 builds
  the chain source board at 1140 -60 1858 (now inserted in pwgr1.qb before SETUP15); -91 known B36.
- 11:08 pwgr1 judged by a subagent (`perf/runs/r1114/verdicts/pwgr1.md`): PASS 75, FAIL 2 (-43 B9, -91 B36),
  BATCH-FAULT 8 (-21, -23, -54, -78 to -82), parts INCONCLUSIVE in -06 (thyratron conducts at grid -30 V / 200 V anode,
  first real read) and -66 (display board `Speed:0.0f`, third run, Create kinetics); -53 open patch BATCH-FAULT (no
  random ticks without a player). -49 basin (cogwheel drive), -51 and the -73 cord pass for the first time.
- 11:09 R2 (restart, overheatedDropChance 0.0; pg_probe and p6 probe recopied), Done 4.1 s: pwgr2.qb PASS 2, LOOK 1
  (-20 reads identical to -20a: 1.19336 A / 10.9885 A, wires 5), then the rerun batch `r1113pwg/pwgr1r.qb` (-21 at Z
  2200, -23 `ns all`, -54 at X 1340, the P9c chain -76 to -82 at X +200): -21 PASS (chunk 65 loaded=false, read unloaded
  resistor, net paused=false, same I, wires 5), -23 PASS, -54 PASS (bearing movedContraption set, panels=2, overrides 2;
  the R1 contraption at 1300 -59 1510 reads attachedToBearing=false after the restart, but its R1 run had the probe
  error, so not judged), -76 PASS (board at 1340 -60 1858 components=3), -78 PASS, -79 PASS (loop done unetched_circuit
  cycles=1), -80 PASS (acid 250 + water 750; etch: incomplete_circuit Schematic components=3, acid 1000 -> 750; qrun FAIL
  = the burner regex kept X 114[26], batch), -81 PASS (circuit_board components=3, same design, parts 8 -> 6 and 8 -> 7),
  -82 INCONCLUSIVE as pwg17r2 (assembly done on the belt, circuit_board x1 components=3, stone depot skipped; the arm
  leaves resistor x7 and wire_connector x6 loose on the belt).
- 11:17 R3 (restart, overheatedDropChance 1.0, clientMirror false), Done 3.9 s: pwgr3.qb -18c PASS, -22 PASS (read 12 V
  1.1934 A wires 5 as R1; config shows clientMirror = false). Then `r1113pwg/pwgr3r.qb` (-43 at X 1340 Z 1700, B9 read for
  60 x 5 ticks): 0.5 V step generated=4 x3 then 0; B9 powered again: generated=1 for 21 reads (about 105 ticks), then 0
  for the last 39, gauge 0.6244 A idle, servo T 38: the servo settles, so pwgr1's -43 FAIL was the 20-read window
  (BATCH-FAULT). Slower than -08 (generated=2 for about 47 ticks): about the same angle at half the speed; recorded.
- 11:20 pwgperf prep (`PONDER=0 prep_pwg.sh`, then only the 4 probes of the header; perfBot jar present; Packet Fixer and
  Debugify @s1 as shipped), Done 9.8 s; pwgperf.qb from 11:21 (perf session r1114, JFR on).
- 12:07 pwgperf.qb done (LOOK 9, PASS 1, all 20 TPS). Mean tick ms (overall): idle 1.86 / 1.91; ladder 25 1.78, 100
  1.94, 500 2.55; 20 grids of 25 3.51; generator and motor 2.04 / 2.21; 40 perfBots near the grid 8.71 / 9.37.
  `powergrid performance` solve avg: ladder 25 8.3 us (max 12.6), 100 about 7 to 21 us, 500 66.2 us (max 131), 20 grids
  219 us (max 414), with bots 257 us (max 525). Worst ticks are /reload (12 s, -13 and -54) and forceload chunk waits
  (-05 4.1 s); SQ-PWG-08 44 ticks over 50 ms with 40 bots (bots, not the grid). Server stopped, `game_jvms.sh` empty.
- 12:08 perf session r1114 stopped and reported (no `--prune`): `perf/sessions/r1114/summary.md`.
- Not run: C1 (throwaway client on the pwgr world): phase 2 ran the -17 client pass (cq_pwg17, CQ-PWG-22, -24, -26 to -29)
  and the queue pins client rows until wave 8+; the CQ-PWG rows of plan section 9 C1 stay with the owner.

## 8. Final verdicts

### Final verdicts summary

| Phase | Content | Rows | PASS | FAIL | INCONCLUSIVE | Other |
|---|---|---|---|---|---|---|
| 1 | SQ-TPF (server rows) | 13 | 9 | 4 (SQ-TPF-01, -10, -18, -19) | 0 | CQ-TPF-31 to -91 not run |
| 2 | r1113 resume steps 1 to 3 | 53 | 45 | 6 (SQ-BBQW-02s, SQ-ROLL-12b, SQ-PG-01a, -01b, SQ-FDI-03, SQ-PWG-91) | 1 (SQ-PWG-82) | 1 BATCH-FAULT (CQ-PWG-24 shot); CQ-PWG hand steps to the owner |
| 3 | r1113 step 4 | 49 | 41 | 6 (SQ-DEP-02, SQ-MIN-03, SQ-CDW-01, -02, -03b, SQ-CBP-11) | 2 (SQ-FCK-08b, SQ-RD-10) | SQ-PST-12 row stale, SQ-HH-328 finding |
| 4 | SQ-CC12 | 3 | 3 | 0 | 0 | |
| 5 | Power Grid P11 on -17 | 95 | 93 | 1 (SQ-PWG-91) | 1 (SQ-PWG-82) | parts open in -06, -66; -53 open patch batch; C1 client not run |
| all | | 213 | 191 | 17 | 4 | SQ-PWG-91 and -82 counted in phases 2 and 5 |

Client finding outside the rows: 420 or more jars in `mods/` kill the client at window creation (section 7, phase 2
03:10; TODO `pack/server #39`). Phase 5 count: pwgr1 85 rows after reruns (PASS 83, FAIL 1, INCONCLUSIVE 1), R2 and R3
5 rows, perf 7 rows.

### Phase 5 Power Grid P11

Jar -17 (sha1 cbc2ddee37ca). pwgr1 per-row table: `perf/runs/r1114/verdicts/pwgr1.md` (subagent); the lines below
override it with the reruns. After the reruns every row PASS except FAIL SQ-PWG-91 (known B36) and INCONCLUSIVE
SQ-PWG-82 (known); parts INCONCLUSIVE in -06 and -66; -53 open patch BATCH-FAULT; no BATCH-FAULT row left.

| ID | Result | Evidence |
|---|---|---|
| SQ-PWG-P11A | PASS | mod 0.6.2-ce-19.2-17, phase 10.0; ns block 78, item 119, block_entity 60, fluid 2, menu 7; 116 recipes; every probe check as pwg17; blank= lines as pwg17r2; nbt summary files=57 clean=42 quirk=15 bad=0; 0 mixin errors (shell exit was a batch fault) |
| SQ-PWG-13 | PASS | reload re-reads "Loaded 6 wire types" (row text 5 is stale: -17 has 6); measurement present because P2 solves ran first |
| SQ-PWG-14, -16, -17, -18a, -19, -20a, -26 to -34, -37, -37cs, -38, -38B, -39 to -42s, -44pre, -44, -48 to -52, -56 to -61, -63 to -65, -68, -69, -71, -73 to -75, -77, -83, -85 to -89, -92, -93, -95, -96 | PASS | numbers as the last passing gate runs (verdicts/pwgr1.md); -49 basin with the cogwheel drive, -51 and the -73 cord pass for the first time |
| SQ-PWG-53 | PASS (lamp); open patch BATCH-FAULT | lamp patch mean 3.917 summer, 3.167 winter (81 %); open patch 0: forceloaded chunk without a player gets no random ticks |
| SQ-PWG-70 | PASS | cap 185 (175 + 10) as the edited look line; queue text still says 135 |
| SQ-PWG-06 | PASS, thyratron INCONCLUSIVE | 15 watches converge at tick 1; thyratron conducts at grid -30 V / 200 V anode (strike voltage at -30 V is 660): first real read, port or upstream not decided |
| SQ-PWG-66 | PASS, board text INCONCLUSIVE | CC peripherals, computer file, display link all pass; display board `Speed:0.0f` (third run; Create kinetics, client check) |
| SQ-PWG-94 | PASS | parity expects: ceiling connector and wire_connector control both T0=0, `index=-1 network=none`, I 1.1987 A through |
| SQ-PWG-91 | FAIL (known B36) | lamp 0/2 drops with tile + factory_light, connector 0/2 tile + wire_connector, junction 0/1, solar 0/1; wrench path fine |
| SQ-PWG-43 | PASS (rerun pwgr3r) | B9: powered again generated=1 for about 105 ticks then 0, idle 0.6244 A, T 38; pwgr1's FAIL was the 20-read window. Slower than -08 (generated=2 for about 47 ticks, similar angle) |
| SQ-PWG-21 | PASS (rerun pwgr1r, Z 2200) | chunk 65 loaded=false, read unloaded resistor, net paused=false, I 1.18681 A before and after, wires 5 |
| SQ-PWG-23 | PASS (rerun) | `zpwg ns powergrid all`: copper_cord, wire_cutter, multimeter, debug, electrical_gizmo registered |
| SQ-PWG-54 | PASS (rerun, X 1340) | bearing movedContraption set, panels=2, running; 3 solarjoin ok; overrides 2 after reload; panel B constant=1361 |
| SQ-PWG-76, -78, -79, -80, -81 | PASS (rerun, X +200) | board components=3; -79 loop done unetched_circuit cycles=1; -80 acid 250 + water 750, etch to incomplete_circuit, acid 1000 -> 750 (qrun FAIL = unshifted X in the burner regex); -81 circuit_board components=3, parts 8 -> 6 / 8 -> 7 |
| SQ-PWG-82 | INCONCLUSIVE (known) | assembly on the belt done (circuit_board x1 components=3, same design, stone depot skipped); arm leaves resistor x7 and wire_connector x6 loose on the belt |
| SQ-PWG-20, -18b, -15 | PASS | R2: reads identical to -20a (1.19336 / 10.98851 A, wires 5); overheated gauge drops nothing at chance 0; cut 1, I 0, wires 4 |
| SQ-PWG-22, -18c | PASS | R3 clientMirror false: 12 V 1.1934 A wires 5; chance 1 drops a current gauge |
| SQ-PWG-01 to -05, -07, -08 | PASS (measured) | 20 TPS throughout; numbers in section 7 Phase 5 (12:07) and the perf summary |

Not run: plan C1 client (owner CQ-PWG rows; the -17 client pass ran in phase 2).


### Phase 1 SQ-TPF

| ID | Result | Evidence |
|---|---|---|
| SQ-TPF-00 | PASS | 7 boots: Debugify "Enabled N/34", Packet Fixer "applied successfully", only the IP payload `@ModifyConstant conflict` warning and 2 benign minVersion ERRORs; generated defaults `allSizesUnlimited=true`, timeout 120, sizes vanilla (`perf/runs/r1114/tpf/packetfixer_generated_defaults.properties`) |
| SQ-TPF-01 | FAIL | `all` crashes the boot: FATAL `Mixin apply failed ... mc121706.BowAttackGoalMixin -> RangedBowAttackGoal: @Shadow field f_25782_ was not located` (field exists in vanilla srg; class changed by another transformer, Old Guns 3.7.3 is the only other jar with mixins on it); `p3_all_crash/latest.log`. With the other 68 on: same verdicts as P2, no new Debugify ERRORs (row 10 3/4) |
| SQ-TPF-10 | FAIL | base reproduces (3 cows dead, no item); fix partial: P2 0/1, P3 3/4 runs with drops. Export: Debugify cancels `thunderHit` only for items with `tickCount <= 8`, so a longer bolt still destroys them |
| SQ-TPF-11 | PASS | base `DeathTime` 26+ after 5 s; fix "No entity was found" (selected by UUID) |
| SQ-TPF-12 | PASS | MCA zombie villager on a chicken converts to `mca:male_villager`; base still riding, fix no passenger, villager exists |
| SQ-TPF-13 | PASS | base a stray, fix still a skeleton |
| SQ-TPF-14 | PASS | wolf base 7.5f, fix 8.0f |
| SQ-TPF-15 | PASS | base 161 striders, 6 saddled; fix 0 saddled |
| SQ-TPF-16 | PASS | base `sound=stone`, fix `sound=copper` |
| SQ-TPF-17 | PASS | base no `LastExecuted`, fix has it |
| SQ-TPF-18 | FAIL | not reproduced: no cow ever left the pen in 12 cycles (base, fix, all-on); every loss was a death (beef/leather by the pen, `cramming` then `inWall`); fix saves `AABB`. Fix stays off unless the owner wants it on the AABB evidence alone |
| SQ-TPF-19 | FAIL | not reproduced: base saves proto chunks at shutdown (`proto=416` after 40 s, `proto=438` after 8 s); fix `proto=425`. Fix stays off |
| SQ-TPF-30 | PASS | generated file has exactly the 11 keys; `vanilla` (`tpf_b1`), `server` (`tpf_b2`) and shipped/`client` (P2, P3) files load unchanged with no packetfixer error |
| CQ-TPF-31 to -91 | not run | client rows: no client batch written yet, hand steps and shot judging; owner / client phase |

### Phase 2 r1113 resume steps 1 to 3

Detail per row: `perf/runs/r1114/verdicts/fix_c2a1.md`, `fix_c3a1.md`; reruns judged by the runner.

| ID | Result | Evidence |
|---|---|---|
| SQ-PJL-04 | PASS | 30 per-id queries, one creativedelight filling/emptying recipe per jam jar and jello |
| SQ-CFS-02b | PASS | lit=false with potatoes and no fuel; coal from the hopper: lit=true BurnTime 429, hopper empty |
| SQ-STN-02k | PASS | kettle buffer bucket first; after 12 s carrot and bottles kept, hopper got green_tea x2 then bucket |
| SQ-STN-03 | PASS | pickle 5 cucumber + salt_dust, kimchi 4 cabbage leaf + salt + onion; progress restarts on the swap |
| SQ-STN-06 | PASS | one bowl plated about 10 + 10 ticks; five bowls 3 plated, 2 left in the buffer; bottle never moves |
| SQ-CBW-02 | PASS | beef cut on both boards, both boards drop themselves with or without an axe |
| SQ-BBQW-02 | PASS | 2 beef + stick + cabbage off hand on a warped basin: raw_beef_skewer, cabbage used, basin empty |
| SQ-BBQW-02s | FAIL | item loss: stick on a 1-beef basin returns CONSUME, the stick is gone (not in bot, basin or drops); Barbeque's Delight basin |
| SQ-BBQW-03 | PASS | spruce support=false, oak tray and birch true; plank removed: all three air, Killed 3 |
| SQ-ROLL-11 | PASS | rerun `fix_c2a1r` (naturalRegeneration false): bread 5, slice 10, golden roll 20 from 0; refused at full; c2a1's +4 was peaceful regen |
| SQ-ROLL-12b | FAIL | live canned_legendary_roll_medley and canned_mythical_roll_medley recipes exist: confirms SQ-ROLL-12 vs SQ-ROLL-27 (owner picks) |
| SQ-BBQ2-03 | PASS | all 6 dump-only skewer ids live |
| SQ-BBQ2-05 | PASS | rerun `fix_c2a1r`: wrap +10, sandwich +16, bibimbap +16 from 2; only Raspberry Pie Speed 740t, no food effect |
| SQ-BBQ2-06 | PASS | endersdelight:cutting/shulker_filet: mollusk -> 2 filets |
| SQ-MG-22 | PASS | coffee bush on basalt both halves air after 10 s at randomTickSpeed 300; wheat grew |
| SQ-AZW-03 | PASS | azalea log strip: stripped log @1 + Immersive Weathering azalea bark @0.5 |
| SQ-BOT-03 | PASS | pot with seeds: bone meal kept (5b), GrowthTime normal (81 ticks over 4 s) |
| SQ-FCK-09 | PASS | 21 feast cans, one recipe each: empty can + feast block item |
| SQ-FCK-11 | PASS | well 3000 -> 2000, can WaterLevel 12, well 10000 after 60 s |
| SQ-FCK-13 | PASS | x6 at the pack value 2 (fix_c3a1); x3 with honeycombMultiplier 1 set before boot r3 (`fix_c3a1r` SQ-FCK-13r). Live read not testable (live toml edits never seen) |
| SQ-FCK-16 | PASS | sneaking use: moisture 0 both reads; water placed above after the item use |
| SQ-FCB-09 | PASS | cooking pot, copper pot, kettle items keep their contents (carrot, bowls, soup; bone, lichen; tea) |
| SQ-FCB-10 | PASS | hung skillet (named, Sharpness II) drops when the wall goes |
| SQ-IP-01 (now SQ-PRT-01) | PASS | five frames: crying obsidian edges, soul fire, no portal entity, no vanilla portal blocks |
| SQ-PG-01a | FAIL | flag reads by colour, not geometry: wall-to-wall blue 0b / orange 1b (both should be false); wall portal with floor partner 0b (should be true). Creative Compat `portalgun/PortalGunExitLift.java` rotates the already rotated content direction; `mixin/PortalGunExitLiftMixin.java` misses portals linked in PortalGunItem (default 1b) |
| SQ-PG-01b | FAIL | after the restart pg's blue wall portal (floor partner) recomputes to 0b (should be true): same cause. qa's pair went at the stop (logout dissolve) |
| SQ-IP-04 (now SQ-PRT-04) | PASS | 4 -> 2 on qa's logout, new pair 4, 2 after the Nether tp, 4 after a new pair |
| SQ-IP-05 (now SQ-PRT-05) | PASS | distance half (fix_c3a1): 4 at 120 blocks, 2 at 140 after 12 s; `fix_c3a1r` boot r1 heartbeat 2: 2 then 0 five s after the move to 140; boot r2 despawnDistance 0: 2 after 14 s at 300 blocks |
| SQ-IP-05b | PASS | pg's saved blue portal present 17.5 s after Done, 0 at 91 s with the owner offline (60 s beat) |
| SQ-FLR-03 | PASS | millstone drained by a hopper: chest meat=20 bone=15 eye=8 misc=8 other=0, millstone empty |
| SQ-FLR-02b | PASS | rig built by the batch on the fresh world; invalid before the motor swap, valid ok after |
| SQ-FLR-04 | PASS | 100 pus bubbles -> 20 slime balls; 100 hair -> 42 string; nothing else |
| SQ-FLR-05 | PASS | heated half (smouldering burner, coal block -> kindled): flesh 250 mB blood + 1 bone, porous 500 mB, tendons 250 mB, inputs used |
| SQ-BREED-03 | PASS | a bee in the poppy control hive carries FlowerPos 40723 -60 40000 (the wild carrot) and HasNectar 1b; the batch's count line reads only bees outside a hive (BATCH-FAULT on that line) |
| SQ-FDI-02 | PASS | no Carrot hearts: max health 22 = Raspberry Pie 5-food rung; the 1 uncounted of 20 is minecraft:pumpkin_pie, not edible in this pack (c4_ar3 SQ-FDI-02b: food 4 -> 4, bread control 4 -> 9 counted): batch should use another 20th food |
| SQ-FDI-03 | FAIL | both cakes bitten but turn into supplementaries:directional_cake bites=1 (Supplementaries `directional_cake = true`); neither book counts cake_slice (applepieEntries 0): PlacedFoodSol sees a different block after the bite and directional_cake is not in placed_food_slices.json |
| SQ-PWG-90 | PASS | `pwg17r` boot `phase 10.0`, every registry count and 116 recipes; `pwg17r2` (probe prints `blank=` apart): every P10 recipe resolves except deploying/ceiling_tile `unresolved=[1]` = empty #forge:cardboard (expected); no mixin error |
| SQ-PWG-91 | FAIL | B36 still in -17: each ceiling tile attachment break drops the tile OR the attachment (backportedPowerGrid `data/powergrid/loot_tables/blocks/ceiling_tile_{lamp,connector,junction,solar}.json`: one pool, rolls 1, two entries); attach, helper, wrench PASS |
| SQ-PWG-95 | PASS | cord 0 V unlit, 100 V filament 1116 glow 0.42 unlit, 120 V filament 1607 lit with 7 light blocks; stray light blocks cleared (B31) |
| SQ-PWG-94 | PASS | ceiling connector reads exactly like the plain wire_connector control (I 1.1987 A through, T0=0, no solver node): a connector without internals is a line middle in 0.6.2 (upstream design); the row's T0 expects are wrong |
| SQ-PWG-73 | PASS | design table by cord: 20.8 at 100 V unpowered, 35.2 at 130 V powered (light 7); cord steps 1 to 7; menus close |
| SQ-PWG-76 | PASS | `pwg17r2`: P9c recipes all true, no mixin error |
| SQ-PWG-78 | PASS | survival chain 1 (pwg17r and pwg17r2) |
| SQ-PWG-79 | PASS | unetched circuit made on the belt (progress 0.5 then done) |
| SQ-PWG-80 | PASS | `pwg17r2` items before water: acid 250 + water 750; etch: incomplete_circuit with Schematic, acid 1000 -> 750. pwg17r finding: water first lets Creative Delight's heated water-to-salt mixing (2 salt dust) take the water: pack recipe clash a player can hit (TODO candidate) |
| SQ-PWG-81 | PASS | cog -64, arm 64; depot result circuit_board x1 Schematic components=3; parts 8 -> 6 and 8 -> 7 |
| SQ-PWG-82 | INCONCLUSIVE | assembly on the belt works (circuit_board x1 components=3, lockedExternally, B25 stone depot skipped); the arm drops the part stacks loose on the belt (x6, x7) instead of leaving them in their depots: compare with upstream Create arm behaviour |
| CQ-PWG-22 | PASS (automatable part) | ponder scenes (resistor, triode, potentiometer, display module, via, encased fan) play with traces; "Received 28 component items"; board shots BATCH-FAULT (pitch puts them behind the WTHIT tooltip) |
| CQ-PWG-24 | BATCH-FAULT | unpowered-table shot taken after the perfeval teleport (table out of frame); SQ-PWG-73 frame inconclusive; editor steps are owner hand steps |
| CQ-PWG-26 | PASS (automatable part) | acid overlay from inside, survival damage, bucket icon; flow and pool shot spoiled by the perfeval teleport |
| CQ-PWG-27 | PASS (automatable part) | chip in hand and hotbar, boosted drill renders; F5 shot equal to first person (F5 did not take) |
| CQ-PWG-28 | PASS (partial) | showcase: plating connected texture, slabs and stairs, lit ceiling with factory light; string-light shots spoiled by the teleport; tour INCONCLUSIVE (sq92 teleported, sq93 and sq96 aim at bare ground); creative tab BATCH-FAULT (opened on the inventory tab) |
| CQ-PWG-29 | PASS | ceiling_tile and string_lights ponder scenes, tag icon on the four tagged scenes; index tag row not in frame. Upstream models with an undefined #missing face (ceiling_tile_solar_panel, generator/clutch, component/button): load warnings as shipped |

Rows left for the owner (hand steps, `perf/runs/r1114/verdicts/cq_pwg17.md`): CQ-PWG-22 (Circuit Components tag page,
boards from a lower pitch, powered boards), -24 (right-click both tables, editor save/load), -26 (acid flow from
outside, bucket place/pick, JEI), -27 (F5, chip boost, swap hands, zapper + chip, JEI), -28 (tab order, cord tooltips,
string lights at night, wrench and pick-block, B32 lamp drops, JEI), -29 (index tag page and its entries, empty index
slot hover). Client finding (not a row): 420+ jars kill the client at window creation (section 7, 03:10).

### Phase 3 r1113 step 4

Results `perf/runs/r1114/<batch>/`; servers built with `tpf_srv.sh ... shipped @s1` except the reduced sets (cc_only,
cpp_chain: no Debugify / Packet Fixer). Common 1.2.0 (build/libs) on every set.

| ID | Result | Evidence |
|---|---|---|
| SQ-CMG-02 | PASS | c2_l1: both carry lines; trinketCaseAnyItem = false, wearThreshold = 4.5; Equipment's later tables added |
| SQ-CEN-02 | PASS | c2_l1: five carry/merge lines; Compat count 12, PERCENT, tutorial true; creativecommon-swaps.toml present |
| SQ-CFR-04 | PASS | c2_l1: [campfire] merged into CD file, rainOutTicks = 120 |
| SQ-CEN-05s | PASS | c2_l1 rerun: "Killed 12 entities" (first try BATCH-FAULT: pace 1 delayed the kill; batch fixed) |
| SQ-CMG-02b | PASS | c2_l1b: no carry lines; values kept; old Engineering file inert with 31 tables (Compat WARN only) |
| SQ-CEN-02b | PASS | c2_l1b: count 12, PERCENT, tutorial true, CD rainOutTicks 120, Compat has no [campfire] |
| SQ-CFR-03 | PASS | c2_l2: carry line before merge; CD rainOutTicks = 100; Compat [campfire] 0 (qrun FAIL = grep -c exit 1, batch fixed) |
| SQ-DEP-02 | FAIL | c2_nobrew: datapack load fails without Creative Brewing: CD Hearth and Harvest member loot modifier `hearthandharvest:cherries_from_cherry_leaves` names vinery:cherry_leaves / vinery:cherry unguarded. With that file overridden (scratch only) the boot is clean: no Brewin'/Vinery class error from our jars (CCK's own optional mixin WARN), grape rules and the Raspberry Pie ladder skip cleanly |
| SQ-GLD-16 | PASS | c1_g1/g2: old stews and jar fluid gone (jar moonlight:empty, chest keeps golden bread, beef, canned bread, mushroom stew), both gives Unknown item, golden bread nutrition 10, canned bread made, stew eat leaves 2 + bowl, no script error. Cans half not covered: the only stew-era CD jar left (jar_archive/cdns_full, 11-10 01:04) predates the golden cans. qrun FAIL = bootreject matched the batch's own give reply (batch fixed) |
| SQ-CMV-02 | PASS | c3_co: Common alone Done 24 s, reload clean, 0 KubeJS errors, clean stop |
| SQ-CMV-03 | PASS | c3_co2: one "armor_trims 1.4.0 is outside the tested range" WARN, no mixin error |
| SQ-CIF-04 | PASS | c3_cc1: Common + Compat + Calendar + Serene Seasons + KubeJS stack, clean |
| SQ-CIF-04b | PASS | c3_cc2: + Equipment, no extra jar needed, clean |
| SQ-ENGM-05 | PASS | c3_ch1 (chain + Worldgen + its 18 mandatory worldgen mods): transition items 86, 1 failed recipe; no Compat/Delight script error. shell FAIL was Worldgen's `[wild-crops] could not read ... kubejs/generation_scripts/worldgen/biome...` (an instance file, absent from a jar-only set) |
| SQ-CDFO-02 | PASS | c3_ch1: oak_spice_rack and duck_nest AirItem; CfB/Incubation lines are config corrections only |
| SQ-FCK-02b | PASS | c3_ch1: richSoilTicks 10, fertileSoilTicks 5 |
| SQ-MIN-01 | PASS | c3_ch1: no could-not-read; creativecalendar:seasons/arid has 15 biomes incl. minecraft:desert |
| SQ-MIN-03 | FAIL | c3_ch1: `Couldn't parse loot table creativedelight:blocks/placed_corn_popsicle` (item creativedelight:corn_popsicle is registered only with Corn Delight; the Display dish block and loot table are not gated) |
| SQ-MIN-06 | PASS | c3_ch1: none of the three lines |
| SQ-CDFO-03 | PASS | c3_ch2: rack BlockItem with 1 recipe, duck_nest AirItem, 8 CfB mixins from creativedelight.mixins.json |
| SQ-CDFO-03b | PASS | c3_ch2b: rack AirItem, 0 recipes, 0 CfB mixins, no rack merge |
| SQ-CDFO-03c | PASS | c3_ch3 (+ Incubation, Blueprint): incubation on, emu/duck nests and rack AirItem, no error naming the missing mods |
| SQ-ENGM-01 | PASS | c3_b1 (judge, verdicts/c3_b1.md): Done, no mixin or moved-class error (shell grep of Mixing lines targeted latest.log: BATCH-FAULT, fixed) |
| SQ-CDW-02 | FAIL | c3_b1r: located jungle box holds 1135 jungle logs and 0 rubberwood logs / leaves; myrtrees pack "on". The pack rubber tree (creativedelight:wild/rubber_tree, wild_crops.json "rubber" rule, rarity 1 in 4 jungle chunks) never placed; Myrtrees' own placement is removed by the same rule. Suspect: placement on OCEAN_FLOOR heightmap + would_survive sapling under the jungle canopy |
| SQ-CBP-11 | FAIL (owner) | c3_b1r: felsic block 50 rolls = 68 scrap, the loot table gives 1-2; its `__comment` and the row say 2-4. Which is intended is the owner's call |
| SQ-IDS-01 | PASS | c3_b1: owner-id lint 0 flagged; bootreject matched an AE2 INFO "Not registering BaseClass" line (regex fixed) |
| SQ-ENGM-02 | PASS | c3_b1 (judge) |
| SQ-ENGM-03 | PASS | c3_b1 (judge) |
| SQ-ENGM-04 | PASS | c3_b1 (judge); excavation half is client |
| SQ-IDS-02 | PASS | c3_b1 + c3_b1r: new commands answer, old names unknown; 9th unknown was the client-only `/cdevdumpnames`; quoted `/cwglocate batch` runs, status and stop answer |
| SQ-CIF-02 | PASS | c3_b1 (judge) |
| SQ-CIF-03 | PASS | c3_b1: coffee (rusticdelight:coffee) gives Caffeinated and hidden Phantom Bane both 1800 t; rest stat 122 then 123 |
| SQ-MIN-07 | PASS | c3_b1r: ocean (locate: in #creativedelight:fishing_net/shrimp_ocean) 5 shrimp in 30 rolls; river (locate 0 blocks) no shrimp; salmon eggs 0 in 30 river rolls (about 2 % chance at weight 3 of 24; owner may recheck); no loot parse error. c3_b1 first try BATCH-FAULT (rolled at the console origin) |
| SQ-FCK-07 | PASS | glow lichen 1048 (c3_b1) vs 83 (c3_b2 control), 12.6x |
| SQ-FCK-08 | PASS | desert box: hamimelon 17 vs 1, Mini Cantaloupe 9 vs 2 (c3_b1/c3_b1r vs c3_b2) |
| SQ-FCK-08b | INCONCLUSIVE | badlands box: hamimelon 4 vs control 6, Mini Cantaloupe not read on c3_b1r, 0 on control; counts too small to show 2.5x |
| SQ-HSC-89 | PASS | c3_b1: 1304 raw rock salt in the desert box |
| SQ-PST-12 | PASS (row stale) | c1_b1: jungle summer 0.97, winter 0.76 of plains spring; current code makes grass neutral in wet/dry biomes (CropSeasons GRASS, crops #28), so the row's 0.35-0.70 is superseded; owner to confirm and update the row |
| SQ-CDW-01 | FAIL | c3_cdw1: without myrtrees the datapack load fails (`Unknown registry key ... myrtrees:rubber_trunk_placer`) from the instance `kubejs/data/creativedelight/worldgen/{configured,placed}_feature/wild/rubber_tree.json` (ungated duplicate of CD's packs/worldgen/myrtrees). With them removed (scratch only): Done, off [myrtrees] |
| SQ-CDW-03 | PASS | c3_cdw1 rerun: exactly `rubber: needs myrtrees` |
| SQ-CDW-01b | PASS | c3_cdw2: Done, off [fruitsdelight]; qrun FAIL = corn_delight GLM WARNs also in the full pack (regex fixed) |
| SQ-CDW-03b | FAIL (minor) | c3_cdw2: 17 skip lines, not 1: 16 fruitsdelight tree/bush rules in wild_crops.json name absent features without `requires` (WARN spam only, no error) |
| SQ-CDW-01c | PASS | c3_cdw3: Done, no datapack error; endersdelight has no pack of its own, so nothing to list under off (row expectation stale) |
| SQ-CDW-03c | PASS | c3_cdw3: no skip line |
| SQ-NSM-08a | PASS | c4_b: no structure, dimension or old-namespace error; [bee-nests] 6 rules / 44 biomes and [mob-spawns] 20 rules / 95 biomes summaries, 0 idle |
| SQ-RD-10 | INCONCLUSIVE | c4_br (c4_b capture BATCH-FAULT: waitlog took rerun_b's pending corn maze reply): farm line "63 pool elements of 54 templates"; two savanna villages (272 -1312, 880 2640) carry warm_dry crops (tomatoes, cabbages, onions, peanut, corn, tea) but no cotton; cotton is 1 of 11 crops per rolled field, too rare to show in two villages |
| SQ-NSM-08 | PASS | c4_b + c4_br: 11 creativeworldgen dimensions load full chunks; dimensions/ has creativeworldgen, ae2, l2library (no creativeengineering); level.dat's 641 creativeengineering strings are Forge registry aliases only; sky_island, meta/waystone_ruins (c4_br; c4_b used `#` on a structure id: BATCH-FAULT) and a settlements member (village_savanna) locate; census (c4_br, radius 32) lists 16 group sets; 0 worldgen/calendar tags under creativeengineering. Flower Test preset is client (CQ) |
| SQ-GAP-03 | PASS | c4_b: 5 Bushier Flowers modifiers each forge:none; 0 bushierflowers blocks in 1444 pregenerated chunks |
| SQ-HH-328 | PASS (finding) | c4_br with searchTimeoutSeconds 600 (scratch only): corn maze found from spawn after 424 s, 10612 blocks away, 1681 candidates; generated with corn 350, oak fence 8 (row: 16; the 38-block box may clip), Lootr centre chest with table corn_maze_center. rerun_b: three locates hit the shipped 120 s deadline (BATCH-FAULT for the row's longer budget). Finding: with the shipped config a player's /locate and maps cannot reach a corn maze from this spawn |

Phase 3 totals: 49 rows, PASS 41 (SQ-PST-12 row stale, SQ-HH-328 with a finding), FAIL 6 (SQ-DEP-02, MIN-03, CDW-02, CDW-01,
CDW-03b minor, CBP-11 owner call), INCONCLUSIVE 2 (SQ-FCK-08b, RD-10), BATCH-FAULT 0 left (all rerun), not run 0.

### Phase 4 SQ-CC12

Common 1.2.0 (build/libs 11-13 23:12) in scratch srv only. Results `perf/runs/r1114/cc12_0N/`.

| ID | Result | Evidence |
|---|---|---|
| SQ-CC12-01 | PASS | cc12_01 (Common + its optional mods): no mixin / class / out-of-range error, no ChatOpen, SwapReport or SwapCapabilityKeys error, no client mixin on the server; IW callTick and Immersive Portals lines; swap summary carries "0 capability key(s)"; 22 of 22 files, 0 startup errors; stop: every listed id resolved |
| SQ-CC12-02 | PASS | cc12_02 (Common + Compat + Calendar + Serene Seasons, no config, empty kubejs): boots, /reload, stops clean; config written; 0 KubeJS errors from Common (plantabletags.js, smokable_tags.js silent). qrun FAIL = Creative Labels probe from the scratch setup (tooling, fixed) |
| SQ-CC12-03 | PASS | cc12_03 (full pack, group A): no mixin or class error from our jars; 739 permanent aliases, 4 capability keys, no unresolved; Common 22 of 22, 0 startup ERROR; stop: every listed id resolved |

Phase 4 totals: PASS 3, FAIL 0.
