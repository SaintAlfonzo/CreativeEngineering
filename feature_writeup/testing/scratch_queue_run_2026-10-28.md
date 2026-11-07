# Scratch queue run 2026-10-28: recent feature block, then bot players and the perf suite

Owner request (2026-10-28 01:10): run the performance stress-test queue. Block 1 is the recent feature checks
from [scratch_queue.md](scratch_queue.md). Block 2 is the perfBot rows (SQ-PB-01 to -05) and the perf suite. Capture
performance the whole way through. Summaries are saved regularly and never truncated (raw recordings may be capped).
When a bug blocks a test, debug it. When the only fix would change a feature's behaviour, record it here for review
and leave the feature as it is. This file is updated after every step, so it survives a machine crash.

## Status (newest first)

- 08:20 Block 2 done. Suite run `g1` 03:04 to 05:27, every scenario `ok`, no server endings; G09 rerun as `g1s`
  (08:09) with the fixed group S. JFR reports for G02, G03a, G04 in `perf/suite/g1/reports/`. Results under
  "Perf suite g1" below. Run finished.
- 03:25 g1 group S: S00, G08, G10, G11a ok. G09 measured an empty pen (entities 1): group S had
  `spawn-animals=false`, which discards animals (as in SQ-HT-02). Plan fixed (`spawn-animals=true` +
  `doMobSpawning false`); G09 to be rerun as run `g1s` after g1.
- 03:06 Block 2 running: `perf_suite.py run general g1` started 03:04 (runner pid in `perf/suite/g1/runner.pid`,
  about 2 h 15 min). If the machine or this session dies: `python3 tools/perf/suite/perf_suite.py resume g1`, then
  `report g1`. Perf session `q1028` closed and reported. Docs updated: scratch_queue.md Run log, crop_yields.md,
  feature_porting.md FP-10, item_merges.md 2.9, perf_suite.md, perf.md 14, TODO (server #15, #28, crops #23,
  seasoning #6, starter-kit #2).
- 02:55 Block 1 done. Group B rows: SQ-SALT-04, SQ-GFB-03, SQ-FTW-03, SQ-GFB-04 PASS; SQ-WGP-08 deferred. Session
  report refreshed (`perf/sessions/q1028/summary.md`, 8 boots). Next: block 2, perf suite `g1` (SQ-PB-05).
- 02:28 Group B `fq_b_on` (eval seed, FTW on): GFB/FTW census baseline at Done + 2 min, SQ-CM-09 PASS. Found and
  fixed a perfBot bug that would have spoiled the suite: a bot spawned away from its login spot never loaded its own
  chunks (fix verified live; jar rebuilt 02:24). Server restarted with the new jar.
- 02:12 Group A finished (boot 3: animals on, `richFarmlandTries = 0`): SQ-SY-07, SQ-HT-03, SQ-SALT-03 (give half),
  SQ-CM-08 PASS; SQ-HT-02 Regeneration PASS, repair half impossible (finding 4). Session report run between boots 2
  and 3 (`perf/sessions/q1028/summary.md`). Next: group B worlds.
- 02:01 SQ-PB-02 (wheat half) and SQ-PB-03 PASS. SQ-HT-02 not possible on this boot: the scratch setup has
  `spawn-animals=false`, which makes vanilla discard every animal each tick (a summoned horse is gone within 2 s).
  Next: SQ-PB-04 (kill -9), then a reboot with `spawn-animals=true` and `richFarmlandTries = 0` for SQ-SY-07, SQ-HT-02/-03.
- 01:55 SQ-STC-01/-02 PASS, SQ-PB-01 PASS. The bot's join exposed `starter_kit.js` failing on every login (no kit for
  anyone since the 1.1.01 port): fixed (three Rhino/KubeJS API calls, logic unchanged), FP-10 PASS on bots. Changelog
  bullet added (Gear & Combat), lint 0 errors.
- 01:40 SQ-CM-05/-07 and SQ-SY-01 to -06 done. The config switches only take effect after a restart (see Findings 3).
  Boot 2 of `fq_a1` ran with 7 crops on (`Done (4.2s)`). New probe `tools/soil_yield_probe/sy_probe.js` (`/syt roll`:
  Block.getDrops N times on the real blocks, so location_check sees the soil).
