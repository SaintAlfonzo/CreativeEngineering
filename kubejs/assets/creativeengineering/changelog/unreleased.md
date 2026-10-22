# Unreleased

All changes since 1.0.9.

## Brewing & Drinks <!-- icon: brewinandchewin:keg -->

- **Changed:** All alcoholic drinks fill half a hunger shank, give no saturation and can be drunk when full. This
  covers all Brewin' and Chewin' drinks, all Vinery wines and Cider, the Bellini Cocktail and the Bottle of Wodka.
  - Wines, the Bellini and the Wodka block food saturation for 90 seconds, like Brewin' drinks.
  - Drinks that don't give Tipsy give 5 seconds of Nausea (10 for Jellie Wine and A Bottle of 'Mojang Noir').
  - Wine tooltips list these effects under the wine's own effect.
- **New:** Alcoholic drinks show *Intoxicating Drink* in their food tooltip.
- **Changed:** Wine storage speed depends on light and temperature. Dark: x2 (was x2.5). Cold: x2. Cold and dark:
  x4. Warm: x0.75.
  - Temperature is judged like a Keg's: biome plus nearby cooling and heating blocks.
  - Checked when the wine is stored. The tooltip shows the result ("In storage: cold cellar x4.0").
- **New:** Wineries generate in about half of plains, taiga, savanna and desert villages, with a Cask (Winemaker
  job) and some wine, fruit and juice. New chunks only.
- **Changed:** Grapevine Pot: holds 12 grapes, 4 stomps, 4 bottles of juice (was 6 grapes, 12 stomps, 2 bottles).
- **New:** Keg: containers left by ingredients go to a new slot right of the output instead of dropping, then to the
  empty container slot. The Keg pauses when both are full.
- **Changed:** Beer, Vodka, Dread Nog and Lilitu Wine stack to 32, like other Brewin' drinks.
- **Changed:** Vinery wines play the drinking sound (was the eating sound).
- **Changed:** Hoppers and pipes fill a Keg from any side. Ingredient slots accept only fermenting ingredients; the
  container slot accepts only drink containers.
  - Filled containers inserted from a side or the bottom go to the fluid slot.
- **Changed:** More Keg heat and cold sources. Heat: lit furnaces, smokers, blast furnaces, bonfires, braziers, and
  running machines such as the Coke Oven. Cold: snow, powder snow and more ice types.
  - Each source says so in its tooltip.
- **Fixed:** An unfuelled Blaze Burner warmed a Keg.
- **Fixed:** The Keg's tankard slot showed a black and purple square.
- **New:** WTHIT shows a Keg's temperature and progress ("Fermenting: Beer 45%"), or in red what the drink needs
  when the temperature is wrong.
- **Changed:** Coasters accept only food and drinks with a Coaster model. 56 more dishes have one (e.g. Hamburger,
  Bacon and Eggs).
  - Wines, juices and teas no longer fit. Tooltips say "Placeable on a coaster" where it applies.
  - WTHIT lists a Coaster's items ("Coaster: 3 / 4", then one line each).
- **Changed:** Wines are Small, Medium or Large bottles. Size sets bottles per placed block (3, 2, 1) and which
  racks accept it. The tooltip shows both.
  - Small: all wine racks and the Wine Box. Medium: Single and Small Wine Rack, Wine Box. Large: Single Wine Rack.
  - Stal Wine, Sun-Kissed Wine and Magnetic Wine are now Medium.
  - Medium wines render as full coloured bottles in the Wine Box and Small Wine Rack.
- **Changed:** Renamed: wine racks are Single, Small and Big Wine Rack (1, 4, 9 bottles); "Dark Cherry" blocks are
  "Cherry".
- **Fixed:** A Bottle of 'Creepers Crush', Eiswein, Chenet Wine, Aegis Wine, Mellohi Wine, Strad Wine and Cider
  placed one per block instead of two.
- **Fixed:** The Single Cherry Wine Rack had a solid door instead of glass.
- **Changed:** Sneak-right-click opens and closes a Wine Box with an item in hand. Hold sneak to place wines while
  flying.
- **Fixed:** Hoppers jammed the Cask and Apple Press with a second stack, mixed recipes or the wrong juice. Items now
  wait in the hopper until usable.
- **Changed:** Vinery's Window Block and Window renamed to Stained Glass and Stained Glass Pane. 6 Stained Glass
  craft 16 panes.
