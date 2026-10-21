# Names (missing translation keys, display-name audit)

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
| Catalog part 2: items, effects, potions, enchantments, entities, fluids, professions | from the dump (section 10): 65 items, 1 effect, 2 potion forms, 53 entities, 2 fluids, 20 professions. Fixed by the audit (section 11) |
| Display-name audit: raw keys + capitalization, every kind | BUILT 2026-10-20 (section 11): 436 fixes; NM-09 to NM-11 PASSED, NM-12 pending |

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
- **Wanted rename (scratchpad 2026-10-21):** the chocolate spider eye becomes "Chocolate Night Berries", in line with
  Canned Goods' `canned_spider_eyes`, already "Night Berries". [TODO](todo/TODO.md) `ui/names #6`.

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
- 2026-10-20: display-name audit (section 11): client dump `/cedumpnames` (patcherMod 1.1.01), `tools/name_audit.py`,
  `name_rules.json`, `client_scripts/name_fixes.js`. 436 names fixed (raw keys and Title Case), all 59 namespaces
  through the KubeJS lang event; none needed a resource-pack zip edited. WTHIT plugin toggle names added (wthit_lines F2).
- 2026-10-20 (later): Pretty Pipes keeps its mod name and creative tab (your call; the items stay "Item Pipe ...").
  All 430 mod display names combed (section 12): 116 renamed through CEConfig `[mod_names]` (author handles dropped,
  Target Dummy, your mods marked "(Alfonzo)").
  NM-09, NM-10 PASSED.

## 9. Catalog part 1: partial blocks

### Triage from the `/dumpnames` export (2026-10-02)

