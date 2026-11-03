# Pack hygiene: rule bypasses and reinvented solutions

Audit of the whole repo for code that builds a new solution where an existing one, or a written design rule,
says it should be done another way. Started by the Creative Delight `apiVersion` gate (finding 1), which adds
version handling between in-tree pieces that always ship together.

## 0. At a glance

| Part | Status |
|---|---|
| Audit (2026-10-27, read-only, whole repo) | DONE: 55 findings in 12 groups |
| Fixes | 42 of 55 fixed and 24 closed with no change, all BUILT 2026-10-27 and installed in `mods/`, none tested (section 5). Open: 11, 12, 18, 22, 23, 30 to 35, 53 (section 2), fix order in section 3 |
| Tests | PH-01 to PH-37 (section 8); server checks also in the scratch queue; each later fix adds its own |

- **Tags on each finding:** **Rule** = breaks a written rule (CLAUDE.md, memory, a writeup). **Reinvent** = a
  second solution beside an existing one. **Judgement** = possibly intended, owner call.
- **Biggest groups:** version gating and fallbacks for in-house code (A, 8 findings, the reference pattern);
  old-version conversions in the Brewin' and Vinery ports (B, which conflicts with alcohol.md 3.3); literal ids
  and repeated keys against the item dict (E, about 400 literal uses); copied Java and build infrastructure
  across the in-tree mods (H, I).
- **Quick wins:** ModernFix row in `mod_sides.md` (52, blocks the deploy preflight and `/dumpservermods`),
  the missing config backup (50), the emoji (54), the repeated item dict keys (19), the generator manifest (44).
- **Re-running the audit:** section 7.

## 1. Overview

**Scope:** `kubejs/` (startup, server, client, data and asset scripts), the jar-shipped `kubejs_startup/` trees,
the Java, mixins, `mods.toml` and `build.gradle` of every mod under `development/`, `build-logic/`, `tools/`,
`configs_edited/` against `config/`, `mod_sides.md` against `mods/`, the datapacks and the status docs.

**Excluded:** `exported/`, `generation_scripts/`, `**/build/`, `moved_code_backup/`, the upstream donor sources
(`*_upstream/`), third-party jars, and verbatim upstream code inside the ports (our own additions to them are in).

**Rules checked:** CLAUDE.md (helpers for recipes and tags, each item defined once, no old-version or cross-mod
conversions, no advancements, config backups, `mod_sides.md` rows, no emoji, status in the repo docs) and the
memory rules (Creative Delight routing, generator manifest, shared frameworks: compost, loot, item merges, food
values, `NCH.cfg`, removed items, armor and tool effects).

**Method:** four parallel read-only passes (server scripts, startup and client scripts, Java and build, tools and
data), each finding checked in the code; spot checks of the strongest claims afterwards.

## 2. Open findings

Paths are repo-relative. Line numbers are from 2026-10-27.

Completed and closed findings moved to section 5 under the same numbers.

### B. Old-version and cross-mod conversions in the ports

The port conversions (9, 10) were removed 2026-10-27 (section 5). This one is left:

11. **Remaps from our own mod splits.** Judgement (same rule's letter, but these move our own ids, not another version's
    or mod's). `incubationAdditions/.../IncubationAdditions.java:128` (`creativeengineering:` nests, split in patcherMod
    1.0.5; [nests.md](nests.md) line 347) and `exposureBackport/src/extras/.../ExposureExtras.java:35` (camera bags,
    patcherMod 1.0.3; [camera_bags.md](camera_bags.md) F3). Several releases old; removing them loses nests and bags in
    worlds not loaded since.

### C. Advancements

The pack drops every advancement at load (`ServerAdvancementManagerMixin`), so none of these grant anything. No
advancement is used for a reward or unlock. All Judgement: dead code and data.

12. **Dead advancement code and data.**
    - `backportedBrewin/.../forge/event/BnCCommonEvents.java:202-243` `checkTemperatureAdvancement`: a scan on every block place and right-click near a keg, for an advancement that no longer exists. Remove both listeners if this is our addition (not confirmed against upstream).
    - `moreMooblooms/src/main/resources/packs/blueprint/data/more_mooblooms/modifiers/advancements/husbandry/bred_all_animals.json` (our own mod).
    - `kubejs/data/watchtower_building/advancements/watchtower_found.json`.
    - Upstream advancement JSONs in the ports: Brewin' 49 (44 with rewards), Exposure 29 (16), Vintage 11 (2). Optional strip.

### D. Recipes and tags written beside the helpers

**Rule** (CLAUDE.md: recipes through `global.NCH`, tags through `global.tags`; extend the helper when it does not cover
a case). Counts: raw `e.custom` 21 calls in 6 files (12 are sequenced-assembly steps, see section 4), raw recipe
`e.remove` 16 in 3 files, raw tag `event.add/remove` 75 in 6 files. No `e.shaped` / `e.shapeless` / cooking calls anywhere.
13 to 17 are fixed (section 5). Left from them: the `mechanisms.js` vibrating step (a sequence step, section 4) and the
literal shape tag ids in `engineeringtags.js` (TODO `tooling/conventions #1`).

18. **Recipe disabling by path override.** Judgement. `patcherMod/src/main/resources/data/tmted/recipes/crusher/wheat_flour.json`
    and `wheat_flour_from_bale.json` are `forge:false` overrides (5c06f29), undocumented, where `NCH.remove` exists.
    Fix: document why, or move to `R.remove`.

### E. Each item defined once

**Rule** (CLAUDE.md: ids live in `global.item` and the block, fluid and food dicts; scripts use the keys).

22. **Literal ids where a key exists.** Rule.
    - Server: about 242 of 1352 literals. Hotspots: `tags/blocktags.js` (about 115, e.g. :89 `minecraft:stone_bricks`, :301 `minecraft:crying_obsidian`, :385 `minecraft:melon`), `tags/engineeringtags.js:22-26` (26 of 31, `i.vplanks_*`), `tags/item_physics_tags.js` (33), `potion_bottling.js:20-22, 73-93`, `tipped_arrows.js:39-46`; single cases in `transport.js:115, 120`, `potion_buckets.js:50`, `bee_nests.js:40`, `lava_vision.js:24`, CD `minecraft/recipes.js:33`.
    - Startup and client: 164 in 12 files: `client_scripts/namechanges.js` 84, `startup_scripts/cooking/potions.js` 36, `func/item_physics.js` 12, `items/sample_syringe.js` 7, `client_scripts/tooltips.js` 7, `func/potion_area.js` 5, `items/potion_buckets.js` 5, `client_scripts/potion_buckets.js` 3, `client_scripts/fluid_jei.js` 2, and one each in `items/echo_tools.js:22`, `jeichanges.js:38`, CD `corn_delight/corn_items.js:20`.
    - Judgement: 68 pack registrations use literals (`e.create('creativeengineering:...')` in components 22, misc_items 18, cultures 14, sample_syringe 8, ...) while CD registers with keys (`e.create(i.frosting)`).
