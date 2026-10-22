# Unused textures audit

Audit 2026-10-22. Item and block textures in the mod jars that no model, data file, resource pack, KubeJS
file or class-file string refers to, plus a shortlist of the ones worth reusing for food or engineering items.

- Tool: `python3 tools/texture_audit.py` rewrites [unused_textures/all_unused.tsv](unused_textures/all_unused.tsv)
  (namespace, texture path, jar). `--sheet <list> <png>` renders a contact sheet from a list file.
- Contact sheets: [food.png](unused_textures/food.png), [engineering.png](unused_textures/engineering.png),
  [blocks.png](unused_textures/blocks.png) (lists: `food.txt`, `engineering.txt`, `blocks.txt` in the same folder).

## 1. Method and limits

1. Collect every `assets/<ns>/textures/{item,items,block,blocks}/**.png` in `mods/*.jar`, including jar-in-jar:
   27,470 textures, `minecraft` namespace skipped.
2. Drop any texture whose path appears in a JSON, `.mcmeta`, `.properties` or `.js` file in any jar, in
   `resourcepacks/` or in `kubejs/`: 4,589 left.
3. Drop any texture whose path appears as a string in a `.class` file (715), or whose file name matches a registered
   item id in `kubejs/exported/items_by_mod` (359): **3,535 left**.

Step 3 still lets through textures that code finds by building the path from pieces. These are false positives,
not spare art:

- Connected textures and states: `*_connected`, `*_on`, `*_powered`, `*_open`, AE2 glass frames, IE capacitor
  `*_in/_out/_none` sides.
- Fluid `*_flow` (Immersive Petroleum, PneumaticCraft, Create Confectionery).
- Colour or variant sets: Create belt scrolls, IE shader layers, ProjectRed gate surfaces, Simple Hats `_e`
  overlays, Ender Storage pouch buttons.
- Material sets that are generated per wood or stone: Every Compat (504), Dramatic Doors, Create and Create
  D&D `palettes/stone_types/alternatives`, Storage Delight bamboo and cherry.

Most of the 3,535 are of this kind or are decor: everycomp 504, create_dd 322, railways 233, oldguns 212,
create 198, immersiveengineering 195.

## 2. Food shortlist (sheet: food.png)

The ids are texture paths. To use one, point a KubeJS item at it: `.texture('create_dd:item/hot_chocolate')`,
the same way `baking_ingredients.js` uses `rusticdelight:item/cotton_boll`. The art stays inside its jar and
nothing is copied. If the mod is ever removed, the item shows the missing texture.

| Texture | Looks like | Possible use | License |
|---|---|---|---|
| `create_dd:item/{chocolate,glowberry,pumpkin,strawberry,vanilla}_milkshake` | glass mug, coloured shake + straw | milkshake set (pack already has `chocolate_milkshake` from another mod; the other four are new flavours) | MIT |
| `create_dd:item/hot_chocolate`, `hot_chocolate_bucket` | mug, bucket | hot chocolate drink and fluid bucket | MIT |
| `create_dd:item/crystallized_sap` | amber chunk | sap/syrup product, candy | MIT |
| `create_dd:item/frozen_nugget` | ice cube | ice cube for drinks or cooling | MIT |
| `create_factory:item/cream_bucket`, `spread_bucket` | white bucket, brown bucket | cream and chocolate/nut spread fluids | MIT |
| `create_things_and_misc:items/bowl_of_porridge` | green bowl | porridge or pea soup | AFL 3.0 |
| `create_things_and_misc:items/friendship_cake` | round dark cake | sponge or chocolate cake | AFL 3.0 |
| `create_confectionery:items/bar_of_chocolate` | brown bar | plain chocolate bar | AFL 3.0 |
| `moredelight:item/hamburger_with_egg_and_cabbage`, `ultimate_hamburger` | burgers | burger tiers | MIT |
| `moredelight:item/steak_and_egg_sandwich`, `toast_with_apple_jam` | sandwich, toast | breakfast foods | MIT |
| `fruitsdelight:item/jelly_bottle` (+ `jelly_content` tint mask) | jar | tinted jelly or jam jar | ARR |
| `buzzier_bees:item/clover_honey_bottle`, `crystallized_clover_honey`, `clover_leaf` | bottle, crystal, leaf | clover honey line; clover as herb | Abnormals license |
| `upgrade_aquatic:item/goose_egg` | large egg | goose or large egg | Abnormals license |
| `upgrade_aquatic:item/barnacle_tongue` | red curl | odd meat ingredient | Abnormals license |
| `goldenfood:item/golden_apple` | gold apple | alternate golden apple art | ARR |
| `farmersrespite:item/tea_kettle` | copper kettle | kettle tool or icon | MIT |
| `the_dirty_stuff:items/dried_tobacco_leaf`, `crop_package` | dried leaves, bundle | old art for existing items (pack has `dried_tobacco_leafs`) | ARR |
| `immersiveengineering:item/fluid_containers_bottle_{plantoil,ethanol,biodiesel,creosote}` | corked bottles | plant oil / cooking oil bottle; ethanol as spirit base | Blu (IE) |
| `immersiveengineering:item/fluid_containers_bucket_{plantoil,ethanol}` | buckets | fluid buckets for those | Blu (IE) |
| `everythingcopper:item/copper_water_bucket`, `copper_lava_bucket` | copper buckets | copper bucket tier | ARR |

