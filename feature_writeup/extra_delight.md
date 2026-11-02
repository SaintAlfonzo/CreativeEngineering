# Extra Delight: overlap and integration report

Survey for TODO `largecontent/mod-additions #11`. Written 2026-10-30, **nothing built, nothing installed**. Jar:
`devmods/toBackport/later_large_content_overlap/extradelight-2.6.6.jar`, unpacked to `DecompiledMods/extradelight-2.6.6/`,
decompiled to `DecompiledMods/extradelight-2.6.6-src/` (349 files). Licence **CC BY-NC-SA 4.0** (not GPL-compatible,
section 0 and D2). The jar is NeoForge 1.21.1 (the TODO row said Forge 1.20.1), so using 2.6.6 needs a **port**
(skill `port`). Upstream also has a **native Forge 1.19.2 line, last release 1.3.3 (2024-07-10)**, which is not on disk
(D1). Short name in this file: **ED**. Test prefix: `EXD-`.

Stage 2 of the large content addition: ED is merged with **Hearth and Harvest** ([hearth_and_harvest.md](hearth_and_harvest.md),
short name H&H). Section 2.5 compares the two item by item and settles H&H's 2.6 placeholders. H&H decisions that waited for this pass are
answered in 5.1.

Scope rule: integration adds no recipe or mechanic that is not in the mod or defined by a pack rule unless the owner
asks by name; upstream bugs are fixed.

## 0. At a glance

| Part | Where | Status |
|---|---|---|
| Inventory: 1490 named ids (668 item, 822 block lang keys; 1441 item models), 35 fluids, 3 effects, 0 potions, 0 entities, 1 dimension + biome + 23 structures, 7 fruit / spice trees, 2 woods, 10 crops + 11 wild crops, about 2470 recipe files, 21 chest GLMs, 2160 advancements | 1 | read from the jar |
| Of the 1490: about 695 foods and ingredients, 233 furniture / display, 208 wallpapers, 99 candy and chocolate decor, 73 fluid blocks and buckets, 58 storage blocks, 51 stations and tools, 49 wood / tree, 24 crops | 1 | regex classification, see Facts |
| Overlap: same item in an installed mod | 2.1 | about **120** ids listed by name (food, crops, crates, stations) |
| Overlap: same system (oven and bakeware, mortar, salt and evaporator, keg, jar, pickling, coffee, chocolate, ice cream, citrus trees, corn) | 2.3 | 18 systems |
| Overlap with Hearth and Harvest | 2.5 | 41 shared or competing rows; H&H 2.6 placeholders settled |
| Dead in the pack: recipes whose slot only Butchercraft-style tags fill (sausage, offal, cuts) | 3.6 B5 | 33 recipe files, about 27 dishes |
| New to the pack | 3 | about **560** ids (most of the food, the decor, 4 crops, 4 trees) |
| Upstream issues | 3.6 | B1-B8 (never loaded in the pack, no log) |
| Fold-in | 4 | 26 steps |
| Decisions | 5 | D1-D16; H&H D4, D7, D12 answered in 5.1 |
| Tests | 8 | EXD-01..EXD-30 drafted |

- **Jar:** `extradelight` 2.6.6 (its `neoforge.mods.toml` says 2.6.5, B1), authors Lance5057 and nine others, "A new more
  involved way to craft". Patchouli cookbook in `data/extradelight/patchouli_books/`.
- **Licence:** CC BY-NC-SA 4.0 (toml, CurseForge and Modrinth agree). Fork allowed, but the fork must stay CC BY-NC-SA
  with attribution, can't become GPLv3, and must stay non-commercial. CurseForge page rule: don't rehost the jar,
  no pay-to-play servers. Port route: its own in-tree jar under that licence, **not** inside the GPLv3 Creative Delight
  bundle (D2).
- **Dependencies:** NeoForge 21+, Minecraft 1.21, Farmer's Delight 1.2.8+ (required). Optional by `mod_loaded`: Create
  (209 mixing recipes), Butchercraft (1). Tag-only compat: Brewin' and Chewin', Corn Delight, Cultural Delights, Pizza
  Delight, The Salt, Serene Seasons, Tough As Nails.
