# Scratch run 2026-11-06

## 1. r1106cba: Creative Brewing chain on its own (SQ-CBA)

Owner request 2026-11-06 10:10: "execute the creative brewing port test scratch tests". Scope: queue section
"SQ-CBA Creative Brewing chain on its own (CEPortTesting)", SQ-CBA-01 to -18 (server) and CQ-CBA-01 to -04 (client,
CEPortTesting through ATLauncher). Targeted run: no perf session.

- Jars: copied from `build/libs` into `CEPortTesting/mods/` at 10:12 (creativecommon-1.1.0 10:02, creativecompat-1.0.0
  01:31, creativecalendar-1.0.0 10:03, creativedelight-1.1.0 10:03, creativebrewing-1.0.0 10:03, MoreJS mandatory in its
  mods.toml). `port_chain.sh check` clean. Disk 11 GB free at the start: each OUT deleted after its rows.
- Batches: `tools/scratch_query/batches/r1106cba/b1.qb` to `b6.qb`, one per boot (queue intro's boot list).
- OUT: scratchpad `r1106cba/oN`; results: `perf/runs/r1106cba/`.

### Run notes

- 10:15 preflight done (no game JVM, RUNNER claimed, jars copied).
- 10:12 boot 1 (chain, A, `o1`) Done in 5.6 s. b1: PASS 6, FAIL 4 (02, 07, 09, 10).
- 10:16 SQ-CBA-07 BATCH-FAULT: no player, so entities stop ticking 300 ticks after start (golems stayed 100.0). Added
  `forceload add 0 0` to the row; rerun PASS (95.2 / 94.0 / 95.2).
- 10:18-10:21 SQ-CBA-09: KubeJS "removed 3823 recipes" on the chain (full pack 11-05: 1329). Turned on
  `local/kubejs/dev.properties` `logRemovedRecipes=true` (read at startup only, /reload does not pick it up) and the
  srv-only wrapper `tools/scratch_query/nch_remove_diag.js` (logs every `global.NCH.remove` filter). One call,
  `{output: undefined}`, matched everything: `kubejs/data/creativedelight/kubejs/veggiesdelight/recipes.js:110`
  `R.remove({ output: fd.dish_stuffed_bellpepper }, e)`; `fd.dish_stuffed_bellpepper` is not in food_dict.js. New in the
  11-06 split commit, inside the CD jar installed in the pack 01:30; the pack has not booted since (last KubeJS log 11-05
  22:39), so the full pack is hit too. Not fixed (behaviour change, owner).
- 10:23 decision: the wrapper now SKIPS top-level undefined/null filter values (srv-only shim) so the CB rows test the
  rest: removed 3823 -> 312. SQ-CBA-09 rerun: keg pouring 24, wine fermentation 26, apple mashing 2 (pack 24/27/2);
  fermenting 17 (pack 21): kombucha (needs farmersrespite, create_central_kitchen; expected), kippers (removed by CD's
  `{input: miners_delight:tentacles}`, matches `#forge:raw_fishes`), `creativebrewing:fermenting/pale_jane_from_rice_wine`
  and `steel_toe_stout_from_strongroot_ale` not added: `#immersive_weathering:bark` and `#creativeengineering:metal_dusts`
  are empty in the chain (no NCH message), and the upstream recipes are removed, so neither drink is brewable on the chain.
- 10:23 SQ-CBA-10 BATCH-FAULT: 42 of 44 is right for the chain (bellini_cocktail is Fruits Delight, bottle_of_wodka is
  tmted, both absent); row expectation changed to 42 for the chain; rerun PASS.
- SQ-CBA-02 FAIL (noise): 7 `Attempted to load class net/minecraft/client/...` lines (AbstractClientPlayer x4,
  PoseStack x2, ClientLevel) during CD's `comfort_nourishment.js` (Rhino member scan over every item's class); not CB code,
  boot continues; earlier full-pack servers logged ClientLevel the same way (r1031 SQ-KT-21).

- 10:25-10:28 boot 2 (`vinery = false`) and boot 3 (`brewinandchewin = false`) on `o1`, shim still in. SQ-CBA-11 first
  FAILed as BATCH-FAULT: the startup `core finished, members:` line lists the members whatever their switch; the row now
  checks `server scripts ready, members on: brewinandchewin` (server.log). Both PASS.
- 10:29 boot 4 (`o4`, no vinery_upstream): stops with the missing-jar message and both URLs; PASS. The JVM stayed alive
  after the FML stop (gotcha), killed by pid (cwd in the scratchpad).
- 10:31 boot 5 (`o5`, KubeJS Delight, Serene Seasons, MoreJS dropped): FML lists morejs for creativebrewing, sereneseasons
  for creativecalendar, kubejsdelight for creativedelight; PASS.
- 10:33 boot 6 (`o6`, instance, B): `Failed to load datapacks`, `Unknown registry key ... structure_placement:
  betterjungletemples:jungle_temple` from Creative Worldgen's structure_config.json (TODO `creativebrewing/publish #5`;
  CEPortTesting's client hit it at 01:34). Decision: rebuilt `o6` with `DROP="creativeworldgen-*.jar"` (CB does not depend
  on it). 10:47 Done in 26.8 s; the shim skipped the same undefined remove.
