# Brief: food / kitchen

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# food`, Sub `kitchen`. Reference writeups:
[remainder_buffer.md](../remainder_buffer.md) (pot, kettle, oven leftover slots), [oven.md](../oven.md),
[wthit_lines.md](../wthit_lines.md) 4.2 F9, [testing/fridge_sort/plan.md](../testing/fridge_sort/plan.md).
Code: patcherMod `cooking/` (`RemainderBuffer`, `RemainderBufferStorage`, `RemainderOutputHandler`, `RemainderSlot`),
`mixin/CookingPotRemainderMixin`, `KettleRemainderMixin`, the menu / screen mixins, `oven/OvenBlockEntity`, `oven/OvenMenu`;
GameTests in `tools/remainder_buffer_test/` (`RemainderBufferTests.java`, run by `tools/test_remainder_buffer.sh`).

Paths below are relative to `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/` unless they
start with `kubejs/`, `config/`, `tools/` or `feature_writeup/`.

## Scope and order

Open rows: Bugs #4, #5, #14; Planned #3, #6, #8, #10, #11, #12, #13. #1 and #7 are Testing (table at the end).

**Findings that change the rows** (details per row):

- **#14 has a clear cause in the code:** the oven's `getUpdateTag` leaves out `Remainders`, and its `load` reads
  `Remainders` without checking that the key is there. Every block update (taking the output is the usual one) wipes
  the client's copy of the two leftover slots. The server still has the items. One-file fix.
