# moobloom-variants

> More Mooblooms (more_mooblooms) own mod: 11 *bloom variants, pools, breeding, feed_weight, witherbloom soul sand valley pack; data generated from the mooblooms.md 4.4 table; version and status in feature_writeup/mooblooms.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-08). Repo status docs win over any status line here.

More Mooblooms **1.0.0** (development/moreMooblooms) is the user's release version, built 2026-10-07 23:22; jar in build/libs + jar_archive, in mods/ (same md5 as build/libs, checked 2026-10-08; earlier jars in jar_archive/prerelease/). Install client AND server: `./gradlew :moreMooblooms:installToMods`. feature_writeup/mooblooms.md was rewritten to the 1.0 state (sections 0-8; section 9 History is marked DO NOT REMOVE by the user, append only; section 5 "JEI changes" is the user's 1.1 plan).

- 11 variants (moobloom default, sunbloom, rosebloom, peonybloom, onionbloom, bluebloom, orchidbloom, lilybloom, oxeyebloom, azaleabloom, witherbloom). Weighted flower pool rolled per position, saved as entity `Flowers`; first shear = carried flowers, second = 3 pool picks.
- The user edits the variant table in mooblooms.md 4.4 (found by its "Flower pool" column) then `python3 tools/gen_moobloom_variants.py` writes variant + moobloom_breeding files (parent cells "a+b (N%)", short names like rose=rosebloom; "feed `weight` N" modifier; bare names must be vanilla). It keeps keys it doesn't own (egg_colors, any_weight).
- feed_weight per pool entry: shared flowers pick a variant by weight (dandelion moobloom 9 / sunbloom 1; pink tulip 50/50). Wild onions = optional pool entry on onionbloom.
- Witherbloom: core switch pack packs/more_mooblooms/witherbloom_spawns (config witherbloomSpawns): biome tag + forge:add_spawns weight 1 + spawn rule; per-variant `spawns_in_dark_on` (only witherbloom -> #more_mooblooms:spawnable_on_in_dark); direct natural spawns pick their variant (biome rule) in Moobloom.checkSpawnRules, because NaturalSpawner runs placement -> create -> checkSpawnRules -> finalizeSpawn; placement predicate is only a coarse any-variant filter. Real spawn untested (needs a player).
- Old-id remapping (`replaces`) was built then REMOVED at the user's request; they clear old entities by hand. Don't reintroduce.
- disableSunny: MobSunBurnMixin priority 900 runs before BB's (verified via mixin export), effect DENY + strip on join.
- Egg "swap" = Quark Automatic Tool Restock; fixed by Ignored Items in pack config (3 copies).
- Forge 1.19.2 tag JSON has no `remove`: buttercup stripped by TagLoader mixin. Spawn swap is @ModifyArg (Botania clash).

- Spawns (2026-10-08): pack rule file kubejs/data/more_mooblooms/moobloom_spawns/creativeengineering.json = 35 hand-translated per-biome rules from mooblooms.md 2.8 (regenerated from gen_wild_flowers.py inventory): tiers by pool-flower patches/chunk (>=1.0 -> flower biome, bloom % + split set BY THE USER in the 4.9 table, rule = bloom% x split/total; 0.5-1 -> 25%, 0.25-0.5 -> 2%, else rareChance), mix = biome's flower->variant shares (D47/D48). flower_variety tag + moobloom_spawns.js REMOVED; cows added to skylands_spring. Scratch-server load OK; spawns not seen in game. User said NO automation for now (2026-10-08): edit the 4.9 table, then hand-apply to the JSON. 2026-10-11: user retuned 8 flower-biome rows; even split = the 9 FLOWER variants (azaleabloom excluded at the user's request; blooming x2, meadow 75%, flower_forest). Azaleabloom instead spawns in lush caves: mod (user: keep version 1.0.0; rebuilt + installed in mods/ 2026-10-11, server not updated) adds spawns_in_dark_on moss_block to azaleabloom; pack adds kubejs/data/creativeengineering/forge/biome_modifier/azaleabloom_lush_caves.json (weight 10, 1-3; lush caves has no other creatures) + lush_caves 100% azaleabloom rule. MB-98. The 1.0.0 jar IS in mods/ (md5 = build/libs, checked 2026-10-08).

**Why:** BB is under the Abnormals License: nothing of BB's in our jar, no mixins into BB, no compile dependency.

**How to apply:** next steps are install + client MB- tests (render positions/SlotPose, JEI, feeding split, grazing, soul sand valley spawn). Scratch-server testing needs `/forceload` for AI (no-player levels stop ticking entities). `python3 tools/check_moobloom_data.py` checks data offline.

Related: [incubation-additions-mod](incubation-additions-mod.md), [sample-syringe](sample-syringe.md), memory `feature-writeup-pattern`, memory `license-gplv3`, memory `scratch-server-verify`, memory `rhino-fieldandmethods-gotcha`.
