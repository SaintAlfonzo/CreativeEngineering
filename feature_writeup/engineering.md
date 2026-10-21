# Engineering: sawblade routing, the dragon-breathing fan, components and coils, the sample syringe

## 0. At a glance

| Part | Status |
|---|---|
| IE Sawmill picks its recipe by the installed blade: `creativeengineering:precision_sawblade` with a `{Profile}` (stairs, door, fence, ...) | DEPLOYED (in the 2026-09-27 initial commit; shipped since 1.0.3); the crash fix (late class load) never explicitly checked in game |
| 12 precision-sawblade blueprints; 211 profiled sawmill recipes from a manual generator pipeline | DEPLOYED; generator still manual (F1) |
| Create fan processing "Dragon Breathing" (dragon head in front of an encased fan), 14 recipes, JEI page | DEPLOYED, in-game test pending |
| 15 components (IE iron and steel plus 13 pack ones) by crafting, sequenced assembly and blueprint, with disassembly | DEPLOYED |
| Wire coils and coil blocks for 12 metals plus the HV coil, by crafting, assembly and blueprint, with disassembly | DEPLOYED |
| Sample syringe: empty + 7 filled variants, sample 8 mob types, feeds cultivated meat | DEPLOYED |

- **Pending:** nothing built and unshipped. EN-01 to EN-18 have never been run. Findings F1 to F8 (section 4) are
  open; F2 and F4 need your call.
- **Files to open:** [sawmill/SawbladeRouting.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/sawmill/SawbladeRouting.java),
  [generation_scripts/sawmill.js](../kubejs/generation_scripts/sawmill.js),
  [fan/DragonBreathingFanProcessingType.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/fan/DragonBreathingFanProcessingType.java),
  [components.js](../kubejs/server_scripts/engineering/components.js),
  [metal_base_materials.js](../kubejs/server_scripts/metal/metal_base_materials.js) lines 687-760 (coils),
  [func/sample_syringe.js](../kubejs/startup_scripts/func/sample_syringe.js).
- **Knobs:** none in the toml. Recipes are server scripts (`/reload`); the sawmill recipes are in the patcherMod jar
  (rebuild); the fan catalyst list is the block tag `#creativeengineering:fan_processing_catalysts/dragon_breathing`.

## 1. Overview

- **IE Sawmill:** takes a log or plank and a sawblade, outputs the recipe's result plus sawdust. IE picks the first
  recipe whose input matches and stops, so with several recipes for one input (planks → stairs, door, slab...) only
  one is reachable, and which one depends on recipe-map order.
- **Create fans:** an encased fan blowing through a catalyst block (water, lava, fire, soul fire) processes items in
  its stream (washing, smelting, smoking, haunting). Create: Halitosis (1.20) added a dragon-head catalyst; it isn't
  available for this version.
- **IE components:** IE has iron and steel mechanical components; Create and IE mods use them in many recipes.
- **Wire coils:** IE has copper, electrum, steel and structural coils, plus coil blocks for LV and MV.

## 2. Reference

### 2.1 Sawblade routing

- **Blade:** `creativeengineering:precision_sawblade` (patcherMod item, 5000 durability, not enchantable, not
  repairable), in `#forge:sawblades` so the sawmill accepts it. Its `{Profile:"<shape>"}` NBT picks the recipe; a plain
  IE sawblade has none. The tooltip shows "Sawmill profile: Stairs" or "Unprogrammed".
- **Rule** (`SawbladeRouting.route`): among recipes whose input matches,
  1. a recipe whose `"blade"` equals the blade's profile;
  2. else the first recipe with no `"blade"` (the default, e.g. planks → slab);
  3. else nothing: the item passes through unchanged.
- **Profiles** (from the generator's `profileFor`): `stairs`, `fence_gate`, `fence`, `door`, `trapdoor`,
  `pressure_plate`, `button`, `sign`, `railing`, `ladder`, `vertical_slab`, `vertical_planks`. Slabs and anything
  unrecognised are the default.
- **Making a blade:** Engineer's Workbench, `precision_sawblades` blueprint (steel plate + sawblade + steel rod over
  blue dye and paper, or over an empty schematic): IE sawblade + a shape reference (any `#minecraft:wooden_stairs`,
  `#creativeengineering:vertical_planks`, `#creativeengineering:railings`, ...) + 2 steel plates → precision sawblade
  with that profile. 12 recipes, one per profile.
