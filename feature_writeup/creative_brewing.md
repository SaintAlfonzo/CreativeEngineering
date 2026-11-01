# Creative Brewing: brewing, alcohol and potion add-ons in one jar

Written 2026-10-31 (owner request: "move the vinery backport and brewin and chewin backports into a parent mod much like
creative delight, called creative brewing ... eventually all of the potion changes made in the main mod and kubejs will be
moved here too, so plan the infrastructure for that as well").

**Status: bundle BUILT 2026-10-31, not installed, not booted.** `creative_brewing-1.0.0.jar`
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
| Let's Do Vinery 1.5.5 port | `:backportedVinery` | `development/creativeBrewing/members/vinery` | `vinery`, `foundation` | "Let's Do Vinery (Creative Brewing)", "Creative Brewing" (the `foundation` id) | `1.5.5-ce-19.2-08` |
| Brewin' and Chewin' 4.5.0 port | `:backportedBrewin` | `development/creativeBrewing/members/brewin` | `brewinandchewin` | "Brewin' & Chewin' (Creative Brewing)" | `4.5.0-ce-19.2-05` |
| (the bundle itself) | `:creativeBrewing` | `development/creativeBrewing` | `creative_brewing` | "Creative Brewing" | `1.0.0` |

Version bumps: Vinery `-07` was already built (in `jar_archive/`, never installed) and the tree has uncommitted work since,
so the first nested build is `-08`; Brewin' `-04` is the installed one, so `-05`. Both carry only the display-name change
on top of their last builds.

## 1. What moved (2026-10-31)

- `development/backportedVinery` -> `development/creativeBrewing/members/vinery`, `development/backportedBrewin` ->
  `.../members/brewin`. Gradle project names unchanged, so `compileOnly project(':backportedBrewin')` in patcherMod and
  Blockheads Additions still works and `./gradlew :backportedBrewin:compileJava` still runs. A member's own
  `installToMods` now stops with "ships nested in creativeBrewing's jar" (ce.forge-mod, `bundled_in`).
- Each member: `bundled_in=creativeBrewing`; `mod_name` "(Creative Brewing)" label (0.3 of creative_delight.md, same rule);
  `../patcherMod/libs/` compile paths now `rootProject.file('development/patcherMod/libs/...')`; Brewin's
  `tools/check_data.py` and `tools/port_resources.py` find the instance root four levels up.
- `.gitignore`: the member build/libs/jar_archive rules are now `/development/*/members/*/...` (CD and CB).
- `settings.gradle`: the two projects left the flat list; a Creative Brewing block includes the bundle and its members.
- `tools/build_deploy.py`: `IN_TREE_MODS` has `creativeBrewing` / `creative_brewing` in place of the two ports;
  `IN_TREE_EXTRA_SRC` watches the members and CB's script trees; the CB script trees are dev-only (ship in the jar);
  `vinery_upstream` donors are now required by the `creative_brewing` jar and pinned from
  `creativeBrewing/members/vinery/.../UpstreamJars.java`; the duplicate-mod-id check treats CB members like CD's.
- `tools/scratch_query/prep_srv.sh`: swaps in `creative_brewing-1.0.0.jar` and drops the standalone port jars beside it.
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
script trees into the jar and writes `creative_brewing_members.txt`; the jar task fails if any of the three script dirs
has no `.js`. `replaces_jars` lists the old standalone port jars and what each port replaced, so the first install
moves `backportedVinery-*`, `backportedBrewin-*` out of `mods/` (to `development/creativeBrewing/jar_archive/displaced/`).

### 2.2 Load order and dependencies

`mods.toml`: `creativeengineering` (KubeJS, helpers, startup loader) and `creative_delight` (nests Farmer's Delight,
Brewin's base; defines `global.CD`), both mandatory, both `ordering="AFTER"`. patcherMod's startup loader runs jar packs
in mod-list order, so the startup order is: patcherMod's pack, `startup_scripts/`, CD, CB. Check
`[jar-startup] startup script packs in order:` in `logs/kubejs/startup.log` (SQ-CBJ-02). Server and client script packs
from jars run in resource order, one pack per namespace, after the pack folder; event callbacks of all packs run in the
same passes (creative_delight.md 10.2). If a CB server script ever needs a CD row's result, do it in a later event
(`ServerEvents.highPriorityData` / `recipes` with a late `// priority:`) and check the server pack order in
`logs/kubejs/server.log` first.

### 2.3 Config: `config/creative_brewing-common.toml` (CBConfig)

- `[members]`: one switch per member id (`vinery`, `brewinandchewin`), default on; off makes CB's scripts skip that
  member's rows (`global.CB.on(id)`), the member's own data stays.
- `[brewing]`, `[alcohol]`, `[effects]`: one switch per moved feature. A feature is one row in `CBConfig.FEATURE_ROWS`
  (`{section, id, comment...}`); a section is written only once it has a row. Read with `global.CB.feature(id)` (scripts,
  inside an event callback) or `CBConfig.feature(id)` (Java).
- Dials that move from `creativeengineering-common.toml` keep their key names and comments in the section of their
  feature (4.3).

### 2.4 Scripts

| Type | Authored in | In the jar | Core file |
|---|---|---|---|
| Startup | `development/creativeBrewing/src/main/resources/kubejs_startup/<modid>/` | same path | `kubejs_startup/creative_brewing/cb_core.js`: `global.CB` (`on`, `feature`, `members`, `recipes`) |
| Server | `kubejs/data/creative_brewing/kubejs/<modid>/` | `data/creative_brewing/kubejs/` | `_core/cb_server.js` (checks `global.CB` on load) |
| Client | `kubejs/assets/creative_brewing/kubejs/<modid>/` | `assets/creative_brewing/kubejs/` | `_core/cb_client.js` (checks `global.CB` on login) |

Rules, as CD's (creative_delight.md 29): one folder per mod the rows touch (`vinery/`, `brewinandchewin/`,
`minecraft/` for vanilla potions, `immersiveengineering/`, `create/`, `alexsmobs/`, ...), pack-native systems in
`creativeengineering/`; each file headed `// requires: <modid>`; member rows wrapped in an early return on
`!global.CB || !global.CB.on('<id>')`; recipes inside `global.CB.recipes(() => ...)`, so NCH writes
`creative_brewing:<type>/<suffix>` ids. Server and client scripts load from the pack folder on `/reload` / F3+T;
startup scripts need a CB rebuild and restart.

### 2.5 Java

Package `com.SaintAlfonzo.creativebrewing`. `CreativeBrewing` (the `@Mod`: config, member log, the stale-jar ERROR line
of creative_delight.md 28.3), `CBConfig`, `compat/CBMixinPlugin` (the shared `GatedMixinPlugin`: a mixin in
`mixin.<modid>.` applies only when that mod is loaded; vanilla targets in `mixin.minecraft.`).
`creative_brewing.mixins.json` is empty. Mixins use SRG names with `remap = false` (no refmap), as patcherMod's do, so a
mixin moves over unchanged apart from its package. `compileOnly project(':patcherMod')` is there for CEConfig and pack
APIs; add `:creativeDelight` the first time a compartment needs CD Java.

## 3. Vinery donors

The `vinery_upstream/` donor folder works as before: the game stops with a message if the two pinned jars are missing.
Deploy now ties the folder to the `creative_brewing` jar (`UPSTREAM_DONORS`).

## 4. Move plan: potion, alcohol and effect code into CB

Inventory taken 2026-10-31 (read-only sweep of patcherMod and the pack folders; full list in section 7). Owner decision
2026-10-25 (TODO `creativebrewing/move #1`): potion brewing, alcohol, Vinery, effect descriptions, changes and swaps go
to Creative Brewing. Nothing below is moved yet.

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

**Phase 1: KubeJS only** (CB rebuild + restart; no patcherMod change). Whole files:
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

**Phase 3: config.** `[effects]`, `[magic_resistance]`, `[alcohol]`, `[coaster]`, `[copper_poisoning]` leave `CEConfig` for `creative_brewing-common.toml` with the same keys. Edited values in
`config/creativeengineering-common.toml` and `configs_edited/` are copied over by hand in the same session (Forge would
otherwise write defaults), plus a startup WARN in CB when the old keys are still in the CE file.

**Stays in patcherMod (shared with other systems):** `shield/EffectTickContext` and `MobEffectTickContextMixin`
(shields; copper poisoning and the gear effects read it as a patcherMod API), the trader system (`TradeListings`,
`global.trades`), the Dragon Breathing fan type (potions.md lists it as not potions), `FluidTagInputMixin` (every IE fluid machine), `DisplayNameDump` (dev tool), `DrinkFluidEffects` /
`JeiFluidHelperTooltipMixin` (all drinks, not only alcohol), Central Kitchen's brewing guide tooltip (shares
`MealSlotTooltip` with the cooking guide). Each stays unless the owner says otherwise (4.4 Q4).

