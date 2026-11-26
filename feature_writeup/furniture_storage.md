# Furniture storage

Storage added to furniture that ships without it. The kitchen corner (Cooking for Blockheads) is in
[kitchen.md](kitchen.md); this doc has the rest.

## 0. At a glance

<!-- generated:status_index begin open_work sha1=ba133888da4a -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 0 open TODO rows (none), 0 Complete.
<!-- generated:status_index end open_work -->

## 1. Handcrafted nightstands (2026-10-31, Creative Common 1.1.04)

User (TODO scratchpad "nightstands (handcrafted) have storage"): a chest's worth of storage.

Handcrafted 2.0.6's nightstand held one item: a sheet (`#handcrafted:sheets`) laid on top, placed with a right click
and taken off with sneak and an empty hand, saved as `Item`. Every Compat's nightstands (other woods) extend
Handcrafted's block and block entity, so the same mixins cover them.

| Piece | File | What |
| --- | --- | --- |
| Storage | `compat/NightstandStorage.java` | 27-slot container, three-row chest menu titled with the block name, item handler capability (hoppers, pipes) |
| Block entity | `mixin/HandcraftedNightstandStorageMixin` | gives `NightstandBlockEntity` the container |
| Save | `mixin/HandcraftedItemHoldingSaveMixin` | saves it as `CENightstandItems` (chest-style `Slot` list) at the tail of `ItemHoldingBlockEntity` save/load; the sheet's `Item` key is untouched |
| Block | `mixin/HandcraftedNightstandUseMixin` | opens the chest when Handcrafted's own use answers PASS; drops the contents when broken (added `onRemove`) |

Why a mixin save and not a capability: `ItemHoldingBlockEntity.load` does not call BlockEntity's, so `ForgeCaps` would
be written but never read back.

Clicks: a sheet in hand still lays the sheet; sneak with an empty hand still takes it off; a block item on the top face
still places the block (lamps, candles); anything else opens the chest. Old worlds: nothing was stored before, so
nothing is lost; the sheet stays.

Not done: comparator output.

### Tests

| ID | Check |
| --- | --- |
| NS-01 | Right-click an oak nightstand with an empty hand: 27-slot chest named after the block; items stay after closing, relog and server restart |
| NS-02 | Same on an Every Compat nightstand (any other wood) |
| NS-03 | Sheet in hand lays the sheet; sneak with an empty hand takes it off; a lantern on the top face places; storage unaffected |
| NS-04 | Break a filled nightstand: every item drops; an old nightstand with a sheet keeps the sheet after the update |
| NS-05 | Hopper above fills it, hopper below empties it |

Scratch run r1126q (2026-11-26, [scratch_run_2026-11-26.md](testing/scratch_run_2026-11-26.md) 7.2): SQ-NS-01 FAIL, known (nightstand and hoppers stay empty, as r1123cx; TODO decor-and-framed-blocks #3).
