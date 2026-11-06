# creativebrewing / move: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `# Creative Brewing`, Sub `move` (row 1 open). Feature writeup:
[creative_brewing.md](../creative_brewing.md) 4 (move plan) and 7 (inventory with line numbers), test prefix `CBR-`
(last CBR-05), scratch `SQ-CB`. The moved features keep their own suites: [potions.md](../potions.md),
[potion_buckets.md](../potion_buckets.md), [effects.md](../effects.md), [alcohol.md](../alcohol.md) (`AL-`, `VB-`).
The bundle itself: [creativebrewing_bundle.md](creativebrewing_bundle.md).

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 0 | `bundle #2` boot | scratch | SQ-CBJ-01 to -05 first: nothing moves into a jar that has not booted |
| 1 | `move #1` phase 1 | KubeJS into the CB jar trees | Whole files, then splits; CB rebuild + restart |
| 2 | `move #1` phase 2 | patcherMod Java into CB | patcherMod and CB built and installed together |
| 3 | `move #1` phase 3 | config | `[effects]`, `[magic_resistance]`, `[alcohol]`, `[coaster]`, `[copper_poisoning]` |

Not this row: the wine clock (`time/WineTime`, `[time.wine]`) goes to Creative Calendar
([creativecalendar_reserve.md](creativecalendar_reserve.md)).

---

## move #1: Move potion brewing, alcohol, Vinery and effect content into Creative Brewing
Status Planned / Work KubeJS / new jar / TODO `creativebrewing/move #1` (Tests S P)

### Goal
Everything about potion brewing, alcohol, the Vinery and Brewin' integrations, effect text, effect changes and swaps,
and gear effects lives in Creative Brewing, so patcherMod and the pack folder keep only shared systems.

### Now
- Framework built 2026-10-31 and installed (16:55), not booted: `global.CB` (`on`, `feature`, `members`, `recipes`) in
  [cb_core.js](../../development/creativeBrewing/src/main/resources/kubejs_startup/creativebrewing/cb_core.js);
  server and client trees hold only `_core/cb_server.js` and `_core/cb_client.js`
  (`kubejs/data/creativebrewing/kubejs/`, `kubejs/assets/creativebrewing/kubejs/`); `CBConfig.FEATURE_ROWS` empty;
  `creativebrewing.mixins.json` empty (creative_brewing.md 2).
- **Moved so far (2026-10-31), to Creative Delight, not CB** (owner Q1): the Brewin' dishes from
  `server_scripts/cooking/cooking.js` and the Brewin' tags from `server_scripts/tags/cookingtags.js` (both pack files
  now 4 lines) into CD `brewinandchewin/recipes.js` and `tags.js` (creative_brewing.md 4.5). The TODO row's Details
  still list them as left.
- Phase 1 files, all present 2026-10-31 (line counts): startup `cooking/potions.js` 308, `cooking/alcohol_drinks.js` 91,
  `cooking/food_values.js` 66, `items/potion_buckets.js` 86, `func/potion_area.js` 187, `magic/magic_resistance.js` 33,
  `func/armor_effects.js` 269, `func/tool_effects.js` 156; server `cooking/alcohol.js` 85, `other/jeed_providers.js` 216,
  `engineering/potion_bottling.js` 107, `engineering/potion_buckets.js` 57, `equipment/tipped_arrows.js` 51,
  `magic/lava_vision.js` 27, `magic/armor_effects.js` 108, `magic/tool_effects.js` 75; client `potion_buckets.js` 72,
  `armor_effects_tooltip.js` 52, `tool_effects_tooltip.js` 26. Splits and the CD compost files: creative_brewing.md 4.2.
- Phase 2 and 3 inventory (classes, mixins, `CreativeEngineering.java` 84-86 / 156-165 / 376-379, CEConfig line ranges):
  creative_brewing.md 7.

### Build
1. **Prerequisite:** SQ-CBJ-01 to -05 pass (bundle boots, pack order `creativedelight` before `creativebrewing` in
   `logs/kubejs/startup.log`).
