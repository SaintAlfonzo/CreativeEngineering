# Scratch run 2026-11-15 (r1115)

Owner request 00:10: "act as the scratch test orchestrator; coordinate with the passthrough agent and give its work
priority; run the tests in order; pass bugs to agents to fix when it would not change behaviour, note otherwise."
Main session (creativeengineering2-82 [319bcc]) as orchestrator; each phase runs in one runner subagent (one at a time,
one game JVM at a time). Runners append to sections 7 and 8; the orchestrator keeps sections 1, 4 and 9 current.

## 0. Performance capture

Perf session `r1115`, opened by the orchestrator at 00:14 before the first boot; left open between phases; closed and
reported (no `--prune`) after the last phase.

## 1. Scope

Passthrough (SQ-PTH / CQ-PTH): owner priority, but the pass-through orchestrator (session "#Passthrough plan
execution") asked at 00:13 to run none of them yet: phases 2 to 5 are still being built into the same Compat jar,
real tier tags come in phase 4, no scratch datapack. It messages this session when phase 5 is built; the PTH rows then
run next, ahead of whatever phase is pending (current phase finishes at a row boundary). Compat code under
`creativecompat/surface/`, the pass-through mixins, `CompatConfig.java`, `creativecompat.mixins.json`,
`CreativeCompat.java` are owned by that session: a Compat fix for a non-PTH row is messaged there first, and
`:creativeCompat:` is not built or installed by this run.

Taken, in queue order (everything added or rewritten since r1114 ended, 11-14 12:08, then the older server rows the
11-14 22:30 coverage audit lists as not run):
1. Rows rewritten in place 11-14 (queue header "agent misc" line): SQ-TPF-00, -01; SQ-PG-01a, -01b, -01c; SQ-GAP-04;
   SQ-T1-04; SQ-OB-05; SQ-CBP-11; SQ-HH-328; SQ-VH-06, -06b, -06c (fresh Nether); SQ-LGM-13; SQ-CDW-01, -03b.
2. SQ-PWG Power Grid P11 fixes (SQ-PWG-97, -80b, -54r; rewritten SQ-PWG-06, -43, -80, -82, -91).
3. SQ-MELT Melting system (SQ-MELT-01 to -10, -P1, SQ-SALT2-06, -06b, SQ-AZW-01, SQ-DCM-02).
4. SQ-SPD / SQ-SLK and Creative Delight fixes (server rows and the listed reruns).
5. SQ-HOP hops, tilled end stone, stomping basin (server rows and the listed reruns).
6. SQ-SY wave 2 (SQ-SY-14 to -22).
7. SQ-DEP optional-mod guards (SQ-DEP-09a, -09b drop-one boots; reruns SQ-DEP-02, SQ-MIN-03, SQ-CDW-01, -03b).
8. SQ-QN Quark Mending and elytra nerfs off.
9. Older server rows not yet run (audit list): SQ-RP-07 (now SQ-RPI-07), -08, SQ-VH-02 to -11 (rest), SQ-FLR-02, SQ-WO-15 to -25,
   -31, -33, SQ-OB-02, -07, -08, -10, -11, SQ-HCS-10, SQ-BREED-01, SQ-FDI-04, SQ-CDNS-05, -10, -12, SQ-NSM-04, -05,
   SQ-RDB-03, -05.
10. Client: CQ-FDR (jar handle leak gate; the 420+ jar client crash). If it passes, the client rows of phases 1 to 8.
11. SQ-PTH-01 to -04, CQ-PTH-01, -02 when the pass-through session releases them (may move ahead, see above). Note at write-back: PTH pending pass-through phase 5.

Not taken: client rows pinned until wave 8+ (queue PINNED note, 2026-11-01); SQ-WO-34 (inotify limit, owner).

Bug policy (owner): a FAIL whose fix does not change behaviour (crash, error, missing lang, tooling, batch fault, code
not doing what its writeup already says) goes to a fix agent; anything needing a value or design choice is noted as a
TODO row for the owner. Fix agents build only; the orchestrator installs between boots (no JVM up) and queues a rerun.

## 2. Tools

As r1114. `prep_srv.sh` gained `SKIPSWAP="mod ..."` (00:15). This run uses `NOSWAP=1` on every boot: every build jar
equalled `mods/` at 00:14 (Common and Devtools 00:07 builds byte-identical), and the Compat build in build/libs is the
pass-through work in progress.

## 3. Jars

`mods/` at 00:14, all installed 11-14 22:40 to 23:55 (Common 1.2.0 23:53, Compat 23:54, Delight 1.1.0 23:28, Brewing
23:29, Engineering 23:54, Worldgen 22:40, Storage 23:29, Power Grid -18 23:55, solRaspberryPie 23:55). Not used:
`development/creativeCompat/build/libs/creativecompat-1.0.0.jar` 00:07 (pass-through phase 1 WIP).

## 4. Order

| Step | Phase | Runner | State |
|---|---|---|---|
| 1 | rewritten rows | runner-1 | done 00:54: PASS 15, client-deferred 3 (SQ-PG-01b, -01c, CQ-PG-03) |
| 2 | PWG P11 | runner-2 | done 01:18: PASS 8, FAIL 1 (SQ-PWG-82), client-deferred 1 (CQ-PWG-66) |
| 3 | MELT | runner-3 | done 02:24: PASS 15, FAIL 2 (SQ-MELT-08, -10, owner) |
| 4 | SPD / SLK | runner-4 | done 03:16: PASS 25, FAIL 2 (SQ-SPD-08, SQ-BBQW-02s), deferred 4 |
| 5 | HOP + SQ-MELT-09b, -P1b | runner-5 | done 04:35: PASS 14 (P1b live-toml clause inconclusive), FAIL 3 (SQ-HOP-04, -05, -06/SQ-HH-304), deferred 2 (SQ-HOP-01 to phase 7, CQ-HOP-01) |
| 6 | SY wave 2 + CD fix reruns | runner-6 | done 05:02: PASS 11 (SPD-08b, BBQW-02t, -02, SLK-01c, SY-14 to -21), deferred 1 (CQ-SY-22) |
| 7 | DEP + SQ-HOP-01 | runner-7 | done 06:15: PASS (09a, 09b x6 + supplementaries pair, DEP-02, HOP-01, MIN-03, CDW-01, -03b), SKIP 4 Forge cascades + quark pair, FAIL 1 extra (UA pair, owner note) |
| 8 | QN | runner-7 | done 06:15: PASS 2 (QN-02 hand feel deferred) |
| 9 | older server rows | runner-9 | done 07:13: PASS / RECORD on every runnable row, no real FAIL; client-deferred SQ-OB-02, SQ-CDNS-12, SQ-NSM-04, -05 render |
| 10 | client (CQ-FDR gate first) | runner-10 | done 08:18: gate PASS (CQ-FDR-01 to -04); PASS 8; FAIL 1 (SQ-PG-01c native crash); finding SQ-PG-01b; INCONCLUSIVE CQ-PWG-66; LOOK owner CQ-PG-03, SQ-QN-02 feel; NOT RUN SQ-NSM-04 (fixture) |
| 11 | PTH: SQ-PTH-01 to -28, CQ-PTH-01 to -05, SQ-HOP-05 rerun (Compat 11:31, Devtools 00:40 build jars; perf session r1115pth) | runner-11 | done: PASS 27, FAIL 6 |
| 12 | PTH fix reruns: SQ-PTH-03b, -06b, -11c, -25b, -28b, CQ-PTH-03b (Compat 14:59, Devtools 14:39 build jars, copies in perf/runs/r1115/pth_jars2/; perf session r1115pth2) | runner-11 | STOPPED 15:01 by the owner before the first boot; all six rows deferred, resume point in 7 "Phase 12" |

## 5. Row map

Batches in `tools/scratch_query/batches/r1115/<phase>_*.qb`; results `perf/runs/r1115_<phase>_<boot>/`.

## 6. After the run

Done 2026-11-15 by the write-back agent (documentation only, nothing booted, built or installed):
- Queue: Run log entry r1115 and header line; "Latest" pointer moved to r1115. Row texts fixed from section 9:
  SQ-HH-328 (census box and forceload centred on the maze), SQ-PWG-54r (`controller=none` not a criterion), SQ-PWG-06
  (thyratron heater 6.5 V), SQ-SPD-01, -02 (24 planted, itemCount 6), SQ-HH-482 (cow b note), SQ-SY-14 (`35 crops`),
  SQ-SY-15 (sweet potato ripe `age=5`), SQ-SLK-01c (air also passes), SQ-DEP-09a (pancakes 4 + bowl), SQ-DEP-09b (four
  cascades named, Quark through Botania), SQ-DEP-02 (Keg mixin, item_dict and grape bush WARNs expected), SQ-HOP-01
  (item_dict WARN expected), SQ-QN-01, -02 (bot via `zqn`, feel is client), SQ-OB-08 (no Nourishment), SQ-OB-11 (swap
  summary line), SQ-OB-02 (client row), SQ-RDX-13 (needs a pre-11-02 save), SQ-BREED-01 (`zp9 breed` until
  `/dumpbreeding`). SQ-PTH rows untouched.
- Writeups (results line in each test list): melting.md 7, plan_power_grid.md P11, hearth_and_harvest.md 8,
  crop_yields.md 7, creative_split.md 7.21 (heading: scratch server PASSED), plan_bugfix_1114.md 9, thirdparty_fixes.md 7,
  visceral_heap.md 5, wise_oak.md 8, raspberry_pie.md 14, food_dictionary.md, breeding_audit.md (sections 3 and 4
  measured from `perf/runs/r1115_p9_a1/breeding_dump.json`, section 7), jar_handle_leak.md 3 (gate passed),
  rustic_delight.md 5.6, plan_portal_gun.md (portal exit lift), loading_tips_audit.md 4 (SQ-QN). README status notes:
  Creative Split (7.21 PASSED), Melting, Crop yields (wave 2 PASSED), Wise Oak, Visceral Heap (all flesh PASSED),
  breeding audit, third-party fixes, jar handle leak (client gate PASSED), bugfix run 1114 (built, tested), Portals
  (SQ-PG-01c BROKEN).
- TODO rows added: `gameplay/tnt-and-portal #38` (URGENT, SQ-PG-01c client crash), `creativedelight/members #60`
  (SQ-MELT-08), `#61` (SQ-MELT-10), `largecontent/mod-additions #88` (SQ-HOP-05, pending with the pass-through session),
  `#89` (SQ-HOP-04), `#90` (Power Grid client on a server without it), `#91` (owner notes: SQ-CDNS-12 version, CQ-HOP-01
  ages, SQ-QN-02 feel), `worldgen/biomes #16` (SQ-VH-06c spill), `farming/wild-crops #9` (wild carrots and adzuki),
  `#10` (SQ-RD-26), `items/storage-and-item-loss #13` (lunchbox pumpkin pie), `creativesplit/deps #28` (Upgrade Aquatic
  pair), `tooling/generators-and-tests #16` (SQ-NSM-04 fixture). Updated, still open: `largecontent/mod-additions #82`
  (SQ-PWG-80b, Bug), `#83` (SQ-PWG-82, Bug), `#86` (CQ-PWG-66 row rewrite), `#71` (wild hops, Bug), `gameplay/tnt-and-portal
  #15` (SQ-PG-01b, Bug), `creativesplit/deps #27` (grape bush lines). S tier passed (Complete where S was the last tier):
  `pack/server #39` (C+, Complete), `largecontent/mod-additions #65`, `#66`, `#67`, `#69`, `#70`, `#72`, `#75`, `#76`,
  `#84`, `#87`, `creativedelight/members #56`, `#57`, `#58`, `#59`, `creativedelight/framework #16`, `#17`,
  `recipes/metal #7` (Complete); `dishes/meals #67`, `food/types-and-tuning #39`, `dishes/ingredients #43`, `locate #6`,
  `gear/tuning #23`, `largecontent/mod-additions #79`, `#81`, `#85`, `worldgen/biomes #10` (S+, other tiers open).
- Changelog: no bullet from the write-back (fix-2 added its bullets; the run changed no gameplay); `changelog_lint.py`:
  0 errors, no warning on `unreleased.md`.
- Gotchas: the "Found in r1115" section of `.claude/skills/scratch-test/gotchas.md` checked for duplicates; removed
  four already covered earlier (spawn-animals, boot piped into tail, `level.dimension` property, `enchant` copy and
  `getTag`) and the `execute if biome` clause.

## 7. Run notes

- 00:10 orchestrator: RUNNER.md claimed; no game JVM; 84 GB free.
- 00:13 pass-through session: no PTH rows now, no datapack, no Compat build or install; it messages when phase 5 is
  built. Adopted.
- 00:14 perf session r1115 opened (collector pid 3110586).
- 00:15 prep_srv.sh: SKIPSWAP added; run uses NOSWAP=1.
- 02:40 orchestrator: fix-1 Common build 02:27 ready (SQ-MELT-09, -P1 fixes); installs into mods/ after phase 4 stops its server; phase 5 adds SQ-MELT-09b, -P1b.
- 00:16 runner-1 (phase 1): boot plan A1 (superflat: SQ-TPF-00, -01 + s1_fix rows -11 to -17, SQ-PG-01a, SQ-GAP-04,
  SQ-T1-04, SQ-LGM-13), B1 (seed 8675309: SQ-CBP-11, SQ-OB-05, SQ-HH-328 seed 1, SQ-VH-06/-06b/-06c), CDW boots
  myrtrees / fruitsdelight / endersdelight removed (SQ-CDW-01, -03b); SQ-HH-328 seeds 2 and 3 ride on the myrtrees and
  endersdelight boots (other seeds; neither jar adds structures or biomes). SQ-PG-01b, -01c, CQ-PG-03: client rows,
  deferred. Batches `batches/r1115/p1_*.qb`.
- 00:16 A1 `NOSWAP=1 prep_srv.sh $S/p1a1 p1a1 A spawn-animals=true spawn-npcs=true difficulty=normal`, shipped
  config/debugify.json (sha1 92d95991) and packetfixer.properties unchanged; probes zz_tpf, zz_c3. Only unlisted jar
  cdevperfbot. Boot 00:16:55, Done 00:18:18 (pid 3136313).
- 00:18 A1 `p1_a1.qb` (results `perf/runs/r1115_p1_a1/`): TPF rows 00 to 17 PASS/LOOK, Debugify "Enabled 8/34",
  Packet Fixer applied; SQ-GAP-04 and SQ-T1-04 FAIL on batch regexes (block tag instead of item tag; powered rail and
  golden apple are pack recipes under other ids). Follow-up `p1_a1b.qb` on the same server 00:22 (SQ-GAP-04b): item tag
  `forge:storage_blocks/gold` holds `mca:rose_gold_block`; powered rail (`creativeengineering:shaped/rail_to_6_powered_rail_by_shaped`,
  gold rod tag), golden apple (`creativedelight:shaped/golden_apple`), golden carrot (`creativedelight:shaped/golden_carrot`,
  nugget tag) take the tag; `gold_ingot_from_nuggets` and `minecraft:gold_block` literal. Stopped 00:23:22, JVM gone
  00:23:40; post-stop `diff` of the scratch debugify.json against configs_edited: empty.
- 00:23 B1 `NOSWAP=1 prep_srv.sh $S/p1b1 p1b1 B difficulty=normal` (seed 8675309), probes zz_c3, zz_zact, zz_hh_mw,
  zz_zq_heapcol (r1115 copy: new "[zqh] blocks" / "[zqh] air" lines, biome cache). Boot 00:23:51.
- 00:26 B1 Done (81.9 s, pid 3156255); `p1_b1.qb` 00:26 to 00:33 (`perf/runs/r1115_p1_b1/`). SQ-VH-06c qrun FAIL: 2212
  flesh in warped_forest cells of a box 32 blocks from the Heap; SQ-VH-06 945 netherrack in Heap cells. Both looked like
  biome-border cells, which a per-cell tally cannot tell from a leak: `zq_heapcol.js` gained an "interior" tally (block
  whose 27 surrounding quart cells are all the biome), full floor/ceiling id lists and a 10th argument listing an id's
  interior positions with the zoomed biome and distance to the nearest Heap cell. Follow-ups on the same server after
  `/reload`: `p1_b1b.qb` (SQ-VH-06r, SQ-HH-328c `/cwglocate stats`), `p1_b1c.qb` (SQ-VH-06cw, SQ-HH-328u), `p1_b1d.qb`
  (SQ-VH-06cw2). Block totals before and after `/reload` identical (biome layout unchanged by the re-solve).
- 00:35 SQ-HH-328 seed 8675309: no `[locate]` line (4 misses < `logMissesAtLeast` 8); `/cwglocate stats`: 4 misses,
  264 candidates, 77064 ms. Seeds 2 and 3 get `logMissesAtLeast 0` in the scratch srv toml. `zq under` finds no
  `hearthandharvest:corn_maze` start in radius 4 around the locate point (7 other starts listed).
- 00:39 B1 stopped, JVM gone 00:39:30. CDW1 `NOSWAP=1 prep_srv.sh $S/p1cdw1 p1cdw1 B level-seed=20261115`, myrtrees
  jar removed from srv/mods, `[worldgen.locate] logMissesAtLeast 0` (srv only), srv `kubejs/data/creativedelight/` has
  no `worldgen`. Boot 00:39:43.
- 00:41 CDW1 Done (67.9 s, pid 3188752); `p1_cdw1.qb`: packs line off [myrtrees], no datapack error; corn maze (seed
  20261115) `[locate]` hit at 63 861 after 1 miss, 18 candidates, 7876 ms. Stopped 00:44:14, JVM gone 00:44:27.
- 00:44 CDW2 `NOSWAP=1 prep_srv.sh $S/p1cdw2 p1cdw2 B` (seed 8675309), fruitsdelight removed; Done 81.3 s;
  `p1_cdw2.qb`: off [fruitsdelight]; 17 `[wild-crops]` skip lines, all `<rule>: needs fruitsdelight`, none naming a
  feature, no apple line. Stopped 00:47:10, JVM gone 00:47:22.
- 00:47 CDW3 `NOSWAP=1 prep_srv.sh $S/p1cdw3 p1cdw3 B level-seed=424242`, endersdelight removed, logMissesAtLeast 0
  (srv only); Done 76.7 s; `p1_cdw3.qb`: off none; corn maze `[locate]` hit at -3408 -639 after 3 misses, 297
  candidates, 39446 ms. The census box of the row (M-2..M+36) holds one quadrant (the locate point is the maze centre:
  the centre chest sits on it), hence oak_fence 8 on all three seeds (and in r1114): `p1_cdw3b.qb` (SQ-HH-328b) over
  M-40..M+40: corn 1026, oak_fence 16, 8 chests (centre table, 4 corn_maze tables). Row box to fix at write-back.
  Stopped 00:53:16, JVM gone 00:53:27; game_jvms.sh empty. Runner-1 done.
- 00:55 runner-2 (phase 2, SQ-PWG P11 fixes): batches `batches/r1115/p2_r1.qb` (P11B boot check, -91, -97, -43 servo
  part, -06 with the rewritten thyratron, prereqs -76, -78, -79, then -80, -80b, -82, and -54 at X 1340 for -54r) and
  `p2_r2.qb` (restart: -54r). Prereq blocks copied from `r1113pwg/pwgr1.qb` / `pwgr1r.qb`; -06 thyratron heater 6.5 V
  (pwg12 batch fix; row says 6 V). CQ-PWG-66 client: deferred.
- 00:59 P2 `NOSWAP=1 PGJAR=jar_archive/pwg_gate/backportedPowerGrid-0.6.2-ce-19.2-18.jar prep_srv.sh $S/p2pwg p2pwg A
  spawn-animals=true spawn-npcs=true` (379 jars, -18 sha1 e2163c7f573f = mods/ copy), 13 pwg probes, loading protection
  1 s; unlisted jars only cdevperfbot and the gate jar. Boot 00:59:39, Done 10.4 s 01:00:53 (pid 3241438); p2_r1.qb
  from 01:01 (`perf/runs/r1115_p2_r1/`).
- 01:01 to 01:10 p2_r1.qb: P11B (version -18, 115 powergrid recipes: -17's 116 minus `mixing/acid`), -91 PASS, -97
  PASS, -43 PASS, -06 PASS, -76 qrun FAIL only on `mixing/acid=true` (now false by the pack script: batch fixed), -78,
  -79 PASS, -80 PASS, -80b recorded, -82 FAIL. The runner's `timeout 590` on the foreground qrun killed it at 01:10:49
  inside -54 (panel A and one wire at X 1340): tooling slip, not the server. Later batches ran under nohup, waited on the
  pid.
- 01:12 p2_r1b.qb (same boot): SQ-PWG-80bx recipe reads, -54 rerun at X 1380 (PASS). 01:14 p2_r1c.qb: -82 again at Z
  1926 with the double lineck (same FAIL). 01:15 p2_r1d.qb: arm `HeldItem` resistor x7 on both arms, end depot holds
  wire_connector x8, no loose item entities.
- 01:15:31 stopped (saved 01:15:43, JVM gone 01:15:49); boot 2 01:15:58, Done 3.8 s (pid 3269412); p2_r2.qb (-54r,
  BX 1380) 01:17, PASS. Stopped 01:17:37, JVM gone 01:17:44; game_jvms.sh empty. Runner-2 done (about 25 min from the
  first batch edit).
- 01:20 runner-3 (phase 3, SQ-MELT): installed `creativedelight-1.1.0.jar` (11-14 23:28) nests
  `repackedSalt-1.2.4.1-ce-19.2-03.jar` whose `Melting.tryMeltFromBlock` is `iconst_0; ireturn` (salt member block melt
  off, plan_bugfix_1114 3.8): SQ-SALT2-06 and SQ-MELT-01 are judgeable. New probe `tools/scratch_query/zmelt_probe.js`
  (`/zmelt w|show|clear|light`: watches positions every tick, logs each block change with the server tick). Batches
  `batches/r1115/p3_a1.qb` (generated: every case in a 1-deep pit at y -61 so melt water stays put; bots m3 overworld,
  m3n on a stone platform on the Nether roof y 128; night, clear, doFireTick off; randomTickSpeed 1000 for 120 s, -04 at
  100 for 5 min), `p3_a1b.qb` (SQ-AZW-01, SQ-MELT-P1, SQ-DCM-02 last), `p3_a2.qb` (restart with
  `vanillaLightMelting true`, SQ-MELT-06 part 2), `p3_c1.qb` (SQ-MELT-09, `cpp_chain_srv.sh compat`). Deviations: the
  flowing-lava case of -03 is a fed trench (a lone `lava[level=3]` dries up), -10 reads the live tag with
  `/zq blocktag` instead of a `/dumpall`.
- 01:26 A1 `NOSWAP=1 prep_srv.sh $S/p3a1 p3a1 A difficulty=normal` (378 jars; unlisted only cdevperfbot), probes
  zz_zmelt, zz_zp. Boot 01:26:40, Done 9.8 s 01:27:49 (pid 3314599). Boot log: `[melting] tags: 6 meltable, 2 slow, 39
  salt, 123 fire, 1 lava, 3 ultra-warm instant`; source-driven `[minecraft:magma_block (light 3)]`; WARN `fire tag
  blocks with light below 4 that do not random tick: [architects_palette:glowstone_cage_lantern (light 3)]`. p3_a1.qb
  from 01:28 (`perf/runs/r1115_p3_a1/`).
- 01:28 to 01:38 p3_a1.qb: -10 qrun FAIL on the low-light warning (finding above); -04 PASS (randomTickSpeed 100: four
  blue ice blue -> packed -> ice -> water, no skipped tier, slow steps about 1100 ticks each against 820 expected, ice ->
  water 17 to 170 ticks; packed by fire and at 2 from lava stepped, at 2 from fire and 3 from lava kept); -05, -07,
  SALT2-06b PASS; -08 FAIL (bot click with `salt:salt`: use SUCCESS, blue ice unchanged, stack turned into
  `vintagedelight:salt_dust`, later clicks FAIL; the salt member calls `tryMeltFromItem` only from its dispenser
  behaviour). -02, -03, -06 qrun FAIL: every distance-4 control melted, and so did the unlit campfire, sea lantern,
  redstone torch, jelly torch and unlit brazier ice, also at randomTickSpeed 3, minutes after placement. Not Immersive
  Weathering (its overworld ice melt needs day; time 18000). Taken as Serene Seasons' seasonal surface melt (heightmap
  top, melting.md 6): `p3_a1r.qb` reruns -01, -02, -03, -06 part 1, SALT2-06 overworld with a glass block over every
  target (+150 x, bot m3r) plus roofed and open no-source controls; `p3_a1x.qb` (-08 salt_dust click and dispensers,
  glowstone cage lantern light). Generators moved to `tools/scratch_query/melt_gen_p3.py`, `melt_gen_p3r.py`.
- 01:40 to 01:47 p3_a1r.qb + p3_a1x.qb (same boot): roofed -01r, SALT2-06r, -02r, -03r, -06r all PASS; P3-RC: roofed
  no-source ice and snow kept, open ones melted (ice t=21664, snow t=18760): the open-air melt is Serene Seasons'
  surface melt, not Common's. -08x: a `vintagedelight:salt_dust` click (use SUCCESS) PLACES a `vintagedelight:salt`
  layer on the ice (direct check at 01:48, 2044 -60 2080); that layer is a salt source, so the ice under it steps later at
  the block rate (m08 blue stepped 3 min after the click, x08 11 s after); the next click FAILs (layer in the way). A
  `salt:salt` dispenser does nothing on ice or blue ice (stack not used): its `SaltItemDispenseBehavior` returns at once
  unless the stack is `#forge:salts`, which holds only `vintagedelight:salt_dust`; a salt_dust dispenser just drops
  dust. P3-X1: glowstone cage lantern lit = block light 3 (2 beside), unlit 0.
- 01:49 p3_a1b.qb (SQ-AZW-01, SQ-MELT-P1 box moved to x 2300..2363, SQ-DCM-02 last).
- 01:50 to 02:14 p3_a1b.qb: SQ-AZW-01 PASS; SQ-MELT-P1: forge tps overworld mean 2.578 ms (glacier, 10 samples) vs
  2.395 ms (stone control): 0.18 ms; JFR (perf/sessions/r1115/boots/08_p3a1/, cut by timeline.tsv, first 60 s of each
  window dropped): `creativecommon.melting.Melting` in 1.00 % of Server thread samples in the glacier window, 1.19 % in
  the control (the background is the forceloaded test area, not the box). Over the whole boot the Melting samples are
  mostly `ChunkReader` palette checks (`mayHaveTargets`, source-driven magma, randomTickSpeed 1000 phases) and 267
  `ForgeConfigSpec$ConfigValue.getRaw` under `onRandomTick` (config read per tick). The box's top ice layer melted to
  water (3923 of 4096: Serene Seasons, open air). SQ-DCM-02: 0 KubeJS errors after /reload; scratch 32042 recipes vs the
  pack dump 32168 (11-15 00:02): the missing ids are 115 powergrid, 12 creativebrewing, 1 creativeengineering, 1
  create_questing (Power Grid gate jar absent from scratch boots); `[absent mods]` names none of the five G5 mods.
- 02:15:33 A1 stopped, JVM gone 02:15:38. `qrun.py toml ... melting vanillaLightMelting true` (srv only), boot A2
  02:16:19, Done 3.8 s (pid 3394759). `p3_a2.qb` (targets roofed, bots m3 and m3r): new sea-lantern ice (block light
  14) melted at t=434, the roofed p3_a1r sea-lantern ice at t=486, redstone-torch ice kept. Stopped, JVM gone 02:20:01.
- 02:20 C1 `NOSWAP=1 cpp_chain_srv.sh $S/p3c1 p3c1 compat` (8 jars: Common, Compat, Calendar, Serene Seasons, KubeJS,
  Rhino, Architectury, perfBot), zz_zmelt; p3_c1.qb gained glass over every target (Serene Seasons is in this set).
  Boot 02:20:15, Done 24 s (pid 3406364). `[melting] tags: 5 meltable, 2 slow, 0 salt, 13 fire, 1 lava, 3 ultra-warm
  instant`; fire and lava pairs as -02/-03. qrun FAIL was the bootreject matching Forge's fresh-config fill (`Incorrect
  key melting... corrected`, no config/ in this set): batch fault. Mixin WARN `Error loading class:
  me/shedaniel/rareice/forge/blocks/RareIceBlock` (Common's Rare Ice pseudo mixin with Rare Ice absent). Stopped, JVM
  gone 02:24:10; game_jvms.sh empty. Runner-3 done (01:15 to 02:24).
- 02:27 runner-4 (phase 4, SQ-SPD / SQ-SLK and CD fixes): batches `batches/r1115/p4_a1.qb` (group A: SQ-SPD-01 to -08
  (-08 on the Nether roof y 128), SQ-HH-1574, -1575, -309, -1593, -1505, -1507, -1534, SQ-SLK-02, -03, -04, SQ-HH-434,
  -440, -440b, -482, SQ-SLK-01, SQ-FDI-03, -03b, SQ-BBQW-02s (+ -02), SQ-CBW-03) and `p4_b1.qb` (fresh B world, SQ-CDW-02
  last). SQ-DEP-02, SQ-MIN-03: phase 7. CQ-SPD-01, CQ-SLK-01: client, deferred. Code reading: the seed container 5x5 is
  centred on the block in front (plan_bugfix_1114 1), so a horizontal dispenser stands in its own 5x5: SQ-SPD-01/-02 can
  plant 24, not 25.
- 02:31 A1 `NOSWAP=1 prep_srv.sh $S/p4a1 p4a1 A difficulty=normal` (378 jars, unlisted only cdevperfbot), probes
  zz_hh_crow, zz_hh_gear, zz_zc4, zz_c2, zz_zp, zz_zact; srv toml `[salt] saltLickInterval 200, saltSearchInterval 20,
  saltAnimalStageChance 1.0`, Loading Protection 1 s. Boot 1 02:33:04: `Failed to load datapacks` (NPE `frame is null` in
  Rhino `Interpreter.getSourcePositionFromStack` from KubeJS `TagLoaderKJS.customTags` console.info on a worker thread:
  parallel tag-event race), JVM stayed up idle; pid 3454368 killed 02:41 (never reached Done). Boot 2 02:42:13, Done
  10.2 s 02:43:22 (pid 3466606); `p4_a1.qb` from 02:43 (`perf/runs/r1115_p4_a1/`).
- 02:43 boot 2 read a stale `stop` at once (Stopping 02:43:24, 2 s after Done): the boot-1 `scratch_server.sh stop`
  wrote `stop` into the fifo while that JVM hung, and the boot-1 feeder (`sleep infinity`, pid 3454359) kept the old
  fifo open with the line buffered. Tooling fix `scratch_server.sh`: boot kills a feeder named in feeder.pid and makes a
  fresh fifo; stop's `kill -9 ... ` chain gets `|| true` (under `set -e` a failed kill ended stop before its feeder kill,
  which is how feeders leaked). Boot 3 02:47:39, Done 3.7 s 02:48:43 (pid 3484230).
- 02:49 to 02:52 `p4_a1.qb`: SPD rows, -1574, -1575, -309, -1593 ran; SQ-HH-1505 / -1507 horses were gone right after
  summon: batch fault, `spawn-animals=false` (prep default) makes the server discard every Animal each tick (horses,
  cows, crows). qrun stopped 02:52 inside -1534, server stopped, `spawn-animals=true` in the srv server.properties,
  SQ-SPD-08x follow-up appended (overworld soul sand control, Nether below the roof). Boot 4 02:53:23, Done 4.0 s
  02:54:26 (pid 3495490); P4-setup again, then `--from SQ-HH-1505` 02:54 to 03:05.
- 03:05 SQ-SPD-08: the pouch plants 10 wart over soul sand in the overworld, 0 in the Nether at y 129 and y 61; a Nether
  cobblestone dispenser fires (3 -> 2). Cause: `SeedContainerDispense.plantOne` takes `FakePlayerFactory.getMinecraft(level)`,
  which Forge 43.3.9 caches by GameProfile only (the overworld player is returned for the Nether), and `UseOnContext`
  uses `player.level`, so Nether seeds are tried in the overworld. SQ-BBQW-02s: the stick is not lost, Supplementaries
  places it as `supplementaries:stick` on top of the basin (StorageBlock.use returns PASS, so the item use runs).
- 03:06 `p4_a1b.qb` (SQ-SLK-01b, same boot): the 18-block cow reaches the salt (from about 520 ticks, minD 1.86) and
  stays near it for the 1600-tick watch but never licks (block still `salt:salt_block`, stage chance 1.0); in SQ-HH-482
  two cows licked the block away (air), not attributable per cow. Stopped 03:08:40, JVM gone 03:08:53.
- 03:09 B1 `NOSWAP=1 prep_srv.sh $S/p4b1 p4b1 B difficulty=normal` (seed 8675309); boot 03:09:05, Done 81.6 s 03:11:25
  (pid 3522796); `p4_b1.qb` (`perf/runs/r1115_p4_b1/`): packs line `off: none`, jungle at -1616 -302, 160x160 box:
  40 `myrtrees:rubberwood_log` (r1113: 0), 3358 jungle logs. Stopped 03:13:59, JVM gone 03:14:09; two leaked feeders
  of this phase killed; game_jvms.sh empty. Runner-4 done (02:27 to 03:16).
- 03:23 runner-5 (phase 5, SQ-HOP + SQ-MELT-09b / -P1b): batches `batches/r1115/p5_o1.qb` (SQ-HOP-11 old-save
  fixture on the previous CD jar: no pre-11-14 CD jar in jar_archive/displaced, so the 11-14 03:46 build from the r1114
  scratchpad (pwgperf/srv/mods, nests backportedHearth -04 with hearthandharvest:hops and tilled_end_stone blocks) is
  copied to scratchpad r1115/oldcd/), `p5_a1.qb` (same world on mods/ jars: -11, HH-1546b, HOP-02 to -05, -07 to -10,
  HH-1546, -1546c, HH-266 rerun, -266c, -266b, MELT-09b full-pack half), `p5_a1p.qb` (MELT-P1b boxes as p3_a1b, plus a
  `[melting] enabled` toml-edit row), `p5_c1.qb` (MELT-09b Common + Compat set), `p5_b1.qb` (fresh B: SQ-HH-304,
  HOP-06). SQ-HOP-01 is "fold into SQ-DEP-02": left to phase 7's no-Brewing boot. CQ-HOP-01: client, deferred.
  Tooling: `zq_probe.js` gained `/zq items|blocks <text>` (registry ids containing text, with class);
  `hh_mw_probe.js` countset prints "(in K chunks)" per id.
- 03:23 O1 `NOSWAP=1 prep_srv.sh $S/p5a p5a A spawn-animals=true difficulty=normal` (378 jars, unlisted only cdevperfbot),
  srv/mods CD replaced by the 11-14 03:46 build (sha1 c98f8504cf38), probes zz_hh2, zz_hh5, zz_zmelt, Loading Protection
  1 s. Done 10.6 s 03:24:40 (pid 3563665). `p5_o1.qb`: old jar has `hearthandharvest:hops` (HopsLatticeBlock) and
  `hearthandharvest:tilled_end_stone`; three fixture blocks placed, saved. Stopped, JVM gone 03:26:12.
- 03:26 A1 = same world, CD hard link of mods/ (sha1 3eaeaeced71c). Done 4.2 s 03:27:25 (pid 3567871); log
  `[swaps] minecraft:block: 2 id(s) remapped`, `minecraft:item: 1`. `p5_a1.qb` from 03:27 (`perf/runs/r1115_p5_a1/`).
- 03:27 to 03:35 `p5_a1.qb`; 03:36 `p5_a1x.qb`, 03:38 `p5_a1y.qb` (follow-ups, same boot). Findings: (1) Compat's
  VineShearing gives `hearthandharvest:hops_vine` a `fertile` state; the first shears use sets fertile=false and
  cancels, so HopsVineBlock's STERILIZED toggle only runs from the 2nd use; HopsVineBlock overrides randomTick, so
  fertile=false does not stop it growing (606 grew age 1 -> 3 at fertile=false). (2) A stick on an age-1 hops vine
  (use PASS) places Supplementaries' `supplementaries:stick` in the vine's space (vine is replaceable). (3) Ender Crop's
  tilled end stone with nothing on top reverts to end stone on a random tick: HH-1546b's block (and one cell of the
  first -1546c area till) reverted under the randomTickSpeed 3 / 3000 steps before the check; -1546c2 at
  randomTickSpeed 0 tills all 9. HOP-11's 622 lattice read age 4 (age 2 saved): grew under randomTickSpeed 3 before the
  check (batch order). Old-save part rerun planned on a fresh world, checks first at randomTickSpeed 0. (4)
  `minecraft:ice` is vanilla IceBlock; Rare Ice's own block is `rare-ice:rare_ice` (RareIceBlock, reverts to ice when
  its block entity holds no item): a bare setblock gives ice. 03:41 `p5_a1p.qb` (SQ-MELT-P1b) started.
- 03:41 to 04:06 `p5_a1p.qb` (SQ-MELT-P1b): overworld mean 3.193 ms glacier vs 3.235 ms control (10 samples each);
  JFR (perf/sessions/r1115/boots/17_p5a/repo, parsed with PerfParse, windows from timeline.tsv minus 60 s, new tool
  `tools/scratch_query/jfr_window_share.py`): `Melting#onRandomTick` in 0.30 % (glacier) / 0.10 % (control) of Server
  thread samples (P1: 1.00 / 1.19 %); no stack in the whole boot holds a ConfigValue frame under onRandomTick (P1: 267).
  P1b-cfg: `[melting] enabled = false` written into the srv toml mid-run, ice and snow beside fire still melted: no
  Forge "Config file ... changed" debug line for any of 4 edits (watcher thread is parked in ModernFix's
  `NightConfigWatchThrottler`; same family as SQ-WO-34): live toml edits are not picked up on these servers, so the
  clause cannot be judged live. 04:07 `p5_a1z.qb`: `rare-ice:rare_ice` with a diamond in its block entity and vanilla
  ice, roofed beside sea lanterns: both kept 60 s at randomTickSpeed 1000 with vanillaLightMelting false; stopped
  04:08:51, srv toml vanillaLightMelting true, restart Done 04:10:00 (pid 3626994), `p5_a1r.qb`: vanilla ice melted
  t=233, rare ice t=239, diamond dropped: Rare Ice's light melt follows the config (redirect applied). Stopped 04:12:09.
- 04:12 O2 `NOSWAP=1 prep_srv.sh $S/p5a2 p5a2 A spawn-animals=true difficulty=normal`, old CD jar, Done 04:13:32;
  `p5_o2.qb` fixture saved (randomTickSpeed 0 in the save). Stopped 04:14:07; A2 on mods/ CD, Done 04:15:17;
  `p5_a2.qb` (`perf/runs/r1115_p5_a2/`): before any random tick 620 lattice age 4 / 622 age 2, facing south, grape
  none, 624 `endercrop:tilled_end_stone[moisture=0]`; after 5 s at 3000 both grape=hops (622 grew 2 -> 4), 624 reverted
  to end stone (Ender Crop: bare tilled end stone reverts on a random tick). Stopped 04:16:06.
- 04:16 C1 `NOSWAP=1 cpp_chain_srv.sh $S/p5c1 p5c1 compat` (8 jars, Common = mods/ 03:15), zz_zmelt; Done 23.7 s
  04:16:53; `p5_c1.qb` (`perf/runs/r1115_p5_c1/`).
- 04:17 to 04:19 `p5_c1.qb`: `[melting] tags: 5 meltable, 2 slow, 0 salt, 13 fire, 1 lava, 3 ultra-warm instant`; no
  Rare Ice line in the log; fire/lava pairs as -09. Stopped 04:19:47.
- 04:20 B1 `NOSWAP=1 prep_srv.sh $S/p5b1 p5b1 B difficulty=normal` (seed 8675309), zz_hh2; Done 73.9 s 04:22:09 (pid
  3656090). `p5_b1.qb`: site 1 forceload refused ("Too many chunks ... 289": a 256-block box spans 17 chunks per axis
  when unaligned; batch boxes now 240 blocks), site 2 (1472 -3264, 256 chunks, 2572 oak / 2634 birch logs): 0 hops
  vines, 0 wild carrots. `p5_b1b.qb`: hops tag has minecraft:forest (18 biomes); site 1b (256 chunks) and site 3
  (-2072 2232, 225 chunks, no oak/birch) 0 vines. `p5_b1c.qb`: carrot tag (25) and adzuki tag (18) hold forest, both 0
  in site 3; `/place feature creativedelight:wild/wild_hops` at 25 points of site 2: 1 placed (3 vines), 24 failed.
  Stopped 04:33:56; game_jvms.sh empty. Runner-5 done (03:20 to 04:36). Verdicts in section 8.
- 04:38 runner-6 (phase 6, CD fix-2 reruns + SQ-SY wave 2): batches `batches/r1115/p6_a1.qb` (setup, SQ-SY-14, id
  lookups via `/zq blocks`, SQ-SPD-08b, SQ-BBQW-02t, SQ-BBQW-02, SQ-SLK-01c) and `p6_a2.qb` (same boot: SQ-SLK-01d
  follow-up, SQ-SY-15 to -21), results `perf/runs/r1115_p6_a1/`. CQ-SY-22: client, deferred. Tooling: `sy_probe.js`
  gained `/syt pot X Y Z` (Botany Pots growth / required growth time, SQ-SY-21).
- 04:40 A1 `NOSWAP=1 prep_srv.sh $S/p6a1 p6a1 A spawn-animals=true difficulty=normal` (378 jars, unlisted only
  cdevperfbot; CD 03:18 = fix-2, Common 03:15), probes zz_hh_crow, zz_c2, zz_zp, zz_zc4, zz_sy; srv toml `[salt]
  saltLickInterval 200, saltSearchInterval 20, saltAnimalStageChance 1.0`, Loading Protection 1 s. Boot 04:40:31, Done
  9.2 s 04:41:40 (pid 3724529). `p6_a1.qb` 04:42 to 04:47: SY-14 boot line reads `35 crops` (row says 36; soil_yield.json
  `loot` has 35 keys, the other two switches propelplant and salmonberry are picks only: row-text count), Fortune
  stripped 90 terms on 71 tables. SPD-08b: 10 / 10 / 10 wart, bundle 1. BBQW-02t: CONSUME on both slots. SLK-01c: block
  `minecraft:air` at the end (licked fully away inside 1600 ticks; batch expected a licked stage only): follow-up
  SQ-SLK-01d in `p6_a2.qb` logs the block every 5 s. 04:47 `p6_a2.qb` started.
- 04:47 to 04:54 `p6_a2.qb`: SLK-01d salt -> lightly (about 15 s) -> well -> heavily -> air (about 50 s). SY rows ran;
  three batch / probe faults: (1) `veggiesdelight:sweet_potato_crop` has age 0 to 5 (`age=7` refused, crop not placed);
  (2) a tool string with NBT (`minecraft:diamond_hoe{Enchantments:[...]}`) made `/syt` throw out of the command
  ("An unexpected error occurred"); (3) a Botany Pot right-click opens its GUI (CONSUME, nothing inserted), and
  `getRequiredGrowthTime` is on the pot's container, not the block entity. Tooling `sy_probe.js`: tool spec
  `<id>@fortune=N`, `/syt props <block>`, `/syt potset X Y Z <soil> <seed>`, pot readout from the container.
- 04:55 `p6_a3.qb` (probe copied in, `/reload`): sweet potato at age 5, Fortune halves, lemon break n 3000, pots. The
  melon control showed no Fortune effect (5.09 / 5.00), and 04:58 `p6_a4.qb` / `p6_a5.qb` coal ore 1.00 with Fortune III
  while vanilla `/loot ... mine` with the same enchant gave 1 to 4 coal and up to 9 melon: probe fault, KubeJS's
  `ItemStack.enchant(Enchantment, int)` returns an enchanted copy and leaves the stack as it was. Fixed (`st = e`).
  05:00 `p6_a6.qb`: roll lines print the tool NBT; coal 2.28, melon 6.44 with Fortune III; SY-15 / -16 Fortune halves
  rerun, equal to the no-tool means. Stopped 05:01:10, JVM gone 05:01:27; game_jvms.sh empty. Runner-6 done (04:38 to
  05:02). Verdicts in section 8.

- 05:05 runner-7 (phases 7 and 8: SQ-DEP optional-mod guards + SQ-HOP-01, then SQ-QN): `optional_ref_audit.py` exits 0.
  Batches generated by `tools/scratch_query/dep_gen_p7.py` into `batches/r1115/`: `p7_a0.qb` (SQ-DEP-09a) + `p8_a0.qb`
  (SQ-QN, same full-pack boot as a cheap extra), `p7_d_<mod>.qb` (SQ-DEP-09b drop-one; fruitsdelight and corn_delight
  boots on a group B new level so the SQ-CDW-01 / -03b 5.2 reruns ride along; myrtrees / endersdelight CDW boots
  "covered by phase 1"), `p7_nb.qb` (SQ-DEP-02 + SQ-HOP-01, no Brewing), `p7_ch.qb` (SQ-MIN-03, r1113 c3_ch1 set). New
  probe `tools/scratch_query/zqn_probe.js` (zqn: Quark GameNerfsModule statics, bot tryToStartFallFlying, hand / XP);
  loot via zhh2 lootroll and zc3 loot / items. mods.toml scan: supplementaries is required by suppsquared, upgrade_aquatic
  by alexsabnormalities, quark by QuarkEngineering, immersiveengineering by 9 jars (kubejs-ie, IP, Posts, ...): those
  four drop-one boots are expected cascades; a confirm boot each, and a "+" extra boot for the first three with the one
  dependant removed too.
- 05:07 A0 `NOSWAP=1 prep_srv.sh $S/p7a0 p7a0 A difficulty=normal` (378 jars, unlisted only cdevperfbot), probes zz_c3,
  zz_hh2, zz_zqn; boot 05:07:54, Done 05:09:03 (pid 3787739). `p7_a0.qb`: SQ-DEP-09a all ten `creativedelight:mods/*`
  packs enabled; corn maze 10 rolls 24 ids (corn, seeds, cornbread, popcorn, caramel apple, apple cider), centre 10 rolls
  21 ids; `/loot spawn` Dropped 5; jam jar 3 lemon_jelly, clover 1 buzzier_bees clover; ERR lines 0. Pancakes
  `servings=2` give 4 `supplementaries:pancake` + bowl, not 3: the stack keeps Rustic's numbering (servings = eaten, the
  CD table drops 6 - servings), so the row's "3" is a row-text error (batch now expects x4). `p8_a0.qb` (phase 8 extra on
  this boot): Quark statics nerfMending false, enableDimensionLockedElytra false; bot Mending pickaxe 500 -> 400 damage
  from a 50-point orb, XP 0; elytra: Quark canEntityUseElytra true, tryToStartFallFlying started, still gliding 2 s later
  in the overworld (y 172 -> 162, 1.5 b/t forward). Batch faults fixed and rerun (a segment-wide `reject` matched the
  pre-orb line; probe `dimension()` is a KubeJS bean property; the first glide started 60 blocks up and landed before the
  check: now 300 up). Stopped 05:21:50; game_jvms.sh empty.
