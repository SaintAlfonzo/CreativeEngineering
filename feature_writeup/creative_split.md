# Creative split: audit plan for breaking patcherMod into high-level jars

Written 2026-11-01. **Status: signed off 2026-11-01 (7.6 holds every decision and overrides older rows); execution
started on branch `creative-split`. Waves 1 to 8 carved: Magic and Equipment 2026-11-04 (7.11, 7.12, installed, not
booted); wave 8, Engineering with the `creativecommon` swap, 2026-11-04 (7.13, built, NOT installed). patcherMod is now
Creative Common (`development/creativeCommon`).** Owner, 2026-11-01: plan an audit of Creative
Engineering with the intent of splitting content into its own high-level jars; the core systems go into a mod called
Creative Common, beside Creative Engineering, Creative Equipment, Creative Worldgen, Creative Compat (misc tweaks for
various mods) and Creative Calendar, plus any other large content categories worth splitting out.

Written for: the owner, and the agents that will run the audit sweeps. Related: [creative_brewing.md](creative_brewing.md)
(the move rules this plan reuses, 2.4 and 4.1 to 4.3), [creative_calendar.md](creative_calendar.md) (reserved list),
[creative_delight.md](creative_delight.md) 29 (food routing, script trees, Creative Cultivation in 29.5).

`PM` = `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`.

## 0. At a glance

- The audit is **read-only**. It produces an inventory (section 7) that puts every patcherMod class, mixin, config
  section, resource folder and pack-folder script into exactly one target jar, lists the cross-jar calls, and fixes
  the carve order. Execution plans are written per jar after the owner signs off on the audit.
- **Content is carved out first. Whatever is left in patcherMod becomes Creative Common** (owner, 2026-11-01).
- **Scripts follow the Creative Delight and Creative Brewing approach** (section 3.9; owner, 2026-11-01).
- **Jars:**
  - Carved in this split: Engineering, Equipment, Worldgen, Compat, Calendar and Devtools.
  - Food and brewing rows go to the existing bundles: Creative Delight and Creative Brewing.
  - Reserved for later: **Creative Magic** and **Creative Computers** (2.3).
- **Mod ids have no underscore** (owner, 2026-11-01): `creativecommon`, `creativeengineering`, `creativeequipment`,
  `creativeworldgen`, `creativecompat`, `creativecalendar`, `creativedevtools`, and later `creativemagic` and
  `creativecomputers`. The same goes for package names, config file names and script-pack folders. The existing
  `creative_delight` and `creative_brewing` ids are Q2.

## 1. Starting point (measured 2026-11-01)

patcherMod: 629 Java files. 289 mixins in one `creativeengineering.mixins.json`. `CEConfig.java` is 3639 lines with
about 60 top-level sections, read from 110 files. `CreativeEngineering.java` is 576 lines and does every registration.
21 classes hold a `DeferredRegister`.

| Package (lines) | Package (lines) | Package (lines) |
|---|---|---|
| worldgen 12844 | compat 12480 | mixin 12554 |
| devtools 1954 | oven 1713 | shield 1678 |
| food 1175 | perf 1171 | time 1132 |
| surface 1029 | seasons 974 | potion 944 (to CB) |
| changelog 928 | block 869 | lunchbox 740 |
| itemphysic 529 | damage 564 | crops 524 |
| stepstool 506 | mobs 430 | fan 426 |
| cooking 386 | backpackskin 380 | fluidbrewing 340 (to CB) |
| gear 330 | trader 309 | shrapnel 246 |
| util 212 | item 210 | dimension 181 |
| bucket 139 | excavation 133 | campfire 126 |
| mooshroom 115 | sawmill 113 | fluid 93, loot 77, forgivingvoid 64 |

Pack folders: `kubejs/startup_scripts` (`_lib` dicts are 18k lines), `server_scripts` (engineering 2001, worldgen
3412, loot 1911, tags 1745, metal 966, `_lib` dumps 4037), `client_scripts` 1568, `kubejs/data/` (about 45 structure
namespaces) and `kubejs/assets/`. Shared helpers live in patcherMod's jar (`kubejs_startup/creativeengineering/func/`).

## 2. Target jars (draft scope; the audit confirms or moves each row)

### 2.1 Carved out in this split

| Jar (mod id) | Holds (from package and mixin names) |
|---|---|
| **Creative Engineering** (`creativeengineering`) | `fan/`, `sawmill/`, `block/` (coils, plates), `item/` components, `excavation/`, `bucket/`, `fluid/`, `surface/` (BlockRunner, sinking, camo), `shrapnel/`; Create, IE, PneumaticCraft and Dreams and Desires machine mixins that are not potion or JEI; `server_scripts/engineering`, `metal`, `wood`; startup `items/` components, coils, plates, dust. Also carries the Creative Computers rows until that mod is planned (2.3). |
| **Creative Equipment** (`creativeequipment`) | `gear/`, `shield/`, `damage/`, `backpackskin/`, `stepstool/`, CETiers and tool tuning, armor caps and enchantability, dragon and echo gear, archery and bullets (Nock Enough Arrows mixins), air brake, camera bags; `server_scripts/equipment`. Gear effects go to CB (creative_brewing.md 4.2). Also carries the Creative Magic rows until that mod is planned (2.3). |
| **Creative Worldgen** (`creativeworldgen`) | `worldgen/` (structures, locate, earth and water fill, terrain adaptation, sky islands, spawn tuning, bee nests), `dimension/`, `loot/` and `server_scripts/loot`, `forgivingvoid/`, the ChunkGenerator, Structure, Jigsaw, Beardifier, DistantClip and leaf decay mixins; `server_scripts/worldgen`; the structure datapacks in `kubejs/data/`. **Fauna features too** (owner, 2026-11-01): `mobs/`, mob caps and spawn areas, MCA villager pathing, bee idle, raid glowing, `removed_mobs.js`. |
| **Creative Compat** (`creativecompat`) | Third-party tweaks with no wider system: Quark, Supplementaries, Handcrafted, FTB, Lightman's Currency, Polymorph, Sophisticated, Pretty Pipes, TrashSlot, MCA combs, Breezy, Botania, `itemphysic/`, storage tweaks (sack bundle, trinket case, nightstand). |
| **Creative Calendar** (`creativecalendar`) | As reserved in [creative_calendar.md](creative_calendar.md) 2: `time/`, `seasons/`, Better Days, Serene Seasons, SeasonHUD, Xaero's time, crop seasons, weather by biome, the wine clock. CD and CB depend on it. |
| **Creative Devtools** (`creativedevtools`) | `devtools/`, `/cdevgen`, `/dumpall` and the `server_scripts/_lib` dump scripts, `PerfCommand` and JFR events, the block-state and random probes. Kept in dev `mods/` only and left out of `build_deploy.py` player payloads (Q6). |

### 2.2 Into the existing bundles

| Bundle | Takes from patcherMod and the pack |
|---|---|
| **Creative Delight** | Kitchen and food Java (`oven/`, `lunchbox/`, `cooking/`, `food/`, `crops/`, `campfire/`), routed by the standing rule (memory `creative-delight-routing`). **Animal husbandry** (owner, 2026-11-01): the trough code (FeedingTroughModule, TemptGoalTroughRetry, TroughRetryAccess), and `babyMobs`, `moreMooblooms`, `incubationAdditions` as CD members. The audit checks whether `mooshroom/` (breeding and walk target) goes with More Mooblooms or with Worldgen fauna. |
| **Creative Brewing** | Its existing move plan (creative_brewing.md 4.2 phases 1 to 3), run as one of the carve-outs. |

### 2.3 Reserved for later (marked, not carved now)

These are tagged in the inventory now. Each file gets a header line "Reserved for Creative Magic" or "Reserved for
Creative Computers", as WineTime was marked for Calendar. The rows ride in their interim jar so they are not carved
twice, and Common never holds them.

| Future mod | Holds | Interim jar |
|---|---|---|
| **Creative Magic** (`creativemagic`) | Enchantment mixins (Cycle, Recovery, Stockpile), enchanting power ([enchant_power.md](enchant_power.md)), `server_scripts/magic` minus what CB takes, [recipes_magic.md](recipes_magic.md) rows, `startup_scripts/magic`, the Botania tweaks. Potions and effects stay CB's. | Equipment |
| **Creative Computers** (`creativecomputers`) | The high-level circuits, ComputerCraft, AE2 (recipes and mixins that are not tooltips), and the control-engineering content: `server_scripts/engineering/computing.js` and the circuit rows in `components.js` and `mechanisms.js`; the audit finds the rest. | Engineering |
| **Creative Economy** (`creativeeconomy`, name not final) | Trading and loot chests (owner, 2026-11-07): the wandering trader spawn and trade tweaks (`trader/WanderingTraderTweaks`, `WanderingTraderSpawnerMixin`, `WanderingTraderTradesMixin`, `[wandering_trader]`, marked); the loot-chest rows the plan for that mod lists. `trader/TradeListings` and `TradeSync` stay Common APIs. | Common |

### 2.4 Creative Common (what remains)

After every carve-out, patcherMod should hold only the following. Anything else still there is a carve the audit
missed.

- The core: config framework, `util/`, the startup-script loader and KubeJS mixins (`compat/kubejs`, the KubeJS
  mixins), the shared helpers (NCH, `tag_helper`, `compost`), the shared `GatedMixinPlugin`, the `changelog/` viewer,
  and the advancement drop (ServerAdvancementManager).
- The shared APIs that other jars call: `shield/EffectTickContext`, `trader/TradeListings`, `FluidTagInput`,
  `DrinkFluidEffects`.
- Engine fixes and perf: ghost blocks, null-state packets, section updates, the GL debug sync, thread-safe random,
  `perf/` and the perf mixins.