**Not CB, not patcherMod for long: Creative Calendar.** `time/WineTime` and `[time.wine]` (the wine clock) are reserved
for the future Creative Calendar mod with the rest of the calendar, day and night, seasons and biome weather (owner,
2026-10-31; [creative_calendar.md](creative_calendar.md)). Creative Brewing will depend on it; until it exists Vinery
keeps reading WineTime from patcherMod.

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

One line per moved file, `old path -> new path, date`.

- `kubejs/server_scripts/cooking/cooking.js` (Brewin' dishes, all rows) -> **Creative Delight**
  `kubejs/data/creative_delight/kubejs/brewinandchewin/recipes.js`, 2026-10-31 (Q1; ids now `creative_delight:`). The pack
  file is left empty.
- `kubejs/server_scripts/tags/cookingtags.js` (Brewin' tags) -> **Creative Delight** `.../brewinandchewin/tags.js`,
  2026-10-31. The pack file is left empty.

## 5. Install (game closed)

patcherMod is unchanged. Run `./gradlew :creativeBrewing:installToMods --offline`. It moves `backportedVinery-1.5.5-ce-19.2-06.jar` and
`backportedBrewin-4.5.0-ce-19.2-04.jar` out of `mods/` and copies `creative_brewing-1.0.0.jar` in. Creative Delight must
be installed (it is). The pack's dedicated server (CreEngServer) needs the same swap at the next deploy.

## 6. Tests

Scratch server: SQ-CBJ-01 to -05 in [scratch_queue.md](testing/scratch_queue.md). In game (after install):

- [ ] **CBR-01 Mod list:** "Creative Brewing" is listed twice (the bundle, and Vinery's `foundation` id); Vinery shows
  "Let's Do Vinery (Creative Brewing)", Brewin' "Brewin' & Chewin' (Creative Brewing)"; JEI's mod line and WTHIT's mod
  line on a keg and a grapevine show the same names; no mod name ends in "(Alfonzo)".
- [ ] **CBR-02 Old world:** a world with kegs, coasters, aging barrels, apple presses and wine racks loads with every block
  and its contents; no "missing registry" or "unknown block" warnings for `vinery`, `foundation` or `brewinandchewin`.
- [ ] **CBR-03 Config:** `config/creative_brewing-common.toml` exists with a `[members]` section holding `vinery` and
  `brewinandchewin`, both `true`.
- [ ] **CBR-04 Log:** `logs/latest.log` has `[creative_brewing] members: [vinery, brewinandchewin]` and no
  `[creative_brewing] member ... is loaded from` ERROR line; `logs/kubejs/startup.log` lists `creative_delight` before
  `creative_brewing` in the startup pack order.
- [ ] **CBR-05 Brewin' and Vinery still work:** brew one keg batch and pour a tankard; press apples; age one wine.
  (Spot check; the full suites are AL- and VB- in alcohol.md.)

## 7. Appendix: inventory for the move (2026-10-31)

Paths: `PM` = `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/`, `PMR` = patcherMod resources,
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
  cheese wheel and pizza); `data/creativeengineering/damage_tags/magic.json` (magic resistance).
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

- 2026-10-31 (owner decisions): Brewin' dishes and tags moved into Creative Delight; Winemaker trades, Vinery
  effects and gear effects added to the CB move plan; wine clock reserved for Creative Calendar.
- 2026-10-31 (later): `foundation` named "Creative Brewing", Brewin' "&", "(Alfonzo)" dropped from every mod name
  override (rebuilt, members clean-built after a missing Vinery refmap).
- 2026-10-31: bundle built with the two ports nested, framework (config, core scripts, mixin plugin), tooling repointed,
  inventory and move plan written. Not installed.

## 9. Publishing on CurseForge (licence audit 2026-11-01)

Audit of the built `creative_brewing-1.0.0.jar` and its two nested members (TODO `creativebrewing/publish #3`).

| Part | Source licence | In the jar | Verdict |
|---|---|---|---|
| CB's own jar (Java, config, 3 KubeJS scripts) | own, GPLv3 | `creative_brewing-LICENSE.txt` | OK |
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
- Hard dependencies `creativeengineering` (patcherMod) and `creative_delight` must be published too, or CB can't be
  installed outside the pack. CD carries ARR overrides of its own (memory `arr-mod-overrides-route`); audit it separately.
- The Vinery donor jars can't be CurseForge "Required dependencies": CurseForge would put Vinery 1.3.10 in `mods/`
  (same mod id `vinery`, duplicate-id crash) and can't install a Fabric 1.21.1 file into a Forge 1.19.2 profile. The
  route is the existing `vinery_upstream/` check (sha1-pinned, stops the game with the file name and source). The 1.3.10
  jar is not on Modrinth (sha1 lookup 404), only CurseForge; its source line points at the files list, not the file.
- Courtesy: Vinery keeps upstream's mod id and name; tell satisfy (Let's Do) about the port before publishing.
- Cosmetic: Foundation's `displayName` is "Creative Brewing".