- **Recipes:** 211 sawmill JSONs in patcherMod's `data/creativeengineering/recipes/sawmill/` (800 RF, sawdust
  secondary, optional `"blade"`), plus 2 non-wood ones in `kubejs/data/creativeengineering/recipes/sawmill/` (layered
  obsidian, rose quartz).

### 2.2 Dragon Breathing (Create fan processing)

- **Catalyst:** a dragon head (floor or wall) pointing along a cardinal direction, with an encased fan directly behind
  it blowing through the back of the skull. Other blocks added to
  `#creativeengineering:fan_processing_catalysts/dragon_breathing` work in any orientation.
- **Priority 500**, above washing (400), so a dragon head standing in water still breathes.
- **Effects:** purple dust and dragon's breath particles in the stream; mobs and players in it take 4 dragon's breath
  damage per hit (ignores armor).
- **Recipes** ([dragon_breathing.js](../kubejs/server_scripts/magic/dragon_breathing.js), `R.dragonBreathing`):

| In | Out |
|---|---|
| bucket | shulker shell, 25% (the bucket is used up either way) |
| thick potion | dragon's breath |
| apple | chorus fruit |
| blaze rod | end rod |
| cobblestone | end stone |
| prismarine crystals | ender pearl |
| stone bricks / slab / stairs / wall / vertical slab | end stone bricks / slab / stairs / wall / vertical slab |
| dark prismarine | dusky myalite |
| prismarine | myalite |
| any corundum | myalite crystal |

- **JEI:** its own category (`DragonBreathingJEI`), with chances shown, catalysts: the fan and a dragon head.

### 2.3 Components

Every component has three recipes and a disassembly. Crafting: 4 of the part (`P P / _C_ / P P`) around a centre;
sequenced assembly: the centre as base, deploy 1 part, 3 loops; blueprint (`components`): 2 parts + the centre.
Disassembly (`disassembly_components` blueprint): component → 2 parts, centre lost.

| Component | Name | Part | Centre (crafting / assembly) | Centre (blueprint) |
|---|---|---|---|---|
| iron (IE) | Iron Mechanical | iron plate | copper rod | **copper ingot** (F4) |
| steel (IE) | Steel Mechanical | steel plate | iron rod | iron rod |
| copper | Copper Mechanical | copper plate | copper rod | copper rod |
| gold | Gold Electrical | gold plate | copper wire | copper wire |
| diamond | Diamond Electrical | diamond dust | electrum component | **gold component** (F4) |
| zinc | Zinc Mechanical | zinc plate | andesite alloy | andesite alloy |
| netherite | Netherite Thermal | netherite plate | constantan component | constantan component |
| aluminum | Aluminum Light | aluminum plate | nickel or silver ingot | same |
| silver | Silver Light | silver plate | nickel or aluminum ingot | same |
| nickel | Nickel Light | nickel plate | aluminum or silver ingot | same |
| lead | Lead Heavy | lead plate | uranium ingot | same |
| uranium | Uranium Heavy | uranium plate | lead ingot | same |
| brass | Brass Mechanical | brass plate | zinc small spring | same |
| electrum | Electrum Electrical | electrum plate | silver wire | same |
| constantan | Constantan Thermal | constantan plate | steel rod | same |

IE's own iron and steel component recipes (crafting and blueprint) are removed.

### 2.4 Wire coils and coil blocks

