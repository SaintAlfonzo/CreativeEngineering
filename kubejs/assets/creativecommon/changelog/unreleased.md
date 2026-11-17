# Unreleased

## Diet Bonuses <!-- icon: solapplepie:food_book -->

- **New:** Spice of Life: Raspberry Pie Addition. Food diversity climbs a ladder of hearts, armor toughness, knockback resistance, permanent buffs, effect slots and effect unlocks. Applies to new worlds.
- **Changed:** The pack's diet bonuses are 8 hearts, 3 armor toughness tiers, knockback resistance, 10% walk speed, permanent Speed I at diversity 20 and 5 effect slots [fixed Apple Pie bonuses].
- **New:** Effect slots hold an unlocked effect of your choice: Speed, Haste, Strength, Jump Boost, Night Vision, Water Breathing, Luck, Regeneration, Shrinking, Long Reach, Patrol Nullifier, Phantom Bane, Fleet-Footed or Lava Walker.
- **New:** Filling a slot or changing its level is free. Changing a slot's effect ages your 8 oldest recent foods out of your diet; dropping a slot ages out foods until your highest slot switches off. A confirm panel shows the cost first.
- **New:** Effect unlocks are kept for good. Slots below their diversity gray out and keep their choice.
- **New:** Diet bonus effects return the tick after milk, a totem or a clear removes them.
- **New:** Diet bonus messages show on the action bar, one at a time.
- **New:** Diet Bonuses screen (P) and a diet bonus bar on the inventory; Shift+T unlocks the bar to move it.
- **Changed:** The Spice of Life Food Book is called Diet Log. It shows the diet ladder and has a Diet Bonuses button.
- **New:** The Diet Log's progress bar shows the last milestone reached and the next 3, with an icon for each bonus. The Spice of Life bonus pages are gone.
- **New:** The Diet Log can show foods in your diet and older foods on two pages side by side (client setting, on by default).
- **Changed:** The Diet Log is crafted from a book and any food that is not a drink [book and potato].
- **New:** Liquid Diet Log, a book (book + any drink) listing every drink you have had. No bonuses.
- **Changed:** Drinks, potions and milk no longer count toward food diversity or push foods out of your diet.
- **New:** New players start with a Diet Log and a Liquid Diet Log.
- **Changed:** Dying ages every recent food out of your diet and resets the foods-eaten count; bonuses return as you eat [the diet was cleared].
- **Changed:** Diet bonuses start after 5 different foods eaten [10 foods, repeats counted]. Applies to new worlds.
- **Changed:** Lava Walker burns like a Magma Block when standing on lava without Fire Resistance.
- **Fixed:** Fleet-Footed stayed on for every player while one player sprint-jumped.

## Visceral Heap <!-- icon: biomesoplenty:flesh -->

- **New:** The Visceral Heap biome generates in the Nether, about 5% of its area, made entirely of flesh, with blood lakes, eyebulbs, hair and flesh tendons. Nether ores do not generate in it.
- **New:** Flesh slows walking to 0.6x. Porous Flesh slows walking to 0.45x, halves jump height and slides you down its sides, like a Honey Block.
- **New:** Flesh Tendons can be climbed like a ladder.
- **New:** Crushing Wheels and the Millstone crush Flesh and Porous Flesh into drops from four groups. In JEI each group is one slot that cycles its items.
- **New:** Crushed Flesh gives 1 meat (Rotten Flesh, Rotten Ham, Leather, Spider Leg, Hoglin Ear, Hoglin Loin), bone at 75% (Bone, Bone Meal, Broken Bone, Bone Chunk, Bone Serpent Tooth), eye at 25% (Spider Eye, Eyebulb, Fermented Spider Eye), and Pus Bubble, Flesh Tendons or Hair at 25%.
- **New:** Crushed Porous Flesh gives 1 meat plus a second at 50%, 1 eye, and a Pus Bubble, Flesh Tendons or Hair at 50%, no bone.
- **New:** Crushing a Pus Bubble gives a Slimeball at 25%; crushing Hair gives String at 50%.
- **New:** Heated mixing turns Flesh into 250 mB of blood and a bone at 25%, Porous Flesh into 500 mB, Flesh Tendons into 250 mB.
- **New:** Mixing 2 Porous Flesh, 2 Flesh and 1000 mB of plant oil, seed oil or vegetable oil gives 2 Porous Flesh and 3 Flesh.
- **New:** Mixing 2 Flesh Tendons, 1 Flesh and 250 mB of blood gives 1 Porous Flesh.

