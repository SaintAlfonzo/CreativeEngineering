# Texture reuse from culinary mods

Catalogue of food-mod textures that could serve new pack items or retexture existing ones. Research only:
nothing here is built. One section per mod; Rustic Delight first (2026-10-22).

## 0. At a glance

| Mod | Version | Surveyed | Notes |
|---|---|---|---|
| Rustic Delight | 1.3.0 | 2026-10-22 | 41 item + 46 block textures; every item is live in the pack; 1 orphan texture |
| Farmer's Respite, Delightful, More Delight, Corn Delight, Fruits Delight, Alex's Delight, Abnormals Delight, Chef's Delight, Ender's / Nether's Delight, Create Central Kitchen, Cooking for Blockheads | | not yet | next candidates |

## 1. How a texture gets reused

- **New KubeJS item on a borrowed sprite**: `.texture('<ns>:item/<name>')` in the startup script, as
  `development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/baking_ingredients.js` already does (frosting on `rusticdelight:item/cotton_boll`).
- **Retexture an existing item**: a model override in `kubejs/assets/<ns>/models/item/<id>.json` pointing
  `layer0` at another mod's sprite (no file copy needed).
- **Recolour / reshape**: `textureswapper/palette_swap.py` (single file or bulk palette onto one shape; `--drop-darkest N` / `--drop-lightest N` leave out an item's outline or highlight shades when recolouring a block); the
  output goes into `kubejs/assets/creativeengineering/textures/`.
- **Caveat**: borrowing a sprite that a live item still shows makes two items look the same in JEI and the
  inventory. Prefer sprites freed by a merge or purge, or recolour.

## 2. Rustic Delight 1.3.0

Contact sheets (regenerable): unzip the jar's `assets/rusticdelight/textures` and tile them.

### 2.1 Fits for open TODO rows

| Need (TODO row) | Rustic texture(s) | How |
|---|---|---|
| Farmer's Respite `long_coffee` and `strong_coffee` both use `farmersrespite:item/coffee` (same problem as the teas in drinks #2) | `item/dark_coffee` (strong), `item/milk_coffee` or a lighter recolour (long) | Model override. Same glass-mug-with-steam style as Respite's teas, so no style clash. `dark_coffee` is a smaller cup. Only clean if the coffee merge (drinks #3) purges Rustic's drinks; otherwise recolour instead |
| Coffee merge (drinks #3) | `coffee`, `dark_coffee`, `milk_coffee`, `honey_coffee`, `chocolate_coffee`, `coffee_beans`, `roasted_coffee_beans`, `golden_coffee_beans` | Whichever side is purged frees its sprites. Rustic has the wider set (5 drinks, raw / roasted / golden beans); Respite has beans, berries, cake |
| Tea recolours (drinks #2) | `item/coffee` mug | Same mug shape as Respite's teas; a palette swap base for the long / strong tea variants alongside `textureswapper/drink/` |
| Frosting texture (textures/food #3) | `item/cotton_boll` (former placeholder) | Done 2026-10-25: frosting has its own sprite, the cotton boll is free again |
| Egg solution and batters (ingredients #2) | `item/batter` (bowl of pale batter) | Recolour base for egg solution or other batter bowls if any are items; `corn_delight:cornbread_batter` is the other batter in the pack |
| Fats and oils (ingredients #10) | `item/cooking_oil` (clear bottle, yellow oil) | Recolour base for an animal fat or blubber oil bottle; compare `delightful:animal_oil_bottle` and `alexsmobs:fish_oil` first |
| Oven bakeware (`textureswapper/baking/`) | `block/tray_top`, `block/tray_bottom`, `block/*_pancakes_tray_top` | Wooden serving tray; weak fit for metal bakeware, better for a serving / feast tray |

### 2.2 Duplicates in the pack (merge before retexture)

| Rustic item | Other mod | Note |
|---|---|---|
| `potato_salad` | `moredelight:potato_salad` | Both a bowl of potato salad; More Delight's is yellower with green flecks. Merge candidate (item_merges) |
| `chocolate_pancakes`, `honey_pancakes`, `vegetable_pancakes` | `supplementaries:pancake` | Supplementaries' is a plain pancake disc item; Rustic's are placeable stacks. Not a straight merge |
| `coffee_beans`, coffee plant, `wild_coffee` | Farmer's Respite coffee | Already tracked: drinks #3, wild-crops #4 |
| `potato_slices` | `moredelight:diced_potatoes` | Done: item_merges 2.4 |

### 2.3 Templates for new items

| Texture(s) | Template for |
|---|---|
| `block/crate_bottom`, `*_crate_side`, `*_crate_top` | Crop crates: same wooden frame, swap the produce fill (crates for crops that lack one) |
| `block/bag_side`, `bag_side_tied`, `bag_bottom`, `*_bag_top` | Seed / produce sacks: plain sack sides, only the top changes |
| `item/stuffed_bell_pepper_*` (3 colours) | Other stuffed vegetables: hollowed shell with a rice filling |
| `item/bell_pepper_*` vs `item/roasted_bell_pepper_*` | Shows the raw -> roasted shading pass (darker, charred spots) for "roasted X" sprites |
| `item/calamari_slice`, `cooked_calamari_slice` | Raw / cooked slice pair for other seafood slices |
| `item/spring_rolls`, `fruit_beignet`, `fried_chicken`, `fried_mushrooms` | Fried-food bases (golden batter shading) |
| `item/bell_pepper_seeds`, `cotton_seeds` | Seed scatter sprites for new seeds |
| crop stages (`bell_peppers_stage0-7`, `coffee_stage0-5`, `cotton_stage0-3`), `wild_*` | Bush-crop and wild-patch bases |

### 2.4 Unused

- `block/tray_top`: no model references it (the pancake blocks use their own `*_tray_top`). Free to borrow.

## 8. History

- 2026-10-22: written; Rustic Delight 1.3.0 surveyed.
