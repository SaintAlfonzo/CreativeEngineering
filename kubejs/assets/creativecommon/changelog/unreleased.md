# Unreleased

## Barbeque's Delight <!-- icon: barbequesdelight:grill -->

- **Changed:** Kebab Wrap, Kebab Sandwich and Bibimbap are ordinary crafts (the wrap and sandwich also a sequenced assembly) and no longer carry the effects or seasonings of the skewers in them
- **Changed:** Kebab Wrap 10 hunger, 0.6 saturation [8, 0.7]; Kebab Sandwich 16 hunger, 0.6 saturation [14, 0.7]; Bibimbap 0.8 saturation [0.7]
- **Changed:** A seasoning's tooltip reads the same as the line it gives a seasoned food

## Skewers <!-- icon: farmersdelight:barbecue_stick -->

- **New:** Raw Barbecue, End, Improvised, Nether, Seafood, Turnip Mutton, Crispy and Cave Skewers: crafted from raw ingredients and a stick (a Blaze Rod for the Nether Skewer), cooked on a Grill, in a furnace or smoker or on a campfire; powders season them
- **Changed:** Barbecue on a Stick, End Barbecue Stick and Improvised Barbecue Stick are named Barbecue Skewer, End Skewer and Improvised Skewer
- **Changed:** The Nether and Cave Skewer crafts make the raw skewer [the cooked skewer]
- **Changed:** The End Skewer is crafted one at a time from a Shulker Mollusk, Roasted Dragon Meat, Chorus Fruit and a stick [3 from Roasted Shulker Meat, Roasted Dragon Meat, Chorus Fruit, Dried Endermite Meat and 3 sticks]
- **Changed:** The Barbecue and Improvised Skewer crafts from cooked ingredients are shaped and by hand only [shapeless]
- **Changed:** Ender's Delight's Shulker Mollusk and Shulker Filet are separate items again: Shulkers killed with a Knife drop the Mollusk, and it cuts into Filets [merged into End's Delight Shulker Meat]; the Stuffed Shulker takes End's Delight Shulker Meat [Shulker Mollusk]

## Large Meals <!-- icon: largemeals:omurice_block -->

- **Changed:** Pufferfish Broth also gives Nausea for 5 seconds
- **New:** Canned Omurice, Canned Mushroom Pot Pie and Canned Roasted Rack of Mutton, filled from the placed feasts

## Roll Delight <!-- icon: roll_delight:amazing_salmon_roll -->

- **Changed:** Salmon Maki is named Salmon Maki Roll Slice
- **Changed:** Golden Roll 20 hunger, 0.6 saturation, no effects, not edible when full, stacks to 64 [4, 1.2, Regeneration II, Absorption, Nourishment, always edible, 16]; Golden Roll Slice 10 hunger, 0.5 saturation, stacks to 64 [2, 1.2, always edible, 16]
- **Changed:** Cooked Nether Wart no longer counts as a cooked vegetable

## Interface & Tooltips <!-- icon: minecraft:written_book -->

- **New:** Tip bar on the world loading screen; a new tip every 10 seconds, from 335 tips. Client option.
- **Changed:** Grass, Tall Grass, Ferns and Large Ferns no longer show growing seasons in tooltips and WTHIT; grass-type blocks that spread are off in winter only [summer and the wet and dry seasons]
- **Changed:** JEI, WTHIT, tooltip, toast and container title settings are in Creative Compat's config file, armor bar overflow settings in Creative Equipment's [Creative Common's]. Edited values carry over.
- **Fixed:** JEI crafting pages for repairing, banner and book copying, map cloning, armor and leather dyeing, firework rockets and firework stars gave errors in the log; recipes that leave different grid cells empty now have one page per layout
- **New:** Copper Pot tooltip: cooks some of what a Cooking Pot can, only fits 4 ingredients
- **New:** Grape seeds that grow on the ground say they need a full solid block such as grass or dirt, not farmland

## Decoration <!-- icon: minecraft:painting -->

- **Changed:** Redeco's Jar is called Empty Terrarium [Jar]
- **Changed:** The Immersive Paintings Datapacks page lists the paintings of every installed mod, including painting mods added later; the Creative Compat config can add paintings from other mod jars and resource packs
- **Fixed:** Neapolitan banner patterns showed nothing on Bedspreads beds and Customizable Elytra wings; patterns from mods added later show too

## Jams <!-- icon: vintagedelight:cherry_jam_jar -->

- **Changed:** Fruits Delight jams cook for 20 seconds in the Cooking Pot [10 seconds], matching every other jam
- **Fixed:** The new jam jars stack to 64

## Pickling Jar <!-- icon: vintagedelight:fermenting_jar -->

- **Fixed:** The Pickling Jar started a batch with several of a recipe's ingredients stacked in one slot and took more than one item from that slot; each ingredient needs its own slot, and every batch takes one item from each

## Creative Brewing <!-- icon: minecraft:potion -->
- **Changed:** Shorter Grape Planting and Wine Aging texts in JEI
- **Changed:** The pack's own potions and Magic Resistance are listed under Creative Common; Long Reach and Phantom Bane potions and potion buckets under Creative Brewing. Existing potions (bottles, tipped arrows, bucket and tank contents), buckets and active Magic Resistance carry over.
- **Changed:** Magic Resistance effect icon is the Resistance icon in purple
- **Changed:** Mod list: "Vinery (Creative Brewing)" [Let's Do Vinery (Creative Brewing)] and "Foundation (Creative Brewing)" [Creative Brewing].
- **Changed:** Farmer's Delight Tree Bark is no longer used in any recipe or as compost [Pale Jane, compost, soil and leather recipes accepted it]; Immersive Weathering bark is used instead.

## Creative Delight <!-- icon: farmersdelight:cooking_pot -->
- **Changed:** Refined Gelatin is called Gelatin
- **Changed:** Rotten Ham and Cooked Rotten Ham are no longer raw or cooked meat; Rotten Flesh and both hams are Rotten Meat
- **Changed:** The pack's drink and cooking fluids (juices, teas, coffees, Egg Solution, Frosting) are listed under Creative Delight [Creative Engineering]. Fluid in tanks, basins and pipes carries over.
- **Changed:** Cooking for Blockheads Additions and Incubation Additions are part of Creative Delight and no longer listed as separate mods; placed and held racks, fruit baskets, nests and egg crates carry over. Their settings are in Creative Delight's config file.
- **Fixed:** Creating or loading a world failed with a datapack error when Myrtrees or Fruits Delight was not installed

## World Generation <!-- icon: minecraft:grass_block -->
- **Changed:** Low gravity in the End also applies to falling blocks, dropped items, lit TNT, experience orbs, arrows, tridents, thrown items, boats and minecarts [players and mobs only]
- **Fixed:** Every backtank supplies air in the End, not only the copper and netherite ones
- **Fixed:** Low gravity in the End was missing on dedicated servers after every restart until the data was reloaded
- **Changed:** Floating sky islands also generate on their own [only around sky villages]
- **Changed:** Glow lichen generates about three times as often in caves, in larger patches [single spots or pairs]
- **New:** A second, more common Cantaloupe and Mini Cantaloupe patch in deserts and badlands

## Crops <!-- icon: minecraft:wheat -->

- **Changed:** Plain Dandelions mature about every 10 minutes on Rich Soil and every 5 on Fertile Soil [20 and 10]
- **New:** 3 Rich Soil craft 1 Fertile Soil
- **Changed:** Weeds can be planted on Rich Soil Farmland; they still never grow there or on Rich Soil or Fertile Soil by themselves
- **Fixed:** Right-clicking ripe Weeds replanted them; right-click now does nothing, break them to harvest
- **Changed:** Pineapples can be planted on Farmland, Rich Soil Farmland and Fertile Soil [sand, red sand and coarse dirt only]
- **Changed:** Tea bushes can be planted on Farmland, Rich Soil Farmland and Fertile Soil
- **Fixed:** Right-clicking the top half of ripe corn did nothing; it now harvests like the lower half
- **New:** A water bucket used on Farmland, Rich Soil Farmland or Fertile Soil soaks a 3x3 area and leaves an empty bucket; a water bottle soaks one block and leaves a glass bottle

## Bees <!-- icon: minecraft:beehive -->

- **Changed:** Shearing a full beehive or bee nest gives 6 honeycombs [3]

## Cans <!-- icon: canned_goods:canned_food -->

- **New:** Every feast can be canned: the placed feast and an empty can make a can of 6 servings
- **New:** Canned feasts: Pizza, Cheese Pizza, Meat Pizza, Vegan Pizza, Lasagna, Stuffed Bell Peppers, Stuffed Cantaloupe, Dragon Meat Stew, Grilled Shulker, Steamed Dragon Egg, Guardian Soup
- **New:** Canned feasts: Fake Meatloaf, Glazed Arachnid Limbs, Stuffed Squid, Rice Roll Medley, Legendary and Mythical Roll Medley, Rice Roll Royale, the three Bell Pepper Medleys
- **New:** Canned Cooked Nether Wart, Cantaloupe Bread, Gleaming Salad, Sweet Salad, Amberveil Stew, Cactus Chili, Cactus Soup, Calamari Soup, Potato Soup, Pufferfish Broth, Red Soup and Tomato Egg Soup
- **Changed:** Canned Oatmeal's second recipe takes sugar [salt or bone broth]

## Coffee <!-- icon: rusticdelight:coffee_beans -->

- **Changed:** Farmer's Respite coffee bushes already in a world break into Rustic Delight coffee items instead of growing; they no longer turn nearby crops into Wither Roots

## Azalea Flowers <!-- icon: immersive_weathering:azalea_flowers -->

- **Changed:** Twigs' Azalea Flowers merged into Immersive Weathering's Azalea Flowers
- **Changed:** Immersive Weathering's Azalea Flowers can be placed on floors, walls and ceilings

## Flax <!-- icon: minecraft:string -->

- **Removed:** Flax, Flax Seeds, Flax Bale and Wild Flax
- **Changed:** Supplementaries Rope is crafted from 3 string [flax]; the Sack from wheat and string [flax]
- **Removed:** Flax Seeds from chests and villager trades

## Botany Pots <!-- icon: botanypots:botany_pot -->

- **Changed:** Crops in Botany Pots grow 8 times slower; flowers 2 times slower
- **Removed:** Bone meal no longer speeds up Botany Pots
- **Fixed:** Lemon trees in Botany Pots showed a cut-off tree; they show the lower half at every stage
- **New:** Botany Pot tooltip: useful for flowers, not great for crops

## Cookware <!-- icon: farmersdelight:cooking_pot -->

- **New:** Cooking Pots, Copper Pots and Kettles broken or picked up by a player keep their ingredients, meal and output in the item and restore them when placed; leftover bottles and bowls drop
- **New:** A Cooking Pot, Copper Pot or Kettle item holding contents shows the enchantment glint and a Holds tooltip
- **Changed:** Picking up a Cooking Pot, Copper Pot or Kettle with sneak right-click keeps its contents in the item [emptied them into the inventory]
- **New:** Skillets hang on walls: sneak right-click the side of a block with space above; a hung Skillet does not cook

## Faucets <!-- icon: supplementaries:faucet -->

- **Changed:** Faucets sit against the side of jars and other narrow containers, and their stream ends on the top of the block below

## Azalea Wood <!-- icon: quark:azalea_log -->

- **Changed:** Restored azalea wood and every block made from it; azalea trees grow an Azalea Log trunk again [Oak Log]. Items already turned into Oak stay Oak
- **New:** Azalea Spice Rack, Tool Rack and Fruit Basket

## Tempad <!-- icon: tempad:tempad -->
- **New:** Tempad: save locations and open a timedoor to any of them, powered by chronons.
  - Chronon Cell, Chronon Battery, Chronon Generator and Chronometer store or make chronons.
  - Teleport, New Location, Settings and Portal Setup apps; upgrades for new locations and player teleport.
  - You keep your speed through a timedoor, turned to face the exit.
  - Workstation: holds a Tempad and opens a timedoor on a redstone signal.
  - Location Cards and a Card Wallet carry saved locations.
  - Location Broadcaster and Screening Device share your position with other players, including with your FTB team.
  - Timedoor Projector, Timedoor Marker and Chronomark: blocks that open or receive timedoors.
  - Metronome generates chronons; Time Twister and the Timeline app return you to recent locations.
  - Knowledge Projector handbook and a Knowledge Repository upgrade; Curios slots for Tempad gear; WTHIT lines.

## Portals <!-- icon: portalgun:portal_gun -->
- **New:** Dye the Portal Gun's portals in a crafting grid: a dye left of the gun colours the blue portal, right of it the orange portal, below or above it both. Several dyes mix. Placed portals change colour when shot again.
- **Fixed:** Walking out of a portal on a wall lifted the player one block.
- **Fixed:** A Portal Gun in the hotbar faded itself and every slot left of it.
- **Fixed:** Portal Gun portals and mirrors showed no name.
- **Fixed:** Ordinary fire lit Nether portals with Immersive Portals installed, and Flint and Steel lit frames smaller than vanilla's; the frame turns to Crying Obsidian again.
- **Changed:** Dark Fire lights see-through Nether portals [loading-screen portals].
- **Changed:** The view through a Nether portal reaches 32 blocks [8 chunks], with the portal tinted.
- **New:** Portal Gun portals close when their owner logs out, changes dimension or is more than 128 blocks away (checked every 10 seconds).
- **New:** Client option: Portal Gun portals drawn as solid discs in their colour, not see-through. Off by default.
- **New:** Fog through portals: Nether portals from 8 to 32 blocks past the portal, Portal Gun portals and mirrors from 16 to 32. Client options.
- **New:** More glass and panes make mirrors with Flint and Steel:
  - Tinted, Reinforced Tinted, Rainbow, Smooth Tinted and Dirty Glass.
  - Quark's Framed Glass, all colours; Create Framed's tinted glass.
  - Frosty Glass while sneaking (otherwise it melts the frost).
- **Changed:** Framed Blocks' panes and bars no longer make mirrors.
- **Changed:** Mirrors reflect only within 12 blocks, show at most 4 reflections inside each other, and show 32 blocks of world.
- **Changed:** Portal Gun portals show 32 blocks of world through them.
- **Changed:** Design and Decor's Tinted Framed Glass is renamed Smooth Tinted Glass (Horizontal and Vertical too).

## Performance & Stability <!-- icon: minecraft:clock -->

- **Changed:** Distant Horizons draws distant terrain to 64 chunks [192] on new installs; existing settings are kept.
- **Changed:** Xaero's World Map takes block colours from the loaded textures instead of reading texture files from disk.
- **Changed:** Distant Horizons no longer generates unexplored Nether terrain; Nether chunks you load are still shown.
- **Changed:** Distant Horizons draws Nether terrain to 32 chunks [64], with its fog fully thick at that edge.
- **Changed:** The Nether has one deep red fog colour everywhere [each biome's own colour].
- **Changed:** Sign text is not drawn more than 24 blocks away, except through a spyglass.
- **Changed:** Grass and foliage season colours on Xaero's World Map and in loaded chunks are worked out faster.
- **Fixed:** Better Fortresses and Better Dungeons filled the log with basalt column warnings while Distant Horizons generated the Nether.

## Rose Gold <!-- icon: mca:rose_gold_ingot -->

- **Changed:** Rose Gold counts as gold in every recipe that takes gold, except those that make gold itself and golden armor, tools and food: Gold ingots, nuggets, blocks, plates, rods, wires and dust accept it

## Commands

- **Changed:** Operator commands renamed: `/ccswap` [`/ceswap`], `/ccaltime` [`/cetime`], `/cwglocate` [`/celocate`], `/cwgspawnbox` [`/cespawnbox`]
