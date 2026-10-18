# Oven: a Farmer's Delight style baking block

The "new farmers delight oven block" from [cooking.md](cooking.md) (sweet > baked). Java in patcherMod, recipes and
tags in KubeJS. Test prefix `OV-`.

The ask, from cooking.md:

- works almost exactly like the cooking pot, but for baked goods
- a recipe can require an input tool (baking sheet, cake pan) and a plating item (paper for muffins)
- needs a non-stove heat source under it (magma block, campfire)
- gives stove heat to whatever sits on top

## 0. At a glance

| Part | Status |
|---|---|
| Oven block, block entity, menu/screen, `creativeengineering:baking` recipe type, JEI page (patcherMod `oven/`) | BUILT 2026-10-18: compiles against Forge 43.3.9 + JEI 11.8.1; 8 server GameTests pass with FD 1.2.4 loaded (6). Not built into a patcherMod jar yet, not tested in game (OV-01 to OV-14) |
| Bakeware items: baking sheet, cake pan, pie tin, muffin tin (KubeJS, IE iron plate texture, no durability) | BUILT 2026-10-18, not booted |
| Baking recipes: copies of the 7 baked-good pot recipes (Fruits Delight muffins, pies, tarts, cake). Pot recipes kept | BUILT 2026-10-18, not booted |
| Muffin pot recipes on `#forge:dough` | BUILT 2026-10-18, not booted. Whether this was the "not cookable" cause is unconfirmed (3.8) |
| Crafting recipes: oven, 4 bakeware | BUILT 2026-10-18 (placeholder patterns, 3.6) |
| Dummy baking recipe per bakeware piece (tool required; muffin tin also plates on paper) | BUILT 2026-10-18, not booted (2.7) |

Files to open first:

- `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/`: `OvenBlock`, `OvenBlockEntity`,
  `OvenHeat`, `OvenItemHandler`, `OvenMenu`, `OvenResultSlot`, `BakingRecipe`, `OvenRegistry`, `client/OvenScreen`,
  `jei/OvenJeiPlugin`, `jei/BakingRecipeCategory`
- [kubejs/server_scripts/cooking/baking.js](../kubejs/server_scripts/cooking/baking.js): baking and crafting recipes
- [kubejs/startup_scripts/cooking/bakeware.js](../kubejs/startup_scripts/cooking/bakeware.js): bakeware items
- `global.NCH.ceBaking` in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js)
- Tags: [blocktags.js](../kubejs/server_scripts/tags/blocktags.js) (heat), [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js) (bakeware)

Config knobs: none.

## 1. Overview: how the FD cooking pot and stove work (1.19.2-1.2.4, the pack's version)

Read from the 1.2.4 source, which matches the jar in `mods/`.

### 1.1 Cooking pot

- **Inventory** (`CookingPotBlockEntity`): 9 slots. 0-5 ingredients, 6 meal display (the finished meal waits
  here), 7 container input (bowls and so on), 8 output.
- **Recipe** `farmersdelight:cooking` (`CookingPotRecipe`): shapeless, 1 to 6 `ingredients`, `result`, optional
  `container`, `experience` (default 0), `cookingtime` (default 200 ticks), optional `recipe_book_tab`. If
  `container` is missing, the result's crafting remainder is used (a bowl for stews).
- **Tick** (`cookingTick`): while heated and holding ingredients, it looks up the recipe (the last recipe is cached,
  and a new search runs only after the inventory changes), counts `cookTime` up to `cookingtime`, then takes one
  of each ingredient. Ingredient remainders (buckets, bottles) are ejected toward the pot's left side. The result
  is added to the meal slot. When it isn't heated, progress counts down 2 per tick.
- **Plating:** when the meal needs a container, it stays in slot 6 until a matching container is in slot 7 (or a
  player right-clicks with one). It then moves to output, one meal per container. A meal that needs no container
  goes to output straight away. **The "plating item" in the oven ask is this same mechanism:** paper for muffins
  is just `"container": {"item": "minecraft:paper"}`.
- **Automation** (`CookingPotItemHandler`): the top face inserts into and extracts from the 6 ingredient slots.
  The sides and bottom insert into the container slot and extract from the output slot.
- **Item form:** a broken pot keeps the meal (with its container) in NBT and serves from the hand.
- **Heat** (`HeatableBlockEntity.isHeated`): the block below is in `#farmersdelight:heat_sources`. A block that has
  a `lit` property counts only while `lit=true`. A `#farmersdelight:heat_conductors` block (hopper, Create chute)
  in between passes heat up from a source one block lower.
