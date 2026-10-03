# Names (missing translation keys)

Anything the game can't translate shows its raw key instead of a name: `block.displaydelight.beef_stew`
in JEI, `block.rusticdelight.coffee` in WTHIT, `effect.everythingcopper.copper_poisoning` on the potion
HUD. This writeup covers the names the pack adds, and keeps a catalog of what is still missing so it can
be worked down. The catalog starts with **partial blocks**: blocks with no item of their own (crops,
potted plants, placed food, filled cauldrons, fluid blocks). They never appear in JEI, so nobody
notices them until WTHIT shows a raw key.

## 0. At a glance

| Part | Status |
|---|---|
| Display Delight: all 205 placed-food blocks named | BUILT, in-game check pending (NM-01 to NM-04) |
| Rustic Delight crops and potted plants | BUILT, in-game check pending (NM-05, NM-06). Your `bell_peppers` and `cotton` keys were right; 4 more added |
| Missing-name catalog, part 1: partial blocks | from the real `/dumpnames` export (2026-10-02 21:54, via `/dumpall`): 191 unnamed; triage redone (section 9). NM-07, NM-08 PASSED |
| Catalog part 2: items, effects, potions, enchantments, entities, fluids, professions | from the dump (section 10): 65 items, 1 effect, 2 potion forms, 53 entities, 2 fluids, 20 professions. No fixes yet |

- **Where names go:**
  - Hand-written names go in [namechanges.js](../kubejs/client_scripts/namechanges.js).
  - Bulk names come from a generator, which writes `kubejs/assets/<mod>/lang/en_us.json`.
  - A name set by an enabled resource-pack zip beats both (see section 1).
- **Regenerate:**
  - Display Delight names: `python3 tools/gen_display_delight_names.py`. Add `--check` to only test whether the file is current.
  - The catalog: run `/dumpnames` **in single player**, then `python3 tools/missing_names.py`.
- **Files:**
  - [lang_index.py](../tools/lang_index.py): every en_us key the client can see, with its source.
  - [gen_display_delight_names.py](../tools/gen_display_delight_names.py), [missing_names.py](../tools/missing_names.py).
  - [dump_names.js](../kubejs/server_scripts/_lib/dump_names.js): the `/dumpnames` command. `/dumpall` runs it too.

## 1. How a name is found

1. The game asks the block, item, effect or other entry for its **description id**. By default that is
   built from the registry id (`block.<mod>.<id>`, `item.<mod>.<id>`, ...). A mod can override it.
2. It looks that key up in the loaded language. If the key is missing, it shows the key itself.
3. The language is merged from every resource pack, highest last. In this pack, from lowest to highest:
   - vanilla
   - mod jars
   - KubeJS: `kubejs/assets/*/lang`, the `ClientEvents.lang` calls in client scripts, `kubejs/assets/*.zip`
   - the resource-pack zips enabled in `options.txt` (memory note: resource-pack-lang-priority)

Who reads which key:

- **JEI and tooltips** show the *item* name. A block item uses the block's key (`block.<mod>.<id>`), so
  naming a block also names its item.
- **WTHIT, the F3 screen and death messages** show the *block* name. That is the only place a partial
  block's name appears.

## 2. Reference: key forms

| Kind | Key | Example | Overridden by |
|---|---|---|---|
| Block | `block.<mod>.<id>` | `block.rusticdelight.coffee` | wall torches, signs, banners and heads use their item's name |
| Item | `item.<mod>.<id>` (block items: `block.*`) | `item.farmersdelight.beef_stew` | potions, Lightman's Currency trader blocks build names in code |
| Effect | `effect.<mod>.<id>` | `effect.everythingcopper.copper_poisoning` | Alex's Mobs uses `alexsmobs.potion.<id>` |
| Potion (4 keys) | `item.minecraft.{potion,splash_potion,lingering_potion,tipped_arrow}.effect.<name>` | `item.minecraft.tipped_arrow.effect.haste` | `<name>` is the base name: long and strong variants share it |
| Enchantment | `enchantment.<mod>.<id>` | | |
| Entity | `entity.<mod>.<id>` | | |
| Fluid | `fluid_type.<mod>.<type id>` | `fluid_type.create_confectionery.caramel` | the fluid *type* id, which can differ from the fluid id |
| Villager profession | `entity.minecraft.villager.<id>`, or `.<mod>:<id>` outside minecraft | `entity.minecraft.villager.immersiveengineering:engineer` | |

