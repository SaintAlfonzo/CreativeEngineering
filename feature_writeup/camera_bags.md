# Camera bags: leather, iron and netherite bags for camera gear (Exposure Extras)

## 0. At a glance

| Part | Status |
|---|---|
| Three bags `exposureextras:{leather,iron,netherite}_camera_bag`, 5 / 9 / 14 slots, only `#exposureextras:camera_equipment` | DEPLOYED (patcherMod in 1.0.3; moved to Exposure Extras in the exposureBackport `-02` jar with 1.0.4); never tested in game |
| Contents in the bag's NBT (tooltip shows them, smithing upgrades keep them, destroyed bags drop them) | DEPLOYED |
| Dyeable like leather armour, colour kept through upgrades | DEPLOYED with 1.0.4 |
| Old `creativeengineering:*_camera_bag` items remapped to the new ids | DEPLOYED with 1.0.4; R4-04 never run |
| Recipes: leather bag crafted; iron and netherite by smithing | DEPLOYED; the netherite step probably needs Armor Trims' template (F1) |

- **Pending:** F4 (flashes in the bag tag, `equipmenttags.js`, `/reload`) built 2026-10-22, DEPLOYED with 1.1.01, scratch server PASSED 2026-10-22
  (SQ-GM-03). CB-01 to CB-11 are new; R4-04 to R4-06 and R4-11 stay in
  [plan_1.0.4.md](plan_1.0.4.md). Findings F1 to F4 (section 4); F1 and F2 decided 2026-10-29 (keep the template; lunchbox textures for the tiers); F4 done 2026-10-22.
- **Files to open:** [camerabag/](../development/exposureBackport/src/extras/java/com/SaintAlfonzo/exposureextras/camerabag/),
  [camera_bag.js](../kubejs/server_scripts/equipment/camera_bag.js) (recipes),
  [camera_equipment.json](../development/exposureBackport/src/extras/resources/data/exposureextras/tags/items/camera_equipment.json)
  (default contents tag), [equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js) (pack add/remove lists).
- **Knobs:** `config/exposureextras-common.toml` `[camera_bag]` (`leatherSlots` 5, `ironSlots` 9, `netheriteSlots` 14;
  1 to 27); the tag (`/reload`).

## 1. Overview

Exposure's cameras take film, lenses, filters and flashes, and players carry several of each. The bags are the pack's
own content (in the Exposure Extras mod inside the exposureBackport jar): a small portable inventory, like Spice of
Life's lunch box, that only takes camera gear.

## 2. Reference

### 2.1 The bags

| Bag | Slots | Fireproof | Recipe |
|---|---|---|---|
| Leather | 5 | no | shaped: string top, leather around (`_S_ / L_L / LLL`) |
| Iron | 9 | no | smithing: leather bag + iron block |
| Netherite | 14 | yes | smithing: iron bag + netherite ingot (F1) |

All stack to 1, sit in the Exposure creative tab, and dye like leather armour (crafting grid with dyes; washed in a
cauldron). All three currently share the leather look (two-layer model: a tinted body and an untinted outline).

### 2.2 What goes in

`#exposureextras:camera_equipment`, any slot, normal stack size. Defaults from the jar: the camera, the instant camera,
the camera stand, `#exposure:lenses`, `#exposure:filters` (every glass pane and the Interplanar Projector count as
filters in Exposure), `#exposure:film_rolls`, `#exposure:developed_film_rolls`, and the 10 instant slides. Not in by
default: flashes (`#exposure:flashes`), photographs, albums, frames. Never: another bag, or anything that refuses to go
in containers (shulker boxes). The pack adds `#exposure:flashes` (redstone lamp, IE light bulb) in `equipmenttags.js`
(2026-10-22, F4); its remove list is empty.

### 2.3 Using one

- Right-click with the bag in either hand → its screen (slots centred, up to 9 per row, rows balanced) over the
  player inventory; a leather-equip sound.
- The bag's own slot is locked while open: hidden, can't be clicked or swapped with a number key or F. If the bag
  leaves its slot anyway, the screen closes.
- Shift-click moves gear into the bag and anything back out.
- Tooltip: `<used> / <slots>` and the first 5 stacks, then "and N more".
- A destroyed bag (burned, despawned, voided) drops its contents, like a shulker box.
- Lowering a slot count in the config: the next time that bag is opened, items in the removed slots go to the player's
  inventory (or drop).

## 3. What the pack changes

