# Remainder buffer: keep bottles, bowls and buckets in FD-style cooking stations

When a Farmer's Delight style station finishes a recipe, it throws each ingredient's leftover container (glass
bottle, bowl, bucket) out of the block as an item. With this feature the leftovers stay in the station: in its
container slot when the waiting meal is served in them, otherwise in two leftover slots right of the output.
Java in patcherMod (mixins for the FD cooking pot and the Farmer's Respite kettle, native code in the oven); the
keg's is native code in the `backportedBrewin` port (3.6). Test prefix `RB-` (the keg's: AL-79 to AL-81, AL-84 in
[alcohol.md](alcohol.md)).

From cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19): kitchen > stove "bottle/bucket output slot for ingredient inputs in upgraded
cooking pot?", and drink > tea "integrate with later bottle/bowl output buffer system?".

## 0. At a glance

| Part | Status |
|---|---|
| Feasibility study (1, 2) | DONE 2026-10-18, from the decompiled jars the pack uses |
| Design (3) | DECIDED 2026-10-18: D1-D5 as recommended (5) |
| Cooking pot + kettle (mixins), oven (native), config `[cooking]` | BUILT 2026-10-18: patcherMod compiles and builds (reobf checked); 10 GameTests pass (6). DEPLOYED with 1.0.7; one leftover per item used (for the oven's ingredient counts) DEPLOYED with 1.0.8 (2026-10-19); not run in game (RB-01 to RB-11) |
| Keg | BUILT 2026-10-19 in the port's `-03` (now the jar in `pack/manifest.json`), see 3.6; not tested |
| WTHIT line "Holding: ..." | BUILT 2026-10-21 (patcherMod 1.1.01, `compat/WthitRemainderBuffer`, 3.3), deployed with 1.1.01 (2026-10-22); not seen in game (WL-31) |
| Oven client sync (leftovers vanished from the GUI) | FIXED 2026-10-21 (patcherMod 1.1.01, 3.4), deployed with 1.1.01; GameTest RB-T11; not seen in game (RB-12) |
| Bowl count ("duplicate bowl", [TODO](todo/TODO.md) `cooking/kitchen #4`) | no duplication: RB-T13 counts bowls before and after on the pot (4 ways) and oven, all equal (3.1) |
| Plating delay: a recycled container shows in the container slot before plating (`[cooking] recycleShowTicks`, [TODO](todo/TODO.md) `cooking/kitchen #13`) | BUILT 2026-10-21 (patcherMod 1.1.01, deployed 2026-10-22); GameTests RB-T14, RB-T15; not seen in game (RB-14) |

Files to open first:

- `development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/cooking/`: `RemainderBuffer` (routing,
  fit check, leftover rule), `RemainderBufferHolder`, `RemainderBufferStorage` (saving), `RemainderOutputHandler`
  (automation), `RemainderSlot`, `client/RemainderSlotFrames`
- Creative Delight (QoL-1 since 2026-10-25, creative_delight.md 22): `mixin/farmersdelight/` `CookingPotRemainderMixin`,
  `CookingPotMenuRemainderMixin`, `CookingPotScreenRemainderMixin`; `mixin/farmersrespite/` `KettleRemainderMixin`,
  `KettleMenuRemainderMixin`, `KettleScreenRemainderMixin`; switch `creative_delight-common.toml [qol] remainderBuffer`
  (`qol/RemainderBufferQol`). patcherMod's `[cooking] remainderBuffer` is the oven's only; the other dials are shared
- `oven/OvenBlockEntity`, `OvenMenu`, `client/OvenScreen` (native buffer)
- `compat/WthitRemainderBuffer` (the WTHIT "Holding" / "Waiting" lines, 3.3)
- `CEConfig.Cooking`; `config/creativeengineering-common.toml` `[cooking]`
- `tools/test_remainder_buffer.sh` + `tools/remainder_buffer_test/` (GameTests, 6)

Config knobs (`creativeengineering-common.toml`, `[cooking]`): `remainderBuffer` (default true; off = thrown as in
the original mods), `stallWhenFull` (default true; off = craft anyway and throw what doesn't fit), `recycleShowTicks`
(default 10; how long a recycled container shows in the container slot before plating, 0 = at once; 3.1).

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
no fluid item). It goes away when the 4.5.0 port replaces the keg; until then a one-line `@ModifyArg` would fix it. Checked 2026-10-19: the port (`-02`) gives one remainder per ingredient and one container per poured item, so the bug is gone.

## 2. What the stations look like in the original mods

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

## 3. Design as built

### 3.1 The buffer and the routing

`cooking/RemainderBuffer` is an `ItemStackHandler` with **2 slots** at normal stack limits (buckets 16). Two slots
cover the mixed cases (a milk bucket and a honey bottle in one pot recipe). Each leftover goes, in order:

1. **Recycle.** If the leftover is what the waiting meal is served in, the container slot is empty or holds the
   same item, and the waiting meals still need containers there (meals waiting minus containers already in the
   slot), it goes to the container slot. The same tick plates the meal with it. Only as many as are needed go
   there: spares would pile up in a slot that automation can't empty (the first GameTest run caught exactly that,
   see 6). "Served in" is the station's own `isContainerValid`: the recipe's `container` (`mealContainerStack` /
   `drinkContainerStack`, set just before the leftover loop), or else the result's crafting remainder (hot cocoa
   has no `container` field; it is served in its remainder, a bottle). Example: kettle strong green tea (green tea
   + honey bottle, served in a glass bottle): one bottle plates the drink, the other goes to the buffer. This
   covers the tea item in cooking.md without touching the recipes.