## 3. What the pack changes

### 3.1 Display Delight (all blocks)

Display Delight 1.2.0 registers 207 blocks: every food you can place by sneaking, on the floor, on a
plate or on a small plate. Its lang file names only the two empty plates. 121 of the blocks have a block
item, so JEI listed 119 raw `block.displaydelight.*` keys. WTHIT showed the same keys for all 205.

`tools/gen_display_delight_names.py` names all 205. Each block's loot table drops the food it holds (plus
the plate), and the name is built from that food's in-game name. Because it goes through
`lang_index.py`, our own renames count too.

| Block id | Rule | Name |
|---|---|---|
| `beef_stew` | Placed + food name | Placed Beef Stew |
| `plated_hamburger` | Plated + food name | Plated Hamburger |
| `small_plated_apple_pie_slice` | Small Plated + food name | Small Plated Slice of Apple Pie |
| `honey_glazed_ham` | the food's own name already says "Plate of" | Placed Plate of Honey Glazed Ham |
| `ad_plated_bison_burger` | Alex's Delight food | Plated Bison Burger |
| `lm_omurice` | food mod (Large Meals) not installed: from the block id | Placed Omurice |
| `bowl` | override | Placed Bowl |

- **Food mod not installed.** 108 blocks belong to food mods the pack doesn't have: Corn Delight,
  Cultural Delights, Expanded Delight, Festive Delight, Large Meals, Ocean's Delight, Pineapple Delight.
  Their names come from the block id, minus DD's prefix (`cd_`, `ctd_`, `ed_`, `fd_`, `lm_`, `od_`, `pd_`).
  This also skips two typos in DD's loot tables (`festive_chiken`, `pineapple_pie_side`).
- **Dead JEI entries.** 46 of the 121 JEI entries are for those absent mods. You can't get the food, so
  you can't place them. They are candidates for `JEIEvents.hideItems` (not done; your call).
- **Output.** The names go to `kubejs/assets/displaydelight/lang/en_us.json`. That file is generated:
  edit the rule or `OVERRIDES` in the tool, not the json.

### 3.2 Rustic Delight

Rustic Delight 1.3.0's lang file leaves out 6 blocks: its three crops (bell peppers, cotton, coffee) and
the three potted wild plants. All 6 are named in
[namechanges.js](../kubejs/client_scripts/namechanges.js):

| Key | Name | Note |
|---|---|---|
| `block.rusticdelight.bell_peppers` | Bell Peppers | yours, key correct (crop block, 6 ages) |
| `block.rusticdelight.cotton` | Cotton | yours, key correct (crop block) |
| `block.rusticdelight.coffee` | Coffee Plant | crop block. Not "Coffee": that is the drink item `rusticdelight:coffee` |
| `block.rusticdelight.potted_wild_bell_peppers` | Potted Wild Bell Peppers | vanilla style "Potted ..." |
| `block.rusticdelight.potted_wild_coffee` | Potted Wild Coffee | |
| `block.rusticdelight.potted_wild_cotton` | Potted Wild Cotton | |

Vanilla calls its crops "Wheat Crops", "Carrots", "Potatoes", "Beetroots". Farmer's Delight uses the
plural form ("Cabbages", "Tomatoes"), so "Bell Peppers" fits. "Coffee Plant" breaks the plural pattern
on purpose, so it can't be mistaken for the drink.

## 4. Limits and decisions

- **The offline scan over-reports.** `missing_names.py` without a dump can't see three things:
  - description ids built in code (wall variants, Alex's Mobs effects, Lightman's Currency)
  - lang written at runtime (Everycomp, the Moonlight / Immersive Weathering / Supplementaries
    generated packs, KubeJS builders)
  - whether a blockstate's block is registered at all. Lightman's Currency and Dramatic Doors ship
    blockstates for compat blocks that only register when the other mod is installed.

  The `/dumpnames` export fixes all three: it uses each entry's real `getDescriptionId()` and the game's
  own `Language.has()`. Until it has been run, read the offline crop, potted, placed-food, cauldron and
  fluid rows as leads. Read the item and "other" counts as noise.
- **`/dumpnames` must run in single player.** A dedicated server loads only vanilla and mod-jar lang, so
  every resource-pack and KubeJS name would look missing. The command says so in chat, marks the file
  `dedicated: true`, and `missing_names.py` then falls back to the offline scan.