Historical: every row below was fixed by the display-name audit on 2026-10-20 (section 11); the generated tables
after it are from the post-fix dump and list only keys that are missing but show a name built in code.

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
Generated by `python3 tools/missing_names.py` on 2026-10-20 16:06 from the `/dumpnames` export of 2026-10-20 (14259 blocks, the game's own lang check). **2 partial blocks have no name** (no item shares their id, so they only show in WTHIT, F3 and death messages).

| Kind | Count |
|---|---|
| crop | 0 |
| potted | 0 |
| placed food | 0 |
| cauldron variant | 0 |
| wall variant | 0 |
| fluid block | 0 |
| other | 2 |

### Other (2)

Technical blocks (moving pieces, multiblock parts, light/air blocks) included. Many are never seen; name the ones that show in WTHIT.

| Mod | Count | Ids |
|---|---|---|
| supplementaries | 2 | `rope_chandelier`, `rope_soul_chandelier` |
<!-- END GENERATED: partial-blocks -->

## 10. Catalog part 2: everything else

<!-- BEGIN GENERATED: other-kinds -->
Same run. Blocks that do have an item but no `block.*` key are listed with items (the item shows the block key in JEI).

### Items and full blocks (48)

| Mod | Count | Ids |
|---|---|---|
| armor_trims | 17 | `coast_armor_trim_smithing_template`, `dune_armor_trim_smithing_template`, `eye_armor_trim_smithing_template`, `host_armor_trim_smithing_template`, `netherite_upgrade_smithing_template`, `raiser_armor_trim_smithing_template`, `rib_armor_trim_smithing_template`, `sentry_armor_trim_smithing_template`, `shaper_armor_trim_smithing_template`, `silence_armor_trim_smithing_template`, `snout_armor_trim_smithing_template`, `spire_armor_trim_smithing_template`, `tide_armor_trim_smithing_template`, `vex_armor_trim_smithing_template`, `ward_armor_trim_smithing_template`, `wayfinder_armor_trim_smithing_template`, `wild_armor_trim_smithing_template` |
| everythingcopper | 12 | `copper_axe`, `copper_boots`, `copper_chestplate`, `copper_helmet`, `copper_hoe`, `copper_horse_armor`, `copper_leggings`, `copper_pickaxe`, `copper_shears`, `copper_shield`, `copper_shovel`, `copper_sword` |
| immersiveengineering | 11 | `armor_piercing`, `buckshot`, `casull`, `dragons_breath`, `firework`, `flare`, `he`, `homing`, `potion`, `silver`, `wolfpack` |
| potionbundles | 3 | `lingering_potion_bundle`, `potion_bundle`, `splash_potion_bundle` |
| storagedrawers | 3 | `keyring_drawer`, `keyring_quantify`, `keyring_shroud` |
| cb_microblock | 1 | `microblock` |
| immersivepetroleum | 1 | `debug` |

### Effects (0)

None.

### Potions (0)

A potion needs four keys: the bottle, splash, lingering and tipped arrow. The table lists the forms that fall back to the raw key.

None.

### Enchantments (0)

None.

### Entities (0)

None.

### Fluids (0)

None.

### Professions (0)

None.
<!-- END GENERATED: other-kinds -->

## 11. Display-name audit (raw keys and capitalization)

Started 2026-10-20 at your request: every name with a raw lang key gets a real name, and every displayed
item and block name is checked for Title Case. Sections 9 and 10 above are the older missing-key catalog
(server `/dumpnames`); this section supersedes their "proposed fix" columns.

**How it works**

1. **Dump** (patcherMod `devtools/DisplayNameDump`, client side). `/cedumpnames` in game, or
   `[debug.names] dumpAtTitle = true` in creativeengineering-common.toml on a client that never joins a world,
   writes `kubejs/exported/display_names.json`: every block, item, effect, enchantment, entity, fluid,
   potion form and villager profession with its key, the text the game shows, and the pack + namespace whose
   lang file set the key. It also rewrites `names.json` in `/dumpnames`' format, so `missing_names.py` keeps
   working, and it works in multiplayer (the server's `/dumpnames` only works in single player).
2. **Audit** (`python3 tools/name_audit.py`, `--print` to preview, `--check` to test). Two checks:
   - **raw**: the shown text is a key (`block.mod.id`) or a bare id. A missing key alone isn't enough:
     IE cartridges, armor trim templates and Everything Copper tools build their names in code and look fine.
     The fix borrows a related name first (a fluid block takes its fluid's name, a wall variant its standing
     block's, a projectile or broom entity its item's), then falls back to the id in Title Case.
   - **case**: vanilla-style Title Case. Every word capitalized except small words mid-name ("Block of Iron",
     "Flint and Steel", "Lily of the Valley"); hyphen and slash parts count as words ("High-Grade",
     "Password-Protected", "Head/Foot"); stray spaces trimmed.
3. **Rules** (`kubejs/generation_scripts/names/name_rules.json`, hand-written): `names` (exact names, e.g.
   "Tomatoes on Rope", "Placed Cherry Pie"), `keep` (names that are right as written), `keepWords`,
   the small-word lists, `skip`.
4. **Apply**: the tool writes `kubejs/generation_scripts/names/name_fixes.json`;
   [name_fixes.js](../kubejs/client_scripts/name_fixes.js) adds each name with `ClientEvents.lang` **in the
   namespace that set the key**. The game merges lang per namespace, not per pack: KubeJS's two-argument
   `e.add(key, name)` files the key under `minecraft`, and a key defined under two namespaces goes to
   whichever namespace the resource manager lists last, whatever the pack order. Same namespace + the KubeJS
   pack (above mod jars and generated packs) = the fix always wins. It still loses to the resource-pack zips;
   a bad name from a zip or from our own KubeJS lang is listed under "fix at the source" instead.

**Decisions (kept on purpose, in `keep`):**
- Blocky Siege's cannon projectiles ("a cannon wielding fire bombs") and Create GBD's "a Guardian Beam Turret"
  are written to be read inside a death message ("was killed by a cannon"), so they stay lowercase.
- "eXclusive-OR (XOR) Gate" and "eXclusive-Not-OR (XNOR) Gate" are styled on purpose; "AE2 Cable and/or Bus"
  and Vinery's "Better than Golden Apples!" effect read correctly as they are.
- Alex's Mobs' model-only items ("This is not a Falconry Glove") are sentences, not names.
- "o'" and "'n'" stay lowercase ("Bottle o' Enchanting", "Link 'n Pin"); "Jack-o-Lantern Hat" became vanilla's
  "Jack o'Lantern Hat", "Cod n Chips" became "Cod 'n' Chips".
- Securitycraft's fake water and lava blocks take the real fluid's name ("Water", "Lava"): that is the disguise.

**Tests:**
- [x] **NM-09 Dump:** a scratch client with `dumpAtTitle` writes both files at the title screen (latest.log
  `[cedumpnames] 59807 lang keys traced`). PASSED 2026-10-20 15:53.
- [x] **NM-10 Audit offline:** `python3 tools/name_audit.py --check` prints "up to date". PASSED 2026-10-20.
- [x] **NM-11 Fixes applied:** a second dump after the fixes: every fixed name shows, the audit finds nothing new,
  and a rerun on that dump writes the same `name_fixes.json` (fixes carried forward). PASSED 2026-10-20 16:05 (scratch
  client: `[name fixes] 436 names in 59 namespaces`; missing keys down to block 2, item 48, all of them names built in
  code: IE cartridges, armor trim templates, Everything Copper tools, Supplementaries chandeliers).
- [ ] **NM-12 In game:** JEI search `witchmobility` shows "Goldenwood Broom" (was `item.witchmobility.goldenwood_broom_item`);
  `@floralench lily` shows "Lily of the Valley Bush"; WTHIT on an Upgrade Aquatic wall coral fan shows "Acan Coral
  Fan"; a sconce on a wall shows "Ender Sconce".

<!-- BEGIN GENERATED: name-audit -->
Generated by `python3 tools/name_audit.py` on 2026-10-20 16:06 from the client name dump of 2026-10-20 16:05 (item 18631, block 14259, effect 140, enchantment 100, entity 542, fluid 519, potion 476, profession 42).

| Kind | Raw key fixed | Capitalization fixed | Named by rule |
|---|---|---|---|
| item | 24 | 105 | 2 |
| block | 225 | 0 | 0 |
| effect | 1 | 0 | 0 |
| enchantment | 0 | 0 | 0 |
| entity | 55 | 1 | 0 |
| fluid | 2 | 0 | 0 |
| potion | 1 | 0 | 0 |
| profession | 20 | 0 | 0 |

**436 fixes** in `name_fixes.json`, 0 to fix at the source, 0 reported only.

### Fix at the source

None.

### Reported only

None.

<details><summary>Raw keys fixed (328)</summary>


| Kind | Id | Was | Now | Namespace |
|---|---|---|---|---|
| block | `botania:fake_air` | block.botania.fake_air | Fake Air | botania |
| block | `botania:solid_vine` | block.botania.solid_vine | Solid Vine | botania |
| block | `cb_multipart:multipart` | block.cb_multipart.multipart | Multipart | cb_multipart |
| block | `colored_water:black_fluid_block` | block.colored_water.black_fluid_block | Black Water | colored_water |
| block | `colored_water:black_water_cauldron` | block.colored_water.black_water_cauldron | Black Water Cauldron | colored_water |
| block | `colored_water:blue_fluid_block` | block.colored_water.blue_fluid_block | Blue Water | colored_water |
| block | `colored_water:blue_water_cauldron` | block.colored_water.blue_water_cauldron | Blue Water Cauldron | colored_water |
| block | `colored_water:brown_fluid_block` | block.colored_water.brown_fluid_block | Brown Water | colored_water |
| block | `colored_water:brown_water_cauldron` | block.colored_water.brown_water_cauldron | Brown Water Cauldron | colored_water |
| block | `colored_water:condense_black_fluid_block` | block.colored_water.condense_black_fluid_block | Condense Black Water | colored_water |
| block | `colored_water:condense_black_water_cauldron` | block.colored_water.condense_black_water_cauldron | Condense Black Water Cauldron | colored_water |
| block | `colored_water:condense_blue_fluid_block` | block.colored_water.condense_blue_fluid_block | Condense Blue Water | colored_water |
| block | `colored_water:condense_blue_water_cauldron` | block.colored_water.condense_blue_water_cauldron | Condense Blue Water Cauldron | colored_water |
| block | `colored_water:condense_brown_fluid_block` | block.colored_water.condense_brown_fluid_block | Condense Brown Water | colored_water |
| block | `colored_water:condense_brown_water_cauldron` | block.colored_water.condense_brown_water_cauldron | Condense Brown Water Cauldron | colored_water |
| block | `colored_water:condense_cyan_fluid_block` | block.colored_water.condense_cyan_fluid_block | Condense Cyan Water | colored_water |
| block | `colored_water:condense_cyan_water_cauldron` | block.colored_water.condense_cyan_water_cauldron | Condense Cyan Water Cauldron | colored_water |
| block | `colored_water:condense_gray_fluid_block` | block.colored_water.condense_gray_fluid_block | Condense Gray Water | colored_water |
| block | `colored_water:condense_gray_water_cauldron` | block.colored_water.condense_gray_water_cauldron | Condense Gray Water Cauldron | colored_water |
| block | `colored_water:condense_green_fluid_block` | block.colored_water.condense_green_fluid_block | Condense Green Water | colored_water |
| block | `colored_water:condense_green_water_cauldron` | block.colored_water.condense_green_water_cauldron | Condense Green Water Cauldron | colored_water |
| block | `colored_water:condense_light_blue_fluid_block` | block.colored_water.condense_light_blue_fluid_block | Condense Light Blue Water | colored_water |
| block | `colored_water:condense_light_blue_water_cauldron` | block.colored_water.condense_light_blue_water_cauldron | Condense Light Blue Water Cauldron | colored_water |
| block | `colored_water:condense_light_gray_fluid_block` | block.colored_water.condense_light_gray_fluid_block | Condense Light Gray Water | colored_water |
| block | `colored_water:condense_light_gray_water_cauldron` | block.colored_water.condense_light_gray_water_cauldron | Condense Light Gray Water Cauldron | colored_water |
| block | `colored_water:condense_lime_fluid_block` | block.colored_water.condense_lime_fluid_block | Condense Lime Water | colored_water |
| block | `colored_water:condense_lime_water_cauldron` | block.colored_water.condense_lime_water_cauldron | Condense Lime Water Cauldron | colored_water |
| block | `colored_water:condense_magenta_fluid_block` | block.colored_water.condense_magenta_fluid_block | Condense Magenta Water | colored_water |
| block | `colored_water:condense_magenta_water_cauldron` | block.colored_water.condense_magenta_water_cauldron | Condense Magenta Water Cauldron | colored_water |
| block | `colored_water:condense_orange_fluid_block` | block.colored_water.condense_orange_fluid_block | Condense Orange Water | colored_water |
| block | `colored_water:condense_orange_water_cauldron` | block.colored_water.condense_orange_water_cauldron | Condense Orange Water Cauldron | colored_water |
| block | `colored_water:condense_pink_fluid_block` | block.colored_water.condense_pink_fluid_block | Condense Pink Water | colored_water |
| block | `colored_water:condense_pink_water_cauldron` | block.colored_water.condense_pink_water_cauldron | Condense Pink Water Cauldron | colored_water |
| block | `colored_water:condense_purple_fluid_block` | block.colored_water.condense_purple_fluid_block | Condense Purple Water | colored_water |
| block | `colored_water:condense_purple_water_cauldron` | block.colored_water.condense_purple_water_cauldron | Condense Purple Water Cauldron | colored_water |
| block | `colored_water:condense_red_fluid_block` | block.colored_water.condense_red_fluid_block | Condense Red Water | colored_water |
| block | `colored_water:condense_red_water_cauldron` | block.colored_water.condense_red_water_cauldron | Condense Red Water Cauldron | colored_water |
| block | `colored_water:condense_white_fluid_block` | block.colored_water.condense_white_fluid_block | Condense White Water | colored_water |
| block | `colored_water:condense_white_water_cauldron` | block.colored_water.condense_white_water_cauldron | Condense White Water Cauldron | colored_water |
| block | `colored_water:condense_yellow_fluid_block` | block.colored_water.condense_yellow_fluid_block | Condense Yellow Water | colored_water |
| block | `colored_water:condense_yellow_water_cauldron` | block.colored_water.condense_yellow_water_cauldron | Condense Yellow Water Cauldron | colored_water |
| block | `colored_water:cyan_fluid_block` | block.colored_water.cyan_fluid_block | Cyan Water | colored_water |
| block | `colored_water:cyan_water_cauldron` | block.colored_water.cyan_water_cauldron | Cyan Water Cauldron | colored_water |
| block | `colored_water:gray_fluid_block` | block.colored_water.gray_fluid_block | Gray Water | colored_water |
| block | `colored_water:gray_water_cauldron` | block.colored_water.gray_water_cauldron | Gray Water Cauldron | colored_water |
| block | `colored_water:green_fluid_block` | block.colored_water.green_fluid_block | Green Water | colored_water |
| block | `colored_water:green_water_cauldron` | block.colored_water.green_water_cauldron | Green Water Cauldron | colored_water |
| block | `colored_water:light_blue_fluid_block` | block.colored_water.light_blue_fluid_block | Light Blue Water | colored_water |
| block | `colored_water:light_blue_water_cauldron` | block.colored_water.light_blue_water_cauldron | Light Blue Water Cauldron | colored_water |
| block | `colored_water:light_gray_fluid_block` | block.colored_water.light_gray_fluid_block | Light Gray Water | colored_water |
| block | `colored_water:light_gray_water_cauldron` | block.colored_water.light_gray_water_cauldron | Light Gray Water Cauldron | colored_water |
| block | `colored_water:lime_fluid_block` | block.colored_water.lime_fluid_block | Lime Water | colored_water |
| block | `colored_water:lime_water_cauldron` | block.colored_water.lime_water_cauldron | Lime Water Cauldron | colored_water |
| block | `colored_water:luminous_black_fluid_block` | block.colored_water.luminous_black_fluid_block | Luminous Black Water | colored_water |
| block | `colored_water:luminous_black_water_cauldron` | block.colored_water.luminous_black_water_cauldron | Luminous Black Water Cauldron | colored_water |
| block | `colored_water:luminous_blue_fluid_block` | block.colored_water.luminous_blue_fluid_block | Luminous Blue Water | colored_water |
| block | `colored_water:luminous_blue_water_cauldron` | block.colored_water.luminous_blue_water_cauldron | Luminous Blue Water Cauldron | colored_water |
| block | `colored_water:luminous_brown_fluid_block` | block.colored_water.luminous_brown_fluid_block | Luminous Brown Water | colored_water |
| block | `colored_water:luminous_brown_water_cauldron` | block.colored_water.luminous_brown_water_cauldron | Luminous Brown Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_black_fluid_block` | block.colored_water.luminous_condense_black_fluid_block | Luminous Condense Black Water | colored_water |
| block | `colored_water:luminous_condense_black_water_cauldron` | block.colored_water.luminous_condense_black_water_cauldron | Luminous Condense Black Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_blue_fluid_block` | block.colored_water.luminous_condense_blue_fluid_block | Luminous Condense Blue Water | colored_water |
| block | `colored_water:luminous_condense_blue_water_cauldron` | block.colored_water.luminous_condense_blue_water_cauldron | Luminous Condense Blue Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_brown_fluid_block` | block.colored_water.luminous_condense_brown_fluid_block | Luminous Condense Brown Water | colored_water |
| block | `colored_water:luminous_condense_brown_water_cauldron` | block.colored_water.luminous_condense_brown_water_cauldron | Luminous Condense Brown Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_cyan_fluid_block` | block.colored_water.luminous_condense_cyan_fluid_block | Luminous Condense Cyan Water | colored_water |
| block | `colored_water:luminous_condense_cyan_water_cauldron` | block.colored_water.luminous_condense_cyan_water_cauldron | Luminous Condense Cyan Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_gray_fluid_block` | block.colored_water.luminous_condense_gray_fluid_block | Luminous Condense Gray Water | colored_water |
| block | `colored_water:luminous_condense_gray_water_cauldron` | block.colored_water.luminous_condense_gray_water_cauldron | Luminous Condense Gray Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_green_fluid_block` | block.colored_water.luminous_condense_green_fluid_block | Luminous Condense Green Water | colored_water |
| block | `colored_water:luminous_condense_green_water_cauldron` | block.colored_water.luminous_condense_green_water_cauldron | Luminous Condense Green Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_light_blue_fluid_block` | block.colored_water.luminous_condense_light_blue_fluid_block | Luminous Condense Light Blue Water | colored_water |
| block | `colored_water:luminous_condense_light_blue_water_cauldron` | block.colored_water.luminous_condense_light_blue_water_cauldron | Luminous Condense Light Blue Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_light_gray_fluid_block` | block.colored_water.luminous_condense_light_gray_fluid_block | Luminous Condense Light Gray Water | colored_water |
| block | `colored_water:luminous_condense_light_gray_water_cauldron` | block.colored_water.luminous_condense_light_gray_water_cauldron | Luminous Condense Light Gray Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_lime_fluid_block` | block.colored_water.luminous_condense_lime_fluid_block | Luminous Condense Lime Water | colored_water |
| block | `colored_water:luminous_condense_lime_water_cauldron` | block.colored_water.luminous_condense_lime_water_cauldron | Luminous Condense Lime Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_magenta_fluid_block` | block.colored_water.luminous_condense_magenta_fluid_block | Luminous Condense Magenta Water | colored_water |
| block | `colored_water:luminous_condense_magenta_water_cauldron` | block.colored_water.luminous_condense_magenta_water_cauldron | Luminous Condense Magenta Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_orange_fluid_block` | block.colored_water.luminous_condense_orange_fluid_block | Luminous Condense Orange Water | colored_water |
| block | `colored_water:luminous_condense_orange_water_cauldron` | block.colored_water.luminous_condense_orange_water_cauldron | Luminous Condense Orange Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_pink_fluid_block` | block.colored_water.luminous_condense_pink_fluid_block | Luminous Condense Pink Water | colored_water |
| block | `colored_water:luminous_condense_pink_water_cauldron` | block.colored_water.luminous_condense_pink_water_cauldron | Luminous Condense Pink Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_purple_fluid_block` | block.colored_water.luminous_condense_purple_fluid_block | Luminous Condense Purple Water | colored_water |
| block | `colored_water:luminous_condense_purple_water_cauldron` | block.colored_water.luminous_condense_purple_water_cauldron | Luminous Condense Purple Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_red_fluid_block` | block.colored_water.luminous_condense_red_fluid_block | Luminous Condense Red Water | colored_water |
| block | `colored_water:luminous_condense_red_water_cauldron` | block.colored_water.luminous_condense_red_water_cauldron | Luminous Condense Red Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_white_fluid_block` | block.colored_water.luminous_condense_white_fluid_block | Luminous Condense White Water | colored_water |
| block | `colored_water:luminous_condense_white_water_cauldron` | block.colored_water.luminous_condense_white_water_cauldron | Luminous Condense White Water Cauldron | colored_water |
| block | `colored_water:luminous_condense_yellow_fluid_block` | block.colored_water.luminous_condense_yellow_fluid_block | Luminous Condense Yellow Water | colored_water |
| block | `colored_water:luminous_condense_yellow_water_cauldron` | block.colored_water.luminous_condense_yellow_water_cauldron | Luminous Condense Yellow Water Cauldron | colored_water |
| block | `colored_water:luminous_cyan_fluid_block` | block.colored_water.luminous_cyan_fluid_block | Luminous Cyan Water | colored_water |
| block | `colored_water:luminous_cyan_water_cauldron` | block.colored_water.luminous_cyan_water_cauldron | Luminous Cyan Water Cauldron | colored_water |
| block | `colored_water:luminous_gray_fluid_block` | block.colored_water.luminous_gray_fluid_block | Luminous Gray Water | colored_water |
| block | `colored_water:luminous_gray_water_cauldron` | block.colored_water.luminous_gray_water_cauldron | Luminous Gray Water Cauldron | colored_water |
| block | `colored_water:luminous_green_fluid_block` | block.colored_water.luminous_green_fluid_block | Luminous Green Water | colored_water |
| block | `colored_water:luminous_green_water_cauldron` | block.colored_water.luminous_green_water_cauldron | Luminous Green Water Cauldron | colored_water |
| block | `colored_water:luminous_light_blue_fluid_block` | block.colored_water.luminous_light_blue_fluid_block | Luminous Light Blue Water | colored_water |
| block | `colored_water:luminous_light_blue_water_cauldron` | block.colored_water.luminous_light_blue_water_cauldron | Luminous Light Blue Water Cauldron | colored_water |
| block | `colored_water:luminous_light_gray_fluid_block` | block.colored_water.luminous_light_gray_fluid_block | Luminous Light Gray Water | colored_water |
| block | `colored_water:luminous_light_gray_water_cauldron` | block.colored_water.luminous_light_gray_water_cauldron | Luminous Light Gray Water Cauldron | colored_water |
| block | `colored_water:luminous_lime_fluid_block` | block.colored_water.luminous_lime_fluid_block | Luminous Lime Water | colored_water |
| block | `colored_water:luminous_lime_water_cauldron` | block.colored_water.luminous_lime_water_cauldron | Luminous Lime Water Cauldron | colored_water |
| block | `colored_water:luminous_magenta_fluid_block` | block.colored_water.luminous_magenta_fluid_block | Luminous Magenta Water | colored_water |
| block | `colored_water:luminous_magenta_water_cauldron` | block.colored_water.luminous_magenta_water_cauldron | Luminous Magenta Water Cauldron | colored_water |
| block | `colored_water:luminous_orange_fluid_block` | block.colored_water.luminous_orange_fluid_block | Luminous Orange Water | colored_water |
| block | `colored_water:luminous_orange_water_cauldron` | block.colored_water.luminous_orange_water_cauldron | Luminous Orange Water Cauldron | colored_water |
| block | `colored_water:luminous_pink_fluid_block` | block.colored_water.luminous_pink_fluid_block | Luminous Pink Water | colored_water |
| block | `colored_water:luminous_pink_water_cauldron` | block.colored_water.luminous_pink_water_cauldron | Luminous Pink Water Cauldron | colored_water |
| block | `colored_water:luminous_purple_fluid_block` | block.colored_water.luminous_purple_fluid_block | Luminous Purple Water | colored_water |
| block | `colored_water:luminous_purple_water_cauldron` | block.colored_water.luminous_purple_water_cauldron | Luminous Purple Water Cauldron | colored_water |
| block | `colored_water:luminous_red_fluid_block` | block.colored_water.luminous_red_fluid_block | Luminous Red Water | colored_water |
| block | `colored_water:luminous_red_water_cauldron` | block.colored_water.luminous_red_water_cauldron | Luminous Red Water Cauldron | colored_water |
| block | `colored_water:luminous_white_fluid_block` | block.colored_water.luminous_white_fluid_block | Luminous White Water | colored_water |
| block | `colored_water:luminous_white_water_cauldron` | block.colored_water.luminous_white_water_cauldron | Luminous White Water Cauldron | colored_water |
| block | `colored_water:luminous_yellow_fluid_block` | block.colored_water.luminous_yellow_fluid_block | Luminous Yellow Water | colored_water |
| block | `colored_water:luminous_yellow_water_cauldron` | block.colored_water.luminous_yellow_water_cauldron | Luminous Yellow Water Cauldron | colored_water |
| block | `colored_water:magenta_fluid_block` | block.colored_water.magenta_fluid_block | Magenta Water | colored_water |
| block | `colored_water:magenta_water_cauldron` | block.colored_water.magenta_water_cauldron | Magenta Water Cauldron | colored_water |
| block | `colored_water:orange_fluid_block` | block.colored_water.orange_fluid_block | Orange Water | colored_water |
| block | `colored_water:orange_water_cauldron` | block.colored_water.orange_water_cauldron | Orange Water Cauldron | colored_water |
| block | `colored_water:pink_fluid_block` | block.colored_water.pink_fluid_block | Pink Water | colored_water |
| block | `colored_water:pink_water_cauldron` | block.colored_water.pink_water_cauldron | Pink Water Cauldron | colored_water |
| block | `colored_water:purple_fluid_block` | block.colored_water.purple_fluid_block | Purple Water | colored_water |
| block | `colored_water:purple_water_cauldron` | block.colored_water.purple_water_cauldron | Purple Water Cauldron | colored_water |
| block | `colored_water:red_fluid_block` | block.colored_water.red_fluid_block | Red Water | colored_water |
| block | `colored_water:red_water_cauldron` | block.colored_water.red_water_cauldron | Red Water Cauldron | colored_water |
| block | `colored_water:white_fluid_block` | block.colored_water.white_fluid_block | White Water | colored_water |
| block | `colored_water:white_water_cauldron` | block.colored_water.white_water_cauldron | White Water Cauldron | colored_water |
| block | `colored_water:yellow_fluid_block` | block.colored_water.yellow_fluid_block | Yellow Water | colored_water |
| block | `colored_water:yellow_water_cauldron` | block.colored_water.yellow_water_cauldron | Yellow Water Cauldron | colored_water |
| block | `create_central_kitchen:cherry_pie` | block.create_central_kitchen.cherry_pie | Placed Cherry Pie | create_central_kitchen |
| block | `create_central_kitchen:mulberry_pie` | block.create_central_kitchen.mulberry_pie | Placed Mulberry Pie | create_central_kitchen |
| block | `create_central_kitchen:pumpkin_pie` | block.create_central_kitchen.pumpkin_pie | Placed Pumpkin Pie | create_central_kitchen |
| block | `create_central_kitchen:truffle_pie` | block.create_central_kitchen.truffle_pie | Placed Truffle Pie | create_central_kitchen |
| block | `endercrop:ender_crop` | block.endercrop.ender_crop | Ender Crop | endercrop |
| block | `exposure:flash` | block.exposure.flash | Flash | exposure |
| block | `immersive_weathering:hanging_roots_wall` | block.immersive_weathering.hanging_roots_wall | Hanging Roots | immersive_weathering |
| block | `immersiveengineering:acetaldehyde_fluid_block` | block.immersiveengineering.acetaldehyde_fluid_block | Acetaldehyde | immersiveengineering |
| block | `immersiveengineering:biodiesel_fluid_block` | block.immersiveengineering.biodiesel_fluid_block | Biodiesel | immersiveengineering |
| block | `immersiveengineering:concrete_fluid_block` | block.immersiveengineering.concrete_fluid_block | Liquid Concrete | immersiveengineering |
| block | `immersiveengineering:creosote_fluid_block` | block.immersiveengineering.creosote_fluid_block | Creosote Oil | immersiveengineering |
| block | `immersiveengineering:ethanol_fluid_block` | block.immersiveengineering.ethanol_fluid_block | Ethanol | immersiveengineering |
| block | `immersiveengineering:fake_light` | block.immersiveengineering.fake_light | Fake Light | immersiveengineering |
| block | `immersiveengineering:herbicide_fluid_block` | block.immersiveengineering.herbicide_fluid_block | Herbicide | immersiveengineering |
| block | `immersiveengineering:phenolic_resin_fluid_block` | block.immersiveengineering.phenolic_resin_fluid_block | Phenolic Resin | immersiveengineering |
| block | `immersiveengineering:plantoil_fluid_block` | block.immersiveengineering.plantoil_fluid_block | Plant Oil | immersiveengineering |
| block | `immersiveengineering:post_transformer` | block.immersiveengineering.post_transformer | Post Transformer | immersiveengineering |
| block | `immersiveengineering:potted_hemp` | block.immersiveengineering.potted_hemp | Potted Hemp | immersiveengineering |
| block | `immersiveengineering:redstone_acid_fluid_block` | block.immersiveengineering.redstone_acid_fluid_block | Redstone Acid | immersiveengineering |
| block | `immersiveengineering:shader_banner` | block.immersiveengineering.shader_banner | Shader Banner | immersiveengineering |
| block | `immersiveengineering:shader_banner_wall` | block.immersiveengineering.shader_banner_wall | Shader Banner | immersiveengineering |
| block | `immersiveengineering:toolbox_block` | block.immersiveengineering.toolbox_block | Engineer's Toolbox | immersiveengineering |
| block | `immersivepetroleum:benzene_fluid_block` | block.immersivepetroleum.benzene_fluid_block | Benzene | immersivepetroleum |
| block | `immersivepetroleum:crudeoil_fluid_block` | block.immersivepetroleum.crudeoil_fluid_block | Crude Oil | immersivepetroleum |
| block | `immersivepetroleum:diesel_fluid_block` | block.immersivepetroleum.diesel_fluid_block | Diesel | immersivepetroleum |
| block | `immersivepetroleum:diesel_sulfur_fluid_block` | block.immersivepetroleum.diesel_sulfur_fluid_block | Sulfurized Diesel | immersivepetroleum |
| block | `immersivepetroleum:ethylene_fluid_block` | block.immersivepetroleum.ethylene_fluid_block | Ethylene | immersivepetroleum |
| block | `immersivepetroleum:gasoline_additives_fluid_block` | block.immersivepetroleum.gasoline_additives_fluid_block | Gasoline Additives | immersivepetroleum |
| block | `immersivepetroleum:gasoline_fluid_block` | block.immersivepetroleum.gasoline_fluid_block | Gasoline | immersivepetroleum |
| block | `immersivepetroleum:kerosene_fluid_block` | block.immersivepetroleum.kerosene_fluid_block | Kerosene | immersivepetroleum |
| block | `immersivepetroleum:lubricant_cracked_fluid_block` | block.immersivepetroleum.lubricant_cracked_fluid_block | Cracked Lubricant | immersivepetroleum |
| block | `immersivepetroleum:lubricant_fluid_block` | block.immersivepetroleum.lubricant_fluid_block | Lubricant | immersivepetroleum |
| block | `immersivepetroleum:napalm_fluid_block` | block.immersivepetroleum.napalm_fluid_block | Napalm | immersivepetroleum |
| block | `immersivepetroleum:naphtha_cracked_fluid_block` | block.immersivepetroleum.naphtha_cracked_fluid_block | Cracked Naphtha | immersivepetroleum |
| block | `immersivepetroleum:naphtha_fluid_block` | block.immersivepetroleum.naphtha_fluid_block | Naphtha | immersivepetroleum |
| block | `immersivepetroleum:propylene_fluid_block` | block.immersivepetroleum.propylene_fluid_block | Propylene | immersivepetroleum |
| block | `immersivepetroleum:well` | block.immersivepetroleum.well | Oil Well | immersivepetroleum |
| block | `immersivepetroleum:well_pipe` | block.immersivepetroleum.well_pipe | Oil Well Pipe | immersivepetroleum |
| block | `myrtrees:filled_rubberwood_log` | block.myrtrees.filled_rubberwood_log | Filled Rubberwood Log | myrtrees |
| block | `nock_enough_arrows:light_air` | block.nock_enough_arrows.light_air | Light Air | nock_enough_arrows |
| block | `pneumaticcraft:drone_redstone_emitter` | block.pneumaticcraft.drone_redstone_emitter | Drone Redstone Emitter | pneumaticcraft |
| block | `pneumaticcraft:ethanol` | block.pneumaticcraft.ethanol | Ethanol | pneumaticcraft |
| block | `pneumaticcraft:gasoline` | block.pneumaticcraft.gasoline | Gasoline | pneumaticcraft |
| block | `pneumaticcraft:kerosene_lamp_light` | block.pneumaticcraft.kerosene_lamp_light | Kerosene Lamp Light | pneumaticcraft |
| block | `pneumaticcraft:memory_essence` | block.pneumaticcraft.memory_essence | Liquid Experience | pneumaticcraft |
| block | `rare-ice:rare_ice` | block.rare-ice.rare_ice | Rare Ice | rare-ice |
| block | `securitycraft:fake_lava_block` | block.securitycraft.fake_lava_block | Lava | securitycraft |
| block | `securitycraft:fake_water_block` | block.securitycraft.fake_water_block | Water | securitycraft |
| block | `securitycraft:horizontal_reinforced_iron_bars` | block.securitycraft.horizontal_reinforced_iron_bars | Horizontal Reinforced Iron Bars | securitycraft |
| block | `securitycraft:sentry_disguise` | block.securitycraft.sentry_disguise | Sentry Disguise | securitycraft |
| block | `supplementaries:block_generator` | block.supplementaries.block_generator | Block Generator | supplementaries |
| block | `supplementaries:edelwood_stick` | block.supplementaries.edelwood_stick | Edelwood Stick | supplementaries |
| block | `supplementaries:gunpowder` | block.supplementaries.gunpowder | Gunpowder | supplementaries |
| block | `supplementaries:prismarine_rod` | block.supplementaries.prismarine_rod | Prismarine Rod | supplementaries |
| block | `supplementaries:propelplant_cane` | block.supplementaries.propelplant_cane | Propelplant Cane | supplementaries |
| block | `supplementaries:rope_tomatoes` | block.supplementaries.rope_tomatoes | Tomatoes on Rope | supplementaries |
| block | `supplementaries:sconce_wall_ender` | block.supplementaries.sconce_wall_ender | Ender Sconce | supplementaries |
| block | `supplementaries:sconce_wall_glow` | block.supplementaries.sconce_wall_glow | Glow Sconce | supplementaries |
| block | `supplementaries:sconce_wall_green` | block.supplementaries.sconce_wall_green | Cupric Sconce | supplementaries |
| block | `supplementaries:sconce_wall_nether_brass` | block.supplementaries.sconce_wall_nether_brass | Nether Brass Sconce | supplementaries |
| block | `supplementaries:sconce_wall_soul` | block.supplementaries.sconce_wall_soul | Soul Sconce | supplementaries |
| block | `supplementaries:skull_candle_soul` | block.supplementaries.skull_candle_soul | Soul Skull Candle | supplementaries |
| block | `supplementaries:skull_candle_soul_wall` | block.supplementaries.skull_candle_soul_wall | Soul Skull Candle | supplementaries |
| block | `supplementaries:skull_candle_wall` | block.supplementaries.skull_candle_wall | Skull Candle | supplementaries |
| block | `supplementaries:stick_tomatoes` | block.supplementaries.stick_tomatoes | Tomatoes on Stick | supplementaries |
| block | `upgrade_aquatic:acan_coral_wall_fan` | block.upgrade_aquatic.acan_coral_wall_fan | Acan Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:branch_coral_wall_fan` | block.upgrade_aquatic.branch_coral_wall_fan | Branch Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:chrome_coral_wall_fan` | block.upgrade_aquatic.chrome_coral_wall_fan | Chrome Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_acan_coral_wall_fan` | block.upgrade_aquatic.dead_acan_coral_wall_fan | Dead Acan Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_branch_coral_wall_fan` | block.upgrade_aquatic.dead_branch_coral_wall_fan | Dead Branch Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_chrome_coral_wall_fan` | block.upgrade_aquatic.dead_chrome_coral_wall_fan | Dead Chrome Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_finger_coral_wall_fan` | block.upgrade_aquatic.dead_finger_coral_wall_fan | Dead Finger Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_moss_coral_wall_fan` | block.upgrade_aquatic.dead_moss_coral_wall_fan | Dead Moss Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_petal_coral_wall_fan` | block.upgrade_aquatic.dead_petal_coral_wall_fan | Dead Petal Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_pillow_coral_wall_fan` | block.upgrade_aquatic.dead_pillow_coral_wall_fan | Dead Pillow Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_rock_coral_wall_fan` | block.upgrade_aquatic.dead_rock_coral_wall_fan | Dead Rock Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_silk_coral_wall_fan` | block.upgrade_aquatic.dead_silk_coral_wall_fan | Dead Silk Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:dead_star_coral_wall_fan` | block.upgrade_aquatic.dead_star_coral_wall_fan | Dead Star Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:elder_prismarine_coral_wall_fan` | block.upgrade_aquatic.elder_prismarine_coral_wall_fan | Elder Prismarine Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:finger_coral_wall_fan` | block.upgrade_aquatic.finger_coral_wall_fan | Finger Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:moss_coral_wall_fan` | block.upgrade_aquatic.moss_coral_wall_fan | Moss Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:petal_coral_wall_fan` | block.upgrade_aquatic.petal_coral_wall_fan | Petal Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:pillow_coral_wall_fan` | block.upgrade_aquatic.pillow_coral_wall_fan | Pillow Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:potted_river_sapling` | block.upgrade_aquatic.potted_river_sapling | Potted River Sapling | upgrade_aquatic |
| block | `upgrade_aquatic:prismarine_coral_wall_fan` | block.upgrade_aquatic.prismarine_coral_wall_fan | Prismarine Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:rock_coral_wall_fan` | block.upgrade_aquatic.rock_coral_wall_fan | Rock Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:silk_coral_wall_fan` | block.upgrade_aquatic.silk_coral_wall_fan | Silk Coral Fan | upgrade_aquatic |
| block | `upgrade_aquatic:star_coral_wall_fan` | block.upgrade_aquatic.star_coral_wall_fan | Star Coral Fan | upgrade_aquatic |
| block | `vinery:potted_apple_tree_sapling` | block.vinery.potted_apple_tree_sapling | Potted Apple Tree Sapling | vinery |
| block | `vinery:potted_cherry_tree_sapling` | block.vinery.potted_cherry_tree_sapling | Potted Cherry Tree Sapling | vinery |
| block | `vintageimprovements:centrifuge_structure` | block.vintageimprovements.centrifuge_structure | Centrifuge | vintageimprovements |
| block | `vintageimprovements:helve_kinetic` | block.vintageimprovements.helve_kinetic | Helve Hammer | vintageimprovements |
| block | `vintageimprovements:helve_structure` | block.vintageimprovements.helve_structure | Helve Hammer | vintageimprovements |
| block | `vintageimprovements:lathe_moving` | block.vintageimprovements.lathe_moving | Lathe | vintageimprovements |
| effect | `everythingcopper:copper_poisoning` | effect.everythingcopper.copper_poisoning | Copper Poisoning | everythingcopper |
| entity | `alexsmobs:emu_egg` | entity.alexsmobs.emu_egg | Emu Egg | alexsmobs |
| entity | `alexsmobs:ice_shard` | entity.alexsmobs.ice_shard | Ice Shard | alexsmobs |
| entity | `blocky_siege:projectile_ice_drop` | entity.blocky_siege.projectile_ice_drop | Ice Drop | blocky_siege |
| entity | `blocky_siege:projectile_keg_of_gunpowder` | entity.blocky_siege.projectile_keg_of_gunpowder | Keg of Gunpowder | blocky_siege |
| entity | `blocky_siege:projectile_lava_drop` | entity.blocky_siege.projectile_lava_drop | Lava Drop | blocky_siege |
| entity | `blocky_siege:projectile_wooden_shrapnel` | entity.blocky_siege.projectile_wooden_shrapnel | Wooden Shrapnel | blocky_siege |
| entity | `canned_goods:projectile_mini_fizz` | entity.canned_goods.projectile_mini_fizz | Mini Fizz | canned_goods |
| entity | `cccbridge:animatronic` | entity.cccbridge.animatronic | Animatronic | cccbridge |
| entity | `create_things_and_misc:projectile_blaze_balloon_fire` | entity.create_things_and_misc.projectile_blaze_balloon_fire | Blaze Balloon Canon | create_things_and_misc |
| entity | `create_things_and_misc:projectile_neon_tube` | entity.create_things_and_misc.projectile_neon_tube | Neon Tube | create_things_and_misc |
| entity | `create_things_and_misc:projectile_spout_gun_chocolate` | entity.create_things_and_misc.projectile_spout_gun_chocolate | Spout Gun | create_things_and_misc |
| entity | `create_things_and_misc:projectile_spout_gun_honey` | entity.create_things_and_misc.projectile_spout_gun_honey | Spout Gun | create_things_and_misc |
| entity | `create_things_and_misc:projectile_spout_gun_lava` | entity.create_things_and_misc.projectile_spout_gun_lava | Spout Gun | create_things_and_misc |
| entity | `create_things_and_misc:projectile_spout_gun_slime` | entity.create_things_and_misc.projectile_spout_gun_slime | Spout Gun | create_things_and_misc |
| entity | `create_things_and_misc:projectile_spout_gun_water` | entity.create_things_and_misc.projectile_spout_gun_water | Spout Gun | create_things_and_misc |
| entity | `creativeengineering:shrapnel` | entity.creativeengineering.shrapnel | Shrapnel | creativeengineering |
| entity | `decorative_blocks:dummy_entity` | entity.decorative_blocks.dummy_entity | Seat | decorative_blocks |
| entity | `domesticationinnovation:giant_bubble` | entity.domesticationinnovation.giant_bubble | Giant Bubble | domesticationinnovation |
| entity | `domesticationinnovation:highlighted_block` | entity.domesticationinnovation.highlighted_block | Highlighted Block | domesticationinnovation |
| entity | `engineersdecor:et_chair` | entity.engineersdecor.et_chair | Chair | engineersdecor |
| entity | `foundation:chair` | entity.foundation.chair | Chair | foundation |
| entity | `immersive_weathering:falling_icicle` | entity.immersive_weathering.falling_icicle | Falling Icicle | immersive_weathering |
| entity | `immersive_weathering:falling_layer` | entity.immersive_weathering.falling_layer | Falling Layer | immersive_weathering |
| entity | `immersive_weathering:falling_propagule` | entity.immersive_weathering.falling_propagule | Falling Propagule | immersive_weathering |
| entity | `immersiveengineering:explosive` | entity.immersiveengineering.explosive | Explosive | immersiveengineering |
| entity | `immersiveengineering:fluorescent_tube` | entity.immersiveengineering.fluorescent_tube | Fluorescent Tube | immersiveengineering |
| entity | `immersiveengineering:skyline_hook` | entity.immersiveengineering.skyline_hook | Skyline Hook | immersiveengineering |
| entity | `immersivepetroleum:molotov` | entity.immersivepetroleum.molotov | Molotov | immersivepetroleum |
| entity | `oldguns:bombard` | entity.oldguns.bombard | Bombard | oldguns |
| entity | `oldguns:rocket_projectile` | entity.oldguns.rocket_projectile | Rocket | oldguns |
| entity | `pneumaticcraft:ring` | entity.pneumaticcraft.ring | Ring | pneumaticcraft |
| entity | `pneumaticcraft:vortex` | entity.pneumaticcraft.vortex | Vortex | pneumaticcraft |
| entity | `prettypipes:pipe_frame` | entity.prettypipes.pipe_frame | Item Pipe Frame | prettypipes |
| entity | `quark:echorang` | entity.quark.echorang | Echorang | quark |
| entity | `quark:flamerang` | entity.quark.flamerang | Flamarang | quark |
| entity | `quark:parrot_egg` | entity.quark.parrot_egg | Parrot Egg | quark |
| entity | `quark:skull_pike` | entity.quark.skull_pike | Skull Pike | quark |
| entity | `quark:stool` | entity.quark.stool | Stool | quark |
| entity | `quark:torch_arrow` | entity.quark.torch_arrow | Torch Arrow | quark |
| entity | `redeco:seat` | entity.redeco.seat | Seat | redeco |
| entity | `securitycraft:bouncingbetty` | entity.securitycraft.bouncingbetty | Bouncing Betty | securitycraft |
| entity | `securitycraft:securitycamera` | entity.securitycraft.securitycamera | Security Camera | securitycraft |
| entity | `sit:entity_sit` | entity.sit.entity_sit | Seat | sit |
| entity | `sophisticatedbackpacks:everlasting_backpack_item` | entity.sophisticatedbackpacks.everlasting_backpack_item | Everlasting Backpack | sophisticatedbackpacks |
| entity | `supplementaries:bomb` | entity.supplementaries.bomb | Bomb | supplementaries |
| entity | `supplementaries:falling_sack` | entity.supplementaries.falling_sack | Falling Sack | supplementaries |
| entity | `supplementaries:pearl_marker` | entity.supplementaries.pearl_marker | Pearl Marker | supplementaries |
| entity | `twigs:pebble` | entity.twigs.pebble | Pebble | twigs |
| entity | `untitledduckmod:duck_egg` | entity.untitledduckmod.duck_egg | Duck Egg | untitledduckmod |
| entity | `untitledduckmod:goose_egg` | entity.untitledduckmod.goose_egg | Goose Egg | untitledduckmod |
| entity | `vinery:cherry_chest_boat` | entity.vinery.cherry_chest_boat | Cherry Boat with Chest | vinery |
| entity | `witchmobility:goldenwood_broom` | entity.witchmobility.goldenwood_broom | Goldenwood Broom | witchmobility |
| entity | `witchmobility:leafshade_broom` | entity.witchmobility.leafshade_broom | Leafshade Broom | witchmobility |
| entity | `witchmobility:snowair_broom` | entity.witchmobility.snowair_broom | Snowair Broom | witchmobility |
| entity | `witchmobility:wings_broom` | entity.witchmobility.wings_broom | Wings Broom | witchmobility |
| fluid | `decorative_blocks:flowing_thatch` | fluid_type.decorative_blocks.flowing_thatch | Flowing Thatch | decorative_blocks |
| fluid | `decorative_blocks:thatch` | fluid_type.decorative_blocks.thatch | Thatch | decorative_blocks |
| item | `alexsmobs:stink_ray_empty_inventory` | item.alexsmobs.stink_ray_empty_inventory | Stink Ray | alexsmobs |
| item | `decorative_blocks:blockstate_copy_item` | item.decorative_blocks.blockstate_copy_item | Blockstate Copy | decorative_blocks |
| item | `engineersdecor:test_block` | block.engineersdecor.test_block | Test Block | engineersdecor |
| item | `everycomp:all_woods` | item.everycomp.all_woods | All Woods | everycomp |
| item | `fulleng:requester_terminal` | item.merequester.requester_terminal | Requester Terminal | fulleng |
| item | `immersive_weathering:sandy_cobblestone_vertical_slab` | block.immersive_weathering.sandy_cobblestone_vertical_slab | Sandy Cobblestone Vertical Slab | immersive_weathering |
| item | `immersive_weathering:sandy_stone_brick_vertical_slab` | block.immersive_weathering.sandy_stone_brick_vertical_slab | Sandy Stone Brick Vertical Slab | immersive_weathering |
| item | `immersive_weathering:sandy_stone_vertical_slab` | block.immersive_weathering.sandy_stone_vertical_slab | Sandy Stone Vertical Slab | immersive_weathering |
| item | `immersive_weathering:snow_brick_vertical_slab` | block.immersive_weathering.snow_brick_vertical_slab | Snow Brick Vertical Slab | immersive_weathering |
| item | `immersive_weathering:snowy_cobblestone_vertical_slab` | block.immersive_weathering.snowy_cobblestone_vertical_slab | Snowy Cobblestone Vertical Slab | immersive_weathering |
| item | `immersive_weathering:snowy_stone_brick_vertical_slab` | block.immersive_weathering.snowy_stone_brick_vertical_slab | Snowy Stone Brick Vertical Slab | immersive_weathering |
| item | `immersive_weathering:snowy_stone_vertical_slab` | block.immersive_weathering.snowy_stone_vertical_slab | Snowy Stone Vertical Slab | immersive_weathering |
| item | `immersiveengineering:fake_icon_birthday` | item.immersiveengineering.fake_icon_birthday | Fake Icon (Birthday) | immersiveengineering |
| item | `immersiveengineering:fake_icon_bttf` | item.immersiveengineering.fake_icon_bttf | Fake Icon (BTTF) | immersiveengineering |
| item | `immersiveengineering:fake_icon_drillbreak` | item.immersiveengineering.fake_icon_drillbreak | Fake Icon (Drill Break) | immersiveengineering |
| item | `immersiveengineering:fake_icon_fried` | item.immersiveengineering.fake_icon_fried | Fake Icon (Fried) | immersiveengineering |
| item | `immersiveengineering:fake_icon_lucky` | item.immersiveengineering.fake_icon_lucky | Fake Icon (Lucky) | immersiveengineering |
| item | `immersiveengineering:fake_icon_ravenholm` | item.immersiveengineering.fake_icon_ravenholm | Fake Icon (Ravenholm) | immersiveengineering |
| item | `oldguns:bombard` | item.oldguns.bombard | Bombard | oldguns |
| item | `supplementaries:checker_vertical_slab` | block.supplementaries.checker_vertical_slab | Checker Vertical Slab | supplementaries |
| item | `witchmobility:goldenwood_broom_item` | item.witchmobility.goldenwood_broom_item | Goldenwood Broom | witchmobility |
| item | `witchmobility:leafshade_broom_item` | item.witchmobility.leafshade_broom_item | Leafshade Broom | witchmobility |
| item | `witchmobility:snowair_broom_item` | item.witchmobility.snowair_broom_item | Snowair Broom | witchmobility |
| item | `witchmobility:wings_broom_item` | item.witchmobility.wings_broom_item | Wings Broom | witchmobility |
| potion | `untitledduckmod:intimidation` | item.minecraft.tipped_arrow.effect.intimidation | Arrow of Intimidation | untitledduckmod |
| profession | `ae2:fluix_researcher` | entity.minecraft.villager.ae2:fluix_researcher | Fluix Researcher | ae2 |
| profession | `beautify:botanist` | entity.minecraft.villager.beautify:botanist | Botanist | beautify |
| profession | `chefsdelight:delightchef` | entity.minecraft.villager.chefsdelight:delightchef | Chef | chefsdelight |
| profession | `chefsdelight:delightcook` | entity.minecraft.villager.chefsdelight:delightcook | Cook | chefsdelight |
| profession | `cloudstorage:balloon_salesman` | entity.minecraft.villager.cloudstorage:balloon_salesman | Balloon Salesman | cloudstorage |
| profession | `domesticationinnovation:animal_tamer` | entity.minecraft.villager.domesticationinnovation:animal_tamer | Animal Tamer | domesticationinnovation |
| profession | `dynamicvillage:hydraulic_engineer` | entity.minecraft.villager.dynamicvillage:hydraulic_engineer | Hydraulic Engineer | dynamicvillage |
| profession | `dynamicvillage:mechanical_engineer` | entity.minecraft.villager.dynamicvillage:mechanical_engineer | Mechanical Engineer | dynamicvillage |
| profession | `dynamicvillage:miner` | entity.minecraft.villager.dynamicvillage:miner | Miner | dynamicvillage |
| profession | `dynamicvillage:train_mechanic` | entity.minecraft.villager.dynamicvillage:train_mechanic | Train Mechanic | dynamicvillage |
| profession | `lightmanscurrency:banker` | entity.minecraft.villager.lightmanscurrency:banker | Banker | lightmanscurrency |
| profession | `lightmanscurrency:cashier` | entity.minecraft.villager.lightmanscurrency:cashier | Cashier | lightmanscurrency |
| profession | `mca:adventurer` | entity.minecraft.villager.mca:adventurer | Adventurer | mca |
| profession | `mca:archer` | entity.minecraft.villager.mca:archer | Archer | mca |
| profession | `mca:cultist` | entity.minecraft.villager.mca:cultist | Cultist | mca |
| profession | `mca:guard` | entity.minecraft.villager.mca:guard | Guard | mca |
| profession | `mca:mercenary` | entity.minecraft.villager.mca:mercenary | Mercenary | mca |
| profession | `mca:outlaw` | entity.minecraft.villager.mca:outlaw | Outlaw | mca |
| profession | `vinery:winemaker` | entity.minecraft.villager.vinery:winemaker | Winemaker | vinery |
| profession | `wares:packager` | entity.minecraft.villager.wares:packager | Packager | wares |

</details>

### Named by rule (2)

| Kind | Id | Was | Now | Namespace |
|---|---|---|---|---|
| item | `pneumaticcraft:cod_n_chips` | Cod n Chips | Cod 'n' Chips | pneumaticcraft |
| item | `simplehats:jackohat` | Jack-o-Lantern Hat | Jack o'Lantern Hat | simplehats |

<details><summary>Capitalization fixed (106)</summary>


| Kind | Id | Was | Now | Namespace |
|---|---|---|---|---|
| entity | `cloudstorage:balloon_cargo` | Balloon-lifted Block | Balloon-Lifted Block | cloudstorage |
| item | `alexsmobs:shield_of_the_deep` | Shield Of The Deep | Shield of the Deep | alexsmobs |
| item | `architects_palette:nub_of_ender` | Nub Of Ender | Nub of Ender | architects_palette |
| item | `controlengineering:clock_edge` | Clock Generator: Rising edge | Clock Generator: Rising Edge | controlengineering |
| item | `controlengineering:clock_free` | Clock Generator: Free running | Clock Generator: Free Running | controlengineering |
| item | `controlengineering:clock_state` | Clock Generator: State triggered | Clock Generator: State Triggered | controlengineering |
| item | `controlengineering:lock` | Lock with key | Lock with Key | controlengineering |
| item | `controlengineering:scope_module_case` | Oscilloscope module casing | Oscilloscope Module Casing | controlengineering |
| item | `create:schematic_and_quill` | Schematic And Quill | Schematic and Quill | create |
| item | `create:wand_of_symmetry` | Wand Of Symmetry | Wand of Symmetry | create |
| item | `create_things_and_misc:card_reader_on` | Card Reader on | Card Reader On | create_things_and_misc |
| item | `createaddition:small_light_connector` | Small Connector With Light | Small Connector with Light | createaddition |
| item | `createdeco:andesite_bars_overlay` | Andesite Panel Bars  | Andesite Panel Bars | createdeco |
| item | `createdeco:brass_bars_overlay` | Brass Panel Bars  | Brass Panel Bars | createdeco |
| item | `createdeco:cast_iron_bars_overlay` | Cast Iron Panel Bars  | Cast Iron Panel Bars | createdeco |
| item | `createdeco:copper_bars_overlay` | Copper Panel Bars  | Copper Panel Bars | createdeco |
| item | `createdeco:gold_bars_overlay` | Gold Panel Bars  | Gold Panel Bars | createdeco |
| item | `createdeco:netherite_bars_overlay` | Netherite Panel Bars  | Netherite Panel Bars | createdeco |
| item | `createdeco:polished_iron_bars_overlay` | Polished Iron Panel Bars  | Polished Iron Panel Bars | createdeco |
| item | `createdeco:zinc_bars_overlay` | Zinc Panel Bars  | Zinc Panel Bars | createdeco |
| item | `davebuildingmod:arrow_left_sign` | Arrow Left  | Arrow Left | davebuildingmod |
| item | `davebuildingmod:arrow_up_sign` | Arrow Up  | Arrow Up | davebuildingmod |
| item | `davebuildingmod:flint_and_steel_passive_generator` | Flint And Steel Passive Generator | Flint and Steel Passive Generator | davebuildingmod |
| item | `endersdelight:stuffed_shulker_bowl` | Bowl Of Stuffed Shulker | Bowl of Stuffed Shulker | endersdelight |
| item | `engineersdecor:sign_decor` | Sign Plate (Engineer's decor) | Sign Plate (Engineer's Decor) | engineersdecor |
| item | `engineersdecor:thin_steel_pole_head` | Straight Thin Steel Pole head/foot | Straight Thin Steel Pole Head/Foot | engineersdecor |
| item | `floralench:large_double_lily_of_the_valley_pot` | Large Double Lily Of The Valley Pot | Large Double Lily of the Valley Pot | floralench |
| item | `floralench:large_lily_of_the_valley_pot` | Large Lily Of The Valley Pot | Large Lily of the Valley Pot | floralench |
| item | `floralench:lily_of_the_valley_basket` | Lily Of The Valley Basket | Lily of the Valley Basket | floralench |
| item | `floralench:lily_of_the_valley_bush` | Lily Of The Valley Bush | Lily of the Valley Bush | floralench |
| item | `floralench:lily_of_the_valley_mossy_cobblestone` | Lily Of The Valley Mossy Cobblestone | Lily of the Valley Mossy Cobblestone | floralench |
| item | `floralench:lily_of_the_valley_mossy_stone_brick` | Lily Of The Valley Mossy Stone Brick | Lily of the Valley Mossy Stone Brick | floralench |
| item | `floralench:lily_of_the_valley_vine` | Lily Of The Valley Vine | Lily of the Valley Vine | floralench |
| item | `glow_ink_plus:glow_black_carpet` | Black glow carpet | Black Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_black_wool` | Black glow wool | Black Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_blue_carpet` | Blue glow carpet | Blue Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_blue_wool` | Blue glow wool | Blue Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_brown_carpet` | Brown glow carpet | Brown Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_brown_wool` | Brown glow wool | Brown Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_cyan_carpet` | Cyan glow carpet | Cyan Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_cyan_wool` | Cyan glow wool | Cyan Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_gray_carpet` | Gray glow carpet | Gray Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_gray_wool` | Gray glow wool | Gray Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_green_carpet` | Green glow carpet | Green Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_green_wool` | Green glow wool | Green Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_light_blue_carpet` | Light blue glow carpet | Light Blue Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_light_blue_wool` | Light blue glow wool | Light Blue Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_light_gray_carpet` | Light gray glow carpet | Light Gray Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_light_gray_wool` | Light gray glow wool | Light Gray Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_lime_carpet` | Lime glow carpet | Lime Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_lime_wool` | Lime glow wool | Lime Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_magenta_carpet` | Magenta glow carpet | Magenta Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_magenta_wool` | Magenta glow wool | Magenta Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_orange_carpet` | Orange glow carpet | Orange Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_orange_wool` | Orange glow wool | Orange Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_pink_carpet` | Pink glow carpet | Pink Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_pink_wool` | Pink glow wool | Pink Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_purple_carpet` | Purple glow carpet | Purple Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_purple_wool` | Purple glow wool | Purple Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_red_carpet` | Red glow carpet | Red Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_red_wool` | Red glow wool | Red Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_white_carpet` | White glow carpet | White Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_white_wool` | White glow wool | White Glow Wool | glow_ink_plus |
| item | `glow_ink_plus:glow_yellow_carpet` | Yellow glow carpet | Yellow Glow Carpet | glow_ink_plus |
| item | `glow_ink_plus:glow_yellow_wool` | Yellow glow wool | Yellow Glow Wool | glow_ink_plus |
| item | `mca:book_blueprint` | Construction work | Construction Work | mca |
| item | `oldguns:high_grade_black_powder` | High-grade Black Powder | High-Grade Black Powder | oldguns |
| item | `oldguns:high_grade_black_powder_block` | Block of High-grade Black Powder | Block of High-Grade Black Powder | oldguns |
| item | `oldguns:high_grade_black_powder_cake` | High-grade Black Powder Cake | High-Grade Black Powder Cake | oldguns |
| item | `oldguns:low_grade_black_powder_block` | Block of Low-grade Black Powder | Block of Low-Grade Black Powder | oldguns |
| item | `oldguns:medium_grade_black_powder` | Medium-grade Black Powder | Medium-Grade Black Powder | oldguns |
| item | `oldguns:medium_grade_black_powder_block` | Block of Medium-grade Black Powder | Block of Medium-Grade Black Powder | oldguns |
| item | `oldguns:wet_high_grade_black_powder_block` | Block of Wet High-grade Black Powder | Block of Wet High-Grade Black Powder | oldguns |
| item | `oldguns:wet_high_grade_black_powder_cake` | Wet High-grade Black Powder Cake | Wet High-Grade Black Powder Cake | oldguns |
| item | `oldguns:wet_low_grade_black_powder_block` | Block of Wet Low-grade Black Powder | Block of Wet Low-Grade Black Powder | oldguns |
| item | `oldguns:wet_medium_grade_black_powder_block` | Block of Wet Medium-grade Black Powder | Block of Wet Medium-Grade Black Powder | oldguns |
| item | `pneumaticcraft:assembly_io_unit_export` | Assembly IO Unit (export) | Assembly IO Unit (Export) | pneumaticcraft |
| item | `pneumaticcraft:assembly_io_unit_import` | Assembly IO Unit (import) | Assembly IO Unit (Import) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_black` | Black Wall Lamp (inverted) | Black Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_blue` | Blue Wall Lamp (inverted) | Blue Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_brown` | Brown Wall Lamp (inverted) | Brown Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_cyan` | Cyan Wall Lamp (inverted) | Cyan Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_gray` | Gray Wall Lamp (inverted) | Gray Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_green` | Green Wall Lamp (inverted) | Green Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_light_blue` | Light Blue Wall Lamp (inverted) | Light Blue Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_light_gray` | Light Gray Wall Lamp (inverted) | Light Gray Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_lime` | Lime Wall Lamp (inverted) | Lime Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_magenta` | Magenta Wall Lamp (inverted) | Magenta Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_orange` | Orange Wall Lamp (inverted) | Orange Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_pink` | Pink Wall Lamp (inverted) | Pink Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_purple` | Purple Wall Lamp (inverted) | Purple Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_red` | Red Wall Lamp (inverted) | Red Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_white` | White Wall Lamp (inverted) | White Wall Lamp (Inverted) | pneumaticcraft |
| item | `pneumaticcraft:wall_lamp_inverted_yellow` | Yellow Wall Lamp (inverted) | Yellow Wall Lamp (Inverted) | pneumaticcraft |
| item | `securitycraft:keypad_barrel` | Password-protected Barrel | Password-Protected Barrel | securitycraft |
| item | `securitycraft:keypad_blast_furnace` | Password-protected Blast Furnace | Password-Protected Blast Furnace | securitycraft |
| item | `securitycraft:keypad_chest` | Password-protected Chest | Password-Protected Chest | securitycraft |
| item | `securitycraft:keypad_furnace` | Password-protected Furnace | Password-Protected Furnace | securitycraft |
| item | `securitycraft:keypad_smoker` | Password-protected Smoker | Password-Protected Smoker | securitycraft |
| item | `sophisticatedbackpacks:advanced_feeding_upgrade` | Advanced Feeding upgrade | Advanced Feeding Upgrade | sophisticatedbackpacks |
| item | `sophisticatedbackpacks:auto_blasting_upgrade` | Auto-blasting Upgrade | Auto-Blasting Upgrade | sophisticatedbackpacks |
| item | `sophisticatedbackpacks:auto_smelting_upgrade` | Auto-smelting Upgrade | Auto-Smelting Upgrade | sophisticatedbackpacks |
| item | `sophisticatedbackpacks:auto_smoking_upgrade` | Auto-smoking Upgrade | Auto-Smoking Upgrade | sophisticatedbackpacks |
| item | `vinery:glowing_wine` | Sun-kissed Wine | Sun-Kissed Wine | vinery |
| item | `yuushya:micro_pos_trans_item` | Entity Block Layer Fine-tuner | Entity Block Layer Fine-Tuner | yuushya_modelling |
| item | `yuushya:slot_trans_item` | Layer-switcher for Entity Block | Layer-Switcher for Entity Block | yuushya_modelling |

</details>
<!-- END GENERATED: name-audit -->

## 12. Mod display names

The mod name is what JEI prints in blue under every tooltip (and searches with `@`), what WTHIT shows under a
block, and what the Mods screen lists. It comes from each jar's `mods.toml` `displayName`, so a lang file can't
change it. patcherMod's `util/ModDisplayNames` rewrites it at common setup from CEConfig `[mod_names] overrides`
(`"modid=Name"` entries; the field is private and final in FML's loader layer, which mixins can't reach, so it is
written with `sun.misc.Unsafe`). The list is the config default in `CEConfig.ModNames`: that is the one place it lives.

**Pack style** (your rules, 2026-10-20), applied to all 430 loaded mods; 116 changed:

| Rule | Examples |
|---|---|
| Spaces between words | AppleSkin -> Apple Skin, CookingForBlockheads -> Cooking for Blockheads, DeathKnell -> Death Knell, ProjectRed Core -> Project Red Core, BoatView360 -> Boat View 360, Waystones2Waypoints2 -> Waystones 2 Waypoints 2, SeasonHud -> Season HUD |
| Possessive apostrophes | Chefs Delight -> Chef's Delight, Engineers Delight -> Engineer's Delight, Lightmans Discord Integration -> Lightman's Discord Integration, FarmersStructures -> Farmer's Structures, Abnormals Delight -> Abnormals' Delight |
| "&" for and / 'n | Brewin' And Chewin' -> Brewin' & Chewin', Towns and Towers -> Towns & Towers, Steam 'n' Rails -> Steam & Rails, Design n' Decor -> Design & Decor, things and misc -> Things & Misc |
| Create addons as "Create: <name>" | every mod that requires Create, plus the `create_*` ones: Create Deco -> Create: Deco, Guardian Beam Defense -> Create: Guardian Beam Defense, Extended Cogwheels -> Create: Extended Cogwheels, Little Contraptions -> Create: Little Contraptions, Bobber Detector -> Create: Bobber Detector, CC:C Bridge -> Create: CC Bridge, Create : The Salt -> Create: The Salt |
| No author handles | Create Compact Exp (by NoCube) -> Create: Compact Exp, Villagers Sell Animals (by NoCube) -> Villagers Sell Animals, SuperMartijn642's Config Library / Core Lib -> Config Library / Core Lib |
| Your own mods end in "(Alfonzo)", the only bracket they keep ("Backport" moves out of brackets) | Creative Engineering (Alfonzo), Brewin' & Chewin' Backport (Alfonzo), Let's Do Vinery Backport (Alfonzo), Foundation Vinery Backport (Alfonzo), Exposure Backport & Extras (Alfonzo), Exposure: Expanded / Polaroid / Catalog Backport (Alfonzo), Exposure Extras, Incubation Additions, More Mooblooms, Baby Mobs (Alfonzo) |
| Renamed | MmmMmmMmmmmm -> Target Dummy |
| Title Case, readable ids | Fish On The Line -> Fish on the Line, Everything is Copper -> Everything Is Copper, (backport) -> (Backport), fortuneonneterite i ig z -> Fortune on Netherite, corpsecurioscompat -> Corpse Curios Compat, restApi -> REST API, wthit -> WTHIT |

**Kept as written:** Pretty Pipes and Pretty Pipes: Fluids (your call); names ending in JS (KubeJS, MoreJS, LootJS,
PonderJS: "JS" is part of the word) and the -Craft brands (PneumaticCraft, SecurityCraft), like Minecraft itself;
JRFTL, TRender, Re:Deco, CC: Tweaked, Geode+;
KubeJS Create / Vintage KubeJS / PonderJS need Create but are KubeJS addons, so they keep the KubeJS form.

**Applies:** JEI and WTHIT read the name once, so a change shows after a restart. The mod id never changes: JEI
`@create_dd`-style searches by id still work, and so does `@` with the new name.

- [ ] **NM-13 Mod names in game:** hover any Create Deco item in JEI -> blue mod line "Create: Deco"; a Pretty Pipes
  pipe -> "Pretty Pipes"; WTHIT on a Farmer's Delight stove -> "Farmer's Delight"; Mods screen lists "Death Knell",
  "Target Dummy" and "Creative Engineering (Alfonzo)".