- 05:24 drop-one boots detached: `tools/scratch_query/dep_p7_boots.sh` (prep, remove jars, probes, boot, wait_boot,
  qrun, stop_wait per spec; log scratchpad r1115/p7boots.log, pid 3809893). Order: cascade confirms (supplementaries,
  upgrade_aquatic, quark, immersiveengineering), then buzzier_bees, create_factory, hostiledelight, moredelight,
  fruitsdelight (B), corn_delight (B), the three "+" extras, then the no-Brewing boot. `wait_boot.sh` now reports
  MISSING on an FML missing-dependency refusal instead of waiting out its cap.
- 05:22 to 05:24 cascade confirms, each FML "Missing or unsupported mandatory dependencies" in 6 s: supplementaries
  (suppsquared), upgrade_aquatic (alexsabnormalities), quark (quarkengineering), immersiveengineering (quarkengineering,
  more_immersive_wires, compressedengineering, controlengineering, immersiveposts, immersivepetroleum,
  kubejs_immersive_engineering, tmted). 05:24 to 05:31 buzzier_bees, create_factory, hostiledelight, moredelight (A):
  Done, ERR 0, pack not listed, corn maze rolls give items (create_factory: only the two mod_items WARNs), placed_omelette
  / placed_suspicious_bacon_and_eggs "Unknown block type", zq 0. 05:31 to 05:37 fruitsdelight and corn_delight on a
  group B new level (seed 8675309, Done 80 s): ERR 0; fruitsdelight packs line lists it under off, 17 "needs
  fruitsdelight" skips, 0 naming a fruitsdelight feature; corn_delight: packs off none (no worldgen pack), six corn maze
  mod_items WARNs only, corn maze 5 rolls 16 ids. 05:37 supplementaries + suppsquared: Done, ERR 0, pancake table WARNs
  only (`rusticdelight:mod_items/supplementaries/pancake`).
