# Brief: food / placeable

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `placeable`; verified 2026-10-31 (Display Delight is
now the pack's 1.7.0 port, [display_delight.md](../display_delight.md); rows #4, #5 added). Feature writeup:
[placeable_food.md](../placeable_food.md) (test prefix `PL-`), generated catalog [placeable_catalog.md](../placeable_catalog.md).

## Scope and order

| Row | Status | Size | Build with |
|---|---|---|---|
| `food/placeable #2` Catalog review pass | In Progress | small, tool only | first: rerun on the 2026-10-30 dump |
| `food/placeable #3` Drinks placeable | In Progress | medium (smaller since the DD port) | after #2's rerun, with `food/drinks #2` |
| `food/placeable #4` Dough blocks for every dough | Planned | small (one dough left) | any time; see TODO note |
| `food/placeable #5` Barbeque's Delight model recolor review | Planned | small, resource pack | after the BBQ port (`largecontent/mod-additions #37`) |

Order:
1. **#2** first: the catalog and `placeable.json` are older than the 2026-10-30 dump (`--check` says OUT OF DATE), and
   the stale "Display Delight gaps" list would mislead #3.
2. The drink merges `#3` waited on are built (`food/drinks #3`, item_merges.md 2.1-2.8). Display Delight 1.7.0 already
   has drink blocks for Farmer's Respite's teas and End's Delight's drinks, so #3 is now about the rest.
3. **#3**, then rerun `tools/gen_coaster_models.py` and `tools/placeable_catalog.py`.

---

## placeable #2: Placeable catalog review pass
Status **In Progress** · Work: tool · id `food/placeable #2`

### Goal
Build the catalog and the five placeable tags from the live Display Delight (DD) links, not the fallback data. Move
the real foods in the catalog's "Review" list into `INCLUDE`.

### Now (2026-10-31)
- The owner's `/dumpall` of 2026-10-30 00:42 wrote `kubejs/exported/food/placeable.json` (present in this instance,
  git-ignored): `ddLoaded` true, Display Delight maps placed 135, plate 57, small plate 25. Display Delight is the 1.7.0
  port (`mods/backportedDisplay-1.7.0-ce-19.2-01.jar`; links by block name, display_delight.md 2.1); the tool finds it
  through `tools/display_delight_jar.py`.
- The committed [placeable_catalog.md](../placeable_catalog.md) and
  [placeable.json](../../kubejs/generation_scripts/food/placeable.json) predate that dump: `python3
  tools/placeable_catalog.py --check` (read-only, run 2026-10-31) prints `placeable.json is OUT OF DATE (4 items lack
  keys)`. Current counts: placeable 159, placeable/sneak 103, plate 34, small plate 13, coaster 302 (374 items, 23 mods).
- The catalog's "Display Delight gaps" section lists 93 foods (Abnormals 15, End's 24, Ender's 12, Respite 14,
  Miner's 12, Brewin' 8, Nether's 7, FD 1) whose 1.7.0 display block exists but which were not linked when it was
  generated. The 2026-10-30 dump links them (e.g. `farmersrespite:green_tea`, `abnormals_delight:duck_noodles` are in
  its `placed` map and carry `#displaydelight:displayable`), so the rerun should empty that list and raise the counts.
