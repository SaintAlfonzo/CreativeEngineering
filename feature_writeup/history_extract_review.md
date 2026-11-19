# Comment rewrites for owner review (plan_rules_fixes.md section 17)

Written by `tools/history_extract.py --apply` on 2026-11-18. Each row is a dated comment that states a rule: the date was taken out and the rule kept in present tense; its dated line is in the writeup History. Check that the rule is still wanted and reads right.

| File | Line | Was | Now |
|---|---|---|---|
| development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/config/OwnConfigLogic.java | 26 | Mod ids whose config files the watcher owns: the in-tree jars of development/ (the Creative jars, Sol: Raspberry Pie) and the in-tree ports and bundle members that keep their own config file in config/ (assumed list, 2026-11-17). | Mod ids whose config files the watcher owns: the in-tree jars of development/ (the Creative jars, Sol: Raspberry Pie) and the in-tree ports and bundle members that keep their own config file in config/ (assumed list). |
| development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/melting/Melting.java | 486 | The slow tier (packed ice, cryosol) rolls {@code meltChance} here, not the low {@code slowMeltChance}, so it melts rapidly in the Nether (owner, 2026-11-17). | The slow tier (packed ice, cryosol) rolls {@code meltChance} here, not the low {@code slowMeltChance}, so it melts rapidly in the Nether (owner). |
| tools/gen_can_types.py | 219 | Ender's Delight's mollusk and filet (End's Delight's meat and slice are merged onto them, plan_misc_1117.md 2) plus End's shulker meat's Levitation II (4 s) as a 50% chance (owner, 2026-11-18) | Ender's Delight's mollusk and filet (End's Delight's meat and slice are merged onto them, plan_misc_1117.md 2) plus End's shulker meat's Levitation II (4 s) as a 50% chance (owner) |

## Undated history phrases, left in code

Comment lines that read as history but carry no date; not moved (no date to file them under).

