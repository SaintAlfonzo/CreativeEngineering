---
name: integration-report
description: Write an integration report for a new or updated mod (e.g. a Farmer's Delight addon) - overlap with the pack, what is new, what must be folded into pack systems. Investigation only, nothing built.
---

# Integration report

Output: one writeup `feature_writeup/<mod_snake>.md`, investigation only (nothing built unless the owner
says so). Models: [hostile_delight.md](../../../feature_writeup/hostile_delight.md) (full report),
[unusual_delight.md](../../../feature_writeup/unusual_delight.md) (overlap-first order),
[more_delight.md](../../../feature_writeup/more_delight.md) (newer jar against an installed one). Read the model's
section you are writing, not the whole file.

## 0. Gather (before writing)

1. **Jar and facts.** Locate the jar (`mods/`, `devmods/`, a donor folder). Mod id, version, MC/loader version,
   licence (decides the delivery path: GPL-compatible = fork allowed; ARR/AFL/Abnormals = untouched jar or donor
   jar, our code written from behaviour), dependencies. Unpack to `DecompiledMods/<jar-name>/`;
   `python3 tools/decompile_mod.py <jar> <out>` for code. Updated jar: `python3 tools/jar_diff.py OLD NEW`
   (method: [devmods_evaluation.md](../../../feature_writeup/devmods_evaluation.md) section 1).
2. **Inventory** by registry id and data path: items, blocks, effects, potions, recipes (count by type), tags, loot
   (added vs replaced tables, GLMs), worldgen, entities, trades, structures, config, advancements, lang, tooltips,
   code-only behaviour (on-eat procedures, events, mixins). Note upstream bugs (`logs/latest.log`:
   `Failed to parse`, the mod id).
3. **Pack state.** grep for the namespace in `kubejs/`, `development/*/src`, `item_dict.js`. Check whether the
   dumps in `kubejs/exported/` predate the jar (then `/dumpall` is step 0 of the fold-in).

## 1. Report order (sections)

Title `# <Mod>: overlap and integration report`. Opening paragraph: date, "nothing built", jar, licence,
whether a port is needed, short name used in the file, test prefix (3+ letters, grep-safe, unused: grep
`feature_writeup` for it). Scope rule paragraph: integration adds no recipe or mechanic that is not in the
mod or defined by a pack rule unless the owner asks by name; upstream bugs are fixed.

- **0. At a glance**: table Part / Where / Status (inventory counts, bugs, overlap, fold-in, decisions D1-Dn,
  tests PREFIX-01..NN drafted); bullets for jar, licence, dependencies, pack references, already-live effects.
- **1. Overview** and **Facts** table (mod id, items, blocks, effects, potions, recipes by type, loot, tags,
  advancements "the pack drops all advancements", lang, tooltips, config).
- **2. Overlap (first)**. Every item and system checked against the pack and every installed mod:
  - Items: search `item_dict.js`, the name dump (`kubejs/exported/` display names), [item_merges.md](../../../feature_writeup/item_merges.md),
    and overlap tables in sibling writeups (other FD addons, CD members). Table Item / Overlaps with / Problem / Proposal.
  - Mob drops, dishes, ingredients (same food under another id = merge candidate).
  - Systems: loot, potions, effects (same effect under another id), worldgen, crops/seasons, machines.
  - Overlap with other mods being integrated in the same batch.
  - Mark every overlapping row `*overlaps w/ <what>*` in the item tables.
- **3. New content**: items and blocks by line (tables, values: nutrition / saturation, effects, stack), new
  effects, new mechanics, recipes, loot, upstream bugs (B1-Bn, with log evidence).
- **4. Fold-in, in build order**: numbered steps, each naming the pack system and its doc. Walk this checklist
  and write a step for every one that applies ("None (reason)" for the rest):
  dumps + generators (memory `dumpall-refresh-workflow`); ids in `item_dict.js` / block dict; tags via
  `global.tags`; upstream recipe removals, literal inputs to tags; mob loot ([loot.md](../../../feature_writeup/loot.md));
  item merges; potions ([potions.md](../../../feature_writeup/potions.md), mixers, [potion_buckets.md](../../../feature_writeup/potion_buckets.md), tipped arrows);
  recipes by pack rules through `global.NCH` ([recipes_cooking.md](../../../feature_writeup/recipes_cooking.md): heatless `_manual_only`, rolls,
  sandwiches; [oven.md](../../../feature_writeup/oven.md) baking; S&D and Central Kitchen mirrors; container remainders);
  effects ([effects.md](../../../feature_writeup/effects.md), JEED text); food types and values (`food_types.json`, [food.md](../../../feature_writeup/food.md));
  food families (`tools/food_families.py --check`); stack sizes ([stack_sizes.md](../../../feature_writeup/stack_sizes.md)); cans and jars
  ([cans.md](../../../feature_writeup/cans.md)); compost; crops, wild crops, seasons ([crops.md](../../../feature_writeup/crops.md)); worldgen / structures;
  names and lang ([names.md](../../../feature_writeup/names.md)); mob and block audit (BlockRunner tier, mineable tags, Item Physic, WTHIT);
  tooltips ([tooltips.md](../../../feature_writeup/tooltips.md)) and JEI ([jei_coverage.md](../../../feature_writeup/jei_coverage.md));
  placeables / Display Delight; trades and chests; release (mod_sides note, changelog bullets, TODO, plan row).
  End with the ordering constraints ("step 0 before 9-13 ...").
  Food and cooking code is routed into Creative Delight trees (memory `creative-delight-routing`): say where each step's files go.
- **5. Decisions for the owner**: table `# | Question | Recommendation | Default if not answered`, D1-Dn.
- **6. File map (planned)**, **7. Offline checks (once built)**, **8. In-game test suite (drafted)**
  (`| ID | Test | Pass |`), **9. History** (dated line: "investigation written, nothing built").

## 2. Also, same session

- [feature_writeup/README.md](../../../feature_writeup/README.md) index row (status: planned), TODO entry in
  [todo/TODO.md](../../../feature_writeup/todo/TODO.md) (usually under `mod-additions`), cross-links from sibling
  writeups whose overlap tables gained rows.
- No changelog bullet (nothing player-visible yet); say so in the hand-over.
- Hand-over: counts, top overlaps, the decisions that block building, test prefix.