- **Interface: moved to Creative Compat** (owner, 2026-11-07; was Common's by the 2026-11-01 note; section 7.19). JEI,
  WTHIT, tooltip, toast and GUI title code and their mixins are Compat's; the Detail Armor Bar row is Equipment's. Common keeps
  the interface pieces that are effect rules or perf: `EffectRules` and the use-item icon marker, `MobEffectCategoryMixin`,
  `CompoundEffects` (the JEED, tooltip and HUD-icon render halves are Compat's), the creative menu disable (perf), the
  `changelog/` viewer, loading tips, `[mod_names]`, and the `client_scripts` helpers (`jei_order.js`).
- Kept in Common although they target one mod: Create: Gravity's dimension skip (perf) and IE `FluidTagInputMixin`
  (shared API: Creative Brewing's potion bottling recipes need it) (owner, 2026-11-07).


## 3. Rules the audit applies to every row

Taken from creative_brewing.md 4.1 and creative_calendar.md 3, extended to several jars:

1. **Ids stay.** Every registered id keeps the `creativeengineering:` namespace whichever jar registers it (a
   `DeferredRegister` for that namespace). No conversions (CLAUDE.md). Lang keys keep their names.
   Exception (owner, 2026-11-04, 7.14): Creative Common's own data (nothing registered) moved to `creativecommon`.
2. **Dependencies point down.** Common depends on nothing of ours. Content jars depend on Common, and Calendar sits
   under CD and CB. A content-to-content call is either moved into Common as an API, or made a soft dependency
   through `GatedMixinPlugin` / `ModList` checks. The audit lists every such edge.
3. **Config keys move with their feature** and keep their names and comments. Each jar gets its own
   `<modid>-common.toml`, and the edited values go to `config/` and `configs_edited/` in the same session.
4. **Helpers stay shared** in Common (NCH, `global.tags`, compost). Dict keys stay in one place (Q4).
5. **KubeJS scope:** each jar script pack has its own scope and only `global` crosses (memory
   `kubejs-jar-startup-scripts`). The audit greps the top-level names of every script that changes jar.
6. **Mixins** are SRG with `remap = false`, so a mixin moves unchanged apart from its package. Each jar has its own
   mixins json and gated plugin. Each mixin records which side it runs on.
7. **One owner per file.** Mixed files are split by row (as `tooltips.js` and `fluidtags.js` are for CB).
8. **Every new jar** gets a `mod_sides.md` row, a `build_deploy.py` entry and a `replaces_jars` line if it displaces
   a jar.
9. **Scripts follow the CD and CB layout** (creative_brewing.md 2.4, creative_delight.md 29):
   - Startup scripts: `development/<jar>/src/main/resources/kubejs_startup/<modid>/`.
   - Server scripts: `kubejs/data/<modid>/kubejs/<mod>/`.
   - Client scripts: `kubejs/assets/<modid>/kubejs/<mod>/`.
   - Each jar has a core file defining `global.<X>` with `on(member)` and `feature(id)` (for example `global.CEQ`;
     the names are set per jar in its execution doc) and a `<modid>-common.toml` with one switch per feature.
   - One folder per mod the rows touch. Each file is headed `// requires: <modid>`.
   - Recipes are written inside `global.<X>.recipes(...)`, so NCH writes `<modid>:<type>/<suffix>` ids. Recipe ids
     change with the move, as they did for CD; item ids do not.
10. **No underscore** in any new mod id, package, config file name or script-pack folder.
11. **Each jar's lang lives under its own namespace** (`assets/<modid>/lang/en_us.json`), keys unchanged. Forge 1.19.2
    serves all mod jars as one resource pack that returns only the first jar holding a path, so six jars each shipping
    `assets/creativeengineering/lang/en_us.json` meant five were ignored (the Seasoning, Canning and Feast Plating JEI
    titles went missing). Moved 2026-11-03 for Creative Delight (merged into its existing file), Brewing, Calendar,
    Compat and Worldgen. The same applies to any other asset path two of our jars share.

## 4. Audit method

Read-only. These sweeps are independent and can run in parallel, one agent per area. Every row gets one of these
targets:

- a carve jar: Engineering, Equipment, Worldgen, Compat, Calendar or Devtools;
- a bundle: CD or CB;
- a reservation: Magic or Computers (with its interim jar);
- Common;
- pack, for a pack resource that stays in the pack folder.

| Sweep | Covers | Output |
|---|---|---|
| A. Java packages | every non-mixin class in `PM` | class, lines, target, calls into other targets, registrations it owns |
| B. Mixins | all 289 entries in `creativeengineering.mixins.json` | mixin, target mod, side, target, the config keys it reads |
| C. Config | every `CEConfig` section and key, and every reader | section, target, reader files; keys read by two jars are flagged |
| D. Registration hub | `CreativeEngineering.java`, the 21 `DeferredRegister` holders, event subscribers, JEI and WTHIT plugin entry points | entry point, target |
| E. Resources | `PM/../resources` (assets, data, `waila_plugins.json`, `kubejs_startup/`), `kubejs/data`, `kubejs/assets` | path, target or "pack" |
| F. Pack scripts | `kubejs/startup_scripts`, `server_scripts`, `client_scripts`, and the `global.*` names each defines and reads | file (or row range), target or "pack", the globals that cross |
| G. External edges | CD and CB references into patcherMod (`compileOnly project(':patcherMod')` users, `PackTime.java`), the own jars going into CD (babyMobs, moreMooblooms, incubationAdditions), `tools/generators.json` output roots, `mod_sides.md` | edge, which new jar it must point at |

After the sweeps, a single pass merges them and adds:

1. The **dependency graph** between targets, with every upward or sideways edge listed and a fix for each (move to
   Common, interface, or soft dependency).
2. The **sizes per jar** (lines, mixins, config sections). If a jar comes out under about 1000 lines it is folded
   into a neighbour.
3. The **carve order**. Content leaves first, and Common is the remainder. Draft order, from the least coupled to the
   most:
   1. **Devtools:** nothing depends on it.
   2. **Compat.**
   3. **Calendar:** the plan already exists, and CD and CB then switch their wine and season edges to it.
   4. **The CB move plan and the CD-bound Java and members.**
   5. **Worldgen,** with fauna.
   6. **Equipment,** with the Magic rows.
   7. **Engineering,** with the Computers rows, and the id swap (Q1).
4. A **remainder check**: what is left in patcherMod is compared against 2.4, and every extra row is reported.

## 5. Deliverables

- Section 7 of this file: the merged inventory (one table per target), the edge list, the size table and the
  remainder check.
- One execution doc per carve (`creative_<name>.md`, in the shape of creative_brewing.md: framework, rules, phases,
  tests, install), written only after sign-off.
- Reservation docs `creative_magic.md` and `creative_computers.md`, in the shape of creative_calendar.md, holding the
  marked list from the audit.
- A TODO row per jar under the Creative Split family.

## 6. Open questions (owner)

1. **Mod id swap.** patcherMod keeps `creativeengineering` while the content is carved. At the last carve, the
   Engineering jar takes `creativeengineering` (and keeps `creativeengineering-common.toml` for its keys), and the
   remainder becomes `creativecommon` (package `com.SaintAlfonzo.creativecommon`, `creativecommon-common.toml`). In
   the same step, CD's and CB's mandatory dependency and the startup loader move to `creativecommon`. Recommended as
   one release step. Confirm.
2. **Existing underscored ids.** `creative_delight` and `creative_brewing` (mod ids, config files, `kubejs/data/creative_delight`, NCH recipe namespaces) break the no-underscore rule. Rename them to
   `creativedelight` and `creativebrewing` as part of the split, or leave them as they are? Item ids are not affected
   (members use their own namespaces); recipe ids and config file names change.
3. **Fixes and perf:** closed 2026-11-01: stay in Common.
4. **Dicts** (`startup_scripts/_lib`, 18k lines): closed 2026-11-01: into Common's jar `kubejs_startup`.
5. **perfBot and clientCapture:** closed 2026-11-01: nested in Creative Devtools.
6. **Devtools on the live server:** dev `mods/` only, or also on CreEngServer for `/dumpall` there?
8. **Release number: CLOSED 2026-11-02 (owner): the Creative Split deployment is 1.2.00.** The Creative Delight cutover (creative_delight.md `members #10`) goes in the next deployment unless a fix must be pushed first, so it ships before 1.2.00.
7. **Publishing:** which jars are meant to be published alone (that drives the licence audit, as creative_brewing.md 9
   did for CB)?

## 7. Inventory (filled by the audit)

Audit run 2026-11-01. The full per-row tables live in [split/](split/README.md) (one file per sweep A to G); this
section holds the merged result. Reservation headers are not written to source files (owner, 2026-11-01); the marked
lists go to the reservation docs instead.

### 7.1 Sizes per target

Java and mixins from sweeps A and B, config sections from C, script lines from F, resources from E. Common's Java
includes `CEConfig` (3639) and `CreativeEngineering` (576), which dissolve into per-jar files, so its real remainder
is about 9500 lines.

| Target | Java classes / lines | Mixins (common / client) | Config sections | Script lines | Resources (files / size) |
|---|---|---|---|---|---|
| Common | 71 / 13693 | 71 (29 / 42) | 9 (+ perf, wthit, jei at section level) | 23061 (18027 dicts, 3185 jar helpers, 1849 pack) | 77 / 4.8M |
| Worldgen | 57 / 13931 | 56 (56 / 0) | 9 | 5430 (+ 26796 loot json and md) | 750 / 8.9M |
| CD | 63 / 6536 | 24 (22 / 2) | 7 | 482 | 510 / 3.2M |
| Equipment | 36 / 3471 | 20 (20 / 0) + 3 Magic | 10 | 1217 + 146 Magic | 80 / 380K |
| Engineering | 45 / 3387 | 31 (27 / 4) | 3 | 4232 + 37 Computers | 440 / 2.4M |
| Devtools | 11 / 2429 | 0 | 1 | 4023 | 2 / 16K |
| Compat | 23 / 2350 | 41 (31 / 10) | 3 | 1231 | 1120 / 5.3M (950 paintings) |
| CB | 20 / 2315 | 25 (14 / 11) | 5 | 2191 | 242 / 890K |
| Calendar | 14 / 2191 | 18 (10 / 8) | 3 | 132 | lang keys only |
| pack / delete | 0 | 0 | 0 | 48 (+ 1890 stale todo txt) | 14 |
| **Total** | 340 / 50303 | 289 (212 / 77) | 50 sections, 394 keys | 42230 | about 5300 files |

No jar comes out under the 1000-line fold threshold; Calendar is the smallest (2191 Java lines, 18 mixins, 3
sections) and stays, since CD and CB depend on it. Devtools has no mixins and almost no resources; it is Java
commands plus the `_lib` dump scripts.

### 7.2 Dependency graph

Allowed edges: content -> Common, CD and CB -> Calendar. Every other edge found, with its fix. "API" means a small
interface or registration hook moved into Common; "soft" means a `ModList`-guarded call.

**Java (sweeps A, B, D)**

| From (target) | To (target) | Fix |
|---|---|---|
| `potion/MagicResistanceEffect` (CB) | `damage/DamageTags` (Equipment) | DamageTags becomes a Common API (the only class read by two content jars); the rest of `damage/` stays Equipment |
| `compat/StateLines` (Common) | `crops/CropGrowth.glob` (CD) | move `glob` into Common util |
| `compat/world/WorldInteractionJEI` (Common) | `CanPagesJEI.doubleIcon` (CD) | move the icon helper into Common |
| `compat/world/BurnRows` (Common) | `itemphysic` rules (Compat) | Compat registers its rows through `WorldRows.provider` |
| `compat/world/CampfireRows` (Common) | `campfire/CampfireRain` (CD) | CD registers its rows through `WorldRows.provider` |
| `SectionUpdateNullStateMixin`, `BlockUpdateNullStateMixin` (Common) | `devtools/BlockStateSyncDebug` (Devtools) | Common listener hook; the probe subscribes from Devtools |
| `itemphysic/ItemBurnTransform` (Compat) | `shrapnel/ShrapnelSpawner` (Engineering) | soft: Engineering registers the TNT transform, or an API event in Common |
| `food/PlacedFoodSol` (CD) | `worldgen/WorldgenFiles` (Worldgen) | WorldgenFiles is generic file access: Common util |
| `dimension/PortalFailureHandler` (Worldgen) | `block/CreativeEngineeringBlocks.DARK_FIRE` (Engineering) | Dark Fire, Flint and Terrasteel and their assets move to Worldgen with the portal rule (see 7.4 owner rows) |
| `FeedingTroughModuleMixin` (CD) | `CEConfig.PERF.troughMissCooldownTicks` | key moves to CDConfig |
| `SereneSeasonsMeltThrottleMixin` (Calendar) | `CEConfig.PERF.seasonsMeltIntervalTicks` | key moves to Calendar's config |
| `surface/StuckInBlock` (Engineering) | `CEConfig.LEAF_PILES.slowFromLayers` (Compat) | key read moves behind a Common API or is duplicated in Engineering's config (owner) |
| NCH (Common) | `CEConfig.COOKING.ovenIngredientCounts` (CD) | NCH takes the value from a hook CD registers at startup |
| `perf/PerfCommand` (Devtools) | `PerfEvents`, `MobCapLevels` | down to Common and Worldgen: fine if Devtools declares both as optional deps |
| `mooshroom/MooshroomFamily` <-> `worldgen/SpawnTuning` | | internal if both stay Worldgen; if breeding goes to CD, `SpawnTuning.brownChance` becomes a Common API |
| `NeaWhistleGoalSkipMixin` (Equipment) | `perf/WhistleArrowTracker` (Common) | down, fine; tracker stays Common |
| `kubejs/KubeJSCanAction` (CD) | re-registered by NCH (Common) | NCH gains a generic action registry; CD registers its action |

**Scripts (sweep F)**

| From | To | Fix |
|---|---|---|
| CD jar `loot.js` x3 | `global.lootTuning` (Worldgen `loot/05_tuning.js`) | already guarded: soft dependency |
| CD jar `wild_crops.js` | `global.biomeRules` (Worldgen `_biome_rules.js`) | unguarded: guard, or move biomeRules into Common |
| `items/echo_tools.js` (Equipment) | `global.toolEffects` (CB) | the echo tool effect rows are gear effects, CB's by creative_brewing.md 4.2; the Equipment file only declares items |
| `neoCraftHelper.js` (Common) | `global.seasoning` (CD jar) | upward: NCH exposes a hook CD fills |
| CD jar, 7 files | `global.addItemMerges` / `itemMergeRun` | merge runner is Common |
| CD jar | `global.trades` (`func/trade_helper.js`) | trade helper is Common (`TradeListings` is already a Common API) |
| `_lib/recipe_scan.js` | | split: runtime `stripRecipeTags` / `stripRemovedItemTags` are Common, the dump command is Devtools |
| CB `cooking/food_values.js` | CD `foodValueCopies` | CB -> CD is allowed only if CB declares CD optional; today both are in the pack folder, so no change until CB's script moves |

**External (sweep G)**: `PackTime.java` in the Vinery member -> `time/WineTime` becomes a Calendar dependency
(`creativecalendar`, optional AFTER). The 13 CD Java imports of `cooking/`, `crops/` and `MealSlotTooltip` become
internal once those classes are CD's. CD's mandatory `creativeengineering` dependency becomes `creativecommon` at Q1.

Cross-pack script order: priorities only order within one script pack, so jar scripts read `global.item` and the
other dicts at event time, not at load. `NCH.cfg` hard-codes the `CEConfig` class and needs a per-jar config class
registration before the first carve.

### 7.3 Carve order (confirmed)

The draft order in section 4 holds. Two preparatory steps come first because every carve needs them:

0. **Prep in patcherMod:** `GatedMixinPlugin` from `development/shared/mixinplugin` replaces `perf/CEMixinPlugin`'s
   accept-all `shouldApplyMixin` (keep its Immersive Weathering `postApply` rewrite); `NCH.cfg` gains per-jar config
   registration; the Common APIs in 7.2 (DamageTags, WorldgenFiles, glob, doubleIcon, WorldRows.provider users, the
   null-state debug hook, the can-action registry, the seasoning hook) are introduced while everything is still one jar.
1. **Devtools:** 11 classes, `_lib` dumps, perfBot and clientCapture nested, dev `mods/` only.
2. **Compat:** 23 classes, 41 mixins, 3 sections, paintings and banner textures.
3. **Calendar:** 14 classes, 18 mixins, 3 sections; the Vinery `PackTime` edge and the CD crop-season readers switch to
   it.
4. **CB phases 1 to 3** (creative_brewing.md 4.2; phase 1 has not run, its 5 sections are still in `CEConfig`), then
   **CD**: 63 classes, 24 mixins, 7 sections, the trough code, and babyMobs, moreMooblooms, incubationAdditions as
   members.
5. **Worldgen** with fauna: 57 classes, 56 mixins, 9 sections, 49 structure namespaces, loot.
6. **Equipment** with the Magic rows: 36 classes, 23 mixins, 10 sections, the two network channels' stepstool half.
7. **Engineering** with the Computers rows: 45 classes, 31 mixins, 3 sections, 211 generated sawmill recipes (the
   generator moves too), then the Q1 id swap.

### 7.4 Remainder check and owner rows

After the seven carves, patcherMod holds exactly the 2.4 list: config framework, `util/`, the KubeJS loader and
mixins, NCH, `tag_helper`, `compost`, the dicts, `GatedMixinPlugin`, `changelog/`, the advancement drop, the shared
APIs (now including DamageTags, WorldgenFiles, the trade helper and the merge runner), fixes and perf, and the
Interface rows (3 JEI plugins, 7 WTHIT entries, tooltip stacks, GUI title, armor bar, toasts). Common registers no
ids. Extras found by the sweeps that are not in 2.4 and must leave or be decided: `trader/WanderingTraderTweaks`
(spawn tweak, Worldgen?), `util/ArmorEnchantabilityBridge`, `util/BonemealCharms`, `util/ElytraChestplates` (Equipment
features in `util/`), `PotionFluidTooltip` (only CB users), and the `working/` parked files.

Rows the owner must settle before the execution docs are written (each sweep file lists its own `?` rows in full):

| Row | Options | Audit lean |
|---|---|---|
| `mooshroom/` (breeding and walk target) and `MooshroomFamily` | Worldgen fauna, or CD with More Mooblooms | Worldgen; the brown-spawn half is fauna and the breeding half is 2 classes |
| Dark Fire, Flint and Terrasteel, their assets | Worldgen (portal rule) or Engineering (block and item packages) | Worldgen, which removes the PortalFailureHandler edge |
| `damage/` | Equipment, or Common | Equipment with `DamageTags` as a Common API |
| `campfire/` (comfort, rain) | CD or Compat | CD, as the plan routes it |
| `block/MossTint`, `SaltBlockTicks`, `VineShearing` and the Salt and vine mixins | Engineering, Worldgen or Compat | Compat (single-mod tweaks) |
| Wandering trader set (`WanderingTraderTweaks`, `trade_helper.js`, `wandering_trades.js`, `tradetags.js`) | Common or Worldgen | trade helper and `TradeListings` Common, the spawn tweak Worldgen |
| `MonsterBoxVault`, monster box loot | Worldgen fauna or Compat (Quark) | Worldgen |
| `item/BottledAir`, `[bottled_air]` | Engineering or Equipment | Engineering |
| `fan/` dragon breathing (Engineering recipe type, CB lingering output and JEI) | split or one owner | Engineering owns the type; the lingering rows and `DragonBreathingJEI` go to CB |
| `WthitCoaster`, `WthitHive`, `WthitKitchenStation`, `WthitRemainderBuffer`, `WthitSheared` | Common or the content jar | with the content jar (CB, Worldgen, CD, CD, Compat) |
| `onServerStarted` IE recycling diagnostics in the main class | Engineering, Devtools or delete | Devtools |
| `NockRecipeBookSync`, `SequencedAssemblyRecipeMixin` | Equipment or Compat / Common | Equipment (they only serve Equipment content) |
| `[botania]` section and `TrinketCaseRules` | Compat or the Magic reservation | Compat |
| `MassPlacer` (`/cdevplace`) | Worldgen or Devtools | Worldgen (it is built on Worldgen internals) |
| `other/harvest.js`, `other/creative_labels.js`, `items/decor_blocks.js`, `tags/entitytags.js` | see F section 13 | Equipment, creativeLabels jar, Compat, Worldgen |
| The three ungated live test-recipe files and `_lib/flower_world.js` | Devtools or delete | Devtools |

Housekeeping found on the way (not split work, listed so it is not lost): one stale lang key
`waila.h12y.tier.creativeengineering.copper`, three empty resource folders, one advancement json in
`watchtower_building`, six unused root PNGs in `kubejs/assets`, 1890 lines of stale `startup_scripts/todo/*.txt`,
`namechanges.js` 249-340 (Delight names left behind by the 10-27 move), `items/CreativeEngineeringItems.java` declares
package `items` while sitting in `item/`, `FlintAndTerraSteelItem` is headed TEMPORARY, `magic/enchants.js` uses six
raw event calls instead of NCH.

### 7.5 Per-jar checklist

G section 7.2 holds the eleven items every new jar needs: `settings.gradle` include, `build.gradle` with
`ce.forge-mod` (`shared_sources=packs,mixinplugin`), `gradle.properties`, `mods.toml` (own `[[mods]]`, mandatory
`creativecommon` AFTER, own optional deps), `<modid>.mixins.json` plus a `GatedMixinPlugin` subclass,
`kubejs_startup/<modid>/` core file with `global.<X>` and the script trees under `kubejs/data|assets/<modid>/kubejs/`,
`<modid>-common.toml` plus `configs_edited/`, a `mod_sides.md` row, `build_deploy.py` entry (`IN_TREE_MODS`, and
`replaces_jars` where a jar is displaced), generator output paths in `tools/generators.json`, and for CD members the
`bundled_in` steps. All ids keep `creativeengineering:`; Forge accepts several mods registering into one namespace
(each jar creates its own `DeferredRegister` for it), and the 31 `@Mod.EventBusSubscriber` annotations must change
`modid` when their class moves.

### 7.6 Owner sign-off (2026-11-01)

These decisions override sections 2, 6, 7.2 to 7.4 wherever they differ. Execution runs on git branch
`creative-split`, one commit per carve, and each carve is verified by a scratch dedicated-server boot before the next.

**Jars (9 carved plus Common).** Devtools, Calendar, Compat, CD, CB, Worldgen, **Magic**, Equipment, Engineering.
Creative Magic is carved in this split, no longer only reserved. Creative Computers is dropped and folded into
Engineering for good.

**Dependencies.**
- **Compat is required by every other Creative jar.** It sits between Common and the content jars: Common <- Compat <-
  everything else. Compat applies each tweak only when the target mod (third-party, or one of ours) is loaded
  (`GatedMixinPlugin` and `ModList` checks). Content jars may call Compat (downward). Compat never hard-links a content
  jar: a Compat row that needs a content jar uses an API the content jar registers into (for example, Engineering
  registers the burning-TNT shrapnel transform with Compat's ItemBurnTransform).
- CD requires Common, Compat, Calendar. CB requires Common, Compat, Calendar, CD (all mandatory, `AFTER`).
- Jars carved before Compat exists get the `creativecompat` dependency when Compat is carved.

**Ids.**
- Q1: the `creativecommon` swap is the last step, after Engineering, as recommended.
- Q2: CD and CB are renamed to `creativedelight` and `creativebrewing` (mod id, config file name,
  `kubejs/data|assets/<id>` script trees, NCH recipe namespace). **Superseded 2026-11-01 (owner, at wave 2):** the ids
  under the old namespaces move too ("any items with the current underscore naming are correctly swapped over to the
  new ones with all data using the swapper system"). `creative_delight:*` became `creativedelight:*` (treated sticks,
  the village farm processor, the wild crop features, recipes, loot, tags, lang keys); `creative_brewing` registered
  nothing. Saved worlds carry over through the swap lists (7.7).

**Q6:** Devtools goes in dev `mods/` only. **Q7:** Creative Brewing is the first jar to publish. Its required chain
(Common, Compat, Calendar, CD) has to be published with it, so each needs the licence audit that creative_brewing.md
9 did for CB. Compat's 950 painting files and CD's ARR overrides are the known risks.

**7.4 rows.**

| Row | Decision |
|---|---|
| Mooshrooms and Mooblooms | **Worldgen** handles all mooshroom and moobloom spawning: `mooshroom/`, `MooshroomFamily`, More Mooblooms as a Worldgen member (not CD). Fungi Delight stays a CD member, and Worldgen takes over its mooshrooms' natural spawning. |
| Dark Fire, Flint and Terrasteel, the Nether portal-failure rule (`dimension/PortalFailureHandler`) | **Creative Magic**. The Magic sweep (`split/H_magic.md`) lists any other content that belongs there. |
| `damage/` | Equipment; `DamageTags` becomes a Common API. |
| `campfire/` (comfort, rain) | ~~**Compat**~~. **CD** (owner, 2026-11-01, 7.9 step 1). |
| `MossTint`, `VineShearing` and their mixins | Compat. **Salt** (`SaltBlockTicks` and the salt mixins) goes to **CD**. |
| Wandering trader | **Everything trader goes to Common**: `TradeListings`, `TagItemListing`, `trade_helper.js`, `WanderingTraderTweaks` (spawn interval and offer counts) with its two mixins and `[wandering_trader]`, `wandering_trades.js`, `tradetags.js`. The Winemaker stock and `WinemakerTrades` stay CB's (creative_brewing.md 4.2). |
| `MonsterBoxVault` | **Compat**. |
| `item/BottledAir`, `[bottled_air]` | **Equipment**. |
| `fan/` dragon breathing | **All Engineering**: the type, the lingering rows and `DragonBreathingJEI`. |
| `WthitCoaster`, `WthitHive`, `WthitKitchenStation`, `WthitRemainderBuffer`, `WthitSheared` | **All in Common**, each line gated on its content mod being loaded. Common must not import content classes: the line reads an accessor interface Common defines, and the content block entity implements it (like `ce$tankLevel`). |
| IE recycling diagnostics (`onServerStarted`) | Devtools. |
| `NockRecipeBookSync` | Equipment. The Equipment carve checks whether it is still needed now that the recipe book is removed; delete it if not. |
| `SequencedAssemblyRecipeMixin` | **Common**. |
| `[botania]` and `TrinketCaseRules` | **Creative Magic**. |
| `MassPlacer` (`/cdevplace`) | **Devtools** (optional dependency on Worldgen). |
| `other/harvest.js` | **Equipment** (owner, wave 4 close; supersedes the earlier CD row). |
| `other/creative_labels.js` | the creativeLabels jar, which stays its own mod. |
| `items/decor_blocks.js`, `tags/entitytags.js` | Compat, Worldgen. |
| `_lib` test recipes (`metal_test_recipes`, `wood_test_recipes`, `sequence_assembly_processes`) and `_lib/flower_world.js` | Devtools. |
| Remainder extras: `ArmorEnchantabilityBridge`, `BonemealCharms`, `ElytraChestplates`; `PotionFluidTooltip` | Equipment (unless the Magic sweep claims the enchantability bridge); CB. |

**Carve order (replaces 7.3).**
1. **Devtools** and **Calendar** (owner: start with these two). Neither needs the 7.3 step 0 APIs: Devtools brings its
   own null-state listener hook into Common, and Calendar takes `[perf] seasonsMeltIntervalTicks` into its own config.
2. Prep in patcherMod (7.3 step 0: `GatedMixinPlugin`, `NCH.cfg` per-jar registration, the remaining Common APIs) and
   the CD and CB id rename.
3. **Compat**, then each earlier jar gains the `creativecompat` dependency.
4. **CB** phases 1 to 3, then **CD** (troughs, babyMobs, incubationAdditions, salt, harvest). Finished by 7.9.
5. **Worldgen** with fauna, More Mooblooms and babyMobs (owner, 2026-11-01: babyMobs leaves CD here, 7.9 step 4).
6. **Magic.**
7. **Equipment.**
8. **Engineering** with the former Computers rows, then the Q1 `creativecommon` swap.

### 7.7 Wave 2 as built (2026-11-01)

- **Rename.** Mod ids, config files, mixin configs, script trees (`kubejs/data|assets/creativedelight`,
  `kubejs_startup/creativedelight`, the same for CB), lang keys, NCH group and the `itemMergeGroups` key lost the
  underscore across code, tools, scripts and current docs (history, deployed plans and run records untouched). Jars are
  now `creativedelight-1.0.0.jar` and `creativebrewing-1.0.0.jar`.
- **Swap lists (first built part of creative_delight.md 11, TODO `creativedelight/swappers`).** Common
  `swap/SwapLists` reads every loaded jar's `data/<modid>/swaps/*.json` (`namespaces` old -> new, `replaces` survivor
  -> [old ids]; never pruned). `swap/SwapRemapper` answers Forge's `MissingMappingsEvent` for every Forge registry, so
  saved blocks, items in any container or inventory, block entities and entities load under the new id and the world
  saves under it. CD and CB ship `swaps/creative_split.json` with the namespace move. Vanilla-only registries have no
  remap hook: CD keeps a load-only alias `creative_delight:village_farm_crops` for farm pieces saved inline in structure
  starts. `swap/ConfigRename.carryOver` renames an old `creative_delight-common.toml` / `creative_brewing-common.toml`
  to the new name before the config loads, so player values stay.
- **Prep (7.3 step 0).** `CEMixinPlugin` is a `GatedMixinPlugin` (`shared_sources=packs,mixinplugin`; table holds
  `IwServerLevelTickMixin`; each carve takes its rows); IW `postApply` kept. `NCH.cfg` looks a section up in every
  registered config class (`global.NCH.registerConfig(modId, className)`; `modid:SECTION` pins one). Common APIs:
  `util/WorldgenFiles` (moved from `worldgen/`), `util/Globs` (`CropGrowth.glob` delegates), `compat/jei/JeiIcons`
  (`CanPagesJEI.doubleIcon` delegates), `compat/kubejs/IngredientActions` (the can action registers into it; NCH
  re-applies all actions and names only the id). `global.seasoning` already is the hook CD fills (NCH reads it at event
  time, guarded): no change. `DamageTags` stays in `damage/` with its snapshot and loader; which of the four classes stay
  Common is settled at the Equipment carve. The null-state hook came in wave 1.
- patcherMod 1.1.06; CD and CB require `[1.1.06,)` (they call `ConfigRename`).

### 7.8 Wave 4 as built (2026-11-01; built, scratch-tested, NOT installed)

Built on waves 2 and 3 (installed). Scratch run 2026-11-01 ([scratch_run_2026-11-01.md](testing/scratch_run_2026-11-01.md)
7): waves 2, 3 and 4 PASS on the server side after three fixes, all in 1.1.07 / CD: `SwapRemapper` now resolves the
survivor in Forge's ACTIVE registry (the event comes from the STAGING copy, so 1.1.06 left every renamed block
missing); `NCH.cfg` treats a missing Java field as "not here" (Rhino throws, so every section moved to CD or CB read as
undefined); CD `MobCobPipeSlotMixin` targets `LivingEntity` (on `Mob` it never applied in patcherMod and crashed CD,
which sets `defaultRequire: 1`). Client rows and the baby squid row remain. Detail: [creative_brewing.md](creative_brewing.md) 4.5 and 4.6 (CB), [creative_delight.md](creative_delight.md)
34 and 29.1 / 29.5 (CD).

- **CB phase 1 (scripts):** the startup, server and client files of creative_brewing.md 4.2 phase 1 and the gear
  effects moved into CB's trees; splits of `tooltips.js`, `namechanges.js`, `fluid_jei.js`, `fluidtags.js`,
  `wandering_trades.js`, `trade_helper.js` (Winemaker helpers now `vinery/winemaker_trades.js`); the two compost
  files moved from CD to CB. CB recipe ids are now `creativebrewing:` (nothing referenced the old ones).
- **CB phases 2 and 3 (Java, config):** 17 classes plus a new `PotionMachinesJEI`, 25 mixins (gated per mod), 10 lang
  keys, the lingerification list; `[alcohol]`, `[effects]`, `[magic_resistance]`, `[copper_poisoning]` to
  `creativebrewing-common.toml` (Common `ConfigRename.mergeSections`, for a file that already exists). Stay in
  patcherMod against 7.6: `PotionFluidTooltip`, `RecipeRows`, `RowPagesCategory` (shared by Common and CD),
  `PotionRows` (all potions, interface), `WthitCoaster` and `[coaster]` (reflection, no CB class).
- **CD (Java, config, members):** 59 classes (`oven/`, `lunchbox/`, `cooking/`, `food/`, `crops/`, the CD `compat/`
  and JEI pages, troughs, `salt/SaltBlockTicks`), 24 mixins, 45 resource files and 41 lang keys; `[crop_growth]`,
  `[dandelion_maturing]`, `[crop_drops]`, `[stem_fruit]`, `[canning]`, `[cooking]` and `[perf]
  troughMissCooldownTicks` to `creativedelight-common.toml` (NCH finds them through `registerConfig`). Common hook
  `compat/RemainderView` for the WTHIT leftover line. babyMobs and incubationAdditions nested as CD members
  (`members/babymobs`, `members/incubation`; mod ids unchanged). CD scripts: sample syringe, `other/harvest.js`, the
  kitchen, crop and food-fluid tag rows, Delight names and the bowl / basket / soil tooltips.
- patcherMod 1.1.07 (never installed before; holds both the CB and CD API changes). CB and CD require `[1.1.07,)`.
- Instance config values were moved by hand into the CB and CD files and `configs_edited/`.
- **Settled (owner):** `other/harvest.js` (metal and ore mining tiers) goes to **Equipment**. It moved to CD in this wave, so the Equipment carve must take it back out of CD's script tree.
- **Known:** `tools/gen_dd_dishes.py --check` cannot see models inside nested jars (old gap); `tools/remainder_buffer_test`
  and `crop_growth_test` repointed but not rerun.
- **Install** (game closed, after SQ-CS2 and SQ-CMP pass): `./gradlew :patcherMod:installToMods
  :creativeCompat:installToMods :creativeCalendar:installToMods :creativeDelight:installToMods
  :creativeBrewing:installToMods --offline`. Scratch: SQ-CBM, SQ-CBJ4, SQ-CDS, SQ-CDJ4 in
  [scratch_queue.md](testing/scratch_queue.md).

- **Installed 2026-11-01:** patcherMod 1.1.07, Compat, Calendar, CD, CB in the dev instance `mods/`.
- **Client tests on hold (owner):** every client row for waves 2 to 7 is pinned until wave 8 is done or later, then run as one bulk session. Server rows keep running per carve.

### 7.9 Plan: finishing Creative Delight (owner, 2026-11-01; plan only, nothing built)

**Status 2026-11-04 evening: steps 1 to 3 built and installed (Compat 19:05, CD 19:19, patcherMod 1.1.09 19:17), not booted, not tested** (scratch
SQ-CFR-01 to -08, CQ-CFR-01; SQ-NDM for step 2). As built, where it differs from the plan below:
- Step 1: the JEI JSON helpers were copied into CD's `compat/jei/CampfireRows` (no Common util, no patcherMod change).
  `[campfire]` is carried from `creativecompat-common.toml` first, then from `creativeengineering-common.toml` for a
  pack that skipped 1.2.00 (`CDConfig.COMPAT_SECTIONS`). No `CDMixinPlugin` row: `minecraft.*` mixins always apply.
  `configs_edited/root_config/` copies updated (section moved, all defaults).
- Step 3: not a second copy of `data/creativecommon/damage_tags/fire.json` (Forge 1.19.2 serves only the first
  jar's copy of a shared path, so the merge would never happen): CD ships its own tag `creativedelight:stoves`
  (`data/creativedelight/damage_tags/stoves.json`) and patcherMod's `fire.json` and `burn_tick.json` include
  `#creativedelight:stoves` in place of the id. That patcherMod edit is in 1.1.09 (built and installed
  by the wave 6/7 carve session, 19:17).
- Step 2: `mod_sides.md` Nether's row merged into CD's row 2026-11-04. `build_deploy.py` has no `replaces_jars`; its
  nested-id duplicate check covers a stale Nether's jar.

Owner request: move the `oven/`, `lunchbox/`, `food/`, `cooking/`, `crops/` and `campfire/` folders to CD; move
incubationAdditions to CD; move babyMobs and moreMooblooms to Worldgen; make Nether's Delight, Cooking for Blockheads,
End's Delight and Ender's Delight CD members. These decisions override the 7.6 rows on campfire and babyMobs.

**Already done (wave 4, 7.8; built and installed, not committed).**

| Item | Where it is |
|---|---|
| `oven/` (11), `lunchbox/` (10), `food/` (9; `IntoxicatingDrinkTooltip` went to CB), `cooking/` (6), `crops/` (7) | `development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/`; nothing left in patcherMod |
| incubationAdditions | CD member `members/incubation` (mod id unchanged) |
| End's Delight | CD member `repackedEnds` (repack of the MIT 2.1 jar) |
| Ender's Delight | ARR, so the jar stays in `mods/`; MIT 1.1.4 content is CD member `endersAdditions` (inside the End's member `repackedEnds` since 2026-11-02) |
| Cooking for Blockheads | ARR, so the jar stays in `mods/`; CD member `blockheadsAdditions` |

Owner, 2026-11-01: Ender's and Cooking for Blockheads stay additions members ("member" = the additions member plus every
leftover row moved into CD). Nether's Delight is **repacked** into CD like End's (supersedes the 11-01 additions-overlay
decision in nethers_delight.md 1).

**Step 1: campfire, Compat -> CD** (one commit; Compat and CD rebuild).

| From (Compat unless noted) | To | Note |
|---|---|---|
| `campfire/CampfireComfort`, `campfire/CampfireRain` | CD `creativedelight/campfire/` | package rename only |
| `mixin/CampfireRainMixin` and its `creativecompat.mixins.json` entry and `CompatMixinPlugin` gate row | CD mixins json and CD's `GatedMixinPlugin` table | targets vanilla `CampfireBlockEntity`; CD sets `defaultRequire: 1`, so confirm it applies in the scratch boot |
| `compat/jei/CampfireRows` and the `WorldRows.provider(WorldTab.CAMPFIRE, ...)` call in `CreativeCompat` | CD JEI setup | uses BurnRows' JSON helpers: move those helpers to a Common util (beside `JeiIcons`) or copy the two methods; no CD -> Compat class edge for a JEI helper |
| `CampfireRainInfo.set(...)` in `CreativeCompat` (lines 85-87) | CD main class | |
| `CompatConfig.CAMPFIRE` and `"campfire"` in its section list (line 28) and the config regex (`CreativeCompat` 163) | `CDConfig` `[campfire]` | `ConfigRename.mergeSections` carries the section from `creativecompat-common.toml` to `creativedelight-common.toml`; copy both files to `configs_edited/` |
| patcherMod `campfire/CampfireRainInfo`, `CampfireRainTicks`, `compat/WthitCampfire` | **stay in Common** | the accessor interfaces behind the WTHIT line (7.6 WTHIT rule) |
| `#creativeengineering:campfire/*` tags | unchanged | ids keep `creativeengineering:` |

No registry ids move, so no swap list. Tests: reuse the campfire rows of [creative_compat.md](creative_compat.md) as a new SQ entry: rain douses a lit campfire after the configured ticks, the coal-block
fuel exception, the comfort effect, the `[campfire]` value carried over from an old Compat config.

**Step 2: Nether's Delight repacked as CD member `repackedNethers`** (one commit; CD rebuild; replaces a live jar).
BUILT 2026-11-01 with the 4.0 carry-overs (TODO `largecontent/mod-additions #16`; as built: nethers_delight.md 4.9); not
installed, not tested. Items 1 to 3 done; items 2 (jar out of `mods/`) and 5 (`mod_sides.md`) happen at the CD install.

1. `development/creativeDelight/members/nethers/` with `ce.forge-repack` (copy `members/ends/` shape):
   `mod_id=nethersdelight`, `mod_name=Nether's Delight (Creative Delight)`, `mod_license=MIT (upstream); overlay GNU GPL3`,
   `upstream_jar=NethersDelight-1.19-3.1.jar`, `upstream_sha1=844fcf67924c682d65adee493e4dd5ba2f6cc9f3`,
   `bundled_in=creativeDelight`, `NOTICE-upstream-MIT.txt`. Add `'repackedNethers': 'nethers'` to the member map in
   root `settings.gradle`.
2. Upstream jar to `development/creativeDelight/upstream/` (git-ignored); the 3.1 jar leaves `mods/` to
   `jar_archive/displaced/`; `build_deploy.py` `replaces_jars` for CD gains `NethersDelight-*` (the nested-id check at
   459 then catches a stale copy).
3. The 4.0 carry-overs of nethers_delight.md 4.1 to 4.6 and 4.8 go into the overlay (Potted Mimicarnation block, the
   Fungus Colony mixin as `nethersdelight.ce.mixins.json`) and into CD `kubejs/data/creativedelight/kubejs/nethersdelight/`
   (tags through `global.tags`, the `stuffed_hoglin` file move through `global.NCH`). Ids keep 3.1's; renames are lang only.
4. Edges that stay where they are: Calendar `CropSeasonGrowHookMixin` (gated on `nethersdelight`, id unchanged),
   CD `FeastPlatingCategory` and `crop_growth.json` rows (already CD), patcherMod `surface/StuckInBlock` (one class-name
   string in a generic table), Display Delight's tags, Engineering `fabric.js` hoglin mount row (non-food, rule 9).
5. `mod_sides.md`: drop the Nether's row, add `nethersdelight` to CD's nested list. Writeup: nethers_delight.md 1 and 4.

Tests: the ND- rows (nethers_delight.md 4.7) plus a load check: one `nethersdelight` mod in the list, display name
with the suffix, existing worlds keep Nether's blocks and stove contents (same ids, same save format).

**Step 3: Ender's and Cooking for Blockheads leftovers into CD** (one commit with step 2 or 4).

| Row | Decision |
|---|---|
| `endersdelight.endstone_stove` in patcherMod `data/creativecommon/damage_tags/fire.json` and `burn_tick.json` | **moved 2026-11-04 as tag `creativedelight:stoves`** (see Status above). Planned: `DamageTagLoader` merges every pack's copy (`listResourceStacks`), so CD ships its own `data/creativecommon/damage_tags/fire.json` and `burn_tick.json` with only that entry, and patcherMod's copies drop it |
| `endersdelight:phasing` in `ShieldConfig`'s default effect list | stays: one id in an Equipment config default; moving it needs a hook for one string |
| `endersdelight:end_house` in `worldgen/earth_fill.js`, `terrain_adaptation.js` | stays: structure rules, Worldgen owns every structure's row |
| `cookingforblockheads:*_kitchen_floor` in `blocktags.js` 164 | stays: one member of a broad pack block tag (smooth stone family) |
| `cookingforblockheads=Cooking for Blockheads` in `CEConfig` 2484 | stays: Common's mod display-name table |
| `config/jei/recipe-category-sort-order.ini` | stays: instance config |

**Step 4: babyMobs to Worldgen** (in the Worldgen carve, `creativesplit/worldgen #8`).

babyMobs depends only on Forge and Minecraft and nothing in CD imports it, so the move is wiring only: the member map
entry `'babyMobs': 'babymobs'` goes to the Worldgen bundle with More Mooblooms, `bundled_in=creativeWorldgen`, and the
`mod_sides.md` note moves from CD's row to Worldgen's. Mod id `baby_mobs` is unchanged, so no swap list. Until Worldgen
exists it stays nested in CD (pulling it out now would mean a standalone jar for one carve).

**Order and gates.** Commit the installed wave 4 first (the CD Java move is uncommitted). Then step 1 and steps 2 + 3
as separate commits, each with a scratch dedicated-server boot queued (SQ entries, owner runs them in a batch); step 4
rides the Worldgen carve. Game closed for every install: `./gradlew :creativeCompat:installToMods
:creativeDelight:installToMods --offline` (plus `:patcherMod:` for the damage-tag edit in step 3).

**Changelog.** Steps 1, 3, 4 are same-behaviour moves: no bullet. Step 2 brings Nether's 4.0 changes (Potted
Mimicarnation, colony rule, renames) to players: bullets under a `## Nether's Delight` family when built.

