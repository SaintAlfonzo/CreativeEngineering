# M2 scratchpad: 7.4 tests ST-170..190 on a scratch server

## State (keep current)

- Constraints (user 2026-10-14): ignore Nether/End (ST-186, Nether/End part of ST-171 skipped); ignore
  structure-to-structure rates (ST-181 split + --check, ST-185 ratio skipped); correctness only. Sleep an hour if
  the token limit gets close.
- Milestone: M2 DONE 10:45 (writeup 02_new_mods_tests.md, inline results in structure_tuning.md). Next: M3.
- SESSION 2 (54725a7d, 2026-10-14): the old srv/ (session 22443b35) was gone; rebuilt in session 54725a7d scratchpad
  srv/ the same way (373 mods: mod_sides server/both + the 4 unlisted campsite_structures, restApi, rsec, unlitcampfire;
  patcherMod = build/libs creativeengineering-1.0.6.jar of 01:28). Probe rewritten: /zp starts cx cz r | blocks x1 y1 z1
  x2 y2 z2 | biome x y z (loaded chunks only). Helper scratchpad/cmd.sh "<cmd>" [wait].
- World st_d, seed 8675309, max-tick-time=-1, view/sim distance 6, port 25599, asyncLocatorThreads = 4.
- Scratch toml (srv copy only): earth_fill log = ALL, meta_structures log = true, placement_priority log = true.
  Soft rule loosened: [worldgen.debug] structureDensity 1.0 -> 4.0 (7.4.8; srv copy only, instance toml untouched,
  nothing to put back).
- Remaining: ST-171, 172, 173, 174, 175, 176, 177, 178, 181 (log part), 182, 183, 185 (locate part), 187 (drum tower),
  188 (illusioner + llama need a player -> game). Done: 170, 179, 180, 184, 189, 188 Ser Marrow part.
- Running: nothing (stopped 10:38). M2 scratch work done; next: writeup 02_new_mods_tests.md + inline results, then M3.

## Log (append-only)

- 2026-10-14: server dir built, toml logs set, probe written.

- 02:45 boot 1: world load FAILED: `Error loading registry data: No key name in MapLike` for 4 eugene template pools
  (iron_golem, red_roof_villagers, ser_marrow, undead_castle_enemies): 1.20 pools have no "name", 1.19.2 requires it.
  Server hung after the failure (killed). FIX: "name" added to the 4 files; porter adds it to every pool missing one;
  new tools/check_worldgen_json.py (1.19.2 required keys for pools/sets/jigsaw structures, 266 files OK) wired into
  test_worldgen_tools.sh as step 9. Also seen: 61x "Adding duplicate value NoneBiomeModifier" ERROR lines (vanilla
  registry noise for the shared forge:none instance; harmless).
