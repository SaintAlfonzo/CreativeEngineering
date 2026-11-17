# Creative Loot: the pack's loot engine in one mod

Written 2026-11-17 (plan_rules_fixes.md section 5b, D13; section 5, D14, D25; TODO `conventions #16`, `#15`).

Written for: the owner and agents working on loot. Own mod `creativeloot` (development/creativeLoot,
`creativeloot-1.0.0.jar`, side both), package `com.SaintAlfonzo.creativeloot`, no config file, script packs
`kubejs_startup/creativeloot/` (in the jar) and `kubejs/data/creativeloot/kubejs/` (pack folder, copied into the jar).
Split out of Creative Worldgen with the same behavior: loot is not worldgen, and Creative Delight and Creative Brewing
need the engine without needing Worldgen. How the engine tunes loot (pre-roll rewrite, post-roll LootJS layer, bans,
packed food, drops) is [loot.md](loot.md); this file is the jar.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=8a534918451a -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (In Progress 1, Testing 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `conventions #15` | In Progress | SQ-LOOT-02 | Global loot modifiers |
| `conventions #16` | Testing | SQ-LOOT-01 to -06 | Split Creative Loot out of Creative Worldgen |
<!-- generated:status_index end open_work -->

- Requires Creative Common, Creative Compat and LootJS (mandatory, AFTER). Required by Creative Worldgen (since
  2026-11-17); Creative Delight and Creative Brewing add the requirement with their next rebuild (plan P3, D25).
- Registers nothing. No config: every knob is in `loot/loot_config.json`. Worldgen never had a `[loot]` config section,
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

- `mixin/LootModifierManagerMixin` injects at the end of Forge's `apply` (SRG `m_5787_`, `remap = false`), so the merge
  runs on the same reload, after Forge has built its map, and replaces that map.
- `GlmMerge.merge` keeps Forge's entries in their order and appends every entry Forge's map lacks from (a) every copy of
  the Forge list (`ResourceManager.getResourceStack`) and (b) every fragment `data/<namespace>/glm_entries.json`
  (`{"entries": ["ns:name", ...]}`). Each is decoded from its definition `data/<ns>/loot_modifiers/<name>.json`.
- Skipped, and named in a second log line: a missing definition, a serializer (`"type"`) that is not registered (its mod
  is gone), a decode error. Duplicates are dropped by entry id.
- Gates: `GlmMerge.gate(Predicate<String>)` turns fragments off by namespace. Creative Delight and Creative Brewing
  register their member switch there when they move their lists to fragments (plan P3, section 5:
  `ns -> !CDConfig.isMember(ns) || CDConfig.member(ns)`, the same with `CBConfig`).
- One log line per reload: `[GlmMerge] N entries (lists: a files, b entries; fragments: c files, d entries, e gated off;
  Forge read f, added g, skipped h)`. Until P3 the members still ship the Forge list and no fragment exists, so
  `added` is 0 unless a `replace: true` copy dropped entries.
- Creative Loot ships no list and no fragment (no modifiers of its own).

## 3. Edges

- `global.CWG.lootRules` is gone (no alias): the four Creative Delight callers (`miners_delight/loot.js`,
  `rusticdelight/loot.js`, `veggiesdelight/loot.js`, `vintagedelight/loot.js`) call `global.LOOT.rules` with no guard
  (D25). Their pack-folder copies load in dev now; the Creative Delight jar carries the old text until its P3 rebuild.
- `06_dead_maps.js` and `07_rules.js` hold the structure-related chest rules (dead explorer maps, village chests); they
  read only loot tables and `loot_config.json`, nothing of Worldgen's.
- Exposure's fragment (P3) is read whenever Creative Loot is present; Exposure does not depend on it.
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

## 5. History

- 2026-11-17: built (P2b of plan_rules_fixes.md): the jar, the loot tree and `loottags.js` moved from Creative Worldgen,
  `global.CWG.lootRules` -> `global.LOOT.rules` with the four Creative Delight callers, `CWGResources` -> `CLResources`,
  the LootJS dependency moved from Worldgen, GlmMerge written; `tools/loot_tuning_sim.js` repointed (TODO
  `tooling/generators-and-tests #9`) and passing. `creativeloot-1.0.0.jar` and `creativeworldgen-1.0.0.jar` installed
  together; SQ-LOOT queued, nothing booted.