### 7.10 Wave 5 as built (2026-11-02; built, installed 2026-11-02 23:16, not booted, not tested)

Creative Worldgen carved: [creative_worldgen.md](creative_worldgen.md) holds the detail (what moved, edges, what stayed
in the pack folder and why). In short: 59 classes plus 4 new, 60 mixins, 6 config tables (`[perf]` mob cap keys only),
the dimension and noise settings data, the worldgen, loot, removed-mob and Worldgen tag scripts, More Mooblooms and Baby
Mobs as members (7.9 step 4 done). patcherMod 1.1.08. Placed differently from the sweeps: `BubbleColumnImmunityMixin` to
Worldgen (its tag moved with `entitytags.js`); `QuarkSpiralSpiresTeleportMixin` stays Common (perf). Structure datapacks
stay in the pack folder (the structure generators tell jar sets from kubejs/data sets). Found on the way: namespace
script packs load in no fixed order, so CD's loot rules now queue through `global.CWG.lootRules`; `build_deploy.py`
`check_climate_reader` read a class path stale since wave 2 (fixed). Scratch SQ-CWG-01 to -10.

### 7.11 Wave 6 as built (2026-11-04; built, installed 2026-11-04 19:17, not booted, not tested)

Creative Magic carved: [creative_magic.md](creative_magic.md) holds the detail. In short: 4 classes plus 3 new (Dark
Fire, Flint and Terrasteel, the portal rule, `TrinketCaseRules`), 7 mixins, `[botania]`, the Dark Fire and Flint and
Terrasteel assets, five `magic/` server scripts plus the XP fluid tags, three client rows. patcherMod 1.1.09 (shared with
wave 7). `CreativeEngineering.BLOCKS` and `block/CreativeEngineeringBlocks` are gone (Dark Fire was their only row).
Placed as sweep H leaned: D1 `dragon_breathing.js` stays for Engineering; D2 and D4 went to Equipment. **Open (owner):
D3**, the liquid XP bridge (`FluidBridge` and five mixins), stays in patcherMod until the Engineering carve. Found on
the way: the pack folder's `dark_fire_0.png` differed from the jar's (the pack one is what players saw; the jar now
ships it). Scratch SQ-CMG-01 to -05.

