# Creative Compat

**Compatibility fixes and tweaks for third-party mods on Forge 1.19.2.** Creative Compat changes how other mods work together: Quark, Supplementaries, Handcrafted, Create, Create: Gravity, Alex's Mobs, Immersive Portals, Polymorph, Sophisticated Backpacks, Corpse, Snow! Real Magic, ItemPhysic, JEI, WTHIT, FTB Quests and about 50 more. A mod's tweaks only switch on when that mod is installed, and only at a version it was tested with. It also holds changes that need no other mod: melting ice and snow with salt, fire and lava, walking through crops, snow and vines, block walking speeds, falling snow layers, an iron golem farm cooldown, boat-only block friction and two flower pots in one block. Most of the changes to vanilla behavior have a switch.

***

## Mods that require Creative Compat

*   **Creative Engineering**: machines and engineering
*   **Creative Delight**: food, cooking and Farmer's Delight addons
*   **Creative Brewing**: brewing, alcohol and potions
*   **Creative Equipment**: armor, tools and gear
*   **Creative Magic**: magic mod integration
*   **Creative Loot**: loot, drops and trades
*   **Creative Worldgen**: world generation and wildlife
*   **Creative Calendar**: seasons and time

***

A line that names a mod in brackets only switches on when that mod is installed. Changes that alter another mod's code are tied to the versions given (the tested version up to the next minor version); with any other version they are turned off and one warning is logged, so an update to that mod never crashes the game. Lines without a bracket need no other mod.

Most changes to vanilla behavior have a switch in the <code>[mixins]</code> section of <code>config/creativecompat-common.toml</code>, or their own section, and are on by default. A switch in <code>[mixins]</code> is read at game start, so restart after changing one.

***

## <u><strong>Walking and movement</strong></u>

Block walking speeds, walking through plants and layers, speed effects and low gravity.

### Walking speed and surfaces

<div class="spoiler"><ul><li><strong>Block walking speed:</strong> Every block belongs to a surface tier and changes how fast you walk on it: asphalt 1.5 times, concrete 1.4, grating 1.3, metal, ice, cut stone and bricks 1.25, trodden paths 1.15, wood 1.1, raw rock 1.05, plain blocks 1.0, then rough ground 0.92, soft blocks 0.88, sand and dust 0.85, farmland 0.8, leaves 0.78, snow 0.75, sticky pistons 0.65, mud and clay 0.6, and slime and honey blocks 0.45. Tier members are block tags, so any mod's blocks can be sorted. Speeds are in <code>[surface.speed]</code>; the server's values are sent to players. The field of view no longer changes with these speeds (client; switch: <code>[mixins] passFov</code>).</li><li><strong>Framed and copycat blocks:</strong> Framed and copycat blocks take the walking speed of the block they copy (the one inserted most recently in a mixed block). Switch: <code>[surface.camouflage] copyCamo</code>.</li><li><strong>Framed and copycat camo (Framed Blocks 6.8.x, Copycats+ 2.2.x):</strong> Double-material blocks order their camo by most recent, for walking speed; flower pots in frames can be unframed.</li><li><strong>Sinking:</strong> Entities standing on full soft, dust, spongy or very sticky blocks are drawn slightly lower, and so is the camera, like soul sand. Only the picture changes; hitboxes and movement do not. Setting: <code>[surface.sinking] depthPixels</code> (client).</li><li><strong>Boat friction:</strong> A block list sets how slippery blocks are for boats only; players and other entities keep the block's own slipperiness. Section: <code>[boat_friction]</code>.</li></ul></div>

### Walking through plants and layers

