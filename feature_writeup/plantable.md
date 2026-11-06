# Plantable items

Test prefix **PT-** · Last updated **2026-11-02** · Overall status **BUILT 2026-11-01, unreleased (Interface & Tooltips bullets); SQ-PT-01 PASSED r1102b (tags: seed 48, crop 14, sapling 36, fungi 23, water 11, vine 23, vertical 5; the `[plantable]` log line says 49 / 37); CQ-PT-01 (the tooltip lines) open; not run in game**

## 0. At a glance

Every item that puts a growing plant in the world gets one dark green tooltip line naming its family. The family comes
from the class of the block the item places, so any mod's plants are covered with no list to keep.

| Line | Tag | What gets it |
|---|---|---|
| Seed Item | `#creativeengineering:plantable/seed` | seeds, pips, beans, grape seeds, nether wart: plant a crop and are not food |
| Plantable Crop | `#creativeengineering:plantable/crop` | foods that plant their own crop: carrot, potato, onion, garlic clove, sweet potato, peanut, ghost pepper, cave carrot, sweet / glow berries, blueberry, cranberry, mulberry, chorus succulent |
| Sapling | `#creativeengineering:plantable/sapling` | saplings, mangrove propagule, azalea, flowering azalea, pineapple sapling, banana frond, magic peanut |
| Fungi Sapling | `#creativeengineering:plantable/fungi_sapling` | mushrooms and nether fungi that bone meal grows huge (vanilla, Botania glowing mushrooms, Fungi Delight inky cap and morel, Quark glow shroom, crimson / warped fungus) |
| Water Plant | `#creativeengineering:plantable/water` | kelp (all Upgrade Aquatic kelps), seagrass, sea pickle, lily pad, sea grape plant, pickerelweed |
| Vine | `#creativeengineering:plantable/vine` | vine, twisting / weeping vines, ivy, vanilla pods, Floral Enchantment flower vines, Quark cut vine |
| Vertical Crop | `#creativeengineering:plantable/vertical` | sugar cane, cactus, bamboo, chorus flower, propelplant cane |

Each family tag also sits in `#creativeengineering:plantable`. No line (owner decision 2026-11-01): flowers, grass,
ferns, dead bush, leaves, coral, mushroom colonies, Botania petals and functional flowers.

| Part | Where | Status |
|---|---|---|
| Family tags (class rules + overrides) | [plantabletags.js](../kubejs/server_scripts/tags/plantabletags.js) | BUILT 2026-11-01 |
| Tooltip line | [plantable_tooltip.js](../kubejs/client_scripts/plantable_tooltip.js) | BUILT 2026-11-01 |
| Tag dict keys `i.t_plantable*` | [item_dict.js](../kubejs/startup_scripts/_lib/item_dict.js) "plantable families" block | BUILT 2026-11-01 (needs a restart; the scripts fall back to the literal tag ids until then) |

**Open first:** the rule list at the top of `plantabletags.js`.
**Reload:** `/reload` for the tags, F3+T for the tooltip script; restart for the dict keys.

## 1. Rules

First match wins, per item, at tag load:

1. **Overrides** (item_dict keys): items whose class says nothing or the wrong thing. Adzuki beans (turn dirt into
   adzuki soil), The Dirty Stuff tobacco seeds (plain MCreator block), Farmer's Delight rice (block shares the item's
   id) are Seed Items; mulberry and chorus succulent are Plantable Crops; pineapple sapling and banana frond are
   Saplings; Quark glow shroom is a Fungi Sapling; sea grape plant and both pickerelweeds are Water Plants; ivy and
   vanilla pods are Vines; propelplant cane is a Vertical Crop; Immersive Weathering weeds (a `CropBlock`) get nothing.
2. **Block class**: `SaplingBlock` (propagules included), `AzaleaBlock` -> Sapling; `MushroomBlock`, `FungusBlock` ->
   Fungi Sapling; `KelpBlock`, `SeagrassBlock`, `TallSeagrassBlock`, `SeaPickleBlock`, `WaterlilyBlock` -> Water Plant;
   `SugarCaneBlock`, `CactusBlock`, `BambooBlock`, `BambooSaplingBlock`, `ChorusFlowerBlock` -> Vertical Crop.
