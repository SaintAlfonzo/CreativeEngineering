# Trash slot: pack defaults, per-tier backpacks, creative

## 0. At a glance

| Part | Status |
|---|---|
| Pack default screens (`[trash_slot] enabledScreens`, switched on once per client) | DEPLOYED with 1.0.3 (patcherMod 1.0.3) |
| `allContainers` switch for every other container (off) + `excludedScreens` | DEPLOYED with 1.0.3, off by default |
| Starting positions per screen (`positions`) | DEPLOYED with 1.0.3 |
| Sophisticated Backpacks: one position and on/off per tier | DEPLOYED with 1.0.3 |
| Creative inventory screen + survival inventory while in creative | DEPLOYED with 1.0.3 |
| Slot kept on screen at high GUI scale (upstream Y clamp bug) | DEPLOYED with 1.0.3 |

- **Files to open:** [TrashSlotCompat.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/TrashSlotCompat.java),
  [TrashSlotGuiHandlerMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotGuiHandlerMixin.java),
  `CEConfig.TrashSlot`.
- **Knobs:** `config/creativeengineering-common.toml` `[trash_slot]`: `enabledScreens`, `allContainers`, `excludedScreens`, `positions`, `splitBackpacksByTier`.
- **Per-client state:** `TrashSlotSaveState.json` (TrashSlot's, one entry per screen) and `TrashSlotSaveState.ce.json` (ours: screens the pack has switched on once). Both live in the game folder and are never deployed.
- **Keys (TrashSlot):** `T` shows or hides the slot on the current screen, Shift+T locks its position, Delete deletes the hovered stack, Shift+Delete deletes every matching stack. Drag the slot to move it.

## 1. Overview

TrashSlot (Balm, 12.1.0) adds a slot next to container screens; dropping an item on it deletes it, and in
survival the last deleted item can be taken back by clicking the slot with an empty cursor. Out of the box
it is on only for the player inventory, the crafting table and chests, and off on every other screen until
the player presses `T` there. Its positions are saved per screen class, so every Sophisticated Backpack
shared one spot even though each tier has a different panel size, and it never appeared in creative.

## 2. Reference

- **Container id.** TrashSlot keys its settings by screen class (`net/minecraft/.../FurnaceScreen`), plus
  `_large` for double chests. Backpacks now add the tier item:
  `net/p3pp3rf1y/sophisticatedbackpacks/client/gui/BackpackScreen#sophisticatedbackpacks:iron_backpack`.
- **Position.** x = panel left + panel width * anchorX + slotX; y = panel top + panel height * anchorY + slotY.
  The slot is 28x28. TrashSlot's default is anchor 0.5 (panel centre) with offsets that land it under the
  bottom-right corner. Examples:
  - `...|1.0|1.0|-28|0`: under the bottom-right corner, at any panel size (backpacks).
  - `...|1.0|1.0|0|-28`: right of the panel, bottom aligned (creative; under the corner is the Survival
    Inventory tab).
  - `...|0.0|1.0|0|0`: under the bottom-left corner.
- **Switch-on once.** For a screen in `enabledScreens`: a new entry starts on; an existing entry that is off
  gets switched on the first time it opens after being listed, and the id is written to
  `TrashSlotSaveState.ce.json`. After that the player's `T` wins. Removing a screen from the list doesn't
  switch it off for anyone.

## 3. What the pack changes

- **Default on:** player inventory, creative inventory, chests (and barrels, ender chests), crafting table,
  furnace, blast furnace, smoker, anvil, smithing table, grindstone, stonecutter, loom, cartography table,
  enchanting table, brewing stand, Sophisticated Backpacks (every tier).
- **Default off (one config line each):** shulker box, dispenser/dropper, hopper, beacon, horse, villager
  trading, every modded screen. `allContainers = true` turns all of them on except `excludedScreens`
  (Sophisticated Backpacks' settings screen).
- **Backpacks:** each tier (leather, copper, iron, gold, diamond, netherite) has its own entry, starting
  under the panel's bottom-right corner. A placed backpack shares its tier's entry. Dragging the slot on
  one tier doesn't move it on the others. `splitBackpacksByTier = false` goes back to one shared entry.
- **Creative:** the creative inventory screen shows the slot on every tab. Dropping an item on it clears the
  cursor (right click: one item), like vanilla's destroy slot; there is no undo in creative. The survival
  inventory screen also shows it for creative players (TrashSlot hid it).
- **On screen:** the slot's Y is clamped to the window height (TrashSlot clamped against the width).

## 4. Limits and decisions

- **Creative deletion is client side.** In the creative screen the cursor stack only exists on the client
  (picking it up already removed it from the server), so TrashSlot's server message would do nothing. The
  pack provider clears the cursor instead. The "delete hovered slot" key branch is kept off in that screen:
  it sends the picker menu's slot index, which the server would apply to the player's real inventory menu,
  deleting the wrong slot. TrashSlot's own creative delete-key branch (which maps the index) still runs.
- **`T` in the creative screen** jumps to the search tab (vanilla binds chat to `T`), so the slot can't be
  toggled there with the default key. Rebind TrashSlot's toggle if needed.
- **Existing positions are kept.** Only the on/off state of an existing entry is changed by the pack.
  `positions` only applies to entries created after it's set. The old shared backpack entry is no longer
  read.
- **Rejected:** shipping `TrashSlotSaveState.default.json`. TrashSlot only reads it when the player has no
  save state at all, so it would miss every existing player, and it can't express per-tier backpacks.
- **Rejected:** per-backpack (UUID) positions. One entry per backpack opened; the user chose per tier.

## 5. File map

- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/TrashSlotCompat.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/TrashSlotCompat.java): settings resolution, switch-on record, `StorageTierLayout`, creative provider.
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotGuiHandlerMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotGuiHandlerMixin.java): `onScreenInit` overwrite, creative redirects.
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotComponentMixin.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotComponentMixin.java): Y clamp.
- [../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotSaveStateAccessor.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/mixin/TrashSlotSaveStateAccessor.java): settings map.
- `../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/CEConfig.java`: `TrashSlot` section.
- `../development/patcherMod/libs/trashslot-forge-1.19.2-12.1.0.jar`: compile-only.