- **Pack references today:** none in code. Docs: TODO rows `mod-additions #10` / `#11`, `farming/crops #8` (watering
  can held for "Bountiful Harvest or Extra Delight"), H&H report 2.5 / 2.6.
- **Already live:** nothing. No old world holds ED ids, so anything dropped at port time needs no item merge.
- **Dumps:** `kubejs/exported/items_by_mod/` is from 2026-10-24, `display_names.json` from 2026-10-20 (the overlap
  search also read in-tree `development/**/lang/en_us.json`). ED isn't installed, so nothing in the dumps needs refreshing yet.

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
| Sunflower Seeds | H&H sunflower seeds | same item, both new to the pack | one survivor, 2.5 |
| Cactus (food item `extradelight:cactus`), Cooked Cactus | `delightful:cactus_flesh`, `cactus_steak` | same food | Drop; Delightful's |
| Salt | `vintagedelight:salt_dust` (pack salt, item_merges 2.9), The Salt, H&H salt | fifth salt | Drop; `#forge:salts` |
| Flour | `create:wheat_flour`, `#forge:flour/wheat`; H&H flour (dropped) | same | Drop; `i.flour` |

**Dairy, eggs, condiments, ingredients**

| ED item | Overlaps with | Problem | Proposal |
|---|---|---|---|
| Butter (+ block, slab, stairs) | H&H butter (new to the pack too) | same item in both stage-2 mods | one butter, ED's (2.5, D10) |
| Cheese (+ block, slab, stairs) | Vintage cheese wheel / slice, Brewin' cheeses, `#forge:cheese`; H&H cheese (dropped) | another cheese | Drop the item; `#forge:cheese`. Blocks: D10 |
| Cooking Oil, oil fluid | `rusticdelight:cooking_oil`; H&H (dropped) | same | Drop; Rustic's, pack oil fluid if one exists (alcohol.md 4.1 fluid rule) |
| Vinegar, vinegar fluid, Vinegar Pot | `vintagedelight:vinegar_bottle` / `vinegar_jar` | same | Drop; Vintage's |
| Jar | `vintagedelight:empty_mason_jar`; H&H jar (dropped) | third food jar | Drop; mason jar (`dishes/jams #1`) |
| Nut Butter (bottle, fluid), Chocolate Nut Butter Spread | Vintage nut mash, Delightful nut butter (merge planned, item_merges 5.1); H&H peanut butter (dropped) | same spread | Drop nut butter; the nut mash survivor. Chocolate spread is new (needs hazelnut, keep) |
| Apple Sauce | `vintagedelight:apple_sauce_bottle` / `jar` | same | Drop |
| Kimchi | `vintagedelight:kimchi`, `brewinandchewin:kimchi` | third kimchi | Drop |
| Pickled Onion, Beet, Egg, Cucumber (gherkin) and their jars | Vintage pickled onion, beetroot, egg, pickle | same pickles | Drop; Vintage's |
| Pickled Carrot (+ jar) | H&H pickled carrots (new to the pack) | same in both stage-2 mods | one survivor, 2.5 |
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
| Glow Berry Juice, Sweet Berry Juice | H&H juices (both new) | same in both stage-2 mods | one survivor, 2.5 |
| Chocolate Milk | Neapolitan chocolate milkshake; H&H chocolate milk (H&H D5: drop) | similar | Drop (follows H&H D5) |
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
| Half Cabinet (13 woods) | **H&H half cabinets (11 woods)**, FD cabinets | same block in both stage-2 mods | one family, 2.5 / D11 |
| Spice Rack (13 woods, empty and full) | `cookingforblockheads:spice_rack` | same | Drop |
| Food Display (13 woods) | Display Delight plates, Supplementaries item shelf | same job | Drop |
| Crates: Apple, Golden Apple, Golden Carrot, Sweet Berry, Glow Berry, Red / Brown Mushroom | Quark crates and sacks, Fungi Delight mushroom crates, Fruits Delight apple crate; the same H&H crates (dropped) | same block | Drop |
| Sacks: Cocoa Beans, Coffee Beans, Sugar, Flour, Cornmeal, Peanut (3) | Quark cocoa sack, Respite coffee sack, Create Compressed flour pile; Vintage peanut crate | same | Drop; keep sacks for new items only (chili powder, cinnamon, hazelnut, ginger, mallow powder, soybean, cocoa solids, breadcrumbs) |
| Corn Crate, Corn Sack, Corn Cob / Husk Bundles | Corn Delight crate and kernel bag; H&H corn husk bundle | same | Drop the crate and sack; husk bundle: 2.5 |
| Salt Block | The Salt `salt_block`, Vintage salt block | same | Drop |