## Walk Speed <!-- icon: minecraft:honey_block -->

- **Fixed:** The view popped up for a moment when walking from one sinking block to another, or onto a Honey Block, Mud or a path from one.
- **Changed:** Standing on a Honey Block shows the same sink depth as wool or sand [smaller].
- **Changed:** Dropped items lie on top of sinking blocks instead of sinking into them.
- **Changed:** Banana Slug Slime Blocks slide you down their sides like a Honey Block, with no fall damage.
- **Fixed:** Framed Blocks and Copycats+ blocks walked at normal speed instead of the speed of their material.
- **Fixed:** Create Confectionery blocks, Metal Fences and Mosaic Masonry blocks walked at normal speed instead of their own.
- **Changed:** Placed dishes, plates, jars and bottles, cakes, ice cream, chocolate blocks and chocolate coins walk at 0.85x [1x].
- **Changed:** Coralstone, tooth and scute blocks and crystal clusters walk at 1.05x, coral blocks at 0.92x, kelpy cobblestone and mossy flower cobblestone at 1.15x, kelpy, tooth and mossy flower bricks, scute shingles and manure bricks at 1.25x, scute pavements and polished manure at 1.25x [1x].
- **Changed:** Sleeping bags, hammocks, bedrolls, pet beds, nests, kelp and thatch blocks, licked salt blocks and cardboard boxes walk at 0.88x [1x].
- **Changed:** Vintage Delight jam and jello blocks walk at 0.45x [1x]; crates of produce, eggs and animals and beehives at 1.1x [1x].

## Walking through plants and webs <!-- icon: minecraft:cobweb -->

- **Changed:** Planted crops slow walking to 0.75x once half grown [no slowdown]; tall crops slow on both blocks once the lower block is half grown.
- **Changed:** Corn, hemp, rice, oats, wheat and tomatoes slow walking to 0.50x with the same crop on 3 or 4 of their 4 sides [no slowdown]; newly planted crops count as neighbours. Swimming in a rice paddy is not slowed.
- **Changed:** Wild crops, tall grass, large ferns and tall flowers slow walking to 0.75x [no slowdown].
- **Changed:** Sugar cane slows walking to 0.50x [no slowdown]; propelplant cane 0.50x [about 0.35x].
- **Changed:** Vines of every kind slow walking to 0.75x while you move through them [no slowdown]; climbing is unchanged.
- **Changed:** Ropes slow walking through them to 0.75x [no slowdown]; climbing, sliding and sneaking on them are unchanged.
- **Changed:** Cobwebs, barbed wire and razor wire slow walking to 0.50x [about 0.1x] and falling to 0.85x, and cut fall damage by 0.85x per web passed. Barbed wire and razor wire still hurt; Spiders and Cave Spiders are not slowed.
- **Changed:** Cobwebs with webs on 3 or more of their 6 sides slow walking to 0.35x and falling to 0.20x, with no fall damage.
- **Fixed:** Falling into a single cobweb or a short stack of cobwebs canceled all fall damage.
- **Changed:** In a cobweb with webs on all 6 sides, falling stops; jump climbs slowly, sneak sinks slowly.
- **Changed:** Taiga grape bushes, salmonberry bushes and cranberry bushes slow walking like sweet berry bushes [no slowdown].
- **Changed:** Sweet berry bushes and the other berry bushes no longer cancel fall damage [sweet berry bushes did].
- **Changed:** Snow layers, leaf piles, ash and sand layers, Fodder and Sawdust slow walking by height, 0.875x at 2 layers down to 0.50x at 5 of 8 (Sawdust 6 of 9); 6 or more layers walk normally [snow 0.75x from 3 layers, leaf piles 0.45x from 4, the others no slowdown].
- **Changed:** Bamboo Leaves slow walking to 0.75x at 2 layers and 0.50x at 3, normal at 4 [about 0.35x from 3 layers], and now slow ocelots, pandas and parrots too.
- **Changed:** Villagers, horses and every mob are slowed by these blocks like players; flying, gliding and swimming are not.
- **Changed:** Dropped items and experience orbs stop inside any cobweb and hang there until the web is broken [they sank slowly through].
- **Changed:** Projectiles slow down in cobwebs [no effect]. Guns, turrets and siege munitions are not affected.
- **Changed:** Snowballs, eggs, fireballs, ender pearls and similar break in the middle of a cobweb with webs on all 6 sides, and where they stop in any other cobweb; an ender pearl teleports you there.
- **Changed:** Projectiles in a cobweb enclosed on every side by webs:
  - Thrown potions and bottles o' enchanting sink down the middle and break below.
  - Firework rockets stay until they explode.
