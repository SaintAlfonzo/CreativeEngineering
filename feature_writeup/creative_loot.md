# Creative Loot: the pack's loot engine in one mod

Written 2026-11-17 (plan_rules_fixes.md section 5b, D13; section 5, D14, D25; TODO `conventions #16`, `#15`).

Written for: the owner and agents working on loot. Own mod `creativeloot` (development/creativeLoot,
`creativeloot-1.0.0.jar`, side both), package `com.SaintAlfonzo.creativeloot`, no config file, script packs
`kubejs_startup/creativeloot/` (in the jar) and `kubejs/data/creativeloot/kubejs/` (pack folder, copied into the jar).
Split out of Creative Worldgen with the same behavior: loot is not worldgen, and Creative Delight and Creative Brewing
need the engine without needing Worldgen. How the engine tunes loot (pre-roll rewrite, post-roll LootJS layer, bans,
packed food, drops) is [loot.md](loot.md); this file is the jar.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=ca442e9150f0 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `conventions #15` | Testing | SQ-LOOT-02, SQ-RFX-16 to -18 | Global loot modifiers |
| `conventions #16` | Testing | SQ-LOOT-01 to -06, SQ-RFX-30, -32 | Split Creative Loot out of Creative Worldgen |
<!-- generated:status_index end open_work -->

- Requires Creative Common, Creative Compat and LootJS (mandatory, AFTER). Required by Creative Worldgen (since
  2026-11-17); Creative Delight and Creative Brewing add the requirement with their next rebuild (plan P3, D25).
- Registers the ancient corpse feature, its configured and placed features and the `creativeloot:ancient_corpses`
  biome modifier serializer (section 2c, since 2026-11-18); nothing else. No config file: every knob is in `loot/loot_config.json`. Worldgen never had a `[loot]` config section,
  so there was nothing to carry with `ConfigRename`.

## 1. What it holds