2. **Buffer.** Otherwise it merges into a matching buffer stack, then into an empty slot.
3. **Overflow.** Whatever is left is thrown as before. With `stallWhenFull` on (the default) this can't happen,
   see 3.2.

The leftover rule is the stations' own: the stack's crafting remainder, else FD's `INGREDIENT_REMAINDER_OVERRIDES`
(pot and oven; the kettle has no override map).

A bowl-food ingredient (tomato sauce, a stew) now gives its bowl back where the original pot threw it out of its
left side. Step 1 can put that bowl straight into the plating slot and plate the meal in the same tick, so a bowl
seems to appear from nowhere. That is what the "duplicate bowl" note ([TODO](todo/TODO.md) `cooking/kitchen #4`) saw:
GameTests RB-T13a-e count every bowl (ingredients, plating slot, leftover slots, plated meals) before and after
cooking, with the plating slot empty, holding 1, holding 10, and with meals already waiting, plus the oven; the
totals match in every case.

**Plating delay** (BUILT 2026-10-21, [TODO](todo/TODO.md) `cooking/kitchen #13`): a step-1 recycle holds the station's
plating for `[cooking] recycleShowTicks` (10 ticks), so the bowl or bottle is seen in the container slot with the meal
unplated, then plated. The hold is a counter on the buffer (`RemainderBuffer.holdPlating`, not saved), set by `store`
when it put something into the container slot and spent by the plating call: a HEAD inject on FD's and Respite's
private `useStoredContainersOnMeal` (pot, kettle), and the oven's own `serverTick`. A container a player or hopper
puts in plates at once. A long value would hold the next cook only once the meal slot fills (`canCook` sees the
unplated meal).

### 3.2 When the buffer is full: the station waits

`canCook` (pot), `canBrew` (kettle) and the oven's `canBake` get a RETURN check: if the station would craft, the
current ingredients' leftovers are simulated through steps 1 and 2 (`RemainderBuffer.fits`). If they don't all
fit, it returns false. The station then holds its progress at 0, as it already does when the meal slot is full,
and resumes as soon as a slot is emptied. A hopper or funnel on an output face keeps it running indefinitely.
`stallWhenFull = false` skips the check: the station crafts and throws what doesn't fit (the old behaviour, only
once the buffer is full).

### 3.3 Getting the items out

- **GUI**: two take-only slots in a column right of the output, x = 150, y = 37 and 55, on the pot, kettle and
  oven (the space is empty on all three textures). The frames are blitted from the station's own container slot
  frame (pot and oven `cooking_pot.png` at 91,54; kettle `kettle.png` at 85,54), so no new texture. The slots are
  added after the player inventory, so the menus' own slot numbers don't move (pot 45-46, kettle 41-42, oven
  46-47). Shift-click moves them to the player inventory; nothing can be put in.
