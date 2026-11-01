# items / storage-and-item-loss: implementation brief

Written 2026-10-31 from [TODO](../todo/TODO.md) `# items`, Sub `storage-and-item-loss` (rows #1, #5 In Progress, #8 Planned;
#2, #3, #4, #7, #9 Testing). Layout per [README.md](README.md). Writeups: [fixes_compat.md](../fixes_compat.md) (prefix
`FX-`, last FX-49: Quark sort 2.20, Polymorph 2.28 / 2.29), [testing/fridge_sort/plan.md](../testing/fridge_sort/plan.md)
(`FS-01` to `FS-05`), [plan_1.0.3.md](../plan_1.0.3.md) 4.7 (item-loss repro), [backpack_skins.md](../backpack_skins.md)
(`BP-`), [storage_audit.md](../storage_audit.md) 6 (`SB-`), [lunchboxes.md](../lunchboxes.md) (`LB-`),
[dyeing.md](../dyeing.md) (`DY-`). New tests here are placeholders `FX-newN`, numbered when queued.

## Scope and order

| Step | Row | Work | Notes |
|---|---|---|---|
| 1 | `#1` container sorting audit | tool + scratch script, maybe patcherMod | Mostly a test harness; may turn up a fix |
| 2 | `#5` Polymorph in more crafting screens | patcherMod | Curios and Cosmetic Armor first (same pattern as 2.29, verified lookups) |
| 3 | `#8` trash can buffer | patcherMod (common + client mixins) | Independent; one build with #5 |
| - | `#2`, `#3`, `#4`, `#7`, `#9` | Testing | Built, needs play (below) |

Steps 2 and 3 can share one patcherMod build. Never replace the jar while the game runs.

---

## storage-and-item-loss #1: Container sorting audit
Status **In Progress** / Work patcherMod / tool / TODO `items/storage-and-item-loss #1`

### Goal
Every sort a player can trigger in the pack keeps every item (counts, NBT), proven by a repeatable programmatic test,
not by hand.

### Now
- **Quark sort is fixed** (1.0.7, fixes_compat 2.20): `compat/QuarkSafeSort.java` (113 lines, `sort(IItemHandler, int,
  int, int[])` at 35, `splitOversized` 89) replaces `SortingHandler.sortInventory` through `mixin/QuarkSortingHandlerMixin`:
  split stacks over the max size, validate the whole layout first, no-op if it will not fit. The fridge harness
  [zz_scratch_fridgesort.js](../testing/fridge_sort/zz_scratch_fridgesort.js) (104 lines; `zfsRun` 62 calls
  `SortingHandler.sortInventory(new InvWrapper(cont), ...)` at 81) tests one 2-tall fridge. FS-01 to FS-05 and
  `cooking/kitchen #7` are still Testing.
- **Where Quark sort reaches**: vanilla container screens plus Quark's whitelist `"Allowed Screens"`
  (`config/quark-common.toml` 24): Cooking for Blockheads fridge, counter, fruit basket; IE crates (standard, entity);
  Ender Storage chest; Framed storage; Cloud Storage chest; Botania bauble box (2.25 added the Trinket Case path).
- **Other sorts**: Sophisticated Core's own sort buttons (`common/gui/SortBy`, `settings/nosort`, and an
  `compat/inventorysorter` package) in backpacks; Quark's player-inventory sort.
- **The 2026-09-30 repro** (`tools/item_repro/`, plan_1.0.3.md 4.7, memory `item-loss-repro`): ~380 random-click rounds
  under lag found no vanilla click loss; the one permanent loss was Sophisticated Backpacks' anti-dupe
  (`UUIDDeduplicator.checkForDuplicateBackpacksAndRemoveTheirUUID`) stripping `contentsUuid` from a re-opened backpack.
  **No fix and no TODO row** for that loss (grep `contentsUuid` / `UUIDDeduplicator` in patcherMod and TODO: none).

### Build
1. **Inventory of sort paths** (write it into fixes_compat 2.20 as a table): each screen above, the backpack sort, the
   player sort; per path, which handler it sorts (`InvWrapper`, Balm `CombinedContainer`, SB `InventoryHandler`) and
   whether it goes through `QuarkSafeSort`.