### 7.12 Wave 7 as built (2026-11-04; built, installed 2026-11-04 19:17, not booted, not tested)

Creative Equipment carved: [creative_equipment.md](creative_equipment.md) holds the detail. In short: 35 classes plus 3
new, 20 mixins, 9 config tables (9, not the audit's 10: `[damage_tags]` stays Common), the shield item models and 8
lang keys, the gear startup scripts (tool and armor tuning, air brake, echo tools, stepstools, Felsic Scrap), the
`equipment/` server scripts, backpack skins, `harvest.js` (out of CD's tree, the wave 4 owner row), the gear tags and
the stepstool Curios rows, and the gear client rows. **Damage row settled:** all of `damage/` with `[damage_tags]`
stays in Common as the API Creative Brewing's Magic Resistance reads, with `shield/EffectTickContext`.
`NockRecipeBookSync` is still needed and moved. Recipe ids of moved recipe scripts are now `creativemagic:` and
`creativeequipment:` (3.9). CD was clean-built and reinstalled in the same step so `harvest.js` runs once. Scratch
SQ-CEQ-01 to -08.

After waves 6 and 7, patcherMod holds Common plus the Engineering rows (`fan/`, `sawmill/`, `item/` sawblade and
sawdust, `bucket/`, `excavation/`, `fluid/`, `surface/`, `shrapnel/`, their mixins, `[shrapnel]`, `[surface]`,
`[vein_undo]`, the 211 sawmill recipes) and the open D3 rows.

### 7.13 Wave 8 as built (2026-11-04; built, NOT installed, not booted, not tested)

Creative Engineering carved and the Q1 swap done in the same step: [creative_engineering.md](creative_engineering.md)
holds the detail. Owner, 2026-11-04: **D3** (the liquid XP bridge) goes to Engineering; the carve and the swap in one
session, and the project folder is renamed too (`development/creativeCommon`, `:creativeCommon`). In short:
- Engineering (`creativeengineering` 2.0.0, package `com.SaintAlfonzo.creativeengineering` unchanged): 67 classes plus
  3 new, 29 mixins (now gated on their mods), `[shrapnel]`, `[surface]`, `[vein_undo]` in the CE file it keeps, 211
  sawmill recipes, 41 lang keys, the engineering startup items and transition items, and the `engineering/`, `metal/`,
  `wood/` and five tag server scripts (recipe ids unchanged: NCH's default namespace is Engineering's own).
- Common (`creativecommon` 1.0.0, package `com.SaintAlfonzo.creativecommon`, `CreativeCommon`, `CommonConfig`,
  `creativecommon-common.toml` carried from the CE file on the first boot, `creativecommon.mixins.json` with 74
  mixins, `kubejs_startup/creativecommon/`, lang under `assets/creativecommon/`). Every other Creative jar requires
  `creativecommon [1.0.0,)` and imports the new package.
- One sideways edge fixed: Compat set Engineering's `surface/LeafPileSlow`; Engineering now sets it from Compat's config.
- Stayed in the pack folder: `removed_woods.js` (load order before `item_merges.js`), `block_properties.js` (mixed),
  `economy/trades.js` (unsettled row).
- No sawmill generator exists (7.3 assumed one). Scratch SQ-CEN-01 to -08, CQ-CEN-01; SQ-CMG and SQ-CEQ run on these jars.

Remainder check (7.4): Common holds the 2.4 list. Not in 2.4 and still in Common: `campfire/` accessors (WTHIT rule,
7.6), `swap/` (built in wave 2), `trader/` (7.6 row), `changelog/`, `ArmorTrimsItemCache` (perf), the parked
`working/` files (D10, follows D3: Engineering's scratch now, never compiled).

### 7.14 Wave 9: Common's namespace and four moves out (2026-11-04; built, NOT installed, not booted, not tested)

Owner, 2026-11-04: move Common's leftover `creativeengineering` data to `creativecommon`, renamed outright (no
read-old fallback), the pack-folder trees included; and move four groups out of Common.

- **Renamed to `creativecommon`** (`CreativeCommon.NS`, `PackIngredientOrder.TAG_NAMESPACE`): the 32 damage tags
  (`data/creativecommon/damage_tags/`, CD's `stoves.json` includes follow), WTHIT plugin and line ids
  (`waila_plugins.json`), JEI category uids (`world/<tab>`, `cycling_crafting`, `smithing_template`), the `jei/` tags
  (`jei_order.js`, `jei_order_tags.js`) and `wthit/needs_silk_touch` / `silk_touch_differs` (`wthittags.js`), the GUI
  tint textures and `state_lines.json`, 69 lang keys (`block.creativeengineering.rich_pot` is now
  `creativecommon.wthit.rich_pot`; the stale `crop_growth` plugin key dropped), WTHIT server-data keys, the item-merge
  world stamp (`creativecommon:swap_merges`), the changelog read marker (`config/creativecommon/changelog_seen.txt`),
  the JFR events (`creativecommon.*`; PerfParse, perf_report, report_html, session_report, `ce-server.jfc`), the
  `-Dcreativecommon.iwFastTick` property, the logger name and the mixin member prefix (`creativecommon$`).
- **Pack folder:** `kubejs/assets/creativeengineering/{changelog,jei_world}` moved to `kubejs/assets/creativecommon/`;
  `changelog_lint.py`, `build_deploy.py`, `test_changelog.sh`, `changelog_test/deploy_test.py`, `gen_jei_world.py`,
  `jei_world/common.py`, `generators.json`, CLAUDE.md, `.gitignore` and the add-mod and scratch-test skills follow.
  Deployed changelog files moved unedited; build_deploy's removals handle the old paths on other machines.
- **Kept `creativeengineering`** (another jar owns them): `sample_syringe`, `magic_resistance`, the
  `dragon_scale_elytra` recipe, Equipment's `trimmable_armor` tag, the Engineering display-name entry, the old
  config file names in the carry-over calls.
- **Moved out:** `MealSlotTooltip` and the two Create Central Kitchen meal-slot mixins to CD
  (`compat/`, compartment `mixin/create_central_kitchen/`); `JetpackItemTooltipMixin` (gated `create_dd`) and
  `FallingBlockWaterSplashMixin` to Compat; `CuriosSlots` and `CuriosSlotTooltip` to Equipment with
  `[tooltips] curiosSlotLine` (now in `creativeequipment-common.toml`); the walk-speed lang key to Engineering, the
  copper tier key to Equipment, the unused `ie_sawblade_blade.png` and the never-loaded `lang/.en_us.json` to
  Engineering; ten empty package folders deleted. Common: 70 mixins (30 common, 40 client).
- **Effects on an existing install:** the item merges run once more on an old world; What's New shows once; WTHIT
  plugin toggles, JEI category order and a changed `curiosSlotLine` fall back to defaults.
- **Why some compat stays in Common:** Common holds the interface rows (2.4: JEI, WTHIT, tooltip and GUI fixes), the
  APIs content jars fill (`RemainderView`, `PottedPlantHolder`, `JarContents`, `DamageTags`, `TradeListings`,
  `BurnBursts`) and engine/perf fixes even when they target a mod (IW growth, Create: Gravity, Quark spiral spires,
  Nock Enough Arrows whistles). Compat holds behaviour tweaks to third-party mods with no system of their own.
- Built: Common, Compat, CD, Equipment, Brewing, Engineering. Scratch SQ-CNS-01 to -06.

### 7.15 Dependency trace: Common, Delight, Brewing (2026-11-04; fixes built, NOT installed, not booted, not tested)

Owner request: trace the dependencies after the split, Common through CD to CB (the other jars later). Method: a
scan of all 2090 in-tree classes for cross-module references (imports, qualified names, mixin targets, class-name and
mod-id strings), a `global.X` define/use scan of every jar script pack and the pack folder, and the Gradle deps,
mods.toml, mixin configs and nested jars of the installed and built jars.

Map (hard deps on our mods; soft = guarded or by name):

| Jar | Hard | Soft |
|---|---|---|
| Common | none | Exposure (toast class name), Brewin' coaster and Vinery namespace (WTHIT), Farmer's and Respite (WTHIT, `ModList`), Salt ids (JEI World tab), `global.seasoning` (guarded) |
| Compat | Common | |
| Calendar | Common, Compat | gated mixins into Vinery, Vintage, Nether's; Vinery clock by reflection |
| CD | Common, Compat, Calendar, Farmer's (member) | `global.CWG`, `global.biomeRules` (Worldgen, guarded) |
| CB | Common, Compat, Calendar, CD | `global.CD` (guarded) |

Clean: no package in two modules; every Common -> CD edge of 7.2 is gone (zero Common -> CD class references), and CB
-> Equipment (`DamageTags`) too; every listed mixin class exists; jar scripts read the pack dicts at top level safely
(order: Common's pack, the pack folder, then jars in mod-list order).

Fixed (owner, 2026-11-04):
1. Blockheads Additions (CD) -> Brewin' (CB): `CoasterRendererOffsetMixin` was an ungated hard target in a required
   config. Now gated by `BlockheadsAdditionsMixinPlugin` (shared `GatedMixinPlugin`, table form) on
   `brewinandchewin`, with an optional mods.toml dependency. Member `13.3.4-ce-19.2-02`.
2. Common's `compileOnly project(':backportedBrewin')` and `':backportedDisplay'` removed: stale since the coaster
   mixin and the dish blocks moved to CD; they inverted the build graph.
3. Vinery member declares `creativecalendar` (mandatory, AFTER): `PackTime` imports `WineTime`. Member `1.5.5-ce-19.2-09`.

Left for the owner: the food and brewing WTHIT providers in Common (soft, but CD's and CB's by ownership); CD and CB
require Compat with no code or script using it. Install note: the swap's jars all require `creativecommon`, so
Common, Engineering 2.0.0, Compat, Calendar, CD and CB go into `mods/` together. Scratch SQ-DEP-01 to -03, CQ-DEP-01.

### 7.16 Dependency trace part 2: every jar (2026-11-04; fixes built, NOT installed, not booted, not tested)

Owner request: finish the trace for the jars 7.15 left out (Engineering, Equipment, Magic, Worldgen with Baby Mobs and
More Mooblooms, Devtools, Labels, Exposure, Storage Delight) and re-check the whole set for broken imports. Method,
on top of 7.15's class and `global.X` scans (rerun over all 2091 classes and 619 scripts):

- `./gradlew build --offline` of every project: all compile, so no in-tree import is unresolved.
- Built jars (nested members included) and the 410 `mods/` jars read for mods.toml deps; the post-install set
  (built jars + every `mods/` jar whose mod ids no built jar provides) solved: no missing mandatory dependency.
- Every import of a third-party package checked against the importing jar's mandatory deps; each hit is a gated mixin,
  a JEI or WTHIT plugin class (`required` set in `waila_plugins.json`), a KubeJS plugin class, or reached only behind
  `ModList.isLoaded`, except fix 2.
- Every mixin config: listed classes exist (`$Inner` and `development/shared/` ones included); third-party targets are
  mandatory deps, gated by plugin table or compartment, or (Common, Baby Mobs) ungated against installed mods, where a
  missing target only logs a Mixin warning.
- Tag reads by our namespaces in Java and scripts checked against their producers (jar data, pack tag scripts and
  dicts), and every wave 9 `creativecommon:` id checked for a `creativeengineering:` leftover: fix 1.

Map (hard deps on our mods; soft = guarded or by name; third-party optional deps in mods.toml not listed):

| Jar | Hard (ours) | Hard (third-party) | Soft (ours and code-guarded) |
|---|---|---|---|
| Common | none | KubeJS | as 7.15 |
| Compat | Common | | Vinery (`VineShearing` class name); Cozy's Improved Cats, Domestication Innovation (`ModList`) |
| Calendar | Common, Compat | | as 7.15 |
| CD | Common, Compat, Calendar, Farmer's (member) | | as 7.15 |
| CB | Common, Compat, Calendar, CD | | `global.CD`; Create, IE (fix 2) |
| Engineering | Common, Compat | KubeJS | Vinery (`StuckInBlock` class name) |
| Equipment | Common, Compat | | none |
| Magic | Common, Compat | | none |
| Worldgen | Common, Compat | | none; CD reads `global.CWG`, `global.biomeRules` (guarded) |
| Baby Mobs, More Mooblooms (Worldgen) | none | | two Baby Mobs client mixins ungated (Cozy's, More Mob Variants, both installed) |
| Devtools | Common, Compat | | Worldgen (`MassPlacer`, `SpawnDump.tuning`, `PerfCommand` behind `isLoaded`) |
| Labels | none | Labels, Moonlight, Sticky It | |
| Exposure | none | | KubeJS plugin classes (loaded by KubeJS only) |
| Storage Delight | none declared | | Farmer's Delight classes (always present in CD) |

No upward or sideways class edge outside this table: Engineering, Equipment, Magic and Worldgen have no unguarded
third-party class, and no jar reads a `global.X` nobody defines.

Fixed (2026-11-04):
1. (Superseded by 7.17: owner moved the tags to `creativecommon` on both sides.) Campfire rain tags: the swap had set Common's `CampfireRainInfo` keys to `creativecommon:campfire/rain_douses` and
   `rain_proof_fuel`, while Compat's and CD's tag scripts fill `creativeengineering:` (`block_dict.js`
   `t_campfire_rain_*`). With the new jars, rain would never douse a campfire or grill, and the WTHIT and JEI lines would
   read empty tags. Back to `creativeengineering` (wave 9 rule: another jar fills them; HEAD had that name). Common rebuilt.
2. CB `commonSetup` ran `DragonBreathBrewing.build` (Create classes) and `Lingerification.registerListGenerator` (IE
   and Create) unconditionally, and Create and IE are not CB dependencies: without them, CB would crash at common setup
   (matters for the CurseForge build, not this pack). Now behind `isLoaded`. CB rebuilt.
3. Build: `:backportedVinery:jar` failed its refmap check after an incremental compile (`vinery.refmap.json is not in`
   the jar); a clean build of the member fixed it. Clean-build Vinery whenever its jar task reports this.

Left for the owner (both done in 7.17): Common's ~40 ungated third-party mixins (JEI, AE2, FTB, Lightman's Currency, WorldEdit and others)
would only warn if those mods left the pack, so they could take `NEEDS_MOD` rows if Common is ever published alone;
Storage Delight could declare `farmersdelight`. The stale `_readme` in `kubejs/generation_scripts/wthit/silk_touch_blocks.json`
updates on the next `/cdevgen run silk_touch_blocks`. Scratch SQ-DEP-04 to -06 (same boot as SQ-DEP-01).

### 7.17 Dependency audit follow-up (2026-11-04; built, NOT installed, not booted, not tested)

Owner, 2026-11-04, on 7.16:
1. **Campfire rain tags to `creativecommon`, both sides.** `CampfireRainInfo` keys are
   `creativecommon:campfire/rain_douses` and `rain_proof_fuel` again, and `block_dict.js` `t_campfire_rain_douses` /
   `t_campfire_rain_proof_fuel` follow, so Compat's and CD's tag scripts fill the new names. Comments, the CD config
   comment (`[campfire]`), wthit_lines.md, barbeques_delight.md, creative_delight.md and SQ-BBQ-03 follow. **Install
   note:** `block_dict.js` is a pack startup script, so after a restart with the old jars (patcherMod 1.1.09 reads
   `creativeengineering:`) rain stops dousing campfires until the new Common goes in; install with the ten jars.
2. **Common's third-party mixins gated.** `CEMixinPlugin.NEEDS_MOD` has a row for every Common mixin whose target lives
   in another mod: 40 rows (Immersive Weathering x2, Armor Trims, IE, WorldEdit x2, Quark, Create: Gravity, Nock Enough
   Arrows, Create x2, Detailed Armor Bar x2, JEI x11, Lightman's Currency x2, Iceberg, Pretty Pipes, FTB Library x4,
   FTB Quests x2, Wares, Patchouli, AE2 x4). Ids checked against the installed jars' mods.toml; every key is a listed
   mixin. With all of them installed nothing changes; without one, its mixins are skipped instead of warning.
3. **Storage Delight requires Farmer's Delight** (`farmersdelight`, `[1.19.2-1.2.4,)`, AFTER), as the CD members do.
   `repackedStorage` `-02`.

Built: Common, CD (config comment), Storage Delight `-02`. No player-visible change (the namespace move ships
with the swap jars), so no changelog bullet. Scratch SQ-DEP-05 (updated), -07, -08.

### 7.18 Feature regression after the split (2026-11-06; scratch r1106spf: PASS 33, FAIL 2)

The carve rows check that each jar boots and carries its config; scratch section "SQ-SPF" checks each jar's
features on the builds of 2026-11-06 18:51-18:54 against a baseline boot on the jars in `mods/`: SQ-SPF-00 (id, tag
and recipe diffs), Common -01 to -05, Compat -10 to -14, Calendar -20 to -22, Devtools -30, -31, Magic -40, -41,
Equipment -50 to -52, Engineering -60 to -62, Worldgen -70 to -73 (fresh normal world), Delight -80 to -83, Brewing
-90 to -92, Labels -100, cross-jar -110 (config files), -111 (one jar removed at a time); client CQ-SPF-01, -02. Rows
marked RERUN repeat the earlier feature suites on the split jars. TODO `creativesplit/regression #14`.

Run r1106spf (2026-11-06 night, [scratch_run_2026-11-06.md](testing/scratch_run_2026-11-06.md) 6): every jar's
features as before the split (recipe counts per type, tags and items equal to the 11-05 dumps apart from the
intended fluid rename and mead removal). FAIL: SQ-CBP-03b (horror lasagna empty filling step), SQ-CBP-08b (Equipment
felsic assembly step; `felsic_gear` pack names Nullscape biomes). Findings for TODO `creativesplit/regression #15` to
`#18`. Worldgen rows partial; CQ-SPF-01, -02 open.

Scratch r1106spfx ([scratch_run_2026-11-06.md](testing/scratch_run_2026-11-06.md) 7): SQ-SPFX-02 to -07 PASS, SQ-SPFX-01 FAIL
(Immersive Portals fix too late in Mixin). Fixes the same night (owner request; all ten Creative jars built 22:07-22:09, not installed; queue SQ-SPFX-01 to -07,
CQ-SPFX-01): Common sets ChunkMap `m_183760_` back to private for Immersive Portals with Serene Seasons; NCH also checks a
sequenced-assembly step's JSON for the empty fluid and drops the steps of a skipped assembly; horror lasagna and the felsic
ingot assembly guarded with `global.has`; Equipment's Nullscape felsic rows in `packs/felsic_gear+nullscape/`; `// requires:
<own jar>` on every script of Compat, Calendar, Magic, Equipment, Engineering, Delight and Brewing (346 files; Devtools and
Worldgen had it), Devtools' two Worldgen dumps also `requires: creativeworldgen`; 17 dead dict ids removed (Vinery cherry
set, mead, King Danis wine, IE JEI buckets, TMTED items, More Immersive Wires redstone parts, Create Questing blueprint)
and the dict validators skip absent mods and globs; sky islands in group `sky`.

### 7.19 Interface carve and owner moves (2026-11-07; built, NOT installed by this session, not booted, not tested)

Owner notes of 2026-11-07, with the answers to the conflicts found:

| Note | Done |
|---|---|
| Create: Gravity changes to Engineering | **Kept in Common** (owner): its only change, `CreateGravityDimensionMixin`, is a perf fix under `[perf]`, and perf stays in Common. |
| Immersive Engineering changes to Engineering | **Kept in Common** (owner): `FluidTagInputMixin` is a shared API; Brewing's potion bottling recipes need it and Brewing does not depend on Engineering. The IE sheetmetal tank WTHIT line went to Compat with the rest of WTHIT. `multiblock_dict.js` stays (dicts are Common's). |
| Detail Armor Bar and its tweaks to Equipment | `compat/DetailArmorBarCompat`, `ArmorOverflowRenderer`, `ArmorBarRendererAccessor`, `ArmorBarRendererOverflowTickMixin`, `[armor_overflow]`, the 12 `*_tint.png` sheets (`creativeequipment:textures/gui/`), the `detailab` dependency and its range. |
| UI, WTHIT and JEI changes to Compat | 89 classes: all of `compat/jei/**`, `compat/guititle/`, the Wthit* classes, `StateLines`, `BlockModelIcons`, `RemainderView`, `JarContents`, `PottedPlantHolder`, `PotionFluidTooltip`, `StringEntry`, the AE2 / FTB / Lightman's tooltip stacks, `CompoundEffectTooltip`, and 35 client mixins (JEI, tooltip fixes, Iceberg, title plaques, toasts, Create config widget, JEED, HUD effect icon). `[jei] [gui_title] [toasts] [tooltips] [coaster] [wthit]`, 64 lang keys, all 13 WTHIT plugin entries, the optional dependencies and tested ranges. |
| Effect UI mixins to Compat (owner answer) | `JeedEffectInfoMixin`, `JeedEffectRendererMixin`, `GuiEffectIconMixin`, `CompoundEffectTooltip`. The rules they read (`EffectRules`, `CompoundEffects`, `[effects]`) stay in Common. |
| Performance changes stay in Common | Unchanged: `perf/`, the perf mixins, IW growth, WorldEdit, Quark spires, NEA whistles, Armor Trims cache, creative menu disable. |
| Better Beacons potions to Brewing | `potion/BetterBeaconsPotions` registers `creativebrewing:long_reach`, `long_long_reach`, `strong_long_reach`, `phantom_bane`, `long_phantom_bane` (owner: rename, reverses plan_cb_polish.md 2 (a)/(b) for these five). Remap rows moved from Common's `effects_potions.json` to `data/creativebrewing/swaps/better_beacons_potions.json`; `potions.js` and the NCH Long Reach filling recipe use the new ids. Phantom Bane's rules (rest reset, spawner skip) stay Common effect rules. |
| Farmer's Respite Caffeinated to Delight | `mixin/farmersrespite/CaffeinatedEffectMixin` (table row with the old range in `CDMixinPlugin`) and `compat/farmersrespite/CaffeinatedPhantomBane` (the Caffeinated -> hidden Phantom Bane half of `PhantomBaneRules`). The compound description row stays in `CompoundEffects`. |
| Wandering trader to a future economy mod | Marked only (2.3 row, file headers). |

Ids kept so nothing resets: lang keys `creativecommon.wthit.*` / `creativecommon.jei.*` (now in Compat's lang file).
The WTHIT plugin ids, JEI uids and categories and `state_lines.json` first kept `creativecommon:` too; reversed the same
day, see 7.20. Config: Compat and Equipment merge the sections from `creativecommon-common.toml` (then from the
pre-swap `creativeengineering-common.toml`) at construction; Common drops them when Forge corrects its file.
Build: Brewing, Delight and Devtools compile against Compat (they already required it in `mods.toml`). Every project
compiles (`./gradlew compileJava --offline`), the seven jars `:build`. Tests: scratch section SQ-CIF (-01 to -04,
CQ-CIF-01). `configs_edited/` copies are untouched: the merge moves the values on first boot; refresh the backups from
`config/` after that boot.

### 7.20 Ids that named the wrong owner jar, `ce` names outside Engineering (2026-11-07; built, NOT installed, not booted, not tested)

Plan and full lists: [plan_owner_ids.md](plan_owner_ids.md) (D1 and D2 taken as recommended). Only ids not saved in
worlds moved; registered ids, NBT keys, capability ids and the stepstool keybind keep their names (rule 1).

- Fixed: `TitlePlaques` (Compat) and `ArmorOverflowRenderer` (Equipment) kept Common's mod id in
  `@Mod.EventBusSubscriber` after 7.19, so Forge never registered them.
- WTHIT plugin and line ids `creativecompat:*` (Compat, 15) and `creativecalendar:crop_seasons`; their
  `config.waila.plugin_*` lang keys follow (remainder_buffer and pot_contents got names, they had none).
  `config/waila/plugin_toggle.json5` renamed, 14 dead `creativeengineering:` rows dropped, backup added in
  `configs_edited/root_config/waila/`.
- JEI uids and categories: Compat `creativecompat:` (3 uids, 19 World tabs), Delight `creativedelight:` (4 uids, Canning,
  Can Uses, Feast Plating, Seasoning); `creativeengineering:baking` kept (mirrors the recipe type).
  `recipe-category-sort-order.ini` and its backup carry the new ids, World tabs back after Tipped Arrows.
- Channels `creativeequipment:stepstool`, `creativecompat:sack_bundle`. `state_lines.json` moved to Compat's
  `assets/creativecompat/`. Empty `assets/creativeengineering/` dropped from Calendar and Worldgen.
- Commands `/ccswap /ccaltime /cwglocate /cwgspawnbox /cdevgen /cdevplace /cdevperf /cdevdumpnames /cdevdumpicons`;
  Devtools members `cdevcapture` (`DevCapture`, `-Dcdevcapture.plan`) and `cdevperfbot` (`DevPerfBot`,
  `-Dcdevperfbot.out`); package `creativedevtools.cdevplace`; log `logs/cdev_block_state_ranges_<side>.txt`, generator
  logs `logs/cdevgen/`. Tools, skills, CLAUDE.md, memory and live docs use the new names; dated plans, scratch run
  logs, run batches and deployed changelogs keep the old ones.
- Classes `CommonMixinPlugin`, `CommonPotions`, `CEQTiers`; constants `CD_`, `CWG_`, `CEQ_`, `COMPAT_`; the JS list in
  Brewing's `potions.js` is `CB_POTION_MIXES`.
- Guard: `tools/id_owner_lint.py` (0 flagged).

Build: the nine jars `:build` OK, MassPlacerTest 30/30. Tests: scratch section SQ-IDS (-01 to -03, CQ-IDS-01 to -06).

## 8. History

- 2026-11-07, owner: interface (JEI, WTHIT, tooltips) moves to Compat, Detail Armor Bar to Equipment, Caffeinated to
  Delight, Better Beacons potions to Brewing; Gravity and the IE fluid input stay in Common; wandering trader marked for a
  future economy mod (7.19).

- 2026-11-02, owner: Creative Split deploys as 1.2.00; the Creative Delight cutover goes in the next deployment before it.

- 2026-11-01: plan written (owner request). Measurements in section 1. Nothing audited, nothing moved.
- 2026-11-01, owner notes:
  - The interface functions go into Common.
  - The Devtools jar is approved.
  - Fauna features go to Worldgen. The trough code, babyMobs, moreMooblooms and incubationAdditions go to CD.
  - Creative Magic and Creative Computers are marked as future mods.
  - No underscores in mod names.
  - Content is carved first, and Common is the remainder.
  - Scripts follow the CD and CB approach.
  - The Interface and Fauna jars are dropped, and the old order question is settled.
- 2026-11-01, audit start: Q3, Q4, Q5 closed (Common, Common jar, Devtools). Inventory files go in `split/`, section 7
  holds the merge. No source headers for reservations. Sweeps A to G run in parallel.
- 2026-11-01, audit done: section 7 filled (sizes, 25 edges with fixes, carve order with a prep step 0, remainder
  check, 16 owner rows). Reservation docs creative_magic.md and creative_computers.md written. Awaiting owner sign-off
  on the 7.4 rows and Q1, Q2, Q6, Q7 before any execution doc.
- 2026-11-01, owner sign-off (7.6): Compat is required by every jar; Magic is carved now and Computers folds into
  Engineering; CD and CB lose the underscore; every 7.4 row is settled; CB is the first publishing goal. Execution
  starts with Devtools and Calendar on branch `creative-split`.
- 2026-11-01, wave 2 (7.7): CD and CB renamed with their ids, swap lists and remapper built in Common, prep step 0
  done. Owner changed Q2: the underscored ids move too, through the swapper.
- 2026-11-01, wave 3: Creative Compat carved ([creative_compat.md](creative_compat.md)); patcherMod 1.1.06 installed with
  Compat, CD, CB, Calendar, Devtools. Scratch SQ-CS2 and SQ-CMP queued. Next: wave 4 (CB phases 1 to 3, then CD).
- 2026-11-01, wave 4 (7.8): CB phases 1 to 3 and the CD carve built (patcherMod 1.1.07), not installed while waves 2
  and 3 are untested. Next: test waves 2 and 3, install wave 4, then wave 5 (Worldgen with fauna and More Mooblooms).
- 2026-11-01, scratch run of waves 2 to 4 ([scratch_run_2026-11-01.md](testing/scratch_run_2026-11-01.md) 7): one save
  taken from the 1.1.05 jars through 1.1.06 to 1.1.07; three bugs fixed (7.8). Next: install wave 4, client rows.
- 2026-11-01, wave 4 close: `harvest.js` goes to Equipment; wave 4 jars installed to the dev instance; client tests pinned until wave 8+.
- 2026-11-02, wave 5 (7.10): Creative Worldgen carved with fauna, More Mooblooms and Baby Mobs; built, not installed. Next: SQ-CWG, then wave 6 (Magic).
- 2026-11-02 23:16, wave 5 installed by the Creative Delight plan-K session (plan_creative_delight.md K): `creativeworldgen-1.0.0.jar` and patcherMod 1.1.08 went into `mods/` with the new CD jar, because 1.1.08 (swappers) is built from the wave-5 tree and the Miner's loot rules use Worldgen's new `loot/09_drops.js`. moreMooblooms-1.1.0.jar displaced. Not booted; SQ-CWG still open. Revert: put back patcherMod 1.1.07 and moreMooblooms from the `jar_archive/displaced/` folders and remove the Worldgen jar (then CD's Miner's drops log an error).
- 2026-11-04, waves 6 and 7 (7.11, 7.12): Creative Magic and Creative Equipment carved; patcherMod 1.1.09. Installed 19:17 with a clean-built CD jar (the parallel cdfinish session's campfire move and stoves damage tag included). Next: SQ-CMG and SQ-CEQ, the D3 decision, then wave 8 (Engineering).
- 2026-11-04, wave 8 (7.13): D3 to Engineering (owner). Creative Engineering carved and patcherMod swapped to Creative Common (`creativecommon`, `development/creativeCommon`); all ten Creative jars built, not installed. Next: install the ten jars together, SQ-CEN with SQ-CMG and SQ-CEQ, then the bulk client session.
- 2026-11-04, wave 9 (7.14): Common's data moved to the `creativecommon` namespace (outright), the pack's changelog and
  JEI world trees to `kubejs/assets/creativecommon/`; meal tooltips to CD, two fixes to Compat, Curios slots to
  Equipment. Built, not installed. Next: install the ten jars together, SQ-CNS with SQ-CEN.
- 2026-11-04, dependency trace part 2 (7.16): every jar traced, full build clean; campfire rain tag namespace and CB's
  Create/IE setup guard fixed, Common and CB rebuilt, not installed. Next: SQ-DEP-01 to -06 on the SQ-CNS boot.
- 2026-11-04, dependency audit follow-up (7.17): campfire rain tags moved to `creativecommon` on both sides, 40
  `NEEDS_MOD` gates for Common's third-party mixins, Storage Delight requires Farmer's Delight. Built, not installed.

#### Creative Common 1.1.0 test list (queued 2026-11-07; server rows run r1107cc the same evening)

Queue section "SQ-CC Creative Common 1.1.0 full check" in `testing/scratch_queue.md`: SQ-CC-01 to -16 (jar contents, full-pack boot,
Common-only boot, neutral and pack defaults, dict probe, scrap tags, Immersive Portals ordering, swap list, Magic Resistance and
Better Beacons, effect rules, trade sync, JFR events, pack settings, Gravity, stall fixes) and CQ-CC-01 to -06 (launch with
Heartstone, tip bar and moved Creative Tips, mod names, mirror glass, changelog viewer). Status: built, installed 2026-11-07 19:35,
scratch server PASSED: SQ-CC-01..11, -13..16 (r1107cc, [scratch_run_2026-11-07.md](testing/scratch_run_2026-11-07.md) 3, with
three NCH fixes made in the run). Open: SQ-CC-12 (owner client on a server), CQ-CC-01 to -06.
