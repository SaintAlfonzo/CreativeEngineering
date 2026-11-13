# r1113 chunk 4 triage

Batches: `c4_a.qb` (group A, level r1113c4a, 42 rows incl. setup row SQ-FLR-02b), `c4_b.qb` (group B, level r1113c4b,
4 rows). Setup is in each file's header. Part 2 reruns: `rerun_a.qb`, `rerun_a2.qb`, `rerun_b.qb` (run rerun_b before
c4_b in the same B boot).

SQ-GAP is marked HOLD in the queue ("only after the Common suite is finished"). SQ-CGE ran 2026-11-08 and no CGE row
is FAIL / PARTIAL in any later run doc, so the hold condition looks met; the GAP rows are batched but can be dropped with
`--rows` if the hold stands.

## Part 1: chunk 4 rows

| ID | decision | reason |
|---|---|---|
| SQ-GAP-01 | c4_a | jam jars via `zq rdetail jam_jar`, cook times via `/zc4 cooktime jam`, stack size via `zq item` |
| SQ-GAP-02 | c4_a (shell, offline) | `gen_moobloom_rules.py --check` (writes nothing) and `check_moobloom_data.py` |
| SQ-GAP-03 | c4_b | Chunky r300 at 0 0, `region_blocks.py` census for `bushierflowers:` |
| SQ-GAP-04 | c4_a | zq tags and rdetail (Mechanical Crafter by text: its pack recipe id is `creativeengineering:shaped/forge_ingots_gold_to_2_mechanical_crafter_by_shaped` in the 11-10 dump) |
| SQ-GAP-05 | c4_a | water, lava, ground drops with a plain egg as control; Pos read at 10 ticks and 4 s; LOOK |
| SQ-GAP-06 | c4_a (shell) | lang grep only; LOOK |
| SQ-GAP-07 | c4_a | survival bot places pineapple sapling, tea seeds, grape seeds on 9 soils (`pbot use`); `execute unless block ... run say` per cell |
| SQ-GAP-08 | c4_a (shell) | grep / awk of the Vinery lang |
| SQ-GAP-09 | c4_a | `/zc4 cmd` reads the command tree (an incomplete and an unknown command print the same console error) |
| SQ-SWL-01 | not batchable | needs SQ-CDFL-03's old-id save: previous CD + Common jars that still register `creativeengineering:` fluids (pre-11-06), not kept. Evidence for the item half already exists: r1111 NSM fixture boot on Common with the 11-08 fix, `perf/runs/r1111/nsm/newjars.log`: 411 items / 29 blocks / 1 entity remapped, 0 `dropped stale alias`. Judge from that or build the old jar set from `jar_archive/` |
| SQ-CDNS-11 | c4_a (rest client) | dish placement by a sneaking bot (`zhh3 sneak`), food-type tag on a moved item; the static part passed r1110 (SQ-CDNS-11a); pouring is CQ-CDNS-01 |
| SQ-NSM-01 | c4_a static half; rest client | the NSM fixture (`perf/runs/r1111/nsm`) has no seasoned food (`nsm_fixture.js` has no seasoning step), so the old-save half needs a fixture rebuild; crafting, overlay, eat bonus, JEI page need a client |
| SQ-NSM-04 | not batchable | client row (picture, Datapacks page, banner render), and the NSM fixture has no paintings or banner: extend `nsm_fixture.js` and rebuild on the 1.2.02 jars first |
| SQ-NSM-08 | c4_b | bot into each of the 11 dimensions, `zq chunk`, three locates, census, `dumptags` scan for leftover `creativeengineering:` tags; Flower Test preset is client |
| SQ-HCS-07 | c4_a | feature nest on an oak (`place feature hearthandharvest:nest`) plus roof nests set by hand with `chests/crow_nest_village` (no village roofs on a superflat) |
| SQ-HCS-10 | not batchable (already run) | it is "the rewritten SQ-HH-401 to -502", run in r1111 / r1112: all PASS except -474 (owner question) and -482 (FAIL, fix not built). Record HCS-10 from those |
| SQ-HCS-12 | c4_a | `zhh3 tame`, shoulder via `zhh3 tally`, dropped and thrown nugget, `zhh3 inv` |
| SQ-RDB-02 | c4_a (last row) | `/dumpall`, then new `rdb_compare.py` against the 11-10 dumps prep copied into srv (270 recipes) |
| SQ-RDB-03 | c4_a + c4_b (server parts) | old rows from r1102b / r1103 batches with coordinates and ids fixed: RD-08 squid half, RD-11, RD-22, RD-24, RDX-01, -03, -03b, -04 to -12, -12b, -14 (A); RD-10 (B). Not run: RD-26 (2000-block biome flights; RDB-04 PASS covers the features), RDX-02 and the JEI halves (client), RDX-13 (old save) |
| SQ-RDB-05 | not batchable | needs a save from the live pack with Rustic plates, crops, crates, potted coffee; no fixture has them (ids unchanged, low risk). Owner: copy a live save that has them, or skip |
| SQ-BREED-02 | c4_a server half; JEI client | tag lines; the `[jei] Just Enough Breeding` log line is client-only |
| SQ-BREED-03 | c4_a (moved B -> A) | `isFood` via `/zc4 isfood`; live bees with only a placed wild carrot (superflat has no other flowers); wild rice checked by tag only |
| SQ-FDI-01 | c4_a | prerequisite re-checked: Carrot jar `SoL-Carrot-1.19.2-1.14.0.jar` is in `~/.local/share/atlauncher/downloads/`, still not in `mods/`; header copies it into srv/mods only. CD 11-11 16:52 build has `PlacedFoodSol$CarrotBridge` |
| SQ-FDI-02 | c4_a | 20 foods eaten via `/zc4 eat` (finishUsingItem plus the Forge Finish event both books listen to); max health 20 |
| SQ-FDI-03 | c4_a | bot bites placed FD apple pie, cake, candle cake, Neapolitan vanilla cake (no Creative Delight cake block found in `placed_food_slices.json`; stand-in); knife on a cheesecake |
| SQ-FDI-04 | client | eating out of a lunchbox goes through the lunchbox item's own use flow and both book screens |
| SQ-FDI-05 | c4_a static half; rest client | serverconfig blacklist grep; book list and lunchbox tooltip are client |
| SQ-FLR-03 | c4_a | Create rig built by setblock (creative motor, millstone), input via `/zc4 put`, outputs via `/zc4 poolsinv` |
| SQ-FLR-04 | c4_a | crushing wheels (controller check, motors swapped when the controller does not form), item entities summoned on top, `/zc4 pools` |
| SQ-FLR-05 | c4_a | heated rig (blaze burner fuelled by `/zc4 fuel`) and unheated rig; basin filled by capability; 7 runs |
| SQ-PWG-01 to -08 | skipped | held for another phase (brief) |