- **#4: no duplication path found** in the routing, FD's leftover loop or the menu code. The bowls add up in every case
  traced. The note fits the designed recycle (the bowl-food ingredient's bowl now comes back instead of being thrown).
  It needs a count test before any code change. Do it together with #13, which touches the same step.
- **#5 is not in the remainder buffer.** The note (commit `908e831`) is about the container a food gives back *when
  eaten* or when a feast block empties ("returned food serve item (bowl, paper for popcorn bucket)"). Every path
  checked builds a plain stack. The NBT has to be found in game first.
- **#8 has two config causes, verified against the Cooking for Blockheads 13.3.4 jar:** the oven takes no ordinary
  fuel because `config/cookingforblockheads-common.toml` has `ovenRequiresCookingOil = true`, and the sink gives no
  water because `sinkRequiresWater = true`.

**Build groups and order:**

1. **#14** (oven sync, patcherMod, ~6 lines + 1 GameTest). Ship first: it is a visible "items vanish" bug.
2. **#4 test, then #13 + #3** (patcherMod, all in the remainder-buffer code): add the bowl-conservation GameTest. If
   it passes, close #4 as working as designed. Build the short plating delay (#13), which makes the recycle visible
   and answers what #4 saw. Add the WTHIT "Holding" line (#3) in the same jar.
3. **#8 config + tags** (no Java): oven fuel and sink water. Then the cutting-board check. Corner storage is a decision.
4. **#12, #10 (bamboo bowl, mortar recipes)**: KubeJS recipe rows, any time.
5. **#5** after an in-game NBT look (one command, see the row).
6. **#6 fridge model, #10 scythe / stepstool, #11 decor**: art and design work. The scythe and stepstool need a
   scope decision first.

Every patcherMod change needs a pack restart and a jar build (`./gradlew :patcherMod:build`). Run
`tools/test_remainder_buffer.sh` after any change under `cooking/` or `oven/`.

---

## kitchen #14: Oven overflow buffer loses bottles and buckets
Status Bug / Work patcherMod / TODO `food/kitchen #14`

### Goal
Bottles and buckets in the oven's two leftover slots stay visible after the output is taken, and can always be taken.

### Now
- `oven/OvenBlockEntity.java`:
  - line 83: `remainders = new RemainderBuffer(() -> setChanged())`. A buffer change marks the block entity dirty
    but sends no block update.
  - `load` (lines 415-427), line 418: `remainders.deserializeNBT(compound.getCompound("Remainders"))`, with no
    `contains` check.
  - `getUpdateTag` (lines 443-448) writes only `Container` and `Inventory`. `getUpdatePacket` (452-454) sends that tag.
  - `inventoryChanged` (456-461) is called from every `inventory` slot change (`createHandler`, 487-493) and from the
    tick. It calls `sendBlockUpdated`, which sends the block-entity data packet.
- On the client, Forge's default `onDataPacket` / `handleUpdateTag` call `load(tag)`. With no `Remainders` key,
  `getCompound` returns an empty tag. `ItemStackHandler.deserializeNBT` then runs `setSize(stacks.size())`, which
  makes a new, empty list. **The client's two leftover slots are wiped on every block update.**
- Why the items seem to go "when the output is taken": a bake changes the buffer and the inventory in the same tick.
  The block-entity packet (wipe) goes out first in the next tick, then the open menu's `broadcastChanges` sends the
  changed buffer slots, so they show again. Taking the output changes only slot 9. The block update wipes the client
  buffer, and the server menu sends nothing for slots 46-47, because to the server they haven't changed. Adding an
  ingredient or a hopper insert does the same.
- The server copy is untouched. Reopening the GUI resyncs every slot, a hopper under the oven still pulls them, and
  breaking the oven drops them. Clicking the "empty" slot also hands the item over (the server runs the click on its
  own state).
- The pot and kettle don't have the bug. FD's `getUpdateTag` is `writeItems`, which calls `super.saveAdditional`.
  Forge writes `ForgeCaps` (the buffer's `RemainderBufferStorage` provider) there, and Forge's `BlockEntity.load` only
  reads `ForgeCaps` when the key is present. (Verified from the decompiled FD 1.2.4 / Respite 2.0 jars; that Forge
  writes caps in `saveAdditional` is shown by GameTest RB-T7, which finds `ForgeCaps` in `saveWithFullMetadata`.)

### Build
1. `OvenBlockEntity.getUpdateTag`: add `compound.put("Remainders", remainders.serializeNBT());`.
2. `OvenBlockEntity.load`: guard the read: `if (compound.contains("Remainders")) remainders.deserializeNBT(...)`.
   This keeps an older or partial tag from wiping it. Do the same for `Container` (line 421) if you like, though it is
   always sent.
3. Line 83: `new RemainderBuffer(this::inventoryChanged)`, so a buffer change (a hopper or shift-click taking a bottle
   while no GUI is open) also reaches the client copy. That matters for #3 if the WTHIT line ever reads the client
   block entity. Cost: one block update per change, the same as any slot change.
4. GameTest **RB-T11 `ovenUpdateTagKeepsBuffer`** in `RemainderBufferTests.java`: put a glass bottle in
   `oven.getRemainders()` slot 0, then `CompoundTag t = oven.getUpdateTag();`. Make a second `OvenBlockEntity` at the
   same position and state, put a bucket in its buffer, `load(t)`, and check that slot 0 is the bottle. Also add
   **RB-T12 `potUpdateTagHasBuffer`**: `pot.getUpdateTag()` contains `ForgeCaps/creativeengineering:remainder_buffer`.
   This pins the assumption the pot and kettle rely on.

### Ids and data
No ids. NBT key `Remainders` (the oven's own save key, unchanged).

### Decisions
- Send the buffer in the update tag (above) or keep it menu-only and just guard `load`. **Recommend both**: the guard
  alone fixes the wipe, but with step 3 a client copy that stays current is what a client-side line (#3) or a
  renderer would need.

### Gotchas
- The client's slot also goes empty in a dev single-player world: the integrated server and the client have separate
  block entities.
- Don't put `RecipesUsed` in the update tag. It isn't needed on the client.

### Tests
- RB-T11, RB-T12 (offline, above).
- In game: **RB-12 Oven leftovers stay visible**: bake blueberry muffins (milk bottle) with the GUI open, take the
  muffins out, and the bottle stays in the leftover slot. Close and reopen: still there. Then take it with a click.

---

## kitchen #4: Duplicate bowl when popping into the buffer
Status Bug / Work patcherMod / TODO `food/kitchen #4`

### Goal
Find out whether a bowl is really duplicated. Fix it if so; otherwise make the recycle readable (#13) and close the row.

### Now
- Source note (cooking plan, added in commit `2e8687f`, 1.0.8): "duplicate bowl given when popping into output
  buffer (can also pass into plating input at the same time)". No station or recipe is named. Bowls point to the
  FD cooking pot; the oven's only bowl recipe is the mangosteen cake (bowl plating, no bowl-leaving ingredient).
- Traced paths, all conserving:
  - FD `CookingPotBlockEntity.processCooking` (decompiled 1.2.4): sets `mealContainerStack`, adds the result to slot
    6, then per ingredient slot 0-5 calls `ejectIngredientRemainder` once (crafting remainder, else
    `INGREDIENT_REMAINDER_OVERRIDES`; never both), then `shrink(1)`.
  - `mixin/CookingPotRemainderMixin.ce$keepRemainder` (lines 75-86): `store()`, then cancels, or shrinks FD's stack to
    what didn't fit.
  - `cooking/RemainderBuffer.store` / `plan` (lines 60-114): each leftover goes to exactly one place: the container
    slot (only up to `meals waiting - containers in the slot`), then a matching buffer stack, then an empty one, then
    overflow. `merge` returns the rest; no stack is counted twice.
  - FD `useStoredContainersOnMeal` in the same tick plates one meal per container (`shrink` on both).
  - Menu: `CookingPotMenuRemainderMixin.ce$quickMoveRemainder` (lines 174-190) copies, moves, then `slot.set(rest)`.
    Vanilla's shift-click loop stops when the move fails. Click and double-click go through `SlotItemHandler.remove`
    / `extractItem`.
  - Pot client sync: the update tag carries `ForgeCaps` (see #14). The client copy is set, never added to.
  - Breaking: FD's loot is `farmersdelight:copy_meal` (meal only, no `ForgeCaps`), so the buffer isn't dropped
    twice.
- What the note likely saw: a pot recipe with a bowl-food ingredient (tomato sauce, a stew, bone broth) served in a
  bowl. Before 1.0.7 that ingredient's bowl flew out of the pot's left side, easy to miss. Now it either goes straight
  to the plating slot and is used at once (container slot empty: "pass into plating input at the same time") or
  lands in a leftover slot while the plating slot's own bowl is used ("given when popping into output buffer"). Both
  look like a bowl from nowhere, but the count is the same as before: one bowl in the ingredient, one bowl on the meal.

### Build
1. GameTest **RB-T13 `potBowlConservation`**: use a pot recipe with one bowl-leaving ingredient and a bowl container.
   The test datapack in `tools/remainder_buffer_test/src/main/resources/data/` can add one, e.g. FD tomato sauce +
   anything -> a bowl-served FD meal. Run it four ways: container slot empty, holding 1 bowl, holding 10 bowls, and
   with a meal already waiting. Before and after the cook, count bowls in the container slot + buffer + ingredient
   bowl-foods + plated meals + `thrown(h, Items.BOWL)`. The totals must match.
2. Repeat for the kettle with a bottle (strong tea, already RB-T9's shape) and for the oven (mangosteen cake with a
   bowl in the plating slot).
3. If all pass: close #4 as working as designed, and say in remainder_buffer.md 3.1 that a bowl-food ingredient's
   bowl now comes back (it used to be thrown). Build #13, so the step shows.
4. If one fails, the failing case names the path. Look first at `plan()`'s `containersNeeded` when the container slot
   holds a *different* item (it subtracts that item's count; harmless as far as traced) and at
   `CookingPotRemainderMixin` setting `remainder.setCount(rest)`. That changes FD's own stack, which is fresh from
   `getCraftingRemainingItem`, unless some mod's item returns a shared one.

### Ids and data
`minecraft:bowl` (`i.bowl`). Recipe candidates for the in-game check: FD meals that take `farmersdelight:tomato_sauce`
(bowl remainder), and `kubejs/server_scripts/cooking/cooking.js` line 19 (`bone_broth`, served in `i.bowl`).

### Decisions
- If it really is the recycle: keep it (it is the remainder-buffer design, D2) or stop recycling bowls into the plating
  slot (they would always go to the buffer). **Recommend keep + #13 delay.**

### Gotchas
- Count with creative mode off: FD's `useHeldItemOnMeal` and the menus behave differently in creative.
- Create Central Kitchen's blaze stove automation (`create_central_kitchen` 1.3.11.c) only hooks `setRecipeUsed`
  (remainder_buffer.md 1.1). Test once with and once without it under the pot, in case the user saw it on a CCK setup.

### Tests
RB-T13 (offline). In game: **RB-13 Bowl count**: 10 bowls in the pot's plating slot, cook 5 of a tomato-sauce bowl
meal, and the bowls in slot + leftover slots + meals add up to 15.

---

## kitchen #5: Returned serve items don't stack
Status Bug / Work KubeJS / patcherMod / TODO `food/kitchen #5`

### Goal
A bowl or paper handed back after eating (or by an emptied feast block) stacks with ordinary bowls and paper.

### Now
- Source note (commit `908e831`, 1.0.7 cooking plan): "returned food serve item (bowl, paper for popcorn bucket)
  dont stack with normal items, extra nbt?". That is the container given back on eating or serving, not the remainder
  buffer.
- Paths checked, all building a plain stack (no tag):
  - Corn Delight 1.0.3 `PopcornBoxBlock.takeServing` (the "popcorn bucket" is `corn_delight:popcorn_box`): gives
    `new ItemStack(Items.PAPER)` when the last serving is taken. Verified (decompiled).
  - `corn_delight:caramel_popcorn` / `popcorn` are mmlib `ItemFoodBase`. `finishUsingItem` hands back
    `getCraftingRemainingItem(stack)`, Forge's default `new ItemStack(item)`. Verified (mysterious_mountain_lib
    1.2.3).
  - FD `ConsumableItem.finishUsingItem` returns `stack.getCraftingRemainingItem()`. Vanilla `BowlFoodItem` returns
    `new ItemStack(Items.BOWL)`.
  - The pack's own eating hook `food/CanServings.finishServing` handles cans only.
  - `kubejs/startup_scripts/items/potion_buckets.js` line 35 and `func/potion_area.js` line 82 call
    `stack.getOrCreateTag()`, which adds an empty `{}` tag. That only touches potion buckets and their clouds, not
    bowls. It is still a pattern to avoid in any hook that sees other stacks.
- So the tag (or a missing one: the *normal* stack could be the odd one) comes from somewhere not traced. Candidates:
  a mod hooking eaten food or item entities. ItemPhysic and Cherished Items mix into `ItemEntity`; Cherished Items
  1.0.2 only changes despawn time, checked. Also any recipe output with an empty `nbt: {}`. A plain bowl crafted by
  a recipe that writes `{}` would refuse to stack with an eaten-stew bowl.

### Build
1. In game: hold the returned bowl, then a normal one, and run `/data get entity @s SelectedItem` (or `/kubejs hand`)
   for each. Note the `tag` difference.
2. From the tag, find the writer. `grep -r "<key>"` over `kubejs/`, then the decompiled jar of the mod the key names.
   A bare `{}` (empty) tag usually means some `getOrCreateTag()` with nothing written. Then look for that call on
   the path the item took (eat, feast block, drop).
3. Fix at the source. If it's a pack script, remove the `getOrCreateTag()` (read with `stack.hasTag() &&` first). If
   it's a mod, a patcherMod mixin on that call. A blanket "strip empty tags from bowls / paper / bottles on inventory
   change" in KubeJS works too, but hides the cause. Only use it if the writer is a mod that can't be patched cleanly.

### Ids and data
`minecraft:bowl` (`i.bowl`), `minecraft:paper` (`i.paper`), `corn_delight:popcorn_box` (`i.popcorn_box`),
`corn_delight:caramel_popcorn` (`i.caramel_popcorn`).

### Decisions
None until the tag is known.

### Gotchas
- Check both "returned" stacks: the bowl from a soup and the paper from the popcorn box can have different causes.
- An item tag written on the *normal* stack (for example by a recipe output) looks the same as one on the returned
  stack. Compare both.

### Tests
**KS-01 Returned containers stack** (new): eat a mushroom stew, a FD bowl meal, and a caramel popcorn; empty a
popcorn box. Each bowl / paper merges into a stack of crafted bowls / paper.

---

## kitchen #13: Short delay before the bowl pops into the buffer
Status Planned / Work patcherMod / TODO `food/kitchen #13`

### Goal
The pot reads step by step: the cooked food shows in the meal slot, the recycled bowl sits in the plating slot for a
moment, then the meal is plated.

### Now
- The recycle (`RemainderBuffer.store` step 1) puts the bowl into the container slot inside `processCooking`, and FD's
  `cookingTick` plates in the same tick (`useStoredContainersOnMeal`, called when the meal and the container slot are
  both non-empty). The player never sees the bowl in the slot.
- Same shape in the kettle (Respite `KettleBlockEntity.brewingTick` -> `useStoredContainersOnMeal`) and the oven
  (`OvenBlockEntity.serverTick` lines 122-131 -> `useStoredContainersOnMeal`, 276-293).

### Build
1. `RemainderBuffer`: `store(...)` reports whether it recycled into the container slot (return a small result or set
   a flag the caller reads). Keep the current signature for the stall check (`fits`).
2. Holder state: add `int ce$plateDelay` next to the buffer. For the pot and kettle it is a mixin `@Unique` field on
   the block entity. For the oven it is a plain field. Set it to the delay when step 1 recycled.
3. Delay:
   - Pot: `@Inject(method = "useStoredContainersOnMeal", at = @At("HEAD"), cancellable = true)` in
     `CookingPotRemainderMixin` (private method in FD 1.2.4; with `remap = false` the name is the same in dev and
     pack, as it is FD's own). If `ce$plateDelay > 0`, decrement and cancel.
   - Kettle: the same on Respite's `KettleBlockEntity.useStoredContainersOnMeal` (private, no arguments in the
     decompiled 2.0 jar, called from `brewingTick`; same in FD's pot).
   - Oven: in `serverTick`, skip `useStoredContainersOnMeal` while the counter runs.
   - Don't save the counter. A reload simply plates at once.
4. Config: `CEConfig.Cooking` (lines 778-804) gets `recycleShowTicks` (int, default 10 = 0.5 s, 0 = off). Write it to
   `config/creativeengineering-common.toml` `[cooking]` (lines 1629-1639).
5. Only recycled containers wait. A player or hopper putting bowls in the slot plates at once, as now.

### Ids and data
Config key `[cooking] recycleShowTicks`.

### Decisions
- How long: **10 ticks** (row "how long"). Longer holds the pot's next recipe check while the meal slot is full.
- Delay the leftover-slot path too (the bowl appears in the leftover slot a moment after the meal)? **No**: the
  leftover slot already shows it; only the plating step is invisible.

### Gotchas
- While the delay runs, `canCook` still sees the meal slot as holding the new meal, so a long delay can hold the next
  cook (the meal slot fills).
- A hopper inserting bowls during the delay merges into the recycled stack. Plating then uses those too (fine).
- `fits()` already counts the containers in the slot, so the stall check needs no change.

### Tests
GameTest **RB-T14 `potRecycleDelay`**: hot cocoa with a milk bottle; one tick after the cook the bottle is in the
container slot and the output is empty; after `recycleShowTicks` the cocoa is plated. In game: **RB-14** watch the
pot: the bowl is seen in the plating slot for half a second.

---

## kitchen #3: Remainder buffer WTHIT line "Holding: ..."
Status Planned / Work patcherMod / TODO `food/kitchen #3`

### Goal
Looking at a pot, kettle or oven shows "Holding: 3 Glass Bottle, 1 Bucket" when its leftover slots aren't empty, and
says it is stalled when they are full.

### Now
- Not built (remainder_buffer.md 3.3, wthit_lines.md 4.2 F9).
- Pattern to copy: `compat/WthitKitchenStation.java` (common + client plugin: `registrar.blockData` on the server
  writes a value into `data.raw()`, `registrar.body` on the client reads it). It is registered in
  `src/main/resources/waila_plugins.json` (`creativeengineering:kitchen_station`, `"side": "*"`, `required`).
- Buffer access: pot and kettle via `cooking/RemainderBufferHolder.ce$getRemainderBuffer()`, oven via
  `OvenBlockEntity.getRemainders()`.
- WTHIT's generic Forge item-handler line may already list the oven's buffer: its capability handler is the same
  on every face and includes the two leftover slots. Check in game before adding the oven.

### Build
1. New `compat/WthitRemainderBuffer.java`: `IWailaCommonPlugin`, `IWailaClientPlugin`, `IBlockComponentProvider`,
   `IDataProvider<BlockEntity>`. Register `blockData` and `body` for `CookingPotBlockEntity`, `KettleBlockEntity` and
   `OvenBlockEntity`.
2. `appendData`: write the two stacks (`ItemStack.save`) under `creativeengineering.remainders`, plus a `stalled`
   boolean if the station has a recipe and `fits` fails. Simplest stall check: both slots non-empty, and the station's
   `cookTime` is 0 while its ingredient slots hold something.
3. `appendBody`: one line `Holding: <count> <name>, ...` (gray), and `Waiting: leftover slots full` (red) when stalled.
   Lang keys in patcherMod `en_us.json`: `creativeengineering.wthit.remainders.holding`, `...stalled`.
4. Toggle: `CEConfig.Wthit` gets `remainderBuffer` (default true), like `kitchenStation`.
5. `waila_plugins.json` entry `creativeengineering:remainder_buffer`, `"side": "*"`, `required`: `farmersdelight`
   (the kettle part needs `farmersrespite`: register the kettle only when `ModList.get().isLoaded("farmersrespite")`,
   or make two entries).

### Ids and data
FD `vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity`, Respite
`umpaz.farmersrespite.common.block.entity.KettleBlockEntity` (verified class names, decompiled jars).

### Decisions
- Build it? (row: "whether"). **Recommend yes, small**: it is the only hint for the stall (remainder_buffer.md 4,
  "Stall surprises").

### Gotchas
- Don't read the client block entity for the oven until #14 is in. Use server data (as above) anyway.

### Tests
**WL-xx Holding line** in wthit_lines.md: pot with a bucket in a leftover slot shows "Holding: 1 Bucket"; with both
slots full and a recipe waiting, the red line.

---

## kitchen #8: Counters, stove, sink
Status Planned / Work patcherMod / TODO `food/kitchen #8`

### Goal
Cooking for Blockheads kitchen parts work in this pack: the oven burns normal fuel, the sink gives water, a cutting
board sits on a counter, corner pieces can store things.

### Now (Cooking for Blockheads 13.3.4, verified against the decompiled jar)
- **Oven fuel**: `OvenBlockEntity.isItemFuel` (and `getBurnTime`) only accept `#balm:cooking_oil` while
  `ovenRequiresCookingOil = true`. That is set in `config/cookingforblockheads-common.toml` ("requires Pam's
  Harvestcraft"). Balm 4.6.0's `#balm:cooking_oil` holds only `#forge:cookingoil` (optional), and nothing in the pack
  fills it as far as the KubeJS tags go. So the oven takes no fuel at all. Rustic Delight's `rusticdelight:cooking_oil`
  (`i.cooking_oil`) exists and is made in `kubejs/server_scripts/cooking/cooking.js` lines 390-395. With oil, one item
  burns 800 x `ovenFuelTimeMultiplier` (0.33) = 264 ticks.
- **Sink**: `sinkRequiresWater = true` in the same file, so the sink only gives water piped in.
- **Cutting board on a counter**: `CounterBlock.use` returns `FAIL` on the server for a click on the top face with
  any `BlockItem` (or CfB's own cutting board), so the item's own placement runs. Placing `farmersdelight:cutting_board`
  on top should work. Not tested in game.
- **Corner**: `CornerBlockEntity` only provides `IKitchenConnector` (no inventory). Farmhouse Decorations'
  `kitchen_corner_cabinet` / `kitchen_corner_wall_cabinet` have no block entity at all (checked).

### Build
1. Oven fuel. Pick one:
   - (a) `config/cookingforblockheads-common.toml` `ovenRequiresCookingOil = false`: any furnace fuel, burning at 0.33x.
   - (b) keep oil-only and give it oil: in `kubejs/server_scripts/tags/cookingtags.js` add
     `t.add(e, i.t_forge_cookingoil, i.cooking_oil)` with a new dict key `i.t_forge_cookingoil = '#forge:cookingoil'`
     (item_dict.js, next to `i.t_cooking_oil_ingredients` at line 134).
   Either way, set the same value in `defaultconfigs/` if the pack copies CfB's config there (it isn't there now).
2. Sink: `sinkRequiresWater = false` for infinite water, or keep it and document piping (a Create pump or a FD/Supplementaries
   faucet into it). "Rework like a well" means a new block. Not recommended.
3. Cutting board: test in game first. If it does fail, the likely reason is a client-only refusal; a patcherMod mixin
   on `CounterBlock.use` that returns `PASS` for the up face with a `BlockItem` would fix it.
4. Corner storage: needs a new block entity with an inventory and a menu (a patcherMod mixin on `CornerBlock` /
   `CornerBlockEntity`, or a new patcherMod block extending CfB's `CounterBlock` with a corner model). Medium work.

### Ids and data
`cookingforblockheads:oven`, `:sink`, `:counter`, `:corner` (no item_dict keys yet; add them under a
`// ===== cookingforblockheads =====` block in item_dict.js and block_dict.js using the file's
`i.<name> = 'ns:id'` style, e.g. `i.cfb_oven`, `i.cfb_sink`). `#balm:cooking_oil`, `#forge:cookingoil`,
`rusticdelight:cooking_oil`.

### Decisions
- Oven fuel: **(b) oil only, with Rustic Delight oil in the tag**. It keeps CfB's design and gives the oil a use.
  Also make the oven smoke (oven #4 in [food_oven.md](food_oven.md)).
- Sink: **`sinkRequiresWater = false`**: the pack has wells and pumps elsewhere, and a kitchen sink that needs piping
  is mostly confusing.
- Corner storage: **defer**; it is the only Java-heavy part of the row.

### Gotchas
- CfB's oven puts the fuel's container back only when the fuel stack runs out (`fuelItem.shrink(1)`; the remainder
  replaces the slot only if it is empty). With a stack of oil bottles, every bottle but the last is lost. Fine as a
  design choice; worth a tooltip.
- The `.toml` is a common config, read at load: restart after changing it.

### Tests
**KT-01 CfB oven fuel**: oil (or coal) starts the oven. **KT-02 Sink**: right-click with a bucket fills it.
**KT-03 Board on counter**: a FD cutting board places on a counter's top and cuts.

---

## kitchen #6: Fridge
Status Planned / Work patcherMod / resource pack / TODO `food/kitchen #6`

### Goal
The 2-tall Cooking for Blockheads fridge looks like a full block from the sides, so decor on top and blocks next to it
line up.

### Now
- CfB fridge models (13.3.4 jar): `fridge_large_lower` / `fridge_large_upper` elements run x 1-15, z 2-15, and the
  upper's top stops at y 13 (`Static_Top` [3,11,3]-[13,13,15]). The door models are separate (`fridge_large_door*`).
  `FridgeBlock` defines no shape of its own (no `getShape` override found).
- Item loss on sort is fixed (row #7, `compat/QuarkSafeSort` + `mixin/QuarkSortingHandlerMixin`).

### Build
1. Resource override in the pack's KubeJS assets: `kubejs/assets/cookingforblockheads/models/block/fridge_large_upper.json`
   and `fridge_large_lower.json`, copied from the jar with the side and top elements stretched to 0-16 (x) and the
   top to y 16. Do the same for the door models' widths. The textures stay.
2. In game, check that the door animation still lines up (the door is drawn by `FridgeRenderer`; its offsets may assume
   the old width).
3. If the visual also needs the collision box full, it already is (no custom shape).

### Ids and data
`cookingforblockheads:fridge` (no dict key; add `i.cfb_fridge`).

### Decisions
- Stretch only the 2-tall form, or the 1-tall `fridge.json` too? **Both**, for one look.

### Gotchas
- The fridge is dyeable (`BlockDyeableKitchen`); the colour variants share the models through the blockstate. Check
  `blockstates/fridge.json` before assuming one model per form.

### Tests
**KT-04 Fridge look**: a 2-tall fridge between two counters: no gaps; a candle on top sits on its top face.

---

## kitchen #10: Kitchen tools
Status Planned / Work KubeJS / patcherMod / TODO `food/kitchen #10`

### Goal
Four small tools: a bamboo bowl recipe, the Old Guns mortar and pestle for food grinding, a scythe for area harvest,
and a stepstool for reach.

### Now
- Bowls: `i.bowl` = `minecraft:bowl`; `i.bamboo` = `minecraft:bamboo` (item_dict.js line 698). No bamboo bowl
  recipe exists.
- Mortar and pestle (Old Guns 3.7.3, verified): `oldguns:mortar_and_pestle`, 512 durability, `getCraftingRemainingItem`
  returns a copy of itself. Its own recipes use type `oldguns:mortar_and_pestle_shapeless` (e.g.
  `oldguns:sulfur_from_netherrack_vanilla`: 3 `#forge:netherrack` + the mortar -> sulfur). It is crafted on the
  Gunsmith's Bench (`oldguns:gunsmiths_bench_shaped`, polished granite + lever). No dict key yet.
- Scythe: only `mca:scythe` (`i.scythe`, MCA's reaper item) is in the pack. No area-harvest tool.
- Stepstool: nothing.

### Build
1. Bamboo bowl: `R.shaped(i.bowl, ['B B', ' B '], { B: i.bamboo }, e, 'bowl_from_bamboo')` (the vanilla bowl
   pattern, 3 bamboo -> 1 bowl? or -> 4 like planks). In a kitchen server script, e.g. `kubejs/server_scripts/cooking/cooking.js`.
2. Mortar food grinding: add `i.mortar_and_pestle = 'oldguns:mortar_and_pestle'` to item_dict.js (Old Guns block,
   near line 12101). Then either:
   - extend NCH with `oldgunsMortar(output, inputs, e, idOverride)`, building the JSON `{ type:
     'oldguns:mortar_and_pestle_shapeless', ingredients: [...inputs, { item: mortar }], result }` through
     `e.custom(...)` with id `creativeengineering:oldgunsmortar/<id>`. It uses Old Guns' own damage handling. Or
   - `R.shapeless(out, [inputs..., i.mortar_and_pestle], e, id, [i.mortar_and_pestle, 1])` (NCH's existing
     `damage` argument, `damageIngredient`).
   **Recommend the NCH helper**: JEI then lists them with Old Guns' mortar recipes, and the damage rule is the mod's.
   Recipes: the "food grinding" list (cooking plan "mortar and pestle"): e.g. wheat -> flour, sugar cane -> sugar,
   cocoa -> cocoa powder, salt crystals -> salt, dried herbs -> spice. Each needs the id of its output from the dict.
3. Scythe and stepstool: new items, so design first (decisions). Scythe in patcherMod: an `Item` subclass whose
   `useOn` harvests mature `CropBlock`s in an N x N square, N by tier (wood 3, iron 5, diamond 7, netherite 9), replanting
   like FD's right-click harvest. Register in patcherMod, with recipes via `R.shaped`. Stepstool: a block that gives
   `ForgeMod.REACH_DISTANCE` +1 to a player standing on it (a `LivingTickEvent` check of the block below, as an
   attribute modifier with a fixed UUID, removed when off it).

### Ids and data
`minecraft:bowl`, `minecraft:bamboo`, `oldguns:mortar_and_pestle` (new key), recipe type
`oldguns:mortar_and_pestle_shapeless` (JSON shape verified from the jar).

### Decisions
- Scope (row: "scope"): **recipes now (bamboo bowl, mortar grinding); scythe and stepstool as their own rows later**.
  Each is a new item with balance questions (area vs hoes; reach).
- Bamboo bowl yield: **1 bowl from 3 bamboo** (same as planks -> 4 would make bamboo the cheap route).

### Gotchas
- The mortar returns itself as the crafting remainder, so as an FD pot or oven ingredient the remainder buffer would
  hold the mortar. Don't use it in station recipes.
- The Gunsmith's Bench gate: the mortar is not craftable in a plain table. Decide whether kitchen use needs a vanilla
  recipe for it.

### Tests
**KT-05 Bamboo bowl** crafts. **KT-06 Mortar**: grinding wheat in the grid gives flour and returns the mortar with 1
damage.

---

## kitchen #11: Kitchen decor
Status Planned / Work KubeJS / TODO `food/kitchen #11`

### Goal
Decorative meat grinder, pots, pans, baking sheets and silverware for kitchens.

### Now (checked jars)
- Handcrafted 2.0.6: cups, plates, bowls, thin / medium / thick / wide pots (flower-pot shapes), kitchen hood. No
  pans, cutlery or grinder.
- Fantasy Furniture 6.7.0 `decorations/*`: bowls (empty, stews), platters, tankards, chalices, cookie jar, muffins,
  paper stack, potion bottles. No pans or cutlery.
- Farmhouse Decorations: kitchen counters / cabinets / antique ovens (decor only).
- The pack's bakeware (`creativeengineering:baking_sheet`, `muffin_tin`, `pie_tin`, `cake_pan`) are plain items
  (`kubejs/startup_scripts/cooking/bakeware.js`).

### Build
1. Source: no mod in the pack has a meat grinder, pans or silverware. Make them as KubeJS blocks
   (`StartupEvents.registry('block', ...)` with a custom model and `box(...)` shape, `.notSolid()`, no collision
   beyond the shape). Put them in `kubejs/startup_scripts/items/decor_blocks.js` (the pack's existing decor block
   registry; read its pattern first) or a sibling `kitchen_decor.js`, ids
   `creativeengineering:meat_grinder`, `frying_pan_decor`, `cutlery_set`, `baking_sheet_stack`, models under
   `kubejs/assets/creativeengineering/`. Add each to item_dict.js and block_dict.js.
2. Baking sheets: give the bakeware a placed form instead (a block item that is still `#creativeengineering:bakeware`).
   That needs the oven's tool slot to accept a block item, which it does (tag test only).
3. Recipes through `R.shaped` / `R.shapeless`; tooltip "Decorative" via `addTooltips` in
   `kubejs/client_scripts/tooltips.js` (`col.norm`).

### Ids and data
New ids above (to add). Unverified: whether another jar in the pack has these (only Handcrafted, Fantasy Furniture
and Farmhouse Decorations were checked). Run `unzip -l` over the decor jars in `pack/MODLIST.md` (Another Furniture,
Beautify, Interiors, Dusty Decorations, Decorative Blocks) for `pan|grinder|fork|spoon|knife` before modelling.

### Decisions
- Source (row: "source"): **check the five unchecked decor jars first; model the rest in KubeJS**.

### Gotchas
- KubeJS blocks with custom models need the model and the texture in `kubejs/assets`; the client needs the files too.

### Tests
**KT-07 Decor** blocks place, show their model, and break back into themselves.

---

## kitchen #12: Heated dough block + green dye -> slime block
Status Planned / Work KubeJS / TODO `food/kitchen #12`

### Goal
A cheaper bulk route to slime: one dough block and one green dye in a heated Create mixer give a slime block.

### Now
- Create 0.5.1.i has `create:crafting/appliances/slime_ball`: shapeless `create:dough` + `#forge:dyes/lime` -> 1
  slime ball (verified). So 9 slime balls (one block) cost 9 dough and 9 lime dye.
- Dough block: `create_compressed:dough_block` (`i.dough_block`); it is already made by IE bottling
  (`kubejs/server_scripts/cooking/cooking.js` line 80) and smoked into bread blocks (line 147).
- Ids: `i.slime_block` (`minecraft:slime_block`), `i.any_dye_green` (`#forge:dyes/green`), `i.any_dye_lime`.

### Build
`R.createMixing(i.slime_block, [i.dough_block, i.any_dye_green], e, 'slime_block_from_dough_block', 'heated')`
in `kubejs/server_scripts/cooking/cooking.js` (dough section). Id `creativeengineering:createmixing/slime_block_from_dough_block`.

### Ids and data
All in item_dict.js already. Recipe type `create:mixing`, heated, through `NCH.createMixing`.

### Decisions
- Green (row) or lime (Create's slime ball)? **Green, as the row says**: it marks the bulk route as different.

### Gotchas
- Cooking.js lines 642 and 659 repeat the dough-block bottling and smoking with literal ids through `CE.*`. They are
  outside this row, but don't copy that style.

### Tests
**KT-08**: dough block + green dye in a heated basin -> slime block; JEI shows it under Mixing.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| kitchen #1 | Remainder buffer on pot, kettle, oven | Testing | Still right in code. Oven client sync bug is #14. The keg (port `-03`) is in `pack/manifest.json` now (remainder_buffer.md 0 corrected). RB-01 to RB-11 not run |
| kitchen #7 | Fridge sort item loss | Testing | `compat/QuarkSafeSort.java` + `mixin/QuarkSortingHandlerMixin.java` present. FS-01 to FS-05 not run |

## Writeup corrections made

- [remainder_buffer.md](../remainder_buffer.md) 4 (Risks), "Known bug ... oven": "Not looked into yet" -> the likely
  cause (update tag without `Remainders`), pointing here.
- [remainder_buffer.md](../remainder_buffer.md) 0: keg row "(`build/libs/`, not installed)" -> the `-03` jar is in
  `pack/manifest.json`.
- [remainder_buffer.md](../remainder_buffer.md): a one-line marker before the second, stale copy of the file (from
  line 272, the 2026-10-18 feasibility draft that says "Code: not started"). It marks the copy as superseded by
  sections 0-8 above it. The copy is left in place for the user to delete.
