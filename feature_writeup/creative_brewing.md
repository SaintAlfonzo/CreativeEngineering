# Creative Brewing: brewing, alcohol and potion add-ons in one jar

Written 2026-10-31 (owner request: "move the vinery backport and brewin and chewin backports into a parent mod much like
creative delight, called creative brewing ... eventually all of the potion changes made in the main mod and kubejs will be
moved here too, so plan the infrastructure for that as well").

**Status: phases 2 and 3 (potion, effect and machine Java, mixins, config) BUILT 2026-11-01 (Creative Split wave 4, 4.6),
not installed, not booted; install with patcherMod 1.1.07 (section 5). Earlier status: bundle BUILT 2026-10-31.**
`creativebrewing-1.0.0.jar`

> **Testing rule (owner, 2026-11-01):** Vinery and Brewin' are a package deal under Creative Brewing. They are never booted or tested separately: no without-Brewin' / without-Vinery boot, no per-member switch check (`[members]` toggles), no standalone port jars. The pack does not start without Vinery anyway (`startup_scripts/_lib/effect_dict.js` needs `vinery:climbing_effect`). Scratch rows SQ-CDF-07 (second half) and SQ-CBJ-05 were dropped for this reason.
(`development/creativeBrewing/build/libs/`) nests the Vinery port `-08` and the Brewin' port `-05`. `mods/` still holds the
standalone `backportedVinery-1.5.5-ce-19.2-06.jar` and `backportedBrewin-4.5.0-ce-19.2-04.jar` until
`./gradlew :creativeBrewing:installToMods` (game closed). Scratch checks SQ-CBJ queued (section 6). The framework for the
later moves (section 2) is in place with no feature moved yet; the move plan is section 4.

Written for: the owner, as the working reference for the Creative Brewing moves. Same shape as Creative Delight
([creative_delight.md](creative_delight.md)); this file only records where CB differs or what is specific to it.
Ports' own content and tests stay in [alcohol.md](alcohol.md) (Part A Brewin', `AL-`; Part B Vinery, `VB-`).

## 0. At a glance

| Member | Gradle project | Folder | Mod ids | Display name | Version |
|---|---|---|---|---|---|
| Let's Do Vinery 1.5.5 port | `:backportedVinery` | `development/creativeBrewing/members/vinery` | `vinery`, `foundation` | "Vinery (Creative Brewing)", "Foundation (Creative Brewing)" (the `foundation` id; both renamed 2026-11-04, plan_cb_publish.md M5) | `1.5.5-ce-19.2-08` |
| Brewin' and Chewin' 4.5.0 port | `:backportedBrewin` | `development/creativeBrewing/members/brewin` | `brewinandchewin` | "Brewin' & Chewin' (Creative Brewing)" | `4.5.0-ce-19.2-05` |
| (the bundle itself) | `:creativeBrewing` | `development/creativeBrewing` | `creativebrewing` | "Creative Brewing" | `1.0.0` |

Version bumps: Vinery `-07` was already built (in `jar_archive/`, never installed) and the tree has uncommitted work since,
so the first nested build is `-08`; Brewin' `-04` is the installed one, so `-05`. Both carry only the display-name change
on top of their last builds.

## 1. What moved (2026-10-31)

- `development/backportedVinery` -> `development/creativeBrewing/members/vinery`, `development/backportedBrewin` ->
  `.../members/brewin`. Gradle project names unchanged, so `compileOnly project(':backportedBrewin')` in patcherMod and
  Blockheads Additions still works and `./gradlew :backportedBrewin:compileJava` still runs. A member's own
  `installToMods` now stops with "ships nested in creativeBrewing's jar" (ce.forge-mod, `bundled_in`).
- Each member: `bundled_in=creativeBrewing`; `mod_name` "(Creative Brewing)" label (0.3 of creative_delight.md, same rule);
  `../patcherMod/libs/` compile paths now `rootProject.file('development/creativeCommon/libs/...')`; Brewin's
  `tools/check_data.py` and `tools/port_resources.py` find the instance root four levels up.
- `.gitignore`: the member build/libs/jar_archive rules are now `/development/*/members/*/...` (CD and CB).
- `settings.gradle`: the two projects left the flat list; a Creative Brewing block includes the bundle and its members.
- `tools/build_deploy.py`: `IN_TREE_MODS` has `creativeBrewing` / `creativebrewing` in place of the two ports;
  `IN_TREE_EXTRA_SRC` watches the members and CB's script trees; the CB script trees are dev-only (ship in the jar);
  `vinery_upstream` donors are now required by the `creativebrewing` jar and pinned from
  `creativeBrewing/members/vinery/.../UpstreamJars.java`; the duplicate-mod-id check treats CB members like CD's.
- `tools/scratch_query/prep_srv.sh`: swaps in `creativebrewing-1.0.0.jar` and drops the standalone port jars beside it.
- `tools/placeable_catalog.py`: coaster model path.
- `mod_sides.md`: one Creative Brewing row replaces the two port rows.
- Names (owner, 2026-10-31): `foundation` displays as "Creative Brewing"; Brewin' uses the pack's "&". CEConfig
  `[mod_names] overrides` lost its `vinery`, `brewinandchewin` and `foundation` entries (they would hide the mods.toml
  names) and every "(Alfonzo)" tag (names.md 12); `config/jei/ingredient-list-mod-sort-order.ini` follows (it is keyed
  by display name). Both copied to `configs_edited/`.
- Not changed, still correct: `vinery_upstream/` donors (read from the game folder, not the jar path; no member code reads
  its own jar path, checked); every `vinery:` / `foundation:` / `brewinandchewin:` id; the members' configs
  (`config/vinery-common.toml`, `brewinandchewin-common.toml`); deploy history and released plans (they record what
  shipped).

## 2. The framework (built 2026-10-31, nothing using it yet)

### 2.1 Build

`ce.forge-bundle` like CD (`build-logic/src/main/groovy/ce.forge-bundle.gradle`): the own jar plus each member under
`META-INF/jarjar/` with a generated `metadata.json`. `development/creativeBrewing/build.gradle` copies the pack-folder
script trees into the jar and writes `creativebrewing_members.txt`; the jar task fails if any of the three script dirs
has no `.js`. `replaces_jars` lists the old standalone port jars and what each port replaced, so the first install
moves `backportedVinery-*`, `backportedBrewin-*` out of `mods/` (to `development/creativeBrewing/jar_archive/displaced/`).

### 2.2 Load order and dependencies