Not usable as item icons: `displaydelight:block/*` (bacon_sandwich, ctd_smoked_eggplant, small_plate, tray) are
model UV maps. `delightful:block/cantaloupe_{side,inside,rind}` and `melon_inside` are full block faces and would
suit a cantaloupe block or a cut-melon block.

## 3. Engineering shortlist (sheet: engineering.png)

| Texture | Looks like | Possible use | License |
|---|---|---|---|
| `create:item/integrated_circuit` | blue board | circuit tier between pack circuits | MIT |
| `create:item/lapis_sheet`, `copper_ingot`, `antioxidant`, `logistical_dial` | sheet, ingot, vial, dial | lapis sheet; alternate copper ingot; coolant or anti-corrosion item; gauge part | MIT |
| `create_dd:item/inductive_mechanism` (+ `incomplete_`) | mechanism with coil | electrical-tier mechanism, sequenced-assembly ready | MIT |
| `create_dd:item/boat_mechanism`, `compound_base`, `magnet`, `rubber_belt` | linkage, grey lump, horseshoe magnet, black belt | ship part; alloy base; magnet; belt component | MIT |
| `create_dd:item/mithril_{ingot,nugget,sheet}`, `blaze_gold`(+`_sheet`), `overcharge_alloy`(+`_sheet`) | ingots and sheets | extra alloy tiers with matching sheets | MIT |
| `create_dd:item/shadow_steel_sheet`, `refined_radiance_sheet`, `experience_ingot`, `spectral_ruby`, `stargaze_singularity` | sheets, ingot, gem | late-game materials | MIT |
| `create_dd:item/pipebomb` | grey canister | explosive charge | MIT |
| `createaddition:item/iron_spool`, `overcharged_capacitor`, `connector_gold` | spool, glowing capacitor, connector | iron wire spool; capacitor tier; gold connector | MIT |
| `create_things_and_misc:items/obsidian_sandpaper`, `railway_casing` | sandpaper, framed casing | sandpaper tier; casing | AFL 3.0 |
| `ae2:item/wooden_gear`, `nether_quartz_dust` | gear, dust | wooden gear; quartz dust (IE `metal_dust_quartz` is a second option) | AE2 (see repo) |
| `immersiveengineering:item/metal_dust_quartz` | grey dust | quartz dust | Blu (IE) |
| `pneumaticcraft:item/drill_bit` | drill bit | generic drill bit part | GPLv3 |
| `projectred_fabrication:item/valid_die_template`, `ic_inert` | chip template, blue IC | chip blank; inert IC | MIT |
| `projectred_core:item/null_routing_chip` | card chip | blank chip or punch card | MIT |
| `bobberdetector:item/zircon` | red gem | zircon or ruby gem | MIT |
| `blocky_siege:items/cannon_ball` | black ball | cannonball or shot | AFL 3.0 |
| `storagedrawers:items/upgrade_{buffer,lock,sorting}` | drawer upgrades | upgrade icons | MIT |
| `lightmanscurrency:item/upgrade_smithing_template` | template | 1.20-style smithing template stand-in | ARR |
| `yuushya:item/tool_bundle` | green screen device | handheld terminal or tablet | see mod |
| `create_enchantment_industry:item/printing_template` | printed plate | printing plate or stencil | MIT |
| `davebuildingmod:items/{green,red}_circuit_*`, `vocalcard` | green and red circuit cards, many variants | circuit card set (empty card plus a dozen stamped variants per colour) | MIT |
| `oldguns:item/*` parts (see below) | gunsmith parts | mechanical parts | AGPL-3.0 |

