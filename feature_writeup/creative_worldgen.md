# Creative Worldgen

Written 2026-11-02 (Creative Split wave 5, [creative_split.md](creative_split.md) 7.6 and 7.10; TODO `creativesplit/worldgen #8`).
Status: **carved 2026-11-02, built, NOT installed; scratch SQ-CWG queued, nothing booted.**

Written for: the owner and agents working on the split. Own mod `creativeworldgen` (development/creativeWorldgen,
`creativeworldgen-1.0.0.jar`, side both): the pack's world generation and fauna. A bundle like Creative Delight: its
own Java plus two nested members. Same behaviour as before the carve; every registry id keeps `creativeengineering:`.

## 1. What it holds

- **Java** (`com.SaintAlfonzo.creativeworldgen`, same subpackages as in patcherMod; 59 classes plus 4 new):
  - `worldgen/` whole (structures, meta structures with the punch piece and meteorite heights, locate, earth and
    water fill, terrain adaptation, ground match, sky islands, bee nests, biome layout tuner, pool tuner, spawn
    tuning and conditions, spawn boxes, ocean geodes, distant clip, `donor/` Farmer's Structures donor).
  - `compat/AsyncLocatorBridge`, `AsyncMapTrades`; `mobs/NamedMobs`; `mooshroom/` (all mooshroom spawning and
    breeding, 7.6); `forgivingvoid/`; `loot/BiomeTagCondition`; `perf/MobCapLevels`; `block/FastLeafDecayPort`,
    `LeafDecayPiles`, `LeafDecayRange`.
  - New: `CreativeWorldgen` (main class, every registration that left `CreativeEngineering.java`; the
    `DeferredRegister`s use namespace `creativeengineering`, `CreativeWorldgen.NS`), `CWGConfig`,
    `WorldgenMixinPlugin` (a `GatedMixinPlugin`), `CWGResources` (copy of `CDResources`: `text(path)` reads a jar
    resource for the loot framework's json).
- **Mixins:** `creativeworldgen.mixins.json`, 60 common, none client, SRG with `remap = false`, member prefixes still
  `creativeengineering$`. The 54 sweep B Worldgen rows, the two mooshroom mating mixins, `NaturalSpawnerBoxHeightMixin`,
  `StructureStartMetaMonumentMixin`, `QuarkSpiralSpireFootprintMixin`, `AeMeteoriteHeightMixin` (the last four
  postdate the audit), and `BubbleColumnImmunityMixin` (its entity tag moved with `entitytags.js`). Gated on their
  mod: LC and Cloud Storage map trades, Forgiving Void, Bushier Flowers, two Quark, two MCA, AE2. The rest target
  vanilla. `QuarkSpiralSpiresTeleportMixin` stays in Common (a perf fix).
- **Config:** `config/creativeworldgen-common.toml` (`CWGConfig`): `[forgiving_void]` (3 subtables), `[leaf_decay]`,
  `[raids]`, `[perf]` (the 13 mob cap and spawn chunk / band keys only), `[worldgen]` with every subtable,
  `[named_mobs]`. Keys, comments, defaults and field names unchanged. First boot after the carve: Common's
  `ConfigRename.carrySections` copies the tables with the player's values from `creativeengineering-common.toml`
  (only when the new file does not exist); Forge then drops them from the CE file. NCH finds the sections through
  `global.NCH.registerConfig('creativeworldgen', ...CWGConfig)` (startup `cwg_core.js`).
- **Resources in the jar** (same paths, `creativeengineering` namespace): `data/creativeengineering/dimension` (11),
  `dimension_type` (1), `worldgen/noise_settings` (3), the named-mob chat lang key; the data tree
  `data/more_mooblooms/moobloom_spawns/` (authored in the pack folder, copied at build).
- **Scripts:** startup `kubejs_startup/creativeworldgen/cwg_core.js` (`global.CWG`: `modId`, `on()`, `recipes()`,
  `lootRules()`); server `kubejs/data/creativeworldgen/kubejs/`: `worldgen/` (11), `loot/` (11 plus
  `loot_config.json`, `loot_index.json`), `other/removed_mobs.js`, `tags/entitytags.js`, `loottags.js`, the Worldgen
  half of `blocktags.js` (villager ladders, ground match, earth fill, template trim, wild plant soils),
  `mobs/end_divers.js`; opened by `_worldgen_core.js`. No client tree. No recipes, so no recipe id changes.
- **Members** (`development/creativeWorldgen/members/`, mod ids unchanged, no swap list): More Mooblooms
  (`more_mooblooms`, a standalone jar before) and Baby Mobs (`baby_mobs`, in Creative Delight 11-01 to 11-02).

## 2. Edges

- Common never imports Worldgen: after the move only `CreativeEngineering.java` and `CEConfig` named moved classes, and
  both were trimmed. No new Common hook was needed. `WthitHive` reads no Worldgen class.
- Devtools (`PerfCommand` mobcap, `SpawnDump` tuning block, `MassPlacer`) calls Worldgen behind
  `ModList.isLoaded("creativeworldgen")`; optional dependency in its mods.toml.
- **Loot rules from Creative Delight.** KubeJS loads namespace script packs in an unordered order, so CD's four
  `loot.js` (Rustic, Veggies, Vintage, Miner's) can run before Worldgen's `05_tuning.js`. They now queue their rules
  with `global.CWG.lootRules(fn)`; `05_tuning.js` runs the queue in `ServerEvents.highPriorityData`, before the tag
  capture, so rules apply in the old order.
- `global.biomeRules` (Worldgen `_biome_rules.js`): CD's `wild_crops.js` reads it at event time and warns once and
  skips when it is missing.
- `05_tuning.js` reads `guardThrowable` and `writeExportJson` from `global` (pack-folder top-level names are not
  visible to a jar script pack). It reads the loot json from the pack folder first, then the jar (`CWGResources`).
- Requires `creativeengineering [1.1.08,)` and `creativecompat [1.0.0,)`, mandatory AFTER.

## 3. Left in the pack folder or elsewhere on purpose

- The structure datapacks (49 namespaces), Eugene, `watchtower_building` and the `creativeengineering` worldgen data
  (incl. `waystone_ruin_part`, `moobloom_cows.json`): the structure generators (`tools/gen_structure_config.py` and
  the scanners) tell jar sets from kubejs/data sets by source, so moving them changes their output.
- Overrides of loaded mods' data (`campsite_structures`, `ipo`, `philipsruins`, `lightmanscurrency` coin loot,
  `terralith`, `minecraft/` and the rest of E 3.2): a mod jar cannot sort above the target mod's jar.
- `dimension/PortalFailureHandler` (Creative Magic, wave 6); `trader/` and `[wandering_trader]` (Common, 7.6);
  `WorldgenFiles` (Common `util/`).
- Deleted: `watchtower_building/advancements/watchtower_found.json` (the pack drops advancements).

## 4. Known effects of the move

- The biome layout disk cache rebuilds once (the solver class bytes changed).
- Event listener order shifts a little: Worldgen's registrations run in its constructor, after patcherMod and Compat.
- LootJS modifier order: Worldgen's post-roll modifier now registers after the pack's and CD's item-merge modifiers.
- `95_tag_capture` sees tag additions from packs that load before Worldgen (more than before, not less).

## 5. Tests

Scratch SQ-CWG-01 to -10 ([scratch_queue.md](testing/scratch_queue.md)). In game (client rows pinned until wave 8+,
7.8): CWG-01 More Mooblooms and baby squid render with models; CWG-02 `/celocate` and a map trade from a cartographer.

## 6. History

- 2026-11-02: carved (wave 5). patcherMod 1.1.08 loses the classes, 60 mixins, 6 sections and the resources; More
  Mooblooms and Baby Mobs nested; Devtools gains the optional dependency. Edits that the worldgen edit pass
  ([plan_worldgen_edit.md](plan_worldgen_edit.md), same day) made to `MetaStructure` and its new classes moved with it.
