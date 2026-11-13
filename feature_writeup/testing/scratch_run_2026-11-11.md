# Scratch run r1111, 2026-11-11

Runner: session a5b59ef3. Owner request: "execute the scratch queue". Bulk run from the r1110 resume point
([scratch_run_2026-11-10.md](scratch_run_2026-11-10.md) 9): newest sections first, then the blocked normal-world rows,
then the batch-fault reruns, then the fixture rows and the SQ-HH backlog.

## 0. Performance capture

Perf session `r1111`, opened 02:05 before the first boot. Report without `--prune` at the end.

## 1. Scope

Taken, in order:
1. A1, group A superflat, newest build jars: SQ-BRA-01 to -07, SQ-RDB-01, -06, SQ-FLR-01, -06, SQ-HCS-05, -06, -11, SQ-NSM-06.
2. Group B normal world (unblocked by the crow fix): SQ-HCS-01 to -04, -08, SQ-WO-35, -36, SQ-RDB-04.
3. Batch-fault reruns: SQ-WO-27, -32, -34.
4. Fixture rows: SQ-NSM-02, -03, -05, -07, -09, -10 (fixture from `deploy/latest/mods/`), SQ-HCS-09.
5. SQ-HH rest, SQ-VH, SQ-OB, SQ-SCAT.

Not taken: client rows (owner list in section 8); SQ-FDI (Spice of Life: Carrot Edition jar not in `mods/`); SQ-FLR-02 to -05
(need a powered Create crusher / millstone / mixer rig with hopper counting; not built yet); SQ-BREED-01 (`/dumpbreeding` not written).

## 2. Tools

`tools/scratch_query/batches/r1111/`: `gen_a1.py` writes `a1.qb`; `a2.qb` (A1 boot 2). Results `perf/runs/r1111/<step>/`.

## 3. Jars

| Jar | `mods/` | build/libs used | Rows |
|---|---|---|---|
| Creative Common | 11-10 21:20 | 11-10 23:26 | all |
| Creative Compat | 11-10 21:11 | 11-10 23:25 | SQ-BRA |
| Creative Devtools | 11-10 21:23 | 11-10 22:38 | SQ-BRA-02 (`/dumpsurface`) |
| Creative Engineering | 11-10 21:11 | 11-10 23:06 | SQ-FLR |
| Creative Delight | 11-10 21:20 | **11-11 16:52 rebuild** (the 11-10 23:24 jar does not boot, section 7 17:00) | SQ-HCS, SQ-RDB, SQ-NSM |

Scratch servers get COPIES of the build jars.

## 7. Run notes

- 02:05 RUNNER.md claimed, perf session r1111 open, A1 prepared (`prep_srv.sh`, group A, `rustic_upstream` link removed for SQ-RDB-01).
- 02:06 A1 boot 1 CRASHED in mod loading: `NoSuchMethodError IntegerProperty.create(String,int,int)` in
  `roll_delight` `RollMedleyBlock.<clinit>`. The FML crash left the JVM up and the runner did not notice until 16:48
  (the boot was started in the background and nothing waited on it): 14 h lost.
- 16:50 Cause: the Creative Delight 11-10 23:24 jar nests `backportedRoll` before its reobf (55737 bytes, Mojmap names;
  the reobfed member jar is 56265 bytes, 23:25). `ce.forge-bundle.gradle` `bundleJar` depended on each member's `jar`
  task only; ForgeGradle's `reobfJar` rewrites that same file afterwards as a finalizer, so a bundle can pack the
  un-reobfed file. Fix (build logic only): `bundleJar` also depends on each member's `reobfJar`. Delight rebuilt 16:52,
  Roll nested at 56265. The `mods/` Delight (21:20) is not affected. Other bundles (Brewing, Worldgen) have the same
  race in their 11-10 builds unless rebuilt: check with the nested jar sizes before installing.
