# Creative Compat

**Compatibility fixes and tweaks for third-party mods on Forge 1.19.2.** Creative Compat changes how other mods work together: Quark, Supplementaries, Handcrafted, Create, Create: Gravity, Alex's Mobs, Immersive Portals, Polymorph, Sophisticated Backpacks, Corpse, JEI, WTHIT, FTB Quests and about 50 more. A mod's tweaks only switch on when that mod is installed, and only at a version it was tested with. It also holds changes that need no other mod: melting ice and snow with salt, fire and lava, walking through crops, snow and vines, block walking speeds, falling snow layers, an iron golem farm cooldown, boat-only block friction and two flower pots in one block. Most of the changes to vanilla behavior have a switch.

***

## Requirements

|                        |Version                               |
| ---------------------- |------------------------------------- |
| Minecraft              |1.19.2                                |
| Forge                  |43 or newer                           |
| Creative Common        |1.3.0 or newer                        |
| KubeJS                 |1902.6.2-build.73 or newer (1902.6.x), required by Creative Common |
| Rhino and Architectury |required by KubeJS                    |

Everything else is optional; see "Optional mods: what changes with each" below.

Both client and server need it. Client-only parts (tooltips, JEI and WTHIT changes, rendering) do nothing on a dedicated server, and server-only parts do nothing on the client.

***

***

## What it does on its own

These features need no other third-party mod. Each feature has a bold name and a dropdown with its description. Most that change vanilla behavior have a switch in the `[mixins]` section of `config/creativecompat-common.toml`, or their own section, and are on by default. A switch in `[mixins]` is read at game start, so restart after changing one.

### Walking and movement

**Block walking speed**

<div class="spoiler"><p>Every block belongs to a surface tier and changes how fast you walk on it: asphalt 1.5 times, concrete 1.4, grating 1.3, metal, ice, cut stone and bricks 1.25, trodden paths 1.15, wood 1.1, raw rock 1.05, plain blocks 1.0, then rough ground 0.92, soft blocks 0.88, sand and dust 0.85, farmland 0.8, leaves 0.78, snow 0.75, sticky pistons 0.65, mud and clay 0.6, and slime and honey blocks 0.45. Tier members are block tags, so any mod's blocks can be sorted. Speeds are in <code>[surface.speed]</code>; the server's values are sent to players. The field of view no longer changes with these speeds (client; switch: <code>[mixins] passFov</code>).</p></div>

**Framed and copycat blocks**

<div class="spoiler"><p>Framed and copycat blocks take the walking speed of the block they copy (the one inserted most recently in a mixed block). Switch: <code>[surface.camouflage] copyCamo</code>.</p></div>

**Walking through plants**

<div class="spoiler"><p>Crops, wild crops, vines, tall plants, sugar cane, ropes, cobwebs, berry bushes, snow layers, leaf piles and bamboo leaves slow you down and soften falls as you move through them, per tier in <code>[surface.passthrough.&lt;tier&gt;]</code>. Cobwebs can hold arrows, fireworks and items, and a surrounded web can be climbed. Switch: <code>[surface.passthrough] enabled</code>.</p></div>

**Snow layers**

<div class="spoiler"><p>Snow layers can be walked through, fall like sand (Snow! Real Magic handles falling snow instead when it is installed), and are replaced by a placed block up to a set depth (<code>replaceMaxLayers</code>, default 3). Dust particles puff from soft, dusty blocks walked in or on (<code>[surface.passthrough] dustParticles</code>). Jumps are weaker inside deep snow and leaf layers, mobs sunk in snow climb out when they walk into a block, and mobs do not path through deep leaf piles.</p></div>

**Sinking**

<div class="spoiler"><p>Entities standing on full soft, dust, spongy or very sticky blocks are drawn slightly lower, and so is the camera, like soul sand. Only the picture changes; hitboxes and movement do not. Setting: <code>[surface.sinking] depthPixels</code> (client).</p></div>

**Boat friction**

<div class="spoiler"><p>A block list sets how slippery blocks are for boats only; players and other entities keep the block's own slipperiness. Section: <code>[boat_friction]</code>.</p></div>

### Melting and fire

**Melting**