## 6. Offline checks

- `./gradlew build --offline` in `patcherMod/`, then `javap -p -c` on the built mixin classes: vanilla
  references must be SRG names (`f_96544_` for `Screen.height`, `m_6262_` for `getMenu`, `m_142503_` for `setCarried`).
- Mixin targets checked against TrashSlot 12.1.0 bytecode: `runKeyBindings` calls `ContainerSettings.isEnabled`
  four times; ordinal 0 is the delete-hovered-slot guard.

## 7. In-game tests

- [ ] **TS-01 inventory:** open the player inventory in survival → slot under the bottom-right corner.
- [ ] **TS-02 workstations:** open each of furnace, blast furnace, smoker, anvil, smithing table, grindstone, stonecutter, loom, cartography table, enchanting table, brewing stand, crafting table → slot shows on every one.
- [ ] **TS-03 chests:** chest, double chest, barrel → slot shows (no regression).
- [ ] **TS-04 off by default:** shulker box, hopper, a modded machine → no slot; `T` shows it.
- [ ] **TS-05 toggle sticks:** press `T` on the furnace to hide it, close, reopen, relog → still hidden.
- [ ] **TS-06 backpack tiers:** open a leather and a netherite backpack → slot under each panel's bottom-right corner.
- [ ] **TS-07 backpack split:** drag the slot on an iron backpack, then open a gold one → gold is still at its default; the iron one keeps the dragged spot after relog.
- [ ] **TS-08 creative screen:** creative mode, open the inventory → slot right of the panel; drop an item on it from any tab → item gone, nothing else in the inventory changes.
- [ ] **TS-09 creative right click:** right click a stack onto the slot → one item removed from the cursor.
- [ ] **TS-10 creative delete key:** hover a hotbar item in the creative inventory tab, press Delete → that item (only) is deleted.
- [ ] **TS-11 survival undo:** survival, drop a stack on the slot, click the slot with an empty cursor → stack comes back.
- [ ] **TS-12 GUI scale:** GUI scale 4 (or the largest), open a netherite backpack → slot fully on screen.
- [ ] **TS-13 existing players:** a client whose save state had a screen off (e.g. backpacks) → on after this update, once.

## 8. History

- 2026-09-30: investigated (plan_1.0.3 section 5.5), then built; compiled as patcherMod 1.0.3 (in `development/patcherMod/build/libs/` and `jar_archive/`, not in `mods/`, not deployed).
- 2026-10-02: in `mods/` (patcherMod 1.0.3 copied in, confirmed against the jar contents); not deployed.
- 2026-10-19: status sweep: went out with pack 1.0.3 (DEPLOYED 2026-10-02); TS-01 to TS-13 pending.
