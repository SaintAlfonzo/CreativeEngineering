# Rules fixes follow-up (closes the owner lists of plan_rules_fixes.md)

Status: PLANNED 2026-11-17, executing the same day. Rounds off [plan_rules_fixes.md](plan_rules_fixes.md) (built P0 to P7;
its Hand-over and Owner list sections hold the items settled here). Short names as in that plan.

## 0. Owner rulings (2026-11-17)

1. **F1 Miner's Delight (V-14)**: treated like every other member of the same license (ARR, donor-jar method). It keeps its
   upstream package (`com.sammy.minersdelight`, Create Central Kitchen targets it by class name); no repackage. It gets the
   same treatment the other ARR members get in the rule passes, notices and lints (excluded as upstream). V-14 closes as
   "Not applicable: upstream-license member".
2. **F2 Comment rewrites**: executed. Every entry of [history_extract_review.md](history_extract_review.md) is read; comments
   that read wrong are fixed or deleted; the 136 undated history-sounding comments are deleted or rewritten in present tense
   (DR-C26), history going to the writeup History.
3. **F3 Queue**: the 7 whole-copy duplicate rows are merged to one (the copy in the feature's own rows file stays; verdict
   history is kept in the run docs); the `SQ-MB-` collision is resolved by renaming (override: the owner does not care which
   prefix); the 25 queue tables with other headers become the standard table.
4. **F4 Prefixes**: any prefix is acceptable; pick the mechanical one (older doc keeps the prefix, D19) and record it.
5. **F5 Loot tags**: every loot tag lives in Creative Loot's namespace: `#creativeloot:loot/*`, dict keys and generated
   data included (P3a renamed the tag ids and left the dict key names).
6. **F6 Scope**: upstream and generated files get no rule pass (D3 stands; confirmed). Where earlier passes edited them,
   that is left; the lints skip them.
7. **F7 Float tag**: `creativecompat:physics/floats` is filled by Creative Compat's own script pack, which also holds the
   step that removes wild plants from the flower tags, so the load order is inside one jar.

## 1. Work

| Agent | Work | Rebuild |
|---|---|---|
| A (queue and docs) | F3, F4, plan Owner list items 1, 10, 11 closed; `tools/check_known.csv` drops the 7 duplicates; test_prefix_index updated | none |
| B (code) | F1 (notices, lint scope, miners_delight.md, design_rules V-14), F2, F5, F7 | Compat, Common (dict), Loot, whichever jars the comment fixes touch |

Queue rows for F5 and F7 (tag dump diff, float tag members, loot tag probe) go in `rows/RFX.qb`; agent B owns that file while
both run, agent A does not touch it.

## History

- 2026-11-17: plan written from the owner's rulings after plan_rules_fixes.md P7.
- 2026-11-17: agent A (queue and docs) done: F3 (7 duplicates merged, `SQ-CNS-` real collision split into `SQ-CSN-`, 30 queue tables standardized), F4 (`SQ-MBX-`, `SQ-RW-00` to `-04`, `SQ-CSN-` recorded in test_prefix_index.md section 2); owner list items 1, 10, 11 closed in plan_rules_fixes.md.
- 2026-11-17: agent B built F1 (V-14 closed as not applicable; Miner's has no NOTICE file and the same lint scope as golden, roll, largemeals and oceanic, so only the docs changed), F2 (history_extract_review.md resolved, comment-only edits in every jar tree, all builds pass), F5 (checked: the loot tags and dict keys were already Creative Loot's; no reader of `creativeworldgen:loot/` left) and F7 (`flower_tags.js` in Compat; installed `creativecompat-1.0.0.jar`). Queue SQ-RFX-30 to -32.
