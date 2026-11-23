# farmers-structures-donor

> Farmer's Structures 1.0.6 (ARR, NeoForge) is read from farmers_upstream/ and converted in memory by Creative Common worldgen/donor; gotchas

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

The 1.0.6 jar lives unmodified in `<game>/farmers_upstream/` (git-ignored, sha1-pinned in `UpstreamJars.java`, deployed by `build_deploy.py` category `farmers_upstream`). `FarmersDonor` adds a built-in server data pack (id `farmers_structures_donor`, sorts below KubeJS ids) above the loaded 1.0.0 mod; `FarmersTranslator` converts 1.21 NBT/JSON. Foreign-dimension structures are skipped, so 1.0.0's disabled versions remain.

**Why:** owner chose the Vinery-style donor route for ARR content (2026-10-25). Details: feature_writeup/farmers_structures.md section 5.

**How to apply:** `tools/gen_structure_config.py` also reads the donor jar (DONOR_SKIP_STRUCTURES must match the Java skip list). Unknown 1.19.2 blocks become air, so check the `[farmers-donor]` counts line after changes. Pack-order of the donor pack vs the 1.0.0 mod pack was reasoned, not observed: verify with SQ-FSD-01/05. Related [mcreator-house-mods-datapack](mcreator-house-mods-datapack.md), [backported-vinery](backported-vinery.md).
