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

- **New:** Tip bar on the world loading screen; a new tip every 7 seconds, from 335 tips
- **Changed:** Grass, Tall Grass, Ferns and Large Ferns no longer show growing seasons in tooltips and WTHIT; grass-type blocks that spread are off in winter only [summer and the wet and dry seasons]
- **Changed:** JEI, WTHIT, tooltip, toast and container title settings are in Creative Compat's config file, armor bar overflow settings in Creative Equipment's [Creative Common's]. Edited values carry over.

## Decoration <!-- icon: minecraft:painting -->

- **Changed:** The Immersive Paintings Datapacks page lists the paintings of every installed mod, including painting mods added later; the Creative Compat config can add paintings from other mod jars and resource packs
- **Fixed:** Neapolitan banner patterns showed nothing on Bedspreads beds and Customizable Elytra wings; patterns from mods added later show too

## Jams <!-- icon: vintagedelight:cherry_jam_jar -->

- **Changed:** Fruits Delight jams cook for 20 seconds in the Cooking Pot [10 seconds], matching every other jam
- **Fixed:** The new jam jars stack to 64

## Pickling Jar <!-- icon: vintagedelight:fermenting_jar -->

- **Fixed:** The Pickling Jar started a batch with several of a recipe's ingredients stacked in one slot and took more than one item from that slot; each ingredient needs its own slot, and every batch takes one item from each

## Creative Brewing <!-- icon: minecraft:potion -->
- **Changed:** The pack's own potions and Magic Resistance are listed under Creative Common; Long Reach and Phantom Bane potions and potion buckets under Creative Brewing. Existing potions (bottles, tipped arrows, bucket and tank contents), buckets and active Magic Resistance carry over.
- **Changed:** Magic Resistance effect icon uses the Resistance icon.
- **Changed:** Mod list: "Vinery (Creative Brewing)" [Let's Do Vinery (Creative Brewing)] and "Foundation (Creative Brewing)" [Creative Brewing].
- **Changed:** Farmer's Delight Tree Bark is no longer used in any recipe or as compost [Pale Jane, compost, soil and leather recipes accepted it]; Immersive Weathering bark is used instead.

## Creative Delight <!-- icon: farmersdelight:cooking_pot -->
- **Changed:** The pack's drink and cooking fluids (juices, teas, coffees, Egg Solution, Frosting) are listed under Creative Delight [Creative Engineering]. Fluid in tanks, basins and pipes carries over.
- **Changed:** Cooking for Blockheads Additions and Incubation Additions are part of Creative Delight and no longer listed as separate mods; placed and held racks, fruit baskets, nests and egg crates carry over. Their settings are in Creative Delight's config file.
- **Fixed:** Creating or loading a world failed with a datapack error when Myrtrees or Fruits Delight was not installed

## World Generation <!-- icon: minecraft:grass_block -->
- **Changed:** Floating sky islands also generate on their own [only around sky villages]

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

## Portal Gun <!-- icon: portalgun:portal_gun -->
- **New:** Dye the Portal Gun's portals in a crafting grid: a dye left of the gun colours the blue portal, right of it the orange portal, below or above it both. Several dyes mix. Placed portals change colour when shot again.
- **Fixed:** Walking out of a portal on a wall lifted the player one block.
- **Fixed:** A Portal Gun in the hotbar faded itself and every slot left of it.
- **Fixed:** Portal Gun portals and mirrors showed no name.

## Performance & Stability <!-- icon: minecraft:clock -->

- **Changed:** Distant Horizons draws distant terrain to 64 chunks [192] on new installs; existing settings are kept.
- **Changed:** Xaero's World Map takes block colours from the loaded textures instead of reading texture files from disk.

## Commands

- **Changed:** Operator commands renamed: `/ccswap` [`/ceswap`], `/ccaltime` [`/cetime`], `/cwglocate` [`/celocate`], `/cwgspawnbox` [`/cespawnbox`]
