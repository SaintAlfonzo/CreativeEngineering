# creativedelight / swappers briefs

Written 2026-10-25. All Pinned: the owner said swappers come later, with no notes on timing. Plan only.

## swappers #1: Swap list format and reader
`data/creative_delight/swaps/<name>.json`: `{"replaces": {"creative_delight:thing": ["oldmod:thing"]}}`, namespace kept since the old mod may be gone, never pruned. Seed from members' `removed_items.js` entries that have a survivor. Decision: reader exposed from patcherMod to KubeJS, or the lists written as a CD startup script (Q19). Test CDS-01: reader returns every list; malformed file logs and is skipped.

## swappers #2: Script half
CD server script reads the lists and feeds `global.itemMerges` (`server_scripts/other/item_merges.js`) so recipes, loot and tags use the survivor. The pack's merge rows for member items become list entries. Test CDS-02: harness (framework #6) shows recipes using the old id now use the survivor.

## swappers #3: World half and triggers
Inventories, containers, dropped items. KubeJS covers player login and inventory, not unloaded containers: Java in patcherMod reading the same lists. Triggers are config booleans: `onWorldLoad`, `onPlayerDataLoad`, `onContainerOpen`, `onCommand`, `onceWhenUpdated`. Block and entity swaps later. Tests CDS-03 (item in chest swapped on open), CDS-04 (once-when-updated runs once per list change).
