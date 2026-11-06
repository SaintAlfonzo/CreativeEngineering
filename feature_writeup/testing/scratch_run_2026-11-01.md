# Scratch run 2026-11-01 (r1101): priority run, bug batch, deferred worldgen, client block

Owner request (2026-11-01): execute the scratch queue; worldgen next to last, client tests last, after everything else.
Scope taken: the queue's "Priority run" (boots 1 to 3 and its client session), the group A / B / C rows of "Bug batch,
2026-11-01", the r1031 worldgen rows deferred on 10-31 (SQ-SB-04 to -08, SQ-SB-01e, W3a / W3b), then the client block
(priority CQ rows + `r1031/c1_client.qb`). Not taken: L2 perf suite, L3 resource pack bench (owner to ask), the older
pre-10-28 backlog.

Perf session `r1101` (JFR on, no prune). Batches `tools/scratch_query/batches/r1101/`. Results and row logs
`perf/runs/r1101/` (`results.md`, `rows/`, `judged_p1.md`, log copies in `p1_logs/`).

Jars: prep_srv.sh hard-linked the build jars of 2026-11-01 01:56 CDT (patcherMod 1.1.04, creative_delight 1.0.0;
newer than `mods/`, so the bug batch's "needs the 01:35 / 01:23 jars" held), creative_brewing 10-31 16:55 (nests Vinery
-08, Brewin' -05), backportedVintage -09, backportedVeggies -03, repackedStorage. Note: the clock fell back (DST) during
the run; log times are CST, an hour behind the debug.log CDT times.

## 1. Priority boot 1 and reruns (group A superflat `r11p1`, 01:00 to 01:30 CST)

First pass p1_server.qb: PASS 9, LOOK 23, FAIL 11; every FAIL was a batch fault (judged offline, `judged_p1.md`).
Reruns on restarts of the same world (p1b, p1c to p1e, p3_reruns, p3b_check). Final:

| ID | Result | Evidence |
|---|---|---|
| SQ-CBJ-01 | PASS | debug.log finds backportedVinery -08 and backportedBrewin -05 inside the bundle (standalone jars removed); `[creative_brewing] members: [vinery, brewinandchewin]` |
| SQ-CBJ-02 | PASS | startup order creative_delight before creative_brewing; core finished and server scripts ready lines |
| SQ-CBJ-03 | PASS | `vinery = true`, `brewinandchewin = true`, one section; vinery and brewinandchewin items resolve. Queue text wrong: `foundation` registers no items (only `foundation:chair` and particles) |
| SQ-CBJ-05 | OPEN | the `members on:` line is logged only in `ServerEvents.loaded` (`cb_server.js`), never on `/reload`: needs a boot with `brewinandchewin = false` or a reload-time log (owner) |
| SQ-KT-22 | PASS | `blockheads_additions` loaded, no mixin / capability / rack errors, `[kitchen]` in its toml |
| SQ-KT-23 | PASS | 20 spice + 20 tool rack recipes; slab tag has blossom slab, no oak slab |
| SQ-KT-24 | PASS (rerun KT-24b) | rack BE holds sugar (set with `data merge`; vanilla `/item replace` refuses non-Container BEs) and drops it on break |
| SQ-KT-25 | PASS | kttest 34 of 34 (better than the expected 30), kctest 9 of 9 |
| SQ-CDF-07 | PASS (with-Brewin' half) / BLOCKED (without) | quiche sequenced assembly id present, lasagna meats tag. Without Creative Brewing the pack does not start: removing the bundle removes Vinery too, and `startup_scripts/_lib/effect_dict.js` hits `Missing effect 'vinery:climbing_effect'` in `ItemEvents.modification` (KubeJS startup error, mod loading fails; log `p1_logs/startup_nocb.log`). The requires-skip half cannot be tested until Vinery can be absent |
| SQ-PRI-01 | PASS | lasagna meats, kimchi, bell peppers in `brewinandchewin:foods/pizza_topping`; pizza (2 recipes), ham and cheese sandwich, scarlet pierogi load. No "supreme pizza" item exists in the pack (queue wording); emptied scripts are comment-only stubs |
| SQ-PRI-02 | PASS | no floral large pot recipe; `floralench:large_poppy_pot` (ids are `large_<flower>_pot`) places |
| SQ-PRI-03 | PASS | no jrftl jar or toml; the only log line naming jrftl is the probe's own |
| SQ-JRFTL-01 | PASS | judged_p1.md |
| SQ-WCT-01, -02, -04 | PASS | six ids; cauldron WaterLevels 8 / 20 / 32 / 10 / 21; dragon 7x7, copper centre only |
| SQ-WCT-03 | PASS (can) | roof-half use gives WaterLevel 12; the well tank stayed at 86000 mB (no 1000 mB drop seen: note) |
| SQ-WCT-05 | PASS | `creative_delight:smithing/diamond_watering_can`; over-full can clamps 16 -> 7 after a use |
| SQ-WC-03, -05 | PASS | moved can: off-hand bone meal loads; 3x3 farmland and mulch soaked (mulch moisture 7) |
| SQ-HR-01 to -03 | PASS | judged_p1.md |
| SQ-HR-04 | PASS | item id is `hearthandharvest:raspberry`: fox_food, forge:berries, forge:fruits/berries; fox emptied the bush to age 1 |
| SQ-SB31-01, SQ-SVA-05, SQ-TSK-02, -03 | PASS | judged_p1.md |
| SQ-NS-01 | PASS | `CENightstandItems` (set by `data merge`) kept across a restart on the Every Compat nightstand; hopper below pulls it; drops on break |
| SQ-DM-03 | PASS | fertile soil row (mulch) 6 then 7 of 10 mature against rich soil 2 then 4 |
| SQ-POT-08 | PASS | planter_box_banned = azalea, flowering azalea; flower_box_plants without azalea; one hanging_pot recipe with rich soil |
| SQ-NCH-01 | PASS | no empty-stack error; sawdust CookTimeTotal 200, ash after 12 s |
| SQ-GT-31 | PASS | `reachPotionMb = 100` set before a boot: override line logged, no missing-key line (a toml edit followed by `/reload` 3 s later was not picked up: not a defect) |
| SQ-RE-16 | PASS | milling, crushing, IE crushing to amethyst dust; no glowstone |
| SQ-RW-23 (bug batch -23b) | PASS | `smokable/*` tags hold no botania, driftwood, quark, crimson, warped |
| SQ-CM-35, SQ-CM-32 | PASS | judged_p1.md: calamari in both tags, the four dishes back, no NCH error |
| SQ-FFH-03, SQ-RSP-09 | PASS | zucchini quiche parses; drink filling / emptying recipes present |
| SQ-AC-02 | PASS | armor 36.0, toughness 18.0 |
| SQ-TER-01b (boot half) | PASS | judged_p1.md; placement half in the worldgen world |
| SQ-TL-09 | OPEN | `/cegen` refuses on a scratch server ("generators only run from the dev instance") |

## 2. Priority boot 2: Flower Test world (`r11ft`)

SQ-POT-07 PASS: `building`, 12 holder lines, `rows 373`, `DONE`; sign at 2 -60 -2, hanging pot one up at 6 -59 0,
filled silt pot at 20 -60 0; restart logs no `[flowerworld]` line.

## 3. Worldgen (`r11w2`, seed 8675309, then W3a / W3b)

`r11w2` (Normal, monsters and animals on, meta log on; boot 93.5 s): w2_worldgen.qb 01:35 to 02:29 CST, follow-ups
w2b / w2c (raspberry), w2d (spawn counts). Log copy `perf/runs/r1101/w2_latest.log`, census copy
`w2_locate_census.json`, trades `villager_trades_w2.json`.

| ID | Result | Evidence |
|---|---|---|
| SQ-TER-01b | PASS (boot) / look pending | load line `109 tagged, 99 trimmed, 1 air-trimmed, 7 sunk starts`, no crypt warning; crypt at -1568 928 for the client tour |
| SQ-HR-05 | PASS | the wild-crops log never names rules (row text wrong); tag `creative_delight:wild_crops/raspberry` = 26 biomes incl. forest, plains, meadow; `place feature creative_delight:wild/raspberry_bush` places; natural bushes in a fully loaded 256-chunk forest square near -272 -46 (3 to 5 counted, on grass) |
| SQ-EN-01 | PASS | trade dump (43 professions, ~2766 offer variants): librarian 113 enchanted books, none with stockpile / recovery / cycle; `nock_enough_arrows` appears only as arrows |
| SQ-EN-02 | PASS | cashier is a villager profession (`lightmanscurrency:cashier`, the entity summon in the row is wrong): 95 enchanted books, no NEA enchantment (the one "recovery" is `create:potato_recovery`) |
| SQ-MRD-03 | PASS | cheese toast shapeless toast + flaxen cheese wedge; `forge:sliced_potato` and `forge:sliced_potatoes` both hold `rusticdelight:potato_slices` (+ sliced sweet potato) |
| SQ-META31-04b | PASS | placed: -5958 87 -1910 and 5802 73 -8182, both `1/1 [minecraft:desert_pyramid]` + meteorite; short slot [387, 583] logged `not placed, core part [minecraft:desert_pyramid] placed 0 of at least 1` (the batch's reject regex matched that refusal line) |
| SQ-WG30-C02b | PASS | census r 96 at 2144 -2592 (100,616 slot chunks, 365 s): 6 false negatives, each with point, biome, nearest entries and start-piece centre; all are underground starts in deep cave biomes (`terralith:cave/mantle_caves`, `minecraft:deep_dark`): the deep-biome tie case |
| SQ-SB-04 | FAIL (likely) | siege meta at -384 32: in 240 s 0 evokers, 0 illusioners, 1 zombie; the T&T tower located 12 blocks away (the meta's own part) had pillagers 2, vindicators 3, no evoker: the siege spot spawns the T&T mix, not the large-tower mix (ST-323). Spawning is noisy: owner to rerun by hand before fixing |
| SQ-SB-05 | OPEN | camp + ruin at -2544 544 at night: 2 pillagers, 0 vindicators / witches / evokers in 240 s (40 natural night monsters around): too few to judge the mix |
| SQ-SB-06 | OPEN | no evoker near the badlands outpost (-1872 -800) in 240 s: totem check not reached |
| SQ-SB-07 | OPEN | ship at -608 -496: 15 entities, no illager and no villager: crew type not seen |
| SQ-SB-08 | OPEN | wagon -307 3478: 2 witches inside, 1 in the ring outside (night: may be natural); swamp hut -2013 1140: 1 inside, 2 outside, no cat in 240 s |
| SQ-SB-01e | PASS | `[spawn-boxes] ... structure(s) boxed` |

W3a (`r11w3a`, r1031 `w3_terrain.qb` rows SQ-TER-02, -03, -04, -06, SQ-SVA-03, -04) and W3b (`r11w3b`, floorPatch
`"CUT"`, SQ-TER-06b) ran to LOOK: positions for the client tour (windswept hills 2016 -1536, snowy slopes 2560 -3232,
deep ocean geodes near 64 -32; sky villages -1568 688, 2768 2672, -2496 2752, 1696 -3665, -3728 -3376). SQ-TER-06
counted no calcite at the first water block (many columns unloaded). SQ-SVA-03: the first village counted no addon
blocks (keg / press / rich farmland 0): look in the client.

## 4. Client block

Throwaway client (`eval_client.py`, JFR on) in the session scratchpad, instance `mods/` plus the 01:56 patcherMod and
Creative Delight builds hard-linked in; worlds QA_A1 (r11p1 with the r1031 A1 look targets rebuilt by
`r1031/a1_world.qb` rows SQ-VS-04, -02, SQ-CS-02, SQ-KT-21, SQ-POT-06, SQ-SVA-02), QA_W2, QA_W3, QA_W3B.

Tool fixes made during the run (`tools/scratch_query/qrun.py` client mode): chat commands are typed with a leading `/`
(without it every batch line was sent as a chat message; the r1031 client batch had never run); typing at 40 ms per
key with pauses after Return (15 ms dropped characters and let a stray `t` into the next line). Client batches insert a
`tp` to each row's area before building (chunks there were not loaded). Loading Protection holds a joining player
for 60 s.

| ID | Result | Evidence (shots in `perf/runs/r1101/shots/`) |
|---|---|---|
| CQ-PRI-01 | PASS (row text stale) | glass, ice, iron ore, grass, stone: yellow "Silk Touch Required: Drops Differently"; oak log, persistent leaves: no line; silk pickaxe hides it; no book icon. The row's "glass, ice, iron ore green True" predates the three-state split: in this pack glass drops Quark glass shards, ice Neapolitan ice cubes, iron ore raw iron, so `silk_touch_blocks.json` puts all three in `differs` (wthit_lines.md 2.11 rule) |
| CQ-PRI-02 | PASS (texture) / FINDING | `creativeengineering:powdered_sugar_block` (row id `creative_delight:` was wrong): texture loads, tiles; but WTHIT names it "Snow Block", mod Minecraft (the block extends `PowderSnowBlock`; lang has "Powdered Sugar"): owner |
| CQ-PRI-03 | PASS | Mods list: "Let's Do Vinery (Creative Brewing)", "Brewin' & Chewin' (Creative Brewing)", "Creative Brewing" (Foundation 1.0.1 and the bundle 1.0.0), "Creative Engineering" (no "(Alfonzo)" anywhere); WTHIT on the wine press and keg shows the same names |
| CQ-PRI-04 | PASS (names) | potted poppy "Contains: Poppy"; "Large Flower Box" (Supplementaries), "Oak Planter Box", "Silt Pot"; contents of the boxes and the tooltips not checked by hand |
| CQ-HR-06 | PASS | Raspberry Bush: cutout art, walk speed 0.36x, Growth 0 % / 67 % / Mature, Fertile Spring, Offseason Winter / Dry, mod "Hearth and Harvest (Creative Delight)" |
| CQ-KT-22 | PASS (look) | fridge textured, racks placed; dyed fridge box by hand not done |
| CQ-OAT-01, CQ-TSK-01 | PASS (look) | oats bushy, ripe oats tall; treated-stick tomato vine and plain stick textured |
| CQ-CSR-01 | PASS | every `creative_delight:kubejs/...` client script loaded once, no KubeJS client errors |
| CQ-WC-06, -07, CQ-SG-01, CQ-NS-02, CQ-JB-01 | OPEN | hand steps (clicks with the can / spout gun, nightstand menu, JEI search) not driven |

r1031 client batch (copy `r1101/c1_client.qb`, 30 rows; CQ-SB-06 and CQ-WGLOOK not run as rows), judged offline in
`perf/runs/r1101/judged_c1.md`: PASS 5 (CQ-R31-BOOT, CQ-UI-02, CQ-CS-07, CQ-GF31-06, CQ-EFX), FAIL 2, BATCH-FAULT 5,
OPEN 20 (hand-only look lines).

| ID | Result | Evidence |
|---|---|---|
| CQ-UI-01 | FAIL (tobacco only) | hotbar shows "Tobacco Package", not a tobacco bundle name, though `namechanges.js:308` renames `item.the_dirty_stuff.tobacco_package`; the other 8 renames show |
| CQ-TD-06 | FAIL (recheck) | 5 s after the date set SeasonHUD read "Summer, Day 1/24 7:30 pm", not Mid Autumn Day 13/24; server message, Xaero "Day 253, 19:30" and re-tint correct. May be season sync lag: reshoot after 20 s before filing |
| CQ-LB-01 | PASS iron / netherite; copper BATCH-FAULT | copper command hit the typing glitch |
| CQ-AC-04, CQ-VS-09, CQ-KT-19, CQ-SVA, CQ-FBS | BATCH-FAULT | typing glitch (AC-04), aim / pitch / fall (VS-09, KT-19, SVA, FBS); `vinery:jungle_grape_vine` is not a block id. Rerun notes in judged_c1.md |

Worldgen tour (`c2_tour_w2.qb`, `c2b`, `c2c` in QA_W2; `c3`, `c3b` in QA_W3):
- QA_W2: siege tower on its cliff, witch wagon area, swamp, cloud cluster islands (sky), crypt area ruins: nothing
  floating or cut. Struck monument at -4304 3790: monument under water, meteorite not made out in the rain.
- **Finding (SQ-META31-04 look):** both placed meteorite desert pyramids (-5958 -1910, 5802 -8182; log `1/1
  [minecraft:desert_pyramid]` + meteorite 18 to 19 blocks off) show a large crater and no sandstone pyramid above the
  sand; a view from inside at y 80 shows the pyramid's treasure shaft and chests intact. The crater (or the pyramid's
  terrain handling) removes or buries the above-ground pyramid. Shots `CQ-WGLOOK-W2b_*`, `CQ-WGLOOK-W2c_*`.
- QA_W3: the first pass fell to the ground (gamemode switch lost), the spectator pass ran at night in rain and snow:
  too dark to judge the TER-02 to -04 slope looks or the geodes. Sky village at 2768 2672 has its full village; the
  addon houses were not made out. SQ-TER-02 to -04, -06, -06b, SQ-SVA-03 looks stay OPEN for a daytime fly-through
  at the positions in section 3.

## 5. Summary for the owner

Perf session `r1101` (11 boots, JFR on, no prune): `perf/sessions/r1101/summary.md`; client recording
`perf/runs/r1101/client.jfr`.

Fixed by the 2026-11-01 jars (previously FAIL): SQ-AC-02 (36 armor, 18 toughness), SQ-CM-32 / SQ-CM-35 (calamari
dishes), SQ-RW-23 (smokable tags), SQ-META31-04 log half (no short placement), SQ-TER-01 (crypt sunk, no warning).

Open points for the owner:
1. (Settled by the owner 2026-11-01) Vinery and Brewin' are one package under Creative Brewing and are not tested separately: SQ-CDF-07's without-Brewin' half and SQ-CBJ-05 are dropped (see creative_brewing.md).
2. Open problems below are now TODO rows: `structures/metas #12`, `mobs/pillagers #6`, `farming/crops #30`, `gameplay/time #7`, `food/seasoning #14`, `ui/names #12`.
3. Meteorite desert pyramid: crater with the above-ground pyramid gone (section 4).
4. SQ-SB-04: the siege meta spawned the T&T mix, no evokers / illusioners in 240 s (likely FAIL; spawning is noisy).
   SQ-SB-05 to -08 inconclusive.
5. CQ-PRI-02: the Powdered Sugar block shows as "Snow Block (Minecraft)" in WTHIT.
6. CQ-UI-01: Tobacco Package not renamed; CQ-TD-06: SeasonHUD season after `/cetime date` (recheck).
7. Queue text to correct: CQ-PRI-01 (glass / ice / iron ore are "Drops Differently" by the three-state rule),
   SQ-CBJ-03 (Foundation has no items), SQ-PRI-01 ("Supreme Pizza" does not exist), SQ-HR-04 (item id `raspberry`),
   SQ-HR-05 (the wild-crops log never names rules), SQ-EN-02 (cashier is a profession, not an entity),
   SQ-PRI-02 (large pot ids `floralench:large_<flower>_pot`), CQ-PRI-02 (block id `creativeengineering:`).
8. SQ-WCT-03: the well tank stayed at 86000 mB after a fill.
9. SQ-TL-09: `/cegen` only runs from the dev instance.

Not run: L1 to L4 (pregen, perf suite, rpack bench, DH flight), the hand steps of every client row, the pre-10-28
backlog.

## 6. Creative Split wave 1 scratch run (rcs1, 11:20 to 12:05 CST)

Owner request: run the scratch tests of the split's first wave (Calendar and Devtools carves, commit 72cc7303).
Batches `tools/scratch_query/batches/r1101/cs1_split.qb`, `cs1b_restart.qb`, `cs1c_normal.qb`; results
`perf/runs/rcs1/`. Jars: the `mods/` jars of 2026-11-01 11:02 to 11:16 (patcherMod 1.1.05, creativecalendar,
creativedevtools, creative_delight, creative_brewing), hard-linked by prep_srv.sh. Boot 1 on the instance config (CE
toml still held `[seasons]`, `[time]`, `[crop_seasons]`); boot 2 = same world restarted with
`[debug.level_random] probe = true`; boot 3 = group B world (seed 8675309) for the two rows a superflat cannot answer.
No perf session (JFR off).

| ID | Result | Evidence |
|---|---|---|
| SQ-CAL-01 | PASS | loaded line; one WARN naming the moved keys; all 10 common mixins apply (12 apply lines, CropSeasonGrowHook into IE hemp, Dirty Stuff tobacco, Nether's Delight fungus colony). `SeasonBiomeConfigMixin` and `SeasonHooksTypesMixin` apply lazily on the first player join (same as before the carve, r1101 p1 at 01:03), not at boot |
| SQ-CAL-02 | PASS | calendar toml has `[seasons]`, `[crop_seasons]`, `[time.day/.seasons/.wine/.wine.boosts]`, `[perf] seasonsMeltIntervalTicks = 4`, `[wthit] cropSeasons`, `cropOutOfSeason`; CE toml corrected on boot 1; boot 2 no WARN |
| SQ-CAL-03 | PASS | pack order `creativeengineering, startup_scripts, creativecalendar, creativedevtools, creative_delight, creative_brewing`; core finished; `server scripts ready, features on: seasons, crop_seasons, time`; no `global.CAL missing` |
| SQ-CAL-04 | PASS | `/cetime`: preset SINGLEPLAYER, 45 + 15 min, 8-day sub-season; push "nothing to push"; date in SQ-TD-01 format; `[time] Better Days from [time]` at start |
| SQ-CAL-05 | PASS | day 1 -> day 2 with `/time add 24000`; `/season get` agrees ("Mid Spring, day 2/8" after the month step). Queue text: `/season` alone is incomplete, use `/season get` |
| SQ-CAL-06 | PASS | both `say` lines; `#creativeengineering:seasons/arid` holds 15 biomes (desert, badlands x3, Terralith sands); desert and badlands ARID; on boot 3 the locate found `terralith:desert_oasis` 1686 blocks away (superflat: none in reach, as expected) |
| SQ-CAL-07 | PASS | `WineYears.getDays` 1 -> 9 with `/cetime date add 1 month`, back to 1 with -1 month ("wine clock moved back"); no `WineTime` / `NoClassDefFound`. A read in the same tick as the forward step still shows the old day. `/wine time` prints bare lang keys on a dedicated console (scratch-only `/zwine` probe used) |
| SQ-CDV-01 | PASS | `loaded, members: [ceperfbot, cecapture]`, no NOT loaded, no hook NoClassDefFoundError; core finished and server scripts ready; toml has `[debug.*]`, `[worldgen.mass_place]`, `[features]`; CE toml has neither |
| SQ-CDV-02 | PASS | every command answers (`/cegen list`: "generators only run from the dev instance", as SQ-TL-09); `/recipescan` "Scan complete. 65 total recipe references ... 0 unreadable"; `spawns.json` written; `flower_world.js` loaded |
| SQ-CDV-03 | PASS | `/dumpall` 24/25 in 209 s on the superflat, the miss `/dumpclimatecaves` (overworld is a FixedBiomeSource there); on boot 3 `/dumpclimatecaves` wrote 43732 points, 138 biomes in 16.7 s. No ReferenceError / guardThrowable / writeExportJson in server.log |
| SQ-CDV-04 | PASS | 30 `creativedevtools:` recipe ids (mech crafting per metal and wood, netherite, `seq_test_star_all_processes`); with the probe on the world loads and no `[LevelRandomProbe]` line (no off-thread use seen; the probe has no "on" line) |
| CQ-CAL-08 | OPEN | client row, not run |

Found and fixed: `tools/scratch_query/zq_probe.js` still loaded `creativeengineering.seasons.SeasonTypes`, so `/zq`
was missing on every scratch server since the carve; repointed to `creativecalendar.seasons.SeasonTypes`. No pack
script names the moved packages. Batch faults (regexes, not the carve): `recipescan` summary wording, the vanilla
locate line ("The nearest ..."), the `dumpclimatecaves done` line has no brackets.
Third-party noise seen: `dumprecipes` reports 7 Create Mechanical Extruder recipes with an abstract `getType()`.

## 7. Creative Split waves 2, 3 and 4 scratch run (rcs234, 13:58 to 15:33 CST)

Owner request: run the scratch tests of the split's waves 2 and 3 (SQ-CS2, SQ-CMP), then wave 4 (SQ-CBJ4, SQ-CBM,
SQ-CDS, SQ-CDJ4). Driver `tools/scratch_query/split_w234.sh prep1|prep2|prep3 OUT` (one group B world, seed 8675309,
upgraded across three jar sets as a player save would be); batches `tools/scratch_query/batches/r1101/cs2_boot1.qb`,
`cs2_boot2.qb`, `cs2_boot2b.qb`, `cs4_boot3.qb`, `cs2r_boot1.qb`, `cs2r_boot2.qb`; results `perf/runs/rcs234/` and
`perf/runs/rcs234r/` (row logs plus `rows/ID.shell.txt`, boot log copies, `boot2_tags/` = the pre-move tag dump).
JFR off. New helpers: `region_ids.py` (ids in a world's region files), `tag_diff.py` (two /dumptags exports compared);
`qrun.py` now keeps each row's shell output.

- **Boot 1 (old):** patcherMod 1.1.05 (`development/patcherMod/jar_archive/displaced/`), `creative_delight` and
  `creative_brewing` (`jar_archive/displaced/`), the installed Calendar with its Compat dependency stripped from the
  jar's mods.toml (no wave 1 Calendar jar was archived; nothing else in it changed since HEAD), no Devtools (so no
  perfBot), HEAD `kubejs/`, `config/`, `defaultconfigs/`. Edited before boot: CE `[campfire] rainOutTicks = 200`, CD
  `[qol] placeableTooltips = false`, a marker comment in the CB file (it has only `[members]`).
- **Boot 2 (waves 2 and 3):** the installed `mods/` jars (patcherMod 1.1.06, creativedelight, creativebrewing,
  creativecompat, calendar, devtools of 12:49 to 12:53) and the instance's current `kubejs/`. **The instance is not a
  pure wave 3 state:** its `kubejs/` already holds the wave 4 script moves (the moved startup files exist only inside
  the wave 4 CB / CD jars), so boot 2 had no CE potions; IE bottling / Create emptying recipe errors and the
  `creativebrewing:kubejs/vinery/wandering_trades.js` error there are that mismatch, not waves 2 or 3. Boot 2b = restart.
- **Boot 3 (wave 4):** build jars copied in (patcherMod 1.1.07, CD 14:46 with the cob pipe fix, CB 13:47, Compat and
  Calendar 13:52); CE file edited before boot (`quarkLine = false`, `reductionPerLevel = 0.3`, copper `10` / `1.0`,
  `emptyCanChance = 0.5`, `troughMissCooldownTicks = 300`). Boot 3b = restart with the NCH.cfg fix.
- **Remap rerun (rcs234r):** fresh world, boot 1 jars, then straight to the wave 4 jars with the remap fix.

| ID | Result | Evidence |
|---|---|---|
| SQ-CS2-01 | PASS | save with `creative_delight:treated_stick` (axis_y) and `treated_stick_tomatoes[age=2]` on farmland, a chest, a `village_plains` start 2754 blocks out (forceload 7 chunks off). Queue text was wrong: the old CD registered only the two blocks and the processor type, no items, so there are no item ids to remap (the stick item is IE's) |
| SQ-CS2-02 | FAIL, fixed, PASS on rcs234r | boot 2: `the survivor is not registered, left missing` for both blocks. Cause: Forge fires `MissingMappingsEvent` from its STAGING registry (`GameData.injectSnapshot`), which holds only the snapshot's ids, so `mapping.getRegistry().containsKey(newId)` is false. Fix: look the survivor up in `RegistryManager.ACTIVE` (patcherMod 1.1.07 `SwapRemapper`). rcs234r: `[swaps] 2 list file(s): 2 namespace move(s), 0 single id(s)`, `minecraft:block: 2 id(s) remapped to their survivors`, no WARN |
| SQ-CS2-03 | FAIL, fixed, PASS on rcs234r | stick `axis_y` and tomato `age=2` kept under `creativedelight:`; after a save the region file names only `creativedelight:treated_stick*`. Note: a world opened once with the broken 1.1.06 remap keeps the old palette names with no snapshot entry, so no later boot remaps them (the rcs234 world); 1.1.06 never shipped |
| SQ-CS2-04 | PASS | resaved village chunks name `creativedelight:village_farm_crops` (2, r.3.3), untouched ones keep the old id until loaded (r.4.3); no `Unknown processor` / decode error. Farm crop look is a client check |
| SQ-CS2-05 | PASS | both `carried over` lines; `creativedelight-common.toml` holds `placeableTooltips = false`, underscore files gone (the CB marker comment is dropped by Forge's rewrite) |
| SQ-CS2-06 | PASS | pack order `creativeengineering, startup_scripts, creativecompat, creativecalendar, creativedelight, creativedevtools, creativebrewing`; CD / CB core lines; 0 `creative_delight` in both KubeJS logs; no `Unknown feature creative_delight`; `/recipescan` 65 references, 0 new. (Batch FAIL was `grep -c` exiting 1 on 0) |
| SQ-CS2-07 | PASS (server half) | no NCH.cfg / IngredientActions WARN in boot 2. The can craft is a client check |
| SQ-CS2-08 | PASS | `IwServerLevelTickMixin` from creativeengineering.mixins.json, `[perf] Immersive Weathering callTick ... IwGrowthFast.blockTick` |
| SQ-CMP-01 | PASS | Compat core line; 25 mixins applied, none also from creativeengineering.mixins.json |
| SQ-CMP-02 | PASS | `config sections [leaf_piles, moss, vine_shearing, campfire, monster_box, trash_slot] carried over`; Compat `rainOutTicks = 200`; CE has none of the six; boot 2b no WARN, no second carry |
| SQ-CMP-03 | PASS | `creativecompat: 2 of 2 files loaded from its jar`, ItemPhysicSplit and ItemBurnTransform loaded, `[item_physics]` tag summary |
| SQ-CMP-04 | PASS | `/dumpphysics` answers, no error |
| SQ-CMP-05 | PASS | TNT item on fire (netherrack under it) burned; netherite in lava survived (the "lost" ingots had merged into the first one: Count 4). First try failed only because the fire had no block under it |
| SQ-CMP-06 | PASS | box placed and given (`creativeengineering:weathered_cardboard_box`), monster box reward table rolled (4 Rabbit Hide), netherite has `physics/fireproof` and `physics/lavaproof` |
| SQ-CMP-07 | LOOK | 10 `paintings` log lines, no painting load error; the 475-image count is a client check |
| SQ-CMP-08 | PASS (wave 3) | no Compat script error; the one error was the wave 4 `vinery/wandering_trades.js` under the wave 3 CB jar (mismatch above) |
| SQ-CBJ4-01 | PASS | CB: 13 of 14 common mixins apply (`PotionHelperMixin` did not apply under patcherMod either), none also from CE |
| SQ-CBJ4-02 | PASS | `config sections [alcohol, effects, magic_resistance, copper_poisoning] merged from creativeengineering-common.toml into creativebrewing-common.toml`; CB file has the four edited values; CE has none; boot 3b no WARN |
| SQ-CBJ4-03 | PASS | long_reach potion given; the dispenser burst the potion bucket and kept the empty bucket (vanilla would eject it); `[potion_area] dispenser handler installed`. The cloud had gone by the 3 s check |
| SQ-CBJ4-04 | PASS | Insomnia refused (`immune to effects`), Agility applied as `vinery:climbing_effect` |
| SQ-CBJ4-05 | PASS | Poison (Id 19) after the roll at game time 27758. Queue text: the CB file also has `jitterSeconds = 120`, so the first roll can be 130 s out, and the poison lasts 4 s: poll every 2 s past the stored `ce_copper_poison_roll` |
| SQ-CBJ4-06 | PASS | no DragonBreathBrewing / Lingerification error; the CB jar holds `data/creativeengineering/recipes/lingerification_list.json` |
| SQ-CBJ4-07 | PASS | iron golems (perfBot bots are invulnerable): Instant Damage I took 6.0 (94.0) without and 4.2 (95.8) with Magic Resistance I, 30 % at the scratch value; same again on boot 3b |
| SQ-CBM-01 | PASS | `creativebrewing: 12 of 12 files loaded from its jar`; `[alcohol] food values set on 44 of 44 drinks`; `[food values] 12 effect swaps`; `[potion_area] dispenser handler installed`; no `global.trades missing` |
| SQ-CBM-02 | PASS | potion bucket, Magic Resistance and long_reach potion exist |
| SQ-CBM-03 | PASS | ids `creativebrewing:createfilling/...`, `creativebrewing:iebottling/...`; bottling 2295 recipes, tipped arrows 127 potions x 2, 82 JEED providers (boot 2 had no CE potions, so no like-for-like count) |
| SQ-CBM-04 | PASS | `[wandering trades] common 208, rare 39` and `winemaker 34` after `/reload` |
| SQ-CBM-05 | PASS | `forge:fluid/potion` = `create:potion` only; `forge:fluid/dragon_breath` = Central Kitchen dragon breath; IE's potion fluid only in `forge:potion` |
| SQ-CBM-06 | LOOK | dragon sword + echo set: Phantom Bane, Soul Cross, Night Vision active; judge against the armor / tool effect tables |
| SQ-CDS-01 | PASS (server half) | sample syringe scripts load from CD, both syringe items given; sampling a cow is a client / bot-use check |
| SQ-CDS-02 to -04 | PASS | `tag_diff.py` against boot 2: all 16 tags the same (oven heat sources 29, FD heat sources 40, mineable/shovel 218, farmland soils 3, hoe 5x5 3, sourdough 1, tea 1, honey 1, kelp net 10, needs_copper 14, iron 345, stone 596, diamond 57) |
| SQ-CDJ4-01 | FAIL, fixed, PASS | first boot crashed: `MobCobPipeSlotMixin` targeted `Mob`, but `m_147233_` (`getEquipmentSlotForItem`) is on `LivingEntity`. It never applied in patcherMod either (that config has no `defaultRequire`, so the miss was silent: Cob Pipe shift click / dispenser to the head never worked); CD's `defaultRequire: 1` made it fatal. Retargeted. A scan of every CD / CB / Compat / Calendar mixin's SRG method names against its target class found no other miss. After the fix: CD 37 of 39 common mixins apply (`KettleDispenseMixin`, `TobaccoPlacementMixin` did not apply under patcherMod either), none also from CE |
| SQ-CDJ4-02 | PASS | `config sections [crop_growth, dandelion_maturing, crop_drops, stem_fruit, canning, cooking, perf] merged ... into creativedelight-common.toml`; `emptyCanChance = 0.5`, `troughMissCooldownTicks = 300`; CE has none; boot 3b no WARN. rcs234r: rename and merge in one boot, all edited values kept |
| SQ-CDJ4-03 | PASS | members end `baby_mobs, incubation_additions`; no standalone jars; `baby_mobs.mixins.json` and `incubation_additions.mixins.json` apply from the nested jars |
| SQ-CDJ4-04 | PASS | oven placed over a lit campfire with its block entity; no missing registry line. (Powdered sugar has no block item, its item is `powdered_sugar_bucket`) |
| SQ-CDJ4-05 | FAIL, fixed, PASS | `NCH.cfg` returned undefined for every section that moved to CD / CB: Rhino throws (`InternalError ... has no public instance field or method named "CANNING"`) on a missing static field of a Java class, and CEConfig is probed first. Fix in `neoCraftHelper.js` (patcherMod 1.1.07): a missing field reads as "not here". Boot 3b: no WARN, values read from CDConfig |
| SQ-CDJ4-06 | FAIL, fixed, PASS | symptom of the NCH.cfg bug: `soil_yield.js ... Cannot read property "farmlandTries" from undefined` (6x). Boot 3b: `[soil-yield] no crop switched on`, 0 event errors in server.log |
| SQ-CDJ4-07 | PASS (server half) | lunch bag given, no lunchbox serializer error |
| SQ-CDJ4-08 | PARTIAL | Emu Nest (`incubation_additions:emu_nest`, the queue's id was wrong) exists; baby squid not testable here: `spawn-animals=false` makes vanilla discard every Animal / WaterAnimal on its next tick (needs a world with animals on) |

Found and fixed (all in unreleased 1.1.07 work, built, NOT installed): `SwapRemapper` (patcherMod), `NCH.cfg` missing-field
lookup (patcherMod `neoCraftHelper.js`), `MobCobPipeSlotMixin` target (CD; player-visible since 1.1.03, changelog bullet
under "Cob Pipe"). Test-setup lessons: hard-linked jars were rewritten in place by a parallel session's patcherMod
build mid-run (a non-reobf jar, `NoSuchFieldError: ENTITY_TYPE_REGISTRY`), so `split_w234.sh` copies jars; perfBot
bots take no damage; group A/B scratch servers run with `spawn-animals=false`.

Still open: client rows (CBJ4-C1 to -C3, CBM-C1 to -C4, CDS-C1 / -C2, CDJ4-C1 to -C5, CS2-07 craft, CMP-07 count),
baby squid, sampling a cow. Install of wave 4 (game closed): `./gradlew :patcherMod:installToMods
:creativeCompat:installToMods :creativeCalendar:installToMods :creativeDelight:installToMods
:creativeBrewing:installToMods --offline`; until then the instance's `kubejs/` (wave 4) does not match its `mods/`
(wave 3): no CE potions in a game launched now.