23. **The effect dict is bypassed.** Rule. `startup_scripts/_lib/effect_dict.js:2-3` says scripts use `global.effect.<key>`.
    No server script uses it: `other/jeed_providers.js` has about 50 literal effect ids with keys. Startup: 46 in 8 files
    (`cooking/potions.js` 16, `func/tool_effects.js` 14, `func/armor_effects.js` 7, `cooking/food_values.js` 4,
    `func/potion_area.js:54`, `cooking/alcohol_drinks.js:50`, CD `vintagedelight/vintage_items.js:20-21`,
    `rusticdelight/glowing_calamari.js:20`). Only CD `seasonings.js` and `jei/order.js` use the dict.

### G. A second solution beside an existing one (scripts)

30. **Hand-rolled item merges.** Reinvent. CD `kubejs/data/creativedelight/kubejs/create/recipes.js:38-39` swaps
    `i.out_flour` (in `removedItems`) into `i.in_flour` with `R.replaceOutput` / `R.replaceInput`: recipes only, no
    loot or inventory. Judgement: `engineering/transport.js:111-122` swaps removed Little Logistics components to
    `i.redstone_link` in inputs. Fix: `global.addItemMerges`.
31. **Six private MobEffect resolvers.** Reinvent. `func/armor_effects.js:177` exposes the cached
    `global.armorEffects._resolveEffect` (tool effects reuse it). Copies: `cooking/food_values.js:22, :53`,
    `cooking/alcohol_drinks.js:47`, `func/potion_area.js:58`, CD `vintagedelight/vintage_items.js:16`. Fix: one resolver
    on `global.effect`.
32. **FoodProperties rebuild copied four times.** Reinvent. CD `creativeengineering/food_value_copies.js:34-39` calls
    itself "the one applier"; the same rebuild is in CD `delightful/delightful_foods.js:25-48` (with its own
    `global.delightfulFoodValues` table), CD `fruitsdelight/fruits_foods.js:10-31` and `cooking/alcohol_drinks.js:64`.
    Fix: absolute-value rows in the one applier.