- **New:** Tooltips on the Grapevine Pot, Apple Press, Cask, Grapevine Stem, Grapevine Lattice, Stackable Log and
  fruit leaves and saplings.
- **Fixed:** Vinery's Grass Slab did not match nearby grass colour.
- **Fixed:** Kimchi, Pickled Pickles, Quiche and Quiche Slice could not be composted.
- **Removed:** Duplicate Vinery blocks (one of each kept). Placed copies are gone, with their contents:
  - Old fruit and grape crates (Apple Bag, Cherry Bag and grape bags stay) and the Crested Barrel.
  - Older wine racks, the old Cherry Cabinet and Drawer, the second set of cherry chairs, tables and shelves, and
    the wooden lattices (jungle grapes grow on the Grapevine Lattice).
  - The unused Stove, Wood Fired Oven and Kitchen Sink.

## Food & Cooking <!-- icon: farmersdelight:cooking_pot -->

- **New:** Jars hold mixed kinds of the same family: cookies, candied apples, candied berries, candies,
  marshmallows, chocolate bars, apples, berries, dried fruit. Each layer keeps its item; a click takes the top one.
  - Every cookie now fits in a jar, and so do the foods in those families.
- **New:** 19 cans: Corn, Coffee Berries, Chips, Grilled Corn, Tortilla Chips, Sweet Rolls, Creamed Corn,
  Cornbread Stuffing, Dumplings, both Creamy Pastas, Fig, Lemon and Sweet Berry Pie Filling, Maggots, Tomato Sauce,
  Pears in Rock Sugar and two cheeses.
  - Canned Flaxen Cheese and Canned Scarlet Cheese: Cooking Pot, 4 wedges, empty can in the bowl slot.
  - Canned Pears in Rock Sugar: 8 servings. Canned Coffee Berries: 2 seconds of Wither.
- **New:** Canned Cake: a Cake, or 4 Slices of Cake with Sugar or a Honey Bottle. 4 servings, each with a short
  Speed effect.
- **Changed:** Sliced meat and fish fill a can at the same count as the whole item. No recipe makes a half-full can.
  - Applies to Minced Beef, Raw Bacon, Raw Chicken Cuts, Raw Mutton Chops, Shulker Filet, raw fish slices, and
    Lemon and Orange slices.
- **Changed:** Raw fish cans: 4 hunger per serving (was 5). Fewer fish needed: 4 Pike, 5 Lobster Tails, 6 Lionfish,
  7 Calamari or Perch.
- **Changed:** Canned Pumpkin: 3 servings (was 2), from 4 Pumpkin Slices, or 3 with Sugar or a Honey Bottle.
- **Changed:** Cantaloupe Slice: 3 hunger, no Resistance (same as Pumpkin Slice). Canned Cantaloupe takes 4 slices.
- **Changed:** Glistering Melon Slice is edible: 4 hunger, 4.8 saturation, can be eaten when full (same as Golden
  Melon Slice), no effects.
- **Changed:** Canned Banana, Glow Berries, Salmonberries and Raw Chicken give their food's effects: Agility,
  Glowing, Water Breathing, and a chance of Hunger.
  - Canned Cheesy Pasta, Creamy Onion Soup, Kimchi and Pickled Pickles match the updated Brewin' foods.
- **Changed:** Cans without their own texture use one for their kind (cookies, bread, stew, pie, golden apples)
  instead of Canned Beetroots.
