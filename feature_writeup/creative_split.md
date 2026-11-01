# Creative split: audit plan for breaking patcherMod into high-level jars

Written 2026-11-01. **Status: audit done 2026-11-01, nothing moved; owner sign-off on 7.4 pending.** Owner, 2026-11-01: plan an audit of Creative
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
| **Creative Devtools** (`creativedevtools`) | `devtools/`, `/cegen`, `/dumpall` and the `server_scripts/_lib` dump scripts, `PerfCommand` and JFR events, the block-state and random probes. Kept in dev `mods/` only and left out of `build_deploy.py` player payloads (Q6). |

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
- **Interface** (owner, 2026-11-01):
  - JEI tweaks: `compat/jei`, the Jei* mixins, world tabs, ordering, craftable-first.
  - WTHIT plugins: the Wthit* classes, `state_lines.json`, `waila_plugins.json`.
  - Tooltips, toasts, GUI title plaques, the armor bar, and the Legendary Tooltips and Iceberg fixes.
  - The tooltip mixins for AE2, FTB, Lightman's Currency, Pretty Pipes and Patchouli.
  - `client_scripts/` tooltips, names and JEI order.

  An Interface row that only shows a content jar's data (a WTHIT line for an oven, a JEI category for a sawmill) goes
  with that content jar instead. The audit applies this test per row.

## 3. Rules the audit applies to every row

Taken from creative_brewing.md 4.1 and creative_calendar.md 3, extended to several jars:

1. **Ids stay.** Every registered id keeps the `creativeengineering:` namespace whichever jar registers it (a
   `DeferredRegister` for that namespace). No conversions (CLAUDE.md). Lang keys keep their names.
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
| `MassPlacer` (`/ceplace`) | Worldgen or Devtools | Worldgen (it is built on Worldgen internals) |
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

## 8. History

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
