# no-advancements

> The pack has NO advancements (user, 2026-10-18): patcherMod [advancements] enabled=false drops all 12,940 at load; never rely on advancements for rewards/unlocks

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-18 the user asked to rip out every advancement (vanilla, mods, datapacks). patcherMod `ServerAdvancementManagerMixin` swaps the JSON map for an empty one before the tree is built when CEConfig `[advancements] enabled = false` (default false); `/reload` applies. The Vinery port's translator also leaves out `vintage_perfection` for good.

**Why:** the user's call; the pack also has no vanilla recipe book (so recipe advancements were unused anyway).

**How to apply:** don't design features that grant items, unlock recipes or detect progress through advancements; use FTB Quests, loot or code instead. Rewards lost with them: Vinery banner (crafting recipe since 10-18, alcohol.md 10.5), and with no source yet: Lootr trophy, LC chocolate event coins; partly elsewhere: MCA books, Yuushya book, IE/IP shader bags (alcohol.md 10.2). Related: [backported-vinery](backported-vinery.md), [patchermod-coremod](patchermod-coremod.md).
