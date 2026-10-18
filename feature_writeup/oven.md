# Oven: a Farmer's Delight style baking block

Plan and feasibility study for the "new farmers delight oven block" item in [cooking.md](cooking.md)
(sweet > baked). Only a placeholder block exists so far. Test prefix `OV-`.

The ask, from cooking.md:

- works almost exactly like the cooking pot, but for baked goods
- a recipe can require an input tool (baking sheet, cake pan) and a plating item (paper for muffins)
- needs a non-stove heat source under it (magma block, campfire)
- gives stove heat to whatever sits on top

## 0. At a glance

| Part | Status |
|---|---|
| Placeholder block `creativeengineering:oven` (KubeJS `cardinal` block with a `lit` state, FD stove textures, no function, no crafting recipe) | BUILT 2026-10-18, not booted |
| Working oven (block entity, GUI, baking recipe type, heat rule, stove heat on top, JEI) | not started. Recommended route: Java in patcherMod (3, 4.1) |
| Bakeware items (baking sheet, cake pan, pie tin, muffin tin) | not started (3.4) |
| First baking recipes (muffins, pies, cakes, tarts, bread) | not started (3.7) |

Files to open first:

- [kubejs/startup_scripts/cooking/oven.js](../kubejs/startup_scripts/cooking/oven.js): the placeholder registration
- `kubejs/assets/creativeengineering/blockstates/oven.json`, `models/block/oven.json`, `models/block/oven_on.json`,
  `models/item/oven.json`: hand-written models that point at `farmersdelight:block/stove_*`
- Farmer's Delight 1.2.4 source (branch `1.19` of vectorwing/FarmersDelight, MIT): `CookingPotBlock`,
  `CookingPotBlockEntity`, `CookingPotMenu`, `CookingPotScreen`, `CookingPotRecipe`, `HeatableBlockEntity`,
  `StoveBlock`, `integration/jei/category/CookingRecipeCategory`. The oven is mostly a copy of these with a few changes.

Config knobs: none yet. 3.8 lists the ones the Java oven would add.

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

### 2.1 Placeholder block (built)

| | |
|---|---|
| Id | `creativeengineering:oven` (`global.item.oven`) |
| Type | KubeJS `cardinal` (`HorizontalDirectionalBlockBuilder`): `facing` faces the player on placement, like the stove |
| States | `facing` (4) x `lit` (`BlockProperties.LIT`, default `false`). Nothing sets `lit=true` yet |
| Stats | FD stove's: stone material and sound, hardness 2, resistance 6, needs a pickaxe (`minecraft:mineable/pickaxe`) |
| Models | `creativeengineering:block/oven` (lit=false) and `oven_on` (lit=true), `orientable_with_bottom` with `farmersdelight:block/stove_{front,front_on,side,top,top_on,bottom}`. The item model uses `block/oven` |
| Recipe / tags | none. Get it from creative or JEI. It isn't a heat source and has no block entity |

The `lit` state is there now so the placeholder's blockstate already matches the planned block (3.1). Placed
placeholders then keep their state when the Java block replaces the registration under the same id.

### 2.2 Planned recipe type `creativeengineering:baking`

The FD cooking JSON with one more field, `tool`:

```json
{
  "type": "creativeengineering:baking",
  "ingredients": [
    { "tag": "forge:fruits/blueberry" }, { "tag": "forge:dough/wheat" },
    { "tag": "forge:milk/milk_bottle" }, { "tag": "forge:eggs" }, { "item": "minecraft:sugar" }
  ],
  "tool": { "tag": "creativeengineering:bakeware/muffin_tin" },
  "container": { "item": "minecraft:paper" },
  "result": { "item": "fruitsdelight:blueberry_muffin", "count": 4 },
  "experience": 0.35,
  "cookingtime": 200
}
```

| Field | Meaning |
|---|---|
| `ingredients` | 1-6, shapeless. Matched like FD's (`RecipeMatcher`), so tags and any order work |
| `tool` | optional. An ingredient that must be in the tool slot. It isn't used up: it loses `tool_damage` durability per bake, or nothing if it has no durability |
| `tool_damage` | optional, default 1 |
| `container` | optional plating item (FD semantics, 1.1). It defaults to the result's crafting remainder |
| `result`, `experience`, `cookingtime` | as FD (`cookingtime` default 200) |

### 2.3 Planned tags

