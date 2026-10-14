# M2: plan 7.4 tests on the scratch server (ST-170 to ST-190)

2026-10-14, two sessions. Live notes: [02_new_mods_tests.scratch.md](02_new_mods_tests.scratch.md). The result
lines are also inline in [structure_tuning.md](../../structure_tuning.md) section 8 (block "New structure mods
before Phase 3"), with a summary above "Gate before Phase 3".

Scope set by the user after M1: Nether and End spawning are out (ST-186 and the Nether/End part of ST-171 skipped),
and so are structure-to-structure rates (the split in ST-181, the harsh/ordinary ratio in ST-185). Every
correctness part was run.

## 1. Setup

- Scratch dedicated server in the session scratchpad: Trash `CreEngServer` libraries; instance `kubejs/`,
  `config/`, `defaultconfigs/` copied; 373 mods symlinked (mod_sides.md server/both rows plus the 4 unlisted jars
  `campsite_structures`, `restApi`, `rsec`, `unlitcampfire`). patcherMod = `development/patcherMod/build/libs/
  creativeengineering-1.0.6.jar` built 2026-10-14 01:28. The jar in `mods/` was not used.
- Seed 8675309, `max-tick-time=-1`, view distance 6, `asyncLocatorThreads = 4`.
- Scratch toml (server copy only): earth fill `log = ALL`, meta structures `log = true`, placement priority
  `log = true`.
- Worlds:
  - `st_c` at density 1 (session 1): /locate-driven checks.
  - `st_d` at `[worldgen.debug] structureDensity = 4.0` (session 2): the soft-rule loosening from 7.4.8, on the
    server copy only, so nothing needs putting back. Two Chunky squares of radius 800 (10,201 chunks each): one
    around 0 0 (76% ocean, little use) and one around 2144 -2592. The second was chosen with a biome-source map
    (92% land, 35 biomes).
- Offline analysis (scratchpad scripts, not in the repo):
  - The structure starts, entities and biomes were read straight from the region files.
  - Biome checks use the point vanilla checks: the centre of the jigsaw start piece, at its floor.
  - The peaceful-radius check reimplements `PlacementPriority.withinRadius`: piece boxes, radius x 16 blocks, XZ
    only, touching counts.

## 2. Results

| Test | Result | Evidence |
|---|---|---|
| ST-170 | PASSED (after 2 fixes) | World loads after the pool `name` fix; load counts exact (15 groups, 30 fits / 181, 104 peaceful / 70 hostile); `/reload` clean for `eugene:*` after the `all_of` fix |
| ST-171 | PASSED (Overworld) | 14 Campsite campers in the pregenerated land, all inside Campsite pieces, none loose; all 6 camps in fitted biomes |
| ST-172 | PASSED | Each camp kind only ever uses its own templates; mobs present |
| ST-173 | PASSED | Passive camps keep a witch hut, dungeons, a vehicle, outposts and hostile camps out at 3 chunks; 0 violations over 78 peaceful / 26 hostile starts |
| ST-174 | PASSED | Hostile camps dropped at 3 (houses/camps), 5 (villages) and 8 chunks (town metas); 0 violations |
| ST-175 | PASSED | 0 out-of-fit starts of 5,530; every kept camp 0 over water, 0 over air; fisher on a beach, one shore fisher cancelled |
| ST-176 | PARTLY | Sizes sensible; undead castle cancelled 4 of 5 over slopes; drum tower "small" (stone base, see 4) |
| ST-177 | PASSED, [game] part left | No `eugene:castle` start; all three castles generate correctly (boss bar needs a client) |
| ST-178 | PASSED | Undead castle in `surface_dungeons`, kept away by the peaceful radius |
| ST-179 | PASSED | Red roof castle: no hostile within 8 chunks, no overlap, not pruned, way-sign destination |
| ST-180 | PASSED | Red roof castle anchors town_villages, fortified_camps and fortified_ruins; companion village clear of it |
| ST-181 | PASSED (log part) | Shares 0.20 / 0.20 / 0.40, no over-100% warning |
| ST-182 | PASSED | Siege weapons out of pillager_settlements; carriage/wagon in fit; `/place` x8 gave all three carriage variants |
| ST-183 | PASSED | 14 vehicle drop lines; surviving vehicles 10 and 15 chunks from settlements (exclusion 4) |
| ST-184 | PASSED | Siege metas assemble with 22 siege-weapon parts; camp metas anchor on `eugene:pillager_camp` |
| ST-185 | PASSED (locate part) | 8 answers in 6 chunks, each holds the start, in fitted ordinary biomes; 2 timeouts |
| ST-186 | SKIPPED | Nether out of scope |
| ST-187 | PASSED | No 1.20 ids left in the templates; tents keep their fences; drum tower in the world matches its template |
| ST-188 | PARTLY | Ser Marrow rides (after a fix); illusioner and lost-llama quest need a player |
| ST-189 | PASSED (via `/loot`) | Map search async in 86 ms, no stall |
| ST-190 | [game] | Client check |

## 3. Bugs found and fixed (all in the new 7.4 port)

1. **World would not load:** 4 eugene template pools from 1.20 have no `name`, which 1.19.2 requires. Added, the
   porter now adds a name to every pool missing one, and the new `tools/check_worldgen_json.py` (step 9 of
   `tools/test_worldgen_tools.sh`) checks required keys.
2. **Predicate type:** `eugene:ranger_spawn` used `minecraft:all_of` (1.20). It is now a JSON array (the 1.19.2
   AND form); the porter converts `all_of` and `any_of`.
3. **Ser Marrow fell off his steed:** an untamed skeleton horse throws a mob rider within seconds.
   `mount_ser_marrow` now summons the steed with `Tame:1b`.

## 4. Open issues (for you)

1. **The undead castle rarely survives:** in area 2, 4 of 5 rolled castles were cancelled by earth fill (huge,
   bearded, on slopes in its plains/highlands/taiga fits). The terrain under two of them varies by 30 and 12-15
   blocks. The fifth was dropped by the peaceful radius. Earth fill is doing its job; the options are to narrow
   the fits (for example drop `land/highlands`) or relax its mound limit. Your call.
2. **Earth fill can't see stone bases:** size and checks count only earth base blocks
   (dirt/sand/gravel/clay/path). The drum tower's 49x45 polished andesite base counts as "small, 10 earth", so it
   could hang over a cliff unchecked. Same family as the piglin camp note from M1.
3. **Census reports every slot "known":** `/celocate census` on fresh land showed no prediction path, so its
   false-negative column means nothing yet. Check before Phase 6.
4. **Locate misses (Phase 6 lead):** a battering ram search loaded 16 "maybe" candidates and all missed (catapult 4),
   then timed out. These were not wrong answers.
5. **Closed:** way-sign metas assembled 0 of 11 on st_c, but area 2 saved 7. The sawmill "in a beach" was my
   sample point.

## 5. Values changed and restored

- `structureDensity = 4.0` and the three log switches were set in the scratch server's toml copy only. The
  instance `config/creativeengineering-common.toml` is untouched.
- Nothing was installed into `mods/` or deployed. The repo changes from M2 are the three port fixes above, the
  porter changes and `tools/check_worldgen_json.py`.

## 6. Notes for later phases

- `/reload` removes Chunky's commands; restart the server instead.
- In chunks that stopped at `structure_starts` (/locate answers), the region biomes are placeholders. Read the
  biome source instead (a scratch probe calling `getNoiseBiome`, no chunk load).
- Jigsaw starts are biome-checked at the centre of the start piece, not the chunk corner.
- A Chunky square of radius 800 takes 25-34 min in this pack (5-12 chunks/s).
- Send console commands one at a time to a busy server. 15 commands sent 1 s apart lost the last 6.