2. **Generalise the fridge harness** into `feature_writeup/testing/sort_audit/zz_scratch_sortaudit.js` (scratch server
   only, never in `kubejs/`): a `zsortaudit run` console command that, for every block in a list (chest, barrel, shulker,
   CfB fridge 1- and 2-tall, counter, IE crate, ender chest, framed chest, cloud chest), places it, fills it with a fixed
   set (an oversized stack, an NBT item, a damaged tool, a full stack, singles), totals by `id+nbt`, sorts through
   `SortingHandler.sortInventory` on the block's item capability, and totals again: `PASS` / `FAIL <diff>` per block.
3. **Backpack sort**: call Sophisticated Core's sort on a backpack's `InventoryHandler` the same way (find the method in
   the 0.6.4 jar with `javap`; unverified name), compare totals.
4. **Fixes**: any `FAIL` gets a patcherMod fix routed through `QuarkSafeSort`'s validate-first approach.
5. **SB `contentsUuid` wipe**: ask the user whether to fix it here (a mixin making the dedupe skip the backpack that
   belongs to the open menu, plan_1.0.3.md 4.7) or as a separate row; recommend a separate TODO row.
6. Queue the scratch rows in scratch_queue.md (SQ-SA-*), then fold results into fixes_compat 2.20.

### Ids and data
- Classes (verified in earlier sessions): `vazkii.quark.base.handler.SortingHandler`,
  `net.minecraftforge.items.wrapper.InvWrapper`; screen class names from quark-common.toml 24.

### Decisions
- **Scratch script or headless harness:** recommend the scratch script (Quark, Balm and SB all need a running server).
- **The SB dedupe fix:** recommend its own row (it is not a sort).

### Gotchas
- Scratch scripts must not ship: keep the harness in `feature_writeup/testing/`, copy into the scratch server only.
- KubeJS hides `getTag`/`setTag`, `getUUID`; use `getNbt`, `getUuid` (memory `item-loss-repro`).
- `tools/item_repro/README.md` says to match the client by `ps -eo pid=,args=`; per CLAUDE.md, redact the access token
  in the same command or use `pgrep -x java` + `readlink /proc/PID/cwd`.

### Tests
- **FX-new1 Sort audit:** `zsortaudit run` on the scratch server: every block `PASS`; latest.log has no `quark-sort`
  errors.
- **FX-new2 Backpack sort:** a backpack filled with the fixed set, sorted by name and by count: totals unchanged.

---

## storage-and-item-loss #5: Polymorph in modded crafting screens
Status **In Progress** / Work patcherMod / TODO `items/storage-and-item-loss #5`

### Goal
Polymorph's recipe choice works in every crafting screen of the pack, not only the vanilla ones.

### Now
- **Built and shipped in 1.1.01, untested (FX-35 to FX-42):** Sophisticated crafting upgrade (fixes_compat 2.28:
  `compat/SophisticatedPolymorphCompat`, `SophisticatedPolymorphClient`, `SophisticatedCraftingPolymorphMixin`,
  `SophisticatedUpgradeOpenMixin`, `PolymorphRecipeSelectionPacketMixin`) and the IE Engineer's Crafting Table plus the
  Pretty Pipes Crafting Terminal (2.29: `CraftingMenuPolymorphCompat`, `CraftingMenuPolymorphClient`,
  `IECraftingTablePolymorphMixin`, `PrettyPipesCraftingTerminalPolymorphMixin`). Mixins listed in
  `creativeengineering.mixins.json` 46, 54-56. (fixes_compat's table rows 37-38 still say "jar not yet in `mods/`":
  stale, the TODO says shipped in 1.1.01.)
- The pattern ([IECraftingTablePolymorphMixin.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/IECraftingTablePolymorphMixin.java)):
  `@Redirect` the menu's `RecipeManager.m_44015_` call to `RecipeSelection.getPlayerRecipe(menu, type, inv, level,
  player)`, implement `CraftingMenuPolymorphCompat.PolymorphCraftingMenu` (`refreshCraftingResult`, `resultSlot`); the
  client widget registers for that marker interface.
- **Not covered** (2.29 audit, lookups verified with `javap` 2026-10-31):
  - Curios `top.theillusivec4.curios.common.inventory.container.CuriosContainer#slotsChanged` (`m_6199_`) calls
    `m_44015_` (curios-forge 5.1.6.4);
  - Cosmetic Armor `lain.mods.cos.impl.inventory.ContainerCosArmor#updateCrafting` (private static, takes menu, level,
    player, grid, result) calls `m_44015_` (CosmeticArmorReworked 1.19.2-v1a);
  - Create blueprint: `BlueprintMenu#onCraftMatrixChanged`, `BlueprintEntity#interactAt` (`m_7111_`, the in-world craft)
    and the client `BlueprintOverlayRenderer#rebuild` each look the recipe up (create 0.5.1.i);
  - modded furnaces / ovens (Engineers Decor lab and electrical furnaces, CfB oven, Fantasy Furniture oven): not
    scanned per method. AE2 terminals: Polymorphic Energistics (`polyeng-forge-0.1.1`) is installed.

