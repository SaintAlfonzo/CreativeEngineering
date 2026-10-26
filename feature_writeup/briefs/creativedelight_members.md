# creativedelight / members briefs

Written 2026-10-25. Gate for rows 2 to 8: framework #2 (installed), #4 (dict) and #6 (move-diff harness). Order: 1, then one member at a time
(2, 3, 4, 5), 6 and 7 with their integration passes, 8 independent, 9 pinned, 10 last.
Rule for every script move: files go to `kubejs/data/creative_delight/kubejs/` (server) or `kubejs/assets/creative_delight/kubejs/` (client) in the jar, `// requires:` header for non-member mods,
recipes still through `global.NCH` under `NCH.withNamespace('creative_delight', ...)`, tags through `global.tags`; the pack keeps no copy. Take the harness snapshot before the first move, diff after each.

## members #1: Freeze and bundle the four ports
Status Planned / build
### Build
1. Freeze: Fungi SQ-FGD, Miner's SQ-MDP / SQ-MD-01 and vintageport #18, #4 landed or parked in writing. 2. Per port (`backportedVeggies`, `backportedVintage`, `backportedFungi`, `backportedMiners`): add to `bundle_members` in `development/creativeDelight/gradle.properties`, set `bundled_in=creativeDelight`, `mod_name` to "<Name> (Creative Delight)", bump the `-NN`, add its jar glob to CD's `replaces_jars`, remove its `IN_TREE_MODS` row in `tools/build_deploy.py`.
3. Grep the old display names ("(Backport)") in `namechanges.js`, `jeed_providers`, tooltip scripts. 4. Build CD; SQ-CDJ-05.
### Gotchas
Miner's reads a donor jar (sha1 pinned): the nested jar must still find it. Fungi's pack placed features hard-depend on `fungidelight:*`. Port Java keeps its packages (`net.mehdinoui.*`, `net.ribs.*`, `com.sammy.*`).
### Tests
CDM-01: all seven mod ids load once; CDM-02: a world with Veggies, Vintage, Fungi and Miner's blocks loads with none missing; CDM-03: mod list, JEI and WTHIT show "(Creative Delight)"; VD-, VTD-, FGD-, MND- suites unchanged.

