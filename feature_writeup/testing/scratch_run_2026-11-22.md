# Scratch run 2026-11-22: r1122cc (Creative Common and Creative Compat rows)

Targeted run (skill `scratch-test` part B, owner request 2026-11-22): the pending server rows of the Creative Common
and Creative Compat features only. Driver `qrun.py drive start r1122cc --plan-json /tmp/r1122cc/plan.json`, results in
`perf/runs/r1122cc/`.

## 1. Scope

- Queue codes, from `feature_writeup/status/features.csv`: Creative Compat (CMP, BPT) and the Creative Common codes of
  the "Creative Engineering and Creative Common" row (CC, CC12, CCB, CCO, CGE, CLOG, CMV, CPP, SPFX). CEN (Creative
  Engineering's own rows) not taken.
- Plan: `qrun.py plan --never --failed --code CMP,BPT,CC,CC12,CCB,CCO,CGE,CLOG,CMV,CPP,SPFX` = 77 server rows
  (jar-changed 62, failed 13, never 1; phases: server 75, worldgen 1 = SQ-SPFX-07, long 1 = SQ-CMP-12). No client rows
  in the plan (client rows not planned without `--client`; none taken). BPT, CCB, CLOG had no pending server rows.
- 69 of the 77 rows are stale (sources changed after `@reviewed`); run as written, not rewritten in this run.

## 2. Tools

`qrun.py drive` (28 steps), `qprescreen.py --plan-json`, `scratch-judge` for LOOKs. Perf capture skipped (targeted run,
run default 4).

## 3. Jars

| jar | `mods/` | build/libs (used, swapped in by prep_srv.sh) |
|---|---|---|
| creativecommon | 1.2.0+a4baff40.2611212233 | 1.3.0+23c5050d.2611220225 |
| creativecompat | 1.0.0+ad7812b3.dirty-24f070.2611211309 | 1.0.0+23c5050d.2611220225 |

Also built and not installed (`jarstamp.py pending`): creativeBrewing, creativeCalendar, creativeDelight, creativeLoot
(all 23c5050d.2611220225), swapped in the same way.

## 4. Preflight

- `game_jvms.sh` empty, RUNNER.md idle, `df -h /` 92 GB free.
- `check_all.py --fast`: 40 run, 4 FAIL, all code lints (comment_dates 9, guard_check 8, spelling_code 9, tab_indent 7
  files). No runtime test failed; the lints need edits under `development/` (not allowed in a run), reported, run went on.
- `qprescreen.py`: 11 likely findings. Fixed: SQ-CGE-04 (End fill at -2 60 -2 with no forceload: added
  `forceload add -2 -2 10 2` in the End; lint clean). Left as false positives: SQ-CC-08, -16, SQ-CCO-07, -11 (bot
  teleports, the bot loads the chunks), `forge:entity_gravity` in SQ-CGE-06, -09a, -09b, -11 (an attribute, not in the
  registry dumps), `create:kjs_` in SQ-SPFX-03 (a prefix in a regex). SQ-SPFX-07 `creativeworldgen:sky_island` not in
  the dumps: run as written (it is what the row checks).

## 7. Run notes

- 05:10 drive started, 28 steps, 77 rows, ETA 08:13.
- 05:13 step 1 (CC group B, 1 row) done.
- 05:17 step 2 (CC group A) boot hit the known Rhino tag-load flake (`Failed to load datapacks`, NPE `frame is null`,
  gotchas.md Boot and shutdown). The driver's reboot-once path then crashed with a tooling bug: `qdrive.py`
  `boot_and_run` built the kept-log path as `Path / 'str' % int` (TypeError) and reused the name `keep`, so the retry
  would have gotten a Path as the keep-forceloads flag. Environment fault, fixed in `tools/scratch_query/qdrive.py`
  (parenthesized, renamed to `kept`); `drive resume r1122cc` at 05:17, step 2 rerun from its start.
- 05:19 to 06:16 steps 2 to 23 ran without driver errors.
- 06:27 step 24 (SQ-CPP-04, mod set `cpp_chain chain`) failed to boot: FML `Mod creativebrewing requires creativeloot
  1.0.0 or above` (and creativedelight), crash report 06:17:08. Environment fault: the manifest predates the Loot carve
  (`port_chain.toml` got the same fix 2026-11-20). Fixed: `creativeloot-*` added to the `chain` group of
  `tools/scratch_query/modsets/cpp_chain.toml`; the `compat` variant (step 25) has no Delight or Brewing and is not
  affected. SQ-CPP-04 added to `perf/runs/r1122cc/rerun.txt`. Note: the driver waited the full 600 s although the
  crash report was written 6 s after launch (tooling gap, not fixed in-run).
- 06:30 to 06:45 three `scratch-judge` agents judged steps 1 to 25 while the drive ran (judge-cc: CC.qb, CC12.qb;
  judge-cco: CCO.qb, CGE.qb; judge-cmp: CMP.qb, CMV.qb, CPP.qb; no boots). BF-FIX lines (all rerun in r1122ccr):
  - BF-FIX SQ-CC-06: kjsreject `rror` matched `mirror_glass` -> word-bounded `(?i)\b(error|exception)\b`.
  - BF-FIX SQ-CC-10: prep_srv has `spawn-animals=false`, summoned cows discarded -> NoAI iron golems, box raised.
  - BF-FIX SQ-CC-11: the `effects removed` toml edit was only in `@commands` -> `qrun.py toml` step + `sleep 4`.
  - BF-FIX SQ-CC12-01, SQ-CCO-01: bootreject matched the `creativedelight:` id in the idleRandomTickRules WARN,
    kjsreject matched the devtools jar's own sequence-assembly script errors -> CCO.qb line 33 / 38 filters.
  - SQ-CC-01: jar pick `head -1` took 1.2.0 over 1.3.0 -> `sort -V | tail -1`; judged PASS by hand on 1.3.0, no rerun.
  - BF-FIX SQ-CCO-08, SQ-CCO-10: cc_only set boots peaceful, zombies despawned, phantoms 0 -> `difficulty normal`
    (restored after); CCO-08 also level-dir glob instead of `cco1/`.
  - BF-FIX SQ-CGE-04: `data get block` on sand (not a block entity) errors -> `execute if block ... sand run say`.
  - BF-FIX SQ-CMV-04: `packOrder`, `tutorial`, `advancement` moved to Compat's toml, absent on `cc_only alone` ->
    row checks Common's own keys. Coverage gap: the Compat defaults are now checked by no row (see 9).
  - BF-FIX SQ-CGE-09a, -09b, -14 (runner): section 2 groups `b1`..`b5` were read by `qdrive.world_letter` as world
    group B, so the section's `@@prep A:` boot 2 / boot 3 lines were skipped (s15 ran on a normal world, default
    gravity config) -> groups now start with `A` (`A b1 ...`, only CGE.qb used this form); 09a also `sleep 15` after
    the Nether forceload (dropall saw entities=0 while chunks generated).
  - SQ-CMP-01 BATCH-FAULT, not simple: the row expects up to 27 Compat mixins; `creativecompat.mixins.json` lists 86,
    76 applied, none doubled with Engineering; creative_compat.md line 169 says 32. Docs and code disagree: owner.

