# Oven: a Farmer's Delight style baking block

The "new farmers delight oven block" from cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19) (sweet > baked). Java in patcherMod, recipes and
tags in KubeJS. Test prefix `OV-`.

The ask, from cooking.md:

- works almost exactly like the cooking pot, but for baked goods
- a recipe can require an input tool (baking sheet, cake pan) and a plating item (paper for muffins)
- needs a non-stove heat source under it (magma block, campfire)
- gives stove heat to whatever sits on top

## 0. At a glance

| Part | Status |
|---|---|
| Oven block, block entity, menu/screen, `creativeengineering:baking` recipe type, JEI page (patcherMod `oven/`) | BUILT 2026-10-18: compiles against Forge 43.3.9 + JEI 11.8.1; 8 server GameTests pass with FD 1.2.4 loaded (6). First version DEPLOYED with 1.0.7; the 1.0.8 jar (with the counts option) DEPLOYED with 1.0.8 and loaded in single player 2026-10-19. JEI [+] fix DEPLOYED with 1.0.9 (patcherMod 1.0.9) ([plan_1.0.9.md](plan_1.0.9.md)); not tested in game (OV-01 to OV-19) |
| Bakeware items: baking sheet, cake pan, pie tin, muffin tin (KubeJS, own textures 2026-10-19 with the muffin tin on the baking sheet's, no durability) | BUILT 2026-10-18, DEPLOYED with 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested |
| (Superseded 2026-10-22) Baking recipes: 38, from every live muffin, cookie, tart, pie and cake recipe (pot and crafting) with cooking.md's baked changes (flour, pie crust, cake base), each needing its bakeware; muffins plate on paper. Originals kept (2.6) | BUILT 2026-10-18, DEPLOYED with 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested |
| Baking overhaul: all cake, pie, cookie, crust and cake base recipes removed and rebuilt on shared bases (cake base, pie crust, frosting, cookie dough) + 2 flavour; pies and cookies oven only; pot pie fillings; crust + filling bakes (2.6) | built, scratch test queued 2026-10-22 ([SQ-BK](testing/scratch_queue.md), probe `tools/baking_probe/baking_probe.js`; node dry-run clean). Needs a restart (new items). In game: OV-25 to OV-32 |
| Opened cans refused as oven ingredients (`BakingRecipe`) | built, scratch test queued (SQ-BK-05); patcherMod 1.1.01 in `mods/`, not deployed; OV-29 |
| Ingredient `count` in `creativeengineering:baking` (one slot gives up n items), behind `[cooking] ovenIngredientCounts`, off by default. Off: repeats take one slot each and 9 recipes over 6 slots drop items (2.6.1) | BUILT 2026-10-18, in patcherMod 1.0.8: DEPLOYED with 1.0.8 |
| Muffin pot recipes on `#forge:dough` | BUILT 2026-10-18, DEPLOYED with 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested. Whether this was the "not cookable" cause is unconfirmed (3.8) |
| Crafting recipes: oven (placeholder pattern, 2.1); bakeware by hammering, iron plate -> baking sheet -> muffin tin -> pie tin -> cake pan, on the cutting board or in the grid (2.5) | BUILT 2026-10-18, DEPLOYED with 1.0.8; loaded in single player 2026-10-19 (no script errors), not tested |
| IE hammer doesn't rotate the cutting board (`#creativeengineering:hammer_no_rotate`, patcherMod `IECompat`) | BUILT 2026-10-18, DEPLOYED with 1.0.8; loaded in single player 2026-10-19 (no errors), not tested |
| Dummy baking recipes (one per bakeware piece) | REMOVED 2026-10-18 (2.7) |

Files to open first:

- `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/oven/`: `OvenBlock`, `OvenBlockEntity`,
  `OvenHeat`, `OvenItemHandler`, `OvenMenu`, `OvenResultSlot`, `BakingRecipe`, `OvenRegistry`, `client/OvenScreen`,
  `jei/OvenJeiPlugin`, `jei/BakingRecipeCategory`
- [kubejs/server_scripts/cooking/baking.js](../kubejs/server_scripts/cooking/baking.js): baking and crafting recipes
- [kubejs/startup_scripts/cooking/bakeware.js](../kubejs/startup_scripts/cooking/bakeware.js): bakeware items
- [kubejs/startup_scripts/cooking/baking_ingredients.js](../kubejs/startup_scripts/cooking/baking_ingredients.js): frosting, cookie dough
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

- `cookingforblockheads:oven`: a fuel-burning furnace that runs smelting recipes (and smoking ones since 2026-10-21,
  2.8). It has no recipe type of its own, no tools, no plating and no heat rule.
- `vinery:wood_fired_oven`, `vinery:stove`: heat sources only, and Vinery 1.5.5 removes both ([alcohol.md](alcohol.md) I21).
- `handcrafted:oven`: decoration. `fantasyfurniture:*/oven` (7): working smokers (2.8).

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
both as `n(id, count)`); scripts write the recipe's count (the overhaul's only repeats are 2 or 3 of one item). `ceBaking` reads `[cooking] ovenIngredientCounts`
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

