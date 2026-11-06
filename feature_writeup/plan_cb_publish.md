# Plan: Creative Brewing publish prep (dicts and helpers to Common, guards, namespace, names, licence files)

Written 2026-11-04. Owner request: "plan a dict and helper migration to common. ensure this plan includes guards on
scripts that require items from mods. ensure my license is in each of the creative mods. cb will work with the [4]
dependency jars. add a plan for the license audit but mark it as a different step than the migration work.
creativebrewing should be used for effect namespaces. id like brewin and vinery to display like creative delight in game".
Owner answers the same day: the 4 dependency jars stay (Creative Common, Creative Compat, Creative Calendar, Creative
Delight); everything CB registers moves to `creativebrewing:` (not only effects); names "Vinery (Creative Brewing)",
"Brewin' & Chewin' (Creative Brewing)", "Foundation (Creative Brewing)".
Second round, same day: D1 accepted; D2 yes, widened to "as many helpers as feasible in Creative Common" (2.3); D3 make
the Cooking for Blockheads and Incubation members optional with load guards and fold them into Creative Delight's own jar
(M8, section 3b). Third round, same day: D4 `GPL-3.0-only`; D5 no read-time potion alias, old potions in saves may break;
D6 every block M8 folds in moves to `creativedelight:` (11). All decisions closed.

**Status: Step 1 BUILT 2026-11-04, INSTALLED 2026-11-05 (C 1.1.0, P, L, W, D 1.1.0, B, Q, Engineering 2.0.0, Magic, Devtools; the old patcherMod jar `creativeengineering-1.1.09` displaced, so this install is also the Creative Split swap), not booted; scratch rows queued (8); Step 2 licence audit DONE 2026-11-04 (creative_brewing.md 9.2; fixes are TODO `licence/*` rows, owner sign-off pending).** Result: section 0b. TODO row `creativebrewing/publish #3`. Background: [creative_brewing.md](creative_brewing.md)
9 (licence audit 2026-11-01), [creative_split.md](creative_split.md) 7.4 and Q7.

Written for: the owner, and the session that executes it.

## 0. Goal and the two steps

**Goal:** Creative Brewing plus its four dependency jars (Common, Compat, Calendar, Creative Delight (CD)) and their own
third-party dependencies boot and work with an **empty pack folder** (no `kubejs/startup_scripts`, `server_scripts`,
`client_scripts`, no `kubejs/assets` overlay). Inside the pack nothing changes for the player apart from the names and
ids in M4 and M5.

| Step | What | Section | Gate |
|---|---|---|---|
| **Step 1: migration work** | M1 dicts and helpers to Common; M2 guards; M8 Blockheads and Incubation folded into CD, optional; M3 CB's pack-folder assets into the CB jar; M4 `creativebrewing:` namespace; M5 display names; M6 GPLv3 LICENSE in every Creative jar; M7 verification | 2 to 8 | M7 passes |
| **Step 2: licence audit** (separate, read-only, runs after Step 1 so it audits the jars that ship) | CB re-audit, Common, Compat, Calendar, CD; findings written up, fixes become their own rows | 10 | owner sign-off |
| Step 3 (not planned here) | Release prep: donor jars out of the public payload, public repo and tags, project page, satisfy notice, CBR-01 to -05 in game | creative_brewing.md 9 | Step 2 |

Rebuild map: **C** = Creative Common, **D** = Creative Delight, **B** = Creative Brewing, **P** = Compat, **L** = Calendar,
**E/Q/M/W** = Engineering, Equipment, Magic, Worldgen (M6 only). Every Java or startup change needs a restart; one jar at a
time, game closed. Common first: CD, Compat and CB all read it.

## 0b. Step 1 result (2026-11-04)

Built in one session (main session plus three agents: M8; M2 for CD and Compat; M2, M3, M4, M5 for CB). Nothing
installed, booted or committed.