| Tag | Members | Use |
|---|---|---|
| `#creativeengineering:oven_heat_sources` (block) | `#farmersdelight:tray_heat_sources` (fire, campfires, lava, nested so it stays live), `minecraft:magma_block`, `minecraft:lava_cauldron`, `create:blaze_burner`, `create:lit_blaze_burner`; decide on `quark:blaze_lantern` and the Brewin `heating_cask` (D3) | what the oven accepts from below. Every stove and oven is left out on purpose (the "non-stove" rule) |
| `#farmersdelight:heat_sources` (block) | `+ creativeengineering:oven` | gives stove heat on top. FD's `lit` gate means only a heated oven counts |
| `#creativeengineering:bakeware/{baking_sheet,cake_pan,pie_tin,muffin_tin}` (item) | the bakeware items (3.4) | `tool` ingredients. Tags let other mods' pans join later |
| `#creativeengineering:bakeware` (item) | all of the above | tool-slot filter and hopper routing (3.3) |

Both block tags go through `global.tags` in `kubejs/server_scripts/tags/blocktags.js`, as CLAUDE.md asks.

## 3. Design: what it would take

### 3.1 Block (`OvenBlock`, Java)

- `BaseEntityBlock` with `facing` and `lit`. It copies the stove's properties, with light 13 while lit, like
  `litBlockEmission(13)`.
- **`lit` means "heated from below"**, not "switched on". Two things recompute it: `updateShape` when the block
  below changes (a campfire being lit or put out is a state change, so its neighbours get the update), and the
  block entity's server tick as a backstop every 20 ticks. There's no flint-and-steel or shovel handling: the oven
  has no fire of its own.
- Heat rule: the block below is in `#creativeengineering:oven_heat_sources` and is active. Active means `lit=true`
  if the block has `lit`, and a Create blaze burner with `blaze` not `none` (the same rule as
  `BnCHeatSources.withoutIdleBurner` in the Brewin port, matched by property name, so it needs no Create classes).
  Heat conductors work as in FD (hopper or chute between the oven and the source) unless D4 says otherwise.
- **Stove heat on top:** add the oven to `#farmersdelight:heat_sources` (2.3). FD's `HeatableBlockEntity` already
  respects `lit`, so a cooking pot, skillet or kettle on an oven works only while the oven itself is heated. The
  stack is campfire -> oven -> cooking pot. The oven is a full block, so the pot sits on it flat with no tray.
- `stepOn` burns entities while lit, like the stove (D6). Path type is `DAMAGE_FIRE` while lit. `animateTick` shows
  smoke at the front and the occasional crackle.
- `use` opens the menu. Right-clicking with a valid container plates the waiting result (FD `useHeldItemOnMeal`).
- Breaking it drops the inventory. FD's "carry the meal in the item" is skipped (D5).

### 3.2 Block entity (`OvenBlockEntity`)

FD's `CookingPotBlockEntity` with one more slot:

| Slot | Role |
|---|---|
| 0-5 | ingredients |
| 6 | **tool** (filter `#creativeengineering:bakeware`, stack size 1) |
| 7 | result waiting for plating (display) |
| 8 | container / plating input |
| 9 | output |

- The tick is FD's `cookingTick`, plus two checks: the recipe's `tool` must match slot 6, and finishing a bake
  calls `hurtAndBreak(tool_damage)` on the tool. A tool that breaks stops the next bake.
- The recipe cache, the "progress counts down while cold" rule, remainder ejection and experience on output all
  stay as in FD.
- Synced `ContainerData`: cook time, total time, heated flag (for the GUI flame).

### 3.3 Automation (item handler per side)

The oven's bottom faces its heat source and its top usually carries a pot, so FD's "top = ingredients" layout would
leave nothing open for automation. Instead (this also covers "allow input on any side" in cooking.md > processes):

- **Any side inserts.** Bakeware goes to the tool slot. An item matching the plating container of the result in
  slot 7 goes to slot 8. Everything else goes to the ingredient slots.
- **Any side extracts**, from the output slot only.
- Create funnels, chutes and hoppers then all work from the sides.

### 3.4 Bakeware items

KubeJS items (`StartupEvents.registry('item')` with `.maxDamage(n)`), with ids in `item_dict.js`, tagged into
`#creativeengineering:bakeware/<kind>`. Durability is a proposal:

| Item | For | Durability |
|---|---|---|
| `baking_sheet` | cookies, bread, toast, rolls | 128 |
| `cake_pan` | cakes, cake bases | 64 |
| `pie_tin` | pies, tarts, quiche, pie crust | 64 |
| `muffin_tin` | muffins | 64 |

Recipes come from plates (IE/Create sheets). Bakeware also fits the "decor > pots, pans, baking sheets" line in
cooking.md, and placeable decor versions could come later.

### 3.5 Menu and screen

- `OvenMenu`: FD `CookingPotMenu` plus the tool slot. `MenuType` comes from `IForgeMenuType.create` and is opened
  with `NetworkHooks.openScreen`, sending the position.