- **Fixed:** Uncraftable Canned Goods cans (Canned Golden Apples, Golden Carrots, Notch Apples, Apples, Suspicious
  Stew and others) refilled all saturation. They now fill like others of their kind.
  - Servings match their kind: 3 for fruit, 8 for stews, 10 for golden cans.
  - Canned Cookies and the golden apple cans no longer count as meat (wolves won't eat them).
- **Changed:** Pie filling can + Pie Crust into a pie needs a full can and works by hand only. Auto-crafters,
  including Create's mixer, no longer accept it. It also bakes in the Oven (pie tin), full cans only.
  - Pie fillings are made in the Cooking Pot: 2 fruit + 2 Sugar, an empty can in the bowl slot.
- **Changed:** Recipes that use or fill a can by the serving are hand-only. Create's mixer and Mechanical Crafters,
  the Immersive Engineering Assembler and AE2 assemblers no longer make them.
- **New:** Canned Dough: 8 Dough, 8 servings, not edible.
- **Changed:** Canned Shulker Flesh: 2 hunger per serving (was 5). Canned White Strawberries: 7 hunger per serving
  (was 5).
- **Removed:** Canned Food, the Hot Hot Hotpot and the Butcher's Canned Food trade.
- **New:** Mortar and Pestle (lever over polished granite) grinds food in a crafting grid, losing durability per use: Maize or Corn
  Kernels into Cornmeal, a Hay Bale or Wheat into Wheat Flour, Sugar Cane into 2 Sugar, and more.
  - Yields are the machines' average, no bonus chance: 2 Maize or Wild Corn = 3 Cornmeal, 3 Corn Kernels = 1,
    1 Hay Bale = 13 Wheat Flour, 1 Raw Rock Salt = 4 Salt.
  - Crafted in the crafting table. Old Guns' Mortar and Pestle is renamed Powder Pestle and is for gunpowder only.
- **New:** The Millstone grinds Maize, Corn Kernels, Corn Crates and Bags of Corn Kernel into Cornmeal, and Hay
  Bales into Wheat Flour (same as crushing wheels). Wild Corn grinds like Maize.
- **Changed:** A knife cuts Maize into 4 Corn Kernels (was 2).
- **Changed:** Raw Tortilla: 3 Cornmeal over a Water Bucket or Water Bottle (the container comes back). Also from
  Cornmeal by washing with a fan, or in the Bottler with 250 mB water, 1 each.
- **Changed:** Raw Rock Salt gives 4 Salt by hand, crushing and milling (was 1). Heated mixing turns water into Salt
  60% of the time (was 20%). A full cauldron evaporating gives a bonus Salt 50% of the time (was 25%).
- **New:** JEI Seasoning tab: salting foods, Create: The Salt mixes and other seasoning crafts move there from the
  Crafting and Mixing tabs.
- **Changed:** Baking rebuilt on shared bases. Every other cake, pie and cookie recipe is removed, including the
  vanilla Cake, Cookie and Pumpkin Pie crafts. Slices still craft back into a whole.
  - New: Frosting (Sugar + 2 milk = 2) and Cookie Dough (Dough + 2 Sugar), by hand.
  - Cake Base: 3 flour or cornmeal + 2 eggs + 2 Sugar, or 3 Dough + egg + 2 Sugar. Baked in the Oven, or baked
    directly from 2 flour or cornmeal (or 2 Dough) + eggs + 2 Sugar.
  - Pie Crust: 3 flour or cornmeal + egg + milk, or 3 Dough + milk, makes 3 (was 2 from 3 flour + milk).
  - No Create recipes for Cake Bases or Pie Crust.
- **Changed:** Cakes: Baked Cake Base + Frosting + 2 of the flavour by hand, or raw base + Frosting + 2 flavour in
  the Oven. Plain Cake: 3 Frosting, or 2 Frosting + a sweet berry, cranberry or cherry.
  Chocolate Cake also spouts: Baked Cake Base + 500 mB chocolate.
- **Changed:** Pies and cookies are Oven only. Pie: Pie Crust + 2 fruit + 2 Sugar or Honey Bottles, 1 per bake
  (Durian and Pineapple Pie were 2). Cookies: Cookie Dough + 2 of the flavour = 12 (was 2 flour + flavour = 8).
  - New: Cherry Cookies, Cookie Dough + 2 cherries.
  - Sweet Berry Pie: Pie Crust + 2 Sweet Berries + 2 Sugar (was 2 milk + crust + 6 berries).
  - Renamed: Sweet Berry Cheesecake to Sweet Berry Pie. It has a pie filling can like the other pies.
- **Changed:** Fish frying. Any raw fish + batter makes Raw Fish Tempura. The Cooking Pot fries with cooking oil:
  Fish Tempura, Fries (2 potato slices = 2) and Fish and Chips (fish + 2 potato slices, on paper).
  - Fish and Chips also by hand: Fries + cooked fish + Paper. Rustic Delight's potato slices count.
  - The Thermopneumatic Processing Plant's chips and tempura and the heated plant oil mix are removed.
  - Renamed: Salmon Tempura to Fish Tempura, Chips to Fries, Cod n Chips to Fish and Chips.
- **Changed:** Golden Carrot and Golden Apple in machines give 6 (mixer) and 5 (Arc Furnace) of the 8 a crafting
  grid gives (was 5 and 3). Rose Gold counts as gold.
- **New:** Dog Food recipe: Rotten Flesh, Bone Meal, 2 cooked meat and Cooked Rice, by hand.
- **New:** Vegetable Soup, Vegetable Noodles, Ratatouille and Corn Soup craft by hand from cooked ingredients, one
  more than the Cooking Pot needs.
  - Also Chorus Stew, Endermite Stew, Crab Pasta, Pumpkin Soup, Pasta with Meatballs, Dumplings and Cavern
    Explorer Stew.
  - Cooked ingredients: Baked Potato, Tomato Sauce, cooked meat, cooked vegetables such as Grilled Corn. Soups add
    Bone Broth.
- **New:** Squid Ink Pasta by heated mixing: 250 mB ink + 250 mB Tomato Sauce = 2.
- **Changed:** Fried Rice, Baked Cod Stew, Chicken Soup and Mushroom Rice by hand take Fried Eggs, Baked Potatoes,
  Tomato Sauce and cooked vegetables (were raw eggs, potatoes, tomatoes and carrots). Sinigang takes 2 cooked fish
  (was 1).
- **Changed:** Meals crafted by hand from cooked ingredients are hand-only: Create's unheated mixer and Mechanical
  Crafters no longer make them.
  - Also hand-only: Bone Broth, Cooked Rice from broth, Cabbage Rolls, Scrambled Eggs, Vegetable Omelet, Bowl of
    Exotic Fruits, Quiche, Frosting, Cookie Dough and Pie Crust (Create's compactor still makes it from flour).
- **Changed:** Burgers are shaped: toppings on top, patty in the middle, any bread (including Cornbread and
  Sourdough Bread) at the bottom. Cheeseburgers take cheese wedges (milk recipes removed).
- **Changed:** Mutton Wrap needs a Tortilla (was bread).
- **Changed:** Scrambled Eggs take 3 eggs (was 2).
- **Changed:** Vegetable Omelet in the Cooking Pot takes Fried Eggs (was raw eggs). Also crafted from 2 Omelettes,
  a cheese wedge, Onion, Carrot and a bowl.
- **Changed:** Bowl of Exotic Fruits takes any Fruits Delight fruit or a melon slice (was Melon Slice only). Fruit
  Salad takes melon, cantaloupe, hami melon, pineapple, lemon or orange slices.
- **Changed:** Quiche: one recipe for bacon or brown mushroom.
- **Changed:** Shepherd's Pie takes a cheese wedge (was milk).
- **Changed:** Crispy Skewer, Crawling Sandwich and Steak and Potatoes are shaped (were shapeless).
- **New:** Sequenced assemblies for every burger (on bread; Hamburger with Egg on a Beef Patty), Mutton Wrap,
  Fruit Salad (2 per run), Shepherd's Pie, Horror Lasagna (2 per run), Stuffed Potato, Crispy Skewer, Crawling
  Sandwich and Steak and Potatoes. Quiche has one assembly for both fillings.
- **New:** Bowl from 3 Bamboo in a V.
- **Fixed:** Cooking for Blockheads' Oven accepted no fuel. It now takes any furnace fuel and Cooking Oil. Leftover
  bottles and buckets go to the output slots, the last one to the fuel slot.
- **Changed:** Cooking for Blockheads' Oven also cooks smoker-only food, such as Ham into Smoked Ham.
- **Changed:** Baking muffins in the Oven gives 4 (was 2). The Cooking Pot still gives 2.
- **New:** Tooltips on the Fantasy Furniture ovens ("Works like a smoker") and Cooking for Blockheads' Oven.
- **Changed:** Cooking for Blockheads' Sink gives endless water without piping.
- **New:** Farmer's Delight Cutting Boards can be placed on a Kitchen Counter or Kitchen Corner.
- **New:** Kitchen Corner has 27 slots of storage, like a counter: hopper access, drops contents when broken,
  usable by an adjacent Cooking Table.
- **Changed:** A two-high Fridge fills its full block space: no gaps beside counters, flat top.
- **Fixed:** Bottles and buckets in the baking Oven's leftover slots appeared to vanish when taking the output.
- **New:** WTHIT shows a Cooking Pot's, Kettle's or baking Oven's leftover slots ("Holding: 3 Glass Bottle"), and a
  red warning when they are full and cooking has stopped.
  - A reused bowl or bottle stays in the container slot for half a second before the meal uses it.
- **Changed:** Duplicate items merged into one: Neapolitan's banana (Alex's Mobs' is gone), Farmer's Delight's milk
  bottle (Neapolitan's is gone), Create Crafts & Additions' chocolate cake (Neapolitan's is gone) and Rustic Delight's
  potato slices (diced potatoes are gone). Old copies turn into the kept item in your inventory.
- **Changed:** Vinery's Cider is now Hard Cider.
- **New:** Long and Strong Apple Cider in the Mechanical Mixer (1000 mB Apple Cider + a Milk Bottle or a Honey
  Bottle, heated) and the IE Mixer (1500 mB). Apple Cider in the IE Mixer (water, 2 apples, sugar); all three
  ciders in the Bottling Machine.

## Blocks & Building <!-- icon: minecraft:paper -->

- **Changed:** Dave's Rocky Dirt is now Pebble Dirt, made from pebbles; Rocky Dirt is made from cobblestone. Coarse
  dirt in place of dirt makes 8 of either. Decorative Blocks' Rocky Dirt is merged into Twigs'.
- **Changed:** Quark's Permafrost is now Froststone: 2 Packed Ice + 2 Stone make 2. Frozen peaks caves generate
  Immersive Weathering's Permafrost instead.
- **Changed:** Sticky notes and labels rebuilt. They are part of the block they sit on and no longer break from
  hitting the chest, arrows or explosions.
  - Up to five notes per block face, including floors, ceilings, stairs, slabs and fence and wall tops. Sneak to
    place one on a chest or barrel.
  - Right-click a placed note: with dye to colour it, empty hand to read or edit, sneak with empty hand to pick it
    up. Left-click breaks it; it keeps its text and colour.
- **New:** Sticky Notes come in any colour and stack to 64. Craft with one dye (colours mix like leather), or with
  an ink sac, glow berries, gunpowder or a gold nugget for the Inverted, Smiley, Creeper and Gold Sticky Notes.
  - 1 written note + 8 blank = 9 copies. A placed plain note has a 1 in 100 chance to become a Gold Sticky Note.
- **Changed:** Write on a Sticky Note by right-clicking in the air. Right-click an inventory item with the note, or
  combine both in an anvil, to label the item (up to three lines). The Eraser removes the last line.
  - Labelled items can't be used in crafting until erased. A note holds 32 characters; labelling in the anvil costs
    1 level; an item takes three labels at most.
  - The Eraser also wipes a placed note (right-click) or a note crafted with it, and turns a blank note back to plain.
- **Changed:** Labels show their item in true colours, rendered only up close. Sneak-right-click with an item to set
  it, empty hand to clear. Dye colours the icon; a feather shows the item name. Chests open through a label.
  - A glow ink sac makes the icon glow; an ink sac turns it off. WTHIT shows a note's text and day, and a label's item.
  - Notes and labels drop when Create moves their block; pistons destroy them.
- **Changed:** Sticky It! notes and Storage Labels labels are uncraftable; their recipes make the new ones. Placed
  old ones work until broken.
  - Text written onto items by old sticky notes no longer shows in tooltips.
- **Fixed:** Hovered items (mostly bowls) would not stack with fresh ones. Already affected items stay separate.
- **Fixed:** Dyeing a placed sack deleted its contents.
  - Placed blocks with contents (sacks, shulker boxes, presents, banners, beds) can no longer be dyed in the world.
    Sacks still dye in the crafting grid. Wool, concrete, glass and other plain blocks still dye in place.
- **Fixed:** Dye on a canvas sign replaced it with another sign and wiped the text. It now colours the text.

## Mobs & Animals <!-- icon: minecraft:squid_spawn_egg -->

- **New:** Baby squid, glow squid and dolphins. 1 in 20 wild squid and glow squid and 1 in 10 wild dolphins spawn
  as babies. They grow up in 20 minutes.
  - Any fish fed to a baby dolphin removes a tenth of its remaining time. Babies drop nothing and give no
    experience; baby dolphins don't fight back. Spawn eggs give adults.
- **New:** Tamed cats ride on your shoulders, one per side. Look at your standing cat and press `B` to pick it up;
  `G` sets shoulder pets down. Both keys are in Controls.
  - Cats stay on through jumps, falls and hits (parrots still hop off). Creepers and phantoms avoid you while a cat
    rides.

## Gameplay <!-- icon: minecraft:campfire -->

- **Changed:** Campfires and soul campfires burn until put out (was about 100 seconds).
- **New:** A campfire on a Block of Coal or Charcoal Block stays lit in rain.
  - Other campfires go out after about 8 seconds of rain; soul campfires never do.
  - Food on a campfire put out by rain stays on the grill.
- **Changed:** Spice of Life: food variety bonuses start after 10 different foods (was 0), reset on death (was kept),
  and the last 256 foods are remembered (was 32).
- **Changed:** Create: schematic limit 256 (was 10).
- **Changed:** Botania Trinket Case holds any item except shulker boxes and other Trinket Cases (was trinkets,
  rods and mana items only). Only trinkets equip from it.
- **Changed:** Netherite Backpack: 8 upgrade slots (was 7). Looting Upgrade: +5% chance per level (was +15%).
- **Changed:** Fishing experience multipliers by catch quality: x2 / x3 / x5 (was x1.5 / x2.5 / x4).
- **Fixed:** In creative, middle-clicking a block with a full hotbar deleted the held item.

## Farming & Nature <!-- icon: minecraft:oak_leaves -->

- **Changed:** Wild tree leaves survive up to 12 blocks from the trunk (vanilla 6). Leaves on untouched trees never
  decay.
  - Leaves of a chopped tree still fall within about a second.
- **Changed:** Rubberwood leaves no longer drop apples.
- **Changed:** Bell peppers drop red, green and yellow equally (red was most of the harvest).
- **Changed:** The Wandering Winemaker no longer sells Fruits Delight's apple sapling.

## Interface & Tooltips <!-- icon: minecraft:written_book -->

- **New:** The Trinket Case screen has Quark's sort and transfer buttons and search bar.
- **New:** A book button beside Singleplayer (title screen) and Back to Game (pause menu) opens *What's New*: notes
  for each pack version, newest first. After an update, a speech bubble marks the button until opened.
- **New:** Drink fluids in JEI list their effects under the name, like potions (Apple Cider, teas and others).
- **Fixed:** Hunger and saturation bars missing on food in: Cooking Pot, Kettle and Oven meal slots, Cooking Guide,
  Brewing Guide, the Keg's tank (and in JEI), and Vinery's recipe book.
  - Also TrashSlot, the Pretty Pipes Item Terminal, FTB Quests, Delivery Agreements, Patchouli books, and AE2
    crafting screens, pattern encoding and guide.
  - Items in JEI recipes, FTB Quests and AE2 crafting lists get the full inventory tooltip, including the mod name.
- **Fixed:** Hundreds of blocks, items, mobs, effects and villager jobs showed a raw name (like
  `item.witchmobility.goldenwood_broom_item`) or wrong capitalisation.
- **Changed:** Mod names in JEI, WTHIT and the Mods screen: spaces between words, "Create:" prefix on Create addons
  (Create: Deco, Create: Steam & Rails), no author tags. JEI `@` search accepts old and new names.
- **Removed:** Tutorial pop-ups: vanilla's movement hints, Exposure's camera and lightroom hints, Fairy Lights'
  "craft hanging lights" balloon. Quest, autocrafting and other pop-ups remain.
- **New:** Every effect's JEI page lists at least one source: mobs (as spawn eggs), blocks, armor set bonuses,
  drinks and treats, Botania brews, potion buckets and bundles.
- **New:** JEI Feast Plating tab: what each placed feast gives per click, what to hold, and the leftovers.
- **Fixed:** Wrong, incomplete or misspelt JEI effect descriptions, including Comfort, Caffeinated, Soul Cross,
  Shocked, Flashed, Double Jump and Frosty Armor. Travel Fatigue now has one.
- **Changed:** Effect colours in tooltips: Rest is blue; Debilitating Sting, Scared Still, Power Outage, Slipping
  and Sensory Perception are red.
- **New:** WTHIT shows more block info:
  - Jars, Goblets and Cages: contents ("Contents: Honey (6 / 12)"), plus potion effects.
  - Beehives and nests: "Bees: 2 / 3" and "Honey Level: 3 / 5". Respawn Anchors: charges. Sea Pickles: count.
  - Feasts: servings left. Cakes, pies, cheese wheels, pizza: slices. Nests: eggs and hatching. Modded cauldrons:
    level.
  - Campfires: burn time and rain status ("Rain: Going out in 6s").
  - Sheetmetal Tanks: fluid on every part. ME Kitchen Station: online status.
- **Fixed:** About 270 blocks showed a blank or purple-and-black icon in WTHIT, including placed pies and plates,
  dyed water cauldrons, posts and belt funnels.
- **Changed:** JEI's ingredient list starts with copper and iron tools from all mods grouped together.
