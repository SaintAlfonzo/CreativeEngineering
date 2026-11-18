# Smoothies and brain freeze (2026-11-17)

<!-- generated:status_index begin open_work sha1=e16e67d53979 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 2 open TODO rows (Planned 2), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `sweets #8` | Planned | S P | Smoothie line: 16 fruit smoothies (2 + 1 fruit + bottle), always edible, honey drink sound |
| `sweets #9` | Planned | S P | Brain freeze: repeated smoothies (and cold treats) can freeze the player for 10 s with a blue action-bar message |
<!-- generated:status_index end open_work -->

Status: **planned, not built.** The parity expansion left open by `baking/sweets #5` (the Neapolitan Strawberry
Banana Smoothie was removed 2026-10-29; brief: [briefs/baking_sweets.md](briefs/baking_sweets.md) "sweets #5").
Status lives in the TODO rows.

## 1. Owner rulings

| # | Item (owner's words) | Class | Ruling | Date | Source |
|---|---|---|---|---|---|
| 1 | "add mixed berry (red berry + blue berry + purple berry, identify color candidates)" | open | Search widened to every item named "berry", tree fruit included (section 3). Pick: raspberry + blueberry + mulberry, 1 each | 2026-11-17 | AskUserQuestion 2026-11-17 (search); pick: agent default, unconfirmed |
| 2 | "the first fruit in the name is 2 input, the second is 1 in the recipe" | clear | Shapeless: 2 x first fruit + 1 x second fruit + glass bottle (the removed Neapolitan recipe's shape: 2 strawberry, 1 banana, bottle). One-fruit names: 3 x that fruit | 2026-11-17 | chat 2026-11-17 |
| 3 | "pear (... gives a buff that allows eating at any hunger)" | premise | That effect already exists: Fruits Delight Appetizing ("eat and drink even when you are full", effects.md line 175), now from hawberry foods. Ruling: pear smoothie = 2 pear + 1 hawberry; Appetizing 5 min, Lozenge 30 s | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 4 | "all smoothies should be drinkable with the honey texture at any hunger" | open | "Honey" means the honey bottle drinking sound. Every smoothie is always edible and plays the honey drink sound | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 5 | "drinking too many smoothies increases the chance of getting the powdered snow overlay and taking frost damage for 10 seconds" | open | 2nd smoothie within 1 min: 20% chance, +20% per extra one in the window ("for now") | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 6 | "brain freeze, give blue text message on in world text line above hotbar, where other features hook in" | premise | The line is the vanilla overlay (action bar) message, also used by the Creative Calendar held line (time.md 3.7) and the jukebox "Now playing" line (raspberry_pie.md). There is no shared Creative Common hook; send it with `displayClientMessage(text, true)`, colored blue | 2026-11-17 | agent check |
| 7 | "search other food families to cause brain freeze" | open | Candidates in section 5; agent default: smoothies + `frozen` family + milkshakes + Iced Berry Matcha Latte, unconfirmed | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 8 | Smoothie effect strength and duration | open | Agent default, unconfirmed: first fruit's effects 60 s, second fruit's 30 s, amplifier I (Strength III from mango and lychee capped to I); Rage Aura (mango) dropped as hostile; owner values (pear) win | 2026-11-17 | agent default, unconfirmed |
| 9 | Pineapple input | clear | Pineapple slice (the whole pineapple is a 4-slice fruit); other fruits whole; melon and cantaloupe as slices | 2026-11-17 | agent default, unconfirmed |

Hunger and saturation follow the `drink` family rule (food_families.md) through the food tuning pass; not set here.

## 2. Smoothie table

Recipe = shapeless in the crafting grid, plus a glass bottle. Effects named are the fruit's Fruits Delight jello/jam
effect (food_catalog.md `sweet` rows); "none" = that fruit has no effect anywhere in the pack.

| # | Smoothie | 2 x | 1 x | Effects (first fruit; second fruit) |
|---|---|---|---|---|
| 1 | Mixed Berry | 1 raspberry, 1 blueberry, 1 mulberry (row 1) | - | raspberry none; Night Vision + Brightening (blueberry); mulberry none |
| 2 | Cran Apple | Cranberry | Apple | Shrinking; Absorption |
| 3 | Orange Mango | Orange | Mango | Regeneration + Recovering; Strength (Rage Aura dropped) |
| 4 | Kiwi Strawberry | Kiwi | Strawberries | Cycling; none |
| 5 | Glowberry | Glow Berries x3 | - | Glowing |
| 6 | Chorus | Chorus Fruit x3 | - | Teleport (random, like eating chorus fruit) |
| 7 | Peach Mango | Peach | Mango | Heal Aura; Strength (Rage Aura dropped) |
| 8 | Mango Pineapple | Mango | Pineapple Slice | Strength (Rage Aura dropped); Speed + Sweetening |
| 9 | Melon | Melon Slice | Cantaloupe Slice | Regeneration; Health Boost |
| 10 | Cherry Lemon | Cherry | Lemon | none; Haste + Refreshing |
| 11 | Blueberry Lemon | Blueberry | Lemon | Night Vision + Brightening; Haste + Refreshing |
| 12 | Pear | Pear | Hawberry | Appetizing 5 min + Lozenge 30 s (owner) |
| 13 | Cherry | Cherry | Lychee | none; Strength |
| 14 | Raspberry Peach | Raspberry | Peach | none; Heal Aura |
| 15 | Orange Peach | Orange | Peach | Regeneration + Recovering; Heal Aura |
| 16 | Hawberry | Hawberry | White Strawberries | Appetizing (short); none |

Every smoothie: always edible, honey drink sound, returns the glass bottle, counts toward brain freeze. Cherry Lemon,
Cherry and Raspberry Peach get nothing from their first fruit (cherry and raspberry have no effect in the pack); the
fruit table below shows which fruits could take a new effect if wanted.

## 3. Berry color candidates (mixed berry)

Every item whose id or name has "berry", minus foods made from them. Color = average of the most saturated half of the
item sprite's pixels, read from the mod jar (no texture edited).

| Berry | Id | Source | Color | Slot | In another smoothie |
|---|---|---|---|---|---|
| Raspberry | `hearthandharvest:raspberry` | bush | red | **red (pick)** | Raspberry Peach |
| Strawberries | `neapolitan:strawberries` | crop | red `#cb3938` | red | Kiwi Strawberry |
| Sweet Berries | `minecraft:sweet_berries` | bush | red | red | - |
| Cranberry | `fruitsdelight:cranberry` | bush | red-orange `#b1431d` | red | Cran Apple |
| Hawberry | `fruitsdelight:hawberry` | tree | red `#d43043` | red | Hawberry, Pear |
| Cherry | `vinery:cherry` | tree | red | red | Cherry, Cherry Lemon |
| Coffee Berries | `farmersrespite:coffee_berries` | bush | dark red `#7d1527` | not fruit-like (caffeine, Wither) | - |
| Blueberry | `fruitsdelight:blueberry` | bush | blue (only blue berry) | **blue (pick)** | Blueberry Lemon |
| Mulberry | `upgrade_aquatic:mulberry` | vine | purple `#844766` | **purple (pick)** | - |
| Bayberry | `fruitsdelight:bayberry` | tree | dark purple-red `#70283b` | purple (alternative) | - |
| Glow Berries | `minecraft:glow_berries` | vine | orange | - | Glowberry |
| Salmonberries | `delightful:salmonberries` | bush | orange-pink | - | - |
| White Strawberries | `neapolitan:white_strawberries` | crop | white `#af8774` | - | Hawberry |
| Tangleberrie | `botania:tangleberrie` | decoration, not food | - | - | - |

Delightful's crimson and nightshade berry sacks exist, but their berries do not (`forge:fruits/crimson_berries`,
`nightshade_berries` are empty).

