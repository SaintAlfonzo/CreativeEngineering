# ui / names: implementation brief

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# ui`, Sub `names` (row 6 open; rows 1 to 5 Testing).
Feature writeup: [names.md](../names.md) (test prefix `NM-`, last id NM-13).

## Scope and order

One open row, and it is already built: `names #6` only needs an in-game look. Nothing to group or order.

---

## names #6: Chocolate night berries
Status **Planned** (really built, see Now) · Work: KubeJS · id `ui/names #6`

### Goal
Neapolitan's chocolate spider eye is called "Chocolate Night Berries", matching Canned Goods' spider-eye can, which the
pack already calls "Canned Night Berries".

### Now
- [namechanges.js](../../kubejs/client_scripts/namechanges.js) line 66:
  `e.renameItem('neapolitan:chocolate_spider_eye', 'Chocolate Night Berries')`, next to line 65
  `e.renameItem('canned_goods:canned_spider_eyes', 'Canned Night Berries')`. Both were added together for pack 1.0.3
  ([plan_1.0.3.md](../plan_1.0.3.md) NM: "Chocolate Spider Eye to Chocolate Night Berries"), git 5c06f29 (2026-10-02).
- `renameItem` writes the key under the item's own namespace (verified, KubeJS 1902.6.2-build.73 `LangEventJS.renameItem`:
  `add(item.kjs$getMod(), item.getDescriptionId(), name)`), so it is `neapolitan` / `item.neapolitan.chocolate_spider_eye`,
  which beats Neapolitan's own "Chocolate Spider Eye" (Neapolitan 4.1.0 en_us). This is the same placement the names
  audit uses on purpose (names.md 11: same namespace + the KubeJS pack wins). No resource-pack zip sets a `neapolitan`
  key (the only enabled zip with lang is `Copycat Framed Block.zip`, framedblocks only).
- The 2026-10-18 `/dumpfood` export already shows the new name: [food_catalog.md](../food_catalog.md) 399 "Chocolate
  Night Berries `neapolitan:chocolate_spider_eye`", and effects.md 2.1 lists it under Poison and Sugar Rush by that name.
  The name of the item in game is therefore already right.
- No other text uses the old name: no `chocolate spider` / `chocolate_spider` in config/, defaultconfigs/,
  configs_edited/ or kubejs/assets (FTB Quests included).
- item_dict: `i.chocolate_spider_eye = 'neapolitan:chocolate_spider_eye'` (12409).

### Build
Nothing to build. Steps to close it:
1. In game, check NM-14 below. If it passes, the row is done (the TODO owner sets it Complete or drops it).
2. If the user saw "Chocolate Spider Eye" somewhere specific, it is a place that doesn't use the item name (a hard-coded
   string, a quest description, a book page). Ask where, then fix that text at its source.
3. Optional, for consistency: move lines 65 and 66 into
   [name_rules.json](../../kubejs/generation_scripts/names/name_rules.json) `names` instead. Not needed: namechanges.js
   is where hand-written renames live (names.md 0).

### Ids and data
`neapolitan:chocolate_spider_eye` (Neapolitan 4.1.0, verified in the jar: lang key `item.neapolitan.chocolate_spider_eye`
= "Chocolate Spider Eye"). Can name for a future chocolate night berries can: can_parity.md 892 proposes "Canned
Chocolate Night Berries" (`/`, no can yet).

### Decisions
None.

### Gotchas
- JEI search "spider" no longer finds it; search "night berries" or `@neapolitan`.

### Tests (NM-)
- NM-14: JEI and inventory hover on `neapolitan:chocolate_spider_eye` -> "Chocolate Night Berries"; Canned Goods' spider
  eye can -> "Canned Night Berries".

---

## names #8: Rename Roll of Ender Guardian
Status **Planned** / Work lang / TODO `ui/names #8`

### Goal
The Ocean's Delight roll reads "Elder Guardian Roll" (the row says "Ender Guardian Roll", see Decisions).

### Now
- No "Ender Guardian" item exists. The match is `oceansdelight:elder_guardian_roll`, named "Roll of Elder Guardian" in
  `oceansdelight-1.0.2-1.19.2.jar` (`item.oceansdelight.elder_guardian_roll`). Its siblings: "Slab of Elder Guardian",
  "Slice of Elder Guardian", "Cooked Slice of Elder Guardian".
- Display Delight's placed versions already read "Plated Elder Guardian Roll" / "Small Plated Elder Guardian Roll"
  (`kubejs/assets/displaydelight/lang/en_us.json` 153, 157), so the new name matches them.
- Renames live in [namechanges.js](../../kubejs/client_scripts/namechanges.js) (`e.renameItem('mod:id', 'Name')`, 211 lines).

### Build
1. `e.renameItem('oceansdelight:elder_guardian_roll', 'Elder Guardian Roll')` in namechanges.js. CLAUDE.md prefers `global.item`
   keys: add `i.elder_guardian_roll = 'oceansdelight:elder_guardian_roll'` to item_dict if the file's other renames are
   moved to keys; the existing renames use literal ids, so follow the file.
2. Check the sibling names for the same pattern (a "X of Y" form) and rename only if the user wants it.

### Decisions
"Ender" or "Elder"? The item is the Elder Guardian's. Recommend "Elder Guardian Roll" (the row's "Ender" looks like a slip).

### Tests (NM-)
- NM-15 (proposed): JEI and inventory hover show "Elder Guardian Roll"; WTHIT on the placed plate unchanged.

---

## Built, needs play

| Row | Change | Status | Notes |
|---|---|---|---|
| names #1 | Missing-name fixes | Testing | NM-01 to NM-06 open; nothing stale found |
| names #2 | Raw lang keys in game | Testing | Copper poisoning and the Intimidation arrow are in `name_fixes.json` (lines 257, 501); `vinery:trippy` comes from the port's lang (not re-checked here); WL-02 open |
| names #3 | Death Knell on server and client | Testing | FP-01 in game |
| names #4 | Display-name audit | Testing | NM-12 in game; `name_fixes.json` holds 436 names in 59 namespaces as stated |
| names #5 | Pretty Pipes "Item Pipe", mod display names | Testing | Pack lang overlay has "Item Pipe" / "Item Pipe Wrench" (kubejs/assets/prettypipes); NM-13 |

## Writeup corrections made

- [names.md](../names.md) 4, "Wanted rename": added that the rename has been built since 1.0.3 (namechanges.js 66, under the
  `neapolitan` namespace) and already shows in the 2026-10-18 food export, so only an in-game look is left.
