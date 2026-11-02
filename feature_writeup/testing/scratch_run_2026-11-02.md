# Scratch run 2026-11-02 (r1102): Creative Delight nests the five ports

Targeted run (one carve: plan_creative_delight.md G steps 1 to 3), queue section "SQ-CDJ5" in
[scratch_queue.md](scratch_queue.md). Owner request 2026-11-02: implement the CD plan, refresh the queue, run it.

## 0. Performance capture

Skipped: targeted run of one carve (skill run default 4 allows it).

## 1. Scope

Taken: SQ-CDJ5 run plan Boots 0 to 5 (new rows SQ-CDJ-09, -10, -11, SQ-DDP-06, -07, SQ-VTD8-01, SQ-MDP-17,
SQ-CDFW-10, -11, and the rerun list in that section). Not taken: the client block (CQ-CDJ-01, CQ-DDP-01, -02,
CQ-MND-01): the queue pin holds creative-split client rows for the bulk client session.

## 2. Tools

`prep_srv.sh` (drop list for the five nested ports updated 2026-11-02), `scratch_server.sh`, `qrun.py`. Batches:
`tools/scratch_query/batches/r1102/` (b0 by the runner, b1 to b5 and README.txt with per-boot setup). Results:
`perf/runs/r1102/<boot>/`. OUT: scratchpad `r1102/`.

## 3. Jars

| Jar | mods/ | build/libs | Rows |
|---|---|---|---|
| creativedelight-1.0.0.jar (20 members) | 11-02 00:45 | 11-02 00:45 | all |
| creativebrewing-1.0.0.jar | 11-01 20:31 | same (only `-own` rebuilt 00:48; sources unchanged) | SQ-RDX |
| displaced backported{Display -01, Fungi -01, Miners -02, Veggies -04, Vintage -10} | no | jar_archive/displaced | Boot 0 only |

## 4. Order

| Step | Setup | Run | Estimate |
|---|---|---|---|
| Boot 0 | prep A cdj5 + displaced jars | b0_oldworld.qb | 10 min |
| Boot 1 | displaced jars out, configs reset, old Display/Vintage files with test values | b1 | 90 to 120 min |
| Boot 2 | CD switches | b2_switches.qb | 15 min |
| Boot 3 | no miners_upstream | b3_nodonor.qb | 3 min |
| Boot 4 | 1.4.5 donor only | b4_wrongdonor.qb | 3 min |
| Boot 5 | group B fresh | b5_groupB.qb | 60 to 90 min |

## 5. Row map

See the SQ-CDJ5 run plan table; batch files named per boot.

## 6. After the run

Run log entry in the queue, results into display_delight.md 6, vintage_delight.md, fungi/miners writeups, README, TODO.

## 7. Run notes

