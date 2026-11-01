# creativedelight / swappers briefs

Written 2026-10-25, checked 2026-10-31 against [TODO](../todo/TODO.md) `# creativedelight`, Sub `swappers`. All Pinned: the
owner said swappers come later, with no notes on timing. Plan only. Feature writeup:
[creative_delight.md](../creative_delight.md) 11 (design), 8.1 (`swappers.*` keys), 17 Q19; test prefix `CDS-`.

## Scope and order

| Row | Change | Status | Order |
|---|---|---|---|
| 1 | Swap list format and reader | Pinned | first: the reader location decides how row 2 feeds the merges |
| 2 | Script half (recipes, loot, tags) | Pinned | after row 1 |
| 3 | World half and triggers | Pinned | after row 2; Java |

**Changed since 2026-10-25 (checked 2026-10-31):** item merges are split by group. `kubejs/startup_scripts/_lib/item_merges.js`
defines `global.addItemMerges(group, pairs)` (groups `pack`, `creative_delight`; a purged id may belong to one group only)
and `global.itemMergeRun.recipes / loot / inventory`. The CD group's pairs come from CD startup scripts
(`kubejs_startup/<mod>/item_merges.js`, e.g. moredelight), and its server half is
[_core/item_merges.js](../../kubejs/data/creative_delight/kubejs/_core/item_merges.js) (recipes, LootJS, and
`PlayerEvents.inventoryChanged`). The pack's merge rows for CD foods already moved there (2026-10-27).

## swappers #1: Swap list format and reader
`data/creative_delight/swaps/<name>.json`: `{"replaces": {"creative_delight:thing": ["oldmod:thing"]}}`, namespace kept
since the old mod may be gone, never pruned. Seed from the CD merge pairs (`global.itemMergeGroups.creative_delight`)
and members' `removed_items` entries that have a survivor. `addItemMerges` runs at **startup**, so the reader must deliver
the lists before CD's startup merge scripts: a Java reader in patcherMod (its jar startup loader already reads
CD's `kubejs_startup/`) exposed as a global, or the lists written as a CD startup script. Decision (Q19): recommend the CD startup script form (no Java,
same place as today's pairs), with the JSON generated from it for row 3's Java side. Test CDS-01: reader returns every
list; malformed file logs and is skipped.

## swappers #2: Script half
Feed the lists into `global.addItemMerges('creative_delight', ...)`; the existing `_core/item_merges.js` run then swaps
recipes, loot and tags (no new server script). The CD `<mod>/item_merges.js` startup pairs become list entries.
Test CDS-02: harness (framework #6) shows recipes using the old id now use the survivor.

## swappers #3: World half and triggers
Player inventories are already swapped on `PlayerEvents.inventoryChanged` (both groups). Left: containers, dropped
items, unloaded chunks: Java (CD jar or patcherMod) reading the same lists. Triggers are config booleans:
`onWorldLoad`, `onPlayerDataLoad`, `onContainerOpen`, `onCommand`, `onceWhenUpdated`. Block and entity swaps later.
Tests CDS-03 (item in chest swapped on open), CDS-04 (once-when-updated runs once per list change).
