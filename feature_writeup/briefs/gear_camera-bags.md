# Brief: gear / camera-bags

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `camera-bags`. Reference writeup:
[camera_bags.md](../camera_bags.md) (test prefix `CB-`, plus R4-04 to R4-06 and R4-11 in
[plan_1.0.4.md](../plan_1.0.4.md)). The bags are Exposure Extras content inside the in-tree exposureBackport jar
(`development/exposureBackport/src/extras/`, shipped as `exposureBackport-1.9.19-ce-19.2-04.jar`); the pack side is
[camera_bag.js](../../kubejs/server_scripts/equipment/camera_bag.js) (recipes) and the second `ServerEvents.tags`
block of [equipmenttags.js](../../kubejs/server_scripts/tags/equipmenttags.js) (lines 110-131).

## Scope and order

| Row | Status | Size |
|---|---|---|
| `gear/camera-bags #2` Netherite upgrade needs a template | Planned | a decision; zero to two lines of KubeJS |
| `gear/camera-bags #3` Tier looks | Planned | two 16x16 textures, two model edits, optionally a small Java change; jar rebuild |
| `gear/camera-bags #4` Flashes in the bag tag | Planned | one line of KubeJS, after a look in game |

All three hang on `gear/camera-bags #1` (Testing: the bags have never been run in game). Do them in one sitting
with the CB tests: run CB-01 to CB-04 first (that is also the in-game check #2 needs), decide #2 and #4 there,
then do #3 with the next exposureBackport build (it is the only row that needs a jar). #2 and #4 are `/reload`
changes and can ship any time.

Related: [shields.md](../shields.md) F8 (the Armor Trims template rule, decided "keep" for gear on 2026-10-05),
`gear/tuning #4` (NBT carried through upgrades; the bag's contents ride in its NBT, see #2 Gotchas).

---

## camera-bags #2: Netherite upgrade needs a template
Status **Planned** · Work: KubeJS · id `gear/camera-bags #2`

### Goal
Make the iron → netherite bag step behave on purpose: either keep Armor Trims' rule (template required and used
up, like every other netherite upgrade in the pack) or move the step off a netherite ingot so no template is needed.

### Now
- Recipe: [camera_bag.js:22](../../kubejs/server_scripts/equipment/camera_bag.js)
  `R.smithing(i.netherite_camera_bag, i.iron_camera_bag, i.ingot_netherite, e)` → recipe id
  `creativeengineering:smithing/<toId(...)>` (NCH `smithing`, [neoCraftHelper.js:793](../../kubejs/startup_scripts/func/neoCraftHelper.js)).
- Armor Trims Backport 1.3.3 (`armor_trims`) replaces the smithing menu with
  `gg.hipposgrumm.armor_trims.gui.SmithingMenuNew`. **Verified against the jar (decompiled 2026-10-21):**
  `createResult()` blocks any recipe whose addition is in a tag listed in `ArmortrimsApi.upgradeBaseBlockedConditions`
  (with `"Disable Vanilla Netherite Upgrading" = true` in `config/armor_trims.toml` that is `#forge:ingots/netherite`)
  unless a Netherite Upgrade Smithing Template (`i.trim_netherite_upgrade` =
  `armor_trims:netherite_upgrade_smithing_template`) is in slot 1. With the template it calls the vanilla recipe's
  `assemble` (`m_5874_`), which copies the base item's NBT, so **contents, name and colour survive the templated
  upgrade too.** The template is consumed (`"Don't Consume Smithing Templates" = false`).
- JEI already shows the templated layout: patcherMod `compat/jei/SmithingTemplateJEI` re-lists every smithing
  recipe whose material Armor Trims blocks on the Item Upgrading page with the template, and hides it from the
  vanilla page ([shields.md](../shields.md) F8). The bag recipe is covered automatically (it reads Armor Trims'
  conditions, not a list of ids). Unchecked in game for this recipe.
- The iron step (`R.smithing(i.iron_camera_bag, i.leather_camera_bag, i.block_iron, e)`, line 21) is not affected:
  an iron block isn't in any blocked tag.

### Build
Option A (recommended, matches the 2026-10-05 gear decision): no recipe change.
1. Run CB-04 in game: iron bag + netherite ingot without a template → no output; with a template → netherite bag,
   contents kept, template gone. Check JEI shows the bag on the Item Upgrading page with the template.
2. Update [camera_bags.md](../camera_bags.md): F1 → "decided: keep, template required", table row 2.1 netherite
   recipe → "smithing: iron bag + Netherite Upgrade template + netherite ingot"; tick CB-04.
3. Optionally add a tooltip line to the iron bag in [tooltips.js](../../kubejs/client_scripts/tooltips.js)
   (`addTooltips(e, [i.iron_camera_bag, ['Upgrade needs a Netherite Upgrade template', col.norm]])`). Not needed if
   JEI shows the template.

Option B (no template): change the addition at line 22 to something outside `#forge:ingots/netherite`, e.g.
`i.scrap_netherite` (`minecraft:netherite_scrap`; also keyed `i.raw_netherite`). Check scrap isn't in a blocked tag
(`/kubejs hand` on it, or JEI's tag list). Keep `R.smithing` so the NBT copy stays.

### Ids and data
- `i.netherite_camera_bag` / `i.iron_camera_bag` / `i.leather_camera_bag` (`exposure_extras:*_camera_bag`,
  item_dict.js:74-76), `i.ingot_netherite`, `i.trim_netherite_upgrade` (item_dict.js:12068), `i.scrap_netherite`.
- Armor Trims behaviour: verified (decompiled `SmithingMenuNew`, lines 151-200 of the decompile).

### Decisions
- **Keep the template or not.** Recommend keep: one rule for every netherite upgrade, JEI already explains it, and
  the 14-slot fireproof bag is endgame-ish anyway. Option B only if the bag should be cheap.

### Gotchas
- A crafting-table recipe would empty the bag (writeup 4, "Rejected options"); stay on smithing either way.
- Netherite templates come from bastion loot and Armor Trims' duplication recipe; spending one on a bag competes
  with gear. That is the cost of Option A.

### Tests
CB-04 already covers it; reword its expected result once decided ("without a template: nothing; with: netherite bag,
contents kept, template used up"). Add **CB-11 JEI:** the netherite bag recipe shows on Armor Trims' Item Upgrading
page with the template, and not on the vanilla Smithing page.

---

## camera-bags #3: Tier looks
Status **Planned** · Work: resource pack / new jar · id `gear/camera-bags #3`

### Goal
Iron and netherite bags look different from the leather one (today all three render the brown dyeable body with
the leather outline).

### Now
- Models (in the jar, `development/exposureBackport/src/extras/resources/assets/exposure_extras/models/item/`):
  `leather_camera_bag.json`, `iron_camera_bag.json`, `netherite_camera_bag.json`, all identical:
  `layer0 = exposure_extras:item/camera_bag_dyable`, `layer1 = exposure_extras:item/camera_bag_outline_leather`.
- Textures (`.../textures/item/`): only `camera_bag_dyable.png` and `camera_bag_outline_leather.png`.
- Tint: `camerabag/client/CameraBagClient.java:24-26` tints layer 0 with `DyeableLeatherItem.getColor(stack)`,
  layer 1 untinted. `CameraBagItem` (line 33) implements `DyeableLeatherItem` without overriding `getColor`, so an
  undyed bag of every tier gets vanilla's leather brown `0xA06540`.
- Unused source art in the pack: `kubejs/assets/dyable.png`, `kubejs/assets/leatheroutline.png` (16x16; writeup
  4 "Known limits").

### Build
1. Draw `camera_bag_outline_iron.png` and `camera_bag_outline_netherite.png` (16x16, same silhouette as the leather
   outline: iron with grey straps / clasp and rivets, netherite with dark grey-purple trim). Start from
   `camera_bag_outline_leather.png`.
2. Point layer 1 of `iron_camera_bag.json` / `netherite_camera_bag.json` at them.
3. Optional (Java, recommended): give each tier its own undyed colour so an undyed iron bag isn't brown. In
   `CameraBagItem` override `getColor(ItemStack)`: return the `display.color` tag if present, else a per-tier default
   from a new `CameraBagTier` field (e.g. leather `0xA06540`, iron `0x8C8C8C`, netherite `0x4A3F45`). Dye and cauldron
   washing keep working (washing removes `display.color`, so the bag falls back to its tier colour).
4. Bump the exposureBackport jar suffix (`-05`), rebuild with `./gradlew :exposureBackport:build`, swap it in `mods/`
   (manifest / MODLIST via the usual deploy tooling, [deploy.md](../deploy.md)).
5. Delete the two stray PNGs in `kubejs/assets/` (or move them under the jar's source art) and drop the "Unused
   source art" line from the writeup.

Pack-side alternative without a jar: put the two outline PNGs under
`kubejs/assets/exposure_extras/textures/item/` and the two model JSONs under
`kubejs/assets/exposure_extras/models/item/` (KubeJS's resource pack overrides the jar). Works, but splits the bag's
assets across two places; only worth it if the jar isn't being rebuilt soon. Step 3 needs the jar either way.

### Ids and data
- Texture ids `exposure_extras:item/camera_bag_outline_iron`, `exposure_extras:item/camera_bag_outline_netherite`
  (new). No item, tag or recipe changes.

### Decisions
- **Draw outlines or keep the shared look.** Recommend draw them (two small sprites), plus the per-tier default
  colour (step 3), which does more for telling tiers apart at a glance than the outline alone.

### Gotchas
- Layer 0 is the tinted layer: keep the outline art on layer 1 or it gets dyed.
- The client needs the new jar too (assets); it's a both-sides jar, so it ships to clients with the next deploy.

### Tests
Extend R4-11 / CB-08: an undyed leather, iron and netherite bag side by side → three different looks; dye each blue →
all blue bodies, outlines still distinct; wash → back to each tier's own colour.

---

## camera-bags #4: Flashes in the bag tag
Status **Testing** (built 2026-10-22, CB-11) · Work: KubeJS · id `gear/camera-bags #4`

### Goal
Decide whether camera flashes go in `#exposure_extras:camera_equipment`, and add them if so.

### Now
- Pack list: [equipmenttags.js:123-131](../../kubejs/server_scripts/tags/equipmenttags.js), `global.tags.add(e,
  i.t_camera_equipment, [ /* e.g. i.t_exposure_flashes, */ ])` (line 126 is the commented example).
- `#exposure:flashes` (`i.t_exposure_flashes`, item_dict.js:13116) in the backport
  (`development/exposureBackport/src/main/resources/data/exposure/tags/items/flashes.json`) is
  **`minecraft:redstone_lamp` and `immersiveengineering:light_bulb`** (optional). Verified in-tree.
- Default bag tag: `development/exposureBackport/src/extras/resources/data/exposure_extras/tags/items/camera_equipment.json`.

### Build
1. In game: carry a camera with a flash attached, check whether players actually carry spare flashes.
2. If yes, recommended form: add only the light bulb, not the whole tag, so the bag doesn't become a redstone-lamp
   box:
   ```js
   global.tags.add(e, i.t_camera_equipment, [
   	i.light_bulb,            // immersiveengineering:light_bulb, the flash nobody builds with
   ])
   ```
   (`i.light_bulb`, item_dict.js:699). If the whole tag is wanted, use `i.t_exposure_flashes` (nested tag reference,
   so later flash additions follow).
3. Update the comment at line 118 ("Not in the defaults: flashes ...") and camera_bags.md 2.2 / F4. `/reload`.

### Ids and data
`i.light_bulb`, `i.redstone_lamp` (item_dict.js:1435), `i.t_exposure_flashes`, `i.t_camera_equipment`. All verified
in the dicts and the backport's data.

### Decisions
- **Flashes in or out.** Recommend in, light bulb only: it's the flash item, a redstone lamp is a building block.

### Gotchas
- `global.tags.remove` only removes direct members (comment at line 119); not relevant for an add.
- Tags sync on join: a client already connected sees the new filter after `/reload` (the server enforces
  `CameraBag.canHold` anyway).

### Tests
**CB-12 Flash:** after the change, an IE light bulb goes into a bag; a redstone lamp doesn't (or does, if the whole
tag was added).

---

## Built, needs play

| Row | Status | Note |
|---|---|---|
| `gear/camera-bags #1` Camera bags | Testing | Row is accurate: never run in game; CB-01 to CB-10, R4-04 to R4-06, R4-11. Nothing stale found in [camera_bags.md](../camera_bags.md). Two facts confirmed while writing this brief: Armor Trims' templated upgrade keeps the bag's NBT (`assemble`), and JEI's template page covers the bag recipe. |

## Writeup corrections made

None to [camera_bags.md](../camera_bags.md) (checked: paths, ids, slot config and recipes match the code).
