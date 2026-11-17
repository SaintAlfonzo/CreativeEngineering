# Scratchpad

## Prompt scratchpad, dont touch

add a saltable tooltip/tag that handles salting (result is handled by lookup and returns air if not listed) and remove saltable from permafrost. id like the melt speed to appear faster, id like to increase the number of random ticks meltable items experience on melting, id like an investigation into the efficiency and performance of possible methods. another method could be increasing random ticks for blocks identified by placed heat source. magma should stop snow forming, increase snow instance melt below distance to 2 blocks (snow layers are melted instantly or not melted, on edge of heat range correct?). can performance of permafrost and cryosol melting be optimized if they mirror blue and packed ice and dont experience most melting? can the heat source originating melt behavior work here? i want snow layers to melt by layer, with a higher chance per layer (configurable). rename rock salt ores to 'Rock Salt Vein' and 'Deepslate ...', rock salt is then the name of the item. can undamaged salt melt be stackable? salt evaporation doesnt match its new boosted rate in jei. bamboo has leafy blocks at the top few blocks of grown bamboo, and these bamboo drops twigs bamboo leafs when cut. id like the 4 blocks adjacent to the leafy blocks to have periodic leaf drops and a leaf drop chance like other trees, but with 'pseudo' leaf blocks that dont trigger if another block occupies the space. stack the 4 layer model on top of itself to make a full block version that can be filled like any other leaf stack. are the full layer version of snow and the normal snow block not different? drop the powdered snow conversion if not. mark the saltmelt as a recolored 'the salt' texture in the texture sources. all layers should experience gravity like immersive weatherings and stack by gravity, including salt. i am ok with the messy salt lick path, the regression is expected. change the current acid recips back to heated, and keep the salt w/ rock salt block catalyst heated. implement the slowdowns for any walkthrough with its own block tier for walking on top and through: on is the same as the material, through is slower for leaves and snow, increasing walk through slowness (not on top walk speed) as height increases to 1 less than full block cap, snow layers turn to snow block (presumably) which cant be walked through, and leaf layers stay as layer block that can be walked through with new tweaks. 

## Write up scratchpad, add to the desigated plan: (plan_post_scratch_1115.md )

- misc
    - magma blocks keep snow off of blocks above them, melt quickly
        - placed below a stone pathway for a 'heated pathway' that doesnt let snow layers form from snowing. also prevents snowy form on adjacent blocks
    - snow and snow layers dont melt on blue ice/permafrost
    - boost global melt speed to configurable rate, 5x by default
    - add 'requires a spark or flame to light' tooltip to lightable campfires
    - allow torches to light campfires
    - boost dragon armor defense by 2 armor bars (4 armor points?) to fill out 2 layers of visual armor bars
    - immersive weathering cryosol melts like packed ice (to dirt) and perrmafrost melts like blue ice (to dirt)
    - rare ice cant be made by placing items in ice, only found in world due to worldgen

- salt
    - tie salt jei displayed yield to configured yield
        - update other jei tabs that are out of date, including salts
    - salt/acid issue
        - remove water-> salt mixing recipe
        - add heated water + rock salt block -> rock salt block + 2 salt recipe
    - keep salt block as salt dust block (rename)
    - where did salt melt texture come from?
    - rename raw rock salt to rock salt
    - rock salt block (new name) is the root block of the salt lick
        - rock salt (new name) item can be used or dispensed to fill salt lick, remove salt layer refilling
    - rock salt block is 4 rock salt
    - salt lamp takes rock salt item
        - swap deepslate for wooden slab
    - rename vintage salt decor to smooth salt, keep mixed salt names as is. 
    - salt lick renamed to '{waxed} {heavily} licked rock salt block', keep rock salt block as full block name
    - salt melt item and mechanics apply to immersive weathering snowy conditional blocks 
    - salt melt stacks to 16 when durability matches

- layers
    - ensure blockrunner materials
    - walkthrough only applies to leaves and snow
    - ensure no walkthrough for salt, sands, ash, fodder
    - salt, sands, ash are dust material
    - snow is snow material
    - full snow layer blocks turn to powder snow
    - immersive weathering drops twigs bamboo leaves rarely, allow to stack to a full block
    - layered snow melts by layer
    - leaf layers never become solid block, remove the tweaks related to this
    - leaf layer blocks provide powdered snow-like overlay (experimental) and a mild 'thorny' damage when moving
        - ensure transparancy of leaf layer blocks can be seed from the inside 
    - leaf and snow layers cut jump height above half layer height, slow fall speed inside by number of layers 
    - leather boots allow standing on any snow layer or powdered snow (walkthrough otherwise)
        - crouch to fall through like scaffold