`mods.toml`: `creativeengineering` (KubeJS, helpers, startup loader), `creativecalendar` (the `[time]` master the Vinery
member's `PackTime` calls, since 2026-11-01, [creative_calendar.md](creative_calendar.md)) and `creativedelight` (nests
Farmer's Delight, Brewin's base; defines `global.CD`), all mandatory, all `ordering="AFTER"`. patcherMod's startup
loader runs jar packs in mod-list order, so the startup order is: patcherMod's pack, `startup_scripts/`, Calendar, CD,
CB. Check
`[jar-startup] startup script packs in order:` in `logs/kubejs/startup.log` (SQ-CBJ-02). Server and client script packs
from jars run in resource order, one pack per namespace, after the pack folder; event callbacks of all packs run in the
same passes (creative_delight.md 10.2). If a CB server script ever needs a CD row's result, do it in a later event
(`ServerEvents.highPriorityData` / `recipes` with a late `// priority:`) and check the server pack order in
`logs/kubejs/server.log` first.

### 2.3 Config: `config/creativebrewing-common.toml` (CBConfig)

- `[members]`: one switch per member id (`vinery`, `brewinandchewin`), default on; off makes CB's scripts skip that
  member's rows (`global.CB.on(id)`), the member's own data stays.
- `[brewing]`, `[alcohol]`, `[effects]`: one switch per moved feature. A feature is one row in `CBConfig.FEATURE_ROWS`
  (`{section, id, comment...}`); a section is written only once it has a row. Read with `global.CB.feature(id)` (scripts,
  inside an event callback) or `CBConfig.feature(id)` (Java).
- Dials that move from `creativeengineering-common.toml` keep their key names and comments in the section of their
  feature (4.3). Moved 2026-11-01 (phase 3): `[alcohol]` (quarkLine), `[effects]` (categoryOverrides, forceHudIcon,
  forceHudIconItems, remap, removed), `[magic_resistance]`, `[copper_poisoning]`, same keys, comments and defaults
  (`CBConfig.MOVED_SECTIONS`). `[alcohol]` and `[effects]` also hold that family's feature switches. On the first boot
  `CreativeBrewing` copies the four tables with the player's values from `creativeengineering-common.toml`
  (`ConfigRename.carrySections` when the CB file is new, else `ConfigRename.mergeSections`, which adds only the tables
  the CB file lacks); patcherMod then drops them from its file. When nothing was carried and the CE file still has them,
  CB logs a WARN naming them. `[coaster]` stays in patcherMod (WthitCoaster stays Common, creative_split.md 7.6), as do
  `[jei] potionBucketCategory` / `tippedArrowCategory` (CB's PotionPagesJEI reads them from `CEConfig`, a downward
  call) and `[perf] mountEffectIntervalTicks`.

### 2.4 Scripts

| Type | Authored in | In the jar | Core file |
|---|---|---|---|
| Startup | `development/creativeBrewing/src/main/resources/kubejs_startup/<modid>/` | same path | `kubejs_startup/creativebrewing/cb_core.js`: `global.CB` (`on`, `feature`, `members`, `recipes`) |
| Server | `kubejs/data/creativebrewing/kubejs/<modid>/` | `data/creativebrewing/kubejs/` | `_core/cb_server.js` (checks `global.CB` on load) |
| Client | `kubejs/assets/creativebrewing/kubejs/<modid>/` | `assets/creativebrewing/kubejs/` | `_core/cb_client.js` (checks `global.CB` on login) |

Rules, as CD's (creative_delight.md 29): one folder per mod the rows touch (`vinery/`, `brewinandchewin/`,
`minecraft/` for vanilla potions, `immersiveengineering/`, `create/`, `alexsmobs/`, ...), pack-native systems in
`creativeengineering/`; each file headed `// requires: <modid>`; member rows wrapped in an early return on
`!global.CB || !global.CB.on('<id>')`; recipes inside `global.CB.recipes(() => ...)`, so NCH writes
`creativebrewing:<type>/<suffix>` ids. Server and client scripts load from the pack folder on `/reload` / F3+T;
startup scripts need a CB rebuild and restart.