**Baking overhaul (2026-10-22, from the user's scratchpad notes).** Every recipe of every mod whose result is a
cake, pie, cookie, pie crust or cake base is removed (baking.js `REMOVED_OUTPUTS`: KubeJS `remove({ output })`, except
`*_from_slices` reassembly and the pack's own recipes). Each family is rebuilt on one shared base plus 2 of its flavour.
The old mirrors of every mod's recipe (38, 2026-10-18 to 10-21) are gone. Muffins and tarts are unchanged.

"Manual craft" is a shapeless craft with an id ending `_manual_only`, which Create's mixer and mechanical crafter skip.
Every base is manual craft or oven only: no Create or IE machine recipe (the removal takes every other mod's, and the
pack adds none; 2026-10-22, user). Sugar is `#forge:sugar`, flour is
`#creativeengineering:flour_or_cornmeal` (wheat flour or cornmeal), dough is `#forge:dough`, milk is `#forge:milk`
(bucket or bottle). Times are 200 ticks and 0.1 xp unless noted.

**Bases**

| Item | Recipe | Where |
|---|---|---|
| Raw cake base (`createaddition:cake_base`) | 3 flour, 2 egg, 2 sugar; or 3 dough, egg, 2 sugar | manual craft |
| Baked cake base (`createaddition:cake_base_baked`) | 2 flour, 2 egg, 2 sugar; or 2 dough, egg, 2 sugar; or a raw base (100 ticks, 0 xp) | oven, cake pan |
| Pie crust, 3 | 3 flour, egg, milk; or 3 dough, milk | manual craft |
| Frosting (`creativeengineering:frosting`), 2 | sugar, 2 milk | manual craft |
| Cookie dough (`creativeengineering:cookie_dough`) | dough, 2 sugar | manual craft |

Frosting and cookie dough are new plain items (not food) in `startup_scripts/cooking/baking_ingredients.js`, on
placeholder textures until they get their own: Rustic Delight's cotton boll (frosting), Create Confectionery's
gingerdough (cookie dough).

**Cakes** (`CAKES`): baked base + frosting + 2 flavour (manual craft), or raw base + frosting + 2 flavour (oven, cake
pan). Plain `minecraft:cake`: 3 frosting, or 2 frosting + one sweet berry, cranberry or cherry (`vinery:cherry`), both ways.

| Cake | Flavour (2) |
|---|---|
| `adzuki_cake`, `banana_cake`, `mint_cake`, `strawberry_cake`, `vanilla_cake` (neapolitan) | roasted adzuki beans / banana / mint leaves / strawberry / dried vanilla pods |
| `chocolate_cake` (createaddition; Neapolitan's merges into it, item_merges.js). Also a spout recipe: baked base + 500 mB `create:chocolate` (C&A's own, restored 2026-10-22, `chocolate_cake_spout`) | `#forge:bars/chocolate` |
| `honey_cake` (createaddition) | honey bottle |
| `coffee_cake` (farmersrespite) | coffee beans |
| `mangosteen_cake` (fruitsdelight) | mangosteen (no bowl now) |

**Pies** (`PIES`, pie tin): crust + 2 fruit + 2 sugar in the oven, a honey bottle allowed in either sugar slot. Or crust
+ a full can of that pie's filling, in the oven or by hand (the hand craft is canning.js's, for the cans whose json
names a `pie`: all but durian and pineapple). The filling comes from the cooking pot: 2 fruit + 2 sugar (honey
allowed), an empty can in the container slot. The canned fillings from 4 slices (canning.js) stay.

| Pie | Fruit (2) |
|---|---|
| `apple_pie` | apple |
| `chocolate_pie` | `#forge:bars/chocolate` |
| `chorus_pie` | chorus fruit |
| `mulberry_pie` (upgrade_aquatic) | mulberry |
| `pumpkin_pie` (minecraft) | FD pumpkin slice |
| `rose_hip_pie` | rose hips |
| `salmonberry_pie` | salmonberries |
| `durian_pie`, `pineapple_pie` | durian flesh / pineapple slice (1 pie now, was 2; see below) |
| `sweet_berry_cheesecake` (renamed Sweet Berry Pie) | sweet berries (a pie since 2026-10-22, pie tin); Canned Sweet Berry Pie Filling |

Durian and pineapple pies are Fruits Delight's single hand-held items (5 hunger, 4 saturation points each), not
placeable pies with 4 slices like the others (a whole apple pie is 4 slices of 3 hunger = 12). Fruits Delight's pot made 2
a batch. Under the "-> 1 pie" rule a batch now gives 5 hunger for the cost of a 12-hunger pie. Their can holds 4 pies
but bakes back into 1, and they have no hand craft back (no `pie` in the can json). Both are marked for a rework, as they
are not really pies (TODO `food/baked-goods #12`).

Sweet Berry Pie is Farmer's Delight's `sweet_berry_cheesecake`, renamed (whole, slice, Central Kitchen's incomplete item,
the Display Delight plates); it is the pack's only sweet berry pie (Central Kitchen's Sweet Berry Cake Slice is a cake).
It has a filling can like the other pies. Pie slices with no whole pie in the pack (cherry, truffle, four BYG ones,
gloomgourd, source berry): TODO `food/baked-goods #13`, `#14`.