- **Changed:** Arrows, tridents and thrown tools hang in a cobweb enclosed on every side by webs, without their hit effect, and are picked up as the same item.
- **Changed:** Fishing bobbers, Feathers on a Stick, pickarangs and the Thorn Chakram snag in any cobweb; snagged pickarangs and chakrams are picked up by walking into them and drop when the web breaks.
- **Changed:** Arrows, tridents and thrown tools players can pick up stay stuck for 10 minutes [1 minute], up to 128 per chunk; Loyalty tridents and tools still never despawn.
- **Changed:** Walking through these blocks does not change the field of view.
- **New:** WTHIT shows Walk Through, Fall Through and Neighbors lines on these blocks; the walk speed includes the block underneath.

## Wise Oak <!-- icon: minecraft:oak_log -->

- **New:** At high trust, each conversation with a Wise Oak has a 5% chance to drop a sapling, a fruit tree sapling, 4 tree fruit or 16 leaves.
- **Changed:** Wise Oaks are awake by day and asleep at night and in thunderstorms [asleep by day].
- **Changed:** A Wise Oak tired of talking sleeps for 5 minutes.
- **Changed:** Right-clicking a sleeping Wise Oak makes it angry [woke it].
- **Changed:** Anger lasts 30 seconds [random], then the Wise Oak sleeps for 10 minutes.
- **Changed:** Wise Oaks generate only in biomes with oak trees [every Overworld biome].
- **Removed:** The Wise Oak's online lookups of player IP address, location, weather, quotes, facts and names.

## Hearth and Harvest <!-- icon: hearthandharvest:butter -->

- **Changed:** The Farmer's Hat harvest bonus no longer applies to the Summer Hat.
- **Changed:** Spool is named Spool of String.
- **Changed:** Street Corn gives Fire Resistance for 30 seconds.
- **Changed:** Biscuits and Gravy are made only in the cooking pot, from any dough, raw sausage, salt and milk [also shapeless].
- **Changed:** Glazed Carrots accept cave carrots.
- **New:** Wooden buckets hold sap, milk and honey; the Tree Tapper fills them with sap.
- **New:** A wooden bucket milks cows and goats; a Wooden Milk Bucket is drunk like a milk bucket.
- **Changed:** The Tree Tapper can be crafted with a wooden bucket.
- **Fixed:** Goat Milk Bottles and Chocolate Milk Bottles showed no tooltip.
- **Fixed:** Thrown cleavers and pitchforks had no names.
- **New:** Raspberry Pie and its slices have the tooltip "Doesn't run programs".
- **Changed:** Corn Mazes generate twice as often.
- **Changed:** Mums go in flower pots, planter boxes and flower boxes, and spread with bone meal like other small flowers.
- **Fixed:** Syrup had two crafting recipes into sugar; only the one giving 4 sugar is left.

## Hops <!-- icon: hearthandharvest:hops -->

- **Changed:** Hops grow as a vine: plant them on the side of a log or any solid block; they need no farmland.
- **Changed:** Hops plant on a Grapevine Lattice like jungle grapes [own hops lattice, on farmland]; old hops lattices load as Grapevine Lattices with their hops.
- **Changed:** Hops vines ripen over 4 stages; right-click a ripe vine for 1 to 2 hops; younger vines cannot be picked; shears stop its growth.
- **Changed:** Bone meal, Manure, Tree Fertilizer, Fertilizer, Floral Fertilizer, the Fertilizer Bag and Organic Mash grow a hops vine one stage.
- **Changed:** Wild hops hang on tree trunks in temperate forests, 1 in 96 chunks [ground patches, 1 in 120 chunks].
- **Changed:** Hops, Hops Salad, Bags of Hops and Wild Hops exist only with Creative Brewing installed.
- **Fixed:** Hops never grew: the lattice dried the farmland under it.