- 01:32 Boot 1 `fq_a1` (group A + perfBot jar) up since 01:21 (`Done (10.4s)`); collector sees it (boot 01).
  Recipe and tag rows of SQ-CM and SQ-SALT done; zq probe got `uses` and `makes`.
- 01:20 Prep done. No game JVM, 11 GB free on `/`. The parallel session ("Scratch server performance tests 2")
  handed over the JVM slot; its tooling (collect.sh, session_report.py segments) has no live run yet.

## Plan

| Step | Server | Rows |
|---|---|---|
| 1 | Group A superflat `fq_a1`, perf session open, perfBot jar in `srv/mods` | SQ-CM-01 to -07, SQ-SALT-01 to -03, SQ-SY-01 to -07, SQ-STC-01/-02, SQ-GFB-03; then SQ-PB-01 to -04 and SQ-HT-02/-03 with a bot |
| 2 | Group B normal world, eval seed, `ftw_on` | SQ-CM-08, SQ-CM-09, SQ-SALT-04, SQ-FTW-03 (on half) |
| 3 | Group B `ftw_off` | SQ-FTW-03 (off half) |
| 4 | Group B, `holdGeneratedFallingBlocks = false` | SQ-GFB-04 |
| 5 | Perf suite `perf_suite.py run general g1` | SQ-PB-05 (G01 to G12) |

Between boots, with no server up: `perf_session.sh report` (summary.md, summary.json, worst_ticks.tsv).

## Jars

- patcherMod `creativeengineering-1.1.03.jar`: `mods/` held the 00:01 build, which has no `soil_yield`. Installed the
  00:16 build (`installToMods`, 01:19), so the sources and the jar match.
- Creative Delight 00:16 (same as build/libs). perfBot `cdevperfbot-0.1.0.jar` 00:50 (scratch `mods/` only).

## Tooling changes made in this run