12 metals: copper, iron, gold, zinc, aluminum, lead, nickel, silver, brass, constantan, steel, electrum (copper,
electrum and steel coils are IE's items, the steel one being IE's structural steel rope; copper and electrum coil
blocks are IE's LV and MV; the pack adds 9 coil items and 10 coil blocks).

| Make | Crafting | Sequenced assembly | Blueprint (`electricalcomponents`) | Disassembly blueprint |
|---|---|---|---|---|
| Wire coil | 4 wire + spool → 4 | spool, deploy wire, 3 loops → 4 | 6 wire + spool → 8 | 4 coils → 3 wire (`disassembly_wire_coils`) |
| Coil block | 8 coils around an iron rod | iron rod, deploy coil, 6 loops | 4 coils + iron rod | block → 4 coils (`disassembly_coil_blocks`) |
| HV wire coil (`immersiveengineering:wirecoil_steel`) | 2 steel + 2 aluminum wire + spool → 4 | spool, steel then aluminum wire, 3 loops → 8 | 3 steel + 3 aluminum wire + spool → 8 | 8 coils → 3 steel or 3 aluminum wire |
| HV coil block (`immersiveengineering:coil_hv`) | 8 HV coils around an iron rod | 6 loops | 4 HV coils + iron rod | wirecutter + block → 4 HV coils |

Coil blocks are KubeJS blocks ([coil_blocks.js](../kubejs/startup_scripts/items/coil_blocks.js)): metal, hardness
3, pickaxe, iron tier.

### 2.5 Sample syringe

- **Items:** `creativeengineering:sample_syringe` (empty) and 7 filled ones (cow, pig, chicken, sheep, rabbit, cod,
  salmon), all unstackable, filled ones glinting; filled ones leave the empty syringe behind when used in crafting.
  Separate items rather than NBT so recipes can match by plain id.
- **Use:** right-click or hit a sampleable mob with the empty syringe in the main hand → it becomes the matching filled
  syringe, bottle-fill sound, "Sampled Cow." in chat. A filled syringe says "This syringe already has a sample."
- **Mobs:** cow, pig, chicken, sheep, rabbit, cod, salmon, and a More Mooblooms moobloom (gives a cow sample).
- **Recipe:** iron rod + 4 glass panes + iron plate. Filled syringes start the cultivated meat cultures
  ([cultivated_meat.js](../kubejs/server_scripts/cooking/cultivated_meat.js); backlog row 20).

## 3. What the pack changes

- **Sawmill:** three mixins and two duck interfaces.
  - `SawmillRecipeSerializerMixin` reads the optional `"blade"` string from the recipe JSON (JSON path only).
  - `SawmillProcessMixin` copies the installed blade's profile onto each item in the saw every tick, redirects IE's
    `SawmillRecipe.findRecipe` to `SawbladeRouting.route`, and saves the profile and the chosen recipe id in the
    process NBT, so the client (which never runs the server step and rebuilds its queue on every sync) shows the same
    recipe.
  - The routing classes are force-loaded at startup (`CreativeEngineering.java` lines 262-269): the mixin applies on the
    first sawmill tick, and a late class load there once crashed.
- **Sawmill recipes:** [generation_scripts/sawmill.js](../kubejs/generation_scripts/sawmill.js) is a node script that
  writes JSONs into `generation_scripts/recipes/sawmill/`, copied by hand into patcherMod and rebuilt. They're JSON, not
  `NCH.ieSawmill`, because kubejs-immersive-engineering's sawmill schema breaks secondaries.
- **Fan type:** ported from Create: Halitosis. `DragonBreathingFanProcessingType` registers into Create's
  `FanProcessingTypeRegistry` at common setup; `DragonBreathingRecipe` / `DragonBreathingRecipeType` are a
  `ProcessingRecipe` type `creativeengineering:dragon_breathing`; `CatalystUtils` reads the skull's direction.
- **Components, coils:** items in KubeJS ([items/components.js](../kubejs/startup_scripts/items/components.js)),
  recipes through `R.shaped` / `R.createSequencedAssembly` / `R.ieBlueprint` with transitional items from
  `transition_items.js`.
- **Syringe:** items in [items/sample_syringe.js](../kubejs/startup_scripts/items/sample_syringe.js), the mob list in
  `global.sampleSyringe`, handlers in [server_scripts/engineering/sample_syringe.js](../kubejs/server_scripts/engineering/sample_syringe.js)
  (`ItemEvents.entityInteracted` and `EntityEvents.hurt`).

## 4. Limits and decisions

### Findings (2026-10-18)

- **F1. The sawmill recipes are on a manual pipeline.** `node sawmill.js` → hand copy into patcherMod → jar rebuild,
  211 copies. The file's TODO plans an NCH helper adding them as raw JSON from `ServerEvents.highPriorityData`.
  In [TODO](todo/TODO.md) `engineering/sawmill-and-components #2`.
- **F2. JEI probably can't tell which blade a sawmill recipe needs. Your call.** The `"blade"` field is the pack's; IE's
  JEI page shows input and output only, so oak planks shows a dozen outputs with no hint which blade makes which.
  Check in game (EN-04); a tooltip line or a JEI info page would fix it.
- **F3. Raw JSON and literal ids:** the 12 blueprint recipes are hand-written files in
  `kubejs/data/creativeengineering/recipes/blueprint/`, and the `vertical_planks` / `railings` shape tags in
  [engineeringtags.js](../kubejs/server_scripts/tags/engineeringtags.js) list literal ids, against the NCH /
  item-dictionary rules (`R.ieBlueprint` handles NBT outputs).
- **F4. Component centres differ by route. Your call.** The iron component's blueprint takes a copper ingot where
  crafting and assembly take a copper rod; the diamond component's blueprint takes a gold component where the others
  take an electrum one. Pick one centre each. The comments are off too ("4 plates around a copper ingot", "deploy 3
  plates onto a copper ingot", "2 plates + 1 copper ingot").
- **F5. Component tier names are decided in the item names but not in the code comments.** `components.js` lines 18 and
  22 still say `--- Light? ---` / `--- Dense? ---`; the items are already "Aluminum Light Component" and "Uranium Heavy
  Component". [TODO](todo/TODO.md) `engineering/sawmill-and-components #6` lists this as undecided; it can be closed by deleting the comments, or the
  names changed.
- **F6. Disassembly comments promise a tool the recipes don't take.** The component, wire-coil and coil-block
  disassembly comments say a screwdriver or wire cutters is spent per craft; only the HV coil block's recipe lists the
  wirecutter. The wire-coil one also says "6 wire coils" (code: 4), and the HV assembly comment says 4 coils (code:
  8). Either add the tool or fix the comments.
- **F7. The bucket → shulker shell fan recipe eats buckets** 3 times in 4. Intended as a cost, or add the bucket back as
  a 75% output.
- **F8. `cultivated_meat.js` redefines `i.culture_base`** (line 18) although `item_dict.js` already has it. Harmless
  (same value), against the define-once rule. Belongs to backlog row 20.
- **F9. Component plans (scratchpad 2026-10-21).** In [TODO](todo/TODO.md) `engineering/sawmill-and-components`:
  - `#8` Remove every existing metal chain recipe and generate them all in KubeJS (an NCH helper) from a 2-D table of
    material x component type (exists or not); hand-tweaked chains are flagged and built separately.
  - `#9` Wire / wire coil / coil block alternative recipes by conductivity.
  - `#10` A cover for IE wire connectors.
  - `#11` Most electron tubes become a tag in recipes (visual ones like nixie tubes stay); fewer vacuum tubes do, lower
    tier tech only.
  - `#12` Redstone mechanism: low tier redstone component variety (rose quartz plate, electron tube tag, 2-3 repeats),
    more efficient low tier recipes (mechanism -> many repeaters, redstone links, Create redstone), used as the redstone
    in high tier components.
  - `#13` Gates: basic gates only, simple vanilla-redstone-like crafts, expensive at scale; Project Red for advanced
    gates, complex crafts but much cheaper at scale, for complex computing / redstone. Project Red (Core, Fabrication,
    Illumination, Integration, Transmission) is already in the pack; its gate recipes are unchanged.

### Known limits

- **A blade with no matching recipe** for the input falls back to the default; an input with no default and no matching
  profile passes through the sawmill unchanged.
- **Dragon Breathing stream hurts:** 4 armor-ignoring damage per hit to anything alive in it.
- **Syringe by hitting** also damages the mob (KubeJS's hurt event can't cancel it); a cod or salmon may die.

### Rejected options

- **NBT-tagged single syringe item:** NBT ingredient matching kept failing; one item per animal instead.
- **`NCH.ieSawmill` for the sawmill recipes:** the kjs-IE schema breaks secondaries (documented at the helper).

## 5. File map

- Sawmill: patcherMod [sawmill/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/sawmill/) (`SawbladeRouting`, `duck/SawmillProcessExt`, `duck/SawmillRecipeExt`), mixins `SawmillProcessMixin`, `SawmillRecipeMixin`, `SawmillRecipeSerializerMixin`, [item/PrecisionSawbladeItem.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/item/PrecisionSawbladeItem.java), `data/forge/tags/items/sawblades.json`, `data/creativeengineering/recipes/sawmill/` (211); [generation_scripts/sawmill.js](../kubejs/generation_scripts/sawmill.js); `kubejs/data/creativeengineering/recipes/{blueprint,sawmill}/`; [blueprints.js](../kubejs/server_scripts/engineering/blueprints.js) (blueprint item); [tooltips.js](../kubejs/client_scripts/tooltips.js) line 446.
- Fan: patcherMod [fan/](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/fan/), [compat/DragonBreathingJEI.java](../development/patcherMod/src/main/java/com/SaintAlfonzo/creativeengineering/compat/DragonBreathingJEI.java), `data/creativeengineering/tags/blocks/fan_processing_catalysts/dragon_breathing.json`; [dragon_breathing.js](../kubejs/server_scripts/magic/dragon_breathing.js); `NCH.dragonBreathing`.
- Components and coils: [items/components.js](../kubejs/startup_scripts/items/components.js), [items/coil_blocks.js](../kubejs/startup_scripts/items/coil_blocks.js), [components.js](../kubejs/server_scripts/engineering/components.js), [metal_base_materials.js](../kubejs/server_scripts/metal/metal_base_materials.js), [transition_items.js](../kubejs/startup_scripts/changes/transition_items.js).
- Syringe: [items/sample_syringe.js](../kubejs/startup_scripts/items/sample_syringe.js), [func/sample_syringe.js](../kubejs/startup_scripts/func/sample_syringe.js), [server_scripts/engineering/sample_syringe.js](../kubejs/server_scripts/engineering/sample_syringe.js).

## 6. Offline checks

None scripted. `node kubejs/generation_scripts/sawmill.js` regenerates the sawmill JSONs; diff them against
patcherMod's copies to see whether the jar is current.

## 7. In-game test suite

- [ ] **EN-01 Plain blade default:** IE Sawmill with a plain sawblade, feed oak planks → oak slabs (and sawdust).
- [ ] **EN-02 Profiled blade:** make a stairs precision sawblade (blueprint: sawblade + any wooden stairs + 2 steel
  plates), install it, feed oak planks → oak stairs. Tooltip says "Sawmill profile: Stairs".
- [ ] **EN-03 Fallback:** install a `sign` blade, feed something with no sign recipe but a default (e.g. a log) → the
  default output.
- [ ] **EN-04 JEI (F2):** JEI on oak planks (U) → note whether the sawmill pages say which blade each output needs.
- [ ] **EN-05 Client matches:** watch items in the running sawmill with a door blade → the item shown on the saw is the
  door path's, and the output is a door.
- [ ] **EN-06 Reload mid-cut:** `/reload` (or leave and rejoin) while planks are in a profiled sawmill → they finish as
  the profiled output; no crash.
- [ ] **EN-07 Dragon Breathing:** encased fan blowing into the back of a dragon head, drop cobblestone in the stream →
  end stone; purple particles.
- [ ] **EN-08 Thick potion → dragon's breath:** throw a thick potion into the stream → dragon's breath bottle.
- [ ] **EN-09 Orientation:** rotate the head so it faces sideways to the fan → no processing.
- [ ] **EN-10 Over water:** dragon head in a water source in the stream → still Dragon Breathing, not washing.
- [ ] **EN-11 Stream damage:** stand in the stream → about 4 damage at a time, armor doesn't reduce it.
- [ ] **EN-12 JEI page:** JEI shows "Dragon Breathing" with the fan and dragon head as catalysts, bucket → shulker shell
  at 25%.
- [ ] **EN-13 Component routes:** JEI on the gold component → crafting (4 gold plates + copper wire), sequenced assembly
  (3 loops), blueprint (2 plates + copper wire), disassembly (2 plates).
- [ ] **EN-14 IE components replaced:** JEI on the iron mechanical component → only the pack's recipes, not IE's.
- [ ] **EN-15 Coils:** 4 aluminum wire + spool → 4 aluminum wire coils; 8 around an iron rod → an aluminum coil block;
  the workbench disassembly gives 4 coils back.
- [ ] **EN-16 HV coil:** 2 steel + 2 aluminum wire + spool → 4 HV wire coils.
- [ ] **EN-17 Syringe:** right-click a cow with an empty syringe → glinting Sample Syringe (Cow), "Sampled Cow."; a
  moobloom gives the cow one too; right-click a zombie → nothing.
- [ ] **EN-18 Syringe used up:** cow syringe + culture base + slimeball → culture beef, empty syringe left in the grid.

## 8. History

- By 2026-09-27 (initial git commit): sawblade routing (with the late-class-load crash fix), precision sawblades, the
  sawmill generator, Dragon Breathing fan type and JEI page, components, coils, the syringe; shipped with 1.0.3.
- 2026-10-10 (More Mooblooms): moobloom added to the syringe list ([mooblooms.md](mooblooms.md) O5).
- 2026-10-18: this writeup; findings F1 to F8; tests EN-01 to EN-18. No code changed.
