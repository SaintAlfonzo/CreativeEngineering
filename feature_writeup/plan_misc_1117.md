# Miscellaneous rulings, not executed (2026-11-17)

<!-- generated:status_index begin open_work sha1=5f14b125c153 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Testing 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `dishes/ingredients #32` | Testing | S+ P | Shulker Mollusk is the shulker meat (End's meat merged onto it) |
| `ingredients #44` | Testing | S | Foxes and bees: leaf piles flyable not pathable; sand/ash/snow layers blocked |
<!-- generated:status_index end open_work -->

Status: **built 2026-11-18, waiting on reruns** (notes in the History). Owner rulings from the 2026-11-17 open-decision review of
[plan_post_scratch_1115.md](plan_post_scratch_1115.md), [plan_post_scratch_1115b.md](plan_post_scratch_1115b.md),
[plan_passthrough.md](plan_passthrough.md) and [plan_meals_rolls_bbq.md](plan_meals_rolls_bbq.md). Append further
unexecuted small items here. Status lives in the TODO rows.

## 1. Foxes and bees in layered blocks (closes the open question in 1115b and passthrough section 10)

Owner (2026-11-17):
- **Leaf piles:** foxes and bees can fly or move through them, but do not pathfind through them (mob pathing treats a
  leaf pile cell as blocked for them).
- **Sand, ash and snow layers:** foxes and bees cannot pass through them (no exemption) and do not pathfind through them.
- So the per-tier exemption narrows from "every layered block" to **leaf piles only, movement only**; the path node
  evaluation avoids layer cells for every mob in every layer tier (this matches the existing rule that other mobs do
  not path through leaf piles, G6 item 21).
- Touches: Creative Compat passthrough tier exempt tags (`layers` tier, per-tier tags in `blocktags.js`), and the
  pathing check for the layer tiers. Update plan_passthrough.md section 10 and the 1115b "Open" list to "settled" when built.
- Player changelog: one bullet under the passthrough family when built (foxes and bees no longer pass sand, ash or
  snow layers).

## 2. Shulker Mollusk is the preferred shulker meat (supersedes `dishes/ingredients #32`)

Owner (2026-11-17): Ender's Delight's Shulker Mollusk is the preferred shulker meat. Undo the 2026-11-02 merge that
preferred End's Delight's shulker meat.
- State today (item_merges.md 2.14): the 2026-11-04 partial undo left only the End Skewer on the mollusk; the stuffed
  shulker (pot and heatless) and Canned Shulker Flesh still use `ends_delight:shulker_meat` and its slice.
- Direction: the mollusk is the canonical item for every recipe (stuffed shulker, canned shulker flesh, the End
  Skewer chain from plan_meals_rolls_bbq.md C3). End's shulker meat and slice become the merged-away pair, mapped
  onto the mollusk and filet (reverse of the 2026-11-02 direction). Open for the build to check, then report, not ask:
  the stack-size and food values to keep (take the mollusk's, unless the food tuning tables say otherwise), and that
  Ender's knife drop and filet cut stay on.
- Stacks in worlds: the swap list must carry saves (End's meat items become mollusks), as with the other merges.
- Touches: CD `endersdelight/item_merges.js`, `ends_delight/recipes.js`, `endersdelight/recipes.js`, can type rows,
  `item_dict.js` keys, `food_types.js`, tags. Update item_merges.md 2.14 and ends_delight.md 5.8; changelog bullet.

## History

- 2026-11-17: written from the owner's answers.
- 2026-11-18: section 1 built (Creative Compat). Sand and ash layers were never in a walk-through tier, so only snow
  changed for foxes and bees. They moved from `passthrough_exempt/layers` to `passthrough_exempt/leaf_layers` (leaf
  piles and Twigs bamboo leaves), so snow slows them. Rabbits and cats keep the tier-wide exemption. A deep leaf pile
  (`pathBlockLayers`) is `BLOCKED` in their path search (`LeafThorns.pathType` through `SnowPathMixin`), because foxes
  take `DAMAGE_OTHER` at no cost. Other mobs keep `DAMAGE_OTHER`. SQ-PTH-21 (updated), -29, -30.
- 2026-11-18: section 2 built (Creative Delight). Report on the checks the build was to make:
  - Values kept: the mollusk's own stack size and food values (the food tuning tables have no shulker entry).
  - Canned Shulker Flesh follows the mollusk's food: 8 a can [1], its Nausea chance [End's Levitation].
  - Ender's knife drop and filet cut stay on. End's knife drop is off; its machete drop gives the mollusk.
  - End's cut is removed. End's roasted shulker meat now cooks from the mollusk.
  - SQ-BBQ2-08, SQ-FCB-05 (reversed).