- **Display Delight names live in a lang json, not namechanges.js.** 205 generated lines would bury the
  hand-written names. A generated json can be regenerated after a DD update.
- **Rejected: naming placed food exactly like the food.** JEI would then list two "Beef Stew" entries, a
  placeable block and the food, with nothing to tell them apart. "Placed" and "Plated" also read
  correctly in WTHIT.
- **Rejected: reading the food name at runtime inside `ClientEvents.lang`.** Whether other mods' lang
  is loaded by then depends on load order. The generator runs offline and gives a file you can check.

## 5. File map

- [tools/lang_index.py](../tools/lang_index.py): key index (vanilla jar from `~/.minecraft/versions/1.19.2/`,
  Forge client jar, mods/, KubeJS, enabled zips). `python3 tools/lang_index.py <key>...` shows a key's
  value and source.
- [tools/gen_display_delight_names.py](../tools/gen_display_delight_names.py) writes
  `kubejs/assets/displaydelight/lang/en_us.json`.
- [tools/missing_names.py](../tools/missing_names.py) rewrites sections 9 and 10 of this file.
- [kubejs/server_scripts/_lib/dump_names.js](../kubejs/server_scripts/_lib/dump_names.js): `/dumpnames` writes
  `kubejs/exported/names.json`.
- [kubejs/client_scripts/namechanges.js](../kubejs/client_scripts/namechanges.js): hand-written names (Rustic
  Delight block).

## 6. Offline checks

- `python3 tools/gen_display_delight_names.py --check` prints "up to date (205 names)". PASSED 2026-10-02.
- `python3 tools/lang_index.py block.rusticdelight.coffee block.displaydelight.beef_stew`: both found,
  with sources `kubejs/client_scripts` and `kubejs/assets`. PASSED 2026-10-02.
- `dump_names.js` compiles in the pack's Rhino fork (rhino-forge 1902). PASSED 2026-10-02. Whether it runs
  correctly is only proven in game (NM-07).
- `python3 tools/missing_names.py --print` gives the counts without touching this file.

## 7. In-game test suite

Restart the game, or reload resources with F3+T (lang only).

- [ ] **NM-01 JEI names:** search `@displaydelight` in JEI → every entry has a name ("Placed ...", "Plated ...",
  "Small Plated ...", "Food Plate", "Small Food Plate"), no `block.displaydelight.` keys.
- [ ] **NM-02 Placed bowl food:** sneak-place Beef Stew on the floor and look at it with WTHIT → "Placed Beef Stew".
- [ ] **NM-03 Plated:** place a Food Plate, sneak-place a Hamburger on it → WTHIT "Plated Hamburger"; breaking it
  drops the hamburger and the plate.
- [ ] **NM-04 Small plate:** a Small Food Plate with a Slice of Apple Pie → WTHIT "Small Plated Slice of Apple Pie".
- [ ] **NM-05 Rustic Delight crops:** plant bell pepper, cotton and coffee seeds → WTHIT "Bell Peppers", "Cotton",
  "Coffee Plant" at every growth stage.
- [ ] **NM-06 Potted wild plants:** put Wild Bell Peppers, Wild Coffee and Wild Cotton in flower pots → WTHIT
  "Potted Wild Bell Peppers", "Potted Wild Coffee", "Potted Wild Cotton".
- [x] **NM-07 /dumpnames:** in single player run `/dumpnames` → green chat line with "missing: block a/b, item ...",
  no yellow dedicated-server warning, and `kubejs/exported/names.json` exists. No errors in `logs/latest.log`
  (grep `dumpnames`). PASSED 2026-10-02 21:54 (through `/dumpall`): `missing: block 237/15007, item 65/18413, effect
  1/142, enchantment 0/100, entity 53/526, fluid 2/483, profession 20/42, potion 2/119`.
- [x] **NM-08 Catalog from the dump:** `python3 tools/missing_names.py` prints `source: dump`, and section 9 says "from
  the `/dumpnames` export". Tell Claude, so the triage in section 9 can be redone from real data. PASSED 2026-10-02
  22:11; triage redone the same day.

## 8. History

- 2026-10-02: Display Delight names (205, generated). Rustic Delight coffee crop and potted wild plants named,
  your bell pepper and cotton keys checked. `lang_index.py`, `missing_names.py`, `/dumpnames` added. Catalog
  started with the offline pass over partial blocks.