- **Tray model:** a pot on a `#farmersdelight:tray_heat_sources` block (fire, campfires, lava) shows a tray under
  it. This only changes the model.

### 1.2 Stove

- `StoveBlock`: `facing` and `lit` states, bricks properties (hardness 2, resistance 6, pickaxe), light 13 when lit.
  It is placed lit. A shovel or a water splash puts it out; flint and steel or a fire charge lights it again. It
  burns entities that step on it while lit, and mobs path around it as `DAMAGE_FIRE`.
- `StoveBlockEntity` is the 6-slot grill on top (campfire recipes). That grill isn't part of the oven ask.
- The stove is in `#farmersdelight:heat_sources`, so it heats whatever sits on it while lit.

### 1.3 Heat sources in this pack

Full audit: [alcohol.md](alcohol.md) "Heat sources today" (38 blocks) and "Missing heat sources". The groups:
fire, campfires (lit-gated), lava / magma / lava cauldron, **stoves and ovens** (`farmersdelight:stove`, Nether's
Delight blackstone stove / furnaces / smoker, `endersdelight:endstone_stove`, `create_central_kitchen:blaze_stove`,
`vinery:stove`, `vinery:wood_fired_oven`), blaze (`create:blaze_burner`, `create:lit_blaze_burner`,
`quark:blaze_lantern`) and the Brewin port's `heating_cask` / `fiery_fondue_pot`. The pack's scripts don't add to
or remove from `#farmersdelight:heat_sources` today.

### 1.4 Existing "ovens" that don't fit

- `cookingforblockheads:oven`: a fuel-burning furnace that runs smelting recipes. It has no recipe type of its own,
  no tools, no plating and no heat rule.
- `vinery:wood_fired_oven`, `vinery:stove`: heat sources only, and Vinery 1.5.5 removes both ([alcohol.md](alcohol.md) I21).
- `handcrafted:oven`, `fantasyfurniture:*/oven`: decoration.

None of them does what the ask needs, so this is a new block.

## 2. Reference

### 2.1 Block

| | |
|---|---|
| Id | `creativeengineering:oven` (`i.oven`, `b.oven`), registered by patcherMod `OvenRegistry` |
| States | `facing` (front faces the player on placement) x `lit`. `lit` = heated from below, mirrored every server tick by the block entity |
| Stats | FD stove's (`Properties.copy(Blocks.BRICKS)`): hardness 2, resistance 6, needs a pickaxe; light 13 while lit |
| Models | `creativeengineering:block/oven` / `oven_on` in the patcherMod jar: `orientable_with_bottom` on `farmersdelight:block/stove_*` (placeholder art) |
| Item tab | Decorations |
| Loot | drops itself (patcherMod `loot_tables/blocks/oven.json`); breaking it also drops every slot, the waiting result included |
| Crafting | `PPP / B B / BBB`, P = `#forge:plates/iron`, B = bricks (`creativeengineering:shaped/oven`) |

### 2.2 Recipe type `creativeengineering:baking`

FD's cooking JSON plus an optional `tool`:

```json
{
  "type": "creativeengineering:baking",
  "ingredients": [ { "tag": "forge:fruits/fig" }, { "item": "farmersdelight:pie_crust" }, { "tag": "forge:eggs" } ],
  "tool": { "tag": "creativeengineering:bakeware/pie_tin" },
  "container": { "item": "minecraft:paper" },
  "result": { "item": "fruitsdelight:fig_tart", "count": 2 },
  "experience": 0.1,
  "cookingtime": 200
}
```

| Field | Meaning |
|---|---|
| `ingredients` | 1-6, shapeless (FD matching: tags, any order, no extra items) |
| `tool` | optional ingredient that must sit in the tool slot. Never used up, no durability. A recipe without `tool` ignores the tool slot |
| `container` | optional plating item (FD semantics: the result waits until one is given). Defaults to the result's crafting remainder |
| `result`, `experience`, `cookingtime` | as FD (`cookingtime` default 200, `experience` default 0) |

KubeJS: `R.ceBaking(inputs, output, e, idOverride, { exp, time, container, tool })`, id
`creativeengineering:baking/<id>`.

### 2.3 Slots

| Slot | Role | GUI position |
|---|---|---|
| 0-5 | ingredients | FD's 3x2 grid |
| 6 | tool (only `#creativeengineering:bakeware`, stack limit 1) | new slot left of the grid |
| 7 | result waiting for plating (display only) | FD's meal slot |
| 8 | plating container | FD's bowl slot |
| 9 | output (take only; taking awards recipe xp) | FD's output slot |

### 2.4 Tags

