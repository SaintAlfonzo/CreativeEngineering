# creative-pick-block-desync

> Creative middle-click with a full hotbar deletes the held item server-side (vanilla 1.19.2 desync); client keeps a ghost copy; fix = client mixin syncing displaced slots

Moved from agent memory 2026-11-16 (last edited there 2026-10-20). Repo status docs win over any status line here.

Diagnosed 2026-10-19 (TODO gameplay row "Item deleted after middle-click pick"). Vanilla 1.19.2 `Minecraft.pickBlock` creative branch calls `Inventory.setPickedItem` client-side, which moves the old hotbar item into a free main-inventory slot (or swaps with slot i when the picked item is already in main inventory), but only sends `ServerboundSetCreativeModeSlotPacket` for `36 + selected`. The server overwrites the hotbar item, so the old item is gone server-side at pick time; the client shows a ghost that disappears on any server-authoritative move/resync (container open, Quark sort/hotbar swap). Opening the creative E screen probably re-sends it (addSlotListener -> broadcastChanges -> CreativeInventoryListener) - untested. Not caused by mods: Quark (long-range pick), AE2 (color applicator), SB (skips creative) pickBlock hooks checked.

**How to apply:** the fix is a Creative Common client mixin around `setPickedItem` in `Minecraft.pickBlock`: snapshot `inventory.items`, then `handleCreativeModeItemAdd` every changed slot (hotbar i -> 36+i, main 9-35 same). Built 2026-10-19 as `mixin/CreativePickBlockSyncMixin` (client list) in the 1.0.9 jar, copied to mods/. Known vanilla bug MC-197793 (dup MC-174436). Related: [item-loss-repro](item-loss-repro.md), [quark-sort-item-loss](quark-sort-item-loss.md).