No cherry pie: Create Central Kitchen has the placed block (`create_central_kitchen:cherry_pie`, "Placed Cherry Pie":
whole and cut-face textures, 3 bite models) and the slice item, but no whole-pie item (the mod expects another mod's).
It needs an item that places the block first (TODO `food/baked-goods #13`).

A partly eaten can is no ingredient in the oven (patcherMod `BakingRecipe.slotCounts` skips `CanServings.isPartial`).
That keeps the old craft's full-can rule.

**Cookies** (`COOKIES`, baking sheet, oven only): cookie dough + 2 flavour -> 12.

| Cookie | Flavour (2) |
|---|---|
| `cookie` | cocoa beans |
| `honey_cookie`, `sweet_berry_cookie` | honey bottle / sweet berries |
| `green_tea_cookie` | green tea leaves |
| `bayberry_cookie`, `cranberry_cookie`, `persimmon_cookie`, `lemon_cookie` | bayberry / cranberry / dried persimmon / lemon |
| `mulberry_cookie` | mulberry |
| `uncanny_cookies` | mite crust |
| `cherry_cookie` (abnormals_delight, new 2026-10-22) | cherry (`vinery:cherry`) |

**Muffins** (muffin tin, plated on paper, 4 per bake; the pot gives 2): `blueberry_muffin`, `cranberry_muffin`: 2x
berry, wheat flour, milk bottle, egg, sugar.

**Tarts** (baking sheet, 2 per bake): `lemon_tart`, `fig_tart`: fruit, pie crust, egg, sugar, milk bottle.

Not touched: the golden cookie / cake / pumpkin pie (gold around a finished item), Botania's mana cookie, the blaze,
echo and slime "cakes" (not baked goods), shepherd's pie (savoury, a feast), cans of cookies / cakes, tarts. No
recipes yet for pies or cookies whose items have no live source: CCK cherry / truffle pies (no whole item), the
Delightful berry pies, Abnormals Delight's maple cookie.

### 2.6.1 Over 6 slots

No recipe needs more than 6 slots now. The largest are the flour baked cake base and the muffins (6 each). With
`ovenIngredientCounts` off nothing is dropped, and the `over 6 slots, dropped` log line no longer appears. Before the
overhaul 9 pies and the cheesecake were trimmed (history, 2026-10-18).

### 2.7 Dummy bakeware recipes

Removed 2026-10-18; the real recipes in 2.6 use every bakeware piece.

### 2.8 Other ovens (TODO `food/oven #4`)

| Block | What it does | Tooltip ([tooltips.js](../kubejs/client_scripts/tooltips.js), kitchen section) |
|---|---|---|
| `cookingforblockheads:oven` (`i.cfb_oven`) | Smelting recipes with food results (CfB's rule), then smoking recipes with food results: patcherMod `mixin/CfbOvenSmokingMixin`, RETURN of CfB's `getSmeltingResult` when it found nothing. The insert filter uses the same method, so smoker-only food goes in too. Fuel: [kitchen.md](kitchen.md) 2.1 | "Cooks food like a furnace and a smoker" (plus the kitchen #8 fuel lines) |
| `fantasyfurniture:{royal,nordic,venthyr,necrolord,dunmer,bone/skeleton,bone/wither}/oven` | Their own block entity runs `RecipeType.SMOKING` with normal fuel (6.7.0, decompiled) | "Works like a smoker" (`addTooltipPattern 'fantasyfurniture:*/oven'`, log line expects 7) |
| Farmhouse Decorations antique ovens | Decorative (no block entity) | None: the mod is in `disabledmods/`. Add "Decorative" if it comes back |

Smoker-only foods that now cook in the CfB oven include `farmersdelight:ham` -> `smoked_ham` and the pack's own
`sourdough` -> `sourdough_bread`. Probe: [tools/kitchen_probe/cfb_oven_smoking_probe.js](../tools/kitchen_probe/cfb_oven_smoking_probe.js)
(`/kotest`, scratch server only; not run yet).

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
- **Recipes mirror the originals, originals stay** (user, 2026-10-18; SUPERSEDED 2026-10-22 by the baking overhaul,
  2.6): the mirrors are gone. Every cake, pie and cookie recipe is rebuilt on shared bases (cake base, pie crust,
  frosting, cookie dough), cookies are oven only, and the oven is the efficient route while the hand-craft route stays
  for cakes and pie fillings. Bakeware by kind and paper plating for muffins are unchanged. No shepherd's pie.
- **Taking several items from one slot is opt-in** (user, 2026-10-18): `[cooking] ovenIngredientCounts`, default off.
  By default a repeated ingredient takes a slot per item (as in the FD pot). With the overhaul no recipe needs more
  than 6 slots, so nothing is trimmed (2.6.1); the option only changes how repeats are stored.
- **Bakeware by hammering** (user, 2026-10-18): plate -> baking sheet -> muffin tin -> pie tin -> cake pan, cutting
  board or grid. The ids stay `pie_tin` / `muffin_tin` (the ask said "pie pan", "muffin pan").
- **Heat conductors allowed** (as FD). **Furnaces aren't oven heat** (stoves-and-ovens group). Brewin's
  `heating_cask` isn't in the tag. It was written when the pack's Brewin 2.0 jar had no such block; the 4.5.0 port
  (in the pack since 1.0.8) has it, and adding it is still open ([briefs/food_oven.md](briefs/food_oven.md)).
- The oven's crafting pattern is a placeholder (the bakeware is hammered, 2.5); not checked for clashes with every
  mod's recipes ([briefs/food_oven.md](briefs/food_oven.md) #2).
- FD's `SkilletItem` held near a heat source ignores `lit`, so a cold oven counts for it (same as a cold stove).
- Brewin kegs count a lit oven within range as heat.
- **Fixed 2026-10-21 (patcherMod 1.1.01, not deployed):** bottles and buckets waiting in the overflow buffer
  disappeared from the GUI when the output item was taken out: the update tag left the buffer out, so each block
  update emptied the client's copy ([TODO](todo/TODO.md) `food/kitchen #14`, [remainder_buffer.md](remainder_buffer.md)
  3.4). RB-12 in game.
- **Plating delay (2026-10-21, patcherMod 1.1.01, not deployed):** a bowl recycled into the plating slot shows there
  for `[cooking] recycleShowTicks` (10 ticks) before the result is plated with it ([remainder_buffer.md](remainder_buffer.md)
  3.1, TODO `food/kitchen #13`; GameTest RB-T15, RB-14 in game). WTHIT shows the leftover slots ("Holding: ...",
  [wthit_lines.md](wthit_lines.md) 2.9).
- **Baking audit wanted (scratchpad 2026-10-21):** every baked good gets an oven recipe with flour, not wheat, and no
  leftover crafting route. Known: banana bread (remove the wheat recipe, add a baking recipe) and honey cake (add a
  baking recipe) ([TODO](todo/TODO.md) `food/oven #6`).

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
| `kubejs/startup_scripts/cooking/baking_ingredients.js` | frosting, cookie dough (baking overhaul) |
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
- [ ] **OV-06 Bake:** a sample from each table in 2.6 bakes in the oven with its bakeware.
- [ ] **OV-07 Plating:** muffins wait for paper (one per muffin, so 4 a bake); right-click with one serves them. The
  mangosteen cake no longer waits for a bowl.
- [ ] **OV-08 Pot on top:** campfire -> oven -> cooking pot: the pot cooks; put out the campfire: the pot stops.
- [ ] **OV-09 Tool slot:** only bakeware goes in, one at a time; shift-click routes it there.
- [ ] **OV-10 Automation:** a hopper or Create funnel on a side fills ingredients; another extracts only output.
- [ ] **OV-11 JEI:** "Baking" page lists the 48 recipes with the oven as catalyst and the bakeware left of the grid;
  counted ingredients show their stack size (apple pie with counts on: crust, 2 apples, sugar, sugar); clicking the GUI arrow opens
  it; "+" moves the counted amounts in.
- [ ] **OV-12 Break / burn:** breaking drops oven and contents; standing on a lit oven hurts unless sneaking.
- [ ] **OV-13 Muffins:** blueberry and cranberry muffins cook in the pot with Create dough. If not, record which
  ingredient slot JEI shows empty.
- [ ] **OV-15 Bakeware required:** no recipe bakes without its bakeware in the tool slot; the tool stays afterwards.
  No `dummy_*` recipe in JEI.
- [ ] **OV-16 Counts off (default):** JEI's apple pie shows 5 single slots (crust, 2 apple, 2 sugar-or-honey); it bakes
  with one item per slot and takes exactly those. The KubeJS log has no `over 6 slots, dropped` line (2.6.1).
- [ ] **OV-18 Counts on:** set `ovenIngredientCounts = true`, `/reload`: apple pie shows 4 slots (crust, 2 apples,
  sugar, sugar); 1 apple in the slot doesn't start, 2 does and takes exactly 1/2/1/1. Honey cake on 2 honey bottles in one
  slot gives back 2 bottles (leftover slots).
- [ ] **OV-19 cooking.md swaps:** superseded by the baking overhaul (OV-25 to OV-31). Still checks: muffins take
  Create wheat flour, not dough; no shepherd's pie in the oven's JEI page.
- [ ] **OV-17 Hammering:** iron plate on a cutting board + Engineer's Hammer -> baking sheet, the board doesn't turn,
  the hammer loses 1 durability; repeat down the chain (muffin tin, pie tin) to the cake pan. Hammer + piece in the crafting grid does the
  same and returns the damaged hammer. The hammer still rotates other blocks and still forms IE multiblocks.
- [ ] **OV-20 Muffin yield:** one oven bake of blueberry (and cranberry) muffins gives 4, plated on 4 paper; the
  FD pot still gives 2.
- [ ] **OV-23 Other-oven tooltips:** CfB oven shows "Cooks food like a furnace and a smoker"; each of the 7 Fantasy
  Furniture ovens shows "Works like a smoker" (client log: `fantasyfurniture:*/oven -> 7 matches`).
- [ ] **OV-24 CfB oven smokes:** raw ham goes into the CfB oven and comes out as smoked ham (smoker-only recipe);
  beef still cooks; cobblestone is still refused.
- [ ] **OV-25 Overhaul boot:** after a restart no KubeJS errors for baking.js; Frosting and Cookie Dough exist (cotton
  boll texture). JEI on any cake, pie or cookie shows only the new recipes plus `*_from_slices`. Uses still list the golden
  and canned recipes.
- [ ] **OV-26 Bases:** raw cake base crafts from 3 flour (or cornmeal) + 2 egg + 2 sugar and from 3 dough + egg + 2
  sugar; the oven (cake pan) bakes a baked base from 2 flour + 2 egg + 2 sugar, from 2 dough + egg + 2 sugar, and from a raw
  base. Pie crust: 3 flour + egg + milk -> 3 and 3 dough + milk -> 3 by hand. None of the bases, crust, frosting or cookie dough
  work in a Create mechanical crafter or mixer, and JEI shows no Create or IE recipe for them. Frosting: sugar + 2 milk
  -> 2. Cookie dough: dough + 2 sugar (gingerdough look).
- [ ] **OV-27 Cakes:** baked base + frosting + 2 strawberries crafts a strawberry cake; raw base + frosting + 2
  strawberries bakes one. Baked base + 3 frosting -> cake; baked base + 2 frosting + a cherry -> cake. Honey cake gives
  back 2 bottles.
- [ ] **OV-28 Pies:** crust + 2 apples + sugar + honey bottle bakes an apple pie (pie tin), the bottle lands in a
  leftover slot. No apple pie recipe in the crafting grid. Cooking pot: 2 apples + 2 sugar + empty can in the container slot
  -> Canned Apple Pie Filling; crust + filling bakes an apple pie, and also crafts one by hand. Cheesecake: crust + 2
  sweet berries + 2 sugar in the pie tin.
- [ ] **OV-29 Opened can:** eat one serving of a pie filling can; crust + that can doesn't start in the oven and
  gives nothing in the grid; a full can does both.
- [ ] **OV-30 Cookies:** cookie dough + 2 cocoa beans bakes 12 cookies on the baking sheet; no cookie craft in the grid.
  Cookie dough + 2 Vinery cherries bakes 12 cherry cookies.
- [ ] **OV-31 Kept:** 7 cake slices -> cake still crafts; golden cookie / cake / pumpkin pie still craft from the
  finished item; muffins and tarts unchanged (OV-20).
- [ ] **OV-32 Sweet Berry Pie** (recipes and can values: scratch test queued, SQ-SB-01; names and the oven screen: in game): the cheesecake shows as Sweet Berry Pie (whole, Slice of Sweet Berry Pie, Incomplete
  Sweet Berry Pie, both Display Delight plates; JEI search "cheesecake" by name finds nothing). Cooking Pot: 2 sweet
  berries + 2 sugar + empty can -> Canned Sweet Berry Pie Filling; 4 slices + empty can also craft one. Pie crust +
  full filling bakes a Sweet Berry Pie (pie tin) and crafts one by hand; an opened can does neither. Eating the can:
  4 servings, 3 hunger each, Speed 30 s.
- [ ] **OV-33 Chocolate cake spout** (recipe loaded: scratch test queued, SQ-BK-06; the Spout run: in game): a Spout with 500 mB chocolate over a Baked Cake Base on a depot gives a
  Chocolate Cake; JEI shows the filling recipe on the chocolate cake.
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
- 2026-10-19: JEI [+] fixed for 1.0.9: the ingredient slots answer `mayPickup = true` (Forge's `SlotItemHandler` said false for an empty slot, and JEI checks every crafting slot) ([plan_1.0.9.md](plan_1.0.9.md)). OV-11 checks it.
- 2026-10-19: status sweep: everything above that was STAGED for 1.0.8 went out with pack 1.0.8 (DEPLOYED 2026-10-19 10:20); the JEI [+] fix with pack 1.0.9 (DEPLOYED 15:54). OV-01 to OV-19 still pending.
- 2026-10-21 (patcherMod 1.1.01, not deployed): leftover slots no longer go blank on the client after taking the output
  (update tag carries the buffer; TODO `food/kitchen #14`, RB-12); a recycled bowl shows in the plating slot for
  `[cooking] recycleShowTicks` before plating (`#13`, RB-14); WTHIT "Holding" / "Waiting" line (`#3`). Details in
  [remainder_buffer.md](remainder_buffer.md).
- 2026-10-21: muffins bake 4 (`food/oven #5`, baking.js). CfB oven also smokes (patcherMod `CfbOvenSmokingMixin`,
  in `build/libs` only, not installed), tooltips on the CfB and Fantasy Furniture ovens (`food/oven #4`, 2.8).
  OV-20, OV-23, OV-24.
- 2026-10-22: baking overhaul (user's scratchpad): every cake, pie, cookie, pie crust and cake base recipe removed
  (slices -> whole kept) and rebuilt (2.6): cake base crafted raw or baked in the oven, crust 3 a craft (manual), new
  frosting and cookie dough items (cotton boll texture for now), cakes = base + frosting + 2 flavour (craft or oven),
  pies = crust + 2 fruit + 2 sugar or crust + canned filling (oven only; filling from the pot), cookies = dough + 2
  flavour -> 12 (oven only). Crust + filling moved from crafting (canning.js) to the oven. The oven refuses opened cans
  (patcherMod `BakingRecipe`). OV-25 to OV-31.
- 2026-10-22 (user follow-up): the hand craft filling + crust -> pie is back beside the oven one; cookie dough on
  Create Confectionery's gingerdough texture; no Create / IE recipe for the bases (crust compacting dropped, raw cake
  base manual only); cheesecake is a pie (pie tin); cherry cookie added. No cherry pie (no whole-pie item). Durian /
  pineapple yield explained in 2.6, left open.
- 2026-10-22 (user): cheesecake renamed Sweet Berry Pie and given Canned Sweet Berry Pie Filling (pot, 4 slices,
  crust + filling in the oven and by hand); durian and pineapple pies marked for a rework. OV-32.
- 2026-10-22: live checks queued as SQ-BK-00 to SQ-BK-05 ([testing/scratch_queue.md](testing/scratch_queue.md), probe
  `tools/baking_probe/baking_probe.js`: recipe audit, expected ids, no machine recipes for the bases, oven matching
  with full and opened cans).
- 2026-10-22 (user): chocolate cake spout recipe restored (baked base + 500 mB chocolate; the removals had swept
  C&A's). OV-33.