### Build
1. `CuriosCraftingPolymorphMixin` (remap false, target `CuriosContainer`): `@Redirect` in `m_6199_` as the IE mixin;
   implement `PolymorphCraftingMenu` (result slot: find its index with `javap`; the 2x2 grid field name too).
2. `CosArmorCraftingPolymorphMixin`: the lookup is in a static method that receives the menu and the player. A static
   `@Redirect` handler can take the target method's own arguments after the redirected call's (Mixin's argument
   capture), so the handler gets `menu` and `player` and calls `RecipeSelection.getPlayerRecipe(menu, ...)`. The marker
   interface goes on `ContainerCosArmor` itself.
3. Register both menus in `CraftingMenuPolymorphClient` through the marker interface (no new client code if the button
   offset suits the 2x2 result slot; check the position in game).
4. Create blueprint: decide first (below). If yes: the menu redirect, plus the in-world craft reading the player's
   Polymorph pick for that grid.
5. Furnaces: scan each mod's furnace block entity for its own `getRecipeFor`; Polymorph's furnace support only covers
   `AbstractFurnaceBlockEntity`. Add mixins only for those that conflict in practice (JEI shows two smelting results for
   one input).
6. fixes_compat 2.29: move each from "Not covered" to built; fix the stale "jar not yet in `mods/`" table cells.

### Ids and data
- Classes and SRG names above, verified 2026-10-31 by `javap` on the jars in `mods/`.

### Decisions
- **Create blueprint:** the overlay is client-only and would show the first recipe while the craft gives the pick.
  Recommend leaving the blueprint out (the menu sets the result item, which is what players read) unless the user meets
  a conflict there.
- **Furnaces:** recommend only on a seen conflict.

### Gotchas
- Polymorph only sends the candidate list for the player's open menu; Curios and Cosmetic Armor menus are opened from
  the inventory, so the `PlayerContainerEvent.Open` recompute in `CraftingMenuPolymorphCompat.onContainerOpen` must
  cover them.
- Curios replaces the player inventory screen: Polymorph's own inventory hook may already attach a widget there; check
  for two buttons.

### Tests
- FX-35 to FX-42 (exist) first.
- **FX-new3 Curios grid:** in the Curios inventory screen, fill the 2x2 grid with a conflicting recipe: the Polymorph
  button shows and the pick crafts.
- **FX-new4 Cosmetic Armor grid:** same in the Cosmetic Armor screen.

---

## storage-and-item-loss #8: Trash can block buffer rework
Status **Planned** / Work patcherMod / TODO `items/storage-and-item-loss #8`

### Goal
The Trash Cans item trash can holds what a player puts in for as long as its screen is open (a chest-sized, 27-slot
area), so anything can be taken back; only on closing the screen are the items deleted. Automation (hoppers, pipes) keeps
deleting at once.

### Now
- `trashcans-1.0.18-forge-mc1.19.jar` (both sides, mod_sides.md 287), read with `javap` 2026-10-31:
  - `TrashCanBlockEntity.ITEM_HANDLER` (`TrashCanBlockEntity$1`): 1 slot, `getStackInSlot` always empty, `insertItem`
    returns empty for anything the filter allows (`itemFilter`, `itemFilterWhitelist`), so the item is gone the moment
    it lands. Hoppers and pipes use the same capability.
  - `screen/ItemTrashCanContainer`: `addSlots` adds one `SlotItemHandler(ITEM_HANDLER, 0, 93, 25)` and 9 filter ghost
    slots (`ItemTrashCanContainer$1`, at 8 + 18n, 64); overrides `clicked` (`m_150399_`) and `quickMoveStack`
    (`m_7648_`). Its base `TrashCanContainer` (SuperMartijn642 core `BlockEntityBaseContainer`) has final `width` /
    `height` and adds the player slots.
  - `screen/ItemTrashCanScreen`: size 175 x 118, background `item_screen.png`, filter label at (8, 52).