- raspberry pie
    - switch back to single page
    - scale book page size to configurable size, default 125% (icons and text extend off book)
    - diet buffs screen
        - move diets slots horizontally
        - add spacing between permanant buffs and buff label
        - drop 'needs diversity' line, replace with simplified progress line
            - stretches length of all slots, tick marks at slots with diversity label
            - green bar extends from 0 to current diversity, and maxes out at the highest slot diversity on this screen
    - use monobank replacement lock if loaded for permanant unlockable icon, otherwise awkward potion (configurable)
    - use create crush everything diamond shard if loaded for permanant buff icon, otherwise diamond
    - use shield for kb resistance and show in key
    - use netherite armor for toughness
    - use supps 2 metal frame if loaded for unlockable slot icon, otherwise an item frame 

- add language 2
    - saltable tooltip
    - remove saltable from permafrost

## Loading Tooltip language

- 'Spyglasses, made from amethyst and copper, can be used zoom in on distant things';  
- 'Ring a bell, by hand or with a projectile, to send villagers indoors and make nearby raiders glow'; 

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- only netherite lunchbox is added currently, remove apple pie versions
    - add diamond?

- ensure all nether crops grow on nether soil
- new adzuki rich soil, peanut soil?

- ensure quark endermen block pickup nerf is enabled

- spiders can pathfind and navigate through any web?'
- investigate philips frozen ocean monument
    - place palette swapped monument in cold oceans?
- rare ice investigation, loot table, part of loot tune

- grilled food 
    - add other meats?
    - bonus: 
        - allows extra seasoning slots?
        - 

- dense effect
    - given by diving boots
        - could just be a visual indicator 'false effect' that shows when diving boots behavior is active, determine how hard a new effect would be
        - 'Increases sinking speed in water. Defying conventional physics, also increases fall speed in the end' description on jei effect

## Power grid notes, dont move

- wire block doenst drop when block under breaks and cant be broken by hand
    'Block wire' in wthit

## Tempad/portals WIP dont move