- **Startup** `kubejs_startup/creativeloot/loot_core.js`: `global.LOOT` with `modId`, `_queue` and `rules(fn)`. A script
  in another pack (Creative Delight's four `loot.js`) queues its rules with `global.LOOT.rules(L => { ... })`; KubeJS loads
  script packs in namespace hash order, so the queue exists before any of them runs. `loot/05_tuning.js` runs the queue
  in `ServerEvents.highPriorityData` (before the tag event that captures `#tag` members) and empties it. The startup log
  prints `[creativeloot] core starting` / `core finished` once.
- **Server scripts** `kubejs/data/creativeloot/kubejs/`: `loot/00_lib.js` (adds the LootJS helpers to `global.LOOT` on
  every reload, keeping `rules` and the queue), `05_tuning.js` (the pre-roll engine, `global.lootTuning`, `/loottuning`),
  `06_dead_maps.js`, `07_rules.js`, `08_leaf_apples.js`, `09_drops.js`, `10_removals.js` to `40_targeted.js` (LootJS),
  `95_tag_capture.js`, `99_debug.js`; `tags/loottags.js` (the loot group tags `#creativeloot:loot/chalk`
  and `#creativeloot:loot/golden_food`, `#creativeworldgen:loot/` before 2026-11-17; dict keys `i.t_loot_chalk`, `i.t_loot_golden_food`). Data next to the
  scripts: `loot/loot_config.json` (hand-edited knobs), `loot/loot_index.json` (generated), `loot/loot_census.md` and
  `loot/readme.md` (not copied into the jar).
- **Java**: `CreativeLoot` (main class, logs `[creativeloot] loaded`), `CLResources` (was Worldgen's `CWGResources`:
  `text(path)` returns a jar resource, so `05_tuning.js` reads the tables from the pack folder in dev and from the jar
  otherwise), `GlmMerge` and `mixin/LootModifierManagerMixin` (section 2).
- **Tools** that read or write the tree: `tools/gen_loot_config.py` (`loot_config` generator), `tools/loot_census.py`,
  `tools/loot_breakdown.py`, `tools/optional_ref_audit.py`, `tools/loot_tuning_sim.js` (node sim: runs `loot_core.js`,
  `05_tuning.js`, `07_rules.js`, `09_drops.js`, `loottags.js`, `95_tag_capture.js`; `node tools/loot_tuning_sim.js`).

## 2. GlmMerge: one global loot modifier list

Forge 1.19.2's `LootModifierManager.apply` reads every copy of `data/forge/loot_modifiers/global_loot_modifiers.json`
in the resource stack, but a copy with `"replace": true` clears every entry read before it, and the stack order is the
mod load order. D14: design for the worst case, where only one copy counts.

- `mixin/LootModifierManagerMixin` injects at the end of Forge's `apply(Map, ResourceManager, ProfilerFiller)`
  (`remap = false`), so the merge runs on the same reload, after Forge has built its map, and replaces that map. Forge's
  own method keeps the name `apply` in the production jar; only the erased bridge `m_5787_(Object, ...)` has the SRG
  name. Until 2026-11-18 the target was `m_5787_(Map, ...)`, which matches nothing, so no jar fragment ran (r1118 F10,
  `conventions #38`). `creativeloot.mixins.json` sets `injectors.defaultRequire 1`: a missed inject now fails at load.
- `GlmMerge.merge` keeps Forge's entries in their order and appends every entry Forge's map lacks from (a) every copy of
  the Forge list (`ResourceManager.getResourceStack`) and (b) every fragment `data/<namespace>/glm_entries.json`
  (`{"entries": ["ns:name", ...]}`). Each is decoded from its definition `data/<ns>/loot_modifiers/<name>.json`.
- Skipped, and named in a second log line: a missing definition, a serializer (`"type"`) that is not registered (its mod
  is gone), a decode error. Duplicates are dropped by entry id.
- Gates: `GlmMerge.gate(Predicate<String>)` turns fragments off by namespace. Creative Delight and Creative Brewing
  register their member switch there in their constructors (`ns -> !CDConfig.isMember(ns) || CDConfig.member(ns)`, the
  same with `CBConfig`), so a member switched off loses its modifiers.
- The fragments (no own jar ships the Forge list; `design_rules_audit.py glm` checks it and that every entry has a
  definition): `hearthandharvest` 2, `veggiesdelight` 5, `rusticdelight` 2, `ends_delight` 15 (definitions in End's
  Delight's upstream jar; its own list is left out by `replaced_resources`), `vintagedelight` 1, `vinery` 1 (Delight's
  `packs/mods/vinery/`, the cherry leaves modifier, gated by the Vinery switch), `brewinandchewin` 4, `exposure` 5,
  `exposure_expanded` 21: 9 files, 56 entries. The Rustic bake and the Brewin', Veggies and Vintage port scripts write
  fragments. Third-party lists (Farmer's Delight, Delightful, Nether's, Ocean's repacks unchanged, other mods) are read
  as before.
- One log line per reload: `[GlmMerge] N entries (lists: a files, b entries; fragments: c files, d entries, e gated off;
  Forge read f, added g, skipped h)`. `added` counts the fragment entries Forge no longer reads itself.
- Creative Loot ships no list and no fragment (no modifiers of its own).

## 2b. Drop rules of other jars

Creative Delight's loot scripts declare rules only; the drops engine (`loot/09_drops.js`) applies them (plan section
12d, V-38). Shapes besides `drop` and `dropShare`: `swapDrop(tables, item, into, opts)` (the item's entries drop only
when the conditions fail, a pool gives `into` when they hold: knife scavenging), `replaceDrops`, `rewrite(tables, fn,
name)` (raw table edits: soil yield, bell pepper weights), `afterTables(fn)` (summary log lines) and the post-roll
`onlyDrop(entities, item, { logName })` (a LootJS modifier, after every global loot modifier: the squid chain). New
`drop` options: `silkTouch`, `state` (with `stateBlock`), `holding`, `notHolding`, `killedByPlayer`; `knife` on a block
table tests the breaking tool. Log: `[loot-drops] N drop pools and M shared entries in T tables` and `[loot-drops] S
swaps and R table rewrites`. `_core/item_merges.js` (Common's item-merge runner) stays a LootJS modifier.

## 2c. Ancient corpses and Visceral Heap remains (2026-11-18)

[plan_corpses.md](plan_corpses.md) 3.2 and 3.4, TODO `corpses #2`, `#5`, `#7`. Only with the Corpse mod loaded; the
decay, names and render are Creative Compat's ([creative_compat.md](creative_compat.md), Corpse decay).

- **Java** `corpse/`: `AncientCorpseModifier` (the serializer, always registered; `data/creativeloot/forge/biome_modifier/ancient_corpses.json`
  adds the placed feature `creativeloot:ancient_corpse` to `#minecraft:is_overworld` biomes and the remains biomes,
  step underground decoration, only when `corpse` is loaded and `enabled`), `AncientCorpseFeature` (placement and
  spawn), `HeightCurve`, `AncientArmor` (armor rolls), `AncientHands` (held items), `AncientCorpseConfig` (reads the block below on server start and
  /reload), `AncientCorpseEvents` and `CorpseHooks` (the only class touching Corpse classes; loaded only with the mod).
- **Per chunk** (new chunks only): cave roll (`cave.chunk_chance` 1/32, overworld), ocean roll (chunk center biome in
  `#minecraft:is_ocean`), remains roll (`remains.chunk_chance` 1/8; the spot's biome must be in `remains.biomes`).
- **Spot** (ruling 11): a `footprint` x `footprint` (3x3) floor of full blocks at one height, `headroom` (3) blocks of
  air above all of it (water on the ocean floor). Cave: a target height drawn from `cave.y_curve`, at most `max_y`
  (48) and below sea level, below the surface; up to `attempts` (12) columns scanned `search` (8) blocks around it.
  Ocean: the ocean floor heightmap under at least `headroom` blocks of water.
- **Height curve** (ruling 12): points `[-64, 1.0] [-32, 0.85] [0, 0.65] [16, 0.5] [24, 0.3] [32, 0.04] [48, 0.01]`. Drawn
  target heights (Java sampler, 200000 draws): 92.3% below y16, 78.4% below y0, 0.6% at y32 or above; density at
  y-64 is 25x that at y32. Ruling 12 asked for about 80% below y16 (flexible) and the largest gap: a gap that large
  leaves little room above y16, so the gap won. Actual corpses also depend on where caves have floors; the worldgen
  survey row (SQ-LOOT-08) reports the real spread.
- **Ocean rate** (ruling 13): `base_chunk_chance` 0.00347 (about 1/288) x `temperature_factor` (warm 1, neutral 2,
  cold 3; cold and frozen oceans cold, lukewarm and warm oceans warm, plain and modded oceans neutral) x
  `depth_factor` (shallow 1, deep 6): warm shallow about 1 per 288 chunks, cold deep about 1 per 16.
- **The corpse** is built from NBT (`EntityType.loadEntityRecursive`, no Corpse classes): a Death with the rolled
  armor and held items (main hand in hotbar slot 0 plus the `Equipment` snapshot, offhand in `OffHandInventory`),
  Compat's `creativecompat:corpse` compound (`Ancient` or `Remains`, stage skeleton) and Forge persistent data:
  `creativeloot:corpse_loot` (the chest table rolled on first open, ruling 5), or `creativeloot:corpse_remains` for
  remains. Every ancient corpse holds at least one item (held items below), so none is empty before it is opened; the
  old `creativecompat:corpse_sealed` mark is gone (ruling 24; no migration of first-build corpses, ruling 29). Ocean
  corpses keep gravity and sink, as every skeleton corpse does (Compat `skeletonsSink`, ruling 27).
- **First open** (`CorpseHooks.onInteract`): the table is rolled as chest loot (LootJS and the tuning layer apply) into
  the free main inventory slots, overflow into additional items; the key is cleared. Tables `creativeloot:corpse/cave` and
  `corpse/ocean` are placeholders (torches and bread; bread) until `corpses #3` fills them.
- **Remains** (rulings 21, 22): plain skeletons, empty, never wither or despawn, named "Ancient Remains". A player's hit
  drops `remains.drops` (1 to 3 bones, 0 to 1 bone chunk, 0 to 2 cracked bones, a skeleton skull at 25%; rolled in
  Java, a missing item is skipped) and removes them; no other corpse can be broken.
- **Armor** (`AncientArmor`, rulings 7, 14 to 17): one of `chains` at random, slots rolled at `chances` 56 / 35 / 15 /
  5 % until the first miss; pieces 1 and 2 roll `material_weights` (wood, leather, dyed leather, copper 25; chain,
  iron 8), later pieces copy one of them 50/50; `damage` 30% to 90% of durability used; `enchant_chance` 5% per piece
  at level 5 to 15. Dyed leather: `same_color_chance` 50% one color for every dyed piece, else two colors at least
  `min_distance` 200 apart in RGB, each piece taking one. Copper: unwaxed pieces fully oxidized; `wax_chance` 10%
  waxed, a waxed piece unaffected (weight 0.5), exposed or weathered (1 each), one state for all waxed pieces or two
  (`waxed_same_state_chance` 50%).
- **Held items** (`AncientHands`, rulings 24 to 26, 28; TODO `corpses #8`), block `hands`: main hand at `main_chance`
  45%; offhand on its own roll at `offhand_chance` 20% (not chained to the main hand): the kind's `offhand_supplies`
  stack at `offhand_supply_chance` 50% (cave 1 to 6 torches, ocean 1 to 3 bread), else a tool. Tools: sword or pickaxe
  (`tool_weights` 1 : 1) in wood, stone, copper 25 or iron 8 (`material_weights`); an offhand tool copies the main
  hand's material at `same_material_chance` 50%; `damage` 30% to 90%, `enchant_chance` 5% at level 5 to 15. Copper
  tools follow the armor's copper rules (`armor.copper`) and share the corpse's waxed states with its armor. No dye.
  `guarantee_tool`: a corpse whose armor and hands all missed gets a main hand tool. Shares: 44% roll no armor, so
  about 0.44 x 0.55 x 0.8 = 19% of corpses take the guaranteed tool.
- **Config** `loot_config.json` block `ancient_corpses` (every value above; `tools/gen_loot_config.py` keeps blocks it
  does not own since 2026-11-18). Worldgen values apply to chunks generated after the change. A block with
  `"requires": <mod id>` is skipped when the mod is absent (the remains on Biomes O' Plenty, the wood and copper armor
  sets on Immersive Armors and Everything Is Copper, the bone chunk and cracked bone drops on Philip's Ruins), which
  keeps `tools/optional_ref_audit.py` clean. Loot tables in the jar: `corpse/cave`, `corpse/ocean` only.
- **Lang** `assets/creativeloot/lang/en_us.json`: `entity.creativeloot.ancient_corpse`, `entity.creativeloot.ancient_remains`.

## 3. Edges

- `global.CWG.lootRules` is gone (no alias): the four Creative Delight callers (`miners_delight/loot.js`,
  `rusticdelight/loot.js`, `veggiesdelight/loot.js`, `vintagedelight/loot.js`) call `global.LOOT.rules` with no guard
  (D25), as do Corn, Delightful, Farmer's, `crop_drops.js` and `soil_yield.js` since P3c; Creative Brewing requires
  Creative Loot too.
- `06_dead_maps.js` and `07_rules.js` hold the structure-related chest rules (dead explorer maps, village chests); they
  read only loot tables and `loot_config.json`, nothing of Worldgen's.
- Exposure's fragments are read whenever Creative Loot is present; Exposure does not depend on it.
- LootJS log names come from `R.logName` (`R = global.NCH.forPack('creativeloot')`): `CL/tuning`, `CL/removals/...` (`CE/loot/...` before 2026-11-17).

## 4. Tests

- [ ] **LOOT-01 Boot:** fresh server boot -> startup log has `[creativeloot] core finished` once, the server log
  `global.LOOT ready` and `[loot-tuning] 5 rules -> rewrote 184 tables` (the 2026-11-16 count before the split). Queue SQ-LOOT-01.
- [ ] **LOOT-02 GlmMerge line:** same boot -> one `[GlmMerge] N entries` line per reload, N at least Forge's count,
  no `could not read` line. Queue SQ-LOOT-02.
- [ ] **LOOT-03 Loot dump unchanged:** `/dumpall` loot dump before and after the split -> diff empty. Queue SQ-LOOT-03.
- [ ] **LOOT-04 Structure chest probe:** chests with a structure loot table roll -> loot as before, no banned item. Queue SQ-LOOT-04.
- [ ] **LOOT-05 Jar copy of the tables:** pack-folder `loot_config.json` / `loot_index.json` moved away -> the tables load
  from the jar, same rule count. Queue SQ-LOOT-05 (replaces SQ-CWG-05).
- [ ] **LOOT-06 Settings carried:** `loot_config.json` settings and the queued Creative Delight crate rules are the same
  as before (the file moved unchanged; Worldgen had no `[loot]` toml section). Queue SQ-LOOT-06.
- [ ] **LOOT-07 Ancient corpse spawn:** a corpse summoned with the feature's NBT -> skeleton, "Ancient Corpse", armor
  shown; first open rolls the cave table; looted, it is gone 5 min later. Queue SQ-LOOT-07, CQ-LOOT-10.
- [ ] **LOOT-08 Worldgen survey:** pregenerate 64x64 chunks of a fresh world -> ancient corpses at about the configured
  rates, cave ones below y48 and mostly deep, ocean ones on the floor. Queue SQ-LOOT-08.
- [ ] **LOOT-11 Held items:** summon 40 corpses with the feature (`/cdevplace` or a fresh area) -> some hold a sword or
  pickaxe in the main hand (wood, stone, copper, iron; worn; copper oxidized or waxed), some an offhand tool or torches
  (bread underwater); none is empty before opening; taking the held item off the corpse removes it from the model.
  Ocean ones have no `NoGravity` (sinking itself: Compat SQ-CMP-16). Queue SQ-LOOT-11, CQ-LOOT-12.
- [ ] **LOOT-09 Remains:** remains in the Visceral Heap -> a hit breaks them and drops bones; they never despawn. Queue
  SQ-LOOT-09.

## 5. History

- 2026-11-23: `L.ban` records its mod (`mod` option, default from an `@mod` target) and `checkBanSwitches` skips a ban whose mod is not loaded with one INFO line (`loot-tables #3`). `swapDrop` with several conditions writes an alternative of inverted terms instead of `minecraft:all_of`, which 1.19.2 lacks (`creativebrewing/publish #13`); the modded composter tables (twisted, ancient, blossom, azalea) carry a top-level `forge:conditions` mod_loaded.
- 2026-11-18: held items and the no-empty guarantee (`hands` block, `AncientHands`; TODO `corpses #8`); sealed mark and ocean `NoGravity` dropped (`corpses #9`); SQ-LOOT-11, CQ-LOOT-12.
- 2026-11-18: ancient corpses, their armor rolls and the Visceral Heap remains (section 2c; plan_corpses.md, TODO `corpses #2`, `#5`, `#7`; SQ-LOOT-07 to -09, CQ-LOOT-10).
- 2026-11-17: P3c of plan_rules_fixes.md: every member list became a fragment, Delight and Brewing gate them (section 5);
  the drops engine gained the shapes of section 2b and Creative Delight's own loot rules moved into it (12d). Queue
  SQ-RFX-16 to -18, -21 to -23; SQ-LOOT-02 and SQ-MDL-01 updated.

- 2026-11-17: F5 of plan_rules_fixes_followup.md checked: the loot group tags are `#creativeloot:loot/*` in Common's dict (`i.t_loot_chalk`, `i.t_loot_golden_food`; no key name mentions worldgen), `tags/loottags.js`, the loot rules and the docs; nothing reads `creativeworldgen:loot/` any more except `tools/dump_diff.py --rename` (maps old baselines) and History lines. Queue SQ-RFX-30, -32.
- 2026-11-17: P3a of plan_rules_fixes.md: LootJS names through `R.logName` (`CL/tuning`, `CL/removals/...`), the loot group tags renamed `#creativeloot:loot/chalk` and `#creativeloot:loot/golden_food` (dict keys unchanged); SQ-RFX-03, -05.
- 2026-11-17: built (P2b of plan_rules_fixes.md): the jar, the loot tree and `loottags.js` moved from Creative Worldgen,
  `global.CWG.lootRules` -> `global.LOOT.rules` with the four Creative Delight callers, `CWGResources` -> `CLResources`,
  the LootJS dependency moved from Worldgen, GlmMerge written; `tools/loot_tuning_sim.js` repointed (TODO
  `tooling/generators-and-tests #9`) and passing. `creativeloot-1.0.0.jar` and `creativeworldgen-1.0.0.jar` installed
  together; SQ-LOOT queued, nothing booted.

Lines moved out of code comments (2026-11-17, plan_rules_fixes.md section 17):

- 2026-10-18: The 2026-10-18 loot dump has no other leaf table with apples besides the two apple trees (fruitsdelight / vinery blocks/apple_leaves) (kubejs/data/creativeloot/kubejs/loot/08_leaf_apples.js)
- 2026-11-01: Oceanic Delight fishing net tags (block, coral item and biome): Creative Delight (kubejs/data/creativedelight/kubejs/oceanic_delight/fishing_net_tags.js, moved 2026-11-01) (kubejs/data/creativeloot/kubejs/tags/loottags.js)
- 2026-11-03: Botania (botania:inject/village_chest: temple, toolsmith, weaponsmith) and CC:Tweaked (computercraft:treasure_disk: cartographer) are not restored: owner 2026-11-03 keeps them out of village chests (plan_scratch_bugfix_1103 7.3) (kubejs/data/creativeloot/kubejs/loot/07_rules.js)
- 2026-11-03: Botania and CC:Tweaked stay out of village chests (owner, 2026-11-03, plan_scratch_bugfix_1103 7.3, SQ-GLD-09) (kubejs/data/creativeloot/kubejs/loot/10_removals.js)
- 2026-11-17: Creative Loot (feature_writeup/creative_loot.md): the pack's loot engine, split out of Creative Worldgen on 2026-11-17 (plan_rules_fixes.md section 5b, D13) (development/creativeLoot/src/main/java/com/SaintAlfonzo/creativeloot/CreativeLoot.java)
