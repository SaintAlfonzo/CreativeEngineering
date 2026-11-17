# Creative Engineering and Creative Common

Written 2026-11-04 (Creative Split wave 8, [creative_split.md](creative_split.md) 6 Q1, 7.6 and 7.13; TODO
`creativesplit/engineering #10`). Status: **carved and swapped 2026-11-04, every Creative jar built; NOT installed,
nothing booted; scratch SQ-CEN queued.**

Written for: the owner and agents working on the split. Two jars come out of this wave:

- **Creative Engineering** (`creativeengineering`, development/creativeEngineering, `creativeengineering-2.0.0.jar`, side
  both): the pack's machines and engineering content. It took over the mod id and `config/creativeengineering-common.toml`
  from patcherMod, so its version starts at 2.0.0 (1.0.0 to 1.1.09 of that id were patcherMod). The former Creative
  Computers rows are here for good ([creative_computers.md](creative_computers.md)).
- **Creative Common** (`creativecommon`, development/creativeCommon, Gradle project `:creativeCommon`,
  `creativecommon-1.0.0.jar`): what was left of patcherMod after waves 1 to 8, as section 2.4 of the split plan lists it.
  Package `com.SaintAlfonzo.creativecommon`, config `creativecommon-common.toml`. Every Creative jar requires it.

Same behavior as before the carve. Engineering's registry ids, tags, lang keys and channels keep the
`creativeengineering` namespace. Common's own data moved to `creativecommon` the same day (owner, 2026-11-04,
[creative_split.md](creative_split.md) 7.14): `CreativeCommon.NS` is now `creativecommon`, which covers the damage tags,
WTHIT plugin and line ids, JEI uids, the `jei/` and `wthit/` tags, the lang keys, the `kubejs/assets/creativecommon/`
changelog and JEI world trees, the JFR events and the mixin member prefix.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=1813904de4e9 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Testing 3), 3 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `creativecommon/publish #1` | Testing | S+ C | Creative Common publish readiness: mixin version gates, `mods.toml`, dependency list |
| `creativecommon/publish #2` | Testing | S C | Neutral defaults for wide distribution |
| `creativesplit/engineering #10` | Testing | none | Carve Creative Engineering with the Computers rows, then the id swap |
<!-- generated:status_index end open_work -->

## 1. What Engineering holds

