# Brief: gear / camera-bags

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `camera-bags`; updated 2026-10-31 (brief sweep). Reference writeup:
[camera_bags.md](../camera_bags.md) (test prefix `CB-`, plus R4-04 to R4-06 and R4-11 in
[plan_1.0.4.md](../plan_1.0.4.md)). The bags are Exposure Extras content inside the in-tree exposureBackport jar
(`development/exposureBackport/src/extras/`, shipped as `exposureBackport-1.9.19-ce-19.2-04.jar`); the pack side is
[camera_bag.js](../../kubejs/server_scripts/equipment/camera_bag.js) (recipes) and the second `ServerEvents.tags`
block of [equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js).

## Scope and order

| Row | Status | Size |
|---|---|---|
| `gear/camera-bags #3` Tier looks | Planned | lunchbox art copied into the jar, two or four models, one Java tint change; jar rebuild |
| `gear/camera-bags #1` Camera bags | Testing | see Built, needs play |
| `gear/camera-bags #4` Flashes in the bag tag | Testing | see Built, needs play |
| `gear/camera-bags #2` Netherite upgrade needs a template | Complete | decided 2026-10-29: keep the template (camera_bags.md F1) |

Only #3 is left to build. Do it with the next exposureBackport build, and run the CB tests (#1) in the same client
session, since CB-08 / R4-11 cover the looks.

---

## camera-bags #3: Tier looks
Status **Planned** (decided 2026-10-29: use the new lunchbox textures) · Work: resource pack / new jar · id `gear/camera-bags #3`