## Tilled End Stone <!-- icon: minecraft:end_stone -->

- **Removed:** Hearth and Harvest's Tilled End Stone; end stone tills into Ender Crop's Tilled End Stone, and old blocks become it.
- **Changed:** End stone tills only with a netherite or better hoe, also with the Tilling enchantment [any hoe with Unbreaking].
- **New:** End stone and Tilled End Stone have the tooltip "Till end stone with a netherite hoe".

## Stomping Basin <!-- icon: hearthandharvest:stomping_basin -->

- **Changed:** The Stomping Basin takes only items used in a stomping recipe; other items keep their own use.
- **Fixed:** The Stomping Basin swallowed empty buckets and any other item, also from hoppers.
- **New:** 3 grapes and an empty wine bottle stomped in a 2x2 Stomping Basin give that grape's juice, for every grape the Grapevine Pot presses.

## Seed Pouches and Bundles <!-- icon: quark:seed_pouch -->

- **Fixed:** A Seed Pouch in a dispenser crashed the server.
- **Changed:** A Seed Pouch in a dispenser plants up to 25 seeds in a 5x5 in front of it, Nether Wart in the Nether included, and stays in the dispenser.
- **New:** A Bundle in a dispenser plants its first seed in front of it and stays in the dispenser.

## Horseshoes <!-- icon: hearthandharvest:horseshoe -->

- **Fixed:** Sneak-clicking a shod horse with an empty hand, shears or a horseshoe opened its inventory instead of taking the horseshoe off.
- **Fixed:** Shod horses took damage from cactus and sweet berry bushes.

## Crows <!-- icon: hearthandharvest:crow_feather -->

- **New:** Hearth and Harvest's Crow replaces Alex's Mobs' Crow.
- **New:** Crows spawn only at nests: a nest generates with 2 to 3 crows and, while a player is within 48 blocks, adds one every 5 minutes up to 3.
- **New:** Crow Nests generate on tree tops in temperate forests and on about a quarter of village house roofs, some holding stolen items.
- **New:** Tame crows fetch items dropped near their owner, hopping off the shoulder to do so.
- **New:** A Crow Nest can be crafted into 2 Straw or milled into 3 Straw.
- **Changed:** Nest is named Crow Nest.
- **Changed:** Crow Nests no longer generate on the ground [grassy biomes].
- **Removed:** Alex's Mobs' Crows no longer spawn; their spawn egg is removed.
- **Fixed:** A crash when new chunks generated near crows.
- **Fixed:** Tame crows never bred; corn and corn seeds start breeding.
- **New:** A hurt tame crow eats crow food (seeds, pips and corn) to heal.
- **Fixed:** With hand feeding switched off, wild crows still ate crow food and fell in love.

## Crop Yields <!-- icon: farmersdelight:rich_soil_farmland -->

- **Changed:** Fortune no longer changes the drops of any crop, berry bush or wild crop except melons.
- **Changed:** The better-soil extra is sized on each crop's average harvest rounded to the nearest half. Carrots, Potatoes, Onions and Peanuts average 4.87 on Rich Soil Farmland and 6.02 on Fertile Soil [4.93 and 6.16]; Tobacco, Sweet Potato, Zucchini, Garlic, Hemp and Bell Peppers move by under 0.2.
- **New:** Right-click picks get the better-soil extra: Tomatoes (the soil under the bottom vine, also on ropes and sticks), Cucumber, Ghost Pepper, Zucchini, Sweet Berries, Strawberries, Blueberries, Cranberries, Lemons and Salmonberries.
- **New:** Breaking a ripe Sweet Berry Bush, Strawberry Bush, Blueberry Bush, Cranberry Bush or Lemon Tree gives the better-soil extra.
- **New:** Nether crops give about a third more on Rich Soul Soil and two thirds more on Fertile Soul Soil:
  - Nether Wart (Fertile Soul Soil only), Ghost Peppers.
  - Picked Propelpearls (Rich Soul Soil under the lowest stem).
  - Fungus Colonies, broken ripe or sheared.