- 02:47 boot 2 Done (94 s). Load lines: [structure-tuning] 15 groups / 178 sets / 30 fits for 181 / 22 metas / 242
  ranked; biome factors for group/vehicles {#harsh=1.0, default=0.5}; [placement-priority] 104 peaceful keep 70 hostile;
  [meta-structures] 21 metas take slots from 164 anchors. ERROR: predicate eugene:ranger_spawn "Unknown type
  minecraft:all_of" (1.20). FIX: file is now a JSON array (1.19.2 AND); porter converts all_of -> array, any_of ->
  alternative. /reload: "Reloaded with no KubeJS errors", no eugene error (rest = old EveryCompat/tag noise).
- 02:49 /locate batch from spawn (-80 76), 4 AL threads: hits eugene:camp (-2707 1588, inside meta/house_camp, 76 s),
  pillager_loot_carriage (-2832 4400, 67 s). Timeouts (120 s): 5 Campsite camps (37-38 misses each), farmer 15, fisher
  16, ruined_drum_tower (46). Server ran 89 s behind during the batch. The last 6 commands of the batch were lost
  (never reached the console; sent again at 08:03, all answered in 1-3 s): fake_trader_camp 848 -1408, pillager_camp
  -160 832, undead_castle 2576 -2992, red_roof_castle -683 2534, witch_wagon -4432 -1136; eugene:castle "Could not
  find" (in no set, correct). Server then sat idle 02:53-08:03 (no hang: jstack shows idle server + AL threads).
- Seen in passing: eugene:camp [-285,-46] dropped T&T mimic_desert and a reinforced battering ram within 3 chunks
  (peaceful radius works for it). Campsite camps generate as meta parts (fortified_camps on a red roof castle at
  [-84,-221]; farmers_camp [144,-119]). Fisher camp [-118,6] CANCELLED by earth fill (26 over water, limit 24).
- Probe fix: KubeJS level.dimension is a property; commands only re-register on full /reload. zp ents ignores its box
  (Rhino AABB binding?): use vanilla `execute if entity @e[type=..,x,y,z,dx,dy,dz]` counts instead.
- eugene:camp (in house_camp): trader_camp piece at -2711 100 1584, built on the surface (105), chest -> lootr_chest;
  1 trader llama in place; the trader walked 44 blocks off (template trader not leashed; mod behaviour). Template
  entities fine.
- pillager_camp -160 832: forest; pillager 1 + vindicator 1 present.
- undead_castle [161,-187] alpine_highlands (land/highlands), earth fill huge/beard kept (0 over water, 180 air of
  320); pieces: castle + 4 zombie_knight + 5 skeleton_archer + ser_marrow; mobs: zombie 3, skeleton 6, ser_marrow 1.
  (The start bbox includes the 12-block beard pad: zp "under" samples below it are aquifer water, not the castle.)
- red_roof_castle -683 2534: anchor of meta/town_villages at [-44,157] (16 pieces), hot_shrubland (land/plains);
  earth fill large/beard kept (3 over air). Another one anchors fortified_camps at [-84,-221] (with eugene:camp,
  circus tent, a Campsite camp).
- Ser Marrow (ST-188): no player -> the HP score is never set -> no mount; forced the score to 150. BUG: the copy was
  not riding (untamed skeleton horse throws a mob rider off within seconds; the original re-rides every tick with
  /ride). Repro: plain `summon skeleton_horse {Passengers:[skeleton]}` rider off in 3 s; with Tame:1b stays on.
  FIX: mount_ser_marrow summons the steed with Tame:1b. Retest: Ser Marrow at horse y + 0.41 and moved with the steed
  on a tp. PASSED after fix. Note: his armor carries 1.20 Trim NBT (ignored by 1.19.2).

- 08:19 witch_wagon -4432 -1136 mangrove_swamp (land/swamp): piece witch_wagon; purple panes 5, urn 1, brewing stand,
  witch 1. pillager_loot_carriage -2832 4400 bryce_canyon (land/badlands): piece pillager_loot_carriage, pillager 2 +
  ravager 1. fake_trader_camp 848 -1408 temperate_highlands (land/highlands).
- red roof castle (-704 95 2512 .. -663 149 2556): bookshelf 168, lantern 26 + chain 8 (hanging-sign remap), no sign,
  no zombie; 11 red_roof_villagers pieces -> 11 MCA villagers (6 male, 5 female; MCA replaces vanilla villagers),
  iron golem 2 of 3 pieces, sheep 3, cow 2.
- ST-180: forceloaded chunks -55..-33 x 146..168 (529): 1 companion at [-45,150] (T&T village_sparse_jungle, 7 chunks);
  closest village piece (a street terminator) 6 blocks from the castle piece, none overlapping. 50 starts in the area,
  none hostile (only underground sets + the village + the town).
- ST-179 extras: red roof castle tags = type/castle, group/settlements, land/plains, land/forest, meta/fortified_towns
  (not type/village -> not in earth_fill/prune); sim way_sign_destinations has it; live
  `execute positioned -683 100 2534 run locate structure #supplementaries:way_sign_destinations` -> meta/town_villages
  at -704 2512 (the castle's meta).
- Log tallies (since boot): siege metas assembled with 22 siege-weapon parts (ballista 4, battering ram 6, catapult 5,
  siege tower 5, trebuchet 2); 4 pillager camp metas anchored on eugene:pillager_camp; fortified_camps 4/4, town_villages
  4; no "over 100%" share warning. waysign_house_camp 0 of 9 and waysign_camps 0 of 2 assembled (7x friendly camp part
  0 of 1, 4x way sign part 0 of 1) -> open issue.
- ST-189: `loot spawn` of eugene:chests/carriage_chest (21 items) and trader_camp_chest (3): the map's village search ran
  on an AL thread in 86 ms, no stall (AL's "couldn't find block entity on chest" = no real chest with /loot spawn).
- Carriage locate misses 19/20 loads: only 2 explained by drops (peaceful farmers_structures:cooks). Misses can be
  normal "maybe" predictions; running `/cwglocate census 64 6000 -6000` (44750 slot chunks) for false negatives.

- Earth fill (st_c, unique starts): Campsite cartographer 1 / cleric 2 kept (medium, beard); fisher [-118,6] CANCELLED
  (26 over water); eugene:camp 6, pillager_camp 6, fake_trader_camp 1, carriage 2, witch_wagon 1 kept (small, beard);
  red_roof_castle 2 kept (large); undead_castle 1 kept (huge); ruined_drum_tower 1 kept + 1 CANCELLED, both "small,
  10 earth". Drum tower template (same as the original mod's) is a 49x45 base of polished andesite (19k of 27k
  blocks): earth fill sizes and judges starts by EARTH base blocks (dirt/sand/gravel/clay/path) only, so a big stone
  base is "small" and mostly invisible to it -> open issue (ST-176 partly).
- 08:27 `/cwglocate census 64 6000 -6000`: 44750 slot chunks, 1712 starts, 0 misses, 0 false negatives, 387 s. BUT every
  structure row is all "known" (predictedMaybe 0, predictedNo 0): no slot went through the prediction path, so false
  negatives could not be seen. Census tooling issue (Phase 6 / LO-), not 7.4 -> open issue.
- ST-185 direct check: from 100 blocks off known starts, /locate eugene:pillager_loot_carriage -> -2832 4400 (the known
  one) and eugene:witch_wagon -> -4432 -1136 (the known one): the prediction with biome factors says "maybe" where they
  are. (`locate structure #creativeengineering:group/vehicles`: no such structure tag; group/* are not real tags.)

- SESSION 2, 09:06 boot 3 (world st_d, density 4): Done in 291 s. Load: `DEBUG density x4, 15 groups, 214 structure_sets
  written (143 emptied into groups), 199 original sets; ... 30 biome fit(s) for 181 structure(s); 22 meta`; 3 subterrestrial
  sets hit their densest grid (stay below x4, fine). placement-priority 104 peaceful in 2 sets keep 70 hostile, 242 ranked.
  Already in the spawn chunks: `dropped eugene:pillager_camp`, `eugene:fake_trader_camp`, `meta/pillager_great_tower_siege`
  at [-10,-9] within 3 chunks of peaceful campsite_structures:armorer_campsite [-7,-9] (ST-173/174 evidence).
- 09:13 Chunky radius 1200 around 0 0 was ~45-60 min at 5-8 cps; cancelled at 09:14 and restarted at radius 800
  (100x100 chunks, ~10.2k) for a shorter run.
- Offline analysis tools (scratchpad): starts.py <world> <out.json> reads every valid start from region files (id,
  chunk, pieces with pool location + BB, biome at the start chunk's (0,0) column at WORLD_SURFACE height and at y64);
  check.py <starts.json> = biome-fit check (union of the structure's fits from the node sim's fits.json, --density=4)
  + peaceful radius check exactly as PlacementPriority.withinRadius (piece boxes, r*16 blocks, XZ, touching counts) +
  nearest-peaceful/hostile per new start. sim/ = node tools/structure_tuning_sim.js report out --density=4.
- Lead (older system, not 7.4): sawmillhouse:sawmill start [-7,3] reads minecraft:beach at its (0,0) column, fits are
  land/forest/dark_forest/taiga. Sample point may differ from the generator's check point; look again on the full area.

- 09:39 pregen 1 done (radius 800 around 0 0, 10201 chunks, 24:48). save-all, starts.py -> 2606 starts. The area is
  76% water (7780 of 10201 chunks ocean/river), so few land starts: 10 peaceful / 6 hostile, 0 radius violations;
  new: armorer_campsite [-7,-9] old_growth_birch_forest, fisher_campsite [-36,-7] terralith:gravel_beach,
  ruined_drum_tower 1; all three in fit. Out-of-fit starts (older structures): 4 skyvillages over ocean (land/sky fit:
  sky villages float, biome read at their column is the ocean below -> probably my sample point, check), 2
  philipsruins:ocean_fortress in deep_ocean (fits.json has no ocean/deep list = checker gap, not a fault), sawmill on beach.
- Toml note: Forge "corrected" the scratch toml on boot 3 = only added the 1.0.6 earth-fill keys (beardReach 6,
  buryReach 10, maxMoundHeight 8, prune true, sizes) and dropped maxOverAir (renamed in 7.3.1). Test values intact.
- 09:41 /zp biomemap x0 z0 x1 z1 step added (biome source getNoiseBiome at y64, NO chunk loads): -4000..4000 step 128.
  Best land window: centre 2144 -2592 (92% land, 35 biomes incl. swamp, mangrove, dark_forest, badlands, beach, river).
- GOTCHA: /reload removes Chunky's commands ("Unknown or incomplete command"); restart needed. 09:45 stop (clean, no
  kill), boot 4 on st_d. boot-3 latest.log kept as scratchpad boot3_latest.log.

- 09:48 boot 4 Done (11 s), pregen 2 started: Chunky radius 800 around 2144 -2592.
- Checker fixes: (1) fits.json lists can hold biome tags (#minecraft:is_ocean in land/sky, #minecraft:is_deep_ocean =
  ocean/deep), now expanded; (2) biome sample point: 1.19.2 JigsawPlacement puts the generation stub at the CENTRE of
  the start piece's box (y = its floor), and isValidBiome reads there; starts.py now samples that point (chunk (0,0)
  for non-jigsaw). Rerun on area 1: 0 out-of-fit starts of 2606. The sawmill "beach" lead was my sample point
  (corner of chunk [-7,3] vs piece centre), CLOSED.

- 10:22 pregen 2 done (radius 800 around 2144 -2592, 10201 chunks, 33:43). Both areas: 5530 starts. check.py:
  0 out-of-fit starts (all structures), 78 peaceful / 26 hostile starts, 0 peaceful-radius violations. New starts saved:
  campsite armorer 1, butcher 2, cartographer 3, cleric 1, farmer 1, fisher 1; eugene:camp 3, fake_trader_camp 2,
  pillager_camp 1, ruined_drum_tower 3; metas with eugene/campsite parts: house_camp 3, waysign_camps 6,
  waysign_house_camp 1, town_villages 1, pillager_tower_siege 3. No eugene:castle start anywhere.
- Vehicles: 2 standalone siege towers saved ([158,-181] plains, [182,-143] arid_highlands), 14 unique vehicle drops
  (all siege weapons: 11 by peaceful radius 3/5/8, 2 catapult overlaps); ~16 rolled vs ~15 expected from the grid
  (26x26 at density 4, x0.5 ordinary). No carriage/wagon rolled here (weights 100 and 200x0.76 of 900): rarity, not a fault.
- Castles: red roof castle 5 earth-fill lines, all kept (large; 0-8 over air), every one a meta anchor (town_villages x4,
  fortified_ruins x1 at [50,-83] = new ST-180 evidence). Undead castle 5 lines: 4 CANCELLED (huge, beard; over air
  683/314/407/514 of 320, mound columns 448/264/0/341 of 128), 1 kept [146,-222] then dropped by the peaceful radius
  (forest_watchtower, 3 chunks). Terrain under 2 of the cancelled ones (48x48 around the start chunk,
  MOTION_BLOCKING_NO_LEAVES): [138,-208] 77..107 (p10 88, p90 105) = real slope; [184,-158] 78..93 (p10 79, p90 91),
  cancelled on mounds (264 of 128) with over-air 314 under its 320. -> open issue: undead castle survives rarely.
- Earth fill (unique, all logs): kept camps all 0 over water and 0 over air (armorer 5, butcher 5, cartographer 9,
  cleric 5, farmer 4, fisher 2 of 3, eugene:camp 14, fake 19, pillager 27); campsite = medium/beard, eugene camps =
  small/beard, red roof = large, undead = huge, drum tower = small (stone-base issue). Fisher [124,-115] CANCELLED (26
  over water of 24, a shore); drum tower [168,-110] CANCELLED (10 over air of 24... mound columns 10 of 8).
- ST-171: entities scan (scratchpad ents.py over st_d/entities): 14 campsite_structures:camper, all inside a Campsite
  piece (12 + meta parts), 0 outside any start. All 6 camps generate in fitted biomes (pregen, stronger than /locate).
- ST-172: pieces: eugene:camp = trader_camp 1 + trader_camp_lost_llama 2; pillager_camp = pillager_camp only (also 3 in
  pillager_tower_siege metas); fake_trader_camp = trader_camp_fake only; waysign_camps parts trader_camp 7 + lost_llama
  1. Mobs within 64 blocks (near.py): camps have their trader (+2 llamas), fake camps trader + 2 llamas, pillager camp
  pillager 1 + vindicator 1 (mobs walk a few blocks out of the piece box). MCA villagers near the fake camp [149,-203]
  are wanderers (no village start within 10 chunks; watchtower 49 blocks off, radius 48).
- ST-187: template palettes (tplpal.py): tents feature_tent1/2 dark_oak_fence 2 each; wagon purple pane 5, urn 1;
  red_roof bookshelf 168, lantern 26, chain 8, gravel 41; drum tower gravel 260, sand 39, bookshelf 29, urn 5; only
  vanilla + supplementaries:urn ids. In world (forceload + /zp blocks) drum tower [154,-174] plains: gravel 260, sand 39,
  bookshelf 29, spruce_fence 65, birch_fence_gate 8, urn 5, chiseled/cut red sandstone 16/15 = template exactly.
- ST-182: /place eugene:pillager_loot_carriage x8 along z -2392 (forceloaded): 5 carriage (ravager, 2 pillagers, 2
  frames), 2 golem van (iron golem, 4 pillagers), 1 chase (5 pillagers + villager -> MCA female). Pool 2/1/1. Vanilla
  "Hanging entity at invalid position" WARN per carriage (item frames read before the template transform; noise).
- ST-183: surviving siege towers 10 and 15 chunks from the nearest settlements start (exclusion r4).
- 10:31 ST-185: 10 vehicle /locates from 10 points (scratchpad loc_batch.txt), answers to be checked in region files.

- 10:34 ST-185 answers: siege_tower [158,-181] plains, carriage [90,-88] fractured_savanna (3 of the 10 searches),
  ballista [141,-276] birch_taiga, witch_wagon [266,-101] mangrove_swamp, carriage [251,-167] desert (chase variant),
  trebuchet [244,-206] desert; catapult from (1000,-2600) timed out (4 misses), battering_ram from (600,-2000) timed out
  (16 misses of 16 loads). save-all + ANY=1 starts.py: every answer chunk holds the start. Biomes from the biome
  source at the piece-centre floor (/zp biomesrc; region biomes of structure_starts-status chunks are placeholders):
  all in fit, none harsh. Load line: biome factors {#harsh=1.0, default=0.5} for group/vehicles.
- ST-173/174 drop tallies (unique): peaceful Campsite/eugene:camp keep away a witch hut, 2 dungeons, a battering ram,
  outposts, a pillager tower, a great-tower siege meta, pillager camp, fake trader camp (r3). Hostile camps dropped:
  pillager_camp 19 (r3 houses/camps/metas 12, r5 village/village_towers 5, r8 town_villages 1...), fake_trader_camp 15
  likewise; undead castle 1 (r3 watchtower). No eugene error lines in either boot.
- 10:38 server stopped clean (no kill). boot-4 log = scratchpad boot4_latest.log. M2 scratch work DONE.

## Results

| Test | Result | Evidence |
|---|---|---|
| ST-179 | PASSED | no hostile start in 23x23 chunks around it; nothing overlaps; type/castle (never pruned); way-sign destination (sim tag + live locate answers its meta); earth fill large/beard kept (3 over air) |
| ST-180 | PASSED | red roof castle anchors meta/town_villages [-44,157] and fortified_camps [-84,-221]; companion village at [-45,150], closest piece 6 blocks off the castle, no overlap |
| ST-184 | PASSED | 6 tower / 2 great tower / 7 stronghold / 4 camp siege metas assembled, 22 siege-weapon parts; 4 camp metas anchored on eugene:pillager_camp |
| ST-189 | PASSED (via /loot) | map's village search async in 86 ms, no stall; real chest-open path needs a player (game) |
| ST-170 | PASSED (after 2 fixes) | world loads after the pool-name fix; load counts exact (15 groups, 181 fits, 104 peaceful / 70 hostile incl. vehicles, biome factors staged); no eugene function/predicate error after the all_of fix, /reload clean; 1.1.1 jar not in mods |
| ST-171 | PASSED (Overworld) | 2 pregen areas (20.4k chunks, density 4): 14 campers, all inside Campsite pieces, 0 loose; all 6 camps saved in fitted biomes. Nether/End skipped (constraint) |
| ST-172 | PASSED | camp = trader + lost llama templates only, pillager_camp = pillager template only, fake = fake template only; mobs present (trader+llamas, pillager+vindicator) |
| ST-173 | PASSED | drop lines with Campsite camps / eugene:camp as the peaceful side (witch hut, dungeons, battering ram, outposts, hostile camps); 0 radius violations over 78 peaceful / 26 hostile starts |
| ST-174 | PASSED | pillager camp 19 / fake camp 15 drops at r3 (houses, camps, way-sign metas), r5 (villages, village_towers), r8 (town_villages); 0 violations |
| ST-175 | PASSED | every kept camp 0 over water, 0 over air; all camp starts in fit (0 out-of-fit of 5530 starts); fisher on gravel beach, 1 shore fisher cancelled (26 over water) |
| ST-176 | PARTLY | sizes sensible (Campsite medium, eugene camps small, red roof large, undead huge); undead castle cancelled 4 of 5 over slopes (real relief 30 / 12-15 blocks); drum tower "small" from its 10 earth base blocks (stone-base issue) |
| ST-177 | PASSED ([game] boss bar left) | no eugene:castle start; drum tower 3 in fit; red roof castles anchor metas; undead castle with knights/archers/Ser Marrow (st_c); red roof villagers + golems, no zombie (st_c) |
| ST-178 | PASSED | in group/surface_dungeons (report); dropped within 3 of a watchtower; 0 violations |
| ST-181 | PASSED (log part) | report shares 0.20/0.20/0.40, no over-100% warning; rate parts skipped (constraint) |
| ST-182 | PASSED | 0 siege weapons in pillager_settlements; carriage/wagon answers in fit (savanna, desert, mangrove swamp); /place x8: carriage 5, golem van 2, chase 1 |
| ST-183 | PASSED | 14 vehicle drop lines (r3/r5/r8, overlaps); surviving siege towers 10 / 15 chunks from settlements (r4) |
| ST-185 | PASSED (locate part) | biome factors staged; 8 answers / 6 chunks, every one holds the start, in fit, ordinary biomes; 2 timeouts (no answer). Ratio skipped |
| ST-186 | SKIPPED | Nether out of scope (constraint) |
| ST-187 | PASSED | template palettes vanilla + urn only; tents 2 fences each; drum tower in world = template counts exactly (wagon + red roof castle in st_c) |
| ST-188 | PARTLY | Ser Marrow rides (after the Tame fix); illusioner + lost-llama quest need a player -> [game] |
| ST-190 | [game] | client |

## Open issues

- Earth fill can't see stone bases: size class and checks use earth base blocks only. The drum tower (49x45 polished
  andesite base, 10 earth) is "small" and could hang over water/cliffs unchecked. Same family as the piglin camp
  finding (M1). Options: count any solid template block in the base row, or an override for the drum tower. User.
- /cwglocate census reports every slot "known" on fresh land (no prediction recorded), so its false-negative column
  is meaningless as run here. Check LocateStats.wasPredicted / the census active flag before Phase 6.

- Way-sign metas: 0 of 11 assembled on st_c (friendly camp part or way sign part placed 0 of 1). Phase 2 found both by
  /locate, so they can assemble; maybe more parts fail since 7.4 added Campsite/eugene camps to meta/friendly_camps
  (house_camp, same camp part, assembles 6/7). Rate/assembly question, not a 7.4 correctness bug; for the user.
  UPDATE session 2: st_d (density 4) saved waysign_camps 6 + waysign_house_camp 1 with Campsite/eugene parts, so they
  do assemble; st_c was a small sample. CLOSED.
- Console commands lost: of 15 commands sent 1 s apart during a heavy /locate batch, the last 6 never ran. Send
  commands to a busy server one at a time and confirm each.
- Undead castle rarely survives: 4 of 5 rolled castles in area 2 cancelled by earth fill (huge size, slopes in its
  plains/highlands/taiga fits), the 5th dropped by the peaceful radius. Rules work as designed; whether to narrow its
  fits (e.g. drop land/highlands) or relax its mound limit is the user's call.
- Locate misses: the battering ram search loaded 16 "maybe" candidates and all missed (catapult 4). Not wrong answers,
  but the miss rate belongs to Phase 6 / LO- (prediction with biome factor 0.5 + peaceful drops).
- GOTCHA for later phases: /reload drops Chunky's commands (restart). Region biomes in structure_starts-status chunks
  are placeholders; use the biome source (/zp biomesrc).