33. **Tooltip helpers not shared.** Judgement (cause of finding 5's tooltip case). CD `_qol/placeable_tooltip.js:39-43`
    re-implements `removeTooltipKey` (`client_scripts/tooltips.js:80`) because pack client helpers are top-level
    functions the CD asset pack cannot see. Fix: put them on `global`, or ship them in the patcherMod jar.
34. **Armor and tool tuners.** Judgement. `func/tune_armor.js:57-75` and `func/tune_tools.js:59-84` duplicate the generic
    half (durability, stack size, fireproof, rarity). Factor out on the next change.
35. **Direct CEConfig reads.** Judgement, defensible. `magic/armor_effects.js:30-36` and `equipment/dragon_autorepair.js:25`
    (per-tick reads; `NCH.cfg` logs on each call), `worldgen/mushroom_spawns.js:46-52` (no `WorldgenConfig` getter yet,
    commented).

### K. Config backups, mod_sides.md, status docs

53. **Status docs.** Judgement. `todo/todo.txt` is "kept as history" (TODO.md:15) but ebb0648 still ticked a box in it;
    [client_capture.md](client_capture.md) and its README row say nothing is built while `development/clientCapture`
    has 11 Java files and `jar_archive/cecapture-0.1.0.jar`; `development/patcherMod/changelog.txt` is a Forge MDK
    leftover (635 lines) missing from TODO row 1's MDK list.

## 3. Suggested fix order

Open:

1. **Owner calls:** 11 (our own split remaps), 12 (dead advancement code), 18 (`forge:false` overrides), 30 to 35, 53.
2. **Literal ids and the effect dict (22, 23):** mechanical, file by file, with the scratch scanners from the audit.
   The counts in 22 predate the 2026-10-27 food moves and key unification; re-scan first.

Done (details in 9): the no-risk cleanups, the reference pattern (A), helper coverage (13 to 17), the food helper drift
(29), and the 2026-10-27 owner batch: conversions (9, 10), one key per id (20), the food moves (25 to 28), infrastructure
(H, I, J: 36 to 49, with 39 and 40 decided), config backups (51); 24 closed with no change.

## 4. Limits and decisions

- **Not findings:** the 183 raw `e.recipes.create.*` calls and 12 `e.custom` calls that are sequenced-assembly steps.
  The NCH step helpers call `.id()` and return undefined, so they cannot be nested; the open item is at
  `_lib/sequence_assembly_processes.js:53`.
- **Not findings:** `kubejs/data/creativeengineering/recipes/sawmill/*.json` and patcherMod's 211 in-jar sawmill JSONs
  (the documented `NCH.ieSawmill` exception); worldgen `event.add` calls (data writes, not tags); `enchants.js`
  `e.remove` (MoreJS filters); the Brewin' / alcohol food rows (reserved); patcherMod's food Java (oven, remainder
  API, cans, seasoning, counter toppers, feast plating, mooshrooms), which creative_delight.md 3.1 rule 3 and 3.2 keep
  there; `CDMixinPlugin`'s `FOR_VERSION` gate (third-party Fruits Delight 0.5.11); `clientCapture` having no
  `mod_sides.md` row (dev-only).
- **Literal-id counts** exclude recipe ids in `R.remove({id})`, NCH recipe-type ids, entity ids, globs and regexes.
  They are approximate; the scanners are rough regex passes.
- **Game-rewritten configs** (`panorama.json`, `salt-client.toml`, `worldedit.properties`, `rare-ice.properties`) are not
  listed under 51. `salt-client.toml` and `modernfix-mixins.properties` differ from their backups only by pack comments
  the game strips; the backups keep the comments.
- **Owner decisions 2026-10-27:** remove the port conversions (9, 10), keep our own split remaps (11, open), leave 24,
  the Vinery port owns its config (39), delete the Java server mods dump (40), live configs are the intended values (51),
  cultivated meat is reserved for a future Creative Cultivation mod (creative_delight.md 3.1 rule 4).

## 5. Completed

Fixed or closed findings, with the audit text kept as found and the fix noted on each. Details per batch in
section 9.

### A. Version gating and fallbacks for in-house code (the reference pattern)

The in-tree mods and the pack scripts are built and deployed together, so there is only ever one current copy of
each. Checks for an older copy add code paths that are never taken and hide a broken install instead of failing.

1. **Creative Delight gates on a patcherMod helper version.** **FIXED 2026-10-27** (section 9). Rule, Reinvent. Current state:
   - [neoCraftHelper.js:17](../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js) publishes `apiVersion: 1` on `global.NCH`.
   - [cd_core.js:19-22](../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_core.js) sets `CD.apiVersion: 1` (read nowhere) and `CD.needsNchApi: 1`; lines 49-52 log an error when `NCH.apiVersion` is lower or missing.
   - Introduced in c36dfc4 (Mid 1.1.03). The docs make it a rule for callers: [creative_delight.md](creative_delight.md) line 478 ("a caller checks it", 10.6), line 505 and the 10.3 row at line 862.
   - Tests depend on it: `tools/creative_delight_test/suite/` (`cdtest_startup.js:7`, `cdtest_probe.js:50` asserts `apiVersion >= 1`, `cdtest_client.js:2`) and [scratch_queue.md](testing/scratch_queue.md) SQ-CDT-02 expects "NCH apiVersion 1".
   - The uncommitted `NCH.replaceItems` addition (item merges) does not touch the gate and is clean.
   - Fix: remove `apiVersion` from NCH and CD, the check in `cd_core.js`, the cdtest assertion and the SQ-CDT-02 wording, and rewrite creative_delight.md 10.6 to "CD and patcherMod ship together; the mods.toml range is the only check".
2. **Creative Delight checks for patcherMod's loader class.** **FIXED 2026-10-27** (section 9). Reinvent. `CreativeDelight.java:42-47` does
   `Class.forName(JarStartupScripts)` and logs "Install the patcherMod built with this jar", while
   `creativeDelight/.../META-INF/mods.toml:31` already declares the mandatory dependency `[1.1.03,)`. Fix: delete
   the check and `LOADER_CLASS`.
3. **"Older jar" branches in the structure scripts.** **FIXED 2026-10-27** (section 9). Reinvent.
   [structure_overrides.js](../kubejs/server_scripts/worldgen/structure_overrides.js) names an older patcherMod
   jar on 13 lines, with fallback branches at :112-120, :471-515, :863 and :1338-1353; `MetaStructure.java:91`
   documents the pairing. The guards protect nothing: :109 loads `worldgen.WorldgenFiles` unguarded, so an older
   jar fails there first. Same in the dev dump `_lib/dump_structuregen.js:73`. Fix: drop the branches.
4. **Worldgen switch reads with "older jar: no switch" fallbacks.** **FIXED 2026-10-27** (section 9). Reinvent.
   [wild_flowers.js:50-55](../kubejs/server_scripts/worldgen/wild_flowers.js), `mushroom_spawns.js:46`,
   `mob_spawns.js:22`, `bee_nests.js:18`, and CD `_worldgen/wild_crops.js:42`
   (`global.CD.worldgen ? ... : true`). Fix: call the getter directly.
5. **Dead fallbacks for keys and globals that always exist.** **FIXED 2026-10-27** (section 9). Reinvent.
   - CD [_qol/placeable_tooltip.js:23-28](../kubejs/assets/creativedelight/kubejs/_qol/placeable_tooltip.js) `tagOf(key, fallback)` hard-codes five tag ids in case `i.t_placeable*` is missing; all five keys exist (`item_dict.js:13744-13748`).
   - [placeabletags.js:51](../kubejs/server_scripts/tags/placeabletags.js) and CD `creativeengineering/food_type_tags.js:56`: `i.t_placeable_on_coaster || '#creativeengineering:...'`.
   - CD `kubejs/data/creativedelight/kubejs/_core/item_merges.js:8`: `!global.itemMergeRun` (always set by `startup_scripts/_lib/item_merges.js:40`).
   - CD startup `compost.js:10` in farmersdelight, fungidelight, veggiesdelight, vintagedelight, and the CD client scripts (`endersdelight/client.js`, `ends_delight/*`, `fungidelight/jei_info.js`, `_core/cd_client.js`): `!!global.CD &&` inside the jar that defines `global.CD` at priority 1000. The routing memory documents this guard for server scripts only.
   - [seasoning_lang.js:3-8](../kubejs/client_scripts/seasoning_lang.js): `global.seasoning || {}`.
   - [tooltips.js:32-34](../kubejs/client_scripts/tooltips.js) `fdVillagerLine`: `Platform.isLoaded('creativedelight')` feature-detects the pack's own mod.
   - Judgement: `neoCraftHelper.js:1375` `global.seasoning && ...` (patcherMod's helper detects a global CD owns).
   - Fix: use the keys and globals directly; delete the fallbacks.
6. **patcherMod still carries Vinery 1.3.10 support.** **FIXED 2026-10-27** (section 9). Reinvent. Only `backportedVinery-1.5.5-ce-19.2-04` is in
   `mods/`, and it ships its own JEI (`core/compat/jei/port/`).
   - `patcherMod/.../compat/vinery/` (about 770 lines): a full JEI plugin for upstream 1.3.x that turns itself off when the backport is loaded (`VineryJEI.java:45`, `:83 isBackport`). The port's `PortCategory.java` is a near-copy of its `VineryCategory.java`.
   - Other 1.3.10 paths: `VineryIds.java:107` (reflective `satisfyu.vinery.config.VineryConfig`), `WinemakerTrades.java:25` (second class name), `WinemakerSpawner.java:179-190` (falls back to `config/vinery/config.json5`), `StuckInBlock.java:62` (`satisfyu` rule), the `TimePush.java:118` comment.
   - `patcherMod/libs/letsdo-vinery-forge-1.3.10.jar`.
   - Fix: delete `compat/vinery/`, the jar and the 1.3.10 branches; keep only the backport names. This retires [vinery_jei.md](vinery_jei.md).
7. **The Vinery port falls back for an older Brewin' port.** **FIXED 2026-10-27** (section 9). Reinvent. `backportedVinery/.../wine/WineYears.java:194-197`
   catches `ReflectiveOperationException` and warns that Brewin' has no `BnCHeatSources.climate`. The
   `ClassNotFoundException` branch (Brewin' absent) is legitimate. Fix: keep only that branch.
8. **patcherMod compiles against copied jars of in-tree mods.** **FIXED 2026-10-27** (section 9). Reinvent. `patcherMod/libs/backportedBrewin-4.5.0-ce-19.2-03.jar`
   (used by `CoasterRendererOffsetMixin.java:21`) and `patcherMod/libs/displaydelight-1.2.0.jar` (upstream 1.2.0,
   while in-tree `backportedDisplay` is 1.7.0; `compat/DisplayDelightDishBlocks.java` imports `com.jkvin114`). The root
   build already does `compileOnly project(':patcherMod')` for CD. Fix: `compileOnly project(':backportedBrewin')` and
   `project(':backportedDisplay')`, then delete the copies. The coaster offset could also live in the Brewin' port's
   renderer instead of a mixin on our own code.

### B. Old-version and cross-mod conversions in the ports

**Rule** (CLAUDE.md "no old-version or cross-mod conversions", added 2026-10-22). These conversions predate the rule
and are documented as port features: [alcohol.md](alcohol.md) Part A item 11 "Old-world conversion (3.3)", W2/W3/W6 at
line 2229; the README index lists "old-world conversion" for both ports. **Owner call:** removing them drops or
blanks the old content in any world that has not loaded the port since 1.0.7. The rule and the docs disagree until
one side changes.

9. **Brewin' port.** **FIXED 2026-10-27** (section 9).
   - `forge/event/BnCRemaps.java:27`: `MissingMappingsEvent` remaps 2.0 items and blocks (`item_coaster`, `scarlet_pierogies`) and the effect `satisfaction` to Farmer's Delight nourishment.
   - `common/mixin/BlockEntityLegacyIdMixin.java:16`: block entity id `item_coaster` to `coaster`.
   - `KegBlockEntity.java:132` (171-241): `isLegacyInventory` / `loadLegacyInventory` / `legacyDrinkToFluid` turn a 2.0 keg's 9 slots into the fluid tank.
   - `CoasterBlockEntity.java:111-126`: 2.0 coaster inventory to slot 0, `legacySizeFix`.
10. **Vinery port.** **FIXED 2026-10-27** (section 9).
    - `core/mixin/BlockEntityLegacyIdMixin.java:20`: `doapi:storage` to `vinery:storage`.
    - `forge/VineryCommonEvents.java:96-108` (map at :34): remaps 1.3.10's `improved_*` effects to vanilla and ignores the old `chair` entity.
    - `core/wine/WineYears.java:71` `convertLegacy` / `hasLegacyYear` (called at :104, :234, :248, :261, `DrinkBlockItem.java:157`, `:225`, `WineCommands.java:222`) with its own config key `VineryConfig.java:157 LEGACY_YEAR_LENGTH_DAYS`.
    - `FermentationBarrelBlockEntity.java:141-148` `convertLegacyLayout`.

### D. Recipes and tags written beside the helpers

**Rule** (CLAUDE.md: recipes through `global.NCH`, tags through `global.tags`; extend the helper when it does not cover
a case). Counts: raw `e.custom` 21 calls in 6 files (12 are sequenced-assembly steps, see section 4), raw recipe
`e.remove` 16 in 3 files, raw tag `event.add/remove` 75 in 6 files. No `e.shaped` / `e.shapeless` / cooking calls anywhere.

13. **Potion bottling and tipped arrows.** [potion_bottling.js](../kubejs/server_scripts/engineering/potion_bottling.js)
    :72, :90 write raw `create:filling` / `create:emptying` JSON; `NCH.createFilling` already takes NBT fluids
    (`tipped_arrows.js:44` uses it). :82, :114 and `equipment/tipped_arrows.js:36` write raw IE `bottling_machine` JSON.
    Fix: `R.createFilling` / `R.createEmptying`; extend `NCH.ieBottling` to build results with `_nbtItemJson` (as
    `ieBlueprint` does), then switch the three calls.
    **FIXED 2026-10-27:** `ieBottling` builds inputs and results with `_nbtItemJson` and writes fluid NBT as SNBT;
    `createEmptying` writes an NBT input as `forge:partial_nbt` through `e.custom` (KubeJS's own NBT ingredients are
    the ones that matched nothing for the Sample Syringe); all five calls use the helpers. Ids moved to the helper
    prefixes (`iebottling/`, `createfilling/`, `createemptying/`).
14. **Types with no helper, and raw removals.** `magic/storage.js:30-61` (3 `enderstorage:create_recipe` customs with
    no `.id()`, plus `e.remove`), `wood/ash.js:21-22` (`sootychimneys:soot_scraping` custom plus `e.remove`),
    `economy/coin.js:13-24` (12 `e.remove`). Fix: `R.remove`; add `NCH` helpers for the two types.
    **FIXED 2026-10-27:** `NCH.enderStorage` (ids `enderstorage/<name>`); soot scraping is the `soot` option of a
    new `NCH.axeScraping` (owner call: no soot-only helper; ids unchanged); the dirty chimneys got item dict keys;
    every removal goes through `R.remove`.
15. **Raw tag edits.** `tags/wood_tags.js:12-44` (23), `tags/equipmenttags.js:45-106` (44, in a file that also uses
    `global.tags.add`), `tags/fluidtags.js:89, 93` (literal fluid ids with `f.*` keys), `harvest.js` (4),
    `item_physics_tags.js` (2), and `_lib/recipe_scan.js:102` `removeAllTagsFrom` (`tags.clearAll` covers it).
    Fix: `t.add` / `t.remove` / `t.clearAll`.
    **FIXED 2026-10-27:** all through `global.tags`; the XP fluids got fluid dict keys (`f.flowing_experience`,
    `f.flowing_memory_essence`, `f.flowing_xp_still`).
16. **Tag logic re-implemented locally.** Reinvent. `tags/item_physics_tags.js:50-85` `physicsFromTag` redoes nested
    `#tag` adds and expand-with-except (`tags.addMatching`) and has its own `strip` (`tags._clean`).
    `other/harvest.js:17-41` `harvestSwap` / `setToolType` hand-roll "move to one tier or tool" with raw events.
    Fix: alias over `t.addMatching`; extend `tags.mineable` with an exclusive option and a `copper` level.
    **FIXED 2026-10-27:** `physicsFromTag` is a 3-line alias (its unused `expand` option dropped); `tags.mineable`
    takes a `copper` level, `tool` null, and `{ exclusive: true }` (also removes the gold and netherite levels, which
    `harvestSwap` skipped).
17. **Hand-written blueprint recipe JSON.** `kubejs/data/creativeengineering/recipes/blueprint/precision_sawblade_*.json`
    (12 files, from the 09-27 initial commit). `NCH.ieBlueprint` (`neoCraftHelper.js:2170`) handles the NBT result
    and base-ingredient counts. Fix: port to `engineering/blueprints.js`, delete the JSONs.
    **FIXED 2026-10-27:** a 12-row loop in `blueprints.js` (ids `blueprint/precision_sawblades/precision_sawblade_<profile>`),
    the shape tags as new `i.any_*` dict keys; JSONs deleted.

### E. Each item defined once

**Rule** (CLAUDE.md: ids live in `global.item` and the block, fluid and food dicts; scripts use the keys).

19. **Repeated keys in the item dict.** **FIXED 2026-10-27** (section 9). Rule. [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js): 58 keys are
    assigned 2 to 4 times with the same value, mostly a block pasted three times around lines 584-753 (`mulch_*` at
    585/604/627/732; `leather` 137/584/1664; `copper_casing` 547/671/697; `brown_mushroom` 622/643/750/1820). Last write
    wins silently. Fix: delete the repeats.
20. **One id, two keys.** **FIXED 2026-10-27** (section 9). Judgement. 77 ids have two different keys (e.g. `felsic_scrap` 49 / `scrap_felsic` 1712,
    `coil_hv` / `wirecoil_hv` 26-27, `ds_ore_*` / `deepslate_ore_*`, `explore_compass` / `explorers_compass`), where the
    hand-written and generated sections overlap. Fix: one key per id, the others as `i.x = i.y` aliases or dropped.
21. **Server scripts reassign dict keys.** **FIXED 2026-10-27** (section 9). Rule. Same value as the dict: `tags/wood_tags.js:6` `i.barrel` (dict :109),
    `engineering/end.js:7` `i.ender_shard` (:2118), `engineering/redstone.js:9` `i.mundane_potion` (:1374),
    `engineering/dust.js:8` `i.matcha` (:115), CD `creativeengineering/cultivated_meat.js:23` `i.culture_base` (:29).
    New key at runtime inside a recipe event: `wood/ash.js:7` `i.any_animal_fat = '#forge:animal_fat'`. Fix: delete the
    lines; move `any_animal_fat` into the dict.
24. **Block dict and item dict share 51 ids.** **CLOSED 2026-10-27** (owner: no change). Judgement, probably intended (separate registries; e.g. `b.coaster` /
    `i.coaster`). Alias where block and item ids match, or leave.

### F. Food code outside the Creative Delight trees

**Rule** (memory `creative-delight-routing`: all food code in the CD trees by mod since 2026-10-25; brewing, alcohol and
effects reserved for Creative Brewing). The Brewin' / alcohol rows in `cooking/cooking.js`, `cooking/alcohol.js` and
`tags/cookingtags.js` are the documented exception and are not listed.

25. **Meat cultures registered in the pack.** **FIXED 2026-10-27** (section 9). `startup_scripts/items/cultures.js:16-44` (7 cultures, 7 blocks); their
    recipes already moved to CD `creativeengineering/cultivated_meat.js`. Fix: move the file to CD.
26. **Member content in pack server scripts.** **FIXED 2026-10-27** (section 9). `tags/blocktags.js:20-22` (Vintage Delight ghost-pepper soil tag),
    `loot/09_crop_drops.js` (Rustic Delight bell-pepper drops, built 10-21 and not moved; creative_delight.md 29.3 does
    not exempt it), `tags/placeabletags.js` (its tooltip moved to CD `_qol/placeable_tooltip.js`, the tags stayed).
27. **Food rows in pack client scripts.** **FIXED 2026-10-27** (section 9). `client_scripts/seasoning_lang.js` (CD's seasoning lang),
    `tooltips.js` `fdVillagerLine` rows (CD repack behaviour, finding 5), 7 food renames in `namechanges.js:74-77`, 12
    food tooltips in `tooltips.js` (:495, :501, :508, :586-587, :723, :785, ...). Judgement: `items/misc_items.js:17`
    mortar and pestle (CD `bakeware.js` holds the other kitchen tools).
28. **Meal tooltip mixins in patcherMod.** **FIXED 2026-10-27** (section 9). Judgement. `mixin/CookingPotMealTooltipMixin.java:14` (Farmer's Delight) and
    `KettleMealTooltipMixin.java:14` (Farmer's Respite, which has about 10 compartment mixins in CD). A shared
    `MealSlotTooltip` may justify the split (creative_delight.md 3.1 rule 2).

### G. A second solution beside an existing one (scripts)

29. **Two food-family recipe helpers with drifted values.** **FIXED 2026-10-27** (section 9). Reinvent. `global.foodFamily` / `global.FOOD_FAMILY`
    (CD `creativeengineering/food_family_recipes.js:17, :44`) and `global.CD.food` / `global.CD.FOOD`
    (CD `creativedelight/cd_food.js:36, :56`) share method names. creative_delight.md 30 calls `CD.food` the successor,
    not yet adopted; 10 server files still call `foodFamily`. Values differ: veggie slice count 4
    (`food_family_recipes.js:21`) against 2 (`cd_food.js:38`), fruit cook xp 0.1 against 0.35 (`cd_food.js:43`).
    Migrating as is would change recipes silently. Fix: one table as the source now, then finish the migration.

### H. Copied Java infrastructure across the in-tree mods

36. **Upstream-donor plumbing per port.** **FIXED 2026-10-27** (section 9). Reinvent. `MemoryPackResources` in four copies (`backportedMiners/.../upstream/`,
    `backportedVinery/.../forge/upstream/`, `creativeDelight/members/rustic/.../upstream/`, `creativeLabels/.../client/`)
    plus patcherMod `worldgen/donor/DonorPackResources.java` (8 lines differ after the header). `UpstreamJars` (Miners
    and Rustic identical but for names, Vinery longer) and `UpstreamPacks` (3 copies, ids only) in the same folders.
    Fix: one class in patcherMod, or one shared source dir through `build-logic`.
37. **Two potion registrars.** **FIXED 2026-10-27** (section 9). Reinvent. patcherMod `potion/MagicResistancePotions.java:42-56` and
    `potion/BetterBeaconsPotions.java:44-50`: each has its own `DeferredRegister<Potion>`, supplier pattern and
    BASE / LONG / STRONG constants. Fix: one registrar with a table.
38. **Smaller copies.** **FIXED 2026-10-27** (section 9). Judgement. Built-in `packs/` registration in `MoreMooblooms.java:117-125` and
    `IncubationAdditions.java:107-122`; three near-identical `NEEDS_MOD` mixin plugins (`MoreMoobloomsMixinPlugin`,
    `IncubationAdditionsMixinPlugin`, `CLMixinPlugin`, where CD's `CDMixinPlugin` is the general form); two
    `ItemHiddenMixin` on `Item.allowedIn` (moreMooblooms, creativeLabels); two Fruits Delight bytecode-rewrite engines
    (patcherMod `compat/FruitsDelightHooks.java` + `CreativeEngineeringMixinPlugin.postApply`, CD
    `compat/FruitsDelightHooks.java` + `CDMixinPlugin.postApply`).
39. **patcherMod drives the Vinery port's config.** **FIXED 2026-10-27** (section 9). Judgement, documented ([time.md](time.md) 3.7). `time/TimePush.java:97-120`
    writes `VineryConfig` by reflection and reads `vinery-common.toml` by regex; `trader/WinemakerSpawner.java:162-190`
    runs its own spawner while the port's `WanderingTraderSpawnerMixin` swap is kept at 0. Option: the port reads CEConfig.
40. **Server mods dump in two languages.** **FIXED 2026-10-27** (section 9). Judgement, documented. patcherMod `devtools/ServerModsDump.java` mirrors
    `tools/dump_server_mods.py` line for line, including a second `mod_sides.md` parser (KubeJS could not copy files;
    `/cegen` can now run Python).

### I. Build scripts

41. **Refmap boilerplate in seven build files.** **FIXED 2026-10-27** (section 9). Reinvent. The `mixin { add ... }` block, the `mixinRefmap` compile output
    and the jar "refmap not in jar" check are copied into `backportedMiners:33`, `backportedFungi:32`,
    `backportedVinery:40`, `exposureBackport:136`, `creativeLabels:35`, `backportedBrewin:48`, `backportedVintage:36`;
    `build-logic/src/main/groovy/ce.forge-mod-refmap.gradle` only applies the plugin. Fix: move all three into it,
    driven by a `refmap_name` property.
42. **Upstream-resource checks copied.** **FIXED 2026-10-27** (section 9). Reinvent. `checkNoUpstreamResources` in `backportedMiners/build.gradle:42`,
    `backportedVinery/build.gradle:51`, `creativeDelight/members/rustic/build.gradle:23`, and `checkNoArrResources` in
    `members/enders/build.gradle:25` (that member was deleted 2026-11-02; the End's member now sets the check up through its `gradle.properties` keys). Fix: a convention with `upstream_jar` and `own_files` properties.
43. **Compiler flag and repo block per mod.** **FIXED 2026-10-27** (section 9). Judgement. `-AdisableTargetValidator=true` in 6 mods (already central in
    `ce.forge-repack`) and the same `flatDir 'libs'` block in about 13. Fix: a property in `ce.forge-mod`.

### J. Tools

44. **Generators missing from the manifest.** **FIXED 2026-10-27** (section 9). Rule (memory `cegen-generator-runner`). `gen_stack_sizes.py` (writes
    `generation_scripts/stacks/stack_sizes.json`, read by `startup_scripts/changes/stack_sizes.js`),
    `gen_nether_swaps.py` (writes `nether_block_swaps.json`, read by patcherMod `TemplateBlockSwaps`), `food_catalog.py`
    (writes `food_types.json` and `item_dict.js`), and the dump readers `jei_coverage.py` and `stack_sizes.py` (in the
    `dumpall-refresh-workflow` run order) are not in [generators.json](../tools/generators.json) or its exclusion list.
    Judgement: `gen_can_types`, `gen_compost_values`, `gen_moobloom_variants`, `gen_cfb_fridge_models`,
    `biome_rarity_floor`, and about 19 report tools run by hand. Fix: add entries, or name them in the `_readme`
    exclusions with the reason.
45. **Item dict re-parsed three more times.** **FIXED 2026-10-27** (section 9). Reinvent. `food_catalog.parse_dict()` (`food_catalog.py:248`, imported by 6
    tools) against own regexes in `gen_mortar_catalog.py:58` (misses 18 indented assignments), `gen_stack_sizes.py:114`,
    `lang_index.py:108`. Fix: import it, or move it into a small shared module.
46. **en_us tables rebuilt.** **FIXED 2026-10-27** (section 9). Reinvent. `effect_audit.py:55-82` and `gen_effect_descriptions.py:85-115` rebuild what
    `lang_index.build()` does, minus jar-in-jar members (the CD nests) and upstream-jar globbing;
    `kubejs/exported/display_names.json` has the effect names too.
47. **Four lenient JSON loaders.** **FIXED 2026-10-27** (section 9). Reinvent. `gen_crop_worldgen_table.py:268`, `nether_worldgen_scan.py:97` (which
    monkeypatches the other copy), `loot_breakdown.py:103`, `gen_structure_biome_table.py:87`, against
    `gen_structure_config.loads_lenient` (:264, already imported by two tools).
48. **Vanilla and Forge jar paths in 13 tools.** **FIXED 2026-10-27** (section 9). Reinvent. Five different hard-coded locations (`gen_structure_config.py:55-62`,
    an identical copy in `gen_loot_config.py:69`, `audit_loot_containers.py:20`, `port_structure_jars.py:36`,
    `potion_bucket_texture.py:26`, `loot_breakdown_scan.py:26`, `lang_index.py:26-27` with Forge pinned to 43.3.9,
    `gen_elytra_pattern_compat.py:41`, `gen_bedspreads_pattern_compat.py:35`, `blockrunner_scan.py:46`,
    `gen_soup_soft_fluids.py:39`, `gen_spawn_table.py:45`, `gen_crop_worldgen_table.py:58`), against
    `gen_structure_config.VANILLA_JAR`. Fix: one jar-locator module.
49. **Two structure NBT walks.** **FIXED 2026-10-27** (section 9). Judgement. `audit_loot_containers.py:107` walks every structure `.nbt` itself instead of
    the cached `loot_breakdown_scan.scan()` (which also covers `disabledmods/`), and uses a different vanilla jar.

### K. Config backups, mod_sides.md, status docs

50. **Hand-edited config without a backup.** **FIXED 2026-10-27** (section 9). Rule. `config/create_central_kitchen-common.toml` (`boostingCookerList`:
    kettle in 1.0.4 per plan_1.0.4.md:334, `miners_delight:copper_pot` uncommitted) has never been copied to
    `configs_edited/root_config/`.
51. **Backups out of step with live.** **FIXED 2026-10-27** (section 9). Judgement (may be in-game changes). `DistantHorizons.toml` (backup 256 / 2 threads /
    SSAO on, live 192 / 3 / off), `rubidium-options.json` (leaves FAST vs DEFAULT), `jei/recipe-category-sort-order.ini`
    and `jei/ingredient-list-mod-sort-order.ini` (JEI rewrote them: the backup's grouping and Vinery block are gone).
    Decide which side is intended.
52. **mod_sides.md rows.** **FIXED 2026-10-27** (section 9). Rule. `mods/modernfix-forge-5.18.1+mc1.19.2.jar` has no row, which stops the
    `build_deploy.py` preflight and `/dumpservermods` (so `/dumpall`). Six rows have no jar since Creative Delight nests
    them: `delightful` (:121), `farmersdelight` (:150), `rusticdelight` (:257), `ends_delight` (:439),
    `oceanic_delight` (:442), `oceansdelight` (:443); the CD row (:438) still says the member rows go "when it is
    installed", and the row count line needs updating.

### L. Minor

54. **Emoji.** **FIXED 2026-10-27** (section 9). Rule. `neoCraftHelper.js:1633` has U+2705 in a block comment. It ships in the patcherMod jar.
55. **Dead and stale lines.** **FIXED 2026-10-27** (section 9). `client_scripts/namechanges.js:1` `const CE = global.CE` (never defined, never used);
    CD `ends_delight/jei_info.js:4` points to `endersdelight/jei_info.js`, which is `endersdelight/client.js`.

## 6. File map

- Reference pattern: [neoCraftHelper.js](../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/neoCraftHelper.js), [cd_core.js](../development/creativeDelight/src/main/resources/kubejs_startup/creativedelight/cd_core.js), [creative_delight.md](creative_delight.md) 10.3 / 10.6
- Rules: [CLAUDE.md](../CLAUDE.md); dicts: [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js), [effect_dict.js](../kubejs/startup_scripts/_lib/effect_dict.js)
- Helpers: [tag_helper.js](../development/patcherMod/src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js), [item_merges.js](../kubejs/startup_scripts/_lib/item_merges.js)
- Manifest: [generators.json](../tools/generators.json); sides: [mod_sides.md](../mod_sides.md)

## 7. Offline checks

Re-run these to see whether a group is fixed (counts as of 2026-10-27 in brackets):

```
# A: version gates and old-jar fallbacks [apiVersion/needsNchApi 10 lines; "older jar" 17 lines in 5 files]
grep -rn "apiVersion\|needsNchApi" --include=*.js development kubejs tools | grep -v "/build/\|moved_code_backup"
grep -rnic "older jar" kubejs/server_scripts kubejs/data | grep -v ":0$"
# D: raw recipe and tag calls [e.custom 21, of which 12 sequence steps; 2026-10-27 after 3.3: 1, a sequence step]
grep -rEn "\be\.(shaped|shapeless|custom|smelting|blasting|smoking|stonecutting)\(" --include=*.js kubejs/server_scripts kubejs/data
# B: conversions [MissingMappingsEvent in 4 mods; 2026-10-27 after 9, 10: 2, finding 11]
grep -rln "MissingMappingsEvent\|Legacy" --include=*.java development | grep -v "/build/\|_upstream"
# L: emoji [1]
grep -rnP "[\x{1F300}-\x{1FAFF}\x{2600}-\x{27BF}]" --include=*.js --include=*.java --include=*.md development kubejs feature_writeup | grep -v "/build/\|moved_code_backup"
```

## 8. In-game test suite

Each fix adds its checks here as `PH-NN` (and to [scratch_queue.md](testing/scratch_queue.md) for server
checks) in the session it is made.

**3.3 helper coverage (2026-10-27).** PH-01 to PH-04 are server checks in the scratch queue (boot log, recipe and tag
dumps against HEAD). In game, after a restart with the new patcherMod jar:

| ID | Check | Expected |
|---|---|---|
| PH-05 | Scrape a dirty brick chimney and a dirty terracotta chimney with an axe, about 20 times each | Soot on about 75 % / 50 % of scrapes; JEI shows the 7 soot scraping recipes |
| PH-06 | Craft an Ender Pouch with hemp fabric in the W slots, and an Ender Chest; set a frequency on one and craft with it (JEI shows all 3) | Both wool and hemp fabric work; the Ender Storage frequency carries over as before |
| PH-07 | Spout an empty bucket with swiftness potion fluid; drain a swiftness and a strength bucket in an Item Drain; IE Bottling Machine: glass bottle and arrow in swiftness lingering fluid | Bucket of swiftness; each drain gives its own potion fluid back; a swiftness potion and a tipped arrow of swiftness |
| PH-08 | Engineer's Workbench with the precision sawblade blueprint | 12 profiles (button to vertical slab), each sawblade + shape + 2 steel plates |

**3.1 and 3.2 (2026-10-27).** PH-09 to PH-11 are server checks in the scratch queue (boot log, worldgen staging, dev
dump commands, `/cdtest`). In game, after a restart with the new patcherMod, CD and Vinery jars:

| ID | Check | Expected |
|---|---|---|
| PH-12 | JEI: look up a grape and the Fermentation Barrel | Only the Vinery port's pages (`vinery:` categories); no patcherMod "Vinery" pages; no missing-lang keys |
| PH-13 | Hover a placeable food (a pie), a Farmer's Delight rich soil block, and a seasoned food | Placeable lines as before; "Farmer villagers work it like Farmland"; the seasoning name, not a lang key |
| PH-14 | Place food on a Brewin' coaster on a counter; place a Display Delight dish block | Coaster item sits at the same height as before; dish blocks place and render |
| PH-15 | JEI: torch | The animal fat torch recipe (12 torches) is still listed |

**Owner batch (2026-10-27).** PH-16 to PH-26 are server checks in the scratch queue. In game, after a restart with the
new patcherMod, CD, Brewin', Vinery, Miner's, Creative Labels, More Mooblooms and Incubation Additions jars:

| ID | Check | Expected |
|---|---|---|
| PH-27 | A world saved with the ports (1.0.7 or later): a keg holding beer, ingredients and a tankard; a coaster with 1 to 4 items | Both load intact, nothing drops |
| PH-28 | Same world: a Fermentation Barrel with juice and ingredients, wines in racks, `/wine info` on one | Contents, fluid level and wine age as before |
| PH-29 | Look at a Miner's copper pot, a Vinery wine bottle, a Rustic bell pepper, a sticky note; open the resource pack screen | Donor art shows; no "reads its textures" error; the Creative Labels pack is not listed |
| PH-30 | JEI brewing for long reach, phantom bane, magic resistance; drink a strong phantom bane | All 9 potions listed and work |
| PH-31 | Creative tabs and search for Buttercup, the Buzzier Bees moobloom egg, Sticky It's note and eraser, a Labels label | All hidden |
| PH-32 | Shoot an arrow through Fruits Delight leaves; let a duck, goose and emu nest run; craft in a Creative Labels FastBench | Arrow passes as before; nests lay eggs; the bench result shows |
| PH-33 | Hover the finished-meal slot of a cooking pot and a Farmer's Respite kettle; open the Central Kitchen cooking and brewing guides | AppleSkin bars show in all four |
| PH-34 | JEI "culture"; a beef culture's uses; cook one with bone meal and cooking oil; mine a culture block with a hoe; cow syringe + culture base + slimeball | 7 cultures and 7 blocks with art and names; a cooking pot recipe for each culture, 3 out; the block mines fast; a beef culture |
| PH-35 | Plant ghost pepper seeds on soul soil and on dirt; hover a pie; put food on a coaster | Soul soil only; the Placeable line; the coaster takes it |
| PH-36 | Hover candy, pancakes, fish and chips, a fishing net, the Blockheads oven, white strawberries, rich farmland, a seasoned food; check the names Fish and Chips, Egg Fried Rice, Canned Tough Jerky, Sweet Berry Pie, Rose Rock Candy, Coffee Plant; use a mortar and pestle | Same tooltip lines and colours as before; seasoning name, not a lang key; the names; mortar has 512 durability and grinds |
| PH-37 | JEI: an incomplete coil (`kubejs:incomplete_coil_iron`); recipes for shields, ender chest, vessel charger, felsic deploying, ink arrow, sawdust | Grey wire-coil sprite; every recipe listed with its inputs |

## 9. History

- 2026-10-27: owner batch. Built, installed in `mods/`, not tested.
  - 9, 10: the Brewin' and Vinery conversions removed (remaps, legacy block entity ids, keg / coaster / barrel legacy
    loading, `WineYears` legacy years and `legacyYearLengthDays`); alcohol.md, time.md, README rows 51 and 52.
  - 39: the Vinery port owns `vinery-common.toml` and the Winemaker spawner (`[trader] ownSpawner`, `ownSpawnerInterval`;
    its timer file is now `vinery_winemaker_spawner.dat`); it reads the pack's `[time]` master through patcherMod
    `time/WineTime` (soft dependency). patcherMod's `TimePush` Vinery push and `trader/WinemakerSpawner` deleted,
    `winemakerEnabled` / `winemakerIntervalTicks` removed from CEConfig. time.md 3.7, wandering_trader.md.
  - 40: `devtools/ServerModsDump` deleted; `/dumpall` no longer runs it; `tools/dump_server_mods.py` is the route.
  - 36 to 38: shared Java under `development/shared/` (`packs`, `upstream`, `mixinplugin`, `itemhidden`), copied into
    each jar under its own package by the `shared_sources` convention in `ce.forge-mod`; `CEPotions` replaces the two
    potion registrars; one Fruits Delight bytecode engine (CD). pack_tooling.md 2.8.
  - 28: the meal tooltip mixins moved to CD; `MealSlotTooltip` stays in patcherMod (two Central Kitchen mixins use it).
  - 41 to 43: `ce.forge-mod-refmap` (`refmap_name`), the resource guard (`resource_guard_*`), `flatDir libs` by default and
    `disable_target_validator`; every jar byte-identical to the baseline after a clean build.
  - 44 to 49: `tools/pack_paths.py` (item dict parser, lenient JSON, jar locator); 15 manifest entries (45), every other
    tool named in the exclusions; outputs diffed before and after (pack_tooling.md 2.6).
  - 25 to 27: cultures, culture tags, the ghost pepper soil tag, bell pepper drops, placeable tags, seasoning lang, food
    tooltips and renames and the mortar and pestle moved into CD (creative_delight.md 29.5, 29.6). Cultivated meat is
    marked for a future Creative Cultivation mod. New: a cooking pot recipe beside each culture mixing recipe.
  - 20: 86 ids had 2 to 4 keys (77 items, 9 tags); 96 keys dropped, every use rewritten (most-used key kept;
    `explorers_compass`). One patcherMod helper line (`neoCraftHelper.js` felsic deploy) changed.
  - 51: the backups follow the live configs (DistantHorizons, rubidium, both JEI sort files). Also: mod_sides.md rows
    for the current patcherMod, Veggies, Vintage, Storage Delight and Create : The Salt jars (413 rows).
  - 24 closed with no change (owner).

- 2026-10-27: fix order 3.4 done (finding 29, TODO `creativedelight/framework #11`). Owner: `global.CD.FOOD` holds the rules
  (fruit cook xp 0.35, veggie slice 2), the helper's own ids, meat cultures their own family (`cultureCut`, no bone meal).
  The three calling files moved to `global.CD.food`, the seven unused `var F` lines removed, `food_family_recipes.js` deleted
  with no alias. No recipe changes today (no caller used the drifted values); the hand-written rows that will change with
  the rule are TODO `creativedelight/framework #12`. creative_delight.md 30, food_families.md 4.5; FFH-03 passed offline.

- 2026-10-27: fix order 3.1 and 3.2 done.
  - 52: ModernFix already had its row; the six Creative Delight member rows removed, CD row and count (412) updated;
    `dump_server_mods.py --dry-run` gives no warnings. 50: `create_central_kitchen-common.toml` backed up. 54: emoji
    removed. 55: `namechanges.js` line and the End's Delight pointer fixed.
  - 19: 55 keys repeated with the same value, 88 lines removed; the 3 keys the audit counted with them
    (`spring_lead`, `small_spring_lead`, `raw_netherite`) are `i.none` placeholders overridden later, left as they are.
    A node run of the dict before and after gives the same object. 21: the six reassignments removed; `any_animal_fat`
    became `i.t_animal_fat` (the dict's tag-key naming).
  - 5: every listed fallback removed, plus `_core/cd_client.js` (a check-only file) and the `itemMergeRun` guard
    (the documented `!global.CD` server guard stays). 2: the loader class check removed from `CreativeDelight.java`.
  - 1: `apiVersion` / `needsNchApi` and the check removed from NCH, `cd_core.js` and the cdtest suite; creative_delight.md
    10.3 / 10.6 and SQ-CDT-02 rewritten.
  - 3, 4: `structure_overrides.js` loads its patcherMod classes once at the top; also gone are three fallbacks the audit
    did not list (`frequencyStage`, `stageBiomeFactors`, `biomeRuleStage` and the "jar too old" error). Same in
    `dump_structuregen.js`, `mob_spawns.js`, `bee_nests.js`, `wild_flowers.js`, CD `wild_crops.js`, and the
    `/dumpbeenests` / `/dumpspawns` commands. `mushroom_spawns.js` now uses a new `WorldgenConfig.mushroomSpawnsEnabled()`.
    bee_nests.md, mob_spawns.md, creative_delight.md updated.
  - 6: `compat/vinery/` (JEI plugin, VineryIds), its 14 lang keys, the 1.3.10 branches in `WinemakerTrades`,
    `WinemakerSpawner` and `StuckInBlock`, and the `TimePush` comment removed; vinery_jei.md retired.
  - 7: `WineYears` keeps the Brewin'-absent branch; a missing `climate` method now throws instead of warning.
  - 8: patcherMod compiles against `project(':backportedBrewin')` and `project(':backportedDisplay')`.
  - Removed lib jars (`letsdo-vinery-forge-1.3.10`, `backportedBrewin-...-03`, `displaydelight-1.2.0`) moved to
    `development/patcherMod/jar_archive/displaced/`. Built and installed, not tested. The first combined build failed once
    in `:backportedBrewin:reobfJar` and passed on the rerun (that task also passes on its own).

- 2026-10-27: fix order 3.3 done (findings 13 to 17): patcherMod helpers `enderStorage`, `axeScraping` (soot option),
  NBT-aware `ieBottling` / `createEmptying`, `tags.mineable` `copper` + `exclusive`; 11 server scripts moved onto the
  helpers, 12 blueprint JSONs ported. Built and installed, not tested.

- 2026-10-27: audit written. Whole repo, read-only, four passes; 55 findings. Finding 1 recorded at its current
  state (apiVersion gate from c36dfc4, uncommitted `replaceItems` clean). No fixes made.
