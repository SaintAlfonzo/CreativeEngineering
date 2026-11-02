# Display Delight 1.7.0 port (Backported Display)

Test prefix **DDP-** · Last updated **2026-10-25** · Status **BUILT 2026-10-25, installed in `mods/`, Shipped in 1.1.03 (deployed to server and players 2026-10-26);
boot check SQ-DDP-01 PASSED 2026-10-25, SQ-DDP-02 to 05 and CQ-DDP queued; not run in game**

The pack's 1.19.2 port of Display Delight 1.7.0 (`development/backportedDisplay`, jar
`backportedDisplay-1.7.0-ce-19.2-01.jar`, mod id `displaydelight`, display name "Display Delight (Backport)") replaces
`displaydelight-1.2.0.jar` (moved to `development/backportedDisplay/jar_archive/displaced/`). Evaluated 2026-10-25 against
`devmods/checkNewerVersionForContent/displaydelight-1.7.0.jar` (1.20.1 / Forge), method in
[devmods_evaluation.md](devmods_evaluation.md) section 1. Plan row: `largecontent/mod-additions #22` in
[todo/TODO.md](todo/TODO.md). How the pack uses the mod: [placeable_food.md](placeable_food.md),
[placeable_catalog.md](placeable_catalog.md).

**Licence:** LGPLv3 for the mod code and models (Copyright (c) 2024 jkvin114), with a third-party MIT notice (owner-supplied
2026-10-25). 5 models are derived from the Farmers 3D resource pack by Pder (MIT, Copyright (c) 2024 Pder; the notice must stay
with copies). The port is GPLv3 (LGPLv3 section 2 / GPLv3 section 7 allow it); both upstream notices and the LGPLv3 text
are in `development/backportedDisplay/NOTICE-upstream.txt`, shipped in the jar.

**Result in one line:** 210 food display blocks and the 2 plates (1.2.0: 191 and 2). 101 are new, 93 of them for foods the
pack has today (95 with Creative Delight's Farmer's Delight 1.3.4 foods); every 1.2.0 block for a mod in the pack keeps its id, so placed blocks stay.

## 1. How it was read

- `python3 tools/jar_diff.py` (1011 files added, 346 removed, 501 changed; en_us lang **+402 / -0 / ~0**).
- Classes decompiled with Vineflower (`~/.cache/ce-tools/vineflower-1.10.1.jar`), then remapped from SRG to Mojmap with the
  1.19.2 `srg_to_official` table (only 19 names had no 1.19.2 counterpart, all 1.20 API).
- Each block name was mapped to its source item the way 1.7.0 does it (`tools/display_delight_jar.py`) and checked
  against the pack registry (`kubejs/exported/items_by_mod/`) and the port's tags.
- 1.2.0's loot-table links were compared with 1.7.0's name rule for every 1.2.0 block: they agree except 3 blocks for
  mods not in the pack (`fd_festive_chicken`, `pd_small_plated_pineapple_pie_slice`, the unregistered `bowl`).

## 2. Changes from 1.2.0 and the decisions taken

### 2.1 How linking works

