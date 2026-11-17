# mcreator-house-mods-datapack

> Data-only structure jars (MCreator etc., many crash Forge 43.3.9 via FMLJavaModLoadingContext ctor) are ported to kubejs/data by tools/port_structure_jars.py; 43 jars done so far

Moved from agent memory 2026-11-16 (last edited there 2026-09-28). Repo status docs win over any status line here.

MCreator structure mods labelled 1.19.2 often only have a `(FMLJavaModLoadingContext)` constructor, which Forge 43.4.x+ supports and 43.3.9 doesn't: mod construction fails with `NoSuchMethodException: <Mod>.<init>()`. Their only content is a datapack.

**Batch 1 (2026-09-27, afternoon):** 10 villager house mods ported by hand (script not kept).
**Batch 2 (2026-09-27, night):** 33 jars (20 crashing + 13 working data-only ones, 56 structures) ported by `tools/port_structure_jars.py`, which does the job end to end: it refuses jars with more than one class, copies only the templates the pools reference, remaps 1.20 blocks (decorated_pot -> supplementaries:urn, chiseled_bookshelf -> bookshelf, cherry_* -> vinery:cherry_*), drops unknown items, drops pool entries whose template the jar doesn't ship (SkyVillages had 37), applies POOL_REPOINT (Mysticrift's pools used 1.21-format templates and now point at the jar's 1.20.1 saves), refuses DataVersion >= 3837, and moves the jar to disabledmods/archive/ (all 43 moved there 2026-10-12; loot_breakdown + gen_pack_manifest read archive/ too). Verified on a scratch server: `/cdevplace` placed 56 of 56, live sets matched.

Kept as jars: villagesandpillages (code), blocky_siege (full mod), Revamped Shipwrecks and Unwrecked Ships Plus (lowcode datapack jars that load fine). Revamped Shipwrecks empties minecraft:shipwrecks and wins over Hopo live, so type/shipwreck lists revamped_shipwrecks:*. The 5 working house jars from batch 1 (greek, hobbit, swamp fisherman, sawmill, SimplyHouses) were never ported; I offered to port them.

**How to apply:** for new data-only structure jars, run `python3 tools/port_structure_jars.py --dry-run mods/<jar>...`, then run it for real. After that: gen_structure_config.py, then add mod/* + type/group tags in structure_tags.json, then gen_structure_groups.py, then add mod_sides rows for any jars that stay. Groups keep their spacing, so new members crowd old ones; tell the user. Related: [structure-groups](structure-groups.md), memory `scratch-server-verify`, [worldgen-config-cdevplace](worldgen-config-cdevplace.md).