- 05:39 upgrade_aquatic + alexsabnormalities: "Failed to load datapacks, can't proceed": `Unknown registry key ...
  worldgen/feature: upgrade_aquatic:river_tree`, from the pack's own `kubejs/data/upgrade_aquatic/worldgen/
  configured_feature/river_tree.json` + `placed_feature/trees_river.json` (instance KubeJS data in Upgrade Aquatic's
  namespace, not a CD guard). The JVM sat idle; wait_boot waited out its cap (TIMEOUT 05:48:56) and
  `scratch_server.sh stop` hung on the idle server, so the chain halted 05:55:57; pid 3902505 killed by hand 05:56
  (own scratch JVM, cwd p7dua/srv). Tooling: wait_boot.sh now reports DATAPACK on that line; dep_p7_boots.sh kills its
  own JVM (cwd OUT/srv) when stop_wait is cut off. 05:57 chain resumed (quark + quarkengineering, then no-Brewing; pid
  3927565, log r1115/p7boots2.log).
- 05:56 quark + quarkengineering: mod loading fails ("Failed to start the minecraft server"): Botania's common_setup
  `NoClassDefFoundError: vazkii/quark/api/IQuarkButtonAllowed` (Botania needs Quark's API classes; third party, a runtime
  cascade). JVM idle again: TIMEOUT 06:06:16, killed by the new fallback 06:08:46. wait_boot.sh now also reports FAILED
  on "Failed to start the minecraft server". 06:08 NB (creativebrewing removed, A), Done 9.6 s (pid 3953986): SQ-DEP-02
  shell FAIL was the batch: `bootreject brewinandchewin.*ClassNotFoundException` caught Create Central Kitchen's own
  optional `KegBlockEntityMixin` WARN (as r1114 noted); regexes narrowed in dep_gen_p7.py, rechecked against the log (0
  hits). ERR 0, `placed_vegetable_omelet` Unknown block type, zq 0; corn maze 5 rolls 18 ids, two
  `mod_items/vinery/apple_cider` WARNs; 8 grape bush lines, all Worldgen's `[wild-crops] mod not loaded, skipped grape*:
  vinery:*_grape_bush_chance` WARNs (for TODO creativesplit/deps #27). SQ-HOP-01: zq hops / wild_hops items, hops_vine
  block all 0; the only hops lines are Common's `[item_dict] Missing/invalid item IDs` WARN (79 ids without Brewing:
  60 everycomp, 7 creativedelight, 5 hearthandharvest hops ids, 4 immersive_weathering, 3 supplementaries/suppsquared;
  none in the full pack). Stopped 06:10:26.
- 06:10 CH `NOSWAP=1 EXTRA=... cpp_chain_srv.sh $S/p7ch p7ch chain` + Worldgen jar (37 jars, as r1113 c3_ch1), Done 54 s
  (pid 3965192): SQ-MIN-03 none of the five lines, ERR 0 in the row list. Stopped 06:13:27; game_jvms.sh empty.
  Runner-7 done (05:05 to 06:15). Verdicts in section 8. Not booted: SQ-CDW-01 myrtrees / endersdelight boots (covered
  by phase 1, CD's only change since is this run's fixes); client rows none in these sections.
- 06:15 runner-9 (phase 9, older server rows from the 11-14 22:30 audit). From the records, no boot needed: SQ-VH-02 to
  -09 and -12 PASS in r1111 (-06 to -06c this run), -10 RECORDED in r1111 (real TerraBlender wins and boots); only -11
  is open. SQ-WO-15 to -25, -31, -33 PASSed in qrun r1110wo (`perf/runs/r1110wo/results.md`) but never got verdict
  lines; rerun on the current Worldgen jar. SQ-HCS-10 (= the rewritten SQ-HH-401 to -502) is judged from r1111/r1112 and
  this run's reruns. SQ-RDB-03's sub-rows ran after the bake in r1113 c4_a / c4_ar (`perf/runs/r1113/verdicts/c4_a.md`).
  SQ-CDNS-10 PASS r1110 (10a to -10e). SQ-OB-02 names come from client lang scripts (`kubejs/assets/*/kubejs/names.js`):
  client. Client rows SQ-CDNS-12, SQ-NSM-04 skipped. New probe `tools/scratch_query/zp9_probe.js` (/zp9: lunchbox fill
  and eat, dye and skin recipe match, breeding dump for SQ-BREED-01 since `/dumpbreeding` was never written, block
  state read). Batches `batches/r1115/p9_*.qb`.
- 06:23 A1 `NOSWAP=1 prep_srv.sh $S/p9a p9a A` (378 jars), probes zz_wo, zz_zc4, zz_zp9; srv toml Wise Oak as r1109wo
  step a2 (timers 5 s, chance 100, threshold 75). Boot 06:23:30, Done 10.9 s (pid 3998785). 06:25 `p9_wo_a2.qb`
  (SQ-WO-15 to -25) then `p9_a1.qb` (SQ-RP-07, SQ-OB-07, -08, -10 on group A: recipe, item and food checks only,
  SQ-FDI-04, SQ-FLR-02, SQ-BREED-01), detached (qrun chain pid 4000887), results `perf/runs/r1115_p9_a1/`.
- 06:25 to 06:45 A1 batches: `p9_wo_a2.qb` PASS 12 (06:25 to 06:28). `p9_a1.qb`: probe faults fixed and rerun on the
  same boot after `/reload` (`p9_a1r.qb`, `p9_a1s.qb`, `p9_a1t.qb`): zp9 `held`/`heldeat` used `ItemStack.hasTag()` (not
  remapped by KubeJS, use `.nbt`); the breeding dump wrote through `java.nio.file.Files` (blocked by KubeJS's class
  filter: KubeJS `JsonIO.write`); `BlockState.is(TagKey)` / `ItemStack.is(TagKey)` are ambiguous to Rhino and the
  explicit-signature form is not remapped (read tags through `ForgeRegistries.X.tags().getTag(tag).contains(...)`).
  The stalled qrun of the first BREED-01 attempt was killed (its waitlog had 900 s left). Batch faults: SQ-OB-10 regex
  (`Gave 1    [` has padding), SQ-FDI-04 used vanilla pumpkin pie, which is not a food in this pack (placeable, stack 8).
  Breeding dump copied to `perf/runs/r1115_p9_a1/breeding_dump.json` (104 Animal types x 20477 items, 190 flower blocks).
- 06:46 A1 stopped. World `serverconfig/solapplepie-server.toml` thresholds [0.0], benefitsUnparsed max_health 4,
  minFoodsToActivate 0; Wise Oak gifts chance 0, threshold 0. A1b Done 4.2 s 06:47:42 (pid 4031314): `p9_a1b.qb`
  (SQ-RP-08 (now SQ-RPI-08); count regex missed "for entity rp8": BATCH-FAULT, fixed), `p9_wo_a3.qb` (SQ-WO-31 PASS). Stopped 06:48;
  gift lists all [] with chance 100, threshold 0; A1c Done 3.8 s 06:50:11 (pid 4036762): `p9_wo_a5.qb` SQ-WO-33 PASS.
  Stopped 06:50:49.
- 06:51 O1 `nsm_fixture.sh $S/p9o1` (1.2.02 jars, commit 40ed32be trees) with the r1111 fixture world copied in as
  level nsmfix; Done 3.9 s 06:52:12 (pid 4046043). `p9_o1.qb`: Rustic plates, crops, crates, potted wild coffee,
  cheesecake placed at x 200 z 200; old wooden buckets in a chest and on bot ob11. `rusticdelight:pancake` and
  `fried_dough` do not exist in 1.2.02 (Unknown item), so SQ-RDX-13's item half cannot be built from a 1.2.02 save.
  Stopped 06:53.
- 06:54 O2 `NOSWAP=1 prep_srv.sh $S/p9o2 nsmfix A spawn-animals=true` + a copy of the O1 world, probes zz_zc4,
  zz_zp9. Done 4.0 s 06:54:55 (pid 4054418). `p9_o2.qb` (SQ-OB-11, SQ-RDB-05, SQ-RDX-13, SQ-NSM-05, SQ-VH-11) and
  `p9_o2r.qb` (NSM-05 skin half after a zp9 fix: `new SimpleContainer(2)` resolves to the varargs constructor in
  Rhino). SQ-VH-11's `execute if biome` does not exist in 1.19.2 (BATCH-FAULT, flesh count used). Stopped 06:58.
- 06:59 RB (SQ-CDNS-05 rollback): the O2 world (saved by the mods/ jars) on the O1 srv (1.2.02 jars). Done 4.5 s
  07:00:14 (pid 4063414), `p9_rb.qb`, stopped, `cdns_probe.py` scan before/after (`perf/runs/r1115_p9_rb/`).
- 07:02 B1 `NOSWAP=1 prep_srv.sh $S/p9b p9b B` (seed 8675309), Done 75.6 s 07:03:41 (pid 4072462): `p9_b1.qb`
  (SQ-RD-26 from r1102b r_B.qb, the one server sub-row of SQ-RDB-03 not rerun after the bake; SQ-RD-10 stays with
  r1114's INCONCLUSIVE and its owner TODO).
- 07:07 to 07:11 B1 `p9_b1.qb` + `p9_b1x.qb` (second jungle, 10 x 10 chunks). Stopped 07:11:56; game_jvms.sh empty,
  no crash report on any phase 9 boot. Runner-9 done (06:15 to 07:13). Verdicts in section 8. Not run: SQ-RD-10
  (r1114 INCONCLUSIVE stands), client rows SQ-CDNS-12, SQ-NSM-04, SQ-OB-02.
- 07:19 runner-10 (phase 10, client). `eval_client.py setup $S/p10cli` (instance mods/, 421 jars, Common 03:15, PG -18
  already in mods/), zz_perfeval_drive.js removed, c3_probe.js as zz_c3_probe.js, shipped config/debugify.json (sha1
  92d95991); sp_world copies p10a (= p1a1 superflat world) and nsmold (= p9o1_world_after_o1, the 1.2.02-saved NSM
  fixture). Launch 1 `eval_client.py launch $S/p10cli nodebuglog` for CQ-FDR-01/-02 then the deferred rows.
- 07:19 to 07:34 launch 1 (pid 4120314, 421 jars): `[jar handles] closed 419 stale mod jar filesystems (419 zip files):
  1091 -> 672 descriptors open` at 07:20:41, title 07:20:48 (69 s), sockets/pipes max fd 657 (CQ-FDR-01 again PASS). World
  p10a joined 07:22:02, JEI 25 s. SQ-PG-01b on gun portal pairs shot by the player (`zc3 shoot PerfEval`, walls at
  X 1395..1409 Z 1100 / 1120, floor portal 1392 1090; walks by xdotool keydown, new `tools/scratch_query/cli_do.py`):
  wall-to-wall both ways no `Adjusting` line; wall-to-floor one `Adjusting` line, player on the floor; floor-to-wall
  no line, but the player ends at y -61.0 (one block INTO the superflat grass, x 1402.5 z 1099.3), cannot walk or jump
  out (walk s/w and space: position unchanged); same after quit-and-reload. Gun portals do not survive the reload
  (`zc3 portals` 0; gun portal despawn by design, plan_portal_changes.md), so the repeat used fresh portals.
  SQ-PG-01c: a vanilla flint and steel lights soul fire and no portal (Creative Magic Nether portal rule, by design);
  Flint and Terrasteel lit an IP Nether portal 07:34:30 ("Generated Portal On Non Solid Surface"), the client loaded
  the Nether client world and DIED 07:34:33: SIGSEGV in libc from `GL11C.nglDrawElements` <- `BufferUploader` <- IP
  `ViewAreaRenderer.buildPortalViewAreaTrianglesBuffer` <- `RendererUsingStencil` (first see-through IP portal on a
  client: gun portals render opaque). hs_err `$S/p10cli/hs_err_pid4120314.log` (+ .jfr). No ClosedFileSystemException
  / ClosedChannelException in latest.log. Next: launch 2 (same dir) for the item rows, NSM, repro of the portal last;
  launch 3/4 with `$S/fdroff/creativecommon-1.2.0.jar` (FdReleaseMixin removed from the client list) for CQ-FDR-03 at
  421 jars and an A/B of the portal crash at 419 jars.
- 07:37 to 08:00 launch 2 (pid 4150311, 421 jars, same dir): `[jar handles] closed 419 ...: 1091 -> 672` again. p10a:
  CQ-SPD-01, CQ-SY-22 (Fertile Soil = immersive_weathering:mulch_block), CQ-SLK-01, CQ-HOP-01 (JEI Grape Planting 1/2
  logs, 2/2 lattice; no stomping tab; lattice tooltip), SQ-OB-02 names by give/hover, SQ-QN-02 (elytra from y 200: z
  1030 -> 1142 in 3.5 s, y 148 -> 137, glide; a bare xdotool `key space` is too short for the jump, `keydown` 0.3 s
  works), F3+T at 07:49:48 (reload finished, textures and "Spool of String" lang present after it, 0
  ClosedFileSystemException / ClosedChannelException in the whole log). nsmold (1.2.02 save): netherite lunchbox from
  the fixture chest renders in hand, red dye in the 2x2 grid and the smithing skin (golden lunchbox) render. SQ-NSM-04:
  the NSM fixture has no paintings or banner (nsm_fixture.js has no painting step): not runnable. CQ-PWG-66 stand-in
  (creative motor, not Power Grid): `create:display_board` (BE `create:flap_display`) beside a 16 RPM shaft end read
  `Speed 0.0f` with facing south, north and east. JFR dumped 07:59 to `perf/runs/r1115_p10_items/client.jfr`. Nether
  portal repro in nsmold (frame 30..33 -20, Flint and Terrasteel): portal 08:00:34, client DIED the same way (SIGSEGV
  libc <- nglDrawElements <- IP ViewAreaRenderer), `$S/p10cli/old_launch2/hs_err_pid4150311.log`. 2 of 2.
- 08:00:53 launch 3 (pid 4181712): `$S/fdroff/creativecommon-1.2.0.jar` (FdReleaseMixin removed) in place of the
  hard link, 421 jars: `*** stack smashing detected ***` at 08:01:17 (24 s), no `[jar handles]` line. CQ-FDR-03 PASS.
- 08:01:38 launch 4 (pid 4184933): fix off, 419 jars (clearvoid and packetfixer moved to `$S/p10cli/mods_out/`), for
  the Nether portal crash A/B.
- 08:02 to 08:05 launch 4 (fix off, 419 jars): title 08:02:46 (no `[jar handles]` line), nsmold, the same frame lit
  08:04:29, "Generated Portal On Ground" 08:04:46, client DIED with the same SIGSEGV stack (IP ViewAreaRenderer ->
  BufferUploader -> nglDrawElements -> libc), `$S/p10cli/old_launch4/hs_err_pid4184933.log`. So the Nether portal
  crash is NOT the jar handle fix (3 of 3, with and without FdReleaseMixin). Client mods restored (Common hard link,
  clearvoid and packetfixer back, 421).
- 08:05 to 08:18 CQ-FDR-04 / SQ-CDNS-12: `NOSWAP=1 PGJAR=jar_archive/pwg_gate/...-18.jar prep_srv.sh $S/p10srv p10srv A`,
  cdevperfbot removed (client has none). First boot (pid 6345, Done 9.9 s) had NO Power Grid: prep_srv.sh resolved a
  relative PGJAR after its `cd` into OUT/srv and skipped it silently ("[absent mods] ... absent powergrid"). The client
  (pid 12166, with PG -18 from mods/) joined 08:09:00 and crashed 08:09:04: `NullPointerException: Registry entry not
  present: powergrid:electrozapper` in PG `ElectroZapperRenderHandler.appliesTo` <- Create
  `ShootableGadgetRenderHandler.onRenderPlayerHand` (a client with Power Grid on a server without it crashes instead of
  being refused; crash report `$S/p10cli/old_launch5/crash-reports/`). Tooling fix: prep_srv.sh resolves PGJAR against
  the instance root and prints "PGJAR NOT FOUND". Reboot 08:12 (pid 26765, Done 3.9 s, PG present, no `[jar handles]`
  line in the server log); client (pid 31081) joined 08:13:51, gave Netherite Lunchbox, Spool of String, Salt,
  Electro-Zapper, hover shot; JFR `perf/runs/r1115_p10_srv/client.jfr`; closed. Old client: the deploy/latest (1.2.02)
  creativedelight-1.1.0.jar in place of the link (pid 35320): refused 08:17:07 "Failed to synchronize registry data
  from server" with the mod version list (Creative Delight 1.1.0 / 1.1.0, Hearth and Harvest 1.4.1-ce-19.2-04 on both
  sides: the version strings were not bumped, so the list shows equal versions), no crash; JFR `client_old.jfr`.
  Client closed, server stopped 08:17:5x, game_jvms.sh empty; client mods restored to the instance links.
- Tooling (runner-10): new `tools/scratch_query/cli_do.py` (one-off client steps: chat commands, timed key holds,
  hover, click, type, screenshots, log counts); `eval_client.py launch ... ownsrv=PID` (ignore your own scratch server
  for rows that need a server and a client); prep_srv.sh PGJAR fix above.
- Gotchas for the write-back (runner-10): (1) a bare `xdotool key space` is shorter than a tick: the elytra second jump
  never registers; hold it (`cli_do.py walk:0.3:space`). (2) A chat command typed right after closing a screen, or
  while the client hitches after a join, can lose the opening `t`: the letters then act as hotkeys (SeasonHud options
  opened twice) or go out as a chat message; screenshot before a command burst and space commands 1 s apart after a
  join. (3) `zc3 shoot PerfEval` works on the SP player: wall portals from 2.5 blocks at pitch 20 sit at centre y -59
  (walkable from a superflat floor); flatter shots land at -59.5 (bottom below the floor, not walkable); a shot onto
  an existing portal's spot returns false. (4) Gun portals are gone after quit-and-reload (despawn by design), so
  "repeat after a reload" needs fresh portals. (5) Vanilla flint and steel lights soul fire and no portal (Creative
  Magic Nether rule): light frames with `creativemagic:flint_and_terrasteel`; any see-through IP portal currently
  kills the client (SQ-PG-01c). (6) prep_srv.sh PGJAR must be absolute before r1115 10's fix. (7) eval_client launch
  refuses while your own server runs: `ownsrv=PID`.
- 08:18 runner-10 done (07:15 to 08:18). Verdicts in section 8.

### Phase 11 (PTH, runner-11)

- 11:33 runner-11 preflight: no game JVM, 73 GB free. Build jars: creativecompat-1.0.0 11:31 (carries
  `data/creativecompat/kubejs/tags/passthrough_tags.js`, same as the instance kubejs copy), creativedevtools-1.0.0
  00:40. No scratch datapack for SQ-PTH-01 to -04 (real tags). New probe `tools/scratch_query/zpth_probe.js` (modifier,
  per-tick watches, bot item use and release, watchnew), batch generators `tools/scratch_query/pth_gen_a1.py`,
  `pth_gen_a2.py`; batches `batches/r1115/pth_a0.qb`, `pth_a1.qb`, `pth_a2.qb`; results `perf/runs/r1115/pth_a1/`.
- 11:39 A1 `SKIPSWAP="creativebrewing creativecommon creativedelight creativeworldgen repackedStorage" COPYDEV=1
  prep_srv.sh $S/p11a p11a A difficulty=normal spawn-animals=true spawn-npcs=true spawn-monsters=true`: swaps only
  creativecompat (11:31) and creativedevtools (00:40); every other Creative jar kept or skipped. srv toml before boot:
  `[surface.passthrough] arrowLifeTicks 3600`, `arrowLongLifeCap 3`, `[debug.passthrough] timing true`. Boot 11:39:32,
  Done 13.0 s (pid 253247).
- 11:42 `pth_a0.qb`: SQ-PTH-01, -05 PASS; -19 tags (overlap lines 0, sizes from a separate `dumptags block` /
  `dumptags entity_type`: the comma form is not accepted); -16a BATCH-FAULT (the live file's value 4 equals the
  default, so the log reads `... minLayers (kept)`; the 6 case runs on boot 2).
- 11:47 `pth_a1.qb`: PTH-20 two samples popped (sugar cane on farmland, quark rope with nothing above): rerun with
  support in `pth_a2.qb` (SQ-PTH-20x). PTH-21: pickarang, firework, revolver shot and mana burst discard themselves
  on the summon tick (no owner / no lifetime); membership taken from the in-game /dumptags export instead.
  Zombies with an `ActiveEffects` slowness NBT were NOT slowed (speed 0.23, they wandered off their blocks): mob rows
  rerun with base movement speed 0 instead.
- 11:55 `pth_a1r.qb` (mob rows on base speed 0 zombies, generator `PTH_ROWS=... PTH_OUT=...`), 12:00 `pth_a1s.qb`
  (pushed mobs: zombies do not chase perfBot bots, so "keeps moving" rows push speed-0 mobs with `zpth motion` 0.25
  every 0.5 s against an open-grass control lane). The srv copy of the probe lacked `watchnew` until 12:01 (SQ-PTH-24r,
  -03r ran without it: rerun as -24s, -03s in `pth_a2.qb` after its /reload).
- Findings so far: IW leaf pile thorns check leggings only on players (`instanceof Player` in `LeafPileBlock.entityInside`),
  so the leggings half of SQ-PTH-18 cannot run on zombies, and bots are invulnerable: not testable on a server.
  Static spiders in barbed wire take no damage (Create Crafts and Additions hurts moving entities only); pushed ones do.
- 12:00 `pth_a2.qb` started (projectile and bot rows, SQ-PTH-13/14 despawn timing, -15, -25, -26, -27, SQ-HOP-05, then
  SQ-PTH-14c and save-all for boot 2). Tooling: `tools/perf/suite/server.py` takes `PERF_JARS="a.jar b.jar"` (build
  jars copied over the mods/ jar of the same name prefix; the suite otherwise measures mods/ only, whose Compat
  11-14 23:54 has no tags) for SQ-PTH-28.
- 12:00 to 12:11 `pth_a2.qb`. `zpth face` threw (Rhino finds no `setYRot` on perfBot's BotPlayer), so every bot
  throw went due south: harmless except the explosive arrow of SQ-PTH-10 (shot away from the cube; rerun -10b with a
  `tp ... 180 0`; the probe now turns bots with a relative tp). SQ-PTH-14 test 1 ran late (the 4th arrow came after the
  first three had reached their 3 min life): rerun -14e. SQ-PTH-15: `datapack enable "file/pth15"` before a /reload
  answers "Unknown data pack" (not enabled), and an empty cart on rails loses 4 % a tick and stopped 1.5 blocks before
  the web: rerun -15b with a scratch `srv/kubejs/data` tag and carts 2.5 blocks before the web. SQ-PTH-27: lines kept
  coming 50 s after `timing = false` was saved (live toml edits not picked up, gotcha r1115).
- 12:11:18 save-all, 12:12 stop (JVM gone 12:12:11). srv toml: `[leaf_piles] slowFromLayers = 6` put back at the top.
  Boot 2 12:12:20, Done 4.6 s (pid 309686); `pth_b1.qb` 12:13 (`perf/runs/r1115/pth_b1/`).
- 12:13 to 12:19 boot 2 `pth_b1.qb` + `pth_b2.qb`: SQ-PTH-16 PASS (value 6 moved), -14d, -14e, -10b, -11b, -12c judged;
  the SQ-PTH-06 items and the SQ-PTH-12 pickarang item had despawned at their vanilla 5 minutes before boot 2 (redone
  as -06r / -12c), the z 1112+ lanes were outside the setup forceload after the restart (entities summoned there never
  ticked: -15b BATCH-FAULT, redone as -15c with a forceload). 12:19 forceload 992 1112 1039 1215 + save-all, stop.
- 12:20 boot 3 (Done 5.0 s, pid 323632) `pth_b3.qb`: SQ-PTH-16b PASS, -07r PASS, -15c PASS.
- 12:23 client `eval_client.py setup $S/p11cli` (instance mods/, 421 jars) with creativecompat 11:31 and
  creativedevtools 00:40 COPIED over the links (same two swaps as the servers; nothing else swapped),
  `backportedPowerGrid-0.6.2-ce-19.2-18.jar` moved to `$S/p11cli/mods_out/` (the scratch server has no Power Grid; a
  client with it crashes on join, r1115 phase 10), zz_perfeval_drive.js removed, zz_zpth_probe.js added. Launch
  `eval_client.py launch $S/p11cli nodebuglog server=127.0.0.1:25599 ownsrv=323632` (client pid 331072). Driver
  `tools/scratch_query/pth_client_drive.py` (server console for setup and zpth, xdotool key holds, screenshots).
- 12:25 the orchestrator's process exited; server 323632 and client 331072 gone before the client had joined
  (resource load). 12:29 boot 4 (Done 4.6 s, pid 350136), client relaunched (pid 354006, `PerfEval` joined 12:32:23,
  opped). Driver scenarios cq01, cq01b (survival wheat, wading from inside the paddy, flight via a new `zpth fly`,
  climb from inside the rope), fov, cq02, cq03, cq03b, cq04, cq04b; summaries with new
  `tools/scratch_query/pth_cli_summary.py` / `pth_cli_speed.py` (client speed from position steps: the server's motion
  for a real player is not the client's, so server-side `mod` and `climb` are only indicative for players).
  Gotchas: /reload on this pack takes about 50 s and queued console commands then all run in the same tick; the first
  scenario lane after setup commands sometimes logs no watch lines; spectator eye is feet + 1.62 (first WTHIT pass
  aimed at the sky). JFR `perf/runs/r1115/pth_cli/client_mp.jfr`. Client closed, server stopped 12:44.
- 12:45 CQ-PTH-05: client `[debug.passthrough] timing = true`, `sp_world.py` p11a -> saves/p11sp, launch (pid 362371)
  auto-joined a single player world; wheat field walked 5 x 4 s; parser: both side=server and side=client blocks,
  client inside 0.34 per tick (peak 1), share 0.39 % (`pth_cli/cq05_parser.txt`); JFR `client_sp.jfr`; closed 12:48.
- 12:48 SQ-PTH-28 `PERF_JARS=... perf_suite.py run passthrough pt1`: the suite copied the CURRENT build jar, which
  another session rebuilt at 12:18 (846337 bytes, not the 11:31 jar under test): runner 368140 and server 370226
  killed at once (pt1 discarded). The 11:31 jar (845639 bytes, the scratch srv copy) and Devtools 00:40 saved to
  `perf/runs/r1115/pth_jars/`; 12:49 `run passthrough pt2` with those (server pid 375826).
- 12:49 to 13:38 SQ-PTH-28 `pt2`: 14 windows ok, server gone at the end; `report pt2` (no --prune). 13:38 SQ-PTH-27
  rejudged from boots 2 to 4 (timing = false in the srv toml before each boot, 0 timing lines). No JVM of runner-11 up.

### Phase 12 (PTH fixes, runner-11)

- 15:00 runner-11 preflight: no game JVM. Jars for the run: the saved copies in `perf/runs/r1115/pth_jars2/`,
  creativecompat-1.0.0.jar 14:59 (856166 bytes) and creativedevtools-1.0.0.jar 14:39 (394956 bytes). Both also carry
  another session's uncommitted edits (Compat `IPExitLiftMixin`, Devtools `CatalogueRules`). Batch generator
  `tools/scratch_query/pth_gen_p12.py` written but not run (it writes `batches/r1115/pth_p12a.qb`: setup, SQ-PTH-03b,
  -06b, -11c, -25b, save; and `pth_p12b.qb`: the SQ-PTH-06b restart half).
- 15:01 STOPPED by the owner (redirect relayed by the pass-through session) before the first boot: no server, client
  or perf suite was started in phase 12 and no row ran. game_jvms.sh empty.
- Deferred (not run): SQ-PTH-03b, SQ-PTH-06b, SQ-PTH-11c, SQ-PTH-25b, SQ-PTH-28b, CQ-PTH-03b.
- Resume point: there is no phase 12 world yet.
  1. `SKIPSWAP="creativebrewing creativecommon creativedelight creativeworldgen repackedStorage creativecompat creativedevtools" COPYDEV=1 prep_srv.sh $S/p12a p12a A difficulty=normal spawn-animals=true spawn-npcs=true spawn-monsters=true`.
  2. Remove the srv/mods copies of Compat and Devtools, copy in the two `pth_jars2/` jars, and copy
     `tools/scratch_query/zpth_probe.js` to srv/kubejs/server_scripts/zz_zpth_probe.js.
  3. `python3 tools/scratch_query/pth_gen_p12.py` (fix the second `waitlog` in SQ-PTH-25b first: its pattern
     needs to match the second "[cdevbench] passthrough: ... calls per case" line).
  4. Boot, then `qrun.py run $S/p12a batches/r1115/pth_p12a.qb perf/runs/r1115/pth_p12/` from row P12-setup.
  5. Restart for `pth_p12b.qb`.
  6. CQ-PTH-03b on a client joined to boot 2 (`pth_client_drive.py`; add a lane along a plain ladder column).
  7. `PERF_JARS="<the two pth_jars2 copies>" perf_suite.py run passthrough pt3` for SQ-PTH-28b.

## 8. Final verdicts

| ID | Result | Evidence |
|---|---|---|
| SQ-TPF-00 | PASS | A1: Debugify "Enabled 8/34 bugs!" (9 shipped fixes, 8 server side), Packet Fixer "applied successfully", no mixin apply error; shipped packetfixer.properties in place (`allSizesUnlimited=false`, timeout 120) |
| SQ-TPF-01 | PASS | A1: shipped debugify.json (sha1 92d95991, same as configs_edited) boots to Done, no mc121706/BowAttackGoalMixin; post-stop diff empty; fix rows SQ-TPF-11 to -17 all PASS (qrun) |
| SQ-PG-01a | PASS (server half) | A1: no ExitLift line in the log; jar has no `portalgun/PortalGunExitLift`, `IPExitLiftMixin` only in the `client` list; fresh gun portal pairs (wall/wall, wall/floor) all `adjustPositionAfterTeleport=1b` (portalgun's own value, no recompute). Old-save half not run: no world with pre-11-14 portals kept (r1114 fixc3a1 world has 0 portals) |
| SQ-PG-01b | skipped | client row (needs a window): client-deferred |
| SQ-PG-01c | skipped | client row: client-deferred (CQ-PG-03 likewise) |
| SQ-GAP-04 | PASS | A1 + `p1_a1b.qb`: plate and wire tags hold rose gold sheet/wire; item tag `forge:storage_blocks/gold` holds `mca:rose_gold_block`; clock, mechanical crafter, powered rail (gold rod tag), golden apple, pickaxe, chestplate, carrot (nugget tag) take the tag; `gold_ingot_from_nuggets` and `minecraft:gold_block` literal. qrun FAIL was batch (block tag, vanilla recipe ids) |
| SQ-T1-04 | PASS | A1: rose gold ingot used by clock, mechanical crafter, golden pickaxe, `creativedelight:shaped/golden_apple`; powered rail through the gold rod tag (`p1_a1b`); not by gold block or gold bars; component_gold takes plate/wire tags. qrun FAIL was batch (vanilla ids) |
| SQ-LGM-13 | PASS | A1: catalog 11-14 22:30: pot pie block feast/whole, serving feast/serving, omurice feast/serving, omurice block feast/whole, tomato egg soup soup/soup, cod deluxe and hearty lunch meal/plate; live feast/whole tag (33) holds both blocks, cake/whole (14) holds minecraft:cake; no food_family tag error |
| SQ-CBP-11 | PASS | B1: Equipment 37 transitions, Engineering 67; packs `creativeequipment:felsic_gear` (+nullscape) enabled; no incomplete/duplicate error; 25 felsic blocks in the End highlands box (688 594); one break 1 scrap, 50 rolls 73 |
| SQ-OB-05 | PASS | B1: 4 recipes (2 cutting + 2 Slice & Dice copies): cave carrot -> 2 raw_vegetarian_patty, baked cave carrot -> 2 cooked_vegetarian_patty |
| SQ-VH-06 | PASS | B1 (Heap at -10 6): bedrock 2304/2304 at y 0 and 127; interior Heap cells (27 cells all Heap): netherrack 0 of 142461; 945 netherrack only in border cells. Floors flesh+porous 1942/2926, ceilings 2767/3866: rest structure (fortress bricks) and Heap decor (tendons, hair, pus); natural non-flesh floor 51 (97 %), ceiling 35 + 258 tendons |
| SQ-VH-06b | PASS | B1: cave_air 4704 in Heap cells; `minecraft:nether_carver_replaceables` holds flesh and porous_flesh |
| SQ-VH-06c | PASS (finding) | B1: nether_wastes, crimson, warped, soul sand, basalt keep vanilla blocks; wastes quartz 267, gold 74; flesh 0 in four biomes. Warped box 32 blocks from the Heap: 2212 flesh in warped cells, 213 interior, all within 12 blocks of a Heap cell, zoomed biome warped at each: spill at the Heap border (features), not the surface rule (qrun FAIL was the per-cell regex) |
| SQ-HH-328 | PASS | three seeds, shipped 120 s: 8675309 77 s (4 misses, 264 candidates, 3315 blocks); 20261115 7.9 s (18 candidates); 424242 39.4 s (297 candidates); centred census (seed 424242): corn 1026, oak_fence 16, centre chest `corn_maze_center`. Row box M-2..M+36 covers one quadrant (fence 8): batch/row fix. `zq under` lists no corn_maze start (probe gap; under-base check not judged) |
| SQ-CDW-01 | PASS | CDW1/2/3 (myrtrees, fruitsdelight, endersdelight removed): Done, no datapack error; off [myrtrees] / [fruitsdelight] / none; srv kubejs/data/creativedelight has no worldgen |
| SQ-CDW-03b | PASS | CDW2: 17 skip lines, all `<rule>: needs fruitsdelight` (hamimelon + 16), 0 naming a fruitsdelight feature, apple not skipped |
| SQ-PWG-91 | PASS | p2_r1 (-18): all four attaches SUCCESS, stone PASS x2; helpers south/east x3; drops (B36) every read lists tile + attachment: lamp 2/2 (tile, factory_light, light_bulb), connector 2/2, junction and solar 2/2 after wrench and reattach; wrench lamp gives bulb + light, others their attachment |
| SQ-PWG-97 | PASS | B37: gauge 12.0 V; connector stays after the control stone; support removed: air, wire_connector 1 (1 entity), wire 4 (2 entities), gauge 0; heavy on top: air, heavy_wire_connector 1, wire 2; no ERROR |
| SQ-PWG-43 | PASS (servo part) | 0.5 V step generated=4 for about 33 ticks then 0, idle 0.6244 A; B9 unpowered generated=0; B38 powered again generated=4 for about 40 ticks (moving within about 13 ticks; -17 gave 1 for 105) then idle 0.6244 A. Rheostat and variac not rerun (unchanged since r1113 PASS) |
| SQ-PWG-06 | PASS | 19 watches firstConverged=1 settledAt=1; diode 0.624 V / -5.0; NPN Vbe 0.474 Vce 4.09, Ic/Ib 20, 5 k Vce 0.036; PNP mirrored; triode 14.36 / 13.04 mA, heater off 0; pentode 21.4 / 17.6 mA; regulator 60.71 / 61.11 V; thyratron (heater 6.5 V as pwg12): -30 then anode up: off (T4 199.8); grid 0 fires (T4 12.0); back to -30 latched (12.0); anode 0 and back: off (199.8) |
| SQ-PWG-80 | PASS | `powergrid:mixing/acid` absent (115 recipes); KubeJS log `power grid recipes done`; burners kindled; items first: no items left, acid 250 + water 750, no salt; etch: incomplete_circuit Schematic components=3, acid 750; mixer 64 RPM |
| SQ-PWG-80b | PASS (recorded) | water first: Creative Delight `water_salt` took the water (tanks empty), then Create: The Salt's salting mix (`create__the_salt:rotten_flesh`) salted the rotten flesh with that salt: output slot rotten flesh x1, redstone and blaze powder left, no acid, no salt left; no ERROR |
| SQ-PWG-82 | FAIL | p2_r1 Z 1906 and p2_r1c Z 1926 identical: circuit locked @4.498 at Assembly 1/3 (missing connector 2) for 20 s, never a circuit_board; the arm took the whole wire_connector x8 stack onto the belt before the circuit arrived (rode to the end depot, which holds it), then took the whole resistor x8 stack, inserted 1 and holds resistor x7 (`HeldItem`), so it never fetches connectors; stone depot x64 unchanged (B25 fine) |
| SQ-PWG-54 | PASS (prereq, X 1380) | panel A 20.5 V / 7 ohm (60 W); 4-panel string 83.7 V / 30 ohm (233 W, 3.9x); `Loaded 2 solar biome overrides`; panel B override temp 34.4 19.6 V (55 W); night 0; bearing assemble running, controlled=1 attachedToBearing=true, 24.4 V |
| SQ-PWG-54r | PASS | restart: after forceload and 100 ticks one controlled entity removed=false blocks=2 attachedToBearing=true, bearing movedContraption=set, 24.4 V at noon. Probe prints `controller=none` before and after the restart (row text expects `controller=BX,-60,1510`: row/probe text fix) |
| CQ-PWG-66 | deferred | client row (client-deferred) |
| SQ-MELT-01 | PASS | p3_a1r (target roofed): touching ice -> `water[level=0]`, snow block, powder snow, layer -> air, Salt Block kept; tags salt 39. Open-air first pass the same |
| SQ-MELT-02 | PASS | p3_a1r roofed: fire, soul fire, lit campfire, lit soul campfire, magma: ice and snow at 3 melted, at 4 kept (all five); unlit campfire ice kept. The open-air p3_a1 qrun FAIL (every 4 melted) was Serene Seasons' surface melt: roofed no-source controls kept, open ones melted (P3-RC) |
| SQ-MELT-03 | PASS | p3_a1r roofed: lava source melts at 4, not 5; flowing lava (fed trench, cell `lava[level=4]`, ice 6 from the source) at 4; lava cauldron at 4 |
| SQ-MELT-04 | PASS | randomTickSpeed 100, 5 min: four blue ice blue -> packed -> ice -> water, no skipped tier; slow steps about 1100 ticks each (expected 820), ice -> water 17 to 170; packed touching fire and 2 from lava stepped, 2 from fire and 3 from lava kept |
| SQ-MELT-05 | PASS | Nether roof: ice beside salt -> air, no water step; a no-salt Nether ice did the same in the same tick (Immersive Weathering, melting.md 6) |
| SQ-MELT-06 | PASS | part 1 (p3_a1r roofed): torch, soul torch, lantern, sconce, copper lantern, glowstone melt ice and snow at 3, not 4; sea lantern, redstone torch, jelly torch, unlit brazier ice kept. Part 2 (A2, `vanillaLightMelting true`): sea-lantern ice (light 14) melted, both the new one and the roofed part 1 one; redstone torch ice kept |
| SQ-MELT-07 | PASS | sealed stone room: ice 3 from magma melted right after randomTickSpeed 1000; source-driven line `[minecraft:magma_block (light 3)]` |
| SQ-MELT-08 | FAIL | no per-click melt: a `vintagedelight:salt_dust` click (the pack's only salt; `salt:salt` turned into it) places a `vintagedelight:salt` layer on the ice, which then melts the ice under it at the block rate; the next click FAILs. `tryMeltFromItem` (-> Common `meltStep`) is reached only from `SaltItemDispenseBehavior` on `salt:salt`, which returns unless the stack is `#forge:salts` (holds only salt_dust): a salt:salt dispenser does nothing, a salt_dust dispenser drops dust. Owner choice |
| SQ-MELT-09 | PASS | Common + Compat set (C1): tags salt 0, fire 13; fire pair ice 3 melted / 4 kept, snow 4 kept (snow 3 already air at the first read); lava 4 melted / 5 kept; 0 KubeJS errors after /reload. qrun FAIL = Forge fresh-config WARN (batch). Minor: Rare Ice pseudo-mixin WARN `Error loading class ... RareIceBlock` with Rare Ice absent |
| SQ-MELT-10 | FAIL (finding) | `[melting] tags: 6 meltable, 2 slow, 39 salt, 123 fire, 1 lava, 3 ultra-warm instant`; source-driven magma only; WARN names `architects_palette:glowstone_cage_lantern (light 3)`: measured lit = block light 3, unlit 0. Owner: source-driven or drop. Live tag read with `/zq blocktag` (rows/SQ-MELT-10.log), not a /dumpall |
| SQ-MELT-P1 | PASS | overworld MSPT 2.578 (glacier) vs 2.395 ms (control), 0.18 ms; Melting 1.00 % of Server thread JFR samples in the glacier window (1.19 % in the control: background from the test area). Box top ice melted by Serene Seasons (open air). Note: `onRandomTick` reads ForgeConfigSpec values per tick (267 samples over the boot) |
| SQ-SALT2-06 | PASS | overworld (roofed rerun and open): ice -> water source, snow -> air, Salt Block kept; Nether: ice and snow -> air; installed CD nests salt -03 with `tryMeltFromBlock` returning false (no double melt) |
| SQ-SALT2-06b | PASS | Nether, survival bot: placed snow layer and snow block air at once (no state seen by the per-tick watch), stacks 4 -> 3; setblock powder snow air; dispenser placed and melted a snow block (Items 4 -> 3, no item entity). Particles not checkable on a server |
| SQ-AZW-01 | PASS | rerun: 30 `quark:azalea_log` in the tree box, 0 oak log; give keeps `quark:azalea_log` x4; `[removedWoods] 4 woods merged into oak`; no `[item_dict] Missing`, no sunflower_stem line |
| SQ-DCM-02 | PASS | rerun: 0 KubeJS errors after /reload; `[absent mods]` names none of towns_and_towers, hopo, unwrecked_ships, yuushya, cozys-improved-cats; recipes 32042 vs pack dump 32168, the 126 missing are Power Grid (gate jar not in scratch boots) |
| SQ-SPD-01 | PASS (row text) | 24 wheat over the 5x5, itemCount 30 -> 6, pouch in slot 0, no Exception. The dispenser stands in its own 5x5 (centre = block in front, plan_bugfix_1114 1), so 25 is unreachable sideways: row text fix |
| SQ-SPD-02 | PASS (row text) | front block farmland: 24 wheat at -59, 0 at -60, itemCount 6 (dispenser cell, as -01) |
| SQ-SPD-03 | PASS | 4 wheat; pouch in slot 0 with `tag: {}` (no storedItem / itemCount) |
| SQ-SPD-04 | PASS | over stone: 0 wheat, itemCount 30, pouch in slot 0 (fail-click sound not readable on a server) |
| SQ-SPD-05 | PASS | empty pouch: 0 wheat, pouch in slot 0 |
| SQ-SPD-06 | PASS | bundle: 1 wheat on the centre; Items wheat_seeds 2b then dirt 1b; bundle in slot 0 |
| SQ-SPD-07 | PASS | dirt first: nothing planted, NBT unchanged |
| SQ-SPD-08 | FAIL | Nether pouch of nether wart plants 0 at y 129 and y 61, itemCount stays 10; overworld soul sand control plants 10; Nether dispenser fires (cobblestone 3 -> 2); `SeedContainerDispense.plantOne` fake player cached by profile (evidence restored by the orchestrator from runner-4's report, 03:25) |
| SQ-HH-1574 | PASS | rerun: no crash; 20 wheat under the down-facing dispenser; pouch kept with `tag: {}` |
| SQ-HH-1575 | PASS | cobblestone pouch: 0 wheat, itemCount 20; empty pouch: 0 wheat; no error |
| SQ-HH-309 | PASS | all 8 mums in blocktag and itemtag minecraft:small_flowers, flowerpatch:bonemeal_able_flowers, terralith:plants, c:ignoredblocks, vinery:small_flower, another_furniture:planter_box_placeables, creativecompat:flower_box_plants |
| SQ-HH-1593 | PASS | only `creativedelight:shapeless/sugar_from_syrup` -> 4 sugar (1 match), no rusticdelight id |
| SQ-HH-1505 | PASS | shod horse: cactus and berry accepted=false, health 53 -> 53, attackEventPasses=false; unshod control accepted=true 53 -> 52; ForgeData holds `hearthandharvest:horseshoe` (first run: horses discarded by spawn-animals=false, batch fault) |
| SQ-HH-1507 | PASS | sneak use with empty hand, shears, and a horseshoe: interact CONSUME, shod=false, mods empty, ForgeData `{}` each time; offer 1, 1, 2; bot holds 2 horseshoes at the end. Screen not checkable on a server |
| SQ-HH-1534 | PASS | rerun alone, sneak true printed: farmland 25, hoe damage=25 |
| SQ-SLK-02 | PASS | armor stands rest at -59.25 / -59.5 / -59.75 (lightly / well / heavily, waxed twins the same), -59.0 on waxed_salt_block |
| SQ-SLK-03 | PASS | heavily + 1 layer -> well_licked, air; heavily + 3 -> salt:salt_block, air; well + 5 -> salt_block with layers=3 on top; salt_block + 1 -> layer stays; waxed heavily + 1 -> waxed_well_licked, air; 0 item entities |
| SQ-SLK-04 | PASS | salt_dust click well -> lightly (dust 4 -> 3); `vintagedelight:salt` click lightly -> salt:salt_block (4 -> 3); dispenser heavily -> well, dust Count 3b left, no item entity |
| SQ-HH-434 | PASS | handFeeding off: feed attempts=5 tamed=0 held corn x16; InLove 0 on all 5 crows |
| SQ-HH-440 | PASS | interact 2 held=empty; InLove 580 on both; after 40 s tally crows=3 (parents on the bot's shoulders) |
| SQ-HH-440b | PASS | cornBreeding off: wheat seeds set InLove 579 on both, tally crows=3 after 40 s; hurt tame crow Health 2.0 -> 4.0, InLove 0; reset to true. qrun FAIL = batch regex (`cfg cornBreeding`, not `crow.cornBreeding`) |
| SQ-HH-482 | PASS (note) | cow at 21: StayNearSaltGoal seen, pulled to lastD 17.27 and on to the salt (d 1.6 to 2.9); salt block licked away to air (stage chance 1.0). Cow b (24) strolled to 22.3 and then also reached the salt (search radius 22): the row's "b not seen" only holds while b stays at 24 |
| SQ-SLK-01 | PASS (finding) | cow at 18 reaches the salt (closest 2.25 at about 420 ticks); SLK-01b 80 s follow-up: stays near, never licks in 1600 ticks, stage chance 1.0, block still `salt:salt_block` (evidence restored by the orchestrator from runner-4's report, 03:25) |
| SQ-FDI-03 | PASS | vanilla cake becomes `supplementaries:directional_cake` on bite 1; cake_slice carrotHas/applepieHas true after bite 1, applepieEntries 1, 2, 3 over 3 bites; minecraft:cake false in both books |
| SQ-FDI-03b | PASS | cake used on a cake -> `supplementaries:double_cake`; 2 bites: cake_slice true in both books, entries 1, 2; minecraft:cake false |
| SQ-BBQW-02s | FAIL | stick not lost: Supplementaries places it as a `supplementaries:stick` block on the basin (also r1113's "no drop"); `StorageBlock.use` tool branch returns PASS (evidence restored by the orchestrator from runner-4's report, 03:25) |
| SQ-BBQW-02 | PASS | 2 beef + stick + cabbage: raw_beef_skewer in the inventory |
| SQ-CBW-03 | PASS | the pumpkin pie goes into the birch board (cutting_board Inventory 0:1 pumpkin_pie), air above, bot hand empty, no error |
| SQ-CDW-02 | PASS (note) | fresh B world seed 8675309: packs on [delightful, enders_additions, ends_delight, farmersdelight, fruitsdelight, fungidelight, hearthandharvest, myrtrees, oceanic_delight], off none; jungle box 160x160: 40 rubberwood logs (about 8 trees; r1113 0), 3358 jungle logs. Fewer than the old "1 chunk in 4" estimate |
| SQ-DEP-02, SQ-MIN-03 | deferred | phase 7 |
| CQ-SPD-01, CQ-SLK-01 | deferred | client rows |
| SQ-HOP-01 | deferred | "fold into SQ-DEP-02": runs on phase 7's no-Brewing boot. Full-pack control (A1): items hops, hops_salad, hops_bag, wild_hops; blocks hops_vine, hops_bag only (no H&H lattice, no `hearthandharvest:hops` block) |
| SQ-HOP-02 | PASS | A1: use CONSUME, `hops_vine[north=true,age=0]` Test passed, hand hops x1 |
| SQ-HOP-03 | PASS | A1 (mid autumn, sky 15): age 0 -> 1 -> 2 -> 3 within 15 ticks at randomTickSpeed 3000 |
| SQ-HOP-04 | FAIL (owner) | age 3 + stick x3: 2, 1, 1 hops, age 1 each; age 2: 0 hops, age 1. Age 1 + stick: `use` PASS from the vine, then Supplementaries places `supplementaries:stick` in the vine's space (vine replaced, -04x). Any placeable item does the same (vines are replaceable) |
| SQ-HOP-05 | FAIL | -05x: 1st shears use = Compat VineShearing (`fertile=false`, event cancelled, STERILIZED untouched); 2nd use toggles `sterilized=true`, 3rd back to false. `fertile=false` does not stop growth (HopsVineBlock overrides randomTick): 606 grew age 1 -> 3 at fertile=false |
| SQ-HOP-06 | FAIL (owner) | B1 seed 8675309: 0 `hops_vine` over 3 forest boxes (site 2: 256 chunks, 2572 oak + 2634 birch logs; site 1b 256 chunks; site 3 225 chunks). `/place feature creativedelight:wild/wild_hops` at 25 forest points (16 apart): placed once (3 vines), 24 "Failed". Carrot comparison void: wild carrots 0 and adzuki 0 in the same boxes although forest is in both crop tags |
| SQ-HOP-07 | PASS | A1: use SUCCESS, `lattice[age=1,grape=hops]`, hand x1 |
| SQ-HOP-08 | PASS | A1: open lattice age 1 -> 4 in 3 ticks of growth at 3000; dark one (block 0, sky 0) stayed age 1 |
| SQ-HOP-09 | PASS | A1: ripe + stick: 3 hops, age 1; age 3 + shears: 3 hops (2 + 1 seed), age 0 |
| SQ-HOP-10 | PASS | A1: pot use FAIL, hand still 3 hops; jungle white seeds `lattice[age=1,grape=jungle_white]`; registry part in SQ-HOP-01 row |
| SQ-HOP-11 | PASS | A2 (rerun on a fresh old-id save, p5_o2/p5_a2): `[swaps] minecraft:block: 2 id(s) remapped`; before any random tick lattices age 4 / age 2, facing south, grape none; after 5 s at 3000 grape=hops. A1 run: batch fault (randomTickSpeed 3 before the check: 622 grew to 4) |
| SQ-HH-1546 | PASS | A1: loot 5 end stone (qrun FAIL = regex expected `5`, probe prints `5.0`: batch fault); id in mineable/pickaxe; ender seeds plant (`endercrop:ender_crop`) |
| SQ-HH-1546b | PASS (note) | A2: loads as `endercrop:tilled_end_stone[moisture=0]`; reverts to end stone on its first random tick (Ender Crop: bare tilled end stone, like dry farmland), which is why A1 read end stone |
| SQ-HH-1546c | PASS | A1: iron and diamond use PASS, end stone stays; netherite tills; tilling diamond (sneak) tills 0 of 9; tilling netherite 9 of 9 (-1546c2 at randomTickSpeed 0; A1's 8 of 9 = one cell reverted by a random tick). Action bar text not visible to a bot |
| SQ-HH-266 | PASS | A1: water 1000 in, bucket out (hand water_bucket, tank 0); empty basin: bucket use PASS (kept), dirt use places a dirt block above (block item), slots empty; hopper above keeps 4 dirt + bucket, slots empty |
| SQ-HH-266b | PASS | A1: 2x2 group; white, savanna red, jungle white: villager fall stomp gives 1 `white_grapejuice_wine_bottle` / `savanna_red_grapejuice_bottle` / `jungle_white_grapejuice_bottle`, basin empty; lone basin takes the 3 grapes, wine bottle remainder 1 |
| SQ-HH-266c | PASS | A1: insert remainder 3, slots empty; bot click use FAIL, hand 3 hops |
| SQ-MELT-09b | PASS | C1 (Common + Compat set, Common 03:15): no Rare Ice line at all, tags `0 salt`, fire/lava pairs as -09, 0 KubeJS errors after /reload. Full pack (A1): no RareIce mixin line; `rare-ice:rare_ice` with a diamond beside a sea lantern kept 60 s at vanillaLightMelting false, melted (t=239, diamond dropped) after a restart at true: the redirect applies |
| SQ-MELT-P1b | PASS (1 clause inconclusive) | MSPT 3.193 (glacier) vs 3.235 ms (control); Melting#onRandomTick 0.30 % / 0.10 % of Server thread samples (P1: 1.00 / 1.19 %); no ConfigValue frame under onRandomTick in the boot (P1: 267). Toml-edit clause: the Forge config watcher never reported a change (no "Config file ... changed" debug line for 4 edits; watcher thread in ModernFix NightConfigWatchThrottler), so a live edit cannot be judged here; a restart picks the value up (rare ice row) |
| SQ-HH-304 | FAIL (owner) | rewritten row: sum of hops_vine counts 0 over the sites (see SQ-HOP-06); site 1 first try: `forceload` "Too many chunks (289)" for an unaligned 256-block box (batch: boxes now 240 blocks) |
| CQ-HOP-01 | deferred | client |
| SQ-SPD-08b | PASS | p6 A1 (CD 03:18 fix-2): Nether roof y 129 pouch 10 wart, Nether y 61 pouch 10 wart, overworld soul sand control 10; all three pouches `tag: {}` in slot 0; Nether bundle 1 wart on the centre (711 61 508), bundle wart 3 -> 2 |
| SQ-BBQW-02t | PASS | p6 A1: stick on slot 0 (beef) and slot 1 (empty): `useat main CONSUME; main now 1 minecraft:stick` both; basin `0:1 minecraft:beef`; air above; 0 `supplementaries:stick` in the 5x3x5 box |
| SQ-BBQW-02 | PASS | p6 A1 (same boot): 2 beef + stick + cabbage off hand gives `raw_beef_skewer` |
| SQ-SLK-01c | PASS | p6 A1: cow from 18 reaches the salt (s3 minD 1.99, StayNearSaltGoal seen); block `minecraft:air` after 1600 ticks (batch regex wanted a licked stage only). Follow-up SQ-SLK-01d (p6_a2, 5 s samples): salt_block 10 s, `lightly_licked` from about 15 s, then well, heavily, air from about 50 s (4 licks in 80 s) |
| SQ-SY-14 | PASS (row text) | `Fortune stripped: 90 terms on 71 crop tables`; `35 crops (...)` naming sweet_berry, lemon, nether_wart, crimson_fungus_colony; soils `... rich soul soil +33%, fertile soul soil +66%`; no `no base`, no PickSoilYield mixin error. Row says 36: `soil_yield.json` `loot` has 35 keys (propelplant and salmonberry are picks only), so the count is a row-text miscount |
| SQ-SY-15 | PASS | p6_a2 / a3 / a6: wheat 1.00 / 1.32 / 1.66, carrots 3.65 / 4.88 / 6.03, sweet potato (max age 5; `age=7` invalid in the row) 2.72 / 3.50 / 4.30; Fortune III: 1.00 / 1.32 / 1.70, 3.74 / 4.89 / 5.96, 2.72 / 3.51 / 4.35, seeds 2.72 both. Control: same Fortune III stack gives coal 2.28 and melon 6.44 (max 9). First two Fortune passes were probe faults (see notes) |
| SQ-SY-16 | PASS | picks cucumber 1.48 / 2.47, zucchini 1.51 / 2.46, ghost pepper (Soul Soil / Nulch) 1.00 / 1.66, rope vine tomato 2.54 (rotten 0.06); Fortune III on the Fertile ones 2.46 / 2.47 / 1.68 / 2.52; after a bot pick cucumber and ghost pepper age 5, zucchini age 4, vine age 0 |
| SQ-SY-17 | PASS | picks sweet berry 2.52 / 3.35 / 4.24, strawberry 1.51 / 2.03 / 2.47, blueberry 1.50 / 2.00 / 2.47, cranberry 1.49 / 1.98 / 2.52, salmonberry 2.50 / 3.35 / 4.13, lemon 3.41 / 4.61 / 5.88; breaks sweet berry 2.52 / 3.35 / 4.14, strawberry 1.00 / 1.33 / 1.65, blueberry 1.00 / 1.32 / 1.70, cranberry 1.00 / 1.30 / 1.65, lemon (n 3000) 3.53 / 4.65 / 5.78; unripe sweet berry 1.53 / 1.48 / 1.50, salmonberry age 3 1.00 on all |
| SQ-SY-18 | PASS | cucumber ripe 1.00 + seeds 2.73 (max 4), age 3 seeds 3.64 (max 5); ghost pepper ripe 1.00 + seeds 2.72, age 3 seeds 3.73 |
| SQ-SY-19 | PASS | wart 2.97 (Soul Sand) / 5.03 (Nulch); colony on Rich Soul Soil 6.67 crimson fungus no tool, shears 1.00 colony item only; shear pick 1.36 fungus |
| SQ-SY-20 | PASS | berry cane pick pearls 1.50 (netherrack) / 1.93 (Rich Soul Soil) |
| SQ-SY-21 | PASS | `[botany_pots] 3 soils set (Rich x1.1, Fertile x1.2)`; wheat pots (soil and seed put in by `/syt potset`): required 7999 Fertile Soil, 8727 Rich Farmland and Rich Soil, 9600 dirt; growth 120 -> 601 in 20 s |
| CQ-SY-22 | deferred | client |
| SQ-DEP-09a | PASS (row text) | p7a0 full pack: all ten `creativedelight:mods/*` packs enabled; corn maze 10 rolls 24 ids incl. corn_delight corn / cornbread / popcorn, caramel apple, vinery apple_cider (centre 21 ids); jam jar 3 lemon_jelly; clover 1 buzzier_bees clover; ERR 0. Pancakes `servings=2` drop 4 `supplementaries:pancake` + bowl (servings = eaten in Rustic's numbering, table gives 6 - servings): the row's "3" is wrong |
| SQ-DEP-09b supplementaries, upgrade_aquatic, quark, immersiveengineering | SKIP (cascade) | FML: supplementaries <- suppsquared; upgrade_aquatic <- alexsabnormalities; quark <- quarkengineering; immersiveengineering <- quarkengineering, more_immersive_wires, compressedengineering, controlengineering, immersiveposts, immersivepetroleum, kubejs_immersive_engineering, tmted |
| SQ-DEP-09b buzzier_bees, create_factory, hostiledelight, moredelight | PASS | Done; ERR 0 naming the mod; pack not listed; corn maze items (create_factory: two mod_items WARNs only); `placed_omelette` / `placed_suspicious_bacon_and_eggs` Unknown block type, zq 0 |
| SQ-DEP-09b fruitsdelight, corn_delight | PASS | group B new level: Done 80 s; ERR 0; pack not listed; corn_delight: six corn maze mod_items WARNs, 5 rolls 16 ids |
| SQ-DEP-09b supplementaries + suppsquared (extra) | PASS | Done, ERR 0, only `rusticdelight:mod_items/supplementaries/pancake` WARNs |
| SQ-DEP-09b upgrade_aquatic + alexsabnormalities (extra) | FAIL (finding, not a CD guard) | "Failed to load datapacks": `Unknown registry key ... worldgen/feature: upgrade_aquatic:river_tree` from the instance's `kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json` + `placed_feature/trees_river.json`; unreachable in practice (alexsabnormalities requires Upgrade Aquatic) |
| SQ-DEP-09b quark + quarkengineering (extra) | SKIP (cascade) | Botania common_setup needs `vazkii/quark/api/IQuarkButtonAllowed` (third party) |
| SQ-DEP-02 (rerun 5.2) | PASS | p7nb: Done; ERR 0 for brewinandchewin / vinery; `placed_vegetable_omelet` Unknown block type; corn maze 18 ids, apple_cider WARNs only; 8 grape bush lines = Worldgen wild-crops skip WARNs (TODO creativesplit/deps #27). Batch FAIL was CCK's own optional Keg mixin WARN (BATCH-FAULT, regex fixed) |
| SQ-HOP-01 | PASS (note) | p7nb: zq hops, wild_hops items and hops_vine block 0; no hops error; Common's `[item_dict] Missing/invalid item IDs` WARN lists the 5 hops ids among 79 missing without Brewing |
| SQ-MIN-03 (rerun 5.2) | PASS | p7ch (r1113 c3_ch1 set + Worldgen, no Corn Delight): none of the five lines; ERR 0 in the row list |
| SQ-CDW-01 (rerun 5.2) | PASS | fruitsdelight (packs line off [fruitsdelight]) and corn_delight (off none, no worldgen pack) boots on new B levels: no datapack error, ERR 0; myrtrees / endersdelight covered by phase 1 |
| SQ-CDW-03b (rerun 5.2) | PASS | dfd: 17 `needs fruitsdelight` skips, 0 naming a fruitsdelight feature, no apple skip |
| SQ-QN-01 | PASS | p7a0 bot: Quark nerfMending false; Mending pickaxe 500 -> 400 from a 50-point orb, XP stays 0 |
| SQ-QN-02 | PASS | bot in the overworld: Quark canEntityUseElytra true, tryToStartFallFlying started, still fallFlying 2 s later (y 172 -> 162, 1.5 b/t forward); hand-feel check not run (client) |
| SQ-RP-07 | PASS | A1 (`perf/runs/r1115_p9_a1/`): "Reset the diet of 1 players"; Apple Pie food list foodsEaten 3 -> 0, foodList [], solrp_gateFoods []; `solraspberrypie:diet` maxDiversity 3.26 -> 0.0, drinks [], slots stays five empty `{}` entries (also before the reset). The bot's "Your diet was reset" chat line is not observable server side |
| SQ-RP-08 | PASS (BATCH-FAULT in the count) | A1b: boot WARN "Apple Pie still has its own benefits configured"; rp8 (new) and rp7 max health 20.0; the only max_health modifier is Carrot's "Health Gained from Trying New Foods" (0.0), no Apple Pie `_modifier_`. Regex missed "for entity" (fixed) |
| SQ-VH-02 to -05, -07 to -09 | PASS (r1111, not rerun) | `perf/runs/r1111/verdicts/bw.md`; -06, -06b, -06c PASS this run (phase 1) |
| SQ-VH-10 | RECORD (r1111, not rerun) | `perf/runs/r1111/verdicts/vh10.md`: a real TerraBlender 2.0.1.166 wins over the nested stub and boots |
| SQ-VH-11 | RECORD | O2: the 1.2.02 fixture never generated the Nether (no DIM-1 region files); on the mods/ jars `locate biome biomesoplenty:visceral_heap` from 3000 3000 answers 2712 64 2712 (407 blocks); the new chunks there hold flesh 17799, netherrack 5178 in a 17 x 17 box y 5 to 120: the Heap appears in new chunks of a pre-change save. (`execute if biome` is 1.19.3+: BATCH-FAULT line) |
| SQ-FLR-02 | PASS | A1, hopper-fed wheels: 200 flesh -> meat 200, bone 144, eye 58, misc 46, other 0; 200 porous -> meat 303, eye 200, misc 98, bone 0; hopper and controller empty before each count |
| SQ-WO-15 to -25 | PASS | A1 `p9_wo_a2.qb`, qrun PASS 11 of 11 (also PASS in r1110wo, never written up) |
| SQ-WO-31 | PASS | A1b (chance 0, threshold 0): five talks at trust 100, `drops none` |
| SQ-WO-33 | PASS | A1c (all four lists [], chance 100, threshold 0): `drops none`, no exception, no gift-list warning |
| SQ-OB-02 | client (deferred) | the names come from client lang scripts (`kubejs/assets/*/kubejs/names.js`, `client_scripts/namechanges.js`), not visible to a server; r1112 classed it client too. Row text says group B |
| SQ-OB-07 | PASS | A1 (group A: recipe checks): biscuits and gravy only as the pot recipe (+ its Slice and Dice copy): any dough, raw sausage, salt, milk, bowl; no shapeless; glazed carrots take cave carrot or carrot x2 + syrup |
| SQ-OB-08 | PASS (row text stale) | Street Corn: Fire Resistance 600 t and Haste II 600 t (`zq food` and the bot's ActiveEffects after eating). No Nourishment: the Comfort/Nourishment rule (`comfort_nourishment.js`, "every other food loses Nourishment") removes it, so the row's "plus the existing Nourishment" is stale |
| SQ-OB-10 | PASS (BATCH-FAULT in the count) | five `creativedelight:wooden_*` items (WoodenBucketItem), five gives; one tree tapper recipe, B slot `minecraft:bucket/creativedelight:wooden_bucket`; `creativedelight:shaped/wooden_bucket`; 0 Creative Delight error lines in the boot log. Regex missed the padded "Gave 1    [" (fixed) |
| SQ-OB-11 | PASS (row text) | O2: 1.2.02 save with `creativeengineering:wooden_bucket` / `wooden_water_bucket` in a chest and a bot inventory reads back as `creativedelight:wooden_bucket` x2 / `wooden_water_bucket` in both. The log has only the summary `[swaps] minecraft:item: 412 id(s) remapped to their survivors` (no per-id line, which the row and batch expected), nothing unresolved |
| SQ-HCS-10 | PASS (aggregate, 2 known) | SQ-HH-401 to -502 across r1111 (crow D, crow F), r1112 (`hh_crow.qb` reruns), r1113 (-501 explained: scratch carryTicks 200) and this run (-434, -440, -482): all PASS except -474 (FAIL, owner question: salt dust on an unworn salt block) and -476 (FAIL expected, writeup 8.2) |
| SQ-BREED-01 | PASS (probe, not Devtools) | `/dumpbreeding` was never written; scratch probe `zp9 breed` wrote `perf/runs/r1115_p9_a1/breeding_dump.json`: 104 Animal types x 20477 items, 190 Flower/TallFlower blocks. Maned wolf: chicken, rabbit raw and cooked; untitledduckmod duck and goose: the 4 vanilla seeds; Upgrade Aquatic goose: 18 seeds; Quark crab: 23 (fish, wheat, chicken, calamari, squid); toretoise: glow berries only. F-B7: Botania shiny flowers and firefly bush are FlowerBlocks in neither #minecraft:flowers tag; H&H four-leaf clover a FlowerBlock in the block tag, no item; Buzzier Bees clovers tagged; Creeper's Crush a WineBottleBlock; Floral Enchantment bushes BushBlock; dancing sunflower a plain Block. breeding_audit.md 3 and 4 rows need these filled (not edited here) |
| SQ-FDI-04 | PASS | A1 (`p9_a1r.qb`, `p9_a1s.qb`): a bite from lunchbag (cooked beef), lunchbox (baked potato), golden lunchbox (cookie), netherite lunchbox (cooked beef) tastes the food in both books (carrotHas, applepieHas true), Apple Pie never lists the lunchbox; Apple Pie queue +1 per bite (a repeat bread bite: entries 1 -> 2, unique 1). Carrot's raw list also records the lunchbox (hasEaten true), as food_dictionary.md P2 expects: the blacklist hides it from the book and progress. First pass used vanilla pumpkin pie, which is not a food here (BATCH-FAULT); note: a lunchbox slot accepted it |
| SQ-CDNS-05 | RECORD | RB (`perf/runs/r1115_p9_rb/`): the O2 save (written by the mods/ jars) on the 1.2.02 jars: Forge "Unidentified mapping" for block, entity_type, fluid, item, sound_event, worldgen/biome, then loads; the wooden bucket chest loses both buckets (`SQ-CDNS-05.cmp`: 2 -> 0, 1 -> 0) and the shields + netherite lunchbox chest comes back empty. A rollback past the namespace moves drops every moved id in loaded containers (as r1110's record) |
| SQ-CDNS-10 | PASS (r1110, not rerun) | scratch_run_2026-11-10.md 8: -10a to -10e PASS |
| SQ-CDNS-12, SQ-NSM-04 | skipped | client rows |
| SQ-NSM-05 | PASS (server half) | O2: the fixture's netherite lunchbox reads back with bread 4, cooked beef 4, apple 4; a bite eats one cooked beef; `creativedelight:lunchbox/lunchbox_dye` + red dye -> display.color 11546150; `creativedelight:smithing/lunchbox_skin` with a golden lunchbox and a Fantasy Furniture berry basket -> `CELunchboxSkin` set. Model and render stay CQ |
| SQ-RDB-03 | PASS (aggregate) | RD-08, -11, -22, -24, RDX-01, -03 to -12, -14 PASS after the bake in r1113 c4_a / c4_ar (`perf/runs/r1113/verdicts/c4_a.md`); RD-10 INCONCLUSIVE in r1114 (owner TODO `creativedelight/members #45`); RDX-02 client; RDX-13 and RD-26 rows below |
| SQ-RDX-13 | PASS (plate half) | O2: the 1.2.02 Pancakes plate serves `supplementaries:pancake`. The item half cannot be built: `rusticdelight:pancake` and `fried_dough` do not exist in 1.2.02 (Unknown item); it needs a pre-11-02 save |
| SQ-RD-26 | PARTIAL (same as r1102b) | B1 seed 8675309: savanna -1828 2308 (9 x 9 chunks): wild cotton 7, no coffee, peppers or giant peppers; jungle 1892 -2372 (6 x 6 chunks): wild bell peppers 0, wild coffee 0; jungle -1380 -2724 (10 x 10 chunks): wild coffee 5, wild bell peppers 0, cocoa 48. Wild bell peppers 0 in every jungle box (r1102b also 0 at its jungle); the tag holds the jungle biomes. Third jungle box: BATCH-FAULT (its waitlogs matched the second box's lines) |
| SQ-RDB-05 | PASS | O1 -> O2: pancakes plate servings=4, honey pancakes 7, cotton age 3, coffee age 4, bell peppers age 6, red pepper crate, cotton boll crate, potted wild coffee, cherry blossom cheesecake bites 1, wild coffee, glowing calamari crate: every state identical on the mods/ jars; 0 missing-registry lines naming rusticdelight |

### Phase 10 (client, runner-10)

Shots under `perf/runs/r1115_p10_{fdr,pg,items,nsm,pwg,srv}/shots/`; launch logs and hs_err files in `$S/p10cli/old_launch1..6/`.

| ID | Result | Evidence |
|---|---|---|
| CQ-FDR-01 | PASS (again) | 3 launches at 421 jars: `[jar handles] closed 419 stale mod jar filesystems (419 zip files): 1091 -> 672`; title 69 s; sockets/pipes max fd 657 |
| CQ-FDR-02 | PASS (jar handle criteria) | 0 ClosedFileSystemException / ClosedChannelException over 3 sessions (sounds, chat, JEI, world reloads, F3+T at 07:49:48); textures and lang ("Spool of String") present after F3+T (`r1115_p10_fdr/shots/sheet_f3t.png`). The dimension change died natively in IP portal rendering, reproduced with FdReleaseMixin off at 419 jars: not this fix (SQ-PG-01c) |
| CQ-FDR-03 | PASS | FdReleaseMixin removed from a copied Common jar in the scratch dir, 421 jars: `*** stack smashing detected ***` 24 s after launch, no `[jar handles]` line |
| CQ-FDR-04 | PASS | dedicated scratch server (PG -18 present): no `[jar handles]` line in the server log; client joined 08:13:51 and played |
| SQ-PG-01b | PASS (log criterion), finding | wall-to-wall both ways: no `Adjusting Client Player Position`; wall-to-floor: one line, player on the floor; floor-to-wall: no line. Same after quit-and-reload (fresh portals: gun portals despawn on reload). FINDING: every floor-to-wall pass (2 of 2) leaves the player at y -61.0, one block inside the floor in front of the wall portal, unable to walk or jump out (`r1115_p10_pg/shots/fw_pass1*.png`, `r2_fw.png`). Likely cause: `IPExitLiftMixin` now skips Immersive Portals' post-teleport adjust on wall exits, and a falling entry through a floor portal maps the feet below the wall portal's bottom; owner choice (allow the adjust when the exit position collides, or accept) |
| SQ-PG-01c | FAIL (client crash) | a vanilla flint and steel gives soul fire and no portal (Creative Magic rule, by design); a Flint and Terrasteel portal opens and the client dies natively within 3 s of the Nether client world loading: SIGSEGV libc <- `GL11C.nglDrawElements` <- `BufferUploader` <- IP 2.3.7 `ViewAreaRenderer.buildPortalViewAreaTrianglesBuffer` <- `RendererUsingStencil` (3 of 3, also with the jar handle fix off). First see-through IP portal on a client (gun portals render opaque). Owner / third party: a render-mod conflict with IP's stencil renderer (ImmediatelyFast, Embeddium/Rubidium or Distant Horizons are the candidates); `$S/p10cli/old_launch1/hs_err_pid4120314.log` (+ .jfr), `old_launch2/`, `old_launch4/` |
| CQ-PG-03 | LOOK: owner | wall exits not lifted, floor exits land on the floor; but floor-to-wall leaves the player stuck in the ground (SQ-PG-01b finding); Nether frame half blocked by SQ-PG-01c |
| CQ-PWG-66 | INCONCLUSIVE | not wired with Power Grid (hand wiring); stand-in with a Create creative motor: `create:display_board` (BE `create:flap_display`) at the end of a 16 RPM shaft read `Speed 0.0f` with facing south, north and east (`r1115_p10_pwg/shots/`). Points at the row's own hypothesis (the board takes no rotation from that shaft end); needs the hookup rewritten by hand (owner) |
| CQ-SPD-01 | PASS | `r1115_p10_items/shots/CQ-SPD-01_pouch.png`, `_bundle.png`: all three pouch lines and the bundle line |
| CQ-SLK-01 | PASS | three heights 12/8/4 px placed, waxed twins identical, rock salt texture, no tint or missing texture; items in hand match (`sheet_hop_slk.png`, `sheet_slk_hand.png`) |
| CQ-HOP-01 | PASS | vine ages 0 to 3 and lattice ages 1 to 4 drawn with the jungle grape art tinted green, no missing texture (log: `hops states drawn with the white jungle grape models: 131`); JEI uses of hops: Grape Planting 1/2 logs, 2/2 lattice, no Grape Stomping; lattice tooltip "Plant jungle grape seeds or hops on it". Note: the four lattice ages look alike (no cones at 3 and 4) (`sheet_hop2.png`) |
| CQ-SY-22 | PASS | Rich Soil and Rich Soil Farmland "+10%", Fertile Soil (`immersive_weathering:mulch_block`) "+20%", each "Does not change yield" |
| SQ-OB-02 | PASS | Salt (`vintagedelight:salt_dust`), Corn (`corn_delight:corn`), Spool of String (`hearthandharvest:spool`), Raw / Cooked Veggie Patty (`SQ-OB-02_sheet.png`). Other spools stay "Spool" (`creativeengineering:spool`, `architects_palette:spool`) |
| SQ-CDNS-12 | PASS | new client joins, items present; client with the 1.2.02 Delight jar refused: "Failed to synchronize registry data from server" plus the mod version list, no crash. Note: the list shows Creative Delight 1.1.0 / 1.1.0 and Hearth and Harvest 1.4.1-ce-19.2-04 on both sides (versions not bumped since 1.2.02) |
| SQ-NSM-04 | NOT RUN | the NSM fixture has no placed paintings or banner (`nsm_fixture.js` has no painting step); needs a fixture with old-namespace paintings |
| SQ-NSM-05 | PASS (render half) | 1.2.02 save: the netherite lunchbox renders in hand; dyed (2x2 grid, Color #B02E26) and skinned (smithing, golden lunchbox) results render in the slot and in hand (`r1115_p10_nsm/shots/`) |
| SQ-QN-02 | PASS; feel LOOK: owner | Overworld, elytra worn, from y 200: z 1030 -> 1142 in about 3.5 s while y fell 148 -> 137: gliding (`sheet_qn.png`) |
| (extra) | FINDING | a client with Power Grid joining a server without it crashes (`Registry entry not present: powergrid:electrozapper`, PG `ElectroZapperRenderHandler.appliesTo` via Create's `ShootableGadgetRenderHandler`) instead of being refused; only reachable when the server lacks PG; owner note |

### Phase 11

Results `perf/runs/r1115/pth_a1/` (boot 1), `pth_b1/` (boot 2), `pth_b3/` (boot 3), `pth_cli/` (client: driver logs,
`shots/`, `cq04_sheet.png`, `cq04b_sheet.png`). Server jars: creativecompat 11:31 and creativedevtools 00:40 build jars,
everything else mods/. Mob rows run on base-speed-0 zombies (an `ActiveEffects` slowness NBT was ignored); "moving"
checks push mobs with `zpth motion`; bots are invulnerable, so damage checks use 100 hp zombies. Client speeds are
read from position steps (the server's motion for a real player is not the client's): normal walk 0.216 b/t, farmland
soil 0.8.

| ID | Result | Evidence |
|---|---|---|
| SQ-PTH-01 | PASS | no mixin error; `[surface.passthrough]` has enabled, arrowLifeTicks, arrowLongLifeCap and 11 tier tables |
| SQ-PTH-02 | PASS | (-02, -02r) lone web -0.500, cube centre -0.650, IE razor wire -0.500 (and hurts); a pushed zombie crosses a lone web on the same path as the open-grass control (no stuck stop); barbed wire hurts only while moving (20 -> 14.2) |
| SQ-PTH-03 | FAIL | (-03, -03s, 100 hp zombies, 20-block drops) plain 16 dmg; into a lone web at ground level 0 (expected about 0.85x), 3 stacked webs 0 (expected about 0.61x), cube 0 and hangs (ok). Per-tick: fall distance 14.55 at y -54.55 -> 1.1 on entering a lone web 5 blocks up (then 3 dmg from the 5 blocks below): the web RESETS fall distance instead of scaling it. Suspect `PassThrough.newCellsFallFactor` / `resetFall` (reset should be dense-only, `denseResetsFall`) or vanilla `Entity.makeStuckInBlock` fallDistance reset not cancelled by `MakeStuckInBlockMixin` |
| SQ-PTH-04 | PASS | reinforced cobweb placed by bot P (block data owner P): P walks through with mod none, bot Q -0.500 in the cell |
| SQ-PTH-05 | PASS | no phase 2 mixin error |
| SQ-PTH-06 | PASS | 16 slow items and the -2 b/t diamond held in the web cell at different heights (y -56.0 to -56.99), NoGravity on; the three 0.1 b/t items drifted past the 1-block cell and fell (path never crossed it) |
| SQ-PTH-07 | PASS | (-06r/-07r) after a restart the five items sit at the same positions, ng=true, `CreativeCompatPass: {StuckX..., PriorNoGravity: 0b}`; web removed: all at y -60, ng=false. First try: the -06 items despawned at their vanilla 5 min before the restart |
| SQ-PTH-08 | PASS | (-08r) orbs held in the lone web (one merged orb, same position over 8 s) with the bot 4 blocks away; walked in: collected, 25 XP. First try: the bot stood 4 blocks off while the orbs were still falling, they homed before reaching the web (row design) |
| SQ-PTH-09 | PASS | bot falls in and stops (mod -0.65, y -58.07, top layer); sneak sinks 0.0525 b/t; jump from the bottom (-09r) climbs 0.07 b/t to the top; zombie hangs |
| SQ-PTH-10 | PASS | tipped arrow (bot bow) hangs at the centre, bot walks in and picks up `tipped_arrow{Potion:slowness}`; no-pickup arrow hangs (not picked up, despawns at 1 min); (-10b) explosive arrow hangs at the centre, no blast, webs intact; centre broken: it drops into the cell below |
| SQ-PTH-11 | FAIL | break group can stall in an outer cell: bot snowball and pearl thrown from z 995.5 stop in the edge cell (z 1000.69, speed x0.35 a tick: 1.44 -> 0.50 -> 0.17 ...) and hang, sinking 0.002 b/t, never breaking (pearl: no teleport); thrown from z 994.5 and 996.2 they break at the centre and the pearl puts the bot in the cube (-0.65). Splash potion settles and sinks 0.004 b/t (edge column); firework holds and explodes at its flight time; fireball explodes on entering the middle layer. Suspect `PassProjectiles.breakIn` (breaks only on reaching a surrounded cell; slowing in a dense outer cell can stop short of it) |
| SQ-PTH-12 | PASS | bobber stops at the web centre and reels in; pickarang held past 10 s, picked up whole (`Damage: 0`); (-12c) web broken: item `quark:pickarang` Damage 0; thorn chakram held and picked up |
| SQ-PTH-13 | PASS | (watch lines) bot arrow lived 3609 ticks after landing, plain trident 3602, no-pickup arrow 1202; Loyalty trident returned to the bot |
| SQ-PTH-14 | PASS | (-14e) 4th arrow 72 s after three: the oldest gone the same tick; (-14c/-14d) 4th after 10 s: the oldest (life 1165 at reboot) vanished at 1200 after the restart, the other three stayed. First try (-14/-14b) BATCH-FAULT: the 4th came after the first three had reached 3 min |
| SQ-PTH-15 | PASS | (-15c) scratch `kubejs/data` exempt tag + /reload: snowball in `passthrough_exempt/all` and flies through the cube at 1.41 b/t; cart under a web slowed (0.1 to 0.3 b/t) and rolls on to x 1017. -15 / -15b BATCH-FAULT (datapack not enabled; lane outside the forceload after the restart) |
| SQ-PTH-16 | PASS | boot 2: `[leaf_piles] slowFromLayers (6) moved to [surface.passthrough.leaf_pile] minLayers (6)`, no `[leaf_piles]` left, minLayers = 6, no LeafPileSlowMixin; boot 3 no move line (-16b). Boot 1 logged the live value 4 as `minLayers (kept)` (-16a regex BATCH-FAULT) |
| SQ-PTH-17 | PASS | 3 layers none (oak, bamboo leaves), 4 layers -0.550 (both), Depth Strider none |
| SQ-PTH-18 | PASS | `leaf_piles_slow = true`; moving zombies: 6 layers 0.5 per hit (20 -> 10.7), 7 layers 1.0 (-> 1.2), modifier -0.550 in both. Leggings half not testable on a server: IW checks leggings only for `Player` (`LeafPileBlock.entityInside`), bots are invulnerable |
| SQ-PTH-19 | PASS | overlap checks 0 (399 / 114 ids); sizes bush 10 (8 + salmonberry, cranberry), cobweb 6 (+ razor wire), grass 194 (119 + Botania), others as the intro; exempt 34 (Power Grid absent on the scratch server); projectile_scan no ungrouped / two-group ids |
| SQ-PTH-20 | PASS | every tier sample in its tag only (incl. the new bush, cobweb, grass members; -20x sugar cane, quark rope with support); left-out five in none; rope tomatoes not rope; growth_any tomatoes yes, budding no |
| SQ-PTH-21 | PASS | summon tests for snowball, potion, arrows, fire bomb, spider, fox as stated; pickarang, firework, revolver shot and mana burst discard themselves on the summon tick: their groups from the in-game /dumptags export (snag, linger, exempt + exempt/all) |
| SQ-PTH-22 | PASS | rope tomatoes centre -0.500, budding age 1 none, age 2 -0.250, coffee stem age 0 -0.250, taiga grape bush -0.640 |
| SQ-PTH-23 | PASS | spider none in reinforced cobweb and barbed wire (pushed spider hurt 16 -> 8), zombie -0.500, fox none, poppy bush no modifier and a pushed zombie moves 0.225 per push vs 0.5 (vanilla stuck kept) |
| SQ-PTH-24 | PASS | IE revolver shot (-24s, motion in the summon) and Blocky Siege cannon ball cross the cube at 1.41 to 1.44 b/t; snowball breaks at x 1011.7 |
| SQ-PTH-25 | FAIL | 10 cases, tagged=true, enabled=true, json `cases` with median_ns / p99_ns, no fixture left, no watchdog (one 9 s "Can't keep up"); tick_air lowest (130 ns); tick_field 749 / 796 ns (green); **tick_web_cube 2029 / 2135 ns (red, budget 1000)**; tick_horse_field 2537 / 2853 ns. Record in blockrunner.md 3.10, caching per plan 7.2 is the owner's call |
| SQ-PTH-26 | PASS | file false then true; while false z26a mod none, pushed zombie stops in the web (vanilla stuck), bench enabled=false with tick cases 13 ns; true: -0.500 again |
| SQ-PTH-27 | PASS | timing lines every 10 s, parser server block (0.48 % of tick time, 9.9 inside per tick, peak 29), no side=client line. `timing = false` saved live at 12:09:29 did not stop them (lines to 12:10:19): the live-toml gotcha, not the code: boots 2, 3 and 4 started with `timing = false` in the srv toml and logged 0 `[passthrough-timing]` lines in 7, 5 and 15 minutes up. The live switch-off itself stays untestable on scratch servers (ModernFix NightConfigWatchThrottler) |
| SQ-PTH-28 | FAIL | perf suite `pt2` (Compat 11:31 copy, `perf/suite/pt2/summary.md`, copy `perf/runs/r1115/pth_cli/pt2_report.txt`), all 14 windows ok, no watchdog. Mean mspt on minus off: PT01 200 cows in corn **+0.57 ms** (8.71 / 8.14, budget 0.5), PT02 50 villagers +0.32, PT03 100 zombies in webs +0.36, PT04 500 items in webs **+3.17 ms** (12.41 / 9.24), PT05 arrows -5.22 (on is cheaper), PT06 bot walk -0.04. PT04 census counts 500 items both ways but does not tell held from grounded (census has no such field). Record in blockrunner.md 3.10 and perf_suite.md 12 (owner) |
| SQ-HOP-05 | PASS | rerun on Compat 11:31: sterilized after one shears use, no growth at randomTickSpeed 3000, second use clears it |
| CQ-PTH-01 | PASS (partial) | client speeds: wheat 7 edge 0.130 (0.60 = 0.75 x farmland 0.8), inside 0.087 (0.40 = 0.5 x 0.8), wheat 3 0.173 (farmland only); corn lower 3 none, lower 4 0.130; rice paddy wading 0.049 b/t (about half of plain shallow water); creative walk slowed the same; rope climb 0.118 b/t up (vanilla), slide down 0.150; vine walk 0.150 (0.69x), standing none; FOV: pillar width and top identical in and out of the wheat field and on and off asphalt. Not judged: creative flight (neither a double-tapped space nor `abilities.flying` from the console kept the player flying), swimming beside the rice (player barely moved) |
| CQ-PTH-02 | FAIL | denseWalk 0.2 saved in the server toml while walking: speed stayed 0.087 and modifier -0.500 (not applied; same watcher cause as SQ-PTH-27); no "moved wrongly" lines |
| CQ-PTH-03 | PASS (partial) | ground walk through the bottom block of FD and Quark rope 0.150 (0.69x, row 0.75x); climbing vanilla; 20-block drop down a FD rope column at 0.15 b/t, hp 20 at the bottom. Sneak on the rope: the player slid down at 0.15 b/t (xdotool Shift_L hold may not register): owner look |
| CQ-PTH-05 | PASS | single player, timing on: parser prints server (3.67 inside per tick, 0.98 % of tick) and client blocks (inside 0.34 per tick while walking the field, 0.39 %) |
| CQ-PTH-04 | FAIL (partial) | wheat 7 middle `Walk Through: 0.40x (Dense neighbors) on Farmland (Til...`, `Neighbors: 8`; wheat 2 no line; lone web `Walk Through: 0.50x`, `Fall Through: 0.85x`; cube face cell `0.35x` / `0.20x (Dense neighbors)` (the centre cannot be targeted through the face); 3-layer pile no line, 5-layer pile no line because this server's minLayers was 6 after SQ-PTH-16; `[surface.wthit] passThrough = false` saved in the client toml did not remove the line after 8 s (live toml edit not applied). Vine and powder snow not captured (aim): owner look |

### Phase 12

Stopped by the owner at 15:01, before the first boot: no row ran.

| ID | Result | Evidence |
|---|---|---|
| SQ-PTH-03b | deferred | not run (phase 12 stopped before boot) |
| SQ-PTH-06b | deferred | not run |
| SQ-PTH-11c | deferred | not run |
| SQ-PTH-25b | deferred | not run |
| SQ-PTH-28b | deferred | not run |
| CQ-PTH-03b | deferred | not run |

## 9. Findings routing

| ID | Finding | Route (fix agent / owner TODO) | State |
|---|---|---|---|
| SQ-VH-06c | Heap features (flesh, 2212 blocks) spill up to 12 blocks into a neighbouring warped forest; surface rule fine | owner TODO (design: is border spill acceptable) | to write back |
| SQ-HH-328 | row census box M-2..M+36 covers one quadrant (locate point is the maze centre) | row text fix at write-back (batch side, no behaviour) | to write back |
| SQ-PWG-82 | arm takes the whole wire_connector x8 / resistor x8 stacks from the depots, circuit stalls at Assembly 1/3 (twice); r1114 passed only because parts were added after the lock | owner TODO (port choice: one-part pickup / held-stack fallback in `ArmInteractionPointMixin`/`ArmBlockEntityMixin`, or row back to "parts after the lock"); TODO `#83` | to write back |
| SQ-PWG-80b | water-first: CD water_salt takes the water, then Create: The Salt salts the rotten flesh; no acid and the flesh is used up | owner TODO: add to `#82` | to write back |
| SQ-PWG-54r | probe prints `controller=none` before and after restart; re-attach itself passes | row text fix at write-back | to write back |
| SQ-PWG-06 | heater runs at 6.5 V (pwg12 batch fix), row says 6 V | row text fix at write-back | to write back |
| SQ-MELT-08 | no per-click salt melt exists: a salt_dust click places a `vintagedelight:salt` layer (a salt source; the ice steps later at block rate); `tryMeltFromItem` is reached only from `SaltItemDispenseBehavior`, registered for `salt:salt` but returning at once unless `#forge:salts` (only salt_dust), so a salt:salt dispenser is a no-op and a salt_dust dispenser just drops dust (members/salt/.../Melting.java 147-163); salt.md / plan 3.8 say the click goes through meltStep | owner TODO (wire a salt_dust click / dispenser = new behaviour, or fix the writeup to the layer route) | to write back |
| SQ-MELT-10 | boot warns `architects_palette:glowstone_cage_lantern` (light 3 lit, 0 unlit) is in the fire tag but does not random tick | owner TODO (make it source-driven or drop it from the fire tag) | to write back |
| SQ-MELT-09 | Common's Rare Ice pseudo mixin logs `Error loading class: me/shedaniel/rareice/forge/blocks/RareIceBlock` when Rare Ice is absent | fix agent fix-1 (behaviour-neutral: mixin plugin skip) | built 02:27 (creativecommon-1.2.0, CommonMixinPlugin NEEDS_MOD rare_ice; Melting Values cache); installed 03:15 (installToMods rebuilt it: same three source files plus another session's unreleased.md); rerun SQ-MELT-09b, -P1b in phase 5 |
| SQ-MELT-P1 | `Melting.onRandomTick` reads ForgeConfigSpec values every tick (267 JFR samples; Melting 1.0 % of server samples) | fix agent fix-1 (behaviour-neutral: cache config values, refresh on config load) | built 02:27 (creativecommon-1.2.0, CommonMixinPlugin NEEDS_MOD rare_ice; Melting Values cache); installed 03:15 (installToMods rebuilt it: same three source files plus another session's unreleased.md); rerun SQ-MELT-09b, -P1b in phase 5 |
| SQ-SPD-08 | nether wart seed pouch in a Nether dispenser plants 0: `SeedContainerDispense.plantOne` uses `FakePlayerFactory.getMinecraft(level)`, cached by profile, so `UseOnContext` acts in the overworld | fix agent fix-2 (behaviour-neutral: explicit-level context) | built 03:18 (creativedelight-1.1.0, fix-2; changelog bullets added); install at the phase 5/6 boundary; reruns SQ-SPD-08b, SQ-BBQW-02t, SQ-SLK-01c in phase 6 |
| SQ-BBQW-02s | stick on a barbeque basin with no recipe is placed as a `supplementaries:stick` block: `StorageBlock.use` tool branch returns PASS | fix agent fix-2 (behaviour-neutral: CONSUME, as its comment and writeup say) | built 03:18 (creativedelight-1.1.0, fix-2; changelog bullets added); install at the phase 5/6 boundary; reruns SQ-SPD-08b, SQ-BBQW-02t, SQ-SLK-01c in phase 6 |
| SQ-SLK-01 | cow reaches the salt lick (2.25 blocks) but never licks in 1600 ticks; `StayNearSaltGoal.closeEnough` dist sq <= 4 on block positions | fix-2 checks the writeup: fix if it says the cow licks after the trip, else owner TODO | built 03:18 (creativedelight-1.1.0, fix-2; changelog bullets added); install at the phase 5/6 boundary; reruns SQ-SPD-08b, SQ-BBQW-02t, SQ-SLK-01c in phase 6 |
| SQ-SPD-01, -02 | row says 25 planted; the 5x5 is centred on the block in front, so a sideways dispenser stands in it: 24 | row text fix at write-back | to write back |
| SQ-HH-482 | cow at 24 strolls to 22 and is pulled in (radius 22) | row text note at write-back | to write back |
| SQ-HOP-05 | Compat `VineShearing` gives `hearthandharvest:hops_vine` a `fertile` state: first shears use sets fertile=false and cancels, so `sterilized` needs a second use; fertile=false does not stop growth (HopsVineBlock overrides randomTick) | behaviour-neutral (add `HopsVineBlock` to VineShearing's own-handling list, as both writeups say); Compat is owned by the pass-through session: sent there 04:40 | waiting on pass-through session |
| SQ-HOP-04 | age-1 hops vine `use` returns PASS, so a stick (any placeable) replaces the vine (vines are replaceable) | owner TODO (block placement on an age-1 vine, or accept vanilla vine behaviour and rewrite the row) | to write back |
| SQ-HOP-06, SQ-HH-304 | 0 wild hops over about 740 forest chunks (seed 8675309); `/place feature` works 1 in 25: `TrunkVineFeature` trunk search (24 columns, radius 6) x rarity 96 is far rarer than the planned 1 in 96 chunks | owner TODO (rarity or trunk search; `TrunkVineFeature.java`, `wild_hops.json`) | to write back |
| (SQ-HOP-06 side) | wild carrots and adzuki also 0 in the same forest boxes though forest is in both crop tags | owner TODO: worth a look (wild crop placement) | to write back |
| SQ-MELT-P1b | live toml edit clause not judgeable on scratch (config watcher parked); restart picks values up | note at write-back | to write back |
| SQ-SPD-08, SQ-BBQW-02s, SQ-SLK-01 | fix-2 reruns SQ-SPD-08b, SQ-BBQW-02t, SQ-SLK-01c PASS (05:02) | fixed | done |
| SQ-SY-14, -15, SQ-SLK-01c | row text: `35 crops` (not 36); sweet potato max `age=5`; SLK-01c pass also accepts air (licked away) | row text fix at write-back | to write back |
| SQ-DEP-09b extra | dropping upgrade_aquatic (+ alexsabnormalities): "Failed to load datapacks: Unknown registry key upgrade_aquatic:river_tree" from `kubejs/data/upgrade_aquatic/worldgen/configured_feature/river_tree.json` and `placed_feature/trees_river.json`; unreachable for players today (alexsabnormalities requires UA) | owner note (guard if UA ever becomes optional) | to write back |
| SQ-DEP-02 | 8 grape bush lines are Worldgen wild-crops skip warnings | TODO creativesplit/deps #27 (existing) | to write back |
| SQ-DEP-09a, -09b, -02, SQ-HOP-01, SQ-QN | row text: pancakes servings=2 give 4 + bowl; name the four cascades (Quark also cascades through Botania); exclude Create Central Kitchen KegBlockEntityMixin warning; item_dict missing-ids warning expected without Brewing; QN runs on a bot via zqn | row text fix at write-back | to write back |
| SQ-RD-26 | wild bell peppers 0 in every jungle box (savanna cotton 7, jungle coffee 5), same as r1102b | owner TODO (rarity / placement check) | to write back |
| SQ-FDI-04 side | a lunchbox slot accepted vanilla pumpkin pie, which is not a food in this pack | owner TODO (lunchbox filter: edible-only or keep) | to write back |
| SQ-RDB-03 side | Rustic pancake plate `servings` 4 -> 5 after taking one | none: servings counts eaten slices (Rustic numbering, as SQ-DEP-09a) | closed |
| SQ-OB-08, -11, -02, SQ-NSM, SQ-RDX-13, SQ-BREED-01 | row text: Street Corn has no Nourishment (comfort_nourishment rule); expect the swap summary line; OB-02 is a client row; RDX-13 needs a pre-11-02 save; BREED-01 dump via zp9 until /dumpbreeding exists, results to breeding_audit.md 3 and 4 | row text fix and writeup fill at write-back | to write back |
| SQ-PG-01c | lighting a Flint and Terrasteel Nether portal kills the client within 3 s (3 of 3, also with the jar handle fix off): SIGSEGV in libc from `nglDrawElements` via Immersive Portals `ViewAreaRenderer` / `RendererUsingStencil`; first see-through IP portal on a client; hs_err in the phase 10 scratch dir `p10cli/old_launch1,2,4/` | owner TODO, URGENT (render-mod conflict, investigation; not a known behaviour-neutral fix) | to write back |
| SQ-PG-01b | floor portal -> wall portal leaves the player at y -61.0 inside the ground, cannot move or jump (2 of 2); likely `IPExitLiftMixin` skipping IP's adjust on wall exits | owner TODO (exit-lift rule for wall exits) | to write back |
| CQ-PWG-66 | creative motor stand-in: display board at a 16 RPM shaft end reads Speed 0.0 on three facings; the row's hookup needs rewriting | owner (row rewrite) | to write back |
| (phase 10 extra) | a client with Power Grid joining a server without it crashes ("Registry entry not present: powergrid:electrozapper") instead of being refused | owner TODO (Power Grid port: network/registry refusal) | to write back |
| SQ-NSM-04 | not run: `nsm_fixture.js` has no painting or banner step | tooling TODO (extend the fixture), rerun | to write back |
| SQ-CDNS-12, CQ-HOP-01 | refusal screen shows equal versions (no version bump); the four hops lattice ages look alike | owner notes | to write back |

## 10. Follow-up

Owner decisions on the section 9 findings and the plan: [plan_post_scratch_1115.md](../plan_post_scratch_1115.md); TODO family `tooling/scratch-1115-follow-ups`; queue rows SQ-PS15 / CQ-PS15. Wall-exit lift (SQ-PG-01b) built; crash SQ-PG-01c investigated, bisect queued.
