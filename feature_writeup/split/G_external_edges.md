# Sweep G: external edges

Audit run 2026-11-01, read-only, for [creative_split.md](../creative_split.md) section 4 row G. Targets use the
vocabulary of [README.md](README.md). `PM` = `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`;
`CDS` = `development/creativeDelight/src/main/resources/kubejs_startup/`; `CBS` = the same under `creativeBrewing`.
Method: `grep -rn` over the CD and CB Java, Gradle files, `mods.toml`, and every `global.*` read in the CD/CB script
trees (jar `kubejs_startup/**`, `kubejs/data/creative_*/kubejs/**`, `kubejs/assets/creative_*/kubejs/**`), then the
own jars, `tools/generators.json`, `tools/build_deploy.py`, `mod_sides.md`, `settings.gradle` and `build-logic/`.
`moved_code_backup/2026-10-25/` under creativeDelight is a dead copy of pre-move patcherMod files and is ignored.

Fix vocabulary in the tables: **Common API** (the class or global lands in, or moves to, Common), **Calendar
dependency** (CD/CB soft-depend on `creativecalendar`), **soft dependency** (`ModList`/`global.X` guard on a content
jar), **stays** (the target is Common; nothing to do but the mod-id swap of Q1), **moves with CD/CB** (the referenced
class lands in the referencing bundle, so the edge becomes internal), **pack** (a pack-folder file; nothing moves).

## 1. Creative Delight and Creative Brewing into patcherMod

### 1.1 Gradle

| from (file) | to | target | fix |
|---|---|---|---|
| `development/creativeDelight/build.gradle:25` `compileOnly project(':patcherMod')` | patcherMod classes (1.3) | Common + CD + Calendar (per class) | After the carve: `compileOnly project(':creativeCommon')` for `CEConfig`/`MealSlotTooltip` if those stay Common; drop once cooking and crops Java are inside CD. |
| `development/creativeBrewing/build.gradle:18` `compileOnly project(':patcherMod')` | none today (CB Java has no `creativeengineering` import; the comment names CEConfig and the remainder buffer for future compartments) | Common | stays as `project(':creativeCommon')` once Q1 renames; CB's brewing move (creative_brewing.md 4.2) adds the real users. |
| `development/creativeBrewing/members/vinery/build.gradle:22` `compileOnly project(':patcherMod')` | `PM/time/WineTime` via `PackTime` | Calendar | Calendar dependency: `compileOnly project(':creativeCalendar')`. |

No CD member (`members/*/build.gradle`) depends on patcherMod; `members/blockheads` depends on `:backportedBrewin`
(CB member), a CD-to-CB compile edge outside this split's scope but worth a line in the graph.

### 1.2 mods.toml

| from (file) | to | target | fix |
|---|---|---|---|
| `development/creativeDelight/src/main/resources/META-INF/mods.toml:28-33` `[[dependencies.creativedelight]] modId="creativeengineering"` mandatory `[1.1.03,)` AFTER BOTH | patcherMod as KubeJS supplier, NCH/tags/compost, startup loader (`compat/kubejs/JarStartupScripts`, `KubeJSStartupJarScriptsMixin`) | Common | stays; at the Q1 swap the modId becomes `creativecommon` (version range reset). Add a non-mandatory `creativecalendar` AFTER dependency if CD takes any Calendar row (none today, 1.5). |
| `development/creativeBrewing/src/main/resources/META-INF/mods.toml:27-33` `modId="creativeengineering"` mandatory `[1.1.03,)` AFTER BOTH | same | Common | stays; same swap. Add `creativecalendar` mandatory=false AFTER for `PackTime` (1.6), replacing the `ModList.isLoaded("creativeengineering")` check. |
| `.../creativeBrewing/.../mods.toml:35-41` `modId="creativedelight"` mandatory AFTER | CD's `global.CD` | CD | unchanged by the split. |
| member `mods.toml` files (farmers, oceans, oceanic, ends, delightful, rustic, enders, hearth, blockheads, vinery, brewin) | none name `creativeengineering` (only a comment line citing creative_delight.md) | - | nothing. |

### 1.3 Java (CD and CB sources, `import com.SaintAlfonzo.creativeengineering.*`)

One row per import edge. All are in `development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/`
except the last.

