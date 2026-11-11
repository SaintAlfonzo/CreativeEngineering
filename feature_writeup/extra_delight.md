# Extra Delight: overlap and integration report

Survey for TODO `largecontent/mod-additions #11`. Written 2026-10-30, **nothing built, nothing installed**. Jar:
`devmods/toBackport/later_large_content_overlap/extradelight-2.6.6.jar`, unpacked to `DecompiledMods/extradelight-2.6.6/`,
decompiled to `DecompiledMods/extradelight-2.6.6-src/` (349 files). Licence **CC BY-NC-SA 4.0** (not GPL-compatible,
section 0 and D2). The jar is NeoForge 1.21.1 (the TODO row said Forge 1.20.1), so using 2.6.6 needs a **port**
(skill `port`). Upstream also has a **native Forge 1.19.2 line, last release 1.3.3 (2024-07-10)**, unpacked to
`DecompiledMods/extradelight-1.3.3/` and compared in section 2.7. Short name in this file: **ED**. Test prefix: `EXD-`.

**Second pass 2026-11-08, still nothing built.** Section 2.7 compares 1.3.3 with 2.6.6 and reads the licence against the
plan. Result: fork upstream's 1.19.2 source and backport only the missing lines from 2.6.6 (D1), and keep everything taken
from ED out of the GPLv3 trees (D2). Steps 0, 1 and 11, the routing note and the file map are corrected in place.

Stage 2 of the large content addition: ED is merged with **Hearth and Harvest** ([hearth_and_harvest.md](hearth_and_harvest.md),
short name H&H). Section 2.5 compares the two item by item and settles H&H's 2.6 placeholders. H&H decisions that waited for this pass are
answered in 5.1.

**Refreshed 2026-11-08, still nothing built.** ED is applied **on top of** the existing Delight mods and the installed H&H
port (owner, N22 in the H&H report): where ED and an installed mod share an item, the installed one stays and ED points at
it. Section 2.6 holds the re-scan; rows it changed are corrected in place, and the 2.6 tables say what moved.

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks by name; upstream bugs are fixed.

## Owner rulings 2026-11-09 (override the recommendations below where they differ)

Still nothing built. These five rulings change D3, D6, D8, D11 and D12 and add 3.3 / 3.4 / 3.7 / 3.8; the old recommendation
text in those rows is kept for history and marked "(rev 11-09)" where it no longer holds.

| # | Ruling | Where |
|---|---|---|
| R1 | **Every ED workstation is registered, for evaluation.** Where a recipe also moves onto a pack station (vat recipes onto the Vintage fermenting jar, juicer onto the Apple Press, and so on), it **stays on the ED station as well**: both stations craft it. Dropping a station later is a data removal, no old ids exist (0 ids live) | 3.3, D3, D6, D12 |
| R2 | **Ground Coffee is kept** (item, plus the recipes that grind and use it). Coffee beans stay Respite's (D8) | D8 |
| R3 | **All furniture and decor is registered for evaluation**, including the pieces D11 dropped (sinks, counters, spice racks, food displays, step stools, keg, wallpapers, cinnamon and fruit half cabinets, ED's 9 shared half cabinets next to H&H's). Only pieces that need a missing 1.20 block stay out (3.4) | 3.4, D11 |
| R4 | **The Cornfield dimension, its maze, its 23 structures and the Halloween portal are defined** in 3.7. D4 itself (keep or drop) is not changed by this: it still reads "drop" until you rule | 3.7 |
| R5 | **Texture comparison plan:** one image per group, ED texture beside the pack texture it loses to, kept in the repo for review | 3.8, [extra_delight_textures.md](extra_delight_textures.md) |

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 1490 named ids (668 item, 822 block lang keys; 1441 item models), 35 fluids, 3 effects, 0 potions, 0 entities, 1 dimension + biome + 23 structures, 7 fruit / spice trees, 2 woods, 10 crops + 11 wild crops, about 2470 recipe files, 21 chest GLMs, 2160 advancements | 1 | read from the jar |
| Of the 1490: about 695 foods and ingredients, 233 furniture / display, 208 wallpapers, 99 candy and chocolate decor, 73 fluid blocks and buckets, 58 storage blocks, 51 stations and tools, 49 wood / tree, 24 crops | 1 | regex classification, see Facts |
| Overlap: same item in an installed mod | 2.1, 2.6 | about **120** ids listed by name (food, crops, crates, stations), plus about 25 found by the 2026-11-08 re-scan |
| Overlap: same system (oven and bakeware, mortar, salt and evaporator, keg, jar, pickling, coffee, chocolate, ice cream, citrus trees, corn) | 2.3 | 18 systems |
| Overlap with Hearth and Harvest | 2.5, 2.6 | 41 shared or competing rows; H&H is installed now and wins every shared item (6 survivors reversed) |
| Dead in the pack: recipes whose slot only Butchercraft-style tags fill (sausage, offal, cuts) | 3.6 B5, 2.6 | 35 recipe files by the 2026-11-08 scan; 6 are the sausage dishes, craftable once H&H's sausages are tagged |
| New to the pack | 3 | about **520** ids after the re-scan (was about 560; most of the food, the decor, 4 crops, 4 trees) |
| Upstream issues | 3.6 | B1-B8 (never loaded in the pack, no log) |
| Fold-in | 4 | 26 steps |
| 1.3.3 and licence pass | 2.7 | 1.3.3 has 807 named ids (755 shared with 2.6.6) and no fluids, effects or chiller; route and licence boundary changed |
| Decisions | 5 | D1-D20; H&H D4, D7, D12 answered in 5.1 |
| Tests | 8 | EXD-01..EXD-34 drafted |

- **Jar:** `extradelight` 2.6.6 (its `neoforge.mods.toml` says 2.6.5, B1), authors Lance5057 and nine others, "A new more
  involved way to craft". Patchouli cookbook in `data/extradelight/patchouli_books/`.
- **Licence:** CC BY-NC-SA 4.0 on both lines (2.7.2). The 1.3.3 `mods.toml` says MIT; treat that as a stale field, not a
  grant. Fork allowed, but the fork must stay CC BY-NC-SA with attribution, can't become GPLv3, and must stay
  non-commercial. CurseForge page FAQ: don't rehost the jar, don't claim the mod as your own, no pay-to-play or cash shop
  modpacks or servers. Port route: its own in-tree jar under that licence, **not** inside the GPLv3 Creative Delight
  bundle (D2).
- **Dependencies:** NeoForge 21+, Minecraft 1.21, Farmer's Delight 1.2.8+ (required). Optional by `mod_loaded`: Create
  (209 mixing recipes), Butchercraft (1). Tag-only compat: Brewin' and Chewin', Corn Delight, Cultural Delights, Pizza
  Delight, The Salt, Serene Seasons, Tough As Nails.
- **Pack references today:** none in code. Docs: TODO rows `mod-additions #10` / `#11`, `farming/crops #8` (watering
  can held for "Bountiful Harvest or Extra Delight"), H&H report 2.5 / 2.6.
- **Already live:** nothing from ED. No old world holds ED ids, so anything dropped at port time needs no item merge. H&H's
  port is installed (tranches 1 and 2, 112 item ids); its in-game checks have not been run.
- **Dumps:** the 2026-11-08 re-scan used `kubejs/exported/items_by_mod/` and `tags/` from 2026-11-08 (H&H included).
  `display_names.json` is from 2026-10-20, so in-tree `lang/en_us.json` files supplied the newer names. ED isn't installed, so
  nothing in the dumps needs refreshing for it yet.

## 1. Overview

ED is a large kitchen-process mod. It adds its own **workstations**: an oven that bakes in pans (pie dish, loaf pan,
square pan, tray, sheet, muffin tray, baking stone, bar mold), a mixing bowl that needs stirring with a spoon, a mortar and pestle
(10 stones), a chiller, a juicer, a melting pot, a fermenting vat with a lid, a drying rack, an evaporator, dough shaping
(GUI), a yeast pot, a vinegar pot, a keg (8-bucket tank), a funnel and tap, and feast and pie blocks served with offset
spatulas. It also adds:

- **Crops and trees:** corn (two-block, its own), chili, coffee, cucumber, garlic, ginger, mallow root, mint (spreads),
  peanut and soybean; apple, lemon, lime, orange, grapefruit, hazelnut and cinnamon trees.
- **Food:** about 460 edible items. Breakfasts, soups, stews (feast blocks), pastas, curries, pickles, cookies (with
  doughs and shaped gingerbread and sugar cookies), cakes, cheesecakes, pies, custards, ice creams, milkshakes,
  popsicles, four chocolates with bars, chips, truffles and dipped foods, candies and jellies, citrus drinks, and dynamic
  jam and toast (one item whose ingredients set its name and colour).
- **Decor:** 208 moulded wallpapers, kitchen furniture in 13 woods, gingham cloth and carpets, ribbons, wreaths,
  picnic baskets, chocolate building blocks, jelly and candy cane blocks, frosted gingerbread blocks.
- **Halloween:** from 1 October to 10 November (real date, config `allYearSpooky`), dense corn can give Nausea. When
  the Nausea ends, it pulls the player into the **Cornfield** dimension: a corn maze generated by `EllersGen`, with
  23 structures (barn, camps, doll circles, sacrifice sites, a haunted house) and corn chest loot. Death there is
  cancelled: corn-tagged items are confiscated and the player is sent home. Blocks can't be broken there.

### Facts

| Fact | Value |
|---|---|
| Mod id, version | `extradelight` 2.6.6 (toml 2.6.5), NeoForge 1.21.1 |
| Items | 1441 item models; 1490 `item.` / `block.` lang names (1291 with an item model) |
| Blocks | 822 blockstates |
| Classification (regex, `ed_names.tsv` in the session scratchpad) | food / ingredient / placed food 695, furniture and display 233, wallpapers 208, candy and chocolate decor 99, fluid blocks and buckets 73, storage blocks 58, stations and tools 51, wood 28, saplings and leaves 21, crops and wild crops 24 |
| Fluids (35) | apple cider, BBQ, broth, cactus juice, caramel sauce, cocoa butter, chocolate nut spread, coffee, 4 chocolate syrups (milk, dark, white, blood), egg mix, egg white, glow berry / grapefruit / lemon / lime / melon / orange / sweet berry / tomato juice, gravy, hot cocoa, jam, ketchup, marshmallow fluff, mayo, milkshake, nut butter, oil, pickle juice, tea, vinegar, whipped cream |
| Effects | Pickled ("chance to negate poison tick", 4 levels), Pucker Up (`sour_pucker`, shrinks field of view), Sunshine (lowers Darkness / Blindness levels); a `SugarRushEffect` class exists but is never registered (B2) |
| Potions, enchantments, entities | none |
| Recipes (about 2470 files) | 622 shapeless, 525 `oven`, 283 shaped, 209 Create mixing, 135 FD cooking, 120 `feast`, 108 `mixing_bowl`, 106 FD cutting, 75 `chiller`, 37 `bottle_fluid`, 35 `mortar`, 33 each smelting / smoking / campfire, 23 `vat`, 21 `dough_shaping`, 15 `tool_on_block`, 12 `dynamic_jam`, 11 `juicer`, 11 `melting_pot`, 10 `drying_rack`, 7 `dynamic_toast`, 2 smithing, 1 `evaporator`, 1 `flour_dough`, 1 Butchercraft grinder |
| Recipe overrides | turns off 13 Farmer's Delight recipes (apple and chocolate pie, pie crust, beef and fish stew, honey and sweet berry cookie, honey glazed ham, melon popsicle, roast chicken, shepherd's pie, sweet berry cheesecake, wheat dough from water) and replaces them with oven, mixing bowl or feast recipes; replaces FD hot cocoa, fruit salad, mixed salad |
| Loot | 785 block tables, 4 corn chest tables (common to legendary), 7 structure tables, evaporator; 21 GLMs add food, curry, furikake, cactus juice, mortar and pestle loot to vanilla chests (dungeon, city, igloo, outpost, ship, butcher, mansion, pyramid, village, ruins, portal, desert) |
| Worldgen | 7 trees (apple, cinnamon, grapefruit, hazelnut, lemon, lime, orange) and 10 wild patches (chili, coffee, corn, cucumber, garlic, ginger, mallow, mint, peanut, soybean) by NeoForge biome modifier; the Cornfield dimension (biome, noise, maze and rail features, 23 jigsaw structures in one structure set) |
| Tags | 313 `c:` item tags, 76 block, 1 fluid; Serene Seasons, Tough As Nails thirst, Pizza Delight, Brewin', Cultural Delights, The Salt (`can_be_salted`) |
| Advancements | 2160 (mostly recipe unlocks); the pack drops all advancements |
| Trades | villagers (farmer) and wandering trader: coffee cherries, mallow root, ginger and more for emeralds |
| Other | 4 paintings (big citrus slices), citrus rind banner pattern, melon slice leaves a Melon Rind when eaten, ED oven recipe book (config, off) |
| Config | debug mode, oven recipe book, mint spread on/off and rate, all-year spooky |