- 2026-10-02 (late): `/dumpnames` ran through `/dumpall` in single player (NM-07, NM-08 PASSED). Catalog
  regenerated from the dump (191 partial blocks, 65 items, 53 entities...) and the section 9 triage redone from it.
  The new 1.0.4 mods only add the Untitled Duck Mod rows.

## 9. Catalog part 1: partial blocks

### Triage from the `/dumpnames` export (2026-10-02)

Read by hand from the generated tables below, which now come from the game's own lang check, so every row is real.
The offline pass had over-reported: Ender Crop, `vinery_vine`, `potted_mimicarnation`, the Central Kitchen mulberry and
pumpkin pies, the Everything Copper cauldrons and all 73 wall variants are named in game (or not registered).

| Kind | Rows | Proposed fix |
|---|---|---|
| Crops (3) | `supplementaries:rope_tomatoes` / `stick_tomatoes`, `botania:solid_vine` | Tomatoes on Rope, Tomatoes on Stick, Solid Vine |
| Potted (4) | `immersiveengineering:potted_hemp`, `upgrade_aquatic:potted_river_sapling`, `vinery:potted_apple_tree_sapling` / `potted_cherry_tree_sapling` | "Potted " + plant name |
| Placed food (2) | `create_central_kitchen:cherry_pie`, `truffle_pie` | Placed Cherry Pie, Placed Truffle Pie (Display Delight rule) |
| Cauldrons (64) | `colored_water:*_water_cauldron` (plain, condense, luminous) | "<Colour> Water Cauldron" with the variant word, from a generator like Display Delight |
| Fluid blocks (92) | 64 Colored Water, 14 Immersive Petroleum, 9 IE, 3 PneumaticCraft, 2 SecurityCraft fake water/lava | the fluid's name; Colored Water from the same generator as its cauldrons |
| Other (26) | technical blocks | name only what WTHIT shows: IP `well` / `well_pipe`, Supplementaries rope chandeliers and skull candles, `myrtrees:filled_rubberwood_log`, `rare-ice:rare_ice`. Leave fake light/air, moving and structure parts |

New with the 1.0.4 mods (section 10): Untitled Duck Mod's Intimidation tipped arrows (2 potion forms) and its thrown
duck and goose egg entities. Fruits Delight, Farmer's Respite, Corn Delight, More Delight, Well and Blue Flame name
everything.

