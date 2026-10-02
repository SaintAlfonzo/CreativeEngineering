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
| Missing-name catalog, part 1: partial blocks | started: offline pass done (section 9), `/dumpnames` written but not run yet (NM-07, NM-08) |
| Catalog part 2: items, effects, potions, enchantments, entities, fluids, professions | offline counts only (section 10). Needs the `/dumpnames` export |

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
- [ ] **NM-07 /dumpnames:** in single player run `/dumpnames` → green chat line with "missing: block a/b, item ...",
  no yellow dedicated-server warning, and `kubejs/exported/names.json` exists. No errors in `logs/latest.log`
  (grep `dumpnames`).
- [ ] **NM-08 Catalog from the dump:** `python3 tools/missing_names.py` prints `source: dump`, and section 9 says "from
  the `/dumpnames` export". Tell Claude, so the triage in section 9 can be redone from real data.

## 8. History

- 2026-10-02: Display Delight names (205, generated). Rustic Delight coffee crop and potted wild plants named,
  your bell pepper and cotton keys checked. `lang_index.py`, `missing_names.py`, `/dumpnames` added. Catalog
  started with the offline pass over partial blocks.

## 9. Catalog part 1: partial blocks

### Triage of the offline pass (2026-10-02)

Read by hand from the generated tables below. "Real?" is a guess until the `/dumpnames` export confirms it.

| Kind | Rows | Real? | Proposed fix |
|---|---|---|---|
| Crops | `endercrop:ender_crop`, `supplementaries:rope_tomatoes` / `stick_tomatoes`, `vinery:vinery_vine`, `botania:solid_vine` | likely real | Ender Crop, Tomatoes on Rope, Tomatoes on Stick, Solid Vine; `vinery_vine` needs a look in game first |
| Potted | `immersiveengineering:potted_hemp`, `nethersdelight:potted_mimicarnation`, `upgrade_aquatic:potted_river_sapling`, `vinery:potted_apple_tree_sapling` / `potted_cherry_tree_sapling` | likely real | "Potted " + plant name |
| Placed food | `create_central_kitchen:cherry_pie`, `mulberry_pie`, `pumpkin_pie`, `truffle_pie` | real if registered (they depend on which pie mods are installed) | Placed Cherry Pie, ... (same rule as Display Delight) |
| Cauldrons | 64 `colored_water:*_water_cauldron`, 4 `everythingcopper:*copper_powder_snow_cauldron` | likely real | "<Colour> Water Cauldron", "Copper Powder Snow Cauldron". A generator, like Display Delight |
| Fluid blocks | 14 Immersive Petroleum and 9 IE `*_fluid_block`, 3 PneumaticCraft, 2 SecurityCraft fake water/lava | likely real (WTHIT shows them with fluids on) | the fluid's name ("Crude Oil", "Creosote Oil", ...) |
| Wall variants | 73 | expected: they borrow the item's name | none |
| Other | 918 offline | mostly compat-block noise | wait for the dump |

<!-- BEGIN GENERATED: partial-blocks -->
Generated by `python3 tools/missing_names.py` on 2026-10-02 12:36 from an **offline** scan of 15546 blockstates and the item dump of 2026-09-30 (has false positives, see section 4; run `/dumpnames` in single player for the real list). **1101 partial blocks have no name** (no item shares their id, so they only show in WTHIT, F3 and death messages).

| Kind | Count |
|---|---|
| crop | 5 |
| potted | 5 |
| placed food | 4 |
| cauldron variant | 68 |
| wall variant | 73 |
| fluid block | 28 |
| other | 918 |

### Crop (5)

Growing crops and climbing plants. The item is the seed, so only WTHIT shows the block name.

| Mod | Count | Ids |
|---|---|---|
| supplementaries | 2 | `rope_tomatoes`, `stick_tomatoes` |
| botania | 1 | `solid_vine` |
| endercrop | 1 | `ender_crop` |
| vinery | 1 | `vinery_vine` |

### Potted (5)

Flower-pot variants: one block per plant.

| Mod | Count | Ids |
|---|---|---|
| vinery | 2 | `potted_apple_tree_sapling`, `potted_cherry_tree_sapling` |
| immersiveengineering | 1 | `potted_hemp` |
| nethersdelight | 1 | `potted_mimicarnation` |
| upgrade_aquatic | 1 | `potted_river_sapling` |

### Placed food (4)

Food placed as a block (pies, feasts, Display Delight plates). Display Delight is named by `tools/gen_display_delight_names.py`.

| Mod | Count | Ids |
|---|---|---|
| create_central_kitchen | 4 | `cherry_pie`, `mulberry_pie`, `pumpkin_pie`, `truffle_pie` |

### Cauldron variant (68)

Filled-cauldron blocks. WTHIT shows these when you look at the cauldron.