- 16:53 A1 boot 1 again: Done 10.7 s. `a1.qb`.
- 17:00 SQ-BRA-05 to -07: full-height blocks read "no modifier", short ones the exact value. Probes: the perfBot does
  not collide with blocks (stops at the superflat floor y -60, inside a block placed at -60); with the block as the
  floor (y -61) and the bot walking across 15 blocks of it, still no modifier. BlockRunner's `LivingEntityMixin`
  applies its modifier only when `level.isClientSide`: the on-top value exists only on the client. The values read
  on the server come from Creative Compat's in-block code (sleeping bag, cake, roast chicken, jam, fence, gate).
  Full-block values move to a client check (section 8).
- 17:05 SQ-HCS-05, -06 boot 1 invalid: `spawn-animals=false` discards summoned crows (gotcha); `/place feature` needs
  the configured feature `hearthandharvest:nest`, not the placed `nest_tree_top`. Set up SQ-FLR-06 (saw and crusher
  controller with 16 flesh); stop.
- 17:10 A1 boot 2: same world, `spawn-animals=true`, `rustic_upstream` linked back. Done 10.2 s. `a2.qb`.
- 17:15 A1 stopped.
- 17:18 Step 2, B1: group B normal world seed 8675309, `spawn-animals=true`, Delight 16:52. Done 87 s at 17:20. `b1.qb`.
- 17:25 `b1.qb` finished, but three faults in the batch: the census read level `world` (the level is `r1111b1`); the
  server ran up to 29 s behind during the chunk walk, so the queued `/place structure` and `/summon` ran after the
  checks read them; SQ-HCS-01's `out of bound` regex also matched Dramatic Doors recipe warnings. Census and checks
  redone by hand on the live server (17:27 to 17:40): Alex's Mobs crow summon in a forceloaded chunk; a natural
  village (`locate structure #minecraft:village`, Chunky radius 128); SQ-RDB-04 features under armor stands
  (`positioned over` is 1.19.4+).
- 17:42 B1 stopped. Wise Oak step b on a fresh group B world (`wo_run.sh ... b`, batches `r1111wo/`). Disk 27 GB free.
- 17:48 Wise Oak a2, a4, a5 on the A world `woa` finished; the wrapper's output pipe stayed open (a child kept it), so the
  runner was not notified until 18:50 (1 h idle). Judged 18:55.
- 20:55 Session 46aadb takes over (a5b59ef3 gone, no game JVM up, RUNNER.md re-claimed). Step 4: no NSM fixture
  builder existed; writing `tools/scratch_query/nsm_fixture.js` (model `cdns_fixture.js`). Fixture jars: the 14
  `deploy/latest/mods/` jars (1.2.02, old namespaces, no swap lists) copied over a NOSWAP=1 srv. Waits follow the stall
  watch (590 s cap, 10-minute checks).
- 20:50 Fixture boot 1 on the 1.2.02 jars FAILED in mod loading: Biomes O' Plenty requires TerraBlender (the stub is
  nested only in the new Worldgen jar). Against `deploy/latest/verify/server.installed.sha1` the only other jar not
  in 1.2.02 is Creative Devtools; both dropped (`nsm_fixture.sh` now does it). Note: `scratch_server.sh boot ... | tail`
  holds the pipe open (the server child keeps it); boot with output to a file and `< /dev/null`, then `wait_boot.sh`.
- 21:02 Fixture boot 2: Done 11.7 s. `/nsmfix` steps (chests, placed, ground, bags, bots, gear, labels, crows, dims,
  logout, manifest). Fixed on the way: the botanist pot is Beautify's `botanist_workbench` block with Compat's block
  entity (`Plant` tag, no item handler); the jar's fluid handler refuses Forge water, so its `FluidHolder` is written;
  the label is placed through `FacePaperInteractions.placeLabel` (a bot use opens the chest) and its picture written
  as NBT. Third note erased with the eraser by the bot; a second note on the first face for layer order. Not in the
  fixture: backpack skins and `creativeengineering:iron_camera_bag` (not in 1.2.02), sky island / meta structure /
  waystone (superflat has no Overworld structures; a 1.2.02 normal world risks the r1110 crow chunk crash), seasoned
  food and paintings (client rows). Fixture world, manifest, old configs and both logs: `perf/runs/r1111/nsm/`.