## 2. Overlap (first)

Searched: `item_dict.js`, `items_by_mod/*.json` (2026-10-24), `display_names.json`, in-tree lang, the H&H jar's lang
and recipes, [item_merges.md](item_merges.md) and the overlap tables of [hearth_and_harvest.md](hearth_and_harvest.md),
[vintage_delight.md](vintage_delight.md), [fruits_delight.md](fruits_delight.md), [corn_delight.md](corn_delight.md),
[rustic_delight.md](rustic_delight.md), [oven.md](oven.md), [kitchen.md](kitchen.md). Search: normalised display-name
match, id-suffix match, then a keyword sweep over every installed mod, curated by hand.

Like H&H, "Drop" means **not registered by the port** (no merge, no old ids); the port's recipes that used a dropped
item point at the pack survivor or a tag. If D1 picks the native 1.3.3 jar instead, every Drop row becomes a
`global.addItemMerges` pair plus a JEI hide (4.6).

### 2.1 Items

**Crops, trees, produce**

| ED item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Corn, Corn Seeds, Unshucked Corn, Corn Cob, Corn Top / Bottom, Wild Corn | `corn_delight:corn`, seeds, wild corn; H&H corn (dropped there) | third corn | Drop; Corn Delight's. ED's corn husk, silk and dried husk are byproducts of shucking: keep them as Corn Delight cutting outputs (D4) |
| Cornmeal, Corn Flakes source | `creativeengineering:cornmeal` | same ingredient | Drop cornmeal; `i.cornmeal` |
| Cucumber, Cucumber Seed, Wild Cucumber | `vintagedelight:cucumber`, seeds, wild cucumbers | same crop | Drop; Vintage's |
| Peanuts, Peanuts in the Shell, Wild Peanut, peanut crop | `vintagedelight:peanut`, wild peanuts; H&H peanut (dropped) | same crop | Drop; Vintage's. In-shell step dropped with it |
| Garlic, Garlic Clove, Wild Garlic | `veggiesdelight:garlic`, `garlic_clove`, wild garlic | same crop | Drop; Veggies' |
| Mint, mint crop | `neapolitan:mint_leaves`, mint sprout | same herb; ED's spreads (config) | Drop; Neapolitan's mint. Spreading: not carried over |
| Coffee Cherries, Green Coffee, Coffee Beans, Ground Coffee, coffee bush, wild coffee | `farmersrespite:coffee_berries`, `coffee_beans`, wild coffee bush; `rusticdelight:coffee_beans` | third coffee crop | Drop the crop and beans; ED's coffee dishes take `#forge:coffee_beans` (Respite). Ground Coffee: D8 |
| Apple tree (sapling, leaves, petal litter) | Fruits Delight apple tree, Vinery apple tree | third apple tree | Drop |
| Lemon, Orange (+ trees, leaves, crates, juice) | `fruitsdelight:lemon`, `orange`, trees, crates, juices | same fruit and trees | Drop; Fruits Delight's. ED's zest, slices and citrus dishes take Fruits Delight's fruit |
| Lime, Grapefruit (+ trees) | none (`lime` only matches dyes) | new | keep (3), D5 |
| Hazelnuts, Hazelnuts in Shell, hazelnut tree | none | new nut and tree | keep (3) |
| Cinnamon tree, Raw Cinnamon, Cinnamon Bark / Stick, Ground Cinnamon, cinnamon wood set | none | new spice and wood | keep (3) |
| Chili, Ginger, Mallow Root, Soybean (+ seeds, wild) | none (Rustic bell peppers, Vintage ghost pepper are other plants) | new | keep (3) |
| Sunflower Seeds | `hearthandharvest:sunflower_seeds` (installed) | same item | Drop; H&H's |
| Cactus (food item `extradelight:cactus`), Cooked Cactus | `delightful:cactus_flesh`, `cactus_steak` | same food | Drop; Delightful's |
| Salt | `vintagedelight:salt_dust` (pack salt, item_merges 2.9), The Salt, H&H salt | fifth salt | Drop; `#forge:salts` |
| Flour | `create:wheat_flour`, `#forge:flour/wheat`; H&H flour (dropped) | same | Drop; `i.flour` |

**Dairy, eggs, condiments, ingredients**

| ED item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Butter (+ block, slab, stairs) | `hearthandharvest:butter` (installed, the only `#forge:butter`) | same item | Drop the item; H&H's. Block, slab and stairs: new, crafted from H&H butter (D10) |
| Cheese (+ block, slab, stairs) | Vintage cheese wheel / slice, Brewin' cheeses, `#forge:cheese`; H&H cheese (dropped) | another cheese | Drop the item; `#forge:cheese`. Blocks: D10 |
| Cooking Oil, oil fluid | `rusticdelight:cooking_oil`; H&H (dropped) | same | Drop; Rustic's, pack oil fluid if one exists (alcohol.md 4.1 fluid rule) |
| Vinegar, vinegar fluid, Vinegar Pot | `vintagedelight:vinegar_bottle` / `vinegar_jar` | same | Drop; Vintage's |
| Jar | `vintagedelight:empty_mason_jar`; H&H jar (dropped) | third food jar | Drop; mason jar (`dishes/jams #1`) |
| Nut Butter (bottle, fluid), Chocolate Nut Butter Spread | Vintage nut mash, Delightful nut butter (merge planned, item_merges 5.1); H&H peanut butter (dropped) | same spread | Drop nut butter; the nut mash survivor. Chocolate spread is new (needs hazelnut, keep) |
| Apple Sauce | `vintagedelight:apple_sauce_bottle` / `jar` | same | Drop |
| Kimchi | `vintagedelight:kimchi`, `brewinandchewin:kimchi` | third kimchi | Drop |
| Pickled Onion, Beet, Egg, Cucumber (gherkin) and their jars | Vintage pickled onion, beetroot, egg, pickle | same pickles | Drop; Vintage's |
| Pickled Carrot (+ jar) | `hearthandharvest:pickled_carrots` (installed) | same item | Drop; H&H's |
| Scrambled Eggs | `incubation:scrambled_eggs` | same | Drop |
| Omelette | `moredelight:omelette` | same | Drop |
| Boiled Egg, Egg White, Egg Yolk, Egg Mix fluid | pack egg solution fluid (recipes_cooking.md 2.2) | egg mix = egg solution | Drop Egg Mix; map to the egg solution. Boiled egg, white, yolk new |
| Ice Cubes | `neapolitan:ice_cubes` | same | Drop |
| Cocoa Butter, Cocoa Powder | `create_confectionery:cocoa_butter`, `cocoa_powder` | same | Drop; Confectionery's |
| Caramel Sauce (item, fluid) | `create_confectionery:caramel` fluid, bar of caramel | same ingredient | Drop; Confectionery's caramel fluid |
| Marshmallow | `create_confectionery:marshmallow`; Delightful stick | same; ED's is made from mallow root | Drop the item, keep mallow root as a marshmallow source (D6) |
| Whipped Cream, Stiff Peaks, Meringue | none (pack frosting is a different item) | new | keep |
| Yeast, Yeast Spread | none | new | keep |
| Bread Slice, Toast | `moredelight:bread_slice`, `moredelight:toast` | same | Drop; More Delight's. Dynamic toast: D9 |
| Croutons, Breadcrumbs | none | new | keep |
| Cookie doughs (apple, chocolate chip, chocolate, glow berry, honey, nut butter, pumpkin, sugar, sweet berry, gingerbread) | pack cookie dough base (oven.md 2.6: every cookie is cookie dough + flavour) | competing cookie chain | D3: cookies follow the pack base, ED's per-flavour doughs dropped |

**Drinks**

| ED item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Coffee (drink, fluid) | `farmersrespite:coffee` (+ long / strong), `rusticdelight:coffee` | same | Drop; Respite's |
| Tea (drink, fluid) | Respite green / black / yellow tea | same concept | Drop; Respite's teas |
| Hot Cocoa fluid | `farmersdelight:hot_cocoa` | fluid for an existing item | map to the pack's hot cocoa fluid |
| Apple Cider fluid, Melon Juice fluid | `farmersdelight:apple_cider`, `melon_juice` | fluid for an existing item | map to the pack fluids (Central Kitchen) |
| Lemon Juice, Orange Juice | Fruits Delight juices | same | Drop |
| Glow Berry Juice, Sweet Berry Juice | `hearthandharvest:glow_berry_juice`, `sweet_berry_juice` (installed) | same item | Drop, with ED's fluids; H&H's |
| Chocolate Milk | `hearthandharvest:chocolate_milk_bottle` (installed); Neapolitan chocolate milkshake | same item | Drop; H&H's |
| Chocolate, Mint Chip Milkshake; Chocolate Ice Cream | `neapolitan:chocolate_milkshake`, `mint_milkshake`, `chocolate_ice_cream`, `mint_ice_cream`; D&D chocolate milkshake | same | Drop; Neapolitan's |
| Milkshake (plain, fluid) | `neapolitan:vanilla_milkshake`, D&D vanilla milkshake bucket | similar | D7 |
| Tomato Juice, Lime / Grapefruit / Cactus / Pickle Juice, Lemonade, Limeade, Orangeade, Ginger Beer, Horchata, Eggnog, Punch, Xocolatl, Gourmet Hot Chocolate, Dalgona Coffee, Soy Milk | none | new | keep (3) |

**Baked goods, sweets, dishes** (same food under another id)