1.2.0 links a display block to the food its loot table drops, if the food has the matching item tag. 1.7.0 links by block
name: a prefix names the source mod (`ad_` Alex's, `cd_` Corn, `df_` Delightful, `abd_` Abnormals, `bnc_` Brewin', `edd_`
End's, `erd_` Ender's, `fr_` Respite, `md_` Miner's, `nd_` Nether's, `od_` Ocean's, and eight more, section 4), then
`plated_` / `small_plated_`, then the food's id; no prefix means Farmer's Delight (or vanilla for 5 foods). The tag check
stays. Links are made on every server level load. Food blocks have no loot tables any more: the drop is computed from the
link.

Port: `md_` maps to the pack's `miners_delight` (1.7.0 says `minersdelight`), in `BlockAssociations` and in the tags.

### 2.2 New display blocks for foods the pack has

| Source mod (pack jar) | New blocks | Link to a pack food today | Examples |
|---|---|---|---|
| Abnormals Delight 4.1.2 | 15 | 15 | Duck Noodles, Perch with Mushrooms, Slabdish, 3 cookies, Escargot, 6 cake slices |
| End's Delight 2.1 | 24 | 24 | Dragon Meat Stew, Roasted Dragon Steak, Bubble Tea, Grilled Shulker, Steamed Dragon Egg |
| Ender's Delight 1.2.2 | 17 | 12 | Chorus Stew, Ender Paella, Pearl Pasta, Twisted Cereal; 5 are 1.20-only foods |
| Farmer's Respite 2.0 | 14 | 14 | the 8 teas, Coffee, Blazing Chili, Tea Curry, Black Cod, Coffee Cake and Rose Hip Pie slices |
| Miner's Delight (port) | 12 | 12 | Cave Soup, Takoyaki, Glow Ink Pasta, Vegan Hamburger, Squid Sandwich, Fake Meatloaf |
| Brewin' and Chewin' (port) | 8 | 8 | Cheesy Pasta, Creamy Onion Soup, Fiery Fondue, Horror Lasagna, Scarlet Pierogi |
| Nether's Delight 3.1 | 7 | 7 | Grilled Strider, Strider Moss Stew, 3 stuffed hoglin plates |
| Farmer's Delight | 4 | 1 (+2) | Nether Salad; Gleaming Salad and Slice of Pumpkin Pie link once Creative Delight's FD 1.3.4 items are in the dump (they are in the release); Onion Soup has no item |
| **Total** | **101** | **93 (95 with Creative Delight)** | |

(The evaluation estimated 112 / 107; these are the registered blocks after the port's exclusions.) Left out on purpose: 7 Abnormals blocks
whose food the pack removes (Cherry Cream Soda, Passion Aloe Nectar, Dune Platter, Passion Fruit Glazed Duck, Venison with
Bamboo Shoots, Seared Venison, Yucca Gateau slice; `removed_items.js`).

Kept although nothing links to them today: 5 Ender's and 8 Delightful blocks for 1.20-only foods (Delightful 3.8.1,
`mod-additions #23`), Onion Soup. They link by themselves if the food ever arrives.

### 2.3 Interaction and behaviour changes (all taken with the port)

| Change | As built |
|---|---|
| Right click a plated food with an axe hides the plate (drops it); right click with a plate puts it back | ported; "axe" is any item that can do Forge's `axe_dig` (1.7.0: the 1.20 `minecraft:axes` tag) |
| Sneak + empty hand takes the whole stack; taking from a plateless display leaves air | ported |
| Silk touch on a display block drops the display block's own item | ported, upstream default on (`SilkTouchDropFoodBlock`); the items have names (generated lang) |
| Wandering trader sells display items whose source mod is missing | **not ported**: those blocks are not in the port (section 4) |
| Creative tab "Display Delight" with every display item; `DisableCreativeTabItems` empties it | ported (1.2.0 had no tab for food items; JEI lists them either way) |
| Flame particles on spicy foods (`block/fiery`), steam on hot drinks (`HotDrinkBlock`) | ported (client); in the port: Blazing Chili (the other 7 fiery blocks are My Nether's Delight) and 15 hot drinks |
| A display block placed on something that is not a full top face (fence, slab top edge, another display block, open trapdoor) stays and grows a wooden leg (`support` state, `wood_support` model); 1.2.0 needed a sturdy top face | ported; `#displaydelight:support_exceptions` also holds `#creativeengineering:counter_supports`, so on a Kitchen Counter the plate sits on the worktop with no leg |
| Pistons destroy display blocks (1.20 `PushReaction.DESTROY`) | ported as a block override (1.19.2 has no property for it) |
| 1.2.0's client mixin adding "Placeable" lines to food tooltips | gone upstream; the pack's own placeable lines ([tooltips.md](tooltips.md)) are unaffected |
| `Tooltips` and `WanderingTraderSellFoodBlock` config keys | dropped (unused); Forge rewrites `config/displaydelight-common.toml` on first load |

Pack interactions checked:

- patcherMod `compat/DisplayDelightDishes` (9 egg dishes) calls `FoodBlock(Properties)` and
  `BlockAssociations.addToMap(Item, Block, boolean)`: both kept with the same signatures. The dishes ship loot tables;
  the port drops only the loot table's items when a block has one, so they do not drop twice.
- Those dishes' models use three 1.2.0 textures 1.7.0 dropped (`displaydelight:block/bowl`,
  `minecraft:farmers3d/tray_top`, `tray_bottom`); the port ships them again (`tools/legacy_1.2.0/`).
- `CounterToppers` pops toppers that cannot survive; 1.7.0's display blocks always survive (leg), so on a counter
  whose block below breaks nothing changes, and elsewhere a display block now stays with a leg where 1.2.0's popped
  (`unreleased.md` bullet updated).

### 2.4 Removed and unused

- Three 1.2.0 block ids are gone in 1.7.0 (`ctd_small_plated_rice_ball`, `ed_plated_peanut_butter_honey_sandwich`,
  `pd_small_plated_pineapple_pie_slice`): all for mods not in the pack.
- `glass_showcase` and `wood_showcase` ship a blockstate and model but are not registered: not copied.
- 1.2.0 had 78 blocks for mods not in the pack (Cultural 29, Expanded 29, Large Meals 13, Pineapple 4, Festive 3), only
  reachable in creative; the port leaves them out with the rest of section 4. One placed in a creative world becomes air.

## 3. As built (2026-10-25)

| Part | Where | Notes |
|---|---|---|
| Gradle project | `development/backportedDisplay` (root `settings.gradle`) | `ce.forge-mod`; `replaces_jars=displaydelight-*.jar`; no compile dependency (DD has no FD code link) |
| Java | `src/main/java/com/jkvin114/displaydelight/` | 1.7.0's classes, upstream package kept (patcherMod links against it). 1.19.2 API: `Material.WOOD` properties, piston override, `CreativeModeTab` subclass and `itemGroup.displaydelight` lang key, `LootContext.Builder` drops, `ModelEvent.BakingCompleted` for the plate-hiding model wrapper, `ForgeRegistries`, `ToolActions.AXE_DIG`. One block register (1.7.0 had three). Config built after its keys (the decompiled 1.7.0 builds the spec first). Dropped: trades, `DisplayVillagerEvents`, Expanded Delight compat class, `RegisterHelper`, "HELLO" log lines |
| Block table | `tools/display_blocks.json` (380 rows, 1.7.0's item order) -> generated `init/DisplayFoods.java` | Type per block: food, wide, drink, hot drink, fiery x4, plated, stackable (with max stacks), small plated |
| Resources | `python3 development/backportedDisplay/tools/port_resources.py` rewrites `src/main/resources/{assets,data}` and `DisplayFoods.java` | Upstream jar sha1-pinned in `gradle.properties`. Only the blockstates, item models, block models (followed through parents) and textures the kept blocks use: 212 blockstates, 302 block models, 203 textures. 19 model faces on the undefined `#missing` texture take the element's top texture. Tags: `minersdelight:` -> `miners_delight:`, entries of the section 4 mods dropped. Recipes / loot lose 1.20 fields. Lang keys of left-out blocks dropped |
| Pack names | `tools/gen_display_delight_names.py` | Names every food block after its food in game ("Placed Beef Stew", "Plated Slice of Sweet Berry Pie"; 1.7.0's own lang says "Beef Stew Block"): 210 names, 14 from the block id (foods not in the pack) |
| Pack models | `tools/gen_display_delight_models.py` | The port has an item model for every item and fixes the faces itself, so this now writes none and deleted the 92 overrides it wrote for 1.2.0 (`kubejs/assets/displaydelight/models/`) |
| Shared tooling | `tools/display_delight_jar.py` (new), `placeable_catalog.py`, `gen_dd_dishes.py`, `build_deploy.py`, `lang_index.py` | The DD jar is found as `backportedDisplay-*.jar` or `displaydelight-*.jar`; links by name for the port, by loot table for 1.2.0. `lang_index` now also reads jars nested in `mods/` jars (Creative Delight's members) and the `*_upstream/` jars the ports load, which the names needed (Farmer's Delight and Miner's names were missing) |

Rebuild: `python3 development/backportedDisplay/tools/port_resources.py`, then `./gradlew :backportedDisplay:build --offline`
and `:backportedDisplay:installToMods` (game closed), then `python3 tools/gen_display_delight_names.py`.

Not done: `gen_dd_dishes.py --check` stops on `ends_delight:shulker_omelette` (End's Delight is now nested in the Creative
Delight jar and `item_sprite` reads only top-level jars); the committed dish models are unchanged and render with the
legacy textures.

## 4. Mods 1.7.0 supports that the pack does not have

Display blocks for these are in 1.7.0 and left out of the port (`EXCLUDED_PREFIXES` in `port_resources.py`; remove a
prefix and rerun the tool if the mod is ever added). Whether each has a Forge 1.19.2 build is not checked here.

| Prefix | Mod (mod id) | Blocks in 1.7.0 | Placed / plated / small plated | In 1.2.0 | Example foods |
|---|---|---|---|---|---|
| `mnd_` | My Nether's Delight (`mynethersdelight`) | 37 | 26 / 10 / 1 | no | Sizzling Pudding, Rock Soup, Spicy Hoglin Stew, Hot Wings, Ghasta, Magma Cake slice |
| `ed_` | Expanded Delight (`expandeddelight`) | 40 | 20 / 14 / 6 | 29 | Asparagus Soup, Mac and Cheese, Peanut Butter Sandwich, Sweet Rolls, juices |
| `ctd_` | Cultural Delights (`culturaldelights`) | 31 | 6 / 18 / 7 | 29 | Eggplant Parmesan, Chicken Taco, Avocado Toast, Midori Roll, Rice Ball |
| `acd_` | Aquaculture Delight (`aquaculturedelight`) | 18 | 15 / 1 / 2 | no | Fish and Chips, Halaszle, Bass Stew, Catfish Barbecue, Fish Fillet Roll |
| `crd_` | Crabber's Delight (`crabbersdelight`) | 15 | 10 / 5 / 0 | no | Clam Chowder, Seafood Gumbo, Coconut Milk, Shrimp Skewer, Stuffed Nautilus Shell |
| `lm_` | Large Meals (`largemeals`) | 13 | 13 / 0 / 0 | 13 | Omurice, Chicken Curry, Rice Pudding, Roasted Mutton Rack (ported 2026-11-01 with Large Meals: [large_meals.md](large_meals.md) step 19; jar not installed) |
| `pd_` | Pineapple Delight (`pineapple_delight`) | 6 | 4 / 0 / 2 | 4 | Pineapple Fried Rice, Pineapple Juice, Pineapple Cake slice |
| `fd_` | Festive Delight (`festive_delight`) | 3 | 3 / 0 / 0 | 3 | Festive Chicken, Christmas Tea, Salmon Verrines |
| **Total** | 8 mods | **163** | | **78** | |

Foods of mods the pack does have but whose 1.20 versions added them (blocks kept, nothing links yet): Ender's Delight
Amberveil Stew, Amberveiled Curry, Chicken Curry, Steak Fries, Veil of Flames Risotto; Delightful Matcha and Salmonberry
Milkshakes, Cactus Chili, Cactus Soup, Stuffed Cantaloupe, Cantaloupe Popsicle, Cantaloupe Bread, Wrapped Cantaloupe;
Farmer's Delight Onion Soup.

## 5. Open for the owner

1. Silk-touch display items: upstream default (on) kept. Turn `SilkTouchDropFoodBlock` off in the pack config if the
   display items should stay creative-only.
2. Creative tab: kept (JEI lists the items with or without it).

## 6. Tests

Scratch-server halves are queued as SQ-DDP, the client halves as CQ-DDP in
[testing/scratch_queue.md](testing/scratch_queue.md).

- [ ] **DDP-01 Load:** start the game. -> No `displaydelight` error in `logs/latest.log`; the mod list shows "Display Delight
  (Backport)"; a "Display Delight" creative tab with plates first.
- [ ] **DDP-02 Old world:** open a world with 1.2.0 display blocks placed (plate, plated hamburger, beef stew, cake
  slice). -> All still there, same models; breaking each gives the food and the plate.
- [ ] **DDP-03 New foods, one per mod:** sneak + right click on the floor: Duck Noodles, Dragon Meat Stew, Chorus Stew,
  Green Tea, Cave Soup, Cheesy Pasta, Grilled Strider. On a Food Plate: Vegan Hamburger; on a Small Food Plate: Slice of
  Coffee Cake. -> Each places its model and gives the food back on right click with an empty hand.
- [ ] **DDP-04 Axe and plate:** right click a plated hamburger with an axe. -> The plate drops, the food sits lower with
  no plate. Right click it with a Food Plate. -> The plate is back, one plate used.
- [ ] **DDP-05 Take all:** stack 3 cookies on a plate, sneak + right click with an empty hand. -> All 3 in the
  inventory, the plate stays.
- [ ] **DDP-06 Silk touch:** break a placed Beef Stew with a silk-touch pickaxe. -> Drops the "Placed Beef Stew" item.
- [ ] **DDP-07 Particles:** place Blazing Chili and Hot Cocoa. -> Flames above the chili, steam above the cocoa.
- [ ] **DDP-08 Legs:** place a stew on a fence and on a slab. -> Wooden leg under it. On a Kitchen Counter. -> No leg,
  sits on the worktop.
- [ ] **DDP-09 Pack dishes:** place Shakshouka and Omelette (patcherMod dishes), break each. -> Bowl and tray textures
  (no purple / black), one food each.
- [ ] **DDP-10 Pistons:** push a plated food with a piston. -> It breaks and drops.
- [ ] **DDP-11 Names and icons:** JEI and WTHIT on 5 new blocks. -> "Placed ..." / "Plated ..." names, 3D icons (no
  purple cube).
- [ ] **DDP-12 Catalog:** `/dumpplaceable`, then `python3 tools/placeable_catalog.py`. -> About 95 more Display Delight
  links than the 2026-10-24 dump; nothing unexpected under "Display Delight gaps".

## 7. History

- **2026-10-25:** evaluated 1.7.0 against 1.2.0 (sections 1, 2), then built the port (section 3), installed in `mods/`.
