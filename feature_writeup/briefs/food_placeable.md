# Brief: food / placeable

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `placeable`. Feature writeup:
[placeable_food.md](../placeable_food.md) (test prefix `PL-`), generated catalog [placeable_catalog.md](../placeable_catalog.md).

## Scope and order

| Row | Status | Size | Build with |
|---|---|---|---|
| `food/placeable #2` Run `/dumpplaceable` | Planned (mostly done) | small, tool only | first, on its own |
| `food/placeable #3` Drinks placeable | Planned | medium: tool + KubeJS startup/server + assets | together with `food/drinks #2` (its "recolor teas and make them placeable" half is this same work) |

Order:
1. **#2** first. The dump it asks for already ran (see below). What is left is a review pass in the tool, which
   gives a clean catalog to diff #3 against.
2. **Decide the drink merges before #3** (`food/drinks #3`: Rustic Delight vs Farmer's Respite coffee, the two milk
   bottles, the FD / Farmer's Respite apple ciders, melon juice). A drink that gets merged away needs no display
   block. #3 reads its item list from rules, so building it before the merges only means rerunning the tool afterwards.
3. **#3**, then rerun `tools/gen_coaster_models.py` and `tools/placeable_catalog.py`. The new display models also
   give the drinks their coaster look back (`alcohol/brewinport #8`, Testing).

---

## placeable #2: Run `/dumpplaceable`
Status **Planned** (TODO). Really **In Progress**: the dump ran on 2026-10-18 · Work: tool · id `food/placeable #2`

### Goal
Build the catalog and the five placeable tags from the live Display Delight (DD) links, not the fallback data. Move
the real foods in the catalog's "Review" list into `INCLUDE`.

### Now
- The dump has been run. [placeable_catalog.md](../placeable_catalog.md) line 5 says `Source: **/dumpplaceable (live)**`.
  The writeup history has "2026-10-18 ... Rerun on today's live `/dumpplaceable`: 269 items" and a rerun on
  2026-10-20. [placeable.json](../../kubejs/generation_scripts/food/placeable.json) holds placeable 132,
  placeable/sneak 101, plate 29, small plate 11, coaster 152 (counted 2026-10-21).
- The export `kubejs/exported/food/placeable.json` is git-ignored (`.gitignore:53` `/kubejs/exported/`), so it is not
  in this checkout. The tool can only rerun on the game instance, which has the export and the `mods/` jars.
  `load()` ([tools/placeable_catalog.py:170](../../tools/placeable_catalog.py)) always calls `dd_from_jar()`, which needs
  `mods/displaydelight-*.jar`, even in dump mode.
- The "Review" section ([placeable_catalog.md:372](../placeable_catalog.md)) is about 150 rows, nearly all noise from the
  wide regex `FOOD_BLOCK_WIDE` ([placeable_catalog.py:67](../../tools/placeable_catalog.py)):
  - `Ham` matches `HammockBlock` (16 `comforts:hammock_*`).
  - `Wheel` matches cogwheels, crushing wheels and water wheels (`create:*`, `copycats:*`, `design_decor:*_crushing_wheel`).
  - `Rice` matches `farmersdelight:rice`, `rice_bale` and `wild_rice`.
  - `Jelly` / `Jam` match the Fruits Delight `*_jelly_block` / `*_jello_cauldron` / `*_jelly_cauldron`,
    `upgrade_aquatic:*_jelly_torch` and `mulberry_jam_block`, and `create_central_kitchen:mulberry_jam_bucket`.
  - `Wine` / `Bottle` match the Vinery wine racks and `wine_box`.
  - `Bread` matches `architects_palette:bread_block`.
- Real food blocks in the list: `brewinandchewin:flaxen_cheese_wheel` and `brewinandchewin:scarlet_cheese_wheel`
  (`CheeseWheelBlock`: placed, then cut into wedges with a knife). Their unripe versions (`UnripeCheeseWheelBlock`) are
  placed to age; they are not food yet.
- "Display Delight gaps" says None for both directions ([placeable_catalog.md:355](../placeable_catalog.md)).