- **Fixed:** A ripe Cucumber or Ghost Pepper dropped no seeds when broken; it now drops 1 to 4.
- **Changed:** In Botany Pots, Rich Soil and Rich Soil Farmland grow plants 10% faster [Rich Soil Farmland 25%] and Fertile Soil is a pot soil that grows them 20% faster. No soil changes pot yield.
- **New:** Rich Soil, Rich Soil Farmland and Fertile Soil show their Botany Pots growth speed in the tooltip.

## Corn <!-- icon: corn_delight:corn -->

- **Changed:** Maize is named Corn.
- **Changed:** Cutting corn or wild corn gives 6 kernels [4], a corn cob with a 75% chance and a corn husk with a 33% chance [75%].
- **Changed:** Milling a corn cob in a hand mill or crusher gives 3 kernels [4].
- **Changed:** Wild corn gives the same results as corn in the crusher and hand mill.
- **Fixed:** The Cob Pipe showed as a flat item on the head and now shows its 3D model.

## Veggie Patties <!-- icon: minecraft:carrot -->

- **Changed:** Vegetarian Patties are named Veggie Patties.
- **New:** Cutting a cave carrot gives 2 raw veggie patties; a baked cave carrot gives 2 cooked ones.

## Salt <!-- icon: salt:salt -->

- **Changed:** Salt Dust is named Salt.
- **Changed:** Licked salt blocks are rock salt layers 12, 8 and 4 pixels high [full blocks].
- **New:** A salt layer placed on a licked salt block refills it one stage per layer; waxed blocks stay waxed.
- **New:** Salt in a dispenser restores a licked salt block, waxed or not, and puts a salt layer on top of a full one.
- **New:** Licking a salt block with salt layers on top eats the top layer; the block keeps its size until the layers are gone.
- **New:** Salt Melt: a 64-use tool made from 2 Salt and 2 Raw Rock Salt. Right-click ice or snow to melt it at once; Packed Ice and Blue Ice go down one tier per use. Works in a dispenser.
- **Changed:** Salt in a dispenser melts the ice or snow in front, one tier per Salt, or places a salt layer on the ground in front [did nothing].
- **Changed:** A salt layer has a 5% chance to lose a layer each time it melts ice or snow; Salt Blocks never wear away.
- **Fixed:** Animals more than 16 blocks from a salt lick never walked to it.
- **Fixed:** Animals that reached a salt lick could stand beside it without licking.

## Melting <!-- icon: minecraft:ice -->

- **New:** Licked salt blocks melt touching ice and snow, like other salt blocks.
- **New:** Fire, soul fire, lit campfires, magma, torches, lanterns, jack o'lanterns and glowstone melt ice and snow within 3 blocks. Modded torches, sconces, lanterns and lit braziers and candelabras count too.
- **New:** Lava melts ice and snow within 4 blocks.
- **New:** Ice and frosted ice melt into water, or into nothing in the Nether. Snow layers, Snow Blocks and Powder Snow melt into nothing.
- **New:** Packed Ice and Blue Ice melt one tier at a time (Blue Ice to Packed Ice to Ice), slowly: touching salt or fire, or within 2 blocks of lava.
- **New:** Snow, Snow Blocks and Powder Snow placed in the Nether melt at once.
- **Changed:** Light alone no longer melts ice or snow: sea lanterns, shroomlights, redstone torches and lamps and other lights without a flame leave them [any light level 12 or more].
- **Fixed:** The Salt Block and the Block of Raw Rock Salt melted no ice or snow.

## Bees <!-- icon: minecraft:honeycomb -->

- **Changed:** Wild plants (wild carrots, beetroots, rice, tomatoes and the rest) no longer breed bees or feed hummingbirds; bees still pollinate wild onions, carrots and potatoes and no other wild plant.
- **Changed:** Botania special flowers, the flower crown and azalea leaf and flower piles no longer breed bees or feed hummingbirds.
- **Changed:** Wild plants still float in water and go in planter and flower boxes, and structures still clear them as plants.
- **New:** Just Enough Breeding pages for ducks, geese, grizzly bears (Buzzier Bees), Fungi Delight mooshrooms and Cozy cats.

## Mod Ids <!-- icon: creativedelight:oven -->