- **Storage:** the contents are in the bag's NBT (`CameraBag.Items`, vanilla container format), not a capability, so the
  client sees them for the tooltip and a smithing upgrade (which copies the base's NBT) keeps them. An empty bag has no
  tag, like a new one. `CameraBagMenu` copies them into a container and writes back after every change.
- **Slot count from the server:** the open packet carries the size, so the client agrees with the server's config.
- **Move from patcherMod (1.0.4):** ids `creativeengineering:*_camera_bag` → `exposure_extras:*_camera_bag`, remapped
  by a `MissingMappingsEvent` handler in `ExposureExtras`; contents come along in the NBT. The old `[camera_bag]`
  section in `creativeengineering-common.toml` was dropped (not carried over). Details: [plan_1.0.4.md](plan_1.0.4.md) 2.
- **Mod id rename (2026-11-10, namespace_move.md D-EXPO):** `exposure_extras` became `exposureextras` (naming rule).
  The `exposure_extras:` ids move by the swap list `data/exposureextras/swaps/owner_namespace.json` (Creative Common
  remaps and aliases them); `exposure_extras-common.toml` is renamed to `exposureextras-common.toml` on first load.
  The `creativeengineering:` remap above stays as a permanent read alias. BUILT, not yet installed; test SQ-NSM-10.
- **Recipes** in KubeJS (the mod ships none), smithing on purpose so upgrades keep contents, name and colour.

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The netherite upgrade probably needs a Netherite Upgrade Smithing Template.** DECIDED 2026-10-29 (owner): keep; the template is used for every netherite upgrade (TODO `gear/camera-bags #2`). Armor Trims blocks any
  smithing recipe whose material is in `#forge:ingots/netherite` unless the template is in its slot
  ([shields.md](shields.md) F8). So iron bag + netherite ingot gives nothing without a template, and the template is
  used up. Keep that (consistent with gear), or change the material (e.g. netherite scrap) to skip it. Check with CB-04.
- **F2. Iron and netherite have no look of their own.** DECIDED 2026-10-29 (owner): use the new lunchbox textures (TODO `gear/camera-bags #3`, not built). All three share the leather model until tier
  outlines are drawn ([plan_1.0.4.md](plan_1.0.4.md) 2, "still open" 4).
- **F3. Never run in game.** GUI, slot locking, upgrades keeping contents, tooltip, overflow spill and the old-id remap
  have only been checked by the build (plan_1.0.4 "still open" 1 to 3).
- **F4. Flashes are left out** of the default tag. DONE 2026-10-22 (TODO `gear/camera-bags #4`): `equipmenttags.js`
  adds `#exposure:flashes` to the bag tag. CB-11.

### Known limits

- **One bag open at a time,** opened from the hand only (no keybind, no Curios slot).
- **No automation:** hoppers can't reach a bag's contents.
- **Unused source art:** `kubejs/assets/dyable.png` and `leatheroutline.png` ship in the payload but nothing reads them
  (the textures are in the jar).

### Rejected options

- **A Forge item capability for contents:** the client wouldn't see them and smithing wouldn't carry them.
- **Crafting-table upgrades:** would empty the bag.

## 5. File map

- exposureBackport `src/extras/java/com/SaintAlfonzo/exposureextras/`: [camerabag/CameraBag.java](../development/exposureBackport/src/extras/java/com/SaintAlfonzo/exposureextras/camerabag/CameraBag.java), `CameraBagItem`, `CameraBagMenu`, `CameraBagTier`, `client/CameraBagScreen`, `client/CameraBagClient`; `ExtrasConfig` (`[camera_bag]`); `ExposureExtras` (remap).
- exposureBackport `src/extras/resources/`: `data/exposureextras/tags/items/camera_equipment.json`; `assets/exposureextras/models/item/*_camera_bag.json`, `textures/item/camera_bag_dyable.png`, `camera_bag_outline_leather.png`.
- KubeJS: [equipment/camera_bag.js](../kubejs/server_scripts/equipment/camera_bag.js), [tags/equipmenttags.js](../kubejs/server_scripts/tags/equipmenttags.js), `item_dict.js` (`i.*_camera_bag`).
- Config: `config/exposureextras-common.toml`.
- Related: [exposure.md](exposure.md) (the rest of the backport).

## 6. Offline checks

None. The exposureBackport build (`./gradlew :exposureBackport:build`) compiles the extras source set.

## 7. In-game test suite

Also R4-04 (remap), R4-05 (basics), R4-06 (slot count), R4-11 (dye) in [plan_1.0.4.md](plan_1.0.4.md).

- [ ] **CB-01 Craft:** string + 5 leather in the shape → leather camera bag; tooltip `0 / 5`.
- [ ] **CB-02 Filter:** open it; a camera, a film roll and a lens go in; a photograph, a stick and another bag don't.
- [ ] **CB-03 Iron upgrade:** smithing table, leather bag with 3 items + iron block → iron bag, same items, tooltip
  `3 / 9`.
- [ ] **CB-04 Netherite upgrade (F1):** iron bag + netherite ingot, without and with a Netherite Upgrade Smithing
  Template → note which works; contents kept; the netherite bag survives lava.
- [ ] **CB-05 Slot lock:** open the bag from the hotbar, try to click its slot, press its number key on a slot, press
  F → the bag doesn't move. Repeat with the bag in the off hand.
- [ ] **CB-06 Shift-click:** shift-click a camera from the inventory → into the bag; shift-click it back → inventory.
- [ ] **CB-07 Tooltip:** a bag with 7 different stacks → 5 lines and "and 2 more".
- [ ] **CB-08 Dye:** dye a leather bag blue in the grid → blue; wash it in a water cauldron → undyed; upgrade a blue bag
  → still blue.
- [ ] **CB-09 Destroyed:** throw a full leather bag into lava → its contents drop out (and burn, by item physics).
- [ ] **CB-11 Flashes (F4):** a redstone lamp and an IE light bulb go into a bag. The accept rule
  (`CameraBag.canHold`) scratch server PASSED 2026-10-22 (SQ-GM-03: lamp and bulb accepted, stick refused); clicking them into the bag screen: in game.
- [ ] **CB-10 Multiplayer:** on the dedicated server with `netheriteSlots = 20`, open a netherite bag → 20 slots
  shown, as the server says.

## 8. History

- By 2026-10-02: camera bags in patcherMod (1.0.3, boot-tested only).
- 2026-10-03: moved to Exposure Extras, made dyeable, old ids remapped (exposureBackport `-02`, 1.0.4).
- 2026-10-18: this writeup; findings F1 to F4; tests CB-01 to CB-10. No code changed.
- 2026-10-22: F4, flashes added to the bag tag; CB-11.
- 2026-10-22 (second bulk run): SQ-GM-03 PASS on the scratch server; the CB-11 click stays in game.