<!-- BEGIN GENERATED: partial-blocks -->
Generated by `python3 tools/missing_names.py` on 2026-10-02 22:11 from the `/dumpnames` export of 2026-10-02 (15007 blocks, the game's own lang check). **191 partial blocks have no name** (no item shares their id, so they only show in WTHIT, F3 and death messages).

| Kind | Count |
|---|---|
| crop | 3 |
| potted | 4 |
| placed food | 2 |
| cauldron variant | 64 |
| wall variant | 0 |
| fluid block | 92 |
| other | 26 |

### Crop (3)

Growing crops and climbing plants. The item is the seed, so only WTHIT shows the block name.

| Mod | Count | Ids |
|---|---|---|
| supplementaries | 2 | `rope_tomatoes`, `stick_tomatoes` |
| botania | 1 | `solid_vine` |

### Potted (4)

Flower-pot variants: one block per plant.

| Mod | Count | Ids |
|---|---|---|
| vinery | 2 | `potted_apple_tree_sapling`, `potted_cherry_tree_sapling` |
| immersiveengineering | 1 | `potted_hemp` |
| upgrade_aquatic | 1 | `potted_river_sapling` |

### Placed food (2)

Food placed as a block (pies, feasts, Display Delight plates). Display Delight is named by `tools/gen_display_delight_names.py`.

| Mod | Count | Ids |
|---|---|---|
| create_central_kitchen | 2 | `cherry_pie`, `truffle_pie` |

### Cauldron variant (64)

Filled-cauldron blocks. WTHIT shows these when you look at the cauldron.

| Mod | Count | Ids |
|---|---|---|
| colored_water | 64 | `black_water_cauldron`, `blue_water_cauldron`, `brown_water_cauldron`, `condense_black_water_cauldron`, `condense_blue_water_cauldron`, `condense_brown_water_cauldron`, `condense_cyan_water_cauldron`, `condense_gray_water_cauldron`, `condense_green_water_cauldron`, `condense_light_blue_water_cauldron`, `condense_light_gray_water_cauldron`, `condense_lime_water_cauldron`, `condense_magenta_water_cauldron`, `condense_orange_water_cauldron`, `condense_pink_water_cauldron`, `condense_purple_water_cauldron`, `condense_red_water_cauldron`, `condense_white_water_cauldron`, `condense_yellow_water_cauldron`, `cyan_water_cauldron`, `gray_water_cauldron`, `green_water_cauldron`, `light_blue_water_cauldron`, `light_gray_water_cauldron`, `lime_water_cauldron`, `luminous_black_water_cauldron`, `luminous_blue_water_cauldron`, `luminous_brown_water_cauldron`, `luminous_condense_black_water_cauldron`, `luminous_condense_blue_water_cauldron`, `luminous_condense_brown_water_cauldron`, `luminous_condense_cyan_water_cauldron`, `luminous_condense_gray_water_cauldron`, `luminous_condense_green_water_cauldron`, `luminous_condense_light_blue_water_cauldron`, `luminous_condense_light_gray_water_cauldron`, `luminous_condense_lime_water_cauldron`, `luminous_condense_magenta_water_cauldron`, `luminous_condense_orange_water_cauldron`, `luminous_condense_pink_water_cauldron`, `luminous_condense_purple_water_cauldron`, `luminous_condense_red_water_cauldron`, `luminous_condense_white_water_cauldron`, `luminous_condense_yellow_water_cauldron`, `luminous_cyan_water_cauldron`, `luminous_gray_water_cauldron`, `luminous_green_water_cauldron`, `luminous_light_blue_water_cauldron`, `luminous_light_gray_water_cauldron`, `luminous_lime_water_cauldron`, `luminous_magenta_water_cauldron`, `luminous_orange_water_cauldron`, `luminous_pink_water_cauldron`, `luminous_purple_water_cauldron`, `luminous_red_water_cauldron`, `luminous_white_water_cauldron`, `luminous_yellow_water_cauldron`, `magenta_water_cauldron`, `orange_water_cauldron`, `pink_water_cauldron`, `purple_water_cauldron`, `red_water_cauldron`, `white_water_cauldron`, `yellow_water_cauldron` |

### Fluid block (92)

Shown by WTHIT when looking at a fluid with the fluid option on, and in some death messages.

| Mod | Count | Ids |
|---|---|---|
| colored_water | 64 | `black_fluid_block`, `blue_fluid_block`, `brown_fluid_block`, `condense_black_fluid_block`, `condense_blue_fluid_block`, `condense_brown_fluid_block`, `condense_cyan_fluid_block`, `condense_gray_fluid_block`, `condense_green_fluid_block`, `condense_light_blue_fluid_block`, `condense_light_gray_fluid_block`, `condense_lime_fluid_block`, `condense_magenta_fluid_block`, `condense_orange_fluid_block`, `condense_pink_fluid_block`, `condense_purple_fluid_block`, `condense_red_fluid_block`, `condense_white_fluid_block`, `condense_yellow_fluid_block`, `cyan_fluid_block`, `gray_fluid_block`, `green_fluid_block`, `light_blue_fluid_block`, `light_gray_fluid_block`, `lime_fluid_block`, `luminous_black_fluid_block`, `luminous_blue_fluid_block`, `luminous_brown_fluid_block`, `luminous_condense_black_fluid_block`, `luminous_condense_blue_fluid_block`, `luminous_condense_brown_fluid_block`, `luminous_condense_cyan_fluid_block`, `luminous_condense_gray_fluid_block`, `luminous_condense_green_fluid_block`, `luminous_condense_light_blue_fluid_block`, `luminous_condense_light_gray_fluid_block`, `luminous_condense_lime_fluid_block`, `luminous_condense_magenta_fluid_block`, `luminous_condense_orange_fluid_block`, `luminous_condense_pink_fluid_block`, `luminous_condense_purple_fluid_block`, `luminous_condense_red_fluid_block`, `luminous_condense_white_fluid_block`, `luminous_condense_yellow_fluid_block`, `luminous_cyan_fluid_block`, `luminous_gray_fluid_block`, `luminous_green_fluid_block`, `luminous_light_blue_fluid_block`, `luminous_light_gray_fluid_block`, `luminous_lime_fluid_block`, `luminous_magenta_fluid_block`, `luminous_orange_fluid_block`, `luminous_pink_fluid_block`, `luminous_purple_fluid_block`, `luminous_red_fluid_block`, `luminous_white_fluid_block`, `luminous_yellow_fluid_block`, `magenta_fluid_block`, `orange_fluid_block`, `pink_fluid_block`, `purple_fluid_block`, `red_fluid_block`, `white_fluid_block`, `yellow_fluid_block` |
| immersivepetroleum | 14 | `benzene_fluid_block`, `crudeoil_fluid_block`, `diesel_fluid_block`, `diesel_sulfur_fluid_block`, `ethylene_fluid_block`, `gasoline_additives_fluid_block`, `gasoline_fluid_block`, `kerosene_fluid_block`, `lubricant_cracked_fluid_block`, `lubricant_fluid_block`, `napalm_fluid_block`, `naphtha_cracked_fluid_block`, `naphtha_fluid_block`, `propylene_fluid_block` |
| immersiveengineering | 9 | `acetaldehyde_fluid_block`, `biodiesel_fluid_block`, `concrete_fluid_block`, `creosote_fluid_block`, `ethanol_fluid_block`, `herbicide_fluid_block`, `phenolic_resin_fluid_block`, `plantoil_fluid_block`, `redstone_acid_fluid_block` |
| pneumaticcraft | 3 | `ethanol`, `gasoline`, `memory_essence` |
| securitycraft | 2 | `fake_lava_block`, `fake_water_block` |

### Other (26)

Technical blocks (moving pieces, multiblock parts, light/air blocks) included. Many are never seen; name the ones that show in WTHIT.

| Mod | Count | Ids |
|---|---|---|
| supplementaries | 8 | `block_generator`, `edelwood_stick`, `propelplant_cane`, `rope_chandelier`, `rope_soul_chandelier`, `skull_candle_soul`, `skull_candle_soul_wall`, `skull_candle_wall` |
| vintageimprovements | 4 | `centrifuge_structure`, `helve_kinetic`, `helve_structure`, `lathe_moving` |
| immersiveengineering | 2 | `fake_light`, `toolbox_block` |
| immersivepetroleum | 2 | `well`, `well_pipe` |
| pneumaticcraft | 2 | `drone_redstone_emitter`, `kerosene_lamp_light` |
| securitycraft | 2 | `horizontal_reinforced_iron_bars`, `sentry_disguise` |
| botania | 1 | `fake_air` |
| cb_multipart | 1 | `multipart` |
| exposure | 1 | `flash` |
| myrtrees | 1 | `filled_rubberwood_log` |
| nock_enough_arrows | 1 | `light_air` |
| rare-ice | 1 | `rare_ice` |
<!-- END GENERATED: partial-blocks -->

## 10. Catalog part 2: everything else

<!-- BEGIN GENERATED: other-kinds -->
Same run. Blocks that do have an item but no `block.*` key are listed with items (the item shows the block key in JEI).

### Items and full blocks (65)

| Mod | Count | Ids |
|---|---|---|
| armor_trims | 17 | `coast_armor_trim_smithing_template`, `dune_armor_trim_smithing_template`, `eye_armor_trim_smithing_template`, `host_armor_trim_smithing_template`, `netherite_upgrade_smithing_template`, `raiser_armor_trim_smithing_template`, `rib_armor_trim_smithing_template`, `sentry_armor_trim_smithing_template`, `shaper_armor_trim_smithing_template`, `silence_armor_trim_smithing_template`, `snout_armor_trim_smithing_template`, `spire_armor_trim_smithing_template`, `tide_armor_trim_smithing_template`, `vex_armor_trim_smithing_template`, `ward_armor_trim_smithing_template`, `wayfinder_armor_trim_smithing_template`, `wild_armor_trim_smithing_template` |
| immersiveengineering | 17 | `armor_piercing`, `buckshot`, `casull`, `dragons_breath`, `fake_icon_birthday`, `fake_icon_bttf`, `fake_icon_drillbreak`, `fake_icon_fried`, `fake_icon_lucky`, `fake_icon_ravenholm`, `firework`, `flare`, `he`, `homing`, `potion`, `silver`, `wolfpack` |
| everythingcopper | 12 | `copper_axe`, `copper_boots`, `copper_chestplate`, `copper_helmet`, `copper_hoe`, `copper_horse_armor`, `copper_leggings`, `copper_pickaxe`, `copper_shears`, `copper_shield`, `copper_shovel`, `copper_sword` |
| immersive_weathering | 7 | `sandy_cobblestone_vertical_slab`, `sandy_stone_brick_vertical_slab`, `sandy_stone_vertical_slab`, `snow_brick_vertical_slab`, `snowy_cobblestone_vertical_slab`, `snowy_stone_brick_vertical_slab`, `snowy_stone_vertical_slab` |
| storagedrawers | 3 | `keyring_drawer`, `keyring_quantify`, `keyring_shroud` |
| alexsmobs | 1 | `stink_ray_empty_inventory` |
| cb_microblock | 1 | `microblock` |
| decorative_blocks | 1 | `blockstate_copy_item` |
| engineersdecor | 1 | `test_block` |
| everycomp | 1 | `all_woods` |
| fulleng | 1 | `requester_terminal` |
| immersivepetroleum | 1 | `debug` |
| oldguns | 1 | `bombard` |
| supplementaries | 1 | `checker_vertical_slab` |

### Effects (1)

| Effect | Key |
|---|---|
| `everythingcopper:copper_poisoning` | `effect.everythingcopper.copper_poisoning` |

### Potions (2)

A potion needs four keys: the bottle, splash, lingering and tipped arrow. The table lists the forms that fall back to the raw key.

| Potion name | Registered as | Missing forms |
|---|---|---|
| `intimidation` | `untitledduckmod:intimidation` | arrow |
| `intimidation` | `untitledduckmod:long_intimidation` | arrow |

### Enchantments (0)

None.

### Entities (53)

| Mod | Count | Ids |
|---|---|---|
| create_things_and_misc | 7 | `projectile_blaze_balloon_fire`, `projectile_neon_tube`, `projectile_spout_gun_chocolate`, `projectile_spout_gun_honey`, `projectile_spout_gun_lava`, `projectile_spout_gun_slime`, `projectile_spout_gun_water` |
| quark | 6 | `echorang`, `flamerang`, `parrot_egg`, `skull_pike`, `stool`, `torch_arrow` |
| blocky_siege | 4 | `projectile_ice_drop`, `projectile_keg_of_gunpowder`, `projectile_lava_drop`, `projectile_wooden_shrapnel` |
| immersive_weathering | 3 | `falling_icicle`, `falling_layer`, `falling_propagule` |
| immersiveengineering | 3 | `explosive`, `fluorescent_tube`, `skyline_hook` |
| supplementaries | 3 | `bomb`, `falling_sack`, `pearl_marker` |
| alexsmobs | 2 | `emu_egg`, `ice_shard` |
| domesticationinnovation | 2 | `giant_bubble`, `highlighted_block` |
| oldguns | 2 | `bombard`, `rocket_projectile` |
| pneumaticcraft | 2 | `ring`, `vortex` |
| securitycraft | 2 | `bouncingbetty`, `securitycamera` |
| terraform | 2 | `boat`, `chest_boat` |
| untitledduckmod | 2 | `duck_egg`, `goose_egg` |
| canned_goods | 1 | `projectile_mini_fizz` |
| cccbridge | 1 | `animatronic` |
| creativeengineering | 1 | `shrapnel` |
| decorative_blocks | 1 | `dummy_entity` |
| doapi | 1 | `chair` |
| engineersdecor | 1 | `et_chair` |
| immersivepetroleum | 1 | `molotov` |
| prettypipes | 1 | `pipe_frame` |
| redeco | 1 | `seat` |
| sit | 1 | `entity_sit` |
| sophisticatedbackpacks | 1 | `everlasting_backpack_item` |
| twigs | 1 | `pebble` |
| vinery | 1 | `chair` |

### Fluids (2)

| Mod | Count | Ids |
|---|---|---|
| decorative_blocks | 2 | `flowing_thatch`, `thatch` |

### Professions (20)

| Mod | Count | Ids |
|---|---|---|
| mca | 6 | `adventurer`, `archer`, `cultist`, `guard`, `mercenary`, `outlaw` |
| dynamicvillage | 4 | `hydraulic_engineer`, `mechanical_engineer`, `miner`, `train_mechanic` |
| chefsdelight | 2 | `delightchef`, `delightcook` |
| lightmanscurrency | 2 | `banker`, `cashier` |
| ae2 | 1 | `fluix_researcher` |
| beautify | 1 | `botanist` |
| cloudstorage | 1 | `balloon_salesman` |
| domesticationinnovation | 1 | `animal_tamer` |
| vinery | 1 | `winemaker` |
| wares | 1 | `packager` |
<!-- END GENERATED: other-kinds -->