oldguns has about 200 unused textures, mostly cut gun variants. The parts on the sheet are general enough to be
engineering items: `breech_block`, `flint_wheel`, `{steel,diamond,titanium}_rifling_bit`, `rifle_bolt`,
`stone_gear_set`, `tether_ropes`, `powder_charge`, `{small,medium,large}_musket_ball_mold`,
`*_iron_artillery_parts`, `*_revolving_cylinder`, `*_iron_rifled_barrel`, `*_cannonball`, `wall_mount`.

Not usable: `littlelogistics:item/cutter` (red X placeholder), `yuushya:item/blueprint` (blank page),
`cb_microblock:item/saw` and `immersiveengineering:item/buzzsaw_diesel` (model UV maps),
`immersiveengineering:item/fluorescent_tube_rod` (one model part).

## 4. Block shortlist (sheet: blocks.png)

Single faces. A machine or decor block would need a model that uses them.

| Texture | Looks like | Possible use |
|---|---|---|
| `create_dd:block/industrial_heater`, `industrial_heater_on` | grate, glowing grate | heater or burner face, off and on |
| `createaddition:block/crude_burner/front`, `front_on`, `furnace_burner/front_on` | copper firebox, stone furnace | burner fronts, off and on |
| `create_dd:block/kinetic_divider`, `kinetic_equalizer`, `kinetic_multiplier`, `accelerator_motor` | brass-framed gearbox faces | kinetic machine faces |
| `create_dd:block/hydraulic_encased_pipe`, `industrial_gearbox_top`, `chute_smart`, `brass_bricks` | copper pipe face, grate, chute, brick | casing and decor |
| `createaddition:block/overcharged_casing/overcharged_casing` | glowing blue casing | high-tier casing |
| `create_things_and_misc:blocks/klaxon`, `speakerfront`, `copper_pylon`, `portable_vault`, `copper_plating` | horn, speaker, pole, vault, plate | alarm or speaker block; copper plate block |
| `create_compressed:blocks/sturdy_steel_sheet_block`, `andesite_crate`, `rose_quartz_block` | steel plate, crate, pink block | storage blocks |
| `dustrial_decor:block/cardboard_box_side` | cardboard | cardboard box or packaging |
| `create:block/machine_base_top`, `immersivepetroleum:block/well_top` | frame with hole | machine top or pump face |
| `geode:block/pink_topaz_block` | pink crystal block | gem storage block |
| `cookingforblockheads:block/cooking_table_front` | dark cabinet | kitchen cabinet face |

## 5. Licensing

The License column is the `license` field from each jar's `mods.toml`. Pointing a KubeJS item at the in-jar path
does not redistribute anything, so license mostly matters only if art is copied into
`kubejs/assets/creativeengineering` or a pack mod. ARR (all rights reserved) mods: fruitsdelight, goldenfood,
the_dirty_stuff, everythingcopper, lightmanscurrency, create_compressed, immersivepetroleum, geode,
cookingforblockheads. Copy art from those only with the author's permission. oldguns is AGPL-3.0, which can be
combined with the pack's GPLv3.

## 6. Open

Nothing has been added yet. Pick from sections 2 to 4 and add the item through `global.item` / `item_dict.js`
with a `.texture()` that points at the path in the table.