- 21:08 New jars (prep_srv swaps: Common 11-11 16:50, Compat 16:51, Delight 16:52, Engineering 11-10 23:06, Brewing,
  Worldgen, Devtools; the rest `mods/`) on a copy of the fixture, old-named Labels / Exposure configs only. Done 4.3 s.
  Probe `tools/scratch_query/nsm_probe.js` (login dimension, shrapnel spawns, `/nsmprobe recipes`). Reads in
  `perf/runs/r1111/nsm/nsm02_reads.txt`.
- 21:12 The "missing" red stepstool on the ground: a wild Alex's Mobs crow picked it up in the fixture and carried it
  to -15 -44; after the swap the tamed crow holds `creativeequipment:red_stepstool`. Not a loss.
- 21:18 Fixture server stopped; no old id left in region, entities or player files; step 4 done. Short targeted step,
  perf session kept open (r1111).
- 21:25 PAUSED after step 4 (session 46aadb, context budget). Resume point for step 5: SQ-HH starts from the
  r1108hh resume point ([scratch_run_2026-11-08.md](scratch_run_2026-11-08.md) 8): Machines batch fixes listed
  there, then Machines boot 2, Worldgen SQ-HH-301 to -329, Crow SQ-HH-401 to -502 (rewritten for
  `hearthandharvest:crow`), Gear SQ-HH-1500 to -1612; then SQ-VH, SQ-OB, SQ-SCAT. Perf session r1111 left open
  for it (stop and report it at the end of the run, without `--prune`). No JVM up, RUNNER.md idle.