3. **Crops**: `CropBlock`, `StemBlock`, `NetherWartBlock`, `CocoaBlock`, `SweetBerryBushBlock`, `CaveVinesBlock`, or any
   other `BushBlock` / `VineBlock` placed by an item with its own id (wheat_seeds -> wheat; this is what catches pips,
   grape seeds and lemon seeds). Food -> Plantable Crop, unless the id has "seed" or "pip" (corn_delight's edible
   corn seeds) -> Seed Item. Items in `#botania:petals` are skipped here (petals place buried petals).
4. **Vines**: remaining `VineBlock` / `GrowingPlantHeadBlock` -> Vine.

Why the class and not the existing tags: `#forge:seeds` has 38 items, `#minecraft:saplings` 35, and neither knows about
pips, edible crops or water plants. Every mushroom block in the pack was checked to grow huge with bone meal (Botania's
glowing mushrooms pass vanilla's huge brown mushroom feature; Fungi Delight's backport has its own huge features).

## 2. Expected lists

From an offline run of the same rules over the 2026-10-30 `/dumpplaceable` block-item list with block classes resolved
from the jars (scratch work, not a pack tool). Counts: Seed Item 47 (+ adzuki beans, + Hearth and Harvest raspberry pips
added after that dump), Plantable Crop 14, Sapling 37, Fungi Sapling 23, Water Plant 11, Vertical Crop 5, Vine 23.
The live counts print in `logs/latest.log` as `[plantable] seed=.., crop=.., ...`.

- **Seed Item:** berry_good glow berry and sweet berry pips; Delightful cantaloupe seeds, salmonberry pips; Ender Crop
  ender seeds; Ender's Delight amberveil, chorusflame, ethereal saffron; Farmer's Delight cabbage seeds, rice, tomato
  seeds; Farmer's Respite coffee beans, tea seeds; Fruits Delight hamimelon seeds, lemon seeds; Immersive Engineering
  hemp seeds; vanilla beetroot / melon / pumpkin / wheat seeds, cocoa beans, nether wart; Neapolitan mint sprout,
  strawberry pips, adzuki beans; Rustic Delight bell pepper (3), coffee beans, cotton seeds; Supplementaries flax seeds;
  The Dirty Stuff tobacco seeds; Veggies Delight broccoli, cauliflower, turnip, zucchini seeds; Vinery grape seeds (8);
  Vintage Delight cucumber, ghost pepper, oat seeds; Corn Delight corn seeds.
- **Plantable Crop:** chorus succulent, onion, blueberry, cranberry, carrot, glow berries, potato, sweet berries, cave
  carrot, mulberry, garlic clove, sweet potato, ghost pepper, peanut.

## 3. Open (review when the live counts are in)

- Floral Enchantment `*_bush` items (17, `BushBlock`, same id as the block) get no line; their `*_vine` items are Vines.
  If the bush is what grows the vine, they should be Vines too.
- Neapolitan magic beans (plain item) get no line; check in game whether they plant a beanstalk.
- Delightful mini melon (`MiniMelonBlock`, no bush class) gets no line; check whether it plants.
- Ender's Delight amberveil / ethereal saffron read "Seed Item" though their names are plant names.

## 4. In-game tests

| ID | Check | Pass |
|---|---|---|
| PT-01 | Hover wheat seeds, raspberry pips, adzuki beans, rice, corn seeds | "Seed Item" |
| PT-02 | Hover carrot, sweet berries, glow berries, blueberry, mulberry | "Plantable Crop" |
| PT-03 | Hover oak sapling, mangrove propagule, azalea, a Quark blossom sapling, banana frond | "Sapling" |
| PT-04 | Hover red mushroom, crimson fungus, a Botania glowing mushroom, glow shroom | "Fungi Sapling" |
| PT-05 | Hover kelp, sea pickle, lily pad, pickerelweed; vine, ivy, weeping vines; sugar cane, bamboo, chorus flower | "Water Plant"; "Vine"; "Vertical Crop" |
| PT-06 | Hover poppy, grass, a Botania petal, a coral fan, weeds, oak leaves, a dirt block | no plant line |

## 5. History

- 2026-11-01: built. Families and labels set by the owner (flowers no line; mushrooms that grow huge are "Fungi Sapling";
  "Vertical Crop" for cane, cactus, bamboo and chorus).
- 2026-11-02 (deep scan): SQ-PT-01 PASSED in r1102b; CQ-PT-01 waits for the client round.
