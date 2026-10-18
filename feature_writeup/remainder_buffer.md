# Remainder buffer: keep bottles, bowls and buckets in FD-style cooking stations

When a Farmer's Delight style station finishes a recipe, it throws each ingredient's leftover container (glass
bottle, bowl, bucket) out of the block as an item. This writeup looks at whether those leftovers can go into a
buffer slot on the station instead. Java in patcherMod (and in our own `oven/` and `backportedBrewin` source).
Test prefix `RB-`.

From [cooking.md](cooking.md): kitchen > stove "bottle/bucket output slot for ingredient inputs in upgraded
cooking pot?", and drink > tea "integrate with later bottle/bowl output buffer system?".

## 0. At a glance

| Part | Status |
|---|---|
| Feasibility study (this file) | DONE 2026-10-18, from the decompiled jars the pack uses (1) |
| Design | PROPOSED (3), open decisions in 5 |
| Code | not started |

**Verdict: feasible, medium size, low risk.** Every station in scope sends its leftovers through one call site,
so each can be redirected with a single injection. No other mod in the pack hooks those call sites. Most of the
work is the GUI slot (a menu mixin and a screen mixin per station), not the buffer.

## 1. Which stations throw leftovers, and how (1.19.2 jars in the pack)

Read from the decompiled jars listed in `pack/manifest.json` (Vineflower). "Leftover" means the ingredient's
crafting remainder, plus FD's `INGREDIENT_REMAINDER_OVERRIDES` (potions and water/milk/honey bottles -> glass
bottle, stews and soups -> bowl, buckets -> bucket).

| Station | Jar | Where the leftover is thrown | Hook | Existing slot for it |
|---|---|---|---|---|
| Cooking pot | FD 1.19.2-1.2.4 | `CookingPotBlockEntity.ejectIngredientRemainder(ItemStack)` (protected), called for ingredient slots 0-5 from `processCooking` | `@Inject` at HEAD, cancellable | none |
| Kettle | Farmer's Respite 1.19-2.0 | inline `ItemUtils.spawnItemEntity(...)` in `KettleBlockEntity.processBrewing`, ingredient slots 0-1 | `@Redirect` on that call | none |
| Keg (current jar) | Brewin' and Chewin' 1.19-2.0 | inline `ItemUtils.spawnItemEntity(...)` in `KegBlockEntity.processFermenting`, ingredient slots 0-3 | `@Redirect` on that call | slot 8 already holds the **fluid-item** slot's (4) leftovers, but ingredient leftovers still fly out |
| Keg (port) | `development/backportedBrewin` (4.5.0 port, in progress) | `KegBlockEntity.ejectIngredientRemainder(ItemStack)`, line ~757 | edit our own source | none |
| Oven | patcherMod `oven/` | `OvenBlockEntity.ejectIngredientRemainder(ItemStack)` (private) | edit our own source | none |

Not in scope, checked: the FD skillet and stove grill (they pop the **finished food**, not leftovers), the
Nether's / Ender's Delight stoves (subclasses of the grill), Create Central Kitchen's blaze stove, Cooking for
Blockheads and Applied Cooking (crafting-grid style, no station inventory). Vinery's fermentation barrel takes a
wine bottle as input and gives nothing back.

### 1.1 Other mods' mixins on these classes

Create Central Kitchen 1.3.11.c (`create_central_kitchen.mixins.json`) is the only jar in the list that hooks them:

- `CookingPotBlockEntityMixin` and `KettleBlockEntityMixin`: `@Inject` at the `setRecipeUsed` call in
  `processCooking` / `processBrewing`, to signal the blaze stove. They run before the leftover loop and don't
  touch it.
- `KegBlockEntityMixin`: `@ModifyExpressionValue` in `updateTemperature`. Unrelated.

No conflict. MixinExtras 0.2.0 is in the pack (CCK jar-in-jar), but plain `@Inject` / `@Redirect` is enough here
and patcherMod doesn't use MixinExtras yet.

### 1.2 How often it happens (recipe scan of the jars)