- **Changed:** Creative Delight items and blocks are listed under Creative Delight [Creative Engineering].
- **Changed:** Tier shields, stepstools, echo tools, the Air Brake, Felsic Scrap and Arrow Fletching are listed under Creative Equipment [Creative Engineering].
- **Changed:** Dark Fire and Flint and Terrasteel are listed under Creative Magic [Creative Engineering].
- **Changed:** The Weathered Cardboard Box is listed under Creative Compat [Creative Engineering].
- **Changed:** The Netherite Lunchbox is listed under Creative Delight [Spice of Life].
- **Changed:** Commands that name the pack's own dimensions, structures and worldgen tags take Creative Worldgen names [Creative Engineering].
- **Changed:** Existing worlds keep these items and blocks, the pack's dimensions with the players in them, generated structures, including stored kitchen corner contents, placed paintings, sticky notes and camera bag contents.
- **Changed:** Food seasoned before this version loses its seasonings.
- **Changed:** JEI bookmarks and other player files outside the world lose the moved items.
- **Changed:** Datapacks that use the old Creative Delight recipe types need updating.
- **Changed:** A world opened on this version loses these items and blocks if opened on an older version.

## Portals <!-- icon: portalgun:portal_gun -->

- **Fixed:** The Immersive Portals portal block had no name.
- **Fixed:** Walking out of a Portal Gun portal on a wall still lifted the player one block, and Nether and End portals did the same. Only portals in a floor lift the player now.
- **Fixed:** Falling from a floor portal into a wall portal left the player stuck in the floor in front of the wall.

## Eggs <!-- icon: farmersdelight:fried_egg -->

- **Changed:** Cooking Oil and 3 eggs make 3 Fried Eggs in the Cooking Pot [1 egg, 1 Fried Egg].
- **New:** 4 Fried Eggs make 4 Omelettes in the Cooking Pot.
- **Removed:** Furnace recipes for Fried Eggs, Fried Turtle Eggs, Omelettes, Fried Dragon Eggs, Shulker Omelettes and Boiled Emu Eggs. The Smoker, campfires and the Skillet still cook them.

## Engineering Recipes <!-- icon: immersiveengineering:workbench -->

- **Fixed:** Engineer's Workbench recipes that list an input twice could not be crafted (spectral and split arrows, dropper, observer, piston, thunder detector, note block, jukebox).
- **Changed:** 8 Blocks of Coal make a Diamond in the Pressure Chamber [9].
- **New:** One Cinder Flour filled with 25 mB of Strength, Night Vision or Harming potion makes Redstone Dust, Glowstone Dust or Gunpowder.
- **Fixed:** Splash and lingering Mundane Potion fluid worked in the Thunder Detector assembly.

## Farms and Exploits <!-- icon: minecraft:iron_ingot -->

- **New:** A village that loses 5 iron golems within 10 minutes summons no golems for one in-game day. Golems built by players do not count.
- **Changed:** Boats on ice: Blue Ice 0.98 [0.989], Packed Ice 0.96 [0.98], Ice 0.94 [0.98] slipperiness. Players, mobs and items slide as before.
- **Changed:** Endermen no longer pick up blocks.
- **Changed:** Curing a zombie villager resets its trade discounts.
- **Changed:** Sheep drop no wool when killed.
- **Fixed:** Falling blocks could be duplicated through portals.
- **Fixed:** Pistons could duplicate TNT and other blocks.
- **Fixed:** Growing mushrooms replaced blocks.
- **Fixed:** Tripwire hooks could be duplicated.

## Jars <!-- icon: vintagedelight:empty_mason_jar -->

- **Changed:** 4 Mason Jars per craft [3].
- **Changed:** 4 Glass Bottles per craft [3].

## Vanilla Bug Fixes

- **New:** Debugify Reforged, on clients and the server, with 9 of its vanilla bug fixes on.
- **Fixed:** Mobs that stayed in their death animation never disappeared.
- **Fixed:** A cured baby zombie villager stayed on the chicken it rode.
- **Fixed:** Skeletons wearing leather armor still turned into strays in powder snow.
- **Fixed:** Adult striders spawned with saddles in Peaceful.
- **Fixed:** The Block of Raw Copper made stone sounds.
- **Fixed:** Minecarts with Command Block forgot their cooldown when the world was saved.
- **Fixed:** Snow golem snowballs hurt wolves.
- **Fixed:** Lightning burned the items dropped by the mob it killed. Only items dropped in the last moment before the strike are spared.

## Food & Cooking

