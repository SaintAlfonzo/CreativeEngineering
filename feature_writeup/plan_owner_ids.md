# Plan: ids that name the wrong owner jar (2026-11-07)

Status: BUILT 2026-11-07 (all of section 5; nine jars built, NOT installed, not booted, not tested). TODO row
`creativesplit/ids #23`. What was done and where it differs from this plan: section 8. Old names below are the audit
record and stay as written.

After the split ([creative_split.md](creative_split.md)) and the 2026-11-07 interface carve (7.19), several ids still
carry the namespace of the jar the code came from, not the jar that owns it now. This plan lists every one found and
how to fix it.

## 1. What counts as wrong

Rule 1 of creative_split.md 3 still holds: **registered ids** (items, blocks, effects, recipe types, worldgen) keep
`creativeengineering:` whichever jar registers them, because saves hold them. Those are not in this plan.

In scope: ids that are **not saved in worlds** and should name the jar that owns the code. These are Forge event
subscriber mod ids, WTHIT plugin and option ids, JEI plugin uids and category ids, and network channel names.

## 2. Decision needed

**D1. Reverse the 7.19 "ids kept so nothing resets" note** (and D_registration.md, which said plugin ids "may
stay"). Recommended: yes. What resets:

- WTHIT: a player's own on/off choices for our plugins and lines, kept in their client `config/waila/`. The pack ships
  every one of ours on, so only a player who turned one off sees it come back on.
- JEI: category order. The pack-shipped `recipe-category-sort-order.ini` is updated in the same change (step 4), so
  nothing visible moves.

Nothing in a world changes.

## 3. Findings

### A. Bugs: event handlers never registered (fix whatever D1 says)

Forge only registers a `@Mod.EventBusSubscriber` class when its `modid` equals the id of the mod whose jar holds it.
Both classes moved jars on 2026-11-07 and kept Common's id, so neither handler runs. Neither is registered by hand.
These builds are not installed yet, so players have not seen the breakage.

| # | File | Now | Fix | Effect today |
|---|---|---|---|---|
| A1 | `creativeCompat/.../compat/guititle/TitlePlaques.java:71` | `modid = "creativecommon"` | `CreativeCompat.MOD_ID`; javadoc line 69 names `creativecommon-common.toml`, change to `creativecompat-common.toml` | Title plaques never stretch, overflow log never written |
| A2 | `creativeEquipment/.../compat/ArmorOverflowRenderer.java:58` | `modid = CreativeCommon.MODID` | `CreativeEquipment.MOD_ID` | Detail Armor Bar overflow tint not drawn |

### B. WTHIT plugin ids and option ids

| Jar | `waila_plugins.json` key now | Target | Option ids in the class (`ResourceLocation` lines) |
|---|---|---|---|
| Compat | `creativecommon:` x13 (block_model_icon, coaster, holder_contents, hive, state_lines, campfire, sheetmetal_tank, kitchen_station, remainder_buffer, sheared, silk_touch, operator_blocks, pot_contents) | `creativecompat:` | `creativecommon:` in WthitSheared, WthitSilkTouch (LINE only), WthitPotContents, WthitHolderContents, WthitHive (2), WthitCoaster, WthitKitchenStation (2), WthitCampfire (2), WthitRemainderBuffer (2), WthitStateLines (`state_<label>`); grep the other four classes at fix time |
| Compat | `creativeengineering:run_speed` | `creativecompat:run_speed` | `surface/WthitRunSpeed` uses `CreativeCompat.NS` (= `creativeengineering`); use `MOD_ID` |
| Calendar | `creativeengineering:crop_seasons` | `creativecalendar:crop_seasons` | `WthitCropSeasons` uses `CreativeCalendar.NAMESPACE` (2 lines); use `MOD_ID` |

Already right: Creative Delight (`creativedelight:` x4), Creative Labels, Barbeques, Vinery, Brewin', Tempad.

Follows the ids:

- Lang: WTHIT reads `config.waila.plugin_<namespace>.<path>`. 12 keys in Compat's `en_us.json`
  (`plugin_creativecommon.*`, `plugin_creativeengineering.run_speed`) and 1 in Calendar's
  (`plugin_creativeengineering.crop_seasons`) get the new namespace. This is a key derived from the id, so rule 1's "lang
  keys keep their names" does not apply. The tooltip keys `creativecommon.wthit.*` stay.
- `config/waila/plugin_toggle.json5`: rename the 15 live rows; delete the 14 dead `creativeengineering:*` rows left
  from before the 2026-11-04 swap (lines 14-34 except `run_speed` and `crop_seasons`). Copy to `configs_edited/`.
- `config/waila/waila_plugins.json5` has no section for our namespaces: nothing to move.

### C. JEI plugin uids

| Jar | Class | Now | Target |
|---|---|---|---|
| Compat | SmithingTemplateJEI, WorldInteractionJEI, CyclingCraftingJEI | `CreativeCommon.NS` | `CreativeCompat.MOD_ID` |
| Delight | OvenJeiPlugin, FeastPlatingJEI, CanPagesJEI, SeasoningJEI | `CreativeDelight.NS` (`creativeengineering`) | `CreativeDelight.MOD_ID` |

Not saved anywhere. Compat's `JeiIngredientChanges` report labels its rows by uid, so the report reads correctly
after the fix. Already right: Brewing (2), Engineering (`dragon_breathing`), Labels, Mooblooms, every member port.

### D. JEI category (recipe type) ids

| Jar | Now | Target |
|---|---|---|
| Compat | `creativecommon:world/*` (19 tabs, `WorldInteractionCategory:77`) | `creativecompat:world/*` |
| Delight | `creativeengineering:canning`, `can_uses` (CanPagesJEI), `feast_plating`, `seasoning` (+ display id `seasoning/salt_mixing`) | `creativedelight:` |
| Delight | `creativeengineering:baking` | **keep**: mirrors the registered recipe type `creativeengineering:baking`, like Engineering's `dragon_breathing` |

Follows: `config/jei/recipe-category-sort-order.ini` rows 176-178, 180, 192-210 (JEI puts unknown categories last),
and its `configs_edited/` copy. No KubeJS script names these ids (grepped).

### E. Network channels (low)

| Jar | Now | Target |
|---|---|---|
| Equipment `stepstool/Stepstool.java:67` | `creativeengineering:stepstool` | `creativeequipment:stepstool` |
| Compat `compat/SackBundle.java:45` | `creativeengineering:sack_bundle` | `creativecompat:sack_bundle` |

Not saved. Client and server must carry the same build, which every release already requires.

### F. Resource left in the old owner (low)

- `assets/creativecommon/state_lines.json` is in Common's jar but only Compat's `WthitStateLines` reads it. Move to
  Compat's `assets/creativecompat/` and change `RULES` (line 55).
- Empty folders `assets/creativeengineering/` in Calendar and Worldgen: delete.

### G. `ce` / `CE` names outside Creative Engineering (added 2026-11-07, owner)

"CE" means the Engineering jar (or the pack). Commands, classes, member mod ids and constants in other jars that use it
take their own jar's prefix. Saved strings (NBT keys) keep their value. Only the Java name around them changes.

**D2. Prefix per jar** (recommended; the existing ones are what the jars already use for `global.X` and config classes):

| Jar | Prefix | Already used by |
|---|---|---|
| Engineering | `cen` / `CEN` (and `ce` stays valid for it) | `global.CEN` |
| Equipment | `ceq` / `CEQ` | `global.CEQ`, `CEQConfig` |
| Worldgen | `cwg` / `CWG` | `CWGConfig` |
| Magic | `cmg` / `CMG` | `CMGConfig` |
| Delight, Brewing, Labels | `cd`, `cb`, `cl` | `CDConfig`, `CBConfig`, `CLConfig` |
| Common | `cc` (commands); class names spell `Common` | `CommonConfig` |
| Calendar | `ccal`; classes spell `Calendar` | `CalendarConfig` |
| Devtools | `cdev`; classes spell `Devtools` / `Dev` | `DevtoolsConfig` |
| Compat | `ccmp` if ever needed (no commands today); classes spell `Compat` | `CompatConfig` |

The other option is one root command per jar (`/creativeworldgen locate ...`). Not recommended because the names are long to type.

**G1. Commands** (all need permission level 2; old names are not kept as aliases):

| Jar | Now | New | Reach (files: tools / docs / dev / memory) |
|---|---|---|---|
| Common `swap/ItemMerges.java:419` | `/ceswap` | `/ccswap` | 3 / 5 / 2 / 0 |
| Calendar `time/TimeCommand.java:32` | `/cetime` | `/ccaltime` | 7 / 13 / 9 / 0 |
| Worldgen `LocateCommand.java:107` | `/celocate` | `/cwglocate` | 14 / 20 / 8 / 2 |
| Worldgen `StructureSpawnBoxes.java:355` | `/cespawnbox` | `/cwgspawnbox` | 5 / 3 / 1 / 0 |
| Devtools `GeneratorCommand.java:135` | `/cegen` | `/cdevgen` | 21 / 83 / 11 / 8, plus CLAUDE.md, skill, `server_scripts` 1 |
| Devtools `MassPlacer.java:156` | `/ceplace` | `/cdevplace` | 8 / 27 / 9 / 7, plus a skill |
| Devtools `PerfCommand.java:47` | `/ceperf` | `/cdevperf` | 11 / 9 / 7 / 1 |
| Devtools `DisplayNameDump.java:75` | `/cedumpnames` | `/cdevdumpnames` | 5 / 8 / 2 / 1 |
| Devtools `WthitIconDump.java:64` | `/cedumpicons` | `/cdevdumpicons` | 1 / 5 / 4 / 1 |

`/census` (perfbot, and the `census` subcommand of `/celocate`) is the word "census", not a prefix, so it stays.
Tools are updated in the same change: the RCON perf runner, the `/cegen` callers, `generators.json`, scratch
skill scripts. History docs (dated `plan_*.md`, scratch run logs) keep the old name. Live docs (CLAUDE.md, skills,
memory, feature writeups' how-to sections) get the new one.

**G2. Devtools member mod ids** (dev `mods/` only, never released):

| Now | New | Follows |
|---|---|---|
| `cecapture`, class `CeCapture`, package `...cecapture` | `cdevcapture`, `DevCapture`, `...cdevcapture` | JEI uid `cecapture:driver`, `mod_sides.md` row, 3 tools, 7 docs |
| `ceperfbot`, class `CePerfBot` | `cdevperfbot`, `DevPerfBot` | 19 tools (perf suite), 8 docs, memory `perf-suite-bot-players` |

**G3. Classes:**

| Jar | Now | New | Follows |
|---|---|---|---|
| Common | `CEMixinPlugin` | `CommonMixinPlugin` | `creativecommon.mixins.json` `plugin`; imports in Equipment's and Compat's plugins and 2 IW classes |
| Common | `CEPotions` (+ `CE_POTION_MIXES`) | `CommonPotions` (`POTION_MIXES`) | CreativeCommon, MagicResistanceEffect, Brewing's BetterBeaconsPotions |
| Equipment | `CETiers` | `CEQTiers` | 4 files |

**G4. Constants and comments** (Java names only):

| Jar | Names | New prefix |
|---|---|---|
| Delight | `CE_TEXTURE`, `CE_FLUID`, `CE_LEVEL`, `CE_POURING`, `CE_BOXES`, `CE_INV_START`, `CE_INV_END`, `CE_EMPTY_MASON_JAR` (kettle, cooking pot, mulberry jam mixins) | `CD_` |
| Worldgen | `CE_VILLAGER_LADDERS`, `CE_TALL_DOORS`, `CE_TALL_WOODEN_DOORS`, `CE_SHORT_DOORS`, `CE_SHORT_WOODEN_DOORS`, `CE_FOOTPRINT_RADIUS` | `CWG_` |
| Equipment | `CE_DEFAULT_ARMOR_CAP` | `CEQ_` |
| Compat | `CE_EYE_COLOURS`, `CE_FACES` (MCA eye mixin) | `COMPAT_` |
| Devtools | log file `logs/ce_block_state_ranges_<side>.txt` (+ DevtoolsConfig comment) | `cdev_` |
| 8 files | comments naming `CEConfig`, a class that no longer exists (CEQConfig, EffectRules, EffectCategories, FeedingTroughModuleMixin, StemExtraFruit, ImmersivePostsMaterials, CWGConfig, CMGConfig) | the config class that owns the key now |

**Kept (saved data):** NBT keys `"CEStash"` (Common), `"CENightstandItems"` (Compat), `"CETankFluid"`, `"CETankLevel"`,
`"CETankBoxes"`, `"CETankPouring"`, `"CELunchboxSkin"` (Delight), `"CESkin"`, `"ce_copper_poison_roll"` (Equipment).
Renaming them would lose stored data in existing worlds, and the pack's no-conversions rule rules out migrating them.
Engineering's own `ce_BladeProfile` / `ce_ResolvedRecipe` are correct. `CEI` (Create: Enchantment Industry) is not ours.

## 4. Kept on purpose (not wrong)

| What | Why |
|---|---|
| `NS = "creativeengineering"` in Compat, Equipment, Magic, Calendar, Worldgen, Delight and its registered content | Rule 1: saves hold these ids |
| Capability ids `creativeengineering:nightstand_storage`, `jar_fluid_pipes` (Compat). CornerStorage (Delight) moved 2026-11-10 with its saved data (plan_cd_namespace.md D2, swap list `capabilities`) | Saved in block entity NBT under the id; a rename without a `capabilities` entry empties the storage |
| Keybind `key.creativeengineering.stepstool` | `options.txt` stores bindings by this name; a rename resets the player's key |
| Tags `creativecommon:wthit/*`, Common's `data/creativeengineering/tags` | Pack data from KubeJS / content tags, not code ids |
| Lang keys `creativecommon.wthit.*`, `creativecommon.jei.*` | Rule 1 (key names stay) |
| Compat config strings naming `creativecommon:magic_resistance`, `creativecommon:burn_tick`, `PackIngredientOrder.TAG_NAMESPACE` | They point at Common-owned content, correctly |
| Creative Labels' mod id `creative_labels` | Breaks the no-underscore rule, but it is a mod id with registered items: a separate decision, not this plan |

## 5. Steps

1. A1, A2 (any D1 answer).
2. If D1 = yes: B, C, D in Compat, Calendar, Delight; lang keys; the two pack config files and their `configs_edited/`
   copies.
3. E, F.
4. G (after D2): G3 and G4 first (compile-only), then G1 and G2 with their tools, skills, CLAUDE.md and memory in the
   same change, so no caller keeps the old command name.
5. Add `tools/id_owner_lint.py` so this does not come back: for each in-tree jar, flag any `@Mod.EventBusSubscriber`
   modid, `waila_plugins.json` key, `getPluginUid` / `RecipeType.create` namespace or channel name that is not the
   jar's own mod id (resolving `X.NS` / `X.MOD_ID` constants), and any `literal("ce...")` command, `CE*` / `Ce*` class
   or `CE_` constant outside Engineering. Allow-list the section 4 rows and the G kept NBT keys. The 11-07 audit
   script is the starting point.
6. Build `:creativeCommon:`, `:creativeCompat:`, `:creativeCalendar:`, `:creativeDelight:`, `:creativeEquipment:`,
   `:creativeWorldgen:`, `:creativeMagic:` (comments only), `:creativeBrewing:` (CEPotions import), `:creativeDevtools:`
   and install with the 7.19 jars, which are not installed yet either. Game must be closed.
7. Add the scratch and client queue rows below; update creative_split.md (new 7.20 pointing here, and the 7.19 "ids
   kept" note) and D_registration.md.

## 6. Tests (rows to add to scratch_queue.md at build time)

- SQ-IDS-01: boot with the new jars; no WTHIT "duplicate plugin" or JEI "duplicate uid" lines; `id_owner_lint.py`
  clean.
- CQ-IDS-01: WTHIT config screen lists the 14 Compat lines under Creative Compat and Crop Seasons under Creative
  Calendar, each with its name (no raw `config.waila.plugin_*` key); each line still shows in world.
- CQ-IDS-02: JEI shows the 19 world tabs, Canning, Can Uses, Feast Plating, Seasoning in their old places.
- CQ-IDS-03 (A1): rename a chest to a long name; plaque stretches; `kubejs/exported/gui_title_overflow.json` gains the
  entry.
- CQ-IDS-04 (A2): over 20 armor with Detail Armor Bar; the overflow tint draws.
- CQ-IDS-05 (E): stepstool key and sack bundle work on a dedicated server.
- SQ-IDS-02 (G1, G2): on the scratch server, `/ccswap`, `/ccaltime`, `/cwglocate`, `/cwgspawnbox`, `/cdevgen`,
  `/cdevplace`, `/cdevperf`, `/cdevdumpnames` run and the old `/ce*` names are unknown; the perf runner drives
  `cdevperfbot` bots; `/cdevgen` runs one generator from `generators.json`.
- SQ-IDS-03 (G kept keys): a world saved before the change keeps a kettle's tank fluid, a nightstand's items, a
  lunchbox and backpack skin, and an in-progress sequenced assembly item.
- CQ-IDS-06: `/cdevdumpicons` and the capture driver (`cdevcapture`) run on the client.

## 7. Changelog

One bullet, for the admin commands in released jars (G1: `/ceswap`, `/cetime`, `/celocate`, `/cespawnbox`), under a
catch-all heading, e.g. "Admin commands renamed: /ccswap [/ceswap], /ccaltime [/cetime], /cwglocate [/celocate],
/cwgspawnbox [/cespawnbox]." Devtools commands are dev-only: no bullet. No bullet for A1 and A2 (they break only
unreleased builds) or for the id changes (not visible in play).

## 8. Execution (2026-11-07)

D1 and D2 taken as recommended. Steps 1 to 7 done; summary in creative_split.md 7.20, tests SQ-IDS-01 to -03 and
CQ-IDS-01 to -06 in scratch_queue.md, changelog bullet under `## Commands` in `unreleased.md`.

Differences from the plan:

- B: the "option ids" are WTHIT line ids (`setLine` keys); our plugins register no config options. The lint checks line
  ids by their `creative*` namespace.
- B: `remainder_buffer` and `pot_contents` never had a `config.waila.plugin_*` name; added ("Pot and Kettle Leftovers",
  "Flower Pot Contents") so CQ-IDS-01 can pass. Calendar also got the namespace name key ("Creative Calendar").
- D: the World tabs had already drifted to the end of `recipe-category-sort-order.ini` when they became
  `creativecommon:`; they go back to their pre-11-07 slot after Tipped Arrows (from the `configs_edited/` copy).
- B: `plugin_toggle.json5` had no `configs_edited/` copy; added at `configs_edited/root_config/waila/`.
- G3: the JS list in Brewing's `potions.js` is `CB_POTION_MIXES`, not `POTION_MIXES`: it sits beside `AM_` / `RD_`
  lists and the file's comments name vanilla's `PotionBrewing.POTION_MIXES`.
- G2: the system properties follow the mod ids (`-Dcdevcapture.plan`, `-Dcdevperfbot.out`), member display names are
  "Creative Devtools Capture" / "Creative Devtools Perf Bot", the Devtools package `ceplace` is `cdevplace` (and its
  test), the generator log folder is `logs/cdevgen/`, the mass placer ticket type `creativedevtools_cdevplace`.
- Memory files `cegen-generator-runner` and `worldgen-config-ceplace` renamed to `cdevgen-...` / `...-cdevplace`.
- Comments naming `CEConfig`: the three carve configs (CEQ, CWG, CMG) say "the pre-carve CEConfig's" (their field
  names really are the old class's); Engineering's ImmersivePostsMaterials says "a config file" (Engineering has none).

Not done (outside this plan): mixin member prefixes `ce$` / `creativeengineering$` in other jars' mixins; Creative
Delight's four WTHIT plugins have no `config.waila.plugin_creativedelight.*` names; Creative Labels' `creative_labels`
mod id (section 4). `tools/id_owner_lint.py -v` lists 7 unresolved ids, all port plugins naming their own mod through
a helper.

