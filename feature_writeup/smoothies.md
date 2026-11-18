# Smoothies

Fruit smoothies and brain freeze. Status: planned, not built; rulings and build order in
[plan_smoothies.md](plan_smoothies.md), fruit facts in [fruit.md](fruit.md). Replaces Neapolitan's Strawberry Banana
Smoothie (removed 2026-10-29, `baking/sweets #5`).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=4e57d17a063e -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 3 open TODO rows (Planned 3), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `sweets #8` | Planned | S P | Smoothie line: 16 fruit smoothies (2 + 1 fruit or slices + bottle), always edible, honey drink sound |
| `sweets #9` | Planned | S P | Brain freeze: repeated smoothies (and cold treats) can freeze the player for 10 s with a blue action-bar message |
| `sweets #10` | Planned | C | Smoothie sprites: set up a mass swap from the strawberry banana placeholder to per-flavor sprites |
<!-- generated:status_index end open_work -->

- 16 smoothies (section 2): 2 x first fruit + 1 x second fruit + glass bottle, shapeless.
- Effects: first fruit at its Fruits Delight base length, second at half, amplifiers kept.
- Brain freeze (section 3): cold foods eaten close together can freeze the player for 5 to 20 s.

## 1. Overview

- Every smoothie is always edible, plays the honey bottle drinking sound and returns the glass bottle.
- A fruit with a slice item goes in as slices (lemon, orange, pineapple, melon, cantaloupe); others whole.
- One-fruit smoothies (Glowberry, Chorus) take 3 of the fruit.
- Sprite: Neapolitan's strawberry banana smoothie for every flavor until the mass swap (`sweets #10`).
- Hunger and saturation: the `drink` family rule ([food_families.md](food_families.md)), set in the food tuning pass.

## 2. Smoothies

Recipe = shapeless in the crafting grid, plus a glass bottle. Base lengths are the fruit's Fruits Delight jam (`_jelly`
bottle) and jello lengths ([fruit.md](fruit.md) 3.1); first fruit = base, second fruit = half base (owner 2026-11-18).

| # | Smoothie | 2 x | 1 x | First fruit (base) | Second fruit (half) |
|---|---|---|---|---|---|
| 1 | Mixed Berry | 2 red berries | 1 blue or purple berry | per berry, section 2.1 | |
| 2 | Cran Apple | Cranberry | Apple | Shrinking 80 s | Absorption 10 s |
| 3 | Orange Mango | Orange Slice | Mango | Regeneration 20 s + Recovering 20 s | Strength III 10 s + Rage Aura 10 s |
| 4 | Kiwi Strawberry | Kiwi | Strawberries | Cycling 4 s | none |
| 5 | Glowberry | Glow Berries x3 | - | Glowing 80 s | |
| 6 | Chorus | Chorus Fruit x3 | - | Teleport (random, like eating chorus fruit) | |
| 7 | Peach Mango | Peach | Mango | Heal Aura 10 s | Strength III 10 s + Rage Aura 10 s |
| 8 | Mango Pineapple | Mango | Pineapple Slice | Strength III 20 s + Rage Aura 20 s | Speed 10 s + Sweetening 10 s |
| 9 | Melon | Melon Slice | Cantaloupe Slice | Regeneration 40 s | Health Boost 10 s |
| 10 | Cherry Lemon | Cherry | Lemon Slice | none | Haste 10 s + Refreshing 10 s |
| 11 | Blueberry Lemon | Blueberry | Lemon Slice | Night Vision 20 s + Brightening 20 s | Haste 10 s + Refreshing 10 s |
| 12 | Pear | Pear | Hawberry | Appetizing 5 min + Lozenge 30 s (owner) | |
| 13 | Cherry | Cherry | Lychee | none | Strength III 5 s |
| 14 | Raspberry Peach | Raspberry | Peach | none | Heal Aura 5 s |
| 15 | Orange Peach | Orange Slice | Peach | Regeneration 20 s + Recovering 20 s | Heal Aura 5 s |
| 16 | Hawberry | Hawberry | White Strawberries | Appetizing 20 s | none |

Every smoothie: always edible, honey drink sound, returns the glass bottle, counts toward brain freeze, sprite =
the strawberry banana smoothie placeholder (owner 2026-11-18; per-flavor sprites: TODO `sweets #10`). Cherry Lemon, Cherry and Raspberry Peach get nothing from their
first fruit (cherry and raspberry have no effect in the pack).

### 2.1 Mixed Berry effects

2 red + 1 blue or purple, any berries of the slot's color ([fruit.md](fruit.md) section 4). Each berry adds its effect at half base; a berry
used twice adds up to the full base (owner 2026-11-18). The item stores the result and shows it in its tooltip, so different
mixes do not stack together.