| Mod | Count | Ids |
|---|---|---|
| colored_water | 64 | `black_water_cauldron`, `blue_water_cauldron`, `brown_water_cauldron`, `condense_black_water_cauldron`, `condense_blue_water_cauldron`, `condense_brown_water_cauldron`, `condense_cyan_water_cauldron`, `condense_gray_water_cauldron`, `condense_green_water_cauldron`, `condense_light_blue_water_cauldron`, `condense_light_gray_water_cauldron`, `condense_lime_water_cauldron`, `condense_magenta_water_cauldron`, `condense_orange_water_cauldron`, `condense_pink_water_cauldron`, `condense_purple_water_cauldron`, `condense_red_water_cauldron`, `condense_white_water_cauldron`, `condense_yellow_water_cauldron`, `cyan_water_cauldron`, `gray_water_cauldron`, `green_water_cauldron`, `light_blue_water_cauldron`, `light_gray_water_cauldron`, `lime_water_cauldron`, `luminous_black_water_cauldron`, `luminous_blue_water_cauldron`, `luminous_brown_water_cauldron`, `luminous_condense_black_water_cauldron`, `luminous_condense_blue_water_cauldron`, `luminous_condense_brown_water_cauldron`, `luminous_condense_cyan_water_cauldron`, `luminous_condense_gray_water_cauldron`, `luminous_condense_green_water_cauldron`, `luminous_condense_light_blue_water_cauldron`, `luminous_condense_light_gray_water_cauldron`, `luminous_condense_lime_water_cauldron`, `luminous_condense_magenta_water_cauldron`, `luminous_condense_orange_water_cauldron`, `luminous_condense_pink_water_cauldron`, `luminous_condense_purple_water_cauldron`, `luminous_condense_red_water_cauldron`, `luminous_condense_white_water_cauldron`, `luminous_condense_yellow_water_cauldron`, `luminous_cyan_water_cauldron`, `luminous_gray_water_cauldron`, `luminous_green_water_cauldron`, `luminous_light_blue_water_cauldron`, `luminous_light_gray_water_cauldron`, `luminous_lime_water_cauldron`, `luminous_magenta_water_cauldron`, `luminous_orange_water_cauldron`, `luminous_pink_water_cauldron`, `luminous_purple_water_cauldron`, `luminous_red_water_cauldron`, `luminous_white_water_cauldron`, `luminous_yellow_water_cauldron`, `magenta_water_cauldron`, `orange_water_cauldron`, `pink_water_cauldron`, `purple_water_cauldron`, `red_water_cauldron`, `white_water_cauldron`, `yellow_water_cauldron` |
| everythingcopper | 4 | `copper_powder_snow_cauldron`, `exposed_copper_powder_snow_cauldron`, `oxidized_copper_powder_snow_cauldron`, `weathered_copper_powder_snow_cauldron` |

### Wall variant (73)

Expected, not a gap: wall torches, signs, banners and heads return their item's name (vanilla's own wall banners have no key either). Only a dump row here is a real gap.

| Mod | Count | Ids |
|---|---|---|
| upgrade_aquatic | 22 | `acan_coral_wall_fan`, `branch_coral_wall_fan`, `chrome_coral_wall_fan`, `dead_acan_coral_wall_fan`, `dead_branch_coral_wall_fan`, `dead_chrome_coral_wall_fan`, `dead_finger_coral_wall_fan`, `dead_moss_coral_wall_fan`, `dead_petal_coral_wall_fan`, `dead_pillow_coral_wall_fan`, `dead_rock_coral_wall_fan`, `dead_silk_coral_wall_fan`, `dead_star_coral_wall_fan`, `elder_prismarine_coral_wall_fan`, `finger_coral_wall_fan`, `moss_coral_wall_fan`, `petal_coral_wall_fan`, `pillow_coral_wall_fan`, `prismarine_coral_wall_fan`, `rock_coral_wall_fan`, `silk_coral_wall_fan`, `star_coral_wall_fan` |
| farmersdelight | 17 | `black_canvas_wall_sign`, `blue_canvas_wall_sign`, `brown_canvas_wall_sign`, `canvas_wall_sign`, `cyan_canvas_wall_sign`, `gray_canvas_wall_sign`, `green_canvas_wall_sign`, `light_blue_canvas_wall_sign`, `light_gray_canvas_wall_sign`, `lime_canvas_wall_sign`, `magenta_canvas_wall_sign`, `orange_canvas_wall_sign`, `pink_canvas_wall_sign`, `purple_canvas_wall_sign`, `red_canvas_wall_sign`, `white_canvas_wall_sign`, `yellow_canvas_wall_sign` |
| minecraft | 16 | `black_wall_banner`, `blue_wall_banner`, `brown_wall_banner`, `cyan_wall_banner`, `gray_wall_banner`, `green_wall_banner`, `light_blue_wall_banner`, `light_gray_wall_banner`, `lime_wall_banner`, `magenta_wall_banner`, `orange_wall_banner`, `pink_wall_banner`, `purple_wall_banner`, `red_wall_banner`, `white_wall_banner`, `yellow_wall_banner` |
| supplementaries | 6 | `enderman_wall_head`, `sconce_wall_ender`, `sconce_wall_glow`, `sconce_wall_green`, `sconce_wall_nether_brass`, `sconce_wall_soul` |
| quark | 4 | `ancient_wall_sign`, `azalea_wall_sign`, `bamboo_wall_sign`, `blossom_wall_sign` |
| framedblocks | 3 | `framed_redstone_wall_torch`, `framed_soul_wall_torch`, `framed_wall_torch` |
| architects_palette | 1 | `nether_brass_wall_torch` |
| botania | 1 | `gaia_wall_head` |
| neapolitan | 1 | `chimpanzee_wall_head` |
| nethersdelight | 1 | `propelplant_wall_torch` |
| oldguns | 1 | `wall_blasting_powder_stick_block` |