| Tag | Members | Where |
|---|---|---|
| `#creativeengineering:oven_heat_sources` (block) | `#farmersdelight:tray_heat_sources` (fire, campfires, lava; nested), `magma_block`, `lava_cauldron`, `create:blaze_burner`, `create:lit_blaze_burner`, `quark:blaze_lantern` | blocktags.js |
| `#farmersdelight:heat_sources` (block) | `+ creativeengineering:oven` | blocktags.js |
| `minecraft:mineable/pickaxe` (block) | `+ creativeengineering:oven` | blocktags.js |
| `#creativeengineering:bakeware/{baking_sheet,cake_pan,pie_tin,muffin_tin}` (item) | the matching item | cookingtags.js |
| `#creativeengineering:bakeware` (item) | the four kind tags | cookingtags.js |

### 2.5 Bakeware

| Item | Recipe (P iron plate, N iron nugget) |
|---|---|
| `creativeengineering:baking_sheet` | `NPN` |
| `creativeengineering:pie_tin` | `N N / _P_` |
| `creativeengineering:cake_pan` | `P P / _P_` |
| `creativeengineering:muffin_tin` | `NNN / PPP` |

All four use `immersiveengineering:item/metal_plate_iron` (IE's iron plate, the pack's `i.plate_iron`) as the texture,
stack to 64 and have no durability.

### 2.6 Baking recipes (copies of the pot recipes)

| Id `creativeengineering:baking/...` | Ingredients | Result | Container |
|---|---|---|---|
| `blueberry_muffin` | blueberry x2, `#forge:dough`, `#forge:milk/milk_bottle`, `#forge:eggs`, `#forge:sugar` | 2 | none |
| `cranberry_muffin` | cranberry x2, same | 2 | none |
| `durian_pie` | durian flesh x2, pie crust, egg, sugar | 2 | none |
| `pineapple_pie` | pineapple slice x2, pie crust, egg, sugar | 2 | none |
| `lemon_tart` | lemon, pie crust, egg, sugar, milk bottle | 2 | none |
| `fig_tart` | fig, pie crust, egg, sugar, milk bottle | 2 | none |
| `mangosteen_cake` | wheat x2, sugar x2, mangosteen x2 | 1 | bowl |

All 200 ticks, 0.1 xp, no tool. Source: Fruits Delight 0.5.11 `data/farmersdelight/recipes/cooking/*.json`. These are
the only `farmersdelight:cooking` recipes with a baked output across the 27 food mods scanned (FD, its add-ons,
Brewin, Respite, Neapolitan, Abnormals Delight, Central Kitchen, Create Confectionery/Factory, ...); the scan didn't
cover every mod in the pack. Sugar is `#forge:sugar`, as the pack-wide sugar swap in cooking.js gives every pot recipe.

### 2.7 Dummy bakeware recipes

Placeholders, one per bakeware piece, so the tool slot is used and testable. Each needs its tool, which stays in
the slot. Their ingredient counts differ from every recipe in 2.6, so they never compete. Replace them with real
bakeware recipes later.

| Id `creativeengineering:baking/...` | Ingredients | Tool | Result | Container |
|---|---|---|---|---|
| `dummy_baking_sheet` | `#forge:dough` | baking sheet | bread | none |
| `dummy_pie_tin` | pie crust, apple | pie tin | FD apple pie | none |
| `dummy_cake_pan` | `#forge:dough`, `#forge:sugar`, `#forge:eggs` | cake pan | cake | none |
| `dummy_muffin_tin` | blueberry, `#forge:dough` | muffin tin | 1 blueberry muffin | paper |

All 200 ticks, 0.1 xp.

## 3. What the pack does, and why

### 3.1 Heat (`OvenHeat`)

- Heated when the block below is in `#creativeengineering:oven_heat_sources` and active, or a
  `#farmersdelight:heat_conductors` block (hopper, Create chute) sits between the oven and such a source.
- Active: a block with `lit` counts only while lit (FD's rule); a Create Blaze Burner whose `blaze` heat level is
  `none` doesn't count (the Brewin port's rule, matched by property name so no Create classes are needed).
- Every stove and oven is left out of the tag, so the oven can't sit on a stove or on another oven.

### 3.2 Stove heat on top

The oven is in `#farmersdelight:heat_sources`, and FD's `HeatableBlockEntity` honours `lit`, so a cooking pot, skillet
or kettle on the oven heats only while the oven itself is heated. Proven by GameTest OV-T5.

### 3.3 Baking and plating

The tick loop is FD's: progress counts up while heated and a recipe matches, counts down 2 per tick while cold. Each
bake takes one of each ingredient, ejects remainders (buckets, bottles) out of the front, and adds the result to
slot 7. A result without a container moves to output; one with a container waits for it (slot 8, or right-click the
oven with it).