## members #2: Veggies Delight rows
Status Planned / KubeJS / data
### Now
Found: `server_scripts/cooking/` (2 files), `data/creativeengineering/moonlight/soft_fluids/` (9 Veggies files), wild crop and flower rows (`wild_crops.json`, `wild_flowers.json` entries), seasons, 24 generated cans, compost, trades.
### Build
Pilot member (also the framework #4 dict pilot). Move cooking rules first, then soft fluids (check whether Moonlight reads a CD-namespaced path: if not they stay in `data/creativeengineering/`), then compost (startup pack of CD), then generated rows (framework #7). Marked merges (veggiesport #2) stay as they are.
### Tests
CDM-04: harness diff clean; VD- cooking tests unchanged.

## members #3: Vintage Delight rows
Status Planned / KubeJS / data
### Now
4 soft fluid files, `startup_scripts/_lib` dict entries, `startup_scripts/compost/` entry, `server_scripts/cooking/` rules, ghost pepper Nether biome modifier + configured feature, 54 food types, 16+4 cans, wild crop rows.
### Build
As #2. Biome modifier and configured feature: keep under `data/creativeengineering/` if data under `creative_delight` cannot be addressed from `wild_crops.json` (check). Dict keys stay in `item_dict.js` until a startup pack of CD owns them.
### Tests
CDM-05: harness diff clean; VTD- cooking and ghost pepper worldgen rows.

## members #4: Fungi Delight rows
Status Planned / KubeJS / data
### Build
Cooking rules and cans move. Keep in the pack: the 10+ placed features and configured features under `worldgen/.../wild/mushrooms` (hard dependency on `fungidelight:*`, accepted) and `mushroom_spawns.json` (patcherMod, shared with Miner's). Mooshroom cow spawn rows go with members #8.
### Tests
CDM-06: harness diff clean; FGD- cooking tests.

## members #5: Miner's Delight rows
Status Planned / KubeJS / data
### Build
`cup_conversion` data (onion soup cup mapped to Brewin' Creamy Onion Soup) and the cave carrot configured feature. Haste potions: settle the overlap with Rustic Delight (MND-40) before moving. Pack data reaches the jar only through the translator, never by editing a file at an upstream path (ARR).
### Tests
CDM-07: harness diff clean; MND- suite.

## members #6: Ocean's and Oceanic
Status Planned / KubeJS / data
### Now
Mixin already moved (Oceanic overlay). Still in the pack: `loottags.js` fills `creativeengineering:fishing_net/*`; two tables under `data/oceanic_delight/loot_tables/gameplay`; `namechanges.js` (caviar).
### Build
1. Move the tag fill and loot tables to CD's Oceanic folder (tag ids stay `creativeengineering:fishing_net/*` unless the overlay changes in the same release). 2. Integration pass from mod-additions #5 and dishes/ocean #1, #3: merge the three raw tentacle items (Ocean's, Oceanic, Miner's), squid drops, guardian food, takoyaki; each merge also gets a swap line when swappers exist.
### Tests
CDM-08: SQ-CDJ-04 still passes after the tag move; CDM-09: merged items have one id in JEI.

## members #7: End's Delight integration
Status Planned / KubeJS / data
### Build
Evaluations in [ends_delight.md](../ends_delight.md) 3.1 to 3.3 and mod-additions #4, #15, #29 are the rows. This row only places them: End's-only rows live in CD's End's folder; rows naming `endersdelight` stay in the pack (`cooking.js`, `end.js`, `potions.js`) because Ender's is not a member. Shared tags (stove pair, chorus) are defined once and read by both.
### Tests
CDM-10: SQ-CDJ-06 plus the stove and chorus recipes after unification.

## members #8: Mooshroom family into moreMooblooms
Status Planned / new jar + patcherMod
### Now
patcherMod: `mooshroom/MooshroomFamily.java`, `mooshroom/MushroomTypeSetter.java`, mixins `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin`, `MushroomCowWalkTargetMixin`. moreMooblooms depends on nothing of patcherMod (soft Buzzier Bees, JEI, Blueprint, Alex's Mobs).
### Build
1. moreMooblooms `mods.toml`: hard dependency on `creativeengineering` (patcherMod, version range). 2. Move the five classes with their packages renamed, mixin json of moreMooblooms, refmap. 3. Gate registration on `fungidelight` loaded. 4. Fungi cow spawn rows move out of patcherMod's spawn data. 5. Remove patcherMod's copies and `creativeengineering.mixins.json` entries in the same release (Q22: hard cut). 6. Install order: patcherMod and moreMooblooms together.
### Gotchas
`SpawnTuning` and `CEConfig` stay in patcherMod and must be public API for the move; `brown_chance` and the `mushroomSpawns` switch are patcherMod config.
### Tests
CDM-11: breeding any two of red, brown, inky, morel; Brown chance per biome; inky/morel cows spawn; world without `fungidelight` boots with moreMooblooms.

## members #9: Ender's Delight compliant rebuild
Status Pinned / new jar. Writeup 15; follows the Miner's Delight method ([miners_delight.md](../miners_delight.md)): fresh code, art and data read from the unmodified donor jar, sha1-pinned. Do not start until the licence question (upstream 1.1.4 declares MIT, installed 1.2.2 is ARR) is answered by the owner.

## members #10: Cutover release
Status Planned / build + docs
### Build
`tools/build_deploy.py --notes "..."`; the member rows in `IN_TREE_MODS` already merged by #1; `mod_sides.md` rows merged; changelog bullet in `unreleased.md` for the jar swap and display names (follow changelog.md 3.6, run `tools/changelog_lint.py`); writeups' status lines; README index.
### Gotchas
Never replace jars while the game runs. The hard cut means worlds from before need both patcherMod and CD in the same update.
