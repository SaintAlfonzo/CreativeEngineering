# camera-bag-design

> Camera bags (leather/iron/netherite) in patcherMod camerabag/ - contents in stack NBT, #creativeengineering:camera_equipment, smithing upgrades; status in feature_writeup/camera_bags.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-02). Repo status docs win over any status line here.

**Built 2026-10-02.** The jar is in mods/ as creativeengineering-1.0.3 (same version, rebuilt; the deploy/latest 1.0.3 staging is now stale). A scratch server boot proved:
- the items register
- the [camera_bag] config is created
- the tag resolves to 64 items
- all 3 recipes load

Test status (GUI, slot locking, smithing keeping the contents, tooltip, overflow spill): feature_writeup/camera_bags.md.

**Pieces:**
- patcherMod `camerabag/`:
  - `CameraBag`: registries, the `CAMERA_EQUIPMENT` tag key, `canHold`, and load/save/contents/spillOverflow on NBT `CameraBag.Items`.
  - `CameraBagTier`, `CameraBagItem`: right-click opens; onDestroyed spills; tooltip.
  - `CameraBagMenu`: the camera-menu pattern (copy to a SimpleContainer + write back on setChanged); locked bag slot + SWAP block; stillValid by stack identity.
  - `client/CameraBagScreen`: draws with solapplepie's gui/inventory.png + slot.png.
- Config: CEConfig `[camera_bag]` leatherSlots 5 / ironSlots 9 / netheriteSlots 14 (range 1-27). The slot count travels in the open packet.
- Item models use solapplepie textures. Iron = lunchbox and netherite = golden_lunchbox are PLACEHOLDERS; the user only chose lunchbag for leather.
- Tag: tags/equipmenttags.js holds the nested #exposure:lenses/filters/film_rolls/developed_film_rolls, cameras, camera_stand, and 10 instant slides. Flashes were left out because the user didn't list them.
  - Exposure's filters tag includes every glass pane and the interplanar projector.
- Recipes (equipment/camera_bag.js): the shaped leather bag (5 leather + 1 string); smithing leather + iron block -> iron; iron + netherite ingot -> netherite.
- item_dict: all exposure / exposure_expanded / exposure_polaroid ids, plus t_exposure_* / t_camera_equipment and the 3 bag keys.

**Future plans from the user:** maybe dedicated slots for certain items later. No curios slot.

**Background that drove the design:**
- Exposure's `Attachment` record + `FilteredSlot` give each slot its own predicate.
- The SoL lunchbox keeps its contents in a ForgeCaps ItemStackHandler, and its upgrades are plain crafts that wipe them.
- ForgeCaps are not in getTag(). So smithing (which copies only getTag) loses them, and they aren't synced to the client.

Related: [exposure-backport](exposure-backport.md), [patchermod-coremod](patchermod-coremod.md), memory `scratch-server-verify`.

**Moved 2026-10-02 (1.0.4):** the bags now live in exposureBackport `src/extras` as mod `exposure_extras` (ids `exposure_extras:*_camera_bag`, MissingMappingsEvent remaps the old creativeengineering ids, config `exposure_extras-common.toml [camera_bag]`, tag `#exposure_extras:camera_equipment` defaults in the jar + kubejs add/remove lists). patcherMod 1.0.4 no longer has camerabag/. Status and tests (CB-, R4-04 remap): [camera_bags.md](../camera_bags.md). See [release-plans-1-0-3-to-1-0-6](release-plans-1-0-3-to-1-0-6.md).

**Dyeable (2026-10-03):** CameraBagItem implements DyeableLeatherItem (display.color; vanilla dye recipe + cauldron wash work, smithing keeps colour). Client item colour handler in CameraBagClient tints layer0 `item/camera_bag_dyable`; layer1 is `camera_bag_outline_leather` untinted. Iron + netherite models still point at the leather outline as a placeholder - to give a tier its own outline, add `camera_bag_outline_<tier>.png` and change layer1 in that tier's model json. Textures originated as kubejs/assets/dyable.png + leatheroutline.png (copies kept there). Built and installed to mods/ (-02); no creative-tab dyed variants.