### 3.4 Tool slot

Recipes can ask for bakeware; only the dummy recipes do so far (2.7). The tool is never used up. Filter: `#creativeengineering:bakeware`.

### 3.5 Automation (`OvenItemHandler`), the same on every face

- Insert: bakeware to the tool slot; the plating item (what the waiting result needs, or the last recipe's
  container) to slot 8; anything else to the ingredient slots.
- Extract: output slot only.
- Limit: an item that is the last recipe's plating item can't be inserted as an ingredient by automation.

### 3.6 Changes from FD's pot

No recipe book (it needs a `RecipeBookType` enum extension), no meal kept in the item when broken, no custom name,
remainders eject from the front instead of the left. The GUI draws FD's own `cooking_pot.png` by resource location
(including its pot picture behind the result slot) plus a copied slot frame for the tool; JEI does the same.

### 3.7 Lang

`block.creativeengineering.oven`, `container.creativeengineering.oven[.heated|.not_heated|.served_on]`,
`jei.creativeengineering.baking[.heat]` in the patcherMod `en_us.json`. Bakeware names come from KubeJS `displayName`.

### 3.8 Muffin pot recipes

`farmersdelight:cooking/blueberry_muffin` and `cranberry_muffin` (Fruits Delight) are replaced by
`creativeengineering:farmersdelightcooking/blueberry_muffin` / `cranberry_muffin`, the same except `#forge:dough`
instead of `#forge:dough/wheat` (and `#forge:sugar`).

Correction to the first version of this writeup: it guessed that `#forge:dough/wheat` was empty. Create 0.5.1.i adds
`create:dough` to both `#forge:dough/wheat` and `#forge:dough`, so the tag isn't empty from that. No other scanned jar
overrides the muffin recipe ids. If muffins still can't be cooked in game, the cause is somewhere else (OV-13).

## 4. Limits and decisions

- **Route:** Java in patcherMod (user, 2026-10-18). A KubeJS block entity was rejected: chest GUI only, no item
  capability for hoppers, no recipe type.
- **No durability** on bakeware (user, 2026-10-18).
- **Recipes identical to the pot's for now, pot recipes stay** (user, 2026-10-18). Adding tools, plating (paper for
  muffins) and moving recipes off the pot are later steps.
- **Heat conductors allowed** (as FD). **Furnaces aren't oven heat** (stoves-and-ovens group). Brewin's
  `heating_cask` isn't in the tag: the pack's Brewin 2.0 jar doesn't have that block.
- Crafting patterns for the oven and bakeware are placeholders; not checked for clashes with every mod's recipes.
- FD's `SkilletItem` held near a heat source ignores `lit`, so a cold oven counts for it (same as a cold stove).
- Brewin kegs count a lit oven within range as heat.

## 5. File map

| File | What |
|---|---|
| `development/patcherMod/.../oven/OvenRegistry.java` | block, item, block entity, menu, recipe type + serializer |
| `.../oven/OvenBlock.java` | states, use (plate or open), drops, burn on step, particles, comparator |
| `.../oven/OvenBlockEntity.java` | inventory, tick (heat, bake, plate), xp, save/sync, capability |
| `.../oven/OvenHeat.java` | heat rule and tags |
| `.../oven/OvenItemHandler.java` | automation routing, `BAKEWARE` tag |
| `.../oven/OvenMenu.java`, `OvenResultSlot.java` | menu layout, shift-click routing, output slot |
| `.../oven/BakingRecipe.java` | recipe + JSON/network serializer |
| `.../oven/client/OvenScreen.java` | GUI on FD's texture |
| `.../oven/jei/OvenJeiPlugin.java`, `BakingRecipeCategory.java` | JEI page, catalyst, click area, transfer |
| `.../CreativeEngineering.java` | `OvenRegistry.register`, `MenuScreens.register` |
| `development/patcherMod/src/main/resources/assets/creativeengineering/{blockstates,models/block,models/item}/oven*.json`, `lang/en_us.json` | models, names |
| `development/patcherMod/src/main/resources/data/creativeengineering/loot_tables/blocks/oven.json` | drop |
| `kubejs/startup_scripts/cooking/bakeware.js` | bakeware items |
| `kubejs/server_scripts/cooking/baking.js` | baking + crafting recipes |
| `kubejs/server_scripts/cooking/cooking.js` "Muffins" | the dough fix |
| `kubejs/server_scripts/tags/blocktags.js`, `cookingtags.js` | tags (2.4) |
| `kubejs/startup_scripts/func/neoCraftHelper.js` | `ceBaking` |
| `kubejs/startup_scripts/_lib/item_dict.js`, `block_dict.js` | ids and tag keys |

