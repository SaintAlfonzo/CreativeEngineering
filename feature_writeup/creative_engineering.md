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
| 6 title panorama PNGs (`assets/minecraft/.../title/background/`) | own: the owner's in-game capture (owner 2026-11-05) | no (out 2026-11-07; back and out again 2026-11-08: the capture is a flat sky with no terrain, so it looked blank; vanilla by default); the pack keeps its `kubejs/assets/` copy | OK |
| `credit.txt` (Numismatics by Layers of Railways, LGPL-3.0; owner 2026-11-05: https://www.curseforge.com/minecraft/mc-mods/numismatics) | the file it covers is not traced (no Numismatics jar to compare against) | yes | OK: kept, URL added |
| `mods.toml` `GPL-3.0-only`, `creativecommon-LICENSE.txt` | own | yes | OK |
| Farmer's Structures donor (ARR) | read at runtime from `farmers_upstream/` | no | OK |

Fixes: TODO `licence/common #1` (tint sheets, closed: vanilla edits accepted), `#2` (notices, `credit.txt`), `#3` (panorama,
closed: the owner's own). No licence row open for Common.

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
behaves as vanilla; the pack keeps its behaviour through `config/creativecommon-common.toml` (and the `configs_edited/root_config/`
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
library behaviour, not pack taste. Pack-flavoured defaults still in place and not in this change: `[mod_names] overrides`
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
   `kubejs/assets/creative_tips/`. Still pack-flavoured, kept: `[effects] forceHudIcon` / `forceHudIconItems` (they
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

Owner request: mods.toml, licence notice, pack-held defaults, foreign tag namespaces, Create / IE / Fruits Delight code
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

**Licence**: Common's `NOTICE-thirdparty.txt` keeps Immersive Weathering only; Equipment's new `NOTICE-thirdparty.txt`
(Detail Armor Bar, MIT) ships as `creativeequipment-NOTICE-thirdparty.txt`; `ArmorOverflowRenderer` points to it.

**Pack-held values**: `config/creativecommon-common.toml` already holds the full `[mod_names]` list and the three removed
effects; on a deploy the `[effects]` values come over from `configs_edited/root_config/creativebrewing-common.toml`
(`ConfigRename.mergeSections`). Common's `configs_edited/` copy was refreshed from `config/` on 2026-11-07 (r1107cc; no value differed). Creative Tips loads from `kubejs/assets/` (always on, as the required built-in pack was).

Tests: SQ-CPP-01 to -05 PASS (scratch r1107cc 2026-11-07); CQ-CPP-01 open (client). Rows SQ-CGE-01 to -06, CQ-CGE-01 (gravity for falling blocks, items, TNT; SQ-CCO-08 rerun) queued 2026-11-08, with the re-run list for the 11-08 00:01 jar. Scratch r1108cge 2026-11-08 ([scratch_run_2026-11-08.md](testing/scratch_run_2026-11-08.md) 1) on Common 00:49: SQ-CGE-01 to -06 PASS after two Common fixes (Create Gravity's biome list built at server start; the entity correction moved before the move), SQ-CPP-01 to -05 PASS again, SQ-CMV-04 PASS; also fixed: `JarStartupScripts` runs Compat's script pack before `startup_scripts/` (the pack's `jei/order.js` needs Compat's `global.jeiOrder`). CQ-CGE-01 and the client re-run rows open.

Found and fixed in r1107cc (Common, `neoCraftHelper.js`): `setCookTime` now writes a JSON number (a JS number made
Rhino write "400.0" and Farmer's Delight dropped the 22 Fruits Delight jelly recipes); a top-level `{ id }` / `{ mod }`
filter of an absent mod joins the absent-mods summary instead of logging an ERROR; `ieMetalPress` is skipped and counted
without KubeJS Immersive Engineering (the only IE helper on its recipe schema).

### 7.3 Common-alone client boot fixes (2026-11-07 20:00; built and installed in this instance)

Found by the owner's client boot of Common alone (CEPortTesting: Forge, Architectury, KubeJS, Rhino, Common 19:37).

| Finding in `latest.log` | Fix |
|---|---|
| `Using missing texture, file creativecommon:textures/mob_effect/magic_resistance.png not found`: the effect is Common's since plan_cb_polish (11-06), the icon stayed in Brewing under `creativebrewing:` (so the pack showed it missing too, unreleased) | icon moved to Common `assets/creativecommon/textures/mob_effect/`; recolored purple (hue shift of the vanilla Resistance icon, owner 2026-11-07 20:25) |
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
  vanilla's normal position updates are sent. The first version (11-08 00:49) set `hurtMarked` and sent one motion
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