| Recipe type | Recipes | With a leftover-bearing ingredient | Typical leftovers |
|---|---|---|---|
| `farmersdelight:cooking` | 156 | 48 | `#forge:milk` (milk bucket -> bucket, milk bottle -> bottle), tomato sauce -> bowl, honey bottle, Fruits Delight `#forge:milk/milk_bottle` |
| `farmersrespite:brewing` | 30 | 25 | every "strong" / "long" upgrade: a bottled drink + honey bottle or milk. By the code that's **two** bottles per brew (both FD and Respite drinks have a glass bottle remainder). cooking.md says only one is thrown for melon juice + honey: check in game (RB-03) |
| `brewinandchewin:fermenting` | 23 | 4 | honey bottle (pale jane), ink sacs give none |

Pack recipes from `kubejs/server_scripts/cooking/cooking.js` and the oven's baking recipes add more (milk bottles
in muffins and tarts).

### 1.3 Bug found on the way: Brewin' 2.0 keg duplicates bottles

`KegBlockEntity.processFermenting` (1.19-2.0): when slot 8 already holds the fluid item's leftover, it grows it by
`resultStack.getCount()`, not by 1. `pickled_pickles` (honey bottle fluid item, result count 2) gives back 2 glass
bottles per honey bottle after the first brew. Only that recipe is affected (the other count-2/3 keg recipes have
no fluid item). It goes away when the 4.5.0 port replaces the keg; until then a one-line `@ModifyArg` would fix it.

## 2. What the stations look like today

- **Pot** (`CookingPotMenu`): ingredients 3x2 at (30,17), meal display (124,26), container (92,55), output
  (124,55). Free space right of the output, where the keg puts its own container output slot. Automation
  (`CookingPotItemHandler`): top inserts/extracts ingredients 0-5; the other faces insert into the container slot
  7 and extract from the output 8.
- **Kettle** (`KettleMenu`): ingredients at the left, meal display (118,26), container, output (118,55). Same face
  rules as the pot.
- **Keg 2.0** (`KegMenu`): output (117,55), fluid-item leftovers (143,55). Its item handler lets the non-top faces
  extract both 7 and 8. This is the model for the design below.
- **Oven**: copies the pot layout and draws FD's `cooking_pot.png`.

Leftovers are thrown left of the pot's facing (the oven: out of the front), at y + 0.7 with an upward kick, so
they often land on the counter or roll off it.

## 3. Proposed design

### 3.1 The buffer

A small shared class in patcherMod (`cooking/RemainderBuffer`): an `ItemStackHandler` with **2 slots** at
normal stack limits (buckets 16), saved under its own NBT key
(`CERemainders`). Two slots cover the mixed cases (a milk bucket and a honey bottle in one pot recipe).

Each station's leftover call goes through one routine:

1. **Recycle.** If the leftover is the item the current meal is served in and the container slot is empty or
   holds that item, put it in the container slot. "Served in" is the station's own `isContainerValid` test: the
   recipe's `container` (`mealContainerStack` / `drinkContainerStack`, set just before the loop), or else the
   result's crafting remainder (hot cocoa has no `container` field; it is served in its remainder, a bottle). The next tick plates the meal with it. Example: kettle strong
   green tea takes green tea + honey bottle, and is served in a glass bottle. Both leftover bottles come back:
   one plates the drink, one goes to the buffer. This answers the tea item in cooking.md without touching the
   recipes.
2. **Buffer.** Otherwise put it in the buffer.
3. **Full.** See 3.2.

### 3.2 When the buffer is full

Recommended: **the station stalls** like it does when its meal slot is full. Inject at RETURN of `canCook` /
`canBrew` / `canFerment`: if the result is true, work out this craft's leftovers (the same rule as the loop: the
stack's crafting remainder, else the override map) and simulate steps 1 and 2. If they don't all fit, return
false. The station then holds its progress at 0, as FD already does when the output is full. A hopper or funnel
pulling from the side keeps it running indefinitely.

Alternative: throw the extra item as it does now (no stall). Cheaper (no `canCook` hook) but it brings the
problem back once the buffer fills.

### 3.3 Getting the items out