<div class="spoiler"><p>Salt, fire, lava and magma melt ice and snow. Snow melts one layer at a time; ice becomes water (air in the Nether); packed ice and cryosol melt slowly, one tier down at a time, and blue ice and permafrost only by touching lava. Fire, torches, lanterns and glowstone reach 3 blocks, magma 3 (snow within 2 melts at once), lava 4, salt only the 6 touching blocks. Ice and snow also melt on their own in the Nether and in hot biomes (Creative Calendar's arid, dry and tropical season types). Snow, snow blocks and powder snow placed in the Nether turn to air at once. No snow forms and no water freezes near magma. Vanilla's light melting is off by default. With Immersive Weathering, Architect's Palette and Rare Ice installed their snowy and icy blocks melt too. Every number is in the <code>[melting]</code> config section; the key is carried over from Creative Common's file when a pack updates. Switches in <code>[mixins]</code>: <code>meltingBlockState</code>, <code>biomeMelt</code>, <code>vanillaLightMelt</code>.</p></div>

**No fire in the End**

<div class="spoiler"><p>Ordinary fire cannot be placed in the End (flint and steel, fire charges, lava, End crystals, commands), and fire already there goes out on its next tick. Soul fire is not affected. In the End every melting source only melts the blocks touching it. Settings: <code>[melting] endNoFire</code>, <code>endContactOnly</code>; switch <code>[mixins] endFire</code>.</p></div>

**Saltable tooltip**

<div class="spoiler"><p>Items of blocks that salt can melt say "Saltable" in their tooltip (client).</p></div>

### Vanilla behavior

**Iron golem farm cooldown**

<div class="spoiler"><p>When 5 iron golems die in one village within 10 minutes, the village summons no golems for 1 in-game day. Golems built by players neither count nor are affected. Section: <code>[golems]</code>.</p></div>

**Moss color**

<div class="spoiler"><p>Moss blocks and moss carpet take the biome's grass color (client). Section: <code>[moss]</code>.</p></div>

**Two flower pots in one block**

<div class="spoiler"><p>Use a flower pot on a placed vanilla-style flower pot (without sneaking) to put two pots in one block, the second one taller. Each takes its own plant, picked by the half you click, and the block breaks back into the two pots and their plants.</p></div>

**Pottable tooltip**

<div class="spoiler"><p>Items that a flower pot accepts say "Pottable" in their tooltip (client).</p></div>

**Sheared vines**

<div class="spoiler"><p>Shears on the tip of a weeping, twisting or cave vine or on kelp stop it growing, as in 1.20. Section: <code>[vine_shearing]</code>.</p></div>

**Berries**

<div class="spoiler"><p>Sweet and glow berries are listed in the creative tabs and JEI again.</p></div>

**TNT shrapnel**

<div class="spoiler"><p>A primed or burning TNT also flings out a burst of fast projectiles (default 24, 3 damage). Section: <code>[shrapnel]</code>.</p></div>

**Falling blocks splash**

<div class="spoiler"><p>Falling blocks splash when they enter water (client).</p></div>

**Nether fog color**

<div class="spoiler"><p>Client side. The whole Nether uses one deep red fog color instead of each biome's own. Lava, water, powder snow, Blindness and Darkness are left alone.</p></div>

**Screen and interface details**

<div class="spoiler"><p>Client: a tooltip taller than the screen keeps its top visible (<code>[tooltips]</code>), the world behind screens can be dimmed (<code>[screens]</code>), tutorial and advancement toasts can be hidden (<code>[toasts]</code>), sign text is not drawn far from the camera, and resource pack title plaques stretch to long container titles (<code>[gui_title]</code>).</p></div>

***

## Optional pack settings

<div class="spoiler"><p>Out of the box, the tweaks are on unless the table says otherwise. Any pack can change these in <code>config/creativecompat-common.toml</code>:</p><table><thead><tr><th>Setting</th><th>What it does</th></tr></thead><tbody><tr><td><code>[mixins]</code></td><td>One on/off key per change that alters vanilla behavior (36 keys); restart to apply</td></tr><tr><td><code>[melting]</code></td><td>Reach, chance and speed of melting by source, the block lists for what melts into what, hot places, and the End rules (<code>endContactOnly</code>, <code>endNoFire</code>)</td></tr><tr><td><code>[surface.speed]</code></td><td>Walk speed per surface tier (0.05 to 4.0)</td></tr><tr><td><code>[surface.passthrough]</code></td><td>Master switch, arrow lifetime (12000 ticks) and one table per plant tier: walk and fall factors, neighbor rule, items and projectiles</td></tr><tr><td><code>[golems]</code></td><td>Deaths (5), window (12000 ticks) and cooldown (24000 ticks) of the golem farm limit</td></tr><tr><td><code>[boat_friction]</code></td><td>Block list for boat friction</td></tr><tr><td><code>[shrapnel]</code></td><td>Count, speed, damage, item and range of TNT shrapnel</td></tr><tr><td><code>[moss]</code>, <code>[vine_shearing]</code>, <code>[gui_title]</code>, <code>[toasts]</code>, <code>[screens]</code>, <code>[tooltips]</code></td><td>Moss color, vine shearing, title plaques, toast filter, screen dimming and tooltip placement</td></tr><tr><td><code>[fleet_footed] fov</code></td><td>With Alex's Mobs (client): how much of the field-of-view widening from Fleet-Footed's speed bonus is kept (0.25; 1.0 = unchanged, 0.0 = none)</td></tr><tr><td><code>[jei] pagesOff</code></td><td>Recipe pages that other Creative mods gather (seasoning, cans, potion buckets, tipped arrows) to leave off; empty by default</td></tr><tr><td><code>[jei] craftableFirst</code>, <code>packOrder</code>, <code>fluidAmountScale</code></td><td>JEI recipe order, ingredient order from the pack's definition (off by default) and how full fluids look in tanks</td></tr><tr><td><code>[corpses]</code></td><td>Decay times and sinking of Corpse mod corpses, and the sectioned corpse screen</td></tr><tr><td><code>[trash_slot]</code>, <code>[monster_box]</code>, <code>[cats]</code>, <code>[vein_undo]</code>, <code>[paintings]</code></td><td>Settings for the matching optional-mod changes below</td></tr><tr><td><code>[wthit]</code></td><td>One switch for each added WTHIT line</td></tr><tr><td><code>[immersive_portals]</code>, <code>[portalgun]</code></td><td>Portal render distances, fog and Portal Gun portal settings</td></tr><tr><td><code>[debug]</code></td><td>Developer switches, all off</td></tr></tbody></table><p>Low gravity has no switch of its own: it follows Create: Gravity's biome settings and Creative Common's <code>[perf] gravityLimitDimensions</code> and <code>gravityDimensions</code>. Sack pick-up and glass shards have no switch either. With Create installed, the file can also be edited in game through Create's config screen.</p></div>

<!-- screenshot: the WTHIT overlay on a Supplementaries jar or a hive, and the Corpse screen with its curio slots -->

***

## Optional mods: what changes with each

None of these are required. Each change only switches on when that mod is installed. Changes that alter another mod's code are tied to the versions given (the tested version up to the next minor version); with any other version they are turned off and one warning is logged, so an update to that mod never crashes the game.

### Low gravity (Create: Gravity)

**Low gravity for falling and flying things (Create: Gravity 1.2.x)**

<div class="spoiler"><p>Wherever Create: Gravity gives a biome low gravity (the Creative Engineering pack lists the End), the same pull reduction applies to dropped items, falling blocks, lit TNT, minecarts, experience orbs, fishing bobbers, arrows, tridents, thrown items (snowballs, eggs, ender pearls, potions and the like), llama spit, Shulker bullets and boats out of water. Server and client work it out alike, so no extra network traffic is sent. Modded entities with their own gravity code are not affected. Only the dimensions in Creative Common's <code>[perf] gravityDimensions</code> are checked (default the End).</p></div>

**Low gravity from the first server tick (Create: Gravity 1.2.x)**

<div class="spoiler"><p>On a dedicated server Create: Gravity's low gravity works from the moment the server starts; without this fix it only began after a <code>/reload</code>.</p></div>

**Dropped items with ItemPhysic (Create: Gravity 1.2.x, ItemPhysic 1.6.7 or newer in 1.6.x)**

<div class="spoiler"><p>With ItemPhysic, dropped items take its own fall pull scaled by the same low gravity.</p></div>

**Air supply tags (Create: Gravity 1.2.x with Create)**

<div class="spoiler"><p>Every backtank that Create accepts as an air source (Create Goggles' backtanks included) and the two goggle diving helmets count for Create: Gravity's low-oxygen biomes.</p></div>

### Melting, drops and storage (optional mods)

**Frozen blocks of other mods (Immersive Weathering 1.2.x, Architect's Palette, Rare Ice)**

<div class="spoiler"><p>Their snowy and frosty blocks melt with the same rules and turn into their plain versions (snowy stone into stone, frosty glass into glass and so on); Rare Ice's own light melting follows the melting settings.</p></div>

**Glass shards (Quark)**

<div class="spoiler"><p>Every glass block and pane in the glass tags, modded ones included, breaks into 1 to 3 shards (panes 1 to 2) of its color without Silk Touch; Quark's own glass drops are unchanged, and tinted glass drops Quark's dirty shard instead of itself. Creative Loot also puts shards in mid-tier structure chests.</p></div>

**Sack sneak pick-up (Supplementaries 2.4.20; Supplementaries Squared for the dyed sacks)**

<div class="spoiler"><p>Sneak and right-click a placed sack with an empty main hand to pick it up with its contents, as a placed Sophisticated Backpack does. The sack's screen does not open then. Break protection (claims, spawn protection) is respected.</p></div>

### Storage, crafting and furniture

**Recipe choice (Polymorph 0.46.x)**

<div class="spoiler"><p>Recipe choice works in Sophisticated Backpacks' crafting upgrades (with Sophisticated Core 0.6.4), Immersive Engineering's crafting table and Pretty Pipes' crafting terminal.</p></div>

**Trash slot (TrashSlot 12.1.x)**

<div class="spoiler"><p>The trash slot can be turned on or off per screen and moved; each Sophisticated Backpacks tier remembers its own position. Section: <code>[trash_slot]</code>.</p></div>

**Nightstands (Handcrafted 2.0.x)**

<div class="spoiler"><p>Nightstand storage and held items save correctly.</p></div>

**Jars (Supplementaries 2.4.20)**

<div class="spoiler"><p>Jars show their contents in WTHIT and tooltips, fill and mix, and Create pumps and other fluid pipes can fill and drain them (and goblets).</p></div>

**Dyeing keeps contents (Supplementaries 2.4.20)**

<div class="spoiler"><p>Supplementaries' dye blocks tweak no longer dyes blocks that hold data (sacks, shulker boxes, banners, beds, signs and the like) in the world, so it cannot delete their contents; the click does the block's own action instead. Plain blocks still dye in the world.</p></div>

**Sacks like bundles (Supplementaries 2.4.20)**

<div class="spoiler"><p>In inventory screens, right-click a filled sack with an empty cursor to take out its first stack, and scroll over it to rotate its contents.</p></div>

**Flower and planter boxes (Supplementaries 2.4.20, Another Furniture 2.1.x)**

<div class="spoiler"><p>Flower boxes and planter boxes take and draw more plants, such as glow berries and glow lichen.</p></div>

**Ash and fodder layers (Supplementaries 2.4.20)**

<div class="spoiler"><p>Ash and fodder layers behave like snow layers.</p></div>

**Wind vane crash (Supplementaries 2.4.20 with Breezy 1.0.8)**

<div class="spoiler"><p>A wind vane crash is fixed.</p></div>

**Safe sorting (Quark 3.4)**

<div class="spoiler"><p>Sorting no longer loses items.</p></div>

**Monster boxes as vaults (Quark 3.4)**

<div class="spoiler"><p>Monster boxes can act as vaults: a reward and a cooldown of 1 hour, one wave at a time. Section: <code>[monster_box]</code>.</p></div>

**Pickarang and torch arrows (Quark 3.4)**

<div class="spoiler"><p>Pickarang and torch arrow handling adjusted for pass-through blocks.</p></div>

**Planter models (Another Furniture 2.1.x)**

<div class="spoiler"><p>Planter models draw their plants (client).</p></div>

**Framed and copycat camo (Framed Blocks 6.8.x, Copycats+ 2.2.x)**

<div class="spoiler"><p>Double-material blocks order their camo by most recent, for walking speed; flower pots in frames can be unframed.</p></div>

**Botanist workbench as a flower pot (Beautify 1.4.x)**

<div class="spoiler"><p>The botanist workbench takes any plant a flower pot takes. Workbenches placed by structures start with a flower.</p></div>

**Botany Pots growth and crops (Botany Pots 9.0.x)**

<div class="spoiler"><p>Pot crops grow 8 times slower, flowers 2 times slower. Bone meal and other pot fertilizers are removed. Rich soil speeds growth by 1.1 and Fertile Soil (Immersive Weathering) is a new pot soil at 1.2. Glow berry, hanging root and vine pot crops are removed. Pot crops are added for many modded plants, and Nether and End plants only grow in their own dimension.</p></div>

**Silt pots and bamboo leaves (Twigs 3.0.x)**

<div class="spoiler"><p>Twigs' silt pots take their soil. Bamboo leaves can be walked through.</p></div>

**Trash can screen (Trash Cans 1.0.18)**

<div class="spoiler"><p>A right-click on the Item Trash Can opens a 27-slot screen whose contents are deleted when it closes. Sneak and right-click with an empty hand opens the filter. The other trash cans are unchanged.</p></div>

**Flowers and flower tags (Bushier Flowers, Beautify, Floral Enchantment)**

<div class="spoiler"><p>Four loose flowers craft one bushy flower. Beautify's hanging pot recipe takes rich soil, and Floral Enchantment's large pot recipe is removed. Wild plants, flower crowns, azalea flower piles and Botania special flowers leave the vanilla flower tags, so bees ignore them.</p></div>

### Create and Immersive Engineering

**Block breaker and config screen (Create 0.5.1)**

<div class="spoiler"><p>Create's block breaker skips Quark monster boxes (with Quark); the Create config screen can open sections.</p></div>

**Jetpack tooltip (Create: Dreams &amp; Desires 1.2c)**

<div class="spoiler"><p>Jetpack tooltip fixed.</p></div>

**Spout Gun waters farmland (Create: Misc and Things)**

<div class="spoiler"><p>The water Spout Gun soaks the farmland it hits, including farmland under a crop.</p></div>

**Sawdust layers (Immersive Engineering 9.2.4)**

<div class="spoiler"><p>Sawdust layers fall and can be walked through.</p></div>

**Layers (Immersive Weathering 1.2.x)**

<div class="spoiler"><p>Leaf piles, sand layers and azalea flowers behave like other layers (azalea flowers with Twigs).</p></div>

### Dropped items

**Sinking, floating and burning items (ItemPhysic 1.6.7)**

<div class="spoiler"><p>Items that sink, float, burn in fire or burn in lava are chosen by item tag. Burning items can transform or explode (with shrapnel), and Allurement curses are handled.</p></div>

### Animals, villagers and companions

**Feather on a Stick (Domestication Innovation 1.6.x)**

<div class="spoiler"><p>The Feather on a Stick can hook mobs. Hostile mobs on by default; neutral and passive mobs and players off. Section: <code>[cats]</code>.</p></div>

**Cozy cats (Cozy's Improved Cats 2.1.x)**

<div class="spoiler"><p>With Cozy's gender system off, the leftover gender lines are hidden, and its two fisherman trades are removed. With Domestication Innovation, Cozy's modes replace its follow, wander and stay right-click on Cozy cats.</p></div>

**Cats and feathers (Domestication Innovation with Cozy's Improved Cats 2.1.x)**

<div class="spoiler"><p>Cats follow and attack a held feather, and get a buff from it (10 seconds, level 3 by default).</p></div>

**Squid flee whales (Alex's Mobs 1.21.x)**

<div class="spoiler"><p>Squid and glow squid in water swim away from any cachalot whale within 16 blocks and keep going until it is 24 blocks off. Orcas are not fled. Vanilla squid otherwise flee only what hurt them.</p></div>

**Giant squid fight whales (Alex's Mobs 1.21.x)**

<div class="spoiler"><p>Giant squid no longer flee whales from 50 blocks away. They target a whale that hurt them, or any whale within 12 blocks, then swim in and grab it. Cachalot whales still hunt giant squid, squid and glow squid as before.</p></div>

**Fleet-Footed per player (Alex's Mobs 1.21.1 to 1.21.x)**

<div class="spoiler"><p>Fleet-Footed's +20% speed while sprinting in the air kept its countdown in one field shared by everyone with the effect, so one sprint-jumping player kept the speed on for all of them. The countdown is now kept per entity. If an Alex's Mobs update changes the code this touches, the fix turns itself off with one warning.</p></div>

**Fleet-Footed field of view (Alex's Mobs)**

<div class="spoiler"><p>Client side. Fleet-Footed's speed bonus widens the field of view like any speed change; only a share of that widening is kept (default 0.25, 0.0 to 1.0). Other speed changes widen the view as usual. Setting: <code>[fleet_footed] fov</code>, carried over from Raspberry Pie's old key.</p></div>

**Villager paths and combs (MCA Reborn 7.5.x)**

<div class="spoiler"><p>Villagers path over leaf piles; eye colors on combs fixed.</p></div>

**Hats (Simple Hats 0.3.x)**

<div class="spoiler"><p>Masks, the chalk stick and the smoking pipe can go in the Curios face slot, and the Falconry Glove in a hands slot.</p></div>

### Corpses

**Corpse decay (Corpse 1.0.17)**

<div class="spoiler"><p>Corpses decay by game time, also while unloaded: a corpse holding items becomes a skeleton after 7 days (1 day in the Nether, then a wither skeleton), an emptied one after 30 seconds and is gone after 5 minutes; in the End it never decays. Skeletons sink in water and lava. Section: <code>[corpses]</code>.</p></div>

**Corpse screen (Corpse 1.0.17 with Curios 5.1.x)**

<div class="spoiler"><p>The corpse screen shows cosmetic armor and curios in their own slots, and both models wear them. Transfer Items returns curios to their slots.</p></div>

### Mining and maps

**Vein mine undo (Ore Excavation 1.11.x)**

<div class="spoiler"><code>/undo_excavation</code> has a cooldown (5 seconds) and refuses when the vein mine broke ores, storage blocks, spawners or blocks with inventories. Section: <code>[vein_undo]</code>.</div>

**Speeds and field of view (BlockRunner 4.2.x)**

<div class="spoiler"><p>Speeds come from <code>[surface.speed]</code>, and the field of view stays steady.</p></div>

**Map colors (Xaero's World Map 1.40.x)**

<div class="spoiler"><p>Map block colors fixed.</p></div>

**Clouds and the Nether (Distant Horizons 3.0.x)**

<div class="spoiler"><p>Distant Horizons' clouds follow the vanilla cloud height. In the Nether it generates no unvisited chunks, draws to 32 chunks and keeps a fog at the edge.</p></div>

**Chakrams (Botania 440)**

<div class="spoiler"><p>Chakrams pass through plants.</p></div>

### Portals

**Rendering limits (Immersive Portals 2.3.x)**

<div class="spoiler"><p>Mirror and portal render limits and fog settings. Walking out of a wall portal no longer lifts the player a block. Section: <code>[immersive_portals]</code>.</p></div>

**Portal Gun (Portal Gun 5.0.x)**

<div class="spoiler"><p>Opaque portals optional, a despawn distance and heartbeat, a dye recipe and its JEI page. Section: <code>[portalgun]</code>.</p></div>

**Animated items in the hotbar (GeckoLib 3.1.x with ImmediatelyFast 1.4.x)**

<div class="spoiler"><p>Animated items in the hotbar no longer break the HUD.</p></div>

### Paintings and banners

**Painting variants (Immersive Paintings 0.6.x)**

<div class="spoiler"><p>Every painting variant of the installed mods is offered, built at load from their own art; old painting ids are kept. Section: <code>[paintings]</code>.</p></div>

**Banner patterns (Bedspreads 6.0.x, Customizable Elytra 1.7.x)**

<div class="spoiler"><p>Their banner patterns are built at load from the installed jars.</p></div>

### JEI, WTHIT and tooltips (client)

**JEI**

<div class="spoiler"><p>Recipes whose + button works are listed first, fluids in tanks scale by a curve so small amounts stay visible, recipe and ingredient tooltips are fixed, and an optional pack ingredient order. Section: <code>[jei]</code>.</p></div>

**Effect descriptions (Just Enough Effect Descriptions 2.1.x)**

<div class="spoiler"><p>Effect descriptions and icons.</p></div>

**WTHIT**

<div class="spoiler"><p>Lines for jar and pot contents, beehives, block state, walking speed, silk touch and sheared drops, operator blocks, campfires and more; each has a switch in <code>[wthit]</code>.</p></div>

**Tooltips (FTB Library 1902.4.x, FTB Quests 1902.5.x, Patchouli 77, Applied Energistics 2 12.9.x, Wares 1.2.x, Pretty Pipes 1.13.x, Lightman's Currency 2.2.x, Legendary Tooltips 1.4.x with Iceberg)**

<div class="spoiler"><p>Item tooltips keep their top on screen and use the pack's tooltip lines; Lightman's Currency screens and Legendary Tooltips title breaks are fixed.</p></div>

***

## Source, issues and license

*   Source code and issue tracker: [github.com/SaintAlfonzo/CreativeEngineering](https://github.com/SaintAlfonzo/CreativeEngineering)
*   License: GNU GPL v3 (GPL-3.0-only). Third-party notices for included art are in the jar.
*   Free to include in modpacks.