<div class="spoiler"><ul><li><strong>Walking through plants:</strong> Crops, wild crops, vines, tall plants, sugar cane, ropes, cobwebs, berry bushes, snow layers, leaf piles and bamboo leaves slow you down and soften falls as you move through them, per tier in <code>[surface.passthrough.&lt;tier&gt;]</code>. Cobwebs can hold arrows, fireworks and items, and a surrounded web can be climbed. Switch: <code>[surface.passthrough] enabled</code>.</li><li><strong>Snow layers:</strong> Snow layers can be walked through, fall like sand (Snow! Real Magic handles falling snow instead when it is installed), and are replaced by a placed block up to a set depth (<code>replaceMaxLayers</code>, default 3). Dust particles puff from soft, dusty blocks walked in or on (<code>[surface.passthrough] dustParticles</code>). Jumps are weaker inside deep snow and leaf layers, mobs sunk in snow climb out when they walk into a block, and mobs do not path through deep leaf piles.</li><li><strong>Layers (Immersive Weathering 1.2.x):</strong> Leaf piles, sand layers and azalea flowers behave like other layers (azalea flowers with Twigs).</li><li><strong>Ash and fodder layers (Supplementaries 2.4.20):</strong> Ash and fodder layers behave like snow layers.</li><li><strong>Sawdust layers (Immersive Engineering 9.2.4):</strong> Sawdust layers fall and can be walked through.</li><li><strong>Chakrams (Botania 440):</strong> Chakrams pass through plants.</li><li><strong>Pickarang and torch arrows (Quark 3.4):</strong> Pickarang and torch arrow handling adjusted for pass-through blocks.</li></ul></div>

### Speed and field of view

<div class="spoiler"><ul><li><strong>Speeds and field of view (BlockRunner 4.2.x):</strong> Speeds come from <code>[surface.speed]</code>, and the field of view stays steady.</li><li><strong>Fleet-Footed per player (Alex's Mobs 1.21.1 to 1.21.x):</strong> Fleet-Footed's +20% speed while sprinting in the air kept its countdown in one field shared by everyone with the effect, so one sprint-jumping player kept the speed on for all of them. The countdown is now kept per entity. If an Alex's Mobs update changes the code this touches, the fix turns itself off with one warning.</li><li><strong>Fleet-Footed field of view (Alex's Mobs):</strong> Client side. Fleet-Footed's speed bonus widens the field of view like any speed change; only a share of that widening is kept (default 0.25, 0.0 to 1.0). Other speed changes widen the view as usual. Setting: <code>[fleet_footed] fov</code>, carried over from Raspberry Pie's old key.</li></ul></div>

### Low gravity