- **GUI**: two take-only slots right of the output, column x = 150 (pot) / 144 (kettle) / oven same as the pot,
  rows y = 37 and 55. The slot frame comes from blitting the container slot's own 18x18 from the station's
  texture (no new texture). Shift-click moves them to the player inventory. Menu mixin: `addSlot` at the end of
  the constructor (indexes after the player inventory, so FD's index maths stay valid) plus a `quickMoveStack`
  HEAD inject for those indexes. Screen mixin: one blit per slot in `renderBg`.
- **Automation**: the non-top faces can extract the buffer, after the output slot, as on the 2.0 keg. Pot and
  kettle: wrap the side/bottom capability in `getCapability` (cached `LazyOptional`, invalidated in
  `invalidateCaps`). A hopper under a pot then pulls meals and bottles alike, so filter it if needed.
- **Breaking**: inject at RETURN of `getDroppableInventory` so the buffer drops with the rest. The pot's item form
  only keeps the meal, as now.
- **Optional**: a WTHIT/Jade line "Holding: 3 Glass Bottle" (patcherMod already has `waila_plugins.json`).

### 3.4 Size of the job

| Station | Pieces | Notes |
|---|---|---|
| Shared | `RemainderBuffer`, a `RemainderBufferHolder` duck interface, the routing + simulate helper, CEConfig switch | ~150 lines |
| Pot | BE mixin (field, load/save, eject HEAD, canCook RETURN, drops, capability), menu mixin, screen mixin | 3 mixins |
| Kettle | same, with `@Redirect` instead of the HEAD inject | 3 mixins |
| Keg | do it in `backportedBrewin` source, not as a mixin on the 2.0 jar that the port replaces | native; port not compiling yet |
| Oven | native: inventory slots 10-11, menu, screen, item handler | own code |

About 7 mixins and 400-600 lines in total. The oven's GameTest setup (8 server tests) can be reused: craft with a
milk bottle, check the bottle is in the buffer, fill the buffer, check the station stalls, pull with a hopper.

## 4. Risks

- **Menu sync**: the extra slots exist only where patcherMod is loaded. It is `both` in
  [mod_sides.md](../mod_sides.md) and required on both sides, so this is fine.
- **Removing the mod** drops nothing: items in the buffer are lost with the NBT key. Same as any modded
  inventory.
- **Stall surprises**: a player who never empties the slot gets a pot that stops after 32-128 crafts. The stalled
  station should say why (a tooltip on the slot, or a WTHIT line).
- **FD/Respite updates**: the pack pins its versions. The HEAD inject on `ejectIngredientRemainder` is stable; the
  two `@Redirect`s depend on the inline code staying the same.
- **Other mixins**: only the jars listed in 1 were checked. Run a quick `javap` scan of `mods/` for other targets
  on `CookingPotBlockEntity` / `KettleBlockEntity` / `CookingPotMenu` before building.

## 5. Decisions needed

| # | Question | Recommendation |
|---|---|---|
| D1 | Buffer full: stall, or throw the extra? | stall (3.2), with a CEConfig switch |
| D2 | Recycle leftovers into the container slot when they match? | yes (3.1 step 1) |
| D3 | Slots per station | 2 |
| D4 | Bottom/side automation can pull the buffer? | yes, after the output slot |
| D5 | Keg: wait for the port, or mixin the 2.0 keg now (and fix 1.3)? | wait for the port; fix 1.3 only if the port slips past 1.0.7 |

## 6. Tests (drafted, not run)

| Id | Check |
|---|---|
| RB-01 | Pot: hot cocoa with a milk bottle. No item on the ground; the bottle plates the cocoa (recycle) |
| RB-02 | Pot: a milk-bucket recipe. The bucket is in a buffer slot |
| RB-03 | Kettle: strong green tea, then strong melon juice. One bottle plates the drink, one is in the buffer. Also count what stock throws for melon juice + honey (code says 2, cooking.md says 1) |
| RB-04 | Fill both slots with different items, then a third kind: the station stalls; empty a slot and it resumes |
| RB-05 | Hopper under the pot pulls meals and then the buffer |
| RB-06 | Break the pot: buffer contents drop |
| RB-07 | Shift-click a buffer slot: moves to the player inventory; nothing can be placed into it |
| RB-08 | Save, quit, reload: buffer contents kept |
| RB-09 | CCK blaze stove under the pot and kettle still signals |
| RB-10 | Oven: muffin with milk bottle, bottle buffered |
