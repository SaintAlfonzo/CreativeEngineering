# quark-sort-item-loss

> Quark sort deleted items (reported on a 2-tall CFB fridge) = oversized stacks + Quark's broken restore path; QuarkSafeSort mixin in Creative Common 1.0.7

Moved from agent memory 2026-11-16 (last edited there 2026-10-14). Repo status docs win over any status line here.

2026-10-14: the report was that Quark's sort button deleted items in a 2-tall Cooking for Blockheads fridge. The cause is not the 2-tall fridge: Balm's CombinedContainer slot mapping is correct. Quark 3.4 `SortingHandler.sortInventory(IItemHandler,int,int,int[])` empties every slot and re-inserts the stacks through Forge InvWrapper, which caps each slot at the item's max stack size. A stack over that max (old 64-stacks of food from before the 1.0.5 stack-size policy, see [stack-size-policy](stack-size-policy.md)) fails. Quark's restore then fails on the same stack, the result is ignored, and the overflow plus every later slot is lost. This affects every Quark sort, not only fridges.

Fix: `mixin/QuarkSortingHandlerMixin` + `compat/QuarkSafeSort` (split to max stack size, validate everything first, no-op if it won't fit). Built into Creative Common 1.0.7 build/libs; the jar differs from 1.0.6 only by this fix. Headless harness proved stock loses and patched keeps items. Not in mods/.

**How to apply:** the test plan and scratch command are in `feature_writeup/testing/fridge_sort/` (plan.md FS-01..05, zz_scratch_fridgesort.js). Run them at the next reboot of the structure scratch server (memory `scratch-server-verify`). It ships with the next Creative Common deploy ([deploy-pipeline](deploy-pipeline.md)).