- 06:41 main drive done (27/28 steps; step 24 never booted). Rerun drive `r1122ccr` started on
  `perf/runs/r1122cc/rerun.plan.json` (13 rows in 7 steps, plus the lower-boot rows qrerun brings along), 07:01 done.
  judge-spfx judged steps 26 to 28; judge-rerun judged r1122ccr (no new BATCH-FAULT, no FAIL).
- 07:01 all JVMs stopped, RUNNER.md idle (released by the drive).

## 8. Final verdicts

77 rows, latest verdict per row (`qrun.py table perf/runs/r1122cc/s*_res perf/runs/r1122ccr/s*_res`): **PASS 69,
FAIL 4, BATCH-FAULT 1, INCONCLUSIVE 3.** SQ-CC-03 prints HARNESS (the shell exit was its only failed check) but its
look is a product FAIL (`--reclassify` cannot set FAIL); it is counted as a FAIL. Main drive before reruns: PASS 57,
BATCH-FAULT 13, FAIL 4, INCONCLUSIVE 2, not booted 1; the 13 reruns: PASS 12, INCONCLUSIVE 1.

FAILs (product, not fixed in the run):
- SQ-CC-03, SQ-CC12-02, SQ-CPP-05: on sets without Immersive Weathering, `[NCH] shapeless sand_layers_from_sand` and
  `red_sand_layers_from_red_sand` output undefined/null; `kubejs/data/creativecompat/kubejs/other/misc_mods.js` lines
  54 to 61 (6c2743d4) have no Immersive Weathering guard. TODO `blockrunner #18`.
