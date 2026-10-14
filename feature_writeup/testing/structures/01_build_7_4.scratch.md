# M1 scratchpad: build plan 7.4 (offline only)

## State (keep current)

- !!!!!!!!!!!!! Ignore nether/end spawning in the future !!!!!!!!!!!!!!!!!!
- !!!!!!!!!!!!! Ignore structure-to-structure tuned frequencies from now on, focus on correctness !!!!!!!!!!!!!!!!!!
- !!!!!!!!!!!!! Sleep for 60 minutes when the token limit is near !!!!!!!!!!!!!!!!!!

- Milestone: M1, build 7.4 (structure_tuning.md 7.4.0 .. 7.4.8). Offline checks only, no scratch server.
- Current step: M1 DONE (writeup 01_build_7_4.md). Next: 30-minute sleep (constraint 3), then M2 (02_new_mods_tests.scratch.md)
- Done: 7.4.1-7.4.6 built; tags, groups, fits, D1 shares, vehicles group + biome_frequency (Java/JS/python), rates set
- Next: M2 scratch server, ST-170..190 minus the skipped Nether/End and rate parts
- Jar: development/patcherMod/build/libs/creativeengineering-1.0.6.jar built 2026-10-14 01:28 (biome factors)
- Backups of the pre-7.4 configs: session scratchpad m1/backup/ (structure_tags/groups/meta/fits/config/estimates/report)
- Config values changed: none
- Running: nothing
- CONSTRAINTS (user, 2026-10-14, after the offline checks): (1) ignore Nether/End spawning from now on (piglin
  camp, pig ranger, ST-186 and other Nether/End checks skipped); (2) ignore relative structure frequencies from now
  on (no more rate tuning, no estimates/rate tables, rate parts of tests skipped: ST-181 ratio, ST-185 harsh ratio,
  C/V/O); (3) sleep 30 minutes when the 7.4 changes are completed. Chat addition: if the token limit gets close,
  sleep an hour instead, and then skip the 30-minute sleep at 7.4 completion.
  Skipped under (2): structures_by_biome.md regen (a per-biome rate table).

## Log (append-only)

- 2026-10-14: M1 started. Read 00_plan.md, 7.4, 8.0, ST-170..190. No earlier scratchpad existed.

- 7.4.1: Campsite features are Overworld-only (generate_dimensions = OVERWORLD) and need a grass block at the
  surface; chance 1000 in 1,000,000 per chunk each. So "Nether and End too" in 7.4.0 is wrong; still outside every rule.
  Feature origin = top block - 5 -> template grass layer (y4) one block BELOW the surface (sunken camp). Jigsaw:
  WORLD_SURFACE_WG + start_height -5 puts y4 on the surface block.
