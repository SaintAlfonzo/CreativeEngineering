# Scratchpad



- todo review (dont move this)
    - finished nested cd jar
    - need to do standalone with integration and standalone w/o

    list {2. Standalone third-party Delight jars, with pack integration
Mod	Licence	State
Ender's Delight 1.2.2	All rights reserved	Planned donor-route member (members #33, 38.1). Nothing built. The old jar stays in mods/ until it is, and the enders_additions member folds into it.
Fruits Delight 0.5.11	All rights reserved	Pack code and the 1.1.3 fixes live in CD folders. Decided 2026-11-06: status quo (standalone jar); only an LGPL port of 1.1.3 or the donor route could nest it.
Farmer's Respite 2.0	MIT	CD scripts and the drink rules cover it. A repack is possible.
Corn Delight 1.0.3	MIT	Same as Farmer's Respite.
More Delight	MIT	Decided 2026-11-06: repack (knife tiers, fast toasts). See creative_delight.md 38.2.
Abnormals Delight 4.1.2	Custom (link)	Needs a licence check.
Alex's Delight	MIT	CD script rows only.
Quark Delight	All rights reserved	CD script rows only. Donor route only.
Hostile Delight 0.2.2	AFL 3.0	Plan written (D1–D16), "not yet integrated". Decided 2026-11-06: repack with replaced classes (Canned Goods route).
Storage Delight	MIT	Built as repackedStorage, but its own jar and not nested.
3. Standalone, with no CD folder or integration
Chefs Delight (MIT)
Engineers Delight (MPL 2.0)
KubeJSDelight (LGPL 3.0)
4. Cooking-adjacent, not Delight add-ons (stay separate)
Cooking for Blockheads: all rights reserved, so only the blockheads_additions member goes in CD.
Applied Cooking
Golden Food
Create Central Kitchen
Farmer's Structures: has its own donor handling.
Canned Goods
SoL: Apple Pie
Salt and Create Salt}

Planned rows, ready vs. needing a decision
"Ready" means the Decisions cell says "none". That is about 168 of the 421 Planned rows (my count of the table, not a documented figure).

Category	Ready to build	Waiting on a decision
Items and Blocks	34	22
Food Rules	19	11
Interface and JEI	19	10
Farming	14	21
Mods and Ports	10	30
Tech	10	14
Mobs	9	12
Loot and Trade	7	9
Tooling	7	7
Structures	6	18
Dishes and Ingredients	5	12
Alcohol	5	5
Worldgen	4	8
Kitchen and Cooking	3	5
Gear and Combat	3	16
Potions and Effects	3	15
Mechanics	3	12
Drinks and Drugs	2	4
Recipes and Materials	2	14
Creative Delight	2	3
Baking and Sweets	1	3
Creative Mods	1	5

## Writeup plans (add these to writeups and todo)

## Idea, Notes, Changes (Human scratchpad, don't write up or add to plans unless directed)

- download jars to mods folder then move to an upstream folder system
- unify all upstream folders into /upstream
- move block runner, detail armor bar to compat jar
- hints on loading
- hints announced in chat on server
- increase crow, crow nest spawns in villages
- missing lootr in wooden nether structures
- monazite rod uses
    - hard to obtain nether resource (hanging above lava lakes, add a tooltip)
- pitchfork
    - exempt flying mobs
    - lower to 3 seconds for players
    - exempt bosses
    - exempt swimming mobs

- portal gun, tempad, as creative computing endgame goal?
    - add crafting recipes that require computing/electronics components (output of complex production chains of standard step-by-step recipes) and complex processes (sequenced assembly w/ very high scrap/recycle rate, automation of niche inworld processes, items with timing requirements, microcrafting steps for extra efficiency intended for autocrafters, pc etching and projectred lithography)

- portal changes
    - nether portal
        - immersive portals behavior overrides custom failure behavior
        - cut through portal render distance
        - apply nether portal blocks in portal for tint, apply fog in other dimension to mask short render distance
    - portal gun
        - block needs name
        - player jumps 1 block upward after leaving a portal when walking
        - portal gun is desaturated in hotbar, desaturates every item to the left of it on the hotbar
        - dyable with dye? backpack part-dyeing mechanic
            - change item model later
        - opaque portal option
            - for performance

    - mirrors
        - block needs name
        - rename d&d tinted glass types to smooth tinted glass
        - decrease mirror render distance (distance from player that it renders, how far into reflections and number of bounces) by config for performance
        - missing
            - tinted 
                - vanilla, design and decor
            - quark framed (just blocks?)
            - dirty glass (just block?)
            - frosted glass (needs shift click, flint defrosts, just block?)
            - rainbow glass

- farmers delight pot, kettle, copper pot keep contents on pickup
    - kettle keeps fluid, ingredients, output
    - pots keep input and output
    - overflow slots drop on break
    - items containing items are glinted w/ enchantment, dont stack

- investigate repack/donor jar fruits delight, hostile delight, more delight
- move worldgen to own jar or common?
- config option to show season speed up/slow down in wthit for current season
- crop growth penalty below y level  48
- waystones requrie diamond/netherite pickaxe to break
- integrate tilled endstone w/ all end crops

- wine bottle size tooltip (small, medium, large bottle)
- expand stomping heavy armor list, move to new h&h stsomping block?
- harmony effect? add to player lit or signal campfire, more?

- dimension bound enchantment
    - applied to items given in tutorial dimension, puzzle dimensions for temporary items

- ensure copper gear is waxable, prevents copper poisoning
- add tooltip to oxidized gear warning of copper poisoning

- ensure swap system allows players running unmodified versions of delight, other mods to swap items to new variants. lower priority, swap for every merge and some removes
- manure buildup system
    - required hearth and harvest manure
    - manure can build up as layers if too little space is in pen or manure not cleared consistanly enough
        - manure mobs pathfind to open space to drop manure
        - crammed mobs (touching other mobs, unable to pathfind to 3x3 clear space) drop manure layers instead of items with a chance

- hints (2026-11-06)
    - chat announcements: add a KubeJS server script that broadcasts a random tip to chat on a timer (hosted server); no extra mod. Now TODO ui/loadingtips #2.
    - loading screen tips: DONE 2026-11-06 (built, not installed): Creative Common loadingtips/ + built-in packs Loading Tips (340 filtered tips) and Creative Tips; see feature_writeup/loading_tips.md, list in loading_tips_list.md.

