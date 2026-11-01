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