- tempad notes
    - change chronons to [chronitons](https://futurama.fandom.com/wiki/Chronitons)
    - allow knowledge projector recipes to update based on actual recipe
    - change knowledge projector lore (remove TVA)
    - change tempad time to in game time
    - multiple chronometers cancel out chronitons charging
    - add braclet curios slot
    - enlarge knowledge projector screen
    - fix 'hold {X} chronotons' knowledge projector
    - cant remove card from wallet?
    - tooltips for every item, based on knowledge projector
    - timedoor marker
        - name and color fields dont render until clicked, color updates inconsistanly when using color selection screen
    - rename timesteel to temporal steel
        - change recipe to felsic/steel alloy
            - ender pearl or ender grain additive?
    - mark a note to make recipes much much more complex, intermediate steps, unusual craft steps
    - change 
    - potential issues
        - default app doesnt work?
        - only 1 macro option?
        - workstation is locked?
        - cant use empty or marked location card on tempad
    - change knowledge base categories (iron vs steel) to basic vs advanced. make advanced recipes harder and nerf basic storage capacity or etc?
    - use projector in timedoor projector, timedoor projector in tempad
    - swap timedoor projector door projection angle 180 degrees (behind station., not ahead)
    - need to test location boradcaster in multiplayer
    - ensure all apps are added to tempad
    - timeline missing languange in tempad

- portal changes
    - dont restrict portal placement by location of the color being moved
        - a blue portal cant be shot so as to make a blue portal move up or down in the same spot

- tempad in server
    - chronomarker used for mining/resource dimension navigation, more valuable resource dimensions availible w/ own tempad (unlock location cards? wandering trader rarely, or found in end chests?)


## Golden Buffs, WIP Dont move

- golden food buffs
    - all effects and saturation/hunger are identical within family
    - fruit
        - golden: regen 2 5s,  speed 2 30s, nourishment 1 30s
        - gilded: regen 3 20s, speed 3 60s, nourishment 1 60s
    - veggie
        - golden: regen 2 5s,  jump boost 2 30s, nourishment 1 30s
        - gilded: regen 3 20s, jump boost 3 60s, nourishment 1 60s
    - meat/fish
        - golden: absorption 2 60s,  resistance 2 15s, nourishment 1 60s
        - gilded: absorption 4 120s, resistance 4 60s, nourishment 1 120s
    - sweets
        - golden: regen 2 5s,  speed 2 15s, nourishment 1 30s
        - gilded: 
    - meals
        - golden: regen 2 5s,  speed 2 15s, nourishment 1 30s
        - gilded: 

    - exceptions
        - golden carrot: jump boost to night vision (60/120s)

- current gold food buffs

    - Current effects (from kubejs/exported/food/foods.json dump, 2026-11-07). Hunger shown as golden / gilded.

    - Standard golden effects: 
            Regeneration II 5s, Absorption I 120s, Nourishment I 120s

    - Standard gilded (enchanted) effects: 
            Regeneration II 20s, Resistance I 300s, Fire Resistance I 300s, Absorption IV 120s, Nourishment I 240s

    - Every item below has the standard effects unless a note says otherwise.

    - fruit
        - Golden / Gilded Apple (8 / 16)
        - Golden / Gilded Melon Slice (4 / 8)
        - Glistering Melon Slice (4): Nourishment I 120s only (pack item, no gilded form)
        - Golden / Gilded Sweet Berries (4 / 8)
        - Golden / Gilded Glow Berries (4 / 8)
        - Golden / Gilded Chorus Fruit (8 / 16)
    - veggie
        - Golden / Gilded Carrot (6 / 12)
        - Golden / Gilded Beetroot (2 / 4)
        - Golden / Gilded Baked Potato (10 / 20)
        - Golden / Gilded Dried Kelp (2 / 4)
        - Golden Moss Clump (4): Regeneration II 10s, Nourishment I 120s (no absorption)
        - Gilded Moss Clump (6): Absorption III 80s, Regeneration II 15s, Water Breathing III 80s, Nourishment I 240s (not the standard gilded set)
    - meat/fish
        - Golden / Gilded Cooked Beef (16 / 20)
        - Golden / Gilded Cooked Porkchop (16 / 20)
        - Golden / Gilded Cooked Mutton (12 / 20)
        - Golden / Gilded Cooked Salmon (12 / 20)
        - Golden / Gilded Cooked Chicken (12 / 20)
        - Golden / Gilded Cooked Rabbit (10 / 20)
        - Golden / Gilded Cooked Cod (10 / 20)
    - sweets
        - Golden / Gilded Cookie (4 / 8)
        - Golden / Gilded Honey Bottle (12 / 20)
        - Golden / Gilded Pumpkin Pie (4 / 8 in the dump; golden_food.md 3.6 expects 16 / 20 from the pie's input, check whether the dump is stale)
        - Golden Cake slice (Java, GoldenCakeBlock): 4 hunger, Regeneration II 5s, Absorption II 120s
        - Enchanted Golden Cake slice: 8 hunger, Regeneration II 20s, Resistance I 300s, Fire Resistance I 300s, Absorption IV 120s
        - Golden Roll / Golden Roll Slice (Roll Delight): no effects
    - meals/prepared
        - Golden / Gilded Bread (10 / 20)
        - Golden Nutritional Bar (10): Nourishment I 120s only (Miner's Delight, no gilded form)
    - not sorted into a family
        - Golden Coffee Beans (Rustic Delight): no effects
        - Golden / Gilded Milk Bucket: listed with the standard effects but they never apply (milk clears effects, never "eats")
    - NO SOUPS OR STEWS, these are removed. Still present in the dump with the standard effects: Golden / Gilded Beetroot Soup (12 / 20), Mushroom Stew (12 / 20), Rabbit Stew (20 / 20)
    - Canned versions (CE canned goods) carry scaled-down effects: Regeneration II 1.25s, Absorption I 30s, Nourishment I 30s (moss can: Regeneration II 2.5s, Nourishment I 30s; nutritional bar can: Nourishment I 30s). Canned Golden Apples (canned_goods) has no effects in the dump.