- `tools/placeable_catalog.py` has an uncommitted edit: `COASTER_DIR` now points at
  `development/creativeBrewing/members/brewin/...` (the Brewin' port moved into Creative Brewing).
- The "Review" section ([placeable_catalog.md:660](../placeable_catalog.md)) still has about 140 rows of regex noise from
  `FOOD_BLOCK_WIDE` ([placeable_catalog.py:71](../../tools/placeable_catalog.py)): Comforts hammocks (`Ham`), cogwheels,
  crushing and water wheels (`Wheel`), `farmersdelight:rice` / `rice_bale` / `wild_rice` (`Rice`), the Fruits Delight
  jelly blocks and cauldrons and Upgrade Aquatic jelly torches (`Jelly` / `Jam`), Vinery wine racks and `wine_box`,
  `architects_palette:bread_block`, `vintagedelight:cheese_mold`.
- Real food blocks in it: `brewinandchewin:flaxen_cheese_wheel` and `scarlet_cheese_wheel` (`CheeseWheelBlock`: placed,
  then cut into wedges with a knife); their unripe versions are placed to age and are not food yet. Not in `INCLUDE`
  (line 90 holds only the two Corn Delight blocks).

### Build
1. In [tools/placeable_catalog.py](../../tools/placeable_catalog.py):
   - `INCLUDE` (line 90): add
     `'brewinandchewin:flaxen_cheese_wheel': 'cheese wheel block, cut with a knife into wedges'` and the scarlet one.
   - Make `FOOD_BLOCK_WIDE` (line 71) narrower so that Review only lists open questions: `Ham` → `Ham(?!mock)`,
     `Wheel` → `CheeseWheel`. Keep `Rice` and `Medley`: `RiceRollMedleyBlock` and `PineappleRiceBlock` are tagged
     today, and the wide regex is only the Review net, but don't narrow it blindly. Add a `REVIEW_IGNORE` regex for the rest:
     `^(farmersdelight:(rice|rice_bale|wild_rice)|fruitsdelight:\w+_(jelly_block|jello_cauldron|jelly_cauldron)|upgrade_aquatic:\w+_jelly_torch|vinery:\w*wine_(rack|box)\w*|architects_palette:bread_block)$`
     (add `vintagedelight:cheese_mold` and the hammock / wheel ids if the regex change misses them) and apply it in
     `review_candidates()` (line 273). Don't put these in `EXCLUDE`: that would list ~120 rows under
     "Excluded" with a reason each.
   - Leave `upgrade_aquatic:mulberry_jam_block` and `create_central_kitchen:mulberry_jam_bucket` in Review for one pass
     (check in game whether the jam block is edible; it is a decor block in Upgrade Aquatic).
2. `python3 tools/placeable_catalog.py` on the 2026-10-30 dump (no new `/dumpplaceable` needed unless food mods change
   again), then `--check`. It also writes the item_dict block (4 new keys).
3. Expected change: the 93 gap rows become catalog rows (counts rise well above 159 / 34 / 13), plus the two cheese
   wheels in `placeable` (plain right click: `BlockItem`, not sneak). Update the counts in
   [placeable_food.md](../placeable_food.md) section 0 and PL-01, then rerun `gen_coaster_models.py`.
4. Close the row (Complete once PL-09 passes).

### Ids and data
- `brewinandchewin:flaxen_cheese_wheel` and `scarlet_cheese_wheel`: Brewin' port, now a Creative Brewing member,
  `development/creativeBrewing/members/brewin/src/main/java/umpaz/brewinandchewin/common/registry/BnCItems.java:57,60`
  (`BlockItem`, stack 16; verified 2026-10-31).
  The tool adds item_dict keys for them if they have none (they already have keys from the Brewin' sections).
- Tags: unchanged (`#creativeengineering:placeable` and the others via `i.t_placeable*`).

### Decisions
- Ripe cheese wheels only, or the unripe ones too? **Recommend ripe only.** An unripe wheel is not food, and the line
  would suggest it can be eaten.

### Gotchas
- Don't run the tool without the export: it falls back to `/dumpfood` and silently rebuilds from fallback data (the
  catalog's source line then says `/dumpfood`). `--check` compares only `placeable.json`.
- The tool's "gap" test reads the dump's DD maps; a catalog built before the port's links were dumped lists every new
  1.7.0 block as a gap with the misleading text "not in the matching tag".
- `tools/gen_coaster_models.py` imports `placeable_catalog` (`pc.load()`), so changes to `load()` reach it too.

### Tests
- Existing PL-01 (counts) and PL-09. New: PL-new1 (proposed, numbered when queued; the 2026-10-21 draft called it
  PL-10, not in the writeup): hover a Flaxen Cheese Wheel -> "Placeable"; place it and cut it with a knife.
- PL-new2 (proposed): Green Tea and Duck Noodles show "Placeable (while sneaking)" after the rerun; sneak-placing works.

---

## placeable #3: Drinks placeable
Status **In Progress** · Work: KubeJS (TODO) / patcherMod (recommended below) (+ tool, assets) · id `food/placeable #3` · overlaps `food/drinks #2`

### Goal
Bottled FD-style drinks with no place-down today ("FD tea-like": Farmer's Respite teas and coffee, Fruits Delight
juices and teas, Delightful teas, Neapolitan milkshakes, Rustic Delight coffees and similar) go down the way FD's
Apple Cider does through Display Delight: sneak + right click the top of a block places a small bottle block, and
breaking it gives the drink back.

### Now (2026-10-31: read this first; the 1.2.0 notes below are kept for the class details)
- Display Delight is the pack's **1.7.0 port** since 2026-10-25 (`development/backportedDisplay`, mod id
  `displaydelight`, upstream package `com.jkvin114.displaydelight` kept). It links a block to its food **by block name**
  (prefix -> mod, display_delight.md 2.1), still behind the `#displaydelight:displayable` tag. Its block classes include
  `block/DrinkBlock` and `block/HotDrinkBlock`; `init/BlockAssociations` keeps the public `blockMap` / `itemMap` maps and
  `addToMap`.
- **Drinks already placeable** (2026-10-30 dump, `placed` map): FD Apple Cider, Hot Cocoa, Melon Juice, Glow Berry
  Custard; Delightful Matcha Latte, Berry Matcha Latte, Ender Nectar, Matcha and Salmonberry Milkshakes; Corn Delight
  Creamy Corn Drink; Ender's Chorus Juice; End's Bubble Tea, Chorus Flower Tea, Chorus Fruit Milk Tea, Dragon Breath
  Soda; **Farmer's Respite black, green, yellow, rose hip, dandelion, purulent and gambler's tea** (`fr_*` blocks, new in
  1.7.0). FR coffee has a block but the item is purged (item_merges.md 2.8). Of the 82 `drink`-typed paths in
  food_types.json, 15 are DD-placed by this count (FD's are typed elsewhere).
- **Still not placeable:** FR long / strong tiers, Fruits Delight juices and teas, Rustic Delight's 8 coffees,
  Delightful lavender / azalea tea, Neapolitan milkshakes, Builder's Tea, FD milk bottle, the pack's chorus juice and
  others in the drink list (recount after `placeable #2`'s rerun).
- The pack already registers its own DD blocks in **patcherMod**: `compat/DisplayDelightDishes.java` +
  `DisplayDelightDishBlocks.java` read `creativeengineering/displaydelight_dishes.json` (10 dishes, kinds `bowl` /
  `plate`), register a DD `FoodBlock` per entry as `creativeengineering:<block>`, and call
  `BlockAssociations.addToMap(item, block, false)` on `LevelEvent.Load`; `tools/gen_dd_dishes.py` writes the
  blockstates, models, loot, tag and names. That is the existing route for exactly this job.

**2026-10-21 notes on 1.2.0 (superseded where they differ):**
- DD (`displaydelight-1.2.0.jar`, decompiled, verified) handles placement in
  `events/DisplayEvents.onCheck` (`RightClickBlock`): when the held stack is in `#displaydelight:displayable`, it calls
  `InterationManager.tryPlaceItem`. That runs only while sneaking: `BlockAssociations.getBlockFor(item)`, then
  `target.asItem().useOn(new UseOnContext(player, hand, hit))`. So **the display block needs its own BlockItem**: the
  vanilla `BlockItem.place` shrinks the held stack, which here is the drink.
- The links are `public static HashMap`s in `init/BlockAssociations` (`blockMap`, `itemMap`, plus the plate maps),
  filled with `putIfAbsent` by `addToMap(Item, Block, boolean isPlated)`. `DisplayEvents.onWorldLoad`
  (`LevelEvent.Load`, `ServerLevel` only) fills them only from DD's own block lists (`DisplayBlocks.getAll()`...). A pack
  block therefore has to be added to the map by the pack. The tag alone does nothing.
- DD's drink block: `block/DrinkBlock extends AbstractItemBlock` (public constructor `DrinkBlock(Properties)`). Shape
  4,0,4 to 12,10,12. Facing is the player's opposite. `canSurvive` needs a center-supported block below.
  `getCloneItemStack` returns the linked food. Properties from `DisplayBlocks.baseProps()`:
  `Properties.of(Material.f_76320_).noOcclusion().instabreak()` + `SoundType.f_56744_` (glass for drinks; SRG names
  from the jar, not mapped here). Its loot table drops the food with `survives_explosion`
  (`data/displaydelight/loot_tables/blocks/apple_cider.json`).
- DD models for bottles: `displaydelight:block/apple_cider` (bottle texture `#4` = `displaydelight:block/glass_bottle`
  64x64, liquid `#6` = 16x16 `displaydelight:block/apple_cider`); `melon_juice` uses `#3`; `hot_cocoa` is a mug with
  `#8`; milkshake glasses `df_matcha_milkshake` (`#3` = `displaydelight:block/df_milkshake`). Blockstates rotate by
  facing (y 0/90/180/270).
- Not placeable today (food catalog type `drink` vs the placeable catalog, 2026-10-21): 57 of 62 `drink` items have no
  DD placement. Only FD Apple Cider, Delightful Berry Matcha Latte / Matcha Latte / Ender Nectar, and Corn Delight
  Creamy Corn Drink are DD placed. Of the 57, Honey Bottle has the coaster; the other 56 can't be put down at all. Plus non-edible bottle drinks that are not in the food catalog:
  `farmersrespite:gamblers_tea` (+ long / strong), `farmersrespite:strong_hot_cocoa`, `strong_melon_juice`,
  `abnormals_delight:cherry_cream_soda`, `passion_aloe_nectar`, `pickerelweed_juice`, `create:builders_tea`,
  `farmersdelight:milk_bottle`, `neapolitan:milk_bottle` ([stack_sizes.md](../stack_sizes.md) 1.2).
  FD's Hot Cocoa, Melon Juice and Glow Berry Custard are already DD placed.
- KubeJS 1902.6.2-build.73 (jar checked): `RegistryEventJS.createCustom(id, Supplier)` exists, and so does the
  `cardinal` block type (`HorizontalDirectionalBlockBuilder`: facing, rotated custom shape, but no `canSurvive` and no
  clone override).
- `kubejs/server_scripts/_lib/dump_placeable.js:75` already reads `BlockAssociations` live, so new links reach the
  catalog and tooltips through the normal `/dumpplaceable` → tool path.

### Build
**Revised 2026-10-31:** build it on the patcherMod dish route instead of KubeJS `createCustom` (steps 2-3 below are the
2026-10-21 KubeJS plan, kept as the fallback):
- A. Extend `displaydelight_dishes.json` with a `dish: "drink"` (and `"mug"`) kind; `DisplayDelightDishBlocks` registers
  a DD `DrinkBlock` / `HotDrinkBlock` for those entries (same `addToMap` link). `gen_dd_dishes.py` writes the drink
  blockstates / models (parents `displaydelight:block/apple_cider`, `hot_cocoa`, a milkshake glass) and tints the liquid
  texture from the item sprite (as step 1 below describes).
- B. The drink list from step 1's rules (one entry per long / strong tier too), minus everything the 1.7.0 port already
  places (list above).
- C. Tag via `global.tags` in a Creative Delight tags file (or the patcherMod data tag the dishes already use).
- D. patcherMod rebuild + install + restart; `/dumpplaceable`; `placeable_catalog.py`; `gen_coaster_models.py`.

1. **Item list (tool).** New `tools/gen_placed_drinks.py`, registered in [tools/generators.json](../../tools/generators.json):
   - Input: the `drink` type from `kubejs/generation_scripts/food/food_types.json` plus `kubejs/exported/food/drinks.json`
     (non-edible drinks, `/dumpfood`), minus items already in a DD map (`kubejs/exported/food/placeable.json`,
     `displayDelight`).
   - `EXCLUDE`: `canned_goods:*` (cans), Vinery bottles (own blocks), tankard drinks (`remainder` `brewinandchewin:tankard`,
     coaster only), `botania:*` flasks, `minecraft:milk_bucket`.
   - `MODEL_BY` rule: bottle → `apple_cider`, mug (coffee, hot chocolate) → `hot_cocoa`, milkshake / smoothie → `df_matcha_milkshake`.
   - Output: `kubejs/generation_scripts/food/placed_drinks.json` (block name → item_dict key, base model,
     liquid texture key). Block names are flat: `placed_<item path>`, plus the namespace when two mods share a path
     (`placed_rusticdelight_coffee` vs `placed_coffee`).
   - It also writes the assets: `kubejs/assets/creativeengineering/blockstates/placed_*.json` (4 facings, as DD's),
     `models/block/placed_*.json` (`{"parent": "displaydelight:block/apple_cider", "textures": {"6": "creativeengineering:block/placed_drink/<x>"}}`),
     `models/item/placed_*.json` (parent the block model), `kubejs/data/creativeengineering/loot_tables/blocks/placed_*.json`
     (copy of DD's apple_cider table with the drink id), lang `block.creativeengineering.placed_*` = the drink's name,
     and the liquid textures: DD's 16x16 liquid sprite tinted with the drink's colour, taken from the item sprite in
     its mod jar. `tools/potion_bucket_texture.py` and `tools/echo_texture.py` show how the pack reads and tints jar sprites.
   - Long / strong variants get their own block (one block ↔ one item in both maps). They share the base drink's texture.
   - `--check` mode like the other generators.
2. **Blocks (KubeJS startup).** New `kubejs/startup_scripts/items/placed_drinks.js`, reading the JSON with
   `JsonIO.readString` as [stack_sizes.js](../../kubejs/startup_scripts/changes/stack_sizes.js) does:
   ```js
   const $DrinkBlock = Java.loadClass('com.jkvin114.displaydelight.block.DrinkBlock')
   const $Props = Java.loadClass('net.minecraft.world.level.block.state.BlockBehaviour$Properties')
   const $Material = Java.loadClass('net.minecraft.world.level.material.Material')
   const $Sound = Java.loadClass('net.minecraft.world.level.block.SoundType')
   const $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
   const $ItemProps = Java.loadClass('net.minecraft.world.item.Item$Properties')
   var PLACED = {} // name -> Block, filled by the block suppliers
   StartupEvents.registry('block', e => names.forEach(n => e.createCustom('creativeengineering:' + n,
       () => (PLACED[n] = new $DrinkBlock($Props.of($Material.DECORATION).noOcclusion().instabreak().sound($Sound.GLASS))))))
   StartupEvents.registry('item', e => names.forEach(n => e.createCustom('creativeengineering:' + n,
       () => new $BlockItem(PLACED[n], new $ItemProps()))))
   ```
   Using DD's own class gives facing, the support check, the clone stack and the shape for free. Fallback, if Rhino
   refuses the class or the `Properties` chain: `e.create(id, 'cardinal').box(4,0,4,12,10,12).noCollision()...`. It
   then loses the support check and pick-block.
   Add block keys to [block_dict.js](../../kubejs/startup_scripts/_lib/block_dict.js) in a generated block, the way
   `placeable_catalog.py` writes its item_dict block (`b.placed_green_tea = 'creativeengineering:placed_green_tea'`).
3. **Links (KubeJS server).** New `kubejs/server_scripts/cooking/placed_drinks.js`:
   `ServerEvents.loaded` → `Java.loadClass('com.jkvin114.displaydelight.init.BlockAssociations').addToMap(item, block, false)`
   for each entry. Get `Item` / `Block` through `ForgeRegistries` as `dump_placeable.js` does, inside `guardThrowable`
   ([java_guard.js](../../kubejs/server_scripts/_lib/java_guard.js)). Log `[placed_drinks] linked N`.
   `putIfAbsent` makes repeat loads harmless.
4. **Tag.** Add every listed drink to `#displaydelight:displayable` with `global.tags.addTag` in
   Creative Delight's [placeable_tags.js](../../kubejs/data/creativedelight/kubejs/creativeengineering/placeable_tags.js)
   (the pack's placeable tags moved there). Add the key
   `i.t_dd_displayable = '#displaydelight:displayable'` to item_dict (tag-key style, next to `i.t_bnc_placeable_on_coaster`, line 1865).
5. **Tool follow-ups.**
   - [tools/placeable_catalog.py](../../tools/placeable_catalog.py) `EXCLUDE`: add `'creativeengineering:placed_*'`
     (display-block items, like `displaydelight:*`). The `Drink` in the `DrinkBlock` class name would otherwise put them in Review.
   - [tools/gen_coaster_models.py](../../tools/gen_coaster_models.py) `build()` (line 63) assumes every linked block is
     `displaydelight:` (`dd[kind][item_id].split(':', 1)[1]`, then a lookup in the jar). Make it read
     `kubejs/assets/<ns>/...` when the namespace is not `displaydelight`, so the drinks get coaster models.
6. **JEI.** Hide the `creativeengineering:placed_*` items in [jeichanges.js](../../kubejs/client_scripts/jeichanges.js)
   (`JEIEvents.hideItems`).
7. Restart (new blocks and items). `/dumpplaceable`, then `gen_coaster_models.py`, then `placeable_catalog.py`,
   then `/reload`.

### Ids and data
- DD classes and members above: verified in `displaydelight-1.2.0.jar` (Vineflower). `Material.DECORATION` and
  `SoundType.GLASS` in the sketch are Mojmap names. KubeJS remaps them in this build, but check at the first load.
  DD uses `Material.f_76320_` (unmapped).
- Drink ids: from [food_catalog.md](../food_catalog.md) `## drink (62)` and [stack_sizes.md](../stack_sizes.md) 1.2
  (dump of 2026-10-02). Recheck the non-edible list against a fresh `/dumpfood` `drinks.json`.
- New ids: `creativeengineering:placed_<x>` blocks and block items, keys `b.placed_<x>` (block_dict) and `i.t_dd_displayable`.

### Decisions
- **KubeJS `createCustom` with DD's `DrinkBlock`, or patcherMod Java?** Revised recommendation (2026-10-31): patcherMod,
  extending `DisplayDelightDishes` (it already registers DD blocks and links them; one list, one generator). KubeJS
  remains the fallback (the TODO says KubeJS; [plate_blocks.js](../../development/creativeEngineering/src/main/resources/kubejs_startup/creativeengineering/items/plate_blocks.js)
  shows plates went the other way). Owner to confirm the Work column.
- **Which drinks?** Recommend every glass-bottle and mug drink after the merges; skip cans and tankards.
- **Liquid colour:** generated from item sprites (recommended; a recolor from `food/drinks #2` flows through on rerun)
  or hand-painted.

### Gotchas
- **Pick-block and WTHIT icon on a dedicated server:** DD fills `BlockAssociations` only on the server, so the client's
  `getCloneItemStack` gets air. DD's own blocks behave the same. Test WTHIT with `WthitBlockModelIcon`
  (patcherMod compat).
- Sneaking with a DD-displayable item skips the Brewin' coaster (AL-52). Same for these drinks.
- Some drinks are always-edible: a plain right click still drinks them. Only sneak + right click on a block top places one.
- `DisplayEvents.on(LivingEntityUseItemEvent.Start)` cancel logic applies only to the plate tags, so it doesn't touch drinks.

### Tests
(PL-11 to PL-14 were proposed on 2026-10-21 and are not in the writeup; renamed to placeholders. Green Tea is placeable by
DD 1.7.0 now, so the examples use Peach Tea.)
- PL-new3 Place a drink (proposed, numbered when queued): sneak + right click the top of a block with Peach Tea -> a
  bottle block, one tea used; break it -> the tea drops. A plain right click drinks it.
- PL-new4 Tooltip (proposed): after the rerun, Peach Tea shows "Placeable (while sneaking)" and "Placeable on a coaster".
- PL-new5 Survival (proposed): break the block under a placed tea -> it pops off (`canSurvive`).
- PL-new6 Log (proposed): the dishes log line counts the new drink entries; no `[placeable]` warning.

---

## placeable #4: Dough blocks for every dough
Status **Planned** · Work: new jar / KubeJS (TODO; really KubeJS) · id `food/placeable #4`

### Goal
Every dough has a block; list the doughs that have none.

### Now (2026-10-31)
- Six pack dough blocks exist (`baking/baked-goods #27`, Testing; built 2026-10-25, shipped 1.1.03): Gingerdough, Cookie
  Dough, Sweet Potato Dough, Oat Dough, Cucumber Dough, Sourdough Blocks, registered in Creative Delight
  [baking_ingredients.js:18-36](../../development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/baking_ingredients.js#L18)
  (`material('sand')`, slime sounds, 0.5 / 10, hand-made models under `kubejs/assets/creativeengineering/`, textures
  `textures/block/dough_block/<id>.png` from `textureswapper/baking/dough/out/`); keys `i.gingerdough_block` ...
  `i.sourdough_block` (item_dict.js 1853-1858); recipes 9 <-> 1 in
  [creativeengineering/recipes.js:18](../../kubejs/data/creativedelight/kubejs/creativeengineering/recipes.js#L18).
  Create's dough (`create:dough`, `#forge:dough/wheat`) keeps Create Compressed's `create_compressed:dough_block`
  (`i.dough_block`, 202).
- Every dough item in the pack (`kubejs/exported/items_by_mod`, 2026-10-30): `create:dough`,
  `create_confectionery:gingerdough`, `creativeengineering:cookie_dough`, `veggiesdelight:sweet_potato_dough`,
  `vintagedelight:oat_dough`, `vintagedelight:cucumber_dough`, `pneumaticcraft:sourdough`, `delightful:nut_dough`,
  `farmersrespite:nether_wart_sourdough`, `veggiesdelight:uncooked_mhadjeb`; `farmersdelight:wheat_dough` is purged
  (`i.out_dough`, removed_items.js:139). Batters (`corn_delight:cornbread_batter`, `rusticdelight:batter`) are not doughs.
- **Without a block:** `delightful:nut_dough` (`i.nut_dough`, item_dict.js 2022; made 3 at a time from nut butter + eggs,
  delightful/recipes.js:69). Nether wart sourdough and uncooked mhadjeb were decided "no block" on 2026-10-25 (TODO
  `baking/baked-goods #27`).

### Build
1. Texture `kubejs/assets/creativeengineering/textures/block/dough_block/nut_dough_block.png` (recolour in
   `textureswapper/baking/dough/`, as the six were made), blockstate / block model / item model like
   `oat_dough_block`'s.
2. Key `i.nut_dough_block = 'creativeengineering:nut_dough_block'` next to line 1858; add
   `[i.nut_dough_block, 'Nut Dough Block']` to the `doughBlocks` list in baking_ingredients.js.
3. Recipes in creativeengineering/recipes.js "Dough blocks" (the same 9 <-> 1 helper calls the six use), storage and
   shovel tags as the six (`global.tags`).
4. Changelog bullet under the dough blocks family; oven.md 2.6 list.

### Decisions
- Which doughs: recommendation Nut Dough only (the rest are decided or are batters). If the owner wants none, close this
  row as covered by `baking/baked-goods #27`.

### Gotchas
- New block: restart, not `/reload` (startup registry; the file is in the CD jar, so CD rebuild + install).

### Tests
- OV-new1 (proposed, numbered when queued; oven.md prefix as OV-35): 9 Nut Dough -> Nut Dough Block -> 9; shovel mines it.

---

## placeable #5: Barbeque's Delight placeables: model recolor review
Status **Planned** · Work: resource pack · id `food/placeable #5`

### Goal
Decide which existing plated stick / wrap / sandwich models could be recoloured for Barbeque's Delight's 10 cooked
skewers and its 3 combos (Kebab Wrap, Kebab Sandwich, Bibimbap) before any display entry is made.

### Now
- Barbeque's Delight is not ported or installed: report only ([barbeques_delight.md](../barbeques_delight.md), 2026-10-30;
  port as a Creative Delight member, O10, `largecontent/mod-additions #37`). Owner decision O8 (2026-10-31): the mod has
  no display placeables, none are added now, this row reviews recolours (writeup 2.3 table and step 19).
- Candidate DD 1.7.0 models (blockstates in `development/backportedDisplay/src/main/resources/assets/displaydelight/blockstates/`;
  types from `tools/display_blocks.json`):
  - sticks: `plated_barbecue_stick` (FD Barbecue on a Stick, stackable 3), `edd_plated_end_barbecue_stick` (End's),
    `nd_plated_nether_skewer` (Nether's), `erd_plated_crispy_skewer` (Ender's, stackable 3),
    `df_plated_cooked_marshmallow_stick` (stackable 3);
  - wraps: `plated_mutton_wrap` (FD, stackable 2), `md_plated_vegan_wrap`, `df_plated_wrapped_cantaloupe`;
  - sandwiches: `plated_chicken_sandwich` / `plated_egg_sandwich` (stackable 2), `plated_bacon_sandwich` (plated);
  - bowls for Bibimbap: FD's placed rice dishes (pick one in the catalog).
- The pack's own DD blocks go through patcherMod `DisplayDelightDishes` (`bowl` / `plate` kinds only; see `#3`), so a
  stackable-plate kind would be new work there.

### Build
1. After the port: for each skewer and combo, pick a base model and the recolour (texture swap of the skewer meat and
   veg), list it in barbeques_delight.md as a table (item -> base block -> textures).
2. If the owner then wants them placed: entries in `displaydelight_dishes.json` with a new `stackable_plate` kind (or
   the existing `plate`), generated by `gen_dd_dishes.py`; tags via `global.tags`.

### Decisions
- Which models: recommendation FD's `plated_barbecue_stick` for all 10 skewers (stackable 3, same stick), FD's
  `plated_mutton_wrap` for the Kebab Wrap, `plated_chicken_sandwich` for the Kebab Sandwich, a placed rice bowl for
  Bibimbap.
- Plate or none: recommendation wait for the skewer family fold (`dishes/meals #46`) so the 18 skewers get one rule.

### Tests
- BBQ-new1 (proposed, BBQ- prefix of barbeques_delight.md, numbered when queued): a recoloured skewer model renders on
  its plate and stacks to 3.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| `food/placeable #1` Placeable tooltip lines and coaster line | Testing | [placeable_food.md](../placeable_food.md): 1.0.6 lines, coaster line + tag 1.0.8; open: PL-01 to PL-09 in game (also AL-91 to AL-93) |
| `food/placeable #2` (dump step) | dump done 2026-10-30 (CQ-DUMP-06) | The review pass itself is the In Progress section above; the 2026-10-30 dump is the one to read |

## Writeup corrections made

- [placeable_food.md](../placeable_food.md) status line: "catalog built from the 2026-10-03 `/dumpfood` export...;
  `/dumpplaceable` not run yet" → built from the live `/dumpplaceable` of 2026-10-18, rerun 2026-10-20.
- Same file, At a glance table: `/dumpplaceable` "not run" → "run 2026-10-18"; generator "run on the fallback data" → "run on the 2026-10-18 live dump".
- Same file, Counts line: fallback 164 / 130 / 73 / 29 / 11 → live 233 items, 132 placeable (101 sneak), 29, 11, 152 coaster.
- Same file, section 1 point 2: Vinery bottles "place on a plain right click" → place only while sneaking (backport 1.5.5 `DrinkBlockItem`, as the tool and catalog say).
- Same file, section 2 examples: Cherry Wine moved to its own row, "Placeable (while sneaking)".
- Same file, section 4 "Fallback data (until `/dumpplaceable` runs)" → used only when there is no export; live dump since 2026-10-18.
- Same file, PL-01 expected log line → `placeable=132, placeable/sneak=101, placeable_on_plate=29, placeable_on_small_plate=11, placeable_on_coaster=152`.
- Same file, PL-06: Cherry Wine expectation → "Placeable (while sneaking)".