| Slot | Berry | Effect per berry (half base) | Used twice |
|---|---|---|---|
| red | Cranberry | Shrinking 40 s | Shrinking 80 s |
| red | Hawberry | Appetizing 10 s | Appetizing 20 s |
| red | Raspberry, Strawberries, Sweet Berries, Cherry | none | none |
| blue | Blueberry | Night Vision 10 s + Brightening 10 s | - |
| purple | Bayberry | Leaf Piercing 10 s | - |
| purple | Mulberry | none | - |

Every possible effect set (red pair x third berry): any of {none, Shrinking 40 s, Shrinking 80 s, Appetizing 10 s,
Appetizing 20 s, Shrinking 40 s + Appetizing 10 s} from the red pair, plus one of {Night Vision 10 s + Brightening 10 s,
Leaf Piercing 10 s, none} from the third berry: 18 effect sets, from no effect at all (e.g. 2 raspberry + mulberry) to
Shrinking 40 s + Appetizing 10 s + Night Vision 10 s + Brightening 10 s (cranberry + hawberry + blueberry).

## 3. Brain freeze

Owner (2026-11-17, 2026-11-18):
- Every cold food (tag below) eaten while another was eaten in the last minute rolls once: the 1st roll 20%, +20% per
  further roll in the window (20, 40, 60, 80, 100%).
- On a hit: the powdered snow overlay and freeze damage (set the player's frozen ticks so the vanilla overlay draws and
  the vanilla freeze damage applies), and a blue action-bar message ("Brain freeze!").
- Length by the number of rolls up to and including the hit: 1 -> 5 s, 2 -> 8 s, 3 -> 11 s, 4 -> 14 s, 5 or more ->
  20 s. Configurable: the list of lengths, the chance step and the window go in Creative Delight's common config
  (`[brainFreeze]`; backup in `configs_edited/` when hand-edited).
- A hit resets the count (owner 2026-11-18). Fire Resistance does not block it (vanilla freeze damage ignores it).

Home: Creative Delight Java (food code goes in CD; creative-delight-routing). A `#creativedelight:brain_freeze` item tag
(through `global.tags`) lists what counts, so families join by tag, not code.

What counts (owner 2026-11-18: all proposals plus ice cubes, which are edible):

| Family | Items |
|---|---|
| Smoothies | all 16 (section 2) |
| Ice cream `frozen/ice_cream` | 11 (Neapolitan 7, Delightful 2, Fungi Delight truffle, ...) |
| Popsicle `frozen/popsicle` | 8 (melon, cantaloupe, kiwi, chocolate, chorus, corn, ...) |
| Shaved ice `frozen/shaved_ice` | 1 (Cantaloupe Shaved Ice) |
| Milkshakes (`drink/milk`, cold ones only; hot chocolate excluded) | 10 (Neapolitan 6, Delightful 2, Fruits Delight mango, ...) |
| Iced Berry Matcha Latte | `delightful:berry_matcha_latte` |
| Ice Cubes | `neapolitan:ice_cubes` (0 hunger, always edible) |

Not counted: Frozen Flesh (`hostiledelight:frozen_flesh`, a mob drop) and Ice Sickle (`immersive_weathering:ice_sickle`).
The exact item list is built from the food family tags at build time, so new frozen foods join automatically.

## 4. Limits and decisions

- Rage Aura stays on the three mango smoothies, as on every Fruits Delight mango food (owner 2026-11-18); it is marked to
  move to another food later (ghost pepper candidate, [fruit.md](fruit.md) section 5).
- Pear smoothie is the "eat at any hunger" drink: Appetizing 5 min (Fruits Delight's own foods give 10 to 20 s).
- Rejected: capping Strength III to I and fixed 60 s / 30 s lengths (2026-11-17 draft), replaced by the Fruits Delight
  ratios. Rejected: a fixed Mixed Berry effect set.
- All rulings with dates: [plan_smoothies.md](plan_smoothies.md) section 1.

## 5. File map

Planned (plan section 6): Creative Delight startup table and items, CD server recipes through `global.NCH`, the
`#creativedelight:brain_freeze` and berry color tags through `global.tags`, brain freeze in Creative Delight Java with a
`[brainFreeze]` common config block.

## 6. Offline checks

None yet.

## 7. Tests

None yet; a prefix is taken from [testing/test_prefix_index.md](testing/test_prefix_index.md) when the build adds them.

## 8. History

- 2026-11-18: written from plan_smoothies.md (smoothie table, Mixed Berry, brain freeze); Rage Aura kept, berries used
  twice add up, a brain freeze resets the count (owner).
