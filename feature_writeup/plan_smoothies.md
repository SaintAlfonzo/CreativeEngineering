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
| 1 | "add mixed berry (red berry + blue berry + purple berry, identify color candidates)" | open | Search widened to every item named "berry", tree fruit included (section 3). Recipe: 2 red berries + 1 blue or purple berry + bottle. Effects come from the berries used, stored on the item: each berry gives its effect at half base length (section 2.1) | 2026-11-18 | AskUserQuestion 2026-11-17, chat 2026-11-18, AskUserQuestion 2026-11-18 |
| 2 | "the first fruit in the name is 2 input, the second is 1 in the recipe" | clear | Shapeless: 2 x first fruit + 1 x second fruit + glass bottle (the removed Neapolitan recipe's shape: 2 strawberry, 1 banana, bottle). One-fruit names: 3 x that fruit | 2026-11-17 | chat 2026-11-17 |
| 3 | "pear (... gives a buff that allows eating at any hunger)" | premise | That effect already exists: Fruits Delight Appetizing ("eat and drink even when you are full", effects.md line 175), now from hawberry foods. Ruling: pear smoothie = 2 pear + 1 hawberry; Appetizing 5 min, Lozenge 30 s | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 4 | "all smoothies should be drinkable with the honey texture at any hunger" | open | "Honey" means the honey bottle drinking sound. Every smoothie is always edible and plays the honey drink sound | 2026-11-17 | AskUserQuestion 2026-11-17 |
| 5 | "drinking too many smoothies increases the chance of getting the powdered snow overlay and taking frost damage" | open | Every cold food after the first within 1 min rolls: 20%, +20% per further roll in the window. Freeze length by the number of rolls up to and including the hit: 5, 8, 11, 14, 20 s (20 s from the 5th roll on); lengths configurable. History: 10 s flat (2026-11-17) -> 5/8/11/14/20 (2026-11-18) | 2026-11-18 | AskUserQuestion 2026-11-17, chat 2026-11-18 |
| 6 | "brain freeze, give blue text message on in world text line above hotbar, where other features hook in" | premise | The line is the vanilla overlay (action bar) message, also used by the Creative Calendar held line (time.md 3.7) and the jukebox "Now playing" line (raspberry_pie.md). There is no shared Creative Common hook; send it with `displayClientMessage(text, true)`, colored blue | 2026-11-17 | agent check |
| 7 | "search other food families to cause brain freeze" / "add the proposals to cold food. add ice cubes if they are edible" | clear | All proposals count (section 5). Ice Cubes are edible (0 hunger, always edible; food_catalog.md line 1569) and count | 2026-11-18 | chat 2026-11-18 |
| 8 | "investigate fruits delight input count to effect length ratios before deciding effect lengths" | open | Findings section 4.1. Ruling: first fruit (x2) = its Fruits Delight base length (jam/jello), second fruit (x1) = half base (juice level); amplifiers as Fruits Delight has them (Strength III stays III). Owner values (pear) win. History: agent default 60 s / 30 s, amp I (2026-11-17) replaced | 2026-11-18 | AskUserQuestion 2026-11-18 |
| 9 | "only slices should be used of fruits" | clear | A fruit with a slice item goes in as slices, same counts (2 + 1): lemon, orange, pineapple, melon, cantaloupe. The others have no slice and go in whole | 2026-11-18 | chat 2026-11-18, AskUserQuestion 2026-11-18 |
| 10 | "use the strawberry banana texture for now, mark a mass swap setup later" | clear | Every smoothie uses Neapolitan's `strawberry_banana_smoothie.png` as its sprite (copy of upstream art; sources.csv row at build). A later row sets up the mass swap to per-flavor sprites | 2026-11-18 | chat 2026-11-18 |
| 11 | Mango's Rage Aura (idle mobs within 24 blocks target you) in Orange Mango, Peach Mango, Mango Pineapple | open | Kept, as in every Fruits Delight mango food; agent default, unconfirmed (was "dropped" in the 2026-11-17 draft) | 2026-11-18 | agent default, unconfirmed |
| 12 | Mixed berry: the same berry twice | open | The two red berries may differ; a berry used twice gives one effect at the full base (2 x half). Agent default, unconfirmed | 2026-11-18 | agent default, unconfirmed |

Hunger and saturation follow the `drink` family rule (food_families.md) through the food tuning pass; not set here.

## 2. Smoothie table

Recipe = shapeless in the crafting grid, plus a glass bottle. Base lengths are the fruit's Fruits Delight jam (`_jelly`
bottle) and jello lengths (section 4.1); first fruit = base, second fruit = half base (ruling 8).

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
the strawberry banana smoothie placeholder (ruling 10). Cherry Lemon, Cherry and Raspberry Peach get nothing from their
first fruit (cherry and raspberry have no effect in the pack).