- **Automation**: the non-top faces' item handler is the station's own one plus the two buffer slots after it
  (`RemainderOutputHandler`, extract only). The pot and kettle get it from a HEAD inject on `getCapability`; the
  cached `LazyOptional` is invalidated by a listener on the station's own output handler, so it dies exactly when
  theirs does. A hopper under a pot pulls meals first, then leftovers; filter it if only meals are wanted. The
  oven's handler is the same on every face, so every face can pull leftovers.
- **Breaking**: RETURN inject on `getDroppableInventory` (pot, kettle; the oven's own list) drops the buffer with
  the rest. The pot's item form keeps only the meal, as before.
- **WTHIT** (BUILT 2026-10-21, `compat/WthitRemainderBuffer`, plugin `creativeengineering:remainder_buffer`):
  looking at a pot, kettle or oven shows "Holding: 3 Glass Bottle, 1 Bucket" while a leftover slot holds
  something, and a red "Waiting: leftover slots full" while a craft is held back for room (3.2). The server sends
  both (`blockData`), so the line doesn't rely on the client's copy of the slots. The stall flag is the game time
  of the station's last refused room check (`RemainderBuffer.markStalled`, not saved; the check runs every tick
  while a recipe waits, so the line clears within 2 ticks of a slot being emptied). Off with
  `[wthit] remainderBuffer`. The pot and kettle parts register only when their mod is loaded.

### 3.4 Saving

Pot and kettle: the buffer is a mixin field, saved as an attached capability provider
(`cooking/RemainderBufferStorage`, `AttachCapabilitiesEvent<BlockEntity>`, key
`ForgeCaps/creativeengineering:remainder_buffer`). Forge reads and writes it in `BlockEntity.load` /
`saveWithoutMetadata`, so no mixin targets the stations' own load/save (whose SRG names would differ in dev). The
provider asks for the buffer lazily: the event fires inside the BlockEntity constructor, before mixin fields are
set. The oven saves its buffer itself under `Remainders`.

Client copy: the pot's and kettle's update tag is FD's full save, so `ForgeCaps` goes along (RB-T12). The oven's
update tag is its own, and until 2026-10-21 it left `Remainders` out while `load` read it unguarded: every block
update (taking the output, adding an ingredient, a hopper) emptied the client's two slots while the server kept the
items, so bottles and buckets seemed to vanish ([TODO](todo/TODO.md) `cooking/kitchen #14`). Now the update tag
carries `Remainders`, `load` only reads it when present, and a buffer change sends a block update like any oven slot
change (RB-T11).

### 3.5 Mixins (patcherMod `creativeengineering.mixins.json`)

| Mixin | Target | Injections |
|---|---|---|
| `CookingPotRemainderMixin` | FD `CookingPotBlockEntity` | `ejectIngredientRemainder` HEAD (cancellable; shrinks the stack to what's left), `canCook` RETURN, `getDroppableInventory` RETURN, `getCapability` HEAD |
| `KettleRemainderMixin` | Respite `KettleBlockEntity` | `@Redirect` of `ItemUtils.spawnItemEntity` in `processBrewing`, `canBrew` RETURN, `getDroppableInventory` RETURN, `getCapability` HEAD |
| `CookingPotMenuRemainderMixin`, `KettleMenuRemainderMixin` | `CookingPotMenu`, `KettleMenu` | main constructor TAIL (`addSlot` x2), `quickMoveStack` HEAD for the new slots |
| `CookingPotScreenRemainderMixin`, `KettleScreenRemainderMixin` (client list) | `CookingPotScreen`, `KettleScreen` | `renderBg` TAIL (frames) |

All `remap = false` with SRG names, as the rest of patcherMod. `quickMoveStack` and `renderBg` are listed under both
names (`m_7648_`/`quickMoveStack`, `m_7286_`/`renderBg`, `require = 1`) so the same mixins also apply in a dev run.
The menu and screen mixins extend the vanilla menu/screen class to call `addSlot`, `moveItemStackTo`, `slots`,
`leftPos`/`topPos`; the reobfuscated jar was checked to carry the SRG names (`m_38897_`, `m_38903_`, `f_38839_`,
`f_97735_`, `f_97736_`).

### 3.6 Keg (BUILT 2026-10-19 in the port, slot rules scratch server PASSED 2026-10-22, not tested in game)

Native code in `development/backportedBrewin` (D5), its own copy of the routing rather than a dependency on
patcherMod; full description in [alcohol.md](alcohol.md) 10.16. Differences from the pot:

- **Slots:** one new take-only slot (8) right of the output, plus the keg's existing empty-container slot (7, under
  the fluid slot), which already holds the same kind of item. There is no room for a second new slot: the right
  bubbles fill the space above the output. Fill order: a matching stack, then an empty slot, 8 before 7.