- 10:48 b6: SQ-CBA-15 FAIL only on the client-class regex (19 lines: Quark Foxhound module, CD `toast.js` and the scans
  after `golden_values.js`, the end of CD's server scripts; none during a creativebrewing script); its other checks pass
  (order, core line, no script error, only `lava_vision.js` skipped). -16: 36 item-agnostic mixes, as the chain; brews as
  SQ-CBA-06: PASS. -17: winemaker 34 (chain 21): PASS. -18: no `[absent mods]` line at all and no CB ERROR: PASS on the
  row's terms (the list is empty, so nothing wrong is named); `[alcohol]` 43 of 44 here.
- 10:50 all JVMs stopped, OUT dirs deleted. Client rows CQ-CBA-01 to -04 not run: they run in CEPortTesting through
  ATLauncher (owner), and -04 (instance set) cannot create a world until `creativebrewing/publish #5` is fixed.

### Verdicts

| ID | Result | Evidence |
|---|---|---|
| SQ-CBA-01 | PASS | `[creativebrewing] members: [vinery, brewinandchewin]`, no load error |
| SQ-CBA-02 | FAIL (noise) | 7 client-class DISTXFORM lines during CD `comfort_nourishment.js` (Rhino scan); none from CB; boot fine |
| SQ-CBA-03 | PASS | order creativedelight before creativebrewing; only `alexsmobs/lava_vision.js` skipped |
| SQ-CBA-04 | PASS | five tables, both members true, no carry WARN |
| SQ-CBA-05 | PASS | 6 registered, long_reach false, Better Beacons skip line |
| SQ-CBA-06 | PASS | 36 mixes; wither, magic_resistance, long and strong brews |
| SQ-CBA-07 | PASS (rerun) | 95.2 / 94.0 / 95.2 after `forceload add 0 0` (first run BATCH-FAULT: no ticking) |
| SQ-CBA-08 | PASS | area effect cloud above the dispenser |
| SQ-CBA-09 | FAIL | undefined-output remove in CD Veggies (`creativedelight/framework #15`); with shim fermenting 17 of 21 (`creativebrewing/publish #4`) |
| SQ-CBA-10 | PASS (rerun) | 42 of 44 (bellini, wodka absent; row fixed), beer has tipsy, winemaker 21, summoned |
| SQ-CBA-11 | PASS (rerun) | `members on: brewinandchewin`, no winemaker trades, wine fermentation 26 |
| SQ-CBA-12 | PASS | `members on: vinery`, fermenting 21 |
| SQ-CBA-13 | PASS | missing letsdo-vinery jars named with URLs, no `Done (` |
| SQ-CBA-14 | PASS | morejs, sereneseasons, kubejsdelight listed by the right requester |
| SQ-CBA-15 | FAIL (noise) | client-class lines from Quark and CD only; other checks pass (Worldgen jar dropped) |
| SQ-CBA-16 | PASS | 36 mixes (chain 36), same brews |
| SQ-CBA-17 | PASS | winemaker 34 (chain 21) |
| SQ-CBA-18 | PASS | no `[absent mods]` line, no CB ERROR |
| CQ-CBA-01 to -04 | not run | owner, CEPortTesting through ATLauncher; -04 blocked by `creativebrewing/publish #5` |

## 2. Open issues before round 2 (SQ-CBB), owner request 2026-11-06

Owner request after r1106cba: move tags like bark and metal dust into Common, reject undefined remove filters, add the
missing food dict key, put the non-repacked cooking mods into the port test and make them optional dependencies of
Creative Delight, gate YUNG's structures, then break down the other open issues before another scratch round.

### 2.1 Done in this session (built, not installed)

| Item | Change | Rows |
|---|---|---|
| Undefined remove filters | `global.NCH.remove` refuses a filter that is or holds undefined/null at any depth (`_undefinedIn`), logged as `[NCH] remove failed and was skipped: remove refused: <path>` | SQ-CBB-01, -02, -12 |
| Missing food dict key | `f.dish_stuffed_bellpepper = i.stuffed_bellpepper` in Common's `food_dict.js` (the dict was already in Common with the other dicts; CD only reads it) | SQ-CBB-01 |
| Cooking mods optional | 11 `mandatory=false`, `ordering="AFTER"` entries in CD's `mods.toml` (abnormals_delight, alexsdelight, corn_delight, create_central_kitchen, endersdelight, farmersrespite, fruitsdelight, hostiledelight, moredelight, quarkdelight, tmted; cookingforblockheads was already there) | SQ-CBB-06 to -09 |
| Cooking set in the port test | `port_chain.sh` `COOKING` list and `cooking` mode / `bare cooking`; the jars and their required mods (Blueprint, Alex's Mobs, Citadel, Balm, Mysterious Mountain Lib, Immersive Engineering) copied into CEPortTesting/mods | SQ-CBB-06 to -09 |
| YUNG's structures gate | `StructurePresence` (Creative Worldgen Java: placement-type registry + the load's resource manager) and `gateSets` in `structure_overrides.js`: sets with an unregistered placement type or no loaded structure are left out, absent ids skipped in tags, groups and standalone rows, one summary line. Covers every structure set from an absent mod or data pack, not only YUNG's | SQ-CBB-10 to -12 |
| Tags into Common | 16 tag files in Common's `data/` (2.3); `i.any_bark` now `#creativeengineering:bark`; the metal dust add left the Engineering jar's `metaltags.js`; Delightful's cactus chili gated on Corn Delight, certus and fluix knives on AE2 | SQ-CBB-03 |

### 2.2 Other open issues, broken down

| # | Issue | What is known | Next step | Where |
|---|---|---|---|---|
| 1 | Kippers removed on the chain | removed by CD `miners_delight/recipes.js:40` `R.remove({ input: t })` for the three removed tentacle items; the removed-item tag strip (`removed_item_tags.js`) runs on the chain too, so either `#forge:raw_fishes` still holds tentacles there or Brewin's kippers recipe's empty 4th ingredient `[]` matches any `{input: X}` filter | SQ-CBB-05 decides; fix: remove by type/id instead of by input (or exclude `brewinandchewin:` ids) | TODO `creativebrewing/publish #4` |
| 2 | **Built 2026-11-06 13:17 (plan_cb_publish.md 15): likely the deploying steps of the failed CD assemblies; SQ-CBC-03.** KubeJS "failed recipes": 204 on the chain, 232 on the instance set, 36 in the full pack | seen: `create:deploying` recipes with `{"fluid":"minecraft:empty","amount":0}` results and ingredients (NCH-made `kjs_` ids), so a fluid dict key resolves to empty when its mod is absent | one diagnostic boot with `logErroringRecipes=true` in `srv/local/kubejs/dev.properties`; group by script; NCH should skip (count as absent) instead of writing an empty fluid | new TODO row after the boot |
| 3 | **Built 2026-11-06 13:17 (plan_cb_publish.md 15): incomplete rows in CD (72), absent ids in remove/replace filters counted (81), 4 `Item.of` call sites (16); SQ-CBC-01, -02.** 137 `! [NCH] ... failed and was skipped` errors on the chain (startup log), e.g. `incomplete('vegetarian_burger') not registered` | rows for items of absent mods that are not caught by the absent-mod count (G5) | same diagnostic boot: list them by script and decide per row: `global.has` guard, `_skip`, or a real bug | plan_cb_publish.md 1.1 |
| 4 | **Built 2026-11-06 13:17: counting works across scopes; nothing was counted; the summary now always prints a line; SQ-CBC-02, -04.** `[absent mods]` summary prints nothing | `absent_mods.js` loads, `ServerEvents.afterRecipes` exists in this KubeJS; either nothing is counted (rows guarded before NCH sees them) or the counts object is a different copy | read `global.NCH._absentCounts` with a probe after the recipe event | TODO `creativebrewing/publish #3` |
| 5 | Client-class lines on a dedicated server | not from CB: Quark Foxhound, CD `toast.js`, the Rhino scans in CD `comfort_nourishment.js` / after `golden_values.js` (KubeJS reflects over every item class); harmless, boot continues | narrow the SQ-CBA-02/-15 regex to `creativebrewing` frames (done in SQ-CBB-04's judging note); optionally walk items through `Item#getFoodProperties` on the Java side so Rhino does not scan client signatures | queue |
| 6 | Creative Brewing optional dependencies | CB's scripts reference create_central_kitchen, fruitsdelight, tmted, alexsmobs, but CB's `mods.toml` does not list them (only CD's does now) | same `mandatory=false` entries in CB's `mods.toml` if wanted (owner) | plan_cb_publish.md |
| 7 | Meta structures and absent parts | the gate covers structure sets; meta structures name anchors and parts that may come from absent packs | check in SQ-CBB-10's log (`meta_structures.json ... (left out)` warnings) | structure framework writeup |
| 8 | Client rows | CQ-CBA-01 to -04 not run; -04 blocked until the gate is installed in CEPortTesting | rerun after round 2's server rows | queue |

### 2.3 Tag sweep (Explore agent, static, item tags only) and what was done

KubeJS 1902 adds a `#tag` entry through `global.tags.add` as a REQUIRED reference: when the nested tag does not exist,
the whole target tag fails to load. So tags the chain lacks broke more than the two recipes.

| Tag | Was | Now (Creative Common `data/`) | Chain effect |
|---|---|---|---|
| `creativeengineering:metal_dusts` | Engineering jar `metaltags.js` | same 15 members as optional entries | tag exists, still EMPTY on the chain (all members are IE, CE, MCA): Steel Toe Stout stays uncraftable there. Owner: pick a chain member (e.g. Create's crushed raw metals) |
| `creativeengineering:bark` (new, `i.any_bark`) | `#immersive_weathering:bark` | optional `#immersive_weathering:bark` + `farmersdelight:tree_bark` | Pale Jane brewable; full pack: FD tree bark also works in the pack's compost, soil and leather recipes (changelog bullet) |
| `forge:rope` | Comforts jar | optional `farmersdelight:rope` | rope fence rows work |
| `forge:wool`, `forge:concrete`, `minecraft:shulker_boxes` | CodeChickenLib, Supplementaries | vanilla members | Compat's physics floats / fireproof / lavaproof tags load |
| `forge:nuggets/netherite`, `forge:bread/corn`, `forge:fruits/lemon`, `forge:fruits/orange`, `create:scrap_copper_blocks(_cut)`, `create:scrap_wood_small/medium/large/and_book` | their mods | empty, exist only | the tags that nest them (lavaproof, fireproof, floats, burn/to_ash, bread_loaves, drink_fruits) load; the full pack's mods fill them |
| `forge:crops/corn`, `forge:gems/certus_quartz` | Corn Delight, AE2 | not defined: Delightful's cactus chili gated on `corn_delight`, certus and fluix knives on `ae2` | no recipe naming a missing tag |

Not checked by the sweep: block and fluid tags (next sweep, before publish).

## 3. r1106cbb: Creative Brewing chain, round 2 (SQ-CBB)

Owner request 2026-11-06 12:00: "execute the creative brewing scratch test and debug any issues that pop up during the
run". Scope: queue section "SQ-CBB Creative Brewing chain, round 2", SQ-CBB-01 to -12 (server); the CQ-CBA client rows
stay with the owner (CEPortTesting through ATLauncher). Targeted run: no perf session.

- Jars: CEPortTesting/mods got Compat 11:12 at 12:03 (was 01:31), then each rebuild below (Common 12:18, CD 12:27).
  Full-pack boot: `prep_srv.sh` swaps (build/libs Common, CD, Worldgen, Brewing, Compat, Calendar).
- Batches `tools/scratch_query/batches/r1106cbb/c1.qb` to `c4.qb` (+ `r1106cba/b1.qb` rows 01 to 08 and 10 for
  SQ-CBB-04); results `perf/runs/r1106cbb/`. OUT under the scratchpad, deleted after each boot (11 GB free).
- New tools: `tools/rhino_scope_test/` (pack Rhino, two scopes: cross-scope plain-object and loop-const checks),
  `tools/rhino_const_loop_check.py` (lists `const` inside loop bodies), `tools/scratch_query/tag_strip_probe.js`
  (what `global.tags._ents` keeps for given ids).

### Run notes

- 12:04 boot 1 (chain, A) Done 5.8 s. SQ-CBB-01 FAIL: removed still 3823. SQ-CBB-02: only the whole-undefined case was
  refused. Cause: `_undefinedIn` walked only objects whose prototype `=== Object.prototype`; each script pack has its own
  `Object.prototype`, so a filter written in a server script was never walked. Proved in the pack's Rhino
  (`tools/rhino_scope_test`: protoSame=false). Fix: `Object.prototype.toString.call(v) === '[object Object]'` (still
  skips Java objects and regexes). Common rebuilt 12:09.
- 12:10 same boot: `Couldn't load tag` x3: Vintage's `minecraft:cauldrons` and `mineable/pickaxe` list 30 fruit
  cauldrons that exist only with Fruits Delight, as required entries, so on the chain the whole `#minecraft:cauldrons`
  (Create's too) failed. Fix: those entries `"required": false` (both `src/main` and `src/own`); no full-pack change.
  CD rebuilt 12:11.
- 12:12 boot 1 again: SQ-CBB-01 to -03 PASS (removed 313, the probe's 4 refusals only, fermenting 18, Pale Jane in).
  SQ-CBB-05: kippers still gone and `miners_delight:tentacles` still in `#forge:raw_fishes` (and 14 other tags).
- 12:15 `tag_strip_probe.js` + /reload: the removed-item strip passes all 868 removed items to `global.tags._ents`, which
  returned 0. Cause: `const s` / `const c` / `const ns` in `_ents`' loop body; this Rhino keeps a loop-body const's first
  value (memory `rhino-fieldandmethods-gotcha`), and on the chain the first removed item is from an absent mod, so every
  entry was judged absent. Fix: `let`. `rhino_const_loop_check.py` over all script trees: one more hit,
  `kubejs/data/creativeengineering/kubejs/tags/metaltags.js:57-60` (full pack, not changed: TODO `creativebrewing/publish #7`).
  Common rebuilt 12:18.
- 12:19 boot 1 third time: strip keeps 52 ids (inEnts true), item tags 983 -> 1025, removed 283; kippers back, fermenting
  19 (21 minus kombucha and Steel Toe). SQ-CBB-01 to -05 and the SQ-CBA rows PASS except SQ-CBA-02 (5 client-class
  DISTXFORM lines, none near a creativebrewing frame: noise as in r1106cba).
- 12:21 boot 2 (cooking, A): crash, `Mixin apply failed creativedelight.mixins.json:alexsmobs.AlexsMobsSeagullItemMixin`
  (ClassMetadataNotFound `com.tarinoita.solsweetpotato...FoodContainerItem`): the two seagull lunchbox mixins test for
  Spice of Life's class; the cooking set has Alex's Mobs without Spice of Life. Fix: `CDMixinPlugin.NEEDS_ALSO_MOD`
  (both mixins also need `solapplepie`). JVM killed by pid (cwd in the scratchpad). CD rebuilt 12:27.
- 12:32 boot 2 Done 5.1 s: SQ-CBB-06 to -09 PASS (44 of 44, winemaker 34, removed 467, 108 dragon-breath brews).
  NCH failures 230 in startup.log (81 `Empty array?`, 63 `incomplete(...) not registered`, Delightful `grown_*` flower
  outputs undefined; list `perf/runs/r1106cbb/c2/nch_failures.txt`; open issue 2.2 #3).
- 12:33 boot 3 (instance + Creative Worldgen, B): the structure gate works (`173 set(s) and 254 structure(s) left out`,
  betterjungletemples among them, no structure-placement error), but `Failed to load datapacks`: `Unbound values in
  registry worldgen/biome` (92 terralith, 3 nullscape). Sources: the biome tuner reused its disk-cached solve made in the
  full pack (the overworld layout names every Terralith biome; its cache key has no loaded-mod set), and six Worldgen
  dimension files (autumnsky, springsky, summersky, wintersky, shield, yellowstone) name a Terralith biome. Not fixed
  (design: TODO `creativebrewing/publish #6`). SQ-CBB-10, -11 BLOCKED; log `perf/runs/r1106cbb/c3/latest.log.gz`.
- 12:35 prep_srv for boot 4: its SWAPS table still mapped `creativeengineering-*` to the Common jar (pre-11-04 name) and
  looked for `creativedelight-1.0.0`. The Common swap hit an existing hard-linked name, `cp -l` failed, and the plain
  `cp` fallback wrote THROUGH the link: **the pack's `mods/creativecommon-1.1.0.jar` was overwritten with the 12:27
  build (12:35:33)**. No copy of the 01:31 jar exists. Tool fixed: SWAPS rows per project via `newest()`
  (Common, Engineering, Equipment, Magic, Delight added), and the target name is unlinked before copying (same in
  `port_chain.sh`). Gotcha added.
- 12:39 boot 4 (full pack, B) Done 101.7 s: removed 1328, 0 refused, but `structure gate: 2 set(s) and 1 structure(s)
  left out (ae2 1)`: AE2's meteorite is registered in code (`InitStructures`, BuiltinRegistries), no JSON. Fix:
  `StructurePresence.structure` also accepts `BuiltinRegistries.STRUCTURES`. Worldgen rebuilt 12:41 (the build rewrote the
  hard-linked jar of the running scratch server; rows were done).
- 12:45 boot 4 again: `1 set(s) and 0 structure(s)`: `minecraft:shipwrecks`, empty in structure_config.json on purpose
  (Revamped Shipwrecks empties it). Same world either way (the mod's own empty set applies); gate now keeps sets that are
  empty in the config. Worldgen rebuilt 12:47.
- 12:50 boot 4 third time Done 95.2 s: SQ-CBB-12 PASS (removed 1328, no gate line, 0 refused). Full-pack tags vs the
  11-05 log: item tag adds 13035 -> 14133, block 7781 -> 7742 (the `_ents` fix and today's tag moves; not attributed yet:
  TODO `creativebrewing/publish #7`).
- 12:52 all JVMs stopped, OUT dirs deleted.

### Verdicts

| ID | Result | Evidence |
|---|---|---|
| SQ-CBB-01 | PASS (after fix) | removed 3823 -> 313 (283 after the `_ents` fix); no pack-script refusal |
| SQ-CBB-02 | PASS (after fix) | 4 refusals `filter.output`, `filter.input`, `filter.not[0].id`, `filter`; bread removed |
| SQ-CBB-03 | PASS (after fix) | fermenting 18 then 19; Pale Jane listed; IW bark empty, CE bark = FD tree bark; metal_dusts exists, 0 members; no tag error after the cauldron fix |
| SQ-CBB-04 | PASS | SQ-CBA-01, -03 to -08, -10 PASS; -02 noise only (no creativebrewing frame) |
| SQ-CBB-05 | PASS (after fix) | kippers listed; tentacles out of `#forge:raw_fishes` (11 members) once the strip works; cause was the `_ents` const, not the `[]` ingredient |
| SQ-CBB-06 | PASS (after fix) | boot crash fixed (seagull mixins need Spice of Life); no skipped CB script, `lava_vision.js` loaded |
| SQ-CBB-07 | PASS | 44 of 44, winemaker 34 |
| SQ-CBB-08 | PASS | removed 467; NCH failures 230 (recorded); fermenting 21+ |
| SQ-CBB-09 | PASS | 108 dragon-breath fluid brews |
| SQ-CBB-10 | BLOCKED | gate line with betterjungletemples PASS; `Done (` FAIL: Terralith/Nullscape biomes (TODO `creativebrewing/publish #6`) |
| SQ-CBB-11 | BLOCKED | boot never finished; no `nothing applied`; 111 "matches no structure" WARNs and meta-structure "never generates" lines (open issue 2.2 #7) |
| SQ-CBB-12 | PASS (after fix) | removed 1328, 0 refused, no gate line (AE2 builtin and empty-set false positives fixed) |
| CQ-CBA-01 to -03 | not run | owner, CEPortTesting through ATLauncher (`port_chain.sh bare`) |
| CQ-CBA-04 | not run | blocked by `creativebrewing/publish #6` (CEPortTesting's instance set has Creative Worldgen, no Terralith) |

## 4. r1106cbo: Creative Brewing chain round 3 and optional-dependency matrix (SQ-CBC, SQ-CBO)

Owner request 2026-11-06 13:50: "execute the ceportesting scratch testing" = the open CEPortTesting sections of the queue,
SQ-CBC (round 3) and SQ-CBO (optional dependencies), server rows. Client rows (CQ-CBC-01, CQ-CBO-01 to -04) stay with
the owner (CEPortTesting through ATLauncher). Targeted run: no perf session (also 11 GB free on `/`).

- Jars: build/libs copied into `CEPortTesting/mods/` at 13:55 (Common 13:36, Compat 13:36, Calendar 13:23, Delight 13:17,
  Brewing 13:23); `port_chain.sh check` clean. Full-pack boot (SQ-CBC-04): prep_srv swaps (Engineering 13:17).
- Tools: `tools/scratch_query/cbo_run.sh` (one port_chain boot: setup, boot, qrun, stop, logs into RES, OUT deleted;
  REUSE / KEEP / PRE / NOQRUN), batches `tools/scratch_query/batches/r1106cbo/` written by `gen.py` (checks S shared),
  `run_singles.sh` (SQ-CBO-20 to -53). Results `perf/runs/r1106cbo/<boot>/`.

### Run notes

- 13:55 preflight: no game JVM, RUNNER claimed, 11 GB free, jars copied.
- 13:57 boot c1 (chain, A) Done 6.0 s. BATCH-FAULTs fixed in `gen.py`: `[Cc]ycle` matched "recycleShowTicks";
  the reload wait regex; `server scripts ready` prints on `ServerEvents.loaded` only (boot), not on /reload.
  Tool fault: `cbo_probe.js` loaded `net.minecraftforge.fml.ModList`, refused by KubeJS's class filter, so `/cbo` was
  never registered; now `Platform.isLoaded`. A REUSE boot keeps the OUT's old probe copy: copy it in with PRE.
- 14:06 c1 third pass. Findings: (1) `[absent mods]` never prints in any boot: `absent_mods.js` (Common,
  `kubejs/data/creativecommon/kubejs/_lib/`) loads, but KubeJS 1902.6.2-build.73 declares `ServerEvents.afterRecipes`
  (`RECIPES_AFTER_LOADED`) and no class posts it (javap over the whole jar), so the listener never runs (full pack too).
  (2) `/cbo effects` leaks 4 on every boot: CB `kubejs_startup/creativeengineering/tool_effects.js` 134-137 register
  `creativeengineering:echo_pickaxe/axe/shovel/hoe` on-hit rows with no guard (Engineering jar items).
  (3) On the chain about 30 loot tables fail to parse (`Can't find block`): Vintage `vintagedelight:blocks/fd_*_jam_jar`
  (Fruits Delight blocks), `endersdelight:blocks/chorus_crate` / `endstone_stove`, `creativeengineering:blocks/placed_*`,
  `rusticdelight:blocks/pancakes`, `creativedelight:gameplay/fishing_net/junk`. CD / Engineering, not CB.
  (4) One failed recipe on the chain: CD `createsequencedassembly/horror_lasagna_sequenced_assembly`, its filling step
  gets `minecraft:empty` (fluid of an absent mod).
- 14:08 c1 restart with `reductionPerLevel 0.5`, copper off: SQ-CBO-05 PASS (97.0 / 94.0 / 97.0).
- 14:09 cbc05 (no `vinery_upstream`): refused as expected; the JVM hung after the crash report, killed by its own pid
  after 180 s (stop_wait.sh).
- 14:14 c3 (cooking): SQ-CBC-02 FAIL, 77 `Empty array?` left (r1106cbb 81), 82 failures in all (target under 40).
- 14:15 c5 (optional, 44 jars) Done 5.2 s; 14:17 restart with both members off (SQ-CBO-18).
- 14:19 single-mod and pair boots started (`run_singles.sh`), leak check now tolerates exactly the 4 echo rows.
- 14:19-14:44 28 single and pair boots (s20 to s53), all reached Done (16 to 18 jars, 2 to 6 s). Judged from the rows:
  the namespace checks of SQ-CBO-10, -23, -26, -28, -33, -35, -41 were row faults: `/cbo effects` groups by ITEM
  namespace; Botania, Supplementaries, Alex's Mobs, Create Deep Dark (echo armor not registered on purpose) and IE give
  only effects, and the Everything Copper / Immersive Armors rows sit inside CB's `has('immersiveengineering')` block
  (they show in the pair boots s52, s53). SQ-CBO-35: no "Lingerification: generated" line exists in the code and the
  bottling ids are not under `creativebrewing:bottling` (generator registered, bottling done 1557, no IE mixin error).
  SQ-CBO-36: Magic Resistance has no explicit provider; JEED's own potion provider covers it.
- 14:45 cbc06 (chain + Creative Worldgen 13:36 copied into OUT, no Geophilic): FML lists `geophilic_reforged` for
  `creativeworldgen`; nothing for creativebrewing.
- 14:46 c4 full pack (prep_srv, B, swaps Common, Engineering, CD, CB, Compat, Calendar; Worldgen kept at the 01:30
  install) Done 94.0 s. Pack `mods/` times unchanged after prep. CD transition items 89, Engineering 104 (its row list
  145 -> 65 in git), removed 1328, 36 failed, 296 `create:sequenced_assembly` (= 11-05 dump). CBC-04 shell FAILs were
  batch faults (`grep -c` exits 1 on 0; CD line is in startup.log, the kjsexpect reads server.log).
- 14:50 c3d: cooking boot with `nch_remove_diag.js` (srv only): the 77 `Empty array?` filters, list
  `perf/runs/r1106cbo/c3d/empty_array_filters.txt` (TODO `creativebrewing/publish #15`).
- 14:51 all JVMs stopped, OUT dirs deleted.

### Verdicts

Counts: 49 server rows, PASS 40, FAIL 9. Five FAILs (SQ-CBO-01, -04, -10, -17, -18) fail only on the 4 echo tool leaks
(TODO `creativebrewing/publish #12`); everything else in them passed. Client rows not run (owner): CQ-CBC-01,
CQ-CBO-01 to -04.

| ID | Result | Evidence |
|---|---|---|
| SQ-CBC-01 | PASS | `transition items (89)` = the rows in `cd_transition_items.js` (row said 80); 0 incomplete-item errors |
| SQ-CBC-02 | FAIL | 77 `Empty array?` left (r1106cbb 81), 82 in all (target under 40); no `grown_` / `twig_nest` lines; no `[absent mods]` line (`#11`, `#15`) |
| SQ-CBC-03 | PASS | `with 1 failed recipes` (r1106cbb 204): horror lasagna filling with `minecraft:empty` (`#14`) |
| SQ-CBC-04 | PASS | CD 89, Engineering 104, removed 1328, 296 sequenced assemblies (11-05 dump 296); `[absent mods]` absent (`#11`) |
| SQ-CBC-05 | PASS | `Vinery (Creative Brewing) reads its textures...`, no `Backport` |
| SQ-CBC-06 | PASS | `Mod ID: 'geophilic_reforged', Requested by: 'creativeworldgen'` |
| SQ-CBC-07 | PASS | no dependency, cycle or ordering error on the chain or the full pack |
| SQ-CBO-01 | FAIL | leaks 4 (echo tools); else as expected: `0 of 24: -`, `creativeengineering=4 minecraft=12`, the 4 potions `false` |
| SQ-CBO-02 | PASS | mixes 36, swaps 0, alcohol 42 of 44, JEED 76, bottling 1557, tipped 86 x 2, re-added 0, both better_beacons skips, no Lingerification, no `mix skipped` |
| SQ-CBO-03 | FAIL | no `[absent mods]` line (`#11`); NCH failures 24 (startup.log) |
| SQ-CBO-04 | FAIL | leaks 4 only; Poison applies, no CB exception after /reload |
| SQ-CBO-05 | PASS | 97.0 / 94.0 / 97.0, no config error |
| SQ-CBO-10 | FAIL | leaks 4 only; 24 of 24; namespace list row fault (see notes); `long_reach`, `phantom_bane`, `long_luck`, `long_lava_vision`, `speed_iii`, `resilience`, `restfulness` all registered |
| SQ-CBO-11 | PASS | mixes 58, swaps 15, dragon-breath 127 / re-added 127, Lingerification generator, bottling 2241, JEED 83, 44 of 44 |
| SQ-CBO-12 | PASS | all seven brews as expected |
| SQ-CBO-13 | PASS | climbing, teleport, knockback_resistance in; the three removed effects absent |
| SQ-CBO-14 | PASS | no CB script skipped, winemaker 34 (chain 21), fermenting 21, providers 94 |
| SQ-CBO-15 | PASS | 95.2 / 94.0 / 95.2; dispenser cloud |
| SQ-CBO-16 | FAIL | no `[absent mods]` line (`#11`); NCH failures 98 vs chain 24 (CD rows of the extra mods, `#15`) |
| SQ-CBO-17 | FAIL | leaks 4 only; no CB error after /reload |
| SQ-CBO-18 | FAIL | leaks 4 only; `members on: ` (both off), no script error |
| SQ-CBO-20 | PASS | 1 of 24 alexsmobs, lava_vision.js loaded, speed_iii and long_lava_vision brews, power_down not applied |
| SQ-CBO-21 | PASS | appleskin, no error |
| SQ-CBO-22 | PASS | long_reach / phantom_bane true, long_long_reach, strong_long_reach, no geode WARN |
| SQ-CBO-23 | PASS | botania; no leak beyond echo; namespace row fault (Botania effects only sit in the DragonLoot block) |
| SQ-CBO-24 | PASS | long_luck, strong_luck, clover -> luck |
| SQ-CBO-25 | PASS | dragon-breath registered and re-added |
| SQ-CBO-26 | PASS | no mixin error; namespace row fault (echo armor not registered on purpose) |
| SQ-CBO-27 | PASS | dragonloot namespace, no leak beyond echo |
| SQ-CBO-28 | PASS | no `immersiveengineering:conductive` leak (copper rows inside the IE block); namespace row fault |
| SQ-CBO-29 | PASS | swaps 8, fermenting 20, no `is not a food` WARN |
| SQ-CBO-30 | PASS | felsic_gear namespace, no better_beacons leak |
| SQ-CBO-31 | PASS | 43 of 44, winemaker 34 (chain 21), chorus -> `vinery:teleport` |
| SQ-CBO-32 | PASS | wrappist -> nothing, no `mix skipped` |
| SQ-CBO-33 | PASS | no `flammable` leak (rows inside the IE block); namespace row fault |
| SQ-CBO-34 | FAIL | Common `compost.js:181` WARN for IW leaf piles of Architect's Palette / Fruits Delight (`#16`) |
| SQ-CBO-35 | PASS | refinery generator registered, bottling done 1557, no IE mixin error; row's `generated` line and bottling id prefix do not exist (row fault) |
| SQ-CBO-36 | PASS | `jeed_providers.js: N`, 54 providers with `creativebrewing:` ids; no Vinery WARN; Magic Resistance via JEED's potion provider (row fault) |
| SQ-CBO-37 | PASS | jei, no JEI plugin frame |
| SQ-CBO-38 | PASS | swaps 7, agility -> `vinery:climbing_effect` |
| SQ-CBO-39 | PASS | no `PotionFluidHandlerMixin` error |
| SQ-CBO-40 | PASS | resilience brews -> nothing; Resilience STAYS (remap target absent), no exception |
| SQ-CBO-41 | PASS | no leak beyond echo; namespace row fault (Supplementaries effect only) |
| SQ-CBO-42 | PASS | 2 of 24, 43 of 44 |
| SQ-CBO-43 | PASS | both effects `Unable to apply`, both brews -> nothing |
| SQ-CBO-50 | PASS | wrappist -> long_reach, celestite -> phantom_bane |
| SQ-CBO-51 | PASS | resilience -> `alexsmobs:knockback_resistance`, mixes 47 (s20: 46) |
| SQ-CBO-52 | PASS | `everythingcopper=4`, no leak beyond echo |
| SQ-CBO-53 | PASS | `immersive_armors=4`, no leak beyond echo |
| CQ-CBC-01, CQ-CBO-01 to -04 | not run | owner, CEPortTesting through ATLauncher |

## 5. r1106cbp: Creative Brewing polish (SQ-CBP, SQ-CDFL)

Owner request 2026-11-06 17:15: "build scratch tests and run them for the latest creative brewing polish steps" =
[plan_cb_polish.md](../plan_cb_polish.md) 1 to 4, queue sections SQ-CBP and SQ-CDFL (rows already written), server
rows. Targeted run: no perf session (11 GB free on `/`). Client rows (CQ-CBP-01, CQ-CDFL-01) stay with the owner.

- Jars: build/libs of 16:51 to 16:54 (Common 1.1.0, Compat, Calendar, Delight, Brewing, Equipment) copied into
  `CEPortTesting/mods/` at 17:20 (Equipment only per boot, by PRE). Full-pack boots: prep_srv swaps, old-save pair
  `NOSWAP=1` then `tools/scratch_query/oldsave_swap.sh`.
- Tools: batches `tools/scratch_query/batches/r1106cbp/` written by `gen.py` (reuses r1106cbo's batches with the echo
  leak tolerance removed), `run_boots.sh [regex]` (cbo_run.sh per boot), new `oldsave_swap.sh`. Results
  `perf/runs/r1106cbp/<boot>/`.

### Run notes

- 17:20 preflight: no game JVM, RUNNER claimed, 11 GB free, jars copied, `port_chain.sh check` clean.
- 17:21 p1 (chain) Done; restarted by the batch's /reload. Leaks 0, `effects items by namespace:` empty (echo and gear
  rows gone from the chain). `[absent mods]` prints after the recipe event (boot and /reload), loot-table line
  `0 left out by forge:conditions, item entries dropped {creativeengineering=1, endersdelight=2, fruitsdelight=22, ...}`,
  0 loot parse errors. Horror lasagna still `Error parsing recipe` with a `minecraft:empty` filling step (SQ-CBP-03
  FAIL). The boot print and the /reload print differ (boot: IE 611 recipe rows, no fruitsdelight / farmersrespite
  recipe rows; reload: IE 627, fruitsdelight 21): not doubled, so the counters reset, but the boot pass counts fewer
  rows. SQ-CBC-01 `kjsexpect` reads server.log, the transition line is in startup.log (89): BATCH-FAULT, PASS by
  evidence.
- Cause of SQ-CBP-03 (read-only): kubejs-create 1902.2.4 builds `create:filling` with `FluidComponents.INPUT_OR_ITEM_ARRAY`,
  so each input value is a `com.mojang.datafixers.util.Either`; `NCH._stepHasEmptyFluid` only tests class names with
  "Fluid" and never unwraps `left()` / `right()`, so the empty `CreateInputFluid` is never seen. Not fixed (behaviour
  change, owner decides).
- 17:32 p2 (cooking): `Empty array?` 0 (r1106cbo 77), failures 6 (82), `[NCH] removed 2 Immersive Engineering recipes
  whose output tag is empty: crusher/ore_nickel (#forge:dusts/platinum), blastfurnace/steel_bloc...` (in startup.log;
  the `kjsexpect` reads server.log: BATCH-FAULT, PASS by evidence). Six failures left: `replaceItems #forge:dough/wheat`
  (tag string as an item id), `replaceInput new input[] undefined`, `createMixing soap_heated_mixing` and
  `soap_vegan_heated_mixing` output undefined, `ceBaking cake_base_baked_flour` / `_dough` result undefined.
- 17:33 p3 (optional, 44 jars) + 17:36 p3b (members off). Leaks 0. `effects items by namespace:` empty even with every
  optional mod: the gear rows are Equipment's now, so SQ-CBO-10's namespace regex is obsolete (row fault). Mixes 58,
  swaps 15, re-added 127, bottling 2241, 44 of 44 drinks; JEED providers 75 (r1106cbo 83: the gear providers moved to
  Equipment). Failures 12 (r1106cbo 98).
- 17:37 p4 (chain + Immersive Weathering): `[compost] skipped 36 values for items of absent mods`, no
  `immersive_weathering:` missing-item line.
- 17:38 p5 and p6 BATCH-FAULT (a `bootreject` before the first row; qrun refused the batch); p6 also crashed at FML:
  `kubejsdelight` requires `farmersdelight`, nested in the CD jar that the boot drops. kubejsdelight dropped too, both
  rerun 17:45.
- 17:39 p5 (cooking set without CB, 34 jars) Done 5.3 s: SQ-CDFL-01, -02 PASS (fluid tags name `creativedelight:`,
  no `Unknown fluid`, filling / emptying / Kettle / mixing name `creativedelight:` fluids).
- 17:40 p6 (Common, Compat, Equipment, chain base; no Calendar, CD, CB, kubejsdelight) Done 5.2 s. Tool fault: `/zq`
  failed to register (`zq_probe.js` loaded `creativecalendar...SeasonTypes` at script load); now loaded on first
  `/zq season`. Rerun 17:42 (p6) and with Felsic Gear (p6f): leaks 0, `tool onHit 4` (echo rows), namespaces
  `creativeengineering=4 minecraft=12` (+ `felsic_gear=4`), the three `creativecommon:` potions registered, no
  `global.CB` or script error. Findings (both boots): 5 `Error parsing recipe`: the 4 steps of Equipment's felsic ingot
  sequenced assembly (`kubejs/data/creativeequipment/kubejs/equipment/end_gear.js` 47-52) get `minecraft:empty`: their
  incomplete item is registered only by Creative Engineering's `transition_items.js` (Equipment needs Engineering, a
  sibling dependency); and `creativeequipment:smithing/backpack_skin` throws `NoClassDefFoundError
  .../sophisticatedbackpacks/backpack/BackpackItem` without Sophisticated Backpacks.
- 17:44 p7 full pack, `NOSWAP=1` (installed jars), superflat Done 10.5 s: tanks of `creativeengineering:carrot_juice`
  4000 and `egg_solution` 500, a Create potion fluid `creativebrewing:resistance`, a chest (two potions, a potion
  bucket, `kubejs:long_wither`), two dispensers with `creativebrewing:` splash potions, a golem with
  `creativebrewing:magic_resistance`; saved, stopped. `oldsave_swap.sh` put in the 16:51-16:54 builds (Engineering
  13:17, Magic 11-05).
- 17:47 p8 same world, new jars, Done 4.3 s. `[swaps] minecraft:fluid: 66`, `mob_effect: 1`, `potion: 38 id(s) remapped`;
  config sections `[effects, magic_resistance]` merged CB -> Common, `[copper_poisoning]` CB -> Equipment (values equal
  to `configs_edited/root_config/creativebrewing-common.toml`, key order changed; CB file keeps `[members] [alcohol]`).
  Golem effect now `creativecommon:magic_resistance`; both dispensed old-id splash potions applied (`minecraft:resistance`,
  `creativecommon:magic_resistance`); tanks read `creativedelight:carrot_juice` 4000 / `egg_solution` 500. Item NBT keeps
  the old Potion strings (resolved by the remap mixin on read). Removed 1328, `Empty array` 0. Configs of the boot in
  `perf/runs/r1106cbp/p8/`.
- 17:53 single-mod and pair reruns s20 to s53 (s34 = p4) with strict leak checks.
- 17:49-18:20 s20 to s53 (27 boots, s34 = p4): leaks 0 in every boot, no script error. The FAILs are all row faults
  now: the `effects items by namespace` checks (SQ-CBO-23, -26, -27, -28, -30, -33, -35, -41) and the IE
  `conductive` / `flammable` rows (SQ-CBO-52, -53) expect gear rows that moved to Equipment (`gear_effects.js`) and so
  are not on the chain; SQ-CBO-35 `Lingerification: generated` and SQ-CBO-36 explicit Magic Resistance provider were
  already row faults in r1106cbo. Counts as in r1106cbo: swaps 8 (Respite) / 7 (Neapolitan), fermenting 20, winemaker
  34, mixes 46 (Alex's) / 47 (+ Quark); Restfulness, Insomnia, Resilience brews -> nothing.
- 18:22 all JVMs stopped, OUT dirs deleted, RUNNER released.

### Verdicts

Counts: 54 server rows judged (27 targeted + 27 single-mod reruns): PASS 52, FAIL 2. BATCH-FAULT judged by evidence:
SQ-CBC-01, SQ-CBP-02. Row faults (gear rows moved to Equipment) judged on leaks and errors: SQ-CBO-10, -23, -26, -27,
-28, -30, -33, -35, -36, -41, -52, -53. Client rows not run (owner): CQ-CBP-01, CQ-CDFL-01.

| ID | Result | Evidence |
|---|---|---|
| SQ-CBP-01 | PASS | `[absent mods]` recipe and tag lines after the recipe event (boot and /reload); `[absent mods] loot tables: 0 left out by forge:conditions, item entries dropped {creativeengineering=1, endersdelight=2, fruitsdelight=22, hostiledelight=1, moredelight=2, supplementaries=11, upgrade_aquatic=1}`; loot parse errors 0 |
| SQ-CBP-02 | PASS | `Empty array?` 0 (77); failures 6 (82); `[NCH] removed 2 Immersive Engineering recipes whose output tag is empty` (crusher/ore_nickel, blastfurnace/steel_block), in startup.log |
| SQ-CBP-03 | FAIL | horror lasagna still `Error parsing recipe` with a `minecraft:empty` filling step: `_stepHasEmptyFluid` does not unwrap kubejs-create's `Either` input values (notes) |
| SQ-CBP-04 | PASS | `[compost] skipped 36 values for items of absent mods`; no `immersive_weathering:` missing-item line |
| SQ-CBP-05 | PASS | `[absent mods]` once per load, `Empty array` 0, no full-pack recipe pruned, removed 1328 |
| SQ-CBP-06 | PASS | old save: golem effect `creativebrewing:magic_resistance` -> `creativecommon:magic_resistance`; dispensed `creativebrewing:resistance` / `:magic_resistance` splash potions applied their effects; `[swaps] potion: 38`, `mob_effect: 1` remapped; no missing-mapping line |
| SQ-CBP-07 | PASS | `[effects, magic_resistance]` merged into Common, `[copper_poisoning]` into Equipment; values equal to configs_edited (key order changed); CB keeps `[members] [alcohol]` |
| SQ-CBP-08 | FAIL | Equipment without CB: leaks 0, echo onHit 4, `creativecommon:` potions registered, no `global.CB` error; but Equipment's felsic ingot sequenced assembly fails (4 steps `minecraft:empty`: incomplete item registered only by Engineering) and `smithing/backpack_skin` throws `NoClassDefFoundError` without Sophisticated Backpacks |
| SQ-CBP-09 | PASS | chain: leaks 0, `effects items by namespace:` empty; 27 single and pair boots leaks 0 |
| SQ-CBP-10 | PASS | SQ-CBO-12, -13, -15 PASS on `creativecommon:` ids |
| SQ-CDFL-01 | PASS | CD alone: fluid tags name `creativedelight:`, no `Unknown fluid`, effect swaps line printed |
| SQ-CDFL-02 | PASS | filling / emptying / Kettle / mixing name `creativedelight:` fluids |
| SQ-CDFL-03 | PASS | old save: tanks read `creativedelight:carrot_juice` 4000, `creativedelight:egg_solution` 500; `[swaps] minecraft:fluid: 66 id(s) remapped` |
| SQ-CDFL-04 | PASS (static) | the three soft fluids name `creativedelight:` as `equivalent_fluids`; no WARN. Filling a jar in game stays with CQ-CDFL-01 |
| SQ-CBC-01, -02, -03, -07 | PASS | transition items 89; failures cooking 6, chain 24 |
| SQ-CBO-01 to -04, -10 to -18 | PASS | leaks 0 (r1106cbo: 4); mixes 36 / 58, swaps 0 / 15, bottling 1557 / 2241, JEED 75 (83 with the gear providers, now Equipment's), failures 12 with all optional (98) |
| SQ-CBO-20 to -43, -50 to -53 | PASS | leaks 0 in all 27; counts as in r1106cbo |

## 6. r1106spf: Creative split feature regression (SQ-SPF)

Owner request 2026-11-06 19:30: "execute the latest creative split feature assurance scratch tests" = queue section
SQ-SPF (SQ-SPF-00 to -111; CQ-SPF-01, -02 stay with the owner: pinned bulk client session). Targeted run, no perf
session (13 GB free on `/` after deleting the finished r1106cbp scratchpad, owner OK).

- Jars: baseline = installed `mods/` (Common 11-06 12:35, Compat / Engineering / Equipment / Magic 11-05 22:09,
  Calendar / Delight / Brewing / Worldgen 11-06 01:30, Devtools 11-04 23:35, Labels 11-03 20:36). New = build/libs
  of 11-06 18:51-18:54 (Common 1.1.0, Compat, Calendar, Magic, Equipment, Engineering 2.0.0, Worldgen, Delight 1.1.0,
  Brewing 1.0.0) via `oldsave_swap.sh` (Worldgen added to it this run). Devtools and Labels: installed jars (no newer
  build).
- Batches `tools/scratch_query/batches/r1106spf/` composed by `gen.sh` from `b0_fixture.qb`, `_probes.qb` (run on
  both boots, `-base` ids on boot 0), `_b1_new.qb`, `_b1_rerun_extra.qb`, reruns pulled from r1031 / r1101 / r1103w
  batches by the new `tools/scratch_query/qb_extract.py`. Results `perf/runs/r1106spf/<boot>/`.
- Boots: b0 (A, NOSWAP, fixture + baseline + /dumpall), b1 (same save, new jars, 58 rows), b1c (CD campfire toml
  variants), b1n (no Devtools), jx_* (Compat, Magic, Equipment, Worldgen, Calendar removed one at a time), b2 (B,
  seed 8675309, `[named_mobs] chance = 1.0`), port chain p1 / p2 / p6 / p6f reruns (SQ-SPF-92).
- Simplified against the row text (noted per row in the verdicts): sawmill and dragon fan run on counts and a
  fan-only rig, vein undo / shield block / gliding / step-up go to the client rows, the keg and barrel are checked
  as block entities plus recipe counts, SQ-STN reruns cover STN-04 and -05 only, SQ-CFR-02 to -04 (config carries
  from pre-1.2 files) not rerun (SQ-CBP-07 / SPF-110 cover the current carry state).

### Run notes

- 19:32 claim, preflight: no game JVM; 7.1 GB free -> owner OK'd deleting the r1106cbp scratchpad (2.7 GB) -> 13 GB.
- 19:38 batches written (gen.sh), b0 next.
- 19:39 b0 on the installed jars (NOSWAP=1) FAILED at datapack load: `Unbound values in registry
  worldgen/placed_feature: creativeengineering:wild/brown_mushroom_colonies, wild/cave_carrots/core, ...`. Those
  placed features now live only in the 18:52 Worldgen build's gated packs (`packs/miners_delight/...`); an installed
  01:30 jar still names them. **Finding: the installed `mods/` set does not boot with the current `kubejs/` tree**
  (the owner's game too, until the 18:5x jars are installed). Decision: no installed-jar baseline; baseline = the
  instance's `kubejs/exported/` dumps (11-05 22:41) and earlier run counts; b0 and b1 both on the 18:5x builds, so
  the "same save" rows check a restart, not the jar swap (old-id remaps were SQ-CBP-06 / SQ-CDFL-03).
- 19:52-19:58 b0 on the 18:5x builds crashed three times at world creation: `imm_ptl.mixins.json:
  common.chunk_sync.MixinChunkMap_C ... PRIVATE @Overwrite method m_183760_ cannot reduce visibility of PUBLIC target
  method`. Cause: Immersive Portals 2.3.7 is in `mods/` (not in `mod_sides.md`, not in r1106cbp's server set: a
  parallel session is adding it, `feature_writeup/immersive_portals.md`), and Serene Seasons' AT makes every ChunkMap
  method public (`public net.minecraft.server.level.ChunkMap *()`). Not ours, not a flake (Portal Gels and IP's new
  config ruled out). **Finding for the IP integration: IP 2.3.7 cannot load on a server with Serene Seasons 8.1.0.24.**
  Run decision: Immersive Portals, Portal Gun (needs IP) and Portal Gels (unlisted) left out of every scratch srv.
- 19:59 b0 Done 9.0 s; b0.qb running (fixture, /dumpall).
- 20:04 b0 rows: fixture built; /dumpall 24/25 (dumpclimatecaves failed on superflat), batch regex expected the old 'all 25 dumps done' text (BATCH-FAULT, fixed). compare.py vs kubejs/exported (11-05): recipes 31138 -> 31145, food_family tags grew (generator), fluid tags renamed to creativedelight:, counter_toppers lost 44 displaydelight:ctd_plated_* and registry blocks 15694 -> 14812 with identical item lists (SQ-SPF-80c added). 20:18 b1 Done 4.4 s.
- 20:45 b1 done (PASS 11, FAIL 17, LOOK 31): bot not respawned after the restart and gen.sh's '^#' filter dropped the row headers of _b1_rerun_extra / _probes (both BATCH-FAULT, fixed); b1r (47 rows) reruns them on the same boot.
- 21:00 b1c (CD campfire toml off switches + fresh-block reruns) PASS 6 LOOK 5; 21:05 b1n (no Devtools) boots clean, shrapnel 24; 21:08 SQ-SPF-111 jar-out loop (jx_run.sh).
- 21:12 SQ-SPF-111: Compat out -> FML refuses (8 jars declare creativecompat, by design 7.6; row text wrong); Calendar out -> Vinery, CD, CB refuse (expected); Magic / Equipment / Worldgen out boot, one script error each (progression.js, archery.js unguarded; Devtools dump_bee_nests.js loads a Worldgen class). 21:14 b2 prep (B, seed 8675309, IP jars left out, [named_mobs] chance 1.0).
- 21:26 b2 (B seed 8675309): SPF-70 3 of 4 locates (sky_island: no structure_set), SPF-72/-73/NM-01 partly unjudged (spawn is ocean; b2r/b2s at the plains 845 blocks out; the server fell 40 s+ behind on chunk saving and stalled, stopped 21:26). 21:27 port chain p1 / p2 / p6 / p6f on CEPortTesting with the 18:5x jars copied in (chain_boots.sh).

### Verdicts (r1106spf)

Boots: b0 fixture + /dumpall, b1 (59 rows), b1r (bot respawned, 47 rows), b1s, b1c (CD campfire off switches), b1n (no
Devtools), jx_* (one jar out), b2 / b2r / b2s (B world). Results `perf/runs/r1106spf/<boot>/`; tag/recipe diff
`perf/runs/r1106spf/compare_b0.md` (base = `kubejs/exported/` of 11-05 22:41).

| ID | Result | Evidence |
|---|---|---|
| SQ-SPF-00 | PASS | 12 Creative jars + Exposure listed; 0 mixin / class errors naming ours (9 lines, all third-party client classes); no script error. compare_b0: recipes 31138 -> 31145, every per-type count the same except `vinery:wine_fermentation` 27 -> 26 (`mead`, removed on purpose: `UpstreamTranslator.REMOVED`); item lists identical; fluid tags renamed to `creativedelight:`; food_family tags grew (generator) |
| SQ-SPF-01 | PASS | andesite alloy 8 recipes, copper ingot tag; NCH recipe namespaces per jar (creativeequipment 167, creativemagic, creativedelight) |
| SQ-SPF-02 | PASS | `/give farmersrespite:coffee_beans` -> `rusticdelight:roasted_coffee_beans`; `[itemMerges]` and `[swaps] 22 list file(s) ... 43 item merge(s)` lines |
| SQ-SPF-03 | PASS | `Loaded 0 advancements`; MCA farmer offers with pack items (b1r; vanilla villager selector was a BATCH-FAULT) |
| SQ-SPF-04 | PASS | `creativecommon:` long_reach, phantom_bane, magic_resistance, long_magic_resistance registered; `creativebrewing:magic_resistance` gone; effect applies (04b) |
| SQ-SPF-05 | PASS | 4 x 2 section fills, 0 null-state / section-update lines; SQ-GFB-01 PASS |
| SQ-SPF-10 | PASS | moss kept; SQ-VS-01..-08 PASS (b1c on fresh blocks: b1r reused b1's blocks), SQ-MB-01..-06 PASS (b1r) |
| SQ-SPF-11 | PASS (partial) | log in fire -> `supplementaries:ash`, stick in lava destroyed; SQ-IP-15 FAIL as on r1102b and r1103w (not a split change) |
| SQ-SPF-12 | PASS (leaf piles) / client | SQ-LP probe: scheduled-tick decay 20 -> 0 leaves, piles 5; tree 300 ticks -> 13 piles. Walk speed: `pbot path` moves at a fixed rate, judge in CQ-SPF-02 |
| SQ-SPF-13 | PASS | `Killed 24 entities` one tick after a TNT blast = `[shrapnel] count 24` (b1n). Vein undo: client |
| SQ-SPF-14 | client | feather on a stick used (CONSUME); the cat is converted by Cozy's Improved Cats so the vanilla selector found none |
| SQ-SPF-20 | PASS | `/cetime date` day 1 -> day 2 after /time add 24000; `[time] Better Days from [time]` push line; SQ-TD-01..-05 PASS (TD-02, -05 by evidence: regex window) |
| SQ-SPF-21 | PASS | off-season cucumber mean age 2.0 (MID_WINTER) vs 3.4 (MID_SUMMER), both grow; wheat read `cold` after SQ-CS-06's edit (no /reload after the restore); SQ-CS-01, -02, -06, -08 PASS |
| SQ-SPF-22 | covered | console prints only the lang keys of `/wine time`; SQ-TD-04 shows the wine clock following `/cetime` both ways |
| SQ-SPF-30 | PASS | SQ-CDV-01..-04 PASS (CDV-02 by evidence: `Recipe scan: 79 references to 68 removed items`); /dumpall 24/25 (dumpclimatecaves fails on a superflat) |
| SQ-SPF-31 | PASS | boots without Devtools; Devtools scripts `Skipped ... not loaded`; /dumpall, /cegen unknown; one WARN `Missing data pack mod:creativedevtools` (world remembers it) |
| SQ-SPF-40 | PASS | flint and steel: PortalFailureHandler fired, no portal; Flint and Terrasteel: `creativeengineering:dark_fire`, portal opened; `creativemagic:` recipes. Crying obsidian not seen on the corner block checked |
| SQ-SPF-41 | PASS | `[XPFluidBridge] Startup OK: create_enchantment_industry:experience` from `creativemagic`; `#forge:experience` = CEI experience + flowing; SQ-CMG-05 tcprobe 0 fail |
| SQ-SPF-50 | PASS | chest NBT kept over two restarts (backpack colours, sawblade Damage 5); tags equal to base (16/16/17/1/1, needs_copper_tool 14); 167 `creativeequipment:` recipes; dragon helmet repaired 50 -> 0 |
| SQ-SPF-51 | client | bot bucket use raycasts its own look (PASS, nothing placed); bucket and fletching recipes present |
| SQ-SPF-52 | PASS (data) / client | SQ-STL-01, -02 PASS (recipes now `creativeequipment:shaped/*_stepstool`, 16); Curios slot and step-up: client |
| SQ-SPF-60 | PASS | dragon breathing 14 recipes (= base); fan + dragon head turns cobblestone into end stone (60b); sawmill 241 (= base) |
| SQ-SPF-61 | PASS | components, coils, dust piles, pellets, prepared flesh, hemp fabric recipes; sequenced assembly 296 (= base) |
| SQ-SPF-62 | PASS | IE sawmill 241, crusher 159, metal press 49, blueprint 224 (= base) |
| SQ-SPF-70 | PARTIAL | creative packs enabled; ruin_scatter (1528), ruin_cluster (977), pillager_camp_ruins (7188) located; `sky_island` not found (no structure_set in the jar); `/zq under` ruin_scatter at chunk 5 -96: air-below 36 columns, max 11 (earlier runs 0 to 6; 49 columns unloaded) -> recheck |
| SQ-SPF-71 | PARTIAL | bred mooshrooms gave a calf (Age -23839); 238 entities at the plains; moobloom / baby mob ids not read (server stalled) |
| SQ-SPF-72 | PARTIAL | leaf decay: SQ-LP; Forgiving Void did not catch the bot (fell to y -259, `ceperfbot remove`): Balm's player tick may not run for bots -> client |
| SQ-SPF-73 | not judged | spawn is ocean; plains box 80 x 80 at 3736 3416: grass 1676, wild crops 0 (one box) |
| SQ-NM-01..-03 | NM-02 PASS; -01, -03 not judged | no animals in range in b2, server stalled before the names were read in b2s |
| SQ-SPF-80 | PASS | cooking 345, cutting 360 (= base); five dishes' food values; potato soup out of a pot (80d); Display Delight `ctd_` plated blocks no longer registered (Cultural Delights absent; 80c, see findings) |
| SQ-SPF-81 | PASS | SQ-CFR-01 (one apply, CD), -05 (only the open-sky one on stone out), -06, -07, -07b, -08 PASS; SQ-STN-05 PASS; SQ-STN-04 not judged (kettle NBT did not start a brew) |
| SQ-SPF-82 | PASS | torch_crafting_animal_fat and seeds_bone_meal now `creativedelight:` (namespace moved, compare_b0); millable seeds 34 |
| SQ-SPF-83 | PASS | `forge:fluid/chorus_juice`, `egg_solution` -> `creativedelight:`; composter level 3 after 20 cucumbers (chance 0.3) |
| SQ-SPF-90 | PASS | wine_fermentation 26 (mead removed on purpose), apple_fermenting 2; barrel and keg block entities load |
| SQ-SPF-91 | PASS | fermenting 21, keg_pouring 24 (= base); beer -> `brewinandchewin:tipsy` 2351 t + intoxication |
| SQ-SPF-92 | FAIL (2) | port chain on CEPortTesting with the 18:5x jars (`chain_boots.sh`, results `c_p1`, `c_p2`, `c_p6`, `c_p6f`): c_p1 / c_p2 as r1106cbp (SQ-CBC-01, SQ-CBP-02 BATCH-FAULT, line in startup.log); **SQ-CBP-03b FAIL**: horror lasagna sequenced assembly still `Error parsing recipe` with a `create:filling` step `{"fluid":"minecraft:empty","amount":0}` on the 18:51 Common (the `Either` unwrap is in the jar); **SQ-CBP-08b FAIL**: c_p6 (no Felsic Gear) `end gear transition items (37)`, `felsic_gear` off, `[absent mods] ... sophisticatedbackpacks` 2, but `Error parsing recipe create:kjs_... create:filling` for `kubejs:incomplete_ingot_felsic`; c_p6f (with Felsic Gear) does not boot: `Unbound values in registry worldgen/biome: [nullscape:crystal_peaks, nullscape:shadowlands, nullscape:void_barrens]` (Equipment `packs/felsic_gear/.../placed_feature/felsic_ore_*.json` and biome modifiers name Nullscape biomes); SQ-DEP-06 no disabled warning (b1) |
| SQ-SPF-100 | PASS (placement) | bot placed a sticky note and a label on the chest face (CONSUME); stored data not readable by `/data` (no block entity, no `creative_labels:label` entity) -> look in CQ-SPF-02 |
| SQ-SPF-110 | PASS | one file per jar; `creativeengineering-common.toml` absent; same table names in several files (`[perf]`, `[worldgen]`, `[wthit]`, `[tooltips]`, `[members]`) with different keys, no key in two files |
| SQ-SPF-111 | PASS with findings | Compat out: FML refuses (all 8 jars declare `creativecompat`, creative_split.md 7.6; the row's "only Common" premise is wrong); Calendar out: Vinery, CD, CB refuse (expected); Magic / Equipment / Worldgen out boot, one script error each (findings) |
| CQ-SPF-01, -02 | not run | owner's bulk client session |

### Findings (r1106spf)

1. **Installed `mods/` set does not boot with the current `kubejs/` tree** (unbound `creativeengineering:wild/*` placed
   features: they now live only in the 18:52 Worldgen jar's gated packs). Install the 18:5x builds together.
2. **Immersive Portals 2.3.7 crashes a server with Serene Seasons 8.1.0.24** (SS's AT `public ChunkMap *()` vs IP's
   private `@Overwrite m_183760_` in `MixinChunkMap_C`). IP, Portal Gun, Portal Gels are in `mods/` but not in
   `mod_sides.md` (another session's integration); left out of every srv here. Gotcha added to the skill.
3. SQ-CBP-03b FAIL: `NCH._stepHasEmptyFluid` still misses the horror lasagna's empty filling fluid on the 18:51 Common.
4. SQ-CBP-08b FAIL: Equipment's felsic ingot assembly has an empty-fluid filling step without Felsic Gear, and its
   `felsic_gear` pack hard-references three Nullscape biomes (boot fails without Nullscape).
5. Scripts that run without their jar: `creativemagic:kubejs/magic/progression.js#4`,
   `creativeequipment:kubejs/equipment/archery.js#5` (`Cannot call method "recipes" of undefined`); Devtools
   `dump_bee_nests.js` loads `creativeworldgen...BeeNests` (no declared dependency).
6. Display Delight `ctd_plated_*` blocks (Cultural Delights, not in the pack) are no longer registered: 44 left
   `creativeengineering:counter_toppers`, block registry 15694 -> 14812 with identical item lists. Probably the
   intended gating; confirm.
7. `item_dict.js:14064` `i.mead = 'vinery:mead'` names an item the Vinery port removes.
8. Worldgen: `creativeengineering:sky_island` has a structure but no structure_set (never generates); one
   `meta/ruin_scatter` start (chunk 5 -96) has 36 air columns under it (max 11), earlier runs 0 to 6: recheck.
9. Row-text faults for the queue: SQ-SPF-111 assumes the jars need only Common (all need Compat, 7.6); SQ-SPF-03
   vanilla villager selector (MCA); SQ-SPF-14 vanilla cat (Cozy's); SQ-SPF-52 armor feet slot (Curios);
   SQ-SPF-73 / SQ-NM-01 near spawn (seed 8675309 spawn is ocean); `zq count` takes no `~`.
- 21:50 all JVMs stopped. Totals over the SQ-SPF rows: PASS 33 (some partial), FAIL 2 (SQ-SPF-92 via SQ-CBP-03b and
  -08b), PARTIAL 3 (SQ-SPF-70, -71, -72), not judged / client 6 (SQ-SPF-14, -51, -73, NM-01, -03, part of -12 / -52 /
  -100); CQ-SPF-01, -02 for the owner.
- 21:53 scratch OUT dirs deleted; CEPortTesting/mods keeps the 18:5x copies of Common, Compat, Calendar, Delight, Brewing (copied 21:10); RUNNER released.

### After the run (2026-11-06 22:10, owner request)

Fixes for findings 2 to 8 built (all ten Creative jars 22:07-22:09, not installed), tests queued as SQ-SPFX-01 to -07,
CQ-SPFX-01; row-text faults (finding 9) fixed in the queue and in the batches (r1106spf `_b1_new.qb`, `_b2_new.qb`,
`_rerun_A.qb`, r1031 `b1_gameplay.qb` / `a1_data.qb`, r1101 `cs1_split.qb`, r1106cbo / r1106cbp `gen.py` and their
startup.log checks). Finding 3 note: `_absent('minecraft:empty')` throws, so the old loop did stop; the miss was the
detection itself, now backed by a JSON check. TODO `creativesplit/regression #15` to `#19`.

## 7. r1106spfx: split regression fixes (SQ-SPFX)

Owner request 2026-11-06 22:14: "run the latest scratch queue, then apply the jars to each instance that needs it
(dev/client, + those needed in port test)". Latest queue section = SQ-SPFX-01 to -07 (CQ-SPFX-01 is the owner's,
client). Targeted run, no perf session (13 GB free).

- Jars: all ten Creative builds of 11-06 22:07-22:09 (prep_srv.sh swaps), Labels 1.0.1 of 11-04 21:53. CEPortTesting/mods
  got the 22:07-22:09 Common, Compat, Calendar, Delight, Brewing at 22:16 (port chain rows; also the owner's install).
- Batches `tools/scratch_query/batches/r1106spfx/` (a1, a2, jx + jx_run.sh, b1, chain_boots.sh); results
  `perf/runs/r1106spfx/<boot>/`. `zq count` takes an optional dimension (8th argument) since this run.
- `loadingscreentips-1.1.0.jar` (Fabric, unlisted in mod_sides.md) left out of every srv.

### Run notes

- 22:15 claim, preflight: no JVM, 13 GB free; unlisted jars: Immersive Portals, Portal Gun, Portal Gels, loadingscreentips.
- 22:18 a1 (A, IP + Portal Gun + Portal Gels kept) CRASHED at world creation, same `MixinChunkMap_C ... cannot reduce
  visibiliy of PUBLIC target method` as r1106spf. Common's `ChunkMapPortalsVisibilityMixin` is mixed in first (debug.log)
  but no `set back to private` line: Mixin checks visibility in `MixinPreProcessorStandard.attach`, which runs when the
  target context is built, before any plugin `preApply`. **SQ-SPFX-01 FAIL: preApply is too late; the fix needs a
  transformer that runs before Mixin (e.g. a Forge JS coremod) or IP's mixin disabled.** Crash in perf/runs/r1106spfx/a1/.
- 22:20 the crashed a1 JVM hung after the crash (stop_wait); IP, Portal Gun, Portal Gels moved out; a1 rows -02, -03a,
  -06 run on the a2 boot.
- 22:22 a2 (IP, Portal Gun, Portal Gels out, fresh world) Done 10.2 s; a2.qb + a1 rows -02, -03a, -06. 36 failed recipes
  (same as r1106spf). SQ-SPFX-01b BATCH-FAULT (`grep -c` exit 1 on 0 = the pass case; fixed). SQ-SPFX-03a: the probe
  prints `built` for the empty-fluid assembly because NCH's G5 now skips quietly (`_absent` returns, counted as
  `[absent mods] skipped 1 recipe rows for absent unknown mod`), and `rdetail zz_probe` lists only zz_probe_good, no
  parse error, no `create:kjs_` recipe: PASS, row text expected a throw. SQ-SPFX-06: no dict Missing list, both info
  lines (block 35, fluid 17); the 15 `is not validated` lines are all NCH ieBottling fluid tags, not dicts: PASS, row
  text too wide.
- 22:24-22:32 SQ-SPFX-05 jx loop (jx_run.sh, same save): magic, equipment, worldgen, brewing, engineering out: Done,
  no ReferenceError / event errors, Skipped lines only for the removed jar (Brewing out also skips Creative Delight's
  `brewinandchewin/*` scripts, gated on `brewinandchewin`); Worldgen out: Devtools `dump_bee_nests.js` /
  `dump_structuregen.js` skipped (fix holds). Delight (+Brewing), Calendar (+CD, CB), Compat (+all) out: FML refuses,
  `Mod ... requires farmersdelight` for every FD addon: Farmer's Delight is nested in the CD jar, so the full pack
  cannot boot without CD (by design, as Compat in r1106spf). Row text: those three belong on the port chain. 22:33
  spfxA deleted; port chain p1, p6, p6f next (chain_boots.sh, CEPortTesting jars of 22:16).
- 22:32-22:36 port chain (chain_boots.sh, r1106cbp batches): p1 Done 6.1 s, **0 failed recipes** (r1106spf p1: 1, the
  horror lasagna), no horror_lasagna error lines: SQ-SPFX-03b PASS. SQ-CBC-01 in p1.qb BATCH-FAULT (`grep -c` over two
  files exits 1 on all-0, the pass case). p6: `mod data packs on: none; off: [felsic_gear, felsic_gear+nullscape]`, 0
  failed, 0 parse errors: SQ-SPFX-04a PASS. p6f: `on: [felsic_gear]; off (mod not loaded): [felsic_gear+nullscape]`,
  no Unbound values, 0 failed: SQ-SPFX-04b PASS. 22:37 b1 (B, seed 8675309, IP jars out) next.
- 22:38 b1 (B seed 8675309) Done 96.8 s. SQ-SPFX-07: `sky_island` at [-752, ~, -912] (1120 blocks), 45 grass blocks at
  y100-250 over that spot (20 on air), 0 below y100; srv structure report lists it in `group/sky` (weight 100). PASS.
  SQ-SPFX-04c: both felsic packs on; crystal_peaks at [688, 108, 1168]; 2x2 chunks: 0 felsic; 10x10 chunks y0-64: 5
  felsic blocks, 1995 end stone (the ore replaces end_stone only at y0-64, and the crystal peaks have little end stone in
  that band): PASS, ore sparse there (tuning note for `felsic_ore_crystal_peaks.json`, not a fault). 22:45 all JVMs
  stopped, scratch OUT dirs deleted.

### Verdicts (r1106spfx)

| ID | Result | Evidence |
|---|---|---|
| SQ-SPFX-01 | FAIL | a1 crash (perf/runs/r1106spfx/a1/): `MixinChunkMap_C ... cannot reduce visibiliy of PUBLIC target method`; no `set back to private` line. `CEMixinPlugin.preApply` runs after Mixin's `MixinPreProcessorStandard.attach` (where visibility is conformed), so it is too late. Second half (IP out): PASS, 0 `m_183760_` lines |
| SQ-SPFX-02 | PASS | `rdetail`: 7 steps (deploy, fill, 5 deploy), result 2 horror_lasagna, 0 error lines (the probe does not print the fluid: CQ-SPFX-01 sees it) |
| SQ-SPFX-03 | PASS | (a) empty-fluid assembly not built (counted in `[absent mods] ... unknown mod`), only zz_probe_good exists, no parse error; row text expected a throw, NCH skips quietly. (b) p1 0 failed recipes, no horror_lasagna error |
| SQ-SPFX-04 | PASS | (a) p6 0 failed, both packs off; (b) p6f `on: [felsic_gear]; off: [felsic_gear+nullscape]`, no Unbound values; (c) full pack: both on, 5 felsic blocks in 10x10 crystal_peaks chunks (sparse) |
| SQ-SPFX-05 | PASS (partial) | Magic, Equipment, Worldgen, Brewing, Engineering out: clean, Skipped lines only for the removed jar; Devtools Worldgen dumps skipped. Delight / Calendar / Compat out: FML refuses (Farmer's Delight is nested in CD; every FD addon needs it): by design, needs the port chain instead |
| SQ-SPFX-06 | PASS | no dict Missing list; `[block_dict] 35` and `[fluid_dict] 17 tag entries not checked`; the 15 `is not validated` lines are NCH ieBottling fluid tags (row text too wide) |
| SQ-SPFX-07 | PASS | locate 1120 blocks, grass at y100-250, `group/sky` in the report |
| CQ-SPFX-01 | open | client, owner |

Totals: PASS 6 (SPFX-05 partial), FAIL 1 (SPFX-01). Batch faults fixed: `grep -c` exit codes (a1/a2.qb, r1106cbp p1
SQ-CBC-01 still has it). Tooling: `zq count` takes an optional dimension.

### After the run (r1106spfx, owner request)

- 22:43 installed in the pack's `mods/` with `installToMods`: all ten Creative jars (Common, Compat, Calendar, Magic,
  Equipment, Engineering 22:43 rebuilds of the same sources, only Common's `unreleased.md` changed since 22:09;
  Devtools 22:07, Delight, Brewing 22:08, Worldgen 22:09). Old jars in `jar_archive/displaced/`.
- 22:44 CEPortTesting/mods: Common, Compat, Calendar, Delight, Brewing replaced with the installed copies.
- CreativeEngineeringClient not touched: it is on the 11-04 payload (`creativeengineering-1.1.08` core, 11-03 kubejs);
  it needs a deploy payload (skill `release`, `apply.sh client`), not loose jars.
- Immersive Portals, Portal Gun, Portal Gels are still in `mods/`: with Serene Seasons a world still crashes at creation
  (SQ-SPFX-01).
