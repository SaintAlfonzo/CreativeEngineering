# creativedelight / members: implementation brief

Written 2026-10-25; swept 2026-10-31 from [TODO](../todo/TODO.md) `# creativedelight`, Sub `members` (open: #1, #6, #8 to #11,
#14 to #21, #25 to #27, #29; the rest Testing).
Feature writeups: [creative_delight.md](../creative_delight.md) (proposed `CDM-` ids, highest used CDM-11),
[rustic_delight.md](../rustic_delight.md) (`RD-` = SQ-RD-, last RD-33), [ocean_delight.md](../ocean_delight.md) (`DA-`, last DA-15).

**Main finding of the 2026-10-31 sweep:** the Rustic Delight 1.7.0 port (built 2026-10-25, live since 1.1.03,
rustic_delight.md 5) already delivers most of the owner list in rustic_delight.md 5.9. Rows #14, #17, #19, #20 and #21 have
nothing left to build, #16 only an unspecified "syrup changes", #15 is the same work as `merges/baking #1`. Their sections
below say what is done (with evidence) and which RD- tests check it. #29 was fixed 2026-10-31.

Rule for every script move: files go to `kubejs/data/creative_delight/kubejs/<modid>/` (server) or
`kubejs/assets/creative_delight/kubejs/<modid>/` (client), startup to `development/creativeDelight/src/main/resources/kubejs_startup/<modid>/`;
`// requires:` header for non-member mods; recipes through `global.NCH`, tags through `global.tags`, ids from the dicts; the pack keeps no copy.
Effect swaps, potions and alcohol belong to Creative Brewing ([creative_brewing.md](../creative_brewing.md) 4.2), not CD.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `members #29` calamari swap order | none left | Fixed 2026-10-31, in the installed CD jar; only SQ-CM-32, SQ-CM-35. Should be Testing |
| 2 | `members #27` pepper blocks, medley texture | check, then maybe resource pack | Medley half most likely fixed 2026-10-26 (platter); pepper block "connect" needs the owner's meaning |
| 3 | `members #25` crate, renames, removals | KubeJS / CD startup | Coffee Pie done (1.1.05); glow calamari crate and fried dough left; Veggies stuffed pepper is `merges/dishes #3` |
| 4 | `members #26` sushi assemblies, grid meals | KubeJS | Pattern exists in CD `farmersdelight/recipes.js` |
| 5 | `members #11` coffee | KubeJS (pack + CD) | Mostly moved already; the real work is 3 coffees into the Caffeinated swap |
| 6 | `members #16`, `#18` | decisions, then KubeJS | Owner input needed (syrup changes; calamari soup overlap) |
| 7 | `members #14`, `#15`, `#17`, `#19`, `#20`, `#21` | none (play tests) | Built by the port; TODO should move them to Testing or close #15 into `merges/baking #1` |
| 8 | `members #1` freeze and bundle | build | Blockers listed in the section |
| 9 | `members #6` Ocean's and Oceanic | KubeJS / overlay data | After #29 tests pass |
| 10 | `members #8` mooshroom family | patcherMod + moreMooblooms | Independent |
| 11 | `members #10` cutover release | build / docs | After #1 |
| - | `members #9` Ender's rebuild | pinned | Licence answer first |

---

## members #29: Calamari swap runs after the tentacle recipes are removed
Status Bug (TODO) / KubeJS / `creativedelight/members #29`. **Fixed 2026-10-31; TODO should be Testing.**
### Now
- Fixed per [ocean_delight.md](../ocean_delight.md) 5.5 and [creative_delight.md](../creative_delight.md) 31: in CD
  [oceansdelight/recipes.js](../../kubejs/data/creative_delight/kubejs/oceansdelight/recipes.js) the `replaceInput` swaps
  (lines 38-44) now run before the removal loop (47-49); raw calamari joins `#forge:squid_tentacles` and `#forge:foods/tentacles`
  in [oceansdelight/tags.js](../../kubejs/data/creative_delight/kubejs/oceansdelight/tags.js) (an empty tag cannot be a `replaceInput` target).
- File mtime 2026-10-31 12:31; `mods/creative_delight-1.0.0.jar` (16:45) holds the same 3237-byte `recipes.js`. Committed in `ffa47cf`.
### Build
Nothing. Rerun the tests.
### Tests
SQ-CM-32 and SQ-CM-35 ([scratch_queue.md](../testing/scratch_queue.md) lines 2168, 2171).

## members #27: Rustic: pepper blocks do not connect; bell pepper medley plate missing texture
Status Bug / new jar / resource pack / `creativedelight/members #27`
### Goal
Every medley state textured; giant pepper blocks look as the owner expects.
### Now
- Medley: the donor models (`assets/rusticdelight/models/block/bell_pepper_medley_stage0..2/leftovers`) take
  `farmersdelight:block/platter`, which FD 1.19.2 lacks. Fixed 2026-10-26 (rustic_delight.md 5.1 translator row):
  `assets/farmersdelight/textures/block/platter.png` ships in the nested repackedFarmers (`repackedFarmers-1.19.2-1.2.4-ce-19.2-02.jar`
  in the installed CD jar, checked). `logs/2026-10-26-4.log.gz` still has `farmersdelight:textures/block/platter.png not found`;
  `logs/latest.log` (client, 2026-10-31 17:15) has no platter line and no `Missing textures` line. The coaster textures the
  peppers use (`textures/block/coaster/stuffed_bell_pepper_*`) are in the donor. So the medley half is most likely the
  pre-fix jar the owner played on 2026-10-27.
- Giant peppers: `RDBlocks.GIANT_PEPPERS` ([RDBlocks.java:50-51](../../development/creativeDelight/members/rustic/src/main/java/com/SaintAlfonzo/creativedelight/rustic/registry/RDBlocks.java)),
  a plain block with the melon's properties; donor model `cube_bottom_top` (stem top, plain bottom, ribbed side), one
  blockstate variant. Upstream 1.7.0 has no connecting shape either; no connected-texture mod is installed.
### Build
1. Client check SQ-RD-32 (medley at every serving). If it passes, the medley half is done.
2. Pepper blocks: only after the owner says what "connect" means (decision). If it is a stacked look, write a
   `bell_pepper_<colour>_block` blockstate with `up`/`down` booleans in the Rustic member's overlay and a small `RusticPepperBlock`
   (`updateShape` sets the booleans) with top / middle / bottom models; nine colours share one model set by texture.
### Decisions
- What "do not connect" means: (a) side-by-side blocks show seams (as melons do), (b) a 2-high stack should read as one
  pepper, (c) something else. Recommend: close as upstream behaviour unless (b), which needs new art for the middle piece.
### Tests
SQ-RD-32, SQ-RD-29 (queued). Proposed RD-34 (only if blocks change): stack 2 and 3 high, place side by side; models change by neighbour.

## members #1: Freeze and bundle the four ports
Status Planned / build / `creativedelight/members #1`
### Now (checked 2026-10-31)
- Still separate jars in `mods/`: `backportedVeggies-1.9.2-ce-19.2-03`, `backportedVintage-0.1.6-ce-19.2-09`, `backportedFungi-1.3-ce-19.2-01`,
  `backportedMiners-1.4.5-ce-19.2-01`; each `gradle.properties` has `mod_name=... (Backport)` and no `bundled_in`.
- CD `bundle_members` ([gradle.properties](../../development/creativeDelight/gradle.properties) line 18) has 9 members
  (repackedFarmers, Oceans, Oceanic, Ends, backportedRustic, repackedDelightful, endersAdditions, backportedHearth, blockheadsAdditions); `replaces_jars` line 20.
- `tools/build_deploy.py` `IN_TREE_MODS` (lines 133-146) has rows for Veggies, Vintage and Fungi only; Miner's has none (nothing to drop).
- Blockers: `ports/fungiport #1` Testing (SQ-FGD), `largecontent/mod-additions #7` (Miner's) In Progress (SQ-MDP / SQ-MD-01),
  `ports/vintageport #18` Testing; `ports/vintageport #4` merged into #16 2026-10-30 (built), no longer a blocker.
### Build
1. Freeze: the three open rows above land or are parked in writing.
2. Per port: add `:backportedX` to `bundle_members`, `bundled_in=creativeDelight`, `mod_name` "<Name> (Creative Delight)",
   bump `-NN`, add its jar glob to `replaces_jars`, drop its `IN_TREE_MODS` row (none for Miner's); the root `settings.gradle`
   list (line 32) keeps the projects.
3. Grep the old display names ("(Backport)") in `kubejs/client_scripts/namechanges.js`, `server_scripts/other/jeed_providers.js`, tooltip scripts, `mod_sides.md`.
4. Build CD only (`:creativeDelight:build --offline`), never a member's own task; SQ-CDJ-05.
### Gotchas
Miner's reads a donor jar (`miners_upstream/`, sha1 pinned, `UPSTREAM_DONORS` in build_deploy.py line 511): the nested jar must
still find it. Fungi's pack placed features hard-depend on `fungidelight:*`. Port Java keeps its packages (`net.mehdinoui.*`, `net.ribs.*`, `com.sammy.*`).
### Tests
Proposed CDM-01: all member mod ids load once; CDM-02: a world with Veggies, Vintage, Fungi and Miner's blocks loads with none
missing; CDM-03: mod list, JEI and WTHIT show "(Creative Delight)"; VD-, VTD-, FGD-, MND- suites unchanged.

## members #6: Ocean's and Oceanic: fishing tags and integration pass
Status Planned / KubeJS / data / `creativedelight/members #6`
### Now (checked 2026-10-31)
- Done since the row was written: display names (both repacks' `mod_name` "(Creative Delight)"); squid drops (`dishes/ocean #3`,
  CD `miners_delight/loot.js`, 1.1.03); tentacles removed and dishes on calamari (`members #24`, 1.1.05; #29 fix).
- Still in the pack: the fishing net tag fill in [loottags.js:19-49](../../kubejs/server_scripts/tags/loottags.js) (block tags
  `fishing_net/kelp`, `sea_grass`; item tag `fishing_net/coral` from Upgrade Aquatic; biome tags `fishing_net/warm_ocean`, `shrimp_ocean`,
  `ocean`, `river`), and the two tables `kubejs/data/oceanic_delight/loot_tables/gameplay/fishing_net_on_kelp.json`, `fishing_net_on_sea_grass.json`.
  The mixin is in the CD Oceanic member (`members/oceanic/.../mixin/OceanicDelightFishingNetMixin.java`); the loot condition
  `creativeengineering:biome_tag` stays in patcherMod.
- No `oceanic_delight/` folder in the CD trees yet (only `oceansdelight/`).
### Build
1. New CD `kubejs/data/creative_delight/kubejs/oceanic_delight/tags.js` with the fill (via `global.tags`, ids from the dicts);
   the coral line keeps `// requires: upgrade_aquatic` or a `Platform.isLoaded` guard. Tag ids stay `creativeengineering:fishing_net/*`.
2. The two tables move into the Oceanic repack overlay `development/creativeDelight/members/oceanic/src/main/resources/data/oceanic_delight/loot_tables/gameplay/`
   (MIT, overlay at the upstream path is allowed); delete the pack copies; clean-build (memory `repack-stale-resources`).
3. Integration pass (merge choices per `largecontent/mod-additions #5`): guardian food (Ocean's `guardian`, `guardian_tail`, elder guardian
   slices against Hostile Delight `guardianfillet` and family), takoyaki (`i.takoyaki` Miner's, `i.oceanic_takoyaki` Oceanic). Each merge
   uses the swappers (`creativedelight/swappers`) and `removed_items.js`.
### Decisions
- Takoyaki: keep Miner's (two calamari slots, in more recipes) and swap Oceanic's to it. Recommend yes.
- Guardian meat: one raw / cooked pair; recommend Ocean's `guardian` (knife drop, already in Ocean's dishes), Hostile's fillet swaps to it.
### Tests
Proposed CDM-08: SQ-CDJ-04 and SQ-FN-01 still pass after the move; CDM-09: merged items show one id in JEI and every recipe that took the dropped id takes the survivor.

## members #8: Mooshroom family into moreMooblooms
Status Planned / new jar / patcherMod / `creativedelight/members #8`
### Now (checked 2026-10-31)
patcherMod: `mooshroom/MooshroomFamily.java` (`MEMBERS` line 40 names `fungidelight:inky_cap_cow`, `morel_cow`), `mooshroom/MushroomTypeSetter.java`,
mixins `MushroomCowWalkTargetMixin`, `MushroomCowBreedingMixin`, `AnimalCanMateMooshroomMixin` (`creativeengineering.mixins.json` lines 178-180);
registered in `CreativeEngineering.java:261-262`; brown chance in `worldgen/SpawnTuning.java:130-196`. moreMooblooms 1.1.0 soft-depends on
Buzzier Bees, JEI, Blueprint, Alex's Mobs and Supplementaries; nothing of patcherMod. The Fungi cow spawn rows are in the pack's
`kubejs/generation_scripts/worldgen/mob_spawns.json` (read by `SpawnTuning`), not in patcherMod.
### Build
1. moreMooblooms `mods.toml`: mandatory `creativeengineering` dependency (version range).
2. Move the five classes (packages renamed), mixin json of moreMooblooms, refmap. 3. Gate registration on `fungidelight` loaded.
4. Fungi cow rows: stay in `mob_spawns.json` (pack data, works with either jar) unless moreMooblooms gets its own spawn data.
5. Remove patcherMod's copies and mixin entries in the same release (Q22 hard cut). 6. Build and install patcherMod and moreMooblooms together.
### Gotchas
`SpawnTuning` and `CEConfig` stay in patcherMod and must be public API for the move; `brown_chance` and the `mushroomSpawns` switch are patcherMod config.
### Tests
Proposed CDM-11: breeding any two of red, brown, inky, morel; brown chance per biome; inky / morel cows spawn; a world without `fungidelight` boots with moreMooblooms.

## members #9: Ender's Delight compliant rebuild
Status Pinned / new jar / `creativedelight/members #9`. Writeup 15; Miner's method ([miners_delight.md](../miners_delight.md)): fresh code, art and
data read from the unmodified donor jar, sha1-pinned. Do not start until the owner answers the licence question (upstream 1.1.4 declares MIT,
installed `endersdelight-1.19-1.2.2.jar` is ARR). Since 2026-10-25 the CD member `endersAdditions` (`enders_additions`, 1.1.4-ce-19.2-02)
already adds the 1.1.4 MIT content under `endersdelight:` ids (`largecontent/mod-additions #28`); a full rebuild would absorb it.

## members #10: Cutover release
Status Planned / build / docs / `creativedelight/members #10`
### Build
After #1: `tools/build_deploy.py --notes "..."` (skill `release`); `mod_sides.md` rows of the four ports merged into the CD row (both sides);
`installToMods` moves the old jars to `jar_archive/displaced/`; changelog bullet in `unreleased.md` for the jar swap and the "(Creative Delight)"
names (changelog.md 3.6, `tools/changelog_lint.py`); writeups' status lines; README index.
### Decisions
Release number: the first release after #1 lands (deployed now: client 1.1.06, server 1.1.05, `deploy/DEPLOYED.json`).
### Gotchas
Never replace jars while the game runs. The hard cut means worlds from before need patcherMod and CD in the same update.

## members #11: Coffee unification in Creative Delight
Status Planned / KubeJS / data / `creativedelight/members #11`
### Goal
One coffee family (Rustic's) with every coffee giving Caffeinated, and no coffee code left in pack folders that belong to CD.
### Now (checked 2026-10-31)
- Already in CD: the Farmer's Respite purge (startup and server `farmersrespite/item_merges.js`, 7 merge pairs, coffee recipe removals),
  Kettle coffees (`farmersrespite/kettle.js`), coffee names (`assets/creative_delight/kubejs/rusticdelight/client.js`), compost
  (`kubejs_startup/rusticdelight/compost_values.js`, `farmersrespite/compost_values.js`), coffee fluids (`kubejs_startup/creativeengineering/fluids.js`),
  canned coffee berries (CD `creativeengineering/canning.js`).
- Still in the pack: the Caffeinated swap [food_values.js:15-16](../../kubejs/startup_scripts/cooking/food_values.js) (coffee, milk, honey,
  chocolate, dark); the coffee fluid tags [fluidtags.js:33-39](../../kubejs/server_scripts/tags/fluidtags.js); the golden coffee bean Haste
  potion `potions.js:115`. food_values.js and potions.js are on Creative Brewing's Phase 1 list (creative_brewing.md 4.2), so they do not go to CD.
- Syrup, Pumpkin and Cherry Blossom Coffee (`i.syrup_coffee`, `i.pumpkin_coffee`, `i.cherry_blossom_coffee`) are not in the swap: they give
  Speed and Haste ([RDFoods.java:42-45](../../development/creativeDelight/members/rustic/src/main/java/com/SaintAlfonzo/creativedelight/rustic/registry/RDFoods.java):
  syrup 1800 / 1800; pumpkin Speed 1200, Haste II 1200; cherry blossom Speed II 1200, Haste 1200).
### Build
1. Add the three keys to the first `global.foodEffectSwaps` row (food_values.js:15). The swap takes the first `from` effect's duration
   and level, so the result is syrup 1:30, pumpkin 1:00, cherry blossom 1:00 at level II unless the decision changes it.
2. Move the seven coffee rows of fluidtags.js to CD `rusticdelight/tags.js` (fluid tags, `global.tags`).
3. Update the comment in food_values.js:9-10 and the farmersrespite header comments.
### Decisions
- Caffeinated durations for the three: recommend syrup 1:30 (as coffee), pumpkin 1:00, cherry blossom 1:00 level I (give it Speed I first
  via a separate swap row if level II is unwanted).
- Where the swap lives: stays with the effect swaps and moves with `creativebrewing/move #1`. Recommend so (routing rule), which narrows
  this row to the three coffees and the fluid tags.
### Gotchas
Startup script: restart needed. The swap reads ticks at startup, so RDFoods values must not change at the same time.
### Tests
SQ-RD-20 (all 8 coffees). Proposed RD-35: AppleSkin / JEED tooltip of the three coffees shows Caffeinated, no Speed or Haste.

## members #14: Rustic: peppers, medleys and stuffed peppers
Status Planned / KubeJS / data / `creativedelight/members #14`. **Built by the port; nothing left to build.**
### Now (evidence)
- Seeds: Dark in `minecraft:chests/ancient_city` and `ancient_city_ice_box` (the sculk chests), Pale in the 11 `skyvillages:chests/sky_village_*`
  and `cloudstorage:chests/loot_balloon`, share 0.04, 1-3 ([rusticdelight/loot.js:23-31](../../kubejs/data/creative_delight/kubejs/rusticdelight/loot.js));
  rare trader offer (`rusticdelight/trades.js`). Re-placing them is `loot/tables #13`.
- Items: all 9 slices, roasted slices, rolls, stuffed peppers; three medleys and Rice Roll Royale (RDBlocks.java:67-69, `PepperMedleyBlock`, `RiceRollRoyaleBlock`);
  roasted-pepper stuffed pepper recipes (rusticdelight/recipes.js:69-76, RD-33).
- Giant peppers: blocks, craft and cut, patch 1 in 64 chunks in `warm dry grassy` (`wild_crops.json` row `giant_bell_pepper`; rustic_delight.md 5.8).
- Values: bell pepper 2 / 0.2, slice 1 / 0.1 (RDFoods.java:25-26).
### Decisions
"Compare the list with the donor": done; every item on the list exists. Recommend TODO moves to Testing.
### Tests
SQ-RD-05, -08, -13, -14, -18, -24, -28, -30, -33.

## members #15: Rustic: pancake rework
Status Planned / KubeJS / data / `creativedelight/members #15`
### Now
- The port has 7 placeable pancake stacks (plain, honey, chocolate, cherry blossom, vegetable, pumpkin, coffee; `PancakeStackBlock`, 6 per plate,
  sneak-use returns one up to 12) with 1.7.0's pot recipes.
- Single pancake items retextured to the Supplementaries shape, shipped 1.1.05 (changelog 1.1.05 Blocks & Building); Supplementaries'
  pancake renamed Stackable Pancake, shipped 1.1.05; its recipes in CD [supplementaries/recipes.js:26-32](../../kubejs/data/creative_delight/kubejs/supplementaries/recipes.js).
- The open pancake work (pot recipe 3 batter + 2 sugar + 1 egg -> 4, syrup on stackable pancakes, keep or drop each overlap) is
  `merges/baking #1` (In Progress, [merges_baking.md](merges_baking.md)); the food type is `dishes/meals #12`.
### Decisions
Recommend: fold this row into `merges/baking #1` (same work, same overlap list rustic_delight.md 5.5) and close it.
### Tests
SQ-RD-04, SQ-RD-17; the `merges/baking #1` tests.

## members #16: Rustic: syrup changes and maple foods
Status Planned / KubeJS / data / `creativedelight/members #16`
### Now
- Maple foods done 2026-10-25: Maple Glazed Bacon = cooked bacon + syrup ([rusticdelight/recipes.js:36-38](../../kubejs/data/creative_delight/kubejs/rusticdelight/recipes.js));
  Maple Cookie = cookie dough + syrup + honey in the oven (recipes.js:24, the cookie family).
- Syrup (`i.syrup`, tag `i.t_syrup`): donor pot recipe, glass bottle + `#rusticdelight:syrup_ingredients` (apple, `#forge:crops/beetroot`) + sugar;
  `sugar_from_syrup`; used by Syrup Cookie, Syrup Cheesecake (pie), Syrup Coffee (also Kettle, `farmersrespite/kettle.js:84`), Syrup Sandwich, Sweet Salad.
- "Syrup changes" has no detail in TODO, rustic_delight.md 5.9 or creative_delight.md 31.
### Build
Only after the decision: syrup recipes through `R.fdCooking` / `R.createMixing` in CD `rusticdelight/recipes.js`; ingredient tag via `global.tags`
in `rusticdelight/tags.js`.
### Decisions
- What the syrup change is. Options: (a) more syrup sources in `#rusticdelight:syrup_ingredients` (sweet berries, sugar beet if present,
  any fruit), (b) a Create mixing route (Slice & Dice already copies the pot recipe heated), (c) syrup as a fluid for the basin.
  Recommend asking the owner; if no answer, close the row as done (maple half built).
### Tests
SQ-RD-07, SQ-RD-19. Proposed RD-37 once the change is known.

## members #17: Rustic: coffee foods and brown dye
Status Planned / KubeJS / data / `creativedelight/members #17`. **Built by the port; nothing left to build.**
### Now (evidence)
1.7.0 data loads as is (rustic_delight.md 5.2): Golden Coffee Beans from a raw or roasted bean, Honey Coffee without sugar, Coffee-Braised
Beef from any coffee ingredient, brown dye from roasted beans. Coffee Cheesecake is a pie (oven, recipes.js:31) and renamed Coffee Pie
([namechanges.js:302-303](../../kubejs/client_scripts/namechanges.js), shipped 1.1.05). Golden beans brew Haste (`potions.js:115`).
Related open work: `members #11`.
### Tests
SQ-RD-06, SQ-RD-21.

## members #18: Rustic: fried foods and calamari
Status Planned / KubeJS / data / `creativedelight/members #18`
### Now
- The port has Fried Fish, Fried Calamari, Fried Chicken, Fried Mushrooms (pot, `#rusticdelight:cooking_oil`), Fried Dough and Fried Dumplings,
  Calamari Soup (the "calamari pot"), Calamari Roll (2 slices + cooked rice). Glowing calamari fries and soups through `#forge:raw_fishes/calamari` (recipes.js:51-61).
- Fried Fish vs Fish Tempura: decided by `merges/dishes #11` (Planned: drop Fish Tempura and Raw Fish Tempura, keep fried fish).
- Calamari Soup vs Oceanic and Ocean's soups: open (rustic_delight.md 5.5).
- Fried Dough: `members #25` asks to remove it.
- None of `fried_fish`, `fried_dough`, `fried_dumplings`, `calamari_soup` has an `item_dict.js` key yet.
### Build
1. Add the dict keys for any id a script names. 2. Apply the decisions: drops through `removed_items.js` and `R.remove`, swaps through the swappers.
### Decisions
- Fried Fish: keep (follows `merges/dishes #11`). Calamari Soup: keep (only calamari soup; Oceanic's and Ocean's soups are guardian / shrimp / squid
  stews with other inputs). Calamari Roll: keep, gets an assembly in #26. Recommend closing the row once `merges/dishes #11` is built.
### Tests
SQ-RD-22. Proposed RD-38: JEI shows one fried fish route and no tempura.

## members #19: Rustic: cherry blossom foods
Status Planned / KubeJS / data / `creativedelight/members #19`. **Built by the port; nothing left to build.**
### Now (evidence)
`#rusticdelight:cherry_blossom_ingredients` (`i.t_cherry_blossom`, item_dict.js:1978) is rewritten by `RusticTranslator` to `vinery:cherry_leaves` and
`quark:pink_blossom_leaves`, so both leaves work (the decision "which leaves" = both). Cherry Blossom Cookie (oven), Cheesecake (pie),
Coffee, Pancakes, Roll all read it (recipes.js:23, 32).
### Tests
SQ-RD-23.

## members #20: Rustic: bell pepper dishes and pizza supreme
Status Planned / KubeJS / data / `creativedelight/members #20`. **Built 2026-10-25; nothing left to build.**
### Now (evidence)
Bell Pepper Pasta and Soup from any 3 peppers (donor) plus the grid pasta from 3 roasted peppers (recipes.js:63-67); bell peppers in
`#brewinandchewin:foods/pizza_topping` ([brewinandchewin/tags.js:22-23](../../kubejs/data/creative_delight/kubejs/brewinandchewin/tags.js));
Supreme Pizza: bell pepper, cheese, onion, mushroom, tomato sauce, cooked meat on a dough ([brewinandchewin/recipes.js:32](../../kubejs/data/creative_delight/kubejs/brewinandchewin/recipes.js));
names Supreme Pizza / Supreme Pizza Slice (namechanges.js:65-66). The "ingredient list" decision is this list.
### Tests
SQ-RD-06, SQ-RD-07.

## members #21: Rustic: food effects pass
Status Planned / KubeJS / data / `creativedelight/members #21`. **Built by the port; nothing left to build.**
### Now (evidence)
[RDFoods.java](../../development/creativeDelight/members/rustic/src/main/java/com/SaintAlfonzo/creativedelight/rustic/registry/RDFoods.java):
Golden Coffee Beans 5 / 1.2 (line 28); honey and chocolate pancake 4 / 0.6 with Speed 900 (49-50); vegetable pancake Haste 600 (52);
Potato Salad Regeneration 100 (65). These are 1.7.0's values (rustic_delight.md 5.3). The coffees' Speed / Haste become Caffeinated (`members #11`).
### Tests
SQ-RD-30 (AppleSkin values), SQ-RD-20.

## members #25: Rustic: crate, renames and removals
Status Planned / KubeJS / data / lang / `creativedelight/members #25`
### Now
- Coffee Pie rename: done ([namechanges.js:302-303](../../kubejs/client_scripts/namechanges.js)), shipped in 1.1.05.
- Glowing calamari items are CD startup items (`kubejs_startup/rusticdelight/glowing_calamari.js`, keys `i.glowing_calamari`, `_slice`, `_roll`
  item_dict.js:1834-1836); no crate. Rustic's own `calamari_crate` uses `cube_bottom_top` with FD's `crate_bottom`, and `calamari_crate_from_calamari` /
  `calamari_from_calamari_crate`; CD unpacks it in loot (`rusticdelight/loot.js:21`, `L.pack`).
- Fried Dough: Rustic pot recipe (cooking oil + `#forge:dough`); no dict key; nothing in the pack uses it.
- Veggies stuffed pepper (`i.stuffed_bellpepper`, `i.stuffed_bellpeppers_block`): not removed yet; that is `merges/dishes #3` (In Progress).
### Build
1. Glow calamari crate: block `creativeengineering:glowing_calamari_crate` in `glowing_calamari.js` (a `StartupEvents.registry('block')` like
   CD `creativeengineering/baking_ingredients.js:22-37`: wood material and sound, hardness 2 as FD crates), dict key `i.glowing_calamari_crate`;
   blockstate and `cube_bottom_top` model under `kubejs/assets/creativeengineering/` (bottom `farmersdelight:block/crate_bottom`, side / top palette-swapped
   from Rustic's calamari crate as the glowing calamari art was, notice beside it); 9 <-> 1 shapeless in CD `rusticdelight/recipes.js` (pattern:
   CD `creativeengineering/recipes.js:21-28`); `L.pack` row in `rusticdelight/loot.js`; `#minecraft:mineable/axe` and the crate tags through `global.tags`.
2. Fried Dough (after the owner confirms): dict key, `removed_items.js` row, `R.remove({ output: i.fried_dough })`.
3. Veggies stuffed peppers: leave to `merges/dishes #3`.
4. Changelog bullets (crate New, fried dough Removed); run `/cegen` placeable / compost generators for the new block.
### Decisions
- Remove Fried Dough: the note had a question mark. Recommend keep: it is the only oil-fried dough and not an overlap; remove only if the owner confirms.
### Tests
Proposed RD-39: craft 9 glowing calamari into the crate and back, break it with an axe, WTHIT and JEI show the name; RD-40 (if removed): Fried Dough hidden, no recipe makes or uses it.

## members #26: Rustic: sequenced assembly sushi and heated manual recipes
Status Planned / KubeJS / data / `creativedelight/members #26`
### Goal
Every sushi roll has a Create sequenced assembly; Rustic's pot meals get the pack's grid route from cooked ingredients.
### Now
- Pattern: CD [farmersdelight/recipes.js:209-235](../../kubejs/data/creative_delight/kubejs/farmersdelight/recipes.js) (salmon, cod, kelp roll, rice
  medley): `R.incomplete(key)` + `R.createSequencedAssembly(out, cooked_rice, [deploying steps], [key], e, id, loops)`; the shapeless craft stays.
  Incomplete items are registered from the list in [transition_items.js:29](../../kubejs/startup_scripts/changes/transition_items.js) (pack startup).
- Rustic rolls (all shapeless 2 of a filling + cooked rice -> 2): `bell_pepper_roll_<9 colours>`, `calamari_roll`, `cherry_blossom_roll`, plus the pack's
  `glowing_calamari_roll` (recipes.js:60); Rice Roll Royale (shapeless from 8 rolls). Other mods' rolls: Oceanic `egg_roll`, `fish_egg_roll`, `sea_pickle_roll`,
  `shrimp_roll`; Ocean's `elder_guardian_roll`, `fugu_roll`; Hostile `guardianroll`. No dict keys yet for the 9 pepper rolls or `cherry_blossom_roll`.
- "Heated manual recipes" read as the heatless meal rule ([farmersdelight/recipes.js:258-268](../../kubejs/data/creative_delight/kubejs/farmersdelight/recipes.js)):
  a grid route from cooked ingredients, ids ending `_manual_only`. Rustic has one (Bell Pepper Pasta, rusticdelight/recipes.js:63-67).
  Pot meals without one: Bell Pepper Soup, Calamari Soup, Spring Rolls, the 9 stuffed peppers.
### Build
1. Dict keys for the rolls. 2. `transition_items.js` rows (key, texture `rusticdelight:item/<roll>`). 3. In CD `rusticdelight/recipes.js`: one assembly per roll,
   cooked rice -> deploy filling x2 -> 2 rolls (as the FD rolls, 3 loops where the grid gives 2); Royale: bowl + the 8 rolls deployed.
4. Grid meals per the rule: soups take `i.bone_broth` and cooked forms, one more main ingredient than the pot; stuffed peppers: roasted pepper + `i.cooked_rice`
   + cooked filling. Fried foods and coffees stay pot / Kettle only.
5. Other mods' rolls in their own CD folders if the decision includes them.
### Decisions
- Scope of "every sushi": recommend all rice rolls in the pack (Rustic here, Oceanic / Ocean's / Hostile rolls in their folders, same commit).
- Confirm "heated manual" means the heatless grid rule. Recommend yes (it is the pack's only manual-vs-heated pair).
### Gotchas
Startup change (transition items, dict): full restart. Slice & Dice copies every pot recipe unless the id ends `_manual_only`.
### Tests
Proposed RD-41: JEI shows a sequenced assembly for each roll; a Create line makes each. RD-42: each new grid meal crafts by hand and not in an unheated basin.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| members #2 | Veggies Delight rows into CD | Testing | [creative_delight.md](../creative_delight.md) 27; harness CDM-04 clean; SQ-CDM-00 PASSED; open SQ-CDM-01 to -05 |
| members #3 | Vintage Delight rows into CD | Testing | creative_delight.md 27; CDM-05 clean; open SQ-CDM |
| members #4 | Fungi Delight rows into CD | Testing | creative_delight.md 27; CDM-06 clean; open SQ-CDM |
| members #5 | Miner's Delight rows into CD | Testing | creative_delight.md 27 (cup conversion in CD `dataTrees`); CDM-07; open SQ-CDM-05 |
| members #7 | End's Delight integration | Testing | [ends_delight.md](../ends_delight.md) 5; open SQ-EN-05, -08 (client), -07 earth-fill half |
| members #12 | Wild crops into CD | Testing | creative_delight.md 24; SQ-CM-09 PASS; open SQ-WCD-01 to -07 (group B) |
| members #13 | All pack food KubeJS into CD by mod | Testing | creative_delight.md 29; open SQ-CDF-01, -02, -04 to -08 |
| members #22 | Rustic cutover cleanups (Fruit Beignet, cotton) | Testing | [rustic_delight.md](../rustic_delight.md) 5.7; SQ-CM-30 PASSED r1031, nothing open |
| members #23 | Storage Delight name tag and JEI order | Testing | creative_delight.md 31; repackedStorage jar (2026-10-29 23:54) is in `mods/`, shipped 1.1.05; open SQ-CM-33, -34 (client) |
| members #24 | Seagrass salad, no tentacles | Testing | [ocean_delight.md](../ocean_delight.md) 5.5; shipped 1.1.05; SQ-CM-31 PASSED; open SQ-CM-32, -35 (with #29) |
| members #28 | Ender's Delight names | Testing | creative_delight.md 31; `namechanges.js` (line 275 block); shipped 1.1.05; open CQ-UI-02 (client) |
| members #29 | Calamari swap order | Bug in TODO, fixed | ocean_delight.md 5.5; open SQ-CM-32, -35 |
