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
| Oven block, block entity, menu/screen, `creativeengineering:baking` recipe type, JEI page (patcherMod `oven/`) | BUILT 2026-10-18: compiles against Forge 43.3.9 + JEI 11.8.1; 8 server GameTests pass with FD 1.2.4 loaded (6). First version DEPLOYED with 1.0.7; the 1.0.8 jar (with the counts option) STAGED for 1.0.8 and loaded in single player 2026-10-19. JEI [+] fails (open, [plan_1.0.8.md](plan_1.0.8.md) 2.2); not tested in game (OV-01 to OV-19) |
| Bakeware items: baking sheet, cake pan, pie tin, muffin tin (KubeJS, own textures 2026-10-19 with the muffin tin on the baking sheet's, no durability) | BUILT 2026-10-18, STAGED for 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested |
| Baking recipes: 38, from every live muffin, cookie, tart, pie and cake recipe (pot and crafting) with cooking.md's baked changes (flour, pie crust, cake base), each needing its bakeware; muffins plate on paper. Originals kept (2.6) | BUILT 2026-10-18, STAGED for 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested |
| Ingredient `count` in `creativeengineering:baking` (one slot gives up n items), behind `[cooking] ovenIngredientCounts`, off by default. Off: repeats take one slot each and 9 recipes over 6 slots drop items (2.6.1) | BUILT 2026-10-18, in patcherMod 1.0.8: STAGED for 1.0.8 |
| Muffin pot recipes on `#forge:dough` | BUILT 2026-10-18, STAGED for 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested. Whether this was the "not cookable" cause is unconfirmed (3.8) |
| Crafting recipes: oven (placeholder pattern, 3.6); bakeware by hammering, iron plate -> baking sheet -> muffin tin -> pie tin -> cake pan, on the cutting board or in the grid (2.5) | BUILT 2026-10-18, STAGED for 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested |
| IE hammer doesn't rotate the cutting board (`#creativeengineering:hammer_no_rotate`, patcherMod `IECompat`) | BUILT 2026-10-18, STAGED for 1.0.8; loaded in single player 2026-10-19 (no errors), not tested |
| Dummy baking recipes (one per bakeware piece) | REMOVED 2026-10-18 (2.7) |

Files to open first:

- `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/`: `OvenBlock`, `OvenBlockEntity`,
  `OvenHeat`, `OvenItemHandler`, `OvenMenu`, `OvenResultSlot`, `BakingRecipe`, `OvenRegistry`, `client/OvenScreen`,
  `jei/OvenJeiPlugin`, `jei/BakingRecipeCategory`
- [kubejs/server_scripts/cooking/baking.js](../kubejs/server_scripts/cooking/baking.js): baking and crafting recipes
- [kubejs/startup_scripts/cooking/bakeware.js](../kubejs/startup_scripts/cooking/bakeware.js): bakeware items
- `global.NCH.ceBaking` in [neoCraftHelper.js](../kubejs/startup_scripts/func/neoCraftHelper.js)
- Tags: [blocktags.js](../kubejs/server_scripts/tags/blocktags.js) (heat, hammer_no_rotate), [cookingtags.js](../kubejs/server_scripts/tags/cookingtags.js) (bakeware)
- `global.NCH.hammering` in neoCraftHelper.js; `IECompat.registerHammerNoRotate` in patcherMod `compat/`

Config knobs: `[cooking] ovenIngredientCounts` in `config/creativeengineering-common.toml` (default false; 2.2, 2.6.1).
Needs `/reload` after a change, because the recipes are built from it.

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
| `ingredients` | 1-6, shapeless (FD matching: tags, any order, no extra items). Each takes one slot |
| `ingredients[].count` | optional, default 1. Only with `[cooking] ovenIngredientCounts` on: that ingredient's slot must hold at least n, and a bake takes n from it (and gives n leftovers: 3 milk buckets -> 3 buckets). JEI shows the stack at n. Lets a 9-item crafting recipe (3 wheat, 3 apples, 2 sugar, a crust) fit in 4 slots. With the option off every count is read as 1 |
| `tool` | optional ingredient that must sit in the tool slot. Never used up, no durability. A recipe without `tool` ignores the tool slot |
| `container` | optional plating item (FD semantics: the result waits until one is given). Defaults to the result's crafting remainder |
| `result`, `experience`, `cookingtime` | as FD (`cookingtime` default 200, `experience` default 0) |

KubeJS: `R.ceBaking(inputs, output, e, idOverride, { exp, time, container, tool })`, id
`creativeengineering:baking/<id>`. A counted input is `Item.of(id, n)` or `{ id: '#tag', count: n }` (baking.js wraps
both as `n(id, count)`); scripts always write the original's full count. `ceBaking` reads `[cooking] ovenIngredientCounts`
(via `NCH.cfg`): on, the count goes into the JSON; off, it becomes n slots of 1, trimmed to 6 slots (2.6.1).

### 2.3 Slots

| Slot | Role | GUI position |
|---|---|---|
| 0-5 | ingredients | FD's 3x2 grid |
| 6 | tool (only `#creativeengineering:bakeware`, stack limit 1) | new slot left of the grid |
| 7 | result waiting for plating (display only) | FD's meal slot |
| 8 | plating container | FD's bowl slot |
| 9 | output (take only; taking awards recipe xp) | FD's output slot |
| (buffer 0-1) | leftover slots, take only, menu slots 46-47 right of the output; saved under `Remainders` | [remainder_buffer.md](remainder_buffer.md) |

### 2.4 Tags

| Tag | Members | Where |
|---|---|---|
| `#creativeengineering:oven_heat_sources` (block) | `#farmersdelight:tray_heat_sources` (fire, campfires, lava; nested), `magma_block`, `lava_cauldron`, `create:blaze_burner`, `create:lit_blaze_burner`, `quark:blaze_lantern` | blocktags.js |
| `#farmersdelight:heat_sources` (block) | `+ creativeengineering:oven` | blocktags.js |
| `minecraft:mineable/pickaxe` (block) | `+ creativeengineering:oven` | blocktags.js |
| `#creativeengineering:bakeware/{baking_sheet,cake_pan,pie_tin,muffin_tin}` (item) | the matching item | cookingtags.js |
| `#creativeengineering:bakeware` (item) | the four kind tags | cookingtags.js |
| `#creativeengineering:hammer_no_rotate` (block) | `farmersdelight:cutting_board` | blocktags.js |

### 2.5 Bakeware

One iron plate, hammered into each shape in turn with the Engineer's Hammer (`R.hammering`):

| Item | From | Used for |
|---|---|---|
| `creativeengineering:baking_sheet` | `#forge:plates/iron` | cookies, tarts |
| `creativeengineering:muffin_tin` | baking sheet | muffins (plated on paper) |
| `creativeengineering:pie_tin` | muffin tin | pies |
| `creativeengineering:cake_pan` | pie tin | cakes, cheesecake, cake base |

Each step is two recipes: `farmersdelight:cutting` with the hammer as the tool (`farmersdelightcutting/<item>_hammering`;
put the piece on a cutting board, use the hammer on it, 1 durability), and a shapeless craft, hammer + piece
(`shapeless/<item>_hammering`; IE's hammer comes back damaged, as for the hammer plate recipes). The earlier
placeholder shaped patterns are gone.

The IE hammer rotates any block that has no IE block entity (`HammerItem.onItemUseFirst` -> `RotationUtil.rotateBlock`),
and that runs before the block's own `use`. FD's cutting board implements `rotate`, so without a fix the hammer turned
the board and never cut. `IECompat.registerHammerNoRotate` adds an IE rotation blacklist entry (main thread, via
`enqueueWork`) that refuses blocks in `#creativeengineering:hammer_no_rotate`; the hammer then passes and the board's
`use` runs the cutting recipe.

Textures are `creativeengineering:item/<id>` (from `textureswapper/`); the muffin tin uses `item/baking_sheet` until it
has its own. All four stack to 64 and have no durability.

### 2.6 Baking recipes

Every live recipe whose result is a muffin, cookie, tart, pie or cake (dump of 2026-10-18: `crafting_audit.json` plus
the pot recipes) has an oven version. The originals stay. Each starts as a mirror (same ingredients, counts, result
count, plating), then takes the cooking.md "sweet > baked" changes that apply to its kind:

| cooking.md | Oven change |
|---|---|
| muffins: "fruit, milk, egg, sugar, flour", "serve w/ paper" | `#forge:dough` -> `#forge:flour/wheat`; plated on paper |
| cookies: "all wheat to flour" | wheat -> flour, one for one |
| pie: "wheat to flour"; pie crust "use to craft all pies"; cornmeal "usable as flour in pies and cakes" | wheat -> flour or cornmeal (`#creativeengineering:flour_or_cornmeal`); the two crustless pies gain a crust |
| cake: "cake base recipe for all cakes" | Create Crafts & Additions' raw cake base (egg, 2 sugar, dough) replaces each cake's egg, sugar and wheat/dough; the oven also makes the base (flour or cornmeal) |

The tables give the full cost. With `ovenIngredientCounts` on that is what the oven takes, a repeated ingredient sharing
one slot; with it off (the default) repeats take a slot each, and the 9 recipes that then need more than 6 slots are
trimmed (2.6.1). Sugar is `#forge:sugar` everywhere, as cooking.js gives the originals. Times are 200 ticks and 0.1 xp
unless noted. `n x` = n of that item; flour is `#forge:flour/wheat` (Create wheat flour).

**Muffins** (muffin tin, plated on paper, 2 per bake): `blueberry_muffin`, `cranberry_muffin`: 2x berry, flour, milk
bottle, egg, sugar (the pot recipes in cooking.js, with flour for their dough; the pot recipes keep dough).

**Cookies** (baking sheet, 8 per bake), all 2x flour plus:

| Id | Plus | Source |
|---|---|---|
| `cookie` | cocoa beans | minecraft (Quark's copy) |
| `honey_cookie`, `sweet_berry_cookie` | honey bottle / sweet berries | farmersdelight |
| `green_tea_cookie` | green tea leaves | farmersrespite |
| `bayberry_cookie`, `cranberry_cookie`, `persimmon_cookie` | bayberry / cranberry / dried persimmon | fruitsdelight |
| `lemon_cookie` | lemon, milk bottle | fruitsdelight |
| `mulberry_cookie` | mulberry | abnormals_delight |
| `uncanny_cookies` | mite crust | endersdelight |

**Tarts** (baking sheet, 2 per bake): `lemon_tart`, `fig_tart`: fruit, pie crust, egg, sugar, milk bottle (Fruits
Delight pot, unchanged: no wheat, already on a crust).

**Pies** (pie tin; "flour" is wheat flour or cornmeal):

| Id | Ingredients | Result | Source |
|---|---|---|---|
| `apple_pie` | 3x flour, 2x sugar, crust, 3x apple | 1 | farmersdelight |
| `chorus_pie` | 3x flour, 2x sugar, crust, 3x chorus fruit | 1 | endersdelight |
| `salmonberry_pie` | 3x flour, 2x sugar, crust, 3x salmonberries | 1 | delightful |
| `mulberry_pie` | 3x flour, 2x sugar, crust, 3x mulberry | 1 | create_central_kitchen |
| `mulberry_pie_simple` | sugar, egg, **crust**, mulberry | 1 | upgrade_aquatic (crust added) |
| `chocolate_pie` | 3x `#forge:milk`, 2x sugar, crust, 3x `#forge:bars/chocolate` | 1 | farmersdelight (Delightful's bar version, the live one) |
| `rose_hip_pie` | 2x milk, honey bottle, 2x sugar, crust, 3x rose hips | 1 | farmersrespite |
| `pumpkin_pie` | 3x flour, sugar, crust, egg, 3x pumpkin | 1 | delightful |
| `pumpkin_pie_from_slices` | 2x sugar, crust, 6x pumpkin slice | 1 | create_central_kitchen |
| `pumpkin_pie_duck_egg` | sugar, duck egg, **crust**, pumpkin | 1 | untitledduckmod (crust added) |
| `durian_pie`, `pineapple_pie` | 2x flesh / slice, crust, egg, sugar | 2 | fruitsdelight pot |

Shepherd's pie isn't baked in the oven (user, 2026-10-18).

**Cakes** (cake pan). With the cake base in place of egg, sugar and wheat/dough, the recipes left differ only in milk,
so they collapse to one per cake, keeping the main mod's milk count:

| Id | Ingredients | From |
|---|---|---|
| `cake` | 3x milk, cake base | farmersdelight = neapolitan:cake. Folded in: Create's (milk bucket + base, exact) and the duck/goose egg cakes |
| `adzuki_cake`, `banana_cake`, `neapolitan_chocolate_cake`, `mint_cake`, `strawberry_cake`, `vanilla_cake` | 2x milk, cake base, 2x flavour (chocolate: any `#forge:bars/chocolate`, as CCK's) | neapolitan. Folded in: Create Central Kitchen's dough versions (3 milk) |
| `coffee_cake` | 2x milk, cake base, 2x coffee beans | farmersrespite (its `minecraft:egg` now comes in the base, any egg). Folded in: CCK's dough version |
| `mangosteen_cake` | cake base, 2x mangosteen; plated in a bowl | fruitsdelight pot (2 wheat, 2 sugar became the base, which adds an egg) |
| `sweet_berry_cheesecake` | 2x milk, crust, 6x sweet berries | farmersdelight; on a pie crust, so no cake base |
| `cake_base` | egg, 2x sugar, 2x flour or cornmeal -> 1 raw cake base | pack. Priced between the two: a cake from it (+ 3 milk) is 1 flour cheaper than FD's 3-wheat craft, and Create's compacted base (egg, 2 sugar, 1 dough) stays cheaper |
| `cake_base_baked` | cake base; 100 ticks, 0 xp (its smoker recipe) | createaddition |

Cake base comes from Create compacting (`createaddition:compacting/cake_base`: egg, 2 sugar, dough in a basin under a
press, unchanged) or the oven (`cake_base` above), so oven cakes don't need Create. cooking.md's "slice dough block
to cake bases" isn't built.

Not taken from cooking.md, because they aren't oven recipe changes or don't exist yet: frosting / egg solution fluids, sequenced-assembly versions,
merging the two chocolate cakes, new muffin/tart variants.

Not mirrored: `*_from_slices` reassembly, the golden cookie / cake / pumpkin pie (gold around a finished item), canned
pie fillings + crust (CanServings), pancakes, Create machine recipes (filling, compacting, mixing), and the blaze, echo
and slime "cakes" (not baked goods).

### 2.6.1 Over 6 slots: what the default drops

With `ovenIngredientCounts` off, `ceBaking` gives each item its own slot. A recipe that needs more than 6 loses one
item at a time from its largest repeated group until it fits; on a tie, the group listed first in baking.js loses one.
Counted recipes list filler (milk, sugar, flour) first and the defining ingredient last, so ties take filler. Every
distinct ingredient keeps at least one slot. `ceBaking` logs each drop (`[NCH] ceBaking <id>: over 6 slots, dropped ...`).
The other 29 recipes fit as they are. The cake base keeps every cake but the cheesecake under 6.

| Recipe | Full (9 items) | Oven, counts off (6 slots) | Saved per bake |
|---|---|---|---|
| `apple_pie` | 3 flour, 2 sugar, crust, 3 apple | flour, 2 sugar, crust, 2 apple | 2 flour, 1 apple |
| `chorus_pie` | 3 flour, 2 sugar, crust, 3 chorus fruit | flour, 2 sugar, crust, 2 chorus fruit | 2 flour, 1 chorus fruit |
| `salmonberry_pie` | 3 flour, 2 sugar, crust, 3 salmonberries | flour, 2 sugar, crust, 2 salmonberries | 2 flour, 1 salmonberries |
| `mulberry_pie` | 3 flour, 2 sugar, crust, 3 mulberry | flour, 2 sugar, crust, 2 mulberry | 2 flour, 1 mulberry |
| `chocolate_pie` | 3 milk, 2 sugar, crust, 3 chocolate bar | milk, 2 sugar, crust, 2 chocolate bar | 2 milk, 1 chocolate bar |
| `rose_hip_pie` | 2 milk, honey bottle, 2 sugar, crust, 3 rose hips | milk, honey bottle, sugar, crust, 2 rose hips | 1 milk, 1 sugar, 1 rose hips |
| `pumpkin_pie` | 3 flour, sugar, crust, egg, 3 pumpkin | flour, sugar, crust, egg, 2 pumpkin | 2 flour, 1 pumpkin |
| `pumpkin_pie_from_slices` | 2 sugar, crust, 6 pumpkin slice | 2 sugar, crust, 3 pumpkin slice | 3 pumpkin slice |
| `sweet_berry_cheesecake` | 2 milk, crust, 6 sweet berries | 2 milk, crust, 3 sweet berries | 3 sweet berries |

Every trimmed recipe saves 3 of its 9 items (a third). Milk can be a bucket or a bottle (`#forge:milk`), so a saved
milk is a bucket's or a bottle's worth. Turning the option on restores the full cost for all of them, with no script
change, after `/reload`.

### 2.7 Dummy bakeware recipes

Removed 2026-10-18; the real recipes in 2.6 use every bakeware piece.

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
bake takes one of each ingredient and adds the result to slot 7. Ingredient leftovers (buckets, bottles) go to the
container slot when the waiting result is served in them, else to the two leftover slots; with the remainder buffer
switched off (`[cooking] remainderBuffer`) or full they come out of the front ([remainder_buffer.md](remainder_buffer.md)).
A bake whose leftovers wouldn't fit waits (`stallWhenFull`). A result without a container moves to output; one with a container waits for it (slot 8, or right-click the
oven with it).

### 3.4 Tool slot

Every baking recipe asks for bakeware (2.6). The tool is never used up. Filter: `#creativeengineering:bakeware`.

### 3.5 Automation (`OvenItemHandler`), the same on every face

- Insert: bakeware to the tool slot; the plating item (what the waiting result needs, or the last recipe's
  container) to slot 8; anything else to the ingredient slots.
- Extract: output slot, then the two leftover slots (handler slots 10-11).
- Limit: an item that is the last recipe's plating item can't be inserted as an ingredient by automation.

### 3.6 Changes from FD's pot

No recipe book (it needs a `RecipeBookType` enum extension), no meal kept in the item when broken, no custom name,
leftovers that don't go to the buffer eject from the front instead of the left. The GUI draws FD's own `cooking_pot.png` by resource location
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
- **Recipes mirror the originals, originals stay** (user, 2026-10-18): every muffin, cookie, cake, pie and tart
  recipe, with bakeware by kind and paper plating for muffins, then cooking.md's baked changes on top (user,
  2026-10-18): flour for wheat/dough, a crust in every pie, a cake base in every cake. Cakes that differed only in
  milk after that were merged, keeping the main mod's milk count. No shepherd's pie.
- **Taking several items from one slot is opt-in** (user, 2026-10-18): `[cooking] ovenIngredientCounts`, default off.
  By default a repeated ingredient takes a slot per item (as in the FD pot), and recipes that won't fit in 6 slots
  drop repeated items (2.6.1, a third cheaper for those 25). The scripts keep the full cost so the option restores it.
- **Bakeware by hammering** (user, 2026-10-18): plate -> baking sheet -> muffin tin -> pie tin -> cake pan, cutting
  board or grid. The ids stay `pie_tin` / `muffin_tin` (the ask said "pie pan", "muffin pan").
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
| `kubejs/startup_scripts/func/neoCraftHelper.js` | `ceBaking`, `hammering` |
| `development/patcherMod/.../cooking/RemainderBuffer.java` | `leftovers(..., counts)`: n leftovers for n items taken |
| `development/patcherMod/.../compat/IECompat.java`, `CreativeEngineering.java` | `registerHammerNoRotate` (cutting board vs IE hammer) |
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
- [ ] **OV-03 Craft:** oven and the four bakeware recipes show in JEI and craft. Baking sheet, pie tin and cake pan show their own textures; the muffin tin looks like the baking sheet.
- [ ] **OV-04 Heat sources:** lit campfire, fire, magma, lava, blaze burner with fuel -> lit; unlit campfire, empty
  blaze burner, FD stove, Nether's Delight blackstone stove, another oven -> not lit.
- [ ] **OV-05 GUI:** right-click opens "Oven": FD pot layout, tool slot left of the grid, flame shows heat, arrow
  fills, heat tooltip reads right.
- [ ] **OV-06 Bake:** a sample from each table in 2.6 bakes in the oven with its bakeware, and the original pot or
  crafting recipe still works.
- [ ] **OV-07 Plating:** muffins wait for paper (one per muffin), the mangosteen cake for a bowl; right-click with
  one serves it.
- [ ] **OV-08 Pot on top:** campfire -> oven -> cooking pot: the pot cooks; put out the campfire: the pot stops.
- [ ] **OV-09 Tool slot:** only bakeware goes in, one at a time; shift-click routes it there.
- [ ] **OV-10 Automation:** a hopper or Create funnel on a side fills ingredients; another extracts only output.
- [ ] **OV-11 JEI:** "Baking" page lists the 38 recipes with the oven as catalyst and the bakeware left of the grid;
  counted ingredients show their stack size (apple pie with counts on: 3 flour, 2 sugar, 3 apples); clicking the GUI arrow opens
  it; "+" moves the counted amounts in.
- [ ] **OV-12 Break / burn:** breaking drops oven and contents; standing on a lit oven hurts unless sneaking.
- [ ] **OV-13 Muffins:** blueberry and cranberry muffins cook in the pot with Create dough. If not, record which
  ingredient slot JEI shows empty.
- [ ] **OV-15 Bakeware required:** no recipe bakes without its bakeware in the tool slot; the tool stays afterwards.
  No `dummy_*` recipe in JEI.
- [ ] **OV-16 Counts off (default):** JEI's apple pie shows 6 single slots (flour, 2 sugar, crust, 2 apple); it bakes
  with one item per slot and takes exactly those. The KubeJS log lists the 9 `over 6 slots, dropped` lines of 2.6.1.
- [ ] **OV-18 Counts on:** set `ovenIngredientCounts = true`, `/reload`: apple pie shows 4 slots (3 flour, 2 sugar,
  crust, 3 apples); 2 flour in the slot doesn't start, 3 does and takes exactly 3/2/1/3. Chocolate pie on milk
  buckets gives back 3 buckets (leftover slots).
- [ ] **OV-19 cooking.md swaps:** cookies, pies and muffins take Create wheat flour, not wheat or dough; every cake
  takes a cake base (Create-compacted) and bakes from it; the Upgrade Aquatic mulberry pie and duck-egg pumpkin pie
  need a crust; no shepherd's pie in the oven's JEI page. Pies and the cake base also take cornmeal; cookies and
  muffins don't. The oven makes a raw cake base from egg, 2 sugar, 2 flour.
- [ ] **OV-17 Hammering:** iron plate on a cutting board + Engineer's Hammer -> baking sheet, the board doesn't turn,
  the hammer loses 1 durability; repeat down the chain (muffin tin, pie tin) to the cake pan. Hammer + piece in the crafting grid does the
  same and returns the damaged hammer. The hammer still rotates other blocks and still forms IE multiblocks.
- [ ] **OV-14 Server:** the same on a dedicated server with a client (menu opens, slots sync, JEI page present).

## 8. History

- 2026-10-18: written as a plan with a KubeJS placeholder block on the FD stove textures.
- 2026-10-18: built. Java oven in patcherMod (route A), placeholder registration removed and its models moved into the
  jar. Bakeware items (IE iron plate texture, no durability), tags, `NCH.ceBaking`, 7 baking recipes copied from the
  pot, oven and bakeware crafting recipes, muffin pot recipes on `#forge:dough`. Dough lead corrected (3.8). Compiles;
  8 GameTests pass.
- 2026-10-18: one dummy baking recipe per bakeware piece (2.7).
- 2026-10-18: all muffin, cookie, cake, pie and tart recipes mirrored (46, 2.6), each with its bakeware; muffins on
  paper. Ingredient `count` added to the recipe type. Dummy recipes removed. Bakeware made by hammering
  (2.5) instead of the placeholder shapes; IE hammer kept from rotating the cutting board.
- 2026-10-18: ingredient counts put behind `[cooking] ovenIngredientCounts` (default off). With it off, repeats take
  a slot each and recipes over 6 slots drop repeated items (2.6.1).
- 2026-10-18: cooking.md baked changes applied to the oven recipes (flour, pie crust in every pie, cake base in every
  cake; cakes merged to one per cake, 46 -> 37 recipes); shepherd's pie dropped.
- 2026-10-19: bakeware textures (`textures/item/<id>.png`; the muffin tin uses the baking sheet's); chain reordered
  to baking sheet -> muffin tin -> pie tin -> cake pan.
- 2026-10-19: oven cake base recipe (38); pies and the cake base take cornmeal ([recipes_cooking.md](recipes_cooking.md) 2.1).
- 2026-10-18: remainder buffer: two leftover slots (GUI, automation, drops, saved), leftovers recycled into the
  container slot when they plate the waiting result ([remainder_buffer.md](remainder_buffer.md)).
- 2026-10-19: the oven block, first bakeware and first baking recipes had gone out in pack 1.0.7 (DEPLOYED 2026-10-18).
  Everything since (37 recipes, counts, hammered bakeware, the hammer fix, textures, cornmeal) STAGED in pack 1.0.8. Open
  bug from the 2026-10-19 single-player log: JEI [+] on the baking page fails with "references crafting slot index [2]
  but the player cannot pickup from it" (1,413 lines); the transfer handler is unchanged since 1.0.7, so 1.0.7 has it
  too. Fix for 1.0.9 ([plan_1.0.8.md](plan_1.0.8.md) 2.2).