### Goal
Iron and netherite camera bags take the iron and netherite lunchbox looks (the owner's art from 2026-10-29), still
dyeable. The leather bag keeps its current look.

### Now
- Bag models (in the jar, `development/exposureBackport/src/extras/resources/assets/exposure_extras/models/item/`):
  `leather_camera_bag.json`, `iron_camera_bag.json`, `netherite_camera_bag.json`, all identical:
  `layer0 = exposure_extras:item/camera_bag_dyable`, `layer1 = exposure_extras:item/camera_bag_outline_leather`.
  Textures there: only those two PNGs (checked 2026-10-31). Installed jar `mods/exposureBackport-1.9.19-ce-19.2-04.jar`
  (`gradle.properties` `mod_version=1.9.19-ce-19.2-04`).
- Tint: `camerabag/client/CameraBagClient.java:24-27` tints layer 0 with `DyeableLeatherItem.getColor(stack)` for every
  tier; `CameraBagItem` (line 33) does not override `getColor`, so undyed bags get leather brown `0xA06540`.
- Lunchbox art (built 2026-10-29, [lunchboxes.md](../lunchboxes.md) section on looks):
  `kubejs/assets/solapplepie/textures/items/iron_lunchbox.png`, `netherite_lunchbox.png` (16x16 full colour, the undyed
  look) and outline masks `iron_lunchbox_outline.png`, `netherite_lunchbox_outline.png`.
  [gen_lunchbox_looks.py](../../tools/gen_lunchbox_looks.py) splits each into `lunchbox_dye/<look>_base.png` (greyscale,
  tinted layer 0) and `<look>_overlay.png` (outline and latches, layer 1), and writes `lunchbox_look/<look>.json` (plain)
  and `<look>_dyed.json` (base + overlay). The lunchbox picks plain or dyed through the item property
  `creativeengineering:lunchbox_dyed` and a tint handler that returns -1 when undyed
  ([LunchboxClient.java](../../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/lunchbox/client/LunchboxClient.java) 35-55).
- Unused source art: `kubejs/assets/dyable.png`, `kubejs/assets/leatheroutline.png` (camera_bags.md "Known limits").
- LB-13 ([lunchboxes.md](../lunchboxes.md)) expects the camera bags unchanged by the lunchbox work: it goes stale with
  this row.

### Build
1. Extend `tools/gen_lunchbox_looks.py` with a second output set (keeps one source of truth for the art): for the iron and
   netherite looks also write, into `development/exposureBackport/src/extras/resources/assets/exposure_extras/`,
   `textures/item/camera_bag_iron.png`, `camera_bag_netherite.png` (copies of the plain look),
   `camera_bag_iron_base.png` / `_overlay.png`, `camera_bag_netherite_base.png` / `_overlay.png`, and the models
   `iron_camera_bag.json` (layer0 plain, override `exposure_extras:dyed` 1 -> `item/camera_bag_look/iron_dyed`) and the
   netherite pair. `--check` covers them too. Leather models untouched.
2. Java, `CameraBagClient`: register an item property `exposure_extras:dyed` (1 when the stack has `display.color`,
   `DyeableLeatherItem.hasCustomColor`) for the iron and netherite bags in `setup`; change the tint handler so iron and
   netherite return -1 unless dyed (leather keeps the brown default tint on the dyable layer).
3. Bump `mod_version` to `1.9.19-ce-19.2-05`, `./gradlew :exposureBackport:build --offline`, `:exposureBackport:installToMods`
   (game not running). Both-sides jar: ships to clients with the next deploy.
4. Delete `kubejs/assets/dyable.png` and `leatheroutline.png` (or move them next to the jar's art); drop the "Unused
   source art" line.
5. camera_bags.md F2 -> built, 2.1 looks; lunchboxes.md LB-13 reworded ("camera bags use the iron / netherite lunchbox
   looks"); changelog bullet under the camera bags family.

### Ids and data
New textures `exposure_extras:item/camera_bag_iron`, `camera_bag_netherite`, their `_base` / `_overlay`; new models
`exposure_extras:item/camera_bag_look/{iron,netherite}_dyed`; item property `exposure_extras:dyed`. No item, tag or
recipe change. Items `i.iron_camera_bag`, `i.netherite_camera_bag` (`exposure_extras:*_camera_bag`).

### Decisions
- **Copy the art into the jar or reference `solapplepie:items/...` from the bag models:** recommend copy (generated by
  the same script). A cross-namespace reference makes the jar depend on KubeJS assets and on Spice of Life's namespace.
- **Copper look for leather:** recommend no; the leather bag keeps its own look (the decision names iron and netherite).

### Gotchas
- `item/generated` gives layer N tint index N: the plain (undyed) model has the full-colour art on layer 0, so the tint
  handler must return -1 for it or it gets dyed brown.
- Keep the outline / latch art on layer 1 in the dyed model.
- The lunchbox override numbers (`lunchbox_skin`) do not apply to bags; do not copy the skin overrides.

### Tests
- Extend **CB-08** / R4-11: undyed leather, iron and netherite bags side by side -> leather brown, iron and netherite
  lunchbox looks; dye each blue -> blue bodies, outlines and latches as drawn; wash -> back to the plain looks.
- **CB-new1** (proposed, numbered when queued): `python3 tools/gen_lunchbox_looks.py --check` exits 0 and lists the
  camera bag outputs.

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| `gear/camera-bags #1` Camera bags | Testing | Never run in game; [camera_bags.md](../camera_bags.md). Open: CB-01 to CB-10, R4-04 to R4-06, R4-11 (S C P). Armor Trims' templated upgrade keeps the bag's NBT (`assemble`, decompiled 2026-10-21) |
| `gear/camera-bags #4` Flashes in the bag tag | Testing | `#exposure:flashes` in `#exposure_extras:camera_equipment` (equipmenttags.js), 2026-10-22, deployed 1.1.01; [camera_bags.md](../camera_bags.md) F4. Scratch SQ-GM-03 PASSED; open: CB-11 in game (P) |
| `gear/camera-bags #2` Netherite upgrade needs a template | Complete | Kept 2026-10-29: template required like every netherite upgrade (camera_bags.md F1); CB-04 checks it |

## Writeup corrections made

None to [camera_bags.md](../camera_bags.md) (checked: paths, ids, slot config and recipes match the code).