### 2.2 Mob drops, dishes, ingredients

- ED replaces no mob loot. Its chest GLMs add food to 16 vanilla chest tables (4.5).
- Same food under another id: 2.1. All are "Drop at port".
- **Tag inputs nothing in the pack fills** (3.6 B5): `c:sausage` (+ raw / cooked), offal (`c:liver`, `heart`,
  `brain`, `lung`, `stomach`, `blood`, `offal`), cuts (`c:chicken/breast|thigh|wing|cubed`, `c:beef/cubed|scrap|ground|oxtail|roast`,
  `c:pork/roast|scrap/cooked`, `c:meat/ribs/raw`, `c:mutton/ribs|cooked`, `c:fat`). These come from Butchercraft, which
  isn't installed. H&H's sausage fills the sausage tags (2.5); FD cuts can fill the cut tags (D13).
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
| Juicer (11) | Vinery apple press, H&H stomping basin, Create | partly | one juicer for stage 2 (2.5, D12) |
| Melting pot (11, chocolate syrups) | Create Confectionery chocolate fluids, Create mixer | partly | D6 |
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
| Effects: Sunshine (lowers Darkness / Blindness) | H&H Clarity (immune to both) | **same job** | D12 |
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
| Butter | item | item + block, slab, stairs; `#c:butter` | 7 / 114 (tag) | **ED**; H&H recipes on `#forge:butter` |
| Half cabinets | 11 vanilla woods (+ cherry, bamboo, palm blocked) | 13 woods (11 vanilla + cinnamon + fruit) | - | **ED** (covers ED's own woods); H&H's dropped |
| Sunflower Seeds | from sunflowers | from sunflowers, 2 uses | 0 / 2 | **ED** |
| Trail Mix | raisins + nuts | dried fruit + nuts | - | **H&H** if raisins stay, else ED (D15) |
| Glow Berry Juice, Sweet Berry Juice | bottle + fluid | bottle + fluid | 6 / 2 each | **H&H** (more uses, wine chain) |
| Pickled Carrots | keg / jar | vat + jar block | - | **H&H**'s food, made in the Vintage jar (both machines dropped) |
| Macaroni and Cheese | dish | pot + bowl (feast) | - | **ED** (feast block, macaroni ingredient) |
| Glazed Carrots | dish | dish | - | **ED** (oven recipe already planned) |
| Corn Husk, Corn Husk Bundle | husk + bundle | husk, dried husk, bundles, doll | 5 / 4 | **ED** (dried husk and doll use it), as Corn Delight cutting outputs |
| Juice block | stomping basin (15) | juicer (11) | - | **one**: D12 |
| Keg | fermenting keg | 8-bucket tank | - | both dropped: Brewin' keg ferments, H&H jug stores |
| Jar | H&H jar | ED jar | 14 / 0 | both dropped: Vintage mason jar |
| Salt, salt block | yes | yes | - | both dropped |
| Crates (apple, golden apple, golden carrot, sweet / glow berry, mushrooms) | yes | yes | - | both dropped (Quark, Fruits Delight, Fungi) |
| Cooking oil, flour, cheese, cornmeal, corn, peanuts, roasted peanuts, popcorn, caramel apple, smore, chocolate cake, jerky, marshmallow, chocolate milk, sink, counter | yes | yes | - | both dropped (pack survivors in each report's 2.1) |
| Clarity / Sunshine | Clarity | Sunshine | - | **one**: D12 |
| Corn maze | Corn Maze structure (overworld) | Cornfield dimension (Halloween) | - | **one**: D4 |

**Complementary (one mod fills a gap in the other)**

| Need | From | Into |
|---|---|---|
| Sausage item for ED's 5 sausage dishes (currywurst, deviled sausages, pickled sausage, sauerkraut and sausage, sausage roll) | H&H Raw / Cooked Sausage | `#forge:sausage`, `#forge:sausage/raw`, `#forge:sausage/cooked` (the port's `c:` tags) |
| Gravy for H&H Biscuits and Gravy | ED gravy (item, fluid) | H&H recipe |
| Raisins for ED's dried fruit chain | H&H raisins | D15 |
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
  table above is the contract: neither port registers an id the other one owns.

## 3. New content

About 560 ids are new to the pack after section 2 (the rest drop). By line:

### 3.1 Crops, trees, woods

| Line | Ids | Notes |
|---|---|---|
| Chili | chili, chili seeds, dried chili, sliced chili, chili powder, hanging chili / dried chili, wild chili, chili crate, chili powder sack | dried on the drying rack |
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

### 3.3 Stations and tools (kept per D3)

Chiller, mixing bowl (wooden to netherite spoons), drying rack, dough shaping, juicer (D12), funnel and tap, grater,
whisk, offset spatulas (5 tiers, serve feasts), yeast pot, corn husk doll (decor; turns to face players in the
Cornfield only), corn cob pipe.

### 3.4 Decor

208 moulded wallpapers (13 woods x 16 colours), step stools, knife blocks, dried corn fences, picnic baskets (16),
wreaths (15), fruit and candy bowls, chocolate boxes (16), jar display, gingham (16 + 16 carpets), ribbon bows (16),
chocolate building blocks (4 kinds x 8 shapes), jelly blocks (16), candy cane blocks (3), frosted gingerbread blocks (16),
hanging produce (chili, corn, garlic, ham, mint, onions), 4 paintings, citrus rind banner pattern. D11.

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
| B5 | 33 recipe files (about 27 dishes) have a slot that only Butchercraft-style tags fill (`c:sausage*`, offal, cuts, `c:fat`); ED defines none of them, so these dishes are uncraftable without Butchercraft. Blood chocolate's whole line hangs on `c:blood` | tag scan of `data/*/recipe` against `data/*/tags` | D13 |
| B6 | Id typos: `lasanga`, `caramel_cheescake_slice` (display names are right) | lang | ids kept (port rule: keep upstream ids) |
| B7 | The Cornfield trigger reads the real date (1 October to 10 November), so it is active in normal play every autumn; today (2026-10-30) included | `CornTop.isHalloween`, `CornBottom` | follows D4 |
| B8 | 2160 recipe advancements (recipe book unlocks) | `data/*/advancement` | dropped by the pack |

No load errors: the jar was never loaded in the pack.

## 4. Fold-in, in build order

0. **Decide D1 and D2 first** (native 1.3.3 vs port of 2.6.6; licence and delivery). Get the 1.3.3 jar (owner
   download from CurseForge) and run `python3 tools/jar_diff.py extradelight-1.3.3.jar extradelight-2.6.6.jar`
   (method: [devmods_evaluation.md](devmods_evaluation.md) section 1). The steps below assume the recommendation:
   an in-tree port of 2.6.6 that registers only the kept content.
1. **Port** (skill `port`, [feature_porting.md](feature_porting.md)): `development/backportedExtraDelight/` (mod id
   `extradelight`, version `2.6.6-ce-19.2-01`), licence file CC BY-NC-SA 4.0 with attribution to Lance5057 et al., root
   `settings.gradle`, `IN_TREE_MODS`. NeoForge 1.21.1 -> Forge 1.19.2: data components (dynamic food, feast state,
   containers) -> NBT; `DeferredHolder` -> `RegistryObject`; `ItemInteractionResult` / `useItemOn` -> 1.19.2 `use`;
   menus, capabilities (`WellFluidCapability`, fluid tanks) -> Forge caps; data paths `recipe` -> `recipes`,
   `loot_table` -> `loot_tables`, `tags/item` -> `tags/items`; result `id` -> `item`; `neoforge:compound` ->
   array ingredients; `c:` -> `forge:`; NeoForge biome modifiers -> Forge; `Material` on blocks; drop advancements.
   Drop every Drop id of 2.1 with its assets, recipes, loot, tags, lang. `mod_sides.md` row (both sides).
2. **Dumps and generators** (memory `dumpall-refresh-workflow`): install, `/dumpall`, `/cegen` (names, food catalog, JEI
   coverage, stack sizes, physics, WTHIT icons, crop table, paintings).
3. **Ids and tags:** `item_dict.js` (and block / fluid dicts) keys for every kept id; tags through `global.tags` in a CD
   `extradelight/tags.js`: crops and seeds (`#forge:crops/chili|ginger|soybean|mallow_root`, `#forge:seeds/*`), fruit
   (`#forge:fruits/lime|grapefruit`), nuts (`#forge:nuts/hazelnut`), `#forge:butter`, `#forge:sausage*` (H&H items),
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
9. **Machines:** vat recipes -> Vintage fermenting jar; mortar -> `foodGrinding`; evaporator -> Vintage; melting pot,
   juicer, keg per D6 / D12 / D11; kept stations (chiller, mixing bowl, drying rack, dough shaping) ported as is with
   JEI categories. Create mixing mirrors (209) kept where they make kept items.
10. **Recipes by pack rules** through `global.NCH` ([recipes_cooking.md](recipes_cooking.md)): the 525 oven recipes ->
    `creativeengineering:baking` with pack bakeware (D3), pack bases (cake base, pie crust, frosting, cookie dough),
    muffins on paper; heatless `_manual_only` for pot meals; roll and sandwich rules; Slice & Dice and Central Kitchen
    mirrors; container remainders (bowls, bottles, buckets, pans not consumed).
11. **Effects** ([effects.md](effects.md), JEED): Pickled, Pucker Up, and the D12 survivor; check the effects table
    generator picks them up.
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
23. **Paintings and banner:** 4 paintings into the paintings catalog (`/cegen` paintings), citrus banner pattern
    through the banner compat fix (memory `banner-pattern-compat-fix`).
24. **Patchouli cookbook:** dropped (it documents ED's own stations and recipes, which the pack replaces).
25. **Config:** `configs_edited/` copy of the port's config (mint spread off since mint is dropped; all-year spooky off).
26. **Release:** `mod_sides.md` row, changelog bullets per family (only when built), TODO row, plan row.

Order: 0 and 1 before everything; the H&H port before step 3 (sausage tags); 2 after 1 and again after 3-4; 3 before
4 and 9-15; 9 before 10; 12 after 10; 14-15 after 2 (crop table and worldgen dumps); 26 last.

Routing (memory `creative-delight-routing`): food recipes, tags, values and cooking rules go in a CD tree
`development/creativeDelight/src/main/resources/kubejs_startup/extradelight/` (startup) and
`kubejs/data/creativedelight/kubejs/extradelight/` (server). Those are pack scripts (GPLv3), not ED code. Engine code
(stations, blocks, feasts) stays in the CC BY-NC-SA port jar. ED has no alcohol, so nothing goes to the alcohol scripts.

## 5. Decisions for the owner

| # | Question | Recommendation | Default if not answered |
|---|---|---|---|
| D1 | Route: native Extra Delight **1.3.3** (Forge 1.19.2, untouched, merges for every duplicate) or a **port of 2.6.6** registering only kept content? | get 1.3.3 and diff it first; if 1.3.3 lacks the citrus, fermentation and soy lines, port 2.6.6 (new content only), the same shape as H&H | **blocks building**: no route until the diff |
| D2 | Licence CC BY-NC-SA 4.0: OK to ship a port jar under that licence (separate from the GPLv3 jars), and to keep servers non-commercial? The deploy payload shouldn't rehost the upstream jar (deploy.md Vinery note) | yes: own jar, licence file and attribution, never inside the CD bundle; a published pack downloads the native jar if D1 goes native | blocks building |
| D3 | Stations: ED oven, pans, mortar, mixing bowl, chiller | drop ED oven and mortar (pack oven, `foodGrinding`); map pie dish -> pie tin, muffin tray -> muffin tin, sheet and tray -> baking sheet, square pan -> cake pan; loaf pan, baking stone, bar mold: keep as new bakeware or map to the cake pan / baking sheet; keep the chiller; mixing bowl: keep (manual mixing with spoons has no pack equivalent) | as recommended, mixing bowl kept |
| D4 | Corn and the Cornfield dimension | drop ED corn (Corn Delight's) and the Cornfield (dimension, maze, 23 structures, Halloween portal); H&H Corn Maze is the stage-2 maze. Keep corn husk, silk, dried husk, doll and pipe as Corn Delight byproducts | drop |
| D5 | Trees: apple, lemon, orange duplicate Fruits Delight | drop those 3; keep lime, grapefruit, hazelnut, cinnamon and the fruit wood (lime and grapefruit grow on it) | as recommended |
| D6 | Chocolate and candy: ED's 4 chocolates vs Create / Confectionery; blood chocolate needs `c:blood`; melting pot; marshmallow; gingerbread | chocolate base is Create / Confectionery (milk, dark = black, white); ED's chips, truffles, dipped, filled bars and fondues kept on those bases; blood chocolate dropped (no blood item) or fed by Alex's Mobs blood sac if wanted; melting pot dropped (Create mixer and Confectionery fluids); shaped cookies kept; plain gingerbread and canes dropped | as recommended, blood chocolate dropped |
| D7 | Ice cream and milkshakes vs Neapolitan | drop ED chocolate and mint duplicates and the plain milkshake (Neapolitan vanilla); keep the other flavours | as recommended |
| D8 | Coffee and tea | drop ED's coffee crop, beans, drink and tea; ED coffee dishes (dalgona, affogato, coffee jelly, chocolate-dipped coffee beans) take Respite beans; Ground Coffee kept only if a recipe needs it after retargets | as recommended |
| D9 | Dynamic jam and toast (one id, ingredient components) vs the jam family (one id per fruit, mason jar) | drop both dynamic items; ED jam fluid dropped | drop |
| D10 | Butter and cheese | ED butter is the stage-2 butter (block, slab, stairs kept); ED cheese item dropped; cheese block, slab, stairs: keep as decor on Vintage cheese or drop | butter as recommended, cheese decor dropped |
| D11 | Furniture and decor: half cabinets (ED and H&H), knife block, step stool, picnic basket, wreaths, bowls, chocolate box, gingham, ribbons, 208 wallpapers; ED keg | one half-cabinet family (ED's); drop sinks, counters, spice racks, food displays, keg; keep the small decor; wallpapers: owner (208 ids for one look) | wallpapers dropped, the rest as recommended |
| D12 | Juicer (ED) vs stomping basin (H&H); Sunshine (ED) vs Clarity (H&H) | juicer: ED's (a station with JEI, covers citrus); basin dropped unless the owner wants the stomping look. Effect: keep H&H **Clarity** (plain immunity, easier to describe); ED foods that gave Sunshine give Clarity | as recommended |
| D13 | Butchercraft-dependent dishes (B5) | fill the tags from pack items where a cut clearly fits (sausage -> H&H sausage; chicken breast / thigh / wing / cubed -> FD chicken cuts; beef cubed / scrap / ground -> FD minced beef; roasts -> raw beef / porkchop / mutton; ribs -> Alex's Mobs moose ribs only for BBQ ribs); drop the offal dishes (haggis, fried brains, liver and onions, stuffed heart, oxtail soup), fat potatoes and salami (`c:fat`) unless Carnivore (`mod-additions #12`) brings offal | as recommended |
| D14 | Trades and chest GLMs | drop the trade code; chest additions: a few ED foods per structure through loot.md, not the 21 GLMs as written | as recommended |
| D15 | Raisins (H&H) vs dried fruit (ED); trail mix | keep both raisins and dried fruit (dried fruit is the drying rack's generic output); trail mix: H&H's, on raisins | as recommended |
| D16 | Build order for stage 2 | H&H port first, then ED; neither registers an id the other owns (2.5) | as recommended |

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
| `development/backportedExtraDelight/` | the port (Java, assets, data of kept content), CC BY-NC-SA 4.0 licence file and attribution |
| `development/creativeDelight/src/main/resources/kubejs_startup/extradelight/` | items dict glue, food values, tags (CD startup) |
| `kubejs/data/creativedelight/kubejs/extradelight/` | recipes (NCH), oven retargets, removals (CD server) |
| `kubejs/startup_scripts/_lib/item_dict.js` | `global.item` keys for kept ids |
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

## 9. History

- 2026-10-30: investigation written (overlap, stage-2 comparison with Hearth and Harvest, integration process),
  nothing built. Jar unpacked to `DecompiledMods/extradelight-2.6.6/` and decompiled to `-src/`. Found that upstream
  has a native Forge 1.19.2 line (1.3.3) and that the licence is CC BY-NC-SA 4.0; D1 and D2 block building.