| from (file) | to (PM class) | target | fix |
|---|---|---|---|
| `mixin/farmersdelight/CookingPotMenuRemainderMixin.java` | `cooking/RemainderBuffer`, `RemainderBufferHolder`, `RemainderSlot` | CD | moves with CD (cooking/ is CD per 2.2) |
| `mixin/farmersdelight/CookingPotRemainderMixin.java` | `cooking/RemainderBuffer`, `RemainderBufferHolder`, `RemainderOutputHandler` | CD | moves with CD |
| `mixin/farmersdelight/CookingPotScreenRemainderMixin.java` | `cooking/client/RemainderSlotFrames` | CD | moves with CD |
| `mixin/farmersrespite/KettleMenuRemainderMixin.java` | `cooking/RemainderBuffer`, `RemainderBufferHolder`, `RemainderSlot` | CD | moves with CD |
| `mixin/farmersrespite/KettleRemainderMixin.java` | `cooking/RemainderBuffer`, `RemainderBufferHolder`, `RemainderOutputHandler` | CD | moves with CD |
| `mixin/farmersrespite/KettleTankMixin.java` | `cooking/RemainderBufferHolder` | CD | moves with CD |
| `mixin/farmersrespite/KettleScreenRemainderMixin.java` | `cooking/client/RemainderSlotFrames` | CD | moves with CD |
| `qol/RemainderBufferQol.java` | `CEConfig.COOKING` | CD | config section moves with the feature (rule 3): becomes a `CDConfig` section, keys and comments kept |
| `mixin/farmersdelight/CookingPotMealTooltipMixin.java` | `compat/MealSlotTooltip` | CD? | Interface row showing only cooking data; its other readers in PM are `CckCookingGuideMealTooltipMixin` and `CckBrewingGuideMealTooltipMixin` (Central Kitchen, food). If sweep A keeps it Common: Common API, nothing to do. If CD: moves with CD. |
| `mixin/farmersrespite/KettleMealTooltipMixin.java` | `compat/MealSlotTooltip` | CD? | as above |
| `compat/wthit/WthitCropGrowth.java` | `crops/CropGrowth` (`evaluate`, `parse`, `Rule`, `Stage`, `FALLBACK_PROPERTY`) | CD | moves with CD (crops/ is CD). PM's other reader `compat/StateLines` (WTHIT) then needs CD soft or the rule-parsing part stays Common: flag for sweep A. |
| `compat/wthit/WthitCropGrowth.java` | `CEConfig.CROP_GROWTH.showHarvestable` | CD | config section moves with the feature into `CDConfig` |
| `development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery/forge/PackTime.java` | `time/WineTime` (`active`, `sunClock`, `worldDayMinutes`, `daysPerMonth`, `monthsPerLevel`, `maxLevel`, `cellarMultiplier`, `coldMultiplier`, `warmMultiplier`) | Calendar | Calendar dependency (1.6) |

Fully qualified references outside imports: none (one javadoc mention in
`members/oceanic/.../OceanicDelightFishingNetMixin.java` describing where the mixin came from).

CB's own sources (`development/creativeBrewing/src/main/java`) have no `creativeengineering` import today.

### 1.4 KubeJS globals read by the CD and CB script trees

Definers: `global.NCH`, `global.tags`, `global.compost` (and `compostOriginal`, `compostListening`) are patcherMod's
jar `kubejs_startup/creativeengineering/func/`. `global.item`, `global.blocks`, `global.fluid`, `global.effect`,
`global.removedItems`, `global.addItemMerges`, `global.itemMergeRun` are `kubejs/startup_scripts/_lib/`
(`item_dict.js`, `block_dict.js`, `fluid_dict.js`, `effect_dict.js`, `removed_items.js`, `item_merges.js`).
`global.trades` is `kubejs/startup_scripts/func/trade_helper.js`. `global.lootTuning` is
`kubejs/server_scripts/loot/05_tuning.js`, `global.biomeRules` is `kubejs/server_scripts/worldgen/_biome_rules.js`,
`global.stripRemovedItemTags` is `kubejs/server_scripts/_lib/recipe_scan.js`. CD defines `global.CD`,
`global.seasoning`, `global.foodValueCopies`, `global.delightfulFoodValues`, `global.fastToasts`; CB defines
`global.CB`. CD and CB members have no `kubejs_startup` of their own.

Reads of the Common globals, aggregated (382 read sites in all; one row per tree, count = files):

| from (tree) | to (global) | target | fix |
|---|---|---|---|
| `CDS/<mod>/*.js`, 42 mod folders (every folder except `creativedelight` reads `global.item` and `global.compost`; `creativedelight/` and `creativeengineering/` also read `global.NCH`, `global.tags`, `global.fluid`) | `global.item` (234 sites over CD), `global.compost` (80), `global.NCH` (67), `global.tags` (52), `global.fluid` (27), `global.blocks` (10) | Common (helpers rule 4; `_lib` dicts Q4) | stays |
| `kubejs/data/creativedelight/kubejs/<mod>/*.js`, 45 mod folders | `global.item`, `global.NCH`, `global.tags`, `global.fluid`, `global.blocks` | Common | stays |
| `kubejs/assets/creativedelight/kubejs/<mod>/*.js`, 16 folders | `global.item` | Common | stays |
| `CBS/creativebrewing/cb_core.js` | `global.NCH`, `global.tags` | Common | stays |
| `kubejs/data/creativebrewing/kubejs/**`, `kubejs/assets/creativebrewing/kubejs/**` | `global.CB` (16), `global.CD` (3) only | CB, CD | stays |

Reads of every other pack global, one row per file:

| from (file) | to (global) | target | fix |
|---|---|---|---|
| `CDS/alexsmobs/item_merges.js` | `global.addItemMerges` | Common? (`_lib/item_merges.js`; sweep F decides, item merges are a pack-wide system) | stays if Common; else Common API |
| `CDS/farmersrespite/item_merges.js` | `global.addItemMerges` | Common? | as above |
| `CDS/fruitsdelight/item_merges.js` | `global.addItemMerges` | Common? | as above |
| `CDS/moredelight/item_merges.js` | `global.addItemMerges` | Common? | as above |
| `CDS/neapolitan/item_merges.js` | `global.addItemMerges` | Common? | as above |
| `CDS/vintagedelight/item_merges.js` | `global.addItemMerges` | Common? | as above |
| `kubejs/data/creativedelight/kubejs/_core/item_merges.js` | `global.itemMergeRun` (`recipes`, `loot`, `inventory`, 3 sites) | Common? | as above |
| `CDS/botania/botania_items.js` | `global.removedItems` (2) | Common (`_lib`) | stays |
| `CDS/veggiesdelight/veggies_items.js` | `global.removedItems` (2) | Common | stays |
| `CDS/vintagedelight/vintage_items.js` | `global.removedItems` (4) | Common | stays |
| `kubejs/data/creativedelight/kubejs/creativeengineering/canning.js` | `global.removedItems` | Common | stays |
| `kubejs/data/creativedelight/kubejs/creativeengineering/mortar.js` | `global.removedItems` | Common | stays |
| `kubejs/data/creativedelight/kubejs/neapolitan/recipes.js` | `global.removedItems` | Common | stays |
| `kubejs/data/creativedelight/kubejs/_core/removed_item_tags.js` | `global.stripRemovedItemTags` (guarded `if (!global.CD \|\| !global.stripRemovedItemTags) return`) | Devtools? (`server_scripts/_lib` is Devtools per 2.1, but `recipe_scan.js` is a runtime tag strip CD relies on, not a dump) | Common API: keep `stripRemovedItemTags` in a Common server script, or accept the guard as a soft dependency on Devtools (then the strip is dev-only: flag for sweep F) |
| `CDS/creativeengineering/seasonings.js` | `global.effect` (`var ef = global.effect`) | Common (`_lib/effect_dict.js`, Q4) | stays. Note: effects content is CB's (creative_brewing.md), the dict is Common. |
| `kubejs/data/creativedelight/kubejs/corn_delight/trades.js` | `global.trades` | Common? (`startup_scripts/func/trade_helper.js`; `trader/TradeListings` is a Common API per 2.4) | stays if Common; sweep F confirms |
| `kubejs/data/creativedelight/kubejs/rusticdelight/trades.js` | `global.trades` | Common? | as above |
| `kubejs/data/creativedelight/kubejs/veggiesdelight/trades.js` | `global.trades` | Common? | as above |
| `kubejs/data/creativedelight/kubejs/rusticdelight/loot.js` | `global.lootTuning` (guarded `if (!global.lootTuning ...) return`) | Worldgen (`server_scripts/loot`) | soft dependency: already guarded; CD's loot rows silently do nothing without Worldgen. Alternative: `lootTuning` helper to Common API, rules stay Worldgen. |
| `kubejs/data/creativedelight/kubejs/veggiesdelight/loot.js` | `global.lootTuning` (guarded) | Worldgen | soft dependency, as above |
| `kubejs/data/creativedelight/kubejs/vintagedelight/loot.js` | `global.lootTuning` (guarded) | Worldgen | soft dependency, as above |
| `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.js` | `global.biomeRules.system(...)` (2 sites, unguarded) | Worldgen (`server_scripts/worldgen/_biome_rules.js`) | soft dependency needs a guard added, or `_biome_rules.js` (the rule engine, not the rules) becomes Common API. CD's wild crops are a worldgen feature living in CD (creative_delight.md 24): the owner may prefer `biomeRules` in Common. |

KubeJS load order (`PM/compat/kubejs/JarStartupScripts.java` javadoc): 1. patcherMod's own jar pack (NCH, tags,
compost); 2. the pack folder `kubejs/startup_scripts/` (dicts, items, rules); 3. every other mod jar's pack in
mod-list order (CD, CB, ...), "so they can read the dicts". CD's top-level `var i = global.item` reads depend on
step 2 preceding step 3. Moving the dicts into Common's jar `kubejs_startup` (Q4) puts them in step 1 (same pack as
the helpers), which keeps every CD/CB read valid; the carve jars' packs are step 3, so a dict read from an
Engineering or Equipment startup script also works, but any `global.<X>` a carve jar defines is visible to CD/CB
only if that jar sorts before them in the mod list (CB already relies on an AFTER dependency on CD for `global.CD`).

### 1.5 Generated data the CD trees read (pack files, not globals)