## 6. Offline checks

Done 2026-10-18 in a scratch ForgeGradle project (Forge 43.3.9, official mappings, JEI 11.8.1.1034 compile-only,
FD 1.2.4 at runtime) holding the `oven/` package and a stand-in main class. The patcherMod jar itself wasn't built: it
needs the ~50 third-party jars in `development/patcherMod/libs/`.

- `compileJava`: clean (the two deprecation warnings are suppressed as in FD).
- `runGameTestServer`, test datapack (magma/campfire/lava heat tag, iron ingot as bakeware, three test recipes):
  **all 8 pass**.

| Test | Proves |
|---|---|
| OV-T1 `magmaBakes` | magma heats, `lit` follows, a recipe bakes 2 results, output extracts, ingredients don't |
| OV-T2 `stoveIsNotHeat` | a lit FD stove below gives no heat, nothing bakes |
| OV-T3 `campfireLitGate` | unlit campfire no, lit campfire yes, heat removed -> `lit` false |
| OV-T4 `heatThroughHopper` | magma -> hopper -> oven is heated |
| OV-T5 `potOnTop` | FD cooking pot on a cold oven isn't heated, on a hot oven is |
| OV-T6 `platingWithPaper` | a result with a container waits; paper from a hopper goes to slot 8 and plates it |
| OV-T7 `toolRequiredNotConsumed` | no bake without the tool; bakeware goes to the tool slot (one only); bake works and the tool stays |
| OV-T8 `breakDropsContents` | breaking drops the inventory |

The test sources are scratch-only (not in the repo).

## 7. In-game test suite

Needs a patcherMod build in `mods/`.

- [ ] **OV-01 Boot:** no registry, recipe or tag errors for `creativeengineering:oven`, `creativeengineering:baking/*`,
  bakeware and the oven tags in `latest.log` / KubeJS logs.
- [ ] **OV-02 Look:** placed oven faces you, looks like an unlit FD stove; lit state shows the lit stove front/top.
- [ ] **OV-03 Craft:** oven and the four bakeware recipes show in JEI and craft. Bakeware has the IE iron plate icon.
- [ ] **OV-04 Heat sources:** lit campfire, fire, magma, lava, blaze burner with fuel -> lit; unlit campfire, empty
  blaze burner, FD stove, Nether's Delight blackstone stove, another oven -> not lit.
- [ ] **OV-05 GUI:** right-click opens "Oven": FD pot layout, tool slot left of the grid, flame shows heat, arrow
  fills, heat tooltip reads right.
- [ ] **OV-06 Bake:** each of the 7 recipes in 2.6 bakes in the oven and still cooks in the pot.
- [ ] **OV-07 Mangosteen cake plating:** the cake waits for a bowl; right-click with a bowl serves one.
- [ ] **OV-08 Pot on top:** campfire -> oven -> cooking pot: the pot cooks; put out the campfire: the pot stops.
- [ ] **OV-09 Tool slot:** only bakeware goes in, one at a time; shift-click routes it there.
- [ ] **OV-10 Automation:** a hopper or Create funnel on a side fills ingredients; another extracts only output.
- [ ] **OV-11 JEI:** "Baking" page lists the 7 recipes with the oven as catalyst; clicking the GUI arrow opens it;
  "+" moves ingredients in.
- [ ] **OV-12 Break / burn:** breaking drops oven and contents; standing on a lit oven hurts unless sneaking.
- [ ] **OV-13 Muffins:** blueberry and cranberry muffins cook in the pot with Create dough. If not, record which
  ingredient slot JEI shows empty.
- [ ] **OV-15 Dummy bakeware recipes:** each 2.7 recipe bakes only with its bakeware in the tool slot, the tool
  stays afterwards, and the dummy muffin waits for paper. JEI shows the tool left of the grid.
- [ ] **OV-14 Server:** the same on a dedicated server with a client (menu opens, slots sync, JEI page present).

## 8. History

- 2026-10-18: written as a plan with a KubeJS placeholder block on the FD stove textures.
- 2026-10-18: built. Java oven in patcherMod (route A), placeholder registration removed and its models moved into the
  jar. Bakeware items (IE iron plate texture, no durability), tags, `NCH.ceBaking`, 7 baking recipes copied from the
  pot, oven and bakeware crafting recipes, muffin pot recipes on `#forge:dough`. Dough lead corrected (3.8). Compiles;
  8 GameTests pass.
- 2026-10-18: one dummy baking recipe per bakeware piece (2.7).