### Build
1. In [tools/placeable_catalog.py](../../tools/placeable_catalog.py):
   - `INCLUDE` (line 86): add
     `'brewinandchewin:flaxen_cheese_wheel': 'cheese wheel block, cut with a knife into wedges'` and the scarlet one.
   - Make `FOOD_BLOCK_WIDE` (line 67) narrower so that Review only lists open questions: `Ham` → `Ham(?!mock)`,
     `Wheel` → `CheeseWheel`. Keep `Rice` and `Medley`: `RiceRollMedleyBlock` and `PineappleRiceBlock` are tagged
     today, and the wide regex is only the Review net, but don't narrow it blindly. Add a `REVIEW_IGNORE` regex for the rest:
     `^(farmersdelight:(rice|rice_bale|wild_rice)|fruitsdelight:\w+_(jelly_block|jello_cauldron|jelly_cauldron)|upgrade_aquatic:\w+_jelly_torch|vinery:\w*wine_(rack|box)\w*|architects_palette:bread_block)$`
     and apply it in `review_candidates()` (line 290). Don't put these in `EXCLUDE`: that would list ~120 rows under
     "Excluded" with a reason each.
   - Leave `upgrade_aquatic:mulberry_jam_block` and `create_central_kitchen:mulberry_jam_bucket` in Review for one pass
     (check in game whether the jam block is edible; it is a decor block in Upgrade Aquatic).
2. On the game instance: `/dumpplaceable` (only needed if mods changed since 2026-10-20; 1.0.9 added no food mods,
   [plan_1.0.9.md](../plan_1.0.9.md)), then `python3 tools/placeable_catalog.py`, then `--check`.
3. Expected change: `placeable` 132 → 134 (the two cheese wheels, plain right click: `BlockItem`, not sneak). Update the
   counts in [placeable_food.md](../placeable_food.md) section 0 and PL-01.
4. Close the row (Complete once PL-09 passes).

### Ids and data
- `brewinandchewin:flaxen_cheese_wheel` and `scarlet_cheese_wheel`: in-tree Brewin' port,
  `development/backportedBrewin/.../common/registry/BnCItems.java:57,60` (`BlockItem`, stack 16; verified in source).
  The tool adds item_dict keys for them if they have none (they already have keys from the Brewin' sections).
- Tags: unchanged (`#creativeengineering:placeable` and the others via `i.t_placeable*`).

### Decisions
- Ripe cheese wheels only, or the unripe ones too? **Recommend ripe only.** An unripe wheel is not food, and the line
  would suggest it can be eaten.

### Gotchas
- Don't run the tool without the export: it falls back to `/dumpfood` and silently rebuilds from fallback data (the
  catalog's source line then says `/dumpfood`). `--check` compares only `placeable.json`.
- `tools/gen_coaster_models.py` imports `placeable_catalog` (`pc.load()`), so changes to `load()` reach it too.

### Tests
- Existing PL-01 (counts updated by this brief's writeup edit) and PL-09. New: **PL-10 Cheese wheel:** hover a Flaxen
  Cheese Wheel → "Placeable"; place it and cut it with a knife.

---

## placeable #3: Drinks placeable
Status **Planned** · Work: KubeJS (+ tool, assets) · id `food/placeable #3` · overlaps `food/drinks #2`

### Goal
Bottled FD-style drinks with no place-down today ("FD tea-like": Farmer's Respite teas and coffee, Fruits Delight
juices and teas, Delightful teas, Neapolitan milkshakes, Rustic Delight coffees and similar) go down the way FD's
Apple Cider does through Display Delight: sneak + right click the top of a block places a small bottle block, and
breaking it gives the drink back.

### Now
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
   [placeabletags.js](../../kubejs/server_scripts/tags/placeabletags.js) or a new `tags/placed_drinks_tags.js`. Add the key
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
- **KubeJS `createCustom` with DD's `DrinkBlock`, or patcherMod Java?** Recommend KubeJS (row says KubeJS; the plate
  blocks moved from Java to KubeJS, [plate_blocks.js](../../kubejs/startup_scripts/items/plate_blocks.js) header). Use
  patcherMod (`CreativeEngineering.BLOCKS` DeferredRegister + a `LevelEvent.Load` listener, DD jar in `libs/`) only if
  Rhino can't build the `Properties` chain.
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
- **PL-11 Place a tea:** sneak + right click the top of a block with Green Tea → a bottle block, one tea used; break
  it → the tea drops. A plain right click drinks it.
- **PL-12 Tooltip:** after the rerun, Green Tea shows "Placeable (while sneaking)" and "Placeable on a coaster".
- **PL-13 Survival:** break the block under a placed tea → it pops off (`canSurvive`). With the `cardinal` fallback this fails; note it.
- **PL-14 Log:** `[placed_drinks] linked N` with N = the JSON's count; no `[placeable]` warning.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| `food/placeable #1` Placeable tooltip lines and coaster line | Testing | PL-01 and PL-06 expected values were stale (fixed in the writeup, below). The coaster tag is also tested in alcohol.md AL-91 to AL-93 |

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
