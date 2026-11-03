# Fruits Delight 1.1.3 against the installed 0.5.11

**Built 2026-10-25 in Creative Delight, Shipped in 1.1.03 (deployed to server and players 2026-10-26); scratch rows SQ-FR-01 to -05 all PASSED (r1101 boot, r1102b 2026-11-02); client tooltip (FR-03) and play open** (section 5; tests FR-01 to FR-06). Installed: `fruitsdelight-0.5.11.jar` (`mods/`, `server_mods/`, `development/patcherMod/libs/`), 1.19.2.
Evaluated 2026-10-25 against `devmods/checkNewerVersionForContent/fruitsdelight-1.1.3.jar` (1.20.1 / Forge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Both jars unpacked in `DecompiledMods/`. Plan row:
`largecontent/mod-additions #20` in [todo/TODO.md](todo/TODO.md).

**Licence:** LGPL on Forge (owner-supplied 2026-10-25). By `mods.toml`, 0.5.11 is All Rights Reserved and 1.1.3 is LGPL-2.1. Not needed for anything below, since
1.1.3 adds no content, but upstream code from 1.1.3 could be reused under LGPL if a fix ever needs it.

**Result in one line:** 1.1.3 is a 1.20 port of the same mod. No item, block, effect or worldgen value is new. What matters
for the pack is one bug fix (placed Mangosteen Cake), two small recipe changes and two tags.

## 1. How it was read

- `python3 tools/jar_diff.py` (288 files added, 205 removed, 447 changed; en_us lang **+0 / -0 / ~0**, no new item or
  block model). Same registry in both jars.
- Recipes and loot tables compared semantically (file moves, wrappers, `group`, `category`, `random_sequence`, list order
  ignored): 151 recipe files and 200 loot tables change on disk, **3 recipes and 0 loot tables change in content**.
- Tags and loot modifiers compared by value.
- Classes decompiled with Vineflower (`~/.cache/ce-tools/vineflower-1.10.1.jar`), registrate package rename normalised,
  then diffed: food values (`FoodType`, `FDFood`, `FDJuice`, `FruitType`), effects, config, plants and worldgen
  (`FDTrees`, `FDBushes`, `FDMelons`, `FDPineapple`, `FDBiomeTagsProvider`), blocks, mixins.

The 1.20 share is almost all of it: worldgen moved from code to datapack JSON (same numbers), recipe files moved from the
`farmersdelight` namespace to `fruitsdelight` (same ids in 1.1.3's namespace, same content), creative tab API, advancements
(dropped by the pack), Sniffer content.

## 2. Changes and proposed decisions

### 2.1 Bug fix in the installed jar

| Change | Detail | Proposed |
|---|---|---|
| Placed Mangosteen Cake: bites give no effects | `MangosteenCakeBlock.eat` in 0.5.11 builds each effect as `new MobEffectInstance(effect, amplifier, duration / 3)`, so the duration is the amplifier (0 ticks) and the amplifier is a third of the duration (Comfort 400, Sliding 800). The effect ends on the next tick. 1.1.3 passes `(effect, duration / 3, amplifier)`: a bite gives Comfort 20 s and Sliding 40 s (a third of the item's 60 s and 120 s) | **fix**: patcherMod mixin on `MangosteenCakeBlock.eat` swapping the two arguments; player-visible, gets a bullet when built |

### 2.2 Recipes on existing items (data only)

| Change | 0.5.11 | 1.1.3 | Proposed |
|---|---|---|---|
| Fig Pudding (grid) | 2 `minecraft:egg` | 2 `#forge:eggs` | **carry over** (one `replaceInput` to `i.eggs`); fits the meal-ingredients audit (`dishes/meals #21`) |
| Lychee Cherry Tea (pot and heated mixing) | Spore Blossom | Cherry Leaves | skip (no cherry blocks in 1.19.2) |

Everything else (all cooking, cutting, Create, Botany Pots, jelly and jello recipes) is the same content under new paths.

### 2.3 Food values and effects

| Change | Proposed |
|---|---|
| Comfort -> Nourishment, same durations: Mangosteen Cake (60 s), Pear with Rock Sugar (60 s), Pineapple Fried Rice (300 s) | skip: Comfort kept, same decision as FD 1.3 ([farmers_delight.md](farmers_delight.md) 2.5) |
| Bellini Cocktail gets its own food type (peach effect at level 20 instead of 10) and loses its explicit Heal Aura 10 s | skip: the result is the same (Heal Aura 10 s, Nausea 5 s); the old jar listed Heal Aura twice |
| Every other food, juice, jelly and effect | unchanged |

### 2.4 Worldgen, loot, seasons

| Change | Proposed |
|---|---|
| Trees, bushes, Hamimelon and pineapple patches as datapack JSON | skip: same rarities, counts and biome tags as the 0.5.11 code ([crops_by_biome.md](crops_by_biome.md) stays right) |
| Durian sapling from Sniffer digging (5%) | skip (no Sniffer in 1.19.2) |
| Leaves, saplings and bushes in `sereneseasons:unbreakable_infertile_crops` | skip: only acts with `out_of_season_crop_behavior = 2`; the pack uses 0 (grow slowly) |
| Loot tables | unchanged |

### 2.5 Tags, other

| Change | Proposed |
|---|---|
| `forge:jams` gets `#fruitsdelight:jelly` (the 22 jam bottles) | **carry over** with `global.tags`; today `forge:jams` holds only Delightful's two jelly bottles. Do it with the jam merge (`dishes/jams #1`) or before |
| The 16 fruit crates in `forge:storage_blocks` (block and item) | proposed: carry over (Quark and Rustic crates are already there); owner's call |
| Hamimelon stems from `minecraft:crops` to `maintains_farmland`; bushes and stems in `sword_efficient` | skip (1.20 tags) |
| `l2harvester` harvest hooks on the bushes, leaves, pineapple and durian | skip (mod not in the pack) |
| Thirst compat config toggle, Create 6 version check, render-type mixin for leaves, Durian Flesh feeds Sniffers | skip (not installed / 1.20 only) |
| `getFoodData` null guard in `PlayerMixin` | skip: never seen in our logs or crash reports; revisit only if a crash names `fruitsdelight$getFoodData` |

## 3. Open questions for the owner

1. Build the Mangosteen Cake mixin (2.1)? Recommended.
2. Fig Pudding any egg (2.2) and `forge:jams` (2.5): carry over now, or with their family rows?
3. Fruit crates in `forge:storage_blocks`: yes or no.

## 4. Decisions (owner, 2026-10-25)

- fix mangosteen cake bug 2.1
- change fig pudding, keep lychee tea
- skip comfort swap, do comfort vs nourishment audit
- apply bellini cocktail fix
- add jam and storage block tags
- add the null guard

Owner, 2026-10-25: apply them only when Fruits Delight is loaded beside Creative Delight; Fruits Delight is not bundled
(it stays a standalone jar, no repack). The comfort vs nourishment audit is its own TODO row, not built here.

## 5. Built (2026-10-25)

All of it ships in the Creative Delight jar and is inactive without Fruits Delight (creative_delight.md 12).

| Fix (section) | Where | How |
|---|---|---|
| Mangosteen Cake bites (2.1) | CD `mixin/fruitsdelight/MangosteenCakeEffectMixin` | `@Pseudo` `@Redirect` on the `MobEffectInstance` constructor in `MangosteenCakeBlock.eat`, swapping duration and amplifier back. A bite gets a third of the item's durations: in the pack (Comfort 120 s from `foods.json`) Comfort and Sliding 800 ticks each (r1102b read 759 t 2 s after the bite; deep scan 2026-11-02, was 400 / 800) |
| `getFoodData` null guard (2.5) | CD `compat/CDMixinPlugin` + empty `mixin/fruitsdelight/FruitsDelightFoodDataAnchorMixin` (Player, priority 5000) + `compat/FruitsDelightHooks` | Same pattern as the leaf check (next row): `postApply` swaps the handler's `FoodDataAccessor.fruitsdelight$setPlayer` call for `FruitsDelightHooks.setPlayer(Object, Player)`, which skips a null FoodData. Same stack effect, no frames change; a warning if the call is not found |
| Leaf collision check (perf.md; in patcherMod until 2026-10-27) | CD `compat/CDMixinPlugin` + empty `mixin/fruitsdelight/FruitsDelightLeafCheckMixin` (BlockStateBase, priority 5000) + `FruitsDelightHooks.isLeafForProjectile` | `postApply` turns the merged `passableLeave` handler's leading `is(BlockTags.LEAVES)` into a projectile-first test; same result. Like every compartment mixin it is now off on a Fruits Delight other than 0.5.11 |
| Bellini Cocktail Heal Aura once (2.3) | CD `kubejs_startup/fruitsdelight/fruits_foods.js` | 0.5.11 lists Heal Aura 10 s (its own) and 5 s (peach at level 10: `FoodType.build` compares suppliers, so the fruit's effect is never skipped). The script keeps the longest entry per effect. It runs after the pack's `alcohol_drinks.js` (jar scripts load after the pack folder), so Intoxication and Nausea stay |
| Fig Pudding any egg (2.2) | `kubejs/data/creativedelight/kubejs/fruitsdelight/recipes.js` | `replaceInput` `fruitsdelight:fig_pudding`: `i.minecraft_egg` -> `i.eggs` |
| Jam and crate tags (2.5) | `.../fruitsdelight/tags.js` | `forge:jams` gets `#fruitsdelight:jelly`; the 16 crates go through `global.tags.storageBlock` (`forge:storage_blocks` and `forge:storage_blocks/<fruit>`, item and block, no mineable change). No pack recipe uses either tag today, so no player-visible change |

**Compartment gate.** `creativedelight.mixins.json` (CD's first mixin config) has `CDMixinPlugin`: a mixin in
`mixin.<modid>.` applies only when that mod is loaded, and Fruits Delight's only at version `0.5.11` (a newer jar may carry
the upstream fixes, and the cake redirect would then swap the arguments back to wrong). Another version logs
`[creativedelight] fruitsdelight <v> is not 0.5.11: its fix mixins are off`. The scripts carry `// requires: fruitsdelight`.
Build: CD compiles against `development/creativeDelight/libs/fruitsdelight-0.5.11.jar` (git-ignored, from `mods/`).

**Checks run (offline).** CD's own jar builds and holds the mixins, config and the three scripts; the reobfed cake mixin keeps
the `MobEffectInstance(MobEffect, int, int)` target. The null-guard patch run on Fruits Delight's real `PlayerMixin` handler
(renamed as Mixin merges it) swaps the call and the class writes. In KubeJS's Rhino: `tags.js` gives 65 tag entries
(jams + 16 crates x 2 tags x item/block); the Bellini dedupe keeps Heal Aura 200, Nausea and Intoxication. The full CD
bundle did not build at the time: the in-progress `:backportedRustic` member did not compile (not this change).

## 6. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-FR-02, -03, -04, -05, -05a.

| ID | Test | Where |
|---|---|---|
| FR-01 | Boot with Fruits Delight: no mixin error for `creativedelight.mixins.json`; no `null guard not applied` warning | SQ-FR-01 |
| FR-02 | Place a Mangosteen Cake, eat a slice: Comfort 0:40 and Sliding 0:40 (pack values) | SQ-FR-02 |
| FR-03 | Bellini Cocktail tooltip: one Heal Aura (0:10), Intoxication, Nausea 0:05; drinking gives Heal Aura 0:10 | SQ-FR-03, game |
| FR-04 | Fig Pudding in JEI / crafting accepts a duck egg | SQ-FR-04 |
| FR-05 | `#forge:jams` holds the 22 jams; `#forge:storage_blocks` and `/peach` hold the Peach Crate (item and block) | SQ-FR-04 |
| FR-06 | Boot without Fruits Delight: CD loads, no mixin error, the Fruits Delight scripts (5 in r1102b) skipped | SQ-FR-05 |

Scratch results: FR-01 PASS (r1101 boot), FR-02 to FR-06 PASS r1102b 2026-11-02 (`perf/runs/r1102b/s2/judged.md`; FR-02 and FR-04 batch regexes were stale, real PASS). Open: the FR-03 tooltip (client) and play. Deep scan 2026-11-02: new queue rows CQ-FR-01 (FR-02, FR-03 on a client).

## Bayberry Juice (2026-10-25, TODO `food/drinks #9`)

Bayberry Soup is renamed Bayberry Juice (item, Forge fluid and can; `namechanges.js`) and typed `drink`
(`food_types.json`, food dict `fruit_juice_bayberry`); ids and recipes unchanged. Its jar fluid
(`kubejs/data/creativeengineering/moonlight/soft_fluids/bayberry_soup.json`) is hand-written now, with the potion still
texture, so the soup fluid generator keeps it. Test RC-D2, MJ-14 (SQ-CDB-11).