## 4. Fruit -> effect (every raw fruit in the pack)

"Raw" = the effect when the fruit is eaten as is. "Fruit foods" = the effect its Fruits Delight jello and jam give (or
the owning mod's own fruit foods), the pack's existing association for that fruit.

| Fruit | Id | Raw | Fruit foods |
|---|---|---|---|
| Acorn | `delightful:acorn` | none | none |
| Apple | `minecraft:apple` | none | Absorption |
| Banana | `neapolitan:banana` (and `alexsmobs:banana`) | Climbing 15 s (Neapolitan banana) | Agility (Neapolitan banana foods) |
| Bayberry | `fruitsdelight:bayberry` | none | Leaf Piercing |
| Blueberry | `fruitsdelight:blueberry` | none | Night Vision + Brightening |
| Cantaloupe | `delightful:cantaloupe_slice` | none | Health Boost |
| Cherry | `vinery:cherry` | none | none |
| Chorus Fruit | `minecraft:chorus_fruit` | random teleport | Teleport |
| Coffee Berries | `farmersrespite:coffee_berries` | Wither 5 s (80%), Caffeinated 10 s | Caffeinated |
| Cranberry | `fruitsdelight:cranberry` | none | Shrinking |
| Durian | `fruitsdelight:durian_flesh` | none | Alienating + Suspicious Smell |
| Fig | `fruitsdelight:fig` | none | Digesting |
| Glow Berries | `minecraft:glow_berries` | Glowing 15 s | Glowing |
| Grapes (red, white; jungle, savanna, taiga) | `vinery:*grape*` | none | none (wines have their own) |
| Hamimelon | `fruitsdelight:hamimelon_slice` | none | none |
| Hawberry | `fruitsdelight:hawberry` | none | Appetizing |
| Kiwi | `fruitsdelight:kiwi` | none | Cycling |
| Lemon | `fruitsdelight:lemon` | none | Haste + Refreshing |
| Lychee | `fruitsdelight:lychee` | none | Strength III |
| Mango | `fruitsdelight:mango` | none | Strength III + Rage Aura |
| Mangosteen | `fruitsdelight:mangosteen` | none | Sliding |
| Melon | `minecraft:melon_slice` | none | Regeneration |
| Mulberry | `upgrade_aquatic:mulberry` | none | none |
| Orange | `fruitsdelight:orange` | none | Regeneration + Recovering |
| Peach | `fruitsdelight:peach` | none | Heal Aura |
| Peanut | `vintagedelight:peanut` | none | none |
| Pear | `fruitsdelight:pear` | none | Lozenge |
| Persimmon | `fruitsdelight:persimmon` | none | Astringent |
| Pineapple | `fruitsdelight:pineapple_slice` | none | Speed + Sweetening |
| Raspberry | `hearthandharvest:raspberry` | none | none |
| Salmonberries | `delightful:salmonberries` | Water Breathing 5 s | Water Breathing (gummy) |
| Strawberries | `neapolitan:strawberries` | none | none |
| Sweet Berries | `minecraft:sweet_berries` | none | none |
| White Strawberries | `neapolitan:white_strawberries` | none | none |

## 5. Brain freeze

Owner (2026-11-17): a smoothie drunk while another was drunk in the last minute has a 20% chance, +20% per further one
in the window (2nd 20%, 3rd 40%, 4th 60%, 5th 80%, 6th 100%). On a hit: the powdered snow overlay and freeze damage for
10 seconds (set the player's frozen ticks so the vanilla overlay draws and the vanilla freeze damage applies), and a
blue action-bar message ("Brain freeze!"). Agent defaults, unconfirmed: a hit resets the count; Fire Resistance does
not block it (freeze damage ignores it in vanilla anyway).

Home: Creative Delight Java (food code goes in CD; creative-delight-routing). A `#creativedelight:brain_freeze` item tag
lists what counts, so the food families below join by tag, not code.

Other cold foods found (food_catalog.md, food_families.md `frozen`):

| Family | Items | Default |
|---|---|---|
| Ice cream `frozen/ice_cream` | 11 (Neapolitan 7, Delightful 2, Fungi Delight truffle, ...) | counts |
| Popsicle `frozen/popsicle` | 8 (melon, cantaloupe, hamimelon, kiwi, chocolate, chorus, corn, ...) | counts |
| Shaved ice `frozen/shaved_ice` | 1 (Cantaloupe Shaved Ice) | counts |
| Milkshakes (`drink/milk`, cold ones only) | 10 (Neapolitan 6, Delightful 2, Fruits Delight mango, ...) | counts; hot chocolate excluded |
| Iced Berry Matcha Latte | `delightful:berry_matcha_latte` | counts |
| Ice Cubes | `neapolitan:ice_cubes` | not counted (ingredient) |
| Frozen Flesh, Ice Sickle | `hostiledelight:frozen_flesh`, `immersive_weathering:ice_sickle` | not counted (not a cold treat) |

## 6. Build notes

- Items: CD startup, the shared sweets table shape from the brief (`global.sweets`-style entry per smoothie: key, name,
  fruits, effects); ids in `item_dict.js` (Creative Common rebuild). Neapolitan's `strawberry_banana_smoothie` stays
  removed; new ids are Creative Delight's (ids follow the owning mod).
- Missing fruit keys: `i.t_fruits_cherry` has no members in `forge:fruits/cherry`; add `vinery:cherry` to it (tag helper).
- Sprites: textures rule; source and palette to be picked by the owner (ask at build time).
- Fluids, jars, can parity, stack size (drink group), compost, food family `drink` row: per the brief's checklist.
- Changelog: one family `## Smoothies` when built.
