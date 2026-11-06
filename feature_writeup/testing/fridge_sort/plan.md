# Fridge sort item loss (FS-)

Report: pressing Quark's sort button in a 2-tall Cooking for Blockheads fridge deleted items.

## Cause

Not the 2-tall fridge itself. CFB joins the two fridges with Balm's `CombinedContainer` (top 0-26,
bottom 27-53), and that slot mapping is correct. The loss is in Quark's `SortingHandler`
(Quark 3.4-418), and it happens in any Quark-sorted inventory:

1. Quark copies every stack, merges and sorts them, then extracts every slot and re-inserts the
   sorted list through Forge `InvWrapper.insertItem`.
2. `insertItem` takes at most `min(item max stack size, slot limit)` per slot. A slot holding more
   than the item's max stack size (a 64-stack of soup in a fridge from before the 1.0.5 stack-size
   policy cut bowl meals and drinks to 32, golden foods to 16) only half inserts, and the sort
   returns FAIL.
3. On FAIL, Quark "restores" the original layout through the same insert path. It fails again on
   the same oversized stack, the result is ignored, and the loop stops. The overflow is lost, and
   so is every stack in the slots after it.

A 2-tall fridge has 54 slots, so more stacks come after the failure and more items are lost. It is
also where old food stacks are kept, which is why the report came from a fridge.

Headless check (Forge InvWrapper + Balm CombinedContainer 27+27, Quark's original code): stock sort
turned 5 stew (max 1) + 7 apples into 1 stew, with the apples gone. The patched sort kept everything.

## Fix (patcherMod 1.0.7, built 2026-10-14; DEPLOYED with pack 1.0.7 on 2026-10-18)

`mixin/QuarkSortingHandlerMixin` replaces `SortingHandler.sortInventory(IItemHandler, int, int, int[])`.
Every Quark sort goes through that method: player inventory, chests, fridges and backpacks. It now
calls `compat/QuarkSafeSort`, which:

- splits merged stacks to the item's max stack size, so a sort also repairs oversized stacks
- checks that every slot can be fully extracted, that the stacks fit in the unlocked slots, and that
  each stack fits its slot's limit and `isItemValid`. If any check fails it changes nothing.
- puts back into free slots anything a handler still refuses, and logs `[quark-sort]` for anything
  it cannot put back

Server side only. The client does not need the new jar.

## Test (on the structure scratch server, at the next reboot)

The Phase 7 server runs `build/libs/creativeengineering-1.0.6.jar`. The 1.0.7 jar is identical
apart from the two new classes, the mixin list entry and the version string, so swapping it in
does not affect the structure numbers.

Setup, at a reboot the structure tests already need (switch tests reboot once each):

1. Stop the server. In `srv/mods/`, point the creativeengineering link at
   `development/creativeCommon/build/libs/creativecommon-1.0.7.jar` and remove the 1.0.6 link
   (two jars with the same modid will not boot).
2. Copy `feature_writeup/testing/fridge_sort/zz_scratch_fridgesort.js` into `srv/kubejs/server_scripts/`.
   Do not use `/reload`, because it drops Chunky's commands. The reboot loads the script.
3. Boot, then check `logs/latest.log` for mixin errors that mention `QuarkSortingHandlerMixin`.

| Test | Steps | Pass |
|---|---|---|
| FS-01 | Console: `zfridgesort run` (builds at world spawn +2/+2, spawn chunks always loaded) | `PASS`, stew 64, oversized=0 (stew is now 32 + 32), bread/apple/carrot/beef counts unchanged |
| FS-02 | Console: `zfridgesort check` again after a structure batch or chunk save | still `PASS` (the sorted layout was saved) |
| FS-03 | Log: `grep -n "quark-sort" logs/latest.log` | no lines |
| FS-04 (optional, needs a client) | Join `localhost:25599`, `op <name>`, stand near spawn, `/zfridgesort fill`, open either half of the fridge, press Quark's sort button, then `/zfridgesort check` | `PASS`; both halves show stew 32 + 32 and the apples, carrots and beef are still there |
| FS-05 (optional, client) | Press sort twice more in the same fridge, then sort your own inventory (Quark's player-side button) while holding a few stacks | repeat sorts change nothing; `/zfridgesort check` still `PASS`; no items lost from the player inventory |

A FAIL in FS-01 that shows stew 32 and missing apples/carrots means the mixin did not apply (old
jar still linked, or the mixin was rejected at boot).

Afterwards: remove `zz_scratch_fridgesort.js` from `srv/` if the server keeps running for structure work.
The fridge at spawn does not matter in a scratch world.

## Results

| Test | Result | Evidence |
|---|---|---|
| FS-01 | | |
| FS-02 | | |
| FS-03 | | |
| FS-04 | | |
| FS-05 | | |