- 01:03 Boot 0: prep_srv A cdj5 (drop list removed the 5 nested ports, babyMobs, incubationAdditions; perfBot added), 5 displaced jars copied in, booted.
- 01:03 Boot 0 Done (84 s). Five ERROR lines "member <id> is loaded from backported...jar ..., not from the Creative Delight jar" (Display -01, Fungi -01, Miners -02, Veggies -04, Vintage -10).
- 01:05 b0_oldworld.qb: home marker at 0 -60 0; 12 blocks placed at the README offsets; Pickling Jar holds 3 garlic + 3 honey bottles, Copper Pot 2 potato + 1 carrot. SQ-CDJ-11 PASS. Saved.
- 01:07 Boot 0 stopped. Note: Boot 0 already ran the CD config move (both old files .moved) and the standalone old ports then wrote fresh displaydelight / vintagedelight tomls; Boot 1 resets configs as the run plan says.
- 01:26 Boot 1 setup per batches/r1102/README.txt: region scan before (r.0.0: display 4, vintage 4, miners 4, veggies 3, fungi 1 ids), displaced jars removed, configs reset (old Display file DisableTakeAllInteratction=true, old Vintage remainderBuffer=false, CD toml without [display]/[vintage]), spawn-animals=true, difficulty=easy, fungi/zcount/ztrades probes. Booted.
- Batch writer notes: SQ-DDP-07 sneak halves, SQ-MDP-07 eat, -10 entity use, -14 chunk reload, -06 cook, SQ-RDX-05 JEI are LOOK / client (CQ-DDP-01, CQ-MND-01). Boot 0 jar holds whole garlic, not cloves: not mid-batch (SQ-CDJ-05 only checks contents survive).
- 01:27 Boot 1 Done. Log: vintagedelight-common.toml carried (3 values), displaydelight-common.toml carried (9 values), both renamed .moved; members line present. b1_main.qb started (results perf/runs/r1102/b1).
- 01:52 b1_main done: PASS 12, FAIL 16, LOOK 35. SQ-CDJ-05: Unidentified mapping displaydelight:od_plated_baked_tentacle_on_a_stick (block + item) from Display -01; -02 no longer registers it (tentacle foods removed, members #24): FINDING, old placed plates of it are lost; needs a swap-list entry or owner OK. SQ-CDJ-09 /give: BATCH-FAULT (console pads 'Gave 1    [', regex wants one space); all five gives worked; Miner's cave_carrot shows untranslated on the server console. b1_long started.
- 02:04 b1_long: FGD-07a LOOK, FGD-07b FAIL (to judge). Boot 1 logs copied to perf/runs/r1102/b1/srvlogs. Boot 2: displaydelight=false, takeAll=true, remainderBuffer=true; booted.
- 02:06 Boot 2 judged: SQ-CDJ-10 PASS (Display stew destroy drops farmersdelight:beef_stew, no displaydelight error/warn with the switch off); SQ-DDP-07b LOOK-CLIENT (sneak half, CQ-DDP-01); SQ-VTD4-04 PASS (slot 7 output + slot 9 bottle with remainderBuffer=true from CD [vintage], hopper pulls both into the chest, broken jar drops jar, honey, bottle).
- Boot 1 judged (agent, logs in perf/runs/r1102/b1/srvlogs; batch faults fixed in place in b1_main.qb / b1_long.qb, fungi_probe.js line 124 fixed): 65 rows: 45 PASS (incl. batch-regex fixes), 7 FAIL rows from 6 defects, 7 BATCH-FAULT not proven (SQ-DDP-07, SQ-MDP-06 takoyaki, SQ-MDP-10 basket, SQ-MXS-03 shears case; rest proven PASS from the log), SQ-MDP-07 LOOK-CLIENT (CQ-MND-01), SQ-DDP-04 hand step. Verdict table in section 8.
- 02:14 Boots 3-4 (tools/scratch_query/batches/r1102/boot34.sh; own pids killed after the FML stop): SQ-MDP-17 PASS (stop names Miner's Delight (Creative Delight), both missing jars with links, only miners_delight fails); SQ-MDP-15 PASS (only the 1.1.2 jar named missing, 1.4.5 copy accepted). Symlink restored.
- 02:14 Boot 5: prep_srv B cdj5b spawn-animals=true (seed 8675309); FGD-12 squares lowered to radius 300 (disk 14 GB). Booted.

## 8. Final verdicts

Boots 0 to 4 (perf/runs/r1102/b0..b4; Boot 1 judged from b1/srvlogs). BF = batch fault, fixed in the batch.

| ID | Result | Evidence |
|---|---|---|
| SQ-CDJ-11 | PASS | five "loaded from backported... not from the Creative Delight jar" lines; 12 blocks placed; jar and pot contents set |
| SQ-CDJ-09 | PASS | members line 20, none NOT loaded; no backported jar; 5 gives (BF: padded "Gave 1"); items display 224, fungi 32, miners 69, veggies 89, vintage 138 |
| SQ-CDJ-05 | FAIL | Unidentified mapping `displaydelight:od_plated_baked_tentacle_on_a_stick` (block + item): Display -02 no longer registers it (tentacle foods removed). Rest passed: all Boot 0 blocks, states and contents survive, region diff empty |
| SQ-DDP-01 | FAIL | same mapping as SQ-CDJ-05; rest passed |
| SQ-DDP-02, -03, -05 | PASS | own-mod drops, plate returned; shakshouka/omelette; stew drops FD stew |
| SQ-DDP-04 | LOOK | 229 links (217 on 10-30); 7 tag items with no Display block (Miner's bat_soup, insect_stew, seasoned_arthropods, insect_sandwich, insect_wrap, improvised_barbecue_stick; Ends steamed_dragon_egg_block); catalog hand step |
| SQ-DDP-06 | PASS | `[display] takeAll = false` + 8 keys, `.moved`, one "carried into" line |
| SQ-DDP-07 / -07b | not proven | BF (bot held a pickaxe) and sneak halves need CQ-DDP-01 |
| SQ-VTD8-01 | FAIL | config carried, bottle ejected, but the jar makes 1 of 3 batches then stalls: `consumeIngredient` drains the first matching slot (FermentingJarBlockEntity) |
| SQ-VTD4-04 | PASS | remainderBuffer=true: output slot 7, bottle slot 9, hopper extracts both, break drops all |
| SQ-VTD4-06, SQ-MXS-01, -02 | PASS | |
| SQ-MXS-03 | not proven | BF (shears case needs a player); chest and grass halves fine |
| SQ-VTD5-01 | FAIL (spec) | slices 4/4/4/2 right; TNT drops 4 slices against VTD-54 "drops nothing" (`survives_explosion` in the cheese wheel / pizza tables) |
| SQ-CDFW-10, SQ-FD-01 | PASS | 108 / 227 keys, no load error; FD-01 key resolves |
| SQ-CDFW-11 | PASS | 23 lost Veggies recipes all moved to `creativedelight:` or removed on purpose (BF: `--ns`); registry half waits for CQ-CDJ-01 |
| SQ-FGD-00 to -11, -03b, -07a, -07b | PASS | FGD-01 probe TypeError fixed (fungi_probe.js); FGD-07b BF timing |
| SQ-MDP-01, -03, -04a, -04b, -05, -08a, -08b, -08d to -08h, -09, -11, -12 | PASS | cup conversions 16 is right (row text 15 stale); squid drops, no tentacles (design) |
| SQ-MDP-06 | not proven | BF: takoyaki used removed tentacles; other pots fine |
| SQ-MDP-07 | LOOK-CLIENT | values right; eat effects CQ-MND-01 |
| SQ-MDP-08c | FAIL | burning knife spider also drops raw arthropod: upstream `scavenging_spider` lacks `is_on_fire:false` |
| SQ-MDP-10 | not proven | BF (basket has no output); cow/fish CQ-MND-01 |
| SQ-MDP-13 | FAIL | `cave_hamburger` has no recipe: the tentacle `R.remove({input})` in CD miners_delight/recipes.js also removed it (baked_tentacles OR brown_mushroom slot) |
| SQ-MDP-14 | FAIL | pot cooks with the leftover slot full and throws the bucket: `CopperPotBlockEntity.canCook` returns before `leftoversFit()` when the meal slot is empty |
| SQ-MDP-16 | FAIL | 0 cave_carrot_crate in 300 rolls: loot_index.json has no miners_delight tables (donor data), so `L.pack` rule never applies |
| SQ-MDP-15, -17 | PASS | stop message names Miner's Delight (Creative Delight) and only the missing jars |
| SQ-CDM-00 to -05 | PASS | |
| SQ-RDX-01, -04 (server half), -05 (server half) | PASS | gamblers_tea id is `creativedelight:farmersrespitebrewing/gamblers_tea` |
| SQ-CDJ-10 | PASS | switch off: no displaydelight error, stew drops FD stew |

Boot 5 (group B cdj5b, seed 8675309, Chunky squares radius 300):

| ID | Result | Evidence |
|---|---|---|
| SQ-MD-01 | PASS | 4412 wild cave carrots in 2601 chunks, max Y 45, min depth 22, all bands non-zero; log says "2 mod modifiers off" (row text "3" stale, as the 11-01 instance log) |
| SQ-FGD-12a to -12e | PASS | squares generated |
| SQ-FGD-12 | LOOK (partial) | dark forest / taiga: Fungi singles rare (1+1) against brown 455 / red 286, huge inky 25 and morel ~190 blocks; swamp: morel blocks 1215; mushroom-fields square is 3 chunks of mushroom fields in ocean at radius 300: not judged |
| SQ-FGD-13 | PASS | underground Fungi singles: none below the surface band; colonies on stone 3 of 29 and 3 of 16 (not bare stone) |
| SQ-FGD-14 | FAIL | rooted dirt everywhere (DF 15587, TA 14519, BI 2343, SW 581), `fungidelight:truffle_dirt` 0 in all four squares; row: "modifiers ran truffle before blob" (placed_feature wild/truffle_dirt_ore, wild_crops.json) |
| SQ-FGD-15 | not judged | 3 animals in five squares (3 morel cows in mangrove swamp); chunk-generation spawns sparse (gotchas) |
- 02:35 Boot 5 done and stopped; verdicts above. Client block not run (queue pin). Run ends.
