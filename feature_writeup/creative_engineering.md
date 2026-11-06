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

Same behaviour as before the carve. Engineering's registry ids, tags, lang keys and channels keep the
`creativeengineering` namespace. Common's own data moved to `creativecommon` the same day (owner, 2026-11-04,
[creative_split.md](creative_split.md) 7.14): `CreativeCommon.NS` is now `creativecommon`, which covers the damage tags,
WTHIT plugin and line ids, JEI uids, the `jei/` and `wthit/` tags, the lang keys, the `kubejs/assets/creativecommon/`
changelog and JEI world trees, the JFR events and the mixin member prefix.

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
  description. Same behaviour otherwise: no changelog bullet (dev-only move, as for the earlier carves).
- Event and listener order shifts a little: Engineering's registrations run in its constructor after Common and Compat.
- Engineering's mixins are now gated on their mods; with every target mod in the pack nothing changes.
- `instance.json` (ATLauncher) still names `creativeengineering-1.1.01.jar`; update it with ATLauncher closed.
- Not updated: `tools/remainder_buffer_test/` and `tools/shield_test/` keep stub sources in the old package (both
  were already stale); prose in tool comments and in history sections of the docs still says patcherMod.
- `tools/build_deploy.py`: both jars in `IN_TREE_MODS`; the climate reader check reads Common's `WorldgenFiles`; the
  `[worldgen.debug]` release check now reads `creativeworldgen-common.toml` (it still read the CE file, stale since wave 5).

## 5. Tests

Scratch SQ-CEN-01 to -08 ([scratch_queue.md](testing/scratch_queue.md)); SQ-CMG and SQ-CEQ run on the same jars.
In game: CQ-CEN-01 (WTHIT Run Speed, leaf piles, camo, sinking, JEI dragon breathing, names), pinned with the other
client rows (creative_split.md 7.8).

**Install** (game closed; all at once, the jars only work together): `./gradlew :creativeCommon:installToMods
:creativeEngineering:installToMods :creativeCompat:installToMods :creativeCalendar:installToMods
:creativeDevtools:installToMods :creativeDelight:installToMods :creativeBrewing:installToMods
:creativeWorldgen:installToMods :creativeMagic:installToMods :creativeEquipment:installToMods --offline`. Common's
install moves `creativeengineering-1.1.09.jar` to `development/creativeCommon/jar_archive/displaced/`. Revert: remove the
two new jars, put 1.1.09 back, and reinstall the other jars from their `jar_archive/displaced/` folders.

## 6. History

- 2026-11-04: carved and swapped (wave 8). Owner: D3 (liquid XP bridge) goes to Engineering; the carve and the swap in
  one step, the project folder renamed too. Built: all ten Creative jars; not installed, not booted.

## 7. Licence audit: Creative Common (2026-11-04, plan_cb_publish.md Step 2)

Audit of `creativecommon-1.1.0.jar` (23:34). Summary row in [creative_brewing.md](creative_brewing.md) 9.2.

| Part | Source licence | In the jar | Verdict |
|---|---|---|---|
| Own Java (about 270 classes) and 70 mixins | own, GPLv3 | yes | OK |
| `IwBlockGrowthHandlerMixin` fallback (about 10 lines of IW 1.2.10's body, the only `@Overwrite`) | Immersive Weathering, LGPLv3 | yes | OK; attribution added 2026-11-05 (`NOTICE-thirdparty.txt`, source comment) |
| `ArmorOverflowRenderer.row0Y` / `drawRow` (from Detail Armor Bar 2.6.4 `ArmorBarRenderer#render`) | MIT | yes | OK; MIT notice added 2026-11-05 (`NOTICE-thirdparty.txt`, source comment) |
| commonmark-java 0.21.0, shaded to `...creativecommon.shadow.commonmark` | BSD-2-Clause | yes, with `NOTICE-commonmark.txt` | fixed 2026-11-05 (names Creative Common and the `creativecommon` package) |
| `kubejs_startup/creativecommon/_lib` (12 dicts) and `func/` helpers, `kubejs/_lib` scripts, data, lang, plugin json | own | yes | OK |
| 12 `textures/gui/*_tint.png` (27x9) | **Mojang-derived**: recolours of the vanilla armor icon | yes | OK: vanilla art and edits of it are accepted everywhere (owner 2026-11-05, [creative_brewing.md](creative_brewing.md) 9.5) |
| 6 title panorama PNGs (`assets/minecraft/.../title/background/`) | own: the owner's in-game capture (owner 2026-11-05) | yes | OK |
| `credit.txt` (Numismatics by Layers of Railways, LGPL-3.0; owner 2026-11-05: https://www.curseforge.com/minecraft/mc-mods/numismatics) | the file it covers is not traced (no Numismatics jar to compare against) | yes | OK: kept, URL added |
| `mods.toml` `GPL-3.0-only`, `creativecommon-LICENSE.txt` | own | yes | OK |
| Farmer's Structures donor (ARR) | read at runtime from `farmers_upstream/` | no | OK |

Fixes: TODO `licence/common #1` (tint sheets, closed: vanilla edits accepted), `#2` (notices, `credit.txt`), `#3` (panorama,
closed: the owner's own). No licence row open for Common.

### 7.1 Publishing readiness (2026-11-05, TODO `creativecommon/publish #1`)

Owner 2026-11-05: Creative Common is used only inside the pack for now, so the pack's defaults stay; a library is fine on
CurseForge. Built and installed 2026-11-05 23:40 (`creativecommon-1.1.0.jar`).

**Mixin version gates.** `perf/CEMixinPlugin` `MOD_RANGE` gives each of the 19 mods Common mixes into a tested range;
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

**For wide distribution later (not now, owner 2026-11-05)**: switch the pack-only defaults to vanilla behaviour and set
them in the pack's own config (`configs_edited/`): `[advancements] enabled` (false drops every advancement),
`[toasts] advancement / tutorial`, the title panorama (move to the pack folder or a config switch), JEI `packOrder`,
the creative menu disable, `[wandering_trader]` timing and offers, the swap triggers. TODO `creativecommon/publish #2`.

**Still open before an upload**: standalone boot (SQ-CMV-02), the out-of-range check (SQ-CMV-03), commit, push and tag the
source (creative_brewing.md 9.4 R6), project page.