- TrashSlot (`trashslot-forge-1.19.2-12.1.0`, the inventory trash slot) is a different mod and out of scope (memory
  `trashslot-pack-defaults`).

### Build
1. **Server, common mixin on `ItemTrashCanContainer`:** a per-menu `SimpleContainer buffer = new SimpleContainer(27)`
   (`@Unique`). At the tail of `addSlots`, replace the single input slot: add 27 `Slot(buffer, n, x, y)` in a 9 x 3 grid
   whose `mayPlace` asks `ITEM_HANDLER.insertItem(0, stack, true).isEmpty()` (so the filter still decides). Grow the
   menu height by 54 (a `@ModifyArg` on the `TrashCanContainer` super-constructor's height) so the player slots move
   down.
2. `quickMoveStack`: shift-click from the player inventory goes into the buffer (`moveItemStackTo` over its range);
   shift-click out of the buffer returns to the player.
3. **Delete on close:** override `removed(Player)` (`m_6877_`) in the mixin (check whether SuperMartijn's base container
   declares it; if so inject there): for each buffer stack, `ITEM_HANDLER.insertItem(0, stack, false)` (deletes it,
   and keeps any future side effect of the mod's handler); a stack the filter now refuses (filter changed while open)
   goes back to the player with `player.getInventory().placeItemBackInInventory`.
4. **Client mixin on `ItemTrashCanScreen`:** size 175 x 172, draw the buffer area (a 9 x 3 slot texture, own PNG in
   patcherMod resources under `assets/creativeengineering/textures/gui/`) between the filter row and the player
   inventory; move the filter label. A small "Deleted when closed" label.
5. Config `[trash_cans] bufferUntilClose = true` (when false, skip the mixins' effect).
6. Writeup: a fixes_compat 2.x entry; changelog bullet when built.

### Ids and data
- Block `trashcans:item_trash_can` (lang key `trashcans.gui.item_trash_can.title`, verified); classes above, verified.

### Decisions
- **Which cans:** the row names the item can. The ultimate trash can (`UltimateTrashCanContainer`) also takes items:
  recommend the same buffer there in a second step.
- **Block broken while open:** the screen closes (`validateObjectOrClose`), so `removed` deletes the buffer. Recommend
  that (the items were trashed); dropping them would turn the can into storage.
- **Buffer size:** 27, per the row.

### Gotchas
- A server crash or kick mid-screen: `removed` still runs on disconnect; on a crash the buffer is lost, which equals
  the old behaviour (deleted).
- Creative mode: vanilla's creative inventory does not open this menu, so creative uses the same path as survival.
- The 9 filter slots are ghost slots handled in `clicked`; keep the new slot indices after them so the mod's index
  checks still hit the filter slots.

### Tests
- **FX-new5 Take back:** put a diamond and a stack of dirt in the can, take the diamond back, close: dirt gone, diamond
  kept; reopen: buffer empty.
- **FX-new6 Filter:** blacklist dirt: dirt cannot be placed in the buffer; a hopper above still deletes cobblestone at
  once.
- **FX-new7 Shift-click:** shift-click a stack in and out; 27 stacks fill the buffer; close with all 27: all deleted.

---

## Built, needs play

| Row | Change | Status | Note |
|---|---|---|---|
| #2 | Items in a sack deleted when dyed | Testing | `SupplementariesDyeBlocksMixin` (patcherMod 1.1.01) skips block-entity blocks and `#creativeengineering:no_world_dye`; [dyeing.md](../dyeing.md) D1. Test in the TODO row (filled sack in world: nothing; grid dye keeps contents; wool still dyes) |
| #3 | Item deleted after middle-click pick | Testing | `CreativePickBlockSyncMixin` (client, 1.1.01), memory `creative-pick-block-desync`. Test on the server in creative with a full hotbar |
| #4 | Backpack reskin by smithing | Testing | [backpack_skins.md](../backpack_skins.md); BP-01 to BP-05 open; equip block off by default |
| #7 | Sacks as bundles | Testing | [storage_audit.md](../storage_audit.md) 6; scratch SQ-GM-10 PASSED 2026-10-22; SB-01 to SB-09 in game |
| #9 | Lunchbox tiers, skins and dye | Testing | [lunchboxes.md](../lunchboxes.md); SQ-LB-01, -02 PASSED r1031 (2026-10-31); DEPLOYED with 1.1.05 (1.1.05 changelog `## Lunchboxes`; server 1.1.05, client 1.1.06). LB-01 to LB-14 in game |