| When | File | Change | Why |
|---|---|---|---|
| 01:15 | `tools/perf/session/session_report.py` | Every tick over 50 ms kept in `summary.json` and a new `worst_ticks.tsv` (was the top 25); the full list of commands from the 60 s before each one is kept; `summary.md` shows the top 50 | Owner: summary data never capped |
| 01:50 | `kubejs/server_scripts/other/starter_kit.js` | Rhino/KubeJS API fixes only (see SQ-SK) | Blocked SQ-SK; player-visible bug: changelog bullet |
| 01:25-01:45 | `tools/scratch_query/zq_probe.js` | New subcommands `uses`, `makes`, `food`, `item`, `potion`, `season` | Queue rows needed them |
| 01:35 | `tools/soil_yield_probe/sy_probe.js` (new) | `/syt roll`: loot rolls on placed blocks | SQ-SY |
| 02:24 | `development/perfBot` `Bots.java`, `BotPlayer.java` | Chunk tracker (`ChunkMap.move`) driven from the server tick event for every bot and right after a spawn teleport, no longer from `BotPlayer.tick()` | Bug: the level ticks an entity only inside entity-ticking chunks, so a bot spawned away from its login spot (world spawn, or its saved position) never ticked and its tickets never followed it: `zq chunk` showed no holder under bot cl at -600 -600 but level 31 at 300 300, its previous login spot. Every bot spawned outside spawn chunks / forceloads was loading chunks at the wrong place (G04's four bots would all have sat at spawn). After: chunk under a new bot at -1200 -1200 at level 31 within 30 s. Group A rows are unaffected (bots inside spawn chunks or forceloads) |
| 02:20 | `tools/perf/eval/entity_census.py` | `--box X1 Z1 X2 Z2`, `--all` | FTW-03 compares the pregen square only; full lists |
| 03:25 | `tools/perf/suite/plans/general.py` | Group S: `spawn-animals=true`, `gamerule doMobSpawning false` in init | G09 measured an empty pen |
| 03:00 | `tools/perf/session/session_report.py` | Skips malformed `proc.tsv` rows | Report crashed on a row written as a JVM exited |
| 01:16 | `tools/perf/suite/perf_suite.py` | `summary.md` rewritten after every result (`write_summary`, atomic replace); `report` calls the same code | Owner: summary saved regularly. Self-test 12/12 PASS after the change |

## Results

| Row | Result | Notes |
|---|---|---|
| SQ-CM-01 | PASS | `[itemMerges] creativedelight: 17 merges, 38 recipes swapped` (was 14 / 29); no script error from item_merges.js / veggies_foods.js / canning.js; `zq makes` canned_hamimelon 0, hamimelon_popsicle 0; `zq uses fruitsdelight:hamimelon_slice` 0 |
| SQ-CM-02 | PASS | `fruitsdelight:hamimelon_seeds` <- `delightful:cantaloupe_slice`; `shaped/mini_cantaloupe`, `shapeless/mini_cantaloupe_seeds`, `shapeless/cantaloupe_seeds_from_mini` present; `shapeless/cantaloupe_seeds` gone. The two popsicle recipes are `creativedelight:shaped/cantaloupe_popsicle` and `..._from_cantaloupe` (Delightful repack ids) |
| SQ-CM-03 | PASS | exotic_fruit holds `delightful:cantaloupe_slice`; `#forge:seeds` holds hamimelon seeds; `#forge:pasta` = raw_pasta, cucumber_noodles, potato_noodle |
| SQ-CM-04 | PASS | `zq uses farmersdelight:raw_pasta`: 0 recipes take it alone, 83 take it through `#forge:pasta` (FD, Brewin', More Delight, Alex's Delight, Vintage, Veggies, Miner's, Oceanic, End's, Ender's, Hostile, Fungi, Rustic, Quark Delight, S&D copies, manual_only) |
| SQ-CM-06 | PASS, one note | shulker_bowl: only `creativedelight:shapeless/shulker_bowl` -> 12; Vintage `bread_from_smelting` / `_smoking` absent; oven `bread_from_dough_1/3/6` accept create dough / sweet potato dough / oat dough; `sourdough_bread_1/3/6` make sourdough bread from `pneumaticcraft:sourdough`; overnight_oats present. Note for review: `creativedelight:farmersdelightcutting/sourdough_bread_to_2_bread_by_fdcutting` (and its S&D copy) cut sourdough BREAD into 2 `minecraft:bread`. Veggies' `bread_from_smelting/_smoking/_campfire_cooking` (sweet potato dough) still load; outside the row |
| SQ-CM-05 | PASS | `zq food`: coleslaw nutrition 11, modifier 0.6, Comfort 2400 t; fermented garlic honey Regeneration amp 1 100 t, Health Boost amp 1 600 t, nothing else |
| SQ-CM-07 | PASS | `felsic_gear:felsic_sword` max damage 2501; `creativeengineering:strong_phantom_bane` not registered; Forge brewing: phantom_bane + glowstone -> nothing, + redstone -> long_phantom_bane, long + glowstone -> nothing (control: swiftness + glowstone brews); no `[CE potions]` error |
| SQ-SY-01 | PASS | all crops off: `[soil-yield] no crop switched on`, no soil_yield.js error; 400 rolls: wheat 1.00 on farmland / Rich Farmland / mulch, seeds 2.6 to 2.8 (1 to 4) |
| SQ-SY-02 | PASS | log `[soil-yield] 7 crops (...), soils: rich farmland 2 x 0.35, fertile soil 2 x 0.25, rich soil 2 x 0.35`; 600 rolls wheat 1.00 / 1.71 / 1.49, max 3; seeds 2.74 / 2.69 / 2.75 |
| SQ-SY-03 | PASS | carrots 3.71 / 4.42 / 4.20 (max 7); unripe age 3 on Rich Farmland 1.00 |
| SQ-SY-04 | PASS | ripe panicles over rice over Rich Soil: 1.77 panicles by hand, 1.70 rice with an iron knife; over dirt 1.00 / 1.00; straw 1 either way |
| SQ-SY-05 | PASS | corn lower 1.70, upper 1.00 (farmland 1.00 / 1.00): extra once per plant; hemp top 3.64 vs 2.84 on farmland, bottom 2.52 vs 2.56; flax lower 1.69 vs 1.00, upper drops nothing |
| SQ-SY-06 | PASS | 1500 rolls on Rich Farmland red 0.72 / green 0.73 / yellow 0.72 (farmland 0.49 / 0.50 / 0.47); `[crop-drops] bell peppers: weights 1:1:1` still logged |
| SQ-STC-01 | PASS (logic) | `zq season` after two `/reload`s: plains, forest untagged (temperate); desert, badlands ARID; snowy plains, ice spikes FROZEN; same both times. HUD not looked at (no client) |
| SQ-STC-02 | PASS (logic) | scratch datapack `creativeengineering:seasons/arid` + plains, forest -> `/reload` -> both ARID at once; file removed -> `/reload` -> back to untagged |
| SQ-PB-01 | PASS | `cdevperfbot ok`; `pbot spawn bot1 0 -60 0` -> normal join (`logged in with entity id`, Paragon handshakes, Distant Horizons `Player [bot1] joined`), no exception from Forge networking / FTB / Lightman's; `list` names bot1; `forge tps` overworld 6.2 ms. The one join error was the pack's own starter_kit.js (below) |
| SQ-SK / FP-10 | FAIL -> fixed -> PASS (bots) | `starter_kit.js` threw on every first join: (1) `Path.resolve('data')` ambiguous overload, (2) `server.isDedicatedServer()` not visible to Rhino on DedicatedServer, (3) `player.getStringUUID()` hidden; the JsonIO calls also passed a File where KubeJS 1902 takes a Path and parsed a Map's toString. Fixed with `resolve(java.lang.String)`, `server.isDedicated()`, `getGameProfile().getId()`, `JsonIO.readString/write(Path)`. After: bot5 got the Engineer kit (copper pickaxe, axe, 8 bread, boots, shield; Animal Dictionary given back), tracking.json lists its uuid, tag set; relog -> nothing; bot6 with its uuid pre-written -> tag only, no kit |
| SQ-PB-02 | PASS (wheat half) | 9 x 9 wheat age 0, `randomTickSpeed 3`, noon, 5 min: 17 of 81 grew beside bot pbA at x 1000, 0 of 81 in a forceloaded field at x -1000 with no bot. The night-mob half runs in the suite (G03b) |
| SQ-PB-03 | PASS | `pbot path pbA 1500 200 10 1.5` -> "path 334 ticks", `path_done:pbA` at tick 23972 (about 334 after the command); status-line chunks 4508 -> 3606 -> 3868 -> 4129 -> 4332 as the bot moved; `census-t.json` written |
| SQ-HT-02 | Regeneration PASS; repair N/A (finding 4) | boot 3, bot hb at spawn, tamed horse with `dragon_horse_armor{Damage:500}`: Regeneration amp 0 within 12 s; Damage stays 500 because the armor has max damage 0 (HorseArmorItem, never damageable), so `repairIfDragonGear` skips it |
| SQ-HT-03 | PASS (via Regeneration) | horse chunk forceloaded, bot to the Nether 30 s: horse has no effects (handler skipped the overworld with no player); bot back: Regeneration again within 12 s |
| SQ-SY-07 | PASS | `richFarmlandTries = 0` + restart: log lists only `fertile soil 2 x 0.25, rich soil 2 x 0.35`; wheat on Rich Farmland 1.00 (farmland 1.00, mulch 1.45), carrots 3.70 |
| SQ-PB-04 | PASS | kill -9 of the scratch JVM: tick file ends `t,24760,8502` / `s,...,24760,...` (complete lines, last second); `jfr assemble` of `perf/jfr-repo/<dir>` -> 54 MB, 4 chunks, 1390 s. Collector: killed boot got `repo/`, clean boot got its `hotspot-pid-*.jfr` moved (PS-02), two boots of one srv = two folders (PS-03) |
| SQ-SALT-03 | PASS (give half), evaporation open | `give hb salt:salt 3` -> 3 `vintagedelight:salt_dust`; `give hb vintagedelight:salt_block 1` -> `salt:salt_block`. Cauldron evaporation x 20 not run |
| SQ-CM-08 | PASS | full `create_deep_dark:echo_armor_*` set on bot hb: Night Vision amp 0, `better_beacons:phantom_bane` amp 0 |
| GFB/FTW baseline | recorded | `fq_b_on` Done + 2 min, `save-all flush`, `entity_census.py`: 279 entities, 112 falling blocks (61 sand, 26 red sand, 25 gravel; ov2 had 102), 73 items (26 gravel, 14 moss carpet, 7 glow berry pips, 6 bamboo...), 64 items below y 50. Raw: scratchpad `census_b_on_done2.txt` |
| SQ-CM-09 | PASS | `creativedelight:wild_crops/cantaloupe` = 15 biomes (badlands x3, desert, Terralith desert / canyon / oasis / spires / painted mountains / sandstone valley...), no savanna; 10 `/place feature creativedelight:wild/mini_cantaloupe` in the desert at 1936 825 (4 at y 64 plus 1 failed-then-surface retries, 1 at the bot): 27 `delightful:cantaloupe`, all on `minecraft:sand` |
| SQ-SALT-04 | PASS | beach at 112 -39 (bot sb present), 4 `/place feature vintagedelight:patch_salt` (others hit water): 22 `vintagedelight:salt` layers (on sand 14, raw rock salt 6, dirt/grass 2) and 98 `salt:raw_rock_salt_block`, 0 `salt:salt_block`, 0 `vintagedelight:salt_block`; 5 min at `randomTickSpeed 300`: 22 and 98 unchanged. Cost: overall MSPT 214 ms / 4.7 TPS during the 300x soak (finding 6) |
| SQ-GFB-03 | PASS | `zq floating 45 56 64` found 10 generated gravel blocks over air near spawn (held, as designed); cobblestone placed beside 2 35 17 -> it fell; air set beside 9 22 98 and 14 16 -1 -> both fell (9 22 98 was a two-high stack: the upper block slid into its place) |
| SQ-FTW-03 | PASS | chunky r120 at 4000 4000 (289 chunks, 30 s), forceload 3888..4112 90 s, census of the square: `fq_b_on` 2 items (kelp, y 63), 0 below y 50; `fq_b_off` 40 items (19 `miners_delight:gossypium`, 18 `cave_carrot`, seed, cornflower, mushroom), 38 below y 50. Falling blocks 0 in both |
| SQ-GFB-04 | PASS | `fq_b_gfb` (`holdGeneratedFallingBlocks = false`) Done + 2 min census: 233 falling blocks (110 gravel, 69 sand, 54 red sand) vs 112 in `fq_b_on`; near the ~190 vanilla figure. Items 80 (72 below y 50) vs 73 |
| SQ-WGP-08 | not run | Builds the `wgp_cl_seam` world only for the client seam check CQ-WGP-01; run both together in the client session |
| SQ-PB-05 | PASS | `g1` every scenario `ok`, no server endings, `summary.md` written; G02 5.55 ms (expected 5-6). See "Perf suite g1" |
| SQ-PB-02 night half | PASS | G03b census: 75 hostiles around the bot (26 creeper, 19 zombie, 13 skeleton, ...) |
| SQ-SALT-01 | FAIL (2 leftovers) | PASS parts: `[itemMerges]` counts include the salt pairs, `zq makes salt:salt` 0, `makes vintagedelight:salt_block` 0, recipe id `vintagedelight:salt_block` makes `salt:salt_block` from salt dust, salt_packing / salt_block_deconstruct / salt_from_smelting / salt_bucket_to_salt / `salt:salt_lamp` gone, `shaped/large_salt_lamp` present. FAIL: (1) `vintagedelight:fermenting/{ochre,verdant,pearlescent}_froglight_from_fermenting` still take `vintagedelight:salt_block` alone; (2) `tactical_fishing:fishing_vintagedelight_salt_bucket` makes `vintagedelight:salt_bucket` |
| SQ-SALT-02 | PASS (tags), Salting craft open | salt dust is in `#forge:salt`, `#forge:salts`, `#forge:dusts`, `#forge:dusts/salt`; `zq uses salt:salt` 0. `salt:salting` is a special recipe: needs a crafting-grid probe or the client (MG-32) |

## Perf suite g1 (SQ-PB-05)

`perf/suite/g1/summary.md` (rewritten after every result), `results.jsonl`, `jfr/<ID>-1.jfr`, `census/`. Same jars
as block 1, perfBot 02:24. No CONTENDED rows. Baseline for later runs (perf_suite.md 10.1).

| ID | Scenario | MSPT mean | p95 | max | >50 ms | TPS | Entities | Extra |
|---|---|---|---|---|---|---|---|---|
| G01-S | boot, new flat world | | | | | | | 62.1 s to Done, `Done (10.2s)` |
| S00 | one bot, empty flat site | 10.33 | 11.18 | 13.2 | 0 | 20.0 | 1 | |
| G08 | 500 item entities | 14.47 | 15.35 | 22.3 | 0 | 20.0 | 501 | |
| G09 (g1s) | 200 farm animals in a pen | 16.20 | 17.33 | 30.0 | 0 | 20.0 | 201 | g1's own G09 measured an empty pen (spawn-animals=false), discarded |
| G10 | 100 hostile mobs, dark box | 12.85 | 15.55 | 102 | 1 | 20.0 | 101 | |
| G11a | 1024 hoppers | 13.84 | 22.18 | 97 | 1 | 20.0 | 1 | |
| G11b | 256 furnaces | 10.71 | 11.49 | 15.5 | 0 | 20.0 | 1 | about S00: check the furnaces actually smelt before trusting it |
| G01-N | boot, new normal world | | | | | | | 128.4 s to Done, `Done (76.7s)` |
| G02 | idle, no player | 5.55 | 6.17 | 8.1 | 0 | 20.0 | 288 | matches perf.md 12.1 (5-6 ms) |
| G03a | one bot at spawn, day | 18.54 | 24.9 | 131 | 2 | 20.0 | 401 | |
| G03b | one bot at spawn, night | 16.29 | 21.08 | 126 | 4 | 20.0 | 357 | census: 75 hostiles around the bot (PB-02 night half PASS) |
| G04 | four bots 600 apart | 50.10 | 61.16 | 1837 | 2428 | 19.9 | 1098 | 7938 chunks loaded; at the tick budget |
| G06 | save-all flush, four bots | 49.91 | 62.28 | 123 | 456 | 18.4 | 1046 | flush 4.95 s |
| G05a | travel 30 b/s, pregenerated | 29.02 | 51.77 | 220 | 112 | 17.6 | 362 | |
| G05b | travel 14 b/s, pregenerated | 29.73 | 40.85 | 115 | 35 | 20.0 | 472 | |
| G07 | /reload, one bot | 18.45 | 24.54 | 330 | 1 | 17.5 | 425 | command 11.6 s, log quiet 1.4 s after |
| G12 | soak, two bots, 45 min | 31.95 | 40.05 | 1704 | 236 | 20.0 | 835 | no drift (slices 33.7 -> 31.0 ms); heap floor 5.4 to 5.8 GB of 6 GB; two 1.7 s ticks about 20 min apart |
| G01b | restart, twice | | | | | | | 60.2 s, 57.1 s |

What G04 spends its time on (JFR window, `reports/G04.html`): overworld 91% of the tick; entity tick 52.8% (vanilla
hostiles and bees first: zombie 6.4%, skeleton 4.9%, bee 4.6%, creeper 4.5%, wither skeleton 3.8% of tick time;
four players = four mob caps), random ticks 15.9% (Immersive Weathering 17% of that phase), chunk system 8.8%
(ModernFix `getTickingChunk` 21% of that), Forge tick events 5.9% (Lootr TileTicker, Serene Seasons). Pack mods by
server-thread samples: Alex's Mobs 4.1%, Immersive Weathering 3.0%, ModernFix 2.7%, Serene Seasons 2.0%, Nock Enough
Arrows 1.7% (`TemptByWhistleGoal#canUse` on every mob), Regrowth 1.6%, Domestication Innovation 1.5%, KubeJS 1.3%
(`tool_effects.js:49` and `armor_effects.js:47` PlayerEvents.tick, 0.39% and 0.33% of tick). GC: 660 collections,
7.4 s paused in 5.5 min, longest 1784 ms = the 1837 ms tick. Bees tick underground (y -27, y 0) at several spots.

## Findings for review (not fixed: would change feature behaviour)

1. **Salt merge leftovers (SQ-SALT-01).** The swap missed the custom recipe types `vintagedelight:fermenting` (3
   froglight recipes take `vintagedelight:salt_block`) and `tactical_fishing:fishing` (catches `vintagedelight:salt_bucket`).
   A player can no longer get a Vintage salt block (it swaps on pickup), so the froglight recipes are dead.
3. **Soil yield switches need a restart, not `/reload` (SQ-SY). Docs fixed 2026-10-28.** Forge's watcher did not reload
   `creativeengineering-common.toml` on the running scratch server, neither after `sed -i` (new inode) nor after an
   in-place rewrite. `/reload` then still logged `no crop switched on`. Restarting works. The queue rows say "and
   `/reload`"; players changing the config on a live server meet the same thing. Doc fix only: rows and
   crop_yields.md should say restart.
4. **Dragon horse armor cannot be repaired (SQ-HT-02).** `dragonloot:dragon_horse_armor` has max damage 0, so the
   horse branch of `dragon_autorepair.js` never changes anything and the row's "Damage 450 or lower" cannot happen.
   Either drop the branch and the row, or give horse armor durability (a feature change). Owner's call.
5. **Session collector: "no clean stop" on a boot followed at once by a reboot of the same srv.**  (Documented in perf.md 12 as a known limit.) The final
   `latest.log` copy is skipped when a newer boot owns it, so the summary's Ended column is wrong for those boots
   (boot 01). Cosmetic; the rotated `logs/*.log.gz` holds the real end. Tooling, parallel session's file.
6. **`randomTickSpeed 300` near a beach costs 214 ms per tick (SQ-SALT-04).** Test setting, not play; the JFR of
   boot `fq_b_on` r2 (segment "SQ-SALT-04 randomTickSpeed 300 x 5 min") shows what dominates. For the suite's C24
   (salt at scale) if it is approved.
7. **`/reload` takes 7 to 9 s of server thread every time** (16 reloads in group A, all the multi-second ticks in the
   session summary). Known (perf suite G07 measures it); listed so the numbers are not mistaken for regressions.
8. **perf_suite.md 4 says bots fall.** They don't: a server-side player gets no movement physics without a client
   (bot cm stayed at y 150); use `spreadplayers` to put one on the ground. Doc fix.
9. **Heap: 6 GB is nearly full with two to four players (G04, G12).** The heap floor never fell below about 5.4 GB
   of 6 GB, and the 1.7 to 1.8 s ticks in G04 and G12 are GC pauses (G04: longest pause 1784 ms). Next: the
   heap-after-GC chart in `reports/G04.html`, a G12 rerun at `-Xmx8G` to see whether the spikes go, and what holds
   the live set (a heap histogram on the scratch server).
10. **Four spread-out players reach the 50 ms tick budget (G04, G06).** Mostly vanilla mob AI under four mob caps,
    then random ticks (Immersive Weathering) and the chunk system. Candidates to look at: mob caps / despawn
    distance on the real server, Nock Enough Arrows' whistle goal on every mob, bees ticking underground.
11. **Suite plan bug (fixed): group S had `spawn-animals=false`,** so G09 measured an empty pen. Now
    `spawn-animals=true` with `doMobSpawning false`; G09 rerun in `g1s`.
2. **Sourdough bread cuts into plain bread (SQ-CM-06 note).** `creativedelight:farmersdelightcutting/sourdough_bread_to_2_bread_by_fdcutting`. Intended: the 1.1.04 changelog says "Sourdough Bread cuts into 3 Bread with a knife" and `pneumaticcraft/recipes.js` makes 3; the id seen says `_to_2_`, so recheck the count in the recipe dump.

## Perf summaries

- Perf session: `perf/sessions/q1028/` (summary.md, summary.json, worst_ticks.tsv, boots/NN/report.html).
- Perf suite: `perf/suite/g1/` (summary.md rewritten after each scenario, results.jsonl).