Guards (2026-11-04, plan_cb_publish.md M2, rules G1 to G5): the jar boots with only its hard dependencies. A row in a
pack-native file that names an optional mod's id (resolved through the dicts, not the folder name) sits in an
`if (global.has('<modid>'))` block or behind an early `if (!global.has('<modid>')) return`: `potions.js` (Alex's Mobs,
Buzzier Bees, Quark, Upgrade Aquatic, Better Beacons + Geode mixes), `armor_effects.js` and `tool_effects.js` (each effect
and armor or tool mod), `effect_swaps.js` (Farmer's Respite, Neapolitan), `wandering_trades.js` (Fruits Delight),
`names.js` (Dramatic Doors), `potion_fluid_jei.js` (Create). Rows handed only to `global.NCH` / `global.tags` need none
(the helpers skip absent mods' ids, G5): `jeed_providers.js` and `fluid_tags.js` carry `// G5` markers. Check with
`python3 tools/guard_check.py --jar creativebrewing` (0 findings on 2026-11-04, from 172).

### 2.5 Java

Package `com.SaintAlfonzo.creativebrewing`. `CreativeBrewing` (the `@Mod`: config, member log, the stale-jar ERROR line
of creative_delight.md 28.3), `CBConfig`, `compat/CBMixinPlugin` (the shared `GatedMixinPlugin`: a mixin in
`mixin.<modid>.` applies only when that mod is loaded; vanilla targets in `mixin.minecraft.`).
Mixins use SRG names with `remap = false` (no refmap), as patcherMod's do, so a mixin moves over unchanged apart from its
package. `compileOnly project(':patcherMod')` is there for CEConfig and pack APIs, plus patcherMod's `libs/` (the target
mods' jars); add `:creativeDelight` the first time a compartment needs CD Java.

Since phase 2 (2026-11-01): `potion/` (CEPotions, CompoundEffects, CompoundEffectTooltip, CopperPoisoningHandler,
EffectCategories, EffectRules, MagicResistanceEffect, PhantomBaneRules, PotionBucketDispenser), `fluidbrewing/`
(DragonBreathBrewing, Lingerification, PotionMixDedup), `compat/jei/` (AutomatedPotionMixingCategory,
LingerificationCategory, PotionPagesJEI, and `PotionMachinesJEI`, the `@JeiPlugin` that registers the first two, split
out of patcherMod's DragonBreathingJEI), `alcohol/IntoxicatingDrinkTooltip`, `trader/WinemakerTrades`. Ids are `creativebrewing:` (`CreativeBrewing.NS` = the
mod id since 2026-11-04, plan_cb_publish.md M4; they were `creativeengineering:`, and the 30 KubeJS potions `kubejs:`).
The 41 old -> new entries (1 effect, 38 potions, 3 buckets; Magic Resistance's effect and potion share one) are in
`data/creativebrewing/swaps/creative_split.json`. Potion ids saved as NBT strings (bottles, tipped arrows, bucket
contents, Create and IE potion fluids) are not remapped (decision D5): those made before read as Uncraftable Potion. `CreativeBrewing` registers CEPotions, the three FORGE-bus
handlers and the three setup tasks that `CreativeEngineering` used to. 25 mixins in compartments: `minecraft` (4),
`create` (6), `immersiveengineering` (8), `jeed` (2), `create_deep_dark` (2), `everythingcopper`, `farmersrespite`,
`fruitsdelight` (1 each); 14 common, 11 client. Resources (all `creativebrewing`): 12 lang keys in `assets/creativebrewing/lang/en_us.json` (JEI page titles, the
intoxicating drink word, the compound effect parts, Magic Resistance's name and JEED description),
`data/creativebrewing/recipes/lingerification_list.json`, and since M3 (2026-11-04) the potion bucket textures and
models, the Magic Resistance icon and the 206 coaster models (`assets/creativebrewing/brewinandchewin/coaster/`, from
`tools/gen_coaster_models.py`; the Brewin' port skips a file whose item is not registered). Scripts load three of these classes by name: `com.SaintAlfonzo.creativebrewing.potion.
PotionBucketDispenser`, `.fluidbrewing.DragonBreathBrewing`, `.trader.WinemakerTrades`.

## 3. Vinery donors

The `vinery_upstream/` donor folder works as before: the game stops with a message if the two pinned jars are missing.
Deploy now ties the folder to the `creativebrewing` jar (`UPSTREAM_DONORS`).

## 4. Move plan: potion, alcohol and effect code into CB

Inventory taken 2026-10-31 (read-only sweep of patcherMod and the pack folders; full list in section 7). Owner decision
2026-10-25 (TODO `creativebrewing/move #1`): potion brewing, alcohol, Vinery, effect descriptions, changes and swaps go
to Creative Brewing. Phases 2 and 3 were built on 2026-11-01 (4.6); phase 1 runs in the same wave (4.5 log).

### 4.1 Rules for every move

1. **Ids stay.** Potions, effects and items registered today as `creativeengineering:*` (long_reach, phantom_bane,
   magic_resistance potions; the magic_resistance effect; the potion buckets) keep that namespace when their
   registration moves into CB (a `DeferredRegister` for another namespace, as Ender's Delight Additions does for
   `endersdelight:`). No conversions (CLAUDE.md), so a new id would break worlds.
2. **Helpers stay shared.** NCH's `bncFermenting`, `frBrewing`, `ieMixer`, `ieBottling`, `jeedProvider` /
   `jeedPotionProvider` stay in neoCraftHelper.js; CB calls them. Dict keys (`global.item`, `global.fluid`,
   `global.effect`) stay in the pack dicts (one place per item).
3. **Top-level names.** Before moving a startup script into the jar, grep the pack for its top-level `var` names: each
   jar pack has its own scope, only `global` is shared (memory `kubejs-jar-startup-scripts`). Known globals that cross:
   `global.alcohol` (read by patcherMod `IntoxicatingDrinkTooltip`), `global.potionArea` (patcherMod
   `PotionBucketDispenser`, server `potion_buckets.js`), `global.foodEffectSwaps`, `global.effect`.
4. **Each moved file leaves no copy behind** and its old path gets a one-line entry in this section's log (4.5), so a
   later grep finds where it went. Mixed files are split: the potion rows move, the rest stays.
5. **Lang keys keep their names.** A key such as `creativeengineering.jei.potion_*` moves into CB's
   `assets/creativeengineering/lang/en_us.json` (resource packs merge lang per key).

### 4.2 Phases

**Phase 1: KubeJS only** (CB rebuild + restart; no patcherMod change). **Done 2026-11-01 (wave 4, move log 4.5; scratch
SQ-CBM).** Whole files:
`startup_scripts/cooking/potions.js`, `cooking/alcohol_drinks.js`, `cooking/food_values.js` (effect swaps and Respite
cake slices), `items/potion_buckets.js`, `func/potion_area.js`, `magic/magic_resistance.js`;
`server_scripts/cooking/alcohol.js`, `other/jeed_providers.js` (incl. the Vinery wine rows), `engineering/potion_bottling.js`,
`engineering/potion_buckets.js`, `equipment/tipped_arrows.js`, `magic/lava_vision.js`; `client_scripts/potion_buckets.js`;
from CD: `kubejs_startup/brewinandchewin/compost_values.js` and `kubejs_startup/vinery/compost_values.js`. Splits:
`tags/fluidtags.js` 75-84,
`client_scripts/tooltips.js` (Brewin' keg temperatures, Vinery section, goblet, flower box),
`client_scripts/namechanges.js` (Brewin' and Vinery names), `client_scripts/fluid_jei.js` 42-63,
`economy/wandering_trades.js` 76-89 (Winemaker stock) and `func/trade_helper.js` 93-114 (Winemaker helpers). Gear
effects (owner, 2026-10-31: to CB): `startup_scripts/func/armor_effects.js`, `func/tool_effects.js`,
`server_scripts/magic/armor_effects.js`, `magic/tool_effects.js`, `client_scripts/armor_effects_tooltip.js`,
`tool_effects_tooltip.js` (whole files; `jeed_providers.js` already reads `global.armorEffects` / `global.toolEffects`,
so both sides end up in CB). Generators whose output roots move (`gen_effect_descriptions.py`,
`gen_coaster_models.py`) get an `outRoot` in `tools/generators.json` (creative_delight.md 28.2).

**Phase 2: patcherMod Java** (patcherMod and CB rebuilds, both installed together). Whole packages `potion/` and
`fluidbrewing/`; `compat/` AutomatedPotionMixingCategory, LingerificationCategory, DragonBreathingJEI (the JEI page
part), PotionFluidTooltip, WthitCoaster, `jei/PotionPagesJEI`, `jei/world/PotionRows`; `food/IntoxicatingDrinkTooltip`;
the effect mixins (EffectRemap, UseItemIcon, GuiEffectIcon, MobEffectCategory, JeedEffectInfo, JeedEffectRenderer,
CaffeinatedEffect, CopperPoisoningEffect, CreateTeaPipeIcon, FruitsDelightEffectFunc, EchoArmorEffectProcedure,
EchoArmorTooltip), `potion/CopperPoisoningHandler` (gear effect, 4.4 Q3), `trader/WinemakerTrades` (owner,
2026-10-31; it calls the trader system's TradeListings, which stays: CB calls it, patcherMod gains no CB reference),
the potion machine mixins
(PotionRecipeGenerators, PotionHelper, PotionMixingRecipes, MechanicalMixerBlockEntity, PotionFluidHandler) and the
JEI potion mixins (Spout, ItemDrain, JEIHelper, Mixer, Refinery, BottlingMachine categories) into CB compartments
(`mixin.minecraft.`, `mixin.immersiveengineering.`, `mixin.create.`, `mixin.jei.`, `mixin.jeed.`, ...). CB then needs
its own `@JeiPlugin` and a `waila_plugins.json`. Registrations at `CreativeEngineering.java` 84-86, 156-165 and 376-379
move to `CreativeBrewing`. A Java class another patcherMod class still calls stays, or moves with an interface left in
patcherMod (patcherMod must not depend on CB: CB is optional to it).

**Phase 3: config.** `[effects]`, `[magic_resistance]`, `[alcohol]`, `[coaster]`, `[copper_poisoning]` leave `CEConfig` for `creativebrewing-common.toml` with the same keys. Edited values in
`config/creativeengineering-common.toml` and `configs_edited/` are copied over by hand in the same session (Forge would
otherwise write defaults), plus a startup WARN in CB when the old keys are still in the CE file.

**Stays in patcherMod (shared with other systems):** `shield/EffectTickContext` and `MobEffectTickContextMixin`
(shields; copper poisoning and the gear effects read it as a patcherMod API), the trader system (`TradeListings`,
`global.trades`), the Dragon Breathing fan type (potions.md lists it as not potions), `FluidTagInputMixin` (every IE fluid machine), `DisplayNameDump` (dev tool), `DrinkFluidEffects` /
`JeiFluidHelperTooltipMixin` (all drinks, not only alcohol), Central Kitchen's brewing guide tooltip (shares
`MealSlotTooltip` with the cooking guide). Each stays unless the owner says otherwise (4.4 Q4).

**Not CB: Creative Calendar.** `time/WineTime` and `[time.wine]` (the wine clock) moved to Creative Calendar with the
rest of the calendar, day and night, seasons and biome weather on 2026-11-01 ([creative_calendar.md](creative_calendar.md)).
Creative Brewing requires Calendar; the Vinery member's `PackTime` calls `com.SaintAlfonzo.creativecalendar.time.WineTime`
directly (`compileOnly project(':creativeCalendar')`).

### 4.3 What a move must check

- Startup potions register in registry events, which run after every startup pack, so moving `potions.js` after the
  pack folder is safe; anything reading its tables at top level is not (4.1 rule 3).
- JEED's provider JSON writer (`jeed_providers.js` end) writes files the client reads: keep its output path.
- `item_dict.js` rows for Vinery and Brewin' blocks stay (rule 2); only scripts move.
- Every move: SQ-CB scratch rows (to add per move) and the moved feature's own tests rerun (potions.md, potion_buckets.md,
  effects.md, alcohol.md suites).

### 4.4 Open questions (owner)

| # | Question | Recommendation |
|---|---|---|
| Q1 | Brewin' dishes and their food tags: CB or CD? | **Decided 2026-10-31 (owner): CD**, added only while Brewin' is loaded. Moved: CD `brewinandchewin/recipes.js` and `tags.js` (`// requires: brewinandchewin`), 4.5. |
| Q2 | Ciders (CD `farmersdelight/recipes.js` 142-155) and the sweetener passes that list `vinery:wine_fermentation` / `brewinandchewin:fermenting` (CD `minecraft/recipes.js` 18-40) | Leave in CD: not alcohol, and the sweetener pass must see every recipe type in one place. |
| Q3 | Copper poisoning (oxidised copper gear rolls Poison): effects (CB) or gear (stays)? | **Follows the gear effects into CB** (inferred from the 2026-10-31 decision that gear effects go to CB; confirm). |
| Q4 | Anything in the "stays" list of 4.2 to move anyway? | **Decided 2026-10-31 (owner):** Winemaker trades, Vinery effects and gear effects move to CB; the wine clock goes to Creative Calendar. The rest stays. |
| Q5 | Effects unrelated to potions or drinks (echo armor set bonus, AM effects, mob effect text in `jeed_providers.js` 35-59): all effect text to CB? | Yes: JEED providers move whole, and with the 2026-10-31 decision gear behaviour moves too. |

### 4.5 Move log

One line per moved file, `old path -> new path, date`. `PM` = patcherMod's Java root, `CB` = `development/creativeBrewing/
src/main/java/com/SaintAlfonzo/creativebrewing/`.

- Phase 2, 2026-11-01: `PM/potion/*` (9) -> `CB/potion/`; `PM/fluidbrewing/*` (3) -> `CB/fluidbrewing/`;
  `PM/compat/AutomatedPotionMixingCategory`, `LingerificationCategory`, `PM/compat/jei/PotionPagesJEI` -> `CB/compat/jei/`
  (new `PotionMachinesJEI` there holds the two pages' registration cut from `PM/compat/DragonBreathingJEI`);
  `PM/food/IntoxicatingDrinkTooltip` -> `CB/alcohol/`; `PM/trader/WinemakerTrades` -> `CB/trader/`; 25 `PM/mixin/*Mixin`
  -> `CB/mixin/<modid>/` (list in 2.5); patcherMod lang keys (10) -> CB `assets/creativeengineering/lang/en_us.json`;
  patcherMod `data/creativeengineering/recipes/lingerification_list.json` -> CB same path.
- Phase 3, 2026-11-01: `CEConfig` `Alcohol`, `Effects`, `Magic`, `CopperPoisoning` -> `CBConfig` (same keys).
- Phase 1 (scripts), 2026-11-01. `KJ` = `kubejs/`, `CBS` = `development/creativeBrewing/src/main/resources/kubejs_startup/`,
  `CBD` = `kubejs/data/creativebrewing/kubejs/`, `CBA` = `kubejs/assets/creativebrewing/kubejs/`. Recipe ids written by
  the moved files are now `creativebrewing:<type>/<suffix>` (were `creativeengineering:`; nothing referenced them).
  - `KJ/startup_scripts/cooking/potions.js` -> `CBS/creativeengineering/potions.js`
  - `KJ/startup_scripts/cooking/alcohol_drinks.js` -> `CBS/creativeengineering/alcohol_drinks.js`
  - `KJ/startup_scripts/cooking/food_values.js` -> `CBS/creativeengineering/effect_swaps.js` (renamed: only effect edits)
  - `KJ/startup_scripts/items/potion_buckets.js` -> `CBS/creativeengineering/potion_buckets.js`
  - `KJ/startup_scripts/func/potion_area.js` -> `CBS/creativeengineering/potion_area.js` (loads `creativebrewing.potion.PotionBucketDispenser`)
  - `KJ/startup_scripts/magic/magic_resistance.js` -> `CBS/creativeengineering/magic_resistance.js`
  - `KJ/startup_scripts/func/armor_effects.js`, `tool_effects.js` -> `CBS/creativeengineering/` (same names)
  - `KJ/startup_scripts/func/trade_helper.js` Winemaker helpers -> `CBS/vinery/winemaker_trades.js` (adds them to
    `global.trades`; loads `creativebrewing.trader.WinemakerTrades`)
  - CD `kubejs_startup/brewinandchewin/compost_values.js`, `vinery/compost_values.js` -> `CBS/brewinandchewin/`, `CBS/vinery/`
    (`tools/gen_compost_values.py` writes those two namespaces there now)
  - `KJ/server_scripts/cooking/alcohol.js` -> `CBD/brewinandchewin/alcohol.js` (keg, spout and drain) and
    `CBD/vinery/alcohol.js` (banner, stained glass); member switch guards
  - `KJ/server_scripts/other/jeed_providers.js` -> `CBD/creativeengineering/jeed_providers.js`
  - `KJ/server_scripts/engineering/potion_bottling.js`, `potion_buckets.js` -> `CBD/creativeengineering/`
  - `KJ/server_scripts/equipment/tipped_arrows.js` -> `CBD/creativeengineering/tipped_arrows.js`
  - `KJ/server_scripts/magic/lava_vision.js` -> `CBD/alexsmobs/lava_vision.js`
  - `KJ/server_scripts/magic/armor_effects.js`, `tool_effects.js` -> `CBD/creativeengineering/`
  - `KJ/server_scripts/economy/wandering_trades.js` Winemaker stock -> `CBD/vinery/wandering_trades.js`
  - `KJ/server_scripts/tags/fluidtags.js` potion and dragon's breath rows and the IE potion removal ->
    `CBD/creativeengineering/fluid_tags.js` (ink and XP stay)
  - `KJ/client_scripts/potion_buckets.js`, `armor_effects_tooltip.js`, `tool_effects_tooltip.js` -> `CBA/creativeengineering/`
  - `KJ/client_scripts/fluid_jei.js` create:potion stacks -> `CBA/creativeengineering/potion_fluid_jei.js`
  - `KJ/client_scripts/tooltips.js` keg heat lines -> `CBA/brewinandchewin/tooltips.js`; Vinery section and flower box ->
    `CBA/vinery/tooltips.js` (both read `global.tooltips`)
  - `KJ/client_scripts/namechanges.js` Vinery names and the Winemaker profession -> `CBA/vinery/names.js`
  - Not moved: generator outRoots (the effect lang overlay and coaster models stay in the KubeJS resource pack, 4.6);
    the keg heat and cold source block tags in `KJ/server_scripts/tags/blocktags.js` (Brewin' port rows, not on the
    phase 1 list).

- `kubejs/server_scripts/cooking/cooking.js` (Brewin' dishes, all rows) -> **Creative Delight**
  `kubejs/data/creativedelight/kubejs/brewinandchewin/recipes.js`, 2026-10-31 (Q1; ids now `creativedelight:`). The pack
  file is left empty.
- `kubejs/server_scripts/tags/cookingtags.js` (Brewin' tags) -> **Creative Delight** `.../brewinandchewin/tags.js`,
  2026-10-31. The pack file is left empty.

### 4.6 Phases 2 and 3 as built (2026-11-01, Creative Split wave 4)

Moved as 4.2 lists, with these changes from the plan (creative_split.md 7.6 and what the code showed):

- **Stay in patcherMod (Common):** `compat/WthitCoaster` and `[coaster]` (7.6: every WTHIT line in Common; it reads the
  Brewin' coaster by reflection, no CB class); `DragonBreathingJEI` without its two potion pages (7.6: fan is
  Engineering); `compat/PotionFluidTooltip`, `compat/jei/RecipeRows`, `RowPagesCategory` (shared JEI helpers: Common's
  RowPagesCategory attaches the potion fluid tooltip, CD's CanPagesJEI calls RecipeRows; `RecipeRows` is now public);
  `compat/jei/world/PotionRows` (expands `per_potion` rows of the JEI world tabs over every potion, vanilla included:
  Interface, not CB data). 7.6 listed PotionFluidTooltip for CB on the premise that only CB used it; that does not hold.
- **Common API added:** `swap/ConfigRename.mergeSections` (sections into an existing file). patcherMod 1.1.07; CB
  requires `[1.1.07,)`.
- `[jei]` tipped arrow and potion bucket switches stay in `CEConfig` (CB reads them downward).
- Pack-folder assets of CB features (`kubejs/assets/creativeengineering/textures/mob_effect/magic_resistance.png`, the
  potion bucket textures, the effect rename lang overlay, the coaster models) are not moved: they stay in the KubeJS
  resource pack. **Superseded 2026-11-04 (plan_cb_publish.md M3):** the textures, models, icon, coaster models and Magic
  Resistance's description moved into the jar under `creativebrewing` (2.5); the effect lang overlay keeps only other
  mods' effects.

## 5. Install (game closed)

Phases 2 and 3 (2026-11-01): install patcherMod 1.1.07 and CB together (`./gradlew :creativeCommon:installToMods
:creativeBrewing:installToMods --offline`), only after the wave 2 and 3 scratch runs (SQ-CS2, SQ-CMP) pass; then SQ-CBJ4.
The instance's config was moved by hand: the four sections are in `config/creativebrewing-common.toml` (backup in
`configs_edited/root_config/`) and gone from the CE file.

patcherMod is unchanged. Run `./gradlew :creativeBrewing:installToMods --offline`. It moves `backportedVinery-1.5.5-ce-19.2-06.jar` and
`backportedBrewin-4.5.0-ce-19.2-04.jar` out of `mods/` and copies `creativebrewing-1.0.0.jar` in. Creative Delight must
be installed (it is). The pack's dedicated server (CreEngServer) needs the same swap at the next deploy.

## 6. Tests

- **r1102b 2026-11-02** (scratch server; verdicts in `perf/runs/r1102b/` HANDOVER.md and the slice judged.md): PASS SQ-CBJ-04.

Scratch server: SQ-CBJ-01 to -05 in [scratch_queue.md](testing/scratch_queue.md). In game (after install):

- [ ] **CBR-01 Mod list:** "Creative Brewing" is listed once (the bundle); Vinery's `foundation` id shows "Foundation
  (Creative Brewing)", Vinery "Vinery (Creative Brewing)", Brewin' "Brewin' & Chewin' (Creative Brewing)"; JEI's mod line
  and WTHIT's mod line on a keg and a grapevine show the same names; no mod name ends in "(Alfonzo)".
- [ ] **CBR-02 Old world:** a world with kegs, coasters, aging barrels, apple presses and wine racks loads with every block
  and its contents; no "missing registry" or "unknown block" warnings for `vinery`, `foundation` or `brewinandchewin`.
- [ ] **CBR-03 Config:** `config/creativebrewing-common.toml` exists with a `[members]` section holding `vinery` and
  `brewinandchewin`, both `true`.
- [ ] **CBR-04 Log:** `logs/latest.log` has `[creativebrewing] members: [vinery, brewinandchewin]` and no
  `[creativebrewing] member ... is loaded from` ERROR line; `logs/kubejs/startup.log` lists `creativedelight` before
  `creativebrewing` in the startup pack order.
- [ ] **CBR-06 `creativebrewing` ids (M3, M4):** JEI lists the potion buckets, the 38 CB potions and Magic Resistance
  under "Creative Brewing"; the buckets show their two-layer textures with the potion's tint, the Magic Resistance HUD
  icon and JEED text show; a world saved before with a potion bucket in a chest, Magic Resistance active and a
  `kubejs:wither` potion loads with the bucket and the effect kept and no missing-registry warning for the old ids; the
  old bottled potion reads Uncraftable Potion (D5, expected); a coaster still shows a Display Delight food's model.
- [ ] **CBR-05 Brewin' and Vinery still work:** brew one keg batch and pour a tankard; press apples; age one wine.
  (Spot check; the full suites are AL- and VB- in alcohol.md.)

## 7. Appendix: inventory for the move (2026-10-31)

Paths: `PM` = `development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/`, `PMR` = patcherMod resources,
`KJ` = `kubejs/`. Line numbers as of 2026-10-31.

- **PM potion/** (whole): CEPotions, CompoundEffects, CompoundEffectTooltip, CopperPoisoningHandler (CB, Q3), EffectCategories,
  EffectRules, MagicResistanceEffect, PhantomBaneRules, PotionBucketDispenser.
- **PM fluidbrewing/** (whole): DragonBreathBrewing, Lingerification, PotionMixDedup.
- **PM compat/**: AutomatedPotionMixingCategory, LingerificationCategory, DragonBreathingJEI, PotionFluidTooltip,
  DrinkFluidEffects (stays), WthitCoaster, WthitHolderContents (split, mostly not potions), jei/PotionPagesJEI,
  jei/world/PotionRows.
- **PM other:** food/IntoxicatingDrinkTooltip; time/WineTime (Creative Calendar); trader/WinemakerTrades (CB);
  shield/EffectTickContext (stays); fan/DragonBreathing* (stays); devtools/DisplayNameDump 168-203 (stays);
  CreativeEngineering.java 84-86, 156-165, 376-379; CEConfig (below).
- **PM mixins:** effects (LivingEntityEffectRemap, LivingEntityUseItemIcon, GuiEffectIcon, MobEffectCategory,
  MobEffectTickContext (stays), JeedEffectInfo, JeedEffectRenderer, CaffeinatedEffect, CopperPoisoningEffect,
  CreateTeaPipeIcon, FruitsDelightEffectFunc, EchoArmorEffectProcedure / EchoArmorTooltip (gear, CB)); machines
  (PotionRecipeGenerators, PotionHelper, PotionMixingRecipes, MechanicalMixerBlockEntity, PotionFluidHandler,
  FluidTagInput (stays)); JEI (SpoutCategory, ItemDrainCategory, JEIHelper, MixerRecipeCategory,
  RefineryRecipeCategory, BottlingMachineRecipeCategory, Squeezer / Fermenter categories (split), JeiFluidHelperTooltip
  (stays), CckBrewingGuideMealTooltip (stays)).
- **PMR:** `creativeengineering.mixins.json` (the entries above); `neoCraftHelper.js` helpers (stay, rule 2); lang
  `en_us.json` 6-8, 31, 49, 59-61, 162, 167, 209-213; `waila_plugins.json` 17-23 (coaster); `state_lines.json` 12 (Brewin'
  cheese wheel and pizza); `data/creativecommon/damage_tags/magic.json` (magic resistance).
- **KJ startup:** as phase 1, plus splits in `_lib/fluid_dict.js` (potion, Brewin' and dragon breath fluids),
  `_lib/item_dict.js`, `_lib/food_dict.js` 1243, `_lib/removed_items.js` 319-321 and 390-391,
  `changes/transition_items.js` 31 and 44, `jei/order.js` 175 and 302+: dict and
  removal rows stay (rule 2; `func/trade_helper.js` 93-114 moves, phase 1); `_lib/effect_dict.js` stays (shared).
- **KJ server:** as phase 1, plus `_lib/dump_potions.js` (dev dump, can stay), `other/removed_mobs.js` 1-5 (Flare with
  Insomnia), `magic/dragon_breathing.js` (stays with the fan), `magic/armor_effects.js` / `tool_effects.js` (gear, CB);
  `engineering/dust.js` and `crystal.js` use potion fluid as an ingredient (stay).
- **KJ client:** as phase 1; `armor_effects_tooltip.js` / `tool_effects_tooltip.js` (gear, CB).
- **tools:** `gen_effect_descriptions.py` (writes 31 `effect.*` keys into the KJ lang overlay), `gen_coaster_models.py`,
  `effect_audit.py`, `potion_bucket_texture.py` (one-off), `jei_world/tab_drops.py` 183-193 (`per_potion` rows).
- **Already in CD:** the two compost files (phase 1); CD `creativeengineering/placeable_tags.js` 8-13
  (`#placeable_on_coaster`), CD `vintagedelight/recipes.js` 20-21 and 34, `vintagedelight/tags.js` 19 (Vintage rows that
  touch Brewin' items: stay in CD, they are Vintage's); `MangosteenCakeEffectMixin` (food fix, stays).
- **CEConfig:** `[effects]` 3477-3537, `[magic_resistance]` 453-469, `[copper_poisoning]` 489-511, `[coaster]` 1293-1300,
  `[alcohol]` 1494-1501 (phase 3); `[time.wine]` 1699-1713 (Creative Calendar), `[wthit] holderPotionEffects`, `[jei] potionBucketCategory`,
  `[crafting]` felsic potion rows (stay).
- **Third-party configs** (stay where they are, the members own them): `vinery-common.toml`, `vinery/config.json5`,
  `brewinandchewin-common.toml` / `-client.toml`, `jeed-client.toml`, `potionbundles-client.toml`.

## 8. History

- 2026-11-04 (plan_cb_publish.md M2 to M5): guards on every pack-native script (guard check 172 -> 0), every id under
  `creativebrewing:` with 41 swap entries, bucket and effect art, coaster models and Magic Resistance's lang into the
  jar, display names "Vinery (Creative Brewing)" and "Foundation (Creative Brewing)". Built, not installed.

- 2026-10-31 (owner decisions): Brewin' dishes and tags moved into Creative Delight; Winemaker trades, Vinery
  effects and gear effects added to the CB move plan; wine clock reserved for Creative Calendar.
- 2026-10-31 (later): `foundation` named "Creative Brewing", Brewin' "&", "(Alfonzo)" dropped from every mod name
  override (rebuilt, members clean-built after a missing Vinery refmap).
- 2026-10-31: bundle built with the two ports nested, framework (config, core scripts, mixin plugin), tooling repointed,
  inventory and move plan written. Not installed.

## 9. Publishing on CurseForge (licence audit 2026-11-01)

Audit of the built `creativebrewing-1.0.0.jar` and its two nested members (TODO `creativebrewing/publish #3`).

| Part | Source licence | In the jar | Verdict |
|---|---|---|---|
| CB's own jar (Java, config, 3 KubeJS scripts) | own, GPLv3 | `creativebrewing-LICENSE.txt` | OK |
| Vinery member: code (port of Let's Do Vinery 1.5.5 / 1.3.10 + Foundation) | MIT code; upstream `*/src/main/resources/` All Rights Reserved (Satisfy, 2022) | GPLv3 `LICENSE` + `backportedVinery-NOTICE-upstream-MIT.txt` (CurseForge licence text verbatim: ARR scope and both MIT notices); same file copied to `creativebrewing-NOTICE-vinery-upstream.txt` in the outer jar | OK. No upstream resources shipped (read from `vinery_upstream/` at runtime); NOTICE updated 2026-11-05 |
| Brewin' member: code, textures, models, data, `logo.png` | Brewin' And Chewin' 4.5.0, MIT (Umpaz; checked against upstream `LICENSE`) | GPLv3 `LICENSE` + MIT notice in `backportedBrewin-NOTICE-upstream-MIT.txt` | OK. NeoForge milk textures (LGPL 2.1) left out |
| Brewin' `fd_tray.png` | Farmer's Delight 1.21, MIT (vectorwing) | named in the same NOTICE | OK |
| Vinery member: code (Vinery 1.5.5, 1.3.10 blocks, Foundation 1.0.1) | MIT (satisfy, Cristelknight) | GPLv3 `LICENSE` + both MIT notices | OK |
| Vinery member: resources | upstream `src/main/resources` is All Rights Reserved | none copied: 116 lang strings are port-written (0 match upstream lang), one port recipe (`praetorian_wine`, rebuilt from 1.3.10's 3 ingredients) | OK; art and data are read at runtime from `vinery_upstream/` |

Fixed 2026-11-01: the Brewin' member's `issueTrackerURL` pointed at upstream's GitHub; it now points at the pack repo
(in the next build).

Open before upload:
- **Pack deploy still ships the two ARR Vinery jars** in the payload (`deploy.md`, `vinery_upstream_jars`). Fine for a
  private server; a public pack must make players download them instead.
- GPLv3: the upload must link the source (the GitHub repo, public, with the commit or tag of each released file).
- Hard dependencies `creativeengineering` (patcherMod) and `creativedelight` must be published too, or CB can't be
  installed outside the pack. CD carries ARR overrides of its own (memory `arr-mod-overrides-route`); audit it separately.
- The Vinery donor jars can't be CurseForge "Required dependencies": CurseForge would put Vinery 1.3.10 in `mods/`
  (same mod id `vinery`, duplicate-id crash) and can't install a Fabric 1.21.1 file into a Forge 1.19.2 profile. The
  route is the existing `vinery_upstream/` check (sha1-pinned, stops the game with the file name and source). The 1.3.10
  jar is not on Modrinth (sha1 lookup 404), only CurseForge; its source line points at the files list, not the file.
- Courtesy: Vinery keeps upstream's mod id and name; tell satisfy (Let's Do) about the port before publishing.
- Cosmetic: Foundation's `displayName` is "Creative Brewing".

### 9.1 Re-audit 2026-11-04 (plan_cb_publish.md Step 2)

Audit of the jars built 2026-11-04 after Step 1: `creativebrewing-1.0.0.jar` (23:31, Vinery `-09` and Brewin' `-05`
nested) and `-own.jar` (271 KB). Same method as 9; textures pixel-compared against the vanilla 1.19.2 client, every jar
in `mods/` and the donor folders. Supersedes the 2026-11-01 verdict for the own jar; the members' verdicts hold.

| Part | Source licence | In the jar | Verdict |
|---|---|---|---|
| Java, 49 classes (33 mixins) | own, GPLv3; no `@Overwrite`, no upstream method bodies (IE, Create, vanilla); the JEI categories extend IE's, not copy | `com/SaintAlfonzo/creativebrewing/**` | OK |
| KubeJS scripts (12 startup, 11 server, 8 client), lang, swap lists, mixin json | own | yes | OK |
| 206 coaster model maps (`gen_coaster_models.py`), 3 bucket item models | own (id to model path only) | yes | OK |
| 3 potion bucket bodies `*_potion_bucket.png` | **Mojang**: 122 of 122 opaque pixels equal vanilla `water_bucket.png` | `textures/item/` | low (9.3); optional: reference `minecraft:item/bucket` |
| 3 liquid layers `*_liquid.png` | **Mojang-derived**: greyscale of vanilla's water pixels (`potion_bucket_texture.py`) | `textures/item/` | low (9.3) |
| Magic Resistance icon | was a recolour of Botania's `cosmetic_lusitanic_shield.png`; **2026-11-05: replaced by vanilla `mob_effect/resistance.png`** (owner), retexture or repalette open | `textures/mob_effect/` | fixed (Botania); low (9.3) |
| `mods.toml` `GPL-3.0-only`, `creativebrewing-LICENSE.txt` | own | yes | OK |
| Vinery member `-09` | as 9 | only `mods.toml` and manifest differ from `-08` | OK |
| Brewin' member `-05` | as 9 | resources identical; 4 classes changed (coaster renderer port) | OK; bump the version at release |

Fixes: TODO `licence/cb #1` (buckets), `licence/cb #2` (effect icon).

### 9.2 Chain summary (Step 2, 2026-11-04)

| Jar | Audited | Verdict | Fixes | Writeup |
|---|---|---|---|---|
| Creative Brewing | 1.0.0 (23:31) | low: 6 Mojang-derived bucket textures and the vanilla Resistance icon (9.3); Botania icon removed 2026-11-05 | `licence/cb #1`, `#2` | 9.1 |
| Creative Common | 1.1.0 (23:34) | OK: notices fixed, vanilla edits accepted (9.5), panorama the owner's own (all 2026-11-05); mixin version gates and manifest 2026-11-05 (`creativecommon/publish #1`) | `licence/common #1` to `#3` (closed) | [creative_engineering.md](creative_engineering.md) 7 |
| Creative Compat | 1.0.0 (23:34) | remove / fix: all 475 paintings are other artists' art (126 not redistributable), banner-derived textures, TrashSlot overwrite; attribution NOTICE added 2026-11-05 | `licence/compat #1` to `#3` | [creative_compat.md](creative_compat.md) 6 |
| Creative Calendar | 1.0.0 (23:34) | OK: Q3 "all own code" holds | none | [creative_calendar.md](creative_calendar.md) 10 |
| Creative Delight | 1.1.0 (23:35), 20 nested members | fix: CfB basket and rack textures, Incubation art (`mobs/nests #2`), Rustic crate placeholders; notices fixed 2026-11-05; no member to remove | `licence/cd #1` to `#3`, `mobs/nests #2` | [creative_delight.md](creative_delight.md) 40 |
| All five | `license="GPL-3.0-only"` (D4 settled as `-only`) and a GPLv3 file in every jar and member (M6 done) | GPL / LGPL source availability not met until the public repo (Step 3) | Step 3 | plan_cb_publish.md 10 |

### 9.3 Mojang (vanilla) assets in our jars (2026-11-05)

Mojang's EULA and usage guidelines do not license redistributing game assets, modified or not, so a copied or
recoloured vanilla texture is technically unlicensed. In practice this is the norm in Forge and Fabric modding:
thousands of mods on CurseForge and Modrinth ship recolours and edits of vanilla textures, neither platform rejects a
file for it, and Mojang has never been reported acting against a mod over it. Rated **low**, not a blocker: CB's 6 bucket
layers and Magic Resistance icon (vanilla Resistance), Common's 12 armor tint sheets, Compat's moss tint, CD's powdered
sugar bucket. Where it costs nothing, prefer referencing the vanilla texture by resource location (the player's own
client supplies it) over shipping a byte copy. Not covered by this: the 30 vanilla paintings in Compat (whole artworks by
Kristoffer Zetterstrand, shipped as a set) stay in `licence/compat #1`.


### 9.4 Readiness review 2026-11-05 (owner: "is creative brewing ready to publish")

Verdict: **not ready.** Licences of CB's own jar and members pass (9, 9.1). What blocks it is the chain around it,
verification, and what a public install looks like. Checked against the jar built 2026-11-05 00:05 and the tree.

**Blockers**

| # | Gap | Evidence | Fix |
|---|---|---|---|
| R1 | The 4 hard dependencies are unpublished; Compat and CD still have open licence rows | `mods.toml`: Common, Compat, Calendar, CD mandatory; 9.2 rows `licence/compat #1` (built, not installed), `#2`, `#3` (TrashSlot `@Overwrite`), `licence/cd #2`, `mobs/nests #2` | Finish those rows; publish Common, Compat, Calendar, CD first |
| R2 | A public install must hand-place 10 jars, from 3 loaders and 3 MC versions | CB: `vinery_upstream/` (Vinery 1.5.5 Fabric 1.21.1, 1.3.10 Forge). CD: 7 members call `UpstreamJars.verifyOrFail()` in their constructor with no switch (enders, golden, largemeals, miners x2, oceanic, roll, rustic) | Install guide with links and sha1s on the project page; consider making CD's donor members skip (not crash) when their folder is absent; confirm the current CurseForge and Modrinth rules on required files not hosted as dependencies |
| R3 | Step 1 builds were never booted | `mods/creativebrewing-1.0.0.jar` is the 11-03 build (sha1 differs from build/libs); SQ-DCM, SQ-CBS (the minimal-chain gate, plan_cb_publish.md M7), SQ-CDFO not run; CBR-01 to -06 open | Install with the game closed, run SQ-DCM, SQ-CBS-01/02, SQ-CDFO, then CBR |
| R4 | 25 mixins into 8 optional mods apply at any version, and `defaultRequire 1` turns a changed target into a boot crash | `CBMixinPlugin.FOR_VERSION` empty; targets Create, IE, JEED, Create: Deep Dark, Everything Copper, Farmer's Respite, Fruits Delight, Better Beacons. The pack pins versions, public players will not | Gate each compartment on the tested version range (plugin, or optional `[[dependencies]]` with `versionRange`), or `require = 0` on the cosmetic (JEI, tooltip) injects |
| R5 | Members claim upstream mod ids `vinery`, `foundation`, `brewinandchewin` | Brewin' And Chewin' 1.19.2 Forge and Vinery 1.3.10 Forge are on CurseForge for the same MC version; the pack removes them only through `replaces_jars` at install. Jar-in-Jar ranges are exact (`[1.5.5-ce-19.2-09]`, `[4.5.0-ce-19.2-05]`) | Scratch test CB plus each upstream jar in `mods/` (what FML resolves, crash text); state the conflict on the project page; tell satisfy and Umpaz before publishing (9) |
| R6 | GPL source for the released file is not pushed | Repo public, but `origin/main` is at 2026-11-01; 262 uncommitted paths in CB's trees | Commit, push, tag; link the tag from each upload |

**Should fix before upload**

- `mods.toml`: description says "Needs the pack (Creative Common and KubeJS)"; header comments are stale ("no registry
  content yet", "patcherMod"); no `issueTrackerURL`, `displayURL` or `logoFile` on CB itself (the members have them).
- `creativedelight` range is `[1.0.0,)` while the jar was built and checked against CD 1.1.0; raise it if CB needs 1.1.0.
- Project page must list KubeJS, Rhino and Architectury (reached only through Common's dependency).
- Vinery's donor error message names "Let's Do Vinery (Backport)" (old name).
- Version bumps at release (Brewin' member resources unchanged but 4 classes changed, 9.1).
- Low (9.3): `licence/cb #1` buckets, `#2` Magic Resistance icon (vanilla Resistance today).

**Checked and fine**

- Licence: `GPL-3.0-only` and the GPLv3 text in the CB jar and both members; MIT notices for Brewin', Vinery,
  Foundation and the Farmer's Delight tray; no ARR Vinery file in the jar (read at runtime, sha1-pinned).
- Coupling: Java imports only Common and Calendar; all 17 globals CB scripts read are defined in the chain jars;
  `tools/guard_check.py --jar creativebrewing` 0 findings.
- Safety: the two serverbound packets (Brewin' JEI keg transfer, Foundation recipe book) run on the main thread and check
  the open menu; no network calls; the only file write is the dev texture dump; reflection limited to compat fields.
- Repo: no credentials in tracked files. Outside CB: CD's `moved_code_backup/.../libs/` tracks a Farmer's Delight jar
  (MIT, no notice beside it); delete the backup or add the notice.

### 9.5 Owner decisions 2026-11-05 on the 9.4 review

| Item | Decision |
|---|---|
| Publish order | Common, Compat, Calendar and Creative Delight (CD) are published first; Creative Brewing (CB) is the goal. R1 stands: their open licence rows gate it |
| R2 donor jars | The 10 jars are a manual download from a list of URLs on the project page and README first (names and sha1s from each `UpstreamJars`). A download script comes later. Future design: the player downloads the upstream mods from any source into `mods/`, and a startup script moves them into the shared upstream folder |
| R4 mixin versions | Closed for now: the pack is static for mod updates, so versions are pinned and a version gate is not needed. Reopen if the mod list ever updates |
| R5 ids | The ports keep the original ids (`vinery`, `foundation`, `brewinandchewin`). Duplicate-jar handling (real Vinery or Brewin' also in `mods/`) is later work; the project page states the conflict meanwhile |
| R6 source | The whole project is one public repo, not split per submod: https://github.com/SaintAlfonzo/CreativeEngineering . Tag the release there and link the tag from the upload |
| Textures | Vanilla textures and edits of them are wanted for visual consistency, and are not treated as a licence issue (supersedes 9.3's "optional cleanup"). `licence/cb #1` and `#2` are closed as accepted; no vanilla-art replacement work anywhere |
| Manifest | Done 2026-11-05, built: CB requires CD `[1.1.0,)`; CB and CD descriptions rewritten; CB has `issueTrackerURL` and `displayURL` (the repo); stale header comments fixed |

Dependencies to list on the project page, beyond the four Creative jars (pack versions in `mods/`): KubeJS
`kubejs-forge-1902.6.2-build.73` (Common requires `[1902.6.0,)`), Rhino `rhino-forge-1902.2.3-build.284` and Architectury
`architectury-6.6.92-forge` (both required by KubeJS, not declared by Common). Vinery needs the Let's Do Vinery 1.5.5 Fabric
and 1.3.10 Forge jars in `vinery_upstream/`.