| ED item | Overlaps with | Proposal |
|---|---|---|
| Cornbread, Cream Corn, Popcorn, Caramel Popcorn, Grilled Corn on Cob, Corn on Cob | Corn Delight `cornbread`, `creamed_corn`, `popcorn`, `caramel_popcorn`, `grilled_corn`, `boiled_corn` | Drop |
| Chocolate Cake | `createaddition:chocolate_cake` (merge survivor 2.3); H&H (dropped) | Drop |
| Coffee Cake | `farmersrespite:coffee_cake` | Drop |
| Pumpkin Pie (placed) | vanilla, Delightful, Central Kitchen pumpkin pie | Drop (the pack's pumpkin pie is already a merge set) |
| Quiche | `brewinandchewin:quiche` | Drop |
| Lasagna (`lasanga`) | `veggiesdelight:lasagna_pan` | Drop |
| Cheeseburger | `delightful:cheeseburger`, `vintagedelight:cheese_burger` | Drop |
| Fried Chicken, Fried Fish, Fried Mushrooms | Rustic Delight | Drop |
| Potato Salad, Cucumber Salad | More Delight / Oceanic / Rustic potato salad (already three), Vintage cucumber salad | Drop |
| Garlic Bread | `veggiesdelight:garlic_bread` | Drop |
| Carrot Soup, Cactus Soup, Onion Soup | More Delight carrot soup, Delightful cactus soup, Brewin' creamy onion soup | Drop carrot and cactus; onion soup is close: D6 |
| Fish and Chips | `veggiesdelight:fish_and_chips` | Drop |
| Orange Chicken | `fruitsdelight:orange_chicken` | Drop |
| Jerky | `brewinandchewin:jerky`; H&H (dropped) | Drop |
| Smore, Caramel Apple | `delightful:smore`, `create_factory:caramel_apple`; H&H (dropped) | Drop |
| Gingerbread Cookie, Candy Canes | Create Confectionery gingerbread, gingerbread man, candy cane | Drop the plain gingerbread and canes; the shaped cookies (Steve, Alex, Creeper, sword...) are new: D6 |
| Chocolate bars (milk, dark, white) | `create:bar_of_chocolate`, Confectionery black / white chocolate bars, `neapolitan:chocolate_bar` | Drop; D6 for the chips, truffles and dipped lines |
| Mashed Potatoes with Gravy | Veggies / More Delight mashed potatoes | keep (gravy dish), base on the mashed potatoes survivor |
| Chili con Carne, Curry | Respite blazing chili, Vintage ghostly chili; Ender's chicken curry, Neapolitan adzuki curry | different dishes, keep |
| Muffin Tin (`muffin_tray`) and the other pans | pack bakeware (baking sheet, cake pan, pie tin, muffin tin) | D3 |

**Stations, tools, furniture**

| ED item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Oven | `creativeengineering:oven` (oven.md), Cooking for Blockheads oven, Handcrafted oven | second food oven with its own recipe type | D3 (recommend drop, rebuild on the pack oven) |
| Pie Dish, Loaf Pan, Square Pan, Tray, Sheet, Muffin Tray, Baking Stone, Bar Mold | pack bakeware: pie tin, cake pan, baking sheet, muffin tin | second bakeware set | D3 (map 4, keep or drop the other 4) |
| Mortar (stone block), 10 Pestles | `creativeengineering:mortar_and_pestle` (recipes_cooking.md `foodGrinding`) | second mortar | Drop; ED mortar recipes through NCH `foodGrinding` |
| Evaporator | `vintagedelight:evaporator` (salt) | same block | Drop |
| Keg (8-bucket tank with tap) | `brewinandchewin:keg`, H&H keg, H&H jug (8 buckets) | fourth keg | D11 (recommend drop, H&H jug covers the tank) |
| Whisk | `create:whisk` (Create mixer part) | name clash | rename "Kitchen Whisk" if kept (4.16) |
| Sink, Counter Cabinet (13 woods each) | Cooking for Blockheads sink and counter, H&H sink / counter (H&H D10: drop) | another family | Drop (follows H&H D10) |
| Half Cabinet (13 woods) | **`hearthandharvest:*_half_cabinet` (9 woods, installed)**, FD cabinets | same block | Drop the 9 shared woods; cherry and bamboo blocked (2.4); cinnamon and fruit: D11 |
| Spice Rack (13 woods, empty and full) | `cookingforblockheads:spice_rack`, `creativedelight:*_spice_rack` (20 woods) | same | Drop |
| Food Display (13 woods) | Display Delight plates, Supplementaries item shelf | same job | Drop |
| Crates: Apple, Golden Apple, Golden Carrot, Sweet Berry, Glow Berry, Red / Brown Mushroom | Quark crates and sacks, Fungi Delight mushroom crates, Fruits Delight apple crate; the same H&H crates (dropped) | same block | Drop |
| Sacks: Cocoa Beans, Coffee Beans, Sugar, Flour, Cornmeal, Peanut (3) | Quark cocoa sack, Respite coffee sack, Create Compressed flour pile; Vintage peanut crate | same | Drop; keep sacks for new items only (chili powder, cinnamon, hazelnut, ginger, mallow powder, soybean, cocoa solids, breadcrumbs) |
| Corn Crate, Corn Sack, Corn Cob / Husk Bundles | Corn Delight crate and kernel bag; `hearthandharvest:corn_husk_bundle` (installed) | same | Drop the crate, sack and husk bundle; dried husk bundle and cob bundle are new |
| Salt Block | The Salt `salt_block`, Vintage salt block | same | Drop |

### 2.2 Mob drops, dishes, ingredients

- ED replaces no mob loot. Its chest GLMs add food to 16 vanilla chest tables (4.5).
- Same food under another id: 2.1. All are "Drop at port".
- **Tag inputs nothing in the pack fills** (3.6 B5): `c:sausage` (+ raw / cooked), offal (`c:liver`, `heart`,
  `brain`, `lung`, `stomach`, `blood`, `offal`), cuts (`c:chicken/breast|thigh|wing|cubed`, `c:beef/cubed|scrap|ground|oxtail|roast`,
  `c:pork/roast|scrap/cooked`, `c:meat/ribs/raw`, `c:mutton/ribs|cooked`, `c:fat`). These come from Butchercraft, which
  isn't installed. H&H's sausages exist now but no `forge:sausage` tag does (2.6); FD cuts can fill the cut tags (D13).
- Inputs to retag at port time (ED literal ids that go to a pack survivor): corn, cucumber, peanut, garlic, mint, coffee
  beans, apple / lemon / orange, cheese, butter (if H&H's survives), oil, vinegar, nut butter, salt, flour, cornmeal,
  jar, cocoa butter and powder, caramel, marshmallow, chocolate bars, bread slice, toast, egg mix.

### 2.3 Systems

| ED system | Pack system | Overlap | Proposal |
|---|---|---|---|
| Oven + pans (525 recipes) | patcherMod oven, `creativeengineering:baking`, bakeware, baking overhaul (oven.md 2.6: cake base, pie crust, frosting, cookie dough) | **same system** | D3: rebuild on the pack oven with pack bases |
| ED turns off 13 FD recipes (pies, crust, cookies, stews, cheesecake, roast chicken, shepherd's pie...) | the pack's baking overhaul already replaced most of these | conflict | don't carry over the `neoforge:false` overrides; the pack's own removals stand |
| Mixing bowl (108, stirring with a spoon) | crafting grid, Cooking Pot `_manual_only` rule, Create mixer (ED itself ships 209 Create mixing mirrors) | partly | D3 |
| Mortar and pestle (35) | NCH `foodGrinding` (crushing wheels, Millstone, pack mortar) | **same** | through `foodGrinding` |
| Chiller (75: ice cream, jellies, popsicles, puddings) | none (Neapolitan ice cream by crafting) | new | D3 (keep) |
| Juicer (11) | Vinery Apple Press (H&H's juices are built on it), H&H stomping basin (pending eval), Create | **same job** | Drop the juicer; kept juices on the Apple Press (D12) |
| Melting pot (11, chocolate syrups) | Create Confectionery chocolate fluids, Create mixer | partly | D6 | **(rev 11-09, R1)** the melting pot is registered; its recipes are also written for the Create mixer. |
| Vat (23: pickles, kimchi, sauerkraut, salami, natto, miso) | Vintage fermenting jar (pack pickling, `dishes/pickles-and-ferments`), Brewin' keg, H&H keg | **same job** | Drop the vat; recipes onto the Vintage jar (step 9) |
| Drying rack (10: dried fruit, chili, jerky, corn husk) | none in the pack | new | keep |
| Dough shaping (21: shaped cookies, pasta shapes) | none | new | keep |
| Evaporator, salt | Vintage evaporator, The Salt, `#forge:salts` | **same** | Drop |
| Feast blocks (120 `feast` recipes, 54 feast blocks, 4 fondues) | FD feast blocks, Display Delight placeables, placeable rules (placeable_food.md) | format | keep the blocks; serve rules per placeable_food.md |
| Dynamic jam (12) and dynamic toast (7): one item, data component ingredients | jam family (`dishes/jams #1`: Fruits Delight jam item in a mason jar, one id per fruit) | **conflict** | D9 (recommend drop) |
| Bottle fluid (37) and fluids (35) | pack fluids rule (alcohol.md 4.1, Central Kitchen fluids, potion buckets) | format | fluids for kept items only, mapped to existing fluids where they exist |
| Coffee chain (cherries -> green -> roasted -> ground -> coffee) | Farmer's Respite coffee | **same** | D8 |
| Chocolate (4 kinds, bars, chips, syrups, truffles, dipped, fondues, building blocks) | Create chocolate, Create Confectionery (milk, black, white, ruby; bars, candies, glazed), Neapolitan bar | **same** base | D6 |
| Ice cream, milkshakes, popsicles | Neapolitan ice cream / milkshakes, FD melon popsicle, Delightful, More Delight popsicles | partly | D7 |
| Citrus and fruit trees (7) | Fruits Delight trees (apple, lemon, orange...), Vinery apple tree; pack tree worldgen | 3 of 7 the same | D5 |
| Corn and the Cornfield dimension | Corn Delight corn; H&H Corn Maze structure | same crop, competing maze | D4 |
| Wild crops (10 patches) | `wild_crops.json`, `tools/gen_crop_worldgen_table.py` (crops.md 2.7) | corn, coffee, cucumber, garlic, mint, peanut already placed by the pack | keep chili, ginger, mallow, soybean through `wild_crops.json` (step 14) |
| Effects: Sunshine (lowers Darkness / Blindness) | H&H Clarity (registered, under eval against Fruits Delight's Brightening, N26-9) | **same job, third effect** | Drop Sunshine (D12) |
| Chest GLMs (21) | loot system (loot.md, `loot_runtime_additions.json`) | format | through loot.md (step 5) |
| Villager and wandering trades (code) | Dynamic Villager Trades, wandering trader catalog | format | drop the code (D14) |
| Melon slice leaves a rind (event) | none | new, small | keep (rind feeds pickled rinds, stir fry) |
| Seasons (`sereneseasons:` crop tags), Tough As Nails thirst tags | Serene Seasons is installed; TAN is not | format | seasons tags for kept crops via `global.tags`; TAN tags dropped |
| Recipe-unlock advancements (2160) | pack drops all advancements | dead | drop |

### 2.4 Blocked: 1.20 / 1.21 vanilla content or missing mods

| Items | Needs | Proposal |
|---|---|---|
| Cherry and bamboo variants of every furniture family (half cabinet, sink, counter, knife block, spice rack, food display, step stool, dried corn fence, wallpapers: about 40 ids) | vanilla cherry / bamboo wood (1.20) | cherry on Vinery's cherry wood (as H&H 2.4) or drop; bamboo drop |
| Petal litter (4 citrus, apple, hazelnut) | 1.20 pink petals block type | drop or wrap as a carpet-like block (port rule: no new worldgen types) |
| Butchercraft dishes (3.6 B5) | Butchercraft (not installed, Forge 1.20+) | D13 |
| Tough As Nails tags | TAN (not installed) | drop |
| Data components (dynamic jam / toast ingredients, feast state) | 1.20.5+ | NBT in the port, or dropped with D9 |

### 2.5 Hearth and Harvest vs Extra Delight (stage 2)

The pair overlaps on 41 rows. Rule used: **one survivor per row**. Prefer the mod that already uses the item more in
its own recipes (fewer retargets), then the one with the fuller family (blocks, fluid, placeable). H&H's 2.6
placeholders are all answered here.

**Same item in both (one survivor)**

| Item | H&H | ED | Uses in own recipes (H&H / ED) | Survivor |
|---|---|---|---|---|
| Butter | item | item + block, slab, stairs; `#c:butter` | 7 / 114 (tag) | **H&H** (installed, N22); ED's 114 recipes on `#forge:butter`; ED's block, slab, stairs kept |
| Half cabinets | 9 woods installed | 13 woods (11 vanilla + cinnamon + fruit) | - | **H&H** (installed); no other woods for now (N26-7), so ED's cinnamon and fruit: D11 |
| Sunflower Seeds | from sunflowers | from sunflowers, 2 uses | 0 / 2 | **H&H** (installed, N22) |
| Trail Mix | raisins + nuts | dried fruit + nuts | - | **H&H** (installed) |
| Glow Berry Juice, Sweet Berry Juice | bottle + fluid | bottle + fluid | 6 / 2 each | **H&H** (more uses, wine chain) |
| Pickled Carrots | keg / jar | vat + jar block | - | **H&H**'s food, made in the Vintage jar (both machines dropped) |
| Macaroni and Cheese | dish (built on FD raw pasta) | pot + bowl (feast) | - | **H&H** (installed, N22); ED's feast pot: D17 |
| Glazed Carrots | dish | dish | - | **H&H** (installed, N22) |
| Corn Husk, Corn Husk Bundle | husk + bundle, built with drop routes | husk, dried husk, bundles, doll | 5 / 4 | **H&H** (installed, N23-1); ED's dried husk, dried bundle and doll stay, made from H&H's husk |
| Juice block | stomping basin (15, pending eval); juices built on Vinery's Apple Press | juicer (11) | - | ED's dropped: D12 |
| Keg | fermenting keg | 8-bucket tank | - | both dropped: Brewin' keg ferments, H&H Fluid Jug stores (not built yet) |
| Jar | H&H jar | ED jar | 14 / 0 | both dropped: Vintage mason jar |
| Salt, salt block | yes | yes | - | both dropped |
| Crates (apple, golden apple, golden carrot, sweet / glow berry, mushrooms) | yes | yes | - | both dropped (Quark, Fruits Delight, Fungi) |
| Cooking oil, flour, cheese, cornmeal, corn, peanuts, roasted peanuts, popcorn, caramel apple, smore, chocolate cake, jerky, marshmallow, chocolate milk, sink, counter | yes | yes | - | both dropped (pack survivors in each report's 2.1) |
| Clarity / Sunshine | Clarity (registered, eval N26-9) | Sunshine | - | ED's dropped: D12 |
| Corn maze | Corn Maze structure (overworld) | Cornfield dimension (Halloween) | - | **one**: D4 |

**Complementary (one mod fills a gap in the other)**

| Need | From | Into |
|---|---|---|
| Sausage item for ED's 5 sausage dishes (currywurst, deviled sausages, pickled sausage, sauerkraut and sausage, sausage roll) | H&H Raw / Cooked Sausage (installed, cleaver recipes) | `#forge:sausage`, `#forge:sausage/raw`, `#forge:sausage/cooked`: **these tags don't exist yet**, step 3 adds them |
| Gravy for H&H Biscuits and Gravy | ED gravy (item, fluid) | Optional now: H&H's dish is built without gravy (cooked sausage, salt, milk, flour). Don't retarget unless the owner asks (scope rule) |
| Raisins for ED's dried fruit chain | H&H raisins (installed) | D15 |
| Jam fruits | H&H raspberry, cherry, grape | pack jam family (D9 drops ED's dynamic jam) |
| Brine (H&H) and pickle juice (ED) | both | keep both: brine is the pickling input, pickle juice the byproduct |
| Mallow root (ED) | ED crop | marshmallow source for H&H's charred marshmallow (H&H D7) |
| Hazelnut (ED) | ED tree | trail mix and nut mash inputs |

**One side only (no conflict)**

- H&H only: watering can, sprinkler, fertilizer bag, seed pouch, tree tapper and syrup, cask aging tiers, goat milk and
  goat cheese, raspberries, hops, mums, croissant, raspberry scone, cider donut, candy corn, biscuits and gravy, chicken
  pot pie, cleavers, pitchfork, horseshoe, manure family, salt licking, chicken plucking, wines and moonshine.
- ED only: chili, ginger, mallow root, soybean, lime, grapefruit, hazelnut, cinnamon; chiller, mixing bowl, drying rack,
  dough shaping, funnel and tap; ice creams, custards, cheesecakes, citrus desserts, chocolate lines, candies and
  jellies, soy foods (tofu-less: natto, miso, soy sauce, edamame), world cuisine dishes, kitchen decor.
- **Watering can** (`farming/crops #8`): ED has none, so H&H's is the one.
- **Muffins:** ED apple, cinnamon, ginger, sweet berry; H&H blueberry (dropped, Fruits Delight's). Disjoint, all on the
  pack muffin tin.
- **Pies:** H&H raspberry, grape, peanut butter, chicken pot pie; ED sweet berry, glow berry, key lime, lemon
  meringue, meat, bacon and egg, grasshopper, Mississippi mud, steak and pickled onion, milk tart, tarte tatin. Disjoint.

**Shape of the merge**

- Two port jars, not one. The licences differ: H&H is MIT (can be GPLv3), ED is CC BY-NC-SA (must stay CC BY-NC-SA).
  One shared drop list (this section) and one CD tree per mod for pack glue (food values, tags, recipes).
- Build order: H&H first (smaller, MIT, owns the farm tools and sausages ED's recipes need), then ED. The survivor
  table above is the contract: the ED port registers no id H&H owns.

### 2.6 Re-scan on top of the pack and H&H (2026-11-08)

Rule: **an installed mod always wins a shared item.** ED adds only what the pack lacks and takes existing items as inputs.

Method: all 1472 ED lang-named ids against 20669 pack ids (dumps of 2026-11-08), by normalised display name, id suffix and
word set; then 103 near matches and a 70-keyword concept sweep, read by hand. Result: 197 ids matched by name (28 of them
H&H ids), 103 near matches. A name match is only a lead: false ones are listed below.

**Survivors reversed by H&H being installed first**

| Item | 2026-10-30 | Now | Ids that leave ED |
|---|---|---|---|
| Butter | ED | H&H | `butter` (block, slab, stairs stay) |
| Sunflower Seeds | ED | H&H | `sunflower_seeds` |
| Macaroni and Cheese | ED | H&H | `macaroni_cheese`; `macaroni_cheese_block` per D17 |
| Glazed Carrots | ED | H&H | `glazed_carrot` |
| Corn Husk, Corn Husk Bundle | ED | H&H | `corn_husk`, `corn_husk_bundle` |
| Half cabinets | ED | H&H | the 9 shared woods |

Also H&H's, now installed: Trail Mix (was conditional on D15), Pickled Carrots, Glow Berry and Sweet Berry Juice, and
Chocolate Milk (the 2026-10-30 table dropped it from both mods; H&H registers it).

**New overlaps found (not in the 2026-10-30 tables)**

| ED item | Overlaps with | Proposal |
|---|---|---|
| Chili Powder | `barbequesdelight:chili_powder` | Drop; Barbeque's. The recipe dump shows no recipe for it, so ED's chili could become its source (D18) |
| BBQ Sauce (jar, fluid, bucket) | `barbequesdelight:barbeque_sauce` | Drop; Barbeque's. ED's BBQ dishes take it |
| Potato Soup; Rice Pudding; Sweet Berry Custard | `largemeals:potato_soup`, `rice_pudding`, `sweet_berry_custard` | Drop; Large Meals' |
| Rack of Lamb (+ feast block) | `largemeals:roasted_mutton_rack` (+ block, raw and cooked rack) | Drop; Large Meals' |
| Bacon Egg Sandwich | `moredelight:egg_with_bacon_sandwich` | Drop. Bacon Egg Cheese Sandwich stays |
| Roasted Garlic | `veggiesdelight:roasted_garlic_clove` | Drop; Veggies' |
| Step Stools (13 woods) | `creativeengineering:*_stepstool` (16 colours, the pack's own) | Drop (D11) |
| Egg Crate | `incubation:chicken_egg_crate` | Drop |
| Cucumber Crate, Garlic Crate | `vintagedelight:cucumber_crate`, `veggiesdelight:garlic_crate` | Drop |
| Sweet Berry and Glow Berry Crates | `quark:berry_sack`, `quark:glowberry_sack` (the Quark storage named in 2.1) | Drop stands |
| Pasta: Macaroni, Penne, Lasagna Noodles, Cooked Pasta | `farmersdelight:raw_pasta` (H&H's Mac and Cheese uses it) | New shapes, kept; ED's pasta dishes accept FD raw pasta only where the shape doesn't matter (D19) |
| Frosting (16 colours) | `creativeengineering:frosting` (one item) | Keep the colours; white is the pack's frosting (D3 baking bases) |

**Close but different food: keep, no merge**

Curry (`largemeals:chicken_curry`, Respite tea curry); Fudge (`neapolitan:vanilla_fudge`, `brewinandchewin:cocoa_fudge`);
French Fries (`endersdelight:steak_fries`, `pneumaticcraft:chips`); Stuffed Mushrooms and Baked Cod (Veggies' garlic versions);
Stuffing (`corn_delight:cornbread_stuffing`); Dipped Apple Slice (`create_factory:*_apple`, whole apples); Dipped Marshmallow
(`create_confectionery:*_glazed_marshmallow`: same idea, so D6 decides); Pumpkin Milkshake (`create_dd:pumpkin_milkshake_bucket` is
a Create fluid); Nut Butter Cookie (`hearthandharvest:peanut_butter_cookie`: same idea, D19); Roasted, Stewed and Stuffed Apples
(`hearthandharvest:baked_apple`); Grilled Corn on Cob (`hearthandharvest:street_corn`, dressed); Pamonha
(`hearthandharvest:tamale`); fruit bowl (`creativedelight:*_fruit_basket`); moulded wallpapers (`design_decor` wavy wallpaper, D11).

**False matches, no overlap**

`fruitsdelight:*_jelly` is named Jam in the pack and ED's jellies are gelatin desserts; `barbequesdelight:tray` is a serving tray;
`blocky_siege:mortar` is a weapon; `botania:lime_petal` is a flower petal; `simplehats:toast` is a hat; `supplementaries:jar` and
`redeco:jar` are storage.

**Tags and recipe inputs**

| Finding | Detail | Action |
|---|---|---|
| 273 recipe tags in ED | 70 already filled in the pack; 154 ED fills itself; 49 are in neither | Step 3 |
| Plain renames among the 49 | `c:tools/knife` (93 recipes) to `#forge:tools/knives`, `c:foods/berry` to `#forge:berries`, fish, strings, glass, stone | port conversion |
| ED's own vanilla-based tags among the 49 | `extradelight:sweetener` (266 recipes), `extradelight:broth` (54), `makes_stock` | ship with the port |
| Butchercraft-type tags | 27 tags, 35 recipe files: 6 sausage (5 dishes) and 29 others | sausage: add the tags. Others: D13 |
| `#forge:butter` | holds only H&H's butter, so it is ready for ED's 114 recipes | none |
| Items only in 1.20+ or missing mods | `minecraft:cherry_slab`, `bamboo_slab` (20 recipes each), `pink_petals`, `pitcher_plant`, `torchflower`, netherite template, `farmersdelight:bamboo_basket` (16), `butchercraft:extruder_tip` | drop or retarget per 2.4 |

**What this changes in the fold-in:** H&H's Apple Press route takes ED's kept juices (D12); ED's pickles follow the H&H pickle
station result (Vintage jar now, brine eval N26-1 open), and pickle juice stays a byproduct, so brine's id must stay stable.
H&H's juice, wine, milk and goat fluids exist, so ED maps to them and registers none of those.

### 2.7 The 1.3.3 jar and the licence (2026-11-08)

Read from `DecompiledMods/extradelight-1.3.3/` (jar in ATLauncher's `downloads/`), `tools/jar_diff.py` against 2.6.6, the
upstream repository, the CurseForge page and the licence text. Not compiled or loaded. Licence reading below is a working
interpretation, not legal advice.

#### 2.7.1 What 1.3.3 is

| | 1.3.3 | 2.6.6 |
|---|---|---|
| Loader | Forge 1.19.2 (loader 43), needs Farmer's Delight 1.2.0+ only | NeoForge 1.21.1 |
| Source | public: upstream branch `1.19.2`, head `22c522ad` ("1.3.3"); all 163 top-level jar classes have source; Parchment mappings | decompile only (349 files) |
| Named ids | 807 (755 shared, 52 only here) | 1473 (718 only here) |
| Recipes | 1362 own: 439 oven, 343 shapeless, 126 shaped, 124 FD cooking, 83 mixing bowl, 47 feast, 35 cutting, 31 mortar, 19 dough shaping, 15 tool on block, 8 drying rack; plus 194 Create mixing | about 2470 |
| Fluids, effects | none | 35, 3 |
| Stations | oven, mixing bowl, mortar, drying rack, dough shaping | those plus chiller, juicer, melting pot, vat, evaporator, keg, funnel and tap |
| Same as 2.6.6 | Cornfield dimension (23 structures), 21 chest GLMs, turns off FD recipes (12 files), advancements (1216) | |
| Soft dependencies | Create and Patchouli data, `ButchercraftAddon` has no Butchercraft class reference | |

Against the keep list (section 3):

| In 1.3.3 | Only in 2.6.6 |
|---|---|
| Ginger, cinnamon tree and wood, mixing bowl, drying rack, dough shaping, spoons, offset spatulas, grater, yeast pot | Chili, mallow root, soybean, lime, grapefruit, hazelnut, fruit wood |
| Cheesecakes, custards, ice creams, milkshakes, popsicles, cookies, shaped gingerbread and sugar cookies, 16 jellies, candies, stews and most mains | Chiller and every fluid; chocolate chips, truffles, dipped foods and fondues (8 chocolate ids against 123); pickles and ferments, soy chain |
| 176 wallpapers, ribbons, wreaths, candy and gingerbread blocks | Gingham, picnic baskets, Pickled and Pucker Up |

The 52 ids only in 1.3.3 are older dishes (jam and jam toast, rabbit and goat dishes, oyakodon, nasi kebuli, chicken
patties) and renamed tools (`mortar_*`, `muffin_tin`, `round_pan`). See D20.

**Routes**

| Route | Result |
|---|---|
| A. Native 1.3.3 jar, untouched | Rejected. Lacks about half the keep list; registers all 807 ids, so every Drop row is a merge and JEI hide; the Cornfield and chest GLMs stay unless overridden; 439 ED oven recipes run beside the pack oven; can't go in the deploy payload (2.7.2) |
| B. Port all of 2.6.6 (the 2026-10-30 plan) | Works, but ports 349 decompiled classes from NeoForge 1.21.1, including stations that 1.3.3 already has running on 1.19.2 |
| C. **Fork branch `1.19.2`, backport the missing lines from 2.6.6** | Recommended. The Forge 1.19.2 engine, stations, feasts and JEI code come as real source. Drops remove registrations (no merges, as in B). Port work shrinks to chiller, the kept fluids, new crops and trees, and new food |

Both versions carry the same licence and author, so combining them in one fork is allowed. Ids match for 755, so 2.6.6 data
carries over after the format changes of step 1. Kept recipes follow 2.6.6 where the two differ (1171 files changed
between the jars), so the overlap tables above stand.

#### 2.7.2 What the licence changes

**Which licence applies.** `license = "MIT"` in the 1.3.3 `mods.toml` should not be relied on. The same branch holds the
full CC BY-NC-SA 4.0 text in `LICENSE.txt`: the first commit (2024-04-03) added BY-NC by mistake and the next one,
"helps to c/p the proper thing", swapped in BY-NC-SA, three months before 1.3.3. CurseForge lists BY-NC-SA, the jar has no
MIT notice, and 2.6.6 declares `CC BY-NC-SA 4.0 Deed`. Only written permission from Lance5057 would change this.

| Clause | Effect on this plan |
|---|---|
| 2(a)(1)(b), adapting allowed | A fork of source is fine; no need to rewrite from behaviour as for ARR mods. The GitHub source replaces the decompile |
| 2(a)(4), format changes | Pure format conversion isn't an adaptation, but dropping ids and retargeting is, so the fork is Adapted Material |
| 3(b)(1) and (3), ShareAlike | The fork stays CC BY-NC-SA 4.0 or later. No GPLv3, no added restrictions on the fork |
| Section 4, database rights | Reusing a substantial part of ED's recipe, food value or name tables carries the same conditions. One recipe is thin; hundreds of transcribed recipes are not |
| 1(k) and 2(a)(1), NonCommercial | Free distribution is fine; paid access is not. The FAQ adds "no pay to play/cash shop modpacks or servers", so server ranks or paid perks would break it. The repo shows no monetisation (owner to confirm, D2) |
| 3(a)(1), attribution | Keep Lance5057 and all credited contributors from both versions, a licence and disclaimer notice, the link to the project, and a statement of what changed |
| 2(a)(6), no endorsement | Display name and description say it is an unofficial fork (FAQ: don't claim the mod as your own) |
| 6(a) and (b), termination | A breach ends the licence at once; it reinstates if cured within 30 days of discovery |

**Boundary with the GPLv3 jars** (the part that changes the fold-in):

- **ED-derived Java goes only in the fork.** Step 11 put Pickled and Pucker Up in Creative Common; that can't hold ED code.
  They stay in the fork, or Common gets its own version written from behaviour.
- **Transcribed ED data goes only in the fork**: recipe lists, food values, lang names and tooltips. The routing note
  put them in Creative Delight trees; they move to the fork's own script folders (it can carry `kubejs_startup/`, memory
  `kubejs-jar-startup-scripts`). Bare ids in `item_dict.js`, tags and recipes the pack designs itself stay GPLv3.
- **The fork shouldn't compile against GPLv3 jars.** NonCommercial is a further restriction GPLv3 forbids. Keep it on
  Farmer's Delight, Create and JEI APIs; script calls to `global.NCH` need a one-line permission from the owner, who holds
  that copyright.
- Bundling the fork alongside GPLv3 jars in the pack is aggregation and is fine; nesting it inside Creative Delight stays
  ruled out.

**Rehosting.** The licence alone would allow sharing the unmodified jar, but the FAQ asks not to, so the unmodified jar
stays out of the payload. The fork is a different work, and an unofficial 1.20.1 port is already on CurseForge under the
same licence. Route A would need a download step that `build_deploy.py` doesn't have.

## 3. New content

About 520 ids are new to the pack after sections 2.1 to 2.6 (the rest drop). By line:

### 3.1 Crops, trees, woods

| Line | Ids | Notes |
|---|---|---|
| Chili | chili, chili seeds, dried chili, sliced chili, hanging chili / dried chili, wild chili, chili crate, chili powder sack | dried on the drying rack; chili powder is Barbeque's (2.6, D18) |
| Ginger | ginger, ginger cutting (seed), peeled / sliced / grated ginger, candied ginger, pickled ginger, wild ginger, crate | propagated by cutting |
| Mallow root | mallow root, mallow powder, wild mallow root, crate, powder sack | marshmallow source |
| Soybean | soybean pod, soybeans, soaked / cooked / mashed soybeans, edamame, soy milk, wild soybean, sack | soy sauce, miso, natto chain |
| Lime, Grapefruit | fruit, juice, zest, slices, crate, sapling, leaves, wreath | on the shared **fruit wood** |
| Hazelnut | hazelnuts in shell, hazelnuts, roasted, sapling, leaves, sacks | |
| Cinnamon | sapling, leaves, log, wood, stripped, planks, slab, stairs, fence, gate, door, trapdoor, button, plate, cabinet; raw cinnamon, bark, stick, ground cinnamon | own wood set |
| Fruit wood | log, wood, stripped, planks, slab, stairs, fence, gate, door, trapdoor, button, plate, cabinet | wood of the citrus and apple trees |

Food values: ED defines 326 `FoodProperties`. Nutrition runs 0 to 17 (most 1 to 8); effects are FD Comfort and
Nourishment (brief / medium / long), vanilla effects and the three ED effects. The pack's food tuning sets values by
type ([food.md](food.md) 3.5), so the per-item table is generated after install (`food_catalog.md`), not copied here.

### 3.2 Food lines (counts approximate)

| Line | Examples | Count |
|---|---|---|
| Breakfast and eggs | boiled egg, egg in the basket, French toast, cinnamon toast, croque monsieur / madame, hash, hashbrowns, cereals (corn flakes, crisp rice) | about 25 |
| Soups and stews (feasts) | borscht, congee, lugaw, corn chowder, gazpacho, melon gazpacho, miso soup, mulligatawny, oxtail, potato, tomato, hazelnut, sauerkraut soup, zupa ogorkowa; beef / chicken / fish / lamb / pork / rabbit stew with rice | about 35 |
| Mains | beef bulgogi, beef wellington, BBQ ribs, chicken kiev, chicken parm, chicken alfredo, chicken fried steak, curry, hotdish, kimchi fried rice, meatloaf, pork tenderloin, pot roast, pulled pork, rack of lamb, salisbury steak, soy-glazed salmon, stir fry, haggis, liver and onions | about 60 (about 20 need Butchercraft tags, B5) |
| Pasta | macaroni, penne, lasagna noodles, aglio e olio, alfredo, buttered pasta, penne all'arrabbiata, preserved lemon pasta, pasta tomato | about 12 |
| Salads and sides | apple slaw, beet mint salad, cactus salad, carrot salad, citrus onion salad, egg salad, fish salad, grapefruit beetroot salad, seaweed salad, Shirazi salad, Thai melon salad, potato au gratin, french fries, potato chips / sticks, onion bhaji, jalapeno poppers | about 35 |
| Sandwiches and handhelds | bacon egg (cheese) sandwiches, egg / fish salad sandwich, grilled cheese, meatloaf / pork tenderloin / pulled pork sandwich, fluffer nutter, kiwiburger, mushroom burger, cheeseburger with pickle, sausage roll, riceball | about 20 |
| Pickles and ferments | pickled fish, ginger, melon rind, sausage; preserved lemons; sauerkraut; natto; miso paste; soy / fish sauce; kongjang | about 15 |
| Condiments | ketchup, mayo, BBQ sauce, aioli, hot sauce, gravy, alfredo sauce, curry powder, furikake, seaweed paste, yeast spread | about 15 |
| Cakes, cheesecakes, pies | 7 cheesecakes, black forest trifle, kyiv cake, melon layer cake, lemon cucumber cake, panforte, pavlova, Christmas pudding, sticky toffee pudding, baked Alaska, pumpkin roll, monkey bread, 11 pies and tarts (2.5) | about 70 with slices |
| Cookies and shaped dough | 10 cookie flavours (+ cookie blocks), 8 gingerbread and 8 sugar cookie shapes (raw and baked) | about 50 |
| Custards, puddings, ice cream, frozen | 7 custards, rice / corn pudding, lemon curd, lemon posset, lime souffle, 11 ice creams, sundae, 9 milkshakes, 7 popsicles, grapefruit sorbet, coffee jelly | about 45 |
| Chocolate and candy | chips, truffles, filled bars, dipped (apple, bacon, coffee bean, glow berry, graham cracker, marshmallow, sweet berry) for each chocolate; fondues; 16 candies, 3 mint candies, 16 jellies, gummies, fudge, toffee, nougat, butterscotch, peanut brittle, peppermint bark, rocky road, scotcharoos, blondies, brownies, crisp rice treats, mallow cup, peanut butter cup | about 130 |
| Drinks | 3.1 juices, ades, eggnog, horchata, punch, ginger beer, xocolatl, gourmet hot chocolate, dalgona | about 20 |
| Seasonal | easter egg, candy bowl, Halloween corn items | a few |

### 3.3 Stations and tools (all registered for evaluation, R1)

**Rule (R1):** every station below is registered. A recipe that is also written for a pack station is written for both:
the ED station keeps its original recipe (ED ids, ED pans, ED inputs where those survive) and the pack station gets a
copy with pack inputs (survivor items, mapped bakeware). Outputs are the same item either way. Evaluation decides which
station stays; the loser is removed by deleting its recipes and block, which is free because no ED id is live. Inputs
that were dropped (ED corn, ED coffee beans, ED lemon...) are retargeted to the survivor or its tag on **both** copies.

| ED station | Recipes | Pack station that also gets the recipes | Notes |
|---|---|---|---|
| Oven + 8 pans (pie dish, loaf pan, square pan, tray, sheet, muffin tray, baking stone, bar mold) | 525 | pack oven (`creativeengineering:baking`, patcherMod oven): pie dish -> pie tin, muffin tray -> muffin tin, sheet and tray -> baking sheet, square pan -> cake pan; loaf pan, baking stone, bar mold -> new bakeware or the cake pan / baking sheet | ED copy uses ED pans; pack copy uses mapped bakeware |
| Mixing bowl + spoons (wooden to netherite) | 108 | none (crafting grid / Cooking Pot `_manual_only` rule has no stirring); ED's 209 Create mixing mirrors stay as upstream ships them | nothing to duplicate |
| Mortar and pestle (10 stones) | 35 | NCH `foodGrinding` (crushing wheels, Millstone, pack mortar) | |
| Chiller | 75 | none (Neapolitan ice cream is by crafting) | nothing to duplicate |
| Juicer | 11 | Vinery Apple Press (H&H's juices are built on it) | the juicer keeps its dye and bone meal byproducts; the Apple Press copy loses them |
| Melting pot | 11 | Create mixer + Confectionery chocolate fluids | |
| Fermenting vat with lid | 23 | Vintage fermenting jar (pack pickling, `dishes/pickles-and-ferments`) | pickle juice stays a byproduct, brine id must stay stable (2.6) |
| Drying rack | 10 | none | |
| Dough shaping (GUI) | 21 | none | |
| Evaporator | 1 | Vintage evaporator, The Salt | salt output points at `#forge:salts` survivor |
| Yeast pot | | none found | |
| Vinegar pot | | none found in the pack; check Vintage / Brewin' for a vinegar source in eval | no decision row existed before; added by R1 |
| Keg (8-bucket tank), funnel and tap | | Brewin' keg / H&H keg | |
| Grater, whisk | | none | tools |
| Offset spatulas (5 tiers) | | none | serve feast and pie blocks |
| Corn husk doll, corn cob pipe | | none | decor in the Cornfield sense, see 3.7 |

Extra cost of R1 (so the eval pass can price it): each dual-routed recipe is an extra recipe to keep in step with the
bakeware map and the survivor-item retargets, and JEI shows the output under two stations. Both are accepted for the
eval period.

### 3.4 Decor and furniture (all registered for evaluation, R3)

Everything ED ships is registered, including the pieces D11 had dropped. Where the pack or H&H already has an equivalent,
**both exist under their own ids** until eval picks one (no merge, no hide before eval).

| Group | Count | Pack or H&H equivalent (compare in eval) |
|---|---|---|
| Moulded wallpapers (13 woods x 16 colours) | 208 | `design_decor` wavy wallpaper |
| Half cabinets (ED, 13 woods + cinnamon + fruit wood) | 15 families | H&H half cabinets (9 woods): ED's 9 shared woods now registered too |
| Step stools (13 woods) | 13 | `creativeengineering:*_stepstool` (16 colours) |
| Sinks, counters, spice racks, food displays, knife blocks | per wood | none found |
| Dried corn fences, picnic baskets (16), wreaths (15) | | none |
| Fruit and candy bowls, chocolate boxes (16), jar display | | `creativedelight:*_fruit_basket` (fruit bowl) |
| Gingham (16 + 16 carpets), ribbon bows (16) | | none |
| Chocolate building blocks (4 kinds x 8 shapes), jelly blocks (16), candy cane blocks (3), frosted gingerbread blocks (16) | | none (plain gingerbread and canes dropped by D6 for the food items, the decor blocks stay now) |
| Hanging produce (chili, corn, garlic, ham, mint, onions) | | none |
| Butter block, cheese block, slab, stairs | | H&H butter; Vintage cheese |
| Corn husk doll, dried corn husk bundle | | H&H corn husk (installed) |
| Paintings (4 big citrus slices), citrus rind banner pattern | | none |

Still blocked, not a choice: cherry and bamboo variants of every family (about 40 ids; vanilla 1.20 woods). Cherry maps to
Vinery cherry wood (as H&H 2.4) if you want it in eval; bamboo has no 1.19.2 plank in the pack, so it stays out unless a mod
supplies one. Petal litter stays dropped (1.20 block type). Everything else, including the pieces that duplicate the pack,
is in.

### 3.5 Effects

Pickled (negates poison ticks by chance, levels I to IV), Pucker Up (narrows the field of view), Sunshine (D12). JEED
text needed for each kept one.

### 3.6 Upstream issues

| # | Issue | Evidence | Fix |
|---|---|---|---|
| B1 | Version in the toml (2.6.5) differs from the jar name (2.6.6) | `META-INF/neoforge.mods.toml` | the port sets its own version |
| B2 | `SugarRushEffect` is never registered | `effects/SugarRushEffect.java`; `ExtraDelightMobEffects` registers only 3 | drop the class |
| B3 | 17 files under the 1.20 path `data/farmersdelight/recipes/` are dead on 1.21 (the live overrides are in `recipe/`) | jar layout | leftovers; drop |
| B4 | 4 biome modifiers under the 1.20 path `data/extradelight/forge/biome_modifier/` (cinnamon, mint, wild corn, wild ginger) duplicate the live `neoforge/` ones | jar layout | drop |
| B5 | 35 recipe files (re-scanned 2026-11-08; about 29 dishes) have a slot that only Butchercraft-style tags fill (`c:sausage*`, offal, cuts, `c:fat`); ED defines none of them, so these dishes are uncraftable without Butchercraft. Blood chocolate's whole line hangs on `c:blood` | tag scan of `data/*/recipe` against `data/*/tags` | D13 |
| B6 | Id typos: `lasanga`, `caramel_cheescake_slice` (display names are right) | lang | ids kept (port rule: keep upstream ids) |
| B7 | The Cornfield trigger reads the real date (1 October to 10 November), so it is active in normal play every autumn; today (2026-10-30) included | `CornTop.isHalloween`, `CornBottom` | follows D4 |
| B8 | 2160 recipe advancements (recipe book unlocks) | `data/*/advancement` | dropped by the pack |

No load errors: the jar was never loaded in the pack.

### 3.7 Cornfield dimension, maze, structures and Halloween portal (defined 2026-11-09, R4)

Read from the 2.6.6 jar and source (`CornTop`, `CornBottom`, `CornMazeEvents`, `CornMazeFeature`, `CornMazeRail`,
`worldgen/`, `structure/`). 1.3.3 has the dimension too (`dimension/corn_dimension.json`, the 23 structure templates under `structures/`, `CornMazeEvents`),
so the fork would inherit it; values below are 2.6.6's and 1.3.3's differ only if its jar says so (not diffed per value).
Nothing here is built. D4 says drop; this section is so you can decide with the full picture.

**Dimension** (`extradelight:cornfield`)

| Property | Value |
|---|---|
| Type | `extradelight:corn`: fixed time 18000 (always night), ultrawarm, no ceiling, skylight, `min_y` 0, height 64, `bed_works` false, `respawn_anchor_works` false, no raids, piglins not safe |
| Generator | noise, one fixed biome `extradelight:cornfield`; stone, sea level 0, no aquifers or ore veins, surface rule = grass block |
| Biome | fog colour dark purple (0x130B24), sky colour 0, grass and foliage 4798742, water 2955861; spawners: zombie (group of 4, weight 95), zombie villager (1, weight 5), witch (1, weight 5), bat (8, weight 10); `has_precipitation` true |
| Client | fog forced to a short cylinder (near plane -8, far x 0.25) and fog colour grey (0.5, 0.5, 0.5) while in the dimension |
| Features | `rail_placer` (step 1) and `patch_pumpkin` before the structures; `corn_placer` (maze) last |

**The maze** (`corn_maze_feature`, placed once per chunk)

- Per chunk, an 18 x 18 grid is made with `EllersGen` (Eller's algorithm, row by row), so each chunk holds an independent
  perfect maze; the border row and column are cut off, leaving a 16 x 16 field. Wall cells (`#`) become `extradelight:corn_bottom`
  (age 0, `dimension=true`); path cells stay open. Chunk mazes do not line up with each other, so seams are not guaranteed
  to connect.
- A wall cell is only placed if a 11 x 11 area around it holds only air or corn already, so structures and the rail
  are not overwritten.
- Corn in the dimension: a random tick sets the `dimension` flag on the block (`CornBottom.randomTick`), which makes it survive
  without light or the usual soil rule. Walking into it slows (x 0.8, 0.75, 0.4); **sprinting hurts 1** (sweet-berry-bush damage).
- **Rail line:** one feature per chunk row: only chunk Z = 0 gets it. Along Z = 0, an east-west rail on a 3-wide gravel bed with
  oak fences 4 blocks either side; every chunk X divisible by 100 (every 1600 blocks) a **rail stop** structure replaces the rail.

**Dimension rules**

- **Entry** is only by the portal below; spawn lands at **y = 33**, same X and Z as the corn block, motion kept.
- **Breaking blocks** is blocked for non-creative players: the break is cancelled and the player takes 1 magic damage and a
  witch-cackle sound. (Placing is not blocked.)
- **No death:** a hit that would kill is cancelled. The player is healed to full, **loses every inventory item in the
  `corn_dim_confiscate` tag** (19 items: corn cob, husk, on cob, seeds, silk, chowder, fritters, corn meal, corn pudding and
  its feast, cornbread and its feast, caramel popcorn, cooked corn, cream corn, dried husk, grilled corn on cob, popcorn,
  corn husk doll), and is teleported to their respawn point (world spawn if none) with the message
  `extradelight.corn_dimension.death`. Only the main inventory list (36 slots, hotbar included) is searched; armour and off-hand are untouched.
- **There is no exit portal.** Leaving is by dying, or by commands.

**Halloween portal** (an overworld corn crop, not a block)

1. Condition `isHalloween()`: real local date from **1 October to 10 November** inclusive, or config `allYearSpooky` = true.
2. The block must be **dense corn**: a `corn_top` with `DENSE` = true. A mature top becomes dense when its shape updates and the
   four neighbours (east, north, west, south) are also mature `corn_top` blocks, so only the inside of a mature corn field qualifies.
3. A player standing inside it gets Nausea (200 ticks = 10 s) with a 1 in 100 chance **per tick spent inside** (about 1 s expected).
4. When that Nausea has 3 ticks or fewer left, the block starts the portal: `setPortalCooldown(0)` then `setAsInsidePortal`, the
   crop being a `Portal` implementation with transition `CONFUSION` (the nausea wobble is the transition).
5. Destination `extradelight:cornfield` at (x, 33, z) of the crop. The transition's post action is empty.
6. Outside the date window (or with the config off) dense corn is plain corn.

Pack facts that matter for D4: the pack's corn is Corn Delight's, which has no `DENSE` state; with ED corn dropped (D4) nothing
triggers the portal. Keeping the dimension therefore needs either ED's corn crop kept for this purpose only or a hook on Corn
Delight's corn. The real-date check also clashes with Creative Calendar's reserved time and season handling, and
`allYearSpooky` is the only way around the date; the portal would be live every autumn in normal play (B7).
H&H's Corn Maze structure (stage 2) is a separate maze with no dimension change.

**The 23 structures.** 23 NBT templates; the structure set `extradelight:corn_structures` holds **21** of them
(`pumpkin_patch3` is unreferenced, `rail_stop` is placed by the rail feature, not by the set). All are single-piece jigsaw
structures: `size` 1, `max_distance_from_center` 80, step `surface_structures`, `WORLD_SURFACE_WG` projection, `beard_thin`
terrain adaptation, biome `extradelight:cornfield` only. Placement is `random_spread`, **spacing 15 chunks, separation 5**, so one
roll per 240-block cell; the weights decide which of the 21 appears. Total weight 645.

| Structure | Weight (share) | Size (x y z) | Blocks | Contents |
|---|---|---|---|---|
| Barn | 1 (0.2 %) | 29 x 17 x 19 | 2412: oak planks, weathered cut copper roof and stairs, spruce fence, 58 hay, 36 canvas rugs | 5 chests (3 common, 1 uncommon, 1 rare); zombie horse, bat, zombie |
| Haunted house | 1 (0.2 %) | 24 x 15 x 28 | 4422: spruce and dark oak planks and slabs, weathered cut copper, bricks, warped fence | 15 chests (10 common, 3 uncommon, 1 rare, **1 legendary**) |
| Camp 1 | 5 (0.8 %) | 17 x 11 x 16 | 690: spruce leaves and logs | 1 chest (common), 3 corn husk dolls, soul campfire, 2 jack o'lanterns |
| Camp 2 | 5 (0.8 %) | 16 x 6 x 16 | 443: wool, carpet, dandelions, barrels | 6 chests (3 uncommon, 2 common, 1 rare), onion crates, cornmeal sacks |
| Fountain | 5 (0.8 %) | 7 x 7 x 7 | 146: 20 water | soul campfire |
| Doll circle (plain) | 25 (3.9 %) | 7 x 2 x 7 | 57 | 8 corn husk dolls |
| Doll circle 1 | 25 (3.9 %) | 7 x 2 x 7 | 58 | 8 dolls, soul campfire |
| Doll circle 2 | 25 (3.9 %) | 7 x 2 x 7 | 58 | 8 dolls, butter block |
| Doll circle 3 | 25 (3.9 %) | 7 x 2 x 7 | 58 | 8 dolls, skeleton skull |
| Doll circle 4 | 25 (3.9 %) | 7 x 2 x 7 | 58 | 8 dolls, **spawner** (zombie) |
| Table 1, 2, 3 | 25 each (3.9 %) | 3 x 3 x 3 | 19 | spruce stairs and one dish (corn pudding, pumpkin pie, apple pie) |
| Table 4 | 25 (3.9 %) | 5 x 4 x 9 | 79 | long banquet: corn pudding, shepherd's pie, roast chicken, lamb stew, apple pie (feast blocks) |
| Pumpkin patch 2, 4, 5 | 100 each (15.5 %) | 3 x 4 x 3 | 36 | 25 pumpkins, carved pumpkin or jack o'lantern, patch 2 has a barrel and 1 chest (common), 4 and 5 a pumpkin pie |
| Pumpkin pile 1 | 100 (15.5 %) | 3 x 3 x 3 | 16 | 5 pumpkins, carved, jack o'lantern |
| Sacrifice, Sacrifice 2 | 1 each (0.2 %) | 3 x 4 x 3 | 22 | 5 corn husk dolls, fences and trapdoors (sacrifice has a soul campfire) |
| Sign | 1 (0.2 %) | 1 x 2 x 1 | 2 | one green canvas sign |
| Pumpkin patch 3 | not in the set | 3 x 4 x 3 | 36 | 25 pumpkins, carved, pie: unused leftover |
| Rail stop | placed by the rail feature, every 1600 blocks on Z = 0 | 16 x 8 x 16 | 698: gravel, bricks, spruce planks, 32 rails, 20 signs, 14 oak fences | 1 chest (uncommon) |

Shares by group: pumpkin patches and pile 62 %, doll circles 19 %, tables 15.5 %, camps 1.6 %, everything else under 1 % each
(the haunted house is a 1 in 645 roll per cell).

**Chest loot** (4 tables, `extradelight:chests/corn_*`, 12 / 11 / 24 / 12 entries)

| Table | Examples |
|---|---|
| Common | candy and caramel apples, caramel candy, corn cob, husk, seeds, silk, unshucked corn, bone, bone meal, rotten flesh, skeleton skull |
| Uncommon | corn chowder, fritters, pudding, cornbread, cream corn, popcorn, caramel candy, FD flint and iron knife, rope, skillet |
| Rare | milk, dark, white and blood chocolate bars, filled bars and truffles, butter, chocolate cake, cake, corn crate, corn pudding and cornbread feasts, dried husk bundle, FD cooking pot, golden knife, gold ingot, cornflower |
| Legendary | candy golden apple, caramel golden apple, golden apple, gold block, gold ingot, butter block, corn crate, easter egg, feasts, dried husk bundle |

Death is the only exit (see rules), and it removes the confiscate-tag items, so the corn-themed loot (common and uncommon
tables, plus the feasts, husk bundle and corn crate in rare and legendary) is lost on the way out; the chocolate, gold, knives,
skillet, cooking pot, bone and cake survive. The confiscate tag matches the loot's corn items one to one.

**Pack decisions this implies** (not made):

- Corn items in the loot are ED ids; with ED corn dropped (D4) they retarget to Corn Delight's corn, cob and cornmeal, and the
  confiscate tag follows. Blood chocolate in the rare table is already dropped by D6.
- The corn husk doll (decor, turns to face players, only animated inside the dimension) is registered for eval under R3 either way.
- A dimension adds a world folder, a dimension id for Dynamic Villager Trades, Async Locator and the structure-gen config
  to ignore, and the pack's own structure framework does not touch it (biome `extradelight:cornfield` only).

### 3.8 Texture comparison plan (R5)

Goal: for every ED item, block and fluid that is **dropped, merged or evaluated against a pack equivalent**, render the ED texture
next to the texture the pack keeps, so the choice can be made by eye and kept as a record. Plan, group list, output paths and
tooling are in [extra_delight_textures.md](extra_delight_textures.md); output lands in
`feature_writeup/extra_delight_textures/` (one PNG per group, committed). TODO row added.

## 4. Fold-in, in build order

0. **Decide D1 and D2 first** (route; licence and delivery; 2.7). The steps below assume the recommendation: a fork of
   upstream's `1.19.2` branch that registers only the kept content, with the missing lines backported from 2.6.6.
1. **Fork and backport** (skill `port`, [feature_porting.md](feature_porting.md)): `development/backportedExtraDelight/`
   (mod id `extradelight`, version `1.3.3-ce-19.2-01`) from commit `22c522ad`, with the fork's licence file, notices and
   display name per 2.7.2, root `settings.gradle`, `IN_TREE_MODS`. Remove the Drop ids of 2.1 and the Cornfield from the
   fork. Then backport what 1.3.3 lacks (2.7.1). For that 2.6.6 code and data only, NeoForge 1.21.1 -> Forge 1.19.2: data components (dynamic food, feast state,
   containers) -> NBT; `DeferredHolder` -> `RegistryObject`; `ItemInteractionResult` / `useItemOn` -> 1.19.2 `use`;
   menus, capabilities (`WellFluidCapability`, fluid tanks) -> Forge caps; data paths `recipe` -> `recipes`,
   `loot_table` -> `loot_tables`, `tags/item` -> `tags/items`; result `id` -> `item`; `neoforge:compound` ->
   array ingredients; `c:` -> `forge:`; NeoForge biome modifiers -> Forge; `Material` on blocks; drop advancements.
   Drop every Drop id of 2.1 with its assets, recipes, loot, tags, lang. `mod_sides.md` row (both sides).
2. **Dumps and generators** (memory `dumpall-refresh-workflow`): install, `/dumpall`, `/cdevgen` (names, food catalog, JEI
   coverage, stack sizes, physics, WTHIT icons, crop table, paintings).
3. **Ids and tags:** `item_dict.js` (and block / fluid dicts) keys for every kept id; tags through `global.tags` in a CD
   `extradelight/tags.js`: crops and seeds (`#forge:crops/chili|ginger|soybean|mallow_root`, `#forge:seeds/*`), fruit
   (`#forge:fruits/lime|grapefruit`), nuts (`#forge:nuts/hazelnut`), **new** `#forge:sausage`, `/raw`, `/cooked` on H&H's items
   (`#forge:butter` already holds H&H's butter),
   cut tags per D13, storage blocks, saplings / leaves / logs (`#minecraft:logs_that_burn`, `#minecraft:saplings`),
   Serene Seasons crop tags.
4. **Recipe retargets** (port data or CD `extradelight/recipes.js` via `global.NCH`): every kept recipe that named a
   dropped item takes the survivor (2.2 list). Upstream removals: the 13 FD `neoforge:false` overrides (pack baking
   overhaul stands), Butchercraft grinder, TAN tags, blocked woods (2.4).
5. **Mob and chest loot** ([loot.md](loot.md)): no mob loot. The 21 chest GLMs either go into the pack loot lists
   (D14) or are dropped; Cornfield chest tables follow D4. Evaporator loot dropped with it.
6. **Item merges:** none for the port route. Native route (D1): every Drop row becomes a `global.addItemMerges` pair in
   the `creativedelight` group plus a JEI hide.
7. **Potions** ([potions.md](potions.md)): none (ED has no potions). Pickled / Pucker Up / Sunshine potions are not
   added (scope rule).
8. **Alcohol:** none (ED's drinks are non-alcoholic). Ginger beer stays non-alcoholic.
9. **Machines:** vat recipes -> Vintage fermenting jar (pickle station per H&H's brine eval, N26-1); mortar -> `foodGrinding`;
   evaporator -> Vintage; juicer -> Vinery Apple Press through `R.vineryPressing` (D12); melting pot, keg per D6 / D11; kept stations (chiller, mixing bowl, drying rack, dough shaping) ported as is with
   JEI categories. Create mixing mirrors (209) kept where they make kept items.
10. **Recipes by pack rules** through `global.NCH` ([recipes_cooking.md](recipes_cooking.md)): the 525 oven recipes ->
    `creativeengineering:baking` with pack bakeware (D3), pack bases (cake base, pie crust, frosting, cookie dough),
    muffins on paper; heatless `_manual_only` for pot meals; roll and sandwich rules; Slice & Dice and Central Kitchen
    mirrors; container remainders (bowls, bottles, buckets, pans not consumed).
11. **Effects** ([effects.md](effects.md), JEED): Pickled and Pucker Up stay in the fork (licence, 2.7.2), an exception to
    the rule that effects live in Creative Common (memory `creative-delight-routing`), unless Common writes its own from
    behaviour; check the effects table generator picks them up. Sunshine dropped (D12).
12. **Food types and values** (`food_types.json`, [food.md](food.md)): about 460 edible items need a type (the pack has
    1030 today); food families (`tools/food_families.py --check`: cheesecake, custard, ice cream, milkshake, popsicle,
    cookie, candy, jelly, pickle, chocolate families); stack sizes ([stack_sizes.md](stack_sizes.md)); cans and jars
    ([cans.md](cans.md)).
13. **Compost** ([composting.md](composting.md)): new crops, pips, husks, leaves, saplings, rinds, peels.
14. **Crops, wild crops, seasons** ([crops.md](crops.md)): chili, ginger, mallow root, soybean soil and harvest rules,
    WTHIT growth line, hoe harvest; wild patches through `wild_crops.json` + `tools/gen_crop_worldgen_table.py`; ED biome
    modifiers removed.
15. **Trees and woods:** lime, grapefruit, hazelnut, cinnamon trees through the pack's tree worldgen (biome fits, the
    Fruits Delight tree rules), fruit and cinnamon wood sets (Every Compat, `#minecraft:planks`, boats none upstream),
    leaf decay range (memory `leaf-decay-range`).
16. **Names and lang** ([names.md](names.md)): clashes "Whisk" (Create), "Oven", "Keg", "Jar", "Sink", "Half Cabinet",
    "Muffin Tin", "Salt", "Cheese", "Toast"; spell-outs ("Smore" is dropped; "SOS" keep the upstream name or spell it
    out: names.md rule); en_us only.
17. **Mob and block audit:** BlockRunner tier for new floor blocks (chocolate, jelly, candy cane, gingerbread, butter),
    mineable tags, Item Physic (sinks / floats), WTHIT lines for crops, stations and feasts.
18. **Tooltips** ([tooltips.md](tooltips.md)) and **JEI** ([jei_coverage.md](jei_coverage.md)): ED's "Shift for more"
    drink tooltips reworded to the pack style; JEI categories of kept stations; dynamic recipes hidden with D9.
19. **Placeables:** 54 feast blocks, 24 pie blocks, 4 fondues ([placeable_food.md](placeable_food.md)); Display Delight
    plates for kept dishes if wanted.
20. **Kitchen furniture and decor** per D11 ([kitchen.md](kitchen.md)).
21. **Cornfield dimension** per D4 (dropped by default: no dimension, no maze, no structures, no portal code).
22. **Trades and chests:** villager / wanderer code dropped (D14); kept crops into the trader catalog if wanted
    ([wandering_trader.md](wandering_trader.md)).
23. **Paintings and banner:** 4 paintings into the paintings catalog (`/cdevgen` paintings), citrus banner pattern
    through the banner compat fix (memory `banner-pattern-compat-fix`).
24. **Patchouli cookbook:** dropped (it documents ED's own stations and recipes, which the pack replaces).
25. **Config:** `configs_edited/` copy of the port's config (mint spread off since mint is dropped; all-year spooky off).
26. **Release:** `mod_sides.md` row, changelog bullets per family (only when built), TODO row, plan row.

Order: 0 and 1 before everything; H&H's port is in, so step 3 can add the sausage tags at once; H&H's brine eval before the
pickle part of step 9; 2 after 1 and again after 3-4; 3 before
4 and 9-15; 9 before 10; 12 after 10; 14-15 after 2 (crop table and worldgen dumps); 26 last.

Routing, **changed by the licence (2.7.2)**: this overrides memory `creative-delight-routing` for ED. Anything transcribed
from ED (recipe lists, food values, names, tooltips) and all engine code go in the CC BY-NC-SA fork, including its own
`kubejs_startup/extradelight/` and server script folders. The GPLv3 CD trees
(`development/creativeDelight/src/main/resources/kubejs_startup/extradelight/`, `kubejs/data/creativedelight/kubejs/extradelight/`)
hold only what the pack defines itself: tags, removals, and recipes designed to pack rules. ED has no alcohol, so nothing goes
to the alcohol scripts.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | **Changed 2026-11-08 (2.7.1).** Route: native 1.3.3 untouched, a port of all of 2.6.6, or a fork of upstream's `1.19.2` source with the missing lines backported from 2.6.6? | fork and backport | **Answered 2026-11-08: approved.** Route is the fork |
| D2 | **Changed 2026-11-08 (2.7.2).** Licence CC BY-NC-SA 4.0: OK to ship the fork under that licence, keep ED-derived code and data out of the GPLv3 trees, give `global.NCH` a one-line permission for the fork, and keep the pack and any server free of paid access or cash shops? | yes to all; the unmodified upstream jar never goes in the payload | **Answered 2026-11-08: approved** (licence boundary and the `global.NCH` permission for the fork; owner states the pack and its servers will never be monetised) |
| D3 | Stations: ED oven, pans, mortar, mixing bowl, chiller | drop ED oven and mortar (pack oven, `foodGrinding`); map pie dish -> pie tin, muffin tray -> muffin tin, sheet and tray -> baking sheet, square pan -> cake pan; loaf pan, baking stone, bar mold: keep as new bakeware or map to the cake pan / baking sheet; keep the chiller; mixing bowl: keep (manual mixing with spoons has no pack equivalent) | as recommended, mixing bowl kept. **(rev 11-09, R1)** every station registered for eval, recipes duplicated onto the pack station and kept on ED's; see 3.3 |
| D4 | Corn and the Cornfield dimension | drop ED corn (Corn Delight's) and the Cornfield (dimension, maze, 23 structures, Halloween portal); H&H Corn Maze is the stage-2 maze. Keep corn husk, silk, dried husk, doll and pipe as Corn Delight byproducts | drop |
| D5 | Trees: apple, lemon, orange duplicate Fruits Delight | drop those 3; keep lime, grapefruit, hazelnut, cinnamon and the fruit wood (lime and grapefruit grow on it) | as recommended |
| D6 | Chocolate and candy: ED's 4 chocolates vs Create / Confectionery; blood chocolate needs `c:blood`; melting pot; marshmallow; gingerbread | chocolate base is Create / Confectionery (milk, dark = black, white); ED's chips, truffles, dipped, filled bars and fondues kept on those bases; blood chocolate dropped (no blood item) or fed by Alex's Mobs blood sac if wanted; melting pot dropped (Create mixer and Confectionery fluids); shaped cookies kept; plain gingerbread and canes dropped | as recommended, blood chocolate dropped. **(rev 11-09, R1)** the melting pot is registered; its recipes are also written for the Create mixer |
| D7 | Ice cream and milkshakes vs Neapolitan | drop ED chocolate and mint duplicates and the plain milkshake (Neapolitan vanilla); keep the other flavours | as recommended |
| D8 | Coffee and tea | drop ED's coffee crop, beans, drink and tea; ED coffee dishes (dalgona, affogato, coffee jelly, chocolate-dipped coffee beans) take Respite beans; Ground Coffee kept only if a recipe needs it after retargets. **(rev 11-09, R2)** Ground Coffee is kept outright: item, its grind recipe (`#forge:coffee_beans` on the mortar and on `foodGrinding`) and every recipe that uses it | as recommended |
| D9 | Dynamic jam and toast (one id, ingredient components) vs the jam family (one id per fruit, mason jar) | drop both dynamic items; ED jam fluid dropped | drop |
| D10 | Butter and cheese | **Changed 2026-11-08:** H&H's butter is the pack's (N22); ED's butter item dropped; ED's butter block, slab and stairs kept and crafted from it; ED cheese item dropped; cheese block, slab, stairs: keep as decor on Vintage cheese or drop | butter as recommended, cheese decor dropped |
| D11 | Furniture and decor: half cabinets (ED and H&H), knife block, step stool, picnic basket, wreaths, bowls, chocolate box, gingham, ribbons, 208 wallpapers; ED keg | **Changed 2026-11-08:** H&H's half cabinets stay (9 woods); ED's cinnamon and fruit variants wait with N26-7 (no other woods for now); step stools dropped (the pack has its own); drop sinks, counters, spice racks, food displays, keg; keep the other small decor; wallpapers: owner (208 ids for one look) | wallpapers and cinnamon / fruit half cabinets dropped, the rest as recommended. **(rev 11-09, R3)** nothing dropped: all furniture and decor registered for eval, see 3.4 |
| D12 | Juicer (ED) vs stomping basin (H&H); Sunshine (ED) vs Clarity (H&H) | **Changed 2026-11-08:** drop ED's juicer; lime, grapefruit, tomato, cactus and pickle juice go on the Vinery Apple Press like H&H's juices (loses the juicer's dye and bone meal byproducts); basin eval stays H&H's (D12 there). Drop Sunshine; ED foods that gave it give whichever of Clarity and Brightening wins the N26-9 eval | as recommended. **(rev 11-09, R1)** the ED juicer is registered and keeps its recipes; the Apple Press gets copies without the dye and bone meal byproducts. |
| D13 | Butchercraft-dependent dishes (B5) | fill the tags from pack items where a cut clearly fits (sausage -> H&H sausage, tags to be added; chicken breast / thigh / wing / cubed -> FD chicken cuts; beef cubed / scrap / ground -> FD minced beef; roasts -> raw beef / porkchop / mutton; ribs -> Alex's Mobs moose ribs only for BBQ ribs); drop the offal dishes (haggis, fried brains, liver and onions, stuffed heart, oxtail soup), fat potatoes and salami (`c:fat`) unless Carnivore (`mod-additions #12`) brings offal | as recommended |
| D14 | Trades and chest GLMs | drop the trade code; chest additions: a few ED foods per structure through loot.md, not the 21 GLMs as written | as recommended |
| D15 | Raisins (H&H) vs dried fruit (ED); trail mix | keep both raisins and dried fruit (dried fruit is the drying rack's generic output); trail mix: H&H's, on raisins | as recommended |
| D16 | Build order for stage 2 | H&H port first (installed), then ED; ED registers no id H&H owns (2.5, 2.6) | as recommended |
| D17 | ED's Pot of Mac and Cheese feast block, now that H&H owns the dish | drop it (H&H's dish is a bowl); or keep the pot and serve H&H's bowl from it | drop |
| D18 | Chili powder: Barbeque's item has no recipe in the dump | check in game, then add ED chili as its source through `foodGrinding` if it has none | as recommended |
| D19 | Near duplicates of H&H and FD: ED Nut Butter Cookie vs H&H Peanut Butter Cookie; ED pasta shapes vs FD raw pasta | drop ED's cookie and use H&H's; keep macaroni, penne and lasagna noodles as shapes, use FD raw pasta where a dish names no shape | as recommended |
| D20 | The 52 ids only in 1.3.3 (2.7.1): upstream later removed or renamed them | drop by default, so the fork matches 2.6.6's list; the jam items follow D9, the renamed tools take 2.6.6's ids; rabbit, goat and rice dishes can be kept by name if wanted | drop |

### 5.1 H&H decisions answered by this pass

| H&H # | Was | Answer from ED |
|---|---|---|
| D4 (sap and syrup) | decide with ED | ED has no sap, syrup or tapper: H&H's recommendation stands |
| D7 (campfire roasting, charred marshmallow) | decide with ED | ED has no roasting mechanic; ED mallow root is a new marshmallow source; H&H's recommendation stands |
| D12 (cask and stomping basin) | decide with ED | ED has no aging: the cask is judged on its own (keep if wanted); stomping basin vs ED juicer: D12 above |
| 2.6 placeholders | open | all settled in 2.5 |

## 6. File map (planned)

| Path | What |
|---|---|
| `development/backportedExtraDelight/` | the fork (Java, assets, data and scripts of kept content, including transcribed recipes and food values), CC BY-NC-SA 4.0 licence file and attribution |
| `development/creativeDelight/src/main/resources/kubejs_startup/extradelight/` | pack-defined glue only: tags (CD startup) |
| `kubejs/data/creativedelight/kubejs/extradelight/` | pack-defined recipes and removals only (CD server) |
| `development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js` | `global.item` keys for kept ids (`:creativeCommon:` rebuild) |
| `kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json` | wild chili, ginger, mallow, soybean |
| `mod_sides.md`, `configs_edited/root_config/extradelight-*.toml` | sides row, config backup |

## 7. Offline checks (once built)

- `./gradlew :backportedExtraDelight:build --offline` clean; the jar holds no dropped id (`unzip -l | grep` per Drop row)
  and no H&H-owned id (2.5).
- Grep the port's data for `c:` tags, `"id":` results, `neoforge:`, `recipe/`, `loot_table/` (1.21 leftovers): none.
- Tag scan (the B5 script): no kept recipe has a slot with only empty tags.
- `python3 tools/food_families.py --check`, `python3 tools/changelog_lint.py`.
- `latest.log` after first boot: no `Failed to parse`, no `extradelight` errors.

## 8. In-game test suite (drafted)

| ID | Test | Pass |
|---|---|---|
| EXD-01 | Boot with the port and the H&H port: no ED errors in `latest.log`; JEI shows only kept items | |
| EXD-02 | None of the 2.1 Drop ids exists (`/give` fails), and no id is registered by both ports | |
| EXD-03 | Chili, ginger, mallow root, soybean grow, drop seeds / cuttings, harvest by hoe area | |
| EXD-04 | Wild chili, ginger, mallow, soybean appear in their biomes (crop table) | |
| EXD-05 | Lime, grapefruit, hazelnut, cinnamon trees generate, drop fruit, saplings grow; fruit and cinnamon wood sets craft | |
| EXD-06 | Former ED oven recipes bake in the pack oven with the mapped bakeware (pie, loaf, cake, cookie samples) | |
| EXD-07 | Kept ED bakeware (per D3) is used and returned, never consumed | |
| EXD-08 | Mixing bowl: needs the set number of stirs with a spoon; JEI page | |
| EXD-09 | Chiller makes ice cream, jelly, popsicles; JEI page | |
| EXD-10 | Drying rack dries fruit, chili, corn husk (JEI page matches) | |
| EXD-11 | Dough shaping makes the shaped gingerbread and sugar cookies and pasta shapes | |
| EXD-12 | Juicer (D12) presses citrus, berries, melon, tomato; fills bottles | |
| EXD-13 | Former vat recipes (pickled fish, ginger, rinds, sausage; sauerkraut; natto; miso) ferment in the Vintage jar | |
| EXD-14 | Former mortar recipes grind on the crushing wheels, Millstone and the pack mortar | |
| EXD-15 | Feast blocks place, serve with the right container / spatula, and give the bowl back | |
| EXD-16 | Pie blocks (11 kinds) place, slice and eat | |
| EXD-17 | Butter: block, slab, stairs craft back to butter; `#forge:butter` holds ED butter only; H&H butter recipes take it | |
| EXD-18 | Sausage dishes (currywurst, sausage roll...) take H&H sausage | |
| EXD-19 | D13 cut-tag dishes craft from FD cuts; dropped offal dishes don't exist | |
| EXD-20 | Chocolate lines (chips, truffles, dipped) craft from Create / Confectionery chocolate | |
| EXD-21 | Ice creams and milkshakes: no flavour duplicated with Neapolitan | |
| EXD-22 | ED coffee dishes take Farmer's Respite beans | |
| EXD-23 | Pickled and Pucker Up effects work (poison ticks negated by chance; field of view narrows) and JEED text shows | |
| EXD-24 | Clarity from former Sunshine foods clears Blindness and Darkness | |
| EXD-25 | Eating a melon slice gives a melon rind | |
| EXD-26 | Dense corn in October gives no Nausea portal (Cornfield dropped, D4) | |
| EXD-27 | Food values and types: a sample of 10 ED foods shows the pack tuning values, AppleSkin agrees | |
| EXD-28 | Decor: half cabinets (13 woods) store items; gingham, ribbons, picnic baskets, wreaths place | |
| EXD-29 | Kept chest additions appear in their structure chests; no dropped id in any chest | |
| EXD-30 | Names: no duplicate display names with Create (whisk), the pack oven, Vintage cheese, More Delight toast | |
| EXD-31 | JEI: no ED butter, sunflower seeds, mac and cheese item, glazed carrot, corn husk or shared-wood half cabinet; H&H's are the only ones | |
| EXD-32 | ED recipe that needs butter accepts H&H's butter | |
| EXD-33 | Currywurst and Sausage Roll craft with H&H cooked or raw sausage | |
| EXD-34 | Lime juice comes from the Vinery Apple Press; no ED juicer in JEI | |

## 9. History

- 2026-11-09: owner rulings R1 to R5 (top of file): all workstations registered for eval with recipes also kept on the original
  station, Ground Coffee kept, all furniture and decor registered for eval, Cornfield dimension / maze / structures / portal
  defined in 3.7, texture comparison plan (3.8, extra_delight_textures.md). Nothing built.

- 2026-10-30: investigation written (overlap, stage-2 comparison with Hearth and Harvest, integration process),
  nothing built. Jar unpacked to `DecompiledMods/extradelight-2.6.6/` and decompiled to `-src/`. Found that upstream
  has a native Forge 1.19.2 line (1.3.3) and that the licence is CC BY-NC-SA 4.0; D1 and D2 block building.
- 2026-11-08: re-scan on top of the existing Delight mods and the installed H&H port (2.6): 6 survivors reversed to H&H, about
  25 more ids overlap, sausage tags found missing, D10 to D12 changed, D17 to D19 added. Nothing built.
- 2026-11-08: second pass with the 1.3.3 jar and the CurseForge licence (2.7): route changed to a fork of upstream's
  `1.19.2` source plus a backport of the missing lines; licence boundary keeps ED code and data out of the GPLv3 trees;
  D1 and D2 rewritten, D20 added. Nothing built.
- 2026-11-08: owner approved D1 (fork and backport) and D2 (licence boundary, `global.NCH` permission for the fork, no
  monetisation ever). Building is unblocked; remaining decisions D3 to D20 keep their defaults until answered.