- **New:** Two Cooking for Blockheads Fridges side by side, facing the same way, join into a double fridge with no gap or seal between them. A Fridge placed next to another opens the other way. Each keeps its own door and inventory.
- **New:** Food Dictionary, a book (book + carrot) listing every food, tasted or not yet tried. Bites of placed pies and cakes count. No hearts or effects.
- **Removed:** Golden and gilded mushroom stew, beetroot soup and rabbit stew, their cans and jar fluids. Existing ones in worlds are deleted.
- **Fixed:** Flavored Rustic Delight pancakes placed on a coaster showed a missing model.
- **Fixed:** Bites of a placed cake after the first, and of a cake stacked on a cake, did not count as cake slices for diet bonuses or the Food Dictionary.
- **Fixed:** A stick used on an Ingredients Basin without enough for a skewer was lost or placed on top of the basin; it stays in the hand.
- **Fixed:** A pumpkin pie used on a Cutting Board was placed as a pie instead of going on the board.
- **Changed:** Golden food in the Arc Furnace takes 4 gold [5].
- **New:** A Banana Bunch cuts into 6 Bananas on a Cutting Board.
- **Changed:** Vinegar ferments from 3 Apple Mash and any mushroom in the Pickling Jar [any jam and a mushroom].
- **Changed:** Fruit pies and cheesecakes take 3 fruit [2].

## Lunchboxes <!-- icon: solapplepie:lunchbox -->

- **Changed:** Lunchboxes show as not counting for food diversity; the food eaten out of them still counts.
- **Changed:** The lunchbox tooltip reads Food slots [Slots].
- **Fixed:** Lunchboxes filled other than through their screen accepted items that are not food.

## Blocks <!-- icon: createdeco:cast_iron_block -->

- **Changed:** Cast Iron Block is named Cast Iron Plating.
- **Fixed:** The breaking cracks on Straw, Hay and Canvas Rugs also covered their fringe outside the block.
- **Changed:** Melons, pumpkins, carved pumpkins, jack o'lanterns, moss blocks and moss carpets no longer go in flower boxes or planter boxes.
- **Changed:** Village waystones have Yellow Corundum in their base [Glowstone].

## Loading screen tips

- **Changed:** The loading screen tip bar also shows while reading world data, creating a world, connecting to a server and joining [world loading and terrain screens only].
- **Changed:** One tip stays through a whole load and changes only every 10 seconds [a new tip on each loading screen].
- **Changed:** Tips rewritten against the pack: 213 tips [335]. Tips on one feature merged into one tip; trivia removed.
- **Fixed:** Tips said anvils turn 'Too Expensive', Illusioners never spawn and Shulker shells drop from End Cities.

## Interface

- **Fixed:** Opening chat with / sometimes typed a second / into the box (T sometimes typed t).
- **Changed:** In JEI, fishing rods and backpacks each have their own group after the equipment materials [with each material]; Simple Hats and Wardrobe backpacks are not sorted.
- **New:** Title screen splash: Stience!
- **Changed:** Tooltips and screen text use American spelling: color, gray, labeled, fueled.

## Gameplay

- **Changed:** Mending repairs gear from collected XP [only by an anvil and a Mending book].
- **Changed:** Elytra fly in every dimension [the End only].
- **Fixed:** Rubber trees did not generate in jungles.
- **Fixed:** Placing a block or item against a hops vine, jungle grape vine, vanilla vine or mulberry vine replaced the vine.
- **Changed:** A planted Grapevine Lattice takes bone meal without a lattice below it.
- **Removed:** Baby rabbits no longer have a 5% chance to be killer rabbits.
- **Changed:** Golden armor, golden tools and golden food can be crafted with rose gold [gold only]. Only the recipes that make gold itself still need gold.

## Performance & Stability

- **Fixed:** Worlds did not load with Creative Delight but without Creative Brewing or Corn Delight.
- **Fixed:** On Linux the game closed without a crash report while opening its window when 420 or more mods were installed.
- **Fixed:** Tags, food types and recipes for Cozy's Improved Cats, Towns and Towers, Hopo Better Underwater Ruins, Unwrecked Ships and Yuushya were skipped as if those mods were not installed.

## Mods Added & Removed

- **New:** Packet Fixer, on clients and the server. Items, block contents and chunks of up to 64 MB are accepted [2 MB].