- 7.4.1 written: kubejs/data/campsite_structures/forge/biome_modifier/*.json = forge:none (6);
  worldgen/{structure,template_pool,structure_set}/<camp>_campsite.json (jigsaw size 1, beard_thin, biomes
  #minecraft:is_overworld as the mod default, set 32/8 = about the feature's 1 per 1000 chunks, salt crc32).
  Templates: each holds 1 campsite_structures:camper (passive) -> all 6 passive. Fisher has a cod + a pond.
- 7.4.2 porter (tools/port_structure_jars.py): single-quoted modId, DirSource for unpacked dirs (never moved),
  BLOCK_REMAP keep_props tuple/dict, ENTITY_STRIP, entities of absent mods stripped, loot: unknown item entries
  dropped + SKIP_LOOT_TYPES (minecraft:archaeology). Two bugs fixed on the way: minecraft: templates (vanilla
  overrides) were skipped as unreferenced; vanilla pool entries were pruned when the jar ships data/minecraft.
- 7.4.2 port output: 64 files. Remaps: diagonal fence x2 (tents), diagonal pane x2 (wagon), chiseled bookshelf,
  decorated pot -> urn, hanging sign -> lantern[hanging] (CHOICE: it hangs under a chain, a wall sign has no
  wall there; plan offered wall sign or air), suspicious gravel/sand -> gravel/sand. Stripped: red_roof zombie,
  6 naturalist:firefly + 1 wildlife:lark. ruined_equipment axe dropped from the carriage item frame. Loot:
  archaeology table not copied (type unknown in 1.19.2, blocks are plain gravel now), snout trim entry dropped.
- 7.4.2 functions: summon_pig_ranger -> hoglin with the piglin as passenger (+ fixed DeathLootTable typo
  "entitties"); new mount_ser_marrow (steed summoned carrying a new skeleton; data modify copies Attributes,
  Health, HandItems, ArmorItems, drop chances, LeftHanded, CustomName, Tags; old one tp ~-600);
  summon_skeleton calls it; tick_ser_marrow: ride line removed, damage line -> wither 2 1 within 1.5 of a steed.
  Perf note (not changed): tick runs `execute as @e[type=wandering_trader]` every tick.

- 7.4.3/7.4.4: eugene:camp pool = trader_camp + lost_llama; new eugene:pillager_camp, eugene:fake_trader_camp
  (copies of camp.json); eugene:camp set lists all three at the old pool weights 115/120/10 so sync fits each at its
  share. Castle: new undead_castle (beard_box), red_roof_castle (legacy element), ruined_drum_tower (legacy);
  eugene:castle set now lists the three (castle itself left registered but in no set = never generates; NOT
  enabled:false, so each castle gets exactly 1/3 of the old rate). Oversized salts (castle, piglin_camp,
  witch_wagon > 2^31) written as their 32-bit wrapped values (what the game reads).
- Out of plan: subterrestrial (8 underground cabin sets, jar added 2026-10-13) showed up in the rescan. Only
  classified (mod/subterrestrial + type/underground -> water_ok keeps the ocean cabin's ocean biomes); standalone
  at mod rates. Tell the user. Coverage also lists takesapillage:* and underground_bunkers (pre-existing gaps).
- Tags: mod/eugene, mod/campsite_structures (+ in mod/villager_camps -> type/camp, remote, meta/friendly_camps),
  type/castle (red roof), type/vehicle (siege + carriage + wagon; siege out of type/camp), group/vehicles
  (#type/vehicle), siege weapons out of group/pillager_settlements; fits as planned; fisher only land/beach (no
  river-side fit exists).
- D4 code: StructureFrequencies.stageBiomeFactors + Factors/Rule; generation roll recomputes findGenerationPoint
  for members with factors (deterministic per chunk) and rolls frequency x factor; LocatePrediction.plain applies
  factorRejects at its stub. JS: groups.<g>.biome_frequency validated (planBiomeFrequency), staged, report line;
  biome_fits.json "biome_tags" (harsh) added as real tags by the worldgen/biome handler. Python estimator applies
  factors (new_densities, doc_densities; Pack.biome_entry/biome_factor). node sim + RhinoRun write
  biome_frequency.json; test_structure_groups.sh compares them. FreqTest section 3b added. FitTest 167 -> 181.
- 7.4.6: earth fill counts only dirt/sand/gravel/clay/path as earth, and lava is SOLID in its terrain kinds. The
  piglin camp's floor is netherrack/nylium: earth fill never sees it, so "treat lava like water" would not help.
  The mod's own terrain_adaptation is beard_box (fill below + carve the box), not overridden by our tags: that is
  what keeps it off the lava and out of the netherrack. No code change; ST-186 checks it. Tell the user.
- Rates: sync, then --fit-rates. The full --fit-rates also re-fits ~75 EXISTING structures (pre-existing drift:
  the same run on the pre-7.4 configs changes 75, 40 by >5%, e.g. T&T desert outpost weight 33 -> 159). Kept only
  the new members' fitted values; every existing entry restored. Decision for the user.
  Campsite own list was #minecraft:is_overworld at first: that put them in every remote biome of the targets
  (igloo x0.28...). Narrowed to their fit families' vanilla+Terralith ids. Final campsite weights hand-set
  (60 land, 15 fisher, freq 1) for 1 per ~155 each (tents now 1 per 117-132).
- Vehicles: C > V > O is impossible in ordinary biomes: outposts are as dense as camps or denser there (plains
  C 1/39, O 1/39; meadow C 45, O 38). Chose: rarer than camps everywhere but terralith:lavender_forest, harsh x2
  (ratio 1.91): spacing 52 / separation 16, weights 100, wagon 200 x 0.76 (4x carriage, mod ratio). Harsh 1 per
  54, ordinary 1 per 75; each siege weapon 1 per 167 (was ~111); carriage 1 per 163, wagon 1 per 82. Tell the user.
- Campsite fit_base / vehicles fit_base hand-set (= values). Scans: nether_blocks_scan tier 1 in 15 live (was 13).

## Results (offline checks)

| Check | Result | Evidence |
|---|---|---|
| gradle build | PASSED | creativeengineering-1.0.6.jar 2026-10-14 01:28; no src/ change since (find -newer) |
| test_structure_groups.sh | PASSED | node sim 15 groups / 178 sets / 30 fits for 181 structures / 22 metas; Rhino parity all identical (also density x4, x0.3, groups off); old-jar cases; FreqTest ALL 28 |
| test_worldgen_tools.sh | PASSED | GroundMatch 37, MassPlacer 30, EarthFill 53, density 264, TerrainAdaptation 50, MetaTest 70, FitTest 30, Locate 28 (FitTest ERROR lines = its bad-input cases) |
| template scans | PASSED | terrain_adaptation / nether_blocks / meat_block scans rerun in the build step (nether tier 1 in 15 live, was 13) |

## Open issues

- 2026-10-14 (session 22443b35): offline checks PASSED (see Results). Loot catalog regenerated (405 tables / 50 ns,
  + eugene + subterrestrial, no new dead refs, no snout). Peaceful/hostile diff via node sim on the backed-up configs
  (temp root in this session's scratchpad m1/oldroot): 96 -> 104 / 65 -> 70, exact 7.4.7.5 list. New structures'
  staged biomes: none in ocean/river/cave/Nether/End. structure_tuning.md: 2.2 table refreshed from the report (counts
  incl. metas, grids current), 3.16 shares + tag rows, 3.22 rank 76 + shares, 7.4 BUILT, 7.4 moved to Done, 8.0.1
  counts, test block header + constraints note + ST-170 offline pre-check, history. Writeup 01_build_7_4.md written.