| ID | Result | Evidence |
|---|---|---|
| SQ-BRA-01 | PASS | `BlockRunner tier overlaps checked: 14204 ids, 0 overlap(s)`; block tags started and finished in the same ms |
| SQ-BRA-02 | PASS | 15180 blocks, `errors` 0; asphalt tier asphalt; soul sand no tier, speedFactor 0.4; white carpet collisionTop 0.0625 |
| SQ-BRA-03 | FAIL | exit 3, 343 walkable blocks with no tier in 10 mods: Dramatic Doors 149, Create Framed 85, Everything Copper 56, minecraft 42 (doors, candles, campfires, chain, lanterns, ladder, iron bars, glass pane, lightning rod, sea pickle, soul soil, magma block), SecurityCraft 6, and one each from Cozy's Improved Cats, Domestication Innovation, Immersive Portals, Neapolitan, Philip's Ruins |
| SQ-BRA-04 | PASS | each block in its tier (`rows/SQ-BRA-04.log`); camouflage 169 members; metal holds `metalfences:` |
| SQ-BRA-05 | PARTIAL | jam block -0.055 PASS; crafting table no modifier PASS; chocolate bricks and kelpy stone bricks need a client (BlockRunner is client-side on top of full blocks) |
| SQ-BRA-06 | PARTIAL | sleeping bag, hammock, pet bed -0.012; cake, roast chicken -0.015: PASS. Thatch, cardboard box, tube coral, kelpy cobblestone, scute pavement, polished manure, manure bricks, carrot crate, beehive, jello block: client only. Their tiers in `surface_audit.json` are the expected ones (soft, natural_rough, natural_path, constructed_smooth, wood, very_sticky) |
| SQ-BRA-07 | PARTIAL | copper fence and closed gate +0.025 PASS; mosaic bonds tier constructed_brick in the audit, value client only |
| SQ-RDB-01 | PASS | no `rustic_upstream`, Done, 0 Rustic error lines, no parse error |
| SQ-RDB-06 | PASS | folder linked back: Done, 0 Rustic errors, no `rustic_upstream_assets` (qrun FAIL was the shell `grep -c` exit 1: BATCH-FAULT) |
| SQ-FLR-01 | PASS | 4 crushing + 4 milling + 5 mixing `creativeworldgen:create*/visceral_heap/*`; no `[result-pools]` error, no "more outputs"; NCH warnings only for the `#forge:plantoil` and `#forge:fluid/creosote` fluid tags |
| SQ-FLR-06 | PASS | saw and crusher controller: 16 flesh and `Size: 32` before the stop and after the restart |
| SQ-HCS-05 | PASS (partial) | `place feature hearthandharvest:nest` put a nest on oak leaves with 2 crows; after the kill 1 crow came back in 60 s and stayed at 1 (respawn is game-time gated, `randomTickSpeed` does not speed it); none with the bot 60 blocks away. Cap of 3 not reached in the window |
| SQ-HCS-06 | PASS | hand-placed nest: 0 crows after 60 s at random tick speed 300; summon 1, spawn egg 2 |
| SQ-HCS-11 | PASS | shapeless 2 straw, milling 3 straw from `hearthandharvest:nest` |
| SQ-NSM-06 | PASS | fresh dump: no `minecraft:air`, no moved recipe type (the `creativeengineering:baking` hits were the stale instance dump `creative_delight.json` copied into srv); `owner_namespace_move.py --left`: 0 files, 0 references |
| SQ-HCS-01 | PASS | Done in 87 s on a fresh normal world; `Exception generating new chunk` 0, `CrowEvents` 0, no crash report. The 15 `out of bound` hits are Dramatic Doors recipe parse warnings (BATCH-FAULT regex) |
| SQ-HCS-02 | PASS | 40 tp steps of 128 blocks through forest and plains by bot: no crow or nest exception; two `Can't keep up` (2.4 s, 29 s) during the chunk generation of the walk; JFR hot methods not yet read (perf report) |
| SQ-HCS-03 | FAIL (density) | Chunky 961 chunks around the nearest forest (only 105 are forest or old-growth birch forest): 5 nests, all on leaves, depth 0, none on the ground. 1 nest per 21 forest chunks against the expected 1 per 6. `nest_tree_top` = rarity 6, `in_square`, `MOTION_BLOCKING` heightmap: a column that is not a leaf top places nothing, so the 1 in 6 is a ceiling. Owner: raise the rate or accept |
| SQ-HCS-04 | PASS (approx.) | `/place structure` villages get no roof nests (it skips biome decoration, as the row warns). Natural village (Towns and Towers Mediterranean, brushland, -1296 240, Chunky r128): 6 nests at roof height, depth 0, about 30 houses (beds) = 1 in 5. One nest per house not checked block by block |
| SQ-HCS-08 | PASS | saved entities after Chunky: 16 `hearthandharvest:crow`, 0 `alexsmobs:crow`; `/summon alexsmobs:crow` works in a loaded chunk. JEI egg hiding is client |
| SQ-RDB-04 | PASS | `place feature` of the 4 configured features on brushland grass: 4 wild bell peppers, 2 cotton, 4 coffee, giant patch 1 red, 1 yellow, 1 green block, all on grass at the surface. Jungle temple and village chest rolls dropped 11 items each, seeds not inspected; trader not checked |
| SQ-WO-35 | PARTIAL | `execute if biome` is 1.19.3+, so the batch's tag tests are command errors (BATCH-FAULT). Offline census of the saved world (`tree_rarity = 1`, 1055 chunks): 131 Wise Oak blocks, 129 in tagged biomes (birch taiga 40, plains 27, cold shrubland 23, brushland 17, savanna plateau 15, forest 6, shield 1); 1 in `minecraft:beach`, 1 in `minecraft:snowy_plains`, neither in the tag, likely trees at a biome edge (the block's 4x4x4 biome cell). None in taiga, desert, ocean, river |
| SQ-WO-36 | PASS | taiga spot 1680 1392: oak sapling + lightning bolt, then `fill ... replace mysticaloaktree:wise_oak`: `Successfully filled 1 blocks` (taiga is not in the tag) |
| SQ-WO-27 | PASS (1 roll open) | `wo_judge.py gifts` against the instance toml (the srv toml holds the WO-34 edit by then): 39 gifts, 0 bad, all four lists (leaves 8, fruit saplings 12, fruit 10, saplings 9); `quark:ancient_sapling` is in the saplings list (allowed), no river, lemon or ancient-leaves id. Roll 28 (tree 14, second talk) returned CONSUME with no drop: the gift hook skips a tree that is not awake when clicked (asleep after the first talk is the likely case, not confirmed) |
| SQ-WO-32 | PASS | oak sapling x3 three times (1 stack), totem x3 seven times (3 stacks of 1); `Wise Oak gift list: unknown item nope:bad` once |
| SQ-WO-34 | BATCH-FAULT (again) | edit written in place 17:48:03, 15 s wait, `drops none`; `debug.log` has no Forge config-change notice for any file through 17:48:28 (same as r1110). Open: does Forge's watcher fire at all on this server? Next try: edit by hand on a running server and grep `debug.log` for `changed, sending notifies` |
| SQ-NSM-02 | PASS | 411 item ids, 29 blocks, 1 entity type remapped, 0 unresolved. Chests, offhand (patterned netherite shield), Curios feet (`creativeequipment:blue_stepstool`) and 8 ground items read back as `creativeequipment:` / `creativemagic:` / `creativecompat:` / `creativedelight:netherite_lunchbox`; shield patterns and lunchbox food kept; Dark Fire block is `creativemagic:dark_fire`. Stepstool lift is client-initiated (Stepstool.java), not judged here |
| SQ-NSM-03 | PASS | box `creativecompat:weathered_cardboard_box`; pot block entity `creativecompat:botanist_pot` with `Plant: minecraft:poppy`; jar water kept, cap key `creativecompat:jar_fluid_pipes`; nightstand items kept; 24 `creativecompat:shrapnel` per TNT; `creativecompat:misc/monster_box_reward` drops (cobweb, creeper head, raw gold) |
| SQ-NSM-05 | PARTIAL | lunchbox contents intact as `creativedelight:netherite_lunchbox`; `creativedelight:lunchbox/lunchbox_dye` and `smithing/lunchbox_skin` recipes loaded. Eating, the crafts and the model need a client |
| SQ-NSM-07 | PARTIAL | dimension half PASS: 11 `[dimension-move]` lines, `[legacy-ids]` dimension key line; nsmY logs in at its spot in `creativeworldgen:yellowstone` with the bed respawn there, nsmH in `creativeworldgen:hub`; `execute in creativeworldgen:yellowstone run say ok` works; after stop only `dimensions/creativeworldgen/`, level.dat names only `creativeworldgen:` dimensions; no Unknown structure / WorldGenSettings error. Not run: sky island, meta structure, waystone (not in the superflat fixture), the respawn walk (perfBot respawns by re-login), Xaero (client) |
| SQ-NSM-09 | PASS | `[creativelabels] config creative_labels-common.toml carried over` (and -client): new files hold 20 and 10.0, old files gone; no face_paper load error; notes keep text, colour, special and order (green spot 0 over red spot 2), erased note keeps its colour with no text; label keeps the diamond; chest reads `creativelabels:sticky_note` with `CreativeLabels.Text` and `creativelabels:eraser`. `/give` not run |
| SQ-NSM-10 | PASS (partial) | bags read `exposureextras:{leather,iron,netherite}_camera_bag` with the same `CameraBag` items and the iron bag's dye; `[exposureextras] config exposure_extras-common.toml carried over`, leatherSlots 6. Slot count on screen and tag acceptance not checked (client / hand). No `creativeengineering:iron_camera_bag` existed in 1.2.02 |
| SQ-HCS-09 | PASS | boot clean (no `CrowAccess`, no `crow_wariness` error); tamed AM crow keeps its Owner, carrier holds its gold nugget; the ground nest set `LastCrow` and a `hearthandharvest:crow` appeared at random tick speed 300 |
- 00:34 (2026-11-12) Session 86146d18 resumes step 5 (RUNNER.md claimed). SQ-HH Machines boot 1 on group A `hhm`, batch `batches/r1111/hh_mach.qb` (`fix_hh_mach.py` applies the r1108hh resume-point batch fixes). Done 10.0 s.
- 00:53 Machines batch done (qrun was killed once by the runner's own 590 s wrapper after SQ-HH-239; resumed --from 240: long batches run in the background, not under timeout). Worldgen group A rows `hh_wga.qb` on the same boot; namespace fixes in the batch (wild_flowers -> creativeworldgen, crop_seasons -> creativecalendar).
- 00:57 hh_wga done (SQ-HH-324, -325 boxes off the maze: rerun on the maze box, `hh_wga_fix.qb`). Boot 2 of hhm: Machines toml edits (SQ-HH-274), crow, zact and gear probes copied in. Runs hh_m2, hh_crow_d, hh_gear_a.
- 01:47 Crow D first pass: 38 FAIL, nearly all probe faults: `hh_crow_probe.js` dist() hit Rhino's Vec3 overload of distanceToSqr (every watch tick threw), goal names were Object.toString; batch regexes wanted `{"7":49}` (probe prints 49.0). Probe and batch fixed, probe reloaded on the live boot 2 (crows and items killed first), failed rows rerun.
- 02:24 Crow D rerun: 22 FAIL left (judge running). Boot 3 of hhm: Boot F crow and salt numbers (SQ-HH-466) and saltCaves, cornMaze false (SQ-HH-320) in one restart. Runs hh_b3, hh_crow_f, hh_gear_b, hh_mach_fix.
- 03:06 hhm boot 3 done (hh_b3, hh_crow_f, hh_mach_fix, hh_gear_b; judges running). hhm stopped. Fresh group B world `bw` (level hhbw, seed 8675309, spawn-animals, Loading Protection 1 s in srv only): bw.qb (SQ-VH-01..09, SQ-OB), hh_wgb.qb, hh_crow_b.qb.
- 03:29 bw: SQ-VH-09 fortress = betterfortresses:fortress (vanilla id redirects), located -944 -416 once the Async Locator thread was free of villager treasure-map locates; SQ-HH-464 rerun with a waitlog (Async Locator answers late). bw stopped.
- 03:05 Boot 3 of hhm CRASHED during SQ-HH-1574 (Quark seed pouch in a dispenser: StackOverflowError in Creative Delight hearth `gear/QuarkCompat.java:44`, the anonymous behaviour calls its own dispense). Rows -1575 on did not run on that boot; queued for hhm boot 4 (`hh_boot4.qb`, -1574 left out). Judged as a crash.
- 03:30 World wc (hhwc, BOP removed, salt toml rarity 1, -40..30): `vh12.qb`, `hh_wgc.qb` (generator had dropped the captures; written in with waitlogs), then `hh_wgb.qb` reruns (same seed; BOP only adds Nether content). Generator gap for the queue's write-back: `hh_queue_to_qb.py` drops every `capture` written as prose (qrun `capture ...` with an escaped pipe), and locates answer late through Async Locator, so every locate needs a waitlog before its capture.
- Verdict files per block: `perf/runs/r1111/verdicts/*.md` (judged by subagents from the row logs, checked by the runner).
- 03:52 hhm boot 4 CRASHED again at SQ-HH-1575 (seed pouch with cobblestone in a dispenser, same QuarkCompat recursion); both dispenser rows dropped. Boot 5 with `hh_boot5.qb` (fixtures cleared first). Gear probe: shod read fixed, hand read fixed, hurt via KubeJS attack.
- 04:00 SQ-VH-10 on vh10 (A + real TerraBlender) recorded. Structure Catalogue boot `scat` (level scat1, level-type creativedevtools:structure_catalogue), batch `scat.qb`.
- 04:06 SQ-SCAT done (all PASS, build 281 s). All scratch JVMs stopped; perf session r1111 stopped, report without `--prune`.

## 8. Step 5 verdicts (session 86146d18, 2026-11-12 00:32 to 04:10)

Full per-row tables: `perf/runs/r1111/verdicts/*.md` (one file per block; rows judged by subagents from `perf/runs/r1111/<block>/rows/`, checked by the runner). Reruns folded in (a batch fault fixed and rerun counts once, with the rerun's result).

| Block | Rows | PASS | FAIL | BATCH-FAULT | Other |
|---|---|---|---|---|---|
| SQ-HH Machines 201 to 273 | 73 | 67 | 1 (262, expected) | 5 (206, 209, 223, 233, 266) | |
| SQ-HH worldgen 301 to 329 (A, B, salt) | 26 | 22 | 2 (304 likely, 309) | 0 | 322 inconclusive, 328 open |
| SQ-HH Machines boot 2 / 3 (274 to 277, 318 to 320) | 7 | 5 | 0 | 2 (274, 320) | |
| SQ-HH Crow 401 to 502 | 102 | 70 | 4 (434, 440, 476 expected, 501) | 28 | |
| SQ-HH Gear 1500 to 1612 | 109 | 69 | 8 (1505 unconfirmed, 1507, 1546, 1547, 1548, 1574 crash, 1575 crash, 1593) | 32 | |
| SQ-VH | 12 | 9 | 0 | 1 (06) | 10 RECORD; 11 not run (no pre-change save) |
| SQ-OB server rows | 11 | 5 | 1 (05) | 5 (02, 03, 04, 06, 09) | 08 PASS pending owner (Nourishment); 11 not run (no old save with the buckets) |
| SQ-SCAT | 4 | 4 | 0 | 0 | |

Findings (TODO `largecontent/mod-additions #65` to `#73`, `dishes/ingredients #43`):
- **Server crash:** a Quark seed pouch in a dispenser (StackOverflowError, hearth `gear/QuarkCompat.java:44`). Crashed boots 3 and 4. #65.
- Crow: hand feeding off still eats corn (#66); tame crows never breed, no BreedGoal (#67); theft from a survival player not stashed (#68, untraced).
- Horseshoe: sneak-use does not unshoe; shod horse hurt by cactus (unconfirmed) (#69).
- Tilled end stone dict key collision (not pickaxe-mineable); hops not planted on an empty lattice (#70).
- Mums missing from the `minecraft:small_flowers` item tag; wild hops never generate naturally (#71).
- Duplicate syrup to sugar recipe (#72).
- Cave carrot veggie patty cutting recipes output air (SQ-OB-05, `dishes/ingredients #43`).
- Owner questions (#73): basin takes any item, farmland under hops dries, nest not Clearable, watering can on a ripe crop harvests, salt near forest.

Tooling fixed on the way: `hh_crow_probe.js` (distance overload, goal names), `hh_gear_probe.js` (hand NBT, Forge persistent data, hurt via KubeJS attack); batches written out in `batches/r1111/` (`fix_hh_*.py`, `split_hh_m2wg.py`, hand-written `bw.qb`, `vh12.qb`, `scat*.qb`). Generator gaps for the next SQ-HH run: `hh_queue_to_qb.py` drops prose `capture` lines and prose fixture steps ("hold X", "sneak", "N times", placeholders); every Async Locator locate needs a waitlog before its capture. Queue rows with stale ids or text (batch faults above) are listed per row in the verdict files, not yet rewritten in the queue.

Not run: every client row (CQ-); SQ-VH-11, SQ-OB-11 (no suitable old save); SQ-FDI (Spice of Life: Carrot Edition jar not in `mods/`); SQ-FLR-02 to -05 (Create rig); SQ-BREED-01 (`/dumpbreeding` not written); the SQ-HH batch-fault reruns listed in the verdict files.