| Item | Result |
|---|---|
| M6 | `LICENSE.txt` in 20 more folders (Oceanic and Rustic too: both are donor-jar members now, only own code ships); `mod_license` `GPL-3.0-only` in 42 `gradle.properties` (repacks keep the upstream part); Common's `mods.toml` reads `${mod_license}`. New `checkLicenseFile` in `ce.forge-mod.gradle` stops the jar when a GPL `mod_license` has no LICENSE file; every project passes |
| M1 | 12 dicts and `trade_helper.js`, `jei_order.js` in Common's `kubejs_startup/creativecommon/` (`git mv`); server `_lib` (3 files) to `kubejs/data/creativecommon/kubejs/_lib/`; client `jei_order.js`, `fluid_jei.js` and the helper half of `tooltips.js` to `kubejs/assets/creativecommon/kubejs/_lib/` (the pack's rows bind `global.tooltips` inside the event). Order holds: nothing in the pack folder touched the priority -10000 lists. Baseline before the move [dict_baseline_2026-11-04.json](testing/dict_baseline_2026-11-04.json) equals the moved copies (22 globals, `tools/dict_baseline/run.sh`); later dict edits (M4 buckets, M8 racks) change it on purpose. Tools via `pack_paths.STARTUP_LIB`; `build_deploy.py` classes the new trees dev-only. Common 1.1.0; all nine Creative jars require `[1.1.0,)`. 2.2 step 3: the Vinery reader is CB's `effect_swaps.js` (Vinery is required by CB), no change |
| 2.3 | `global.has` added (Common `func/has.js`, priority 2000). Loot engine **stays in Worldgen**: it reads Worldgen's `loot_config.json` through `CWGResources` and its post-roll half needs LootJS, so the move's precondition fails; CD's loot rules keep their `if (global.CWG)` and do nothing without Worldgen |
| M2 | G5 in NCH (absent mod's id, or `Item.of`/`Fluid.of` giving air/empty: call skipped and counted), `global.tags` (entries dropped), `global.tooltips`, CD's `F.*` food helpers; NCH helpers of a mod's recipe type return early; summary `kubejs/data/creativecommon/kubejs/_lib/absent_mods.js` (`[absent mods] skipped N ...`, after recipes). `tools/guard_check.py` (in `generators.json` as `guard_check`, group `checks`): CB 172 -> 0, CD 252 -> 0, Compat 5 -> 0, Common 29 -> 0. Marker `// G5` / `// G5 begin`..`end` for tables that only reach the helpers. Known checker gaps (open): `CD.on(...)`, `[..].every(Platform.isLoaded)`, local NCH wrappers, third-party hard deps, filtered tables, non-item colon strings, `// G5 end` on the last row, one-line `if` continuation, an NCH call line that also opens `{` |
| M8 | Both members are compartments of CD 1.1.0 (`compat/cookingforblockheads/`, `compat/incubation/`, `compat/Compartments`, `CompartmentPacks`); config in `CDConfig` `[cookingforblockheads]`, `[incubation]`, `[compartments]`; swap list `data/creativedelight/swaps/compartments.json` (65 entries, 71 old ids); members removed from the build; mixins under CD's `defaultRequire 1` (an injection that failed silently before now stops boot) |
| M3, M4, M5 | CB assets (buckets, Magic Resistance icon, 206 coaster models, lang) in the CB jar; everything CB registers is `creativebrewing:`; `data/creativebrewing/swaps/creative_split.json` +41 entries (42 old ids); no potion alias (D5). Names "Vinery (Creative Brewing)", "Foundation (Creative Brewing)" |
| Builds | C 1.1.0, Compat, Calendar, Worldgen, CD 1.1.0, CB, Equipment, Engineering, Magic, Devtools: all pass, each with its LICENSE. CB, Compat, Calendar and the others keep their version numbers (bump at release) |

Open after Step 1: `/dumpall` refresh in game (M4 step 5), then `/cegen item_physics_audit` (regenerates
`item_physics_families.json`, stale `incubation_additions:` rows); checker gaps above; the M7 scratch pass. Done 2026-11-05:
`rp_bench.py` KEEP list (`creativedelight:`, `creativedelight_compartment_art`), stale `incubation_additions` name
override removed from `CommonConfig.java` (Common rebuilt and installed).

## 1. Findings this plan rests on (2026-11-04)

### 1.1 Pack-folder globals the chain jars read

Counted from the jars' script trees (startup in `kubejs_startup/`, server in `kubejs/data/<modid>/kubejs/`, client in
`kubejs/assets/<modid>/kubejs/`, members included):

| Jar | Scripts | Pack-folder globals read (uses) |
|---|---|---|
| Creative Brewing | 32 | `item` 27, `fluid` 5, `effect` 2, `trades` 6, `tooltips` 6 |
| Creative Delight | 372 | `item` 320, `fluid` 31, `blocks` 21, `removedItems` 20, `food` 4, `effect` 2, `itemMergeRun` 3, `addItemMerges` 2, `stripRemovedItemTags` 2, `trades` 3, `tooltips` 5 |
| Creative Compat | 17 | `item` 13, `fluid` 3, `blocks` 1, `removedItems` 1, `tooltips` 4 |
| Creative Common (own jar scripts) | 3 | `item` 18, `fluid` 9 |
| Creative Calendar | 4 | none |

Where they are defined today (all pack folder, none in a jar):