| from (file) | to (file) | target | fix |
|---|---|---|---|
| `CDS/creativeengineering/cans.js`, `comfort_nourishment.js`; `kubejs/data/creativedelight/kubejs/creativeengineering/{canning,can_tags,food_type_tags,mortar,placeable_tags,seasoning_tags}.js` | `kubejs/generation_scripts/food/{can_types,can_servings,food_types,comfort_nourishment,mortar_catalog,placeable,food_families}.json` | pack (generators `food_catalog`, `placeable_catalog`, `comfort_nourishment`, `mortar_catalog`, `food_families`; see 3) | pack; `can_servings.json` is already a runtime payload file in build_deploy.py (line 266) |
| `kubejs/data/creativedelight/kubejs/hearthandharvest/tags.js` | `kubejs/generation_scripts/worldgen/crop_seasons.json` | pack (Calendar data: PM `seasons/CropSeasons` reads the same file) | pack; CD reads a Calendar-owned file without a mod dependency: acceptable (file read), note in Calendar's doc |
| `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.js`, `wild_crops.json` | `kubejs/generation_scripts/worldgen/biome_traits.json` | pack (Worldgen data) | pack |

The CD folders named `creativeengineering/` (10 startup, 20 server, 1 client file: cans, baking, seasonings, salt,
cultivated meat, mortar, soil yield) are rows that touch patcherMod's own items. Rule 3.9 names a folder per mod the
rows touch; after the Q1 swap these items are registered by whichever jar owns them (Common or Engineering), so the
folder name needs a decision (keep `creativeengineering/` since the item namespace stays, which is this sweep's
recommendation; ids do not change).

### 1.6 PackTime

`development/creativeBrewing/members/vinery/src/main/java/net/satisfy/vinery/forge/PackTime.java` is the only
WineTime user outside patcherMod. Its single caller is `VineryConfig.java` in the same package. It is already a soft
dependency (`ModList.get().isLoaded("creativeengineering") && WineTime.active()`), with `compileOnly
project(':patcherMod')` for the type. `WineTime` is in `PM/time/` and already headed "Reserved for Creative Calendar";
its PM readers are `CreativeEngineering.java`, `CEConfig.java`, `time/TimePush.java`. Fix: the Calendar carve changes
the `isLoaded` id to `creativecalendar`, the vinery `build.gradle` line to `project(':creativeCalendar')`, the import
to the Calendar package, and adds a `mandatory=false` `creativecalendar` AFTER dependency to CB's `mods.toml`
(the member's `mods.toml` carries no pack dependency today). Nothing else in CD or CB references `time/` or
`seasons/` (every other "season" hit is the seasonings feature).

## 2. Own jars

### 2.1 Going into Creative Delight (owner, 2026-11-01)

| Project | mod id | version | plugin | mods.toml dependencies | patcherMod reference | mixins | kubejs_startup | config toml |
|---|---|---|---|---|---|---|---|---|
| `development/babyMobs` | `baby_mobs` | 1.0.0 | `ce.forge-mod` | forge, minecraft (BOTH) | none | `baby_mobs.mixins.json` (12 Java files) | none | none |
| `development/moreMooblooms` | `more_mooblooms` | 1.1.0 | `ce.forge-mod`, `compileOnly fileTree('libs')` (jei) | forge, minecraft; optional buzzier_bees (AFTER), jei, blueprint, alexsmobs, supplementaries, abnormals_delight, botania, delightful | none | `more_mooblooms.mixins.json` (24 Java files); `data/`, `assets/`, `packs/` resources | none | none |
| `development/incubationAdditions` | `incubation_additions` | 1.1.0 | `ce.forge-mod`, `compileOnly fileTree('libs')` (incubation, alexsmobs, untitledduckmod, blueprint, citadel, geckolib) | forge, minecraft, incubation (mandatory AFTER); optional alexsmobs, untitledduckmod | string only: `OLD_NAMESPACE = "creativeengineering"` in `IncubationAdditions.java` remaps nests saved under the old `creativeengineering:` ids (data fix-up, no class dependency) | `incubation_additions.mixins.json` (14 Java files); `packs/` | none | none |

Reverse edges from patcherMod and the pack to these three: `CEConfig.java` lines 3354, 3419, 3429 list their mod ids
in a display-name table (`"baby_mobs=Baby Mobs (Alfonzo)"` and so on; changelog or mod-name map, no code
dependency); `kubejs/startup_scripts/_lib/item_dict.js`, `block_dict.js`, `func/sample_syringe.js` use their item
ids; `CDS/incubation_additions/compost_values.js` already exists as a CD per-mod folder. `PM/mooshroom/`
(`MooshroomFamily`, `MushroomTypeSetter`) does not reference More Mooblooms; its only "moobloom" mention is in
`CEConfig`. The 2.2 question (mooshroom/ with More Mooblooms or with Worldgen fauna) is sweep A's; this sweep records
that no code edge ties it to `more_mooblooms`.

Nesting each as a CD member (compare `development/creativeDelight/members/rustic/gradle.properties` and
`settings.gradle`):

1. Move the folder to `development/creativeDelight/members/<dir>` (or leave it under `development/` and only
   re-point `projectDir`; the existing members all live under `members/`).
2. `settings.gradle`: remove the name from the plain list, add `'<projectName>': '<dir>'` to the CD members map.
3. `gradle.properties`: add `bundled_in=creativeDelight` (ce.forge-bundle refuses a member without it;
   `installToMods` of the member then refuses and names `:creativeDelight:installToMods`); `mod_group_id` and
   `mod_version` must be set (they are). The mod ids keep their underscores: they are existing ids (as Q2 for
   `creativedelight`), not new ones.
4. `development/creativeDelight/gradle.properties`: append the project path to `bundle_members`, and the old
   standalone jar glob (`babyMobs-*.jar`, `moreMooblooms-*.jar`, `incubationAdditions-*.jar`) to `replaces_jars`.
5. `build-logic`: nothing; `ce.forge-mod` members are supported as-is (rustic, enders, hearth, blockheads are
   `ce.forge-mod` ports; the repacks use `ce.forge-repack`).
6. `tools/build_deploy.py`: delete their three `IN_TREE_MODS` rows (members never ship alone, as the CD comment
   says) and let CD's `replaces_jars` retire the old jars. `IN_TREE_EXTRA_SRC['creativeDelight']` already covers
   `development/creativeDelight/members`.
7. `mod_sides.md`: fold their rows into the Creative Delight row's member list (`dump_server_mods.py` matches by mod
   id, and nested mod ids are still reported by Forge; keep a row per nested id only if the dump tool needs it: check
   how the existing nested members such as `farmersdelight` are listed; row 436 lists them in the notes column).
