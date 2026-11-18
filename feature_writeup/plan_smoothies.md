# Smoothies and brain freeze (2026-11-17)

<!-- generated:status_index begin open_work sha1=4e57d17a063e -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Planned 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `sweets #8` | Planned | S P | Smoothie line: 16 fruit smoothies (2 + 1 fruit or slices + bottle), always edible, honey drink sound |
| `sweets #9` | Planned | S P | Brain freeze: repeated smoothies (and cold treats) can freeze the player for 10 s with a blue action-bar message |
| `sweets #10` | Planned | C | Smoothie sprites: set up a mass swap from the strawberry banana placeholder to per-flavor sprites |
<!-- generated:status_index end open_work -->

Status: **planned, not built.** The parity expansion left open by `baking/sweets #5` (the Neapolitan Strawberry
Banana Smoothie was removed 2026-10-29; brief: [briefs/baking_sweets.md](briefs/baking_sweets.md) "sweets #5").
Status lives in the TODO rows.

## 1. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | "add mixed berry (red berry + blue berry + purple berry, identify color candidates)" | open | Search widened to every item named "berry", tree fruit included ([fruit.md](fruit.md) section 4). Recipe: 2 red berries + 1 blue or purple berry + bottle. Effects come from the berries used, stored on the item: each berry gives its effect at half base length ([smoothies.md](smoothies.md) 2.1) | 2026-11-18 | AskUserQuestion 2026-11-17, chat 2026-11-18, AskUserQuestion 2026-11-18 |
| 2 | "the first fruit in the name is 2 input, the second is 1 in the recipe" | clear | Shapeless: 2 x first fruit + 1 x second fruit + glass bottle (the removed Neapolitan recipe's shape: 2 strawberry, 1 banana, bottle). One-fruit names: 3 x that fruit | 2026-11-17 | chat 2026-11-17 |
| 3 | "pear (... gives a buff that allows eating at any hunger)" | premise | That effect already exists: Fruits Delight Appetizing ("eat and drink even when you are full", effects.md line 175), now from hawberry foods. Ruling: pear smoothie = 2 pear + 1 hawberry; Appetizing 5 min, Lozenge 30 s | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 4 | "all smoothies should be drinkable with the honey texture at any hunger" | open | "Honey" means the honey bottle drinking sound. Every smoothie is always edible and plays the honey drink sound | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 5 | "drinking too many smoothies increases the chance of getting the powdered snow overlay and taking frost damage" | open | Every cold food after the first within 1 min rolls: 20%, +20% per further roll in the window. Freeze length by the number of rolls up to and including the hit: 5, 8, 11, 14, 20 s (20 s from the 5th roll on); lengths configurable. History: 10 s flat (2026-11-17) -> 5/8/11/14/20 (2026-11-18) | 2026-11-18 | AskUserQuestion 2026-11-17, chat 2026-11-18 |
| 6 | "brain freeze, give blue text message on in world text line above hotbar, where other features hook in" | premise | The line is the vanilla overlay (action bar) message, also used by the Creative Calendar held line (time.md 3.7) and the jukebox "Now playing" line (raspberry_pie.md). There is no shared Creative Common hook; send it with `displayClientMessage(text, true)`, colored blue | 2026-11-17 | agent check |
| 7 | "search other food families to cause brain freeze" / "add the proposals to cold food. add ice cubes if they are edible" | clear | All proposals count ([smoothies.md](smoothies.md) section 3). Ice Cubes are edible (0 hunger, always edible; food_catalog.md line 1569) and count | 2026-11-18 | chat 2026-11-18 |
| 8 | "investigate fruits delight input count to effect length ratios before deciding effect lengths" | open | Findings: [fruit.md](fruit.md) 3.1. Ruling: first fruit (x2) = its Fruits Delight base length (jam/jello), second fruit (x1) = half base (juice level); amplifiers as Fruits Delight has them (Strength III stays III). Owner values (pear) win. History: agent default 60 s / 30 s, amp I (2026-11-17) replaced | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 9 | "only slices should be used of fruits" | clear | A fruit with a slice item goes in as slices, same counts (2 + 1): lemon, orange, pineapple, melon, cantaloupe. The others have no slice and go in whole | 2026-11-18 | chat 2026-11-18, AskUserQuestion 2026-11-18 |
| 10 | "use the strawberry banana texture for now, mark a mass swap setup later" | clear | Every smoothie uses Neapolitan's `strawberry_banana_smoothie.png` as its sprite (copy of upstream art; sources.csv row at build). A later row sets up the mass swap to per-flavor sprites | 2026-11-18 | chat 2026-11-18 |
| 11 | Mango's Rage Aura (idle mobs within 24 blocks target you) in Orange Mango, Peach Mango, Mango Pineapple / "keep rage aura (mark a note to move this to a different food, maybe ghost peppers?)" | clear | Kept. Note to move Rage Aura to another food later (ghost pepper candidate), TODO `fruit` row. History: dropped (2026-11-17 draft) -> kept, agent default -> confirmed | 2026-11-18 | chat 2026-11-18 |
| 12 | Mixed berry: the same berry twice / "effects should add from 2 fruit used" | clear | The two red berries may differ; a berry used twice adds up to the full base (2 x half) | 2026-11-18 | chat 2026-11-18 |
| 13 | Brain freeze: does a hit reset the count / "brain freeze resets the counter" | clear | A hit resets the count | 2026-11-18 | chat 2026-11-18 |

Hunger and saturation follow the `drink` family rule (food_families.md) through the food tuning pass; not set here.

## 2. Findings (moved 2026-11-18)

- Smoothie table, Mixed Berry effects, brain freeze: [smoothies.md](smoothies.md) sections 2 and 3.
- Fruit -> effect, berry colors, slices, Fruits Delight length ratios: [fruit.md](fruit.md) sections 2 to 4.

## 6. Build notes

- Items: CD startup, the shared sweets table shape from the brief (`global.sweets`-style entry per smoothie: key, name,
  fruits, effects); ids in `item_dict.js` (Creative Common rebuild). Neapolitan's `strawberry_banana_smoothie` stays
  removed; new ids are Creative Delight's (ids follow the owning mod).
- Mixed Berry: one item; the crafting result stores its effects on the item (NBT) from the berries used, and its eaten
  callback and tooltip read them. Red, blue and purple berry tags (`#creativedelight:berries/red` ...) through
  `global.tags`.
- Missing fruit keys: `i.t_fruits_cherry` has no members in `forge:fruits/cherry`; add `vinery:cherry` to it (tag helper).
- Sprites: every smoothie points its model at Neapolitan's `strawberry_banana_smoothie.png` (copied upstream art; one
  `feature_writeup/texture_sources/sources.csv` row per use, then `tools/texture_provenance.py --write`). The swap to
  per-flavor sprites is its own row (`sweets` family), set up so one table maps each smoothie to its sprite and the
  swap is one run.
- Fluids, jars, can parity, stack size (drink group), compost, food family `drink` row: per the brief's checklist.
- Changelog: one family `## Smoothies` when built.