### 2.1 Mixed Berry effects

2 red + 1 blue or purple, any berries of the slot's color (section 3). Each berry adds its effect at half base; a berry
used twice adds up to the full base (ruling 12). The item stores the result and shows it in its tooltip, so different
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

## 3. Berry color candidates (mixed berry)

Every item whose id or name has "berry", minus foods made from them. Color = average of the most saturated half of the
item sprite's pixels, read from the mod jar (no texture edited).

| Berry | Id | Source | Color | Slot | In another smoothie |
|---|---|---|---|---|---|
| Raspberry | `hearthandharvest:raspberry` | bush | red | red | Raspberry Peach |
| Strawberries | `neapolitan:strawberries` | crop | red `#cb3938` | red | Kiwi Strawberry |
| Sweet Berries | `minecraft:sweet_berries` | bush | red | red | - |
| Cranberry | `fruitsdelight:cranberry` | bush | red-orange `#b1431d` | red | Cran Apple |
| Hawberry | `fruitsdelight:hawberry` | tree | red `#d43043` | red | Hawberry, Pear |
| Cherry | `vinery:cherry` | tree | red | red | Cherry, Cherry Lemon |
| Coffee Berries | `farmersrespite:coffee_berries` | bush | dark red `#7d1527` | not fruit-like (caffeine, Wither) | - |
| Blueberry | `fruitsdelight:blueberry` | bush | blue (only blue berry) | blue | Blueberry Lemon |
| Mulberry | `upgrade_aquatic:mulberry` | vine | purple `#844766` | purple | - |
| Bayberry | `fruitsdelight:bayberry` | tree | dark purple-red `#70283b` | purple | - |
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

### 4.1 Fruits Delight: fruit count vs effect length

Checked 2026-11-18 from the dumps (`kubejs/exported/food/recipes.json` inputs, `foods.json` effects). Length does not
follow fruit count. Each fruit has a base length, set by how strong its effect is (jam and jello: Kiwi 4 s, Peach and
Lychee 10 s, most 20 s, Melon 40 s, Cranberry, Fig, Glowberry, Mangosteen and Persimmon 80 s); the food form scales it:

| Form | Fruits per serving | Length vs base | Examples (base) |
|---|---|---|---|
| Jam (`_jelly` bottle, pot) | 2 to 4 | 1x | Lemon 20 s, Cranberry 80 s, Kiwi 4 s |
| Jello | from a jam | 1x | same as the jam |
| Muffin, tart, pie | 0.4 to 3 | 1x | Blueberry Muffin 20 s, Fig Tart 80 s |
| Juice, tea, soup, shaved ice | 1 to 4 | 0.5x | Lemon Juice 10 s, Peach Tea 5 s, Mangosteen Tea 40 s, Hawberry Tea 10 s |
| Cookie (12 per batch) | 0.17 | 0.25x | Lemon Cookie 5 s, Bayberry Cookie 5 s |
| Meat main (with Nourishment) | 0.5 to 1 | 2x | Orange Chicken 40 s, Pineapple Marinated Pork 40 s |
| Salad, cake, sugared fruit | 1 to 2 | 1.5x | Mango Salad 30 s, Mangosteen Cake 120 s, Pear with Rock Sugar 30 s |

Amplifiers never drop with the form: Mango and Lychee give Strength III in jam, tea and milkshake alike. Smoothies (3
fruits, a drink) take the jam base for the doubled fruit and the juice half for the single one (ruling 8).

## 5. Brain freeze

Owner (2026-11-17, 2026-11-18):
- Every cold food (tag below) eaten while another was eaten in the last minute rolls once: the 1st roll 20%, +20% per
  further roll in the window (20, 40, 60, 80, 100%).
- On a hit: the powdered snow overlay and freeze damage (set the player's frozen ticks so the vanilla overlay draws and
  the vanilla freeze damage applies), and a blue action-bar message ("Brain freeze!").
- Length by the number of rolls up to and including the hit: 1 -> 5 s, 2 -> 8 s, 3 -> 11 s, 4 -> 14 s, 5 or more ->
  20 s. Configurable: the list of lengths, the chance step and the window go in Creative Delight's common config
  (`[brainFreeze]`; backup in `configs_edited/` when hand-edited).
- Agent defaults, unconfirmed: a hit resets the count; Fire Resistance does not block it (vanilla freeze damage ignores
  it anyway).

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