| File | Lines | Defines |
|---|---|---|
| `startup_scripts/_lib/item_dict.js` | 15270 | `item` (+ `subdict`) |
| `startup_scripts/_lib/food_dict.js`, `food_grammar.js` | 1669, 73 | `food`, `foodGrammar`, `foodKey` |
| `startup_scripts/_lib/fluid_dict.js`, `block_dict.js`, `effect_dict.js`, `tool_dict.js`, `misc_dict.js`, `multiblock_dict.js` | 293, 250, 55, 151, 268, 36 | `fluid`, `blocks`, `effect`, `tool*`, `misc*`, `multiblock` |
| `startup_scripts/_lib/removed_items.js`, `removed_woods.js`, `item_merges.js` | 399, 463, 90 | `removedItems`, `removedWoods`, `removedWoodTracks`, `itemMerges`, `itemMergeGroups`, `itemMergeRun`, `addItemMerges` |
| `startup_scripts/func/trade_helper.js`, `jei_order.js` | 95, 141 | `trades`, `jeiOrder` |
| `server_scripts/_lib/removed_item_tags.js`, `java_guard.js`, `export_io.js` | 12, 32, 30 | `stripRemovedItemTags`, `guardThrowable`, `writeExportJson` |
| `client_scripts/tooltips.js` | 573 | `tooltips` (helper and the pack's own tooltip rows in one file) |

Already in Common's jar: `global.NCH`, `global.tags`, `global.compost` (`kubejs_startup/creativecommon/func/`).

### 1.2 Hard dependencies of the chain

`creativecommon` needs `kubejs`; Compat needs Common; Calendar needs Common and Compat; CD needs those three and
`farmersdelight` (met by CD's own nested Farmer's Delight member); CB needs all four. **Inside CD, members add hard deps:**
`blockheads` needs Cooking for Blockheads and Balm, `incubation` needs Incubation. A nested member with a missing hard
dependency stops the game, so today the minimal chain also needs CfB, Balm and Incubation. Decided (D3): both members
become optional compartments of CD's own jar (M8); after it the chain needs none of the three.

### 1.3 Script guards today

Common's jar loader (`compat/kubejs/JarStartupScripts.java`) honours `// requires:` per file, as KubeJS does in the pack
folder. Files sitting in a mod-named folder with no matching `// requires:` header: CB 0, CD 4, Compat 11. CB's 22 files in
pack-native folders (`creativeengineering/`) have no header and mix many mods per file:

| CB file (pack-native folder) | Dict uses | Third-party ids named |
|---|---|---|
| startup `potions.js` | 8 | alexsmobs 22, buzzier_bees 12, rusticdelight 5, upgrade_aquatic 4, quark 3, ends_delight 1 |
| startup `armor_effects.js` | 2 | immersiveengineering 4, alexsmobs 4, better_beacons 2, botania 1 |
| startup `tool_effects.js` | 1 | alexsmobs 6, vinery 1, supplementaries 1, botania 1 |
| startup `effect_swaps.js`, `alcohol_drinks.js` | 2, 1 | farmersrespite 2; brewinandchewin 2 |
| server `jeed_providers.js` | 1 | alexsmobs 12, xaerominimap 8, immersiveengineering 7, botania 6, xaeroworldmap 4, better_beacons 4, and 8 more |
| server `potion_bottling.js` | 2 | create 2, immersiveengineering 1 |
| server `tipped_arrows.js`, `fluid_tags.js` | 2, 1 | via dict keys only |
| client `potion_fluid_jei.js` | 0 | create 1 |

Dict keys hide the namespace (`global.item.x` can be any mod), so a grep is not enough: the guard pass resolves each key.

### 1.4 What CB registers under another mod's namespace

| Kind | Ids today | Registered by |
|---|---|---|
| Mob effect | `creativeengineering:magic_resistance` | startup `magic_resistance.js` |
| Potions (8) | `creativeengineering:{,long_,strong_}magic_resistance`, `{,long_,strong_}long_reach`, `{,long_}phantom_bane` | `potion/CEPotions.java` (`CreativeBrewing.NS`) |
| Potions (30) | `kubejs:{,long_,strong_}<absorption, blindness, darkness, glowing, health_boost, hunger, levitation, mining_fatigue, nausea, resistance, saturation, wither>` | startup `potions.js` (`event.create(name)`, KubeJS default namespace) |
| Items (3) | `creativeengineering:potion_bucket`, `splash_potion_bucket`, `lingering_potion_bucket` | startup `potion_buckets.js` |
| JEI and data ids | categories `potion_pages`, `potion_buckets`, `tipped_arrows`, `automated_potion_mixing`, `lingerification`; `data/creativeengineering/recipes/lingerification_list.json` | `compat/jei/*`, resources |
| Lang keys | `creativeengineering.jei.*` (4), `tooltip.creativeengineering.intoxicating_drink`, `effect.creativeengineering.*` (compound parts, magic resistance and its JEED text) | CB `en_us.json`, the pack lang overlay (`tools/gen_effect_descriptions.py`) |
| Assets in the pack folder, not in the jar | `kubejs/assets/creativeengineering/textures/mob_effect/magic_resistance.png`, 6 bucket textures, 3 bucket models | (the KubeJS resource pack) |

References outside CB to these ids (as of 2026-11-04): `config/` 3 files, Common (`StringEntry.java`, `neoCraftHelper.js`),
Equipment (`CEQConfig.java`, `kubejs/data/creativeequipment/kubejs/equipment/end_gear.js`), `startup_scripts/_lib/effect_dict.js`.

## 2. M1: dicts and helpers into Creative Common (C)

### 2.1 What moves where

| From (pack folder) | To (Common's source tree) | Loads as |
|---|---|---|
| `kubejs/startup_scripts/_lib/*.js` (all 12) | `development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/` | Common's startup pack (first pack) |
| `kubejs/startup_scripts/func/trade_helper.js`, `jei_order.js` | `.../kubejs_startup/creativecommon/func/` (beside NCH) | same |
| `kubejs/server_scripts/_lib/removed_item_tags.js`, `java_guard.js`, `export_io.js` | `kubejs/data/creativecommon/kubejs/_lib/` (copied into the jar by the build, as CB and CD do) | Common's server pack |
| helper half of `kubejs/client_scripts/tooltips.js` (the `global.tooltips` API) | `kubejs/assets/creativecommon/kubejs/_lib/tooltips.js` | Common's client pack |
| the pack's own tooltip rows (rest of `tooltips.js`) | stay in `kubejs/client_scripts/` | pack folder |
| `kubejs/client_scripts/jei_order.js` (hands `global.jeiOrder` to Common's JEI sorter; moves with `jei_order.js` above) | `kubejs/assets/creativecommon/kubejs/_lib/jei_order.js` | Common's client pack |
| `kubejs/client_scripts/fluid_jei.js` (puts every registered fluid in JEI's fluid list; CD and CB fluids need it outside the pack) | `kubejs/assets/creativecommon/kubejs/_lib/fluid_jei.js` | Common's client pack |

Stay in the pack folder (pack content, not helpers): `namechanges.js`, `name_fixes.js`, `jeichanges.js`,
`plantable_tooltip.js`. Nothing else in `startup_scripts/` moves (`items/`, `changes/`, `jei/`, `todo/` stay pack content; they load after
Common's pack and keep reading the dicts as now).

### 2.2 Steps

1. **Baseline.** Before moving, record per-dict key counts and a hash of the resolved values (`global.item`, `food`,
   `fluid`, `blocks`, `effect`, `tool`, `misc`, `removedItems`, `itemMerges`, `trades` table sizes) with the headless Rhino
   harness (`tools/food_dict_test/RhinoRun.java`, memory `kubejs-headless-rhino-harness`) or from `logs/kubejs/startup.log`.
   Save to `feature_writeup/testing/dict_baseline_2026-11-xx.json`.
2. **Order inside Common's pack.** Files sort by `// priority:`. Keep each dict's current header; give the helpers
   (`neoCraftHelper.js`, `tag_helper.js`, `compost.js`) and the dicts headers so the existing cross-reads hold: dicts that
   read other dicts at top level (`food_dict` reads `item`, `removed_*` and `item_merges` read `item`) load after `item_dict`.
   Check every top-level read with a grep of `global\.` outside function bodies.
3. **Top-level lookups.** `item_dict.js`, `fluid_dict.js`, `block_dict.js`, `item_merges.js`, `trade_helper.js` call Java or
   KubeJS at load (`Java.loadClass`, `Item.of`, registry reads). Each such call that names a mod must survive that mod's
   absence (M2 rule G4). `effect_dict.js` names `vinery:climbing_effect`; find the reader behind "the pack does not start
   without Vinery" (creative_brewing.md, testing rule) and make it skip when Vinery is absent.
4. **`git mv`** the files (history kept), delete nothing else. The server and client trees go into Common's jar through a
   copy block in `development/creativeCommon/build.gradle` like CB's (`from(rootProject.file("kubejs/${tree}"))`), with the
   same "no `.js` found" failure.
5. **Repoint tooling.** `tools/pack_paths.py` (`ITEM_DICT` and siblings) first, then every tool that hard-codes the old
   paths: `food_families.py`, `food_dict.py`, `food_catalog.py`, `gen_stack_sizes.py`, `kitchen_dicts.py`, `gen_can_types.py`,
   `shield_check.py`, `gen_soup_soft_fluids.py`, `test_gen_members.py`, `loot_tuning_sim.js`, `blockrunner_tag_sim.js`,
   `cd_food_test/FfhRun.java`, `food_dict_test/RhinoRun.java`, `tools/generators.json` outputs. Make every one go through
   `pack_paths.py` while there. `tools/build_deploy.py`: the moved files leave the `kubejs/` payload (they ship in Common's
   jar) and join `IN_TREE_EXTRA_SRC` for Common; check the deploy sync deletes them on the server copy.
6. **Docs and skills.** CLAUDE.md layout line and "Build and verify" (a dict edit now needs a C rebuild and restart),
   `.claude/skills/add-mod` and any skill naming `startup_scripts/_lib`, memory `item-dict-mechanics`.
7. **Version.** Common `1.0.0` -> `1.1.0`; CD, Compat and CB raise their `creativecommon` range to `[1.1.0,)`.
8. **Check.** Rebuild C, rerun the baseline harness against the jar's copies: every count and hash equal. Boot (M7).

Cost the owner should know: a dict edit (most `/cegen` generator output) no longer loads from the pack folder; it needs a
Common rebuild, install and restart. Startup scripts needed a restart already, so the added cost is the rebuild (D1,
accepted: no dev-only override).

### 2.3 Helpers held in other Creative jars (D2: as many in Common as feasible)

Rule: a helper moves to Common when it is an API (not a jar's own rows) and either a second jar reads it or it has no
tie to its jar's domain. It stays when only its own jar uses it, or it is a framework of that jar's feature (routing in
memory `creative-delight-routing`). Census of every `global.<name> =` in the jar trees, 2026-11-04:

| Global | Defined in | Read by | Verdict |
|---|---|---|---|
| `global.CWG.lootRules` queue + `global.lootTuning` engine (`loot/05_tuning.js`) | Worldgen | Worldgen, CD (4 files, soft: `if (global.CWG)`) | **Move** to Common as `global.lootRules` / `global.lootTuning`; `CWG.lootRules` stays as an alias. CD's loot rules then work without Worldgen. Worldgen's own rules (`07_rules.js`) stay. First check the engine reads nothing Worldgen-only. |
| `global.has` | (new, M2 G3) | all | Common |
| `global.seasoning` | CD | CD; Common's NCH (soft: `global.seasoning &&`) | Stay: CD's seasoning rows; NCH already degrades. |
| `global.foodValueCopies` | CD | CD, CB | Stay: CB hard-depends on CD. |
| `global.toolEffects` (and the armor-effect framework) | CB | CB (Equipment only names it in a comment) | Stay: gear effects are CB's feature. |
| `global.tuneTools`, `global.tuneArmor` | Equipment | Equipment (the pack only mentions them in a comment) | Stay. |
| `global.potionArea`, `foodEffectSwaps` (CB); `sampleSyringe`, `fastToasts`, `delightfulFoodValues` (CD); `itemPhysics` (Compat); `LOOT` (Worldgen); devtools texture dumps; `CMG`, `CEQ`, `CWG` objects | own jar | own jar only | Stay. |

Recheck this table when a jar starts reading another jar's global: that is the trigger to move it.

## 3. M2: guards on scripts that need items from other mods (B, D, P, C)

### 3.1 Rules

- **G1, one mod per file:** a file whose rows all touch one optional mod lives in that mod's folder and starts with
  `// requires: <modid>` (honoured by the jar loader and by KubeJS). This is the existing CD/CB rule, now enforced.
- **G2, mixed files:** a file in a pack-native folder (`creativeengineering/`, `creativecommon/`, `minecraft/`) that names
  several mods is split by mod into G1 files where the rows are independent (preferred: `potions.js` rows for Alex's Mobs
  go to `alexsmobs/potions.js`, and so on). Where a row has to stay in the shared file, it is wrapped in
  `if (global.has('<modid>'))`.
- **G3, dict keys:** a row that uses `global.item.x` / `global.fluid.x` for an item of an optional mod is guarded by that
  mod's id, found by resolving the key, not by the folder name. New Common helper (2.1, beside NCH):
  `global.has(...modsOrIds)`: true when every argument's mod is loaded (`'alexsmobs'` or `'alexsmobs:x'` or a dict value).
- **G4, top-level code:** nothing at file top level may resolve an item, fluid, block or effect of an optional mod (Rhino
  throws on a missing Java static field, memory `rhino-fieldandmethods-gotcha`; `Item.of` of a missing id gives air). Move
  such reads into the event callback or behind `global.has`.
- **G5, helpers fail soft for absent mods:** NCH and `tag_helper` skip a row whose input or output belongs to a mod that is
  **not loaded**, with one `console.info` summary per mod ("skipped 14 rows for absent alexsmobs"). A missing id in a mod
  that **is** loaded stays a warning, as now, so pack bugs still show. Extend the helpers; do not add guards in every
  caller for this case.
- **G6, Java:** mixins already gate by compartment (`CBMixinPlugin`, `GatedMixinPlugin`). Check the non-mixin classes that
  touch optional mods (`compat/jei/*` is only loaded by JEI; `trader/WinemakerTrades` needs Vinery, always present in CB)
  and guard any `Class.forName` / registry read of an optional mod's ids.
- **G7, required vs optional for CB:** required (no guard): Vinery, Brewin', Foundation, Farmer's Delight, KubeJS, the four
  Creative jars. Everything else is optional.

### 3.2 Work list

| Jar | Work |
|---|---|
| CB | split or wrap the 22 pack-native files of 1.3 (the table is the starting list); resolve every `global.item` / `global.fluid` key in all 32 files; `potion_area.js`, `potion_buckets.js`, `magic_resistance.js` need no guard (own ids) |
| CD | 4 files in mod folders without a matching header; the 59 pack-native/vanilla-folder files checked for foreign ids (G2, G3); 320 `global.item` uses resolved |
| Compat | 11 files in mod folders without a matching header; the 6 others checked |
| Common | the moved dicts and helpers (G4), `StringEntry.java` and NCH's references |

### 3.3 Checker (so it stays guarded)

`tools/guard_check.py` (new, listed in `generators.json` as a check, not a generator): reads `kubejs/exported/` dumps for
the id-to-mod map, loads each jar tree, resolves dict keys with the Rhino harness, and reports every row that names an
optional mod's id without a G1/G2/G3 guard. Run it after every script edit in these jars; zero findings is part of M7.
It also flags a `// requires:` naming a mod id that no jar provides (the M8 fold-in leaves two such headers, 3b step 4).

## 3b. M8: Cooking for Blockheads and Incubation folded into Creative Delight, optional (D)

Today `blockheadsAdditions` (mod id `blockheads_additions`, 22 classes, racks, corner storage, oven, fridge, counter
toppers) and `incubationAdditions` (`incubation_additions`, 14 classes, emu, duck and goose nests, egg crates, golden goose
egg) are nested member mods of CD with mandatory deps on `cookingforblockheads` + `balm` and on `incubation`. A nested
mod missing a mandatory dep stops the game at the mod-loading screen. After M8 both are compartments of CD's own jar: the
two mod ids are gone, CD loads without CfB, Balm or Incubation, and with them everything works as now.

1. **Code.** `git mv` member Java into CD: `compat/cookingforblockheads/` (BA* classes, `kitchen/`, `rack/`, `client/`)
   and `compat/incubation/` (IA* classes, `goldenegg/`, `crate/`, `nest/`, `client/`), packages renamed. `BAConfig` and
   `IAConfig` become sections of `CDConfig`; `MemberConfigMove` (already in CD) carries the values over from
   `config/blockheads_additions-common.toml` and `incubation_additions-common.toml`, then the old files and their
   `configs_edited/` copies go. The two `[members]` switches are replaced by `[compartments] cookingforblockheads` and
   `incubation` (default true), so either half can still be turned off.
2. **Load guards (Java).**
   - One entry point per compartment: `CreativeDelight` calls `CfbCompartment.init(modBus)` only when
     `ModList.isLoaded("cookingforblockheads") && isLoaded("balm")`, and `IncubationCompartment.init(modBus)` only when
     `isLoaded("incubation")`; emu parts also need `alexsmobs`, duck and goose parts `untitledduckmod` (as now). Only those
     classes and what they call may name the optional mod's types, so the JVM never loads a CfB or Incubation class when
     the mod is absent: no such types in CD's fields, method signatures or lambdas outside the compartment, and no
     `@Mod.EventBusSubscriber` on compartment classes (Forge's annotation scan loads them); register their listeners from
     `init`.
   - The DeferredRegisters (`cookingforblockheads:` racks, the nest blocks) are created and registered inside `init`.
   - Mixins: both member mixin configs merge into `creativedelight.mixins.json` under `mixin/cookingforblockheads/`,
     `mixin/incubation/`, `mixin/alexsmobs/` (emu egg layer, beside CD's existing AM mixins), `mixin/untitledduckmod/`;
     `CuttingBoardRendererOffsetMixin` goes to `mixin/farmersdelight/` and `CoasterRendererOffsetMixin` to
     `mixin/brewinandchewin/` (Brewin' lives in CB, so this one must be gated: CD does not require CB). `CDMixinPlugin`
     gates each package by its mod id; the two member mixin plugins are deleted.
   - CD `mods.toml`: `cookingforblockheads`, `balm`, `incubation` added with `mandatory=false`, `ordering="AFTER"`.
3. **Resources.** Data and assets that name the optional mods' blocks or items (rack recipes, loot tables, tags, the
   `cookingforblockheads` blockstates and models, the nest worldgen) move into mod-gated built-in packs
   (`packs/cookingforblockheads/`, `packs/incubation/`, registered with `BuiltInPacks` only when the mod is loaded), so a
   missing mod gives no recipe, tag or loot errors. The emu and duck packs need two mods each (`incubation` + `alexsmobs`,
   `incubation` + `untitledduckmod`): extend the pack registration to an all-of mod list if it takes one mod today. Add
   `packs` to CD's `shared_sources`. `checkNoArrResources` stays on CD's jar (nothing of CfB may ship).
4. **Scripts.** `kubejs/data/creativedelight/kubejs/incubation_additions/bird_nests.js` (`// requires:
   incubation_additions`) and CD's startup `kubejs_startup/incubation_additions/compost_values.js` name the mod id that
   disappears, so they would silently stop loading: rename both folders to `incubation/` and change the header to
   `// requires: incubation` (rows for emus or ducks also guarded by `alexsmobs` / `untitledduckmod`, G3). The
   `cookingforblockheads/` scripts already require `cookingforblockheads`. Grep `blockheads_additions` /
   `incubation_additions` as a mod id (`requires:`, `isLoaded`, `global.has`) across `kubejs/`, `development/`, `tools/`,
   `config/`: 19 files name the strings today, most as block ids (step 5).
5. **Ids (D6, decided).** Every block and item M8 folds in moves to `creativedelight:*`: the CfB racks and fruit baskets
   (`cookingforblockheads:*_spice_rack`, `_tool_rack`, `_fruit_basket`, per wood) and Incubation Additions' nests, egg
   crates and eggs (`incubation_additions:*`). Old -> new go into a CD swap list (`data/creativedelight/swaps/`,
   `replaces`; `SwapRemapper` handles all Forge registries, so placed blocks, block entities' items and held items carry
   over). Consequences to handle: block models, blockstates, loot tables, recipes and lang keys move to the
   `creativedelight` namespace; the racks' blockstates stop living in the `cookingforblockheads` asset namespace (so the
   ARR namespace is no longer touched at all); `tools/gen_cfb_racks.py`, `item_dict.js`/`block_dict.js` entries, the
   `merges_cookingforblockheads.json` swap, `config/` and the 19 files of step 4 are repointed; CfB tags that list racks
   (`cookingforblockheads:*`) keep working because tags name the new ids. The racks still extend CfB's block classes, so
   they register only when CfB is loaded (step 2). Without CfB the ids do not exist and an old save's racks are dropped by
   the swap, as for any absent mod.
6. **Build.** `:blockheadsAdditions` and `:incubationAdditions` leave `bundle_members`, `settings.gradle` and the member
   map; their `compileOnly` libs (CfB, Balm, Farmer's Delight, Incubation, Blueprint, Alex's Mobs, Citadel, Untitled Duck
   Mod, GeckoLib) join CD's `build.gradle`; the member folders' `jar_archive/` goes to `development/creativeDelight/jar_archive/`,
   then the member folders are removed. Incubation Additions' `LICENSE.txt` is the same GPLv3 file CD has. CD minor
   version up. `mod_sides.md`: the two member rows point at CD; CfB, Balm and Incubation rows lose "Required by:" for CD.
7. **Docs.** kitchen.md 9, the incubation and nest writeups (`mobs/nests` rows), creative_delight.md 34 (members list),
   remainder_buffer.md (oven half), skill `add-mod` if it names the members.

## 4. M3: CB's pack-folder assets into the CB jar (B)

Today the bucket textures and models and the Magic Resistance icon live in the pack's KubeJS resource pack, so a CB jar
installed outside the pack has untextured buckets and no effect icon. Move them into
`development/creativeBrewing/src/main/resources/assets/creativebrewing/` under their new namespace (M4). Also check the
"effect rename lang overlay" and the coaster models (creative_brewing.md 4.6): every lang key or model a CB feature needs
moves into the CB jar; keys of other mods' effects stay in the overlay. `tools/potion_bucket_texture.py`,
`gen_effect_descriptions.py` and `gen_coaster_models.py` get their output paths changed.

## 5. M4: `creativebrewing:` for everything CB registers (B, C, Q)

1. `CreativeBrewing.NS` = `"creativebrewing"`. The 30 KubeJS potions are created as `creativebrewing:<name>`; the effect and
   the three buckets as `creativebrewing:<id>`. JEI category ids, `lingerification_list.json` (to `data/creativebrewing/`)
   and every lang key (`creativebrewing.jei.*`, `tooltip.creativebrewing.*`, `effect.creativebrewing.*`, item names) follow.
2. **Saves.** Add every old -> new id to `data/creativebrewing/swaps/creative_split.json` `replaces` (1 effect, 38 potions,
   3 items). `SwapRemapper` remaps every Forge registry through `MissingMappingsEvent`, so blocks of world registry ids,
   active effects (the `forge:id` string falls back to the remapped int id) and placed or held buckets carry over.
3. **Potion ids in NBT are strings** (`Potion` on potions, tipped arrows, buckets; Create and IE potion fluids), which
   `MissingMappingsEvent` never sees. **Decided (D5): no read-time alias.** Existing bottled potions, tipped arrows,
   potion buckets' contents and potion fluids made before the rename read as unknown potions (Uncraftable Potion, no
   effect). The old -> new entries in step 2 stay anyway (they fix the registry table); nothing else is added. The
   changelog says so (section 12).
4. **References elsewhere** (1.4 last paragraph): Equipment's `CEQConfig` defaults and `end_gear.js`, Common's
   `StringEntry.java` and `neoCraftHelper.js`, `effect_dict.js` (now in Common), the three `config/*.toml` files and their
   `configs_edited/` copies, `config/jei/` files keyed by id. Grep `creativeengineering:(magic_resistance|...)` and
   `kubejs:(long_|strong_)?(absorption|...)` across `development/`, `kubejs/`, `config/`, `configs_edited/` and `tools/`
   until nothing is left outside the swap list.
5. Refresh `/dumpall` afterwards; generators that read `potions.json` / `mob_effects.json` pick up the new ids.
6. JEI now shows the potions, buckets and the effect under "Creative Brewing". Player-visible: changelog bullet (11).

## 6. M5: display names (B)

| Mod id | Today | New |
|---|---|---|
| `vinery` | Let's Do Vinery (Creative Brewing) | **Vinery (Creative Brewing)** |
| `brewinandchewin` | Brewin' & Chewin' (Creative Brewing) | unchanged |
| `foundation` | Creative Brewing | **Foundation (Creative Brewing)** (the mod list stops showing "Creative Brewing" twice) |

Edit `members/vinery/gradle.properties` `mod_name` and the `foundation` `displayName` in the Vinery member's `mods.toml`;
`config/jei/ingredient-list-mod-sort-order.ini` (keyed by display name) and its `configs_edited/` copy; creative_brewing.md
0 and CBR-01, alcohol.md header, names.md row, scratch row CQ-PRI-03. Released changelog files stay as they are.

## 7. M6: GPLv3 LICENSE in every Creative jar (all)

`ce.forge-mod.gradle` already copies `LICENSE`, `LICENSE.txt` and `NOTICE*` from a mod's folder into its jar. Missing today:

| Where | Missing |
|---|---|
| Creative jars | `creativeCalendar`, `creativeCompat`, `creativeEngineering`, `creativeEquipment`, `creativeMagic`, `creativeWorldgen` |
| CD members, own code | `blockheads`, `largemeals`, `oceanic`, `roll`, `rustic` (Oceanic and Rustic: check the upstream licence in Step 2 before adding) |
| CD repack members (upstream licence + GPLv3 overlay, NOTICE only) | `barbeques`, `cannedgoods`, `createsalt`, `delightful`, `ends`, `farmers`, `hearth`, `nethers`, `oceans`, `salt` |

Have (all the same file): `creativeBrewing`, `creativeCommon`, `creativeDelight`, `creativeDevtools`, `creativeLabels`; CB's two
members and the Worldgen members carry their own.

1. Copy `development/creativeCommon/LICENSE.txt` to each folder above (repack members: the upstream NOTICE stays beside it).
2. **Build check:** in `ce.forge-mod.gradle`, fail the jar task when `mod_license` mentions GPL and the folder has no
   `LICENSE*`, so a new jar cannot ship without it.
3. **Licence text (D4):** set `mod_license` to `GPL-3.0-only` in every own-code `gradle.properties` (32 jars); repacks read
   `<upstream> (upstream); overlay GPL-3.0-only`. No "or later" notice anywhere.
4. Rebuild and install each jar (a LICENSE file alone changes nothing in game; ride along with the next build of each jar
   where possible, but CB's chain (C, P, L, D, B) must have it before Step 3).

## 8. M7: verification

Offline: dict baseline equal (2.2 step 8); `tools/guard_check.py` zero findings; every jar's build includes `LICENSE*`
(`unzip -l`).

Scratch queue rows to add when the work is built (skill `scratch-test`; the owner starts the run):

| Row | Check |
|---|---|
| SQ-DCM-01 | Full pack boots after M1: `logs/kubejs/startup.log` lists `creativecommon` first and no `ReferenceError` / `is not defined`; dict counts logged equal the baseline |
| SQ-DCM-02 | `/reload` clean in `logs/kubejs/server.log`; recipes count equals the pre-migration dump |
| SQ-DCM-03 | Old world: no missing-registry warnings for moved ids; a held `creativeengineering:potion_bucket` and an active Magic Resistance effect read as the `creativebrewing:` ids (old bottled potions and potion fluids are expected to read as unknown, D5) |
| SQ-CBS-01 | **Minimal chain**: fresh server folder with Forge, KubeJS (with Rhino, Architectury), Common, Compat, Calendar, CD, CB, the Vinery donor jars, and no CfB, Balm or Incubation (M8); an empty `kubejs/` folder; boots with no script error; `/effect give @s creativebrewing:magic_resistance` works; a potion bucket fills from a cauldron; one keg brew and one wine ferment |
| SQ-CBS-02 | Minimal chain plus Alex's Mobs only: AM's potion rows appear, no warnings for the other optional mods |
| SQ-CDFO-01 | Full pack after M8, old world: placed racks, fridge, corner and oven keep contents; oven fuel and smoking work; emu, duck and goose nests and egg crates work; no missing-registry warnings (with D6 rename: old nests and crates read as the new ids); `config/creativedelight-common.toml` holds the moved values |
| SQ-CDFO-02 | CD chain without CfB, Balm, Incubation: boots; no CfB/Incubation class load errors, no recipe, tag or loot errors for `cookingforblockheads` / nests in `latest.log` |
| SQ-CDFO-03 | Pack with CfB but not Incubation, and Incubation without Alex's Mobs and Untitled Duck Mod: each boots; only the present half's content loads |
| CQ-CDFO-01 | client: rack textures, raised oven and fridge-top fill render, duck egg models; mod list no longer shows the two members |
| CQ-CBS-01 | client: mod list shows "Vinery (Creative Brewing)", "Brewin' & Chewin' (Creative Brewing)", "Foundation (Creative Brewing)"; buckets and the effect icon textured on the minimal-chain client |

## 9. Order and installs

1. M6 LICENSE files (no code; can go first, rides along with every later build).
2. M1 (C), then M2 for Common's own files; build C `1.1.0`.
3. M2 for CD, Compat, CB; M8 in CD (with the 2.3 loot-engine move, which touches C, D and W); M3, M4, M5 in CB;
   Equipment's id references (Q).
4. Build and install in order C, P, L, W, D, B, Q (game closed, `./gradlew :<mod>:build --offline` then `installToMods`).
5. Queue M7 rows; the owner runs the scratch pass.

## 10. Step 2: licence audit (separate from the migration)

Read-only; runs on the jars built in Step 1 (they are what ships). Same method and table shape as creative_brewing.md 9.
Output: a "Licence audit" section in each jar's writeup and one summary row per jar in creative_brewing.md 9. Fixes found
become their own TODO rows, not part of this step.

| Jar | Scope | Known risks to settle |
|---|---|---|
| Creative Brewing | **re-audit of the own jar** (2026-11-01 audited a 28 KB jar; it is now about 195 KB after phases 2 and 3) and the M3 assets now inside it; members re-confirmed (no resource change since `-08`, checked 2026-11-04) | potion bucket textures (`potion_bucket_texture.py` output: derived from vanilla bucket art? Mojang textures cannot be redistributed), the Magic Resistance icon's origin, mixin classes that copy upstream method bodies |
| Creative Common | all Java, the dicts and helpers after M1, every resource file | fixes and perf mixins that copy upstream code; the dicts are id lists (fine); any donor files |
| Creative Compat | everything | the 950 painting files (creative_split Q7): authors and licences of each source |
| Creative Calendar | confirm creative_calendar.md Q3 ("all own code") against the built jar | none known |
| Creative Delight | own jar and all 22 members | ARR overrides (CfB, Ender's: memory `arr-mod-overrides-route`), Incubation-derived nest textures (`mobs/nests #2`, repaint open), Oceanic licence conflict (creative_delight.md 28), Rustic and Oceanic upstream licences (7), every repack's NOTICE matching its upstream licence |
| All five | `mods.toml` `license=` text, the GPLv3 file present (M6), NOTICE files for every MIT / LGPL / AFL upstream | `mod_license` reads `GPL-3.0-only` (D4); project pages pick GPL-3.0-only |

Per jar: list every file with an origin (own, upstream with licence, generated from what); compare against the upstream
jar or repo where a member is a port; note anything ARR, unknown or Mojang-derived. Verdict per row: OK, fix, or remove.

## 11. Decisions (owner)

| # | Question | Answer / recommendation |
|---|---|---|
| D1 | Dict edits after M1 need a Common rebuild and restart. Accept, or add a dev-only override? | **Decided 2026-11-04: accept**, no override. |
| D2 | Server and client helpers into Common too? | **Decided: yes, as many helpers as feasible** (2.1 rows, 2.3 census). |
| D3 | CD's `blockheads` and `incubation` members hard-require CfB + Balm and Incubation. | **Decided: make both optional with load guards and fold them into CD's own jar** (M8). |
| D4 | `mod_license` text (below). | **Decided: `GPL-3.0-only`.** |
| D5 | Keep a read-time potion alias? (below) | **Decided: no.** Old potions in existing saves may break. |
| D6 | Ids after M8. | **Decided: all blocks (and their items) move to `creativedelight:*`**, racks and baskets included (3b step 5). |

**D4 in detail.** `mod_license` is the free-text `license=` line in each jar's `mods.toml`; Forge shows it in the mod
list and nothing checks it. 32 of our jars say "GNU GPL3"; the repacks say "MIT (upstream); overlay GNU GPL3" and
similar (10 jars). The LICENSE file is the same GPLv3 text either way; what differs is the grant:
- `GPL-3.0-only`: users get GPLv3 and nothing else. If the FSF ever publishes a GPLv4, our code stays v3 unless the owner
  relicenses it.
- `GPL-3.0-or-later`: users may also take it under any later GPL. Others can then fold our code into a future GPLv4
  project; the owner gives up control over which future terms apply.
Nothing in the repo picks one today: no file of ours carries the "either version 3 ... or any later version" notice (the
phrase appears only in the GPL text's own appendix and in an LGPL credit). Upstream licences we combine with (MIT, AFL
3.0, LGPL-2.1) allow either. CurseForge and Modrinth pick the licence from their own list on the project page (both list
"GPL-3.0-only" and "GPL-3.0-or-later" separately), so the choice also decides that dropdown. If chosen: set
`mod_license` to the SPDX id (`GPL-3.0-only` or `GPL-3.0-or-later`; repacks keep their upstream part, e.g. "MIT
(upstream); overlay GPL-3.0-only"), and, for "or later", add the standard two-line notice to each jar's README or
NOTICE. Pure text change, rides along with M6. Recommendation: `GPL-3.0-only` unless the owner wants others to be able
to move the code to a later GPL; it is the narrower grant and can be widened later, not narrowed.

**D5 in detail.** M4 renames 38 potions. Forge's remap (`MissingMappingsEvent`, `SwapRemapper`) only fixes ids saved as
registry numbers in the world's registry table: blocks, items, the effect. A potion in a bottle, tipped arrow or potion
bucket is stored as a string in the item's NBT (`Potion: "kubejs:wither"`), and Create and IE potion fluids do the same.
Forge never sees those strings. Without help, every old potion item and potion fluid reads as an unknown name and shows
as "Uncraftable Potion" with no effect, in chests, tanks, pipes and inventories. The alias (5.3) is a mixin on
`Potion.byName`: when the name is not registered it looks it up in the swap list's `replaces` and returns the new
potion. It costs one map lookup, only on a miss, and does nothing for anyone without old ids (a fresh install outside the
pack never has them). The NBT itself keeps the old string until the item is rewritten, which is why it has to stay:
- **Keep forever (recommended):** old items in chunks nobody has loaded for months still read right; no cost.
- **Drop after a release or two:** saves a small mixin, but every old potion still sitting in an unvisited chest, tank or
  shulker box turns uncraftable for good.
- **One-time NBT rewrite instead** (walk every chunk, container and fluid tank): heavy, misses modded storage it does not
  know, and still needs the alias for items carried in from elsewhere. Not worth it.

## 12. Docs and changelog when built

- Docs: creative_brewing.md (0, 2.4, 2.5, 4.6, 6, 9), creative_split.md 7.4 (dicts moved), CLAUDE.md layout and build notes,
  README index row, TODO `creativebrewing/publish #3`, alcohol.md header, names.md, scratch_queue.md (8); M8: kitchen.md,
  the nest writeups, creative_delight.md, remainder_buffer.md, mod_sides.md (3b step 7).
- Changelog (`unreleased.md`): add a bullet that potions, tipped arrows and potion fluids made before this update no longer
  work (D5). One bullet under a Creative Brewing family for the new names, one for the potions, buckets
  and Magic Resistance now listed under Creative Brewing (old ones carry over). M8: one bullet under a Creative Delight
  family (the Cooking for Blockheads and Incubation additions are part of Creative Delight and listed under it; with D6
  rename, old nests and crates carry over). M1, M2, M6 are dev-only (no bullet) unless
  a guard changes what a player sees in the pack.
- No bullet for this plan itself (nothing built).

## 13. Notes for the next plan (owner 2026-11-05): chain licence fixes before publishing

Input to the plan that follows Step 2 (Compat and CD rows gate the publish order in creative_brewing.md 9.5).

| # | Item | Row | What |
|---|---|---|---|
| N1 | Install the painting fix | `licence/compat #1` | Install the Compat jar (paintings built at load from the installed jars, 950 files out of the jar), then run SQ-PA-01 to -04, CQ-PA-01, -02. The 30 vanilla paintings stay a pack-folder datapack, never in a jar |
| N2 | Banners | `licence/compat #2` | Delete the 25 MineColonies banner textures. Build the elytra banner and bedspread patterns at runtime from the installed jars (as the paintings), so no derived PNG ships; the 100 shipped files go. Credit the source mods in `NOTICE-thirdparty.txt` |
| N3 | TrashSlot compliance | `licence/compat #3` | Replace `TrashSlotGuiHandlerMixin`'s `@Overwrite onScreenInit` (follows TrashSlot's all-rights-reserved method) with targeted injects that do not copy its body. Needs a plan of its own: read what the overwrite changes first |
| N4 | Rustic crate placeholders | `licence/cd #2` | Closed: Rustic Delight is MIT (CurseForge page, owner). Add the MIT notice for the 2 crate PNGs to the Rustic member's NOTICE; no repaint |