<div class="spoiler"><ul><li><strong>Low gravity for falling and flying things (Create: Gravity 1.2.x):</strong> Wherever Create: Gravity gives a biome low gravity (the Creative Engineering pack lists the End), the same pull reduction applies to dropped items, falling blocks, lit TNT, minecarts, experience orbs, fishing bobbers, arrows, tridents, thrown items (snowballs, eggs, ender pearls, potions and the like), llama spit, Shulker bullets and boats out of water. Server and client work it out alike, so no extra network traffic is sent. Modded entities with their own gravity code are not affected. Only the dimensions in Creative Common's <code>[perf] gravityDimensions</code> are checked (default the End).</li><li><strong>Low gravity from the first server tick (Create: Gravity 1.2.x):</strong> On a dedicated server Create: Gravity's low gravity works from the moment the server starts; without this fix it only began after a <code>/reload</code>.</li><li><strong>Dropped items with ItemPhysic (Create: Gravity 1.2.x, ItemPhysic 1.6.7 or newer in 1.6.x):</strong> With ItemPhysic, dropped items take its own fall pull scaled by the same low gravity.</li><li><strong>Air supply tags (Create: Gravity 1.2.x with Create):</strong> Every backtank that Create accepts as an air source (Create Goggles' backtanks included) and the two goggle diving helmets count for Create: Gravity's low-oxygen biomes.</li></ul></div>

***

## <u><strong>Melting, snow and dropped items</strong></u>

Salt and heat melt ice and snow, Snow! Real Magic's snowy blocks, and what dropped items do in water, fire and lava.

### Melting and fire

<div class="spoiler"><ul><li><strong>Melting:</strong> Salt, fire, lava and magma melt ice and snow. Snow melts one layer at a time; ice becomes water (air in the Nether); packed ice and cryosol melt slowly, one tier down at a time, and blue ice and permafrost only by touching lava. Fire, torches, lanterns and glowstone reach 3 blocks, magma 3 (snow within 2 melts at once), lava 4, salt only the 6 touching blocks. Salt used on snow removes 4 layers; a salt block removes 1, 2 or 3 layers per melt (55%, 35% and 10%). Ice and snow also melt on their own in the Nether and in hot biomes (Creative Calendar's arid, dry and tropical season types). Snow, snow blocks and powder snow placed in the Nether turn to air at once. No snow forms and no water freezes near magma. Vanilla's light melting is off by default. Every number is in the <code>[melting]</code> config section; the key is carried over from Creative Common's file when a pack updates. Switches in <code>[mixins]</code>: <code>meltingBlockState</code>, <code>biomeMelt</code>, <code>vanillaLightMelt</code>.</li><li><strong>Frozen blocks of other mods (Immersive Weathering 1.2.x, Architect's Palette, Rare Ice):</strong> Their snowy and frosty blocks melt with the same rules and turn into their plain versions (snowy stone into stone, frosty glass into glass and so on); Rare Ice's own light melting follows the melting settings.</li><li><strong>No fire in the End:</strong> Ordinary fire cannot be placed in the End (flint and steel, fire charges, lava, End crystals, commands), and fire already there goes out on its next tick. Soul fire is not affected. In the End every melting source only melts the blocks touching it. Settings: <code>[melting] endNoFire</code>, <code>endContactOnly</code>; switch <code>[mixins] endFire</code>.</li><li><strong>Saltable tooltip:</strong> Items of blocks that salt can melt say "Saltable" in their tooltip (client).</li></ul></div>

### Snow! Real Magic

<div class="spoiler"><ul><li><strong>Melting snowy blocks (Snow! Real Magic 6.5.x):</strong> Snowy plants, fences, gates, walls, slabs and stairs melt from heat and salt back into the block under the snow; a snowy plant loses one layer at a time. Snow on a plant in the Nether melts at once and leaves the plant, as does rain that melts it (Immersive Weathering). Magma stops snow from falling near it, and Snow! Real Magic's own ice melting is off.</li><li><strong>Walking and grass (Snow! Real Magic 6.5.x):</strong> Snowy plants walk through like snow layers, and a snowy berry bush slows like a berry bush. Snow-topped stairs, slabs, fences and walls have the walking speed of the block under the snow. Grass with snow on it does not spread onto dirt.</li><li><strong>Scooping and snowy looks (Snow! Real Magic 6.5.x):</strong> A shovel takes 2 snow layers and drops 2 snowballs; sneaking with an empty hand takes 1. A plain empty-hand click hides the snowy look while the snow stays, until new snow falls or is placed. Snow drips down the open sides of the block under it. WTHIT names snowy blocks, such as Snowy Oak Stairs.</li><li><strong>Blizzards and ice (Snow! Real Magic 6.5.x):</strong> In new worlds, thunderstorms in snowy places drop clumps of 1 or 2 snow layers, never onto a full 8 layers. Falling snow that lands in water freezes it 1 time in 5.</li></ul></div>

### Dropped items and shards

<div class="spoiler"><ul><li><strong>What floats and what burns (ItemPhysic 1.6.7):</strong> Dropped items float or sink and burn or survive fire and lava by what they are made of, sorted into item tags by material (<code>#creativecompat:physics/family/...</code>) covering the Creative Engineering pack's mods; items outside those tags sink and burn. Wood, food, plants, fiber, light powders, ice and packed blocks float. Metal, machines, stone, earth, crystals, containers and powders survive fire. Nether wood and refractory materials also survive lava. Pack scripts and tags can change any of it.</li><li><strong>Resting on water, lava and snow (ItemPhysic 1.6.7):</strong> Leaves, flowers, seeds, wool, paper, feathers, dyes and other light items rest on the water surface; other floating items dip once and rest just below it. Items that survive lava rest on top of it, while heavy mineral blocks sink. Light items also rest on top of snow layers and other layers. Section: <code>[item_physics]</code>.</li><li><strong>Burn results (ItemPhysic 1.6.7):</strong> Wooden items leave ash 1 time in 4 (Supplementaries), burned glass leaves dirty glass or shards (Quark), and TNT, gunpowder and modded explosives blow up without breaking blocks. Allurement curses on dropped items are handled.</li><li><strong>Glass shards (Quark):</strong> Every glass block and pane in the glass tags, modded ones included, breaks into 1 to 3 shards (panes 1 to 2) of its color without Silk Touch; Quark's own glass drops are unchanged, and tinted glass drops Quark's dirty shard instead of itself. Creative Loot also puts shards in mid-tier structure chests.</li></ul></div>

***

## <u><strong>Storage, crafting and furniture</strong></u>

Sacks and jars, crafting and trash, flower pots and planters, furniture and Create.

### Sacks and jars

<div class="spoiler"><ul><li><strong>Sack sneak pick-up (Supplementaries 2.4.20; Supplementaries Squared for the dyed sacks):</strong> Sneak and right-click a placed sack with an empty main hand to pick it up with its contents, as a placed Sophisticated Backpack does. The sack's screen does not open then. Break protection (claims, spawn protection) is respected.</li><li><strong>Sacks like bundles (Supplementaries 2.4.20):</strong> In inventory screens, right-click a filled sack with an empty cursor to take out its first stack, and scroll over it to rotate its contents.</li><li><strong>Jars (Supplementaries 2.4.20):</strong> Jars show their contents in WTHIT and tooltips, fill and mix, and Create pumps and other fluid pipes can fill and drain them (and goblets).</li><li><strong>Dyeing keeps contents (Supplementaries 2.4.20):</strong> Supplementaries' dye blocks tweak no longer dyes blocks that hold data (sacks, shulker boxes, banners, beds, signs and the like) in the world, so it cannot delete their contents; the click does the block's own action instead. Plain blocks still dye in the world.</li></ul></div>

### Crafting, sorting and trash

<div class="spoiler"><ul><li><strong>Recipe choice (Polymorph 0.46.x):</strong> Recipe choice works in Sophisticated Backpacks' crafting upgrades (with Sophisticated Core 0.6.4), Immersive Engineering's crafting table and Pretty Pipes' crafting terminal.</li><li><strong>Safe sorting (Quark 3.4):</strong> Sorting no longer loses items.</li><li><strong>Trash slot (TrashSlot 12.1.x):</strong> The trash slot can be turned on or off per screen and moved; each Sophisticated Backpacks tier remembers its own position. Section: <code>[trash_slot]</code>.</li><li><strong>Trash can screen (Trash Cans 1.0.18):</strong> A right-click on the Item Trash Can opens a 27-slot screen whose contents are deleted when it closes. Sneak and right-click with an empty hand opens the filter. The other trash cans are unchanged.</li><li><strong>Vein mine undo (Ore Excavation 1.11.x):</strong> <code>/undo_excavation</code> has a cooldown (5 seconds) and refuses when the vein mine broke ores, storage blocks, spawners or blocks with inventories. Section: <code>[vein_undo]</code>.</li></ul></div>

### Flower pots and planters

<div class="spoiler"><ul><li><strong>Two flower pots in one block:</strong> Use a flower pot on a placed vanilla-style flower pot (without sneaking) to put two pots in one block, the second one taller. Each takes its own plant, picked by the half you click, and the block breaks back into the two pots and their plants.</li><li><strong>Pottable tooltip:</strong> Items that a flower pot accepts say "Pottable" in their tooltip (client).</li><li><strong>Flower and planter boxes (Supplementaries 2.4.20, Another Furniture 2.1.x):</strong> Flower boxes and planter boxes take and draw more plants, such as glow berries and glow lichen.</li><li><strong>Planter models (Another Furniture 2.1.x):</strong> Planter models draw their plants (client).</li><li><strong>Botanist workbench as a flower pot (Beautify 1.4.x):</strong> The botanist workbench takes any plant a flower pot takes. Workbenches placed by structures start with a flower.</li><li><strong>Botany Pots crops (Botany Pots 9.0.x):</strong> Pot crops are added for many modded plants, and Nether and End plants only grow in their own dimension.</li><li><strong>Silt pots and bamboo leaves (Twigs 3.0.x):</strong> Twigs' silt pots take their soil. Bamboo leaves can be walked through.</li></ul></div>

### Furniture and decoration

<div class="spoiler"><ul><li><strong>Nightstands (Handcrafted 2.0.x):</strong> Nightstand storage and held items save correctly.</li><li><strong>Painting variants (Immersive Paintings 0.6.x):</strong> Every painting variant of the installed mods is offered, built at load from their own art; old painting ids are kept. Section: <code>[paintings]</code>.</li><li><strong>Banner patterns (Bedspreads 6.0.x, Customizable Elytra 1.7.x):</strong> Their banner patterns are built at load from the installed jars.</li><li><strong>Wind vane crash (Supplementaries 2.4.20 with Breezy 1.0.8):</strong> A wind vane crash is fixed.</li></ul></div>

### Create

<div class="spoiler"><ul><li><strong>Block breaker and config screen (Create 0.5.1):</strong> Create's block breaker skips Quark monster boxes (with Quark); the Create config screen can open sections.</li><li><strong>Jetpack tooltip (Create: Dreams &amp; Desires 1.2c):</strong> Jetpack tooltip fixed.</li><li><strong>Spout Gun waters farmland (Create: Misc and Things):</strong> The water Spout Gun soaks the farmland it hits, including farmland under a crop.</li></ul></div>

***

## <u><strong>Animals, villagers and corpses</strong></u>

Cats, squid and whales, villages and monster boxes, and corpses and worn gear.

### Animals

<div class="spoiler"><ul><li><strong>Feather on a Stick (Domestication Innovation 1.6.x):</strong> The Feather on a Stick can hook mobs. Hostile mobs on by default; neutral and passive mobs and players off. Section: <code>[cats]</code>.</li><li><strong>Cozy cats (Cozy's Improved Cats 2.1.x):</strong> With Cozy's gender system off, the leftover gender lines are hidden, and its two fisherman trades are removed. With Domestication Innovation, Cozy's modes replace its follow, wander and stay right-click on Cozy cats.</li><li><strong>Cats and feathers (Domestication Innovation with Cozy's Improved Cats 2.1.x):</strong> Cats follow and attack a held feather, and get a buff from it (10 seconds, level 3 by default).</li><li><strong>Squid flee whales (Alex's Mobs 1.21.x):</strong> Squid and glow squid in water swim away from any cachalot whale within 16 blocks and keep going until it is 24 blocks off. Orcas are not fled. Vanilla squid otherwise flee only what hurt them.</li><li><strong>Giant squid fight whales (Alex's Mobs 1.21.x):</strong> Giant squid no longer flee whales from 50 blocks away. They target a whale that hurt them, or any whale within 12 blocks, then swim in and grab it. Cachalot whales still hunt giant squid, squid and glow squid as before.</li></ul></div>

### Villages, golems and monster boxes

<div class="spoiler"><ul><li><strong>Iron golem farm cooldown:</strong> When 5 iron golems die in one village within 10 minutes, the village summons no golems for 1 in-game day. Golems built by players neither count nor are affected. Section: <code>[golems]</code>.</li><li><strong>Villager paths and combs (MCA Reborn 7.5.x):</strong> Villagers path over leaf piles; eye colors on combs fixed.</li><li><strong>Monster boxes as vaults (Quark 3.4):</strong> Monster boxes can act as vaults: a reward and a cooldown of 1 hour, one wave at a time. Section: <code>[monster_box]</code>.</li></ul></div>

### Corpses and worn gear

<div class="spoiler"><ul><li><strong>Corpse decay (Corpse 1.0.17):</strong> Corpses decay by game time, also while unloaded: a corpse holding items becomes a skeleton after 7 days (1 day in the Nether, then a wither skeleton), an emptied one after 30 seconds and is gone after 5 minutes; in the End it never decays. Skeletons sink in water and lava. Section: <code>[corpses]</code>.</li><li><strong>Corpse screen (Corpse 1.0.17 with Curios 5.1.x):</strong> The corpse screen shows cosmetic armor and curios in their own slots, and both models wear them. A corpse with an empty main inventory still opens this screen while it holds curios or cosmetic armor. Transfer Items returns curios to their slots.</li><li><strong>Hats (Simple Hats 0.3.x):</strong> Masks, the chalk stick and the smoking pipe can go in the Curios face slot, and the Falconry Glove in a hands slot.</li></ul></div>

<!-- screenshot: the Corpse screen with its curio slots -->

***

## <u><strong>World, maps and portals</strong></u>

Block and plant details, fog and distant terrain, and portals.

### Blocks and plants

<div class="spoiler"><ul><li><strong>Moss color:</strong> Moss blocks and moss carpet take the biome's grass color (client). Section: <code>[moss]</code>.</li><li><strong>Sheared vines:</strong> Shears on the tip of a weeping, twisting or cave vine or on kelp stop it growing, as in 1.20. Section: <code>[vine_shearing]</code>.</li><li><strong>TNT shrapnel:</strong> A primed or burning TNT also flings out a burst of fast projectiles (default 24, 3 damage). Section: <code>[shrapnel]</code>.</li><li><strong>Falling blocks splash:</strong> Falling blocks splash when they enter water (client).</li></ul></div>

### Fog, maps and distant terrain

<div class="spoiler"><ul><li><strong>Nether fog color:</strong> Client side, off by default. When on, the whole Nether uses one deep red fog color instead of each biome's own. Lava, water, powder snow, Blindness and Darkness are left alone. Section: <code>[nether_fog]</code>.</li><li><strong>Clouds and the Nether (Distant Horizons 3.0.x):</strong> Distant Horizons' clouds follow the vanilla cloud height. In the Nether it generates no unvisited chunks, draws to 32 chunks and keeps a fog at the edge.</li><li><strong>Map colors (Xaero's World Map 1.40.x):</strong> Map block colors fixed.</li></ul></div>

### Portals

<div class="spoiler"><ul><li><strong>Rendering limits (Immersive Portals 2.3.x):</strong> Mirror and portal render limits and fog settings. Walking out of a wall portal no longer lifts the player a block. Section: <code>[immersive_portals]</code>.</li><li><strong>Portal Gun (Portal Gun 5.0.x):</strong> Opaque portals optional, a despawn distance and heartbeat, a dye recipe and its JEI page. Section: <code>[portalgun]</code>.</li></ul></div>

***

## <u><strong>Interface</strong></u>

JEI, WTHIT, tooltips, screens and the HUD.

### JEI and recipe lists

<div class="spoiler"><ul><li><strong>JEI:</strong> Recipes whose + button works are listed first, fluids in tanks scale by a curve so small amounts stay visible, recipe and ingredient tooltips are fixed, and an optional pack ingredient order. Section: <code>[jei]</code>.</li><li><strong>Effect descriptions (Just Enough Effect Descriptions 2.1.x):</strong> Effect descriptions and icons.</li><li><strong>Berries:</strong> Sweet and glow berries are listed in the creative tabs and JEI again.</li></ul></div>

### WTHIT and tooltips

<div class="spoiler"><ul><li><strong>WTHIT:</strong> Lines for jar and pot contents, beehives, block state, walking speed, silk touch and sheared drops, operator blocks, campfires and more; each has a switch in <code>[wthit]</code>.</li><li><strong>Tooltips (FTB Library 1902.4.x, FTB Quests 1902.5.x, Patchouli 77, Applied Energistics 2 12.9.x, Wares 1.2.x, Pretty Pipes 1.13.x, Lightman's Currency 2.2.x, Legendary Tooltips 1.4.x with Iceberg):</strong> Item tooltips keep their top on screen and use the pack's tooltip lines; Lightman's Currency screens and Legendary Tooltips title breaks are fixed.</li></ul></div>

<!-- screenshot: the WTHIT overlay on a Supplementaries jar or a hive -->

### Screens and HUD

<div class="spoiler"><ul><li><strong>Screen and interface details:</strong> Client: a tooltip taller than the screen keeps its top visible (<code>[tooltips]</code>), the world behind screens can be dimmed (<code>[screens]</code>), tutorial and advancement toasts can be hidden (<code>[toasts]</code>), sign text is not drawn far from the camera, and resource pack title plaques stretch to long container titles (<code>[gui_title]</code>).</li><li><strong>Animated items in the hotbar (GeckoLib 3.1.x with ImmediatelyFast 1.4.x):</strong> Animated items in the hotbar no longer break the HUD.</li></ul></div>

***

## <u><strong>Optional pack settings</strong></u>

<div class="spoiler"><p>Out of the box, the tweaks are on unless the table says otherwise. Any pack can change these in <code>config/creativecompat-common.toml</code>:</p><table><thead><tr><th>Setting</th><th>What it does</th></tr></thead><tbody><tr><td><code>[mixins]</code></td><td>One on/off key per change that alters vanilla behavior (36 keys); restart to apply</td></tr><tr><td><code>[nether_fog]</code></td><td>One deep red fog color for the whole Nether (client); off by default</td></tr><tr><td><code>[melting]</code></td><td>Reach, chance and speed of melting by source, the block lists for what melts into what, hot places, and the End rules (<code>endContactOnly</code>, <code>endNoFire</code>)</td></tr><tr><td><code>[surface.speed]</code></td><td>Walk speed per surface tier (0.05 to 4.0)</td></tr><tr><td><code>[surface.passthrough]</code></td><td>Master switch, arrow lifetime (12000 ticks) and one table per plant tier: walk and fall factors, neighbor rule, items and projectiles</td></tr><tr><td><code>[item_physics]</code></td><td>With ItemPhysic: resting items on water, lava and layers, and the motion numbers</td></tr><tr><td><code>[golems]</code></td><td>Deaths (5), window (12000 ticks) and cooldown (24000 ticks) of the golem farm limit</td></tr><tr><td><code>[boat_friction]</code></td><td>Block list for boat friction</td></tr><tr><td><code>[shrapnel]</code></td><td>Count, speed, damage, item and range of TNT shrapnel</td></tr><tr><td><code>[moss]</code>, <code>[vine_shearing]</code>, <code>[gui_title]</code>, <code>[toasts]</code>, <code>[screens]</code>, <code>[tooltips]</code></td><td>Moss color, vine shearing, title plaques, toast filter, screen dimming and tooltip placement</td></tr><tr><td><code>[fleet_footed] fov</code></td><td>With Alex's Mobs (client): how much of the field-of-view widening from Fleet-Footed's speed bonus is kept (0.25; 1.0 = unchanged, 0.0 = none)</td></tr><tr><td><code>[jei] pagesOff</code></td><td>Recipe pages that other Creative mods gather (seasoning, cans, potion buckets, tipped arrows) to leave off; empty by default</td></tr><tr><td><code>[jei] craftableFirst</code>, <code>packOrder</code>, <code>fluidAmountScale</code></td><td>JEI recipe order, ingredient order from the pack's definition (off by default) and how full fluids look in tanks</td></tr><tr><td><code>[corpses]</code></td><td>Decay times and sinking of Corpse mod corpses, and the sectioned corpse screen</td></tr><tr><td><code>[trash_slot]</code>, <code>[monster_box]</code>, <code>[cats]</code>, <code>[vein_undo]</code>, <code>[paintings]</code></td><td>Settings for the matching mod changes above</td></tr><tr><td><code>[wthit]</code></td><td>One switch for each added WTHIT line</td></tr><tr><td><code>[immersive_portals]</code>, <code>[portalgun]</code></td><td>Portal render distances, fog and Portal Gun portal settings</td></tr><tr><td><code>[debug]</code></td><td>Developer switches, all off</td></tr></tbody></table><p>Low gravity has no switch of its own: it follows Create: Gravity's biome settings and Creative Common's <code>[perf] gravityLimitDimensions</code> and <code>gravityDimensions</code>. Sack pick-up and glass shards have no switch either. With Create installed, the file can also be edited in game through Create's config screen.</p></div>

***

## <u><strong>Requirements</strong></u>

|                        |Version                               |
| ---------------------- |------------------------------------- |
| Minecraft              |1.19.2                                |
| Forge                  |43 or newer                           |
| Creative Common        |1.3.0 or newer                        |
| KubeJS                 |1902.6.2-build.73 or newer (1902.6.x), required by Creative Common |
| Rhino and Architectury |required by KubeJS                    |

Everything else is optional; the mod each change needs is named in brackets in the families above.

Both client and server need it. Client-only parts (tooltips, JEI and WTHIT changes, rendering) do nothing on a dedicated server, and server-only parts do nothing on the client.

***

## <u><strong>Source, issues and license</strong></u>

*   Source code and issue tracker: [github.com/SaintAlfonzo/CreativeEngineering](https://github.com/SaintAlfonzo/CreativeEngineering)
*   License: GNU GPL v3 (GPL-3.0-only). Third-party notices for included art are in the jar.
*   Free to include in modpacks.