8. `CDConfig`/`creativedelight_members.txt` is generated from `bundle_members`, so the member-gated mixin plugin
   (`compat/CDMixinPlugin`, `development/shared/mixinplugin`) needs no edit; their own mixin jsons stay theirs.
9. The CD `kubejs_startup` already has `incubation_additions/`; `baby_mobs/` and `more_mooblooms/` folders only if
   rows exist for them (none today in CD; the pack dicts hold their ids).
10. The trough code (`PM/mixin/FeedingTroughModuleMixin`, `TemptGoalTroughRetryMixin`, `compat/TroughRetryAccess`,
    3 entries in `creativeengineering.mixins.json`) moves into CD's own Java with a `creativedelight.mixins.json`
    entry each (sweep B lists the side and config keys).

### 2.2 Not touched by the split

| Project | mod id | plugin | patcherMod reference | split |
|---|---|---|---|---|
| `development/creativeLabels` | `creative_labels` | `ce.forge-mod-refmap` (jei, moonlight, wthit compile deps) | none (`waila_plugins.json` is its own) | no |
| `development/exposureBackport` | `exposure` (+ `exposure_expanded`, `exposure_polaroid`, `exposure_catalog`, `exposure_extras`) | `ce.forge-mod-refmap` | string only: `OLD_NAMESPACE = "creativeengineering"` in `src/extras/.../ExposureExtras.java` remaps camera bags saved under `creativeengineering:<tier>_camera_bag` (patcherMod 1.0.3) | no. Note: 2.1 lists "camera bags" under Equipment; the bags are already in `exposure_extras`, so Equipment holds at most the `backpackskin/` and bag-related mixins, not the items. |

Also untouched: `backportedVeggies`, `backportedVintage`, `backportedFungi`, `backportedDisplay`, `backportedMiners`,
`repackedStorage` (standalone in-tree jars; `patcherMod/build.gradle` has `compileOnly project(':backportedBrewin')`
and `project(':backportedDisplay')`, reverse edges for sweep A).

## 3. tools/generators.json: output roots after the split

Run in game by `/cegen` (`PM/devtools/GeneratorCommand`, `GeneratorRunner`: Devtools). The generators themselves are
Python in `tools/` and stay pack tooling. Output roots and where each lands:

