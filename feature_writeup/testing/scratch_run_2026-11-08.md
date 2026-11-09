# Scratch runs 2026-11-08

## 1. r1108cge: Creative Common 1.1.0 of 11-08 00:01 (SQ-CGE, with the re-run list's server rows)

Owner request 2026-11-08 00:10: "execute the latest creative common scratch tests. after (for common) notes have been
taken and bugs that dont require a decision fixed, give a list of concerns to consider". Scope: queue section "SQ-CGE"
server rows SQ-CGE-01 to -06 and the server rows of its re-run list (SQ-CPP-01 to -05, SQ-CMV-04). Not run: CQ-CGE-01
and the other client rows of the re-run list (need a window), SQ-CC-12 (needs the owner client on CreEngServer).
Targeted run, no perf session (JFR on per boot, recordings in each srv's perf/).

- Jars: Common `creativecommon-1.1.0` 11-08 00:01 (then the rebuilds below), the rest from `mods/` or newer build/libs
  (prep_srv.sh swaps: Delight 11-07 21:39, Brewing / Calendar 20:00, Compat 23:10 = `mods/`).
- Probe: new `tools/scratch_query/cge_probe.js` (`/zgv drop|watch|cfg|reload`: tick-exact fall distances of a falling
  block, an item and lit TNT; Create Gravity's own biome lookup). Batches `tools/scratch_query/batches/r1108cge/`.
- b1: srv `b1/` in the session scratchpad, level r1108cge, group A; left out loadingscreentips (as r1107cc), kept
  Immersive Portals and Portal Gun.

### Run notes

- 00:14 claim, preflight: no JVM, 60 GB free; Common 00:01 build newer than every Common source file.
- 00:14 read Create Gravity 1.2.0 (javap): `ServerConfig.BIOME_CONFIGS` is built only by `reloadConfig()`, called from
  `ForgeEvents.reloadConfiguration(TagsUpdatedEvent)` (returns while `ServerConfig.SPEC` is not loaded) and on a config
  file edit (`ModConfigEvent.Reloading`), never on `Loading`. `getBiomeConfig` returns null while the list is null.
  Suspect: on a dedicated server the first TagsUpdatedEvent fires before the server config loads, so nothing gets low
  gravity until a `/reload` (fits SQ-CCO-08's inert End; singleplayer gets a client-side TagsUpdatedEvent after the
  world loads). b1 checks it before and after a `/reload`.
- 00:17 b1 CRASHED in mod loading: `startup_scripts:jei/order.js#30: Cannot call method "define" of undefined`.
  Cause: the JEI order helper moved from Common to Compat at 11-07 19:59 (SQ-CCB), but Common's `JarStartupScripts`
  runs only Common's pack before `kubejs/startup_scripts/`; Compat's pack ran after the pack's `jei/order.js`. The
  Compat jar in `mods/` (23:10) has the same file, so the owner's next full-pack launch would crash the same way.
  FIXED (Common): `JarStartupScripts` puts Common's and then Compat's pack before `startup_scripts` (the order
  Compat's `jei_order.js` header already states). Common rebuilt 00:23.
- 00:24 b1 reboot: script order `creativecommon, creativecompat, startup_scripts, ...`, `[jeiOrder] defined 43 nodes`.
  World creation FAILED: `Error loading registry data: Value provider too high: 256 [312-471]`. Cause (not Common):
  `kubejs/data/minecraft/worldgen/placed_feature/glow_lichen.json` (farming batch A8, TODO `flowers #4`, 11-07 21:26)
  sets the lichen count to uniform 312-471; a count provider allows at most 256. FIXED (no decision: same 3x): vanilla
  uniform 104-157 followed by a second `count` 3 (counts multiply). The server hung after the failure (mine, stopped).
- 00:37 b1 Done 10.3 s (Common 00:23, lichen fix copied into srv). Batch b1: probe faults (Rhino `const` inside `try`
  "redeclaration of var server"; `dimension()` ambiguous; `getType()` is a string in KubeJS): probe fixed (var,
  `getDimension()`), rows rerun. A manual `/reload` during the fix spoiled the "before any reload" state of this boot:
  that check moved to b2.
- 00:45 b1 after /reload: End drop 6.162 vs overworld 24.570 at 40 ticks (25 percent), villager End 0.016 / overworld
  0.08; SQ-CPP-01 to -03 PASS (tags, sequenced assembly keeps enchantments and name, IE selftest 0 fail, bottling and
  refinery). SQ-CGE-04: resting item and TNT stay at 61.000, falling sand places (a stray sand item at 62.57 came from
  the untracked drops of the probe-fault runs; a fresh tossed item rises to 62.4 and rests at 61.000).
- 00:47 b2 (same world, `biome_configs = ["#minecraft:is_end;;-1", "minecraft:plains;;"]`), no /reload: CONFIRMED
  `getBiomeConfig` null in the End, End drop 24.570 (vanilla), villager 0.08. After /reload: factor -1 still sank 1.56
  blocks in 40 ticks (vy 0 after each tick, 0.04 a tick): the mixin added the pull back at TAIL, after the move.
- 00:49 FIXED (Common): `CreateGravityEntitiesMixin` at HEAD with PULL 0.04 (the move of the same tick uses the reduced
  pull); `CreateGravityDimensionMixin` also injects at the TAIL of `ForgeEvents.setServer` (ServerStartedEvent) and
  calls `ServerConfig.reloadConfig()` when the spec is loaded. Common rebuilt 00:49.
- 00:51 b3 (Common 00:49, -1 config, no /reload): factor -1 from the start, End drop 0.000 for all three, plains
  4.914 (20.0 percent of 24.570). A live toml edit back to the default was not picked up within 6 s; Forge also writes
  the in-memory config back on shutdown (my first b4 boot still had -1). b4 with the default set while stopped:
  End 4.914 vs overworld 24.570, sand places, tossed item rests at 61.000, End villager 0.016, overworld 0.08.
- 00:58 b5 `cc_only_srv.sh full` (Common 00:49 + optional mods only): SQ-CMV-04 PASS (generated config: advancements
  on, trader off 24000 / 5 / 1, packOrder false, creative menu not disabled; grant succeeds). SQ-CGE-06: no /reload,
  zombie, villager, survival bot 0.016 in the End, falling objects 4.914 vs 24.570; again with
  `gravityLimitDimensions = false`: same. Batch faults fixed: peaceful server removed the zombie (`difficulty normal`),
  1.19.2 has no `execute if biome`, mobs at 0 70 0 sit inside the End island and suffocate (y 75), `--rows` skipped
  the forceload of an earlier row. The low-gravity modifier is transient (not in the saved Attributes): the value is
  the check.
- 01:06 b6 (new `tools/scratch_query/cpp_chain_srv.sh OUT cpp4 chain`, from the pack's jars; CEPortTesting holds the
  Common-only client set and was left alone): SQ-CPP-04 PASS, 0 recipe parse errors, REGULAR swiftness bottles to a
  Swiftness potion without Engineering. Only KubeJS ERROR: the scratch-only `zz_cl_probe.js` (Creative Labels probe,
  Labels not in the set). r1107cc's 23 Delight / Brewing errors on its chain set did not appear (that set used
  CEPortTesting's kubejs/ and Delight 17:03; this one Delight 21:39 and an empty kubejs/): TODO
  `creativebrewing/publish #24` needs a recheck, not closed here.
- 01:08 b7 (`cpp_chain_srv.sh OUT cpp5 compat`: Common, Compat, Calendar, Serene Seasons): SQ-CPP-05 PASS, no scrap
  error, 0 KubeJS errors, `creativeengineering:physics/floats` 462 entries; script order creativecommon, creativecompat,
  startup_scripts, creativecalendar.
- 01:09 all JVMs stopped.

### Verdicts (r1108cge, server rows)

| ID | Result | Evidence |
|---|---|---|
| SQ-CGE-01 | PASS | b1 to b5: no mixin apply error, no WARN naming either Create Gravity mixin |
| SQ-CGE-02 | PASS (after 2 fixes) | 00:01 jar: inert until /reload on a dedicated server (Create Gravity's own bug), then 25 percent; 00:49 jar, no /reload: End 4.914 vs overworld 24.570 at 40 ticks for falling block, item and TNT |
| SQ-CGE-03 | PASS | item and TNT lines identical to the falling block in every drop |
| SQ-CGE-04 | PASS | item and TNT rest at 61.000 (120 ticks), falling sand places at 0 61 0, a tossed item rests at 61.000 |
| SQ-CGE-05 | PASS (00:49) | factor -1: drop 0.000 (00:01 jar: sank 0.04 a tick); plains listed -0.8: 4.914; unlisted biome (overworld default config): vanilla 24.570. Desert half: superflat has no desert, the unlisted path is the same null branch |
| SQ-CGE-06 | PASS | zombie, villager, survival bot 0.016 in the End without /reload, gate on and off; falling objects as SQ-CGE-02. Modifier name not checkable (transient) |
| SQ-CPP-01 | PASS | no mixin error; tag failures only creategoggles' Mekanism tag; metal_dusts 15 (IE dusts, zinc grit), bark 20 (IW); config holds the 3 removed effects and the name list |
| SQ-CPP-02 | PASS | echo shovel keeps Unbreaking III and the name over 6 steps; IE selftest 1952 pass, 0 fail |
| SQ-CPP-03 | PASS (recipe walk) | bottling REGULAR swiftness -> Swiftness potion; refinery splash + dragon's breath -> LINGERING swiftness |
| SQ-CPP-04 | PASS | chain without Engineering: 0 parse errors, bottling works |
| SQ-CPP-05 | PASS | Common + Compat without Create / Create Recycle: no scrap error, 0 KubeJS errors |
| SQ-CMV-04 | PASS | generated defaults as the row lists; advancement grant succeeds |
| CQ-CGE-01, client re-run rows, SQ-CC-12 | not run | need a window / the owner client |

Fixes this run: Common `JarStartupScripts` (Compat's pack before `startup_scripts`), `CreateGravityDimensionMixin`
(biome list at server start), `CreateGravityEntitiesMixin` (HEAD); pack `placed_feature/glow_lichen.json` (count max
256). Changelog: one Fixed bullet (End low gravity missing on dedicated servers after a restart). Tooling:
`cge_probe.js`, `cpp_chain_srv.sh`, batches `r1108cge/` b1 to b7.

## 2. r1108cge2: Creative Common 1.1.0 of 11-08 13:51 (SQ-CGE part 2)

Owner request 2026-11-08: "execute the latest creative common tests". Scope: queue section "SQ-CGE part 2", server
rows on four boots (b1 to b4, `tools/scratch_query/cge2_run.sh`), then client blocks c1 and c2. Targeted run, no perf
session (JFR per boot as prep_srv sets it). Jars: Common 13:51 and Compat 13:21 copied from build/libs into each srv.
Batches `tools/scratch_query/batches/r1108cge2/`, results `perf/runs/r1108cge2/`.

### Run notes

- 13:59 claim, preflight: no JVM, 60 GB free; build/libs Common 13:51, Compat 13:21 (`mods/` holds 01:10 and 11-07 23:10).
- 14:00 first start stopped at the runner's jar check: `grep -q` under pipefail kills unzip (SIGPIPE) and reads as "not found"; check fixed in `cge2_run.sh` (captures the listing first).
- 14:00 to 14:07 b1 to b4 done. Fall rows: every type at the expected ratio except `item` in the full pack (24.570 = vanilla, b1, b2, b3); Common alone (b4, no ItemPhysic) 4.914. Cause: ItemPhysic's `updatePre` adds its own -0.04 to items, outside `LowGravityPullMixin`'s constant.
- 14:10 batch faults fixed: Nether column cleared from x 0 wedged the 1.375-wide boat in x -1 (cleared from x -3, chunks -1 force loaded); SQ-CGE-10 expected Air -10 in 8 s, but Create Gravity 1.2.0 takes 4 air every `oxygen_factor` (100) ticks, 390 s to -10; a rerun bot reloaded its saved backtank (row now clears the slot); `cge2_run.sh client` now resets the world's biome list (b2, b3 left End -1).
- 14:21 reruns of SQ-CGE-14, -09a, -09b, -10, -10b done (4 more boots of the b1 srv). SQ-CGE-10 log: 300 to 288 in 12 s (3 steps of 4); its stored FAIL is the older regex, corrected in the batch.
- 14:22 client c1 launched; my world-open helper waited on a log line this client never writes (helper deleted; the owner opened the world by hand).
- 14:32 first c1 pass invalid: `qrun.py` client typing leaked the opening `t` into chat (`t/execute ... fill` sent as chat) and lost trailing characters, so the End platform was not placed and the player fell twice. Fixed in `qrun.py` (longer wait, clears the chat line first, longer pause before Return); CQ-CGE-04 reordered (creative, End, wait, platform, survival).
- 14:40 second c1 pass: no leaks. 14:44:27 `out_of_oxygen` damage 6.0 after about 290 s in the End, 14:44:51 death "ran out of air". Owner watched it: oxygen drain works correctly. Fixed 400 s wait overshot, so the batch was stopped there and the row now waits on the log; backtank half split into CQ-CGE-04b (330 s in survival).
- 14:55 owner hastened CQ-CGE-04b with a nearly empty backtank in survival in the End: pressure reached 0 and the display swapped to the bubble oxygen level; bubbles fell from 10 to about 7 in 90 s (screenshots). Owner: backtank and End air tests complete. CQ-CGE-04 and -04b PASS on the owner's observation (qrun's FAIL on 04b is the Health read colliding with the owner's chat use).
- 14:57 CQ-CGE-02 and -02c rerun: the first shots were unusable (camera sank to y 55, summons typed over 35 s, the overworld row had no dimension); now one datapack function drops everything, camera on glass.
- 15:00 CQ-CGE-02 third pass: camera held at y 112, all commands ran. Stills inconclusive: at the first shot the objects sit low in the frame and by the next shot (about 1 s) they are out of view, which does not match the slow descent expected. Asked the owner to watch `/function cge2:drop_end` live. CQ-CGE-02c shots also show no objects (a vanilla fall leaves the frame before the first shot); SQ-CGE-08 covers the overworld numbers.
- 15:05 CQ-CGE-02 PASS: owner ran `/function cge2:drop_end` live, every entity falls slowly.
- 15:08 owner asked for both findings to be fixed. Common: new `LowGravityItemPhysicMixin` (scales ItemPhysic's own -0.04 in `ItemPhysicServer.updatePre`; gated on ItemPhysic and Create Gravity). Compat: removed `data/creativeengineering/tags/items/physics/` (four files; nothing reads them, the new tags already held all but bucket and snowball). Common and Compat rebuilt 15:08. Rerunning every server row from a fresh world, with new boot b5 (SQ-CGE-16: ItemPhysic without Create Gravity).
- 15:09 to 15:18 all server rows rerun from a fresh world on Common and Compat of 15:08 (b1 to b5). All PASS. One runner stop before b5: the b4 JVM outlived `stop_wait.sh` by a few seconds (runner now waits; b5 run with `cge2_run.sh b5`).
- 15:20 CQ-CGE-03a to -03c not run: they need a client and a dedicated server up together, and the project's rule is one game JVM at a time with no exceptions (both `cge2_run.sh join` and `eval_client.py launch` refuse). Handed to the owner.

### Verdicts (r1108cge2)

Server rows on Common and Compat of 15:08. Client rows CQ-CGE-02, -02a, -02b, -02c, -04, -04b ran on Common 13:51 and Compat 13:21, before the ItemPhysic patch.

| ID | Result | Evidence |
|---|---|---|
| SQ-CGE-07 | PASS | no mixin error in any boot; helper order line, `1 helper file(s)`, `[jeiOrder] defined`, 0 startup errors |
| SQ-CGE-08 | PASS (after the ItemPhysic patch) | all 9 types ratio 0.200, item 4.914 vs 24.570; before the patch the item was 24.570 in the End |
| SQ-CGE-13 | PASS | boats at 65.523 in both tubs |
| SQ-CGE-10 | PASS | Air 300 then 288 after 12 s (row's old target of -10 in 8 s was wrong) |
| SQ-CGE-10b | PASS | Air back to 300, tank 890 of 900; tags backtanks 7, diving_helmets 4 |
| SQ-CGE-11 | PASS | overworld Air 300, gravity 0.08 |
| SQ-CGE-12 | PASS (after removing the old tag files) | new floats 1067, sinks 8, fireproof 397, lavaproof 578; all four old tags 0; log 65.900, redstone 61.000 |
| SQ-CGE-14 | PASS (after the patch) | every type 0.000 in the End at factor -1 |
| SQ-CGE-09a | PASS | Nether factor -0.8 printed, all 9 types = overworld, villager 0.08 |
| SQ-CGE-09b | PASS (after the patch) | all 9 types 0.200 in the Nether, villager 0.016 |
| SQ-CGE-15 | PASS | no helper pack, 0 startup errors, 9 types low (no ItemPhysic in this set), backtanks 2 |
| SQ-CGE-16 | PASS | ItemPhysic without Create Gravity: boots, no LowGravity error, all 9 types vanilla in the End |
| CQ-CGE-02a | PASS | clean client boot, no mixin error |
| CQ-CGE-02 | PASS (owner, live) | owner ran `/function cge2:drop_end`: every entity falls slowly; pre-patch jars |
| CQ-CGE-02b | PASS | `[low gravity] client biome list: 1 of 1 entries [#minecraft:is_end;;]` |
| CQ-CGE-02c | not judged | shots show no objects (a vanilla fall leaves the frame first); SQ-CGE-08 has the overworld numbers |
| CQ-CGE-04 | PASS (owner, live) | `out_of_oxygen` damage 6.0 at about 290 s, death "ran out of air"; owner: drain correct |
| CQ-CGE-04b | PASS (owner, live) | owner: pressure reached 0 and the display swapped to bubbles; bubbles 10 to about 7 in 90 s after that |
| CQ-CGE-03a, -03, -03b, -03c | not run | need two game JVMs (rule above); owner to run |

Fixes this run: Common `LowGravityItemPhysicMixin` (ItemPhysic's own pull scaled; applies only with ItemPhysic and Create Gravity), `CommonMixinPlugin` gate and version range; Compat `data/creativeengineering/tags/items/physics/` removed. The old tags held two items the new tags do not (bucket, snowball); nothing read the old tags, so nothing changes. Changelog: no new bullet; the unreleased low gravity bullet already says dropped items are covered. Tooling: `qrun.py` client typing, `cge2_run.sh` (jar check, client config reset, datapack, JVM wait, `b5`), datapack `tools/scratch_query/datapack/cge2_drop`, batch `b5_itemphysic_no_gravity.qb`. Results `perf/runs/r1108cge2/`.

## 3. r1108jg: Creative Compat JEI gate, JEI order skip, cycling page split (CQ-JG)

Owner request 2026-11-08: "build and run these scratch tests". Targeted run, two throwaway clients one after the other (no perf session). Compat of 15:49 built and installed to `mods/` (and CEPortTesting afterwards). Client logs kept in the session scratchpad (`jg02_client.log`, `jg01_client.log`).

- 15:52 CQ-JG-02: full pack client (b1 world copy, JEI 11.8.1.1034). `[jei order] definition: 4 types, 43 nodes` through `JeiOrderBridge`, `[jei cycle] 528 crafting pages`, 0 `could not hand the JEI order`. 2 `broken recipe` lines remain, both Just Enough Resources' villager page (`VillagersHelper.getPoiBlocks`, `Optional.get`), a third-party mod; 0 from Creative Compat's cycling pages (12 before).
- 15:55 CQ-JG-01: minimal client (Common 15:08 build, Compat 15:49, JEI 11.80.0.1094, no pack scripts). One `outside the tested range` line from the mixin plugin and one from `JeiGate`; `[jei_order] no order defined`; 0 `broken recipe`; 0 `could not hand the JEI order`; 0 mixin failure; JEI started in 1.6 s.

| ID | Result | Evidence |
|---|---|---|
| CQ-JG-01 | PASS | as above |
| CQ-JG-02 | PASS for Creative Compat (2 third-party JER errors left) | 0 cycling `broken recipe`, order handed over, pages built; page layout names and counts not read from the log |
| CQ-JG-03 | not run | needs hand work in the JEI window (press R / U on a cycling page); owner |


## 4. Owner client, CEPortTesting minimum set (Common 17:57, 14 jars; run 18:00 to 18:12)

Owner report: tip bar shows on world load, no creative menu, no toasts, WTHIT works, JEI tabs work, Immersive Portals works.
Log: `Loading tips: 335 tips from 2 files`, one Heartstone priority line, no `Mixin apply failed`.

| ID | Result | Left |
|---|---|---|
| CQ-CC-01 | PARTIAL | launch PASS; Mods screen description not reported |
| CQ-CC-02 | PARTIAL | tip bar and count PASS; 7 s roll, Resource Packs list, custom tip not reported |
| CQ-CMV-03 | PARTIAL | creative E and toasts PASS; panorama not reported; JEI order needs the full pack |
| CQ-CC-06 | PARTIAL | WTHIT and JEI PASS; changelog viewer not reported |

Log triage (79 ERROR, 570 WARN):

- Fixed, Common: 60 `Failed to create recipe for type 'create:splashing'` plus 60 WARN. The Create helpers in NCH use KubeJS
  Create's schema; with Create but no KubeJS Create they failed per row. Now skipped and counted for `kubejs_create` in the
  `[absent mods]` line (same as `ieMetalPress`).
- Fixed, Compat: 10 `[NCH] ... output: is undefined/null` (copper vertical slabs need Quark, soap needs Supplementaries);
  rows now guarded with `global.has`.
- Fixed, Compat: flower box extra models registered with their owners absent (2 missing textures, `cross_template`,
  `#missingno`); now registered only with Supplementaries + Quark (berries) or Beautify (lichen).
- Test set only: about 240 `[mod names] X is not loaded` WARN from copying the pack's config; CEPortTesting's list is now
  empty, the published default. `Removed resource pack` lines (stale options.txt), first-run `Incorrect key` config lines.
- Third-party, left: Immersive Portals (5 `minVersion` ERROR, duplicate codec ERROR, `nether_portal_block` missing variant,
  `Received passengers for unknown entity`), KubeJS `Failed to get filter section from pack kubejs` (also in the full pack; its
  pack has no readable `pack.mcmeta`, vanilla then applies no filter), SecurityCraft, Heartstone refmap, vanilla sound lines.

## 5. Owner client, CEPortTesting, second launch (Common and Compat with the log cleanup, 18:29 to 18:38)

Owner report: world loads; Mods screen has the gravity description and the repo link works; changelog viewer works and shows
every version's changelog (accepted); tips roll every 7 s; flint and steel makes mirrors on tinted and reinforced tinted
glass, not on a framed cube with tinted glass (correct, owner: framed and copycat blocks are never mirrors).
Panorama: blank on the title screen with the images in the jar; owner accepted it and closed the check, cause not found.

| ID | Result | Left |
|---|---|---|
| CQ-CLOG-01 | PASS | 8 ERROR: the expected 7 plus one Immersive Portals `Null RenderChunk` on the Nether's client world creation (`MyBuiltChunkStorage`); `[absent mods]` names `kubejs_create` (32 rows); no mod names or flower box lines |
| CQ-CC-01, CQ-CMV-02 | PASS | |
| CQ-CMV-03 | PASS for Common (panorama closed by owner) | JEI order needs the full pack |
| CQ-CC-06 | PASS | |
| CQ-CC-02 | PARTIAL | Resource Packs list and custom tip after F3+T not reported |
| CQ-CC-05 | PARTIAL | framed pane and framed bars not yet tried |

Static finding: Immersive Portals' mirror check accepts every `IronBarsBlock`, and Framed Blocks' `FramedPaneBlock` extends it, so
framed panes and bars would have become mirrors. Fixed: new tag `creativecompat:not_mirror_glass` (`IPMirrorGlassMixin`).
Copycats+ has no such block, and Create's copycat bars class is not an `IronBarsBlock` subclass in the jar scan.

CEPortTesting config set for the next launch: `[mod_names] overrides = ["create=Create Test"]`, `[loading_tips] cycleSeconds = 2`.

## 6. Owner client, CEPortTesting, third launch (18:48 on)

CQ-CC-05 PASS (framed panes and bars make no mirror). CQ-CC-04 first half PASS (Create items read "Create Test" in all three screens). Owner saw the tips roll faster at the small GUI scale: not the scale, CEPortTesting had `cycleSeconds = 2` set for CQ-LT-04. Owner: default 10 s, per client; moved to `creativecommon-client.toml` (CQ-LTC-01). CQ-LT-04's wrap result not reported. Test tips for CQ-CC-02 added to CEPortTesting only (`creative_tips/.../tips.json` line and `kubejs/assets/zz_tip_test/`); remove after the check.

## 7. Last launch (19:09 to 19:2x)

CQ-LTC-01, CQ-CC-04 (second half), CQ-SPFX-02 PASS (log: no Heartstone line, no mixin failure). CQ-LT-06 PASS (owner: no tip bar with `enabled = false` in `creativecommon-client.toml`). Panorama: the six jar images are a terrain-free sky capture (contact sheet: flat blue, hazy ground, sun), hence blank; removed from Common's jar, vanilla by default.
