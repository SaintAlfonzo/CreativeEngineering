# plan-7-4-new-structure-mods

> structure_tuning.md 7.4: port Camps. Castles. Carriages. 2.3.4 (DecompiledMods) + Campsite Structures, split mixed camp/castle pools, hostile group/vehicles, fortified metas 0.2/0.2/0.4; tests ST-170..190; status in feature_writeup/testing/structures/

Moved from agent memory 2026-11-16 (last edited there 2026-10-14). Repo status docs win over any status line here.

User 2026-10-14. The source for the castles/carts/wagons is **Camps. Castles. Carriages. 2.3.4** (namespace `eugene`), decompiled at DecompiledMods/camps_castles_carriages-2.3.4. `mods/` has the older 1.1.1 jar. The plan ports 2.3.4 into kubejs/data/eugene and moves 1.1.1 out. The plan runs BEFORE Phase 3.

Key findings:
- eugene:camp and eugene:castle each mix friendly and hostile templates in one pool, so both are split. Camp becomes trader camp (passive) + pillager camp + fake trader camp (both hostile). Castle becomes undead castle (surface_dungeons) / red roof castle (settlements + big_settlements + fortified_towns anchor) / ruined drum tower (ruins).
- Vehicles group = 6 siege weapons + eugene:pillager_loot_carriage (carriage / villager chase / golem prison van) + eugene:witch_wagon. Siege weapons are meta PARTS only, so the metas are unaffected. Add `vehicles` to priority.peaceful.hostile.
- 1.20 content: suspicious gravel/sand, decorated pot, chiseled bookshelf, hanging sign, Diagonal Fences/Windows blocks, a snout trim template in loot. `ride`/`damage` (1.19.4) break tick.mcfunction entirely on 1.19.2.
- port_structure_jars.py mod_id regex fails on single-quoted Modrinth tomls and can't read directories.
- The piglin camp starts at a fixed y 33 with no heightmap, so it can sit over the lava sea. Check whether earth fill treats lava as non-ground.
- Campsite Structures 1.0.3 places MCreator FEATURES in forge:any on OCEAN_FLOOR_WG. They are live and bypass every rule; the fix is forge:none overrides plus real structures, keeping the jar for CamperEntity.
- D1 DECIDED (user, final): fortified_camps 0.20 / fortified_ruins 0.20 / town_villages 0.40 (sum 0.8, 20% plain towns; shares stack in id order, cut at 1).

**Why:** the user said correctness comes first (biome, peaceful radius, earth/water fill). Balance waits for the audit.

**How to apply:** for rare structures that are hard to test, loosen only the soft rules (frequency, weight, share, spacing, separation, debug density), never the correctness rules (7.4.8).

Related: [plan-7-1-execution](plan-7-1-execution.md), [meta-structures](meta-structures.md), [placement-priority](placement-priority.md), [structure-groups](structure-groups.md), [mcreator-house-mods-datapack](mcreator-house-mods-datapack.md).

- 2026-10-14: BUILT (M1, writeup feature_writeup/testing/structures/01_build_7_4.md). Peaceful 96->104, hostile 65->70 (offline). User constraints from then on: ignore Nether/End spawning; ignore structure-to-structure rates (no tuning/estimates/rate tables, rate parts of tests skipped) -- correctness only. See memory `structure-testing-log-method`.
- 2026-10-14: M2 (ST-170..190) DONE, writeup feature_writeup/testing/structures/02_new_mods_tests.md. Fixed: pool `name`, all_of predicate, Ser Marrow Tame:1b. Open for user: undead castle cancelled 4/5 by earth fill on slopes; earth fill blind to stone bases (drum tower); census all "known".