- `OvenScreen`: draw FD's own `farmersdelight:textures/gui/cooking_pot.png` by resource location (FD is installed,
  so nothing gets copied), then blit one extra slot background for the tool. Reuse its progress arrow and
  heated/not-heated flame. A dedicated GUI texture can come later; FD is MIT, so copying and editing its texture is
  allowed with the notice.
- Leave out FD's recipe book: it needs `RecipeBookType` enum extension and categories, which is a lot of code for
  little gain.

### 3.6 JEI

- A category for `creativeengineering:baking`, cloned from FD's `CookingRecipeCategory` with the tool slot added
  (tool shown as a catalyst-style slot, container under the result). The oven item is the catalyst.
- patcherMod already compiles against JEI (its JEI mixins), so this needs no new dependency, just a `@JeiPlugin`
  class.

### 3.7 Recipes (KubeJS side)

- Add a **`global.NCH.ceBaking(input, output, e, idOverride, opts)`** helper next to `fdCooking` in
  `neoCraftHelper.js` (`opts`: `tool`, `toolDamage`, `container`, `exp`, `time`). It emits
  `e.custom({ type: 'creativeengineering:baking', ... })` with id `creativeengineering:baking/<id>`, which follows
  CLAUDE.md's helpers-first rule.
- First recipe pass. Each one moves from the pot to the oven, so remove the old recipe:
  - **Muffins:** `fruitsdelight:blueberry_muffin` / `cranberry_muffin` are currently `farmersdelight:cooking`
    recipes from Fruits Delight 0.5.11 (blueberry or cranberry x2, `#forge:dough/wheat`,
    `#forge:milk/milk_bottle`, `#forge:eggs`, sugar -> 2). The oven version adds the muffin tin and paper plating.
    cooking.md's muffin variants (orange cranberry, lemon poppy) then become oven recipes.
  - Pies (pie tin, from the pie crust chain), cakes (cake pan, from cake bases), tarts (pie tin), bread / toast
    (baking sheet). Each one follows its own cooking.md line.
- **Lead on the "muffins not cookable?" bug** ([TODO.md](todo/TODO.md) 3): the muffin recipe needs
  `#forge:dough/wheat`. In FD 1.2.4 that tag holds only `farmersdelight:wheat_dough`, and
  `kubejs/server_scripts/tags/cookingtags.js` strips every tag from that item (`t.clearAll(e, [..., i.out_dough])`)
  because the pack uses Create's dough. If nothing else adds `create:dough` to `#forge:dough/wheat`, the tag is
  empty and no muffin recipe can match. Check the tag in game (JEI tag view, or `/kubejs hand` on Create dough).
  If it is empty, add `create:dough` to it in `cookingtags.js`. This fix is independent of the oven.

### 3.8 Config (`CEConfig`, `[oven]`)

- `ovenSpeed` (multiplier on `cookingtime`, default 1.0)
- `allowHeatConductors` (default true, D4)
- `burnOnStep` (default true, D6)

### 3.9 Size of the job