- **No recycle step:** the keg's container slot is for pouring drinks out of a tank, not for plating one waiting
  meal, so leftovers never go there.
- **Settings:** the port's own `brewinandchewin-common.toml [keg] remainderBuffer` / `stallWhenFull` (defaults
  true), not patcherMod's `[cooking]`, so the port doesn't depend on patcherMod.
- **Automation and breaking:** the sides and bottom extract slot 8 with the output and slot 7; it is part of the
  keg inventory, so it drops on break and saves with it.

The 2.0 bottle dupe (1.3) went with the 2.0 jar.

Scratch server 2026-10-22 (SQ-OM-01, alcohol.md AL-81 to AL-84): 9 slots; the output, empty-container and leftover
slots take nothing from any face; bottom and sides pull output, empties and leftovers, the top only ingredients; an
8-slot `-02` keg loads into the same slots. The leftover overflow and stall (AL-79, AL-80) stay in game.

## 4. Risks

- **Menu sync**: the extra slots exist only where patcherMod is loaded. It is `both` in
  [mod_sides.md](../mod_sides.md) and required on both sides, so this is fine.
- **Removing the mod** drops nothing: items in the buffer are lost with the NBT key. Same as any modded
  inventory.
- **Stall surprises**: a player who never empties the slots gets a pot that stops after 16-128 crafts. The GUI
  shows two full leftover slots, and WTHIT says "Waiting: leftover slots full" (3.3).
- **FD/Respite updates**: the pack pins its versions. The HEAD inject on `ejectIngredientRemainder` is stable; the
  two `@Redirect`s depend on the inline code staying the same.
- **Other mixins**: checked 2026-10-18 across all 414 jars of `pack/manifest.json` (mixin classes naming the pot,
  kettle, their menus or screens): only Create Central Kitchen's two (1.1), which don't overlap.
- **Mixin aliases**: the official names listed next to the SRG ones (3.5) never match in the pack; `require = 1`
  is met by the SRG name.
- **Oven leftovers vanishing (scratchpad 2026-10-21):** fixed 2026-10-21, see 3.4 ([TODO](todo/TODO.md)
  `cooking/kitchen #14`).

## 5. Decisions (all taken as recommended, 2026-10-18)

| # | Question | Decision |
|---|---|---|
| D1 | Buffer full: stall, or throw the extra? | stall (3.2), with a CEConfig switch |
| D2 | Recycle leftovers into the container slot when they match? | yes (3.1 step 1) |
| D3 | Slots per station | 2 |
| D4 | Bottom/side automation can pull the buffer? | yes, after the output slot |
| D5 | Keg: wait for the port, or mixin the 2.0 keg now (and fix 1.3)? | wait for the port; fix 1.3 only if the port slips past 1.0.7 |

## 6. Offline checks (2026-10-18)