- SQ-SPFX-06: `[item_dict] Missing/invalid item IDs: canned_legendary_roll_medley, canned_mythical_roll_medley`
  (Creative Common `_lib/item_dict.js` lines 12777-12778; the cans were dropped by ruling, ROLL-27). TODO `mod-updates #3`.

BATCH-FAULT, not simple: SQ-CMP-01 (mixin count: row 27, writeup creative_compat.md 32, `creativecompat.mixins.json`
86 listed / 76 applied, none doubled with Engineering; owner to settle the expected count before the row is rewritten).

INCONCLUSIVE: SQ-CC-10 (the Better Beacons phantom half lives in `batches/r1107cc/b1_phantom.qb`, not run);
SQ-SPFX-01 (second boot without Immersive Portals not scripted in the row); SQ-SPFX-03 (half b, the port chain p1 boot
`run_boots.sh '^p1$'`, not scripted). These need row rewrites to become runnable.

| ID | Result | Evidence |
|---|---|---|
| SQ-CC-01 | PASS | mods.toml of the 1.3.0 jar (checked by hand; row picked 1.2.0 via ls/head -1, now sort -V/tail -1): immersive_portals and heartstone ordering=NONE, mandatory=false; no moved files, 0 mixins without class (judge-cc) |
| SQ-CC-02 | PASS | 6 checks passed |
| SQ-CC-05 | PASS | 5 checks passed |
| SQ-CC-06 | PASS | ccdict lines print ids for createframed and securitycraft keys and tag lines; no exception, no error lines |
| SQ-CC-07 | PASS | 5 checks passed |
| SQ-CC-08 | PASS | 6 checks passed |
| SQ-CC-10 | INCONCLUSIVE | in-batch half passes (poison 87.2>84, wither 92>90 on NoAI golems); the Better Beacons phantom half lives in batches/r1107cc/b1_phantom.qb, not run here |
| SQ-CC-11 | PASS | 6 checks passed |
| SQ-CC-13 | PASS | jfr summary lists creativecommon.ServerTick, LevelTick, ChunkWaitStats/ChunkWait, KubeJSHandlerStats/KubeJSHandler (judge-cc) |
| SQ-CC-14 | PASS | 6 checks passed |
| SQ-CC-15 | PASS | gravity grep: only Portal 'Gravity API not present' INFO and Create Gravity config reload lines; no mixin error; gravityLimitDimensions on (judge-cc) |
| SQ-CC-16 | PASS | Done (10.063s); Preparing spawn/Generating count 12 before and after the teleport to 50000; whistle arrow is a CQ check (judge-cc) |
| SQ-CC-03 | HARNESS | shell exit 1: ; FAIL: ls count 0, stop clean, but logs/kubejs has ERROR lines: NCH shapeless sand_layers_from_sand / red_sand_layers_from_red_sand output undefined/null (Immersive Weathering absent) (judge-cc) |
| SQ-CC-04 | PASS | 5 checks passed |
| SQ-CC12-01 | PASS | stale version look, judged on content: jar creativecommon-1.3.0 only (plus devtools); [swaps] summary with 0 capability keys; 'creativecommon: 25 of 25 files loaded'; 0 startup ERRORs; no client mixin lines |
| SQ-CC12-02 | FAIL | config written, server.log 0 errors, no plantabletags/smokable_tags error, but startup.log has 4 [ERROR]: NCH shapeless sand_layers_from_sand / red_sand_layers_from_red_sand output undefined/null (same as SQ-CC-03) (j... |
| SQ-CC12-03 | PASS | mods: creativecommon-1.3.0; class errors only third-party (sodium, backpacked, multimine, aquaculture, botarium, overweightfarming, create_dd XrayParticle); no out-of-range WARN; 25 of 25 files loaded; 0 startup ERROR... |
| SQ-CCO-01 | PASS | Done (26.051s) present; startup.log 'creativecommon: 25 of 25 files loaded from its jar'; all bootreject/bootexpect/kjsreject checks ok |
| SQ-CCO-02 | PASS | 4 checks passed |
| SQ-CCO-03 | PASS | fill 25 blocks succeeded, CCO03 fill done; fill stands as the check |
| SQ-CCO-04 | PASS | shell: config present, overrides=[] (l114), removed=[] (l139), gravityLimitDimensions=true, [advancements] enabled=true, [creative_menu] disabled=false |
| SQ-CCO-05 | PASS | mossy_cobblestone count 1 -> 85 after 1200 ticks at randomTickSpeed 300 |
| SQ-CCO-06 | PASS | 3 checks passed |
| SQ-CCO-07 | PASS | CCO07 start 05:32:20.3, arrived 05:32:24.3: 4 s |
| SQ-CCO-08 | PASS | End zombie at y 141.6 after ~6 s from y 200 (slowed fall); overworld zombie gone (fell at vanilla speed, died from fall damage); biome_configs #minecraft:is_end set. Modifier is transient and never shows in data get A... |
| SQ-CCO-09 | PASS | pig, cow and whistling arrow summoned, no NEA/whistle ERROR line |
| SQ-CCO-10 | PASS | 5 checks passed |
| SQ-CCO-11 | PASS | overwrites conform 1, Static binding violation 1 (designed) |
| SQ-CCO-15 | PASS | creativecommon: 25 of 25 files loaded; ccdict lines show tags/ids, no exception (14 'error' lines in server.log are all creativedevtools sequence_assembly_processes) |
| SQ-CCO-16 | PASS | wandering trader offers: 6 vanilla trades plus Quark pathfinders_quill, no pack trade ids; advancement granted |
| SQ-CCO-12a | PASS | boots clean: 25 of 25 files, 0 startup ERROR; mixin list differs from the full set only by the absent mod's mixins; effects apply |
| SQ-CCO-12b | PASS | boots clean: 25 of 25 files, 0 startup ERROR; mixin list differs from the full set only by the absent mod's mixins; effects apply |
| SQ-CCO-12c | PASS | boots clean: 25 of 25 files, 0 startup ERROR; mixin list differs from the full set only by the absent mod's mixins; effects apply |
| SQ-CCO-12d | PASS | boots clean: 25 of 25 files, 0 startup ERROR; mixin list differs from the full set only by the absent mod's mixins; effects apply |
| SQ-CCO-12e | PASS | boots clean: 25 of 25 files, 0 startup ERROR; mixin list differs from the full set only by the absent mod's mixins; effects apply |
| SQ-CCO-12f | PASS | boots clean: 25 of 25 files, 0 startup ERROR; mixin list differs from the full set only by the absent mod's mixins; effects apply |
| SQ-CGE-01 | PASS | Done (4.008s) at boot, no mixin error/WARN naming the Create Gravity mixins |
| SQ-CGE-02 | PASS | End 4.914 vs overworld 24.570 at t=40 (ratio 0.200) for falling_block, item, tnt; cge_judge PASS |
| SQ-CGE-03 | PASS | item and TNT 4.914 vs 24.570 (ratio 0.200), same lines as SQ-CGE-02 |
| SQ-CGE-04 | PASS | item y=61.000 stable t=120..180, falling_block removed=true, tnt y=61.000, 'CGE04 sand landed at 0 61 0' |
| SQ-CGE-05 | PASS | End t=40 drop 0.000 for all three (cfg factor=-1); overworld 24.570 = vanilla by gravityLimitDimensions (documented), cfg shows plains -0.8 |
| SQ-CGE-06 | PASS | End 4.914 vs overworld 24.570 (0.2); zombie, villager, bot entity_gravity 0.016 |
| SQ-CGE-07 | PASS | four ok lines: order ok, helper line ok, jeiOrder ok, 0 startup errors |
| SQ-CGE-08 | PASS | cge_judge PASS low the_end vs overworld at t=40: 9 types, ratio 0.200 each |
| SQ-CGE-09a | PASS | cge_judge PASS same nether vs overworld, 9 types ratio 1.000; Nether villager gravity 0.08; config lists is_nether;; |
| SQ-CGE-09b | PASS | 4 checks passed |
| SQ-CGE-10 | PASS | Air 300s then 288s after 12 s (3 steps of 4 per 5 s; 296/292 in the look is the 1-2 step case), no backtank |
| SQ-CGE-10b | PASS | Leather Backtank worn: Air stays 300 (supplied), backtank Air NBT 890 about 10 s after 900 start (about 1 a second) |
| SQ-CGE-11 | PASS | 2 checks passed |
| SQ-CGE-12 | PASS | [65958, 61000]: log 65.958 >= 64.5, redstone 61.000 <= 61.5; startup names creativecompat |
| SQ-CGE-13 | PASS | boat heights End 65.523 and overworld 65.523, equal (<=0.05); boat entity origin sits about 0.4 below the 65.9 surface (log floats at 65.958) |
| SQ-CGE-14 | PASS | 4 checks passed |
| SQ-CGE-15 | PASS | order ok (no _helpers), 0 startup errors, 9 types ratio 0.200, backtanks n=2 |
| SQ-CGE-16 | PASS | 9 types ratio 1.000 in End and overworld, no LowGravity/NoClassDefFound line |
| SQ-CMP-01 | BATCH-FAULT | reclassified from FAIL: judge-cmp, 2026-11-22, row says up to 27 common mixins; Compat json now lists 86 (76 applied here, none doubled with CE), writeup creative_compat.md says 32: docs and code disagree, not simple;... |
| SQ-CMP-03 | PASS | startup.log: jar-startup creativecompat 3 of 3 files loaded, pack in order, [item_physics] canonical tags summary line, 0 ERROR |
| SQ-CMP-04 | PASS | [dumpphysics] 20522 of 20522 items, 1416 survive lava, loads com.SaintAlfonzo.creativecompat.itemphysic.ItemPhysicSplit, no error |
| SQ-CMP-05 | PASS | CMP tnt item burned; CMP fireproof item survived (netherite stays); fire still burning |
| SQ-CMP-06 | PASS | box placed, given, monster_box_reward loot dropped 6 Cobweb, no Unknown block/loot; item_tags.json has creativecompat:physics/{fireproof 397,floats 1089,lavaproof 578,sinks 8} |
| SQ-CMP-07 | PASS | no immersive paintings error/fail line in latest.log; kubejs/data present; count baseline is a client check (2026-11-01 note) |
| SQ-CMP-08 | PASS | 0 Error occurred while handling event, 0 ReferenceError in server.log before and after /reload; all moved creativecompat scripts loaded |
| SQ-CMP-11 | PASS | GT 411; c11b DeathTime 491L (near game time, not 0); c11a Stage 1b, c11b 0b |
| SQ-CMP-13 | PASS | GT 2238; Stage lines in order 1b, 2b, 0b |
| SQ-CMV-02 | PASS | Reloaded with no KubeJS errors; 0 [ERROR] in kubejs logs; no NoClassDef/Mixin error; after Stopping server only chunk-save lines, no exception |
| SQ-CMV-04 | PASS | config: advancements enabled=true; wandering_trader enabled=false, 24000/5/1; creative_menu disabled=false |
| SQ-CMV-03 | PASS | 4 checks passed |
| SQ-CPP-01 | PASS | metal_dusts n=15 (IE dust_*, zinc_grit); bark n=20 (immersive_weathering); 2 tag failures both creategoggles mekanism:mekasuit_helmet (third party, as 2026-11-08 PASS); removed holds 4 (3 + alexsmobs:fear, N22) and th... |
| SQ-CPP-02 | PASS | zsa result echo_shovel enchantments=true name=true steps=6; selftest all fail 0 |
| SQ-CPP-03 | PASS | bottle recipe creativebrewing:iebottling/create_potion/minecraft_swiftness_regular -> swiftness potion; refinery lingerification/minecraft_swiftness_from_splash -> Bottle LINGERING |
| SQ-CPP-05 | FAIL | startup.log 2 [ERROR] NCH shapeless sand_layers_from_sand and red_sand_layers_from_red_sand output undefined (Immersive Weathering absent in cpp_chain compat); no scrap error, floats tag n>=1 |
| SQ-SPFX-01 | INCONCLUSIVE | first boot: Done, one [compat] conform line, one Static binding violation WARN for m_183760_ in MixinChunkMap_C, no Apply failed (all pass); second boot (IP and Portal Gun moved out) was not run by the batch (single b... |
| SQ-SPFX-02 | PASS | rdetail lists the recipe with serializer create:filling step1 and 7 steps, result brewinandchewin:horror_lasagna*2, errlines=0; filling step is only built under global.has(f.tomato_sauce) (brewinandchewin/recipes.js:8... |
| SQ-SPFX-03 | INCONCLUSIVE | (a) PASS: create:kjs_ n=0, zz_probe_good listed, zz_probe_empty_fluid logged once, bootexpect/bootreject passed; (b) port chain p1 boot was not run by this batch (separate chain boot), unverified |
| SQ-SPFX-06 | FAIL | boot log: /\[(item/block/fluid)_dict\] Missing/ matched: [22Nov2026 06:28:47.321] [modloading-worker-0/WARN] [KubeJS Startup/]: creativecommon:creativecommon/_lib/item_dict.js#14973: [item_dict] Missing/invalid item I... |
| SQ-SPFX-07 | PASS | locate: nearest creativeworldgen:sky_island at [-480,~,512], 611 blocks; 2706 grass_block in y100-250 within 80x80 at the spot (2325 in y0-99, ground, for the record); structure_tag_report.md lists it in group/sky (li... |
| SQ-CMP-12 | PASS | 4 checks passed |
| SQ-CPP-04 | PASS | 0 'Error parsing recipe'; swiftness bottle recipe creativebrewing:iebottling/create_potion/minecraft_swiftness_regular; 10 KubeJS ERRORs all creativedevtools sequence_assembly_processes.js#58 (devtools-jar script, sam... |

## 9. Handover

- Not run: client rows (none planned for these codes without `--client`); CEN (Engineering) out of scope.
- Owner: SQ-CMP-01 expected mixin count. Coverage gap: Compat's `packOrder` / `tutorial` / `advancement` defaults
  moved to `creativecompat-common.toml` and no row checks them now (SQ-CMV-04 narrowed to Common's keys).
- Row text worth a rewrite pass (stale, judged on content): SQ-CC12-01 look names creativecommon-1.2.0; SQ-CCO-08 look
  reads the modifier from `data get Attributes` (never shows); SQ-CGE-10, -13 look values (288 Air, boat 65.523).
- 69 of 77 rows are still marked stale (sources changed after `@reviewed`); not rewritten here.
- Jars: creativeCommon 1.3.0 and creativeCompat 1.0.0 at 23c5050d.2611220225 (also Brewing, Calendar, Delight, Loot)
  are built and not installed (`jarstamp.py pending`); this run tested those builds.
- Tooling fixed: `qdrive.py` Rhino-flake retry path; `modsets/cpp_chain.toml` + creativeloot. Open tooling gap: the
  driver waits the full 600 s when FML writes a crash report at boot.
- `check_all.py --fast` had 4 code-lint FAILs (comment_dates, guard_check, spelling_code, tab_indent), untouched.
