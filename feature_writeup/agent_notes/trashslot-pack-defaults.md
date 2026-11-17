# trashslot-pack-defaults

> TrashSlot pack defaults in Creative Common (CEConfig [trash_slot], per-tier backpack ids, creative support); feature_writeup/trashslot.md TS-01..13

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-09-30 as Creative Common 1.0.3 (mod_version bumped on request). By 22:00 a 1.0.3 jar was in mods/ (not by me); I rebuilt it with the gzip climate reader and copied it over.
- `compat/TrashSlotCompat.settingsFor` replaces TrashSlot's `getSettings` (via an `onScreenInit` @Overwrite in TrashSlotGuiHandlerMixin). Screens listed in `enabledScreens` are switched on once per client; the switched-on ids are recorded in `TrashSlotSaveState.ce.json` in the game dir, so a later `T` press by the player still sticks.
- Backpack container id = `...BackpackScreen#<tier item id>` (StorageTierLayout, via sophisticatedcore `getWrappedStorageStack`). The user chose per tier over per UUID.
- Creative: the creative screen clears the client cursor (no undo). The `runKeyBindings` isEnabled ordinal 0 is redirected off in creative, because it sends the picker slot index and would delete the wrong server slot.
- User, 2026-09-30: the random item deletion (IM) is NOT TrashSlot. It happens with no slot visible or nearby.
- User, 2026-09-30: IM has only been seen on the dedicated server, never single player.

**Why:** the user wants the slot on for vanilla workstations, both inventories (creative too) and backpacks, with an `allContainers` switch kept off for now.
**How to apply:** add screens through the CEConfig lists, not TrashSlotSaveState.default.json. Related: [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md), [creative-common-coremod](creative-common-coremod.md), memory `feature-writeup-pattern`.
