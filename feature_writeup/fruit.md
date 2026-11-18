# Fruit

Reference for every raw fruit in the pack: which have slices, which effect each one carries, berry colors, and how
Fruits Delight scales effect length. Investigation for [plan_smoothies.md](plan_smoothies.md) (2026-11-17,
2026-11-18); [smoothies.md](smoothies.md) uses it. Recipe rules for fruit drinks: [creative_delight_drinks.md](creative_delight_drinks.md).
Fruits Delight's own fixes: [fruits_delight.md](fruits_delight.md).

## 0. At a glance

<!-- generated:status_index begin open_work sha1=a86ebe23d6f0 -->
**Open work** (generated from `todo/rows/*.csv` by `tools/status_index.py`; change a row with `tools/todo.py`). Status: 1 open TODO row (Planned 1), 0 Complete.

| Id | Status | Tests | Change |
|---|---|---|---|
| `fruit #5` | Planned | S P | Move Rage Aura off mango to another food (ghost pepper candidate) |
<!-- generated:status_index end open_work -->

- Fruits: `#forge:fruits` holds 27 items; the food catalog's `raw/fruit` type 44 ([food_catalog.md](food_catalog.md)).
- Effects: a fruit's effect is its Fruits Delight jam and jello effect; 10 fruits (grapes as one) have none (section 3).
- Lengths scale by food form, not fruit count (section 3.1).
- Open note: Rage Aura moves off mango (section 5).

## 1. Overview

Fruit sources: Fruits Delight (most tree and bush fruit), vanilla (apple, melon, sweet and glow berries, chorus fruit),
Neapolitan (strawberries, white strawberries, banana), Delightful (cantaloupe, salmonberries, acorn), Hearth and Harvest
(raspberry), Upgrade Aquatic (mulberry), Vinery (cherry, grapes), Vintage Delight (peanut), Farmer's Respite (coffee
berries). Item keys: `i.t_fruits_<name>` in `item_dict.js` (`#forge:fruits/<name>`); `#forge:fruits/cherry` is empty
today (Vinery's cherry is only in `#c:berries` and `#forge:berries`).

## 2. Slices

Fruits with a slice item: lemon, orange, pineapple (Fruits Delight), melon (vanilla), cantaloupe (Delightful), hamimelon
(Fruits Delight); durian has its flesh. Every other fruit is used whole. Smoothies use only slices where a slice exists
(owner 2026-11-18); the juice and tea rules accept whole or sliced ([creative_delight_drinks.md](creative_delight_drinks.md)).
Cut yields and slice -> whole crafts are being changed by TODO `fruit #3` (melon, pumpkin and cantaloupe to 8, lemon and
orange to 5, pineapple 6 + 2 saplings).

## 3. Fruit -> effect (every raw fruit in the pack)

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
| Mango | `fruitsdelight:mango` | none | Strength III + Rage Aura (to move, section 5) |
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

### 3.1 Fruits Delight: fruit count vs effect length

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
fruits, a drink) take the jam base for the doubled fruit and the juice half for the single one ([plan_smoothies.md](plan_smoothies.md) ruling 8).

## 4. Berries by color

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

## 5. Notes

- **Rage Aura** (idle mobs within 24 blocks target you) is mango's Fruits Delight effect and stays on mango foods and
  the mango smoothies for now (owner 2026-11-18). Note: move it to a different food later; candidate the ghost pepper
  (`vintagedelight:ghost_pepper`, no effect today). TODO `fruit` family row.
- Coffee berries are a berry by name but not a fruit for recipes (caffeine, Wither chance).

## 6. File map

- [food_catalog.md](food_catalog.md) `raw/fruit` and `sweet` (jam and jello effects), [effects.md](effects.md) (what each
  Fruits Delight effect does).
- Dumps: `kubejs/exported/tags/item_tags.json` (`forge:fruits/*`), `kubejs/exported/food/foods.json` and `recipes.json`.

## 7. Tests

None: reference.

## 8. History

- 2026-11-18: written from the smoothie investigation (fruit effects, berry colors, slices, Fruits Delight length ratios).