2. **Phase 1, one group per session** (each: move the file whole, no copy left, a 4.5 log line; mixed files split):
   a. Startup: `potions.js`, `potion_area.js`, `potion_buckets.js`, `magic_resistance.js`, `alcohol_drinks.js`,
      `food_values.js` into `development/creativeBrewing/src/main/resources/kubejs_startup/<modid>/` (`minecraft/`,
      `creativeengineering/`, `farmersrespite/` ...). Grep each file's top-level `var` names first: only `global`
      crosses packs (`global.alcohol`, `global.potionArea`, `global.foodEffectSwaps`, `global.effect` are already global).
   b. Server: the server files into `kubejs/data/creativebrewing/kubejs/<modid>/`, each headed `// requires: <modid>`,
      member rows behind `if (!global.CB || !global.CB.on('<id>')) return`, recipes inside `global.CB.recipes(...)`.
   c. Client: `potion_buckets.js`, the effect tooltips, then the splits (`tooltips.js` Brewin' / Vinery lines,
      `namechanges.js`, `fluid_jei.js` 42-63) into `kubejs/assets/creativebrewing/kubejs/<modid>/`.
   d. Gear effects (`armor_effects.js`, `tool_effects.js`, both sides) and the Winemaker trade split
      (`economy/wandering_trades.js` 76-89, `func/trade_helper.js` 93-114).
   e. Generators `gen_effect_descriptions.py`, `gen_coaster_models.py`: an `outRoot` in `tools/generators.json`
      (`tools/gen_out.py` must allow the CB trees; today it allows only CD's, creative_delight.md 28.2).
   After each group: `./gradlew :creativeBrewing:build --offline`, install with the game closed, restart; SQ-CB rows.
3. **Phase 2** (patcherMod + CB together): `potion/`, `fluidbrewing/`, the potion compat and JEI classes, the effect,
   machine and JEI mixins into CB compartments (`mixin.minecraft.`, `mixin.create.`, `mixin.immersiveengineering.`,
   `mixin.jei.`, `mixin.jeed.`); registrations into `CreativeBrewing`; CB gets its own `@JeiPlugin` and
   `waila_plugins.json`. Ids stay `creativeengineering:*` (a `DeferredRegister` for that namespace, as Ender's
   Additions does). A class another patcherMod class calls stays, or leaves an interface in patcherMod.
4. **Phase 3** (config): the five sections into `CBConfig` with the same keys and comments; copy edited values from
   `config/creativeengineering-common.toml` and `configs_edited/` by hand; a CB startup WARN while old keys remain.
5. creative_brewing.md 4.5 log and the moved features' writeups (file maps) updated per group; mod_sides unchanged.

### Ids and data
No id changes (rule 4.1.1): potions `creativeengineering:long_reach`, `phantom_bane`, `magic_resistance`, the
magic_resistance effect, the potion buckets. **Recipe ids do change** for moved server scripts:
`creativeengineering:<type>/<suffix>` -> `creativebrewing:<type>/<suffix>` (`global.CB.recipes`), as the CD moves did.
Dict keys stay in the pack dicts (rule 4.1.2). Lang keys keep their names (rule 4.1.5).

### Decisions
- Q3 copper poisoning: **follows the gear effects into CB** (inferred from the 2026-10-31 decision; owner to confirm,
  the row's only open decision).
- Order inside phase 1: **startup first** (potions register in registry events after every startup pack, so a later
  pack is safe), then server, then client.
- Recipe id change: **accept it** (CD set the precedent, Q24); grep JEI hiding lists, `R.remove` lists and quests
  for the old ids in the same session.

### Gotchas
- A startup script in a jar has its own scope: a top-level `var` read by another file breaks silently (memory
  `kubejs-jar-startup-scripts`).
- `jeed_providers.js` writes JSON the client reads: keep its output path.
- Startup changes need a CB rebuild and restart; server and client scripts in the CB trees load from the pack folder on
  `/reload` / F3+T during development.
- patcherMod must never depend on CB (CB is optional to it).
- Dev-only `_lib/dump_potions.js` and the shared `effect_dict.js`, `EffectTickContext`, trader system, Dragon Breathing
  fan stay (creative_brewing.md 4.2 "Stays").

### Tests (proposed)
- CBR-new1 (proposed, numbered when queued): after phase 1, the pack's potion, alcohol and effect content is unchanged in game: JEI potion
  pages, a potion bucket, an alcohol tooltip, a JEED effect page, an armor effect tooltip (spot checks; full suites in
  the moved features' writeups).
- CBR-new2 (proposed, numbered when queued): after phase 2, the JEI potion categories, WTHIT coaster lines and effect HUD icons show as before;
  `latest.log` has no mixin apply errors from CB.
- CBR-new3 (proposed, numbered when queued): after phase 3, edited values in `creativebrewing-common.toml` match the old CE file; no WARN.
- Scratch: an SQ-CB row per group (boot clean, recipe count per type unchanged apart from the namespace, using the
  move-diff harness of `creativedelight/framework #6`).