- **Java** (`com.SaintAlfonzo.creativeengineering`, the same package and subpackages as in patcherMod, so the moved
  classes are unchanged; 67 classes plus 3 new, about 4300 lines):
  - `fan/` (Create's dragon breathing fan processing type and recipe, exclusive results, the catalyst helper) and
    `compat/DragonBreathingJEI`; `sawmill/` (IE sawmill blade routing); `item/` (package `items`: precision sawblade,
    creosote-soaked sawdust); `bucket/` (wooden bucket); `shrapnel/` (TNT shrapnel entity, spawner, detonation
    listener); `excavation/` (Ore Excavation undo rules); `surface/` (BlockRunner tier speeds, sticky facing,
    camouflage, visual sinking, the WTHIT Run Speed line, `LeafPileSlow`); `fluid/FluidBridge` (the liquid XP bridge,
    D3 settled here by the owner, 2026-11-04); `compat/IECompat`, `compat/ImmersivePostsMaterials`.
  - New: `CreativeEngineering` (main class: every registration that left patcherMod's main class, same order),
    `CEConfig` (this jar's three tables), `EngineeringMixinPlugin` (a `GatedMixinPlugin`).
- **Mixins:** `creativeengineering.mixins.json`, 26 common and 3 client, SRG with `remap = false`, member prefixes
  `creativeengineering$` unchanged. Each one is gated on its mod (IE, Immersive Posts, PneumaticCraft, Sophisticated
  Core, Create, Dreams and Desires, Ore Excavation, BlockRunner, Framed Blocks, Copycats+); patcherMod applied them
  all unconditionally. The three client ones (dragon head catalyst, sinking render and camera) target vanilla.
- **Config:** `config/creativeengineering-common.toml` keeps `[shrapnel]`, `[surface]` (with every subtable) and
  `[vein_undo]`, keys and comments unchanged. Forge drops the other tables from the file on the first boot after Common
  has copied them out (2.1). `global.NCH.registerConfig('creativeengineering', ...CEConfig)` in the startup core.
- **Resources in the jar:** the 211 generated sawmill recipes (`data/creativeengineering/recipes/sawmill/`; no
  generator exists for them, they are hand-maintained data), the dragon breathing catalyst tag, `forge:sawblades`,
  the four item models and textures (sawblade, sawdust, wooden buckets), `waila_plugins.json` (`run_speed`), lang
  `assets/creativeengineering/lang/en_us.json` (41 keys: fan type, walk speed and Run Speed lines, the 26 surface tiers,
  vein undo messages, wooden buckets).
- **Scripts:**
  - Startup `kubejs_startup/creativeengineering/`: `ce_core.js` (`global.CEN`: `modId`, `on()`, `recipes()`),
    `items/` (components, coil blocks, plate blocks, dust piles, misc items, wood pellets, amethyst dust and lens),
    `changes/transition_items.js` (every sequenced assembly transition item).
  - Server `kubejs/data/creativeengineering/kubejs/`: `engineering/` (22 files, `computing.js` and `redstone.js`
    included), `metal/` (3), `wood/` (6), `tags/` (engineering, fluid, metal, storage block and wood tags); opened by
    `_engineering_core.js`. Recipe ids do not change: NCH's default namespace is `creativeengineering`, which is now this
    jar's own, so the files need no `global.CEN.recipes` wrapper.
  - No client rows (sweep F found none).

## 2. The swap: patcherMod becomes Creative Common

| What | Before | After |
|---|---|---|
| Folder, Gradle project | `development/patcherMod`, `:patcherMod` | `development/creativeCommon`, `:creativeCommon` |
| Mod id, jar | `creativeengineering`, `creativeengineering-1.1.09.jar` | `creativecommon`, `creativecommon-1.0.0.jar` (`replaces_jars=creativeengineering-1.*.jar`) |
| Package | `com.SaintAlfonzo.creativeengineering` | `com.SaintAlfonzo.creativecommon` (shared sources and the shaded commonmark follow) |
| Main class, config class | `CreativeEngineering`, `CEConfig` | `CreativeCommon` (`MODID = "creativecommon"`, `NS = "creativecommon"` since 7.14), `CommonConfig` |
| Config files | `creativeengineering-common.toml`, `creativeengineering-swaps.toml` | `creativecommon-common.toml`, `creativecommon-swaps.toml` |
| Mixin config | `creativeengineering.mixins.json` | `creativecommon.mixins.json` (74 mixins) |
| Startup pack | `kubejs_startup/creativeengineering/` | `kubejs_startup/creativecommon/` (the loader keys on the jar's mod id: `JarStartupScripts.HELPERS_MOD_ID`) |
| Lang | `assets/creativeengineering/lang/` | `assets/creativecommon/lang/` (rule 11: Engineering ships the old path) |

### 2.1 Config carry-over

`CreativeCommon`'s constructor calls `ConfigRename.carrySections("creativeengineering-common.toml",
"creativecommon-common.toml", CommonConfig.SECTIONS)` (the 13 tables: armor_overflow, jei, gui_title, toasts, tooltips,
changelog, damage_tags, coaster, wthit, perf, wandering_trader, advancements, mod_names), only when the new file does
not exist yet, and `ConfigRename.carryOver` renames the swaps toml. Common constructs before Engineering (Engineering
requires it, AFTER), and both run before Forge reads any COMMON config. The other carved jars still carry their tables
from `creativeengineering-common.toml` for a pack that skips releases: that file still exists, it is Engineering's.
`configs_edited/root_config/` holds both files, split the same way (backup of the pre-split file in the session
scratchpad only). The live `config/` file is left for the first boot to split (SQ-CEN-02).

### 2.2 What every other jar changed

- `mods.toml`: the mandatory `creativeengineering [1.1.09,)` dependency is `creativecommon [1.0.0,)` (Compat, Calendar,
  Devtools, Delight and its Barbeque's member, Brewing, Worldgen, Magic, Equipment). Nothing but Engineering requires
  `creativeengineering`.
- Java imports of Common classes moved to `com.SaintAlfonzo.creativecommon` (50 files); `CEConfig.JEI` reads in CB and
  CD are `CommonConfig.JEI`. Build files: `project(':creativeCommon')` and `development/creativeCommon/libs`.
- Creative Compat no longer imports `surface.LeafPileSlow`: Engineering sets that hook from `CompatConfig.LEAF_PILES`
  (Engineering requires Compat; Compat links no content jar, 7.6).
- Scripts that load Common classes by name: `trade_helper.js`, `export_io.js`, `jei_order.js`, Devtools `dump_climate.js`,
  Worldgen `structure_overrides.js`, CB `armor_effects.js` (`CommonConfig.PERF`). NCH's default config class is
  `{ id: 'creativecommon', name: '...creativecommon.CommonConfig' }`.
- Checked offline after the build: no Java package in two of the 10 jars (Forge refuses a split package), no resource
  path in two jars, every mixin, mixin plugin and WTHIT entry class present, no class or script string naming a Common
  class under the old package, every mod id an Engineering gate names is in `mods/`.

## 3. Edges

- Engineering -> Common: `util/BurnBursts` (its shrapnel burst reaches Compat's burn-explosion rule), the startup
  loader, NCH. Engineering -> Compat: `CompatConfig.LEAF_PILES` (2.2). Nothing calls Engineering.
- Stays in Common against the sweep tables: `compat/WthitSheetmetalTank` (third-party IE data, 7.6 WTHIT rule),
  `JetpackItemTooltipMixin` (tooltip fix), `CreateGravityDimensionMixin` (perf), `shield/EffectTickContext` and
  `damage/` (CB's APIs), `campfire/` accessors.
- Stays in the pack folder: `server_scripts/wood/removed_woods.js` (must run before `other/item_merges.js`, priority
  -100, and script packs of different jars load in no fixed order), `startup_scripts/changes/block_properties.js` (one
  D&D asphalt row mixed with the Quark monster box row, a Compat row), `server_scripts/economy/trades.js` (the
  engineer professions; the sweep left it `Engineering?` and 7.6 did not settle it).

## 4. Known effects and leftovers

- Mods screen: "Creative Common 1.0.0" is a new entry and "Creative Engineering" shows 2.0.0 with the narrower
  description. Same behavior otherwise: no changelog bullet (dev-only move, as for the earlier carves).
- Event and listener order shifts a little: Engineering's registrations run in its constructor after Common and Compat.
- Engineering's mixins are now gated on their mods; with every target mod in the pack nothing changes.
- `instance.json` (ATLauncher) still names `creativeengineering-1.1.01.jar`; update it with ATLauncher closed.
- Not updated: `tools/remainder_buffer_test/` and `tools/shield_test/` keep stub sources in the old package (both
  were already stale); prose in tool comments and in history sections of the docs still says patcherMod.
- `tools/build_deploy.py`: both jars in `IN_TREE_MODS`; the climate reader check reads Common's `WorldgenFiles`; the
  `[worldgen.debug]` release check now reads `creativeworldgen-common.toml` (it still read the CE file, stale since wave 5).

## 4.1 Carve of 2026-11-06

Non-engineering content moved to Magic, Compat, Equipment, Brewing, Delight and Worldgen; Engineering keeps machines,
metals, components, coils, wood processing, prepared flesh, hemp fabric, and now ships its own block and item assets.
No config file of its own any more (sections in `creativecompat-common.toml`). Details: [plan_cb_polish.md](plan_cb_polish.md) 9.

KubeJS-registered blocks (dust piles, plate blocks, coil blocks) get their blockstate and models from KubeJS's
generated pack, which outranks the jar: the scripts set textures with `.textureAll()` / `.texture(face, ...)` and the jar
holds only the textures. Hand-made JSONs for these ids in the jar are ignored (they left the jar 2026-11-07, TODO
`creativesplit/regression #21`). Compat's `weathered_cardboard_box` still has the problem (custom UVs lost).

## 5. Tests

Scratch SQ-CEN-01 to -08 ([scratch_queue.md](testing/scratch_queue.md)); SQ-CMG and SQ-CEQ run on the same jars.
Scratch run r1114 (2026-11-14, [scratch_run_2026-11-14.md](testing/scratch_run_2026-11-14.md) 8): SQ-CEN-02, -02b, -05s PASS (legacy config carry and merge, shrapnel kill); SQ-CMV-02, -03 PASS (Common alone, Armor Trims outside the tested range warns once).
Common with only its optional mods (no Compat, Calendar or other Creative jar): SQ-CCO-01 to -16, CQ-CCO-01 to -03 (server from `tools/scratch_query/cc_only_srv.sh`, batches `r1107cco/`); scratch r1107cco 2026-11-07: SQ-CCO-01..07, -09..16 PASS, SQ-CCO-08 PARTIAL (Create Gravity inert on the Common-only set, TODO `creativecommon/publish #3`), CQ-CCO-01..03 not run ([scratch_run_2026-11-07.md](testing/scratch_run_2026-11-07.md) 4).
In game: the 37 KubeJS blocks (dust piles, plate blocks, coil blocks) show textures; CQ-CEN-01 (WTHIT Run Speed, leaf piles, camo, sinking, JEI dragon breathing, names), pinned with the other
client rows (creative_split.md 7.8).

**Install** (game closed; all at once, the jars only work together): `./gradlew :creativeCommon:installToMods
:creativeEngineering:installToMods :creativeCompat:installToMods :creativeCalendar:installToMods
:creativeDevtools:installToMods :creativeDelight:installToMods :creativeBrewing:installToMods
:creativeWorldgen:installToMods :creativeMagic:installToMods :creativeEquipment:installToMods --offline`. Common's
install moves `creativeengineering-1.1.09.jar` to `development/creativeCommon/jar_archive/displaced/`. Revert: remove the
two new jars, put 1.1.09 back, and reinstall the other jars from their `jar_archive/displaced/` folders.

## 6. History

- 2026-11-07: dust piles, plate blocks and coil blocks rendered untextured (generated KubeJS models beat the jar's);
  fixed in the scripts, 111 dead JSONs removed. Built, not installed.

- 2026-11-06 (plan_cb_polish.md 6): `engineering/dust.js` (powdered obsidian) asks for the `creativecommon:resistance`
  potion (was `creativebrewing:`), so Engineering no longer needs Creative Brewing for it.

- 2026-11-04: carved and swapped (wave 8). Owner: D3 (liquid XP bridge) goes to Engineering; the carve and the swap in
  one step, the project folder renamed too. Built: all ten Creative jars; not installed, not booted.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-09-08: ############################## Magic mods (generated 2026-09-08 by tools/gen_create_ie_dict.mjs) ############################## botania / dragonloot / felsic_gear / heartstone / sculktransporting (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-09-09: ############################## Worldgen mods (generated 2026-09-09) ############################## geode  (Geodes+ / geodeforge) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-09-12: ############################## Partial-mod completion (generated 2026-09-12) ############################## Filling out mods that already had some curated entries elsewhere in this file: decor/furniture, storage, PneumaticCraft, Alexs Mobs, farming/cooking addons, etc. kubejs namespace skipped — its unmapped ids are all kubejs:incomplete_* placeholders (broken item/model registrations), not real items; see memory for the list (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-09-12: ############################## Redstone mods (generated 2026-09-12) ############################## Project Red (core/fabrication/integration/transmission/illumination) + Gates. controlengineering and more_immersive_wires already fully mapped above (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-09-15: ############################## Armor mods (generated 2026-09-15) ############################## immersive_armors, armor_trims, netherite_horse_armor were entirely unmapped (0 existing entries) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-09-24: The shield block handler reaches the damage-tag classes only on the first block (crash 2026-09-24: NoClassDefFoundError on DamageFlag); pre-load them too (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-10-02: ############################## New mods 1.0.4 (generated 2026-10-02) ############################## Every item of the mods added since 1.0.2 that no section above maps yet (from kubejs/exported/items_by_mod) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-03: ===== Wandering Trader stock (2026-10-03, economy/wandering_trades.js) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-18: {@code mixin/ServerAdvancementManagerMixin}: the pack has no advancements at all (owner, 2026-10-18) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-10-19: Plate-block ↔ plate: copper's 9 plate_copper -> plate_block_copper added (2026-10-19) (kubejs/data/creativeengineering/kubejs/metal/metal_base_materials.js)
- 2026-10-19: 4. hammercrushing_zinc / raw_hammercrushing_zinc — removed with the other metals' (2026-10-19) (kubejs/data/creativeengineering/kubejs/metal/metal_base_materials.js)
- 2026-10-20: None of the overridden effects' classes override either method (checked 2026-10-20) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/MobEffectCategoryMixin.java)
- 2026-10-20: ===== JEED effect providers (2026-10-20, CB creativeengineering/jeed_providers.js, effects.md 3) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-20: ===== AE2 cables (2026-10-20, AE2 QoL Recipes port: creativecompat/kubejs/other/feature_ports.js) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-20: ===== creativelabels  -  3 new  (development/creativeLabels, feature_writeup/sticky_notes.md; 2026-10-20) ===== One sticky note item (colour = display.color, text / special under the CreativeLabels compound), one label, one eraser (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-22: ===== removed woods  -  130 new  (recipes/wood #3, feature_writeup/recipes_wood.md 2.6; 2026-10-22) ===== Upgrade Aquatic's river wood set and every mod's river-wood variant (purged, removed_woods.js), then the oak twins they merge into that no section above maps (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-22: ############################## Veggies Delight backport (generated 2026-10-22) ############################## ===== veggiesdelight (89) ===== Veggies Delight backport (development/creativeDelight/members/veggies, feature_writeup/veggies_delight.md) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-23: ===== fungidelight (32) - development/creativeDelight/members/fungi, feature_writeup/fungi_delight.md; 2026-10-23 ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-23: ===== vintagedelight (134) - development/creativeDelight/members/vintage, feature_writeup/vintage_delight.md; 2026-10-23 ===== Prefixed where the bare key was taken: kimchi, salt (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-23: ===== Removed items with no other key (2026-10-23, removed_items.js) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-24: ===== miners_delight (69) - development/creativeDelight/members/miners, feature_writeup/miners_delight.md; 2026-10-24 ===== Merged 1.4.5 + 1.1.2 port (the 1.1.2 jar has 37 of these) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-25: patcherMod: src/main/resources/kubejs_startup/creativeengineering/func/compost.js (was kubejs/startup_scripts/func/compost.js; moved 2026-10-25, creative_delight.md 10.5 item 3). patcherMod's startup loader runs this pack before the pack's startup_scripts folder, so global.compost exists before compost/compost_rules.js + compost/compost_values.js call it at top level (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/compost.js)
- 2026-10-25: Registry-agnostic: pass the event from ServerEvents.tags('item' | 'block' | 'fluid' | 'entity_type' | ...). patcherMod: src/main/resources/kubejs_startup/creativeengineering/func/tag_helper.js (was kubejs/startup_scripts/func/tag_helper.js; moved 2026-10-25, creative_delight.md 10.5 item 3) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/tag_helper.js)
- 2026-10-25: ===== oceanic_delight (47) - ocean_delight.md; 2026-10-25 ===== Prefixed oceanic_ where the bare key was taken. salmon_eggs is keyed caviar (shown as Caviar, dishes/ocean #5) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-25: Seeded 2026-10-25 by tools/food_dict.py --seed; run tools/food_dict.py (or /cdevgen run food_dict) after a /dumpfood to lint this file and list foods with no key, with a proposed key for each (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/food_dict.js)
- 2026-10-25: Every food and cooking row moved to Creative Delight: 2026-10-25 by the mod of the food it makes (kubejs/data/creativedelight/kubejs/<mod>/recipes.js), and the Brewin' and Chewin' dishes 2026-10-31 (brewinandchewin/recipes.js there, owner: the dishes stay in CD, added only while Brewin' is loaded) (kubejs/server_scripts/cooking/cooking.js)
- 2026-10-25: ===== Food dict audit (2026-10-25): foods and kitchen ingredients with no key (tools/food_dict.py) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-25: ===== Abnormals Delight 6.0.3 salvaging and recipes (abnormals_delight.md 3.1-3.4; 2026-10-25) ===== Driftwood set and Boatload boats for the cutting-board salvaging (farmersdelight/recipes.js), the pickerelweed tag (upgrade_aquatic/tags.js), Neapolitan's milk bucket tag and the raw pike tag (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-25: Every food and cooking item tag moved to Creative Delight: 2026-10-25 one tags.js per mod owning the entries (kubejs/data/creativedelight/kubejs/<mod>/tags.js; food types, jars and cans in creativeengineering/), and the Brewin' and Chewin' tags 2026-10-31 (brewinandchewin/tags.js there) (kubejs/server_scripts/tags/cookingtags.js)
- 2026-10-26: ===== JEI equipment tree (2026-10-26): gear with no key (startup_scripts/jei/order.js, jei_ordering.md) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-27: ---- wire coils + coil blocks + pack components ---------------------------------------- Keep COIL_METALS in sync with server_scripts/metal/metal_base_materials.js. coil_<m>       : wire-coil-in-progress  — gray-tint of the wire coil's item sprite, taken from i.wire_coil_<m> (the dict has one key per id since 2026-10-27, so there is no i.coil_<m> to auto-derive from; the incomplete id stays kubejs:incomplete_coil_<m>). creativeengineering:* for the 9 pack coils, immersiveengineering:* for copper / steel / electrum. coil_block_<m> : coil-block-in-progress — the block's own side texture; the coil-block item model is a 3D block, so there is no flat item sprite to derive from. copper -> IE coil_lv, electrum -> IE coil_mv (placeholder until a creativeengineering:coil_electrum block exists); the other 10 (incl. steel) -> the pack's own coil block (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/changes/transition_items.js)
- 2026-10-27: The 7 meat culture blocks moved to Creative Delight 2026-10-27 (creativeengineering/cultivated_meat_tags.js; reserved for Creative Cultivation, creative_delight.md 29.5) (kubejs/data/creativeengineering/kubejs/tags/storageblocktags.js)
- 2026-10-27: The Vintage Delight ghost pepper soil tag moved to Creative Delight 2026-10-27 (vintagedelight/tags.js) (kubejs/server_scripts/tags/blocktags.js)
- 2026-10-27: The culture base tag moved to Creative Delight 2026-10-27 (creativeengineering/cultivated_meat_tags.js; reserved for Creative Cultivation, creative_delight.md 29.5) (kubejs/data/creativeengineering/kubejs/tags/engineeringtags.js)
- 2026-10-27: The mortar and pestle moved to Creative Delight 2026-10-27 (kubejs_startup/creativedelight/bakeware.js). arrow_fletching moved to Creative Equipment 2026-11-06 (kubejs_startup/creativeequipment/items/archery_items.js) (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/misc_items.js)
- 2026-10-31: Cooking for Blockheads corner storage and counter toppers moved to Creative Delight's blockheadsAdditions (2026-10-31) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-10-31: ===== removed woods  -  Create: Dreams & Desires rose, rubber, spirit; 246 new (no tree, sapling or recipe makes a log; 2026-10-31) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-10-31: [kitchen] (counter-top offsets) moved to Creative Delight's blockheadsAdditions config (2026-10-31) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-10-31: Counter supports / toppers (Cooking for Blockheads): Creative Delight cookingforblockheads/kitchen.js (2026-10-31) (kubejs/server_scripts/tags/blocktags.js)
- 2026-10-31: Phantom prevention, one rule for the pack ({@code potions/effects #10}, decided 2026-10-31): <ul> (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/effect/PhantomBaneRules.java)
- 2026-11-01: Multi-serving cans, seasonings and placed-food Spice of Life: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: ============================================================ Smokable wood: vanilla overworld woods, per block shape (smoking recipes in wood/smoked_wood.js) ============================================================ Allow-list (decided 2026-11-01, engineering/dreams-and-desires #10): only the seven vanilla overworld woods smoke (kubejs/data/creativecommon/kubejs/tags/smokable_tags.js)
- 2026-11-01: Flowers, grass, ferns, leaves and coral get no tag (owner decision 2026-11-01) (kubejs/data/creativecommon/kubejs/tags/plantabletags.js)
- 2026-11-01: Sacks as bundles moved to Creative Compat (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: The Wandering Winemaker's stock moved to Creative Brewing 2026-11-01 (kubejs/data/creativebrewing/kubejs/vinery/wandering_trades.js) (kubejs/server_scripts/economy/wandering_trades.js)
- 2026-11-01: Salt ticks and Corn Delight villager food: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: seasonsMeltIntervalTicks: creativecalendar-common.toml [perf] (Creative Calendar, 2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-01: Remainder buffer (cooking/): Creative Delight (2026-11-01); the WTHIT line reads compat.RemainderView (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: The create:potion stacks (one REGULAR bottle per potion): Creative Brewing, kubejs/assets/creativebrewing/kubejs/creativebrewing/potion_fluid_jei.js (moved 2026-11-01) (kubejs/assets/creativecommon/kubejs/_lib/fluid_jei.js)
- 2026-11-01: The Automated Potion Mixing and Lingerification pages it used to register moved to Creative Brewing (compat.jei.PotionMachinesJEI, 2026-11-01) (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/compat/DragonBreathingJEI.java)
- 2026-11-01: Moved to Creative Calendar: kubejs/data/creativecalendar/kubejs/sereneseasons/seasontags.js (feature_writeup/creative_calendar.md 4.4, 2026-11-01) (kubejs/server_scripts/tags/seasontags.js)
- 2026-11-01: Quark monster boxes as vaults moved to Creative Compat (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Lunchbox looks, cob pipe renderer and the oven screen: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Potions, effects, potion machines, the alcohol tooltip and copper poisoning moved to Creative Brewing (2026-11-01, creative_brewing.md 4.2 phases 2 and 3) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Season types ([seasons]), crop seasons ([crop_seasons]) and the [time] master: Creative Calendar's CalendarConfig (creativecalendar-common.toml), moved with their keys and comments on 2026-11-01 (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-01: Moved to Creative Calendar: kubejs/data/creativecalendar/kubejs/sereneseasons/cropseasontags.js (feature_writeup/creative_calendar.md 4.4, 2026-11-01) (kubejs/server_scripts/tags/cropseasontags.js)
- 2026-11-01: [seasons], [crop_seasons], [time]: creativecalendar-common.toml (Creative Calendar, 2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-01: [time], seasons and crop seasons (/ccaltime): Creative Calendar (development/creativeCalendar), carved 2026-11-01 (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Lunchbox colors (onItemColors): Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Botanist workbench pot and flower box plant models moved to Creative Compat (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Per-hoe Quark harvest area and its tooltip: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: The Wandering Winemaker's pool helpers (winemakerReset / winemakerSell / winemakerSize) moved to Creative Brewing 2026-11-01 (kubejs_startup/vinery/winemaker_trades.js in the CB jar), which adds them to this object (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/trade_helper.js)
- 2026-11-01: Spice of Life lunchboxes: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: The Weathered Cardboard Box moved to Creative Compat on 2026-11-01 (creativecompat/decor_blocks.js in that jar) (kubejs/startup_scripts/items/decor_blocks.js)
- 2026-11-01: Oven, powdered sugar, Display Delight dishes: Creative Delight (2026-11-01, creative_delight.md 34) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Polymorph in the Sophisticated crafting upgrade, IE and Pretty Pipes tables moved to Creative Compat (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Respite potted tea bush and the KubeJS can action: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: Vine shearing moved to Creative Compat (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-01: On a world load Forge fires the event from its STAGING copy (GameData.injectSnapshot), which holds only the snapshot's ids at that point, so {@code mapping.getRegistry()} never contains an id that is new in this version (found 2026-11-01, scratch_run_2026-11-01.md 7) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/swap/SwapRemapper.java)
- 2026-11-01: Rustic Delight member items, 2026-11-01 (rustic_delight.md 5.10): roll assemblies, Supreme Pizza, golden coffee beans (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-11-01: troughMissCooldownTicks: creativedelight-common.toml [perf] (Creative Delight, 2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-01: Corn Delight cob pipe: Creative Delight (2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-02: the pack vinegar: the mason jar since the 2026-11-02 jar merge (vintage_delight.md S9, plan_scratch_bugfix_1103 1.1) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-11-02: Seeded 2026-11-02 by tools/kitchen_dicts.py --seed (108 keys); run tools/kitchen_dicts.py to lint this file and misc_dict.js and list member items with no key (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/tool_dict.js)
- 2026-11-02: Villager ladders, structure ground match, earth fill, template trim and wild plant soil tags: Creative Worldgen (creativeworldgen/kubejs/tags/blocktags.js, 2026-11-02) (kubejs/server_scripts/tags/blocktags.js)
- 2026-11-02: ===== removed woods  -  Quark azalea; 7 new (the rest already had keys; farming/trees #5, recipes_wood.md 2.6; 2026-11-02) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-11-02: [forgiving_void], [leaf_decay], [raids], [worldgen], [named_mobs] and the [perf] mob cap keys: creativeworldgen-common.toml (Creative Worldgen, 2026-11-02) [botania]: creativemagic-common.toml (Creative Magic, 2026-11-04) [shrapnel], [surface], [vein_undo]: creativeengineering-common.toml (Creative Engineering, 2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-02: Seeded 2026-11-02 by tools/kitchen_dicts.py --seed (227 keys); run tools/kitchen_dicts.py to lint (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/misc_dict.js)
- 2026-11-02: Rustic Delight pancakes, 2026-11-02 (pancake family, food_families.md 4.7, TODO dishes/meals #12): the six flavored single pancakes (food type sweet/pancake) and the seven placed plates (block items, PancakeStackBlock) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-11-02: mobCapLevels, mobCapForceLevel, mobCapLevel1/2Chunks, mobCapHysteresisChunks, mobCapLevel1/2Monster, mobCapScaledCategories, spawnChunksLevel1/2, spawnBandLevel1/2, spawnBandCategories: creativeworldgen-common.toml [perf] (Creative Worldgen, 2026-11-02) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-02: Pack text overrides (none since the glycerol one went, 2026-11-02) are lang entries in kubejs/assets/pneumaticcraft/lang/en_us.json (kubejs/startup_scripts/changes/pnc_tooltips.js)
- 2026-11-02: World generation and fauna (structures, locate, earth and water fill, terrain adaptation, meta structures, sky islands, bee nests, mob spawns, mooshrooms, named mobs, leaf decay, the biome_tag loot condition, the Farmer's Structures donor, Forgiving Void) moved to Creative Worldgen (2026-11-02, creative_split.md 7.6) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-02: ===== Earth fill sand base: Philip's desert brick blocks in server_scripts/tags/blocktags.js (2026-11-02) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/components_amethyst.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/components_amethyst.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/dust_piles.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/dust_piles.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/wood.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/wood.js)
- 2026-11-04: flint_and_terrasteel: Creative Magic (creativemagic, 2026-11-04) (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/item/CreativeEngineeringItems.java)
- 2026-11-04: TNT shrapnel, the IE tweaks, BlockRunner surfaces (speeds, camo tooltip, sinking): Creative Engineering (2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/wood/sawdust.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/wood/sawdust.js)
- 2026-11-04: Stays in the pack folder at the Engineering carve (2026-11-04): it must run before other/item_merges.js (priority -100), and script packs of different jars load in no fixed order (kubejs/server_scripts/wood/removed_woods.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/resource_blocks.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/resource_blocks.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/wood/wood_types.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/wood/wood_types.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/dust.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/dust.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/tags/metaltags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/tags/metaltags.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/plate_blocks.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/plate_blocks.js)
- 2026-11-04: The sawmill classes pre-loaded here for SawmillProcessMixin: Creative Engineering (2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Shrapnel renderer: Creative Engineering (2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/process.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/process.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/components.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/components.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/fabric.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/fabric.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/coil_blocks.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/coil_blocks.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/end.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/end.js)
- 2026-11-04: The mixins moved from patcherMod on 2026-11-04 (Creative Split wave 8) unchanged: same package, SRG names with remap = false, creativeengineering$ member prefixes (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/EngineeringMixinPlugin.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/mechanisms.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/mechanisms.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/tags/engineeringtags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/tags/engineeringtags.js)
- 2026-11-04: Shared Forge setup (runs, mods.toml expansion, jar manifest, jar_archive, installToMods) comes from build-logic/src/main/groovy/ce.forge-mod.gradle; this file has only what is specific to Creative Common (patcherMod until the 2026-11-04 swap) (development/creativeCommon/build.gradle)
- 2026-11-04: Creative Common (patcherMod until 2026-11-04): src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js (was kubejs/startup_scripts/func/neoCraftHelper.js; moved 2026-10-25, creative_delight.md 10.5 item 3) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/metal/misc_metal_crafts.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/metal/misc_metal_crafts.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/vibrating.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/vibrating.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/music.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/music.js)
- 2026-11-04: Creative Common's mod id (patcherMod's until the 2026-11-04 swap): its whole pack goes first (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/kubejs/JarStartupScripts.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/redstone.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/redstone.js)
- 2026-11-04: This jar's mod id since the Creative Split swap (2026-11-04, creative_split.md 6 Q1 and 7.13): patcherMod was (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/mob_resources.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/mob_resources.js)
- 2026-11-04: Stepstool key, tier shield item properties: Creative Equipment (2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/health.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/health.js)
- 2026-11-04: The liquid XP bridge diagnostic, the dragon breathing fan type and the IE hammer blacklist: Creative Engineering (2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/wood/smoked_wood.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/wood/smoked_wood.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/pneumatic.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/pneumatic.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/oil.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/oil.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/transport.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/transport.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/components.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/components.js)
- 2026-11-04: Split out of items/echo_tools.js on 2026-11-04 when the echo tools moved to Creative Equipment (creative_split.md 7.6, wave 7); rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/components_amethyst.js)
- 2026-11-04: Creative Engineering (feature_writeup/creative_engineering.md): the pack's machines and engineering content, carved out of patcherMod in the Creative Split wave 8 (2026-11-04, creative_split.md 7.6 and 7.13) (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/CreativeEngineering.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/tags/wood_tags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/tags/wood_tags.js)
- 2026-11-04: Sawblade and sawdust items, the shrapnel entity, the dragon breathing recipe type, the wooden bucket: Creative Engineering (creativeengineering, 2026-11-04, creative_split.md 7.13 wave 8) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/items/misc_items.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/misc_items.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/tags/storageblocktags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/tags/storageblocktags.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/wood/ash.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/wood/ash.js)
- 2026-11-04: creativeEngineering: Creative Engineering (mod id creativeengineering, feature_writeup/creative_engineering.md), carved out of patcherMod on 2026-11-04 (creative_split.md 7.6 and 7.13, wave 8) (development/creativeEngineering/build.gradle)
- 2026-11-04: [dragon_gear], [air_brake], [stepstool], [crafting], [armor_enchantability], [shields], [bonemeal_charm], [bottled_air], [backpackSkins]: creativeequipment-common.toml (Creative Equipment, 2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-04: Nothing in the pack passes air or the empty fluid on purpose (checked 2026-11-04); in the full pack the label showing up in the [absent mods] summary means a real missing id (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/metal/metal_building.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/metal/metal_building.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/crystal.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/crystal.js)
- 2026-11-04: Dark Fire and Flint and Terrasteel: Creative Magic (creativemagic, 2026-11-04) Elytra duplication, tier shields, backpack skins, stepstools, the air brake, Bottled Air, the copper tool tier and the gear mixins: Creative Equipment (creativeequipment, 2026-11-04, creative_split.md 7.6 wave 7) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/dreams_desires.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/dreams_desires.js)
- 2026-11-04: Creative Engineering; moved from kubejs/startup_scripts/changes/transition_items.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/changes/transition_items.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/create_utilities.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/create_utilities.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/power.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/power.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/metal/metal_base_materials.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/metal/metal_base_materials.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/computing.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/computing.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/wood/bark.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/wood/bark.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/laser_cutting.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/laser_cutting.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/engineering/blueprints.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/engineering/blueprints.js)
- 2026-11-04: Creative Engineering; moved from kubejs/server_scripts/tags/fluidtags.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged (kubejs/data/creativeengineering/kubejs/tags/fluidtags.js)
- 2026-11-04: Hands, face, feet Curios slots and the "Slot:" tooltip line: Creative Equipment (2026-11-04) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-06: One effect: registers {@code <name>}, {@code long_<name>} and, for an effect with levels, {@code strong_<name>} in creativecommon (plan_cb_polish.md 2; creativebrewing: until 2026-11-06, data/creativecommon/swaps/effects_potions.json) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/effect/CommonPotions.java)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's server_scripts/magic/tool_effects.js (kubejs/data/creativecommon/kubejs/effects/tool_effects.js)
- 2026-11-06: The animal-fat torch moved to Creative Delight 2026-11-06 (creativeengineering/animal_fat_torch.js there) (kubejs/data/creativeengineering/kubejs/wood/ash.js)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's server_scripts/magic/armor_effects.js (kubejs/data/creativecommon/kubejs/effects/armor_effects.js)
- 2026-11-06: end gear (echo, felsic, dragon) and air brake rows: Creative Equipment's items/end_gear.js since 2026-11-06 (TODO `creativebrewing/publish #22`) (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/changes/transition_items.js)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2: the effect frameworks live in Common, the rows with the jar that owns the gear: Creative Equipment's kubejs_startup/creativeequipment/gear_effects.js) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/effects/tool_effects.js)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2: every potion and effect the Creative mods own is registered from Common, under creativecommon:; was creativebrewing:magic_resistance, remapped by data/creativecommon/swaps/effects_potions.json) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/effects/magic_resistance.js)
- 2026-11-06: Until 2026-11-06 night the loop only counted and the assembly was still built (scratch r1106spf SQ-CBP-03b: horror lasagna kept its empty filling step) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
- 2026-11-06: [effects] and [magic_resistance] came from Creative Brewing's file (plan_cb_polish.md 2, 2026-11-06), and before that from patcherMod's: the first boot copies the player's values in (sections this file already has are kept) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-06: <p>On 2026-11-06 the non-engineering features moved out with their ids unchanged: TNT shrapnel, Ore Excavation's undo rules and BlockRunner surfaces to Creative Compat (with [shrapnel], [surface] and [vein_undo], so this jar has no config file any more), the liquid XP bridge to Creative Magic, the wooden bucket to Creative Equipment (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/CreativeEngineering.java)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's client_scripts/armor_effects_tooltip.js (kubejs/assets/creativecommon/kubejs/effects/armor_effects_tooltip.js)
- 2026-11-06: Creative Compat: required by every Creative jar (creative_split.md 7.6); no class of it is called since the surface/ code moved there (2026-11-06) (development/creativeEngineering/build.gradle)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2), from Creative Brewing; before that the pack's client_scripts/tool_effects_tooltip.js (kubejs/assets/creativecommon/kubejs/effects/tool_effects_tooltip.js)
- 2026-11-06: [effects], [magic_resistance]: from creativebrewing-common.toml (plan_cb_polish.md 2, 2026-11-06); the constructor carries the player's values over (EFFECT_SECTIONS) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-06: effect mixin from Creative Brewing (plan_cb_polish.md 2, 2026-11-06); the Create tea pipe icon went to Engineering and the Fruits Delight remap to Delight 2026-11-07 (creative_engineering.md 7.1) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/perf/CommonMixinPlugin.java)
- 2026-11-06: Every potion and mob effect the Creative mods own (plan_cb_polish.md 2, from Creative Brewing 2026-11-06): the creativecommon: potions (Magic Resistance; Better Beacons potions are Creative Brewing's since 2026-11-07; the vanilla-effect ones are KubeJS, kubejs_startup/creativecommon/effects/potions.js), Magic Resistance's damage cut (the MobEffect itself is KubeJS, effects/magic_resistance.js) and Phantom Bane's rules (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CreativeCommon.java)
- 2026-11-06: any metal dust or grit (Steel Toe Stout, alcohol.md 8.6): moved 2026-11-06 to Creative Common's data (data/creativeengineering/tags/items/metal_dusts.json, optional entries) so Creative Brewing finds the tag alone (kubejs/data/creativeengineering/kubejs/tags/metaltags.js)
- 2026-11-06: Creative Common since 2026-11-06 (plan_cb_polish.md 2: the effect frameworks live in Common, the rows with the jar that owns the gear: Creative Equipment's kubejs_startup/creativeequipment/gear_effects.js) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/effects/armor_effects.js)
- 2026-11-06: The XP bridge, Ore Excavation, BlockRunner and camouflage-order mixins moved on to Creative Magic and Creative Compat on 2026-11-06 (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/EngineeringMixinPlugin.java)
- 2026-11-06: Mulch and millable-seed tags moved to Creative Delight 2026-11-06 (creativeengineering/compost_tags.js there); the Vinery barrel tag to Creative Brewing (vinery/tags.js) (kubejs/data/creativeengineering/kubejs/tags/wood_tags.js)
- 2026-11-06: Pack fluids (Creative Delight kubejs_startup/creativedelight/fluids.js; creativeengineering: until 2026-11-06, remapped by CD swaps/food_fluids.json) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/fluid_dict.js)
- 2026-11-06: The creativecommon: potions of vanilla effects (plan_cb_polish.md 2, 2026-11-06: every potion and effect the Creative mods own is registered from Creative Common; these were creativebrewing:*, before that kubejs:*, all remapped by data/creativecommon/swaps/effects_potions.json and, for ids saved in item NBT, PotionByNameRemapMixin) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/effects/potions.js)
- 2026-11-06: No config of its own since 2026-11-06: [shrapnel], [surface] and [vein_undo] moved to Creative Compat's CompatConfig (creativecompat-common.toml); every table NCH.cfg reads is Common's CommonConfig or another carved jar's class (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/ce_core.js)
- 2026-11-07: Reserved for Creative Economy: wandering trader trades and spawning move to the future economy mod (trading and loot chests; owner, 2026-11-07, creative_split.md 2.3) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/trader/WanderingTraderTweaks.java)
- 2026-11-07: JEI: drink fluid effect lines in fluid tooltips (mixin/JeiFluidHelperTooltipMixin, from Creative Common 2026-11-07); the tested range is in EngineeringMixinPlugin MOD_RANGE (development/creativeEngineering/src/main/resources/META-INF/mods.toml)
- 2026-11-07: Reserved for Creative Economy: wandering trader trades and spawning move to the future economy mod (trading and loot chests; owner, 2026-11-07, creative_split.md 2.3) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/WanderingTraderSpawnerMixin.java)
- 2026-11-07: Phantom Bane rules (rest reset, spawner skip); its potions are Creative Brewing's since 2026-11-07 (development/creativeCommon/src/main/resources/META-INF/mods.toml)
- 2026-11-07: Drink fluid effect lines in JEI fluid tooltips (compat/DrinkFluidEffects, from Creative Common 2026-11-07): client only, and only with Create, whose emptying recipes it reads (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/CreativeEngineering.java)
- 2026-11-07: <p>From Creative Common 2026-11-07 (creative_engineering.md 7.1) (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/compat/DrinkFluidEffects.java)
- 2026-11-07: Reserved for Creative Economy: wandering trader trades and spawning move to the future economy mod (trading and loot chests; owner, 2026-11-07, creative_split.md 2.3) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/WanderingTraderTradesMixin.java)
- 2026-11-07: Rose gold counts as gold (TODO recipes/metal #3, owner 2026-11-07; #7 closed as a decision 2026-11-14, plan_bugfix_1114.md 4.2) (kubejs/data/creativeengineering/kubejs/metal/rose_gold_interchange.js)
- 2026-11-07: From Creative Common 2026-11-07 (creative_engineering.md 7.1): sequenced assembly keeps gear NBT, Builder's Tea pipe Haste icon, IE fluid inputs by id with subset NBT match, drink fluid effect lines in JEI (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/EngineeringMixinPlugin.java)
- 2026-11-07: [armor_overflow]: creativeequipment-common.toml; [jei], [gui_title], [toasts], [tooltips], [coaster], [wthit]: creativecompat-common.toml (interface carve, 2026-11-07) [leaf_piles], [moss], [vine_shearing], [campfire], [monster_box], [trash_slot]: creativecompat-common.toml (Creative Compat, 2026-11-01) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-07: The time goes in as a parsed JSON number: a JS number (and even Integer.valueOf) makes Rhino pick addProperty(String, String) and write "400.0", which Farmer's Delight rejects ("Expected cookingtime to be a Int"), dropping the recipe (checked on a scratch server 2026-11-07) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/neoCraftHelper.js)
- 2026-11-07: Added 2026-11-07 with the mixins that came from Creative Common, whose ranges they had; the same bounds as Common's and Compat's tables (development/creativeEngineering/src/main/java/com/SaintAlfonzo/creativeengineering/EngineeringMixinPlugin.java)
- 2026-11-07: The Better Beacons potions (long_reach, phantom_bane) are Creative Brewing's since 2026-11-07 (potion/BetterBeaconsPotions) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/effect/CommonPotions.java)
- 2026-11-07: <li>Farmer's Respite's Caffeinated adds a hidden Phantom Bane: Creative Delight's {@code compat.farmersrespite.CaffeinatedPhantomBane} and its CaffeinatedEffectMixin (moved 2026-11-07).</li> (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/effect/PhantomBaneRules.java)
- 2026-11-07: KubeJS's generated pack outranks mod jars, so hand-authored JSONs in this jar would be ignored; do not add them back (2026-11-07: they rendered untextured after the jar move) (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/coil_blocks.js)
- 2026-11-07: KubeJS's generated pack outranks mod jars, so hand-authored JSONs in this jar would be ignored; do not add them back (2026-11-07: they rendered untextured after the jar move) (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/dust_piles.js)
- 2026-11-07: KubeJS's generated pack outranks mod jars, so hand-authored JSONs in this jar would be ignored; do not add them back (2026-11-07: they rendered untextured after the jar move) (development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/plate_blocks.js)
- 2026-11-08: [loading_tips]: creativecommon-client.toml (CommonClientConfig, per client, 2026-11-08) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-08: Replaces CreateGravityEntitiesMixin of 2026-11-08 00:01 (a velocity add-back that needed a motion packet per entity per tick) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/LowGravityPullMixin.java)
- 2026-11-08: Forge is about to add old -> survivor, and a world saved before an earlier swap the other way (old -> survivor is the reverse of a saved survivor -> old) would then hold a cycle. {@code ForgeRegistry.getRaw} follows aliases with no cycle check and spins forever when neither name is registered yet, which is the case while it copies the staging registry into the active one (hang found 2026-11-08) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/swap/SwapRemapper.java)
- 2026-11-08: Until 2026-11-08 the second step was a hard-coded list naming Creative Compat (scratch run r1108cge) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/compat/kubejs/JarStartupScripts.java)
- 2026-11-10: Creative Common; moved from Creative Engineering's tags/wood_tags.js 2026-11-10 (plan_cd_namespace.md, owner decision) (kubejs/data/creativecommon/kubejs/tags/smokable_tags.js)
- 2026-11-10: confectionery: every placed dish, plate, jar and bottle (decided 2026-11-10), food blocks, chocolate (kubejs/server_scripts/tags/blocktags.js)
- 2026-11-10: Breeding and pollination tags (feature_writeup/breeding_audit.md, owner decisions 2026-11-10) (kubejs/server_scripts/tags/animal_tags.js)
- 2026-11-10: ===== Visceral Heap items (Biomes O' Plenty) and flesh crush drops (plan_flesh_recipes.md, 2026-11-10) ===== (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js)
- 2026-11-10: On the final dispatcher, not on CapabilityProvider: that is a superclass of Level, Entity and ItemStack, and a mixin there changes how Mixin binds other mods' super calls in those classes (IncompatibleClassChangeError in Level, found 2026-11-10) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/mixin/SwapCapabilityKeysMixin.java)
- 2026-11-10: ── Audit 2026-11-10 (plan_blockrunner_audit.md): every walkable block gets a tier ────────── Part 1, real materials: families that walk like a tier above (kubejs/server_scripts/tags/blocktags.js)
- 2026-11-10: Smokable wood tags moved to Creative Common 2026-11-10 (data/creativecommon/kubejs/tags/smokable_tags.js) (kubejs/data/creativeengineering/kubejs/tags/wood_tags.js)
- 2026-11-13: Modded flame lights counted as fire (plan 3.8 table, 2026-11-13 dump); the non-flame ones of the same mods are left out (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/block_dict.js)
- 2026-11-14: Namespace -> mod id, read from each jar's mods.toml and data/assets folders (2026-11-14) (development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/has.js)
- 2026-11-14: [melting]: new 2026-11-14 (melting/ package, feature_writeup/melting.md, plan_bugfix_1114.md 3.8) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/CommonConfig.java)
- 2026-11-14: Something still references those objects, so a GC alone frees nothing (tested 2026-11-14) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/util/StaleJarHandles.java)
- 2026-11-15: no MOD_RANGE row: the jar's mods.toml version is ${file.jarVersion} with no Implementation-Version, and the redirect is require = 0, so any loaded Rare Ice keeps it (2026-11-15, SQ-MELT-09) (development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/perf/CommonMixinPlugin.java)


## 7. License audit: Creative Common (2026-11-04, plan_cb_publish.md Step 2)

Audit of `creativecommon-1.1.0.jar` (23:34). Summary row in [creative_brewing.md](creative_brewing.md) 9.2.

| Part | Source license | In the jar | Verdict |
|---|---|---|---|
| Own Java (about 270 classes) and 70 mixins | own, GPLv3 | yes | OK |
| `IwBlockGrowthHandlerMixin` fallback (about 10 lines of IW 1.2.10's body, the only `@Overwrite`) | Immersive Weathering, LGPLv3 | yes | OK; attribution added 2026-11-05 (`NOTICE-thirdparty.txt`, source comment) |
| `ArmorOverflowRenderer.row0Y` / `drawRow` (from Detail Armor Bar 2.6.4 `ArmorBarRenderer#render`) | MIT | yes | OK; MIT notice added 2026-11-05 (`NOTICE-thirdparty.txt`, source comment) |
| commonmark-java 0.21.0, shaded to `...creativecommon.shadow.commonmark` | BSD-2-Clause | yes, with `NOTICE-commonmark-BSD.txt` | fixed 2026-11-05 (names Creative Common and the `creativecommon` package) |
| `kubejs_startup/creativecommon/_lib` (12 dicts) and `func/` helpers, `kubejs/_lib` scripts, data, lang, plugin json | own | yes | OK |
| 12 `textures/gui/*_tint.png` (27x9) | **Mojang-derived**: recolours of the vanilla armor icon | yes | OK: vanilla art and edits of it are accepted everywhere (owner, 2026-11-05, [creative_brewing.md](creative_brewing.md) 9.5) |
| 6 title panorama PNGs (`assets/minecraft/.../title/background/`) | own: the owner's in-game capture (owner, 2026-11-05) | no (out 2026-11-07; back and out again 2026-11-08: the capture is a flat sky with no terrain, so it looked blank; vanilla by default); the pack keeps its `kubejs/assets/` copy | OK |
| `credit.txt` (Numismatics by Layers of Railways, LGPL-3.0; owner 2026-11-05: https://www.curseforge.com/minecraft/mc-mods/numismatics) | the file it covers is not traced (no Numismatics jar to compare against) | yes | OK: kept, URL added |
| `mods.toml` `GPL-3.0-only`, `creativecommon-LICENSE.txt` | own | yes | OK |
| Farmer's Structures donor (ARR) | read at runtime from `farmers_upstream/` | no | OK |

Fixes: TODO `licence/common #1` (tint sheets, closed: vanilla edits accepted), `#2` (notices, `credit.txt`), `#3` (panorama,
closed: the owner's own). No license row open for Common.

### 7.1 Publishing readiness (2026-11-05, TODO `creativecommon/publish #1`)

Owner 2026-11-05: Creative Common is used only inside the pack for now, so the pack's defaults stay; a library is fine on
CurseForge. Built and installed 2026-11-05 23:40 (`creativecommon-1.1.0.jar`). Superseded 2026-11-07 (owner): the defaults
are now vanilla and the pack sets its values in its config (vanilla defaults below).

**Immersive Portals with Serene Seasons (2026-11-06, reworked 2026-11-07).** Serene Seasons' access transformer makes
every ChunkMap method public; Immersive Portals 2.3.7's `MixinChunkMap_C` overwrites `m_183760_` (playerLoadedChunk) as
private, and IP's config does not set `overwrites.conformVisibility`, so Mixin throws "cannot reduce visibility" when
ChunkMap loads (server crash at world creation or single-player join; scratch r1106spf, owner crash 2026-11-07 12:55).
The first fix (an empty carrier mixin whose `preApply` set the method back to private) could not work: Mixin runs that
check while building the target context, before any plugin `preApply` (SQ-SPFX-01 FAIL r1106spfx). Now
`CommonMixinPlugin.acceptTargets` (gated on `immersive_portals` `[2.3.7,2.4)`) sets `conformAccessModifiers` on IP's
`MixinConfig` through Unsafe, as the Heartstone reorder does, so Mixin upgrades the overwrite to public with one
"Static binding violation" warning and logs `[compat] Immersive Portals overwrites conform to target visibility`.
Built and installed 2026-11-07 13:00; test SQ-SPFX-01 (TODO `creativesplit/regression #19`).

**Heartstone with Immersive Portals (2026-11-06 23:07).** Heartstone 1.1.2's client `LevelRendererMixin` @ModifyArg's the
`renderEntity` call in `LevelRenderer.renderLevel`, and Immersive Portals 2.3.7's `MixinLevelRenderer` @Redirect's that same
call. Both are priority 1000, IP registers first, so Heartstone's injector (require 1) finds no call and the client crashes at
launch. `CommonMixinPlugin.orderHeartstoneBeforePortals` (from `acceptTargets`, retried in `preApply` until it finds the config)
sets that one MixinInfo's priority to 999, so it applies first and both injectors work. Mixin neither exports nor opens its
`transformer` package to mods, so `Config.allConfigs` (static; `Mixins.getConfigs()` is only the not-yet-selected queue, which
was empty by then: first build 23:07 found nothing and the crash repeated at 23:10, fixed 23:11), `Config.config`,
`MixinConfig.pendingMixins`/`mixins` and `MixinInfo.priority` are read
and written with Unsafe; only `IMixinConfig`/`IMixinInfo` methods are called. Guards: client only, both mods loaded and
inside `MOD_RANGE` (`heartstone` `[1.19.2-1.1.2,1.19.2-1.2)`, `immersive_portals` `[2.3.7,2.4)`); any reflection failure
logs one ERROR and leaves Mixin unchanged. The shared `GatedMixinPlugin` gained `modOn(modId)` for this. Test CQ-SPFX-02.

**Mixin version gates.** `perf/CommonMixinPlugin` `MOD_RANGE` gives each of the 20 mods Common mixes into a tested range (Immersive Portals added 2026-11-06);
the shared `GatedMixinPlugin` (development/shared/mixinplugin) skips that mod's mixins with one WARN
(`[creativecommon] <mod> <version> is outside the tested range <range>: its mixins are off`) when the loaded version is
outside it, as when the mod is absent. Rule: from the version the pack runs to the mod's next minor release
(internal classes change there most). Raise a bound only after checking the mixins against that jar. Mixins into
vanilla and Forge are not gated (MC 1.19.2 and Forge `[43,)` are fixed). 47 of 70 mixins are gated (was 40):
`IwBlockGrowthHolderMixin` had no row (a boot crash without Immersive Weathering), the 6 KubeJS mixins were ungated, and
`IcebergTitleBreakMixin` now needs Legendary Tooltips (it reads Legendary Tooltips' `ItemModelComponent`, a
`NoClassDefFoundError` with Iceberg alone; Legendary Tooltips requires Iceberg). Offline harness: every range holds the
installed version and excludes its upper bound; a malformed range counts as out.

**Dependencies** (`mods.toml`; list the same on the CurseForge project page):

| Kind | Mod (mod id) | Range | Pack version |
|---|---|---|---|
| Required | Forge, Minecraft | `[43,)`, `1.19.2` | 43.3.9 |
| Required | KubeJS (`kubejs`) | `[1902.6.2-build.73,1902.7)`, also in `mods.toml` (Common's scripts need its mixins) | 1902.6.2-build.73 |
| Required through KubeJS | Rhino, Architectury (not declared by Common, no direct use) | KubeJS's own | 1902.2.3-build.284, 6.6.92 |
| Optional, mixins (gated) | JEI 11.8.x, WTHIT (plugins only), Create 0.5.1 (`[0.5.1.i,0.5.2)`), Immersive Weathering 1.2.x, Immersive Engineering 9.2.x, Detail Armor Bar 2.6.x, Armor Trims 1.3.x, WorldEdit 7.2.x, Quark 3.4, Create Gravity 1.2.x, Nock Enough Arrows 1.2.x, Lightman's Currency 2.2.x, Legendary Tooltips 1.4.x (+ Iceberg), Pretty Pipes 1.13.x, FTB Library 1902.4.x, FTB Quests 1902.5.x, Wares 1.2.x, Patchouli 1.19.2-77+, AE2 12.9.x | exact bounds in `MOD_RANGE` | as in `mods/` |
| Optional, plain compat (no version gate) | WTHIT, Supplementaries + Moonlight, Unlit Campfire, Framed Blocks, Farmer's Delight, Farmer's Respite, Applied Cooking, Create: Deep Dark (armor bar icons by id) | `*` | as in `mods/` |

Optional rows use `versionRange="*"` in `mods.toml`: the gate skips an out-of-range mod, where a range there would stop the
boot. Removed: ItemPhysic, Create: Deep Dark's echo mixin and Easy Elytra Takeoff rows (their mixins left with the carves).

**`mods.toml`**: description rewritten (library, interface, fixes and performance, pack settings); `issueTrackerURL` and
`displayURL` point to the repo; authors from `gradle.properties`; stale patcherMod-era comments gone.

**Vanilla defaults (2026-11-07, owner; TODO `creativecommon/publish #2`)**. Built and installed 13:27. A standalone install
behaves as vanilla; the pack keeps its behavior through `config/creativecommon-common.toml` (and the `configs_edited/root_config/`
copy), which already held every value explicitly:

| Setting | Default now (vanilla) | Pack value (in config) |
|---|---|---|
| `[advancements] enabled` | true | false |
| `[toasts] tutorial`, `advancement` | true, true | false, false |
| `[jei] packOrder` | false | true |
| `[wandering_trader] enabled`, `traderIntervalTicks`, `commonTrades`, `rareTrades` | false, 24000, 5, 1 | true, 96000, 25, 5 |
| `[creative_menu] disabled` (new key; `CreativeMenuDisableMixin` now asks it, before: always on) | false | true |

The title panorama left the jar for `kubejs/assets/minecraft/textures/gui/title/background/` (pack-owned, shipped to
clients as `assets/*`; KubeJS assets rank above mod jars). **2026-11-08 (owner): the six images were put back in Common's jar, found to be a terrain-free sky capture (blank on screen) and taken out again; Common ships no panorama (vanilla). A new capture is owner work; the planned switch is a built-in resource pack, enabled by default, with a client config key, stepping aside when a panorama mod is installed.** The `mods.toml` description now says the pack settings are off by
default. Kept as they are: the swap triggers (`creativecommon-swaps.toml`) act only on merge lists that jars ship, so they are
library behavior, not pack taste. Pack-flavored defaults still in place and not in this change: `[mod_names] overrides`
(the pack's display-name list), `[effects]` remap / removed / HUD lists. Tests SQ-CMV-04, CQ-CMV-03.

**Project page**: CurseForge project created 2026-11-07 (private, `curseforge.com/minecraft/mc-mods/creative-common`);
description draft in [project_pages/creative_common.md](project_pages/creative_common.md). Update it when a feature,
dependency range or default changes.

**Project page rewritten 2026-11-07 evening** for the interface carve (creative_split.md 7.19, 7.20): JEI, WTHIT,
tooltip, toast, title plaque, JEED and Create config widget rows now point to Creative Compat, the armor bar to Equipment;
Better Beacons potions (Brewing) and Farmer's Respite Caffeinated (Delight) dropped; the `[toasts]` and `[jei] packOrder`
settings left the table (Compat's file); added trade sync, armor and tool effect frameworks, absent-mod summary, effect
rules, drink fluid effects, the Loading Tips (Naw7k, MIT) credit; the dependency table above is pre-carve (AE2, FTB,
Patchouli, Lightman's, Pretty Pipes, Wares, Legendary Tooltips, JEI, Detail Armor Bar rows are no longer Common's).

**Still open before an upload** (review 2026-11-07 evening; items 2 to 5 done the same evening, 7.2):
1. Installed 2026-11-07 19:35 and booted in scratch r1107cc ([scratch_run_2026-11-07.md](testing/scratch_run_2026-11-07.md) 3):
   SQ-CC-01..11, -13..16 and SQ-CPP-01 to -05 PASS, which cover SQ-CMV-01, -02 (Common alone, with Compat and Calendar),
   -04 and SQ-SPFX-01 on this build. Owner client 2026-11-07 evening (scratch_run_2026-11-07.md 5): CQ-CCO-01,
   CQ-CMV-01, CQ-CCB-01 PASS; CQ-CCO-02, -03, CQ-SPFX-02 partly done; CQ-CCB-03 crashed (Compat needs the Tempad donor
   jar, TODO `creativecompat/publish #1`). Still open: the full-pack client rows (CQ-CC-01 to -06, CQ-CPP-01,
   CQ-CMV-02, -03, CQ-CCB-02, CQ-CIF-01), SQ-CC-12, SQ-CMV-03 (out-of-range version WARN), SQ-CCO-08 / Gravity.
2. Done: `mods.toml` description (gradle.properties `mod_description`) matches the project page.
3. Done: Detail Armor Bar notice moved to Equipment's `NOTICE-thirdparty.txt`.
4. Done: `[mod_names] overrides` and `[effects] removed` default to empty; Creative Tips left the jar for the pack's
   `kubejs/assets/creative_tips/`. Still pack-flavored, kept: `[effects] forceHudIcon` / `forceHudIconItems` (they
   only show icons).
5. Done: no tag in another jar's namespace (7.2).
6. Commit, push and tag the source (creative_brewing.md 9.4 R6); the page links the repo.

**Release scope 2026-11-08 (owner): dedicated server tests are not completed for this release.** Dropped from the upload
gate: SQ-CC-12, SQ-CMV-03, CQ-CGE-03, -03a to -03c, the server-join half of CQ-SPFX-02 (CQ-CGE-01 is superseded by
SQ-CGE part 2). So trade sync, the out-of-range version warning and low gravity on a client joined to a dedicated
server ship untested on this build. Project page updated the same day for 7.4 (low gravity, ItemPhysic, script phase).
Gate now: the client rows CQ-CC-01 to -06, CQ-CPP-01, CQ-CMV-02, -03, CQ-CCB-02, CQ-CIF-01, the no-Immersive-Portals
launch of CQ-SPFX-02; source tag on the final commit. `mod_description` and the optional `itemphysic` row in `mods.toml`
done 2026-11-08 17:57 (built, in CEPortTesting, NOT installed in this instance). CEPortTesting set for the Common client
rows: Common, Compat, KubeJS, Rhino, Architectury, JEI 11.8.1.1034, WTHIT, Bad Packets, Create, Heartstone, Moonlight,
Immersive Portals, Framed Blocks, SecurityCraft; this instance's Common, Compat and Immersive Portals configs (its own
in `config_backup_2026-11-08/`), tips, panorama and changelog. Full-pack only: the Engineering half of CQ-CPP-01,
CQ-CCB-02, the JEI order check of CQ-CMV-03, CQ-CIF-01 (Compat and Engineering features).

**Log cleanup 2026-11-08 evening** (scratch_run_2026-11-08.md 4, tests CQ-CLOG-01, -02; Common and Compat built, in
CEPortTesting, NOT installed in this instance): NCH's Create helpers that use KubeJS Create's schema now skip and count
for `kubejs_create` when that addon is missing; Compat guards the copper vertical slab and soap rows and registers the
flower box extra models only with the mods they draw from.

### 7.2 Publish cleanup (2026-11-07 evening; built, NOT installed: the game was running)

Owner request: mods.toml, license notice, pack-held defaults, foreign tag namespaces, Create / IE / Fruits Delight code
out of Common.

| Change | Where now | Guard |
|---|---|---|
| `SequencedAssemblyRecipeMixin` (gear NBT through sequenced assembly; `CEStash` key kept) | Engineering `mixin/` | `EngineeringMixinPlugin` `create` + range `[0.5.1.i,0.5.2)` |
| `CreateTeaPipeIconMixin` (Builder's Tea pipe Haste icon) | Engineering `mixin/` | same |
| `FluidTagInputMixin` (IE fluid input by id, subset NBT match) | Engineering `mixin/` | `immersiveengineering` + range `[1.19.2-9.2.4-170,1.19.2-9.3)` |
| `DrinkFluidEffects` and its JEI half `JeiFluidHelperTooltipMixin` (was Compat's; Compat would otherwise need Engineering) | Engineering `compat/`, `mixin/` (client) | listener registered only on the client with Create; `lines()` returns nothing without Create; mixin gated on `jei` `[11.8.1.1034,11.9)`; `jei` optional in mods.toml |
| `FruitsDelightEffectFuncMixin` (effect remap in jam / jelly lines) | Delight `mixin/fruitsdelight/` | the `fruitsdelight.` compartment (Fruits Delight 0.5.11 only) |

Engineering's plugin gained a `MOD_RANGE` (create, IE, JEI), so its older Create and IE mixins are now range-gated too.
Common's `mods.toml` dropped `create`, `immersiveengineering`, `fruitsdelight` and gained `immersive_portals`,
`heartstone` (optional, for the two crash fixes).

**Effects without the IE fix**: IE reads `tag` and `nbt` natively with an exact NBT match (checked in IE 9.2's
`FluidTagInput.deserialize`). Every `ieBottling` caller passes a tag, and Brewing's potion inputs (Lingerification, the
mixer generator, `potion_bottling.js`, `tipped_arrows.js`) carry exactly `{Bottle, Potion}`, what Create writes on a
potion fluid, so they match without Engineering. Only potion fluids with extra keys (CustomPotionColor) need the subset
match. NCH's `ieBottling` note says to use tags in jars that do not require Engineering.

**Tags**: `creativeengineering:bark` and `creativeengineering:metal_dusts` became `creativecommon:bark` /
`creativecommon:metal_dusts` (the dict is Common's; every user goes through `i.any_bark` / `i.t_metal_dusts`).
The six empty `create:scrap_*` placeholders were dropped: those tags belong to Create Recycle
(`create_crush_everything`), which defines them under `create:`, so they cannot move. They existed only because a required
`#tag` entry fails its whole target tag; Compat's `item_physics_tags.js` and Engineering's `sawdust.js` now add them
only with Create Recycle. Not touched: Compat writes `creativeengineering:burn/to_ash` and similar physics tags (Compat's
own cleanup).

**License**: Common's `NOTICE-thirdparty-various.txt` keeps Immersive Weathering only; Equipment's new `NOTICE-thirdparty-MIT.txt`
(Detail Armor Bar, MIT) ships as `creativeequipment-NOTICE-thirdparty-MIT.txt`; `ArmorOverflowRenderer` points to it.

**Pack-held values**: `config/creativecommon-common.toml` already holds the full `[mod_names]` list and the three removed
effects; on a deploy the `[effects]` values come over from `configs_edited/root_config/creativebrewing-common.toml`
(`ConfigRename.mergeSections`). Common's `configs_edited/` copy was refreshed from `config/` on 2026-11-07 (r1107cc; no value differed). Creative Tips loads from `kubejs/assets/` (always on, as the required built-in pack was).

Tests: SQ-CPP-01 to -05 PASS (scratch r1107cc 2026-11-07); CQ-CPP-01 open (client). Rows SQ-CGE-01 to -06, CQ-CGE-01 (gravity for falling blocks, items, TNT; SQ-CCO-08 rerun) queued 2026-11-08, with the re-run list for the 2026-11-08 00:01 jar. Scratch r1108cge 2026-11-08 ([scratch_run_2026-11-08.md](testing/scratch_run_2026-11-08.md) 1) on Common 00:49: SQ-CGE-01 to -06 PASS after two Common fixes (Create Gravity's biome list built at server start; the entity correction moved before the move), SQ-CPP-01 to -05 PASS again, SQ-CMV-04 PASS; also fixed: `JarStartupScripts` runs Compat's script pack before `startup_scripts/` (the pack's `jei/order.js` needs Compat's `global.jeiOrder`). CQ-CGE-01 and the client re-run rows open.

Found and fixed in r1107cc (Common, `neoCraftHelper.js`): `setCookTime` now writes a JSON number (a JS number made
Rhino write "400.0" and Farmer's Delight dropped the 22 Fruits Delight jelly recipes); a top-level `{ id }` / `{ mod }`
filter of an absent mod joins the absent-mods summary instead of logging an ERROR; `ieMetalPress` is skipped and counted
without KubeJS Immersive Engineering (the only IE helper on its recipe schema).

### 7.3 Common-alone client boot fixes (2026-11-07 20:00; built and installed in this instance)

Found by the owner's client boot of Common alone (CEPortTesting: Forge, Architectury, KubeJS, Rhino, Common 19:37).

| Finding in `latest.log` | Fix |
|---|---|
| `Using missing texture, file creativecommon:textures/mob_effect/magic_resistance.png not found`: the effect is Common's since plan_cb_polish (2026-11-06), the icon stayed in Brewing under `creativebrewing:` (so the pack showed it missing too, unreleased) | icon moved to Common `assets/creativecommon/textures/mob_effect/`; recolored purple (hue shift of the vanilla Resistance icon, owner 2026-11-07 20:25) |
| `[damage-tags] ... includes '#creativedelight:stoves' ... never match` on every boot without Creative Delight | `DamageTagSnapshot` skips the undefined-include WARN when the include's namespace is a mod that is not loaded (a typo in a loaded namespace still warns) |
| Potion, splash, lingering and tipped arrow names of Magic Resistance (3 variants each, 12 keys) blank when Common runs alone: they sat in the pack's `kubejs/assets/kubejs/lang/` | moved to Common's `lang/en_us.json` (removed from the pack file) |
| `fluid_jei.js`: `ReferenceError: "JEIEvents" is not defined` | wrapped in `Platform.isLoaded('jei')`; `global.jeiOrder` read inside the event, empty skip list without Compat |
| `jei_order.js`: Compat's `PackIngredientOrder` class not found | the JEI order moved to Creative Compat: helper `global.jeiOrder` to `kubejs_startup/creativecompat/func/jei_order.js`, client handoff to `kubejs/assets/creativecompat/kubejs/jei_order.js` (also guarded on JEI, optional for Compat); the order tags are now `creativecompat:jei/...` (`TAG_ROOT`, `PackIngredientOrder.TAG_NAMESPACE`) |

Client notes (owner, Common + all optionals, 20:24): panorama, creative menu, toasts, recipe book, Quark, Immersive Weathering, Nock Enough Arrows, Heartstone and Immersive Portals as expected; no JEI order (correct, it is Compat's). Lag after a resource reload: server 361 ticks behind at 20:23:31 on a world save, view distance 32 with Immersive Portals' 7 alternate dimensions (settled at 16); not Common. Rotating-model item underline (Legendary Tooltips) is the old Compat bug, not Common.

Not changed: `item_dict` WARN `incomplete -> kubejs:incomplete_item` (pack-registered item); the 66 `[mod names] ... not
loaded` WARNs came from that instance's old config holding the pack's `overrides` list (delete the file to get the empty
default). Tests SQ-CCB-01, CQ-CCB-01 to -03 ([scratch_queue.md](testing/scratch_queue.md)).

### 7.4 Low gravity for every falling entity, End oxygen, startup script phase (2026-11-08; built, NOT installed)

Owner request 2026-11-08 after scratch r1108cge: low gravity for falling blocks, items, TNT, arrows and other
projectiles, boats, minecarts and every mob, with no network cost, the dimension limit on all of it, the End's oxygen
rule working, a cleaner script load order, and the physics tags named for Compat. Owner answers: client mirror, keep
low oxygen in the End, per-file header.

- **Mobs**: Create: Gravity already lowers the gravity attribute of every living entity in a gravity biome except
  `#create_gravity:low_gravity_blacklist` (= `#forge:bosses`: Ender Dragon, Wither), elytra flyers, creative and
  spectator players and anyone in Create diving boots (`#create_gravity:anti_low_gravity_boots`); falling mobs also
  take less fall damage (fall distance x0.925 a tick). The attribute is synced, so players predict it themselves.
  Common keeps its dimension limit on that handler (`CreateGravityDimensionMixin`).
- **Everything else** (`gravity/LowGravity` + `LowGravity*Mixin`): these entities add a fixed pull per tick; Common
  scales that constant by `1 + factor` (0.2 at -0.8, 0 at -1): items, falling blocks, TNT, minecarts, Shulker bullets
  (0.04), XP orbs and fishing bobbers (0.03), arrows / tridents (0.05), llama spit (0.06), thrown projectiles via
  `getGravity()` (snowball, egg, pearl, potions, bottle o' enchanting, modded subclasses), boats out of water (in water
  that pull balances buoyancy). With ItemPhysic installed, items take their pull from ItemPhysic's own -0.04 in
  `ItemPhysicServer.updatePre` instead of vanilla's: `LowGravityItemPhysicMixin` scales that one (applies only with
  ItemPhysic and Create Gravity both loaded; version range `[1.6.7,1.7)`). Modded entities with their own gravity code (Create's potato cannon projectile and
  so on) are not covered.
- **Network (client mirror)**: the scaling runs on server and client alike. The client predicts items, blocks, TNT,
  orbs, arrows and throwables itself and moves a boat its player steers, so both sides compute the same path and only
  vanilla's normal position updates are sent. The first version (2026-11-08 00:49) set `hurtMarked` and sent one motion
  packet per airborne entity per tick; it is gone. Server lookup = the mod's `ServerConfig.getBiomeConfig`; client
  lookup = Common's own parse of the mod's `biome_configs` strings (`CreateGravityConfigAccessor`; Forge syncs SERVER
  configs at login; the mod's parsed list is always empty on a dedicated client because each entry needs the server).
- **Dimension limit**: `LowGravity.dimensionAllowed` (perf `gravityLimitDimensions` / `gravityDimensions`, default
  the End only) gates the lookup for every type and the living handler. The client uses its own copy of Common's
  config (the pack ships the same file to both sides).
- **End oxygen (kept)**: Create: Gravity's default makes every gravity biome low-oxygen (factor 100): a survival player
  with no air supply loses about 1 air a second (measured: air out after about 290 s, 4 air at a time; not the 4 s
  the config comment implies) and then takes 6 damage every 3 s; a backtank with air stops it
  (`full_set` false: no helmet needed), Respiration slows it. Was effectively off on dedicated servers until a
  `/reload` (fixed at 00:49). The mod's tags listed only Create's copper and netherite pieces: Compat
  `tags/misctags.js` adds every `#create:pressurized_air_sources` backtank (Create Goggles' five) to
  `create_gravity:backtanks` and the two goggle diving helmets to `create_gravity:diving_helmets`.
- **Startup script phase** (concern 5 of r1108cge): `JarStartupScripts` runs Creative Common's pack first, then from
  each other jar only the files headed `// phase: helpers` (one `<modid>_helpers` pack per jar, own scope), then the
  pack's `startup_scripts/`, then the rest of every jar. Compat's `func/jei_order.js` carries the header. Replaces the
  hard-coded list (`creativecommon`, `creativecompat`) of 00:23.
- **Tags**: Compat's item physics tags `creativeengineering:physics/{floats,sinks,fireproof,lavaproof}` and
  `creativeengineering:burn/*` are now `creativecompat:` (Java `ItemPhysicSplit.TAG_NS`, both scripts, the audit tool).
  The old `data/creativeengineering/tags/items/physics/` files were removed from Compat 15:08, so the old tags are empty.

Install: Common and Compat together (the new Common with the old Compat crashes the full pack's startup: the old
Compat's `jei_order.js` has no header). Common and Compat rebuilt 15:08 (Common also logs one client line per parse of the synced list,
`[low gravity] client biome list: ...`, for the join test). Tests: run r1108cge2 ([scratch_run_2026-11-08.md](testing/scratch_run_2026-11-08.md) 2): SQ-CGE-07 to -16 with -10b PASS;
CQ-CGE-02a, -02, -02b, -04, -04b PASS on the jars before the ItemPhysic patch, -02c not judged; CQ-CGE-03a to -03c not run (owner), run by `tools/scratch_query/cge2_run.sh` (server: four boots; client: single player and a join
to the scratch server), judged by `cge_judge.py`; queue section "SQ-CGE part 2".

### 7.5 Re-check after 1.1.0 (2026-11-13)

Tag `creativecommon-1.1.0` is at 40ed32be (2026-11-08). Changes in Common since then:

| Change | Tested |
|---|---|
| Swap lists: permanent aliases for `replaces`, `capabilities` key renames (`SwapCapabilityKeysMixin` on `CapabilityDispatcher`), load report (`SwapReport`, chunk and ItemStack mixins) | SQ-CDNS-01 to -04, -06 PASS (scratch r1110) |
| Chat key typed into the chat box (MC-122477): `ChatOpenArmMixin`, `ChatOpenCharDropMixin` | FX-50 / CQ-CHK-01 PASS (owner, 2026-11-13) |
| `creativecommon:trimmable_armor` (was `creativeengineering:`), filled by Equipment | with SQ-CDNS-08 tag check |
| NCH: `hhStomping`, `vineryFermenting`, `vineryPressing`, `ieFertilizer`, `createFluidTag`, Create crushing / milling `pools` (without Engineering Create ignores `result_pools` and gives every output); recipe types of Delight / Equipment / Labels named directly | the pack's recipe load |
| Tag scripts `plantabletags.js` (from the plantable work) and `smokable_tags.js` (from Engineering); dict moves (`plan_cd_namespace.md`, flesh items) | the pack's tag load |

All new mixins target vanilla or Forge, so no `MOD_RANGE` row is needed; the chat mixins only apply when the client
classes load. `:creativeCommon:build --offline` clean 2026-11-13 22:35.

Before the upload:
1. Done 2026-11-13 (owner): `mod_version` 1.2.0 (new library API). `creativecommon-1.2.0.jar` built 22:40 in
   build/libs, NOT installed (`mods/` still holds the 2026-11-12 `creativecommon-1.1.0.jar`; `installToMods` displaces it).
   On install, update the `mod_sides.md` row; `pack/MODLIST.md` and `pack/manifest.json` follow at the next deploy.
2. Done 2026-11-13: project page ([project_pages/creative_common.md](project_pages/creative_common.md)) gained the swap
   list additions (permanent aliases, capability keys, load report), the plantable tags and the chat-key fix (MC-122477).
3. Deferred (owner 2026-11-13: no changelog yet): CurseForge changelog for the file. Common has no own release notes; the
   pack's `unreleased.md` holds the chat bullet.
4. Boot check of the 1.2.0 jar: queued as SQ-CC12-01 to -03 (Common alone, Common + Compat + Calendar, full pack); the 2026-11-10 runs predate the chat
   mixins and the 2026-11-13 dict.
   **Run r1114 2026-11-14 ([scratch_run_2026-11-14.md](testing/scratch_run_2026-11-14.md) 8, phase 4; build/libs jar of 2026-11-13 23:12 in scratch srv only): SQ-CC12-01, -02, -03
   PASS: built, scratch server PASSED.** No mixin or class error from our jars, 22 of 22 startup files, 0 startup
   ERROR, swap summary with 739 permanent aliases and 4 capability keys on the full pack, "every listed id resolved" on
   stop; Common 1.2.0 also ran under every other r1114 phase without a Common error.
5. Commit, then tag `creativecommon-1.2.0` on that commit.