### Fluid block (28)

Shown by WTHIT when looking at a fluid with the fluid option on, and in some death messages.

| Mod | Count | Ids |
|---|---|---|
| immersivepetroleum | 14 | `benzene_fluid_block`, `crudeoil_fluid_block`, `diesel_fluid_block`, `diesel_sulfur_fluid_block`, `ethylene_fluid_block`, `gasoline_additives_fluid_block`, `gasoline_fluid_block`, `kerosene_fluid_block`, `lubricant_cracked_fluid_block`, `lubricant_fluid_block`, `napalm_fluid_block`, `naphtha_cracked_fluid_block`, `naphtha_fluid_block`, `propylene_fluid_block` |
| immersiveengineering | 9 | `acetaldehyde_fluid_block`, `biodiesel_fluid_block`, `concrete_fluid_block`, `creosote_fluid_block`, `ethanol_fluid_block`, `herbicide_fluid_block`, `phenolic_resin_fluid_block`, `plantoil_fluid_block`, `redstone_acid_fluid_block` |
| pneumaticcraft | 3 | `ethanol`, `gasoline`, `memory_essence` |
| securitycraft | 2 | `fake_lava_block`, `fake_water_block` |

### Other (918)

Technical blocks (moving pieces, multiblock parts, light/air blocks) included. Many are never seen; name the ones that show in WTHIT.

Offline this bucket is mostly blockstates for compat blocks that only register when another mod is installed (Lightman's Currency, Dramatic Doors), so only counts are listed.

| Mod | Count |
|---|---|
| lightmanscurrency | 750 |
| dramaticdoors | 59 |
| securitycraft | 22 |
| supplementaries | 18 |
| another_furniture | 10 |
| exposure | 7 |
| redeco | 6 |
| immersiveengineering | 5 |
| immersive_weathering | 4 |
| vintageimprovements | 4 |
| buzzier_bees | 3 |
| engineersdecor | 3 |
| pneumaticcraft | 3 |
| bellsandwhistles | 2 |
| cookingforblockheads | 2 |
| createaddition | 2 |
| dynamictrees | 2 |
| immersivepetroleum | 2 |
| suppsquared | 2 |
| architects_palette | 1 |
| botania | 1 |
| cb_multipart | 1 |
| create_factory | 1 |
| decorative_blocks | 1 |
| everythingcopper | 1 |
| ftbquests | 1 |
| myrtrees | 1 |
| rare-ice | 1 |
| storagedelight | 1 |
| storagedrawers | 1 |
| upgrade_aquatic | 1 |
<!-- END GENERATED: partial-blocks -->

## 10. Catalog part 2: everything else

<!-- BEGIN GENERATED: other-kinds -->
Same run. Blocks that do have an item but no `block.*` key are listed with items (the item shows the block key in JEI).

### Items and full blocks (1341)

Offline this count is mostly false positives: Everycomp writes its lang at runtime, and mods like Lightman's Currency build names in code. Counts only; `/dumpnames` gives the real ids.

| Mod | Count |
|---|---|
| everycomp | 831 |
| lightmanscurrency | 195 |
| creativeengineering | 97 |
| supplementaries | 50 |
| immersive_weathering | 38 |
| handcrafted | 36 |
| suppsquared | 23 |
| armor_trims | 17 |
| immersiveengineering | 17 |
| everythingcopper | 12 |
| fulleng | 7 |
| trashcans | 4 |
| storagedrawers | 3 |
| oldguns | 2 |
| portablemobs | 2 |
| alexsmobs | 1 |
| cb_microblock | 1 |
| customizableelytra | 1 |
| decorative_blocks | 1 |
| endersdelight | 1 |
| engineersdecor | 1 |
| immersivepetroleum | 1 |

### Effects (1)

| Effect | Key |
|---|---|
| `everythingcopper:copper_poisoning` | `effect.everythingcopper.copper_poisoning` |

### Potions (0)

A potion needs four keys: the bottle, splash, lingering and tipped arrow. The table lists the forms that fall back to the raw key.

None.

Enchantments, entities, fluids and villager professions need the `/dumpnames` export.
<!-- END GENERATED: other-kinds -->