Not batchable: 5 rows (SQ-SWL-01, SQ-NSM-04, SQ-HCS-10, SQ-RDB-05, SQ-FDI-04), plus the client halves listed.

## Part 2: pending reruns (from the fork; batches rerun_a, rerun_a2, rerun_b)

| ID | last result (run) | rerun now? | reason |
|---|---|---|---|
| SQ-WO-03 | FAIL, batch fault (r1109wo) | yes, rerun_a | `Nobody123` -> `@a[name=Nobody123]`; expected message "No player was found" (queue row text needs the same change) |
| SQ-WO-04, -06 | FAIL, unconfirmed (r1109wo) | yes, rerun_a | never rerun; random tick speed 3 for 120 s, prints `sleep_until` |
| SQ-WO-08, -09 | FAIL (r1109wo) | yes, rerun_a | never rerun; tree state read after setblock, trust command and use; new -08b at midnight |
| SQ-WO-34 | BATCH-FAULT twice (r1110, r1111) | in rerun_a, likely BLOCKED | inotify instances 128 of 128: Forge's config watcher cannot start; owner raises `fs.inotify.max_user_instances` |
| SQ-WO-35 | PARTIAL (r1111) | yes, rerun_a (SQ-WO-35t) | tag half via `zq biometag` in group A; placement half judged PASS offline |
| SQ-HCS-05 | PASS partial (r1111) | yes, rerun_a | `zwo skip 6100` x5 passes the refill gate, so the cap of 3 can be reached |
| SQ-HH-501 | FAIL, cause not traced (r1111) | yes, rerun_a | TODO `largecontent/mod-additions #68`: rerun with a watch (`zhh3 watch`, `seen`) |
| SQ-HH-1534 | INCONCLUSIVE (r1112) | yes, rerun_a | alone, tp before sneak, shift state via new `/zsneak` |
| SQ-HH-1551 | not run (two-boot row) | yes, rerun_a (1551a) + rerun_a2 (1551b) | live reload does not fire: key set before a restart |
| SQ-HH-1555 | FAIL, mod gap (r1112) | height-cap half only, rerun_a2 (SQ-HH-1555h) | farmland rule off, hops placed first; the gap is TODO #75, not built |
| SQ-HH-328 | OPEN (r1111) | yes, rerun_b | 600 s locate budget, three origins; run before other locates in the B boot |
| SQ-HH-434, -440, -482, -1507, -1546, -1547, -1548, -1574, -1575, -1593, -304, -309 | FAIL (r1111, r1112) | wait | fixes not built: Creative Delight last built 11-11 16:52, before both runs (TODO #65 to #72, #76); `tilled_end_stone` still defined twice in `item_dict.js`; the mum line in `tags.js` unchanged |
| SQ-OB-05 | FAIL (r1111) | wait | `veggiesdelight/recipes.js` fix not applied (file of 11-10) |
| SQ-HH-474 | FAIL (r1112) | wait | owner question |
| SQ-HH-322, SQ-HCS-03 | INCONCLUSIVE / FAIL density (r1111) | wait | owner questions, TODO #73 items 5 and 6 |
| SQ-HH-262, -476 | FAIL expected (r1111) | no | known, by design |
| SQ-BRA-03 | FAIL (r1111) | wait | 343 untiered blocks; `blocktags.js` unchanged since 11-10 22:45 |
| SQ-BRA-05, -06, -07, SQ-NSM-05, -10, SQ-OB-02, SQ-HH-1607 to -1611 | PARTIAL / client | client | remaining parts need a client or a real player |
| SQ-NSM-07 | PARTIAL (r1111) | wait | superflat fixture (no sky island, meta structure, waystone): needs a group B fixture rebuild |
| SQ-VH-11, SQ-OB-11 | not run | wait | no old save |
| SQ-OB-08 | PASS pending owner | wait | owner (Nourishment) |
| SQ-CDNS-07, SQ-WO-27, -32, r1111 crow / machine / gear batch faults | resolved | no | resolved in r1110 section 10, r1111 or r1112 |

## Id fixes (11-09 / 11-10 namespace moves and later changes)

- `creativeengineering:glowing_calamari`, `glowing_calamari_slice` -> `creativedelight:` (SQ-RDX-12, -12b).
- tag `creativeengineering:roasted_bell_pepper_slice` -> `creativedelight:roasted_bell_pepper_slice` (SQ-RDX-07).
- SQ-RDX-01: bootexpect `Rustic Delight upstream data: N files` dropped (no runtime donor since the bake, SQ-RDB-01).
- SQ-RD-10: log line is now `village farm crops added to N pool elements` (queue says "5 farm processor lists").
- SQ-NSM-08: structure tags `#creativeworldgen:meta/*`, `#creativeworldgen:group/*`; the `kubejs/exported` structure
  dumps still carry `creativeengineering:` names from before the move (stale dumps, refresh with `/dumpall`).
- SQ-GAP-07: "Fertile Soil" = `immersive_weathering:mulch_block`; pineapple = `fruitsdelight:pineapple_sapling`.
- SQ-GAP-05: golden egg = `vintagedelight:golden_egg`; floating tag `creativecompat:physics/floats`.

## Probes and helpers added

- `tools/scratch_query/zc4_probe.js` (`/zc4`): cmd, put, fill, inv, pools, poolsinv, fuel, cooktime, eat, hunger, sol,
  isfood, bees. Parses with `node --check`; not yet run in game (Rhino: first boot may need a fix, e.g. the blaze
  burner `tryUpdateFuel` signature or the Carrot / Apple Pie class names).
- `tools/scratch_query/rdb_compare.py` (SQ-RDB-02): compares one namespace's recipes, tags, loot, items between two
  dump trees; self-test against the instance dumps prints SAME on all five kinds.
- Fork: `tools/scratch_query/zsneak_probe.js` (`/zsneak NAME`).
