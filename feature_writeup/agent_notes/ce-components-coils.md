# ce-components-coils

> creativeengineering component items, wire coil items, coil blocks — what's registered and their recipes

Moved from agent memory 2026-11-16 (last edited there 2026-09-08). Repo status docs win over any status line here.

Pack-added metal-chain items/blocks in the `creativeengineering` namespace, filling gaps IE/Create
leave. Registered in `kubejs/startup_scripts/items/{components,coil_blocks}.js`. Item/block
models + textures hand-authored on disk under `kubejs/assets/creativeengineering/`.

## Items / blocks

- **Component items** (`items/components.js`) — 13 `creativeengineering:component_<m>`: copper,
  gold, diamond, zinc, netherite, aluminum, lead, silver, nickel, uranium, brass, electrum,
  constantan. `i.component_iron` / `i.component_steel` = IE's `component_iron`/`_steel` (item
  sprite is `material_component_<m>`, not `component_<m>`). `i.component_diamond` DOES have a dict
  key (`item_dict.js` line ~26).
- **Wire coil items** — 9 `creativeengineering:wirecoil_<m>` (iron, gold, zinc, aluminum, lead,
  nickel, silver, brass, constantan). copper/electrum/steel = IE's `wirecoil_<m>`. No uranium
  coil (`i.*_uranium` coil keys = `i.none`).
- **Coil blocks** (`items/coil_blocks.js`, `StartupEvents.registry('block')`, `COIL_BLOCK_METALS`)
  — 10 `creativeengineering:coil_<m>`: the 9 wire-coil metals **+ steel** (added 2026-09-08).
  Hand-authored blockstate + `cube_bottom_top` model + block-item model on disk
  (`{blockstates,models/block,models/item}/coil_<m>.json`), textures
  `creativeengineering:block/coil_block/coil_<m>_{side,top}` (bottom = top). copper -> IE
  `coil_lv`, electrum -> IE `coil_mv` (still aliases in `item_dict`; steel is no longer an alias —
  it has its own block now).

## Recipes — DONE

### Wire coil + coil block — `server_scripts/metal/metal_base_materials.js`
Data-driven from `const COIL_METALS` (12: the 9 + copper, steel, electrum). **The same list is
duplicated in `startup_scripts/changes/transition_items.js` — keep both in sync.** `R.bulk(name,
COIL_METALS.map(m => [...]))` families per metal:
- wire coil: shaped (4 wire + spool -> 4), sequenced-assembly (spool base, deploy wire, loops 3),
  workbench blueprint `electricalcomponents` (3 wire + spool -> 6), **disassembly** blueprint
  (`[wirecutter, 6 wirecoil] → 3 wire`).
- coil block: shaped (8 wirecoil + iron rod -> 1), sequenced-assembly (iron-rod base, deploy
  wirecoil ×6), blueprint `electricalcomponents` (4 wirecoil + iron rod -> 1), **disassembly**
  (`[wirecutter, 1 coil_block] → 4 wirecoil`).
- Disassembly reverses the workbench blueprint minus spool/rod; exploit-safe. IE's
  `WirecutterItem.getCraftingRemainingItem` damages the wirecutter 1/craft and
  `BlueprintCraftingRecipe.consumeInputs` honors remainders, so it survives in the workbench.
- Recipe ids: `<m>_wire_coil`, `<m>_coil_block`, `<m>_{wire_coil,coil_block}_sequenced_assembly`,
  `<m>_{wire_coil,coil_block}_disassembly`. Coil-block core is always an **iron rod**.
- copper/steel/electrum recipes are extra paths alongside IE's stock `crafting/wirecoil_*` /
  `crafting/coil_{lv,mv,hv}` (steel's now produce the pack's own `coil_steel`).

### Components — `server_scripts/engineering/components.js`
- iron + steel: shaped (`['P P',' C ','P P']`, plate + copper ingot), sequenced-assembly (copper
  ingot base, deploy plate ×3), workbench blueprint `components` (2 plate + 1 copper ingot).
- the 13 pack components: **shapeless** (4 plate + 1 copper ingot, plates spelled out per slot —
  KubeJS doesn't expand `Item.of(x,N)` in shapeless, `writeInputItem` drops the count),
  **sequenced-assembly** (copper ingot base, deploy plate ×3), **blueprint `components`** (2
  plate + 1 copper ingot). `component_diamond` uses `minecraft:diamond` everywhere (no diamond
  plate). Copper ingot is a placeholder binder ("for now"). All 15 blueprint rows are in one
  merged `R.bulk('ieBlueprint', …)`.
- IE only ships `blueprint/component_{iron,steel,electronic}`, so all the pack recipes are new
  (the iron/steel blueprint rows functionally dup IE's, no id clash — different namespace).
- Recipe ids: `creativeengineering:{shapeless,blueprint/components}/component_<m>`,
  `createsequencedassembly/component_<m>_sequenced_assembly`.

## Transition / incomplete items — `startup_scripts/changes/transition_items.js`
`StartupEvents.registry('item')` -> **full restart** to pick up new keys, then `/reload` for
recipes. Registered: `coil_<m>` (12, auto gray-tint of the `wirecoil_<m>` sprite),
`coil_block_<m>` (12, explicit side texture — `creativeengineering:block/coil_block/coil_<m>_side`
for the 10, `immersiveengineering:block/metal_decoration/coil_{lv,mv}_side` for copper/electrum),
`component_iron`/`component_steel` (explicit, `material_component_<m>` sprite), and `component_<m>`
for the 13 pack components (`COMPONENT_METALS` forEach, auto texture — these are populated ahead
of any sequenced-assembly recipe using them).

Note: `server_scripts/_lib/metal_test_recipes.js` `seqForms` includes `wire_coil` + `component`,
so its mech-crafting grid generator auto-includes them for wired metals (dict-driven).

See memory `item-dict-mechanics`.