| Output root | Generators | Lands in |
|---|---|---|
| `kubejs/generation_scripts/worldgen/` (structure_config, biome_config + biome_baselines, structure_groups, soil_compat, nether_worldgen manifest, nether_block_swaps, meat_block_swaps, flower_features, terrain_adaptation_scan, loot_container_audit) | 10 | pack. Read at runtime by `PM/worldgen/*` (Worldgen) and `server_scripts/worldgen`, `tags/*` (Worldgen, Calendar for `crop_seasons.json`, `season_types.json`); `CEConfig` holds the paths. The folder stays in the pack; Worldgen's config takes the path keys. |
| `kubejs/data/minecraft/worldgen/noise_settings` (nether_worldgen) | 1 | pack (datapack); Worldgen's execution doc owns it |
| `kubejs/server_scripts/loot/` (`loot_config.json`, `loot_index.json`, `loot_census.md`) | 2 | Worldgen (rule 3.9: `kubejs/data/creativeworldgen/kubejs/loot/`); the generators' output paths change with the move |
| `kubejs/generation_scripts/food/` (food_types, placeable, food_dict_index, comfort_nourishment, mortar_catalog, can_* via food_catalog) | 5 | pack; read by CD (1.5) and `PM/food/PlacedFoodSol` (CD) |
| `kubejs/startup_scripts/_lib/item_dict.js` (food_catalog, placeable_catalog write into it) | 2 | Common (Q4: dicts into Common's jar `kubejs_startup`); the two generators' output path changes to the jar resource path, and the dict then needs a patcherMod/Common rebuild per regeneration (today a restart suffices). Flag for the Common execution doc. |
| `kubejs/generation_scripts/{stacks,names,physics,wthit}/` (stack_size_rules, name_audit, item_physics_audit, silk_touch_blocks) | 4 | pack; readers: `startup_scripts/changes/stack_sizes.js` (Common? sweep F), `client_scripts/name_fixes.js` (Common interface), itemphysic (Compat), `tags/wthittags.js` (Common) |
| `kubejs/assets/creativeengineering/lang/en_us.json` (effect_descriptions) | 1 | CB (JEED effect text is CB's per creative_brewing.md) or Common (lang of the `creativeengineering` namespace): sweep E decides; the generator's path follows |
| `kubejs/assets/creativeengineering/brewinandchewin/coaster` (coaster_models), `kubejs/assets/creativeengineering/jei_world` (jei_world) | 2 | CB (coasters); Common (JEI world tabs, interface) |
| `kubejs/data/creativeengineering/paintings` (immersive_paintings), `kubejs/data/creativeengineering/moonlight/soft_fluids` (soup_soft_fluids), `kubejs/data/immersive_weathering/block_growths` (ua_coral_growths) | 3 | Compat (paintings), CD (soup fluids), Worldgen or Compat (coral growths): sweep E decides |
| `kubejs/assets/displaydelight/` (display_delight_names, display_delight_models) | 2 | CD (Display Delight is a CD port) |
| `feature_writeup/*.md`, `feature_writeup/loot_catalog`, `feature_writeup/unused_textures/`, `pack/manifest.json`, `pack/MODLIST.md`, `tools/blockrunner_untagged_blocks.json` | 20 reports | docs and pack tooling; none ships |
| `kubejs/exported/food/catalog.json` (food_catalog) | 1 | dev dump, pack |

Every generator reads `kubejs/exported/` dumps written by `/dumpall` (`server_scripts/_lib/dump_*.js`): those scripts
and the `/cegen` runner are Devtools. The generators' `reads` keys were not audited here.

## 4. mod_sides.md

Rows today (matched by mod id; `tools/dump_server_mods.py` refuses to run if a jar in `mods/` has no row):

| Line | Side | Mod | mod id | Jar |
|---|---|---|---|---|
| 116 | both | Creative Engineering | `creativeengineering` | `creativeengineering-1.1.03.jar` (mods/ holds 1.1.04; the column is informational) |
| 117 | both | Creative Labels | `creative_labels` | `creativeLabels-1.0.0.jar` |
| 147 | both | Exposure (Backport & Extras) | `exposure` | `exposureBackport-1.9.19-ce-19.2-04.jar` |
| 183 | both | Incubation Additions | `incubation_additions` | `incubationAdditions-1.1.0.jar` |
| 211 | both | More Mooblooms | `more_mooblooms` | `moreMooblooms-1.1.0.jar` |
| 212 | both | Baby Mobs | `baby_mobs` | `babyMobs-1.0.0.jar` |
| 436 | both | Creative Delight | `creativedelight` | `creativedelight-1.0.0.jar` |
| 437 | both | Creative Brewing | `creativebrewing` | `creativebrewing-1.0.0.jar` |

perfBot (`ceperfbot`) and clientCapture (`cecapture`) have no row: neither jar is ever in `mods/`.

Rows each new jar needs (side from this sweep; every carve jar has both server logic and client guards today, as
patcherMod's row says "client-only parts are guarded", so `both` unless sweep B finds a jar whose mixins are all one
side):

| Side | Mod | mod id | Note |
|---|---|---|---|
| both | Creative Common | `creativecommon` | the remainder of patcherMod; row 116 becomes this at the Q1 swap |
| both | Creative Engineering | `creativeengineering` | row 116's id, new jar after the swap |
| both | Creative Equipment | `creativeequipment` | |
| both | Creative Worldgen | `creativeworldgen` | server-heavy; client parts (DistantClip, leaf decay visuals) keep it `both` |
| both | Creative Compat | `creativecompat` | |
| both | Creative Calendar | `creativecalendar` | |
| server (dev only) | Creative Devtools | `creativedevtools` | dev `mods/` only, never in a payload (Q6); `/cegen`, `/dumpall` and the probes are server commands. If clientCapture is nested in it (6), the nested `cecapture` is `client`, which makes the bundle `both` on a dev machine; the row should say dev-only so `dump_server_mods.py` never ships it. |

The three CD-bound jars lose their rows (or move into the CD row's member list) when nested.

## 5. settings.gradle and build-logic: adding an in-tree mod

`settings.gradle` includes every mod by name from a list and sets `projectDir = file("development/<name>")`; bundles
and their members are added in two more blocks (CD: a `name: dir` map under `development/creativeDelight/members/`;
CB: the same under `development/creativeBrewing/members/`). `pluginManagement { includeBuild 'build-logic' }`
supplies the convention plugins:

- `ce.forge-mod` (`build-logic/src/main/groovy/ce.forge-mod.gradle`): Forge setup, `mods.toml` expansion from
  `gradle.properties` (`mod_id`, `mod_name`, `mod_license`, `mod_version`, `mod_group_id`, `mod_authors`,
  `mod_description`), `jar_name`, `mixin_configs` (default `<mod_id>.mixins.json`, `none` for no mixins),
  `replaces_jars`, `bundled_in`, `shipped_jar_task`, `shared_sources`/`shared_base` (copies
  `development/shared/<module>/` into the jar under its own package: `mixinplugin` is the gated plugin template CD
  and CB use), `disable_target_validator=true` (SRG mixins with `remap = false`, as every new jar's mixins will be,
  rule 3.6), `resource_guard_*`, the `libs/` flatDir, reproducible jars, `archiveJar` to `jar_archive/`,
  `installToMods` (moves old jars to `jar_archive/displaced/`).
- `ce.forge-mod-refmap`: for Mojmap mixins needing a refmap (ports); not needed for the carve jars.
- `ce.forge-repack`: third-party jar repacks (CD members).
- `ce.forge-bundle`: nests `bundle_members` under `META-INF/jarjar/` with generated `metadata.json`; the own jar gets
  the `-own` classifier, `bundleJar` ships. Needed only by Devtools if it nests perfBot and clientCapture (6).

A new carve jar therefore needs: a folder `development/<name>/` with `build.gradle` (`plugins { id 'ce.forge-mod' }`
plus `compileOnly project(':patcherMod')` and the `libs/` jars its mixins target), `gradle.properties` (the keys
above, `mod_id` without underscore, `shared_sources=mixinplugin` and `shared_base=com.SaintAlfonzo.<modid>` for the
gated mixin plugin, `disable_target_validator=true`), `src/main/resources/META-INF/mods.toml` (dependency on
`creativeengineering` now, `creativecommon` after Q1, mandatory AFTER BOTH; KubeJS comes through Common),
`<modid>.mixins.json` with the plugin class, `pack.mcmeta`, and the name added to the `settings.gradle` list. Root
`gradle.properties` (versions) needs nothing.

## 6. perfBot and clientCapture (Devtools, Q5 closed)

| Project | mod id | What | Today | Nesting in Devtools needs |
|---|---|---|---|---|
| `development/perfBot` | `ceperfbot` | Server-side fake players for the perf suite (`tools/perf/suite/perf_suite.py`, `feature_writeup/perf_suite.md`): `CePerfBot`, `Bots`, `BotPlayer`, `BotConnection`, `BotCommands`, `TickLog` (6 files, no mixins, `mods.toml` side SERVER) | `ce.forge-mod`; `installToMods` throws (dev-only); `perf_suite.py` copies `build/libs/ceperfbot-*.jar` into its scratch server | `bundled_in=creativeDevtools`, Devtools as `ce.forge-bundle` with `bundle_members=:perfBot,:clientCapture`; `perf_suite.py` then copies the Devtools bundle (or keeps reading the member's own `build/libs` jar, which the bundle plugin still builds); `settings.gradle` entry moves to a Devtools members map |
| `development/clientCapture` | `cecapture` | Client-side driver for queued visual tests in a throwaway client (`feature_writeup/client_capture.md`: designed 2026-10-24, "not started" in its status table, though 11 Java files and `cecapture.mixins.json` exist: `CeCapture`, `CaptureDriver`, `CapturePlan`, `Steps`, `VirtualInput`, `StageScreen`, `TestWorld`, two mixins, a JEI plugin); `mods.toml` side CLIENT, optional jei | `ce.forge-mod`, `compileOnly fileTree('libs')` (jei); `installToMods` throws; the jar goes to `tools/client_capture`'s own game dir (that folder does not exist yet) | same as perfBot; because it is CLIENT-only and perfBot SERVER-only, a Devtools bundle carrying both is loaded on both sides, each member no-op on the wrong side (Forge honours per-mod `side` for dependencies, not for loading); the `mod_sides.md` row (4) must say dev-only |

Neither references patcherMod. Both keep their current ids (existing ids, as the CD-bound jars do); Devtools itself
is `creativedevtools`.

## 7. Summary

### 7.1 Edge list by fix type

**Stays (target is Common):** CD and CB `mods.toml` dependency on `creativeengineering` (becomes `creativecommon` at
Q1); CB `build.gradle` `project(':patcherMod')`; every CD/CB script read of `global.NCH`, `global.tags`,
`global.compost`, `global.item`, `global.blocks`, `global.fluid`, `global.effect`, `global.removedItems` (about 350
of the 382 read sites); probably `global.addItemMerges`/`itemMergeRun` (7 files) and `global.trades` (3 files),
pending sweep F.

**Moves with CD (edge becomes internal):** 7 CD mixins importing `cooking/Remainder*` and `cooking/client/RemainderSlotFrames`;
`WthitCropGrowth` importing `crops/CropGrowth`; `CEConfig.COOKING` and `CEConfig.CROP_GROWTH` (become CDConfig
sections). CD's `build.gradle` keeps `compileOnly project(':creativeCommon')` only for what stays Common.

**Common API (decision needed):** `compat/MealSlotTooltip` (2 CD mixins; CD? per the interface test, PM's own users
are Central Kitchen food mixins); `global.stripRemovedItemTags` (`server_scripts/_lib/recipe_scan.js` would be
Devtools by folder but is a runtime strip CD calls); `global.biomeRules` engine (unguarded CD read) if the owner does
not want CD to soft-depend on Worldgen.

**Soft dependency (content to content):** CD `rusticdelight/loot.js`, `veggiesdelight/loot.js`,
`vintagedelight/loot.js` on `global.lootTuning` (Worldgen; already guarded); CD `_worldgen/wild_crops.js` on
`global.biomeRules` (Worldgen; needs a guard or the Common API above).

**Calendar dependency:** `members/vinery/.../PackTime.java` on `time/WineTime` (soft today via `ModList`; the id,
import, Gradle line and a `mandatory=false` CB `mods.toml` entry change to `creativecalendar`). No CD edge into
`time/` or `seasons/`; CD reads the pack file `generation_scripts/worldgen/crop_seasons.json` only.

**Pack files:** CD reads 9 `generation_scripts/food/*.json`, `worldgen/crop_seasons.json`, `worldgen/biome_traits.json`
(pack folder stays; generator owners in 3).

**Own jars:** babyMobs, moreMooblooms, incubationAdditions have no patcherMod code edge (incubationAdditions and
exposureBackport carry the string `"creativeengineering"` as an old-id remap only); creativeLabels and
exposureBackport are not touched.

### 7.2 Checklist per new jar

For each of Engineering, Equipment, Worldgen, Compat, Calendar, Devtools (and Common at the Q1 swap):

1. `settings.gradle`: name in the include list (Devtools: plus a members map for perfBot and clientCapture).
2. `development/<name>/build.gradle`: `ce.forge-mod` (Devtools: `ce.forge-bundle`), `compileOnly project(':patcherMod')`
   (`:creativeCommon` after Q1), the `libs/` jars its mixins and compat code compile against (copied from `mods/`,
   git-ignored, as patcherMod/libs).
3. `development/<name>/gradle.properties`: `mod_id` (no underscore), `mod_name`, `mod_license=GNU GPL3`, `mod_version`,
   `mod_group_id=com.SaintAlfonzo.<modid>`, `mod_authors`, `mod_description`, `jar_name`, `mixin_configs`,
   `shared_sources=mixinplugin`, `shared_base`, `disable_target_validator=true`; `replaces_jars` only if it displaces
   a jar (none of the carve jars does; CD's gains the three own jars).
4. `src/main/resources/META-INF/mods.toml`: the jar's id, a mandatory AFTER BOTH dependency on Common, optional AFTER
   dependencies on the third-party mods its gated mixins target; CD and CB add `creativecalendar` as needed.
5. `src/main/resources/<modid>.mixins.json` with `plugin` = the copied gated plugin class; `pack.mcmeta`.
6. `src/main/resources/kubejs_startup/<modid>/` with the core file defining `global.<X>` (`on`, `feature`, `recipes`),
   one folder per mod the rows touch, each file headed `// requires: <modid>`; server and client trees authored in
   `kubejs/data/<modid>/kubejs/` and `kubejs/assets/<modid>/kubejs/` and copied into the jar by `processResources`
   (copy the `scriptTrees` block of `development/creativeBrewing/build.gradle`).
7. Config: `<modid>-common.toml` class (sections moved from `CEConfig` with keys and comments), the edited values in
   `config/` and `configs_edited/root_config/`; generator path keys (3) move with their readers.
8. `mod_sides.md` row (4); `tools/dump_server_mods.py` refuses an unlisted jar.
9. `tools/build_deploy.py`: an `IN_TREE_MODS` row `('<project>', '<jar prefix>', True)`, `IN_TREE_EXTRA_SRC` for its
   pack script trees, a `PINNED_MODS` row for any third-party jar its fixes target; Devtools gets no payload row
   (Q6) and its members none (as perfBot and clientCapture have none today). The `creativeengineering-*.jar` special
   case at line 643 (patcherMod build check) moves to Common at Q1.
10. Generators whose outputs move into the jar's script trees (3): `kubejs/server_scripts/loot/*` to Worldgen,
    `item_dict.js` to Common's jar, `displaydelight` assets to CD; update `outputs` in `tools/generators.json`.
11. The CD-bound own jars: `bundled_in`, `bundle_members`, `replaces_jars`, `IN_TREE_MODS` row removed, `mod_sides.md`
    row folded (2.1).