- `./gradlew :patcherMod:build`: compiles (the whole mod, against the pack's 414 jars in `libs/`) and reobfuscates;
  the reobf jar's mixin classes carry SRG names for the inherited menu/screen members (3.5).
- GameTests: `tools/test_remainder_buffer.sh` (harness in `tools/remainder_buffer_test/`: builds the `cooking/`
  classes, the pot/kettle BE + menu mixins and `oven/` straight from patcherMod's source, with FD 1.2.4 and Respite
  2.0 deobfuscated at runtime, a stand-in main class/config and a small test datapack). **All 10 pass.** The first
  run failed RB-T9: both bottles went to the kettle's container slot, leaving a spare there after plating. That
  led to the "only as many as needed" rule in 3.1.
- 2026-10-21: **17 tests** (RB-T11 to RB-T13e added). The stand-in config had fallen behind (`ovenIngredientCounts`
  was missing, so the harness no longer compiled); it is a copy of the real `[cooking]` section again. Three runs:
  all 17 passed twice; once RB-T4 failed ("bucket not thrown"). RB-T4 counts item entities near the test, and the
  thrown bucket's random motion can carry it out of range: a flaky test, not a routing fault. The new bowl tests
  count what the station holds, not item entities, because RB-T6 drops bowls next door.
- 2026-10-21 (later): **19 tests**, RB-T14 and RB-T15 for the plating delay; stand-in config resynced again
  (`recycleShowTicks`). All 19 pass.

| Test | Proves |
|---|---|
| RB-T1 `potRecyclesBottle` | hot cocoa with a milk bottle: the bottle plates the cocoa, buffer empty, nothing thrown |
| RB-T2 `potBucketToBuffer` | hot cocoa with a milk bucket: the bucket is in buffer slot 0, nothing thrown |
| RB-T3 `potStallsWhenFull` | both slots blocked (16 buckets, 64 bowls): no cooking for 300 ticks, ingredients kept; free a slot and it cooks, bucket in that slot |
| RB-T4 `potThrowsWhenStallOff` | `stallWhenFull = false`, full buffer: it cooks and throws the bucket |
| RB-T5 `potAutomation` | side/bottom handler = 9 + 2 slots, top 9; output still extracts; buffer extracts, refuses inserts; container-slot insert still works; handler invalid after the pot is removed |
| RB-T6 `potBreakDrops` | breaking drops the buffer contents |
| RB-T7 `potSaveLoad` | saved under `ForgeCaps/creativeengineering:remainder_buffer` and loaded back |
| RB-T8 `menus` | pot menu 9 + 36 + 2 slots, kettle 5 + 36 + 2; take-only; shift-click moves the buffer to the player; FD's own output shift-click still works |
| RB-T9 `kettleStrongTea` | strong green tea: one bottle plates the drink, one in the buffer, container slot empty, nothing thrown |
| RB-T10 `ovenBuffer` | oven (test recipe milk bottle + sugar -> bread): bottle buffered, oven handler 10 + 2 slots, buffer extractable |
| RB-T11 `ovenUpdateTagKeepsBuffer` | the oven's update tag carries `Remainders`; loading it into a second oven replaces that oven's slots; a tag without the key leaves them alone |
| RB-T12 `potUpdateTagHasBuffer` | the pot's update tag holds `ForgeCaps/creativeengineering:remainder_buffer` (what the pot and kettle client copies rely on) |
| RB-T13a-d `potBowlsSlotEmpty`, `potBowlsSlotOne`, `potBowlsSlotTen`, `potBowlsMealWaiting` | test recipe tomato sauce + carrot -> beef stew (bowl): bowls in ingredients + plating slot + leftover slots + plated stews are the same before and after, with 0 / 1 / 10 bowls in the plating slot and with 2 stews already waiting |
| RB-T13e `ovenBowls` | oven test recipe tomato sauce + sugar -> bread plated in a bowl, 2 bowls in the plating slot: the same count holds, 2 plated |
| RB-T14 `potRecycleDelay` | hot cocoa with a milk bottle: when cooked, the bottle is in the container slot and the output empty; plated 10 ticks later |
| RB-T15 `ovenRecycleDelay` | oven tomato sauce + sugar -> bread in a bowl, plating slot empty: the sauce's bowl shows in the plating slot, plated 10 ticks later |

Not covered offline: the screens (frames checked only by compositing them onto the two textures), the pack's
SRG-named jar in game, Create Central Kitchen alongside.

## 7. In-game test suite

In patcherMod 1.0.7 and later (pack 1.0.7, DEPLOYED 2026-10-18).

- [ ] **RB-01 Pot recycle:** hot cocoa with a milk bottle. Nothing on the ground; the cocoa comes out plated.
- [ ] **RB-02 Pot bucket:** a milk-bucket recipe. The bucket is in a leftover slot (right of the output).
- [ ] **RB-03 Kettle:** strong green tea, then strong melon juice. One bottle plates the drink, one is in a leftover
  slot. Also: with `remainderBuffer = false`, count what stock throws for melon juice + honey (code says 2,
  cooking.md says 1).
- [ ] **RB-04 Stall:** fill both slots with different items, then cook something with a third kind of leftover:
  the station waits; take one out and it resumes.
- [ ] **RB-05 Automation:** a hopper under the pot pulls meals, then leftovers; a hopper into the side still feeds
  bowls to the container slot.
- [ ] **RB-06 Break:** breaking the pot/kettle/oven drops the leftovers.
- [ ] **RB-07 GUI:** the two slot frames show right of the output on pot, kettle and oven; nothing can be placed in
  them; shift-click moves to the inventory. The pot and kettle recipe books still open and shift the GUI correctly.
- [ ] **RB-08 Save:** save, quit, reload: leftovers kept.
- [ ] **RB-09 CCK:** Create Central Kitchen's blaze stove under the pot and kettle still signals.
- [ ] **RB-10 Oven:** a baking recipe with a milk bottle (muffin): bottle in a leftover slot.
- [ ] **RB-11 Server:** the same on a dedicated server with a client (slots sync, shift-click works).
- [ ] **RB-12 Oven leftovers stay visible** (patcherMod 1.1.01): bake blueberry muffins (milk bottle) with the GUI
  open, take the muffins out: the bottle stays in the leftover slot. Close and reopen: still there. Take it with a
  click.
- [ ] **RB-13 Bowl count** (patcherMod 1.1.01): 10 bowls in the pot's plating slot, cook 5 of a tomato-sauce bowl
  meal: bowls in the slot + leftover slots + meals add up to 15. Once with Create Central Kitchen's blaze stove under
  the pot.
- [ ] **RB-14 Recycle shows** (patcherMod 1.1.01): watch an FD pot cook a tomato-sauce bowl meal with the plating
  slot empty: the bowl appears in the plating slot for half a second, then the meal is plated. The same with hot
  cocoa and a milk bottle in the pot, and strong green tea in the kettle.

## 8. History

- 2026-10-18: feasibility study (1-5 as a proposal).
- 2026-10-18: built as decided (D1-D5 as recommended). `cooking/` package, six mixins (pot and kettle: block entity,
  menu, screen), native oven buffer, `[cooking]` config, GameTest harness; container-slot recycle capped at what
  the waiting meals need after the first test run. patcherMod compiles and builds; 10 GameTests pass.
- 2026-10-19: keg built in the Brewin' port `-03` (3.6); compiles, no GameTest (in-game tests in alcohol.md).
- 2026-10-21 (patcherMod 1.1.01, not deployed): oven client sync fixed (3.4, `cooking/kitchen #14`); "duplicate bowl"
  checked and found to be the designed recycle (3.1, `cooking/kitchen #4`); WTHIT "Holding" / "Waiting" line built
  (3.3, `cooking/kitchen #3`). GameTests 17, harness stand-in config resynced.
- 2026-10-21 (later): plating delay `[cooking] recycleShowTicks` (3.1, `cooking/kitchen #13`); GameTests 19.

> **Superseded copy below (noted 2026-10-21):** everything from here on is the 2026-10-18 feasibility draft, left in
> the file by mistake. Sections 0-8 above are current; delete the copy once checked.

# Remainder buffer: keep bottles, bowls and buckets in FD-style cooking stations

When a Farmer's Delight style station finishes a recipe, it throws each ingredient's leftover container (glass
bottle, bowl, bucket) out of the block as an item. This writeup looks at whether those leftovers can go into a
buffer slot on the station instead. Java in patcherMod (and in our own `oven/` and `backportedBrewin` source).
Test prefix `RB-`.

From cooking.md (the cooking plans, folded into [todo/TODO.md](todo/TODO.md) and removed 2026-10-19): kitchen > stove "bottle/bucket output slot for ingredient inputs in upgraded
cooking pot?", and drink > tea "integrate with later bottle/bowl output buffer system?".
- 2026-10-19: one leftover per item used DEPLOYED with pack 1.0.8 (10:20). RB-01 to RB-11 pending.

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
- 2026-10-19: shipped in pack 1.0.7 (DEPLOYED 2026-10-18 19:43). For 1.0.8, `RemainderBuffer.leftovers` takes a per-slot
  count, so an oven slot that gives up n items (`[cooking] ovenIngredientCounts`) leaves n containers. The keg is
  still not covered: the Brewin' port `-02` ships in 1.0.8 without a remainder buffer.