| Piece | New code |
|---|---|
| Block, block entity, item handler, menu, slots, screen | about 900 lines, mostly adapted from FD (FD's pot set is about 1,500 lines) |
| Recipe + serializer + type registration | about 200 |
| JEI plugin + category | about 150 |
| Registration (`BLOCK_ENTITY_TYPES`, `MENU_TYPES`, `RECIPE_TYPES`, `RECIPE_SERIALIZERS`, `BlockItem`), lang, loot table | about 80 |
| KubeJS: tags, `ceBaking` helper, bakeware items + textures, recipe pass | about 150 lines plus 4 textures |

On landing, the same release has to **delete `kubejs/startup_scripts/cooking/oven.js`**: patcherMod and KubeJS
both register in the `creativeengineering` namespace, and a duplicate `creativeengineering:oven` crashes at
registry time. The on-disk models and blockstate stay as they are (the Java block has the same states).

## 4. Limits and decisions

### 4.1 Routes considered

| Route | Verdict |
|---|---|
| **A. Java oven in patcherMod** (3.1-3.9) | **Recommended.** It keeps the `creativeengineering:oven` id and uses no FD classes (only FD tag ids and its GUI texture by resource location), so no new jar and no FD compile dependency. patcherMod already registers Java blocks, items and a custom recipe type (`fan/DragonBreathingRecipeType`) and builds against JEI. The GUI, automation, JEI and recipe JSON all come out like FD's |
| B. KubeJS block entity | Possible only as a prototype. KubeJS 1902.6.2-build.73 (checked in the jar) has `blockEntity(info => ...)` with `inventory(w, h)`, `rightClickOpensInventory()`, `serverTick` and `enableSync()`. But that gives a plain chest GUI with no slot roles, progress bar or heat display; `BlockEntityJS` has no item-handler capability, so hoppers and Create can't reach it; the jar has no recipe-type or serializer builder, so recipes would be a JS table invisible to JEI; and the matching logic would run in Rhino every tick per oven |
| C. Plain FD cooking pot recipes with "bakeware" | **A stopgap you can ship today.** Bakeware with `.containerItem(itself)` in KubeJS is handed back by the pot as an ingredient remainder (the tin isn't used up), and `container: paper` covers plating. What it lacks: no separate block, no heat rule, no stove heat on top, and no durability loss (the remainder is a fresh copy). The recipes move to route A by swapping the helper call |
| D. New in-tree mod (for example a kitchen mod for all of cooking.md's tools) | Worth it if mortar and pestle, stepstool, sourdough timing and the rest also become Java. It would change the block's namespace (or needs missing-mapping remaps), so decide before any worlds hold placeholders |

### 4.2 Open decisions

| # | Question | Default if you don't choose |
|---|---|---|
| D1 | Route: A (patcherMod), D (new mod), or C first as a stopgap? | A, with C only if recipes are wanted before the Java work |
| D2 | Do furnaces count as heat under the oven (vanilla furnace / smoker / blast furnace once [alcohol.md](alcohol.md) adds them to FD heat, and Nether's Delight's blackstone furnaces)? | no: they sit in the "stoves and ovens" group |
| D3 | `quark:blaze_lantern` and Brewin `heating_cask` as oven heat? | yes to both: neither is a stove |
| D4 | Heat through a hopper or chute (FD conductors)? | yes, as FD |
| D5 | Keep the result in the item when broken (FD pot item behaviour)? | no, drop the contents |
| D6 | Burn entities on top while lit? | yes, like the stove |
| D7 | Results per bake (for example 4 muffins per tin) and bakeware durability | as in 2.2 / 3.4 |
| D8 | Own textures: draw new ones or recolour the FD stove (MIT allows it)? | keep the FD stove textures by reference until art exists |

### 4.3 Known side effects

- Brewin kegs count a lit oven within 5x5x5 as heat (it is in `#farmersdelight:heat_sources`). That's intended.
- FD's `SkilletItem` (a skillet held near a heat source) checks `#farmersdelight:heat_sources` **without** the
  `lit` gate, so a cold oven next to you counts, the same as a cold stove does today. This is FD behaviour and
  stays as is.

## 5. File map

| File | What |
|---|---|
| `kubejs/startup_scripts/cooking/oven.js` | placeholder registration (delete when the Java block lands) |
| `kubejs/startup_scripts/_lib/item_dict.js` | `i.oven` |
| `kubejs/assets/creativeengineering/blockstates/oven.json` | `facing` x `lit` -> `block/oven` / `block/oven_on` |
| `kubejs/assets/creativeengineering/models/block/oven.json`, `oven_on.json` | `orientable_with_bottom` with the FD stove textures |
| `kubejs/assets/creativeengineering/models/item/oven.json` | parent `block/oven` |
| planned: `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/` | block, block entity, handler, menu, screen, recipe, JEI |
| planned: `kubejs/server_scripts/tags/blocktags.js` | the two block tags (2.3) |
| planned: `kubejs/startup_scripts/func/neoCraftHelper.js` | `ceBaking` |

## 6. Offline checks

- The JSON files parse (`python3 -m json.tool` on each oven file).
- The blockstate has all 8 `facing` x `lit` variants and every model it names exists.
- The textures named in the models exist in FD 1.2.4: `stove_bottom`, `stove_front`, `stove_front_on`, `stove_side`,
  `stove_top`, `stove_top_on` (checked against the 1.2.4 source 2026-10-18).

## 7. In-game test suite

Placeholder:

- [ ] **OV-01 Boots:** the game loads with no KubeJS startup error for `oven.js`, and `creativeengineering:oven` is
  in JEI as "Oven".
- [ ] **OV-02 Looks:** placed, it faces the player and looks like an unlit FD stove from every side, with no missing
  texture. The held item looks the same.
- [ ] **OV-03 Lit model:** with a debug stick set `lit=true`: the front and top switch to the animated lit stove
  textures.
- [ ] **OV-04 Mining:** breaks with a pickaxe and drops itself. With bare hands it drops nothing.
- [ ] **OV-05 Inert:** a cooking pot on it isn't heated (not a heat source yet).

Working oven: to be written with the Java build. It should cover heat per source and lit gating, the stove-only
rule, pot-on-top heating, tool required and damaged, paper plating, side automation, JEI, and the
placeholder-to-Java upgrade in an old world.

## 8. History

- 2026-10-18: written. Investigated FD 1.2.4 pot, stove and heat code, KubeJS 1902.6.2 block entity support and the
  existing ovens. Placeholder block added with the FD stove textures. Lead on the muffin bug recorded (3.7).
